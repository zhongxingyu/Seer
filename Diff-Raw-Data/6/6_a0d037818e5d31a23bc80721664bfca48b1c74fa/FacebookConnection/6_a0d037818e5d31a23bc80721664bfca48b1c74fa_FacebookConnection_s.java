 package org.social.core.network.connection;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONException;
 import net.sf.json.JSONObject;
 import net.sf.json.JSONSerializer;
 
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.conn.params.ConnRoutePNames;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.social.core.query.Query;
 import org.social.core.util.UtilProperties;
 import org.social.core.util.UtilValidate;
 
 public class FacebookConnection implements SocialNetworkConnection {
 
 	private Logger logger = LoggerFactory.getLogger(this.getClass());
 
 	private String MY_ACCESS_TOKEN = null;
 
 	private HttpClient httpClient;
 
 	public FacebookConnection() {
 		loadProperties();
 
 		httpClient = new DefaultHttpClient();
 
 		setHttpsProxy();
 	}
 
 	private void setHttpsProxy() {
 
 		String host = System.getProperty("https.proxyHost");
 		String portString = System.getProperty("https.proxyPort");
 
 		if (UtilValidate.isNotEmpty(host) && UtilValidate.isNotEmpty(portString)) {
 			int port = Integer.parseInt(portString);
 
 			HttpHost proxy = new HttpHost(host, port);
 			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
 		}
 	}
 
 	@Override
 	public List<JSONObject> getRemoteData(Query query) {
 		List<JSONObject> resultList = new ArrayList<JSONObject>();
 		String nextPageUrl = query.constructQuery() + "&access_token=" + MY_ACCESS_TOKEN;
 
 		do {
 			String response = readDataFromUrl(nextPageUrl);
 
 			JSONObject json = null;
 			try {
 				json = (JSONObject) JSONSerializer.toJSON(response);
 			} catch (JSONException je) {
 				logger.error(je.getMessage());
 				continue;
 			}
 
 			if (json.containsKey("data")) {
 				resultList.addAll(addToResultList(json.getJSONArray("data")));
 			}
 
 			nextPageUrl = getNextRequestUrl(json);
 		} while (nextPageUrl != null && !nextPageUrl.isEmpty());
 
 		return resultList;
 	}
 
 	private String getNextRequestUrl(JSONObject json) {
 		String nextPageUrl = null;
 		if (json.containsKey("paging")) {
 			JSONObject paging = json.getJSONObject("paging");
 			if (paging.containsKey("next")) {
 				nextPageUrl = paging.getString("next");
 			}
 		}
 		return nextPageUrl;
 	}
 
 	private String readDataFromUrl(String queryUrl) {
 
 		StringBuilder resultStringBuilder = null;
 		BufferedReader in = null;
 		try {
 			HttpUriRequest req = new HttpGet(queryUrl);
 			HttpResponse resp = httpClient.execute(req);
 
 			in = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
 
 			resultStringBuilder = new StringBuilder();
 			String inputLine;
 			while ((inputLine = in.readLine()) != null) {
 				resultStringBuilder.append(inputLine);
 			}
 
 		} catch (Exception e) {
 			logger.error(e.getMessage(), e);
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException e) {
 					logger.error(e.getMessage());
 				}
 			}
 		}
 
 		return resultStringBuilder.toString();
 
 	}
 
 	private List<JSONObject> addToResultList(JSONArray jsonArray) {
 		List<JSONObject> resultList = new ArrayList<JSONObject>();
 
 		for (Object object : jsonArray) {
 			JSONObject jo = (JSONObject) object;
 			resultList.add(jo);
 		}
 		return resultList;
 	}
 
 	private void loadProperties() {
 		MY_ACCESS_TOKEN = UtilProperties.getPropertyValue("conf/fb.properties", "token");
 	}
 }
