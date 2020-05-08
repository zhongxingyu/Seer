 package misc;
 
 import com.google.common.io.ByteStreams;
 import com.google.common.io.Files;
 import com.google.common.io.InputSupplier;
 import com.google.common.io.OutputSupplier;
 import com.google.common.io.Resources;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Enumeration;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 class PhantomJsDownloader {
   private final boolean isWindows;
   private final boolean isMac;
 
   PhantomJsDownloader() {
    isWindows = System.getProperty("os.name").startsWith("Windows");
    isMac = System.getProperty("os.name").startsWith("Mac OS X");
   }
 
   public File downloadAndExtract() {
     File installDir = new File(new File(System.getProperty("user.home")), ".phantomjstest");
 
     String url;
     File phantomJsExe;
     if (isWindows) {
       url = "http://phantomjs.googlecode.com/files/phantomjs-1.8.1-windows.zip";
       phantomJsExe = new File(installDir, "phantomjs-1.8.1-windows/phantomjs.exe");
     } else if (isMac) {
       url = "http://phantomjs.googlecode.com/files/phantomjs-1.8.1-macosx.zip";
       phantomJsExe = new File(installDir, "phantomjs-1.8.1-macosx/bin/phantomjs");
     } else {
       url = "http://phantomjs.googlecode.com/files/phantomjs-1.8.1-linux-x86_64.tar.bz2";
       phantomJsExe = new File(installDir, "phantomjs-1.8.1-linux-x86_64/bin/phantomjs");
     }
 
     extractExe(url, installDir, phantomJsExe);
 
     return phantomJsExe;
   }
 
   private void extractExe(String url, File phantomInstallDir, File phantomJsExe) {
     if (phantomJsExe.exists()) {
       return;
     }
 
     File targetZip = new File(phantomInstallDir, "phantomjs.zip");
     downloadZip(url, targetZip);
 
     System.out.println("Extracting phantomjs");
     try {
       if (isWindows) {
         unzip(targetZip, phantomInstallDir);
       } else if (isMac) {
         new ProcessBuilder().command("/usr/bin/unzip", "-qo", "phantomjs.zip").directory(phantomInstallDir).start().waitFor();
       } else {
         new ProcessBuilder().command("/usr/bin/tar", "-xjvf", "phantomjs.zip").directory(phantomInstallDir).start().waitFor();
       }
     } catch (Exception e) {
       throw new IllegalStateException("Unable to unzip phantomjs from " + targetZip.getAbsolutePath());
     }
   }
 
   private void downloadZip(String url, File targetZip) {
     if (targetZip.exists()) {
       return;
     }
 
     System.out.println("Downloading phantomjs from " + url + "...");
 
     File zipTemp = new File(targetZip.getAbsolutePath() + ".temp");
     try {
       zipTemp.getParentFile().mkdirs();
 
       InputSupplier<InputStream> input = Resources.newInputStreamSupplier(URI.create(url).toURL());
       OutputSupplier<FileOutputStream> ouput = Files.newOutputStreamSupplier(zipTemp);
 
       ByteStreams.copy(input, ouput);
     } catch (IOException e) {
       throw new IllegalStateException("Unable to download phantomjs from " + url);
     }
 
     zipTemp.renameTo(targetZip);
   }
 
   private static void unzip(File zip, File toDir) throws IOException {
     final ZipFile zipFile = new ZipFile(zip);
     try {
       Enumeration<? extends ZipEntry> entries = zipFile.entries();
       while (entries.hasMoreElements()) {
         final ZipEntry entry = entries.nextElement();
         if (entry.isDirectory()) {
           continue;
         }
 
         File to = new File(toDir, entry.getName());
         to.getParentFile().mkdirs();
 
         Files.copy(new InputSupplier<InputStream>() {
           @Override
           public InputStream getInput() throws IOException {
             return zipFile.getInputStream(entry);
           }
         }, to);
       }
     } finally {
       zipFile.close();
     }
   }
 }
