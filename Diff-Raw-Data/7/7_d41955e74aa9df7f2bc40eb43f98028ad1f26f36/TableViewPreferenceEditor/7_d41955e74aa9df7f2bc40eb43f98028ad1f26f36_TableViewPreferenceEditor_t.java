 /*
  * @(#) $RCSfile: TableView.java,v $ $Revision: 1.51 $ $Date: 2004/08/02 20:23:46 $ $Name: TableView1_3 $
  *
  * Center for Computational Genomics and Bioinformatics
  * Academic Health Center, University of Minnesota
  * Copyright (c) 2000-2002. The Regents of the University of Minnesota  
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * see: http://www.gnu.org/copyleft/gpl.html
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  */
 
 package edu.umn.genomics.table;
 
 import java.io.Serializable;
 // import java.io.*;
 // import java.net.*;
 import java.awt.Component;
 import java.awt.BorderLayout;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.table.*;
 import java.lang.reflect.*;
 import java.util.*;
 import java.util.prefs.*;
 import edu.umn.genomics.component.table.*;
 import edu.umn.genomics.graph.*;
 import edu.umn.genomics.graph.swing.DrawableIcon;
 
 public class TableViewPreferenceEditor extends JFrame  {
   PreferenceTableModel ptm = null;
   Map<Class,TableCellRenderer> rendererMap = new HashMap<Class,TableCellRenderer>();
   Map<Class,TableCellEditor> editorMap = new HashMap<Class,TableCellEditor>();
   class DrawableListCellRenderer extends JLabel implements ListCellRenderer {
     DrawableIcon icon = new DrawableIcon(12,12,new DrawableO(3,true));
     public DrawableListCellRenderer() {
       setOpaque(true);
       setHorizontalAlignment(CENTER);
       setVerticalAlignment(CENTER);
     }
     public Component getListCellRendererComponent(JList list,
                                               Object value,
                                               int index,
                                               boolean isSelected,
                                               boolean cellHasFocus) {
       
       if (isSelected) {
         setBackground(list.getSelectionBackground());
         setForeground(list.getSelectionForeground());
       } else {
         setBackground(list.getBackground());
         setForeground(list.getForeground());
       }
       if (value instanceof Drawable) {
         icon.setDrawable((Drawable)value);
         setIcon(icon);
       } else {
         System.err.println("No icon " + value);
         setIcon(null);
       }
       setText(value != null ? value.toString() : "");
       return this;
     }
   }
   DrawableListCellRenderer drawableListCellRenderer = new DrawableListCellRenderer(); 
 
   public void setDefaultPreferences() throws Exception {
     if (ptm != null) {
       ptm.useDefaultValues();
     }
   }
   public TableViewPreferenceEditor() throws BackingStoreException {
     super("Preference Editor");
     ptm = new PreferenceTableModel();
     JComboBox drawableChoices = new JComboBox();
     drawableChoices.addItem(new DrawableT(1));
     drawableChoices.addItem(new DrawableT(2));
     drawableChoices.addItem(new DrawableT(3));
     drawableChoices.addItem(new DrawableT(4));
     drawableChoices.addItem(new DrawableT(5));
     drawableChoices.addItem(new DrawableX(1));
     drawableChoices.addItem(new DrawableX(2));
     drawableChoices.addItem(new DrawableX(3));
     drawableChoices.addItem(new DrawableX(4));
     drawableChoices.addItem(new DrawableX(5));
     drawableChoices.addItem(new DrawableO(1,false));
     drawableChoices.addItem(new DrawableO(2,false));
     drawableChoices.addItem(new DrawableO(3,false));
     drawableChoices.addItem(new DrawableO(1,true));
     drawableChoices.addItem(new DrawableO(2,true));
     drawableChoices.addItem(new DrawableO(3,true));
     drawableChoices.addItem(new DrawableBox(1,false));
     drawableChoices.addItem(new DrawableBox(2,false));
     drawableChoices.addItem(new DrawableBox(3,false));
     drawableChoices.addItem(new DrawableBox(1,true));
     drawableChoices.addItem(new DrawableBox(2,true));
     drawableChoices.addItem(new DrawableBox(3,true));
     drawableChoices.addItem(new DrawableStar(4,3));
     drawableChoices.addItem(new DrawableStar(4,4));
     drawableChoices.addItem(new DrawableStar(4,5));
     drawableChoices.addItem(new DrawableStar(4,6));
     drawableChoices.addItem(new DrawableStar(4,7));
     drawableChoices.addItem(new DrawableStar(4,8));
     drawableChoices.addItem(new DrawableStar(4,9));
     drawableChoices.setRenderer(drawableListCellRenderer);
     rendererMap.put(java.awt.Color.class,new ColorRenderer());
     editorMap.put(java.awt.Color.class,new ColorEditor());
     editorMap.put(edu.umn.genomics.graph.Drawable.class,new DefaultCellEditor(drawableChoices));
     JTable table = new JTable(ptm) {
       public TableCellEditor getCellEditor(int row, int column) {
         if (column == 2) {
           Object value = getValueAt(row,column);
           if (value != null) {
             for (Class cellClass : editorMap.keySet()) {
               if (cellClass.isInstance(value)) {
                 return editorMap.get(cellClass);
               }
             }
           }
         }
         return super.getCellEditor(row, column);
       }
     };
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    TableColumn col = table.getColumnModel().getColumn(0);
    col.setPreferredWidth(100);
    col = table.getColumnModel().getColumn(1);
    col.setPreferredWidth(100);
    col = table.getColumnModel().getColumn(2);
    col.setPreferredWidth(250);
     table.setDefaultRenderer(Class.class, new ClassNameRenderer());
     table.setDefaultRenderer(String.class, new DefaultTableCellRenderer());
     table.setDefaultRenderer(Object.class, new DelegatingRenderer(rendererMap));
     // table.getColumnModel().getColumn(2).setCellEditor(new DelegatingEditor(editorMap));
     // table.getColumnModel().getColumn(2).setCellEditor(new ColorEditor());
     // table.setDefaultEditor(java.awt.Color.class,new ColorEditor());
     JScrollPane jsp = new JScrollPane(table);
     add(jsp);
     JToolBar tb = new JToolBar();
     tb.setFloatable(false);
     JButton btn;
     // Defaults
 
     btn = new JButton("Close");
     btn.setToolTipText("Close this window.");
     btn.addActionListener(
         new ActionListener() {
           public void actionPerformed(ActionEvent e) {
             try {
               ((JFrame)((JButton)e.getSource()).getTopLevelAncestor()).dispose();
             } catch (Exception ex) {
               ExceptionHandler.popupException(""+ex);
             }
           }
         }
     );
     tb.add(btn);
 
     btn = new JButton("Set Defaults");
     btn.setToolTipText("Set Preferences to Application Defaults");
     btn.addActionListener(
         new ActionListener() {
           public void actionPerformed(ActionEvent e) {
             try {
               setDefaultPreferences();
             } catch (Exception ex) {
               ExceptionHandler.popupException(""+ex);
             }
           }
         }
     );
     tb.add(btn);
 
     // Import
     // Export
     add(tb,BorderLayout.NORTH);
     pack();
     setVisible(true);
   }
 }
