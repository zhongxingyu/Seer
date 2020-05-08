 // %1117664849171:hoplugins.conv%
 package hoplugins.conv;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author Thorsten Dietz
  */
 public class SelectObjectDialog extends JDialog {
     //~ Instance fields ----------------------------------------------------------------------------
 
     /** TODO Missing Parameter Documentation */
     protected JTable table;
 
     /** TODO Missing Parameter Documentation */
     protected String colOneName;
 
     /** TODO Missing Parameter Documentation */
     protected String colTwoName;
     private Object[] object;
     private Object[] selectedObjects = new Object[0];
     private boolean defaultSelected;
     private boolean isSaved = false;
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new SelectObjectDialog object.
      *
      * @param frame TODO Missing Constructuor Parameter Documentation
      * @param dialogWidth TODO Missing Constructuor Parameter Documentation
      * @param dialogHeight TODO Missing Constructuor Parameter Documentation
      * @param object TODO Missing Constructuor Parameter Documentation
      */
     public SelectObjectDialog(JFrame frame, int dialogWidth, int dialogHeight, Object[] object) {
         super(frame, "", true);
         this.object = object;
         this.colOneName = "Export";
         this.colTwoName = "HRF"; //colTwoName;
         setLocation(frame.getX() + ((frame.getWidth() - dialogWidth) / 2),
                     frame.getY() + ((frame.getHeight() - dialogHeight) / 2));
         setSize(dialogWidth, dialogHeight);
         inizialize();
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * TODO Missing Method Documentation
      *
      * @param b TODO Missing Method Parameter Documentation
      */
     protected void setDefaultSelected(boolean b) {
         defaultSelected = b;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     protected boolean isDefaultSelected() {
         return defaultSelected;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param selected TODO Missing Method Parameter Documentation
      * @param columnNames TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     protected SelectObjectTableModel getModel(boolean selected, String[] columnNames) {
         Object[][] value = new Object[object.length][2];
 
         for (int i = 0; i < object.length; i++) {
             value[i][0] = new Boolean(selected);
             value[i][1] = object[i];
         }
 
         SelectObjectTableModel model = new SelectObjectTableModel(value, columnNames);
         return model;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     protected boolean isSaved() {
         return isSaved;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     protected Object[] getSelectedObjects() {
         return selectedObjects;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     protected JTable getTable() {
         return table;
     }
 
     /**
      * Erweitert Object-Array
      *
      * @param o TODO Missing Constructuor Parameter Documentation
      */
     protected void addSelectedObject(Object o) {
         Object[] objectNew = new Object[selectedObjects.length + 1];
 
         for (int i = 0; i < selectedObjects.length; i++) {
             objectNew[i] = selectedObjects[i];
         }
 
         objectNew[selectedObjects.length] = o;
         selectedObjects = objectNew;
     }
 
     /**
      * TODO Missing Method Documentation
      */
     protected void cancel() {
         isSaved = false;
         this.dispose();
     }
 
     /**
     * Speichert ausgewählte Objekte in einem Array
      */
     protected void findSelectedObjects() {
         if (table != null) {
             for (int i = 0; i < table.getRowCount(); i++) {
                 boolean selected = ((Boolean) table.getValueAt(i, 0)).booleanValue();
 
                 if (selected) {
                     addSelectedObject(table.getValueAt(i, 1));
                 }
             }
 
             isSaved = true;
         }
 
         this.dispose();
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private JPanel createButtons() {
         JPanel buttonPanel = new JPanel();
         ((FlowLayout) buttonPanel.getLayout()).setAlignment(FlowLayout.RIGHT);
 
         JButton okButton = new JButton(RSC.MINIMODEL.getResource().getProperty("Speichern"));
         okButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     findSelectedObjects();
                 }
             });
 
         JButton cancelButton = new JButton(RSC.PROP_CANCEL);
         cancelButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     cancel();
                 }
             });
 
         JButton selectAllButton = new JButton(new ImageIcon(SelectObjectDialog.class.getResource("rsc/CheckBoxSelected.gif")));
         selectAllButton.setBorderPainted(false);
         selectAllButton.setPreferredSize(new Dimension(18, 19));
         selectAllButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     String[] columnNames = {colOneName, colTwoName};
                     int[] columnWidth = {10, 100,};
                     table.setModel(getModel(true, columnNames));
 
                     for (int i = 0; i < columnNames.length; i++) {
                         table.getColumn(columnNames[i]).setPreferredWidth(columnWidth[i]);
                     }
 
                     table.repaint();
                 }
             });
 
         JButton selectNoneButton = new JButton(new ImageIcon(SelectObjectDialog.class.getResource("rsc/CheckBoxNotSelected.gif")));
         selectNoneButton.setBorderPainted(false);
         selectNoneButton.setPreferredSize(new Dimension(18, 19));
         selectNoneButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     String[] columnNames = {colOneName, colTwoName};
                     int[] columnWidth = {10, 100,};
                     table.setModel(getModel(false, columnNames));
 
                     for (int i = 0; i < columnNames.length; i++) {
                         table.getColumn(columnNames[i]).setPreferredWidth(columnWidth[i]);
                     }
 
                     table.repaint();
                 }
             });
 
         okButton.setPreferredSize(cancelButton.getPreferredSize());
         buttonPanel.add(selectAllButton);
         buttonPanel.add(selectNoneButton);
         buttonPanel.add(okButton);
         buttonPanel.add(cancelButton);
         return buttonPanel;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private JScrollPane createTable() {
         String[] columnNames = {colOneName, colTwoName};
         int[] columnWidth = {10, 100,};
 
         table = new JTable(getModel(defaultSelected, columnNames));
 
         for (int i = 0; i < columnNames.length; i++) {
             table.getColumn(columnNames[i]).setPreferredWidth(columnWidth[i]);
         }
 
         JScrollPane scroll = new JScrollPane(table);
         return scroll;
     }
 
     /**
      * TODO Missing Method Documentation
      */
     private void inizialize() {
         Container contenPane = getContentPane();
         contenPane.add(createTable());
         contenPane.add(createButtons(), BorderLayout.SOUTH);
     }
 }
