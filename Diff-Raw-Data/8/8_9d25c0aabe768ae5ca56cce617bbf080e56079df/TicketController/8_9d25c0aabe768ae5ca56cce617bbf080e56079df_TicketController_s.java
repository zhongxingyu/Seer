 package de.articmodding.TroubleTicket;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 
 public class TicketController {
 	
 	private TicketView mainView;
 	private TicketNewView newTicketView;
 	private TicketEditView editTicketView;
 	private SessionFactory sessionFactory;
 	private TicketModel model;
 	
 	public TicketController() {
 		mainView = new TicketView();
 		newTicketView = new TicketNewView();
 		editTicketView = new TicketEditView();
 		model = new TicketModel();		
 		
 		sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
 		
 		addListener();
 		addTableModel();
 		addNewTicketListener();
 		addEditTicketListener();
 		loadOptions();
 
 		loadTickets();
 	}
 
 	public void showView() {
 		mainView.setVisible(true);
 	}
 	
 	private void loadOptions() {
 		for(String status : Ticket.statusList) {
 			editTicketView.addStatus(status);
 		}
 		for(Integer i = 0; i < 5; i++) {
 			editTicketView.addPrioritaet(i);
 		}
 	}
 	
 	private boolean loadTicket(int ticket) {
 		try {
 			Ticket selectedTicket = model.getTicket(ticket);
 			editTicketView.reset();
 			editTicketView.setPrioritaet(selectedTicket.getPrioritaet());
 			int status = selectedTicket.getStatus();
 			if(status == 0) {status = 1;} // "Neu"-Tickets werden "Angenommen"
 			editTicketView.setStatus(status);
 			editTicketView.setErsteller(selectedTicket.getErsteller());
 			editTicketView.setErstellerEmail(selectedTicket.getErstellerEMail());
 			editTicketView.setBetreff(selectedTicket.getBetreff());
 			editTicketView.setText(selectedTicket.getText());
 
 			addReplyTableModel();
 			
 			return true;
 		} catch (Error e) {
 			System.out.println("Ticket konnte nicht geladen werden - Fehlermeldung: " + e.getMessage());
 		}
 		return false;
 	}
 
 	private void saveTickets() {
 		List<Ticket> tickets = model.getTicketList();		
 		Session session = sessionFactory.openSession();
 		for(Ticket t : tickets) {
 			
 			List<TicketReply> replies = t.getReplies();
 			for(TicketReply r : replies) {
 				if(r != null) {
 					session.beginTransaction();
 					session.saveOrUpdate(r);
 					session.getTransaction().commit();
 				}
 			}
			session.beginTransaction();
			session.saveOrUpdate(t);
			session.getTransaction().commit();
 		}
 		session.close();
 	}
 
 	@SuppressWarnings("unchecked")
 	private void loadTickets() {
 		Session session = sessionFactory.openSession();
 		session.beginTransaction();
 		ArrayList<Ticket> tickets = new ArrayList<Ticket>();
 		try {
 			tickets.addAll(session.createQuery( "from Ticket" ).list());
 		} catch(Exception e) {
 			System.out.println("Tickets konnten nicht geladen werden! Fehler: "+e.getMessage() +e.getStackTrace().toString());
 		}
 		session.getTransaction().commit();
 		model.setTicketList(tickets);
 		session.close();
 	}
 	
 	/*
 	 * Main View Functions
 	 */
 	
 	private void addListener() {		
 		mainView.setNeuActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				mainView.setEnabled(false);
 				newTicketView.setVisible(true);
 			}
 		});		
 		
 		mainView.setBearbeitenActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {				
 				int selectedTicket = mainView.getSelectedTableRow();
 				
 				if(selectedTicket > -1) {
 					loadTicket(selectedTicket);
 					
 					mainView.setEnabled(false);
 					editTicketView.setVisible(true);
 				}else{
 					JOptionPane.showMessageDialog(null, "Du hast kein Ticket aus der Tabelle ausgewählt", "Kein Ticket!", JOptionPane.NO_OPTION);
 				}
 			}
 		});
 		
 		mainView.setBeendenActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(e.getActionCommand() == "save") {
 					saveTickets();		
 				}else{
 					mainView.dispose();
 				}
 			}
 		});
 	}
 
 	private void addTableModel() {		
 		mainView.setTableModel(new TicketTableModel(model));		
 	}
 
 	/*
 	 * New Ticket View Functions
 	 */	
 	private void addNewTicketListener() {	
 		newTicketView.setOkButtonListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Ticket neuesTicket = new Ticket(
 						newTicketView.getErsteller(), 
 						newTicketView.getErstellerEmail(), 
 						newTicketView.getBetreff(), 
 						newTicketView.getText()
 						);
 				model.addTicket(neuesTicket);
 				
 				mainView.refreshTable();
 				
 				mainView.setEnabled(true);
 				newTicketView.reset();
 				newTicketView.dispose();
 			}
 		});
 		
 		newTicketView.setExitButtonListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(e.getActionCommand() == "enable") {
 					mainView.setEnabled(true);
 					mainView.toFront();
 				}else{
 					newTicketView.dispose();
 				}
 			}
 		});
 	}
 	
 	/*
 	 * Edit Ticket View Functions
 	 */
 	private void addEditTicketListener() {
 		editTicketView.setOkButtonListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {		
 				int selectedTicket = mainView.getSelectedTableRow();
 				if(selectedTicket > -1) {			
 					Ticket ticket = model.getTicket(selectedTicket);
 					ticket.setPrioritaet(editTicketView.getPrioritaet());
 					ticket.setStatus(editTicketView.getStatus());
 					
 					if(!editTicketView.getReplyBetreff().equals("")) {
 						TicketReply reply = new TicketReply( 
 							editTicketView.getReplyBetreff(),
 							editTicketView.getReplyText()
 							);
 						ticket.addReply(reply);
 					}
 					
 					mainView.refreshTable();
 					mainView.setEnabled(true);
 					editTicketView.dispose();
 				}
 			}
 		});
 		
 		editTicketView.setExitButtonListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				mainView.setEnabled(true);
 				mainView.toFront();
 				if(e.getActionCommand() != "enable") {
 					editTicketView.dispose();
 				}
 			}
 		});
 	}
 	
 	private void addReplyTableModel() {
 		int selectedTicket = mainView.getSelectedTableRow();
 		final Ticket ticket = model.getTicket(selectedTicket);
 		final TicketReplyTableModel replyTableModel = new TicketReplyTableModel(ticket);
 		editTicketView.setTableModel(replyTableModel);
 		editTicketView.setTableSelectionListener(new ListSelectionListener() {			
 			public void valueChanged(ListSelectionEvent e) {
 				// quasi onClick-Event
 				if(e.getValueIsAdjusting()) {
 					ListSelectionModel model = (ListSelectionModel) e.getSource();
 					if(!model.isSelectionEmpty()) {
 						int selectedRow = model.getMinSelectionIndex();
 						TicketReply reply = replyTableModel.getReply(selectedRow);
 						editTicketView.setReplyBetreff(reply.getBetreff());
 						editTicketView.setReplyText(reply.getText());
 						
 						if(selectedRow == 0) {
 							editTicketView.setReplyBetreffEtidable(true);
 							editTicketView.setReplyTextEtidable(true);
 						}else{
 							editTicketView.setReplyBetreffEtidable(false);
 							editTicketView.setReplyTextEtidable(false);							
 						}
 					}
 				}
 			}
 		});
 	}
 }
