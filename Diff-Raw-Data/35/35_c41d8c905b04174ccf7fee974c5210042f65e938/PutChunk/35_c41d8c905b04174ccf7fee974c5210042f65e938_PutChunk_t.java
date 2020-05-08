 package messages;
 
 import datatypes.FileDescriptor;
 import exceptions.InvalidMessageArguments;
 import gui.FileChooserFrame;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.NoSuchAlgorithmException;
 
 public class PutChunk
 {
     // Header and Data
     private String chunkData;
     private String protocolVersion;
     private String fileId;
     private int replicationDegree;
     private String chunkNo;
 
     public PutChunk(String pathToChunk, int replicationDegree, String protocolVersion) throws NoSuchAlgorithmException, FileNotFoundException, IOException
     {
         // Getting the fileId and chunkNo through the filename
         FileDescriptor chunk = new FileDescriptor(pathToChunk);
         FileChooserFrame.log.append("Chunk Name: " + chunk.getName() + "\n");
 
         String[] chunkFileNameSplitted = (chunk.getName()).split("_");
 
         fileId = chunkFileNameSplitted[0];
         chunkNo = chunkFileNameSplitted[1];
 
         // Getting the contents of the chunk to include in the message
         chunkData = FileDescriptor.readFile(pathToChunk);
        
         // Setting the protocol version and replication degree used
         this.protocolVersion = protocolVersion;
         this.replicationDegree = Math.min(replicationDegree, 9); // maximum replication degree is 9
     }
 
     public PutChunk(String pathToChunk, int replicationDegree) throws NoSuchAlgorithmException, FileNotFoundException, IOException
     {
         // Assumes only version 1.0 of protocol
         this(pathToChunk, replicationDegree, "1.0");
     }
 
     public PutChunk(String pathToChunk) throws NoSuchAlgorithmException, FileNotFoundException, IOException
     {
         // Assumes replication degree of 1, with only version 1.0 of protocol
         this(pathToChunk, 1, "1.0");
     }
 
     public PutChunk(String fileId, int replicationDegree, String protocolVersion, String chunkNo, String chunkData)
     {
         this.chunkData = chunkData.toString();
         this.protocolVersion = protocolVersion;
         this.fileId = fileId;
         this.replicationDegree = replicationDegree;
         this.chunkNo = chunkNo;
     }
 
     @Override
     public String toString()
     {
         return "PUTCHUNK " + protocolVersion + " " + fileId + " "
                + chunkNo + " " + replicationDegree + " "
               + "\r\n" + "\r\n" + chunkData.toString();
     }
 
     public static PutChunk parseMsg(String msg) throws InvalidMessageArguments, UnsupportedEncodingException
     {
         String[] splittedMsg = msg.trim().split(" ");
 
         if (splittedMsg.length < 5)
         {
             throw new InvalidMessageArguments();
         }
 
         String protocolVersion = splittedMsg[1];
         String fileID = splittedMsg[2];
         String chunkNo = splittedMsg[3];
         int replicationDegree = Integer.decode((String) splittedMsg[4]);
 
         if (Float.parseFloat(protocolVersion) < 1.0 || fileID.length() != 64 || Integer.parseInt(chunkNo) < 0 || Integer.parseInt(chunkNo) > 999999)
         {
             throw new InvalidMessageArguments();
         }
 
         StringBuilder temp = new StringBuilder("");
         String chunkData = null;
 
         // Cycle to ignore unknown header stuff
         for (int i = 5; i < splittedMsg.length; i++)
         {
             if (splittedMsg[i].charAt(0) == '\r'
                 && splittedMsg[i].charAt(1) == '\n'
                 && splittedMsg[i].charAt(2) == '\r'
                 && splittedMsg[i].charAt(3) == '\n')
             {
                 for (int k = 4; k < splittedMsg[i].length(); k++)
                 {
                     temp.append(splittedMsg[i].charAt(k));
                 }
 
               
                 i++;
                 for (; i < splittedMsg.length; i++)
                 {
                     {
                         temp.append(" ");
                         temp.append(splittedMsg[i]);
                     }
                 }
 
               
             }
 
             chunkData = temp.toString();
         }
 
         // Header is invalid - throw an exception!
         if (chunkData == null)
         {
             throw new InvalidMessageArguments();
         }
 
         return new PutChunk(fileID, replicationDegree, protocolVersion, chunkNo, chunkData);
     }
 
     public byte[] getChunkData() throws UnsupportedEncodingException
     {
         return chunkData.getBytes("UTF-8");
     }
 
     public String getChunkNo()
     {
         return chunkNo;
     }
 
     public String getProtocolVersion()
     {
         return protocolVersion;
     }
 
     public String getFileId()
     {
         return fileId;
     }
 
     public int getReplicationDegree()
     {
         return replicationDegree;
     }
 }
