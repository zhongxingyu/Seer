 package edu.duke.cabig.catrip.gui.util;
 
 import java.io.File;
 import java.io.FileWriter;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.table.TableModel;
 
 /**
  *
  * @author Sanjeev Agarwal
  */
 public class CSVResultExporter {
     private static JFrame owner ; 
     
     public static void exportToCSV(JTable table, JFrame owner_){ 
         owner = owner_;
         if(GUIConstants.resultAvailable){
             export(table);
         }
     }
     
     
     public static void export(JTable table){
         try {
             
             String file = GUIConstants.CATRIP_HOME+File.separator+"caTRIP_Query_Results.csv";
             
             TableModel model = table.getModel();
             FileWriter out = new FileWriter(file);
             
             int rows = model.getRowCount();
             int columns = model.getColumnCount();
             
             out.write("\n");
             for(int i=0; i<columns;i++) {
                 out.write(model.getColumnName(i));out.write(",");
             }
             
             out.write("\n");
             for(int i=0; i< model.getRowCount();i++){
                 for(int j=0;j< model.getColumnCount();j++){
                     String val = "-";
                     if ((model.getValueAt(i, j) != null) && !(model.getValueAt(i, j).toString().equals(" ")) ){
                         val = model.getValueAt(i, j).toString();
                     }
                     out.write(val);
                     out.write(",");
                 }
                 out.write("\n");
             }
             
             
             JOptionPane jpane = new JOptionPane();
            jpane.showMessageDialog(owner ,"The results are exported to HTML file :\n"+file);
             
             
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     
     
     
     
     
     
 }
