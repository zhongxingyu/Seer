 package com.screenshot;
 
 import java.awt.Desktop;
 import java.awt.Toolkit;
 import java.awt.datatransfer.StringSelection;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.URI;
 import java.util.UUID;
 
 import com.screenshot.gui.ScreenshotApp;
 import com.screenshot.upload.FtpClient;
 import com.screenshot.upload.PicasaClient;
 import com.screenshot.util.Messenger;
 
 public class ScreenshotTaker implements ScreenshotListener {
 
     private Messenger messenger;
     private ScreenshotApp app;
 
     public ScreenshotTaker(Messenger messenger) {
         this.messenger = messenger;
     }
 
     public void start(){
         if (app != null) {
             app.close("Start new", true);
         }
         app = new ScreenshotApp(this, messenger);    
     }
 
     @Override
     public void screenshotSelected(BufferedImage img) {
         try {
             String url;
             if (Settings.getInstance().isPicasawebMode()) {
                 url = postToPicasaweb(img);
             } else {
                 url = sendToFtp(img);
             }
 
             if (url != null) {
                 Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(url), null);
                 try {
                     Desktop.getDesktop().browse(new URI(url));
                 } catch (Exception e) {
                     messenger.error("Can't open URL " + url + ". Error message: " + e.getMessage(), e);
                 }
             }
         } finally {
            app.close("snapshot is took", false);
         }
     }
 
     private String postToPicasaweb(BufferedImage img) {
         try {
             return new PicasaClient().upload(img);
         } catch (Exception e) {
             messenger.error("Can't send screenshot.\nReason: '" + e.getMessage() + "'.\nCheck google account settings or internet access.", e);
             return null;
         } catch (NoClassDefFoundError error){
             messenger.error("Can't send screenshot.\nReason: '" + error.getMessage() + "'.\nCheck google account settings or internet access.", error);
             return null;
         }
     }
 
     private String sendToFtp(BufferedImage img) {
         String fileName = UUID.randomUUID().toString() + ".png";
         String url = Settings.getInstance().getHttpBaseUrl() + "/" + fileName;
         try {
             new FtpClient().upload(fileName, img);
             return url;
         } catch (IOException e) {
             messenger.error("Can't send screenshot.\nReason: '" + e.getMessage() + "'.\nCheck FTP settings or internet connection.", e);
             return null;
         }
     }
 
     @Override
     public void screenshotCancelled() {
         app.close("screenshot is cancelled", false);
     }
 
 }
