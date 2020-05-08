 package info.mikaelsvensson.ftpbackup.command.fileprocessing;
 
 import info.mikaelsvensson.ftpbackup.model.FtpDestination;
 import info.mikaelsvensson.ftpbackup.model.filesystem.FileSystemFile;
 import info.mikaelsvensson.ftpbackup.model.filesystem.FileSystemFolder;
 import info.mikaelsvensson.ftpbackup.util.FTPSession;
 import info.mikaelsvensson.ftpbackup.util.FTPSessionException;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.net.ftp.FTPFile;
 
 import java.io.IOException;
 import java.util.*;
 
 public class FTPFileProcessingStrategy extends AbstractFileProcessingStrategy {
 // ------------------------------ FIELDS ------------------------------
 
     private FTPSession session;
     private final FtpDestination ftpDestination;
     private final Stack<FolderStackItem> workingPath = new Stack<>();
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
 //    private static final Logger LOGGER = RunJobCommand.LOGGER;
     public FTPFileProcessingStrategy(FtpDestination ftpDestination) throws IOException, FTPSessionException {
         this.ftpDestination = ftpDestination;
     }
 
 // ------------------------ INTERFACE METHODS ------------------------
 
 
 // --------------------- Interface FileProcessingStrategy ---------------------
 
     @Override
     public void changeToParentDirectory() throws IOException {
         if (!session.changeToParentDirectory()) {
             throw new IOException("Could not change to parent directory.");
         }
     }
 
     @Override
     public void changeWorkingDirectory(String folderName) throws IOException {
         if (!session.changeWorkingDirectory(folderName)) {
             throw new IOException("Could not change to directory '" + folderName + "'.");
         }
     }
 
     @Override
     public void connect() throws IOException {
         try {
             this.session = FTPSession.connect(ftpDestination, ftpDestination.getPathToBaseFromRoot());
             String workingDirectory = session.getWorkingDirectory();
             for (String folderName : StringUtils.split(workingDirectory, FTPSession.FTP_PATH_SEPARATOR_CHAR)) {
                 workingPath.push(new FolderStackItem(ExistStatus.YES, folderName));
             }
         } catch (FTPSessionException e) {
             throw new IOException(e);
         }
     }
 
     @Override
     public void disconnect() throws IOException {
         session.quit();
     }
 
     @Override
     public String getDestinationId() {
         return ftpDestination.getId();
     }
 
     @Override
    public Collection<FileSystemFolder> getDirectoriesInWorkingDirectory() throws IOException {
        ArrayList<FileSystemFolder> files = new ArrayList<>();
        for (FTPFile file : session.listDirectories()) {
            if (file.isDirectory() && !isCurrentOrParentDirectory(file)) {
                files.add(new FileSystemFolder(file.getName(), file.getTimestamp()));
             }
         }
        return files;
     }
 
     @Override
     public char getDirectorySeparator() {
         return FTPSession.FTP_PATH_SEPARATOR_CHAR;
     }
 
     @Override
     public Collection<FileSystemFile> getFilesInWorkingDirectory() {
         try {
             ArrayList<FileSystemFile> files = new ArrayList<>();
             for (FTPFile file : session.listFiles()) {
                 if (file.isFile() && !isCurrentOrParentDirectory(file)) {
                     files.add(new FileSystemFile(file.getName(), file.getTimestamp(), file.getSize()));
                 }
             }
             return files;
         } catch (IOException e) {
             LOGGER.warn("Could not list files.", e);
             return Collections.emptyList();
         }
     }
 
     @Override
     public String getInitialPath() {
         return session.getInitialPath();
     }
 
     @Override
     public int getServerTimeOffset() throws IOException {
         return session.getServerTimeOffset();
     }
 
     @Override
     public String getWorkingDirectory() {
         try {
             return session.getWorkingDirectory();
         } catch (IOException e) {
             LOGGER.warn("Could not determine working directory.", e);
             return null;
         }
     }
 
     @Override
     public String getWorkingDirectoryRelativeToInitialPath(char separator) {
         //TODO: Use system-dependent folder separator
         String workingDirectory = getWorkingDirectory();
         return workingDirectory.substring(session.getInitialPath().length()).replace(FTPSession.FTP_PATH_SEPARATOR_CHAR, separator);
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     @Override
     public void createFolderInWorkingDirectoryImpl(String folderName) throws IOException {
         if (!session.makeDirectory(folderName)) {
             throw new IOException("Could not create folder'" + folderName + "'.");
         }
     }
 
     @Override
     public void deleteFileInWorkingDirectoryImpl(String fileName) throws IOException {
         if (!session.deleteFile(fileName)) {
             throw new IOException("Could not delete file '" + fileName + "'.");
         }
     }
 
     @Override
     public void deleteFolderInWorkingDirectoryImpl(String folderName) throws IOException {
         if (!session.deleteDirectory(folderName)) {
             throw new IOException("Could not delete directory '" + folderName + "'.");
         }
     }
 
     private boolean isCurrentOrParentDirectory(final FTPFile file) {
         return ".".equals(file.getName()) || "..".equals(file.getName());
     }
 
     @Override
     public void moveFileImpl(String fromAbsolute, String toAbsolute) throws IOException {
         if (!session.rename(fromAbsolute, toAbsolute)) {
             throw new IOException("Could not move '" + fromAbsolute + "' to '" + toAbsolute + "'.");
         }
     }
 
     @Override
     public void uploadFileToWorkingDirectoryImpl(String absolutePath) throws IOException {
         if (!session.uploadFile(absolutePath)) {
             throw new IOException("Could not upload '" + absolutePath + "'.");
         }
     }
 
 // -------------------------- ENUMERATIONS --------------------------
 
     protected enum ExistStatus {
         NO,
         YES,
         YES_FAKE
     }
 
 // -------------------------- INNER CLASSES --------------------------
 
     class FolderStackItem {
         final ExistStatus exists;
         final Set<String> fakeSubFolders = new HashSet<>();
         public final String name;
 
         FolderStackItem(ExistStatus exists, String name) {
             this.exists = exists;
             this.name = name;
         }
 
         @Override
         public String toString() {
             return name;
         }
     }
 }
