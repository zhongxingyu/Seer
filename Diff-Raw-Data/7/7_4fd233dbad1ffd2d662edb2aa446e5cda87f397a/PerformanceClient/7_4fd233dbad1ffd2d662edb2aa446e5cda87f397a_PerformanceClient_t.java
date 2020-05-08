 package net.praqma.ccanalyzer;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import net.praqma.ccanalyzer.ConfigurationReader.Configuration;
 import net.praqma.monkit.MonKit;
 import net.praqma.util.debug.Logger;
 
 public class PerformanceClient extends AbstractClient {
 
 	private static Logger logger = Logger.getLogger();
 	
     public PerformanceClient( int port, String host, String clientName, MonKit mk ) {
         super( port, host, clientName, mk );
     }
     
     @Override
     protected void perform( Configuration counters, PrintWriter out, BufferedReader in ) throws IOException {
         
         String line = "";
         
         logger.info( "Obtaining Performance information" );
         
         /* Get the performance counters */
         for( PerformanceCounterConfiguration pc : counters.getPerformanceCounters( clientName ) ) {
             out.println( PerformanceCounterMeter.RequestType.NAMED_COUNTER.toString() );
             out.println( pc.counter );
             out.println( pc.numberOfSamples );
             out.println( pc.intervalTime );
             out.println( "." );
             
             System.out.print( "* " + pc.host + ": " );
 
             while( ( line = in.readLine() ) != null ) {
                 break;
             }
 
             /* Error result */
             if( line == null || line.length() == 0 ) {
                 System.err.println( "Erroneous result" );
             } else {
 	            System.out.println( line + " " + pc.scale );
 	
 	            monkit.addCategory( pc.host, pc.scale );
 	
 	            monkit.add( this.clientName, line, pc.host );
             }
         }
     }
     
 }
