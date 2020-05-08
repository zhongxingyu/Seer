 package jewas.http.impl;
 
 import jewas.http.FileResponse;
 import jewas.http.HttpRequest;
 import jewas.http.HttpStatus;
 import jewas.http.RequestHandler;
 import jewas.resources.Resource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import java.io.*;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.StandardCopyOption;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: driccio
  * Date: 19/07/11
  * Time: 16:50
  *
  * ResourceRequestHandler is a {@link RequestHandler} to use to load static resources.
  *
  */
 public class ResourceRequestHandler extends AbstractRequestHandler {
 
     /**
      * Class logger.
      */
     private Logger logger = LoggerFactory.getLogger(ResourceRequestHandler.class);
 
     /**
      * The resource to load.
      */
     Resource resource;
 
     /**
      *  Directory that will be used to extract files from jar to the filesystem the first
      *  time it is accessed
      */
     File cachedResourcesFileSystemRootDir;
 
     public ResourceRequestHandler(File cachedResourcesFileSystemRootDir, Resource resource) {
         this.resource = resource;
         this.cachedResourcesFileSystemRootDir = cachedResourcesFileSystemRootDir;
     }
 
     /**
      * Loads the static file from the path into the given request.
      * @param request the {@link HttpRequest}
      */
     @Override
     public void onRequest(HttpRequest request) {
         Path extractedFileInCache = Paths.get(cachedResourcesFileSystemRootDir.getAbsolutePath(), resource.path());
 
         try {
             if (Files.notExists(extractedFileInCache)) {
                 Path parentDirectoryInCache = extractedFileInCache.getParent();
                 if(Files.notExists(parentDirectoryInCache)){
                    Files.createDirectory(parentDirectoryInCache);
                 }
 
                 // Let's extract resource and copy it in cached folder
                 try(InputStream resourceStream = resource.in();
                     OutputStream fileSystemStream = Files.newOutputStream(extractedFileInCache)){
                     Files.copy(resourceStream, extractedFileInCache, StandardCopyOption.REPLACE_EXISTING);
                 }
             }
             FileResponse fileResponse = request.respondFile();
             if(headers != null){
                 for(Map.Entry<String, String> headerEntry : headers.entrySet()){
                     fileResponse.addHeader(headerEntry.getKey(), headerEntry.getValue());
                 }
             }
             fileResponse.file(extractedFileInCache);
         } catch (IOException e) {
             logger.error("Error opening :" + extractedFileInCache, e);
             request.respondError(HttpStatus.NOT_FOUND);
         }
     }
 }
