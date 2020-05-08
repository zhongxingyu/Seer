 package org.publicmain.gui;
 
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 import javax.swing.JToggleButton;
 
 import org.publicmain.common.Config;
 import org.publicmain.sql.DatabaseEngine;
 import org.resources.Help;
 
 public class SettingsWindow extends JDialog{
 	
 	private static SettingsWindow me;
 				
 	private JLabel		banner;
 	
 	private JPanel		cardButtonsPanel;
 	private JToggleButton userBtn;
 	private JToggleButton databaseBtn;
 	private JToggleButton pushPullBtn;
 	private ButtonGroup btnGrp;
 	private JPanel		cardsPanel;
 	private JPanel		cardUser;
 	private JPanel		cardDB;
 	private JPanel		cardPushPull;
 	
 	private JPanel 		userSettingsPanel;
 	private JLabel 		aliasLabel;
 	private JTextField 	aliasTextField;
 	private JLabel 		fileTransferLabel;
 	private JCheckBox 	fileTransferCheckBox;
 	
 	private JPanel 		trayIconNotificationPanel;
 	private JLabel		grpMsgLabel;
 	private JCheckBox	grpMsgCheckBox;
 	private JLabel		privMsgLabel;
 	private JCheckBox	privMsgCheckBox;
 	
 	private JPanel		localDBPanel;
 	private JLabel		portLocalDBLabel;
 	private JTextField	portLocalDBTextField;
 	private JLabel		userLocalDBLabel;
 	private JTextField	userLocalDBTextField;
 	private	JLabel		pwLocalDBLabel;
 	private JPasswordField pwLocalDBPasswordField;
 	
 	private JPanel		pushPullPanel;
 	private JLabel		userPushPullLabel;
 	private JTextField	userPushPullTextField;
 	private JLabel		pwPushPullLabel;
 	private JPasswordField pwPushPullPasswordField;
 	private JLabel		createPushPullLabel;
 	private JButton		createPushPullBtn;
 	
 	private JPanel		backupDBPanel;
 	private JLabel		ipBackupLabel;
 	private	JTextField	ipBackupTextField;
 	private JLabel		portBackupLabel;
 	private JTextField	portBackupTextField;
 	private JLabel		userBackupLabel;
 	private JTextField	userBackupTextField;
 	private JLabel		pwBackupLabel;
 	private JPasswordField pwBackPasswordField;
 	
 	private JPanel		buttonPanel;
 	private JButton		resetBtn;
 	private JButton		acceptBtn;
 	private JButton		cancelBtn;
 
 	public SettingsWindow() {
 		this.me = this;
 		this.setResizable(false);
 		this.setLayout(new FlowLayout(FlowLayout.CENTER));
 		
 		this.banner					 = new JLabel(Help.getIcon("textlogo.png",210,50));
 		
 		this.cardButtonsPanel		 = new JPanel(new GridLayout(1,3));
 		this.userBtn				 = new JToggleButton("User", Help.getIcon("userSettingsSym.png",10,16), true);
 		this.databaseBtn			 = new JToggleButton("DB", Help.getIcon("dbSettingsSym.png",12,16), false);
 		this.pushPullBtn			 = new JToggleButton("Push/Pull", false);
 		this.btnGrp					 = new ButtonGroup();
 		this.cardsPanel				 = new JPanel(new CardLayout());
 		this.cardUser				 = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		this.cardDB					 = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		this.cardPushPull			 = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		
 		this.userSettingsPanel		 = new JPanel(new GridLayout(2,2));
 		this.aliasLabel				 = new JLabel("Alias");
 		this.aliasTextField			 = new JTextField();
 		this.fileTransferLabel		 = new JLabel("Deny file transfer");
 		this.fileTransferCheckBox	 = new JCheckBox();
 		
 		this.trayIconNotificationPanel = new JPanel(new GridLayout(2,2));
 		this.grpMsgLabel 			 = new JLabel("Group messages");
 		this.grpMsgCheckBox			 = new JCheckBox();
 		this.privMsgLabel			 = new JLabel("Private messages");
 		this.privMsgCheckBox		 = new JCheckBox();
 		
 		this.localDBPanel			 = new JPanel(new GridLayout(3,2));
 		this.portLocalDBLabel		 = new JLabel("Port");
 		this.portLocalDBTextField	 = new JTextField();
 		this.userLocalDBLabel		 = new JLabel("Username");
 		this.userLocalDBTextField	 = new JTextField();
 		this.pwLocalDBLabel			 = new JLabel("Password");
 		this.pwLocalDBPasswordField	 = new JPasswordField();
 		
 		this.pushPullPanel 			 = new JPanel(new GridLayout(3,2));
 		this.userPushPullLabel 		 = new JLabel("Username");
 		this.userPushPullTextField	 = new JTextField();
 		this.pwPushPullLabel 		 = new JLabel("Password");
 		this.pwPushPullPasswordField = new JPasswordField();
 		this.createPushPullLabel	 = new JLabel("Generate account");
 		this.createPushPullBtn		 = new JButton("Create");
 		
 		this.backupDBPanel 			 = new JPanel(new GridLayout(4,2));
 		this.ipBackupLabel 			 = new JLabel("IP address");
 		this.ipBackupTextField 		 = new JTextField();
 		this.portBackupLabel 		 = new JLabel("Port");
 		this.portBackupTextField 	 = new JTextField();
 		this.userBackupLabel 		 = new JLabel("Username");
 		this.userBackupTextField 	 = new JTextField();
 		this.pwBackupLabel 			 = new JLabel("Password");
 		this.pwBackPasswordField 	 = new JPasswordField();
 		
 		this.buttonPanel 	= new JPanel(new GridLayout(1,3));
 		this.resetBtn 		= new JButton("Reset");
 		this.acceptBtn	 	= new JButton("Accept");
 		this.cancelBtn 		= new JButton("Cancel");
 		
 		
 		this.btnGrp.add(userBtn);
 		this.btnGrp.add(databaseBtn);
 		this.btnGrp.add(pushPullBtn);
 		
 		this.userBtn.addActionListener(new CardButtonController(cardsPanel));
 		this.databaseBtn.addActionListener(new CardButtonController(cardsPanel));
 		this.pushPullBtn.addActionListener(new CardButtonController(cardsPanel));
 		this.resetBtn.addActionListener(new SettingButtonController());
 		this.acceptBtn.addActionListener(new SettingButtonController());
 		this.cancelBtn.addActionListener(new SettingButtonController());
 		this.createPushPullBtn.addActionListener(new PushPullButtonController());
 		
 		this.cardButtonsPanel.setPreferredSize(new Dimension(230,25));
 		this.cardButtonsPanel.setBackground(Color.WHITE);
 		this.cardButtonsPanel.add(userBtn);
 		this.cardButtonsPanel.add(databaseBtn);
 		this.cardButtonsPanel.add(pushPullBtn);
 		
 		this.cardUser.setPreferredSize(new Dimension(230,62));
 		this.cardUser.setBackground(Color.WHITE);
 		this.cardUser.add(userSettingsPanel);
 		this.cardUser.add(trayIconNotificationPanel);
 		
 		this.cardDB.setPreferredSize(new Dimension(230,62));
 		this.cardDB.setBackground(Color.WHITE);
 		this.cardDB.add(localDBPanel);
 		this.cardDB.add(backupDBPanel);
 		
 		this.cardPushPull.setPreferredSize(new Dimension(230,62));
 		this.cardPushPull.setBackground(Color.WHITE);
 		this.cardPushPull.add(pushPullPanel);
 		
 		this.userSettingsPanel.setBorder(BorderFactory.createTitledBorder("User"));
 		this.userSettingsPanel.setPreferredSize(new Dimension(230,62));
 		this.userSettingsPanel.setBackground(Color.WHITE);
 		this.fileTransferCheckBox.setBackground(Color.WHITE);
 		this.userSettingsPanel.add(aliasLabel);
 		this.userSettingsPanel.add(aliasTextField);
 		this.userSettingsPanel.add(fileTransferLabel);
 		this.userSettingsPanel.add(fileTransferCheckBox);
 		
 		this.trayIconNotificationPanel.setBorder(BorderFactory.createTitledBorder("Notification"));
 		this.trayIconNotificationPanel.setPreferredSize(new Dimension(230,62));
 		this.trayIconNotificationPanel.setBackground(Color.WHITE);
 		this.grpMsgCheckBox.setBackground(Color.WHITE);
 		this.privMsgCheckBox.setBackground(Color.WHITE);
 		this.trayIconNotificationPanel.add(grpMsgLabel);
 		this.trayIconNotificationPanel.add(grpMsgCheckBox);
 		this.trayIconNotificationPanel.add(privMsgLabel);
 		this.trayIconNotificationPanel.add(privMsgCheckBox);
 		
 		this.localDBPanel.setBorder(BorderFactory.createTitledBorder("local database"));
 		this.localDBPanel.setPreferredSize(new Dimension(230,82));
 		this.localDBPanel.setBackground(Color.WHITE);
 		this.localDBPanel.add(portLocalDBLabel);
 		this.localDBPanel.add(portLocalDBTextField);
 		this.localDBPanel.add(userLocalDBLabel);
 		this.localDBPanel.add(userLocalDBTextField);
 		this.localDBPanel.add(pwLocalDBLabel);
 		this.localDBPanel.add(pwLocalDBPasswordField);
 		
 		this.pushPullPanel.setBorder(BorderFactory.createTitledBorder("push/pull to backup DB"));
 		this.pushPullPanel.setPreferredSize(new Dimension(230,80));
 		this.pushPullPanel.setBackground(Color.WHITE);
 		this.pushPullPanel.add(userPushPullLabel);
 		this.pushPullPanel.add(userPushPullTextField);
 		this.pushPullPanel.add(pwPushPullLabel);
 		this.pushPullPanel.add(pwPushPullPasswordField);
 		this.pushPullPanel.add(createPushPullLabel);
 		this.pushPullPanel.add(createPushPullBtn);
 		
 		this.backupDBPanel.setBorder(BorderFactory.createTitledBorder("backup database"));
 		this.backupDBPanel.setPreferredSize(new Dimension(230,100));
 		this.backupDBPanel.setBackground(Color.WHITE);
 		this.backupDBPanel.add(ipBackupLabel);
 		this.backupDBPanel.add(ipBackupTextField);
 		this.backupDBPanel.add(portBackupLabel);
 		this.backupDBPanel.add(portBackupTextField);
 		this.backupDBPanel.add(userBackupLabel);
 		this.backupDBPanel.add(userBackupTextField);
 		this.backupDBPanel.add(pwBackupLabel);
 		this.backupDBPanel.add(pwBackPasswordField);
 		
 		this.cardsPanel.setPreferredSize(new Dimension(230, 270));
 		this.cardsPanel.setBackground(Color.WHITE);
 		this.cardsPanel.add(cardUser, "User");
 		this.cardsPanel.add(cardDB, "DB");
 		this.cardsPanel.add(cardPushPull, "Push/Pull");
 		
 		this.buttonPanel.setPreferredSize(new Dimension(230,25));
 		this.buttonPanel.setBackground(Color.WHITE);
 		this.buttonPanel.add(resetBtn);
 		this.buttonPanel.add(acceptBtn);
 		this.buttonPanel.add(cancelBtn);
 		
 		this.add(banner);
 		this.add(cardButtonsPanel);
 		this.add(cardsPanel);
 		this.add(buttonPanel);
 
 		
 		this.setTitle("Settings");
 		this.setModal(false);
 		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
 		this.setIconImage(Help.getIcon("pM_Logo.png").getImage());
 		this.getContentPane().setBackground(Color.WHITE);
 		this.setMinimumSize(new Dimension(250, GUI.getGUI().getHeight()));
 		this.setMaximumSize(new Dimension(250, GUI.getGUI().getHeight()));
 		this.setPreferredSize(new Dimension(250, GUI.getGUI().getHeight()));
 		this.pack();
 		
 		this.getDefaults();
 		this.setLocationRelativeTo(null);
 		this.setVisible(true);
 	}
 	
 	
 	public static void closeThis(){
 		if(me!=null)me.dispose();
 	}
 
 	public static void showThis(){
 		if(me==null) new SettingsWindow();
 		me.setVisible(true);
 	}
 	
 	private void getDefaults(){
 		this.aliasTextField.setText(Config.getConfig().getAlias());
 		this.fileTransferCheckBox.setSelected(Config.getConfig().getDisableFileTransfer());
 		
 		this.grpMsgCheckBox.setSelected(Config.getConfig().getNotifyGroup());
 		this.privMsgCheckBox.setSelected(Config.getConfig().getNotifyPrivate());
 		
 		this.userPushPullTextField.setText(Config.getConfig().getBackupDBChoosenUsername());
 		this.pwPushPullPasswordField.setText(Config.getConfig().getBackupDBChoosenUserPassWord());
 		
 		this.portLocalDBTextField.setText(Config.getConfig().getLocalDBPort());
 		this.userLocalDBTextField.setText(Config.getConfig().getLocalDBUser());
 		this.pwLocalDBPasswordField.setText(Config.getConfig().getLocalDBPw());
 		
 		this.ipBackupTextField.setText(Config.getConfig().getBackupDBIP());
 		this.portBackupTextField.setText(Config.getConfig().getBackupDBPort());
 		this.userBackupTextField.setText(Config.getConfig().getBackupDBUser());
 		this.pwBackPasswordField.setText(Config.getConfig().getBackupDBPw());
 	}
 	
 	private void acceptSettings(){
 		boolean changes = false;
 		
 		if(!aliasTextField.getText().equals(Config.getConfig().getAlias())){
 			GUI.getGUI().changeAlias(aliasTextField.getText());
 			//hier wird bewusst changes nicht auf true gesetz, da die Methode changeAlias()
 			//das schreiben der config bernimmt.
 		}
 		if(Config.getConfig().getDisableFileTransfer() != fileTransferCheckBox.isSelected()){
 			Config.getConfig().setDisableFileTransfer(fileTransferCheckBox.isSelected());
 			changes = true;
 		}
 		
 		if(Config.getConfig().getNotifyGroup() != grpMsgCheckBox.isSelected()){
 			Config.getConfig().setNotifyGroup(grpMsgCheckBox.isSelected());
 			changes = true;
 		}
 		if(Config.getConfig().getNotifyPrivate() != privMsgCheckBox.isSelected()){
 			Config.getConfig().setNotifyPrivate(privMsgCheckBox.isSelected());
 			changes = true;
 		}
 		
 		if (!userPushPullTextField.getText().equals(""))
 		{
 			String tmpUserPushPull = userPushPullTextField.getText();
 			if (!tmpUserPushPull.equals(Config.getConfig().getBackupDBChoosenUsername())) {
 				Config.getConfig().setBackupDBChoosenUsername(userPushPullTextField.getText());
 				changes = true;
 			}
 		}
 		if(pwPushPullPasswordField.getPassword().length>1)
 		{
 			String tmp_password = new String(pwPushPullPasswordField.getPassword());
 			if(!tmp_password.equals(Config.getConfig().getBackupDBChoosenUserPassWord())) {
 				Config.getConfig().setBackupDBChoosenUserPassWord(tmp_password);
 				changes = true;
 			}
 		}
 		
 		if (portLocalDBTextField.getText().length()>1)
 		{
 			String tmp_port = new String(portLocalDBTextField.getText());
 			if (!tmp_port.equals(Config.getConfig().getLocalDBPort())) {
 				Config.getConfig().setLocalDBPort(portLocalDBTextField.getText());
 				changes = true;
 			}
 		}
 		if (userLocalDBTextField.getText().length()>1)
 		{
 			String tmp_user = new String(userLocalDBTextField.getText());
 			if (!tmp_user.equals(Config.getConfig().getLocalDBUser())) {
 				Config.getConfig().setLocalDBUser(userLocalDBTextField.getText());
 				changes = true;
 			}
 		}
 		if(pwLocalDBPasswordField.getPassword().length>1)
 		{
 			String tmp_password = new String(pwLocalDBPasswordField.getPassword());
 			if(!tmp_password.equals(Config.getConfig().getLocalDBPw())) {
 				Config.getConfig().setLocalDBPw(tmp_password);
 				changes = true;
 			}
 		}
 		
 		if (!ipBackupTextField.getText().equals(""))
 		{
 			String tmpBackupIp = ipBackupTextField.getText();
 			if (!tmpBackupIp.equals(Config.getConfig().getBackupDBIP())) {
 				Config.getConfig().setBackupDBIP(ipBackupTextField.getText());
 				changes = true;
 			}
 		}
 		if (portBackupTextField.getText().length()>1)
 		{
 			String tmp_port = new String(portBackupTextField.getText());
 			if (!tmp_port.equals(Config.getConfig().getBackupDBPort())){
 				Config.getConfig().setBackupDBPort(
 						portBackupTextField.getText());
 				changes = true;
 			}
 		}
 		if (userBackupTextField.getText().length()>1)
 		{
 			String tmp_user = new String(userBackupTextField.getText());
 			if (!tmp_user.equals(Config.getConfig().getBackupDBUser())) {
 				Config.getConfig().setBackupDBUser(userBackupTextField.getText());
 				changes = true;
 			}
 		}
 		if(pwBackPasswordField.getPassword().length>1)
 		{
 			String tmp_password = new String(pwBackPasswordField.getPassword());
 			if(!tmp_password.equals(Config.getConfig().getBackupDBPw())) {
 				Config.getConfig().setBackupDBPw(tmp_password);
 				changes = true;
 			}
 		}
 		
 		
 		if(changes){
 			Config.write();
 		}
 	}
 	
 	class CardButtonController implements ActionListener{
 
 		private JPanel ref;
 		
 		public CardButtonController(JPanel ref){
 			this.ref = ref;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			CardLayout card = (CardLayout) ref.getLayout();
 			card.show( ref, ((JToggleButton)e.getSource()).getText() );
 		}
 	}
 	
 	class SettingButtonController implements ActionListener{
 
 		public void actionPerformed(ActionEvent e) {
 			JButton source = (JButton)e.getSource();
 			
 			switch(source.getText()){
 			case "Reset" :
 				getDefaults();
 				break;
 			case "Accept" :
 				acceptSettings();
 				closeThis();
 				break;
 			case "Cancel" :
 				closeThis();
 				break;
 			}
 		}
 		
 	}
 	class PushPullButtonController implements ActionListener{
 		
 		public void actionPerformed(ActionEvent e) {
 			JButton source = (JButton)e.getSource();
 			
 			switch(source.getText()){
 			case "Create" :
				String username=userPushPullTextField.getText();
				String password=pwPushPullPasswordField.getText();
				int res = DatabaseEngine.getDatabaseEngine().createUser(username, password);{
				 switch(res){
				 case 2:
					 //alles Sahne
					 break;
				 case 1:
					 //user gab es bereits
					 break;
				 case 0:
					 //Backupserver nicht erreichbar Datanbank fehlt
					 break;
				 }
				}
 				break;
 			}
 		}
 		
 	}
 }
