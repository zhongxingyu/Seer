 /*
 * $Id: Utils.java,v 1.3 2007/11/04 18:34:36 vtschopp Exp $
  * 
  * Created on May 30, 2006 by tschopp
  *
  * Copyright (c) Members of the EGEE Collaboration. 2004.
  * See http://eu-egee.org/partners/ for details on the copyright holders.
  * For license conditions see the license file or http://eu-egee.org/license.html
  */
 package org.glite.slcs.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.CharacterCodingException;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.CharsetEncoder;
 
 import org.apache.commons.lang.SystemUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Utils some utility functions
  * 
  * @author Valery Tschopp <tschopp@switch.ch>
 * @version $Revision: 1.3 $
  */
 public class Utils {
 
     /** logging */
     static private Log LOG= LogFactory.getLog(Utils.class);
 
     /**
      * Filter an unicode Java string accentuated characters and replace them
      * with their non-accentuated equivalants.
      * 
      * @param unicode
      *            The string to filter.
      * @return A unicode string without accentuated characters or
      *         <code>null</code> if the filtering failed.
      */
     static public String filterUnicodeAccentuedString(String unicode) {
         String latin1= Utils.convertUnicodeToISOLatin1(unicode);
         String filteredLatin1= Utils.filterISOLatin1AccentuedString(latin1);
         if (filteredLatin1 == null) {
             return null;
         }
         String filteredUnicode= null;
         try {
             byte[] latin1Bytes= filteredLatin1.getBytes("ISO-8859-1");
             filteredUnicode= new String(latin1Bytes, "ISO-8859-1");
         } catch (UnsupportedEncodingException e) {
             LOG.error("Failed to convert ISO-8859-1: " + filteredLatin1
                     + " to Unicode", e);
         }
         return filteredUnicode;
     }
 
     /**
      * Converts ISO-8859-1 accentued chars into their unaccentued equivalent
      * 
      * 192 => 'A', 193 => 'A', 194 => 'A', 195 => 'A', 196 => 'Ae', 197 => 'A',
      * 198 => 'AE', 199 => 'C', 200 => 'E', 201 => 'E', 202 => 'E', 203 => 'E',
      * 204 => 'I', 205 => 'I', 206 => 'I', 207 => 'I', 209 => 'N', 210 => 'O',
      * 211 => 'O', 212 => 'O', 213 => 'O', 214 => 'Oe', 216 => 'O', 217 => 'U',
      * 218 => 'U', 219 => 'U', 220 => 'Ue', 221 => 'Y', 223 => 'ss', 224 => 'a',
      * 225 => 'a', 226 => 'a', 227 => 'a', 228 => 'ae', 229 => 'a', 230 => 'ae',
      * 231 => 'c', 232 => 'e', 233 => 'e', 234 => 'e', 235 => 'e', 236 => 'i',
      * 237 => 'i', 238 => 'i', 239 => 'i', 241 => 'n', 242 => 'o', 243 => 'o',
      * 244 => 'o', 245 => 'o', 246 => 'oe', 248 => 'o', 249 => 'u', 250 => 'u',
      * 251 => 'u', 252 => 'ue', 253 => 'y', 255 => 'y'
      */
     static public String filterISOLatin1AccentuedString(String latin1) {
         if (latin1 == null) {
             LOG.warn("null string as argument.");
             return null;
         }
         char[] content= latin1.toCharArray();
         StringBuffer unaccentued= new StringBuffer();
         for (int i= 0; i < content.length; i++) {
             int code= content[i];
             switch (code) {
             case 192:
             case 193:
             case 194:
             case 195:
             case 197:
                 unaccentued.append('A');
                 break;
             case 196:
                 unaccentued.append("Ae");
                 break;
             case 198:
                 unaccentued.append("AE");
                 break;
             case 199:
                 unaccentued.append('C');
                 break;
             case 200:
             case 201:
             case 202:
             case 203:
                 unaccentued.append('E');
                 break;
             case 204:
             case 205:
             case 206:
             case 207:
                 unaccentued.append('I');
                 break;
             case 209:
                 unaccentued.append('N');
                 break;
             case 210:
             case 211:
             case 212:
             case 213:
             case 216:
                 unaccentued.append('O');
                 break;
             case 214:
                 unaccentued.append("Oe");
                 break;
             case 217:
             case 218:
             case 219:
                 unaccentued.append('U');
                 break;
             case 220:
                 unaccentued.append("Ue");
                 break;
             case 221:
                 unaccentued.append('Y');
                 break;
             case 223:
                 unaccentued.append("ss");
                 break;
             case 224:
             case 225:
             case 226:
             case 227:
             case 229:
                 unaccentued.append('a');
                 break;
             case 228:
             case 230:
                 unaccentued.append("ae");
                 break;
             case 231:
                 unaccentued.append('c');
                 break;
             case 232:
             case 233:
             case 234:
             case 235:
                 unaccentued.append('e');
                 break;
             case 236:
             case 237:
             case 238:
             case 239:
                 unaccentued.append('i');
                 break;
             case 241:
                 unaccentued.append('n');
                 break;
             case 242:
             case 243:
             case 244:
             case 245:
             case 248:
                 unaccentued.append('o');
                 break;
             case 246:
                 unaccentued.append("oe");
                 break;
             case 249:
             case 250:
             case 251:
                 unaccentued.append('u');
                 break;
             case 252:
                 unaccentued.append("ue");
                 break;
             case 253:
             case 255:
                 unaccentued.append('y');
                 break;
             default:
                 char c= content[i];
                 unaccentued.append(c);
                 break;
             }
         }
         return unaccentued.toString();
     }
 
     /**
      * Shibboleth sends values UTF8 encoded. But mod_jk seems to screw the
      * encoding up.
      * 
      * @param shibUTF8
      *            The strange encoded shibboleth UTF8 string.
      * @return a Java unicode string or <code>null</code> if the convertion
      *         failed.
      */
     static public String convertShibbolethUTF8ToUnicode(String shibUTF8) {
         if (shibUTF8 == null) {
             return null;
         }
         String unicode= null;
         try {
             // get the received bytes as ISO-Latin1 encoded
             byte [] latin1= shibUTF8.getBytes("ISO-8859-1");
             // but interpret them as UTF-8
             unicode= new String(latin1, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             // ignored: e.printStackTrace();
             LOG.error("Failed to convert Shibboleth UTF-8: " + shibUTF8 + " to Unicode", e);
         }
         return unicode;
     }
 
     /**
      * Converts a Java unicode string in a ISO-8859-1 (ISO Latin1) string.
      * 
      * @param unicode
      *            The string to convert
      * @return The ISO-8859-1 string or <code>null</code> if the convertion
      *         failed.
      */
     static public String convertUnicodeToISOLatin1(String unicode) {
         if (unicode == null) {
             return null;
         }
         String iso_8859_1= null;
         try {
             Charset latin1= Charset.forName("ISO-8859-1");
             CharsetDecoder decoder= latin1.newDecoder();
             CharsetEncoder encoder= latin1.newEncoder();
             ByteBuffer bbuf= encoder.encode(CharBuffer.wrap(unicode));
             CharBuffer cbuf= decoder.decode(bbuf);
             iso_8859_1= cbuf.toString();
         } catch (CharacterCodingException e) {
             // ignored: e.printStackTrace();
             LOG.error("Failed to convert Unicode: " + unicode
                     + " to ISO-8859-1", e);
         }
         return iso_8859_1;
     }
 
     /** HEX char table */
     private static final char[] HEX= { '0', '1', '2', '3', '4', '5', '6', '7',
                                       '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
 
     /**
      * Returns the hexadecimal representation of the byte array (uppercase).
      * 
      * @param b
      * @return
      */
     static public String toHexString(byte[] b) {
         if (b == null) {
             return null;
         }
         char[] buf= new char[b.length * 2];
         int j, k;
 
         j= 0;
         for (int i= 0; i < b.length; i++) {
             k= b[i];
             buf[j++]= HEX[(k >>> 4) & 0x0F];
             buf[j++]= HEX[k & 0x0F];
         }
         return new String(buf);
     }
 
     /**
      * Sets permissions on a given file. The permissions are set using the
      * <i>chmod</i> command and will only work on Unix machines. Chmod command
      * must be in the path.
      * 
      * @param file
      *            the file to set the permissions of.
      * @param mode
      *            the Unix style permissions.
      * @return true, if change was successful, otherwise false. It can return
      *         false, in many instances, e.g. when file does not exits, when
      *         chmod is not found, or other error occurs.
      */
     public static boolean setFilePermissions(File file, int mode) {
         String filename= file.getPath();
         if (!file.exists()) {
             try {
                 file.createNewFile();
             } catch (IOException e) {
                 // ignored e.printStackTrace();
                 LOG.warn("Failed to create new empty file: " + filename, e);
             }
         }
         // only on Unix
         if (!SystemUtils.IS_OS_WINDOWS) {
             Runtime runtime= Runtime.getRuntime();
             String[] cmd= new String[] { "chmod", String.valueOf(mode), filename };
             try {
                 Process process= runtime.exec(cmd);
                 return (process.waitFor() == 0) ? true : false;
             } catch (Exception e) {
                 LOG.warn("Command 'chmod " + mode + " " + filename + "' failed", e);
                 return false;
             }
         }
         // on Windows always return true
         else {
            LOG.info("Can not set file permissions " + mode + " on Windows");
             return true;
         }
         
     }
 
     /**
      * Prevents instantiation of utility class.
      */
     private Utils() {
     }
 }
