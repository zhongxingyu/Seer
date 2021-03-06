 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.axiom.util.stax.dialect;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 class Woodstox4Dialect extends AbstractStAXDialect {
     public static final Woodstox4Dialect INSTANCE = new Woodstox4Dialect();
 
     public String getName() {
         return "Woodstox 4.x";
     }
 
     public XMLInputFactory enableCDataReporting(XMLInputFactory factory) {
         // For Woodstox, this is sufficient
         factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
         return factory;
     }
 
     public XMLInputFactory disallowDoctypeDecl(XMLInputFactory factory) {
         return StAXDialectUtils.disallowDoctypeDecl(factory);
     }
 
     public XMLInputFactory makeThreadSafe(XMLInputFactory factory) {
         // Woodstox' factories are designed to be thread safe
         return factory;
     }
 
     public XMLOutputFactory makeThreadSafe(XMLOutputFactory factory) {
         // Woodstox' factories are designed to be thread safe
         return factory;
     }
 
     public XMLStreamReader normalize(XMLStreamReader reader) {
         return new Woodstox4StreamReaderWrapper(reader);
     }
 
     public XMLStreamWriter normalize(XMLStreamWriter writer) {
         return new Woodstox4StreamWriterWrapper(writer);
     }
 
     public XMLInputFactory normalize(XMLInputFactory factory) {
         return new Woodstox4InputFactoryWrapper(factory, this);
     }
 
     public XMLOutputFactory normalize(XMLOutputFactory factory) {
         return new Woodstox4OutputFactoryWrapper(factory, this);
     }
 }
