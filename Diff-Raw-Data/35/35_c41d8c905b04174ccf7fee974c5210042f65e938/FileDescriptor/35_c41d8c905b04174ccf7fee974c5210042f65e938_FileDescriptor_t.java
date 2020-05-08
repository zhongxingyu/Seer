 package datatypes;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 public class FileDescriptor extends File
 {
     public static final String backupDir = "./Backup";
     public static final String receivedChunkDir = "./RecvChunk";
     public static final String sentChunkDir = backupDir + "/SentChunk";
     public static final String configDir = "./config";
     public static final int chunkSize = 64000; // 64KB
     private String SHA256filenameHash;
 
     /**
      * @param filename
      * @throws NoSuchAlgorithmException
      */
     public FileDescriptor(String filename) throws NoSuchAlgorithmException
     {
         super(filename);
         SHA256filenameHash = filename2Hash(filename);
     }
 
     /**
      * @return the SHA256 hash of the filename
      */
     public String getSHA256filenameHash()
     {
         return SHA256filenameHash;
     }
 
     /**
      * Divides a file into Chunks, with size fixed in the constant chunkSize.
      *
      * @throws FileNotFoundException
      * @throws IOException
      */
     public void breakToChunks() throws FileNotFoundException, IOException
     {
         BufferedInputStream bis = new BufferedInputStream(new FileInputStream(this));
         FileOutputStream out;
 
         String hash = this.getSHA256filenameHash();
 
         // If it doesn't exist, create a dir to place chunks
         File newDir = new File(sentChunkDir + "/" + hash);
         if (!newDir.exists())
         {
             newDir.mkdir();
         }
 
         // Start chunking
         int partCounter = 1;
         byte[] buffer = new byte[chunkSize];
         int tmp = 0;
         while ((tmp = bis.read(buffer)) > 0)
         {
             // Filenames will be placed with Hash.ChunkNo format, with string sizes of 256.6
             File newFile = new File(sentChunkDir + "/" + hash + "/" + hash + "_" + String.format("%06d", partCounter++));
             newFile.createNewFile();
             out = new FileOutputStream(newFile);
             out.write(buffer, 0, tmp);
             out.close();
         }
 
         // If last chunk has chunkSize bytes, we need to create a final chunk with 0 byte size
         if (this.length() % chunkSize == 0)
         {
             File newFile = new File("./ChunkFolder/" + hash + "/" + hash + "_" + String.format("%06d", partCounter++));
             newFile.createNewFile();
         }
     }
 
     /**
      * Hashes a filename using SHA-256 algorithm.
      *
      * @param filename String to hash.
      * @return Hash (in String format)
      */
     public static String filename2Hash(String filename) throws NoSuchAlgorithmException
     {
         MessageDigest md = MessageDigest.getInstance("SHA-256");
         md.update(filename.getBytes());
         byte[] digest = md.digest();
 
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < digest.length; i++)
         {
             sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
         }
 
         return sb.toString();
     }
 
     public static String readFile(String path) throws IOException
     {
         FileInputStream stream = new FileInputStream(new File(path));
         try
         {
             FileChannel fc = stream.getChannel();
             MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            
             /* Instead of using default, pass in a decoder. */
             return Charset.defaultCharset().decode(bb).toString();

         }
         finally
         {
             stream.close();
         }
     }
 }
