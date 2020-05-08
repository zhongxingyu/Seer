 /**
  * ******************************************************************************************
  * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
  *       promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.common;
 
 import com.sun.istack.ByteArrayDataSource;
 import com.sun.pdfview.PDFFile;
 import com.sun.pdfview.PDFPage;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.text.DecimalFormat;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import javax.activation.DataHandler;
 import javax.activation.FileDataSource;
 import javax.swing.ImageIcon;
 import net.lingala.zip4j.core.ZipFile;
 import net.lingala.zip4j.exception.ZipException;
 import net.lingala.zip4j.model.FileHeader;
 import net.lingala.zip4j.model.ZipParameters;
 import net.lingala.zip4j.util.Zip4jConstants;
 import org.apache.sanselan.Sanselan;
 import org.jvnet.staxex.StreamingDataHandler;
 import org.sola.common.messaging.ClientMessage;
 import org.sola.common.messaging.MessageUtility;
 import org.sola.common.messaging.ServiceMessage;
 
 /**
  * Provides static methods to manage various aspects related to the files.
  *
  * The FileUtility also maintains a cache of documents and will automatically
  * purge old files from the cache if the cache exceeds its maximum size (default
  * max size is 200Mb).
  */
 public class FileUtility {
 
     public final static String csv = "csv";
     private static long maxCacheSizeBytes = 200 * 1024 * 1024;
     private static long resizedCacheSizeBytes = 120 * 1024 * 1024;
     private static int minNumberCachedFiles = 10;
     private static long maxFileSizeBytes = 100 * 1024 * 1024;
     private static String cachePath = System.getProperty("user.home") + "/sola/cache/documents/";
 
     /**
      * Checks the cache to ensure it won't exceed the max size cache size. If
      * the new document will cause the cache to exceed the max size, the older
      * documents in the cache are deleted until the cache reaches the resize
      * limit.
      *
      * @param cache The directory for the documents cache
      * @param newFileSize The size of the new file to open in bytes.
      */
     private static void maintainCache(File cache, long newFileSize) {
         long cacheSize = getDirectorySize(cache, false);
         cacheSize += newFileSize;
         if (cacheSize > maxCacheSizeBytes) {
             System.out.println("Resizing SOLA documents cache.");
             // The cache has exceeded its max size. Delete the oldest files in the cache based
             // on thier last modified date. 
             List<File> files = Arrays.asList(cache.listFiles());
             Collections.sort(files, new Comparator<File>() {
                 @Override
                 public int compare(File f1, File f2) {
                     return (f1.lastModified() > f2.lastModified() ? 1
                             : (f1.lastModified() == f2.lastModified() ? 0 : -1));
                 }
             });
 
             int numFiles = files.size();
             for (File f : files) {
                 if (numFiles < minNumberCachedFiles) {
                     break;
                 }
                 // Only delete files - ignore subdirectories. 
                 if (f.isFile()) {
                     cacheSize = cacheSize - f.length();
                     f.delete();
                     if (cacheSize < resizedCacheSizeBytes) {
                         break;
                     }
                 }
                 numFiles--;
             }
         }
     }
 
     /**
      * Sets the minimum number of files that should be left in the cache when it
      * is being resized. Default is 10.
      */
     public static void setMinNumberCachedFiles(int num) {
         minNumberCachedFiles = num;
     }
 
     /**
      * The target size of the cache in bytes after a resize/maintenance is
      * performed. Default is 120MB.
      *
      * @param sizeInBytes The target size of the cache in bytes.
      */
     public static void setResizedCacheSizeBytes(long sizeInBytes) {
         resizedCacheSizeBytes = sizeInBytes;
     }
 
     /**
      * The maximum size of the cache in bytes. Default is 200MB
      *
      * @param sizeInBytes The maximum size of the cache in bytes.
      */
     public static void setMaxCacheSizeBytes(long sizeInBytes) {
         maxCacheSizeBytes = sizeInBytes;
     }
 
     /**
      * The maximum size of a file (in bytes) that can be loaded into SOLA.
      * Default is 100MB.
      *
      * <p>SOLA uses a file streaming service to upload and download files to and
      * from the client. The file streaming service streams files directly to
      * disk and does not store them in memory allowing the SOLA client
      * application to potentially handle files of any size. However, be aware
      * that files must be completely loaded into memory by the Digital Archive
      * Service before they can be saved to the SOLA database. Increasing this
      * value from its default may require adjusting the memory settings for the
      * SOLA domain on the SOLA Glassfish Server. </p>
      *
      * @param sizeInBytes The maximum size of the file in bytes.
      */
     public static void setMaxFileSizeBytes(long sizeInBytes) {
         maxFileSizeBytes = sizeInBytes;
     }
 
     /**
      * Sets the path to use for the documents cache
      *
      * @param newCachePath The new cache path.
      */
     public static void setCachePath(String newCachePath) {
         if (newCachePath != null) {
             cachePath = newCachePath;
         }
     }
 
     /**
      * Returns the absolute file path for the documents cache directory.
      */
     public static String getCachePath() {
         File cache = new File(cachePath);
         if (!cache.exists()) {
             // Need to create the file cache directory. 
             cache.mkdirs();
         }
         return cachePath;
     }
 
     /**
      * Returns true if the file to check is already in the documents cache. Note
      * that the document name should include the rowVersion number to ensure any
      * documents that get updated also get reloaded in the cache.
      *
      * @param tmpFileName The name of the file to check in the documents cache.
      */
     public static boolean isCached(String tmpFileName) {
         tmpFileName = sanitizeFileName(tmpFileName, true);
         File file = new File(getCachePath() + File.separator + tmpFileName);
         return file.exists();
     }
 
     /**
      * Returns the byte array for the file. The default maximum size of a file
      * to load is 100MB. This can be modified using
      * {@linkplain #setMaxFileSizeBytes(long)}
      *
      * @param filePath The full path to the file
      */
     public static byte[] getFileBinary(String filePath) {
         File file = new File(filePath);
         if (!file.exists()) {
             return null;
         }
         if (file.length() > maxFileSizeBytes) {
             DecimalFormat df = new DecimalFormat("#,###.#");
             String maxFileSizeMB = df.format(maxFileSizeBytes / (1024 * 1024));
             String fileSizeMB = df.format(file.length() / (1024 * 1024));
             throw new SOLAException(ServiceMessage.EXCEPTION_FILE_TOO_BIG,
                     new String[]{fileSizeMB, maxFileSizeMB});
         }
         try {
             return readFile(file);
         } catch (IOException ex) {
             throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED_ERROR_DETAILS,
                     new String[]{"File could not be read", ex.getLocalizedMessage()});
         }
     }
 
     /**
      * Returns file's extention.
      *
      * @param fileName The name of the file.
      */
     public static String getFileExtension(String fileName) {
         String ext = null;
         if (fileName.lastIndexOf(".") > 0 && fileName.lastIndexOf(".") < fileName.length()) {
             ext = fileName.substring(fileName.lastIndexOf(".") + 1);
         }
         return ext;
     }
 
     /**
      * Returns file name excluding extention.
      *
      * @param fileName The name of the file.
      */
     public static String getFileNameWithoutExtension(String fileName) {
         String name = fileName;
         if (fileName.lastIndexOf(".") > 0 && fileName.lastIndexOf(".") < fileName.length()) {
             name = fileName.substring(0, fileName.lastIndexOf("."));
         }
         return name;
     }
 
     /*
      * Get the extension of a file.
      */
     public static String getFileExtension(File f) {
         String ext = null;
         String s = f.getName();
         int i = s.lastIndexOf('.');
 
         if (i > 0 && i < s.length() - 1) {
             ext = s.substring(i + 1).toLowerCase();
         }
         return ext;
     }
 
     /**
      * Returns the size of the directory. This is done by summing the size of
      * each file in the directory. The sizes of all subdirectories can be
      * optionally included.
      *
      * @param directory The directory to calculate the size for.
      */
     public static long getDirectorySize(File directory, boolean recursive) {
         long length = 0;
         if (!directory.isFile()) {
             for (File file : directory.listFiles()) {
                 if (file.isFile()) {
                     length += file.length();
                 } else {
                     if (recursive) {
                         length += getDirectorySize(file, recursive);
                     }
                 }
 
             }
         }
         return length;
     }
 
     /**
      * Opens the specified file from the documents cache. If the file does not
      * exist in the cache a File Open exception is thrown.
      *
      * @param tmpFileName The name of the file to open from the documents cache.
      */
     public static void openFile(String tmpFileName) {
         String fileName = sanitizeFileName(tmpFileName, true);
         openFile(new File(getCachePath() + File.separator + fileName));
     }
 
     /**
      * Creates a new file in the documents cache using the fileBinary data then
      * opens the file for display.
      *
      * @param fileBinary The binary content of the file to open.
      * @param fileName The name to use for creating the file. This name must
      * exclude any file path.
      */
     public static void openFile(byte[] fileBinary, String fileName) {
         File file = writeFileToCache(fileBinary, fileName);
         openFile(file);
     }
 
     /**
      * Opens the file from the documents cache using the Java Desktop.
      *
      * @param file The file to open
      * @throws SOLAException Failed to open file
      */
     public static void openFile(File file) {
         if (file == null) {
             return;
         }
         String fileName = file.getName();
         if (isExecutable(fileName)) {
             // Make sure the extension is changed before opening the file. 
             fileName = setTmpExtension(fileName);
             File nonExeFile = new File(getCachePath() + File.separator + fileName);
             file.renameTo(nonExeFile);
             file = nonExeFile;
         }
         // Try to open the file. Need to check if the current platform has Java Desktop support and 
         // if so, whether the OPEN action is also supported. 
         boolean fileOpened = false;
         if (Desktop.isDesktopSupported()) {
             Desktop dt = Desktop.getDesktop();
             if (dt.isSupported(Desktop.Action.OPEN)) {
                 try {
                     dt.open(file);
                     fileOpened = true;
                 } catch (Exception ex) {
                     // The file could not be opened. The most likely cause is there is no editor
                     // installed for the file extension, but it may be due to file security 
                     // restrictions. Either way, inform the user they should open the file manually. 
                     fileOpened = false;
                 }
             }
         }
         if (!fileOpened) {
             // The Java Desktop is not supported on this platform. Riase a mesage to 
             // tell the user they must manually open the document. 
             MessageUtility.displayMessage(ClientMessage.ERR_FAILED_OPEN_FILE,
                     new String[]{file.getAbsolutePath()});
         }
     }
 
     /**
      * Creates thumbnail image for the given file. Returns null if format is not
      * supported.
      *
      * @param filePath The full path to the file.
      * @param width Thumbnail width.
      * @param height Thumbnail height.
      */
     public static BufferedImage createImageThumbnail(String filePath, int width, int height) {
         try {
             File file = new File(filePath);
 
             if (!file.exists()) {
                 return null;
             }
 
             Image thumbnail = null;
             String fileExt = getFileExtension(filePath);
 
             if (fileExt.equalsIgnoreCase("jpg") || fileExt.equalsIgnoreCase("jpeg")) {
 
                 ImageIcon tmp = new ImageIcon(filePath);
                 if (tmp == null || tmp.getIconWidth() <= 0
                         || tmp.getIconHeight() <= 0) {
                     return null;
                 }
 
                 ImageIcon scaled = null;
 
                 if ((tmp.getIconWidth() > width && width > 0)
                         || (tmp.getIconHeight() > height && height > 0)) {
                     scaled = new ImageIcon(tmp.getImage().getScaledInstance(
                             width, height, Image.SCALE_SMOOTH));
                 } else {
                     scaled = tmp;
                 }
 
                 BufferedImage buffered = new BufferedImage(
                         scaled.getIconWidth(),
                         scaled.getIconHeight(),
                         BufferedImage.TYPE_INT_RGB);
                 Graphics2D g = buffered.createGraphics();
                 g.drawImage(scaled.getImage(), 0, 0, null);
                 g.dispose();
 
                 return buffered;
 
             } else {
 
                 if (fileExt.equalsIgnoreCase("pdf")) {
 
                     RandomAccessFile raf = new RandomAccessFile(file, "r");
                     FileChannel channel = raf.getChannel();
                     ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                     PDFFile pdffile = new PDFFile(buf);
 
                     // draw the first page to an image
                     PDFPage page = pdffile.getPage(0);
 
                     //generate the image
                     //#319 Improve quality of the image preview by ensuring the whole page
                     // is captured correctly post any rotation that may be required. 
                     // Use a multiple of 3 to increase the image depth for better image
                     // definition. 
                     thumbnail = page.getImage(
                             (int) page.getWidth() * 3,
                             (int) page.getHeight() * 3,
                             null, // null for the clip rectangle to ensure entire page is captured
                             null,
                             true, // fill background with white
                             true // block until drawing is done
                             );
 
                     buf.clear();
                     channel.close();
                     raf.close();
 
                 } else {
 
                     BufferedImage img = Sanselan.getBufferedImage(file);
                     thumbnail = Toolkit.getDefaultToolkit().createImage(img.getSource());
 
                 }
 
                 if (thumbnail == null || thumbnail.getWidth(null) <= 0
                         || thumbnail.getHeight(null) <= 0) {
                     return null;
                 }
 
                 Image scaled = null;
 
                 if ((thumbnail.getWidth(null) > width && width > 0)
                         || (thumbnail.getHeight(null) > height && height > 0)) {
                     scaled = thumbnail.getScaledInstance(
                             width, height, Image.SCALE_SMOOTH);
                 } else {
                     scaled = thumbnail;
                 }
 
                 BufferedImage buffered = new BufferedImage(
                         scaled.getWidth(null),
                         scaled.getHeight(null),
                         BufferedImage.TYPE_INT_RGB);
                 Graphics2D g = buffered.createGraphics();
                 g.drawImage(scaled, 0, 0, null);
                 g.dispose();
 
                 return buffered;
             }
 
         } catch (Exception e) {
             // Most likely the a thumbnail cannot be generated for the file type. Ignore the
             // exception and continue. 
             System.out.println("Unable to generate thumbnail - " + e.getMessage());
         }
         return null;
     }
 
     /**
      * Removes path separator characters (i.e. / and \) from the fileName. Used
      * to ensure user input does not redirect files to an unsafe locations. Also
      * replaces the extension for any file with an executable file extension
      * with .tmp if the replaceExtension parameter is true.
      *
      * @param fileName The fileName to sanitize.
      * @param replaceExtension If true, any executable extension on the file
      * will be replaced with .tmp
      * @see #isExecutable(java.lang.String)
      * @see #setTmpExtension(java.lang.String)
      */
     public static String sanitizeFileName(String fileName, boolean replaceExtension) {
         String result = fileName.replaceAll("\\\\|\\/", "#");
         if (isExecutable(result) && replaceExtension) {
             result = setTmpExtension(fileName);
         }
         return result;
     }
 
     /**
      * Checks if the file extension is considered to be an executable file
      * extension. Returns true if the extension is .exe, .msi, .bat, .cmd
      *
      * @param fileName The file name to check
      */
     public static boolean isExecutable(String fileName) {
         String extension = getFileExtension(fileName);
         boolean result = extension.equalsIgnoreCase("exe") || extension.equalsIgnoreCase("msi")
                 || extension.equalsIgnoreCase("bat") || extension.equalsIgnoreCase("cmd");
         return result;
     }
 
     /**
      * Replaces the file extension with tmp. Note that the original file
      * extension is retained as part of the file name. e.g. file.exe becomes
      * file_exe.tmp
      *
      * @param fileName The file name to check.
      */
     public static String setTmpExtension(String fileName) {
         fileName = fileName.replaceAll("\\.", "_");
         return fileName + ".tmp";
     }
 
     /**
      * Generates a default file name using a random GUID as the primary file
      * name value.
      *
      * @see #generateFileName(java.lang.String, int, java.lang.String)
      * generateFileName
      */
     public static String generateFileName() {
         return generateFileName(java.util.UUID.randomUUID().toString(), 0, "tmp");
     }
 
     /**
      * Creates a versioned file name based on the document information.
      *
      * @param fileNr The number assigned to the document
      * @param rowVersion The rowversion of the document.
      * @param extension The file extension of the document.
      * @see #generateFileName()
      */
     public static String generateFileName(String fileNr, int rowVersion, String extension) {
         if (fileNr == null || extension == null || fileNr.isEmpty() || extension.isEmpty()) {
             return generateFileName();
         }
         String fileName = String.format("sola_%s_%s.%s", fileNr, rowVersion, extension);
         return sanitizeFileName(fileName, true);
     }
 
     /**
      * Saves a data stream from a {@linkplain DataHandler} to the specified
      * file. Used to allow more efficient management of large file transfers
      * between the SOLA client(s) and the web services.
      *
      * <p>If the DataHandler is a {@linkplain StreamingDataHandler}, then the
      * {@linkplain StreamingDataHandler#moveTo(java.io.File)} method is used to
      * save the file to disk. Otherwise the InputStream from the DataHandler is
      * written to disk using
      * {@linkplain #writeFileToCache(java.io.InputStream, java.io.File) writeFileToCache}.</p>
      *
      * <p>Note that file streaming is not currently supported if Metro security
      * is used. Refer to http://java.net/jira/browse/WSIT-1081 for details.
      * Using Security also substantially increases the memory required to handle
      * large files with a practical limit around 15MB to 20MB.</p>
      *
      * @param dataHandler The dataHandler representing the file.
      * @param fileName The name of the file to write the DataHander stream to.
      * If null a random file name will be generated for the stream.
      * @return The file name used to save the file data. This may differ from
      * the fileName passed in if the fileName was null or it included invalid
      * characters (e.g. / \). Will return null if the dataHandler is null;
      */
     public static String saveFileFromStream(DataHandler dataHandler, String fileName) {
         if (dataHandler == null) {
             return null;
         }
         if (fileName == null) {
             fileName = generateFileName();
         } else {
             fileName = sanitizeFileName(fileName, true);
         }
         String filePathName = getCachePath() + File.separator + fileName;
         File file = new File(filePathName);
         deleteFile(file);
         try {
             if (dataHandler instanceof StreamingDataHandler) {
                 StreamingDataHandler sdh = null;
                 try {
                     sdh = (StreamingDataHandler) dataHandler;
                     sdh.moveTo(file);
                 } finally {
                     if (sdh != null) {
                         sdh.close();
                     }
                 }
             } else {
                 writeFile(dataHandler.getInputStream(), file);
             }
             maintainCache(new File(getCachePath()), 0);
         } catch (Exception ex) {
             throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED_ERROR_DETAILS,
                     new Object[]{"Saving file " + fileName, ex.getLocalizedMessage(), ex});
         }
         return fileName;
     }
 
     /**
      * Creates a {@linkplain DataHandler} for a file located on the local file
      * system. The file can be loaded from any accessible location.
      *
      * @param fileName The name of the file to create the DataHandler for. If
      * the file is in the cache, only the file name is required. If the file is
      * located elsewhere, the full file pathname is required.
      */
     public static DataHandler getFileAsStream(String filePathName) {
         File file = new File(getCachePath() + File.separator + filePathName);
         if (!file.exists()) {
             file = new File(filePathName);
         }
         DataHandler result = null;
         if (file.exists()) {
             result = new DataHandler(new FileDataSource(file));
         } else {
             throw new SOLAException(ClientMessage.ERR_FAILED_OPEN_FILE,
                     new String[]{filePathName});
         }
         return result;
     }
 
     /**
      * Creates a {@linkplain DataHandler} for a byte array representing the
      * content of a file. Also configures the MIME type to ensure the content is
      * correctly mapped as a DataHander.
      *
      * @param fileContent The byte array containing the file content.
      */
     public static DataHandler getFileAsStream(byte[] fileContent) {
         return new DataHandler(new ByteArrayDataSource(fileContent, "application/octet-stream"));
     }
 
     /**
      * Writes the file content to a file in the documents cache. The fileName is
      * sanitized before the new file is written. The new file name can be
      * obtained from the {@linkplain File#getName()} method.
      *
      * @param fileContent The content of the file to write to the file system
      * @param fileName The name to use for the new file. That file name may
      * change due to sanitization. If the fileName is null, a random file name
      * will be used.
      */
     public static File writeFileToCache(byte[] fileContent, String fileName) {
         if (fileContent == null) {
             return null;
         }
         if (fileName == null) {
             fileName = generateFileName();
         } else {
             fileName = sanitizeFileName(fileName, true);
         }
         File file = new File(getCachePath() + File.separator + fileName);
         try {
             // Check if the cache needs to have some documents purged
             maintainCache(new File(getCachePath()), fileContent.length);
             // Write the file to disk
             writeFile(new ByteArrayInputStream(fileContent), file);
         } catch (IOException iex) {
             Object[] lstParams = {fileName, iex.getLocalizedMessage()};
             throw new SOLAException(ClientMessage.ERR_FAILED_CREATE_NEW_FILE, lstParams);
         }
         return file;
     }
 
     /**
      * Reads a file in the documents cache into a byte array for further
      * processing.
      *
      * @param fileName THe name of the file to read. The fileName will be
      * sanitized.
      * @return The byte array representing the content of the file.
      */
     public static byte[] readFileFromCache(String fileName) {
         fileName = sanitizeFileName(fileName, true);
         File file = new File(getCachePath() + File.separator + fileName);
         try {
             return readFile(file);
         } catch (IOException ex) {
             throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED_ERROR_DETAILS,
                     new String[]{"File could not be read from cache", ex.getLocalizedMessage()});
         }
     }
 
     /**
      * Writes the data from an input stream to the specified file using buffered
      * 8KB chunks. This method closes the input stream once the write is
      * completed.
      *
      * @param in The InputStream to write
      * @param file The file to write the input stream to
      * @throws IOException If an IO error occurs while attempting to write the
      * file.
      */
     public static void writeFile(InputStream in, File file) throws IOException {
         if (file == null || in == null) {
             // Nothing to write
             return;
         }
         OutputStream out = null;
         try {
             deleteFile(file);
             file.setLastModified(DateUtility.now().getTime());
             out = new FileOutputStream(file);
             // Use an 8K buffer for writing the file. This is usually the most effecient 
             // buffer size. 
             byte[] buf = new byte[8 * 1024];
             int len;
             while ((len = in.read(buf)) != -1) {
                 out.write(buf, 0, len);
             }
             out.flush();
         } finally {
             if (in != null) {
                 in.close();
             }
             if (out != null) {
                 out.close();
             }
         }
     }
 
     /**
      * Reads a file from the file system into a byte array.
      *
      * @param file The file to read.
      * @return Byte array representing the file content. Returns null if the
      * file does not exist.
      * @throws IOException
      */
     public static byte[] readFile(File file) throws IOException {
         byte[] result = null;
         if (file != null && file.exists()) {
             FileInputStream in = new FileInputStream(file);
             try {
                 int length = (int) file.length();
                 result = new byte[length];
                 int offset = 0;
                 int bytesRead = 1;
                 // Attempt to slurp the entire file into the array in one go. Note that sometimes
                 // read will not return all of the data, so need to add a while loop to continue
                 // trying to read the reminaing bytes. If no bytes are read, exit the loop. 
                 while (offset < length && bytesRead > 0) {
                     bytesRead = in.read(result, offset, (length - offset));
                     offset = bytesRead > 0 ? (offset + bytesRead) : offset;
                 }
                 if (offset < length) {
                     throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED_ERROR_DETAILS,
                             new Object[]{"File could not be read", file.getName()});
                 }
             } finally {
                 in.close();
             }
         }
         return result;
     }
 
     /**
      * Deletes the file from the file system if it exists.
      *
      * @param file The file to delete.
      */
     public static void deleteFile(File file) {
         if (file != null && file.exists()) {
             file.delete();
         }
     }
 
     /**
      * Deletes the file from the documents cache if it exists.
      *
      * @param fileName The name of the file to remove from the cache.
      */
     public static void deleteFileFromCache(String fileName) {
         fileName = sanitizeFileName(fileName, true);
         File file = new File(getCachePath() + File.separator + fileName);
         deleteFile(file);
     }
 
     public static String compress(String fileName, String password) {
         fileName = sanitizeFileName(fileName, true);
         String inputFilePath = getCachePath() + File.separator + fileName;
         String zipFileName = getFileNameWithoutExtension(fileName) + ".zip";
         String outputFilePath = getCachePath() + File.separator + zipFileName;
         try {
             ZipFile zipFile = new ZipFile(outputFilePath);
             File inputFile = new File(inputFilePath);
             ZipParameters parameters = new ZipParameters();
             parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
             parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);
             parameters.setEncryptFiles(true);
             parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
             parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
             parameters.setPassword(password);
             zipFile.addFile(inputFile, parameters);
             return zipFileName;
         } catch (ZipException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     public static String uncompress(String fileName, String password) throws ZipException {
         fileName = sanitizeFileName(fileName, true);
         //Full path of the file that is compressed
         String inputFilePath = getCachePath() + File.separator + fileName;
         String destinationPath = getCachePath();
         ZipFile zipFile = new ZipFile(inputFilePath);
         if (zipFile.isEncrypted()) {
             zipFile.setPassword(password);
         }
         FileHeader fileHeader = (FileHeader) zipFile.getFileHeaders().get(0);
         String fileUncompressed = fileHeader.getFileName();
         zipFile.extractAll(destinationPath);
         return fileUncompressed;
     }
 }
