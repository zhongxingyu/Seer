 /*
  Copyright (c) 2009-2013 Flavio Miguel ABREU ARAUJO (flavio.abreuaraujo@uclouvain.be)
                          Yannick GILLET (yannick.gillet@uclouvain.be)
 
 Universit catholique de Louvain, Louvain-la-Neuve, Belgium
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 
 1. Redistributions of source code must retain the above copyright
 notice, this list of conditions, and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions, and the disclaimer that follows
 these conditions in the documentation and/or other materials
 provided with the distribution.
 
 3. The names of the author may not be used to endorse or promote
 products derived from this software without specific prior written
 permission.
 
 In addition, we request (but do not require) that you include in the
 end-user documentation provided with the redistribution and/or in the
 software itself an acknowledgement equivalent to the following:
 "This product includes software developed by the
 Abinit Project (http://www.abinit.org/)."
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE JDOM AUTHORS OR THE PROJECT
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.
 
 For more information on the Abinit Project, please see
 <http://www.abinit.org/>.
  */
 
 package abinitgui;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import javax.swing.JTable;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 
 //@SuppressWarnings("serial")
 class ScriptTableModel extends AbstractTableModel {
 
     private String[] columnNames = null;
     private ArrayList<ScriptArgument> data;
     private JTable table = null;
 
     public ScriptTableModel(JTable table) {
         super();
         this.table = table;
         data = new ArrayList<ScriptArgument>();
     }
 
     @Override
     public int getColumnCount() {
         if (getRowCount() == 0) {
             return 0;
         } else {
             return 2;
         }
     }
 
     @Override
     public int getRowCount() {
         return data.size();
     }
 
     @Override
     public String getColumnName(int col) {
         return columnNames[col];
     }
 
     @Override
     public Object getValueAt(int row, int col) {
         switch(col)
         {
             case 0:
                 return data.get(row);
             case 1:
                 return data.get(row).value;
             default:
                 return null;
         }
     }
 
     @Override
     public Class getColumnClass(int c) {
         return getValueAt(0, c).getClass();
     }
 
     @Override
     public boolean isCellEditable(int row, int col) {
         return col == 1;
     }
 
     @Override
     public void setValueAt(Object value, int row, int col) {
         switch(col)
         {
             case 0:
                 return; // Not possible to modify name
             case 1:
                 data.get(row).value = (String)value;
         }
         fireTableCellUpdated(row, col);
     }
 
     void setHeader(String[] header) {
         if (header == null) {
             columnNames = null;
         } else {
             columnNames = header;
             TableColumnModel tcm = table.getTableHeader().getColumnModel();
             Enumeration<TableColumn> cols = tcm.getColumns();
             for (int i = 0; cols.hasMoreElements(); i++) {
                 TableColumn tc = cols.nextElement();
                 tc.setHeaderValue(columnNames[i]);
             }
         }
     }
 
     void addScript(ScriptArgument data) {
         this.data.add(data);
        if (table.isEditing())
        {
            table.getCellEditor().cancelCellEditing();
        }   
         this.table.updateUI();
     }
     
     void resetScripts()
     {
         this.data.clear();
        if (table.isEditing())
        {
            table.getCellEditor().cancelCellEditing();
        }   
         this.table.updateUI();
     }
 }
