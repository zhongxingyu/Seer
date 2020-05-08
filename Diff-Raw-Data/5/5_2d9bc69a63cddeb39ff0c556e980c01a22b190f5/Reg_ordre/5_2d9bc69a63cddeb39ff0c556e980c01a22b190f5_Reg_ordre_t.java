 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package streamfish;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 import static javax.swing.JOptionPane.*;
 
 /**
  *
  * @author Kristian
  */
 public class Reg_ordre extends javax.swing.JPanel {
 
     private final int CUSTID;
     private GUI gui;
     private Menu[] menu;
     private CustomerAddress[] address;
     private Object[] addressPlus1;
     private Reg_ordre order = this;
     private DefaultComboBoxModel comboBox;
     private Customer customer;
     private Menu selMenu;
     private int antPers;
     private double priceReduction = 0;
     private int subDays;
 
     /**
      * Creates new form Reg_kunde
      */
     public Reg_ordre(int custid, final GUI gui) {
 
         this.gui = gui;
 
         this.CUSTID = custid;
         this.customer = gui.getCustomer(CUSTID);
         menu = gui.getMenus("", false);
         address = gui.getAddress(CUSTID);
         addressPlus1 = new Object[address.length + 1];
         priceReduction = customer.getPriceReduction();
 
         for (int i = 0; i < address.length; i++) {
             addressPlus1[i] = address[i];
         }
         addressPlus1[address.length] = new String("Add new address");
         initComponents();
         antPers = Integer.parseInt(jSpinner1.getValue().toString());
         jLabel10.setText(customer.getPriceReduction() + " %");
         jLabel7.setText(updatePrice() + ",-");
         jLabel1.setText("Kundenr: " + CUSTID);
         updateMenu();
         
          jComboBox7.addItemListener(new ItemListener(){
 
             @Override
             public void itemStateChanged(ItemEvent e) {
                 String durr = (String) jComboBox7.getSelectedItem();
                 int duration = Integer.parseInt(durr.substring(0, 2).trim());
                 jLabel10.setText(updateReduction(duration)+"%");
                 jLabel7.setText(updatePrice()+",-");
             }
         
          });
            
         jSpinner1.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 antPers = Integer.parseInt(jSpinner1.getValue().toString());
                 jLabel7.setText(updatePrice() + ",-");
             }
         });
         
           
 
         comboBox = (DefaultComboBoxModel) jComboBox1.getModel();
         for (Object addr : addressPlus1) {
             comboBox.addElement(addr);
         }
         jComboBox1.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 javax.swing.JComboBox box = (javax.swing.JComboBox) e.getSource();
                 if (box.getSelectedItem() != null && box.getSelectedItem().getClass().equals(String.class)) {
                     AddAddress address = new AddAddress(gui, CUSTID);
                     address.addWindowListener(new WindowListener() {
                         @Override
                         public void windowOpened(WindowEvent e) {
                         }
 
                         @Override
                         public void windowClosing(WindowEvent e) {
                         }
 
                         @Override
                         public void windowClosed(WindowEvent e) {
                             order.updateAddress();
                         }
 
                         @Override
                         public void windowIconified(WindowEvent e) {
                         }
 
                         @Override
                         public void windowDeiconified(WindowEvent e) {
                         }
 
                         @Override
                         public void windowActivated(WindowEvent e) {
                         }
 
                         @Override
                         public void windowDeactivated(WindowEvent e) {
                         }
                     });
                 }
             }
         });
     }
 
     private double updateReduction(int i) {
             switch (i) {
                 case 0:
                     priceReduction = customer.getPriceReduction();
                     break;
                 case 1:
                     priceReduction = 5 + customer.getPriceReduction();
                     break;
                 case 3:
                     priceReduction = 10 + customer.getPriceReduction();
                     break; 
                 case 6:
                     priceReduction = 20  + customer.getPriceReduction();
                     break;
                 case 12:
                     priceReduction =  30  + customer.getPriceReduction();
                     break;
         }
         return priceReduction;
     }
 
     private double updatePrice() {
         if(jTabbedPane1.getSelectedIndex()== 0){
             if (selMenu != null) {
             double price = selMenu.getPrice() * antPers -((selMenu.getPrice() * antPers)*customer.getPercentageReduction());
             return price;
             }
         }else{
             if (selMenu != null) {
             String durr = (String) jComboBox7.getSelectedItem();
             int totalSubDays = subDays*(Integer.parseInt(durr.substring(0, 2).trim())*4);
             double priceReductionDouble = priceReduction;
             priceReductionDouble /= 100;
             double price = selMenu.getPrice() * antPers * totalSubDays -((selMenu.getPrice() * antPers*totalSubDays)*priceReductionDouble);
             return price;      
         }  
     }
         return 0;
     }
 
     private void updateAddress() {
         address = gui.getAddress(CUSTID);
         addressPlus1 = new Object[address.length + 1];
         for (int i = 0; i < address.length; i++) {
             addressPlus1[i] = address[i];
         }
         addressPlus1[address.length] = new String("Add new address");
         comboBox.removeAllElements();
         comboBox = (DefaultComboBoxModel) jComboBox1.getModel();
 
         for (Object addr : addressPlus1) {
             comboBox.addElement(addr);
         }
     }
 
     private void updateMenu() {
         menu = gui.getMenus("", false);
         if (menu != null && menu.length > 0) {
			DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
			model.setRowCount(0);
             for (int i = 0; i < menu.length; i++) {
                
                 model.addRow(new Object[]{menu[i].getMenuName(), menu[i].getPrice(), menu[i].getDescription()});
             }
         }
         jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 			@Override
             public void valueChanged(ListSelectionEvent event) {
                 int viewRow = jTable1.getSelectedRow();
                 if (!event.getValueIsAdjusting()) {
                     try {
                         selMenu = menu[viewRow];
                         jLabel7.setText(updatePrice() + ",-");
                     } catch (Exception e) {
                     }
                 }
             }
         });
 
     }
     public void update(){
         updateMenu();
         if (jTabbedPane1.getSelectedIndex() == 0) {
             antPers = (int) jSpinner1.getModel().getValue();
             jLabel10.setText(updateReduction(0)+"%");
         }else{
             antPers = (int) jSpinner2.getModel().getValue();
             String durr = (String) jComboBox7.getSelectedItem();
             int choice= (Integer.parseInt(durr.substring(0, 2).trim()));
             jLabel10.setText(updateReduction(choice)+"%");
         }
         
         jLabel7.setText(updatePrice()+",-");
     }
 
     private String getDays() {
         String res = "";
         if (jToggleButton1.isSelected()) {
             res += '1';
         } else {
             res += '0';
         }
         if (jToggleButton2.isSelected()) {
             res += '1';
         } else {
             res += '0';
         }
         if (jToggleButton3.isSelected()) {
             res += '1';
         } else {
             res += '0';
         }
         if (jToggleButton4.isSelected()) {
             res += '1';
         } else {
             res += '0';
         }
         if (jToggleButton5.isSelected()) {
             res += '1';
         } else {
             res += '0';
         }
         if (jToggleButton6.isSelected()) {
             res += '1';
         } else {
             res += '0';
         }
         if (jToggleButton7.isSelected()) {
             res += '1';
         } else {
             res += '0';
         }
 
 
         return res;
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jButton1 = new javax.swing.JButton();
         jButton2 = new javax.swing.JButton();
         jLabel1 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         jComboBox1 = new javax.swing.JComboBox();
         jLabel7 = new javax.swing.JLabel();
         jLabel8 = new javax.swing.JLabel();
         jLabel9 = new javax.swing.JLabel();
         jLabel10 = new javax.swing.JLabel();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         jPanel1 = new javax.swing.JPanel();
         jLabel2 = new javax.swing.JLabel();
         jComboBox2 = new javax.swing.JComboBox();
         jComboBox3 = new javax.swing.JComboBox();
         jComboBox4 = new javax.swing.JComboBox();
         jComboBox5 = new javax.swing.JComboBox();
         jLabel6 = new javax.swing.JLabel();
         jComboBox6 = new javax.swing.JComboBox();
         jLabel3 = new javax.swing.JLabel();
         jSpinner1 = new javax.swing.JSpinner();
         jPanel2 = new javax.swing.JPanel();
         jToggleButton1 = new javax.swing.JToggleButton();
         jToggleButton2 = new javax.swing.JToggleButton();
         jToggleButton3 = new javax.swing.JToggleButton();
         jToggleButton4 = new javax.swing.JToggleButton();
         jToggleButton5 = new javax.swing.JToggleButton();
         jToggleButton6 = new javax.swing.JToggleButton();
         jToggleButton7 = new javax.swing.JToggleButton();
         jComboBox7 = new javax.swing.JComboBox();
         jLabel11 = new javax.swing.JLabel();
         jSpinner2 = new javax.swing.JSpinner();
         jComboBox8 = new javax.swing.JComboBox();
         jComboBox9 = new javax.swing.JComboBox();
         jLabel12 = new javax.swing.JLabel();
         jLabel13 = new javax.swing.JLabel();
         jScrollPane2 = new javax.swing.JScrollPane();
         jTable1 = new javax.swing.JTable();
 
         jButton1.setText("Cancel");
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
 
         jButton2.setText("Register");
         jButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton2ActionPerformed(evt);
             }
         });
 
         jLabel1.setText("Kundenr: " + CUSTID);
 
         jLabel4.setText("Select menu:");
 
         jLabel5.setText("Address");
 
         jComboBox1.setModel(new DefaultComboBoxModel());
         jComboBox1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jComboBox1ActionPerformed(evt);
             }
         });
 
         jLabel7.setText("TODO" + ",-");
 
         jLabel8.setText("Price:");
 
         jLabel9.setText("Price reduction");
 
         jLabel10.setText("TODO" + " %");
 
         jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jTabbedPane1StateChanged(evt);
             }
         });
 
         jLabel2.setText("Delivery date: (yyyy-mm-dd)");
         jLabel2.setToolTipText("");
 
         jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020" }));
 
         jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }));
 
         jComboBox4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" }));
 
         jComboBox5.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
         jComboBox5.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jComboBox5ActionPerformed(evt);
             }
         });
 
         jLabel6.setText("Time: (hh-mm)");
         jLabel6.setToolTipText("");
 
         jComboBox6.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "15", "30", "45" }));
 
         jLabel3.setText("# Persons");
 
         jSpinner1.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
         jSpinner1.setValue(1);
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, 0)
                         .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, 0)
                         .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, 0)
                         .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addComponent(jLabel2)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jLabel6)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 121, Short.MAX_VALUE)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel3))
                 .addContainerGap())
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel2)
                     .addComponent(jLabel6)
                     .addComponent(jLabel3))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(44, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab("Normal order", jPanel1);
 
         jToggleButton1.setText("Mon");
         jToggleButton1.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jToggleButton1StateChanged(evt);
             }
         });
 
         jToggleButton2.setText("Tue");
         jToggleButton2.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jToggleButton2StateChanged(evt);
             }
         });
 
         jToggleButton3.setText("Wed");
         jToggleButton3.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jToggleButton3StateChanged(evt);
             }
         });
 
         jToggleButton4.setText("Thu");
         jToggleButton4.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jToggleButton4StateChanged(evt);
             }
         });
 
         jToggleButton5.setText("Fri");
         jToggleButton5.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jToggleButton5StateChanged(evt);
             }
         });
 
         jToggleButton6.setText("Sat");
         jToggleButton6.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jToggleButton6StateChanged(evt);
             }
         });
 
         jToggleButton7.setText("Sun");
         jToggleButton7.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jToggleButton7StateChanged(evt);
             }
         });
 
         jComboBox7.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1 Month", "3 Months", "6 Months", "12 Months" }));
         jComboBox7.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jComboBox7ActionPerformed(evt);
             }
         });
 
         jLabel11.setText("Duration");
 
         jSpinner2.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
         jSpinner2.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jSpinner2StateChanged(evt);
             }
         });
 
         jComboBox8.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "15", "30", "45" }));
 
         jComboBox9.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
 
         jLabel12.setText("Time: (hh-mm)");
 
         jLabel13.setText("# Persons");
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                         .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel2Layout.createSequentialGroup()
                                 .addComponent(jComboBox9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jComboBox8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addComponent(jLabel12))
                         .addGap(69, 69, 69)
                         .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel11)
                             .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 130, Short.MAX_VALUE)
                         .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jSpinner2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.TRAILING)))
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(jToggleButton1)
                         .addGap(0, 0, 0)
                         .addComponent(jToggleButton2)
                         .addGap(0, 0, 0)
                         .addComponent(jToggleButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, 0)
                         .addComponent(jToggleButton4)
                         .addGap(0, 0, 0)
                         .addComponent(jToggleButton5)
                         .addGap(0, 0, 0)
                         .addComponent(jToggleButton6)
                         .addGap(0, 0, 0)
                         .addComponent(jToggleButton7)))
                 .addContainerGap())
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jToggleButton1)
                     .addComponent(jToggleButton2)
                     .addComponent(jToggleButton3)
                     .addComponent(jToggleButton4)
                     .addComponent(jToggleButton5)
                     .addComponent(jToggleButton6)
                     .addComponent(jToggleButton7))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel11)
                     .addComponent(jLabel12)
                     .addComponent(jLabel13))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jComboBox8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jComboBox9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(27, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab("Subscription", jPanel2);
 
         jTable1.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Name", "Price", "Description"
             }
         ) {
             boolean[] canEdit = new boolean [] {
                 false, false, false
             };
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jTable1.getTableHeader().setReorderingAllowed(false);
         jScrollPane2.setViewportView(jTable1);
         jTable1.getColumnModel().getColumn(0).setMinWidth(50);
         jTable1.getColumnModel().getColumn(0).setPreferredWidth(175);
         jTable1.getColumnModel().getColumn(0).setMaxWidth(250);
         jTable1.getColumnModel().getColumn(1).setMinWidth(20);
         jTable1.getColumnModel().getColumn(1).setPreferredWidth(50);
         jTable1.getColumnModel().getColumn(1).setMaxWidth(50);
         jTable1.getColumnModel().getColumn(2).setResizable(false);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addGap(0, 0, Short.MAX_VALUE)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel9)
                             .addComponent(jLabel8))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel7)
                             .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)))
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING))
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addComponent(jTabbedPane1)
                     .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jButton1)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(jButton2)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel1)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel4)
                 .addGap(4, 4, 4)
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jLabel5)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel9)
                     .addComponent(jLabel10))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel7)
                     .addComponent(jLabel8))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton1)
                     .addComponent(jButton2))
                 .addContainerGap())
         );
 
         jTabbedPane1.getAccessibleContext().setAccessibleName("Normal order");
     }// </editor-fold>//GEN-END:initComponents
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         // TODO add your handling code here:
          if(gui.getEmployee(gui.employee_id).getUsertype() == 1){
                 gui.byttVindu(this, new SalesMain(gui));
          }else{
                 gui.byttVindu(this, new MainMenu(gui));
          }
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
         if (selMenu != null) {
             if (jTabbedPane1.getSelectedIndex() == 0) {
                 antPers = Integer.parseInt(jSpinner1.getValue().toString());
                 
                 String year = (String)jComboBox2.getSelectedItem();
                 String month = (String)jComboBox3.getSelectedItem();
                 String day = (String) jComboBox4.getSelectedItem();
                 if(month.length() == 1){
                     month = "0"+month;
                 }
                 if(day.length() == 1){
                     day = "0"+day;
                 }
                 System.out.println("ny runde");
                 int priceOriginal = selMenu.getPrice();
                 String getContent = jLabel7.getText();
                 getContent = getContent.replaceAll(",-", "");
                 System.out.println(getContent);
                 double priceWithReductionDouble = Double.parseDouble(getContent);
                 int priceWithReduction = (int) priceWithReductionDouble;
                 System.out.println(priceWithReduction);
                 selMenu.setPrice(priceWithReduction);
                 String date = year + "-" + month + "-" + day;
                 String time = (String) jComboBox5.getSelectedItem() + ":" + (String) jComboBox6.getSelectedItem();
                 CustomerAddress orderAddress = (CustomerAddress) jComboBox1.getSelectedItem();
                 Order order = new Order(selMenu.getMenuId(), CUSTID, gui.employee_id, antPers, date, time, orderAddress);
                 selMenu.setPrice(priceOriginal);
                 if (gui.registerOrder(order)) {
                      if(gui.getEmployee(gui.employee_id).getUsertype() == 1){
                          gui.byttVindu(this, new SalesMain(gui));
                     }else{
                          gui.byttVindu(this, new MainMenu(gui));
                     }
                 } else {
                     showMessageDialog(null, "Cannot register orders past todays date.", "Order registration", ERROR_MESSAGE);
                 }
                 
             } else {
                 antPers = Integer.parseInt(jSpinner2.getValue().toString());
                 String time = (String) jComboBox9.getSelectedItem() + ":" + (String) jComboBox8.getSelectedItem();
                 CustomerAddress orderAddress = (CustomerAddress) jComboBox1.getSelectedItem();
                 Order order = new Order(selMenu.getMenuId(), CUSTID, gui.employee_id, antPers, time, orderAddress);
                 String durr = (String) jComboBox7.getSelectedItem();
                 String days = getDays();
                 int duration = Integer.parseInt(durr.substring(0, 2).trim());
                 gui.registrerSubscription(new Subscription(duration, TodaysDate.getDate(), TodaysDate.getADateAddMonth(duration), days, '1'), order);
                 if(gui.getEmployee(gui.employee_id).getUsertype() == 1){
                     gui.byttVindu(this, new SalesMain(gui));
                 }else{
                     gui.byttVindu(this, new MainMenu(gui));
                 }
                 
             }
         } else {
             showMessageDialog(null, "No menu is selected.");
         }
 
     }//GEN-LAST:event_jButton2ActionPerformed
 
     private void jComboBox7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox7ActionPerformed
         
     }//GEN-LAST:event_jComboBox7ActionPerformed
 
     private void jComboBox5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox5ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jComboBox5ActionPerformed
 
     private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jComboBox1ActionPerformed
 
     private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
         // TODO add your handling code here:
         update();
     }//GEN-LAST:event_jTabbedPane1StateChanged
 
     private void jToggleButton1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jToggleButton1StateChanged
         // TODO add your handling code here:
         if(jToggleButton1.getModel().isSelected()){
             subDays +=1;
         }else{
             subDays -=1;
         }
         update();
     }//GEN-LAST:event_jToggleButton1StateChanged
 
     private void jToggleButton2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jToggleButton2StateChanged
         // TODO add your handling code here:'if(jToggleButton7.getModel().isSelected()){
           if(jToggleButton2.getModel().isSelected()){
             subDays +=1;
         }else{
             subDays -=1;
         }
           update();
     }//GEN-LAST:event_jToggleButton2StateChanged
 
     private void jToggleButton3StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jToggleButton3StateChanged
         // TODO add your handling code here:
          if(jToggleButton3.getModel().isSelected()){
             subDays +=1;
         }else{
             subDays -=1;
         }
          update();
     }//GEN-LAST:event_jToggleButton3StateChanged
 
     private void jToggleButton4StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jToggleButton4StateChanged
         // TODO add your handling code here:
          if(jToggleButton4.getModel().isSelected()){
             subDays +=1;
         }else{
             subDays -=1;
         }
          update();
     }//GEN-LAST:event_jToggleButton4StateChanged
 
     private void jToggleButton5StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jToggleButton5StateChanged
         // TODO add your handling code here:
          if(jToggleButton5.getModel().isSelected()){
             subDays +=1;
         }else{
             subDays -=1;
         }
          update();
     }//GEN-LAST:event_jToggleButton5StateChanged
 
     private void jToggleButton6StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jToggleButton6StateChanged
         // TODO add your handling code here:
          if(jToggleButton6.getModel().isSelected()){
             subDays +=1;
         }else{
             subDays -=1;
         }
          update();
     }//GEN-LAST:event_jToggleButton6StateChanged
 
     private void jToggleButton7StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jToggleButton7StateChanged
         // TODO add your handling code here:
          if(jToggleButton7.getModel().isSelected()){
             subDays +=1;
         }else{
             subDays -=1;
         }
          update();
     }//GEN-LAST:event_jToggleButton7StateChanged
 
     private void jSpinner2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner2StateChanged
         // TODO add your handling code here:
         antPers = (int)jSpinner2.getModel().getValue();
         update();
     }//GEN-LAST:event_jSpinner2StateChanged
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     private javax.swing.JComboBox jComboBox1;
     private javax.swing.JComboBox jComboBox2;
     private javax.swing.JComboBox jComboBox3;
     private javax.swing.JComboBox jComboBox4;
     private javax.swing.JComboBox jComboBox5;
     private javax.swing.JComboBox jComboBox6;
     private javax.swing.JComboBox jComboBox7;
     private javax.swing.JComboBox jComboBox8;
     private javax.swing.JComboBox jComboBox9;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JSpinner jSpinner1;
     private javax.swing.JSpinner jSpinner2;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JTable jTable1;
     private javax.swing.JToggleButton jToggleButton1;
     private javax.swing.JToggleButton jToggleButton2;
     private javax.swing.JToggleButton jToggleButton3;
     private javax.swing.JToggleButton jToggleButton4;
     private javax.swing.JToggleButton jToggleButton5;
     private javax.swing.JToggleButton jToggleButton6;
     private javax.swing.JToggleButton jToggleButton7;
     // End of variables declaration//GEN-END:variables
 }
