 package com.chalmers.frapp.database;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import android.util.Pair;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 import com.google.android.maps.GeoPoint;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 public class Parser extends DefaultHandler {
 
 	private enum XMLTags {
 		BUILDINGS,
 		BUILDING,
 		ENTRANCES,
 		ENTRANCE,
 		ADDRESS,
 		GEOPOINT,
 		LOCATIONS,
 		LOCATION,
 		ENTRANCEID,
 		CATEGORY,
 		DESCRIPTION
 	};
 
 	private LocationDatabase database;
 	private LinkedList<Pair<XMLTags, Object>> parseStack;
 	
 	public Parser(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
 		database = new LocationDatabase();
 		parseStack = new LinkedList<Pair<XMLTags, Object>>();
 		
 		// getting SAXParserFactory instance
 		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
 	
 		// Getting SAXParser object from AXParserFactory instance
 		SAXParser saxParser = saxParserFactory.newSAXParser();
 	
 		// Parsing XML Document by calling parse method of SAXParser class
 		saxParser.parse(inputStream, this);
 	}
 
 	public LocationDatabase getDatabase() {
 		return database;
 	}
 
 	private GeoPoint parseGeopoint(Attributes atts) throws SAXException {
 		Integer latitude = null;
 		Integer longitude = null;
 		int attributeCount = 0;
 		for(int i = 0; i < atts.getLength(); i++) {
 			if(latitude == null && atts.getLocalName(i).toLowerCase().equals("lat")) {
 				latitude = (int) (Double.parseDouble(atts.getValue(i)) * 1E6);
 			} else if(longitude == null && atts.getLocalName(i).toLowerCase().equals("long")) {
 				longitude = (int) (Double.parseDouble(atts.getValue(i)) * 1E6);
 			} else {
 				throw new SAXException("Unexpected attribute: " + atts.getLocalName(i) + ", expected lat and long.");
 			}
 			attributeCount++;
 		}
 
 		if(attributeCount != 2) {
 			throw new SAXException("Unexpected attribute count: " + attributeCount + ", expected two.");
 		}
 		return new GeoPoint(latitude, longitude);
 	}
 
     public void startElement(String uri, String localName, String qName,
             Attributes atts) throws SAXException {
     	if(parseStack.size() == 0) {
     		if(localName.toLowerCase().equals("buildings")) {
         		parseStack.add(new Pair<XMLTags, Object>(XMLTags.BUILDINGS, null));	
     		} else {
     			throw new SAXException("Unexpected tag: " + localName + ", expected buildings.");
     		}
     	} else if(parseStack.peekLast().first.equals(XMLTags.BUILDINGS)) {
     		if(localName.toLowerCase().equals("building")) {
         		int attributeCount = 0;
         		for(int i = 0; i < atts.getLength(); i++) {
         			if(atts.getLocalName(i).toLowerCase().equals("name")) {
         				parseStack.add(new Pair<XMLTags, Object>(XMLTags.BUILDING, database.addBuilding(atts.getValue(i))));			
         			} else {
         				throw new SAXException("Unexpected attribute: " + atts.getLocalName(i) + ", expected name.");
         			}
         			attributeCount++;
         		}
      
         		if(attributeCount != 1) {
         			throw new SAXException("Unexpected attribute count: " + attributeCount + ", expected one.");
         		}
     		} else {
     			throw new SAXException("Unexpected tag: " + localName + ", expected building.");
     		}
     	} else if(parseStack.peekLast().first.equals(XMLTags.BUILDING)) {
     		if(localName.toLowerCase().equals("entrances")) {
     			parseStack.add(new Pair<XMLTags, Object>(XMLTags.ENTRANCES, parseStack.peekLast().second));
     		} else if(localName.toLowerCase().equals("locations")) {
     			parseStack.add(new Pair<XMLTags, Object>(XMLTags.LOCATIONS, parseStack.peekLast().second));
     		} else {
     			throw new SAXException("Unexpected tag: " + localName + ", expected entrances or locations.");
     		}
     	} else if(parseStack.peekLast().first.equals(XMLTags.ENTRANCES)) {
     		if(localName.toLowerCase().equals("entrance")) {
         		int attributeCount = 0;
         		for(int i = 0; i < atts.getLength(); i++) {
         			if(atts.getLocalName(i).toLowerCase().equals("id")) {
         				ArrayList<Object> entranceData = new ArrayList<Object>(3);
         				entranceData.add(atts.getValue(i));
         				entranceData.add(null);
         				entranceData.add(null);
         				parseStack.add(new Pair<XMLTags, Object>(XMLTags.ENTRANCE, entranceData));			
         			} else {
         				throw new SAXException("Unexpected attribute: " + atts.getLocalName(i) + ", expected id.");
         			}
         			attributeCount++;
         		}
 
         		if(attributeCount != 1) {
         			throw new SAXException("Unexpected attribute count: " + attributeCount + ", expected one.");
         		}
     		} else {
     			throw new SAXException("Unexpected tag: " + localName + ", expected entrance.");
     		}
     	} else if(parseStack.peekLast().first.equals(XMLTags.ENTRANCE)) {
     		ArrayList<Object> entranceData = (ArrayList<Object>) parseStack.peekLast().second;
     		if(entranceData.get(1) == null && localName.toLowerCase().equals("address")) {
     			parseStack.add(new Pair<XMLTags, Object>(XMLTags.ADDRESS, entranceData));
     		} else if(entranceData.get(2) == null && localName.toLowerCase().equals("geopoint")) {
         		entranceData.set(2, parseGeopoint(atts));
         		parseStack.add(new Pair<XMLTags, Object>(XMLTags.GEOPOINT, null));
     		} else {
     			throw new SAXException("Unexpected tag: " + localName + ", expected address or geopoint.");
     		}
     	} else if(parseStack.peekLast().first.equals(XMLTags.LOCATIONS)) {
     		if(localName.toLowerCase().equals("location")) {
         		int attributeCount = 0;
         		for(int i = 0; i < atts.getLength(); i++) {
         			if(atts.getLocalName(i).toLowerCase().equals("name")) {
         				ArrayList<Object> locationData = new ArrayList<Object>(5);
         				locationData.add(atts.getValue(i));
         				locationData.add(new LinkedList<String>());
         				locationData.add(null);
         				locationData.add(null);
         				locationData.add(null);
         				parseStack.add(new Pair<XMLTags, Object>(XMLTags.LOCATION, locationData));
         			} else {
         				throw new SAXException("Unexpected attribute: " + atts.getLocalName(i) + ", expected name.");
         			}
         			attributeCount++;
         		}
 
         		if(attributeCount != 1) {
         			throw new SAXException("Unexpected attribute count: " + attributeCount + ", expected one.");
         		}
     		} else {
     			throw new SAXException("Unexpected tag: " + localName + ", expected location.");
     		}
     	} else if(parseStack.peekLast().first.equals(XMLTags.LOCATION)) {
     		ArrayList<Object> locationData = (ArrayList<Object>) parseStack.peekLast().second;
     		if(localName.toLowerCase().equals("entrance")) {
     			parseStack.add(new Pair<XMLTags, Object>(XMLTags.ENTRANCEID, locationData.get(1)));
     		} else if(localName.toLowerCase().equals("category")) {
     			// Unsupported for now... null operation.
     			locationData.set(2, "");
     			parseStack.add(new Pair<XMLTags, Object>(XMLTags.CATEGORY, null));
     		} else if(localName.toLowerCase().equals("geopoint")) {
     			locationData.set(3, parseGeopoint(atts));
         		parseStack.add(new Pair<XMLTags, Object>(XMLTags.GEOPOINT, null));
    		} else if(localName.toLowerCase().equals("descirption")) {
     			parseStack.add(new Pair<XMLTags, Object>(XMLTags.DESCRIPTION, locationData));
     		} else {
    			throw new SAXException("Unexpected tag: " + localName + ", expected entrance, category, geopoint or descirption.");
     		}
     	}
     }
 
     public void characters(char[] ch, int start, int length)
     		throws SAXException {
     	if(parseStack.peekLast().first.equals(XMLTags.ADDRESS)) {
     		ArrayList<Object> entranceData = (ArrayList<Object>) parseStack.peekLast().second;
     		String address = new String(ch, start, length);
     		entranceData.set(1, address);
     	} else if(parseStack.peekLast().first.equals(XMLTags.ENTRANCEID)) {
     		LinkedList<String> entrances = (LinkedList<String>) parseStack.peekLast().second;
     		String entranceID = new String(ch, start, length);
     		entrances.add(entranceID);
     	} else if(parseStack.peekLast().first.equals(XMLTags.CATEGORY)) {
     		// Unsupported for now... null operation.
     	} else if(parseStack.peekLast().first.equals(XMLTags.DESCRIPTION)) {
     		ArrayList<Object> locationData = (ArrayList<Object>) parseStack.peekLast().second;
     		String description = new String(ch, start, length);
     		locationData.set(4, description);
     	}
     }
     
     public void endElement(String uri, String localName, String qName) 
             throws SAXException {
     	Pair<XMLTags, Object> current = parseStack.removeLast();
     	if(current.first.equals(XMLTags.ENTRANCE)) {
     		ArrayList<Object> entranceData = (ArrayList<Object>) current.second;
     		Building b = (Building) parseStack.peekLast().second;
     		b.addEntrance((String) entranceData.get(0), (String) entranceData.get(1), (GeoPoint) entranceData.get(2));
     	} else if(current.first.equals(XMLTags.LOCATION)) {
     		ArrayList<Object> locationData = (ArrayList<Object>) current.second;
     		Building b = (Building) parseStack.peekLast().second;
     		// Note: category is missing.
    		b.addRoom((String) locationData.get(0), (String) locationData.get(4), (LinkedList<String>) locationData.get(1), (GeoPoint) locationData.get(2));
     	}
     }
  
     public void endDocument() throws SAXException {
     	if(parseStack.size() != 0) {
     		throw new SAXException("Unexpected end of document.");
     	}
     }
 }
