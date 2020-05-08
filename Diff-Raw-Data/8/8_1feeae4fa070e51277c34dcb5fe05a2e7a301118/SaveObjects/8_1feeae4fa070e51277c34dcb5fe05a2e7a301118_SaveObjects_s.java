 package hci;
 
 import java.io.File;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
  
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.util.ArrayList;
 import hci.utils.Point;
 
 public class SaveObjects {
 	public void buildXML(ArrayList<ArrayList<hci.utils.Point>> objs, ArrayList<String> labels){
 		int nodeLabel = 0;
 		try {
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("imageLabels");
 			doc.appendChild(rootElement);
			Attr attr = doc.createAttribute("label");
 			
 			for (int i = 0; i < objs.size(); i++){
 				attr.setValue(labels.get(i));
 				Element objectNodes = doc.createElement("objectNodes");
 				objectNodes.setAttributeNode(attr);
 				rootElement.appendChild(objectNodes);
 				
 				for (hci.utils.Point point:objs.get(i)){
 					Element nodePoint = doc.createElement("nodePoint");
 					Element xPoint = doc.createElement("x");
 					xPoint.appendChild(doc.createTextNode(Integer.toString(point.getX())));
 					Element yPoint = doc.createElement("y");
 					yPoint.appendChild(doc.createTextNode(Integer.toString(point.getY())));
 					nodePoint.appendChild(xPoint);
 					nodePoint.appendChild(yPoint);
 					objectNodes.appendChild(nodePoint);
 				}
 			}
 			TransformerFactory transformerFactory = TransformerFactory.newInstance();
 			Transformer transformer = transformerFactory.newTransformer();
 			DOMSource source = new DOMSource(doc);
 			StreamResult result = new StreamResult(new File("test.xml"));
 			transformer.transform(source, result);
 			System.out.println("file saved");
 		} catch (ParserConfigurationException pce) {
 			pce.printStackTrace();
 		  } catch (TransformerException tfe) {
 			tfe.printStackTrace();
 		  }
 	}
 }
