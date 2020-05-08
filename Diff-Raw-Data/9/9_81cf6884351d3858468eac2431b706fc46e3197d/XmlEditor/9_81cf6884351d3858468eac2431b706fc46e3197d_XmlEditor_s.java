 package edu.bxml.generator;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
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
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import edu.bxml.io.FilterAJ;
 
 public class XmlEditor  extends FilterAJ {
 	private static Log log = LogFactory.getLog(XmlEditor.class);
 	File xmlFile;
 	Map<String, Object> env = new HashMap<String, Object>();
 
 	public Map<String, Object> getEnv() {
 		return env;
 	}
 
 	public void setEnv(Map<String, Object> env) {
 		this.env = env;
 	}
 
 	public File getXmlFile() {
 		return xmlFile;
 	}
 
 	public void setXmlFile(File xmlFile) {
 		this.xmlFile = xmlFile;
 	}
 	
 	public void execute() {
 		log.debug("XML EDIT EXECUTE on file " + xmlFile);
 		Generator gen = getAncestorOfType(Generator.class);
 		String tempDir = gen.getTemplateDir();
 		String genDir = gen.getGeneratedDir();
 		File destFile = getDest(tempDir, genDir, xmlFile);
 		
 		String source= null;
 		try {
 			source = TemplateParser.readFileAsString(xmlFile, env);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		log.debug("source xml file = " + source);
 		
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder = null;
 		try {
 			docBuilder = docFactory.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 		
 		Document src= null;
 		try {
 			src = docBuilder.parse(new ByteArrayInputStream(source.getBytes()));
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		Document doc = null;
 		try {
 			doc = docBuilder.parse(destFile);
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		Map<String, Node> nodes = new HashMap<String, Node>();
 		
 		NodeList srcNodes = src.getElementsByTagName("nodedef").item(0).getChildNodes();
 
 		for (int x = srcNodes.getLength()-1; x >=0; x--) {
 			Node v = srcNodes.item(x);
 			log.debug("first child = " + v);
 			if (v.getAttributes()!= null) {
 				Node n = v.getAttributes().getNamedItem("id");
 				if (n != null) {
 					
 					String id = n.getNodeValue();
 					log.debug("PUT " + id + "   node: " + srcNodes.item(x));
 					nodes.put(id, srcNodes.item(x));
 				}
 			}
 		}
 		
 		NodeList selectNodeList = doc.getElementsByTagName("select");
 		for (int x = selectNodeList.getLength()-1; x >= 0 ; x--) {
 			Node id = selectNodeList.item(x).getAttributes().getNamedItem("id");
 			System.out.println("id = '" + id.getNodeValue() + "'");
 			Node replacement = nodes.get(id.getNodeValue());
 			if (replacement != null) {
 				Node repl = doc.importNode(replacement, true);
 				selectNodeList.item(x).getParentNode().removeChild(selectNodeList.item(x));
 				//selectNodeList.item(x).getParentNode().replaceChild(repl, selectNodeList.item(x));
 			}
 		}
 		
		NodeList nodeList = doc.getElementsByTagName("sql");
		for (int x = nodeList.getLength()-1; x >= 0 ; x--) {
			Node id = nodeList.item(x).getAttributes().getNamedItem("id");
 			String idName = id.getNodeValue();
 			System.out.println("id = " + id.getNodeValue());
 			Node replacement = nodes.get(id.getNodeValue());
 			if (replacement != null) {
 				Node repl = doc.importNode(replacement, true);
 				log.debug("REPLACE replacement = " + replacement);
				selectNodeList.item(x).getParentNode().removeChild(selectNodeList.item(x));
 				
 			}
 		}
 		
 		NodeList mapperNodeList = doc.getElementsByTagName("mapper");
 		for (Entry<String, Node> x : nodes.entrySet()) {
 			Node repl = doc.importNode(x.getValue(), true);
 			mapperNodeList.item(0).appendChild(repl);
 		}
 		
 		// write the content into xml file
 		TransformerFactory transformerFactory = TransformerFactory.newInstance();
 		transformerFactory.setAttribute("indent-number", 8);
 
 		Transformer transformer = null;
 		try {
 			transformer = transformerFactory.newTransformer();
 		} catch (TransformerConfigurationException e1) {
 			e1.printStackTrace();
 		}
 		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
 		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 		DOMSource domSource = new DOMSource(doc);
 		StreamResult result = new StreamResult(destFile);
 		try {
 			transformer.transform(domSource, result);
 		} catch (TransformerException e) {
 			e.printStackTrace();
 		}
  
 		System.out.println("Done");
 	}
 	
 	public File getDest(String tempDir, String genDir, File xmlFile) {
 		try {
 			Generator generator = getAncestorOfType(Generator.class);
 			String basename = generator.getBasename();
 			String propername = basename.substring(0,1).toUpperCase() + basename.substring(1);
 			File tempD = new File(tempDir);
 			String relativePath  = xmlFile.getCanonicalPath().substring(tempD.getCanonicalPath().length());
 			log.debug("relative path = " + relativePath);
 			File ret = new File(genDir, relativePath);
 			String destFileName = propername + ret.getCanonicalPath().substring(ret.getCanonicalPath().lastIndexOf('/')+1);
 			ret = new File(ret.getParent(), destFileName);
 			log.debug("getDest = " + ret.getCanonicalPath());
 			return ret;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
