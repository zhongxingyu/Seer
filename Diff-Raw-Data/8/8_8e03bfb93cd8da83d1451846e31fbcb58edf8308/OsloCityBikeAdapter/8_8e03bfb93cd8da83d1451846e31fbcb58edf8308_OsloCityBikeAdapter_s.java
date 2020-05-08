 /**
  *   Copyright (C) 2010, Roger Kind Kristiansen <roger@kind-kristiansen.no>
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package no.rkkc.bysykkel;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 
 import no.rkkc.bysykkel.model.Rack;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.HttpConnectionParams;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import android.util.Log;
 
 public class OsloCityBikeAdapter {
     HttpClient httpClient;
     HttpGet httpGet;
     
     private static final String TAG = "Bysyklist-OsloCityBikeAdapter";
     
     public OsloCityBikeAdapter() {
         httpGet = new HttpGet();
         
         httpClient = new DefaultHttpClient();
         httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
         httpClient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, 10000);
     }
     
     /**
      * Get all IDs corresponding to all current racks
      * 
      * @return
      * @throws URISyntaxException 
      * @throws IOException 
      */
     public ArrayList<Integer> getRacks() throws OsloCityBikeCommunicationException {
     	ArrayList<Integer> rackIds = new ArrayList<Integer>();
         String wsMethod ="getRacks";
         
         String xml;
         try {
         	xml = makeWebServiceCall(wsMethod);
         	Element docElement = getXmlDocumentElement(xml);
         	NodeList elements = docElement.getElementsByTagName("station");
         	
         	for (int i = 0; i < elements.getLength(); i++) {
         		Integer rackId = Integer.valueOf(elements.item(i).getFirstChild().getNodeValue());
         		if (rackId < 500) { // Racks with id over 500 seem to be for testing purposes
         			rackIds.add(rackId);
         		}
         	}
         } catch (IOException e) {
         	Log.v(TAG, e.getStackTrace().toString());
         	throw new OsloCityBikeCommunicationException(e);
 		} catch (SAXException e) {
 			Log.v(TAG, e.getStackTrace().toString());
 			throw new OsloCityBikeCommunicationException(e);
 		} catch (ParserConfigurationException e) {
 			Log.v(TAG, e.getStackTrace().toString());
 			throw new OsloCityBikeCommunicationException(e);
 		} catch (URISyntaxException e) {
 			Log.v(TAG, e.getStackTrace().toString());
 			throw new OsloCityBikeCommunicationException(e);
 		}
         
         return rackIds;
     }
     
     public Rack getRack(int id) throws OsloCityBikeException {
 		HashMap<String, String> rackMap = getRackInfo(id);
 		
 		Boolean online = null;
 		if (rackMap.get("online") != null) {
 			online = Integer.parseInt(rackMap.get("online")) == 1? true : false;
 		}
 		
 		String description = rackMap.get("description");
 		
 		// Get rid of the ID from the string.
 		if (description.indexOf("-") > -1) {
 			description = description.substring(description.indexOf("-")+1);
 		}
 		
 		Integer latitude = null;
 		if (rackMap.get("latitude") != null) {
 			Double latitudeE6 = Float.parseFloat(rackMap.get("latitude"))*1E6;
 			latitude = latitudeE6.intValue();
 		}
 			
 		Integer longitude = null;
 		if (rackMap.get("longitute") != null) {
 			Double longitudeE6 = Float.parseFloat(rackMap.get("longitute"))*1E6;
 			longitude = longitudeE6.intValue();
 		}
 		
 		Integer emptyLocks = null;
 		if (rackMap.containsKey("empty_locks")
 				&& rackMap.get("empty_locks") != null
 				&& rackMap.get("empty_locks") != "") {
 			emptyLocks = Integer.parseInt(rackMap.get("empty_locks"));
 		}
 		
 		Integer readyBikes = null;
 		if (rackMap.containsKey("ready_bikes")
 				&& rackMap.get("ready_bikes") != null
 				&& rackMap.get("ready_bikes") != "") {
 			readyBikes = Integer.parseInt(rackMap.get("ready_bikes"));
 		}
 		
 		Rack rack = new Rack(id, description, latitude, longitude, 
 								online, emptyLocks, readyBikes);
 		
 		return rack;
     }
     
     /** 
      * Retrieve rack info XML from ClearChannel and extract relevant info.
      * 
      * We expect an XML on the following format:
      * 
      * <?xml version="1.0" encoding="utf-8"?>
      * <string xmlns="http://smartbikeportal.clearchannel.no/public/mobapp/">
      *         <station>
      *             <online>1</online>
      *             <ready_bikes>1</ready_bikes>
      *             <empty_locks>17</empty_locks>
      *             <description>02-Middelthunsgate 28 (utenfor Frognerbadet)</description>
      *             <longitute>10.708515644073486</longitute>
      *            <latitude>59.92805479800621</latitude>
      *        </station>
      * </string>
      **/
     private HashMap<String, String> getRackInfo(int id) throws OsloCityBikeException {
         String wsMethod = "getRack?id=".concat(String.valueOf(id));
         String xml;
 
         try {
         	xml = makeWebServiceCall(wsMethod);
         	Log.v(OsloCityBikeAdapter.TAG, xml); // TODO: Remove this logging before release
         } catch (Exception e) {
         	throw new OsloCityBikeCommunicationException(e);
         }
         
         try {
         	Element docElement = getXmlDocumentElement(xml);
         	
             // These are all the elements we wish to extract from the XML
             String elements[] = {"online", "description", "longitute", "latitude", "ready_bikes", "empty_locks"};
             HashMap<String, String> rackMap = new HashMap<String, String>();
             
             for (int i = 0; i < elements.length; i++) {
                 if (docElement.getElementsByTagName(elements[i]).getLength() > 0
                 	&& docElement.getElementsByTagName(elements[i]).item(0).hasChildNodes()) {
                     rackMap.put(elements[i], docElement.getElementsByTagName(elements[i]).item(0).getFirstChild().getNodeValue().trim());
                 } else {
                     rackMap.put(elements[i], null);
                 }
             }
 
             return rackMap;
         } catch (Exception e) {
         	Log.e("OsloCityBikeAdapter", e.getStackTrace().toString());
             throw new OsloCityBikeParseException(e);
         }
     }
 
 	/**
 	 * Get the root DocumentElement of a given XML
 	 * 
 	 * @param xml
 	 * @return
 	 * @throws ParserConfigurationException
 	 * @throws FactoryConfigurationError
 	 * @throws SAXException
 	 * @throws IOException
 	 */
 	private Element getXmlDocumentElement(String xml)
 			throws ParserConfigurationException, FactoryConfigurationError,
 			SAXException, IOException {
 		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 		Document document = builder.parse(new InputSource(new StringReader(xml)));
 		Element docElement = document.getDocumentElement();
 		return docElement;
 	}
 
     /**
      * Make a web service call to ClearChannel
      * 
      * Handles the requesting of the given resource as well as parsing of the
      * response. Methods that are known to work:
      * 
      * getRack?id=<integer>    - Retrieves information about the rack with the given id
      * getRacks                - Retrieves a complete list of rack IDs
      * 
      * @param method String
      * @param xml
      * @return
      * @throws Exception 
      */
    private String makeWebServiceCall(String method) throws IOException, URISyntaxException {
         
         httpGet.setURI(new URI("http://smartbikeportal.clearchannel.no/public/mobapp/maq.asmx/".concat(method)));
         
         HttpResponse httpResponse = null;
         httpResponse = httpClient.execute(httpGet);
 
         try {
             String returnXml = "";
             returnXml = convertStreamToString(httpResponse.getEntity().getContent()).replace("\n", "");
             returnXml = returnXml.replace("&lt;", "<");
             returnXml = returnXml.replace("&gt;", ">");
             
             return returnXml;
         } catch (IOException e) {
         	Log.e("OsloCityBikeAdapter", e.getStackTrace().toString());
         	throw e;
         }
     }
     
     private static String convertStreamToString(InputStream is) {
         /*
          * To convert the InputStream to String we use the BufferedReader.readLine()
          * method. We iterate until the BufferedReader return null which means
          * there's no more data to read. Each line will appended to a StringBuilder
          * and returned as String.
          */
         BufferedReader reader = new BufferedReader(new InputStreamReader(is), 1024);
         StringBuilder sb = new StringBuilder();
  
         String line = null;
         try {
             while ((line = reader.readLine()) != null) {
                 sb.append(line + "\n");
             }
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 is.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return sb.toString();
     }
     
     public class OsloCityBikeException extends Exception {
 		private static final long serialVersionUID = -1658595799469140717L;
 
 		public OsloCityBikeException(Exception e) {
     		super(e);
     	}
     }
     
     public class OsloCityBikeCommunicationException extends OsloCityBikeException{
 		private static final long serialVersionUID = -4574284801307284546L;
 
 		public OsloCityBikeCommunicationException(Exception e) {
     		super(e);
     	}
     }
 
     private class OsloCityBikeParseException extends OsloCityBikeException {
 		private static final long serialVersionUID = -5677634395427608346L;
 
 		public OsloCityBikeParseException(Exception e) {
     		super(e);
     	}
     }
 
 }
