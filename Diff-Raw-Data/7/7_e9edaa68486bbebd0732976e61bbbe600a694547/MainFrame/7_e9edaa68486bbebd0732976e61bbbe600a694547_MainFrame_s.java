 package com.ada.sme.view;
 
import net.miginfocom.swing.MigLayout;

 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
/**
 * User: Enes Kıdık
 * Date: 3/10/13 -- Time: 10:03 AM
 */
 public class MainFrame
 {
     private JPanel mainFrameContainerPanel;
     private JPanel topPanel;
     private JPanel leftPanel;
     private JPanel bottomPanel;
     private JPanel contentContainerPanel;
     private JButton statusButton;
     private JButton localStoreButton;
     private JButton onlineStoreButton;
     private JButton notificationsButton;
     private JButton stockButton;
     private JButton supportButton;
     private JLabel current;
 
 
     public MainFrame()
     {
     }
 
     public static void main(String[] args)
     {
         JFrame frame = new JFrame("MainFrame");
         frame.setContentPane(new MainFrame().mainFrameContainerPanel);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.pack();
         frame.setVisible(true);
     }
 }
