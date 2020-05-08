 /**
  * Copyright (c) 2012 centeractive ag. All Rights Reserved.
  *
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.centeractive.ws.client;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 /**
  * @author Tom Bujok
  * @since 1.0.0
  */
 public class TransmissionExceptionTest {
 
     @Test
     public void messageFormatEmptyTest() {
         TransmissionException ex = new TransmissionException("", 0, new RuntimeException());
         assertEquals("", ex.getMessage());
     }
 
     @Test
     public void messageFormatCodeTest() {
         TransmissionException ex = new TransmissionException("", 404, new RuntimeException());
        assertEquals("HTTP code = [404]; ", ex.getMessage());
     }
 
     @Test
     public void messageFormatMessageTest() {
         TransmissionException ex = new TransmissionException("Message", 0, new RuntimeException());
         assertEquals("Message; ", ex.getMessage());
     }
 
     @Test
     public void messageFormatCauseTest() {
         TransmissionException ex = new TransmissionException("", 0, new RuntimeException("Cause"));
        assertEquals("Possible cause: Cause;", ex.getMessage());
     }
 
     @Test
     public void messageFormatFullFormatTest() {
         TransmissionException ex = new TransmissionException("SOAP communication failed", 500, new RuntimeException("Internal server error"));
         assertEquals("SOAP communication failed; HTTP code = [500];", ex.getMessage());
     }
 
 }
