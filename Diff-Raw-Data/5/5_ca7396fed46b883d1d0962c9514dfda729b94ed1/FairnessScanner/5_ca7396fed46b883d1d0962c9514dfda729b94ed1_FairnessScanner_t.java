 /*******************************************************************************
  * Copyright (c) 2010-2011 Sonatype, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * and Apache License v2.0 which accompanies this distribution.
  * The Eclipse Public License is available at
  *   http://www.eclipse.org/legal/epl-v10.html
  * The Apache License v2.0 is available at
  *   http://www.apache.org/licenses/LICENSE-2.0.html
  * You may elect to redistribute this code under either of these licenses.
  *******************************************************************************/
 package org.sonatype.sisu.resource.scanner.scanners;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.atomic.AtomicInteger;
 
import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Singleton;
 
 import org.sonatype.sisu.resource.scanner.Listener;
 import org.sonatype.sisu.resource.scanner.Scanner;
 
 @Named( "fairness" )
 @Singleton
 public class FairnessScanner
     implements Scanner
 {
     private final Semaphore sem = new Semaphore( 1 );
 
     private final AtomicInteger waitingCount = new AtomicInteger( 0 );
 
     private final List<ThreadWithList> threads = new ArrayList<ThreadWithList>();
 
     private final int installmentSize;
 
     private static class DirInfo
     {
 
         private final File[] listing;
 
         private int index;
 
         public DirInfo( File dir )
         {
             this.listing = dir.listFiles();
             this.index = 0;
         }
 
         public int getIndex()
         {
             return index;
         }
 
         public void setIndex( int index )
         {
             this.index = index;
         }
 
         public File[] getListing()
         {
             return listing;
         }
     }
 
     private final BlockingQueue<DirInfo> queue;
 
     private final class ThreadWithList
         extends Thread
     {
         private final List<File> files;
 
         private int useCount = 0;
 
         public ThreadWithList()
         {
             super();
             files = new ArrayList<File>();
         }
 
         public int getUseCount()
         {
             return useCount;
         }
 
         public void run()
         {
             while ( true )
             {
                 if ( FairnessScanner.this.recurse() )
                 {
                     break;
                 }
             }
         }
     }
 
    @Inject
    public FairnessScanner( @Named( "${sisu.scanner.fairness.threads}" ) int threads,
                              @Named( "${sisu.scanner.fairness.installmentSize}" ) int installmentSize )
     {
         this.installmentSize = installmentSize;
         this.queue = new LinkedBlockingQueue<DirInfo>();
         for ( int i = 0; i < threads; i++ )
         {
             ThreadWithList t = new ThreadWithList();
             this.threads.add( t );
         }
     }
 
     public void scan( final File directory, Listener listener )
     {
         try
         {
             sem.acquire();
             queue.add( new DirInfo( directory ) );
             for ( ThreadWithList t : threads )
             {
                 t.start();
             }
             sem.acquire();
         }
         catch ( InterruptedException e )
         {
         }
 
         List<File> ret = new ArrayList<File>();
 
         for ( ThreadWithList t : threads )
         {
             ret.addAll( t.files );
         }
 
         // return ret;
     }
 
     private boolean recurse()
     {
         waitingCount.incrementAndGet();
         // Remove the next item from the queue.
         ThreadWithList thread = ( (ThreadWithList) Thread.currentThread() );
         DirInfo dirInfo = null;
         try
         {
             if ( waitingCount.get() == threads.size() && queue.isEmpty() )
             {
                 sem.release();
                 return true;
             }
             dirInfo = queue.take();
         }
         catch ( InterruptedException exc )
         {
             Thread.currentThread().interrupt();
             return true;
         }
         finally
         {
             waitingCount.decrementAndGet();
         }
 
         int index = dirInfo.getIndex();
         File[] listing = dirInfo.getListing();
         int upperBound = Math.min( index + installmentSize, listing.length );
         for ( int i = index; i < upperBound; i++ )
         {
             thread.files.add( listing[i] );
             if ( listing[i].isDirectory() )
             {
                 DirInfo subdirInfo = new DirInfo( listing[i] );
                 try
                 {
                     queue.put( subdirInfo );
                 }
                 catch ( InterruptedException exc )
                 {
                     Thread.currentThread().interrupt();
                     return true;
                 }
             }
         }
         if ( upperBound != listing.length )
         {
             dirInfo.setIndex( upperBound );
             queue.add( dirInfo );
         }
         thread.useCount += ( upperBound - index );
 
         return false;
     }
 
     public void close()
     {
         for ( ThreadWithList t : threads )
         {
             if ( t.isAlive() )
             {
                 t.interrupt();
             }
         }
         for ( ThreadWithList t : threads )
         {
             try
             {
                 t.files.clear();
                 t.join();
                 System.out.print( t.getUseCount() + " " );
             }
             catch ( InterruptedException exc )
             {
                 Thread.currentThread().interrupt();
                 return;
             }
         }
         System.out.println();
         queue.clear();
     }
 
     private static final List<FairnessScanner> scanners = new ArrayList<FairnessScanner>();
 
     public static void flush()
     {
         for ( FairnessScanner scanner : scanners )
         {
             scanner.close();
         }
     }
 
 }
