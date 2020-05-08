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
 
 package com.jpii.navalbattle.debug;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 import com.jpii.navalbattle.NavalBattle;
 import com.jpii.navalbattle.gui.Window;
 import com.jpii.navalbattle.gui.listeners.KeyboardListener;
 
 @SuppressWarnings("serial")
 public class DebugWindow extends Window {
 	private JLabel lblNavalBattle;
 	private JLabel lblDebugMode;
 	private JTextPane debugPrinter;
 	private JTextField commandField;
 	private boolean paused = false;
 
 	/**
 	 * Constructor for DebugWindow.
 	 */
 	public DebugWindow() {
 		super(465,365,0,0);
 		getContentPane().setLayout(null);
 
 		lblNavalBattle = new JLabel("NavalBattle");
 		lblDebugMode = new JLabel("Debug Mode");
 		JScrollPane scrollPane = new JScrollPane();
 		debugPrinter = new JTextPane();
 		commandField = new JTextField();
 		final JButton btnSubmit = new JButton("Submit");
 		
 		lblNavalBattle.setBounds(10, 11, 120, 14);
 		lblDebugMode.setBounds(95, 13, 120, 14);
 		scrollPane.setBounds(10, 35, 439, 255);
		commandField.setBounds(10, 301, 337, 25);
		btnSubmit.setBounds(357, 301, 90, 25);
 		
 		lblNavalBattle.setFont(new Font("Tahoma", Font.BOLD, 14));
 		debugPrinter.setFont(new Font("Consolas",0,12));
 		debugPrinter.setEditable(false);
 		scrollPane.setViewportView(debugPrinter);
 		commandField.setColumns(10);
 		btnSubmit.setFocusable(false);
 		debugPrinter.setFocusable(false);
 		
 		getContentPane().add(lblNavalBattle);
 		getContentPane().add(lblDebugMode);
 		getContentPane().add(scrollPane);
 		getContentPane().add(commandField);
 		getContentPane().add(btnSubmit);
 		
 		commandField.grabFocus();
 		commandField.addKeyListener(new KeyboardListener(this));
 		btnSubmit.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if(!commandField.getText().isEmpty()) {
 					submitCommand(commandField.getText());
 					commandField.setText("");
 				}
 			}
 		});
	
 		printInfo("Debug mode enabled");
 		setDefaults();
 	}
 
 	/**
 	 * Prints a message without a tag. Next message does not go to a new line.
 	 * @param message
 	 */
 	public void print(String message) {
 		debugPrinter.setText(debugPrinter.getText() + message);
 	}
 
 	/**
 	 * Prints a message without a tag. Next message goes to a new line.
 	 * @param message
 	 */
 	public void println(String message) {
 		debugPrinter.setText(debugPrinter.getText() + message + "\n");
 	}
 
 	/**
 	 * Prints a message with an [INFO] tag.
 	 * @param message
 	 */
 	public void printInfo(String message) {
 		if(!paused)
 			debugPrinter.setText(debugPrinter.getText() + "[INFO] " + message + "\n");
 	}
 
 	/**
 	 * Prints a message with a [WARN] tag.
 	 * @param message
 	 */
 	public void printWarning(String message) {
 		if(!paused)
 			debugPrinter.setText(debugPrinter.getText() + "[WARN] " + message + "\n");
 	}
 
 	/**
 	 * Prints a message with an [ERROR] tag.
 	 * @param message
 	 */
 	public void printError(String message) {
 		if(!paused)
 			debugPrinter.setText(debugPrinter.getText() + "[ERROR] " + message + "\n");
 	}
 
 	/**
 	 * Prints a message with a [OTHER] tag.
 	 * @param message
 	 */
 	public void printOther(String message) {
 		if(!paused)
 			debugPrinter.setText(debugPrinter.getText() + "[OTHER] " + message + "\n");
 	}
 
 	/**
 	 * Clears the DebugWindow and prints message.
 	 * @param message
 	 */
 	public void printNew(String message) {
 		debugPrinter.setText(message + "\n");
 	}
 
 	/**
 	 * Get current JFrame.
 	 * @return JFrame
 	 */
 	public JFrame getFrame() {
 		return this;
 	}
 
 	/**
 	 * Parses command.
 	 * @param command
 	 */
 	public void submitCommand(String command) {
 		NavalBattle.getCommandHandler().parseCommand(command);
 	}
 
 	/**
 	 * Remotely grabs entered in <code>commandField</code> and parses.
 	 */
 	public void submitCommandRemote() {
 		if(!commandField.getText().isEmpty()) {
 			submitCommand(commandField.getText());
 			commandField.setText("");
 		}
 	}
 
 	/**
 	 * Pause logging of tagged messages.
 	 */
 	public void pause() {
 		this.paused  = true;
 	}
 	
 	/**
 	 * Resume logging of tagged messages.
 	 */
 	public void resume() {
 		this.paused = false;
 	}
 	
 	/**
 	 * Grab focus of the <code>commandField</code>.
 	 */
 	public void focusOnField() {
 		commandField.grabFocus();
 	}
 }
