 package de.zib.gndms.taskflows.filetransfer.server.network;
 
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
 
 
 
 import de.zib.gndms.kit.security.CredentialProvider;
 import de.zib.gndms.stuff.threading.DV;
 import de.zib.gndms.stuff.threading.QueuedExecutor;
 import de.zib.gndms.stuff.threading.TimedForkable;
 import org.globus.ftp.GridFTPClient;
 import org.globus.ftp.exception.ServerException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
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
 public class NonblockingClientFactory extends AbstractNonblockingClientFactory{
 
     private static final Logger log = LoggerFactory.getLogger( NonblockingClientFactory.class );
     private final Map<String, QueuedExecutor> hostExecutors = new HashMap<String, QueuedExecutor>( );
 
 
     public GridFTPClient createClient( String host, int port, CredentialProvider cp ) throws TimeoutException, ServerException, IOException {
 
         final QueuedExecutor exec;
         synchronized( hostExecutors ) {
             if( hostExecutors.containsKey( host ) ) {
                 log.debug( "Returning executor for host: " + host );
                 exec = hostExecutors.get( host ) ;
             } else {
                 log.debug( "Creating executor for host: " + host );
                 exec = new QueuedExecutor( );
                 exec.setDefaultDelay( getDelay() );
                 hostExecutors.put( host, exec );
             }
         }
 
         final GridFTPClientCreator creator = new GridFTPClientCreator( host, port, cp, inc() );
         final TimedForkable<GridFTPClient> fork = new TimedForkable<GridFTPClient>( creator,
                 getTimeout() * 1000 );
         try {
             final DV<GridFTPClient,Exception> f = exec.submit( fork );
             return f.getValue();
         } catch ( TimeoutException e ) {
             creator.getLog().debug( "", e );
             throw e;
         } catch ( InterruptedException e ) {
             Thread.interrupted();
             creator.getLog().debug( "", e );
            throw new RuntimeException( "GridFTPClient create interrupted " + host + ":" + port + ".", e );
         } catch ( ExecutionException e ) {
             creator.getLog().debug( "", e );
             if( e.getCause() instanceof ServerException )
                 throw ServerException.class.cast( e.getCause() );
             throw new RuntimeException( e );
         } catch ( Exception e ) {
            throw new RuntimeException( "Unexpected exception in GridFTPClient creation for " + host + ":" + port + ".", e );
         }
     }
 
 
     public void shutdown() {
 
         log.info( "shutting down executors" );
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
 
 
     @Override
     public void setDelay( int delay ) {
         super.setDelay( delay );
         updateDelay( );
     }
 
 
     private void updateDelay() {
         for( String k : hostExecutors.keySet() )
             hostExecutors.get( k ).setDefaultDelay( getDelay() );
     }
 }
