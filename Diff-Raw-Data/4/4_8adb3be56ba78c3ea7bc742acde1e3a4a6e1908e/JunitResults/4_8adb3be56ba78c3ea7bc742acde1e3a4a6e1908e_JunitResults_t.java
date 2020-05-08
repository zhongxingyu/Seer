 package uk.co.codemonkey.concordion.specLinker;
 
 import static java.lang.Integer.parseInt;
 import static org.apache.commons.io.FilenameUtils.normalize;
import static org.apache.commons.io.FilenameUtils.separatorsToSystem;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.filechooser.FileSystemView;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.commons.io.FilenameUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class JunitResults {
 
 	private static final FileFilter filter = new FileFilter() {
 		@Override
 		public boolean accept(File file) {
 			return file.isDirectory() || file.getName().matches("TEST-.*\\.xml");
 		}
 	};
 	
 	private final DocumentBuilder docBuilder;
 	private final Map<String, JunitTestResults> testResults = new HashMap<String, JunitTestResults>();
 	private final String specDirectory;
 	
 	public JunitResults(String resultDirectory, String specDirectory) throws Exception {
 		this.specDirectory = specDirectory;
 		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
 		docBuilder = dbfac.newDocumentBuilder();
 		populateResults(new File(normalize(resultDirectory, true)));
 	}
 	
 	public JunitTestResults resultsFor(String testSpecFile) {
 		return testResults.get(testSpecFile);
 	}
 	
 	public JunitTestResults indexResults(String indexName) {
 		// relies on the fact that indexes are in a tree
 		// hierarchy. The numbers are probably wrong.
 		
 		String testPackage = indexName.replaceAll(File.separator, ".").replaceFirst("\\.[^\\.]+\\.html$", "");
 		JunitTestResults results = new JunitTestResults(testPackage, 0, 0, 0, 0);
 		for(Map.Entry<String, JunitTestResults> entry : testResults.entrySet()){
 			if(entry.getKey().startsWith(testPackage)) {
 				results = results.addTo(entry.getValue());
 			}
 		}
 		return results;
 	}
 	
 	private void populateResults(File directory) throws Exception {
 		for(File file : directory.listFiles(filter)) {
 			if(file.isDirectory()){
 				populateResults(file);
 			}
 			else {
 			    processResults(file);
 			}
 		}
 	}
 
 	private void processResults(File file) throws Exception {
 		Document results = docBuilder.parse(file);
 		JunitTestResults testResults = new JunitTestResults(results.getDocumentElement());
 		this.testResults.put(testResults.specName, testResults);
 	}
 
 	class JunitTestResults {
 
 		private final String specName;
 		final int tests;
 		private final int failures;
 		private final int skipped;
 		private final int errors;
 		private final int passed;
 
 		JunitTestResults(Element testSuite) {
 			specName = specFromTest(testSuite.getAttribute("name"));
 			this.tests =      asInt(testSuite.getAttribute("tests"));
 			this.failures =     asInt(testSuite.getAttribute("failures")); 
 			this.skipped =      asInt(testSuite.getAttribute("skipped")); 
 			this.errors =     asInt(testSuite.getAttribute("errors"));
 			passed = tests - failures - errors - skipped;
 		}
 		
 		private JunitTestResults(String name, int tests, int failures,int skipped,int errors) {
 			specName = name;
 			this.tests = tests;
 			this.failures = failures;
 			this.skipped = skipped;
 			this.errors = errors;
 			passed = tests - failures - errors - skipped;
 		}
 		
 		private int asInt(String elem) {
 			return elem == null || elem.equals("") ? 0 : parseInt(elem);
 		}
 		
 		private String specFromTest(String testName) {
 			String filename = testName.replace('.', '/');
 			filename = filename.replaceFirst("Test$", ".html");
			return separatorsToSystem(normalize(specDirectory + '/' + filename, true));
 		}
 
 		public boolean isSuccess() {
 			return tests == passed;
 		}
 		
 		public JunitTestResults addTo(JunitTestResults results) {
 			return new JunitTestResults(
 					specDirectory,
 					tests + results.tests,
 					failures + results.failures,
 					skipped + results.skipped,
 					errors + results.errors);
 		}
 	}
 	
 }
