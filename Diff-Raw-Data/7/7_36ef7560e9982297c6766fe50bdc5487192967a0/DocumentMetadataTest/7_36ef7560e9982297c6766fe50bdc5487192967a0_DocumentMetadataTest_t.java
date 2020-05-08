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
 package org.projecthdata.javahstore.hdr;
 
 import java.io.IOException;
 import javax.xml.parsers.ParserConfigurationException;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author marc
  */
 public class DocumentMetadataTest {
 
   public DocumentMetadataTest() {
   }
 
   @BeforeClass
   public static void setUpClass() throws Exception {
   }
 
   @AfterClass
   public static void tearDownClass() throws Exception {
   }
 
   @Before
   public void setUp() {
   }
 
   @After
   public void tearDown() {
   }
 
   private static final String TEST_AUTHOR = "Fred Bloggs";
   public static final String TEST_METADATA = "<DocumentMetaData xmlns='http://projecthdata.org/hdata/schemas/2009/11/metadata'>"
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
 
   /**
    * Test of getXml method, of class DocumentMetadata.
    */
   @Test
   public void testGetXml() throws ParserConfigurationException, SAXException, IOException {
     System.out.println("getXml");
     DocumentMetadata instance = new DocumentMetadata(TEST_METADATA);
     String expResult = TEST_METADATA;
     String result = instance.getXml();
     assertEquals(expResult, result);
   }
 
   /**
    * Test of getAuthor method, of class DocumentMetadata.
    */
   @Test
   public void testGetAuthor() throws ParserConfigurationException, SAXException, IOException {
     System.out.println("getAuthor");
     DocumentMetadata instance = new DocumentMetadata(TEST_METADATA);
     String expResult = TEST_AUTHOR;
     String result = instance.getAuthor();
   }
 
 }
