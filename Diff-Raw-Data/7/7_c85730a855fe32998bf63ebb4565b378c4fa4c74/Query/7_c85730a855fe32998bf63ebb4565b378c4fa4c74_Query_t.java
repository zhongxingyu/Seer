 package com.seo.webapp;
 
 import java.net.*;
 import java.io.*;
 
 public class Query {
 	private String m_query;
 	private String m_siteToCompare = "Shopzilla";
 	
 	public String HTTP_Request() throws Exception {
 		URL url           = new URL("http://localhost:5000/");
 		URLConnection uc  = url.openConnection();
 		BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
		String inputLine  = "";
		String prev       = "";
 		
		while ( (inputLine = in.readLine()) != null ) {
 			System.out.println(inputLine);
 			prev = inputLine;
 		}
 		
 		in.close();
 		return prev;
 	}
 
 	public String getQuery() {
 		return m_query;
 	}
 
 	public void setQuery(String query) {
 		m_query = query;
 	}
 
 	public String getSiteToCompare() {
 		return m_siteToCompare;
 	}
 
 	public void setSiteToCompare(String siteToCompare) {
 		m_siteToCompare = siteToCompare;
 	}
 }
