 package com.seitenbau.reader.eclipse;
 
 import java.io.File;
import java.io.FileOutputStream;
 import java.io.IOException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * This class offers methods for transforming Enterprise Architect UML xmi files
  * into eclipse readable xmi files (EMF project).
  * 
  * @author nkunstek
  */
 public abstract class XmiTransformer {
 
 	/** The logger. */
 	private static final Logger LOG = Logger.getLogger(XmiTransformer.class);
 
 	private static final String TARGET_FILE_PATH = "target/transformed.xmi";
 
 	private final static String EA_ELEMENT_PRIMARY_KEY = "thecustomprofile:Schlsselattribut";
 	private final static String EA_ELEMENT_MAX = "thecustomprofile:max";
 	private final static String EA_ATTRIBUT_BASE = "base_Attribute";
 	private final static String EA_PROPERTY_BASE = "base_Property";
 
 	private final static String XMI_ELEMENT_OWNED_ATTRIBUTE = "ownedAttribute";
 	private final static String XMI_ATTR_ID = "xmi:id";
 	private final static String XMI_ATTR_IS_ID = "isID";
 
 	/**
 	 * Transforms an Enterprise Architect UML xmi file into an eclipse readable
 	 * xmi file (EMF project).
 	 * 
 	 * @param eaXmiFile
 	 *            the Enterprise Architect UML xmi file to transform.
 	 * @param primitivetypesFile
 	 *            Adding the primitive Types to the transformed file.
 	 * @return an eclipse readable xmi file (EMF project)
 	 */
 	public static File transform(File eaXmiFile, File primitivetypesFile) {
 		String method = "transform(): ";
 		LOG.debug(method + "Start");
 
 		try {
 			Document doc = parseDocument(eaXmiFile);
 			Document primitivetypesDoc = parseDocument(primitivetypesFile);
 
 			appendPrimitiveTypes(doc, primitivetypesDoc);
 			transformPrimaryKeys(doc);
 			transformLengths(doc);
 
 			File transformedFile = new File(TARGET_FILE_PATH);
 			writeTransformedFile(doc, transformedFile);
 
 			LOG.debug(method + "End");
 			return transformedFile;
 		} catch (Exception e) {
 			String msg = method + e.getMessage();
 			System.out.println(msg);
 			LOG.error(msg);
 		}
 
 		LOG.debug(method + "End");
 		return null;
 	}
 
 	/**
 	 * Parsing the given Enterprise Architect xmi file.
 	 * 
 	 * @param eaXmiFile
 	 *            the Enterprise Architect xmi file to parse.
 	 * @return
 	 * @throws ParserConfigurationException
 	 *             Exception when parsing file.
 	 * @throws SAXException
 	 *             Exception when parsing file.
 	 * @throws IOException
 	 *             Exception when writing file.
 	 */
 	private static Document parseDocument(File eaXmiFile)
 			throws ParserConfigurationException, SAXException, IOException {
 		String method = "parseDocument(): ";
 		LOG.debug(method + "Start");
 
 		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
 				.newInstance();
 		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
 		Document doc = docBuilder.parse(eaXmiFile);
 
 		LOG.debug(method + "End");
 		return doc;
 	}
 
 	/**
 	 * Adding the primitive types into the given document in the extension
 	 * section.
 	 * 
 	 * @param docToAdd
 	 *            the xmi document to add the primitive typs.
 	 * @param primitivetypesDoc
 	 *            the primitive types.
 	 */
 	private static void appendPrimitiveTypes(Document docToAdd,
 			Document primitivetypesDoc) {
 		String method = "appendPrimitiveTypes(): ";
 		LOG.debug(method + "Start");
 
 		Element root = docToAdd.getDocumentElement();
 		Node newChild = docToAdd.importNode(
 				primitivetypesDoc.getDocumentElement(), true);
 		root.appendChild(newChild);
 
 		LOG.debug(method + "End");
 	}
 
 	/**
 	 * Add the isID attribute to the owned attributes elements for the given
 	 * document.
 	 * 
 	 * @param doc
 	 *            the document to transform.
 	 */
 	private static void transformPrimaryKeys(Document doc) {
 		String method = "transformPrimaryKeys(): ";
 		LOG.debug(method + "Start");
 
 		NodeList primaryKeysEA = doc
 				.getElementsByTagName(EA_ELEMENT_PRIMARY_KEY);
 		for (int i = 0; i < primaryKeysEA.getLength(); i++) {
 			Element primaryKey = (Element) primaryKeysEA.item(i);
 
 			String xmiId = primaryKey.getAttribute(EA_ATTRIBUT_BASE);
 			if (xmiId.equals("")) {
 				xmiId = primaryKey.getAttribute(EA_PROPERTY_BASE);
 			}
 			Element primaryKeyElement = getClassAttributeByXmiId(doc, xmiId);
 			primaryKeyElement.setAttribute(XMI_ATTR_IS_ID,
 					Boolean.TRUE.toString());
 		}
 
 		LOG.debug(method + "End");
 	}
 	
 	/**
 	 * Add the max length attribute to the owned attributes elements for the given
 	 * document.
 	 * 
 	 * @param doc
 	 *            the document to transform.
 	 */
 	private static void transformLengths(Document doc) {
 		String method = "transformLengths(): ";
 		LOG.debug(method + "Start");
 
 		NodeList lengthsEA = doc
 				.getElementsByTagName(EA_ELEMENT_MAX);
 		for (int i = 0; i < lengthsEA.getLength(); i++) {
 			Element length = (Element) lengthsEA.item(i);
 
 			String xmiId = length.getAttribute(EA_ATTRIBUT_BASE);
 			if (xmiId.equals("")) {
 				xmiId = length.getAttribute(EA_PROPERTY_BASE);
 			}
 
 			Element lengthElement = getClassAttributeByXmiId(doc, xmiId);
 			//liefert aktuell nur das Element, welches der xmi id entspricht.
 			//Auslesen des max Wertes funktioniert noch nicht.
 		}
 
 		LOG.debug(method + "End");
 	}
 
 	/**
 	 * Returns the owned attribute element for the given doc and xmiId.
 	 * 
 	 * @param doc
 	 *            the document to transform.
 	 * @param xmiId
 	 *            the xmiId to search for.
 	 * @return the owned attribute element for the given doc and xmiId
 	 */
 	private static Element getClassAttributeByXmiId(Document doc, String xmiId) {
 		String method = "getClassAttributeByXmiId(): ";
 		LOG.debug(method + "Start");
 
 		NodeList classAttributeList = doc
 				.getElementsByTagName(XMI_ELEMENT_OWNED_ATTRIBUTE);
 		for (int i = 0; i < classAttributeList.getLength(); i++) {
 			Element classAttribute = (Element) classAttributeList.item(i);
 			String xmiIdAttribute = classAttribute.getAttribute(XMI_ATTR_ID);
 			if (xmiIdAttribute.equals(xmiId)) {
 				LOG.debug(method + "End");
 				return classAttribute;
 			}
 		}
 
 		LOG.debug(method + "End");
 		return null;
 	}
 
 	/**
 	 * Write transformed xmi document to target file.
 	 * 
 	 * @param doc
 	 *            the transformed xmi document.
 	 * @param targetFile
 	 * @throws IOException
 	 *             Error writing file.
 	 * @throws TransformerException
 	 *             Error transforming file.
 	 */
 	private static void writeTransformedFile(Document doc, File targetFile)
 			throws IOException, TransformerException {
 		String method = "writeTransformedFile(): ";
 		LOG.debug(method + "Start");
 
		StreamResult outputStream = new StreamResult(new FileOutputStream(targetFile));
 		DOMSource xmiSource = new DOMSource(doc);
 		Transformer transformer = TransformerFactory.newInstance()
 				.newTransformer();
 		transformer.transform(xmiSource, outputStream);
 
 		LOG.debug(method + "End");
 	}
 }
