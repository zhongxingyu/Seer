 package com.dovico.commonlibrary;
 
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 
 import javax.swing.JOptionPane;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 
 
 public class CRESTAPIHelper {
 	// The root URI for REST API calls
 	private final static String m_sRootURI = "https://api.dovico.com/"; 
 
 	// sURIPart: Clients/
 	// sOptionalQueryStrings: some resources like GET 'TimeEntries/' can accept certain query strings (like 'daterange={daterange}')
 	// sRESTAPIVersion: the version of the REST API being targeted (e.g. 1)
 	public static String buildURI(String sURIPart, String sOptionalQueryStrings, String sRESTAPIVersion) {
 		// Check to see if there was a query string passed in. Build up the Query string for the URI
 		boolean bHaveQueryString = !sOptionalQueryStrings.isEmpty();
 		String sQueryString = ("?" + sOptionalQueryStrings + (bHaveQueryString ? "&": "") + "version=" + sRESTAPIVersion);
 		
 		// Return the proper URI
 		return (m_sRootURI + sURIPart + sQueryString);
 	}		
 	
 	
 	// Builds the Authorization header's content
 	private static String buildAuthorizationHeaderContent(String sConsumerSecret, String sDataAccessToken) {
 		return ("WRAP access_token=\"client="+ sConsumerSecret + "&user_token="+ sDataAccessToken + "\"");
 	}
 	
 
 	// Returns the result of a call (REST API v2 is currently XML-based so an XML Document object is returned by this function) 
 	/// <history>
     /// <modified author="C. Gerard Gallant" date="2012-03-30" reason="Modified to call the newly overloaded version of this function (left this one so that I don't break existing code implementations)"/>
    /// </history>
 	public static APIRequestResult makeAPIRequest(String sURI, String sHttpMethod, String sPostPutXMLData, String sConsumerSecret, String sDataAccessToken) {
 		// Create our Request/Result object (3rd param is specified as an empty string since the URI has already been generated...version # is only needed when 
 		// building up the URI. 4th param indicates that the function is to show any error that happens to the user) 
 		APIRequestResult aRequestResult = new APIRequestResult(sConsumerSecret, sDataAccessToken, "", true);
 		aRequestResult.setRequestURI(sURI);
		aRequestResult.setRequestHttpMethod((sHttpMethod == null ? "" : sHttpMethod));
		aRequestResult.setRequestPostPutXmlData(sPostPutXMLData);
 		
 		// Call the overloaded method to handle the work and then return the result object to the caller 
 		makeAPIRequest(aRequestResult);
 		return aRequestResult;
 	}
 	
 	
 	// Returns the result of a call (REST API v2 is currently XML-based so an XML Document object is returned by this function) 
 	public static void makeAPIRequest(APIRequestResult aRequestResult) {
 		try {
 			// Build up the Request to the REST API
 			URL url = new URL(aRequestResult.getRequestURI());			
 			HttpURLConnection hConn = (HttpURLConnection)url.openConnection();
 			hConn.setRequestProperty("Authorization", buildAuthorizationHeaderContent(aRequestResult.getConsumerSecret(), aRequestResult.getDataAccessToken()));
 			hConn.setRequestProperty("Accept", "text/xml"); // Can also use 'application/xml'			
 			hConn.setRequestMethod(aRequestResult.getRequestHttpMethod()); // e.g. GET, POST, PUT, DELETE
 			
 			// If data was provided to be sent then...
 			if(aRequestResult.getHaveRequestPostPutXmlData()) {
 				hConn.setRequestProperty("Content-Type", "text/xml");
 				hConn.setDoOutput(true); // Needed if we want to send data
 			
 				// Write the data to the output stream 
 				OutputStreamWriter wrOutput = new OutputStreamWriter(hConn.getOutputStream());
 				wrOutput.write(aRequestResult.getRequestPostPutXmlData());
 				wrOutput.flush();
 				wrOutput.close();
 			} // End if(aRequestResult.getHaveRequestPostPutXmlData())
 			
 			
 			DocumentBuilderFactory dbfFactory = DocumentBuilderFactory.newInstance();
 			
 			// Establish the connection to the REST API and get the response status code. If the response is 300 or above (error condition) then...
 			hConn.connect();
 			int iResponseCode = hConn.getResponseCode();
 			if(iResponseCode >= 300) {
 				// Get the XML Document object from the Error stream
 				Document xdDocResult = dbfFactory.newDocumentBuilder().parse(hConn.getErrorStream());
 				aRequestResult.setResultDocument(xdDocResult);
 				
 				// Format the error message with the error description (an Error node is returned containing a Status and Description element - we don't need to
 				// parse the Status element since we already have it) 
 				aRequestResult.setRequestErrorMessage(String.format("Error %d\n%s", iResponseCode, CXMLHelper.getChildNodeValue(xdDocResult.getDocumentElement(), "Description")));
 			} else { // No error...
 				// Get the resulting XML Document from the REST API
 				aRequestResult.setResultDocument(dbfFactory.newDocumentBuilder().parse(hConn.getInputStream()));
 			} // End f(iResponseCode >= 300)
 		}
 		catch (MalformedURLException e) { aRequestResult.setRequestErrorMessage(e.getMessage()); } 
 		catch (IOException e) { aRequestResult.setRequestErrorMessage(e.getMessage()); } 
 		catch (Exception e) { aRequestResult.setRequestErrorMessage(e.getMessage()); }		
 		
 		
 		// If there was an error AND we are to show errors to the user then...
 		if(aRequestResult.getHadRequestError() && aRequestResult.getShowErrorsToUser()) { 
 			JOptionPane.showMessageDialog(null, aRequestResult.getRequestErrorMessage(), "Error", JOptionPane.ERROR_MESSAGE);			
 		} // End if(aRequestResult.getHadRequestError() && aRequestResult.getShowErrorsToUser())
 	}
 }
 
