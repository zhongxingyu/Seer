 package edu.brown.cs32.scheddar.gui;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Collection;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 
 import edu.brown.cs32.scheddar.Group;
 import edu.brown.cs32.scheddar.Person;
 
 /**
  * @author sdemane
  * 
  * Class implementing the person creation/editor form.
  *
  */
 public class EditPersonForm extends AbstractForm {
 	private static final long serialVersionUID = 1L;
 	
 	JTextField firstName;
 	JTextField lastName;
 	JTextField email;
 	JTextField phone;
 	JTextField description;
 	Person p;
 	
 	public EditPersonForm(ScheddarPane s, Person person) {
 		super(s);
 		p = person;
 		JPanel panel = new JPanel();
 		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		
 		//getting name of new group
 		firstName = new JTextField(20);
 		firstName.setText(p.getFirstName());
 		lastName = new JTextField(20);
 		lastName.setText(p.getLastName());
 		email = new JTextField(20);
 		email.setText(p.getEmail());
 		phone = new JTextField(20);
 		phone.setText(p.getPhoneNum());
 		description = new JTextField(20);
 		description.setText(p.getDescription());
 		
 		// making "Create Person" button
 		
 		JButton create = new JButton("Save");
 		create.addActionListener(new ActionListener() {			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(firstName.getText().trim().length()>0 && lastName.getText().trim().length()>0) {
 					Collection <Person> existingPeople = _scheddarPane._scheddar.getAllPeople();
 					if(!p.getFirstName().equals(firstName.getText().trim()) || !p.getLastName().equals(lastName.getText().trim())) {
 						for(Person ep : existingPeople){
 							if(ep.getFirstName().equals(firstName.getText().trim()) && ep.getLastName().equals(lastName.getText().trim())){
 								PopUps.popUpPersonAlreadyExists();
 								return;
 							}
 						}
 					}
 					p.setFirstName(firstName.getText().trim());
 					p.setLastName(lastName.getText().trim());
 					p.setEmail(email.getText().trim());
 					p.setPhoneNum(phone.getText().trim());
 					p.setDescription(description.getText().trim());
					_scheddarPane._groupTree.updateTree();
 					dispose();				
 				}
 			}
 		});
 		
 		
 		// adding everything
 		panel.add(new JLabel("First name:"));
 		panel.add(firstName);
 		panel.add(new JLabel("Last name:"));
 		panel.add(lastName);
 		panel.add(new JLabel("Email:"));
 		panel.add(email);
 		panel.add(new JLabel("Phone:"));
 		panel.add(phone);
 		panel.add(new JLabel("Description:"));
 		panel.add(description);
 		panel.add(create);
 		add(panel);
 		pack();
 		setVisible(true);
 	}
 }
