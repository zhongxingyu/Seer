 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package edacc.experiment;
 
 import edacc.model.ExperimentHasInstance;
 import edacc.model.Instance;
 import javax.swing.table.AbstractTableModel;
 import java.util.Vector;
 
 /**
  *
  * @author daniel
  */
 public class InstanceTableModel extends AbstractTableModel {
     private String[] columns = {"Name", "numAtoms", "numClauses", "ratio", "maxClauseLength", "selected"};
     protected Vector<Instance> instances;
     protected ExperimentHasInstance[] experimentHasInstances;
     protected Boolean[] selected;
 
     public void setInstances(Vector<Instance> instances) {
         this.instances = instances;
         experimentHasInstances = new ExperimentHasInstance[instances.size()];
         selected = new Boolean[instances.size()];
         for (int i = 0; i < selected.length; i++) {
             selected[i] = false;
         }
         this.fireTableDataChanged();
     }
 
     public void setExperimentHasInstances(Vector<ExperimentHasInstance> experimentHasInstances) {
         for (int i = 0; i < instances.size(); i++) {
             this.setValueAt(false, i, 5);
         }
         for (int i = 0; i < experimentHasInstances.size(); i++) {
             for (int j = 0; j < instances.size(); j++) {
                 if (instances.get(j).getId() == experimentHasInstances.get(i).getInstances_id()) {
                     this.experimentHasInstances[j] = experimentHasInstances.get(i);
                     this.selected[j] = true;
                     break;
                 }
             }
         }
         this.fireTableDataChanged();
     }
 
     public void setExperimentHasInstance(ExperimentHasInstance e, int row) {
         this.experimentHasInstances[row] = e;
     }
 
     public InstanceTableModel() {
         this.instances = new Vector<Instance>();
     }
 
     public int getRowCount() {
         return instances.size();
     }
 
     public int getColumnCount() {
         return columns.length;
     }
 
     @Override
     public String getColumnName(int col) {
         return columns[col];
     }
 
     @Override
     public Class getColumnClass(int col) {
        return getValueAt(0, col).getClass();
     }
 
     @Override
     public boolean isCellEditable(int row, int col) {
         if (col == 5) return true;
         else return false;
     }
 
     @Override
     public void setValueAt(Object value, int row, int col) {
         if (col == 5) selected[row] = (Boolean)value;
         fireTableCellUpdated(row, col);
     }
 
 
 
     public Object getValueAt(int rowIndex, int columnIndex) {
         switch (columnIndex) {
             case 0:
                 return instances.get(rowIndex).getName();
             case 1:
                 return instances.get(rowIndex).getNumAtoms();
             case 2:
                 return instances.get(rowIndex).getNumClauses();
             case 3:
                 return instances.get(rowIndex).getRatio();
             case 4:
                 return instances.get(rowIndex).getMaxClauseLength();
             case 5:
                 return selected[rowIndex];
             case 6:
                 return experimentHasInstances[rowIndex];
             case 7:
                 return instances.get(rowIndex);
             default:
                 return "";
         }
     }
 
 }
