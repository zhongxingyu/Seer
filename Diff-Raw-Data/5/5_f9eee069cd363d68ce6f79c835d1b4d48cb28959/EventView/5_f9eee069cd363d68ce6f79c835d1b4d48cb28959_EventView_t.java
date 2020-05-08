 package no.ntnu.fp.gui;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.util.Date;
 import no.ntnu.fp.model.Employee;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import no.ntnu.fp.model.Employee;
 
 public class EventView extends JFrame{
 	
 	JList participantList;
 	JComponent eventTitle, fromField, toField, participantsField, roomBox, descriptionBox;
 	JButton saveButton, cancelButton, deletebutton, acceptButton, declineButton;
 	JPanel eventPanel;
 	GridBagConstraints gbc;
 	DefaultListModel listModel;
 	ParticipantRenderer renderer;
 	Employee user;
 	
 	public EventView(){
 		gbc = new GridBagConstraints();
 		gbc.insets = new Insets(5, 5, 5, 5);
 //		user = EventController.getEmplyee();
 		eventPanel = new JPanel();
 		eventPanel.setLayout(new GridBagLayout());
 		createPanel();
 		
 		this.add(eventPanel);
 		this.pack();
 		
 
 	}
 	
 	@SuppressWarnings("unused")
 	private void createPanel(){
 		Employee hans = new Employee("Hans", "heihei", new Date(1998,2,2), Employee.Gender.MALE);
 		Employee geir = new Employee("Geir", "heihei", new Date(1998,2,2), Employee.Gender.MALE);
 		Employee bjarne = new Employee("Bjarne", "heihei", new Date(1998,2,2), Employee.Gender.MALE);
 		Employee arne = new Employee("Arne", "heihei", new Date(1998,2,2), Employee.Gender.MALE);
 		renderer = new ParticipantRenderer();
 		listModel = new DefaultListModel();
 		
		participantList = new JList(listModel);
		participantList.setCellRenderer(renderer);
 		
 		listModel.addElement(bjarne);
 		listModel.addElement(hans);
 		listModel.addElement(geir);
 		listModel.addElement(arne);
 		
 		participantList.setPreferredSize(new Dimension(200, 200));
 		
 		//skal sjekke om brukeren er eventmanager
 		if(true){
 			eventTitle = new JTextField("Title", 23);
 			fromField = new JTextField("From", 10);
 			toField = new JTextField("to", 10);
 			roomBox = new JComboBox();
 			descriptionBox = new JTextArea("Description");
 			saveButton = new JButton("Save");
 			cancelButton = new JButton("Cancel");
 			deletebutton = new JButton("Delete");
 			participantsField = new JTextField("Participants", 23);
 			
 			roomBox.setPreferredSize(new Dimension(275, 25));
 			descriptionBox.setPreferredSize(new Dimension(200, 100));
 			
 			gbc.gridx = 0;	gbc.gridy = 7;
 			gbc.gridwidth = 1;
 			gbc.gridheight = 1;
 			eventPanel.add(saveButton, gbc);
 			
 			gbc.gridx = 2;	gbc.gridy = 7;
 			gbc.gridwidth = 1;
 			gbc.gridheight = 1;
 			eventPanel.add(cancelButton, gbc);
 			
 			gbc.gridx = 4;	gbc.gridy = 7;
 			gbc.gridwidth = 1;
 			gbc.gridheight = 1;
 			eventPanel.add(deletebutton, gbc);
 			
 		}
 		
 		else{
 			eventTitle = new JLabel();
 			fromField = new JLabel();
 			toField = new JLabel();
 			roomBox = new JLabel();
 			descriptionBox = new JTextArea();
 			acceptButton = new JButton("Accept");
 			declineButton = new JButton("Decline");
 			participantsField = new JLabel();
 			
 			eventPanel.add(acceptButton);
 			eventPanel.add(declineButton);
 			
 			descriptionBox.setEnabled(false);
 		}
 		gbc.gridx = 0;	gbc.gridy = 0;
 		gbc.gridwidth = 2;
 		eventPanel.add(eventTitle, gbc);
 		
 		gbc.gridx = 0;	gbc.gridy = 1;
 		gbc.gridwidth = 1;
 		gbc.gridheight = 1;
 		eventPanel.add(fromField, gbc);
 		
 		gbc.gridx = 1;	gbc.gridy = 1;
 		gbc.gridwidth = 1;
 		gbc.gridheight = 1;
 		eventPanel.add(toField, gbc);
 		
 		gbc.gridx = 0;	gbc.gridy = 2;
 		gbc.gridwidth = 2;
 		gbc.gridheight = 1;
 		eventPanel.add(roomBox, gbc);
 		
 		gbc.gridx = 0;	gbc.gridy = 3;
 		gbc.gridwidth = 2;
 		eventPanel.add(participantsField, gbc);
 		
 		gbc.gridx = 0;	gbc.gridy = 4;
 		gbc.gridheight = 2;
 		gbc.gridwidth = 2;
 		eventPanel.add(descriptionBox, gbc);
 		
 		gbc.gridx = 3;	gbc.gridy = 0;
 		gbc.gridwidth = 3;
 		gbc.gridheight = 6;
 		eventPanel.add(participantList, gbc);
 	}
 	
 	public static void main(String[] args){
 		JFrame frame = new EventView();
 		frame.setVisible(true);
 		frame.setLocationRelativeTo(null);
 		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
 	}
 
 }
