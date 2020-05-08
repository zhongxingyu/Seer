 package net.chromiumupdater;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author morth, cfstras
  */
 public class ChromiumUpdater {
 
     protected static GUI gui;
     static Download download;
     public static String baseDLUrl = "http://commondatastorage.googleapis.com/chromium-browser-continuous/";
     public static String tempDir;
     public static String installDir;
     static Settings settings;
     public static URL dlurl;
     public static boolean win32 = false;
     public static boolean macosx = false;
 
     public static void main(String[] args) {
         if (System.getProperty("os.name").contains("Mac OS X")) {
             macosx = true;
            tempDir = "mactempdir"; //TODO find the mac temp folder
             installDir = "/Applications/Chromium.app/"; //"null" often leads to NullPointerExceptions.
         } else if (System.getProperty("os.name").contains("Windows")) {
             win32 = true;
             tempDir = System.getenv("TMP");
             installDir = System.getenv("PROGRAMFILES") + "/Chromium/";
         } else {
             System.out.println("Your OS is currently not supported. Please refer to google for a suitable chromium build.");
             //TODO: make ^ this as a pop up box or similar
             System.exit(1);
         }
 
         settings = Settings.load();
         if (settings.OS == -1) {
             if (System.getProperty("os.name").contains("Windows")) {
                 settings.OS = Settings.WIN32;
             }
             if (System.getProperty("os.name").contains("Mac")) {
                 settings.OS = Settings.MACOSX;
             }
         }
         gui = new GUI();
         download = new Download(gui);
 
         gui.runGUI();
 
         check(gui);
 
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override public void run() {
                 settings.save();
             }
         });
     }
 
     public static void check(GUI g) {
         VersionCheck check = new VersionCheck(settings);
         check.checkRemote();
         g.setLastUpdateTime(settings.lastRemoteCheck);
         g.setLocalVersion(settings.localBuild);
         g.setRemoteVersion(settings.remoteBuild);
 	//TODO use "Force" update if checked
 	ChangeLogViewer chlv = new ChangeLogViewer(settings.remoteBuild, settings.OS);
         chlv.parseXML();
         g.setChangeLog(chlv.changeLog);
        
         if (settings.localBuild < settings.remoteBuild) {
            g.showUpdateButton();
         } else {
             g.setLabel("already up-to-date!");
 	    g.showUpdateButton = false;
 	    g.hideUpdateButton();
         }
     }
 
     static File download(GUI g, int build) {
         try {
             File f = File.createTempFile("chrome-" + (win32 ? "win32" : "mac") + "-" + build, ".zip");
             f.deleteOnExit();
             if (!f.exists()) {
                 //ohmygodwearegoingtodie
                 return null;
             }
             if (win32) {
                dlurl = new URL(baseDLUrl + "Win/" + build + "/chrome-win32.zip");
             } else if (macosx) {
                dlurl = new URL(baseDLUrl + "/Mac/" + build + "/chrome-mac.zip");
             }
             download.download(dlurl, f);
             return f;
         } catch (MalformedURLException ex) {
             ex.printStackTrace();
         } catch (IOException ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     /**
      *
      * @param filename the file to unzip
      * @param destination the desinationfolder, should look like c:\\path\\to\\folder\\
      */
     static void unzip(File from, String destination) {
         Unzip zip = new Unzip(from, new File(destination));
         try {
             zip.extract();
         } catch (Exception ex) {
             System.out.println("Error unzipping!");
         }
     }
 
     static boolean delete(File dir) {
         if (dir.isDirectory()) {
             String[] children = dir.list();
             for (int i = 0; i < children.length; i++) {
                 boolean success = false;
                 if (children[i].endsWith("updater-settings")) {
                     success = true;
                 } else {
                     success = delete(new File(dir, children[i]));
                 }
                 if (!success) {
                     return false;
                 }
             }
         }
 
         return dir.delete();
     }
 
     static void update(final GUI g) {
         //start the download in a new thread
         Thread t = new Thread() {
 
             @Override
             public void run() {
                 Settings s = ChromiumUpdater.settings;
                 int buildToDownload = s.remoteBuild;
                 //check if we updated
                if (s.lastRemoteCheck + s.minCheckTime < System.currentTimeMillis()) {
                     int oldChecked = s.remoteBuild;
                     check(ChromiumUpdater.gui);
                     if (oldChecked != s.remoteBuild) {
                         int response = JOptionPane.showConfirmDialog(g, "The build number changed since we last checked. Before, it was "
                                 + oldChecked + ", now it is " + s.remoteBuild + "." + System.getProperty("line.separator")
                                 + "If you want to download the new one (" + s.remoteBuild + "), hit \"Yes\", "
                                 + "if you still want the one we were looking for before, hit \"No\". If you want to bail out, click \"Cancel\".",
                                 "Remote build changed", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                         switch (response) {
                             case JOptionPane.YES_OPTION:
                                 buildToDownload = s.remoteBuild;
                                 break;
                             case JOptionPane.NO_OPTION:
                                 buildToDownload = oldChecked;
                                 break;
                             case JOptionPane.CANCEL_OPTION:
                                 //bail out!
                                 return;
                             default:
                                 //okay...
                                 System.out.println("weird return value from messagebox, stopping : " + response);
                                 return;
                         }
                     }
                 }
 
                 //download the latest build
                 System.out.println("Downloading ...");
                 File f = download(gui, buildToDownload);
                 System.out.println("Download done. Cleaning install directory ...");
                 if (macosx) {
                     Runtime run = Runtime.getRuntime();
                     String delcmd = "./cocoasudo --prompt=\"Copying Chromium to Applications\" rm -R Applications/Chromium.app";
                     try {
                         Process pr = run.exec(delcmd);
                     } catch (IOException ex) {
                         System.out.println("Error whilst copying to Applications");
                     }
                 } else if (win32) {
                     if (!delete(new File(installDir))) {
                         System.out.println("Error whilst cleaning install directory!");
                     } else {
                         System.out.println("Done. Unzipping new build into install directory ...");
                     }
                 }
                 //TODO add unzip time to GUI progressbar
                 if (settings.OS == Settings.WIN32) {
                     unzip(f, installDir); //unzip to program files folder
                 } else if (settings.OS == Settings.MACOSX) {
                     unzip(f, tempDir); //unzip to temp dir
                    //TODO: load the cocoasudo binary
                     Runtime run = Runtime.getRuntime();
                     String copycmd = "./cocoasudo --prompt=\"Copying Chromium to Applications\" mv -R" + tempDir +" Applications/";
                     try {
                         Process pr = run.exec(copycmd);
                     } catch (IOException ex) {
                         System.out.println("Error whilst copying to Applications");
                     }
                 }
                 System.out.println("Deleting temporary files...");
                 if (!delete(f)) {
                     System.out.println("Error whilst deleting temporary file!");
                 } else {
                     System.out.println("Temporary files deleted.");
                 }
 
                 //save new build number and a timestamp
                 s.localBuild = buildToDownload;
                 s.lastUpdate = System.currentTimeMillis();
                 settings.save();
                 g.setLocalVersion(buildToDownload);
                 g.setLastUpdateTime(s.lastUpdate);
                 g.setLabel("Done!");
             }
         };
         t.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
         t.start();
     }
 }
