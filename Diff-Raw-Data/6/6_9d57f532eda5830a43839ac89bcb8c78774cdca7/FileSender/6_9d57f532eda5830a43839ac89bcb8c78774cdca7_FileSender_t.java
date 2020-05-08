 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pro.dmtn2.utilities;
 
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.swing.SwingWorker;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import pro.dmtn2.gui.GUI;
 
 /**
  *
  * @author Lalli
  */
 public class FileSender extends SwingWorker {
 
     private GUI GUI;
     private List<File> fileList;
     private boolean isText = false;
     private boolean oldFiles = false;
 
     public FileSender(GUI gui) {
         this.GUI = gui;
     }
 
     private void linkkiLeikepoydalle(String nimi) throws IOException {
 
         String linkki = "http://www.neekeri.com/up/" + nimi;
         Clipboard clbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
         StringSelection strsel = new StringSelection(linkki);
         clbrd.setContents(strsel, strsel);
         GUI.closeMouth();
        
 
     }
 
     public void setFileList(List<File> fileList) {
         this.fileList = fileList;
         this.oldFiles = true;
 
     }
 
     public void contentIsText(boolean isText) {
         this.isText = isText;
     }
 
     @Override
     public Object doInBackground() throws Exception {
         Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
         Transferable tran = clipboard.getContents(null);
         if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor) && fileList == null) {
             sendText();
         } else {
             sendFiles(tran);
         }
         return null;
     }
 
     private void sendFiles(Transferable tran) throws UnsupportedFlavorException, IOException {
         if (fileList == null) {     // If files are drag&dropped filelist will be filled at DragAndDropListener
 
             if (tran.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                 fileList = (List<File>) tran.getTransferData(DataFlavor.javaFileListFlavor);
             } else {
                 Date date = new Date();
                 String nimi = date.getTime() + "jpeg";
                 File tiedosto = new File(date.getTime() + ".jpeg");
 
                 BufferedImage kuvaa = (BufferedImage) tran.getTransferData(DataFlavor.imageFlavor);
                 ImageIO.write(kuvaa, "jpeg", tiedosto);
                 fileList = new ArrayList<File>();
                 fileList.add(tiedosto);
             }
         }
         try {
             for (File tiedosto : fileList) {    //sending files 1 by 1 to neekeri.com then deleting 
                 File file = tiedosto;
                 HttpClient httpclient = new DefaultHttpClient();
                 HttpPost httppost = new HttpPost("http://neekeri.com/upscript/uploadify.php");
                 MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                 FileBody bin = new FileBody(file);
                 reqEntity.addPart("Filename", new StringBody(file.getName()));
                reqEntity.addPart("fo8lder", new StringBody("/up"));
                 reqEntity.addPart("fileext", new StringBody("*.php;*.pdf;*.fla;*.dat;*.ass;*.jar;*.java;*.csv;*.bmp;*.jpg;*.jpeg;*.png;*.gif;*.swf;*.svg;*.txt;*.exe;*.wmv;*.avi;*.mp3;*.nfo;*.mpg;*.naf;*.zip;*.rar;*.7z;*.tif;*.psd;*.doc;*.docx;*.ppt;*.pptx;*.flv;*.mp4;*.mid;*.aac;*.tar.bz2;*.tar.gz;*.tar;*.tif;*.yuv;*.svg;*.db;*.nfo;*.bat;*.sav;*.ttf;*.dll;*.sys;*.deb;*.pkg;*.gz;*.iso;*.toast;*.dmg;*.bak;*.tmp;*.msi;*.torrent;*.flac;*.ogg;*.JPG,*.PNG,*.JPEG;*.TCL;*.tcl"));
                 reqEntity.addPart("Filedata", bin);
                 reqEntity.addPart("Upload", new StringBody("Submit Query"));
                 httppost.setEntity(reqEntity);
                 HttpResponse response = httpclient.execute(httppost);
                 HttpEntity resEntity = response.getEntity();
                 linkkiLeikepoydalle(file.getName());
             }
             if (!oldFiles) {  // Files are screenshots and will be removed
                 for (File file : fileList) {
                     file.delete();
                 }
             }
         } catch (IOException ex) {
             Logger.getLogger(FileSender.class.getName()).log(Level.SEVERE, null, ex);
         }
        fileList = null;
     }
 
     private void sendText() throws UnsupportedFlavorException, IOException {
         Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
         String text = (String) clipboard.getData(DataFlavor.stringFlavor);
         HttpClient httpclient = new DefaultHttpClient();
 
         HttpPost httppost = new HttpPost("http://hastebin.com/documents");
         httppost.addHeader("Content-Type", "application/json; charset=utf-8");
         StringEntity params = new StringEntity(text, "UTF-8");
         httppost.setEntity(params);
         HttpResponse response = httpclient.execute(httppost);
         BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
         String json = reader.readLine();
         StringSelection strsel = new StringSelection("http://hastebin.com/" + json.substring(8, 18));
         clipboard.setContents(strsel, strsel);
         GUI.closeMouth();
     }
 }
