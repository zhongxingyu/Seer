 /*
  * $Header: $
  */
 
 package soccerscope;
 
 import java.awt.Container;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Toolkit;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.prefs.Preferences;
 
 import javax.swing.JFrame;
 
 import soccerscope.file.LogFileReader;
 import soccerscope.model.SceneSet;
 import soccerscope.model.SceneSetMaker;
 import soccerscope.model.SoccerScopePreferences;
 import soccerscope.model.WorldModel;
 import soccerscope.net.SoccerServerConnection;
 import soccerscope.util.ButtonManager;
 import soccerscope.view.InformationWindow;
 import soccerscope.view.OptionWindow;
 import soccerscope.view.ScenePlayer;
 import soccerscope.view.ScopePane;
 import soccerscope.view.SelectWindow;
 import soccerscope.view.StatisticsWindow;
 import soccerscope.view.StatusBar;
 
 public class SoccerScope extends JFrame {
 
 	public static void main(String args[]) {
 		// check if we should run in batch mode.
 		if (args.length > 0) {
 			if (args[0].equals("--batch") && args.length == 3) {
 				String filename = args[1];
				String xmlFilename = args[2];
 				try {
 					NonGUISoccerScope.run(filename, xmlFilename);
 				} catch (Exception e) {
 					System.err.println("batch mode failed...");
 					e.printStackTrace();
 					System.exit(1);
 				}
 				return;
 			} else {
 				System.out.format("invalid option %s or wrong"
 						+ " number of arguments (0 or 2)\n", args[0]);
				System.out.println("usage [--batch input_file output_file]"
 						+ "\nif you just want to use the GUI,"
 						+ " don't use any parameters.");
 				System.out.println("defaulting to GUI anyway.");
 			}
 		}
 		// the default stuff with GUI...
 		new SoccerScope(args);
 	}
 
 	public final static String title = "SoccerScope";
 
 	Container container = null;
 
 	private File chooseFile = null;
 	private int maxX = Toolkit.getDefaultToolkit().getScreenSize().width;
 	private int maxY = Toolkit.getDefaultToolkit().getScreenSize().height;
 
 	// ��������³
 	private SoccerServerConnection ssConnection = null;
 
 	// main window component
 	private SoccerScopeMenuBar menuBar = null;
 	private SoccerScopeToolBar toolBar = null;
 	private ScopePane scopePane = null;
 	private StatusBar statusBar = null;
 	private ScenePlayer scenePlayer = null;
 
 	// �ץ쥤�䡼���� window
 	private SelectWindow selectWindow = null;
 
 	// information window
 	private InformationWindow infoWindow = null;
 
 	// Statistics window
 	private StatisticsWindow statisticsWindow = null;
 
 	// ���ץ���� window
 	private OptionWindow optionWindow = null;
 
 	public SoccerScope(String[] args) {
 		// frame title
 		super("SoccerScope");
 
 		// initialize the world model
 		WorldModel.getInstance();
 
 		// get content pane
 		container = this.getContentPane();
 		container.setLayout(new GridBagLayout());
 
 		// init mainpanel (ScopePane, fieldPane, infoBar)
 		initMainPanel();
 
 		// init MenuBar (menuBar)
 		initMenuBar();
 
 		// init statusBar (statusBar)
 		initStatusBar();
 
 		// init ToolBar (toolBar, scenePlayer)
 		initToolBar();
 
 		// init all Button's state
 		initButtonState();
 
 		// ScenePlayer ��ScopePane�˴�Ϣ�Ť���
 		scopePane.setScenePlayer(scenePlayer);
 
 		// WindowListener
 		addWindowListener(new WindowEventHander());
 
 		// realize
 		pack();
 		setVisible(true);
 
 		// appear center of screen
 		setLocationRelativeTo(null);
 	}
 
 	private void initMainPanel() {
 		scopePane = new ScopePane(this);
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.gridx = 0;
 		gbc.gridy = 2;
 		gbc.gridwidth = 1;
 		gbc.gridheight = 1;
 		gbc.weightx = 1.0;
 		gbc.weighty = 1.0;
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.anchor = GridBagConstraints.NORTH;
 		container.add(scopePane, gbc);
 	}
 
 	private void initMenuBar() {
 		menuBar = new SoccerScopeMenuBar(this);
 		setJMenuBar(menuBar);
 	}
 
 	private void initToolBar() {
 		toolBar = new SoccerScopeToolBar(this);
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.gridx = 0;
 		gbc.gridy = 0;
 		gbc.gridwidth = 1;
 		gbc.gridheight = 1;
 		gbc.weightx = 1.0;
 		gbc.weighty = 0.0;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.anchor = GridBagConstraints.SOUTHWEST;
 		container.add(toolBar, gbc);
 
 		// �ġ���С�����2
 		scenePlayer = new ScenePlayer(scopePane, statusBar, true);
 		scenePlayer.setFloatable(false);
 		gbc.gridx = 0;
 		gbc.gridy = 1;
 		gbc.gridwidth = 1;
 		gbc.gridheight = 1;
 		gbc.weightx = 1.0;
 		gbc.weighty = 0.0;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.anchor = GridBagConstraints.SOUTHWEST;
 		container.add(scenePlayer, gbc);
 	}
 
 	private void initStatusBar() {
 		statusBar = new StatusBar(scopePane);
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.gridx = 0;
 		gbc.gridy = 3;
 		gbc.gridwidth = 1;
 		gbc.gridheight = 1;
 		gbc.weightx = 1.0;
 		gbc.weighty = 0.0;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.anchor = GridBagConstraints.SOUTHWEST;
 		container.add(statusBar, gbc);
 	}
 
 	private void initButtonState() {
 		ButtonManager.setEnabled("logfile", false);
 		ButtonManager.setEnabled("AgentWorldModel", false);
 		ButtonManager.setEnabled("AgentLog", false);
 		ButtonManager.setEnabled("network", false);
 	}
 
 	public void openLogFile(SceneSet sceneSet, String filename) {
 		try {
 			LogFileReader lfr = new LogFileReader(filename);
 			SceneSetMaker ssm = new SceneSetMaker(lfr, sceneSet, statusBar
 					.getProgressBar());
 			ssm.start();
 		} catch (IOException ie) {
 			System.err.println(ie);
 		}
 	}
 
 	class WindowEventHander extends WindowAdapter {
 		public void windowClosing(WindowEvent evt) {
 			JFrame f = (JFrame) evt.getSource();
 			if (f.getTitle().startsWith(SoccerScope.title))
 				System.exit(0);
 			else
 				ButtonManager.setSelected(f.getTitle(), false);
 		}
 	}
 
 	public SoccerScopeMenuBar getSoccerScopeMenuBar() {
 		return menuBar;
 	}
 
 	public SoccerScopeToolBar getSoccerScopeToolBar() {
 		return toolBar;
 	}
 
 	public ScopePane getScopePane() {
 		return scopePane;
 	}
 
 	public ScenePlayer getScenePlayer() {
 		return scenePlayer;
 	}
 
 	public SelectWindow getSelectWindow() {
 		if (selectWindow == null) {
 			selectWindow = new SelectWindow();
 			selectWindow.addWindowListener(new WindowEventHander());
 			scenePlayer.addScopeWindow(selectWindow);
 			// ScopePane������������
 			scopePane.addSoccerObjectSelectObserver(selectWindow);
 
 			// SelectWindow������������
 			selectWindow.addSoccerObjectSelectObserver(scopePane);
 			if (infoWindow != null)
 				selectWindow.addSoccerObjectSelectObserver(infoWindow);
 			// �и����֤�����
 			selectWindow.setLocation(Math.min(maxX - selectWindow.getWidth(),
 					getLocationOnScreen().x + getWidth()), Math.min(maxY
 					- selectWindow.getHeight(), getLocationOnScreen().y
 					+ getHeight() - selectWindow.getHeight()));
 		}
 		return selectWindow;
 	}
 
 	public void setSelectWindow(SelectWindow sw) {
 		selectWindow = sw;
 	}
 
 	public InformationWindow getInformationWindow() {
 		if (infoWindow == null) {
 			infoWindow = new InformationWindow();
 			infoWindow.addWindowListener(new WindowEventHander());
 			scenePlayer.addScopeWindow(infoWindow);
 			// ScopePane������������
 			scopePane.addSoccerObjectSelectObserver(infoWindow);
 
 			// SelectWindow������������
 			if (selectWindow != null)
 				selectWindow.addSoccerObjectSelectObserver(infoWindow);
 			// �и����֤�����
 			infoWindow.setLocation(Math.min(maxX - infoWindow.getWidth(),
 					getLocationOnScreen().x + getWidth()
 							- infoWindow.getWidth()), Math.min(maxY
 					- infoWindow.getHeight(), getLocationOnScreen().y
 					+ getHeight()));
 		}
 		return infoWindow;
 	}
 
 	public void setInformationWindow(InformationWindow sw) {
 		infoWindow = sw;
 	}
 
 	public StatisticsWindow getStatisticsWindow() {
 		if (statisticsWindow == null) {
 			statisticsWindow = new StatisticsWindow();
 			statisticsWindow.addWindowListener(new WindowEventHander());
 			// �и����֤�����
 			statisticsWindow.setLocation(Math.min(maxX
 					- statisticsWindow.getWidth(), getLocationOnScreen().x),
 					Math.min(maxY - statisticsWindow.getHeight(),
 							getLocationOnScreen().y + getHeight()));
 		}
 		return statisticsWindow;
 	}
 
 	public void setStatisticsWindow(StatisticsWindow sw) {
 		statisticsWindow = sw;
 	}
 
 	public OptionWindow getOptionWindow() {
 		if (optionWindow == null) {
 			optionWindow = new OptionWindow(scopePane);
 			optionWindow.addWindowListener(new WindowEventHander());
 			scenePlayer.addScopeWindow(optionWindow);
 			optionWindow.setLocation(Math.min(maxX - optionWindow.getWidth(),
 					getLocationOnScreen().x + getWidth()), Math.min(maxY
 					- optionWindow.getHeight(), getLocationOnScreen().y));
 		}
 		return optionWindow;
 	}
 
 	public void setOptionWindow(OptionWindow sw) {
 		optionWindow = sw;
 	}
 
 	public SoccerServerConnection getSoccerServerConnection() {
 		return ssConnection;
 	}
 
 	public void setSoccerServerConnection(SoccerServerConnection ssc) {
 		ssConnection = ssc;
 	}
 
 	public File getChooseFile() {
 		if (chooseFile == null) {
 			Preferences pf = SoccerScopePreferences.getPreferences();
 			chooseFile = new File(pf.get("lastfile", System
 					.getProperty("user.dir")));
 		}
 		return chooseFile;
 	}
 
 	public void setChooseFile(File f) {
 		chooseFile = f;
 		Preferences pf = SoccerScopePreferences.getPreferences();
 		pf.put("lastfile", f.getPath());
 	}
 
 	public String getDataSourceName() {
 		if (ssConnection != null)
 			return ssConnection.getHostName() + ":" + ssConnection.getPort();
 		if (chooseFile != null)
 			return chooseFile.getName();
 		return "scene";
 	}
 }
