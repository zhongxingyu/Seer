 package com.test;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.file.FileAlreadyExistsException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 /**
  * Provides access to tree structure of File System.
  * @see FATFile
  *
  * All [transact-safe] methods have the [ts_] prefix.
  * The [transact-safe] means that methods can terminated successfully,
  *   or restore the object state to initial condition and throw exception,
  *   or mark system as "dirty" and throw exception (critical error in host FS).
  *
  * [wl_] prefix means "write-lock" - mandatory external write lock
  * [rl_] prefix means "read-lock" - mandatory external read lock
  *
  * All public functions have to be [transact-safe] by default.
  *
  * The most locks over the folder operation are syncronouse.
  * Any folder opertaion is limited in time.
  */
 
 public class FATFolder {
     static final String ROOT_NAME = "ROOT";
 
     // to check long in call params
     public static final long EMPTY_FILE_SIZE = 0L;
 
     // SIZE HINT POINT
     // FS guaranty, that deleted record less then a half.
     private int deletedCount = 0;
     // set this flag on folder delete
     private boolean packForbidden = false;
 
     final FATFile fatFile;
 
     //PERFORMANCE HINT POINT
     //use collections adapted for critical operations
     ArrayList<Integer> childFiles = new ArrayList<>();
     //unique index
     HashMap<String, Integer> childNames = new HashMap<>();
 
 
     /**
      * Creates new file
      *
      * @param fileName the name for created file
      * @param fileType the file type (FATFile.TYPE_XXXX const)
      * @return created file
      * @throws IOException
      */
     private FATFile createFile(String fileName, int fileType) throws IOException {
         FATLock lock = fatFile.getLockInternal(true);
         try {
             if (findFile(fileName) != null)
                 throw new FileAlreadyExistsException(fileName);
 
             // reserve space first!
             ts_wl_reserveRecord();
 
             // [access] is the same as in parent by default
             FATFile file = new FATFile(
                     this,
                     fileName,
                     fileType,
                     EMPTY_FILE_SIZE,
                     fatFile.access());
 
             ts_wl_ref(file); //dirty inside
             return file;
         } finally {
             lock.unlock();
         }
     }
 
     public FATFolder createFolder(String folderName) throws IOException {
         return createFile(folderName, FATFile.TYPE_FOLDER).getFolder();
     }
 
     public FATFile createFile(String fileName) throws IOException {
         return createFile(fileName, FATFile.TYPE_FILE);
     }
 
     public FATFile[] listFiles() throws IOException {
         FATLock lock = fatFile.getLockInternal(false);
         try {
             // PERFORMANCE HINT POINT
             // make it better!
             ArrayList<FATFile> _childFiles = new ArrayList<>();
             for (Integer currentId : childFiles) if (currentId != FATFile.INVALID_FILE_ID) {
                 _childFiles.add(ts_rl_getFile(currentId));
             }
             return _childFiles.toArray(new FATFile[_childFiles.size()]);
         } finally {
             lock.unlock();
         }
     }
 
     private FATFile ts_rl_getFile(int fileId) throws IOException {
         int index = childFiles.indexOf(fileId);
         if (index < 0)
             throw new FileNotFoundException("fileId: " + fileId);
         FATFile ret = ts_fs().ts_getFileFromCache(fileId);
         if (ret == null) {
             boolean success = false;
             try (FATFileChannel folderContent = fatFile.getChannelInternal(false, false)) {
                 ByteBuffer fileRecord = ts_fs().ts_allocateBuffer(FATFile.RECORD_SIZE);
                 int wasRead = folderContent
                         .position(index * FATFile.RECORD_SIZE)
                         .read(fileRecord);
                 if (wasRead == FATFile.RECORD_SIZE) {
                     fileRecord.flip();
                     int _fileId = fileRecord.getInt();
                     if (_fileId != fileId)
                         throw new IOException("Wrong state of folder:" + _fileId + "!=" + fileId);
                     int type = fileRecord.getInt();
                     ret = new FATFile(ts_fs(), fatFile, fileId, type);
                     ret.ts_initFromBuffer(fileRecord);
                     // commit
                     success = true;
                 }
             } finally {
                 if (!success) {
                     //primitive rollback - cannot restore (not [ts_] function call in action).
                     ts_fs().ts_setDirtyState("Cannot read folder record", false);
                 }
             }
         }
         return ret;
     }
 
     /**
      * Deletes all children, terminates on the first locked file
      *
      * Deletes all child files and folders.
      * To delete a folder the {@link #cascadeDelete()} function is called.
      *
      * @throws IOException
      * @throws FATFileLockedException
      */
     public void deleteChildren() throws IOException {
         FATLock lock = fatFile.tryLockThrowInternal(true);
         try {
             try {
                 packForbidden = true;
                 for (Integer currentId : childFiles) {
                     if (currentId != FATFile.INVALID_FILE_ID) {
                         FATFile current = ts_rl_getFile(currentId);
                         if (current.isFolder())
                             current.getFolder().cascadeDelete();
                         else
                             current.delete();
                     }
                 }
             } finally {
                 packForbidden = false;
                 ts_wl_optionalPack();
             }
         } finally {
             lock.unlock();
         }
     }
 
     /**
      * Cascade folder delete, terminates on the first locked file
      *
      * Delete process stops on the first error. No rollback.
      * Can be called for [root]: that removes children and throw
      * an exception while root file delete.
      *
      * @throws IOException
      * @throws FATFileLockedException
      */
     public void cascadeDelete() throws IOException {
         FATLock lock = fatFile.tryLockThrowInternal(true);
         try {
             deleteChildren();
             //PERFORMANCE HINT: make it better!
             pack(); //need: fatFile.delete() checks 0 size
             // commit
             fatFile.delete();
         } finally {
             lock.unlock();
         }
     }
 
     /**
      * Packs the folder in external memory.
      *
      * @return the number of bytes that were free.
      * @throws IOException
      */
     public int pack() throws IOException {
         FATLock lock = fatFile.getLockInternal(true);
         try {
             // PERFORMANCE HINT POINT
             // make it better!
             int startSize = childFiles.size();
             ArrayList<Integer> _childFiles = new ArrayList<>();
             for (Integer currentId : childFiles) {
                 if (currentId != FATFile.INVALID_FILE_ID) {
                     _childFiles.add(currentId);
                 }
             }
             _childFiles.trimToSize();
 
             int endSize = _childFiles.size();
             if (startSize != endSize) {
                 childFiles = _childFiles;
 
                 ArrayList<FATFile> childFATFiles = new ArrayList<>();
                 for (Integer currentId : childFiles) {
                     childFATFiles.add(ts_rl_getFile(currentId));
                 }
                 deletedCount = 0;
 
                 ts_wl_writeContent(childFATFiles);
                 //commit transaction
             }
             return startSize - endSize;
         } finally {
             lock.unlock();
         }
     }
 
     /***
      * Finds the file with selected name in folder collection.
      *
      * FUNCTIONAL HINT POINT: [folderName] as regexp
      * FUNCTIONAL HINT POINT: Hash map collection for fast Unique test.
      *
      * @param fileName the exact name to find, case sensitive.
      * @return the found file or [null].
      * @throws IOException
      */
     public FATFile findFile(String fileName) throws IOException {
         if (fileName.length() > FATFile.FILE_MAX_NAME)
             throw new IOException("Name is too long.");
 
         FATLock lock = fatFile.getLockInternal(false);
         try {
             if (fileName == null)
                 return null;
             Integer fileId = childNames.get(fileName);
             return  (fileId != null)
                 ? ts_rl_getFile(fileId)
                 : null;
         } finally {
             lock.unlock();
         }
     }
 
     /***
      * Get the file by name
      *
      * @param fileName the exact name to find, case sensitive.
      * @return the found file or throws FileNotFoundException.
      * @throws IOException
      */
     public FATFile getChildFile(String fileName) throws IOException {
         FATLock lock = fatFile.getLockInternal(false);
         try {
             if (fileName == null)
                 throw new IllegalArgumentException();
 
             FATFile ret = findFile(fileName);
             if (ret != null)
                 return ret;
             throw new FileNotFoundException(fileName);
         } finally {
             lock.unlock();
         }
     }
 
     public String getView() throws IOException {
         FATLock lock = fatFile.getLockInternal(false);
         try {
             StringBuilder sb = new StringBuilder();
             if (fatFile.isRoot())
                 sb.append("<?xml version=\"1.0\"?>");
             sb.append("<folder name=\"");
             sb.append(fatFile.getName());
             sb.append("\" size=\"");
             sb.append(fatFile.length());
             sb.append("\" created=\"");
             sb.append(fatFile.timeCreate());
             sb.append("\" lastModified=\"");
             sb.append(fatFile.lastModified());
             sb.append("\">\n");
             byte[] bcontext = new byte[16];
             ByteBuffer content = ByteBuffer.wrap(bcontext);
             for (Integer currentId : childFiles) {
                 if (currentId != FATFile.INVALID_FILE_ID) {
                     FATFile current = ts_rl_getFile(currentId);
                     switch (current.getType()) {
                         case FATFile.TYPE_FILE:
                             sb.append("<file name=\"");
                             sb.append(current.getName());
                             sb.append("\" size=\"");
                             sb.append(current.length());
                             sb.append("\" created=\"");
                             sb.append(current.timeCreate());
                             sb.append("\" lastModified=\"");
                             sb.append(current.lastModified());
                             sb.append("\">");
                             Arrays.fill(bcontext, (byte)' ');
                             content.clear();
                             try (FATFileChannel fc = current.getChannelInternal(false, true)) {
                                 fc.read(content);
                             }
                             sb.append(new String(bcontext));
                             sb.append("</file>\n");
                             break;
                         case FATFile.TYPE_DELETED:
                             sb.append("<deleted/>\n");
                             break;
                         case FATFile.TYPE_FOLDER:
                             sb.append(current.getFolder().getView());
                             break;
                     }
                 }
             }
             sb.append("</folder>\n");
             return sb.toString();
         } finally {
             lock.unlock();
         }
     }
 
     /***
      * Get the folder by name
      *
      * @param folderName the exact name to find, case sensitive.
      * @return the found folder or throws FileNotFoundException.
      * @throws IOException
      */
     public FATFolder getChildFolder(String folderName) throws IOException {
         FATLock lock = fatFile.getLockInternal(false);
         try {
             FATFile file = getChildFile(folderName);
             if (file.isFolder())
                 return file.getFolder();
             throw new IOException("File is not a folder:" + folderName);
         } finally {
             lock.unlock();
         }
     }
 
 
     /**
      * Creates Folder from File.
      *
      * Could not be called directly, use fs cache instead.
      *
      * @param file the folder storage
      * @throws IOException
      */
     FATFolder(FATFile file) throws IOException {
         this.fatFile = file;
         ts_fs().addFolder(this, new SelfDisposer(ts_fs(), ts_getFolderId()));
     }
 
 
     /**
      * Creates root folder on empty storage.
      *
      * @param fs the File System to mount in.
      * @param access the desired access
      * @return the Root Folder object
      * @throws IOException
      */
     static FATFolder ts_createRoot(FATFileSystem fs, int access) throws IOException {
         // exclusive access to [ret]
         boolean success = false;
         try {
             FATFile rootFile = fs.ts_createRootFile(access);
             if (rootFile.ts_getFileId() != FATFile.ROOT_FILE_ID)
                 throw new IOException("Root already exists.");
             FATFolder ret = rootFile.getFolder();
             // commit
             success = true;
             return ret;
         } finally {
             if (!success) {
                 // primitive rollback
                 fs.ts_setDirtyState("Cannot create the root folder.", false);
             }
         }
     }
     
     /**
      * Opens existent root folder.
      *
      * @param fs the File System to read from.
      * @return the Root Folder object
      * @throws IOException
      */
     static FATFolder ts_openRoot(FATFileSystem fs) throws IOException {
         // exclusive access to [ret]
         boolean success = false;
         try {
             FATFile rootFile = fs.ts_openRootFile();
             if (rootFile.ts_getFileId() != FATFile.ROOT_FILE_ID
                 || !ROOT_NAME.equals(rootFile.getName()))
             {
                 throw new IOException("Root folder is damaged!");
             }
             FATFolder ret = rootFile.getFolder();
             success = true;
             return ret;
         } finally {
             if (!success) {
                 // primitive rollback
                 fs.ts_setDirtyState("Cannot open the root folder.", false);
             }
         }
     }
 
     void ts_rl_readContent() throws IOException {
         boolean success = false;
         try {
             try (FATFileChannel folderContent = fatFile.getChannelInternal(false, false)) {
                 ByteBuffer bf = ts_fs().ts_allocateBuffer(FATFile.RECORD_SIZE);
                 long storageSize = fatFile.length();
                 while (folderContent.position() < storageSize) {
                     folderContent.read(bf);
                     bf.flip();
 
                     //read fileId
                     int fileId = bf.getInt();
                     childFiles.add(fileId);
 
                     if (fileId != FATFile.INVALID_FILE_ID) {
                         //read file name
                         bf.position(FATFile.RECORD_NAME_OFFSET);
                         char[] name = new char[FATFile.FILE_MAX_NAME];
                         bf.asCharBuffer().get(name);
                         childNames.put(FATFile.unlockedGetName(name), fileId);
                     }
                     bf.position(0);
                 }
                 if (folderContent.position() != storageSize)
                     throw new IOException("Folder is damaged!");
                 success = true;
             }
         } finally {
             if (!success) {
                 //primitive rollback - cannot restore.
                 ts_fs().ts_setDirtyState("Cannot read folder content", false);
             }
         }
     }
 
     private void ts_wl_writeContent(ArrayList<FATFile> childFATFiles) throws IOException {
         boolean success = false;
         try {
             try (FATFileChannel folderContent = fatFile.getChannelInternal(false, true)) {
                 ByteBuffer bf = ts_fs().ts_allocateBuffer(FATFile.RECORD_SIZE);
                 for (FATFile file : childFATFiles) {
                     bf.position(0);
                     file.ts_serialize(bf, ts_fs().getVersion());
                     bf.flip();
                     folderContent.write(bf);
                 }
                 //update size in parent
                 fatFile.setLengthInternal(folderContent.position());
                 fatFile.updateLastModified();
                 success = true;
             }
         } finally {
             if (!success) {
                 //primitive rollback - cannot restore.
                 ts_fs().ts_setDirtyState("Cannot write folder content", false);
             }
         }
     }
 
     /**
      * Updates the [index] element in folder storage.
      *
      * @param index
      * @param updateFile
      * @throws IOException
      */
     private void ts_wl_updateFileRecord(int index, FATFile updateFile) throws IOException {
         boolean success = false;
         try (FATFileChannel folderContent = fatFile.getChannelInternal(false, true)) {
             int wasWritten = folderContent
                 .position(index * FATFile.RECORD_SIZE)
                 .write(
                         (ByteBuffer) updateFile
                                 .ts_serialize(
                                         ts_fs().ts_allocateBuffer(FATFile.RECORD_SIZE),
                                         ts_fs().getVersion())
                                 .flip());
             // commit
            success = (wasWritten == FATFile.RECORD_SIZE);
         } finally {
            if (!success) {
                 //primitive rollback - cannot restore (not [ts_] function call in action).
                ts_fs().ts_setDirtyState("Cannot read folder record", false);
             }
         }
     }
 
     void ts_updateFileRecord(FATFile updateFile) throws IOException {
         FATLock lock = fatFile.getLockInternal(true);
         try {
             int index = childFiles.indexOf(updateFile.ts_getFileId());
             if (index == -1)
                 throw new IOException("Cannot update file attributes: Child not found.");
             ts_wl_updateFileRecord(index, updateFile);
         } finally {
             lock.unlock();
         }
     }
 
     void ts_wl_reserveRecord() throws IOException {
         int pos = childFiles.indexOf(FATFile.INVALID_FILE_ID);
         if (pos < 0) {
             childFiles.add(FATFile.INVALID_FILE_ID);
             ts_wl_updateFileRecord(childFiles.size() - 1, FATFile.DELETED_FILE);
             ++deletedCount;
         }
     }
 
     void ts_wl_ref(FATFile addFile) throws IOException {
         int pos = childFiles.indexOf(FATFile.INVALID_FILE_ID);
         int fileId = addFile.ts_getFileId();
         if (pos >= 0) {
             childFiles.set(pos, fileId);
             --deletedCount;
         } else {
             //{debug
             System.err.println("Unreserved Allocation! Exclusive mode only!");
             //}debug
             childFiles.add(fileId);
             pos = childFiles.size() - 1;
         }
         childNames.put(addFile.getName(), fileId);
         ts_wl_updateFileRecord(pos, addFile);
     }
 
     void ts_deRef(FATFile removeFile) throws IOException {
         FATLock lock = fatFile.tryLockThrowInternal(true);
         try {
             int offset = childFiles.indexOf(removeFile.ts_getFileId());
             if (offset == -1)
                 throw new IOException("Cannot remove file from folder: Child not found.");
             childNames.remove(removeFile.getName());
             childFiles.set(offset, FATFile.INVALID_FILE_ID);
             ++deletedCount;
             ts_wl_updateFileRecord(offset, FATFile.DELETED_FILE);
             ts_wl_optionalPack();
         } finally {
             lock.unlock();
         }
     }
 
     public void ts_renameChild(FATFile renameFile, String newFileName) throws IOException {
         FATLock lock = fatFile.tryLockThrowInternal(true);
         try {
             if (findFile(newFileName) != null)
                 throw new FileAlreadyExistsException(newFileName);
             childNames.remove(renameFile.getName());
             childNames.put(newFileName, renameFile.ts_getFileId());
             renameFile.initName(newFileName);
             ts_updateFileRecord(renameFile);
         } finally {
             lock.unlock();
         }
     }
 
 
     private void ts_wl_optionalPack() throws IOException {
         //SIZE HINT POINT
         //compact folder
         if (deletedCount > (childFiles.size() >> 1) && !packForbidden)
             pack();
     }
 
     int ts_getFolderId() {
         return fatFile.ts_getFileId();
     }
 
     private FATFileSystem ts_fs() {
         return fatFile.fs;
     }
 
     public FATFile asFile() {
         return fatFile;
     }
 
     private static class SelfDisposer implements FATDisposerRecord {
         private final FATFileSystem fs;        
         private final int folderId;
         SelfDisposer(FATFileSystem fs, int folderId) {
             this.fs = fs;
             this.folderId = folderId;
         }
         @Override 
         public void dispose() {
             fs.disposeFolder(folderId);
         }
     }
 
     @Override
     public String toString() {
         return fatFile.toString() + ",[" + super.toString() + "]";
     }
 
 }
