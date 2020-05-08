 /*
  * This file is part of CraftCommons <http://www.craftfire.com/>.
  *
  * CraftCommons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CraftCommons is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.craftfire.commons;
 
 import com.craftfire.commons.encryption.EncryptionUtil;
 import com.craftfire.commons.encryption.Whirlpool;
 
 import java.awt.*;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.GeneralSecurityException;
 import java.security.MessageDigest;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class CraftCommons {
     /**
      * Checks if the string is an email.
      *
      * @param string The email.
      * @return true, if the string is an email.
      */
     public static boolean isEmail(String string) {
         if (string != null && ! string.isEmpty()) {
             Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
             Matcher m = p.matcher(string);
             if (m.matches()) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Checks if string is a valid IP. Wildcards (*) are allowed.
      *
      * @param string The IP as a string.
      * @return true, if string is an IP.
      */
     public static boolean isIP(String string) {
         if (string != null && ! string.isEmpty()) {
             String[] parts = string.split("\\.");
             if (parts.length == 4) {
                 for (String s : parts) {
                     if (s == "*") {
                         continue;
                     }
                     if (! isInteger(s)) {
                         return false;
                     }
                     int i = Integer.parseInt(s);
                     if (i < 0 || i > 255) {
                         return false;
                     }
                 }
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Checks if input is an integer.
      *
      * @param input The string to parse/check.
      * @return true, if the input is an integer.
      */
     public static boolean isInteger(String input) {
         try {
             Integer.parseInt(input);
             return true;
         } catch (Exception e) {
             return false;
         }
     }
 
     public static String long2ip(Long i) {
         return ((i >> 24) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                (i & 0xFF);
     }
 
     public static Long ip2long(String ip) {
         String[] ipArray = ip.split("\\.");
         long num = 0;
         for (int i = 0; i < ipArray.length; i++) {
             int power = 3 - i;
             num += ((Integer.parseInt(ipArray[i]) % 256 * Math.pow(256, power)));
         }
         return num;
     }
 
     public static boolean inVersionRange(String lastversion, String compare) {
         if (lastversion.equalsIgnoreCase(compare)) {
             return true;
         }
         String s1 = normalisedVersion(compare);
         String s2 = normalisedVersion(lastversion);
         int cmp = s1.compareTo(s2);
         if (cmp < 0) {
             return true;
         }
         return false;
     }
 
     /**
      * Converts a string into a MD5 hash.
      *
      * @param string The string that is going to be encrypted.
      * @return Returns a MD5 hash of the string.
      */
     public static String md5(String string) {
         try {
             MessageDigest md = MessageDigest.getInstance("MD5");
             md.update(string.getBytes("ISO-8859-1"), 0, string.length());
             byte[] hash = md.digest();
             return EncryptionUtil.bytesTohex(hash);
         } catch (GeneralSecurityException e) {
             throw new RuntimeException(e);
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Converts a string into a SHA-1 hash.
      *
      * @param string The string that is going to be encrypted.
      * @return Returns a SHA-1 hash of the string.
      */
     public static String sha1(String string) {
         try {
             MessageDigest md = MessageDigest.getInstance("SHA-1");
             md.update(string.getBytes("ISO-8859-1"), 0, string.length());
             byte[] hash = md.digest();
             return EncryptionUtil.bytesTohex(hash);
         } catch (GeneralSecurityException e) {
             throw new RuntimeException(e);
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Converts a string into a SHA-256 hash.
      *
      * @param string The string that is going to be encrypted.
      * @return Returns a SHA-256 hash of the string.
      */
     public static String sha256(String string) {
         try {
             MessageDigest md = MessageDigest.getInstance("SHA-256");
             md.update(string.getBytes("ISO-8859-1"), 0, string.length());
             byte[] hash = md.digest();
             return EncryptionUtil.bytesTohex(hash);
         } catch (GeneralSecurityException e) {
             throw new RuntimeException(e);
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Converts a string into a SHA-512 hash.
      *
      * @param string The string that is going to be encrypted.
      * @return Returns a SHA-512 hash of the string.
      */
     public static String sha512(String string) {
         try {
             MessageDigest md = MessageDigest.getInstance("SHA-512");
             md.update(string.getBytes("ISO-8859-1"), 0, string.length());
             byte[] hash = md.digest();
             return EncryptionUtil.bytesTohex(hash);
         } catch (GeneralSecurityException e) {
             throw new RuntimeException(e);
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Converts a string into a Whirpool hash.
      *
      * @param string The string that is going to be encrypted.
      * @return Returns a Whirpool hash of the string.
      */
     public static String whirlpool(String string) {
         Whirlpool w = new Whirlpool();
         byte[] digest = new byte[Whirlpool.DIGESTBYTES];
         w.NESSIEinit();
         w.NESSIEadd(string);
         w.NESSIEfinalize(digest);
         return Whirlpool.display(digest);
     }
 
     public static String normalisedVersion(String version) {
         return normalisedVersion(version, ".", 4);
     }
 
     public static String normalisedVersion(String version, String sep, int maxWidth) {
         String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
         StringBuilder sb = new StringBuilder();
         for (String s : split) {
             sb.append(String.format("%" + maxWidth + 's', s));
         }
         return sb.toString();
     }
 
     /**
      * @param urlstring The url of the image.
      * @return Image object.
      */
     public static Image urlToImage(String urlstring) {
         try {
             URL url = new URL(urlstring);
             return Toolkit.getDefaultToolkit().getDefaultToolkit().createImage(url);
         } catch (MalformedURLException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public static String convertStreamToString(InputStream is) {
         try {
             return new java.util.Scanner(is).useDelimiter("\\A").next();
         } catch (java.util.NoSuchElementException e) {
             return "";
         }
     }
 
     public static String convertHexToString(String hex) {
         StringBuilder sb = new StringBuilder();
         StringBuilder temp = new StringBuilder();
         for ( int i=0; i<hex.length()-1; i+=2 ) {
             String output = hex.substring(i, (i + 2));
             int decimal = Integer.parseInt(output, 16);
             sb.append((char)decimal);
             temp.append(decimal);
         }
         return sb.toString();
     }
 
     public static String convertStringToHex(String str) {
         char[] chars = str.toCharArray();
         StringBuffer hex = new StringBuffer();
         for (int i = 0; i < chars.length; i++){
             hex.append(Integer.toHexString((int)chars[i]));
         }
         return hex.toString();
     }
 
     public static String forumCache(String cache, String player, int userid, String nummember, String activemembers,
                                     String newusername, String newuserid, String extrausername, String lastvalue) {
         Util util = new Util();
         return util.forumCache(cache, player, userid, nummember, activemembers, newusername, newuserid,
                                extrausername, lastvalue);
     }
 
     public static String forumCacheValue(String cache, String value) {
         Util util = new Util();
         return util.forumCacheValue(cache, value);
     }
 
     public static String removeChar(String s, char c) {
         Util util = new Util();
         return util.removeChar(s, c);
     }
     
     public HashMap<String, Object> loadLocalYaml(String path) throws IOException {
        return loadYaml(getClass().getClassLoader().getResourceAsStream(path));
     }
 
     public static HashMap<String, Object> loadYaml(File file) throws IOException {
         return loadYaml(new FileInputStream(file));
     }
     
     public static HashMap<String, Object> loadYaml(InputStream yamlStream) throws IOException {
         HashMap<String, Object> yaml = new HashMap<String, Object>();
         if (yamlStream != null) {
            try {
                 InputStreamReader yamlStreamReader = new InputStreamReader (yamlStream);
                 BufferedReader buffer = new BufferedReader (yamlStreamReader);
                 String line, node = "";
                 int lastBlank = 0;
                 while  ((line = buffer.readLine()) != null) {
                     int blank = 0;
                     if (line.charAt(line.length() - 1) == ':') {
                         for (int i=0; i < line.length(); i++) {
                             if (Character.isWhitespace(line.charAt(i))) {
                                 blank++;
                             }
                         }
                         line = line.replaceAll("\\s+", "");
                         line = line.replaceAll(":", "");
                         if (blank == 0) {
                             node = line + ".";
                         } else if (blank > lastBlank) {
                             node += line + ".";
                         } else if (blank <= lastBlank) {
                             String[] split = node.split("\\.");
                             node = node.replace("." + split[split.length - 1], "");
                             node += line + ".";
                         }
                         lastBlank = blank;
                     } else {
                         String[] split = line.split("\\:");
                         String finalNode = split[0].replaceAll("\\s+", "");
                         if (finalNode.startsWith("#")) {
                             continue;
                         }
                         String temp = split[1].substring(1);
                         if (split.length > 1) {
                             for(int i=2; split.length > i; i++){
                                 temp += ":" + split[i];
                             }
                         }
                         int index = temp.lastIndexOf("#");
                         if (index != -1 && Character.isWhitespace(temp.charAt(index - 1))) {
                             temp = temp.substring(0, index - 1);
                         }
                         String value = "";
                         char last = 0;
                         for (int i=0; i < temp.length(); i++) {
                             if (Character.isWhitespace(temp.charAt(i)) && Character.isWhitespace(last)) {
                                 continue;
                             }
                             value += temp.charAt(i);
                             last = temp.charAt(i);
                         }
                         value = value.substring(0, value.length() - 1);
                         yaml.put(node + finalNode, value);
                     }
                 }
             } finally  {
                 yamlStream.close();
             }
         }
         return yaml;
     }
 }
