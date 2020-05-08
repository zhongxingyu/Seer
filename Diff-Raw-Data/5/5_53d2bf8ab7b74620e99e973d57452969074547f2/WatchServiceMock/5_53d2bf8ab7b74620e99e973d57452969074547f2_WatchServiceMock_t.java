 /*
  * xTest
  * Copyright (C) 2013 Stefano Fornari
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY Stefano Fornari, Stefano Fornari
  * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  */
 package ste.xtest.nio;
 
 import static java.nio.file.StandardWatchEventKinds.*;
 import java.io.IOException;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.WatchEvent;
 import java.nio.file.WatchKey;
 import java.nio.file.WatchService;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.concurrent.TimeUnit;
 
 /**
  * WhatService with the following characteristics:
  *
  * - interruptible (set interrupt to true)
  * - generates an event every x milliseconds (set interval)
  *
  * @author ste
  */
 public class WatchServiceMock implements WatchService {
 
     public boolean interrupt;
     public int interruptAfterNPolls;
     public long interval;
     public LinkedList<WatchKeyMock> paths = null;
 
     private boolean closed;
 
     public WatchServiceMock(String[] paths) {
         if (paths == null) {
             throw new IllegalArgumentException("paths cannot be null");
         }
         interrupt = closed = false;
         interval = 1000;
         interruptAfterNPolls = -1;
 
         this.paths = new LinkedList<WatchKeyMock>();
         for (String path: paths) {
             this.paths.push(newWatchKeyMock(path));
         }
 
     }
 
     @Override
     public void close() throws IOException {
         closed = true;
     }
 
     @Override
     public WatchKey poll() {
         if (closed) {
             throw new IllegalStateException("the service is closed");
         }
 
         return paths.peek();
     }
 
     @Override
     public WatchKey poll(long timeout, TimeUnit unit) throws InterruptedException {
        //
        // TODO: mock better the poll method
        //
        return take();
     }
 
     @Override
     public WatchKey take() throws InterruptedException {
         if (closed) {
             throw new IllegalStateException("the service is closed");
         }
 
         if (interruptAfterNPolls == 0) {
             throw new InterruptedException("mocked interrumption");
         } else if (interruptAfterNPolls > 0) {
             --interruptAfterNPolls;
         }
 
         Thread.sleep(interval);
 
         return paths.peek();
     }
 
     /**
      * @return true if previously closed, false otherwise
      */
     public boolean isClosed() {
         return closed;
     }
 
     // --------------------------------------------------------- Private methods
 
     private WatchKeyMock newWatchKeyMock(final String path) {
 
         WatchEventMock<Path> event = new WatchEventMock<Path>();
         event.context = Paths.get(path);
         event.kind = ENTRY_MODIFY;
 
         WatchKeyMock watchKey = new WatchKeyMock();
         watchKey.valid = true;
         watchKey.path = event.context;
         watchKey.events = new ArrayList<WatchEvent<?>>();
 
         watchKey.events.add(event);
 
         return watchKey;
     }
 
 
 }
