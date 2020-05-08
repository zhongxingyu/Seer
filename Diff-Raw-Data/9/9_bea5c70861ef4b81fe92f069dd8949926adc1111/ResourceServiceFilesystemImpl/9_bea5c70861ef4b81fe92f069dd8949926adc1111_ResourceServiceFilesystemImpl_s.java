 package net.crfsol.know.resource.filesystem;
 
 import net.crfsol.know.core.domain.Resource;
 import net.crfsol.know.resource.ResourceListener;
 import net.crfsol.know.resource.ResourceService;
 import net.crfsol.know.resource.filesystem.resolver.ResourceResolverFactory;
 
 import javax.inject.Inject;
 import java.io.File;
 import java.util.Date;
 
 
 public class ResourceServiceFilesystemImpl implements ResourceService {
 
     @Inject
     private ResourceResolverFactory resolverFactory;
 
 
     @Override
     public void findResourcesAtLocation(String location, Date sinceDate, ResourceListener listener) {
         findResourcesAtLocation(new File(location), sinceDate, listener);
         listener.scanComplete(location);
     }
 
     private void findResourcesAtLocation(File root, Date sinceDate, ResourceListener listener) {
         if (root.exists()) {
             if (root.isFile()) {
                 resolveResource(root, sinceDate, listener);
 
             } else {
                 for (File child : root.listFiles()) {
                     if (child.isDirectory()) {
                         findResourcesAtLocation(child, sinceDate, listener);
                     } else {
                         resolveResource(child, sinceDate, listener);
                     }
                 }
             }
         }
     }
 
     private void resolveResource(File file, Date sinceDate, ResourceListener listener) {
        if (sinceDate == null || file.lastModified() > sinceDate.getTime()) {
             Resource r = resolverFactory.getResourceResolver(file).resolveResource(file);
             if (r != null) {
                 listener.resourceFound(r);
             }
         }
     }
 }
