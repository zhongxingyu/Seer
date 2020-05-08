 /******************************************************************************* 
 * Copyright (c) 2010, 2011 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Holger Staudacher - initial API and implementation
 *******************************************************************************/
 package org.eclipse.libra.warproducts.core;
 
 import java.io.*;
 import java.lang.reflect.InvocationTargetException;
 import java.util.*;
 
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.osgi.service.resolver.BundleDescription;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.pde.core.plugin.*;
 import org.eclipse.pde.internal.build.*;
 import org.eclipse.pde.internal.core.ICoreConstants;
 import org.eclipse.pde.internal.core.PDECore;
 import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
 import org.eclipse.pde.internal.core.exports.FeatureExportOperation;
 import org.eclipse.pde.internal.core.ifeature.IFeature;
 import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
 import org.eclipse.pde.internal.core.iproduct.IJREInfo;
 import org.eclipse.pde.internal.core.iproduct.IProduct;
 import org.eclipse.pde.internal.core.util.CoreUtility;
 import org.w3c.dom.Element;
 
 public class WARProductExportOperation extends FeatureExportOperation {
 
   private static final String STATUS_MESSAGE = "!MESSAGE"; //$NON-NLS-1$
   private static final String STATUS_ENTRY = "!ENTRY"; //$NON-NLS-1$
   private static final String STATUS_SUBENTRY = "!SUBENTRY"; //$NON-NLS-1$
   private static final String MAC_JAVA_FRAMEWORK 
     = "/System/Library/Frameworks/JavaVM.framework"; //$NON-NLS-1$
   private String featureLocation;
   private String root;
   private IProduct product;
   protected static String errorMessage;
 
   public static void setErrorMessage( final String message ) {
     errorMessage = message;
   }
 
   public static String getErrorMessage() {
     return errorMessage;
   }
 
   public static IStatus parseErrorMessage( final CoreException e ) {
     IStatus result = null;
     if( errorMessage != null ) {
       MultiStatus status = null;
       StringTokenizer tokenizer = new StringTokenizer( errorMessage, "\n" );  //$NON-NLS-1$
       while( tokenizer.hasMoreTokens() ) {
         String line = tokenizer.nextToken().trim();
         if( line.startsWith( STATUS_ENTRY ) && tokenizer.hasMoreElements() ) {
           String next = tokenizer.nextToken();
           if( next.startsWith( STATUS_MESSAGE ) ) {
             status = new MultiStatus( PDECore.PLUGIN_ID,
                                       0,
                                       next.substring( 8 ),
                                       null );
           }
         } else if( line.startsWith( STATUS_SUBENTRY )
                    && tokenizer.hasMoreElements()
                    && status != null )
         {
           String next = tokenizer.nextToken();
           if( next.startsWith( STATUS_MESSAGE ) ) {
             status.add( new Status( IStatus.ERROR,
                                     PDECore.PLUGIN_ID,
                                     next.substring( 8 ) ) );
           }
         }
       }
       if( status != null ) {
         result = status;
       } else {
         // parsing didn't work, just set the message
         result = new MultiStatus( PDECore.PLUGIN_ID, 0, new IStatus[]{
           e.getStatus()
         }, errorMessage, null );
       }
     }
     return result;
   }
 
   public WARProductExportOperation( final FeatureExportInfo info,
                                     final String name,
                                     final IProduct product,
                                     final String root )
   {
     super( info, name );
     this.product = product;
     this.root = root;
   }
 
   protected IStatus run( final IProgressMonitor monitor ) {
     IStatus result = null;
     String[][] configurations = fInfo.targets;
     if( configurations == null ) {
       configurations = new String[][] {
         {
           TargetPlatform.getOS(),
           TargetPlatform.getWS(),
           TargetPlatform.getOSArch(),
           TargetPlatform.getNL()
         }
       };
     }
     cleanupBuildRepo();
     errorMessage = null;
     try {
       monitor.beginTask( "", 10 ); //$NON-NLS-1$
       try {
         // create a feature to wrap all plug-ins and features
         String featureID = "org.eclipse.pde.container.feature"; //$NON-NLS-1$
         featureLocation = fBuildTempLocation + File.separator + featureID;
         createFeature( featureID,
                        featureLocation,
                        configurations,
                        product.includeLaunchers() );
         createBuildPropertiesFile( featureLocation, configurations );
         doExport( featureID,
                   null,
                   featureLocation,
                   configurations,
                   new SubProgressMonitor( monitor, 8 ) );
       } catch( final IOException e ) {
         PDECore.log( e );
       } catch( final InvocationTargetException e ) {
         String problemMessage 
           = Messages.FeatureBasedExportOperation_ProblemDuringExport;
         result = new Status( IStatus.ERROR,
                              PDECore.PLUGIN_ID,
                              problemMessage,
                              e.getTargetException() );
       } catch( final CoreException e ) {
         if( errorMessage != null ) {
           result = parseErrorMessage( e );
         } else {
           result = e.getStatus();
         }
       } finally {
         // Clean up generated files
         for( int j = 0; j < fInfo.items.length; j++ ) {
           try {
             deleteBuildFiles( fInfo.items[ j ] );
           } catch( CoreException e ) {
             PDECore.log( e );
           }
         }
         cleanup( null, new SubProgressMonitor( monitor, 1 ) );
       }
       if( hasAntErrors() ) {
         String compilationErrors 
           = Messages.FeatureExportOperation_CompilationErrors;
         String bind = NLS.bind( compilationErrors,
                                 fInfo.destinationDirectory );
         result = new Status( IStatus.WARNING,
                              PDECore.PLUGIN_ID,
                              bind );
       }
     } finally {
       monitor.done();
       errorMessage = null;
     }
     if( result == null ) {
       result = Status.OK_STATUS;
     }
     return result;
   }
 
   protected boolean groupedConfigurations() {
     // we never group product exports
     return false;
   }
 
   private void cleanupBuildRepo() {
     File metadataTemp = new File( fBuildTempMetadataLocation );
     if( metadataTemp.exists() ) {
       // make sure our build metadata repo is clean
       deleteDir( metadataTemp );
     }
   }
 
   protected String[] getPaths() {
     String[] paths = super.getPaths();
     String[] all = new String[ paths.length + 1 ];
     all[ 0 ] = featureLocation
                + File.separator
                + ICoreConstants.FEATURE_FILENAME_DESCRIPTOR;
     System.arraycopy( paths, 0, all, 1, paths.length );
     return all;
   }
 
   private void createBuildPropertiesFile( final String featureLocation,
                                           final String[][] configurations ) 
     throws IOException
   {
     File file = new File( featureLocation );
     if( !file.exists() || !file.isDirectory() ) {
       file.mkdirs();
     }
     Properties properties = new Properties();
     handleRootFiles( configurations, properties );
     handleJREInfo( configurations, properties );
     handleExportSource( properties );
     File fileToSave 
       = new File( file, ICoreConstants.BUILD_FILENAME_DESCRIPTOR );
     save( fileToSave, properties, "Build Configuration" ); //$NON-NLS-1$
   }
 
   private void handleRootFiles( final String[][] configurations,
                                 final Properties properties ) 
     throws IOException
   {
     if( configurations.length > 0 ) {
       String rootPrefix = IBuildPropertiesConstants.ROOT_PREFIX
                           + configurations[ 0 ][ 0 ]
                           + "." + configurations[ 0 ][ 1 ]  //$NON-NLS-1$
                           + "." + configurations[ 0 ][ 2 ]; //$NON-NLS-1$
       properties.put( rootPrefix, getRootFileLocations( false ) );
       prepareWARFile( properties, rootPrefix );
     }
   }
   
   private void prepareWARFile( final Properties properties,
                                final String rootPrefix ) 
     throws IOException
   {
     if( product instanceof IWARProduct ) {
       IWARProduct warProduct = ( IWARProduct )product;
       IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
       IFile launchIni = wsRoot.getFile( warProduct.getLaunchIni() );
       String launchIniPath 
         = createWarContent( launchIni.getLocation(), "launch.ini" ); //$NON-NLS-1$
       IFile webXml = wsRoot.getFile( warProduct.getWebXml() );
       String webXmlPath = createWarContent( webXml.getLocation(), "web.xml" ); //$NON-NLS-1$
       properties.put( rootPrefix, "absolute:file:" + webXmlPath  //$NON-NLS-1$
                                   + ",absolute:file:" + launchIniPath ); //$NON-NLS-1$
       String libDir = createLibDir();
       copyLibraries( libDir );
       properties.put( rootPrefix + ".folder." + "lib", "absolute:" + libDir ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
     }
   }
 
   private String createLibDir() {
     String location = featureLocation;
     File dir = new File( location, "lib" ); //$NON-NLS-1$
     dir.mkdirs();
     return dir.getAbsolutePath();
   }
   
   private void copyLibraries( final String libDir ) throws IOException {
     IWARProduct warProduct = ( IWARProduct)product;
     IPath[] libraries = warProduct.getLibraries();
     for( int i = 0; i < libraries.length; i++ ) {
       IPath lib 
         = WARProductUtil.getAbsolutLibraryPath( libraries[ i ], warProduct );
       copyLibrary( libDir, lib );
     }
   }
       
   private void copyLibrary( final String libDir, final IPath filePath ) 
     throws IOException 
   {
     String fileName = filePath.segment( filePath.segmentCount() - 1 );
     File template = new File( filePath.toOSString() );
     File destinationFile = new File( libDir + File.separator + fileName );
     CoreUtility.readFile( new FileInputStream( template ), destinationFile );      
   }
 
   private String createWarContent( final IPath pathToContent, 
                                    final String fileName ) 
     throws IOException 
   {
     File destinationFile = new File( featureLocation, fileName );
     File template = new File( pathToContent.toOSString() );
     CoreUtility.readFile( new FileInputStream( template ), destinationFile );
     return destinationFile.getAbsolutePath();
   }
   
   private void handleJREInfo( final String[][] configurations,
                               final Properties properties )
   {
     IJREInfo jreInfo = product.getJREInfo();
     for( int i = 0; i < configurations.length; i++ ) {
       String[] config = configurations[ i ];
       File vm = jreInfo != null ? jreInfo.getJVMLocation( config[ 0 ] ) : null;
       if( vm != null ) {
         if( config[ 0 ].equals( "macosx" )  //$NON-NLS-1$
             && vm.getPath().startsWith( MAC_JAVA_FRAMEWORK ) ) 
         { 
           continue;
         }
         String rootPrefix = IBuildPropertiesConstants.ROOT_PREFIX
                             + config[ 0 ]
                             + "." + config[ 1 ] + //$NON-NLS-1$
                             "." //$NON-NLS-1$
                             + config[ 2 ]; 
         properties.put( rootPrefix + ".folder.jre", "absolute:"  //$NON-NLS-1$ //$NON-NLS-2$
                         + vm.getAbsolutePath() ); 
         String perms = ( String )properties.get( rootPrefix
                                                  + ".permissions.755" );  //$NON-NLS-1$
         if( perms != null ) {
           StringBuffer buffer = new StringBuffer( perms );
           buffer.append( "," ); //$NON-NLS-1$
           buffer.append( "jre/bin/java" ); //$NON-NLS-1$
           properties.put( rootPrefix + ".permissions.755", buffer.toString() ); //$NON-NLS-1$
         }
       }
     }
   }
   
   private void handleExportSource( final Properties properties ) {
     if( fInfo.exportSource && fInfo.exportSourceBundle ) {
       properties.put( IBuildPropertiesConstants.PROPERTY_INDIVIDUAL_SOURCE,
                       "true" ); //$NON-NLS-1$
       List workspacePlugins     
         = Arrays.asList( PluginRegistry.getWorkspaceModels() );
       for( int i = 0; i < fInfo.items.length; i++ ) {
         if( fInfo.items[ i ] instanceof IFeatureModel ) {
           IFeature feature = ( ( IFeatureModel )fInfo.items[ i ] ).getFeature();
           properties.put( "generate.feature@" + feature.getId().trim() //$NON-NLS-1$
                           + ".source", feature.getId() ); //$NON-NLS-1$ //$NON-NLS-2$
         } else {
           BundleDescription bundle = null;
           if( fInfo.items[ i ] instanceof IPluginModelBase ) {
             IPluginModelBase baseItem = ( IPluginModelBase )fInfo.items[ i ];
             bundle = baseItem.getBundleDescription();
           }
           if( bundle == null ) {
             if( fInfo.items[ i ] instanceof BundleDescription )
               bundle = ( BundleDescription )fInfo.items[ i ];
           }
           if( bundle == null ) {
             continue;
           }
           // it doesn't matter if we generate extra properties for platforms we
           // aren't exporting for
           if( workspacePlugins.contains( PluginRegistry.findModel( bundle ) ) )
           {
             properties.put( "generate.plugin@" + bundle.getSymbolicName().trim()  //$NON-NLS-1$
                             + ".source", bundle.getSymbolicName() ); //$NON-NLS-1$ //$NON-NLS-2$
           }
         }
       }
     }
   }
 
   protected boolean publishingP2Metadata() {
     return fInfo.exportMetadata;
   }
 
   private String getRootFileLocations( final boolean hasLaunchers ) {
     // Get the files that go in the root of the eclipse install, excluding the
     // launcher
     StringBuffer buffer = new StringBuffer();
     if( !hasLaunchers ) {
       File homeDir = new File( TargetPlatform.getLocation() );
       if( homeDir.exists() && homeDir.isDirectory() ) {
         File file = new File( homeDir, "startup.jar" ); //$NON-NLS-1$
         if( file.exists() )
           appendAbsolutePath( buffer, file );
         file = new File( homeDir, "libXm.so.2" ); //$NON-NLS-1$
         if( file.exists() ) {
           appendAbsolutePath( buffer, file );
         }
       }
     }
     return buffer.toString();
   }
 
   private void appendAbsolutePath( final StringBuffer buffer, 
                                    final File file ) 
   {
     if( buffer.length() > 0 )
       buffer.append( "," ); //$NON-NLS-1$
     buffer.append( "absolute:file:" ); //$NON-NLS-1$
     buffer.append( file.getAbsolutePath() );
   }
 
   protected HashMap createAntBuildProperties( final String[][] configs ) {
     HashMap properties = super.createAntBuildProperties( configs );
     fAntBuildProperties.put( IXMLConstants.PROPERTY_COLLECTING_FOLDER, root );
     fAntBuildProperties.put( IXMLConstants.PROPERTY_ARCHIVE_PREFIX, root );
     return properties;
   }
 
   protected void setupGenerator( final BuildScriptGenerator generator,
                                  final String featureID,
                                  final String versionId,
                                  final String[][] configs,
                                  final String featureLocation ) 
     throws CoreException
   {
     super.setupGenerator( generator,
                           featureID,
                           versionId,
                           configs,
                           featureLocation );
     generator.setGenerateVersionsList( true );
     if( product != null ) {
       generator.setProduct( product.getModel().getInstallLocation() );
     }
   }
 
   protected void setAdditionalAttributes( final Element plugin,
                                           final BundleDescription bundle )
   {
     String unpack = Boolean.toString( CoreUtility.guessUnpack( bundle ) );
     plugin.setAttribute( "unpack", unpack ); //$NON-NLS-1$
   }
   
 }
