 package com.joshdholtz.protocol.lib;
 
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Log;
 
 import com.joshdholtz.protocol.lib.helpers.ProtocolConnectBitmapTask;
 import com.joshdholtz.protocol.lib.helpers.ProtocolConnectTask;
 import com.joshdholtz.protocol.lib.helpers.ProtocolConstants;
 import com.joshdholtz.protocol.lib.helpers.ProtocolConnectTask.GotResponse;
 import com.joshdholtz.protocol.lib.helpers.ProtocolConstants.HttpMethod;
 
 public class Protocol {
 	
 	public final static String CONTENT_TYPE_FORM_DATA = "application/x-www-form-urlencoded";
 	public final static String CONTENT_TYPE_JSON = "application/json";
 
 	private String baseUrl;
 	private Map<String, BasicNameValuePair> headers;
 	
 	private boolean debug;
 	
 	private Protocol() {
 		baseUrl = null;
 		headers = new HashMap<String, BasicNameValuePair>();
 		
 		debug = false;
 	}
 	
 	public static Protocol getInstance() {
 		return LazyHolder.instance;
 	}
 	
 	private static class LazyHolder {
 		private static Protocol instance = new Protocol();
 	}
 	
 	/**
 	 * Sets the base url.
 	 * @param baseUrl
 	 */
 	public void setBaseUrl(String baseUrl) {
 		this.baseUrl = baseUrl;
 	}
 	
 	/**
 	 * Gets the base url.
 	 * @return
 	 */
 	public String getBaseUrl() {
 		return this.baseUrl;
 	}
 	
 	/**
 	 * Adds a header to append to every request.
 	 * @param key
 	 * @param value
 	 */
 	public void addHeader(String key, String value) {
 		headers.put(key, new BasicNameValuePair(key, value));
 	}
 	
 	/**
 	 * Removes a header that would be appended to every request.
 	 * @param key
 	 */
 	public void removeHeader(String key) {
 		headers.remove(key);
 	}
 	
 	/**
 	 * Gets all the headers.
 	 * @return List<BasicNameValuePair>
 	 */
 	public List<BasicNameValuePair> getHeaders() {
 		return new ArrayList<BasicNameValuePair>(headers.values());
 	}
 	
 	/**
 	 * @return the debug
 	 */
 	public boolean isDebug() {
 		return debug;
 	}
 
 	/**
 	 * @param debug the debug to set
 	 */
 	public void setDebug(boolean debug) {
 		this.debug = debug;
 	}
 
 	/**
 	 * Checks if network is available.
 	 * @return boolean
 	 */
 	public boolean isNetworkAvailable(Context context) {
 	    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
 	    return activeNetworkInfo != null;
 	}
 	
 	/**
 	 * Performs a GET request with no params.
 	 * 
 	 * If no base url is set, the route passed in will be the full route used.
 	 * 
 	 * @param route
 	 * @param responseHandler
 	 */
 	public void doGet(String route, final ProtocolResponse responseHandler) {
 		this.doGet(route, new HashMap<String, Object>(), responseHandler);
 	}
 	
 	/**
 	 * Performs a GET request with params.
 	 * 
 	 * If no base url is set, the route passed in will be the full route used.
 	 * 
 	 * @param route
 	 * @param params
 	 * @param responseHandler
 	 */
 	public void doGet(String route, Map<String,Object> params, final ProtocolResponse responseHandler) {
 		this.doGet(route, params, null, responseHandler);
 	}
 	
 	/**
 	 * Performs a GET request with params.
 	 * 
 	 * If no base url is set, the route passed in will be the full route used.
 	 * 
 	 * @param route
 	 * @param params
 	 * @param contentType
 	 * @param responseHandler
 	 */
 	public void doGet(String route, Map<String,Object> params, String contentType, final ProtocolResponse responseHandler) {
 		route = this.formatRoute(route);
 		route = route + this.paramsToString(params);
 		
 		ProtocolConnectTask task = new ProtocolConnectTask(HttpMethod.HTTP_GET, route, contentType, null, new GotResponse() {
 
 			@Override
 			public void handleResponse(HttpResponse response, int status, String data) {
 				if (debug) {
 					Log.d(ProtocolConstants.LOG_TAG, "GET - " + status + ", " + data);
 				}
 				
 				responseHandler.handleResponse(response, status, data);
 			}
 
 			@Override
 			public void handleResponse(HttpResponse response, int status, InputStream in) {
 				
 			}
 			
 		});
 		task.execute();
 	}
 	
 	/**
 	 * Performs a GET request with no params.
 	 * 
 	 * If no base url is set, the route passed in will be the full route used.
 	 * 
 	 * @param route
 	 * @param responseHandler
 	 */
 	public void doGetBitmap(String route, final ProtocolBitmapResponse responseHandler) {
 		route = this.formatRoute(route);
 		
 		ProtocolConnectBitmapTask task = new ProtocolConnectBitmapTask(route, responseHandler);
 		task.execute();
 		
 	}
 	
 	/**
 	 * Performs a POST request with no params.
 	 * 
 	 * If no base url is set, the route passed in will be the full route used.
 	 * 
 	 * @param route
 	 * @param responseHandler
 	 */
 	public void doPost(String route, final ProtocolResponse responseHandler) {
 		this.doPost(route, new HashMap<String, Object>(), responseHandler);
 	}
 	
 	/**
 	 * Performs a POST request with params.
 	 * 
 	 * If no base url is set, the route passed in will be the full route used.
 	 * 
 	 * @param route
 	 * @param params
 	 * @param responseHandler
 	 */
 	public void doPost(String route, Map<String,Object> params, final ProtocolResponse responseHandler) {
 		this.doPost(route, params, CONTENT_TYPE_FORM_DATA, responseHandler);
 	}
 		
 	/**
 	 * Performs a POST request with params.
 	 * 
 	 * If no base url is set, the route passed in will be the full route used.
 	 * 
 	 * @param route
 	 * @param params
 	 * @param contentType
 	 * @param responseHandler
 	 */
 	public void doPost(String route, Map<String,Object> params, String contentType, final ProtocolResponse responseHandler) {
 		this.doPost(route, new HashMap<String,String>(), params, contentType, responseHandler);
 	}
 	
 	/**
 	 * Performs a POST request with params.
 	 * 
 	 * If no base url is set, the route passed in will be the full route used.
 	 * 
 	 * @param route
 	 * @param params
 	 * @param contentType
 	 * @param responseHandler
 	 */
 	public void doPost(String route, Map<String,String> headers, Map<String,Object> params, String contentType, final ProtocolResponse responseHandler) {
 		route = this.formatRoute(route);
 		
 		HttpEntity entity = null;
 		if (Protocol.CONTENT_TYPE_JSON.equals(contentType)) {
 			try {
 				JSONObject jsonObject = new JSONObject(params);
 				entity = new StringEntity(jsonObject.toString());
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 		} else {
 			try {
 				Log.d(ProtocolConstants.LOG_TAG, this.paramsToString(params));
 				entity = new UrlEncodedFormEntity(this.paramsToValuePairs(params), HTTP.UTF_8);
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		ProtocolConnectTask task = new ProtocolConnectTask(HttpMethod.HTTP_POST, route, headers, contentType, entity, new GotResponse() {
 
 			@Override
 			public void handleResponse(HttpResponse response, int status, String data) {
 				if (debug) {
 					Log.d(ProtocolConstants.LOG_TAG, "POST - " + status + ", " + data);
 				}
 				
 				responseHandler.handleResponse(response, status, data);
 			}
 			
 			@Override
 			public void handleResponse(HttpResponse response, int status, InputStream in) {
 				
 			}
 			
 		});
 		task.execute();
 	}
 	
 	/**
 	 * Performs a POST request with params.
 	 * 
 	 * If no base url is set, the route passed in will be the full route used.
 	 * 
 	 * @param route
 	 * @param params
 	 * @param contentType
 	 * @param responseHandler
 	 */
 	public void doPost(String route, JSONObject body, String contentType, final ProtocolResponse responseHandler) {
 		route = this.formatRoute(route);
 		
 		HttpEntity entity = null;
 		try {
 			entity = new StringEntity(body.toString());
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		
 		ProtocolConnectTask task = new ProtocolConnectTask(HttpMethod.HTTP_POST, route, contentType, entity, new GotResponse() {
 
 			@Override
 			public void handleResponse(HttpResponse response, int status, String data) {
 				if (debug) {
 					Log.d(ProtocolConstants.LOG_TAG, "POST - " + status + ", " + data);
 				}
 				
 				responseHandler.handleResponse(response, status, data);
 			}
 			
 			@Override
 			public void handleResponse(HttpResponse response, int status, InputStream in) {
 				
 			}
 			
 		});
 		task.execute();
 	}
 	
 	/**
 	 * Performs a PUT request with no params.
 	 * 
 	 * If no base url is set, the route passed in will be the full route used.
 	 * 
 	 * @param route
 	 * @param responseHandler
 	 */
 	public void doPut(String route, final ProtocolResponse responseHandler) {
 		this.doPut(route, new HashMap<String, Object>(), responseHandler);
 	}
 	
 	/**
 	 * Performs a PUT request with params.
 	 * 
 	 * If no base url is set, the route passed in will be the full route used.
 	 * 
 	 * @param route
 	 * @param params
 	 * @param responseHandler
 	 */
 	public void doPut(String route, Map<String,Object> params, final ProtocolResponse responseHandler) {
 		this.doPut(route, params, CONTENT_TYPE_FORM_DATA, responseHandler);
 	}
 		
 	/**
 	 * Performs a PUT request with params.
 	 * 
 	 * If no base url is set, the route passed in will be the full route used.
 	 * 
 	 * @param route
 	 * @param params
 	 * @param contentType
 	 * @param responseHandler
 	 */
 	public void doPut(String route, Map<String,Object> params, String contentType, final ProtocolResponse responseHandler) {
 		route = this.formatRoute(route);
 		
 		HttpEntity entity = null;
 		if (Protocol.CONTENT_TYPE_JSON.equals(contentType)) {
 			try {
 				JSONObject jsonObject = new JSONObject(params);
 				entity = new StringEntity(jsonObject.toString());
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 		} else {
 			try {
 				entity = new UrlEncodedFormEntity(this.paramsToValuePairs(params), HTTP.UTF_8);
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		ProtocolConnectTask task = new ProtocolConnectTask(HttpMethod.HTTP_PUT, route, contentType, entity, new GotResponse() {
 
 			@Override
 			public void handleResponse(HttpResponse response, int status, String data) {
 				if (debug) {
 					Log.d(ProtocolConstants.LOG_TAG, "PUT - " + status + ", " + data);
 				}
 				
 				responseHandler.handleResponse(response, status, data);
 			}
 			
 			@Override
 			public void handleResponse(HttpResponse response, int status, InputStream in) {
 				
 			}
 			
 		});
 		task.execute();
 	}
 	
 	/**
 	 * Performs a PUT request with params.
 	 * 
 	 * If no base url is set, the route passed in will be the full route used.
 	 * 
 	 * @param route
 	 * @param params
 	 * @param contentType
 	 * @param responseHandler
 	 */
 	public void doPut(String route, JSONObject body, String contentType, final ProtocolResponse responseHandler) {
 		route = this.formatRoute(route);
 		
 		HttpEntity entity = null;
 		try {
 			entity = new StringEntity(body.toString());
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 
 		ProtocolConnectTask task = new ProtocolConnectTask(HttpMethod.HTTP_PUT, route, contentType, entity, new GotResponse() {
 
 			@Override
 			public void handleResponse(HttpResponse response, int status, String data) {
 				if (debug) {
 					Log.d(ProtocolConstants.LOG_TAG, "PUT - " + status + ", " + data);
 				}
 				
 				responseHandler.handleResponse(response, status, data);
 			}
 			
 			@Override
 			public void handleResponse(HttpResponse response, int status, InputStream in) {
 				
 			}
 			
 		});
 		task.execute();
 	}
 	
 	/**
 	 * Performs a DELETE request with no params.
 	 * 
 	 * If no base url is set, the route passed in will be the full route used.
 	 * 
 	 * @param route
 	 * @param responseHandler
 	 */
 	public void doDelete(String route, final ProtocolResponse responseHandler) {
 		this.doDelete(route, new HashMap<String, Object>(), responseHandler);
 	}
 	
 	/**
 	 * Performs a DELETE request with params.
 	 * 
 	 * If no base url is set, the route passed in will be the full route used.
 	 * 
 	 * @param route
 	 * @param params
 	 * @param responseHandler
 	 */
 	public void doDelete(String route, Map<String,Object> params, final ProtocolResponse responseHandler) {
 		this.doDelete(route, params, null, responseHandler);
 	}
 	
 	/**
 	 * Performs a DELETE request with params.
 	 * 
 	 * If no base url is set, the route passed in will be the full route used.
 	 * 
 	 * @param route
 	 * @param params
 	 * @param contentType
 	 * @param responseHandler
 	 */
 	public void doDelete(String route, Map<String,Object> params, String contentType, final ProtocolResponse responseHandler) {
 		route = this.formatRoute(route);
 		route = route + this.paramsToString(params);
 		
 		ProtocolConnectTask task = new ProtocolConnectTask(HttpMethod.HTTP_DELETE, route, contentType, null, new GotResponse() {
 
 			@Override
 			public void handleResponse(HttpResponse response, int status, String data) {
 				if (debug) {
 					Log.d(ProtocolConstants.LOG_TAG, "DELETE - " + status + ", " + data);
 				}
 				
 				responseHandler.handleResponse(response, status, data);
 			}
 			
 			@Override
 			public void handleResponse(HttpResponse response, int status, InputStream in) {
 				
 			}
 			
 		});
 		task.execute();
 	}
 	
 	/**
 	 * Performs a multipart POST with params and files.
 	 * @param route
 	 * @param params
 	 * @param files
 	 * @param responseHandler
 	 */
 	public void doPostWithFile(String route, List<BasicNameValuePair> params, List<File> files, final ProtocolResponse responseHandler) {
 		Map<String, File> filesMap = new HashMap<String, File>();
 		for (int i = 0; i < files.size(); ++i) {
 			filesMap.put("file" + (i+1), files.get(i));
 		}
 		
 		this.doPostWithFile(route, params, filesMap, responseHandler);
 	}
 	
 	/**
 	 * Performs a multipart POST with params and files.
 	 * @param route
 	 * @param params
 	 * @param files
 	 * @param responseHandler
 	 */
 	public void doPostWithFile(String route, List<BasicNameValuePair> params, Map<String, File> files, final ProtocolResponse responseHandler) {
 		route = this.formatRoute(route);
 		
 		Log.d(ProtocolConstants.LOG_TAG, "SHOW THIS!!!!");
 		
 		final String boundary = "---------------------------14737809831466499882746641449";
 		String contentType = "multipart/form-data; boundary=" + boundary;
 		
 		Log.d(ProtocolConstants.LOG_TAG, "Number of files: " + files.size());
 		
 		ProtocolMultipartEntity entity = new ProtocolMultipartEntity(boundary, params, files);
 		Log.d(ProtocolConstants.LOG_TAG, "Size - " + entity.forRealSize());
 		ProtocolConnectTask task = new ProtocolConnectTask(HttpMethod.HTTP_POST_FILE, route, contentType, entity, new GotResponse() {
 
 			@Override
 			public void handleResponse(HttpResponse response, int status, String data) {
 				if (debug) {
 					Log.d(ProtocolConstants.LOG_TAG, "POST FILE - " + status + ", " + data);
 				}
 				
 				responseHandler.handleResponse(response, status, data);
 			}
 			
 			@Override
 			public void handleResponse(HttpResponse response, int status, InputStream in) {
 				
 			}
 			
 		});
 		task.execute();
 	}
 	
 	private String formatRoute(String route) {
 		if (!route.startsWith("http://") && !route.startsWith("https://" ) ) {
 			if (this.getBaseUrl() != null) {
 				route = this.getBaseUrl() + route;
 			}
 		}
 		
 		return route;
 	}
 	
 	private String paramsToString(Map<String, Object> params) {
 		String paramsStr = "";
		if (params != null && params.size() > 0) {
			paramsStr += "?";
 			try {
 				List<String> keys = new ArrayList<String>(params.keySet());
 				for (int i = 0; i < keys.size(); ++i) {
 					if (i != 0) {
 						paramsStr += "&";
 					}
 					paramsStr += URLEncoder.encode(keys.get(i), "UTF-8") + "=" + URLEncoder.encode(params.get(keys.get(i)).toString(), "UTF-8");
 				}
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return paramsStr;
 	}
 	
 	private List<BasicNameValuePair> paramsToValuePairs(Map<String, Object> params) {
 		List<BasicNameValuePair> nameValuePair = new ArrayList<BasicNameValuePair>();
 
 		List<String> keys = new ArrayList<String>(params.keySet());
 		for (int i = 0; i < keys.size(); ++i) {
 			nameValuePair.add(new BasicNameValuePair(keys.get(i), params.get(keys.get(i)).toString()));
 		}
 		
 		return nameValuePair;
 	}
 	
 }
