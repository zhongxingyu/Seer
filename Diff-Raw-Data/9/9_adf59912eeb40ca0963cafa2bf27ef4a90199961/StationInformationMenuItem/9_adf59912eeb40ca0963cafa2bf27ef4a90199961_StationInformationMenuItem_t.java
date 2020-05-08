 package dk.frv.eavdam.menus;
 
 import dk.frv.eavdam.data.Address;
 import dk.frv.eavdam.data.AISFixedStationData;
 import dk.frv.eavdam.data.AISFixedStationStatus;
 import dk.frv.eavdam.data.AISFixedStationType;
 import dk.frv.eavdam.data.Antenna;
 import dk.frv.eavdam.data.AntennaType;
 import dk.frv.eavdam.data.EAVDAMData;
 import dk.frv.eavdam.data.EAVDAMUser;
 import dk.frv.eavdam.data.Person;
 import dk.frv.eavdam.io.XMLExporter;
 import dk.frv.eavdam.io.XMLImporter;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.xml.bind.JAXBException;
 
 /**
  * This class represents a menu item that opens a frame where the user can edit
  * stations' information.
  */
 public class StationInformationMenuItem extends JMenuItem {
 
     public static final long serialVersionUID = 3L;
 
     public StationInformationMenuItem(EavdamMenu eavdamMenu) {
         super("Edit Station Information");       
         addActionListener(new StationInformationActionListener(eavdamMenu, null));
     }
 
     public StationInformationMenuItem(EavdamMenu eavdamMenu, String stationName) {
         super("Edit Station Information");       
         addActionListener(new StationInformationActionListener(eavdamMenu, stationName));
     }
 }
         
         
 class StationInformationActionListener implements ActionListener, DocumentListener {
 
     private EavdamMenu eavdamMenu;
 
     private JDialog dialog;
     
     private JComboBox selectStationComboBox;
     private JTextField stationNameTextField;
     private JComboBox stationTypeComboBox;
     private JTextField latitudeTextField;
     private JTextField longitudeTextField;    
     private JTextField mmsiNumberTextField;
     private JTextField transmissionPowerTextField;
     private JComboBox stationStatusComboBox;
 
     private JComboBox antennaTypeComboBox;
     private JLabel antennaHeightLabel;
     private JTextField antennaHeightTextField;
     private JLabel terrainHeightLabel;
     private JTextField terrainHeightTextField;    
     private JLabel headingLabel;
     private JTextField headingTextField;
     private JLabel fieldOfViewAngleLabel;
     private JTextField fieldOfViewAngleTextField;                    
     private JLabel gainLabel;
     private JTextField gainTextField;   
     
     private JTextArea additionalInformationJTextArea;
     
     private JButton saveButton;
     private JButton deleteButton;
     private JButton cancelButton;
 
     private EAVDAMData data;  
     
     private String initiallySelectedStationName;
     private int selectedStationIndex = 0; 
     
     private boolean noListeners = false;
 
     public StationInformationActionListener(EavdamMenu eavdamMenu, String stationName) {
         this.eavdamMenu = eavdamMenu;
         this.initiallySelectedStationName = stationName;
     }
      
     public void actionPerformed(ActionEvent e) {
         
         if (e.getSource() instanceof StationInformationMenuItem) {
                                         
             dialog = new JDialog(eavdamMenu.getOpenMapFrame(), "Station Information", true);
 
             selectStationComboBox = new JComboBox();
             selectStationComboBox.addActionListener(this);
             stationNameTextField = new JTextField(16);
             stationNameTextField.getDocument().addDocumentListener(this);
             stationTypeComboBox = new JComboBox(new String[] {"AIS Base Station", "AIS Repeater", "AIS Receiver station", "AIS AtoN station"});
             stationTypeComboBox.addActionListener(this);
             latitudeTextField = new JTextField(16);
             latitudeTextField.getDocument().addDocumentListener(this);
             longitudeTextField = new JTextField(16);
             longitudeTextField.getDocument().addDocumentListener(this);
             mmsiNumberTextField = new JTextField(16);
             mmsiNumberTextField.getDocument().addDocumentListener(this);
             transmissionPowerTextField = new JTextField(16);
             transmissionPowerTextField.getDocument().addDocumentListener(this);
             stationStatusComboBox = new JComboBox(new String[] {"Operative", "Inoperative"});
             stationStatusComboBox.addActionListener(this);
     
             antennaTypeComboBox = new JComboBox(new String[] {"No antenna", "Omnidirectional", "Directional"});
             antennaTypeComboBox.setSelectedIndex(0);
             antennaTypeComboBox.addActionListener(this);
             antennaHeightLabel = new JLabel("Antenna height above terrain (meters):");
             antennaHeightTextField = new JTextField(16);
             antennaHeightTextField.getDocument().addDocumentListener(this);
             terrainHeightLabel = new JLabel("Terrain height above sealevel (meters):");
             terrainHeightTextField = new JTextField(16);
             terrainHeightTextField.getDocument().addDocumentListener(this);
             headingLabel = new JLabel("Heading (degrees - integer):");
             headingTextField = new JTextField(16);
             headingTextField.getDocument().addDocumentListener(this);
             fieldOfViewAngleLabel = new JLabel("Field of View angle (degrees - integer)");
             fieldOfViewAngleTextField = new JTextField(16);           
             fieldOfViewAngleTextField.getDocument().addDocumentListener(this);
             gainLabel = new JLabel("Gain (dB)");
             gainTextField = new JTextField(16);
             gainTextField.getDocument().addDocumentListener(this);
             
             additionalInformationJTextArea = new JTextArea("");                
             additionalInformationJTextArea.getDocument().addDocumentListener(this);
             
             loadStation(selectedStationIndex);
             
             if (data == null || (data != null && data.getStations() == null) ||
                     (data != null && data.getStations() != null && data.getStations().length == 0)) {
                 dialog = new JDialog(eavdamMenu.getOpenMapFrame(), "No stations available", true);
                 JPanel panel = new JPanel();
                 panel.add(new JLabel("There are no stations. Please, add at least one to be able to edit them."));
                 dialog.getContentPane().add(panel);
                 
             } else {
                 JPanel panel = new JPanel();
                 panel.setLayout(new GridBagLayout());                  
                 
                 JPanel p1 = new JPanel(new GridBagLayout());
                 p1.setBorder(BorderFactory.createTitledBorder("Select station"));
                 GridBagConstraints c = new GridBagConstraints();
                 c.insets = new Insets(5,5,5,5);
                 c.gridx = 0;
                 c.gridy = 0;                  
                 c.anchor = GridBagConstraints.LINE_START;     
                 c.weightx = 0.5;
                 p1.add(selectStationComboBox, c);
     
                 c.gridx = 0;
                 c.gridy = 0;
                 c.gridheight = 1;
                 c.anchor = GridBagConstraints.FIRST_LINE_START;
                 c.fill = GridBagConstraints.HORIZONTAL;
                 panel.add(p1, c);
                                   
                 JPanel p2 = new JPanel(new GridBagLayout());
                 p2.setBorder(BorderFactory.createTitledBorder("General information"));
                 c.gridx = 0;
                 c.gridy = 0;                   
                 c.anchor = GridBagConstraints.LINE_START;
                 c.fill = GridBagConstraints.NONE;
                 p2.add(new JLabel("Descriptive name for the station:"), c);
                 c.gridx = 1;                
                 p2.add(stationNameTextField, c);                    
                 c.gridx = 0;
                 c.gridy = 1;                  
                 p2.add(new JLabel("Type of the fixed AIS station:"), c);
                 c.gridx = 1;
                 p2.add(stationTypeComboBox, c);                                                                       
                 c.gridx = 0;
                 c.gridy = 2;                                         
                 p2.add(new JLabel("Latitude in decimal degrees, datum must be WGS84:"), c);
                 c.gridx = 1;                    
                 p2.add(latitudeTextField, c);                    
                 c.gridx = 0;
                 c.gridy = 3;                  
                 p2.add(new JLabel("Longitude in decimal degrees, datum must be WGS84:"), c);
                 c.gridx = 1;                    
                 p2.add(longitudeTextField, c);        
                 c.gridx = 0;
                 c.gridy = 4;                  
                 p2.add(new JLabel("MMSI number (optional for receivers):"), c);
                 c.gridx = 1;                    
                 p2.add(mmsiNumberTextField, c);
                 c.gridx = 0;
                 c.gridy = 5;                 
                 p2.add(new JLabel("Transmission power (Watt):"), c);
                 c.gridx = 1;                    
                 p2.add(transmissionPowerTextField, c);
                 c.gridx = 0;
                 c.gridy = 6;                 
                 p2.add(new JLabel("Status of the fixed AIS station:"), c);
                 c.gridx = 1;                    
                 p2.add(stationStatusComboBox, c);
     
                 c.gridx = 0;
                 c.gridy = 1;
                 c.anchor = GridBagConstraints.FIRST_LINE_START;
                 c.fill = GridBagConstraints.HORIZONTAL;
                 panel.add(p2, c);                    
                   
                 JPanel p3 = new JPanel(new GridBagLayout());
                 p3.setBorder(BorderFactory.createTitledBorder("Antenna information"));
                 c.gridx = 0;
                 c.gridy = 0;                 
                 c.anchor = GridBagConstraints.LINE_START;                    
                 c.fill = GridBagConstraints.NONE;
                 p3.add(new JLabel("Antenna type:"), c);
                 c.gridx = 1;                    
                 p3.add(antennaTypeComboBox, c);
                 c.gridx = 0;
                 c.gridy = 1;                  
                 p3.add(antennaHeightLabel, c);
                 c.gridx = 1;                    
                 p3.add(antennaHeightTextField, c);                    
                 c.gridx = 0;
                 c.gridy = 2;                  
                 p3.add(terrainHeightLabel, c);
                 c.gridx = 1;                    
                 p3.add(terrainHeightTextField, c); 
                 c.gridx = 0;
                 c.gridy = 3;
                 p3.add(headingLabel, c);
                 c.gridx = 1;                    
                 p3.add(headingTextField, c); 
                 c.gridx = 0;
                 c.gridy = 4;                  
                 p3.add(fieldOfViewAngleLabel, c);
                 c.gridx = 1;                    
                 p3.add(fieldOfViewAngleTextField, c);                                         
                 c.gridx = 0;
                 c.gridy = 5;                  
                 p3.add(gainLabel, c);
                 c.gridx = 1;                    
                 p3.add(gainTextField, c);                       
     
                 c.gridx = 0;
                 c.gridy = 2;
                 c.anchor = GridBagConstraints.FIRST_LINE_START;
                 c.fill = GridBagConstraints.HORIZONTAL;
                 panel.add(p3, c);                      
                                                          
                 additionalInformationJTextArea.setLineWrap(true);
                 additionalInformationJTextArea.setWrapStyleWord(true);                    
                 JScrollPane p4 = new JScrollPane(additionalInformationJTextArea);
                 p4.setBorder(BorderFactory.createTitledBorder("Additional information"));
                 p4.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                 p4.setPreferredSize(new Dimension(580, 90));
                 p4.setMaximumSize(new Dimension(580, 90));
                 
                 c.gridx = 0;
                 c.gridy = 3;
                 c.anchor = GridBagConstraints.FIRST_LINE_START;
                 c.fill = GridBagConstraints.HORIZONTAL;
                 panel.add(p4, c);
                 
                 saveButton = new JButton("Save", null);        
                 saveButton.setVerticalTextPosition(AbstractButton.BOTTOM);
                 saveButton.setHorizontalTextPosition(AbstractButton.CENTER);
                 saveButton.setPreferredSize(new Dimension(100, 20));
                 saveButton.setMaximumSize(new Dimension(100, 20));
                 saveButton.addActionListener(this);
                 
                 deleteButton = new JButton("Delete", null);        
                 deleteButton.setVerticalTextPosition(AbstractButton.BOTTOM);
                 deleteButton.setHorizontalTextPosition(AbstractButton.CENTER);
                 deleteButton.setPreferredSize(new Dimension(100, 20));
                 deleteButton.setMaximumSize(new Dimension(100, 20));
                 deleteButton.addActionListener(this);            
                 
                 cancelButton = new JButton("Cancel", null);        
                 cancelButton.setVerticalTextPosition(AbstractButton.BOTTOM);
                 cancelButton.setHorizontalTextPosition(AbstractButton.CENTER);
                 cancelButton.setPreferredSize(new Dimension(100, 20));
                 cancelButton.setMaximumSize(new Dimension(100, 20));                              
                 cancelButton.addActionListener(this);
     
                 JPanel buttonPanel = new JPanel();
                 buttonPanel.add(saveButton);          
                 saveButton.setEnabled(false);              
                 buttonPanel.add(deleteButton);
                 if (data.getStations() == null || data.getStations().length == 0) {
                     deleteButton.setVisible(false);
                 }
                 buttonPanel.add(cancelButton);                    
                 c.gridx = 0;
                 c.gridy = 4;
                 c.fill = GridBagConstraints.NONE;
                 c.anchor = GridBagConstraints.CENTER;                    
                 panel.add(buttonPanel, c);
     
                 dialog.getContentPane().add(panel);
             }
                                                                          
             int frameWidth = 620;
             int frameHeight = 770;
             Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
             dialog.setBounds((int) screenSize.getWidth()/2 - frameWidth/2,
                 (int) screenSize.getHeight()/2 - frameHeight/2, frameWidth, frameHeight);
             dialog.setVisible(true);
         
         } else if (saveButton != null && e.getSource() == saveButton) {
          
             boolean success = saveStation(selectedStationIndex);
 
             if (success) {
                 selectStationComboBox.removeActionListener(this);
                 selectStationComboBox.setSelectedItem(stationNameTextField.getText());
                 saveButton.setEnabled(false);
                 deleteButton.setVisible(true);
                 selectStationComboBox.addActionListener(this);
                 eavdamMenu.getStationLayer().updateStations();
             }
             
         } else if (deleteButton != null && e.getSource() == deleteButton) {
             int response = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to delete the current station?", "Confirm action", JOptionPane.YES_NO_OPTION);
             if (response == JOptionPane.YES_OPTION) {
                 noListeners = true;
                 deleteStation(selectedStationIndex);
                 selectStationComboBox.removeItemAt(selectedStationIndex);
                 if (selectStationComboBox.getItemCount() == 0) {
                     dialog.setVisible(false);                    
                     dialog = new JDialog(eavdamMenu.getOpenMapFrame(), "No stations available", true);
                     JPanel panel = new JPanel();
                     panel.add(new JLabel("There are no stations. Please, add at least one to be able to edit them."));
                     dialog.getContentPane().add(panel);
                     int frameWidth = 620;
                     int frameHeight = 770;
                     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                     dialog.setBounds((int) screenSize.getWidth()/2 - frameWidth/2,
                         (int) screenSize.getHeight()/2 - frameHeight/2, frameWidth, frameHeight);
                     dialog.setVisible(true);
                 } else {
                     selectedStationIndex = 0;
                     loadStation(selectedStationIndex);
                     saveButton.setEnabled(false);
                 }
                 noListeners = false;
                 eavdamMenu.getStationLayer().updateStations();
             } else if (response == JOptionPane.NO_OPTION) {                        
                 // do nothing
             }    
         } else if (cancelButton != null && e.getSource() == cancelButton) {
             if (saveButton.isEnabled()) {
                 int response = JOptionPane.showConfirmDialog(dialog,
                     "Do you want to save the changes made to the current station?",
                     "Confirm action", JOptionPane.YES_NO_OPTION);
                 if (response == JOptionPane.YES_OPTION) {
                     boolean success = saveStation(selectedStationIndex);                    
                     if (success) {
                         eavdamMenu.getStationLayer().updateStations();
                         dialog.dispose();
                     }
                 } else if (response == JOptionPane.NO_OPTION) {                        
                     dialog.dispose();
                 }
             } else {       
                 dialog.dispose();                  
             }
         
         } else if (!noListeners && selectStationComboBox != null && e.getSource() == selectStationComboBox) {
             if (selectStationComboBox.getSelectedIndex() != selectedStationIndex) {
                 noListeners = true;
                 if (saveButton.isEnabled()) {
                     int response = JOptionPane.showConfirmDialog(dialog,
                         "Do you want to save the changes made to the current station?",
                         "Confirm action", JOptionPane.YES_NO_CANCEL_OPTION);
                     if (response == JOptionPane.YES_OPTION) {
                         boolean success = saveStation(selectedStationIndex);
                         if (success) {
                             selectedStationIndex = selectStationComboBox.getSelectedIndex();
                             loadStation(selectedStationIndex);
                             saveButton.setEnabled(false); 
                         }
                     } else if (response == JOptionPane.NO_OPTION) {                        
                         selectedStationIndex = selectStationComboBox.getSelectedIndex();
                         loadStation(selectedStationIndex);
                         saveButton.setEnabled(false); 
                     } else if (response == JOptionPane.CANCEL_OPTION) {
                         // do nothing
                     }
                 } else {
                     selectedStationIndex = selectStationComboBox.getSelectedIndex();
                     loadStation(selectedStationIndex);                    
                 }
                 noListeners = false;
             }
         
         } else if (!noListeners && antennaTypeComboBox != null && e.getSource() == antennaTypeComboBox) {
             saveButton.setEnabled(true);
             updateAntennaTypeComboBox();                   
         
         } else if (saveButton != null) {
             if (saveButton != null) {
                 if (isChanged(selectedStationIndex)) {
                     saveButton.setEnabled(true);
                 } else {
                     saveButton.setEnabled(false);
                 }
             }
         }
     }
     
     public void changedUpdate(DocumentEvent e) {
         if (saveButton != null) {
             if (isChanged(selectedStationIndex)) {
                 saveButton.setEnabled(true);
             } else {
                 saveButton.setEnabled(false);
             }
         }
     }  
     
     public void insertUpdate(DocumentEvent e) {
         if (saveButton != null) {
             if (isChanged(selectedStationIndex)) {
                 saveButton.setEnabled(true);
             } else {
                 saveButton.setEnabled(false);
             }
         }
     } 
     
     public void removeUpdate(DocumentEvent e) {
         if (saveButton != null) {
             if (isChanged(selectedStationIndex)) {
                 saveButton.setEnabled(true);
             } else {
                 saveButton.setEnabled(false);
             }
         }
     }       
 
     private void updateAntennaTypeComboBox() {
         if (antennaTypeComboBox.getSelectedIndex() == 0) {  // no antenna
             antennaHeightLabel.setVisible(false);
             antennaHeightTextField.setText("");
             antennaHeightTextField.setVisible(false);
             terrainHeightLabel.setVisible(false);
             terrainHeightTextField.setText("");
             terrainHeightTextField.setVisible(false);
             headingLabel.setVisible(false);
             headingTextField.setText("");
             headingTextField.setVisible(false);
             fieldOfViewAngleLabel.setVisible(false);
             fieldOfViewAngleTextField.setText("");
             fieldOfViewAngleTextField.setVisible(false);
             gainLabel.setVisible(false);
             gainTextField.setText("");
             gainTextField.setVisible(false); 
         } else if (antennaTypeComboBox.getSelectedIndex() == 1) {  // omnidirectional
             antennaHeightLabel.setVisible(true);
             antennaHeightTextField.setVisible(true);
             terrainHeightLabel.setVisible(true);
             terrainHeightTextField.setVisible(true);
             headingLabel.setVisible(false);
             headingTextField.setText("");
             headingTextField.setVisible(false);
             fieldOfViewAngleLabel.setVisible(false);
             fieldOfViewAngleTextField.setText("");
             fieldOfViewAngleTextField.setVisible(false);
             gainLabel.setVisible(false);
             gainTextField.setText("");
             gainTextField.setVisible(false);                        
         } else if (antennaTypeComboBox.getSelectedIndex() == 2) {  // directional
             antennaHeightLabel.setVisible(true);
             antennaHeightTextField.setVisible(true);
             terrainHeightLabel.setVisible(true);
             terrainHeightTextField.setVisible(true);
             headingLabel.setVisible(true);
             headingTextField.setVisible(true);
             fieldOfViewAngleLabel.setVisible(true);
             fieldOfViewAngleTextField.setVisible(true);
             gainLabel.setVisible(true);
             gainTextField.setVisible(true);
         } 
     }    
 
     /** 
      * Loads a station.
      *
      * @param stationIndex Index of the station to be loaded
      */    
     private void loadStation(int stationIndex) {
         
         noListeners = true;
         
         try {
             data = XMLImporter.readXML(new File("data/eavdam_data.xml"));
         } catch (MalformedURLException ex) {
             System.out.println(ex.getMessage());
         } catch (JAXBException ex) {
             System.out.println(ex.getMessage());
         }
 
         AISFixedStationData[] stations = null;
         if (data != null) {
             stations = data.getStations();
         }
 
         if (stations != null && stations.length > 0 && stationIndex >= stations.length) {
             return;
         }
         
         if (initiallySelectedStationName != null) {
             for (int i=0; i< stations.length; i++) {
                 if (stations[i].getStationName().equals(initiallySelectedStationName)) {
                     stationIndex = i;
                     selectedStationIndex = i;
                     break;
                 }
             }
            initiallySelectedStationName = null;
         }
         
         if (stationIndex < stations.length) {
             AISFixedStationData station = stations[stationIndex];
             
             selectStationComboBox.removeAllItems();
             for (int i=0; i<stations.length; i++) {                    
                 AISFixedStationData temp = stations[i];
                 if (temp.getStationName() != null) {
                     selectStationComboBox.addItem(temp.getStationName());
                 } else {                        
                     selectStationComboBox.addItem("Undefined");
                 }
             }
             selectStationComboBox.setSelectedIndex(stationIndex);
             
             if (station.getStationName() != null) {
                 stationNameTextField.setText(station.getStationName());
             } else {
                 stationNameTextField.setText("Undefined");                
             }
             if (station.getStationType() != null) {
                 if (station.getStationType() == AISFixedStationType.BASESTATION) {
                     stationTypeComboBox.setSelectedIndex(0);
                 } else if (station.getStationType() == AISFixedStationType.REPEATER) {
                     stationTypeComboBox.setSelectedIndex(1);
                 } else if (station.getStationType() == AISFixedStationType.RECEIVER) {
                     stationTypeComboBox.setSelectedIndex(2);
                 } else if (station.getStationType() == AISFixedStationType.ATON) {
                     stationTypeComboBox.setSelectedIndex(3);
                 }
             }
             if (!Double.isNaN(station.getLat())) {                
                 latitudeTextField.setText(String.valueOf(station.getLat()));                         
             }
             if (!Double.isNaN(station.getLon())) {  
                 longitudeTextField.setText(String.valueOf(station.getLon()));
             }
             if (station.getMmsi() != null) {
                 mmsiNumberTextField.setText(station.getMmsi());
             }
             if (station.getTransmissionPower() != null) {
                 transmissionPowerTextField.setText(station.getTransmissionPower().toString());
             }
             if (station.getStatus() != null) {
                 if (station.getStatus() == AISFixedStationStatus.OPERATIVE) {
                     stationStatusComboBox.setSelectedIndex(0);
                 } else if (station.getStatus() == AISFixedStationStatus.INOPERATIVE) {
                     stationStatusComboBox.setSelectedIndex(1);
                 }
             }
             if (station.getAntenna() != null) {
                 Antenna antenna = station.getAntenna();
                 if (antenna.getAntennaType() != null) {
                     if (antenna.getAntennaType() == AntennaType.OMNIDIRECTIONAL) {
                         antennaTypeComboBox.setSelectedIndex(1);
                     } else if (antenna.getAntennaType() == AntennaType.DIRECTIONAL) {
                         antennaTypeComboBox.setSelectedIndex(2);
                     }
                } else {
                    antennaTypeComboBox.setSelectedIndex(0);
                 }
                 if (!Double.isNaN(antenna.getAntennaHeight())) {
                     antennaHeightTextField.setText(String.valueOf(antenna.getAntennaHeight()));
                 }
                 if (!Double.isNaN(antenna.getTerrainHeight())) {
                     terrainHeightTextField.setText(String.valueOf(antenna.getTerrainHeight()));
                 }
                 if (antenna.getHeading() != null) {
                     headingTextField.setText(antenna.getHeading().toString());
                 }
                 if (antenna.getFieldOfViewAngle() != null) {
                     fieldOfViewAngleTextField.setText(antenna.getFieldOfViewAngle().toString());
                 }
                 if (antenna.getGain() != null) {
                     gainTextField.setText(antenna.getGain().toString());
                 }                    
            } else {           
                antennaTypeComboBox.setSelectedIndex(0);
            }
             if (station.getDescription() != null) {
                 additionalInformationJTextArea.setText(station.getDescription());
             }            
             updateAntennaTypeComboBox();           
         }
         noListeners = false;       
     }
     
     /**
      * Saves station's data.
      *
      * @param stationIndex  Index of the station in the stations list
      * @return True if the data was saved or false if it was not
      */
     private boolean saveStation(int stationIndex) {
 
         if (data == null) {
             data = new EAVDAMData();
         }
         AISFixedStationData[] stations = data.getStations();
         for (int i=0; i<stations.length; i++) {
             if (i != stationIndex && stations[i].getStationName().equals(stationNameTextField.getText().trim())) {
                 JOptionPane.showMessageDialog(dialog, "A station with the given name already exists. " +
                     "Please, select another name for the station.");                 
                 return false;
             }
         }
         if (latitudeTextField.getText().trim().isEmpty()) {
             JOptionPane.showMessageDialog(dialog, "Latitude is mandatory.");
             return false;
         } else {
             if (!latitudeTextField.getText().trim().isEmpty()) {
                 try {
                     Double.parseDouble(latitudeTextField.getText().trim());                    
                 } catch (NumberFormatException ex) {
                     JOptionPane.showMessageDialog(dialog, "Latitude is not a valid number.");
                     return false;
                 }
             }
         }            
         if (longitudeTextField.getText().trim().isEmpty()) {          
             JOptionPane.showMessageDialog(dialog, "Longitude is mandatory.");                    
             return false;
         } else {                                                        
             if (!longitudeTextField.getText().trim().isEmpty()) {
                 try {
                     Double.parseDouble(longitudeTextField.getText().trim());
                 } catch (NumberFormatException ex) {
                     JOptionPane.showMessageDialog(dialog, "Longitude is not a valid number.");
                     return false;
                 }
             }
         }                    
         try {
             if (!transmissionPowerTextField.getText().trim().isEmpty()) {
                 Double.parseDouble(transmissionPowerTextField.getText().trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(dialog, "Transmission power is not a valid number.");
             return false;
         }   
         if (antennaTypeComboBox.getSelectedIndex() == 1 &&
                 (antennaHeightTextField.getText().trim().isEmpty() ||
                 terrainHeightTextField.getText().trim().isEmpty())) {
             JOptionPane.showMessageDialog(dialog, "Antenna height and terrain height must both be given.");
             return false;
         }
         if (antennaTypeComboBox.getSelectedIndex() == 2 &&
                 (antennaHeightTextField.getText().trim().isEmpty() ||
                 terrainHeightTextField.getText().trim().isEmpty() ||
                 headingTextField.getText().trim().isEmpty() ||
                 fieldOfViewAngleTextField.getText().trim().isEmpty() ||
                 gainTextField.getText().trim().isEmpty())) {
             JOptionPane.showMessageDialog(dialog, "Antenna height, terrain height, heading, field of view angle and gain must all be given.");
             return false;
         }
         try {
             if (!antennaHeightTextField.getText().trim().isEmpty()) {
                 Double.parseDouble(antennaHeightTextField.getText().trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(dialog, "Antenna height is not a valid number.");                     
             return false;
         }  
         try {
             if (!terrainHeightTextField.getText().trim().isEmpty()) {
                 Double.parseDouble(terrainHeightTextField.getText().trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(dialog, "Terrain height is not a valid number.");
             return false;            
         }                                 
         try {
             if (!headingTextField.getText().trim().isEmpty()) {
                 Integer.parseInt(headingTextField.getText().trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(dialog, "Heading is not a valid integer.");
             return false;
         }  
         try {
             if (!fieldOfViewAngleTextField.getText().trim().isEmpty()) {
                 Integer.parseInt(fieldOfViewAngleTextField.getText().trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(dialog, "Field of view angle is not a valid integer.");                        
             return false;
         }      
         try {
             if (!gainTextField.getText().trim().isEmpty()) {
                 Double.parseDouble(gainTextField.getText().trim());
             }
         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(dialog, "Gain is not a valid number.");
             return false;
         }                          
     
         AISFixedStationData station = new AISFixedStationData();
         if (stations != null && stations.length > 0 && stationIndex >=stations.length) {
             return false;
         } else if (stations != null && stations.length > 0) {
             station = stations[stationIndex];
         }
 
         try {
             station.setStationName(stationNameTextField.getText().trim());
             if (stationTypeComboBox.getSelectedIndex() == 0) {
                 station.setStationType(AISFixedStationType.BASESTATION);
             } else if (stationTypeComboBox.getSelectedIndex() == 1) {
                 station.setStationType(AISFixedStationType.REPEATER); 
             } else if (stationTypeComboBox.getSelectedIndex() == 2) {
                 station.setStationType(AISFixedStationType.RECEIVER); 
             } else if (stationTypeComboBox.getSelectedIndex() == 3) {
                 station.setStationType(AISFixedStationType.ATON); 
             }  
             station.setLat(new Double(latitudeTextField.getText().trim()).doubleValue());                                
             station.setLon(new Double(longitudeTextField.getText().trim()).doubleValue());  
             if (mmsiNumberTextField.getText().trim().isEmpty()) {
                 station.setMmsi(null);
             } else {
                 station.setMmsi(mmsiNumberTextField.getText().trim());
             }
             if (transmissionPowerTextField.getText().trim().isEmpty()) {
                 station.setTransmissionPower(null);
             } else {
                 station.setTransmissionPower(new Double(transmissionPowerTextField.getText().trim()));
             }
             if (stationStatusComboBox.getSelectedIndex() == 0) {
                 station.setStatus(AISFixedStationStatus.OPERATIVE);
             } else if (stationStatusComboBox.getSelectedIndex() == 1) {
                 station.setStatus(AISFixedStationStatus.INOPERATIVE);
             }
             Antenna antenna = station.getAntenna();
             if (antennaTypeComboBox.getSelectedIndex() == 0) {
                 station.setAntenna(null);
             } else if (antennaTypeComboBox.getSelectedIndex() == 1) {
                 if (antenna == null) {
                     antenna = new Antenna();
                 }
                 antenna.setAntennaType(AntennaType.OMNIDIRECTIONAL);                    
             } else if (antennaTypeComboBox.getSelectedIndex() == 2) {
                 if (antenna == null) {
                     antenna = new Antenna();
                 }
                 antenna.setAntennaType(AntennaType.DIRECTIONAL);
             }
             if (antennaTypeComboBox.getSelectedIndex() == 1 ||
                     antennaTypeComboBox.getSelectedIndex() == 2) {
                 if (!antennaHeightTextField.getText().trim().isEmpty()) {
                     antenna.setAntennaHeight(new Double(antennaHeightTextField.getText().trim()).doubleValue());
                 }
                 if (!terrainHeightTextField.getText().trim().isEmpty()) {
                     antenna.setTerrainHeight(new Double(terrainHeightTextField.getText().trim()).doubleValue());
                 }
             }
             if (antennaTypeComboBox.getSelectedIndex() == 2) {
                 if (headingTextField.getText().trim().isEmpty()) {                        
                     antenna.setHeading(null);
                 } else {
                     antenna.setHeading(new Integer(headingTextField.getText().trim()));
                 }
                 if (fieldOfViewAngleTextField.getText().trim().isEmpty()) { 
                     antenna.setFieldOfViewAngle(null);
                 } else {
                     antenna.setFieldOfViewAngle(new Integer(fieldOfViewAngleTextField.getText().trim()));
                 }
                 if (gainTextField.getText().trim().isEmpty()) {             
                     antenna.setGain(null);
                 } else {
                     antenna.setGain(new Double(gainTextField.getText().trim()));
                 }
             }
             if (antennaTypeComboBox.getSelectedIndex() == 1 ||
                     antennaTypeComboBox.getSelectedIndex() == 2) {
                 station.setAntenna(antenna);
             }
             if (additionalInformationJTextArea.getText().trim().isEmpty()) {
                 station.setDescription(null);
             } else {
                 station.setDescription(additionalInformationJTextArea.getText().trim());
             }
         } catch (IllegalArgumentException e) {
             JOptionPane.showMessageDialog(dialog, e.getMessage());              
             return false;
         }
                         
         stations[stationIndex] = station;
         List<AISFixedStationData> stationList = new ArrayList<AISFixedStationData>(Arrays.asList(stations));
         data.setStations(stationList);     
         saveData(data);    
         
         return true;   
     }
 
     /** 
      * Deletes a station.
      *
      * @param stationIndex  Index of the station to be deleted
      */
     private void deleteStation(int stationIndex) {        
         if (data != null) {
             AISFixedStationData[] stations = data.getStations();
             if (stations != null && stationIndex < stations.length) {                
                 AISFixedStationData[] result = new AISFixedStationData[stations.length-1];
                 if (stationIndex > 0) {
                     System.arraycopy(stations, 0, result, 0, stationIndex);
                 }
                 if (stationIndex+1 < stations.length) {
                     System.arraycopy(stations, stationIndex+1, result, stationIndex, stations.length-1-stationIndex);
                 }                
                 List<AISFixedStationData> stationList = new ArrayList<AISFixedStationData>(Arrays.asList(result));               
                 data.setStations(stationList);
                 saveData(data);
             }
         }
     }
     
     /** 
      * Saves data to XML file.
      *
      * @param data  Data to be saved
      */    
     private void saveData(EAVDAMData data) {
         try {
             File file = new File("data");
             if (!file.exists()) {
                 file.mkdir();
             }
             XMLExporter.writeXML(data, new File("data/eavdam_data.xml"));
         } catch (FileNotFoundException ex) {
             System.out.println(ex.getMessage());
         } catch (JAXBException ex) {
             System.out.println(ex.getMessage());
         }            
     }
 
     /** 
      * Checks whether the form fields have changed.
      *
      * @param stationIndex Index of the station
      * @return  True if the fields have changed, false if not
      */
     private boolean isChanged(int stationIndex) {
 
         if (noListeners) {
             return false;
         }
 
         if (data == null) {
             return true;
         }
 
         AISFixedStationData station = new AISFixedStationData();
         AISFixedStationData[] stations = data.getStations();
                 
         if (stations != null && stations.length > 0 && stationIndex >= stations.length) {
             return true;
         } else if (stations != null && stations.length > 0) {
             station = stations[stationIndex];
         }                                
 
         if (station.getStationName() == null && !stationNameTextField.getText().isEmpty()) {
             return true;
         }         
         if (!station.getStationName().equals(stationNameTextField.getText())) {
             return true;
         }
         if (station.getStationType() == null) {
             return true;
         }
         if (station.getStationType() == AISFixedStationType.BASESTATION && stationTypeComboBox.getSelectedIndex() != 0) {
             return true;
         }
         if (station.getStationType() == AISFixedStationType.REPEATER && stationTypeComboBox.getSelectedIndex() != 1) {
             return true;
         }
         if (station.getStationType() == AISFixedStationType.RECEIVER && stationTypeComboBox.getSelectedIndex() != 2) {
             return true;
         }
         if (station.getStationType() == AISFixedStationType.ATON && stationTypeComboBox.getSelectedIndex() != 3) {
             return true;
         }  
         try {
             if (Double.isNaN(station.getLat()) && !latitudeTextField.getText().isEmpty()) {
                 return true;
             }
             if (!Double.isNaN(station.getLat()) && station.getLat() != (new Double(latitudeTextField.getText()).doubleValue())) {
                 return true;
             }
         } catch (NumberFormatException ex) {
             return true;
         }
         try {
             if (Double.isNaN(station.getLon()) && !longitudeTextField.getText().isEmpty()) {
                 return true;
             }
             if (!Double.isNaN(station.getLon()) && station.getLon() != (new Double(longitudeTextField.getText()).doubleValue())) {
                 return true;
             }
         } catch (NumberFormatException ex) {
             return true;
         }
         if (station.getMmsi() == null && !mmsiNumberTextField.getText().isEmpty()) {
             return true;
         }         
         if (station.getMmsi() != null && !station.getMmsi().equals(mmsiNumberTextField.getText())) {
             return true;
         }
         if (station.getTransmissionPower() == null && !transmissionPowerTextField.getText().isEmpty()) {
             return true;
         }
         try {       
             if (station.getTransmissionPower() != null && !station.getTransmissionPower().equals(new Double(transmissionPowerTextField.getText()))) {
                 return true;
             }
         } catch (NumberFormatException ex) {
                 return true;          
         }
         if (station.getStatus() == null) {
             return true;
         }
         if (station.getStatus() == AISFixedStationStatus.OPERATIVE && stationStatusComboBox.getSelectedIndex() != 0) {
             return true;
         }  
         if (station.getStatus() == AISFixedStationStatus.INOPERATIVE && stationStatusComboBox.getSelectedIndex() != 1) {
             return true;
         }  
         Antenna antenna = station.getAntenna();
         if (antenna == null) {
             antenna = new Antenna();
         }
         if (antenna.getAntennaType() == null && antennaTypeComboBox.getSelectedIndex() != 0) {
             return true;
         }
         if (antenna.getAntennaType() == AntennaType.OMNIDIRECTIONAL && antennaTypeComboBox.getSelectedIndex() != 1) {
             return true;
         }
         if (antenna.getAntennaType() == AntennaType.DIRECTIONAL && antennaTypeComboBox.getSelectedIndex() != 2) {
             return true;
         }
         if (Double.isNaN(antenna.getAntennaHeight()) && !antennaHeightTextField.getText().isEmpty()) {
             return true;
         }
         try {       
             if (antenna.getAntennaHeight() != new Double(antennaHeightTextField.getText()).doubleValue()) {
                 return true;
             }
         } catch (NumberFormatException ex) {
                 return true;
         }        
         if (Double.isNaN(antenna.getTerrainHeight()) && !terrainHeightTextField.getText().isEmpty()) {
             return true;
         }
         try {       
             if (antenna.getTerrainHeight() != new Double(terrainHeightTextField.getText()).doubleValue()) {
                 return true;
             }
         } catch (NumberFormatException ex) {
             return true;
         }                         
         if (antenna.getAntennaType() == AntennaType.DIRECTIONAL) {
             if (antenna.getHeading() == null && !headingTextField.getText().isEmpty()) {
                 return true;
             }
             try {       
                 if (antenna.getHeading() != null && !antenna.getHeading().equals(new Integer(headingTextField.getText()))) {
                     return true;
                 }
             } catch (NumberFormatException ex) {
                 return true;
             }              
             if (antenna.getFieldOfViewAngle() == null && !fieldOfViewAngleTextField.getText().isEmpty()) {
                 return true;
             }
             try {       
                 if (antenna.getFieldOfViewAngle() != null && !antenna.getFieldOfViewAngle().equals(new Integer(fieldOfViewAngleTextField.getText()))) {
                     return true;
                 }
             } catch (NumberFormatException ex) {
                 return true;
             }         
             if (antenna.getGain() == null && !gainTextField.getText().isEmpty()) {
                 return true;
             }
             try {       
                 if (antenna.getGain() != null && !antenna.getGain().equals(new Integer(gainTextField.getText()))) {
                     return true;
                 }
             } catch (NumberFormatException ex) {
                 return true;
             }
         }
         if (station.getDescription() == null && !additionalInformationJTextArea.getText().isEmpty()) {
             return true;
         }         
         if (station.getDescription() != null && !station.getDescription().equals(additionalInformationJTextArea.getText())) {
             return true;
         }
         return false;
     }
 
 }
