 package org.apache.maven.it;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.xml.utils.DOMBuilder;
 import org.apache.xpath.XPathAPI;
 import org.w3c.dom.Document;
 
 /**
  * @author <a href="mailto:jason@maven.org">Jason van Zyl </a>
  * @author <a href="mailto:brett@apache.org">Brett Porter</a>
  * @version $Id$
  */
 public class Verifier
 {
     private static String localRepo;
 
     private final String basedir;
 
     private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
     private final ByteArrayOutputStream errStream = new ByteArrayOutputStream();
 
     private final PrintStream originalOut;
     private final PrintStream originalErr;
 
     public Verifier( String basedir )
     {
         this.basedir = basedir;
 
         originalOut = System.out;
         System.setOut( new PrintStream( outStream ) );
             
         originalErr = System.err;
         System.setErr( new PrintStream( errStream ) );
     }
 
     public void resetStreams()
     {
         System.setOut( originalOut );
         System.setErr( originalErr );
     }
 
     public void displayStreamBuffers()
     {
         String out = outStream.toString();
         if ( out != null && out.trim().length() > 0 )
         {
             System.out.println( "----- Standard Out -----" );
             System.out.println( out );
         }
 
         String err = errStream.toString();
         if ( err != null && err.trim().length() > 0 )
         {
             System.err.println( "----- Standard Error -----" );
             System.err.println( err );
         }
     }
 
     // ----------------------------------------------------------------------
     //
     // ----------------------------------------------------------------------
 
     public void verify() throws VerificationException
     {
         List lines = loadFile( basedir, "expected-results.txt" );
         for ( Iterator i = lines.iterator(); i.hasNext(); )
         {
             String line = ( String ) i.next();
             verifyExpectedResult( line );
         }
     }
 
     private static List loadFile( String basedir, String filename ) throws VerificationException
     {
         return loadFile( new File( basedir, filename ) );
     }
 
     private static List loadFile( File file ) throws VerificationException
     {
         List lines = new ArrayList();
 
         try
         {
             BufferedReader reader = new BufferedReader( new FileReader( file ) );
 
             String line = "";
 
             while ( (line = reader.readLine()) != null )
             {
                 line = line.trim();
                 if ( line.startsWith( "#" ) || line.length() == 0 )
                 {
                     continue;
                 }
 
                 line = replace( line, "${localRepository}", localRepo );
                 lines.add( line );
             }
         }
         catch ( Exception e )
         {
             throw new VerificationException( e );
         }
         return lines;
     }
 
     public void executeHook( String filename ) throws VerificationException
     {
         try
         {
             File f = new File( basedir, filename );
             if ( !f.exists() )
             {
                 return;
             }
 
             List lines = loadFile( f );
  
             for ( Iterator i = lines.iterator(); i.hasNext(); )
             {
                 String line = ( String ) i.next();
                 executeCommand( line );
             }
         }
         catch ( VerificationException e )
         {
             throw e;
         }
         catch ( Exception e )
         {
             throw new VerificationException( e );
         }
     }
 
     private static void executeCommand( String line ) throws VerificationException
     {
         int index = line.indexOf( " " );
         String cmd;
         String args = null;
         if ( index >= 0 )
         {
             cmd = line.substring( 0, index );
             args = line.substring( index + 1 );
         }
         else
         {
             cmd = line;
         }
 
         if ( cmd.equals( "rm" ) )
         {
             System.out.println( "Removing file: " + args );
             File f = new File( args );
             if ( f.exists() && !f.delete() )
             {
                 throw new VerificationException( "Error removing file - delete failed" );
             }
         }
         else
         {
             throw new VerificationException( "unknown command: " + cmd );
         }
     }
 
     private static String retrieveLocalRepo()
     {
         String repo = System.getProperty( "maven.repo.local" );
         if ( repo == null )
         {
             try
             {
                // parse ~/.m2/override.xml for it...
                 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                 DocumentBuilder builder = factory.newDocumentBuilder();
     
                File pom = new File( System.getProperty( "user.home" ), ".m2/override.xml" );
                 Document dom = builder.parse( pom );
     
                 repo = XPathAPI.selectSingleNode( dom, "/project/local/repository/text()" ).getNodeValue();
             }
             catch ( Exception e )
             {
                 System.err.println( "WARNING: failed to parse user pom (ignoring): " + e.getMessage() );
             }
         }
         if ( repo == null )
         {
             repo = System.getProperty( "user.home" ) + "/.m2/repository";
         }
         return repo;
     }
 
     private void verifyExpectedResult( String line ) throws VerificationException
     {
         if ( line.indexOf( "!/" ) > 0 )
         {
             String urlString = "jar:file:" + basedir + "/" + line;
 
             try
             {
                 URL url = new URL( urlString );
 
                 InputStream is = url.openStream();
 
                 if ( is == null )
                 {
                     throw new VerificationException( "Expected JAR resource was not found: " + line );
                 }
             }
             catch ( Exception e )
             {
                 throw new VerificationException( "Expected JAR resource was not found: " + line );
             }
         }
         else
         {
             File expectedFile = new File( line );
             if ( !expectedFile.isAbsolute() && !line.startsWith( "/" ) )
             {
                 expectedFile = new File( basedir, line );
             } 
 
             if ( !expectedFile.exists() )
             {
                 throw new VerificationException( "Expected file was not found: " + expectedFile.getPath() );
             }
         }
     }
 
     // ----------------------------------------------------------------------
     //
     // ----------------------------------------------------------------------
 
     public static String replaceOnce( String text, String repl, String with )
     {
         return replace( text, repl, with, 1 );
     }
 
     public static String replace( String text, String repl, String with )
     {
         return replace( text, repl, with, -1 );
     }
 
     public static String replace( String text, String repl, String with, int max )
     {
         if ( text == null || repl == null || with == null || repl.length() == 0 )
         {
             return text;
         }
 
         StringBuffer buf = new StringBuffer( text.length() );
         int start = 0, end = 0;
         while ( (end = text.indexOf( repl, start )) != -1 )
         {
             buf.append( text.substring( start, end ) ).append( with );
             start = end + repl.length();
 
             if ( --max == 0 )
             {
                 break;
             }
         }
         buf.append( text.substring( start ) );
         return buf.toString();
     }
 
     public void executeGoals( String filename ) throws VerificationException
     {
         String mavenHome = System.getProperty( "maven.home" );
         if ( mavenHome == null )
         {
             throw new VerificationException( "maven.home has not been specified" );
         }
 
         List goals = loadFile( basedir, filename );
         if ( goals.size() == 0 )
         {
             throw new VerificationException( "No goals specified" );
         }
         List allGoals = new ArrayList();
         allGoals.add( "clean:clean" );
         allGoals.addAll( goals );
 
         int ret = 0;
         try 
         {
             String prevUserDir = System.getProperty( "user.dir" );
             System.setProperty( "user.dir", basedir );
             System.setProperty( "classworlds.conf", mavenHome + "/bin/classworlds.conf" );
             URL classWorldsUrl = new URL( "file:" + mavenHome + "/core/classworlds-1.1-SNAPSHOT.jar" );
             ClassLoader cl = URLClassLoader.newInstance( new URL[] { classWorldsUrl } );
             Class c = Class.forName( "org.codehaus.classworlds.Launcher", true, cl );
             Method m = c.getMethod( "mainWithExitCode", new Class[] { String[].class } );
             Object o = m.invoke( null, new Object[] { allGoals.toArray( new String[0] ) } );
             System.setProperty( "user.dir", prevUserDir );
             ret = ( ( Integer ) o ).intValue();
         }
         catch ( Exception e )
         {
             throw new VerificationException( e );
         }
 
         if ( ret > 0 )
         {
             System.err.println( "Exit code: " + ret );
             throw new VerificationException();
         }
     }
 
     // ----------------------------------------------------------------------
     //
     // ----------------------------------------------------------------------
 
     public static void main( String args[] )
     {
         String basedir = System.getProperty( "user.dir" );
         localRepo = retrieveLocalRepo();
 
         List tests = null;
         try 
         {
             tests = loadFile( basedir, "integration-tests.txt" );
         }
         catch ( VerificationException e )
         {
             System.err.println( "Unable to load integration tests file" );
             System.err.println( e.getMessage() );
             System.exit( 2 );
         }
 
         if ( tests.size() == 0 )
         {
             System.out.println( "No tests to run" );
         }
 
         int exitCode = 0;
 
         for ( Iterator i = tests.iterator(); i.hasNext(); )
         {
             String test = ( String ) i.next(); 
 
             System.out.print( test + "... " );
 
             Verifier verifier = new Verifier( basedir + "/" + test );
 
             try
             {
                 verifier.executeHook( "prebuild-hook.txt" );
                 verifier.executeGoals( "goals.txt" );
                 verifier.executeHook( "postbuild-hook.txt" );
     
                 verifier.verify();
 
                 verifier.resetStreams();
 
                 System.out.println( "OK" );
             }
             catch ( VerificationException e )
             {
                 verifier.resetStreams();
 
                 System.out.println( "FAILED" );
 
                 verifier.displayStreamBuffers();
 
                 e.printStackTrace();
                 exitCode = 1;
             }
         }
         System.exit( exitCode );
     }
 }
 
