 package org.realityforge.sqlshell;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.Driver;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.realityforge.cli.CLArgsParser;
 import org.realityforge.cli.CLOption;
 import org.realityforge.cli.CLOptionDescriptor;
 import org.realityforge.cli.CLUtil;
 
 /**
  * The entry point in which to run the tool.
  */
 public class Main
 {
   private static final int HELP_OPT = 1;
   private static final int QUIET_OPT = 'q';
   private static final int VERBOSE_OPT = 'v';
   private static final int DATABASE_DRIVER_OPT = 2;
   private static final int DATABASE_PROPERTY_OPT = 'D';
   private static final int SQL_FILE_OPT = 'f';
 
   private static final CLOptionDescriptor[] OPTIONS = new CLOptionDescriptor[]{
     new CLOptionDescriptor( "database-driver",
                             CLOptionDescriptor.ARGUMENT_REQUIRED,
                             DATABASE_DRIVER_OPT,
                             "The jdbc driver to load prior to connecting to the databases." ),
     new CLOptionDescriptor( "database-property",
                             CLOptionDescriptor.ARGUMENTS_REQUIRED_2 | CLOptionDescriptor.DUPLICATES_ALLOWED,
                             DATABASE_PROPERTY_OPT,
                             "A jdbc property." ),
     new CLOptionDescriptor( "file",
                             CLOptionDescriptor.ARGUMENT_REQUIRED,
                             SQL_FILE_OPT,
                             "A file containing sql commands." ),
     new CLOptionDescriptor( "help",
                             CLOptionDescriptor.ARGUMENT_DISALLOWED,
                             HELP_OPT,
                             "print this message and exit" ),
     new CLOptionDescriptor( "quiet",
                             CLOptionDescriptor.ARGUMENT_DISALLOWED,
                             QUIET_OPT,
                             "Do not output unless an error occurs, just return 0 on no difference.",
                             new int[]{ VERBOSE_OPT } ),
     new CLOptionDescriptor( "verbose",
                             CLOptionDescriptor.ARGUMENT_DISALLOWED,
                             VERBOSE_OPT,
                             "Verbose output of differences.",
                             new int[]{ QUIET_OPT } ),
   };
 
   private static final int ERROR_PARSING_ARGS_EXIT_CODE = 2;
   private static final int ERROR_BAD_DRIVER_EXIT_CODE = 3;
   private static final int ERROR_OTHER_EXIT_CODE = 4;
 
   private static String c_databaseDriver;
   private static final SqlShell c_shell = new SqlShell();
   private static final Logger c_logger = Logger.getAnonymousLogger();
   private static int c_commandIndex = 1;
   private static BufferedReader c_input;
   private static String c_inputFile;
 
   public static void main( final String[] args )
   {
     setupLogger();
     if ( !processOptions( args ) )
     {
       System.exit( ERROR_PARSING_ARGS_EXIT_CODE );
       return;
     }
 
     if ( c_logger.isLoggable( Level.FINE ) )
     {
       c_logger.log( Level.FINE, "SqlShell starting..." );
     }
 
     final Driver driver = loadDatabaseDriver();
     if ( null == driver )
     {
       System.exit( ERROR_BAD_DRIVER_EXIT_CODE );
       return;
     }
 
     c_shell.setDriver( driver );
 
     if ( !isInteractive() )
     {
       try
       {
         c_input = new BufferedReader( new InputStreamReader( new FileInputStream( c_inputFile ) ) );
       }
       catch ( FileNotFoundException e )
       {
         c_logger.log( Level.SEVERE, "Error: Unable to load input file " + c_inputFile + " due to: " + e, e );
         System.exit( ERROR_OTHER_EXIT_CODE );
         return;
       }
     }
     else
     {
       c_input = new BufferedReader( new InputStreamReader( System.in ) );
     }
     String command;
     if ( isInteractive() )
     {
       printPrompt();
     }
     while ( null != ( command = readCommand() ) && !command.trim().equalsIgnoreCase( "quit" ) )
     {
       if ( command.trim().length() > 0 )
       {
         //Add new line after entered value
         if ( isInteractive() )
         {
           System.out.println();
         }
         executeSQL( command );
         if ( isInteractive() )
         {
           printPrompt();
         }
       }
     }
 
     if ( c_logger.isLoggable( Level.FINE ) )
     {
       c_logger.log( Level.FINE, "SqlShell completed." );
     }
   }
 
   private static boolean isInteractive()
   {
     return null == c_inputFile;
   }
 
   private static String readCommand()
   {
     try
     {
       final StringBuilder sb = new StringBuilder();
       String line;
       boolean readLine = false;
       while ( null != ( line = c_input.readLine() ) && !"GO".equals( line ) )
       {
         readLine = true;
         sb.append( line );
         sb.append( "\n" );
       }
       if ( !readLine )
       {
         return null;
       }
       else
       {
         return sb.toString().trim();
       }
     }
     catch ( final IOException e )
     {
       c_logger.log( Level.SEVERE, "Error: Error reading input due to: " + e, e );
       System.exit( ERROR_OTHER_EXIT_CODE );
       return null;
     }
   }
 
   private static void printPrompt()
   {
     System.out.print( c_commandIndex++ + "> " );
   }
 
   private static void executeSQL( final String command )
   {
     if ( c_logger.isLoggable( Level.FINE ) )
     {
       c_logger.log( Level.FINE, "Executing: " + command );
     }
     final List<Map<String, Object>> results;
     try
     {
       results = c_shell.query( command );
     }
     catch ( final Throwable t )
     {
       c_logger.log( Level.SEVERE, "Error: Error executing sql due to " + t, t );
       return;
     }
     if ( c_logger.isLoggable( Level.FINE ) )
     {
       c_logger.log( Level.FINE, "Result Row Count: " + results.size() );
     }
     final JSONArray jsonArray = new JSONArray();
     for ( Map<String, Object> row : results )
     {
       jsonArray.put( new JSONObject( row ) );
     }
     c_logger.log( Level.INFO, jsonArray.toString() );
   }
 
   private static void setupLogger()
   {
     c_logger.setUseParentHandlers( false );
    final ConsoleHandler handler = new ConsoleHandler();
    handler.setFormatter( new RawFormatter() );
     handler.setLevel( Level.ALL );
     c_logger.addHandler( handler );
   }
 
   private static Driver loadDatabaseDriver()
   {
     try
     {
       return (Driver) Class.forName( c_databaseDriver ).newInstance();
     }
     catch ( final Exception e )
     {
       c_logger.log( Level.SEVERE, "Error: Unable to load database driver " + c_databaseDriver + " due to " + e );
       System.exit( ERROR_BAD_DRIVER_EXIT_CODE );
       return null;
     }
   }
 
   private static boolean processOptions( final String[] args )
   {
     // Parse the arguments
     final CLArgsParser parser = new CLArgsParser( args, OPTIONS );
 
     //Make sure that there was no errors parsing arguments
     if ( null != parser.getErrorString() )
     {
       c_logger.log( Level.SEVERE, "Error: " + parser.getErrorString() );
       return false;
     }
 
     // Get a list of parsed options
     @SuppressWarnings( "unchecked" ) final List<CLOption> options = parser.getArguments();
     for ( final CLOption option : options )
     {
       switch ( option.getId() )
       {
         case CLOption.TEXT_ARGUMENT:
         {
           if ( null == c_shell.getDatabase() )
           {
             c_shell.setDatabase( option.getArgument() );
           }
           else
           {
             c_logger.log( Level.SEVERE, "Error: Unexpected argument: " + option.getArgument() );
             return false;
           }
           break;
         }
         case DATABASE_PROPERTY_OPT:
         {
           c_shell.getDbProperties().setProperty( option.getArgument(), option.getArgument( 1 ) );
           break;
         }
         case DATABASE_DRIVER_OPT:
         {
           c_databaseDriver = option.getArgument();
           break;
         }
         case SQL_FILE_OPT:
         {
           c_inputFile = option.getArgument();
           break;
         }
         case VERBOSE_OPT:
         {
           c_logger.setLevel( Level.ALL );
           break;
         }
         case QUIET_OPT:
         {
           c_logger.setLevel( Level.WARNING );
           break;
         }
         case HELP_OPT:
         {
           printUsage();
           return false;
         }
 
       }
     }
     if ( null == c_databaseDriver )
     {
       c_logger.log( Level.SEVERE, "Error: Database driver must be specified" );
       return false;
     }
     if ( null == c_shell.getDatabase() )
     {
       c_logger.log( Level.SEVERE, "Error: Jdbc url must supplied for the database" );
       return false;
     }
     if ( c_logger.isLoggable( Level.FINE ) )
     {
       c_logger.log( Level.FINE, "Database: " + c_shell.getDatabase() );
       c_logger.log( Level.FINE, "Database Driver: " + c_databaseDriver );
       c_logger.log( Level.FINE, "Database Properties: " + c_shell.getDbProperties() );
       if ( null != c_inputFile )
       {
         c_logger.log( Level.FINE, "Input File: " + c_inputFile );
       }
     }
 
     return true;
   }
 
   /**
    * Print out a usage statement
    */
   private static void printUsage()
   {
     final String lineSeparator = System.getProperty( "line.separator" );
 
     final StringBuilder msg = new StringBuilder();
 
     msg.append( "java " );
     msg.append( Main.class.getName() );
     msg.append( " [options] jdbcURL" );
     msg.append( lineSeparator );
     msg.append( "Options: " );
     msg.append( lineSeparator );
 
     msg.append( CLUtil.describeOptions( OPTIONS ).toString() );
 
     c_logger.log( Level.INFO, msg.toString() );
   }
 }
