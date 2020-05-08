 package FindBugsManager.Core;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import FindBugsManager.DataSets.BugData;
 import FindBugsManager.DataSets.BugInstanceSet;
 import edu.umd.cs.findbugs.BugInstance;
 
 public class XMLReader {
 
 	private ArrayList<BugData> fixed = new ArrayList<BugData>();
 	private ArrayList<BugData> remain = new ArrayList<BugData>();
 
 	private String _filepath = "../bugOutput/Comparisons/bugData.xml";
 
 	private DocumentBuilderFactory _factory = DocumentBuilderFactory.newInstance();
 	private DocumentBuilder builder = null;
 
 	public XMLReader() {
 
 	}
 
 	public ArrayList<BugInstanceSet> parseFindBugsXML(ArrayList<BugInstanceSet> bugInfoList,
 			File file) {
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder = null;
 
 		int startLine = 0, endLine = 0;
 
 		try {
 			builder = factory.newDocumentBuilder();
 			Document doc = builder.parse(file);
 
 			Element root = doc.getDocumentElement();
 			NodeList children = root.getChildNodes();
 
 			for (int i = 0; i < children.getLength(); i++) {
 				Node child = children.item(i);
 				if (child instanceof Element) {
 					Element childElement = (Element) child;
 					if (childElement.getTagName().equals("BugInstance")) {
 						int bugPriority = Integer.parseInt(childElement.getAttribute("priority"));
 						String bugType = childElement.getAttribute("type");
 						String remChan = childElement.getAttribute("removedByChange");
 						if (!(remChan.equals("true"))) {
 							NodeList grandChild = childElement.getChildNodes();
 							for (int j = 0; j < grandChild.getLength(); j++) {
 								Node grand = grandChild.item(j);
 								if (grand instanceof Element) {
 									Element grandElement = (Element) grand;
 									if (grandElement.getTagName().equals("SourceLine")) {
 										String start = grandElement.getAttribute("start");
 										String end = grandElement.getAttribute("end");
										if (start.isEmpty() || end.isEmpty()) {
											//
										} else {
 											startLine = Integer.parseInt(start);
 											endLine = Integer.parseInt(end);
 										}
 										break;
 									}
 								}
 							}
 							BugInstance instance = new BugInstance(bugType, bugPriority);
 
 							BugInstanceSet info = new BugInstanceSet(instance, startLine, endLine);
 							bugInfoList.add(info);
 						}
 					}
 				}
 			}
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return bugInfoList;
 	}
 
 	public void createBugLists() {
 		try {
 			builder = _factory.newDocumentBuilder();
 			Document doc = builder.parse(_filepath);
 
 			Element root = doc.getDocumentElement();
 			NodeList children = root.getChildNodes();
 
 			for (int i = 0; i < children.getLength(); i++) {
 				Node child = children.item(i);
 				if (child instanceof Element) {
 					Element childElement = (Element) child;
 					String tagName = childElement.getTagName();
 					if (tagName.equals("FixedBugs")) {
 						NodeList grandChild = childElement.getChildNodes();
 						for (int j = 0; j < grandChild.getLength(); j++) {
 							Node grand = grandChild.item(j);
 							if (grand instanceof Element) {
 								Element grandElement = (Element) grand;
 								getElements(grandElement, fixed);
 							}
 						}
 					} else if (tagName.equals("RemainingBugs")) {
 						NodeList grandChild = childElement.getChildNodes();
 						for (int j = 0; j < grandChild.getLength(); j++) {
 							Node grand = grandChild.item(j);
 							if (grand instanceof Element) {
 								Element grandElement = (Element) grand;
 								getElements(grandElement, remain);
 							}
 						}
 					}
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void getElements(Element grandElement, ArrayList<BugData> list) {
 		String bugCategory = null;
 		String bugAbbrev = null;
 		String bugType = null;
 		String bugRank = null;
 		String bugPoint = null;
 		String bugPriority = null;
 		String bugCondition = null;
 		String bugLine = null;
 		String bugFixer = null;
 		String bugAuthor = null;
 
 		if (grandElement.getTagName().equals("BugInstance")) {
 			NodeList greatChild = grandElement.getChildNodes();
 			for (int i = 0; i < greatChild.getLength(); i++) {
 				Node great = greatChild.item(i);
 				if (great instanceof Element) {
 					Element greatElement = (Element) great;
 					String tagName = greatElement.getTagName();
 					if (tagName.equals("Category")) {
 						bugCategory = greatElement.getTextContent();
 					} else if (tagName.equals("Abbreviation")) {
 						bugAbbrev = greatElement.getTextContent();
 					} else if (tagName.equals("Type")) {
 						bugType = greatElement.getTextContent();
 					} else if (tagName.equals("Rank")) {
 						bugRank = greatElement.getTextContent();
 					} else if (tagName.equals("Point")) {
 						bugPoint = greatElement.getTextContent();
 					} else if (tagName.equals("Priority")) {
 						bugPriority = greatElement.getTextContent();
 					} else if (tagName.equals("Condition")) {
 						bugCondition = greatElement.getTextContent();
 					} else if (tagName.equals("Line")) {
 						bugLine = greatElement.getTextContent();
 					} else if (tagName.equals("Amender")) {
 						bugFixer = greatElement.getTextContent();
 					} else if (tagName.equals("Author")) {
 						bugAuthor = greatElement.getTextContent();
 					}
 				}
 			}
 			list.add(new BugData(bugCategory, bugAbbrev, bugType, bugRank, bugPoint, bugPriority,
 					bugCondition, bugLine, bugFixer, bugAuthor));
 		}
 	}
 	public ArrayList<BugData> getFixedBugDataList() {
 		return fixed;
 	}
 
 	public ArrayList<BugData> getRemainBugDataList() {
 		return remain;
 	}
 }
