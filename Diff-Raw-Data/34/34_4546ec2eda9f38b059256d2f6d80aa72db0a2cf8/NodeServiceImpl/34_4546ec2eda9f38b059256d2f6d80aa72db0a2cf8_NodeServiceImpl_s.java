 package org.esgf.service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import javax.xml.xpath.XPath;
 
 import org.esgf.web.NewsController;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 
 
 /**
  * This class provides node status update based on registry information
  *
  * @author Feiyi Wang
  *
  */
 
 public class NodeServiceImpl implements NodeService {
 
     private static String NODE_EXPR = "//registry:Node";
     private static String NODE_NAME = "hostname";
     private static String NODE_IP = "ip";
     private final static Logger LOG = LoggerFactory.getLogger(NodeServiceImpl.class);
    
     private String regfile = "";
     private XPath xpath;
     private DocumentBuilder builder;
     private Document document;
 
     public NodeServiceImpl(String regfile) {
 
         xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext( new NamespaceContext() {
             public String getNamespaceURI( String prefix) {
                 if ( prefix.equals("registry")) {
                     return "http://www.esgf.org/registry";
                 }
                 return null;
             }
 
             public String getPrefix( String namespaceURI) {
                 if (namespaceURI.equals("http://www.esgf.org/registry")) {
                     return "registry";
                 }
                 return null;
             }
 
             public Iterator<String> getPrefixes (String namespaceURI) {
                 ArrayList<String> list = new ArrayList<String>();
                 if (namespaceURI.equals("http://www.esgf.org/registry")) {
                     list.add("registry");
                 }
                 return null;
             }
         });
 
         try {
             DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
             domFactory.setNamespaceAware(true);
             builder = domFactory.newDocumentBuilder();
         } catch (Exception e) {
             e.printStackTrace();
         }
         this.regfile = regfile;
        
     }
 
     /**
      * Analyze the document, retrieve and return the node list
      */
     @Override
     public List<NodeStatus> getLiveNodeList() {
 
         NodeList nodeList = null;
        List<NodeStatus> liveNodes = new ArrayList<NodeStatus>();        
         InputSource isource = new InputSource(regfile);
 
 
         try {
             document = builder.parse(isource);
 
             Object result = xpath.evaluate(NODE_EXPR, document.getDocumentElement(), XPathConstants.NODESET);
             nodeList = (NodeList) result;
 
         } catch (XPathExpressionException e) {
             e.printStackTrace();
         } catch (SAXException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             LOG.error("Input Source is not Valid, run empty list");
             return liveNodes;
            
         }
 
         for (int i = 0; i < nodeList.getLength(); i++) {
             NamedNodeMap attrs = nodeList.item(i).getAttributes();
            String name = attrs.getNamedItem(NODE_NAME).getNodeValue();
            String ip = attrs.getNamedItem(NODE_IP).getNodeValue();
             liveNodes.add(new NodeStatus(name, ip));
 
         }
         return liveNodes;
     }
 
     private static void printNodeList(List<NodeStatus> nodeList) {
         for (NodeStatus ns : nodeList )
             System.out.println("node:" + ns.getNodeName() + "; ip:" + ns.getNodeIp());
     }
 
     public static void main(String[] argv) throws Exception {
 
         NodeService ns = new NodeServiceImpl("file:///esg/config/registration.xml");
         printNodeList(ns.getLiveNodeList());
     }
 }
