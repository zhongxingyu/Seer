 package com.yoursway.tinyupdater;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 
 import com.yoursway.utils.log.Log;
 import com.yoursway.utils.os.YsOSUtils;
 
 public class TinyUpdater {
     
     private static TinyUpdater instance;
     
     private static final int TIMER_INTERVAL_MS = 24 * 60 * 60 * 1000;
     
     private final String version;
     private final URL lastVersionDescription;
     
     private Timer timer;
     
     private TinyUpdater(String version, String product, String platform, String releaseType, URL updateSite)
             throws MalformedURLException {
         if (version == null)
             throw new NullPointerException("version is null");
         
         this.version = version;
         
        lastVersionDescription = new URL(updateSite + "/version_" + product + "_" + platform + "_"
                 + releaseType + ".txt");
     }
     
     public static TinyUpdater instance() {
         if (instance == null) {
             try {
                 InputStream stream = Activator.instance().getBundle().getEntry("/version.txt").openStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                 
                 String version = reader.readLine();
                 String product = reader.readLine();
                 String platform = reader.readLine();
                 String releaseType = reader.readLine();
                 URL updateSite = new URL(reader.readLine());
                 
                 String envRelType = System.getenv(product.toUpperCase() + "_RELTYPE");
                 if (envRelType != null)
                     releaseType = envRelType;
                 
                 instance = new TinyUpdater(version, product, platform, releaseType, updateSite);
             } catch (IOException e) {
                 Log.writeError("Cannot initialize TinyUpdater. " + e);
                 e.printStackTrace();
             }
         }
         return instance;
     }
     
     public void checkUpdate(boolean fromMenu) {
         try {
             InputStream stream = lastVersionDescription.openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
             
             String lastVersion = reader.readLine();
             String updateUrl = reader.readLine();
             
             if (!lastVersion.equalsIgnoreCase(version))
                 suggestUpdate(updateUrl);
             else {
                 if (fromMenu)
                     notifyOfNoUpdates();
             }
             
         } catch (IOException e) {
             
             Log.writeError("Cannot check for updates. " + e);
             e.printStackTrace();
             
             if (fromMenu)
                 notifyOfCheckUpdateFailure();
             
         }
         
         resetTimer();
     }
     
     private void resetTimer() {
         
         if (timer != null)
             timer.cancel();
         
         timer = new Timer();
         timer.schedule(new TimerTask() {
             
             @Override
             public void run() {
                 checkUpdate(false);
             }
             
         }, TIMER_INTERVAL_MS);
         
     }
     
     private void suggestUpdate(final String updateUrl) {
         
         Display.getDefault().syncExec(new Runnable() {
             
             public void run() {
                 Shell shell = new Shell(); //!
                 
                 MessageBox msgbox = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
                 msgbox.setMessage("A new version available. Do you want to download it now?");
                 msgbox.setText("Updater");
                 
                 int answer = msgbox.open();
                 if (answer == SWT.YES) {
                     try {
                         YsOSUtils.openBrowser(updateUrl);
                     } catch (IOException e) {
                         e.printStackTrace();
                         
                         MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
                         box.setText("Updater Error");
                         String string = "Couldn't open browser. You can download the update from "
                                 + updateUrl;
                         box.setMessage(string);
                         
                         box.open();
                     }
                 }
             }
         });
     }
     
     private void notifyOfNoUpdates() {
         
         Display.getDefault().syncExec(new Runnable() {
             
             public void run() {
                 MessageBox msgbox = new MessageBox(new Shell(), SWT.ICON_INFORMATION); //!
                 msgbox.setMessage("No updates available.");
                 msgbox.setText("Updater");
                 
                 msgbox.open();
             }
         });
     }
     
     private void notifyOfCheckUpdateFailure() {
         
         Display.getDefault().syncExec(new Runnable() {
             
             public void run() {
                 MessageBox msgbox = new MessageBox(new Shell(), SWT.ICON_ERROR); //!
                 msgbox.setMessage("Cannot check for updates.");
                 msgbox.setText("Updater Error");
                 
                 msgbox.open();
             }
         });
     }
 }
