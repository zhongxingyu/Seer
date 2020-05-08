 package com.mobidevday.demo;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Base64;
 import com.mobidevday.demo.ntlm.NTLMSchemeFactory;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.NTCredentials;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.conn.util.InetAddressUtils;
 import org.apache.http.impl.client.AbstractHttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.commons.io.IOUtils;
 
 import java.io.IOException;
 import java.io.InputStream;

 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.Enumeration;
 
 public class WebHelper {
     AbstractHttpClient mClient = new DefaultHttpClient();
 
 
     public static boolean isOnline(Context ctx){
         ConnectivityManager manager =  (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo info = manager.getActiveNetworkInfo();
         return info != null && info.isConnected();
     }
 
     public String getHttp(String url, String userName, String password, String domain) throws IOException {
 
         String deviceIP = getLocalIpAddress();
 
         mClient.getAuthSchemes().register("ntlm", new NTLMSchemeFactory());
         mClient.getCredentialsProvider().setCredentials(new AuthScope("192.168.0.33", -1),
                 new NTCredentials(userName, password, deviceIP, domain));
 
         //Set up the HTTP calls
         HttpGet request = new HttpGet(url);
 
         HttpResponse response = mClient.execute(request);
 
         //Get the data from the body of the response
         InputStream stream = response.getEntity().getContent();
      	byte byteArray[] = IOUtils.toByteArray(stream);
      	String json = new String( byteArray );
      	stream.close();
         return json;
     }
 
     public String getHttp(String url, String cookie) throws IOException {
 
         //Set up the HTTP calls
         HttpGet request = new HttpGet(url);
         request.addHeader("Cookie", cookie);
 
         HttpResponse response = mClient.execute(request);
 
         //Get the data from the body of the response
         InputStream stream = response.getEntity().getContent();
      	byte byteArray[] = IOUtils.toByteArray(stream);
      	String json = new String( byteArray );
      	stream.close();
         return json;
     }
 
     public String getHttp(String url) throws IOException {
 
         //Set up the HTTP calls
         HttpGet request = new HttpGet(url);
 
         HttpResponse response = mClient.execute(request);
 
         //Get the data from the body of the response
         InputStream stream = response.getEntity().getContent();
      	byte byteArray[] = IOUtils.toByteArray(stream);
      	String json = new String( byteArray );
      	stream.close();
         return json;
     }
 
//    public String getHttp(String url, String token) throws IOException {
//
//        //Set up the HTTP calls
//        HttpGet request = new HttpGet(url);
//        request.addHeader("Authorization", String.format("Bearer {0}", token));
//
//        HttpResponse response = mClient.execute(request);
//
//        //Get the data from the body of the response
//        InputStream stream = response.getEntity().getContent();
//     	byte byteArray[] = IOUtils.toByteArray(stream);
//     	String json = new String( byteArray );
//     	stream.close();
//        return json;
//    }

     public String getHttp(String url, String user, String password) throws IOException {

         //Set up the HTTP calls
         HttpGet request = new HttpGet(url);
 
         String basicHeader = createBasicHeader(user, password);
 
         request.addHeader("Authorization", basicHeader);
 
         HttpResponse response = mClient.execute(request);
 
         //Get the data from the body of the response
         InputStream stream = response.getEntity().getContent();
      	byte byteArray[] = IOUtils.toByteArray(stream);
      	String json = new String( byteArray );
      	stream.close();
         return json;
     }
 
     private String createBasicHeader(String userName, String password) {
        String combined = String.format("{0}:{1}", userName, password);
 
        String b64 =  Base64.encodeToString(combined.getBytes(), Base64.DEFAULT);
 
        return String.format("Basic {0}", b64);
     }
 
     public String getLocalIpAddress()
     {
     	String deviceIp = null;
     	boolean keepLookupOn = true;
 
     	try
     	{
     	    Enumeration availableNetwork = NetworkInterface.getNetworkInterfaces();
 
     	while( availableNetwork.hasMoreElements() && keepLookupOn )
     	{
     		NetworkInterface intf = (NetworkInterface) availableNetwork.nextElement();
     		Enumeration enumIpAddr = intf.getInetAddresses();
 
 
     		while( enumIpAddr.hasMoreElements() )
     		{
     			InetAddress inetAddress = (InetAddress)enumIpAddr.nextElement();
 
     			deviceIp = inetAddress.getHostAddress().toString();
 
     			if( !inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(deviceIp) )
     			{
     	            		keepLookupOn = false;
     	            		break;
     			}
     		}
     	}
     	}
     	catch (SocketException ex)
     	{
     	        ex.printStackTrace();
     	}
 
     	return deviceIp;
     }
 }
