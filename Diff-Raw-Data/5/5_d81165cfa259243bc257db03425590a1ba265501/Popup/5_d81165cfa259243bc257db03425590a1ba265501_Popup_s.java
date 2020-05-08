 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Viewer;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 /**
  *
  * @author mbaxkak4
  */
 public class Popup extends JDialog {
       private String message;
     public Popup (String message)
 
     {    
         this.message=message;
         initComponents();
 
     }
     
     
     void initComponents(){
       JFrame frame=new JFrame();
       JPanel panel=new JPanel();
       
       JOptionPane.showMessageDialog(frame,
          "Eggs are not supposed to be green.");
      
       setVisible(true);
          
     }
 }
