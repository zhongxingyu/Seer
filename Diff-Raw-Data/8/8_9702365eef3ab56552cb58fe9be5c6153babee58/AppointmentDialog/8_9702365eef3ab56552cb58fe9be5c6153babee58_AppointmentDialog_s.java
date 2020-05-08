 package tim.view.dialog.appointment;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.Observable;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import tim.application.Config;
 import tim.application.GlobalRegistry;
 import tim.application.exception.OperationNotPossibleException;
 import tim.application.exception.PersistanceException;
 import tim.application.exception.ResourceNotFoundException;
 import tim.controller.AbstractController;
 import tim.controller.AppointmentDialogController;
 import tim.model.Appointment;
 import tim.model.Client;
 import tim.model.Element;
 import tim.model.Employee;
 import tim.model.Person;
 import tim.view.Application;
 import tim.view.ParentView;
 
 public class AppointmentDialog extends JDialog implements ActionListener, ParentView {
 	Form form = null;
 	AbstractController controller = new AppointmentDialogController();
 	private JPanel buttonPanel;
 	private JButton btnCancel;
 	private JButton btnSave;
 	private JButton btnDelete;
 	private JPanel errorPanel;
 	private JLabel lblErrorMsg;
 	private String mode = "add";
 
 	public AppointmentDialog(Appointment appointment) {
 		errorPanel = new JPanel();
 		lblErrorMsg = new JLabel(" ");
 		lblErrorMsg.setForeground(Color.RED);
 
 		errorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		errorPanel.add(lblErrorMsg);
 		
 		buttonPanel = new JPanel();
 		btnCancel = new JButton(Config.RESSOURCE_BUNDLE.getString("dialogCancel"));
 		btnDelete = new JButton(Config.RESSOURCE_BUNDLE.getString("dialogDelete"));
 		
 		btnCancel.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				close();
 			}
 		});
 		
 		form = new Form();
 		form.setData(appointment);
 		try {
 			if (appointment.getClient() != null) {
 				form.setClients(controller.getAll("client"), (int)appointment.getClient().getId());
 				mode = "edit";
 			} else {
 				form.setClients(controller.getAll("client"), null);
 				btnDelete.setEnabled(false);
 			}
 		} catch (PersistanceException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ResourceNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		btnSave = new JButton(Config.RESSOURCE_BUNDLE.getString("dialogSave"));
 		
 		btnSave.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (check((Appointment) form.getData())) {
 					Appointment appointment =  (Appointment) form.getData();
 					boolean ret;
 					try {
 						ret = ((AppointmentDialogController)controller).checkAvailability(appointment);
 						if (ret) {
 							save(mode, appointment);
 							close();
 						}
 						else {
 							notAvailable();
 						}
 					} catch (PersistanceException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (ParseException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}	
 				}
 			}
 		});
 		
 
 		
 		btnDelete.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (check((Appointment) form.getData())) {
 					save("delete", form.getData());
 					close();	
 				}
 			}
 		});
 		
 
 		
 		
 		
 		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		buttonPanel.add(btnCancel);
 		buttonPanel.add(btnDelete);
 		buttonPanel.add(btnSave);
 
 		setTitle("TIM - Appointment");
 
 		add(errorPanel, BorderLayout.NORTH);
 		add(form, BorderLayout.CENTER);
 		add(buttonPanel, BorderLayout.SOUTH);
		System.out.println(mode);
 	}
 
 	private boolean check(Appointment appointment) {
 		boolean ret = true;
 		if (appointment == null) {
 			lblErrorMsg.setText(Config.RESSOURCE_BUNDLE.getString("dialogMessages"));
 			ret = false;
 		}
 		return ret;
 	}
 	
 	private void close() {
 		setVisible(false);
 		dispose();
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 
 		
 	}
 
 	@Override
 	public void save(String action, Object value) {
 		lblErrorMsg.setText(" ");
 		
 		try {
 			controller.save(action, (Appointment) value);			
 		} catch (ClassCastException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (PersistanceException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ResourceNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (OperationNotPossibleException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void notAvailable() {
 		JOptionPane.showMessageDialog(
 				this,
 				Config.RESSOURCE_BUNDLE.getString("dialogErrorAppointment"));
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		close();
 	}
 }
