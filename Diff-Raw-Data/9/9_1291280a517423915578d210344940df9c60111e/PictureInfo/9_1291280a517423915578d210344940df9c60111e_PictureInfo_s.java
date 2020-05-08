 package edu.nouri.photoReX.picture;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import com.google.gson.Gson;
 
 /*
  * This class represents a picture that the client will see. It only contains information that the client needs 
  * to retrieve the actual picture from the corresponding website. 
  */
 
 public abstract class PictureInfo {
 
 	public static MessageDigest sha1; 
 	
 	public String website;
 	public String hash; 
 	public boolean isViewed = false; 
 	public boolean isVisited = false; 
 	
 	PictureInfo(String w)
 	{
 		if (sha1==null)
 		{
 			try
 			{
 				sha1 = MessageDigest.getInstance("SHA1"); 
 			}catch(NoSuchAlgorithmException e)
 			{
 				
 			}
 		}
 		
 		
 		website = w; 
 	}
 	
 	//each subclass should implement their own
 	public String toString()
 	{
 		return website + ":incomplete"; 
 	}
 	
 	public String toHash()
 	{
 		try {
 			String encode = this.toString(); 
			byte[] digest = sha1.digest(encode.getBytes("UTF-8"));
 			
 			StringBuffer result = new StringBuffer(); 
 			for(int i=0; i< digest.length; i++)
 			{
 				result.append(Integer.toHexString(0xFF & digest[i])); 
 			}
 			return result.toString(); 
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return "";  
 		} 
 	}
 
 	//remember to call toHash before calling this so that the hash field is generated 
 	public String toJson()
 	{
 		Gson gson = new Gson(); 
 		String result = gson.toJson(this); 
 		return result; 
 	}
 
 }
