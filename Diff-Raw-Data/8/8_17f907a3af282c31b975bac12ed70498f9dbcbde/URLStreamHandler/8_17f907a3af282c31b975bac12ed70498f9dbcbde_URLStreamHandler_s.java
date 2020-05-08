 package org.concord.otrunk.transfer;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.zip.GZIPInputStream;
 
 public class URLStreamHandler
 {
 	URLConnection connection;
 	URL url;
 	InputStream inStream;
 	
 	public URLStreamHandler(URL url)
 	{
 		this.url = url;
 	}
 	
 	public void printAndThrowURLError(String message, boolean printInStream, Exception e)
     	throws Exception
     {
     	HttpURLConnection httpConnection = null;
     	if (connection instanceof HttpURLConnection) {
     		httpConnection = (HttpURLConnection) connection;
     	}		
     	
     	System.err.println(message + ": " + connection.getURL());
    	if (httpConnection != null) {			
    		System.err.println("  Response code: " + httpConnection.getResponseCode()); 
     	}		
     
     	if (inStream != null) {
     		try {
     			System.err.println("  available bytes in input stream: " + inStream.available());
     		} catch (IOException e1){
     			// can't get the available bytes for some reason.
     		}
     	}
     	
     	Map headerFields = connection.getHeaderFields();
     	if(headerFields != null){
     		Set entrySet = headerFields.entrySet();
     		Iterator iterator = entrySet.iterator();
     		while(iterator.hasNext()){
     			Entry entry = (Entry) iterator.next();
     			System.err.println("  " + entry.getKey() + ": " + entry.getValue());
     		}
     	}
     
     	try {
     
     		if(httpConnection != null){
     			InputStream errorStream = httpConnection.getErrorStream();
     			StreamUtil.printFromStream("Error Message", errorStream);
     		}
     
     		if(printInStream){
     			if(inStream == null){
     				// Open the connection stream if it hasn't been passed in
     				inStream = connection.getInputStream();
     			}
     			StreamUtil.printFromStream("Response Body", inStream);
     		}
     	} catch (Throwable t){
     		// Don't throw errors caused by trying to print out errors
     		System.err.println("  Error printing url input stream");
     	}
     	
     	throw new Exception("Error loading url", e);
     }
 
 	public InputStream getURLStream()
         throws Exception
     {
         try {
         	connection = url.openConnection();
         } catch (IOException e){
         	throw new Exception("Error loading url: " + url.toExternalForm(), e);
         }
         
     	try {
     		connection.setRequestProperty("Accept-Encoding", "gzip");
     		connection.connect();
     	} catch (IOException e) {
     		printAndThrowURLError("Error connecting to", false, e);
     	}
     
     	// check if this is a valid response
     	if (connection instanceof HttpURLConnection) {
     		int responseCode = ((HttpURLConnection)connection).getResponseCode();
     		if((responseCode / 100) != 2) {
     			printAndThrowURLError("Server returned error for", true, null);
     		}
     	}					
     
     	try {
     		inStream = connection.getInputStream();			
 
     		String encoding = connection.getContentEncoding();
     		if(encoding != null && encoding.toLowerCase().equals("gzip")){
     			inStream = new GZIPInputStream(inStream);
     		}
     	} catch (IOException e){
     		printAndThrowURLError("Error opening stream for", false, e);
     	}
         return inStream;
     }
 
 }
