 package gui;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.ListCellRenderer;
 import javax.swing.ListModel;
 import javax.swing.ListSelectionModel;
 import db.User;
 
 
 public class ParticipantListPanel extends JList<CheckListItem> { 
 	
 	DefaultListModel<CheckListItem> model;
 	ArrayList<User> participantList;
 	/*static User user1 = new User(142, "Kathrine Steffensen", "morr4d1erm4nn", "kathrine.steffensen@gmail.com");
 	static User user2 = new User(142, "Petter Astrup", "morr4d1erm4nn", "kathrine.steffensen@gmail.com");
 	static User user3 = new User(142, "Espen Hellerud", "morr4d1erm4nn", "kathrine.steffensen@gmail.com"); //<---- For testing purposes*/
 	
 	public ParticipantListPanel() {
 		model = new DefaultListModel<CheckListItem>();
 		participantList = new ArrayList<User>();
 		setModel(model);
 		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		setCellRenderer(new CheckListRenderer());
 		addMouseListener(new MouseAdapter() {
 			
 			// Handle selection and adding users to the list of participants.
 			
 			public void mouseClicked(MouseEvent event) {
 				JList list = (JList) event.getSource();
 				
 				// Get index of item clicked
 				
 				int index = list.locationToIndex(event.getPoint());
 				CheckListItem item = (CheckListItem)list.getModel().getElementAt(index);
 				
 				// Toggle selected state
				
				item.setSelected(! item.isSelected());
 				
 				if(item.isSelected() == true) {
 					addParticipant(item.getUser());
 					System.out.println(participantList);
 				}
 				if(item.isSelected() == false) {
 					removeParticipant(item.getUser());
 					System.out.println(participantList);
 				}
 				
 				// Repaint cell
 				
				list.repaint(list.getCellBounds(index, index));
 			}
 		});   
 		
 	}
 
 	public static void main(String args[]) {
 		
 		ParticipantListPanel participants = new ParticipantListPanel();
 		/*participants.getModel().addElement(new CheckListItem(user1));
 		participants.getModel().addElement(new CheckListItem(user2));
 		participants.getModel().addElement(new CheckListItem(user3));*/
 		JFrame frame = new JFrame("Participants");
 		Dimension d = new Dimension(400,400);
 		frame.setSize(d);
 		frame.getContentPane().add(new JScrollPane(participants));
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.pack();
 		frame.setVisible(true);
 	}
 	
 	public void addParticipant(User user) {
 		participantList.add(user);
 	}
 	public void removeParticipant(User user) {
 		participantList.remove(user);
 	}
 
 	public DefaultListModel getModel() {
 		return model;
 	}
 	public ArrayList<User> getParticipantList() {
 		return participantList;
 	}
 }
 
 class CheckListItem {
 	protected User user;
 	protected boolean isSelected = false;
 	
 	public CheckListItem(User u) {
 		this.user = u;
 	}
 	
 	public boolean isSelected() {
 	    return isSelected;
 	}
 	
 	public void setSelected(boolean isSelected) {
 		this.isSelected = isSelected;
 	}
 	public String toString() {
 		return user.getName();
 	}
 	public User getUser() {
 		return user;
 	}
 }
 
 class CheckListRenderer extends JCheckBox implements ListCellRenderer {
 	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
 		setEnabled(list.isEnabled());
 		setSelected(((CheckListItem)value).isSelected());
 		setFont(list.getFont());
 		setBackground(list.getBackground());
 		setForeground(list.getForeground());
 		setText(((CheckListItem)value).toString());
 		return this;
 	}
 }
