 /*
  * $Id$
  * $Revision$
  * $Date$
  * $Author$
  *
  * The DOMS project.
  * Copyright (C) 2007-2010  The State and University Library
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package dk.statsbiblioteket.doms.bitstorage.highlevel.status;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.xml.bind.*;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.ws.WebServiceException;
 import java.util.*;
 import java.io.StringWriter;
 
 import dk.statsbiblioteket.doms.bitstorage.highlevel.ConfigException;
 import dk.statsbiblioteket.doms.bitstorage.highlevel.status.Event;
 import dk.statsbiblioteket.doms.bitstorage.highlevel.HighlevelSoapException;
 
 
 public class StaticStatus {
     private static List<Operation> threads = new ArrayList<Operation>();
 
     private static Log log = LogFactory.getLog(StaticStatus.class);
 
     private static DatatypeFactory dataTypeFactory;
 
     static {//static constructor
         try {
             dataTypeFactory = DatatypeFactory.newInstance();
         } catch (DatatypeConfigurationException e) {
             throw new ConfigException(e);
         }
 
         StaticStatus.threads
                 = Collections.synchronizedList(new ArrayList<Operation>());
 
     }
 
     public static Operation initOperation(String s) {
         Operation op = new Operation();
         op.setID(UUID.randomUUID().toString());
         op.setHighlevelMethod(s);
         threads.add(op);
         return op;
     }
 
     public static void endOperation(Operation op) {
         try {
             log.trace(dumpOperation(op));
         } catch (Exception e) {
             log.warn("Caught exception as we tried to dump operation", e);
         } finally {
             threads.remove(op);
         }
     }
 
     public static String dumpOperation(Operation op) {
         java.io.StringWriter sw = new StringWriter();
 
         JAXBContext jaxbcontext = null;
         try {
            jaxbcontext = JAXBContext.newInstance(
                    "dk.statsbiblioteket.doms.bitstorage.highlevel.status");
         } catch (JAXBException e) {
             log.error("Cannot create jaxbcontext", e);
             return "";
         }
         Marshaller marshaller = null;
         try {
             marshaller = jaxbcontext.createMarshaller();
         } catch (JAXBException e) {
             log.error("Cannot create jaxb marshaller", e);
             return "";
         }
         try {
             marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
         } catch (PropertyException e) {
             log.error("Cannot set marshaller property", e);
             return "";
         }
         try {
             marshaller.marshal(op, sw);
         } catch (JAXBException e) {
             log.error("Cannot marshall operation", e);
             return "";
         }
         return sw.toString();
     }
 
     public static void event(Operation operation, String message) {
         Event event = new Event();
 
         XMLGregorianCalendar now
                 = dataTypeFactory.newXMLGregorianCalendar(new GregorianCalendar());
         event.setWhen(now);
         event.setWhat(message);
         operation.getHistory().add(event);
     }
 
 
     public static StatusInformation status() throws HighlevelSoapException {
         try {
             String message = "Invoking status()";
             log.trace(message);
 
             StatusInformation status = new StatusInformation();
 
             status.getOperations().addAll(threads);
 
             return status;
         } catch (RuntimeException e) {
             throw new WebServiceException(e);
         }
 
     }
 }
