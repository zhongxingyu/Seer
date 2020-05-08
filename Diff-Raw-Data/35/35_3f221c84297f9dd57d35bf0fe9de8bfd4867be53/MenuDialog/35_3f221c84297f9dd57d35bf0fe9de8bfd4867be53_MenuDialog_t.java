 /*
  *  (c) Copyright 2012, Bryan Stockus
  *  	Bryan Stockus <bstockus@gmail.com>
  *  
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License version 3 as
  *  published by the Free Software Foundation.
  */
 
 package convenience.dialogs;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 
 public class MenuDialog implements Runnable, ActionListener, WindowListener {
 	
 	// Constants
 	public static Integer EXIT_RESULT = -1;
 	private static String DIALOG_EXIT_BUTTON_TEXT = "Exit";
 	private static String DIALOG_EXIT_BUTTON_ACTION_COMMAND = "dialogExitButtonActionCommand";
 	private static String DIALOG_MENU_ENTRY_BUTTON_ACTION_COMMAND_BASE = "dialogMenuEntryButtonActionCommand-";
 	
 	// Instance Variables
 	private String menuTitle = "";
 	private String menuHeader = "";
 	private String exitLabelText = "";
 	private ArrayList<String> menuEntries = null;
 	private Integer results = 0;
 	private Boolean running = false;
 	
 	public String getMenuTitle() {
 		return this.menuTitle;
 	}
 	
 	public String getMenuHeader() {
 		return this.menuHeader;
 	}
 	
 	public String getExitLabelText() {
 		return this.exitLabelText;
 	}
 	
 	public List<String> getMenuEntries() {
 		return this.menuEntries;
 	}
 	
 	public Integer getResults() {
 		return this.results;
 	}
 	
 	public Boolean isRunning() {
 		return this.running;
 	}
 	
 	// GUI Variables
 	private JFrame dialogFrame;
 	private JLabel dialogDescriptionLabel;
 	private JButton dialogExitButton;
 	private ArrayList<JButton> dialogMenuEntryButtons;
 	private ArrayList<String> dialogMenuEntryButtonActionCommands;
 	
 	// Action Command Handlers
 	
 	private void dialogExitButtonActionHandler() {
 		this.results = MenuDialog.EXIT_RESULT;
 		this.dialogFrame.dispose();
 		this.running = false;
 	}
 	
 	private void dialogMenuEntryButtonActionHandler(Integer menuEntryIndex) {
 		this.results = menuEntryIndex;
 		this.dialogFrame.dispose();
 		this.running = false;
 	}
 	
 	public MenuDialog(String menuTitle, String menuHeader, List<String> menuEntries, String exitLabelText) {
 		// Initialize Instance Variables
 		this.menuTitle = menuTitle;
 		this.menuHeader = menuHeader;
 		this.menuEntries = new ArrayList<String>(menuEntries);
 		this.exitLabelText = exitLabelText;
 		
 		// Initialize GUI Variables
 		this.dialogFrame = new JFrame();
 		this.dialogDescriptionLabel = new JLabel();
 		this.dialogExitButton = new JButton();
 		this.dialogMenuEntryButtons = new ArrayList<JButton>();
 		this.dialogMenuEntryButtonActionCommands = new ArrayList<String>();
 		
 		// Setup dialogMenuEntryButtons and dialogMenuEntryButtonActionCommands
 		Integer index = 0;
 		for (String menuEntry: this.getMenuEntries()) {
 			JButton dialogMenuEntryButton = new JButton();
 			String dialogMenuEntryButtonActionCommand = MenuDialog.DIALOG_MENU_ENTRY_BUTTON_ACTION_COMMAND_BASE + index.toString();
 			dialogMenuEntryButton.setText(menuEntry);
 			dialogMenuEntryButton.setActionCommand(dialogMenuEntryButtonActionCommand);
 			dialogMenuEntryButton.addActionListener(this);			
 			this.dialogMenuEntryButtons.add(dialogMenuEntryButton);
 			this.dialogMenuEntryButtonActionCommands.add(dialogMenuEntryButtonActionCommand);
 			index ++;
 		}
 		
 		// Setup dialogFrame
 		this.dialogFrame.setTitle(this.getMenuTitle());
 		this.dialogFrame.addWindowListener(this);
 		this.dialogFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		
 		// Setup dialogDescriptionLabel
 		this.dialogDescriptionLabel.setText(this.getMenuHeader());
 		this.dialogFrame.getContentPane().add(this.dialogDescriptionLabel);
 		
 		// Setup dialogExitButton
 		if (this.exitLabelText == null) {
 			this.dialogExitButton.setText(MenuDialog.DIALOG_EXIT_BUTTON_TEXT);
 		} else {
 			this.dialogExitButton.setText(this.exitLabelText);
 		}
 		this.dialogExitButton.setActionCommand(MenuDialog.DIALOG_EXIT_BUTTON_ACTION_COMMAND);
 		this.dialogExitButton.addActionListener(this);
 		this.dialogFrame.getContentPane().add(this.dialogExitButton);
 		
 		// Layout dialogFrame
 		GroupLayout dialogFrameGroupLayout = new GroupLayout(this.dialogFrame.getContentPane());
 		dialogFrameGroupLayout.setAutoCreateContainerGaps(true);
 		GroupLayout.ParallelGroup dialogFrameGroupHorizantialLayout = 
 				dialogFrameGroupLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
 		GroupLayout.SequentialGroup dialogFrameGroupVerticalLayout = dialogFrameGroupLayout.createSequentialGroup();
 		dialogFrameGroupHorizantialLayout.addComponent(this.dialogDescriptionLabel);
 		dialogFrameGroupVerticalLayout.addComponent(this.dialogDescriptionLabel);
 		dialogFrameGroupVerticalLayout.addGap(10);
 		for (JButton dialogMenuEntryButton: this.dialogMenuEntryButtons) {
 			dialogFrameGroupHorizantialLayout.addComponent(dialogMenuEntryButton);
 			dialogFrameGroupVerticalLayout.addComponent(dialogMenuEntryButton);
 			dialogFrameGroupLayout.linkSize(SwingConstants.HORIZONTAL, dialogMenuEntryButton, this.dialogExitButton);
 		}
 		dialogFrameGroupHorizantialLayout.addComponent(this.dialogExitButton);
 		dialogFrameGroupVerticalLayout.addComponent(this.dialogExitButton);
 		dialogFrameGroupLayout.setHorizontalGroup(dialogFrameGroupHorizantialLayout);
 		dialogFrameGroupLayout.setVerticalGroup(dialogFrameGroupVerticalLayout);
 		
 		dialogFrameGroupLayout.linkSize(SwingConstants.HORIZONTAL, this.dialogDescriptionLabel, this.dialogExitButton);
 		
 		this.dialogFrame.getContentPane().setLayout(dialogFrameGroupLayout);
 	}
 	
 	@Override
 	public void windowActivated(WindowEvent e) {
 		
 	}
 	
 	@Override
 	public void windowClosed(WindowEvent e) {
 		
 	}
 	
 	@Override
 	public void windowClosing(WindowEvent e) {
 		System.exit(0);
 	}
 	
 	@Override
 	public void windowDeactivated(WindowEvent e) {
 		
 	}
 	
 	@Override
 	public void windowDeiconified(WindowEvent e) {
 		
 	}
 	
 	@Override
 	public void windowIconified(WindowEvent e) {
 		
 	}
 	
 	@Override
 	public void windowOpened(WindowEvent e) {
 		
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent actionEvent) {
 		String actionCommand = actionEvent.getActionCommand();
 		if (actionCommand.equals(MenuDialog.DIALOG_EXIT_BUTTON_ACTION_COMMAND)) {
 			this.dialogExitButtonActionHandler();
 		} else {
 			Integer menuEntryIndex = this.dialogMenuEntryButtonActionCommands.indexOf(actionCommand);
 			if (menuEntryIndex != -1) {
 				this.dialogMenuEntryButtonActionHandler(menuEntryIndex);
 			} else {
 				System.out.println("ERROR: Unrecognized action command sent!");
 			}
 		}
 	}
 	
 	@Override
 	public void run() {
 		// Set status as running
 		this.running = true;
 		
 		// Display dialogFrame
 		this.dialogFrame.pack();
 		this.dialogFrame.setLocationRelativeTo(null);
 		this.dialogFrame.setVisible(true);
 	}
 	
 }
