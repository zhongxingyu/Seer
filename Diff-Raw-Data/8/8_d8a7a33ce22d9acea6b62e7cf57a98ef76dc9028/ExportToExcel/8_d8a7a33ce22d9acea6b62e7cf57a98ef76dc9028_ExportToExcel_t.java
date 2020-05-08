 /*
  * ExportToExcel.java
  *
  * Created on Dec 13, 2010, 1:28:25 PM
  */
 
 package mysqljavacat.dialogs;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.swing.JTable;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.table.TableModel;
 import mysqljavacat.MysqlJavaCatApp;
 import mysqljavacat.MysqlJavaCatView;
 import org.jdesktop.application.Task;
 
 /**
  *
  * @author strelok
  */
 public class ExportToExcel extends javax.swing.JDialog {
 
     /** Creates new form ExportToExcel */
     public ExportToExcel(java.awt.Frame parent, boolean modal) {
         super(parent, modal);
         initComponents();
         jFileChooser1.addChoosableFileFilter(new FileFilter() {
 
             @Override
             public boolean accept(File f) {
                 return f.getName().endsWith(".xml");
             }
 
             @Override
             public String getDescription() {
                 return "XML file";
             }
         });
         final ExportToExcel dialog = this;
         jFileChooser1.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
 
                 if(e.getActionCommand().equals("CancelSelection")){
                     dialog.dispose();
                 }else if(e.getActionCommand().equals("ApproveSelection")){
                     Task task = new Task(MysqlJavaCatApp.getApplication()) {
                         @Override
                         protected Object doInBackground() throws Exception {
                             MysqlJavaCatView view = MysqlJavaCatApp.getApplication().getView();
                             PrintWriter fw = null;
                             try {
                                 fw = new PrintWriter(jFileChooser1.getSelectedFile());
                                 StringBuffer out = new StringBuffer("<?xml version=\"1.0\"?>\n"
                                     +"<?mso-application progid=\"Excel.Sheet\"?>\n"
                                     +"<Workbook\n"
                                     +"xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\n"
                                     +"xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n"
                                     +"xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">\n"
                                     +"<Worksheet ss:Name=\"Sheet1\">\n"
                                     +"<ss:Table>\n");
                                 JTable table = view.getResultTable();
                                 TableModel model  = table.getModel();
                                 out.append("<ss:Row>\n");
                                 for(int col = 1;col <= model.getColumnCount();col = col + 1){
                                     out.append("<ss:Cell><ss:Data ss:Type=\"String\">");
                                      out.append(model.getColumnName(col));
                                     out.append("</ss:Data></ss:Cell>\n");
                                 }
                                 out.append("</ss:Row>\n");
                                 fw.write(out.toString());
                                 dialog.dispose();
                                 out = new StringBuffer();
                                 int buffer = 0;
                                 for(int row = 0;row < model.getRowCount();row = row + 1){
                                      out.append("<ss:Row>\n");
                                      for(int col = 0;col < model.getColumnCount();col = col + 1){
                                          out.append("<ss:Cell><Data ss:Type=\"String\">");
                                          out.append(model.getValueAt(row, col));
                                          out.append("</Data></ss:Cell>\n");
                                      }
                                      out.append("</ss:Row>\n");
                                      if(buffer >= 10000){
                                         fw.write(out.toString());
                                         buffer = 0;
                                         out = new StringBuffer();
                                      }
                                      buffer = buffer + 1;
                                      setMessage("Exported: " + row);
                                 }
                                out.append("</ss:Table>\n"
                                     + "</Worksheet>\n"
                                     + "</Workbook>\n");
                                 fw.write(out.toString());
                                 fw.close();
                             } catch (IOException ex) {
                                 MysqlJavaCatApp.getApplication().showError(ex.getMessage());
                             }
                             return null;
                         }
                  };
                  MysqlJavaCatApp.getApplication().getContext().getTaskService().execute(task);
                  MysqlJavaCatApp.getApplication().getContext().getTaskMonitor().setForegroundTask(task);
                 }
             };
         });
         
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jFileChooser1 = new javax.swing.JFileChooser();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setName("Form"); // NOI18N
 
         jFileChooser1.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
         jFileChooser1.setName("jFileChooser1"); // NOI18N
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jFileChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, 716, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jFileChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     /**
     * @param args the command line arguments
     */
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JFileChooser jFileChooser1;
     // End of variables declaration//GEN-END:variables
 
 }
