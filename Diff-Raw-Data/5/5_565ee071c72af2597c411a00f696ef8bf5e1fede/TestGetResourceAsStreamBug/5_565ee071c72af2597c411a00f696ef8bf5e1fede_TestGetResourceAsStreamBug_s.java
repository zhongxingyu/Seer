 package com.github.snowindy.getResourceAsStream;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.InputStream;
 
 import org.apache.commons.io.FileUtils;
 
 public class TestGetResourceAsStreamBug {
     public static void main(String[] args) throws Exception {
 
         byte[] fileBytes = FileUtils.readFileToByteArray(new File(
                "src/com/github/snowindy/getResourceAsStream/test-short-enc.xml"));
 
         printBytes(fileBytes);
 
         ByteArrayOutputStream buffer = new ByteArrayOutputStream();
 
         InputStream is = TestGetResourceAsStreamBug.class
                .getResourceAsStream("/com/github/snowindy/getResourceAsStream/test-short-enc.xml");
 
         int nRead;
         byte[] data = new byte[16384];
 
         while ((nRead = is.read(data, 0, data.length)) != -1) {
             buffer.write(data, 0, nRead);
         }
 
         buffer.flush();
 
         byte[] bis = buffer.toByteArray();
 
         printBytes(bis);
 
         is.close();
 
     }
 
     public static void printBytes(byte[] bv) {
         System.out.println();
         for (byte b : bv) {
             System.out.print(' ');
             System.out.print(String.format("%02X", b));
         }
     }
 }
