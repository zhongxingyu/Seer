 /*
  * JOnAS: Java(TM) Open Application Server
  * Copyright (C) 2004 Bull S.A.
  * All rights reserved.
  * 
  * Contact: howl@objectweb.org
  * 
  * This software is licensed under the BSD license.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 
  *   * Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *     
  *   * Redistributions in binary form must reproduce the above copyright
  *     notice, this list of conditions and the following disclaimer in the
  *     documentation and/or other materials provided with the distribution.
  *     
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.objectweb.howl.log;
 
 
 import org.objectweb.howl.log.LogBufferStatus;
 
 import java.io.IOException;
 
 import java.nio.BufferOverflowException;
 import java.nio.InvalidMarkException;
 
 /**
  * An implementation of LogBuffer that
  * provides features necessary for a reliable Transaction Monitor
  * journal.
  *
  * <p>Each block contains a header, zero or more log records,
  * and a footer.  The header and footer contain enough
  * information to allow recovery operations to validate
  * the integrity of each log block.
  */
 class BlockLogBuffer extends LogBuffer
 {
   /**
    * currentTimeMillis that last record was added.
    * <p>This field is used by shouldForce() to determine if the buffer
    * should be forced.
    */
   long todPut = 0;
   
   /**
    * number of times this buffer was used.
    * <p>In general, should be about the same for all buffers in a pool.
    */
   int initCounter = 0;
 
   /**
    * size of a buffer header.
    * <pre>
    * <b>buffer Header format</b>
    * byte[]  HEADER_ID              [4] "HOWL"
    * int     block_sequence_number  [4]
    * int     block_size             [4] in bytes
    * int     bytes used             [4]
    * int     checkSum               [4] 
    * long    currentTimeMillis      [8]
    * byte[]  CRLF                   [2] to make it easier to read buffers in an editor
    * </pre>
    */
   private final static int bufferHeaderSize = 30; 
  
   /**
    * Offset within the block header of the bytes_used field.
    */
   private int bytesUsedOffset = 0;
   
   /**
    * The number of bytes to reserve for block footer information.
    */
   private final static int bufferFooterSize = 18;
   /* 
    * byte FOOTER_ID             [4] "LOWH"
    * int block_sequence_number  [4]
    * long currentTimeMillis     [8] same value as header
    * byte CRLF                  [2] to make it easier to read buffers in an editor
    */
 
   /**
    * Size of the header for each data record in the block.
    * 
    * <p>Record header format:
    * short record type                [2] see LogRecordType
    * short record length of user data [2]
    */
   private int recordHeaderSize = 4;
 
   // block header & footer fields
   /**
    * Carriage Return Line Feed sequence used for debugging purposes.
    */
   private byte[] CRLF      = "\r\n".getBytes();
   
   private byte[] crlf = new byte[CRLF.length];
   
   /**
    * Signature for each logical block header.
    */
   private byte[] HEADER_ID = "HOWL".getBytes();
   
   private byte[] headerId = new byte[HEADER_ID.length]; 
   
   /**
    * Signature for each logical block footer.
    */
   private byte[] FOOTER_ID = "LWOH".getBytes();
   
   private byte[] footerId = new byte[FOOTER_ID.length];
   
   /**
    * switch to disable writes.
    * <p>Used to measure performance of implementation sans physical writes.
    * Subclass defines <i> doWrite </i> to be false to eliminate IO.
    */
   boolean doWrite = true;
 
   /**
    * maximum size of user data record.
    * 
    * <p>Although this member is local to a LogBuffer instance,
    * it is assumed to have the same value in all instances.
    */
   private int maxRecordSize;
   
   /**
    * end-of-block string stuffed into buffers
    * to help identify end of used portion of buffer
    * in a hex dump.
    * 
    * <p>This string is only stored if there is
    * room, and it is not accounted for in
    * the length field of the buffer header.  
    */
   private final static int EOB = 0x454F420A;  // "EOB\n" 
   
   /**
    * default constructor calls super class constructor.
    */
   BlockLogBuffer(Configuration config)
   {
     super(config);
   }
   
   /**
    * constructs instance of BlockLogBuffer with file IO disabled.
    * 
    * <p>use this constructor when doing performance measurements
    * on the implementation sans file IO.
    * 
    * @param doWrite false to disable IO for performance measurements.
    * <p>When set to false, the write() method does not issue writes
    * to the file.  This reduces the elapse time of a force() to
    * zero and allows performance of the log logic (sans IO) to be
    * measured. 
    */
   BlockLogBuffer(Configuration config, boolean doWrite)
   {
     super(config);
     this.doWrite = doWrite;
   }
 
   /**
    * puts a data record into the buffer and returns a token for record.
    * <p>Each record consists of zero or more byte[] fields.
    * Each field is preceded by a short (2 bytes) containing the
    * length of the field to allow for subsequent unpacking.
    * @see LogBuffer#put(short, byte[][], boolean)
    */
   long put(short type, byte[][] data, boolean sync) throws LogRecordSizeException
   {
     long logKey = 0L;
     int dataSize = 0;
     int recordSize = recordHeaderSize;
 
     for (int i=0; i < data.length; ++i)
       dataSize += data[i].length + 2;  // field size + short length 
     
     recordSize += dataSize;
     
     /*
      * The following synchronized() statement might be unnecessary. 
      * All calls to this put() method are synchronized by the
      * LogBufferManager.bufferManagerLock.
      * 
      * It does not seem to degrade performance any, so we leave
      * it to improve clarity of the code.
      */ 
     synchronized(buffer)
     {
       if (recordSize > maxRecordSize)
         throw new LogRecordSizeException(maxRecordSize);
       // TODO: improve exception message w/ text: configured max xxx, size yyy
       
       if (recordSize <= buffer.remaining()) 
       {
         // first 8 bits available to Logger -- possibly to carry file rotation number
         logKey = ((long)bsn << 24) | buffer.position();
   
         // put a new record into the buffer
         buffer.putShort(type).putShort((short)dataSize);
         for (int i=0; i < data.length; ++i)
         {
           buffer.putShort((short)data[i].length);
           buffer.put(data[i]);
         }
         todPut = System.currentTimeMillis();
         
         if (sync)
         {
           synchronized(waitingThreadsLock)
           {
             ++waitingThreads;
           }
         }
       }
     }
 
     return logKey;
   }
   
   /**
    * write ByteBuffer to the log file.
    */
   void write() throws IOException
   {
     assert lf != null: "LogFile lf is null";
     
     synchronized(this)
     {
       // guard against gating errors that might allow
       // multiple threads to be writing this buffer.
       if (iostatus == LogBufferStatus.WRITING)
         throw new IOException();
 
     }
 
     // increment count of threads waiting for IO to complete
     synchronized (waitingThreadsLock)
     {
       ++waitingThreads;
     }
 
     // Update bytesUsed in the buffer header
     buffer.putInt(bytesUsedOffset, buffer.position());
     
     // Try to stuff an End-Of-Block (EOB) marker
     // so we can find end of data in a hex dump
     // EOB\n
     // 2004-09-27 mg check buffer.remaining to avoid overhead of the 
     //               BufferOverflowException
     if (buffer.remaining() >= 4)
     {
       try {
         buffer.putInt(EOB); // "EOB\n".getBytes()
       } catch (BufferOverflowException e) {
       	/* ignore it -- we do not care if it does not get written */
       }
     }
     
     
     // update checksum -- hashCode
     int checksumOffset = bytesUsedOffset + 4;
     buffer.putInt(checksumOffset, 0);
     if (doChecksum) {
       int checksum = buffer.clear().hashCode();
       buffer.putInt(checksumOffset, checksum);
     }
 
     try
     {
       synchronized(this)
       {
         // BUG 300613 - update of iostatus needs to be synchronized
         iostatus = LogBufferStatus.WRITING;
       }
       buffer.clear();
       if (doWrite) lf.write(this);
       // iostatus is updated to COMPLETE by the LogBufferManage after force() is done.
     }
     catch (IOException e)
     {
       ioexception = e;
       iostatus = LogBufferStatus.ERROR;
       throw e;
     }
   }
 
   /**
    * initialize members for buffer reuse.
    * 
    * @param bsn Logic Block Sequence Number of the buffer.
    * LogBufferManager maintains a list of block sequence numbers
    * to ensure correct order of writes to disk.  Some implementations
    * of LogBuffer may include the BSN as part of a record or
    * block header.
    */ 
   LogBuffer init(int bsn, LogFileManager lfm) throws LogFileOverflowException
   {
     this.bsn = bsn;
     
     tod = todPut = System.currentTimeMillis();
     iostatus = LogBufferStatus.FILLING;
     
     ++initCounter;
 
     // initialize the logical block footer
     int bufferSize = buffer.capacity();
 
     buffer.clear();
     buffer.position(bufferSize - bufferFooterSize);
     buffer.put(FOOTER_ID).putInt(bsn).putLong(tod).put(CRLF);
     
     // initialize the logical block header
     buffer.clear();
     buffer.put(HEADER_ID);
     buffer.putInt(bsn);
     buffer.putInt(bufferSize);
     
     bytesUsedOffset = buffer.position();
 
     buffer.putInt( 0 ); // holding place for data used
     buffer.putInt( 0 ); // holding place for checkSum
     buffer.putLong( tod );
     buffer.put(CRLF);
     
     // reserve room for buffer footer
     buffer.limit(bufferSize - bufferFooterSize);
     
     // set maxRecordSize now so LogFileManager can put records
     maxRecordSize = buffer.remaining();
 
     /*
      * obtain LogFile from the LogFileManager
      * LogFileManager will put a header record into this buffer
      * so we must make this call after all other initialization is complete.
      */
     lf = lfm.getLogFileForWrite(this);
     assert lf != null: "LogFileManager returned null LogFile pointer";
     
     // set maxRecordSize again for user records
     maxRecordSize = buffer.remaining();
 
     return this;
   }
   
   /**
    * internal routine used to compare two byte[] objects.
    * 
    * @param val byte[] containing data to be validated
    * @param expected byte[] containing expected sequence of data
    * 
    * @return true if the two byte[] objects are equal, otherwise false.
    */
   private boolean compareBytes(byte[] val, byte[] expected)
   {
     for (int i=0; i<val.length; ++i)
       if (val[i] != expected[i]) { return false; }
     
     return true;
   }
   
   /**
    * Reads a block from LogFile <i> lf </i> and validates
    * header and footer information.
    * 
    * @see LogBuffer#read(LogFile, long)
    * @throws IOException
    * if anything goes wrong during the file read.
    * @throws InvalidLogBufferException
    * if any of the block header or footer fields are invalid.
    */
   LogBuffer read(LogFile lf, long position)
     throws IOException, InvalidLogBufferException, InvalidMarkException
   {
     assert lf != null : "LogFile reference lf is null";
     assert buffer != null : "ByteBuffer reference is null";
     
     this.lf = lf;
     
     // fill our ByteBuffer with a block of data from the file
     // NOTE: file position is not changed by following call
     buffer.clear();
    int bytesRead = -1;
    if (lf.channel.size() > position) // BUG 300986 JRockit throws IOException
      bytesRead = lf.channel.read(buffer, position);
     if (bytesRead == -1)
     {
       // end of file
       this.bsn = -1;
       return this;
     }
 
     if (bytesRead != buffer.capacity())
       throw new InvalidLogBufferException("FILESIZE Error: bytesRead=" + bytesRead);
     
     // verify header
     buffer.clear();
     buffer.get(headerId);
     if (!compareBytes(headerId, HEADER_ID))
       throw new InvalidLogBufferException("HEADER_ID" + bufferInfo());
     
     // get bsn (int)
     this.bsn = buffer.getInt();
     
     // get buffer size (int) compare with buffer capacity
     int bufferSize = buffer.getInt();
     if (bufferSize != buffer.capacity())
       throw new InvalidLogBufferException("bufferSize" + bufferInfo());
     
     // get data used (int)
     bytesUsed = buffer.getInt();
     if (bytesUsed < 0 || bytesUsed >= buffer.capacity())
       throw new InvalidLogBufferException("data used: " + bytesUsed + bufferInfo());
     
     // verify checkSum if it is non-zero
     int checksumOffset = buffer.position();
     int checkSum = buffer.getInt();
     if (checkSum != 0)
     {
       buffer.putInt(checksumOffset, 0);
       int expectedChecksum = buffer.clear().hashCode();
       buffer.clear().position(checksumOffset);
       buffer.putInt(checkSum);      // put the original value back
       if (checkSum != expectedChecksum)
         throw new InvalidLogBufferException("CHECKSUM" + bufferInfo());
     }
     
     // get tod
     this.tod = buffer.getLong();
     
     // get CRLF
     buffer.get(crlf);
     if (!compareBytes(crlf, CRLF))
       throw new InvalidLogBufferException("HEADER_CRLF" + bufferInfo());
 
     // mark start of first data record
     buffer.mark();
     
     // get FOOTER_ID and compare 
     buffer.position(bufferSize - bufferFooterSize);
     buffer.get(footerId);
     if (!compareBytes(footerId, FOOTER_ID))
       throw new InvalidLogBufferException("FOOTER_ID" + bufferInfo());
     
     // compare FOOTER_BSN field with HEADER_BSN
     int bsn = buffer.getInt();
     if (bsn != this.bsn)
       throw new InvalidLogBufferException("FOOTER_BSN" + bufferInfo());
     
     // compare FOOTER_TOD field with HEADER_TOD
     long tod = buffer.getLong();
     if (tod != this.tod)
       throw new InvalidLogBufferException("FOOTER_TOD" + bufferInfo());
     
     // get FOOTER_CRLF
     buffer.get(crlf);
     if (!compareBytes(crlf, CRLF))
       throw new InvalidLogBufferException("FOOTER_CRLF" + bufferInfo());
     
     // reset position to first data record
     buffer.reset();
     
     return this;
   }
 
   /**
    * determines if buffer should be forced to disk.
    * 
    * <p>If there are any waiting threads, then buffer
    * is forced when it is 50 ms old.  Otherwise, if there
    * are no waiting threads, we wait 1/4 second before we
    * force.
    *
    * @return true if buffer should be forced now.
    */ 
   boolean shouldForce()
   {
     int forceDelta = getWaitingThreads() > 0 ? 50 : 250;
     long now = System.currentTimeMillis();
 
     return ((todPut + forceDelta) < now);
   }
   
   /**
    * return statistics for this buffer.
    * 
    * @return String containing statistics as an XML node
    */
   String getStats()
   {
     String name = this.getClass().getName();
     
     String result = "<LogBuffer class='" + name + "' workerID='" + index + "'>" +
       "\n  <timesUsed value='" + initCounter + "'>Number of times this buffer was initialized for use</timesUsed>" +
       "\n  <physicalWrites value='" + doWrite + "'>Physical writes " + (doWrite ? "enabled" : "disabled" ) + "</physicalWrites>" +
       "\n  <checksums value='" + doChecksum + "'>Checksum Calculations " + (doChecksum ? "enabled" : "disabled" ) + "</checksums>" +
       "\n</LogBuffer>" +
       "\n";
     
     return result;
   }
   
   /**
    * generate a String that represents the state of this LogBuffer object
    */
   public String bufferInfo()
   {
     buffer.clear();
     
     StringBuffer sb = new StringBuffer(
         "\nClass: " + getClass().getName() + 
         "\n  workerID: " + Integer.toHexString(index) +
         "\n  LogFile: " + lf.file.getPath() +
         "\n  HEADER" + 
         "\n    HEADER_ID: 0x" + Integer.toHexString(buffer.getInt()) +
         "\n    bsn: 0x" + Integer.toHexString(buffer.getInt()) +
         "\n    size: 0x" + Integer.toHexString(buffer.getInt()) +
         "  should be: 0x" + Integer.toHexString(buffer.capacity()) +
         "\n    data used: 0x" + Integer.toHexString(buffer.getInt()) +
         "\n    checkSum: 0x" + Integer.toHexString(buffer.getInt()) +
         "\n    tod: 0x" + Long.toHexString(buffer.getLong()) +
         "\n    crlf: 0x" + Integer.toHexString(buffer.getShort()) +
         "" );
     
     buffer.position(buffer.capacity() - bufferFooterSize);
     sb.append(
         "\n  FOOTER" +
         "\n    FOOTER_ID: 0x" + Integer.toHexString(buffer.getInt()) +
         "\n    bsn: 0x" + Integer.toHexString(buffer.getInt()) +
         "\n    tod: 0x" + Long.toHexString(buffer.getLong()) +
         "\n    crlf: 0x" + Integer.toHexString(buffer.getShort()) +
         "" );
 
     return sb.toString();
   }
   
 }
