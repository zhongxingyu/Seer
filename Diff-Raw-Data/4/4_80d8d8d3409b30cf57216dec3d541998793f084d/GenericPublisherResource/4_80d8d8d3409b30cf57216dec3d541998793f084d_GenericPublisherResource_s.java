 package local.rest.resources;
 
 import local.analytics.*;
 import is4.*;
 import local.db.*;
 import local.rest.*;
 import local.rest.resources.util.*;
 import local.metadata.context.*;
 import com.mongodb.*;
 
 import net.sf.json.*;
 
 import java.util.*;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 import java.lang.StringBuffer;
 
 import javax.naming.InvalidNameException;
 import java.io.*;
 import java.net.*;
 
 import org.simpleframework.http.core.Container;
 import org.simpleframework.http.Response;
 import org.simpleframework.http.Request;
 import org.simpleframework.http.Query;
 
 /**
  *  Resource object for a device.
  */
 public class GenericPublisherResource extends Resource{
 	protected static transient Logger logger = Logger.getLogger(GenericPublisherResource.class.getPackage().getName());
 	protected static MySqlDriver database = (MySqlDriver)DBAbstractionLayer.database;
 	public UUID publisherId =null;
 	protected static final int headCount = 5;
 
 	//public static int TYPE = ResourceUtils.GENERIC_PUBLISHER_RSRC;
 	protected long last_data_ts = 0;
 
     private ObjectInputStream routerIn = null;
     private ObjectOutputStream routerOut = null;
 
     //last received data value
     private JSONObject lastValuesReceived = new JSONObject();
 
 	public GenericPublisherResource(String uri, UUID pubId) throws Exception, InvalidNameException{
 		super(uri);
 		if (pubId != null)
 			publisherId = pubId;
 		else
 			throw new Exception("Null pointer to pubId");
 
 		//set type to generic_publisher
 		TYPE=ResourceUtils.GENERIC_PUBLISHER_RSRC;
 		database.setRRType(URI, ResourceUtils.translateType(TYPE).toLowerCase());
 	}
 
     public UUID getPubId(){
         return publisherId;
     }
 
 	public void get(Request m_request, Response m_response, String path, boolean internalCall, JSONObject internalResp){
 
         Query query = m_request.getQuery();
 		if(query.get("query") != null &&
 			((String) query.get("query")).equalsIgnoreCase("true")){
 			query_(m_request, m_response, null, internalCall, internalResp);
 			return;
 		}
 
         if(query.containsKey("incident_paths")){
             super.get(m_request, m_response, path, internalCall, internalResp);
             return;
         }
 		
 		logger.info("GET " + this.URI);
 		JSONObject response = new JSONObject();
 		try {
 			JSONObject properties = database.rrGetProperties(this.URI);
 			if(properties == null){
 				properties = new JSONObject();
 			}
 			response.put("status", "success");
 
 			UUID assocPubid = database.isRRPublisher2(this.URI);
 			if(assocPubid != null){
				/*response.put("pubid", assocPubid.toString());
				logger.info("POPULATING");
 				JSONObject queryJSON = new JSONObject();
 				queryJSON.put("pubid", publisherId.toString());
 				queryJSON.put("ts", new Long(last_data_ts));
 				JSONObject sortByJSON = new JSONObject();
 				sortByJSON.put("timestamp",1);
 				JSONObject lastValuesReceived = mongoDriver.queryWithLimit(
 									queryJSON.toString(), 
 									sortByJSON.toString(), 
 									headCount);
                 JSONArray res = lastValuesReceived.optJSONArray("results");
                 if(res!=null && res.size()>0)
                     lastValuesReceived = (JSONObject) res.get(0);
                 else
                     lastValuesReceived = new JSONObject();*/
 				response.put("head", lastValuesReceived.toString());
 			}
 			response.put("properties", properties);
 			sendResponse(m_request, m_response, 200, response.toString(), internalCall, internalResp);
 			return;
 		} catch (Exception e){
 			logger.log(Level.WARNING, "", e);
 		}
 		sendResponse(m_request, m_response, 200, null, internalCall, internalResp);
 	}
 
 	public void put(Request m_request, Response m_response, String path, String data, boolean internalCall, JSONObject internalResp){
 		post(m_request, m_response, path, data, internalCall, internalResp);
 	}
 
 	public void post(Request m_request, Response m_response, String path, String data, boolean internalCall, JSONObject internalResp){
 		logger.info("Publisher handling PUT/POST data request");
         Query query = m_request.getQuery();
 		if(query.get("query") != null &&
 		    ((String) query.get("query")).equalsIgnoreCase("true")){
 			query_(m_request, m_response, data, internalCall, internalResp);
 		} else {
 			JSONObject resp = new JSONObject();
 			JSONArray errors = new JSONArray();
 			try{
 				JSONObject dataObject = (JSONObject) JSONSerializer.toJSON(data);
 				logger.info("data: " + dataObject.toString());
 				String operation = dataObject.optString("operation");
 				if(operation!= null && !operation.equals("")){
 					if(operation.equalsIgnoreCase("create_symlink")){
 						super.put(m_request, m_response, path, data, internalCall, internalResp);
 					} else {
 						super.handlePropsReq(m_request, m_response, data, internalCall, internalResp);
 					}
 				} else {
                     UUID pubid = null;
 					String type = (String) query.get("type");
 					String addts = (String) query.get("addts");
                     
                     try {
 					    pubid = UUID.fromString((String) query.get("pubid"));
                     } catch(Exception b){
                         logger.warning("\"pubid\" was not set");
                         sendResponse(m_request, m_response, 500, null, internalCall, internalResp);
                     }
 
                     if(pubid!=null)
 				        logger.info("type: " + type +"; pubid: " + pubid.toString());
 					
 					if(type != null && pubid != null &&  !type.equals("") && !pubid.equals("") &&
 							type.equalsIgnoreCase("generic") && pubid.compareTo(publisherId)==0){
 
 						//store and send success
 						if(addts != null && !addts.equals("") && addts.equalsIgnoreCase("false"))
 							handleIncomingData(dataObject, false);
 						else
 							handleIncomingData(dataObject, true);
 						resp.put("status", "success");
 						sendResponse(m_request, m_response, 200, resp.toString(), internalCall, internalResp);
 					} else {
 						resp.put("status", "fail");
 						if(type == null || type.equalsIgnoreCase(""))
 							errors.add("type parameter missing");
 						if(pubid == null || pubid.equals(""))
 							errors.add("pubid parameter missing");
 						if(type != null)
 							errors.add("Unknown type");
 						if(pubid !=null && pubid.compareTo(publisherId) != 0)
 							errors.add("pubid does not match that of this generic publisher");
 						resp.put("errors", errors);
 						sendResponse(m_request, m_response, 200, resp.toString(), internalCall, internalResp);
 					}
                 }
             }catch(Exception e){
                 logger.log(Level.WARNING, "", e);
 				if(e instanceof JSONException){
 					errors.add("Invalid JSON");
 				}
 				resp.put("status", "fail");
 				resp.put("errors", errors);
 				sendResponse(m_request, m_response, 200, resp.toString(), internalCall, internalResp);
 			}
 		}
 	}
 
 	public void delete(Request m_request, Response m_response, String path, boolean internalCall, JSONObject internalResp){
 
 		logger.info("Handling DELETE PUBLISHER command for " + this.URI);
 
 		//reset properties
 		JSONObject emptyProps = new JSONObject();
 		super.updateProperties(emptyProps);
 
 		//remove association with device
 		database.removeDeviceEntry(this.URI);
 
 		//delete entry from publishers table
 		database.removePublisher(this.publisherId);
 
 		//delete rest_resource entry
 		database.removeRestResource(this.URI);
 		RESTServer.removeResource(this);
 		
 		//remove subscriptions to this publisher
 		SubMngr submngr = SubMngr.getSubMngrInstance();
 		submngr.pubRemoved(m_request, m_response, true, internalResp, publisherId.toString());
 
 		//remove from internal graph
 		this.metadataGraph.removeNode(this.URI);
 
 		sendResponse(m_request, m_response, 200, null, internalCall, internalResp);
 		
 	}
 
 	protected void handleIncomingData(JSONObject data, boolean addTimestamp){
 
 		long timestamp;
 		if(addTimestamp || !data.containsKey("ts")){
 			//add timestamp
 			Date date = new Date();
 			timestamp = date.getTime()/1000;
 			data.put("ts", timestamp);
 			logger.info("adding ts: " + timestamp);
 		} else {
 			try {
 				timestamp = data.getLong("ts");
 			} catch(Exception e){
 				logger.log(Level.WARNING, "", e);
 				timestamp = 0L;
 			}
 		}
 		data.put("pubid", publisherId.toString());
 
 		//Forward to subscribers
 		String dataStr = data.toString();
 		dataStr = dataStr.replace("$","d_");
 		JSONObject dataCopy = (JSONObject)JSONSerializer.toJSON(dataStr);
 	
 		dataCopy.put("timestamp", timestamp);
 		dataCopy.put("PubId", publisherId.toString());
 		dataCopy.put("is4_uri", this.URI.toString());
 		SubMngr submngr = SubMngr.getSubMngrInstance();
 		logger.info("SubMngr Copy: " + dataCopy.toString());
 		submngr.dataReceived(dataCopy);
 
         logger.info("Called submngr.dataReceived() with the data copy");
 		//get the alias associated with this publisher
 		String alias = null;
 		if(URI.endsWith(publisherId.toString() + "/") ||
 				URI.endsWith(publisherId.toString())){
 			alias = publisherId.toString();
 		} else {
 			String thisuri = URI;
 			if(thisuri.endsWith("/"))
 				thisuri = thisuri.substring(0, thisuri.length()-1);
 			alias = thisuri.substring(thisuri.lastIndexOf("/"), thisuri.length());
 		}
 
         //forward up the olap graph
         JSONObject properties = database.rrGetProperties(this.URI);
         String unitsStr = properties.optString("units");
         if(dataCopy.containsKey("timestamp")){
             Long ts = dataCopy.getLong("timestamp");
             dataCopy.remove("timestamp");
             dataCopy.put("ts", ts);
         }
         
         if(dataCopy.containsKey("value")){
             Double v = dataCopy.getDouble("value");
             dataCopy.remove("value");
             dataCopy.put("v", v);
         } else if(dataCopy.containsKey("val")){
             Double v = dataCopy.getDouble("val");
             dataCopy.remove("val");
             dataCopy.put("v", v);
         }
 
         if(RESTServer.tellRouter && !unitsStr.equals(""))
            metadataGraph.streamPush(URI, unitsStr, dataCopy.toString()); 
 
 		logger.info("Publsher PUTTING in data repository");
 
 		//put the data entry in the database
 		//database.putInDataRepository(data, publisherId, alias);
 		database.updateLastRecvTs(URI, timestamp);
 
 		//store in the mongodb repos
 		//MongoDBDriver mongod = new MongoDBDriver();
 		//mongod.putEntry(dataCopy);
 		mongoDriver.putTsEntry(data);
         lastValuesReceived = data;
 		last_data_ts = timestamp;
 	}
 
     public void setRouterCommInfo(String routerHost, int routerPort){
         try {
             Socket s = new Socket(InetAddress.getByName(routerHost), routerPort);
             routerOut = new ObjectOutputStream(s.getOutputStream());
             routerOut.flush();
             routerIn = new ObjectInputStream(s.getInputStream());
         } catch(Exception e) {
             logger.log(Level.SEVERE, "", e);
             System.exit(1);
         }
     }
 
 	public JSONObject queryTimeseriesRepos(JSONObject queryJson){
 		JSONObject queryResults = new JSONObject();
 		try{
 			//only run the query for this publisher
 			queryJson.put("PubId", publisherId.toString());
 			
 			//remove the PubId key from the results
 			JSONObject keys = new JSONObject();
 			keys.put("PubId",0);
 
 			logger.info("QUERY: " + queryJson.toString() + "\nKEYS:  " + keys.toString());
 
 			JSONObject queryR = mongoDriver.query(queryJson.toString(), keys.toString());
 			if(queryR != null)
 				queryResults.putAll(queryR);
 		} catch(Exception e){
 			logger.log(Level.WARNING, "", e);
 		}
 		return queryResults;
 	}
 
 	public JSONArray queryTimeseriesRepos2(JSONObject queryJson){
 		JSONArray queryResults = new JSONArray();
 		try{
 
 			//only run the query for this publisher
 			queryJson.put("pubid", publisherId.toString());
 
 			//remove the PubId key from the results
 			JSONObject keys = new JSONObject();
 			keys.put("pubid",0);
 
 			logger.info("QUERY: " + queryJson.toString() + "\nKEYS:  " + keys.toString());
 
 			return mongoDriver.queryTsColl(queryJson.toString(), keys.toString());
 		} catch(Exception e){
 			logger.log(Level.WARNING, "", e);
 		}
 		return queryResults;
 	}
 
 	public void query_(Request m_request, Response m_response, String data, boolean internalCall, JSONObject internalResp){
 		JSONObject resp = new JSONObject();
 		JSONArray errors = new JSONArray();
 		resp.put("path", URI);
         Query query = m_request.getQuery();
 		try{
 			//JSONObject tsQueryObj = new JSONObject();
 			JSONObject tsQueryObj2 = new JSONObject();
 		
 			//get query object from input data
 			if(data != null && !data.equals("")){	
 				JSONObject dataJsonObj = (JSONObject) JSONSerializer.toJSON(data);
 				JSONObject dataTsQuery = dataJsonObj.optJSONObject("ts_query");
 				//tsQueryObj.putAll(dataTsQuery);
 				tsQueryObj2.putAll(dataTsQuery);
 			}
 
             logger.fine("query::" + query.toString());
 			Iterator keys = query.keySet().iterator();
 			Vector<String> attributes = new Vector<String>();
 			Vector<String> values = new Vector<String>();
 			while(keys.hasNext()){
 				String thisKey = (String) keys.next();
 				logger.fine("Keys found!; thisKey=" + thisKey);
 				if(thisKey.startsWith("ts_")){
 					String str = "ts_";
 					String queryKey = thisKey.substring(thisKey.indexOf(str)+str.length(), thisKey.length());
 					String queryValue = (String)query.get(thisKey);
 
 					logger.info("Query Value: " + queryValue);
 
 					JSONObject conditions = Resource.genJSONClause(queryValue);
 					logger.info("Conditions: " + conditions);
 					if(conditions!=null){
 						//tsQueryObj.put(queryKey, conditions);
 						if(queryKey.equalsIgnoreCase("timestamp"))
 							tsQueryObj2.put("ts", conditions);
 					} else{
 						if(isNumber(queryValue)){
 							long val = Long.parseLong(queryValue);
 							//tsQueryObj.put(queryKey, val);
 							if(queryKey.equalsIgnoreCase("timestamp"))
 								tsQueryObj2.put("ts", val);
 						} else {
 							//tsQueryObj.put(queryKey, queryValue);
 							if(queryKey.equalsIgnoreCase("timestamp"))
 								tsQueryObj2.put("ts", queryValue);
 						}
 					}
 
 				} else if(thisKey.startsWith("ts")){
 					String queryValue = (String)query.get(thisKey);
 
 					JSONObject conditions = Resource.genJSONClause(queryValue);
 					if(conditions!=null){
 						//tsQueryObj.putAll(conditions);
 						tsQueryObj2.putAll(conditions);
 					} else{
 						if(isNumber(queryValue)){
 							long val = Long.parseLong(queryValue);
 							//tsQueryObj.put(thisKey, val);
 							if(thisKey.equalsIgnoreCase("timestamp"))
 								tsQueryObj2.put("ts", queryValue);
 							else
 								tsQueryObj2.put(thisKey, val);
 						} else {
 							logger.warning("Invalid conditions set for generic props query");
 						}
 					}
 						
 				}
 			}
 
 			//logger.fine("Timeseries Query: " + tsQueryObj.toString());
 			logger.fine("Timeseries Query2: " + tsQueryObj2.toString());
 
 			if(!tsQueryObj2.toString().equals("{}")){
 				//tsQueryObj.put("is4_uri", URI);
 				/*if(last_props_ts>0)
 					tsQueryObj.put("timestamp", last_props_ts);*/
 
 				//JSONObject mqResp = queryTimeseriesRepos(tsQueryObj);
 				JSONArray mqResp2 = queryTimeseriesRepos2(tsQueryObj2);
 				//logger.fine("mqResp: " + mqResp.toString());
 				logger.fine("mqResp2: " + mqResp2.toString());
 				resp.put("ts_query_results", mqResp2);
 			} else {
 				errors.add("TS Query Error: Empty or invalid query");
 				logger.warning(errors.toString());
 				resp.put("errors", errors);
 			}
 		} catch (Exception e){
 			logger.log(Level.WARNING, "", e);
 			if(e instanceof JSONException){
 				errors.add("Invalid JSON for POST data; url params ignored");
 				resp.put(errors, errors);
 				sendResponse(m_request, m_response, 200, resp.toString(), internalCall, internalResp);
 				return;
 			}
 		}
 		JSONObject propsQueryResultsBuffer = new JSONObject();
 		super.query_(m_request, m_response, data, true, propsQueryResultsBuffer);
 		resp.put("props_query_results", propsQueryResultsBuffer);
 		sendResponse(m_request, m_response, 200, resp.toString(), internalCall, internalResp);
 	}
 
 }
