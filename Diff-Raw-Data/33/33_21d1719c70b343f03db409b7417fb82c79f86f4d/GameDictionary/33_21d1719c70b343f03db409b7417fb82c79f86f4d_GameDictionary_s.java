 package edu.berkeley.gcweb;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
import java.net.URL;
 import java.net.Socket;
 import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.*;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Document;
import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * A thread-safe implementation of a two-way mapping between game names that 
  * are internal to Gamesman (e.g., ttt) and their canonical names (e.g., 
  * Tic-Tac-Toe). Mappings are read from a simple XML file that may be passed 
  * to this class's constructor.
  * @author Ide
  */
 public class GameDictionary {
     public class HostInfo {
         private String hostName;
         private InetAddress host;
         private int port;
         
        public HostInfo(String hostname, int port) throws UnknownHostException {
             this.port = port;
             this.hostName = hostName;
            this.host = InetAddress.getByName(hostname);
         }
         public InetAddress getInetAddress() {
             return host;
         }
         public String getHostName() {
             return hostName;
         }
         public int getPort() {
             return port;
         }
         public Socket connect() throws IOException {
             return new Socket(getInetAddress(), getPort());
         }
     }
     public class GameInfo {
         private String canonicalName;
         public String uifile;
         public boolean puzzle;
         private ArrayList<HostInfo> hosts;
         private Random rand;
         
         public GameInfo(String cname) {
             this.canonicalName = cname;
             this.hosts = new ArrayList<HostInfo>();
             this.rand = new Random();
         }
         public void addHostInfo(HostInfo i) {
             this.hosts.add(i);
         }
         
         public HostInfo getHostInfo() {
             if (hosts.size() == 0) return null;
             return hosts.get(rand.nextInt(hosts.size()));
         }
         public List<HostInfo> getAllHostInfo() {
             return hosts;
         }
         public Socket connectRandom() throws IOException {
             HostInfo inf = getHostInfo();
             if (inf == null) return null;
             return inf.connect();
         }
 
         public String getCanonicalName() {
             return canonicalName;
         }
     };
     private Map<String, GameInfo> gameInfo;
     private Map<String, String> canonicalToInternal;
     private DocumentBuilder domBuilder;
     
     public GameDictionary() throws ParserConfigurationException {
         gameInfo = new HashMap<String, GameInfo>();
         canonicalToInternal = new HashMap<String, String>();
         
         DocumentBuilderFactory domFactory = 
             DocumentBuilderFactory.newInstance();
         domFactory.setNamespaceAware(true);
         domFactory.setIgnoringComments(true);
         domBuilder = domFactory.newDocumentBuilder();
     }
     
     public GameDictionary(URL xml) throws 
             ParserConfigurationException, IOException {
         this();
         unmarshal(xml);
     }
     /*
     public void marshal(File xmlFile) throws IOException {
         try {
             Document xmlDocument;
             synchronized (domBuilder) {
                 xmlDocument = domBuilder.newDocument();
             }
             
             Element rootElement = xmlDocument.createElement("game-dictionary");
             xmlDocument.appendChild(rootElement);
             
             synchronized (this) {
                 Iterator<String> internalNames = 
                     internalToCanonical.keySet().iterator();
                 while (internalNames.hasNext()) {
                     String internalName = internalNames.next();
                     String canonicalName = getCanonicalName(internalName);
                     Element gameElement = xmlDocument.createElement("game");
                     gameElement.setAttribute("internal-name", internalName);
                     gameElement.setAttribute("canonical-name", canonicalName);
                     rootElement.appendChild(gameElement);
                 }
             }
             
             TransformerFactory transformFactory = 
                 TransformerFactory.newInstance();
             Transformer transformer = transformFactory.newTransformer();
             transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             
             Source xmlSource = new DOMSource(xmlDocument);
             Result xmlOutput = new StreamResult(xmlFile);
             transformer.transform(xmlSource, xmlOutput);
         } catch (TransformerException e) {
             throw new IOException(e.getMessage());
         }
     }
     */
     public void marshal(File f) throws IOException {
         throw new UnsupportedOperationException("unimplemented");
     }
     
     private void addServers(GameInfo myInfo, NodeList children) {
         for (int j = 0; j < children.getLength(); j++) {
             Node server = children.item(j);
             if (server.getNodeType()==Node.ELEMENT_NODE && server.getNodeName()=="server") {
                 NamedNodeMap attributes = server.getAttributes();
                 Node hostNode = attributes.getNamedItem("host");
                 Node portNode = attributes.getNamedItem("port");
                 String host = hostNode.getNodeValue();
                 String portstr = portNode.getNodeValue();
                 if (host == null || portstr == null) {
                     continue;
                 }
                int port;
                HostInfo inf;
                 try {
                     myInfo.addHostInfo(new HostInfo(host, Integer.valueOf(portstr)));
                 } catch (NumberFormatException e) {
                     continue;
                 } catch (UnknownHostException e) {
                     continue;
                 }
             }
         }
     }
     public void unmarshal(URL xml) throws IOException {
         InputStream xmlStream = null;
         try {
             Document xmlDocument;
             xmlStream = xml.openStream();
             synchronized (domBuilder) {
                 xmlDocument = domBuilder.parse(xmlStream);
             }
             
             XPathFactory xpathFactory = XPathFactory.newInstance();
             XPath xpath = xpathFactory.newXPath();
             XPathExpression query = xpath.compile("//game");
             NodeList gameNodes = (NodeList)query.evaluate(
                 xmlDocument, XPathConstants.NODESET);
             
             for (int i = 0; i < gameNodes.getLength(); i++) {
                 Node game = gameNodes.item(i);
                 NamedNodeMap attributes = game.getAttributes();
                 Node internalNode = attributes.getNamedItem("internal-name");
                 Node canonicalNode = attributes.getNamedItem("canonical-name");
                 Node puzzleNode = attributes.getNamedItem("puzzle");
                 Node urlNode = attributes.getNamedItem("ui");
                 if (internalNode != null && canonicalNode != null) {
                     String internalName = internalNode.getNodeValue();
                     GameInfo myInfo = new GameInfo(canonicalNode.getNodeValue());
                     myInfo.puzzle = (puzzleNode != null);
                     myInfo.uifile = (urlNode != null) ? urlNode.getNodeValue() : null;
                     
                     addMapping(internalName, myInfo);
                     addServers(myInfo, game.getChildNodes());
                 }
             }
         } catch (SAXException e) {
             throw new IOException(e.getMessage());
         } catch (XPathExpressionException e) {
             throw new IOException(e.getMessage());
         } finally {
             if (xmlStream != null) {
                 xmlStream.close();
             }
         }
     }
     
     public synchronized void addMapping(String internal, GameInfo info) {
         gameInfo.put(internal, info);
         canonicalToInternal.put(info.getCanonicalName(), internal);
     }
     
     public synchronized void removeMappingByInternalName(String internalName) {
         String canonicalName = getCanonicalName(internalName);
         if (canonicalName != null) {
             assert internalName.equals(getInternalName(canonicalName)) : 
                 "Canonical-to-internal mapping exists but " + 
                 "internal-to-canonical mapping does not.";
             gameInfo.remove(internalName);
             canonicalToInternal.remove(canonicalName);
         }
     }
     
     public synchronized void removeMappingByCanonicalName(String canonicalName) {
         String internalName = getInternalName(canonicalName);
         if (internalName != null) {
             assert canonicalName.equals(getCanonicalName(internalName)) : 
                 "Internal-to-canonical mapping exists but " + 
                 "canonical-to-internal mapping does not.";
             canonicalToInternal.remove(canonicalName);
             gameInfo.remove(internalName);
         }
     }
     
     public synchronized GameInfo getGameInfo(String internalName) {
         GameInfo thisGame = null;
         if (gameInfo.containsKey(internalName)) {
             thisGame = gameInfo.get(internalName);
         }
         return thisGame;
     }
     
     public synchronized String getCanonicalName(String internalName) {
         String canonicalName = null;
         if (gameInfo.containsKey(internalName)) {
             canonicalName = gameInfo.get(internalName).getCanonicalName();
         }
         return canonicalName;
     }
     
     public synchronized boolean getIsPuzzle(String internalName) {
         boolean ispuzzle = false;
         if (gameInfo.containsKey(internalName)) {
             ispuzzle = gameInfo.get(internalName).puzzle;
         }
         return ispuzzle;
     }
     
     public synchronized String getUI(String internalName) {
         String uifile = internalName;
         if (gameInfo.containsKey(internalName)) {
             String ui = gameInfo.get(internalName).uifile;
             if (ui != null) {
                 uifile = ui;
             }
         }
         return uifile;
     }
     
     public synchronized String getInternalName(String canonicalName) {
         String internalName = null;
         if (canonicalToInternal.containsKey(canonicalName)) {
             internalName = canonicalToInternal.get(canonicalName);
         }
         return internalName;
     }
     
     public synchronized String[] getCanonicalNames() {
         Set<String> keys = canonicalToInternal.keySet();
         String[] names = new String[keys.size()];
         keys.toArray(names);
         return names;	
     }
     
     @Override
     public String toString() {
         StringBuilder description = new StringBuilder();
         description.append("GameDictionary ");
         String[] internalNames;
         synchronized (this) {
             internalNames = gameInfo.keySet().toArray(new String[0]);
         }
         description.append('{');
         for (int i = 0; i < internalNames.length; i++) {
             if (i > 0) {
                 description.append(", ");
             }
             String internalName = internalNames[i];
             String canonicalName = getCanonicalName(internalName);
             description.append(internalName + "=" + canonicalName);
         }
         description.append('}');
         return description.toString();
     }
 }
