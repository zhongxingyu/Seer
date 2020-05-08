 package org.openstreetmap.OSMZmiany;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.util.Iterator;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.zip.GZIPInputStream;
 
 import javax.swing.JFrame;
 
 import org.openstreetmap.OSMZmiany.Configuration.Profile;
 import org.openstreetmap.OSMZmiany.DataContainer.Changeset;
 import org.openstreetmap.OSMZmiany.DataContainer.Node;
 import org.openstreetmap.gui.jmapviewer.Coordinate;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.ComboBoxModel;
 import javax.swing.JSplitPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JList;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.JCheckBox;
 import java.awt.Color;
 import javax.swing.JComboBox;
 import javax.swing.JSeparator;
 import javax.swing.SwingConstants;
 import java.awt.event.ItemListener;
 import java.awt.event.ItemEvent;
 import javax.swing.JScrollPane;
 import java.awt.GridLayout;
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 
 public class OSMZmiany extends JFrame implements ZMapWidgetListener,ConfigurationListener {
 	private static final long serialVersionUID = -2976479683829295126L;	
 	
 	public static OSMZmiany instance;
 	
 	private Timer refetchTimer;
 	private int seqNum;
 		
 	public DataContainer dc;
 	private Configuration conf;
 	
 	//Widgets
 	private ZMapWidget map;
 	private JTextField tfURL;
 	private JCheckBox cbxLiveEdit;
 	private JList list;
 	private JComboBox cbProfiles;
 	private DefaultComboBoxModel profilesModel = new DefaultComboBoxModel();
 	private DefaultListModel model = new DefaultListModel();
 	private DefaultListModel modelUsers = new DefaultListModel();
 	
 	
 	private JButton btUser;
 	private JButton btChangeset;
 	private JButton btNode;
 	private JLabel lblBoxB1;
 	private JLabel lblBoxB2;
 	private JList listUsers ;
 	private JComboBox usersListType;
 	boolean setBox;
 
 	public OSMZmiany() {
 		dc=new DataContainer();
 		this.setTitle("OSMZmiany");
 		this.addWindowListener(new windowHandler());
 						
 		conf=Configuration.loadFromFile();
 		conf.addConfigurationListener(this);
 		//MAP
 		map = new ZMapWidget(dc);
 		Profile p=conf.getSelectedProfile();
 		map.addZMapWidgetListener(this);
 		
 		makeGUI();
 		
 		this.profileChanged(p);
 	}
 	
 	public void makeGUI(){
 		GridBagLayout gbl = new GridBagLayout();
 		gbl.rowWeights = new double[]{1.0};
 		gbl.columnWeights = new double[]{1.0};		
 		
 		getContentPane().setLayout(gbl);
 		
 		JSplitPane splitPane = new JSplitPane();
 		GridBagConstraints gbc_splitPane = new GridBagConstraints();
 		gbc_splitPane.fill = GridBagConstraints.BOTH;
 		gbc_splitPane.gridx = 0;
 		gbc_splitPane.gridy = 0;
 		getContentPane().add(splitPane, gbc_splitPane);
 		
 		this.setSize(723, 472);
 splitPane.setRightComponent(map);
 		
 		JPanel panel = new JPanel();
 		splitPane.setLeftComponent(panel);
 		splitPane.setDividerLocation(350);
 		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
 		
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		
 		JPanel panel_1 = new JPanel();
 		tabbedPane.addTab("General", null, panel_1, null);
 		
 		final JButton btnSetBox = new JButton("Set Box");
 		btnSetBox.setBounds(41, 127, 129, 24);
 		btnSetBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				setBox=true;
 			}
 		});
 		
 		JButton btnRemoveBox = new JButton("Remove Box");
 		btnRemoveBox.setBounds(175, 127, 118, 24);
 		btnRemoveBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				conf.getSelectedProfile().setMapFilter(null);
 				map.refrashOverlay();
 			}
 		});
 		panel_1.setLayout(null);
 		
 		JLabel lblSetBoundary = new JLabel("Set boundary:");
 		lblSetBoundary.setBounds(12, 93, 101, 14);
 		panel_1.add(lblSetBoundary);
 		panel_1.add(btnRemoveBox);
 		
 		panel_1.add(btnSetBox);
 		
 		JLabel lblData = new JLabel("Data:");
 		lblData.setBounds(12, 244, 39, 14);
 		panel_1.add(lblData);
 		
 		JLabel lblDiffUrl = new JLabel("Diff URL");
 		lblDiffUrl.setBounds(12, 270, 55, 14);
 		panel_1.add(lblDiffUrl);
 		
 		tfURL = new JTextField();
 		tfURL.setBounds(94, 270, 199, 24);
 		panel_1.add(tfURL);
 		tfURL.setColumns(10);
 		
 		JButton btnLoad = new JButton("Load");
 		btnLoad.setBounds(224, 307, 69, 24);
 		btnLoad.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				getData(tfURL.getText());
 			}
 		});
 		panel_1.add(btnLoad);
 		
 		cbxLiveEdit = new JCheckBox("Live edit diff");
 		cbxLiveEdit.setBounds(12, 308, 159, 22);
 		cbxLiveEdit.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				getData();	
 			}
 		});
 		
 		panel_1.add(cbxLiveEdit);
 		
 		
 		JButton btnClear = new JButton("Clear");
 		btnClear.setBounds(222, 343, 71, 24);
 		btnClear.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				dc.clear();	
 			}
 		});
 		
 		
 		panel_1.add(btnClear);
 		
 		JButton btAddProfile = new JButton("Add");
 		btAddProfile.setBounds(198, 43, 95, 24);
 		panel_1.add(btAddProfile);
 		
 		cbProfiles = new JComboBox(profilesModel);
 		cbProfiles.setBounds(100, 8, 193, 23);
 		panel_1.add(cbProfiles);
 		
 		JLabel label = new JLabel("Profile");
 		label.setBounds(12, 12, 70, 14);
 		panel_1.add(label);
 		
 		JSeparator separator = new JSeparator();
 		separator.setBounds(12, 79, 351, 2);
 		panel_1.add(separator);
 		
 		JLabel lblPointA = new JLabel("Point A:");
 		lblPointA.setBounds(41, 163, 70, 14);
 		panel_1.add(lblPointA);
 		
 		JLabel lblPointB = new JLabel("Point B:");
 		lblPointB.setBounds(43, 189, 70, 14);
 		panel_1.add(lblPointB);
 		
 		lblBoxB1 = new JLabel("Not selected");
 		lblBoxB1.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblBoxB1.setBounds(123, 163, 170, 14);
 		panel_1.add(lblBoxB1);
 		
 		lblBoxB2 = new JLabel("Not selected");
 		lblBoxB2.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblBoxB2.setBounds(125, 189, 168, 14);
 		panel_1.add(lblBoxB2);
 		
 		JSeparator separator_1 = new JSeparator();
 		separator_1.setBounds(12, 230, 351, 2);
 		panel_1.add(separator_1);
 		
 		JButton btnEditInP2 = new JButton("Edit in Potlatch2");
 		btnEditInP2.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				int i=map.getZoom();
 				Coordinate c=map.getPosition();
 				openURL("http://www.openstreetmap.org/edit?editor=potlatch2&lat="+c.getLat()+"&lon="+c.getLon()+"&zoom="+i);
 			}
 		});
 		btnEditInP2.setBounds(12, 379, 149, 24);
 		panel_1.add(btnEditInP2);
 		
 		JButton btnEditInJosm = new JButton("Edit in JOSM");
 		btnEditInJosm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
