 package gui;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.FocusTraversalPolicy;
 import java.awt.KeyboardFocusManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.swing.BoundedRangeModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.KeyStroke;
 import javax.swing.SpinnerDateModel;
 import javax.swing.SpinnerModel;
 import javax.swing.SwingConstants;
 
 import messageTypes.AllMessageTypes;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 import com.toedter.calendar.JDateChooser;
 
 import core.IGUIHandler;
 import core.MessageInfo;
 
 /**
  * The MultiChannelGUI class is used to generate the main GUI and is called by the <code>GUIHandler</code>.
  *
  * @see IGUIHandler
  * @author  Benjamin Buetikofer, Yannik Kopp, Roland Hofer
  * @version 1.0
  */
 public class MultiChannelGUI {
 
 	// Instanzvariablen GUI
 	private JFrame frame;
 	private JPanel schedulerPanel;
 	private JPanel calendarPanel;
 	private JPanel reminderPanel;
 	private IGUIHandler guiHandler;
 	private JComboBox comboBox;
 	private JTextField tFRecipient;
 	private JTextField tFSubject;
 	private JTextPane messageBody;
 	private JCheckBox chckbxScheduler;
 	private String selectedItem;
 	private JPanel reminderTimePanel;
 	private JTextField tFReminderTime;
 	private JCheckBox chckbxReminder;
 	private JSpinner timespinner;
 	private JDateChooser dateChooser;
 	private SpinnerModel model;
 	private JComponent editor;
 	private JTextField tFAttachment;
 	private JLabel lblAttachment;
 	private JButton btnDurchsuchen;
 	private File file;
 	static TabFocusPolicy tabPolicy;
 
 	
 	/**
 	 * Constructor MultiChannel GUI
 	 * @param pGuiHandler Handler from the GUI Interface
 	 */
 	public MultiChannelGUI(IGUIHandler pGuiHandler) {
 
 		guiHandler = pGuiHandler;
 
 		initialize();
 		
 		frame.setVisible(true);
 	}
 
 	/**
 	 * Initialises the contents of the frame, layout was created with Googles WindowBuilder Pro
 	 * 
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 526, 350);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(
 				new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.MIN_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("min:grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.MIN_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.MIN_COLSPEC,},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.NARROW_LINE_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("fill:max(46dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("bottom:max(22dlu;default):grow"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("fill:default"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.PREF_ROWSPEC,}));
 
 		// Reciepient
 		JLabel lblRecipient = new JLabel("Empfänger:");
 		tFRecipient = new JTextField();
 		tFRecipient.setColumns(10);
 		tFRecipient.setToolTipText("Email-Adresse wie 'Felix.Muster@muster.ch' oder 'muster@muster.ch' eingeben");
 
 		// Subject
 		JLabel lblSubject = new JLabel("Betreff:");
 		tFSubject = new JTextField();
 		tFSubject.setColumns(10);
 
 		// MessageField
 		JLabel lblMessage = new JLabel("Nachricht:");
 		messageBody = new JTextPane();
 		final JScrollPane conversationScrollPane = new JScrollPane(messageBody);
 		  conversationScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
 
 		     BoundedRangeModel brm = conversationScrollPane.getVerticalScrollBar().getModel();
 		     boolean wasAtBottom = true;
 
 		     @Override
 			public void adjustmentValueChanged(AdjustmentEvent e) {
 		        if (!brm.getValueIsAdjusting()) {
 		           if (wasAtBottom)
 		              brm.setValue(brm.getMaximum());
 		        } else
 		           wasAtBottom = ((brm.getValue() + brm.getExtent()) == brm.getMaximum());
 
 		     }
 		  });  
 		patch(messageBody);
 
 		// Combobox for choosing the message type
 		comboBox = new JComboBox();
 		comboBox.addActionListener(new ComboboxActionLister());
 		comboBox.setModel(new DefaultComboBoxModel(AllMessageTypes.values()));
 		
 		// Scheduler
 		chckbxScheduler = new JCheckBox("Scheduler: ");
 		chckbxScheduler
 				.setToolTipText("Select this if you want to schedule this message.");
 		chckbxScheduler.setHorizontalTextPosition(SwingConstants.LEADING);
 		chckbxScheduler.addActionListener(new SchedulerActionListener());
 
 		schedulerPanel = new JPanel();
 		schedulerPanel.add(chckbxScheduler);
 		schedulerPanel.setVisible(true);
 		
 		// Display the calendar
 		calendarPanel = datePicker(null, new Date());
 		calendarPanel.setVisible(false); // Nur Anzeigen wenn Schedule ausgewählt wird
 		
 		// Reminder
 		chckbxReminder = new JCheckBox("Reminder: ");
 		chckbxReminder
 				.setToolTipText("Select this if you want to recieve a reminder before this message is sent.");
 		chckbxReminder.setHorizontalTextPosition(SwingConstants.LEADING);
 		chckbxReminder.addActionListener(new ReminderActionListener());
 
 		reminderPanel = new JPanel();
 		reminderPanel.add(chckbxReminder);
 		reminderPanel.setVisible(false); // Only show when scheduler is selected
 		
 		// Panel for reminder time input
 		reminderTimePanel = new JPanel();
 		reminderTimePanel.setLayout(null);
 		reminderTimePanel.setVisible(false);
 		tFReminderTime = new JTextField();
 		tFReminderTime.setBounds(64, 6, 52, 28);
 		reminderTimePanel.add(tFReminderTime);
 		tFReminderTime.setColumns(10);
 		
 		JLabel lblMinuten = new JLabel("Minuten:");
 		lblMinuten.setBounds(6, 12, 61, 16);
 		reminderTimePanel.add(lblMinuten);
 		
 		// Send Button
 		JButton btnSend = new JButton("Abschicken");
 		btnSend.addActionListener(new SendActionListener());
 		
 		// Put everything into the frame
 		frame.getContentPane().add(lblRecipient, "2, 2");
 		frame.getContentPane().add(tFRecipient, "4, 2, 25, 1, fill, default");
 		frame.getContentPane().add(lblSubject, "2, 4");
 		frame.getContentPane().add(tFSubject, "4, 4, 25, 1, fill, default");
 		frame.getContentPane().add(lblMessage, "2, 6");
 		frame.getContentPane().add(conversationScrollPane, "2, 8, 27, 7, fill, fill");
 		frame.getContentPane().add(comboBox, "30, 6, fill, default");
 		frame.getContentPane().add(schedulerPanel, "30, 2, left, fill");
 		frame.getContentPane().add(reminderPanel, "30, 4, left, fill");
 		frame.getContentPane().add(calendarPanel, "30, 10, fill, default");
 		frame.getContentPane().add(reminderTimePanel, "30, 12, fill, fill");
 		frame.getContentPane().add(btnSend, "30, 14, fill, fill");
 		
 		createAttachmentPanel();
 		
 		// Tab Policy
 		Vector<Component> order = new Vector<Component>(7);
         order.add(tFRecipient);
         order.add(tFSubject);
         order.add(messageBody);
         order.add(chckbxScheduler);
         order.add(btnSend);
         tabPolicy = new TabFocusPolicy(order);
         frame.setFocusTraversalPolicy(tabPolicy);
 		
 	}
 	
 	/**
 	 * This method creates the attachment panel
 	 */
 	public void createAttachmentPanel() {
 		lblAttachment = new JLabel("Attachment:");
 		frame.getContentPane().add(lblAttachment, "2, 16, right, default");
 		
 		tFAttachment = new JTextField();
 		tFAttachment.setEditable(false);
 		frame.getContentPane().add(tFAttachment, "4, 16, 25, 1, fill, default");
 		tFAttachment.setColumns(10);
 		
 		btnDurchsuchen = new JButton("Durchsuchen");
 		btnDurchsuchen.addActionListener(new AttachmentActionListener());
 		frame.getContentPane().add(btnDurchsuchen, "30, 16");
 	}
 
 	/**
 	 * This method creates the date picker panel
 	 * 
 	 * @param label Label name
 	 * @param value Date to display
 	 * @return the panel with the date picker and the timespinner
 	 */
 	private JPanel datePicker(String label, Date value) {
 		JPanel datePanel = new JPanel();
 		dateChooser = new JDateChooser();
 		if (value != null) {
 			dateChooser.setDate(value);
 		}
 		for (Component comp : dateChooser.getComponents()) {
 			if (comp instanceof JTextField) {
 				((JTextField) comp).setColumns(50);
 				((JTextField) comp).setEditable(false);
 			}
 		}
 
 		model = new SpinnerDateModel();
 		timespinner = new JSpinner(model);
 		editor = new JSpinner.DateEditor(timespinner, "HH:mm");
 		timespinner.setEditor(editor);
 		if (value != null) {
 			timespinner.setValue(value);
 		}
 
 		datePanel.add(timespinner);
 		datePanel.add(dateChooser);
 
 		return datePanel;
 	}
 	
 	/**
 	 * 
 	 * Patches the JTextArea so we can leave the TextArea with Tab
 	 * based on http://stackoverflow.com/a/525867
 	 * 
 	 * @param c Component to patch
 	 *
 	 */
 	public static void patch(Component c) {
         Set<KeyStroke> 
         strokes = new HashSet<KeyStroke>(Arrays.asList(KeyStroke.getKeyStroke("pressed TAB")));
         c.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, strokes);
         strokes = new HashSet<KeyStroke>(Arrays.asList(KeyStroke.getKeyStroke("shift pressed TAB")));
         c.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, strokes);
     }
 
 	/*
 	 * Inner classes
 	 */
 	
 	/**
 	 * This ActionListener will collect all necessary information to dispatch
 	 * the message created by the user.
 	 * 
 	 * @author bbuetikofer
 	 *
 	 */
 	private class SendActionListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			Date scheduleDate = null;
 			Date scheduleHour = null;
 			Date reminderDate = null;
 			
 			// Get all message types
 			AllMessageTypes enums = (AllMessageTypes) comboBox.getSelectedItem();
 			selectedItem = enums.toString();
 
 			List<String> recipients = new ArrayList<String>();
 			
 			// Split the address input field into single addresses
 			String[] addresses = tFRecipient.getText().split("[,;]+");
 			
 			for (int i = 0; i < addresses.length; i++) {
 				recipients.add(addresses[i]);
 			}
 
 			// Check if the user wants to schedule the message and take the apropriate action
 			if (chckbxScheduler.isSelected()) {
 				scheduleHour = (Date) timespinner.getValue();
 				scheduleDate = dateChooser.getDate();
 
 				// Convert Date to String
 				DateFormat df = new SimpleDateFormat("dd MMMM yyyy");
 				String convertedDate = df.format(scheduleDate);
 				df = new SimpleDateFormat("HH");
 				int convertedHour = Integer.parseInt(df.format(scheduleHour));
 				df = new SimpleDateFormat("mm");
 				int convertedMin = Integer.parseInt(df.format(scheduleHour));
 
 				int schedulerTime = convertedMin;
 
 				// Scheduletime in one string
 				String convertedTime = "" + convertedHour + ":" + schedulerTime;
 
 				df = new SimpleDateFormat("dd MMMM yyyy HH:mm");
 				try {
 					scheduleDate = df
 							.parse(convertedDate + " " + convertedTime);
 				} catch (ParseException e) {
 					// TODO Handle this error
 					e.printStackTrace();
 				}
 
 				// Remindertime in one string, if reminder is selected
 				if (chckbxReminder.isSelected()) {
 					int reminderTime = convertedMin; // If nothing is set, reminder time = send time
 					reminderDate = (Date) timespinner.getValue();
 					
 					if(!tFReminderTime.getText().equals("")) {
 						reminderTime = convertedMin	- Integer.parseInt(tFReminderTime.getText());
 					}
 					
 					convertedTime = "" + convertedHour + ":" + reminderTime;
 
 					df = new SimpleDateFormat("dd MMMM yyyy HH:mm");
 					try {
 						reminderDate = df.parse(convertedDate + " "
 								+ convertedTime);
 					} catch (ParseException e) {
 						// TODO Write to Log window!
 						e.printStackTrace();
 					}
 				}
 			}
 			// Send message
 			try {
 				MessageInfo newInfo = new MessageInfo(recipients, tFSubject.getText(),messageBody.getText(), selectedItem, scheduleDate, reminderDate, file);
 				ArrayList<String> errorList = guiHandler.sendMessage(newInfo);
 				if(!errorList.isEmpty()){
 					//TODO: Create Dialog with all error Messages!
 					JOptionPane.showMessageDialog(frame, "Message versenden fehlgeschlagen! Mehr Informationen im Log-Window");
 				} else {
 					tFRecipient.setText("");
 					tFSubject.setText("");
 					messageBody.setText("");
 					tFAttachment.setText("");
 				}
 			} catch (Exception e) {
 				JOptionPane.showMessageDialog(frame, e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Shows the reminder and calendar panel
 	 * 
 	 * @author bbuetikofer
 	 *
 	 */
 	private class SchedulerActionListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			if (chckbxScheduler.isSelected()) {
 				calendarPanel.setVisible(true);
 				reminderPanel.setVisible(true);
 			} else {
 				reminderTimePanel.setVisible(false);
 				calendarPanel.setVisible(false);
 				// Also remove the reminder panel
 				reminderPanel.setVisible(false);
 				// and reset the reminder checkbox
 				chckbxReminder.setSelected(false);
 			}
 		}
 	}
 
 	/**
 	 * Shows the reminder panel
 	 * 
 	 * @author bbuetikofer
 	 *
 	 */
 	private class ReminderActionListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			if (chckbxReminder.isSelected()) {
 				reminderTimePanel.setVisible(true);
 			} else {
 				reminderTimePanel.setVisible(false);
 			}
 
 		}
 	}
 	
 	/**
 	 * ActionListener for the combobox selector
 	 * 
 	 * @author R. Hofer
 	 *
 	 */
 	private class ComboboxActionLister implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			AllMessageTypes type = (AllMessageTypes) comboBox.getSelectedItem();
 			
 			switch (type){
 				case Email:
 					tFAttachment.setVisible(true);
 					lblAttachment.setVisible(true);
 					btnDurchsuchen.setVisible(true);
 					tFRecipient.setToolTipText("Email-Adresse wie 'Felix.Muster@muster.ch' oder 'muster@muster.ch' eingeben");
 					break;
 				case Mms:
 					tFAttachment.setVisible(true);
 					lblAttachment.setVisible(true);
 					btnDurchsuchen.setVisible(true);
 					tFRecipient.setToolTipText("<html> MMS-Nummer wie folgt eingeben u,x, y und z stehen für Ziffern 0-9 <br>" +
 							"+417uxxxyyzz, 07uxxxyyyzz, 07u / xxx yy zz, +41 7u xxx yy zz, 07u xxx yy zz, 07u/xxx yy zz </html>");
 					break;
 				case Sms:
 					tFAttachment.setVisible(false);
 					lblAttachment.setVisible(false);
 					btnDurchsuchen.setVisible(false);
 					tFRecipient.setToolTipText("<html> SMS-Nummer wie folgt eingeben u,x, y und z stehen für Ziffern 0-9 <br>" +
 							"+lluuxxxyyzz, 07uxxxyyyzz, 0uu / xxx yy zz, +ll uu xxx yy zz, 0uu xxx yy zz, 0uu/xxx yy zz </html>");
 					break;
 				case Print:
 					tFAttachment.setVisible(false);
 					lblAttachment.setVisible(false);
 					btnDurchsuchen.setVisible(false);
 					tFRecipient.setToolTipText("Geben Sie den Empfängername oder eine Druckeradresse ein");
 					break;
 			}
 		}
 	}
 	
 	/**
 	 * ActionListener for attachment selection
 	 * 
 	 * @author bbuetikofer
 	 *
 	 */
 	private class AttachmentActionListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			// Create Load Dialog
 			JFileChooser loadDialog = new JFileChooser();
 
 			// open load dialog
 			int returnVal = loadDialog.showOpenDialog(frame);
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				try {
 					file = loadDialog.getSelectedFile();
 					tFAttachment.setText(file.getCanonicalPath());
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Inner class which sets the TabFocusPolicy
 	 * Derived from http://docs.oracle.com/javase/tutorial/uiswing/misc/focus.html
 	 * 
 	 * @author bbuetikofer
 	 *
 	 */
 	public static class TabFocusPolicy extends FocusTraversalPolicy {
 		Vector<Component> order;
 
 		public TabFocusPolicy(Vector<Component> order) {
 			this.order = new Vector<Component>(order.size());
 			this.order.addAll(order);
 		}
 
 		@Override
 		public Component getComponentAfter(Container focusCycleRoot,
 				Component aComponent) {
 			int idx = (order.indexOf(aComponent) + 1) % order.size();
 			return order.get(idx);
 		}
 
 		@Override
 		public Component getComponentBefore(Container focusCycleRoot,
 				Component aComponent) {
 			int idx = order.indexOf(aComponent) - 1;
 			if (idx < 0) {
 				idx = order.size() - 1;
 			}
 			return order.get(idx);
 		}
 
 		@Override
 		public Component getDefaultComponent(Container focusCycleRoot) {
 			return order.get(0);
 		}
 
 		@Override
 		public Component getLastComponent(Container focusCycleRoot) {
 			return order.lastElement();
 		}
 
 		@Override
 		public Component getFirstComponent(Container focusCycleRoot) {
 			return order.get(0);
 		}
 }
 
 }
