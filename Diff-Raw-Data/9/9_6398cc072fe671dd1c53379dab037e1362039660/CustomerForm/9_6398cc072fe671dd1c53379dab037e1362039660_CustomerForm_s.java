 package gui.forms.add;
 
 import gui.forms.util.ComboKeyHandler;
 import gui.forms.util.FormDropdown;
 import gui.forms.util.FormField;
 import gui.popup.SuccessPopup;
 
 import java.awt.Color;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 import common.entity.profile.Person;
 import common.manager.Manager;
 
 import util.ErrorLabel;
 import util.FormCheckbox;
 import util.SimplePanel;
 import util.Tables;
 import util.Values;
 import util.soy.SoyButton;
 
 public class CustomerForm extends SimplePanel {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1337878469442864579L;
 	private ArrayList<FormField> fields = new ArrayList<FormField>();
 	private SoyButton clear, save;
 	private FormDropdown acctType;
 	private JComboBox employeeCombo;
 	private JTextField employeesField;
 	private DefaultComboBoxModel model;
 
 	private ErrorLabel error;
 	private String msg;
 	
 	private JPanel panel;
 	private JScrollPane scrollPane;
 
 	private final int num = Tables.customerFormLabel.length;
 
 	public CustomerForm() {
 		super("Add Customer");
 		addComponents();
 
 		// Values.accountForm = this;
 	}
 
 	private void addComponents() {
 		// TODO Auto-generated method stub
 		clear = new SoyButton("Clear");
 		save = new SoyButton("Save");
 		
 		panel = new JPanel();
 		panel.setLayout(null);
 		panel.setOpaque(false);
 
 		scrollPane = new JScrollPane();
 
 		error = new ErrorLabel();
 
 		int ctr = 0;
		for (int i = 0, y = 0, x1 = 32; i < num; i++, y += 54) {
 
 				fields.add(new FormField(Tables.customerFormLabel[i], 100, Color.white, Color.gray));
 				fields.get(i).setBounds(x1, 12 + y, 200, 25);
 				panel.add(fields.get(i));
 		}
 
 		clear.setBounds(149, 287, 80, 30);
 		save.setBounds(39, 287, 80, 30);
 
 		error.setBounds(68, 300, 170, 22);
 
 		clear.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 
 				clearFields();
 			}
 		});
 
 		save.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 
 				if(isValidated()){
 				Person p = new Person(fields.get(1).getText(), fields.get(2).getText(), fields.get(0).getText(), fields.get(3).getText(), fields.get(4)
 						.getText(), true);
 				try {
 					Manager.employeePersonManager.addPerson(p);
 					Values.centerPanel.changeTable(Values.CUSTOMERS);
 					new SuccessPopup("Add").setVisible(true);
 					clearFields();
 					
 					Values.salesForm.refreshCustomer(true);
 //					Values.supplierForm.refreshCustomer(true);
 					Values.accountReceivablesForm.refreshCustomer(true);
 					Values.discountForm.refreshDropdown(true);
 					Values.arPaymentForm.refreshDropdown(true);
 					Values.caPaymentForm.refreshDropdown(true);
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				}else
 					error.setToolTip(msg);
 			}
 		});
 
 		panel.add(clear);
 		panel.add(save);
 
 		
 		scrollPane.setViewportView(panel);
 		scrollPane.setOpaque(false);
 		scrollPane.getViewport().setOpaque(false);
 		scrollPane.setBorder(BorderFactory.createEmptyBorder());
 
 		scrollPane.setBounds(10, 42, 270, 340);
 
 		add(error);
 		add(scrollPane);
 
 	}
 
 	private void clearFields() {
 		for (int i = 0; i < fields.size(); i++)
 			fields.get(i).setText("");
 
 		error.setToolTip("");
 	}
 
 	private boolean isValidated() {
 
 		if(fields.get(0).getText().equals("")){
 			
 			msg = "Last Name is required";
 			
 			return false;
 		}
 		
 		if(fields.get(1).getText().equals("")){
 			
 			msg = "First Name is required";
 			
 			return false;
 		}
 
 		return true;
 	}
 
 	public void refreshDropdown() {
 		try {
 			model = new DefaultComboBoxModel();
 			acctType = new FormDropdown();
 			acctType.setModel(model);
 		} catch (Exception e2) {
 			e2.printStackTrace();
 		}
 	}
 }
