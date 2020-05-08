 package org.publicmain.gui;
 
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Vector;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JTabbedPane;
 import javax.swing.JToggleButton;
 import javax.swing.SwingUtilities;
 import javax.swing.UIDefaults;
 import javax.swing.UIManager;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.publicmain.chatengine.ChatEngine;
 import org.publicmain.chatengine.GruppenKanal;
 import org.publicmain.chatengine.KnotenKanal;
 import org.publicmain.common.LogEngine;
 import org.publicmain.common.Node;
 import org.publicmain.gui.ContactList;
 import org.publicmain.sql.LocalDBConnection;
 
 import com.nilo.plaf.nimrod.NimRODLookAndFeel;
 
 /**
  * @author ATRM
  * 
  */
 
 public class GUI extends JFrame implements Observer , ChangeListener{
 
 	private final int GRP_NAME_LENGTH = 10; 
 	
 	// Deklarationen:
 	private ChatEngine ce;
 	LogEngine log;
 
 	private static GUI me;
 	private List<ChatWindow> chatList;
 	private JMenuBar menuBar;
 	private JMenu fileMenu;
 	private JMenu configMenu;
 	private JMenu lafMenu;
 	private JMenu helpMenu;
 	private JMenu historyMenu;
 	private JMenu backupServer;
 	private JMenuItem pullHistoryFromBackUpServer;
 	private JMenuItem pushHistoryToBackUpServer;
 	private JMenuItem backUpServerSettings;
 	private JMenuItem checkoutHistory;
 	private JMenuItem aboutPMAIN;
 	private JMenuItem helpContents;
 	private JMenuItem menuItemSendFile;
 	private JMenuItem lafNimROD;
 	private ButtonGroup btnGrp;
 	
 	private DragableJTabbedPane jTabbedPane;
 	private JToggleButton contactListBtn;
 	private boolean contactListActive;
 	private ContactList contactListWin;
 	private pMTrayIcon trayIcon;
 	private LocalDBConnection locDBCon;
 
 	/**
 	 * Konstruktor fr GUI
 	 */
 	private GUI() {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception ex) {
 			log.log(ex);
 		}
 
 		// Initialisierungen:
 		try {
 			this.ce=new ChatEngine();
 		} catch (Exception e) {
 			log.log(e);
 		}
 		this.me = this;
 		this.log = new LogEngine();
 		this.locDBCon = LocalDBConnection.getDBConnection(); // bei bedarf einbinden!
 		this.aboutPMAIN 	= new JMenuItem("About pMAIN");
 		this.helpContents	= new JMenuItem("Help Contents", new ImageIcon(getClass().getResource("helpContentsIcon.png")));	// evtl. noch anderes Icon whlen
 		this.menuItemSendFile = new JMenuItem("send File to");
 		this.lafMenu		= new JMenu("Switch Design");
 		this.btnGrp 		= new ButtonGroup();
 		this.chatList 		= new ArrayList<ChatWindow>();
 		this.jTabbedPane 	= new DragableJTabbedPane();
 		this.contactListBtn = new JToggleButton(new ImageIcon(getClass().getResource("UserListAusklappen.png")));
 		this.contactListActive = false;
 		this.menuBar 		= new JMenuBar();
 		this.fileMenu 		= new JMenu("File");
 		this.configMenu 	= new JMenu("Settings");
 		this.helpMenu 		= new JMenu("Help");
 		this.historyMenu 	= new JMenu("History");
 		this.backupServer	= new JMenu("Backup-Server");
 		this.pushHistoryToBackUpServer 		= new JMenuItem("push History to Backup-Server");
 		this.pullHistoryFromBackUpServer	= new JMenuItem("pull History from Backup-Server");
 		this.backUpServerSettings 			= new JMenuItem("Backup-Server Settings");
 		this.checkoutHistory				= new JMenuItem("checkout History");
 		this.lafNimROD 		= new JRadioButtonMenuItem("NimROD");
 		this.trayIcon 		= new pMTrayIcon();
 		
 		// Anlegen der Meneintrge fr Designwechsel (installierte
 		// LookAndFeels)
 		// + hinzufgen zum lafMenu ("Designwechsel")
 		// + hinzufgen der ActionListener (lafController)
 		for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
 			JRadioButtonMenuItem tempJMenuItem = new JRadioButtonMenuItem(laf.getName());
 			if((laf.getName().equals("Windows")) &&
 					(UIManager.getSystemLookAndFeelClassName().equals("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"))){
 				tempJMenuItem.setSelected(true);
 			}
 			lafMenu.add(tempJMenuItem);
 			btnGrp.add(tempJMenuItem);
 			tempJMenuItem.addActionListener(new lafController(lafMenu, laf));
 		}
 
 		// Anlegen bentigter Controller und Listener:
 		// WindowListener fr das GUI-Fenster:
 		this.addWindowListener(new winController());
 		
 		// ChangeListener fr Focus auf Eingabefeld
 		this.jTabbedPane.addChangeListener(this);
 
 		// ActionListener fr Menu's:
 		this.menuItemSendFile.addActionListener(new menuContoller());
 		this.aboutPMAIN.addActionListener(new menuContoller());
 		this.helpContents.addActionListener(new menuContoller());
 		this.lafNimROD.addActionListener(new lafController(lafNimROD, null));
 		this.checkoutHistory.addActionListener(new menuContoller());
 		this.backUpServerSettings.addActionListener(new menuContoller());
 		
 		// Konfiguration contactListBtn:
 		this.contactListBtn.setMargin(new Insets(2, 3, 2, 3));
 		this.contactListBtn.setToolTipText("show contacts");
 		this.contactListBtn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JToggleButton source = (JToggleButton) e.getSource();
 				if (source.isSelected()) {
 					contactListAufklappen();
 				} else {
 					contactListZuklappen();
 				}
 			}
 		});
 		
 		//lafNimROD zur ButtonGroup btnGrp hinzufgen:
 		this.btnGrp.add(lafNimROD);
 		
 		// Mens hinzufgen:
 		this.lafMenu.add(lafNimROD);
 		
 		this.configMenu.add(lafMenu);
 		
 		this.fileMenu.add(menuItemSendFile);
 		
 		this.helpMenu.add(aboutPMAIN);
 		this.helpMenu.add(helpContents);
 		
 		this.historyMenu.add(checkoutHistory);
 		this.historyMenu.add(backupServer);
 		
 		this.backupServer.add(pushHistoryToBackUpServer);
 		this.backupServer.add(pullHistoryFromBackUpServer);
 		this.backupServer.add(backUpServerSettings);
 		
 		this.menuBar.add(contactListBtn);
 		this.menuBar.add(fileMenu);
 		this.menuBar.add(configMenu);
 		this.menuBar.add(helpMenu);
 		this.menuBar.add(historyMenu);
 
 		// GUI Komponenten hinzufgen:
 		this.setJMenuBar(menuBar);
 		this.add(jTabbedPane);
 		
 		// StandardGruppe erstellen:
 		this.addGrpCW("public");
 		
 		
 
 		// GUI JFrame Einstellungen:
 		this.setIconImage(new ImageIcon(getClass().getResource("pM_Logo2.png")).getImage());
 		this.setMinimumSize(new Dimension(250,250));
 		this.pack();
 		this.setLocationRelativeTo(null);
 		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		this.setTitle("publicMAIN");
 		this.contactListWin = new ContactList(GUI.me);
 		this.setVisible(true);
 		chatList.get(0).focusEingabefeld(); // das tut's net
 	}
 	
 	/**
 	 * Diese Methode klappt die Contactlist auf
 	 * 
 	 * 
 	 */
 	private void contactListAufklappen(){
 
 		if(!contactListActive){
 			
 			this.contactListBtn.setToolTipText("hide contacts");
 			this.contactListBtn.setIcon(new ImageIcon(getClass().getResource("UserListEinklappen.png")));
 			this.contactListBtn.setSelected(true);
 			this.contactListWin.repaint();
 			this.contactListWin.setVisible(true);
 			contactListActive = true;
 		}
 
 	}
 	
 	/**
 	 * Diese Methode klappt die Contactlist zu
 	 * 
 	 * 
 	 */
 	private void contactListZuklappen(){
 
 		if(contactListActive){
 			
 			this.contactListBtn.setToolTipText("show contacts");
 			this.contactListBtn.setIcon(new ImageIcon(getClass().getResource("UserListAusklappen.png")));
 			this.contactListBtn.setSelected(false);
 			
 			this.contactListWin.setVisible(false);
 			
 			this.contactListActive = false;
 		}
 	}
 	
 	/**
 	 * Diese Methode fgt ein ChatWindow hinzu
 	 * 
 	 * Diese Methode fgt ein ChatWindow zum GUI hinzu und setzt dessen
 	 * Komponenten
 	 * 
 	 * @param cw
 	 */
 	public void createChat(ChatWindow cw) {
 		// Title festlegen:
 		String title = cw.getChatWindowName();
 
 		// neues ChatWindow (cw) zur Chatliste (ArrayList<ChatWindow>)
 		// hinzufgen:
 		this.chatList.add(cw);
 		// erzeugen von neuem Tab fr neues ChatWindow:
 		this.jTabbedPane.addTab(title, cw);
 
 		// Index vom ChatWindow im JTabbedPane holen um am richtigen Ort
 		// einzufgen:
 		int index = jTabbedPane.indexOfComponent(cw);
 		// den neuen Tab an die Stelle von index setzen:
 		this.jTabbedPane.setTabComponentAt(index, cw.getWindowTab());
 	}
 
 	/**
 	 * Diese Methode erstellt ein ChatWindow fr Gruppen, falls ChatWindow bereits vorhanden, wird dieses fokusiert.
 	 * @param grpName
 	 */
 	public void addGrpCW(String grpName){
 		String grp_name = grpName;
		grp_name = grp_name.substring(0, GRP_NAME_LENGTH);
 		grp_name = grp_name.trim();
 		grp_name = grp_name.replaceAll("[*?\\/@<>\\t\\n\\x0B\\f\\r]*", "");
 		grp_name = grp_name.toLowerCase();
 		if(existCW(grp_name) == null){
 			createChat(new ChatWindow(grp_name));
 			// ChatWindow am Gruppen NachrichtenListener (MSGListener) anmelden und Gruppe joinen:
 			ce.group_join(grp_name);
 			ce.add_MSGListener(existCW(grp_name), grp_name);
 		} else {
 			existCW(grp_name).focusEingabefeld();
 			System.out.println(existCW(grp_name).toString());
 		}
 	}
 	
 	/**
 	 * Diese Methode erstellt ein ChatWindow fr Privatechat's, falls ChatWindow bereits vorhanden, wird dieses fokusiert.
 	 * @param aliasName
 	 */
 	public void addPrivCW(String aliasName){
 		if(existCW(aliasName) == null){
 			long tmpUID = ce.getNodeforAlias(aliasName).getUserID();
 			createChat(new ChatWindow(tmpUID, aliasName));
 			// ChatWindow am privaten NachrichtenListener (MSGListener) anmelden:
 			ce.add_MSGListener(existCW(aliasName), tmpUID);
 		} else {
 			// focus auf CW setzen
 		}
 	}
 	
 	/**
 	 * Diese Methode entfernt ein ChatWindow
 	 * 
 	 * Diese Methode sorgt dafr das ChatWindows aus der ArrayList "chatList"
 	 * entfernt werden und im GUI nicht mehr angezeigt werden.
 	 * 
 	 * @param ChatWindow
 	 */
 	public void delChat(ChatWindow cw) {
 		// TODO: Hier evtl. noch anderen Programmablauf implementier
 		// z.B. schlieen des Programms wenn letztes ChatWindow geschlossen
 		// wird
 		
 		// Falls nur noch ein ChatWindow brig kann dieses nicht entfernt werden
 		if(chatList.size() >= 2){
 			// ChatWindow (cw) aus jTabbedPane entfernen:
 			this.jTabbedPane.remove(cw);
 			// ChatWindow aus Chatliste entfernen:
 			this.chatList.remove(cw);
 			// ChatWindow aus Gruppe entfernen (MSGListener abschalten):
 			ce.remove_MSGListener(cw);
 		}
 	}
 	
 	/**
 	 * Diese Methode entfernt ein ChatWindow anhand des Namens
 	 * 
 	 * Diese Methode sorgt dafr das ChatWindows aus der ArrayList "chatList"
 	 * entfernt werden und im GUI nicht mehr angezeigt werden.
 	 * 
 	 * @param chatname
 	 */
 	public void delChat(String chatname){
 		delChat(existCW(chatname));
 	}
 	
 	/**
 	 * Diese Methode nimmt die nderungsanforderung entgegen und prft ob der Name bereits an einen anderen
 	 * Benutzer oder an eine Gruppe vergeben wurde. Existiert der Alias wird false zurckgeliefert in jedem
 	 * anderen Fall wird der Name ber die ChatEngine gendert.
 	 *   
 	 * @param alias
 	 * @return boolean
 	 */
 	public boolean changeAlias(String alias){
 		if(contactListWin.nameExists(alias)){
 			System.out.println("GUI: Name existiert bereits! Wird nicht gendert!");
 			return false;
 		} else {
 			ce.setAlias(alias);
 			return true;
 		}
 	}
 	
 	/**
 	 * Fhrt das Programm ordnungsgem runter
 	 */
 	void shutdown(){
 		//TODO: ordentlicher shutdown
 		ce.shutdown();
 		System.exit(0);
 	}
 
 	/**
 	 * Diese Methode prft ob ein ChatWindow bereits vorhanden ist
 	 * 
 	 * Diese Methode prft ob ein ChatWindow vorhanden ist falls ja wird dieses returned
 	 * falls nein wird null returned
 	 * @param chatWindowname Name Chatwindow
 	 * @return ChatWindow or null 
 	 */
 	private ChatWindow existCW(String chatWindowname){
 		for(ChatWindow x : chatList){
 			if(x.getChatWindowName().equals(chatWindowname)){
 				return x;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Diese Methode wird in einem privaten ChatWindow zum versenden der Nachricht verwendet
 	 * @param empfUID long EmpfngerUID
 	 * @param msg String die Nachricht
 	 */
 	void privSend(long empfUID,String msg){
 		ce.send_private(empfUID, msg);
 	}
 	
 	/**
 	 * Diese Methode sendet eine private Nachricht durch /w
 	 * 
 	 * Diese Methode wird vom ChatWindow durch die Eingabe von /w aufgerufen
 	 * zunchst wird geprft ob schon ein ChatWindow fr den privaten Chat existiert
 	 * falls nicht wird eines angelegt und die private nachricht an die UID versendet
 	 * @param empfAlias String Empfnger Alias
 	 * @param msg String die Nachricht
 	 */
 	void privSend(String empfAlias, String msg){
 		ChatWindow tmpCW = existCW(empfAlias);
 		int tabNr = 0;
 		long tmpUID;
 		try{
 			for(Node x : ce.getUsers()){
 				if(x.getAlias().equals(empfAlias)){
 					tmpUID = x.getNodeID();
 					if(tmpCW == null){
 						createChat(new ChatWindow(tmpUID, empfAlias));
 						tabNr = jTabbedPane.indexOfComponent(existCW(empfAlias));
 						jTabbedPane.setSelectedIndex(tabNr);
 					} else {
 						tabNr = jTabbedPane.indexOfComponent(tmpCW);
 						jTabbedPane.setSelectedIndex(tabNr);
 					}
 					ce.send_private(tmpUID, msg);
 				}
 			}
 		} catch (NullPointerException npex){
 			LogEngine.log("User nicht gefunden!", LogEngine.ERROR);
 		}
 	}
 	
 	/**
 	 * Diese Methode wird fr das Senden von Gruppennachrichten verwendet
 	 * Falls noch kein ChatWindow fr diese Gruppe besteht wird eines erzeugt.
 	 * @param empfGrp String Empfngergruppe
 	 * @param msg String die Nachricht/Msg
 	 * @param cw ChatWindow das aufrufende ChatWindow
 	 */
 	void groupSend(String empfGrp, String msg){
 		ChatWindow tmpCW = existCW(empfGrp);
 		int tabNr = 0;
 		if(tmpCW == null){
 			createChat(new ChatWindow(empfGrp));
 			tabNr = jTabbedPane.indexOfComponent(existCW(empfGrp));
 			jTabbedPane.setSelectedIndex(tabNr);
 		} else {
 			tabNr = jTabbedPane.indexOfComponent(tmpCW);
 			jTabbedPane.setSelectedIndex(tabNr);
 		}
 		ce.send_group(empfGrp, msg);
 	}
 	
 	/**
 	 * Diese Methode ist fr das Ignorien eines users
 	 * @param alias String Alias des Users
 	 * @returns true Wenn User gefunden
 	 */
 	boolean ignoreUser(String alias){
 		long tmpUID = -1;
 		tmpUID = ce.getNodeforAlias(alias).getUserID();
 		if(tmpUID != -1){
 			ce.ignore_user(tmpUID);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Diese Methode ist fr das nicht weitere Ignorieren eines users
 	 * @param alias String Alias des Users
 	 * @return true Wenn User gefunden
 	 */
 	boolean unignoreUser(String alias){
 		long tmpUID = -1;
 		tmpUID = ce.getNodeforAlias(alias).getUserID();
 		if(tmpUID != -1){
 			ce.unignore_user(tmpUID);
 			return true;
 		}
 		return false;
 	}
 	
 	public void sendFile(String aliasName) {
 		// TODO: hier stimmen noch paar sachen nicht spter berarbeiten!
 		JFileChooser fileChooser = new JFileChooser();
 		int returnVal = fileChooser.showOpenDialog(me);
 		if(returnVal == JFileChooser.APPROVE_OPTION){
 			System.out.println("You chose to send this file: " + fileChooser.getSelectedFile().getName());
 		}
 		File datei = new File(fileChooser.getSelectedFile().getAbsolutePath());
 		ce.send_file(ce.getNodeforAlias(aliasName).getUserID(), datei);
 	}
 	
 	/**
 	 * Diese Methode liefert ein Fileobjekt
 	 * 
 	 * Diese Methode bittet die GUI(den Nutzer) um ein Fileobjekt zur Ablage der
 	 * empfangenen Datei
 	 * 
 	 * @return File
 	 */
 	public File request_File() {
 		// TODO: hier stimmt noch nix! spter berarbeiten!
 		JFileChooser fileChooser = new JFileChooser();
 		int returnVal = fileChooser.showSaveDialog(me);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			System.out.println("You chose to save this file: " + fileChooser.getSelectedFile().getName());
 		}
 		return fileChooser.getSelectedFile();
 	}
 
 	/**
 	 * Diese Methode soll ber nderungen informieren
 	 */
 	public void notifyGUI() {
 		// TODO:
 		// da muss noch was gemacht werden !!!
 		// evtl fliegt die Methode auch raus wenn wir das
 		// mit den Observerpattern machen...
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
 	 */
 	@Override
 	public void update(Observable o, Object arg) {
 		if (o instanceof GruppenKanal){
 			if (o.countObservers()==1){
 			//erzeuge gruppenfenster fge nachricht ein sei happy
 			}
 			else{
 				//
 			}
 			
 		}
 		if(o instanceof KnotenKanal&&o.countObservers()==1){
 			//erzeuge gruppen
 		}
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * Diese Methode stellt das Node bereit
 	 * 
 	 * Diese Methode ist ein Getter fr das Node
 	 * 
 	 * @param sender
 	 * @return Node
 	 */
 	//TODO: evtl. lschen ???
 	public Node getNode(long sender) {
 		return ce.getNode(sender);
 	}
 	
 	/**
 	 * Diese Methode stellt das GUI bereit
 	 * 
 	 * Diese Methode stellt das GUI fr andere Klassen bereit um einen Zugriff
 	 * auf GUI Attribute zu ermglichen
 	 * 
 	 * @return GUI
 	 */
 	public static GUI getGUI() {
 		if (me == null) {
 			me = new GUI();
 		}
 		return me;
 	}
 	
 	/**
 	 * @return
 	 */
 	JTabbedPane getTabbedPane(){
 		return this.jTabbedPane;
 	}
 
 	/**
 	 * ActionListener fr Design wechsel (LookAndFeel)
 	 * 
 	 * hier wird das Umschalten des LookAndFeels im laufenden Betrieb ermglicht
 	 * 
 	 * @author ABerthold
 	 * 
 	 */
 	class lafController implements ActionListener {
 
 		private JMenuItem lafMenu;
 		private UIManager.LookAndFeelInfo laf;
 		private boolean userListWasActive;
 
 		public lafController(JMenuItem lafMenu, UIManager.LookAndFeelInfo laf) {
 			this.lafMenu = lafMenu;
 			this.laf = laf;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			
 			userListWasActive = contactListActive;
 			JMenuItem source = (JMenuItem)e.getSource();
 			
 			contactListZuklappen();
 			
 			if(source.getText().equals("NimROD")){
 				try{
 					UIManager.setLookAndFeel(new NimRODLookAndFeel());
 				} catch (Exception ex){
 					LogEngine.log(ex);
 				}
 			} else {
 				try {
 					UIManager.setLookAndFeel(laf.getClassName());
 				} catch (Exception ex) {
 					LogEngine.log(ex);
 				}
 			}
 			SwingUtilities.updateComponentTreeUI(GUI.me);
 			GUI.me.pack();
 			if(userListWasActive)contactListAufklappen();
 			
 		}
 	}
 	
 	/**
 	 * ActionListener fr Menu's
 	 * 
 	 * @author ABerthold
 	 *
 	 */
 	class menuContoller implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			
 			JMenuItem source = (JMenuItem)e.getSource();
 			
 			switch(source.getText()){
 			
 			case "send File to":
 				//TODO: nochmal berprfen!!!
 				String aliasName = null;
 				aliasName = (String)JOptionPane.showInputDialog(me, "Enter Name", "Reciver Name", JOptionPane.OK_CANCEL_OPTION, new ImageIcon(getClass().getResource("private.png")), null, null);
 				if(aliasName != null && ce.getNodeforAlias(aliasName) != null && !aliasName.equals("")){
 					sendFile(aliasName);
 				} else if(aliasName == null){
 					
 				} else {
 					JOptionPane.showMessageDialog(GUI.getGUI(), "User not found!", "unknown Username", JOptionPane.ERROR_MESSAGE);
 				}
 				break;
 			case "About pMAIN":
 				new AboutPublicMAIN(me, "About publicMAIN", true);
 				break;
 			case "Help Contents":
 				//TODO: HelpContents HTML schreiben
 				new HelpContents();
 				break;
 			case "checkout History":
 				new checkoutHistoryWindow();
 				break;
 			case "Backup-Server Settings":
 				new BackUpServerSettingsWindow();
 				break;
 			}
 			
 		}
 		
 	}
 	
 	/**
 	 * WindowListener fr GUI
 	 * 
 	 * 
 	 * 
 	 * @author ABerthold
 	 *
 	 */
 	class winController implements WindowListener{
 		public void windowOpened(WindowEvent arg0) {
 			// TODO Auto-generated method stub
 		}
 		@Override
 		// Wird das GUI minimiert wird die Userlist zugeklappt und der
 		// userListBtn zurckgesetzt:
 		public void windowIconified(WindowEvent arg0) {
 			if (contactListBtn.isSelected()) {
 				contactListZuklappen();
 			}
 		}
 		@Override
 		public void windowDeiconified(WindowEvent arg0) {
 			// TODO Auto-generated method stub
 		}
 		@Override
 		public void windowDeactivated(WindowEvent arg0) {
 			// TODO Auto-generated method stub
 		}
 		@Override
 		public void windowClosing(WindowEvent arg0) {
 			// TODO Auto-generated method stub
 		}
 		@Override
 		public void windowClosed(WindowEvent arg0) {
 			shutdown();
 			// Object[] eventCache =
 			// {"super, so ne scheisse","deine Mama liegt im Systemtray"};
 			// Object anchor = true;
 			// JOptionPane.showInputDialog(me,
 			// "pMAIN wird ins Systemtray gelegt!",
 			// "pMAIN -> Systemtray", JOptionPane.PLAIN_MESSAGE, new
 			// ImageIcon("media/pM16x16.png"), eventCache, anchor);
 		}
 		@Override
 		public void windowActivated(WindowEvent arg0) {
 			if (contactListBtn.isSelected()) {
 				contactListWin.toFront();
 			}
 		}
 	}
 	
 	/**
 	 * Diese Methode gibt die Default Settings des aktuellen L&F in der Console aus
 	 */
 	private void getLookAndFeelDefaultsToConsole(){
 		UIDefaults def = UIManager.getLookAndFeelDefaults();
 		Vector<?> vec = new Vector<Object>(def.keySet());
 		Collections.sort(vec, new Comparator<Object>() {
 			public int compare(Object arg0, Object arg1) {
 				return arg0.toString().compareTo(arg1.toString());
 			}
 		});
 		for (Object obj : vec) {
 			System.out.println(obj + "\n\t" + def.get(obj));
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
 	 */
 	@Override
 	public void stateChanged(ChangeEvent e) {
 		((ChatWindow)jTabbedPane.getSelectedComponent()).focusEingabefeld();
 	}
 }
