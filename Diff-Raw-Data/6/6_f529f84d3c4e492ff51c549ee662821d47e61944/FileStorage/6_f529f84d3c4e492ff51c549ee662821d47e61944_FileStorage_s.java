 package otanga.Controllers;
 
 /**
  * Created with IntelliJ IDEA.
  * User: cedric
  * Date: 5/10/13
  * Time: 12:59 PM
  * To change this template use File | Settings | File Templates.
  */
 import com.google.appengine.api.files.*;
 import com.google.appengine.api.files.GSFileOptions.GSFileOptionsBuilder;
 import sun.security.ssl.Debug;
 
 import java.nio.ByteBuffer;
 import java.io.IOException;
 import java.nio.channels.Channels;
 import java.util.UUID;
 import java.io.BufferedReader;
 
 public class FileStorage {
     // Get the file service
     private final static FileService fileService = FileServiceFactory.getFileService();
 
     private final static String _bucketName = "OtangaImages";
 
     private FileStorage() {}
 
     public static String storeImage(byte[] fileContent, String contentType, java.io.PrintWriter responseWriter){
 
         String key = UUID.randomUUID().toString().replace("-", "").toLowerCase() + "$" + contentType.replace('/','!');
 
         responseWriter.println();
         responseWriter.println("\t[FileStorage.storeImage] Key: " + key);
         responseWriter.println("\t[FileStorage.storeImage] ContentType: " + contentType);
         responseWriter.println("\t[FileStorage.storeImage] Length: " + fileContent.length);
 
         GSFileOptionsBuilder optionsBuilder = new GSFileOptionsBuilder()
             .setBucket(_bucketName)
             .setKey(key)
             .setMimeType(contentType)
             .setAcl("private");
 
         try {
             AppEngineFile writableFile = fileService.createNewGSFile(optionsBuilder.build());
 
             FileWriteChannel writeChannel = fileService.openWriteChannel(writableFile, true);
 
             ByteBuffer byteBuffer = ByteBuffer.allocate(fileContent.length);
             byteBuffer.put(fileContent);
             byteBuffer.rewind();
 
             int writeResult = writeChannel.write(byteBuffer, "UTF-8");
 
             responseWriter.println("\t[FileStorage.storeImage] WriteResult: " + writeResult);
             responseWriter.println();
 
             writeChannel.closeFinally();
 
         } catch (IOException e) {
             key = null;
             responseWriter.println("\t[FileStorage.storeImage] IOException: " + e.getMessage());
             e.printStackTrace();
         }
 
         return key;
     }
 
     public static String retrieveImage(String imageKey, java.io.PrintWriter responseWriter)
     {
         if (imageKey == null || imageKey.length() == 0)
             throw new IllegalArgumentException("imageKey");
 
         String output = null;
 
         String filename = "/gs/" +  _bucketName + "/" +  imageKey;
 
         String contentType = imageKey.substring(imageKey.indexOf('$') + 1).replace('!','/');
 
         AppEngineFile readableFile = new AppEngineFile(filename);
         try {
             FileStat stat = fileService.stat(readableFile);
             output = stat.getLength().toString() + " bytes";
 
             responseWriter.println();
             responseWriter.println("\t[FileStorage.retrieveImage] FileName: " + stat.getFilename());
             responseWriter.println("\t[FileStorage.retrieveImage] ContentType: " + contentType);
             responseWriter.println("\t[FileStorage.retrieveImage] Content: ");
             responseWriter.println();
             responseWriter.print("\t");
 
             FileReadChannel readChannel = fileService.openReadChannel(readableFile, false);
 
             ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
             int count;
             while ((count = readChannel.read(buffer)) > 0)
             {
                 buffer.rewind();
 
                 if (contentType.toLowerCase().startsWith("text/"))
                 {
                     byte[] byteArray = new byte[count];
                     buffer.get(byteArray, 0, count);
                     responseWriter.print(new String(byteArray));
                 }
                 else
                 {
                     for (int i = 0; i < count; i++)
                     {
                        Byte b = buffer.get();
                         responseWriter.print(Integer.toHexString(buffer.get() & 0xff) + " ");
                     }
                 }
 
                //buffer.rewind();

                 buffer.clear();
             }
             responseWriter.println();
             responseWriter.println();
 
             readChannel.close();
 
         } catch (IOException e) {
             e.printStackTrace();
             output = e.getMessage();
         }
 
         return output;
     }
 }
