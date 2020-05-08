 package com.zarcode.data.maint;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.w3c.dom.CharacterData;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import ch.hsr.geohash.WGS84Point;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.google.appengine.api.datastore.DatastoreFailureException;
 import com.zarcode.app.GeoRssUtil;
 import com.zarcode.common.EmailHelper;
 import com.zarcode.common.PlatformCommon;
 import com.zarcode.data.dao.WaterResourceDao;
 import com.zarcode.data.exception.WebCrawlException;
 import com.zarcode.data.model.WaterResourceDO;
 
 /**
  * This is a web GeoRSS Webcrawler for accessing Google Maps feeds.
  * 
  * @author Administrator
  */
 public class GeoRSSWrite extends HttpServlet {
 
 	private Logger logger = Logger.getLogger(GeoRSSWrite.class.getName());
 	
 	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
 	
 	private StringBuilder report = null;
 	
 	private String rssTitle = null;
 	
 	private String rssLink = null;
 	
 	private String rssDesc = null;
 	
     public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     	int i = 0;
     	int j = 0;
     	int k = 0;
     	String msg = null;
     	String urlStr = null;
     	String temp = null;
 		int itemCount = 0;
  		int memberCount = 0;
  		Node itemNode = null;
  		WaterResourceDO res = null;
  		HashMap props = null;
  		List<WGS84Point> polygon = null;
 		OutputStream os = null;
  		Document doc = null;
  		int MAX_TIME_THREHOLD = 50;
  		String[] targetList = null;
  		
  		report = new StringBuilder();
  		
  		WaterResourceDao dao = new WaterResourceDao();
  		Date startTimestamp = new Date();
  		
  		Node n = null;
  		Date now = null;
  		int startingIndex = 0;
  		String blobKeyParam = req.getParameter("blobKey");
  		
  		BlobKey blobKey = new BlobKey(blobKeyParam);
  		byte[] rawDoc = blobstoreService.fetchData(blobKey, 0, blobstoreService.MAX_BLOB_FETCH_SIZE-1);
  		try {
  			doc = PlatformCommon.bytesToXml(rawDoc);
  		}
  		catch (Exception e) {
  			logger.severe("EXCEPTION :: " + e.getMessage());
  		}
  		
 		
 		///////////////////////////////////////////////////////////////////////////////
 		//
 		// Process actual data in the Geo RSS feed
 		//
 		///////////////////////////////////////////////////////////////////////////////
  		
  		logger.info("Start process GeoRSS header ...");
  		processGeoRSSHeader(doc);
  		logger.info("GeoRSS header Done.");
  		int resAdded = 0;
 		
 		dao.deleteByRegion(rssTitle);
 		
 		try {
 			if (doc != null) {
 	 			NodeList lakeItemList = doc.getElementsByTagName("item");
 	 			if (lakeItemList != null && lakeItemList.getLength() > 0) {
 	 				itemCount = lakeItemList.getLength();
 	 				for (i=startingIndex; i<itemCount; i++) {
 	 				
 	 					/**
 	 					 * check if we are about to hit the Google Timeout Threshold
 	 					 */
 	 					now = new Date();
 		        		long durationInSecs = (now.getTime() - startTimestamp.getTime())/1000;
 		        		if (durationInSecs > MAX_TIME_THREHOLD) {
 		        			logger.warning("Hitting ending of processing time -- Queuing task to handle late!");
 		        			/*
 		            		Queue queue = QueueFactory.getDefaultQueue();
 		            		String nextIndex = "" + i;
 		            		queue.add(TaskOptions.Builder.withUrl("/georssload").param("url", urlParam).param("start", nextIndex).param("delete", "false"));
 		            		*/
 		            		return;
 		        		}
 		        		else {
 		        			logger.info(i + ") Time is still good ---> " + durationInSecs);
 		        		}
 		        		
 	 					itemNode = lakeItemList.item(i);
 	 					
 	 					///////////////////////////////////////////////////////////////////////
 	 					//
 	 					// Process each lake item data item
 	 					//
 	 					///////////////////////////////////////////////////////////////////////
 	 					
 	 					if (itemNode != null) {
 	 						
 	 						// create water resource
 	 						res =  new WaterResourceDO();
 	 						res.setLastUpdate(new Date());
 	 						res.setRegion(rssTitle);
 	 						
 	 						NodeList itemMembers = itemNode.getChildNodes();
 	 						memberCount = itemMembers.getLength();
 	 						for (j=0; j<memberCount; j++) {
 	 							n = itemMembers.item(j);
 	 							if (n != null) {
 	 								if ("guid".equalsIgnoreCase(n.getNodeName())) {
 	 	    	        				temp = n.getFirstChild().getNodeValue();
 	 	    	        				logger.info("Found guid=" + temp);
 	 	    	        				res.setGuid(temp);
 	 	    	        			}
 	 	    	        			else if ("title".equalsIgnoreCase(n.getNodeName())) {
 	 	    	        				temp = n.getFirstChild().getNodeValue();
 	 	    	        				logger.info("Found title=" + temp);
 	 	    	        				res.setName(temp);
 	 	    	        				res.setContent(temp);
 	 	    	        			}
 	 	    	        			else if ("author".equalsIgnoreCase(n.getNodeName())) {
 	 	    	        				temp = n.getFirstChild().getNodeValue();
 	 	    	        				logger.info("Found author=" + temp);
 	 	    	        			}
 	 	    	        			else if ("description".equalsIgnoreCase(n.getNodeName())) {
 	 	           						String descData = getCharacterDataFromElement((Element)n);
 	 	    	        				props = convertKVString2HashMap(descData);
 	 	    	        				if (props != null && props.containsKey("reportKey")) {
 	 	    	        					res.setReportKey((String)props.get("reportKey"));
 	 	    	        				}
 	 	    	        				else {
 	 	    	        					logger.warning("reportKey not found for resource=" + res.getName());
 	 	    	        				}
 	 	    	        			}
 	 	    	        			else if ("gml:Polygon".equalsIgnoreCase(n.getNodeName())) {
 	 	    	        				List<String> textList = new ArrayList<String>();
 	 	    	        				GeoRssUtil.findMatchingNodes(n, Node.TEXT_NODE, textList);
 	    	        					if (textList != null && textList.size() > 0) {
 	    	        						String polygonStr = textList.get(0);
 	    	        						polygonStr = polygonStr.trim();
 	    	        						polygon = convertGMLPosList2Polygon(polygonStr);
 	    	        						logger.info("Converted incoming polygonStr into " + polygon.size() + " object(s).");
 	    	        						res.setPolygon(polygon);
 	    	        					}
 	 	    	        			}
 	 							}
 	 						}
 	 						//
 	 						// add water resource to model
 	 						//
 	 						logger.info("Inserting resource into model!");
 	 						dao.insertResource(res);
 	 						report.append("Adding resource :: ");
 	 						report.append(res.getName());
 	 						report.append(" [ Region: ");
 	 						report.append(res.getRegion());
 	 						report.append("]\n");
 	 						resAdded++;
 	 					}
 	 				}
 	 				report.append("\n\nTOTAL ADDED: " + resAdded);
 	 				logger.info("Processing is done on index=" + i);
 	 				EmailHelper.sendAppAlert("Docked" + ": GeoRSSFeed Status", report.toString() , "Docked");
 	 			}
 			}
 			else {
 				logger.warning("Document is NULL -- Write FAILED");
 			}
         } 
 		catch (DatastoreFailureException e) {
  			logger.severe("EXCEPTION :: " + e.getMessage());
         }
     }
     
     /**
      * Process the header of the Geo RSS from Google Maps.
      * 
      * @param doc
      */
     private void processGeoRSSHeader(Document doc) {
     	int j = 0;
     	Node n = null;
 		String nodeName = null;
 		Date now = null;
 		NodeList channelList = doc.getElementsByTagName("channel");
 			
 		if (channelList != null && channelList.getLength() > 0) {
 			logger.info("Found channel node --> " + channelList.getLength());
 			Node header = channelList.item(0);
 			NodeList headerChildren = header.getChildNodes();
 			logger.info("# of children in <channel> node: " + headerChildren.getLength());
 			
 			for (j=0; j<headerChildren.getLength(); j++) {
        		
 	       		n = headerChildren.item(j);
 	       		logger.info("Found <channel> children node: " + n.getNodeName());
 	       		nodeName = n.getNodeName();
 	       		if (nodeName != null) {
 	       			nodeName = nodeName.trim();
 	       		}
 	       		if ("link".equalsIgnoreCase(nodeName)) {
 	       			rssLink = n.getFirstChild().getNodeValue();
 	       			logger.info("Saving header value for link: " + rssLink);
 	       			report.append("RSSLINK: " + rssLink + "\n");
 	       		}
 	       		else if ("title".equalsIgnoreCase(nodeName)) {
 	       			rssTitle = n.getFirstChild().getNodeValue();
 	       			logger.info("Saving header value for title: " + rssTitle);
 	       			report.append("RSSTITLE: " + rssTitle + "\n");
 	       		}
 	       		else if ("description".equalsIgnoreCase(nodeName)) {
 					rssDesc = getCharacterDataFromElement((Element)n);
 					logger.info("Saving header value for description: " + rssDesc);
 	       			report.append("DESC: " + rssDesc + "\n");
 	       			break;
 	       		}
 			}
 		}
 	}
 			
 	
 	public String getCharacterDataFromElement(Element elem) {
 		Node child = elem.getFirstChild();
 	    if (child instanceof CharacterData) {
 	    	CharacterData cd = (CharacterData) child;
 	    	String str = cd.getData();
 	    	// take of html
 	    	str = str.substring(15);
 	    	str = str.substring(0, str.length()-6);
 	    	str = str.trim();
 	    	logger.info("Data --> " + str);
 	    	return str;
 	    }
 	   	logger.warning("First child is not instanceof 'CharacterData'");
 	    return "";
 	}
 	
 	private List<WGS84Point> convertGMLPosList2Polygon(String dataStr) {
 		int i = 0;
 		List<WGS84Point> res = null;
 		String latLngStr = null;
 		double lat = 0;
 		double lng = 0;
 		WGS84Point pt = null;
 		int numOfPoints = 0;
 		
 		if (dataStr != null) {
 			res = new ArrayList<WGS84Point>();
 			dataStr = dataStr.trim();
 			String[] pointList = dataStr.split("\n"); 
 			if (pointList != null && pointList.length > 0) {
 				//
 				// GeoRSS is returning starting pt as the last pt as well.
 				//
 				numOfPoints = pointList.length - 1;
 				for (i=0; i<numOfPoints; i++) {
 					latLngStr = pointList[i];
 					latLngStr = latLngStr.trim();
 					String[] latLngList = latLngStr.split(" ");
 					// should have individual points here
 					if (latLngList != null && latLngList.length == 2) {
 						lat = Double.parseDouble(latLngList[0]);
 						lng = Double.parseDouble(latLngList[1]);
 						pt = new WGS84Point(lat, lng);
 						res.add(pt);
 					}
 					else {
 						logger.warning("LatLng format is not as expected --> " + latLngStr);
 					}
 				}
 			}
 			else {
 				logger.warning("LatLng List format is not as expected --> " + dataStr);
 				
 			}
 		}
 		
 		return res;
 	}
 	
 	private HashMap<String, String> convertKVString2HashMap(String dataStr) {
 		int i = 0;
 		String nvPair = null;
 		HashMap<String, String> map = null;
 
 		if (dataStr != null) {
 			map = new HashMap<String, String>();
 			String[] nameValueList = dataStr.split("\n"); 
 			if (nameValueList != null && nameValueList.length > 0) {
 				for (i=0; i<nameValueList.length; i++) {
 					nvPair = nameValueList[i];
 					String[] keyValue = nvPair.split("=");
 					if (keyValue != null && keyValue.length == 2) {
 						logger.info("Adding key=" + keyValue[0] + " val=" + keyValue[1]);
 						map.put(keyValue[0], keyValue[1]);
 					}
 				}
 			}
 		}
 		return map;
 	}
 }
