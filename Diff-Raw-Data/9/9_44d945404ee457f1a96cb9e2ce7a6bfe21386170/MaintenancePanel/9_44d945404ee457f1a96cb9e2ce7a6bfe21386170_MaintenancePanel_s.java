 package carrental;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import javax.swing.table.DefaultTableModel;
 import java.sql.Timestamp;
 import java.util.Date;
 
 /**
  * This is the main panel regarding maintenances.
  * It contains JPanels for every relevant screen, when dealing with maintenenaces.
  * These are implemented as inner classes.
  * @author CNN
  * @version 2011-12-13
  */
 public class MaintenancePanel extends SuperPanel {
 
     private Maintenance maintenanceToView;
     private MaintenanceType maintenanceTypeToView;
     private ArrayList<Maintenance> maintenanceList;
     private ArrayList<MaintenanceType> maintenanceTypes;
     private ArrayList<Vehicle> vehicles;
     private CreatePanel createPanel;
     private ViewMaintenancePanel viewMaintenancePanel;
     private ViewMaintenanceTypePanel viewMaintenanceTypePanel;
     private ListPanel listPanel;
     private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
 
     /**
      * Sets up the maintenance panel and all its subpanels.
      */
     public MaintenancePanel() {
         maintenanceList = CarRental.getInstance().requestMaintenances();
         maintenanceTypes = CarRental.getInstance().requestMaintenanceTypes();
         vehicles = CarRental.getInstance().requestVehicles();
         createPanel = new CreatePanel();
         viewMaintenancePanel = new ViewMaintenancePanel();
         viewMaintenanceTypePanel = new ViewMaintenanceTypePanel();
         listPanel = new ListPanel();
         //Sets the different subpanels. Also adds them to this object with JPanel.add().
         AssignAndAddSubPanels(new MainScreenPanel(), createPanel, viewMaintenancePanel, new AddTypePanel(), viewMaintenanceTypePanel, listPanel);
         this.setPreferredSize(new Dimension(800, 600));
         showListPanel();
     }
     
     public void setMaintenanceToView(Maintenance m) {
         maintenanceToView = m;
     }
 
     @Override
     public void showCreatePanel() {
         createPanel.update();
         super.showCreatePanel();
     }
 
     @Override
     public void showViewEntityPanel() {
         viewMaintenancePanel.update();
         super.showViewEntityPanel();
     }
 
     @Override
     public void showListPanel() {
         listPanel.update();
         super.showListPanel();
     }
 
     @Override
     public void showViewTypePanel() {
         viewMaintenanceTypePanel.update();
         super.showViewTypePanel();
     }
     
     public class MainScreenPanel extends JPanel {}
 
     /**
      * This inner class creates a JPanel with the functionality to create a maintenance.
      */
     public class CreatePanel extends JPanel {
 
         private JTextField dateStartField, dateEndField;
         private DefaultComboBoxModel maintenanceTypeComboModel, vehicleComboModel;
         private JCheckBox typeIsServiceCheckBox;
 
         public CreatePanel() {
 
             JPanel centerPanel, buttonPanel, maintenanceTypePanel, datePanel, vehiclePanel;
             JLabel maintenanceTypeLabel, fromLabel, toLabel, vehicleLabel;
             JButton createButton, cancelButton;
             final JComboBox maintenanceTypeCombo, vehicleCombo;
             final int defaultJTextFieldColumns = 20, strutDistance = 0;
 
             //Panel settings
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Schedule maintenance"));
             //Center Panel
             centerPanel = new JPanel();
             centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
             centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
 
             add(centerPanel, BorderLayout.CENTER);
 
             //Colors
             setBackground(new Color(216, 216, 208));
             centerPanel.setBackground(new Color(239, 240, 236));
 
             //Vehicle Type
             maintenanceTypeLabel = new JLabel("Maintenance Type");
             maintenanceTypeComboModel = new DefaultComboBoxModel();
             maintenanceTypeCombo = new JComboBox(maintenanceTypeComboModel);
 
             for (MaintenanceType maintenanceType : maintenanceTypes) {
                 maintenanceTypeComboModel.addElement(maintenanceType.getName());
             }
             maintenanceTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             maintenanceTypePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             maintenanceTypePanel.add(maintenanceTypeLabel);
 
             maintenanceTypePanel.add(Box.createRigidArea(new Dimension(30 + strutDistance, 0)));
             maintenanceTypePanel.add(maintenanceTypeCombo);
             maintenanceTypePanel.add(Box.createRigidArea(new Dimension(30 + strutDistance, 0)));
             
             typeIsServiceCheckBox = new JCheckBox("Service check");
             typeIsServiceCheckBox.setEnabled(false);
             
             maintenanceTypeCombo.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if(maintenanceTypes.size() > maintenanceTypeCombo.getSelectedIndex() &&
                             maintenanceTypeCombo.getSelectedIndex() != -1)
                         typeIsServiceCheckBox.setSelected(maintenanceTypes.get(maintenanceTypeCombo.getSelectedIndex()).getIs_service());
                 }
             });
             
             maintenanceTypePanel.add(typeIsServiceCheckBox);
 
             centerPanel.add(maintenanceTypePanel);
 
             //Date
             fromLabel = new JLabel("Start date");
             dateStartField = new JTextField(defaultJTextFieldColumns);
             toLabel = new JLabel("End date");
             dateEndField = new JTextField(defaultJTextFieldColumns);
             datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             datePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             datePanel.add(fromLabel);
             datePanel.add(Box.createRigidArea(new Dimension(15 + strutDistance, 0)));
             datePanel.add(dateStartField);
             datePanel.add(Box.createRigidArea(new Dimension(15, 0)));
             datePanel.add(toLabel);
             datePanel.add(Box.createRigidArea(new Dimension(15 + strutDistance, 0)));
             datePanel.add(dateEndField);
             centerPanel.add(datePanel);
             
             //Vehicle
             vehicleLabel = new JLabel("Vehicle");
             vehicleComboModel = new DefaultComboBoxModel();
             vehicleCombo = new JComboBox(vehicleComboModel);
             
             for (Vehicle vehicle : vehicles) {
                 vehicleCombo.addItem(vehicle.getDescription());
             }
             
             vehiclePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             vehiclePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             vehiclePanel.add(vehicleLabel);
             vehiclePanel.add(Box.createRigidArea(new Dimension(30 + strutDistance, 0)));
             vehiclePanel.add(vehicleCombo);
             centerPanel.add(vehiclePanel);
 
             //ButtonPanels
             buttonPanel = new JPanel();
 
             centerPanel.add(buttonPanel);
 
             buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
             buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
             buttonPanel.add(Box.createHorizontalGlue());
 
             //cancel-Button
             cancelButton = new JButton("Cancel");
             cancelButton.addActionListener(
                     new ActionListener() {
                         @Override
                         public void actionPerformed(ActionEvent e) {
                             update();
                             showListPanel();
                         }
                     });
             
             buttonPanel.add(cancelButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(15, 0)));
 
             //create-button
             createButton = new JButton("Create");
             createButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (!dateStartField.getText().trim().isEmpty()
                             && !dateEndField.getText().trim().isEmpty()) {
                         try {
                             String[] dateStartSplit = dateStartField.getText().split("-");
                             String[] dateEndSplit = dateEndField.getText().split("-");
                             if(dateStartSplit[0].length() == 2 &&
                                     dateStartSplit[1].length() == 2 &&
                                     dateStartSplit[2].length() == 4 &&
                                     dateEndSplit[0].length() == 2 &&
                                     dateEndSplit[1].length() == 2 &&
                                     dateEndSplit[2].length() == 4) {
                                 Maintenance newMaintenance = new Maintenance(CarRental.getInstance().requestNewMaintenanceId(),
                                         vehicles.get(vehicleCombo.getSelectedIndex()).getID(),
                                         new Timestamp(dateFormat.parse(dateStartField.getText().trim()).getTime()),
                                         new Timestamp(dateFormat.parse(dateEndField.getText().trim()).getTime()),
                                         maintenanceTypes.get(maintenanceTypeCombo.getSelectedIndex()).getID());
                                 CarRental.getInstance().saveMaintenance(newMaintenance);
                                 maintenanceList = CarRental.getInstance().requestMaintenances();
                             }
                             else {
                                 CarRental.getInstance().appendLog("Date format in date-fields should be DD-MM-YYYY.");
                             }
                         } catch (java.text.ParseException ex) {
                             CarRental.getInstance().appendLog("Could not format the date entered, maintenance not scheduled",ex);
                         }
                     } else {
                         CarRental.getInstance().appendLog("The maintenance wasn't created. Please specify both start and end dates.");
                     }
                 }
             });
             buttonPanel.add(createButton);
         }
 
         /**
          * Updates the panel. Textfields are set blank and the maintenance types
          * and vehicles are updated.
          */
         public void update() {
             //Update combo boxes
             vehicleComboModel.removeAllElements();
             for(Vehicle v : vehicles) {
                 vehicleComboModel.addElement(v.getDescription());
             }
             maintenanceTypeComboModel.removeAllElements();
             for (MaintenanceType maintenanceType : maintenanceTypes) {
                 maintenanceTypeComboModel.addElement(maintenanceType.getName());
             }
             //Sets all text fields blank
             dateStartField.setText(null);
             dateEndField.setText(null);
         }
     }
 
     /**
      * This inner class creates a JPanel which shows a certain maintenance. The
      * maintenance is selected in the ListPanel-class
      */
     public class ViewMaintenancePanel extends JPanel {
         private JTextField dateStartField, dateEndField;
         private DefaultComboBoxModel maintenanceTypeComboModel, vehicleComboModel;
         private JComboBox maintenanceTypeCombo, vehicleCombo;
         private JCheckBox typeIsServiceCheckBox;
 
         public ViewMaintenancePanel() {
             JPanel centerPanel, buttonPanel, maintenanceTypePanel, datePanel, vehiclePanel;
             JLabel maintenanceTypeLabel, fromLabel, toLabel, vehicleLabel;
             JButton backButton, editButton, deleteButton, viewSelectedTypeButton;
             final int defaultJTextFieldColumns = 20;
 
             //Panel settings
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Viewing Maintenance"));
 
             //Center Panel
             centerPanel = new JPanel();
             centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
             centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
             add(centerPanel, BorderLayout.CENTER);
 
             //Colors
             setBackground(new Color(216, 216, 208));
             centerPanel.setBackground(new Color(239, 240, 236));
 
             //Vehicle Type
             maintenanceTypeLabel = new JLabel("Maintenance Type");
             maintenanceTypeComboModel = new DefaultComboBoxModel();
             maintenanceTypeCombo = new JComboBox(maintenanceTypeComboModel);
 
             typeIsServiceCheckBox = new JCheckBox("Service check");
             typeIsServiceCheckBox.setEnabled(false);
             
             maintenanceTypeCombo.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if(maintenanceTypes.size() > maintenanceTypeCombo.getSelectedIndex() && maintenanceTypeCombo.getSelectedIndex() != -1)
                         typeIsServiceCheckBox.setSelected(maintenanceTypes.get(maintenanceTypeCombo.getSelectedIndex()).getIs_service());
                 }
             });
             
             viewSelectedTypeButton = new JButton("View selected type");
             viewSelectedTypeButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     maintenanceTypeToView = maintenanceTypes.get(maintenanceTypeCombo.getSelectedIndex());
                     showViewTypePanel();
                     CarRental.getInstance().appendLog("Showing maintenance type \"" + maintenanceTypeToView.getName() + "\" now.");
                 }
             });
 
             maintenanceTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             maintenanceTypePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             maintenanceTypePanel.add(maintenanceTypeLabel);
 
             maintenanceTypePanel.add(Box.createRigidArea(new Dimension(30, 0)));
             maintenanceTypePanel.add(maintenanceTypeCombo);
             
             maintenanceTypePanel.add(Box.createRigidArea(new Dimension(30, 0)));
             maintenanceTypePanel.add(typeIsServiceCheckBox);
 
             maintenanceTypePanel.add(Box.createRigidArea(new Dimension(30, 0)));
             maintenanceTypePanel.add(viewSelectedTypeButton);
 
             centerPanel.add(maintenanceTypePanel);
             
             //Date
             fromLabel = new JLabel("Start date");
             dateStartField = new JTextField(defaultJTextFieldColumns);
             toLabel = new JLabel("End date");
             dateEndField = new JTextField(defaultJTextFieldColumns);
             datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             datePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             datePanel.add(fromLabel);
             datePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             datePanel.add(dateStartField);
             
             datePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             datePanel.add(toLabel);
             datePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             datePanel.add(dateEndField);
 
             centerPanel.add(datePanel);
             
             //Vehicle
             vehicleLabel = new JLabel("Vehicle");
             vehicleComboModel = new DefaultComboBoxModel();
             vehicleCombo = new JComboBox(vehicleComboModel);
             for (Vehicle vehicle : vehicles) {
                 vehicleCombo.addItem(vehicle.getDescription());
             }
             
             vehiclePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             vehiclePanel.add(Box.createRigidArea(new Dimension(5, 0)));
             vehiclePanel.add(vehicleLabel);
             vehiclePanel.add(Box.createRigidArea(new Dimension(20, 0)));
             vehiclePanel.add(vehicleCombo);
             centerPanel.add(vehiclePanel);
             
             //ButtonPanels
             buttonPanel = new JPanel();
             add(buttonPanel, BorderLayout.SOUTH);
             buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
             buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
             buttonPanel.add(Box.createHorizontalGlue());
 
             //Cancel-Button
             backButton = new JButton("Back");
             backButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     showListPanel();
                 }
             });
             buttonPanel.add(backButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //Delete-Button
             deleteButton = new JButton("Delete");
             deleteButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     CarRental.getInstance().deleteMaintenance(maintenanceToView.getID());
                     CarRental.getInstance().appendLog("Maintenance #" + maintenanceToView.getID() + " deleted from the database.");
                     maintenanceList = CarRental.getInstance().requestMaintenances();
                     showListPanel();
                 }
             });
             buttonPanel.add(deleteButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //Create-button
             editButton = new JButton("Save changes");
             editButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     assert (maintenanceToView != null); //VehicleToView should never be null here
                     if (!dateStartField.getText().trim().isEmpty()
                                     && !dateEndField.getText().trim().isEmpty()) {
                         try {
                             Maintenance updatedMaintenance = new Maintenance(maintenanceToView.getID(),
                                             vehicles.get(vehicleCombo.getSelectedIndex()).getID(),
                                             new Timestamp(dateFormat.parse(dateStartField.getText().trim()).getTime()),
                                             new Timestamp(dateFormat.parse(dateEndField.getText().trim()).getTime()),
                                             maintenanceTypes.get(maintenanceTypeCombo.getSelectedIndex()).getID());
 
                             CarRental.getInstance().saveMaintenance(updatedMaintenance);
                             CarRental.getInstance().appendLog("Maintenance #" + updatedMaintenance.getID() + " changed in the database.");
                             maintenanceList = CarRental.getInstance().requestMaintenances();
                         } catch (java.text.ParseException ex) {
                             CarRental.getInstance().appendLog("Failed to parse the dates provided, format wrong.",ex);
                         }
                     }
                 }
             });
             buttonPanel.add(editButton);
             update();
         }
 
         /**
          * Updates the panel to show the selected vehicle. This type is selected in the ViewVehiclePanel-class
          */
         public void update() {
             //Update comboboxes
             int v_index = 0;
             vehicleComboModel.removeAllElements();
             for(Vehicle v : vehicles) {
                 vehicleComboModel.addElement(v.getDescription());
                 if(maintenanceToView != null && v.getID() == maintenanceToView.getVehicleID())
                     vehicleCombo.setSelectedIndex(v_index);
                 v_index++;
             }
             maintenanceTypeComboModel.removeAllElements();
             int type_index = 0;
             for (MaintenanceType maintenanceType : maintenanceTypes) {
                 maintenanceTypeComboModel.addElement(maintenanceType.getName());
                 if(maintenanceToView != null && maintenanceType.getID() == maintenanceToView.getTypeID())
                     maintenanceTypeCombo.setSelectedIndex(type_index);
                 type_index++;
             }
             //Sets all text fields
             if(maintenanceToView != null) dateStartField.setText(dateFormat.format(new Date(maintenanceToView.getTStart().getTime())));
             if(maintenanceToView != null) dateEndField.setText(dateFormat.format(new Date(maintenanceToView.getTEnd().getTime())));
         }
     }
 
     /**
      * This inner class creates a JPanel which shows a certain maintenance type.
      * The maintenance type is selected in the ViewMaintenancePanel-class
      */
     public class ViewMaintenanceTypePanel extends JPanel {
         private JButton backButton, editButton, deleteButton;
         JTextField typeField;
         JCheckBox isServiceBox;
         final int defaultJTextFieldColumns = 15, strutDistance = 0;
 
         public ViewMaintenanceTypePanel() {
             JPanel viewPanel, buttonPanel, centerPanel;
             JLabel typeLabel;
             
             //Panel settings
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Viewing maintenance type"));
             setBackground(new Color(216, 216, 208));
             centerPanel = new JPanel();
             centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
             add(centerPanel, BorderLayout.CENTER);
 
             //View panel
             viewPanel = new JPanel();
             typeLabel = new JLabel("Name");
             typeField = new JTextField(defaultJTextFieldColumns);
             isServiceBox = new JCheckBox("Service check");
             
             viewPanel.add(typeLabel);
             viewPanel.add(Box.createRigidArea(new Dimension(10, 0)));
             viewPanel.add(typeField);
             viewPanel.add(Box.createRigidArea(new Dimension(10, 0)));
             viewPanel.add(isServiceBox);
             centerPanel.add(viewPanel);
 
             //Create the buttons needed
             buttonPanel = new JPanel();
             buttonPanel.setLayout(new FlowLayout());
             
             //Back-button
             backButton = new JButton("Back");
             backButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     showViewEntityPanel();
                     CarRental.getInstance().appendLog("Showing maintenance #" + maintenanceToView.getID() + " now.");
                 }
             });
             buttonPanel.add(backButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //Delete-button
             deleteButton = new JButton("Delete");
             deleteButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     boolean inUse = false;
                     for (Maintenance maintenance : maintenanceList) {
                         if (maintenance.getTypeID() == maintenanceTypeToView.getID()) {
                             inUse = true;
                         }
                     }
                     if (!inUse) {
                         CarRental.getInstance().deleteMaintenanceType(maintenanceTypeToView.getID());
                         CarRental.getInstance().appendLog("Maintenance type \"" + maintenanceTypeToView.getName() + "\" deleted from the database.");
                         maintenanceTypes = CarRental.getInstance().requestMaintenanceTypes();
                         showViewEntityPanel();
                     } else {
                         CarRental.getInstance().appendLog("Maintenance type \"" + maintenanceTypeToView.getName() + "\" is in use by at least one car. Could not be deleted.");
                     }
                 }
             });
             buttonPanel.add(deleteButton);
             buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             // Edit-button
             editButton = new JButton("Save changes");
             editButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (!typeField.getText().trim().isEmpty()) {
                         //Checks if name is in use already
                         boolean nameTaken = false;
                         for (MaintenanceType maintenanceType : maintenanceTypes) {
                             if (maintenanceType.getName().equals(typeField.getText().trim()) && maintenanceType.getID() != maintenanceTypeToView.getID()) { //if the name is in use and it´s not from the currently viewed vehicle
                                 nameTaken = true;
                             }
                         }
                         if (!nameTaken) {
                             MaintenanceType updatedMaintenanceType = new MaintenanceType(maintenanceTypeToView.getID(), typeField.getText().trim(), isServiceBox.isSelected());
 
                             CarRental.getInstance().saveMaintenanceType(updatedMaintenanceType);
                             CarRental.getInstance().appendLog("Maintenance type \"" + typeField.getText().trim() + "\" changed in the database.");
                             maintenanceTypes = CarRental.getInstance().requestMaintenanceTypes(); //update ment for if name check is implemented
                         } else {
                             CarRental.getInstance().appendLog("Another maintenance type with the name, \"" + typeField.getText().trim() + "\", already exists.");
                         }
                     } else {
                         CarRental.getInstance().appendLog("The maintenance type wasn't edited. All fields must be filled.");
                     }
                 }
             });
             buttonPanel.add(editButton);
             centerPanel.add(buttonPanel);
         }
 
         /**
          * Updates the panel to show the selected maintenance type. This type is
          * selected in the ViewMaintenancePanel-class
          */
         public void update() {
             typeField.setText(maintenanceTypeToView.getName());
             isServiceBox.setSelected(maintenanceTypeToView.getIs_service());
         }
     }
 
     /**
      * This inner class creates a JPanel with the functionality to create a new
      * maintenance type.
      */
     public class AddTypePanel extends JPanel {
 
         private JButton cancelButton, createButton;
         private JTextField typeField;
         private JCheckBox isServiceBox;
         private final int defaultJTextFieldColumns = 15;
 
         public AddTypePanel() {
             //Create the panel for viewing the vehicle type.
             JPanel viewPanel, buttonPanel, centerPanel;
             JLabel typeLabel;
             
             //Panel settings
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Viewing maintenance type"));
             setBackground(new Color(216, 216, 208));
             centerPanel = new JPanel();
             centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
             add(centerPanel, BorderLayout.CENTER);
 
             //View panel
             viewPanel = new JPanel();
             
             typeLabel = new JLabel("Name");
             typeField = new JTextField(defaultJTextFieldColumns);
             isServiceBox = new JCheckBox("Service check");
             
             viewPanel.add(typeLabel);
             viewPanel.add(typeField);
             viewPanel.add(isServiceBox);
             centerPanel.add(viewPanel);
             
             //Create the buttons needed
             buttonPanel = new JPanel();
             buttonPanel.setLayout(new FlowLayout());
             
             //Cancel-button
             cancelButton = new JButton("Cancel");
             cancelButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     showListPanel();
                 }
             });
             buttonPanel.add(cancelButton);
 
             // Create-button
             createButton = new JButton("Create");
             createButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (!typeField.getText().trim().isEmpty()) {
                         //Checks if name is in use already
                         boolean nameTaken = false;
                         for (MaintenanceType maintenanceType : maintenanceTypes) {
                             if (typeField.getText().trim().equals(maintenanceType.getName())) {
                                 nameTaken = true;
                             }
                         }
                         if (!nameTaken) {
                             MaintenanceType newMaintenanceType = new MaintenanceType(CarRental.getInstance().requestNewMaintenanceTypeId(), typeField.getText().trim(), isServiceBox.isSelected());
 
                             CarRental.getInstance().saveMaintenanceType(newMaintenanceType);
                             CarRental.getInstance().appendLog("Maintenance type \"" + typeField.getText().trim() + "\" added to the database.");
                             maintenanceTypes = CarRental.getInstance().requestMaintenanceTypes();
                         } else {
                             CarRental.getInstance().appendLog("A vehicle type with the name \"" + typeField.getText().trim() + "\" already exists.");
                         }
                     } else {
                         CarRental.getInstance().appendLog("The maintenance type wasn't created. You need to enter text in all the fields.");
                     }
                 }
             });
             buttonPanel.add(createButton);
             centerPanel.add(buttonPanel);
         }
         
         /**
          * Updates the create panel, resetting the fields to blank.
          */
         public void update() {
             typeField.setText(null);
             isServiceBox.setSelected(false);
         }
     }
     
     /**
      * This inner class creates a JPanel with a list of maintenances. It is
      * possible to filter the list.
      */
     public class ListPanel extends JPanel {
 
         private DefaultTableModel maintenanceTableModel;
         private JComboBox maintenanceTypeCombo;
         private DefaultComboBoxModel maintenanceTypeComboModel;
         private JTextField dateStartField, dateEndField;
         private JCheckBox typeIsServiceBox;
         private int currentVehicleTypeIndex = -1;
 
         public ListPanel() {
             JPanel centerPanel, maintenanceListPanel, filterPanel, topFilterPanel, middleFilterPanel, bottomFilterPanel, buttonPanel;
             JLabel maintenanceTypeLabel, dateStartLabel, dateEndLabel;
             JButton cancelButton, viewButton;
             final JTable maintenanceTable;
             JScrollPane listScrollPane;
             final int defaultJTextFieldColumns = 20, strutDistance = 0;
 
             //Panel settings
             setLayout(new BorderLayout());
             setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "List of maintenances"));
 
             //CenterPanel
             centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
             centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 40));
             add(centerPanel, BorderLayout.CENTER);
 
             //VehicleListPanel.
             maintenanceListPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 
             //Colors
             setBackground(new Color(216, 216, 208));
 
             //Creating the table
             maintenanceTableModel = new DefaultTableModel(new Object[]{"Vehicle", "Type", "Service check", "Start date", "End date"}, 0);
             maintenanceTable = new JTable(maintenanceTableModel);
 
             listScrollPane = new JScrollPane(maintenanceTable);
             maintenanceTable.setPreferredScrollableViewportSize(new Dimension(700, 200));
             maintenanceListPanel.add(listScrollPane);
             centerPanel.add(maintenanceListPanel);
 
             //FilterPanel
             filterPanel = new JPanel();
             filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.PAGE_AXIS));
             filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 2), "Filters"));
             centerPanel.add(filterPanel);
 
             //top row of filters
             topFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             filterPanel.add(topFilterPanel);
 
             //Vehicle Type
             maintenanceTypeLabel = new JLabel("Maintenance Type");
             maintenanceTypeComboModel = new DefaultComboBoxModel();
             maintenanceTypeCombo = new JComboBox(maintenanceTypeComboModel);
             maintenanceTypeCombo.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (currentVehicleTypeIndex == -1 && maintenanceTypeCombo.getSelectedIndex() > 0) { //if the current selection hasn't been set and it was not just set to "All"
                         filter();
                         currentVehicleTypeIndex = maintenanceTypeCombo.getSelectedIndex();
                     } else if (currentVehicleTypeIndex > -1 && currentVehicleTypeIndex != maintenanceTypeCombo.getSelectedIndex()) {
                         filter();
                         currentVehicleTypeIndex = maintenanceTypeCombo.getSelectedIndex();
                     }
                 }
             });
             topFilterPanel.add(maintenanceTypeLabel);
             topFilterPanel.add(Box.createRigidArea(new Dimension(16 + strutDistance, 0)));
             topFilterPanel.add(maintenanceTypeCombo);
             topFilterPanel.add(Box.createRigidArea(new Dimension(91, 0)));
 
             //Checkbox
             typeIsServiceBox = new JCheckBox("Service check");
             typeIsServiceBox.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     filter();
                 }
             });
             
             topFilterPanel.add(Box.createRigidArea(new Dimension(62 + strutDistance, 0)));
             topFilterPanel.add(typeIsServiceBox);
             topFilterPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
             //Middle Filter panel
             middleFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             filterPanel.add(middleFilterPanel);
 
             //Date start
             dateStartLabel = new JLabel("Start date");
             dateStartField = new JTextField(defaultJTextFieldColumns);
             dateStartField.addKeyListener(new KeyAdapter() {
                 @Override
                 public void keyReleased(KeyEvent e) {
                     if(dateStartField.getText().matches("[0-9]{2}-[0-9]{2}-[0-9]{4}") ||
                             dateStartField.getText().trim().isEmpty()) filter();
                 }
             });
             
             middleFilterPanel.add(dateStartLabel);
             middleFilterPanel.add(Box.createRigidArea(new Dimension(11 + strutDistance, 0)));
             middleFilterPanel.add(dateStartField);
             
             //Date End
             dateEndLabel = new JLabel("End date");
             dateEndField = new JTextField(defaultJTextFieldColumns);
             dateEndField.addKeyListener(new KeyAdapter() {
                 @Override
                 public void keyReleased(KeyEvent e) {
                     if(dateEndField.getText().matches("[0-9]{2}-[0-9]{2}-[0-9]{4}") ||
                             dateEndField.getText().trim().isEmpty()) filter();
                 }
             });
             middleFilterPanel.add(Box.createRigidArea(new Dimension(10 + strutDistance, 0)));
             middleFilterPanel.add(dateEndLabel);
             middleFilterPanel.add(Box.createRigidArea(new Dimension(11 + strutDistance, 0)));
             middleFilterPanel.add(dateEndField);
 
             //ButtonPanels
             buttonPanel = new JPanel();
             add(buttonPanel, BorderLayout.SOUTH);
             buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
             buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); //add some space between the right edge of the screen
             buttonPanel.add(Box.createHorizontalGlue());
             
             //View-button
             viewButton = new JButton("View selected");
             viewButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (maintenanceTable.getSelectedRow() >= 0) { //getSelectedRow returns -1 if no row is selected
                         for (Maintenance maintenance : maintenanceList) {
                             if ((maintenance.getVehicleID() < 1 && maintenanceTableModel.getValueAt(maintenanceTable.getSelectedRow(), 0).toString().isEmpty() ||
                                     CarRental.getInstance().requestVehicle(maintenance.getVehicleID()).getDescription().trim().equals(maintenanceTableModel.getValueAt(maintenanceTable.getSelectedRow(),0).toString().trim())) &&
                                     dateFormat.format(maintenance.getTStart().getTime()).equals(maintenanceTableModel.getValueAt(maintenanceTable.getSelectedRow(), 3)) && 
                                     dateFormat.format(maintenance.getTEnd().getTime()).equals(maintenanceTableModel.getValueAt(maintenanceTable.getSelectedRow(),4))) {
                                 maintenanceToView = maintenance;
                                 break;
                             }
                         }
                         showViewEntityPanel();
                         CarRental.getInstance().appendLog("Showing maintenance #" + maintenanceToView.getID() + " now.");
                     }
                 }
             });
             buttonPanel.add(viewButton);
         }
 
         /**
          * Updates the panel to show an updated list of maintenances.
          */
         public void update() {
             currentVehicleTypeIndex = -1;
 
             //Delete exisiting rows
             maintenanceTableModel.setRowCount(0);
             //Add the updated rows with vehicles
             for (Maintenance maintenance : maintenanceList) {
                 maintenanceTableModel.addRow(new String[]{
                     CarRental.getInstance().requestVehicle(maintenance.getVehicleID()).getDescription(),
                     CarRental.getInstance().requestMaintenanceType(maintenance.getTypeID()).getName(),
                     (CarRental.getInstance().requestMaintenanceType(maintenance.getTypeID()).getIs_service() ? "Yes" : "No"),
                     dateFormat.format(new Date(maintenance.getTStart().getTime())),
                     dateFormat.format(new Date(maintenance.getTEnd().getTime()))
                 });
             }
             assert (maintenanceList.size() == maintenanceTableModel.getRowCount()) :
                     "Maintenances list size: " + maintenanceList.size() + "\n Maintenances table rows: " + maintenanceTableModel.getRowCount();
 
             //Update the JComboBox
             maintenanceTypeComboModel.removeAllElements();
             maintenanceTypeComboModel.addElement("All");
             for (MaintenanceType maintenanceType : maintenanceTypes) {
                 maintenanceTypeComboModel.addElement(maintenanceType.getName());
             }
 
             //Sets all text fields blank
             dateStartField.setText(null);
             dateEndField.setText(null);
             typeIsServiceBox.setSelected(false);
         }
 
         /**
          * Updates the list of maintenances so that only entries matching the
          * filters will be shown.
          */
         public void filter() {
             //Delete exisiting rows
             maintenanceTableModel.setRowCount(0);
 
             //Add the rows that match the filter
             for (Maintenance maintenance : maintenanceList) {
                 try {
                     if (((maintenanceTypeCombo.getSelectedIndex() == -1 || maintenanceTypeCombo.getSelectedIndex() == 0) ||
                             maintenance.getTypeID() == maintenanceTypes.get(maintenanceTypeCombo.getSelectedIndex() - 1).getID()) &&
                             (dateStartField.getText().trim().isEmpty() ||
                             maintenance.getTStart().after(new Timestamp(dateFormat.parse(dateStartField.getText().trim()).getTime()))) &&
                             (dateEndField.getText().trim().isEmpty() ||
                             maintenance.getTEnd().before(new Timestamp(dateFormat.parse(dateEndField.getText().trim()).getTime()))) &&
                             (!typeIsServiceBox.isSelected() ||
                             typeIsServiceBox.isSelected() && CarRental.getInstance().requestMaintenanceType(maintenance.getTypeID()).getIs_service())
                             ) {
                         
                         maintenanceTableModel.addRow(new String[]{
                             CarRental.getInstance().requestVehicle(maintenance.getVehicleID()).getDescription(),
                             CarRental.getInstance().requestMaintenanceType(maintenance.getTypeID()).getName(),
                             (CarRental.getInstance().requestMaintenanceType(maintenance.getTypeID()).getIs_service() ? "Yes" : "No"),
                             dateFormat.format(new Date(maintenance.getTStart().getTime())),
                             dateFormat.format(new Date(maintenance.getTEnd().getTime()))
                         });
                     }
                 } catch (java.text.ParseException e) {
                     CarRental.getInstance().appendLog("Could not parse date entered in date field. Format should be DD-MM-YYYY",e);
                 }
             }
         }
     }
 }
