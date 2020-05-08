 /*
  * File:   JTrayIcon.java
  * Author: David Brun <brundavid@gmail.com>
  *
  * Created on 27 sept. 2011, 17:13:38
  */
 
 package fr.davidbrun.wifiautoconnect.views;
 
 import javax.swing.*;
 import javax.swing.event.PopupMenuListener;
 import javax.swing.event.PopupMenuEvent;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 /**
  * Represent a SWING based tray icon.<br/>
  * This allows customization of popup menus, especially with look and feels
  * 
  * @author David Brun <brundavid@gmail.com>
  */
 public class JTrayIcon extends TrayIcon
 {
     // <editor-fold defaultstate="collapsed" desc="Fields">
     
     private JPopupMenu popupMenu;
    private static JWindow window;
     static
     {
        window = new JWindow();
        window.setAlwaysOnTop(true);
     }
     
     private static PopupMenuListener popupListener = new PopupMenuListener()
     {
         @Override
         public void popupMenuWillBecomeVisible(PopupMenuEvent e)
         { }
 
         @Override
         public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
         {
             window.setVisible(false);
         }
 
         @Override
         public void popupMenuCanceled(PopupMenuEvent e)
         {
             window.setVisible(false);
         }
     };
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="Constructor">
     
     /**
      * Create an new SWING tray icon, with the specified image
      * 
      * @param image The image to display in the system tray
      */
     public JTrayIcon(Image image)
     {
         super(image);
         addMouseListener(new MouseAdapter()
         {
             @Override
             public void mousePressed(MouseEvent e)
             {
                 showJPopupMenu(e);
             }

             @Override
             public void mouseReleased(MouseEvent e)
             {
                 showJPopupMenu(e);
             }
         });
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="Private methods">
     
     /**
      * Show the SWING popup menu attached to the tray icon
      * 
      * @param e The MouseEvent who triggered the action
      */
     private void showJPopupMenu(MouseEvent e)
     {
         if (popupMenu != null && !popupMenu.isVisible())
         {
             Dimension size = popupMenu.getPreferredSize();
             int adjustedY = e.getY() - size.height;
             int adjustedAdjustedY = (adjustedY < 0 ? e.getY() : adjustedY);
             window.setLocation(e.getXOnScreen(), adjustedAdjustedY);
             window.setVisible(true);
            popupMenu.show(window.getContentPane(), 0, (e.getY() < this.getSize().height ? this.getSize().height - adjustedAdjustedY : 0));
            window.toFront();
         }
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="Public methods">
     
     /**
      * Get the SWING popup menu attached to the tray icon
      * 
      * @return The SWING popup menu attached to the tray icon
      */
     public JPopupMenu getJPopupMenu()
     {
         return popupMenu;
     }
     
     /**
      * Set the SWING popup menu attached to the tray icon
      * 
      * @param popupMenu The SWING popup menu to attach to the tray icon
      */
     public void setJPopupMenu(JPopupMenu popupMenu)
     {
         if (this.popupMenu != null)
             this.popupMenu.removePopupMenuListener(popupListener);
         
         this.popupMenu = popupMenu;
         popupMenu.addPopupMenuListener(popupListener);
     }
     
     // </editor-fold>
 }
