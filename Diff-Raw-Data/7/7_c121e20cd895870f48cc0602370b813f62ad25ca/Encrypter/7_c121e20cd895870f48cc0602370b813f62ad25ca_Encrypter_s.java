 package util;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 /**
  *
  * @author guilherme
  */
 public class Encrypter {
     public static String hashify(String str) throws NoSuchAlgorithmException{
         return byteArrayToHexString(computeHash(str));
     }
     private static byte[] computeHash(String str) throws NoSuchAlgorithmException{
         MessageDigest md = MessageDigest.getInstance("SHA-1");
         md.update(str.getBytes());
         return md.digest();
     }
    private static byteArrayToHexString(byte[] ba){
        String s = ""
         for(byte b:ba){
            s+=b&0xff<16 ? "0" : Integer.toHexString(b&0xff);
         }
         return s;
     }
 }
