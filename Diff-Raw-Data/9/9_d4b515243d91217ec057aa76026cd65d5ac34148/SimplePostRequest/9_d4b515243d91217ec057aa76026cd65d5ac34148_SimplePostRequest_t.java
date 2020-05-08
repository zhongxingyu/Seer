 package com.tracktopell.util;
 
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 
 public class SimplePostRequest {
 
 	private URL url;
 	private String data;
 
 	public SimplePostRequest(String url) throws MalformedURLException {		
 		this.url = new URL(url);
 		data = "";
 	}
 
 	public void add(String parameter, String value) throws UnsupportedEncodingException {
 		if (data.length() > 0) {
 			data += "&" + URLEncoder.encode(parameter, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");
 		} else {
 			data += URLEncoder.encode(parameter, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");
 		}
 	}
 
 	public String getGETFullResponseAsString() throws IOException {
 		String respuesta = "";
 		
 		String fullURL = url.toString()+"?"+data;		
 		
		//System.err.println("->getGETFullResponseAsString : fullURL ->"+fullURL+"<-");
 		
 		this.url = new URL(fullURL);		
 		
 		URLConnection conn = url.openConnection();		
 		
 		//obtenemos el flujo de lectura		
 		InputStream is = conn.getInputStream();
 		byte[] buffer= new byte[1024];
 		int r;
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		while(( r = is.read(buffer, 0, buffer.length)) != -1) {
 			baos.write(buffer, 0, r);
 		}
 		is.close();
 		baos.close();
 		
 		respuesta = new String(baos.toByteArray(),"UTF-8");		
 		return respuesta;
 	}
 	
 	public String getPOSTFullResponseAsString() throws IOException {
 		String respuesta = "";
 		
 		URLConnection conn = url.openConnection();
 		//especificamos que vamos a escribir
 		conn.setDoOutput(true);
 		
 		//conn.setRequestProperty("", "");
 		
 		//obtenemos el flujo de escritura
 		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
 		//System.err.println("data: ->"+data+"<-");
 		//escribimos
 		wr.write(data);
 		
 		wr.close();
 		InputStream is = conn.getInputStream();
 		byte[] buffer= new byte[1024];
 		int r;
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		while(( r = is.read(buffer, 0, buffer.length)) != -1) {
 			baos.write(buffer, 0, r);
 		}
 		is.close();
 		baos.close();
 		
 		respuesta = new String(baos.toByteArray(),"UTF-8");		
 
 		return respuesta;
 	}
 }
