 package vms.gui;
 
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
import javax.swing.JRootPane;
 import javax.swing.JTextField;
 
 public class Login implements ActionListener, WindowListener {
 	MainGUI _Main;
 	JFrame log;
 	JTextField pass;
 	JButton but[] = {new JButton("Login"), new JButton("Exit")};
 	public Login (MainGUI main) {
 		_Main = main;
 		log = new JFrame("Login");
 		log.addWindowListener(this);
 		log.setSize(200, 150);
 		log.setResizable(false);
 
 		//Creating fields
		JLabel label = new JLabel("Enter your password");
 //		JLabel op = new JLabel("Operator: op123");
 //		JLabel us = new JLabel("User: user123");
 		pass = new JTextField();
 		
 		JPanel output = new JPanel();
 		output.setLayout(new GridLayout(3,1));		
 		JPanel commands = new JPanel();
 		
 		// Creating functions
 		but[0].addActionListener(this);
 		but[1].addActionListener(this);
 		// Adding fields to panels
 //		output.add(op);
 //		output.add(us);
 		output.add(label);
 		output.add(pass);
 		
 		commands.add(but[0]);
 		commands.add(but[1]);
 		// Adding Panels to Frame
 		log.add(output, BorderLayout.CENTER);
 		log.add(commands, BorderLayout.SOUTH);
		
		JRootPane rootPane = log.getRootPane();
	    rootPane.setDefaultButton(but[0]);
 	}
 	
 	public void show() {
 		pass.setText("");
 		log.setVisible(true);
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource().equals(but[0])) {
 			if (pass.getText().compareTo("op123") == 0) {
 				_Main.showRadar(MainGUI.UserIdentity.OPERATOR);
 				log.setVisible(false);
 			}
 			else if (pass.getText().compareTo("user123") == 0) {
 				_Main.showRadar(MainGUI.UserIdentity.NORMAL_USER);
 				log.setVisible(false);
 			}
 		}
 		else if (e.getSource().equals(but[1])) {
 			close();
 		}
 	}
 	
 	public void close() {
 		//This is the proper way to close
 		WindowEvent windowClosing = new WindowEvent(log, WindowEvent.WINDOW_CLOSING);
 		log.dispatchEvent(windowClosing);
 	}
 
 	@Override
 	public void windowActivated(WindowEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void windowClosed(WindowEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void windowClosing(WindowEvent arg0) {
 		_Main.stopServer();
 	}
 
 	@Override
 	public void windowDeactivated(WindowEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void windowIconified(WindowEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void windowOpened(WindowEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
