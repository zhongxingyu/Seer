 import java.awt.AWTException;
 import java.awt.MenuItem;
 import java.awt.MouseInfo;
 import java.awt.PointerInfo;
 import java.awt.PopupMenu;
 import java.awt.Robot;
 import  java.awt.SystemTray;
 import java.awt.TrayIcon;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
import java.sql.Time;
 
 import javax.imageio.ImageIO;
 
 public class SleepLess implements Runnable, ActionListener {
   private Robot r;
   private PointerInfo pi; 
   
   private SystemTray tray;
   private Thread runner;
   
   private PopupMenu popup;
   private MenuItem exit;
   
   private TrayIcon trayIcon      = null;
   private BufferedImage imageOn  = null;
   private BufferedImage imageOff = null;
   private Boolean moveMouse      = true;
   private int ctr                = 1;
 
   public SleepLess() {
     // Robot needed to sleep
     try{
       r = new Robot();
     }catch(Exception e) {
       System.exit(0);
     }
 
     tray = SystemTray.getSystemTray();
     runner = new Thread(this);
     runner.start();              // Start running
 
     if (SystemTray.isSupported()) {  // Create system icon
       // Get icon image
       try {
         imageOn  = ImageIO.read(SleepLess.class.getResource("/icon/zzz.gif"));
         imageOff = ImageIO.read(SleepLess.class.getResource("/icon/aaa.gif"));
       } catch (IOException e1) {
         e1.printStackTrace();
       }
 
       // Create icon menu
       popup = new PopupMenu();
       exit = new MenuItem("Exit");
       popup.add(exit);
 
       // Create Icon
       trayIcon = new TrayIcon(imageOn, "Sleep Less - " + ctr, popup);
 
       trayIcon.setImageAutoSize(true);
       trayIcon.addActionListener(this);
       exit.addActionListener(this);
 
       try {
         tray.add(trayIcon);
       }
       catch (AWTException e) {
         System.err.println("Error: Icon could not be added.");
       }
     }   
     else {
       System.err.println("Error: Icon cannot be implimented");
     } 
   }
 
   public void run() {
     ctr = 1;
     while(true) {
      r.delay(600);
       ctr++;
       // If moveMouse is true, that means we want to disable
       // the screen saver
       if(moveMouse) {
         trayIcon.setToolTip("Sleep Less - " + ctr);
         pi = MouseInfo.getPointerInfo();
         int x = pi.getLocation().x;
         int y = pi.getLocation().y;
         System.out.println("X: " + x + " Y: " + y + " CTR = " + ctr);
         r.mouseMove(x,y);
         if(ctr%5==0) System.gc();
       }
     }
   }
 
   @Override
   public void actionPerformed(ActionEvent e) {
     if(e.getSource() == exit) {
       System.out.println("Exiting...");
       System.exit(0);
     }
     else if(e.getSource() == trayIcon) {
       if(moveMouse) {
         trayIcon.setImage(imageOff);
         moveMouse = false;            // Turn mouse mover off
         System.out.println("Turning Off...");
       }
       else {
         trayIcon.setImage(imageOn);
         moveMouse = true;              // Turn mouse mover on
         System.out.println("Turning On...");
       }
     }
   }
 }
