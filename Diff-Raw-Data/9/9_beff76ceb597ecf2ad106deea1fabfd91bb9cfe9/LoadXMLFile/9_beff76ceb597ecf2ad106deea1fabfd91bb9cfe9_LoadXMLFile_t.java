 
 //=================================================
 
 //Sorry about this ugly comment block!
 
 //Do we need to cast the nodes to elements?
 
 //Would the line: 
 // String obstacleName = doc.getElementsByTagName("Obstacle_Name").item(0).getTextContent();
 // do the same thing?
 
 // Also, I am personally (I admit this is a matter of opinion), 
 // not a fan of shortened variable names e.g "len" vs. "length"
 
 //Finally - See last method, it needs a javadoc, and I don't know what it is doing :p
 
 //==================================================
 
 
 package Model;
 
 import javax.swing.JFileChooser;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Node;
 import org.w3c.dom.Element;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Class to load an object from an XML file
  * @author Oscar
  */
 public class LoadXMLFile {
 
 	File xmlFile;
 	Airport airport = null;
 	Obstacle obstacle = null;
 	ArrayList<Contact> contacts;
 	ArrayList<Runway> runways;
 	int index = 0; //used to iterate over runways
 	boolean toPromtOrNotToPromt = true;//says whether to open a prompt when opening a file or not.
 	String fileAddress = "";
 
 	/**
 	 * TODO: this
 	 */
 	public LoadXMLFile() {
 		// this.loadFile();
 	}
 	
 	/**
 	 * @param String representing address of file to be loaded.
 	 */
 	public Obstacle silentLoadObstacle(String address){
 		toPromtOrNotToPromt = false;
 		fileAddress = address;
 		try {
 			return this.loadObstacle();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 		
 	}
 	
 	/**
 	 * @param String representing address of file to be loaded.
 	 */
 	public Airport silentLoadAirport(String address){
 		toPromtOrNotToPromt = false;
 		fileAddress = address;
 		try {
 			return this.loadAirport();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 		
 	}
 	
 	/**
 	 * @param String representing address of file to be loaded.
 	 */
 	public List<Contact> silentLoadContacts(String address){
 		toPromtOrNotToPromt = false;
 		fileAddress = address;
 		try {
 			return this.loadContacts();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 		
 	}
 
 	/**
 	 * @return Obstacle that was contained in XML
 	 * @throws Exception Exceptions to do with reading files and parsing XML
 	 */
 	public Obstacle loadObstacle() throws Exception{		
 		if (toPromtOrNotToPromt == true){
 			JFileChooser fileChooser = new JFileChooser();
 			XMLFileFilter fileFilter = new XMLFileFilter();
 			fileChooser.setFileFilter(fileFilter);
 			int returnValue = fileChooser.showOpenDialog(null);
 		
 			if (returnValue == JFileChooser.APPROVE_OPTION) {
 				xmlFile = fileChooser.getSelectedFile();
 			} else {
 				System.out.println("Open command cancelled by user.");
 			}
 		}else{
 			xmlFile = new File(fileAddress);
 		}
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			Document document = dBuilder.parse(xmlFile);
 			document.getDocumentElement().normalize();
 
 			//String root = doc.getDocumentElement().getNodeName();
 			// can add an if statement here to make sure its the right kind of file
 
 			// Creating an Obstacle object using the name from the xml file
 			NodeList airportName = document.getElementsByTagName("Obstacle_Name");
 			Node node = airportName.item(0);
 			Element element = (Element) node;
 			String obstacleName = element.getTextContent();
 
 			NodeList height = document.getElementsByTagName("Height");
 			Node heightNode = height.item(0);
 			Element element2 = (Element) heightNode;
 			Double heightValue = Double.parseDouble(element2.getTextContent());
 
 			NodeList width = document.getElementsByTagName("Width");
 			Node widthNode = width.item(0);
 			Element element3 = (Element) widthNode;
 			Double widthValue = Double.parseDouble(element3.getTextContent());
 
 			NodeList length = document.getElementsByTagName("Length");
 			Node lengthNode = length.item(0);
 			Element element4 = (Element) lengthNode;
 			Double lengthValue = Double.parseDouble(element4.getTextContent());
 
 
 			obstacle = new Obstacle(obstacleName, heightValue);
 			obstacle.setWidth(widthValue);
 			obstacle.setLength(lengthValue);
 			// System.out.println(arpt.getName());
 
 			fileAddress = "";
 			toPromtOrNotToPromt = true;
 
 		return obstacle;
 	}
 
 	/**
 	 * @return Airport that was contained in XML
 	 * @throws Exception Exceptions to do with reading files and parsing XML
 	 */
 	public Airport loadAirport() throws Exception {
 
 		runways = new ArrayList<Runway>();
 
 		if (toPromtOrNotToPromt == true){
 			JFileChooser fileChooser = new JFileChooser();
 			XMLFileFilter fileFilter = new XMLFileFilter();
 			fileChooser.setFileFilter(fileFilter);
 			int returnValue = fileChooser.showOpenDialog(null);
 
 			if (returnValue == JFileChooser.APPROVE_OPTION) {
 				xmlFile = fileChooser.getSelectedFile();
 			} else {
 			//			System.out.println("Open command cancelled by user.");
 			}
 		}
 		else{
 			xmlFile = new File(fileAddress);
 		}
 		
 			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
 			Document document = documentBuilder.parse(xmlFile);
 			document.getDocumentElement().normalize();
 
 			//String root = doc.getDocumentElement().getNodeName(); This line seems unneeded
 			// can add an if statement here to make sure its the right kind of file
 
 			// Creating an Airport object using the name from the xml file
 			NodeList airportName = document.getElementsByTagName("Airport_Name");
 			Node airportNameNode = airportName.item(0);
 			Element element = (Element) airportNameNode;
 			String airportNameString = element.getTextContent();
 			airport = new Airport(airportNameString);
 			// System.out.println(arpt.getName());
 
 			NodeList nodeList = document.getElementsByTagName("Runway");
 
 			for (int temp = 0; temp < nodeList.getLength(); temp++) {
 
 				Node nNode = nodeList.item(temp);
 				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
 					Element eElement = (Element) nNode;
 
 					String name = getTagValue("RunwayName", eElement);
 					double tora = Double.parseDouble(getTagValue("TORA", eElement));//Integer.parseInt(getTagValue("TORA", eElement));
 					double asda = Double.parseDouble(getTagValue("ASDA", eElement));
 					double toda = Double.parseDouble(getTagValue("TODA", eElement));
 					double lda = Double.parseDouble(getTagValue("LDA", eElement));
 					double displacedThreshold = Double.parseDouble(getTagValue("DisplacedThreshold", eElement));
 
 					Runway runway = new Runway(name, tora, asda, toda, lda, displacedThreshold);
 					runways.add(runway);//arpt.addRunway(r);
 				}
 			}
 
 			NodeList physicalRunway = document.getElementsByTagName("PhysicalRunway");
 			for (int temp = 0; temp < physicalRunway.getLength(); temp++){
 				Node nNode = physicalRunway.item(temp);
 				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
 					Element eElement = (Element) nNode;
 
 					String name = getTagValue("Name", eElement);
 					Double resa = Double.parseDouble(getTagValue("RESA", eElement));
 					Double stopway = Double.parseDouble(getTagValue("Stopway", eElement));
 					Double blastAllowance = Double.parseDouble(getTagValue("Blast_Allowance", eElement));
 					Double runwayWidth = Double.parseDouble(getTagValue("Runway_Strip_Width", eElement));
 					Double clearedWidth = Double.parseDouble(getTagValue("Clear_And_Graded_Width", eElement));
 					Double distanceThreshold = Double.parseDouble(getTagValue("Distance_Away_From_Threshold", eElement));
 					Double distanceCenterline = Double.parseDouble(getTagValue("Distance_Away_From_Centerline", eElement));
 					Double angle = Double.parseDouble(getTagValue("Angle_Of_Slope", eElement));
 					
 					
 					PhysicalRunway physicalRunwayObject = new PhysicalRunway(name, runways.get(index), runways.get(index+1));
					physicalRunwayObject.setResa(resa);//.setRESA(0, resa);
					physicalRunwayObject.setStopway(stopway);//.setStopway(0, stopway);
					physicalRunwayObject.setBlastAllowance(blastAllowance);//.setBlastAllowance(0, blastAllowance);
 					physicalRunwayObject.setRunwayStripWidth(runwayWidth);
 					physicalRunwayObject.setClearedAndGradedWidth(clearedWidth);
 					physicalRunwayObject.setDistanceAwayFromThreshold(distanceThreshold);
 					physicalRunwayObject.setDistanceAwayFromCenterLine(distanceCenterline);
					physicalRunwayObject.setAngleOfSlope(angle);//.setAngleOfSlope(0, angle);
 					
 					
 					airport.addPhysicalRunway(physicalRunwayObject);
 					index = index + 2;
 				}
 
 
 			}
 
 			fileAddress = "";
 			toPromtOrNotToPromt = true;
 
 		return airport;
 	}
 	
 	/**
 	 * @return ArrayList<Contact> that was contained in XML
 	 * @throws Exception Exceptions to do with reading files and parsing XML
 	 */
 	public ArrayList<Contact> loadContacts() throws Exception {
 
 		contacts = new ArrayList<Contact>();
 
 		if (toPromtOrNotToPromt == true){
 			JFileChooser fileChooser = new JFileChooser();
 			XMLFileFilter fileFilter = new XMLFileFilter();
 			fileChooser.setFileFilter(fileFilter);
 			int returnValue = fileChooser.showOpenDialog(null);
 
 			if (returnValue == JFileChooser.APPROVE_OPTION) {
 				xmlFile = fileChooser.getSelectedFile();
 			} else {
 			//			System.out.println("Open command cancelled by user.");
 			}
 		}
 		else{
 			xmlFile = new File(fileAddress);
 		}
 		
 			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
 			Document document = documentBuilder.parse(xmlFile);
 			document.getDocumentElement().normalize();
 
 			//String root = doc.getDocumentElement().getNodeName(); This line seems unneeded
 			// can add an if statement here to make sure its the right kind of file
 
 
 			NodeList nodeList = document.getElementsByTagName("Contact");
 
 			for (int temp = 0; temp < nodeList.getLength(); temp++) {
 
 				Node nNode = nodeList.item(temp);
 				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
 					Element eElement = (Element) nNode;
 
 					String firstName = getTagValue("First_Name", eElement);
 					String lastName = getTagValue("Last_Name", eElement);
 					String email = getTagValue("Email_Address", eElement);
 				
 					Contact contact = new Contact(firstName, lastName, email);
 					contacts.add(contact);
 				}
 			}
 
 			fileAddress = "";
 			toPromtOrNotToPromt = true;
 
 		return contacts;
 	}
 
 	/**
 	 * @return Value of the node in the XML file.
 	 * @param String representing the tag name and an Element object.
 	 */
 	private static String getTagValue(String sTag, Element eElement) {
 
 		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
 		Node nValue = (Node) nlList.item(0);
 		return nValue.getNodeValue();
 
 	}
 	
 	public String getFilename(){
 		return xmlFile.getPath();
 	}
 
 }
