 public class Chunk
 {
     // First byte is sequence number last 9 bytes are the checksum/CRC
    // the rest is the actual data
     private byte[] data;
 
     public Chunk(byte[] bytes, int num)
     {
        //creates the data byte array with sequence number and checksum
     }
 
     public byte[] getData()
     {
         return data;
     }
 
     public boolean checkCRC()
     {
         //check crc
     }
 
     public int getSequenceNumber()
     {
 
     }
 }
