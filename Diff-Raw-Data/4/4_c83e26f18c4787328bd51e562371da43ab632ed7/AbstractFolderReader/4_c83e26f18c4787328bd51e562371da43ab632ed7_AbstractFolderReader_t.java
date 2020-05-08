 package info.mikaelsvensson.ftpbackup.util;
 
 import info.mikaelsvensson.ftpbackup.model.FileExpression;
 import info.mikaelsvensson.ftpbackup.model.filesystem.FileSystemFile;
 import info.mikaelsvensson.ftpbackup.model.filesystem.FileSystemFolder;
 import info.mikaelsvensson.ftpbackup.model.filesystem.FileSystemRootFolder;
 
 import java.io.FileNotFoundException;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.LinkedList;
 
 public abstract class AbstractFolderReader implements FolderReader {
     private Collection<? extends FileExpression> inclusionExpressions;
 
     protected AbstractFolderReader(Collection<? extends FileExpression> exclusionExpressions, Collection<? extends FileExpression> inclusionExpressions) {
         this.exclusionExpressions = exclusionExpressions;
         this.inclusionExpressions = inclusionExpressions;
     }
 
     private Collection<? extends FileExpression> exclusionExpressions;
 
     @Override
     public FileSystemRootFolder getFileSystemTree(String rootFolderPath) throws FileNotFoundException {
        if (isFolder(rootFolderPath)) {
             FileSystemRootFolder rootFolder = new FileSystemRootFolder(rootFolderPath);
             fillFolder(rootFolder, rootFolder.getAbsolutePath());
             return rootFolder;
         } else {
             throw new FileNotFoundException("Folder '" + rootFolderPath + "' does not exist.");
         }
     }
 
     private final Collection<FolderReaderListener> eventListeners = new LinkedList<>();
 
     @Override
     public void addFolderReaderListener(FolderReaderListener listener) {
         eventListeners.add(listener);
     }
 
     @Override
     public void removeFolderReaderListener(FolderReaderListener listener) {
         eventListeners.remove(listener);
     }
 
     protected void fireFileExpressionMatchedEvent(final FolderReaderEvent event) {
         for (FolderReaderListener listener : eventListeners) {
             listener.onFileExpressionMatched(event);
         }
     }
 
     private void fillFolder(FileSystemFolder folder, final String sourceFolderAbsolutePath) {
         String rootFolder = folder.getRoot().getAbsolutePath();//job.getFileSet().getRootFolder();
 
         for (String path : listFilesAndFolders(sourceFolderAbsolutePath)) {
             String name = getName(path);
             Calendar modificationTime = getLastModificationTime(path);
             boolean isFolder = isFolder(path);// f.isDirectory();
             if (isFolder) {
                 FileSystemFolder folder1 = new FileSystemFolder(name, modificationTime);
                 folder.addChild(folder1);
                 fillFolder(folder1, path);
             } else {
                 if (isPathMatch(rootFolder, path)) {
                     long size = getSize(path);
                     folder.addChild(new FileSystemFile(name, modificationTime, size));
                 }
             }
         }
     }
 
     private boolean isPathMatch(final String rootAbsolutePath, final String testAbsolutePath) {
         // Include file if no specific inclusion expressions have been defined OR if the file matches one of the defined inclusion expressions
 //        Collection<? extends FileExpression> inclusionExpressions = job.getFileSet().getInclusionExpressions();
         boolean isInclExprSet = inclusionExpressions != null && inclusionExpressions.size() > 0;
         boolean isIncluded = !isInclExprSet || isFileMatch(inclusionExpressions, rootAbsolutePath, testAbsolutePath);
 
         // Exclude file if the file matches one of the defined inclusion expressions
 //        Collection<? extends FileExpression> exclusionExpressions = job.getFileSet().getExclusionExpressions();
         boolean isExclExprSet = exclusionExpressions != null && exclusionExpressions.size() > 0;
         boolean isExcluded = isExclExprSet && isFileMatch(exclusionExpressions, rootAbsolutePath, testAbsolutePath);
 
         boolean isMatch = isIncluded && !isExcluded;
 
         fireFileExpressionMatchedEvent(new FolderReaderEvent(this, isMatch, rootAbsolutePath, testAbsolutePath));
 
         return isMatch;
     }
 
     private <T extends FileExpression> T findFileExpression(String path, Collection<T> expressions) {
         for (T expression : expressions) {
             if (expression.accept(path)) {
                 return expression;
             }
         }
         return null;
     }
 
     private boolean isFileMatch(Collection<? extends FileExpression> expressions, final String rootAbsolutePath, final String fileAbsolutePath) {
         return findFileExpression(fileAbsolutePath.substring(rootAbsolutePath.length()), expressions) != null;
     }
 }
