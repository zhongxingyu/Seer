 /*
  * Copyright 1999-2005 The Apache Software Foundation.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *
  */
 package org.jcp.xml.dsig.internal;
 
 import java.io.ByteArrayOutputStream;
 
 /**
  * Derived from Apache sources and changed to use HmacSHA1 objects
  * objects instead of org.apache.xml.security.algorithms.SignatureAlgorithm
  * objects.
  *
  * @author raul
  * @author Sean Mullan
  *
  */
 public class MacOutputStream extends ByteArrayOutputStream {
     private final HmacSHA1 mac;
 
     public MacOutputStream(HmacSHA1 mac) {
         this.mac = mac;
     }
 
     /** @inheritDoc */
     public void write(byte[] arg0)  {
         mac.update(arg0);
     }
     
     /** @inheritDoc */
     public void write(int arg0) {
         mac.update((byte)arg0);
     }
     
     /** @inheritDoc */
     public void write(byte[] arg0, int arg1, int arg2) {
         mac.update(arg0,arg1,arg2);
     }
 }
