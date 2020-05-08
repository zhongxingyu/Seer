 package org.gnuton.newshub.utils;
 
 import android.util.Xml;
 
 import org.xmlpull.v1.XmlPullParser;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 
 /**
  * Created by gnuton on 6/16/13.
  */
 public class TextUtils {
     public static String removeNonPrintableChars(String s) {
         //s = s.replaceAll("[\\x00-\\x1F]","");
        String rxp = "^\\s*";
        s = s.replaceFirst(rxp,"");
         s = s.replaceAll("[\t\n\r\f]","");
         return s;
     }
     //TODO This class should be removed and part of its code should be in DowlooadTask
     public static String getXMLEncoding(byte[] xmlBuffer) {
         String xml;
         InputStream is;
         final XmlPullParser xpp = Xml.newPullParser();
         try {
             xml = new String(xmlBuffer, "UTF-8");
             is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
             xpp.setInput(is, null);
             while (!"xml".equals(xpp.getName()) && xpp.getEventType() != XmlPullParser.START_TAG) {
                 xpp.next();
             }
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
 
         String encoding =xpp.getInputEncoding();
         return encoding;
     }
 
     //TODO This class should be removed and part of its code should be in DowlooadTask
     public static String getXMLasString(byte[] xmlBuffer, String encoding){
         if (xmlBuffer == null || encoding == null)
             return null;
 
         try {
             String xml = new String(xmlBuffer, encoding);
             return xml;
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
             return null;
         }
     }
 }
