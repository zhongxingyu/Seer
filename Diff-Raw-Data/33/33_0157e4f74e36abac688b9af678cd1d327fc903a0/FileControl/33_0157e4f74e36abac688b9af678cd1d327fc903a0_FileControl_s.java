 package org.jitu.wagtail;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.http.util.CharArrayBuffer;
 
 import android.annotation.SuppressLint;
 import android.os.Environment;
 
 public class FileControl {
     private File currentFile;
     private Exception error;
 
     public String readFile(File file) {
         try {
             URL url = file.toURI().toURL();
             URLConnection conn = url.openConnection();
             InputStream is = conn.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr);
             int nread;
             char[] chunk = new char[8192];
             CharArrayBuffer buf = new CharArrayBuffer(8192);
             while ((nread = br.read(chunk, 0, chunk.length)) != -1) {
                 buf.append(chunk, 0, nread);
             }
             br.close();
             currentFile = file;
             return buf.toString();
         } catch (IOException e) {
             error = e;
             currentFile = null;
             return null;
         }
     }
 
     public File getCurrentFile() {
         return currentFile;
     }
 
     public String getAbsolutePath() {
         if (currentFile == null) {
             return getHomePath() + File.separator + getDefaultName();
         }
         return currentFile.getAbsolutePath();
     }
 
     public String getHomePath() {
         File storage = Environment.getExternalStorageDirectory();
         File home = new File(storage, "notes");
         if (home.exists()) {
             return home.getAbsolutePath();
         } else {
             return storage.getAbsolutePath();
         }
     }
 
     @SuppressLint("SimpleDateFormat")
     private String getDefaultName() {
         SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
         String name = fmt.format(new Date());
         return name + ".txt";
     }
 
     public boolean saveFile(File file, String text) {
         try {
             FileWriter wf = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(wf);
             bw.write(text, 0, text.length());
             bw.close();
             currentFile = file;
             return true;
         } catch (IOException e) {
             error = e;
             return false;
         }
     }
 
     public void newFile() {
         currentFile = null;
     }
 
     public String getErrorMessage() {
         if (error == null) {
             return "";
         }
         return error.getMessage();
     }
 }
