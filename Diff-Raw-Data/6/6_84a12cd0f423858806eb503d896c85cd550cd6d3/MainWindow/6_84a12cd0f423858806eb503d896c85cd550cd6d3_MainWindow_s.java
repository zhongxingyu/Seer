 /*
  cursus - Race series management program
  Copyright 2011  Simon Arlott
 
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package eu.lp0.cursus.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.Executor;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 
 import eu.lp0.cursus.app.Main;
 import eu.lp0.cursus.db.Database;
 import eu.lp0.cursus.db.InvalidDatabaseException;
 import eu.lp0.cursus.db.data.Event;
 import eu.lp0.cursus.db.data.Race;
 import eu.lp0.cursus.db.data.RaceHierarchy;
 import eu.lp0.cursus.db.data.Series;
 import eu.lp0.cursus.ui.component.AbstractDatabaseTab;
 import eu.lp0.cursus.ui.component.DatabaseWindow;
 import eu.lp0.cursus.ui.component.Displayable;
 import eu.lp0.cursus.ui.event.EventPenaltiesTab;
 import eu.lp0.cursus.ui.event.EventResultsTab;
 import eu.lp0.cursus.ui.preferences.JFrameAutoPrefs;
 import eu.lp0.cursus.ui.race.RaceAttendeesTab;
 import eu.lp0.cursus.ui.race.RaceLapsTab;
 import eu.lp0.cursus.ui.race.RacePenaltiesTab;
 import eu.lp0.cursus.ui.race.RaceResultsTab;
 import eu.lp0.cursus.ui.series.SeriesClassesTab;
 import eu.lp0.cursus.ui.series.SeriesPenaltiesTab;
 import eu.lp0.cursus.ui.series.SeriesPilotsTab;
 import eu.lp0.cursus.ui.series.SeriesResultsTab;
 import eu.lp0.cursus.ui.tree.RaceTree;
 import eu.lp0.cursus.util.Constants;
 import eu.lp0.cursus.util.Messages;
 
 public class MainWindow extends JFrame implements Executor, Displayable, DatabaseWindow {
 	/**
 	 * Primary background execution thread used to serialises all actions
 	 */
 	private final ExecutorService background = Executors.newSingleThreadExecutor();
 	private final Main main;
 
 	private JFrameAutoPrefs prefs = new JFrameAutoPrefs(this);
 	private DatabaseManager dbMgr = new DatabaseManager(this);
 	private TabbedPaneManager tabMgr;
 	private SelectedTabManager selMgr;
 
 	// Main
 	private JSplitPane splitPane;
 	private RaceTree<MainWindow> raceList;
 	private JTabbedPane tabbedPane;
 	private AbstractDatabaseTab<Series> serPilotsTab;
 	private AbstractDatabaseTab<Series> serClassesTab;
 	private AbstractDatabaseTab<Series> serPenaltiesTab;
 	private AbstractDatabaseTab<Series> serResultsTab;
 	private List<AbstractDatabaseTab<Series>> seriesTabs;
 	private AbstractDatabaseTab<Event> evtPenaltiesTab;
 	private AbstractDatabaseTab<Event> evtResultsTab;
 	private List<AbstractDatabaseTab<Event>> eventTabs;
 	private AbstractDatabaseTab<Race> racAttendeesTab;
 	private AbstractDatabaseTab<Race> racLapsTab;
 	private AbstractDatabaseTab<Race> racPenaltiesTab;
 	private AbstractDatabaseTab<Race> racResultsTab;
 	private List<AbstractDatabaseTab<Race>> raceTabs;
 
 	// Menu
 	private JMenuBar menuBar;
 	private JMenu mnuFile;
 	private JMenuItem mnuFileNew;
 	private JMenuItem mnuFileOpen;
 	private JMenuItem mnuFileSave;
 	private JMenuItem mnuFileSaveAs;
 	private JMenuItem mnuFileClose;
 	private JSeparator mnuFileSeparator1;
 	private JMenuItem mnuFileExit;
 	private JMenu mnuHelp;
 	private JMenuItem mnuHelpAbout;
 
 	public MainWindow(Main main, final String[] args) {
 		super();
 		this.main = main;
 
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowOpened(WindowEvent we) {
 				execute(new Runnable() {
 					@Override
 					public void run() {
 						try {
 							MainWindow.this.startup(args);
 						} catch (InvalidDatabaseException e) {
 							// TODO handle uncaught exceptions
 							throw new RuntimeException(e);
 						}
 					}
 				});
 			}
 
 			@Override
 			public void windowClosing(WindowEvent we) {
 				execute(new Runnable() {
 					@Override
 					public void run() {
 						shutdown();
 					}
 				});
 			}
 		});
 
 		initialise();
 		bind();
 
		databaseClosed();
 		enableStartupGUI(false);
 	}
 
 	public void display() {
 		prefs.display();
 	}
 
 	public Main getMain() {
 		return main;
 	}
 
 	public boolean isOpen() {
 		return main.isOpen();
 	}
 
 	public Database getDatabase() {
 		return main.getDatabase();
 	}
 
 	public RaceHierarchy getSelected() {
 		return isOpen() ? tabMgr.getSelected() : null;
 	}
 
 	@Override
 	public void execute(Runnable command) {
 		background.execute(command);
 	}
 
 	private void startup(String[] args) throws InvalidDatabaseException {
 		try {
 			if (args.length == 0) {
 				dbMgr.newDatabase();
 			} else if (args.length == 1) {
 				// TODO open file
 			} else {
 				// TODO error message
 			}
 		} finally {
 			enableStartupGUI(true);
 		}
 	}
 
 	private void shutdown() {
 		if (dbMgr.trySaveDatabase(Messages.getString("menu.file.exit"))) { //$NON-NLS-1$
 			try {
 				dispose();
 			} finally {
 				background.shutdownNow();
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void initialise() {
 		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		setTitle(Constants.APP_NAME);
 		setSize(800, 600);
 
 		{ // Main
 			splitPane = new JSplitPane();
 			getContentPane().add(splitPane, BorderLayout.CENTER);
 
 			raceList = new RaceTree<MainWindow>(this);
 			splitPane.setLeftComponent(raceList);
 			raceList.setMinimumSize(new Dimension(150, 0));
 
 			tabbedPane = new JTabbedPane();
 			splitPane.setRightComponent(tabbedPane);
 		}
 
 		{ // Series
 			serClassesTab = new SeriesClassesTab(this);
 			serPilotsTab = new SeriesPilotsTab(this);
 			serPenaltiesTab = new SeriesPenaltiesTab(this);
 			serResultsTab = new SeriesResultsTab(this);
 			seriesTabs = Arrays.asList(serPilotsTab, serClassesTab, serPenaltiesTab, serResultsTab);
 		}
 
 		{ // Event
 			evtPenaltiesTab = new EventPenaltiesTab(this);
 			evtResultsTab = new EventResultsTab(this);
 
 			eventTabs = Arrays.asList(evtPenaltiesTab, evtResultsTab);
 		}
 
 		{ // Race
 			racAttendeesTab = new RaceAttendeesTab(this);
 			racLapsTab = new RaceLapsTab(this);
 			racPenaltiesTab = new RacePenaltiesTab(this);
 			racResultsTab = new RaceResultsTab(this);
 
 			raceTabs = Arrays.asList(racAttendeesTab, racLapsTab, racPenaltiesTab, racResultsTab);
 		}
 
 		{ // Menu
 			menuBar = new JMenuBar();
 			setJMenuBar(menuBar);
 
 			mnuFile = new JMenu();
 			menuBar.add(mnuFile);
 			mnuFile.setText(Messages.getString("menu.file")); //$NON-NLS-1$
 			mnuFile.setMnemonic(Messages.getKeyEvent("menu.file")); //$NON-NLS-1$
 
 			mnuFileNew = new JMenuItem();
 			mnuFile.add(mnuFileNew);
 			mnuFileNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
 			mnuFileNew.setText(Messages.getString("menu.file.new")); //$NON-NLS-1$
 			mnuFileNew.setMnemonic(Messages.getKeyEvent("menu.file.new")); //$NON-NLS-1$
 			mnuFileNew.setActionCommand(DatabaseManager.Commands.NEW.toString());
 			mnuFileNew.addActionListener(dbMgr);
 
 			mnuFileOpen = new JMenuItem();
 			mnuFile.add(mnuFileOpen);
 			mnuFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
 			mnuFileOpen.setText(Messages.getString("menu.file.open")); //$NON-NLS-1$
 			mnuFileOpen.setMnemonic(Messages.getKeyEvent("menu.file.open")); //$NON-NLS-1$
 			mnuFileOpen.setActionCommand(DatabaseManager.Commands.OPEN.toString());
 			mnuFileOpen.addActionListener(dbMgr);
 
 			mnuFileSave = new JMenuItem();
 			mnuFile.add(mnuFileSave);
 			mnuFileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
 			mnuFileSave.setText(Messages.getString("menu.file.save")); //$NON-NLS-1$
 			mnuFileSave.setMnemonic(Messages.getKeyEvent("menu.file.save")); //$NON-NLS-1$
 			mnuFileSave.setActionCommand(DatabaseManager.Commands.SAVE.toString());
 			mnuFileSave.addActionListener(dbMgr);
 
 			mnuFileSaveAs = new JMenuItem();
 			mnuFile.add(mnuFileSaveAs);
 			mnuFileSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
 			mnuFileSaveAs.setText(Messages.getString("menu.file.save-as")); //$NON-NLS-1$
 			mnuFileSaveAs.setMnemonic(Messages.getKeyEvent("menu.file.save-as")); //$NON-NLS-1$
 			mnuFileSaveAs.setActionCommand(DatabaseManager.Commands.SAVE_AS.toString());
 			mnuFileSaveAs.addActionListener(dbMgr);
 
 			mnuFileClose = new JMenuItem();
 			mnuFile.add(mnuFileClose);
 			mnuFileClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));
 			mnuFileClose.setText(Messages.getString("menu.file.close")); //$NON-NLS-1$
 			mnuFileClose.setMnemonic(Messages.getKeyEvent("menu.file.close")); //$NON-NLS-1$
 			mnuFileClose.setActionCommand(DatabaseManager.Commands.CLOSE.toString());
 			mnuFileClose.addActionListener(dbMgr);
 
 			mnuFileSeparator1 = new JSeparator();
 			mnuFile.add(mnuFileSeparator1);
 
 			mnuFileExit = new JMenuItem();
 			mnuFileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
 			mnuFileExit.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent ae) {
 					WindowEvent wev = new WindowEvent(MainWindow.this, WindowEvent.WINDOW_CLOSING);
 					Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
 				}
 			});
 			mnuFile.add(mnuFileExit);
 			mnuFileExit.setText(Messages.getString("menu.file.exit")); //$NON-NLS-1$
 			mnuFileExit.setMnemonic(Messages.getKeyEvent("menu.file.exit")); //$NON-NLS-1$
 
 			mnuHelp = new JMenu();
 			menuBar.add(mnuHelp);
 			mnuHelp.setText(Messages.getString("menu.help")); //$NON-NLS-1$
 			mnuHelp.setMnemonic(Messages.getKeyEvent("menu.help")); //$NON-NLS-1$
 
 			mnuHelpAbout = new JMenuItem();
 			mnuHelp.add(mnuHelpAbout);
 			mnuHelpAbout.setText(Messages.getString("menu.help.about")); //$NON-NLS-1$
 			mnuHelpAbout.setMnemonic(Messages.getKeyEvent("menu.help.about")); //$NON-NLS-1$
 		}
 	}
 
 	private void bind() {
 		tabMgr = new TabbedPaneManager(raceList, tabbedPane, seriesTabs, eventTabs, raceTabs);
 		selMgr = new SelectedTabManager(this, tabbedPane);
 	}
 
 	private void enableStartupGUI(boolean enabled) {
 		mnuFileNew.setEnabled(enabled);
 		mnuFileOpen.setEnabled(enabled);
 	}
 
 	private void sync(final boolean open, final String title) {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
				tabbedPane.setVisible(open);
 				enableStartupGUI(true);
 				mnuFileSave.setEnabled(open);
 				mnuFileSaveAs.setEnabled(open);
 				mnuFileClose.setEnabled(open);
 				getRootPane().validate();
 
 				setTitle(title);
 			}
 		});
 
 		if (open) {
 			raceList.databaseRefresh();
 			selMgr.databaseRefresh();
 		} else {
 			raceList.databaseClosed();
 			selMgr.databaseClosed();
 		}
 	}
 
 	public void databaseOpened() {
 		sync(true, Constants.APP_NAME + Constants.EN_DASH + main.getDatabase().getName());
 	}
 
 	public void databaseClosed() {
 		tabMgr.showSelected(null);
 		sync(false, Constants.APP_DESC);
 	}
 }
