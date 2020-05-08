 /* BattleTableModel.java
  *
  * This file is a part of Shoddy Battle.
  * Copyright (C) 2009  Catherine Fitzpatrick and Benjamin Gwin
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
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, visit the Free Software Foundation, Inc.
  * online at http://gnu.org.
  */
 
 package shoddybattleclient.utils;
 
 import javax.swing.table.AbstractTableModel;
 import java.util.*;
 import shoddybattleclient.network.ServerLink;
 import shoddybattleclient.network.ServerLink.Metagame;
 import shoddybattleclient.utils.SortableJTable.SortableTableModel;
 
 public class BattleTableModel extends AbstractTableModel implements SortableTableModel {
 
     private class TableRow {
         private Integer m_id;
         private Integer m_ladder;
         private boolean m_rated;
         private String m_user1;
         private String m_user2;
         private Integer m_pop;
         private Integer m_n;
 
         public TableRow(int id, int ladder, boolean rated, String u1,
                 String u2, int n, int pop) {
             m_id = id;
             m_ladder = ladder;
             m_rated = rated;
             m_user1 = u1;
             m_user2 = u2;
             m_pop = pop;
             m_n = n;
         }
     }
 
     private ServerLink m_link;
     private List<TableRow> m_rows = new ArrayList<TableRow>();
 
     private static final String[] HEADERS = { "Players", "Metagame", "Rated?",
             "N", "Population" };
 
     public BattleTableModel(ServerLink link) {
         m_link = link;
     }
 
     @Override
     public String getColumnName(int col) {
         return HEADERS[col];
     }
 
     public int getRowCount() {
         return m_rows.size();
     }
 
     public int getColumnCount() {
         return HEADERS.length;
     }
 
     public Object getValueAt(int rowIndex, int columnIndex) {
         TableRow row = m_rows.get(rowIndex);
         switch (columnIndex) {
             case 0:
                 return row.m_user1 + " v. " + row.m_user2;
             case 1:
                 if (row.m_ladder != -1) {
                     Metagame[] metagames = m_link.getMetagames();
                     return metagames[row.m_ladder].getName();
                 }
                 return "(Custom)";
             case 2:
                 return row.m_rated ? "Yes" : "No";
             case 3:
                 return row.m_n;
             case 4:
                 return row.m_pop;
         }
         assert false;
         return null;
     }
 
     @Override
     public boolean isCellEditable(int row, int col) {
         return false;
     }
 
     public void addBattle(int id, int ladder, boolean rated, String u1,
             String u2, int n, int pop) {
         m_rows.add(new TableRow(id, ladder, rated, u1, u2, n, pop));
         fireTableDataChanged();
     }
 
     public int getId(int row) {
         if ((row < 0) || row >= m_rows.size()) return -1;
         return m_rows.get(row).m_id;
     }
 
     public void sort(final int col, boolean reverse) {
         Collections.sort(m_rows, new Comparator<TableRow>() {
             public int compare(TableRow t1, TableRow t2) {
                 switch (col) {
                     case 0:
                         return t1.m_user1.compareToIgnoreCase(t2.m_user1);
                     //todo: change this
                     case 1:
                         return t1.m_ladder.compareTo(t2.m_ladder);
                     case 2:
                        boolean r1 = t1.m_rated;
                        boolean r2 = t2.m_rated;
                        return r1 ? (r2 ? 0 : 1) : (r2 ? -1 : 0);
                     case 3:
                        return t1.m_n.compareTo(t2.m_n);
                    case 4:
                         return t1.m_pop.compareTo(t2.m_pop);
                 }
                 assert false;
                 return 0;
             }
         });
         if (reverse) Collections.reverse(m_rows);
     }
 }
