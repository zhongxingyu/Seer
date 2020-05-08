 package ru.rkfg.tests.diststorage;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 public class PlainContentWriter implements RaindropContentWriter {
 
     private FileOutputStream stream;
     private long fileLength;
     private int len = RainDrop.RAINSIZE;
     private int dataIndex = 0;
 
     public PlainContentWriter(FileOutputStream stream, long fileLength) {
         this.stream = stream;
         this.fileLength = fileLength;
     }
 
     @Override
     public boolean writeBlock(byte[] block, int dataIndex) throws IOException, ExtractionError {
         if (this.dataIndex++ != dataIndex) {
             throw new ExtractionError("Inconsistent dataIndex in plain file writer, expected: " + (this.dataIndex - 1) + ", got: "
                     + dataIndex);
         }
         if (fileLength > 0) {
            if (fileLength <= block.length) {
                 len = (int) fileLength;
             }
             stream.write(block, 0, len);
             fileLength -= len;
         }
         return false;
     }
 
     @Override
     public void closeStream() throws IOException {
         stream.close();
     }
 
 }
