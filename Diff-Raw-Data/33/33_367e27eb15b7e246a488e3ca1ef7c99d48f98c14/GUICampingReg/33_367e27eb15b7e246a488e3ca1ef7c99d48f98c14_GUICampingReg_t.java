 package package1;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import javax.print.attribute.standard.OutputDeviceAssigned;
 import javax.swing.*;
 
 import VariableInputApi.*;
 
 public class GUICampingReg extends JFrame implements ActionListener {
 	
 	/** the serial version UID */
 	private static final long serialVersionUID = 1L;
 
 	/** JMenu for file */
 	private JMenu fileMenu;
 
 	/** JMenu for check in menu */
 	private JMenu checkInMenu;
 
 	/** JMenu for check out menu */
 	private JMenu checkOutMenu;
 
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
 	
 	/** Maximum number of sites **/
 	private final int MAX_NUMBER_OF_SITES;
 	
 	/** **/
 	private Boolean[] sitesTaken;
 	
 	//Make these not instance variables???
 	/** Inputed value for name */
 	private String name;
 
 	/** Cost for the stay */
 	private double cost;
 
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
 
 		// add ActionListeners
 		quit.addActionListener(this);
 		openT.addActionListener(this);
 		saveT.addActionListener(this);
 		openS.addActionListener(this);
 		saveS.addActionListener(this);
 		checkInTent.addActionListener(this);
 		checkInRV.addActionListener(this);
 		checkOut.addActionListener(this);
 
 		// set the menu bar
 		setJMenuBar(menus);
 
 		// instantiate the SiteModel and add it to the frame
 		siteTableModel = new SiteModel();
 		table = new JTable(siteTableModel);
 		scrollPane = new JScrollPane(table);
 		add(scrollPane);
 
 		// set the default close operation
 		setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
 
 		setSize(700,300);
 		setVisible(true);
 	}
 
 	private void clearAllSites() {
 		for (int i = 0; i<MAX_NUMBER_OF_SITES; i++) {
 			sitesTaken[i]= false;
 		}
 	}
 
 	/******************************************************************
 	 * Main method to run the GUI
 	 * @param args
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
 
 		if(comp == quit){
 			System.exit(1);
 		}
 
 		if(comp == openS){
 			
 			JFileChooser fc = new JFileChooser();
 			File file;
 			int returnVal = fc.showOpenDialog(this);
 	        if (returnVal == JFileChooser.APPROVE_OPTION) {
 	            file = fc.getSelectedFile();
 	            siteTableModel.loadDatabase(file.getName());
 	        }
 			
 		}
 
 		if(comp == saveS){
 			siteTableModel.saveDatabase("siteDB");
 		}
 
 		if(comp == openT){
 			siteTableModel.loadFromText("siteDBText");
 		}
 
 		if(comp == saveT){
 			siteTableModel.saveAsText("siteDBText");
 		}
 
 		if(comp == checkInTent){
 			String[] labelsTent = {"Name Reserving:", "Site Number:", "Occupied On:", "Number of Tenters:", "Days Staying:"};
 
 			VarInputPanel vT = new VarInputPanel(labelsTent, DEFAULT_NAME, DEFAULT_SITE_NUMBER, DEFAULT_DATE, DEFAULT_TENTERS, DEFAULT_DAYS_STAYING);
 			int resultTent;
 
 			do {
 				resultTent = JOptionPane.showConfirmDialog(null, vT, "Reserve a Tent", 
 						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
 			} while(!checkInputForError(vT, resultTent, Tent.TYPE));
 			if(resultTent == JOptionPane.OK_OPTION){
 				// got this to work
 				Object[] varResult = vT.getUpdatedVars();
 				
 				Tent t = new Tent(varResult);
 				sitesTaken[t.getSiteNumber() - 1] = true;
 				
 				cost = t.getDaysStaying() * t.getNumOfTenters() * 3;
 				DecimalFormat df = new DecimalFormat("#.00");
 				JOptionPane.showMessageDialog(null, "You owe $" + df.format(cost));
 				
 				siteTableModel.addSite(t);
 			}
 		}
 
 		if(comp == checkInRV){
 
 			//DialogCheckInRV x = new DialogCheckInRV(this, r);
 			String[] labelsRV = {"Name Reserving:", "Site Number:", "Occupied On:", "Power needed:", "Days Staying:"};
 
 			VarInputPanel vR = new VarInputPanel(labelsRV, DEFAULT_NAME, DEFAULT_SITE_NUMBER, DEFAULT_DATE, DEFAULT_POWER_USED, DEFAULT_DAYS_STAYING);
 			int resultRV;
 
 			do {
 				resultRV = JOptionPane.showConfirmDialog(null, vR, "Reserve an RV", 
 						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
 			} while(!checkInputForError(vR, resultRV, RV.TYPE));
 				
 			if(resultRV == JOptionPane.OK_OPTION){	
 				// got this to work
 				Object[] varResult = vR.getUpdatedVars();
 				
 				RV r = new RV(varResult);
 				sitesTaken[r.getSiteNumber() - 1] = true;
 				
 				cost = r.getDaysStaying() * 30;
 				DecimalFormat df = new DecimalFormat("#.00");
 				JOptionPane.showMessageDialog(null, "You owe $" + df.format(cost));
 				
 				siteTableModel.addSite(r);
 			}
 
 		}
 
 		if(comp == checkOut){
 			int index = table.getSelectedRow();
 			System.out.println("index: "+ index);
 			
 			try {
 				siteTableModel.checkOut(index);
 			}
 			catch(IndexOutOfBoundsException e) {
 				//e.printStackTrace();
 				JOptionPane.showMessageDialog(this,"You have not selected an Entry to Check Out");
 			}
 		}
 	}
 
 	private boolean checkInputForError(VarInputPanel p, int i, int type) {
 		if (i==JOptionPane.OK_OPTION){	
 			System.out.println(p.doUpdatedVarsMatchInput());
 			if(p.doUpdatedVarsMatchInput()) {
 				Object[] varResult = p.getUpdatedVars(); 				
 
 				System.out.println((Integer)varResult[1]);
 				
 				//Check the Site number
 				//TODO: Check to make sure the same site can't be used twice!
 				if ((Integer)varResult[1] < 1) {
 					JOptionPane.showMessageDialog(null, "The Site Number must be 1 or larger.");
 					return false;
 				}
 				if ((Integer)varResult[1] > MAX_NUMBER_OF_SITES) {
 					JOptionPane.showMessageDialog(null, "The Site Number must be " + MAX_NUMBER_OF_SITES + " or less.");
 					return false;
 				}
 				if (sitesTaken[(Integer)varResult[1] - 1]) {
 					JOptionPane.showMessageDialog(null, "The Site has already been taken!");
 					return false;
 				}
 				
 				//Check the Date
 				//TODO: Check the incoming Date String
 //				if ((String)varResult[2]) {
 //					JOptionPane.showMessageDialog(null, "The Date is out of range?");
 //					return false;
 //				}
 //				if ((String)varResult[2]) {
 //					JOptionPane.showMessageDialog(null, "The Date is out of range?");
 //					return false;
 //				}
 				
 				//Check the Number of Tenters, or the Power used!
 				if (type == Tent.TYPE)
 				{
 					if ((Integer)varResult[3] < 1) {
						JOptionPane.showMessageDialog(null, "There must be at least one tenter!");
 						return false;
 					}
 				} else if (type == RV.TYPE) {
					if ((Integer)varResult[3] < 0) {
 						JOptionPane.showMessageDialog(null, "We will not accept your RV's Power as payment");
 						return false;
 					}
					if (((Integer)varResult[3] / 10 < 3) || 
						((Integer)varResult[3] / 10 > 5) ||
						 (Integer)varResult[3]%10 != 0) {
						JOptionPane.showMessageDialog(null, "Power must be either 30, 40, or 50 Amps");
						return false;
					}
 				}
 				
 				//Check the Number of Days Stayed.
 				if ((Integer)varResult[4] < 1) {
 					JOptionPane.showMessageDialog(null, "You can't stay a negative number of Days!");
 					return false;
 				}
 				
 				
 				return true;
 			}
 
 			JOptionPane.showMessageDialog(null, "Numbers out of range. " +
 												" Please check your inputs.");
 			return false;
 		}
 		
 		return true;
 	}
 }
