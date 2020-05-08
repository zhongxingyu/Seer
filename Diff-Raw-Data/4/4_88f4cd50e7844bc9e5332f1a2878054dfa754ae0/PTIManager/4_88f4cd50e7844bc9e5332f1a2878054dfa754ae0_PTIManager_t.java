 /**
  * \file PTIManager.java
  * \brief The PTIManager class
  * \package pti;
  */
 package dk.dbc.opensearch.components.pti;
 
 /*
    
 This file is part of opensearch.
 Copyright © 2009, Dansk Bibliotekscenter a/s, 
 Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
 opensearch is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 opensearch is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 
 import dk.dbc.opensearch.common.config.PTIManagerConfig;
 import dk.dbc.opensearch.common.db.Processqueue;
 import dk.dbc.opensearch.common.db.IProcessqueue;
 import dk.dbc.opensearch.common.types.CompletedTask;
 import dk.dbc.opensearch.common.types.InputPair;
 import dk.dbc.opensearch.common.types.Pair;
 
 import java.io.IOException;
 import java.lang.ClassNotFoundException;
 import java.lang.Integer;
 import java.net.MalformedURLException;
 import java.sql.SQLException;
 import java.util.Vector;
 import java.util.concurrent.RejectedExecutionException;
 
 import javax.xml.rpc.ServiceException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 
 
 /**
  * \brief the PTIManager manages the startup, running and
  * closedown of the associated threadpool
  */
 public class PTIManager
 {
     static Logger log = Logger.getLogger( PTIManager.class );
 
     
     private PTIPool pool= null;
     private IProcessqueue processqueue = null;
     private int rejectedSleepTime;
     private int resultsetMaxSize;
 
     
     /**
      * Constructs the the PTIManager instance.
      */
     public PTIManager( PTIPool pool, IProcessqueue processqueue ) throws ClassNotFoundException, SQLException, ConfigurationException
     {
         log.debug( "Constructor( pool ) called" );
 
         this.processqueue = processqueue;
         this.pool = pool;
 
         // get config parameters
         log.debug( String.format( "the PTImanagerQueueResultSetMaxSzie: %s ",PTIManagerConfig.getQueueResultsetMaxSize() ) );
         resultsetMaxSize = new Integer( PTIManagerConfig.getQueueResultsetMaxSize() );
         rejectedSleepTime = new Integer( PTIManagerConfig.getRejectedSleepTime() );
 
         log.debug( "Removing entries marked as active from the processqueue" );
         int removed = processqueue.deActivate();
         log.debug( String.format( "marked  %s 'active' threads as ready to process", removed ) );
     }
     
 
     /**
      * Updates the ptimanager.  First of it checks for new jobs and
      * executes them if it found any.Afterwards it checks already
      * submitted jobs, and if they have returned with a value (job
      * comenced succesfully), the corresponding element in the
      * processqueue are commited.
      */
     public void update() throws SQLException, ConfigurationException, ClassNotFoundException, InterruptedException, ServiceException, MalformedURLException, IOException
     {
         log.debug( "update() called" );
 
         // checking for new jobs
         Vector< InputPair< String, Integer > > newJobs = processqueue.pop( resultsetMaxSize );
         log.debug( String.format( "Found '%s' new jobs", newJobs.size() ) );
         //log.debug( "size of newJobs: "+ newJobs.size() );
         // Starting new Jobs
         //log.debug( newJobs.get(0).getFirst() );
         //log.debug( "newJobs: " + newJobs);
         for( Pair<String, Integer> job : newJobs )
         {
             boolean submitted = false;
             while( ! submitted )
             {
                 try
                 {
                     //          log.debug( job );
                     pool.submit( job.getFirst(), job.getSecond() );
                     submitted = true;
                     log.debug( String.format( "submitted job: fedorahandle='%s' and queueID='%s'",job.getFirst(), job.getSecond() ) );
                 }
                 catch( RejectedExecutionException re )
                 {
                    log.warn( String.format( "job: fedorahandle='%s' and queueID='%s' rejected (all threads are busy)- trying again"
                                             ,job.getFirst(), job.getSecond() ) );
                     Thread.currentThread();
                     Thread.sleep( rejectedSleepTime );
                 }
             }
         }
 
         // Checking jobs and commiting jobs
         Vector< CompletedTask<InputPair< Long, Integer > > > finishedJobs = pool.checkJobs();
         // log.debug( "size of finishedJobs: " + finishedJobs.size() );
         for ( CompletedTask<InputPair< Long, Integer > > task : finishedJobs)
         {            
             InputPair< Long, Integer > pair = task.getResult();
             Long result = pair.getFirst();
             int queueID = pair.getSecond();
             if ( result != null ) // success
             {
                 log.debug( String.format( "Commiting queueID: '%s', result: '%s' to processqueue", queueID, result ) );
                 processqueue.commit( queueID );
             }
             else /** the job returned null - rollback ? */
             {
                 log.debug( String.format( "job with queueID: '%s', result: '%s' Not successfull... rolling back", queueID, result ) );
                 processqueue.notIndexed( queueID );
                 //processqueue.rollback( queueID );
             }    
         }
     }
 
     
     /**
      * Shuts down the ptiManager. Shuts down the pool and waits until
      * all jobs are finished.
      */    
     public void shutdown() throws InterruptedException
     {
         log.debug( "Shutting down the pool" );
         // waiting for threads to finish before returning
         pool.shutdown();
         try
         {
             update();
         }
         catch( Exception e)
         {
             log.fatal( String.format( "Caught exception in PTIManager:\n %s \n'%s'",e.getClass(),  e.getMessage() ) );
             StackTraceElement[] trace = e.getStackTrace();
             for( int i = 0; i < trace.length; i++ )
             {
             	log.fatal( trace[i].toString() );
             }
         }
         
         log.debug( "The pool is down" );
     }
 }
