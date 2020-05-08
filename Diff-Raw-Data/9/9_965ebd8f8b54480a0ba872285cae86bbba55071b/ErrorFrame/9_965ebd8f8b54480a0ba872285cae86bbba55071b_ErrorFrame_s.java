 package gui;
 
 import java.util.*;
 import javax.swing.*;
 
 /**
  * Log errors.
  */
 public class ErrorFrame extends JFrame {
 	private static final long serialVersionUID = 771240677910547456L;
 
 	static ErrorFrame me;
     JTextArea errors;
 
     private ErrorFrame () {
        setTitle("Errors");
         setSize(600, 400);
         setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 
         errors = new JTextArea();
         JScrollPane scroll = new JScrollPane(errors);
 
         errors.setEditable(false);
 
         add(scroll);
     }
 
     public static void log(String msg) {
         if (me == null)
             me = new ErrorFrame();
 
         me.errors.setText(me.errors.getText() + (new Date()) + ": " + msg + "\n");
     }
 
     public static void showMe() {
         if (me == null)
             me = new ErrorFrame();
 
         me.setVisible(true);
     }
 }
