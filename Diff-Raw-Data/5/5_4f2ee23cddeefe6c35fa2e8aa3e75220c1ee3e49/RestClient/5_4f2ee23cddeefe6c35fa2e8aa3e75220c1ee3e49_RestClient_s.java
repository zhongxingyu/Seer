 package com.quasma.android.bustrip.rest;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 
 import com.quasma.android.bustrip.R;
 
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 
 public class RestClient 
 {
 	private Context context; 
 	
 	public RestClient(Context context)
 	{
 		this.context = context;
 	}
 	public Response execute(Request request) 
 	{
 		ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
 		NetworkInfo info = conMgr.getActiveNetworkInfo();  
 		if(info !=  null && info.isConnected()) 
 		{
     		HttpURLConnection conn = null;
     		try 
     		{
     			URL url = request.getRequestUri().toURL();
     			conn = (HttpURLConnection) url.openConnection();
     			if (request.getHeaders() != null) 
     			{
     				for (String header : request.getHeaders().keySet()) 
     				{
     					for (String value : request.getHeaders().get(header)) 
     					{
     						conn.addRequestProperty(header, value);
     					}
     				}
     			}
    			Object content = conn.getContent();
    
    			if (content instanceof InputStream) 
     			{
     				BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
     				byte[] body = readStream(in);
     				return new Response(conn.getURL(), conn.getResponseCode(), conn.getResponseMessage(), conn.getHeaderFields(), body);
     			} 
     			else 
     			{
     				return new Response(conn.getURL(), conn.getResponseCode(), conn.getResponseMessage(), conn.getHeaderFields(), new byte[] {});
     			}
     
     		} 
     		catch (IOException e) 
     		{
     			e.printStackTrace();
     			return new Response(conn.getURL(), 600, e.getMessage(), conn.getHeaderFields(), new byte[] {}); 
     		} 
     		finally 
     		{
     			if (conn != null)
     				conn.disconnect();
     		}
 		}
 		else
 		{
 			return new Response(null, Response.NO_CONNECTION, context.getString(R.string.noconnection), new HashMap<String, List<String>>(), new byte[] {});
 		}
 	}
 
 	private static byte[] readStream(InputStream in) throws IOException 
 	{
 		byte[] buf = new byte[1024];
 		int count = 0;
 		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
 		while ((count = in.read(buf)) != -1)
 			out.write(buf, 0, count);
 		return out.toByteArray();
 	}
 }
