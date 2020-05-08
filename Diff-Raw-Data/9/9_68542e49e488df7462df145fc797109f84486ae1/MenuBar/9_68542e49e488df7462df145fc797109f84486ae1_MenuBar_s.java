 package com.test9.irc.display;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.KeyStroke;
 import javax.swing.event.MenuEvent;
 import javax.swing.event.MenuListener;
 
 import com.test9.irc.display.prefWins.NewServerConfigWindow;
 import com.test9.irc.display.prefWins.ServerPropertiesWindow;
 
 public class MenuBar extends JMenuBar implements MenuListener, ActionListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7455171824970879713L;
 	private static ChatWindow owner;
 	
 	int actionEvent = 0;
 	
 	/*
 	 * JIRCC Menu
 	 */
 	private static final JMenu jirccMenu = new JMenu("JIRCC");
 	private static final JMenuItem about = new JMenuItem("About");
 	private static final JMenuItem preferences = new JMenuItem("Preferences");
 	private static final JMenuItem quit = new JMenuItem("Quit");
 	
 	/*
 	 * File Menu
 	 */
 	private static final JMenu fileMenu = new JMenu("File");
 
 	/*
 	 * Edit Menu
 	 */
 	private static final JMenu editMenu = new JMenu("Edit");
 	private static final JMenuItem undo = new JMenuItem("Undo");
 	private static final JMenuItem redo = new JMenuItem("Redo");
 	private static final JMenuItem cut = new JMenuItem("Cut");
 	private static final JMenuItem copy = new JMenuItem("Copy");
 	private static final JMenuItem paste = new JMenuItem("Paste");
 	private static final JMenuItem delete = new JMenuItem("Delete");
 	private static final JMenuItem selectAll = new JMenuItem("Select All");
 
 	/*
 	 * Server Menu
 	 */
 	private static final JMenu serverMenu = new JMenu("Server");
 	private static final JMenuItem connect = new JMenuItem("Connect");
 	private static final JMenuItem disconnect = new JMenuItem("Disconnect");
 	private static final JMenuItem cancelReconnecting = new JMenuItem("Cancel Reconnecting");
 	private static final JMenuItem nickname = new JMenuItem("Nickname");
 	private static final JMenuItem channelList = new JMenuItem("Channel List");
 	private static final JMenuItem addServer = new JMenuItem("Add Server");
 	private static final JMenuItem copyServer = new JMenuItem("Copy Server");
 	private static final JMenuItem deleteServer = new JMenuItem("Delete Server");
 	private static final JMenuItem newServer = new JMenuItem("New Server");
 	private static final JMenuItem serverProperties = new JMenuItem("Server Properties");
 	
 	
 	/*
 	 * Channel Menu
 	 */
 	private static final JMenu channelMenu = new JMenu("Channel");
 	private static final JMenuItem join = new JMenuItem("Join");
 	private static final JMenuItem leave = new JMenuItem("Leave");
 	private static final JMenuItem mode = new JMenuItem("Mode");
 	private static final JMenuItem topic = new JMenuItem("Topic");
 	private static final JMenuItem addChannel = new JMenuItem("Add Channel");
 	private static final JMenuItem deleteChannel = new JMenuItem("Delete Channel");
 	private static final JMenuItem channelProperties = new JMenuItem("Channel Properties");
 	
 
 
 	MenuBar(ChatWindow owner) {
 		MenuBar.owner = owner;
 		if(System.getProperty("os.name").toLowerCase().contains("os x")) {
 			actionEvent = ActionEvent.META_MASK;
 		} else {
 			actionEvent = ActionEvent.CTRL_MASK;
 		}
 		
 	//	if(!isOSX) {
 			jirccMenu.add(about);
 			jirccMenu.add(preferences);
 			preferences.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, actionEvent));
 			jirccMenu.add(quit);
 			quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, actionEvent));
 			addActionListeners(jirccMenu);
 			add(jirccMenu);
 		//}
 		/*
 		 * File Menu
 		 */
 		
 		/*
 		 * Edit Menu
 		 */
 		editMenu.add(undo);
 		undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, actionEvent));
 		editMenu.add(redo);
 		redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, actionEvent));
 		editMenu.add(new JSeparator());
 		
 		editMenu.add(cut);
 		cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, actionEvent));
 		editMenu.add(copy);
 		copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, actionEvent));
 		editMenu.add(paste);
 		paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, actionEvent));
 		editMenu.add(delete);
 		editMenu.add(selectAll);
 		selectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, actionEvent));
 		addActionListeners(editMenu);
 		
 		/*
 		 * Server Menu
 		 */
 		serverMenu.add(connect);
 		serverMenu.add(disconnect);
 		disconnect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, actionEvent));
 		serverMenu.add(cancelReconnecting);
 		serverMenu.add(new JSeparator());
 		
 		serverMenu.add(nickname);
 		nickname.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, actionEvent));
 		serverMenu.add(new JSeparator());
 		
 		serverMenu.add(channelList);
 		serverMenu.add(new JSeparator());
 		
 		serverMenu.add(addServer);
 		serverMenu.add(copyServer);
 		serverMenu.add(deleteServer);
 		serverMenu.add(new JSeparator());
 		
 		serverMenu.add(newServer);
 		serverMenu.add(serverProperties);
 		addActionListeners(serverMenu);
 		
 		/*
 		 * Channel Menu
 		 */
 		channelMenu.add(join);
 		channelMenu.add(leave);
 		channelMenu.add(new JSeparator());
 
 		channelMenu.add(mode);
 		mode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, actionEvent));
 		channelMenu.add(topic);
 		topic.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, actionEvent));
 		channelMenu.add(new JSeparator());
 
 		channelMenu.add(addChannel);
 		channelMenu.add(deleteChannel);
 		channelMenu.add(new JSeparator());
 
 		channelMenu.add(channelProperties);
 		channelProperties.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, actionEvent));
 		addActionListeners(channelMenu);
 		
 		
 		/*
 		 * Add the menus to the menu bar.
 		 */
 		add(fileMenu);
 		
 		add(editMenu);
 		add(serverMenu);
 		add(channelMenu);
 	}
 	
 	private void addActionListeners(JMenu menu) {
 		for(Component c : menu.getMenuComponents()) {
 			try {
 				JMenuItem temp = (JMenuItem) c;
 				temp.addActionListener(this);
 			} catch (ClassCastException e) {}
 		}
 	}
 
 	@Override
 	public void menuSelected(MenuEvent e) {
 		System.out.println("menuSelected");
 	}
 
 	@Override
 	public void menuDeselected(MenuEvent e) {
 		System.out.println("menuDeselected");
 	}
 
 	@Override
 	public void menuCanceled(MenuEvent e) {
 		System.out.println("menyCanceled");
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() == newServer) {
 			new NewServerConfigWindow();
 		} else if (e.getSource() == serverProperties) {
 			new ServerPropertiesWindow(owner.getActiveServer());
 		}else if (e.getSource() == undo) {
 
 		} else if (e.getSource() == cut) {
 
 		} else if (e.getSource() == copy) {
 
 		} else if (e.getSource() == paste) {
 
 		} else if (e.getSource() == delete) {
 
 		} else if (e.getSource() == selectAll) {
 		
 		} else if (e.getSource() == quit) {
 			owner.windowClosing(null);
 		} else {
 			System.out.println("Well shit!");
 		}
 	}
 }
