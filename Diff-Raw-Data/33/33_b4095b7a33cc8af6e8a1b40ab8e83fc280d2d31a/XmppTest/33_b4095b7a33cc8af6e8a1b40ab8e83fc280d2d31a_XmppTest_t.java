 package gov.ca.ceres;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Properties;
 
 import javax.xml.XMLConstants;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.parsers.*;
 import javax.xml.xpath.*;
 
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.ChatManager;
 import org.jivesoftware.smack.ChatManagerListener;
 import org.jivesoftware.smack.Connection;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.MessageListener;
 import org.jivesoftware.smack.Roster;
 import org.jivesoftware.smack.RosterEntry;
 import org.jivesoftware.smack.RosterListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.PacketExtension;
 import org.jivesoftware.smack.packet.Presence;
 import org.w3c.dom.*;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import _0._1.structure.message.StructuredPayloadType;
 
 import com.saic.www.precis._2009._06.structures.WorkProductDocument.WorkProduct;
 
 
 public class XmppTest {
 	
 	private static final String LISTEN_TO = "uicds@uicds.resources.ca.gov";
 	private static final String SAMPLE_URL = "http://ceres.ca.gov/mobile/?urls=" +
 			"http%3A%2F%2Fatlas.resources.ca.gov%2Farcgis%2Frest%2Fservices%2FGeoScience%2FFault_Lines%2FMapServer%3B" +
 			"http%3A%2F%2Fatlas.resources.ca.gov%2Farcgis%2Frest%2Fservices%2FGeoScience%2FShaking_1sec%2FMapServer%3B" +
 			"http%3A%2F%2Fatlas.resources.ca.gov%2Farcgis%2Frest%2Fservices%2FGeoScience%2FLiquefaction%2FMapServer&center=";
 
 	private static Properties prop = new Properties();
 	static {
 		try {
 			prop.load(new FileInputStream("/opt/uicds/sumbot.conf"));
 		} catch (IOException ex) {
 			ex.printStackTrace();
 	    }
 	}
 	private final static String USERNAME = prop.getProperty("xmppUsername");
 	private final static String PASSWORD = prop.getProperty("xmppPassword");
 	
 	private static Connection xmppConnection;
 	private static HashMap<String, Chat> chats = new HashMap<String, Chat>();
 	
 	private static LinkedList<String> handledIncidents = new LinkedList<String>();
 	
 	static UcidsTest testSoapConnection = new UcidsTest();
 	
 	public static void main(String[] args) throws XMPPException, InterruptedException {
 		ConnectionConfiguration config = new ConnectionConfiguration("uicds.resources.ca.gov", 5222);
 		xmppConnection = new XMPPConnection(config);
 		xmppConnection.connect();
 		System.out.println("Connected to xmpp server");
 	
 		xmppConnection.login(USERNAME, PASSWORD);
 		System.out.println("Authenticated");
 		
 		onConnect();
 		
 		while( true ) {
 			Thread.sleep(500);
 		}
 	}
 	
 	/**
 	 * Setup communication to fake core
 	 * 
 	 * @param conn2
 	 */
 	private static void onConnect() {
 		Roster roster = xmppConnection.getRoster();
 		roster.addRosterListener(new RosterListener(){
 			@Override
 			public void entriesAdded(Collection<String> arg0) {
 				for( String entry: arg0 ) {
 					System.out.println("Added: "+entry);
 				}
 			}
 			@Override
 			public void entriesDeleted(Collection<String> arg0) {
 				for( String entry: arg0 ) {
 					System.out.println("Added: "+entry);
 				}
 			}
 			@Override
 			public void entriesUpdated(Collection<String> arg0) {
 				for( String entry: arg0 ) {
 					System.out.println("Updated: "+entry);
 				}
 			}
 			@Override
 			public void presenceChanged(Presence arg0) {
 				System.out.println("presenceChange: "+arg0.getFrom());
 			}
 		});
 		
 		/*PacketFilter filter = new AndFilter(new PacketTypeFilter(Message.class), 
 		        new FromContainsFilter(LISTEN_TO));
 		// Assume we've created a Connection name "connection".
 
 		// First, register a packet collector using the filter we created.
 		PacketCollector myCollector = xmppConnection.createPacketCollector(filter);
 		// Normally, you'd do something with the collector, like wait for new packets.
 
 		// Next, create a packet listener. We use an anonymous inner class for brevity.
 		PacketListener myListener = new PacketListener() {
 		        public void processPacket(Packet packet) {
 		           int t = 0;
 		        }
 		    };
 		// Register the listener.
 		    xmppConnection.addPacketListener(myListener, filter);*/
 		
 		
 		ChatManager chatmanager = xmppConnection.getChatManager();
 		chatmanager.addChatListener(new ChatManagerListener(){
 			@Override
 			public void chatCreated(Chat arg0, boolean arg1) {
 				if( !arg0.getParticipant().replaceAll("/.*", "").equals(LISTEN_TO) ) return;
 				arg0.addMessageListener(new MessageListener(){
 					@Override
 					public void processMessage(Chat arg0, Message message) {
 						System.out.println("Received message: #"+message.getFrom());
 						
 						PacketExtension pe = message.getExtension("http://uicds.saic.com/xmpp/extensions/notification#event");
 						
 						sendUserMessage(pe);
 						
 					}
 				});
 			}
 		});
 	}
 	
 	private static void sendUserMessage(PacketExtension pe) {
 		WorkProduct workProduct;
 		NotificationEvent event;
 		try {
 			event = new NotificationEvent(pe.toXML().toString());
 			
			if( !event.state.equals("Active") ) {
				System.out.println("Ignoring event: "+event.id+", status: "+event.state);
				return;
			}
			if( !event.type.equals("Incident") ) {
				System.out.println("Ignoring event: "+event.id+", type: "+event.type);
				return;
			}
			if( handledIncidents.contains(event.id) ) {
				System.out.println("Ignoring event: "+event.id+", already attached product");
				return;
			}
			handledIncidents.push(event.id);
 			
 			workProduct = testSoapConnection.getIncident(event.id, event.checksum, event.version, event.type);
 		} catch (Exception e1) {
 			e1.printStackTrace();
 			return;
 		}
 		
 		String urls = "";
 		StructuredPayloadType[] payload = workProduct.getStructuredPayloadArray();
 		for( int i = 0; i < payload.length; i++ ) {
 			StructuredPayloadType spt = payload[i];
 			Incident incident = new Incident(spt.getDomNode(), workProduct.getPackageMetadata().getDomNode());
 			urls += SAMPLE_URL+incident.latitude+","+incident.longitude+",12";
 			
 			// store as sensor service
 			try {
 				testSoapConnection.createObservation(incident.groupId, "Ceres Maps - CA Hazard Layers", 
 						"Submitted: Summary Bot (CERES),<br />Link: <a href='"+urls+"'>CERES Maps<a>"
 						, incident.latitude, incident.longitude);
				String resp = testSoapConnection.createObservation(incident.groupId, "KML - CA Hazard Layers", 
 						"http://gnow.casil.ucdavis.edu:4000/rest/kml?lat="+incident.latitude+"&lng="+incident.longitude, incident.latitude, incident.longitude);
				System.out.println("Attached KML Product, UICDS response:\n"+resp);
 			} catch (Exception e) {
 				e.printStackTrace();
 			} 
 			
 			break;
 		}
 		
 		
 		
 		// send url as product back to core
 		sendUrlLink(urls);
 		
 		Roster roster = xmppConnection.getRoster();
 		ChatManager chatmanager = xmppConnection.getChatManager();
 		Collection<RosterEntry> entries = roster.getEntries();
 		Chat chat;
 		
 		for (RosterEntry entry : entries) {
 			
 			// ignore the core
 			if( entry.getUser().equals(LISTEN_TO) ) continue;
 			
 			if( chats.containsKey(entry.getUser()) ) {
 				chat = chats.get(entry.getUser());
 			} else {
 				chat = chatmanager.createChat(entry.getUser(), new MessageListener() {
 					@Override
 					public void processMessage(Chat chat, Message message) {
 						System.out.println("Received message: #"+message.getFrom());
 					}
 				});
 				chats.put(entry.getUser(), chat);
 			}
 			
 			try {
 				System.out.println("Sending to: "+chat.getParticipant());
 				
 				
 				
 				chat.sendMessage("Incident Occured:\n " +
 						"Event: "+event.id+"\n"+
 						"Type: "+event.type+"\n"+
 						"State: "+event.state+"\n"+
 						urls);
 			} catch (XMPPException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private static void sendUrlLink(String url) {
 		
 	}
 	
 	private static String parseMessage(String xml)  {
 		String message = "fail";
 		try {
 			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
 		    domFactory.setNamespaceAware(true); // never forget this!
 		    DocumentBuilder builder = domFactory.newDocumentBuilder();
 		    Document doc = builder.parse(new InputSource(new StringReader(xml)));
 
 		    message = getXpath(doc, "//cap:alert/cap:info/cap:event/text()");
 		    message += "["+getXpath(doc, "//cap:alert/cap:info/cap:severity/text()")+"]";
 		    message += " @"+getXpath(doc, "//cap:alert/cap:info/cap:area/cap:polygon/text()");
 		    
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	    
 		
 		return message+"\n\nMap: http://ceres.ca.gov/mobile";
 	}
 	
 	private static class NotificationEvent {
 		
 		public String id;
 		public String version;
 		public String type;
 		public String checksum;
 		public String state;
 		
 		public NotificationEvent(String xml) throws ParserConfigurationException, 
 													SAXException, IOException, XPathExpressionException {
 			
 			
 			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
 		    DocumentBuilder builder = domFactory.newDocumentBuilder();
 		    Document doc = builder.parse(new InputSource(new StringReader(xml)));
 
 		    id = getXpath(doc, "//notification/DataItemID/text()");
 		    type = getXpath(doc, "//notification/Type/text()");
 		    version = getXpath(doc, "//notification/Version/text()");
 		    checksum = getXpath(doc, "//notification/Checksum/text()");
 		    state = getXpath(doc, "//notification/State/text()");
 		}
 		
 		private String getXpath(Document doc, String query) throws XPathExpressionException {
 			String value = "";
 			
 			XPathFactory factory = XPathFactory.newInstance();
 		    XPath xpath = factory.newXPath();
 		    XPathExpression expr = xpath.compile(query);
 
 		    Object result = expr.evaluate(doc, XPathConstants.NODESET);
 		    NodeList nodes = (NodeList) result;
 		    for (int i = 0; i < nodes.getLength(); i++) {
 		        value = nodes.item(i).getNodeValue(); 
 		    }
 			
 			return value;
 		}
 		
 	}
 	
 	public static String getXpath(Document doc, String query) throws XPathExpressionException {
 		String value = "";
 		
 		XPathFactory factory = XPathFactory.newInstance();
 	    XPath xpath = factory.newXPath();
 	    xpath.setNamespaceContext(new PersonalNamespaceContext());
 	    XPathExpression expr = xpath.compile(query);
 
 	    Object result = expr.evaluate(doc, XPathConstants.NODESET);
 	    NodeList nodes = (NodeList) result;
 	    for (int i = 0; i < nodes.getLength(); i++) {
 	        value = nodes.item(i).getNodeValue(); 
 	    }
 		
 		return value;
 	}
 	
 	
 	public static class PersonalNamespaceContext implements NamespaceContext {
 
 	    public String getNamespaceURI(String prefix) {
 	        if (prefix == null) throw new NullPointerException("Null prefix");
 	        else if ("cap".equals(prefix)) return "urn:oasis:names:tc:emergency:cap:1.1";
 	        else if ("ims".equals(prefix)) return "http://uicds.org/IncidentManagementService";
 	        else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
 	        return XMLConstants.NULL_NS_URI;
 	    }
 
 	    // This method isn't necessary for XPath processing.
 	    public String getPrefix(String uri) {
 	        throw new UnsupportedOperationException();
 	    }
 
 	    // This method isn't necessary for XPath processing either.
 	    public Iterator getPrefixes(String uri) {
 	        throw new UnsupportedOperationException();
 	    }
 
 	}
 	
 	private static class Incident {
 		
 		private String groupId = "";
 		
 		private double latitude = 0;
 		private double longitude = 0;
 		
 		public Incident(Node node, Node metaData) {
 			// get group id
 			Node workProductProperties = getWorkProductPropertiesNode(metaData);
 			if( workProductProperties == null ) return;
 			
 			Node associatedGroups = getAssociatedGroupsNode(workProductProperties);
 			if( associatedGroups == null ) return;
 			
 			Node identifier = getIdentifierNode(associatedGroups);
 			if( identifier == null ) return;
 			groupId = getNodeValueStr(identifier);
 			
 			// now parse lat long
 			Node incident = getIncidentNode(node);
 			if( incident == null ) return;
 			
 			Node location = getIncidentLocationNode(incident);
 			if( location == null ) return;
 			
 			Node locationArea = getLocationAreaNode(location);
 			if( locationArea == null ) return;
 			
 			Node circleRegion = getAreaCircularRegionNode(locationArea);
 			if( circleRegion == null ) return;
 			
 			Node center = getCircularRegionCenterCoordinateNode(circleRegion);
 			if( center == null ) return;
 			
 			Node latitude = getGeographicCoordinateLatitudeNode(center);
 			if( latitude == null ) return;
 		
 			Node latDegVal = getLatitudeDegreeValueNode(latitude);
 			Node latMinVal = getLatitudeMinuteValueNode(latitude);
 			Node latSecVal = getLatitudeSecondValueNode(latitude);
 			
 			this.latitude = azimuth(
 					getNodeValue(latDegVal),
 					getNodeValue(latMinVal),
 					getNodeValue(latSecVal)
 			);
 			
 			Node longitude = getGeographicCoordinateLongitudeNode(center);
 			if( longitude == null ) return;
 		
 			Node lonDegVal = getLongitudeDegreeValueNode(longitude);
 			Node lonMinVal = getLongitudeMinuteValueNode(longitude);
 			Node lonSecVal = getLongitudeSecondValueNode(longitude);
 			
 			this.longitude = azimuth(
 					getNodeValue(lonDegVal),
 					getNodeValue(lonMinVal),
 					getNodeValue(lonSecVal)
 			);
 		}
 		
 		public String getNodeValueStr(Node n) {
 			return n.getChildNodes().item(0).getNodeValue();
 		}
 		
 		public double getNodeValue(Node n) {
 			return Double.parseDouble(n.getChildNodes().item(0).getNodeValue());
 		}
 		
 		public double azimuth(double deg, double min, double sec) {
 			double azi = 0;
 			if (deg < 0) {
 				azi= -1.0 * deg + 1.0 * min/60.0 + 1.0 * sec/3600.0;
 				return -1.0 * azi;
 			}
 			azi=1.0 * deg + 1.0 * min/60.0 + 1.0 * sec/3600.0;
 			return azi;
 		}
 		
 		
 		public Node getIdentifierNode(Node node) {
 			Node n = getChildNode(node, "Identifier");
 			if( n == null ) System.out.println("Could not find Identifier node in AssociatedGroups");
 			return n;
 		}
 		
 		public Node getAssociatedGroupsNode(Node node) {
 			Node n = getChildNode(node, "AssociatedGroups");
 			if( n == null ) System.out.println("Could not find AssociatedGroups node in WorkProductProperties");
 			return n;
 		}
 		
 		public Node getWorkProductPropertiesNode(Node node) {
 			Node n = getChildNode(node, "WorkProductProperties");
 			if( n == null ) System.out.println("Could not find WorkProductProperties node in PackageMetadata");
 			return n;
 		}
 		
 		public Node getLongitudeSecondValueNode(Node node) {
 			Node n = getChildNode(node, "LongitudeSecondValue");
 			if( n == null ) System.out.println("Could not find LongitudeSecondValue node in GeographicCoordinateLongitude");
 			return n;
 		}
 		
 		public Node getLongitudeMinuteValueNode(Node node) {
 			Node n = getChildNode(node, "LongitudeMinuteValue");
 			if( n == null ) System.out.println("Could not find LongitudeMinuteValue node in GeographicCoordinateLongitude");
 			return n;
 		}
 		
 		public Node getLongitudeDegreeValueNode(Node node) {
 			Node n = getChildNode(node, "LongitudeDegreeValue");
 			if( n == null ) System.out.println("Could not find LongitudeDegreeValue node in GeographicCoordinateLongitude");
 			return n;
 		}
 		
 		public Node getGeographicCoordinateLongitudeNode(Node node) {
 			Node n = getChildNode(node, "GeographicCoordinateLongitude");
 			if( n == null ) System.out.println("Could not find GeographicCoordinateLongitude node in CircularRegionCenterCoordinate");
 			return n;
 		}
 		
 		public Node getLatitudeSecondValueNode(Node node) {
 			Node n = getChildNode(node, "LatitudeSecondValue");
 			if( n == null ) System.out.println("Could not find LatitudeSecondValue node in GeographicCoordinateLatitude");
 			return n;
 		}
 		
 		public Node getLatitudeMinuteValueNode(Node node) {
 			Node n = getChildNode(node, "LatitudeMinuteValue");
 			if( n == null ) System.out.println("Could not find LatitudeMinuteValue node in GeographicCoordinateLatitude");
 			return n;
 		}
 		
 		public Node getLatitudeDegreeValueNode(Node node) {
 			Node n = getChildNode(node, "LatitudeDegreeValue");
 			if( n == null ) System.out.println("Could not find LatitudeDegreeValue node in GeographicCoordinateLatitude");
 			return n;
 		}
 		
 		public Node getGeographicCoordinateLatitudeNode(Node node) {
 			Node n = getChildNode(node, "GeographicCoordinateLatitude");
 			if( n == null ) System.out.println("Could not find GeographicCoordinateLatitude node in CircularRegionCenterCoordinate");
 			return n;
 		}
 		
 		public Node getCircularRegionCenterCoordinateNode(Node node) {
 			Node n = getChildNode(node, "CircularRegionCenterCoordinate");
 			if( n == null ) System.out.println("Could not find CircularRegionCenterCoordinate node in AreaCircularRegion");
 			return n;
 		}
 		
 		public Node getAreaCircularRegionNode(Node node) {
 			Node n = getChildNode(node, "AreaCircularRegion");
 			if( n == null ) System.out.println("Could not find AreaCircularRegion node in LocationArea");
 			return n;
 		}
 		
 		public Node getLocationAreaNode(Node node) {
 			Node n = getChildNode(node, "LocationArea");
 			if( n == null ) System.out.println("Could not find LocationArea node in IncidentLocation");
 			return n;
 		}
 		
 		public Node getIncidentLocationNode(Node node) {
 			Node n = getChildNode(node, "IncidentLocation");
 			if( n == null ) System.out.println("Could not find IncidentLocation node in Incident");
 			return n;
 		}
 		
 		public Node getIncidentNode(Node node) {
 			String t = node.toString();
 			Node n = getChildNode(node, "Incident");
 			if( n == null ) System.out.println("Could not find Incident node in payload");
 			return n;
 		}
 		
 		public Node getChildNode(Node node, String name) {
 			NodeList list = node.getChildNodes();
 			for( int i = 0; i < list.getLength(); i++ ) {
 				String t = list.item(i).getNodeName().replaceAll(".*:", "");
 				if( list.item(i).getNodeName().replaceAll(".*:", "").equals(name) ) return list.item(i);
 			}
 				
 			return null;
 		}
 		
 	}
 	
 }
