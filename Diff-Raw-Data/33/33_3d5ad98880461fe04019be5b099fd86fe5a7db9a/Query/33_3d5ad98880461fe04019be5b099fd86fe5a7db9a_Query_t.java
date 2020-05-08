 package com.seo.webapp;
 
 import org.json.simple.*;
 import java.net.*;
 import java.io.*;
 import java.util.*;
 
 
 public class Query {
 	
 	/********************************************
 	 *                                          *
 	 *        MANIFEST CONSTANTS                *
 	 *                                          *
 	 ********************************************/
 	private static final String WEBSERVICE_ADDR = "http://localhost:5000/";
 	public  static final String LOG_ERR_MSG     = "Warning!! Could not connect to localhost:5000/*";
 	public  static final String PAGE_ERR_MSG    = "Could not connect to webservice.";
 	
 	/********************************************
 	 *                                          *
 	 *             Member variables             *
 	 *                                          *
 	 ********************************************/
 	private String m_query;
 	private String m_siteToCompare = "shopzilla";
	private String m_JSONString = "";
 	private ArrayList<SiteMetrics> m_sites;
 	private Recommendation m_recommendation;
 
 	/********************************************
 	 *                                          *
 	 *                Methods                   *
 	 *                                          *
 	 ********************************************/
 	
 	
 	/**
 	 * @return JSON Object in the form of a string 
 	 * @throws Exception	If connection to webservice cannot be established. 
 	 */
 	public void HTTP_Request() throws Exception {
 		
 		URI site_uri = new URI(
 					// scheme
 					"http",
 					
 					// userInfo
 					null,
 					
 					// host
 					"localhost",
 					
 					//port
 					5000,
 					
 					// path
 					"/" + m_query + "/" + m_siteToCompare,
 					
 					//query 
 					null,
 					
 					// fragment
 					null
 				);
 		
 		URL url = site_uri.toURL();
 		URLConnection uc  = url.openConnection();
 		BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
 		String inputLine  = "";
 		String prev       = "";
 		
 		while ( (inputLine = in.readLine()) != null ) {
			m_JSONString += inputLine;
 		}
 		
 		in.close();
 	}
 
 	// Parses JSON string and places each site into a SiteMetrics object
 	// Creates ArrayList of these objects
 	// Throws exception if certain properties are missing
 	public void setSiteArray() throws Exception
 	{
 		m_sites = new ArrayList<SiteMetrics>();
 		JSONObject jobj = (JSONObject)JSONValue.parse(m_JSONString);
 		JSONArray domainNames = (JSONArray)jobj.get("domain");
 
 		for(int i = 0; i < domainNames.size(); i++)
 		{
 			String siteName = domainNames.get(i).toString();
 			JSONObject siteObj = (JSONObject)jobj.get(siteName);
 			siteObj.put("domain", siteName);
 			SiteMetrics  siteMets = new SiteMetrics(siteObj);
 			m_sites.add(siteMets);
 		}
 	}
 
 	// Creates the Recommendation object for results.jsp
 	// Throws exception if certain properties are not found
 	public void setRecommendation() throws Exception
 	{
		m_recommendation = new Recommendation(m_sites);
 		m_recommendation.calculateDifferenceValues();
 	}
 
 	// Returns ArrayList of SiteMetrics
 	public ArrayList<SiteMetrics> getSites()
 	{
 		return m_sites;
 	}
 
 	// Returns Recommendations object
 	public Recommendation getRecommendation()
 	{
 		return m_recommendation;
 	}
 
 	/**
 	 * @return Query string
 	 */
 	public String getQuery() {
 		return m_query;
 	}
 
 	/**
 	 * @param query Sets m_query. 
 	 */
 	public void setQuery(String query) {
 		m_query = query;
 	}
 
 	/**
 	 * @return The site that the customer wishes to compare (e.g. Shopzilla)
 	 */
 	public String getSiteToCompare() {
 		return m_siteToCompare;
 	}
 
 	/**
 	 * @param siteToCompare Sets string that represents the site that will be compared against the top Google searches
 	 */
 	public void setSiteToCompare(String siteToCompare) {
 		m_siteToCompare = siteToCompare;
 	}
 }
