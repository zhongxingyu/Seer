 /*
 * FrameDataStream.java  $Revision: 1.7 $ $Date: 2001/07/28 17:25:52 $
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
 
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.io.InputStream;
 
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.LinkedList;
 import java.util.Iterator;
 
 
 /**
  * <code>FrameDataStream</code> holds a stream of <code>Frame</code>(s) and
  * provides accessor methods to that stream.
  * <p>
  * <b>Note that this implementation
  * is not synchronized.</b> If multiple threads access a
  * <code>FrameDataStream</code> concurrently, data may be inconsistent or lost.
  *
  * @see org.beepcore.beep.core.Frame
  * @see org.beepcore.beep.core.DataStream
  *
  * @author Eric Dixon
  * @author Huston Franklin
  * @author Jay Kint
  * @author Scott Pead
 * @version $Revision: 1.7 $, $Date: 2001/07/28 17:25:52 $
  */
 public class FrameDataStream extends DataStream {
 
     private static final String ERR_HEADERS_NOT_FOUND =
         "Unable to distinguish MIME entity headers from the rest of the payload's data (two CRLF pairs not found).";
     // linked list of Frame(s)
     private LinkedList frames;
     // total number of bytes read from the list of frames
     private int bytesRead = 0;
     // total length of headers and data in the linked list of frames
     private int length = 0;
     // the offset to the data being read from the current frame
     private int offset = 0;
     // have we received the last Frame for this list of frames
     private boolean haveLast = false;
     private boolean headersParsed = false;
     private boolean parsingHeaders = false;
     // index of the last frame whose bytes have been reported as read to channel
     private int lastFreedIndex = 0;
     // current state of the stream set in close()
     private boolean streamOpen = true;
     private boolean release = false;
     private final int LF = 10;
     private final int CR = 13;
     private final int COLON = 58;
 
     /**
      * Creates a <code>FrameDataStream</code>.
      * For use with in <code>Channel</code>.
      *
      * @param release Indicates whether or not the frames with in this stream
      * are released as they are read.  If you are receiving frames, this must
      * be set to <code>true</code> so the channel can free it's receive buffers.
      */
     FrameDataStream( boolean release )
     {
         this.release = release;
         frames = new LinkedList();
     }
 
     /**
      * Creates a <code>FrameDataStream</code>.
      */
     public FrameDataStream()
     {
         frames = new LinkedList();
     }
 
     /**
      * Creates a <code>FrameDataStream</code>.
      *
      * @param frame The first frame in the data stream.
      */
     public FrameDataStream(Frame frame)
     {
         frames = new LinkedList();
 
         this.frames.add(frame);
 
         this.length = frame.getPayload().length;
 
         if (frame.isLast()) {
             this.haveLast = true;
         }
     }
 
     /**
      * Returns the content type of a <code>FrameDataStrea</code>.  If the
      * <code>Frame</code> containing the content type hasn't been received yet,
      * the method blocks until it is received.
      *
      * @return Content type.
      *
      * @throws BEEPException
      */
     public String getContentType() throws BEEPException
     {
         if (!this.headersParsed) {
             parseHeaders();
         }
 
         return super.getContentType();
     }
 
     /**
      * Returns the transfer encoding of a <code>FrameDataStrea</code>.  If the
      * <code>Frame</code> containing the content type hasn't been received yet,
      * the method blocks until it is received.
      *
      * @return Content type.
      *
      * @throws BEEPException
      */
     public String getTransferEncoding() throws BEEPException
     {
         if (!this.headersParsed) {
             parseHeaders();
         }
 
         return super.getTransferEncoding();
     }
 
     /**
      * Returns the value of the MIME entity header which corresponds to the
      * given <code>name</code>. If the <code>Frame</code> containing the content
      * type hasn't been received yet, the method blocks until it is received.
      *
      * @param name Name of the entity header.
      * @return String Value of the entity header.
      *
      * @throws BEEPException
      */
     public String getHeaderValue(String name) throws BEEPException
     {
         if (!this.headersParsed) {
             parseHeaders();
         }
 
         return super.getHeaderValue(name);
     }
 
     /**
      * Returns an <code>Enumeration</code> of all the MIME entity header names
      * belonging to this <code>FrameDataStream</code>.  If the
      * <code>Frame</code> containing the content type hasn't been received yet,
      * the method blocks until it is received.
      *
      * @throws BEEPException
      */
     public Enumeration getHeaderNames() throws BEEPException
     {
         if (!this.headersParsed) {
             parseHeaders();
         }
 
         return super.getHeaderNames();
     }
 
     private String getName() throws BEEPException
     {
         int b;
         ByteArrayOutputStream name = new ByteArrayOutputStream(32);
 
         // find the ':'
         b = read();
 
         if (b == CR) {
             read();    // move off of the LF
 
             return null;
         }
 
         int i;
 
         for (i = 0; (b != -1) && (b != COLON); i++, b = read()) {
             name.write((byte) b);
         }
 
         read();    // we are pointing to ':' move off of ' '
 
         return new String(name.toByteArray(), 0, i);
     }
 
     private String getValue() throws BEEPException
     {
         int b;
         ByteArrayOutputStream value = new ByteArrayOutputStream(32);
 
         // find the '\r' or '\n'
         int i;
 
         for (b = read(), i = 0; (b != -1) && (b != CR); i++, b = read()) {
             value.write((byte) b);
         }
 
         read();    // move off of LF
 
         return new String(value.toByteArray(), 0, i);
     }
 
     private void parseHeaders() throws BEEPException
     {
         if (this.parsingHeaders) {
             return;
         }
 
         this.parsingHeaders = true;
 
         String name = getName();
 
         if (name == null) {
 
             // set default content type and encoding
             super.setContentType(DataStream.DEFAULT_CONTENT_TYPE);
             super.setTransferEncoding(DataStream.DEFAULT_CONTENT_TRANSFER_ENCODING);
 
             // reset the mark to data portion of payload
             this.headersParsed = true;
            this.parsingHeaders = false;
 
             return;
         }
 
         String value = getValue();
 
         super.setHeader(name, value);
 
         while (true) {
             name = getName();
 
             if (name == null) {
                 break;
             }
 
             value = getValue();
 
             super.setHeader(name, value);
         }
 
         this.headersParsed = true;
         this.parsingHeaders = false;
     }
 
     /**
      * Adds a <code>Frame</code> to this <code>FrameDataStream</code>.
      * Note that this implementation is not synchronized.
      *
      * @param frame Adds a <code>Frame</code> to the data stream.
      */
     public void add(Frame frame)
     {
         if( !this.streamOpen && this.release ) {
             frame.getChannel()
               .freeReceiveBufferBytes(frame.getPayload().length);
             return;
         }
 
         synchronized (this) {
             if( frame.getPayload().length > 0 ) {
                 this.frames.add(frame);
                 this.length += frame.getPayload().length;
             }
 
             if (frame.isLast()) {
                 haveLast = true;
             }
 
             notify();
         }
     }
 
     int available() throws BEEPException
     {
         if (!this.headersParsed) {
 
             // check if headers exist so we don't block
             if (!doHeadersExist()) {
                 return 0;
             }
 
             parseHeaders();
         }
 
         return this.length - this.bytesRead;
     }
 
     private void waitForFrame(int bytesXfered) throws BEEPException
     {
         try {
 
             // we are expecting more bytes wait for a byte to become available
             this.wait();    // notify() is called from add()
         } catch (InterruptedException e) {
             BEEPInterruptedException ie =
                 new BEEPInterruptedException(e.getMessage());
 
             ie.bytesTransferred = bytesXfered;
 
             throw (BEEPException) ie;
         }
     }
 
     int read() throws BEEPException
     {
         if (!this.headersParsed) {
             parseHeaders();
         }
 
         Frame f;
         synchronized (this) {
             do {
                 if (available() == 0) {
 
                     // caller wants to read bytes despite the fact there are no
                     // bytes currently available.
                     if ((this.haveLast == true) && (this.frames.size() == 0)) {
 
                         // no more bytes to read() and none are
                         // expected, return -1
                         return -1;
                     }
 
                     // no bytes available to read, but more are
                     // expected... block
                     waitForFrame(0);
                 }
 
                 // for ease of reading
                 f = (Frame) this.frames.getFirst();
             } while (f == null);
         }
 
         int pos = this.offset;    // index for reading the byte
 
         this.offset++;
         this.bytesRead++;
 
         if (this.offset >= f.getPayload().length) {
             this.offset = 0;
 
             // now check if we should free the frame from our list
             synchronized (this) {
                 this.frames.removeFirst();
             }
 
             // call channel to free the bytes from its buffer
             if (this.release) {
               f.getChannel().freeReceiveBufferBytes(f.getPayload().length);
             }
         }
 
         byte[] b = f.getPayload().data;
 
         return b[f.getPayload().offset + pos] & 0xff;
     }
 
     int read(byte[] dest) throws BEEPException
     {
         return read(dest, 0, dest.length);
     }
 
     int read(byte[] dest, int destOffset, int destLen) throws BEEPException
     {
         if (!this.headersParsed) {
             parseHeaders();
         }
 
         return privateRead(dest, destOffset, destLen);
     }
 
     private int privateRead(byte[] dest, int destOffset, int destLen)
             throws BEEPException
     {
         if ((destOffset < 0) || (destLen < 0)
                 || (destOffset + destLen > dest.length)) {
             throw new IndexOutOfBoundsException();
         }
 
         // check to see if we've already read all there is to read
         if ((this.haveLast == true) && (this.frames.size() == 0)) {
 
             // no more bytes to read() and none are expected, return -1
             return -1;
         }
 
         int count = 0;         // bytes read during this method call
         int bytesAvailable;    // number of bytes available to read
 
         // while count is less then dest buffer length, fill the buffer
         while (count < destLen) {
             Frame f;
 
             // if the legth of the stream equals the number of bytes read plus
             // the entity headers, check if we are done reading
             synchronized (this) {
                 if ((this.length - this.bytesRead) == 0) {
 
                     // are there more bytes expected?
                     // if we have the last frame and we've read it
                     // (index >= size),
                     // return -1
                     if ((this.haveLast == true) && (this.frames.size() == 0)) {
 
                         // no more bytes to read() and none are expected
                         return count;
                     }
 
                     // no bytes available to read, but more are expected...
                     // block
                     waitForFrame(count);
                 }
 
                 f = (Frame) this.frames.getFirst();
                 if (f == null) {
                     continue;
                 }
             }
 
             // let exceptions handle arraycopy() error cases
             bytesAvailable = f.getPayload().length - this.offset;
 
             // if there is enough room to copy all available bytes,
             // do it, else just fill the remaining space.
             if (destLen - count >= bytesAvailable) {
                 System.arraycopy(f.getPayload().data,
                                  f.getPayload().offset + this.offset,
                                  dest, destOffset + count, bytesAvailable);
 
                 this.offset = 0;
                 count += bytesAvailable; // set count to number of bytes copied
                 this.bytesRead += bytesAvailable;
 
                 // call channel to free the bytes from its buffer
                 if( this.release )
                 {
                   f.getChannel().freeReceiveBufferBytes(f.getPayload().length);
                 }
                 synchronized (this) {
                     this.frames.removeFirst();
                 }
             } else {
                 System.arraycopy(f.getPayload().data,
                                  f.getPayload().offset + this.offset,
                                  dest, destOffset + count, destLen - count);
 
                 this.bytesRead += destLen - count;
                 this.offset += destLen - count;
 
                 return destLen;
 
                 //                count += destLen - count;
             }
         }
 
         return count;
     }
 
     private class FrameIterator {
 
         private Iterator i;
         private int pos = 0;
         private byte[] curBytes = new byte[0];
 
         FrameIterator(Collection frames)
         {
             this.i = frames.iterator();
         }
 
         int read()
         {
             int b = -1;
 
             if (pos == curBytes.length) {
                 if (!i.hasNext()) {
                     return -1;
                 }
 
                 curBytes = ((Frame) i.next()).getPayload().data;
                 pos = 0;
             }
 
             b = curBytes[pos];
 
             pos++;
 
             return b & 0xff;
         }
     }
 
     private boolean doHeadersExist()
     {
 
         // none blocking way of finding the headers
         FrameIterator iter = new FrameIterator(this.frames);
         int b1 = iter.read();
         int b2 = iter.read();
 
         if ((b1 == CR) && (b2 == LF)) {
             return true;
         }
 
         for (b1 = b2; b2 != -1; b2 = iter.read()) {
             if ((b1 == CR) && (b2 == LF)) {
                 b1 = iter.read();
                 b2 = iter.read();
 
                 if ((b1 == CR) && (b2 == LF)) {
                     return true;
                 }
             }
 
             b1 = b2;
         }
 
         return false;
     }
 
     /**
      * Returns the number of available bytes to read including both the
      * payload's entity headers and data.  Use this call in conjunction with
      * <code>readHeadersAndData</code>.
      *
      * @return Bytes available
      * @see #readHeadersAndData
      */
     protected int availableHeadersAndData()
     {
         return this.length - this.bytesRead;
     }
 
     /**
      * Reads both the entity headers and data portion of a payload.
      * Use <code>availableHeadersAndData</code> in conjunction with this call.
      * Note that this implementation is not synchronized.
      * <p>
      * This method
      * may only be preceed by <code>availableHeadersAndData</code> and may
      * not be proceeded by any other method call which reads or manipulates the
      * the stream (i.e. <code>read</code>, <code>skip</code>,
      * <code>available</code>, <code>getHeaderValue</code>)
      *
      * @param dest
      * @param destOffset
      * @param destLen
      *
      * @return number of bytes read or -1 if there are no more bytes to read
      * and none are expected.
      *
      *
      * @throws BEEPException
      */
     protected int readHeadersAndData(byte[] dest, int destOffset, int destLen)
             throws BEEPException
     {
         if (this.headersParsed) {
             return -1;
         }
 
         return privateRead(dest, destOffset, destLen);
     }
 
     long skip(long n) throws BEEPException
     {
         int i = 0;
 
         if (available() == 0) {
             return 0;
         }
 
         try {
             Frame f = (Frame) frames.getFirst();
 
             while (i < n) {
                 this.offset++;
                 this.bytesRead++;
 
                 if (this.offset == f.getPayload().length) {
                     this.offset = 0;
 
                     synchronized (this) {
                         frames.removeFirst();
                     }
                     if( this.release )
                     {
                       f.getChannel().freeReceiveBufferBytes(f.getPayload().length);
                     }
 
                     f = (Frame) frames.getFirst();
                 }
 
                 i++;
             }
         } catch (java.util.NoSuchElementException e) {}
 
         return i;
     }
 
     public void close()
     {
       this.streamOpen = false;
       if( this.release )
       {
         for(int i=0; i < this.frames.size(); i++ )
         {
           Frame f = (Frame)this.frames.get(i);
           f.getChannel().freeReceiveBufferBytes(f.getPayload().length);
         }
       }
       this.frames.clear();
     }
 
     /**
      * Returns <code>true</code> if this stream has received the final
      * <code>Frame</code>, otherwise returns <code>false</code>.
      */
     public boolean isComplete()
     {
         return this.haveLast;
     }
 }
