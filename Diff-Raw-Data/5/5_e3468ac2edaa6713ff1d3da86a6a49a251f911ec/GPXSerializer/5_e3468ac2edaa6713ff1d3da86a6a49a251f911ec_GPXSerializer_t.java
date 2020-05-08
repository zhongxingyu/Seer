 package com.delin.speedlogger.Serialization;
 
 import java.io.File;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
  
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 import android.location.Location;
 
 public class GPXSerializer  extends Serializer {
 	static final String GPX_STR =			"gpx";
 	static final String VERSION_STR =		"version";
 	static final String VERSION_VALUE =		"1.1";
 	static final String CREATOR_STR =		"creator";
 	static final String CREATOR_VALUE =		"SpeedLogger";
 	static final String METADATA_STR =		"metadata";
 	static final String TRACK_STR = 		"trk";
 	static final String TRKSEG_STR = 		"trkseg";
 	static final String NAME_STR =			"name";
 	static final String TRKPOINT_STR = 		"trkpt";
 	static final String LATITUDE = 			"lat";
 	static final String LONGITUDE = 		"lon";
 	static final String ALTITUDE = 			"ele";
 	static final String TIME = 				"time";
 	static final String SATNUMBER = 		"sat";
 	// non standard, used for internal purposes
 	static final String SPEED = 			"speed";
 	static final String BEARING = 			"bear";
 	static final String ACCURACY = 			"acc";
 	
 	static final String FILE_EXTENSION = 	".gpx";
 	
 	static final long SEGMENT_TIME_INTERVAL = 5000; // milliseconds
 	
 	boolean mStopped = false;
 	boolean mWriteMode = true;
 	
 	String mFilename = null;
 	DocumentBuilderFactory docFactory = null;
 	DocumentBuilder docBuilder = null;
 	SimpleDateFormat mDateFormat = null;
 	long mLastAddedLocTime = 0;
 	int mGPSFixNumber = 0;
 	
 	// xml objects
 	Document mDoc = null;
 	Element mRootElement = null;
 	Element mTrack = null;
 	Element mTrackSegment = null;
 	NodeList mList; // location list from file
 	
 	public GPXSerializer() {
 		SimpleDateFormat dateFormat = new SimpleDateFormat(TIMEPATTERN_FILE);
 		mFilename = STORAGE_DIR+"/"+dateFormat.format(new Date())+FILE_EXTENSION;
 		Initialize();
 	}
 	
 	public GPXSerializer(String filename, boolean write) {
 		mFilename = STORAGE_DIR + "/" + filename;
 		mWriteMode = write;
 		Initialize();
 	}
 	
 	public Location GetFix() {
 		try{
 			if (mGPSFixNumber==mList.getLength()) mGPSFixNumber=0;
 			Node nNode = mList.item(mGPSFixNumber++);
 			//System.out.println("\nCurrent Element :" + nNode.getNodeName());
 			return NodeToLoc(nNode);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}	
 	}
 	
 	public List<Location> GetAllFixes() {
 		List<Location> locList = new ArrayList<Location>();
 		try{
 			Node nNode;
 			for(int i=0; i<mList.getLength(); ++i) {
 				nNode = mList.item(i);
 				locList.add(NodeToLoc(nNode));
 			}
 		} catch(Exception e){ e.printStackTrace();}
 		return locList;
 	}
 	
 	public void SaveAllFixes(List<Location> locList) {
 		for(Location loc : locList){
 			AddFix(loc);
 		}
 	}
 	
 	private Location NodeToLoc(Node nNode) {
 		Location loc = new Location("test"); // TODO
 		if (nNode.getNodeType() == Node.ELEMENT_NODE) {		 
 			Element eElement = (Element) nNode;
 			loc.setLatitude(Float.parseFloat(eElement.getAttribute(LATITUDE))); // we assume lat/lon is always with us
 			loc.setLongitude(Float.parseFloat(eElement.getAttribute(LONGITUDE)));
 			loc.setSpeed(Float.parseFloat(eElement.getElementsByTagName(SPEED).item(0).getTextContent()));
 			loc.setAltitude(Float.parseFloat(eElement.getElementsByTagName(ALTITUDE).item(0).getTextContent()));
 			try {
 				loc.setTime(mDateFormat.parse(eElement.getElementsByTagName(TIME).item(0).getTextContent()).getTime());
				loc.setAccuracy(Float.parseFloat(eElement.getElementsByTagName(ACCURACY).item(0).getTextContent()));			
				loc.setBearing(Float.parseFloat(eElement.getElementsByTagName(BEARING).item(0).getTextContent()));
 			} catch (ParseException e) { // null pointer exception isnt handled!
 				// that's why I don't like exceptions
 				e.printStackTrace();
 			}		
 		}
 		return loc;
 	}
 	
 	public void AddFix(Location loc) {
 		// compare times of this fix and last added, insert new segment if needed
 		if (loc.getTime()-mLastAddedLocTime>SEGMENT_TIME_INTERVAL) {
 			NewSegment();
 		}
 		
 		Attr attr=null;
 		Text secondText;
 		Element second;
 		Element point = mDoc.createElement(TRKPOINT_STR); // point
 		
 		// add lat/lon
 		attr = mDoc.createAttribute(LATITUDE);
 		attr.setValue(Double.toString(loc.getLatitude()));
 		point.setAttributeNode(attr);
 		attr = mDoc.createAttribute(LONGITUDE);
 		attr.setValue(Double.toString(loc.getLongitude()));
 		point.setAttributeNode(attr);		
 		second = mDoc.createElement(TIME); //add time
 		secondText = mDoc.createTextNode(mDateFormat.format(new Date(loc.getTime())));
 		second.appendChild(secondText);
 		point.appendChild(second);
 		
 		// add optional parameters
 		if (loc.hasAltitude()) { // add altitude if available
 			second = mDoc.createElement(ALTITUDE); //add time
 			secondText = mDoc.createTextNode(Double.toString(loc.getAltitude()));
 			second.appendChild(secondText);
 			point.appendChild(second);
 		}		
 		if (loc.hasSpeed()) { // add speed if available
 			second = mDoc.createElement(SPEED);
 			secondText = mDoc.createTextNode(Float.toString(loc.getSpeed()));
 			second.appendChild(secondText);
 			point.appendChild(second);
 		}
 		if (loc.hasBearing()) {
 			second = mDoc.createElement(BEARING);
 			secondText = mDoc.createTextNode(Float.toString(loc.getBearing()));
 			second.appendChild(secondText);
 			point.appendChild(second);
 		}
 		if (loc.hasAccuracy()) {
 			second = mDoc.createElement(ACCURACY);
 			secondText = mDoc.createTextNode(Float.toString(loc.getAccuracy()));
 			second.appendChild(secondText);
 			point.appendChild(second);
 		}
 		mTrackSegment.appendChild(point); // attach point to segment
 		mLastAddedLocTime = loc.getTime();
 	}
 	
 	private void NewDocument() {
 		Attr attr;
 		// root element
 		mDoc = docBuilder.newDocument();
 		mRootElement = mDoc.createElement(GPX_STR);
 		mDoc.appendChild(mRootElement);
 		
 		attr = mDoc.createAttribute(VERSION_STR);
 		attr.setValue(VERSION_VALUE);
 		mRootElement.setAttributeNode(attr);
 		attr = mDoc.createAttribute(CREATOR_STR);
 		attr.setValue(CREATOR_VALUE);
 		mRootElement.setAttributeNode(attr);		
 		// start new Track
 		NewTrack();	
 	}
 	
 	public void NewSegment() {
 		mTrackSegment = mDoc.createElement(TRKSEG_STR);
 		mTrack.appendChild(mTrackSegment);
 	}
 	
 	public void NewTrack() {
 		mTrack = mDoc.createElement(TRACK_STR);
 		mRootElement.appendChild(mTrack);
 	}
 	
 	public void Stop() {
 		if (!mStopped) { // once stopped do nothing
 			mStopped=true;
 			if(mWriteMode){
 				// write the content into xml file
 				File mFile = new File(mFilename);
 				File mDir = mFile.getParentFile();
 				try {
 					// if file doesn't exists, then create it
 					if(!mDir.exists()) mDir.mkdirs(); // create all needed dirs
 					if (!mFile.exists()) mFile.createNewFile();
 				}
 				catch(Exception e) {
 					return;
 				}
 				try {
 					TransformerFactory transformerFactory = TransformerFactory.newInstance();
 					Transformer transformer = transformerFactory.newTransformer();
 					transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 					DOMSource source = new DOMSource(mDoc);
 					StreamResult result = new StreamResult(mFile);
 					transformer.transform(source, result);
 					System.out.println("File saved!");
 				}
 				catch(TransformerException tfe) {
 					tfe.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	protected void finalize () {
 		Stop();
 	}
 	
 	private void Initialize() {
 		mDateFormat = new SimpleDateFormat(TIMEPATTERN);
 		docFactory = DocumentBuilderFactory.newInstance();
 		try {
 			docBuilder = docFactory.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 		if (mWriteMode)	NewDocument();
 		else PrepareToRead();
 	}	
 
 	private void PrepareToRead() {
 		try {
 			mDoc = docBuilder.parse(new File(mFilename));
 			mList = mDoc.getElementsByTagName(TRKPOINT_STR);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void DeleteGPX(String filename) {
 		new File(STORAGE_DIR + "/" + filename).delete();
 	}
 }
