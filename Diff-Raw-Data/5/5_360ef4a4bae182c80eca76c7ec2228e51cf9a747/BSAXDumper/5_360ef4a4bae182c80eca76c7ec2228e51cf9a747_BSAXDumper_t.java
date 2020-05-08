 /*
  * BSAXDumper.java
  * 
  *  Copyright 2005 Gregor N. Purdy. All rights reserved.
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
  */
 package com.gregorpurdy.xml.bsax;
 
 import org.xml.sax.SAXException;
 
 
 /**
  * This class reads in a BSAX stream and dumps a textual representation
  * of it to System.out. Its intended use is as a debugging aid.
  * 
 * @author Gregor N. Purdy &lt;gregor@focusresearch.com&gt; http://www.gregorpurdy.com/gregor/
 * @version $Id$
  */
 public class BSAXDumper extends AbstractBSAXReader {
   
   /**
    * @param attrs
    * @param i
    * @throws SAXException
    */
   protected void doOpAttribute(int i, int attrUri, int attrLocalName, int attrQName, int attrType, int attrValue) throws SAXException {
     System.out.println("ATTR(" + attrUri + ", " + attrLocalName + ", " + attrQName + ", " + attrType + ", " + attrValue + ") /* " + i + " */");
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpCharacters(int characters) throws SAXException {
     System.out.println("CHAR(" + characters + ")");
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpEndDocument() throws SAXException {
     System.out.println("END_DOC()");
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpEndElement(int uri, int localName, int qName) throws SAXException {
     System.out.println("END_ELEMENT(" + uri + ", " + localName + ", " + qName + ")");
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpEndPrefixMapping(int prefix) throws SAXException {
     System.out.println("END_PREFIX_MAPPING(" + prefix + ")");
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpIgnorableWhitespace(int characters) throws SAXException {
     System.out.println("IGNORABLE_WHITESPACE(" + characters + ")");
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpProcessingInstruction(int target, int data) throws SAXException {
     System.out.println("PROCESSING_INSTRUCTION(" + target + ", " + data + ")");
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpSkippedEntity(int name) throws SAXException {
     System.out.println("SKIPPED_ENTITY(" + name + ")");
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpStartDocument() throws SAXException {
     System.out.println("START_DOCUMENT()");
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpStartElement(int uri, int localName, int qName, int attributeCount) throws SAXException {
     System.out.println("START_ELEMENT(" + uri + ", " + localName + ", " + qName + ") /* " + attributeCount + " attributes */");
   }
 
   /* (non-Javadoc)
    * @see com.gregorpurdy.xml.bsax.AbstractBSAXReader#doOpStartElementFinalize()
    */
   protected void doOpStartElementFinalize() throws SAXException { }
   
   /**
    * @throws SAXException
    */
   protected void doOpStartPrefixMapping(int prefix, int uri) throws SAXException {
     System.out.println("START_PREFIX_MAPPING(" + prefix + ", " + uri + ")");
   }
   
   /**
    * @throws SAXException
    */
   protected void doOpString(int id, String value) throws SAXException {
     System.out.println("STR(" + id + ", /* " + value.length() + " characters */)");
   }
 
   /* (non-Javadoc)
    * @see com.gregorpurdy.xml.bsax.AbstractBSAXReader#doStartStream(byte[], int, int)
    */
   protected void doStartStream() {
     System.out.println("MAGIC(/* 4 bytes */)");
     System.out.println("VERSION(" + getVersion() + ")");
     System.out.println("MAX_STRING_TABLE_SIZE(" + getMaxStringTableSize() + ")");
   }
   
 }
