 package com.studentersamfundet.app;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import android.content.Context;
 
 public class FeedFetcher {
 	private final String LOCAL_FILENAME = "dns_events";
 	
 	private Context context;
 	private String url;
 	
 	public FeedFetcher(Context context, String url) {
 		this.context = context;
 		this.url = url;
 	}
 	
 	private InputStream openHttpConnection() throws IOException {
 		InputStream in = null;
 		int response = -1;
 		URL url = new URL(this.url);
 		URLConnection con = url.openConnection();
 		
 		if (!(con instanceof HttpURLConnection))       
 			throw new IOException("Not an HTTP connection!");
 		
 		HttpURLConnection httpCon = (HttpURLConnection) con;
 		httpCon.setAllowUserInteraction(false);
 		httpCon.setInstanceFollowRedirects(true);
 		httpCon.setRequestMethod("GET");
 		httpCon.connect();
 		
 		response = httpCon.getResponseCode();
 		
 		if (response == HttpURLConnection.HTTP_OK)
 			in = httpCon.getInputStream();
 		
 		return in;
 	}
 	
 	/**
 	 * Fetches list of events from the web.
 	 * @param forcedUpdate specifies if we HAVE TO update local file, or let 
 	 * the function decide.
 	 * @return Returns list of events as a XML NodeList
 	 * @throws IOException Is thrown if the app cannot download the file from
 	 * the internet and there is no local copy.
 	 */
 	public NodeList fetch(boolean forcedUpdate) throws IOException {
 	       InputStream in = null;
 	       NodeList itemNodes = null;
 	       
         	/* Update the local file if possible. */
         	try {
         		if (forcedUpdate || doesFileNeedUpdate(context)) {
 	        		in = openHttpConnection();
 	            	saveFile(in, context);
         		}
             	
         	} catch(IOException e) {
         		/* OK, so we failed to update it. It doesn't matter yet, because we can try to... */
         	} finally {
         		if (in != null) 
         			in.close();
         	}
         	
     		/* ...load local file: */
         	in = loadFile(context); // if we fail here, we fail ultimately. Throw the Exception! 
         	
             Document doc = null;
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             DocumentBuilder db;
             
             try {
                 db = dbf.newDocumentBuilder();
                 doc = db.parse(in);
             } catch (ParserConfigurationException e) {
             	/* This should never happen. */
             } catch (SAXException e) {
             	/* This shouldn't happen unless the xml returned by the server
             	 * is somehow invalid. If it happens, the best course of action 
             	 * is to pretend we failed to download the event list (it's 
             	 * close enough and it simplifies exception-handling). */
             	throw new IOException("Bad response from server.");
             }        
             
             doc.getDocumentElement().normalize(); 
             
             // Retrieve all the <item> nodes.
             itemNodes = doc.getElementsByTagName("item");
             in.close();
 
 	        return itemNodes;
 	}
 	
 	private boolean doesFileNeedUpdate(Context c) throws IOException {
 		/** Maximum amount of time before the file needs updating, in ms: */
 		final long maxInterval = 1000 * 60 * 60 * 24 * 7; // one week
 			
 		File file = c.getFileStreamPath(LOCAL_FILENAME);
 		if (! file.exists()) return true;
 		
 		long now = System.currentTimeMillis();
 		long lastMod = file.lastModified();
 		
 		return now - lastMod > maxInterval;
 	}
 	
 	private void saveFile(InputStream in, Context c) throws IOException, FileNotFoundException {
 		final int BUFFER_SIZE = 128;
 		
 		OutputStream os = c.openFileOutput(LOCAL_FILENAME, Context.MODE_PRIVATE);
 		byte[] inputBuffer = new byte[BUFFER_SIZE];
 		
		int bytes = 0;
		while ((bytes = in.read(inputBuffer)) > 0) {
			os.write(inputBuffer, 0, bytes);
 			inputBuffer = new byte[BUFFER_SIZE];
 		}
 		os.close();
 	}
 	
 	private InputStream loadFile(Context c) throws FileNotFoundException {
 		return c.openFileInput(LOCAL_FILENAME);
 	}
 }
