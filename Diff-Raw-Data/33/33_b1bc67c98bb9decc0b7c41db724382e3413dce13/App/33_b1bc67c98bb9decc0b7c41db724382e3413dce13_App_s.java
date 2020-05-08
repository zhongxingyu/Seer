 package gui;
 
 import middleend.*;
 import backbone.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.Component;
 import java.awt.Event;
 import java.awt.BorderLayout;
 import javax.swing.KeyStroke;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JMenuItem;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JFrame;
 import javax.swing.JDialog;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JToolBar;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import exception.InvalidRoundException;
 
 public class App implements GUIConstants {
 
 	private MiddleEnd _middleEnd;
 	private JFrame _jFrame;
 	private JToolBar _toolbar;
 	private JPanel _mainContentAndToolbarPane;
 	private JComponent _mainContentPane;
 	private InputPanel _inputPane;
 	private ManagementPanel _managementPane;
 	private JMenuBar _jJMenuBar;
 	private JMenu _fileMenu;
 	private JMenuItem _newTournamentMenuItem;
 	private JMenuItem _openTournamentMenuItem;
 	private JMenuItem _saveTournamentMenuItem;
 	private JMenuItem _importCategoryMenuItem;
 	private JMenuItem _exportCategoryMenuItem;
 	private JMenuItem _exitMenuItem;
 	private JMenu _optionsMenu;
 	private JMenuItem _programOptionsMenuItem;
 	private JDialog _programOptionsDialog;
 	private JPanel _programOptionsContentPane;
 	private JMenuItem _pluginOptionsMenuItem;
 	private JDialog _pluginOptionsDialog;
 	private JPanel _pluginOptionsContentPane;
 	private JMenu _editMenu;
 	private JMenu _viewMenu;
 	private JRadioButtonMenuItem _viewInputMenuItem;
 	private JRadioButtonMenuItem _viewManagementMenuItem;
 	private JMenu _helpMenu;
 	private JMenuItem _aboutMenuItem;
 	private JDialog _aboutDialog;
 	private JPanel _aboutContentPane;
 	private JLabel _aboutVersionLabel;
 	
 	public App(MiddleEnd me) {
 		_middleEnd = me;
 		this.getJFrame().setVisible(true);
 	}
 	
 	public MiddleEnd getMiddleEnd() {
 		return _middleEnd;
 	}
 	
 	/**
 	 * This method initializes jFrame
 	 * 
 	 * @return javax.swing.JFrame
 	 */
 	public JFrame getJFrame() {
 		if (_jFrame == null) {
 			_jFrame = new JFrame();
 			_jFrame.setJMenuBar(getJJMenuBar());
 			_jFrame.setSize(DEFAULT_SIZE);
 			_jFrame.setMinimumSize(MIN_SIZE);
 			_jFrame.addWindowListener(new WindowAdapter() {
 				public void windowClosing(WindowEvent e) {
 					_middleEnd.closeThisMiddleEnd();
 				}
 			});
 			System.out.println(System.getProperty("user.dir"));
 			if (IMAGESON) {
 				if (FRAMEIMAGE != null)
 					_jFrame.setIconImage(FRAMEIMAGE.getImage());
 			}
 			getViewInputMenuItem().doClick();
 			_jFrame.setContentPane(getMainContentAndToolbarPane());
 			getViewInputMenuItem().setSelected(true);
 			_jFrame.setTitle("TurtleTab v1.0");
 		}
 		return _jFrame;
 	}
 	
 	/**
 	 * This method initializes JToolBar
 	 * 
 	 * @return JToolBar
 	 */
 	public JToolBar getToolbar() {
 		if (_toolbar == null) {
 			_toolbar = new JToolBar();
 			_toolbar.setSize(TOOLBAR_SIZE);
 			_toolbar.setMinimumSize(TOOLBAR_SIZE);
 			_toolbar.setMaximumSize(TOOLBAR_SIZE);
 			for (int i = 0; i < getEditMenu().getMenuComponentCount(); i++) {
 				Component comp = getEditMenu().getMenuComponent(i);
 				if (comp instanceof JMenuItem) {
 					_toolbar.add(createButtonFromMenuItem((JMenuItem) comp));
 					_toolbar.add(Box.createRigidArea(SMALLSPACING_SIZE));
 				}
 			}
 		}
 		return _toolbar;
 	}
 	
 	/**
 	 * Creates a JButton from a JMenuItem
 	 * 
 	 * @param JMenuItem
 	 * @return JButton
 	 */
 	public JButton createButtonFromMenuItem(final JMenuItem item) {
 		JButton button = new JButton(item.getText());
 		if (IMAGESON)
 			button.setIcon(ADDBUTTONIMAGE);
 		if (COLORSON) {
 			button.setBackground(BACKGROUND_COLOR);
 			button.setForeground(FOREGROUND_COLOR);
 		}
 		button.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				item.doClick();
 			}
 		});
 		return button;
 	}
 	
 	public JPanel getMainContentAndToolbarPane() {
 		if (_mainContentAndToolbarPane == null) {
 			_mainContentAndToolbarPane = new JPanel();
 			if (COLORSON) {
 				_mainContentAndToolbarPane.setBackground(BACKGROUND_COLOR);
 				_mainContentAndToolbarPane.setForeground(FOREGROUND_COLOR);
 			}
 			_mainContentAndToolbarPane.setLayout(new BorderLayout());
 			_mainContentAndToolbarPane.add(getToolbar(), BorderLayout.NORTH);
 			_mainContentAndToolbarPane.add(getMainContentPane(), BorderLayout.CENTER);
 		}
 		return _mainContentAndToolbarPane;
 	}
 	
 	public void setMainContentPane(JComponent pane) {
 		getMainContentPane().removeAll();
 		getMainContentPane().add(pane, BorderLayout.CENTER);
 	}
 	
 	public JComponent getMainContentPane() {
 		if (_mainContentPane == null) {
 			_mainContentPane = new JPanel();
 			if (COLORSON) {
 				_mainContentPane.setBackground(BACKGROUND_COLOR);
 				_mainContentPane.setForeground(FOREGROUND_COLOR);
 			}
 			_mainContentPane.setLayout(new BorderLayout());
 			_mainContentPane.add(getInputPanel(), BorderLayout.CENTER);
 		}
 		return _mainContentPane;
 	}
 	
 	/**
 	 * This method initializes InputPanel
 	 * 
 	 * @return InputPanel
 	 */
 	public InputPanel getInputPanel() {
 		if (_inputPane == null) {
 			_inputPane = new InputPanel(getMiddleEnd());
 		}
 		return _inputPane;
 	}
 	
 	/**
 	 * This method initializes ManagementPanel
 	 * 
 	 * @return MangementPanel
 	 */
 	public ManagementPanel getManagementPanel() {
 		if (_managementPane == null) {
 			_managementPane = new ManagementPanel(getMiddleEnd());
 		}
 		return _managementPane;
 	}
 
 	/**
 	 * This method initializes jJMenuBar	
 	 * 	
 	 * @return javax.swing.JMenuBar	
 	 */
 	public JMenuBar getJJMenuBar() {
 		if (_jJMenuBar == null) {
 			_jJMenuBar = new JMenuBar();
 			_jJMenuBar.add(getFileMenu());
 			_jJMenuBar.add(getOptionsMenu());
 			_jJMenuBar.add(getViewMenu());
 			_jJMenuBar.add(getEditMenu());
 			_jJMenuBar.add(getHelpMenu());
 		}
 		return _jJMenuBar;
 	}
 
 	/**
 	 * This method initializes jMenu	
 	 * 	
 	 * @return javax.swing.JMenu	
 	 */
 	public JMenu getFileMenu() {
 		if (_fileMenu == null) {
 			_fileMenu = new JMenu();
 			_fileMenu.setText("File");
 			_fileMenu.add(getNewTournamentMenuItem());
 			_fileMenu.add(getOpenTournamentMenuItem());
 			_fileMenu.add(getSaveTournamentMenuItem());
 			_fileMenu.add(getImportCategoryMenuItem());
 			_fileMenu.add(getExportCategoryMenuItem());
 			_fileMenu.add(getExitMenuItem());
 		}
 		return _fileMenu;
 	}
 	
 	/**
 	 * This method initializes jMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	public JMenuItem getNewTournamentMenuItem() {
 		if (_newTournamentMenuItem == null) {
 			_newTournamentMenuItem = new JMenuItem();
 			_newTournamentMenuItem.setText("New Tournament...");
 			_newTournamentMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
 					Event.CTRL_MASK, true));
 			_newTournamentMenuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					_middleEnd.openNewMiddleEnd();
 				}
 			});
 		}
 		return _newTournamentMenuItem;
 	}
 	
 	/**
 	 * This method initializes jMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	public JMenuItem getOpenTournamentMenuItem() {
 		if (_openTournamentMenuItem == null) {
 			_openTournamentMenuItem = new JMenuItem();
 			_openTournamentMenuItem.setText("Open Tournament...");
 			_openTournamentMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
 					Event.CTRL_MASK, true));
 			_openTournamentMenuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					JFileChooser chooser = new JFileChooser();
 					chooser.setFileFilter(new FileNameExtensionFilter("Tournament File (.tmnt)", TOURNAMENT_EXTENSION));
 					int returnval = chooser.showOpenDialog(getJFrame());
 					if (returnval == JFileChooser.APPROVE_OPTION) {
 						if (!getMiddleEnd().openTournament(chooser.getSelectedFile())) {
 							JOptionPane.showMessageDialog(_jFrame, "The selected file was not a valid tournament file.", "Error", JOptionPane.ERROR_MESSAGE);
 						}
 						else {
 							_middleEnd.repaintAll();
 						}
 					}
 				}
 			});
 		}
 		return _openTournamentMenuItem;
 	}
 	
 	/**
 	 * This method initializes jMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	public JMenuItem getSaveTournamentMenuItem() {
 		if (_saveTournamentMenuItem == null) {
 			_saveTournamentMenuItem = new JMenuItem();
 			_saveTournamentMenuItem.setText("Save Tournament...");
 			_saveTournamentMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
 					Event.CTRL_MASK, true));
 			_saveTournamentMenuItem.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					JFileChooser chooser = new JFileChooser();
 					chooser.setFileFilter(new FileNameExtensionFilter("Tournament File (.tmnt)", TOURNAMENT_EXTENSION));
 					int returnval = chooser.showSaveDialog(getJFrame());
 					if (returnval == JFileChooser.APPROVE_OPTION) {
 						if (!getMiddleEnd().saveTournament(chooser.getSelectedFile())) {
 							JOptionPane.showMessageDialog(_jFrame, "The name specified for the file was invalid.", "Error", JOptionPane.ERROR_MESSAGE);
 						}
 						else {
 							_middleEnd.repaintAll();
 						}
 					}
 				}
 			});
 		}
 		return _saveTournamentMenuItem;
 	}
 	
 	/**
 	 * This method initializes jMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	public JMenuItem getImportCategoryMenuItem() {
 		if (_importCategoryMenuItem == null) {
 			_importCategoryMenuItem = new JMenuItem();
 			_importCategoryMenuItem.setText("Import Category...");
 			_importCategoryMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
 					Event.CTRL_MASK, true));
 			_importCategoryMenuItem.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					JFileChooser chooser = new JFileChooser();
 					chooser.setFileFilter(new FileNameExtensionFilter("CSV File (.csv)", CATEGORY_EXTENSION));
 					int returnval = chooser.showOpenDialog(getJFrame());
 					if (returnval == JFileChooser.APPROVE_OPTION) {
 						if (!getMiddleEnd().importCategory(chooser.getSelectedFile())) {
 							JOptionPane.showMessageDialog(_jFrame, "The name specified for the file was invalid.", "Error", JOptionPane.ERROR_MESSAGE);
 						}
 						else {
 							_middleEnd.repaintAll();
 						}
 					}
 				}
 			});
 		}
 		return _importCategoryMenuItem;
 	}
 	
 	/**
 	 * This method initializes jMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	public JMenuItem getExportCategoryMenuItem() {
 		if (_exportCategoryMenuItem == null) {
 			_exportCategoryMenuItem = new JMenuItem();
 			_exportCategoryMenuItem.setText("Export Category...");
 			_exportCategoryMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
 					Event.CTRL_MASK, true));
 			_exportCategoryMenuItem.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					ArrayList<Grouping> categories = new ArrayList<Grouping>(_middleEnd.getTournament().getCategories());
 					ArrayList<String> catnames = new ArrayList<String>();
 					for (Grouping<Unit> category : categories)
 						catnames.add(category.getName());
 					String str = (String) JOptionPane.showInputDialog(getJFrame(), "Which category would you like to export?", "Export Category",
 							JOptionPane.PLAIN_MESSAGE, null, catnames.toArray(new String[0]), catnames.get(0));
 					Grouping toadd = categories.get(catnames.indexOf(str));
 					JFileChooser chooser = new JFileChooser();
 					chooser.setFileFilter(new FileNameExtensionFilter("CSV File (.csv)", CATEGORY_EXTENSION));
 					int returnval = chooser.showSaveDialog(getJFrame());
 					if (returnval == JFileChooser.APPROVE_OPTION) {
 						if (!getMiddleEnd().exportCategory(toadd, chooser.getSelectedFile())) {
 							JOptionPane.showMessageDialog(_jFrame, "The selected file was not a valid tournament file.", "Error", JOptionPane.ERROR_MESSAGE);
 						}
 						else {
 							_middleEnd.repaintAll();
 						}
 					}
 				}
 			});
 		}
 		return _exportCategoryMenuItem;
 	}
 	
 	/**
 	 * This method initializes jMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	public JMenuItem getExitMenuItem() {
 		if (_exitMenuItem == null) {
 			_exitMenuItem = new JMenuItem();
 			_exitMenuItem.setText("Exit");
 			_exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
 					Event.CTRL_MASK, true));
 			_exitMenuItem.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					_middleEnd.closeThisMiddleEnd();
 				}
 			});
 		}
 		return _exitMenuItem;
 	}
 	
 	/**
 	 * This method initializes jMenu	
 	 * 	
 	 * @return javax.swing.JMenu	
 	 */
 	public JMenu getOptionsMenu() {
 		if (_optionsMenu == null) {
 			_optionsMenu = new JMenu();
 			_optionsMenu.setText("Options");
 			_optionsMenu.add(getProgramOptionsMenuItem());
 			_optionsMenu.add(getPluginOptionsMenuItem());
 		}
 		return _optionsMenu;
 	}
 	
 	/**
 	 * This method initializes jMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	public JMenuItem getPluginOptionsMenuItem() {
 		if (_pluginOptionsMenuItem == null) {
 			_pluginOptionsMenuItem = new JMenuItem();
 			_pluginOptionsMenuItem.setText("Plugin Options...");
 			_pluginOptionsMenuItem.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					JDialog pluginOptionsDialog = getPluginOptionsDialog();
 					pluginOptionsDialog.pack();
 					Point loc = getJFrame().getLocation();
 					loc.translate(20, 20);
 					pluginOptionsDialog.setLocation(loc);
 					pluginOptionsDialog.setVisible(true);
 				}
 			});
 		}
 		return _pluginOptionsMenuItem;
 	}
 	
 	/**
 	 * This method initializes _pluginOptionsPane	
 	 * 	
 	 * @return javax.swing.JDialog	
 	 */
 	private JDialog getPluginOptionsDialog() {
 		if (_pluginOptionsDialog == null) {
 			_pluginOptionsDialog = new JDialog(getJFrame());
 			_pluginOptionsDialog.setTitle("Plugin Options");
 			_pluginOptionsDialog.setContentPane(getPluginOptionsContentPane());
 		}
 		return _pluginOptionsDialog;
 	}
 
 	/**
 	 * This method initializes _pluginOptionsContentPane	
 	 * 	
 	 * @return javax.swing.JPanel	
 	 */
 	private JPanel getPluginOptionsContentPane() {
 		if (_pluginOptionsContentPane == null) {
 			_pluginOptionsContentPane = new JPanel();
 			if (COLORSON) {
 				_pluginOptionsContentPane.setBackground(BACKGROUND_COLOR);
 				_pluginOptionsContentPane.setForeground(FOREGROUND_COLOR);
 			}
 			_pluginOptionsContentPane.setLayout(new BorderLayout());
 		}
 		return _pluginOptionsContentPane;
 	}
 	
 	/**
 	 * This method initializes jMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	public JMenuItem getProgramOptionsMenuItem() {
 		if (_programOptionsMenuItem == null) {
 			_programOptionsMenuItem = new JMenuItem();
 			_programOptionsMenuItem.setText("Program Options...");
 			_programOptionsMenuItem.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					JDialog programOptionsDialog = getProgramOptionsDialog();
 					programOptionsDialog.pack();
 					Point loc = getJFrame().getLocation();
 					loc.translate(20, 20);
 					programOptionsDialog.setLocation(loc);
 					programOptionsDialog.setVisible(true);
 				}
 			});
 		}
 		return _programOptionsMenuItem;
 	}
 	
 	/**
 	 * This method initializes _optionsDialog	
 	 * 	
 	 * @return javax.swing.JDialog	
 	 */
 	private JDialog getProgramOptionsDialog() {
 		if (_programOptionsDialog == null) {
 			_programOptionsDialog = new JDialog(getJFrame());
 			_programOptionsDialog.setTitle("Program Options");
 			_programOptionsDialog.setContentPane(getProgramOptionsContentPane());
 		}
 		return _programOptionsDialog;
 	}
 
 	/**
 	 * This method initializes _optionsContentPane	
 	 * 	
 	 * @return javax.swing.JPanel	
 	 */
 	private JPanel getProgramOptionsContentPane() {
 		if (_programOptionsContentPane == null) {
 			_programOptionsContentPane = new JPanel();
 			if (COLORSON) {
 				_programOptionsContentPane.setBackground(BACKGROUND_COLOR);
 				_programOptionsContentPane.setForeground(FOREGROUND_COLOR);
 			}
 			_programOptionsContentPane.setLayout(new BorderLayout());
 		}
 		return _programOptionsContentPane;
 	}
 	
 	/**
 	 * This method initializes jMenu	
 	 * 	
 	 * @return javax.swing.JMenu	
 	 */
 	public JMenu getViewMenu() {
 		if (_viewMenu == null) {
 			_viewMenu = new JMenu();
 			_viewMenu.setText("View");
 			_viewMenu.add(getViewInputMenuItem());
 			_viewMenu.add(getViewManagementMenuItem());
 			ButtonGroup viewgroup = new ButtonGroup();
 			viewgroup.add(getViewInputMenuItem());
 			viewgroup.add(getViewManagementMenuItem());
 		}
 		return _viewMenu;
 	}
 
 	/**
 	 * This method initializes jMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	public JRadioButtonMenuItem getViewInputMenuItem() {
 		if (_viewInputMenuItem == null) {
 			_viewInputMenuItem = new JRadioButtonMenuItem();
 			_viewInputMenuItem.setText("View Input Panel");
 			_viewInputMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,	Event.CTRL_MASK, true));
 			_viewInputMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				@Override
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					getInputPanel().setSize(_jFrame.getContentPane().getSize());
 					setMainContentPane(getInputPanel());
 					getMainContentAndToolbarPane().repaint();
 					getInputPanel().repaintAll();
 				}
 			});
 		}
 		return _viewInputMenuItem;
 	}
 	
 	/**
 	 * This method initializes jMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	public JRadioButtonMenuItem getViewManagementMenuItem() {
 		if (_viewManagementMenuItem == null) {
 			_viewManagementMenuItem = new JRadioButtonMenuItem();
 			_viewManagementMenuItem.setText("View Management Panel");
 			_viewManagementMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, Event.CTRL_MASK, true));
 			_viewManagementMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				@Override
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					getManagementPanel().setSize(_jFrame.getSize());
 					setMainContentPane(getManagementPanel());
 					getMainContentAndToolbarPane().repaint();
 					getManagementPanel().repaintAll();
 				}
 			});
 		}
 		return _viewManagementMenuItem;
 	}
 	
 	/**
 	 * This method initializes jMenu	
 	 * 	
 	 * @return javax.swing.JMenu	
 	 */
 	public JMenu getEditMenu() {
 		if (_editMenu == null) {
 			_editMenu = new JMenu();
 			_editMenu.setText("Edit");
 			_editMenu.add(getCreateRoundMenuItem());
 			List<Grouping> groupings = _middleEnd.getTournament().getCategories();
 			for (Grouping<Unit> g : groupings) {
 				_editMenu.add(createEditMenuItem(g));
 			}
 		}
 		return _editMenu;
 	}
 	
 	/**
 	 * This method initializes jMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	public JMenuItem getCreateRoundMenuItem() {
 		JMenuItem item = new JMenuItem();
 		item.setText("Create new round from existing units...");
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try{
					_middleEnd.getTournament().createNextRound();
 					getViewManagementMenuItem().doClick();
 				}catch(InvalidRoundException err){
					err.printStackTrace();
 				}
 			}
 		});
 		return item;
 	}
 	
 	/**
 	 * This method initializes jMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	public JMenuItem createEditMenuItem(final Grouping<Unit> g) {
 		JMenuItem item = new JMenuItem();
 		item.setText("New " + g.getName() + "...");
 		item.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				getInputPanel().getAddingPanel().setAddPanel(g);
 				getViewInputMenuItem().doClick();
 			}
 		});
 		return item;
 	}
 	
 	/**
 	 * This method initializes jMenu	
 	 * 	
 	 * @return javax.swing.JMenu	
 	 */
 	public JMenu getHelpMenu() {
 		if (_helpMenu == null) {
 			_helpMenu = new JMenu();
 			_helpMenu.setText("Help");
 			_helpMenu.add(getAboutMenuItem());
 		}
 		return _helpMenu;
 	}
 	
 	/**
 	 * This method initializes jMenuItem	
 	 * 	
 	 * @return javax.swing.JMenuItem	
 	 */
 	public JMenuItem getAboutMenuItem() {
 		if (_aboutMenuItem == null) {
 			_aboutMenuItem = new JMenuItem();
 			_aboutMenuItem.setText("About...");
 			_aboutMenuItem.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					JDialog aboutDialog = getAboutDialog();
 					aboutDialog.pack();
 					Point loc = getJFrame().getLocation();
 					loc.translate(20, 20);
 					aboutDialog.setLocation(loc);
 					aboutDialog.setVisible(true);
 				}
 			});
 		}
 		return _aboutMenuItem;
 	}
 
 	/**
 	 * This method initializes aboutDialog	
 	 * 	
 	 * @return javax.swing.JDialog
 	 */
 	public JDialog getAboutDialog() {
 		if (_aboutDialog == null) {
 			_aboutDialog = new JDialog(getJFrame(), true);
 			_aboutDialog.setTitle("About");
 			_aboutDialog.setContentPane(getAboutContentPane());
 		}
 		return _aboutDialog;
 	}
 
 	/**
 	 * This method initializes aboutContentPane
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	public JPanel getAboutContentPane() {
 		if (_aboutContentPane == null) {
 			_aboutContentPane = new JPanel();
 			if (COLORSON) {
 				_aboutContentPane.setBackground(BACKGROUND_COLOR);
 				_aboutContentPane.setForeground(FOREGROUND_COLOR);
 			}
 			_aboutContentPane.setLayout(new BorderLayout());
 			_aboutContentPane.add(getAboutVersionLabel(), BorderLayout.CENTER);
 		}
 		return _aboutContentPane;
 	}
 
 	/**
 	 * This method initializes aboutVersionLabel	
 	 * 	
 	 * @return javax.swing.JLabel	
 	 */
 	public JLabel getAboutVersionLabel() {
 		if (_aboutVersionLabel == null) {
 			_aboutVersionLabel = new JLabel();
 			_aboutVersionLabel.setText("<html><center>Tournament Scheduler - Version 1.0<br>" +
 					"Created by Patrick Clay, Matthew Mahoney, and Aswin Karumbunathan</center></html>");
 			_aboutVersionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		}
 		return _aboutVersionLabel;
 	}
 	
 	/**
 	 * Repaints all the important components.
 	 */
 	public void repaintAll() {
 		getInputPanel().repaintAll();
 		getManagementPanel().repaintAll();
 		getMainContentAndToolbarPane().repaint();
 	}
 
 	/**
 	 * Launches this application
 	 *
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				App application = new App(new MiddleEnd());
 				application.getJFrame().setVisible(true);
 			}
 		});
 	}*/
 
 }
