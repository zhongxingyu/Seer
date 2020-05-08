 package pw.svn.util;
 
 import java.security.*;
 /**
  * Class for hashing messages
  * @author Mads
  * @version 1.0
  */
 public class Hasher {
 	
<<<<<<< HEAD
 	private String message;
 	MessageDigest digest;
 	
 	
 	public Hasher(String message){
 		this.message = message;
 		message += new SaltGenerator().getSalt();
 		
 		try {
 			digest = MessageDigest.getInstance("MD5");
 			digest.update(message.getBytes());
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Converts the bytes in the MessageDigest into hex
 	 * @return sb.toString() - the bytes in the MessageDigest in hex
 	 */
 	public String convertToHex(){
 		StringBuffer sb = new StringBuffer();
 		byte[] byteData = digest.digest();
         for (int i = 0; i < byteData.length; i++) {
          sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
         }
         return sb.toString();
 	}
 	
 	
 	
	
=======
>>>>>>> b7696175e1c6e8e11578093058e0736c44816622
 }
