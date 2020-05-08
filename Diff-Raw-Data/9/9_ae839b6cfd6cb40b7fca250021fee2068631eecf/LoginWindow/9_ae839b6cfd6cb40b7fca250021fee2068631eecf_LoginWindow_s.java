 /*
  * Copyright (C) 2012 JPII and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.jpii.navalbattle.gui;
 
 import javax.swing.*;
 import java.awt.event.*;
 import com.jpii.navalbattle.NavalBattle;
 import com.jpii.navalbattle.data.Constants;
 import com.jpii.navalbattle.renderer.Helper;
 import com.jpii.navalbattle.util.URLUtils;
 import com.jpii.roketgamer.Player;
 import com.jpii.roketgamer.rauth.APIKey;
 import com.jpii.roketgamer.rauth.AuthStatus;
 import com.jpii.roketgamer.rauth.Password;
 
 @SuppressWarnings("serial")
 public class LoginWindow extends JFrame{
 	JButton loginButton;
 	JLabel usernameLabel, passwordLabel;
 	JTextField usernameField;
 	JPasswordField passwordField;
 
 	public LoginWindow() {
 		
 		NavalBattle.getDebugWindow().printInfo("LoginWindow opened");
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {}
 		this.setIconImage(Helper.GUI_WINDOW_ICON);
 		
 		setTitle("NavalBattle");
 		getContentPane().setLayout(null);
 		usernameLabel = new JLabel();
 		usernameLabel.setText("Username");
 		usernameLabel.setBounds(311,11,58,20);
 		usernameField = new JTextField(25);
 		usernameField.setBounds(365,11,100,20);
 		passwordLabel = new JLabel();
 		passwordLabel.setToolTipText("Use RoketGamer application password");
 		passwordLabel.setText("Password");
 		passwordLabel.setBounds(311,42,58,20);
 		passwordField = new JPasswordField(25);
 		passwordField.setToolTipText("Use RoketGamer application password");
 		passwordField.setBounds(365,42,100,20);
 		loginButton = new JButton("Login");
 		loginButton.setBounds(387,86,78,22);
 
 		getContentPane().add(usernameLabel);
 		getContentPane().add(usernameField);
 		getContentPane().add(passwordLabel);
 		getContentPane().add(passwordField);
 		getContentPane().add(loginButton);
 		this.setIconImage(Helper.GUI_WINDOW_ICON);
 		
 		passwordField.addKeyListener(new KeyboardListener(this));
 		usernameField.addKeyListener(new KeyboardListener(this));		
 		
 		JLabel lblVersion = new JLabel(Constants.NAVALBATTLE_VERSION_TITLE);
 		lblVersion.setBounds(10, 90, 193, 14);
 		getContentPane().add(lblVersion);
 
 		JButton registerButton = new JButton("Register");
 		registerButton.setBounds(301, 86, 78, 22);
 		getContentPane().add(registerButton);
 		registerButton.addKeyListener(new KeyboardListener(this));
 		
 		JButton offlineButton = new JButton("Offline");
 		offlineButton.setBounds(213, 86, 78, 22);
 		getContentPane().add(offlineButton);
 
 		setSize(491,143);
		setVisible(true);
 		setResizable(false);
 		setLocation(1280/2-getWidth()/2,800/2-getHeight()/2);
 
 		loginButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				login();
 			}
 		});
 		
 		offlineButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				NavalBattle.getToasterManager().showToaster(new ImageIcon(getClass().getResource("/com/jpii/roketgamer/res/logo_100px.png")), "Offline mode enabled");
 				NavalBattle.getDebugWindow().printInfo("Opening in offline mode");
 				NavalBattle.getDebugWindow().printWarning("RoketGamer disabled");
 				NavalBattle.getGameState().setOffline(true);
 				
 				NavalBattle.getDebugWindow().printInfo("Disposing LoginWindow");
 				dispose();
 				NavalBattle.getDebugWindow().printInfo("Opening LoggingInWindow");
 				new LoggingInWindow();
 			}
 		});
 		
 		registerButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {			
 				NavalBattle.getDebugWindow().printInfo("Opening register page");
 				URLUtils.openURL(Constants.SERVER_LOCATION + "/register.php?game=1&name=NavalBattle");
 			}
 		});
 		
 		setFocusable(true);
 		addKeyListener(new KeyboardListener(this));
 
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent we) {
 				NavalBattle.close();
 			}
 		});
 	}
 	
 	/**
 	 *  Method for handling login.
 	 */
 	public void login() {
 		@SuppressWarnings("deprecation")
 		AuthStatus status = NavalBattle.getRoketGamer().init(new APIKey(Constants.API_KEY), 
 				new Player(usernameField.getText(), 
 				new Password(passwordField.getText())));
 		
 		if (status == AuthStatus.GOOD) {
 			NavalBattle.getToasterManager().showToaster(new ImageIcon(getClass().getResource("/com/jpii/roketgamer/res/logo_100px.png")), "Logged in as " + NavalBattle.getRoketGamer().getPlayer().getName());
 			NavalBattle.getDebugWindow().printInfo("User authenticated");
 			NavalBattle.getDebugWindow().printInfo("Logged in as: " + NavalBattle.getRoketGamer().getPlayer().getName());
 			NavalBattle.getDebugWindow().printInfo("Disposing LoginWindow");
 			dispose();
 			NavalBattle.getDebugWindow().printInfo("Opening LoggingInWindow");
 			new LoggingInWindow();
 		} else {
 			if(status == AuthStatus.BAD) {
 				NavalBattle.getDebugWindow().printWarning("Authentication failed: AuthStatus.BAD");	
 				JOptionPane.showMessageDialog(this, "Incorrect username or password. \nUse your application password to login.");
 			} else if (status == AuthStatus.OFFLINE) {
 				NavalBattle.getDebugWindow().printWarning("Authentication failed: AuthStatus.OFFLINE");	
 				JOptionPane.showMessageDialog(this, "Unable to login. RoketGamer is offline.");
 			} else if (status == AuthStatus.INVALID_API_KEY) {
 				NavalBattle.getDebugWindow().printWarning("Authentication failed: AuthStatus.INVALID_API_KEY");	
 				JOptionPane.showMessageDialog(this, "Unable to login. API key is invalid.");
 			} else if (status == AuthStatus.UNKNOWN) {
 				NavalBattle.getDebugWindow().printWarning("Authentication failed: AuthStatus.UNKNOWN");	
 				JOptionPane.showMessageDialog(this, "Unable to login. Retry later or check the RoketGamer website.");
 			} else {
 				NavalBattle.getDebugWindow().printWarning("Authentication failed: AuthStatus is not recognized.");
 				NavalBattle.getDebugWindow().printWarning("Internet may be disconnected.");
 				JOptionPane.showMessageDialog(this, "Unable to login. Check your internet connection.");
 			}
 		}
 	}
 	
 	/**
 	 * Get method for LoginWindow
 	 * 
 	 * @return LoginWindow
 	 */
 	public JFrame getFrame() {
 		return this;
 	}
 }
