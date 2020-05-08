 package de.zib.gndms.kit.network;
 
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 
 import de.zib.gndms.stuff.threading.QueuedExecutor;
 import de.zib.gndms.kit.access.CredentialProvider;
 import org.apache.log4j.Logger;
 import org.globus.ftp.GridFTPClient;
 import org.globus.ftp.exception.ServerException;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.*;
 
 /**
  * @author try ma ik jo rr a zib
  * 
  * @version $Id$
  *          <p/>
  *          User: mjorra, Date: 20.02.2009, Time: 17:37:59
  */
 public class NonblockingClientFactory extends AbstractGridFTPClientFactory{
     private static final Logger log = Logger.getLogger( NonblockingClientFactory.class );
 
     private int timeout = 20;
     private final TimeUnit unit = TimeUnit.SECONDS;
     private long delay = 500; // in ms
     private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool( 1 );
     private final Map<String, QueuedExecutor> hostExecutors = new HashMap<String, QueuedExecutor>( );
 
 
     public GridFTPClient createClient( String host, int port, CredentialProvider cp ) throws ServerException, IOException {
 
         final QueuedExecutor exec;
         synchronized( hostExecutors ) {
             if( hostExecutors.containsKey( host ) ) {
                 log.debug( "Returning executor for host: " + host );
                 exec = hostExecutors.get( host ) ;
             } else {
                 log.debug( "Creating executor for host: " + host );
                 exec = new QueuedExecutor( scheduledExecutor );
                 exec.setDefaultDelay( delay );
                 hostExecutors.put( host, exec );
             }
         }
 
         final GridFTPClientCreator c = new GridFTPClientCreator( host, port, cp );
         final Future<GridFTPClient> f = exec.submit( c );
         try {
             try{
                 return f.get( exec.actualTimeout( f, timeout, unit ), unit );
             } catch ( TimeoutException e ) {
                 log.info( "GridFTPClient get() create exceeded timeout" );
                 f.cancel( true );
             }
          //   System.err.println( "awaiting termination" );
          //   exec.shutdown();
          //   exec.awaitTermination( timeout, TimeUnit.SECONDS );
          //   System.err.println( "done" );
         } catch ( InterruptedException e ) {
             e.printStackTrace(  );
            throw new RuntimeException( "GridFTPClient create exceeded timeout", e );
         } catch ( ExecutionException e ) {
             // this mustn't happen here due to the blocked wait op
             e.printStackTrace();
            if( e.getCause() instanceof ServerException )
                throw ServerException.class.cast( e.getCause() );

            throw new RuntimeException( e );
         }
         return null;
     }
 
 
     public void shutdown() {
 
         log.info( "shuting down executors" );
         for( String hn: hostExecutors.keySet() ) {
             hostExecutors.get( hn ).shutdown();
         }
 
         /*
         log.debug( "awaiting termination" );
         for( String hn: hostExecutors.keySet() ) {
             try {
                 hostExecutors.get( hn ).awaitTermination( timeout, TimeUnit.SECONDS );
             } catch ( InterruptedException e ) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
         */
     }
 
 
     public int getTimeout() {
         return timeout;
     }
 
 
     public void setTimeout( int timeout ) {
         
         if( timeout < 0 )
             throw new IllegalArgumentException( "Timeout must be greater or equal 0" );
         
         this.timeout = timeout;
     }
 
 
     public long getDelay() {
         return delay;
     }
 
 
     public void setDelay( int delay ) {
 
         if( delay < 0 )
             throw new IllegalArgumentException( "Delay must be greater or equal 0" );
 
         this.delay = delay;
 
         for( String hn: hostExecutors.keySet() ) {
             hostExecutors.get( hn ).setDefaultDelay( delay );
         }
     }
 }
