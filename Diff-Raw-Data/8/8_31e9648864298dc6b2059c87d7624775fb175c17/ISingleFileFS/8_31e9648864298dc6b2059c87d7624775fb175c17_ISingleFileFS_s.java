 package org.fs;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 /**
  * Virtual file system which stores all data (including metadata) inside single "real" file.
  * The primary benefit for this type of file system is that it is centralized and easy to remove.
  * <p/>
  * This is more a prototype, rather that production implementation due to several limitations:
  * <ul>
  * <li>limited number of files you can keep in this file system (all metadata is stored in a single chunk)</li>
  * <li>flushMetadata() operation which is required after adds or deletes</li>
  * <li>no support for directories</li>
  * </ul>
  *
  * @author Yury Litvinov
  */
 public interface ISingleFileFS {
 
     /**
      * Get output stream to submit file content.
      *
      * @param fileName file name
      * @return returns output stream where client should write file content.
      *         Output should be closed at the end to let file become readable and deletable.
      * @throws IOException if file already exists
      */
     OutputStream writeFile(String fileName) throws IOException;
 
     /**
      * Return content input stream for the given file.
      *
      * @param fileName file name
     * @return Input stream which should be closed at the end of readed to let file become deletable.
      * @throws IOException if file does not exist or file is currently being written.
      */
     InputStream readFile(String fileName) throws IOException;
 
     /**
     * Delete given file from file system
      *
      * @param fileName file name
      * @throws IOException if file does not exist or file is currently being read or written.
      */
     void deleteFile(String fileName) throws IOException;
 
     /**
      * Saves all changes made to metadata (file creation, file deletion) to disk.
     * Without flushing metadata is not saved and you won't see created file, as well as deleted file might me corrupted.
      *
      * @throws IOException if some io error occurs during flushing
      */
     void flushMetadata() throws IOException;
 }
