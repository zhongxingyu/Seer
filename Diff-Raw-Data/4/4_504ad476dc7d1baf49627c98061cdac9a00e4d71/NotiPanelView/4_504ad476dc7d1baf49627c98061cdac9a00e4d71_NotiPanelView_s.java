 package framePackage;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import data.CalendarModel;
 import data.Meeting;
 import data.MeetingRoom;
 import data.Notification;
 import data.Person;
 import data.Team;
 
 public class NotiPanelView extends JPanel implements PropertyChangeListener {
 
 	private JFrame frame;
 	private JPanel varselPanel;
 	private JLabel lblWarning;
 	private JList warningList;
 	private DefaultListModel listModel;
 	private List<Notification> notifications;
 	private AppointmentOverView appointOverView;
 	private CalendarModel calendarModel;
 
 	public NotiPanelView(CalendarModel calendarModel) {
 		this.calendarModel = calendarModel;
 		initialize();
 	}
 
 	private void initialize() {

 		varselPanel = new JPanel(new GridBagLayout());
 		varselPanel.setPreferredSize(new Dimension(250, 300));
 		varselPanel.setVisible(true);
 		varselPanel.setBorder(BorderFactory.createLineBorder(Color.black, 2));
 		GridBagConstraints c = new GridBagConstraints();
 
 		lblWarning = new JLabel("Varsel");
 		lblWarning.setFont(new Font(lblWarning.getFont().getName(), lblWarning
 				.getFont().getStyle(), 20));
 		c.gridx = 0;
 		c.gridy = 0;
 		varselPanel.add(lblWarning, c);
 
 		listModel = new DefaultListModel<Notification>();
 		warningList = new JList<Notification>(listModel);
 		warningList.setFixedCellWidth(15);
 		warningList.setCellRenderer(new NotiViewRender());
 		JScrollPane scrollPane = new JScrollPane(warningList);
 		scrollPane
 				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane
 				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		scrollPane.setPreferredSize(new Dimension(200, 250));
 		c.gridx = 0;
 		c.gridy = 1;
 		varselPanel.add(scrollPane, c);
 		warningList.addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent arg0) {
 				Meeting meeting = ((Notification) warningList
 						.getSelectedValue()).getMeeting();
 				appointOverView = new AppointmentOverView(meeting);
 				appointOverView.getPanel();
 			}
 		});
 		
 	}
 	
 	private void filList(){
 		listModel.clear();
 		for(int i = 0; i < notifications.size(); i++){
 			listModel.addElement(notifications.get(i));
 		}
 	}
 
 	public JPanel getPanel() {
 		return varselPanel;
 	}
 
 	public void propertyChange(PropertyChangeEvent evt) {
 		switch (evt.getPropertyName()) {
 		case CalendarModel.NOTIFICATIONS_CHANGED_Property:
 			System.out.println("notifications changed!");
 			System.out.println(notifications = calendarModel.getUnansweredNotificationsOfUser());
 			filList();
 			break;
 		}
 	}
 }
