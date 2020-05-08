 import org.apache.xerces.parsers.DOMParser;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import java.io.IOException;
 
 import java.util.HashMap;
 import java.util.Collections;
 import java.util.Map;
 
 import java.util.logging.Logger;
 
 
 public class AuthorizationData {
 
     /** Used for logging. */
    private static Logger log = Logger.getLogger(WSAuthorization.class.toString());
 
     private Map webServices = Collections.synchronizedMap(new HashMap());
    
 
 
     private synchronized HashMap updateWebServices(String xml) {
         HashMap attributes = null;
         HashMap profiles;
         Map newWebservices = Collections.synchronizedMap(new HashMap());
 
         DOMParser parser = new DOMParser();
       
         try {
             parser.parse(xml);
         } 
         catch (SAXException e) {
             System.err.println (e);
         } 
         catch (IOException e) {
             System.err.println (e);
         }
 
 
         Document document = parser.getDocument();
         Node root = document.getDocumentElement(); // WebServices
 
         NodeList children = root.getChildNodes();
 
         for (int i = 0; i < children.getLength(); i++) {
 
             Node node = children.item(i);
             String nodeName = node.getNodeName();
 
             if (nodeName.equals("Attributes")) {
                 attributes = getAttributes(node);
             }
 
             else if (nodeName.equals("Profiles")) {
                 profiles = getProfiles(node, attributes);
             }
 
             if (nodeName.equals("WebServices")) {
                 System.out.println(nodeName);
             }
 
         }
 
  
 
       return null;
 
     }
 
 
 
 
     private HashMap getAttributes(Node attrNode) {
         HashMap attributes = new HashMap();
         NodeList attrNodes = attrNode.getChildNodes();
 
         for (int i = 0; i < attrNodes.getLength(); i++) { 
             Node node = attrNodes.item(i);
 
             if (node.getNodeType() == Node.ELEMENT_NODE) {
                 String name = node.getAttributes().getNamedItem("name").getNodeValue();
                 String sso = node.getAttributes().getNamedItem("SSO").getNodeValue();
                 attributes.put(name, new Attribute(name, sso));
             }
         }
 
         return attributes;
     }
  
 
     private HashMap getProfiles(Node profileNode, HashMap attributes) {
         HashMap profiles = new HashMap();
         NodeList profileNodes = profileNode.getChildNodes();
 
 
         for (int i = 0; i < profileNodes.getLength(); i++) { 
             Node node = profileNodes.item(i);
             
             /* <Profile> */
             if (node.getNodeType() == Node.ELEMENT_NODE) {
 
                 Profile profile = new Profile(node.getAttributes().getNamedItem("name").getNodeValue());
                 profiles.put(profile.getName(), profile);
                 NodeList attrNodes = node.getChildNodes();
 
                 for (int j = 0; j < attrNodes.getLength(); j++) { 
                     Node attrNode = attrNodes.item(j);
 
                     /* <Attribute> */
                     if (attrNode.getNodeType() == Node.ELEMENT_NODE) {
                         String attrName = attrNode.getAttributes().getNamedItem("name").getNodeValue();
                         Attribute attr = (Attribute) attributes.get(attrName);
                         
                         if (attr == null) 
                             log.severe("No such attribute: "+attrName);
                         else
                             profile.addAttribute(attr, attrNode.getAttributes().getNamedItem("SSO").getNodeValue());
                     }
                 }
         
             }
         }
        
         return profiles;
 
     }
 
 
 
    // Main Method
    public static void main (String[] args) {
        AuthorizationData wa = new AuthorizationData();
        wa.updateWebServices(args[0]);
    }
 }
 
 
