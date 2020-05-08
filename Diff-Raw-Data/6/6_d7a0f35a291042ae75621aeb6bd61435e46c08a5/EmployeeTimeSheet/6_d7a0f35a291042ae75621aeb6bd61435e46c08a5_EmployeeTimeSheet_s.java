 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * medewerkerInfo.java
  *
  * Created on 8-jan-2011, 14:25:19
  */
 package view;
 
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Locale;
 import javax.swing.ImageIcon;
 import javax.swing.table.DefaultTableModel;
 import model.Employee;
 import model.WorkHours;
 import roosterprogramma.RoosterProgramma;
 import roosterprogramma.Translater;
 import roosterprogramma.Utils;
 import roosterprogramma.WhiteRenderer;
 
 /**
  *
  * @author Dark
  */
 public class EmployeeTimeSheet extends javax.swing.JPanel {
 
     private Employee employee;
     private DefaultTableModel model;
     private Calendar calendar = Calendar.getInstance();
     private int year, month, modifier = 0;
     private DecimalFormat format = new DecimalFormat("0.00");
     private static final boolean DEBUG = false;
     private ItemListener changeListener = new ItemListener() {
 
         @Override
         public void itemStateChanged(ItemEvent e) {
             int selectedYear = Integer.parseInt(cmbYear.getSelectedItem().toString());
             int selectedMonth = Integer.parseInt(cmbMonth.getSelectedItem().toString());
             handleTime(selectedYear, selectedMonth);
         }
     };
 
     /**
      * Creates new form medewerkerInfo
      *
      * @param employee
      * @param year
      * @param month
      */
     public EmployeeTimeSheet(Employee employee, int year, int month) {
         this.employee = employee;
         calendar.set(Calendar.YEAR, year);
         calendar.set(Calendar.MONTH, month - 1);
         this.year = year;
         this.month = month;
         initComponents();
         initializeTable();
         fillInfoTable();
         fillVerantwoordingTable();
         fillBoxes();
         calculateTotal();
     }
 
     private void initializeTable() {
         ImageIcon plusIcon = new ImageIcon(getClass().getResource("/images/btnPlus.png"));
         ImageIcon minusIcon = new ImageIcon(getClass().getResource("/images/btnMinus.png"));
         plusIcon = new ImageIcon(plusIcon.getImage().getScaledInstance(28, 28, java.awt.Image.SCALE_SMOOTH));
         minusIcon = new ImageIcon(minusIcon.getImage().getScaledInstance(28, 28, java.awt.Image.SCALE_SMOOTH));
         btnNextYear.setIcon(plusIcon);
         btnPreviousYear.setIcon(minusIcon);
         btnNextMonth.setIcon(plusIcon);
         btnPreviousMonth.setIcon(minusIcon);
         tblTimesheet.setModel(new DefaultTableModel() {
 
             @Override
             public boolean isCellEditable(int rowIndex, int colIndex) {
                 return !model.getColumnName(colIndex).contains("Compensatie") && colIndex != 0 && !model.getColumnName(colIndex).equals("Ingeroosterd") && !model.getColumnName(colIndex).equals("Totaal") && !model.getValueAt(rowIndex, 0).toString().equals("Totaal");
             }
 
             @Override
             public void fireTableCellUpdated(int row, int column) {
                 if (column < model.getColumnCount() - 4 && !model.getValueAt(row, 0).equals("Totaal")) {
                     calculateTotal();
                 }
                 super.fireTableCellUpdated(row, column);
             }
         });
     }
 
     private void fillBoxes() {
         for (int i = -20; i <= 20; i++) {
             cmbYear.addItem(calendar.get(Calendar.YEAR) + i);
         }
         for (int j = 1; j <= 12; j++) {
             cmbMonth.addItem(j);
         }
         cmbYear.setSelectedItem(year);
         cmbMonth.setSelectedItem(month);
         cmbYear.addItemListener(changeListener);
         cmbMonth.addItemListener(changeListener);
     }
 
     private void fillInfoTable() {
         ((DefaultTableModel) tblEmployeeInformation.getModel()).addRow(new Object[]{
                     employee.getEmployeeNumber(),
                     employee.getFirstName(),
                     employee.getFamilyName(),
                     employee.isFullTime() ? "Ja" : "Nee",
                     employee.isPartTime() ? "Ja" : "Nee",
                     employee.isCallWorker() ? "Ja" : "Nee"
                 });
         for (int i = 0; i < tblEmployeeInformation.getColumnCount(); i++) {
             tblEmployeeInformation.getColumnModel().getColumn(i).setCellRenderer(new WhiteRenderer());
         }
     }
 
     private void fillVerantwoordingTable() {
         model = (DefaultTableModel) tblTimesheet.getModel();
         model.addColumn("Dag van de Maand");
         if (employee.isClerk()) {
             model.addColumn("Ingeroosterd");
         } else {
             modifier = 1;
         }
         model.addColumn("Gewerkt");
         model.addColumn("Vakantie");
         model.addColumn("ADV");
         model.addColumn("Ziek");
         model.addColumn("Speciaal Verlof");
         model.addColumn("Opg. compensatie");
         model.addColumn("Totaal");
         model.addColumn("Opmerking");
         model.addColumn("Compensatie 150");
         model.addColumn("Compensatie 200");
         int daysOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
         for (int i = 1; i <= daysOfMonth; i++) {
             calendar.set(Calendar.DAY_OF_MONTH, i);
             WorkHours hour = RoosterProgramma.getQueryManager().getWorkHours(employee, getYear() + "-" + getMonth() + "-" + getDay());
             Object[] fields = employee.isClerk() ? new Object[11] : new Object[10];
             fields[0] = calendar.get(Calendar.DAY_OF_MONTH) + " - " + Translater.Translate(calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH));
             if (employee.isClerk()) {
                 fields[1] = hour.getShouldWorkHours();
             }
             fields[2 - modifier] = (hour.getWorked() == 0.0 ? "" : hour.getWorked());
             fields[3 - modifier] = (hour.getVacation() == 0.0 ? "" : hour.getVacation());
             fields[4 - modifier] = (hour.getADV() == 0.0 ? "" : hour.getADV());
             fields[5 - modifier] = (hour.getIllness() == 0.0 ? "" : hour.getIllness());
             fields[6 - modifier] = (hour.getLeave() == 0.0 ? "" : hour.getLeave());
             fields[7 - modifier] = (hour.getWithdrawnCompensation() == 0.0 ? "" : hour.getWithdrawnCompensation());
             fields[9 - modifier] = hour.getComment();
             model.addRow(fields);
         }
         Object[] fields = employee.isClerk() ? new Object[]{"Totaal", 0, 0, 0, 0, 0, 0, 0, 0} : new Object[]{"Totaal", 0, 0, 0, 0, 0, 0, 0};
         model.addRow(fields);
         for (int i = 1; i <= (11 - modifier); i++) {
             tblTimesheet.getColumnModel().getColumn(i).setPreferredWidth(100);
         }
         tblTimesheet.getColumnModel().getColumn(0).setPreferredWidth(120);
         tblTimesheet.getColumnModel().getColumn(9 - modifier).setPreferredWidth(400);
         for (int i = 0; i < tblTimesheet.getColumnCount(); i++) {
             tblTimesheet.getColumnModel().getColumn(i).setCellRenderer(new WhiteRenderer());
         }
     }
 
     private void calculateVacation() {
         if (employee.isCallWorker()) {
             double gewerkt = 0;
             double ziekte = 0;
             for (int i = 0; i < model.getColumnCount(); i++) {
                 if (model.getColumnName(i).equalsIgnoreCase("Gewerkt")) {
                     gewerkt = Double.parseDouble(model.getValueAt(model.getRowCount() - 1, i).toString().replace(",", "."));
                 } else if (model.getColumnName(i).equalsIgnoreCase("Ziekte")) {
                     ziekte = Double.parseDouble(model.getValueAt(model.getRowCount() - 1, i).toString().replace(",", "."));
                 }
             }
             double vakantieUren = Double.valueOf((gewerkt + ziekte) * (employee.getVacationPercentage() / 100));
             lblVacationHours.setText(Double.toString(vakantieUren));
         } else {
             lblVacationHours.setVisible(false);
             lblExpVacationHours.setVisible(false);
         }
     }
 
     private void calculateTotal() {
         // Totaalkolom
         for (int i = 0; i < model.getRowCount(); i++) { // Voor iedere rij
             double totalHours = 0;
             for (int j = -modifier + 2; j < model.getColumnCount() - 4; j++) {    // Alle rijen behalve degene vanaf Totaal
                 if (model.getValueAt(i, j) != null && !model.getValueAt(i, j).toString().isEmpty()) {
                     totalHours += verifyInput(i, j);
                 }
             }
             model.setValueAt(totalHours, i, model.getColumnCount() - 4);
         }
         // Totaalrij
         for (int k = 1; k < model.getColumnCount() - 3; k++) {  // Voor iedere kolom behalve Opmerking en Compensatie
             double totalHours = 0;
             for (int l = 0; l < model.getRowCount() - 1; l++) { // Voor iedere rij
                 if (model.getValueAt(l, k) != null && !model.getValueAt(l, k).toString().isEmpty()) {
                     totalHours += verifyInput(l, k);
                 }
             }
             model.setValueAt(totalHours, model.getRowCount() - 1, k);
         }
         // Compensatie
         for (int k = 1; k < model.getColumnCount() - 3; k++) {  // Voor iedere kolom behalve Opmerking en Compensatie
            for (int l = 0; l < model.getRowCount() - 2; l++) { // Voor iedere rij behalve Totaal
                 if (model.getColumnName(k).toString().equals("Gewerkt")) {  // Als de kolomnaam gelijk is aan Gewerkt
                     if (model.getValueAt(l, k) != null && !model.getValueAt(l, k).toString().isEmpty()) {
                         double gewerkt = Double.parseDouble(model.getValueAt(l, k).toString().replace(",", "."));
                         String pieces[] = model.getValueAt(l, 0).toString().split(" - ");
                         Calendar today = Calendar.getInstance();
                         today.set(year, month - 1, Integer.parseInt(pieces[0]));
                         if (Utils.isHoliday(today)) {
                             model.setValueAt(format.format(gewerkt), l, model.getColumnCount() - 1);
                         } else if (pieces[1].equalsIgnoreCase("Zondag")) {
                             model.setValueAt(format.format(gewerkt / 2), l, model.getColumnCount() - 2);
                         }
                     }
                 }
             }
         }
         tblTimesheet.repaint();
         calculateVacation();
     }
 
     private double verifyInput(int row, int column) {
         String value = model.getValueAt(row, column).toString().replace(",", ".");
         Double hours = 0.0;
         if (Utils.isNumeric(value)) {
             hours = Double.parseDouble(value);
         } else if (value.equalsIgnoreCase("v")) {
         } else if (value.equalsIgnoreCase("z")) {
         } else if (value.equalsIgnoreCase("c")) {
         } else if (value.equals("*")) {
         } else {
             Utils.showMessage("Fout opgetreden, foutieve invoer (" + value + ") in veld (" + row + ", " + column + ")", "Fout!", null, false);
         }
         return hours;
     }
 
     private void handleTime(int year, int month) {
         int newMonth = month;
         int newYear = year;
         if (newMonth == 0) {
             newMonth = 12;
             newYear -= 1;
         } else if (newMonth == 13) {
             newMonth = 1;
             newYear += 1;
         }
         RoosterProgramma.getInstance().showPanel(new EmployeeTimeSheet(employee, newYear, newMonth));
     }
 
     private String getYear() {
         return year < 10 ? "0" + Integer.toString(year) : Integer.toString(year);
     }
 
     private String getMonth() {
         return month < 10 ? "0" + Integer.toString(month) : Integer.toString(month);
     }
 
     private String getDay() {
         return calendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)) : Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
     }
 
     private boolean isCorrectlyFilled() {
         if (!employee.isClerk()) {
             return true;
         }
         for (int i = 0; i < tblTimesheet.getRowCount(); i++) {
             String ingeroosterd = model.getValueAt(i, 1).toString();
             if (!ingeroosterd.isEmpty()) {
                 if (Utils.isNumeric(ingeroosterd)) {
                     String shouldWork = model.getValueAt(i, 1).toString().replace(",", ".");
                     double haveWorked = Double.parseDouble(model.getValueAt(i, tblTimesheet.getColumnCount() - 4).toString().replace(",", "."));
                     if (Utils.isNumeric(shouldWork)) {
                         if (haveWorked < Double.parseDouble(shouldWork)) {
                             String[] pieces = model.getValueAt(i, 0).toString().split(" - ");
                             Utils.showMessage("De urenverantwoording voor " + pieces[1] + " de " + pieces[0] + "e komt niet overeen met de ingeroosterde uren.", "Foutieve urenverantwoording.", "", false);
                             return false;
                         }
                     }
                 } else if (ingeroosterd.equalsIgnoreCase("v") || ingeroosterd.equalsIgnoreCase("z") || ingeroosterd.equalsIgnoreCase("*") || ingeroosterd.equalsIgnoreCase("c")) {
                 } else {
                     Utils.showMessage("Bij de " + (i + 1) + "e staat een teken, geen getal.\nAlleen in het rooster hebben letters betekenis.", "Foutieve urenverantwoording.", "", false);
                     return false;
                 }
             }
         }
         return true;
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         btnBack = new javax.swing.JButton();
         btnSave = new javax.swing.JButton();
         lblExpVacationHours = new javax.swing.JLabel();
         lblVacationHours = new javax.swing.JLabel();
         jspTimesheet = new javax.swing.JScrollPane();
         tblTimesheet = new javax.swing.JTable();
         jspEmployeeInformation = new javax.swing.JScrollPane();
         tblEmployeeInformation = new javax.swing.JTable();
         pnlDateSelect = new javax.swing.JPanel();
         jpTimeNavigation = new javax.swing.JPanel();
         jpBox = new javax.swing.JPanel();
         jpFlowYear = new javax.swing.JPanel();
         btnPreviousYear = new javax.swing.JButton();
         cmbYear = new javax.swing.JComboBox<Integer>();
         btnNextYear = new javax.swing.JButton();
         jpFlowMonth = new javax.swing.JPanel();
         btnPreviousMonth = new javax.swing.JButton();
         cmbMonth = new javax.swing.JComboBox<Integer>();
         btnNextMonth = new javax.swing.JButton();
 
         setBackground(new java.awt.Color(153, 204, 255));
 
         btnBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/btnPrevious.png"))); // NOI18N
         btnBack.setToolTipText("Vorige");
         btnBack.setContentAreaFilled(false);
         btnBack.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBackActionPerformed(evt);
             }
         });
 
         btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/btnSave.png"))); // NOI18N
         btnSave.setToolTipText("Opslaan");
         btnSave.setContentAreaFilled(false);
         btnSave.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSaveActionPerformed(evt);
             }
         });
 
         lblExpVacationHours.setText("Opgebouwde vakantieuren:");
 
         lblVacationHours.setText("0");
 
         tblTimesheet.setBackground(new java.awt.Color(153, 204, 255));
         tblTimesheet.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
 
             }
         ));
         tblTimesheet.setFillsViewportHeight(true);
         tblTimesheet.getTableHeader().setReorderingAllowed(false);
         jspTimesheet.setViewportView(tblTimesheet);
 
         jspEmployeeInformation.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
 
         tblEmployeeInformation.setBackground(new java.awt.Color(153, 204, 255));
         tblEmployeeInformation.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Personeelsnummer", "Voornaam", "Achternaam", "Fulltime", "Parttime", "Oproepkracht"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false, false
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         tblEmployeeInformation.setFillsViewportHeight(true);
         jspEmployeeInformation.setViewportView(tblEmployeeInformation);
 
         pnlDateSelect.setBackground(new java.awt.Color(153, 204, 255));
 
         jpTimeNavigation.setBackground(new java.awt.Color(153, 204, 255));
 
         jpBox.setLayout(new javax.swing.BoxLayout(jpBox, javax.swing.BoxLayout.Y_AXIS));
 
         jpFlowYear.setBackground(new java.awt.Color(153, 204, 255));
 
         btnPreviousYear.setToolTipText("Vorig jaar");
         btnPreviousYear.setContentAreaFilled(false);
         btnPreviousYear.setMaximumSize(new java.awt.Dimension(28, 28));
         btnPreviousYear.setMinimumSize(new java.awt.Dimension(28, 28));
         btnPreviousYear.setPreferredSize(new java.awt.Dimension(28, 28));
         btnPreviousYear.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnPreviousYearActionPerformed(evt);
             }
         });
         jpFlowYear.add(btnPreviousYear);
 
         cmbYear.setMaximumSize(new java.awt.Dimension(50, 28));
         cmbYear.setMinimumSize(new java.awt.Dimension(50, 28));
         cmbYear.setPreferredSize(new java.awt.Dimension(50, 28));
         jpFlowYear.add(cmbYear);
 
         btnNextYear.setToolTipText("Volgend jaar");
         btnNextYear.setContentAreaFilled(false);
         btnNextYear.setMaximumSize(new java.awt.Dimension(28, 28));
         btnNextYear.setMinimumSize(new java.awt.Dimension(28, 28));
         btnNextYear.setPreferredSize(new java.awt.Dimension(28, 28));
         btnNextYear.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnNextYearActionPerformed(evt);
             }
         });
         jpFlowYear.add(btnNextYear);
 
         jpBox.add(jpFlowYear);
 
         jpFlowMonth.setBackground(new java.awt.Color(153, 204, 255));
 
         btnPreviousMonth.setToolTipText("Vorige maand");
         btnPreviousMonth.setContentAreaFilled(false);
         btnPreviousMonth.setMaximumSize(new java.awt.Dimension(28, 28));
         btnPreviousMonth.setMinimumSize(new java.awt.Dimension(28, 28));
         btnPreviousMonth.setPreferredSize(new java.awt.Dimension(28, 28));
         btnPreviousMonth.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnPreviousMonthActionPerformed(evt);
             }
         });
         jpFlowMonth.add(btnPreviousMonth);
 
         cmbMonth.setMaximumSize(new java.awt.Dimension(50, 28));
         cmbMonth.setMinimumSize(new java.awt.Dimension(50, 28));
         cmbMonth.setPreferredSize(new java.awt.Dimension(50, 28));
         jpFlowMonth.add(cmbMonth);
 
         btnNextMonth.setToolTipText("Volgende maand");
         btnNextMonth.setContentAreaFilled(false);
         btnNextMonth.setMaximumSize(new java.awt.Dimension(28, 28));
         btnNextMonth.setMinimumSize(new java.awt.Dimension(28, 28));
         btnNextMonth.setPreferredSize(new java.awt.Dimension(28, 28));
         btnNextMonth.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnNextMonthActionPerformed(evt);
             }
         });
         jpFlowMonth.add(btnNextMonth);
 
         jpBox.add(jpFlowMonth);
 
         javax.swing.GroupLayout jpTimeNavigationLayout = new javax.swing.GroupLayout(jpTimeNavigation);
         jpTimeNavigation.setLayout(jpTimeNavigationLayout);
         jpTimeNavigationLayout.setHorizontalGroup(
             jpTimeNavigationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpTimeNavigationLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jpBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jpTimeNavigationLayout.setVerticalGroup(
             jpTimeNavigationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jpTimeNavigationLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jpBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pnlDateSelect.add(jpTimeNavigation);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(pnlDateSelect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jspEmployeeInformation, javax.swing.GroupLayout.DEFAULT_SIZE, 902, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(btnBack)
                         .addGap(18, 18, 18)
                         .addComponent(lblExpVacationHours)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(lblVacationHours)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(btnSave))
                     .addComponent(jspTimesheet, javax.swing.GroupLayout.Alignment.TRAILING))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jspEmployeeInformation, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jspTimesheet, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(pnlDateSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(btnBack)
                         .addComponent(btnSave))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(lblExpVacationHours)
                         .addComponent(lblVacationHours)))
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
         if (tblTimesheet.getCellEditor() != null) {
             tblTimesheet.getCellEditor().stopCellEditing();
         }
         if (isCorrectlyFilled()) {
             ArrayList<Integer> failures = new ArrayList<Integer>();
             for (int i = 0; i < model.getRowCount() - 1; i++) {
                 String date = getYear() + "-" + getMonth() + "-" + model.getValueAt(i, 0).toString().split(" - ")[0];
                 boolean changed = false;
                 WorkHours hour = RoosterProgramma.getQueryManager().getWorkHours(employee, date);
                 for (int j = 0; j < model.getColumnCount() - 1; j++) {
                     Object tmpValue = model.getValueAt(i, j);
                     if (tmpValue != null) {
                         String value = tmpValue.toString();
                         String columnName = model.getColumnName(j);
                         if ((model.getColumnName(1).equals("Ingeroosterd") || !employee.isClerk()) // Alle rijen behalve Totaal
                                 && !model.getValueAt(i, j).toString().isEmpty()) {  // Vakje is niet leeg
                             if (model.getColumnName(j).equals("Gewerkt")) {
                                 hour.setWorked(value.isEmpty() ? 0 : Double.parseDouble(value.replace(",", ".")));
                                 changed = true;
                                 if (DEBUG) {
                                     System.out.println(hour.getWorked());
                                 }
                             } else if (columnName.equals("Compensatie 150")) {
                                 hour.setCompensation150(value.isEmpty() ? 0 : Double.parseDouble(value.replace(",", ".")));
                                 changed = true;
                                 if (DEBUG) {
                                     System.out.println(hour.getCompensation150());
                                 }
                             } else if (columnName.equals("Compensatie 200")) {
                                 hour.setCompensation200(value.isEmpty() ? 0 : Double.parseDouble(value.replace(",", ".")));
                                 changed = true;
                                 if (DEBUG) {
                                     System.out.println(hour.getCompensation200());
                                 }
                             } else if (columnName.equals("Vakantie")) {
                                 hour.setVacation(value.isEmpty() ? 0 : Double.parseDouble(value.replace(",", ".")));
                                 changed = true;
                                 if (DEBUG) {
                                     System.out.println(hour.getVacation());
                                 }
                             } else if (columnName.equals("ADV")) {
                                 hour.setADV(value.isEmpty() ? 0 : Double.parseDouble(value.replace(",", ".")));
                                 changed = true;
                                 if (DEBUG) {
                                     System.out.println(hour.getADV());
                                 }
                             } else if (columnName.equals("Ziek")) {
                                 hour.setIllness(value.isEmpty() ? 0 : Double.parseDouble(value.replace(",", ".")));
                                 changed = true;
                                 if (DEBUG) {
                                     System.out.println(hour.getIllness());
                                 }
                             } else if (columnName.equals("Speciaal Verlof")) {
                                 hour.setLeave(value.isEmpty() ? 0 : Double.parseDouble(value.replace(",", ".")));
                                 changed = true;
                                 if (DEBUG) {
                                     System.out.println(hour.getLeave());
                                 }
                             } else if (columnName.equals("Opg. compensatie")) {
                                 hour.setWithdrawnCompensation(value.isEmpty() ? 0 : Double.parseDouble(value.replace(",", ".")));
                                 changed = true;
                                 if (DEBUG) {
                                     System.out.println(hour.getWithdrawnCompensation());
                                 }
                             } else if (columnName.equals("Opmerking")) {
                                 hour.setComment(value);
                                 changed = true;
                                 if (DEBUG) {
                                     System.out.println(hour.getComment());
                                 }
                             }
                         }
                     }
                 }
                 if (hour.getEmployeeNumber() != -1) {
                     if (!RoosterProgramma.getQueryManager().updateWorkHours(hour)) {
                         failures.add(i);
                     }
                 } else if (changed) {
                     if (DEBUG) {
                         System.out.println("Changed");
                     }
                     hour.setEmployeeNumber(employee.getEmployeeNumber());
                     hour.setDate(date);
                     if (!RoosterProgramma.getQueryManager().insertWorkHours(hour)) {
                         failures.add(i);
                     }
                 }
             }
             if (failures.isEmpty()) {
                 Utils.showMessage("Succesvol opgeslagen.", "Opslaan gelukt!", null, false);
             } else {
                 String errors = "";
                 for (int failure : failures) {
                     errors += "\nRij " + failure;
                 }
                 Utils.showMessage("Opslaan van de volgende rijen is niet gelukt: " + errors, "Fout!", null, false);
             }
         }
     }//GEN-LAST:event_btnSaveActionPerformed
 
     private void btnPreviousYearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreviousYearActionPerformed
         handleTime(year - 1, month);
     }//GEN-LAST:event_btnPreviousYearActionPerformed
 
     private void btnNextYearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextYearActionPerformed
         handleTime(year + 1, month);
     }//GEN-LAST:event_btnNextYearActionPerformed
 
     private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
         RoosterProgramma.getInstance().showPanel(RoosterProgramma.getInstance().getPrevPanel());
     }//GEN-LAST:event_btnBackActionPerformed
 
     private void btnNextMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextMonthActionPerformed
         handleTime(year, month + 1);
     }//GEN-LAST:event_btnNextMonthActionPerformed
 
     private void btnPreviousMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreviousMonthActionPerformed
         handleTime(year, month - 1);
     }//GEN-LAST:event_btnPreviousMonthActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnBack;
     private javax.swing.JButton btnNextMonth;
     private javax.swing.JButton btnNextYear;
     private javax.swing.JButton btnPreviousMonth;
     private javax.swing.JButton btnPreviousYear;
     private javax.swing.JButton btnSave;
     private javax.swing.JComboBox<Integer> cmbMonth;
     private javax.swing.JComboBox<Integer> cmbYear;
     private javax.swing.JPanel jpBox;
     private javax.swing.JPanel jpFlowMonth;
     private javax.swing.JPanel jpFlowYear;
     private javax.swing.JPanel jpTimeNavigation;
     private javax.swing.JScrollPane jspEmployeeInformation;
     private javax.swing.JScrollPane jspTimesheet;
     private javax.swing.JLabel lblExpVacationHours;
     private javax.swing.JLabel lblVacationHours;
     private javax.swing.JPanel pnlDateSelect;
     private javax.swing.JTable tblEmployeeInformation;
     private javax.swing.JTable tblTimesheet;
     // End of variables declaration//GEN-END:variables
 }
