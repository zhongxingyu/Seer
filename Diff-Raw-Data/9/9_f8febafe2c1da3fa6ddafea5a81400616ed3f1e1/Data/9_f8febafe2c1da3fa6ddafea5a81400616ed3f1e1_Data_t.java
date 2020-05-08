 /**
  * User: ivan
  * Date: 3-Feb-2005
  * Time: 6:49:35 PM
  */
 
 package org.pcl.parser;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 public class Data {
 
 
   protected ArrayList<Byte> data = new ArrayList<Byte>();
 
   protected static final String EBCIDIC = "cp285";
   /**
    * Lowest printable ASCII
    */
   private static final String LOWEST_PRINTABLE_ASCII = " ";
   /**
    * Highest printable ASCII
    */
   private static final String HIGHEST_PRINTABLE_ASCII = "~";
 
   /**
    * Return data
    *
    * @return data
    */
  public final ArrayList<Byte> getData() {
     return data;
   }
 
   public byte[] getBytes() {
     final byte[] data = new byte[this.data.size()];
     for (int i = 0; i < this.data.size(); i++) {
       data[i] = this.data.get(i);
     }
     return data;
   }
 
   public void addByte(byte bt) {
     data.add(bt);
   }
 
   public int size() {
     return data.size();
   }
 
   public void clear() {
     data.clear();
   }
 
   /**
    * Convert given byte array into a printable string
    *
    * @param data byte array
    * @return string
    */
   public static String dataToString(byte[] data) {
     String result;
     try {
       String ascii = new String(data);
       String ebcidic = new String(data, EBCIDIC);
       int asciiLength = ascii.length();
       int ebcidicLength = ebcidic.length();
       if (asciiLength < 10000 && ebcidicLength < 10000) {
         asciiLength += stripUnprintableAsciiCharacters(ascii).length();
         ebcidicLength += stripUnprintableAsciiCharacters(ebcidic).length();
       }
       if (asciiLength >= ebcidicLength) {
         result = ascii;
       } else {
         result = ebcidic;
       }
     } catch (UnsupportedEncodingException e) {
       throw new RuntimeException(e);
     }
     return result;
   }
 
   /**
    * Strip unprintable characters from the string
    *
    * @param string string
    * @return printable string
    */
   public static String stripUnprintableAsciiCharacters(String string) {
     for (int i = 0; i < string.length(); i++) {
       String s = string.substring(i, i + 1);
       if (s.compareTo(LOWEST_PRINTABLE_ASCII) < 0 ||
         s.compareTo(HIGHEST_PRINTABLE_ASCII) > 0) {
         string = string.replaceAll(s, "");
         i--;
       }
     }
     return string;
   }
 
   @Override
   public String toString() {
     return dataToString(getBytes());
   }
 
 }
