 package controllers;
 
 import models.Organizer;
 import views.gui.LoginManager;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 
 public class DeleteUserController extends Controller {
 	private LoginManager login;
 	private JComboBox<String> username;
 	private JPasswordField passwd;
 	
 	public DeleteUserController(LoginManager login, JComboBox<String> username, 
 			JPasswordField passwd) {
 		this.login = login;
 		this.username = username;
 		this.passwd = passwd;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		JOptionPane alert = new JOptionPane("Are you sure you want to delete user '" + username.getSelectedItem() + "'?",
 				JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
 		alert.createDialog(login, "").setVisible(true);
		if(alert.getValue().equals(1))
 			return;
 		Organizer organizer = Organizer.getInstance();
 		boolean removed = organizer.getUsers().remove(
 				(String) username.getSelectedItem(), new String(passwd.getPassword()));
 		if(removed) {
 			alert = new JOptionPane("User '" + username.getSelectedItem() + "' has been deleted!",
 					JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
 			login.updateUserList();
 			passwd.setText("");
 			alert.createDialog(login, "Confirmation").setVisible(true);
 		} else {
 			alert = new JOptionPane("The password is invalid!",
 					JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION);
 			alert.createDialog(login, "Error").setVisible(true);
 		}
 	}
 }
