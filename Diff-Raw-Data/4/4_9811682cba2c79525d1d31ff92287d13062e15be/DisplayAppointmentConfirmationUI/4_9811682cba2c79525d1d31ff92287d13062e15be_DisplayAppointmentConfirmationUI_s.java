 package gui.sub;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.border.EmptyBorder;
 
 import backend.DataService.DataServiceImpl;
 import backend.DataTransferObjects.AppointmentDto;
 import backend.DataTransferObjects.PatientDto;
 
 /**
  * DisplayAppointmentConfirmationUI shows information about a patient's appointment when a patient in the table
  * is clicked.
  */
 public class DisplayAppointmentConfirmationUI extends JDialog implements ActionListener {
 	private static DisplayAppointmentConfirmationUI displayAppointmentConfirmationUI;
 	
 	private JButton okButton = new JButton("OK");
 	private JTextArea textArea;
 	private Font font = new Font("Arial", Font.PLAIN, 16);
 	
 	/**
 	 * Constructor - creates the actual UI to display the patient's appointment information
 	 * @param name - the title to be displayed in the top bar of the pop up window
 	 * @param appt - the appointment information to be displayed
 	 */
 	private DisplayAppointmentConfirmationUI(String name, AppointmentDto appt) {
 		setModal(true);
 		setTitle(name);
 		
 		setLayout(new BorderLayout());
 		
 		JPanel infoPanel = new JPanel(new BorderLayout());
 		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		
 		PatientDto patient = DataServiceImpl.GLOBAL_DATA_INSTANCE.getPatient(appt.getPatientID());
 		
 		String text = "Date: " + appt.getApptDate().toString() + "\n" +
 					  "Patient Name: " + patient.getFirst() + " " + patient.getLast() +
 					  "Phone Number: " + patient.getPhone() +
					  "Confirmed: " + appt.getConfirmed(); // TODO: WE NEED CONFIRMATIONS!!!
 		
 		textArea = new JTextArea();
 		textArea.setLineWrap(true);
 		textArea.setWrapStyleWord(true);
 		textArea.setEditable(false);
 		textArea.setFont(font);
 		textArea.setOpaque(false);
 		textArea.setHighlighter(null);
 		textArea.setText(text);
 		infoPanel.add(textArea);
 
 		okButton.setFont(font);
 		okButton.addActionListener(this);
 		buttonPanel.add(okButton);
 		
 		infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 		buttonPanel.setBorder(new EmptyBorder(10, 10, 20, 10));
 		
 		add(infoPanel, BorderLayout.CENTER);
 		add(buttonPanel, BorderLayout.SOUTH);
 		
 		setResizable(false);
 	}
 	
 	/**
 	 * Makes the pop up window visible when a patient from the table is selected.
 	 * 
 	 * @param owner - the component that owns this pane (the AppointmentConfirmationListener)
 	 * @param appt  - the appointment information
 	 */
 	public static void ShowDialog(Component owner, AppointmentDto appt) {
 		displayAppointmentConfirmationUI = new DisplayAppointmentConfirmationUI("View Appointment", appt);
 		displayAppointmentConfirmationUI.pack();
 		displayAppointmentConfirmationUI.setLocationRelativeTo(owner);
 		displayAppointmentConfirmationUI.setVisible(true);
 	}
 	
 	/**
 	 * Closes the window once the user hits the "OK" button.
 	 */
 	public void actionPerformed(ActionEvent e) {
 		displayAppointmentConfirmationUI.setVisible(false);
 	}
 	
 }
