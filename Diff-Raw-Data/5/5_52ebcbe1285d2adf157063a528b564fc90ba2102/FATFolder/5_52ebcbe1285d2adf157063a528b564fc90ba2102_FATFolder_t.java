 package com.test;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.file.FileAlreadyExistsException;
 import java.util.ArrayList;
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: uta
  *
  * All [transact-safe] methods have the [ts_] prefix.
  * The [transact-safe] means that methods can terminated successfully,
  *   or restore the object state to initial condition and throw exception,
  *   or mark system as "dirty" and throw exception (critical error in host FS).
  *
  * All public functions have to be [transact-safe] by default.
  */
 
 public class FATFolder {
    private static final String ROOT_NAME = "ROOT";
 
     // to check long in call params
     private static final long EMPTY_FILE_SIZE = 0L;
 
     // SIZE HINT POINT
     // FS guaranty, that deleted record less then a half.
     private int deletedCount = 0;
     // set this flag on folder delete
     private boolean packForbidden = false;
 
     final FATFile fatFile;
 
     //PERFORMANCE HINT POINT
     //use collections adapted for critical operations
     ArrayList<FATFile> childFiles = new ArrayList<>();
 
     /**
      * Creates new file
      *
      * @param fileName the name for created file
      * @param fileType the file type (FATFile.TYPE_XXXX const)
      * @return created file
      * @throws IOException
      */
     private FATFile createFile(String fileName, int fileType) throws IOException {
         synchronized (fatFile) {
             fatFile.checkValid();
             ts_fs().begin(true);
             try {
                 if (findFile(fileName) != null)
                     throw new FileAlreadyExistsException(fileName);
 
                 // reserve space first!
                 ts_reserveRecord();
 
                 // [access] is the same as in parent by default
                 FATFile file = ts_fs().ts_createFile(ts_getFolderId(),
                         fileName,
                         fileType, EMPTY_FILE_SIZE, fatFile.access());
 
                 boolean success = false;
                 try {
                     ts_ref(file);
                     // commit
                     success = true;
                 } finally {
                     if (!success) {
                         ts_fs().ts_setDirtyState("Cannot save file record to reserved space.", false);
                     }
                 }
                 return file;
             } finally {
                 ts_fs().end();
             }
         }
     }
 
     public FATFolder createFolder(String folderName) throws IOException {
         return createFile(folderName, FATFile.TYPE_FOLDER).getFolder();
     }
 
     public FATFile createFile(String fileName) throws IOException {
         return createFile(fileName, FATFile.TYPE_FILE);
     }
 
     public FATFile[] listFiles() throws IOException {
         synchronized (fatFile) {
             fatFile.checkValid();
             // PERFORMANCE HINT POINT
             // make it better!
             ArrayList<FATFile> _childFiles = new ArrayList<>();
             for (FATFile current : childFiles) {
                 if (current != FATFile.DELETED_FILE) {
                     _childFiles.add(current);
                 }
             }
             return _childFiles.toArray(new FATFile[_childFiles.size()]);
         }
     }
 
     /**
      * Deletes all children.
      *
      * Deletes all child files and folders.
      * To delete a folder the {@link #cascadeDelete()} function is called.
      *
      * @throws IOException
      */
     public void deleteChildren() throws IOException {
         synchronized (fatFile) {
             fatFile.checkValid();
             boolean success = false;
             ts_fs().begin(true);
             try {
                 packForbidden = true;
                 for (FATFile current : childFiles) {
                     if (current != FATFile.DELETED_FILE) {
                         if (current.isFolder())
                             current.getFolder().cascadeDelete();
                         else
                             current.delete();
                     }
                 }
                 // commit
                 success = true;
             } finally {
                 packForbidden = false;
                 // partial delete
                 if (!success)
                     ts_optionalPack();
                 ts_fs().end();
             }
         }
     }
 
     /**
      * Cascade folder delete.
      *
      * Delete process stops on the first error. No rollback.
      * Can be called for [root]: that removes children and throw
      * an exception while root file delete.
      *
      * @throws IOException
      */
     public void cascadeDelete() throws IOException {
         synchronized (fatFile) {
             fatFile.checkValid();
             ts_fs().begin(true);
             try {
                 deleteChildren();
                 //PERFORMANCE HINT: make it better!
                 pack();
                 // commit
                 fatFile.delete();
             } finally {
                 ts_fs().end();
             }
         }
     }
 
     /**
      * Packs the folder in external memory.
      *
      * @return the number of bytes that were free.
      * @throws IOException
      */
     public int pack() throws IOException {
         synchronized (fatFile) {
             fatFile.checkValid();
 
             // PERFORMANCE HINT POINT
             // make it better!
             int startSize = childFiles.size();
             ArrayList<FATFile> _childFiles = new ArrayList<>();
             for (FATFile current : childFiles) {
                 if (current != FATFile.DELETED_FILE) {
                     _childFiles.add(current);
                 }
             }
             _childFiles.trimToSize();
 
             int endSize = _childFiles.size();
             if (startSize != endSize) {
                 //write first, truncate after!
                 ts_fs().begin(true);
                 try {
                     childFiles = _childFiles;
                     deletedCount = 0;
                     ts_writeContent();
                     //commit transaction
                 } finally {
                     ts_fs().end();
                 }
             }
             return startSize - endSize;
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
         synchronized (fatFile) {
             fatFile.checkValid();
             if (fileName == null)
                 return null;
             for (FATFile file : childFiles) {
                 if (fileName.equals(file.getName()))
                     return file;
             }
         }
         return null;
     }
 
     /***
      * Get the file by name
      *
      * @param fileName the exact name to find, case sensitive.
      * @return the found file or throws FileNotFoundException.
      * @throws IOException
      */
     public FATFile getChildFile(String fileName) throws IOException {
         synchronized (fatFile) {
             fatFile.checkValid();
             if (fileName == null)
                 throw new IllegalArgumentException();
             for (FATFile file : childFiles) {
                 if (fileName.equals(file.getName()))
                     return file;
             }
         }
         throw new FileNotFoundException(fileName);
     }
 
     public String getView() throws IOException {
         synchronized (fatFile) {
             ts_fs().begin(false);
             try {
                 StringBuilder sb = new StringBuilder();
                 sb.append("<folder name=\"");
                 sb.append(fatFile.getName());
                 sb.append("\">");
                 for (FATFile current : childFiles) {
                     switch (current.getType()) {
                         case FATFile.TYPE_FILE:
                             sb.append("<file name=\"");
                             sb.append(current.getName());
                             sb.append("\"/>");
                             break;
                         case FATFile.TYPE_DELETED:
                             sb.append("<deleted/>");
                             break;
                         case FATFile.TYPE_FOLDER:
                             sb.append(current.getFolder().getView());
                             break;
                     }
                 }
                sb.append("<\\folder>");
                 return sb.toString();
             } finally {
                 ts_fs().end();
             }
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
         synchronized (fatFile) {
             FATFile file = getChildFile(folderName);
             if (file.isFolder())
                 return file.getFolder();
             throw new IOException("File is not a folder:" + folderName);
         }
     }
 
 
     /**
      * Creates Folder from File.
      *
      * Could not be called directly, use [ts_fs().ts_getFolder] instead.
      * [ts_] constructor with [dirty] rollback
      *
      * @param fatFile the folder storage
      * @throws IOException
      */
     FATFolder(FATFile fatFile) throws IOException {
         this.fatFile = fatFile;
         ts_readContent();
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
             FATFile rootFile = fs.ts_createFile(FATFile.ROOT_FILE_ID,
                     ROOT_NAME, FATFile.TYPE_FOLDER, EMPTY_FILE_SIZE, access);
             if (rootFile.ts_getFileId() != FATFile.ROOT_FILE_ID)
                 throw new IOException("Root already exists.");
             FATFolder ret = fs.ts_getFolder(rootFile.ts_getFileId());
             // update record in header
             rootFile.setLastModified(FATFileSystem.getCurrentTime());
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
             FATFile rootFile = fs.ts_openFile(fs.getRootInfo(), FATFile.ROOT_FILE_ID);
             if (rootFile.ts_getFileId() != FATFile.ROOT_FILE_ID
                 || !ROOT_NAME.equals(rootFile.toString()))
             {
                 throw new IOException("Root folder is damaged!");
             }
 
             FATFolder ret = fs.ts_getFolder(rootFile.ts_getFileId());
             //ret.ts_readContent();
             success = true;
             return ret;
         } finally {
             if (!success) {
                 // primitive rollback
                 fs.ts_setDirtyState("Cannot open the root folder.", false);
             }
         }
     }
 
     private void ts_readContent() throws IOException {
         boolean success = false;
         synchronized (fatFile) {
             ts_fs().begin(false);
             try {
                 try (FATFileChannel folderContent = fatFile.getChannel(false)) {
                     ByteBuffer bf = ts_fs().ts_allocateBuffer(FATFile.RECORD_SIZE);
                     long storageSize = fatFile.length();
                     while (folderContent.position() < storageSize) {
                         folderContent.read(bf);
                         bf.flip();
                         childFiles.add(ts_fs().ts_openFile(bf, ts_getFolderId()));
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
                 ts_fs().end();
             }
         }
     }
 
     private void ts_writeContent() throws IOException {
         boolean success = false;
         synchronized (fatFile) {
             ts_fs().begin(true);
             try {
                 try (FATFileChannel folderContent = fatFile.getChannel(false)) {
                     ByteBuffer bf = ts_fs().ts_allocateBuffer(FATFile.RECORD_SIZE);
                     for (FATFile file : childFiles) {
                         bf.position(0);
                         file.ts_serialize(bf, ts_fs().getVersion());
                         bf.flip();
                         folderContent.write(bf);
                     }
                     //update size in parent
                     fatFile.setLength(folderContent.position());
                     success = true;
                 }
             } finally {
                 if (!success) {
                     //primitive rollback - cannot restore.
                     ts_fs().ts_setDirtyState("Cannot write folder content", false);
                 }
                 ts_fs().end();
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
     private void ts_updateFileRecord(int index, FATFile updateFile) throws IOException {
         boolean success = false;
         try (FATFileChannel folderContent = fatFile.getChannel(false)) {
             folderContent
                 .position(index * FATFile.RECORD_SIZE)
                 .write(
                     (ByteBuffer) updateFile
                         .ts_serialize(
                             ts_fs().ts_allocateBuffer(FATFile.RECORD_SIZE),
                             ts_fs().getVersion())
                         .flip());
             // commit
             success = true;
         } finally {
             if (!success) {
                 //primitive rollback - cannot restore (not [ts_] function call in action).
                 ts_fs().ts_setDirtyState("Cannot read folder record", false);
             }
         }
     }
 
     private void ts_updateRootFileRecord(FATFile rootFile) throws IOException {
         boolean success = false;
         try {
             ByteBuffer store = rootFile
                 .ts_serialize(
                         ts_fs().ts_allocateBuffer(FATFile.RECORD_SIZE),
                         ts_fs().getVersion());
             store.flip();
             ts_fs().updateRootRecord(store);
             // commit
             success = true;
         } finally {
             if (!success) {
                 //primitive rollback - cannot restore (not [ts_] function call in action).
                 ts_fs().ts_setDirtyState("Cannot update root folder record", false);
             }
         }
     }
 
     void ts_updateFileRecord(FATFile updateFile) throws IOException {
         synchronized (fatFile) {
             fatFile.checkValid();
             if (updateFile.isRoot()) {
                 ts_updateRootFileRecord(updateFile);
             } else {
                 int index = childFiles.indexOf(updateFile);
                 if (index == -1)
                     throw new IOException("Cannot update file attributes");
                 ts_updateFileRecord(index, updateFile);
             }
         }
     }
 
     void ts_reserveRecord() throws IOException {
         synchronized (fatFile) {
             int pos = childFiles.indexOf(FATFile.DELETED_FILE);
             if (pos < 0) {
                 childFiles.add(FATFile.DELETED_FILE);
                 ts_updateFileRecord(childFiles.size() - 1, FATFile.DELETED_FILE);
                 ++deletedCount;
             }
         }
     }
 
     void ts_ref(FATFile addFile) throws IOException {
         synchronized (fatFile) {
             fatFile.checkValid();
             int pos = childFiles.indexOf(FATFile.DELETED_FILE);
             if (pos >= 0) {
                 childFiles.set(pos, addFile);
                 --deletedCount;
             } else {
                 System.err.println("Unreserved Allocation! Exclusive mode only!");
                 childFiles.add(addFile);
                 pos = childFiles.size() - 1;
             }
             ts_updateFileRecord(pos, addFile);
         }
     }
 
     void ts_deRef(FATFile removeFile) throws IOException {
         synchronized (fatFile) {
             fatFile.checkValid();
             int offset = childFiles.indexOf(removeFile);
             if (offset == -1)
                 throw new IOException("Cannot remove file from folder.");
             childFiles.set(offset, FATFile.DELETED_FILE);
             ++deletedCount;
             ts_updateFileRecord(offset, FATFile.DELETED_FILE);
             ts_optionalPack();
         }
     }
 
     private void ts_optionalPack() throws IOException {
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
 }
