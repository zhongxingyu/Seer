 package com.liveensure.example;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 public class LiveEnsureServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	PrintWriter out;
 
 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
 		res.setContentType("application/json;charset=UTF-8");
 		res.setHeader("Cache-Control", "no-store"); // HTTP 1.1
 		res.setHeader("Pragma", "no-cache"); // HTTP 1.0
 		res.setDateHeader("Expires", 0); // prevents caching at the proxy server
 		out = res.getWriter();
 		HttpSession session = req.getSession();
 		String sessionToken = (String)session.getAttribute("liveensure_token");
 		String sessionHost = (String)session.getAttribute("liveensure_host_url");
 		if (sessionToken == null) {
 			printObject("error", "Missing user session token.");
 			return;
 		}
 		if (sessionHost == null) {
 			printObject("error", "Missing user session host.");
 			return;
 		}
 		
 		// Proxy to LiveEnsure "Session" handler to deal with SOP difficulties
 		String data = buildPostBody(req);
 		URL u = new URL(sessionHost + "/Session");
 		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
 		conn.setDoOutput(true);
 		conn.setRequestMethod( "POST" );
 		conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
 		conn.setRequestProperty( "Content-Length", String.valueOf(data.length()));
 		OutputStream os = conn.getOutputStream();
 		os.write( data.getBytes() );
 		try {
         	os.close();
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
         StringBuilder leResponse = new StringBuilder();
 		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
         String inputLine;
 
         while ((inputLine = in.readLine()) != null) {
             leResponse.append(inputLine);
 			leResponse.append("\n");
 		}
 		try {
         	in.close();
 		} catch (Exception e2) {
 			e2.printStackTrace();
 		}
 	    out.print(leResponse.toString());
 	    out.flush();
 	}
 
 	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
 		doGet(req, res);
 	}
 
 	void printObject(String objectType, String objectMessage) {
 		String json = "{\"type\":\"" + objectType + "\",\"message\":\"" + objectMessage + "\"}";
 		out.println(json);
 	}
 	
 	String buildPostBody(HttpServletRequest req) {
 		StringBuilder response = new StringBuilder();
		boolean addAmpersand = true;
 		for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
 			if (addAmpersand) {
 				response.append("&");
 			} else {
 				addAmpersand = true;
 			}
		    System.out.println(entry.getKey() + "/" + entry.getValue());
 			String values[] = entry.getValue();
 			response.append(entry.getKey());
 			response.append("=");
 			// Just use the simple case of a single value (no checkbox lists are used)
 			try {
 				response.append(URLEncoder.encode(values[0], "UTF-8"));
 			} catch(Exception e) {
 			}
 		}
 		return response.toString();
 	}
 
 	public static String[] startSession(String email) {
 		// Load up our account settings for easy access
 		String apiKey  = LiveEnsureConfig.sharedConfig.getApiKey();
 		String apiPass = LiveEnsureConfig.sharedConfig.getApiPassword();
 		String agentID = LiveEnsureConfig.sharedConfig.getAgentID();
 		String version = LiveEnsureConfig.sharedConfig.getApiVersion();
 		String startUrl = "/sessionStart/" + version + "/" + email + "/" + agentID + "/" + apiKey + "/" + apiPass;
 		String response[] = new String[2];
 		String result = makeLiveEnsureRequest(startUrl);
 		if (result == null || result.startsWith("0")) {
 			response[0] = "error";
 			response[1] = "Failed to start session.";
 		} else if (result.startsWith("1") || result.startsWith("4")) {
 			// Success! get the session token and redirect host (if any)
 			int cap = result.indexOf(":",2);
 			response[0] = result.substring(2,cap);
 			if (result.startsWith("4")) {
 				String lines[] = result.split("\n");
 				response[1] = lines[1];
 			} else {
 				// Just use the main API host
 				response[1] = LiveEnsureConfig.sharedConfig.getHostURL();
 			}
 		} else if (result.startsWith("2")) {
 			response[0] = "down";
 			response[1] = "LiveEnsure service appears to be down.";
 		}
 		return response;
 	}
 	
 	public static String sessionStatus(String hostURL, String sessionToken) {
 		// Load up necessary account settings for easy access
 		String apiKey  = LiveEnsureConfig.sharedConfig.getApiKey();
 		String apiPass = LiveEnsureConfig.sharedConfig.getApiPassword();
 		String version = LiveEnsureConfig.sharedConfig.getApiVersion();
 		boolean extended = LiveEnsureConfig.sharedConfig.getExtendedStatus();
 		String statusUrl = "/sessionStatus" + (extended ? version + "/" : "/") + sessionToken + "/" + apiKey + "/" + apiPass;
 		String response = makeLiveEnsureRequest(statusUrl);
 		return response;
 	}
 	
 	static String makeLiveEnsureRequest(String url) {
 		return makeLiveEnsureRequest(LiveEnsureConfig.sharedConfig.getHostURL(), url, true);
 	}
 	
 	static String makeLiveEnsureRequest(String hostURL, String url) {
 		return makeLiveEnsureRequest(LiveEnsureConfig.sharedConfig.getHostURL(), url, true);
 	}
 	
 	public static String makeLiveEnsureRequest(String url, boolean apiCall) {
 		return makeLiveEnsureRequest(LiveEnsureConfig.sharedConfig.getHostURL(), url, apiCall);
 	}
 	
 	static String makeLiveEnsureRequest(String hostURL, String url, boolean apiCall) {
 		// Load up our settings for easy access
 		String baseUrl = apiCall ? (hostURL + "/idr") : hostURL;
 
 		if (!url.startsWith("/")) {
 			url = "/" + url;
 		}
         StringBuilder leResponse = new StringBuilder();
 		BufferedReader in = null;
         URL leRequest = null;
 		try {
 			leRequest = new URL(baseUrl + url);
 			URLConnection connection = leRequest.openConnection();
 	        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 
 	        String inputLine;
 
 	        while ((inputLine = in.readLine()) != null) {
 	            leResponse.append(inputLine);
 				leResponse.append("\n");
 			}
 		} catch (Exception e) {
 			System.err.println("Error reading LiveEnsure URL: " + url);
 			e.printStackTrace();
 		} finally {
 			try {
 	        	in.close();
 			} catch (Exception e2) {
 				e2.printStackTrace();
 			}
 		}
 	    return leResponse.toString();
 	}
 }
