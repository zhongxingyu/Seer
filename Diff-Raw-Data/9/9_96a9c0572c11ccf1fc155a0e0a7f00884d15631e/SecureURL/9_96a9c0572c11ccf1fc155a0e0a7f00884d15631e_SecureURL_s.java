 package com.zrd.zr.pnj;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import android.util.Log;
 
 public class SecureURL {
 
 	public String phpMd5(String str) {
 		char[] charArray = str.toCharArray();
 		 
 	    byte[] byteArray = new byte[charArray.length];
 	 
 	    for (int i=0; i<charArray.length; i++)
 	       byteArray[i] = (byte) charArray[i];
 	 
 		MessageDigest md;
 		try {
 			md = MessageDigest.getInstance("MD5");
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 	    byte[] md5Bytes = md.digest(byteArray);
 
 	    StringBuffer hexValue = new StringBuffer();
 
 	    for (int i=0; i<md5Bytes.length; i++)
 	    {
 	       int val = ((int) md5Bytes[i] ) & 0xff; 
 	       if (val < 16) hexValue.append("0");
 	       hexValue.append(Integer.toHexString(val));
 	    }
 	    return hexValue.toString();
 	}
 	
 	public URLConnection getConnection(URL url) {
 		if (url == null) return null;
 		URLConnection conn = null;
 		try {
 			conn = url.openConnection();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			Log.e("DEBUGTAG", "Remtoe Image Exception", e);
 			e.printStackTrace();
 			return null;
 		}
 		try {
 			conn.connect();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			Log.e("DEBUGTAG", "Remtoe Image Exception", e);
 			e.printStackTrace();
 			return null;
 		}
 		
 		return conn;
 	}
 	
 	public URLConnection getConnection(String sUrl) {		
 		URL url = null;
 		try {
 			url = new URL(sUrl);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
			Log.e("DEBUGTAG", "Remtoe Image Exception", e);
 			e.printStackTrace();
 			return null;
 		}
 		
 		return getConnection(url);
 	}
 }
