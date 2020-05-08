 package controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JFrame;
 
 import view.AdminForm;
 import view.CreateEventForm;
 import view.CredentialsForm;
 import view.MainForm;
 import model.Model;
 
 /**
  * This class handles what happens when the various buttons are pressed on the main form
  * @author Trevor Hodde
  */
 public class ButtonController implements ActionListener {
 	/* The following numbers are used to determine different buttons throughout the application */
 	final int MODERATOR_BUTTON_VALUE = 0;
 	final int USER_BUTTON_VALUE = 1;
 	final int ADMIN_BUTTON_VALUE = 2;
 	final int EXIT_BUTTON_VALUE = 3;
 	boolean isValid;
 	
 	/** The box being controlled. */
 	final int number;
 	
 	/** model being manipulated. */
 	Model model;
 	
 	/** view under management. */
 	JFrame frame;
 	
 	/** Constructor records all information. */
 	public ButtonController(int n, JFrame f) {
 		model = Model.getModel();
 		frame = f;
 		number = n;
 	}
 
 	/** Take action when pressing a button. */
 	public void actionPerformed(ActionEvent e) {
 		switch(number) {
 		case MODERATOR_BUTTON_VALUE:
 			//handle button clicks that have to do with the moderator
 			setupEventOptions();
 			break;
 		case USER_BUTTON_VALUE:
 			//handle button clicks that have to do with the user
			//isValid = checkValidId();
			loadCredentialsForm();
 			model.getDecisionLinesEvent().eventId = ((MainForm)frame).getTextField();
 			break;
 		case ADMIN_BUTTON_VALUE:
 			//handle button clicks that have to do with the administrator
 			loadAdminForm();
 			break;
 		case EXIT_BUTTON_VALUE:
 			//Handle Exit buttons throughout the application
 			frame.dispose();
 			break;
 		default:
 			break;
 		}
 	}
 	
 	/**
 	 * This method is called when the user button is clicked
 	 * on the MainForm
 	 */
 	public void loadCredentialsForm() {
 		//load up the credentials form
 		CredentialsForm cf = new CredentialsForm(false);
 		cf.setVisible(true);
 	}
 	
 	/**
 	 * This method checks to make sure that the event ID specified by the
 	 * user is a valid ID
 	 * @return boolean
 	 */
 	public boolean checkValidId() {
 		//In here we will check for a valid event ID with the server
 		
 		return true;
 	}
 	
 	/**
 	 * This method is called when the moderator button is clicked
 	 * on the MainForm
 	 */
 	public void setupEventOptions() {
 		//load up create event form for the moderator
 		CreateEventForm cef = new CreateEventForm(model);
 		cef.setVisible(true);
 	}
 	
 	/**
 	 * This stuff happens when the administrator button is clicked
 	 */
 	public void loadAdminForm() {
 		//load up the administrator form
 		AdminForm af = new AdminForm(model);
 		af.setVisible(true);
 	}
 }
