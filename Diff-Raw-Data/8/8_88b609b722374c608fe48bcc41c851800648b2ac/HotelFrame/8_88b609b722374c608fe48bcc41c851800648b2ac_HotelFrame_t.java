 package suncertify.ui;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import suncertify.db.DataDBAccess;
 import suncertify.util.ApplicationMode;
 import suncertify.util.PropertyManager;
 
 /**
  *
  * @author William Brosnan
  * 
  * This class holds the code for creating the main GUI
  */
 public class HotelFrame extends JFrame {
     
     /*
      * Adding a logger instance for logging and debugging purposes.
      */
     private Logger logger = Logger.getLogger("suncertify.ui");   
     /**
      * This JPanel will contain the search fields and JButtons   
      */
     private JPanel topPanel; 
     /**
      * This JPanel will contain JTable loaded with records from the database
      */
     private JPanel tablePanel;
     /**
      * This JPanel will contain the booking field and JButtons 
      */
     private JPanel bottomPanel;
     /*
      * JLabel for hotel name search term
      */
     private JLabel nameLabel;
     /*
      * JLabel for location search term
      */
     private JLabel locationLabel;
     /*
      * JTextField to enter the hotel name search term
      */
     private JTextField nameField;
     /*
      * JTextField to enter the location search term
      */
     private JTextField locationField;
     /*
      * JButton to start search based on input in JTextFields
      */
     private JButton searchButton;
     /*
      * JButton to load all records in JTable
      */
     private JButton loadButton;
     /**
      * JLabel for customer ID
      */
     private JLabel custIDLabel;
     /**
      * JTextField to hold the customer ID
      */
     private JTextField custIDField;
     /**
      * JButton for reserving a room
      */
     private JButton reserveButton;
     /**
      * ArrayList holding the records for each room
      */
     private String[] roomList;
     /**
      * JMenuBar to hold the file menu to quit and the help menu
      */
     private JMenuBar menuBar = new JMenuBar();
     /**
      * Enumeration to know in what mode to start in
      */
     private ApplicationMode applicationMode;
     /**
      * A controller to fulfill the controller function of the MVC pattern
      */
     private HotelFrameController controller;
     /**
      * This is the <code>JTable</code> that will hold the records
      */
     private JTable hotelTable;
     /**
      * The Custom Table Model created the hold the <code>JTable</code> records
      * It extends <code>AbstractTableModel</code>
      */
     private RoomTableModel tableModel;
     /**
      * PropertyManager instance to get properties from property file
      */
     private PropertyManager propManager = PropertyManager.getInstance();
     /**
      * String to hold the location of the database got from the 
      * <code>ConfigurationFrame</code> dialog.
      */
     private String dbLocation;
     
     
     
     /**
      * Constructor for the JFrame, the JPanels that make up the GUI are added
      * the the JFrame here
      * @throws FileNotFoundException
      * @throws IOException 
      */
     public HotelFrame(String[] args) {
         setTitle("URLyBird Hotel User Interface");
         setSize(1000,800);
         setResizable(false);
         setLocationRelativeTo(null);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         
         if (args.length == 0) {
             applicationMode = ApplicationMode.NETWORK;
         }
         else {
             applicationMode = ApplicationMode.ALONE;
         }
         
         ConfigurationDialog configurationFrame = new ConfigurationDialog(applicationMode);
         configurationFrame.setVisible(true);
         String dbPath = configurationFrame.getDBLocation();
        propManager.setProperty("dbPath", dbPath);
         this.dbLocation = propManager.getProperty(dbPath);
         logger.log(Level.INFO, "Database location is: " + dbLocation);
         System.out.println("Database location is: " + dbLocation);
         
         controller = new HotelFrameController(ApplicationMode.ALONE, dbLocation 
                 + DataDBAccess.DATABASE_NAME, "5005");
         JMenu fileMenu = new JMenu("File");
         JMenuItem quitMenuItem = new JMenuItem("Quit");
         quitMenuItem.addActionListener(new QuitApplication());
         fileMenu.add(quitMenuItem);
         JMenu helpMenu = new JMenu("Help");
         menuBar.add(fileMenu);
         menuBar.add(helpMenu);
         this.setJMenuBar(menuBar);
         topPanel = loadSearchPanel();
         tablePanel = loadTablePanel();
         bottomPanel = loadBookingPanel();
         add(topPanel, BorderLayout.NORTH);
         add(tablePanel, BorderLayout.CENTER);
         add(bottomPanel, BorderLayout.SOUTH);
         //pack();
     }
     
     /**
      * Used to separate the GUI construction code out into manageable functions
      * instead of having all the code in the constructor
      * @return the JPanel that contains the search fields and buttons
      */
     private JPanel loadSearchPanel() {
         JPanel searchPanel = new JPanel();
         searchPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
         nameLabel = new JLabel("Name");
         searchPanel.add(nameLabel);
         nameField = new JTextField(30);
         searchPanel.add(nameField);
         locationLabel = new JLabel("Location");
         searchPanel.add(locationLabel);
         locationField = new JTextField(30);
         searchPanel.add(locationField);
         searchButton = new JButton("Search");
         searchButton.addActionListener(new SearchRoom());
         searchPanel.add(searchButton);
         loadButton = new JButton("Load Table");
         loadButton.addActionListener(new LoadRooms());
         searchPanel.add(loadButton);
         return searchPanel;
     }
     
     /**
      * Used to separate the GUI construction code out into manageable functions
      * instead of having all the code in the constructor
      * @return the JPanel that holds the JTable populated with records from the
      * database
      * @throws FileNotFoundException
      * @throws IOException 
      */
     private JPanel loadTablePanel() {
         JPanel tablePanel = new JPanel(new BorderLayout());
         hotelTable = loadTable();
         tablePanel.add(new JScrollPane(hotelTable));
         return tablePanel;
     }
     
     /**
      * The records from the database are created in the RoomTableModel which 
      * the JTable is using
      * @return the JTable populated with records
      * @throws FileNotFoundException
      * @throws IOException 
      */
     private JTable loadTable() {
         tableModel = this.controller.getAllRooms();
         JTable table = new JTable(tableModel);
         return table;
     }
     
     /**
      * Refreshes the data in the <code>JTable</code> by re-setting the 
      * <code>RoomTableModel</code>
      */
     private void refreshTable() {
         this.hotelTable.setModel(this.tableModel);       
     }
     
     private JPanel loadBookingPanel() {
         JPanel bookingPanel = new JPanel();
         bookingPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
         custIDLabel = new JLabel("Select a record and press Reserve Room button "
                 + "to reserve");
         bookingPanel.add(custIDLabel);
         //custIDField = new JTextField(30);
         //bookingPanel.add(custIDField);
         reserveButton = new JButton("Reserve Room");
         reserveButton.addActionListener(new ReserveRoom());
         bookingPanel.add(reserveButton);
         return bookingPanel;
     }
     
     /**
      * Private class used to listen when the Search button is fired
      * Search will use the entries added in hotel name and location fields
      * as search terms
      */
     private class SearchRoom implements ActionListener {
     
         @Override
         public void actionPerformed(ActionEvent e) {
             String hotelNameCriteria = nameField.getText();
             String locationCriteria = locationField.getText();
             try {                
                 if (hotelNameCriteria.equals("") && locationCriteria.equals("")) {
                     tableModel = controller.getAllRooms();
                     refreshTable();
                 }
                 else {
                     tableModel = controller.getRoomsByCriteria(hotelNameCriteria, locationCriteria);
                     refreshTable();
                 }
             } catch (Exception ex) {
                 logger.log(Level.SEVERE, ex.getMessage(), ex);
                 System.err.println("Search problem found: " + ex.getMessage());
             }
             nameField.setText("");
             locationField.setText("");
         }
     }
     
     /**
      * Private class used to listen when the Load Table button is fired
      * This loads all valid records from the DB
      */
     private class LoadRooms implements ActionListener {
     
         @Override
         public void actionPerformed(ActionEvent e) {
             try {
                 tableModel = controller.getAllRooms();
                 refreshTable();
             } catch (Exception ex) {
                 logger.log(Level.SEVERE, ex.getMessage(), ex);
                 System.err.println("Load all rooms problem found: " + ex.getMessage());
             }            
         }
     }
     
     /**
      * Private class used to listen when the Reserve Room button is fired
      * This reserves a room in the DB
      */
     private class ReserveRoom implements ActionListener {
     
         @Override
         public void actionPerformed(ActionEvent e) {
             int recordRow = hotelTable.getSelectedRow();
             String csrNumber = "";
             if (recordRow < 0) {
                 JOptionPane.showMessageDialog(bottomPanel, "Please select a row");
             }
             else {
                 do {
                 csrNumber = (String) JOptionPane.showInputDialog
                         (bottomPanel, "Enter CSR number of 8 digits");
                 } while (csrNumber.length() != 8);
             }
             try {
                 controller.reserveRoom(recordRow, csrNumber);
                 tableModel = controller.getAllRooms();
                 refreshTable();
             } catch(Exception ex) {
                 logger.log(Level.SEVERE, ex.getMessage(), ex);
                 System.err.println("Reserve room problem found: " + ex.getMessage());
             }
         }
     }    
 }
