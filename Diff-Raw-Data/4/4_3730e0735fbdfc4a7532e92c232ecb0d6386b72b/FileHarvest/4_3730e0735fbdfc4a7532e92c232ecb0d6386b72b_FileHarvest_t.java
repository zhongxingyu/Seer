 /**
  * \file FileHarvest.java
  * \brief The FileHarvest class
  * \package components.harvest;
  */
 package dk.dbc.opensearch.components.harvest;
 
 
 import dk.dbc.opensearch.common.config.DatadockConfig;
 import dk.dbc.opensearch.common.helpers.XMLFileReader;
 import dk.dbc.opensearch.common.types.DatadockJob;
 import dk.dbc.opensearch.common.types.Pair;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Vector;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 
 /**
  * FileHarvest class. Implements the IHarvester interface and acts as a
  * fileharvester for the datadock. It implements the methods start,
  * shutdown and getJobs. It is an eventdriven class.
  * 
  * This file harvester assumes some things about path given as an argument:
  * 
  * The path has to be a directory with the following structure:
  * 
  * polling path
  * | - submitter1
  * | | -format1
  * | | | - job1
  * | | | - job2
  * | | -format2
  * | | | - job3
  * | - submitter2
  * .
  * .
  * .
  * 
  * There are no restrictions on the number of submitters, formats or
  * jobs - and the jobs can be files or directorys.
  * 
  * The harvester only returns a job after the second consecutive time
  * it has been found and its filesize is unchanged.
  */
 public class FileHarvest implements IHarvester
 {
     static Logger log = Logger.getLogger( FileHarvest.class );
     
     
     private File path;
     private Vector< Pair< File, Long > > submitters;
     private Vector< Pair< File, Long > > formats;
     private Vector< Pair< String, String > > submittersFormatsVector;
     private HashSet< File > jobSet;
     private HashSet< Pair< File, Long > > jobApplications;
 
     
     /**
      * Constructs the FileHarvest class, and starts polling the given path for 
      * files and subsequent file-changes.
      * 
      * @param path The path to the directory to harvest from.
      * 
      * @throws IllegalArgumentException if the path given is not a directory.
      * @throws IOException 
      * @throws SAXException 
      * @throws ParserConfigurationException 
      * @throws ConfigurationException 
      */
     public FileHarvest( File path ) throws IllegalArgumentException, ParserConfigurationException, SAXException, IOException, ConfigurationException 
     {
         System.out.println(String.format( "Constructor( path='%s' )", path.getAbsolutePath() ) );
         log.debug( String.format( "Constructor( path='%s' )", path.getAbsolutePath() ) );
         
         if ( ! path.isDirectory() )
             throw new IllegalArgumentException( String.format( "'%s' is not a directory !", path.getAbsolutePath() ) );
         
         this.path = path;
         this.jobApplications = new HashSet< Pair< File, Long > >();
         this.submitters = new Vector< Pair< File, Long > >();
         this.formats = new Vector< Pair< File, Long > >();
         this.jobSet = new HashSet< File >();        
 
         String datadockJobsFilePath = DatadockConfig.getPath();
     	File datadockJobsFile = new File( datadockJobsFilePath );
     	NodeList jobNodeList = XMLFileReader.getNodeList( datadockJobsFile, "job" );
     	submittersFormatsVector = new Vector< Pair< String, String > >();
     	for( int i = 0; i < jobNodeList.getLength(); i++ )
     	{
     		Element pluginElement = (Element)jobNodeList.item( i );		        
             String formatAtt = pluginElement.getAttribute( "format" );
             String submitterAtt = pluginElement.getAttribute( "submitter" );
             if( ! submittersFormatsVector.contains( formatAtt ) )
             {
             	Pair< String, String > submitterFormatPair = new Pair< String, String >( submitterAtt, formatAtt );
             	submittersFormatsVector.add( submitterFormatPair );
             }
     	}
     }
 
     
     /**
      * Starts The datadock. It initializes vectors and add found jobs to the application vector.
      */
     public void start()
     {
         log.debug( "start() called" );
 
         initVectors();        
         log.debug( "Vectors initialized" );
         
         for( Pair< File, Long > job : findAllJobs() )
         {
             System.out.println( String.format( "adding path='%s' to jobSet and jobApllications", job.getFirst().getAbsolutePath() ) );
             log.debug( String.format( "adding path='%s' to jobSet and jobApllications", job.getFirst().getAbsolutePath() ) );
             jobSet.add( job.getFirst() );
         }
         
         jobApplications = findAllJobs();
     }
 
     
     /**
      * Shuts down the fileharvester
      */
     public void shutdown()
     {
         log.debug( "shutdown() called" );
     }
     
 
     /**
      * getJobs. Locate jobs and returns them.  First off, the
      * candidates already registered analyzed. if their filesize has
      * remained the same as last time it is removed from the
      * applications vector and added to the newJobs vector and
      * returned when the method exits.
      * 
      * afterwards it finds new jobs and adds them to the applications
      * vector, and generate a new snapshot of the harvest directory.
      * 
      * @returns A vector of Datadockjobs containing the necessary information to process the jobs.
      */
 
     public Vector<DatadockJob> getJobs()
     {
         log.debug( "FileHarvest getJobs called " );
         
         // validating candidates - if the filelength have remained the
         // same for two consecutive calls it is added to newJobs
         Vector< DatadockJob > newJobs = new Vector<DatadockJob>();
         HashSet< Pair< File, Long > > removeJobs = new HashSet< Pair< File, Long > >();
         System.out.println( jobApplications);
         for( Pair< File, Long > job : jobApplications )
         {
             if( job.getFirst().length() == job.getSecond() )
             {
                 DatadockJob datadockJob = new DatadockJob( job.getFirst().toURI(),
                                                            job.getFirst().getParentFile().getParentFile().getName(),
                                                            job.getFirst().getParentFile().getName() );
                 System.out.println( String.format( "found new job: path='%s', submitter='%s', format='%s'",
                                                    datadockJob.getUri().getRawPath(),
                                           datadockJob.getSubmitter(),
                                           datadockJob.getFormat() ) );
 
                 log.debug( String.format( "found new job: path='%s', submitter='%s', format='%s'",
                                           datadockJob.getUri().getRawPath(),
                                           datadockJob.getSubmitter(),
                                           datadockJob.getFormat() ) );
                 newJobs.add( datadockJob );
                 removeJobs.add( job );
             }
         }
         
         // removing confirmed jobs from applications
         for( Pair< File, Long > job : removeJobs )
         {
             log.debug( String.format( "Removing job='%s' from applications", job.getFirst().getAbsolutePath() ) );
             jobApplications.remove( job );
         }
 
         // Finding new Jobs
         // Has anything happened ?
         boolean changed = false;
         for( Pair< File, Long > format : formats )
         {
             if( format.getFirst().lastModified() > format.getSecond() )
             {
                 changed = true;
             }
         }
 
         if( changed )
         {
             log.debug( "Files changed" );
             for( File newJob : findNewJobs() )
             {
                 log.debug( String.format( "adding new job to applications: path='%s'", newJob.getAbsolutePath() ) );
                 jobApplications.add( new Pair< File, Long >( newJob, newJob.length() ) );
             }
 
             // generating new snapshot
             submitters = new Vector<Pair<File, Long >>();
             formats = new Vector<Pair<File, Long >>();
             initVectors();
              
             jobSet = new HashSet< File >();
             for( Pair< File, Long > job : findAllJobs() )
             {                
             	//log.debug( String.format( "adding path='%s' to jobSet", Tuple.get1( job ).getAbsolutePath() ) );
             	//log.debug( String.format( "adding path='%s' to jobSet", job.getFirst().getAbsolutePath() ) );
                 //jobSet.add( Tuple.get1( job ) );
             	//jobSet.add( job.getFirst() );
             }
         }
         
         return newJobs;
     }
 
     
     /**
      * Private method to initialize the local vectors representing the
      * polling directory.
      */
     private void initVectors()
     {
         log.debug( "initvectors() called" );
         log.debug( "submitterFormatsVector: \n" + submittersFormatsVector.toString() );
         log.debug( "Submitters:" );        
         for( File submitter : path.listFiles() )
         {
             if( submitter.isDirectory() )
             {
                 log.debug( String.format( "adding submitter: path='%s'", submitter.getAbsolutePath() ) );
                 submitters.add( new Pair< File, Long >( submitter, submitter.lastModified() ) );
             }
         }
         
         log.debug( "formats:" );        
         for( Pair<File, Long> submitter : submitters )
         {
         	File submitterFile = submitter.getFirst();
             for( File format : submitterFile.listFiles() )
             {
             	if ( checkSubmitterFormat( submitterFile, format ) )
             	{
             		log.debug( String.format( "format: path='%s'", format.getAbsolutePath() ) );
             		formats.add( new Pair< File, Long >( format, format.lastModified() ) );
             	}
             }
         }
     }
     
     
     private boolean checkSubmitterFormat( File submitterFile, File formatFile )
     {
     	String submitterFilePath = sanitize( submitterFile );
     	String formatFilePath = sanitize( formatFile );
     	submitterFilePath = submitterFile.getAbsolutePath().substring( submitterFile.getAbsolutePath().lastIndexOf( "/" ) + 1 );    	
     	log.debug( "FileHarvest.checkSubmitterFormat -> submitter: " + submitterFilePath );    	
     	formatFilePath = formatFile.getAbsolutePath().substring( formatFile.getAbsolutePath().lastIndexOf( "/") + 1 );
     	log.debug( "FileHarvest.checkSubmitterFormat -> format: " + formatFilePath );
     	
     	Pair< String, String > pair = new Pair< String, String >( submitterFilePath, formatFilePath );
     	if ( submittersFormatsVector.contains( pair ) )
     	{
     		return true;
     	}
     	else
     	{
     		return false;
     	}
     }
     
     
     private String sanitize( File file )
     {
     	if ( file.getAbsolutePath().endsWith( "/" ) )
     	{
     		return ( String )file.getAbsolutePath().subSequence( 0 , ( file.getAbsolutePath().length() - 1) );
     	}
     	else
     	{
     		return file.getAbsolutePath();
     	}
     }
     
     /**
      * Finds the new jobs in the poll Directory
      * 
      * @returns a hashset of new job files.
      */
     private HashSet< File > findNewJobs()
     {
         log.debug( "findNewJobs() called" );
         HashSet< File > currentJobs = new HashSet< File >();
         for( Pair< File, Long > job : findAllJobs() )
         {
             currentJobs.add( job.getFirst() );
         }
         
         HashSet<File> newJobs = new HashSet<File>( jobSet );
         log.debug( String.format( "newjob size='%s', '%s'", newJobs.size(), newJobs.size() ) );
         newJobs.addAll( currentJobs );
         newJobs.removeAll( jobSet );
 
         for( File job : newJobs )
         {
             log.debug( String.format( "found job: '%s'", job.getAbsolutePath() ) );
         }
         
         return newJobs;
     }
 
     /**
      * Finds all jobs in the poll Directory
      * 
      * @returns a hashset of pairs containing new job files and their size.
      */
     private HashSet< Pair< File, Long > > findAllJobs()
     {
         log.debug( "findAllJobs() called" );
         HashSet< Pair< File, Long > > jobs = new HashSet< Pair< File, Long > >();
         
         for( Pair< File, Long > format : formats )
         {	
        	//int l = format.getFirst().listFiles().length;
        	//log.debug( "FileHarvest: fileList length:" + l + " Format: " + format.getFirst().getAbsolutePath() );        	
             for( File job : format.getFirst().listFiles() )
             {            	
                 log.debug( String.format( "found job: '%s'", job.getAbsolutePath() ) );
                 jobs.add( new Pair< File, Long >( job, job.length() )  );
             }
         }
         
         return jobs;
     }
 }
