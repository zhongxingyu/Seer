 package no.ntnu.fp.client.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import no.ntnu.fp.client.gui.EventView;
 import no.ntnu.fp.common.model.Employee;
 import no.ntnu.fp.common.model.Event;
 import no.ntnu.fp.common.model.Room;
 import no.ntnu.fp.server.storage.db.EventHandler;
 
 public class EventViewController implements PropertyChangeListener, KeyListener, MouseListener, ComponentListener, ActionListener {
 	
 	private EventView eventView;
 	private Employee currentUser;
 	private Calendar fromDate, toDate;
 	private Event event;
 	private String toHour, toMinute, toTime, fromHour, fromMinute, fromTime;
 	private ArrayList<String> popList, popListFound;
 	
 	
 	public EventViewController(Employee currentUser, EventView view){
 		this.currentUser = currentUser;
 		this.eventView = view;
 		
 		event = new Event(currentUser);
 
         toDate = Calendar.getInstance();
         fromDate = Calendar.getInstance();
         
         toDate.setTime(event.getDateTo());
         fromDate.setTime(event.getDateFrom());
 
 		popList = new ArrayList<String>();
 		popListFound = new ArrayList<String>();
 
 		toHour = "0";
 		toMinute = "0";
 
 		fromHour = "0";
 		fromMinute = "0";
 
 		eventView = new EventView();
 		eventView.setVisible(false);
 		eventView.addComponentListener(this);
 
 		eventView.getCalendarToPopPanel().getHourTextField().addKeyListener(this);
 		eventView.getCalendarToPopPanel().getMinuteTextField().addKeyListener(this);
 		eventView.getCalendarFromPopPanel().getHourTextField().addKeyListener(this);
 		eventView.getCalendarFromPopPanel().getMinuteTextField().addKeyListener(this);
 		eventView.getSaveButton().addActionListener(this);
 		eventView.getCancelButton().addActionListener(this);
 		eventView.getDeleteButton().addActionListener(this);
 		eventView.getAcceptButton().addActionListener(this);
 		eventView.getDeclineButton().addActionListener(this);
 		eventView.getDeletePersonButton().addActionListener(this);
 
 		eventView.getFromField().addMouseListener(this);
 		eventView.getToField().addMouseListener(this);
 
 		eventView.getTitleField().addMouseListener(this);
 		eventView.getDescriptionArea().addMouseListener(this);
 		eventView.getParticipantField().addMouseListener(this);
 
 		eventView.getParticipantField().addKeyListener(this);
 		popList = new ArrayList<String>();
 
 
 
 		for (int i = 0; i < popList.size(); i++) {
 			eventView.getPopListModel().addElement(popList.get(i));
 		}
 
 		eventView.getCalendarToPopPanel().getOverviewCalendarPanel().addPCL(new PropertyChangeListener() {
 
 			@Override
 			public void propertyChange(PropertyChangeEvent evt) {
 				Calendar cal = (Calendar) evt.getNewValue();
 				toDate.set(Calendar.YEAR, cal.get(Calendar.YEAR));
 				toDate.set(Calendar.MONTH, cal.get(Calendar.MONTH));
 				toDate.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
 				event.setDateTo(toDate.getTime());
 				eventView.getToField().setText(toDate.getTime().toString());
 			}
 		});
 
 		eventView.getCalendarFromPopPanel().getOverviewCalendarPanel().addPCL(new PropertyChangeListener() {
 
 			@Override
 			public void propertyChange(PropertyChangeEvent evt) {
 				Calendar cal = (Calendar) evt.getNewValue();
 				fromDate.set(Calendar.YEAR, cal.get(Calendar.YEAR));
 				fromDate.set(Calendar.MONTH, cal.get(Calendar.MONTH));
 				fromDate.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
 				event.setDateTo(fromDate.getTime());
 				eventView.getFromField().setText(fromDate.getTime().toString());
 			}
 		});
 
 		eventView.getParticipantPopList().addListSelectionListener(new ListSelectionListener() {
 
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				if(eventView.getParticipantPopList().getSelectedIndex() != -1){
 					eventView.getListModel().addElement(eventView.getParticipantPopList().getSelectedValue());
 					eventView.getPopListModel().removeElement(eventView.getParticipantPopList().getSelectedValue());
 					eventView.getParticipantField().grabFocus();
 				}
 			}
 		});
     }
 
 	public void setCurrentUser(Employee currentUser){
 		this.currentUser = currentUser;
 	}
 
 	public void showEvent(){
 
 	}
 
 	public void showEvent(Event event){
 		eventView.setLocationRelativeTo(ClientApplication.getMainViewController().getMainView());
 		setEvent(event);
 		eventView.setVisible(true);
 	}
 
 	public void setVisible(boolean visible){
 		eventView.setVisible(visible);
 	}
 
 	public void setEvent(Event event){
 		this.event = event;
 		eventView.getTitleField().setText(event.getTitle());
 		eventView.setFromField(event.getDateFrom().toString());
 		eventView.setToField(event.getDateTo().toString());
 		eventView.setDescriptionArea(event.getDescription());
 
         eventView.removeAllParticipants();
 		for (int i = 0; i < event.getParticipants().size(); i++) {
 			eventView.addParticipant(event.getParticipants().get(i));
 		}
 	}
 
 //
 //	public Event getEvent (int ID){
 //		try {
 //			return EventHandler.getEvent(ID);
 //		} catch (SQLException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 //
 //		return null;
 //
 //	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 		if (e.getSource() == eventView.getCalendarToPopPanel().getHourTextField()) {
			toDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(eventView.getCalendarToPopPanel().getHourTextField().getText()));
 		}
 		else if (e.getSource() == eventView.getCalendarToPopPanel().getMinuteTextField()) {
 			toDate.set(Calendar.MINUTE, Integer.parseInt(eventView.getCalendarToPopPanel().getMinuteTextField().getText()));
 		}
 		if (e.getSource() == eventView.getCalendarFromPopPanel().getHourTextField()) {
			fromDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(eventView.getCalendarFromPopPanel().getHourTextField().getText()));
 		}
 		else if (e.getSource() == eventView.getCalendarFromPopPanel().getMinuteTextField()) {
 			fromDate.set(Calendar.MINUTE, Integer.parseInt(eventView.getCalendarFromPopPanel().getMinuteTextField().getText()));
 		}
 		else if(e.getSource() == eventView.getParticipantField()){
 			for (int i = 0; i < popList.size(); i++) {
 				if(eventView.getParticipantField().getText().length() <= 1){
 					if(popList.get(i).charAt(eventView.getParticipantField().getText().length() - 1) == eventView.getParticipantField().getText().charAt(eventView.getParticipantField().getText().length() - 1)){
 						popListFound.add(popList.get(i));
 					}
 				}
 			}
 			for (int y = 0; y < popListFound.size(); y++) {
 				for (int i = 0; i < eventView.getPopListModel().size(); i++) {
 					if(i< eventView.getPopListModel().size()){
 						if(eventView.getPopListModel().get(i) != popListFound.get(y)){
 							eventView.getPopListModel().remove(i);
 							i--;
 						}
 					}
 				}
 			}
 		}
 		eventView.getToField().setText(toDate.getTime().toString());
 		eventView.getFromField().setText(fromDate.getTime().toString());
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 
 		if (e.getSource() == eventView.getTitleField()) {
 			if (eventView.getTitleField().getText().equals("Title")) {
 				eventView.getTitleField().setText("");
 			}
 		}
 		else if(e.getSource() == eventView.getFromField()){
 			eventView.getFromPop().show(eventView.getFromField(), 0, 30);
 			if (eventView.getFromField().getText().equals("From")) {
 				eventView.getFromField().setText("");
 			}
 			eventView.getFromField().grabFocus();
 		}
 		else if(e.getSource() == eventView.getToField()){
 			eventView.getToPop().show(eventView.getToField(), 0, 30);
 			if (eventView.getToField().getText().equals("To")) {
 				eventView.getToField().setText("");
 			}
 			eventView.getToField().grabFocus();
 		}
 		else if (e.getSource() == eventView.getParticipantField()) {
 			eventView.getParticipantPop().show(eventView.getParticipantField(), 0, 30);
 			eventView.getParticipantField().setText("");
 			eventView.getParticipantField().grabFocus();
 		}
 		else if(e.getSource() == eventView.getTitleField()){
 			eventView.getTitleField().setText("");
 		}
 		else if(e.getSource() == eventView.getDescriptionArea()){
 			eventView.getDescriptionArea().setText("");
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void componentHidden(ComponentEvent arg0) {
 
 
 	}
 
 	@Override
 	public void componentMoved(ComponentEvent arg0) {
 
 //		view.getFromPop().show(view.getFromField(), 0, 30);
 //		view.getToPop().show(view.getToField(), 0, 30);
 //		view.getParticipantPop().show(view.getParticipantField(), 0, 30);
 	}
 
 	@Override
 	public void componentResized(ComponentEvent arg0) {
 
 //		view.getFromPop().show(view.getFromField(), 0, 30);
 //		view.getToPop().show(view.getToField(), 0, 30);
 //		view.getParticipantPop().show(view.getParticipantField(), 0, 30);
 	}
 
 	@Override
 	public void componentShown(ComponentEvent arg0) {
 
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 
 		if(e.getSource() == eventView.getSaveButton()){
 			this.setVisible(false);
 			event.setTitle(eventView.getTitleField().getText());
 //			event.setDateFrom();
 //			event.setDateTo(dateTo);
 			event.setDescription(eventView.getDescriptionArea().getText());
 			ArrayList<Employee> participants = new ArrayList<Employee>();
 			for (int i = 0; i < eventView.getListModel().size(); i++) {
 				participants.add((Employee) eventView.getListModel().get(i));
 			}
 			event.setParticipants(participants);
 			event.setRoom((Room) eventView.getRoomBox().getSelectedItem());
 
 	        event.save();
 		}
 		else if (e.getSource() == eventView.getCancelButton()) {
 			this.setVisible(false);
 		}
 		else if (e.getSource() == eventView.getDeleteButton()) {
 			this.setVisible(false);
 		}
 		else if (e.getSource() == eventView.getDeletePersonButton()) {
 			int temp = eventView.getParticipantList().getSelectedIndex();
 			Employee tempEmployee = (Employee) eventView.getParticipantList().getSelectedValue(); 
 			if(temp > -1){
 				eventView.getPopListModel().addElement(tempEmployee);
 				eventView.getParticipantList().setSelectedIndex(temp - 1);
 				eventView.removeParticipant(temp);
 			}
 		}
 	}
 	
 	
 
 }
