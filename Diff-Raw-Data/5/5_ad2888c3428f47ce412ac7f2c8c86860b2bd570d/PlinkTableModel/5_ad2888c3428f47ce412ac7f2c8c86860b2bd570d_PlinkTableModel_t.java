 package edu.mit.wi.plink;
 
 
 import edu.mit.wi.haploview.Options;
 
 import javax.swing.table.AbstractTableModel;
 import java.util.Vector;
 import java.util.Comparator;
 import java.util.Collections;
 
 public class PlinkTableModel extends AbstractTableModel{
 
     private Vector columnNames;
     private Vector data;
     private Vector filtered;
     private Vector unknownColumns;
     private Vector snps;
 
     private int CHROM_COLUMN = 0;
     private int MARKER_COLUMN = 1;
     private int POSITION_COLUMN = 2;
     private int HAPLOTYPE_COLUMN = -1;
     private int A1_COLUMN = -1;
     private int A2_COLUMN = -1;
     private int FID_COLUMN = -1;
     private int IID_COLUMN = -1;
 
     public PlinkTableModel(Vector c, Vector d){
         columnNames=c;
         data=d;
         unknownColumns = new Vector();
         unknownColumns.add("");
 
         if (Options.getSNPBased()){
             snps = new Vector();
             for (int i = 0; i < d.size(); i++){
                 snps.add(((AssociationResult)d.get(i)).getMarker().getMarkerID());
             }
             for (int j = 3; j < columnNames.size(); j++){
                 String column = (String)columnNames.get(j);
 
                 if (column.equalsIgnoreCase("HAPLOTYPE")){ //TODO: change to starts with?
                     HAPLOTYPE_COLUMN = j;
                 }else if (column.equalsIgnoreCase("A1")){
                     A1_COLUMN = j;
                 }else if (column.equalsIgnoreCase("A2")){
                     A2_COLUMN = j;
                 }
                 unknownColumns.add(column);
             }
         }else{ //not snp based
             CHROM_COLUMN = -1;
             MARKER_COLUMN = -1;
             POSITION_COLUMN = -1;
             for (int i = 0; i < columnNames.size(); i++){
                 String column = (String)columnNames.get(i);
                 if (column.equalsIgnoreCase("FID")){
                     FID_COLUMN = i;
                 }else if (column.equalsIgnoreCase("IID")){
                     IID_COLUMN = i;
                 }else{
                     unknownColumns.add(column);
                 }
             }
         }
 
         filtered = new Vector();
 
         for (int i = 0; i < data.size(); i++){
             filtered.add(new Integer(i));
         }
     }
 
     public String getColumnName(int i){
         return (String)columnNames.elementAt(i);
     }
 
     public Class getColumnClass(int c){
         //things look nicer if we use the String renderer to left align all the cols.
         return String.class;
     }
 
     public int getColumnCount(){
         return columnNames.size();
     }
 
     public int getRowCount(){
         //return data.size();
         return filtered.size();
     }
 
     public Vector getUnknownColumns(){
         return unknownColumns;
     }
 
     public Object getValueAt(int row, int column){
         int realIndex = ((Integer)filtered.get(row)).intValue();
         //AssociationResult result = (AssociationResult)data.get(row);
         AssociationResult result = (AssociationResult)data.get(realIndex);
         Object value;
         if (Options.getSNPBased()){
             Marker marker = result.getMarker();
             if (column == CHROM_COLUMN){
                 value = marker.getChromosome();
             }else if (column == MARKER_COLUMN){
                 value = marker.getMarkerID();
             }else if (column == POSITION_COLUMN){
                 value = new Long(marker.getPosition());
             }else if (column == HAPLOTYPE_COLUMN || column == A1_COLUMN || column == A2_COLUMN){
                 if (result.getValues().size() <= column-3){
                     value = null;
                 }else{
                     if(result.getValues().get(column-3) != null){
                         if (result.getValues().get(column-3) instanceof String){
                             value = result.getValues().get(column-3);
                         }else{
                             value = String.valueOf(((Double)(result.getValues().get(column-3))).intValue());
                         }
                     }else{
                         value = null;
                     }
                 }
             }else{
                 try{
                     if (result.getValues().size() <= column-3){
                         value = null;
                     }else{
                         if(result.getValues().get(column-3) != null){
                             if (result.getValues().get(column-3) instanceof String){
                                 value = result.getValues().get(column-3);
                             }else{
                                 value = result.getValues().get(column-3);
                             }
                         }else{
                             value = null;
                         }
                     }
                 }catch (NumberFormatException nfe){
                     value = result.getValues().get(column-3);
                     if ((value).equals("NA")){
                         value = new Double(Double.NaN);
                     }
                 }
             }
         }else{
            if (column == IID_COLUMN || column == FID_COLUMN){
                 if (result.getValues().get(column) instanceof Double){
                    value = (result.getValues().get(column)).toString();
                 }else{
                     value = result.getValues().get(column);
                 }
             }else{
                 value = result.getValues().get(column);
             }
         }
 
         return value;
     }
 
     public void setValueAt(Object o, int row, int column){
         //This method shouldn't get called...
         //((Vector)data.elementAt(row)).set(column,o);
         fireTableCellUpdated(row, column);
     }
 
     public void filterAll(String chr, int start, int end, String column, String s, String value){
         resetFilters();
         int rows = getRowCount();
         Vector newFiltered = new Vector();
         boolean chromPass = false;
         boolean genericPass = false;
         long realStart = start*1000;
         long realEnd = end*1000;
         int col = -1;
 
         for (int i = 0; i < rows; i++){
             if (!(chr.equals(""))){
                 if (((String)getValueAt(i,CHROM_COLUMN)).equalsIgnoreCase(chr)){
                     if ((((Long)getValueAt(i,POSITION_COLUMN)).longValue() >= realStart) || (start == -1)){
                         if ((((Long)getValueAt(i,POSITION_COLUMN)).longValue() <= realEnd) || (end == -1)){
                             chromPass = true;
                         }
                     }
                 }
             }else{
                 chromPass = true;
             }
 
             if (column != null && s != null && value != null){
                 double rowVal;
                 double val = 0;
                 String stringVal = null;
 
                 try{
                     val = Double.parseDouble(value);
                 }catch (NumberFormatException nfe){
                     stringVal = value;
                 }
 
                 if (col == -1){
                     for (int j = 0; j < columnNames.size(); j++){
                         if(column.equalsIgnoreCase((String)columnNames.get(j))){
                             col = j;
                             break;
                         }
                     }
                 }
 
                 if (col == HAPLOTYPE_COLUMN || col == A1_COLUMN || col == A2_COLUMN){
                     stringVal = value;
                 }
 
                 if (getValueAt(i,col) != null){
                     if (stringVal != null && getValueAt(i,col) instanceof String){
                         if (((String)getValueAt(i,col)).equalsIgnoreCase(stringVal)){
                             genericPass = true;
                         }
                     }else{
                         if (getValueAt(i,col) instanceof Double){
                             rowVal = ((Double)getValueAt(i,col)).doubleValue();
                         }else{
                             rowVal = Double.NaN;
                         }
                         if (s.equals(">=")){
                             if (rowVal >= val){
                                 genericPass = true;
                             }
                         }else if (s.equals(">")){
                             if (rowVal > val){
                                 genericPass = true;
                             }
                         }else if (s.equals("<=")){
                             if (rowVal <= val){
                                 genericPass = true;
                             }
                         }else if (s.equals("<")){
                             if (rowVal < val){
                                 genericPass = true;
                             }
                         }else{
                             if (rowVal == val){
                                 genericPass = true;
                             }
                         }
                     }
                 }
             }else{
                 genericPass = true;
             }
 
 
             if (chromPass && genericPass){
                 newFiltered.add(new Integer(i));
             }
 
             chromPass = false;
             genericPass = false;
         }
         filtered = newFiltered;
     }
 
     public void resetFilters(){
         filtered = new Vector();
 
         Collections.sort(data,new IndexComparator());
 
         for (int i = 0; i < data.size(); i++){
             filtered.add(new Integer(i));
         }
     }
 
     public Vector getSNPs(){
         return snps;
     }
 
     public int getFIDColumn(){
         return FID_COLUMN;
     }
 
     public int getIIDColumn(){
         return IID_COLUMN;
     }
 
     class IndexComparator implements Comparator {
         public int compare (Object o1, Object o2) {
             Integer i1 = new Integer(((AssociationResult)o1).getIndex());
             Integer i2 = new Integer(((AssociationResult)o2).getIndex());
             return i1.compareTo(i2);
         }
     }
 }
