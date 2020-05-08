 package histaroach.util;
 
 import histaroach.model.DiffFile;
 import histaroach.model.Revision;
 import histaroach.model.Revision.Compilable;
 import histaroach.model.TestResult;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
import org.w3c.dom.Node;
 import org.w3c.dom.Text;
 
 
 /**
  * XMLWriter contains common methods and tag names used by HistoryGraphXMLWriter 
  * and MixedRevisionXMLWriter.
  */
 public abstract class XMLWriter {
 	
 	public static final String COMMIT_ID = "commitID";
 	public static final String COMPILABLE = "Compilable";
 	public static final String TEST_ABORTED = "testAborted";
 	public static final String TEST_RESULT = "TestResult";
 	public static final String TESTS = "Tests";
 	public static final String FAILED_TESTS = "FailedTests";
 	public static final String TEST_NAME = "testName";
 	public static final String DIFF_FILES = "DiffFiles";
 	public static final String DIFF_FILE = "DiffFile";
 	public static final String FILE_NAME = "fileName";
 	public static final String DIFF_TYPE = "DiffType";
 	
 	protected final Document doc;
 	private final File xmlFile;
 	
 	protected XMLWriter(File xmlFile) throws ParserConfigurationException {
 		this.xmlFile = xmlFile;
 		
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder = factory.newDocumentBuilder();
 		doc = builder.newDocument();
 	}
 	
 	public abstract void write() throws TransformerException;
 
	protected void write(Element alienRootElement) throws TransformerException {
		Node rootElement = doc.importNode(alienRootElement, true);
 		doc.appendChild(rootElement);
 		
 		/* write the content into xml file */
 		TransformerFactory transformerFactory = TransformerFactory.newInstance();
 		Transformer transformer = transformerFactory.newTransformer();
 		
 		// set indentation
 		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
 		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
 		
 		DOMSource source = new DOMSource(doc);
 		StreamResult result = new StreamResult(xmlFile);
 		transformer.transform(source, result);
 	}
 	
 	public Element createCommitIDElement(String commitID) {
 		Element commitIDElement = doc.createElement(COMMIT_ID);
 		addText(commitIDElement, commitID);
 		
 		return commitIDElement;
 	}
 	
 	public Element createCompilableElement(Compilable compilable) {
 		Element compilableElement = doc.createElement(COMPILABLE);
 		addText(compilableElement, compilable.toString());
 		
 		return compilableElement;
 	}
 	
 	public Element createTestAbortedElement(boolean testAborted) {
 		Element testAbortedElement = doc.createElement(TEST_ABORTED);
 		addText(testAbortedElement, Boolean.toString(testAborted));
 		
 		return testAbortedElement;
 	}
 
 	public Element createTestResultElement(TestResult testResult) {
 		Element testResultElement = doc.createElement(TEST_RESULT);
 				
 		if (testResult != null) {
 			Set<String> allTests = testResult.getAllTests();
 			Set<String> failedTests = testResult.getFailedTests();
 			
 			Element allTestsElement = doc.createElement(TESTS);
 			Element failedTestsElement = doc.createElement(FAILED_TESTS);
 			
 			addTestNameElements(allTestsElement, allTests);
 			addTestNameElements(failedTestsElement, failedTests);
 			
 			testResultElement.appendChild(allTestsElement);
 			testResultElement.appendChild(failedTestsElement);
 		}
 		
 		return testResultElement;
 	}
 	
 	public Element createDiffRecordElement(Revision otherRevision, 
 			Collection<DiffFile> diffFiles, String tagName) {
 		Element diffRecordElement = doc.createElement(tagName);
 		
 		String commitID = otherRevision.getCommitID();
 		
 		Element commitIDElement = createCommitIDElement(commitID);
 		Element diffFilesElement = createDiffFilesElement(diffFiles);
 		
 		diffRecordElement.appendChild(commitIDElement);
 		diffRecordElement.appendChild(diffFilesElement);
 		
 		return diffRecordElement;
 	}
 	
 	public Element createDiffFilesElement(Collection<DiffFile> diffFiles) {
 		Element diffFilesElement = doc.createElement(DIFF_FILES);
 		
 		for (DiffFile diffFile : diffFiles) {
 			Element diffFileElement = createDiffFileElement(diffFile);
 			diffFilesElement.appendChild(diffFileElement);
 		}
 		
 		return diffFilesElement;
 	}
 	
 	public Element createDiffFileElement(DiffFile diffFile) {
 		Element diffFileElement = doc.createElement(DIFF_FILE);
 		
 		String fileName = diffFile.getFileName();
 		String diffTypeStr = diffFile.getDiffType().toString();
 		
 		Element fileNameElement = doc.createElement(FILE_NAME);
 		addText(fileNameElement, fileName);
 		
 		Element diffTypeElement = doc.createElement(DIFF_TYPE);
 		addText(diffTypeElement, diffTypeStr);
 		
 		diffFileElement.appendChild(fileNameElement);
 		diffFileElement.appendChild(diffTypeElement);
 		
 		return diffFileElement;
 	}
 	
 	private void addText(Element element, String data) {
 		Text text = doc.createTextNode(data);
 		element.appendChild(text);
 	}
 	
 	private void addTestNameElements(Element testsElement, Set<String> tests) {
 		for (String testName : tests) {
 			Element testNameElement = doc.createElement(TEST_NAME);
 			addText(testNameElement, testName);
 			
 			testsElement.appendChild(testNameElement);
 		}
 	}
 }
