 /******************************************************************************* 
 * Copyright (c) 2010, 2011 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Holger Staudacher - initial API and implementation
 *******************************************************************************/ 
 package org.eclipse.libra.warproducts.core.test.tests;
 
 import java.io.*;
 import java.util.*;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.libra.warproducts.core.*;
 import org.eclipse.osgi.service.resolver.BundleDescription;
 import org.eclipse.osgi.service.resolver.State;
 import org.eclipse.pde.internal.core.TargetPlatformHelper;
 import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
 import org.eclipse.pde.internal.core.iproduct.*;
 import org.eclipse.pde.internal.core.iproduct.IProduct;
 import org.osgi.framework.Version;
 
 
 public class WARProductExportOperationTest extends TestCase {
   
   private static final String WAR_FILE = "export.war";
   private static final String WAR_FILE_PATH 
     = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() 
       + File.separator + WAR_FILE;
 
   protected void setUp() throws Exception {
   }
   
   private IFolder createTempDir() throws CoreException {
     IWorkspace workspace = ResourcesPlugin.getWorkspace();
     IWorkspaceRoot root = workspace.getRoot();
     IProject project = root.getProject( "warProject" );
     if( !project.exists() ) {
       project.create( null );
       project.open( null );
     }
     IFolder tempFolder = project.getFolder( "warFolder" );
     if( !tempFolder.exists() ) {
       tempFolder.create( true, true, null );
     }
     return tempFolder;
   }
   
   protected void tearDown() throws Exception {
     deleteWarFile();
   }
   
   private void deleteWarFile() {
     File war = new File( WAR_FILE_PATH );
     if( war.exists() ) {
       war.delete();
     }
   }
   
   public void testWARFileWithLinkedResources() throws Exception {
     IFolder folder = getLinkedFolder();
     File war = runBlockingWARExportJob( folder );
     testWARContents( war );
   }
 
   private IFolder getLinkedFolder() throws CoreException, IOException {
     IWorkspace workspace = ResourcesPlugin.getWorkspace();
     IWorkspaceRoot root = workspace.getRoot();
     IProject project = root.getProject( "testLinkedResources" );
     project.create( null );
     project.open( null );
     IFolder folder = project.getFolder( project.getFullPath() );
     File tempFolder = File.createTempFile( "tempFolder", null );
     tempFolder.delete();
     tempFolder.mkdir();
     folder.createLink( new Path( tempFolder.getAbsolutePath() ), 
                        IResource.REPLACE, 
                        null );
     return folder;
   }
   
   public void testWARFileContents() throws Exception {
     IFolder folder = createTempDir();
     File war = runBlockingWARExportJob( folder );
     assertTrue( war.exists() );
     testWARContents( war );
   }
 
   private void testWARContents( final File war ) throws Exception
   {
     List warEntryList = extractWarEntriesAsString( war );
     testWARFileRootIsWebInf( warEntryList );
     testWARFileContainsWebXML( warEntryList );
     testWARFileContainsLibFolder( warEntryList );
     testWebInfFolderContainsLaunchIni( warEntryList );
 //    testWebInfFolderContainsPlugins( warEntryList );
     testDidntContainsDotEclipseProduct( warEntryList );
     testLibContainsJar( warEntryList );
   }
   
   private void testWARFileRootIsWebInf( final List warEntryList ) 
     throws Exception 
   {
     assertTrue( warEntryList.contains( "WEB-INF/" ) );
   }
   
   private void testWARFileContainsWebXML( final List warEntryList ) 
     throws Exception 
   {
     assertTrue( warEntryList.contains( "WEB-INF/web.xml" ) );
   }
   
   private  void testWARFileContainsLibFolder( final List warEntryList) 
     throws Exception 
   {
     assertTrue( warEntryList.contains( "WEB-INF/lib/" ) );
   }
   
   private void testWebInfFolderContainsLaunchIni( final List warEntryList ) 
     throws Exception 
   {
     assertTrue( warEntryList.contains( "WEB-INF/launch.ini" ) );
   }
   
  private void testWebInfFolderContainsPlugins( final List warEntryList ) 
     throws Exception 
   {
     assertTrue( warEntryList.contains( "WEB-INF/plugins/" ) );
   }
   
   private void testDidntContainsDotEclipseProduct( final List warEntryList ) 
     throws Exception 
   {
     assertTrue( !warEntryList.contains( "WEB-INF/.eclipseproduct" ) );
   }
   
   private void testLibContainsJar( final List warEntryList ) 
   throws Exception 
 {
   String path = "WEB-INF/lib/test.jar";
   assertTrue( warEntryList.contains( path ) );
 }
 
   private List extractWarEntriesAsString( File war )
     throws ZipException, IOException
   {
     ZipFile zip = new ZipFile( war );
     List warEntryList = new ArrayList();
     Enumeration entries = zip.entries();
     while( entries.hasMoreElements() ) {
       Object nextElement = entries.nextElement();
       warEntryList.add( nextElement.toString() );
     }
     return warEntryList;
   }
 
   private File runBlockingWARExportJob( final IFolder folder ) throws Exception
   {
     WARProductExportOperation job = createWarExportOperation( folder );
     job.setUser( true );
     job.setRule( ResourcesPlugin.getWorkspace().getRoot() );
     job.schedule();
     job.join();
     return new File( WAR_FILE_PATH );
   }
 
   private WARProductExportOperation createWarExportOperation( 
     final IFolder folder )
     throws Exception
   {
     InfrastructureCreator creator = new InfrastructureCreator( folder );
     WARProductModel model = new WARProductModel();
     model.load( getTestWarProduct(), false );
     IWARProduct product = ( IWARProduct )model.getProduct();
     creator.createWebInf();
     creator.createLaunchIni();
     creator.createWebXml();
     product.removeLibrary( new Path( "/test.rap/lib.jar" ) );
     IFile file = folder.getFile( "test.jar" );
     if( !file.exists() ) {
       File jar = File.createTempFile( "test", ".jar" );
       FileInputStream stream = new FileInputStream( jar );
       file.create( stream, true, null );
     }
     product.addLibrary( file.getFullPath(), false );
     product.addLaunchIni( creator.getLaunchIniPath() );
     product.setIncludeLaunchers( false );
     IProductModelFactory factory = model.getFactory();
     IConfigurationFileInfo configInfo = factory.createConfigFileInfo();
     product.setConfigurationFileInfo( configInfo );
     configInfo.setUse(Platform.OS_MACOSX, "default" ); //$NON-NLS-1$
     configInfo.setPath( Platform.OS_MACOSX, null );
     product.addWebXml( creator.getWebXmlPath() );
     FeatureExportInfo info = configureFeatureExport( product );
     String rootDirectory = "WEB-INF";
     WARProductExportOperation job 
       = new WARProductExportOperation( info,
                                        "a job",
                                        product,
                                        rootDirectory );
     return job;
   }
 
   private FeatureExportInfo configureFeatureExport( final IWARProduct product ) 
   {
     FeatureExportInfo info = new FeatureExportInfo();
     info.toDirectory = false;
     info.exportSource = false;
     info.exportSourceBundle = false;
     info.allowBinaryCycles = false;
     info.exportMetadata = false;
     info.destinationDirectory 
       = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
     info.zipFileName = WAR_FILE;
     BundleDescription[] pluginModels = getPluginModels( product );
     info.items = pluginModels;
     return info;
   }
 
   private InputStream getTestWarProduct() {
     return getClass().getResourceAsStream( "/test.warproduct" );
   }
 
   private BundleDescription[] getPluginModels( final IProduct product ) {
     ArrayList list = new ArrayList();
     State state = TargetPlatformHelper.getState();
     IProductPlugin[] plugins = product.getPlugins();
     for( int i = 0; i < plugins.length; i++ ) {
       BundleDescription bundle = null;
       String v = plugins[ i ].getVersion();
       if( v != null && v.length() > 0 ) {
         bundle = state.getBundle( plugins[ i ].getId(),
                                   Version.parseVersion( v ) );
       }
       // if there's no version, just grab a bundle like before
       if( bundle == null ) {
         bundle = state.getBundle( plugins[ i ].getId(), null );
       }
       if( bundle != null ) {
         list.add( bundle );
       }
     }
     Object[] bundleArray = list.toArray( new BundleDescription[ list.size() ] );
     return ( BundleDescription[] )bundleArray;
   }
 }
