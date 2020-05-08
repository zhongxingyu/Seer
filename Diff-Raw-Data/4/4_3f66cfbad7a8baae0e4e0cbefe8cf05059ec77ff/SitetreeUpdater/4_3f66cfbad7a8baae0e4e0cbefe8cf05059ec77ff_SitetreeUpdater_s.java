 package org.apache.lenya.svn;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.ListIterator;
 
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
 
 import org.tmatesoft.svn.core.SVNErrorCode;
 import org.tmatesoft.svn.core.SVNErrorMessage;
 import org.tmatesoft.svn.core.SVNException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /** updates sitetree.xml nodes with local svn changes **/
 public class SitetreeUpdater {
 
   static Document document;
   static boolean debug;
   
   /** receive sitetree.xml location, path of new file, title of new file updateSitetree 
    * @throws SVNException */
   public static void updateSitetree (String pathToSitetree, ArrayList newNodes, boolean debug_) throws SVNException
 	  {
 	      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
           debug = debug_;
           
 	      try {
 	    	  
               File sitetree = new File(pathToSitetree);              
               if (!sitetree.exists()) {
                 SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.UNKNOWN, 
                    "Could not find sitetree.xml, make sure your repositories point" +
                    "to the authoring of a Lenya repository");
                 throw new SVNException(err);
               }
               
 	    	  DocumentBuilder builder = factory.newDocumentBuilder();
 	    	  document = builder.parse(sitetree);
 
               // sort by lowest depth first
               Collections.sort(newNodes, new DepthComparator());
               
               ListIterator it = newNodes.listIterator();
               while (it.hasNext()) {
                 addNode((String)it.next());
               }
               
 	          writeSitetree(document, pathToSitetree);
 
 	      } catch (SAXException sxe) {
 	         // Error generated during parsing
 	         Exception  x = sxe;
 	         if (sxe.getException() != null)
 	             x = sxe.getException();
 	         x.printStackTrace();
 	         System.exit(1);
 
 	      } catch (ParserConfigurationException pce) {
 	         // Parser with specified options can't be built
 	         pce.printStackTrace();
 	         System.exit(1);
 
 	      } catch (IOException ioe) {
 	         // I/O error
 	         ioe.printStackTrace();
 	         System.exit(1);
 	      }
 	  } 
 
   static class DepthComparator implements Comparator {
 
     public int compare(Object o1, Object o2) {
 
       String s1 = (String) o1;
       String s2 = (String) o2;
 
       String [] depth1 = s1.split("/");
       String [] depth2 = s2.split("/");
       
       return (depth1.length - depth2.length);
     }
   }
   
   /**
    * @param newNode
    */
   private static void addNode(String newNode) {
     
     String [] nodePath = newNode.split("/");
 
     String newNodeName = nodePath[nodePath.length - 2];
     if (debug) {
       System.err.println("\naddNode(), newNode: " + newNode);
       System.err.println("newNodeName " + newNodeName);
       System.err.println("nodepathlengti " + nodePath.length);
     }
     
     Element current = document.getDocumentElement(); //start with root
     
     // iterate through all nodePath elements, mind the init and start values !
     for (int i=1; i< nodePath.length -2; i++) {
       if (debug) {
         System.err.println("i  " + i + ", nodePath " + nodePath[i]);
       }
       NodeList children = current.getElementsByTagName("node");
 
       for (int j = 0; j < children.getLength(); j++){
         
         // one child <node>
         Element currentChild = (Element)children.item(j);
         if (debug) {
           System.err.println("j  " + j + ", currentChild " + currentChild);
           System.err.println("currentChildid " + currentChild.getAttributeNode("id").getValue());
         }
         
         // <node> id ?= path id
         if (currentChild.getAttributeNode("id").getValue().equals(nodePath[i])){
           // this is the child on the nodePath, use it for next iteration
           current = currentChild;
         }
       }
     }
     // now we are at the parent of the new node to add, so add it.
     createNode(current, newNodeName);
   }
 
   private static void createNode(Element parent, String newNodeName) {
     if (debug) {
       System.err.println("**** createNode ");
       System.err.println("newNodeName " + newNodeName);
       System.err.println("parent " + parent + ", id: " + parent.getAttributeNode("id").getValue() );
     }
     
     Element newNode = document.createElement("node");
     newNode.setAttribute("id", newNodeName);
     parent.appendChild(newNode);
 
     Element newNodeLabel = document.createElement("label");
     newNodeLabel.setAttribute("xml:lang", "en");
     newNodeLabel.appendChild(document.createTextNode(newNodeName));
     newNode.appendChild(newNodeLabel);
   }
 
   //This method writes a DOM document to a file
   private static void writeSitetree(Document doc, String filename) {
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
     	  System.err.println("Could not write to sitetree.xml");
     	  System.exit(1);
       } catch (TransformerException e) {
     	  System.err.println("Could not write to sitetree.xml");
     	  System.exit(1);
       }
   }
 }
