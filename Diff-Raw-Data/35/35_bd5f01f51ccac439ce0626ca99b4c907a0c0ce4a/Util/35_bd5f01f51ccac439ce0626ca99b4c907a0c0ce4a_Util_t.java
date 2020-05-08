 package org.geworkbench.util;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Enumeration;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.Date;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 import javax.swing.ImageIcon;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class Util {
     static Log log = LogFactory.getLog(Util.class);
 
     public static ImageIcon createImageIcon(String path) {
         java.net.URL imgURL = Util.class.getResource(path);
         return new ImageIcon(imgURL);
     }
 
     static public boolean deleteDirectory(File path) {
         if (path.exists()) {
             File[] files = path.listFiles();
             for (int i = 0; i < files.length; i++) {
                 if (files[i].isDirectory()) {
                     deleteDirectory(files[i]);
                 } else {
                     files[i].delete();
                 }
             }
         }
         return (path.delete());
     }
 
     public static void unZip(String inFile, String outDir) throws IOException {
 
         ZipFile zipFile = new ZipFile(inFile);
 
         Enumeration<? extends ZipEntry> entries = zipFile.entries();
 
         while (entries.hasMoreElements()) {
             ZipEntry entry = (ZipEntry) entries.nextElement();
 
             if (entry.isDirectory()) {
                 // Assume directories are stored parents first then children.
                 log.trace("Extracting directory: " + entry.getName());
                 // Check for entry that contains multiple directories - must be created individually
                 String[] dirs = entry.getName().split("/");
                 String dirSoFar = "";
                 for (String thisDir : dirs) {
                     dirSoFar += "/" + thisDir;
                     File newdir = new File(outDir, dirSoFar);
                     log.trace("Creating directory "+dirSoFar);
                     newdir.mkdir();
                 }
             } else {
                 log.trace("Extracting file: " + entry.getName());
                 File file = new File(outDir, entry.getName());
                 File path = file.getParentFile();
                 // Make directory if needed
                 path.mkdir();
                 copyInputStream(zipFile.getInputStream(entry),
                         new BufferedOutputStream(new FileOutputStream(file)));
             }
         }
 
         zipFile.close();
     }
 
     private static void copyInputStream(InputStream in, OutputStream out) throws IOException {
         byte[] buffer = new byte[1024];
         int len;
 
         while ((len = in.read(buffer)) >= 0) out.write(buffer, 0, len);
 
         in.close();
         out.close();
     }
 
     /**
      * Generates a unique name given a desired name and a set of existing names.
      * Appends ' (n)' to the name for n = 1, 2, 3, ... until a unique name is found.
      * @param desiredName
      * @param existingNames
      * @return
      */
     public static String getUniqueName(String desiredName, Set<String> existingNames) {
         String name = desiredName;
         int i = 0;
         while (existingNames.contains(name)) {
             i++;
             name = desiredName + " (" + i + ")";
         }
         return name;
     }
 
     public static void copyFile(InputStream in, File dst) throws IOException {
         //InputStream in = new FileInputStream(src);
         OutputStream out = new FileOutputStream(dst);
 
         // Transfer bytes from in to out
         byte[] buf = new byte[1024];
         int len;
         while ((len = in.read(buf)) > 0) {
             out.write(buf, 0, len);
         }
         in.close();
         out.close();
     }
 
     public static boolean isRunningOnAMac() {
         String osName = System.getProperty("os.name");
         if (osName.startsWith("Mac OS")) {
             return true;
         }
         return false;
     }
 
     public static void centerWindow(Window window) {
         Toolkit toolkit = Toolkit.getDefaultToolkit();
         Dimension screenSize = toolkit.getScreenSize();
         Dimension winSize = window.getSize();
         int x = (screenSize.width - winSize.width) / 2;
         int y = (screenSize.height - winSize.height) / 2;
         window.setLocation(x, y);
     }
     
     /**
 	 * 
 	 * @param title
 	 * @param message
 	 * @return ProgressBar
 	 */
 	public static ProgressBar createProgressBar(String title, String message) {
 
 		ProgressBar pBar = createProgressBar(title);
 		pBar.setMessage(message);
 
 		return pBar;
 	}
 
 	/**
 	 * 
 	 * @param title
 	 * @return ProgressBar
 	 */
 	public static ProgressBar createProgressBar(String title) {
 		final ProgressBar pBar = ProgressBar
 				.create(ProgressBar.INDETERMINATE_TYPE);
 
 		pBar.setTitle(title);
 
 		centerWindow(pBar);
 
 		return pBar;
 	}
 
 	/**
      * Filters the string by removing those patterns that match the regex.
      */
     public static String filter(String s, String regex) {
         String[] tokens = s.split(regex);
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < tokens.length; i++) {
             sb.append(tokens[i]);
         }
         return sb.toString();
     }

     public static String formatDateStandard(Date date) {
         if (date == null) return "";
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         return df.format(date);
     }
 
 }
