 package com.blastedstudios.crittercaptors.util;
 
import java.io.OutputStream;
 import java.util.Vector;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.badlogic.gdx.Gdx;
 
 public class XMLUtil {
 	public static Iterable<Element> iterableElementList(NodeList nodeList) {
 		Vector<Element> nodes = new Vector<Element>(nodeList.getLength());
 		for(int i = 0; i < nodeList.getLength(); i++) {
 			Node node = nodeList.item(i);
 			if(Element.class.isAssignableFrom(node.getClass()))
 				nodes.add((Element)node);
 		}
 		return nodes;
 	}
 	
 	public static Document parse(String path){
 		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder;
 		try {
 			docBuilder = docBuilderFactory.newDocumentBuilder();
	        return docBuilder.parse(Gdx.files.internal(path).read());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * @param xmlDoc xml document to write
 	 * @param path to write xml document
 	 */
 	public static void writeToFile(Document xmlDoc, String path) {
 		try {
 			DOMSource source = new DOMSource(xmlDoc);
			OutputStream destStream = Gdx.files.internal(path).write(false);
 			StreamResult dest = new StreamResult(destStream);
 			TransformerFactory factory = TransformerFactory.newInstance();
 			Transformer xformer = factory.newTransformer();
 			xformer.setOutputProperty(OutputKeys.METHOD, "xml");
 			xformer.setOutputProperty(OutputKeys.INDENT, "yes");
 			xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
 			xformer.transform(source, dest);
 			destStream.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
