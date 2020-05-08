 package findMyIP;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFileChooser;
 import javax.swing.SwingUtilities;
 
 import java.awt.AWTException;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.SystemTray;
 import java.awt.Toolkit;
 import java.awt.TrayIcon;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.prefs.Preferences;
 
 public class FindMyIP {
   
    private static final int QUICK_INTERVAL = 5 * 1000;      // 5 seconds.
    private static final int SLOW_INTERVAL  = 5 * 60 * 1000; // 5 minutes.
 
     private static final Preferences prefs = Preferences.userNodeForPackage(FindMyIP.class);
     private static final String DEFAULT_FILENAME = "";
     private static final String SAVE_FILE = "save";
 
     private static final String name = "FindMyIP";
 
     private String currentIP;
     private String currentLocalIP;
     private TrayIcon trayIcon;
     private ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
   
     public FindMyIP() {
         currentIP = IPUtils.getIPAddress();
         currentLocalIP = IPUtils.getLocalIPAddress();
     }
 
     private File getSaveFile() {
         String saveFileName = prefs.get(SAVE_FILE, DEFAULT_FILENAME);
         return saveFileName.equals(DEFAULT_FILENAME) ? null : new File(saveFileName);
     }
 
     private void displayMessage(String message, String localMessage) {
        trayIcon.displayMessage(name, message + currentIP + "\n" + localMessage + currentLocalIP, TrayIcon.MessageType.INFO);
     }
 
     private void update(boolean onlyWhenChanged) {
 
         String message = onlyWhenChanged ? null : "IP not changed: ";
         String localMessage = onlyWhenChanged ? null : "Local IP not changed: ";
 
         boolean save = false;
         String newIP = IPUtils.getIPAddress();
 
         if (!currentIP.equals(newIP)) {
             currentIP = newIP;
             message = "New public IP: ";
             save = true;
 
         }
 
         newIP = IPUtils.getLocalIPAddress();
 
         if (!currentLocalIP.equals(newIP)) {
             currentLocalIP = newIP;
             localMessage = "New local IP: ";
             save = true;
         }
 
         if (save) save();
 
         if (timer != null) {
 
             if (currentIP.equals("<unknown>") || currentLocalIP.equals("<unknown>")) {
                 updateTimer(QUICK_INTERVAL);
             }
             else {
                 updateTimer(SLOW_INTERVAL);
             }
         }
 
         if (message != null && localMessage != null) {
             displayMessage(message, localMessage);
         }
         trayIcon.setToolTip("Current IP: " + currentIP + "\n" + "Current Local IP: " + currentLocalIP);
     }
 
     private void save() {
         File saveFile = getSaveFile();
         if (saveFile == null) return;
         try {
             PrintWriter printWriter = new PrintWriter(saveFile);
             printWriter.println(currentIP);
             printWriter.println(currentLocalIP);
             printWriter.close();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
     }
 
     private void copyIPToClipboard() {
 
         Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                 new StringSelection(currentIP), null);
 
     }
 
     private void copyLocalIPToClipboard() {
          Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                 new StringSelection(currentLocalIP), null);
     }
 
     private void createAndShowGUI() {
 
         if (!SystemTray.isSupported()) {
             System.out.println("SystemTray is not supported");
             System.exit(1);
         }
 
         final PopupMenu popupMenu= new PopupMenu();
 
         MenuItem copyItem = new MenuItem("Copy public IP to clipboard");
         MenuItem copyLocalItem = new MenuItem("Copy local IP to clipboard");
         final MenuItem saveItem = new MenuItem(getSaveFile() == null ? "Save to file..." : "Cancel save to file");
         MenuItem updateItem = new MenuItem("Update now");
         MenuItem exitItem = new MenuItem("Exit");
 
         popupMenu.add(copyItem);
         popupMenu.add(copyLocalItem);
         popupMenu.addSeparator();
         popupMenu.add(updateItem);
         popupMenu.add(saveItem);
         popupMenu.add(exitItem);
 
         try {
             trayIcon = new TrayIcon(ImageIO.read(getClass().getResource(("/icon.png"))), name, popupMenu);
         }
         catch (IOException e) {
             e.printStackTrace();
         }
 
         try {
             SystemTray.getSystemTray().add(trayIcon);
         }
         catch (AWTException e) {
             e.printStackTrace();
         }
 
         trayIcon.setImageAutoSize(true);
         trayIcon.setToolTip("Current IP: " + currentIP + "\n" + "Current Local IP: " + currentLocalIP);
         displayMessage("Current IP: ", "Current Local IP: ");
 
         copyItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 copyIPToClipboard();
             }
         });
 
         copyLocalItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 copyLocalIPToClipboard();
             }
         });
 
         updateItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 update(false);
             }
         });
 
         saveItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (getSaveFile() == null) {
                 JFileChooser fileChooser = new JFileChooser();
                 int chosenOption = fileChooser.showSaveDialog(null);
                 if (chosenOption == JFileChooser.APPROVE_OPTION) {
                     prefs.put(SAVE_FILE, fileChooser.getSelectedFile().getAbsolutePath());
                     saveItem.setLabel("Cancel save to file");
                     save();
                 }
 
                 }
                 else {
                     prefs.put(SAVE_FILE, DEFAULT_FILENAME);
                     saveItem.setLabel("Save to file...");
                 }
             }
         });
 
         exitItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 SystemTray.getSystemTray().remove(trayIcon);
                 System.exit(0);
             }
         });
     }
 
     private void updateTimer(int intervalTime) {
 
         timer.shutdown();
         timer = Executors.newSingleThreadScheduledExecutor();
         timer.scheduleAtFixedRate(new Runnable() {
             @Override
             public void run() {
                 update(true);
             }
         }, 0, intervalTime, TimeUnit.SECONDS);
 
     }
 
     public static void main(String[] args) {
 
         final FindMyIP ipFinder = new FindMyIP();
 
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 ipFinder.createAndShowGUI();
                 ipFinder.updateTimer(SLOW_INTERVAL);
             }
         });
 
     }
 
 }
