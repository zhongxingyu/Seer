 package gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import net.PackageReceiver;
 import net.PackageSender;
 
 import datapackage.DataPackage;
 import datapackage.ErrorPackage;
 import datapackage.ErrorType;
 import datapackage.LoginPackage;
 
 @SuppressWarnings("serial")
 public class LoginPanel extends JPanel {
 	private JTextField username, password;
 	private JButton login;
 	private PackageSender sender;
 	
 	
 	public LoginPanel() throws IOException{
 		username = new JTextField();
 		username.setName("username");
 		username.setColumns(30);
 		add(username);
 		
 		password = new JTextField();
 		password.setName("password");
 		password.setColumns(30);
 		add(password);
 		
 		login = new JButton();
 		login.setName("login");
 		login.setText("Login");
 		login.addActionListener(new loginAction());
 		add(login);
 		
 		sender = new PackageSender();
 		
 		
 	}
 	
 	public class loginAction implements ActionListener
 	{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (username.getText().isEmpty()){
 				JOptionPane.showMessageDialog(null, "Username may not be blank");
 			}
 			else if (password.getText().isEmpty()){
 				JOptionPane.showMessageDialog(null, "Password may not be blank");			}
 			else{
 				try {
 					if (validLogin(username.getText(), password.getText())){
						//Login approved - Move to calender-view
						//.removeAll()
 						JOptionPane.showMessageDialog(null, "YOU CAN LOGIN! GANDALF WILL LET YOU PASS!");
 					}
 				} catch (IOException e1) {
 					// Could not receive answer from server, alert user
 					JOptionPane.showMessageDialog(null, "There was a critical error connecting to the server. Try again");
 					e1.printStackTrace();
 				}
 					
 			}
 			
 		}
 		
 		public boolean validLogin(String username, String password) throws IOException{
 			//PackageReceiver receiver = new PackageReceiver();
 			LoginPackage loginPack = new LoginPackage(username, password);
 			sender.sendPackage(loginPack);
 			DataPackage responsePack = sender.receivePackage();
 			if (responsePack instanceof ErrorPackage){
 				ErrorPackage loginResponse = (ErrorPackage)responsePack;
 				if (loginResponse.getErrorType() == ErrorType.OK){
 						return true;
 					}
 					else if (loginResponse.getErrorType() == ErrorType.WRONG_PASSWORD){
 						return false;
 
 					}
 					else{
 						JOptionPane.showMessageDialog(null, "The servers response was not of an expected type. Try again.");
 						return false;
 					}
 			}
 			return false;
 			
 		}
 		
 	}
 }
