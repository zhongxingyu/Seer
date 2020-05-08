 package npanday.its;
 
 /*
  * Copyright 2009
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import junit.framework.TestCase;
 import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
 import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
 import org.apache.maven.artifact.versioning.VersionRange;
 import org.apache.maven.it.VerificationException;
 import org.apache.maven.it.Verifier;
 import org.apache.maven.it.util.FileUtils;
 
 public abstract class AbstractNPandayIntegrationTestCase
     extends TestCase
 {
     private boolean skip;
 
     private String skipReason;
 
     private static final String NPANDAY_MAX_FRAMEWORK_VERSION_PROPERTY = "npanday.framework.version";
 
     private static final String NPANDAY_VERSION_SYSTEM_PROPERTY = "npanday.version";
 
     private static DefaultArtifactVersion version = checkVersion();
 
     private static DefaultArtifactVersion frameworkVersion = checkFrameworkVersion();
 
     private static boolean forceVersion = Boolean.valueOf( System.getProperty( "npanday.version.force", "false" ) );
 
     protected AbstractNPandayIntegrationTestCase()
     {
         this( "(0,)" );
     }
 
     protected AbstractNPandayIntegrationTestCase( String versionRangeStr )
     {
         VersionRange versionRange = createVersionRange( versionRangeStr );
 
         if ( !versionRange.containsVersion( version ) && !forceVersion )
         {
             skip = true;
             skipReason = "NPanday version " + version + " not in range " + versionRange;
         }
     }
 
     protected AbstractNPandayIntegrationTestCase( String versionRangeStr, String frameworkVersionStr )
     {
         this( versionRangeStr );
 
         VersionRange versionRange = createVersionRange( frameworkVersionStr );
 
         if ( !versionRange.containsVersion( frameworkVersion ) && !forceVersion )
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
             //       It may be best to have an NPanday plugin that can reveal it then call that first to set it
 
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
                         return f.isDirectory() && ( new File( f, "Mscorlib.dll" ).exists() ||
                             new File( f, "Microsoft.CompactFramework.Build.Tasks.dll" ).exists() );
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
 
     private static VersionRange createVersionRange( String versionRangeStr )
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
         System.out.print( getITName() + "(" + getName() + ").." );
 
         if ( skip )
         {
             System.out.println( " Skipping (" + skipReason + ")" );
             return;
         }
 
         try
         {
             super.runTest();
             System.out.println( " Ok" );
         }
         catch ( Throwable t )
         {
             System.out.println( " Failure" );
             throw t;
         }
     }
 
     private String getITName()
     {
         String simpleName = getClass().getName();
         int idx = simpleName.lastIndexOf( '.' );
         simpleName = idx >= 0 ? simpleName.substring( idx + 1 ) : simpleName;
         simpleName = simpleName.startsWith( "NPandayIT" ) ? simpleName.substring( "NPandayIT".length() ) : simpleName;
         simpleName = simpleName.endsWith( "Test" ) ? simpleName.substring( 0, simpleName.length() - 4 ) : simpleName;
         return simpleName;
     }
 
     protected Verifier getVerifier( File testDirectory )
         throws VerificationException
     {
         Verifier verifier = new Verifier( testDirectory.getAbsolutePath() );
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
 
     private String getBuildFile( String buildDirectory, String fileName )
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
         StringBuilder sb = new StringBuilder();
         sb.append( "target/" );
         sb.append( assemblyName );
         sb.append( "." );
         sb.append( type );
         return sb.toString();
     }
 
     protected void clearRdfCache()
         throws IOException
     {
         FileUtils.deleteDirectory( new File( System.getProperty( "user.home" ), ".m2/uac/rdfRepository" ) );
     }
 }
