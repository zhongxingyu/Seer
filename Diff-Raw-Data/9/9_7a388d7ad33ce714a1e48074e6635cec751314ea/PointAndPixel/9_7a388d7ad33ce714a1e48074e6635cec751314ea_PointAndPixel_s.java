 import javax.swing.*;
 import javax.swing.text.*;
 import javax.swing.event.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import java.io.*;
 import pointAndPixel.*;
 
 class PointAndPixel {
 
   private int height = 0;
   private int width = 0;
 
   private JFrame tools = null;
   
   public PointAndPixel() {
 		tools = new ToolsWindow();
 		JDialog d2 = new JDialog(tools);
     d2.setModalityType(Dialog.ModalityType.MODELESS);
     tools.setLocationRelativeTo(null);
    tools.setLocation(0, 80);
 		tools.setVisible(true);
   }
   
   public static void main(String args[]) {
     PointAndPixel pointAndPixel = new PointAndPixel();
   }
 }
 
