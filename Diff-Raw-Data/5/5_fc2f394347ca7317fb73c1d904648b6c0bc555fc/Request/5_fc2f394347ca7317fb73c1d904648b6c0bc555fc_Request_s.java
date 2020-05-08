 package com.sk.web;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class Request {
 
 	private final URL startUrl;
 	private final URL baseUrl;
 	private final String method;
 	private URL url;
 	private final Map<String, String> query = new HashMap<>(), headers = new HashMap<>();
 
 	public Request(String url, String method) throws MalformedURLException {
 		this.method = method.toUpperCase();
 		startUrl = new URL(url);
 		baseUrl = new URL(startUrl, startUrl.getPath());
 	}
 
 	public void addHeader(String key, String value) {
 		headers.put(key, value);
 	}
 
 	public void addQuery(String key, String value) {
 		query.put(key, value);
 	}
 
 	public Map<String, String> getQuery() {
 		return query;
 	}
 
 	public URL getBaseURL() {
 		return baseUrl;
 	}
 
 	public URLConnection openConnection() throws IOException {
 		finalizeUrl();
 		URLConnection conn = url.openConnection();
 		setRequestMethod(conn);
 		addRequestHeaders(conn);
 		return conn;
 	}
 
 	private void finalizeUrl() {
 		url = isGetRequest() ? addGetParamsToUrl() : startUrl;
 	}
 
 	private URL addGetParamsToUrl() {
 		Map<String, String> params = getQueryAndUrlParams();
 		return params.size() > 0 ? addParamsToBaseUrl(params) : baseUrl;
 	}
 
 	protected Map<String, String> getQueryAndUrlParams() {
 		Map<String, String> params = getUrlParams();
 		params.putAll(query);
 		return params;
 	}
 
 	private Map<String, String> getUrlParams() {
 		return IOUtil.splitParams(this.startUrl.getQuery());
 	}
 
 	private URL addParamsToBaseUrl(Map<String, String> params) {
 		try {
			return new URL(baseUrl, "?" + IOUtil.joinParams(params));
 		} catch (MalformedURLException ignored) {
 			throw new RuntimeException("Bad parameters");
 		}
 	}
 
 	public void setRequestMethod(URLConnection conn) throws ProtocolException {
 		if (conn instanceof HttpURLConnection)
 			((HttpURLConnection) conn).setRequestMethod(method);
 	}
 
 	public String getRequestMethod() {
 		return method;
 	}
 
 	public boolean isPostRequest() {
 		return method.equals("POST");
 	}
 
 	public boolean isGetRequest() {
 		return method.equals("GET");
 	}
 
 	public void addRequestHeaders(URLConnection conn) {
 		for (Entry<String, String> prop : headers.entrySet())
 			conn.addRequestProperty(prop.getKey(), prop.getValue());
 	}
 
 	public void postQueryToConnection(URLConnection conn) throws IOException {
 		String data = IOUtil.joinParams(query);
 		conn.getOutputStream().write(data.getBytes());
 		conn.getOutputStream().close();
 	}
	
 	public boolean needsToPostQuery() {
 		return isPostRequest() && getQuery().size() > 0;
 	}
 
 }
