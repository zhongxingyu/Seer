 package edu.stanford.cs.gaming.sdk.service;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.net.URLEncoder;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.stanford.cs.gaming.sdk.model.AppRequest;
 import edu.stanford.cs.gaming.sdk.model.AppResponse;
 import edu.stanford.cs.gaming.sdk.model.Criterion;
 import edu.stanford.cs.gaming.sdk.model.Obj;
 
 import android.util.Log;
 
 
 public class Util {
   public static final String TAG = "Util";
   public static JSONObject toJson(Field field) {
      return null;
   }
 
   private static final HashSet<Class<?>> WRAPPER_TYPES = getWrapperTypes();
 
   public static boolean isWrapperType(Class<?> clazz)
   {
       return WRAPPER_TYPES.contains(clazz);
   }
 
   private static HashSet<Class<?>> getWrapperTypes()
   {
       HashSet<Class<?>> ret = new HashSet<Class<?>>();
       ret.add(Boolean.class);
       ret.add(Character.class);
       ret.add(Byte.class);
       ret.add(Short.class);
       ret.add(Integer.class);
       ret.add(Long.class);
       ret.add(Float.class);
       ret.add(Double.class);
       ret.add(Void.class);
       ret.add(String.class);
       return ret;
   }  
 
   /*
   public static JSONArray toJsonArray(Collection obj) {
 	  JSONArray array = new JSONArray();
 	  return null;
   }
   */
 /*
   public static JSONObject toJson(Object obj) {
 	  return (JSONObject) toJson(obj.getClass().getName(), obj);
   }
   */
   public static Object toJson(Object obj) {
 	  if (obj != null)
 //	  Log.d(TAG, obj.getClass().getName());
 
 
 	  try {
 	  if (obj == null)
 		  return null;
 	  if (obj.getClass().isPrimitive() || isWrapperType(obj.getClass())) {
         return obj;
 			
 	  }
 //	  Log.d(TAG, "isArray: " + obj.getClass().isArray());
 	  if (obj.getClass().isArray()) {
 		  int arrLen = Array.getLength(obj);
 		  JSONArray jsonArray = new JSONArray();
 		  for (int i=0; i < arrLen; i++) {
 			  Object obj1 = Array.get(obj, i);
 		      jsonArray.put(toJson(obj1));
 		  }
 //		  Log.d(TAG, "ARRAY PUTTING: " + name + ":" + jsonArray);
 //		  jsonObj.put(name, jsonArray);
 		  return jsonArray;
 	  }
 	
 	  // It is a complex object
 //	  JSONArray jsonArray = new JSONArray();		  
 	      JSONObject jsonObj = new JSONObject();
 	      JSONObject jsonObj1 = new JSONObject();
   		  Field[] fields = obj.getClass().getFields();
 		  for (Field field: fields) {
 			  Object obj1 = field.get(obj);
 			  if (obj1 != null) {
 //			  Log.d(TAG, "HERE: " + field.getName() + field.get(obj));
 //			  Log.d(TAG, "Primitive check for: " + obj1.getClass() + ":" + obj1.getClass().isPrimitive() + ":" + isWrapperType(obj1.getClass()));
 					jsonObj1.put(field.getName(), toJson(obj1));	
 //	            jsonObj1.put(field.getName(), toJson(obj1);	
  //            jsonArray.put(toJson(field.getName(), field.get(obj)));
 				  
 //					 Log.d(TAG, "NOT EQUALS");
 //				     Log.d(TAG, field.getName() + ":" + field.getType().toString() + ":" + field.get(obj));
 			  }
 		  }	  
  
 		jsonObj.put(obj.getClass().getName(), jsonObj1);
 		return jsonObj;
 	  } catch (Exception e) {
 		  e.printStackTrace();
 	  }
 	return null;
 
 
   }
   
     public static Object fromJson(Object obj, String name, String field) {
     	Object objTmp = null;
 //    	Log.d(TAG, "HERE 1: " + ":" + obj.getClass() + obj );
     	if (obj == null)
     		return null;
 //    	Log.d(TAG, "Object not null");
     	
     	if (obj.getClass().isPrimitive() || isWrapperType(obj.getClass())) {
     		return obj;
     	}
 //    	Log.d(TAG, "Object not primitive");
 
     	if (obj instanceof JSONArray) {
     		JSONArray jsonArray = (JSONArray) obj; 
     		if (jsonArray.length() ==0)
     			return null;
     		try {
 
     		Object objToCheckType = fromJson(jsonArray.get(0), null, null);
     		Object objArr = Array.newInstance(objToCheckType.getClass(), jsonArray.length());
     		
     		for (int i=0; i < jsonArray.length(); i++) {
     			Array.set(objArr, i, fromJson(jsonArray.get(i), null, null));
     		}    		
 //    		JSONArray arr = (JSONArray) obj;
 //    		Array.newInstance(field.g, size)
             return objArr;
     		} catch (Exception e) {
     			e.printStackTrace();
     			return null;
     		}
     		
     	}
 //    	Log.d(TAG, "Object not a JSON Array");
 
     	//Complex object
     	if (obj instanceof JSONObject) {
 //        	Log.d(TAG, "Object is JSON");
     		
     		JSONObject jsonObj = (JSONObject) obj;
     		Object obj1 = null;
     		try {
     			Iterator<String> keys = (Iterator<String>) jsonObj.keys();
     			while (keys.hasNext()) {
     				String className = keys.next();
 //    		    	Log.d(TAG, "Classname is: " + className);
     				
     			    obj1 = Class.forName(className).newInstance(); 
 //    			    Log.d(TAG, "Class.forName(className): " + Class.forName(className).getName());
 //    			    Log.d(TAG, "obj1 instance of GameObject: " + (obj1 instanceof Obj));
 //    			    Log.d(TAG, "Object class is: " + ((Object)(new Obj())).getClass());
         	    		Field[] fields = obj1.getClass().getFields();
 //    			        Field[] fields = GameObject.class.getFields();
 //      	    		Log.d(TAG, "Fields length is: " + fields.length);
         	    		Hashtable<String, Field> fieldsHash = new Hashtable<String, Field>();
         	    		for (Field field1 : fields) {
 //            	        	Log.d(TAG, "field inserted is: " + field1.getName());
         	    			
 		                   fieldsHash.put(field1.getName(), field1);
     			    }
 //       	    	Log.d(TAG, "ASLAI HERE: " + jsonObj.get(className));
         	    	JSONObject jsonObj2 = (JSONObject) jsonObj.get(className);
         	    	Iterator<String> keys2 = (Iterator<String>) jsonObj2.keys(); 
         	    	while (keys2.hasNext()) {
         	    		Field field3 = null;
         	    		String key3 = keys2.next();
 //      	        	Log.d(TAG, "variable name is: " + key3);
         	    		
         	    		field3 = fieldsHash.get(key3);
         	    		if (field3 != null) {
 //           	        	Log.d(TAG, "field match found: " + key3 + "||" + field3.getType().getName());
 //           	        	Log.d(TAG, "instanceOf Array: " + (field3.getType().isArray()));
                             if (field3.getType().isArray()) {
                             	JSONArray jsonArr = (JSONArray) jsonObj2.get(key3);
                             	if (jsonArr != null) {
 
 //                              	  Object arr = Array.newInstance(Class.forName("java.lang.String"), jsonArr.length());                            		
                             	  Object arr = Array.newInstance(field3.getType().getComponentType(), jsonArr.length());
 //                           	  Log.d(TAG, "arr is of type: " + arr.getClass());
                             	  for (int i = 0; i < jsonArr.length(); i++) {
  //                           		  Object objTmp = fromJson(jsonArr.get(i), null, null);
 //                          		  Log.d(TAG, "Object retured of class: " + objTmp.getClass());
     //                          		  Array.set(arr, i, "333");          
 
                             		  Array.set(arr, i, fromJson(jsonArr.get(i), null, null));
                             	  }
                             	  field3.set(obj1, arr);
                             	}
                             } else {                            	
                             	if ((objTmp = fromJson(jsonObj2.get(key3), null, null)) != null && field3 != null) {
 //                               	Log.d(TAG, "FIELD3 is " + field3.getName());
 //                               	Log.d(TAG, "VALUE is " + objTmp);       
 //                            		Log.d(TAG, "FIELD3 CLASS is " + field3.getType());
 //                            		Log.d(TAG, "objTmp CLASS is " + objTmp.getClass().getName());
 
 //                            		if ("float".equals(field3.getType())) {
 //                            			field3.set(obj1, new Float(objTmp))
 //                            		} else
 							        field3.set(obj1, objTmp);
                             	}
                             }
 
         	    	} else {
       	        	Log.d(TAG, "field NO MATCH: " + key3);
         	    		
         	    	}
         	    	
     			}
     			}
     			return obj1;
     		} catch (Exception e) {
     			// TODO Auto-generated catch block			
     			e.printStackTrace();
     			return obj1;
     		}
 
         	
         }
     	
     	return null;
     }
 
       public static JSONObject criteriaToJsonStr(Criterion[] criteria) {
     	  JSONObject json = new JSONObject();
     	  try {
     	  for (int i = 0; i < criteria.length; i++) {
     		  json.put(criteria[i].parameter, criteria[i].value);
     	  }
     	  } catch (Exception e) {
     		  e.printStackTrace();
     	  }
     	  return json;
       }
 
       //ASLAI: Add error handling if issues with network, possibly tell user to enable internet access
       public static String constructParams(AppRequest request) {
        	  String str = request.path + "?format=json&request_id=" + request.id + "&app_id=" + request.app_id +
        	  "&app_api_key=" + URLEncoder.encode(request.app_api_key);
        	  if (request.user_id != 0) {
            	 str += "&user_id=" + request.user_id;
           	 str += "&fb_id=" + request.fb_id;
           	 str += "&fb_token=" + URLEncoder.encode(request.token);
        	  }
        	  /*
        	  if (request.object != null) {
             str += "&object=" + URLEncoder.encode(Util.toJson(request.object).toString());
        	  }
        	  */
        	  if (request.criteria != null && request.criteria.length > 0)
        		  str += "&criteria=" + URLEncoder.encode(criteriaToJsonStr(request.criteria).toString());
 			Log.d(TAG, "=====================================");
 			Log.d(TAG, "-------------------------------------");			
             Log.d(TAG, "REQUEST SENT IS:");
             Log.d(TAG, str);
 			Log.d(TAG, "-------------------------------------");			
 			Log.d(TAG, "=====================================");       	  
           return str;
  //   	  return "?testing=here&format=json&criterion={\"testvar\", "testvalue";
     	  
       }
 	  public static String makeRequest(AppRequest request) {
 		  StringBuffer content = new StringBuffer();
 		  try {
 			HttpRequestBase hrb = null;			
 
 			DefaultHttpClient httpclient = new DefaultHttpClient();
 			StringEntity se;	
 			Log.d(TAG, "GAMINGSERVICE.GAMINGSERVER IS: " + GamingService.gamingServer);
 			if ("get".equals(request.action)) {
 				hrb = new HttpGet(GamingService.gamingServer + constructParams(request));
 				
 			} else if ("put".equals(request.action)) {
 				hrb = new HttpPut(GamingService.gamingServer + constructParams(request));
 		       	  if (request.object != null) {
 		       		  se = new StringEntity(URLEncoder.encode(Util.toJson(request.object).toString()));
 		       		  ((HttpPut) hrb).setEntity(se);
 		         	  }				
 			} else if ("delete".equals(request.action)) {
 				hrb = new HttpDelete(GamingService.gamingServer + constructParams(request));
 			} else if ("post".equals(request.action)) {
 				hrb = new HttpPost(GamingService.gamingServer + constructParams(request));	
 		       	  if (request.object != null) {
 		       		  se = new StringEntity(URLEncoder.encode(Util.toJson(request.object).toString()));
 		       		  ((HttpPost) hrb).setEntity(se);
 		         	  }				
 			}
 
 /*			
 			BasicHttpParams httpParams = new BasicHttpParams();
 			httpParams.setParameter("request_id", request.id);
 			httpParams.setParameter("app_id", request.app_id);
 			httpParams.setParameter("app_api_key", request.app_api_key);
 			httpParams.setParameter("user_id", request.user_id);
 			hrb.setParams(httpParams);	
             httpclient.setParams(httpParams);
             Log.d(TAG, "HTTPPARAMS IS: " + httpParams);
 */
 			hrb.setHeader("Accept", "application/json"); 
 			hrb.setHeader("Content-type", "application/json");			
 			Log.d(TAG, "EXECUTING HTTP REQUEST");
 			HttpResponse response = httpclient.execute(hrb);
 			Log.d(TAG, "COMPLETED HTTP REQUEST, GETTING RESPONSE");
 
 			InputStream is = response.getEntity().getContent();
 			BufferedReader sr = new BufferedReader(new InputStreamReader(is));
 			String line = "";
 			while ((line = sr.readLine()) != null) {
 					content.append(line);
 			}
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return generateErrResponse(request);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return generateErrResponse(request);
 
 			}
 			Log.d(TAG, "=====================================");
 			Log.d(TAG, "-------------------------------------");			
             Log.d(TAG, "RESPONSE RECEIVED FROM THE NETWORK IS:");
             Log.d(TAG, content.toString());
 			Log.d(TAG, "-------------------------------------");	
 			Log.d(TAG, "=====================================");
 			
 			return content.toString();		  
 	  }    
     public static String generateErrResponse(AppRequest appRequest) {
     	AppResponse appResponse = new AppResponse();
     	appResponse.request_id = appRequest.id;
     	appResponse.error = new String[1];
     	appResponse.error[0] = "Error sending request";
     	appResponse.appRequest = appRequest;
     	appResponse.result_code = GamingServiceConnection.RESULT_CODE_ERROR;
     	return toJson(appResponse).toString();
     }
     
 	public static String makeGet(String path) { //, Map params) {
 		StringBuffer content = new StringBuffer();
 
 		DefaultHttpClient httpclient = new DefaultHttpClient();
 //		HttpDelete httpDelete = new HttpDelete(path);
 		HttpGet httpGet = new HttpGet(path+ "?format=json");
 //		httpclient.set
 //		StringEntity se = new StringEntity(holder.toString());
 //		httpost.setEntity(se);
 //		Log.d(TAG, "HERE HERE path is " + path + " port is: " + httpGet.getURI());
 		BasicHttpParams httpParams = new BasicHttpParams();
 		httpParams.setParameter("format", "json");
 		httpParams.setParameter("testing", "123");
 		httpGet.setParams(httpParams);
 		
 		/*
 		Iterator iter = params.entrySet().iterator();
 
 		JSONObject holder = new JSONObject();
 
 		while(iter.hasNext()) {
 		Map.Entry pairs = (Map.Entry)iter.next();
 		String key = (String)pairs.getKey();
 		Map m = (Map)pairs.getValue();
 		   
 		JSONObject data = new JSONObject();
 		Iterator iter2 = m.entrySet().iterator();
 		while(iter2.hasNext()) {
 		Map.Entry pairs2 = (Map.Entry)iter2.next();
 		data.put((String)pairs2.getKey(), (String)pairs2.getValue());
 		}
 		holder.put(key, data);
 		}
 
 		StringEntity se = new StringEntity(holder.toString());
 		httpost.setEntity(se);
 		httpost.setHeader("Accept", "application/json");
 		httpost.setHeader("Content-type", "application/json");
 
 		ResponseHandler responseHandler = new BasicResponseHandler();
 
 		try {
 			Object response = httpclient.execute(httpget, responseHandler);
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 			*/
 		try {
 //			HttpResponse response = httpGet.
 //            HttpPost httppost = new HttpPost("test");
 //            httppost.sethe
 //			httpGet.setHeader("Accept", "application/json");
 //			httpGet.setHeader("Content-type", "application/json");			
 			HttpResponse response = httpclient.execute(httpGet);
 			InputStream is = response.getEntity().getContent();
 			BufferedReader sr = new BufferedReader(new InputStreamReader(is));
 			String line = "";
 //			while ((line = sr.readLine()) != null) {
 //				content.append(line);
 //			}
 	        content.append(sr.readLine() + "\n");
 //	        completedTaskList.add("Counter: " + counter + "\nName: " + path + "\n Content: " +content.toString() + "\n");
 //			Log.d(TAG, "Counter: " + counter + "\nContent: " +content.toString());
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return content.toString();
 		}
 			/*
 		public static String makeRequest(String path, Map params)
 		throws Exception {
 
 		DefaultHttpClient httpclient = new DefaultHttpClient();
 		HttpPost httpost = new HttpPost(path);
 		Iterator iter = params.entrySet().iterator();
 
 		JSONObject holder = new JSONObject();
 
 		while(iter.hasNext()) {
 		Map.Entry pairs = (Map.Entry)iter.next();
 		String key = (String)pairs.getKey();
 		Map m = (Map)pairs.getValue();
 		   
 		JSONObject data = new JSONObject();
 		Iterator iter2 = m.entrySet().iterator();
 		while(iter2.hasNext()) {
 		Map.Entry pairs2 = (Map.Entry)iter2.next();
 		data.put((String)pairs2.getKey(), (String)pairs2.getValue());
 		}
 		holder.put(key, data);
 		}
 
 		StringEntity se = new StringEntity(holder.toString());
 		httpost.setEntity(se);
 		httpost.setHeader("Accept", "application/json");
 		httpost.setHeader("Content-type", "application/json");
 
 		ResponseHandler responseHandler = new BasicResponseHandler();
 		Object response = httpclient.execute(httpost, responseHandler);
 		return "done";
 		}
     */
   
   /*
   public static Object toJson(String name, Object obj) {
 	  if (obj != null)
 	  Log.d(TAG, obj.getClass().getName());
 
 	  JSONObject jsonObj = new JSONObject();
 
 
 	  try {
 	  if (obj == null)
 		  return null;
 	  if (obj.getClass().isPrimitive() || isWrapperType(obj.getClass())) {
 		jsonObj.put(name, obj);
         return jsonObj;
 			
 	  }
 //	  Log.d(TAG, "OBJECT CLASS IS: " + obj.getClass());
 	  Log.d(TAG, "isArray: " + obj.getClass().isArray());
 	  if (obj.getClass().isArray()) {
 		  Log.d(TAG, "ARRAY!!!");
 //		  Array arr = (Array) obj;
 		  int arrLen = Array.getLength(obj);
 		  JSONArray jsonArray = new JSONArray();
 		  for (int i=0; i < arrLen; i++) {
 			  Object obj1 = Array.get(obj, i);
 			  if (obj1.getClass().isPrimitive() || isWrapperType(obj1.getClass())) {
 				  jsonArray.put(obj1);
 			  }  else {
 				  jsonArray.put(toJson(obj1.getClass().getName(), obj1));
 					  
 			  }
 		  }
 //		  jsonObj.put
 		  Log.d(TAG, "ARRAY PUTTING: " + name + ":" + jsonArray);
 //		  jsonObj.put(name, jsonArray);
 		  return jsonArray;
 	  }
 	
 	  // It is a complex object
 //	  JSONArray jsonArray = new JSONArray();		  
 	      JSONObject jsonObj1 = new JSONObject();
 	      
   		  Field[] fields = obj.getClass().getFields();
 		  for (Field field: fields) {
 			  Object obj1 = field.get(obj);
 			  Log.d(TAG, "HERE: " + field.getName() + field.get(obj));
 			  Log.d(TAG, "Primitive check for: " + obj1.getClass() + ":" + obj1.getClass().isPrimitive() + ":" + isWrapperType(obj1.getClass()));
 			  if (obj1.getClass().isPrimitive() || isWrapperType(obj1.getClass())) {
 					jsonObj1.put(field.getName(), obj1);	
 			  } else {
 //	            jsonObj1.put(field.getName(), toJson(obj1);	
 			  }
  //            jsonArray.put(toJson(field.getName(), field.get(obj)));
 				  
 //					 Log.d(TAG, "NOT EQUALS");
 //				     Log.d(TAG, field.getName() + ":" + field.getType().toString() + ":" + field.get(obj));
 
 		  }	  
  
 		jsonObj.put(name, jsonObj1);
 	  } catch (Exception e) {
 		  e.printStackTrace();
 	  }
 	  return jsonObj;
 
 
   }
 */
   /*.equals(String.class) || obj.getClass().equals(Integer.class) ||
   obj.getClass().equals(Float.class) || obj.getClass().equals(Long.class) ||
   obj.getClass().equals(Double.class) || obj.getClass().equals(Boolean.class))*/
 
   /*
   jsonObj.
   jsonObj.
   jsonObj.
   jsonObj.put.put(jsonObj, obj.getClass().getName());
 
   */
 
   /*
   public JSONObject toJson(Object object, String names[]) {
 	  JSONObject jsonObj = new JSONObject();
       Class c = object.getClass();
       for (int i = 0; i < names.length; i += 1) {
           String name = names[i];
           try {
               jsonObj.putOpt(name, c.getField(name).get(object));
           } catch (Exception ignore) {
           }
       }
       return jsonObj;
   }
 */
   
 }
