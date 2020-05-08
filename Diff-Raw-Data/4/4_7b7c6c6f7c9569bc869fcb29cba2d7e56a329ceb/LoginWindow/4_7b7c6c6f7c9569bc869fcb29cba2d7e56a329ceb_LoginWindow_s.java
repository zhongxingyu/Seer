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
 import com.jpii.navalbattle.util.URLUtils;
 import com.jpii.roketgamer.Player;
 import com.jpii.roketgamer.rauth.*;
 
 @SuppressWarnings("serial")
 public class LoginWindow extends Window {
 	JButton loginButton;
 	JLabel usernameLabel, passwordLabel;
 	JTextField usernameField;
 	JPasswordField passwordField;
 
 	public LoginWindow() {
 		super(500,190);
 		getContentPane().setLayout(null);
 		usernameLabel = new JLabel();
 		usernameField = new JTextField(25);
 		passwordLabel = new JLabel();
 		passwordField = new JPasswordField(25);
 		loginButton = new JButton("Login");
 		JButton registerButton = new JButton("Register");
 		JButton offlineButton = new JButton("Offline");
 		JButton optionsButton = new JButton("Options");
 		JLabel lblVersion = new JLabel(Constants.NAVALBATTLE_VERSION_TITLE);
 		
 		usernameLabel.setText("Username");
 		passwordLabel.setText("Password");
 		passwordLabel.setToolTipText("Use RoketGamer application password");
 		passwordField.setToolTipText("Use RoketGamer application password");
 		
 		usernameLabel.setBounds(295,8,78,30);
 		usernameField.setBounds(365,8,113,30);
 		passwordLabel.setBounds(295,39,78,30);
 		passwordField.setBounds(365,39,113,30);
 		loginButton.setBounds(400,81,78,30);
 		registerButton.setBounds(315, 113, 78, 30);
 		offlineButton.setBounds(315, 81, 78, 30);
 		optionsButton.setBounds(400, 113, 78, 30);
 		lblVersion.setBounds(6, 129, 193, 14);
 		getContentPane().add(usernameLabel);
 		getContentPane().add(usernameField);
 		getContentPane().add(passwordLabel);
 		getContentPane().add(passwordField);
 		getContentPane().add(loginButton);
 		getContentPane().add(lblVersion);
 		getContentPane().add(registerButton);
 		getContentPane().add(offlineButton);
 		getContentPane().add(optionsButton);
 		
 		loginButton.setFocusable(false);
 		registerButton.setFocusable(false);
 		offlineButton.setFocusable(false);
 		optionsButton.setFocusable(false);	
 		
 		passwordField.addKeyListener(Constants.keys);
 		usernameField.addKeyListener(Constants.keys);
 		
 		setDefaults();	
 		setSize(500,190);
 		
 		loginButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				login();
 			}
 		});
 		
 		offlineButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				NavalBattle.getWindowHandler().getToasterManager().showToaster(new ImageIcon(getClass().getResource("/com/jpii/roketgamer/res/logo_100px.png")), "Offline mode enabled");
 				NavalBattle.getDebugWindow().printInfo("Opening in offline mode");
 				NavalBattle.getDebugWindow().printWarning("RoketGamer disabled");
 				NavalBattle.getGameState().setOffline(true);
 				nextWindow("LoggingInWindow");
 				donewithMe();
 			}
 		});
 		
 		registerButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {			
 				NavalBattle.getDebugWindow().printInfo("Opening register page");
 				URLUtils.openURL(NavalBattle.getRoketGamer().getServerLocation() + "/register.php?game=1&name=NavalBattle");
 			}
 		});
 		
 		optionsButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				nextWindow("LoginOptionsWindow");
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
 			NavalBattle.getWindowHandler().getToasterManager().showToaster(new ImageIcon(getClass().getResource("/com/jpii/roketgamer/res/logo_100px.png")), "Logged in as " + NavalBattle.getRoketGamer().getPlayer().getName());
 			NavalBattle.getDebugWindow().printInfo("User authenticated");
 			NavalBattle.getDebugWindow().printInfo("Logged in as: " + NavalBattle.getRoketGamer().getPlayer().getName());
 			NavalBattle.getGameState().setOffline(false);
 			nextWindow("LoggingInWindow");
 			donewithMe();
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
 }
