 /*
  * Copyright 2010-2011 Ning, Inc.
  *
  * Ning licenses this file to you under the Apache License, version 2.0
  * (the "License"); you may not use this file except in compliance with the
  * License.  You may obtain a copy of the License at:
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 
 package com.ning.metrics.serialization.writer;
 
 import com.ning.metrics.serialization.event.Event;
import org.apache.commons.io.FileUtils;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 public class MockEventWriter implements EventWriter
 {
     private final List<Event> writtenEventList = new ArrayList<Event>();
     protected final List<Event> committedEventList = new ArrayList<Event>();
     protected final List<Event> flushedEventList = new ArrayList<Event>();
     protected final List<Event> quarantinedEventList = new ArrayList<Event>();
 
     private boolean commitThrowsException;
     private boolean rollbackThrowsException;
     private boolean writeThrowsException;
     private boolean isClosed = false;
 
     public MockEventWriter()
     {
         this(false, false, false);
     }
 
     public MockEventWriter(final boolean commitThrowsException, final boolean rollbackThrowsException, final boolean writeThrowsException)
     {
         this.commitThrowsException = commitThrowsException;
         this.rollbackThrowsException = rollbackThrowsException;
         this.writeThrowsException = writeThrowsException;
     }
 
     @Override
     public synchronized void commit() throws IOException
     {
         if (commitThrowsException) {
             rollback();
             throw new IOException("IGNORE - Expected exception for tests");
         }
 
         committedEventList.addAll(writtenEventList);
         writtenEventList.clear();
     }
 
     @Override
     public void forceCommit() throws IOException
     {
         commit();
     }
 
     @Override
     public synchronized void flush() throws IOException
     {
         flushedEventList.addAll(committedEventList);
         committedEventList.clear();
     }
 
     @Override
     public synchronized void rollback() throws IOException
     {
         if (rollbackThrowsException) {
             throw new IOException("IGNORE - Expected exception for tests");
         }
 
         quarantinedEventList.addAll(writtenEventList);
         writtenEventList.clear();
     }
 
     @Override
     public synchronized void write(final Event event) throws IOException
     {
         if (writeThrowsException) {
             throw new IOException("IGNORE - Expected exception for tests");
         }
 
         writtenEventList.add(event);
     }
 
     @Override
     public synchronized void close() throws IOException
     {
         isClosed = true;
     }
 
     /**
      * @return local spool path used by the writer
      */
     @Override
     public String getSpoolPath()
     {
        return FileUtils.getTempDirectory().getAbsolutePath();
     }
 
     public boolean isClosed()
     {
         return isClosed;
     }
 
     public void setCommitThrowsException(final boolean commitThrowsException)
     {
         this.commitThrowsException = commitThrowsException;
     }
 
     public void setRollbackThrowsException(final boolean rollbackThrowsException)
     {
         this.rollbackThrowsException = rollbackThrowsException;
     }
 
     public void setWriteThrowsException(final boolean writeThrowsException)
     {
         this.writeThrowsException = writeThrowsException;
     }
 
     public Collection<Event> getWrittenEventList()
     {
         return writtenEventList;
     }
 
     public Collection<Event> getCommittedEventList()
     {
         return committedEventList;
     }
 
     public List<Event> getFlushedEventList()
     {
         return flushedEventList;
     }
 
     public List<Event> getQuarantinedEventList()
     {
         return quarantinedEventList;
     }
 }
