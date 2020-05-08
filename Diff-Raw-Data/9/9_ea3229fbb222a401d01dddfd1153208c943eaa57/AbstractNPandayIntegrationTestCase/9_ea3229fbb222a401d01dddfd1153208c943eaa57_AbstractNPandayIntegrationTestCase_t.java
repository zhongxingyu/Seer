 package npanday.its;
 
 /*
  * Copyright 2009
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import junit.framework.TestCase;
 import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
 import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
 import org.apache.maven.artifact.versioning.VersionRange;
 import org.apache.maven.it.VerificationException;
 import org.apache.maven.it.Verifier;
 import org.apache.maven.it.util.FileUtils;
 import org.apache.maven.it.util.IOUtil;
 import org.apache.maven.it.util.ResourceExtractor;
 import org.apache.maven.it.util.cli.CommandLineException;
 import org.apache.maven.it.util.cli.CommandLineUtils;
 import org.apache.maven.it.util.cli.Commandline;
 import org.apache.maven.it.util.cli.StreamConsumer;
 import org.apache.maven.it.util.cli.WriterStreamConsumer;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 public abstract class AbstractNPandayIntegrationTestCase
     extends TestCase
 {
     protected boolean skip;
 
     protected String skipReason;
 
     private static final String NPANDAY_MAX_FRAMEWORK_VERSION_PROPERTY = "npanday.framework.version";
 
     private static final String NPANDAY_VERSION_SYSTEM_PROPERTY = "npanday.version";
 
     private static DefaultArtifactVersion version = checkVersion();
 
     private static DefaultArtifactVersion frameworkVersion = checkFrameworkVersion();
 
     private static boolean debugMaven = Boolean.valueOf( System.getProperty( "debug.maven", "false" ) );
 
     private static boolean forceVersion = Boolean.valueOf( System.getProperty( "npanday.version.force", "false" ) );
 
     private static final Pattern PATTERN = Pattern.compile( "(.*?)-(RC[0-9]+|SNAPSHOT)" );
 
     private static String disasmArg;
 
     private static String disasmExec;
 
     protected AbstractNPandayIntegrationTestCase()
     {
         this( "(0,)" );
     }
 
     protected AbstractNPandayIntegrationTestCase( String versionRangeStr )
     {
         VersionRange versionRange = createVersionRange( versionRangeStr );
 
         if ( !checkNPandayVersion( versionRange, version ) && !forceVersion )
         {
             skip = true;
             skipReason = "NPanday version " + version + " not in range " + versionRange;
         }
     }
 
     protected static boolean checkNPandayVersion( VersionRange versionRange, DefaultArtifactVersion version )
     {
         String v = version.toString();
 
         Matcher m = PATTERN.matcher( v );
         if ( m.matches() )
         {
             return versionRange.containsVersion( new DefaultArtifactVersion( m.group( 1 ) ) );
         }
         else
         {
             return versionRange.containsVersion( version );
         }
     }
 
     protected AbstractNPandayIntegrationTestCase( String versionRangeStr, String frameworkVersionStr )
     {
         this( versionRangeStr );
 
         VersionRange versionRange = createVersionRange( frameworkVersionStr );
 
         if ( frameworkVersion != null && !versionRange.containsVersion( frameworkVersion ) && !forceVersion )
         {
             skip = true;
             skipReason = "Framework version " + frameworkVersion + " not in range " + versionRange;
         }
     }
 
     private static DefaultArtifactVersion checkVersion()
     {
         DefaultArtifactVersion version = null;
         String v = System.getProperty( NPANDAY_VERSION_SYSTEM_PROPERTY );
         if ( v != null )
         {
             version = new DefaultArtifactVersion( v );
             System.out.println( "Using NPanday version " + version );
         }
         else
         {
             System.out.println( "No NPanday version given" );
         }
         return version;
     }
 
     private static DefaultArtifactVersion checkFrameworkVersion()
     {
         DefaultArtifactVersion version = null;
         String v = System.getProperty( NPANDAY_MAX_FRAMEWORK_VERSION_PROPERTY );
         if ( v != null )
         {
             version = new DefaultArtifactVersion( v );
             System.out.println( "Using Framework versions <= " + version );
         }
         else
         {
             // TODO: this is not necessarily accurate. While it gets all those available, the max should actually be
             //       the one in the path (which can be obtained from the output for csc, but there may be other better
             //       ways such as a small C# app to interrogate it.
             //       It may be best to have an NPanday plugin that can reveal it then call that first to set it,
             //       reusing the vendor info
 
             File versions = new File( System.getenv( "systemroot" ) + "\\Microsoft.NET\\Framework" );
             if ( versions.exists() )
             {
                 List<DefaultArtifactVersion> frameworkVersions = new ArrayList<DefaultArtifactVersion>();
                 String[] list = versions.list( new java.io.FilenameFilter()
                 {
                     public boolean accept( File parent, String name )
                     {
                         File f = new File( parent, name );
                         // Mscorlib.dll can be used to detect 2.0 SDK, Microsoft.CompactFramework.Build.Tasks.dll for 3.5 SDK
                         // Having just the runtime (without these files) is not sufficient
                         return f.isDirectory() && ( new File( f, "Mscorlib.dll" ).exists() || new File( f,
                                                                                                         "Microsoft.CompactFramework.Build.Tasks.dll" ).exists() );
                     }
                 } );
                 if ( list != null && list.length > 0 )
                 {
                     for ( String frameworkVersion : list )
                     {
                         frameworkVersions.add( new DefaultArtifactVersion( frameworkVersion ) );
                     }
                     Collections.sort( frameworkVersions );
                     System.out.println( "Available framework versions: " + frameworkVersions );
                     version = frameworkVersions.get( frameworkVersions.size() - 1 );
                     System.out.println( "Selected framework version: " + version );
                 }
             }
             if ( version == null )
             {
                 System.out.println( "No Framework version given - attempting to use all" );
             }
         }
         return version;
     }
 
     protected static VersionRange createVersionRange( String versionRangeStr )
     {
         VersionRange versionRange;
         try
         {
             versionRange = VersionRange.createFromVersionSpec( versionRangeStr );
         }
         catch ( InvalidVersionSpecificationException e )
         {
             throw new RuntimeException( "Invalid version range: " + versionRangeStr + " - " + e.getMessage(), e );
         }
         return versionRange;
     }
 
     protected void runTest()
         throws Throwable
     {
         System.out.println();
        System.out.print( String.format("%1$-70s", getITName() + "." + getName()));
 
         if ( skip )
         {
             System.out.print( " Skipping (" + skipReason + ")" );
             return;
         }
 
         try
         {
             super.runTest();
            System.out.print( "OK" );
         }
         catch ( Throwable t )
         {
            // TODO: this actually never happens on my machine. me@lcorneliussen.de
            System.out.print( "FAILURE" );
             throw t;
         }
     }
 
     private String getITName()
     {
         String simpleName = getClass().getSimpleName();
         simpleName = simpleName.endsWith( "Test" ) ? simpleName.substring( 0, simpleName.length() - 4 ) : simpleName;
         return simpleName;
     }
 
     protected Verifier getVerifier( File testDirectory )
         throws VerificationException
     {
         Verifier verifier;
         if ( debugMaven )
         {
             verifier = new Verifier( testDirectory.getAbsolutePath() ) {
                 public String getExecutable() { return super.getExecutable() + "Debug"; }
             };
         }
         else
         {
             verifier = new Verifier( testDirectory.getAbsolutePath() );
         }
         List<String> cliOptions = new ArrayList<String>( 2 );
         cliOptions.add( "-Dnpanday.version=" + version );
         verifier.setCliOptions( cliOptions );
         return verifier;
     }
 
     protected String getCommentsFile()
     {
         return "target/comments.xml";
     }
 
     protected String getBuildSourcesMain( String fileName )
     {
         return getBuildFile( "build-sources", fileName );
     }
 
     protected String getBuildSourcesGenerated( String fileName )
     {
         return getBuildSourcesMain( fileName );
     }
 
     protected String getTestSourcesMain( String fileName )
     {
         return getBuildFile( "build-test-sources", fileName );
     }
 
     protected String getTestSourcesGenerated( String fileName )
     {
         return getTestSourcesMain( fileName );
     }
 
     protected String getBuildFile( String buildDirectory, String fileName )
     {
         return "target/" + buildDirectory + "/" + fileName;
     }
 
     protected String getAssemblyFile( String assemblyName, String type )
     {
         return getAssemblyFile( assemblyName, null, type, null );
     }
 
     protected String getAssemblyFile( String assemblyName, String version, String type )
     {
         return getAssemblyFile( assemblyName, version, type, null );
     }
 
     protected String getAssemblyFile( String assemblyName, String version, String type, String classifier )
     {
         return getAssemblyFilePath( "target", assemblyName, type );
     }
 
     protected void clearRdfCache()
         throws IOException
     {
         FileUtils.deleteDirectory( new File( System.getProperty( "user.home" ), ".m2/uac/rdfRepository" ) );
     }
 
     protected void deleteArtifact( Verifier verifier, String groupId, String artifactId, String version, String type )
         throws IOException
     {
         FileUtils.deleteDirectory( new File( System.getProperty( "user.home" ), ".m2/uac/gac_msil/" + artifactId + "/" + version + "__" + groupId ) );
         verifier.deleteArtifact( groupId, artifactId, version, type );
     }
 
     protected void assertClassPresent( String assembly, String className )
         throws VerificationException
     {
         if ( !isClassPresent( assembly, className ) )
         {
             fail( "Unable to find class " + className + " in assembly " + assembly );
         }
     }
 
     protected void assertClassNotPresent( String assembly, String className )
         throws VerificationException
     {
         if ( isClassPresent( assembly, className ) )
         {
             fail( "Found unexpected class " + className + " in assembly " + assembly );
         }
     }
 
     private boolean isClassPresent( String assembly, String className )
         throws VerificationException
     {
         String output = runILDisasm( assembly );
 
         String currentClassName = className;
 
         int index = className.indexOf( '.' );
         String namespace = index > 0 ? className.substring( 0, index ) : "";
         for ( String line : output.split( "\n" ) )
         {
             line = line.trim();
             // mono disassembles like this instead
             if ( line.startsWith( ".namespace " + namespace ) )
             {
                 currentClassName = className.substring( namespace.length() + 1 );
             }
 
             if ( line.startsWith( ".class " ) && line.endsWith( currentClassName ) )
             {
                 return true;
             }
         }
         return false;
     }
 
     private String runILDisasm( String assembly )
         throws VerificationException
     {
         if ( disasmExec != null )
         {
             if ( disasmArg != null )
             {
                 return execute( disasmExec, new String[]{disasmArg, assembly} );
             }
             else
             {
                 return execute( disasmExec, new String[]{assembly} );
             }
         }
 
         String value = null;
         try
         {
             value = execute( "ildasm", new String[]{"/text", assembly} );
             disasmExec = "ildasm";
             disasmArg = "/text";
         }
         catch ( VerificationException e )
         {
             value = execute( "monodis", new String[]{assembly} );
             disasmExec = "monodis";
             disasmArg = null;
         }
         return value;
     }
 
     private String execute( String executable, String[] args )
         throws VerificationException
     {
         try
         {
             Commandline cli = new Commandline();
             cli.setExecutable( executable );
             for ( String arg : args )
             {
                 cli.createArgument().setValue( arg );
             }
 
             Writer logWriter = new StringWriter();
 
             StreamConsumer out = new WriterStreamConsumer( logWriter );
 
             StreamConsumer err = new WriterStreamConsumer( logWriter );
 
             System.err.println( "Command: " + Commandline.toString( cli.getCommandline() ) );
 
             int ret = CommandLineUtils.executeCommandLine( cli, out, err );
 
             logWriter.close();
 
             String output = logWriter.toString();
             if ( ret > 0 )
             {
                 System.err.println( output );
                 throw new VerificationException( "Exit code: " + ret );
             }
 
             return output;
         }
         catch ( CommandLineException e )
         {
             throw new VerificationException( e );
         }
         catch ( IOException e )
         {
             throw new VerificationException( e );
         }
     }
 
     protected String getTestAssemblyFile( String artifactId, String version, String type )
     {
         String basedir = "target/test-assemblies";
         return getAssemblyFilePath( basedir, artifactId, type );
     }
 
     private String getAssemblyFilePath( String basedir, String artifactId, String type )
     {
         StringBuilder sb = new StringBuilder();
         sb.append( basedir );
         sb.append( "/" );
         sb.append( artifactId );
         sb.append( "." );
         sb.append( type );
         return sb.toString();
     }
 
     protected String getAssemblyResourceFile( String fileName )
     {
         return getBuildFile( "assembly-resources", fileName );
     }
 
     protected void assertResourcePresent( String assembly, String resource )
         throws VerificationException
     {
         if ( !isResourcePresent( assembly, resource ) )
         {
             fail( "Unable to find resource " + resource + " in assembly " + assembly );
         }
     }
 
     protected void assertResourcePresent( String assembly, String assemblyName, String resource )
         throws VerificationException
     {
         if ( !isResourcePresent( assembly, assemblyName, resource ) )
         {
             fail( "Unable to find resource " + resource + " in assembly " + assembly );
         }
     }
 
     private boolean isResourcePresent( String assembly, String resource )
         throws VerificationException
     {
         return isResourcePresent( assembly, getAssemblyName( assembly ), resource );
     }
 
     private boolean isResourcePresent( String assembly, String assemblyName, String resource )
         throws VerificationException
     {
         String output = runILDisasm( assembly );
 
         String prefix = ".mresource public ";
         String value = assemblyName + "." + resource.replace( '/', '.' );
         for ( String line : output.split( "\n" ) )
         {
             line = line.trim();
             if ( line.startsWith( prefix ) )
             {
                 line = line.substring( prefix.length() );
                 if ( line.equals( value ) || line.equals( "'" + value + "'" ) )
                 {
                     return true;
                 }
             }
         }
         return false;
     }
 
     private String getAssemblyName( String assembly )
     {
         return assembly.substring( assembly.lastIndexOf( File.separatorChar ) + 1, assembly.lastIndexOf( '.' ) );
     }
 
     protected void assertPublicKey( String assembly )
         throws VerificationException
     {
         if ( !hasPublicKey( assembly ) )
         {
             fail( "Couldn't find public key in assembly " + assembly );
         }
     }
 
     private boolean hasPublicKey( String assembly )
         throws VerificationException
     {
         String output = runILDisasm( assembly );
 
         boolean insideCorrectAssembly = false;
         for ( String line : output.split( "\n" ) )
         {
             if ( line.startsWith( ".assembly " + getAssemblyName( assembly ) ) )
             {
                 insideCorrectAssembly = true;
             }
             else if ( line.startsWith( "}" ) )
             {
                 insideCorrectAssembly = false;
             }
             else if ( insideCorrectAssembly && line.trim().startsWith( ".publickey" ) )
             {
                 return true;
             }
         }
         return false;
     }
 
     protected static boolean checkNPandayVersion( String versionRangeStr )
     {
         return checkNPandayVersion( createVersionRange( versionRangeStr ), version ) || forceVersion;
     }
 
     protected File unzipResources( String resourcePath )
         throws IOException
     {
         String tempDirPath = System.getProperty( "maven.test.tmpdir", System.getProperty( "java.io.tmpdir" ) );
         File tempDir = new File( tempDirPath );
 
         File testDir = new File( tempDir, resourcePath.substring( 0, resourcePath.length() - 4 ) );
 
         FileUtils.deleteDirectory( testDir );
 
         File f = ResourceExtractor.extractResourcePath( getClass(), resourcePath, tempDir, true );
 
         ZipFile zipFile = new ZipFile( f );
         try
         {
             for ( Enumeration entries = zipFile.entries(); entries.hasMoreElements(); )
             {
                 ZipEntry entry = (ZipEntry) entries.nextElement();
 
                 File file = new File( testDir.getParentFile(), entry.getName() );
                 file.getParentFile().mkdirs();
                 if ( entry.isDirectory() )
                 {
                     file.mkdir();
                     continue;
                 }
 
                 InputStream inputStream = null;
                 FileOutputStream outputStream = null;
                 try
                 {
                     inputStream = zipFile.getInputStream( entry );
                     outputStream = new FileOutputStream( file );
                     IOUtil.copy( inputStream, outputStream );
                 }
                 finally
                 {
                     IOUtil.close( inputStream );
                     IOUtil.close( outputStream );
                 }
             }
         }
         finally
         {
             zipFile.close();
         }
 
         return testDir;
     }
 }
