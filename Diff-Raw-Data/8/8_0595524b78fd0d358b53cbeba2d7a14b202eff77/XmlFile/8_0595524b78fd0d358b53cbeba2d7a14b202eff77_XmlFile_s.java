 package thesaurus.parser;
 
<<<<<<< HEAD
 import java.io.File;
 
 import javax.xml.parsers.*;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
=======
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
>>>>>>> 81fb39b51356ec6bf9830831699f5638d69e70db
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class XmlFile {
 	
 	private Document xml;
 	private String path;
 	
 	
 	public XmlFile (String path)
 	{
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		try 
 		{
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 			this.path = getClass().getResource(path).getPath();
 			this.xml = docBuilder.parse(getClass().getResource(path).getPath());
 		} catch (Exception e) 
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public String toString()
 	{
 		NodeList n = this.xml.getElementsByTagName("data");
 		
 		for(int i=0;i<n.getLength();i++)
 		{
 			System.out.println(n.item(i).getTextContent());
 			
 		}
 		return null;
 	}
 	
 	/*
 	 * for adding, ndoe needs word and id.
 	 * id can get from number of nodes, and increment
 	 * go to last child
 	 * 
 	 * 
 	 * add node, add edge method
 	 * add vertex method
 	 */
 	
 	
 	
 	//graph, then last child of graph should be last node
 	public void addNode(String word)
 	{
 		Node graph = this.xml.getElementsByTagName("graph").item(0);
 		Element node = this.xml.createElement("node");
 		Element data = this.xml.createElement("data");
 		
 	
 		
 		//get last child of graph, last node, grab id
 		Node lastNode = graph.getLastChild();
 		String id = lastNode.getAttributes().getNamedItem("id").getTextContent();
 		System.out.println(id);
 		
 		/*see if the last id is being properly printed
 		 * 
 		 * 
 		 * 
 		 * 
 		 */
 		
 		node.setAttribute("id", Integer.toString(20));
 		data.setAttribute("key","w");
 		data.setTextContent(word);
 		node.appendChild(data);
 		graph.appendChild(node);
 		saveFile();
 	}
 	
 	private void saveFile()
 	{
 		
 		TransformerFactory transformer = TransformerFactory.newInstance();
 		Transformer trans = null;
 		try {
 		trans = transformer.newTransformer();
 		} catch (TransformerConfigurationException e)
 		{
 			e.printStackTrace();
 		}
 		DOMSource source = new DOMSource(this.xml);
 		StreamResult result = new StreamResult(new File(this.path));
 		try 
 		{
 			trans.transform(source, result);
 			System.out.println("file saved to "+this.path);
 		} catch (TransformerException e) 
 		{
 				e.printStackTrace();
 		}
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 
 }
