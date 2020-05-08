 //    Data SQL is a light JDBC wrapper.
 //    Copyright (C) 2012 Adrián Romero Corchado.
 //
 //    This file is part of Data SQL
 //
 //     Licensed under the Apache License, Version 2.0 (the "License");
 //     you may not use this file except in compliance with the License.
 //     You may obtain a copy of the License at
 //     
 //         http://www.apache.org/licenses/LICENSE-2.0
 //     
 //     Unless required by applicable law or agreed to in writing, software
 //     distributed under the License is distributed on an "AS IS" BASIS,
 //     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //     See the License for the specific language governing permissions and
 //     limitations under the License.
 
 package com.adr.datasql;
 
 import java.io.UnsupportedEncodingException;
 import org.apache.commons.codec.DecoderException;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.codec.binary.Hex;
 
 /**
  *
  * @author adrian
  */
 public class EncodeUtils {
     
     private EncodeUtils() {
     }
  
     public static byte[] decode(String base64) {
 
         try {
             return base64 == null ? null : Base64.decodeBase64(base64.getBytes("ASCII"));
         } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
         }
     }
 
     public static String encode(byte[] raw) {
         try {
             return raw == null ? null : new String(Base64.encodeBase64(raw), "ASCII");
         } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
         }
     }
 
     public static byte[] decodeHEX(String hex) {
         try {
             return hex == null ? null : Hex.decodeHex(hex.toCharArray());
         } catch (DecoderException ex) {
            throw new RuntimeException(ex);
         }
     }
 
     public static String encodeHEX(byte[] raw) {
         return raw == null ? null : new String(Hex.encodeHex(raw));
     }   
 }
