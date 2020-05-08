 import java.awt.*;
 import javax.swing.*;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.util.List;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.LayoutStyle.ComponentPlacement;
 
 
 public class GroupServerClientFrame extends JInternalFrame {
 	
 	private static final long serialVersionUID = -5379369279411541444L;
 
 	private ClientApplication parentApp;
 	
 	private JTextField usernameField;
 	private JLabel lblUser;
 	private JTextField userField;
 	private JLabel lblGroup;
 	private JButton btnCreateUser;
 	private JButton btnDeleteUser;
 	private JTextField groupField;
 	private JButton btnCreateGroup;
 	private JButton btnNewButton_1;
 	private JButton btnAddUserTo;
 	private JButton btnListMembersOf;
 	private JTextPane membersTextPane;
 	private JButton btnDisconnect;
 	private JLabel lblServer_1;
 	private JLabel lblPort_1;
 	private JTextField fileserverField;
 	private JTextField fileserverportField;
 	private JButton btnDeleteUserFrom;
 	private JPanel groupActionsPanel;
 	private JPanel connectFileServerPanel;
 	private JPanel panel_2;
 
 	/**
 	 * Create the frame.
 	 */
 	public GroupServerClientFrame(ClientApplication parentApp_) {
 		setTitle("Group Server Client");
 		parentApp = parentApp_;
 		
 		setIconifiable(true);
 		setResizable(true);
 		setMaximizable(true);
 		setBounds(25, 69, 600, 528);
 		
 		getContentPane().setLayout(null);
 		
 		JPanel topPanel = new JPanel();
 		topPanel.setBounds(12, 12, 524, 461);
 		getContentPane().add(topPanel);
 		
 		JPanel getTokenPanel = new JPanel();
 		
 		groupActionsPanel = new JPanel();
 		
 		connectFileServerPanel = new JPanel();
 		GroupLayout gl_topPanel = new GroupLayout(topPanel);
 		gl_topPanel.setHorizontalGroup(
 			gl_topPanel.createParallelGroup(Alignment.TRAILING)
 				.addGroup(gl_topPanel.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_topPanel.createParallelGroup(Alignment.TRAILING)
 						.addComponent(connectFileServerPanel, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 500, Short.MAX_VALUE)
 						.addComponent(groupActionsPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addComponent(getTokenPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE))
 					.addContainerGap())
 		);
 		gl_topPanel.setVerticalGroup(
 			gl_topPanel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_topPanel.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(getTokenPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 					.addGap(28)
 					.addComponent(groupActionsPanel, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(connectFileServerPanel, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap(26, Short.MAX_VALUE))
 		);
 		
 		JLabel lblConnectToA = new JLabel("Connect to a File Server");
 		lblConnectToA.setHorizontalAlignment(SwingConstants.CENTER);
 		
 		panel_2 = new JPanel();
 		
 		lblServer_1 = new JLabel("Server:");
 		panel_2.add(lblServer_1);
 		
 		fileserverField = new JTextField();
 		panel_2.add(fileserverField);
 		fileserverField.setColumns(10);
 		
 		lblPort_1 = new JLabel("Port:");
 		panel_2.add(lblPort_1);
 		
 		fileserverportField = new JTextField();
 		panel_2.add(fileserverportField);
 		fileserverportField.setColumns(10);
 		
 		JButton btnConnectFileServer = new JButton("Connect");
 		panel_2.add(btnConnectFileServer);
 		GroupLayout gl_connectFileServerPanel = new GroupLayout(connectFileServerPanel);
 		gl_connectFileServerPanel.setHorizontalGroup(
 			gl_connectFileServerPanel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_connectFileServerPanel.createSequentialGroup()
 					.addComponent(lblConnectToA, GroupLayout.PREFERRED_SIZE, 500, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 				.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
 		);
 		gl_connectFileServerPanel.setVerticalGroup(
 			gl_connectFileServerPanel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_connectFileServerPanel.createSequentialGroup()
 					.addComponent(lblConnectToA)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 					.addGap(31))
 		);
 		connectFileServerPanel.setLayout(gl_connectFileServerPanel);
 		btnConnectFileServer.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				connectAction();
 			}
 		});
 		
 		lblGroup = new JLabel("Group:");
 		
 		lblUser = new JLabel("User:");
 		
 		userField = new JTextField();
 		userField.setColumns(10);
 		
 		groupField = new JTextField();
 		groupField.setColumns(10);
 		
 		btnCreateGroup = new JButton("Create Group");
 		btnCreateGroup.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				createGroupAction();
 			}
 		});
 		
 		btnCreateUser = new JButton("Create User");
 		btnCreateUser.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				createUserAction();
 			}
 		});
 		
 		btnNewButton_1 = new JButton("Delete Group");
 		btnNewButton_1.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				deleteGroupAction();
 			}
 		});
 		
 		btnDeleteUser = new JButton("Delete User");
 		btnDeleteUser.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				deleteUserAction();
 			}
 		});
 		
 		btnDeleteUserFrom = new JButton("Delete User from Group");
 		btnDeleteUserFrom.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				deleteUserFromGroupAction();
 			}
 		});
 		
 		btnAddUserTo = new JButton("Add User to Group");
 		btnAddUserTo.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				addUserToGroupAction();
 			}
 		});
 		
 		btnListMembersOf = new JButton("List Members of Group");
 		btnListMembersOf.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				listMembersAction();
 			}
 		});
 		
 		membersTextPane = new JTextPane();
 		membersTextPane.setEditable(false);
 		GroupLayout gl_groupActionsPanel = new GroupLayout(groupActionsPanel);
 		gl_groupActionsPanel.setHorizontalGroup(
 			gl_groupActionsPanel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_groupActionsPanel.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_groupActionsPanel.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_groupActionsPanel.createParallelGroup(Alignment.LEADING, false)
 							.addComponent(btnCreateGroup, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 							.addComponent(btnNewButton_1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 							.addComponent(btnDeleteUser, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 							.addComponent(btnCreateUser, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 							.addGroup(gl_groupActionsPanel.createSequentialGroup()
 								.addGroup(gl_groupActionsPanel.createParallelGroup(Alignment.TRAILING, false)
 									.addComponent(lblUser, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 									.addComponent(lblGroup, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 								.addPreferredGap(ComponentPlacement.RELATED)
 								.addGroup(gl_groupActionsPanel.createParallelGroup(Alignment.LEADING)
 									.addComponent(userField, GroupLayout.PREFERRED_SIZE, 119, GroupLayout.PREFERRED_SIZE)
 									.addComponent(groupField, GroupLayout.PREFERRED_SIZE, 119, GroupLayout.PREFERRED_SIZE))))
 						.addComponent(btnAddUserTo, GroupLayout.PREFERRED_SIZE, 235, GroupLayout.PREFERRED_SIZE)
 						.addComponent(btnDeleteUserFrom, GroupLayout.PREFERRED_SIZE, 235, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addGroup(gl_groupActionsPanel.createParallelGroup(Alignment.TRAILING, false)
 						.addComponent(membersTextPane)
 						.addComponent(btnListMembersOf, GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE))
 					.addContainerGap())
 		);
 		gl_groupActionsPanel.setVerticalGroup(
 			gl_groupActionsPanel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_groupActionsPanel.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_groupActionsPanel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblUser)
 						.addComponent(userField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(gl_groupActionsPanel.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblGroup)
 						.addComponent(groupField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addComponent(btnListMembersOf))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(gl_groupActionsPanel.createParallelGroup(Alignment.LEADING, false)
 						.addGroup(gl_groupActionsPanel.createSequentialGroup()
 							.addComponent(btnCreateUser)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(btnDeleteUser)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(btnCreateGroup)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(btnNewButton_1)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(btnAddUserTo)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(btnDeleteUserFrom))
 						.addComponent(membersTextPane)))
 		);
 		groupActionsPanel.setLayout(gl_groupActionsPanel);
 		getTokenPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 		
 		JLabel lblUsername = new JLabel("Username:");
 		getTokenPanel.add(lblUsername);
 		
 		usernameField = new JTextField();
 		getTokenPanel.add(usernameField);
 		usernameField.setColumns(10);
 		
 		JButton btnGetToken = new JButton("Get Token!");
 		getTokenPanel.add(btnGetToken);
 		
 		btnDisconnect = new JButton("Disconnect");
 		getTokenPanel.add(btnDisconnect);
 		btnDisconnect.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				disconnectAction();
 				
 			}
 		});
 		btnGetToken.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				getTokenAction();
 			}
 		});
 		topPanel.setLayout(gl_topPanel);
 		
 		// Start with the panels not visible
 		groupActionsPanel.setVisible(false);
 		connectFileServerPanel.setVisible(false);
 
 	}
 	
 	private void connectAction() {
 		String tempServer;
 		int tempPort;
 		
 		if (!(fileserverField.getText().equals("")) && !(fileserverportField.getText().equals(""))) {
 			tempServer = fileserverField.getText();
 			try {
 				tempPort = Integer.parseInt(fileserverportField.getText());
 				parentApp.fClient = new FileClient(tempServer, tempPort);
 				if(parentApp.fClient.connect() == true) {
 					parentApp.initializeFileClientWindow();
 				}
 				else {
 					JOptionPane.showMessageDialog(this, "Invalid server or port, or host down.");
 				}
 			}
 			catch (Exception e) {
 				JOptionPane.showMessageDialog(this, "Enter a number for the port!");
 			}
 		}
 		else {
 			JOptionPane.showMessageDialog(this, "Enter both a server and a port!");
 		}
 	}
 	
 	private void getTokenAction() {
 		parentApp.myToken = parentApp.gClient.getToken(usernameField.getText());
 		if (parentApp.myToken != null) {
 			groupActionsPanel.setVisible(true);
 			connectFileServerPanel.setVisible(true);
			System.out.println("Token received!\n" + parentApp.myToken.toString());
 		}
 		else {
 			JOptionPane.showMessageDialog(this, "Invalid username!");
 		}
 	}
 	
 	private void disconnectAction() {
 		parentApp.gClient.disconnect();
 		
 		if (parentApp.fClient != null) {
 			if (parentApp.fClient.isConnected()) {
 				parentApp.fClient.disconnect();
 			}
 		}
 		groupActionsPanel.setVisible(false);
 		connectFileServerPanel.setVisible(false);
 		parentApp.fileClientFrame.setVisible(false);
 		parentApp.groupClientFrame.setVisible(false);
 		parentApp.connectFrame.setVisible(true);
 	}
 	
 	private void createUserAction() {
 		if (userField.getText().equals("")) {
 			JOptionPane.showMessageDialog(this, "Enter a user to create one!");
 		}
 		else {
 			if ((parentApp.gClient.createUser(userField.getText(), parentApp.myToken)) == true) {
 				JOptionPane.showMessageDialog(this, "Created user successfully!");
 			}
 			else {
 				JOptionPane.showMessageDialog(this, "Failed to create user!");
 			}
 		}
 	}
 	
 	private void deleteUserAction() {
 		if (userField.getText().equals("")) {
 			JOptionPane.showMessageDialog(this, "Enter a user to delete one!");
 		}
 		else {
 			if ((parentApp.gClient.deleteUser(userField.getText(), parentApp.myToken)) == true) {
 				JOptionPane.showMessageDialog(this, "Deleted user successfully!");
 			}
 			else {
 				JOptionPane.showMessageDialog(this, "Failed to delete user!");
 			}
 		}
 	}
 	
 	private void createGroupAction() {
 		if (groupField.getText().equals("")) {
 			JOptionPane.showMessageDialog(this, "Enter a group to create one!");
 		}
 		else {
 			if ((parentApp.gClient.createGroup(groupField.getText(), parentApp.myToken)) == true) {
 				JOptionPane.showMessageDialog(this, "Created group successfully!");
 			}
 			else {
 				JOptionPane.showMessageDialog(this, "Failed to create group!");
 			}
 		}
 	}
 	
 	private void deleteGroupAction() {
 		if (groupField.getText().equals("")) {
 			JOptionPane.showMessageDialog(this, "Enter a group to delete one!");
 		}
 		else {
 			if ((parentApp.gClient.deleteGroup(groupField.getText(), parentApp.myToken)) == true) {
 				JOptionPane.showMessageDialog(this, "Deleted group successfully!");
 			}
 			else {
 				JOptionPane.showMessageDialog(this, "Failed to delete group!");
 			}
 		}
 	}
 	
 	private void addUserToGroupAction() {
 		if ((groupField.getText().equals("")) || (userField.getText().equals(""))) {
 			JOptionPane.showMessageDialog(this, "Enter a user AND group!");
 		}
 		else {
 			if ((parentApp.gClient.addUserToGroup(userField.getText(), groupField.getText(), parentApp.myToken)) == true) {
 				JOptionPane.showMessageDialog(this, "User added to group successfully!");
 			}
 			else {
 				JOptionPane.showMessageDialog(this, "Failed to add user to group!");
 			}
 		}
 	}
 	
 	private void deleteUserFromGroupAction() {
 		if ((groupField.getText().equals("")) || (userField.getText().equals(""))) {
 			JOptionPane.showMessageDialog(this, "Enter a user AND group!");
 		}
 		else {
 			if ((parentApp.gClient.deleteUserFromGroup(userField.getText(), groupField.getText(), parentApp.myToken)) == true) {
 				JOptionPane.showMessageDialog(this, "User deleted from group successfully!");
 			}
 			else {
 				JOptionPane.showMessageDialog(this, "Failed to delete user from group!");
 			}
 		}
 	}
 	
 	private void listMembersAction() {
 		StringBuilder listBuilder = new StringBuilder();
 		List<String> tempList;
 		String listMember;
 		
 		if (groupField.getText().equals("")) {
 			JOptionPane.showMessageDialog(this, "Enter a group to list members!");
 		}
 		else
 		{
 			tempList = parentApp.gClient.listMembers(groupField.getText(), parentApp.myToken);
 			if (tempList != null) {
 				for (int i = 0; i < tempList.size(); i++) {
 					listMember = tempList.get(i);
 					if (i > 0) {
 						listBuilder.append(", ");
 					}
 					listBuilder.append(listMember);
 				}
 				membersTextPane.setText(listBuilder.toString());
 			}
 			else {
 				JOptionPane.showMessageDialog(this, "Invalid group or no members!");
 			}
 		}
 	}
 }
