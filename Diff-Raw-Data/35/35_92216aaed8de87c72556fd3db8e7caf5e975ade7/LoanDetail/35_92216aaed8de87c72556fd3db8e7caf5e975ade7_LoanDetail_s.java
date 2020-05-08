 package view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Arrays;
 import java.util.GregorianCalendar;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.UIManager;
 import javax.swing.border.TitledBorder;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 
 import renderer.IconAndDescriptionRenderer;
 import tablemodel.TableModelLoanDetail;
 
 import comperator.CustomerComperator;
 
 import domain.Copy;
 import domain.Copy.Condition;
 import domain.Customer;
 import domain.Library;
 import domain.Loan;
 import exception.IllegalLoanOperationException;
 
 public class LoanDetail implements Observer {
 
     private JFrame frmLoanDetail;
     private JTextField txtCustomerIdentifier;
     private JTextField txtCopyInventoryNumber;
     private JTextField txtReturnDate;
     private JTable table;
     private TableModel tableModel;
     private Library library;
     private Customer customer;
     private JComboBox<Customer> customersComboBox;
     private JLabel lblAmountOfLoansByCustomer;
     private JLabel lblCheck;
     private JButton btnAddLoan;
     private JButton btnReturnLoan;
     private JLabel lblReturnAt;
     private JLabel lblStatus;
     private ImageIcon iconInventoryNumberOK = new ImageIcon("icons/ok.png","OK");
     private ImageIcon iconInventoryNumberWrong = new ImageIcon("icons/warning.png", "Falsche Inventarnummer");
     private TableRowSorter<TableModelLoanDetail> sorter;
     private JLabel lblActualLoansByCustomer;
     private JLabel lblAmountOfActualLoansByCustomer;
     private JLabel lblOverdueLoansByCustomer;
     private JLabel lblAmountOfOverdueLoansByCustomer;
 
     public LoanDetail(Library library) {
         this(library, null);
     }
 
     public LoanDetail(Library library, Loan loan) {
         this.library = library;
         library.addObserver(this);
         initialize();
         if (loan == null) {
             initializeForNewLoan();
         } else {
             initializeForExistingLoan(loan);
         }
         updateFields();
         frmLoanDetail.setVisible(true);
         
     }
 
     private void initializeForExistingLoan(Loan loan) {
         customer = loan.getCustomer();
         txtCustomerIdentifier.setText(String.valueOf(loan.getCustomer().getIdentifier()));
         txtCopyInventoryNumber.setText(String.valueOf(loan.getCopy().getInventoryNumber()));
         
         fillCustomersComboBox();
         
         customersComboBox.setSelectedItem(loan.getCustomer());
     }
 
     private void initializeForNewLoan() {
         customer = null;
         txtCustomerIdentifier.setText("");
         txtCopyInventoryNumber.setText("");
         
         fillCustomersComboBox();
         
         customersComboBox.setSelectedIndex(-1);
     }
 
     private void fillCustomersComboBox() {
         Customer[] carr = library.getCustomers().toArray(new Customer[library.getCustomers().size()]);
         Arrays.sort(carr, new CustomerComperator());
         customersComboBox.setModel(new DefaultComboBoxModel<Customer>(carr));
     }
 
     private void initialize() {
         final String[] headers = { "Status", "Exemplar-ID", "Titel", "Autor" };
 
         frmLoanDetail = new JFrame();
         frmLoanDetail.setTitle("Ausleihe");
         frmLoanDetail.setBounds(100, 100, 550, 380);
         GridBagLayout gridBagLayout = new GridBagLayout();
         gridBagLayout.columnWidths = new int[] { 0, 0 };
         gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
         gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
         gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
         frmLoanDetail.getContentPane().setLayout(gridBagLayout);
         frmLoanDetail.setMinimumSize(new Dimension(500, 360));
         frmLoanDetail.setMaximumSize(new Dimension(749, 659));
 
         addKeyboardListeners(frmLoanDetail);
 
         ImageIcon iconExemplarAusleihen = new ImageIcon("icons/book_go.png");
 
         ImageIcon iconExemplarZurueckgeben = new ImageIcon("icons/arrow-return.png");
 
         JPanel panel_customer = new JPanel();
         panel_customer.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Kunde", TitledBorder.LEADING, TitledBorder.TOP, null, null));
         GridBagConstraints gbc_panel_customer = new GridBagConstraints();
         gbc_panel_customer.insets = new Insets(0, 0, 5, 0);
         gbc_panel_customer.fill = GridBagConstraints.BOTH;
         gbc_panel_customer.gridx = 0;
         gbc_panel_customer.gridy = 0;
         frmLoanDetail.getContentPane().add(panel_customer, gbc_panel_customer);
         GridBagLayout gbl_panel_customer = new GridBagLayout();
         gbl_panel_customer.columnWidths = new int[] { 100, 0, 0 };
         gbl_panel_customer.rowHeights = new int[] { 0, 0, 0 };
         gbl_panel_customer.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
         gbl_panel_customer.rowWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
         panel_customer.setLayout(gbl_panel_customer);
 
         JLabel lblCustomerIdenfifier = new JLabel("Kennung:");
         GridBagConstraints gbc_lblError = new GridBagConstraints();
         gbc_lblError.anchor = GridBagConstraints.WEST;
         gbc_lblError.insets = new Insets(0, 0, 5, 5);
         gbc_lblError.gridx = 0;
         gbc_lblError.gridy = 0;
         panel_customer.add(lblCustomerIdenfifier, gbc_lblError);
 
         txtCustomerIdentifier = new JTextField();
         txtCustomerIdentifier.setEditable(false);
         GridBagConstraints gbc_textField = new GridBagConstraints();
         gbc_textField.insets = new Insets(0, 0, 5, 0);
         gbc_textField.fill = GridBagConstraints.HORIZONTAL;
         gbc_textField.gridx = 1;
         gbc_textField.gridy = 0;
         panel_customer.add(txtCustomerIdentifier, gbc_textField);
         txtCustomerIdentifier.setColumns(10);
 
         JLabel lblKunde = new JLabel("Kunde:");
         GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
         gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
         gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
         gbc_lblNewLabel_1.gridx = 0;
         gbc_lblNewLabel_1.gridy = 1;
         panel_customer.add(lblKunde, gbc_lblNewLabel_1);
 
         customersComboBox = new JComboBox<Customer>();
         customersComboBox.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (customersComboBox.getSelectedIndex() != -1) {
                     // Falls Customer ausgewählt
                     customer = (Customer) customersComboBox.getSelectedItem();
                     txtCustomerIdentifier.setText(String.valueOf(customer.getIdentifier()));
                     tableModel = new TableModelLoanDetail(library, customer, headers);
                     table.setModel(tableModel);
                     table.getColumnModel().getColumn(0).setCellRenderer(new IconAndDescriptionRenderer());
 
                     sorter = new TableRowSorter<TableModelLoanDetail>((TableModelLoanDetail) tableModel);
                     table.setRowSorter(sorter);
                     sorter.setSortsOnUpdates(true);
                     sorter.toggleSortOrder(2);
                     
                     updateFields();
                 } else {
                     // Falls kein Customer ausgewählt
                     txtCustomerIdentifier.setText("");
                     tableModel = new TableModelLoanDetail(library, null, headers);
                     table.setModel(tableModel);
                     lblAmountOfLoansByCustomer.setText("0");
                 }
             }
         });
 
         GridBagConstraints gbc_comboBox = new GridBagConstraints();
         gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
         gbc_comboBox.gridx = 1;
         gbc_comboBox.gridy = 1;
 
         panel_customer.add(customersComboBox, gbc_comboBox);
         
         JPanel panel_newLoan = new JPanel();
         panel_newLoan.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Exemplar", TitledBorder.LEADING, TitledBorder.TOP, null, null));
         GridBagConstraints gbc_panel_newLoan = new GridBagConstraints();
         gbc_panel_newLoan.insets = new Insets(0, 0, 5, 0);
         gbc_panel_newLoan.fill = GridBagConstraints.BOTH;
         gbc_panel_newLoan.gridx = 0;
         gbc_panel_newLoan.gridy = 1;
         frmLoanDetail.getContentPane().add(panel_newLoan, gbc_panel_newLoan);
         GridBagLayout gbl_panel_newLoan = new GridBagLayout();
         gbl_panel_newLoan.columnWidths = new int[] { 100, 0, 0, 0, 0, 0 };
         gbl_panel_newLoan.rowHeights = new int[] { 0, 0, 0, 0 };
         gbl_panel_newLoan.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
         gbl_panel_newLoan.rowWeights = new double[] { 1.0, 1.0, 0.0, Double.MIN_VALUE };
         panel_newLoan.setLayout(gbl_panel_newLoan);
         
         JLabel lblExemplarID = new JLabel("Exemplar-ID:");
         GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
         gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
         gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
         gbc_lblNewLabel_2.gridx = 0;
         gbc_lblNewLabel_2.gridy = 0;
         panel_newLoan.add(lblExemplarID, gbc_lblNewLabel_2);
 
         txtCopyInventoryNumber = new JTextField();
         txtCopyInventoryNumber.addKeyListener(new KeyAdapter() {
             @Override
             public void keyReleased(KeyEvent arg0) {
                setButtonsAndReturnDate();
             }
         });
         txtCopyInventoryNumber.setText("");
         GridBagConstraints gbc_textField_1 = new GridBagConstraints();
         gbc_textField_1.insets = new Insets(0, 0, 5, 5);
         gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
         gbc_textField_1.gridx = 1;
         gbc_textField_1.gridy = 0;
         panel_newLoan.add(txtCopyInventoryNumber, gbc_textField_1);
         txtCopyInventoryNumber.setColumns(10);
         
         lblCheck = new JLabel(iconInventoryNumberWrong);
         GridBagConstraints gbc_lblX = new GridBagConstraints();
         gbc_lblX.insets = new Insets(0, 0, 5, 5);
         gbc_lblX.gridx = 2;
         gbc_lblX.gridy = 0;
         panel_newLoan.add(lblCheck, gbc_lblX);
         btnAddLoan = new JButton("Exemplar ausleihen", iconExemplarAusleihen);
         btnAddLoan.setToolTipText("Leihe das Exemplar mit nebenstehender Exemplar-ID an den obigen Kunden aus");
         btnAddLoan.setEnabled(false);
         btnAddLoan.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
             	addLoan();
             }
         });
         
         GridBagConstraints gbc_btnSetLost = new GridBagConstraints();
         gbc_btnSetLost.anchor = GridBagConstraints.WEST;
         gbc_btnSetLost.insets = new Insets(0, 0, 5, 5);
         gbc_btnSetLost.gridx = 3;
         gbc_btnSetLost.gridy = 0;
         panel_newLoan.add(btnAddLoan, gbc_btnSetLost);
         btnReturnLoan = new JButton("Exemplar zur\u00FCckgeben", iconExemplarZurueckgeben);
         btnReturnLoan.setToolTipText("Gebe das Exemplar mit nebenstehender Exemplar-ID zurück");
         
         GridBagConstraints gbc_btnExemplarZurckgeben = new GridBagConstraints();
         gbc_btnExemplarZurckgeben.insets = new Insets(0, 0, 5, 0);
         gbc_btnExemplarZurckgeben.gridx = 4;
         gbc_btnExemplarZurckgeben.gridy = 0;
         panel_newLoan.add(btnReturnLoan, gbc_btnExemplarZurckgeben);
         btnReturnLoan.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 returnLoan();
             }
         });
 
         lblReturnAt = new JLabel("Zurück am:");
        GridBagConstraints gbc_lblZurckAm = new GridBagConstraints();
        gbc_lblZurckAm.anchor = GridBagConstraints.WEST;
        gbc_lblZurckAm.insets = new Insets(0, 0, 5, 5);
        gbc_lblZurckAm.gridx = 0;
        gbc_lblZurckAm.gridy = 1;
        panel_newLoan.add(lblReturnAt, gbc_lblZurckAm);
         
         txtReturnDate = new JTextField();
        txtReturnDate.setEnabled(false);
         GridBagConstraints gbc_txtAsdasd = new GridBagConstraints();
         gbc_txtAsdasd.gridwidth = 4;
         gbc_txtAsdasd.insets = new Insets(0, 0, 5, 5);
         gbc_txtAsdasd.fill = GridBagConstraints.HORIZONTAL;
         gbc_txtAsdasd.gridx = 1;
         gbc_txtAsdasd.gridy = 1;
         panel_newLoan.add(txtReturnDate, gbc_txtAsdasd);
         txtReturnDate.setColumns(10);
         
         lblStatus = new JLabel();
        GridBagConstraints gbc_lblError2 = new GridBagConstraints();
        gbc_lblError2.anchor = GridBagConstraints.EAST;
        gbc_lblError2.gridwidth = 5;
        gbc_lblError2.gridx = 0;
        gbc_lblError2.gridy = 2;
        panel_newLoan.add(lblStatus, gbc_lblError2);
 
         JPanel panel_loansByCustomer = new JPanel();
         panel_loansByCustomer.setBorder(new TitledBorder(null, "Ausleihen von Kunde", TitledBorder.LEADING, TitledBorder.TOP, null, null));
         GridBagConstraints gbc_panel_loansByCustomer = new GridBagConstraints();
         gbc_panel_loansByCustomer.fill = GridBagConstraints.BOTH;
         gbc_panel_loansByCustomer.gridx = 0;
         gbc_panel_loansByCustomer.gridy = 2;
         frmLoanDetail.getContentPane().add(panel_loansByCustomer, gbc_panel_loansByCustomer);
         GridBagLayout gbl_panel_loansByCustomer = new GridBagLayout();
         gbl_panel_loansByCustomer.columnWidths = new int[] { 0, 20, 0, 0, 0, 0, 0 };
         gbl_panel_loansByCustomer.rowHeights = new int[] { 0, 0, 0 };
         gbl_panel_loansByCustomer.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
         gbl_panel_loansByCustomer.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
         panel_loansByCustomer.setLayout(gbl_panel_loansByCustomer);
 
         lblActualLoansByCustomer = new JLabel("Aktuelle Ausleihen:");
         GridBagConstraints gbc_lblActualLoansByCustomer = new GridBagConstraints();
         gbc_lblActualLoansByCustomer.anchor = GridBagConstraints.WEST;
         gbc_lblActualLoansByCustomer.insets = new Insets(0, 0, 5, 5);
         gbc_lblActualLoansByCustomer.gridx = 0;
         gbc_lblActualLoansByCustomer.gridy = 0;
         panel_loansByCustomer.add(lblActualLoansByCustomer, gbc_lblActualLoansByCustomer);
 
         lblAmountOfActualLoansByCustomer = new JLabel();
         GridBagConstraints gbc_lblAmountOfActualLoansByCustomer = new GridBagConstraints();
         gbc_lblAmountOfActualLoansByCustomer.anchor = GridBagConstraints.WEST;
         gbc_lblAmountOfActualLoansByCustomer.insets = new Insets(0, 0, 5, 5);
         gbc_lblAmountOfActualLoansByCustomer.gridx = 1;
         gbc_lblAmountOfActualLoansByCustomer.gridy = 0;
         panel_loansByCustomer.add(lblAmountOfActualLoansByCustomer, gbc_lblAmountOfActualLoansByCustomer);
 
         JLabel lblLoansByCustomer = new JLabel("Anzahl Ausleihen total:");
         GridBagConstraints gbc_lblLoansByCustomer = new GridBagConstraints();
         gbc_lblLoansByCustomer.insets = new Insets(0, 0, 5, 5);
         gbc_lblLoansByCustomer.gridx = 2;
         gbc_lblLoansByCustomer.gridy = 0;
         panel_loansByCustomer.add(lblLoansByCustomer, gbc_lblLoansByCustomer);
         
         lblAmountOfLoansByCustomer = new JLabel();
         GridBagConstraints gbc_lblAmountOfLoansByCustomer = new GridBagConstraints();
         gbc_lblAmountOfLoansByCustomer.anchor = GridBagConstraints.WEST;
         gbc_lblAmountOfLoansByCustomer.insets = new Insets(0, 0, 5, 10);
         gbc_lblAmountOfLoansByCustomer.gridx = 3;
         gbc_lblAmountOfLoansByCustomer.gridy = 0;
         panel_loansByCustomer.add(lblAmountOfLoansByCustomer, gbc_lblAmountOfLoansByCustomer);
         
         lblOverdueLoansByCustomer = new JLabel("Anzahl überfällige Ausleihen:");
         lblOverdueLoansByCustomer.setForeground(Color.RED);
         GridBagConstraints gbc_lblOverdueLoansByCustomer = new GridBagConstraints();
         gbc_lblOverdueLoansByCustomer.insets = new Insets(0, 0, 5, 5);
         gbc_lblOverdueLoansByCustomer.gridx = 4;
         gbc_lblOverdueLoansByCustomer.gridy = 0;
         panel_loansByCustomer.add(lblOverdueLoansByCustomer, gbc_lblOverdueLoansByCustomer);
         
         lblAmountOfOverdueLoansByCustomer = new JLabel();
         lblAmountOfOverdueLoansByCustomer.setForeground(Color.RED);
         GridBagConstraints gbc_lblAmountOfOverdueLoansByCustomer = new GridBagConstraints();
         gbc_lblAmountOfOverdueLoansByCustomer.anchor = GridBagConstraints.WEST;
         gbc_lblAmountOfOverdueLoansByCustomer.insets = new Insets(0, 0, 5, 0);
         gbc_lblAmountOfOverdueLoansByCustomer.gridx = 5;
         gbc_lblAmountOfOverdueLoansByCustomer.gridy = 0;
         panel_loansByCustomer.add(lblAmountOfOverdueLoansByCustomer, gbc_lblAmountOfOverdueLoansByCustomer);
 
         JScrollPane scrollPane = new JScrollPane();
         GridBagConstraints gbc_scrollPane = new GridBagConstraints();
         gbc_scrollPane.gridwidth = 6;
         gbc_scrollPane.fill = GridBagConstraints.BOTH;
         gbc_scrollPane.gridx = 0;
         gbc_scrollPane.gridy = 1;
         panel_loansByCustomer.add(scrollPane, gbc_scrollPane);
 
         table = new JTable();
         table.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseReleased(MouseEvent arg0) {
                 long inventoryNumber = (Long) tableModel.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 1);
                 txtCopyInventoryNumber.setText(Long.toString(inventoryNumber));
                 setButtonsAndReturnDate();
             }
         });
         table.getTableHeader().setReorderingAllowed(false);
         table.setAutoCreateRowSorter(true);
         GridBagConstraints gbc_table = new GridBagConstraints();
         gbc_table.insets = new Insets(0, 0, 0, 5);
         gbc_table.fill = GridBagConstraints.BOTH;
         gbc_table.gridx = 0;
         gbc_table.gridy = 2;
 
         scrollPane.setViewportView(table);
     }
 
     @Override
     public void update(Observable arg0, Object arg1) {
         updateFields();
     }
 
     private void updateFields() {
         lblAmountOfActualLoansByCustomer.setText(String.valueOf(library.getActualLoansByCustomer(customer).size()));
         lblAmountOfLoansByCustomer.setText(String.valueOf(library.getCustomerLoans(customer).size()));
         if (library.checkHasOverdueLoans(customer)) {
             lblOverdueLoansByCustomer.setVisible(true);
             lblAmountOfOverdueLoansByCustomer.setVisible(true);
             lblAmountOfOverdueLoansByCustomer.setText(String.valueOf(library.getOverdueLoans(customer).size()));
         }
         else{
             lblOverdueLoansByCustomer.setVisible(false);
             lblAmountOfOverdueLoansByCustomer.setVisible(false);
         }
         setButtonsAndReturnDate();
         
     }
     
     private void setButtonsAndReturnDate(){
         if (library.checkInventoryNumberExists(txtCopyInventoryNumber.getText())) {
             lblCheck.setIcon(iconInventoryNumberOK);
             Copy c = library.getCopyByInventoryNumber(Long.parseLong(txtCopyInventoryNumber.getText()));
             if (library.isCopyLent(c)) {
                 
                 Loan l = library.getLoanOfCopy(c);
                 if (!l.isOverdue()) {
                     txtReturnDate.setText(l.getDueDateString() + " (Noch maximal " + l.getDaysTilDue() + " Tage bis zur Rückgabe)");  
                 } else {
                     if (l.getDaysOverdue() == 1) {
                         txtReturnDate.setText(l.getDueDateString() + " (Seit 1 Tag überfällig!)");                          
                     } else {
                        txtReturnDate.setText(l.getDueDateString() + " (Seit " + l.getDaysTilDue() + " Tagen überfällig!)");  
                     }
                }
                
                 btnAddLoan.setEnabled(false);
                 btnReturnLoan.setEnabled(true);
             } else {
                 GregorianCalendar returnDate = new GregorianCalendar();
                 returnDate.add(GregorianCalendar.DAY_OF_YEAR, Loan.DAYS_TO_RETURN_BOOK);
                 txtReturnDate.setText(Loan.getFormattedDate(returnDate) + " (Ab jetzt " + Loan.DAYS_TO_RETURN_BOOK + " Tage ausleihbar)");
                 
                 btnAddLoan.setEnabled(true);
                 btnReturnLoan.setEnabled(false);
             }
         } else {
             lblCheck.setIcon(iconInventoryNumberWrong);
             btnAddLoan.setEnabled(false);
             btnReturnLoan.setEnabled(false);
         }
     }
 
     private void returnLoan() {
         if (library.checkInventoryNumberExists(txtCopyInventoryNumber.getText())) {
             try {
                 Loan l = library.getLoanOfCopy(library.getCopyByInventoryNumber(Long.valueOf(txtCopyInventoryNumber.getText())));
                 if (l != null) {
                     if (l.isLent()) {
                         lblStatus.setForeground(Color.BLACK);
                         if (l.isOverdue()) {
                             l.returnCopy(new GregorianCalendar());
                             initializeForExistingLoan(l);
                             lblStatus.setForeground(Color.RED);
                             lblStatus.setText("Buch wurde zurückgegeben, Ausleihe war überfällig!");
                         } else {
                             l.returnCopy(new GregorianCalendar());
                             initializeForExistingLoan(l);
                             lblStatus.setText("Buch wurde zurückgegeben");
                         }
                         library.update(null, null);
                     }
                 } else {
                 	lblStatus.setText("Buch konnte nicht zurückgegeben werden");
                 }
             } catch (NumberFormatException e) {
                 e.printStackTrace();
             } catch (IllegalLoanOperationException e) {
                 e.printStackTrace();
             }
         }
     }
     
     private void addLoan(){
     	 lblStatus.setForeground(Color.RED);
          if (customersComboBox.getSelectedIndex() > -1) {
              if (library.checkInventoryNumberExists(txtCopyInventoryNumber.getText())) {
                  Copy copy = library.getCopyByInventoryNumber(Long.parseLong(txtCopyInventoryNumber.getText()));
                  if (library.isCopyLent(copy)) {
                      lblStatus.setText("Buch konnte nicht ausgeliehen werden, diese Kopie ist bereits ausgeliehen!");
                  } else if (!library.checkMaxLoanAmount(customer)) {
                      lblStatus.setText("Buch konnte nicht ausgeliehen werden, der Kunde hat bereits " + Library.MAX_AMOUNT_OF_LOANS + " Bücher ausgeliehen!");
                  } else if (library.checkHasOverdueLoans(customer)) {
                      lblStatus.setText("Buch konnte nicht ausgeliehen werden, der Kunde hat überfällige Ausleihen!");
                  } else if (copy.getCondition() == Condition.LOST) {
                      lblStatus.setText("Buch konnte nicht ausgeliehen werden, das Buch ist als verloren markiert!");
                  } else {
                      Loan l = library.createAndAddLoan(customer, copy);
                      if (l != null) {
                          lblStatus.setForeground(Color.BLACK);
                          lblStatus.setText("Buch wurde erfolgreich ausgeliehen");
                      } else {
                          lblStatus.setText("Buch konnte nicht ausgelehnt werden");
                      }
                  }
              }
          } else {
              lblStatus.setText("Buch konnte nicht ausgeliehen werden, es muss ein Kunde ausgewählt sein!");
          }
     }
 
     public void addKeyboardListeners(final JFrame frame) {
         ActionListener escListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 frame.dispose();
             }
         };
         ActionListener enterListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
             	if(btnAddLoan.isEnabled()){
             		addLoan();
             	} else if(btnReturnLoan.isEnabled()){
             		returnLoan();
             	}             
             }
         };
         frame.getRootPane().registerKeyboardAction(escListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
         frame.getRootPane().registerKeyboardAction(enterListener, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
     }
     
 }
