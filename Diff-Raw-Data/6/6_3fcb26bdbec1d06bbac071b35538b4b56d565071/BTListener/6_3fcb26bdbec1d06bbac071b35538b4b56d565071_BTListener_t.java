 package org.zeroglitch.mqtt.listener;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.fusesource.mqtt.client.*;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 //import org.zeroglitch.mqtt.data.User;
 import org.zeroglitch.mqtt.data.DataManager;
 import org.zeroglitch.mqtt.data.Transaction;
 import org.zeroglitch.mqtt.data.User;
 
 public class BTListener {
 	
 	private static final Logger log = Logger.getLogger(BTListener.class.getName());
 
 	final MQTT mqtt = new MQTT();
 	
 	public BTListener() {
 		log.info(" I am bt listener");
 	}
 	
 	public String onMessage(String body) {
 		
 		log.info("xxxxxxxxxxxx HFUDK FDKL:JL:SDFKJDF  xxxxxxxxxxxxxxxxxxxxxxxxx");
 		
 		log.info("from camel: " + body);
 		try {
 			 Document doc = loadXMLFromString(body.toString());
 
 			 NodeList n = doc.getElementsByTagName("Register");
 			 
 			 log.info("nodeList length: " + n.getLength());
 
 			 if (n.getLength() > 0) {
 				Node node = n.item(0);
 				Element eElement = (Element)node;
 				log.info("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx: " +  eElement.getElementsByTagName("Username").item(0).getTextContent());
 				log.info( eElement.getElementsByTagName("Region").item(0).getTextContent());
 
 
 				DataManager dm = new DataManager();
 				User user = dm.getUser(eElement.getElementsByTagName("Username").item(0).getTextContent());
 
 				String responseMessage = "";
 				if (user == null) {
 					try {
 					dm.insertUser(eElement.getElementsByTagName("Username").item(0).getTextContent(),
 							eElement.getElementsByTagName("Region").item(0).getTextContent(),
 							5);
 						responseMessage = "SUCCESS";
 					} catch (Exception e) {
 						e.printStackTrace();
 						responseMessage = "FAILEDINSERT";
 					}
 				} else responseMessage = "USERALREADYEXISTS";
 				
 				
 				
 				BlockingConnection respCon = mqtt.blockingConnection();
 				mqtt.setClientId(eElement.getElementsByTagName("Username").item(0).getTextContent() + "responseSenderuseradd");
 				respCon.connect();
 				 //Publish messages to a topic using the publish method:
 
 				respCon.publish(eElement.getElementsByTagName("Username").item(0).getTextContent() + "response", responseMessage.getBytes(), QoS.EXACTLY_ONCE, false);
 				//Message m = respCon.receive();
 				//log.info(m.getPayload().toString());
 				log.info("responded");
 				respCon.disconnect();
 				
 
 			 } 
 			 
 			 n = doc.getElementsByTagName("Rates");
 			 if (n.getLength() > 0){
 				 log.info("rates: ");
 				 log.info("body: " + body);
 				 n = doc.getElementsByTagName("Rates");
 				 Node node = n.item(0);
 				 Element eElement = (Element)node;
 				 
 				 StringBuffer ratesMessage = new StringBuffer();
 				   ratesMessage.append("<BitmoneyExchange>");
 				   ratesMessage.append("<Rates>");
				   ratesMessage.append("<NA>356.00</NA>");
				   ratesMessage.append("<LTAM>256.00</LTAM>");
				   ratesMessage.append("<EMEA>456.00</EMEA>");
				   ratesMessage.append("<APAC>556.00</APAC>");
 				   ratesMessage.append("</Rates>");
 				   ratesMessage.append("</BitmoneyExchange>");
 				   
 				   BlockingConnection respCon = mqtt.blockingConnection();
 					mqtt.setClientId(eElement.getElementsByTagName("Username").item(0).getTextContent() + "responseSenderRates");
 					respCon.connect();
 					 //Publish messages to a topic using the publish method:
 
 					respCon.publish(eElement.getElementsByTagName("Username").item(0).getTextContent(), ratesMessage.toString().getBytes(), QoS.EXACTLY_ONCE, false);
 					//Message m = respCon.receive();
 					//log.info(m.getPayload().toString());
 					log.info("responded");
 					respCon.disconnect();
 			 }
 			 
 			 n = doc.getElementsByTagName("Balance");
 			 if (n.getLength() > 0){
 				 
 				 log.info("balance: ");
 				 log.info("body: " + body);
 				 n = doc.getElementsByTagName("Balance");
 				 Node node = n.item(0);
 				 Element eElement = (Element)node;
 				 
 				DataManager dm = new DataManager();
 				User user = dm.getUser(eElement.getElementsByTagName("Username").item(0).getTextContent());
 
 				 
 				 StringBuffer ratesMessage = new StringBuffer();
 				   ratesMessage.append("<BitmoneyExchange>");
 				   ratesMessage.append("<Balance>");
 				   ratesMessage.append("<UserId>" + user.getId() + "</UserId>");
 				   ratesMessage.append("<BalanceAmount>" + user.getBalance() + "</BalanceAmount>");
 				   ratesMessage.append("</Balance>");
 				   ratesMessage.append("</BitmoneyExchange>");
 				   
 				   BlockingConnection respCon = mqtt.blockingConnection();
 					mqtt.setClientId(eElement.getElementsByTagName("Username").item(0).getTextContent() + "responseSenderBalance");
 					respCon.connect();
 					 //Publish messages to a topic using the publish method:
 
 					respCon.publish(eElement.getElementsByTagName("Callback").item(0).getTextContent(), ratesMessage.toString().getBytes(), QoS.EXACTLY_ONCE, false);
 					//Message m = respCon.receive();
 					//log.info(m.getPayload().toString());
 					log.info("responded");
 					respCon.disconnect();
 			 }
 			 
 			 n = doc.getElementsByTagName("Buy");
 			 if (n.getLength() > 0){
 				 
 				 log.info("buy: ");
 				 log.info("body: " + body);
 				 n = doc.getElementsByTagName("Buy");
 				 Node node = n.item(0);
 				 Element eElement = (Element)node;
 				 
 				DataManager dm = new DataManager();
 				String userId = eElement.getElementsByTagName("UserId").item(0).getTextContent();
 				String credit = eElement.getElementsByTagName("Amount").item(0).getTextContent();
 				String debit = "0";
 				dm.insertTransaction(userId, credit, debit);
 				
 
 				 
 				 StringBuffer message = new StringBuffer();
 				 message.append("<BitmoneyExchange>");
 				 message.append("<Buy>");
 				 message.append("<Result>SUCCESS</Result>");
 				 message.append("</Buy>");
 				 message.append("</BitmoneyExchange>");
 				   
 				 sendCallBack(eElement.getElementsByTagName("Callback").item(0).getTextContent() + "responseSenderBalance",
 						 eElement.getElementsByTagName("Callback").item(0).getTextContent(),
 						 message);
 			 }
 			 
 			 n = doc.getElementsByTagName("Sell");
 			 if (n.getLength() > 0){
 				 
 				 log.info("Sell: ");
 				 log.info("body: " + body);
 				 n = doc.getElementsByTagName("Sell");
 				 Node node = n.item(0);
 				 Element eElement = (Element)node;
 				 
 				DataManager dm = new DataManager();
 				String userId = eElement.getElementsByTagName("UserId").item(0).getTextContent();
 				String debit = eElement.getElementsByTagName("Amount").item(0).getTextContent();
 				String credit = "0";
 				dm.insertTransaction(userId, credit, debit);
 				
 
 				 
 				 StringBuffer message = new StringBuffer();
 				 message.append("<BitmoneyExchange>");
 				 message.append("<Sell>");
 				 message.append("<Result>SUCCESS</Result>");
 				 message.append("</Sell>");
 				 message.append("</BitmoneyExchange>");
 				   
 				 sendCallBack(eElement.getElementsByTagName("Callback").item(0).getTextContent() + "responseSenderBalance",
 						 eElement.getElementsByTagName("Callback").item(0).getTextContent(),
 						 message);
 			 }
 			 
 			 n = doc.getElementsByTagName("Transactions");
 			 if (n.getLength() > 0){
 				 
 				 log.info("Transactions: ");
 				 log.info("body: " + body);
 				 n = doc.getElementsByTagName("Transactions");
 				 Node node = n.item(0);
 				 Element eElement = (Element)node;
 				 
 				 DataManager dm = new DataManager();
 				ArrayList<Transaction> elements = dm.getTransactions(eElement.getElementsByTagName("Username").item(0).getTextContent());
 
 				 
 				 StringBuffer returnData = new StringBuffer();
 				 returnData.append("<BitmoneyExchange>");
 				 returnData.append("<Transactions>");
 				 
 				 
 				 
 				 for (Transaction t: elements) {
 					 returnData.append("<Transaction>");
 					 returnData.append("<TranDate>" + t.getTrandate() + "</TranDate>");
 					 returnData.append("<Credit>" + t.getCredit() + "</Credit>");
 					 returnData.append("<Debit>" + t.getDebit() + "</Debit>");
 					 returnData.append("</Transaction>");
 				 }
 			
 				 returnData.append("</Transactions>");
 				 returnData.append("</BitmoneyExchange>");
 				   
 				   BlockingConnection respCon = mqtt.blockingConnection();
 					mqtt.setClientId(eElement.getElementsByTagName("Username").item(0).getTextContent() + "responseSenderBalance");
 					respCon.connect();
 					 //Publish messages to a topic using the publish method:
 		
 					respCon.publish(eElement.getElementsByTagName("Callback").item(0).getTextContent(), returnData.toString().getBytes(), QoS.EXACTLY_ONCE, false);
 					//Message m = respCon.receive();
 					//log.info(m.getPayload().toString());
 					log.info("responded");
 					respCon.disconnect();
 			 }
 			} catch (Exception ex) {
 			  Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
 			}
 		return "blah";
 	}
 	
 	  private void sendCallBack(String callBackId, String callBackChanel, StringBuffer message) throws Exception {
 		   BlockingConnection respCon = mqtt.blockingConnection();
 			mqtt.setClientId(callBackId);
 			respCon.connect();
 			 //Publish messages to a topic using the publish method:
 
 			respCon.publish(callBackChanel, message.toString().getBytes(), QoS.AT_LEAST_ONCE, false);
 			//Message m = respCon.receive();
 			//log.info(m.getPayload().toString());
 			log.info("responded");
 			respCon.disconnect();
 	  }
 	  
 	  public static Document loadXMLFromString(String xml) throws Exception {
 	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 	        DocumentBuilder builder = factory.newDocumentBuilder();
 	        InputSource is = new InputSource(new StringReader(xml));
 	        return builder.parse(is);
 	    }
 	  
 	  public static void main(String args[]) {
 		  new BTListener();
 	  }
 }
