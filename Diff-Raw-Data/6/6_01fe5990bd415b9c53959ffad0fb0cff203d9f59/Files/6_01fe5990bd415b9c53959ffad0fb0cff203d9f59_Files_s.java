 package jewas.util.file;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 /**
  * Created by IntelliJ IDEA.
  * User: driccio
  * Date: 20/07/11
  * Time: 10:58
  * To change this template use File | Settings | File Templates.
  */
 public class Files {
     /**
      * Get a file from a given path.
      * @param path the path
      * @return the inputstream at the given path
      * @throws IOException a {@link IOException}
      */
     public static InputStream getInputStreamFromPath(String path) throws IOException {
         if (path == null) {
             throw new FileNotFoundException("The given path is null.");
         }
 
         URL resource = Files.class.getClassLoader().getResource(path);
 
         if (resource == null) {
             throw new FileNotFoundException("The path: " + path + " was not found in the classpath.");
         }
 
         return resource.openStream();
     }
 
     /**
      * Get the bytes array that correspond to the given file.
      * @param stream a stream
      * @return the bytes array that correspond to the given file
      * @throws IOException an {@link IOException}
      */
     public static byte[] getBytesFromStream(InputStream stream) throws IOException {
         if (stream == null) {
             throw new IOException("Stream is null");
         }
 
         // Create the byte array to hold the data
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         byte[] chunks = new byte[1024];
 
        while(stream.read(chunks) == 1024){
             baos.write(chunks);
         }
 
        baos.write(chunks);
 
         // Close the input stream and return bytes
         stream.close();
 
         return baos.toByteArray();
     }
 
     /**
      * Get the string that correspond to the content of the given file.
      * @param stream the {@link InputStream}
      * @return the string that correspond to the content of the given file.
      */
     public static String getStringFromStream(InputStream stream) throws IOException {
         return new String(getBytesFromStream(stream));
     }
 }
