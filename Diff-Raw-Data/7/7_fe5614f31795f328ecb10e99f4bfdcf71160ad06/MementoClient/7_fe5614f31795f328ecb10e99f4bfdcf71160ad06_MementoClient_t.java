 /**
  * MementoBrowser.java
  * 
  * Copyright 2010 Frank McCown
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *
  *  
  *  This is the Memento Browser activity which houses a customized web browser for
  *  performing http queries using Memento.
  *  
  *  Learn more about Memento:
  *  http://mementoweb.org/
  */
 
 package dev.memento;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.Header;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.ParseException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.conn.params.ConnRoutePNames;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.apache.log4j.Logger;
 
 
 public class MementoClient {
 	Logger log = Logger.getLogger(MementoClient.class.getCanonicalName());
 	
 	static final int DIALOG_DATE = 0;
     static final int DIALOG_ERROR = 1;
     static final int DIALOG_MEMENTO_DATES = 2;
     static final int DIALOG_MEMENTO_YEARS = 3;
     static final int DIALOG_HELP = 4;
     
 	private String[] mTimegateUris = { "http://mementoproxy.lanl.gov/aggr/timegate/" , "http://mementoproxy.lanl.gov/google/timegate/" };
 	private String mDefaultTimegateUri = mTimegateUris[0];
 	
     private SimpleDateTime mDateChosen = new SimpleDateTime();
         
     private TimeBundle mTimeBundle;
     private TimeMap mTimeMap;
     private Memento mFirstMemento;
     private Memento mLastMemento;
     private MementoList mMementos;
     
     private final int MAX_NUM_MEMENTOS_IN_LIST = 20;
     
     private CharSequence mErrorMessage;
 
     // Used when selecting a memento
     int mSelectedYear = 0;
 
     // Used in http requests
     public String mUserAgent;
 
 	private SimpleDateTime mDateDisplayed;
    	
     private void returnToPresent() {
     	
     	SimpleDateTime mToday = new SimpleDateTime();
     	log.info("Returning to the present.");
     	mMementos.setCurrentIndex(-1);
     }
     
     /**
      *  Helper to create a web-proxy-aware HttpClient:
      * @return
      */
     private HttpClient getHttpClient() {
     	HttpClient httpclient = new DefaultHttpClient();
     	if( System.getProperty("http.proxyHost") != null ) {
     		HttpHost proxy = new HttpHost( System.getProperty("http.proxyHost"), 
     				Integer.parseInt(System.getProperty("http.proxyPort")), "http");
     		httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
     	}
     	return httpclient;
     }
     
     /**
      * Make http requests using the Memento protocol to obtain a Memento or list
      * of Mementos. 
      */
     private void makeHttpRequests(String initUrl) {
     	
         CharSequence mErrorMessage = null;
     	
     	// Contact Memento proxy with chosen Accept-Datetime:
     	// http://mementoproxy.lanl.gov/aggr/timegate/http://example.com/
     	// Accept-Datetime: Tue, 24 Jul 2001 15:45:04 GMT    	   	
         
     	HttpClient httpclient = getHttpClient();
     	
     	// Disable automatic redirect handling so we can process the 302 ourself 
     	httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
  
     	String url = mDefaultTimegateUri + initUrl;
         HttpGet httpget = new HttpGet(url);
         
         // Change the request date to 23:00:00 if this is the first memento.
         // Otherwise we'll be out of range.
         
         String acceptDatetime;
         
         if (mFirstMemento != null && mFirstMemento.getDateTime().equals(mDateChosen)) {
         	log.debug("Changing chosen time to 23:59 since datetime matches first Memento.");
         	SimpleDateTime dt = new SimpleDateTime(mDateChosen);
         	dt.setToLastHour();
         	acceptDatetime = dt.longDateFormatted();
         }
         else {
         	acceptDatetime = mDateChosen.longDateFormatted(); 
         }
         
         httpget.setHeader("Accept-Datetime", acceptDatetime);
         httpget.setHeader("User-Agent", mUserAgent);
         
         //log.debug(getHeadersAsString(response.getAllHeaders()));
         
         log.debug("Accessing: " + httpget.getURI());
         log.debug("Accept-Datetime: " + acceptDatetime);
 
         HttpResponse response = null;
 		try {			
 			response = httpclient.execute(httpget);
 			
 			log.debug("Response code = " + response.getStatusLine());
 			
 			//log.debug(getHeadersAsString(response.getAllHeaders()));
 		} catch (ClientProtocolException e) {
 			mErrorMessage = "Unable to contact proxy server. ClientProtocolException exception.";
 			log.error(getExceptionStackTraceAsString(e));
 			return;
 		} catch (IOException e) {
 			mErrorMessage = "Unable to contact proxy server. IOException exception.";
 			log.error(getExceptionStackTraceAsString(e));
 			return;
 		} finally {
 			// Deallocate all system resources
 	        httpclient.getConnectionManager().shutdown(); 
 		}
         
         // Get back:
 		// 300 (TCN: list with multiple Mementos to choose from)
 		// or 302 (TCN: choice) 
 		// or 404 (no Mementos for this URL)
     	// or 406 (TCN: list with only first and last Mementos)
 		
 		int statusCode = response.getStatusLine().getStatusCode(); 
 		if (statusCode == 300) {
 			// TODO: Implement.  Right now the lanl proxy doesn't appear to be returning this
 			// code, so let's just ignore it for now.
 			log.debug("Pick a URL from list");			
 		}
 		else if (statusCode == 302) {
 			// Send browser to Location URL
 			// Note that the date/time of this memento is not given in the Location.
 			
 			Header[] headers = response.getHeaders("Location");
 			if (headers.length == 0) {
 				mErrorMessage = "Sorry, but there was an unexpected error that will " +
 					"prevent the Memento from being displayed. Try again in 5 minutes.";
 				log.error("Error: Location header not found in response headers.");
 			}
 			else {
 				String redirectUrl = headers[0].getValue();
 				
 				// Find out the datetime of this resource
 				/*SimpleDateTime d = getResourceDatetime(redirectUrl);
 				if (d != null)
 					mDateDisplayed = d;
 					*/
 				
 				log.debug("Sending browser to " + redirectUrl);
 									
 				// We can't update the view directly since we're running
 				// in a thread, so use mUpdateResults to show a toast message
 				// if accessing a different date than what was requested.
 				
 				//mHandler.post(mUpdateResults);
 				
 				// Parse various Links
 				headers = response.getHeaders("Link");
 				if (headers.length == 0) {
 					log.error("Error: Link header not found in response headers.");
 					mErrorMessage = "Sorry, but the Memento could not be accessed. Try again in 5 minutes.";
 				}
 				else {
 					String linkValue = headers[0].getValue();
 											
 					mTimeMap = null;
 			    	mTimeBundle = null;
 			    	
 			    	// Get the datetime of this mememnto which should be supplied in the
 			    	// Link: headers
 			    	mDateDisplayed = parseCsvLinks(initUrl, linkValue);
 					
 					// Now that we know the date, update the UI to reflect it
 			    	
 					if (mTimeMap != null)
 						if (!accessTimeMap(initUrl))
 							mErrorMessage = "There were problems accessing the Memento's TimeMap.";
 				}
 			}
 		}		
 		else if (statusCode == 404) {
 			mErrorMessage = "Sorry, but there are no Mementos for this URL.";
 		}
 		else if (statusCode == 406) {
 														
 			// Parse various Links
 			Header[] headers = response.getHeaders("Link");
 			
 			if (headers.length == 0) {
 				log.debug("Error: Link header not found in 406 response headers.");
 				//mErrorMessage = "Sorry, but there was an error in retreiving this Memento.";
 				
 				// The lanl proxy has it wrong.  It should return 404 when the URL is not
 				// present, so we'll just pretend this is a 404.
 				mErrorMessage = "Sorry, but there are no Mementos for this URL.";
 				
 				//log.debug("BODY: " + EntityUtils.toString(response.getEntity());							
 			}
 			else {
 				String linkValue = headers[0].getValue();
 				
 				mTimeMap = null;
 		    	mTimeBundle = null;
 		    	
 				parseCsvLinks(initUrl,linkValue);		
 		    	
 				if (mTimeMap != null)
 					accessTimeMap(initUrl);
 				
 				if (mFirstMemento == null || mLastMemento == null) {
 					log.error("Could not find first or last Memento in 406 response for " + url);
 					mErrorMessage = "Sorry, but there was an error in retreiving this Memento.";
 				}
 				else {			
 					log.debug("Not available in this date range (" + mFirstMemento.getDateTimeSimple() +
 							" to " + mLastMemento.getDateTimeSimple() + ")");
 					
 					// According to Rob Sanderson (LANL), we will only get 406 when the date is too
 					// early, so redirect to first Memento
 										
 					mDateDisplayed = new SimpleDateTime(mFirstMemento.getDateTime());
 					String redirectUrl = mFirstMemento.getUrl();
 					log.debug("Sending browser to " + redirectUrl);
 				}
 			}
 		}
 		else {
 			mErrorMessage = "Sorry, but there was an unexpected error that will " +
 				"prevent the Memento from being displayed. Try again in 5 minutes.";
			log.error("While GETting URL: "+url);
 			log.error("Unexpected response code in makeHttpRequests = " + statusCode);
 		}
 		
 		// Report error message as a log message:
 		if( mErrorMessage != null ) {
 			log.error(mErrorMessage);
 			this.mErrorMessage = mErrorMessage;
 		}
     }
     
     
     /**
      * Parse the links in CSV format and return the date of the last item with rel="memento" since
      * this information is needed when getting a 302 and needing to find the resource's datetime.
      * 
      * Example data:
      * 	 <http://mementoproxy.lanl.gov/aggr/timebundle/http://www.harding.edu/fmccown/>;rel="timebundle",
      * 	 <http://www.harding.edu/fmccown/>;rel="original",
      * 	 <http://web.archive.org/web/20010724154504/www.harding.edu/fmccown/>;rel="first memento";datetime="Tue, 24 Jul 2001 15:45:04 GMT",
      * 	 <http://web.archive.org/web/20010910203350/www.harding.edu/fmccown/>;rel="memento";datetime="Mon, 10 Sep 2001 20:33:50 GMT",
      * 
      * Another example:
      *   <http://mementoproxy.lanl.gov/google/timebundle/http://www.digitalpreservation.gov/>;rel="timebundle",
      *   <http://www.digitalpreservation.gov/>;rel="original",
      *   <http://mementoproxy.lanl.gov/google/timemap/link/http://www.digitalpreservation.gov/>;rel="timemap";type="application/link-format",
      *   <http://webcache.googleusercontent.com/search?q=cache:http://www.digitalpreservation.gov/>;rel="first last memento";datetime="Tue, 07 Sep 2010 11:54:29 GMT"
      *   
      * @param links
      * @return The datetime of the last item marked rel="memento"
      */
     private SimpleDateTime parseCsvLinks(String initUri, String links) {
     	if( log.isDebugEnabled() ) {
     		if( links.length() > 200 ) {
         		log.debug("Parsing: "+links.substring(0,200));
     		} else {
         		log.debug("Parsing: "+links);
     		}
     	}
     	if (mMementos != null ) mMementos.clear();
     	mFirstMemento = null;
     	mLastMemento = null;
     	mTimeMap = null;
     	
     	SimpleDateTime date = null;
     	
     	// Use a temporary list instead of the actual mMemento list so that we don't 
     	// show a list of available dates until they have all been parsed.
     	MementoList tempList = new MementoList();
     	
     	log.debug("Start parsing links");
 		String[] linkStrings = links.split("\",");
 				
     	// Place all Links into the array and then sort it based on date
     	for (String linkStr : linkStrings) {
 			    		
 			// Add back "
 			if (!linkStr.endsWith("\""))
 				linkStr += "\"";
 			
 			//log.debug(linkStr);	
 			
 			linkStr = linkStr.trim();
 			
 			Link link = new Link(linkStr);
 			String rel = link.getRel();
			if (rel == null) continue;
 			if (rel.contains("memento")) {
 				Memento m = new Memento(link);
 				tempList.add(m);
 				
 				//log.debug("Added memento " + m.toString());
 				
 				// Peel out all values in rel which are separated by white space
 				String[] items = link.getRelArray();
 				for (String r : items) {						
 					r = r.toLowerCase();
 					
 					//log.debug("Processing rel [" + r + "]");
 					
 					// Change the Showing date to the memento's date
 					//if (link.mRel.equals("first-memento"))
 					if (r.contains("first")) {
 						mFirstMemento = m;
 					}
 					if (r.contains("last")) {
 						mLastMemento = m;
 					}
 					if (r.equals("memento")) {
 						date = link.getDatetime();
 					}
 				}					
 			}
 			else if (rel.equals("timemap")) {
 				mTimeMap = new TimeMap(link);
 			}
 			else if (rel.equals("timebundle")) {
 				mTimeBundle = new TimeBundle(link);
 			}
 		}
     	
 		log.debug("Finished parsing, found " + tempList.size() + " links");
 		
 		mMementos = tempList;
 				
 		if (date != null)
 			log.debug("parseCsvLinks returning " + date.toString());
 		else
 			log.debug("parseCsvLinks returning null");
 		
 		return date;
     }
         
 
     /**
      * Change IA URLs back to their original.
      * 
      * Example of IA URLs: 
      * 
      * http://www.foo.org.wstub.archive.org/links.html
      * http://web.archive.org/web/20071222090517/http://www.foo.org/
      * http://web.archive.org/web/20070127071850rn_1/www.harding.edu/USER/fmccown/WWW/
      */
     private String convertIaUrlBack(String iaUrl) {
     	String url = iaUrl;
     	
     	url = url.replace(".wstub.archive.org", "");
     	
     	String pattern = "^http://web.archive.org/.+\\d{14}.*?/";
     	
 		// Create a Pattern object
 		Pattern r = Pattern.compile(pattern);
 
 		// Now create matcher object.
 		Matcher m = r.matcher(iaUrl);
 		if (m.find()) {
 			log.info("Found value: " + m.group(0));
 			url = m.replaceFirst("");
 		}
 		
 		if (!url.startsWith("http://") && !url.startsWith("https://"))
 			url = "http://" + url;
 		
 		return url;
     }
     
     /**
      * Retrieve the TimeMap from the Web and parse out the Mementos.
      * Currently this only recognizes TimeMaps using CSV formats. 
      * Other formats to be implemented: RDF/XML, N3, and HTML.
      * @return true if TimeMap was successfully retreived, false otherwise.
      */
     private boolean accessTimeMap( String initUrl ) {    	   	
         
     	HttpClient httpclient = getHttpClient();
     	
     	String url = mTimeMap.getUrl();
         HttpGet httpget = new HttpGet(url);
         httpget.setHeader("User-Agent", mUserAgent);
                 
         log.debug("Accessing TimeMap: " + httpget.getURI());
 
         HttpResponse response = null;
 		try {			
 			response = httpclient.execute(httpget);
 			
 			log.debug("Response code = " + response.getStatusLine());
 			
 			//log.debug(getHeadersAsString(response.getAllHeaders()));
 		} catch (ClientProtocolException e) {
 			log.error(getExceptionStackTraceAsString(e));
 			return false;
 		} catch (IOException e) {
 			log.error(getExceptionStackTraceAsString(e));
 			return false;
 		}                
         
         // Should get back 200
 		
 		int statusCode = response.getStatusLine().getStatusCode(); 
 		if (statusCode == 200) {
 			
 			// See if MIME type is the same as Type		
 			Header type = response.getFirstHeader("Content-Type");
 			if (type == null)
 				log.warn("Could not find the Content-Type for " + url);
 			else if (!type.getValue().contains(mTimeMap.getType()))
 				log.warn("Content-Type is [" + type.getValue() + "] but TimeMap type is [" +
 						mTimeMap.getType() + "] for " + url);
 			
 			// Timemap MUST be "application/link-format", but leave csv for
 			// backwards-compatibility with earlier Memento implementations
 			if (mTimeMap.getType().equals("text/csv") ||
 				mTimeMap.getType().equals("application/link-format")) {
 				try {
 					String responseBody = EntityUtils.toString(response.getEntity());
 					parseCsvLinks(initUrl,responseBody);
 				} catch (ParseException e) {
 					log.error(getExceptionStackTraceAsString(e));
 					return false;
 				} catch (IOException e) {
 					log.error(getExceptionStackTraceAsString(e));
 					return false;
 				}
 			}
 			else {
 				log.error("Unable to handle TimeMap type " + mTimeMap.getType());
 				return false;
 			}
 		}		
 		else {
 			log.debug("Unexpected response code in accessTimeMap = " + statusCode);
 			return false;
 		}        
         
 		// Deallocate all system resources
         httpclient.getConnectionManager().shutdown();
         
         return true;
     }
     
     public static String getExceptionStackTraceAsString(Exception exception) {
     	StringWriter sw = new StringWriter();
     	exception.printStackTrace(new PrintWriter(sw));
     	return sw.toString();
     }
     
     /**
      * Purely for testing.
      */
     @SuppressWarnings("unused")
 	private void testMementos() {
     	
     	String[] urls = {
     			"http://www.foo.org.wstub.archive.org/links.html",
     			"http://web.archive.org/web/20071222090517/http://www.foo.org/",
     			"http://web.archive.org/web/20070127071850rn_1/www.harding.edu/USER/fmccown/WWW/"
     	};
     	
     	for (String u : urls) {
     		System.out.println("Convert " + u + " to " + convertIaUrlBack(u));
     	}
     	    		
     	SimpleDateTime date = new SimpleDateTime();
     	System.out.println("date = " + date);
     	
     	SimpleDateTime date2 = new SimpleDateTime();
     	System.out.println("date2 = " + date2);
     	
     	int comp = date.compareTo(date2);
     	System.out.println("compareTo = " + comp);
     	
     	date = new SimpleDateTime(31, 12, 2010);
     	System.out.println("date formatted = " + date.dateFormatted());
     	System.out.println("date and time formatted = " + date.dateAndTimeFormatted());
     	System.out.println("long formatted = " + date.longDateFormatted());
     	
     	
     	//System.exit(0);
     	//this.finish();
     	
     	String url = "<http://web.archive.org/web/20010910203350/www.harding.edu/fmccown/>;rel=\"memento\";datetime=\"Mon, 10 Sep 2001 20:33:50 GMT\"";
     	Memento m1 = new Memento(new Link(url));
     	System.out.println("m1=" + m1.toString());
     	System.out.println("getDateTimeString: " + m1.getDateTimeString());
     	System.out.println("getDateTimeSimple: " + m1.getDateTimeSimple());
     	
     	Memento m2 = new Memento(new Link(url));
     	
     	System.out.println("m2=" + m2.toString());
     	System.out.println("getDateTimeString: " + m2.getDateTimeString());
     	System.out.println("getDateTimeSimple: " + m2.getDateTimeSimple());
     	
     	System.out.println("\ncompare m1,m2: " + m1.compareTo(m2));
     	System.out.println("\ncompare m2,m1: " + m2.compareTo(m1));
     	
     	String newDatetime = "Sun, 09 Sep 2001 20:33:50 GMT";
     	m2.setDateTime(newDatetime);
     	
     	System.out.println("getDateTimeString: " + m2.getDateTimeString());
     	System.out.println("getDateTimeSimple: " + m2.getDateTimeSimple());
     	
     	System.out.println("\ncompare m1,m2: " + m1.compareTo(m2));
     	System.out.println("\ncompare m2,m1: " + m2.compareTo(m1));
     	
     	m2.getDateTime().setToLastHour();
     	
     	System.out.println("New value for getDateTimeString: " + m2.getDateTimeString());
     	    	
     	    	
     	String links = 
     		"<http://mementoproxy.lanl.gov/aggr/timebundle/http://www.harding.edu/fmccown/>;rel=\"timebundle\"," +
     		"<http://www.harding.edu/fmccown/>;rel=\"original\",<http://mementoproxy.lanl.gov/aggr/timemap/link/http://www.harding.edu/fmccown/>;rel=\"timemap\";type=\"text/csv\"," +
     		"<http://web.archive.org/web/20010724154504/www.harding.edu/fmccown/>;rel=\"first prev memento\";datetime=\"Tue, 24 Jul 2001 15:45:04 GMT\"," +
     		"<http://web.archive.org/web/20071222090517/www.harding.edu/fmccown/>;rel=\"last memento\";datetime=\"Sat, 22 Dec 2007 09:05:17 GMT\"," +
     		"<http://web.archive.org/web/20020104194811/www.harding.edu/fmccown/>;rel=\"next memento\";datetime=\"Fri, 04 Jan 2002 19:48:11 GMT\"," +
     		"<http://web.archive.org/web/20010910203350/www.harding.edu/fmccown/>;rel=\"memento\";datetime=\"Mon, 10 Sep 2001 20:33:50 GMT\"," + 
     		"<http://webcache.googleusercontent.com/search?q=cache:http://www.digitalpreservation.gov/>;rel=\"first last memento\";datetime=\"Tue, 07 Sep 2010 11:54:29 GMT\"";
     	
     	parseCsvLinks(url,links);
     	mMementos.displayAll();
     	
     	System.exit(0);
     	
     	System.out.println(mTimeMap.toString());
     	System.out.println(mTimeBundle.toString());
     	
     	System.out.println("\nAll years:");
     	for (CharSequence year : mMementos.getAllYears()) {
     		System.out.println(year);
     	}
     	
     	System.out.println("\nAll for 2001:");
     	for (CharSequence year : mMementos.getDatesForYear(2001)) {
     		System.out.println(year);
     	}
     	
 
     	System.out.println("\nAll for 2000:");
     	for (CharSequence year : mMementos.getDatesForYear(2000)) {
     		System.out.println(year);
     	}
     	
     	//date = getResourceDatetimeForWebcite("http://webcitation.org/query?id=1218127693715930");
     	//System.out.println("Date returned from getResourceDatetimeForWebcite: " + date.toString());
     	
     	//accessTimeMap();
     }
     
     public void setTargetURI( String target ) {
     	this.makeHttpRequests( target );
     }
     
     public MementoList getMementos() {
     	return this.mMementos;
     }
     
     /**
      * @return null if all is well.
      */
     public String getErrorMessage() {
     	if( this.mErrorMessage == null ) return null;
     	return this.mErrorMessage.toString();
     }
     
     /**
      * Command-line utility to take a URL and look up who holds archived copies (Mementos)
      * @param args
      * @throws URISyntaxException 
      */
     public static void main( String[] args ) throws URISyntaxException {
     	String query = "http://www.bl.uk";
     	if( args.length > 0 ) {
     		query = args[0];
     	}
     	System.out.println("Looking for: "+query);
     	// Query:
     	MementoClient mc = new MementoClient();
     	mc.setTargetURI(query);
     	// Get results:
     	mc.getMementos().displayAll();
     	
     }
}
