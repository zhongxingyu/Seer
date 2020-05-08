 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.maven.doxia.cli;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.Properties;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.ParseException;
 import org.apache.maven.doxia.Converter;
 import org.apache.maven.doxia.ConverterException;
 import org.apache.maven.doxia.DefaultConverter;
 import org.apache.maven.doxia.UnsupportedFormatException;
 import org.apache.maven.doxia.logging.Log;
 import org.apache.maven.doxia.logging.SystemStreamLog;
 import org.apache.maven.doxia.wrapper.InputFileWrapper;
 import org.apache.maven.doxia.wrapper.OutputFileWrapper;
 import org.codehaus.plexus.util.Os;
 
 /**
 * Doxia converter CLI
  *
  * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
  * @version $Id$
  */
 public class ConverterCli
 {
     /**
      * Default main which terminates the JVM with <code>0</code> if no errors occurs.
      *
      * @param args
      * @see #doMain(String[])
      * @see System#exit(int)
      */
     public static void main( String[] args )
     {
         System.exit( ConverterCli.doMain( args ) );
     }
 
     /**
      * @param args
      */
     private static int doMain( String[] args )
     {
         // ----------------------------------------------------------------------
         // Setup the command line parser
         // ----------------------------------------------------------------------
 
         CLIManager cliManager = new CLIManager();
 
         CommandLine commandLine;
         try
         {
             commandLine = cliManager.parse( args );
         }
         catch ( ParseException e )
         {
             System.err.println( "Unable to parse command line options: " + e.getMessage() );
             CLIManager.displayHelp();
 
             return 1;
         }
 
         if ( "1.4".compareTo( System.getProperty( "java.specification.version" ) ) > 0 )
         {
            System.err.println( "Sorry, but JDK 1.4 or above is required to execute Maven. You appear to be using "
                 + "Java:" );
             System.err.println( "java version \"" + System.getProperty( "java.version", "<unknown java version>" )
                 + "\"" );
             System.err.println( System.getProperty( "java.runtime.name", "<unknown runtime name>" ) + " (build "
                 + System.getProperty( "java.runtime.version", "<unknown runtime version>" ) + ")" );
             System.err.println( System.getProperty( "java.vm.name", "<unknown vm name>" ) + " (build "
                 + System.getProperty( "java.vm.version", "<unknown vm version>" ) + ", "
                 + System.getProperty( "java.vm.info", "<unknown vm info>" ) + ")" );
 
             return 1;
         }
 
         if ( commandLine.hasOption( CLIManager.HELP ) )
         {
             CLIManager.displayHelp();
 
             return 0;
         }
 
         if ( commandLine.hasOption( CLIManager.VERSION ) )
         {
             showVersion();
 
             return 0;
         }
 
         boolean debug = commandLine.hasOption( CLIManager.DEBUG );
 
         boolean showErrors = debug || commandLine.hasOption( CLIManager.ERRORS );
 
         if ( showErrors )
         {
             System.out.println( "+ Error stacktraces are turned on." );
         }
 
         Converter converter = new DefaultConverter();
         Log log = new SystemStreamLog();
         if ( debug )
         {
             log.setLogLevel( Log.LEVEL_DEBUG );
         }
         converter.enableLogging( log );
 
         InputFileWrapper input;
         OutputFileWrapper output;
         try
         {
             input = InputFileWrapper.valueOf( commandLine.getOptionValue( CLIManager.IN ), commandLine
                 .getOptionValue( CLIManager.FROM ), commandLine.getOptionValue( CLIManager.INENCODING ), converter
                 .getInputFormats() );
             output = OutputFileWrapper.valueOf( commandLine.getOptionValue( CLIManager.OUT ), commandLine
                 .getOptionValue( CLIManager.TO ), commandLine.getOptionValue( CLIManager.OUTENCODING ), converter
                 .getOutputFormats() );
         }
         catch ( IllegalArgumentException e )
         {
             showFatalError( "Illegal argument: " + e.getMessage(), e, showErrors );
 
             CLIManager.displayHelp();
 
             return 1;
         }
         catch ( UnsupportedEncodingException e )
         {
             showFatalError( e.getMessage(), e, showErrors );
 
             return 1;
         }
         catch ( FileNotFoundException e )
         {
             showFatalError( e.getMessage(), e, showErrors );
 
             return 1;
         }
 
         try
         {
             converter.convert( input, output );
         }
         catch ( UnsupportedFormatException e )
         {
             showFatalError( e.getMessage(), e, showErrors );
 
             return 1;
         }
         catch ( ConverterException e )
         {
             showFatalError( "Converter exception: " + e.getMessage(), e, showErrors );
 
             return 1;
         }
         catch ( IllegalArgumentException e )
         {
             showFatalError( "Illegal argument: " + e.getMessage(), e, showErrors );
 
             return 1;
         }
         catch ( RuntimeException e )
         {
             showFatalError( "Runtime exception: " + e.getMessage(), e, showErrors );
 
             return 1;
         }
 
         return 0;
     }
 
     private static void showVersion()
     {
         InputStream resourceAsStream;
         try
         {
             Properties properties = new Properties();
             resourceAsStream = ConverterCli.class.getClassLoader()
                 .getResourceAsStream( "META-INF/maven/org.apache.maven.doxia/doxia-converter/pom.properties" );
 
             if ( resourceAsStream != null )
             {
                 properties.load( resourceAsStream );
 
                 if ( properties.getProperty( "builtOn" ) != null )
                 {
                     System.out.println( "Doxia Converter version: " + properties.getProperty( "version", "unknown" )
                         + " built on " + properties.getProperty( "builtOn" ) );
                 }
                 else
                 {
                     System.out.println( "Doxia Converter version: " + properties.getProperty( "version", "unknown" ) );
                 }
             }
             else
             {
                 System.out.println( "Doxia Converter version: " + properties.getProperty( "version", "unknown" ) );
             }
 
             System.out.println( "Java version: " + System.getProperty( "java.version", "<unknown java version>" ) );
 
             System.out.println( "OS name: \"" + Os.OS_NAME + "\" version: \"" + Os.OS_VERSION + "\" arch: \""
                 + Os.OS_ARCH + "\" family: \"" + Os.OS_FAMILY + "\"" );
 
         }
         catch ( IOException e )
         {
             System.err.println( "Unable to determine version from JAR file: " + e.getMessage() );
         }
     }
 
     private static void showFatalError( String message, Exception e, boolean show )
     {
         System.err.println( "FATAL ERROR: " + message );
         if ( show )
         {
             System.err.println( "Error stacktrace:" );
 
             e.printStackTrace();
         }
         else
         {
             System.err.println( "For more information, run with the -e flag" );
         }
     }
 }
