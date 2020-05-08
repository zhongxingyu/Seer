 /*
  * JCloudApp - Easy access to CloudApp (tm) - cross-platform
  *
  * Copyright 2011 Christian Nicolai <chrnicolai@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * See the NOTICE file distributed along with this work for further
  * information.
  */
 
 package de.jcloudapp;
 
 import com.cloudapp.rest.CloudApi;
 import com.cloudapp.rest.CloudApiException;
 import com.cloudapp.rest.CloudAppInputStream;
 import java.awt.AWTException;
 import java.awt.Dialog;
 import java.awt.FileDialog;
 import java.awt.Image;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.Rectangle;
 import java.awt.Robot;
 import java.awt.SystemTray;
 import java.awt.Toolkit;
 import java.awt.TrayIcon;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.swing.JOptionPane;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * @author Ch. Nicolai
  */
 public class Main {
 
     private static final SimpleDateFormat df = (SimpleDateFormat) SimpleDateFormat.getInstance();
     
     static {
         df.applyPattern("yyyyMMdd-HHmmss");
     }
     
     public static void main(String[] args) throws Exception {
         if(args.length != 2) {
             showErrorDialog("Username and password should be given as command line arguments!\n"
                 + "Please add them in the 'run-classes' start script for your OS.");
             System.exit(1);
         }
         new Main(args[0], args[1]);
     }
     
     private CloudApi client;
     private TrayIcon icon;
     private boolean working = false;
     
     private Image imNormal;
     private Image imWorking;
     
     public Main(String user, String pwd) {
         client = new CloudApi(user, pwd);
         
         try {
             client.getItems(1, 1, null, false);
         } catch(CloudApiException ex) {
             showErrorDialog("Login incorrect!");
             exit();
         }
         
         if(!SystemTray.isSupported()) {
             showErrorDialog("SystemTray is unsupported!");
             exit();
         }
         
         try {
             // borrowed from https://github.com/cmur2/gloudapp
             imNormal = ImageIO.read(Main.class.getResourceAsStream("gloudapp.png"));
             imWorking = ImageIO.read(Main.class.getResourceAsStream("gloudapp_working.png"));
         } catch(IOException ex) {
             showErrorDialog("Could not load image!\n"+ex);
             exit();
         }
         
         icon = new TrayIcon(imNormal);
         icon.setImageAutoSize(true);
         icon.setToolTip("JCloudApp");
         icon.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if(!working) { working = true; doScreen(); working = false; }
             }
         });
         
         MenuItem screen = new MenuItem("Take screenshot");
         screen.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if(!working) { working = true; doScreen(); working = false; }
             }
         });
         
         MenuItem uploadClip = new MenuItem("Upload from clipboard");
         uploadClip.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if(!working) { working = true; doUploadClip(); working = false; }
             }
         });
         
         MenuItem upload = new MenuItem("Upload...");
         upload.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if(!working) { working = true; doUpload(); working = false; }
             }
         });
         
         MenuItem about = new MenuItem("About");
         about.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) { doAbout(); }
         });
         
         MenuItem quit = new MenuItem("Quit");
         quit.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) { doQuit(); }
         });
         
         PopupMenu popupMenu = new PopupMenu();
         popupMenu.add(screen);
         popupMenu.add(uploadClip);
         popupMenu.add(upload);
         popupMenu.add(about);
         popupMenu.addSeparator();
         popupMenu.add(quit);
         icon.setPopupMenu(popupMenu);
 
         try {
             SystemTray.getSystemTray().add(icon);
         } catch(AWTException ex) {
             showErrorDialog("No SystemTray found!\n"+ex);
             exit();
         }
     }
     
     public void doScreen() {
         System.out.println("Taking screenshot...");
         BufferedImage bi = takeScreenshot();
         try {
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ImageIO.write(bi, "png", baos);
             baos.close();
             byte[] image = baos.toByteArray();
             ByteArrayInputStream bais = new ByteArrayInputStream(image);
             String filename = String.format("Screenshot %s.png", df.format(new Date()));
             
             setImageWorking();
             JSONObject drop = client.uploadFile(new CloudAppInputStream(bais, "image/png", filename, image.length));
             String url = getDropUrl(drop);
             System.out.println("Upload complete, URL:\n"+url);
             setClipboard(url);
             setImageNormal();
             icon.displayMessage("Upload finished", String.format("Item: %s", filename), TrayIcon.MessageType.INFO);
         } catch(IOException ex) {
             System.out.println(ex);
         } catch(CloudApiException ex) {
             icon.displayMessage("Upload failed", ex.toString(), TrayIcon.MessageType.ERROR);
         }
     }
     
     public void doUploadClip() {
         Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
         Transferable t = cb.getContents(null);
         List<File> data = null;
         try {
             data = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
         } catch(UnsupportedFlavorException ex) {
             return;
         } catch(IOException ex) {
             return;
         }
         if(data != null && data.size() > 0) {
             setImageWorking();
             ArrayList<String> urls = new ArrayList<String>();
             for(File f : data) {
                 JSONObject drop = upload(f);
                 // cancel all uploads on error
                 if(drop == null) { return; }
                 String url = getDropUrl(drop);
                 System.out.println("Upload complete, URL:\n"+url);
                 urls.add(url);
             }
             int n = urls.size();
             String text = urls.remove(0);
             for(String s : urls) { text += " "+s; }
             setClipboard(text);
             String msg;
             if(n == 1) {
                 msg = String.format("Item: %s", data.get(0).getName());
             } else {
                 msg = String.format("%d Items: %s", n, data.get(0).getName());
                 int nchars = msg.length();
                 for(int i = 1; i < data.size(); i++) {
                     if(nchars + data.get(i).getName().length() > 140) {
                         msg += ", ...";
                         break;
                     } else {
                         msg += ", "+data.get(i).getName();
                         nchars += data.get(i).getName().length();
                     }
                 }
             }
             setImageNormal();
             icon.displayMessage("Upload finished", msg, TrayIcon.MessageType.INFO);
         }
     }
     
     public void doUpload() {
         FileDialog dlg = new FileDialog((Dialog)null, "Upload...");
         dlg.setVisible(true);
        if(dlg.getDirectory() == null || dlg.getFile() == null) {
            return;
        }
        File f = new File(dlg.getDirectory()+File.separator+dlg.getFile());
         if(f.exists()) {
             setImageWorking();
             JSONObject drop = upload(f);
             if(drop == null) { return; }
             String url = getDropUrl(drop);
             System.out.println("Upload complete, URL:\n"+url);
             setClipboard(url);
             setImageNormal();
             icon.displayMessage("Upload finished", String.format("Item: %s", f.getName()), TrayIcon.MessageType.INFO);
         }
     }
     
     public void doAbout() {
         String msg = "JCloudApp (C) 2011 Christian Nicolai\n\n"
         + "Easy uploading of screenshots and files to CloudApp (tm) - cross-plattform.";
         JOptionPane.showMessageDialog(null, msg, "About", JOptionPane.INFORMATION_MESSAGE);
     }
     
     public void doQuit() {
         exit();
     }
     
     private BufferedImage takeScreenshot() {
         try {
             Robot robot = new Robot();
             Rectangle captureSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
             return robot.createScreenCapture(captureSize);
         } catch(AWTException ex) {
             System.out.println(ex);
         }
         return null;
     }
     
     private JSONObject upload(File file) {
         try {
             return client.uploadFile(file);
         } catch(CloudApiException ex) {
             icon.displayMessage("Upload failed", ex.toString(), TrayIcon.MessageType.ERROR);
         }
         return null;
     }
     
     private void setClipboard(String s) {
         Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
         cb.setContents(new StringSelection(s), null);
     }
     
     private void setImageNormal() {
         icon.setImage(imNormal);
     }
     
     private void setImageWorking() {
         icon.setImage(imWorking);
     }
     
     private void exit() {
         System.exit(0);
     }
     
     // statics
     
     private static void showErrorDialog(String msg) {
         JOptionPane.showMessageDialog(null, msg, "JCloudApp - Error", JOptionPane.ERROR_MESSAGE);
     }
     
     private static String getDropUrl(JSONObject drop) {
         try {
             return drop.getString("url");
         } catch(JSONException ex) {
             System.out.println(ex);
         }
         return null;
     }
     
     private static void debug(JSONObject o) {
         try {
             System.out.println(o.toString(2));
         } catch(JSONException ex) {
             System.out.println(ex);
         }
     }
 }
