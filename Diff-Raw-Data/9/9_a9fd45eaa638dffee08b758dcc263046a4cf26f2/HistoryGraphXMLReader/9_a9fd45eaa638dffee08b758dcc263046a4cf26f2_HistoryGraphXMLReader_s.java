 package util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 import org.xml.sax.SAXException;
 
 import common.DiffFile;
 import common.DiffFile.DiffType;
 import common.HistoryGraph;
 import common.Pair;
 import common.Revision;
 import common.Revision.Compilable;
 import common.TestResult;
 
 public class HistoryGraphXMLReader {
 	
 	private final File hGraphXML;
 	private final Map<String, Revision> revisions;
 	
 	public HistoryGraphXMLReader(File hGraphXML) {
 		this.hGraphXML = hGraphXML;
 		revisions = new HashMap<String, Revision>();
 	}
 	
 	public HistoryGraph reconstructHistoryGraph() throws ParserConfigurationException, 
 			SAXException, IOException {
 		HistoryGraph hGraph = new HistoryGraph();
 		
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder = factory.newDocumentBuilder();
 		Document doc = builder.parse(hGraphXML);
 		doc.getDocumentElement().normalize();
 		
 		Element root = doc.getDocumentElement(); // <HistoryGraph>
 		
 		NodeList nodes = root.getChildNodes();
 		
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Node node = nodes.item(i);
 			
 			if (node.getNodeType() == Node.ELEMENT_NODE) {
 				Element revisionElement = (Element) node; // <Revision>
 				
 				Revision revision = parseRevisionElement(revisionElement);
 				hGraph.addRevision(revision);
 				
 				revisions.put(revision.getCommitID(), revision);
 			}
 		}
 		
 		return hGraph;
 	}
 	
 	private Revision parseRevisionElement(Element revisionElement) {
 		Iterator<Element> iter = getSubElementIterator(revisionElement);
 		
 		Element commitIDElement = iter.next();		// <commitID>
 		Element compilableElement = iter.next();	// <Compilable>
 		Element testResultElement = iter.next();	// <TestResult>
 		Element parentsElement = iter.next();		// <Parents>
 		
 		String commitID = getString(commitIDElement);
 		Compilable compilable = parseCompilableElement(compilableElement);
 		TestResult testResult = null;
 		
 		if (compilable == Compilable.YES) {
 			testResult = parseTestResultElement(testResultElement);
 		}
 		
 		Map<Revision, List<DiffFile>> parentToDiffFiles = parseParentsElement(parentsElement);
 		
 		Revision revision = new Revision(commitID, parentToDiffFiles, compilable, testResult);
 		
 		return revision;
 	}
 	
 	private Compilable parseCompilableElement(Element compilableElement) {
 		String compilableStr = getString(compilableElement);
 		
 		if (compilableStr.equals(Compilable.YES.toString())) {
 			return Compilable.YES;
 		} else if (compilableStr.equals(Compilable.NO.toString())) {
 			return Compilable.NO;
 		} else if (compilableStr.equals(Compilable.UNKNOWN.toString())) {
 			return Compilable.UNKNOWN;
 		}
 		
 		return Compilable.NO_BUILD_FILE;
 	}
 	
 	private TestResult parseTestResultElement(Element testResultElement) {
 		Iterator<Element> iter = getSubElementIterator(testResultElement);
 		
 		Element allTestsElement = iter.next();		// <Tests>
 		Element failedTestsElement = iter.next();	// <FailedTests>
 		
 		Set<String> allTests = parseTestsElement(allTestsElement);
 		Set<String> failedTests = parseTestsElement(failedTestsElement);
 		
 		TestResult testResult = new TestResult(allTests, failedTests);
 		
 		return testResult;
 	}
 	
 	private Set<String> parseTestsElement(Element testsElement) {
 		Set<String> tests = new HashSet<String>();
 		
 		NodeList nodes = testsElement.getChildNodes();
 		
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Node node = nodes.item(i);
 			
 			if (node.getNodeType() == Node.ELEMENT_NODE) {
 				Element testNameElement = (Element) node;	// <testName>
 				
 				String testName = getString(testNameElement);
 				
 				tests.add(testName);
 			}
 		}
 		
 		return tests;
 	}
 	
 	private Map<Revision, List<DiffFile>> parseParentsElement(Element parentsElement) {
 		Map<Revision, List<DiffFile>> parentToDiffFiles = new HashMap<Revision, List<DiffFile>>();
 		
 		NodeList nodes = parentsElement.getChildNodes();
 		
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Node node = nodes.item(i);
 			
 			if (node.getNodeType() == Node.ELEMENT_NODE) {
 				Element parentElement = (Element) node; // <Parent>
 				
 				Pair<Revision, List<DiffFile>> pair = parseParentElement(parentElement);
 				Revision parent = pair.getFirst();
 				List<DiffFile> diffFiles = pair.getSecond();
 				
 				parentToDiffFiles.put(parent, diffFiles);
 			}
 		}
 		
 		return parentToDiffFiles;
 	}
 	
 	private Pair<Revision, List<DiffFile>> parseParentElement(Element parentElement) {
 		Iterator<Element> iter = getSubElementIterator(parentElement);
 		
 		Element commitIDElement = iter.next();	// <commitID>
 		Element diffFilesElement = iter.next();	// <DiffFiles>
 		
 		String parentCommitID = getString(commitIDElement);
 		List<DiffFile> diffFiles = parseDiffFilesElement(diffFilesElement);
 		
		Revision parent = revisions.get(parentCommitID);
 		
 		Pair<Revision, List<DiffFile>> pair = new Pair<Revision, List<DiffFile>>(parent, diffFiles);
 		
 		return pair;
 	}
 	
 	private List<DiffFile> parseDiffFilesElement(Element diffFilesElement) {
 		List<DiffFile> diffFiles = new ArrayList<DiffFile>();
 		
 		NodeList nodes = diffFilesElement.getChildNodes();
 		
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Node node = nodes.item(i);
 			
 			if (node.getNodeType() == Node.ELEMENT_NODE) {
 				Element diffFileElement = (Element) node;
 				
 				DiffFile diffFile = parseDiffFileElement(diffFileElement);
 				
 				diffFiles.add(diffFile);
 			}
 		}
 		
 		return diffFiles;
 	}
 	
 	private DiffFile parseDiffFileElement(Element diffFileElement) {
 		Iterator<Element> iter = getSubElementIterator(diffFileElement);
 		
 		Element fileNameElement = iter.next();	// <fileName>
 		Element diffTypeElement = iter.next();	// <DiffType>
 		
 		String fileName = getString(fileNameElement);
 		DiffType diffType = parseDiffTypeElement(diffTypeElement);
 		
 		DiffFile diffFile = new DiffFile(diffType, fileName);
 		
 		return diffFile;
 	}
 	
 	private DiffType parseDiffTypeElement(Element diffTypeElement) {
 		String diffTypeStr = getString(diffTypeElement);
 		
 		if (diffTypeStr.equals(DiffType.ADDED.toString())) {
 			return DiffType.ADDED;
 		} else if (diffTypeStr.equals(DiffType.MODIFIED.toString())) {
 			return DiffType.MODIFIED;
 		}
 		
 		return DiffType.DELETED;
 	}
 	
 	private Iterator<Element> getSubElementIterator(Element element) {
 		NodeList subNodes = element.getChildNodes();
 		List<Element> subElements = new ArrayList<Element>();
 		
 		for (int i = 0; i < subNodes.getLength(); i++) {
 			Node subNode = subNodes.item(i);
 			
 			if (subNode.getNodeType() == Node.ELEMENT_NODE) {
 				Element subElement = (Element) subNode;
 				
 				subElements.add(subElement);
 			}
 		}
 		
 		Iterator<Element> iter = subElements.iterator();
 		
 		return iter;
 	}
 	
 	private String getString(Element element) {
 		Text text = (Text) element.getFirstChild();
 		String str = text.getData().trim();
 		
 		return str;
 	}
 }
