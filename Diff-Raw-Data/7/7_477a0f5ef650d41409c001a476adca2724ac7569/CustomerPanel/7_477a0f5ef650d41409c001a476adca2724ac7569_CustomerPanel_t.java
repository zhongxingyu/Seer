 package carrental;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import javax.swing.*;
 import java.util.Calendar;
 import javax.swing.border.TitledBorder;
 
 /**
  * This is the main panel regarding vehicles.
  * It contains JPanels for every relevant screen, when dealing with customers.
  * @author CNN
  * @version 2011-12-07
  */
 public class CustomerPanel extends SuperPanel {
 
     private JPanel mainScreenPanel, createPanel, viewEntityPanel, listPanel; //main panels used in this class
     private JPanel emptyPanel = null;
     private Customer customerToView; //specific customer, used to view details
     private ArrayList<Customer> customers; //array of costumers
     private View view = View.getInstance(); //
     
     public CustomerPanel() {
         remakeAll(); //Method also called from View
         //Sets the different subpanels. Also adds them to this object with JPanel.add().
         AssignAndAddSubPanels(mainScreenPanel, createPanel, viewEntityPanel, emptyPanel, listPanel);
         this.setPreferredSize(new Dimension(800,600));
 
         //Removes the default gaps between components
         setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        mainScreenPanel.add(viewEntityPanel, BorderLayout.CENTER);
        mainScreenPanel.add(listPanel, BorderLayout.CENTER);
         showMainScreenPanel();
     }
 
     //Temporary Main
     //TODO: Remove this:
     public static void main(String[] args) {
         JFrame frame = new JFrame("CustomerPanel");
         frame.setPreferredSize(new Dimension(800, 600));
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         Container contentPane = frame.getContentPane();
         contentPane.add(new CustomerPanel());
         frame.pack();
         frame.setVisible(true);
     }
     
     public void setVehicleToView (Customer customer){
         customerToView = customer;
     }
     
     public void setCustomerArray(ArrayList<Customer> customers){
         this.customers = customers;
     } 
 
     @Override
     public void makeMainScreenPanel() {
         //Fields
         TitledBorder titleBorder;
         
         //mainScreenPanel
         mainScreenPanel = new JPanel();
         mainScreenPanel.setLayout(new BorderLayout());
         titleBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Customers");
         mainScreenPanel.setBorder(titleBorder);
         
        //test
         mainScreenPanel.add(viewEntityPanel, BorderLayout.CENTER);
         mainScreenPanel.add(listPanel, BorderLayout.CENTER);
     }
 
     @Override
     public void makeCreatePanel() {
         //Fields
         JPanel centerPanel, idPanel, namePanel, phonePanel, adressPanel, buttonPanel;
         JLabel customerIDLabel, customerNameLabel, customerPhoneLabel, customerAdressLabel;
         JTextField customerNameTextField, customerPhoneTextField, customerAdressTextField;
         JButton createButton, cancelButton;
         final int defaultJTextFieldColumns = 20, strutDistance = 0;
         
         //createPanel
         createPanel = new JPanel();
         createPanel.setLayout(new BorderLayout());
         createPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Create a vehicle"));
         
         //Center Panel
         centerPanel = new JPanel();
         centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
         centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
         createPanel.add(centerPanel, BorderLayout.CENTER);
         
         //Colors
         createPanel.setBackground(new Color(216, 216, 208));
         centerPanel.setBackground(new Color(239, 240, 236));
         
         //ID
         customerIDLabel = new JLabel("ID is automaticly generated"); 
         idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         idPanel.add(Box.createHorizontalStrut(5));
         idPanel.add(customerIDLabel);
         centerPanel.add(idPanel);
         
         //Name
         customerNameLabel = new JLabel("Name");
         customerNameTextField = new JTextField(defaultJTextFieldColumns);
         namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         namePanel.add(Box.createHorizontalStrut(5));
         namePanel.add(customerNameLabel);
         namePanel.add(Box.createHorizontalStrut(87 + strutDistance));
         namePanel.add(customerNameTextField);
         centerPanel.add(namePanel);
 
         //Phone
         customerPhoneLabel = new JLabel("Phone number");
         customerPhoneTextField = new JTextField(defaultJTextFieldColumns);
         phonePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         phonePanel.add(Box.createHorizontalStrut(5));
         phonePanel.add(customerPhoneLabel);
         phonePanel.add(Box.createHorizontalStrut(43 + strutDistance));
         phonePanel.add(customerPhoneTextField);
         centerPanel.add(phonePanel);
 
         //TODO Split adress
         //Adress
         customerAdressLabel = new JLabel("Adress");
         customerAdressTextField = new JTextField(defaultJTextFieldColumns);
         adressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         adressPanel.add(Box.createHorizontalStrut(5));
         adressPanel.add(customerAdressLabel);
         adressPanel.add(Box.createHorizontalStrut(101 + strutDistance));
         adressPanel.add(customerAdressTextField);
         centerPanel.add(adressPanel);
 
         //ButtonPanel
         buttonPanel = new JPanel();
         createPanel.add(buttonPanel, BorderLayout.SOUTH);
         buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
         buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
         buttonPanel.add(Box.createHorizontalGlue());
 
         //cancelButton
         cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 //TODO NICLASONLY Set text() for all fields aka blank
                 showMainScreenPanel();
             }
         });
         buttonPanel.add(cancelButton);
         buttonPanel.add(Box.createHorizontalStrut(5));
 
         //create-button
         createButton = new JButton("Create");
         createButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 //TODO Save customer to db and set customerIDLabel
             }
         });
         buttonPanel.add(createButton);
     }
 
     @Override
     public void makeViewEntityPanel() {
         //Fields
         JPanel centerPanel, idPanel, namePanel, phonePanel, adressPanel, buttonPanel;
         JLabel customerIDLabel, customerNameLabel, customerPhoneLabel, customerAdressLabel;
         JTextField customerIDTextField, customerNameTextField, customerPhoneTextField, customerAdressTextField;
         String customerID, customerName, customerPhone, customerAdress;
         JButton cancelButton;
         final int defaultJTextFieldColumns = 20, strutDistance = 0;
         
         //createPanel
         viewEntityPanel = new JPanel();
         viewEntityPanel.setLayout(new BorderLayout());
         viewEntityPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "View vehicles"));
         
         //Center Panel
         centerPanel = new JPanel();
         centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
         centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
         viewEntityPanel.add(centerPanel, BorderLayout.CENTER);
         
         //Colors
         viewEntityPanel.setBackground(new Color(216, 216, 208));
         centerPanel.setBackground(new Color(239, 240, 236));
         
         //ID
         customerIDLabel = new JLabel("Customer ID"); 
         customerID = "0000";
         customerIDTextField = new JTextField(customerID, defaultJTextFieldColumns);
         customerIDTextField.setEditable(false);
         idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         idPanel.add(Box.createHorizontalStrut(5));
         idPanel.add(customerIDLabel);
         idPanel.add(Box.createHorizontalStrut(87 + strutDistance));
         idPanel.add(customerIDTextField);
         centerPanel.add(idPanel);
         
         //Name
         customerNameLabel = new JLabel("Name");
         customerName = "Hans Hansen";
         customerNameTextField = new JTextField(customerName, defaultJTextFieldColumns);
         customerNameTextField.setEditable(false);
         namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         namePanel.add(Box.createHorizontalStrut(5));
         namePanel.add(customerNameLabel);
         namePanel.add(Box.createHorizontalStrut(87 + strutDistance));
         namePanel.add(customerNameTextField);
         centerPanel.add(namePanel);
 
         //Phone
         customerPhoneLabel = new JLabel("Phone number");
         customerPhone = "12345678";
         customerPhoneTextField = new JTextField(customerPhone, defaultJTextFieldColumns);
         customerPhoneTextField.setEditable(false);
         phonePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         phonePanel.add(Box.createHorizontalStrut(5));
         phonePanel.add(customerPhoneLabel);
         phonePanel.add(Box.createHorizontalStrut(43 + strutDistance));
         phonePanel.add(customerPhoneTextField);
         centerPanel.add(phonePanel);
 
         //TODO Split adress
         //Adress
         customerAdressLabel = new JLabel("Adress");
         customerAdress = "9800 Jylland";
         customerAdressTextField = new JTextField(customerAdress, defaultJTextFieldColumns);
         customerAdressTextField.setEditable(false);
         adressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
         adressPanel.add(Box.createHorizontalStrut(5));
         adressPanel.add(customerAdressLabel);
         adressPanel.add(Box.createHorizontalStrut(101 + strutDistance));
         adressPanel.add(customerAdressTextField);
         centerPanel.add(adressPanel);
         
         //TODO insert list
 
         //ButtonPanel
         buttonPanel = new JPanel();
         viewEntityPanel.add(buttonPanel, BorderLayout.SOUTH);
         buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
         buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
         buttonPanel.add(Box.createHorizontalGlue());
 
         //cancelButton
         cancelButton = new JButton("Back");
         cancelButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 //TODO NICLASONLY Set text() for all fields aka blank
                 showMainScreenPanel();
             }
         });
         buttonPanel.add(cancelButton);
         buttonPanel.add(Box.createHorizontalStrut(5));
     }
 
     @Override
     public void makeAddTypePanel(){ //not used
         
     }       
 
     @Override
     public void makeListPanel() {
         //Fields
         JPanel centerPanel, customerListPanel, filterPanel, topFilterPanel, bottomFilterPanel, buttonPanel;
         JScrollPane scrollPane;
         JTable customerTable;
         JLabel customerIDLabel, customerNameLabel, customerPhoneLabel, customerAdressLabel;
         JTextField customerIDTextField, customerNameTextField, customerPhoneTextField, customerAdressTextField;
         JButton cancelButton, filterButton;
         final int defaultJTextFieldColumns = 20, strutDistance = 0;
 
         //listPanel
         listPanel = new JPanel();
         listPanel.setLayout(new BorderLayout());
         listPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "List of customers"));
         
         //centerPanel
         centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
         centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
         listPanel.add(centerPanel, BorderLayout.CENTER);
         
         //customerListPanel.
         customerListPanel = new JPanel();
         
         //Colors
         listPanel.setBackground(new Color(216, 216, 208));
 
         //Testing Table setup
         Object[] columnNames = {"ID", "Name", "Phone", "Adress"};
         ArrayList<ArrayList<String>> customerData = new ArrayList<>();
         ArrayList<String> rowData;
         //TODO Add adress needs splitting
         //getting the data in the arrayList - this might be unnecessary in final implementation depending on how this class receives the simple type objects.
         Customer testCustomer = new Customer(100, 123456789, "Hans Hansen", "Jylland", "email");
         for (int i = 0; i < 30; i++) {
             rowData = new ArrayList<>();
             rowData.add("" + testCustomer.getID());
             rowData.add("" + testCustomer.getTelephone());
             rowData.add(testCustomer.getName());
             rowData.add(testCustomer.getAdress());
            //rowData.add(testCustomer.getEMail()); //makes assert error on build
             customerData.add(rowData);
             assert customerData.get(i).size()== columnNames.length;
         }
         
         //Converting to Object[][] for the JTable
         Object[][] tableData = new Object[customerData.size()][columnNames.length];
         for (int i = 0; i < customerData.size(); i++) { //  'i' represents a row
             for (int j = 0; j < columnNames.length; j++) { //'j' represents a certain cell on row 'i'
                 tableData[i][j] = customerData.get(i).get(j); //out of bounds cannot happen because of the conditions in the for loops.
             }
         }
         
         //Creating the table
         customerTable = new JTable(tableData, columnNames);
         //adding it to it's own scrollpane
         scrollPane = new JScrollPane(customerTable);
         //Setting the default size for the scrollpane
         customerTable.setPreferredScrollableViewportSize(new Dimension(680, 200));
         customerListPanel.add(scrollPane);
         centerPanel.add(customerListPanel);
 
         //FilterPanel
         JLabel filterAdressLabel, filterPhoneLabel, filterNameLabel, filterIDLabel;
         JTextField filterAdressTextField, filterPhoneTextField, filterNameTextField, filterIDTextField;
         
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
 
         topFilterPanel.add(Box.createHorizontalStrut(5));
         topFilterPanel.add(filterIDLabel);
         topFilterPanel.add(Box.createHorizontalStrut(77 + strutDistance));
         topFilterPanel.add(filterIDTextField);
         
         //Name
         filterNameLabel = new JLabel("Name");
         filterNameTextField = new JTextField(defaultJTextFieldColumns);
 
         topFilterPanel.add(Box.createHorizontalStrut(5));
         topFilterPanel.add(filterNameLabel);
         topFilterPanel.add(Box.createHorizontalStrut(77 + strutDistance));
         topFilterPanel.add(filterNameTextField);
 
         //Bottom Filter panel
         bottomFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         filterPanel.add(bottomFilterPanel);
 
         //Phone
         filterPhoneLabel = new JLabel("Phone number");
         filterPhoneTextField = new JTextField(defaultJTextFieldColumns);
 
         bottomFilterPanel.add(filterPhoneLabel);
         bottomFilterPanel.add(Box.createHorizontalStrut(strutDistance));
         bottomFilterPanel.add(filterPhoneTextField);
         
         //Adress
         filterAdressLabel = new JLabel("Adress");
         filterAdressTextField = new JTextField(defaultJTextFieldColumns);
 
         bottomFilterPanel.add(filterAdressLabel);
         bottomFilterPanel.add(Box.createHorizontalStrut(strutDistance));
         bottomFilterPanel.add(filterAdressTextField);
 
         //ButtonPanels
         buttonPanel = new JPanel();
         listPanel.add(buttonPanel, BorderLayout.SOUTH);
         buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
         buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
         buttonPanel.add(Box.createHorizontalGlue());
 
         //cancel-Button
         cancelButton = new JButton("Back");
         cancelButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 //TODO NICLASONLY Set text() for all fields aka blank
                 showMainScreenPanel();
             }
         });
         buttonPanel.add(cancelButton);
         buttonPanel.add(Box.createHorizontalStrut(5));
 
         //filter-button
         filterButton = new JButton("Filter");
         filterButton.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 //TODO NICLASONLY make the database update here
             }
         });
         buttonPanel.add(filterButton);
 
 
     }
 }
