 /*
  * Copyright (c) 2009, Paul Merlin. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package org.codeartisans.mojo.flamingo;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.util.Locale;
 import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 import org.codehaus.plexus.util.FileUtils;
 
 import org.pushingpixels.flamingo.api.svg.SvgTranscoder;
 import org.pushingpixels.flamingo.api.svg.TranscoderListener;
 
 /**
  * @goal transcode
  */
 public final class SvgTranscoderMojo
         extends AbstractMojo
 {
 
     // Configuration ---------------------------------------------------------------------------------------------------
     /**
      * @parameter
      * @required
      */
     private File sourceDirectory;
     /**
      * @parameter
      * @required
      */
     private String outputPackage;
     /**
      * @parameter default-value="false"
      */
     private boolean implementsResizableIcon;
     /**
      * @parameter default-value="true"
      */
     private boolean stopOnFailure;
     // Context ---------------------------------------------------------------------------------------------------------
     /**
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     private MavenProject project;
 
     @Override
     public void execute()
             throws MojoExecutionException, MojoFailureException
     {
         try {
 
             File generatedSources = new File( new File( new File( project.getBuild().getDirectory() ), "generated-sources" ), "flamingo" );
             FileUtils.forceMkdir( generatedSources );
 
             project.addCompileSourceRoot( generatedSources.getAbsolutePath() );
 
             File[] svgFiles = sourceDirectory.listFiles( new SvgFilenameFilter() );
             int transcoded = 0;
             int toTranscode = svgFiles.length;
 
             for ( File eachSvg : svgFiles ) {
 
                 String svgClassName = classNameFromFileName( eachSvg.getName() ) + "Icon";
                File java2dClassFileDirectory = new File( generatedSources + File.separator + outputPackage.replaceAll( "\\.", Matcher.quoteReplacement(File.separator )) );
                 String javaClassFilename = java2dClassFileDirectory + File.separator + svgClassName + ".java";
 
                 FileUtils.forceMkdir( java2dClassFileDirectory );
 
                 try {
 
                     CountDownLatch latch = new CountDownLatch( 1 );
                     PrintWriter pw = new PrintWriter( javaClassFilename );
                     SvgTranscoder transcoder = new SvgTranscoder( eachSvg.toURI().toURL().toString(), svgClassName );
                     transcoder.setJavaToImplementResizableIconInterface( implementsResizableIcon );
                     transcoder.setJavaPackageName( outputPackage );
                     transcoder.setListener( new SvgTranscodeListener( latch, pw ) );
                     transcoder.transcode();
                     latch.await();
 
                     transcoded++;
 
                 } catch ( Throwable ex ) {
                     if ( stopOnFailure ) {
                         throw new MojoExecutionException( "Unable to transcode: " + eachSvg.getAbsolutePath(), ex );
                     } else {
                         getLog().warn( "Unable to transcode: " + eachSvg.getAbsolutePath() );
                         if ( getLog().isDebugEnabled() ) {
                             getLog().debug( ex.getMessage(), ex );
                         }
                     }
                 }
             }
 
             getLog().info( "Transcoded " + transcoded + '/' + toTranscode + " svg files in " + outputPackage + " package" );
 
         } catch ( IOException ex ) {
             throw new MojoFailureException( ex.getMessage(), ex );
         }
     }
 
     private static class SvgFilenameFilter
             implements FilenameFilter
     {
 
         @Override
         public boolean accept( File dir, String name )
         {
             return name.endsWith( ".svg" );
         }
 
     }
 
     private static class SvgTranscodeListener
             implements TranscoderListener
     {
 
         private final CountDownLatch latch;
         private final Writer writer;
 
         public SvgTranscodeListener( CountDownLatch latch, Writer writer )
         {
             this.latch = latch;
             this.writer = writer;
         }
 
         @Override
         public Writer getWriter()
         {
             return writer;
         }
 
         @Override
         public void finished()
         {
             latch.countDown();
         }
 
     }
 
     private static String classNameFromFileName( final String filename )
     {
         String className = removeExtension( filename );
         // CHECKSTYLE:OFF We use the Locale.ENGLISH on purpose to get rid of any special characters
         className = filename.toUpperCase( Locale.ENGLISH ).toLowerCase();
         // CHECKSTYLE:ON
         className = className.replace( '-', ' ' );
         className = className.replace( '_', ' ' );
         className = upperCaseFirstLetterOfWords( className );
         className = className.replace( ".", "" );
         className = className.replace( " ", "" );
         return className;
     }
 
     /* package */ static String removeExtension( String filename )
     {
         return filename.substring( 0, filename.lastIndexOf( '.' ) );
     }
 
     private static String upperCaseFirstLetterOfWords( final String input )
     {
         if ( input == null || input.length() < 1 ) {
             return input;
         }
         char ch;
         char prevCh = '.';
         int i;
         final StringBuffer sb = new StringBuffer( input.length() );
         for ( i = 0; i < input.length(); i++ ) {
             ch = input.charAt( i );
             if ( Character.isLetter( ch ) && !Character.isLetter( prevCh ) ) {
                 sb.append( Character.toUpperCase( ch ) );
             } else if ( Character.isLetter( ch ) ) {
                 sb.append( Character.toLowerCase( ch ) );
             } else {
                 sb.append( ch );
             }
             prevCh = ch;
         }
         return sb.toString();
     }
 
 }
