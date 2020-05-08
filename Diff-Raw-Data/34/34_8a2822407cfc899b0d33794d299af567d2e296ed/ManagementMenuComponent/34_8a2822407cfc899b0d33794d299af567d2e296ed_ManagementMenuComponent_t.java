 package icm.gui.employee;
 
 import icm.client.ICMClient;
 import icm.dao.Employee;
 import icm.dao.EmployeeType;
 import icm.intent.BrowseEmployeesIntent;
import icm.intent.ModifyEmployeeIntent;
 import icm.intent.RegisterEmployeeIntent;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.DefaultListModel;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListCellRenderer;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.JComboBox;
 
 @SuppressWarnings("serial")
 public class ManagementMenuComponent extends JComponent implements Observer
 {
 
 	private DefaultListModel<Employee> model;
 	private JList<Employee> employees;
 	private ICMClient client;
 	private Component scrollpane;
 	private JPanel panel;
 	private JLabel lblName;
 	private JLabel lblSurname;
 	private JLabel lblType;
 	private JLabel lblId;
 	private JLabel lblEmail;
 	private JTextField nameText;
 	private JTextField surnameText;
 	private JTextField idText;
 	private JTextField emailText;
 	private JTextField passText;
 	private JComboBox comboBox;
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public ManagementMenuComponent(final ICMClient client, Employee employee) 
 	{
 		this.client = client;
 		
 		setLayout(new BorderLayout(0, 0));
 		
 		employees = new JList<Employee>();
 		employees.addListSelectionListener(new ListSelectionListener() 
 		{
 			public void valueChanged(ListSelectionEvent arg0) 
 			{
 				if(employees.getSelectedValue()!=null)
 				{
 					nameText.setText(employees.getSelectedValue().getName());
 					surnameText.setText(employees.getSelectedValue().getSurname());
 					comboBox.setSelectedItem(employees.getSelectedValue().getType());
 					//typeText.setText(employees.getSelectedValue().getType().toString());
 					idText.setText(new Integer(employees.getSelectedValue().getEmployeeId()).toString());
 					emailText.setText(employees.getSelectedValue().getEmail());
 					passText.setText(employees.getSelectedValue().getPassword());
 				}
 			}
 		});
 		model = new DefaultListModel<Employee>()
 				{
 					@Override
 					public void addElement(Employee element) 
 					{
 						int i;
 						for( i=0 ; i<getSize() ; ++i )
 						{
 							if( get(i).getEmployeeId() <= element.getEmployeeId())
 								break;
 						}
 						super.add(i, element);
 					}
 				};
 		employees.setModel(model);		
 		
 		employees.setCellRenderer(new ListCellRenderer<Employee>()
 				{
 					public Component getListCellRendererComponent(JList<? extends Employee> list, Employee value, int index, boolean isSelected, boolean cellHasFocus) 
 					{
 						
 						JLabel entry = new JLabel(value.getName()+" "+value.getSurname());
 						
 						entry.setOpaque(true);
 						if( isSelected )
 							entry.setBackground(Color.LIGHT_GRAY);
 						else
 							entry.setBackground( new Color(240,240,240) );
 					    return entry;
 					}
 				});
 		
 		scrollpane = new JScrollPane(employees,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		add(scrollpane, BorderLayout.WEST);
 		
 		JButton refreshButton = new JButton();
 		refreshButton.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		refreshButton.setText("refresh list");
 		refreshButton.setSize(100, 20);
 		refreshButton.addActionListener(
 		new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e) 
 			{
 				refreshList();
 			}
 		});
 		add(refreshButton, BorderLayout.NORTH);
 		
 		
 		JPanel upperPanel = new JPanel();
 		upperPanel.setBounds(10, 11, 404, 208);
 		add(upperPanel,BorderLayout.EAST);
 		upperPanel.setLayout(null);
 		
 		panel = new JPanel();
 		add(panel, BorderLayout.CENTER);
 		
 		lblName = new JLabel("Name:");
 		lblName.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		
 		lblSurname = new JLabel("Surname:");
 		lblSurname.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		
 		lblType = new JLabel("Type:");
 		lblType.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		
 		lblId = new JLabel("ID:");
 		lblId.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		
 		lblEmail = new JLabel("e-mail:");
 		lblEmail.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		
 		nameText = new JTextField();
 		nameText.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		nameText.setColumns(10);
 		
 		surnameText = new JTextField();
 		surnameText.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		surnameText.setColumns(10);
 		
 		idText = new JTextField();
 		idText.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		idText.setEditable(false);
 		idText.setColumns(10);
 		
 		emailText = new JTextField();
 		emailText.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		emailText.setColumns(10);
 		
 		JButton btnAddNew = new JButton("Add New");
 		btnAddNew.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		btnAddNew.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				if(nameText.getText().isEmpty() || surnameText.getText().isEmpty() 
 						|| emailText.getText().isEmpty() || passText.getText().isEmpty())
 				{
 					JOptionPane.showMessageDialog(ManagementMenuComponent.this,"Field empty", "Operation failed", JOptionPane.ERROR_MESSAGE);
 				}
 				else
 				{
 					RegisterEmployeeIntent intent = new RegisterEmployeeIntent();
 					intent.setEmail(emailText.getText());
 					intent.setName(nameText.getText());
 					intent.setSurname(surnameText.getText());
 					intent.setPassword(passText.getText());
 					intent.setEmployeeType((EmployeeType)comboBox.getSelectedItem());
 					
 					
 					try {
 						client.registerAndSend(intent, ManagementMenuComponent.this);
 						refreshList();
 					} catch (IOException e) {
 					
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 		
 		JButton btnModify = new JButton("Modify");
		btnModify.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				if(nameText.getText().isEmpty() || surnameText.getText().isEmpty() 
						|| emailText.getText().isEmpty() || passText.getText().isEmpty())
				{
					JOptionPane.showMessageDialog(ManagementMenuComponent.this,"Field empty", "Operation failed", JOptionPane.ERROR_MESSAGE);
				}
				else
				{
					ModifyEmployeeIntent intent = new ModifyEmployeeIntent();
					intent.setEmail(emailText.getText());
					intent.setName(nameText.getText());
					intent.setSurname(surnameText.getText());
					intent.setPassword(passText.getText());
					intent.setEmployeeType((EmployeeType)comboBox.getSelectedItem());
					
					
					try {
						client.registerAndSend(intent, ManagementMenuComponent.this);
						refreshList();
					} catch (IOException e) {
					
						e.printStackTrace();
					}
				}
			}
		});
 		btnModify.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		
 		JLabel lblPassword = new JLabel("Password:");
 		
 		passText = new JTextField();
 		passText.setColumns(10);
 		
 		comboBox = new JComboBox();
 		comboBox.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		comboBox.addItem(EmployeeType.ENGINEER);
 		comboBox.addItem(EmployeeType.ADMINISTRATION);
 		comboBox.addItem(EmployeeType.COMMITTEE_CHAIRMAN);
 		comboBox.addItem(EmployeeType.COMMITTEE_MEMBER);
 		comboBox.addItem(EmployeeType.DIRECTOR);
 		
 		
 		GroupLayout gl_panel = new GroupLayout(panel);
 		gl_panel.setHorizontalGroup(
 			gl_panel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
 						.addComponent(lblSurname)
 						.addComponent(lblType)
 						.addComponent(lblName)
 						.addComponent(lblId)
 						.addComponent(lblEmail)
 						.addComponent(lblPassword))
 					.addGap(18)
 					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_panel.createSequentialGroup()
 							.addComponent(btnAddNew)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(btnModify))
 						.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
 							.addComponent(idText, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
 							.addGroup(Alignment.LEADING, gl_panel.createParallelGroup(Alignment.TRAILING, false)
 								.addComponent(nameText, Alignment.LEADING)
 								.addComponent(surnameText, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE))
 							.addGroup(Alignment.LEADING, gl_panel.createParallelGroup(Alignment.TRAILING, false)
 								.addComponent(passText, Alignment.LEADING)
 								.addComponent(emailText, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)))
 						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, 169, GroupLayout.PREFERRED_SIZE))
 					.addGap(169))
 		);
 		gl_panel.setVerticalGroup(
 			gl_panel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblName)
 						.addComponent(nameText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addGap(18)
 					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblSurname)
 						.addComponent(surnameText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addGap(18)
 					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblType)
 						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE))
 					.addGap(18)
 					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblId)
 						.addComponent(idText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addGap(18)
 					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblEmail)
 						.addComponent(emailText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addGap(18)
 					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblPassword)
 						.addComponent(passText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addGap(32)
 					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(btnAddNew)
 						.addComponent(btnModify))
 					.addContainerGap(16, Short.MAX_VALUE))
 		);
 		panel.setLayout(gl_panel);
 		
 	}
 	
 
 	public void refreshList()
 	{
 		nameText.setText("");
 		surnameText.setText("");
 		idText.setText("");
 		emailText.setText("");
 		passText.setText("");
 		
 		try
 		{
 			model.clear();
 			BrowseEmployeesIntent browse = new BrowseEmployeesIntent();
 			client.registerAndSend(browse, this);
 		}
 		catch (IOException e) {e.printStackTrace();}
 	}
 	
 	@Override
 	public void update(Observable o, Object arg1) 
 	{
		//System.out.println(arg1);
 		if( arg1 instanceof Employee )
 			model.addElement((Employee)arg1);
 		
 	}
 }
