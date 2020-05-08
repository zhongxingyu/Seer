 package FileUnique;
 
 import java.io.*;
 import java.security.MessageDigest;
 
 /**
  * User: Mikita_Abramenka
  * Date: 3/20/13
  * Time: 4:47 PM
  */
@SuppressWarnings("serial")
 class HashFile extends File{
     public HashFile(String pathName) {
         super(pathName);
     }
 
     public String getHash() throws GetKeyException {
         try {
             MessageDigest md = MessageDigest.getInstance("MD5");
             try (FileInputStream fis = new FileInputStream(super.getAbsoluteFile())) {
                 byte[] dataBytes = new byte[1024];
                 int nread;
                 while ((nread = fis.read(dataBytes)) != -1) {
                     md.update(dataBytes, 0, nread);
                 }
             }
             return bytesToString(md.digest());
         }
         catch (Exception e) {
             throw new GetKeyException(e);
         }
     }
 
     private static String bytesToString(byte[] bytes) {
         StringBuilder sb = new StringBuilder();
         for (byte aByte : bytes) {
             sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
         }
         return sb.toString();
     }
 }
