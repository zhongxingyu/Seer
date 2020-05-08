 package edacc;
 
 import javax.swing.JTable;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.table.TableModel;
 
 /**
  *
  * @author simon
  */
 public class JTableTooltipInformation extends JTable {
 
     private String defaultToolTip;
 
     public JTableTooltipInformation() {
         super();
         defaultToolTip = "";
         this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 updateToolTipText();
             }
         });
     }
 
     public JTableTooltipInformation(TableModel tableModel) {
         super(tableModel);
     }
 
     @Override
     public void setToolTipText(String text) {
         defaultToolTip = text;
         updateToolTipText();
     }
     
     
 
     private void updateToolTipText() {
        int rows = JTableTooltipInformation.this.getSelectedRowCount();
         String text = "<html>";
        if (!"".equals(defaultToolTip)) {
             text += defaultToolTip + "<br/>";
         }
         text += rows + " / " + JTableTooltipInformation.this.getRowCount() + " selected</html>";
         JTableTooltipInformation.super.setToolTipText(text);
     }
 
     @Override
     public void tableChanged(TableModelEvent e) {
         super.tableChanged(e);
         updateToolTipText();
     }
 }