//TODO:				http://localhost:8111/load_and_zoom?left=16.8853396&right=16.8873396&top=52.4321631&bottom=52.4301631
 			}
 		});
 		btnEditInJosm.setBounds(164, 379, 129, 24);
 		panel_1.add(btnEditInJosm);
 		panel.add(tabbedPane);
 		
 		JPanel panel_2 = new JPanel();
 		tabbedPane.addTab("Changesets", null, panel_2, null);
 		panel_2.setLayout(null);
 		
 		JButton btnShowSite = new JButton("Info");
 		btnShowSite.setBounds(154, 5, 61, 24);
 		panel_2.add(btnShowSite);
 		
 		list = new JList(model);
 		list.setBounds(12, 41, 351, 664);
 		panel_2.add(list);
 		
 		JPanel panel_4 = new JPanel();
 		tabbedPane.addTab("Users list", null, panel_4, null);
 		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.Y_AXIS));
 		
 		JPanel panel_5 = new JPanel();
 		panel_4.add(panel_5);
 		panel_5.setLayout(null);
 		
 		JLabel lblListType = new JLabel("List type");
 		lblListType.setBounds(12, 16, 62, 14);
 		panel_5.add(lblListType);
 		
 		usersListType = new JComboBox();
 		usersListType.setBounds(87, 12, 204, 23);
 		panel_5.add(usersListType);
 		usersListType.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent arg0) {
 				//System.out.println(usersListType.getSelectedIndex());
 				boolean z=false;
 				if(usersListType.getSelectedIndex()==1)
 					z=true;
 				conf.getSelectedProfile().setListType(z);
 				reloadUsersList();
 				map.refrashOverlay();
 			}
 		});
 		usersListType.setModel(new DefaultComboBoxModel(new String[] {"Blacklist", "Whitelist"}));
 		
 		JButton btnRemoveUser = new JButton("Remove selected user");
 		btnRemoveUser.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				int i=listUsers.getSelectedIndex();
 				User[] us=conf.getSelectedProfile().getUsers();
 				if(i<us.length&&i>=0){
 					conf.getSelectedProfile().removeUser(us[i]);
 					reloadUsersList();
 					map.refrashOverlay();
 				}
 			}
 		});
 		btnRemoveUser.setBounds(87, 59, 204, 24);
 		panel_5.add(btnRemoveUser);
 		
 		JScrollPane scrollPane = new JScrollPane();
 		panel_4.add(scrollPane);
 		
 		listUsers = new JList(modelUsers);
 		scrollPane.setViewportView(listUsers);
 		
 		JPanel panel_3 = new JPanel();
 		tabbedPane.addTab("Info", null, panel_3, null);
 		panel_3.setLayout(null);
 		
 		JLabel lblNode = new JLabel("Node");
 		lblNode.setBounds(12, 12, 70, 14);
 		panel_3.add(lblNode);
 		
 		JLabel lblChangeset = new JLabel("Changeset");
 		lblChangeset.setBounds(12, 38, 90, 14);
 		panel_3.add(lblChangeset);
 		
 		JLabel lblUser = new JLabel("User");
 		lblUser.setBounds(12, 64, 70, 14);
 		panel_3.add(lblUser);
 		
 		btChangeset = new JButton("#ID");
 		btChangeset.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(!btChangeset.getText().equals("#ID"))
 					OSMZmiany.openURL("http://www.openstreetmap.org/browse/changeset/"+btChangeset.getText());	
 			}
 		});
 		btChangeset.setForeground(Color.BLUE);
 		btChangeset.setBounds(109, 38, 120, 14);
 		panel_3.add(btChangeset);
 		
 		btNode = new JButton("#ID");
 		btNode.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(!btNode.getText().equals("#ID"))
 					OSMZmiany.openURL("http://www.openstreetmap.org/browse/node/"+btNode.getText());
 			}
 		});
 		btNode.setForeground(Color.BLUE);
 		btNode.setBounds(109, 12, 120, 14);
 		panel_3.add(btNode);
 		
 		btUser = new JButton("#ID");
 		btUser.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(!btUser.getText().equals("#ID"))
 					OSMZmiany.openURL("http://www.openstreetmap.org/user/"+btUser.getText());
 			}
 		});
 		btUser.setForeground(Color.BLUE);
 		btUser.setBounds(109, 64, 120, 14);
 		panel_3.add(btUser);
 		
 		JButton btSelectChangeset = new JButton("Select changeset");
 		btSelectChangeset.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Node n=map.drawStyle.getSelectedNode();
 				if(n!=null){
 					map.drawStyle.setSelection(dc.changesets.get(dc.changesetsIndex.get(n.changesetId)));
 					map.refrashOverlay();
 				}								
 			}
 		});
 		btSelectChangeset.setBounds(13, 116, 216, 24);
 		panel_3.add(btSelectChangeset);
 		
 		JButton btnAddUser = new JButton("Add to list");
 		btnAddUser.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(map.drawStyle.getSelectedNode()!=null){
 					conf.getSelectedProfile().addUser(dc.users.get(dc.changesets.get(dc.changesetsIndex.get(map.drawStyle.getSelectedNode().changesetId)).userId));
 				}else if(map.drawStyle.getSelectedChangeset()!=null){
 					conf.getSelectedProfile().addUser(dc.users.get(dc.changesets.get(dc.changesetsIndex.get(map.drawStyle.getSelectedChangeset().id)).userId));
 				}
 				reloadUsersList();
 				map.refrashOverlay();
 			}
 		});
 		
 		btnAddUser.setBounds(109, 90, 120, 14);
 		panel_3.add(btnAddUser);
 		setVisible(true);
 
 
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				System.exit(0);
 			}
 		});
 
 		refetchTimer = new Timer();
 		refetchTimer.scheduleAtFixedRate(new TimerTask() {
 			public void run() {
 				if(cbxLiveEdit.isSelected()){
 					initChangeStream();
 					getData();
 				}
 			}
 		}, 20000, 30000);	
 		
 
 	}
 
 	private void initChangeStream() {
 		try {
 			BufferedReader br = new BufferedReader(
 					new InputStreamReader(
 							new BufferedInputStream(
 									new URL(
 											"http://planet.openstreetmap.org/minute-replicate/state.txt")
 											.openStream())));
 			br.readLine();
 			String seqNumStr = br.readLine();
 			seqNum = Integer.parseInt(seqNumStr.substring(seqNumStr
 					.indexOf("=") + 1));
 			br.readLine();
 			br.close();
 		} catch (IOException ioe) {
 			System.err.println("initChangeStream: Connection error!");
 		}
 	}
 
 	public void getData() {		
 			DecimalFormat myFormat = new DecimalFormat("000");
 			String url = "http://planet.openstreetmap.org/minute-replicate/"
 					+ myFormat.format(seqNum / 1000000) + "/"
 					+ myFormat.format((seqNum / 1000) % 1000) + "/"
 					+ myFormat.format(seqNum % 1000) + ".osc.gz";			
 			getData(url);
 			seqNum++;			
 	}
 	
 	public void getData(String url){
 		try {
 			BufferedInputStream bis = new BufferedInputStream(
 					new GZIPInputStream(new URL(url).openStream()));
 			System.out.println("Download: "+url);
 			dc.addData(bis);		
 		} catch (IOException ioe) {
 			if (ioe instanceof FileNotFoundException) {
 			} else {
 				ioe.printStackTrace();
 			}
 		}
 	}
 
 
 	public void reloadChangesets(){
 		model.clear();
 		Iterator<Changeset> iterator = dc.changesets.iterator();
 	    while (iterator.hasNext()) {
 	    	Changeset ch=iterator.next();
 	    	model.add(0, ch.id+":"+dc.users.get(ch.userId).name);  	
 	    }
 	}
 				
 	public static void openURL(String url) {
 		if( !java.awt.Desktop.isDesktopSupported() ) {
             System.err.println( "Desktop is not supported (fatal)" );
         }
         java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
         if( !desktop.isSupported( java.awt.Desktop.Action.BROWSE ) ) {
             System.err.println( "Desktop doesn't support the browse action (fatal)" );
         }
        try {
            java.net.URI uri = new java.net.URI( url );
            desktop.browse( uri );
        }
        catch ( Exception e ) {
             System.err.println( e.getMessage() );
        }
     }
 	
 	public static void main(String[] args) {
 		instance = new OSMZmiany();
 		instance.initChangeStream();
 		instance.setVisible(true);
 	}
 
 	public void nodeClicked(Node node) {
 		btNode.setText(Long.toString(node.id));
 		btChangeset.setText(Long.toString(node.changesetId));
 		btUser.setText(dc.users.get(dc.changesets.get(dc.changesetsIndex.get(node.changesetId)).userId).name);
 		map.drawStyle.setSelection(node);
 	}
 
 	public void profileChanged(Profile p) {
 		this.reloadProfiles();
 		this.reloadUsersList();
 		this.reloadUserType();		
 		map.refrashOverlay();
 	}
 	
 	public void reloadProfiles(){
 		profilesModel.removeAllElements();
 		Profile[] p=conf.getProfiles();
 		for(int i=0;i<p.length;i++)
 			profilesModel.addElement(p[i].getName());
 	}
 	
 	public void reloadUserType(){
 		Profile p=conf.getSelectedProfile();
 		//Combo type	
 		usersListType.setSelectedIndex(p.getListType()?1:0);
 	}
 	
 	public void reloadUsersList(){
 		Profile p=conf.getSelectedProfile();
 		//Users
 		modelUsers.removeAllElements();
 		User[]users=p.getUsers();
 		for(int i=0;i<users.length;i++)
 			modelUsers.addElement(users[i].id+":"+users[i].name);
 	}
 	
 	
 	private class windowHandler extends WindowAdapter
 	{
 		public void windowClosing( java.awt.event.WindowEvent event )
 		{            
 			System.out.println("EXIT");
 			conf.saveToFile();
 		}
 	}
 	
 	
 
 
 	public void boxDrawed(Coordinate c1, Coordinate c2) {
 		if(setBox){
 		double d1=Math.round(c1.getLat()*100);
 		d1/=100;
 		double d2=Math.round(c1.getLon()*100);
 		d2/=100;
 		double e1=Math.round(c2.getLat()*100);
 		e1/=100;
 		double e2=Math.round(c2.getLon()*100);
 		e2/=100;
 		
 		lblBoxB1.setText("LAT:"+d1+";LON:"+d2);
 		lblBoxB2.setText("LAT:"+e1+";LON:"+e2);
 		MapFilter mf=new BoundaryMapFilter(c1.getLat(),c1.getLon(),c2.getLat(),c2.getLon());
 		dc.removeData(mf);		
 		conf.getSelectedProfile().setMapFilter(mf);
 		map.refrashOverlay();
 		setBox=false;
 		}
 	}
 }
