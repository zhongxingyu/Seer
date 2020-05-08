 package filesystem;
 
 import communication.FileIdentifierFactory;
 import communication.message.MessagesFactory;
 import communication.TCPClient;
 import main.MainEntry;
 import membership.Proc;
 import misc.MiscTool;
 import misc.TimeMachine;
 import org.apache.log4j.Logger;
 
 import java.io.*;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.*;
 
 import static communication.message.Messages.*;
 
 /**
  * This is the main part of SimpleDistributedFileSystem
  */
 public class SDFS {
 
     private Proc proc;
     private FileList fileList;
 
     private Map<String, Integer> timeStampMap;
     private Map<String, Long> localTimeMap;
     private Map<String, FileState> stateMap;
     private Map<String, Long> lastWriteTime;
 
     private static Logger logger = Logger.getLogger(SDFS.class);
     private String rootDirectory;
 
     private static final Integer MAX_TIME_DIFFERENCE = 500;
     private static final Integer MIN_TIME_DIFFERENCE = 200;
 
     public SDFS(String rootDirectory) {
         fileList = new FileList();  //file list
         timeStampMap = new HashMap<String, Integer>();  //heartbeat counting of file
         localTimeMap = new HashMap<String, Long>(); //localtime of file
         stateMap = new HashMap<String, FileState>();    // file state
         lastWriteTime = new HashMap<String, Long>();
         this.rootDirectory = rootDirectory;
     }
 
     public void init() {
         File root = new File(rootDirectory);
         if(!root.exists()) {
             if(!root.mkdir()){
                 logger.fatal("Create root directory fails");
                 System.exit(-1);
             }
         } else {
             if(!root.isDirectory()) {
                 logger.fatal("sdfs is not directory!!!");
                 System.exit(-1);
             }
             loadFilesFromRootDirectory();
         }
     }
 
     private void loadFilesFromRootDirectory() {
         File root = new File(rootDirectory);
         File[] files = root.listFiles();
         if(files == null) {
             return;
         }
         for(File f : files) {
             loadFileFromRootDirectory(f);
         }
     }
 
     public File getFile(String fileName) {
         return new File(rootDirectory + fileName);
     }
 
     /**
      * Get a file from local process.
      * @param SDFSFileName
      * @param localFileName
      */
     public void getRemoteFile(String SDFSFileName, String localFileName){
 //        new FileOperations().setProc(proc).get(localFileName, SDFSFileName, fileList);
         FileIdentifier remote, local;
         remote = local = null;
 
         for(FileIdentifier fileIdentifier : fileList.getList()) {
             if(!isAvailable(fileIdentifier)) {
                 continue;
             }
 
             if(fileIdentifier.getFileName().equals(SDFSFileName)) {
                 remote = fileIdentifier;
                 if(fileIdentifier.getFileStoringProcess().getId().equals(proc.getId())) {
                     local = fileIdentifier;
                 }
             }
         }
 
         if(local != null) {
             copyFile(getFile(SDFSFileName), localFileName);
             return;
         }
 
         sendGetMessage(remote);
         startReceivingFile(remote, localFileName);
     }
 
     public boolean isLocalFile(String fileName) {
         List<FileIdentifier> list = fileList.getList();
         for(FileIdentifier fid : list) {
             if(fid.getFileName().equals(fileName) && fid.getFileStoringProcess().getId().equals(proc.getId())) {
                 return true;
             }
         }
         return false;
     }
 
     private void sendGetMessage(FileIdentifier remote) {
         String address = remote.getFileStoringProcess().getIP() + ":"
                 + remote.getFileStoringProcess().getPort();
         TCPClient tcpclient = new TCPClient(address);
         tcpclient.setProc(proc);
         if(tcpclient.connect()) {
             Message m = MessagesFactory.generateGetMessage(remote, proc.getIdentifier());
             tcpclient.sendData(m);
             tcpclient.close();
         }
     }
 
     private void startReceivingFile(FileIdentifier fileIdentifier, String savedName) {
         try {
             ServerSocket serverSocket = new ServerSocket(proc.getTcpPort()+3);
             Socket socket = serverSocket.accept();
             File file = new File(savedName);
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
             BufferedInputStream bis  = new BufferedInputStream(socket.getInputStream());
 //            int nextByte;
 //            while((nextByte=bis.read())!=-1) {
 //                bos.write(nextByte);
 //            }
 //            bos.flush();
 //            bos.close();
             MiscTool.readFromInputStreamToOutputStream(bis, bos);
             socket.close();
             serverSocket.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
 
     public File openFile(String fileName) {
         return new File(rootDirectory + fileName);
     }
 
     public void setProc(Proc proc) {
         this.proc = proc;
     }
 
     public FileList getFileList() {
         return fileList;
     }
 
     /**
      * Add a local file to SDFS.
      * @param sourceFileName local file name
      */
     public void addFileLocally(String sourceFileName, String savedName) {
         synchronized (this) {
             File file = new File(sourceFileName);
             if(!file.exists()) {
                 return;
             }
 
             FileIdentifier fileIdentifier = FileIdentifierFactory.generateFileIdentifier(
                     proc.getIdentifier(), savedName, FileState.available);
 
             copyFile(file, rootDirectory + savedName);
             System.out.println("add available entry: " + file.getName() + ", " + proc.getTimeStamp());
             addAvailableEntryToFileList(fileIdentifier, proc.getTimeStamp());
         }
     }
 
     public void loadFileFromRootDirectory(File file) {
         synchronized (this) {
             String fileName = file.getName();
             FileIdentifier fileIdentifier = FileIdentifierFactory.generateFileIdentifier(
                     proc.getIdentifier(), fileName, FileState.available);
 
 //            copyFile(file, rootDirectory + fileName);
             addAvailableEntryToFileList(fileIdentifier, proc.getTimeStamp());
 
 //            addFileLocally(file.getName(), file.getName());
         }
     }
 
     private void copyFile(File sourceFile, String destination) {
         copyFile(sourceFile, new File(destination));
     }
 
     private void copyFile(File sourceFile, File destFile) {
         if(sourceFile.getAbsolutePath().equals(destFile.getAbsolutePath())){
             return;
         }
 
         FileInputStream fis;
         BufferedInputStream bis;
         BufferedOutputStream bos;
 
         try {
             fis = new FileInputStream(sourceFile);
             bis = new BufferedInputStream(fis);
         } catch (FileNotFoundException e) {
             logger.error("open source file error", e);
             return;
         }
 
         FileOutputStream fos;
         try {
             fos = new FileOutputStream(destFile);
             bos = new BufferedOutputStream(fos);
         } catch (FileNotFoundException e) {
             logger.error("open dest file error", e);
             return;
         }
 
         try {
             MiscTool.readFromInputStreamToOutputStream(bis, bos);
         } catch (IOException e) {
             logger.error("copy error", e);
         }
     }
 
     public void addAvailableEntryToFileList(FileIdentifier fileIdentifier, Integer timeStamp) {
         synchronized (this) {
             if(fileList.find(fileIdentifier)!=-1){
                 String key = generateKey(fileIdentifier);
                 if(stateMap.get(key) == FileState.syncing) {
                     timeStampMap.put(key, timeStamp);
                     localTimeMap.put(key, TimeMachine.getTime());
                     stateMap.put(key, FileState.available);
                 }
                 return;
             }
             addEntryToFileList(fileIdentifier, timeStamp, FileState.available);
         }
     }
 
     public void
     addEntryToFileList(FileIdentifier fileIdentifier, Integer timeStamp, FileState state) {
         synchronized (this) {
             fileList.addFile(fileIdentifier);
             String key = generateKey(fileIdentifier);
             timeStampMap.put(key, timeStamp);
             localTimeMap.put(key, TimeMachine.getTime());
             stateMap.put(key, state);
             lastWriteTime.put(key, fileIdentifier.getLastWriteTime());
         }
     }
 
     public void addSyncEntryToFileList(FileIdentifier fileIdentifier, Integer timeStamp) {
         synchronized (this) {
             if(fileList.find(fileIdentifier)!=-1){
                 return;
             }
             System.out.println("add syncing: " + fileIdentifier.getFileName() + ", " + timeStamp);
             addEntryToFileList(fileIdentifier, timeStamp, FileState.syncing);
         }
     }
 
     private String generateKey(FileIdentifier identifier) {
         return identifier.getFileStoringProcess().getIP()+":"+
                 identifier.getFileStoringProcess().getPort()+"/" +
                 identifier.getFileName();
     }
 
     public Integer getFileTimeStamp(FileIdentifier fileIdentifier) {
         String key = generateKey(fileIdentifier);
         return timeStampMap.get(key);
     }
 
     public FileState getFileState(FileIdentifier fileIdentifier) {
         String key =generateKey(fileIdentifier);
         return stateMap.get(key);
     }
 
     public Long getLastWriteTime(FileIdentifier fileIdentifier) {
         String key = generateKey(fileIdentifier);
         return lastWriteTime.get(key);
     }
 
     public Long getFileLocalTime(FileIdentifier fileIdentifier) {
         String key = generateKey(fileIdentifier);
         return localTimeMap.get(key);
     }
 
     public boolean isAvailable(FileIdentifier fileIdentifier) {
         String key = generateKey(fileIdentifier);
         return stateMap.containsKey(key) && stateMap.get(key) == FileState.available;
     }
 
     public boolean isSyncing(FileIdentifier fileIdentifier) {
         String key = generateKey(fileIdentifier);
         return stateMap.containsKey(key);
     }
 
     public boolean isValid(FileIdentifier fileIdentifier) {
         String key = generateKey(fileIdentifier);
         return stateMap.containsKey(key) &&
                 ((stateMap.get(key) == FileState.available) || (stateMap.get(key) == FileState.syncing));
     }
 
     public void updateFileList() {
         synchronized (this) {
             for(FileIdentifier fileIdentifier : getFileList()) {
 
                 if(fileIdentifier.getFileStoringProcess().getId().equals(proc.getId())
                         && isAvailable(fileIdentifier)) {
                     continue;
                 }
 
                 Long diff = TimeMachine.getTime() - localTimeMap.get(generateKey(fileIdentifier));
                 if(diff > MAX_TIME_DIFFERENCE) {
                     removeFileIdentifierFromList(fileIdentifier);
                     break;
                 } else if(diff > MIN_TIME_DIFFERENCE){
                     setToBeDeleted(fileIdentifier);
                 }
             }
         }
     }
 
     private void setToBeDeleted(FileIdentifier fileIdentifier) {
         synchronized (this){
             String key = generateKey(fileIdentifier);
             stateMap.put(key, FileState.toBeDeleted);
         }
     }
 
 
     private void removeFileIdentifierFromList(FileIdentifier fileIdentifier) {
         synchronized (this) {
             fileList.removeFile(fileIdentifier);
             String key = generateKey(fileIdentifier);
             timeStampMap.remove(key);
             localTimeMap.remove(key);
             stateMap.remove(key);
         }
     }
 
 
     public void updateFileListEntry(FileIdentifier identifier, Integer timeStamp) {
         synchronized (this) {
             String key = generateKey(identifier);
             timeStampMap.put(key, timeStamp);
             localTimeMap.put(key, TimeMachine.getTime());
             FileState oldState, newState;
             oldState = stateMap.get(key);
             newState = FileState.valueOf(identifier.getFileState());
             if(oldState.equals(FileState.available)) {
                 return;
             }
             stateMap.put(key, newState);
             lastWriteTime.put(key, identifier.getLastWriteTime());
         }
     }
 
     /**
      * Delete a file from SDFS.
      * @param fileName  file name to be deleted.
      * @param initialDelete When is is true, its the first time running this delete command.
      *                      It will be sent to all processes in the member list.
      */
 
     public void deleteFile(String fileName, boolean initialDelete) {
         synchronized (this) {
             boolean flag = false;
             LinkedList<ProcessIdentifier> list = new LinkedList<ProcessIdentifier>();
             for(FileIdentifier fileIdentifier : getFileList()) {
                 if(!fileIdentifier.getFileName().equals(fileName)) {
                     continue;
                 }
 
                 if(!isValid(fileIdentifier)) {
                     continue;
                 }
 
                 ProcessIdentifier processIdentifier = fileIdentifier.getFileStoringProcess();
                 if(processIdentifier.getId().equals(proc.getId())) {
                     setToBeDeleted(fileIdentifier);
                     deleteFileLocally(fileName);
                     flag = true;
                 } else {
                     list.add(processIdentifier);
                 }
             }
 
             if(flag || initialDelete) {
                 for(ProcessIdentifier processIdentifier : list) {
                     new FileOperations().setProc(proc).sendDeleteMessage(fileName,
                             processIdentifier.getIP(), processIdentifier.getPort());
                 }
             }
         }
     }
 
     /**
      * delete the file from local disk
      * @param fileName file name to be deleted.
      */
     private void deleteFileLocally(String fileName) {
         File f = new File(rootDirectory + fileName);
         long startTime = TimeMachine.getTime();
         if(f.delete()) {
             logger.info("successfully delete file: " + f.getName());
         } else {
             logger.error("failed in deleting file: " + f.getName());
         }
         float usingTime = (TimeMachine.getTime() - startTime) /10;
         System.out.println("Del uses time " + usingTime + " seconds");
     }
 
     public boolean createLocalSDFSFile(String fileName) {
         File newFile = openFile(fileName);
         try {
             if(!newFile.exists() && newFile.createNewFile()) {
                 loadFileFromRootDirectory(newFile);
             }
         } catch (IOException e) {
             logger.error("create sdfs file fail " + e);
             return false;
         }
         return true;
     }
 
     public boolean isStoredOnProcess(ProcessIdentifier pid, String fileName) {
         for(FileIdentifier fid : fileList.getList()) {
             if(fid.getFileName().equals(fileName) && fid.getFileStoringProcess().equals(pid)) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean hasSDFSFile(String fileName) {
         return fileList.hasFile(fileName);
     }
 
     public void updateLastWriteTime(String fileName, ProcessIdentifier pid, Long time) {
         synchronized (this) {
             FileIdentifier fid = fileList.find(pid, fileName);
             try {
                 String key = generateKey(fid);
                 lastWriteTime.put(key, time);
             } catch (NullPointerException ex) {
                 System.out.println("fileName = " + fileName);
                 System.out.println("Process = " + pid.getId() + ":" + pid.getPort());
 
                 for(FileIdentifier tmpfid : proc.getSDFS().getFileList()) {
                     System.out.println(tmpfid.getFileName() + "\t" + tmpfid.getFileStoringProcess().getIP()
                         + ":" + tmpfid.getFileStoringProcess().getPort());
                 }
 
             }
         }
     }
 
 
     public boolean appendDataToLocalFile(String fileName, String data) {
         try {
             BufferedWriter bw = new BufferedWriter(new FileWriter(rootDirectory + fileName, true));
             bw.write(data);
             bw.write('\n');
             bw.close();
             updateLastWriteTime(fileName, proc.getIdentifier(), new Date().getTime());
 
         } catch (IOException e) {
             logger.error("error in appending data to local file ", e);
             return false;
         }
         return true;
     }
 
     public String getRootDirectory() {
         return rootDirectory;
     }
 }
