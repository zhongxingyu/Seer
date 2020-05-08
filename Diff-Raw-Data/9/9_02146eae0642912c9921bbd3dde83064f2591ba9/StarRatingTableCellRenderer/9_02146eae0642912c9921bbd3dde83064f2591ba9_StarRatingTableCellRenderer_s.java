 package com.customfit.ctg.view;
 
 import java.awt.*;
 import javax.swing.*;
 import javax.swing.table.*;
 
 /**
  * Draws an icon into a table cell for the rating.
  * 
  * @author David
  */
 public class StarRatingTableCellRenderer extends DefaultTableCellRenderer
 {
     public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
         if (table.getModel().getValueAt(row, column).getClass().getName().equals(Double.class.getName()))
         {
             StarRatingPanel ratingPanel = new StarRatingPanel();
             ratingPanel.setScale(5);
            ratingPanel.setRating((double)value);
             ratingPanel.setEditable(false);
             
             table.setRowHeight(ratingPanel.getIconHeight()+3);
 
             return ratingPanel;
         }
         else
         {
             return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
         }
     }
 }
