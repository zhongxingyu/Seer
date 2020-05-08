 package nl.ctmm.trait.proteomics.qcviewer.input;
 
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Properties;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import nl.ctmm.trait.proteomics.qcviewer.Main;
 import nl.ctmm.trait.proteomics.qcviewer.gui.ViewerFrame;
 import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;
 import nl.ctmm.trait.proteomics.qcviewer.utils.DatePicker;
 
 import org.jfree.ui.RefineryUtilities;
 
 public class DataEntryForm extends JFrame implements ActionListener, Runnable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	JTextField inputText; 
 	Main parentMain = null; 
 	ViewerFrame parentViewerFrame = null; 
 	Properties appProperties = null; 
 	JDialog initialDialog = null;
 	JLabel message1 = null;
 	String rootDirectoryName = "";
 	
 	public DataEntryForm(final Main parent, final Properties appProperties) {
 		super("DataEntry Frame");
 		this.parentMain = parent; 
 		this.appProperties = appProperties; 
 	}
 	
 	public DataEntryForm(final ViewerFrame parent, final Properties appProperties) {
 		super("DataEntry Frame");
 		this.parentViewerFrame = parent; 
 		this.appProperties = appProperties;
 	}
 	
 	public void setRootDirectoryName(String rootDirectoryName) {
 		this.rootDirectoryName = rootDirectoryName;
 	}
 	
 	public void displayInitialDialog() {
 		message1 = new JLabel("<html>Reading reports from " + rootDirectoryName + "</html>");
 		initialDialog = new JDialog();
 		initialDialog.setTitle("Operation in progress");
 		initialDialog.getContentPane().add(message1);
 		initialDialog.setPreferredSize(new Dimension(300,100));
 		RefineryUtilities.centerFrameOnScreen(initialDialog);
 		initialDialog.pack();
 		initialDialog.setVisible(true);
 		initialDialog.revalidate();
 		System.out.println("Displaying initial dialog with message " + message1.getText());
 	}
 	
 	public void disposeInitialDialog() {
 		if (initialDialog != null) {
 			initialDialog.dispose();
 			initialDialog = null;
 		}
 	}
 	
 	public void displayErrorMessage (String errorMessage) {
     	JOptionPane.showMessageDialog(this, errorMessage,
 				  "Error",JOptionPane.ERROR_MESSAGE);
 	}
 	
 	 public void displayRootDirectoryChooser () {
 	 JFileChooser chooser = new JFileChooser();
 	 	chooser.setName("Select Report Folder");
 		chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
 	    int returnVal = chooser.showOpenDialog(null);
 	    String preferredRootDirectory = null;
 	    if(returnVal == JFileChooser.APPROVE_OPTION) {
 	    	preferredRootDirectory = chooser.getSelectedFile().getAbsolutePath();
 	       System.out.println("You chose to open this folder: " +
 	            chooser.getSelectedFile().getAbsolutePath());
 		    updatePreferredRootDirectory(preferredRootDirectory);
 		    if (parentViewerFrame != null) {
 				System.out.println("Cleaning everything and restarting..");
 				parentViewerFrame.clean();
 				parentViewerFrame.dispose();
 			}
 		    dispose();
 			new Main().runReportViewer();
 	    } 
 	 }
 	
     public void displayRootDirectoryEntryForm () {
     	JLabel instruction = new JLabel();
     	instruction.setText("Enter new report folder location:");
     	JLabel label = new JLabel();
     	label.setText("Root folder:");
     	inputText = new JTextField(100);
     	JButton SUBMIT = new JButton("SUBMIT");
     	SUBMIT.setPreferredSize(new Dimension(50, 20));
     	JButton CANCEL = new JButton("CANCEL"); 
     	CANCEL.setPreferredSize(new Dimension(50, 20));
   	  	SUBMIT.addActionListener(this);
   	  	SUBMIT.setActionCommand("SUBMITDIR");
   	  	CANCEL.addActionListener(this);
   	  	//CANCEL.setActionCommand("CANCELDIR");
   	  	CANCEL.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				dispose();
 			}
 		});
     	JPanel warningPanel = new JPanel(new GridLayout(1,1));
     	warningPanel.add(instruction);
     	JPanel inputPanel = new JPanel(new GridLayout(2,2));
     	inputPanel.setPreferredSize(new Dimension(120, 40));
     	inputPanel.add(label);
     	inputPanel.add(inputText);
     	inputPanel.add(SUBMIT);
     	inputPanel.add(CANCEL);
     	JPanel displayPanel = new JPanel(new GridLayout(2, 1)); 
     	displayPanel.add(warningPanel, 0);
     	displayPanel.add(inputPanel, 1);
     	displayPanel.setPreferredSize(new Dimension(220, 80));
     	add(displayPanel);
     	setSize(new Dimension(300, 150));
     	RefineryUtilities.centerFrameOnScreen(this);
     	setVisible(true);
     	repaint();
     	revalidate();
     }
     
     public void displayPreferredServerEntryForm () {
     	JLabel instruction = new JLabel();
     	instruction.setText("Enter server IP address:");
     	JLabel label = new JLabel();
     	label.setText("Server IP:");
     	inputText = new JTextField(15);
     	inputText.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                 //Process event like Submit button pressed
             	System.out.println("Enter pressed in server settings..");
             	String preferredServer = inputText.getText();
     			if (!preferredServer.trim().equals("")) { //appProperty not empty
     				System.out.println("Preferred web server= " + preferredServer);
     				dispose();
     				if (parentMain != null) {
     					updatePreferredServer(preferredServer);
     					parentMain.runReportViewer();
     				} else if (parentViewerFrame != null) {
     					System.out.println("Invoke parentViewerFrame methods");
     					parentViewerFrame.clean();
     					parentViewerFrame.dispose();
     					updatePreferredServer(preferredServer);
     					new Main().runReportViewer();
     				}
     			}
             }});
     	JButton SUBMIT = new JButton("SUBMIT");
     	SUBMIT.setPreferredSize(new Dimension(50, 20));
     	JButton CANCEL = new JButton("CANCEL"); 
     	CANCEL.setPreferredSize(new Dimension(50, 20));
   	  	SUBMIT.addActionListener(this);
   	  	SUBMIT.setActionCommand("SUBMITSER");
   	    CANCEL.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				dispose();
 			}
 		});
     	JPanel warningPanel = new JPanel(new GridLayout(1,1));
     	warningPanel.add(instruction);
     	JPanel inputPanel = new JPanel(new GridLayout(2,2));
     	inputPanel.setPreferredSize(new Dimension(120, 40));
     	inputPanel.add(label);
     	inputPanel.add(inputText);
     	inputPanel.add(SUBMIT);
     	inputPanel.add(CANCEL);
     	JPanel displayPanel = new JPanel(new GridLayout(2, 1)); 
     	displayPanel.add(warningPanel, 0);
     	displayPanel.add(inputPanel, 1);
     	displayPanel.setPreferredSize(new Dimension(220, 80));
     	add(displayPanel);
     	setSize(new Dimension(300, 150));
     	RefineryUtilities.centerFrameOnScreen(this);
     	setVisible(true);
     	revalidate();
     }
     
     /**
      * Sets the preferredRootDirectory 
      */
     public void updatePreferredRootDirectory(String newRootDirectory) {
     	System.out.println("Changing root directory to " + newRootDirectory);
     	appProperties.setProperty(Constants.PROPERTY_ROOT_FOLDER, newRootDirectory);
 		try {
 			FileOutputStream out = new FileOutputStream(Constants.PROPERTIES_FILE_NAME);
 			appProperties.store(out, null);
 	    	out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
     }
     
     /**
      * Sets the preferredServer 
      */
     public void updatePreferredServer(String newWebserver) {
     	System.out.println("Changing server to " + newWebserver);
     	appProperties.setProperty(Constants.PROPERTY_PREFERRED_WEBSERVER, newWebserver);
 		try {
 			FileOutputStream out = new FileOutputStream(Constants.PROPERTIES_FILE_NAME);
 			appProperties.store(out, null);
 	    	out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
     }
     
 	@Override
 	 public void actionPerformed(ActionEvent ae) {
 		System.out.println("DataEntryFrame Action command = " + ae.getActionCommand());
 		if (ae.getActionCommand().equals("SUBMITDIR")) {
 			String preferredRootDirectory = inputText.getText();
 			if (!preferredRootDirectory.trim().equals("")) { //appProperty not empty
 				dispose();
 				System.out.println("Preferred root directory = " + preferredRootDirectory);
 				if (parentMain != null) {
 					updatePreferredRootDirectory(preferredRootDirectory);
 					parentMain.runReportViewer();
 				} else if (parentViewerFrame != null) {
 					System.out.println("Invoke parentViewerFrame methods");
 					parentViewerFrame.clean();
 					parentViewerFrame.dispose();
 					updatePreferredRootDirectory(preferredRootDirectory);
 					new Main().runReportViewer();
 				}
 			} else displayErrorMessage ("Enter valid root directory.");
 		} else if (ae.getActionCommand().equals("SUBMITSER")) {
 			String preferredServer = inputText.getText();
 			if (!preferredServer.trim().equals("")) { //appProperty not empty
 				System.out.println("Preferred web server= " + preferredServer);
 				dispose();
 				if (parentMain != null) {
 					updatePreferredServer(preferredServer);
 					parentMain.runReportViewer();
 				} else if (parentViewerFrame != null) {
 					System.out.println("Invoke parentViewerFrame methods");
 					parentViewerFrame.clean();
 					parentViewerFrame.dispose();
 					updatePreferredServer(preferredServer);
 					new Main().runReportViewer();
 				}
 			}
 		} else if (ae.getActionCommand().startsWith("CANCEL")) {
 			if (parentViewerFrame != null) {
 				System.out.println("Invoke parentViewerFrame methods");
 				parentViewerFrame.clean();
 			}
 			dispose();
 		}
 	}
 
 	@Override
 	public void run() {
 		displayInitialDialog();
 	}
 
 	public void displayDateFilterEntryForm() {
 		JLabel label1 = new JLabel("From Date:");
 		final JTextField text1 = new JTextField(10);
 		text1.disable();
 		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
 				Constants.SIMPLE_DATE_FORMAT_STRING);
 		//Set current date - 2 weeks in fromDate
 		Calendar now = Calendar.getInstance();
     	now.add(Calendar.DATE, -14); 
     	Date fromDate = now.getTime();
     	text1.setText(sdf.format(fromDate));
 		JButton b1 = new JButton("select");
 		JPanel p1 = new JPanel();
 		p1.add(label1);
 		p1.add(text1);
 		p1.add(b1);
 		
 		JLabel label2 = new JLabel("Till Date:");
 		final JTextField text2 = new JTextField(10);
 		text2.disable();
 		//Set current date in text2 field
 		
 		Date tillDate = new Date(); 
 		text2.setText(sdf.format(tillDate));
 		JButton b2 = new JButton("select");
 		JPanel p2 = new JPanel();
 		p2.add(label2);
 		p2.add(text2);
 		p2.add(b2);
 		
 		JButton b3 = new JButton("Submit");
 		JButton b4 = new JButton("Cancel");
 
 		JPanel p3 = new JPanel(new GridLayout(1,2));
 		p3.add(b3);
 		p3.add(b4);
 		
 		JPanel p4 = new JPanel(new GridLayout(3,1));
 		p4.add(p1, 0);
 		p4.add(p2, 1);
 		p4.add(p3, 2);
 		getContentPane().add(p4);
 		pack();
 		RefineryUtilities.centerFrameOnScreen(this);
 		setVisible(true);
 		
 		/*addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
             	if (parentViewerFrame != null) {
     				System.out.println("You choose to Cancel date selection.. cleaning everything..");
     				parentViewerFrame.clean();
     				System.exit(0);
     			}
             }
         });*/
 		
 		b1.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				text1.setText(new DatePicker().setPickedDate());
 			}
 		});
 		b2.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				text2.setText(new DatePicker().setPickedDate());
 			}
 		});
 		b3.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				String date1 = text1.getText();
 				String date2 = text2.getText();
 				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
 						Constants.SIMPLE_DATE_FORMAT_STRING);
 				if (date1.equals("") || date2.equals("")) {
 					JOptionPane.showMessageDialog(null, "Press Select to choose proper date", "Error",JOptionPane.ERROR_MESSAGE);
 				} else {
 					try {
 						if (sdf.parse(date1).compareTo(sdf.parse(date2))>0) {
 							JOptionPane.showMessageDialog(null, "From date " + date1 + " is > To date " + date2, "Error",JOptionPane.ERROR_MESSAGE);
 						} else {
 							dispose();
 					    	appProperties.setProperty(Constants.PROPERTY_SHOW_REPORTS_FROM_DATE, date1);
 					    	appProperties.setProperty(Constants.PROPERTY_SHOW_REPORTS_TILL_DATE, date2);
 							try {
 								FileOutputStream out = new FileOutputStream(Constants.PROPERTIES_FILE_NAME);
 								appProperties.store(out, null);
 						    	out.close();
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
							if (parentViewerFrame != null) {
								System.out.println("Invoke parentViewerFrame methods");
								parentViewerFrame.clean();
								parentViewerFrame.dispose();
							}
 							new Main().runReportViewer();
 						}
 					} catch (ParseException e) {
 						e.printStackTrace();
 					} 
 				}
 			}
 		});
 		b4.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				dispose();
 			}
 		});
 	}
 }
