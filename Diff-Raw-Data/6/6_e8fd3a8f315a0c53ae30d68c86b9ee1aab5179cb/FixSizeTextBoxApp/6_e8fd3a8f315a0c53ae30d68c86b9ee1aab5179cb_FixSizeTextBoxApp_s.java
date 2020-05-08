 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.varman.desktopapp.fixsizetextbox;
 
 import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 /**
  *
  * @author varman
  */
 public class FixSizeTextBoxApp extends JFrame{
     private static final int MAX_CHARS_ALLOWED = 10;
     private final JTextField fixSizeTextField;
     
     
     public FixSizeTextBoxApp() {
         
         // create document class for text field
         TextFieldDocument textFieldDoc = new TextFieldDocument();
         textFieldDoc.setMaxTextLength(MAX_CHARS_ALLOWED);
         
         // create text field, and pass the document to it
         fixSizeTextField = new JTextField();
         fixSizeTextField.setDocument(textFieldDoc);
         
         setLayout(new BorderLayout());
        add(new JLabel("Allow only " + MAX_CHARS_ALLOWED + "characters"), BorderLayout.NORTH);
         add(fixSizeTextField, BorderLayout.CENTER);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setSize(500, 70);
         setResizable(false);
     }
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         // TODO code application logic here
         
         java.awt.EventQueue.invokeLater(
             new Runnable() {
             @Override
             public void run() {
                 FixSizeTextBoxApp fixSizeTextBoxApp = 
                         new FixSizeTextBoxApp();
                 fixSizeTextBoxApp.setVisible(true);
             }
         }
                 
         );
     }
 }
