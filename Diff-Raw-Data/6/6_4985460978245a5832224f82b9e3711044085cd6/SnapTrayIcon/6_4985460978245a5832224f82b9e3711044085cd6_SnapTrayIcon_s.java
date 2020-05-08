 package at.yawk.snap;
 
 import java.awt.AWTException;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.HeadlessException;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.SystemTray;
 import java.awt.TrayIcon;
 import java.awt.TrayIcon.MessageType;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JFrame;
 
 
 public class SnapTrayIcon implements Runnable, UpdateMonitor {
     private final YawkatSnap snapper;
     
     private int value = 16;
     private boolean downloading = false;
     
     private TrayIcon trayIcon;
     
     public SnapTrayIcon(YawkatSnap snapper) {
         this.snapper = snapper;
     }
     
     @Override
     public void run() {
         try {
             trayIcon = new TrayIcon(generateLogo());
             trayIcon.setToolTip("YawkatSnap");
             final PopupMenu popup = new PopupMenu();
             final MenuItem exit = new MenuItem("Exit");
             exit.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent arg0) {
                     new Thread(new Runnable() {
                         @Override
                         public void run() {
                             System.exit(0);
                         }
                     }).start();
                 }
             });
             final MenuItem snap = new MenuItem("Snap");
             snap.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent arg0) {
                     new Thread(new Runnable() {
                         @Override
                         public void run() {
                             snapper.doSnap();
                         }
                     }).start();
                 }
             });
             final MenuItem config = new MenuItem("Config");
             config.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent arg0) {
                     new Thread(new Runnable() {
                         @Override
                         public void run() {
                             final JFrame jframe = new JFrame("Config");
                             jframe.setLayout(new BorderLayout());
                             jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                             jframe.setResizable(false);
                             final Runnable callback = new Runnable() {
                                 @Override
                                 public void run() {
                                     jframe.setVisible(false);
                                     jframe.dispose();
                                 }
                             };
                             jframe.add(new SettingsPanel(callback, callback, snapper.config));
                             jframe.validate();
                             jframe.pack();
                             jframe.setLocationRelativeTo(null);
                             jframe.setVisible(true);
                         }
                     }).start();
                 }
             });
             popup.add(config);
             popup.add(snap);
             popup.add(exit);
             trayIcon.setPopupMenu(popup);
             SystemTray.getSystemTray().add(trayIcon);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    SystemTray.getSystemTray().remove(trayIcon);
                }
            }));
         } catch (HeadlessException e) {
             e.printStackTrace();
         } catch (AWTException e) {
             e.printStackTrace();
         }
     }
     
     @Override
     public void setValue(float value) {
         boolean newDownloading = value >= 0;
         int newValue = Math.round(value * 16);
         if (newDownloading != this.downloading || newValue != this.value) {
             this.downloading = newDownloading;
             this.value = newValue;
             trayIcon.setImage(generateLogo());
         }
     }
     
     private BufferedImage generateLogo() {
         final BufferedImage image = new BufferedImage(16, 16, downloading ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR);
         if (downloading) {
             final Graphics2D gfx = image.createGraphics();
             gfx.setColor(Color.BLUE);
             gfx.fillRect(0, 16 - value, 16, value);
             gfx.dispose();
         }
         return image;
     }
     
     public void displayMessage(String caption, String text, MessageType type) {
         trayIcon.displayMessage(caption, text, type);
     }
 }
