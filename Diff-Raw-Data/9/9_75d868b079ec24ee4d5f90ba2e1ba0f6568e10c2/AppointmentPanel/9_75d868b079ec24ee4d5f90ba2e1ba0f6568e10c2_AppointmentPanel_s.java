 package no.ntnu.fp.gui;
 
 import java.awt.Dimension;
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
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
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
 	
 	private JButton save, delete, cancel;
 	
 	private TimePickableFieldListener startListener;
 	private TimePickableFieldListener endListener;
 	
 	//f� det ryddig i boksen
 	protected GridBagLayout grid;
 	protected GridBagConstraints constraints;
 	
 	private Appointment model;
 	
 
 	public AppointmentPanel(Appointment appmnt) {
 		plPickPanel = new PlacePickerPanel();
 		
 		
 		appointment = new JLabel("Avtale");
 		description = new JLabel("Beskrivelse");
 		startTime = new JLabel("Starttid");
 		endTime = new JLabel("Sluttid");
 		location = new JLabel("Stedsnavn");
 		panel = new JPanel();
 		
 		
 		Dimension ps = new Dimension(150, 25);
 		descComp = new JTextField();
 			descComp.setPreferredSize(ps);
 		startComp = new JTextField();
 			startComp.setPreferredSize(ps);
 		endComp = new JTextField();
 			endComp.setPreferredSize(ps);
 		locComp = new JTextField();
 			locComp.setPreferredSize(ps);
 		
 		plPickPanel.addPropertyChangeListener(this);
 		
 		startListener = new TimePickableFieldListener(startComp, this);
 		endListener = new TimePickableFieldListener(endComp, this);
 		
 		save = new JButton("Lagre");
 		delete = new JButton("Slett");
 		cancel = new JButton("Avbryt");
 		grid = new GridBagLayout();
 		constraints = new GridBagConstraints();
 		
 		panel.setLayout(grid); // gj�r at man faktisk endrer noe(det synes)
 		constraints.gridwidth = constraints.REMAINDER;
 		panel.add(appointment, GridBagHelper.setConstraints(constraints, 0, 0));
 		constraints.gridwidth = 1;
 		panel.add(description, GridBagHelper.setConstraints(constraints, 0, 1));
 		panel.add(startTime,   GridBagHelper.setConstraints(constraints, 0, 2));
 		panel.add(endTime,     GridBagHelper.setConstraints(constraints, 0, 3));
 		panel.add(location,    GridBagHelper.setConstraints(constraints, 0, 4));
 		panel.add(save,        GridBagHelper.setConstraints(constraints, 0, 6));
 		panel.add(delete,      GridBagHelper.setConstraints(constraints, 2, 6));
 		panel.add(cancel,      GridBagHelper.setConstraints(constraints, 3, 6));
 		constraints.gridwidth = 2;
 		panel.add(startComp,   GridBagHelper.setConstraints(constraints, 1, 2));
 		panel.add(descComp,    GridBagHelper.setConstraints(constraints, 1, 1));
 		panel.add(endComp,     GridBagHelper.setConstraints(constraints, 1, 3));
 		panel.add(locComp,     GridBagHelper.setConstraints(constraints, 1, 4));
 		
 		constraints.gridwidth = constraints.REMAINDER;
 		constraints.gridheight = constraints.RELATIVE;
 		constraints.fill = constraints.BOTH;
 		panel.add(plPickPanel, GridBagHelper.setConstraints(constraints, 0, 5));
 		
 		
 		add(panel);
 		
 		setModel(appmnt);
 		model.setOwner(CommunicationController.getInstance().getUser());
 		plPickPanel.setModel(this.model);
 		
 		
 		descComp.addKeyListener(new KeyAdapter(){
 			public void keyReleased(KeyEvent e){
 				model.setDescription(descComp.getText());
 			}
 		});
 		
 		startComp.addFocusListener(startListener);
 		startListener.setDate(model.getStartDate());
 		endComp.addFocusListener(endListener);
 		endListener.setDate(model.getEndDate());
 		endComp.getDocument().addDocumentListener(new DocumentListener() {
 			public void removeUpdate(DocumentEvent arg0) {}
 			public void changedUpdate(DocumentEvent arg0){}
 			@Override
 			public void insertUpdate(DocumentEvent arg0)
 			 {
 				if (model != null) {
 					//System.out.println("end date changed");
 					model.setEndDate(TimeLord.parseDate(endComp.getText()));
 				}
 			}});
 		startComp.getDocument().addDocumentListener(new DocumentListener() {
 			public void removeUpdate(DocumentEvent e) {}
 			public void changedUpdate(DocumentEvent e) {}
 			public void insertUpdate(DocumentEvent e)
 			 {
 				if (model != null) {
 					//System.out.println("start date changed");
 					model.setStartDate(TimeLord.parseDate(startComp.getText()));
 				}
 			}});
 		
 		
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
 //					System.out.println(model.getStartDate());
 //					System.out.println(model.getEndDate());
 					int id = c.saveAppointment(model.shallowCopy());
 					model.setID(id);
					CommunicationController.getInstance().getUser().getCalendar().addAppointment(model);
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
 			}
 		});
 		cancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				dispose();				
 			}
 		});
 		panel.setPreferredSize(new Dimension(400, 500));
 		this.setPreferredSize(new Dimension(400, 500));
 		this.setLocationRelativeTo(null); //center a frame
 		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		this.setVisible(true); //display the frame
 		
 		this.pack();
 		
 	}
 	
 	 private void updatePanel() {
 	       if (model != null) {
 	    	   descComp.setText(model.getDescription());
 	    	   startComp.setText(TimeLord.formatDate(model.getStartDate()));
 	    	   endComp.setText(TimeLord.formatDate(model.getEndDate()));
 	    	   endListener.setDate(model.getEndDate());
 	    	   startListener.setDate(model.getStartDate());
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
 			//endComp.setText(TimeLord.formatDate(model.getEndDate()));
 			endListener.setDate(model.getEndDate());
 		}
 		if(evt.getPropertyName() == Appointment.START_PROPERTY){
 			//startComp.setText(TimeLord.formatDate(model.getStartDate()));
 			startListener.setDate(model.getStartDate());
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
