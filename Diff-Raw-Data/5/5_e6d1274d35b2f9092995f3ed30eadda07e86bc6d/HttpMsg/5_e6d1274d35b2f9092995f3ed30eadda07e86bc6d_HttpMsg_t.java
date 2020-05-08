 // The Grinder
 // Copyright (C) 2000  Paco Gomez
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.plugin.http;
 
 import java.io.*;
 import java.net.*;
 import java.util.Hashtable;
 
 /**
  * Util class for sending HTTP requests.
  *
  * Wrap up HTTP requests, cache a cookie across a number of calls,
  * simulate a browser cache.
  * 
  * @author Paco Gomez
  * @author Philip Aston
  * @version $Revision$
  */
 public class HttpMsg{
  
     public HttpMsg(boolean useCookie) {
 	_useCookie = useCookie;
 	reset();
     }
 
     public String sendRequest(HttpRequestData requestData)
 	throws java.io.IOException
     {
 	// do not forget to include in the the classpath
 	// the following class:
 	//  sun.net.www.protocol 
 	// it is included in c:\jdk1.1.7B\lib\classes.zip
 
 	URL url = null;
 	String urlString = requestData.getURLString();
 
 	try {
 	    url = new URL(urlString);
 	}
 	catch (MalformedURLException e) {
 	    // Maybe it was a relative URL.
 	    final URL contextURL = new URL(requestData.getContextURLString());
 
 	    url = new URL(contextURL, urlString); // Let this one fail.
 	}
 
 	urlString = url.toString();
 
 	final String postString = requestData.getPostString();
 
 	HttpURLConnection connection = 
 	    (HttpURLConnection) url.openConnection();

	// Stop URLConnection from handling http 302 forwards, because
	// these contain the cookie when handling web app form based
	// authentication.
	connection.setInstanceFollowRedirects(false);
             
 	if (_useCookie) {
 	    connection.setRequestProperty("Cookie", _cookie);
 	}
 
 	connection.setUseCaches(false);
 	
 	if (postString != null) {
 	    connection.setRequestMethod("POST");
 	    connection.setDoOutput(true);
 
 	    final BufferedOutputStream bos = 
 		new BufferedOutputStream(connection.getOutputStream());
 	    final PrintWriter out = new PrintWriter(bos);
 
 	    out.write(postString);
 	    out.close();
 	}
 	
 	connection.connect();
 
 	if (_useCookie) {
 	    final String s = connection.getHeaderField("Set-Cookie");
 	    if (s != null) {
 		_cookie = s;
 	    }
 	}
             
 	// Slurp the response into a StringWriter.
 	final InputStreamReader isr =
 	    new InputStreamReader(connection.getInputStream());
 	final BufferedReader in = new BufferedReader(isr);
 
 
 	// Default StringWriter buffer size is usually 16 which is way small.
 	final StringWriter stringWriter = new StringWriter(512);
 
 	char[] buffer = new char[512];
 	int charsRead = 0;
 
 	while ((charsRead = in.read(buffer, 0, buffer.length)) > 0) {
 	    stringWriter.write(buffer, 0, charsRead);
 	}    
 
 	in.close();
 	stringWriter.close();
 
 	final String result = stringWriter.toString();
 
 	return result;
     }
 
     public void reset(){
         _cookie = "";
     }
 
     private boolean _useCookie;
     private String _cookie;
 }
