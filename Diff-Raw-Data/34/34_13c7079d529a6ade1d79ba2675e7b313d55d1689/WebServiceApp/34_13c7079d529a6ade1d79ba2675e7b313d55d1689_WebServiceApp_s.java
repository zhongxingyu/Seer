 /**
  * Meerkat Monitor - Network Monitor Tool
  * Copyright (C) 2011 Merkat-Monitor
  * mailto: contact AT meerkat-monitor DOT org
  * 
  * Meerkat Monitor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Meerkat Monitor is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *  
  * You should have received a copy of the GNU Lesser General Public License
  * along with Meerkat Monitor.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.meerkat.services;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.log4j.Logger;
 import org.meerkat.util.Counter;
 import org.meerkat.util.xml.XMLComparator;
 import org.meerkat.util.xml.XmlFormatter;
 import org.meerkat.webapp.WebAppResponse;
 
 
 public class WebServiceApp extends WebApp {
 	
 	private static Logger log = Logger.getLogger(WebServiceApp.class);
 	private String postXML = "";
 	private String responseXML = "";
 	private String soapAction = "";
 
 	/**
 	 * WebService
 	 * 
 	 * @param name
 	 * @param url
 	 */
 	public WebServiceApp(String name, String url) {
 		super(name, url, "");
 		this.setTypeWebService();
 	}
 
 	/**
 	 * WebService
 	 * 
 	 * @param name
 	 * @param url
 	 * @param executeOnOffline
 	 */
 	public WebServiceApp(String name, String url, String soapAction, String executeOnOffline) {
 		super(name, url, "", executeOnOffline);
 		this.setTypeWebService();
 		this.setSOAPAction(soapAction);
 	}
 
 	/**
 	 * WebService
 	 */
 	public WebServiceApp() {
 		super();
 		this.setTypeWebService();
 	}
 	
 	/**
 	 * checkWebAppStatus
 	 */
 	public final WebAppResponse checkWebAppStatus() {
 		// Set the response at this point to empty in case of no response at all
 		setCurrentResponse("");
 		int statusCode = 0;
 
 		// DefaultHttpClient httpClient = new DefaultHttpClient();
 		HttpClient httpClient = super.httpClientSSLAuth(); // Allow self signed
 		// certificates
 
 		HttpResponse httpresponse = null;
 
 		WebAppResponse response = new WebAppResponse();
 		XMLComparator xmlCompare;
 		response.setResponseWebService();
 		String responseFromPostXMLRequest;
 
 		// Get target URL
 		String strURL = this.getUrl();
 
 		// Prepare HTTP post
 		HttpPost httpPost = new HttpPost(strURL);
 
 		StringEntity strEntity = null;
 		try {
 			strEntity = new StringEntity(postXML, "UTF-8");
 		} catch (UnsupportedEncodingException e3) {
 			log.error("UnsupportedEncodingException: "+e3.getMessage());
 		}
 		httpPost.setEntity(strEntity);
 
 		// Set Headers
 		httpPost.setHeader("Content-type", "text/xml; charset=ISO-8859-1");
 
 		// Set the SOAP Action if specified
 		if (!soapAction.equalsIgnoreCase("")) {
 			httpPost.setHeader("SOAPAction", this.soapAction);
 		}
 
 		// Measure the request time
 		Counter c = new Counter();
 		c.startCounter();
 
 		// Get headers
 		// Header[] headers = httpPost.getAllHeaders();
 		// int total = headers.length;
 		// log.info("\nHeaders");
 		// for(int i=0;i<total;i++){
 		// log.info(headers[i]);
 		// }
 
 		// Execute the request
 		try {
 			httpresponse = httpClient.execute(httpPost);
 			// Set status code
 			statusCode = httpresponse.getStatusLine().getStatusCode();
 		} catch (Exception e) {
			log.error("ClientProtocolException: "+e.getMessage());
 			httpClient.getConnectionManager().shutdown();
 			c.stopCounter();
 			response.setPageLoadTime(c.getDurationSeconds());
 			setCurrentResponse(e.getMessage());
 			return response;
 		}
 
 		response.setHttpStatus(statusCode);
 
 		// Get the response
 		BufferedReader br = null;
 		try {
 			// Read in UTF-8
 			br = new BufferedReader(new InputStreamReader(httpresponse
 					.getEntity().getContent(), "UTF-8"));
 		} catch (IllegalStateException e1) {
 			log.error("IllegalStateException in http buffer: "+e1.getMessage());
 		} catch (IOException e1) {
 			log.error("IOException in http buffer: "+e1.getMessage());
 		}
 
 		String readLine;
 		String responseBody = "";
 		try {
 			while (((readLine = br.readLine()) != null)) {
 				responseBody += "\n" + readLine;
 			}
 		} catch (IOException e) {
 			log.error("IOException in http response: "+e.getMessage());
 		}
 
 		try {
 			br.close();
 		} catch (IOException e1) {
 			log.error("Closing BufferedReader: "+e1.getMessage());
 		}
 
 		response.setHttpTextResponse(responseBody);
 		setCurrentResponse(responseBody);
 
 		// When HttpClient instance is no longer needed,
 		// shut down the connection manager to ensure
 		// immediate deallocation of all system resources
 		httpClient.getConnectionManager().shutdown();
 
 		// Stop the counter
 		c.stopCounter();
 		response.setPageLoadTime(c.getDurationSeconds());
 
 		if (statusCode != HttpStatus.SC_OK) {
 			log.warn("Httpstatus code: " + statusCode + " | Method failed: "
 					+ httpresponse.getStatusLine());
 			// Set the response to the error if none present
 			if (this.getCurrentResponse().equals("")) {
 				setCurrentResponse(httpresponse.getStatusLine().toString());
 			}
 		}
 
 		// Prepare to compare with expected response
 		responseFromPostXMLRequest = responseBody;
 
 		// Format both request response and response file XML
 		String responseFromPostXMLRequestFormatted = "";
 		String xmlResponseExpected = "";
 		XmlFormatter formatter = new XmlFormatter();
 
 		try {
 			responseFromPostXMLRequestFormatted = formatter
 					.format(responseFromPostXMLRequest.trim());
 			xmlResponseExpected = formatter.format(responseXML);
 		} catch (Exception e) {
 			log.error("Error parsing XML: "+e.getMessage());
 		}
 
 		try {
 			// Compare the XML response file with the response XML
 			xmlCompare = new XMLComparator(xmlResponseExpected,
 					responseFromPostXMLRequestFormatted);
 			if (xmlCompare.areXMLsEqual()) {
 				response.setContainsWebServiceExpectedResponse(true);
 			}
 		} catch (Exception e) {
 			log.error("Error parsing XML for comparison: "+e.getMessage());
 		}
 
 		return response;
 	}
 
 	/**
 	 * getPostXML
 	 * 
 	 * @return
 	 */
 	public final String getPostXML() {
 		return postXML;
 	}
 
 	/**
 	 * getResponseXML
 	 * 
 	 * @return
 	 */
 	public final String getResponseXML() {
 		return responseXML;
 	}
 
 	/**
 	 * setPostXML
 	 * 
 	 * @param postXML
 	 */
 	public final void setPostXML(String postXML) {
 		this.postXML = postXML;
 	}
 
 	/**
 	 * setResponseXML
 	 * 
 	 * @param responseXML
 	 */
 	public final void setResponseXML(String responseXML) {
 		this.responseXML = responseXML;
 	}
 
 	/**
 	 * setSOAPAction
 	 * 
 	 * @param soapAction
 	 */
 	public final void setSOAPAction(String soapAction) {
 		this.soapAction = soapAction;
 	}
 
 	/**
 	 * getSOAPAction
 	 */
 	public final String getSOAPAction() {
 		return soapAction;
 	}
 
 }
