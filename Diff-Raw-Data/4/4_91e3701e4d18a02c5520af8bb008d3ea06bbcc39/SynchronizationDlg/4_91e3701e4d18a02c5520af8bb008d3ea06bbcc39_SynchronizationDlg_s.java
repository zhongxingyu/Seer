 /*
  * Copyright 2010, Intelix, LLC. All rights reserved.
  */
 
 package com.intelix.digihdmi.app.views.dialogs;
 
 import javax.swing.Action;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import net.miginfocom.swing.MigLayout;
 
 /**
  *
  * @author Michael Caron <michael.r.caron@gmail.com>
  */
 public class SynchronizationDlg extends JDialog {
     private JButton btnNo;
     private JButton btnYes;
     private JButton btnDisconnect;
 
     public SynchronizationDlg(JFrame f) {
         super(f);
 
         setModal(true);
         setTitle("Device Synchronization");
         initializeComponents();
         setLocationRelativeTo(null);
     }
 
     private void initializeComponents() {
         JPanel p = new JPanel();
         p.setLayout(new MigLayout((System.getProperty("DEBUG_UI") == null ? "" : "debug,"),"","[]20[]"));
 
         // Use HTML tags to create a line break in the JLabel text
         JLabel l = new JLabel("<html>Push current configuration to device"
                 + "?<br/>(all data will be <i>live</i> otherwise)?"
                 + "</html>");
 
         p.add(l,"wrap");
 
         btnYes = new JButton("Yes");
         p.add(btnYes, "tag other, span, split");
         btnNo = new JButton("No");
         p.add(btnNo, "tag other, span, split");
 
        btnDisconnect = new JButton("Cancel");
         p.add(btnDisconnect, "tag cancel");
 
         setContentPane(p);
         pack();
     }
 
     public void setBtnNoAction(Action a)
     {
         btnNo.setAction(a);
     }
     public void setBtnYesAction(Action a)
     {
         btnYes.setAction(a);
     }
     public void setBtnDisconnectAction(Action a)
     {
         btnDisconnect.setAction(a);
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 System.setProperty("DEBUG_UI", "true");
                 final SynchronizationDlg d = new SynchronizationDlg(null);
                 d.setVisible(true);
                 d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
             }
         });
     }
 }
 
