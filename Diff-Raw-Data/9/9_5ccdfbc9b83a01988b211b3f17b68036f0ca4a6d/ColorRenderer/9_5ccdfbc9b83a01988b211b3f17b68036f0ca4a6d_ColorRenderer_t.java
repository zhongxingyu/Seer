 package ch9k.chat.gui.components;
 
 import java.awt.Color;
 import java.awt.Component;
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 import javax.swing.border.Border;
 
 /**
  * Renders colors
  * @author Bruno
  */
 public class ColorRenderer extends JLabel implements ListCellRenderer {
     private Border unselectedBorder;
     private Border selectedBorder;
 
     public ColorRenderer() {
         setOpaque(true);
     }
 
    /**
     * Needs to be empty, otherwise JComboBox won't show the selected color
     * @param color
     */
    @Override
    public void setBackground(Color color){

    }

     @Override
     public Component getListCellRendererComponent(JList list, Object color,
             int index, boolean isSelected, boolean cellHasFocus) {
         setText("     ");
         super.setBackground((Color) color);
 
         if(isSelected) {
             if(selectedBorder == null) {
                 selectedBorder = BorderFactory.createMatteBorder(2, 2, 2, 2,
                         list.getSelectionBackground());
             }
             setBorder(selectedBorder);
         } else {
             if(unselectedBorder == null) {
                 unselectedBorder = BorderFactory.createMatteBorder(2, 2, 2, 2,
                         list.getBackground());
             }
             setBorder(unselectedBorder);
         }
         return this;
     }
 }
