 package clientapp;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.*;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.Date;
 import javax.mail.Session;
 import javax.swing.*;
 
 import helpertools.*; // custom import 
 
 // how to sign apps
 // http://java.sun.com/developer/onlineTraining/Programming/JDCBook/signed.html#1.1
 
 @SuppressWarnings({ "unused", "serial" })
 public class ITUserInputClientApp extends JApplet implements ActionListener, Runnable, WindowListener {
 	// Form GUI
 //	private JFrame clientWindow = new JFrame("New User Input Client");
 	private JPanel mainPanel = new JPanel();
 	private JPanel topPanel = new JPanel();
 	private JPanel bottomPanel = new JPanel();
 	
 	/* Radiobuttons */
 	private JRadioButton regEmp = new JRadioButton("Regular", true); // when active
 	private JRadioButton tempContract = new JRadioButton("Temporary/ Contract");
 	private ButtonGroup btnGroup1 = new ButtonGroup(); // group for our radio buttons
 	
 	private JRadioButton empIsRehire = new JRadioButton("Yes");
 	private JRadioButton empIsNotRehire	= new JRadioButton("No", true);
 	private ButtonGroup btnGroup2 = new ButtonGroup();
 	
 	/* Text labels */
 	private JLabel lastName = new JLabel("Last Name: ");
 	private JLabel firstName = new JLabel ("Legal First Name: ");
 	private JLabel midName = new JLabel("Middle Initial(s): ");
 	private JLabel prefName = new JLabel("Preferred Name: ");
 	private JLabel empLocation = new JLabel("Employee Location: ");
 	private JLabel empStartDate = new JLabel("Employee Start Date: ");
 	private JLabel empRehire = new JLabel("Is the employee a re-hire?");
 	private JLabel empTitle = new JLabel ("Title: ");
 	private JLabel empManager = new JLabel("Manager: ");
 	private JLabel empDept = new JLabel ("Department: ");
 	private JLabel empID = new JLabel ("Diosynth Employee ID: ");
 	private JLabel empStatus = new JLabel ("Employee Status: ");
 	
 	/* Text fields */
 	private JTextField lastNametxt = new JTextField(8);
 	private JTextField firstNametxt = new JTextField (8);
 	private JTextField midNametxt = new JTextField(8);
 	private JTextField prefNametxt = new JTextField(8);
 //	private JTextField empLocationtxt = new JTextField(8);
 	private JTextField empStartDatetxt = new JTextField(8);
 	private JTextField empTitletxt = new JTextField (8);
 	private JTextField empManagertxt = new JTextField(8);
 	private JTextField empDepttxt = new JTextField (8);
 	private JTextField empIDtxt = new JTextField (8);
 	
 	/* Selection Items */
 	private String[] comboBoxItem = {"THC", "RTP", "WES", "D&I"};
 	private JComboBox<?> empLoc = new JComboBox<Object>(comboBoxItem);
 	
 	/* Control Buttons */
 	private JButton submitButton = new JButton("Submit");
 	private JButton clearButton = new JButton("Clear Form");
 	private JButton selectDate = new JButton("Select Date");
 //	private JButton windowSizeButton = new JButton("WindowSize");
 	
 	/* Our Data */
 	private String clientid = "client1"; //System.getProperty("user.name"); // Get the person's user name
 	private String compname; // log computer name
 	
 	/* Network Streams */
 	private String serverAddress; 
 	private ObjectInputStream ois;
 	private ObjectOutputStream oos;
 	private Socket socket;
 	
 	/* Email GUI*/
 	private JLabel emailLabel = new JLabel("Email address:");
 	private JLabel passwordLabel = new JLabel("Password:");
 	private JTextField emailField = new JTextField(16);
 	private JPasswordField passwordField = new JPasswordField(16);
 	private JButton EsubmitBtn = new JButton("Submit");
 	private JFrame emailFrame = new JFrame("Google Mail login");
 	private JPanel emailPanel = new JPanel();
 	private JLabel statusMsgs = new JLabel("Click submit to login...");
 	private String filename;
 	
 
 	public void init() {
 		/* Start building GUI */
 		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
 
 		// Get Server address
 		serverAddress = getCodeBase().getHost();
 		if ((serverAddress == null) || (serverAddress.length() == 0)) serverAddress = "localhost"; // to run with appletviewer
 		
 //		clientWindow.getContentPane().add(topPanel, "Center");
 //		clientWindow.getContentPane().add(bottomPanel, "South");
 		
 		
 		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
 //		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
 		
 		// Start adding things to the gui
 		topPanel.add(firstName);
 		topPanel.add(firstNametxt);
 		topPanel.add(midName);
 		topPanel.add(midNametxt);
 		topPanel.add(lastName);
 		topPanel.add(lastNametxt);
 		topPanel.add(prefName);
 		topPanel.add(prefNametxt);
 		topPanel.add(empID);
 		topPanel.add(empIDtxt);
 		topPanel.add(empTitle);
 		topPanel.add(empTitletxt);
 		topPanel.add(empManager);
 		topPanel.add(empManagertxt);
 		topPanel.add(empLocation);
 		topPanel.add(empLoc); // a dropdown menu
 		topPanel.add(empDept);
 		topPanel.add(empDepttxt);
 		topPanel.add(empStartDate);
 		topPanel.add(empStartDatetxt);
 		empStartDatetxt.setEditable(false); // Just display whatever the user chooses as a date
 		topPanel.add(selectDate); // Instantiate datePicker
 		selectDate.addActionListener(this);
 		
 //		rightPanel.add(Box.createHorizontalStrut(15)); // create some space in the GUI
 		topPanel.add(Box.createHorizontalStrut(5));
 		
 		topPanel.add(empRehire);
 		topPanel.add(empIsNotRehire);
 		topPanel.add(empIsRehire);	
 		topPanel.add(Box.createHorizontalStrut(15)); // create some space in the GUI
 		topPanel.add(empStatus);
 		topPanel.add(regEmp);
 		topPanel.add(tempContract);
 		
 		// radio button functionality
 		btnGroup1.add(regEmp);
 		btnGroup1.add(tempContract);
 		btnGroup2.add(empIsNotRehire);
 		btnGroup2.add(empIsRehire);
 		
 		// Form Buttons
 		bottomPanel.add(clearButton);
 		clearButton.addActionListener(this);
 		bottomPanel.add(submitButton);
 		submitButton.addActionListener(this);
 		
 		// For the entire panel
 		mainPanel.add(topPanel);
 		mainPanel.add(bottomPanel);
 		add(mainPanel);
 		// For diagnostic purposes
 //		windowSizeButton.addActionListener(this);
 //		windowSizeButton.setMnemonic(KeyEvent.VK_ENTER);
 //		bottomPanel.add(windowSizeButton);
 //		windowSizeButton.setVisible(false);
 		
 		
 		/* Window Attributes */
 //		clientWindow.setSize(300, 610);
 //		clientWindow.setVisible(true);
 //		clientWindow.setResizable(false);
 //		clientWindow.setLocation(250, 250);
 //		clientWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 	}
 	
 	public void start() {
 		try {
 			socket = new Socket(this.serverAddress, 1234); // connect to server - let server close the socket for us
 			oos = new ObjectOutputStream(socket.getOutputStream());
 			ois = new ObjectInputStream(socket.getInputStream());
 			this.compname =  InetAddress.getLocalHost().getCanonicalHostName();
 			oos.writeObject(new ActiveConnectionObj(clientid, compname, new Date(), "Client"));
 			new Thread(this).start();
 		}
 		catch (Exception e) {
 			JOptionPane.showMessageDialog(null, "Can't connect to server. Restart App. " + e.toString());
 			return;
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		if (ae.getSource() == clearButton) {
 			// Popup and ask before making changes
 			int response = JOptionPane.showConfirmDialog(mainPanel, "Are you sure you want to clear the form?", 
 					"Erase all form entries?", JOptionPane.YES_NO_OPTION);
 			if (response == JOptionPane.YES_OPTION) clearForm();
 			else {
 				JOptionPane.showMessageDialog(mainPanel, "Entries not Erased.", "Clear Form Cancelled"
 						, JOptionPane.PLAIN_MESSAGE);
 			}
 		}
 		if (ae.getSource() == submitButton) {
 			/* Ask before submitting processing */
 			int response = JOptionPane.showConfirmDialog(mainPanel, "Are you sure you want to submit the form?", 
 					"Submit User", JOptionPane.YES_NO_OPTION);
 			
 			if (response == JOptionPane.YES_OPTION) processUser();
 			else {
 				JOptionPane.showMessageDialog(mainPanel, "User form not submitted.", "Submit Form Cancelled"
 						, JOptionPane.PLAIN_MESSAGE);
 			}
 		}
 //		if(ae.getSource() == windowSizeButton) {
 //			Dimension dim = clientWindow.getSize();
 //			System.out.println(dim);
 //		}
 		if (ae.getSource() == selectDate) {
 			empStartDatetxt.setText(new DatePicker(null).setPickedDate());
 		}
 		if (ae.getSource() == EsubmitBtn){
 			// Get authentication
 			try {			
 				final String user = emailField.getText();
 //				char[] pass = passwordField.getPassword();
 				emailFrame.dispose();
 				oos.writeObject(new RequestAuth(user));
 			} catch (IOException e) {
 				JOptionPane.showMessageDialog(null, e.toString());
 			}
 		}
 		
 		
 	}
 	private void processUser () {
 		/* Get the input from the form */
 		String l_name = lastNametxt.getText().trim();
 		String f_name = firstNametxt.getText().trim();
 		String m_name = midNametxt.getText().trim();
 		String p_name = prefNametxt.getText().trim();
 		String emp_loc = empLoc.getSelectedItem().toString().trim();
 		String emp_start = empStartDatetxt.getText().trim();
 		String emp_title = empTitletxt.getText().trim();
 		String emp_manager = empManagertxt.getText().trim();
 		String emp_dept = empDepttxt.getText().trim();
 		String emp_ID = empIDtxt.getText().trim();
 		boolean emp_rehire = empIsRehire.isSelected();
 		boolean emp_reg = regEmp.isSelected();
 		
 		if (f_name.length() == 0 || l_name.length() == 0 || emp_ID.length() == 0 || emp_title.length() == 0) {
 			JOptionPane.showMessageDialog(mainPanel, "First name, Last Name, Employee ID, and Title need to be filled in!");
 		}
 		else { 
 			try {
 				/* Create user object */
 				NewUser u = new NewUser(f_name, l_name, p_name, m_name, emp_title, emp_manager, emp_dept, emp_ID, emp_loc, emp_start, emp_rehire, emp_reg);
 				/* Send it to the server in an Object and receive a payload to process */
 				oos.writeObject(u);
 				
 			} catch (Exception e) {
 				// Code the part where the server's down and you're sending the info it
 				JOptionPane.showMessageDialog(null, "Restart application or call IT.", "Connection to server lost.", JOptionPane.ERROR_MESSAGE);
 				e.printStackTrace(); // TODO Take out for production
 			}
 		}	
 	}
 	
 	private void emailIt() {
 		// call the email gui
 		emailFrame.getContentPane().add(emailPanel, "Center");
 		emailPanel.add(emailLabel);
 		emailPanel.add(emailField);
 		emailPanel.add(statusMsgs);
 		emailPanel.add(EsubmitBtn);
 //		this.filename = filename;
 		EsubmitBtn.addActionListener(this);
 		emailFrame.setSize(300, 125);
 		emailFrame.setVisible(true);
 		emailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 	}
 	
 	private void clearForm() {
 		lastNametxt.setText("");
 		firstNametxt.setText("");
 		midNametxt.setText("");
 		prefNametxt.setText("");
 		empLoc.setSelectedIndex(0);
 		empStartDatetxt.setText("");
 		empTitletxt.setText("");
 		empManagertxt.setText("");
 		empDepttxt.setText("");
 		empIDtxt.setText("");
 		regEmp.setSelected(true);
 		empIsNotRehire.setSelected(true);
 	}
 
 	@Override
 	public void run() {receive();}
 	
 	// Multithreaded receive loop
 	private void receive() {
 		try {
 			while (true) {
 				Object serverResponse = ois.readObject();
 				// get form status
 				if (serverResponse instanceof FormStatus) {
 					FormStatus fs = (FormStatus) serverResponse;
 					int serverStatus = fs.getStatusID();
 					
 					if (serverStatus == UserStatusID.FORM_RECIEVED) { // form received code
 						emailIt();
 					}
 					else if (serverStatus == UserStatusID.EMAIL_SENT) { // email sent code
 						clearForm();
 						JOptionPane.showMessageDialog(null, "Form sent to Network Administrator.", "User Added!", JOptionPane.INFORMATION_MESSAGE);
 					}
 					else {
 						JOptionPane.showMessageDialog(null, "Form not sent to server, try again.", "Error processing form.", JOptionPane.WARNING_MESSAGE);
 					}
 				}
				if (serverResponse instanceof String) {
					JOptionPane.showMessageDialog(null, (String) serverResponse, "Error processing form.", JOptionPane.INFORMATION_MESSAGE);
				}
 			}
 		}
 		catch (IOException ioe) {
 			JOptionPane.showMessageDialog(null, ioe.getLocalizedMessage(), "Error with IO",  JOptionPane.WARNING_MESSAGE);
 			ioe.printStackTrace();
 		} catch (ClassNotFoundException cnfe) {
 			JOptionPane.showMessageDialog(null, cnfe.getMessage() + cnfe.getCause().toString(), "Error with Classes",  JOptionPane.WARNING_MESSAGE);
 		}
 	}
 	
 	@Override 
 	public void windowClosing(WindowEvent arg0) {
 		try {
 			socket.close(); // break connection to server
 		} catch (Exception e) {/* Don't do anything */}
 	}
 	
 	@Override // Not Implemented
 	public void windowActivated(WindowEvent arg0) {}
 
 	@Override // Not Implemented
 	public void windowClosed(WindowEvent arg0) {}
 
 	@Override // Not Implemented
 	public void windowDeactivated(WindowEvent arg0) {}
 	
 	@Override // Not Implemented
 	public void windowDeiconified(WindowEvent arg0) {}
 	
 	@Override // Not Implemented
 	public void windowIconified(WindowEvent arg0) {}
 	
 	@Override // Not Implemented
 	public void windowOpened(WindowEvent arg0) {}
 }
