 package jsc_controller;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.TrayIcon.MessageType;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.Desktop;
 import java.io.*;
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.Enumeration;
 
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 
 import translation.T;
 
 import jsc_server.CantFindMachine;
 import jsc_server.Machine;
 import jsc_server.MenuItem;
 
 public class Jsc_controller {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public static void main(String[] args) {
 		new Jsc_controller();
 	}
 
 	// Sizes
 	protected Dimension           dimension_tree = new Dimension(350,400);
 	protected Dimension           dimension_frame = dimension_tree;
 
 	// 
 	protected ItemList<MenuItem>  menuitems; // All menuitems (machines, projectors, etc)
 	protected ArrayList<Group>    groups; // Groups
 	protected JSCTree             tree; // The tree
 	protected String              namerootnode = "Vitenfabrikken"; // Name of the root node
 
 	protected int                 statusupdate_rate_seconds = 60;
 
 	protected String projector_username = "";
 	protected String projector_password = "";
 	boolean wait_for_input = false;
 
 	// Types of menuitems
 	public static int type_machine = 1;
 	public static int type_projectorNEC = 2;
 
 	//
 	private JFrame main_frame;
 	private JFrame group_frame;
 	private JFrame projector_frame;
 	private JFrame account_frame;
 
 	//TextFields
 	JTextField txt_projector_name;
 	JTextField txt_projector_ip;
 	JTextField txt_projector_username;
 	JPasswordField txt_projector_password;
 
 	//Combo box
 	JComboBox projectorList;
 	String list_result;
 
 	public Jsc_controller () {
 		getMenuItems();
 		getGroups();
 
 		/************ USERNAME AND PASSWORD PANEL ************/
 
 		// Setting up panel
 		JPanel account_panel = new JPanel();
 		account_panel.setLayout(new GridLayout(0, 2));
 		//projector_panel.setLayout(new BorderLayout(0,0));
 		account_panel.setSize(dimension_frame);
 
 		//Labels
 		JLabel lbl_projector_username = new JLabel("Brukernavn:");
 		JLabel lbl_projector_password = new JLabel("Passord:");
 
 		//buttons
 		JButton saveAccountButton = new JButton("Lagre");
 		saveAccountButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				if (txt_projector_username.getText().equals(""))
 				{
 					projector_username = "";
 				} else {
 					projector_username = txt_projector_username.getText();
 				}
 				if (txt_projector_password.getText().equals(""))
 				{
 					projector_password = "";
 				} else {
 					projector_password = txt_projector_password.getText();
 				}
 
 				account_frame.setVisible(false);
 				writeToProjectorConfig(txt_projector_ip.getText(),txt_projector_name.getText(),list_result);
 
 
 			}
 		});
 
 		//TextFields
 		txt_projector_username = new JTextField();
 		txt_projector_password = new JPasswordField();
 
 		// add to panel
 
 		account_panel.add(lbl_projector_username);
 		account_panel.add(txt_projector_username);
 
 		account_panel.add(lbl_projector_password);
 		account_panel.add(txt_projector_password);
 
 		account_panel.add(saveAccountButton);
 
 		/************ PROJECTOR PANEL ************/
 
 		// Setting up panel
 		JPanel projector_panel = new JPanel();
 		projector_panel.setLayout(new GridLayout(0, 2));
 		//projector_panel.setLayout(new BorderLayout(0,0));
 		projector_panel.setSize(dimension_frame);
 
 
 		//Labels
 		JLabel lbl_projector_type = new JLabel(T.t("Type")+":");
 		JLabel lbl_projector_name = new JLabel(T.t("Name")+":");
 		JLabel lbl_projector_ip = new JLabel(T.t("IP")+":");
 
 		//Buttons
 		JButton saveButton = new JButton(T.t("Save"));
 		saveButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				validateNewProjector();
 
 			}
 		});
 
 		//TextFields
 		txt_projector_ip = new JTextField();
 		txt_projector_name = new JTextField();
 
 		// Drop down list of projectors
 		String[] projectorDropdown = {"NEC","HITACHI","PD" };
 		projectorList = new JComboBox(projectorDropdown);
 		projectorList.setSelectedIndex(0);
 		list_result = (String)projectorList.getSelectedItem();
 		projectorList.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				list_result = (String)projectorList.getSelectedItem();
 
 			}
 		});
 
 		// add to panel
 
 		projector_panel.add(lbl_projector_type);
 		projector_panel.add(projectorList);
 
 		projector_panel.add(lbl_projector_ip);
 		projector_panel.add(txt_projector_ip);
 
 		projector_panel.add(lbl_projector_name);
 		projector_panel.add(txt_projector_name);
 
 		projector_panel.add(saveButton);
 
 
 		/************ GROUP WINDOW ************/
 
 		// Setting up panel
 		JPanel group_panel = new JPanel();
 		group_panel.setLayout(new BorderLayout(0, 0));
 		group_panel.setSize(dimension_frame);
 
 		// Setting up the tree
 		tree = new JSCTree();
 		populateTree(); // Populate tree
 		group_panel.add(tree, BorderLayout.CENTER);
 
 		// Buttons
 		JButton wakeupButton = new JButton(T.t("Turn on"));
 		wakeupButton.setActionCommand(WAKEUP_COMMAND);
 		wakeupButton.addActionListener(new buttonListner(group_panel));
 
 		JButton shutdownButton = new JButton(T.t("Turn off"));
 		shutdownButton.setActionCommand(SHUTDOWN_COMMAND);
 		shutdownButton.addActionListener(new buttonListner(group_panel));
 
 		JButton rebootButton = new JButton(T.t("Restart"));
 		rebootButton.setActionCommand(REBOOT_COMMAND);
 		rebootButton.addActionListener(new buttonListner(group_panel));
 
 		JButton addProjector = new JButton(T.t("New projector"));
 		addProjector.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				projector_frame.setVisible(true);
 			}
 		});
 
 		JPanel panel = new JPanel(new GridLayout(0,4));
 		panel.add(wakeupButton);
 		panel.add(shutdownButton);
 		panel.add(rebootButton);
 		panel.add(addProjector);
 		group_panel.add(panel, BorderLayout.SOUTH);
 
 
 		/************ MAIN WINDOW ************/
 
 		JPanel main_panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		JPanel main_panel2 = new JPanel(new GridLayout(0,1));
 		main_panel.add(main_panel2);
 
 		JLabel gp_txt;
 		JPanel gp, gp_buttonsandstatus, gp_buttons, gp_status;
 		JButton gp_turnoff, gp_turnon;
 		for (Group group : groups) {
 			if(group.mainwindow)
 			{
 				gp                   = new JPanel(new GridLayout(3,0));
 				gp_buttonsandstatus  = new JPanel(new FlowLayout(FlowLayout.LEFT));
 				gp_buttons           = new JPanel(new GridLayout(0, 2));
 				gp_txt = new JLabel(group.name);
 				gp_txt.setFont(new Font("Serif", Font.BOLD, 20));
 				gp.add(gp_txt);
 
 				// Buttons
 				gp_turnon   = new JButton(T.t("Turn on"));
 				gp_turnoff  = new JButton(T.t("Turn off"));
 
 				gp_turnon   .setSize(100, 20);
 				gp_turnoff  .setSize(100, 20);
 
 				gp_turnon   .addActionListener(new group_onoff(group, true));
 				gp_turnoff  .addActionListener(new group_onoff(group, false));
 
 				gp_buttons.add(gp_turnon);
 				gp_buttons.add(gp_turnoff);
 
 				gp_status = new JPanel();
 				gp_status.add(group.mainwindow_label);
 
 				gp_buttonsandstatus.add(gp_buttons);
 				gp_buttonsandstatus.add(gp_status);
 
 
 				gp.add(gp_buttonsandstatus);
 				main_panel2.add(gp);
 			}
 		}
 
 		// Show all
 		JPanel showall_panel = new JPanel();
 		JButton showall = new JButton(T.t("Show all groups"));
 		showall.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				group_frame.setVisible(true);
 			}
 
 		});
 		showall_panel.add(showall);
 		main_panel2.add(showall_panel);
 
 
 		// Setting up the updater thread
 		(new Thread() {
 			public void run () {
 				while(true)
 				{
 					updateStatuses();
 
 					try {
 						Thread.sleep(statusupdate_rate_seconds * 1000); // Every minute
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}).start();
 
 		/**** FRAMES ****/
 
 
 		//make the projector window
 		projector_frame = new JFrame(T.t("Add new projector"));
 		projector_frame.add(projector_panel);
 		projector_frame.pack();
 		//projector_frame.setSize(500, 500);
 		projector_frame.setLocation(200, 200);
 
 		account_frame = new JFrame(T.t("User information"));
 		account_frame.add(account_panel);
 		account_frame.pack();
 		account_frame.setSize(300, 100);
 		account_frame.setLocation(200, 200);
 
 		// Make the window
 		group_frame = new JFrame("Java System Control - " + T.t("all groups"));
 		/*group_frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);*/
 		group_frame.add(group_panel);
 		group_frame.pack();
 		group_frame.setSize(500, 500);
 		group_frame.setLocation(200, 200);
 		/*group_frame.addWindowListener(new WindowAdapter(){
 			public void windowClosing (WindowEvent w)
 			{
 				group_frame.setVisible(false);
 			}
 		});*/
 
 		// Make the main window
 		main_frame = new JFrame("Java System Control");
 		main_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		main_frame.add(main_panel);
 		main_frame.pack();
 		main_frame.setSize(500, 500);
 		main_frame.setVisible(true);
 
 
 	}
 
 	private static String WAKEUP_COMMAND = "wol";
 	private static String SHUTDOWN_COMMAND = "shutdown";
 	private static String REBOOT_COMMAND = "reboot";
 
 	public class buttonListner implements ActionListener
 	{
 		JPanel panel;
 		public buttonListner (JPanel panel)
 		{
 			this.panel = panel;
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			String command = e.getActionCommand();
 
 			MenuItem[] selected = tree.currentSelected();
 
 			for (int i = 0; i < selected.length; i++) {
 				if (WAKEUP_COMMAND.equals(command)) {
 					selected[i].wakeup();
 				} else if (SHUTDOWN_COMMAND.equals(command)) {
 					selected[i].shutdown();
 				} else if (REBOOT_COMMAND.equals(command)) {
 					selected[i].reboot();
 				}
 			}
 
 			panel.repaint();
 		}
 	}
 
 	public void getMenuItems () {
 		// Getting from the directories
 		File dir = new File(System.getProperty("user.home") + File.separatorChar + "jsc_config" + File.separatorChar);
 
 		if (!dir.exists())
 		{
 			boolean FileCreated = (new File(System.getProperty("user.home") + File.separatorChar + "jsc_config" + File.separatorChar)).mkdir();
 
 			if (FileCreated) {
 				System.out.println("Config folder created");
 			}
 			else {
 				System.out.println("Could not create config folder - Exiting");
 			}
 		}
 		String[] dirlist = dir.list();
 
 		menuitems = new ItemList<MenuItem>();
 		int machines = 0;
 		int projector_nec = 0;
 		int projector_hitachi = 0;
 		int projector_pd = 0;
 		for (int i = 0; i < dirlist.length; i++) {
 			//System.out.println("i = " + i + ", dirlist[i] = " + dirlist[i]);
 			MenuItem menuitem = null;
 			if(dirlist[i].startsWith("machine_") && dirlist[i].length() > 12)
 			{
 				machines++;
 				try {
 					// TODO: use itemList.equals
 					menuitem = new Machine(dirlist[i].substring(8, dirlist[i].length()-4));
 				} catch (CantFindMachine a){
 					System.out.println("Problem with the machine " + dirlist[i].substring(8, dirlist[i].length()-4));
 					menuitem = null;
 				}
 			}
 			else if(dirlist[i].startsWith("projector_NEC_") && dirlist[i].length() > 18)
 			{
 				projector_nec++;
 				try {
 					// TODO: use itemList.equals
 					menuitem = new ProjectorNEC(dirlist[i].substring(14, dirlist[i].length()-4));
 				} catch (CantFindMachine a){
 					System.out.println("Problem with NEC projector " + dirlist[i].substring(14, dirlist[i].length()-4));
 					menuitem = null;
 				}
 			}
 			else if(dirlist[i].startsWith("projector_Hitachi_") && dirlist[i].length() > 18)
 			{
 				projector_hitachi++;
 				try {
 					// TODO: use itemList.equals
 					menuitem = new ProjectorHitachi(dirlist[i].substring(18, dirlist[i].length()-4));
 				} catch (CantFindMachine a){
 					System.out.println("Problem with Hitachi projector " + dirlist[i].substring(18, dirlist[i].length()-4));
 					menuitem = null;
 				}
 			}
 			else if(dirlist[i].startsWith("projector_PD_") && dirlist[i].length() > 18)
 			{
 				projector_pd++;
 				try {
 					// TODO: use itemList.equals
					menuitem = new ProjectorPD(dirlist[i].substring(13, dirlist[i].length()-4));
 				} catch (CantFindMachine a){
					System.out.println("Problem with PD projector " + dirlist[i].substring(13, dirlist[i].length()-4));
 					menuitem = null;
 				}
 			}
 			
 			if(menuitem != null)
 				menuitems.add(menuitem);
 		}
 		System.out.println("Machines found: " + machines);
 		System.out.println("NEC projectors found: " + projector_nec);
 		System.out.println("Hitachi projectors found: " + projector_hitachi);
 		System.out.println("PD projectors found: " + projector_pd);
 	}
 
 	public synchronized void updateStatuses ()
 	{
 		for (Group item2 : groups) {
 			item2.getStatusText();
 		}
 		for (MenuItem item : menuitems) {
 			item.getStatusText();
 		}
 
 		tree.updateTree();
 	}
 
 	public void getGroups () {
 		groups = new ArrayList<Group>();
 
 		// Make the group "All machines"
 		this.addGroup(T.t("All machines"));
 		for (MenuItem item : menuitems) {
 			this.addContentLastGroup(item);
 		}
 
 		// Getting groups
 		File groupsettings = new File(System.getProperty("user.home") + File.separatorChar + "jsc_config" + File.separatorChar + "groups.conf");
 
 		if(!groupsettings.exists()) {
 			// Create config file
 			try {
 				File file = new File(groupsettings.getAbsolutePath());
 				boolean FileCreated = file.createNewFile();
 				if (FileCreated) {
 					System.out.println("Successfully created config empty config file @ "+ groupsettings.getAbsolutePath());
 					writeToConfig(groupsettings.getAbsolutePath());
 				} else {
 					System.out.println("File allready exists");
 				}
 			} catch (IOException e) {
 				System.out.println("Error: "+e);
 			}
 
 		}
 
 		if(!groupsettings.exists())
 		{
 			System.out.println("Can't find groups.conf. Was trying "+groupsettings.getAbsolutePath());
 
 		}	
 		else
 		{
 			// Behandle
 			FileInputStream fis = null;
 			BufferedInputStream bis = null;
 			DataInputStream dis = null;
 
 			try {
 				fis = new FileInputStream(groupsettings);
 
 				// Here BufferedInputStream is added for fast reading.
 				bis = new BufferedInputStream(fis);
 				dis = new DataInputStream(bis);
 				String line;
 				// dis.available() returns 0 if the file does not have more lines.
 				while (dis.available() != 0) {
 					// this statement reads the line from the file and print it to
 					// the console.
 					line = dis.readLine();
 					if(!line.equals(""))
 					{
 						if(line.startsWith("[") && line.length() > 2)
 						{
 							// New group
 							this.addGroup(line.substring(1, line.length()-1));
 						}
 
 						/* Group settings */
 						else if (line.equals("mainwindow"))
 						{
 							lastGroup().mainwindow = true;
 						}
 						else if (line.equals("shutdown_enabled"))
 						{
 							lastGroup().shutdown_enabled = true;
 						}
 						else if (line.equals("shutdown_disabled"))
 						{
 							lastGroup().shutdown_enabled = false;
 						}
 						else if (line.equals("wakeup_enabled"))
 						{
 							lastGroup().wakeup_enabled = true;
 						}
 						else if (line.equals("wakeup_disabled"))
 						{
 							lastGroup().wakeup_enabled = false;
 						}
 						else if (line.startsWith("wakeup_msg "))
 						{
 							lastGroup().wakeup_msg = line.substring("wakeup_msg ".length());
 						}
 						else if (line.startsWith("shutdown_msg "))
 						{
 							lastGroup().shutdown_msg = line.substring("shutdown_msg ".length());
 						}
 
 						/* Group content */
 						else if (line.startsWith("projectorNEC ") && line.length() > 13) {
 							try {
 								ProjectorNEC element = new ProjectorNEC (line.substring(13));
 								this.addContentLastGroup(element);
 							} catch (CantFindMachine e) {
 								System.out.println("Can't find a config file for NEC projector: " + 
 										line.substring(13));
 							}
 						}
 						else if (line.startsWith("projectorPD ") && line.length() > 9) {
 							try {	
 								ProjectorPD element = new ProjectorPD (line.substring(9));
 								this.addContentLastGroup(element);
 							} catch (CantFindMachine e) {
 								System.out.println("Can't find a config file for PD projector: " + 
 										line.substring(9));
 							}
 						}
 						else if (line.startsWith("projectorHitachi ") && line.length() > 16) {
 							try {	
 								ProjectorHitachi element = new ProjectorHitachi (line.substring(12));
 								this.addContentLastGroup(element);
 							} catch (CantFindMachine e) {
 								System.out.println("Can't find a config file for Hitachi projector: " + 
 										line.substring(16));
 							}
 						}
 						else {
 							// Finding the machine
 							int index = menuitems.indexOf(line,	type_machine);
 							if(index >= 0)
 							{
 								this.addContentLastGroup(menuitems.get(index));
 							}
 							else
 							{
 								System.out.println("Finner ikke konfigrasjonsfil for maskinen med adresse " + line);
 							}
 						}
 					}
 				}
 
 				// dispose all the resources after using them.
 				fis.close();
 				bis.close();
 				dis.close();
 
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.exit(0);
 			}
 		}
 	}
 
 	public void populateTree()
 	{
 		// Leser grupper
 		if(groups.size() > 0)
 		{
 			DefaultMutableTreeNode gruppeX;
 			for (int i = 0; i < groups.size(); i++) {
 				gruppeX = tree.addObject(groups.get(i));
 				for(int j = 0; j < groups.get(i).getContent().size(); j++)
 				{
 					tree.addObject(groups.get(i).getContent().get(j), gruppeX);
 				}
 			}
 		}
 	}
 
 	/**
 	 * JSCTree contains a scroll pane with a tree in it
 	 */
 	public class JSCTree extends JPanel
 	{
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		private JTree realtree;
 		private DefaultTreeModel treeModel;
 		private DefaultMutableTreeNode rootNode;
 		public JSCTree()
 		{
 			super(new GridLayout(1, 0));
 
 			rootNode = new DefaultMutableTreeNode(namerootnode);
 			treeModel = new DefaultTreeModel(rootNode);
 
 			realtree = new JTree(treeModel);
 			JScrollPane treeView = new JScrollPane(realtree);
 			treeView.setBorder(BorderFactory.createTitledBorder("Maskiner"));
 			setLayout(new BorderLayout(0,0));
 			add(treeView);
 
 			realtree.setShowsRootHandles(true);
 
 			setSize(dimension_tree);
 		}
 
 		public DefaultMutableTreeNode addObject(MenuItem item)
 		{
 			return addObject(item, null);
 		}
 
 		public DefaultMutableTreeNode addObject(MenuItem item, DefaultMutableTreeNode parent)
 		{
 			if (parent == null) {
 				parent = rootNode;
 			}
 
 			DefaultMutableTreeNode item2 = new DefaultMutableTreeNode(item);
 			treeModel.insertNodeInto(item2, parent, 
 					parent.getChildCount());
 			return item2;
 		}
 
 		public void viewRoot()
 		{
 			DefaultMutableTreeNode child = (DefaultMutableTreeNode)rootNode.getFirstChild();
 			realtree.scrollPathToVisible(new TreePath(child.getPath()));
 		}
 
 		public MenuItem[] currentSelected()
 		{
 			TreePath[] tmp = realtree.getSelectionPaths();
 			//int[] tmp = this.realtree.getSelectionRows();
 			MenuItem[] tmp2 = new MenuItem[tmp.length];
 			DefaultMutableTreeNode tmp3;
 			for (int i = 0; i < tmp.length; i++) {
 				tmp3 = (DefaultMutableTreeNode)tmp[i].getLastPathComponent();
 				tmp2[i] = (MenuItem)tmp3.getUserObject();
 			}
 
 			return tmp2;
 		}
 
 		public void updateTree ()
 		{
 			for (Enumeration e = rootNode.breadthFirstEnumeration(); e.hasMoreElements();) {
 				DefaultMutableTreeNode c = (DefaultMutableTreeNode) e.nextElement();
 
 				treeModel.valueForPathChanged(
 						new TreePath(c.getPath()), 
 						c.getUserObject());
 			}
 			//treeModel.reload();
 		}
 	}
 
 
 	public void addGroup(String gruppe_navn) {
 		groups.add(new Group(gruppe_navn));
 	}
 
 	public void writeToConfig(String config_file) {
 		System.out.println("File: "+ config_file);
 		int keep_on = JOptionPane.showConfirmDialog(null, "No groups are defined, do you want to define groups in the config file now?");
 		System.out.println(keep_on);
 		if(keep_on == 0) {
 			keep_on = JOptionPane.showConfirmDialog(null,"Do you want me to add some example data for you?");
 			if (keep_on == 0) {
 				writeExampleData(config_file);
 			}
 			try {
 				File file = new File(config_file);
 				Desktop desktop = null;
 				if (Desktop.isDesktopSupported()) {
 					desktop = Desktop.getDesktop();
 				}
 				desktop.edit(file);
 			} catch (IOException e) {
 				System.out.println(e);
 			}
 		} else {
 			keep_on = JOptionPane.showConfirmDialog(null,"Do you want me to load example groups into the config file?");
 			if (keep_on == 0) {
 				writeExampleData(config_file);
 			}
 		}
 	}
 
 	public void writeExampleData(String file) {
 		BufferedWriter ftw = null;
 		try {
 			ftw = new BufferedWriter(new FileWriter(file));
 
 			ftw.write("[groupname]");
 			ftw.newLine();
 			ftw.write("00:00:00:00:00:00");
 			ftw.newLine();
 			ftw.newLine();
 			ftw.write("[Next group]");
 			ftw.newLine();
 			ftw.write("00:00:00:00:00:00");
 			ftw.newLine();
 			ftw.write("projectorNEC Projectorname");
 			ftw.newLine();
 			ftw.write("projectorPD Projectorname2");
 			ftw.newLine();
 			ftw.write("00:00:00:00:00:00");
 			ftw.newLine();
 			ftw.newLine();
 			ftw.write("[Group in mainwindow]");
 			ftw.newLine();
 			ftw.write("mainwindow");
 			ftw.newLine();
 			ftw.write("00:00:00:00:00:00");
 			ftw.newLine();
 			ftw.write("00:00:00:00:00:00");
 			ftw.newLine();
 			ftw.newLine();
 			ftw.write("[Group without shutdown and wakeup]");
 			ftw.newLine();
 			ftw.write("shutdown_msg This group can not be shut down");
 			ftw.newLine();
 			ftw.write("shutdown_disabled");
 			ftw.newLine();
 			ftw.write("wakeup_msg No wakeup");
 			ftw.newLine();
 			ftw.write("wakeup_disabled");
 			ftw.newLine();
 		} catch (IOException e){
 			System.out.println("Error: "+ e);
 		} finally {
 			try {
 				if (ftw != null) {
 					ftw.flush();
 					ftw.close();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void addContentLastGroup (MenuItem item)
 	{
 		groups.get(groups.size()-1).addContent (item);
 	}
 
 	public void addGroupContent (int gruppe_num, MenuItem maskin) {
 		groups.get(gruppe_num).addContent (maskin);
 	}
 
 	private void validateNewProjector() {
 		boolean validateStatus = true;
 		String projectorError = "";
 
 		if (txt_projector_name.getText().length() <= 5) {
 			projectorError = projectorError + "* No name for the projector is specified!\n";
 			validateStatus = false;
 		}
 
 		if (txt_projector_ip.getText() != null)
 		{
 
 			if (txt_projector_ip.getText().length() < 7 )
 			{
 				projectorError = projectorError + "* The IP address is not valid\n";
 				validateStatus = false;
 			}
 		}
 		projector_frame.setVisible(false);
 		if (validateStatus == false) {
 			JOptionPane.showMessageDialog(null, projectorError,"Ugyldige parametre",JOptionPane.ERROR_MESSAGE);
 		} else {
 			if(list_result.equals("NEC"))
 			{
 				writeToProjectorConfig(txt_projector_ip.getText(),txt_projector_name.getText(),list_result);
 			}else {
 				account_frame.setVisible(true);
 			}
 		}
 	}
 
 	private void writeToProjectorConfig(String ip, String name, String type) {
 
 		// "(0)NEC","(1)HITACHI","(2)PD"
 		if (type.equals("NEC")) {
 
 			try {
 				ProjectorNEC projector = new ProjectorNEC(name, ip);
 			} catch (CantFindMachine e) {
 				System.out.println(e.getMessage());
 			}
 		}
 		else if (type.equals("PD")) {
 			//TODO: Legge til Brukernavn og passord funksjon på akkuratt denne prosjektoren.
 			try {
 				ProjectorPD projector = new ProjectorPD(name, ip,projector_username,projector_password);
 			} catch (CantFindMachine e) {
 				System.out.println(e.getMessage());
 			}
 		}
 		else if (type.equals("HITACHI")) {
 			//TODO: Legge til Brukernavn og passord funksjon på akkuratt denne prosjektoren.
 			try {
 				ProjectorHitachi projector = new ProjectorHitachi(name, ip,projector_username,projector_password);
 			} catch (CantFindMachine e) {
 				System.out.println(e.getMessage());
 			}
 		}
 	}
 
 	public Group lastGroup ()
 	{
 		return groups.get(groups.size()-1);
 	}
 
 	public class ItemList<E> extends ArrayList<E>
 	{
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 
 		public int indexOf (String uniqueid, int type) throws Exception
 		{
 
 			for (int i = 0; i < size(); i++) {
 				Object item = get(i);
 				if(type == type_machine)
 				{
 					if(item instanceof Machine)
 					{
 						Machine item2 = (Machine)item;
 						if(item2.getMac().equals(Machine.macFilter(uniqueid)))
 						{
 							return i;
 						}
 					}
 					// else: not a match
 				}
 				else if (type == type_projectorNEC)
 				{
 					// TODO
 				}
 				else
 				{
 					throw new Exception("Invalid type.");
 				}
 			}
 			return -1;
 		}
 	}
 
 	class group_onoff implements ActionListener
 	{
 		Group    group;
 		boolean  on;
 		public group_onoff (Group group, boolean on)
 		{
 			this.group  = group;
 			this.on     = on;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if(on)
 			{
 				// Message window
 				if(!group.wakeup_msg.equals(""))
 				{
 					JOptionPane.showMessageDialog(null, group.wakeup_msg);
 				}
 
 				if(group.wakeup_enabled)
 				{
 					group.wakeup();
 					new CountDownWindow(group.name, on, group.getTurnonTime(), main_frame.getLocation());
 				}
 			}
 			else
 			{
 				// Message window
 				if(!group.shutdown_msg.equals(""))
 				{
 					JOptionPane.showMessageDialog(null, group.shutdown_msg);
 				}
 
 				if(group.shutdown_enabled)
 				{
 					group.shutdown();
 					new CountDownWindow(group.name, on, group.getTurnoffTime(), main_frame.getLocation());
 				}
 			}
 			group.getStatusText(); // Updates the status text
 		}
 
 	}
 }
