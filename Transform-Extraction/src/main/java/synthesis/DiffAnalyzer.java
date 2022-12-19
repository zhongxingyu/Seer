package synthesis;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import add.entities.PatternInstance;
import add.entities.RepairPatterns;
import add.features.detector.EditScriptBasedDetector;
import add.features.detector.repairpatterns.RepairPatternDetector;
import add.main.Config;
import add.main.TimeChrono;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtElement;

public class DiffAnalyzer {

	File out = null;
	private Logger log = Logger.getLogger(this.getClass());

	public DiffAnalyzer (String outfile) {
		if (!Files.isDirectory(Paths.get(outfile))) {
			out = new File(outfile);
			out.mkdirs();
		}	
	}

	public List<Pair<CtElement, ArrayList<PatternInstance>>> run(String path) throws Exception {

		File dir = new File(path);
		TimeChrono cr = new TimeChrono();
		cr.start();
		Map<String, Diff> diffOfcommit = new HashMap();

		processDiff(dir, diffOfcommit);
		List<Pair<CtElement, ArrayList<PatternInstance>>> elementRepairInfo = atEndCommit(dir, diffOfcommit);	
		return elementRepairInfo;
	}

	public void processDiff(File difffile, Map<String, Diff> diffOfcommit) {

		String pathname = difffile.getAbsolutePath() + File.separator + difffile.getName();
		File previousVersion = new File(pathname + "_s.java");
		File postVersion = new File(pathname + "_t.java");
			
		try {
			Diff diff = getdiffFuture(previousVersion, postVersion);
			String key = difffile.getParentFile().getName() + "_" + difffile.getName();
			diffOfcommit.put(key, diff);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public Diff getdiffFuture(File left, File right) throws Exception {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		Future<Diff> future = getfutureResult(executorService, left, right);

		Diff resukltDiff = null;
		try {
			resukltDiff = future.get(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("job was interrupted");
		} catch (ExecutionException e) {
			log.error("caught exception: " + e.getCause());
		} catch (TimeoutException e) {
			log.error("timeout");
		}

		executorService.shutdown();		
		try {
		    if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
		        executorService.shutdownNow();
		    } 
		} catch (InterruptedException e) {
		    executorService.shutdownNow();
		}
		
		return resukltDiff;
	}

	private Future<Diff> getfutureResult(ExecutorService executorService, File left, File right) {
		Future<Diff> future = executorService.submit(() -> {
			AstComparator comparator = new AstComparator();
			return comparator.compare(left, right);
		});
		return future;
	}

	public List<Pair<CtElement, ArrayList<PatternInstance>>> atEndCommit(File difffile, Map<String, Diff> diffOfcommit) {
		try {
			List<Pair<CtElement, ArrayList<PatternInstance>>> statsjsonRoot = getContextFuture(difffile.getName(), diffOfcommit);
			return statsjsonRoot;
		} catch (Exception e) {
			return null;
		}
	}

	public List<Pair<CtElement, ArrayList<PatternInstance>>> getContextFuture(String id, Map<String, Diff> operations) throws Exception {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		Future<List<Pair<CtElement, ArrayList<PatternInstance>>>> future = getInfo(executorService, id, operations);

		List<Pair<CtElement, ArrayList<PatternInstance>>> result = null;
		try {
			result = future.get(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			log.error("job was interrupted");
		} catch (ExecutionException e) {
			log.error("caught exception: " + e.getCause());
		} catch (TimeoutException e) {
			log.error("timeout context analyzed.");
		}

		executorService.shutdown();	
		try {
		    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
		        executorService.shutdownNow();
		    } 
		} catch (InterruptedException e) {
		    executorService.shutdownNow();
		}	
		return result;
	}

	private Future<List<Pair<CtElement, ArrayList<PatternInstance>>>> getInfo(ExecutorService executorService, String id,
			Map<String, Diff> diffOfcommit) {

		Future<List<Pair<CtElement, ArrayList<PatternInstance>>>> future = executorService.submit(() -> {
			List<Pair<CtElement, ArrayList<PatternInstance>>> statsinfo = 
					calculateElementRepairInfo(id, diffOfcommit);
			return statsinfo;
		});
		return future;
	}

	public List<Pair<CtElement, ArrayList<PatternInstance>>> calculateElementRepairInfo(String id, Map<String, Diff> operations) {
		
		List<Pair<CtElement, ArrayList<PatternInstance>>> elementRepairInfo = new 
				ArrayList<Pair<CtElement, ArrayList<PatternInstance>>>();
		
		for (String modifiedFile : operations.keySet()) {
			List<PatternInstance> patternInstances = new ArrayList<>();
			Diff diff = operations.get(modifiedFile);
			if(diff.getRootOperations().size()<=10) {
			   Config config = new Config();
			   EditScriptBasedDetector.preprocessEditScript(diff);
			   TimeChrono cr = new TimeChrono();
			   cr.start();
			   RepairPatternDetector detector = new RepairPatternDetector(config, diff);
			   RepairPatterns rp = detector.analyze();

			   for (List<PatternInstance> pi : rp.getPatternInstances().values()) {
				  patternInstances.addAll(pi);
			   }
			   cr.start();
			   elementRepairInfo = getElementRepairInfo (diff, patternInstances);
		   }
		}
		return elementRepairInfo;
	}

	public List<Pair<CtElement, ArrayList<PatternInstance>>> getElementRepairInfo(Diff diff, 
			List<PatternInstance> patternInstancesOriginal) {
		
		List<Pair<CtElement, ArrayList<PatternInstance>>> elementRepairInfo = new 
				ArrayList<Pair<CtElement, ArrayList<PatternInstance>>>();
		List<PatternInstance> patternInstancesMerged = merge(patternInstancesOriginal);

		for (PatternInstance patternInstance : patternInstancesMerged) {
	
			CtElement getAffectedCtElement = patternInstance.getFaultyLine();		
			CtElement tostudy = retrieveElementToStudy(getAffectedCtElement);		
			if(whetherDiscardElement(getAffectedCtElement))
			       continue;
			
			ArrayList<PatternInstance> statPatterns = new ArrayList<PatternInstance>();
			
			for(int index=0; index<patternInstancesOriginal.size(); index++) {
				if(patternInstancesOriginal.get(index).getFaultyLine().equals(getAffectedCtElement))
					statPatterns.add(patternInstancesOriginal.get(index));
			}
			
			elementRepairInfo.add(Pair.of(tostudy, statPatterns));
		}
		return elementRepairInfo;
	}
	
	private boolean whetherDiscardElement(CtElement tostudy) {
		
		List<CtBinaryOperator> allbinaporators = tostudy.getElements(e -> (e instanceof CtBinaryOperator)).stream()
				.map(CtBinaryOperator.class::cast).collect(Collectors.toList());	
		if(allbinaporators.size()>=3) {		
			for (CtBinaryOperator anbinaryoperator: allbinaporators) {			
				int numberString=0;			
				CtElement parent = anbinaryoperator;				
				do {
					numberString+=getNumberOfStringInBinary((CtBinaryOperator)parent);
					parent=parent.getParent();		
				} while (parent instanceof CtBinaryOperator &&
						((CtBinaryOperator) parent).getKind().equals(BinaryOperatorKind.PLUS));
				
				if(numberString>=4)
					return true;
			}
		}		
		return false;
	}
	
	private int getNumberOfStringInBinary (CtBinaryOperator binarytostudy) {
		
		int stringnumber=0;	
		if(binarytostudy.getKind().equals(BinaryOperatorKind.PLUS)) {			
		   if(binarytostudy.getLeftHandOperand() instanceof CtLiteral && 
				((CtLiteral) binarytostudy.getLeftHandOperand()).toString().trim().startsWith("\""))
			 stringnumber++;	
		   if(binarytostudy.getRightHandOperand() instanceof CtLiteral && 
				((CtLiteral) binarytostudy.getRightHandOperand()).toString().trim().startsWith("\""))
			stringnumber++;	   
		}
		return stringnumber;
	}
	
	private CtElement retrieveElementToStudy(CtElement element) {

		if (element instanceof CtIf) {
			return (((CtIf) element).getCondition());
		} else if (element instanceof CtWhile) {
			return (((CtWhile) element).getLoopingExpression());
		} else if (element instanceof CtFor) {
			return (((CtFor) element).getExpression());
		} else if (element instanceof CtDo) {
			return (((CtDo) element).getLoopingExpression());
		} else if (element instanceof CtForEach) {
			return (((CtForEach) element).getExpression());
		} else if (element instanceof CtSwitch) {
			return (((CtSwitch) element).getSelector());
		} else
			return (element);
	}

	private List<PatternInstance> merge(List<PatternInstance> patternInstancesOriginal) {
		
		List<PatternInstance> patternInstancesMerged = new ArrayList<>();
		Map<CtElement, PatternInstance> cacheFaultyLines = new HashMap<>();

		for (PatternInstance patternInstance : patternInstancesOriginal) {
			if (!cacheFaultyLines.containsKey(patternInstance.getFaultyLine())) {
				cacheFaultyLines.put(patternInstance.getFaultyLine(), patternInstance);
				patternInstancesMerged.add(patternInstance);
			}
		}
		return patternInstancesMerged;
	}
}