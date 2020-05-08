 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package view.employee;
 
 import connectivity.*;
 import java.awt.Component;
 import java.awt.Frame;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.sql.Date;
 import java.util.List;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.table.DefaultTableModel;
 import main.*;
 
 /**
  *
  * @author Flo
  */
 public class Employee extends javax.swing.JFrame {
 
     private Luggage luggageModel = new Luggage();
     private Customer customerModel = new Customer();
     private List<Luggage> luggages;
     private List<Customer> customers;
     private DefaultTableModel modelLuggage1, modelCustomer1, modelLuggage2, modelCustomer2;
     public static int customerId;
     public static int luggageId;
     String customerToLink = null;
     String luggageToLink = null;
     public boolean isLinked = false;
     public static String customerFullName;
     private Component ErrorPopUp;
     private Component LuggagePopUp;
     
     public Employee() {
         initComponents();
         modelLuggage1 = (DefaultTableModel) this.luggageTable1.getModel();
         modelCustomer1 = (DefaultTableModel) this.customerTable1.getModel();
         modelLuggage2 = (DefaultTableModel) this.luggageTable2.getModel();
         modelCustomer2 = (DefaultTableModel) this.customerTable2.getModel();
         
         searchCustomerTable1(9999, "");
         searchLuggageTable1(9999, "", 1);
         searchCustomerTable2(9999, "");
         searchLuggageTable2(9999, "", 1);
     }
     
     public void searchLuggageTable1(int dbField, String searchArg, int showHandled) {
         modelLuggage1.setRowCount(0); //nodig voor 
         luggages = luggageModel.searchLuggageList(dbField, searchArg, showHandled);
         for(Luggage luggage : luggages) {
             modelLuggage1.addRow(new Object[] {new Integer(luggage.getLuggageId()),
                (luggage.getCustomerId() == 0) ? "Nog niet toegewezen" : luggage.getCustomerId(),
                 luggage.getDescription(),
                 luggage.getLocation(),
                 luggage.getDateLost(),
                 luggage.isIsLost(),
                 luggage.isIsHandled()});
 
             //System.out.println(user.getFirstName());
         }
     }
     
     private void searchLuggageTable2(int dbField, String searchArg, int showHandled) {
         modelLuggage2.setRowCount(0); //nodig voor 
         luggages = luggageModel.searchLuggageList(dbField, searchArg, showHandled);
         for(Luggage luggage : luggages) {
             modelLuggage2.addRow(new Object[] {new Integer(luggage.getLuggageId()),
                (luggage.getCustomerId() == 0) ? "Nog niet toegewezen" : luggage.getCustomerId(),
                 luggage.getDescription(),
                 luggage.getLocation(),
                 luggage.getDateLost(),
                 luggage.isIsLost(),
                 luggage.isIsHandled()});
 
             //System.out.println(user.getFirstName());
         }
     }
     
     public void searchCustomerTable1(int dbField, String searchArg) {
         modelCustomer1.setRowCount(0); //nodig voor 
         customers = customerModel.searchCustomerList(dbField, searchArg);
         for(Customer customer : customers) {
             modelCustomer1.addRow(new Object[] {new Integer(customer.getCustomerId()),
                 customer.getFirstName(),
                 customer.getLastName(),
                 customer.getAddress(),
                 customer.getPostalCode(),
                 customer.getCity(),
                 customer.getCountry(),
                 customer.getEmail(),
                 customer.getPhoneHome(),
                 customer.getPhoneMobile()});
 
             //System.out.println(user.getFirstName());
         }
     }
     
     private void searchCustomerTable2(int dbField, String searchArg) {
         modelCustomer2.setRowCount(0); //nodig voor 
         customers = customerModel.searchCustomerList(dbField, searchArg);
         for(Customer customer : customers) {
             modelCustomer2.addRow(new Object[] {new Integer(customer.getCustomerId()),
                 customer.getFirstName(),
                 customer.getLastName(),
                 customer.getAddress(),
                 customer.getPostalCode(),
                 customer.getCity(),
                 customer.getCountry(),
                 customer.getEmail(),
                 customer.getPhoneHome(),
                 customer.getPhoneMobile()});
         }
     }
     
     private void clearFields() {
         tfAddress1.setText("");
         tfAddress2.setText("");
         tfCity.setText("");
         tfEmail1.setText("");
         tfEmail2.setText("");
         tfFirstName.setText("");
         tfLastName.setText("");
         tfPhoneHome.setText("");
         tfPhoneMobile.setText("");
         tfPostalCode1.setText("");
         tfPostalCode2.setText("");
     }
     
     public int getShowHandled1() {
         if(showHandledLuggage1.isSelected()){
             return 0;
         } else {
             return 1;
         }
     }
     
     public int getShowHandled2() {
         if(showHandledLuggage2.isSelected()){
             return 0;
         } else {
             return 1;
         }
     }
             
     public void refreshLink()
     {
         searchCustomerTable1(cbSearchLuggage.getSelectedIndex(), customerSearchField.getText());
         searchLuggageTable1(cbSearchCustomer.getSelectedIndex(), luggageSearchField.getText(), getShowHandled1());
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         buttonGroup1 = new javax.swing.ButtonGroup();
         overviewPane = new javax.swing.JTabbedPane();
         customer = new javax.swing.JPanel();
         customerRegistrationPanel = new javax.swing.JPanel();
         jLabel7 = new javax.swing.JLabel();
         tfAddress2 = new javax.swing.JTextField();
         tfAddress1 = new javax.swing.JTextField();
         jLabel12 = new javax.swing.JLabel();
         tfFirstName = new javax.swing.JTextField();
         tfPostalCode2 = new javax.swing.JTextField();
         jLabel13 = new javax.swing.JLabel();
         jLabel14 = new javax.swing.JLabel();
         tfLastName = new javax.swing.JTextField();
         jLabel15 = new javax.swing.JLabel();
         tfEmail1 = new javax.swing.JTextField();
         jLabel16 = new javax.swing.JLabel();
         tfEmail2 = new javax.swing.JTextField();
         tfPhoneHome = new javax.swing.JTextField();
         jLabel17 = new javax.swing.JLabel();
         tfPhoneMobile = new javax.swing.JTextField();
         jLabel18 = new javax.swing.JLabel();
         createCustomer1 = new javax.swing.JButton();
         jButton4 = new javax.swing.JButton();
         jLabel19 = new javax.swing.JLabel();
         jLabel20 = new javax.swing.JLabel();
         tfCity = new javax.swing.JTextField();
         tfCountry = new javax.swing.JComboBox();
         warningLabel1 = new javax.swing.JLabel();
         tfPostalCode1 = new javax.swing.JTextField();
         customerTablePanel = new javax.swing.JPanel();
         customerSearchField1 = new javax.swing.JTextField();
         customerSearchButton2 = new javax.swing.JButton();
         jScrollPane3 = new javax.swing.JScrollPane();
         customerTable2 = new javax.swing.JTable();
         cbSearchCustomer1 = new javax.swing.JComboBox();
         refreshCustomerTable2 = new javax.swing.JButton();
         customerOptionsPanel = new javax.swing.JPanel();
         jButton1 = new javax.swing.JButton();
         luggage = new javax.swing.JPanel();
         luggageRegistrationPanel = new javax.swing.JLayeredPane();
         lblCustomerID4 = new javax.swing.JLabel();
         tfCustomerID1 = new javax.swing.JTextField();
         lblCustomerID5 = new javax.swing.JLabel();
         lblCustomerID6 = new javax.swing.JLabel();
         tfLocation1 = new javax.swing.JTextField();
         lblCustomerID7 = new javax.swing.JLabel();
         createLuggage1 = new javax.swing.JButton();
         jButton3 = new javax.swing.JButton();
         jScrollPane2 = new javax.swing.JScrollPane();
         tfDescription = new javax.swing.JTextArea();
         cbStatus = new javax.swing.JCheckBox();
         cbDone = new javax.swing.JCheckBox();
         luggageTablePanel = new javax.swing.JPanel();
         luggageSearchField1 = new javax.swing.JTextField();
         luggageSearchButton2 = new javax.swing.JButton();
         jScrollPane7 = new javax.swing.JScrollPane();
         luggageTable2 = new javax.swing.JTable();
         cbSearchLuggage1 = new javax.swing.JComboBox();
         refreshLuggageTable2 = new javax.swing.JButton();
         showHandledLuggage2 = new javax.swing.JCheckBox();
         luggageOptionsPanel = new javax.swing.JPanel();
         jButton2 = new javax.swing.JButton();
         jButton5 = new javax.swing.JButton();
         linkLuggage = new javax.swing.JPanel();
         linkTableSplitter = new javax.swing.JSplitPane();
         linkLuggageTablePanel = new javax.swing.JPanel();
         luggageSearchField = new javax.swing.JTextField();
         luggageSearchButton1 = new javax.swing.JButton();
         jScrollPane6 = new javax.swing.JScrollPane();
         luggageTable1 = new javax.swing.JTable();
         cbSearchLuggage = new javax.swing.JComboBox();
         showHandledLuggage1 = new javax.swing.JCheckBox();
         linkCustomerTablePanel = new javax.swing.JPanel();
         customerSearchField = new javax.swing.JTextField();
         customerSearchButton1 = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         customerTable1 = new javax.swing.JTable();
         cbSearchCustomer = new javax.swing.JComboBox();
         linkOptionsPanel = new javax.swing.JPanel();
         linkButton = new javax.swing.JButton();
         refreshTables1 = new javax.swing.JButton();
         cancelButton = new javax.swing.JButton();
         menuBar = new javax.swing.JMenuBar();
         userMenu = new javax.swing.JMenu();
         changePassword = new javax.swing.JMenuItem();
         logout = new javax.swing.JMenuItem();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Medewerker - " + Session.storedFirstName + " " + Session.storedLastName);
         setMinimumSize(new java.awt.Dimension(820, 460));
 
         overviewPane.setToolTipText("tooltip");
         overviewPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
 
         customerRegistrationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Klant Registreren"));
 
         jLabel7.setText("Adres");
 
         tfAddress2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 tfAddress2ActionPerformed(evt);
             }
         });
 
         jLabel12.setText("Postcode");
 
         tfFirstName.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 tfFirstNameActionPerformed(evt);
             }
         });
 
         jLabel13.setText("Voornaam");
 
         jLabel14.setText("Achternaam");
 
         jLabel15.setText("E-mail");
 
         jLabel16.setText("@");
 
         tfEmail2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 tfEmail2ActionPerformed(evt);
             }
         });
 
         jLabel17.setText("Huis telefoon");
 
         jLabel18.setText("Mobiel tel.");
 
         createCustomer1.setText("Voeg toe");
         createCustomer1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 createCustomer1ActionPerformed(evt);
             }
         });
 
         jButton4.setText("Annuleren");
         jButton4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton4ActionPerformed(evt);
             }
         });
 
         jLabel19.setText("Stad");
 
         jLabel20.setText("Land");
 
         tfCountry.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Nederland", "Turkije", "Australië" }));
 
         warningLabel1.setForeground(new java.awt.Color(255, 0, 0));
 
         javax.swing.GroupLayout customerRegistrationPanelLayout = new javax.swing.GroupLayout(customerRegistrationPanel);
         customerRegistrationPanel.setLayout(customerRegistrationPanelLayout);
         customerRegistrationPanelLayout.setHorizontalGroup(
             customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(warningLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, customerRegistrationPanelLayout.createSequentialGroup()
                         .addGap(0, 0, Short.MAX_VALUE)
                         .addComponent(jButton4)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(createCustomer1))
                     .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                         .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel15)
                             .addComponent(jLabel17)
                             .addComponent(jLabel18))
                         .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                                 .addGap(132, 132, 132)
                                 .addComponent(jLabel16)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(tfEmail2))
                             .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                                 .addGap(34, 34, 34)
                                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                     .addComponent(tfPhoneHome, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                                     .addComponent(tfPhoneMobile))
                                 .addGap(0, 0, Short.MAX_VALUE))))
                     .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                         .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel13)
                             .addComponent(jLabel14)
                             .addComponent(jLabel12)
                             .addComponent(jLabel7)
                             .addComponent(jLabel20)
                             .addComponent(jLabel19))
                         .addGap(39, 39, 39)
                         .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(tfLastName)
                             .addComponent(tfFirstName, javax.swing.GroupLayout.Alignment.TRAILING)
                             .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                                         .addComponent(tfAddress1, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(tfAddress2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                     .addComponent(tfCountry, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(tfCity, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                                         .addComponent(tfPostalCode1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(tfPostalCode2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                                     .addComponent(tfEmail1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addGap(0, 27, Short.MAX_VALUE)))))
                 .addContainerGap())
         );
         customerRegistrationPanelLayout.setVerticalGroup(
             customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(tfFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel13))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel14)
                     .addComponent(tfLastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel7)
                     .addComponent(tfAddress1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(tfAddress2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                         .addComponent(jLabel12)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jLabel19)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jLabel20))
                     .addGroup(customerRegistrationPanelLayout.createSequentialGroup()
                         .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(tfPostalCode2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(tfPostalCode1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(tfCity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(tfCountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(tfEmail2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel16)
                         .addComponent(jLabel15))
                     .addComponent(tfEmail1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel17)
                     .addComponent(tfPhoneHome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(tfPhoneMobile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel18))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(warningLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 17, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(customerRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(createCustomer1)
                     .addComponent(jButton4))
                 .addContainerGap())
         );
 
         customerTablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Klanten"));
 
         customerSearchField1.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 customerSearchField1KeyPressed(evt);
             }
         });
 
         customerSearchButton2.setText("Zoeken");
         customerSearchButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 customerSearchButton2ActionPerformed(evt);
             }
         });
 
         customerTable2.setAutoCreateRowSorter(true);
         customerTable2.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Klant ID", "Voornaam", "Achternaam", "Adres", "Postcode", "Stad", "Land", "Email", "Tel. thuis", "Tel. mobiel"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false, false, false, false, false, false
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jScrollPane3.setViewportView(customerTable2);
 
         cbSearchCustomer1.setMaximumRowCount(11);
         cbSearchCustomer1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Alle velden", "Klant ID", "Voornaam", "Achternaam", "Adres", "Postcode", "Stad", "Land", "Email", "Tel. huis", "Tel. mobiel" }));
 
         refreshCustomerTable2.setText("Overzicht verversen");
         refreshCustomerTable2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 refreshCustomerTable2ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout customerTablePanelLayout = new javax.swing.GroupLayout(customerTablePanel);
         customerTablePanel.setLayout(customerTablePanelLayout);
         customerTablePanelLayout.setHorizontalGroup(
             customerTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerTablePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(customerTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(customerTablePanelLayout.createSequentialGroup()
                         .addComponent(customerSearchField1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(customerSearchButton2)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(cbSearchCustomer1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 207, Short.MAX_VALUE)
                         .addComponent(refreshCustomerTable2))
                     .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE))
                 .addContainerGap())
         );
         customerTablePanelLayout.setVerticalGroup(
             customerTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerTablePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(customerTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(customerSearchField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(customerSearchButton2)
                     .addComponent(cbSearchCustomer1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(refreshCustomerTable2))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         customerOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Opties"));
 
         jButton1.setText("Klant gegevens aanpassen");
 
         javax.swing.GroupLayout customerOptionsPanelLayout = new javax.swing.GroupLayout(customerOptionsPanel);
         customerOptionsPanel.setLayout(customerOptionsPanelLayout);
         customerOptionsPanelLayout.setHorizontalGroup(
             customerOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerOptionsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jButton1)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         customerOptionsPanelLayout.setVerticalGroup(
             customerOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerOptionsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jButton1)
                 .addContainerGap(62, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout customerLayout = new javax.swing.GroupLayout(customer);
         customer.setLayout(customerLayout);
         customerLayout.setHorizontalGroup(
             customerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(customerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(customerRegistrationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(customerOptionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(customerTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         customerLayout.setVerticalGroup(
             customerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(customerLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(customerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(customerTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(customerLayout.createSequentialGroup()
                         .addComponent(customerRegistrationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(customerOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
 
         overviewPane.addTab("Klanten overzicht en registratie", customer);
 
         luggageRegistrationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Bagage Registeren"));
 
         lblCustomerID4.setText("Klant ID");
 
         lblCustomerID5.setText("Omschrijving");
 
         lblCustomerID6.setText("Locatie");
 
         tfLocation1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 tfLocation1ActionPerformed(evt);
             }
         });
 
         lblCustomerID7.setText("Status");
 
         createLuggage1.setText("Voeg toe");
         createLuggage1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 createLuggage1ActionPerformed(evt);
             }
         });
 
         jButton3.setText("Annuleren");
 
         tfDescription.setColumns(20);
         tfDescription.setRows(5);
         jScrollPane2.setViewportView(tfDescription);
 
         cbStatus.setText("Vermist");
 
         cbDone.setText("Afgehandeld");
 
         javax.swing.GroupLayout luggageRegistrationPanelLayout = new javax.swing.GroupLayout(luggageRegistrationPanel);
         luggageRegistrationPanel.setLayout(luggageRegistrationPanelLayout);
         luggageRegistrationPanelLayout.setHorizontalGroup(
             luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(luggageRegistrationPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, luggageRegistrationPanelLayout.createSequentialGroup()
                         .addGap(0, 0, Short.MAX_VALUE)
                         .addComponent(jButton3)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(createLuggage1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(luggageRegistrationPanelLayout.createSequentialGroup()
                         .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(lblCustomerID4)
                             .addComponent(lblCustomerID5)
                             .addComponent(lblCustomerID6, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblCustomerID7, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jScrollPane2)
                             .addGroup(luggageRegistrationPanelLayout.createSequentialGroup()
                                 .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(tfLocation1, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(tfCustomerID1, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(cbStatus)
                                     .addComponent(cbDone))
                                 .addGap(0, 69, Short.MAX_VALUE)))))
                 .addContainerGap())
         );
         luggageRegistrationPanelLayout.setVerticalGroup(
             luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(luggageRegistrationPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblCustomerID4)
                     .addComponent(tfCustomerID1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(luggageRegistrationPanelLayout.createSequentialGroup()
                         .addComponent(lblCustomerID5)
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblCustomerID6)
                     .addComponent(tfLocation1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(10, 10, 10)
                 .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblCustomerID7)
                     .addComponent(cbStatus))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(cbDone)
                 .addGap(28, 28, 28)
                 .addGroup(luggageRegistrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(createLuggage1)
                     .addComponent(jButton3))
                 .addContainerGap())
         );
         luggageRegistrationPanel.setLayer(lblCustomerID4, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(tfCustomerID1, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(lblCustomerID5, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(lblCustomerID6, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(tfLocation1, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(lblCustomerID7, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(createLuggage1, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(jButton3, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(jScrollPane2, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(cbStatus, javax.swing.JLayeredPane.DEFAULT_LAYER);
         luggageRegistrationPanel.setLayer(cbDone, javax.swing.JLayeredPane.DEFAULT_LAYER);
 
         luggageTablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Bagage"));
 
         luggageSearchField1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 luggageSearchField1ActionPerformed(evt);
             }
         });
         luggageSearchField1.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 luggageSearchField1KeyPressed(evt);
             }
         });
 
         luggageSearchButton2.setText("Zoeken");
         luggageSearchButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 luggageSearchButton2ActionPerformed(evt);
             }
         });
 
         luggageTable2.setAutoCreateRowSorter(true);
         luggageTable2.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Bagage ID", "Klant ID", "Omschrijving", "Locatie", "Datum Vermist", "Vermist", "Afgehandeld"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false, false, false
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         luggageTable2.setVerifyInputWhenFocusTarget(false);
         jScrollPane7.setViewportView(luggageTable2);
 
         cbSearchLuggage1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Alle velden", "Bagage ID", "Klant ID", "Omschrijving", "Locatie", "Datum Vermist" }));
         cbSearchLuggage1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cbSearchLuggage1ActionPerformed(evt);
             }
         });
 
         refreshLuggageTable2.setText("Overzicht verversen");
         refreshLuggageTable2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 refreshLuggageTable2ActionPerformed(evt);
             }
         });
 
         showHandledLuggage2.setText("Toon afgehandelde bagage");
         showHandledLuggage2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 showHandledLuggage2ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout luggageTablePanelLayout = new javax.swing.GroupLayout(luggageTablePanel);
         luggageTablePanel.setLayout(luggageTablePanelLayout);
         luggageTablePanelLayout.setHorizontalGroup(
             luggageTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(luggageTablePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(luggageTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(luggageTablePanelLayout.createSequentialGroup()
                         .addComponent(luggageSearchField1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(luggageSearchButton2)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(cbSearchLuggage1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(showHandledLuggage2)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(refreshLuggageTable2))
                     .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 684, Short.MAX_VALUE))
                 .addContainerGap())
         );
         luggageTablePanelLayout.setVerticalGroup(
             luggageTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(luggageTablePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(luggageTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addGroup(luggageTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(luggageSearchField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(cbSearchLuggage1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(refreshLuggageTable2)
                         .addComponent(showHandledLuggage2))
                     .addComponent(luggageSearchButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         luggageOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Opties"));
 
         jButton2.setText("Bagage gegevens aanpassen");
         jButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton2ActionPerformed(evt);
             }
         });
 
         jButton5.setText("Bewijs voor klant printen");
 
         javax.swing.GroupLayout luggageOptionsPanelLayout = new javax.swing.GroupLayout(luggageOptionsPanel);
         luggageOptionsPanel.setLayout(luggageOptionsPanelLayout);
         luggageOptionsPanelLayout.setHorizontalGroup(
             luggageOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(luggageOptionsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(luggageOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         luggageOptionsPanelLayout.setVerticalGroup(
             luggageOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(luggageOptionsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jButton2)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jButton5)
                 .addContainerGap(30, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout luggageLayout = new javax.swing.GroupLayout(luggage);
         luggage.setLayout(luggageLayout);
         luggageLayout.setHorizontalGroup(
             luggageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(luggageLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(luggageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(luggageRegistrationPanel)
                     .addComponent(luggageOptionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(luggageTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         luggageLayout.setVerticalGroup(
             luggageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(luggageLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(luggageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(luggageTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(luggageLayout.createSequentialGroup()
                         .addComponent(luggageRegistrationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(luggageOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
 
         overviewPane.addTab("Bagage overzicht en registratie", luggage);
 
         linkLuggage.setPreferredSize(new java.awt.Dimension(1024, 768));
 
         linkTableSplitter.setBorder(javax.swing.BorderFactory.createTitledBorder("Overzichten"));
         linkTableSplitter.setDividerLocation(600);
 
         linkLuggageTablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Bagage"));
 
         luggageSearchField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 luggageSearchFieldActionPerformed(evt);
             }
         });
         luggageSearchField.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 luggageSearchFieldKeyPressed(evt);
             }
         });
 
         luggageSearchButton1.setText("Zoeken");
         luggageSearchButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 luggageSearchButton1ActionPerformed(evt);
             }
         });
 
         luggageTable1.setAutoCreateRowSorter(true);
         luggageTable1.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Bagage ID", "Klant ID", "Omschrijving", "Locatie", "Datum Vermist", "Vermist", "Afgehandeld"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false, false, false
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         luggageTable1.setVerifyInputWhenFocusTarget(false);
         jScrollPane6.setViewportView(luggageTable1);
 
         cbSearchLuggage.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Alle velden", "Bagage ID", "Klant ID", "Omschrijving", "Locatie", "Datum Vermist" }));
 
         showHandledLuggage1.setText("Toon afgehandelde bagage");
         showHandledLuggage1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 showHandledLuggage1ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout linkLuggageTablePanelLayout = new javax.swing.GroupLayout(linkLuggageTablePanel);
         linkLuggageTablePanel.setLayout(linkLuggageTablePanelLayout);
         linkLuggageTablePanelLayout.setHorizontalGroup(
             linkLuggageTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(linkLuggageTablePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(linkLuggageTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(linkLuggageTablePanelLayout.createSequentialGroup()
                         .addComponent(luggageSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(luggageSearchButton1)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(cbSearchLuggage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(showHandledLuggage1)
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 561, Short.MAX_VALUE))
                 .addContainerGap())
         );
         linkLuggageTablePanelLayout.setVerticalGroup(
             linkLuggageTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(linkLuggageTablePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(linkLuggageTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(luggageSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(luggageSearchButton1)
                     .addComponent(cbSearchLuggage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(showHandledLuggage1))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         linkTableSplitter.setLeftComponent(linkLuggageTablePanel);
 
         linkCustomerTablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Klanten"));
 
         customerSearchField.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 customerSearchFieldKeyPressed(evt);
             }
         });
 
         customerSearchButton1.setText("Zoeken");
         customerSearchButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 customerSearchButton1ActionPerformed(evt);
             }
         });
 
         customerTable1.setAutoCreateRowSorter(true);
         customerTable1.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 "Klant ID", "Voornaam", "Achternaam", "Adres", "Postcode", "Stad", "Land", "Email", "Tel. thuis", "Tel. mobiel"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false, false, false, false, false, false
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         customerTable1.setDragEnabled(true);
         jScrollPane1.setViewportView(customerTable1);
 
         cbSearchCustomer.setMaximumRowCount(11);
         cbSearchCustomer.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Alle velden", "Klant ID", "Voornaam", "Achternaam", "Adres", "Postcode", "Stad", "Land", "Email", "Tel. huis", "Tel. mobiel" }));
 
         javax.swing.GroupLayout linkCustomerTablePanelLayout = new javax.swing.GroupLayout(linkCustomerTablePanel);
         linkCustomerTablePanel.setLayout(linkCustomerTablePanelLayout);
         linkCustomerTablePanelLayout.setHorizontalGroup(
             linkCustomerTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(linkCustomerTablePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(linkCustomerTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(linkCustomerTablePanelLayout.createSequentialGroup()
                         .addComponent(customerSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(customerSearchButton1)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(cbSearchCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE))
                 .addContainerGap())
         );
         linkCustomerTablePanelLayout.setVerticalGroup(
             linkCustomerTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(linkCustomerTablePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(linkCustomerTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(customerSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(customerSearchButton1)
                     .addComponent(cbSearchCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         linkTableSplitter.setRightComponent(linkCustomerTablePanel);
 
         linkOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Opties"));
 
         linkButton.setText("Koppelen");
         linkButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 linkButtonActionPerformed(evt);
             }
         });
 
         refreshTables1.setText("Overzichten verversen");
         refreshTables1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 refreshTables1ActionPerformed(evt);
             }
         });
 
         cancelButton.setText("Annuleren");
         cancelButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cancelButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout linkOptionsPanelLayout = new javax.swing.GroupLayout(linkOptionsPanel);
         linkOptionsPanel.setLayout(linkOptionsPanelLayout);
         linkOptionsPanelLayout.setHorizontalGroup(
             linkOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, linkOptionsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(refreshTables1)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(cancelButton)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(linkButton)
                 .addContainerGap())
         );
         linkOptionsPanelLayout.setVerticalGroup(
             linkOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(linkOptionsPanelLayout.createSequentialGroup()
                 .addGroup(linkOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(linkButton)
                     .addComponent(refreshTables1)
                     .addComponent(cancelButton))
                 .addGap(0, 11, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout linkLuggageLayout = new javax.swing.GroupLayout(linkLuggage);
         linkLuggage.setLayout(linkLuggageLayout);
         linkLuggageLayout.setHorizontalGroup(
             linkLuggageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(linkLuggageLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(linkLuggageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(linkTableSplitter)
                     .addComponent(linkOptionsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         linkLuggageLayout.setVerticalGroup(
             linkLuggageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(linkLuggageLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(linkTableSplitter)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(linkOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         overviewPane.addTab("Bagage en klanten koppelen", linkLuggage);
 
         userMenu.setText("Gebruiker");
 
         changePassword.setText("Wachtwoord wijzigen..");
         changePassword.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 changePasswordActionPerformed(evt);
             }
         });
         userMenu.add(changePassword);
 
         logout.setText("Uitloggen");
         logout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 logoutActionPerformed(evt);
             }
         });
         userMenu.add(logout);
 
         menuBar.add(userMenu);
 
         setJMenuBar(menuBar);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(overviewPane)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(overviewPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE)
         );
 
         overviewPane.getAccessibleContext().setAccessibleName("Tabbed Pane");
 
         pack();
         setLocationRelativeTo(null);
     }// </editor-fold>//GEN-END:initComponents
 
     private void changePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changePasswordActionPerformed
         Main.displayChangeMyPassword();
     }//GEN-LAST:event_changePasswordActionPerformed
 
     private void logoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutActionPerformed
         dispose();
         Main.displayLogin();
     }//GEN-LAST:event_logoutActionPerformed
 
     private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
         dispose();
     }//GEN-LAST:event_cancelButtonActionPerformed
 
     private void refreshTables1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshTables1ActionPerformed
         searchCustomerTable1(9999, "");
         searchLuggageTable1(9999, "", getShowHandled1());
         //        updateUserTable();
     }//GEN-LAST:event_refreshTables1ActionPerformed
 
     private void tfLocation1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfLocation1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_tfLocation1ActionPerformed
 
     private void tfAddress2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfAddress2ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_tfAddress2ActionPerformed
 
     private void tfFirstNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfFirstNameActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_tfFirstNameActionPerformed
 
     private void tfEmail2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfEmail2ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_tfEmail2ActionPerformed
 
     private void createCustomer1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createCustomer1ActionPerformed
         Customer customer = new Customer();
         boolean[] correctInput = new boolean[6];
         boolean totalCorrectInput = false;
         
         
         String newFirstName = tfFirstName.getText().trim();
         if (newFirstName.equals("") || newFirstName.length() > 50) {
             warningLabel1.setText("Voer een voornaam in");
             correctInput[0] = false;
         } else {
             correctInput[0] = true;
         }
         
         String newLastName = tfLastName.getText().trim();
         if (newLastName.equals("") || newLastName.length() > 50) {
             warningLabel1.setText("Voer een achternaam in");
             correctInput[1] = false;
         } else {
             correctInput[1] = true;
         }
         
         String newAddress = tfAddress1.getText().trim() + " " + tfAddress2.getText().trim();
         if (newAddress.equals(" ")) {
             warningLabel1.setText("Voer een adres in");
             correctInput[2] = false;
         }
         else if (tfAddress1.getText().equals("")) {
             warningLabel1.setText("Voer een straatnaam in");
             correctInput[2] = false;
         }
         else if (tfAddress2.getText().equals("")) {
             warningLabel1.setText("Voer een huisnummer in");
             correctInput[2] = false;
         } 
         else {
             correctInput[2] = true;
         }
             
         String newPostalCode = tfPostalCode1.getText().trim() + tfPostalCode2.getText().trim().toUpperCase();
         System.out.println(tfPostalCode1.getText() + " " + tfPostalCode2.getText() + " " + newPostalCode);
         if (newPostalCode.equals("")) {
             warningLabel1.setText("Voer een postcode in");
             correctInput[3] = false;
         }
         else if (!tfPostalCode1.getText().matches("[0-9]+")) {
             warningLabel1.setText("Eerste vier karakters van een postcode zijn cijfers");
             correctInput[3] = false;
         }
         else if (!tfPostalCode2.getText().toUpperCase().matches("[A-Z]+")) {
             warningLabel1.setText("Laatste twee karakters van een postcode zijn letters");
             correctInput[3] = false;
         }
         else {
             correctInput[3] = true;
         }
         
         String newCountry = tfCountry.getSelectedItem().toString();
         
         String newCity = tfCity.getText().trim();
         if (newCity.equals("")) {
             warningLabel1.setText("Voer een stad in");
             correctInput[4] = false;
         }
         else {
             correctInput[4] = true;
         }
         
         String newEmail = tfEmail1.getText().trim() + "@" + tfEmail2.getText().trim();
         if (newEmail.length() > 75) {
             warningLabel1.setText("Emailadres is te lang");
             correctInput[5] = false;
         }
         else if (!tfEmail2.getText().contains(".")) {
             warningLabel1.setText("Voer een emailadres met een punt in");
             correctInput[5] = false;
         }
         else {
             correctInput[5] = true;
         }
         
         String newPhoneHome = tfPhoneHome.getText().trim();
         String newPhoneMobile = tfPhoneMobile.getText().trim();
  
         for (int i = 0; i < correctInput.length; i++) {
             if (correctInput[i] == false) {
                 totalCorrectInput = false;
 //                System.out.println("false" + i); koekje
                 break;
             }
             else {
                 totalCorrectInput = true;
             }
         }
         
         if (totalCorrectInput) {
             customer.setNewCustomer(newFirstName, newLastName,
                  newAddress, newPostalCode, newCity, newCountry, newEmail,
                  newPhoneHome, newPhoneMobile);
 
             clearFields();
             searchCustomerTable2(9999, "");
             warningLabel1.setText("");
         }
     }//GEN-LAST:event_createCustomer1ActionPerformed
 
     private void refreshCustomerTable2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshCustomerTable2ActionPerformed
         searchCustomerTable2(9999, "");
     }//GEN-LAST:event_refreshCustomerTable2ActionPerformed
 
     private void refreshLuggageTable2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshLuggageTable2ActionPerformed
         searchLuggageTable2(9999, "", getShowHandled2());
     }//GEN-LAST:event_refreshLuggageTable2ActionPerformed
 
     private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
         clearFields();
     }//GEN-LAST:event_jButton4ActionPerformed
 
     private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jButton2ActionPerformed
 
     private void linkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkButtonActionPerformed
 
         isLinked = true;
         try
         {
             customerToLink = customerTable1.getValueAt(customerTable1.getSelectedRow(), 0).toString();
             luggageToLink = luggageTable1.getValueAt(luggageTable1.getSelectedRow(), 0).toString();
             String customerFirstName = customerTable1.getValueAt(customerTable1.getSelectedRow(), 1).toString() + " ";
             String customerLastName = customerTable1.getValueAt(customerTable1.getSelectedRow(), 2).toString();
             customerFullName = customerFirstName + customerLastName;
         }
         catch(IndexOutOfBoundsException e)
         {
             JOptionPane.showMessageDialog(ErrorPopUp,
             "Please make a selection in both tables and try again.");
             isLinked = false;
         }
 
 
         customerId = Integer.parseInt(customerToLink);
         luggageId = Integer.parseInt(luggageToLink);
    
         final JOptionPane linkLuggagePopUp = new JOptionPane(
         "Weet u zeker dat u klant: " + customerFullName + "\n"
         + "Wilt koppelen aan baggagestuk: " + luggageId ,
         JOptionPane.QUESTION_MESSAGE,
         JOptionPane.YES_NO_OPTION);
         if(isLinked == true) 
         {
         final JDialog dialog = new JDialog((Frame) LuggagePopUp, "Click a button", true);
         dialog.setContentPane(linkLuggagePopUp);
         linkLuggagePopUp.addPropertyChangeListener(
         new PropertyChangeListener()
         {
         @Override
         public void propertyChange(PropertyChangeEvent e) {
             String prop = e.getPropertyName();
 
             if (dialog.isVisible() 
              && (e.getSource() == linkLuggagePopUp)
              && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                 dialog.setVisible(false);
             }
         }
     });
     dialog.pack();
     dialog.setVisible(true);
 
     int value = ((Integer)linkLuggagePopUp.getValue()).intValue();
     if (value == JOptionPane.YES_OPTION) {
         Luggage luggage = new Luggage();
         luggage.linkCustomerId(customerId, luggageId);
         searchCustomerTable1(cbSearchLuggage.getSelectedIndex(), customerSearchField.getText());
         searchLuggageTable1(cbSearchCustomer.getSelectedIndex(), luggageSearchField.getText(), getShowHandled1());
     }
     }
     }//GEN-LAST:event_linkButtonActionPerformed
 
     private void customerSearchButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerSearchButton2ActionPerformed
         int searchField = cbSearchCustomer1.getSelectedIndex();
         searchCustomerTable2(searchField, customerSearchField1.getText().trim());
     }//GEN-LAST:event_customerSearchButton2ActionPerformed
 
     private void customerSearchButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerSearchButton1ActionPerformed
         int searchField = cbSearchCustomer.getSelectedIndex();
         searchCustomerTable1(searchField, customerSearchField.getText().trim());
     }//GEN-LAST:event_customerSearchButton1ActionPerformed
 
     private void luggageSearchButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_luggageSearchButton1ActionPerformed
         int searchField = cbSearchLuggage.getSelectedIndex();
         searchLuggageTable1(searchField, luggageSearchField.getText().trim(), getShowHandled1());
     }//GEN-LAST:event_luggageSearchButton1ActionPerformed
 
     private void luggageSearchButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_luggageSearchButton2ActionPerformed
         int searchField = cbSearchLuggage1.getSelectedIndex();
         searchLuggageTable2(searchField, luggageSearchField1.getText().trim(), getShowHandled2());
     }//GEN-LAST:event_luggageSearchButton2ActionPerformed
 
     private void createLuggage1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createLuggage1ActionPerformed
         Luggage luggage = new Luggage();
         boolean[] correctInput = new boolean[2];
         boolean totalCorrectInput = false;
         int isLost;
         int isDone;       
         String customerId;
         int storedUserId;
         
          String description = tfDescription.getText().trim();
         if (description.equals("")) {
             warningLabel1.setText("Voer een beschrijving in");
             correctInput[0] = false;
         }
         else 
             correctInput[0] = true;
         
        
        customerId = tfCustomerID1.getText().trim();
         
         String location = tfLocation1.getText().trim();
         if( location.equals("")){
             warningLabel1.setText("Voer een locatie in");
             correctInput[1] = false;
         }
         else 
             correctInput[1] = true;
                     
         if(cbStatus.isSelected())
             isLost = 1;
         else
             isLost = 0;
         
         if(cbDone.isSelected())
             isDone = 1;
         else
             isDone = 0;        
       
         for (int i = 0; i < correctInput.length; i++) {
             if (correctInput[i] == false) {
                 totalCorrectInput = false;
 
                 break;
             } else {
                 totalCorrectInput = true;
             }
         }
         System.out.println(totalCorrectInput);
         if (totalCorrectInput) {            
             storedUserId = Session.storedUserId;                   
             luggage.setNewLuggage(customerId, description, location, isLost, isDone, storedUserId);
         }        
         tfCustomerID1.setText("");
         tfDescription.setText("");
         tfLocation1.setText("");
         cbDone.setEnabled(false);
         cbStatus.setEnabled(false);
         searchLuggageTable2(9999, "", getShowHandled2());
         
         
         
        
       
      
       // TODO add your handling code here:
     }//GEN-LAST:event_createLuggage1ActionPerformed
 
     private void customerSearchField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_customerSearchField1KeyPressed
         if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             System.out.println("Enter pressed");
         int searchField = cbSearchCustomer1.getSelectedIndex();
         searchCustomerTable2(searchField, customerSearchField1.getText().trim());
         }
     }//GEN-LAST:event_customerSearchField1KeyPressed
 
     private void luggageSearchField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_luggageSearchField1KeyPressed
         if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             int searchField = cbSearchLuggage1.getSelectedIndex();
             searchLuggageTable2(searchField, luggageSearchField1.getText().trim(), getShowHandled2());
         }
     }//GEN-LAST:event_luggageSearchField1KeyPressed
 
     private void luggageSearchFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_luggageSearchFieldKeyPressed
     if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             int searchField = cbSearchLuggage.getSelectedIndex();
             searchLuggageTable1(searchField, luggageSearchField.getText().trim(), getShowHandled1());
     }
     }//GEN-LAST:event_luggageSearchFieldKeyPressed
 
     private void customerSearchFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_customerSearchFieldKeyPressed
     if(evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
         int searchField = cbSearchCustomer.getSelectedIndex();
         searchCustomerTable1(searchField, customerSearchField.getText().trim());
     }
     }//GEN-LAST:event_customerSearchFieldKeyPressed
 
     private void luggageSearchField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_luggageSearchField1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_luggageSearchField1ActionPerformed
 
     private void luggageSearchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_luggageSearchFieldActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_luggageSearchFieldActionPerformed
 
     private void cbSearchLuggage1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSearchLuggage1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_cbSearchLuggage1ActionPerformed
 
     private void showHandledLuggage2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showHandledLuggage2ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_showHandledLuggage2ActionPerformed
 
     private void showHandledLuggage1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showHandledLuggage1ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_showHandledLuggage1ActionPerformed
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(Employee.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(Employee.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(Employee.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(Employee.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new Employee().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup buttonGroup1;
     private javax.swing.JButton cancelButton;
     private javax.swing.JCheckBox cbDone;
     private javax.swing.JComboBox cbSearchCustomer;
     private javax.swing.JComboBox cbSearchCustomer1;
     private javax.swing.JComboBox cbSearchLuggage;
     private javax.swing.JComboBox cbSearchLuggage1;
     private javax.swing.JCheckBox cbStatus;
     private javax.swing.JMenuItem changePassword;
     private javax.swing.JButton createCustomer1;
     private javax.swing.JButton createLuggage1;
     private javax.swing.JPanel customer;
     private javax.swing.JPanel customerOptionsPanel;
     private javax.swing.JPanel customerRegistrationPanel;
     private javax.swing.JButton customerSearchButton1;
     private javax.swing.JButton customerSearchButton2;
     private javax.swing.JTextField customerSearchField;
     private javax.swing.JTextField customerSearchField1;
     private javax.swing.JTable customerTable1;
     private javax.swing.JTable customerTable2;
     private javax.swing.JPanel customerTablePanel;
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     private javax.swing.JButton jButton3;
     private javax.swing.JButton jButton4;
     private javax.swing.JButton jButton5;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel13;
     private javax.swing.JLabel jLabel14;
     private javax.swing.JLabel jLabel15;
     private javax.swing.JLabel jLabel16;
     private javax.swing.JLabel jLabel17;
     private javax.swing.JLabel jLabel18;
     private javax.swing.JLabel jLabel19;
     private javax.swing.JLabel jLabel20;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane6;
     private javax.swing.JScrollPane jScrollPane7;
     private javax.swing.JLabel lblCustomerID4;
     private javax.swing.JLabel lblCustomerID5;
     private javax.swing.JLabel lblCustomerID6;
     private javax.swing.JLabel lblCustomerID7;
     private javax.swing.JButton linkButton;
     private javax.swing.JPanel linkCustomerTablePanel;
     private javax.swing.JPanel linkLuggage;
     private javax.swing.JPanel linkLuggageTablePanel;
     private javax.swing.JPanel linkOptionsPanel;
     private javax.swing.JSplitPane linkTableSplitter;
     private javax.swing.JMenuItem logout;
     private javax.swing.JPanel luggage;
     private javax.swing.JPanel luggageOptionsPanel;
     private javax.swing.JLayeredPane luggageRegistrationPanel;
     private javax.swing.JButton luggageSearchButton1;
     private javax.swing.JButton luggageSearchButton2;
     private javax.swing.JTextField luggageSearchField;
     private javax.swing.JTextField luggageSearchField1;
     private javax.swing.JTable luggageTable1;
     private javax.swing.JTable luggageTable2;
     private javax.swing.JPanel luggageTablePanel;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JTabbedPane overviewPane;
     private javax.swing.JButton refreshCustomerTable2;
     private javax.swing.JButton refreshLuggageTable2;
     private javax.swing.JButton refreshTables1;
     private javax.swing.JCheckBox showHandledLuggage1;
     private javax.swing.JCheckBox showHandledLuggage2;
     private javax.swing.JTextField tfAddress1;
     private javax.swing.JTextField tfAddress2;
     private javax.swing.JTextField tfCity;
     private javax.swing.JComboBox tfCountry;
     private javax.swing.JTextField tfCustomerID1;
     private javax.swing.JTextArea tfDescription;
     private javax.swing.JTextField tfEmail1;
     private javax.swing.JTextField tfEmail2;
     private javax.swing.JTextField tfFirstName;
     private javax.swing.JTextField tfLastName;
     private javax.swing.JTextField tfLocation1;
     private javax.swing.JTextField tfPhoneHome;
     private javax.swing.JTextField tfPhoneMobile;
     private javax.swing.JTextField tfPostalCode1;
     private javax.swing.JTextField tfPostalCode2;
     private javax.swing.JMenu userMenu;
     private javax.swing.JLabel warningLabel1;
     // End of variables declaration//GEN-END:variables
 }
