 /*-
  * Copyright (c) 2006, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer. 
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution. 
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.logicprobe.LogicMail.util;
 
 import java.io.IOException;
 import org.logicprobe.LogicMail.AppInfo;
 
 /**
  * Proxy for utility classes and methods that may
  * have multiple implementations depending on the
  * project build configuration.
  */
 public abstract class UtilProxy {
     private static UtilProxy instance = null;
     
     /**
      * Creates a new instance of UtilProxy
      */
     private static UtilProxy createUtilProxy() {
         UtilProxy utilProxy = null;
         String version = AppInfo.getVersion();
         try {
            if(version.indexOf("BB v4.0") != -1) {
                 utilProxy =
                     (UtilProxy)Class.forName("org.logicprobe.LogicMail.util.UtilProxyBB40").newInstance();
             }
            else if(version.indexOf("BB v4.1") != -1) {
                 utilProxy =
                     (UtilProxy)Class.forName("org.logicprobe.LogicMail.util.UtilProxyBB41").newInstance();
             }
         } catch (ClassNotFoundException e) {
             utilProxy = null;
         } catch (InstantiationException e) {
             utilProxy = null;
         } catch (IllegalAccessException e) {
             utilProxy = null;
         }
         return utilProxy;
     }
 
     public static synchronized UtilProxy getInstance() {
         if(instance == null) {
             instance = createUtilProxy();
             if(instance == null) {
                 throw new RuntimeException("Application configuration error");
             }
         }
         
         return instance;
     }
     
     /**
      * Decode the Base64 encoded input and return the result.
      *
      * @param input The Base64 encoded input
      * @return A byte array containing the decoded input.
      * @throw IOException Thrown if a decoding error occurred.
      */
     public abstract byte[] Base64Decode(String input) throws IOException;
     
     /**
      * Encodes the provided input into Base 64 and returns the encoded result.
      *
      * @param input       The input data to encode
      * @param inputOffset The offset into the array
      * @param inputLength The length of the array
      * @param insertCR    Set to true if you want to insert a CR after every 76th encoded character
      * @param insertLF    Set to true if you want to insert a LF after every 76th encoded character
      * @throw IOException Thrown if an encoding error occurred
      * @return The encoded input as a byte array
      */
     public abstract byte[] Base64Encode(byte[] input, int inputOffset, int inputLength, boolean insertCR, boolean insertLF) throws IOException;
     
     /**
      * Encodes the provided input into Base 64 and returns the encoded result.
      *
      * @param input       The input data to encode
      * @param inputOffset The offset into the array
      * @param inputLength The length of the array
      * @param insertCR    Set to true if you want to insert a CR after every 76th encoded character
      * @param insertLF    Set to true if you want to insert a LF after every 76th encoded character
      * @throw IOException Thrown if an encoding error occurred
      * @return The encoded input as a string
      */
     public abstract String Base64EncodeAsString(byte[] input, int inputOffset, int inputLength, boolean insertCR, boolean insertLF) throws IOException;
 }
