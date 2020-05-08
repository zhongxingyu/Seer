 package admin;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JTabbedPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import admin.contestanttab.ContestantPanel;
 import admin.data.GameData;
 import admin.playertab.PlayerPanel;
 
 public class MainFrame extends JFrame{
 
 	static MainFrame m;
 	
 
 	private JTabbedPane tabPane = new JTabbedPane();
 	
 	private JLabel lblGeneral = new JLabel(GENERAL_PANEL);
 	private JLabel lblContestants = new JLabel(CONTESTANT_PANEL);
 	private JLabel lblPlayers = new JLabel(PLAYER_PANEL);
 	
 	private JMenuBar menuBar = new JMenuBar();
 	private JMenu mnuFile = new JMenu("File");
 	private JMenu mnuTheme = new JMenu("Theme");
 	
 	private JMenuItem mnuItemReset;
 	private JMenuItem mnuItemExit;
 	private JRadioButtonMenuItem mnuItemTheme1;
 	private JRadioButtonMenuItem mnuItemTheme2;
 	private JRadioButtonMenuItem mnuItemTheme3;
 	
 	public static final String GENERAL_PANEL 		= "General",
 							   CONTESTANT_PANEL 	= "Contestants",
 							   PLAYER_PANEL 		= "Players";
 	
 	private StatusPanel statusBar;
 
 	private ContestantPanel conPanel;
 	private PlayerPanel playerPanel;
 	
 	ActionListener al = new ActionListener() {
 		@Override
 		public void actionPerformed(ActionEvent ae) {
 			if (ae.getSource() == mnuItemExit) {
 				System.exit(0);
 			}else if (ae.getSource() == mnuItemReset){
 				resetSeason();
 			}else if (ae.getSource() == mnuItemTheme1) {
 				changeTheme(ae.getActionCommand());
 			}else if (ae.getSource() == mnuItemTheme3) {
 				changeTheme(ae.getActionCommand());
 			}else if (ae.getSource() == mnuItemTheme2) {
 				changeTheme(ae.getActionCommand());
 			}
 		}
 	};
 	
 	ChangeListener cl = new ChangeListener() {
 		public void stateChanged(ChangeEvent ce) {
 			JTabbedPane tabSource = (JTabbedPane) ce.getSource();
 		    String tab = tabSource.getTitleAt(tabSource.getSelectedIndex());
 		    statusBar.setTabLabel(tab);
 		}
 	};
 	
 	public MainFrame(){
 		
 		GameData g = GameData.initGameData();
 		if(g!=null)
 			initGUI();
 		else
 			initSeasonCreateGUI();
 		
 		applyTheme();
 		this.setSize(640, 480);
 		this.setVisible(true);
 		this.setTitle("Survivor Pool Admin");
 		this.addWindowListener(new WindowAdapter(){
 			public void windowClosing(WindowEvent we) {
 				if(GameData.getCurrentGame()!=null)
 					GameData.getCurrentGame().writeData();
 			    System.exit(0);
 			}
 		});
 	}
 	
 	private void initSeasonCreateGUI(){
 		this.setLayout(new BorderLayout());
 		statusBar = new StatusPanel();
 		this.add(new SeasonCreatePanel(),BorderLayout.CENTER);
 		this.add(statusBar, BorderLayout.SOUTH);
 		statusBar.setTabLabel("SEASON CREATE");
 	}
 	
 	private void initGUI(){		
 		Dimension d = new Dimension(150,20);
 		
 		lblGeneral.setPreferredSize(d);
 		lblContestants.setPreferredSize(d);
 		lblPlayers.setPreferredSize(d);
 		
 		conPanel = new ContestantPanel();
 		playerPanel = new PlayerPanel();
 		tabPane.addTab(lblGeneral.getText(),new GeneralPanel());
 		tabPane.addTab(lblContestants.getText(),conPanel);
 		tabPane.addTab(lblPlayers.getText(), playerPanel);
 		
 		tabPane.addChangeListener(playerPanel);
 		
 		tabPane.setTabComponentAt(0, lblGeneral);
 		tabPane.setTabComponentAt(1, lblContestants);
 		tabPane.setTabComponentAt(2, lblPlayers);
 		//tabPane.setBackground(Color.cyan);//tab background color,not the panel
 		
 		mnuItemReset = new JMenuItem("Reset");
 		mnuItemExit = new JMenuItem("Exit");
 		String[] themeName = AdminUtils.getThemes();
 		mnuItemTheme1 = new JRadioButtonMenuItem(themeName[0]);
 		mnuItemTheme3 = new JRadioButtonMenuItem(themeName[1]);
 		mnuItemTheme2 = new JRadioButtonMenuItem(themeName[2]);
 		
 		ButtonGroup g = new ButtonGroup();
 		mnuTheme.add(mnuItemTheme1);
 		g.add(mnuItemTheme1);
 		mnuTheme.add(mnuItemTheme3);
 		g.add(mnuItemTheme3);
 		mnuTheme.add(mnuItemTheme2);
 		g.add(mnuItemTheme2);
 		
 		mnuFile.add(mnuItemReset);
 		mnuFile.add(mnuItemExit);
 		
 		menuBar.add(mnuFile);
 		menuBar.add(mnuTheme);
 		
 		statusBar = new StatusPanel();
 		
 		mnuItemReset.addActionListener(al);
 		mnuItemExit.addActionListener(al);
 		mnuItemTheme1.addActionListener(al);
 		mnuItemTheme3.addActionListener(al);
 		mnuItemTheme2.addActionListener(al);
 		tabPane.addChangeListener(cl);
 		statusBar.setTabLabel(GENERAL_PANEL);
 		
 		this.setLayout(new BorderLayout());
 		
 		this.setJMenuBar(menuBar);
 		this.add(tabPane);
 		this.add(statusBar, BorderLayout.SOUTH);
		if(GameData.getCurrentGame().getSeasonStarted())
 			seasonStarted();
 	}
 	
 	/**
 	 * Apply the theme to current components.
 	 */
 	private void applyTheme(){
 		AdminUtils.style(this);
 	}
 	
 	/**
 	 * Change the current theme.
 	 * @param name The theme name
 	 */
 	private void changeTheme(String name){
 		AdminUtils.changeTheme(name);
 		applyTheme();
 	}
 	
 	// TODO: Less ambiguous name?
 	/**
 	 * Called by SeasonCreatePanel when a new season has been created/
 	 */
 	public static void seasonCreated(){
 		GameData.initGameData();
 		m.getContentPane().removeAll();
 		m.initGUI();
 		m.applyTheme();
 	}
 	
 	/**
 	 * Used when a season has been started(from gen panel)
 	 * 
 	 */
 	public void seasonStarted(){
 		//TODO: change enabled status of components
 		conPanel.seasonStarted();
 		//TODO: add season started method for users
 	}
 	
 	private void resetSeason() {
 		int response = JOptionPane.showConfirmDialog(null,
 							"Would you like to delete current season?","Reset Season",
 									JOptionPane.YES_NO_OPTION);
 		if(response == JOptionPane.YES_OPTION){
 			GameData.getCurrentGame().endCurrentGame();
 			m.dispose();
 			m = new MainFrame();
 		}
 	}
 	
 	public StatusPanel getStatusBar() {
 		return statusBar;
 	}
 	
 	/**
 	 * Used to get reference to the running GUI.
 	 * @return Gets the running MainFrame.
 	 */
 	public static MainFrame getRunningFrame() {
 		return m;
 	}
 	
 	public static void main(String[] args) {
 		m = new MainFrame();
 	}
 
 }
