 package ru.xrm.app.httpclient;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 
 public class UrlHelper {
 
 	MessageDigest md;
 	static UrlHelper instance;
 	
 	public static synchronized UrlHelper getInstance(){
 		if (instance==null){
 			instance=new UrlHelper();
 		}
 		return instance;
 	}
 	
 	private UrlHelper(){
 		try {
 			md=MessageDigest.getInstance("MD5");
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public synchronized String encode(String url){
 		md.update(url.getBytes());
 		byte[] digest=md.digest();
 		StringBuilder sb=new StringBuilder();
 
 		for (int i=0;i<digest.length;i++){
			String hex=Integer.toHexString(0x00FF & (int)(digest[i]));
 			
 			if (hex.length()==1){
 				sb.append('0');
 			}
 			sb.append(hex);
 		}
 		return sb.toString();
 	}
 	
 	public static String getBasename(String address){
 		String result="";
 		try {
 			URL url=new URL(address);
 			result=url.getProtocol()+"://"+url.getHost();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 	
 	public static String constructAbsoluteUrl(String address, String basename){
 		// relative url
 		if (address.charAt(0)=='/'){
 			return basename+address;
 		}else{
 			return address;
 		}
 	}
 }
