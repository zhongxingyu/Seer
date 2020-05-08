 package com.buildbotwatcher.worker;
 
 import java.io.DataInputStream;
 import java.io.Serializable;
 import java.net.Authenticator;
 import java.net.PasswordAuthentication;
 import java.net.URL;
 import java.net.URLConnection;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.X509Certificate;
 
 import javax.net.ssl.HostnameVerifier;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 
 public class JsonParser implements Serializable {
 	private static final long serialVersionUID = -7555081429962281610L;
 	private String	_host;
 	private boolean	_auth;
 	private String	_username;
 	private String	_password;
 
 	private static final String PATH_BUILDERS = "/json/builders";
 	private static final String PATH_SLAVES = "/json/slaves";
 	private static final String PATH_PROJECT = "/json?select=project";
 	private static final String PATH_BUILDS = "/json/builders/#{builder}/builds/_all";
 	private static final String PATH_BUILD = "/json/builders/#{builder}/builds/#{number}";
 
 	public JsonParser(String url, int port, boolean auth, String username, String password) {
 		if (port != 80 && port != 443)
 			_host = url + ":" + port;
 		else
 			_host = url;
 		_auth = auth;
 		_username = username;
 		_password = password;
 	}
 
 	private JSONObject getJson(String url) {
 		TrustManager[] trustAllCerts = new TrustManager[] {
 				new X509TrustManager() {
 					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
 						return null;
 					}
 					public void checkClientTrusted(X509Certificate[] certs, String authType) {}
 					public void checkServerTrusted(X509Certificate[] certs, String authType) {}
 				}
 		};
 
 		SSLContext sc = null;
 		try {
 			sc = SSLContext.getInstance("SSL");
 			sc.init(null, trustAllCerts, new java.security.SecureRandom());
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		} catch (KeyManagementException e) {
 			e.printStackTrace();
 		}
 		
 		if (sc == null) {
 			try {
 				sc = SSLContext.getInstance("TLS");
 				sc.init(null, trustAllCerts, new java.security.SecureRandom());
 			} catch (NoSuchAlgorithmException e) {
 				e.printStackTrace();
 			} catch (KeyManagementException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		if (sc == null)
 			Log.e("SSL", "SSL error");
 		else
 			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 
 		// Create all-trusting host name verifier
 		HostnameVerifier allHostsValid = new HostnameVerifier() {
 			public boolean verify(String hostname, SSLSession session) {
 				return true;
 			}
 		};
 		// Install the all-trusting host verifier
 		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
 
 		try {
 			URLConnection cnx = new URL(url).openConnection();
 			if (_auth) {
 				Authenticator.setDefault (new Authenticator() {
 				    protected PasswordAuthentication getPasswordAuthentication() {
 				        return new PasswordAuthentication (_username, _password.toCharArray());
 				    }
 				});
 			}
 			
 			DataInputStream dis = new DataInputStream(cnx.getInputStream());
 			String inputLine;
 			StringBuilder sb = new StringBuilder();
 
 			while ((inputLine = dis.readLine()) != null) {
 				sb.append(inputLine);
 			}
 			dis.close();
 			String tmp = sb.toString();
 			Log.d("json", tmp);
 			return new JSONObject(tmp);
 		} catch (Exception e) {
 			Log.w("JsonParser", e.getMessage());
 			return null;
 		}
 	}
 
 	public List<Builder> getBuilders() {
 		List<Builder> builders = new ArrayList<Builder>();
 		JSONObject objects = getJson(_host + PATH_BUILDERS);
 		if (objects == null)
 			return null;
 		
 		Iterator<?> keys = objects.keys(); 
 
 		while (keys.hasNext()) {
 			Object o = keys.next();
 			try {
 				JSONObject jsono = (JSONObject)objects.get(o.toString());
 				Builder b = new Builder(o, jsono, this);
 				builders.add(b);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return builders;
 	}
 
 	public List<Slave> getSlaves() {
 		List<Slave> slaves = new ArrayList<Slave>();
 		JSONObject objects = getJson(_host + PATH_SLAVES);
 		if (objects == null)
 			return null;
 		
 		Iterator<?> keys = objects.keys(); 
 
 		while (keys.hasNext()) {
 			Object o = keys.next();
 			try {
 				JSONObject jsono = (JSONObject)objects.get(o.toString());
 				Slave s = new Slave(jsono);
 				slaves.add(s);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return slaves;
 	}
 
 	public List<Build> getBuilds(String builderName) {
 		List<Build> builds = new ArrayList<Build>();
 		String url = _host + PATH_BUILDS;
 		url = url.replace("#{builder}", builderName);
 		JSONObject objects = getJson(url);
 		if (objects == null)
 			return null;
 		
 		Iterator<?> keys = objects.keys(); 
 
 		while (keys.hasNext()) {
 			Object o = keys.next();
 			try {
 				JSONObject jsono = (JSONObject)objects.get(o.toString());
 				Build b = new Build(jsono);
 				builds.add(b);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return builds;
 	}
 	
 	public Build getBuild(String builderName, int buildNumber) {
 		String url = _host + PATH_BUILD;
 		url = url.replace("#{builder}", builderName).replace("#{number}", String.valueOf(buildNumber));
 		JSONObject jsono = getJson(url);
 		if (jsono == null)
 			return null;
 		return new Build(jsono);
 	}
 
 	public Project getProject() {
 		Project project = null;
 		JSONObject jsono = getJson(_host + PATH_PROJECT);
 		try {
 			jsono = jsono.getJSONObject("project");
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		if (jsono == null)
 			return null;
 		
 		project = new Project(jsono);
 
 		return project;
 	}
 
 	public static Timestamp parseJSONTimeStamp(String ts) {
		if (ts == null)
			return null;
		String[] tss = ts.split("E");
		if (tss.length < 2)
			return null;
		int mult = Integer.parseInt(tss[1]);
		long time = Long.parseLong(tss[0].replace(".", "").substring(0, mult + 1));
 		Timestamp timestamp = new Timestamp(time * 1000);
 		return timestamp;
 	}
 
 	public static List<Integer> arrayToListInteger(JSONArray jsonArray) {
 		if (jsonArray == null)
 			return Collections.emptyList();
 
 		String array = jsonArray.toString();
 		array = array.replace("[", "").replace("]", "");
 
 		if (array.equals(""))
 			return Collections.emptyList();
 
 		List<Integer> list = new ArrayList<Integer>();
 		for (String s : array.split(",")) {
 			Integer i = Integer.parseInt(s);
 			list.add(i);
 		}
 
 		return list;
 	}
 
 	public static List<String> arrayToListString(JSONArray jsonArray) {
 		if (jsonArray == null)
 			return Collections.emptyList();
 
 		String array = jsonArray.toString();
 		array = array.replace("[", "").replace("]", "");
 
 		if (array.equals(""))
 			return Collections.emptyList();
 
 		List<String> list = new ArrayList<String>();
 		for (String s : array.split(","))
 			list.add(s);
 
 		return list;
 	}
 }
