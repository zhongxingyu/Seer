 
 /*
 * StringDataStream.java            $Revision: 1.2 $ $Date: 2001/04/15 04:47:54 $
  *
  * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
  *
  * The contents of this file are subject to the Blocks Public License (the
  * "License"); You may not use this file except in compliance with the License.
  *
  * You may obtain a copy of the License at http://www.invisible.net/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  */
 package org.beepcore.beep.core;
 
 
 import java.util.MissingResourceException;
 
 import java.io.UnsupportedEncodingException;
 
 
 /**
  * <code>StringDataStream</code> represents a BEEP message's payload.
  * Allows implementors to treat a
  * <code>String</code> as a <code>DataSream</code>. The <code>String</code>
  * is stored as a <code>byte[]</code>.
  * <p>
  * <b>Note that this implementation
  * is not synchronized.</b> If multiple threads access a
  * <code>StringDataStream</code> concurrently, data may be inconsistent or lost.
  *
  * @see org.beepcore.beep.core.DataStream
  *
  * @author Eric Dixon
  * @author Huston Franklin
  * @author Jay Kint
  * @author Scott Pead
 * @version $Revision: 1.2 $, $Date: 2001/04/15 04:47:54 $
  */
 public class StringDataStream extends ByteDataStream {
 
     private String enc;
 
     /**
      * Creates a <code>StringDataStream</code> with a <code>String</code> and
      * a <code>BEEP_XML_CONTENT_TYPE</code> content type.
      *
      * @param data  A <code>String</code> representing a message's payload.
      */
     public StringDataStream(String data)
     {
         super(BEEP_XML_CONTENT_TYPE);
 
         try {
             setData(data.getBytes("UTF-8"));
 
             this.enc = "UTF-8";
         } catch (UnsupportedEncodingException e) {
             throw new MissingResourceException("Encoding UTF-8 not supported",
                                                "StringDataStream", "UTF-8");
         }
     }
 
     /**
      * Creates a <code>StringDataStream</code> with a <code>String</code> and
      * a specified content type.
      *
      * @param contentType Content type of <code>data</code>
      * @param data  A <code>String</code> representing a message's payload.
      */
     public StringDataStream(String contentType, String data)
     {
        super(contentType);
 
         try {
             setData(data.getBytes("UTF-8"));
 
             this.enc = "UTF-8";
         } catch (UnsupportedEncodingException e) {
             throw new MissingResourceException("Encoding UTF-8 not supported",
                                                "StringDataStream", "UTF-8");
         }
     }
 
     /**
      * Creates a <code>StringDataStream</code> with a <code>String</code> and
      * a specified content type and encoding.
      *
      * @param contentType Content type of <code>data</code>
      * @param data  A <code>String</code> representing a message's payload.
      * @param enc The encoding used when converting <code>data</code> to a
      * <code>bytes[]</code>.
      *
      * @throws UnsupportedEncodingException
      */
     public StringDataStream(String contentType, String data, String enc)
             throws UnsupportedEncodingException
     {
         super(contentType, data.getBytes(enc));
 
         this.enc = enc;
     }
 
     /**
      * Returns the encoding used to convert the <code>String</code> to a
      * <code>bytes[]</code>.
      */
     public String getEncoding()
     {
         return this.enc;
     }
 }
