 package edu.cs319.client;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import edu.cs319.client.customcomponents.JChatPanel;
 import edu.cs319.client.customcomponents.JDocTabPanel;
 import edu.cs319.client.customcomponents.JRoomListPanel;
 import edu.cs319.client.customcomponents.SubSectionList;
 import edu.cs319.connectionmanager.clientside.Proxy;
 import edu.cs319.dataobjects.DocumentSubSection;
 import edu.cs319.dataobjects.SectionizedDocument;
 import edu.cs319.dataobjects.impl.DocumentInfoImpl;
 import edu.cs319.dataobjects.impl.DocumentSubSectionImpl;
 import edu.cs319.server.CoLabPrivilegeLevel;
 import edu.cs319.util.Util;
 
 /**
  * 
  * @author Amelia Gee
  * @author Justin Nelson
  * 
  */
 public class WindowClient extends JFrame implements IClient {
 
 	private Proxy proxy;
 
 	private WindowJoinCoLab colabRoomFrame;
 
 	private String userName;
 	private String roomName;
 
 	private JTabbedPane documentPane;
 	private Map<String, JDocTabPanel> documents;
 
 	private JRoomListPanel roomMemberListPanel;
 	private JChatPanel chatPanel;
 
 	private JMenuItem logIn;
 	private JMenuItem joinCoLabRoom;
 	private JMenuItem disconnect;
 	private JMenuItem exitCoLab;
 	private JMenuItem newDocument;
 	private JMenuItem openDocument;
 	private JMenuItem removeDocument;
 	private JMenuItem saveDocument;
 	private JMenuItem addSection;
 	private JMenuItem deleteSection;
 	private JMenuItem splitSection;
 	private JMenuItem mergeSection;
 	private final JCheckBox showRoomMembers = new JCheckBox("Display Room Members Window");
 	private final JCheckBox showChat = new JCheckBox("Display Chat Window");
 	private JMenuItem about;
 
 	public WindowClient() {
 		// setLookAndFeel();
 		setTitle("CoLab");
 		setSize(new Dimension(1000, 500));
 		setJMenuBar(createMenuBar());
 		setListeners();
 
 		roomMemberListPanel = new JRoomListPanel();
 		documentPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
 		documents = new HashMap<String, JDocTabPanel>();
 		chatPanel = new JChatPanel();
 
 		JPanel panel = new JPanel(new BorderLayout(10, 10));
 		panel.add(roomMemberListPanel, BorderLayout.WEST);
 		panel.add(documentPane, BorderLayout.CENTER);
 		panel.add(chatPanel, BorderLayout.EAST);
 		add(panel);
 	}
 
 	private JMenuBar createMenuBar() {
 		JMenuBar mainMenu = new JMenuBar();
 		JMenu file = new JMenu("File");
 		JMenu connect = new JMenu("Connect");
 		JMenu doc = new JMenu("Document");
 		JMenu view = new JMenu("View");
 		JMenu help = new JMenu("Help");
 		logIn = new JMenuItem("Log In");
 		joinCoLabRoom = new JMenuItem("Join CoLab Room");
 		disconnect = new JMenuItem("Disconnect");
 		exitCoLab = new JMenuItem("Exit CoLab");
 		newDocument = new JMenuItem("Add New Document");
 		openDocument = new JMenuItem("Open Document From File");
 		removeDocument = new JMenuItem("Remove Document");
 		saveDocument = new JMenuItem("Save Document");
 		addSection = new JMenuItem("Add Section");
 		deleteSection = new JMenuItem("Delete Section");
 		splitSection = new JMenuItem("Split Section");
 		mergeSection = new JMenuItem("Merge Section");
 		about = new JMenuItem("About CoLab");
 
 		file.setMnemonic(KeyEvent.VK_F);
 		doc.setMnemonic(KeyEvent.VK_D);
 		view.setMnemonic(KeyEvent.VK_V);
 		help.setMnemonic(KeyEvent.VK_H);
 		logIn.setMnemonic(KeyEvent.VK_L);
 		joinCoLabRoom.setMnemonic(KeyEvent.VK_J);
 		disconnect.setMnemonic(KeyEvent.VK_D);
 		exitCoLab.setMnemonic(KeyEvent.VK_X);
 		newDocument.setMnemonic(KeyEvent.VK_N);
 		openDocument.setMnemonic(KeyEvent.VK_O);
 		removeDocument.setMnemonic(KeyEvent.VK_R);
 		saveDocument.setMnemonic(KeyEvent.VK_S);
 		addSection.setMnemonic(KeyEvent.VK_A);
 		deleteSection.setMnemonic(KeyEvent.VK_D);
 		splitSection.setMnemonic(KeyEvent.VK_S);
 		mergeSection.setMnemonic(KeyEvent.VK_M);
 		showRoomMembers.setMnemonic(KeyEvent.VK_R);
 		showChat.setMnemonic(KeyEvent.VK_C);
 		about.setMnemonic(KeyEvent.VK_A);
 
 		file.add(connect);
 		connect.add(logIn);
 		connect.add(joinCoLabRoom);
 		file.add(disconnect);
 		file.addSeparator();
 		file.add(exitCoLab);
 		doc.add(newDocument);
 		doc.add(openDocument);
 		doc.add(removeDocument);
 		doc.add(saveDocument);
 		doc.addSeparator();
 		doc.add(addSection);
 		doc.add(deleteSection);
 		doc.add(splitSection);
 		doc.add(mergeSection);
 		view.add(showChat);
 		view.add(showRoomMembers);
 		help.add(about);
 
 		setDisconnected();
 		showChat.setSelected(true);
 		showRoomMembers.setSelected(true);
 
 		mainMenu.add(file);
 		mainMenu.add(doc);
 		mainMenu.add(view);
 		mainMenu.add(help);
 		return mainMenu;
 	}
 
 	private void setListeners() {
 		// FILE menu items
 		// CONNECT menu items
 		logIn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				proxy = WindowLogIn.showLoginWindow(WindowClient.this, WindowClient.this);
 				if (proxy != null) {
 					colabRoomFrame = new WindowJoinCoLab(WindowClient.this, proxy.getServer());
 					setLogIn();
 				}
 			}
 		});
 		joinCoLabRoom.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int result = colabRoomFrame.showRoomDialogue();
 				if (result == WindowJoinCoLab.ROOM_JOINED) {
 					setJoinedRoom();
 				}
 			}
 		});
 		disconnect.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				proxy.getServer().leaveCoLabRoom(userName, roomName);
 				try {
 					proxy.close();
 				} catch (IOException io) {
 					if (Util.DEBUG) {
 						io.printStackTrace();
 					}
 				}
 			}
 		});
 		exitCoLab.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				WindowClient.this.processWindowEvent(new WindowEvent(WindowClient.this,
 						WindowEvent.WINDOW_CLOSING));
 			}
 		});
 
 		// DOCUMENT menu items
 		newDocument.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				String docName = JOptionPane.showInputDialog(WindowClient.this,
 						"Enter the name of the document:");
 				String secName = JOptionPane.showInputDialog(WindowClient.this,
 						"Enter the name of the subsection:");
 				if (docName == null || secName == null)
 					return;
 				proxy.getServer().newDocument(userName, roomName, docName);
 				DocumentSubSection section = new DocumentSubSectionImpl(secName);
 				section.setLocked(true, userName);
 				proxy.getServer().newSubSection(userName, roomName, docName, secName, 0);
 				proxy.getServer().subSectionLocked(userName, roomName, docName, secName);
 				System.out.println("WindowClient New Blank Document: Username: " + userName
 						+ " DocumentName: " + docName + " SectionName: " + secName
 						+ " LockHolder: " + section.lockedByUser());
 				proxy.getServer().subSectionUpdated(userName, roomName, docName, secName, section);
 				setDocumentsOpen();
 			}
 		});
 
 		openDocument.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser choose = new JFileChooser();
 				int choice = choose.showOpenDialog(WindowClient.this);
 				if (choice != JFileChooser.APPROVE_OPTION)
 					return;
 				String docName = JOptionPane.showInputDialog(WindowClient.this,
 						"Enter the name of the document:");
 				String secName = JOptionPane.showInputDialog(WindowClient.this,
 						"Enter the name of the subsection:");
 				if (docName == null || secName == null)
 					return;
 				File choiceF = choose.getSelectedFile();
 				proxy.getServer().newDocument(userName, roomName, docName);
 				DocumentSubSection section = new DocumentSubSectionImpl(secName);
 				section.setLocked(true, userName);
 				proxy.getServer().newSubSection(userName, roomName, docName, secName, 0);
 				proxy.getServer().subSectionLocked(userName, roomName, docName, secName);
 				try {
 					section.setText(userName, new Scanner(choiceF).useDelimiter("//Z").next());
 				} catch (FileNotFoundException e1) {
 					if (Util.DEBUG)
 						e1.printStackTrace();
 					JOptionPane.showMessageDialog(null, "Shit, file could not be opened!!!!");
 					return;
 				}
 				System.out.println("WindowClient Upload Document: Username: " + userName
 						+ " DocumentName: " + docName + " SectionName: " + secName
 						+ " LockHolder: " + section.lockedByUser());
 				proxy.getServer().subSectionUpdated(userName, roomName, docName, secName, section);
 				setDocumentsOpen();
 			}
 		});
 		removeDocument.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO NOT COMPLETE YET - agee
 				JDocTabPanel doc = (JDocTabPanel) documentPane.getSelectedComponent();
 				documentPane.remove(doc);
 				proxy.getServer().documentRemoved(userName, roomName, doc.getName());
 				documents.remove(doc.getName());
 				if (documents.size() == 0) {
 					// Sets menus enabled for user in room with no documents
 					setJoinedRoom();
 				}
 			}
 		});
 		saveDocument.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser choose = new JFileChooser();
 				int choice = choose.showSaveDialog(WindowClient.this);
 				if (choice != JFileChooser.APPROVE_OPTION)
 					return;
 				File f = choose.getSelectedFile();
 				PrintStream out = null;
 				try {
 					out = new PrintStream(f);
 				} catch (FileNotFoundException e1) {
 					if (Util.DEBUG)
 						e1.printStackTrace();
 					JOptionPane.showMessageDialog(WindowClient.this, "Oops, error",
 							"The file could not be saved.  Try again maybe??",
 							JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				JDocTabPanel selectedTab = (JDocTabPanel) documentPane.getSelectedComponent();
 				String docName = selectedTab.getName();
 				out.println(documents.get(docName).getSectionizedDocument().getFullText());
 			}
 		});
 		addSection.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 			}
 		});
 		deleteSection.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 			}
 		});
 		splitSection.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 			}
 		});
 		mergeSection.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 		// VIEW menu items
 		showRoomMembers.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				roomMemberListPanel.setVisible(showRoomMembers.isSelected());
 			}
 		});
 		showChat.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				chatPanel.setVisible(showChat.isSelected());
 			}
 		});
 
 		// HELP menu items
 		about.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO make info window
 
 			}
 		});
 
 		this.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				if (disconnect.isEnabled()) {
 					proxy.getServer().leaveCoLabRoom(userName, roomName);
 					proxy.getServer().logOut(userName);
 				}
 				chatPanel.shutdownTray();
 			}
 		});
 	}
 
 	/**
 	 * Set menu items enabled/disabled for when user is logged in.
 	 */
 	private void setLogIn() {
 		logIn.setEnabled(false);
 		joinCoLabRoom.setEnabled(true);
 		disconnect.setEnabled(true);
 		newDocument.setEnabled(false);
 		openDocument.setEnabled(false);
 		removeDocument.setEnabled(false);
 		saveDocument.setEnabled(false);
 		addSection.setEnabled(false);
 		deleteSection.setEnabled(false);
 		splitSection.setEnabled(false);
 		mergeSection.setEnabled(false);
 		String title = getTitle();
 		if (userName != null)
 			setTitle(title + " - " + getUserName());
 	}
 
 	/**
 	 * Set menu items enabled/disabled for when user has joined a CoLab Room or when all documents
 	 * are removed from CoLab Room.
 	 */
 	private void setJoinedRoom() {
 		logIn.setEnabled(false);
 		joinCoLabRoom.setEnabled(false);
 		disconnect.setEnabled(true);
 		newDocument.setEnabled(true);
 		openDocument.setEnabled(true);
 		removeDocument.setEnabled(false);
 		saveDocument.setEnabled(false);
 		addSection.setEnabled(false);
 		deleteSection.setEnabled(false);
 		splitSection.setEnabled(false);
 		mergeSection.setEnabled(false);
 		String title = getTitle();
 		if (roomName != null)
 			setTitle(title + " - " + getRoomName());
 	}
 
 	/**
 	 * Set menu items enabled/disabled for when documents are open in a CoLab Room.
 	 */
 	private void setDocumentsOpen() {
 		logIn.setEnabled(false);
 		joinCoLabRoom.setEnabled(false);
 		disconnect.setEnabled(true);
 		newDocument.setEnabled(true);
 		openDocument.setEnabled(true);
 		removeDocument.setEnabled(true);
 		saveDocument.setEnabled(true);
 		addSection.setEnabled(true);
 		deleteSection.setEnabled(true);
 		splitSection.setEnabled(true);
 		mergeSection.setEnabled(true);
 	}
 
 	/**
 	 * Set menu items enabled/disabled for when user is disconnected.
 	 */
 	private void setDisconnected() {
 		logIn.setEnabled(true);
 		joinCoLabRoom.setEnabled(false);
 		disconnect.setEnabled(false);
 		newDocument.setEnabled(false);
 		openDocument.setEnabled(false);
 		removeDocument.setEnabled(false);
 		saveDocument.setEnabled(false);
 		addSection.setEnabled(false);
 		deleteSection.setEnabled(false);
 		splitSection.setEnabled(false);
 		mergeSection.setEnabled(false);
 	}
 
 	@Override
 	public boolean allCoLabRooms(Collection<String> roomNames) {
 		if (colabRoomFrame == null) {
 			if (Util.DEBUG) {
 				System.out.println("Client was sent list of all rooms before frame was created");
 			}
 			return false;
 		}
 		colabRoomFrame.roomsUpdated(roomNames);
 		return true;
 	}
 
 	@Override
 	public boolean allUsersInRoom(List<String> usernames, List<CoLabPrivilegeLevel> privs) {
 		roomMemberListPanel.updateList(usernames, privs);
 		return true;
 	}
 
 	@Override
 	public boolean changeUserPrivilege(String username, CoLabPrivilegeLevel newPriv) {
 		return this.roomMemberListPanel.setUserPrivledge(username, newPriv);
 	}
 
 	@Override
 	public boolean coLabRoomMemberArrived(String username) {
 		chatPanel.newChatMessage("Server", "<New Chat Member '" + username + "'>");
 		return roomMemberListPanel.addUser(username);
 	}
 
 	@Override
 	public boolean coLabRoomMemberLeft(String username) {
 		chatPanel.newChatMessage("Server", "<Chat Member Left '" + username + "'>");
 		return roomMemberListPanel.removeUser(username);
 	}
 
 	@Override
 	public boolean newChatMessage(String usernameSender, String message) {
 		chatPanel.newChatMessage(usernameSender, message);
 		return true;
 	}
 
 	@Override
 	public boolean newChatMessage(String usernameSender, String message, String recipiant) {
 		chatPanel.newChatMessage(usernameSender, message, recipiant);
 		return true;
 	}
 
 	@Override
 	public boolean newSubSection(String username, String documentName, String sectionID,
 			DocumentSubSection section, int idx) {
 		if (Util.DEBUG)
 			System.out.println("WindowClient New SubSection: Username: " + username + " Document: "
 					+ documentName + " SectionID: " + sectionID + " LockHolder: "
 					+ section.lockedByUser());
 
 		SectionizedDocument doc = documents.get(documentName).getSectionizedDocument();
 		doc.addSubSection(section, idx);
 		return true;
 	}
 
 	@Override
 	public boolean newDocument(String username, String documentName) {
 		JDocTabPanel doc = documents.get(documentName);
 		if (doc != null) {
 			throw new IllegalStateException("Two documents cannot have the same name");
 		}
 		doc = new JDocTabPanel(new DocumentInfoImpl(proxy.getServer(), roomName, documentName,
 				userName));
 		documents.put(documentName, doc);
 		documentPane.add(documentName, doc);
 		System.out.println("WindowClient New Document: Username: " + username + " DocumentName: "
 				+ documentName);
 		// TODO keep update? documents.get(documentName).updateDocumentView();
 		setDocumentsOpen();
 		return true;
 
 	}
 
 	@Override
 	public boolean removeDocument(String username, String documentName) {
 		JDocTabPanel doc = documents.get(documentName);
 		if (doc == null) {
 			throw new IllegalStateException("This document does not exist");
 		}
 		documents.remove(documentName);
 		documentPane.remove(doc);
 		// documents.get(documentName).updateDocPane();
 		if (documents.size() == 0) {
 			// Sets menus enabled for user in room with no documents
 			setJoinedRoom();
 		}
 		// TODO make sure this works!
 		// documents.get(((JDocTabPanel) documentPane.getSelectedComponent()).getName())
 		// .updateDocPane();
 		documents.get(documentName).updateDocumentView();
 		return true;
 	}
 
 	@Override
 	public boolean subsectionLocked(String usernameSender, String documentName, String sectionId) {
 		SubSectionList doc = (SubSectionList) documents.get(documentName).getSectionizedDocument();
 		DocumentSubSection hi = doc.getSection(sectionId);
 		hi.setLocked(true, usernameSender);
 		doc.subSectionUpdated(hi);
 		System.out.println("WindowClient SubSection Locked: Username: " + usernameSender
 				+ " Document: " + documentName + " SectionName: " + sectionId + " LockHolder: "
 				+ hi.lockedByUser());
 		return true;
 	}
 
 	@Override
 	public boolean subsectionUnLocked(String usernameSender, String documentName, String sectionId) {
 		SubSectionList doc = (SubSectionList) documents.get(documentName).getSectionizedDocument();
 		DocumentSubSection sec = doc.getSection(sectionId);
 		sec.setLocked(false, usernameSender);
 		doc.subSectionUpdated(sec);
 		return true;
 	}
 
 	@Override
 	public boolean subsectionFlopped(String usernameSender, String documentName,
 			String sectionIDMoveUp, String sectionIDMoveDown) {
 		SectionizedDocument doc = documents.get(documentName).getSectionizedDocument();
 		int idx1 = doc.getSubSectionIndex(sectionIDMoveUp);
 		int idx2 = doc.getSubSectionIndex(sectionIDMoveDown);
 		doc.flopSubSections(idx1, idx2);
 		// TODO need method to do this in the tabbed doc pane
 		// documents.get(documentName).updateDocPane();
 		documents.get(documentName).updateDocumentView();
 		return true;
 	}
 
 	@Override
 	public boolean subSectionRemoved(String username, String sectionId, String documentName) {
 		SectionizedDocument doc = documents.get(documentName).getSectionizedDocument();
 		doc.removeSubSection(sectionId);
 		// TODO need method to do this in the tabbed doc pane
 		// documents.get(documentName).updateDocPane();
 		documents.get(documentName).updateDocumentView();
 		return true;
 	}
 
 	@Override
 	public boolean updateAllSubsections(String documentId, List<DocumentSubSection> allSections) {
 		SectionizedDocument doc = documents.get(documentId).getSectionizedDocument();
 		doc.removeAllSubSections();
 		doc.addAllSubSections(allSections);
 		documents.get(documentId).updateDocumentView();
 		return true;
 	}
 
 	@Override
 	public boolean updateSubsection(String usernameSender, String documentname,
 			DocumentSubSection section, String sectionID) {
 		JDocTabPanel docPane = documents.get(documentname);
 		SectionizedDocument doc = documents.get(documentname).getSectionizedDocument();
 		doc.getSection(sectionID).setText(usernameSender, section.getText());
 		documents.get(documentname).updateDocumentView();
		if (userName.equals(usernameSender)) {
 			docPane.updateWorkPane(section);
 		}
 		return true;
 	}
 
 	@Override
 	public boolean subSectionSplit(String username, String documentName, String oldSecName,
 			String newName1, String newName2, int index) {
 		JDocTabPanel docPane = documents.get(documentName);
 		docPane.getSectionizedDocument().splitSubSection(oldSecName, newName1, newName2, index,
 				username);
 		docPane.updateDocumentView();
		if (username.equals(userName)) {
 			((SubSectionList) docPane.getSectionizedDocument()).setSelectedValue(docPane
 					.getSectionizedDocument().getSection(newName1), true);
 			docPane.updateWorkPane(newName1);
		}
 		return true;
 	}
 
 	@Override
 	public boolean subSectionCombined(String username, String documentName, String sectionA,
 			String sectionB, String newSection) {
 		JDocTabPanel docPane = documents.get(documentName);
 		documents.get(documentName).getSectionizedDocument().combineSubSections(sectionA, sectionB,
 				newSection);
 		documents.get(documentName).updateDocumentView();
		if (username.equals(userName)) {
 			((SubSectionList) docPane.getSectionizedDocument()).setSelectedValue(docPane
 					.getSectionizedDocument().getSection(newSection), true);
 			docPane.updateWorkPane(newSection);
		}
 		return true;
 	}
 
 	@Override
 	public String getUserName() {
 		return userName;
 	}
 
 	public void setUserName(String un) {
 		userName = un;
 	}
 
 	public String getRoomName() {
 		return roomName;
 	}
 
 	public void setRoomName(String rn) {
 		roomName = rn;
 	}
 
 	public void chatLogin() {
 		chatPanel.connect(proxy.getServer(), userName, roomName);
 	}
 
 	/**
 	 * Sets the look and feel of an application to that of the system it is running on. (Java's
 	 * default looks bad)
 	 */
 	private static void setLookAndFeel() {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
