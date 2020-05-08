 
 package client;
 
 import fileutil.FileInfo;
 import fileutil.FileUtil;
 import fileutil.MD5Calculator;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.nio.file.DirectoryNotEmptyException;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.rmi.Naming;
 import java.rmi.RemoteException;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.HashSet;
 import javax.swing.Timer;
 import rmiClass.RedirectorControl;
 import rmiClass.ServerControl;
 
 /**
  *
  * @author
  */
 public class Client {
     final public static int CHECK_INTERVAL = 6000; // check update every 6 seconds
     private String userName;
     private Path syncPath; // the path of the sync folder, eg. C:\blackboxsync
     private Path metaPath; // the path of the folder that stores meta data
     private ArrayList<String> redirectorList = new ArrayList<>(3); // list of all ip addresses of re-director
     private int whichIP = 0; // the index of re-director IP that this client connects to
     private HashSet<Path> taskSet = new HashSet<>(); // the set of all processing transfer task
     
     /**
      * Constructor
      * @param userName
      * @param rootPath the root path of the sync folder, eg. "C:"
      */
     public Client(String userName, String rootPath) throws RemoteException {
         
         this.syncPath = Paths.get(rootPath, "blackboxsync");
         metaPath = Paths.get(".metadata");
         
         checkFolder();
         
         redirectorList.add("23.21.222.40");
         redirectorList.add("54.234.9.61");
         redirectorList.add("23.22.127.255");
         
         /* log in and validate the user name first */
         if ( ! logIn(userName) ) {
             System.exit(1);
         }
         this.userName = userName;
         /* check update first */
         checkUpdate();
         /* setup the timer to check update periodically */
         new Timer(CHECK_INTERVAL, new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 checkFolder();
                 checkUpdate();
             }
             
         }).start();
         while (true);
     }
     /**
      * connect to the server and log in
      * @param userName
      * @return true if log in successfully
      */
     private boolean logIn(String userName) {
          while (true) {
             String ipAddr = redirectorList.get(whichIP % redirectorList.size());
             try {
                 /* log in and validate the user name */
                 RedirectorControl rdCtrl = (RedirectorControl) Naming.lookup("rmi://" + ipAddr + ":51966/Redirect");
                 if (!rdCtrl.login(userName)) {
                     System.out.println("Fail to log in: user name is not correct");
                     return false;
                 }
                 System.out.println("Login successfully!");
                 return true;
             } catch (RemoteException ex) {
                 /* the server may fail, connect to another server */
                 System.out.println("Re-director: " + ipAddr + " has no response. Change to another Re-director.");
                 whichIP++;
             }  catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
     /**
      * check whether the metadata folder and the sync folder exists under the given root path
      */
     private void checkFolder() {
         File folder = metaPath.toFile();
         if ( ! folder.exists() ) {
             folder.mkdir();
         }
         folder = syncPath.toFile();
         if ( ! folder.exists() ) {
             folder.mkdir();
         }
     }
     /**
      * check if there are any changes in both local and server sides
      * then take actions depends on the changes
      */
     private void checkUpdate() {
         System.out.println("Checking update...");
         /* check if there are any files have been deleted */
         checkDeletion();
 
         try {
              /* get the records from the server */
             final ArrayList<FileInfo> serverRecords = getServerRecord();
            /*
            * check if there is a file that exists in servers but not in the local machine
             * if yes, download this file
             */
            for ( int i = 0; i < serverRecords.size(); i++ ) {
                FileInfo serverRecord = serverRecords.get(i);
                if ( !searchLocalFile(serverRecord) ) {
                    /* download the file */
                    Path downloadPath = syncPath.resolve(serverRecord.getSubpath());
                    if ( taskSet.add(downloadPath) ) {
                        new Thread(new FileOps(Client.this, FileOps.DOWNLOAD, downloadPath)).start();
                    }
                }
            }
             /* visit all files in the sync folder */
             Files.walkFileTree(syncPath, 
                     new SimpleFileVisitor<Path>() {
                         @Override
                         public FileVisitResult visitFile(Path dir,
                                 BasicFileAttributes attrs) throws IOException {
                             /* check if the file has been recorded on both client side and server side */
                             LocalRecord localRecord = searchLocalRecord(dir);
                             FileInfo fileInfo = searchServerRecord(serverRecords, dir);
                             if ( localRecord != null && fileInfo != null ) {
                                 /* deal with conflict based on timestamp */
                                 switch (checkConflict(localRecord, fileInfo, dir)) {
                                     case 1:
                                         /* no conflicts. do nothing */
                                         break;
                                     case 2:
                                         /* 
                                          * file may has been modified off-line 
                                          * upload the file and replace the one in the server
                                          */
                                         if ( taskSet.add(dir) ) {
                                             new Thread(new FileOps(Client.this, FileOps.UPLOAD, dir)).start();
                                         }
                                         break;
                                     case 3:
                                         /* 
                                          * file may has been modified by another client
                                          * download the file and replace the one in local machine
                                          */
                                         if ( taskSet.add(dir) ) {
                                             new Thread(new FileOps(Client.this, FileOps.DOWNLOAD, dir)).start();
                                         }
                                         break;
                                     case 4:
                                         /*
                                          * file has been modified by another client,
                                          * but the same file in local machine has also been modified but never been synchronized
                                          * in this case, rename the file as a copy and upload the copy
                                          */
                                         Path copyPath = FileUtil.createCopyName(dir.toFile());
                                         /* before renaming, delete the related local record first */
                                         Path recordPath = metaPath.resolve(
                                                             MD5Calculator.getMD5(
                                                                 FileUtil.parsePath(dir).toString().getBytes()));
                                         Files.delete(recordPath);
                                         dir.toFile().renameTo(copyPath.toFile());// rename the file as a copy
                                         dir = copyPath;
                                         if ( taskSet.add(dir) ) {
                                             new Thread(new FileOps(Client.this, FileOps.UPLOAD, dir)).start();
                                         }
                                         break;
                                     default:
                                 }
                             }
                             if ( localRecord != null && fileInfo == null ) {
                                 /* 
                                  * the file has been deleted by another client
                                  * delete the local file as well
                                  */
                                 deleteLocalFile(dir);
                             }
                             if ( localRecord == null && fileInfo != null ) {
                                 /*
                                  * the local file is created off-line and
                                  * there is a file with the same name on the server
                                  * rename the file as a copy and upload this file
                                  */
                                 Path copyPath = FileUtil.createCopyName(dir.toFile());
                                 dir.toFile().renameTo(copyPath.toFile());// rename the file as a copy
                                 dir = copyPath;
                                 if ( taskSet.add(dir) ) {
                                     new Thread(new FileOps(Client.this, FileOps.UPLOAD, dir)).start();
                                 }
                             }
                             if ( localRecord == null && fileInfo == null ) {
                                 /* 
                                  * the file is not synchroned yet
                                  * just simply upload this file
                                  */
                                 if ( taskSet.add(dir) ) {
                                     new Thread(new FileOps(Client.this, FileOps.UPLOAD, dir)).start();
                                 }
                             }
                            
                             return FileVisitResult.CONTINUE;
                         }
                     });
         } catch (Exception ex) {
            /* 
             * the server may fail while checking updates
             * try checking updates next time
             */
              System.out.println("Server has no response. We will try again.");
              return;
         }
         System.out.println("Finish checking update.");
     }
     /**
      * check if any local files have been deleted
      */
     private void checkDeletion() {
         /* get all local record */
         File[] records = metaPath.toFile().listFiles();
         /* get all files in the sync folder */
         File[] localFiles = listAllFiles(syncPath);
         /* 
          * to see if for each record, the corresponding file can be found
          */
         for ( int i = 0; i < records.length; i++ ) {
             String recordsName = records[i].getName(); // the file name of the record file
             boolean shouldDelete = true;
             for ( int j = 0; j < localFiles.length; j++ ) {
                 String subpathStr = FileUtil.parsePath(localFiles[j].toPath()).toString();
                 if ( recordsName.equals(
                         MD5Calculator.getMD5(
                             subpathStr.getBytes())) ) {
                     /* 
                      * because the record file's name comes from the MD5 of the corresponding file's name
                      * equals means the record can find the original file
                      */
                     shouldDelete = false;
                     break;
                 }
             }
             if (shouldDelete) {
                 /* 
                  * the record cannot find the corresponding file
                  * means the local file has been deleted
                  * tell the server to delete this file
                  * the client will wait for this operation to complete
                  */
                 new FileOps(this, FileOps.DELETE, recordsName).run();
                     try {
                         /* delete the record too */
                        Files.delete(records[i].toPath());
                     } catch (IOException ex) {
                         ex.printStackTrace();
                     }
             }
         }
     }
     /**
      * ask for the records from the server
      * @return list of all records belongs to this user
      */
     private ArrayList<FileInfo> getServerRecord() throws Exception{
         String serverIP = FileOps.askServerIP(redirectorList);
         while (true) {
             ServerControl serverCtrl = (ServerControl) Naming.lookup("rmi://" + serverIP + ":47805/Server");
             return serverCtrl.getRecord(userName);        
         }
     }
     /**
      * check if the given file has the corresponding record stored in the metadata folder
      * @param localFilePath
      * @return null if the record doesn't exist
      */
     private LocalRecord searchLocalRecord(Path localFilePath) {
         File localReocordFile = findLocalRecord(localFilePath).toFile();
         if (localReocordFile.exists()) {
             try {
                 ObjectInputStream ois = new ObjectInputStream(
                                  new FileInputStream(localReocordFile));
                 LocalRecord localRecord = (LocalRecord) ois.readObject();
                 ois.close();
                 return localRecord;
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
             
         }
         return null;
     }
     /**
      * search if the server records contains the records of the given local file
      * @return 
      */
     private FileInfo searchServerRecord(ArrayList<FileInfo> serverRecordsList, Path localFilePath) {
         Path subpath = FileUtil.parsePath(localFilePath);
         for ( int i = 0; i < serverRecordsList.size(); i++ ) {
             FileInfo fileInfo = serverRecordsList.get(i);
             if ( fileInfo.getSubpath().equals(subpath) ) {
                 return fileInfo;
             }
         }
         return null;
     }
     /**
      * delete a local file and the corresponding local record
      * @param dir the path of the local file which should be deleted
      */
     private void deleteLocalFile(Path dir) {
         try {
             Files.delete(dir);
             /* delete the folder if the foler is empty */
             Path parent = dir.getParent();
             while ( !parent.equals(syncPath) ) {
                 try {
                     Files.delete(parent);
                     parent = parent.getParent();
                 } catch (DirectoryNotEmptyException ex) {
                     break;
                 }
             }
             Files.delete(findLocalRecord(dir));
         } catch (Exception ex) {
             System.out.println("Fail to delete a local file: " + dir);
         }
     }
     /**
      * get the corresponding local record based on the given local file
      * @param dir the path of the local file
      * @return the path of the local record
      */
     private Path findLocalRecord(Path dir) {
         Path recordPath = metaPath.resolve(
                                     MD5Calculator.getMD5(
                                         FileUtil.parsePath(dir).toString().getBytes())
                                             );
         return recordPath;
     }
     /**
      * find the corresponding local file based on the given server record
      * @param serverRecord
      * @return true if the local file is found
      */
     private boolean searchLocalFile(FileInfo serverRecord) {
         Path dir = syncPath.resolve(serverRecord.getSubpath());
         if (dir.toFile().exists()) {
             return true;
         }
         return false;
     }
     public String getUserName() {
         return userName;
     }
     /**
      * remove a task from the transfer task set
      * normally, it is called when the task is finished
      * @param dir the absolute path of the file that finishes transfer
      */
     public void removeTask(Path dir) {
         taskSet.remove(dir);
     }
     public Path getSyncPath() {
         return syncPath;
     }
     public Path getMetaPath() {
         return metaPath;
     }
     public ArrayList<String> getRedirectorList() {
         return redirectorList;
     }
     /**
      * check how the local timestamps and server timestamps conflict with each other
      * @param localrcd
      * @param serverrcd
      * @return 1 if there is no conflict; 2 if server time are same but local time are different;
      * 3 if server time are different but local time are same; 4 if both are different
      */
     private int checkConflict(LocalRecord localrcd, FileInfo serverrcd, Path dir) throws IOException {
         long localSyncTime = localrcd.getLastSyncTime();
         long serverSyncTime = serverrcd.getSyncTime();
         long localMdfTime = localrcd.getLastModifiedTime();
         long fileMdfTime = Files.getLastModifiedTime(dir).toMillis();
         if ( (localSyncTime == serverSyncTime) &&
              (localMdfTime == fileMdfTime) ) {
             return 1;
         }
         if ( (localSyncTime == serverSyncTime) &&
              (localMdfTime != fileMdfTime) ) {
             return 2;
         }
         if ( (localSyncTime < serverSyncTime) &&
              (localMdfTime == fileMdfTime) ) {
             return 3;
         }
         if ( (localSyncTime < serverSyncTime) &&
              (localMdfTime != fileMdfTime) ) {
             return 4;
         }
         return 5;
     }
     /**
      * list all files in the given directory, including files in all folder
      * @param path
      * @return list of all files
      */
     private File[] listAllFiles(Path path) {
         final ArrayDeque<File> queue = new ArrayDeque<>();
         try {
             Files.walkFileTree(path, 
                 new SimpleFileVisitor<Path>() {
                     @Override
                     public FileVisitResult visitFile(Path dir,
                         BasicFileAttributes attrs) throws IOException {
                             queue.add(dir.toFile());
                             return FileVisitResult.CONTINUE;
                         }
                     });
         } catch (IOException ex) {
             ex.printStackTrace();
         }
         File[] result = new File[queue.size()];
         queue.toArray(result);
         return result;
     }
 }
