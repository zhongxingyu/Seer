 package no.ntnu.fp.gui;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Date;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import no.ntnu.fp.gui.timepicker.TimePickableFieldListener;
 import no.ntnu.fp.model.Appointment;
 import no.ntnu.fp.model.CalendarEntry;
 import no.ntnu.fp.model.Location;
 import no.ntnu.fp.model.Person;
 import no.ntnu.fp.model.Place;
 import no.ntnu.fp.model.Room;
 import no.ntnu.fp.net.network.client.CommunicationController;
 import no.ntnu.fp.util.GridBagHelper;
 import no.ntnu.fp.util.TimeLord;
 
 public class AppointmentPanel extends JFrame implements PropertyChangeListener {
 	private JLabel appointment, description, startTime, endTime, location;
 	private JTextField descComp, startComp, endComp;
 	private JTextField locComp;
 	private JPanel panel;
 	private PlacePickerPanel plPickPanel;
 	
 	private JButton save, delete;
 	
 	//f det ryddig i boksen
 	protected GridBagLayout grid;
 	protected GridBagConstraints constraints;
 	
 	private Appointment model;
 	
 	public AppointmentPanel(Appointment appmnt) {
 		this();
 		setModel(appmnt);
 		plPickPanel.setModel(appmnt);
 	}
 	public AppointmentPanel() {
 		appointment = new JLabel("Avtale");
 		description = new JLabel("Beskrivelse");
 		startTime = new JLabel("Starttid");
 		endTime = new JLabel("Sluttid");
 		location = new JLabel("Sted");
 		panel = new JPanel();
 		
 		plPickPanel = new PlacePickerPanel();
 		plPickPanel.addPropertyChangeListener(this);
 		
 		descComp = new JTextField(10);
 		startComp = new JTextField(10);
 		endComp = new JTextField(10);
 		locComp = new JTextField(10);
 		
 		save = new JButton("Lagre");
 		delete = new JButton("Slett");
 		
 		grid = new GridBagLayout();
 		constraints = new GridBagConstraints();
 		
 		panel.setLayout(grid); // gjr at man faktisk endrer noe(det synes)
 		
 		constraints.gridwidth = constraints.RELATIVE;
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		panel.add(appointment, constraints);
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		panel.add(description, constraints);
 		constraints.gridx = 0;
 		constraints.gridy = 2;
 		panel.add(startTime, constraints);
 		constraints.gridx = 0;
 		constraints.gridy = 3;
 		panel.add(endTime, constraints);
 		constraints.gridx = 0;
 		constraints.gridy = 4;
 		panel.add(location, constraints);
 		constraints.gridx = 3;
 		constraints.gridy = 1;
 		panel.add(descComp, constraints);
 		constraints.gridx = 3;
 		constraints.gridy = 2;
 		panel.add(startComp, constraints);
 		constraints.gridx = 3;
 		constraints.gridy = 3;
 		panel.add(endComp, constraints);
 		constraints.gridx = 3;
 		constraints.gridy = 4;
 		panel.add(locComp, constraints);
 		constraints.gridx = 0;
 		constraints.gridy = 6;
 		panel.add(save, constraints);
 		constraints.gridx = 3;
 		constraints.gridy = 6;
 		panel.add(delete, constraints);
 		
 		constraints.gridwidth = constraints.REMAINDER;
 		constraints.gridheight = constraints.RELATIVE;
 		panel.add(plPickPanel, GridBagHelper.setConstraints(constraints, 0, 5));
 		
 		add(panel);
 		
 		descComp.addKeyListener(new KeyAdapter(){
 			public void keyReleased(KeyEvent e){
 				model.setDescription(descComp.getText());
 			}
 		});
 		
 		startComp.addKeyListener(new KeyAdapter(){
 			public void keyReleased(KeyEvent e){
 				model.setStartDate(TimeLord.parseDate(startComp.getText()));
 			}
 		});
 		startComp.addFocusListener(new TimePickableFieldListener(startComp, this));
 		endComp.addFocusListener(new TimePickableFieldListener(endComp, this));
 		
 		endComp.addKeyListener(new KeyAdapter() {
 			public void keyReleased(KeyEvent e) {
 				model.setEndDate(TimeLord.parseDate(endComp.getText()));
 			}
 		});
 		
 		
 		locComp.addKeyListener(new KeyAdapter() {
 			public void keyReleased(KeyEvent e) {
 				//allow entering of text
 				//update location accordingly
 				
 				model.setLocation(new Place(-1, locComp.getText()));
 			}
 		});
 		
 		
 		//private JButton save, delete:
 		this.save.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				//button is clicked, run code that will save the model
 				CommunicationController c = CommunicationController.getInstance();
 				if (model != null) {
 					if (model.getOwner() == null) 
 						model.setOwner(c.getUser());
 					c.saveAppointment(model);
 				}
 				//close window if successfull.
 			}
 		});
 		this.delete.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				//button is clicked DELETE EVERYTHING.
 				//wait _where_ are we keeping the code to send the DB-req to delete something?
 				CommunicationController c = CommunicationController.getInstance();
 				if (model != null) {
 					c.deleteAppointment(model);
 				}
 				//close window.
 			}
 		});
 	}
 	
 	 private void updatePanel() {
 	       if (model != null) {
 	    	   descComp.setText(model.getDescription());
 	    	   startComp.setText(TimeLord.formatDate(model.getStartDate()));
 	    	   endComp.setText(TimeLord.formatDate(model.getEndDate()));
 	    	   locComp.setText(
 	    			   (model.getLocation() != null) ? 
 	    			    model.getLocation().getID()+"" :
 	    				"");
	    	   locComp.setText((model.getLocation() != null) ?
	    			   			model.getLocation().getDescription() :
	    			   				"");
 	    	   plPickPanel.updatePanel();
 	       }
 	    }
 	 
 	   public void setModel(Appointment app) {
    		if (app != null) {
    			if (model != null) {
    				model.removePropertyChangeListener(this);
    			}
    			model = app;
    			model.addPropertyChangeListener(this);
    			plPickPanel.setLocation(model.getLocation());
    			
    			updatePanel();
    		}
     }
 	
 	public static void main(String[] args){
 		
 		Appointment app = new Appointment(new Date(0, 0, 0), new Date(2012, 05, 03),
 				"Kill the batman", 35);
 		app.setLocation(new Place(33, "Gotham City"));
 		
 		AppointmentPanel frame = new AppointmentPanel(app);
 		
 		frame.setModel(app);
 		
 		frame.setLocationRelativeTo(null); //center a frame
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setVisible(true); //display the frame
 		
 		frame.pack();
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		if(evt.getPropertyName() == Appointment.DESC_PROPERTY) {
 			descComp.setText(model.getDescription());
 		}
 		if(evt.getPropertyName() == Appointment.END_PROPERTY){
 			endComp.setText(TimeLord.formatDate(model.getEndDate()));
 		}
 		if(evt.getPropertyName() == Appointment.START_PROPERTY){
 			startComp.setText(TimeLord.formatDate(model.getStartDate()));
 		}
 		if(evt.getPropertyName() == Appointment.LOC_PROPERTY){
 			if (model.getLocation() != null)
 				locComp.setText(model.getLocation().getDescription());
 		}
 		if (evt.getPropertyName() == PlacePickerPanel.LOCATIONC_PROPERTY) {
 			model.setLocation((Location) evt.getNewValue());
 		}
 	}
 
 }
