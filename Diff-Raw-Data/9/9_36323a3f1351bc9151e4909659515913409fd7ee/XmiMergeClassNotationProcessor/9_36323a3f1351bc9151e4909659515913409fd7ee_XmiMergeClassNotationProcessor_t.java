 package controller.merge.xmi.xclass;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.Queue;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import uml2parser.Attribute;
 import uml2parser.ModelFileInfo;
 import uml2parser.XmiElement;
 
 public class XmiMergeClassNotationProcessor {
 
 	private final static String TAG_CHILDREN = "children";
 	private final static String TAG_EANNOTATIONS = "eAnnotations";
 	private final static String TAG_STYLES = "styles";
 	private final static String TAG_LAYOUT_CONSTRAINT = "layoutConstraint";
 	private final static String TAG_ELEMENT = "element";
 	private final static String TAG_EDGE = "edge";
 	
 	private final static String ATTRIBUTE_TYPE = "type";
 	private final static String ATTRIBUTE_XMI_TYPE = "xmi:type";
 	private final static String ATTRIBUTE_XMI_ID = "xmi:id";
 	private final static String ATTRIBUTE_HREF = "href";
 	private final static String ATTRIBUTE_TARGET = "target";
 	private final static String ATTRIBUTE_SOURCE = "source";
 	
 	private final static String TYPE_NOTATION_DECORATIONONDE = "notation:DecorationNode";
 	private final static String TYPE_NOTATION_BASICCOMPARTMENT = "notation:BasicCompartment";
 	
 	// Type values for attribute, operation and nested class compartments in a class
 	private final static String TYPE_VALUE_ATTRIBUTE = "7017"; 
 	private final static String TYPE_VALUE_OPERATION = "7018"; 
 	private final static String TYPE_VALUE_NESTED_CLASS = "7019"; 
 	
 	private String newFileName = "TEST";
 	private String umlFileId;
 	private String notationName;
 	private String notationId;
 	
 	private HashMap<String, String> mapElementToChildren = new HashMap<String, String>();
 	private HashMap<String, String> mapClass2Ids = new HashMap<String, String>();
 	private HashMap<String, String> mapParentToSource = new HashMap<String, String>();
 	
 	private Queue<XmiNotationElement> elements;
 	
 	// fields for DOM
 	private DocumentBuilderFactory docFactory = DocumentBuilderFactory
 			.newInstance();
 	private DocumentBuilder docBuilder;
 
 	private Document umlDoc;
 	private Element umlRootElement;
 	
 	
 	public XmiMergeClassNotationProcessor(NotationData notationData) {
 		this(notationData.getElements(), 
 			notationData.getReplaceClass2Id(), 
 			notationData.getMapParentToSource(), 
 			notationData.getFileName(), 
 			notationData.getUmlId(), 
 			notationData.getNotationName(), 
 			notationData.getNotationId());
 	}
 	
 	private XmiMergeClassNotationProcessor(Queue<XmiNotationElement> elements, HashMap<String, String> replaceClass2Id, HashMap<String, String> mapParentToSource, String fileName, String umlId, String notationName, String notationId) {
 		this.elements = elements;
 		this.umlFileId = umlId;
 		this.newFileName = fileName;
 		this.notationName = notationName;
 		this.notationId = notationId;
 		this.mapClass2Ids = replaceClass2Id;
 		this.mapParentToSource = mapParentToSource;
 		
 		try {
 			Initialize();
 			
 			while (!elements.isEmpty()) {
 				createNode(elements.poll(), umlDoc, umlRootElement);
 			}
 			umlRootElement.appendChild(createUmlElement(umlDoc));
 			
 		} catch (ParserConfigurationException  e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void Initialize() throws ParserConfigurationException {
 		docBuilder = docFactory.newDocumentBuilder();
 
 		// UML Header
 		umlDoc = docBuilder.newDocument();
 		umlRootElement = umlDoc.createElement("notation:Diagram");
 		umlRootElement.setAttribute("xmi:version", "2.0");
 		umlRootElement.setAttribute("xmlns:xmi",
 				"http://www.omg.org/XMI");
 		umlRootElement.setAttribute("xmlns:ecore",
 				"http://www.eclipse.org/emf/2002/Ecore");
 		umlRootElement.setAttribute("xmlns:notation",
 				"http://www.eclipse.org/gmf/runtime/1.0.2/notation");
 		
 		umlRootElement.setAttribute("xmlns:uml",
 				"http://www.eclipse.org/uml2/4.0.0/UML");
 		
 		umlRootElement.setAttribute("type",
 				"PapyrusUMLClassDiagram");
 		
 		umlRootElement.setAttribute("measurementUnit",
 				"Pixel");
 		
		umlRootElement.setAttribute("name", this.newFileName);
 		umlRootElement.setAttribute("xmi:id", this.notationId);
 
 		umlDoc.appendChild(umlRootElement);
 	}
 	
 	private Element createUmlElement(Document doc) {
 		Element umlElement = doc.createElement(TAG_ELEMENT);
 		
 		umlElement.setAttribute(ATTRIBUTE_HREF, ClassMergeUtility.buildHref(umlFileId, newFileName));
 		umlElement.setAttribute(ATTRIBUTE_XMI_TYPE, "uml:Model");
 		
 		return umlElement;
 	}
 	
 	private void createNode(XmiNotationElement element, Document doc, Element rootElement) {
 		
 		if (element.getType() == XmiNotationElement.TYPE_GENERALIZATION) {
 			createRelationNode(element, doc, rootElement);
 		}else if (element.getType() == XmiNotationElement.TYPE_ASSOCIATION) {
 			createAssociationNode(element, doc, rootElement);
 		} else {
 			createClassNode(element, doc, rootElement);
 		}
 		
 		
 	}
 	
 	private void createClassNode(XmiNotationElement element, Document doc, Element rootElement) {
 		ModelFileInfo notation = element.getNotation();
 		
 		String href = notation.getFileNameNoExtension() + ".uml#" + element.getId();
 	
 		// Build class
 		XmiElement parentXmiElement = notation.getXmiElementFromHref(href).getParentElem();
 		
 		Element parentElement = generateElement(parentXmiElement, doc);
 		
 		// Store mapping information for target tag. Store the uml element ID to the notation children Id
 		mapElementToChildren.put(element.getId(), parentElement.getAttribute(ATTRIBUTE_XMI_ID));
 			
 		for (XmiElement xmiElement : parentXmiElement.getChildElemList()) {
 			String tag = xmiElement.getElementName();
 			
 			switch(tag) {
 				case TAG_CHILDREN:
 					parentElement.appendChild(generateChildren(xmiElement, doc, element));
 				break;
 				case TAG_ELEMENT:
 					Element elementTag = doc.createElement(TAG_ELEMENT);
 					
 					for (Attribute attribute : xmiElement.getAttrib()) {
 						if (attribute.getName().equals(ATTRIBUTE_HREF)) {
 							elementTag.setAttribute(attribute.getName(), ClassMergeUtility.buildHref(element.getId(), this.newFileName));
 						} else {
 							elementTag.setAttribute(attribute.getName(), attribute.getValue());
 						}
 					}
 					
 					parentElement.appendChild(elementTag);
 				break;
 				default:
 					parentElement.appendChild(generateElementAndChildren(xmiElement, doc));
 			}
 		}
 		
 		rootElement.appendChild(parentElement);
 	}
 	
 	// Generate children tag elements
 	private void createRelationNode(XmiNotationElement element, Document doc, Element rootElement) {
 			
 		ModelFileInfo notation = element.getNotation();
 		
 		String href = notation.getFileNameNoExtension() + ".uml#" + element.getId();
 	
 		// Build class
 		XmiElement xmiElement = notation.getXmiElementFromHref(href);
 		
 		Element parentElement = generateElementAndChildren(xmiElement.getParentElem(), doc, TAG_ELEMENT);
 		
 		// Check if we need to override Target attribute if it doesn't exist
 		String target = parentElement.getAttribute(ATTRIBUTE_TARGET);
 		if (!mapElementToChildren.containsValue(target)) {		
 			String intendedElement = mapClass2Ids.get(element.getId());			
 			parentElement.setAttribute(ATTRIBUTE_TARGET, mapElementToChildren.get(intendedElement));
 		}
 		
 	
 		// Check if we need to override Source attribute
 		if (mapParentToSource.containsKey(element.getId())) {
 			String intendedSource = mapParentToSource.get(element.getId());		
 			parentElement.setAttribute(ATTRIBUTE_SOURCE, mapElementToChildren.get(intendedSource));
 		} 
 		
 		Element elementTag = doc.createElement(TAG_ELEMENT);
 		for (Attribute attribute : xmiElement.getAttrib()) {
 			if (attribute.getName().equals(ATTRIBUTE_HREF)) {
 				elementTag.setAttribute(attribute.getName(), ClassMergeUtility.buildHref(element, newFileName));
 			} else {
 				elementTag.setAttribute(attribute.getName(), attribute.getValue());
 			}
 		}
 		
 		parentElement.appendChild(elementTag);
 		rootElement.appendChild(parentElement);
 	}
 	
 	// Generate children tag elements
 	private void createAssociationNode(XmiNotationElement element, Document doc, Element rootElement) {
 				
 		ModelFileInfo notation = element.getNotation();
 		
 		String href = notation.getFileNameNoExtension() + ".uml#" + element.getId();
 	
 		// Build class
 		XmiElement xmiElement = notation.getXmiElementFromHref(href);
 		
 		Element parentElement = generateElementAndChildren(xmiElement.getParentElem(), doc, TAG_ELEMENT);
 		
 		parentElement.setAttribute(ATTRIBUTE_TARGET, mapElementToChildren.get(element.getTarget()));
 		parentElement.setAttribute(ATTRIBUTE_SOURCE, mapElementToChildren.get(element.getSource()));
 				
 		Element elementTag = doc.createElement(TAG_ELEMENT);
 			
 		for (Attribute attribute : xmiElement.getAttrib()) {
 			if (attribute.getName().equals(ATTRIBUTE_HREF)) {
 				elementTag.setAttribute(attribute.getName(), ClassMergeUtility.buildHref(element, newFileName));
 			} else {
 				elementTag.setAttribute(attribute.getName(), attribute.getValue());
 			}
 		}
 		
 		parentElement.appendChild(elementTag);
 		rootElement.appendChild(parentElement);
 	}
 		
 	// Generate children tag elements
 	private Element generateChildren(XmiElement xmiElement, Document doc, XmiNotationElement notationElement) {
 			
 		Element element;
 		
 		switch(xmiElement.getAttributeValue(ATTRIBUTE_TYPE)) {
 			case TYPE_VALUE_ATTRIBUTE:
 				element = generateElementAndChildren(xmiElement, doc, TAG_ELEMENT + TAG_CHILDREN);
 				appendAttributeElements(element, doc, notationElement);
 				break;
 			case TYPE_VALUE_OPERATION:
 				element = generateElementAndChildren(xmiElement, doc, TAG_ELEMENT + TAG_CHILDREN);
 				appendOperationElements(element, doc, notationElement);
 				break;
 			default:
 				element = generateElement(xmiElement, doc);
 				
 		}
 		
 		return element;
 	}
 	
 	private void appendAttributeElements(Element parentElement, Document doc, XmiNotationElement notationElement) {
 		
 		for (XmiNotationElement childElement : ClassMergeUtility.getNotationElements(notationElement, XmiNotationElement.TYPE_ATTRIBUTE)) {
 			ModelFileInfo notation = childElement.getNotation();
 			
 			String href = ClassMergeUtility.buildHref(childElement.getId(), notation.getFileNameNoExtension());
 			XmiElement xmiElement = notation.getXmiElementFromHref(href);
 			
 			XmiElement parentXmiElement = xmiElement.getParentElem();
 						
 			Element elementTag = doc.createElement(TAG_ELEMENT);
 			
 			for (Attribute attribute : xmiElement.getAttrib()) {
 				if (attribute.getName().equals(ATTRIBUTE_HREF)) {
 					elementTag.setAttribute(attribute.getName(), ClassMergeUtility.buildHref(childElement, newFileName));
 				} else {
 					elementTag.setAttribute(attribute.getName(), attribute.getValue());
 				}
 			}
 
 			Element attributeChild = generateElementAndChildren(parentXmiElement, doc, TAG_ELEMENT);
 			attributeChild.appendChild(elementTag);
 			
 			parentElement.appendChild(attributeChild);
 		}
 	}
 	
 	private void appendOperationElements(Element parentElement, Document doc, XmiNotationElement notationElement) {
 		
 		for (XmiNotationElement childElement : ClassMergeUtility.getNotationElements(notationElement, XmiNotationElement.TYPE_OPERATION)) {
 			ModelFileInfo notation = childElement.getNotation();
 			
 			String href = ClassMergeUtility.buildHref(childElement.getId(), notation.getFileNameNoExtension());
 			XmiElement xmiElement = notation.getXmiElementFromHref(href);
 			
 			Element elementTag = doc.createElement(TAG_ELEMENT);
 			
 			for (Attribute attribute : xmiElement.getAttrib()) {
 				if (attribute.getName().equals(ATTRIBUTE_HREF)) {
 					elementTag.setAttribute(attribute.getName(), ClassMergeUtility.buildHref(childElement, newFileName));
 				} else {
 					elementTag.setAttribute(attribute.getName(), attribute.getValue());
 				}
 			}
 
 			XmiElement parentXmiElement = xmiElement.getParentElem();
 			
 			Element operationChild = generateElementAndChildren(parentXmiElement, doc, TAG_ELEMENT);
 			operationChild.appendChild(elementTag);
 			
 			parentElement.appendChild(operationChild);
 		}
 	}
 			
 	// Default method to generate an element and its children
 	private Element generateElement(XmiElement xmiElement, Document doc) {
 		Element element = doc.createElement(xmiElement.getElementName());
 		
 		for (Attribute attribute : xmiElement.getAttrib()) {
 			
 			if (attribute.getName().equals(ATTRIBUTE_XMI_ID) && mapClass2Ids.containsKey(attribute.getValue())) {
 				element.setAttribute(attribute.getName(), mapClass2Ids.get(attribute.getValue()));
 			} else {
 				element.setAttribute(attribute.getName(), attribute.getValue());
 			}
 		}
 		
 		return element;
 	}
 	
 	// Default method to generate an element and its children
 	private Element generateElementAndChildren(XmiElement xmiElement, Document doc) {
 		Element element = doc.createElement(xmiElement.getElementName());
 
 		for (Attribute attribute : xmiElement.getAttrib()) {
 			element.setAttribute(attribute.getName(), attribute.getValue());
 		}
 		
 		for (XmiElement childElement : xmiElement.getChildElemList()) {
 			element.appendChild(generateElementAndChildren(childElement, doc));
 		}
 		return element;
 	}
 	
 	// Generate element and children elements, but skips elements that has a tag that is in containsSkip
 	private Element generateElementAndChildren(XmiElement xmiElement, Document doc, String skipTag) {
 		Element element = doc.createElement(xmiElement.getElementName());
 		
 		for (Attribute attribute : xmiElement.getAttrib()) {
 			element.setAttribute(attribute.getName(), attribute.getValue());
 		}
 		
 		for (XmiElement childElement : xmiElement.getChildElemList()) {
 			// Skip element tags
 			if (!skipTag.isEmpty() && skipTag.contains(childElement.getElementName())) {
 				continue;
 			}
 			
 			element.appendChild(generateElementAndChildren(childElement, doc, skipTag));
 		}
 		return element;
 	}
 	
 	public File GenerateFile(String fileName) {
 		// write the content into xml file
 		TransformerFactory transformerFactory = TransformerFactory
 				.newInstance();
 		Transformer transformer;
 		try {
 			transformer = transformerFactory.newTransformer();
 			
 			DOMSource source = new DOMSource(umlDoc);
 			
 			File file = new File("C:\\temp\\" + fileName + ".notation");
 			StreamResult result = new StreamResult(file);
 			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 			transformer.transform(source, result);
 
 			System.out.println("notation file created!");
 			
 			return file;
 			
 		} catch (TransformerException e) {
 			System.out.println("Failed notation file!");
 			e.printStackTrace();
 		}
 		
 		return null;
 
 	}
 }
