 package ro.finsiel.eunis.admin;
 
 import java.util.List;
 import java.util.Properties;
 import java.io.File;
 import java.io.BufferedOutputStream;
 import java.io.FileOutputStream;
 
 /**
  * Different utilities used to access the filesystem from the server.
  * @author finsiel
  */
 public class FileUtils {
 
   /**
    * This method lists the content of a directory (files & directories).
    * @param directory Directory to be listed.
    * @return Array of File with directories and files from the specified directory.
    */
   public static File[] listDirContent(File directory) {
     try {
       if (directory.isDirectory())
         return directory.listFiles();
       return new File[0];
     } catch (Exception ex) {
       ex.printStackTrace();
       return new File[0];
     }
   }
 
   /**
    * Remove all the files given as parameters from the filesystem.
    * @param files Files to be deleted.
    */
   public static void deleteFiles(File[] files) {
     if (null == files || files.length <= 0) return;
     for (int i = 0; i < files.length; i++) {
       File file = files[i];
       if (null != file) {
         if (file.exists() && file.isFile() && file.canWrite()) file.delete();
       }
     }
   }
 
   /**
    * Find the real name of a file (extract from a path the name of the file).
    * It seems that if web browser is a Windows browser, it sends the whole path (c:\xxx\yyy\zzz\filename.ext) to the
    * server which waits only for filenames (filename.ext) and being a linux cannot extract correctly the filename
    * using file.getName() - Which does theoretically exact what this method does.
    * @param filename Name of the file (w/ or without path)
    * @return The filename. This method checks to see if filename param contains '\' or '/' character. If it find, then
    * trims the path and returns filename. If it doesn't find, then returns filename...
    */
   public static String getRealName(String filename) {
     String ret = "temp_";
     if (-1 != filename.lastIndexOf("\\")) {
       // Client machine is a Windows with path separator \
       ret = filename.substring(filename.lastIndexOf("\\") + 1, filename.length());
     } else if (-1 != filename.lastIndexOf("/")) {
       // Client machine is a *NIX with path separator /
       ret = filename.substring(filename.lastIndexOf("/") + 1, filename.length());
     } else {
       ret = filename;
     }
     return ret;
   }
 
   /**
   * Write debug information withing $INSTANCE_HOME//temp.
    * @param text Data to be written.
    * @param append Append to existing file or create and write into a new file.
    * @return True if operation was successfully.
    */
   public static boolean dumpToFile(String text, boolean append) {
     boolean ret = true;
     try {
       Properties p = ro.finsiel.eunis.OSEnvironment.getEnvVars();
      String BASE_FILENAME = p.getProperty("INSTANCE_HOME") + "/temp/";
       //System.out.println("BASE_FILENAME+dump.txt=" + BASE_FILENAME+ "dump.txt");
       File file = new File(BASE_FILENAME + "dump.txt");
       /** Basic I/O data stream constructed around the file object */
       BufferedOutputStream fileStream = new BufferedOutputStream(new FileOutputStream(file, append));
       fileStream.write(text.getBytes());
       fileStream.close();
     } catch (Exception _ex) {
       _ex.printStackTrace(System.err);
       ret = false;
     }
     return ret;
   }
}
