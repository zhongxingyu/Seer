 /*
 * Copyright 2012 INRIA 
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
*/
 package com.mymed.utils;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import org.slf4j.Logger;
 
 public class HashFunction {
 
  Logger LOGGER = MLogger.getLogger();
 
   private final int MaxInt = Integer.MAX_VALUE;
   private final String epsilon;
 
   public HashFunction(final String epsilon) {
     this.epsilon = epsilon;
   }
 
   private String convertToHex(final byte[] data) {
     final StringBuffer buf = new StringBuffer();
 
     for (final byte element : data) {
       int halfbyte = element >>> 4 & 0x0F;
       int two_halfs = 0;
 
       do {
         if (0 <= halfbyte && halfbyte <= 9) {
           buf.append((char) ('0' + halfbyte));
         } else {
           buf.append((char) ('a' + (halfbyte - 10)));
         }
 
         halfbyte = element & 0x0F;
       } while (two_halfs++ < 1);
     }
 
     return buf.toString();
   }
 
   public String SHA1ToString(final String text) {
     MessageDigest md;
     byte[] sha1hash = new byte[40];
 
     try {
       md = MessageDigest.getInstance("SHA-1");
       md.update(text.getBytes("iso-8859-1"), 0, text.length());
       sha1hash = md.digest();
     } catch (final NoSuchAlgorithmException e) {
       LOGGER.info("Error converting SHA-1 to String: algorithm not found");
       LOGGER.debug("Error convertin SHA-1 to String: algorithm not found", e);
     } catch (final UnsupportedEncodingException e) {
       LOGGER.info("Error converting SHA-1 to String: encoding not found");
       LOGGER.debug("Error convertin SHA-1 to String: encoding not found", e);
     }
 
     return convertToHex(sha1hash);
   }
 
   public int convertHexToInt(final String hex) {
     return Integer.valueOf(hex, 16).intValue();
   }
 
   private int _SHA1ToInt(final String text) {
     final String sha1 = SHA1ToString(text + epsilon);
     int res = 0;
     for (int i = 0; i < sha1.length(); i++) {
       final int pow = (int) (Math.pow(16, i) % MaxInt);
       switch (sha1.charAt(i)) {
         case '0' :
           break;
         case '1' :
           res += pow;
           break;
         case '2' :
           res += 2 * pow;
           break;
         case '3' :
           res += 3 * pow;
           break;
         case '4' :
           res += 4 * pow;
           break;
         case '5' :
           res += 5 * pow;
           break;
         case '6' :
           res += 6 * pow;
           break;
         case '7' :
           res += 7 * pow;
           break;
         case '8' :
           res += 8 * pow;
           break;
         case '9' :
           res += 9 * pow;
           break;
         case 'a' :
           res += 10 * pow;
           break;
         case 'b' :
           res += 11 * pow;
           break;
         case 'c' :
           res += 12 * pow;
           break;
         case 'd' :
           res += 13 * pow;
           break;
         case 'e' :
           res += 14 * pow;
           break;
         case 'f' :
           res += 15 * pow;
           break;
       }
 
       res %= MaxInt;
     }
 
     return res;
   }
 
   public int SHA1ToInt(final String text) {
     try {
       return Integer.parseInt(text);
     } catch (final NumberFormatException e) {
       return _SHA1ToInt(text);
     }
   }
 }
