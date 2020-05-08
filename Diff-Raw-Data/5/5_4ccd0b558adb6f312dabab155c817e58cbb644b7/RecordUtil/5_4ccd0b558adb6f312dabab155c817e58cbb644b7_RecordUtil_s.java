 package com.tomclaw.utils;
 
 import com.tomclaw.bingear.BinGear;
 import com.tomclaw.bingear.GroupNotFoundException;
 import com.tomclaw.bingear.IncorrectValueException;
 import java.io.*;
 import javax.microedition.rms.RecordStore;
 import javax.microedition.rms.RecordStoreException;
 
 /**
  * Solkin Igor Viktorovich, TomClaw Software, 2003-2012
  * http://www.tomclaw.com/
  * @author Игорь
  */
 public class RecordUtil {
 
     public static void saveFile(final String fileName, final BinGear dataGear, boolean isBackground, final boolean isINIFormat) {
         Thread thread = new Thread() {
 
             public void run() {
                 try {
                     removeFile(fileName);
                 } catch (RecordStoreException ex) {
                 }
                 try {
                     saveFile(fileName, dataGear, isINIFormat);
                 } catch (IOException ex) {
                 }
             }
         };
         if (isBackground) {
             thread.setPriority(Thread.MIN_PRIORITY);
             thread.start();
         } else {
             thread.run();
         }
     }
 
    public static int saveFile(String fileName, BinGear dataGear, boolean isINIFormat) throws IOException {
         try {
             RecordStore recordStore = RecordStore.openRecordStore(fileName, true);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (isINIFormat) {
                 dataGear.exportToIni(new DataOutputStream(baos));
             } else {
                 dataGear.saveToDat(new DataOutputStream(baos));
             }
             int index = recordStore.addRecord(baos.toByteArray(), 0, baos.size());
             recordStore.closeRecordStore();
             return index;
         } catch (RecordStoreException ex) {
         }
         return -1;
     }
 
     public static void readFile(String fileName, BinGear dataGear) throws IOException,
             IncorrectValueException, GroupNotFoundException {
         dataGear.readFromDat(new DataInputStream(openInputStream(fileName)));
     }
 
     public static InputStream openInputStream(String fileName) {
         return openInputStream(fileName, 1);
     }
 
     public static InputStream openInputStream(String fileName, int index) {
         InputStream inputStream;
         try {
             RecordStore recordStore = RecordStore.openRecordStore(fileName, true);
             if (recordStore.getNumRecords() >= index) {
                 inputStream = new ByteArrayInputStream(recordStore.getRecord(index));
                 recordStore.closeRecordStore();
                 return inputStream;
             }
         } catch (RecordStoreException ex) {
         }
         return new ByteArrayInputStream(new byte[0]);
     }
 
     public static int getRecordsCount(String fileName) {
         try {
             RecordStore recordStore = RecordStore.openRecordStore(fileName, true);
             int numRecords = recordStore.getNumRecords();
             recordStore.closeRecordStore();
             return numRecords;
         } catch (RecordStoreException ex) {
         }
         return -1;
     }
 
     public static int getIndexSize(String fileName, int index) {
         try {
             RecordStore recordStore = RecordStore.openRecordStore(fileName, true);
             int recordSize = recordStore.getRecordSize(index);
             recordStore.closeRecordStore();
             return recordSize;
         } catch (RecordStoreException ex) {
         }
         return -1;
     }
 
     public static void removeFile(String fileName) throws RecordStoreException {
         RecordStore.openRecordStore(fileName, true).closeRecordStore();
         RecordStore.deleteRecordStore(fileName);
     }
 }
