 package ibfb.germplasmlist.models;
 
 import ibfb.domain.core.Factor;
 import ibfb.domain.core.Workbook;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import javax.swing.table.AbstractTableModel;
 
 public class GermplasmEntriesTableModelChecks extends AbstractTableModel {
 
     private static final String TRIAL_INSTANCE = "TRIAL INSTANCE";
     private static final String NUMBER = "NUMBER";
     public static final String TRIAL = "TRIALINSTANCENUMBER";
     public static final String ENTRY = "GERMPLASMENTRYNUMBER";
     public static final String ENTRY_CODE = "GERMPLASMENTRYCODE";
     public static final String DESIG = "GERMPLASMIDDBCV";
     public static final String GID = "GERMPLASMIDDBID";
     public static final String CROSS = "CROSSHISTORYPEDIGREESTRING";
     public static final String CROSS_NAME = "CROSSNAMENAME";
     public static final String SOURCE = "SEEDSOURCENAME";
     public static final String PLOT = "FIELDPLOTNESTEDNUMBER";
     public static final String PLOT_NESTED = "PLOTNESTEDNUMBER";
     public static final String PLOTNUMBER = "FIELDPLOTNUMBER";
     public static final String REPLICATION = "REPLICATIONNUMBER";
     public static final String BLOCK = "BLOCKNUMBER";
     public static final String BLOCK_NESTED = "BLOCKNESTEDNUMBER";
     public static final String ROW = "ROWINLAYOUTNUMBER";
     public static final String COL = "COLUMNINLAYOUTNUMBER";
     private boolean hasChecks = false;
     private List<Factor> factorHeaders;
     private List<List<Object>> germplasmData;
     private String[] checkHeaders = {"Initial position", "Frequency"};
     private boolean withColor = false;
     /**
      * List of items containing all headers. Items in list can be Factor o
      * Variates
      */
     private List<Object> headers;
     /**
      * To easy retrieving of column indexes
      */
     private HashMap<String, Integer> headerIndex = new HashMap<String, Integer>();
 
     public GermplasmEntriesTableModelChecks() {
         clearTable();
     }
 
     public GermplasmEntriesTableModelChecks(List<Factor> factorHeaders, List<List<Object>> germplasmData) {
         this.factorHeaders = factorHeaders;
         this.germplasmData = germplasmData;
         assignHeaders();
 
     }
 
     /**
      * Assign headers from template
      */
     private void assignHeaders() {
         headers = new ArrayList<Object>();
         int columnIndex = 0;
         // add headers from factor section which are TRIAL
         for (Factor factor : factorHeaders) {
             headers.add(factor);
             headerIndex.put(Workbook.getStringWithOutBlanks(factor.getProperty() + factor.getScale()), columnIndex);
             columnIndex++;
         }
 
 
     }
 
     @Override
     public boolean isCellEditable(int rowIndex, int columnIndex) {
         if (!hasChecks) {
             return false;
         } else {
             if (columnIndex > factorHeaders.size() - 1) {
                 return false;  //true if position and frequency will be editables
             }
         }
 
         return super.isCellEditable(rowIndex, columnIndex);
     }
 
     public void setHasChecks(boolean hasChk) {
         this.hasChecks = hasChk;
     }
 
     @Override
     public int getRowCount() {
         return germplasmData.size();
     }
 
     @Override
     public int getColumnCount() {
         if (!hasChecks) {
             return factorHeaders.size();
         } else {
             return factorHeaders.size() + 2;
         }
     }
 
     @Override
     public String getColumnName(int column) {
         if (!hasChecks) {
             return factorHeaders.get(column).getFactorName();
         } else {
 
             if (column < factorHeaders.size()) {
                 return factorHeaders.get(column).getFactorName();
             } else {
                 return checkHeaders[column - factorHeaders.size()];
             }
 
         }
 
 
     }
 
     @Override
     public Object getValueAt(int rowIndex, int columnIndex) {
         List<Object> columnValues = germplasmData.get(rowIndex);
        return columnValues.get(columnIndex);
     }
 
     @Override
     public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
 
         if (hasChecks) {
 
             List<Object> columnValues = germplasmData.get(rowIndex);
 
             columnValues.set(columnIndex, aValue);
 
 
             fireTableCellUpdated(rowIndex, columnIndex);
         } else {
         }
 
 
 
     }
 
     public List<Factor> getFactorHeaders() {
         return factorHeaders;
     }
 
     public List<List<Object>> getGermplasmData() {
         return germplasmData;
     }
 
     public void clearTable() {
         factorHeaders = new ArrayList<Factor>();
         germplasmData = new ArrayList<List<Object>>();
         fireTableDataChanged();
     }
 
     /**
      * Get the column index for header
      *
      * @param columnName Column name to search
      * @return column index number greater than 0 if found or -1 if not found
      */
     public int getHeaderIndex(String columnName) {
         int columnIndex = -1;
         if (headerIndex.get(columnName) != null) {
             columnIndex = headerIndex.get(columnName);
         }
         return columnIndex;
     }
 }
