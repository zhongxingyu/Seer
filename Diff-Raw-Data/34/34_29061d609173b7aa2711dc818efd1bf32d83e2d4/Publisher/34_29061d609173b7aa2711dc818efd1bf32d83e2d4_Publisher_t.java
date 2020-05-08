 package org.deri.xmpppubsub;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
import java.net.URLDecoder;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smackx.pubsub.AccessModel;
 import org.jivesoftware.smackx.pubsub.ConfigureForm;
 import org.jivesoftware.smackx.pubsub.FormType;
 import org.jivesoftware.smackx.pubsub.LeafNode;
 import org.jivesoftware.smackx.pubsub.PayloadItem;
 import org.jivesoftware.smackx.pubsub.PubSubManager;
 import org.jivesoftware.smackx.pubsub.PublishModel;
 import org.jivesoftware.smackx.pubsub.SimplePayload;
 import org.apache.log4j.Logger;
 import org.apache.log4j.BasicConfigurator;
import com.tecnick.htmlutils.htmlentities.HTMLEntities;
import org.apache.commons.lang3.StringEscapeUtils;
 //import com.javacodegeeks.xmpp.XmppManager;
 
 /**
  * @author Maciej Dabrowski
  * @author Julia Anaya
  *
  */
 public class Publisher {
     XMPPConnection connection;
     PubSubManager mgr;
     static Logger logger = Logger.getLogger(Publisher.class);
 
     /**
      * @param userName
      * @param password
      * @param xmppserver
      * @param port
      * @return void 
      *
      */
     public Publisher(String userName, String password, String xmppserver, int port) throws XMPPException {
     	connect(userName, password, xmppserver, port); 	
     }
     
     /**
      * @param userName
      * @param password
      * @param xmppserver
      * @param port
      * @return void 
      *
      */
     public void connect(String userName, String password, String xmppserver, int port) throws XMPPException {
 	    ConnectionConfiguration config = new ConnectionConfiguration(xmppserver,port);
 	    connection = new XMPPConnection(config);
 	    connection.connect();
 	    connection.login(userName, password);
 	    logger.debug("logued in");
 
 		//Create a pubsub manager using an existing Connection
 		mgr = new PubSubManager(connection);
 		logger.debug("manager created");
     }
 
     /**
      * @return void 
      *
      */
     public void disconnect() {
     	connection.disconnect();
     }
 
 
     /**
      * @return void 
      *
      */
     public LeafNode createNode(String nodename) throws XMPPException {
 		ConfigureForm form = new ConfigureForm(FormType.submit);
 		form.setAccessModel(AccessModel.open);
 		form.setDeliverPayloads(true);
 		form.setNotifyRetract(true);
 		form.setPersistentItems(true);
 		form.setPublishModel(PublishModel.open);
 		LeafNode leaf = (LeafNode) mgr.createNode(nodename, form);
 		logger.debug("node created");
     	return leaf;
     }
 
 
     /**
      * @return void 
      *
      */
     public LeafNode getNode(String nodename) throws XMPPException {
 		LeafNode node = (LeafNode) mgr.getNode(nodename);
 		logger.debug("got node");
 		return node;
     }
 
 
     /**
      * @return void 
      *
      */
     public void send_payload() {
     	
     }
 
     /**
      * @param fileName
      * @return String
      *
      */
     public static String get_triples(String fileName) {
     	logger.debug("Reading from file.");
     	try {
     	    BufferedReader br = new BufferedReader(new FileReader(fileName));
         	StringBuilder sb = new StringBuilder();
     	    String line = br.readLine();
     	    while (line != null) {
     	    	sb.append(line + "\n");
     	    	line = br.readLine();
 	        	}
 	        	br.close();
 	        	return sb.toString();
     	} catch (IOException e) {
 		      e.printStackTrace();
 		      logger.debug(fileName);
 		      logger.debug(e);
 		      return "";
     	}
     }
     
 	/**
 	 * @param args
 	 * @TODO refactorize exceptions
 	 * @TODO disconnect on exception
 	 * @TODO manage arguments
 	 * @TODO manage/create config file
 	 * @TODO separate xmpp login in other class?
 	 * @TODO separate create node in other executable or new function getOrCreateNode?
 	 * @TODO what exactly we want to send?, a sparql query to the server, that will not be answered to the publisher but to the subscribers?, raw data?, rdf?, which serialization?
 	 * @TODO in case sending sparql query, needed encode xml entities
 	 * @TODO publisher should get data from sparql endpoint instead file?
 	 */
 	public static void main(String[] args) throws XMPPException, IOException {
 
 	    // Set up a simple configuration that logs on the console.
 	    BasicConfigurator.configure();
 	    //logger.setLevel(Level.DEBUG);
 	    logger.info("Entering application.");
 	    
 	    // declare variables
 		String username = "testuser2";
 		String password = "testuser2pass";
 		String xmppserver = "vmuss12.deri.ie";
 		int port = 5222;
 		String fileName = "src/org/deri/xmpppubsub/data/deri.ie.rdf";
 		String testid = "test" + System.currentTimeMillis();
 		//String namespace = "http://jabber.org/protocol/pubsub";
 		//String payloadXmlWithNS = "<book xmlns='pubsub:test:book'><author name='Stephen King'/></book>";
   
 	    // turn on the enhanced debugger
 	    XMPPConnection.DEBUG_ENABLED = true;
 	 
 	    Publisher p = new Publisher(username, password, xmppserver, port);
 	    
 		// Get the node
 	    LeafNode node = p.getNode("testNodeWithPayloadU2");
 			
 		// Create the node
 	    // LeafNode node = p.createNode("testNodeWithPayloadU2");
 		
 	    // Get triples to send
	    
	    
 	    //String triples = get_triples(fileName);
 	    //triples = "INSERT INTO &lt;http://mygraph&gt; {"+triples+"}";
 	    //triples = "PREFIX dc: <http://purl.org/dc/elements/1.1/> INSERT DATA INTO <http://example/bookStore> { <http://example/book3>  dc:title  'Fundamentals of Compiler Desing' }";

 	    
//		String payloadXmlWithNS = "<query xmlns='http://www.w3.org/2005/09/xmpp-sparql-binding'>"+triples+"</query>";	  	    
//		SimplePayload payloadNS = new SimplePayload("query", "http://www.w3.org/2005/09/xmpp-sparql-binding", payloadXmlWithNS);
//	    String triples = "<title>book6</title>";
//	    SimplePayload payloadNS = new SimplePayload("query", "http://www.w3.org/2005/09/xmpp-sparql-binding", triples);

	    String beginNS = "<query xmlns='http://www.w3.org/2005/09/xmpp-sparql-binding'>";
	    String endNS = "</query>";
    	String triples = "PREFIX dc:<http://purl.org/dc/elements/1.1/> INSERT DATA <http://example/book1> dc:title 'A new book' ; dc:creator 'A.N. Other' .";
//	    String query = HTMLEntities.htmlAngleBrackets(triples);
//    	String query = StringEscapeUtils.escapeHtml4(triples);
//    	triples = StringEscapeUtils.escapeHtml4(triples);
//    	String query = URLDecoder.decode(triples, "UTF-8");
//    	String query = "<![CDATA["+triples+"]]>";
//	    String query = beginNS + triples + endNS;
    	String query = beginNS+"<![CDATA["+triples+"]]>"+endNS;
    	System.out.println(query);
	    logger.debug(query);
    	
	    SimplePayload payloadNS = new SimplePayload("query", "http://www.w3.org/TR/sparql11-update/", query);
 	    PayloadItem<SimplePayload> item = new PayloadItem<SimplePayload>(testid, payloadNS);
 	    node.send(item);
 	    
 //		node.send(new PayloadItem("test" + System.currentTimeMillis(), 
 //				new SimplePayload("book4", "pubsub:test:book", "<title>book6</title>")));
 		logger.info("query sent");
 		
 		//p.disconnect();
 
 	    //System.exit(0);
 		
 	}
 }
