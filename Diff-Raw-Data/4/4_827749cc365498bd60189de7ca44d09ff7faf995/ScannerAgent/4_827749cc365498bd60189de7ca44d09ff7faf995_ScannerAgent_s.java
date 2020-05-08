 /**
  * Apache License 2.0
  */
 package com.googlecode.mad_schuelerturnier.business.scanner;
 
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 import org.apache.xerces.impl.dv.util.Base64;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Component;
 
 import java.io.*;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * mad letzte aenderung: $Date: 2012-01-11 22:50:30 +0100 (Mi, 11 Jan 2012) $
  *
  * @author $Author: marthaler.worb@gmail.com $
  * @version $Revision: 158 $
  * @headurl $HeadURL: https://mad-schuelerturnier.googlecode.com/svn/trunk/mad_schuelereturnier/src/main/java/com/googlecode/mad_schuelerturnier/business/controller/out/OutToWebsitePublisher.java $
  */
   @Component
 public class ScannerAgent {
 
     private final static Logger LOG = Logger.getLogger(ScannerAgent.class);
 
     private boolean geradeamGehen = false;
 
     private String imageUrl = "192.168.1.107";
 
 
     private boolean running = true;
 
     private boolean init = false;
 
     private boolean deleteGarbage = false;
 
 
 
 
     private int wartefaktor = 7;
 
     private  String path = "/";
     private  String schirizettel = "/";
 
     public void init(String path) {
         this.schirizettel = path  +  "schirizettel";
         this.path = path + "scannbilder";
         new File(this.path).mkdirs();
         LOG.info("scannbilder erstellt: " + path);
         this.init = true;
     }
 
     public void reset(){
         try {
             this.geradeamGehen = true;
             openConnection("http://"+imageUrl+"/decoder_control.cgi?command=31");
             Thread.sleep(wartefaktor * 1000 * 2);
             this.geradeamGehen = false;
         } catch (Exception e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     @Scheduled(fixedDelay = 60000)
     private void trigger(){
         if(! init || ! running){
             return;
         }
     }
 
 
     public void scann(){
 
 
         if( this.geradeamGehen){
             return;
         }
 
         this.geradeamGehen = true;
 
         List<String> files = new ArrayList<String>() ;
 
         try {
 
             openConnection("http://" + imageUrl + "/decoder_control.cgi?command=33");
 
         Thread.sleep(wartefaktor * 1000);
             files.add(saveImage());
             openConnection("http://"+imageUrl+"/decoder_control.cgi?command=35");
     Thread.sleep(wartefaktor * 1000);
             files.add(saveImage());
             openConnection("http://"+imageUrl+"/decoder_control.cgi?command=37");
     Thread.sleep(wartefaktor * 1000);
             files.add(saveImage());
             openConnection("http://"+imageUrl+"/decoder_control.cgi?command=39");
     Thread.sleep(wartefaktor * 1000);
 
             openConnection("http://"+imageUrl+"/decoder_control.cgi?command=31");
         } catch (Exception e) {
             LOG.error(e.getMessage(),e);
         }
 
 
        for(String file : files){
 
            GryscaleConverter.convertToGray(file,file);
           Cropper.crop(file,file,0, 180, 590 , 260);
            String res = BarcodeDecoder.decode(file );
 
            if(res.length() == 2){
                try {
                    FileUtils.moveFile(new File(file), new File(schirizettel + System.getProperty("file.separator") + res.toLowerCase() + ".png"));
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
 
            if(this.deleteGarbage){
             FileUtils.deleteQuietly(new File(file));
            }
 
            this.geradeamGehen = false;
        }
 
     }
 
     public String saveImage() throws IOException {
 
         String imageUrlN = "http://"+imageUrl+"/snapshot.cgi";
 
         String destinationFile = path + System.getProperty("file.separator") +System.currentTimeMillis() + ".png";
 
         new File(destinationFile).createNewFile();
 
         URL url = new URL(imageUrlN);
         URLConnection uc = url.openConnection();
         String userpass = "admin" + ":" + "";
         String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
         uc.setRequestProperty ("Authorization", basicAuth);
 
         InputStream is = uc.getInputStream();
         OutputStream os = new FileOutputStream(destinationFile);
 
         byte[] b = new byte[2048];
         int length;
 
         while ((length = is.read(b)) != -1) {
             os.write(b, 0, length);
         }
 
         is.close();
         os.close();
         return destinationFile;
     }
 
     public void openConnection(String imageUrl) throws IOException {
         URL url = new URL(imageUrl);
         URLConnection uc = url.openConnection();
         String userpass = "admin" + ":" + "";
         String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
         uc.setRequestProperty ("Authorization", basicAuth);
         InputStream is = uc.getInputStream();
         is.close();
 
     }
 
     public boolean isDeleteGarbage() {
         return deleteGarbage;
     }
 
     public void setDeleteGarbage(boolean deleteGarbage) {
         this.deleteGarbage = deleteGarbage;
     }
 
     public int getWartefaktor() {
         return wartefaktor;
     }
 
     public void setWartefaktor(int wartefaktor) {
         this.wartefaktor = wartefaktor;
     }
 
     public String getImageUrl() {
         return imageUrl;
     }
 
     public void setImageUrl(String imageUrl) {
         this.imageUrl = imageUrl;
     }
 
     public boolean isRunning() {
         return running;
     }
 
     public void setRunning(boolean running) {
         this.running = running;
     }
 
 }
