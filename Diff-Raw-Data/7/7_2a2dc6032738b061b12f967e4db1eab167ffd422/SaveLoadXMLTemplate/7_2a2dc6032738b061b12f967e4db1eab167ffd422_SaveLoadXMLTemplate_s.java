 package ussr.builder.saveLoadXML;
 
 import java.awt.Color;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.sax.SAXTransformerFactory;
 import javax.xml.transform.sax.TransformerHandler;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 
 import ussr.builder.helpers.BuilderHelper;
 import ussr.builder.labelingTools.LabelingTemplate;
 import ussr.model.Module;
 
 /**
  * The main responsibility of this class is to host the methods common to children classes.
  * It is following design pattern called TEMPLATE method. As a result contains Template,
  * common and primitive methods.  
  * @author Konstantinas
  *
  */ 
 public abstract class SaveLoadXMLTemplate implements SaveLoadXMLFileTemplateInter {
 
 	
 	protected String robotMorphologyFileDirectoryName;
 	
 	/**
 	 * The string representation of XML encoding
 	 */
 	private final static String XML_ENCODING = "ISO-8859-1";
 	
 	/**
 	 *  The empty attributes for the tag, in case there is no need to have attributes.
 	 */
 	public final static AttributesImpl EMPTY_ATT = new AttributesImpl();
 		
 	/**
 	 *  String representation of "CONNECTOR" tag. 
 	 */
 	public final static String CONNECTOR_TAG = "CONNECTOR";
 	
 	/**
 	 *  The name of the attribute for connector tag.
 	 */
 	private final static String ATT_NUMBER = "NUMBER";
 	
 	/**
 	 * Adornment of quaternion appearing when transforming  quaternion into String. 
 	 */
 	private final static String QUATERNION_ADORNMENT ="com.jme.math.Quaternion:";
 	
 	/**
 	 * Adornment of Vector3f appearing when transforming  Vector3f into String. 
 	 */
 	private final static String VECTOR3F_ADORNMENT =  "com.jme.math.Vector3f";
 	
 	/**
 	 * The states of connectors.
 	 */
 	private final static String CONNECTED = "connected", DISCONNECTED = "disconnected";
 	
 	/**
 	 *  Acts a host for common, template and primitive methods to children classes.
 	 * @param simulation, the physical simulation.
 	 */
 	public SaveLoadXMLTemplate(){
 	}
 	
 	/**	
 	 * Saves the data about simulation in chosen XML format file.
 	 * This operation is TEMPLATE method. Operation means that it should be executed on the object.
 	 * @param fileDirectoryName, the name of directory, like for example: "C:/newXMLfile". 
 	 */
 	public void saveXMLfile(UssrXmlFileTypes ussrXmlFileType, String fileDirectoryName) {
 		TransformerHandler transformerHandler = initializeTransformer(fileDirectoryName);
 		this.robotMorphologyFileDirectoryName = fileDirectoryName;
 		printOutXML(ussrXmlFileType, transformerHandler);
 	}
 
 	/**  
 	 * Loads the data about simulation from chosen XML file into simulation.
 	 * This operation is TEMPLATE method. Operation means that it should be executed on the object.
 	 * @param fileDirectoryName, the name of directory, like for example: "C:/newXMLfile".	 
 	 */
 	public void loadXMLfile(UssrXmlFileTypes ussrXmlFileType, String fileDirectoryName){
 		Document document = initializeDocument(fileDirectoryName);
 		this.robotMorphologyFileDirectoryName = fileDirectoryName;
 		loadInXML(ussrXmlFileType, document);	
 	}
 
 	/**
 	 * Initializes SAX 2.0 content handler and assigns it to newly created XML file.
 	 * This method is so-called common and at the same time "Primitive operation" for above TEMPLATE method, called "saveXMLfile(String fileDirectoryName)". 	   
 	 * @param fileDirectoryName,the name of directory, like for example: "C:/newXMLfile".
 	 * @return transformerHandler, the content handler used to print out XML format. 
 	 */
 	public TransformerHandler initializeTransformer(String fileDirectoryName) {
		File newFile = new File (fileDirectoryName + XML_EXTENSION);
 		BufferedWriter characterWriter = null;
 		try {
 			characterWriter = new BufferedWriter(new FileWriter(newFile, true));
 		} catch (IOException e) {			
 			throw new Error ("Input Output exception for file appeared and named as: "+ e.toString());
 		}
 		StreamResult streamResult = new StreamResult(characterWriter);
 		SAXTransformerFactory transformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
 		/* SAX2.0 ContentHandler*/
 		TransformerHandler transformerHandler = null;
 		try {
 			transformerHandler = transformerFactory.newTransformerHandler();
 		} catch (TransformerConfigurationException e) {
 			throw new Error ("SAX exception appeared and named as: "+ e.toString());
 		}
 		Transformer serializer = transformerHandler.getTransformer();
 		serializer.setOutputProperty(OutputKeys.ENCODING,XML_ENCODING);		
 		serializer.setOutputProperty(OutputKeys.INDENT,"yes");
 		transformerHandler.setResult(streamResult);	
 		return transformerHandler;
 	}
 	
 	/**
 	 * Initializes the document object of DOM for reading xml file.
 	 * This method is so-called common and at the same time "Primitive operation" for above TEMPLATE method, called "loadXMLfile(String fileDirectoryName)". 	   
 	 * @param fileDirectoryName,the name of directory, like for example: "C:/newXMLfile".
 	 * @return doc, DOM object of document.
 	 */
 	private Document initializeDocument(String fileDirectoryName) {
 		File file = new File(fileDirectoryName);
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db;
 		Document doc = null;
 		try {
 			db = dbf.newDocumentBuilder();
 			doc = db.parse(file);
 			doc.getDocumentElement().normalize();
 		}catch (Throwable e) {
 			throw new Error ("DOM exception appeared and named as: "+ e.toString());
 		}		
 		return doc;			
 	}
 
 	/**
 	 * Method for defining the format of XML to print into the xml file. In other words
 	 * what to save in the file about simulation.
 	 * This method is so-called "Primitive operation" for above TEMPLATE method, called "saveXMLfile(String fileDirectoryName)". 	  
 	 * @param ussrXmlFileType, the type of xml file (ROBOT or Simulation)
 	 * @param transformerHandler,the content handler used to print out XML format. 
 	 */
 	public abstract void printOutXML(UssrXmlFileTypes ussrXmlFileType, TransformerHandler transformerHandler);
 
 	/**
 	 * Method for defining the format of reading the data from XML file.  In other words
 	 * what to read from the file into simulation.
 	 *  This method is so-called "Primitive operation" for above TEMPLATE method, called "loadXMLfile(String fileDirectoryName)". 	  
 	 * @param ussrXmlFileType,the type of xml file (ROBOT or Simulation).
 	 * @param document,DOM object of document. 
 	 */
 	public abstract void loadInXML(UssrXmlFileTypes ussrXmlFileType, Document document);
 	
 	
 	/**
 	 * Prints out the first start tag in the hierarchy of XML file. Now for example this is "<MODULES>".
 	 * @param transformerHandler,the content handler used to print out XML format. 
 	 * @param firstStartTagName, the name of the tag.
 	 */
 	public void printFirstStartTag(TransformerHandler transformerHandler, XMLTagsUsed firstStartTagName){
 		try {
 			transformerHandler.startDocument();			
 			transformerHandler.startElement("","",firstStartTagName.toString(),EMPTY_ATT);
 		} catch (SAXException e) {
 			throw new Error ("SAX exception appeared and named as: "+ e.toString());
 		}
 	}
 
 	/**
 	 * Prints out the first end tag in the hierarchy of XML file(from bottom). Now for example this 
 	 * is "</MODULES>".
 	 * @param transformerHandler,the content handler used to print out XML format. 
 	 * @param firstEndTagName, the  name of the tag.
 	 */
 	public void printFirstEndTag(TransformerHandler transformerHandler, XMLTagsUsed firstEndTagName){
 		try {			
 			transformerHandler.endElement("","",firstEndTagName.toString());
 			transformerHandler.endDocument();
 		} catch (SAXException e) {
 			throw new Error ("SAX exception appeared and named as: "+ e.toString());			
 		}		
 	}
 
 	/**
 	 * Prints out the start and end tags, plus the value(data) between them. This can be anything.
 	 * In current version this is for example: "<ID>1</ID>" or "<TYPE>ATRON</TYPE>".
 	 * @param transformerHandler,the content handler used to print out XML format. 
 	 * @param tagName, the name of the tag.
 	 * @param value, the value between the tags.
 	 */
 	public void printSubTagsWithValue(TransformerHandler transformerHandler, XMLTagsUsed tagName, char[] value){
 		try {			
 			transformerHandler.startElement("","",tagName.toString(),EMPTY_ATT);
 			transformerHandler.characters(value,0,value.length);
 			transformerHandler.endElement("","",tagName.toString());
 		} catch (SAXException e) {
 			throw new Error ("SAX exception appeared and named as: "+ e.toString());		
 		}	
 	}
 
 	/**
 	 * Prints out the state of connectors for the module in simulation. 
 	 * @param transformerHandler, the content handler used to print out XML format. 
 	 * @param statesConnectors, the states of connectors, for example "connected" or disconnected.
 	 * @param numbersConnectors, the numbers of connectors, for example 1,2,3 and so on.
 	 */	
 	//FIXME MAYBE WILL BE NEEDED
 /*	public void printInfoConnectors(TransformerHandler transformerHandler,ArrayList<String> statesConnectors, ArrayList<String> numbersConnectors ){
 		AttributesImpl atts1 = new AttributesImpl();
 		for (int index=0; index<statesConnectors.size();index++){				
 			atts1.addAttribute("","",ATT_NUMBER,"first",numbersConnectors.get(index));
 			try {
 				transformerHandler.startElement("","",CONNECTOR_TAG,atts1);
 				char[] state = statesConnectors.get(index).toCharArray(); 
 				transformerHandler.characters(state,0,state.length);
 				transformerHandler.endElement("","",CONNECTOR_TAG);
 			} catch (SAXException e) {
 				throw new Error ("SAX exception appeared and named as: "+ e.toString());
 			}	
 		}		
 	}*/
 
 	/**
 	 * Returns the type of module, like for example ATRON,MTRAN and so on.
 	 * @param currentModule, the module in simulation environment
 	 * @return char[], the type of the module.
 	 */
 	public char[] getType(Module currentModule){    		
 		return currentModule.getProperty(BuilderHelper.getModuleTypeKey()).toCharArray();    	
 	}
 
 	/**
 	 * Returns the global ID of the module. 
 	 * @param currentModule, the module in simulation environment
 	 * @return char[], the global ID of the module in simulation.
 	 */
 	public char[] getID(Module currentModule){   	
 		return String.valueOf(currentModule.getID()).toCharArray();    	
 	}
 
 	/**
 	 * Returns the name of the module, which can be anything. For example: "driver","leftwheel" and so on.
 	 * @param currentModule,the module in simulation environment.
 	 * @return char[], the name of the module.
 	 */
 	public char[] getName(Module currentModule){    		
 		return currentModule.getProperty(BuilderHelper.getModuleNameKey()).toCharArray();    	
 	}
 
 	/**
 	 * Returns the rotation (RotationDescription) of the module in simulation environment, 
 	 * as rotation of its zero component.
 	 * @param currentModule,the module in simulation environment.
 	 * @return char[], the rotation as RotationDescription of the module.
 	 */
 	public char[] getRotation(Module currentModule){    		
 		return currentModule.getPhysics().get(0).getRotation().toString().toCharArray();    	
 	}
 
 	/**
 	 * Returns the rotation quaternion of the module in simulation environment, as rotation of its component
 	 * @param currentModule, the module in simulation environment.
 	 * @return moduleRotationQuat,the rotation as quaternion of component of the module.
 	 */
 	public char[] getRotationQuaternion(Module currentModule){  
 		String moduleRotationQuat = currentModule.getPhysics().get(0).getRotation().getRotation().toString();
 		moduleRotationQuat = moduleRotationQuat.replace(QUATERNION_ADORNMENT, "");
 		return moduleRotationQuat.toCharArray();		
 	}
 
 	/**
 	 * Returns the position(VectorDescription) of the module (component)
 	 * @param currentModule, the module in simulation environment.
 	 * @return modulePosition,the position(as VectorDescription) of the module (component)
 	 */
 	public char[] getPosition(Module currentModule){  
 		char[] modulePosition;		
 		if(currentModule.getProperty(BuilderHelper.getModuleTypeKey()).equalsIgnoreCase("MTRAN")){			
 			modulePosition = currentModule.getPhysics().get(1).getPosition().toString().toCharArray();			
 		}else {
 			modulePosition = currentModule.getPhysics().get(0).getPosition().toString().toCharArray();			
 		}
 		return modulePosition;
 	}
 
 	/**
 	 * Returns the position (Vector3f)of the module (component)
 	 * @param currentModule, the module in simulation environment.
 	 * @return modulePositionVect, the position (as Vector3f)of the module (component)
 	 */
 	public char[] getPositionVector(Module currentModule){ 
 		String modulePositionVect = null; 
 		if(currentModule.getProperty(BuilderHelper.getModuleTypeKey()).equalsIgnoreCase("MTRAN")){
 			modulePositionVect = currentModule.getPhysics().get(1).getPosition().getVector().toString();			
 		}else {
 			modulePositionVect = currentModule.getPhysics().get(0).getPosition().getVector().toString();			
 		}			
 		modulePositionVect = modulePositionVect.replace(VECTOR3F_ADORNMENT, "");
 
 		return modulePositionVect.toCharArray();
 	}
 
 	/**
 	 *  Returns the amount of components the module consists of.
 	 * @param currentModule, the module in simulation environment.
 	 * @return amountComponents, the amount of components the module consists of.
 	 */
 	public char[] getAmountComponents(Module currentModule){
 		int amountComponents = currentModule.getNumberOfComponents();       	
 		return (""+amountComponents).toCharArray();
 	}
 
 	/**
 	 *  Returns the colours of the module components in RGB format.
 	 * @param currentModule, the module in simulation environment.
 	 * @return colorsComponents,the colours of the module components in RGB format. 
 	 */
 	public char[] getColorsComponents(Module currentModule){
 		String colorsComponents= "";
 		int amountComponents = currentModule.getNumberOfComponents();
 		for (int component=0; component<amountComponents;component++){
 			colorsComponents = colorsComponents + currentModule.getComponent(component).getModuleComponentColor().getRed()+",";//For debugging
 			colorsComponents = colorsComponents + currentModule.getComponent(component).getModuleComponentColor().getGreen()+",";
 			colorsComponents = colorsComponents + currentModule.getComponent(component).getModuleComponentColor().getBlue()+";";
 		}		
 		return colorsComponents.toCharArray();
 	}
 
 	/** 
 	 * Returns amount of connectors on the module.
 	 * @param currentModule, the module in simulation environment.
 	 * @return amountConnectors, the amount of connectors on the module. 
 	 */
 	public char[] getAmountConnectors(Module currentModule){
 		int amountConnectors = currentModule.getConnectors().size();	
 		return (""+amountConnectors).toCharArray(); 
 	}
 
 	/**
 	 * Returns the colours of connectors on the module.
 	 * @param currentModule, the module in simulation environment.
 	 * @return colorsConnectors,the colours of connectors on the module.
 	 */
 	public char[] getColorsConnectors(Module currentModule){
 		String colorsConnectors = "";	
 		int amountConnectors = currentModule.getConnectors().size();	
 		for (int connector=0; connector<amountConnectors;connector++){
 			colorsConnectors= colorsConnectors + currentModule.getConnectors().get(connector).getColor().getRed()+",";//For debugging
 			colorsConnectors = colorsConnectors + currentModule.getConnectors().get(connector).getColor().getGreen()+",";
 			colorsConnectors = colorsConnectors + currentModule.getConnectors().get(connector).getColor().getBlue()+";";
 		}	 
 		return colorsConnectors.toCharArray();
 	}
 
 	/**
 	 * Returns the labels assigned to the module.
 	 * @param currentModule, the module in simulation environment.
 	 * @return char[], the labels assigned to the module.
 	 */
 	public char[] getLabelsModule(Module currentModule){		
 		String labels = currentModule.getProperty(BuilderHelper.getLabelsKey());
 		if (labels == null){// means there are no labels assigned to this module. 
 			labels = BuilderHelper.getTempLabel();
 		}else if (labels.contains(LabelingTemplate.NONE)){/*do nothing*/}
 		return labels.toCharArray();    	
 	}
 
 
 	/**
 	 * Returns the labels of connectors on specific module.
 	 * @param currentModule,the module in simulation environment.
 	 * @return labels, the labels of all connectors on the module.
 	 */
 	public char[] getLabelsConnectors(Module currentModule){		
 		int amountConnectors = currentModule.getConnectors().size();
 		String labels = null;
 		int counter=0;
 		for (int connector=0; connector<amountConnectors;connector++){
 			counter++;
 			String label = currentModule.getConnectors().get(connector).getProperty(BuilderHelper.getLabelsKey());
 			if (label == null||labels==null){//module do not even have labels  assigned					
 				if (counter==1){//for 0 connector do not consider labels, because they not yet exist
 				labels=BuilderHelper.getTempLabel()+ LabelingTemplate.LABEL_SEPARATOR;
 				}else{
 					labels=labels+BuilderHelper.getTempLabel()+LabelingTemplate.LABEL_SEPARATOR;
 				}
 			}else{				
 					labels = labels+label+LabelingTemplate.LABEL_SEPARATOR;				
 			}
 		}
 		return labels.toCharArray();    	
 	}
 
 
 	/**
 	 * Returns states of connectors(for example:"connected" or "disconnected") or numbers of connectors of the module.
 	 * @param currentModule, the module in simulation environment. 
 	 * @param state, flag to indicate what should be returned. True when states of connectors should be returned.
 	 * False when numbers of connectors should be returned. 
 	 * @return statesConnectors or numbersConnectors. 
 	 */
 	//FIXME Maybe will be needed later
 /*	public ArrayList<String> getInfoConnectors (Module currentModule, boolean state){
 		int amountConnectors = currentModule.getConnectors().size();
 		ArrayList<String> statesConnectors = new ArrayList<String>();
 		ArrayList<String> numbersConnectors = new ArrayList<String>();	
 		for (int connector=0; connector<amountConnectors;connector++){
 			boolean connectorState = currentModule.getConnectors().get(connector).isConnected();
 			String connectorNumber = currentModule.getConnectors().get(connector).getProperty(BuilderHelper.getModuleConnectorNrKey());
 			numbersConnectors.add(connectorNumber);			
 			if (connectorState){
 				statesConnectors.add(CONNECTED);
 			}else {
 				statesConnectors.add(DISCONNECTED);	
 			}				
 		}
 		if (state ==true){
 			return statesConnectors;
 		}else return numbersConnectors;
 	}*/
 
 	/**
 	 * Extract the data between the start and end tags.
 	 * @param firstElmnt, first appearance of the node. 
 	 * @param tagName, the name of the tag.
 	 * @return value, the data between the start and end tags.
 	 */
 	public String extractTagValue(Element firstElmnt,XMLTagsUsed tagName){
 		NodeList firstNmElmntLst = firstElmnt.getElementsByTagName(tagName.toString());		      
 		Element firstNmElmnt = (Element) firstNmElmntLst.item(0);
 		NodeList firstNm = firstNmElmnt.getChildNodes();		 
 		return ((Node) firstNm.item(0)).getNodeValue();
 	}
 
 	/**
 	 *  Extracts the colors of components from the String.
 	 * @param amountComponents, the amount of components.
 	 * @param colorsComponents, the string representing the colors in RGB form.
 	 * @return listColorsComponents, the list of colors of components. 
 	 */
 	public LinkedList<Color> extractColorsComponents(int amountComponents, String colorsComponents){
 		String[] newColorsComponents = colorsComponents.split(";");
 		LinkedList<Color> listColorsComponents = new LinkedList<Color>();
 		for (int component=0;component<amountComponents;component++){
 			String[] specificColors = newColorsComponents[component].split(",");
 			int red =  Integer.parseInt(specificColors[0]);
 			int green =  Integer.parseInt(specificColors[1]);
 			int blue =  Integer.parseInt(specificColors[2]);
 			Color  newColor = new Color(red,green,blue);
 			listColorsComponents.add(newColor); 
 		}
 		return listColorsComponents;
 	}
 
 
 	/**
 	 * Extracts the colors of connectors from the String.
 	 * @param amountConnectors, the amount of connectors. 
 	 * @param colorsConnectors,the string representing the colors in RGB form. 
 	 * @return listColorsConnectors, the list of colors of connectors.
 	 */
 	public LinkedList<Color> extractColorsConnectors(int amountConnectors, String colorsConnectors){
 		String[] newColorsConnectors= colorsConnectors.split(";");
 		LinkedList<Color> listColorsConnectors= new LinkedList<Color>();
 		for (int connector=0;connector<amountConnectors;connector++){
 			String[] specificColors = newColorsConnectors[connector].split(",");
 			int red =  Integer.parseInt(specificColors[0]);
 			int green =  Integer.parseInt(specificColors[1]);
 			int blue =  Integer.parseInt(specificColors[2]);
 			Color  newColor = new Color(red,green,blue);
 			listColorsConnectors.add(newColor);		
 		}
 		return listColorsConnectors;
 	}	
 
 	/**
 	 * Extracts the specific coordinate from Quaternion string.
 	 * @param textString, the string representing Quaternion;
 	 * @param coordinate, the coordinate to extract.
 	 * @return extractedValue, the value of coordinate.
 	 */
 	public float extractFromQuaternion(String textString, String coordinate){		
 		textString =textString.replace("]", "");
 		textString =textString.replace("x", "");
 		textString =textString.replace("y", "");
 		textString =textString.replace("z", "");
 		textString =textString.replace("w", "");
 		textString =textString.replace("=", ",");
 		String[] temporary = textString.split(",");
 
 		float extractedValue = 100000; 
 		if (coordinate.equalsIgnoreCase("X")){			
 			extractedValue = Float.parseFloat(temporary[1]);				
 		}else if (coordinate.equalsIgnoreCase("Y")){
 			extractedValue = Float.parseFloat(temporary[2]);			
 		}else if (coordinate.equalsIgnoreCase("Z")){
 			extractedValue = Float.parseFloat(temporary[3]);			
 		}else if (coordinate.equalsIgnoreCase("W")){
 			extractedValue = Float.parseFloat(temporary[4]);			
 		}else throw new Error ("There is no such coordinate");
 		return extractedValue; 
 	}
 
 
 	
 	/*EXPERIMENTAL PART FOR SIMULATION SET UP*/
 	public char[]  getControllerLocation(Module currentModule){
 		String roughControllerLocation = currentModule.getController().toString();//.toString();
 		String[]controllerLocation = null;
 		if (roughControllerLocation.contains("@")){
 			controllerLocation =  roughControllerLocation.split("@");			
 		}else if (roughControllerLocation.contains("$")){
 			controllerLocation =  roughControllerLocation.split("$");
 		}
 			
 		return controllerLocation[0].toCharArray();
 	}
 
 
 }
