 /***************************************************************
  *
  * Helper functions for DUBwise
  *                                                           
  * Author:        Marcus -LiGi- Bueschleb
  * Mailto:        LiGi @at@ LiGi DOTT de                    
  * 
  ***************************************************************/
 
 
 import java.io.*;
 import javax.microedition.io.*;
 
 public final class InstallHelper
 {
     public final static String get_http_string(String url) 
     {
 	
 	try {
 
 	    InputStream stream = null;
 	    StringBuffer buff = new StringBuffer();
 	    StreamConnection conn=null;
 	    
 	    System.out.println("starting conn");
 	    conn = (StreamConnection)Connector.open(url);
 	    stream = conn.openInputStream();
 	    int ch;
 	    
 	    while((ch = stream.read()) != -1) 
 		    buff.append((char) ch);
 	
 	    if(stream != null) 
 		stream.close();
 	    
 	    if(conn != null) 
 		conn.close();
 	    
 	    
 	    return buff.toString();
 	    
 	}
 	catch ( Exception e)
 	    {
 		return "err";
 	    }
 	
     } 
 
 
     public final static String post_http(String url,String params) {
 
 
     HttpConnection httpConn = null;
     InputStream is = null;
     OutputStream os = null;
     StringBuffer sb =null;
 
     try {
       // Open an HTTP Connection object
       httpConn = (HttpConnection)Connector.open(url);
       // Setup HTTP Request to POST
       httpConn.setRequestMethod(HttpConnection.POST);
 
       //      httpConn.setRequestProperty("User-Agent",        "Profile/MIDP-1.0 Confirguration/CLDC-1.0");
       httpConn.setRequestProperty("Accept_Language","en-US");
       //Content-Type is must to pass parameters in POST Request
       httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 
       os = httpConn.openOutputStream();
 
       os.write(params.getBytes());
 
       /**Caution: os.flush() is controversial. It may create unexpected behavior
             on certain mobile devices. Try it out for your mobile device **/
 
       //os.flush();
 
       // Read Response from the Server
 
       sb = new StringBuffer();
       is = httpConn.openDataInputStream();
       int chr;
       while ((chr = is.read()) != -1)
         sb.append((char) chr);
 
      return sb.toString();
       } 
 
     catch (Exception e)
 	{}
     finally {
 	try {
 	    if(is!= null)
 		is.close();
 	    if(os != null)
 		os.close();
 	    if(httpConn != null)
 		httpConn.close();
 	}
 	catch (Exception e)
 	    {}
     }
 
    return "err";
     }
 
 
 
     static public String Conditional_URL_Encode(String sUrl,boolean condition)
     {
 	if (condition) 
 	    return URL_Encode(sUrl);
 	else
 	    return sUrl;
     }
 
     static public String URL_Encode(String sUrl)   
     {  
 	if (sUrl==null) return "";
          StringBuffer urlOK = new StringBuffer();  
          for(int i=0; i<sUrl.length(); i++)   
          {  
              char ch=sUrl.charAt(i);  
              switch(ch)  
              {  
 	     case '\n': urlOK.append("%0A"); break;  
 	     case '-': urlOK.append("%2D"); break;  
 	     case '<': urlOK.append("%3C"); break;  
 	     case '>': urlOK.append("%3E"); break;  
 	     case '/': urlOK.append("%2F"); break;  
 	     case ' ': urlOK.append("%20"); break;  
 	     case ':': urlOK.append("%3A"); break;  
 
                  default: urlOK.append(ch); break;  
              }   
          }  
          return urlOK.toString();  
      }  
 }
