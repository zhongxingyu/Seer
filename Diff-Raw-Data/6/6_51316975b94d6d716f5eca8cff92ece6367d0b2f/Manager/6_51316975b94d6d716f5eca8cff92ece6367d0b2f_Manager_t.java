 /*
  * Created on Dec 21, 2004
  */
 package org.penguincoder.mass;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 /**
  * @author Andrew Coleman
  *  
  */
 public class Manager {
 
     private static ArrayList serverlist;
 
     protected final static double currentVersion = .01;
 
     private static String hostname, fqdnhostname;
 
     private static void parseServerList ( String inputfile ) {
         /* the result arraylist, useful for unknown array sizes */
         if ( serverlist == null ) {
             serverlist = new ArrayList ( 64 );
         }
         /*
          * either fill from the file if the file exists, if the file does not
          * exist, assume that you mean a hostname
          */
         try {
             /* nice buffered reader for input */
             BufferedReader inReader = new BufferedReader ( new FileReader (
                     inputfile ) );
             /* read until the file is done */
             while ( inReader.ready () ) {
                 /* the most effective, not efficient, way to do this is by line */
                 String line = inReader.readLine ().trim ();
                 if ( !line.startsWith ( "#" ) && !line.equals ( hostname ) && !line.equals ( fqdnhostname ) ) {
                     serverlist.add ( line );
                 }
             }
             /* finished with the file */
             inReader.close ();
         } catch ( FileNotFoundException e ) {
             serverlist.add ( inputfile );
         } catch ( IOException e ) {
             System.err.println ( "There was a problem reading the file: "
                     + e.getLocalizedMessage () );
             System.exit ( 1 );
         }
     }
 
     public static StringBuffer getCommandOutput ( String[] command ) {
         StringBuffer resultBuffer = new StringBuffer ( 2048 );
 
         try {
             Process process = Runtime.getRuntime ().exec ( command );
 
             /* get the regular output */
             BufferedReader reader = new BufferedReader ( new InputStreamReader (
                     process.getInputStream () ) );
             int bytesRead = 0;
             char[] buf = new char[2048];
             do {
                 bytesRead = reader.read ( buf );
                 if ( bytesRead > 0 ) {
                     resultBuffer.append ( buf, 0, bytesRead );
                 }
             } while ( bytesRead > 0 );
             /* sometimes this can be null, unknown as to the reason */
             if ( reader != null ) {
                 reader.close ();
             }
 
             /* get the error stream */
             BufferedReader ereader = new BufferedReader (
                     new InputStreamReader ( process.getErrorStream () ) );
             do {
                 bytesRead = ereader.read ( buf );
                 if ( bytesRead > 0 ) {
                     resultBuffer.append ( buf, 0, bytesRead );
                 }
             } while ( bytesRead > 0 );
             /* sometimes this can be null, unknown as to the reason */
             if ( ereader != null ) {
                 ereader.close ();
             }
         } catch ( IOException e ) {
             /* report IO errors */
             resultBuffer.append ( "IOException caught!"
                     + System.getProperty ( "line.separator" ) );
             resultBuffer.append ( e.toString ()
                     + System.getProperty ( "line.separator" ) );
         }
         return resultBuffer;
     }
 
     private static void usage () {
         String lineSeparator = System.getProperty ( "line.separator" );
         System.out.println ( "This is the Massive Cluster Utility" );
         System.out
                 .println ( "General Usage: java -jar mass.jar <command> <serverfiles or servers> -- <arguments>" );
         System.out.println ( "Current Version: " + currentVersion
                 + lineSeparator );
         System.out
                 .println ( "The serverfile is a line delimited list of machines." );
         System.out.println ( "Supported Commands: (case-insensitive)" );
         System.out.println ( "\trun" + lineSeparator + "\tcopy" );
     }
 
     public static void main ( String[] args2 ) {
         /* sigh, this is a dirty hack to make
          * Eclipse run this program, remove the block
          * and change the main arguments to args from
          * args2 to remove the hack.
          */
         String[] args = null;
        if ( args2.length >= 1 && args2[0].indexOf ( "Manager" ) > 0 ) {
             args = new String[args2.length - 1];
             for ( int i = 1; i < args2.length; i++ ) {
                 args[i - 1] = args2[i];
             }
         } else {
             args = args2;
         }
         /* end hacky block */
 
         /* see if the gui needs to be started */
         if ( args.length < 4 ) {
             usage ();
             System.exit ( 1 );
         }
 
         /* basic sanity check on the command, there are only two */
         String command = args[0].toLowerCase ();
         if ( !command.equals ( "copy" ) && !command.equals ( "run" ) ) {
             usage ();
             System.exit ( 1 );
         }
 
         /* determine the hostname of the current box, must skip current box */
         hostname = "";
         try {
             java.net.InetAddress localMachine = java.net.InetAddress
                     .getLocalHost ();
             fqdnhostname = localMachine.getHostName ().toLowerCase ();
             hostname = fqdnhostname.replaceFirst("[.].*", "");
         } catch ( java.net.UnknownHostException uhe ) {
             hostname = "localhost";
             fqdnhostname = "localhost.localdomain";
         }
         /* populate the list of servers */
         int j = 1;
         for ( ; j < args.length && !args[j].equals ( "--" ); j++ ) {
             /* either fill from the file or from the listing */
             parseServerList ( args[j] );
         }
         /* found the double dash, so populate the argument array */
         j++;
         String[] commandArgs = new String[args.length - j];
         for ( int i = 0; j < args.length; j++, i++ ) {
             commandArgs[i] = args[j];
         }
 
         /* start the textual report */
         generateTextReport ( command, commandArgs );
     }
 
     private static void generateTextReport ( String command,
             String[] commandArgs ) {
         int serverCount = serverlist.size ();
 
         /* the ThreadGroup that will house all of the commands */
         ThreadGroup threadGroup = new ThreadGroup ( "MassiveClusterUtility" );
 
         /* the array of Commands that will eventually be run */
         CMD[] commands = new CMD[serverCount];
 
         /* create the command for each site */
         int threadCount = 0;
         System.out.println ( "Creating threads:" );
 
         /*
          * simple command check, there are only two. more would require the use
          * of a factory for simplicity
          * 
          * I'm going to go ahead and do all of the processing required to get
          * the object moving here. This is better done once than done
          * serverCount times :P
          */
         if ( command.equals ( "run" ) ) {
             String commandToRun = "";
             for ( int i = 0; i < commandArgs.length; i++ ) {
                 commandToRun += commandArgs[i] + " ";
             }
             commandToRun.trim ();
             System.out.println ( "Going to run the command: " + commandToRun );
             for ( int serverNum = 0; serverNum < serverCount; serverNum++ ) {
                 String server = (String) serverlist.get ( serverNum );
                 System.out.print ( server + " " );
                 commands[serverNum] = new CMDrun ( commandToRun, server,
                         threadGroup, "mass-run-" + server );
                 commands[serverNum].setPriority ( Thread.MIN_PRIORITY );
                 commands[serverNum].start ();
                 if ( threadCount == 5 ) {
                     System.out.println ();
                     threadCount = 0;
                 } else {
                     threadCount++;
                 }
             }
         } else {
             String remotePath = "", fileString = "";
             ArrayList files = new ArrayList ( 10 );
             remotePath = commandArgs[commandArgs.length - 1];
             int top = commandArgs.length - 2;
             for ( int i = 0; i <= top; i++ ) {
                 files.add ( commandArgs[i].trim () );
                 fileString += commandArgs[i].trim () + " ";
             }
             fileString.trim ();
             System.out.println ( "Going to copy the fileset: " + files );
             System.out.println ( "Remote path will be: " + remotePath );
             for ( int serverNum = 0; serverNum < serverCount; serverNum++ ) {
                 String server = (String) serverlist.get ( serverNum );
                 System.out.print ( server + " " );
                 commands[serverNum] = new CMDcopy ( files, remotePath, server,
                         threadGroup, "mass-copy-" + server );
                 commands[serverNum].setPriority ( Thread.MIN_PRIORITY );
                 commands[serverNum].start ();
                 if ( threadCount == 5 ) {
                     System.out.println ();
                     threadCount = 0;
                 } else {
                     threadCount++;
                 }
             }
         }
         System.out.println ( "Done" );
 
         /* wait on all of the threads to start */
         do {
             threadCount = threadGroup.activeCount ();
             try {
                 Thread.sleep ( 100 );
             } catch ( InterruptedException e1 ) {
             }
         } while ( threadCount != threadGroup.activeCount () );
 
         /* wait on all the threads to finish */
         int dotNumber = 0;
         while ( (threadCount = threadGroup.activeCount ()) > 0 ) {
             /*
              * this will print out 12 dots in one line, with one dot every 5
              * seconds. each line is a minute of processing, makes it very easy
              * to figure out how long it ran.
              */
             System.out.print ( "." );
             try {
                 Thread.sleep ( 5000 );
             } catch ( InterruptedException e1 ) {
             }
             if ( dotNumber >= 11 ) {
                 System.out.println ();
                 dotNumber = 0;
             } else {
                 dotNumber++;
             }
         }
 
         /* print out the report */
         System.out.println ( System.getProperty ( "line.separator" ) );
         System.out.println ( "===== Begin Report =====" );
         for ( int thread = 0; thread < commands.length; thread++ ) {
             System.out.print ( commands[thread].toString () );
         }
 
         /* finished */
         System.out.println ( "Fin" );
     }
}
