 package tim.view.dialog.appointment;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Observable;
 
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import tim.application.Config;
 import tim.application.utils.DateHelper;
 import tim.model.Appointment;
 import tim.model.Client;
 import tim.model.Element;
 import tim.model.Employee;
 import tim.view.ChildView;
 import tim.view.ParentView;
 import tim.view.dialog.appointment.AppointmentDialogValidator;
 
 public class Form extends JPanel implements ChildView {
 	private JLabel lblClient;
 	private JComboBox cbClient;
 	private JLabel lblDate;
 	private JTextField txtDate;
 	private JLabel lblBegin;
 	private JComboBox cbBeginH;
 	private JComboBox cbBeginM;
 	private JLabel lblEnd;
 	private JComboBox cbEndM;
 	private JComboBox cbEndH;
 	private JLabel lblDescription;
 	private JTextArea txtDescription;
 	private Employee employee;
 	private long id;
 
 	public Form() {
 		lblClient = new JLabel(
 				Config.RESSOURCE_BUNDLE.getString("dialogClient") + " :");
 		cbClient = new JComboBox();
 
 		lblDate = new JLabel(Config.RESSOURCE_BUNDLE.getString("dialogDate")
 				+ " :");
 		txtDate = new JTextField(10);
 		txtDate.setToolTipText(Config.DATE_FORMAT_SHORT);
 		lblBegin = new JLabel(Config.RESSOURCE_BUNDLE.getString("dialogBegin")
 				+ " :");
 		cbBeginH = new JComboBox();
 		cbBeginM = new JComboBox();
 
 		lblEnd = new JLabel(Config.RESSOURCE_BUNDLE.getString("dialogEnd")
 				+ " :");
 		cbEndH = new JComboBox();
 		cbEndM = new JComboBox();
 
 		lblDescription = new JLabel(
 				Config.RESSOURCE_BUNDLE.getString("dialogDescription") + " :");
 		txtDescription = new JTextArea(10, 20);
 
 		setLayout(new GridBagLayout());
 		GridBagConstraints gbc = new GridBagConstraints();
 
 		gbc.insets = new Insets(5, 5, 5, 5);
 		gbc.gridwidth = 1;
 
 		/*
 		 * Labels
 		 */
 		gbc.anchor = GridBagConstraints.EAST;
 
 		gbc.gridy = 5;
 		gbc.gridx = 0;
 		add(lblDescription, gbc);
 
 		gbc.gridy = 1;
 		gbc.gridx = 0;
 		add(lblClient, gbc);
 
 		gbc.gridy = 2;
 		gbc.gridx = 0;
 		gbc.gridwidth = 1;
 		add(lblDate, gbc);
 
 		gbc.gridy = 3;
 		gbc.gridx = 0;
 		gbc.gridwidth = 1;
 		add(lblBegin, gbc);
 
 		gbc.gridy = 4;
 		gbc.gridx = 0;
 		gbc.gridwidth = 1;
 		add(lblEnd, gbc);
 
 		/*
 		 * Components
 		 */
 		gbc.anchor = GridBagConstraints.WEST;
 
 		gbc.gridy = 3;
 		gbc.gridx = 1;
 		add(cbBeginH, gbc);
 
 		gbc.gridy = 3;
 		gbc.gridx = 2;
 		add(new JLabel(":"), gbc);
 
 		gbc.gridy = 3;
 		gbc.gridx = 3;
 		gbc.gridwidth = GridBagConstraints.REMAINDER;
 		add(cbBeginM, gbc);
 
 		gbc.gridy = 4;
 		gbc.gridx = 1;
 		add(cbEndH, gbc);
 
 		gbc.gridy = 4;
 		gbc.gridx = 2;
 		add(new JLabel(":"), gbc);
 
 		gbc.gridy = 4;
 		gbc.gridx = 3;
 		gbc.gridwidth = GridBagConstraints.REMAINDER;
 		add(cbEndM, gbc);
 
 		gbc.gridy = 5;
 		gbc.gridx = 1;
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.gridwidth = GridBagConstraints.REMAINDER;
 		add(txtDescription, gbc);
 
 		gbc.gridy = 1;
 		gbc.gridx = 1;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.gridwidth = GridBagConstraints.REMAINDER;
 		add(cbClient, gbc);
 
 		gbc.gridy = 2;
 		gbc.gridx = 1;
 		gbc.gridwidth = GridBagConstraints.REMAINDER;
 		add(txtDate, gbc);
 
 		init();
 	}
 
 	public void init() {
 
 		for (int i = Config.CALENDAR_DAY_START; i <= Config.CALENDAR_DAY_END; i++) {
 			cbBeginH.addItem(i);
 			cbEndH.addItem(i);
 		}
 
 		for (int i = 0; i < 60; i += Config.CALENDAR_DAY_INTERVAL) {
 			cbBeginM.addItem(i);
 			cbEndM.addItem(i);
 		}
 	}
 
 	public void setParentView(ParentView view) {
 
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public Object getData() {
 		Appointment appointment = null;
 		Date begin = null;
 		int beginH = 0, endH = 0, beginM = 0, endM = 0;
 
 		beginH = (Integer) cbBeginH.getSelectedItem();
 		endH = (Integer) cbEndH.getSelectedItem();
 		beginM = (Integer) cbBeginM.getSelectedItem();
 		endM = (Integer) cbEndM.getSelectedItem();
 
 		Client client = (Client) cbClient.getSelectedItem();
 
 		if ((AppointmentDialogValidator.dateField(lblDate, txtDate) && AppointmentDialogValidator
 				.startEnd(lblBegin, lblEnd, beginH, beginM, endH, endM))) {
 			try {
 				begin = DateHelper.StringToDate(
 						txtDate.getText() + " " + String.valueOf(beginH) + ":"
 								+ String.valueOf(beginM),
 						Config.DATE_FORMAT_LONG);
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 			Date end = null;
 			try {
 				end = DateHelper
 						.StringToDate(
 								txtDate.getText() + " " + String.valueOf(endH)
 										+ ":" + String.valueOf(endM),
 								Config.DATE_FORMAT_LONG);
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 			if (id > 0) {
 				appointment = new Appointment(id, begin, end, txtDescription.getText(),
 						employee, client);
 			}
 			else {
 			appointment = new Appointment(begin, end, txtDescription.getText(),
 					employee, client);
 			}
 		}
 
 		return appointment;
 	}
 
 	protected void setClients(ArrayList<Element> elements, Integer selectedId) {
 		for (Element element : elements) {
 			cbClient.addItem((Client) element);
 			if (selectedId != null && element.getId() == selectedId) {
 				cbClient.setSelectedItem(element);
 			}
 		}
 	}
 
 	@Override
 	public void setData(Object value) {
 		if (value != null) {
 
 			Appointment appointment = (Appointment) value;
 			this.id = appointment.getId();
 			this.employee = (Employee) appointment.getEmployee();
 			Date begin = appointment.getBegin();
 			Date end = appointment.getEnd();
 
 			String date = "";
 			String description = "";
 			int beginH = 0;
 			int beginM = 0;
 
 			int endH = 0;
 			int endM = 0;
 
 			if (begin != null) {
 				date = DateHelper.DateToString(appointment.getBegin());
 
 				beginH = DateHelper.getHour(appointment.getBegin());
				beginM = DateHelper.getMinutes(appointment.getBegin());
 
 				if (end != null) {
 					endH = DateHelper.getHour(appointment.getEnd());
 					endM = DateHelper.getMinutes(appointment.getEnd());
 				}
 			}
 
 			if (appointment.getDescription() != null) {
 				description = appointment.getDescription();
 			}
 
 			txtDate.setText(date);
 			txtDate.setEditable(false);
 
 			cbBeginH.setSelectedItem(beginH);
 			cbBeginM.setSelectedItem(beginM);
 
 			cbEndH.setSelectedItem(endH);
 			cbEndM.setSelectedItem(endM);
 
 			txtDescription.setText(description);
 		}
 	}
 }
