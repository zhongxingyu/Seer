 package histaroach.algorithm;
 
 import histaroach.model.DiffFile;
 import histaroach.model.IntermediateRevision;
 import histaroach.model.Revision;
 import histaroach.model.Revision.Compilable;
 import histaroach.model.TestResult;
 import histaroach.util.Pair;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 
 /**
  * IntermediateRevisionAnalysis performs file manipulation to create actual 
  * intermediate revisions on the file system, runs tests on them and records 
  * the results and all data necessary for Histaroach analysis.
  */
 public class IntermediateRevisionAnalysis {
 		
 	private static final String COLUMN_SEPARATOR = ";";
 	private static final String FILE_SEPARATOR = ",";
 	
 	private static final String HEADER1 = 
 		"IID;parentCommitID;childCommitID;delta;" + 
 		"compilable;testAborted;test;intermediateTestResult;parentTestResult;childTestResult\n";
 	private static final String HEADER2 = "parentCommitID;childCommitID;totalDelta\n";
 	
 	private static final String TRUE = "1";
 	private static final String FALSE = "0";
 	private static final String NONE = "n";
 
 	private final List<IntermediateRevision> intermediateRevisions;
 	private final BufferedWriter out1;
 	private final BufferedWriter out2;
 	
 	public IntermediateRevisionAnalysis(List<IntermediateRevision> intermediateRevisions, 
 			File outputFile1, File outputFile2) throws IOException {
 		this.intermediateRevisions = intermediateRevisions;
 		out1 = new BufferedWriter(new FileWriter(outputFile1));
		out2 = new BufferedWriter(new FileWriter(outputFile2));
 	}
 	
 	/**
 	 * For all IntermediateRevisions, creates actual intermediate revisions 
 	 * on the file system, runs tests on them and records the results to 
 	 * the output file.
 	 * 
 	 * @throws Exception
 	 */
 	public void runTestOnIntermediateRevisions() throws Exception {
 		runTestOnIntermediateRevisions(0, intermediateRevisions.size());
 	}
 	
 	/**
 	 * For a specified range of IntermediateRevisions, creates actual 
 	 * intermediate revisions on the file system, runs tests on them 
 	 * and records the results to the output file.
 	 * 
 	 * @throws Exception
 	 */
 	public void runTestOnIntermediateRevisions(int startIndex, int numElements) 
 			throws Exception {
 		out1.write(HEADER1);
 		out1.flush();
 		out2.write(HEADER2);
 		out2.flush();
 		
 		Set<Pair<Revision, Revision>> pairs = new HashSet<Pair<Revision,Revision>>();
 		
 		for (int i = startIndex; i < startIndex + numElements; i++) {
 			// intermediateRevision already has its delta set
 			IntermediateRevision intermediateRevision = intermediateRevisions.get(i);
 			
 			Revision base = intermediateRevision.getBaseRevision();
 			Revision successor = intermediateRevision.getSuccessorRevision();
 			
 			Pair<Revision, Revision> pair = new Pair<Revision, Revision>(
 					base, successor);
 			
 			if (!pairs.contains(pair)) {
 				pairs.add(pair);
				String line = base.getCommitID() + COLUMN_SEPARATOR + 
					successor.getCommitID() + COLUMN_SEPARATOR + 
					getLineDelta(intermediateRevision.getTotalDelta()) + "\n";
				out2.write(line);
 				out2.flush();
 			}
 			
 			intermediateRevision.checkoutBaseSuccessorRevisions();
 			intermediateRevision.applyDelta();
 			intermediateRevision.runTest();
 			
 			String lines = analyzeIntermediateRevision(intermediateRevision, i);
 			out1.write(lines);
 			out1.flush();
 			
 			intermediateRevision.restoreBaseRevision();
 		}
 		
 		out1.close();
 		out2.close();
 	}
 	
 	/**
 	 * Records data of intermediateRevision.
 	 * 
 	 * @return a String representation of data of intermediateRevision.
 	 * @throws InterruptedException 
 	 * @throws IOException 
 	 */
 	public String analyzeIntermediateRevision(IntermediateRevision intermediateRevision, 
 			int iid) throws IOException, InterruptedException {
 		String lines = "";
 		
 		Revision parent = intermediateRevision.getBaseRevision();
 		Revision child = intermediateRevision.getSuccessorRevision();
 		TestResult parentTestResult = parent.getTestResult();
 		TestResult childTestResult = child.getTestResult();
 		
 		// IID parentCommitID childCommitID
 		String lineHeader = iid + COLUMN_SEPARATOR + 
 			parent.getCommitID() + COLUMN_SEPARATOR + 
 			child.getCommitID();
 		
 		// ?file1,?file2,...,?fileN
 		String lineDelta = getLineDelta(intermediateRevision.getDelta());
 		
 		if (intermediateRevision.isCompilable() == Compilable.YES && 
 				!intermediateRevision.hasTestAborted()) {
 			TestResult intermediateTestResult = intermediateRevision.getTestResult();
 			assert intermediateTestResult != null;
 		
 			for (String test : childTestResult.getAllTests()) {
 				lines += getFullLine(lineHeader, lineDelta, test, 
 						intermediateTestResult, parentTestResult, childTestResult);
 			}
 		} else {
 			lines += getFullLineNoTestResult(lineHeader, lineDelta, intermediateRevision);
 		}
 		
 		return lines;
 	}
 	
 	/**
 	 * Line format: 
 	 * IID parentCommitID childCommitID delta 
 	 * compilable testAborted test intermediateTestResult parentTestResult childTestResult
 	 */
 	private String getFullLine(String lineHeader, String lineDelta, 
 			String test, TestResult intermediateTestResult, 
 			TestResult parentTestResult, TestResult childTestResult) {
 		String line = "";
 		
 		line += lineHeader + COLUMN_SEPARATOR;
 		line += lineDelta + COLUMN_SEPARATOR;
 				
 		// compilable testAborted
 		line += TRUE + COLUMN_SEPARATOR + FALSE + COLUMN_SEPARATOR;
 		
 		// test intermediateTestResult parentTestResult childTestResult
 		line += getLineTestResults(test, intermediateTestResult, parentTestResult, 
 				childTestResult) + "\n";
 		
 		return line;
 	}
 	
 	/**
 	 * Line format: 
 	 * IID parentCommitID childCommitID delta 
 	 * compilable testAborted n n n n
 	 */
 	private String getFullLineNoTestResult(String lineHeader, 
 			String lineDelta, IntermediateRevision intermediateRevision) {
 		String line = "";
 		
 		line += lineHeader + COLUMN_SEPARATOR;
 		line += lineDelta + COLUMN_SEPARATOR;
 		
 		// compilable
 		line += (intermediateRevision.isCompilable() == Compilable.YES ? 
 				TRUE : FALSE) + COLUMN_SEPARATOR;
 		
 		// testAborted
 		line += (intermediateRevision.hasTestAborted() ? TRUE : FALSE) + 
 				COLUMN_SEPARATOR;
 		
 		line += NONE + COLUMN_SEPARATOR + // test
 				NONE + COLUMN_SEPARATOR + // intermediateTestResult
 				NONE + COLUMN_SEPARATOR + // parentTestResult
 				NONE + "\n";              // childTestResult
 		
 		return line;
 	}
 	
 	/**
 	 * Format: ?file1,?file2,...,?fileN
 	 */
 	private String getLineDelta(Set<DiffFile> delta) {
 		String res = "";
 		Iterator<DiffFile> iter = delta.iterator();
 		
 		while (iter.hasNext()) {
 			DiffFile diffFile = iter.next();
 			res += diffFile.getDiffType().toString().charAt(0) + diffFile.getFileName();
 			
 			if (iter.hasNext()) {
 				res += FILE_SEPARATOR;
 			}
 		}
 		
 		return res;
 	}
 	
 	/**
 	 * Format: test intermediateTestResult parentTestResult childTestResult
 	 */
 	private String getLineTestResults(String test, TestResult intermediateTestResult, 
 			TestResult parentTestResult, TestResult childTestResult) {
 		String testResults = test + COLUMN_SEPARATOR + 
 					intermediateTestResult.encodeAsString(test) + COLUMN_SEPARATOR + 
 					parentTestResult.encodeAsString(test) + COLUMN_SEPARATOR + 
 					childTestResult.encodeAsString(test);
 		
 		return testResults;
 	}
 }
