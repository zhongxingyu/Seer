 package dbc.opensearch.components.pti;
 
 import dbc.opensearch.tools.FedoraHandler;
 import dbc.opensearch.tools.Processqueue;
 
 import org.apache.log4j.xml.DOMConfigurator;
 import org.apache.log4j.Logger;
 
 import org.apache.commons.configuration.ConfigurationException;
 
 import java.util.Vector;
 import java.util.Iterator;
 
 import java.util.NoSuchElementException;
 import java.lang.ClassNotFoundException;
 import java.sql.SQLException;
 import java.lang.InterruptedException;
 import java.util.concurrent.ExecutionException;
 //import org.compass.core.converter.ConversionException;
 
 import java.util.concurrent.Executors;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.FutureTask;
 
 import com.mallardsoft.tuple.Pair;
 import com.mallardsoft.tuple.Tuple;
 import com.mallardsoft.tuple.Triple;
 
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import javax.xml.rpc.ServiceException;
 import java.io.IOException;
 
 public class PTIPoolAdm {
 
     Logger log = Logger.getLogger("PTIPoolAdm");
 
     private Processqueue processqueue;
     private PTIPool PTIpool;
 
     private Vector activeThreads;
     private Iterator iter;
 
     private long sleepInMilliSec;
 
     /**
      * Initializes the PTIPool with the given number of
      * threads. Initializes a Processqueue and starts the mainLoop for
      * the processing of data within the threads.
      * @param numberOfThreads is the number of threads to initialize the PTIPool with
      * @throws ConfigurationException If the PTIPool could not be correctly initialized
      * @throws ClassNotFoundException If the Processqueue could not load the database driver
      * @throws MalformedURLException Could not obtain compass configuration
      * @throws UnknownHostException Error obtaining fedora configuration
      * @throws ServiceException Something went wrong initializing the fedora client
      * @throws IOException Something went wrong initializing the fedora client
      * @throws SQLException The processqueue could not retrieve information from the database
      */
     public PTIPoolAdm( int numberOfThreads )throws ConfigurationException, ClassNotFoundException, MalformedURLException, UnknownHostException, ServiceException, IOException, SQLException{
         log.debug( String.format( "Entering PTIPoolAdm(numberOfThreads=%s)", numberOfThreads ) );
 
         /** /todo: where should sleepInMilliSec be set?? in a configuration file or what*/
         sleepInMilliSec= 20000;
         
         log.debug( String.format( "Setting up the PTIPool with %s threads", numberOfThreads ) );
         PTIpool = new PTIPool( numberOfThreads );
         
         processqueue = new Processqueue();
         activeThreads = new Vector();
         
         log.debug( "Removing entries marked as active from the processqueue" );
         int removed = processqueue.deActivate( );
         log.debug( String.format( "marked  %s 'active' threads as ready to process", removed ) );
         log.debug( "PTIPoolAdm is set up" );
     }
 
     /**
      * the mainLoop polls the processqueue and start threads when the queue pops a fedorahandle.   
      * the mainLoop also checks whether already active threads are finished.
      * @throws ClassNotFoundException if the Processqueue could not load the database driver
      * @throws SQLException if the processqueue could not retrieve information from the database
      * @throws RuntimeException if the started thread stop due to an exception
      * @throws ConfigurationException if the PTIPool could not be correctly initialized
      * @throws InterruptedException is a thread exception
      */
     public void mainLoop()throws ClassNotFoundException, SQLException, RuntimeException, ConfigurationException, InterruptedException{
         log.debug( "PTIPoolAdm mainloop" );
 
         // creates initial timestamp. creates it with an offset - so
         // when the loop is entered it polls the processqueue
         // immediatly
         long stamp = System.currentTimeMillis() - ( sleepInMilliSec+ 1 ) ;
 
         /** /todo: we need a nicer way to do this than a while true loop. */
         while(true){
 
             if( System.currentTimeMillis() > stamp+sleepInMilliSec ){                
                 startThreads();
                 stamp = System.currentTimeMillis();
             }            
             checkThreads();
         }
     }
     
     /**
      * Iterates through the activeThreads vector and remove all entries
      * where the associated thread is done. The entries are also
      * committed to the processqueue, which effectivly removes them
      * @throws ClassNotFoundException if the Processqueue could not load the database driver
      * @throws SQLException if the processqueue could not retrieve information from the database
      * @throws NoSuchElementException if the processqueue does not contain an element matching the queueid
      * @throws InterruptedException is a thread exception
      * @throws RuntimeException if the started thread stop due to an exception
      */
     private void checkThreads()throws ClassNotFoundException, SQLException, NoSuchElementException, InterruptedException, RuntimeException {
         //        log.debug( "Entering PTIPoolAdm.checkThreads()" );
         
         Pair<FutureTask, Integer> vectorPair = null;
         int queueID;
         FutureTask future;
         Vector removeableThreads = new Vector();
         iter = activeThreads.iterator();
 
         while( iter.hasNext() ){
 
             vectorPair = ( Pair ) iter.next();
             future = Tuple.get1(vectorPair);
             queueID = Tuple.get2(vectorPair);
             // log.debug( "checking future belonging to queueID = "+queueID );
             if( future.isDone() ){// this thread is done
                 try{
                     Long processtime = (Long) future.get();
                 }
                 catch(ExecutionException ee){
                     // catching exception from thread
                     Throwable cause = ee.getCause();
                     log.fatal( String.format( "Caught thread error associated with queueid = %s", queueID ) );     
                    
                     RuntimeException re = new RuntimeException(cause);
                    log.debug( String.format( "message of the exception: '%s'", re.getMessage() ) );
                     if(re.getMessage().startsWith("org.compass.core.converter.ConversionException") ) {
                        //processqueue.removeElem(queueID);
                         log.debug( String.format( "Element removed with queueID: '%s'",queueID ) );  
                     }
                     throw re;
                 }
                 
                 processqueue.commit( queueID );
                 removeableThreads.add( vectorPair );
                 log.info( String.format( "job done, Committed to queue, queueID = %s", queueID ) );
             }
         }
 
         // remove threads that have returned, from activeThreads
         iter = removeableThreads.iterator();
         while( iter.hasNext() ){
             activeThreads.remove( iter.next() );
         }        
     }
 
     /** pop processqueue until its empty, and starts threads which
      * fetch and process data from the fedora repository. Puts thread
      * (futureTask, and queueid on activeThreads vector)
      * committed to the processqueue, which effectivly removes them
      * @throws ClassNotFoundException if the Processqueue could not load the database driver
      * @throws SQLException if the processqueue could not retrieve information from the database
      * @throws RuntimeException if the started thread stop due to an exception
      * @throws ConfigurationException if the PTIPool could not be correctly initialized
      */
     private void startThreads()throws ClassNotFoundException, SQLException, RuntimeException, ConfigurationException{
         log.debug( "Entering PTIPoolAdm.startThreads()" );
 
         boolean fetchedLast = false;
         Triple<String, Integer, String> queueTriple = null;
         Pair<FutureTask, Integer> vectorPair = null;
         String fedoraHandle;
         String itemID;
         int queueID;
         FutureTask future;
 
         while( !fetchedLast ){
 
             try{
                 queueTriple = processqueue.pop();
             }
             catch(NoSuchElementException nse){
                 log.debug( "processqueue is empty" );
                 fetchedLast = true;
             }
 
             if( !fetchedLast ){
                 log.debug( "Fetched element from processqueue" );
                 fedoraHandle = Tuple.get1(queueTriple);
                 queueID = Tuple.get2(queueTriple);
                 itemID = Tuple.get3(queueTriple);
               
                 future = PTIpool.createAndJoinThread( fedoraHandle, itemID );
               
                 // add thread to active thread vector
                 vectorPair = Tuple.from(future, queueID);
                 activeThreads.add( vectorPair );
                 log.info( String.format( "started new thread with fedoraHandle: %s, queueID: %s and itemID: %s ",             
                                          fedoraHandle, queueID, itemID ) );
             }
         }
     }
 }
