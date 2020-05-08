 package org.powerbat;
 
 import javax.swing.*;
 
 import org.powerbat.configuration.Global;
 import org.powerbat.configuration.Global.Paths;
 import org.powerbat.executor.Executor;
 import org.powerbat.gui.GUI;
 import org.powerbat.gui.Splash;
 import org.powerbat.methods.Updater;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URI;
 
 /**
  * The boot class is responsible for basic loading for the client. Bringing all
  * the classes into unison, it effectively creates what is known as Powerbat.
  * Advanced technology to help you learn Java and fulfill what I like to know as
  * 'Good standing'. Helping others for free. Give what you can and take what you
  * must. From everything to the CustomClassLoader class to the Project class,
  * everything here was made for you, the user. I hope you have a great time
  * running this application.
  * <br>
  * <br>
  *
  * @author Naux
  * @version 1.0
  * @since 1.0
  */
 
 public class Boot {
 
     /**
      * Nothing truly big to see here. Runs the application. Really should be
      * monitored but it isn't.
      *
      * @param args ignored. Or is it?
      * @since 1.0
      */
 
     public static void main(String[] args) {
         if (!Executor.hasJDKInstalled()) {
            final int option = JOptionPane.showConfirmDialog(null, "<html>You need to have JDK installed to run Powerbat.<br>Click 'Ok' if you would like to go to the JDK site.</html>", "JDK Required",
                    JOptionPane.OK_CANCEL_OPTION);
             if (option == JOptionPane.OK_OPTION) {
                 final Desktop desktop = Desktop.getDesktop();
                 if (desktop.isSupported(Desktop.Action.BROWSE)) {
                     try {
                         desktop.browse(new URI("www.oracle.com/technetwork/java/javase/downloads/"));
                     } catch (Exception ignored) {
 
                     }
                 }
             }
             System.exit(0);
             return;
         }
         Paths.build();
         Global.loadImages();
         Splash.setStatus("Loading");
         final Splash splash = new Splash();
         final Timer repaint = new Timer(20, new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 splash.repaint();
             }
         });
         try {
             SwingUtilities.invokeAndWait(new Runnable() {
                 public void run() {
                     splash.setVisible(true);
                     repaint.start();
                 }
             });
         } catch (InterruptedException | InvocationTargetException e) {
             e.printStackTrace();
             System.exit(0);
         }
         Updater.update();
         Splash.setStatus("Loading framework");
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 try {
                     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                     Splash.setStatus("Building GUI");
                     new GUI();
                     splash.shouldDispose(true);
                     splash.dispose();
                     Splash.setStatus(null);
                     repaint.stop();
                 } catch (Exception e) {
                     e.printStackTrace();
                     System.exit(0);
                 }
             }
         });
     }
 }
