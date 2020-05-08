 /*
  *    Copyright 2009 The MITRE Corporation
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package org.projecthdata.javahstore;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.parsers.ParserConfigurationException;
 import org.projecthdata.javahstore.hdr.DocumentMetadata;
 import org.projecthdata.javahstore.hdr.SectionDocument;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author marc
  */
 class DummySectionDocumentImpl implements SectionDocument {
 
   public DummySectionDocumentImpl() {
   }
 
   @Override
   public String getMediaType() {
     return "text/plain";
   }
 
   @Override
   public void update(String mediaType, InputStream content) {
     throw new UnsupportedOperationException("Not supported yet.");
   }
 
   @Override
   public InputStream getContent() {
     return new ByteArrayInputStream("Hello World !".getBytes());
   }
 
   @Override
   public String getPath() {
     return "hello.txt";
   }
 
   @Override
   public Date getLastUpdated() {
     return new Date();
   }
 
   public static final String TEST_AUTHOR = "Fred Bloggs";
   private static final String TEST_METADATA = "<DocumentMetaData xmlns='http://projecthdata.org/hdata/schemas/2009/11/metadata'>"
           + "<PedigreeInfo>"
           + "<Author>"
           + TEST_AUTHOR
           + "</Author>"
           + "</PedigreeInfo>"
           + "<DocumentId>xyzzy</DocumentId>"
          + "<Title>The Title</Title>"
           + "<RecordDate>"
           + "<CreatedDateTime>2006-05-04T18:13:51.0Z</CreatedDateTime>"
           + "</RecordDate>"
           + "</DocumentMetaData>";
 
   @Override
   public DocumentMetadata getMetadata() {
     try {
       return new DocumentMetadata(TEST_METADATA);
     } catch (ParserConfigurationException ex) {
       Logger.getLogger(DummySectionDocumentImpl.class.getName()).log(Level.SEVERE, null, ex);
     } catch (SAXException ex) {
       Logger.getLogger(DummySectionDocumentImpl.class.getName()).log(Level.SEVERE, null, ex);
     } catch (IOException ex) {
       Logger.getLogger(DummySectionDocumentImpl.class.getName()).log(Level.SEVERE, null, ex);
     }
     return null;
   }
 
   @Override
   public void setMetadata(DocumentMetadata metadata) {
     throw new UnsupportedOperationException("Not supported yet.");
   }
 
 }
