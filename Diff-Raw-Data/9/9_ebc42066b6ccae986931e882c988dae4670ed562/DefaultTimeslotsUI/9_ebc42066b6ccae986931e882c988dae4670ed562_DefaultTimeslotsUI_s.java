 package gui.sub;
 
 import backend.DataService.DataService.Day;
 import backend.DataService.DataServiceImpl;
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import gui.Constants;
 import gui.TimeSlot;
 
 public class DefaultTimeslotsUI extends JDialog implements ActionListener {
 	
 	
 	private static DefaultTimeslotsUI defaultTimeslotsUI;
 	private JTextField startTimeField1;
 	private JTextField endTimeField1;
 	private JTextField startTimeField2;
 	private JTextField endTimeField2;
 	private JTextField startTimeField3;
 	private JTextField endTimeField3;
 	private JTextField startTimeField4;
 	private JTextField endTimeField4;
 	private JTextField startTimeField5;
 	private JTextField endTimeField5;
 	private JTextField startTimeField6;
 	private JTextField endTimeField6;
 	private JTextField startTimeField7;
 	private JTextField endTimeField7;
 	private JButton okButton;
 	private JButton cancelButton;
 	
 	
 	private DefaultTimeslotsUI(String name) {
 		setModal(true);
 		setTitle(name);		
 		
 		startTimeField1 = new JTextField();
 		
 		//startTimeField1.set
 		startTimeField1.setFont(Constants.PARAGRAPH);
 		endTimeField1 = new JTextField();
 		endTimeField1.setFont(Constants.PARAGRAPH);
 		TimeSlot t1 = DataServiceImpl.GLOBAL_DATA_INSTANCE.getDayTimeslot(Day.SUNDAY);
 		startTimeField1.setText(t1.to24HrStringStart());
 		endTimeField1.setText(t1.to24HrStringEnd());
 		startTimeField2 = new JTextField();
 		startTimeField2.setFont(Constants.PARAGRAPH);
 		endTimeField2 = new JTextField();
 		endTimeField2.setFont(Constants.PARAGRAPH);
 		TimeSlot t2 = DataServiceImpl.GLOBAL_DATA_INSTANCE.getDayTimeslot(Day.MONDAY);
 		startTimeField2.setText(t2.to24HrStringStart());
 		endTimeField2.setText(t2.to24HrStringEnd());
 		startTimeField3 = new JTextField();
 		startTimeField3.setFont(Constants.PARAGRAPH);
 		endTimeField3 = new JTextField();
 		endTimeField3.setFont(Constants.PARAGRAPH);
 		TimeSlot t3 = DataServiceImpl.GLOBAL_DATA_INSTANCE.getDayTimeslot(Day.TUESDAY);
 		startTimeField3.setText(t3.to24HrStringStart());
 		endTimeField3.setText(t3.to24HrStringEnd());
 		startTimeField4 = new JTextField();
 		startTimeField4.setFont(Constants.PARAGRAPH);
 		endTimeField4 = new JTextField();
 		endTimeField4.setFont(Constants.PARAGRAPH);
 		TimeSlot t4 = DataServiceImpl.GLOBAL_DATA_INSTANCE.getDayTimeslot(Day.WEDNESDAY);
 		startTimeField4.setText(t4.to24HrStringStart());
 		endTimeField4.setText(t4.to24HrStringEnd());
 		startTimeField5 = new JTextField();
 		startTimeField5.setFont(Constants.PARAGRAPH);
 		endTimeField5 = new JTextField();
 		endTimeField5.setFont(Constants.PARAGRAPH);
 		TimeSlot t5 = DataServiceImpl.GLOBAL_DATA_INSTANCE.getDayTimeslot(Day.THURSDAY);
 		startTimeField5.setText(t5.to24HrStringStart());
 		endTimeField5.setText(t5.to24HrStringEnd());
 		startTimeField6 = new JTextField();
 		startTimeField6.setFont(Constants.PARAGRAPH);
 		endTimeField6 = new JTextField();
 		endTimeField6.setFont(Constants.PARAGRAPH);
 		TimeSlot t6 = DataServiceImpl.GLOBAL_DATA_INSTANCE.getDayTimeslot(Day.FRIDAY);
 		startTimeField6.setText(t6.to24HrStringStart());
 		endTimeField6.setText(t6.to24HrStringEnd());
 		startTimeField7 = new JTextField();
 		startTimeField7.setFont(Constants.PARAGRAPH);
 		endTimeField7 = new JTextField();
 		endTimeField7.setFont(Constants.PARAGRAPH);
 		TimeSlot t7 = DataServiceImpl.GLOBAL_DATA_INSTANCE.getDayTimeslot(Day.SATURDAY);
 		startTimeField7.setText(t7.to24HrStringStart());
 		endTimeField7.setText(t7.to24HrStringEnd());
 		okButton = new JButton("OK");
 		okButton.setFont(Constants.DIALOG);
 		cancelButton = new JButton("Cancel");
 		cancelButton.setFont(Constants.DIALOG);
 		setLayout(new BorderLayout());
 		add(makeTimeSlotPanel(), BorderLayout.CENTER);
 		setResizable(false);
 		pack();
 	}
 	
 	public static void ShowDialog(Component owner) {
 		defaultTimeslotsUI = new DefaultTimeslotsUI("Change Default Hours of Operation");
 		defaultTimeslotsUI.setLocationRelativeTo(owner);
 		defaultTimeslotsUI.setVisible(true);
 		
 		return;
 	}
 	
 	private JComponent makeTimeSlotPanel() {
     	JPanel panel = new JPanel(new GridLayout(0,1));
     	
     	JLabel note = new JLabel(" NOTE: All times must be entered in military time (00:00 - 23:59).");
     	panel.add(note);
     	
     	JPanel main1 = new JPanel(new BorderLayout());
     	JLabel label = new JLabel("Sunday  ");
     	label.setFont(Constants.HEADER);
     	JPanel labelPanel = new JPanel(new BorderLayout());
     	labelPanel.setPreferredSize(new Dimension(80, 0));
     	labelPanel.add(label);
     	JPanel sub1 = new JPanel(new GridLayout(1,0));
     	sub1.setPreferredSize(new Dimension(180,0));
     	JPanel start1 = new JPanel(new BorderLayout());
     	JLabel sub = new JLabel("Start: ");
     	start1.add(sub, BorderLayout.WEST);
     	start1.add(startTimeField1, BorderLayout.CENTER);
     	JPanel end1 = new JPanel(new BorderLayout());
     	sub = new JLabel("End: ");
     	end1.add(sub, BorderLayout.WEST);
     	end1.add(endTimeField1, BorderLayout.CENTER);
     	sub1.add(start1);
     	sub1.add(end1);
     	main1.add(labelPanel, BorderLayout.WEST);
     	main1.add(sub1, BorderLayout.CENTER);
     	panel.add(main1);
     	
     	JPanel main2 = new JPanel(new BorderLayout());
     	label = new JLabel("Monday  ");
     	label.setFont(Constants.HEADER);
     	labelPanel = new JPanel(new BorderLayout());
     	labelPanel.setPreferredSize(new Dimension(80, 0));
     	labelPanel.add(label);
     	JPanel sub2 = new JPanel(new GridLayout(1,0));
     	JPanel start2 = new JPanel(new BorderLayout());
     	sub = new JLabel("Start: ");
     	start2.add(sub, BorderLayout.WEST);
     	start2.add(startTimeField2, BorderLayout.CENTER);
     	JPanel end2 = new JPanel(new BorderLayout());
     	sub = new JLabel("End: ");
     	end2.add(sub, BorderLayout.WEST);
     	end2.add(endTimeField2, BorderLayout.CENTER);
     	sub2.add(start2);
     	sub2.add(end2);
     	main2.add(labelPanel, BorderLayout.WEST);
     	main2.add(sub2, BorderLayout.CENTER);
     	panel.add(main2);
     	
     	JPanel main3 = new JPanel(new BorderLayout());
     	label = new JLabel("Tuesday  ");
     	label.setFont(Constants.HEADER);
     	labelPanel = new JPanel(new BorderLayout());
     	labelPanel.setPreferredSize(new Dimension(80, 0));
     	labelPanel.add(label);
     	JPanel sub3 = new JPanel(new GridLayout(1,0));
     	JPanel start3 = new JPanel(new BorderLayout());
     	sub = new JLabel("Start: ");
     	start3.add(sub, BorderLayout.WEST);
     	start3.add(startTimeField3, BorderLayout.CENTER);
     	JPanel end3 = new JPanel(new BorderLayout());
     	sub = new JLabel("End: ");
     	end3.add(sub, BorderLayout.WEST);
     	end3.add(endTimeField3, BorderLayout.CENTER);
     	sub3.add(start3);
     	sub3.add(end3);
     	main3.add(labelPanel, BorderLayout.WEST);
     	main3.add(sub3, BorderLayout.CENTER);
     	panel.add(main3);
     	
     	JPanel main4= new JPanel(new BorderLayout());
     	label = new JLabel("Wednesday  ");
     	label.setFont(Constants.HEADER);
     	labelPanel = new JPanel(new BorderLayout());
     	labelPanel.setPreferredSize(new Dimension(80, 0));
     	labelPanel.add(label);
     	JPanel sub4 = new JPanel(new GridLayout(1,0));
     	JPanel start4 = new JPanel(new BorderLayout());
     	sub = new JLabel("Start: ");
     	start4.add(sub, BorderLayout.WEST);
     	start4.add(startTimeField4, BorderLayout.CENTER);
     	JPanel end4 = new JPanel(new BorderLayout());
     	sub = new JLabel("End: ");
     	end4.add(sub, BorderLayout.WEST);
     	end4.add(endTimeField4, BorderLayout.CENTER);
     	sub4.add(start4);
     	sub4.add(end4);
     	main4.add(labelPanel, BorderLayout.WEST);
     	main4.add(sub4, BorderLayout.CENTER);
     	panel.add(main4);
     	
     	JPanel main5 = new JPanel(new BorderLayout());
     	label = new JLabel("Thursday  ");
     	label.setFont(Constants.HEADER);
     	labelPanel = new JPanel(new BorderLayout());
     	labelPanel.setPreferredSize(new Dimension(80, 0));
     	labelPanel.add(label);
     	JPanel sub5 = new JPanel(new GridLayout(1,0));
     	JPanel start5 = new JPanel(new BorderLayout());
     	sub = new JLabel("Start: ");
     	start5.add(sub, BorderLayout.WEST);
     	start5.add(startTimeField5, BorderLayout.CENTER);
     	JPanel end5 = new JPanel(new BorderLayout());
     	sub = new JLabel("End: ");
     	end5.add(sub, BorderLayout.WEST);
     	end5.add(endTimeField5, BorderLayout.CENTER);
     	sub5.add(start5);
     	sub5.add(end5);
     	main5.add(labelPanel, BorderLayout.WEST);
     	main5.add(sub5, BorderLayout.CENTER);
     	panel.add(main5);
     	
     	JPanel main6 = new JPanel(new BorderLayout());
     	label = new JLabel("Friday  ");
     	label.setFont(Constants.HEADER);
     	labelPanel = new JPanel(new BorderLayout());
     	labelPanel.setPreferredSize(new Dimension(80, 0));
     	labelPanel.add(label);
     	JPanel sub6 = new JPanel(new GridLayout(1,0));
     	JPanel start6 = new JPanel(new BorderLayout());
     	sub = new JLabel("Start: ");
     	start6.add(sub, BorderLayout.WEST);
     	start6.add(startTimeField6, BorderLayout.CENTER);
     	JPanel end6 = new JPanel(new BorderLayout());
     	sub = new JLabel("End: ");
     	end6.add(sub, BorderLayout.WEST);
     	end6.add(endTimeField6, BorderLayout.CENTER);
     	sub6.add(start6);
     	sub6.add(end6);
     	main6.add(labelPanel, BorderLayout.WEST);
     	main6.add(sub6, BorderLayout.CENTER);
     	panel.add(main6);
     	
     	JPanel main7 = new JPanel(new BorderLayout());
     	label = new JLabel("Saturday  ");
     	label.setFont(Constants.HEADER);
     	labelPanel = new JPanel(new BorderLayout());
     	labelPanel.setPreferredSize(new Dimension(80, 0));
     	labelPanel.add(label);
     	JPanel sub7 = new JPanel(new GridLayout(1,0));
     	JPanel start7 = new JPanel(new BorderLayout());
     	sub = new JLabel("Start: ");
     	start7.add(sub, BorderLayout.WEST);
     	start7.add(startTimeField7, BorderLayout.CENTER);
     	JPanel end7 = new JPanel(new BorderLayout());
     	sub = new JLabel("End: ");
     	end7.add(sub, BorderLayout.WEST);
     	end7.add(endTimeField7, BorderLayout.CENTER);
     	sub7.add(start7);
     	sub7.add(end7);
     	main7.add(labelPanel, BorderLayout.WEST);
     	main7.add(sub7, BorderLayout.CENTER);
     	panel.add(main7);
     	
     	
     	
     	JPanel buttonPanel = new JPanel(new FlowLayout());
     	okButton.setActionCommand("ok");
     	okButton.addActionListener(this);
     	cancelButton.addActionListener(this);
     	okButton.setFont(Constants.DIALOG);
     	cancelButton.setFont(Constants.DIALOG);
     	buttonPanel.add(okButton);
     	buttonPanel.add(cancelButton);
     	
     	panel.add(buttonPanel);
     	
     	return panel;
     }
 	
 	private Integer checkTime(String time) {
 		Integer t;
 		if (time.equals("")) {
 			return null;
 		} else if (time.matches("[0-2][0-9]:[0-5][0-9]")) {
 			String hour = time.replaceAll(":[0-5][0-9]", "");
 			String minute = time.replaceAll("[0-2][0-9]:", "");
 			try { 
 				t = 60 * Integer.parseInt(hour) + Integer.parseInt(minute);
 			} catch(Exception ex) {
 				return null;
 			}
 		} else {
 			return null;
 		}
 		return t;
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("ok")) {
 			Integer t1, t2;
 			ArrayList<TimeSlot> slots = new ArrayList<TimeSlot>();
 			
 			t1 = checkTime(startTimeField1.getText());
 			t2 = checkTime(endTimeField1.getText());
 			if (t1 == null || t2 == null) {
 				JOptionPane.showMessageDialog(this, "All times must be valid format (00:00 - 23:59).", "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			} else if (t1 >= t2) {
 				JOptionPane.showMessageDialog(this, "End time must be after start time.", "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			} else{
 				slots.add(new TimeSlot(t1,t2));
 			}
 			
 			t1 = checkTime(startTimeField2.getText());
 			t2 = checkTime(endTimeField2.getText());
 			if (t1 == null || t2 == null) {
 				JOptionPane.showMessageDialog(this, "All times must be valid format (00:00 - 23:59).", "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			} else if (t1 >= t2) {
 				JOptionPane.showMessageDialog(this, "End time must be after start time.", "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			} else{
 				slots.add(new TimeSlot(t1,t2));
 			}
 			
 			t1 = checkTime(startTimeField3.getText());
 			t2 = checkTime(endTimeField3.getText());
 			if (t1 == null || t2 == null) {
 				JOptionPane.showMessageDialog(this, "All times must be valid format (00:00 - 23:59).", "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			} else if (t1 >= t2) {
 				JOptionPane.showMessageDialog(this, "End time must be after start time.", "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			} else{
 				slots.add(new TimeSlot(t1,t2));
 			}
 			
 			t1 = checkTime(startTimeField4.getText());
 			t2 = checkTime(endTimeField4.getText());
 			if (t1 == null || t2 == null) {
 				JOptionPane.showMessageDialog(this, "All times must be valid format (00:00 - 23:59).", "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			} else if (t1 >= t2) {
 				JOptionPane.showMessageDialog(this, "End time must be after start time.", "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			} else{
 				slots.add(new TimeSlot(t1,t2));
 			}
 			
 			t1 = checkTime(startTimeField5.getText());
 			t2 = checkTime(endTimeField5.getText());
 			if (t1 == null || t2 == null) {
 				JOptionPane.showMessageDialog(this, "All times must be valid format (00:00 - 23:59).", "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			} else if (t1 >= t2) {
 				JOptionPane.showMessageDialog(this, "End time must be after start time.", "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			} else{
 				slots.add(new TimeSlot(t1,t2));
 			}
 			
 			t1 = checkTime(startTimeField6.getText());
 			t2 = checkTime(endTimeField6.getText());
 			if (t1 == null || t2 == null) {
 				JOptionPane.showMessageDialog(this, "All times must be valid format (00:00 - 23:59).", "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			} else if (t1 >= t2) {
 				JOptionPane.showMessageDialog(this, "End time must be after start time.", "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			} else{
 				slots.add(new TimeSlot(t1,t2));
 			}
 			
 			t1 = checkTime(startTimeField7.getText());
 			t2 = checkTime(endTimeField7.getText());
 			if (t1 == null || t2 == null) {
 				JOptionPane.showMessageDialog(this, "All times must be valid format (00:00 - 23:59).", "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			} else if (t1 >= t2) {
 				JOptionPane.showMessageDialog(this, "End time must be after start time.", "Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			} else{
 				slots.add(new TimeSlot(t1,t2));
 			}
 			//TODO: STORE TIMES IN DATABASE	
			//dm.storeTimeSlots(slots);
 		}
 		defaultTimeslotsUI.setVisible(false);
     }
 }
