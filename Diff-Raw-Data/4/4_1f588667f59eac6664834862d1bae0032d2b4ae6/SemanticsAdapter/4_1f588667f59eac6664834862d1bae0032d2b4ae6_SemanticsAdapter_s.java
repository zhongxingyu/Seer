 package core.jtester.ontology.adapter;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import core.common.model.jobflow.IJob;
 import core.common.model.jobflow.JobConst;
 import core.common.model.semantics.DIPair;
 import core.common.model.semantics.DeclarationSemantics;
 import core.common.model.semantics.InferenceSemantics;
 import core.common.model.semantics.SemanticsStore;
 import core.common.model.test.TestData;
 import core.common.model.test.TestFile;
 import core.common.model.test.TestResult;
 import core.common.model.test.TestResultItem;
 
 /**
  * SemanticsAdapter builds a bridge between static analysis result and semantics
  * 
  * еľָ̬֧ͱķǲûм¼ͣ˷ֻǵ㷨Ӧõ˱ϡ
  * 
  * ٵǣ ©ҪϢʱ о̬ȴʧⲿϢ
  * 
  * ҵĽ һбϢļϣٸݱƥϾ̬ʹǵĽŻϡ
  * 
  * SemanticsAdapterľƥŻĹ
  * 
  * @author XingxuWu
  *
  */
 public class SemanticsAdapter implements IJob{
 	private String name = this.getClass().getName();
 	
 	public boolean run(TestData data) {
 		TestFile file = data.getCurrentTestFile();
 		SemanticsStore store = (SemanticsStore) file.get(JobConst.SEMANTICS);
 		TestResult result = data.getTestResult();
 		List<TestResultItem> items = result.getCurrentFileTestResult();
 		refineSemantics(store, items);
 		return true;
 	}
 
 	private void refineSemantics(SemanticsStore store, List<TestResultItem> items) {
 		List<DIPair> diPairs = getDIPair(items);
 		
 		for(DIPair pair: diPairs){
 			int line = pair.getLine();
 			String name = pair.getName();
 			int declarationLine = pair.getDeclarationLine();
 			
 			Iterator<InferenceSemantics> ir = store.iterator2();
 			while(ir.hasNext()){
 				InferenceSemantics is = ir.next();
 				if(is.getLine() == line && is.getName().toString().equals(name)){
 					Iterator<DeclarationSemantics> ir2 = store.iterator1();
 					while(ir2.hasNext()){
 						DeclarationSemantics ds = ir2.next();
 						if(ds.getLine() == declarationLine && ds.getName().toString().equals(name)){
 							is.addDeclaration(ds);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private List<DIPair> getDIPair(List<TestResultItem> items){
 		List<DIPair> pairs = new ArrayList<DIPair>();
 		
 		for(TestResultItem item: items){
 			int lineNumber = Integer.parseInt(item.getDetail().get(0));
 			
 			String content = item.getDetail().get(3);
 			Pattern pattern = Pattern.compile("\\(.*?\\)");
 			Matcher matcher = pattern.matcher(content);
 			
 			while(matcher.find()){
 				String text = matcher.group();
 				text = text.substring(1,text.length()-1);
 				if(!text.isEmpty()){
 					String name = text.split(",")[0];
 					int line = Integer.parseInt(text.split(",")[1]);
 					pairs.add(new DIPair(lineNumber, name, line));
 				}
 			}
 		}
 		return pairs;
 	}
 
 	public String getName() {
 		return name;
 	}
 }
