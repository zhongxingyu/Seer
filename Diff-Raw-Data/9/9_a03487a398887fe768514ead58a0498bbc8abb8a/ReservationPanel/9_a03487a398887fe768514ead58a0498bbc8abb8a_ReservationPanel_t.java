 package carrental;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import javax.swing.*;
 import java.util.Locale;
 import javax.swing.border.TitledBorder;
 import javax.swing.table.DefaultTableModel;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * This is the main panel for reservations
  * It contains JPanels for every relevant screen, when dealing with reservations.
  * @author CNN
  * @version 2011-12-12
  */
 public class ReservationPanel extends SuperPanel {
 
     //TODO CustomerName and VehicleDescription in ViewEntity, check if vehicle already lent on date, Calendar
     //change err to append
     
     /** time occupied
      * 
      * for(reservations){
      * check times against vehicleid
      * } if(!succes){
      * try another vehicle of same type
      * }
      */
     
     private Reservation reservationToView; //specific customer, used to view details
     private ArrayList<Reservation> reservations; //array of reservations
     private final CreatePanel createPanel = new CreatePanel();
     private final ViewEntityPanel viewEntityPanel = new ViewEntityPanel();
     private final ListPanel listPanel = new ListPanel();
     private final GetCustomerPanel getCustomerPanel = new GetCustomerPanel();
     private final GetVehiclePanel getVehiclePanel = new GetVehiclePanel();
     private final JPanel emptyPanel = null;
     private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
 
     public ReservationPanel() {
         this.reservations = CarRental.getInstance().requestReservations();
         if (!reservations.isEmpty()) {
             reservationToView = reservations.get(0);
         } else {
             reservationToView = CarRental.getInstance().requestReservation();
         }
 
         //Sets the different subpanels. Also adds them to this object with JPanel.add().
         AssignAndAddSubPanels(new MainScreenPanel(), createPanel, viewEntityPanel, emptyPanel, emptyPanel, listPanel);
         this.setPreferredSize(new Dimension(800, 600));
 
         //Removes the default gaps between components
         setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
 
         //For testing: Choose your panel
         //showCreatePanel();
         //showMainScreenPanel();
         //showViewEntityPanel();
         listPanel.updateListPanel();
         showListPanel();
     }
 
     //Temporary Main
     //TODO: Remove main:
     public static void main(String[] args) {
         JFrame frame = new JFrame("ReservationPanel");
         frame.setPreferredSize(new Dimension(800, 600));
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         Container contentPane = frame.getContentPane();
         contentPane.add(new CustomerPanel());
         frame.pack();
         frame.setVisible(true);
     }
 
     public void setReservationToView(Reservation reservation) {
         reservationToView = reservation;
     }
 
     public void setBookingsList(ArrayList<Reservation> reservations) {
         this.reservations = reservations;
     }
 
     /**
      * Updates entire panel
      */
     public void updateReservationPanel() {
         reservations = CarRental.getInstance().requestReservations();
         if (reservations.get(0) != null) {
             reservationToView = reservations.get(0);
         } else {
             reservationToView = CarRental.getInstance().requestReservation();
         }
 
         createPanel.updateCreatePanel();
         viewEntityPanel.updateViewEntityPanel();
         listPanel.updateListPanel();
     }
 
     public class MainScreenPanel extends JPanel {
 
         public MainScreenPanel() {
             //Fields
             TitledBorder titleBorder;
 
             setLayout(new BorderLayout());
             titleBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Reservations");
             setBorder(titleBorder);
             add(new ViewEntityPanel());
             // add(new ListPanel());
         }
     }
 
     public class GetVehiclePanel extends JPanel {
 
         private DefaultTableModel vehicleTableModel;
         private JComboBox vehicleTypeCombo;
         private DefaultComboBoxModel vehicleTypeComboModel;
         private JTextField descriptionField, licensePlateField, vinField, drivenField;
         private int currentVehicleTypeIndex = -1; //this is for storing the currently selected choice from the combobox.
         private ArrayList<Vehicle> vehicleList;
         private ArrayList<VehicleType> vehicleTypes;
 
         public GetVehiclePanel() {
             JPanel centerPanel, vehicleListPanel, filterPanel, topFilterPanel, middleFilterPanel, bottomFilterPanel, buttonPanel;
             JLabel vehicleTypeLabel, descriptionLabel, licensePlateLabel, vinLabel, drivenLabel;
             JButton cancelButton, setButton;
             final JTable vehicleTable;
             JScrollPane listScrollPane;
             final int defaultJTextFieldColumns = 20, strutDistance = 0;
             vehicleList = CarRental.getInstance().requestVehicles();
             vehicleTypes = CarRental.getInstance().requestVehicleTypes();
 
             //Panel settings
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "List of vehicles"));
 
             //CenterPanel
             centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
             centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
             add(centerPanel, BorderLayout.CENTER);
 
             //VehicleListPanel.
             vehicleListPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             //Colors
             setBackground(new Color(216, 216, 208));
 
             //Creating the table model
             vehicleTableModel = new DefaultTableModel(new Object[]{"Type", "Description", "LicensePlate", "VIN", "Distance driven"}, 0);
 
             //Creating the JTable
             vehicleTable = new JTable(vehicleTableModel);
 
             listScrollPane = new JScrollPane(vehicleTable);
             //Setting the default size for the table in this scrollpane
             vehicleTable.setPreferredScrollableViewportSize(new Dimension(700, 200));
             vehicleListPanel.add(listScrollPane);
             centerPanel.add(vehicleListPanel);
 
             //FilterPanel
             filterPanel = new JPanel();
             filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.PAGE_AXIS));
             filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 2), "Filters"));
             centerPanel.add(filterPanel);
 
             //top row of filters
             topFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             filterPanel.add(topFilterPanel);
 
             //Vehicle Type
             vehicleTypeLabel = new JLabel("Vehicle Type");
             vehicleTypeComboModel = new DefaultComboBoxModel();
             vehicleTypeCombo = new JComboBox(vehicleTypeComboModel);
             vehicleTypeCombo.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (currentVehicleTypeIndex == -1 && vehicleTypeCombo.getSelectedIndex() > 0) { //if the current selection hasn't been set and it was not just set to "All"
                         filter();
                         currentVehicleTypeIndex = vehicleTypeCombo.getSelectedIndex();
                     } else if (currentVehicleTypeIndex > -1 && currentVehicleTypeIndex != vehicleTypeCombo.getSelectedIndex()) {
                         filter();
                         currentVehicleTypeIndex = vehicleTypeCombo.getSelectedIndex();
                     }
                 }
             });
             topFilterPanel.add(vehicleTypeLabel);
             topFilterPanel.add(Box.createRigidArea(new Dimension(16 + strutDistance, 0)));
             topFilterPanel.add(vehicleTypeCombo);
             topFilterPanel.add(Box.createRigidArea(new Dimension(91, 0)));
 
             //Description
             descriptionLabel = new JLabel("Description");
             descriptionField = new JTextField(defaultJTextFieldColumns);
             descriptionField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
             topFilterPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             topFilterPanel.add(descriptionLabel);
             topFilterPanel.add(Box.createRigidArea(new Dimension(45 + strutDistance, 0)));
             topFilterPanel.add(descriptionField);
 
             //Middle Filter panel
             middleFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             filterPanel.add(middleFilterPanel);
 
             //LicensePlate
             licensePlateLabel = new JLabel("License Plate");
             licensePlateField = new JTextField(defaultJTextFieldColumns);
             licensePlateField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
             middleFilterPanel.add(licensePlateLabel);
             middleFilterPanel.add(Box.createRigidArea(new Dimension(11 + strutDistance, 0)));
             middleFilterPanel.add(licensePlateField);
 
 
             //VIN
             vinLabel = new JLabel("VIN");
             vinField = new JTextField(defaultJTextFieldColumns);
             vinField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             middleFilterPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             middleFilterPanel.add(vinLabel);
             middleFilterPanel.add(Box.createRigidArea(new Dimension(90 + strutDistance, 0)));
             middleFilterPanel.add(vinField);
 
             //Bottom Filter panel
             bottomFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             filterPanel.add(bottomFilterPanel);
 
             //Driven
             drivenLabel = new JLabel("Distance driven");
             drivenField = new JTextField(defaultJTextFieldColumns);
             drivenField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
             bottomFilterPanel.add(drivenLabel);
             bottomFilterPanel.add(Box.createRigidArea(new Dimension(strutDistance, 0)));
             bottomFilterPanel.add(drivenField);
 
             //ButtonPanels
             buttonPanel = new JPanel();
             add(buttonPanel, BorderLayout.SOUTH);
             buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
             buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
             buttonPanel.add(Box.createHorizontalGlue());
 
             //cancel-Button
             cancelButton = new JButton("Back");
             cancelButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     update();
                     getVehiclePanel.setVisible(false);
                     createPanel.centerPanel.setVisible(true);
                 }
             });
             buttonPanel.add(cancelButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //Set-button
             setButton = new JButton("Set Vehicle");
             setButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (vehicleTable.getSelectedRow() >= 0) {
                         createPanel.setVehicle(Integer.toString(vehicleList.get(vehicleTable.getSelectedRow()).getID()));
 
                         //Sets all text fields blank
                         descriptionField.setText(null);
                         licensePlateField.setText(null);
                         vinField.setText(null);
                         drivenField.setText(null);
 
                         getVehiclePanel.setVisible(false);
                         createPanel.centerPanel.setVisible(true);
                     }
                 }
             });
             buttonPanel.add(setButton);
         }
 
         /**
          * Updates the panel to show an updated list of vehicles.
          */
         public void update() {
             vehicleList = CarRental.getInstance().requestVehicles();
 
             //reset the selected vehicle type
             currentVehicleTypeIndex = -1;
 
             //Delete exisiting rows
             vehicleTableModel.setRowCount(0);
 
             //Add the updated rows with vehicles
             for (Vehicle vehicle : vehicleList) {
                 vehicleTableModel.addRow(new String[]{
                             CarRental.getInstance().requestVehicleType(vehicle.getVehicleType()).getName(),
                             vehicle.getDescription(),
                             vehicle.getLicensePlate(),
                             vehicle.getVin(),
                             Integer.toString(vehicle.getOdo())});
             }
             assert (vehicleList.size() == vehicleTableModel.getRowCount()) : "size: " + vehicleList.size() + " rows: " + vehicleTableModel.getRowCount();
 
             //Update the JComboBox
             vehicleTypeComboModel.removeAllElements();
             vehicleTypeComboModel.addElement("All");
             for (VehicleType vehicleType : vehicleTypes) {
                 vehicleTypeComboModel.addElement(vehicleType.getName());
             }
 
             //Sets all text fields blank
             descriptionField.setText(null);
             licensePlateField.setText(null);
             vinField.setText(null);
             drivenField.setText(null);
         }
 
         /**
          * Rearranges the list of vehicles so that only entries matching the filters will be shown.
          */
         public void filter() {
 
             //Delete exisiting rows
             vehicleTableModel.setRowCount(0);
 
             //Add the rows that match the filter
             for (Vehicle vehicle : vehicleList) {
 
                 //As long as -
                 if (((vehicleTypeCombo.getSelectedIndex() == -1 || vehicleTypeCombo.getSelectedIndex() == 0) || //vehicle type is not chosen or set to "All" OR
                         vehicle.getVehicleType() == vehicleTypes.get(vehicleTypeCombo.getSelectedIndex() - 1).getID()) && //Vehicle's type is the vehicle type chosen AND
                         (descriptionField.getText().trim().isEmpty() || //description field is empty OR
                         vehicle.getDescription().toLowerCase(Locale.ENGLISH).contains(descriptionField.getText().trim().toLowerCase(Locale.ENGLISH))) && //vehicles descripton equals the description given AND
                         (licensePlateField.getText().trim().isEmpty() || //License plate field is empty OR
                         vehicle.getLicensePlate().toLowerCase(Locale.ENGLISH).contains(licensePlateField.getText().trim().toLowerCase(Locale.ENGLISH))) && //vehicles license plate number equals the license plate number given AND
                         (vinField.getText().trim().isEmpty() || //VIN field is empty OR
                         vehicle.getVin().toLowerCase(Locale.ENGLISH).contains(vinField.getText().trim().toLowerCase(Locale.ENGLISH))) && //vehicles VIN equals the VIN given AND
                         (drivenField.getText().trim().isEmpty() || //driven field is empty OR
                         Integer.toString(vehicle.getOdo()).toLowerCase(Locale.ENGLISH).contains(drivenField.getText().trim().toLowerCase(Locale.ENGLISH)))) { //vehicles ODO equals the "distance driven" given
 
                     // - does the vehicle match the filter, and following row is added to the table
                     vehicleTableModel.addRow(new String[]{
                                 CarRental.getInstance().requestVehicleType(vehicle.getVehicleType()).getName(),
                                 vehicle.getDescription(),
                                 vehicle.getLicensePlate(),
                                 vehicle.getVin(),
                                 Integer.toString(vehicle.getOdo())});
                 }
             }
         }
     }
 
     public class GetCustomerPanel extends JPanel {
 
         private JTextField filterAdressTextField, filterPhoneTextField, filterNameTextField, filterIDTextField;
         private JTable customerTable;
         private DefaultTableModel customerTableModel;
         private ArrayList<Customer> customers;
 
         public GetCustomerPanel() {
             customers = CarRental.getInstance().requestCustomers();
 
             //Fields
             JPanel centerPanel, customerListPanel, filterPanel, topFilterPanel, bottomFilterPanel, buttonPanel;
             JScrollPane scrollPane;
             JButton cancelButton, setButton;
             final int defaultJTextFieldColumns = 20, strutDistance = 0;
 
             //listPanel
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "List of customers"));
 
             //centerPanel
             centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
             centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
             add(centerPanel, BorderLayout.CENTER);
 
             //customerListPanel.
             customerListPanel = new JPanel();
 
             //Colors
             setBackground(new Color(216, 216, 208));
 
             //Creating the table model
             customerTableModel = new DefaultTableModel(new Object[]{"ID", "Phone", "Name", "Adress", "E-Mail"}, 0);
             setCustomerTable();
 
             //Creating the table
             customerTable = new JTable(customerTableModel);
             //adding it to it's own scrollpane
             scrollPane = new JScrollPane(customerTable);
             //Setting the default size for the scrollpane
             customerTable.setPreferredScrollableViewportSize(new Dimension(680, 200));
             customerListPanel.add(scrollPane);
             centerPanel.add(customerListPanel);
 
             //FilterPanel
             JLabel filterAdressLabel, filterPhoneLabel, filterNameLabel, filterIDLabel;
 
             filterPanel = new JPanel();
             filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.PAGE_AXIS));
             filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 2), "Filters"));
             centerPanel.add(filterPanel);
 
             //top row of filters
             topFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             filterPanel.add(topFilterPanel);
 
             //ID
             filterIDLabel = new JLabel("ID");
             filterIDTextField = new JTextField(defaultJTextFieldColumns);
             filterIDTextField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             topFilterPanel.add(Box.createRigidArea(new Dimension(0, 0)));
             topFilterPanel.add(filterIDLabel);
             topFilterPanel.add(Box.createRigidArea(new Dimension(68 + strutDistance, 0)));
             topFilterPanel.add(filterIDTextField);
 
             //Name
             filterNameLabel = new JLabel("Name");
             filterNameTextField = new JTextField(defaultJTextFieldColumns);
             filterNameTextField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             topFilterPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             topFilterPanel.add(filterNameLabel);
             topFilterPanel.add(Box.createRigidArea(new Dimension(12 + strutDistance, 0)));
             topFilterPanel.add(filterNameTextField);
 
             //Bottom Filter panel
             bottomFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             filterPanel.add(bottomFilterPanel);
 
             //Phone
             filterPhoneLabel = new JLabel("Phone number");
             filterPhoneTextField = new JTextField(defaultJTextFieldColumns);
             filterPhoneTextField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             bottomFilterPanel.add(filterPhoneLabel);
             bottomFilterPanel.add(Box.createRigidArea(new Dimension(strutDistance, 0)));
             bottomFilterPanel.add(filterPhoneTextField);
 
             //Adress
             filterAdressLabel = new JLabel("Adress");
             filterAdressTextField = new JTextField(defaultJTextFieldColumns);
             filterAdressTextField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             bottomFilterPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             bottomFilterPanel.add(filterAdressLabel);
             bottomFilterPanel.add(Box.createRigidArea(new Dimension(5 + strutDistance, 0)));
             bottomFilterPanel.add(filterAdressTextField);
 
             //ButtonPanels
             buttonPanel = new JPanel();
             add(buttonPanel, BorderLayout.SOUTH);
             buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
             buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
             buttonPanel.add(Box.createHorizontalGlue());
 
             //cancel-Button
             cancelButton = new JButton("Back");
             cancelButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     setFilterTextFields();
                     getCustomerPanel.setVisible(false);
                     createPanel.centerPanel.setVisible(true);
                 }
             });
             buttonPanel.add(cancelButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //Set-button
             setButton = new JButton("Set selected");
             setButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (customerTable.getSelectedRow() >= 0) {
                         createPanel.setCustomer(Integer.toString(customers.get(customerTable.getSelectedRow()).getID()));
                         setFilterTextFields();
                         getCustomerPanel.setVisible(false);
                         createPanel.centerPanel.setVisible(true);
                     }
                 }
             });
             buttonPanel.add(setButton);
         }
 
         public void setCustomerTable() {
             customers = CarRental.getInstance().requestCustomers(); //update customers array
 
             for (Customer customer : customers) { //update table
                 String[] split = customer.getAdress().split("\n"); //for nicer look
                 String displayed = split[0];
                 for (int i = 1; i < split.length; i++) {
                     displayed = displayed + ", " + split[i];
                 }
 
                 customerTableModel.addRow(new Object[]{customer.getID(), //ID
                             customer.getTelephone(), //Phone
                             customer.getName(), //Name
                             displayed, //Adress
                             customer.getEMail() //E-Mail
                         });
             }
         }
 
         public void setFilterTextFields() {
             filterAdressTextField.setText("");
             filterPhoneTextField.setText("");
             filterNameTextField.setText("");
             filterIDTextField.setText("");
         }
 
         public void filter() {
             //Delete exisiting rows
             customerTableModel.setRowCount(0);
             //Add the rows that match the filter
             for (Customer customer : customers) {
                 //parameters
                 if (filterIDTextField.getText().trim().isEmpty() || //Filter ID is empty OR
                         Integer.toString(customer.getID()).trim().toLowerCase(Locale.ENGLISH).contains(filterIDTextField.getText().toLowerCase(Locale.ENGLISH)) && //Customer matches criteria
                         filterNameTextField.getText().trim().isEmpty() || //Filter name is empty OR
                         customer.getName().trim().toLowerCase(Locale.ENGLISH).contains(filterNameTextField.getText().trim().toLowerCase(Locale.ENGLISH)) && //Customer matches criteria
                         filterPhoneTextField.getText().trim().isEmpty() || //Filter Phone is empty OR
                         Integer.toString(customer.getTelephone()).trim().toLowerCase(Locale.ENGLISH).contains(filterPhoneTextField.getText().trim().toLowerCase(Locale.ENGLISH)) &&//Customer matches criteria
                         filterAdressTextField.getText().trim().isEmpty() || //Adress field is empty OR
                         customer.getAdress().trim().toLowerCase(Locale.ENGLISH).contains(filterAdressTextField.getText().trim().toLowerCase(Locale.ENGLISH))) //Customer matches criteria
                 {
                     customerTableModel.addRow(new Object[]{customer.getID(), //ID
                                 customer.getTelephone(), //Phone
                                 customer.getName(), //Name
                                 customer.getAdress(), //Adress
                                 customer.getEMail() //E-Mail
                             });
                 }
             }
         }
     }
 
     public class CreatePanel extends JPanel {
         //Uses Calendar libary to create Timestamps
         //Dropdown of VehicleTypes
 
         private JTextField vehicleIDTextField, reservationIDTextField, customerIDTextField, startDateTextField, endDateTextField;
         private GetCustomerPanel getCustomerPanel;
         private GetVehiclePanel getVehiclePanel;
         private JPanel centerPanel;
 
         public CreatePanel() {
             //Fields
             JPanel panelContainer, vehiclePanel, endDatePanel, startDatePanel, reservationIDPanel, customerPanel, buttonPanel;
             JLabel vehicleIDLabel, dateFormatLabel, dateFormatLabel2, reservationIDLabel, customerIDLabel, startDateLabel, endDateLabel;
             JButton findVehicleButton, findCustomerButton, createButton, cancelButton;
             final int defaultJTextFieldColumns = 20, strutDistance = 0;
            //createPanel
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Make a reservation"));
             //Panel Container
             panelContainer = new JPanel(new FlowLayout());
             add(panelContainer, BorderLayout.CENTER);
 
             getCustomerPanel = new GetCustomerPanel();
             getVehiclePanel = new GetVehiclePanel();
 
             panelContainer.add(getCustomerPanel);
             panelContainer.add(getVehiclePanel);
 
             getCustomerPanel.setVisible(false);
             getVehiclePanel.setVisible(false);
 
             //Center Panel
             centerPanel = new JPanel();
             centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
             centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
             panelContainer.add(centerPanel);
 
             //Visibility
             panelContainer.setVisible(true);
             centerPanel.setVisible(true);
 
             //Colors
             setBackground(new Color(216, 216, 208));
             centerPanel.setBackground(new Color(239, 240, 236));
 
             //Reservation ID
             reservationIDLabel = new JLabel("Reservation ID");
             reservationIDTextField = new JTextField(" Automaticly generated", defaultJTextFieldColumns);
             reservationIDTextField.setEditable(false);
             reservationIDPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             reservationIDPanel.add(Box.createRigidArea(new Dimension(7, 0)));
             reservationIDPanel.add(reservationIDLabel);
             reservationIDPanel.add(Box.createRigidArea(new Dimension(13 + strutDistance, 0)));
             reservationIDPanel.add(reservationIDTextField);
             centerPanel.add(reservationIDPanel);
 
             //Customer ID
             customerIDLabel = new JLabel("Customer ID");
             customerIDTextField = new JTextField("", defaultJTextFieldColumns);
             customerIDTextField.setBackground(Color.WHITE);
             findCustomerButton = new JButton("Find Customer");
             findCustomerButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     createPanel.setVisible(false);
                     getCustomerPanel.setVisible(true);
                 }
             });
             customerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             customerPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             customerPanel.add(customerIDLabel);
             customerPanel.add(Box.createRigidArea(new Dimension(28 + strutDistance, 0)));
             customerPanel.add(customerIDTextField);
             customerPanel.add(Box.createRigidArea(new Dimension(13, 0)));
             customerPanel.add(findCustomerButton);
             centerPanel.add(customerPanel);
 
             //Vehicle ID
             vehicleIDLabel = new JLabel("Vehicle ID");
             vehicleIDTextField = new JTextField(defaultJTextFieldColumns);
             findVehicleButton = new JButton("Find Vehicle");
             findVehicleButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     createPanel.setVisible(false);
                     getVehiclePanel.setVisible(true);
                 }
             });
 
             vehiclePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             vehiclePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             vehiclePanel.add(vehicleIDLabel);
             vehiclePanel.add(Box.createRigidArea(new Dimension(42 + strutDistance, 0)));
             vehiclePanel.add(vehicleIDTextField);
             vehiclePanel.add(Box.createRigidArea(new Dimension(14 + strutDistance, 0)));
             vehiclePanel.add(findVehicleButton);
             centerPanel.add(vehiclePanel);
 
             //Start date
             startDateLabel = new JLabel("Reserved from");
             startDateTextField = new JTextField(defaultJTextFieldColumns);
             dateFormatLabel = new JLabel("dd-mm-yyyy");
 
             startDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             startDatePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             startDatePanel.add(startDateLabel);
             startDatePanel.add(Box.createRigidArea(new Dimension(16 + strutDistance, 0)));
             startDatePanel.add(startDateTextField);
             startDatePanel.add(Box.createRigidArea(new Dimension(5 + strutDistance, 0)));
             startDatePanel.add(dateFormatLabel);
             centerPanel.add(startDatePanel);
 
             //End date
             endDateLabel = new JLabel("Reserved till");
             endDateTextField = new JTextField(defaultJTextFieldColumns);
             dateFormatLabel2 = new JLabel("dd-mm-yyyy");
 
             endDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             endDatePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             endDatePanel.add(endDateLabel);
             endDatePanel.add(Box.createRigidArea(new Dimension(29 + strutDistance, 0)));
             endDatePanel.add(endDateTextField);
             endDatePanel.add(Box.createRigidArea(new Dimension(5 + strutDistance, 0)));
             endDatePanel.add(dateFormatLabel2);
             centerPanel.add(endDatePanel);
 
             //ButtonPanel
             buttonPanel = new JPanel();
             add(buttonPanel, BorderLayout.SOUTH);
             buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
             buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
             buttonPanel.add(Box.createHorizontalGlue());
 
             //cancelButton
             cancelButton = new JButton("Cancel");
             cancelButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     listPanel.updateListPanel();
                     updateCreatePanel();
                     showListPanel();
                 }
             });
             buttonPanel.add(cancelButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //create-button
             createButton = new JButton("Create");
             createButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (reservationIDTextField.getText().trim().length() > 0
                             && vehicleIDTextField.getText().trim().length() > 0
                             && startDateTextField.getText().trim().length() > 0
                             && endDateTextField.getText().trim().length() > 0) {
                         try {
                             CarRental.getInstance().saveReservation(new Reservation(
                                     CarRental.getInstance().requestNewReservationId(),
                                     Integer.parseInt(vehicleIDTextField.getText()),
                                     new Timestamp(dateFormat.parse(startDateTextField.getText().trim()).getTime()),
                                     new Timestamp(dateFormat.parse(endDateTextField.getText().trim()).getTime()),
                                     Integer.parseInt(customerIDTextField.getText())));
                             CarRental.getInstance().appendLog("Reservation " + reservationIDTextField.getText() + " edited");
                             CarRental.getView().displayError("Reservation " + customerIDTextField.getText() + " edited");
                         } catch (NumberFormatException ex) {
                             CarRental.getView().displayError("Vehicle ID number must be numbers only");
                         } catch (java.text.ParseException ex) {
                             CarRental.getView().displayError("Time fields must be in format dd-mm-yyyy");
                         }
                         reservations = CarRental.getInstance().requestReservations();
                         updateCreatePanel();
                         showListPanel();
 
                     } else { //A TextFild is empty
                         CarRental.getView().displayError("A text field is empty");
                     }
                     updateCreatePanel();
                     listPanel.updateListPanel();
                 }
             });
             buttonPanel.add(createButton);
         }
 
         public void setCustomer(String customerID) {
             customerIDTextField.setText(customerID);
         }
 
         public void setVehicle(String vehicleID) {
             vehicleIDTextField.setText(vehicleID);
         }
 
         public void updateCreatePanel() {
             reservationIDTextField.setText(" Automaticly generated");
             vehicleIDTextField.setText("");
             customerIDTextField.setText("");
             startDateTextField.setText("");
             endDateTextField.setText("");
         }
     }
 
     public class ViewEntityPanel extends JPanel {
 
         private JTextField vehicleIDTextField, reservationIDTextField, customerIDTextField, startDateTextField, endDateTextField;
 
         public ViewEntityPanel() {
             //Fields
             JPanel vehiclePanel, endDatePanel, startDatePanel, reservationIDPanel, customerPanel, centerPanel, buttonPanel;
             JLabel vehicleIDLabel, dateFormatLabel, dateFormatLabel2, reservationIDLabel, customerIDLabel, startDateLabel, endDateLabel;
             JButton deleteButton, editButton, cancelButton;
             final int defaultJTextFieldColumns = 20, strutDistance = 0;
 
             //createPanel
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "View a reservation"));
 
             //Center Panel
             centerPanel = new JPanel();
             centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
             centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
             add(centerPanel, BorderLayout.CENTER);
 
             //Colors
             setBackground(new Color(216, 216, 208));
             centerPanel.setBackground(new Color(239, 240, 236));
 
             //Reservation ID
             reservationIDLabel = new JLabel("Reservation ID");
             reservationIDTextField = new JTextField(" Automaticly generated", defaultJTextFieldColumns);
             reservationIDTextField.setEditable(false);
             reservationIDPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             reservationIDPanel.add(Box.createRigidArea(new Dimension(7, 0)));
             reservationIDPanel.add(reservationIDLabel);
             reservationIDPanel.add(Box.createRigidArea(new Dimension(13 + strutDistance, 0)));
             reservationIDPanel.add(reservationIDTextField);
             centerPanel.add(reservationIDPanel);
 
             //Customer ID
             customerIDLabel = new JLabel("Customer ID");
             customerIDTextField = new JTextField("", defaultJTextFieldColumns);
             customerIDTextField.setBackground(Color.WHITE);
 
             customerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             customerPanel.add(Box.createRigidArea(new Dimension(3, 0)));
             customerPanel.add(customerIDLabel);
             customerPanel.add(Box.createRigidArea(new Dimension(23 + strutDistance, 0)));
             customerPanel.add(customerIDTextField);
             customerPanel.add(Box.createRigidArea(new Dimension(13, 0)));
             centerPanel.add(customerPanel);
 
             //Vehicle ID
             vehicleIDLabel = new JLabel("Vehicle ID");
             vehicleIDTextField = new JTextField(defaultJTextFieldColumns);
 
             vehiclePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             vehiclePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             vehiclePanel.add(vehicleIDLabel);
             vehiclePanel.add(Box.createRigidArea(new Dimension(43 + strutDistance, 0)));
             vehiclePanel.add(vehicleIDTextField);
             centerPanel.add(vehiclePanel);
 
             //Start date
             startDateLabel = new JLabel("Reserved from");
             startDateTextField = new JTextField(defaultJTextFieldColumns);
             dateFormatLabel = new JLabel("dd-mm-yyyy");
 
             startDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             startDatePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             startDatePanel.add(startDateLabel);
             startDatePanel.add(Box.createRigidArea(new Dimension(16 + strutDistance, 0)));
             startDatePanel.add(startDateTextField);
             startDatePanel.add(Box.createRigidArea(new Dimension(5 + strutDistance, 0)));
             startDatePanel.add(dateFormatLabel);
             centerPanel.add(startDatePanel);
 
             //End date
             endDateLabel = new JLabel("Reserved till");
             endDateTextField = new JTextField(defaultJTextFieldColumns);
             dateFormatLabel2 = new JLabel("dd-mm-yyyy");
 
             endDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             endDatePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             endDatePanel.add(endDateLabel);
             endDatePanel.add(Box.createRigidArea(new Dimension(29 + strutDistance, 0)));
             endDatePanel.add(endDateTextField);
             endDatePanel.add(Box.createRigidArea(new Dimension(5 + strutDistance, 0)));
             endDatePanel.add(dateFormatLabel2);
             centerPanel.add(endDatePanel);
 
             //ButtonPanel
             buttonPanel = new JPanel();
             add(buttonPanel, BorderLayout.SOUTH);
             buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
             buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
             buttonPanel.add(Box.createHorizontalGlue());
 
             //deleteButton
             deleteButton = new JButton("Delete");
             deleteButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     String id = Integer.toString(reservationToView.getID());
                     delete(reservationToView);
                     CarRental.getInstance().appendLog("Succesfully deleted reservation " + id);
                     CarRental.getView().displayError("Succesfully deleted reservation " + id);
                     updateViewEntityPanel();
                     listPanel.updateListPanel();
                 }
             });
             buttonPanel.add(deleteButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //editButton
             editButton = new JButton("Save changes");
             editButton.addActionListener(new ActionListener() {
                 //TODO Fix this:
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (reservationIDTextField.getText().trim().length() > 0
                             && vehicleIDTextField.getText().trim().length() > 0
                             && startDateTextField.getText().trim().length() > 0
                             && endDateTextField.getText().trim().length() > 0) {
                         try {
                             CarRental.getInstance().saveReservation(new Reservation(
                                     Integer.parseInt(reservationIDTextField.getText()),
                                     Integer.parseInt(vehicleIDTextField.getText().trim()),
                                     new Timestamp(dateFormat.parse(startDateTextField.getText().trim()).getTime()),
                                     new Timestamp(dateFormat.parse(endDateTextField.getText().trim()).getTime()),
                                     Integer.parseInt(customerIDTextField.getText().trim())));
                             reservations = CarRental.getInstance().requestReservations();
                             CarRental.getInstance().appendLog("Reservation " + reservationIDTextField.getText() + " edited");
                             CarRental.getView().displayError("Reservation " + reservationIDTextField.getText() + " edited");
                             listPanel.updateListPanel();
                             updateViewEntityPanel();
                             showViewEntityPanel();
                         } catch (java.text.ParseException ex) {
                             CarRental.getView().displayError("Time fields must be in format dd-mm-yyyy");
                         } catch( NumberFormatException ex){
                             CarRental.getView().displayError("NumberFormatExceptopm...");
                         }
 
                     } else { //A TextFild is empty
                         CarRental.getView().displayError("A text field is empty");
                     }
                 }
             });
             buttonPanel.add(editButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //cancelButton
             cancelButton = new JButton("Back");
             cancelButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     updateViewEntityPanel();
                     showListPanel();
                     listPanel.updateListPanel();
                 }
             });
             buttonPanel.add(cancelButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
         }
 
         private void delete(Reservation reservation) {
             CarRental.getInstance().deleteReservation(reservation.getID());
         }
 
         public void updateViewEntityPanel() {
             customerIDTextField.setText(" " + reservationToView.getCustomerID());
             reservationIDTextField.setText(" " + reservationToView.getID());
             vehicleIDTextField.setText(" " + reservationToView.getVehicleID());
             startDateTextField.setText(" " + dateFormat.format(new Date(reservationToView.getTStart().getTime())));
             endDateTextField.setText(" " + dateFormat.format(new Date(reservationToView.getTEnd().getTime())));
         }
     }
 
     public class ListPanel extends JPanel {
 
         private JTextField filterCustomerIDTextField, filterReservationIDTextField, filterVehicleIDTextField, filterStartDateTextField, filterEndDateTextField;
         private JTable reservationTable;
         private DefaultTableModel reservationTableModel;
 
         public ListPanel() {
             //Fields
             JPanel centerPanel, reservationListPanel, filterPanel, topFilterPanel, middleFilterPanel, bottomFilterPanel, buttonPanel;
             JScrollPane scrollPane;
             JButton cancelButton, viewButton;
             final int defaultJTextFieldColumns = 20, strutDistance = 0;
 
             //listPanel
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "List of customers"));
 
             //centerPanel
             centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
             centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
             add(centerPanel, BorderLayout.CENTER);
 
             //customerListPanel
             reservationListPanel = new JPanel();
 
             //Colors
             setBackground(new Color(216, 216, 208));
 
             //Creating the table model
             reservationTableModel = new DefaultTableModel(new Object[]{"ID", "VehicleID", "Start", "End", "CustomerID"}, 0);
 
             //Creating the table
             reservationTable = new JTable(reservationTableModel);
             //adding it to it's own scrollpane
             scrollPane = new JScrollPane(reservationTable);
             //Setting the default size for the scrollpane
             reservationTable.setPreferredScrollableViewportSize(new Dimension(680, 200));
             reservationListPanel.add(scrollPane);
             centerPanel.add(reservationListPanel);
 
             //FilterPanel
             JLabel filterReservationIDLabel, filterCustomerIDLabel, filterVehicleIDLabel, filterStartDateLabel, filterEndDateLabel;
 
             filterPanel = new JPanel();
             filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.PAGE_AXIS));
             filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 2), "Filters"));
             centerPanel.add(filterPanel);
 
             //top row of filters
             topFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             filterPanel.add(topFilterPanel);
 
             //Reservation ID
             filterReservationIDLabel = new JLabel("Reservation ID");
             filterReservationIDTextField = new JTextField(defaultJTextFieldColumns);
             filterReservationIDTextField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             topFilterPanel.add(Box.createRigidArea(new Dimension(0, 0)));
             topFilterPanel.add(filterReservationIDLabel);
             topFilterPanel.add(Box.createRigidArea(new Dimension(17 + strutDistance, 0)));
             topFilterPanel.add(filterReservationIDTextField);
 
             //Vehicle ID
             filterVehicleIDLabel = new JLabel("Vehicle ID");
             filterVehicleIDTextField = new JTextField(defaultJTextFieldColumns);
             filterVehicleIDTextField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             topFilterPanel.add(Box.createRigidArea(new Dimension(19, 0)));
             topFilterPanel.add(filterVehicleIDLabel);
             topFilterPanel.add(Box.createRigidArea(new Dimension(12 + strutDistance, 0)));
             topFilterPanel.add(filterVehicleIDTextField);
 
             //Customer ID
             filterCustomerIDLabel = new JLabel("Customer ID");
             filterCustomerIDTextField = new JTextField(defaultJTextFieldColumns);
             filterCustomerIDTextField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             middleFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             filterPanel.add(middleFilterPanel);
 
             middleFilterPanel.add(Box.createRigidArea(new Dimension(5, 0)));
             middleFilterPanel.add(filterCustomerIDLabel);
             middleFilterPanel.add(Box.createRigidArea(new Dimension(12 + strutDistance, 0)));
             middleFilterPanel.add(filterCustomerIDTextField);
 
             //Bottom Filter panel
             bottomFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             filterPanel.add(bottomFilterPanel);
 
             //Start Date
             filterStartDateLabel = new JLabel("Start");
             filterStartDateTextField = new JTextField(defaultJTextFieldColumns);
             filterStartDateTextField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             bottomFilterPanel.add(Box.createRigidArea(new Dimension(strutDistance, 50)));
             bottomFilterPanel.add(filterStartDateLabel);
             bottomFilterPanel.add(Box.createRigidArea(new Dimension(strutDistance, 0)));
             bottomFilterPanel.add(filterStartDateTextField);
 
             //End Date
             filterEndDateLabel = new JLabel("End");
             filterEndDateTextField = new JTextField(defaultJTextFieldColumns);
             filterEndDateTextField.addKeyListener(new KeyAdapter() {
 
                 public void keyReleased(KeyEvent e) {
                     filter();
                 }
             });
 
             bottomFilterPanel.add(filterEndDateLabel);
             bottomFilterPanel.add(Box.createRigidArea(new Dimension(5 + strutDistance, 0)));
             bottomFilterPanel.add(filterEndDateTextField);
 
             //ButtonPanels
             buttonPanel = new JPanel();
             add(buttonPanel, BorderLayout.SOUTH);
             buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
             buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
             buttonPanel.add(Box.createHorizontalGlue());
 
             //cancel-Button
             cancelButton = new JButton("Back");
             cancelButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     setFilterTextFields();
                     showListPanel();
                 }
             });
             buttonPanel.add(cancelButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //View-button
             viewButton = new JButton("View selected");
             viewButton.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (reservationTable.getSelectedRow() >= 0) {
                         reservationToView = reservations.get(reservationTable.getSelectedRow());
                         viewEntityPanel.updateViewEntityPanel();
                         showViewEntityPanel();
                         CarRental.getInstance().appendLog("Showing reservation \"" + reservationToView.getID() + "\" now.");
                     }
                 }
             });
             buttonPanel.add(viewButton);
         }
 
         public void updateListPanel() {
             setFilterTextFields();
             reservations = CarRental.getInstance().requestReservations(); //update array
             
             reservationTableModel.setRowCount(0);
             for (Reservation reservation : reservations) { //update table
                 reservationTableModel.addRow(new Object[]{
                             reservation.getID(), //ID
                             reservation.getVehicleID(), //Vehicle ID
                             dateFormat.format(new Date(reservation.getTStart().getTime())), //TStart 
                             dateFormat.format(new Date(reservation.getTEnd().getTime())), //TEnd 
                             reservation.getCustomerID() //Customer
                         });
             }
         }
 
         public void setFilterTextFields() {
             filterCustomerIDTextField.setText("");
             filterEndDateTextField.setText("");
             filterReservationIDTextField.setText("");
             filterStartDateTextField.setText("");
             filterVehicleIDTextField.setText("");
         }
 
         public void filter() {
             //Delete exisiting rows
             reservationTableModel.setRowCount(0);
             //Add the rows that match the filter
             reservations = CarRental.getInstance().requestReservations();
 
             for (Reservation reservation : reservations) {
                 Timestamp tStart = new Timestamp(0);
                 Timestamp tEnd = new Timestamp(0);
                 if (!(filterStartDateTextField.getText().trim().isEmpty()) || !(filterEndDateTextField.getText().trim().isEmpty())) {
                     try {
                         tStart.setTime(dateFormat.parse(filterStartDateTextField.getText().trim()).getTime());
                         tEnd.setTime(dateFormat.parse(filterEndDateTextField.getText().trim()).getTime());
                     } catch (java.text.ParseException ex) {
                         CarRental.getView().displayError("Time fields must be in format dd-mm-yyyy");
                     }
                 }
 
                 //parameters
                 if (filterReservationIDTextField.getText().trim().isEmpty() || //Filter ID is empty OR
                         Integer.toString(reservation.getID()).trim().toLowerCase(Locale.ENGLISH).contains(filterReservationIDTextField.getText().toLowerCase(Locale.ENGLISH)) && //Customer matches criteria
                         filterCustomerIDTextField.getText().trim().isEmpty() || //Filter Phone is empty OR
                         Integer.toString(reservation.getCustomerID()).trim().toLowerCase(Locale.ENGLISH).contains(filterCustomerIDTextField.getText().trim().toLowerCase(Locale.ENGLISH)) && //Customer matches criteria
                         filterVehicleIDTextField.getText().trim().isEmpty() || //Adress field is empty OR
                         Integer.toString(reservation.getVehicleID()).trim().toLowerCase(Locale.ENGLISH).contains(filterVehicleIDTextField.getText().trim().toLowerCase(Locale.ENGLISH)) && //Customer matches criteria
                         filterStartDateTextField.getText().trim().isEmpty()
                         || tStart.before(reservation.getTEnd())
                         && filterEndDateTextField.getText().trim().isEmpty()
                         || tEnd.before(reservation.getTStart())) {
 
                     reservationTableModel.addRow(new Object[]{
                                 reservation.getID(), //ID
                                 reservation.getVehicleID(), //Vehicle ID
                                 dateFormat.format(new Date(reservation.getTStart().getTime())), //TStart
                                 dateFormat.format(new Date(reservation.getTEnd().getTime())), //TEnd
                                 reservation.getCustomerID(),});
                 }
             }
         }
     }
 }
