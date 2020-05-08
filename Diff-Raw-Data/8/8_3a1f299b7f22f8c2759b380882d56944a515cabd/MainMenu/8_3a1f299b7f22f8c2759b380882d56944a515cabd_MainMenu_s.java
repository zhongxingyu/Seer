 package de.unikassel.ann.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.KeyStroke;
 
 import de.unikassel.ann.controller.ActionJMenuItem;
 import de.unikassel.ann.controller.Actions;
 import de.unikassel.ann.controller.Settings;
 
 public class MainMenu extends JMenuBar {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private JMenu fileMenu;
 	public JMenu subMenuSession;
 	private final ButtonGroup buttonGroup = new ButtonGroup();
 
 	/**
 	 * Constructor
 	 */
 	public MainMenu() {
 		super();
 		fileMenu = new JMenu(Settings.i18n.getString("menu.file"));
 		addMenus();
 	}
 
 	/**
 	 * Add all menus with their items to this menu.
 	 */
 	private void addMenus() {
 		initFileMenu();
 		this.add(fileMenu);
 
 		JMenu mnAnsicht = getAnsichtMenu();
 		this.add(mnAnsicht);
 
 		JMenu mnOptions = getOptionsMenu();
 		this.add(mnOptions);
 
 		JMenu mnTest = getTestMenu();
 		this.add(mnTest);
 
 		// JMenu mnLoggingLevel = getLoggingLevelMenu();
 		// this.add(mnLoggingLevel);
 
 		JMenu mnHilfe = getHilfeMenu();
 		this.add(mnHilfe);
 	}
 
 	/**
 	 * File menu
 	 * 
 	 * @return JMenu
 	 */
 	public void initFileMenu() {
 
 		JMenuItem mntmNeu = new ActionJMenuItem(Settings.i18n.getString("menu.file.new"), Actions.NEW);
 		fileMenu.add(mntmNeu);
 
 		JMenuItem mntmImport = new ActionJMenuItem(Settings.i18n.getString("menu.file.import"), Actions.IMPORT);
 		fileMenu.add(mntmImport);
 
 		JMenuItem mntmExport = new ActionJMenuItem(Settings.i18n.getString("menu.file.export"), Actions.EXPORT);
 		fileMenu.add(mntmExport);
 
 		fileMenu.addSeparator();
 
 		// hier is the Position, if sessions exists
 		// addItemToMenu()
 		subMenuSession = new JMenu(Settings.i18n.getString("menu.file.submenu.sessions"));
 		fileMenu.add(subMenuSession);
 
 		fileMenu.addSeparator();
 
 		JMenuItem mntmCloseCurrentSession = new ActionJMenuItem(Settings.i18n.getString("menu.file.closeCurrentsession"),
 				Actions.CLOSE_CURRENT_SESSION);
 		fileMenu.add(mntmCloseCurrentSession, -1);
 
 		JMenuItem mntmBeenden = new ActionJMenuItem(Settings.i18n.getString("menu.file.exit"), Actions.EXIT);
 		mntmBeenden.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
 		fileMenu.add(mntmBeenden, -1);
 	}
 
 	/**
 	 * View menu
 	 * 
 	 * @return JMenu
 	 */
 	private JMenu getAnsichtMenu() {
 		JMenu mnAnsicht = new JMenu(Settings.i18n.getString("menu.view"));
 
 		JRadioButtonMenuItem rdbtnmntmKonsoleAnzeigen = new JRadioButtonMenuItem("Konsole anzeigen");
 		rdbtnmntmKonsoleAnzeigen.setSelected(true);
 		buttonGroup.add(rdbtnmntmKonsoleAnzeigen);
 		mnAnsicht.add(rdbtnmntmKonsoleAnzeigen);
 
 		JRadioButtonMenuItem radioTrainDataChart = new JRadioButtonMenuItem("Trainingsdaten anzeigen");
 		buttonGroup.add(radioTrainDataChart);
 		mnAnsicht.add(radioTrainDataChart);
 
 		JRadioButtonMenuItem rdbtnmntmTrainingsfehlerAnzeigen = new JRadioButtonMenuItem("Trainingsfehler anzeigen");
		rdbtnmntmTrainingsfehlerAnzeigen.setEnabled(false);
 		buttonGroup.add(rdbtnmntmTrainingsfehlerAnzeigen);
 		mnAnsicht.add(rdbtnmntmTrainingsfehlerAnzeigen);
 
 		JSeparator separator = new JSeparator();
 		mnAnsicht.add(separator);
 
 		JMenuItem mntmTrainingchartLeeren = new JMenuItem("Konsole leeren");
 		mnAnsicht.add(mntmTrainingchartLeeren);
 
 		rdbtnmntmKonsoleAnzeigen.addActionListener(new ActionJMenuItem("switch console", Actions.SWITCH_CONSOLE));
 		rdbtnmntmTrainingsfehlerAnzeigen.addActionListener(new ActionJMenuItem("switch train error", Actions.SWITCH_TRAINERROR));
 		radioTrainDataChart.addActionListener(new ActionJMenuItem("switch train data", Actions.SWITCH_TRAINDATA));
 		radioTrainDataChart.addActionListener(new ActionJMenuItem("clear console", Actions.CLEAR_CONSOLE));
 
 		return mnAnsicht;
 	}
 
 	/**
 	 * Options Menu
 	 * 
 	 * @return JMenu
 	 */
 
 	private JMenu getOptionsMenu() {
 		JMenu mnOptions = new JMenu(Settings.i18n.getString("menu.options"));
 
 		JMenuItem mntmSOM = new ActionJMenuItem(Settings.i18n.getString("menu.options.SOM"), Actions.SOM_VIEW);
 		mnOptions.add(mntmSOM);
 
 		mnOptions.addSeparator();
 
 		JMenuItem mntmTrainData = new ActionJMenuItem(Settings.i18n.getString("menu.options.trainData"), Actions.NORMALIZE_TRAIN_DATA);
 		mnOptions.add(mntmTrainData);
 
 		JMenuItem mntmCreateTrainData = new ActionJMenuItem(Settings.i18n.getString("menu.options.createTrainData"),
 				Actions.CREATE_TRAIN_DATA);
 		mnOptions.add(mntmCreateTrainData);
 
 		return mnOptions;
 	}
 
 	/**
 	 * Test menu
 	 * 
 	 * @return JMenu
 	 */
 	private JMenu getTestMenu() {
 		JMenu mntmNetzwerk = new JMenu(Settings.i18n.getString("menu.network"));
 
 		JMenuItem mntnORNetwork = new ActionJMenuItem(Settings.i18n.getString("menu.network.or"), Actions.LOAD_OR_NETWORK);
 		mntmNetzwerk.add(mntnORNetwork);
 
 		JMenuItem mntnXORNetwork = new ActionJMenuItem(Settings.i18n.getString("menu.network.xor"), Actions.LOAD_XOR_NETWORK);
 		mntmNetzwerk.add(mntnXORNetwork);
 
 		JMenuItem mntnANDNetwork = new ActionJMenuItem(Settings.i18n.getString("menu.network.and"), Actions.LOAD_AND_NETWORK);
 		mntmNetzwerk.add(mntnANDNetwork);
 
 		JMenuItem mntn2BitAddiererNetwork = new ActionJMenuItem(Settings.i18n.getString("menu.network.2bitaddierer"),
 				Actions.LOAD_2_BIT_ADDIERER_NETWORK);
 		mntmNetzwerk.add(mntn2BitAddiererNetwork);
 
 		return mntmNetzwerk;
 	}
 
 	/**
 	 * @return JMenu Logging-Level
 	 */
 	private JMenu getLoggingLevelMenu() {
 		JMenu mnLoggingLevel = new JMenu(Settings.i18n.getString("menu.loggingLevel"));
 
 		JMenuItem mntmDebug = new ActionJMenuItem(Settings.i18n.getString("menu.loggingLevel.debug"), Actions.DEBUG_LOGGING_LEVEL);
 		mnLoggingLevel.add(mntmDebug);
 
 		JMenuItem mntmInfo = new ActionJMenuItem(Settings.i18n.getString("menu.loggingLevel.info"), Actions.INFO_LOGGING_LEVEL);
 		mnLoggingLevel.add(mntmInfo);
 
 		JMenuItem mntmWarn = new ActionJMenuItem(Settings.i18n.getString("menu.loggingLevel.warn"), Actions.WARN_LOGGING_LEVEL);
 		mnLoggingLevel.add(mntmWarn);
 
 		return mnLoggingLevel;
 	}
 
 	/**
 	 * Help menu
 	 * 
 	 * @return JMenu
 	 */
 	private JMenu getHilfeMenu() {
 		JMenu mnHilfe = new JMenu(Settings.i18n.getString("menu.help"));
 
 		JMenuItem mntmUeber = new ActionJMenuItem(Settings.i18n.getString("menu.help.about"), Actions.ABOUT);
 		mnHilfe.add(mntmUeber);
 
 		// JMenuItem mntmFunktionUebersicht = new ActionJMenuItem("Funktionen", Actions.jANN_FUNCTION_OVERVIEW);
 		// mnHilfe.add(mntmFunktionUebersicht); // Settings.i18n.getString("menu.help.functions")
 
 		return mnHilfe;
 	}
 }
