 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.client.ui;
 
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.net.InetAddress;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.AppConstants;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.MapToolRegistry;
 import net.tsc.servicediscovery.AnnouncementListener;
 import net.tsc.servicediscovery.ServiceFinder;
 /**
  * @author trevor
  */
 public class ConnectToServerDialog extends JDialog implements AnnouncementListener{
 
 	public static final int OPTION_OK = 0;
 	public static final int OPTION_CANCEL = 1;
 
 	private javax.swing.JPanel jContentPane = null;
 	private JLabel jLabel = null;
 	private JTextField usernameTextField = null;
 	private JPanel jPanel1 = null;
 	private JButton cancelButton = null;
 	private JButton okButton = null;
 	private JTextField serverTextField = null;
 	private JTextField portTextField = null;
 	
 	private int option;
 	
 	private JLabel jLabel3 = null;
 	private JComboBox roleComboBox = null;
 	private JLabel passwordLabel = null;
 	private JPasswordField passwordPasswordField = null;
 	private JTabbedPane typeTabbedPane = null;
 	private JPanel wanPanel = null;
 	private JPanel lanPanel = null;
 	private JLabel localServersLabel = null;
 	private JList serverList = null;
 	private JButton rescanButton = null;
 	private JLabel serverLabel = null;
 	private JLabel portLabel = null;
 	private JLabel spacerLabel = null;
 	
 	private ServiceFinder finder;
 	
 	private int selectedPort;
 	private String selectedServerAddress;
 	private JPanel rptoolsPanel = null;
 	private JLabel serverNameLabel = null;
 	private JLabel serverPasswordLabel = null;
 	private JPanel spacerPanel = null;
 	private JTextField serverNameTextField = null;
 	private JLabel spacerLabel2 = null;
 	private JPasswordField serverPasswordField = null;
 	
 	/**
 	 * This is the default constructor
 	 */
 	public ConnectToServerDialog() {
 		super(MapTool.getFrame(), "Connect to Server", true);
 		initialize();
 		
 		finder = new ServiceFinder(AppConstants.SERVICE_GROUP);
 		finder.addAnnouncementListener(this);
 		
 		finder.find();
 	}
 	/**
 	 * This method initializes this
 	 * 
 	 * @return void
 	 */
 	private void initialize() {
 		this.setSize(300, 325);
 		this.setTitle("Connect to Server");
 		this.setContentPane(getJContentPane());
 		
 		getRootPane().setDefaultButton(okButton);
 		
 		// Prefs
 		ConnectToServerDialogPreferences prefs = new ConnectToServerDialogPreferences();
 		portTextField.setText(Integer.toString(prefs.getPort()));
 		serverTextField.setText(prefs.getHost());
 		usernameTextField.setText(prefs.getUsername());
 		roleComboBox.setSelectedIndex(prefs.getRole());
 		passwordPasswordField.setText(prefs.getPassword());
 		getTypeTabbedPane().setSelectedIndex(prefs.getTab());
 		getServerNameTextField().setText(prefs.getServerName());
 		getServerPasswordField().setText(prefs.getServerPassword());
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.awt.Component#setVisible(boolean)
 	 */
 	public void setVisible(boolean b) {
 		
 		if (b) {
 			SwingUtil.centerOver(this, MapTool.getFrame());
 		} else {
 			// Shutting down
 			finder.dispose();
 		}
 		super.setVisible(b);
 	}
 	
 	/**
 	 * This method initializes jContentPane
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private javax.swing.JPanel getJContentPane() {
 		if(jContentPane == null) {
 			GridBagConstraints gridBagConstraints110 = new GridBagConstraints();
 			gridBagConstraints110.gridx = 0;
 			gridBagConstraints110.gridy = 7;
 			spacerLabel2 = new JLabel();
 			spacerLabel2.setText("   ");
 			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
 			gridBagConstraints14.fill = java.awt.GridBagConstraints.BOTH;
 			gridBagConstraints14.gridy = 8;
 			gridBagConstraints14.weightx = 1.0;
 			gridBagConstraints14.weighty = 1.0;
 			gridBagConstraints14.gridwidth = 2;
 			gridBagConstraints14.gridx = 0;
 			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
 			gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
 			gridBagConstraints1.gridy = 1;
 			gridBagConstraints1.weightx = 1.0;
 			gridBagConstraints1.gridx = 1;
 			GridBagConstraints gridBagConstraints = new GridBagConstraints();
 			gridBagConstraints.gridx = 0;
 			gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
 			gridBagConstraints.gridy = 1;
 			passwordLabel = new JLabel();
 			passwordLabel.setText("Password:");
 			jLabel3 = new JLabel();
 			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
 			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
 			jLabel = new JLabel();
 			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
 			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
 			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
 			jContentPane = new javax.swing.JPanel();
 			jContentPane.setLayout(new GridBagLayout());
 			gridBagConstraints12.gridx = 0;
 			gridBagConstraints12.gridy = 0;
 			gridBagConstraints12.weightx = 0.5D;
 			gridBagConstraints12.anchor = java.awt.GridBagConstraints.WEST;
 			jLabel.setText("Username:");
 			gridBagConstraints13.gridx = 1;
 			gridBagConstraints13.gridy = 0;
 			gridBagConstraints13.weightx = 1.0;
 			gridBagConstraints13.fill = java.awt.GridBagConstraints.HORIZONTAL;
 			jContentPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(10,10,5,10));
 			gridBagConstraints17.gridx = 0;
 			gridBagConstraints17.gridy = 10;
 			gridBagConstraints17.fill = java.awt.GridBagConstraints.HORIZONTAL;
 			gridBagConstraints17.gridwidth = 2;
 			gridBagConstraints3.gridx = 0;
 			gridBagConstraints3.gridy = 6;
 			gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
 			jLabel3.setText("Role:");
 			gridBagConstraints4.gridx = 1;
 			gridBagConstraints4.gridy = 6;
 			gridBagConstraints4.weightx = 1.0;
 			gridBagConstraints4.fill = java.awt.GridBagConstraints.NONE;
 			gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
 			jContentPane.add(jLabel, gridBagConstraints12);
 			jContentPane.add(getUsernameTextField(), gridBagConstraints13);
 			jContentPane.add(getJPanel1(), gridBagConstraints17);
 			jContentPane.add(jLabel3, gridBagConstraints3);
 			jContentPane.add(getRoleComboBox(), gridBagConstraints4);
 			jContentPane.add(passwordLabel, gridBagConstraints);
 			jContentPane.add(getPasswordPasswordField(), gridBagConstraints1);
 			jContentPane.add(getTypeTabbedPane(), gridBagConstraints14);
 			jContentPane.add(spacerLabel2, gridBagConstraints110);
 		}
 		return jContentPane;
 	}
 	/**
 	 * This method initializes jTextField	
 	 * 	
 	 * @return javax.swing.JTextField	
 	 */    
 	private JTextField getUsernameTextField() {
 		if (usernameTextField == null) {
 			usernameTextField = new JTextField();
 		}
 		return usernameTextField;
 	}
 	/**
 	 * This method initializes jPanel1	
 	 * 	
 	 * @return javax.swing.JPanel	
 	 */    
 	private JPanel getJPanel1() {
 		if (jPanel1 == null) {
 			FlowLayout flowLayout18 = new FlowLayout();
 			jPanel1 = new JPanel();
 			jPanel1.setLayout(flowLayout18);
 			flowLayout18.setAlignment(java.awt.FlowLayout.RIGHT);
 			jPanel1.add(getOkButton(), null);
 			jPanel1.add(getCancelButton(), null);
 		}
 		return jPanel1;
 	}
 	/**
 	 * This method initializes jButton	
 	 * 	
 	 * @return javax.swing.JButton	
 	 */    
 	private JButton getCancelButton() {
 		if (cancelButton == null) {
 			cancelButton = new JButton();
 			cancelButton.setText("Cancel");
 			cancelButton.addActionListener(new java.awt.event.ActionListener() { 
 				public void actionPerformed(java.awt.event.ActionEvent e) {    
 					option = OPTION_CANCEL;
 					setVisible(false);
 				}
 			});
 		}
 		return cancelButton;
 	}
 	/**
 	 * This method initializes jButton1	
 	 * 	
 	 * @return javax.swing.JButton	
 	 */    
 	private JButton getOkButton() {
 		if (okButton == null) {
 			okButton = new JButton();
 			okButton.setText("Ok");
 			okButton.addActionListener(new java.awt.event.ActionListener() { 
 				public void actionPerformed(java.awt.event.ActionEvent e) {    
 
 					handleOK();
 				}
 			});
 		}
 		return okButton;
 	}
 	/**
 	 * This method initializes jTextField1	
 	 * 	
 	 * @return javax.swing.JTextField	
 	 */    
 	private JTextField getServerTextField() {
 		if (serverTextField == null) {
 			serverTextField = new JTextField();
 		}
 		return serverTextField;
 	}
 	/**
 	 * This method initializes jTextField2	
 	 * 	
 	 * @return javax.swing.JTextField	
 	 */    
 	private JTextField getPortTextField() {
 		if (portTextField == null) {
 			portTextField = new JTextField();
 		}
 		return portTextField;
 	}
 	
 	public int getOption() {
 		return option;
 	}
 	
 	public String getUsername() {
 		return usernameTextField.getText();
 	}
 	
 	public String getServer() {
 		return selectedServerAddress;
 	}
 	
 	public int getPort() {
 		return selectedPort;
 	}
 	
 	public int getRole() {
 		// LATER: This is kinda hacky, it assumes the order of the 
 		// options are the value of the role, which may not always be true
 		return roleComboBox.getSelectedIndex();
 	}
 	
 	public String getPassword() {
 		return passwordPasswordField.getText();
 	}
 	
 	/**
 	 * This method initializes jComboBox	
 	 * 	
 	 * @return javax.swing.JComboBox	
 	 */    
 	private JComboBox getRoleComboBox() {
 		if (roleComboBox == null) {
 			roleComboBox = new JComboBox(new String[]{"Player", "GM"});
 		}
 		return roleComboBox;
 	}
 	/**
 	 * This method initializes passwordPasswordField	
 	 * 	
 	 * @return javax.swing.JPasswordField	
 	 */
 	private JPasswordField getPasswordPasswordField() {
 		if (passwordPasswordField == null) {
 			passwordPasswordField = new JPasswordField();
 		}
 		return passwordPasswordField;
 	}
 	/**
 	 * This method initializes typeTabbedPane	
 	 * 	
 	 * @return javax.swing.JTabbedPane	
 	 */
 	private JTabbedPane getTypeTabbedPane() {
 		if (typeTabbedPane == null) {
 			typeTabbedPane = new JTabbedPane();
 			typeTabbedPane.addTab("LAN", null, getLanPanel(), null);
 			typeTabbedPane.addTab("RPTools.net", null, getRptoolsPanel(), null);
			typeTabbedPane.addTab("Internet", null, getWanPanel(), null);
 		}
 		return typeTabbedPane;
 	}
 	/**
 	 * This method initializes lanPanel	
 	 * 	
 	 * @return javax.swing.JPanel	
 	 */
 	private JPanel getWanPanel() {
 		if (wanPanel == null) {
 			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
 			gridBagConstraints11.gridx = 1;
 			gridBagConstraints11.weightx = 1.0;
 			gridBagConstraints11.weighty = 1.0;
 			gridBagConstraints11.gridy = 2;
 			spacerLabel = new JLabel();
 			spacerLabel.setText("  ");
 			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
 			gridBagConstraints10.gridx = 0;
 			gridBagConstraints10.anchor = java.awt.GridBagConstraints.WEST;
 			gridBagConstraints10.insets = new java.awt.Insets(0,0,0,5);
 			gridBagConstraints10.gridy = 1;
 			portLabel = new JLabel();
 			portLabel.setText("Port:");
 			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
 			gridBagConstraints9.gridx = 0;
 			gridBagConstraints9.anchor = java.awt.GridBagConstraints.WEST;
 			gridBagConstraints9.insets = new java.awt.Insets(0,0,0,5);
 			gridBagConstraints9.gridy = 0;
 			serverLabel = new JLabel();
 			serverLabel.setText("Address:");
 			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
 			gridBagConstraints8.fill = GridBagConstraints.HORIZONTAL;
 			gridBagConstraints8.gridy = 1;
 			gridBagConstraints8.weightx = 1.0;
 			gridBagConstraints8.gridx = 1;
 			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
 			gridBagConstraints7.fill = GridBagConstraints.HORIZONTAL;
 			gridBagConstraints7.gridy = 0;
 			gridBagConstraints7.weightx = 1.0;
 			gridBagConstraints7.gridx = 1;
 			wanPanel = new JPanel();
 			wanPanel.setLayout(new GridBagLayout());
 			wanPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5,5,5,5));
 			wanPanel.add(getServerTextField(), gridBagConstraints7);
 			wanPanel.add(getPortTextField(), gridBagConstraints8);
 			wanPanel.add(serverLabel, gridBagConstraints9);
 			wanPanel.add(portLabel, gridBagConstraints10);
 			wanPanel.add(spacerLabel, gridBagConstraints11);
 		}
 		return wanPanel;
 	}
 	/**
 	 * This method initializes internetPanel	
 	 * 	
 	 * @return javax.swing.JPanel	
 	 */
 	private JPanel getLanPanel() {
 		if (lanPanel == null) {
 			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
 			gridBagConstraints6.gridx = 0;
 			gridBagConstraints6.anchor = java.awt.GridBagConstraints.EAST;
 			gridBagConstraints6.insets = new java.awt.Insets(4,0,0,0);
 			gridBagConstraints6.gridy = 2;
 			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
 			gridBagConstraints5.fill = java.awt.GridBagConstraints.BOTH;
 			gridBagConstraints5.gridy = 1;
 			gridBagConstraints5.weightx = 1.0;
 			gridBagConstraints5.weighty = 1.0;
 			gridBagConstraints5.gridx = 0;
 			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
 			gridBagConstraints2.gridx = 0;
 			gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
 			gridBagConstraints2.insets = new java.awt.Insets(0,0,4,0);
 			gridBagConstraints2.gridy = 0;
 			localServersLabel = new JLabel();
 			localServersLabel.setText("Local Servers:");
 			lanPanel = new JPanel();
 			lanPanel.setLayout(new GridBagLayout());
 			lanPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5,5,5,5));
 			lanPanel.add(localServersLabel, gridBagConstraints2);
 			lanPanel.add(getServerList(), gridBagConstraints5);
 			lanPanel.add(getRescanButton(), gridBagConstraints6);
 		}
 		return lanPanel;
 	}
 	/**
 	 * This method initializes serverList	
 	 * 	
 	 * @return javax.swing.JList	
 	 */
 	private JList getServerList() {
 		if (serverList == null) {
 			serverList = new JList(new DefaultListModel());
 			serverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			serverList.addMouseListener(new MouseAdapter() {
 				public void mouseClicked(MouseEvent e) {
 					if (e.getClickCount() == 2) {
 						handleOK();
 					}
 				};
 			});
 		}
 		return serverList;
 	}
 	/**
 	 * This method initializes rescanButton	
 	 * 	
 	 * @return javax.swing.JButton	
 	 */
 	private JButton getRescanButton() {
 		if (rescanButton == null) {
 			rescanButton = new JButton();
 			rescanButton.setText("Rescan");
 			rescanButton.addActionListener(new ActionListener() {
 				
 				public void actionPerformed(ActionEvent e) {
 					((DefaultListModel)getServerList().getModel()).clear();
 					finder.find();
 				}
 			});
 		}
 		return rescanButton;
 	}
 	
 	private void handleOK() {
 		if (usernameTextField.getText().length() == 0) {
 			MapTool.showError("Must supply a username");
 			return;
 		}					
 
 		switch (getTypeTabbedPane().getSelectedIndex()) {
 		// LAN
 		case 0:
 			if (getServerList().getSelectedIndex() < 0) {
 				MapTool.showError("Must select a server");
 				return;
 			}
 			
 			// OK
 			ServerInfo info = (ServerInfo) getServerList().getSelectedValue();
 			selectedPort = info.port;
 			selectedServerAddress = info.address.getHostAddress();
 			
 			break;
 			
 	    // Internet
		case 1:
 			// TODO: put these into a validation method
 			if (portTextField.getText().length() == 0) {
 				MapTool.showError("Must supply a port");
 				return;
 			}
 			try {
 				Integer.parseInt(portTextField.getText());
 			} catch (NumberFormatException nfe) {
 				MapTool.showError("Port must be numeric");
 				return;
 			}
 
 			if (serverTextField.getText().length() == 0) {
 				MapTool.showError("Must supply a server");
 				return;
 			}					
 
 			// OK
 			selectedPort = Integer.parseInt(getPortTextField().getText());
 			selectedServerAddress = getServerTextField().getText();
 			break;
 			
 		// RPTools.net
		case 2:
 			
 			if (serverNameTextField.getText().length() == 0) {
 				MapTool.showError("Must supply a server name");
 				return;
 			}
 			
 			// Do the lookup
 			String serverInfo = MapToolRegistry.findInstance(serverNameTextField.getText(), serverPasswordField.getText());
 			if (serverInfo == null || serverInfo.length() == 0) {
 				MapTool.showError("Could not find that server.");
 				return;
 			}
 			
 			String[] data = serverInfo.split(":");
 			selectedServerAddress = data[0];
 			selectedPort = Integer.parseInt(data[1]);
 			break;
 		}
 		
 		option = OPTION_OK;
 		setVisible(false);
 		
 		// Prefs
 		ConnectToServerDialogPreferences prefs = new ConnectToServerDialogPreferences();
 		prefs.setUsername(getUsername());
 		prefs.setHost(getServer());
 		prefs.setPort(getPort());
 		prefs.setRole(getRole());
 		prefs.setPassword(getPassword());
 		prefs.setTab(getTypeTabbedPane().getSelectedIndex());		
 		prefs.setServerName(getServerNameTextField().getText());
 		prefs.setServerPassword(getServerPasswordField().getText());
 	}
 
 	////
 	// ANNOUNCEMENT LISTENER
 	public void serviceAnnouncement(String type, InetAddress address, int port, byte[] data) {
 		((DefaultListModel)getServerList().getModel()).addElement(new ServerInfo(new String(data), address, port));
 	}
 
 	private class ServerInfo {
 		
 		String id;
 		InetAddress address;
 		int port;
 		
 		public ServerInfo (String id, InetAddress address, int port) {
 			this.id = id;
 			this.address = address;
 			this.port = port;
 			
 		}
 		
 		public String toString() {
 			return id;
 		}
 	}
 
 	/**
 	 * This method initializes rptoolsPanel	
 	 * 	
 	 * @return javax.swing.JPanel	
 	 */
 	private JPanel getRptoolsPanel() {
 		if (rptoolsPanel == null) {
 			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
 			gridBagConstraints20.fill = java.awt.GridBagConstraints.HORIZONTAL;
 			gridBagConstraints20.gridy = 1;
 			gridBagConstraints20.weightx = 1.0;
 			gridBagConstraints20.gridx = 1;
 			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
 			gridBagConstraints19.fill = java.awt.GridBagConstraints.HORIZONTAL;
 			gridBagConstraints19.gridy = 0;
 			gridBagConstraints19.weightx = 1.0;
 			gridBagConstraints19.gridx = 1;
 			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
 			gridBagConstraints18.gridx = 0;
 			gridBagConstraints18.weighty = 1.0;
 			gridBagConstraints18.gridy = 2;
 			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
 			gridBagConstraints16.gridx = 0;
 			gridBagConstraints16.anchor = java.awt.GridBagConstraints.WEST;
 			gridBagConstraints16.insets = new java.awt.Insets(0,0,0,5);
 			gridBagConstraints16.gridy = 1;
 			serverPasswordLabel = new JLabel();
 			serverPasswordLabel.setText("Password:");
 			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
 			gridBagConstraints15.gridx = 0;
 			gridBagConstraints15.anchor = java.awt.GridBagConstraints.WEST;
 			gridBagConstraints15.insets = new java.awt.Insets(0,0,0,5);
 			gridBagConstraints15.gridy = 0;
 			serverNameLabel = new JLabel();
 			serverNameLabel.setText("Server Name:");
 			rptoolsPanel = new JPanel();
 			rptoolsPanel.setLayout(new GridBagLayout());
 			rptoolsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5,5,5,5));
 			rptoolsPanel.add(serverNameLabel, gridBagConstraints15);
 			rptoolsPanel.add(serverPasswordLabel, gridBagConstraints16);
 			rptoolsPanel.add(getSpacerPanel(), gridBagConstraints18);
 			rptoolsPanel.add(getServerNameTextField(), gridBagConstraints19);
 			rptoolsPanel.add(getServerPasswordField(), gridBagConstraints20);
 		}
 		return rptoolsPanel;
 	}
 	/**
 	 * This method initializes spacerPanel	
 	 * 	
 	 * @return javax.swing.JPanel	
 	 */
 	private JPanel getSpacerPanel() {
 		if (spacerPanel == null) {
 			spacerPanel = new JPanel();
 		}
 		return spacerPanel;
 	}
 	/**
 	 * This method initializes serverNameTextField	
 	 * 	
 	 * @return javax.swing.JTextField	
 	 */
 	private JTextField getServerNameTextField() {
 		if (serverNameTextField == null) {
 			serverNameTextField = new JTextField();
 		}
 		return serverNameTextField;
 	}
 	/**
 	 * This method initializes serverPasswordField	
 	 * 	
 	 * @return javax.swing.JPasswordField	
 	 */
 	private JPasswordField getServerPasswordField() {
 		if (serverPasswordField == null) {
 			serverPasswordField = new JPasswordField();
 		}
 		return serverPasswordField;
 	}
 	
 }  //  @jve:decl-index=0:visual-constraint="10,10"
