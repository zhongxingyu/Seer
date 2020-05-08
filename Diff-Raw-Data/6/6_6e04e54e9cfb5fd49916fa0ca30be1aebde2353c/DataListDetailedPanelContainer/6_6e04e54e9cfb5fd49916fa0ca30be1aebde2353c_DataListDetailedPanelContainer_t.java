 package edu.wustl.cab2b.client.ui.viewresults;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 
 import edu.wustl.cab2b.client.ui.RiverLayout;
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bFileFilter;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 
 /**
  * Contains of the components in data list detailed section
  * @author rahul_ner
  */
 public class DataListDetailedPanelContainer extends Cab2bPanel implements ActionListener {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     Cab2bButton m_exportButton;
 
     Cab2bPanel dataListDetailedPanel;
 
     public DataListDetailedPanelContainer() {
 
     }
 
     public DataListDetailedPanelContainer(Cab2bPanel dataListDetailedPanel) {
         this.dataListDetailedPanel = dataListDetailedPanel;
         initGUI();
     }
 
     private void initGUI() {
         this.setLayout(new RiverLayout());
         this.add("br hfill vfill", dataListDetailedPanel);
 
         m_exportButton = new Cab2bButton("Export");
         this.add("br", this.m_exportButton);
         m_exportButton.addActionListener(this);
         this.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 220)));
     }
 
     /**
      * Method to perform Button click events
      */
     public void actionPerformed(ActionEvent event) {
         if (event.getSource() == m_exportButton) {
             // Perform export operation
             performExportOperation();
         }
     }
 
     /**
      * Method to perform export operation on data list
      * It prompt user for specifying file name and
      * then saves selected rows into it in the from of .csv
      */
     private void performExportOperation() {
 
         DataListDetailedPanelInterface dataListDetailedPanel = (DataListDetailedPanelInterface) this.dataListDetailedPanel;
 
         if (dataListDetailedPanel.getNoOfSelectedRows() == 0) {
             JOptionPane.showMessageDialog(this, "No data set is selected for export", "Data List Export Warning",
                                           JOptionPane.WARNING_MESSAGE);
             return;
         }
         boolean done = false;
         do {
             JFileChooser fileChooser = new JFileChooser();
             fileChooser.setFileFilter(new Cab2bFileFilter(new String[] { "csv" }));
             int status = fileChooser.showSaveDialog(this);
             if (JFileChooser.APPROVE_OPTION == status) {
                File selFile = fileChooser.getSelectedFile();
                String fileName = selFile.getAbsolutePath();

                 if (true == selFile.exists()) {
                     // Prompt user to confirm if he wants to override the value 
                     int confirmationValue = JOptionPane.showConfirmDialog(fileChooser, "The file "
                             + selFile.getName() + " already exists.\nDo you want to replace existing file?",
                                                                           "caB2B Confirmation",
                                                                           JOptionPane.YES_NO_OPTION,
                                                                           JOptionPane.WARNING_MESSAGE);
                     if (confirmationValue == JOptionPane.NO_OPTION)
                         continue;
                 } else {
                     if (false == fileName.endsWith(".csv")) {
                         fileName = fileName + ".csv";
                     }
                 }
                 BufferedWriter out = null;
                 try {
                     out = new BufferedWriter(new FileWriter(fileName));
 
                     String csvString = dataListDetailedPanel.getCSVData();
                     out.write(csvString);
 
                 } catch (IOException e) {
                     CommonUtils.handleException(e, this, true, true, true, false);
                 } finally {
                     try {
                         out.close();
                     } catch (IOException e) {
                         CommonUtils.handleException(e, this, true, true, true, false);
                     }
                 }
             }
             done = true;
         } while (!done);
 
     }
 }
