 package org.srdbs.core;
 
 import org.apache.log4j.Logger;
 import org.srdbs.scheduler.RunBackupJob;
 import org.srdbs.scheduler.RunScheduler;
 import org.srdbs.sftp.Sftp;
 import org.srdbs.split.FileData;
 import org.srdbs.split.MYSpFile;
 import org.srdbs.split.MyFile;
 import java.text.DateFormat;
 import java.util.Date;
 import javax.crypto.Cipher;
 import javax.crypto.CipherOutputStream;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.SecretKeySpec;
 import java.io.*;
 import java.security.SecureRandom;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import static org.srdbs.split.Split.mySplit;
 
 
 /**
  * Secure and Redundant Data Backup System.
  * User: Thilina Piyasundara
  * Date: 5/25/12
  * Time: 10:46 AM
  * For more details visit : http://www.thilina.org
  */
 public class RunBackup {
 
     public static Logger logger = Logger.getLogger("systemsLog");
     public static Logger backplogger = Logger.getLogger("backupLog");
     public static long fid;
 
     private static final int IV_LENGTH = 16;
     public static String Despath;
     public static int count;
     public static String newFileName;
 
     public static Date date;
     public static DateFormat datef;
 
     public static int runBackup(String path, final String dest, int compress, int encrypt) {
 
         backplogger.info("Running the backup log.");
         backplogger.info("Running the backup for : " + dest + " (compress " + compress + ", encrypt " + encrypt + ")");
 
         DbConnect dbConnect = new DbConnect();
         int noOfFiles = 0;
         List<MyFile> listOfFiles = null;
         listOfFiles = FileData.Read(path);
         List<MYSpFile> dListOfFiles = null;
 
 
         for (final MyFile file : listOfFiles) {
 
             //if both compression and encryption enable
             if ((compress != 0) && (encrypt != 0)) {
 
                 String fzip = file.getName() + ".zip";
 
                 try {
                     compressFile(path + "/" + file.getName());
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
                 String fileName = path + "/" + fzip;
                 String tempFileName = fileName + ".enc";
 
 
                 try {
                     copy(Cipher.ENCRYPT_MODE, fileName, tempFileName, "password12345678");
                     System.out.println("Success. Find encrypted and decripted files in current directory");
                     logger.info("Success. Find encrypted and decripted files in current directory" + fzip);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
                 String fzipencrypt = file.getName() + ".zip.enc";
                 newFileName = fzipencrypt;
 
                 try {
                     Despath = CreateFolder(dest);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
                 long FSize = FileData.getFileSize(path + "/" + fzip);
 
                 int bandwidthSum = 1024 * (((int) (Math.random() * 10) % 9) + 10);
                 int packetVal = (int) (FSize / bandwidthSum);
                 backplogger.info("Packet size : " + packetVal + " File size : " + file.getSize() + " c1 : " + Global.c1Bandwidth
                         + " c2 : " + Global.c2Bandwidth + " c3 : " + Global.c3Bandwidth);
                 count = mySplit(path + Global.fs + fzipencrypt, Despath
                         + Global.fs + fzipencrypt, packetVal);
                 File f = new File(path + Global.fs + fzip);
                 File f1 = new File(path + Global.fs + fzipencrypt);
                 f.delete();
                 f1.delete();
                 backplogger.info("Split Files in the file path of : "
                         + path + Global.fs + file.getName()
                         + " in to " + count + " parts.");
                 noOfFiles++;
 
                 try {
                     dbConnect.saveFiles(fzipencrypt, file.getSize(), file.getHash(), file.getcDate());
                     backplogger.info("Save full file details to the database.");
                 } catch (Exception e) {
                     backplogger.error("Database connection error : " + e);
                 }
 
                 backplogger.info("Compressing and Encrypting the backup files : " + file.getName());
             }
 
             //if compression is enabled.
             if ((compress != 0) && (encrypt == 0)) {
 
                 String fzip = file.getName() + ".zip";
 
                 try {
                     compressFile(path + "/" + file.getName());
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
 
                 String fzipencrypt = file.getName() + ".zip";
                 newFileName = fzipencrypt;
 
                 try {
                     Despath = CreateFolder(dest);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 long FSize = FileData.getFileSize(path + "/" + fzip);
 
                 int bandwidthSum = 1024 * (((int) (Math.random() * 10) % 9) + 10);
                 int packetVal = (int) (FSize / bandwidthSum);
                 backplogger.info("Packet size : " + packetVal + " File size : " + file.getSize() + " c1 : " + Global.c1Bandwidth
                         + " c2 : " + Global.c2Bandwidth + " c3 : " + Global.c3Bandwidth);
                 count = mySplit(path + Global.fs + fzipencrypt, Despath
                         + Global.fs + fzipencrypt, packetVal);
                 File f = new File(path + Global.fs + fzipencrypt);
                 f.delete();
                 backplogger.info("Split Files in the file path of : "
                         + path + Global.fs + file.getName()
                         + " in to " + count + " parts.");
                 noOfFiles++;
                 try {
                     dbConnect.saveFiles(fzip, file.getSize(), file.getHash(), file.getcDate());
                     backplogger.info("Save full file details to the database.");
                 } catch (Exception e) {
                     backplogger.error("Database connection error : " + e);
                 }
 
                 backplogger.info("Compressing the backup files : " + file.getName());
             }
             // if encryption is enabled.
             if ((encrypt != 0) && (compress == 0)) {
 
                 String fzip = file.getName();
 
                 String fileName = path + "/" + fzip;
                 String tempFileName = fileName + ".enc";
 
                 try {
                     copy(Cipher.ENCRYPT_MODE, fileName, tempFileName, "password12345678");
                     System.out.println("Success. Find encrypted and decripted files in current directory");
                     logger.info("Success. Find encrypted and decripted files in current directory" + fzip);
 
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
 
                 String fzipencrypt = file.getName() + ".enc";
                 newFileName = fzipencrypt;
 
                 try {
                     Despath = CreateFolder(dest);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
                 long FSize = FileData.getFileSize(path + "/" + fzip);
 
                 int bandwidthSum = 1024 * (((int) (Math.random() * 10) % 9) + 10);
                 int packetVal = (int) (FSize / bandwidthSum);
                 backplogger.info("Packet size : " + packetVal + " File size : " + file.getSize() + " c1 : " + Global.c1Bandwidth
                         + " c2 : " + Global.c2Bandwidth + " c3 : " + Global.c3Bandwidth);
                 count = mySplit(path + Global.fs + fzipencrypt, Despath
                         + Global.fs + fzipencrypt, packetVal);
                 File f = new File(path + Global.fs + fzipencrypt);
                 f.delete();
                 backplogger.info("Split Files in the file path of : "
                         + path + Global.fs + file.getName()
                         + " in to " + count + " parts.");
                 noOfFiles++;
                 try {
                     dbConnect.saveFiles(fzipencrypt, file.getSize(), file.getHash(), file.getcDate());
                     backplogger.info("Save full file details to the database.");
                     dbConnect.InsertStatus("test1","done","Save Full file details to the database.");
                 } catch (Exception e) {
                     backplogger.error("Database connection error : " + e);
                     dbConnect.InsertStatus("test1","done","Save fail details to the database.");
                 }
 
                 backplogger.info("Encrypting the backup files : " + file.getName());
 
             }
 
             if ((encrypt == 0) && (compress == 0)) {
                 String fnormal = file.getName();
                 newFileName = fnormal;
 
                 try {
                     Despath = CreateFolder(dest);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
                 long FSize = FileData.getFileSize(path + "/" + fnormal);
 
 
                 int bandwidthSum = 1024 * (((int) (Math.random() * 10) % 9) + 10);
                 int packetVal = (int) (FSize / bandwidthSum);
                 backplogger.info("Packet size : " + packetVal + " File size : " + file.getSize() + " c1 : " + Global.c1Bandwidth
                         + " c2 : " + Global.c2Bandwidth + " c3 : " + Global.c3Bandwidth);
                 count = mySplit(path + Global.fs + fnormal, Despath
                         + Global.fs + fnormal, packetVal);
 
                 backplogger.info("Split Files in the file path of : "
                         + path + Global.fs + file.getName()
                         + " in to " + count + " parts.");
                 noOfFiles++;
 
                 try {
                     dbConnect.saveFiles(file.getName(), file.getSize(), file.getHash(), file.getcDate());
                     dbConnect.deleteStatus();
                     backplogger.info("Save full file details to the database.");
                     dbConnect.deleteStatus();
                     dbConnect.InsertStatus("test1","done","Save full file details to the database.");
                 } catch (Exception e) {
                     dbConnect.InsertStatus("test1","done","Save fail details to the database.");
                     backplogger.error("Database connection error : " + e);
                 }
 
 
             }
 
             // RAID
             int[] raidArray = Sftp.raid(count);
             dbConnect.deleteStatus();
             backplogger.info("Raid is done.");
             dbConnect.InsertStatus("test1","done","raid is done.");
 
             //TODO remove this
             for (int j = 0; j < raidArray.length; ) {
                 backplogger.info("Packet " + (j + 2) / 2 + ": Orignal: " + raidArray[j] + " , " + "Raid: " + raidArray[j + 1]);
                 j = j + 2;
             }
 
             try {
                 dListOfFiles = FileData.ReadSPFile(Despath, count, raidArray);
 
                 for (MYSpFile file2 : dListOfFiles) {
                     dbConnect.saveSPFiles(file2.getFid(), file2.getName(), file2.getSize(),
                             file2.getHash(), file2.getCloud(), file2.getRCloud());
 
                     fid = file2.getFid();
 
 
                 }
                 dbConnect.InsertStatus("isankatest","isankatest","Save split file details to the database.");
                 dbConnect.InsertStatus("isankatest","isankatest","save to database");
 
                 backplogger.info("Save split file details to the database. ");
             } catch (Exception e) {
                 backplogger.error("Database connection error : " + e);
                 dbConnect.InsertStatus("isankatest","isankatest","fail Save split file details to the database.");
                 dbConnect.InsertStatus("isankatest","isankatest","fail some operations.");
             }
 
             date = new Date();
             datef = new SimpleDateFormat("yyMMddHHmmss");
 
             backplogger.info("Uploading " + newFileName + " to cloud 1.");
             Sftp.upload(Despath + Global.fs + newFileName, fid, datef.format(date));
 
             backplogger.info("Uploading " + newFileName + " to cloud 2.");
             Sftp.upload1(Despath + Global.fs + newFileName, fid, datef.format(date));
 
             backplogger.info("Uploading " + newFileName + " to cloud 3.");
             Sftp.upload2(Despath + Global.fs + newFileName, fid, datef.format(date));
             dbConnect.InsertStatus("test2","done","raid is done.");
            dbConnect.InsertStatus("isankatest","isankatest","upload done");
 
             File delfol = new File(Despath);
             boolean isDeleted = deleteDir(delfol);
             System.out.println("Folder is Deleted : "+ isDeleted);
             backplogger.info("Folder is Deleted : "+ isDeleted);
 
         }
 
         backplogger.info("Split " + noOfFiles + " Files in the file path of : " + path);
         dbConnect.InsertStatus("isankatest","isankatest","All operation done.");
 
         return 0;
     }
 
     public static void compressFile(String path) {
 
         String D_path = path + ".zip";
 
         try {
             try (ZipOutputStream out = new ZipOutputStream(new
                     BufferedOutputStream(new FileOutputStream(D_path)))) {
                 byte[] data = new byte[1000];
                 BufferedInputStream in = new BufferedInputStream(new FileInputStream(path));
                 int count;
                 out.putNextEntry(new ZipEntry("outFile.zip"));
                 while ((count = in.read(data, 0, 1000)) != -1) {
                     out.write(data, 0, count);
                 }
                 in.close();
                 out.flush();
             }
             System.out.println("Your file is zipped");
 
 
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
 
     public static String CreateFolder(String path) throws Exception {
 
         String fldate;
 
         DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
         java.util.Date date = new java.util.Date();
         fldate = dateFormat.format(date);
 
         String strDirectoy = path + "/" + fldate;
 
         // Create one directory
         boolean success = (
                 new File(strDirectoy)).mkdir();
         if (success) {
             System.out.println("Directory: " + strDirectoy + " created");
         }
 
         return strDirectoy;
     }
 
     public static void copy(int mode, String inputFile, String outputFile, String password) throws Exception {
 
         BufferedInputStream is = new BufferedInputStream(new FileInputStream(inputFile));
         BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
         if (mode == Cipher.ENCRYPT_MODE) {
             encrypt(is, os, password);
         } else if (mode == Cipher.DECRYPT_MODE) {
             //decrypt(is, os, password);
         } else throw new Exception("unknown mode");
         is.close();
         os.close();
     }
 
     public static void encrypt(InputStream in, OutputStream out, String password) throws Exception {
 
         SecureRandom r = new SecureRandom();
         byte[] iv = new byte[IV_LENGTH];
         r.nextBytes(iv);
         out.write(iv); //write IV as a prefix
         out.flush();
         //System.out.println(">>>>>>>>written"+Arrays.toString(iv));
 
         Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding"); //"DES/ECB/PKCS5Padding";"AES/CBC/PKCS5Padding"
         SecretKeySpec keySpec = new SecretKeySpec(password.getBytes(), "AES");
         IvParameterSpec ivSpec = new IvParameterSpec(iv);
         cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
 
         out = new CipherOutputStream(out, cipher);
         byte[] buf = new byte[1024];
         int numRead = 0;
         while ((numRead = in.read(buf)) >= 0) {
             out.write(buf, 0, numRead);
         }
         out.close();
     }
 
       public static boolean deleteDir(File dir) {
 
         if (dir.isDirectory()) {
             String[] children = dir.list();
             for (int i=0; i<children.length; i++) {
 
                 boolean success = deleteDir(new File(dir, children[i]));
                 if (!success) {
 
                     System.out.println("Folder is not Deleted : "+ dir);
                     backplogger.error("Folder is not Deleted : " + dir);
                     return false;
                 }
             }
         }
 
         // The directory is now empty so delete it
         return dir.delete();
     }
 
 }
