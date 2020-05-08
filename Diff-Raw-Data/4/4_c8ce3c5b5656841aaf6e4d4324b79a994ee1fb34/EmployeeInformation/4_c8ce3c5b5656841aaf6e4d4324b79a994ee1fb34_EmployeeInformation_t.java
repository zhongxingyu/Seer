 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * EmployeeInformation.java
  *
  * Created on Sep 27, 2010, 1:13:42 PM
  */
 package com.jcl.payroll.ui;
 
 import com.jcl.customizetable.DateTableCellRenderer;
 import com.jcl.customizetable.NonEditableDefaultTableModel;
 import com.jcl.customizetable.NumberTableCellRenderer;
 import com.jcl.dao.CompanyDao;
 import com.jcl.dao.DepartmentDao;
 import com.jcl.dao.EmployeeDao;
 import com.jcl.dao.PositionDao;
 //import com.jcl.dbms.dbms;
 import com.jcl.model.Employee;
 import com.jcl.model.Position;
 import com.jcl.main.MainApp;
 import com.jcl.model.*;
 import com.jcl.observables.PanelMessage;
 import com.jcl.payroll.enumtypes.DTRDisplayType;
 import com.jcl.payroll.enumtypes.DTRType;
 import com.jcl.payroll.enumtypes.EmploymentStatus;
 import com.jcl.payroll.enumtypes.MaritalStatus;
 import com.jcl.payroll.enumtypes.PayrollPeriodType;
 import com.jcl.payroll.transaction.PaySlipProcess;
 import com.jcl.payroll.transaction.PaySlipReportObject;
 import com.jcl.payroll.transaction.PaySlipReportRow;
 import com.jcl.payroll.enumtypes.*;
 import com.jcl.reports.ReportViewerFactory;
 import com.jcl.utilities.MyDateFormatter;
 import com.jcl.utilities.MyNumberFormatter;
 import com.jcl.utils.KeyValue;
 import com.jcl.verycommon.JOptionErrorMessage;
 import com.jcl.utils.SelectedButton;
 import java.awt.Component;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 import net.sf.jasperreports.swing.JRViewer;
 import org.apache.poi.ss.usermodel.ExcelStyleDateFormatter;
 import org.springframework.beans.factory.annotation.Autowired;
 
 /**
  *
  * @author jlavador
  */
 @org.springframework.stereotype.Component
 public class EmployeeInformation extends javax.swing.JPanel {
 
     @Autowired
     EmployeeDao eDao;
     @Autowired
     PositionDao pDao;
     @Autowired
     DepartmentDao dDao;
     @Autowired
     CompanyDao cDao;
     private Employee ce;
     private boolean isSelectionMade = false;
     private SimpleDateFormat sdf;
     private SimpleDateFormat stf;
 
     /**
      * Creates new form EmployeeInformation
      */
     public EmployeeInformation() {
         initComponents();
     }
 
     public void setup() {
         try {
             sdf = MyDateFormatter.getSimpleDateTimeFormatter();
             stf = MyDateFormatter.getTimeFormatter();
             initTableView();
 
 
             disabledComponents(false);
             disabledComponents();
             //initTreeView();
             initComboBoxes();
             txtDateTo.setFormats("MM/dd/yyyy");
             txtDateFrom.setFormats("MM/dd/yyyy");
 
         } catch (Exception ex) {
             Logger.getLogger(EmployeeInformation.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void initTableView() throws Exception {
 
         List<Employee> employeeList = new ArrayList<Employee>();
 
         NonEditableDefaultTableModel dtm = new NonEditableDefaultTableModel();
         DateTableCellRenderer dtcr = new DateTableCellRenderer("MM/dd/yyyy");
 
 
         dtm.setColumnIdentifiers(new String[]{"#", "ID", "Position", "Name"});
 
         try {
             employeeList = eDao.getSortedEmployee();
             int counter = 1;
             for (Employee e : employeeList) {
 
                 if (e != null) {
                     Object[] o = new Object[]{counter++, e.getIdNumber(), e.getPosition(), e};
                     dtm.addRow(o);
                 }
             }
 
         } finally {
         }
 
 
 
         tableEmployees.setModel(dtm);
 //        tableEmployees.getColumn("DateOfBirth").setCellRenderer(dtcr);
         tableEmployees.getColumn("#").setMaxWidth(30);
         tableEmployees.getColumn("#").setMinWidth(30);
         tableEmployees.getColumn("ID").setMaxWidth(50);
         tableEmployees.getColumn("ID").setMinWidth(50);
         tableEmployees.getColumn("Position").setMaxWidth(70);
         tableEmployees.getColumn("Position").setMinWidth(70);
 
     }
 
 //    public void init() {
 //        treeEmployee.addTreeSelectionListener(new TreeSelectionListener() {
 //
 //            @Override
 //            public void valueChanged(TreeSelectionEvent e) {
 //                if (treeEmployee != null) {
 //
 //
 //                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeEmployee.getLastSelectedPathComponent();
 //                    if (node != null && node.isLeaf()) {
 //                        isSelectionMade = true;
 //                        JCLDefaultTreeNode dmtn = (JCLDefaultTreeNode) node;
 //
 //                        ce =   dbms.getDBInstance().ext().getByID(dmtn.getId());
 //                        dbms.getDBInstance().activate(ce, 3);
 //                        initEmployee(ce);
 //                        System.out.println(ce);
 //                        isSelectionMade = false;
 //                    }
 //
 //                }
 //            }
 //        });
 //
 //    }
     private void disabledComponents() {
         this.comboCompany.setVisible(true);
         this.comboDepartment.setVisible(true);
         this.jLabel33.setVisible(true);
         this.jLabel36.setVisible(true);
 
         this.jLabel40.setVisible(true);
         this.jLabel41.setVisible(true);
         this.jLabel42.setVisible(true);
         this.textAllowance.setVisible(true);
         this.textBenefits.setVisible(true);
         this.textHourlyRate.setVisible(true); //dailyRate
 
         this.jLabel25.setVisible(false);
         this.jLabel43.setVisible(false);
         this.textDLoan1.setVisible(false);
         this.textDLoan2.setVisible(false);
 
 //        panelDTR.setVisible(false);
 //        panelPayslip.setVisible(false);
         jTabbedPane1.remove(panelDTR);
         jTabbedPane1.remove(panelPayslip);
     }
 
     private void disabledComponents(boolean s) {
         for (Component c : panelEmployeeInfo.getComponents()) {
             c.setEnabled(s);
         }
 
         for (Component c : panelContactInfo1.getComponents()) {
             c.setEnabled(s);
         }
         for (Component c : panelContactInfo2.getComponents()) {
             c.setEnabled(s);
         }
         for (Component c : panelContactInfo3.getComponents()) {
             c.setEnabled(s);
         }
         for (Component c : panelContactInfo4.getComponents()) {
             c.setEnabled(s);
         }
 
         for (Component c : panelContactInfo.getComponents()) {
             c.setEnabled(s);
         }
         for (Component c : jPanel1.getComponents()) {
             c.setEnabled(s);
         }
 
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         buttonGroup1 = new javax.swing.ButtonGroup();
         panelTop = new javax.swing.JPanel();
         jLabel8 = new javax.swing.JLabel();
         panelBottom = new javax.swing.JPanel();
         btnClose = new javax.swing.JButton();
         btnSave = new javax.swing.JButton();
         jPanel5 = new javax.swing.JPanel();
         btnNew = new javax.swing.JButton();
         btnCancel = new javax.swing.JButton();
         jSplitPane1 = new javax.swing.JSplitPane();
         panelLeft = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         tableEmployees = new javax.swing.JTable();
         jPanel18 = new javax.swing.JPanel();
         panelCenter = new javax.swing.JPanel();
         panelEmployeeInfo = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         txtEmployeeID = new javax.swing.JTextField();
         txtFirstName = new javax.swing.JTextField();
         txtMiddleName = new javax.swing.JTextField();
         jLabel5 = new javax.swing.JLabel();
         txtLastName = new javax.swing.JTextField();
         jLabel4 = new javax.swing.JLabel();
         txtDateOfBirth = new org.jdesktop.swingx.JXDatePicker();
         jLabel6 = new javax.swing.JLabel();
         jPanel1 = new javax.swing.JPanel();
         rbMale = new javax.swing.JRadioButton();
         rbFemale = new javax.swing.JRadioButton();
         jLabel7 = new javax.swing.JLabel();
         comboMaritalStatus = new javax.swing.JComboBox();
         labelName = new javax.swing.JLabel();
         labelCompleteName = new javax.swing.JLabel();
         jLabel38 = new javax.swing.JLabel();
         comboPosition = new javax.swing.JComboBox();
         jLabel9 = new javax.swing.JLabel();
         txtNumberOfDependents = new javax.swing.JFormattedTextField();
         panelOthers = new javax.swing.JPanel();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         panelEmployeeData = new javax.swing.JPanel();
         panelContactInfo1 = new javax.swing.JPanel();
         jLabel21 = new javax.swing.JLabel();
         jLabel22 = new javax.swing.JLabel();
         jLabel23 = new javax.swing.JLabel();
         jLabel24 = new javax.swing.JLabel();
         jLabel25 = new javax.swing.JLabel();
         jPanel8 = new javax.swing.JPanel();
         textDTax = new javax.swing.JFormattedTextField();
         textDSSS = new javax.swing.JFormattedTextField();
         textDPagIbig = new javax.swing.JFormattedTextField();
         textDPhilHealth = new javax.swing.JFormattedTextField();
         textDLoan2 = new javax.swing.JFormattedTextField();
         jLabel43 = new javax.swing.JLabel();
         textDLoan1 = new javax.swing.JFormattedTextField();
         panelContactInfo2 = new javax.swing.JPanel();
         jLabel26 = new javax.swing.JLabel();
         jLabel27 = new javax.swing.JLabel();
         jLabel28 = new javax.swing.JLabel();
         jLabel29 = new javax.swing.JLabel();
         jLabel30 = new javax.swing.JLabel();
         textBankAccountNo = new javax.swing.JTextField();
         textTaxIdNo = new javax.swing.JTextField();
         textSSSNo = new javax.swing.JTextField();
         textPagIbigNo = new javax.swing.JTextField();
         textPhilHealthNo = new javax.swing.JTextField();
         jPanel10 = new javax.swing.JPanel();
         panelContactInfo3 = new javax.swing.JPanel();
         jLabel31 = new javax.swing.JLabel();
         jLabel33 = new javax.swing.JLabel();
         jLabel34 = new javax.swing.JLabel();
         jPanel11 = new javax.swing.JPanel();
         jLabel36 = new javax.swing.JLabel();
         jLabel37 = new javax.swing.JLabel();
         comboEmploymentStatus = new javax.swing.JComboBox();
         comboDepartment = new javax.swing.JComboBox();
         comboCompany = new javax.swing.JComboBox();
         txtDateHired = new org.jdesktop.swingx.JXDatePicker();
         txtDateEnd = new org.jdesktop.swingx.JXDatePicker();
         jLabel47 = new javax.swing.JLabel();
         jLabel48 = new javax.swing.JLabel();
         textSickLeave = new javax.swing.JFormattedTextField();
         textVacationLeave = new javax.swing.JFormattedTextField();
         panelContactInfo4 = new javax.swing.JPanel();
         jLabel32 = new javax.swing.JLabel();
         jLabel39 = new javax.swing.JLabel();
         jLabel40 = new javax.swing.JLabel();
         jLabel41 = new javax.swing.JLabel();
         jLabel42 = new javax.swing.JLabel();
         jPanel9 = new javax.swing.JPanel();
         textBasicSalary = new javax.swing.JFormattedTextField();
         textDailyRate = new javax.swing.JFormattedTextField();
         textHourlyRate = new javax.swing.JFormattedTextField();
         textBenefits = new javax.swing.JFormattedTextField();
         textAllowance = new javax.swing.JFormattedTextField();
         jLabel16 = new javax.swing.JLabel();
         comboType = new javax.swing.JComboBox();
         jLabel46 = new javax.swing.JLabel();
         comboPayCode = new javax.swing.JComboBox();
         panelPersonalTab = new javax.swing.JPanel();
         panelContactInfo = new javax.swing.JPanel();
         jLabel10 = new javax.swing.JLabel();
         jLabel13 = new javax.swing.JLabel();
         textStreet = new javax.swing.JTextField();
         jPanel6 = new javax.swing.JPanel();
         txtContactNo = new javax.swing.JFormattedTextField();
         panelDTR = new javax.swing.JPanel();
         jPanel7 = new javax.swing.JPanel();
         btnInsertDetail = new javax.swing.JButton();
         btnDelete = new javax.swing.JButton();
         jPanel12 = new javax.swing.JPanel();
         jPanel13 = new javax.swing.JPanel();
         jLabel17 = new javax.swing.JLabel();
         txtTotal = new javax.swing.JTextField();
         jPanel14 = new javax.swing.JPanel();
         txtDateTo = new org.jdesktop.swingx.JXDatePicker();
         jLabel19 = new javax.swing.JLabel();
         jLabel35 = new javax.swing.JLabel();
         comboDisplayType = new javax.swing.JComboBox();
         jPanel15 = new javax.swing.JPanel();
         jLabel44 = new javax.swing.JLabel();
         txtDateFrom = new org.jdesktop.swingx.JXDatePicker();
         btnRefreshDTR = new javax.swing.JButton();
         jTabbedPane2 = new javax.swing.JTabbedPane();
         jScrollPane4 = new javax.swing.JScrollPane();
         tableDTR = new javax.swing.JTable();
         jPanel16 = new javax.swing.JPanel();
         jScrollPane5 = new javax.swing.JScrollPane();
         tableJob = new javax.swing.JTable();
         panelPayslip = new javax.swing.JPanel();
         jScrollPane6 = new javax.swing.JScrollPane();
         txtPayslip = new javax.swing.JTextArea();
         jPanel19 = new javax.swing.JPanel();
         jLabel45 = new javax.swing.JLabel();
         comboPayrollPeriod = new javax.swing.JComboBox();
         jPanel20 = new javax.swing.JPanel();
         btnPayslip = new javax.swing.JButton();
         btnPayslipAll = new javax.swing.JButton();
 
         setBackground(new java.awt.Color(255, 255, 255));
         setBorder(javax.swing.BorderFactory.createEtchedBorder());
         setLayout(new java.awt.BorderLayout());
 
         panelTop.setBackground(javax.swing.UIManager.getDefaults().getColor("InternalFrame.inactiveTitleGradient"));
         panelTop.setMinimumSize(new java.awt.Dimension(0, 25));
         panelTop.setPreferredSize(new java.awt.Dimension(750, 25));
         panelTop.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
 
         jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         jLabel8.setText("Employee Information");
         panelTop.add(jLabel8);
 
         add(panelTop, java.awt.BorderLayout.PAGE_START);
 
         panelBottom.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         panelBottom.setMinimumSize(new java.awt.Dimension(0, 32));
         panelBottom.setPreferredSize(new java.awt.Dimension(750, 40));
         panelBottom.setLayout(new java.awt.GridBagLayout());
 
         btnClose.setText("Close");
         btnClose.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCloseActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         panelBottom.add(btnClose, gridBagConstraints);
 
         btnSave.setMnemonic('S');
         btnSave.setText("Save");
         btnSave.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSaveActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
         panelBottom.add(btnSave, gridBagConstraints);
 
         javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
         jPanel5.setLayout(jPanel5Layout);
         jPanel5Layout.setHorizontalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         jPanel5Layout.setVerticalGroup(
             jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         panelBottom.add(jPanel5, gridBagConstraints);
 
         btnNew.setMnemonic('N');
         btnNew.setText("New");
         btnNew.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnNewActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
         panelBottom.add(btnNew, gridBagConstraints);
 
         btnCancel.setText("Cancel");
         btnCancel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCancelActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
         panelBottom.add(btnCancel, gridBagConstraints);
 
         add(panelBottom, java.awt.BorderLayout.PAGE_END);
 
         jSplitPane1.setDividerLocation(350);
 
         panelLeft.setLayout(new java.awt.BorderLayout());
 
         tableEmployees.setAutoCreateRowSorter(true);
         tableEmployees.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {},
                 {},
                 {},
                 {}
             },
             new String [] {
 
             }
         ));
         tableEmployees.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
         tableEmployees.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tableEmployeesMouseClicked(evt);
             }
         });
         jScrollPane1.setViewportView(tableEmployees);
 
         panelLeft.add(jScrollPane1, java.awt.BorderLayout.CENTER);
 
         jPanel18.setMaximumSize(new java.awt.Dimension(32767, 32));
         jPanel18.setMinimumSize(new java.awt.Dimension(0, 32));
         jPanel18.setPreferredSize(new java.awt.Dimension(349, 32));
 
         javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
         jPanel18.setLayout(jPanel18Layout);
         jPanel18Layout.setHorizontalGroup(
             jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 349, Short.MAX_VALUE)
         );
         jPanel18Layout.setVerticalGroup(
             jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 32, Short.MAX_VALUE)
         );
 
         panelLeft.add(jPanel18, java.awt.BorderLayout.SOUTH);
 
         jSplitPane1.setLeftComponent(panelLeft);
 
         panelCenter.setLayout(new java.awt.GridBagLayout());
 
         panelEmployeeInfo.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         panelEmployeeInfo.setLayout(new java.awt.GridBagLayout());
 
         jLabel1.setText("Employee ID");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelEmployeeInfo.add(jLabel1, gridBagConstraints);
 
         jLabel2.setText("First Name");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelEmployeeInfo.add(jLabel2, gridBagConstraints);
 
         jLabel3.setText("Middle Name");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelEmployeeInfo.add(jLabel3, gridBagConstraints);
 
         txtEmployeeID.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
         txtEmployeeID.setMinimumSize(new java.awt.Dimension(2, 22));
         txtEmployeeID.setPreferredSize(new java.awt.Dimension(55, 22));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelEmployeeInfo.add(txtEmployeeID, gridBagConstraints);
 
         txtFirstName.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
         txtFirstName.setMinimumSize(new java.awt.Dimension(2, 22));
         txtFirstName.setPreferredSize(new java.awt.Dimension(55, 22));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelEmployeeInfo.add(txtFirstName, gridBagConstraints);
         txtFirstName.getAccessibleContext().setAccessibleName("firstName");
 
         txtMiddleName.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
         txtMiddleName.setMinimumSize(new java.awt.Dimension(2, 20));
         txtMiddleName.setPreferredSize(new java.awt.Dimension(55, 22));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 5;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelEmployeeInfo.add(txtMiddleName, gridBagConstraints);
         txtMiddleName.getAccessibleContext().setAccessibleName("middleName");
 
         jLabel5.setText("Marital Status");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelEmployeeInfo.add(jLabel5, gridBagConstraints);
 
         txtLastName.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
         txtLastName.setMinimumSize(new java.awt.Dimension(2, 20));
         txtLastName.setPreferredSize(new java.awt.Dimension(55, 22));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelEmployeeInfo.add(txtLastName, gridBagConstraints);
         txtLastName.getAccessibleContext().setAccessibleName("lastName");
 
         jLabel4.setText("Date of Birth");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(4, 5, 0, 0);
         panelEmployeeInfo.add(jLabel4, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(4, 5, 0, 0);
         panelEmployeeInfo.add(txtDateOfBirth, gridBagConstraints);
 
         jLabel6.setText("Sex");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelEmployeeInfo.add(jLabel6, gridBagConstraints);
 
         jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
 
         buttonGroup1.add(rbMale);
         rbMale.setText("Male");
         jPanel1.add(rbMale);
 
         buttonGroup1.add(rbFemale);
         rbFemale.setText("Female");
         jPanel1.add(rbFemale);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelEmployeeInfo.add(jPanel1, gridBagConstraints);
 
         jLabel7.setText("Last Name");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelEmployeeInfo.add(jLabel7, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 5;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelEmployeeInfo.add(comboMaritalStatus, gridBagConstraints);
 
         labelName.setText("Name");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelEmployeeInfo.add(labelName, gridBagConstraints);
 
         labelCompleteName.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         labelCompleteName.setForeground(java.awt.Color.blue);
         labelCompleteName.setText("Juan Dela Cruz");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.gridwidth = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelEmployeeInfo.add(labelCompleteName, gridBagConstraints);
 
         jLabel38.setText("Position");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
         panelEmployeeInfo.add(jLabel38, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
         panelEmployeeInfo.add(comboPosition, gridBagConstraints);
 
         jLabel9.setText("No. Of Dependents");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
         panelEmployeeInfo.add(jLabel9, gridBagConstraints);
 
         txtNumberOfDependents.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("##"))));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
         panelEmployeeInfo.add(txtNumberOfDependents, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.ipady = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         panelCenter.add(panelEmployeeInfo, gridBagConstraints);
 
         panelOthers.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         panelOthers.setLayout(new java.awt.GridBagLayout());
 
         panelEmployeeData.setLayout(new java.awt.GridBagLayout());
 
         panelContactInfo1.setBorder(javax.swing.BorderFactory.createTitledBorder("Monthly / Pay Day Deduction"));
         panelContactInfo1.setLayout(new java.awt.GridBagLayout());
 
         jLabel21.setText("Tax WithHeld");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo1.add(jLabel21, gridBagConstraints);
 
         jLabel22.setText("SSS");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo1.add(jLabel22, gridBagConstraints);
 
         jLabel23.setText("Pag-Ibig");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo1.add(jLabel23, gridBagConstraints);
 
         jLabel24.setText("PhilHealth");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo1.add(jLabel24, gridBagConstraints);
 
         jLabel25.setText("Loan 2");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo1.add(jLabel25, gridBagConstraints);
 
         javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
         jPanel8.setLayout(jPanel8Layout);
         jPanel8Layout.setHorizontalGroup(
             jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         jPanel8Layout.setVerticalGroup(
             jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.weighty = 1.0;
         panelContactInfo1.add(jPanel8, gridBagConstraints);
 
         textDTax.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
         textDTax.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo1.add(textDTax, gridBagConstraints);
 
         textDSSS.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
         textDSSS.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo1.add(textDSSS, gridBagConstraints);
 
         textDPagIbig.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
         textDPagIbig.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo1.add(textDPagIbig, gridBagConstraints);
 
         textDPhilHealth.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
         textDPhilHealth.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo1.add(textDPhilHealth, gridBagConstraints);
 
         textDLoan2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
         textDLoan2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo1.add(textDLoan2, gridBagConstraints);
 
         jLabel43.setText("Loan 1");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo1.add(jLabel43, gridBagConstraints);
 
         textDLoan1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
         textDLoan1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo1.add(textDLoan1, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelEmployeeData.add(panelContactInfo1, gridBagConstraints);
 
         panelContactInfo2.setBorder(javax.swing.BorderFactory.createTitledBorder("Goverment ID's / Bank Accounts"));
         panelContactInfo2.setLayout(new java.awt.GridBagLayout());
 
         jLabel26.setText("Bank Account");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo2.add(jLabel26, gridBagConstraints);
 
         jLabel27.setText("Tax ID");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo2.add(jLabel27, gridBagConstraints);
 
         jLabel28.setText("SSS");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo2.add(jLabel28, gridBagConstraints);
 
         jLabel29.setText("Pag-Ibig");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo2.add(jLabel29, gridBagConstraints);
 
         jLabel30.setText("PhilHealth");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo2.add(jLabel30, gridBagConstraints);
 
         textBankAccountNo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo2.add(textBankAccountNo, gridBagConstraints);
 
         textTaxIdNo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo2.add(textTaxIdNo, gridBagConstraints);
 
         textSSSNo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo2.add(textSSSNo, gridBagConstraints);
 
         textPagIbigNo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo2.add(textPagIbigNo, gridBagConstraints);
 
         textPhilHealthNo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo2.add(textPhilHealthNo, gridBagConstraints);
 
         javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
         jPanel10.setLayout(jPanel10Layout);
         jPanel10Layout.setHorizontalGroup(
             jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         jPanel10Layout.setVerticalGroup(
             jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.weighty = 1.0;
         panelContactInfo2.add(jPanel10, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelEmployeeData.add(panelContactInfo2, gridBagConstraints);
 
         panelContactInfo3.setBorder(javax.swing.BorderFactory.createTitledBorder("Data"));
         panelContactInfo3.setLayout(new java.awt.GridBagLayout());
 
         jLabel31.setText("Employment Status");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo3.add(jLabel31, gridBagConstraints);
 
         jLabel33.setText("Department");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo3.add(jLabel33, gridBagConstraints);
 
         jLabel34.setText("Date End");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo3.add(jLabel34, gridBagConstraints);
 
         javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
         jPanel11.setLayout(jPanel11Layout);
         jPanel11Layout.setHorizontalGroup(
             jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         jPanel11Layout.setVerticalGroup(
             jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.weighty = 1.0;
         panelContactInfo3.add(jPanel11, gridBagConstraints);
 
         jLabel36.setText("Company");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo3.add(jLabel36, gridBagConstraints);
 
         jLabel37.setText("Date Hired");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo3.add(jLabel37, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo3.add(comboEmploymentStatus, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo3.add(comboDepartment, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo3.add(comboCompany, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo3.add(txtDateHired, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo3.add(txtDateEnd, gridBagConstraints);
 
         jLabel47.setText("Sick Leave");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo3.add(jLabel47, gridBagConstraints);
 
         jLabel48.setText("Vacation Leave");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo3.add(jLabel48, gridBagConstraints);
 
         textSickLeave.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0.##"))));
         textSickLeave.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo3.add(textSickLeave, gridBagConstraints);
 
         textVacationLeave.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0.##"))));
         textVacationLeave.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo3.add(textVacationLeave, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelEmployeeData.add(panelContactInfo3, gridBagConstraints);
 
         panelContactInfo4.setBorder(javax.swing.BorderFactory.createTitledBorder("Salary Rates"));
         panelContactInfo4.setLayout(new java.awt.GridBagLayout());
 
         jLabel32.setText("Basic Pay");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo4.add(jLabel32, gridBagConstraints);
 
         jLabel39.setText("Daily Rate");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo4.add(jLabel39, gridBagConstraints);
 
         jLabel40.setText("Hourly Rate");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo4.add(jLabel40, gridBagConstraints);
 
         jLabel41.setText("Benefits/others");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo4.add(jLabel41, gridBagConstraints);
 
         jLabel42.setText("Allowance");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo4.add(jLabel42, gridBagConstraints);
 
         javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
         jPanel9.setLayout(jPanel9Layout);
         jPanel9Layout.setHorizontalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         jPanel9Layout.setVerticalGroup(
             jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 8;
         gridBagConstraints.weighty = 1.0;
         panelContactInfo4.add(jPanel9, gridBagConstraints);
 
         textBasicSalary.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
         textBasicSalary.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo4.add(textBasicSalary, gridBagConstraints);
 
         textDailyRate.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
         textDailyRate.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo4.add(textDailyRate, gridBagConstraints);
 
         textHourlyRate.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
         textHourlyRate.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo4.add(textHourlyRate, gridBagConstraints);
 
         textBenefits.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
         textBenefits.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo4.add(textBenefits, gridBagConstraints);
 
         textAllowance.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
         textAllowance.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo4.add(textAllowance, gridBagConstraints);
 
         jLabel16.setText("Pay Type");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo4.add(jLabel16, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo4.add(comboType, gridBagConstraints);
 
         jLabel46.setText("Pay Code");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo4.add(jLabel46, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 6;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo4.add(comboPayCode, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.5;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelEmployeeData.add(panelContactInfo4, gridBagConstraints);
 
         jTabbedPane1.addTab("Employee Data", panelEmployeeData);
 
         panelPersonalTab.setLayout(new java.awt.BorderLayout());
 
         panelContactInfo.setBorder(javax.swing.BorderFactory.createTitledBorder("Contact Information"));
         panelContactInfo.setLayout(new java.awt.GridBagLayout());
 
         jLabel10.setText("Address");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo.add(jLabel10, gridBagConstraints);
 
         jLabel13.setText("Contact No.");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
         panelContactInfo.add(jLabel13, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo.add(textStreet, gridBagConstraints);
 
         javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
         jPanel6.setLayout(jPanel6Layout);
         jPanel6Layout.setHorizontalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         jPanel6Layout.setVerticalGroup(
             jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 5;
         gridBagConstraints.weighty = 1.0;
         panelContactInfo.add(jPanel6, gridBagConstraints);
 
         try {
             txtContactNo.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("###-####")));
         } catch (java.text.ParseException ex) {
             ex.printStackTrace();
         }
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         panelContactInfo.add(txtContactNo, gridBagConstraints);
 
         panelPersonalTab.add(panelContactInfo, java.awt.BorderLayout.CENTER);
 
         jTabbedPane1.addTab("Personal", panelPersonalTab);
 
         panelDTR.setLayout(new java.awt.GridBagLayout());
 
         jPanel7.setMaximumSize(new java.awt.Dimension(150, 33));
         jPanel7.setMinimumSize(new java.awt.Dimension(100, 33));
         jPanel7.setPreferredSize(new java.awt.Dimension(150, 33));
         jPanel7.setLayout(new java.awt.GridBagLayout());
 
         btnInsertDetail.setMnemonic('I');
         btnInsertDetail.setText("Insert DTR");
         btnInsertDetail.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnInsertDetailActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(21, 5, 0, 5);
         jPanel7.add(btnInsertDetail, gridBagConstraints);
 
         btnDelete.setText("Delete");
         btnDelete.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnDeleteActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
         jPanel7.add(btnDelete, gridBagConstraints);
 
         javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
         jPanel12.setLayout(jPanel12Layout);
         jPanel12Layout.setHorizontalGroup(
             jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         jPanel12Layout.setVerticalGroup(
             jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.weighty = 1.0;
         jPanel7.add(jPanel12, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
         panelDTR.add(jPanel7, gridBagConstraints);
 
         jPanel13.setLayout(new java.awt.GridBagLayout());
 
         jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         jLabel17.setText("Total");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.weightx = 0.8;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
         jPanel13.add(jLabel17, gridBagConstraints);
 
         txtTotal.setEditable(false);
         txtTotal.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
         txtTotal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         txtTotal.setText(" ");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.2;
         jPanel13.add(txtTotal, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
         panelDTR.add(jPanel13, gridBagConstraints);
 
         jPanel14.setLayout(new java.awt.GridBagLayout());
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
         jPanel14.add(txtDateTo, gridBagConstraints);
 
         jLabel19.setText("To");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel14.add(jLabel19, gridBagConstraints);
 
         jLabel35.setText("Display");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 4;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
         jPanel14.add(jLabel35, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 5;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
         jPanel14.add(comboDisplayType, gridBagConstraints);
 
         javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
         jPanel15.setLayout(jPanel15Layout);
         jPanel15Layout.setHorizontalGroup(
             jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         jPanel15Layout.setVerticalGroup(
             jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 7;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         jPanel14.add(jPanel15, gridBagConstraints);
 
         jLabel44.setText("From");
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel14.add(jLabel44, gridBagConstraints);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
         jPanel14.add(txtDateFrom, gridBagConstraints);
 
         btnRefreshDTR.setText("Refresh");
         btnRefreshDTR.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRefreshDTRActionPerformed(evt);
             }
         });
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 6;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
         jPanel14.add(btnRefreshDTR, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         panelDTR.add(jPanel14, gridBagConstraints);
 
         tableDTR.setAutoCreateRowSorter(true);
         tableDTR.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
 
             }
         ));
         tableDTR.setFillsViewportHeight(true);
         tableDTR.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tableDTRMouseClicked(evt);
             }
         });
         jScrollPane4.setViewportView(tableDTR);
 
         jTabbedPane2.addTab("Time Entries", jScrollPane4);
 
         jPanel16.setLayout(new java.awt.GridBagLayout());
 
         tableJob.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane5.setViewportView(tableJob);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.ipadx = 430;
         gridBagConstraints.ipady = 395;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         jPanel16.add(jScrollPane5, gridBagConstraints);
 
         jTabbedPane2.addTab("Job Entries/Deliveries", jPanel16);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
         panelDTR.add(jTabbedPane2, gridBagConstraints);
 
         jTabbedPane1.addTab("Daily Time Record", panelDTR);
 
         panelPayslip.setLayout(new java.awt.GridBagLayout());
 
         txtPayslip.setColumns(20);
         txtPayslip.setRows(5);
         jScrollPane6.setViewportView(txtPayslip);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         panelPayslip.add(jScrollPane6, gridBagConstraints);
 
         jPanel19.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
 
         jLabel45.setText("Payroll Period");
         jPanel19.add(jLabel45);
 
         comboPayrollPeriod.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         comboPayrollPeriod.setForeground(java.awt.Color.blue);
         jPanel19.add(comboPayrollPeriod);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         panelPayslip.add(jPanel19, gridBagConstraints);
 
         jPanel20.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
 
         btnPayslip.setText("Generate Payslip");
         btnPayslip.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnPayslipActionPerformed(evt);
             }
         });
         jPanel20.add(btnPayslip);
 
         btnPayslipAll.setText("Generate All Payslip");
         btnPayslipAll.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnPayslipAllActionPerformed(evt);
             }
         });
         jPanel20.add(btnPayslipAll);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         panelPayslip.add(jPanel20, gridBagConstraints);
 
         jTabbedPane1.addTab("PaySlip", panelPayslip);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         panelOthers.add(jTabbedPane1, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.ipadx = 100;
         gridBagConstraints.ipady = 100;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         panelCenter.add(panelOthers, gridBagConstraints);
 
         jSplitPane1.setRightComponent(panelCenter);
 
         add(jSplitPane1, java.awt.BorderLayout.CENTER);
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
         MainApp.messagePanelObservable.callObserver(new PanelMessage("Employee", "remove"));
     }//GEN-LAST:event_btnCloseActionPerformed
 
     private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed
         ce = new Employee();
         initScreen();
     }//GEN-LAST:event_btnNewActionPerformed
 
     private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
         if (ce != null) {
 
             try {
                 saveScreen();
 
                 eDao.save(ce);
 
                 initTableView();
                 JOptionPane.showMessageDialog(this, "Employee information save.", "Employee", JOptionPane.INFORMATION_MESSAGE);
 
             } catch (Exception ex) {
                 JOptionErrorMessage.showErrorMessage(this.getClass().getCanonicalName(), ex);
             }
         }
     }//GEN-LAST:event_btnSaveActionPerformed
 
     private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_btnCancelActionPerformed
 
     private void tableEmployeesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableEmployeesMouseClicked
         if (evt.getClickCount() > 1) {
 
             JTable jTable = (JTable) evt.getSource();
             if (jTable.getRowCount() > 0) {
                 int row = jTable.getSelectedRow();
                 if (row < 0) {
                     return;
                 }
 
                 Employee v = (Employee) jTable.getValueAt(row, 3);
 
                 if (v != null) {
                     try {
                         ce = eDao.find(v.getId());
                         initScreen();
                     } catch (Exception ex) {
                         Logger.getLogger(EmployeeInformation.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             }
         }
     }//GEN-LAST:event_tableEmployeesMouseClicked
 
     private void btnPayslipAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPayslipAllActionPerformed
 //        KeyValue kv = (KeyValue) comboPayrollPeriod.getSelectedItem();
 //        if (kv == null) {
 //            return;
 //        } else {
 //        }
 //        try {
 //
 //            StringBuffer sb = new StringBuffer();
 //
 //            PayrollPeriod pp = PayrollPeriod.getPayrollPeriodByTid((Long) kv.getValue());
 //            LinkedHashMap<Long, Employee> emplist = PaySlipProcess.processPayslip(pp, null);
 //
 //            SimpleDateFormat sdf = MyDateFormatter.getSimpleDateTimeFormatter2();
 //
 //            List list = new ArrayList();
 //
 //
 //            HashMap parameters = new HashMap();
 //            CompanySetting cs = CompanySetting.companySetting();
 //
 //            parameters.put("REPORT_TITLE", cs.getName());
 //            String payroll_period = pp.getPayrollPeriodCode() + " - [" + sdf.format(pp.getDateFrom()) + "-" + sdf.format(pp.getDateTo()) + "]";
 //            parameters.put("PAYROLL_PERIOD", "Payroll Period: " + payroll_period);
 //            parameters.put("DATE_GENERATED", sdf.format(pp.getDatePrepared()));
 ////            parameters.put("PREPARED_BY", dbms.user.getFullName());
 ////            parameters.put("SUBREPORT_DIR", dbms.codebaseReports);
 //
 //            for (Employee emp : emplist.values()) {
 //
 //                if (emp.getPayslip().getPayslipDetails().size() == 0) {
 //                    continue;
 //                }
 //
 //                PaySlipReportObject psro = new PaySlipReportObject();
 //                psro.setEmployeeTid(emp.getId());
 //                System.out.println(emp.getPayslip());
 //                System.out.println(emp.getPayslip().getPayrollPeriod());
 //                System.out.println(emp.getPayslip().getPayrollPeriod().getPayrollPeriodCode());
 //
 //                double totalAdd = 0;
 //                int row = 1;
 //                for (PaySlipDetail psd : emp.getPayslip().getPayables()) {
 //                    PaySlipReportRow psrr = new PaySlipReportRow();
 //                    psrr.setRow(row++);
 //                    String psdString = psd.getDescription() + " (" + 0 + " X " + MyNumberFormatter.formatAmount(psd.getAmount()) + ") / " + 1;
 //                    psrr.setDescription(psdString);
 //                    psrr.setEmployeeName("Name: " + emp.getName());
 //
 //                    psrr.setPosition(emp.getPosition().getDescription());
 //                    psrr.setAmount(psd.getTotal());
 //                    psro.getList().add(psrr);
 //                    totalAdd = totalAdd + psd.getTotal();
 //                    System.out.println(psd.getRowNumber() + "     " + psd.getDescription() + " " + psd.getTotal());
 //                }
 //
 //                double totalLess = 0;
 //                for (PaySlipDetail psd : emp.getPayslip().getReceivables()) {
 //
 //                    PaySlipReportRow psrr = new PaySlipReportRow();
 //                    psrr.setRow(row++);
 //                    psrr.setDescription(psd.getDescription());
 //                    psrr.setEmployeeName(emp.getName());
 //                    psrr.setPosition(emp.getPosition().getDescription());
 //                    psrr.setAmount(psd.getTotal());
 //                    psro.getList().add(psrr);
 //
 //                    totalLess = totalLess + psd.getTotal();
 //
 //                    System.out.println(psd.getRowNumber() + "     " + psd.getDescription() + " " + psd.getTotal());
 //                }
 //                psro.setNetTotal(totalAdd - totalLess);
 //                list.add(psro);
 //
 //
 //            }
 //
 //            ReportViewerFactory rvf = new ReportViewerFactory("Payroll", parameters, list);
 //
 //            JRViewer jrv = rvf.getReport(false);
 //
 //            rvf.showReport(jrv);
 //
 //        } catch (Exception ex) {
 //            Logger.getLogger(PaySlipProcess.class.getName()).log(Level.SEVERE, null, ex);
 //        }
     }//GEN-LAST:event_btnPayslipAllActionPerformed
 
     private void btnPayslipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPayslipActionPerformed
 //        if (tableEmployees.getRowCount() > 0) {
 //            int row = tableEmployees.getSelectedRow();
 //            Employee v = (Employee) tableEmployees.getValueAt(row, 3);
 //
 //            if (v != null) {
 //                try {
 //                    ce = Employee.getEmployeeByTid(v.getId());
 //                    //   dbms.getDBInstance().ext().refresh(ce, Integer.MAX_VALUE);
 //                    initScreen();
 //                    processPayslip();
 //                } catch (Exception ex) {
 //                    Logger.getLogger(EmployeeInformation.class.getName()).log(Level.SEVERE, null, ex);
 //                }
 //            }
 //        }
     }//GEN-LAST:event_btnPayslipActionPerformed
 
     private void tableDTRMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableDTRMouseClicked
         if (evt.getClickCount() > 1) {
 
             JTable jTable = (JTable) evt.getSource();
             if (jTable.getRowCount() > 0) {
                 int row = jTable.getSelectedRow();
                 DailyTimeRecord dtr = (DailyTimeRecord) jTable.getValueAt(row, 2);
 
                 if (dtr != null) {
                     try {
                         openDTREntryDialog(dtr);
                     } catch (Exception ex) {
                         Logger.getLogger(EmployeeInformation.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             }
         }
     }//GEN-LAST:event_tableDTRMouseClicked
 
     private void btnRefreshDTRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshDTRActionPerformed
         initDTR();
     }//GEN-LAST:event_btnRefreshDTRActionPerformed
 
     private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
         if (tableDTR.getRowCount() > 0) {
             int row = tableDTR.getSelectedRow();
             if (row > -1) {
 //                TransactionDetail b = (TransactionDetail) tableDTR.getModel().getValueAt(row, 1);
 //
 //                if (b != null && !trans.isIsGenerated()) {
 //                    int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this details.", "Confirm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
 //                    if (result == JOptionPane.YES_OPTION) {
 //                        try {
 //                            trans.getTransactionDetails().remove(b);
 //                            initDetails();
 //                            dbms.getDBInstance().delete(b);
 //                        } catch (Exception ex) {
 //                            Logger.getLogger(AccountingInformation.class.getName()).log(Level.SEVERE, null, ex);
 //                        }
 //
 //                    }
 //
 //                } else {
 //                    JOptionPane.showMessageDialog(this, "Cannot delete generated details.");
 //                }
             } else {
                 JOptionPane.showMessageDialog(this, "Please select detail");
             }
         }
     }//GEN-LAST:event_btnDeleteActionPerformed
 
     private void btnInsertDetailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInsertDetailActionPerformed
         DailyTimeRecord dtr = new DailyTimeRecord(ce);
         openDTREntryDialog(dtr);
     }//GEN-LAST:event_btnInsertDetailActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnCancel;
     private javax.swing.JButton btnClose;
     private javax.swing.JButton btnDelete;
     private javax.swing.JButton btnInsertDetail;
     private javax.swing.JButton btnNew;
     private javax.swing.JButton btnPayslip;
     private javax.swing.JButton btnPayslipAll;
     private javax.swing.JButton btnRefreshDTR;
     private javax.swing.JButton btnSave;
     private javax.swing.ButtonGroup buttonGroup1;
     private javax.swing.JComboBox comboCompany;
     private javax.swing.JComboBox comboDepartment;
     private javax.swing.JComboBox comboDisplayType;
     private javax.swing.JComboBox comboEmploymentStatus;
     private javax.swing.JComboBox comboMaritalStatus;
     private javax.swing.JComboBox comboPayCode;
     private javax.swing.JComboBox comboPayrollPeriod;
     private javax.swing.JComboBox comboPosition;
     private javax.swing.JComboBox comboType;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel16;
     private javax.swing.JLabel jLabel17;
     private javax.swing.JLabel jLabel19;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel21;
     private javax.swing.JLabel jLabel22;
     private javax.swing.JLabel jLabel23;
     private javax.swing.JLabel jLabel24;
     private javax.swing.JLabel jLabel25;
     private javax.swing.JLabel jLabel26;
     private javax.swing.JLabel jLabel27;
     private javax.swing.JLabel jLabel28;
     private javax.swing.JLabel jLabel29;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel30;
     private javax.swing.JLabel jLabel31;
     private javax.swing.JLabel jLabel32;
     private javax.swing.JLabel jLabel33;
     private javax.swing.JLabel jLabel34;
     private javax.swing.JLabel jLabel35;
     private javax.swing.JLabel jLabel36;
     private javax.swing.JLabel jLabel37;
     private javax.swing.JLabel jLabel38;
     private javax.swing.JLabel jLabel39;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel40;
     private javax.swing.JLabel jLabel41;
     private javax.swing.JLabel jLabel42;
     private javax.swing.JLabel jLabel43;
     private javax.swing.JLabel jLabel44;
     private javax.swing.JLabel jLabel45;
     private javax.swing.JLabel jLabel46;
     private javax.swing.JLabel jLabel47;
     private javax.swing.JLabel jLabel48;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel10;
     private javax.swing.JPanel jPanel11;
     private javax.swing.JPanel jPanel12;
     private javax.swing.JPanel jPanel13;
     private javax.swing.JPanel jPanel14;
     private javax.swing.JPanel jPanel15;
     private javax.swing.JPanel jPanel16;
     private javax.swing.JPanel jPanel18;
     private javax.swing.JPanel jPanel19;
     private javax.swing.JPanel jPanel20;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JPanel jPanel8;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JScrollPane jScrollPane5;
     private javax.swing.JScrollPane jScrollPane6;
     private javax.swing.JSplitPane jSplitPane1;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JTabbedPane jTabbedPane2;
     private javax.swing.JLabel labelCompleteName;
     private javax.swing.JLabel labelName;
     private javax.swing.JPanel panelBottom;
     private javax.swing.JPanel panelCenter;
     private javax.swing.JPanel panelContactInfo;
     private javax.swing.JPanel panelContactInfo1;
     private javax.swing.JPanel panelContactInfo2;
     private javax.swing.JPanel panelContactInfo3;
     private javax.swing.JPanel panelContactInfo4;
     private javax.swing.JPanel panelDTR;
     private javax.swing.JPanel panelEmployeeData;
     private javax.swing.JPanel panelEmployeeInfo;
     private javax.swing.JPanel panelLeft;
     private javax.swing.JPanel panelOthers;
     private javax.swing.JPanel panelPayslip;
     private javax.swing.JPanel panelPersonalTab;
     private javax.swing.JPanel panelTop;
     private javax.swing.JRadioButton rbFemale;
     private javax.swing.JRadioButton rbMale;
     private javax.swing.JTable tableDTR;
     private javax.swing.JTable tableEmployees;
     private javax.swing.JTable tableJob;
     private javax.swing.JFormattedTextField textAllowance;
     private javax.swing.JTextField textBankAccountNo;
     private javax.swing.JFormattedTextField textBasicSalary;
     private javax.swing.JFormattedTextField textBenefits;
     private javax.swing.JFormattedTextField textDLoan1;
     private javax.swing.JFormattedTextField textDLoan2;
     private javax.swing.JFormattedTextField textDPagIbig;
     private javax.swing.JFormattedTextField textDPhilHealth;
     private javax.swing.JFormattedTextField textDSSS;
     private javax.swing.JFormattedTextField textDTax;
     private javax.swing.JFormattedTextField textDailyRate;
     private javax.swing.JFormattedTextField textHourlyRate;
     private javax.swing.JTextField textPagIbigNo;
     private javax.swing.JTextField textPhilHealthNo;
     private javax.swing.JTextField textSSSNo;
     private javax.swing.JFormattedTextField textSickLeave;
     private javax.swing.JTextField textStreet;
     private javax.swing.JTextField textTaxIdNo;
     private javax.swing.JFormattedTextField textVacationLeave;
     private javax.swing.JFormattedTextField txtContactNo;
     private org.jdesktop.swingx.JXDatePicker txtDateEnd;
     private org.jdesktop.swingx.JXDatePicker txtDateFrom;
     private org.jdesktop.swingx.JXDatePicker txtDateHired;
     private org.jdesktop.swingx.JXDatePicker txtDateOfBirth;
     private org.jdesktop.swingx.JXDatePicker txtDateTo;
     private javax.swing.JTextField txtEmployeeID;
     private javax.swing.JTextField txtFirstName;
     private javax.swing.JTextField txtLastName;
     private javax.swing.JTextField txtMiddleName;
     private javax.swing.JFormattedTextField txtNumberOfDependents;
     private javax.swing.JTextArea txtPayslip;
     private javax.swing.JTextField txtTotal;
     // End of variables declaration//GEN-END:variables
 
 //    private void initTreeView() {
 //        DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode("Employees");
 //
 //
 //
 //        LinkedHashMap<String, DefaultMutableTreeNode> positions = new LinkedHashMap<String, DefaultMutableTreeNode>();
 //
 //
 //        DefaultMutableTreeNode group = null;
 //
 //        for ( Employee e : dbms.getDBInstance().query(Employee.class)) {
 //
 //            Position p = e.getPosition();
 //
 //            String groupText = p != null ? p.getDescription() : "No Position";
 //
 //            if (positions.containsKey(groupText)) {
 //                group = positions.get(groupText);
 //
 //            } else {
 //
 //                group = new DefaultMutableTreeNode(groupText);
 //                positions.put(groupText, group);
 //            }
 //
 //            JCLDefaultTreeNode jdtn = new JCLDefaultTreeNode(e.getName(), e.getId(), groupText);
 //            System.out.println(e);
 //            group.add(jdtn);
 //
 //        }
 //
 //        for (DefaultMutableTreeNode _dmtn : positions.values()) {
 //            dmtn.add(_dmtn);
 //        }
 //
 //        treeEmployee.setModel(new DefaultTreeModel(dmtn));
 //
 //
 //
 //    }
     private void initComboBoxes() throws Exception {
 
         ComboBoxModel cbmPostion = new DefaultComboBoxModel(pDao.getPositions().toArray());
         comboPosition.setModel(cbmPostion);
 
         ComboBoxModel cbmCompany = new DefaultComboBoxModel(cDao.getCompanies().toArray());
         comboCompany.setModel(cbmCompany);
 
         ComboBoxModel cbmDepartment = new DefaultComboBoxModel(dDao.getDepartments().toArray());
         comboDepartment.setModel(cbmDepartment);
 
         for (MaritalStatus s : MaritalStatus.values()) {
             comboMaritalStatus.addItem(s.name());
         }
 
         for (EmploymentStatus es : EmploymentStatus.values()) {
             comboEmploymentStatus.addItem(es.name());
         }
 
         for (PayrollPeriodType ppt : PayrollPeriodType.values()) {
             comboType.addItem(ppt.name());
         }
 
         for (PayrollPeriodCode ppc : PayrollPeriodCode.values()) {
             comboPayCode.addItem(ppc.name());
         }
 
         for (DTRDisplayType ddt : DTRDisplayType.values()) {
             comboDisplayType.addItem(ddt.name());
         }
 
 //        for (PayrollPeriod p : dbms.getDBInstance().query(PayrollPeriod.class)) {
 //            String code = p.getPayrollPeriodType() + " :[" + sdf.format(p.getDateFrom()) + "-" + sdf.format(p.getDateTo()) + "] " + p.getPayrollPeriodCode();
 //            KeyValue kv = new KeyValue(code, p.getTid());
 //            comboPayrollPeriod.addItem(kv);
 //        }
     }
 
     private void initEmployee(Employee ce) {
         this.ce = ce;
         if (this.ce != null) {
             initScreen();
         }
     }
 
     private void initScreen() {
         disabledComponents(true);
         txtEmployeeID.setText(ce.getIdNumber());
 
         txtFirstName.setText(ce.getFirstName());
         txtLastName.setText(ce.getLastName());
         txtMiddleName.setText(ce.getMiddleName());
         txtDateOfBirth.setDate(ce.getDateOfBirth());
         txtNumberOfDependents.setValue(ce.getNumberOfDependents());
         comboMaritalStatus.setSelectedItem(ce.getMaritalStatus());
 
         labelCompleteName.setText(ce.getName());
         if (ce.getGender().equals("F")) {
             rbFemale.setSelected(true);
         } else {
             rbMale.setSelected(true);
         }
 
         comboEmploymentStatus.setSelectedItem(ce.getStatus());
         comboPosition.setSelectedItem(ce.getPosition());
         comboDepartment.setSelectedItem(ce.getDepartment());
         comboCompany.setSelectedItem(ce.getCompany());
         comboType.setSelectedItem(ce.getPayType() == null ? "Variable" : ce.getPayType());
 
         txtDateHired.setDate(ce.getDateHired());
         txtDateEnd.setDate(ce.getDateEnd());
 
         textBasicSalary.setValue(ce.getSalary());
         textDailyRate.setValue(ce.getDailyRate());
         textHourlyRate.setValue(ce.getHourRate());
         textBenefits.setValue(ce.getBenefits());
         textAllowance.setValue(ce.getAllowance());
 
         textSickLeave.setValue(ce.getSickLeave());
         textVacationLeave.setValue(ce.getVacationLeave());
 
 
         textDTax.setValue(ce.getTaxWithheld());
         textDSSS.setValue(ce.getSssD());
         textDPagIbig.setValue(ce.getPagibigD());
         textDPhilHealth.setValue(ce.getPhilhealthD());
 
         textBankAccountNo.setText(ce.getBankAccountNumber());
         textTaxIdNo.setText(ce.getTaxID());
         textSSSNo.setText(ce.getSssNo());
         textPagIbigNo.setText(ce.getPagibigNo());
         textPhilHealthNo.setText(ce.getPhilhealthNo());
 
 
         textStreet.setText(ce.getAddress());
 
         txtContactNo.setText(ce.getTelephoneNo());
 
 
         txtDateTo.setDate(new Date());
         txtDateFrom.setDate(new Date());
 
         initLoanTableView();
         initDTR();
         initJOB();
 
 
 
     }
 
     private void saveScreen() {
 
         ce.setIdNumber(txtEmployeeID.getText());
         ce.setFirstName(txtFirstName.getText());
         ce.setLastName(txtLastName.getText());
         ce.setMiddleName(txtMiddleName.getText());
         ce.setDateOfBirth(txtDateOfBirth.getDate());
         ce.setMaritalStatus(comboMaritalStatus.getSelectedItem().toString());
         ce.setNumberOfDependents(Integer.parseInt(txtNumberOfDependents.getText()));
         ce.setName(labelCompleteName.getText());
 
         if (rbFemale.isSelected()) {
             ce.setGender("F");
         } else {
             ce.setGender("M");
         }
 
         ce.setStatus(comboEmploymentStatus.getSelectedItem().toString());
 
         Position pp = (Position) comboPosition.getSelectedItem();
         ce.setPosition(pp);
 
         Department dt = (Department) comboDepartment.getSelectedItem();
         ce.setDepartment(dt);
 
 
         Company co = (Company) comboCompany.getSelectedItem();
         ce.setCompany(co);
 
         ce.setPayType(comboType.getSelectedItem().toString());
         ce.setPayCode(comboPayCode.getSelectedItem().toString());
 
         ce.setDateHired(txtDateHired.getDate());
         ce.setDateEnd(txtDateEnd.getDate());
 
 
         ce.setSalary(Double.valueOf(textBasicSalary.getText()));
         ce.setDailyRate(Double.valueOf(textDailyRate.getText()));
         ce.setHourRate(Double.valueOf(textHourlyRate.getText()));
         ce.setBenefits(Double.valueOf(textBenefits.getText()));
         ce.setAllowance(Double.valueOf(textAllowance.getText()));
        ce.setSickLeave(Integer.valueOf(textSickLeave.getText()));
        ce.setVacationLeave(Integer.valueOf(textVacationLeave.getText()));
 
         ce.setTaxWithheld(Double.valueOf(textDTax.getText()));
         ce.setSssD(Double.valueOf(textDSSS.getText()));
         ce.setPagibigD(Double.valueOf(textDPagIbig.getText()));
         ce.setPhilhealthD(Double.valueOf(textDPhilHealth.getText()));
 
 
         ce.setBankAccountNumber(textBankAccountNo.getText());
         ce.setTaxID(textTaxIdNo.getText());
         ce.setSssNo(textSSSNo.getText());
         ce.setPagibigNo(textPagIbigNo.getText());
         ce.setPhilhealthNo(textPhilHealthNo.getText());
 
         ce.setAddress(textStreet.getText());
         ce.setTelephoneNo(txtContactNo.getText());
 
     }
 
     private void initLoanTableView() {
 //        try {
 //
 //            NonEditableDefaultTableModel dtm = new NonEditableDefaultTableModel();
 //            DateTableCellRenderer dtcr = new DateTableCellRenderer("MM/dd/yyyy");
 //            dtm.setColumnIdentifiers(new String[]{"Date", "Transaction #", "Description", "Amount", "Balance"});
 //            List<Transaction> transList = new ArrayList<Transaction>();
 //            //     transList = Transaction.getTransactionByClientEmployeeWithBalance(billd);
 //
 //            transList = Transaction.getAllTransactionWithBalanceEmployee2(ce);
 //
 //            for (Transaction v : transList) {
 //                if (v != null) {
 //                    String companyName = v.getClient() != null ? v.getClient().getCompanyName() : (v.getEmployee() != null ? v.getEmployee().getName() : "");
 //                    Object[] o = null;
 //                    o = new Object[]{v.getTransactionDate(), v, v.getNotes(), v.getTotalAmount(), v.getGenBalance()};
 //                    dtm.addRow(o);
 //                }
 //            }
 //            tableInvoice.setModel(dtm);
 //            NumberTableCellRenderer ntcr = new NumberTableCellRenderer();
 //            tableInvoice.getColumn("Date").setCellRenderer(dtcr);
 //            tableInvoice.getColumn("Amount").setCellRenderer(ntcr);
 //            tableInvoice.getColumn("Balance").setCellRenderer(ntcr);
 //            tableInvoice.getColumn("Date").setMaxWidth(85);
 //            // tableInvoice.getColumn("Description").setMaxWidth(110);
 //            tableInvoice.getColumn("Transaction #").setMaxWidth(200);
 //            tableInvoice.getColumn("Transaction #").setMinWidth(200);
 //
 //            tableInvoice.getColumn("Amount").setMaxWidth(110);
 //            tableInvoice.getColumn("Balance").setMaxWidth(110);
 //        } catch (Exception ex) {
 //            Logger.getLogger(EmployeeInformation.class.getName()).log(Level.SEVERE, null, ex);
 //        }
     }
 
     private void initDTR() {
         try {
 
             NonEditableDefaultTableModel dtm = new NonEditableDefaultTableModel();
             DateTableCellRenderer dtcr = new DateTableCellRenderer("MM/dd/yy");
             dtm.setColumnIdentifiers(new String[]{"#", "Date", "Type", "In 1", "Out 1", "In 2", "Out 2", "Notes", "Process", "DTR"});
 
 
             List<DailyTimeRecord> list = new ArrayList<DailyTimeRecord>();
 
             Date fDate = txtDateFrom.getDate();
             Date tDate = txtDateTo.getDate();
 
             boolean withDate = false;
 
             if (fDate != null && tDate != null) {
                 withDate = true;
             } else if (fDate != null || tDate != null) {
                 JOptionPane.showMessageDialog(null, "Please enter both date or remove both date");
                 System.out.println(fDate);
                 System.out.println(tDate);
             }
 
             list = PaySlipProcess.retreiveDTR(fDate, tDate, ce);
 
             int rowCounter = 1;
 
             for (DailyTimeRecord v : list) {
 
                 if (v != null && v.getEmployee().getId() == ce.getId()) {
 
                     Object[] o = new Object[]{rowCounter++, v.getTransactionDate(), v,
                         stf.format(v.getTimeIn1()), stf.format(v.getTimeOut1()),
                         stf.format(v.getTimeIn2()), stf.format(v.getTimeOut2()),
                         v.getNotes(), v.getProcess(), v.getIsDTR()};
                     dtm.addRow(o);
                 }
             }
 
 
             tableDTR.setModel(dtm);
             NumberTableCellRenderer ntcr = new NumberTableCellRenderer();
             tableDTR.getColumn("Date").setCellRenderer(dtcr);
 
 
             tableDTR.getColumn("Type").setMaxWidth(110);
             tableDTR.getColumn("Date").setMaxWidth(80);
             tableDTR.getColumn("#").setMaxWidth(30);
             tableDTR.getColumn("In 1").setMaxWidth(50);
             tableDTR.getColumn("Out 1").setMaxWidth(50);
             tableDTR.getColumn("In 2").setMaxWidth(50);
             tableDTR.getColumn("Out 2").setMaxWidth(50);
             tableDTR.getColumn("Process").setMaxWidth(50);
             tableDTR.getColumn("DTR").setMaxWidth(50);
 
 
         } catch (Exception ex) {
             Logger.getLogger(EmployeeInformation.class.getName()).log(Level.SEVERE, null, ex);
         }
 
 
     }
 
     private void initJOB() {
         //read job from trucking or box deliveries
     }
 
     private void processPayslip() {
 //        KeyValue kv = (KeyValue) comboPayrollPeriod.getSelectedItem();
 //        if (kv == null) {
 //            return;
 //        } else {
 //        }
 //        try {
 //
 //            StringBuffer sb = new StringBuffer();
 //
 //            PayrollPeriod pp = PayrollPeriod.getPayrollPeriodByTid((Long) kv.getValue());
 //            LinkedHashMap<Long, Employee> emplist = PaySlipProcess.processPayslip(pp, ce);
 //
 //            SimpleDateFormat sdf = MyDateFormatter.getSimpleDateTimeFormatter2();
 //
 //            for (Employee emp : emplist.values()) {
 //
 //                if (emp.getId() != ce.getId()) {
 //                    continue;
 //                }
 //
 //                System.out.println("================================");
 //                System.out.println(emp.getName());
 //
 //                sb.append("Payroll Period: " + sdf.format(pp.getDateFrom()) + "-" + sdf.format(pp.getDateTo()) + "\n");
 //                sb.append("Name: " + emp.getName() + "\n");
 //                sb.append("Position: " + emp.getPosition().getDescription() + "\n");
 //
 //
 //                System.out.println(emp.getPayslip().getPayrollPeriod().getPayrollPeriodCode());
 //                System.out.println("Add: ");
 //                sb.append("Summary:\n");
 //                double totalAdd = 0;
 //                for (PaySlipDetail psd : emp.getPayslip().getPayables()) {
 //                    totalAdd = totalAdd + psd.getTotal();
 //                    String psdString = psd.getRowNumber() + " " + psd.getDescription() + " (" + 0+ " x " + psd.getAmount() + ")/" + 1 + " = " + psd.getTotal();
 //                    sb.append(psdString + "\n");
 //                    System.out.println(psd.getRowNumber() + "     " + psd.getDescription() + " " + psd.getTotal());
 //                }
 //                System.out.println("     Less: ");
 //                sb.append("Deduction:\n");
 //                double totalLess = 0;
 //                for (PaySlipDetail psd : emp.getPayslip().getReceivables()) {
 //                    totalLess = totalLess + psd.getTotal();
 //                    String psdString = psd.getRowNumber() + " " + psd.getDescription() + " " + psd.getTotal();
 //                    sb.append(psdString + "\n");
 //                    System.out.println(psd.getRowNumber() + "     " + psd.getDescription() + " " + psd.getTotal());
 //                }
 //
 //
 //                sb.append("\n");
 //                sb.append("Total: " + (totalAdd - totalLess));
 //                sb.append("\n");
 //     //         sb.append(dbms.user.getFullName() + "\n");
 //
 //                txtPayslip.setText(sb.toString());
 //
 //            }
 //
 //        } catch (Exception ex) {
 //            Logger.getLogger(PaySlipProcess.class.getName()).log(Level.SEVERE, null, ex);
 //        }
     }
 
     private void openDTREntryDialog(DailyTimeRecord d) {
         DTREntry dui = new DTREntry(null, true, d);
         dui.setLocationRelativeTo(this);
         dui.setVisible(true);
         if (dui.selectedButton == SelectedButton.Save) {
             try {
                 initDTR();
             } catch (Exception ex) {
                 Logger.getLogger(EmployeeInformation.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 }
