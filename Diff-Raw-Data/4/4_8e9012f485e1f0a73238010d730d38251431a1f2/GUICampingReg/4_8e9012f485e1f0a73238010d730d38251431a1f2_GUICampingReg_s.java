 package package1;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.swing.*;
 
 import VariableInputApi.*;
 
 public class GUICampingReg extends JFrame implements ActionListener, 
 													 MouseListener {
 
 	/** the serial version UID */
 	private static final long serialVersionUID = 1L;
 
 	/** JMenu for file */
 	private JMenu fileMenu;
 
 	/** JMenu for check in menu */
 	private JMenu checkInMenu;
 
 	/** JMenu for check out menu */
 	private JMenu checkOutMenu;
 
 	/** JMenu for status */
 	private JMenu statusMenu;
 
 	/** JMenu Item for checking status */
 	private JMenuItem statusMenuItem;
 
 	/** JMenu Item for saving serializable file */
 	private JMenuItem saveS;
 
 	/** JMenu Item for opening serializable file */
 	private JMenuItem openS;
 
 	/** JMenu Item for saving text file */
 	private JMenuItem saveT;
 
 	/** JMenu Item for opening text file */
 	private JMenuItem openT;
 
 	/** JMenu Item for exiting */
 	private JMenuItem quit;
 
 	/** JMenu Item for checking in a tent */
 	private JMenuItem checkInTent;
 
 	/** JMenu Item for checking in a RV */
 	private JMenuItem checkInRV;
 
 	/** JMenu Item for checking out */
 	private JMenuItem checkOut;
 
 	/** JMenu bar to hold the menus */
 	private JMenuBar menus;
 
 	/** JTable for holding the information */
 	private JTable table;
 
 	/** instance of SiteModel */
 	private SiteModel siteTableModel;
 
 	/** JScrollPanel to allow for scrolling */
 	private JScrollPane scrollPane;
 
 	/** Default Name */
 	private final String DEFAULT_NAME;
 
 	/** Default Site Number */
 	private final int DEFAULT_SITE_NUMBER;
 
 	/** Default Date */
 	private final String DEFAULT_DATE;
 
 	/** Default number of days staying */
 	private final int DEFAULT_DAYS_STAYING;
 
 	/** Default power use  */
 	private final int DEFAULT_POWER_USED;
 
 	/** Default number of days staying */
 	private final int DEFAULT_TENTERS;
 
 	/** Maximum number of sites */
 	private final int MAX_NUMBER_OF_SITES;
 
 	/** Represents the sites taken */
 	private Boolean[] sitesTaken;
 
 //	/** Cost for the stay */
 //	private double[] costs;
 
 	/** Decimal Formatter */
 	private static final DecimalFormat DECIMAL_FORMAT = 
 			new DecimalFormat("#0.00");
 
 	/** Sites being used */
 	private int usedSites;
 
 	/** SimpleDate Formater */
 	public static final SimpleDateFormat SIMPLE_FORMAT = 
 			new SimpleDateFormat("MM/dd/yyyy");
 
 	/******************************************************************
 	 * Sets up the GUI
 	 *****************************************************************/
 	public GUICampingReg(){
 		DEFAULT_NAME = "John Doe";
 		DEFAULT_SITE_NUMBER = 1;
 		DEFAULT_DATE = "10/15/2013";
 		DEFAULT_DAYS_STAYING = 1;
 		DEFAULT_POWER_USED = 30;
 		DEFAULT_TENTERS = 1;
 
 		MAX_NUMBER_OF_SITES = 5;
 		sitesTaken = new Boolean[MAX_NUMBER_OF_SITES];
 
 		clearAllSites();
 
 		// Instantiate the menus and menu items
 		fileMenu = new JMenu("File:");
 		checkInMenu = new JMenu("Check In:");
 		checkOutMenu = new JMenu("Check Out:");
 		statusMenu = new JMenu("Status:");
 		statusMenuItem = new JMenuItem("Check Status");
 		saveS = new JMenuItem("Save Serializable");
 		openS = new JMenuItem("Open Serializable");
 		openT = new JMenuItem("Open Text");		
 		saveT = new JMenuItem("Save Text");
 		quit = new JMenuItem("Quit");
 		checkInTent = new JMenuItem("Check In Tent");
 		checkInRV = new JMenuItem("Check In RV");
 		checkOut = new JMenuItem("Check Out");
 		menus = new JMenuBar();
 
 		// add the menus and menu items to the frame
 		menus.add(fileMenu);
 		fileMenu.add(saveS);
 		fileMenu.add(openS);
 		fileMenu.add(saveT);
 		fileMenu.add(openT);
 		fileMenu.add(quit);
 		menus.add(checkInMenu);
 		checkInMenu.add(checkInTent);
 		checkInMenu.add(checkInRV);
 		menus.add(checkOutMenu);
 		checkOutMenu.add(checkOut);
 		menus.add(statusMenu);
 		statusMenu.add(statusMenuItem);
 
 		// add ActionListeners
 		quit.addActionListener(this);
 		openT.addActionListener(this);
 		saveT.addActionListener(this);
 		openS.addActionListener(this);
 		saveS.addActionListener(this);
 		checkInTent.addActionListener(this);
 		checkInRV.addActionListener(this);
 		checkOut.addActionListener(this);
 		statusMenuItem.addActionListener(this);
 
 		// set the menu bar
 		setJMenuBar(menus);
 
 		// instantiate the SiteModel and add it to the frame
 		siteTableModel = new SiteModel();
 		table = new JTable(siteTableModel);
 		table.getTableHeader().addMouseListener(this);
 		table.getTableHeader().setReorderingAllowed(false);
 		scrollPane = new JScrollPane(table);
 		add(scrollPane);
 
 		// set the default close operation
 		setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
 
 		setSize(700,300);
 		setVisible(true);
 	}
 	
 	/******************************************************************
 	 * Clears all the sites of their site number so a site can be reserved
 	 *****************************************************************/
 	private void clearAllSites() {
 		for (int i = 0; i<MAX_NUMBER_OF_SITES; i++) {
 			sitesTaken[i]= false;
 		}
 		usedSites=0;
 	}
 
 	/******************************************************************
 	 * Main method to run the GUI
 	 * @param args Command Line Arguments - Unused
 	 *****************************************************************/
 	public static void main(String[] args){
 		new GUICampingReg();
 	}
 
 	/******************************************************************
 	 * actionPerformed method for the buttons
 	 * @param event listens for buttons to be clicked
 	 *****************************************************************/
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		JComponent comp = (JComponent) event.getSource();
 		
 		// checks the status of the camp
 		if(comp == statusMenuItem){
 			// instantiate a new campStatus
 			CampFullStatus campStatus = new CampFullStatus();
 			// labels to be sent to the variable input panel
 			String[] labelsStatus = {"Enter a date to check"};
 			// new variable input panel
 			VarInputPanel vS = new VarInputPanel(labelsStatus, DEFAULT_DATE);
 			int rStatus;
 			Date checkOut = new Date();
 
 			// used for if it was a successful parse
 			boolean success = true;
 			// used to see if the OK button was pushed
 			boolean btnOption;
 
 			do{
 				// shows the variable input dialog
 				rStatus = JOptionPane.showConfirmDialog(null, vS, "Check Status" 
 					, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
 
 				btnOption = (rStatus == JOptionPane.OK_OPTION);
 				// if the variables sent in match what was given and hit ok
 				if(vS.doUpdatedVarsMatchInput() && btnOption) {
 
 					Object[] varResult = vS.getUpdatedVars();
 					success = false;
 					try {
 						// parse the date given using SimpleDateFormat
 						checkOut = SIMPLE_FORMAT.parse((String)varResult[0]);
 
 						BetterGregorianCalendar date = 
 								new BetterGregorianCalendar();
 
 						date.setTime(checkOut);
 						// save off the table 
 						siteTableModel.saveAsText("CampStatus");
 						// send the date to campStatus
 						campStatus.checkStatus(date);
 					} catch (Exception e) {
 						JOptionPane.showMessageDialog(null, 
 								"Enter a correct date (MM/DD/YYYY)");
 						success = true;
 					}
 				// checks to make sure OK was pushed
 				} else if (btnOption){
 					JOptionPane.showMessageDialog(null, "Date out of range. " +
 							" Please check your inputs.");
 				}
 
 			}while(success && btnOption);
 		}
 		
 		// checks to see if quit was clicked
 		if(comp == quit){
 			System.exit(1);
 		}
 		
 		// opens a serializable file
 		if(comp == openS){
 			siteTableModel.loadDatabase(fileOperations());
 		}
 		
 		// saves a serialiazable file
 		if(comp == saveS){
 			siteTableModel.saveDatabase(fileOperations());
 		}
 		
 		// opens a text file
 		if(comp == openT){
 			siteTableModel.loadFromText(fileOperations());	  
 		}
 		
 		// saves a file as text
 		if(comp == saveT){
 			siteTableModel.saveAsText(fileOperations());
 		}
 		
 		//checks in a tent object
 		if(comp == checkInTent){
 			// labels to be sent to variable input panel
 			String[] labelsTent = {"Name Reserving:", "Site Number:",
 					"Occupied On:", "Number of Tenters:", "Days Staying:"};
 			// creates a new input panel with the labels and default values
 			VarInputPanel vT = new VarInputPanel(labelsTent, DEFAULT_NAME, 
 					DEFAULT_SITE_NUMBER, DEFAULT_DATE, DEFAULT_TENTERS,
 					DEFAULT_DAYS_STAYING);
 			int rTent;
 			
 			do {
 				// shows the variable input dialog box
 				rTent =JOptionPane.showConfirmDialog(null, vT, "Reserve a Tent", 
 					   JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
 			// checks for any error that may occur, sends the panel information
 			// and the results, along with the type of the object
 			} while(!checkInputForError(vT, rTent, Tent.TYPE));
 			// if the ok button is clicked
 			if(rTent == JOptionPane.OK_OPTION){
 				// creates an array list of objects with the input
 				Object[] varResult = vT.getUpdatedVars();
 				// creates a new tent with the results
 				Tent t = new Tent(varResult);
 				// calculates the cost for the tent duration
 				t.setAccount(t.getDaysStaying() * t.getNumOfTenters() * 3);
 				JOptionPane.showMessageDialog(null, "Please Deposit $" + 
 						DECIMAL_FORMAT.format(t.getAccount()));
 				// add the site to the siteModel
 				siteTableModel.addSite(t);
 				// fills in the siteNumber taken
 				fillSite(t.getSiteNumber() - 1);
 			}
 		}
 		
 		// checks in an RV
 		if(comp == checkInRV){
 			// labels to be sent to variable input panel
 			String[] labelsRV = {"Name Reserving:", "Site Number:", 
 					"Occupied On:", "Power needed:", "Days Staying:"};
 			//creates a new variable input panel with the labels, default values
 			VarInputPanel vR = new VarInputPanel(labelsRV, DEFAULT_NAME, 
 					DEFAULT_SITE_NUMBER, DEFAULT_DATE, DEFAULT_POWER_USED, 
 					DEFAULT_DAYS_STAYING);
 			int rRV;
 
 			do {
 				// shows the dialog box
 				rRV = JOptionPane.showConfirmDialog(null, vR, "Reserve an RV", 
 					   JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
 			// checks for any input error
 			} while(!checkInputForError(vR, rRV, RV.TYPE));
 
 			// if the OK button was pushed
 			if(rRV == JOptionPane.OK_OPTION){	
 				// creates an Object Array of the input
 				Object[] varResult = vR.getUpdatedVars();
 				// creates a new RV with the input
 				RV r = new RV(varResult);
 				
 				// calculates the cost for an RV
 				r.setAccount(r.getDaysStaying() * 30);
 				JOptionPane.showMessageDialog(null, "Please Deposit $" +
 						DECIMAL_FORMAT.format(r.getAccount()));
 				// adds the RV to the SiteModel
 				siteTableModel.addSite(r);
 				// Fills the site number taken
 				fillSite(r.getSiteNumber() - 1);
 			}
 
 		}
 		
 		//checks out a camper
 		if(comp == checkOut){
 			
 			// label to be sent to the variable input panel
 			String[] labelsCheckOut = {"Check Out On"};
 			// creates a new variable input panel with the label, default date
 			VarInputPanel vR = new VarInputPanel(labelsCheckOut, DEFAULT_DATE);
 			int rCheckOut;
 			// gets the selected row
 			int index = table.getSelectedRow();
 			
 			Site s;
 			// if a row hasn't been selected
 			if (index < 0) {
 				JOptionPane.showMessageDialog(this,"You have not selected " + 
 												"an Entry to Check Out");
 				
 				
 			} else {
 				s = siteTableModel.getSite(index);
 				
 				Date checkOut = new Date();
 				// used to make sure it was a successful parse
 				boolean success = true;
 				// used to make sure ok was clicked
 				boolean btnOption;
 
 				do{
 					// shows the variable input dialog
 					rCheckOut=JOptionPane.showConfirmDialog(null,vR,"Check Out", 
 					   JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
 					
 					// sets btnOption to whether or not ok was clicked
 					btnOption = (rCheckOut == JOptionPane.OK_OPTION);
 					// if the input matched the output and ok was clicked
 					if(vR.doUpdatedVarsMatchInput() && btnOption) {
 						// creates an Object Array of the input
 						Object[] varResult = vR.getUpdatedVars();
 						success = false;
 						try {
 							// parses the date into mm/dd/yyyy
 							checkOut =SIMPLE_FORMAT.parse((String)varResult[0]);
 						} catch (Exception e) {
 							JOptionPane.showMessageDialog(null, "Enter a " +
 												"correct date (MM/DD/YYYY)");
 							success = true;
 						}
 					} else if (btnOption){
 					   JOptionPane.showMessageDialog(null,"Date out of range. "+
 								" Please check your inputs.");
 					}
 				}while(success && btnOption);
 				
 				// if ok was clicked
 				if (btnOption) {
 					BetterGregorianCalendar g = new BetterGregorianCalendar();
 					// set the check out date
 					g.setTime(checkOut);
 					// get the check in date
 					int depositDays = s.getDaysStaying();
 					int d = g.daysSince(s.getCheckIn());
 					// calculate the cost of leaving
 					
 					double costs = s.getAccount();
 					if (d<depositDays) {
 						costs -= s.calcCost(d);
 						JOptionPane.showMessageDialog(null,
 								"Here is your Refund $" + 
 								DECIMAL_FORMAT.format(costs));
 					}
 					if(d==depositDays) {
 						costs = 0;
 						JOptionPane.showMessageDialog(null, "No Transaction");
 					}
 					if (d>depositDays) {
 						costs -= siteTableModel.getSite(index).calcCost(d);				
 						JOptionPane.showMessageDialog(null, "You owe $" + 
 								DECIMAL_FORMAT.format((-1)*costs));
 					}
 
 
 					siteTableModel.checkOut(index);
 					decrementSite(index);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Helper method that calls the JFileChooser and allows the user to select
 	 * a file.
 	 * 
 	 * @return The selected absolute file path is returned as a String
 	 */
 	private String fileOperations() {
 		// new file chooser
 		JFileChooser fc = new JFileChooser();
 		// shows the JFileChooser
 		int returnVal = fc.showSaveDialog(this);
 		// if the user clicked ok, return the file
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			try{
 				// send the file to the SiteModel
 				return fc.getSelectedFile().getAbsolutePath();
 			// catch any error that may occur
 			}catch(Throwable e){
 				JOptionPane.showMessageDialog(null, 
 						"File not recognized");
 			}
 		}
 		//If the file was invalid, or something really bad happend, an empty
 		//string is returned... this is "caught" in the save and load methods.
 		return "";
 	}
 	
 	/******************************************************************
 	 * Fills the siteNumber according to the number they checked in
 	 * @param d takes in the site number
 	 *****************************************************************/
 	private void fillSite(int d) {
 		if (sitesTaken[d]== false) {
 			sitesTaken[d] = true;
 			
 			if (++usedSites==MAX_NUMBER_OF_SITES) {
 				JOptionPane.showMessageDialog(null, "All sites are occupied",
 						"Warning", JOptionPane.WARNING_MESSAGE);
 				checkInRV.setEnabled(false);
 				checkInTent.setEnabled(false);
 			}
 		} else {
 			JOptionPane.showMessageDialog(null, "This site was already taken!", 
 					"Error", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	/******************************************************************
 	 * Removes a siteNumber taken when checked out
 	 * @param d takes in the site number
 	 *****************************************************************/
 	private void decrementSite(int d) {
 		if (sitesTaken[d] == true) {
 			sitesTaken[d] = false;
 
 			checkInRV.setEnabled(true);
 			checkInTent.setEnabled(true);
 
 			usedSites--;
 			if (usedSites < 0)
 				JOptionPane.showMessageDialog(null, "Stop Decrementing Sites!", 
 						"Error", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	/******************************************************************
 	 * Checks the input for Errors
 	 * @param p the panel containing the input
 	 * @param i the int representing the button clicked
 	 * @param type either a Tent or RV
 	 * @returns true or false depending on the input
 	 *****************************************************************/
 	private boolean checkInputForError(VarInputPanel p, int i, int type) {
 		if (i==JOptionPane.OK_OPTION){	
 			if(type == RV.TYPE || type == Tent.TYPE){
 				if(p.doUpdatedVarsMatchInput()) {
 					Object[] varResult = p.getUpdatedVars(); 				
 					return checkInputVariableBounds(varResult, type);
 				}
 
 				JOptionPane.showMessageDialog(null, "Numbers out of range. " +
 						" Please check your inputs.");
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/******************************************************************
 	 * Checks the input for Errors
 	 * @param varResult takes in an array of input
 	 * @param type the type of the site (RV, Tent)
 	 * @return true or false depending on the input
 	 *****************************************************************/
 	private boolean checkInputVariableBounds(Object[] varResult, int type) {
 		//Check the Site number
 
 		if ((Integer)varResult[1] < 1) {
 			JOptionPane.showMessageDialog(null, "The Site Number must be " +
 												"\1 or larger.");
 			return false;
 		}
 		if ((Integer)varResult[1] > MAX_NUMBER_OF_SITES) {
 			JOptionPane.showMessageDialog(null, "The Site Number must be " + 
 							MAX_NUMBER_OF_SITES + " or less.");
 			return false;
 		}
 		if (sitesTaken[(Integer)varResult[1] - 1]) {
 			JOptionPane.showMessageDialog(null, "The Site has already been " +
 												"taken!");
 			return false;
 		}
 
 		//Check the Date
 		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
 
 		try {			
 			sdf.parse((String)varResult[2]);
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(null, "Enter a correct date " +
 												"(MM/DD/YYYY)");
 			return false;
 		}
 
 
 		//Check the Number of Tenters, or the Power used!
 		if (type == Tent.TYPE)
 		{
 			if ((Integer)varResult[3] < 1) {
 				JOptionPane.showMessageDialog(null, "There must be at least " +
 													"one tenter!");
 				return false;
 			}
 		} else if (type == RV.TYPE) {
 			if ((Integer)varResult[3] < 0) {
 				JOptionPane.showMessageDialog(null, "We will not accept your " +
 													"RV's Power as payment");
 				return false;
 			}
 			if (((Integer)varResult[3] / 10 < 3) || 
 					((Integer)varResult[3] / 10 > 5) ||
 					(Integer)varResult[3]%10 != 0) {
 				JOptionPane.showMessageDialog(null, "Power must be either 30, "+
 													"40, or 50 Amps");
 				return false;
 			}
 		}
 
 		//Check the Number of Days Stayed.
 		if ((Integer)varResult[4] < 1) {
 			JOptionPane.showMessageDialog(null, "You can't stay a " +
 												"negative number of Days!");
 			return false;
 		}
 		return true;
 	}
 
 	/******************************************************************
 	 * Checks to see which column was clicked
 	 * @param m MouseEvent
 	 *****************************************************************/
 	@Override
 	public void mouseClicked(MouseEvent m) {
 		table.getSelectionModel().clearSelection();
 		siteTableModel.sort(table.columnAtPoint(m.getPoint()));
 		siteTableModel.fireTableDataChanged();
 	}
 
 	//Must be implemented, but they are not used
 	public void mouseEntered(MouseEvent arg0) {}
 	public void mouseExited(MouseEvent arg0) {}
 	public void mousePressed(MouseEvent arg0) {}
 	public void mouseReleased(MouseEvent arg0) {}
 }
