 /*
  * MainWindow.java
  *
  * Created on 15. September 2006, 14:56
  */
 
 package de.unisiegen.tpml.ui;
 
 import java.awt.Component;
 import java.awt.KeyEventDispatcher;
 import java.awt.KeyboardFocusManager;
 import java.awt.Rectangle;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.ResourceBundle;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 
 import org.apache.log4j.Logger;
 
 import de.unisiegen.tpml.core.languages.Language;
 import de.unisiegen.tpml.core.languages.LanguageFactory;
 import de.unisiegen.tpml.core.languages.NoSuchLanguageException;
 import de.unisiegen.tpml.graphics.theme.ThemeManager;
 
 /**
  * The main programm window.
  * 
  * @author Christoph Fehling
  * @version $Rev$
  * 
  * @see de.unisiegen.tpml.ui.Main
  */
 public class MainWindow extends javax.swing.JFrame {
 	//
 	// Constants
 	//
 
 	/**
 	 * The unique serialization identifier for this class.
 	 */
 	private static final long serialVersionUID = -3820623104618482450L;
 
 	/**
 	 * The preferences for the <code>de.unisiegen.tpml.ui</code> package.
 	 */
 	// private static final Preferences preferences = Preferences
 	// .userNodeForPackage(MainWindow.class);
 	//
 	// Constructor
 	//
 	/**
 	 * Creates new form <code>MainWindow</code>.
 	 */
 	public MainWindow() {
 		initComponents();
 
 		setTitle("TPML " + Versions.UI);
 		// position the window
 		PreferenceManager prefmanager = PreferenceManager.get();
 		this.setBounds(prefmanager.getWindowBounds());
 		// Setting the default states
 		setGeneralStates(false);
 		this.saveItem.setEnabled(false);
 		this.saveButton.setEnabled(false);
 		this.preferencesItem.setEnabled(true);
 		this.copyItem.setEnabled(false);
 		this.pasteItem.setEnabled(false);
 		this.recentFilesMenu.setVisible(false);
 		this.cutButton.setEnabled(false);
 		this.copyButton.setEnabled(false);
 		this.pasteButton.setEnabled(false);
 		// Finished setting the states.
 
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				MainWindow.this.handleQuit();
 			}
 		});
 
 		KeyboardFocusManager.getCurrentKeyboardFocusManager()
 				.addKeyEventDispatcher(new KeyEventDispatcher() {
 					public boolean dispatchKeyEvent(KeyEvent evt) {
 						if ((evt.getID() == KeyEvent.KEY_PRESSED)) {
 							if (((evt.getKeyCode() == KeyEvent.VK_RIGHT) && evt
 									.isAltDown())
									|| ((evt.getKeyCode() == KeyEvent.VK_PAGE_UP) && evt
 											.isControlDown())) {
 								if (tabbedPane.getSelectedIndex() + 1 == tabbedPane
 										.getTabCount()) {
 									tabbedPane.setSelectedIndex(0);
 									return true;
 								} else {
 									tabbedPane.setSelectedIndex(tabbedPane
 											.getSelectedIndex() + 1);
 									return true;
 								}
 							} else {
 								if (((evt.getKeyCode() == KeyEvent.VK_LEFT) && evt
 										.isAltDown())
										|| ((evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) && evt
 												.isControlDown())) {
 									if (tabbedPane.getSelectedIndex() == 0) {
 										tabbedPane.setSelectedIndex(tabbedPane
 												.getTabCount() - 1);
 										return true;
 									} else {
 										tabbedPane.setSelectedIndex(tabbedPane
 												.getSelectedIndex() - 1);
 										return true;
 									}
 								}
 							}
 						}
 						return false;
 					}
 				});
 
 		this.editorPanelListener = new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
 				editorStatusChange(evt.getPropertyName(), evt.getNewValue());
 			}
 		};
 
 		this.recentlyUsed = prefmanager.getRecentlyUsed();
 		// TODO this is ugly :(
 		for (int i = 0; i < recentlyUsed.size(); i++) {
 			recentlyUsed.get(i).setWindow(this);
 		}
 		updateRecentlyUsed();
 
 		// apply the last "advanced mode" setting
 		boolean advanced = prefmanager.getAdvanced();
 		this.advancedRadioButton.setSelected(advanced);
 		this.beginnerRadioButton.setSelected(!advanced);
 
 		// apply the last maximization state
 		if (prefmanager.getWindowMaximized()) {
 			// needs to be visible first
 			this.setVisible(true);
 
 			// set to maximized
 			this.setExtendedState(this.getExtendedState()
 					| JFrame.MAXIMIZED_BOTH);
 		}
 	}
 
 	/**
 	 * This method is called from within the constructor to initialize the form.
 	 * WARNING: Do NOT modify this code. The content of this method is always
 	 * regenerated by the Form Editor.
 	 */
 	// <editor-fold defaultstate="collapsed" desc=" Generated Code
 	// <editor-fold defaultstate="collapsed" desc=" Generated Code
 	// <editor-fold defaultstate="collapsed" desc=" Generated Code
 	// <editor-fold defaultstate="collapsed" desc=" Generated Code
 	// <editor-fold defaultstate="collapsed" desc=" Generated Code
 	// ">//GEN-BEGIN:initComponents
 	private void initComponents() {
 		javax.swing.JMenuBar MainMenuBar;
 		javax.swing.JMenu editMenu;
 		javax.swing.JSeparator editMenuSeparator1;
 		javax.swing.JSeparator editMenuSeperator;
 		javax.swing.JToolBar editToolBar;
 		javax.swing.JMenu fileMenu;
 		javax.swing.JSeparator fileMenuSeperator1;
 		javax.swing.JSeparator fileMenuSerpator2;
 		javax.swing.JMenu helpMenu;
 		javax.swing.JToolBar mainToolbar;
 		javax.swing.JButton newButton;
 		javax.swing.JMenuItem newItem;
 		javax.swing.JButton openButton;
 		javax.swing.JMenuItem openItem;
 		javax.swing.JMenuItem quitItem;
 		javax.swing.JMenu runMenu;
 
 		modeSettingsGroup = new javax.swing.ButtonGroup();
 		mainToolbar = new javax.swing.JToolBar();
 		jToolBar1 = new javax.swing.JToolBar();
 		newButton = new javax.swing.JButton();
 		openButton = new javax.swing.JButton();
 		saveButton = new javax.swing.JButton();
 		saveAsButton = new javax.swing.JButton();
 		editToolBar = new javax.swing.JToolBar();
 		cutButton = new javax.swing.JButton();
 		copyButton = new javax.swing.JButton();
 		pasteButton = new javax.swing.JButton();
 		undoButton = new javax.swing.JButton();
 		redoButton = new javax.swing.JButton();
 		tabbedPane = new javax.swing.JTabbedPane();
 		MainMenuBar = new javax.swing.JMenuBar();
 		fileMenu = new javax.swing.JMenu();
 		newItem = new javax.swing.JMenuItem();
 		openItem = new javax.swing.JMenuItem();
 		closeItem = new javax.swing.JMenuItem();
 		fileMenuSeperator1 = new javax.swing.JSeparator();
 		saveItem = new javax.swing.JMenuItem();
 		saveAsItem = new javax.swing.JMenuItem();
 		saveAllItem = new javax.swing.JMenuItem();
 		fileMenuSerpator2 = new javax.swing.JSeparator();
 		recentFilesMenu = new javax.swing.JMenu();
 		fileMenuSeperator3 = new javax.swing.JSeparator();
 		quitItem = new javax.swing.JMenuItem();
 		editMenu = new javax.swing.JMenu();
 		undoItem = new javax.swing.JMenuItem();
 		redoItem = new javax.swing.JMenuItem();
 		editMenuSeparator1 = new javax.swing.JSeparator();
 		cutItem = new javax.swing.JMenuItem();
 		copyItem = new javax.swing.JMenuItem();
 		pasteItem = new javax.swing.JMenuItem();
 		editMenuSeperator = new javax.swing.JSeparator();
 		preferencesItem = new javax.swing.JMenuItem();
 		runMenu = new javax.swing.JMenu();
 		smallstepItem = new javax.swing.JMenuItem();
 		bigstepItem = new javax.swing.JMenuItem();
 		typecheckerItem = new javax.swing.JMenuItem();
 		runMenuSeparator1 = new javax.swing.JSeparator();
 		beginnerRadioButton = new javax.swing.JRadioButtonMenuItem();
 		advancedRadioButton = new javax.swing.JRadioButtonMenuItem();
 		helpMenu = new javax.swing.JMenu();
 		aboutItem = new javax.swing.JMenuItem();
 
 		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
 		setName("mainframe");
 		addKeyListener(new java.awt.event.KeyAdapter() {
 			public void keyPressed(java.awt.event.KeyEvent evt) {
 				tabChange(evt);
 			}
 		});
 
 		mainToolbar.setFloatable(false);
 		newButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/new24.png")));
 		newButton.setToolTipText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("New_File"));
 		newButton.setBorderPainted(false);
 		newButton.setOpaque(false);
 		newButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				newButtonActionPerformed(evt);
 			}
 		});
 
 		jToolBar1.add(newButton);
 
 		openButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/open24.png")));
 		openButton.setToolTipText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Open_File"));
 		openButton.setBorderPainted(false);
 		openButton.setOpaque(false);
 		openButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				openButtonActionPerformed(evt);
 			}
 		});
 
 		jToolBar1.add(openButton);
 
 		saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/save24.png")));
 		saveButton.setToolTipText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Save_File"));
 		saveButton.setBorderPainted(false);
 		saveButton.setOpaque(false);
 		saveButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				saveButtonActionPerformed(evt);
 			}
 		});
 
 		jToolBar1.add(saveButton);
 
 		saveAsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/saveas24.png")));
 		saveAsButton.setToolTipText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Save_File_As..."));
 		saveAsButton.setBorderPainted(false);
 		saveAsButton.setOpaque(false);
 		saveAsButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				saveAsButtonActionPerformed(evt);
 			}
 		});
 
 		jToolBar1.add(saveAsButton);
 
 		mainToolbar.add(jToolBar1);
 
 		editToolBar.setMaximumSize(new java.awt.Dimension(32767, 40));
 		cutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/cut24.gif")));
 		cutButton.setToolTipText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Cut"));
 		cutButton.setBorderPainted(false);
 		cutButton.setOpaque(false);
 		cutButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				cutButtonActionPerformed(evt);
 			}
 		});
 
 		editToolBar.add(cutButton);
 
 		copyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/copy24.gif")));
 		copyButton.setToolTipText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Copy"));
 		copyButton.setBorderPainted(false);
 		copyButton.setOpaque(false);
 		copyButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				copyButtonActionPerformed(evt);
 			}
 		});
 
 		editToolBar.add(copyButton);
 
 		pasteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/paste24.gif")));
 		pasteButton.setToolTipText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Paste"));
 		pasteButton.setBorderPainted(false);
 		pasteButton.setOpaque(false);
 		pasteButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				pasteButtonActionPerformed(evt);
 			}
 		});
 
 		editToolBar.add(pasteButton);
 
 		undoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/undo24.gif")));
 		undoButton.setToolTipText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Undo_the_last_step."));
 		undoButton.setBorderPainted(false);
 		undoButton.setOpaque(false);
 		undoButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				undoButtonActionPerformed(evt);
 			}
 		});
 
 		editToolBar.add(undoButton);
 
 		redoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/redo24.gif")));
 		redoButton.setToolTipText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Redo_the_last_step."));
 		redoButton.setBorderPainted(false);
 		redoButton.setOpaque(false);
 		redoButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				redoButtonActionPerformed(evt);
 			}
 		});
 
 		editToolBar.add(redoButton);
 
 		mainToolbar.add(editToolBar);
 
 		getContentPane().add(mainToolbar, java.awt.BorderLayout.NORTH);
 
 		tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
 			public void stateChanged(javax.swing.event.ChangeEvent evt) {
 				tabbedPaneStateChanged(evt);
 			}
 		});
 
 		getContentPane().add(tabbedPane, java.awt.BorderLayout.CENTER);
 
 		fileMenu.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("FileMnemonic").charAt(0));
 		fileMenu.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("File"));
 		newItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_N,
 				java.awt.event.InputEvent.CTRL_MASK));
 		newItem.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/new16.gif")));
 		newItem.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("NewMnemonic").charAt(0));
 		newItem.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("New"));
 		newItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				newItemActionPerformed(evt);
 			}
 		});
 
 		fileMenu.add(newItem);
 
 		openItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_O,
 				java.awt.event.InputEvent.CTRL_MASK));
 		openItem.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/open16.png")));
 		openItem.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("OpenMnemonic").charAt(0));
 		openItem.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Open"));
 		openItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				openItemActionPerformed(evt);
 			}
 		});
 
 		fileMenu.add(openItem);
 
 		closeItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_W,
 				java.awt.event.InputEvent.CTRL_MASK));
 		closeItem.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/empty16.gif")));
 		closeItem
 				.setMnemonic(java.util.ResourceBundle.getBundle(
 						"de/unisiegen/tpml/ui/ui").getString("CloseMnemonic")
 						.charAt(0));
 		closeItem.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Close"));
 		closeItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				closeItemActionPerformed(evt);
 			}
 		});
 
 		fileMenu.add(closeItem);
 
 		fileMenu.add(fileMenuSeperator1);
 
 		saveItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_S,
 				java.awt.event.InputEvent.CTRL_MASK));
 		saveItem.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/save16.png")));
 		saveItem.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("SaveMnemonic").charAt(0));
 		saveItem.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Save"));
 		saveItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				saveItemActionPerformed(evt);
 			}
 		});
 
 		fileMenu.add(saveItem);
 
 		saveAsItem.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/saveas16.png")));
 		saveAsItem.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("SaveAsMnemonic")
 				.charAt(0));
 		saveAsItem.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Save_As..."));
 		saveAsItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				saveAsItemActionPerformed(evt);
 			}
 		});
 
 		fileMenu.add(saveAsItem);
 
 		saveAllItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_S,
 				java.awt.event.InputEvent.SHIFT_MASK
 						| java.awt.event.InputEvent.CTRL_MASK));
 		saveAllItem.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/saveAll16.gif")));
 		saveAllItem.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("SaveAllMnemonic").charAt(
 				0));
 		saveAllItem.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Save_All"));
 		saveAllItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				saveAllItemActionPerformed(evt);
 			}
 		});
 
 		fileMenu.add(saveAllItem);
 
 		fileMenu.add(fileMenuSerpator2);
 
 		recentFilesMenu.setIcon(new javax.swing.ImageIcon(getClass()
 				.getResource("/de/unisiegen/tpml/ui/icons/empty16.gif")));
 		recentFilesMenu.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("RecentlyUsedMnemonic")
 				.charAt(0));
 		recentFilesMenu.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Recently_Used"));
 		fileMenu.add(recentFilesMenu);
 
 		fileMenu.add(fileMenuSeperator3);
 
 		quitItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_Q,
 				java.awt.event.InputEvent.CTRL_MASK));
 		quitItem.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/empty16.gif")));
 		quitItem.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("QuitMnemonic").charAt(0));
 		quitItem.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Quit"));
 		quitItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				quitItemActionPerformed(evt);
 			}
 		});
 
 		fileMenu.add(quitItem);
 
 		MainMenuBar.add(fileMenu);
 
 		editMenu.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("EditMnemonic").charAt(0));
 		editMenu.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Edit"));
 		undoItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_Z,
 				java.awt.event.InputEvent.CTRL_MASK));
 		undoItem.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/undo16.gif")));
 		undoItem.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("UndoMnemonic").charAt(0));
 		undoItem.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Undo"));
 		undoItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				undoItemActionPerformed(evt);
 			}
 		});
 
 		editMenu.add(undoItem);
 
 		redoItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_Y,
 				java.awt.event.InputEvent.CTRL_MASK));
 		redoItem.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/redo16.gif")));
 		redoItem.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("RedoMnemonic").charAt(0));
 		redoItem.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Redo"));
 		redoItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				redoItemActionPerformed(evt);
 			}
 		});
 
 		editMenu.add(redoItem);
 
 		editMenu.add(editMenuSeparator1);
 
 		cutItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_X,
 				java.awt.event.InputEvent.CTRL_MASK));
 		cutItem.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/cut16.gif")));
 		cutItem.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("CutMnemonic").charAt(0));
 		cutItem.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Cut"));
 		cutItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				cutItemActionPerformed(evt);
 			}
 		});
 
 		editMenu.add(cutItem);
 
 		copyItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_C,
 				java.awt.event.InputEvent.CTRL_MASK));
 		copyItem.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/copy16.gif")));
 		copyItem.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("CopyMnemonic").charAt(0));
 		copyItem.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Copy"));
 		copyItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				copyItemActionPerformed(evt);
 			}
 		});
 
 		editMenu.add(copyItem);
 
 		pasteItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_V,
 				java.awt.event.InputEvent.CTRL_MASK));
 		pasteItem.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/de/unisiegen/tpml/ui/icons/paste16.gif")));
 		pasteItem
 				.setMnemonic(java.util.ResourceBundle.getBundle(
 						"de/unisiegen/tpml/ui/ui").getString("PasteMnemonic")
 						.charAt(0));
 		pasteItem.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Paste"));
 		pasteItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				pasteItemActionPerformed(evt);
 			}
 		});
 
 		editMenu.add(pasteItem);
 
 		editMenu.add(editMenuSeperator);
 
 		preferencesItem.setIcon(new javax.swing.ImageIcon(getClass()
 				.getResource("/de/unisiegen/tpml/ui/icons/empty16.gif")));
 		preferencesItem.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("PreferencesMnemonic")
 				.charAt(0));
 		preferencesItem.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Preferences"));
 		preferencesItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				preferencesItemActionPerformed(evt);
 			}
 		});
 
 		editMenu.add(preferencesItem);
 
 		MainMenuBar.add(editMenu);
 
 		runMenu
 				.setMnemonic(java.util.ResourceBundle.getBundle(
 						"de/unisiegen/tpml/ui/ui").getString("ProofMnemonic")
 						.charAt(0));
 		runMenu.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Proof"));
 		smallstepItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_F9, 0));
 		smallstepItem.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("SmallStepMnemonic")
 				.charAt(0));
 		smallstepItem.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("SmallStep"));
 		smallstepItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				smallstepItemActionPerformed(evt);
 			}
 		});
 
 		runMenu.add(smallstepItem);
 
 		bigstepItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_F11, 0));
 		bigstepItem.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("BigStepMnemonic").charAt(
 				0));
 		bigstepItem.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("BigStep"));
 		bigstepItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				bigstepItemActionPerformed(evt);
 			}
 		});
 
 		runMenu.add(bigstepItem);
 
 		typecheckerItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_F12, 0));
 		typecheckerItem.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("TypeCheckerMnemonic")
 				.charAt(0));
 		typecheckerItem.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("TypeChecker"));
 		typecheckerItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				typecheckerItemActionPerformed(evt);
 			}
 		});
 
 		runMenu.add(typecheckerItem);
 
 		runMenu.add(runMenuSeparator1);
 
 		modeSettingsGroup.add(beginnerRadioButton);
 		beginnerRadioButton.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("BeginnerMnemonic")
 				.charAt(0));
 		beginnerRadioButton.setSelected(true);
 		beginnerRadioButton.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Beginner"));
 		beginnerRadioButton.addItemListener(new java.awt.event.ItemListener() {
 			public void itemStateChanged(java.awt.event.ItemEvent evt) {
 				beginnerRadioButtonItemStateChanged(evt);
 			}
 		});
 
 		runMenu.add(beginnerRadioButton);
 
 		modeSettingsGroup.add(advancedRadioButton);
 		advancedRadioButton.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("AdvancedMnemonic")
 				.charAt(0));
 		advancedRadioButton.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Advanced"));
 		advancedRadioButton.addItemListener(new java.awt.event.ItemListener() {
 			public void itemStateChanged(java.awt.event.ItemEvent evt) {
 				advancedRadioButtonItemStateChanged(evt);
 			}
 		});
 
 		runMenu.add(advancedRadioButton);
 
 		MainMenuBar.add(runMenu);
 
 		helpMenu.setMnemonic(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("HelpMnemonic").charAt(0));
 		helpMenu.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("Help"));
 		aboutItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
 				java.awt.event.KeyEvent.VK_F1, 0));
 		aboutItem
 				.setMnemonic(java.util.ResourceBundle.getBundle(
 						"de/unisiegen/tpml/ui/ui").getString("AboutMnemonic")
 						.charAt(0));
 		aboutItem.setText(java.util.ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("About..."));
 		aboutItem.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				aboutItemActionPerformed(evt);
 			}
 		});
 
 		helpMenu.add(aboutItem);
 
 		MainMenuBar.add(helpMenu);
 
 		setJMenuBar(MainMenuBar);
 
 		setBounds(0, 0, 706, 561);
 	}// </editor-fold>//GEN-END:initComponents
 
 	private void tabChange(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_tabChange
 	// TODO add your handling code here:
 
 	}// GEN-LAST:event_tabChange
 
 	private void aboutItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_aboutItemActionPerformed
 		// TODO add your handling code here:
 		AboutDialog about = new AboutDialog(this, true);
 		about.setLocationRelativeTo(this);
 		about.setVisible(true);
 	}// GEN-LAST:event_aboutItemActionPerformed
 
 	private void pasteButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_pasteButtonActionPerformed
 		// TODO add your handling code here:
 		getActiveEditor().handlePaste();
 	}// GEN-LAST:event_pasteButtonActionPerformed
 
 	private void copyButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_copyButtonActionPerformed
 		// TODO add your handling code here:
 		getActiveEditor().handleCopy();
 	}// GEN-LAST:event_copyButtonActionPerformed
 
 	private void cutButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cutButtonActionPerformed
 		// TODO add your handling code here:
 		getActiveEditor().handleCut();
 	}// GEN-LAST:event_cutButtonActionPerformed
 
 	private void advancedRadioButtonItemStateChanged(
 			java.awt.event.ItemEvent evt) {// GEN-FIRST:event_advancedRadioButtonItemStateChanged
 		if (this.advancedRadioButton.isSelected()) {
 			for (Component component : this.tabbedPane.getComponents()) {
 				if (component instanceof EditorPanel) {
 					EditorPanel editorPanel = (EditorPanel) component;
 					editorPanel.setAdvanced(true);
 				}
 			}
 		}
 	}// GEN-LAST:event_advancedRadioButtonItemStateChanged
 
 	private void beginnerRadioButtonItemStateChanged(
 			java.awt.event.ItemEvent evt) {// GEN-FIRST:event_beginnerRadioButtonItemStateChanged
 		if (this.beginnerRadioButton.isSelected()) {
 			for (Component component : this.tabbedPane.getComponents()) {
 				if (component instanceof EditorPanel) {
 					EditorPanel editorPanel = (EditorPanel) component;
 					editorPanel.setAdvanced(false);
 				}
 			}
 		}
 	}// GEN-LAST:event_beginnerRadioButtonItemStateChanged
 
 	private void pasteItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_pasteItemActionPerformed
 		// 
 		getActiveEditor().handlePaste();
 	}// GEN-LAST:event_pasteItemActionPerformed
 
 	private void copyItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_copyItemActionPerformed
 		// 
 		getActiveEditor().handleCopy();
 	}// GEN-LAST:event_copyItemActionPerformed
 
 	private void cutItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cutItemActionPerformed
 		// 
 		getActiveEditor().handleCut();
 	}// GEN-LAST:event_cutItemActionPerformed
 
 	private void saveAllItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_saveAllItemActionPerformed
 		// 
 		handleSaveAll();
 	}// GEN-LAST:event_saveAllItemActionPerformed
 
 	private void redoItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_redoItemActionPerformed
 		// 
 		(getActiveEditor()).handleRedo();
 	}// GEN-LAST:event_redoItemActionPerformed
 
 	private void undoItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_undoItemActionPerformed
 		// 
 		(getActiveEditor()).handleUndo();
 	}// GEN-LAST:event_undoItemActionPerformed
 
 	private void openItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_openItemActionPerformed
 		// 
 		handleOpen();
 	}// GEN-LAST:event_openItemActionPerformed
 
 	private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_openButtonActionPerformed
 		handleOpen();
 	}// GEN-LAST:event_openButtonActionPerformed
 
 	private void preferencesItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_preferencesItemActionPerformed
 		// 
 		PreferenceDialog prefdialog = new PreferenceDialog(this, true);
 		prefdialog.setLocationRelativeTo(this);
 		prefdialog.setVisible(true);
 
 	}// GEN-LAST:event_preferencesItemActionPerformed
 
 	private void newItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_newItemActionPerformed
 		// 
 		handleNew();
 	}// GEN-LAST:event_newItemActionPerformed
 
 	private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_tabbedPaneStateChanged
 		// 
 		updateEditorStates((EditorPanel) tabbedPane.getSelectedComponent());
 	}// GEN-LAST:event_tabbedPaneStateChanged
 
 	private void typecheckerItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_typecheckerItemActionPerformed
 		// 
 		(getActiveEditor()).handleTypeChecker();
 	}// GEN-LAST:event_typecheckerItemActionPerformed
 
 	private void bigstepItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_bigstepItemActionPerformed
 		// 
 		(getActiveEditor()).handleBigStep();
 	}// GEN-LAST:event_bigstepItemActionPerformed
 
 	private void smallstepItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_smallstepItemActionPerformed
 		// 
 		(getActiveEditor()).handleSmallStep();
 	}// GEN-LAST:event_smallstepItemActionPerformed
 
 	private void quitItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_quitItemActionPerformed
 		// 
 		handleQuit();
 	}// GEN-LAST:event_quitItemActionPerformed
 
 	private void saveAsItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_saveAsItemActionPerformed
 		// 
 		getActiveEditor().handleSaveAs();
 	}// GEN-LAST:event_saveAsItemActionPerformed
 
 	private void saveItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_saveItemActionPerformed
 		// 
 		getActiveEditor().handleSave();
 	}// GEN-LAST:event_saveItemActionPerformed
 
 	private void closeItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_closeItemActionPerformed
 		// 
 		handleClose();
 
 	}// GEN-LAST:event_closeItemActionPerformed
 
 	private void redoButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_redoButtonActionPerformed
 		// 
 		(getActiveEditor()).handleRedo();
 	}// GEN-LAST:event_redoButtonActionPerformed
 
 	private void undoButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_undoButtonActionPerformed
 		// 
 		(getActiveEditor()).handleUndo();
 	}// GEN-LAST:event_undoButtonActionPerformed
 
 	private void saveAsButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_saveAsButtonActionPerformed
 		// 
 		getActiveEditor().handleSaveAs();
 	}// GEN-LAST:event_saveAsButtonActionPerformed
 
 	private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_saveButtonActionPerformed
 		// 
 		getActiveEditor().handleSave();
 	}// GEN-LAST:event_saveButtonActionPerformed
 
 	private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_newButtonActionPerformed
 		// 
 		handleNew();
 	}// GEN-LAST:event_newButtonActionPerformed
 
 	// Variables declaration - do not modify//GEN-BEGIN:variables
 	private javax.swing.JMenuItem aboutItem;
 
 	private javax.swing.JRadioButtonMenuItem advancedRadioButton;
 
 	private javax.swing.JRadioButtonMenuItem beginnerRadioButton;
 
 	private javax.swing.JMenuItem bigstepItem;
 
 	private javax.swing.JMenuItem closeItem;
 
 	private javax.swing.JButton copyButton;
 
 	private javax.swing.JMenuItem copyItem;
 
 	private javax.swing.JButton cutButton;
 
 	private javax.swing.JMenuItem cutItem;
 
 	private javax.swing.JSeparator fileMenuSeperator3;
 
 	private javax.swing.JToolBar jToolBar1;
 
 	private javax.swing.ButtonGroup modeSettingsGroup;
 
 	private javax.swing.JButton pasteButton;
 
 	private javax.swing.JMenuItem pasteItem;
 
 	private javax.swing.JMenuItem preferencesItem;
 
 	private javax.swing.JMenu recentFilesMenu;
 
 	private javax.swing.JButton redoButton;
 
 	private javax.swing.JMenuItem redoItem;
 
 	private javax.swing.JSeparator runMenuSeparator1;
 
 	private javax.swing.JMenuItem saveAllItem;
 
 	private javax.swing.JButton saveAsButton;
 
 	private javax.swing.JMenuItem saveAsItem;
 
 	private javax.swing.JButton saveButton;
 
 	private javax.swing.JMenuItem saveItem;
 
 	private javax.swing.JMenuItem smallstepItem;
 
 	private javax.swing.JTabbedPane tabbedPane;
 
 	private javax.swing.JMenuItem typecheckerItem;
 
 	private javax.swing.JButton undoButton;
 
 	private javax.swing.JMenuItem undoItem;
 
 	// End of variables declaration//GEN-END:variables
 	private PropertyChangeListener editorPanelListener;
 
 	private static final Logger logger = Logger.getLogger(MainWindow.class);
 
 	// private PreferenceManager prefmanager;
 
 	private static int historyLength = 9;
 
 	private LinkedList<HistoryItem> recentlyUsed;
 
 	// Self-defined methods:
 
 	void openFile(File file) {
 		if (file == null) {
 			throw new NullPointerException("file is null");
 		}
 
 		try {
 			// check if we already have an editor panel for the file
 			EditorPanel editorPanel = null;
 			for (Component component : this.tabbedPane.getComponents()) {
 				if (component instanceof EditorPanel
 						&& file.equals(((EditorPanel) component).getFile())) {
 					editorPanel = (EditorPanel) component;
 					break;
 				}
 			}
 
 			// if we don't already have the editor panel, create a new one
 			if (editorPanel == null) {
 				LanguageFactory langfactory = LanguageFactory.newInstance();
 				Language language = langfactory.getLanguageByFile(file);
 
 				StringBuffer buffer = new StringBuffer();
 				FileInputStream in = new FileInputStream(file);
 				int onechar;
 
 				while ((onechar = in.read()) != -1) {
 					buffer.append((char) onechar);
 				}
 				in.close();
 
 				editorPanel = new EditorPanel(language, this);
 				tabbedPane.add(editorPanel);
 				editorPanel.setAdvanced(this.advancedRadioButton.isSelected());
 
 				editorPanel.setFileName(file.getName());
 				editorPanel.setEditorText(buffer.toString());
 				editorPanel.setFile(file);
 				editorPanel.addPropertyChangeListener(editorPanelListener);
 				editorPanel.setTexteditor(true);
 			}
 
 			this.tabbedPane.setSelectedComponent(editorPanel);
 			setGeneralStates(true);
 			updateEditorStates(editorPanel);
 
 		} catch (NoSuchLanguageException e) {
 			logger.error("Language does not exist.", e);
 			JOptionPane.showMessageDialog(this, java.util.ResourceBundle
 					.getBundle("de/unisiegen/tpml/ui/ui").getString(
 							"FileNotSupported"), java.util.ResourceBundle
 					.getBundle("de/unisiegen/tpml/ui/ui")
 					.getString("Open_File"), JOptionPane.ERROR_MESSAGE);
 		} catch (FileNotFoundException e) {
 			logger.error("File specified could not be found", e);
 			JOptionPane.showMessageDialog(this, java.util.ResourceBundle
 					.getBundle("de/unisiegen/tpml/ui/ui").getString(
 							"FileCannotBeFound"), java.util.ResourceBundle
 					.getBundle("de/unisiegen/tpml/ui/ui")
 					.getString("Open_File"), JOptionPane.ERROR_MESSAGE);
 		} catch (IOException e) {
 			logger.error("Could not read from the file specified", e);
 			JOptionPane.showMessageDialog(this, java.util.ResourceBundle
 					.getBundle("de/unisiegen/tpml/ui/ui").getString(
 							"FileCannotBeRead"), java.util.ResourceBundle
 					.getBundle("de/unisiegen/tpml/ui/ui")
 					.getString("Open_File"), JOptionPane.ERROR_MESSAGE);
 		}
 
 	}
 
 	private void setGeneralStates(boolean state) {
 		smallstepItem.setEnabled(state);
 		bigstepItem.setEnabled(state);
 		typecheckerItem.setEnabled(state);
 		saveAsItem.setEnabled(state);
 		saveAsButton.setEnabled(state);
 		saveAllItem.setEnabled(state);
 		closeItem.setEnabled(state);
 		cutItem.setEnabled(state);
 		cutButton.setEnabled(state);
 		copyItem.setEnabled(state);
 		copyButton.setEnabled(state);
 		pasteItem.setEnabled(state);
 		pasteButton.setEnabled(state);
 
 		setUndoState(state);
 		setRedoState(state);
 	}
 
 	private void editorStatusChange(String ident, Object newValue) {
 		logger.debug("Editor status changed: " + ident);
 		if (ident.equals("redoStatus")) {
 			logger.debug("Editor status changed. Ident: redoStatus");
 			setRedoState((Boolean) newValue);
 		} else if (ident.equals("filename")) {
 			logger.debug("Editor status changed. Ident: filename");
 			tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(),
 					(String) newValue);
 			// TODO merge undostatus and changestatus
 		} else if (ident.equals("undoStatus")) {
 			logger.debug("Editor status changed. Ident: undoStatus");
 			setUndoState((Boolean) newValue);
 			// setChangeState((Boolean) newValue);
 		} else if (ident.equals("changed")) {
 			logger.debug("Editor status changed. Ident: changed");
 			setChangeState((Boolean) newValue);
 			// setSaveState((Boolean) newValue);
 		} else if (ident.equals("texteditor")) {
 			logger.debug("Editor status changed. Ident: textditor");
 			cutItem.setEnabled((Boolean) newValue);
 			cutButton.setEnabled((Boolean) newValue);
 			copyItem.setEnabled((Boolean) newValue);
 			copyButton.setEnabled((Boolean) newValue);
 			pasteItem.setEnabled((Boolean) newValue);
 			pasteButton.setEnabled((Boolean) newValue);
 		}
 	}
 
 	private void updateEditorStates(EditorPanel editor) {
 		if (editor == null) {// last tab was closed
 			setGeneralStates(false);
 			// }
 			// if (getActiveEditor() == null) { // the same as above?
 			// setGeneralStates(false);
 		} else {
 			setRedoState(editor.isRedoStatus());
 			setUndoState(editor.isUndoStatus());
 			// setSaveState(editor.isUndoStatus());
 			setChangeState(editor.isUndoStatus());
 			if (editor.isTexteditor()) {
 				setEditorFunctions(true);
 			} else {
 				setEditorFunctions(true);
 			}
 		}
 	}
 
 	private void setEditorFunctions(boolean state) {
 		cutButton.setEnabled(state);
 		cutItem.setEnabled(state);
 		copyButton.setEnabled(state);
 		copyItem.setEnabled(state);
 		pasteButton.setEnabled(state);
 		pasteItem.setEnabled(state);
 	}
 
 	private void updateRecentlyUsed() {
 		final int length = (this.recentlyUsed.size() > historyLength) ? historyLength
 				: this.recentlyUsed.size();
 		if (length > historyLength) {
 			logger
 					.error("Error: The list of recently used files is larger than "
 							+ historyLength);
 		}
 		HistoryItem item;
 		this.recentFilesMenu.setVisible(length > 0);
 		this.fileMenuSeperator3.setVisible(length > 0);
 		this.recentFilesMenu.removeAll();
 		for (int i = 0; i < length; i++) {
 			item = this.recentlyUsed.get(i);
 			item.setText("" + (i + 1) + ". " + item.getFile().getName());
 			this.recentFilesMenu.add(item);
 		}
 		this.recentFilesMenu.addSeparator();
 		JMenuItem openAllItem = new JMenuItem(ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("OpenAll"));
 		openAllItem.setMnemonic(ResourceBundle.getBundle(
 				"de/unisiegen/tpml/ui/ui").getString("OpenAllMnemonic").charAt(
 				0));
 		openAllItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				for (int i = 0; i < length; ++i) {
 					File file = MainWindow.this.recentlyUsed.get(i).getFile();
 					openFile(file);
 				}
 			}
 		});
 		this.recentFilesMenu.add(openAllItem);
 	}
 
 	public void addRecentlyUsed(HistoryItem historyItem) {
 
 		boolean alreadyPresent = false;
 		// check if a similar entry in the history already exists:
 		for (int i = 0; i < recentlyUsed.size(); i++) {
 			if (recentlyUsed.get(i).getFile().toURI().equals(
 					historyItem.getFile().toURI())) {
 				alreadyPresent = true;
 			}
 		}
 
 		if (!alreadyPresent)
 			recentlyUsed.addFirst(historyItem);
 		historyItem.setWindow(this);
 		if (recentlyUsed.size() > historyLength)
 			recentlyUsed.removeLast();
 		updateRecentlyUsed();
 	}
 
 	private void setRedoState(Boolean state) {
 		redoButton.setEnabled(state);
 		redoItem.setEnabled(state);
 	}
 
 	private void setUndoState(Boolean state) {
 		logger.debug("UndoStatus of MainWindow set to " + state);
 		undoButton.setEnabled(state);
 		undoItem.setEnabled(state);
 
 	}
 
 	private void setSaveState(Boolean state) {
 		saveButton.setEnabled(state);
 		saveItem.setEnabled(state);
 	}
 
 	private void setChangeState(Boolean state) {
 		if (state) {
 			tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), "*"
 					+ ((EditorPanel) tabbedPane.getSelectedComponent())
 							.getFileName());
 			setSaveState(true);
 
 		} else {
 			tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(),
 					((EditorPanel) tabbedPane.getSelectedComponent())
 							.getFileName());
 			setSaveState(false);
 		}
 	}
 
 	private EditorPanel getActiveEditor() {
 		return (EditorPanel) tabbedPane.getSelectedComponent();
 	}
 
 	private void handleNew() {
 		FileWizard wizard = new FileWizard(this, true);
 		wizard.setLocationRelativeTo(this);
 		wizard.setVisible(true);
 		Language language = wizard.getLanguage();
 
 		if (language == null)
 			return;
 
 		EditorPanel newEditorPanel = new EditorPanel(language, this);
 		tabbedPane.add(newEditorPanel);
 		newEditorPanel.setAdvanced(this.advancedRadioButton.isSelected());
 		tabbedPane.setSelectedComponent(newEditorPanel);
 		newEditorPanel.addPropertyChangeListener(editorPanelListener);
 		newEditorPanel.setTexteditor(true);
 		setGeneralStates(true);
 		updateEditorStates(newEditorPanel);
 	}
 
 	private void handleOpen() {
 		PreferenceManager prefmanager = PreferenceManager.get();
 		JFileChooser chooser = new JFileChooser(prefmanager.getWorkingPath());
 		chooser.setMultiSelectionEnabled(true);
 		final LanguageFactory factory = LanguageFactory.newInstance();
 		chooser.addChoosableFileFilter(new FileFilter() {
 			@Override
 			public boolean accept(File f) {
 				if (f.isDirectory()) {
 					return true;
 				}
 				try {
 					factory.getLanguageByFile(f);
 					return true;
 				} catch (NoSuchLanguageException e) {
 					return false;
 				}
 			}
 
 			@Override
 			public String getDescription() {
 				Language[] languages = factory.getAvailableLanguages();
 				StringBuilder builder = new StringBuilder(128);
 				builder.append("Source Files (");
 				for (int n = 0; n < languages.length; ++n) {
 					if (n > 0) {
 						builder.append("; ");
 					}
 					builder.append("*.");
 					builder.append(languages[n].getName().toLowerCase());
 				}
 				builder.append(')');
 				return builder.toString();
 			}
 		});
 		chooser.setAcceptAllFileFilterUsed(false);
 
 		int returnVal = chooser.showOpenDialog(this);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			File[] files = chooser.getSelectedFiles();
 			for (int i = 0; i < files.length; i++) {
 				openFile(files[i]);
 			}
 		}
 		prefmanager.setWorkingPath(chooser.getCurrentDirectory()
 				.getAbsolutePath());
 	}
 
 	private void handleQuit() {
 		// be sure to save all files first
 		for (Component component : this.tabbedPane.getComponents()) {
 			if (component instanceof EditorPanel) {
 				EditorPanel editorPanel = (EditorPanel) component;
 				if (!editorPanel.shouldBeSaved()) {
 					continue;
 				}
 
 				// Custom button text
 				Object[] options = {
 						java.util.ResourceBundle.getBundle(
 								"de/unisiegen/tpml/ui/ui").getString("Yes"),
 						java.util.ResourceBundle.getBundle(
 								"de/unisiegen/tpml/ui/ui").getString("No"),
 						java.util.ResourceBundle.getBundle(
 								"de/unisiegen/tpml/ui/ui").getString("Cancel") };
 				int n = JOptionPane
 						.showOptionDialog(this, editorPanel.getFileName()
 								+ " "
 								+ java.util.ResourceBundle.getBundle(
 										"de/unisiegen/tpml/ui/ui").getString(
 										"WantTosave"), java.util.ResourceBundle
 								.getBundle("de/unisiegen/tpml/ui/ui")
 								.getString("Save_File"),
 								JOptionPane.YES_NO_CANCEL_OPTION,
 								JOptionPane.QUESTION_MESSAGE, null, options,
 								options[2]);
 				switch (n) {
 				case 0: // Save changes
 					logger.debug("Quit dialog: YES");
 					if (!editorPanel.handleSave()) {
 						// abort the quit
 						return;
 					}
 					break;
 
 				case 1: // Do not save changes
 					logger.debug("Quit dialog: NO");
 					break;
 
 				default: // Cancelled
 					logger.debug("Quit dialog: CANCEL");
 					return;
 				}
 			}
 		}
 
 		// save the session
 		saveOpenFiles();
 
 		// remember the settings
 		PreferenceManager prefmanager = PreferenceManager.get();
 		prefmanager.setAdvanced(this.advancedRadioButton.isSelected());
 		// remember the history
 		prefmanager.setRecentlyUsed(recentlyUsed);
 		// remember window state
 		prefmanager.setWindowPreferences(this);
 		// save the themes
 		ThemeManager.get().saveThemes();
 		// terminate the application
 		System.exit(0);
 	}
 
 	/**
 	 * Closes the active editor window.
 	 * 
 	 * @return true if the active editor could be closed.
 	 */
 	private boolean handleClose() {
 		EditorPanel selectedEditor = getActiveEditor();
 		boolean success;
 		if (selectedEditor.shouldBeSaved()) {
 
 			Object[] options = {
 					java.util.ResourceBundle.getBundle(
 							"de/unisiegen/tpml/ui/ui").getString("Yes"),
 					java.util.ResourceBundle.getBundle(
 							"de/unisiegen/tpml/ui/ui").getString("No"),
 					java.util.ResourceBundle.getBundle(
 							"de/unisiegen/tpml/ui/ui").getString("Cancel") };
 			int n = JOptionPane.showOptionDialog(this, selectedEditor
 					.getFileName()
 					+ java.util.ResourceBundle.getBundle(
 							"de/unisiegen/tpml/ui/ui").getString("WantTosave"),
 					java.util.ResourceBundle.getBundle(
 							"de/unisiegen/tpml/ui/ui").getString("Save_File"),
 					JOptionPane.YES_NO_CANCEL_OPTION,
 					JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
 			switch (n) {
 			case 0: // Save Changes
 				logger.debug("Close dialog: YES");
 				success = selectedEditor.handleSave();
 				if (success) {
 					this.tabbedPane.remove(tabbedPane.getSelectedIndex());
 					this.repaint();
 				}
 				return success;
 
 			case 1: // Do not save changes
 				logger.debug("Close dialog: NO");
 				this.tabbedPane.remove(tabbedPane.getSelectedIndex());
 				this.repaint();
 				success = true;
 
 			case 2: // Cancelled.
 				logger.debug("Close dialog: CANCEL");
 				success = false;
 
 			default:
 				success = false;
 			}
 		} else {
 			this.tabbedPane.remove(tabbedPane.getSelectedIndex());
 			this.repaint();
 			success = true;
 		}
 		if (getActiveEditor() == null) {
 			setGeneralStates(false);
 			saveItem.setEnabled(false);
 			saveButton.setEnabled(false);
 		}
 		return success;
 	}
 
 	private void handleSaveAll() {
 		int tabcount = tabbedPane.getComponentCount();
 		for (int i = 0; i < tabcount; i++) {
 			if (!((EditorPanel) tabbedPane.getComponentAt(i)).handleSave())
 				return;
 		}
 	}
 
 	/**
 	 * Stores the list of open files for the next start (see
 	 * {@link #restoreOpenFiles()}), that is the list of files from the
 	 * {@link EditorPanel}s that have valid <code>File</code> objects.
 	 * 
 	 * This is called exactly once on quit.
 	 * 
 	 * @see #restoreOpenFiles()
 	 */
 	public void saveOpenFiles() {
 		int tabcount = tabbedPane.getComponentCount();
 		LinkedList<File> filelist = new LinkedList<File>();
 		File file;
 		for (int i = 0; i < tabcount; i++) {
 			file = ((EditorPanel) tabbedPane.getComponentAt(i)).getFile();
 			if (file != null) {
 				filelist.add(file);
 			}
 		}
 		PreferenceManager.get().setOpenFiles(filelist);
 	}
 
 	/**
 	 * Restores the list of open files from a previous session, previously saved
 	 * by the {@link #saveOpenFiles()} method.
 	 * 
 	 * This is called on startup if no files where provided.
 	 * 
 	 * @see #saveOpenFiles()
 	 */
 	public void restoreOpenFiles() {
 		LinkedList<File> filelist = PreferenceManager.get().getOpenFiles();
 		File currentfile;
 		for (int i = 0; i < filelist.size(); i++) {
 			currentfile = filelist.get(i);
 			if (currentfile.exists() && currentfile.canRead()) {
 				openFile(currentfile);
 			}
 		}
 	}
 }
