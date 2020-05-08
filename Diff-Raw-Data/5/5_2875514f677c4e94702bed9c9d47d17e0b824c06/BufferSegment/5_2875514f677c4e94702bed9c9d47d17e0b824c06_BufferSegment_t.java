 /*
 * BufferSegment.java  $Revision: 1.3 $ $Date: 2001/11/23 15:10:56 $
  *
  * Copyright (c) 2001 Huston Franklin.  All rights reserved.
  *
  * The contents of this file are subject to the Blocks Public License (the
  * "License"); You may not use this file except in compliance with the License.
  *
  * You may obtain a copy of the License at http://www.beepcore.org/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  */
 package org.beepcore.beep.util;
 
 /**
  * A <code>BufferSegment</code> represents a BEEP Frame payload and holds
  * the BEEP Frames's Header, Trailer and the message payload.
  *
  * It contains a byte array an offset into the array and the
  * length from the offset.
  *
  * @author Huston Franklin
 * @version $Revision: 1.3 $, $Date: 2001/11/23 15:10:56 $
  */
 public class BufferSegment {
     /**
      * Constructor BufferSegment
      *
      * @param data A byte array containing a BEEP Frame payload.
      */
     public BufferSegment(byte[] data)
     {
         this.data = data;
         this.offset = 0;
         this.length = data.length;
     }
 
     /**
      * Constructor BufferSegment
      *
      * @param data A byte array containing a BEEP Frame payload.
      * @param offset Indicates the begining position of the BEEP Frame
      * payload in the byte array <code>data</code>.
      * @param length Number of valid bytes in the byte array starting from
      * <code>offset</code>.
      */
     public BufferSegment(byte[] data, int offset, int length)
     {
         this.data = data;
         this.offset = offset;
         this.length = length;
     }
 
     public byte[] getData()
     {
         return this.data;
     }
 
     public int getOffset()
     {
         return this.offset;
     }
 
     public int getLength()
     {
         return this.length;
     }
 
     private byte[] data;
     private int offset;
     private int length;
 }
 
