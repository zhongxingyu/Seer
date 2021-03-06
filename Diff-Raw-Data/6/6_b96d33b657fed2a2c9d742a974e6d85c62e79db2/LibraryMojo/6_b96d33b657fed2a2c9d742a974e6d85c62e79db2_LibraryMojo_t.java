 /**
  * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
  * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package info.rvin.mojo.flexmojo.compiler;
 
 /*
  * Copyright 2001-2005 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import static info.rvin.flexmojos.utilities.MavenUtils.resolveArtifact;
 import flex2.tools.oem.Library;
 import info.flexmojos.compatibilitykit.FlexCompatibility;
 import info.flexmojos.utilities.PathUtil;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.model.Resource;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 /**
  * Goal which compiles the Flex sources into a library for either Flex or AIR depending.
  * 
  * @goal compile-swc
  * @requiresDependencyResolution
  * @phase compile
  */
 public class LibraryMojo
     extends AbstractFlexCompilerMojo<Library>
 {
 
     /**
      * Enable or disable the computation of a digest for the created swf library. This is equivalent to using the
      * <code>compiler.computDigest</code> in the compc compiler.
      * 
      * @parameter default-value="true"
      */
     private boolean computeDigest;
 
     /**
      * This is the equilvalent of the <code>include-classes</code> option of the compc compiler.<BR>
      * Usage:
      * 
      * <pre>
      * &lt;includeClasses&gt;
      *   &lt;class&gt;AClass&lt;/class&gt;
      *   &lt;class&gt;BClass&lt;/class&gt;
      * &lt;/includeClasses&gt;
      * </pre>
      * 
      * @parameter
      */
     private String[] includeClasses;
 
     /**
      * This is equilvalent to the <code>include-file</code> option of the compc compiler.<BR>
      * Usage:
      * 
      * <pre>
      * &lt;includeFiles&gt;
      *   &lt;file&gt;${baseDir}/anyFile.txt&lt;/file&gt;
      * &lt;/includeFiles&gt;
      * </pre>
      * 
      * @parameter
      */
     private File[] includeFiles;
 
     /**
      * This is equilvalent to the <code>include-namespaces</code> option of the compc compiler.<BR>
      * Usage:
      * 
      * <pre>
      * &lt;includeNamespaces&gt;
      *   &lt;namespace&gt;http://www.adobe.com/2006/mxml&lt;/namespace&gt;
      * &lt;/includeNamespaces&gt;
      * </pre>
      * 
      * @parameter
      */
     private String[] includeNamespaces;
 
     /**
      * This is equilvalent to the <code>include-resource-bundles</code> option of the compc compiler.<BR>
      * Usage:
      * 
      * <pre>
      * &lt;includeResourceBundles&gt;
      *   &lt;bundle&gt;SharedResources&lt;/bundle&gt;
      *   &lt;bundle&gt;collections&lt;/bundle&gt;
      *   &lt;bundle&gt;containers&lt;/bundle&gt;
      * &lt;/includeResourceBundles&gt;
      * </pre>
      * 
      * @parameter
      */
     private String[] includeResourceBundles;
 
     /**
      * @parameter TODO check if is used/useful
      */
     private MavenArtifact[] includeResourceBundlesArtifact;
 
     /**
      * This is the equilvalent of the <code>include-sources</code> option of the compc compiler.<BR>
      * Usage:
      * 
      * <pre>
      * &lt;includeSources&gt;
      *   &lt;sources&gt;${baseDir}/src/main/flex&lt;/sources&gt;
      * &lt;/includeSources&gt;
      * </pre>
      * 
      * @parameter
      */
     protected File[] includeSources;
 
     /**
      * Sets the RSL output directory.
      * 
      * @parameter
      */
     private File directory;
 
     /*
      * TODO how to set this on flex-compiler-oem -include-lookup-only private boolean includeLookupOnly;
      */
 
     /**
      * Adds a CSS stylesheet to this <code>Library</code> object. This is equilvalent to the
      * <code>include-stylesheet</code> option of the compc compiler.<BR>
      * Usage:
      * 
      * <pre>
      * &lt;includeStylesheet&gt;
      *   &lt;stylesheet&gt;
      *     &lt;name&gt;style1&lt;/name&gt;
      *     &lt;path&gt;${baseDir}/src/main/flex/style1.css&lt;/path&gt;
      *   &lt;/stylesheet&gt;
      * &lt;/includeStylesheet&gt;
      * </pre>
      * 
      * @parameter
      */
     private Stylesheet[] includeStylesheet;
 
     /**
      * Turn on generation of debuggable SWFs. False by default for mxmlc, but true by default for compc.
      * 
      * @parameter default-value="true"
      */
     private boolean debug;
 
     @Override
     public void setUp()
         throws MojoExecutionException, MojoFailureException
     {
         // need to initialize builder before go super
         builder = new Library();
 
         if ( directory != null )
         {
             builder.setDirectory( directory );
         }
 
         super.setUp();
 
         if ( outputFile == null )
         {
             if ( output == null )
             {
                 outputFile = new File( build.getDirectory(), build.getFinalName() + ".swc" );
             }
             else
             {
                 outputFile = new File( build.getDirectory(), output );
             }
         }
 
         builder.setOutput( outputFile );
 
         if ( checkNullOrEmpty( includeClasses ) && checkNullOrEmpty( includeFiles )
             && checkNullOrEmpty( includeNamespaces ) && checkNullOrEmpty( includeResourceBundles )
             && checkNullOrEmpty( includeResourceBundlesArtifact ) && checkNullOrEmpty( includeSources )
             && checkNullOrEmpty( includeStylesheet ) )
         {
             getLog().warn( "Nothing expecified to include.  Assuming source and resources folders." );
             includeSources = sourcePaths.clone();
             includeFiles = listAllResources();
         }
 
         if ( !checkNullOrEmpty( includeClasses ) )
         {
             for ( String asClass : includeClasses )
             {
                 builder.addComponent( asClass );
             }
         }
 
         if ( !checkNullOrEmpty( includeFiles ) )
         {
             for ( File file : includeFiles )
             {
                 if ( file == null || !file.isFile() )
                 {
                     throw new MojoFailureException( "File " + file + " not found" );
                 }
 
                 File folder = getResourceFolder( file );
 
                 // If the resource is external to project add on root
                 String relativePath = PathUtil.getRelativePath( folder, file );
                 if ( relativePath.startsWith( ".." ) )
                 {
                     relativePath = file.getName();
                 }
 
                 builder.addArchiveFile( relativePath.replace( '\\', '/' ), file );
             }
         }
 
         if ( !checkNullOrEmpty( includeNamespaces ) )
         {
             for ( String uri : includeNamespaces )
             {
                 try
                 {
                     builder.addComponent( new URI( uri ) );
                 }
                 catch ( URISyntaxException e )
                 {
                     throw new MojoExecutionException( "Invalid URI " + uri, e );
                 }
             }
         }
 
         if ( !checkNullOrEmpty( includeResourceBundles ) )
         {
             for ( String rb : includeResourceBundles )
             {
                 builder.addResourceBundle( rb );
             }
         }
 
         if ( !checkNullOrEmpty( includeResourceBundlesArtifact ) )
         {
             for ( MavenArtifact mvnArtifact : includeResourceBundlesArtifact )
             {
                 Artifact artifact =
                     artifactFactory.createArtifactWithClassifier( mvnArtifact.getGroupId(),
                                                                   mvnArtifact.getArtifactId(),
                                                                   mvnArtifact.getVersion(), "properties",
                                                                   "resource-bundle" );
                 resolveArtifact( artifact, resolver, localRepository, remoteRepositories );
                 String bundleFile;
                 try
                 {
                     bundleFile = FileUtils.readFileToString( artifact.getFile() );
                 }
                 catch ( IOException e )
                 {
                     throw new MojoExecutionException( "Ocorreu um erro ao ler o artefato " + artifact, e );
                 }
                 String[] bundles = bundleFile.split( " " );
                 for ( String bundle : bundles )
                 {
                     builder.addResourceBundle( bundle );
                 }
             }
         }
 
         if ( !checkNullOrEmpty( includeSources ) )
         {
             for ( File file : includeSources )
             {
                 if ( file == null )
                 {
                     throw new MojoFailureException( "Cannot include a null file" );
                 }
                 if ( !file.getName().contains( "{locale}" ) && !file.exists() )
                 {
                     throw new MojoFailureException( "File " + file.getName() + " not found" );
                 }
                 builder.addComponent( file );
             }
         }
 
         includeStylesheet();
 
         computeDigest();
 
         builder.addArchiveFile( "maven/" + project.getGroupId() + "/" + project.getArtifactId() + "/pom.xml",
                                 new File( project.getBasedir(), "pom.xml" ) );
     }
 
     @SuppressWarnings( "unchecked" )
     private File[] listAllResources()
     {
         List<File> resources = new ArrayList<File>();
 
         List<Resource> resourcesDirs = project.getResources();
         for ( Resource resource : resourcesDirs )
         {
             File resourceDir = new File( resource.getDirectory() );
             if ( !resourceDir.exists() )
             {
                 continue;
             }
 
            Collection<File> files =
                FileUtils.listFiles( resourceDir, HiddenFileFilter.VISIBLE, TrueFileFilter.INSTANCE );
             resources.addAll( files );
         }
 
         return resources.toArray( new File[resources.size()] );
     }
 
     @FlexCompatibility( minVersion = "3" )
     private void computeDigest()
     {
         configuration.enableDigestComputation( computeDigest );
     }
 
     @FlexCompatibility( minVersion = "3" )
     private void includeStylesheet()
         throws MojoExecutionException
     {
         if ( !checkNullOrEmpty( includeStylesheet ) )
         {
             for ( Stylesheet sheet : includeStylesheet )
             {
                 if ( !sheet.getPath().exists() )
                 {
                     throw new MojoExecutionException( "Stylesheet not found: " + sheet.getPath() );
                 }
                 builder.addStyleSheet( sheet.getName(), sheet.getPath() );
             }
         }
     }
 
     private File getResourceFolder( File file )
     {
         String absolutePath = file.getAbsolutePath();
         for ( File sourcePath : sourcePaths )
         {
             if ( absolutePath.startsWith( sourcePath.getAbsolutePath() ) )
             {
                 return sourcePath;
             }
         }
         return project.getBasedir();
     }
 
     private boolean checkNullOrEmpty( Object[] array )
     {
         if ( array == null )
         {
             return true;
         }
 
         if ( array.length == 0 )
         {
             return false;
         }
 
         return false;
     }
 
     @Override
     protected void writeResourceBundle( String[] bundles, String locale, File localePath )
         throws MojoExecutionException
     {
         getLog().info( "Generating resource-bundle for " + locale );
 
         Library localized = new Library();
         localized.setConfiguration( configuration );
         configuration.setLibraryPath( new File[0] );
 
         localized.setLogger( new CompileLogger( getLog() ) );
 
         configuration.addLibraryPath( new File[] { outputFile } );
         setLocales( new String[] { locale } );
 
         configuration.setSourcePath( new File[] { localePath } );
         for ( String bundle : bundles )
         {
             localized.addResourceBundle( bundle );
         }
         configuration.addLibraryPath( getResourcesBundles( locale ) );
 
         File output = new File( build.getDirectory(), build.getFinalName() + "-" + locale + ".rb.swc" );
 
         localized.setOutput( output );
 
         build( localized );
 
         projectHelper.attachArtifact( project, "resource-bundle", locale, output );
     }
 
     @Override
     protected boolean isDebug()
     {
         return this.debug;
     }
 
     @Override
     protected boolean isApplication()
     {
         return false;
     }
 
     @Override
     protected String getDefaultLocale()
     {
         throw new UnsupportedOperationException( "Default locale is not available to Libraries" );
     }
 
 }
