 package info.mikaelsvensson.ftpbackup.command.fileprocessing;
 
 import info.mikaelsvensson.ftpbackup.command.CancellationRequestedException;
 import info.mikaelsvensson.ftpbackup.model.filesystem.LightFileSystemObject;
 import info.mikaelsvensson.ftpbackup.util.FTPSession;
 import info.mikaelsvensson.ftpbackup.util.I18n;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 
 public class DryRunFileProcessingStrategy extends AbstractFileProcessingStrategy {
 // ------------------------------ FIELDS ------------------------------
 
     private static final Logger LOGGER = Logger.getLogger(FileProcessingStrategy.class);
 
     private final FileProcessingStrategy realStrategy;
 
     private final Stack<FolderStackItem> workingPath = new Stack<>();
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     public DryRunFileProcessingStrategy(final FileProcessingStrategy realStrategy) {
         this.realStrategy = realStrategy;
     }
 
 // ------------------------ INTERFACE METHODS ------------------------
 
 
 // --------------------- Interface FileProcessingStrategy ---------------------
 
     @Override
     public void changeToParentDirectory() throws IOException {
         ExistStatus status = workingPath.empty() ? ExistStatus.YES : workingPath.peek().exists;
 
         workingPath.pop();
 
         switch (status) {
             case YES:
                 realStrategy.changeToParentDirectory();
             default:
 //                return true;
         }
     }
 
     @Override
     public void changeWorkingDirectory(String folderName) throws IOException {
         ExistStatus status = workingPath.empty() ? ExistStatus.YES : workingPath.peek().exists;
 
         switch (status) {
             case YES:
                 if (!workingPath.empty() && workingPath.peek().fakeSubFolders.contains(folderName)) {
                     workingPath.push(new FolderStackItem(ExistStatus.YES_FAKE, folderName));
                 } else {
                     realStrategy.changeWorkingDirectory(folderName);
                     workingPath.push(new FolderStackItem(ExistStatus.YES, folderName));
                 }
                 break;
             case YES_FAKE:
                 if (workingPath.peek().fakeSubFolders.contains(folderName)) {
                     workingPath.push(new FolderStackItem(ExistStatus.YES_FAKE, folderName));
                 } else {
                     String message = I18n.text(I18n.CORE_STRINGS, "FileProcessingStrategy.could.not.change.to.0.in.fake.folder", folderName);
                     LOGGER.warn(message);
                     throw new FileNotFoundException(message);
                 }
                 break;
             case NO:
             default:
                 throw new IllegalArgumentException();
         }
     }
 
     @Override
     public void connect() throws IOException {
         realStrategy.connect();
         String workingDirectory = this.realStrategy.getWorkingDirectory();
         for (String folderName : StringUtils.split(workingDirectory, FTPSession.FTP_PATH_SEPARATOR_CHAR)) {
             workingPath.push(new FolderStackItem(ExistStatus.YES, folderName));
         }
     }
 
     @Override
     public void disconnect() throws IOException {
         realStrategy.disconnect();
     }
 
     @Override
     public String getDestinationId() {
         return realStrategy.getDestinationId();
     }
 
     @Override
    public Collection<? extends LightFileSystemObject> getDirectoriesInWorkingDirectory() throws IOException, CancellationRequestedException {
         if (workingPath.peek().exists == ExistStatus.YES) {
             return realStrategy.getDirectoriesInWorkingDirectory();
         } else {
             return Collections.emptyList();
         }
     }
 
     @Override
     public char getDirectorySeparator() {
         return realStrategy.getDirectorySeparator();
     }
 
     @Override
     public Collection<? extends LightFileSystemObject> getFilesInWorkingDirectory() throws FileNotFoundException, CancellationRequestedException {
         if (workingPath.peek().exists == ExistStatus.YES) {
             return realStrategy.getFilesInWorkingDirectory();
         } else {
             return Collections.emptyList();
         }
     }
 
     @Override
     public String getInitialPath() {
         return realStrategy.getInitialPath();
     }
 
     @Override
     public int getServerTimeOffset() throws IOException {
         return realStrategy.getServerTimeOffset();
     }
 
     @Override
     public String getWorkingDirectoryRelativeToInitialPath(char separator) {
 //        throw new NotImplementedException();
         return realStrategy.getWorkingDirectoryRelativeToInitialPath(separator);
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     @Override
     public void createFolderInWorkingDirectoryImpl(String folderName) {
         LOGGER.debug("Would create folder " + folderName + " in " + getWorkingDirectory());
         workingPath.peek().fakeSubFolders.add(folderName);
     }
 
     @Override
     public String getWorkingDirectory() {
         return FTPSession.FTP_PATH_SEPARATOR_CHAR + StringUtils.join(workingPath, FTPSession.FTP_PATH_SEPARATOR_CHAR);
     }
 
     @Override
     public void deleteFileInWorkingDirectoryImpl(String fileName) {
         LOGGER.warn("The file '" + fileName + "' was not removed since this is a dry run. Subsequent file lists will be incorrect.");
     }
 
     @Override
     public void deleteFolderInWorkingDirectoryImpl(String folderName) {
         LOGGER.warn("The folder '" + folderName + "' was not removed since this is a dry run. Subsequent file lists will be incorrect.");
     }
 
     @Override
     public void moveFileImpl(String fromAbsolute, String toAbsolute) throws IOException {
         LOGGER.debug("Would move" + fromAbsolute + " to " + toAbsolute + " but didn't since this is a dry run. Subsequent file lists will be incorrect.");
     }
 
     @Override
     public void uploadFileToWorkingDirectoryImpl(String absolutePath) {
         LOGGER.debug("Would upload " + absolutePath + " to " + getWorkingDirectory() + " but didn't since this is a dry run. Subsequent file lists will be incorrect.");
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
