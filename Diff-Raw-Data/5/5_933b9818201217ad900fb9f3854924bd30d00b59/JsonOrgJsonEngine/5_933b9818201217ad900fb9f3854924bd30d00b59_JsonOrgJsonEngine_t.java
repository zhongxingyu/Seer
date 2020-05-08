 package com.googlecode.jsonrpc4j.jsonorg;
 
 import java.io.InputStream;
 
 import java.io.OutputStream;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Random;
 
 import org.apache.commons.io.IOUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.googlecode.jsonrpc4j.JsonEngine;
 import com.googlecode.jsonrpc4j.JsonException;
 import com.googlecode.jsonrpc4j.JsonRpcError;
 
 public class JsonOrgJsonEngine 
 	implements JsonEngine {
 	
 	private static final String JSON_RPC_VERSION = "2.0";
 	private static final Random RANDOM = new Random(System.currentTimeMillis());
 
 	public void addTypeAlias(Class<?> fromType, Class<?> toType)
 		throws JsonException {
 		// TODO Auto-generated method stub
 
 	}
 
 	public Object createRpcRequest(String methodName, Object[] arguments)
 		throws JsonException {
 		try {
 			JSONObject request = new JSONObject();
 			request.put("id", RANDOM.nextInt()+"");
 			request.put("jsonrpc", JSON_RPC_VERSION);
 			request.put("method", methodName);
 			request.put("params", JSONUtil.toJSON(arguments));
 			return request;
 		} catch(Exception e) {
 			throw new JsonException(e);
 		}
 	}
 
 	public Object createRpcRequest(
 		String methodName, Map<String, Object> arguments) 
 		throws JsonException {
 		try {
 			JSONObject request = new JSONObject();
 			request.put("id", RANDOM.nextInt()+"");
 			request.put("jsonrpc", JSON_RPC_VERSION);
 			request.put("method", methodName);
 			JSONObject params = new JSONObject();
 			for (String name : arguments.keySet()) {
 				params.put(name, JSONUtil.toJSON(arguments.get(name)));
 			}
 			request.put("params", params);
 			return request;
 		} catch(Exception e) {
 			throw new JsonException(e);
 		}
 	}
 
 	public String getIdFromRpcRequest(Object json) 
 		throws JsonException {
 		try {
 			return ((JSONObject)json).getString("id");
 		} catch(Exception e) {
 			throw new JsonException(e);
 		}
 	}
 
 	public JsonRpcError getJsonErrorFromResponse(Object jsonResponse)
 		throws JsonException {
 		try {
 			return JSONUtil.fromJSON(
 				((JSONObject)jsonResponse).get("error"), 
 				JsonRpcError.class);
 		} catch(Exception e) {
 			throw new JsonException(e);
 		}
 	}
 
 	public Object getJsonResultFromResponse(Object jsonResponse)
 		throws JsonException {
 		try {
 			return ((JSONObject)jsonResponse).get("result");
 		} catch(Exception e) {
 			throw new JsonException(e);
 		}
 	}
 
 	public String getMethodNameFromRpcRequest(Object json) 
 		throws JsonException {
 		try {
 			return ((JSONObject)json).getString("method");
 		} catch(Exception e) {
 			throw new JsonException(e);
 		}
 	}
 
 	public int getParameterCountFromRpcRequest(Object json)
 		throws JsonException {
 		try {
 			JSONObject request = (JSONObject)json;
 			if (!request.has("params")) {
 				return 0;
 			}
 			Object params = request.get("params");
 			if (params instanceof JSONArray) {
 				return ((JSONArray)params).length();
 			} else {
 				return ((JSONObject)params).length();
 			}
 		} catch(Exception e) {
 			throw new JsonException(e);
 		}
 	}
 
 	public Object getParameterFromRpcRequest(Object json, int index)
 		throws JsonException {
 		
 		// make sure it's what we expect
         if (!isRpcRequestParametersIndexed(json)) {
             throw new JsonException(
                 "JSON-RPC request params are not indexed");
         }
         
         try {
         	return ((JSONObject)json).getJSONArray("params").get(index);
         } catch(JSONException je) {
         	throw new JsonException(je);
         }
 	}
 
 	public Object getParameterFromRpcRequest(Object json, String name)
 		throws JsonException {
 		
 		// make sure it's what we expect
         if (isRpcRequestParametersIndexed(json)) {
             throw new JsonException(
                 "JSON-RPC request params are indexed");
         }
         
         try {
         	return ((JSONObject)json).getJSONObject("params").get(name);
         } catch(JSONException je) {
         	throw new JsonException(je);
         }
 	}
 
 	public Iterator<Object> getRpcBatchIterator(Object json)
 		throws JsonException {
 		if (!(json instanceof JSONArray)) {
             throw new JsonException(
                 "Source is not an JSONArray");
         }
 		JSONArray array = JSONArray.class.cast(json);
 		ArrayList<Object> ret = new ArrayList<Object>();
 		for (int i=0; i<array.length(); i++) {
 			ret.add(array.opt(i));
 		}
 		return ret.iterator();
 	}
 
 	public boolean isNotification(Object json) 
 		throws JsonException {
 		return (!(json instanceof JSONObject))
 			? false : (
 				((JSONObject)json).opt("id")==null
 				|| ((JSONObject)json).isNull("id")
 			);
 	}
 
 	public boolean isRpcBatchRequest(Object json) 
 		throws JsonException {
 		return (json instanceof JSONArray);
 	}
 
 	public boolean isRpcRequestParametersIndexed(Object json)
 		throws JsonException {
 		
 		// make sure it's what we expect
         if (!(json instanceof JSONObject)) {
             throw new JsonException(
                 "Source is not a JSONObject");
         }
         
         JSONObject node = JSONObject.class.cast(json);
         return (node.opt("params") instanceof JSONArray);
 	}
 
	@SuppressWarnings("unchecked")
 	public <T> T jsonToObject(Object json, Class<T> valueType)
 		throws JsonException {
         
         try {
         	return (T)JSONUtil.fromJSON(json, valueType);
         } catch(Exception e) {
         	throw new JsonException(e);
         }
 	}
 
	@SuppressWarnings("unchecked")
 	public <T> T jsonToObject(Object json, Type valueType) 
 		throws JsonException {
         
         try {
         	return (T)JSONUtil.fromJSON(json, valueType);
         } catch(Exception e) {
         	throw new JsonException(e);
         }
 	}
 
	@SuppressWarnings("unchecked")
 	public <T> Object objectToJson(T obj) 
 		throws JsonException {
         try {
         	return (T)JSONUtil.toJSON(obj);
         } catch(Exception e) {
         	throw new JsonException(e);
         }
 	}
 
 	public Object readJson(InputStream in) 
 		throws JsonException {
         try {
         	return JSONUtil.fromJSONString(IOUtils.toString(in));
         } catch(Exception e) {
         	throw new JsonException(e);
         }
 	}
 
 	public Object validateRpcBatchRequest(Object json) 
 		throws JsonException {
 		if (!(json instanceof JSONArray)) {
             throw new JsonException(
                 "Source is not an JSONArray");
         }
 		JSONArray array = JSONArray.class.cast(json);
 		for (int i=0; i<array.length(); i++) {
 			validateRpcRequest(array.opt(i));
 		}
         return array;
 	}
 
 	public Object validateRpcRequest(Object json) 
 		throws JsonException {
 		
 		// make sure it's what we expect
 		if (!(json instanceof JSONObject)) {
             throw new JsonException(
                 "Source is not an JSONObject");
         } 
         
         // cast
 		JSONObject obj = JSONObject.class.cast(json);
         String versionNode = obj.optString("jsonrpc");
         String methodNode = obj.optString("method");
         
         // verify version node
         if (versionNode==null 
             || !versionNode.equals("2.0")) {
             throw new JsonException(
                 "\"jsonrpc\" attribute not \"2.0\" or not found");
             
         // verify method node
         } else if (methodNode==null
             || methodNode.trim().equals("")) {
             throw new JsonException(
                 "\"method\" attribute empty or not found");
             
         }
         
         return obj;
 	}
 
 	public void writeJson(Object json, OutputStream out) 
 		throws JsonException {
 		try {
 			out.write(JSONUtil.toJSONString(json).getBytes());
 		} catch(Exception e) {
 			throw new JsonException(e);
 		}
 	}
 
 }
