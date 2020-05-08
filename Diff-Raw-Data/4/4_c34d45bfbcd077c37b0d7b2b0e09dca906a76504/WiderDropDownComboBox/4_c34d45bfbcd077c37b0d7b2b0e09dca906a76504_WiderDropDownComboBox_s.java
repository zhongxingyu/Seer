 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package nava.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import javax.swing.ComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 public class WiderDropDownComboBox extends JComboBox {
     private static final long serialVersionUID = -4994305358602391496L;
 
 
     private String type;
     private boolean layingOut = false;
     private int widestLengh = 0;
     private boolean wide = false;
     
     public WiderDropDownComboBox()
     {
         super();
     }
 
     public WiderDropDownComboBox(Object[] objs) {
         super(objs);
     }
     
     public WiderDropDownComboBox(ComboBoxModel comboBoxModel)
     {
         super(comboBoxModel);
     }
 
     public boolean isWide() {
         return wide;
     }
 
     // Setting the JComboBox wide
     public void setWide(boolean wide) {
         this.wide = wide;
         widestLengh = getWidestItemWidth();
 
     }
 
     @Override
     public Dimension getSize() {
         Dimension dim = super.getSize();
         if (!layingOut && isWide())
             dim.width = Math.max(widestLengh, dim.width);
         return dim;
     }
 
     public int getWidestItemWidth() {
 
         int numOfItems = this.getItemCount();
         Font font = this.getFont();
         FontMetrics metrics = this.getFontMetrics(font);
         int widest = 0;
         for (int i = 0; i < numOfItems; i++) {
             Object item = this.getItemAt(i);
             int lineWidth = metrics.stringWidth(item.toString());
             widest = Math.max(widest, lineWidth);
         }
 
         return widest + 10;
     }
 
     //@Override
     @Override
     public void doLayout() {
         try {
             layingOut = true;
             super.doLayout();
         } finally {
             layingOut = false;
         }
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String t) {
         type = t;
     }
 
     public static void main(String[] args) {
         
         try {
             //javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 System.out.println(info.getName());
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             //java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
            // java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             //java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             //java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         
         String title = "Combo Test";
         JFrame frame = new JFrame(title);
 
         String[] items = {
                 "I need lot of width to be visible , oh am I visible now",
                 "I need lot of width to be visible , oh am I visible now" };
         WiderDropDownComboBox simpleCombo = new WiderDropDownComboBox(items);
        // simpleCombo.setPreferredSize(new Dimension(180, 20));
         simpleCombo.setWide(true);
         JLabel label = new JLabel("Wider Drop Down Demo");
 
         frame.getContentPane().add(simpleCombo, BorderLayout.NORTH);
         frame.getContentPane().add(label, BorderLayout.SOUTH);
         int width = 200;
         int height = 150;
         frame.setSize(width, height);
         frame.setVisible(true);
 
     }
 }
