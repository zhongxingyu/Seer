 package org.mosaic.runner.deploy.watcher;
 
 import java.io.IOException;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author arik
  */
 public class JarsWatchedResourceProvider implements WatchedResourceProvider {
 
     private final Logger logger = LoggerFactory.getLogger( getClass() );
 
     private final BundleContext bundleContext;
 
     private final Path directory;
 
     private final Map<Path, WatchedResource> watchedJars = new HashMap<>();
 
    private final WatchedResourceHandler jarFileResourceHandler = new ManagedJarResourceHandler();
 
     public JarsWatchedResourceProvider( BundleContext bundleContext, Path directory ) {
         this.bundleContext = bundleContext;
         this.directory = directory;
     }
 
     @Override
     public Collection<WatchedResource> getWatchedResources() {
         try {
 
             //
             // check if any new files have been added to our watched directory; note that we don't remove from our map
             // any resources might not exist anymore - that will be done automatically when the 'handleNoLongerExists'
             // method is called.
             //
             Files.walkFileTree( this.directory, new SimpleFileVisitor<Path>() {
                 @Override
                 public FileVisitResult visitFile( Path file, BasicFileAttributes attrs ) throws IOException {
                     file = file.normalize().toAbsolutePath();
                     if( !watchedJars.containsKey( file ) && file.getFileName().toString().toLowerCase().endsWith( ".jar" ) ) {
                         //
                         // new, un-tracked JAR file - add it to our list of watched files
                         //
                         watchedJars.put( file, new WatchedResource( file, jarFileResourceHandler ) );
                     }
                     return FileVisitResult.CONTINUE;
                 }
             } );
 
             //
             // return our list of watched resources
             //
             return this.watchedJars.values();
 
         } catch( IOException e ) {
             logger.error( "Could not scan directory '{}' for JAR files: {}",
                           new Object[] { this.directory, e.getMessage(), e } );
             return Collections.emptyList();
         }
     }
 
     private class ManagedJarResourceHandler extends JarWatchedResourceHandler {
 
         private ManagedJarResourceHandler() {
             super( JarsWatchedResourceProvider.this.bundleContext );
         }
 
         @Override
         public void handleNoLongerExists( Path resource ) throws BundleException {
             watchedJars.remove( resource );
             super.handleNoLongerExists( resource );
         }
     }
 }
