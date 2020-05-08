 package org.concord.otrunk.handlers.jres;
 
import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 
 public class JResConnection extends java.net.URLConnection
 {
 	
 	protected JResConnection(URL url)
     {
 	    super(url);
     }
 
 	@Override
     public void connect()
         throws IOException
     {
 		connected = true;		// do we need to do something more interesting here?
     }
 	
 	public java.io.InputStream getInputStream()
     		throws java.io.IOException {
       
       URL resourceURL = this.getClass().getResource(getURL().getFile());
      if(resourceURL == null) {
    	  throw new FileNotFoundException(getURL().toString());
      }
       return resourceURL.openStream();
     }
 
 
 }
