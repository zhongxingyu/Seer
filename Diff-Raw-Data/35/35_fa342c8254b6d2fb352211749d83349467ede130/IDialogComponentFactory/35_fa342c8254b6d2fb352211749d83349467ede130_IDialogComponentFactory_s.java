 package jDistsim.ui.dialog;
 
 import javax.swing.*;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 24.2.13
  * Time: 16:49
  */
 public interface IDialogComponentFactory {
     JComponent makeLabel(String text);
 
     JButton makeButton(String text);
 
     JTextField makeTextField();
 }
