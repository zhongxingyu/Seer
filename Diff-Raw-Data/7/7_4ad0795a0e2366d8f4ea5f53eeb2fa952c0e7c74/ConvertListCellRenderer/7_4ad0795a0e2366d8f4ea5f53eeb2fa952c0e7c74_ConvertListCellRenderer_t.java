 package com.edsdev.jconvert.presentation.component;
 
 import java.awt.Color;
 import java.awt.Component;
 
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 import javax.swing.UIManager;
 
 import com.edsdev.jconvert.presentation.ConversionUnitData;
 
 /**
  * This is responsible for rendering conversions in a list.
  * 
  * @author Ed Sarrazin
  */
 public class ConvertListCellRenderer extends JLabel implements ListCellRenderer {
 
     public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
             boolean cellHasFocus) {
 
         ConversionUnitData cud = (ConversionUnitData) value;
 
         String text = "";
         if (cud.getGenerationAge() >= 2) {
             text = "*** ";
         }
         text += cud.getTranslatedUnit();
         if (cud.getTranslatedUnitAbbrev() != null && !cud.getTranslatedUnitAbbrev().trim().equals("")) {
             text += " - " + cud.getTranslatedUnitAbbrev();
         }
         this.setText(text);
         this.setOpaque(true);
 
         if (isSelected) {
             if (cud.getGenerationAge() >= 2) {
                 this.setBackground(Color.YELLOW);
                 this.setForeground(list.getForeground());
             } else {
                 this.setBackground(list.getSelectionBackground());
                 this.setForeground(list.getSelectionForeground());
             }
         } else {
             this.setBackground(list.getBackground());
             this.setForeground(list.getForeground());
         }
 
         this.setEnabled(list.isEnabled());
         this.setFont(list.getFont());
 
// On java 1.5 this seems to cause a lot of cells to get a border around them other than the sellected/focused celll
//        if (cellHasFocus) {
//            this.setBorder(UIManager.getBorder("List.focusCellHighlightBorder"));
//        }
 
         return this;
     }
 
 }
