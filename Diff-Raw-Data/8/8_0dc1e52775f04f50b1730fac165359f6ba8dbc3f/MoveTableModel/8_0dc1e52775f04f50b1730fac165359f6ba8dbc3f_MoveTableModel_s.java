 /*
  * MoveTableModel.java
  *
  * Created on March 21, 2007, 5:20 PM
  *
  * This file is a part of Shoddy Battle.
  * Copyright (C) 2007  Catherine Fitzpatrick
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  */
 
 package shoddybattleclient.utils;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import javax.swing.table.*;
 import java.util.*;
 import javax.swing.JButton;
 import shoddybattleclient.TeamBuilderForm;
 import shoddybattleclient.shoddybattle.PokemonMove;
 import shoddybattleclient.utils.SortableJTable.SortableTableModel;
 
 /**
  *
  * @author Catherine
  * @author Ben
  */
 public class MoveTableModel extends AbstractTableModel implements SortableTableModel {
     
     protected List<TableRow> m_row = new ArrayList<TableRow>();
     private TeamBuilderForm m_parent = null;
     private int m_sortIndex = 1;
     private boolean m_sortReverse = false;
     
     public class TableRow {
         private String m_category;
         private String m_move;
         private String m_type;
         private Integer m_pp;
         private Integer m_power;
         private Integer m_accuracy;
         private JButton m_button;
         private Integer m_ppUps = 3;
         private boolean m_selected = false;
         private MoveTableModel m_model = null;
         
         public TableRow(String category,
                 String move,
                 String type,
                 int pp,
                 int power,
                 int accuracy) {
             m_category = category;
             m_move = move;
             m_type = type;
             m_pp = new Integer(pp);
             m_power = new Integer(power);
             m_accuracy = new Integer(accuracy);
             m_button = new JButton("+");
             m_button.addMouseListener(new MouseAdapter() {
                 public void mousePressed(MouseEvent e) {
                     m_model.selectMove(TableRow.this);
                 }
             });
         }
 
         public void setSelected(boolean selected) {
             m_selected = selected;
             m_button.setText(selected ? "-" : "+");
         }
 
         public boolean equals(TableRow t2) {
             return m_move.equals(t2.m_move);
         }
 
         public boolean isSelected() {
             return m_selected;
         }
 
         public void addMouseListener(MouseListener m) {
             m_button.addMouseListener(m);
         }
 
         public String getMove() {
             return m_move;
         }
     }
 
     public static class SelectedMoveModel extends MoveTableModel {
         public SelectedMoveModel(TeamBuilderForm parent) {
             super(null, new String[0], parent);
             m_row = new ArrayList<TableRow>();
         }
 
         @Override
         public boolean addMove(TableRow row) {
             if (m_row.size() >= 4) return false;
             boolean success = super.addMove(row);
             if (success) row.setSelected(true);
             return success;
         }
 
         @Override
         public Object getValueAt(int i, int j) {
             TableRow row = m_row.get(i);
             switch (j) {
                 case 0: return row.m_button;
                 case 1: return row.m_ppUps;
                 default: return super.getValueAt(i, j - 1);
             }
         }
 
         public String getColumnName(int col) {
             switch (col) {
                 case 0: return "";
                 case 1: return "PP Ups";
                 default: return super.getColumnName(col - 1);
             }
         }
 
         public int getColumnCount() {
             return super.getColumnCount() + 1;
         }
 
         public boolean isCellEditable(int i, int j) {
             return (j == 1);
         }
 
         public void setValueAt(Object value, int i, int j) {
             if (j == 1) {
                 int ppUps;
                 try {
                     ppUps = Integer.parseInt(value.toString());
                 } catch (NumberFormatException e) {
                     ppUps = 3;
                 }
                 if (ppUps < 0) ppUps = 0;
                 else if (ppUps > 3) ppUps = 3;
                 m_row.get(i).m_ppUps = ppUps;
             }
         }
 
         public String[] getMoves() {
             String[] ret = new String[m_row.size()];
             for (int i = 0; i < m_row.size(); i++) {
                 ret[i] = m_row.get(i).m_move;
             }
             return ret;
         }
 
         public int[] getPpUps() {
             int[] ret = new int[m_row.size()];
             for (int i = 0; i < m_row.size(); i++) {
                 ret[i] = m_row.get(i).m_ppUps;
             }
             return ret;
         }
 
         public void sort(final int col, boolean reverse) {
             if (col <= 1) {
                 super.sort(1, reverse);
             }
             super.sort(col - 1, reverse);
         }
 
         public void clear() {
             m_row.clear();
             fireTableDataChanged();
         }
     }
     
     /**
      * Get a value from the table at (i, j).
      */
     public Object getValueAt(int i, int j) {
         final TableRow row = m_row.get(i);
         switch (j) {
             case 0: return row.m_button;
             case 1: return row.m_move;
             case 2: return row.m_category;
             case 3: return row.m_type;
             case 4: return row.m_pp;
             case 5: return (row.m_power <= 1) ? "---" : row.m_power;
             case 6: return (row.m_accuracy == 0) ? "---" : row.m_accuracy;
         }
         assert false;
         return null;
     }
     
     public String getColumnName(int col) {
         switch (col) {
             case 0: return "";
             case 1: return "Name";
             case 2: return "Class";
             case 3: return "Type";
             case 4: return "PP";
             case 5: return "Power";
             case 6: return "Accuracy";
         }
         assert false;
         return null;
     }
     
     public int getColumnCount() {
         return 7;
     }
     
     public int getRowCount() {
         return m_row.size();
     }
     
     @Override
     public Class getColumnClass(int c) {
          return getValueAt(0, c).getClass();
     }
     
     @Override
     public boolean isCellEditable(int row, int col) {
         return false;
     }
      
     @Override
     public void setValueAt(Object value, int i, int j) {
         TableRow row = m_row.get(i);
         switch (j) {
             case 0:                
                 break;
             case 1:
                 row.m_move = (String)value;
                 break;
             case 2:
                 row.m_category = (String)value;
                 break;
             case 3:
                 row.m_type = (String)value;
                 break;
             case 4:
                 row.m_pp = (Integer)value;
                 break;
             case 5:
                 row.m_power = (Integer)value;
                 break;
             case 6:
                 try {
                     int acc = (Integer)value;
                     row.m_accuracy = acc;
                 } catch (Exception e) {
                     row.m_accuracy = 0;
                 }
             default:
                 assert false;
         }
         fireTableCellUpdated(i, j);
     }
     
     /**
      * Creates a new instance of MoveTableModel
      */
     public MoveTableModel(ArrayList<PokemonMove> moveList, String[] moves, TeamBuilderForm parent) {
         m_parent = parent;
         ArrayList<TableRow> list = new ArrayList<TableRow>();
         HashSet<String> set = new HashSet<String>();
         for (int i = 0; i < moves.length; i++) {
             if (set.contains(moves[i])) {
                 continue;
             }
             PokemonMove m = getMove(moves[i], moveList);
             set.add(moves[i]);
             if (m == null) {
                 continue;
             }
             TableRow row = new TableRow(
                     m.damageClass, m.name, m.type, m.pp, m.power, m.accuracy);
             row.m_model = this;
             list.add(row);
         }
         Collections.sort(list, new Comparator<TableRow>() {
             public int compare(TableRow o1, TableRow o2) {
                     TableRow t1 = (TableRow)o1;
                     TableRow t2 = (TableRow)o2;
                     return t1.m_move.compareToIgnoreCase(t2.m_move);
                 }
             });
         m_row = list;
     }
 
     protected void selectMove(TableRow row) {
         boolean success = m_parent.moveSelected(row);
         if (success) {
             m_row.remove(row);
         }
     }
 
     public boolean addMove(final TableRow row) {
         if (m_row.contains(row)) return false;
         row.setSelected(false);
         row.m_model = this;
         m_row.add(row);
         sort(m_sortIndex, m_sortReverse);
         this.fireTableDataChanged();
         return true;
     }
 
     // Finds a move in an ArrayList by name
     private PokemonMove getMove(String name, ArrayList<PokemonMove> moveList) {
         for (PokemonMove m : moveList) {
             if (m.name.equals(name)) {
                 return m;
             }
         }
         return null;
     }
 
     public void sort(final int col, boolean reverse) {
         m_sortIndex = col;
         m_sortReverse = reverse;
         Collections.sort(m_row, new Comparator<TableRow>() {
             public int compare(TableRow t1, TableRow t2) {
                 switch(col) {
                     case 0:
                         return 1;
                     case 1:
                         return t1.m_move.compareTo(t2.m_move);
                     case 2:
                         return t1.m_category.compareTo(t2.m_category);
                     case 3:
                         return t1.m_type.compareTo(t2.m_type);
                     case 4:
                         return -(t1.m_pp.compareTo(t2.m_pp));
                     case 5:
                         return -(t1.m_power.compareTo(t2.m_power));
                     case 6:
                         return -(t1.m_accuracy.compareTo(t2.m_accuracy));
                 }
                 assert false;
                 return 0;
             }
         });
         if (reverse) Collections.reverse(m_row);
     }
 
     public void selectMoves(String[] moves, int[] ppUps) {
         List<TableRow> selected = new ArrayList<TableRow>();
         for (int i = 0; i < moves.length; i++) {
             for (TableRow row: m_row) {
                 if (row.m_move.equalsIgnoreCase(moves[i])) {
                     row.m_ppUps = ppUps[i];
                     selected.add(row);
                 }
             }
         }
         for (TableRow row : selected) {
             selectMove(row);
         }
     }
 }
