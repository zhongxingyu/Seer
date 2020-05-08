 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.event.*;
 
 public class login extends JFrame {
 	private JPanel frame;
 	private JButton login,addb;
 	private JLabel username, password, type, message,message2;
 	private JTextField name, atype;
 	private JPasswordField passw;
 	private boolean correct;
 	Members memList;
 
 	// private ArrayList<user> classList;
 
 	public login(Members mem) {
 		correct = false;
 		
 		memList = mem;
 
 		this.setSize(new Dimension(300, 200));
 
 		frame = new JPanel();
 		frame.setLayout(new GridLayout(5, 2));
 
 		username = new JLabel("UserName:");
 		password = new JLabel("Password:");
 		type = new JLabel("Account Type:");
 		message = new JLabel("");
 		message2 = new JLabel("");
 
 		name = new JTextField(16);
 		// name.addActionListener(new NameListener());
 		
 		atype = new JTextField(16);
 		
 		passw = new JPasswordField(16);
 		// passw.addActionListener(new PassListener());
 
 		login = new JButton("Continue");
 		login.addActionListener(new LoginListener());
 		
 		addb = new JButton("Add Account");
 		addb.addActionListener(new AddListener());
 
 		frame.add(username);
 		frame.add(name);
 		frame.add(password);
 		frame.add(passw);
 		frame.add(type);
 		frame.add(atype);
 		frame.add(message);
 		frame.add(message2);
 		frame.add(login);
 		frame.add(addb);
 		add(frame);
 		frame.setVisible(true);
 		setVisible(true);
 
 	}
 	
 	public void close(){
 		this.setVisible(false);
 	}
 
 	public boolean getAccept() {
 		return correct;
 	}
 
 	private class LoginListener implements ActionListener {
 		
 		public void actionPerformed(ActionEvent e) {
 			
 			if (memList.checkUser(name.getText())){	
 				
 				User currentU = memList.getUser(name.getText());
 		
 				if(currentU.checkPassword(new String(passw.getPassword()))){		
 
 					
 					if (memList.getUser(name.getText()).getType()==0){
 						studentUI sUI = new studentUI ((Student)(memList.getUser(name.getText())),memList);
 						sUI.setVisible(true);
 
 					}
 					else {
 						instructorUI iUI = new instructorUI((Instructor)(memList.getUser(name.getText())),memList);
 						iUI.setVisible(true);
 
 					}
 				}
 				else {
 					message.setText("Incorrect Password.");
 				}
 			}
 			else
 			{	
 				message.setText("Username not found.");
 			}
 		}
 	}
 	
 	private class AddListener implements ActionListener{
 
 		public void actionPerformed(ActionEvent arg0) {
 			if (!memList.checkUser(name.getText())){
 				if(atype.getText().equalsIgnoreCase("Instructor"))
 					memList.addUser(name.getText(), new String (passw.getPassword()), 1);
 				else
 					memList.addUser(name.getText(), new String(passw.getPassword()), 0);
 				message.setText("User added.");
 				memList.dataUpdate();
 			}
 		}		
 	}
 }
