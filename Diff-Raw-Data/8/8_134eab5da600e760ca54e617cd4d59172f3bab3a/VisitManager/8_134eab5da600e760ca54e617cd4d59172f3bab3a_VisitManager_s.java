 /*
  * Copyright (c) 2011 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you
  * may not use this file except in compliance with the License.  You may
  * obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.  See the License for the specific language governing
  * permissions and limitations under the License.
  */
 
 package iudex.core;
 
 import iudex.filter.FilterContainer;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.gravitext.htmap.UniMap;
 import com.gravitext.util.Closeable;
 
 /**
  * A manager to poll work and create visit tasks.
  */
 public class VisitManager
     implements Closeable, Runnable, VisitCounter
 {
     public VisitManager( WorkPollStrategy poller )
     {
         this( null, poller );
     }
 
     public VisitManager( FilterContainer chain, WorkPollStrategy poller )
     {
         _chain = chain;
         _poller = poller;
     }
 
     public void setMaxThreads( int maxThreads )
     {
         _maxThreads = maxThreads;
     }
 
     public void setMinHostDelay( long minHostDelay )
     {
         _minHostDelay = minHostDelay;
     }
 
     public void setMaxShutdownWait( long maxShutdownWait )
     {
         _maxShutdownWait = maxShutdownWait;
     }
 
     public void setDoWaitOnGeneration( boolean doWaitOnGeneration )
     {
         _doWaitOnGeneration = doWaitOnGeneration;
     }
 
     public void setMaxGenerationsToShutdown( int generations )
     {
         _maxGenerationsToShutdown = generations;
     }
 
     public void setFilterChain( FilterContainer chain )
     {
         _chain = chain;
     }
 
     public synchronized ThreadPoolExecutor startExecutor()
     {
         if( _executor == null ) {
             LinkedBlockingQueue<Runnable> queue =
                 new LinkedBlockingQueue<Runnable>( _maxExecQueueCapacity );
 
             _executor = new ThreadPoolExecutor( _maxThreads, _maxThreads,
                                                 30, TimeUnit.SECONDS,
                                                 queue );
 
             _executor.setRejectedExecutionHandler(
                 new ThreadPoolExecutor.CallerRunsPolicy() );
         }
         return _executor;
     }
 
     public synchronized void start()
     {
         if( _manager != null ) {
             throw new IllegalStateException( "Manager already started." );
         }
         _manager = new Thread( this, "manager" );
 
         startExecutor();
 
         if( _doShutdownHook ) {
             _shutdownHook = new ShutdownHook();
             Runtime.getRuntime().addShutdownHook( _shutdownHook );
         }
 
         _shutdown = false;
 
         _manager.start();
     }
 
     /**
      * Returns executor, only available after start() has returned.
      */
     public ThreadPoolExecutor executor()
     {
         return _executor;
     }
 
     public void join() throws InterruptedException
     {
         Thread executor = null;
         synchronized( this ) {
             executor = _manager;
         }
         if( executor != null ) executor.join();
     }
 
     public void shutdown() throws InterruptedException
     {
         shutdown( false );
     }
 
     /**
      * Shutdown executor and visitor threads. Note the provided filter chain
      * should be closed externally.
      */
     @Override
     public synchronized void close()
     {
         try {
             shutdown( false );
         }
         catch( InterruptedException x ) {
             _log.warn( "On executor shutdown: " + x );
         }
     }
 
     @Override
     public void add( UniMap order )
     {
         _visitQ.add( order );
     }
 
     @Override
     public void release( UniMap acquired, UniMap newOrder )
     {
         //FIXME: Risk of null, or need for synchronization?
         _visitQ.release( acquired, newOrder );
     }
 
     @Override
     public void run()
     {
         //FIXME: Replace with Runnable for privacy?
 
         _running = true;
 
         try {
             final long maxTakeWait = maxTakeWait();
             long now = System.currentTimeMillis();
             while( _running ) {
 
                 if( checkWorkPoll( now ) ) {
                     _log.warn( "Shutting down after {} generations",
                                _generation );
 
                     synchronized( this ) {
                         _manager = null;
                         _running = false;
                     }
 
                     shutdown();
                     return;
                 }
 
                 UniMap order = _visitQ.acquire( maxTakeWait );
                 if( _running && ( order != null ) ) {
                     now = order.get( ContentKeys.VISIT_START ).getTime();
                     _executor.execute( new VisitTask( order ) );
                     _log.debug( "Queued order for rldomain {}, depth: {}",
                                 order.get( ContentKeys.URL ).domain(),
                                _executor.getTaskCount() );
                 }
                 else {
                     now = System.currentTimeMillis();
                 }
             }
         }
         catch( InterruptedException x ) {
             _log.warn( "Executor run loop: " + x );
         }
         finally {
             _log.info( "Manager thread exit" );
             _manager = null;
             _running = false;
         }
     }
 
     /**
      * Check if work needs to be polled by delegating to the
      * WorkPollStrategy. If strategy returns a new VisitQueue, then await
      * for existing VisitTasks to complete.
      */
     private synchronized boolean checkWorkPoll( long now )
         throws InterruptedException
     {
         long delta = 0;
         boolean doShutdown = false;
 
         if( _shutdown || now < _nextCheckWorkPoll ) {
             return doShutdown;
         }
 
         if( _visitQ != null ) {
             delta = _poller.nextPollWork( _visitQ, now - _lastPollTime );
         }
 
         if( ( _visitQ == null ) || ( delta <= 0 ) ) {
 
             if( ( _visitQ == null ) || _poller.shouldReplaceQueue( _visitQ ) ) {
 
                 if( ( _visitQ != null ) && _doWaitOnGeneration ) {
                     awaitExecutorEmpty();
 
                     if( _log.isDebugEnabled() ) {
                         _log.debug( "Dump of old visit queue:\n{}",
                                     _visitQ.dump() );
                     }
                 }
 
                 if( ( _maxGenerationsToShutdown > 0 ) &&
                     ( _generation >= _maxGenerationsToShutdown ) ) {
                     doShutdown = true;
                 }
                 else {
                     ++_generation;
                     _visitQ = _poller.pollWork( null );
                 }
             }
             else {
                 _poller.pollWork( _visitQ );
             }
             now = System.currentTimeMillis();
 
             _lastPollTime = now;
 
             delta = _poller.nextPollWork( _visitQ, 0 );
         }
 
         _nextCheckWorkPoll = now + delta;
         return doShutdown;
     }
 
     private class VisitTask implements Runnable
     {
         public VisitTask( UniMap order )
         {
             _order = order;
         }
 
         @Override
         public void run()
         {
             try {
                 _chain.filter( _order );
             }
             catch( RuntimeException x ) {
                 _log.error( "While processing: ", x );
                 //FIXME: Add this as executor thread error handler? InterOp
                 //with http clients?
             }
         }
 
         private final UniMap _order;
     }
 
     private long maxTakeWait()
     {
         long mwait = _minHostDelay + 10;
         mwait = Math.min( mwait, _maxShutdownWait - 10 );
 
         return mwait;
     }
 
     private synchronized void awaitExecutorEmpty() throws InterruptedException
     {
         //FIXME: Or use custom task that notify's?
         // * Or really shutdown, as empty queue does not guarantee completion?
         // * Shutdown only guaranteed if HTTPClient uses same pool?
         // * Executor can't be restarted after, so implies new HTTPClient on
         //   each generation?!?
         //FIXME: Or support true asynchronous work polling by pre-filtering
         // work by pending URL.
 
         long now = System.currentTimeMillis();
         long end = now + _maxShutdownWait;
         while( now < end ) {
             if( _executor.getQueue().isEmpty() ) break;
             wait( Math.min( 50, end - now ) );
             now = System.currentTimeMillis();
         }
         if( now >= end ) {
             _log.warn( "Executor not empty after {}ms", _maxShutdownWait );
         }
         else {
             // FIXME: Lame additional padding, hoping for any remaining
             // HTTP + chain jobs to timeout
             wait( end - now  );
         }
     }
 
     private void shutdown( boolean fromVM )
         throws InterruptedException
     {
         Thread manager = null;
 
         synchronized( this ) {
             _shutdown = true;         //Avoid more visitors
 
            //Shutdown executor
             _running = false;
             notifyAll();
             manager = _manager;
         }
 
         if( manager != null ) {
             manager.join( _maxShutdownWait );
         }
 
         synchronized( this ) {
             if( _manager != null ) {
                 _log.warn(  "Manager not exiting: interrupt" );
                 _manager.interrupt();
             }
         }
 
        //FIXME: Is this really how we want to do this?
         _executor.shutdown();
         _executor.awaitTermination( _maxShutdownWait,
                                     TimeUnit.MILLISECONDS );
 
         if( !fromVM ) {
             synchronized( this ) {
                 if( _shutdownHook != null ) {
                     Runtime.getRuntime().removeShutdownHook( _shutdownHook );
                     _shutdownHook = null;
                 }
             }
         }
     }
 
     private class ShutdownHook extends Thread
     {
         public ShutdownHook()
         {
             super( "visitor-shutdown" );
         }
 
         @Override
         public void run()
         {
             _log.info( "Visitor shutdown hook closing" );
             try {
                 shutdown( true );
                 //FIXME: Need a better solution for allow main to exit.
                 Thread.sleep( 1000 );
             }
             catch( InterruptedException x ) {
                 _log.warn( "On shutdown: " + x );
             }
         }
     }
 
     private FilterContainer _chain;
     private final WorkPollStrategy _poller;
     private ThreadPoolExecutor _executor = null;
 
     private long _nextCheckWorkPoll = 0;
 
     private int  _maxThreads              = 10;
     private long _minHostDelay            =  2 * 1000; //2s
     private long _maxShutdownWait         = 19 * 1000; //19s
     private boolean _doWaitOnGeneration   = false;
     private boolean _doShutdownHook       = true;
     private int   _maxExecQueueCapacity   = Integer.MAX_VALUE;
     private int   _maxGenerationsToShutdown = 0;
 
     private Thread _manager               = null;
     private ShutdownHook _shutdownHook    = null;
     private volatile boolean _running     = false;
     private volatile boolean _shutdown    = true;
 
     private long _lastPollTime            = 0;
     private int _generation               = 0;
 
     private VisitQueue _visitQ            = null;
 
     private Logger _log = LoggerFactory.getLogger( getClass() );
 }
