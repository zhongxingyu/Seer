 package ms.client.ui;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.HashMap;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.table.TableModel;
 
 import ms.client.Client;
 import ms.client.logic.TaskList;
 import ms.client.logic.TaskRunningList;
 import ms.client.ui.dialogs.AboutDialog;
 import ms.client.ui.dialogs.ConfigDialog;
 import ms.client.ui.dialogs.LogDialog;
 import ms.client.ui.dialogs.MessageDialog;
 import ms.client.ui.models.TaskComboBoxModel;
 import ms.client.ui.models.TaskTableModel;
 import ms.client.ui.tables.TaskTable;
 import ms.server.domain.Auftrag;
 

 public class MediaStopf extends JFrame {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public static final String PROGRAM = "MediaStopf";
 	public static final String UIIMAGELOCATION = "/ms/client/ui/images/";
 
 	private TaskComboBoxModel boxModel;
 	private TaskTableModel tableModel;
 	private JComboBox taskComboBox;
 	private JScrollPane tableScrollPane;
 	private JPanel tablePanel;
 	private JTextField statusBar;
 	private TaskTable taskTable;
 	private TaskList taskList;
 	private TaskRunningList runningList;
 	private HashMap<String, JButton> buttonMap = new HashMap<String, JButton>();
 	private HashMap<String, JPanel> panelMap = new HashMap<String, JPanel>();
 	private String run = "Run", send = "Send", cancel = "Cancel",
 			runningTask = "Running Tasks", tasks = "Tasks",
 			statusbar = "StatusBar";
 
 	private Client client;
 
 	public MediaStopf(Client client) {
 		this.client = client;
 		taskList = new TaskList();
 		boxModel = new TaskComboBoxModel(taskList);
 		runningList = new TaskRunningList();
 		tableModel = new TaskTableModel(runningList);
 
 		initGUI();
 	}
 
 	/**
 	 * init GUI Components
 	 */
 	private void initGUI() {
 		// createAuftragsTable();
 
 		setTitle(PROGRAM);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLayout(null);
 		setMinimumSize(new Dimension(400, 450));
 		setSize(400, 450);
 		setIconImage(new ImageIcon(getClass().getResource(
 				UIIMAGELOCATION + "icon.png")).getImage());
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		setLocation((dim.width - getWidth()) / 2,
 				(dim.height - getHeight()) / 2);
 		setJMenuBar(createMenuBar());
 
 		addStatusBar();
 		addTaskComboBox();
 		addTaskTable();
 		addTaskPanel();
 		addRunningTaskPanel();
 
 		componentListener();
 		windowListener();
 	}
 
 	private void createAuftragsTable() {
 		// FIXME by Martin remove this block of test code until So, 29.3.09
 		// Achtung RaceCondition. Table wird manchmal angezeigt, manchmal
 		// nicht!!!
 //		TaskList testList = new TaskList();
 //		testList.add("myTask"); //new Auftrag("Franz Muster", "DVD", 5));
 //		//TableModel dataModel = new TaskTableModel(testList);
 //		JTable table = new JTable(dataModel);
 //		table.setVisible(true);
 //		JScrollPane scrollpane = new JScrollPane(table);
 //		scrollpane.setVisible(true);
 //
 //		this.add(scrollpane);
 //		this.setVisible(true);
 //		getContentPane().setVisible(true);
 //		this.setBounds(0, 0, 400, 300);
 
 	}
 
 	private void windowListener() {
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				exit();
 			}
 		});
 	}
 
 	private void componentListener() {
 		addComponentListener(new ComponentAdapter() {
 			private boolean isShown = false;
 
 			@Override
 			public void componentResized(ComponentEvent e) {
 				if (isShown) {
 					int width = getWidth();
 					int height = getHeight();
 
 					JPanel runtaskPanel = panelMap.get(runningTask);
 					runtaskPanel.setSize(width - 5, height - 180);
 					JPanel taskPanel = panelMap.get(tasks);
 					taskPanel.setSize(width - 5, taskPanel.getHeight());
 					JPanel statusPanel = panelMap.get(statusbar);
 					statusPanel.setBounds(0, height - 70, width - 5,
 							statusPanel.getHeight());
 
 					updateComponentBounds(runtaskPanel, taskPanel, statusPanel);
 				}
 			}
 
 			@Override
 			public void componentShown(ComponentEvent e) {
 				isShown = true;
 			}
 		});
 	}
 
 	private void updateComponentBounds(JPanel runtaskPanel, JPanel taskPanel,
 			JPanel statusPanel) {
 		buttonMap.get(run).setLocation(taskPanel.getWidth() - 135,
 				taskPanel.getHeight() - 40);
 
 		int width = runtaskPanel.getWidth() - 10;
 		int height = runtaskPanel.getHeight();
 		buttonMap.get(send).setLocation(width - 235, height + 40);
 		buttonMap.get(cancel).setLocation(width - 125, height + 40);
 
 		taskComboBox.setSize(taskPanel.getWidth() - 30, 20);
 
 		tablePanel.setSize(width, height + 10);
 		tableScrollPane.setSize(width, height);
 		tableScrollPane.revalidate();
 
 		statusBar.setSize(statusPanel.getWidth(), statusPanel.getHeight());
 	}
 
 	private void addStatusBar() {
 		JPanel panel = new JPanel();
 		panel.setLayout(null);
 		panel.setBounds(0, 380, 450, 20);
 		panel.setBorder(BorderFactory.createTitledBorder(statusbar));
 		panelMap.put(statusbar, panel);
 
 		statusBar = new JTextField("(C)2009 MediaStopf");
 		statusBar.setBounds(0, 0, panel.getWidth(), panel.getHeight());
 		statusBar.setEditable(false);
 		statusBar.setFocusable(false);
 
 		panel.add(statusBar);
 		add(panel);
 	}
 
 	/**
 	 * add task panel
 	 */
 	private void addTaskPanel() {
 		JPanel panel = new JPanel();
 		panel.setLayout(null);
 		panel.setBounds(0, 5, 395, 90);
 		panel.setBorder(BorderFactory.createTitledBorder(tasks));
 		panel.add(taskComboBox);
 		panel.add(addRunButton());
 		panelMap.put(tasks, panel);
 		add(panel);
 	}
 
 	/**
 	 * combobox which show the tasks available
 	 * 
 	 * @return JComboBox
 	 */
 	private void addTaskComboBox() {
 		taskComboBox = new JComboBox(boxModel);
 		taskComboBox.setBounds(10, 20, 365, 20);
 		if (0 < taskComboBox.getItemCount())
 			taskComboBox.setSelectedIndex(0);
 		taskComboBox.setUI(new javax.swing.plaf.metal.MetalComboBoxUI() {
 			public void layoutComboBox(Container parent,
 					MetalComboBoxLayoutManager manager) {
 				super.layoutComboBox(parent, manager);
 				arrowButton.setBounds(0, 0, 0, 0);
 			}
 		});
 	}
 
 	/**
 	 * add button to run the task
 	 * 
 	 * @return JButton
 	 */
 	private JButton addRunButton() {
 		JButton runButton = new JButton();
 		runButton.setBounds(260, 50, 100, 25);
 		runButton.setText(run);
 		runButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				runSelectedItem();
 			}
 		});
 		buttonMap.put(run, runButton);
 		return runButton;
 	}
 
 	private void runSelectedItem() {
 		String taskID = (String) taskComboBox.getSelectedItem();
 		File task = new File(taskID);
 		if (!task.isDirectory()) {
 			MessageDialog.info("Not a Directory", taskID
 					+ " is not a directory.");
 			return;
 		}
 
 		client.sendFiles(taskID);
 		client.observeDir(taskID);
 
 		// TODO
 		// ApplicationLauncher.open(program);
 	}
 
 	/**
 	 * add running task panel
 	 */
 	private void addRunningTaskPanel() {
 		JPanel panel = new JPanel();
 		panel.setLayout(null);
 		panel.setBounds(0, 100, 395, 270);
 		panel.setBorder(BorderFactory.createTitledBorder(runningTask));
 
 		addRunningTaskButtons(panel);
 
 		panel.add(tablePanel);
 		panelMap.put(runningTask, panel);
 		add(panel);
 	}
 
 	/**
 	 * add task table
 	 * 
 	 * @return JPanel
 	 */
 	private void addTaskTable() {
 		tablePanel = new JPanel();
 		tablePanel.setBounds(5, 15, 385, 200);
 		tablePanel.setLayout(null);
 
 		taskTable = new TaskTable(tableModel);
 		tableScrollPane = new JScrollPane(taskTable);
 		tableScrollPane.setBounds(0, 0, tablePanel.getWidth(), tablePanel
 				.getHeight());
 		tablePanel.add(tableScrollPane);
 	}
 
 	/**
 	 * add button for the running tasks
 	 * 
 	 * @param panel
 	 *            JPanel
 	 */
 	private void addRunningTaskButtons(JPanel panel) {
 		int x = 150;
 		int y = 230;
 		int width = 100;
 		int height = 25;
 		final String[] buttonText = { send, cancel };
 		final Rectangle sendBounds = new Rectangle(x, y, width, height);
 		final Rectangle cancelBounds = new Rectangle(x + 110, y, width, height);
 		final Rectangle[] bounds = { sendBounds, cancelBounds };
 		final int sendAcc = KeyEvent.VK_S;
 		final int cancelAcc = KeyEvent.VK_C;
 		final int[] mnemonic = { sendAcc, cancelAcc };
 		for (int i = 0; i < buttonText.length; i++) {
 			JButton button = new JButton();
 			button.setBounds(bounds[i]);
 			button.setText(buttonText[i]);
 			button.setMnemonic(mnemonic[i]);
 			button.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					if (e.getActionCommand() == send) {
 						taskTable.send();
 					} else if (e.getActionCommand() == cancel) {
 						taskTable.cancel();
 					}
 				}
 			});
 			panel.add(button);
 			buttonMap.put(buttonText[i], button);
 		}
 	}
 
 	private void exit() {
 		int result = MessageDialog.yesNoDialog("Exit",
 				"Do your really want to Quit?");
 		switch (result) {
 		case JOptionPane.YES_OPTION:
 			System.exit(0);
 			break;
 		case JOptionPane.NO_OPTION:
 			return;
 		default:
 			return;
 		}
 	}
 
 	/**
 	 * MenuBar
 	 * 
 	 * @return JMenuBar
 	 */
 	private JMenuBar createMenuBar() {
 		JMenuBar menuBar = new JMenuBar();
 		final String file = "File", help = "Help";
 		final String[] menuItems = { file, help };
 		final int fileMnemonic = KeyEvent.VK_F, helpMnemonic = KeyEvent.VK_H;
 		final int[] keyEvent = new int[] { fileMnemonic, helpMnemonic };
 		for (int i = 0; i < menuItems.length; i++) {
 			JMenu menu = new JMenu(menuItems[i]);
 			menu.setMnemonic(keyEvent[i]);
 			if (menuItems[i] == file) {
 				addFileItems(menu);
 			} else {
 				addHelpItems(menu);
 			}
 			menuBar.add(menu);
 		}
 		return menuBar;
 	}
 
 	/**
 	 * help menu items
 	 * 
 	 * @param helpMenu
 	 */
 	private void addHelpItems(JMenu helpMenu) {
 		JMenuItem aboutItem = new JMenuItem("About...");
 		aboutItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
 		aboutItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				AboutDialog about = new AboutDialog();
 				about.setVisible(true);
 			}
 		});
 		helpMenu.add(aboutItem);
 	}
 
 	/**
 	 * filemenu items
 	 * 
 	 * @param fileMenu
 	 *            JMenu
 	 */
 	private void addFileItems(JMenu fileMenu) {
 		final String config = "Config", log = "Log", exit = "Exit";
 		final String[] fileTitles = { config, log, exit };
 		final KeyStroke configAccelerator = KeyStroke.getKeyStroke(
 				KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
 		final KeyStroke logAccelerator = KeyStroke.getKeyStroke(KeyEvent.VK_L,
 				KeyEvent.CTRL_DOWN_MASK);
 		final KeyStroke exitAccelerator = null;
 		final KeyStroke[] keyStrokes = { configAccelerator, logAccelerator,
 				exitAccelerator };
 		for (int i = 0; i < fileTitles.length; i++) {
 			if (i == 2) {
 				fileMenu.addSeparator();
 			}
 			JMenuItem fileItem = new JMenuItem();
 			fileItem.setText(fileTitles[i]);
 			fileItem.setAccelerator(keyStrokes[i]);
 			fileItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					if (e.getActionCommand() == config) {
 						ConfigDialog cd = new ConfigDialog();
 						cd.setVisible(true);
 					} else if (e.getActionCommand() == log) {
 						LogDialog ld = new LogDialog();
 						ld.setVisible(true);
 					} else if (e.getActionCommand() == exit) {
 						exit();
 					}
 				}
 			});
 			fileMenu.add(fileItem);
 		}
 	}
 }
