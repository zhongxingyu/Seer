 /*
  * OutputDataStream.java  $Revision: 1.5 $ $Date: 2003/04/21 15:09:11 $
  *
  * Copyright (c) 2001,2002 Huston Franklin.  All rights reserved.
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
 package org.beepcore.beep.core;
 
 
 import java.util.LinkedList;
 
 import org.beepcore.beep.util.BufferSegment;
 
 /**
  * <code>OutputDataStream</code> represents a BEEP message's payload as a
  * stream.
  *
  * @author Huston Franklin
  * @version $Revision: 1.5 $, $Date: 2003/04/21 15:09:11 $
  */
 public class OutputDataStream {
 
     /**
      * Creates an <code>OutputDataStream</code> without any mime
      * headers. It is the responsibility of the application to ensure
      * the mime headers exist in the first <code>BufferSegment</code>
      * added.
      */
     public OutputDataStream()
     {
         this.mimeHeaders = null;
     }
 
     /**
      * Creates an <code>OutputDataStream</code> using the specified
      * mime headers.
      *
      * @param headers Mime headers to be prepended to the buffers in
      * the stream.
      */
     public OutputDataStream(MimeHeaders headers)
     {
         this.mimeHeaders = headers;
     }
 
     /**
      * Creates an <code>OutputDataStream</code> using the specified
      * mime headers.
      *
      * @param headers Mime headers to be prepended to the buffers in
      * the stream.
      */
     public OutputDataStream(MimeHeaders headers, BufferSegment buf)
     {
         this.mimeHeaders = headers;
         this.add(buf);
     }
 
     public void add(BufferSegment segment) {
         this.buffers.addLast(segment);
         if (channel != null) {
             try {
                 channel.sendQueuedMessages();
             } catch (BEEPException e) {
             }
         }
     }
 
     /**
      * @deprecated
      */
     public void close() {
         this.setComplete();
     }
 
     /**
      * Returns <code>true</code> if no more bytes will be added to
      * those currently available on this stream.  Returns
      * <code>false</code> if more bytes are expected.
      */
     public boolean isComplete() {
         return this.complete;
     }
 
     public void setComplete() {
         this.complete = true;
         if (channel != null) {
             try {
                 channel.sendQueuedMessages();
             } catch (BEEPException e) {
             }
         }
     }
 
     boolean availableSegment() {
         return (buffers.isEmpty() == false);
     }
 
    BufferSegment getNextSegment(int maxLength) {
         if (this.headersSent == false) {
             if (this.mimeHeaders != null) {
                 this.buffers.addFirst(mimeHeaders.getBufferSegment());
             }
             this.headersSent = true;
         }
 
         BufferSegment b = (BufferSegment)buffers.getFirst();
 
         if (curOffset != 0 || maxLength < b.getLength()) {
 
             int origLength = b.getLength();
 
             b = new BufferSegment(b.getData(), b.getOffset() + curOffset,
                                   Math.min(maxLength, origLength - curOffset));
 
             if (curOffset + b.getLength() != origLength) {
                 curOffset += b.getLength();
                 return b;
             }
         }
 
         buffers.removeFirst();
         curOffset = 0;
 
         return b;
     }
     
     void setChannel(ChannelImpl channel) {
         this.channel = channel;
     }
 
     protected final MimeHeaders mimeHeaders;
 
     private LinkedList buffers = new LinkedList();
     private boolean complete = false;
     private boolean headersSent = false;
     private int curOffset = 0;
     private ChannelImpl channel = null;
 }
