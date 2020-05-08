 package org.openstreetmap.OSMZmiany;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.util.Date;
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
 import javax.swing.JOptionPane;
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
 
 public class OSMZmiany extends JFrame implements ZMapWidgetListener,ConfigurationListener {
 	private static final long serialVersionUID = -2976479683829295126L;	
 	
 	public static OSMZmiany instance;
 	
 	private Timer refetchTimer;
 	private int seqNum;
 	private int firstSeq = Integer.MAX_VALUE;
 		
 	public DataContainer dc;
 	private Configuration conf;
 	
 	//Widgets
 	private ZMapWidget map;
 	private JTextField tfBaseUrl;
 	private JTextField tfURL;
 	private JTextField tfKeepHours;
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
 	private JSplitPane splitPane;
 	private JButton btSelectChangeset;
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
 		this.reloadProfiles();
 		btSChangeset();
 	}
 	
 	public void makeGUI(){
 		GridBagLayout gbl = new GridBagLayout();
 		gbl.rowWeights = new double[]{1.0};
 		gbl.columnWeights = new double[]{1.0};		
 		
 		getContentPane().setLayout(gbl);
 		
 		splitPane = new JSplitPane();
 		GridBagConstraints gbc_splitPane = new GridBagConstraints();
 		gbc_splitPane.fill = GridBagConstraints.BOTH;
 		gbc_splitPane.gridx = 0;
 		gbc_splitPane.gridy = 0;
 		getContentPane().add(splitPane, gbc_splitPane);
 		
 		this.setSize(conf.getWindowSize());
 splitPane.setRightComponent(map);
 		
 		JPanel panel = new JPanel();
 		splitPane.setLeftComponent(panel);
 		splitPane.setDividerLocation(conf.getDividerLocation());
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
 				map.refrashOverlay(true);
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
 		
 		JLabel lblBaseUrl = new JLabel("Base URL");
 		tfBaseUrl = new JTextField();
 		JButton btnBaseUrl = new JButton("Set");
 		tfBaseUrl.setText(conf.getDiffBaseUrl());
 		lblBaseUrl.setBounds(12, 270, 70, 14);
 		tfBaseUrl.setBounds(94, 270, 199, 24);
 		btnBaseUrl.setBounds(222, 295, 71, 24);
 		btnBaseUrl.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				conf.setDiffBaseURL(tfBaseUrl.getText());
 				firstSeq = Integer.MAX_VALUE;
 			}
 		});
 		panel_1.add(lblBaseUrl);
 		panel_1.add(tfBaseUrl);
 		panel_1.add(btnBaseUrl);
 		
 		JLabel lblDiffUrl = new JLabel("OSC URL");
 		lblDiffUrl.setBounds(12, 323, 70, 14);
 		panel_1.add(lblDiffUrl);
 		
 		tfURL = new JTextField();
 		tfURL.setBounds(94, 323, 199, 24);
 		panel_1.add(tfURL);
 		tfURL.setColumns(10);
 		
 		JButton btnLoad = new JButton("Load");
 		btnLoad.setBounds(222, 349, 71, 24);
 		btnLoad.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				getData(tfURL.getText());
 			}
 		});
 		panel_1.add(btnLoad);
 		
 		cbxLiveEdit = new JCheckBox("Live edit diff");
 		cbxLiveEdit.setBounds(12, 349, 159, 22);
 		cbxLiveEdit.setSelected(true);
 		cbxLiveEdit.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				getData();	
 			}
 		});
 		
 		panel_1.add(cbxLiveEdit);
 		
 		final JLabel postep = new JLabel("0/0");
 		postep.setBounds(12, 430, 159, 22);
 		panel_1.add(postep);
 		
 		final JButton btnGetLast = new JButton("Get 6h more");
 		btnGetLast.setBounds(12, 410, 159, 22);
 		btnGetLast.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(firstSeq == Integer.MAX_VALUE){
 					initChangeStream();
 				}
 				
 				Thread t=new Thread(){
 					public void run(){
 						final int values=60*6;
 						int sq = firstSeq;
 						btnGetLast.setEnabled(false);
 						while(firstSeq - sq < values  && sq > 0){
 							getData(sq);
 							sq--;
 							postep.setText(""+(firstSeq-sq) +"/" +values);
 						}
 						postep.setText("Complete");
 						firstSeq=sq;
 						btnGetLast.setEnabled(true);
 					}
 				};
 				t.start();
 					
 			}
 		});
 		
 		
 		panel_1.add(btnGetLast);
 		
 		
 		JButton btnClear = new JButton("Clear");
 		btnClear.setBounds(12, 380, 71, 24);
 		btnClear.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				//TODO: handle bad input
 				Integer hours = Integer.parseInt(tfKeepHours.getText());
 				if(hours == 0) {
 					dc.clearAll();
 				}
 				else {
 					long clearTo = new Date().getTime() - (hours * 3600000);
 					dc.clearToTime(clearTo);
 				}
 			}
 		});
 
 		panel_1.add(btnClear);
 		
 		JLabel clearLabel = new JLabel("keep hours:");
 		clearLabel.setBounds(90, 380, 95, 24);
 		panel_1.add(clearLabel);
 		
 		tfKeepHours = new JTextField();
 		tfKeepHours.setText("1");
 		tfKeepHours.setBounds(190, 380, 50, 24);
 		panel_1.add(tfKeepHours);
 		
 		JButton btAddProfile = new JButton("Add");
 		btAddProfile.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				String str=newStringDialog(instance,"New profile","Name:");
 				if(str!=null){					
 					profileChanged(conf.addProfile(str));
 					reloadProfiles();					
 				}
 			}
 		});
 		btAddProfile.setBounds(222, 43, 71, 24);
 		panel_1.add(btAddProfile);
 		
 		cbProfiles = new JComboBox(profilesModel);
 		cbProfiles.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent arg0) {
 				Profile []p=conf.getProfiles();
 				if(cbProfiles.getSelectedIndex()>=0&&cbProfiles.getSelectedIndex()<p.length){
 					conf.selectProfile(p[cbProfiles.getSelectedIndex()]);
 					reloadUserType();
 				}
 			}
 		});
 		cbProfiles.setBounds(94, 8, 199, 23);
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
 		separator_1.setBounds(12, 240, 351, 2);
 		panel_1.add(separator_1);
 		
 		
 		
 		JButton btRemoveProfile = new JButton("Remove");
 		btRemoveProfile.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				conf.removeProfile((String)cbProfiles.getSelectedItem());
 				reloadProfiles();
 				reloadUserType();
 			}
 		});
 		btRemoveProfile.setBounds(94, 43, 95, 24);
 		panel_1.add(btRemoveProfile);
 		
 		JButton btnZoomToBox = new JButton("Show Box");
 		btnZoomToBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				MapFilter f=conf.getSelectedProfile().getMapFilter();
 				if(f!=null&& f instanceof BoundaryMapFilter){
 					BoundaryMapFilter p=(BoundaryMapFilter) f;
 					double l1=Math.abs(p.lat2-p.lat1);
 					double l2=Math.abs(p.lon2-p.lon1);
 					
 					map.setDisplayPositionByLatLon((p.lat2+p.lat1)/2,(p.lon2+p.lon1)/2, findZoom(l1,l2));
 					
 				}
 			}
 		});
 		btnZoomToBox.setBounds(41, 208, 129, 24);
 		panel_1.add(btnZoomToBox);
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
 				conf.getSelectedProfile().setListType((short)usersListType.getSelectedIndex());
 				reloadUsersList();
 				map.refrashOverlay(true);
 			}
 		});
 		usersListType.setModel(new DefaultComboBoxModel(new String[] {"None","Whitelist","Blacklist"}));
 		
 		JButton btnRemoveUser = new JButton("Remove selected user");
 		btnRemoveUser.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				int i=listUsers.getSelectedIndex();
 				User[] us=conf.getSelectedProfile().getUsers();
 				if(i<us.length&&i>=0){
 					conf.getSelectedProfile().removeUser(us[i]);
 					reloadUsersList();
 					map.refrashOverlay(true);
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
 		
 		btSelectChangeset = new JButton("Select changeset");
 		btSelectChangeset.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Node n=map.drawStyle.getSelectedNode();
 				if(n!=null){
 					map.drawStyle.setSelection(dc.getChangesets().get(dc.getChangesetsIndex().get(n.changesetId)));
 					map.refrashOverlay(true);
 				}
 				btSChangeset();				
 			}
 		});
 		btSelectChangeset.setBounds(13, 116, 216, 24);
 		panel_3.add(btSelectChangeset);
 		
 		
 		JButton btnEditInP2 = new JButton("Edit in Potlatch2");
 		btnEditInP2.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(map.drawStyle.getSelectedChangeset()==null){
 					int i=map.getZoom();
 					Coordinate c=map.getPosition();
 					openURL("http://www.openstreetmap.org/edit?editor=potlatch2&lat="+c.getLat()+"&lon="+c.getLon()+"&zoom="+i);
 				}else{
 					Coordinate []p=map.getChangesetCoordinate(map.drawStyle.getSelectedChangeset().id);
 					double pA=p[0].getLat()+p[1].getLat();
 					pA/=2;
 					double pB=p[0].getLon()+p[1].getLon();
 					pB/=2;
 					openURL("http://www.openstreetmap.org/edit?editor=potlatch2&lat="+pA+"&lon="+pB+"&zoom=18");
 					
 				}
 			}
 		});
 		btnEditInP2.setBounds(12, 152, 217, 24);
 		panel_3.add(btnEditInP2);
 		
 		JButton btnEditInJosm = new JButton("Edit in JOSM");
 		btnEditInJosm.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(map.drawStyle.getSelectedChangeset()==null){
 					Coordinate lC=map.getPosition(1, 1);
 					//corner
 					Coordinate mC=map.getPosition(map.getWidth()-1, map.getHeight()-1);
 					openURL("http://localhost:8111/load_and_zoom?left="+lC.getLon()+"&right="+mC.getLon()+"&top="+lC.getLat()+"&bottom="+mC.getLat());
 				}else{
 					Coordinate []p=map.getChangesetCoordinate(map.drawStyle.getSelectedChangeset().id);					
 					openURL("http://localhost:8111/load_and_zoom?left="+p[0].getLon()+"&right="+p[1].getLon()+"&top="+p[0].getLat()+"&bottom="+p[1].getLat());
 				}
 			}
 		});
 		btnEditInJosm.setBounds(12, 188, 217, 24);
 		panel_3.add(btnEditInJosm);
 		
 		JButton btnAddUser = new JButton("Add to list");
 		btnAddUser.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(map.drawStyle.getSelectedNode()!=null){
 					conf.getSelectedProfile().addUser(dc.getUsers().get(dc.getChangesets().get(dc.getChangesetsIndex().get(map.drawStyle.getSelectedNode().changesetId)).userId));
 				}else if(map.drawStyle.getSelectedChangeset()!=null){
 					conf.getSelectedProfile().addUser(dc.getUsers().get(dc.getChangesets().get(dc.getChangesetsIndex().get(map.drawStyle.getSelectedChangeset().id)).userId));
 				}
 				reloadUsersList();
 				map.refrashOverlay(true);
 			}
 		});
 		
 		btnAddUser.setBounds(109, 90, 120, 14);
 		panel_3.add(btnAddUser);
 		
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
 	
 	private void btSChangeset(){
 		if(map.drawStyle.getSelectedChangeset()==null&&
 				map.drawStyle.getSelectedNode()!=null)
 			btSelectChangeset.setEnabled(true);
 		else{
 			btSelectChangeset.setEnabled(false);
 		}
 	}
 
 	private void initChangeStream() {
 		try {
 			BufferedReader br;
 			br = new BufferedReader(
 					new InputStreamReader(
 							new BufferedInputStream(
 									new URL(
 											conf.getDiffBaseUrl() + "state.txt")
 											.openStream())));
 			br.readLine();
 			String seqNumStr = br.readLine();
 			
 			seqNum = Integer.parseInt(seqNumStr.substring(seqNumStr
 					.indexOf("=") + 1));
 			
 			if(seqNum < firstSeq){
 				firstSeq = seqNum;
 			}
 			
 			br.readLine();
 			br.close();
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 	}
 
 	public void getData() {		
 			getData(seqNum);
 			seqNum++;			
 	}
 	
 	public void getData(int seqNum) {		
 		DecimalFormat myFormat = new DecimalFormat("000");
 		String url = conf.getDiffBaseUrl()
 				+ myFormat.format(seqNum / 1000000) + "/"
 				+ myFormat.format((seqNum / 1000) % 1000) + "/"
 				+ myFormat.format(seqNum % 1000) + ".osc.gz";			
 		getData(url);		
 }
 	
 	public void getData(String url){			
 		try {
 			BufferedInputStream bis;
 			bis = new BufferedInputStream(
 					new GZIPInputStream(new URL(url).openStream()));
 			System.out.println("Download: "+url);
 			dc.addData(bis);		
 		} catch (MalformedURLException e) {
 			System.err.println("");
 		} catch (IOException e) {
 			System.err.println("");
 		}
 	}
 
 
 	public void reloadChangesets(){
 		model.clear();
 		Iterator<Changeset> iterator = dc.getChangesets().iterator();
 	    while (iterator.hasNext()) {
 	    	Changeset ch=iterator.next();
 	    	model.add(0, ch.id+":"+dc.getUsers().get(ch.userId).name);  	
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
 		btUser.setText(dc.getUsers().get(dc.getChangesets().get(dc.getChangesetsIndex().get(node.changesetId)).userId).name);
 		map.drawStyle.setSelection(node);
 		btSChangeset();
 	}
 
 	public void profileChanged(Profile p) {		
 		this.reloadUsersList();	
 		map.refrashOverlay(true);
 	}
 	
 	public void reloadProfiles(){
 		profilesModel.removeAllElements();
 		Profile[] p=conf.getProfiles();
 		Profile s=conf.getSelectedProfile();
 		int tmpInt=0;
 		for(int i=0;i<p.length;i++){
 			if(p[i]==s)
 				tmpInt=i;
 			profilesModel.addElement(p[i].getName());
 		}
 		this.cbProfiles.setSelectedIndex(tmpInt);
 	}
 	
 	public void reloadUserType(){
 		Profile p=conf.getSelectedProfile();
 		//Combo type	
 		usersListType.setSelectedIndex(p.getListType());
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
 			conf.setDividerLocation(splitPane.getDividerLocation());
 			conf.setWindowSize(getSize());
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
 		map.refrashOverlay(true);
 		setBox=false;
 		}
 	}
 	
 	
 	
 	 private static String newStringDialog(JFrame frame,String name,String text) {
 	        JPanel panel = new JPanel();
 	        panel.add(new JLabel(text));
 	        final JTextField fooField = new JTextField(10);
 	        panel.add(fooField);
 	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
 	            public void run() {
 	                fooField.requestFocusInWindow();
 	            }
 	        });
 	        int choice = JOptionPane.showConfirmDialog(frame, panel,
 	                name, JOptionPane.OK_CANCEL_OPTION,
 	                JOptionPane.PLAIN_MESSAGE);
 
 	        switch (choice) {
 	        case JOptionPane.OK_OPTION:
 	            return fooField.getText();
 	        }
 	        return null;
 	    }
 	 private static int findZoom(double lat,double lon){
 		 int zoom=1;
 		 double p=360;
 		 double d=180;
 		 for(;p>lat&&d>lon;zoom++){
 			 p/=2;
 			 d/=2;
 		 }
 		 
 		return zoom; 
 	 }
 }
