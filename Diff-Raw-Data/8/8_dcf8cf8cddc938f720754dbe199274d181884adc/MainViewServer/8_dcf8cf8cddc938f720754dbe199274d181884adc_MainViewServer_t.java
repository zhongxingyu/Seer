 package ms.server.ui;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
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
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 
 import ms.server.Server;
 import ms.server.StartServer;
 import ms.server.ui.dialogs.AboutDialog;
 import ms.server.ui.dialogs.ExportDialog;
 import ms.server.ui.dialogs.MessageDialog;
 import ms.server.ui.models.TableModel;
 import ms.server.ui.models.TaskComboBoxModel;
 import ms.server.ui.tables.ExportTable;
 import ms.server.ui.tables.Table;
 
 
 public class MainViewServer extends JFrame {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public static final String PROGRAM = "MediaStopf Server";
 	public static final String UIIMAGELOCATION = "/ms/server/ui/images/";
	private static final String SPLASHIMAGE = UIIMAGELOCATION + "splash.jpg";
 
 	private TaskComboBoxModel boxModel;
 	private TableModel importTableModel;
 	private TableModel exportTableModel;
 	private JComboBox taskComboBox;
 	private JPanel tablePanel;
 	private ExportTable exportTable;
 	private JTextField statusBar;
 	private JTabbedPane tabPane;
 	private HashMap<String, JButton> buttonMap = new HashMap<String, JButton>();
 	private HashMap<String, JPanel> panelMap = new HashMap<String, JPanel>();
 	private String export = "Export", cancel = "Cancel", runningTask = "Running Tasks", tasks = "Tasks", statusbar = "StatusBar";
 	
 	public MainViewServer(Server server) {
 		if (StartServer.DEBUG) {
 			setTitle(MainViewServer.PROGRAM + " - Debug");
 		} else {
 			new SplashScreen(SPLASHIMAGE);
 		}
 		boxModel = new TaskComboBoxModel(server);
 		importTableModel = new TableModel();
 		exportTableModel = new TableModel();
 
 		initGUI();
 	}
 
 	/**
 	 * init GUI Components
 	 */
 	private void initGUI() {
 		initFrame();
 
 		addTrayIcon();
 		addStatusBar();
 		addTaskPanel();
 	}
 
 	private void initFrame() {
 		setTitle(PROGRAM);
 		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		setLayout(null);
 		setMinimumSize(new Dimension(400, 450));
 		setSize(400, 450);
 		setIconImage(new ImageIcon(getClass().getResource(UIIMAGELOCATION + "icon.png")).getImage());
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);
 		setJMenuBar(createMenuBar());
 		
 		componentListener();
 		windowListener();
 	}
 
 	private void windowListener() {
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				setVisible(false);
 			}
 		});
 	}
 
 	private void componentListener() {
 		addComponentListener(new ComponentAdapter() {
 			private boolean isShown = false;
 
 			@Override
 			public void componentResized(ComponentEvent e) {
 				if (isShown) {
 					updatePanelBounds();
 				}
 			}
 
 			@Override
 			public void componentShown(ComponentEvent e) {
 				isShown = true;
 			}
 		});
 	}
 	
 	private void updatePanelBounds() {
 		int width = getWidth() - 5;
 		int height = getHeight() - 70;
 		
 		JPanel runtaskPanel = panelMap.get(runningTask);
 		runtaskPanel.setSize(width, height - 110);
 		JPanel taskPanel = panelMap.get(tasks);
 		taskPanel.setSize(width, taskPanel.getHeight());
 		JPanel statusPanel = panelMap.get(statusbar);
 		statusPanel.setBounds(0, height, width, statusPanel.getHeight());
 		
 		updateComponentBounds(runtaskPanel, taskPanel, statusPanel);
 	}
 	
 	private void updateComponentBounds(JPanel runtask, JPanel task, JPanel status) {
 		buttonMap.get(export).setLocation(task.getWidth() - 135,task.getHeight() - 40);
 		
 		int width = runtask.getWidth() - 10;
 		int height = runtask.getHeight() - 40;
 		buttonMap.get(cancel).setLocation(width - 125, height);
 		tablePanel.setSize(width, height - 30);
 		
 		taskComboBox.setSize(task.getWidth() - 30, 20);
 
 		tabPane.setSize(tablePanel.getWidth(), tablePanel.getHeight());
 		
 		statusBar.setSize(status.getWidth(), status.getHeight());
 	}
 	
 	private void addTrayIcon() {
 		new SystemTrayIcon(this);
 	}
 	
 	private void addStatusBar() {
 		JPanel panel = new JPanel();
 		panel.setLayout(null);
 		panel.setBounds(0, 380, 450, 20);
 		panel.setBorder(BorderFactory.createTitledBorder(statusbar));
 		panelMap.put(statusbar, panel);
 		
 		statusBar = new JTextField("(C)2009 MediaStopf Server");
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
 		addButtons();
 		addTaskComboBox();
 		addTaskTable();
 		
 		final int[] y = { 5, 100 };
 		final int[] height = { 90, 270 };
 		final String[] panelLabel = { tasks, runningTask };
 		final String[] buttonLabel = { export, cancel };
 		for(int i=0; i<panelLabel.length;i++) {
 			JPanel panel = new JPanel();
 			panel.setLayout(null);
 			panel.setBounds(0, y[i], 395, height[i]);
 			panel.setBorder(BorderFactory.createTitledBorder(panelLabel[i]));
 			panel.add(buttonMap.get(buttonLabel[i]));
 			add(panel);
 			panelMap.put(panelLabel[i], panel);
 		}
 		panelMap.get(tasks).add(taskComboBox);
 		panelMap.get(runningTask).add(tablePanel);
 	}
 	
 	/**
 	 * combobox which show the tasks available
 	 * 
 	 * @return JComboBox
 	 */
 	private void addTaskComboBox() {
 		taskComboBox = new JComboBox(boxModel);
 		taskComboBox.setBounds(10, 20, 365, 20);
 		if(0<taskComboBox.getItemCount())
 			taskComboBox.setSelectedIndex(0);
 		taskComboBox.setUI(new javax.swing.plaf.metal.MetalComboBoxUI() {
 			public void layoutComboBox(Container parent, MetalComboBoxLayoutManager manager) {
 				super.layoutComboBox(parent, manager);
 				arrowButton.setBounds(0, 0, 0, 0);
 			}
 		});
 	}
 
 	/**
 	 * add buttons
 	 * 
 	 * @return JButton
 	 */
 	private void addButtons() {
 		final int x = 260;
 		final int[] y = { 50, 230 };
 		final int width = 100;
 		final int height = 25;
 		final String[] label = { export, cancel };
 		final int[] events = { KeyEvent.VK_E, KeyEvent.VK_C };
 		for(int i=0;i<label.length;i++) {
 			JButton button = new JButton();
 			button.setBounds(x, y[i], width, height);
 			button.setText(label[i]);
 			button.setMnemonic(events[i]);
 			button.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					if(e.getActionCommand() == export) {
 						exportSelectedItem();
 					} else {
 						exportTable.cancel();
 					}
 				}
 			});
 			buttonMap.put(label[i], button);
 		}
 		buttonMap.get(cancel).setEnabled(false);
 	}
 	
 	private void exportSelectedItem() {
 		String tasknum = (String)taskComboBox.getSelectedItem();
 		File file = new File(tasknum);
 		if(!file.isDirectory()) {
 			MessageDialog.info("Not found", "No directory of " + tasknum + " found");
 			return;
 		}
 		ExportDialog ed = new ExportDialog(tasknum);
 		ed.setVisible(true);
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
 		
 		exportTable = new ExportTable(exportTableModel);
 		
 		tabPane = new JTabbedPane();
 		tabPane.setBounds(0, 5, tablePanel.getWidth(), tablePanel.getHeight());
 		tabPane.addTab("Import", new JScrollPane(new Table(importTableModel)));
 		tabPane.addTab("Export", new JScrollPane(exportTable));
 		tabPane.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				if (tabPane.getSelectedIndex() == 0)
 					buttonMap.get(cancel).setEnabled(false);
 				else
 					buttonMap.get(cancel).setEnabled(true);
 			}
 		});
 
 		tablePanel.add(tabPane);
 	}
 
 	void exit() {
 		int result = MessageDialog.yesNoDialog("Exit", "Do your really want to Quit?");
 		switch(result) {
 		case JOptionPane.YES_OPTION:
 			System.exit(0);
 			break;
 		case JOptionPane.NO_OPTION:
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
 		final String log = "Log", exit = "Exit";
 		final String[] fileTitles = { log, exit };
 		final KeyStroke configAccelerator = KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK);
 		final KeyStroke exitAccelerator = null;
 		final KeyStroke[] keyStrokes = { configAccelerator, exitAccelerator };
 		for (int i = 0; i < fileTitles.length; i++) {
 			if(i == 1) {
 				fileMenu.addSeparator();
 			}
 			JMenuItem fileItem = new JMenuItem();
 			fileItem.setText(fileTitles[i]);
 			fileItem.setAccelerator(keyStrokes[i]);
 			fileItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					if (e.getActionCommand() == log) {
 						LogFrame ld = new LogFrame();
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
