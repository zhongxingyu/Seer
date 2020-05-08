 package info.mikaelsvensson.ftpbackup.command.fileprocessing;
 
 import info.mikaelsvensson.ftpbackup.command.CancellationRequestedException;
 import info.mikaelsvensson.ftpbackup.model.filesystem.LightFileSystemObject;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Collection;
 
 @SuppressWarnings("UnusedReturnValue")
 public interface FileProcessingStrategy {
 // -------------------------- OTHER METHODS --------------------------
 
     void addFileProcessingStrategyListener(FileProcessingStrategyListener listener);
 
     void changeToParentDirectory() throws IOException;
 
     void changeWorkingDirectory(String folderName) throws IOException;
     void connect() throws IOException;
 
     void createFolderInWorkingDirectory(String folderName) throws IOException;
 
     void deleteFileInWorkingDirectory(String fileName) throws IOException;
 
     void deleteFolderInWorkingDirectory(String folderName) throws IOException;
 
     void disconnect() throws IOException;
 
     String getDestinationId();
 
    Collection<? extends LightFileSystemObject> getDirectoriesInWorkingDirectory() throws FileNotFoundException, CancellationRequestedException;
 
     char getDirectorySeparator();
 
     Collection<? extends LightFileSystemObject> getFilesInWorkingDirectory() throws FileNotFoundException, CancellationRequestedException;
 
     String getInitialPath();
 
     int getServerTimeOffset() throws IOException;
 
     String getWorkingDirectory();
 
     // TODO: Document this method
     String getWorkingDirectoryRelativeToInitialPath(char separator);
 
     void moveFile(String fromAbsolute, String toAbsolute) throws IOException;
 
     void removeFileProcessingStrategyListener(FileProcessingStrategyListener listener);
 
     //void processFile(File source, File rootFolder, String target);
 
     //void processFolder(Folder folder);
 
     void uploadFileToWorkingDirectory(String absolutePath) throws IOException;
 }
