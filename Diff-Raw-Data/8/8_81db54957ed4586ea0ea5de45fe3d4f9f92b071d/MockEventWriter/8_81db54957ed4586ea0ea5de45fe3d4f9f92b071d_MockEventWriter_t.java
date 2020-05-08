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
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class MockEventWriter implements EventWriter
 {
     private final List<Event> writtenEventList = new ArrayList<Event>();
     protected final List<Event> committedEventList = new ArrayList<Event>();
     private boolean commitThrowsException;
     private boolean rollbackThrowsException;
     private boolean writeThrowsException;
     private int numberOfFlushedEvents = 0;
 
     public MockEventWriter()
     {
         this(false, false, false);
     }
 
     public MockEventWriter(boolean commitThrowsException, boolean rollbackThrowsException, boolean writeThrowsException)
     {
         this.commitThrowsException = commitThrowsException;
         this.rollbackThrowsException = rollbackThrowsException;
         this.writeThrowsException = writeThrowsException;
     }
 
     @Override
     public void commit() throws IOException
     {
         if (commitThrowsException) {
            throw new IOException("IGNORE. This is an expected commit exception for testing.");
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
     public void flush() throws IOException
     {
         committedEventList.clear();
         numberOfFlushedEvents++;
     }
 
     @Override
     public void rollback() throws IOException
     {
         if (rollbackThrowsException) {
            throw new IOException("IGNORE. This is an expected rollback exception for testing.");
         }
 
         writtenEventList.clear();
     }
 
     @Override
     public void write(Event event) throws IOException
     {
         if (writeThrowsException) {
            throw new IOException("IGNORE. This is an expected write exception for testing.");
         }
 
         writtenEventList.add(event);
     }
 
     public void setCommitThrowsException(boolean commitThrowsException)
     {
         this.commitThrowsException = commitThrowsException;
     }
 
     public void setRollbackThrowsException(boolean rollbackThrowsException)
     {
         this.rollbackThrowsException = rollbackThrowsException;
     }
 
     public void setWriteThrowsException(boolean writeThrowsException)
     {
         this.writeThrowsException = writeThrowsException;
     }
 
     public List<Event> getCommittedEventList()
     {
         return committedEventList;
     }
 
     public List<Event> getWrittenEventList()
     {
         return writtenEventList;
     }
 
     public int getNumberOfFlushedEvents()
     {
         return numberOfFlushedEvents;
     }
 }
