 package com.core.util;
 
 
 import javax.swing.*;
 import java.awt.Font;
 import java.awt.GraphicsEnvironment;
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import java.util.HashMap;
 import java.util.Enumeration;
 import java.util.Date;
 import java.util.Calendar;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 /**
  * The model for the Lesson component, which tracks the text, icons, and enabled state
  * of each of the buttons, as well as the current page that is displayed. Note that
  * the model, in its current form, is not intended to be subclassed.
  */
 
 
 public class Utils {
     public static JRadioButton getSelection(ButtonGroup group) {
         for (Enumeration e=group.getElements(); e.hasMoreElements(); ) {
            JRadioButton b = (JRadioButton)e.nextElement();
            if (b.getModel() == group.getSelection()) {
                return b;
            }
         }
         return null;
     }
     public static String getToday() {
         DateFormat df = new SimpleDateFormat("yyyyMMdd");
         // Get the date today using Calendar object.
         Date today = Calendar.getInstance().getTime();
         // Using DateFormat format method we can create a string
         // representation of a date with the defined format.
         return df.format(today);
     }
 
     public static String Hash2String(HashMap submit) {
         String str = "";
         int i = 0;
         for (Object key : submit.keySet()) {
              i++;
             if (key.equals("linebreak"))
                  str += "\r\n";
              else
                  str += key +":"+submit.get(key) +"\r\n";
         }
         return str;
     }
 
     public static String Hash2CSV(HashMap submit) {
         int i = 0;
         String row = "";
         for (Object key : submit.keySet()) {
              i++;
              row += "\"" + key + "\"";
              if (i < submit.size()) {
                  row += ",";
              }
         }
         row = row + "\r\n";
         i=0;
         for (Object key : submit.keySet()) {
             row += "\"" + (String)submit.get(key) + "\"";
             i++;
             if (i < submit.size()) {
                row += ",";
             }
         }
         row += "\r\n";
         return row;
     }
 
     public static void WriteFile(String fName, String content) {
         try {
             FileWriter fstream = new FileWriter(fName);
             BufferedWriter out = new BufferedWriter(fstream);
             out.write(content);
             out.close();
         } catch (Exception e) {
             System.out.println(e.getMessage());
         }
     }
     /**
    * check whether the Chinese font has been installed, if not, exit.
    */
    public static Font checkFont() {
       Font[] allfonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
       boolean chineseFontFound = false;
         for (int j = 0; j < allfonts.length; j++) {
             if (allfonts[j].canDisplayUpTo("\u4e00") > 0) {
                 chineseFontFound = true;
                 return new Font(allfonts[j].getFontName(), Font.PLAIN, 14);
             }
          }
       System.out.println("Found failed");
       return null;
    }
 
    static public String byteToHex(byte b) {
       // Returns hex String representation of byte b
       char hexDigit[] = {
          '0', '1', '2', '3', '4', '5', '6', '7',
          '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
       };
       char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
       return new String(array);
    }
 
    static public String charToHex(char c) {
       // Returns hex String representation of char c
       byte hi = (byte) (c >>> 8);
       byte lo = (byte) (c & 0xff);
       return byteToHex(hi) + byteToHex(lo);
    }
 }
