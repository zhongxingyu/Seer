 /*
  * Copyright (c) 2013 Philippe VIENNE
  *
  * This file is a part of SpeleoGraph
  *
  * SpeleoGraph is free software: you can redistribute
  * it and/or modify it under the terms of the GNU General
  * Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * SpeleoGraph is distributed in the hope that it will
  * be useful, but WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with SpeleoGraph.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.cds06.speleograph;
 
 import org.cds06.speleograph.data.Series;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 /**
  * This file is created by PhilippeGeek.
  * Distributed on licence GNU GPL V3.
  */
 public class CheckBoxList extends JList<Series> {
     protected static Border noFocusBorder =
             new EmptyBorder(1, 1, 1, 1);
 
     //private JPopupMenu popupMenu = new JPopupMenu();
 
     public CheckBoxList(ListModel<Series> model) {
         super(model);
 
         setCellRenderer(new CellRenderer());
 
         model.addListDataListener(new ListDataListener() {
             @Override
             public void intervalAdded(ListDataEvent e) {
                 repaint();
             }
 
             @Override
             public void intervalRemoved(ListDataEvent e) {
                 repaint();
             }
 
             @Override
             public void contentsChanged(ListDataEvent e) {
                 repaint();
             }
         });
 
         addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 int index = locationToIndex(e.getPoint());
                 if (index != -1 && getCellBounds(index, index).contains(e.getPoint())) {
                     Series value = getModel().getElementAt(index);
                     switch (e.getButton()) {
                         case MouseEvent.BUTTON1:
                             value.setShow(!value.isShow());
                             repaint();
                             break;
                         case MouseEvent.BUTTON3:
                             openPopupMenuFor(value, e);
                             break;
                     }
                 }
             }
         }
         );
 
         setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     }
 
     private void openPopupMenuFor(final Series series, MouseEvent mouseEvent) {
         JPopupMenu menu = SpeleoGraphApp.getInstance().getSeriesMenu().getPopupMenu(series);
        System.out.println("Menu entries: " + menu.getComponentCount());
         menu.show(this, mouseEvent.getX(), mouseEvent.getY());
        System.out.println("Menu :" + mouseEvent + "\n" + menu);
     }
 
     protected final class CellRenderer implements ListCellRenderer<Series> {
 
         private JCheckBox box = new JCheckBox();
 
         public Component getListCellRendererComponent(
                 JList list, Series value, int index,
                 boolean isSelected, boolean cellHasFocus) {
             box.setSelected(value.isShow());
             box.setText(value.getName());
             box.setBackground(isSelected ?
                     getSelectionBackground() : getBackground());
             box.setForeground(isSelected ?
                     getSelectionForeground() : getForeground());
             box.setEnabled(isEnabled());
             box.setFont(getFont());
             box.setFocusPainted(false);
             box.setBorderPainted(true);
             box.setBorder(isSelected ?
                     UIManager.getBorder(
                             "List.focusCellHighlightBorder") : noFocusBorder);
             return box;
         }
     }
 }
