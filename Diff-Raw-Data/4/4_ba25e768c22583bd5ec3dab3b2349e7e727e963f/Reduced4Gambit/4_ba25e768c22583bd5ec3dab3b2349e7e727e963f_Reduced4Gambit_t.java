 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import lse.math.games.io.ExtensiveFormXMLReader;
 import lse.math.games.reduced.ReducedForm;
 import lse.math.games.tree.ExtensiveForm;
 import lse.math.games.tree.SequenceForm.ImperfectRecallException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
  
 
 public class Reduced4Gambit {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		if (args.length < 1 || args[0].equalsIgnoreCase("--help") || args[0].equalsIgnoreCase("-h")) {
 			System.out.println("Usage: java -jar Reduced4Gambit.jar [FILE]");
 			return;
 		}
 
 		File file = new File(args[0]);
 		
 		/* load XML into ExtensiveForm returning any errors */
 		ExtensiveForm tree = null;
 		
 		try {
 			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 			Document doc = builder.parse(file);
 			
 //			Node subroot = getExtensiveForm(doc.getFirstChild());
 //			if (subroot == null) {
 //				System.out.println("Subroot is null.");
 //			} else {
 //				System.out.println("Subroot is " + subroot);
 //			}
 //			System.out.println("Original doc element: " + doc.getDocumentElement());
 //
 //			Document doc2 = builder.newDocument();
 //			Element root = doc2.createElement("gte");
 //			doc2.appendChild(root);
 //			Node newroot = doc2.importNode(subroot, true);
 //		    doc2.getDocumentElement().appendChild(newroot);
 //		    System.out.println("New doc element: " + doc2.getDocumentElement());
 //		    
 //		    writeXmlFile(doc2, "output.xml");
 //            
 //		    System.out.println(doc2.toString());
 		    
 			ExtensiveFormXMLReader reader = new ExtensiveFormXMLReader();
 			tree = reader.load(doc);
 			ReducedForm reducedForm = new ReducedForm(tree);
//			reducedForm.printOriginalSystem();
//			reducedForm.printReducedSystem();
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SAXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ImperfectRecallException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	// This method writes a DOM document to a file
 	public static void writeXmlFile(Document doc, String filename) {
 	    try {
 	        // Prepare the DOM document for writing
 	        Source source = new DOMSource(doc);
 
 	        // Prepare the output file
 	        File file = new File(filename);
 	        Result result = new StreamResult(file);
 
 	        // Write the DOM document to the file
 	        Transformer xformer = TransformerFactory.newInstance().newTransformer();
 	        xformer.transform(source, result);
 	    } catch (TransformerConfigurationException e) {
 	    } catch (TransformerException e) {
 	    }
 	}
 	
 	static Node getExtensiveForm(Node node) {
 		if ("extensiveForm".equals(node.getNodeName())) {
 			return node;
 		}
 		
 		if (node.getFirstChild() != null) {
 			return getExtensiveForm(node.getFirstChild());
 		}
 		
 		while (node.getNextSibling() != null) {
 			node = node.getNextSibling();
 			if ("extensiveForm".equals(node.getNodeName())) {
 				return node;
 			}
 		}
 				
 		return null;
 	}
 }
