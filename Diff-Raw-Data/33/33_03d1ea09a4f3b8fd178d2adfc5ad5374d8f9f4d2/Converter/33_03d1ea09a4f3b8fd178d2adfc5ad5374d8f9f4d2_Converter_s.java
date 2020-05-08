 /* 
 *  Copyright 2012-2013 Coronastreet Networks 
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 *  implied. See the License for the specific language governing 
 *  permissions and limitations under the License 
 */
 package org.coronastreet.gpxconverter;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JTextArea;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.xml.serialize.OutputFormat;
 import org.apache.xml.serialize.XMLSerializer;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 
 @SuppressWarnings("deprecation")
 public class Converter implements Runnable {
 
 	private Document inDoc;
 	private Document outDoc;
 	private String inFile;
 	private String outFile = "C:\\Temp\\temp.tcx";
 	
 	List<Trkpt> trackPoints;
 	private String rideStartTime;
 	private JTextArea statusTextArea;
 	protected String newline = "\n";
 	private String authToken;
 	private String activityType;
 	private String activityName;
 	private String deviceType;
 	private String Brand;
 	private String email;
 	private String password;
 	private boolean hasAltimeter = false;
 	private boolean runStrava = false;
 	private boolean runRWGPS = false;
 	
 	public Converter(){
 		//create a list to hold the employee objects
 		trackPoints = new ArrayList<Trkpt>();
 	}
 
 	@Override
 	public void run() {	
 		this.convert();
 	}
 	
 	public void convert () {
 		
 		//parse the GPX file and get the dom object
 		loadInFile(inFile);
 		
 		//load the Tracks from the GPX File
 		parseInFile();
 
 		if (runRWGPS) {
 			doRWGPS();
 		}
 		if (runStrava) {
 			doStrava();
 		}
 	}
 
 	// Not Currently used....but will use maybe someday...
 	private void printOutFile(){
 		try	{
 			OutputFormat format = new OutputFormat(outDoc);
 			format.setIndenting(true);
 			XMLSerializer serializer = new XMLSerializer(new FileOutputStream(new File(outFile)), format);
 
 			log("Writing out TCX file.");
 			serializer.serialize(outDoc);
 
 		} catch(IOException ie) {
 		    ie.printStackTrace();
 		}
 	}
 		
 	private void doRWGPS() {
 		RideWithGPS rwgps = new RideWithGPS();
 		rwgps.setEmail(email);
 		rwgps.setPassword(password);
 		rwgps.setTripName(activityName);
 		rwgps.setDescription(activityName);
 		rwgps.setActivityType(activityType);
 		rwgps.setStatusTextArea(statusTextArea);
 		rwgps.setTrackPoints(trackPoints);
 		if (rwgps.processData()) {
 			rwgps.upload();
 		}
 	}	
 	
 	private void doStrava() {
 		Strava strava = new Strava();
 		strava.setEmail(email);
 		strava.setPassword(password);
 		strava.setTripName(activityName);
 		strava.setActivityType(activityType);
 		strava.setHasAltimeter(hasAltimeter);
 		strava.setStatusTextArea(statusTextArea);
 		strava.setTrackPoints(trackPoints);
 		strava.setRideStartTime(rideStartTime);
 		if (strava.processData()) {
 			strava.upload();
 		}
 	}
 	
 	private String getTextValue(Element ele, String tagName) {
 		String textVal = null;
 		NodeList nl;
 		nl = ele.getElementsByTagName(tagName);
 		if(nl != null && nl.getLength() > 0) {
 			Element el = (Element)nl.item(0);
 			textVal = el.getFirstChild().getNodeValue(); 
 		} 
 		return textVal;
 	}
 
 	private String getTextValue(Element ele, String[] tagNames) {
 		String textVal = null;
 		NodeList nl;
 		for (String tagName: tagNames) {
 			nl = ele.getElementsByTagName(tagName);
 			if(nl != null && nl.getLength() > 0) {
 				Element el = (Element)nl.item(0);
 				textVal = el.getFirstChild().getNodeValue();
 			}
 		}
 		return textVal;
 	}
 	
 	private void parseInFile(){
 		log("Parsing Input File...");
 		//get the root element
 		Element docEle = (Element) inDoc.getDocumentElement();
 
 		//get a node list of  elements
 		NodeList nl = docEle.getElementsByTagName("trkpt");
 		if(nl != null && nl.getLength() > 0) {
 			for(int i = 0 ; i < nl.getLength();i++) {
 
 				//get the Trackpoint element
 				Element el = (Element)nl.item(i);
 				
 				Trkpt tp = new Trkpt();
 
 				//get the Trackpoint object
 				tp.setTime(getTextValue(el, "time"));
 				tp.setLon(el.getAttribute("lon"));
 				tp.setLat(el.getAttribute("lat"));
 				
 				tp.setElevation(getTextValue(el, "ele"));
 				
 				// These fields can have multiple names (Garmin vs. Mio, etc.)
 				tp.setHr(getTextValue(el, new String[]{"gpxtpx:hr", "heartrate"}));
 				tp.setCad(getTextValue(el, new String[]{"gpxtpx:cad", "cadence"}));
 				tp.setTemp(getTextValue(el, new String[]{"gpxtpx:atemp", "temp"}));
 				
 				//tp.dump();
 				
 				//add it to list
 				trackPoints.add(tp);
 			}
 		}
 		log("Imported " + trackPoints.size() + " trackpoints.");
 		
 		// set StartTime
 		NodeList ml = docEle.getElementsByTagName("metadata");
		rideStartTime = getTextValue((Element)ml.item(0), "time");
 		log("Importing start time as " + rideStartTime);
 	}
 	
 	private void log(String s) {
 		this.statusTextArea.append(s + "\n");
 		this.statusTextArea.repaint(1);
 	}
 
 	private void loadInFile(String file){
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 
 		try {
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			inDoc = db.parse(file);
 		}catch(ParserConfigurationException pce) {
 			log(pce.getMessage());
 		}catch(SAXException se) {
 			log(se.getMessage());
 		}catch(IOException ioe) {
 			log(ioe.getMessage());
 		}
 		
 	}
 	
 	public String getInFile() {
 		return inFile;
 	}
 
 	public void setInFile(String inFile) {
 		this.inFile = inFile;
 	}
 
 	public String getOutFile() {
 		return outFile;
 	}
 
 	public void setOutFile(String outFile) {
 		this.outFile = outFile;
 	}
 
 	public String getAuthToken() {
 		return authToken;
 	}
 
 	public void setAuthToken(String authToken) {
 		this.authToken = authToken;
 	}
 
 	public String getActivityType() {
 		return activityType;
 	}
 
 	public void setActivityType(String at) {
 		activityType = at;
 	}
 
 	public String getActivityName() {
 		return activityName;
 	}
 
 	public void setActivityName(String an) {
 		activityName = an;
 	}
 
 	public String getDeviceType() {
 		return deviceType;
 	}
 
 	public void setDeviceType(String dt) {
 		deviceType = dt;
 	}
 
 	public String getBrand() {
 		return Brand;
 	}
 
 	public void setBrand(String brand) {
 		Brand = brand;
 	}
 
 	public void setStatusTextArea(JTextArea txtArea) {
 		this.statusTextArea = txtArea;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public boolean hasAltimeter() {
 		return hasAltimeter;
 	}
 
 	public void setHasAltimeter(boolean hasAltimeter) {
 		this.hasAltimeter = hasAltimeter;
 	}
 	public void setDoStrava(boolean b) {
 		this.runStrava = b;
 	}
 
 	public void setDoRWGPS(boolean b) {
 		this.runRWGPS = b;
 	}
 
 }
