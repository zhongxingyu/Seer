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
 
 /**
  * \file DatadockManager.java
  * \brief manages the responsebilities of the datadock.
  */
 
 
 package dk.dbc.opensearch.components.datadock;
 
 
import dk.dbc.opensearch.common.config.HarvesterConfig;
 import dk.dbc.opensearch.components.harvest.HarvesterIOException;
 import dk.dbc.opensearch.components.harvest.HarvesterInvalidStatusChangeException;
 import dk.dbc.opensearch.components.harvest.IHarvest;
 import dk.dbc.opensearch.common.types.Pair;
 import dk.dbc.opensearch.common.types.TaskInfo;
 import dk.dbc.opensearch.common.pluginframework.PluginTask;
 
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Collections;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 
 
 /**
  * \brief the DataDockManager manages the startup, running and
  * closedown of the associated harvester and threadpool
  */
 public final class DatadockManager
 {
     private boolean shutdownRequested = false;
     private static Logger log = Logger.getLogger( DatadockManager.class );
 
     private final DatadockPool pool;
     private final IHarvest harvester;
     private List<TaskInfo> registeredJobs;
     private final Map<String, List< PluginTask > > flowMap;
 
     private final Map< Pair< String,String >, Boolean > jobExecutionCheckSet = 
         Collections.synchronizedMap( new HashMap< Pair< String,String >, Boolean >() );
 
 
     /**
      * Constructs the the DatadockManager instance.
      *
      * @param pool the threadpool used for executing datadock jobs
      * @param harvester the harvester to supply the datadock with jobs
      * @param flowMap the map used for checking which submitter format pairs are valid
      * @throws ConfigurationException
      * @throws HarvesterIOException
      * @throws IOException
      * @throws ParserConfigurationException
      * @throws SAXException
      * 
      */
     public DatadockManager( DatadockPool pool, IHarvest harvester, Map< String, List< PluginTask > > flowMap ) throws ConfigurationException, ParserConfigurationException, SAXException, IOException, HarvesterIOException
     {
         log.trace( "entering DatadockManager" );
 
         this.pool = pool;
         this.harvester = harvester;
         /** TODO: Should it really be part of the object initialization to start the harvester?*/
         harvester.start();
         registeredJobs = new ArrayList<TaskInfo>();
         this.flowMap = flowMap;
     }
 
 
     /**
      * The update method constitutes the main workflow of the DatadockManager:
      * upon being called, it will:
      * 
      * 1) check if there are any jobs waiting for execution and
      *  a) continue on to 2), or
      *  b) request the harvester for another 100 jobs
      * 2) loop while there are still jobs to execute, and
      * 3) check if the job has a workflow (i.e. there exists plugins to process it), and
      *  a) submit it for execution with the {@link DatadockPool}, or
      *  b) log a warning that the job could not be processed
      * 4) remove the job from the jobs waiting for execution, and
      * 5) call the {@link DatadockPool#checkJobs()} which will block until all jobs have finished and
      * 6) return the number of submitted jobs
      *
      * @throws HarvesterIOException
      * @throws HarvesterInvalidStatusChangeException
      * @throws InterruptedException
      */
    public int update()throws HarvesterIOException, HarvesterInvalidStatusChangeException, InterruptedException, ConfigurationException
     {
         log.trace( "DatadockManager update called" );
 
         // Check if there are any registered jobs ready for docking
         // if not... new jobs are requested from the harvester
         if( registeredJobs.isEmpty() )
         {
             log.trace( "no more jobs. requesting new jobs from the harvester" );
            registeredJobs = this.harvester.getJobs( HarvesterConfig.getMaxToHarvest() );
         }
 
         log.debug( "DatadockManager.update: Size of registeredJobs: " + registeredJobs.size() );
         int jobs_submitted = 0;
 
         while ( registeredJobs.size() > 0 && ! shutdownRequested )
         {
             log.trace( String.format( "processing job: %s", registeredJobs.get( 0 ).getIdentifier() ) );
 
             try
             {
             TaskInfo job = registeredJobs.get( 0 );
 
             if( hasWorkflow( job ) )
             {
                 pool.submit( job.getIdentifier() );
                 registeredJobs.remove( 0 );
                 ++jobs_submitted;
 
                 log.debug( String.format( "submitted job: '%s'", job ) );
 
             }
             else
             {
                 log.warn( String.format( "Jobs for submitter, format \"%s,%s\" has no workflow from plugins and will henceforth be rejected.", job.getSubmitter(), job.getFormat() ) );
                 registeredJobs.remove( 0 );
             }
             }
             catch ( RuntimeException re )
             {
                 String error = "Runtime exception caught " + re.getMessage();
                 log.error( error, re );
             }
         }
 
         //checking for finished jobs in the pool
         pool.checkJobs();
 
         return jobs_submitted;
     }
 
 
     private synchronized Boolean hasWorkflow( TaskInfo job )
     {
         Boolean exists = Boolean.FALSE;
         final Pair<String, String> entry = new Pair<String,String>( job.getSubmitter(), job.getFormat() );
 
         if( ! this.jobExecutionCheckSet.containsKey( entry ) )
         {
             List< PluginTask > checkList = flowMap.get( job.getSubmitter() + job.getFormat() );
             //            if ( DatadockJobsMap.hasPluginList( job.getSubmitter(), job.getFormat() ) )
             if( ! ( checkList == null ) )
             {
                 exists = Boolean.TRUE;
             }else
             {
                 exists = Boolean.FALSE;
             }
 
             this.jobExecutionCheckSet.put( entry, exists );
 
         }else
         {
             return this.jobExecutionCheckSet.get( entry );
         }
 
         return exists;
     }
 
     /**
      * shuts down the resources of the datadock and the datadock
      * itself.
      * @throws InterruptedException
      * @throws HarvesterIOException
      */
     public void shutdown() throws InterruptedException, HarvesterIOException
     {
 	shutdownRequested = true;
 
 	log.debug( String.format( "Registered Jobs still in manager before pool shutdown: %s", registeredJobs.size() ) );
         log.debug( "Shutting down the pool" );
         pool.shutdown();
         log.debug( "The pool is down" );
 	log.debug( String.format( "Registered Jobs still in manager after pool shutdown: %s", registeredJobs.size() ) );
 
 	// Release jobs in Harvester:
 	for ( TaskInfo taskInfo : registeredJobs )
 	{
 	    harvester.releaseJob( taskInfo.getIdentifier() );
 	}
 
         log.debug( "Stopping harvester" );
         harvester.shutdown();
         log.debug( "The harvester is stopped" );
     }
 
 }
