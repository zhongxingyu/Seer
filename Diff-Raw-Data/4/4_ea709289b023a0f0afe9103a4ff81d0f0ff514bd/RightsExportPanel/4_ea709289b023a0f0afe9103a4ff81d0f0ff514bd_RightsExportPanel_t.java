 package org.sola.clients.swing.ui.administrative;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.util.List;
 import javax.swing.JFileChooser;
 import javax.swing.JFormattedTextField;
 import javax.swing.filechooser.FileFilter;
 import org.sola.clients.beans.administrative.RightsExportResultBean;
 import org.sola.clients.beans.administrative.RightsExportResultListBean;
 import org.sola.clients.beans.administrative.RrrBean;
 import org.sola.clients.beans.referencedata.RrrTypeBean;
 import org.sola.clients.beans.referencedata.RrrTypeListBean;
 import org.sola.clients.swing.common.controls.CalendarForm;
 import org.sola.clients.swing.common.tasks.SolaTask;
 import org.sola.clients.swing.common.tasks.TaskManager;
 import org.sola.clients.swing.ui.renderers.FormattersFactory;
 import org.sola.common.FileUtility;
 import org.sola.common.messaging.ClientMessage;
 import org.sola.common.messaging.MessageUtility;
 
 /**
  * Panel to search rights and export results into CSV file.
  */
 public class RightsExportPanel extends javax.swing.JPanel {
 
     private RrrTypeListBean createRightTypes() {
         if (rrrTypes == null) {
             rrrTypes = new RrrTypeListBean(true);
         }
         return rrrTypes;
     }
 
     /**
      * Default constructor
      */
     public RightsExportPanel() {
         initComponents();
         postInit();
     }
 
     private void postInit() {
         rightsExportResults.addPropertyChangeListener(new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 if (evt.getPropertyName().equals(RightsExportResultListBean.LIST_ITEM_CHECKED)) {
                     customizeExportSelectedButtons((Boolean) evt.getNewValue());
                 }
             }
         });
         customizeExportAllButtons(false);
         customizeExportSelectedButtons(false);
     }
 
     private void customizeExportSelectedButtons(boolean checked) {
         btnExportSelected.setEnabled(checked);
     }
 
     private void customizeExportAllButtons(boolean enabled) {
         btnExportAll.setEnabled(enabled);
     }
 
     private void clean() {
         txtDateFrom.setValue(null);
         txtDateTo.setValue(null);
         rightsExportResults.getRightsList().clear();
         customizeExportAllButtons(false);
         customizeExportSelectedButtons(false);
         lblRecords.setText("0");
     }
 
     private void search() {
         SolaTask t = new SolaTask<Void, Void>() {
             @Override
             public Void doTask() {
                 setMessage(MessageUtility.getLocalizedMessageText(ClientMessage.PROGRESS_MSG_SEARCH_RIGHTS));
                 // Set type to lease
                 rightsExportParams.setRightTypeCode(RrrBean.CODE_LEASE);
                 rightsExportResults.search(rightsExportParams);
                 return null;
             }
 
             @Override
             public void taskDone() {
                 if (rightsExportResults.getRightsList().size() < 1) {
                     customizeExportAllButtons(false);
                     customizeExportSelectedButtons(false);
                     lblRecords.setText("0");
                     MessageUtility.displayMessage(ClientMessage.SEARCH_NO_RESULTS);
                 } else {
                     lblRecords.setText(String.valueOf(rightsExportResults.getRightsList().size()));
                     customizeExportAllButtons(true);
                 }
             }
         };
         TaskManager.getInstance().runTask(t);
     }
 
     private void export(final List<RightsExportResultBean> list) {
         if (list == null || list.size() < 1) {
             return;
         }
 
         File f = new File("//hr-fmsmain/SOLAexports");
         JFileChooser jfc;
 
         if(f.exists()){
             jfc = new JFileChooser(f);
         } else {
             jfc = new JFileChooser();
         }
 
         jfc.removeChoosableFileFilter(jfc.getFileFilter());
         jfc.setAcceptAllFileFilterUsed(false);
         jfc.setFileFilter(new FileFilter() {
             @Override
             public boolean accept(File f) {
                if(f.isDirectory()){
                    return true;
                }
                
                 String extension = FileUtility.getFileExtension(f);
                 if (extension != null) {
                     if (extension.equalsIgnoreCase(FileUtility.csv)) {
                         return true;
                     } else {
                         return false;
                     }
                 }
                 return false;
             }
 
             @Override
             public String getDescription() {
                 return "CSV files";
             }
         });
 
         int result = jfc.showSaveDialog(this);
         if (result == JFileChooser.CANCEL_OPTION) {
             return;
         }
 
         final File file;
         if (jfc.getSelectedFile().getPath().endsWith("." + FileUtility.csv)) {
             file = jfc.getSelectedFile();
         } else {
             file = new File(jfc.getSelectedFile().getPath() + "." + FileUtility.csv);
         }
 
         SolaTask t = new SolaTask<Boolean, Void>() {
             Exception ex = null;
 
             @Override
             public Boolean doTask() {
                 setMessage(MessageUtility.getLocalizedMessageText(ClientMessage.PROGRESS_MSG_EXPORT_RIGHTS));
                 try {
                     if (!rightsExportResults.exportToCsv(file, list)) {
                         return false;
                     }
                 } catch (Exception e) {
                     ex = e;
                     return false;
                 }
                 return true;
             }
 
             @Override
             public void taskDone() {
                 if (ex != null) {
                     MessageUtility.displayMessage(ClientMessage.BAUNIT_RIGHTS_EXPORT_FAILED,
                             new Object[]{ex.getMessage()});
                 } else if (get()) {
                     lblRecords.setText(String.valueOf(rightsExportResults.getRightsList().size()));
                     MessageUtility.displayMessage(ClientMessage.BAUNIT_RIGHTS_EXPORT_SUCCESS);
                 }
             }
         };
         TaskManager.getInstance().runTask(t);
     }
 
     private void showCalendar(JFormattedTextField dateField) {
         CalendarForm calendar = new CalendarForm(null, true, dateField);
         calendar.setVisible(true);
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         jPanel1 = new javax.swing.JPanel();
         rightsExportParams = new org.sola.clients.beans.administrative.RightsExportParamsBean();
         rightsExportResults = new org.sola.clients.beans.administrative.RightsExportResultListBean();
         rrrTypes = createRightTypes();
         jPanel2 = new javax.swing.JPanel();
         jPanel9 = new javax.swing.JPanel();
         jPanel6 = new javax.swing.JPanel();
         jLabel5 = new javax.swing.JLabel();
         btnSearch = new javax.swing.JButton();
         jPanel7 = new javax.swing.JPanel();
         jLabel6 = new javax.swing.JLabel();
         btnClean = new javax.swing.JButton();
         jPanel10 = new javax.swing.JPanel();
         jPanel4 = new javax.swing.JPanel();
         jLabel2 = new javax.swing.JLabel();
         txtDateFrom = new javax.swing.JFormattedTextField();
         btnRegDateFrom = new javax.swing.JButton();
         jPanel5 = new javax.swing.JPanel();
         jLabel3 = new javax.swing.JLabel();
         txtDateTo = new javax.swing.JFormattedTextField();
         btnRegDateTo = new javax.swing.JButton();
         jScrollPane2 = new javax.swing.JScrollPane();
         tableRightResults = new org.sola.clients.swing.common.controls.JTableWithDefaultStyles();
         jToolBar1 = new javax.swing.JToolBar();
         btnExportSelected = new javax.swing.JButton();
         btnExportAll = new javax.swing.JButton();
         jSeparator1 = new javax.swing.JToolBar.Separator();
         filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
         jLabel4 = new javax.swing.JLabel();
         lblRecords = new javax.swing.JLabel();
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 100, Short.MAX_VALUE)
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 100, Short.MAX_VALUE)
         );
 
         jPanel9.setLayout(new java.awt.GridLayout(1, 2, 10, 0));
 
         java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/sola/clients/swing/ui/administrative/Bundle"); // NOI18N
         jLabel5.setText(bundle.getString("RightsExportPanel.jLabel5.text")); // NOI18N
 
         btnSearch.setText(bundle.getString("RightsExportPanel.btnSearch.text")); // NOI18N
         btnSearch.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSearchActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
         jPanel6.setLayout(jPanel6Layout);
         jPanel6Layout.setHorizontalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(btnSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, Short.MAX_VALUE))
         );
         jPanel6Layout.setVerticalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addComponent(jLabel5)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnSearch))
         );
 
         jPanel9.add(jPanel6);
 
         jLabel6.setText(bundle.getString("RightsExportPanel.jLabel6.text")); // NOI18N
 
         btnClean.setText(bundle.getString("RightsExportPanel.btnClean.text")); // NOI18N
         btnClean.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCleanActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
         jPanel7.setLayout(jPanel7Layout);
         jPanel7Layout.setHorizontalGroup(
             jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(btnClean, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
             .addGroup(jPanel7Layout.createSequentialGroup()
                 .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, Short.MAX_VALUE))
         );
         jPanel7Layout.setVerticalGroup(
             jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel7Layout.createSequentialGroup()
                 .addComponent(jLabel6)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnClean)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jPanel9.add(jPanel7);
 
         jPanel10.setLayout(new java.awt.GridLayout(1, 2, 15, 0));
 
         jLabel2.setText(bundle.getString("RightsExportPanel.jLabel2.text")); // NOI18N
 
         txtDateFrom.setFormatterFactory(FormattersFactory.getInstance().getDateFormatterFactory());
         txtDateFrom.setText(bundle.getString("RightsExportPanel.txtDateFrom.text")); // NOI18N
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rightsExportParams, org.jdesktop.beansbinding.ELProperty.create("${dateFrom}"), txtDateFrom, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         btnRegDateFrom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/calendar.png"))); // NOI18N
         btnRegDateFrom.setText(bundle.getString("RightsExportPanel.btnRegDateFrom.text")); // NOI18N
         btnRegDateFrom.setBorder(null);
         btnRegDateFrom.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRegDateFromActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addComponent(txtDateFrom)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnRegDateFrom))
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addComponent(jLabel2)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(txtDateFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnRegDateFrom)))
         );
 
         jPanel10.add(jPanel4);
 
         jLabel3.setText(bundle.getString("RightsExportPanel.jLabel3.text")); // NOI18N
 
         txtDateTo.setFormatterFactory(FormattersFactory.getInstance().getDateFormatterFactory());
         txtDateTo.setText(bundle.getString("RightsExportPanel.txtDateTo.text")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rightsExportParams, org.jdesktop.beansbinding.ELProperty.create("${dateTo}"), txtDateTo, org.jdesktop.beansbinding.BeanProperty.create("value"));
         bindingGroup.addBinding(binding);
 
         btnRegDateTo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/calendar.png"))); // NOI18N
         btnRegDateTo.setText(bundle.getString("RightsExportPanel.btnRegDateTo.text")); // NOI18N
         btnRegDateTo.setBorder(null);
         btnRegDateTo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRegDateToActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
         jPanel5.setLayout(jPanel5Layout);
         jPanel5Layout.setHorizontalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel5Layout.createSequentialGroup()
                         .addComponent(jLabel3)
                         .addGap(0, 25, Short.MAX_VALUE))
                     .addComponent(txtDateTo))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnRegDateTo))
         );
         jPanel5Layout.setVerticalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addComponent(jLabel3)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(btnRegDateTo)
                     .addComponent(txtDateTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
         );
 
         jPanel10.add(jPanel5);
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(133, Short.MAX_VALUE))
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
             .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${rightsList}");
         org.jdesktop.swingbinding.JTableBinding jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rightsExportResults, eLProperty, tableRightResults);
         org.jdesktop.swingbinding.JTableBinding.ColumnBinding columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${checked}"));
         columnBinding.setColumnName("Checked");
         columnBinding.setColumnClass(Boolean.class);
         columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${rightType}"));
         columnBinding.setColumnName("Right Type");
         columnBinding.setColumnClass(String.class);
         columnBinding.setEditable(false);
         columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${parcelNumber}"));
         columnBinding.setColumnName("Parcel Number");
         columnBinding.setColumnClass(String.class);
         columnBinding.setEditable(false);
         columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${parcelArea}"));
         columnBinding.setColumnName("Parcel Area");
         columnBinding.setColumnClass(java.math.BigDecimal.class);
         columnBinding.setEditable(false);
         columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${payeeFullName}"));
         columnBinding.setColumnName("Payee Full Name");
         columnBinding.setColumnClass(String.class);
         columnBinding.setEditable(false);
         columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${leaseNumber}"));
         columnBinding.setColumnName("Lease Number");
         columnBinding.setColumnClass(String.class);
         columnBinding.setEditable(false);
         columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${rightRegistrationNumber}"));
         columnBinding.setColumnName("Right Registration Number");
         columnBinding.setColumnClass(String.class);
         columnBinding.setEditable(false);
         columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${rightRegistrationDate}"));
         columnBinding.setColumnName("Right Registration Date");
         columnBinding.setColumnClass(java.util.Date.class);
         columnBinding.setEditable(false);
         columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${startDate}"));
         columnBinding.setColumnName("Start Date");
         columnBinding.setColumnClass(java.util.Date.class);
         columnBinding.setEditable(false);
         columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${rightExpirationDate}"));
         columnBinding.setColumnName("Right Expiration Date");
         columnBinding.setColumnClass(java.util.Date.class);
         columnBinding.setEditable(false);
         columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${rightHolders}"));
         columnBinding.setColumnName("Right Holders");
         columnBinding.setColumnClass(String.class);
         columnBinding.setEditable(false);
         columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${rightStatusDate}"));
         columnBinding.setColumnName("Right Status Date");
         columnBinding.setColumnClass(java.util.Date.class);
         columnBinding.setEditable(false);
         columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${rightStatus}"));
         columnBinding.setColumnName("Right Status");
         columnBinding.setColumnClass(String.class);
         columnBinding.setEditable(false);
         bindingGroup.addBinding(jTableBinding);
         jTableBinding.bind();binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rightsExportResults, org.jdesktop.beansbinding.ELProperty.create("${selectedRight}"), tableRightResults, org.jdesktop.beansbinding.BeanProperty.create("selectedElement"));
         bindingGroup.addBinding(binding);
 
         jScrollPane2.setViewportView(tableRightResults);
         tableRightResults.getColumnModel().getColumn(0).setMaxWidth(25);
         tableRightResults.getColumnModel().getColumn(0).setHeaderValue(bundle.getString("RightsExportPanel.tableRightResults.columnModel.title0_1")); // NOI18N
         tableRightResults.getColumnModel().getColumn(1).setHeaderValue(bundle.getString("RightsExportPanel.tableRightResults.columnModel.title12")); // NOI18N
         tableRightResults.getColumnModel().getColumn(2).setPreferredWidth(100);
         tableRightResults.getColumnModel().getColumn(2).setMaxWidth(200);
         tableRightResults.getColumnModel().getColumn(2).setHeaderValue(bundle.getString("RightsExportPanel.tableRightResults.columnModel.title4")); // NOI18N
         tableRightResults.getColumnModel().getColumn(3).setPreferredWidth(100);
         tableRightResults.getColumnModel().getColumn(3).setMaxWidth(150);
         tableRightResults.getColumnModel().getColumn(3).setHeaderValue(bundle.getString("RightsExportPanel.tableRightResults.columnModel.title2_1")); // NOI18N
         tableRightResults.getColumnModel().getColumn(4).setHeaderValue(bundle.getString("RightsExportPanel.tableRightResults.columnModel.title1_1")); // NOI18N
         tableRightResults.getColumnModel().getColumn(5).setHeaderValue(bundle.getString("RightsExportPanel.tableRightResults.columnModel.title10")); // NOI18N
         tableRightResults.getColumnModel().getColumn(6).setHeaderValue(bundle.getString("RightsExportPanel.tableRightResults.columnModel.title9")); // NOI18N
         tableRightResults.getColumnModel().getColumn(7).setPreferredWidth(100);
         tableRightResults.getColumnModel().getColumn(7).setMaxWidth(120);
         tableRightResults.getColumnModel().getColumn(7).setHeaderValue(bundle.getString("RightsExportPanel.tableRightResults.columnModel.title6")); // NOI18N
         tableRightResults.getColumnModel().getColumn(8).setHeaderValue(bundle.getString("RightsExportPanel.tableRightResults.columnModel.title11")); // NOI18N
         tableRightResults.getColumnModel().getColumn(9).setPreferredWidth(100);
         tableRightResults.getColumnModel().getColumn(9).setMaxWidth(120);
         tableRightResults.getColumnModel().getColumn(9).setHeaderValue(bundle.getString("RightsExportPanel.tableRightResults.columnModel.title5")); // NOI18N
         tableRightResults.getColumnModel().getColumn(10).setHeaderValue(bundle.getString("RightsExportPanel.tableRightResults.columnModel.title3_1")); // NOI18N
         tableRightResults.getColumnModel().getColumn(11).setHeaderValue(bundle.getString("RightsExportPanel.tableRightResults.columnModel.title7")); // NOI18N
         tableRightResults.getColumnModel().getColumn(12).setHeaderValue(bundle.getString("RightsExportPanel.tableRightResults.columnModel.title8")); // NOI18N
 
         jToolBar1.setFloatable(false);
         jToolBar1.setRollover(true);
 
         btnExportSelected.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/export.png"))); // NOI18N
         btnExportSelected.setText(bundle.getString("RightsExportPanel.btnExportSelected.text")); // NOI18N
         btnExportSelected.setFocusable(false);
         btnExportSelected.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnExportSelectedActionPerformed(evt);
             }
         });
         jToolBar1.add(btnExportSelected);
 
         btnExportAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/common/export2.png"))); // NOI18N
         btnExportAll.setText(bundle.getString("RightsExportPanel.btnExportAll.text")); // NOI18N
         btnExportAll.setFocusable(false);
         btnExportAll.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnExportAllActionPerformed(evt);
             }
         });
         jToolBar1.add(btnExportAll);
         jToolBar1.add(jSeparator1);
         jToolBar1.add(filler1);
 
         jLabel4.setText(bundle.getString("RightsExportPanel.jLabel4.text")); // NOI18N
         jToolBar1.add(jLabel4);
 
         lblRecords.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         lblRecords.setText(bundle.getString("RightsExportPanel.lblRecords.text")); // NOI18N
         jToolBar1.add(lblRecords);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jScrollPane2)
             .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE))
         );
 
         bindingGroup.bind();
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
         search();
     }//GEN-LAST:event_btnSearchActionPerformed
 
     private void btnCleanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCleanActionPerformed
         clean();
     }//GEN-LAST:event_btnCleanActionPerformed
 
     private void btnExportSelectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportSelectedActionPerformed
         export(rightsExportResults.getSelectedRightsList());
     }//GEN-LAST:event_btnExportSelectedActionPerformed
 
     private void btnExportAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportAllActionPerformed
         export(rightsExportResults.getRightsList());
     }//GEN-LAST:event_btnExportAllActionPerformed
 
     private void btnRegDateFromActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegDateFromActionPerformed
         showCalendar(txtDateFrom);
     }//GEN-LAST:event_btnRegDateFromActionPerformed
 
     private void btnRegDateToActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegDateToActionPerformed
         showCalendar(txtDateTo);
     }//GEN-LAST:event_btnRegDateToActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnClean;
     private javax.swing.JButton btnExportAll;
     private javax.swing.JButton btnExportSelected;
     private javax.swing.JButton btnRegDateFrom;
     private javax.swing.JButton btnRegDateTo;
     private javax.swing.JButton btnSearch;
     private javax.swing.Box.Filler filler1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel10;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JToolBar.Separator jSeparator1;
     private javax.swing.JToolBar jToolBar1;
     private javax.swing.JLabel lblRecords;
     private org.sola.clients.beans.administrative.RightsExportParamsBean rightsExportParams;
     private org.sola.clients.beans.administrative.RightsExportResultListBean rightsExportResults;
     private org.sola.clients.beans.referencedata.RrrTypeListBean rrrTypes;
     private org.sola.clients.swing.common.controls.JTableWithDefaultStyles tableRightResults;
     private javax.swing.JFormattedTextField txtDateFrom;
     private javax.swing.JFormattedTextField txtDateTo;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 }
