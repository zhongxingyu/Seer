 package controllers;
 
 import models.Organizer;
 import views.gui.EventDetails;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 
 public class EventDetailsController extends Controller {
 	private EventDetails frame;
 
 	public EventDetailsController(EventDetails frame) {
 		this.frame = frame;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		JOptionPane alert = new JOptionPane("Are you sure you want to remove this event?",
 				JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
 		alert.createDialog(frame, "").setVisible(true);
		if(alert.getValue().equals(1))
 			return;
 		frame.getEvent().delete();
 		Organizer.getInstance().update();
 		Organizer.getInstance().notifyObservers(Organizer.getInstance().getCurrentUser().getUserProfile().getState());
 		frame.dispose();
 	}
 }
