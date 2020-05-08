 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package edacc.manageDB;
 
 import edacc.model.ResultCode;
import edacc.model.ResultCodeDAO;
import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
import java.util.List;
 import javax.swing.table.AbstractTableModel;
import sun.security.jca.GetInstance;
 
 /**
  *
  * @author dgall
  */
 public class ResultCodeTableModel extends AbstractTableModel {
 
     private static ResultCodeTableModel instance = null;
     private ArrayList<ResultCode> resultCodes;
     private static final int RESULT_CODE = 0;
     private static final int DESCRIPTION = 1;
 
     public static ResultCodeTableModel getInstance() {
         if (instance == null)
             instance = new ResultCodeTableModel();
         return instance;
     }
 
     private ResultCodeTableModel() {
         resultCodes = new ArrayList<ResultCode>();
     }
 
     @Override
     public int getRowCount() {
         return resultCodes.size();
     }
 
     @Override
     public int getColumnCount() {
         return 2;
     }
 
     @Override
     public Object getValueAt(int rowIndex, int columnIndex) {
         switch (columnIndex) {
             case RESULT_CODE:
                 return resultCodes.get(rowIndex).getResultCode();
             case DESCRIPTION:
                 return resultCodes.get(rowIndex).getDescription();
             default:
                 return null;
         }
     }
 
     public void addList(Collection<ResultCode> add) {
         resultCodes.addAll(add);
         fireTableDataChanged();
     }
     
     @Override
     public String getColumnName(int c) {
         switch (c) {
             case RESULT_CODE:
                 return "ResultCode";
             case DESCRIPTION:
                 return "Description";
             default: return "";
         }
     }
 
     public void clear() {
         resultCodes.clear();
         fireTableDataChanged();
     }
 
     public void add(ResultCode add) {
         resultCodes.add(add);
         fireTableDataChanged();
     }
 
     public ResultCode getResultCode(int i) {
         return resultCodes.get(i);
     }
 
     void removeAll(Collection<ResultCode> rcs) {
         resultCodes.removeAll(rcs);
         fireTableDataChanged();
     }
 }
