 /*
  * XmlParser.java
  *
  * Copyright (C) 2005-2008 Tommi Laukkanen
  * http://www.substanceofcode.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.substanceofcode.utils;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 
 /**
  * Simple and lightweight XML parser without complete error handling.
  *
  * @author Tommi Laukkanen (tlaukkanen at gmail dot com)
  */
 public class XmlParser {
     
     private CustomInputStream m_inputStream = null;
     private static String encoding = "UTF-8";
     
     /** Current XML element name (eg. <title> = title) */
     private String m_currentElementName = "";
     private String m_currentElementData = "";
     private boolean m_currentElementContainsText = false;
     
     /** Enumerations for parse function */
     public static final int END_DOCUMENT = 0;
     public static final int ELEMENT = 1;
     
     /** 
      * Creates a new instance of XmlParser
      * @param inputStream   Stream containing XML document.
      */
     public XmlParser(CustomInputStream inputStream) {
         m_inputStream = inputStream;
     }
     
     public XmlParser(String xmlDocument) {
         InputStream is = new ByteArrayInputStream(xmlDocument.getBytes());
         CustomInputStream cis = new CustomInputStream(is);
         m_inputStream = cis;
     }
     
     /** 
      * Parse next element
      * @return Element type or end-of-document.
      * @throws java.io.IOException 
      */
     public int parse() throws IOException {
         StringBuffer inputBuffer = new StringBuffer();
         
         boolean parsingElementName = false;
         boolean elementFound = false;
         boolean elementStart = false;
         boolean parsingElementData = false;
                 
         int inputCharacter;
         char c;
         inputCharacter = m_inputStream.read();
         
         while (inputCharacter != -1 && elementFound==false) {
             c = (char)inputCharacter;
             
             if(c=='/' && elementStart==true) {
                 parsingElementName = false;
             }
             else if(elementStart==true && (c=='?' || c=='!')) {
                 if(m_currentElementData.charAt(m_currentElementData.length()-1)=='<') {
                     parsingElementName = false;
                 }
             }
             if(parsingElementName==true) {
                 if(c==' ' || c=='/' ) {
                     parsingElementName = false;
                     parsingElementData = true;
                 }
                 else if(c!='>') {
                     m_currentElementName += c;
                 }
             }              
             if(c=='<') {
                 elementStart = true;
                 parsingElementName = true;
                 parsingElementData = true;
                 m_currentElementName = "";
                 m_currentElementData = "";
             }            
             if(parsingElementData==true) {
                 m_currentElementData += c;
             }
             if(c=='>') {
                 if(m_currentElementName.length()>0) {
                     elementFound = true;
                     parsingElementName = false;
                 }
             }    
 
             if(!elementFound){
                 inputCharacter = m_inputStream.read();
             }
         }
         
         if( m_currentElementData.charAt( m_currentElementData.length()-2 )=='/' &&
             m_currentElementData.charAt( m_currentElementData.length()-1 )=='>' ) {
             m_currentElementContainsText = false;
         } else {
             m_currentElementContainsText = true;
         }
         
         if( inputCharacter==-1 ) {
             return END_DOCUMENT;
         } else {
             return ELEMENT;
         }
     }
     
     /** Get element name */
     public String getName() {
         return m_currentElementName;
     }
     
     /** Get element text including inner xml */
     public String getText() throws IOException {
         Log.debug("Getting text for element '" + m_currentElementName + "'");
         if(m_currentElementContainsText==false) {
             return "";
         }
         boolean endParsing = false;
         
         String endElementName = "";
         String text;
         StringBuffer textBuffer = new StringBuffer();
         int inputCharacter;
         char c;
         char lastChars[] = new char[3];
         lastChars[0] = ' ';
         lastChars[1] = ' ';
         lastChars[2] = ' ';
         
         char elementNameChars[] = new char[2];
         elementNameChars[0] = m_currentElementName.charAt( m_currentElementName.length()-2 );
         elementNameChars[1] = m_currentElementName.charAt( m_currentElementName.length()-1 );
         while ((inputCharacter = m_inputStream.read()) != -1 && endParsing==false) {
             c = (char)inputCharacter;
             lastChars[0] = lastChars[1];
             lastChars[1] = c;
             //System.out.print(c);
 
             textBuffer.append(c);
             if( lastChars[0] == elementNameChars[0] &&
                 lastChars[1] == elementNameChars[1]) {
                 if( textBuffer.toString().endsWith("</" + m_currentElementName)) {
                     endParsing = true;
                 }
             }
         }
 
         if (encoding.equals("")) {
             text = textBuffer.toString();
         } else {
             try {
                 text = new String(textBuffer.toString().getBytes(), encoding);
             } catch (UnsupportedEncodingException e) {
                 Log.add("Couldn't use UTF-8 encoding");
                 try {
                     text = new String(textBuffer.toString().getBytes(), "UTF8");
                     encoding = "UTF8";
                 } catch (UnsupportedEncodingException e2) {
                     Log.add("Couldn't use UTF8 encoding");
                     text = textBuffer.toString();
                     encoding = "";
                 }
             }
         }
 
         text = textBuffer.toString();
         text = StringUtil.replace(text, "</" + m_currentElementName, "");
         
         /** Handle some entities and encoded characters */
         //Log.add("GetText() before: " + text);
         text = decodeCharacters(text);
         //Log.add("GetText() after : " + text);
         return text;
     }
 
     /** 
      * Get attribute value from current element 
      */
     public String getAttributeValue(String attributeName) {
         
         /** Check whatever the element contains given attribute */
         int attributeStartIndex = m_currentElementData.indexOf(attributeName);
         if( attributeStartIndex<0 ) {
             return null;
         }
         
         /** Calculate actual value start index */
         int valueStartIndex = attributeStartIndex + attributeName.length() + 2;
         
         /** Check the attribute value end index */
         int valueEndIndex = m_currentElementData.indexOf("\"", valueStartIndex);
         if( valueEndIndex<0 ) {
             return null;
         }
         
         /** Parse value */
         String value = m_currentElementData.substring(valueStartIndex, valueEndIndex);
         value = decodeCharacters(value);
         return value;
     }
     
     private String decodeCharacters(String text) {
         text = StringUtil.replace(text, "&lt;", "<");
         text = StringUtil.replace(text, "&gt;", ">");
         text = StringUtil.replace(text, "&nbsp;", " ");
        text = StringUtil.replace(text, "&quot;", "\"");
         text = StringUtil.replace(text, "&amp;", "&");
         text = StringUtil.replace(text, "&auml;", "ä");
         text = StringUtil.replace(text, "&ouml;", "ö");
         text = StringUtil.replace(text, "Ã¤", "ä");
         text = StringUtil.replace(text, "Ã¶", "ö");
         text = StringUtil.replace(text, "â??", "'");
         text = StringUtil.replace(text, "&#8217;", "'");
         text = StringUtil.replace(text, "&#8216;", "'");
         text = StringUtil.replace(text, "&#8220;", "\"");
         text = StringUtil.replace(text, "&#8221;", "\"");
         text = StringUtil.replace(text, "&#39;", "\"");
         text = StringUtil.replace(text, "â‚¬", "€");
         text = StringUtil.replace(text, String.valueOf((char)226) + String.valueOf((char)128) + String.valueOf((char)153), "'"); 
         text = StringUtil.replace(text, String.valueOf((char)226) + String.valueOf((char)128) + String.valueOf((char)166), "..."); 
         text = StringUtil.replace(text, String.valueOf((char)226) + String.valueOf((char)128) + String.valueOf((char)156), "\""); 
         text = StringUtil.replace(text, String.valueOf((char)226) + String.valueOf((char)128) + String.valueOf((char)157), "\""); 
        boolean foundEscape = text.indexOf("&#")>=0;
        int startIndex = 0;
        while(foundEscape) {
            int entityStart = text.indexOf("&#");
            int entityEnd = text.indexOf(";",entityStart);
            if(entityStart>0 && entityEnd>0) {
                String character = text.substring(entityStart+2, entityEnd);
                try {
                    int charValue = 0;
                    charValue = Integer.parseInt(character);
                    if(charValue>0 && charValue<255) {
                        text = StringUtil.replace(text, "&#" + charValue + ";", String.valueOf((char)charValue));    
                    }                    
                } catch(Exception ex) {
                    // Do nothing...
                }
            }
            startIndex++;
            if(startIndex<text.length()) {
                foundEscape = text.indexOf("&#", startIndex)>=0;
            } else {
                foundEscape = false;
            }
        }
         return text;
     }
     
 }
