 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Vista;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 /**
  *
  * @author Kraken
  */
 public abstract class Interfaz extends javax.swing.JFrame {
     
     protected Log log;
     protected ArrayList<javax.swing.JFrame> forms;
     
     protected Interfaz() {
         this.log = Log.check();
         this.forms = new ArrayList<javax.swing.JFrame>();
     }
     
     protected void salir() {
         for(javax.swing.JFrame form: forms) {
             form.dispose();
         }
         log.out();
         new GUILogin().setVisible(true);
     }
     
     public static void soloNum(java.awt.event.KeyEvent evt) {
         char caracter = evt.getKeyChar();
         if(((caracter < '0') || (caracter > '9')) && (caracter != '\b')) {
             evt.consume();  // ignorar el evento de teclado
         }
     }
     
     public static void soloABC(java.awt.event.KeyEvent evt) {
         char c = evt.getKeyChar();
         if (!(Character.isLetter(c) || c == ' ' || c == 8)) {
             evt.consume();
         }
     }
     
     public static void showError(javax.swing.JTextField txt, String msj) {
         txt.setBorder(javax.swing.BorderFactory.createEtchedBorder(Color.lightGray, Color.red));
         txt.setToolTipText(msj);
     }
     
     public static void showError(javax.swing.JComboBox cmb, String msj) {
         cmb.setBackground(Color.red);
         cmb.setToolTipText(msj);
     }
     
    public static void showError(javax.swing.JTextArea txt, String msj) {
        txt.setBorder(javax.swing.BorderFactory.createEtchedBorder(Color.lightGray, Color.red));
        txt.setToolTipText(msj);
    }
    
     public static void normalizeInput(javax.swing.JTextField txt) {
             txt.setBorder(new javax.swing.JTextField().getBorder());
             txt.setToolTipText(null);
     }
     
     public static void normalizeInput(javax.swing.JComboBox cmb) {
             cmb.setBackground(new javax.swing.JComboBox().getBackground());
             cmb.setToolTipText(null);
     }
     
    public static void normalizeInput(javax.swing.JTextArea txt) {
            txt.setBorder(new javax.swing.JTextArea().getBorder());
            txt.setToolTipText(null);
    }
    
 }
