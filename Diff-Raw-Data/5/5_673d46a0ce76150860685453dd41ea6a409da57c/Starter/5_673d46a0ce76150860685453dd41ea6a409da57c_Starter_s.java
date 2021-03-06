 /*
  * $Id$
  * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
  * For details on use and redistribution please refer to the
  * LICENSE file included with these sources.
  */
 
 package org.apache.fop.apps;
 
 import org.apache.log.*;
 
 // SAX
 import org.xml.sax.XMLReader;
 import org.xml.sax.SAXException;
 
 // Java
 import java.io.*;
 import java.net.URL;
 
 /**
  * abstract super class
  * Creates a SAX Parser (defaulting to Xerces).
  *
  */
 public abstract class Starter {
 
     Options options;
     InputHandler inputHandler;
     protected Logger log;
 
     public Starter() throws FOPException {
         options = new Options();
     }
 
     public void setLogger(Logger logger) {
         log = logger;
     }
 
     public void setInputHandler(InputHandler inputHandler) {
         this.inputHandler = inputHandler;
     }
 
     abstract public void run() throws FOPException;
 
     // setting the parser features
     public void setParserFeatures(XMLReader parser) throws FOPException {
         try {
             parser.setFeature("http://xml.org/sax/features/namespace-prefixes",
                               true);
         } catch (SAXException e) {
            throw new FOPException("Error in setting up parser feature namespace-prefixes\n"
                                   + "You need a parser which supports SAX version 2", e);
         }
     }
 
 }
