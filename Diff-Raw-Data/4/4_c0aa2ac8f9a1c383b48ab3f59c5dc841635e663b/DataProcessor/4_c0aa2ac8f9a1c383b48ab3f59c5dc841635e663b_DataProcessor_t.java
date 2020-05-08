 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mnorrman.datastorageproject.storage;
 
 import com.mnorrman.datastorageproject.objects.IndexedDataObject;
 import com.mnorrman.datastorageproject.objects.UnindexedDataObject;
 import com.mnorrman.datastorageproject.tools.MetaDataComposer;
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileLock;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.CRC32;
 
 /**
  *
  * @author Mikael
  */
 public class DataProcessor {
     
     public static final int BlOCK_SIZE = 131072;
     
     private FileChannel dataChannel;
     
     
     /**
      * Constructor. Requires a new FileChannel-object from the backstorage.
      * @param channel FileChannel from the backstorage.
      */
     public DataProcessor(FileChannel channel){
         this.dataChannel = channel;
     }
     
     public int retrieveData(ByteBuffer buffer, long position, IndexedDataObject ido){
         try{
             if(buffer.position() != 0)
                 buffer.clear();
             
             dataChannel.position(((ido.getOffset() + 512L) + position));
             
             if(ido.getLength() - position < buffer.capacity() ){
                 buffer.limit((int)(ido.getLength() - position));
             }
             
             return dataChannel.read(buffer);
         }catch(IOException e){
             Logger.getLogger("b-log").log(Level.SEVERE, "An error occured when retrieving data!", e);
         }
         return -1;
     }
     
     /**
      * A method for retrieving data from our backStorage. It does not return any
      * data, it simply returns a boolean value telling if the operation was 
      * successful or not. 
      * @param os An outputstream to which the data will be written.
      * @param ido The indexedDataObject that contains the metadata for the data.
      * @return True if everything went as expected, otherwise false.
      */
     public boolean retrieveData(OutputStream os, IndexedDataObject ido){
         try{
             ByteBuffer buffer = ByteBuffer.allocate(BlOCK_SIZE);
             dataChannel.position(ido.getOffset() + 512);
             
             int readBytes = 0;
             long totalBytes = ido.getLength();
 
             while(totalBytes > 0){
                 buffer.clear();
                 readBytes = dataChannel.read(buffer);
                 buffer.flip();
                 
                 if(readBytes >= totalBytes){
                     os.write(buffer.array(), 0, (int)(totalBytes));
                 }else{
                     os.write(buffer.array(), 0, readBytes);
                 }
                 
                 totalBytes -= readBytes;
                 if(totalBytes <= 0)
                     break;
             }
             os.flush();
             return true;
         }catch(IOException e){
             Logger.getLogger("b-log").log(Level.SEVERE, "An error occured when retrieving data!", e);
         }
         return false;
     }
 
     public IndexedDataObject storeData(UnindexedDataObject udo){
         
 //        CRC32 crc = new CRC32();
         //String fileName = udo.getColname() + "-" + udo.getRowname() + Math.random();
         
 //        int readBytes = 0;
 //        long totalBytesRead = 0;
 //        byte[] bytes = null;
 //        
 //        try {
 //            FileOutputStream fos = new FileOutputStream(udo.getTempFile());
 //            while(totalBytesRead < udo.getLength()){
 //                bytes = new byte[BlOCK_SIZE];
 //                readBytes = udo.getStream().read(bytes);
 //                crc.update(bytes, 0, readBytes);
 //                fos.write(bytes, 0, readBytes);
 //                totalBytesRead += readBytes;
 //                if(totalBytesRead >= udo.getLength())
 //                    break;
 //            }
 //            fos.flush();
 //            fos.close();
 //        }catch(IOException e){
 //            Logger.getLogger("b-log").log(Level.SEVERE, "An error occured when creating temporary data!", e);
 //        }
 
 //        udo.setChecksum(crc.getValue());
         
         long filesizeBeforeOperation = -1; 
         
         
         
         try{
             ByteBuffer bbb = MetaDataComposer.decompose(udo);
            bbb.position(256);
             long newVersion = bbb.getLong();
             bbb.position(0);
             
            
             FileLock fl = dataChannel.lock();
 
             long newOffset = dataChannel.size();
                         
             filesizeBeforeOperation = newOffset;
             
             dataChannel.position(newOffset);
             dataChannel.write(bbb);
             
             //This part makes sure that the full amount of bytes are pre-
             //allocated, thus making it easier to rollback the changes.
             //(Since we still know how much data to remove)
             long tempPos = dataChannel.position();
             ByteBuffer voidbuf = ByteBuffer.allocate(1);
             voidbuf.put((byte)0);
             voidbuf.flip();
             if(tempPos+(udo.getLength()-1) < 0)
                 dataChannel.position(0);
             else
                 dataChannel.position(tempPos+(udo.getLength()-1));
             dataChannel.write(voidbuf);
             dataChannel.position(tempPos);
             
             FileInputStream fis = new FileInputStream(udo.getTempFile());
             FileChannel fc = fis.getChannel();
             
             //Transfer all data from the temporary file into the backstorage.
             dataChannel.transferFrom(fc, dataChannel.position(), udo.getTempFile().length());
             fc.close();
             
             //Remove the temporary file.
             udo.removeTempFile();
             
             fl.release();
             return new IndexedDataObject(udo, newOffset, newVersion);
             
         }catch(IOException e){
             Logger.getLogger("b-log").log(Level.SEVERE, "An error occured when storing data!", e);
             try{
                 dataChannel.truncate(filesizeBeforeOperation);
             }catch(IOException e2){
                 Logger.getLogger("b-log").log(Level.SEVERE, "An error occured when rolling back changes!", e);
             }
         }
         return null;
     }
     
     public boolean removeData(IndexedDataObject ido){
         long amount = ido.getLength() + 512L;
         
         try{
             dataChannel.position(ido.getOffset() + amount);
             dataChannel.transferFrom(dataChannel, ido.getOffset(), dataChannel.size()-(ido.getOffset()+amount));
             dataChannel.truncate(dataChannel.size()-amount);
             return true;
         }catch(IOException e){
             Logger.getLogger("b-log").log(Level.SEVERE, "Error when removing data", e);
         }
         
         return false;
     }
     
     /**
      * Removes several indexedDataObjects. Designed to try removal of all items
      * in the list, regardless if anyone fails.
      * Practical to use when removing a table cell with multiple versions.
      * @param idos List of IndexedDataObjects
      * @return true if all IDO's were removed successfully. False if any of them
      * failed to be removed.
      */
     public boolean removeData(List<IndexedDataObject> idos){
         boolean value = true;
         for(IndexedDataObject ido : idos){
             if(value)
                 value = removeData(ido);
         }
         return value;
     }
 }
