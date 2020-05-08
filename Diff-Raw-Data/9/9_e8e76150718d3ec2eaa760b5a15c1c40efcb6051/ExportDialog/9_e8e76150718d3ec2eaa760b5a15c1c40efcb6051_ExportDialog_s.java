 package ms.ui.dialogs.server;
 
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.net.URL;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JRootPane;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 
 import ms.application.server.ServerController;
 import ms.domain.Auftrag;
 import ms.domain.LaufendeAuftragsListe;
 import ms.domain.MediaStopfListe;
 import ms.ui.Constants;
 import ms.ui.server.ServerConstants;
 import ms.utils.ConfigHandler;
 import ms.utils.I18NManager;
 import ms.utils.ui.Button;
 import ms.utils.ui.Dialog;
 import ms.utils.ui.Label;
 import ms.utils.ui.Panel;
 import ms.utils.ui.TextField;
 
 /**
  * dialog to choose a destination, where the files should be copied
  */
 public class ExportDialog extends Dialog {
 
 	private static final long serialVersionUID = 1L;
 	private ConfigHandler config = ConfigHandler.getClientHandler();
 	private I18NManager manager = I18NManager.getManager();
 	private final String exportFolder = manager.getString("Exporter.exportstorage");
 	private final String export = manager.getString("export"), close = manager.getString("close");
 	private JLabel folderNotValidLabel = getNotValidLabel(140, 10);
 	private JTextField exportTextField;
 	private int taskID;
 	private MediaStopfListe taskList; 
 	private LaufendeAuftragsListe exportTaskList;
 
 	public ExportDialog(int taskID, MediaStopfListe taskList, LaufendeAuftragsListe exportTaskList) {
 		this.taskID = taskID;
 		this.taskList = taskList;
 		this.exportTaskList = exportTaskList;
 		
 		initGUI();
 	}
 
 	/**
 	 * init GUI
 	 */
 	private void initGUI() {
 		initDialog();
 
 		addESCListener();
 		addButtons();
 		addDefaultFolderPanel();
 		
 		loadProperties();
 		
 		showPathNotValidLabel();
 	}
 
 	private void initDialog() {
 		String title = ServerConstants.PROGRAM + " - " + export;
 		URL icon = getClass().getResource(Constants.UIIMAGE + Constants.ICON);
 		super.initDialog(title, icon, new Dimension(400, 150));
 	}
 
 	private void addDefaultFolderPanel() {
 		JPanel panel = new Panel(new Rectangle(0, 10, 395, 70), BorderFactory.createTitledBorder(exportFolder));
 		panel.setOpaque(false);
 		add(panel);
 		
 		add(new Label(getClass().getResource(Constants.UIIMAGE + Constants.EXPORT_L), new Rectangle(12, 30, 40, 40)));
 		
 		exportTextField = createTextField(60, 40);
 		exportTextField.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyReleased(KeyEvent e) {
 				showPathNotValidLabel();
 			}
 		});
 		exportTextField.addMouseListener(new MouseAdapter() {
 			@Override
 			 public void mousePressed(MouseEvent e) {
 				if((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2))
 					openExportFileChooser();
 			}
 		});
 		createOpenButton(new Rectangle(355, 40, 22, 22));
 	}
 	
 	private void createOpenButton(Rectangle bounds) {
 		JButton button = new Button(bounds, getClass().getResource(Constants.OPEN), manager.getString("Exporter.choosedir"));
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				openExportFileChooser();
 			}
 		});
 		add(button);
 	}
 
 	private JTextField createTextField(int x, int y) {
 		final JTextField textField = new TextField(new Rectangle(x, y, 290, 22));
 		textField.setComponentPopupMenu(addPopUpMenu(textField));
 		textField.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				textField.requestFocus();
 				textField.selectAll();
 			}
 		});
 		add(textField);
 		return textField;
 	}
 	
 
 	/**
 	 * add buttons
 	 */
 	private void addButtons() {
 		final int x = 135;
 		final int y = 90;
 		final int width = 115;
 		final int height = 25;
 		final Rectangle sendBounds = new Rectangle(x, y, width, height);
 		final Rectangle cancelBounds = new Rectangle(x + width + 10, y, width, height);
 		final Rectangle[] bounds = { sendBounds, cancelBounds };
 		final String[] buttonText = { export, close };
 		final URL[] icons = { getClass().getResource(Constants.UIIMAGE + Constants.TICK), getClass().getResource(Constants.UIIMAGE + Constants.CANCEL) };
 		final int exportMnemonic = manager.getMnemonic("export"), cancelMnemonic = manager.getMnemonic("close");
 		final int[] mnemonic = { exportMnemonic, cancelMnemonic };
 		for (int i = 0; i < buttonText.length; i++) {
 			JButton button = new Button(bounds[i], buttonText[i], mnemonic[i], icons[i]);
 			button.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					if (e.getActionCommand() == export) {
 						export();
 						saveAndClose();
 					} else if (e.getActionCommand() == close) {
 						close();
 					}
 				}
 			});
 			add(button);
 		}
 	}
 	
 	private void export() {
 		final File exportFolder = new File(exportTextField.getText().trim());
 		final File file = new File(Integer.toString(taskID));
		final int _auftragsid = taskID;
 		Auftrag auftrag = taskList.getbyAuftragsNr(taskID);
 		auftrag.setStatus(4);
 		final Auftrag _auftrag = auftrag;
 		taskList.removebyId(taskID);
 		
 		Thread copyThread = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				ServerController.getInstance().copyFiles(file.listFiles(), exportFolder);
 			}
 		});
 		copyThread.start();
 		Thread t = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				while(true) {
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 					if(file.listFiles().length == exportFolder.listFiles().length) {
 						exportTaskList.clean();
 						_auftrag.setStatus(4);
 						break;
 					}
 				}
 			}
 		});
 		t.start();
 	}
 	
 	/**
 	 * close
 	 */
 	private void saveAndClose() {
 		saveProperties();
 		close();
 	}
 
 	/**
 	 * save properties.
 	 */
 	private void saveProperties() {
 		saveValues();
 		config.save();
 	}
 
 	private void saveValues() {
 		if(!exportTextField.getText().isEmpty())
 			config.setProperty(Constants.EXPORTCFG, exportTextField.getText().trim());
 	}
 	
 	/**
 	 * load properties.
 	 */
 	private void loadProperties() {
 		if(config.containsKey(Constants.EXPORTCFG))
 			exportTextField.setText(config.getProperty(Constants.EXPORTCFG));
 	}
 	
 	private void openExportFileChooser() {
 		JFileChooser dirChooser = new JFileChooser();
 		dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		dirChooser.setAcceptAllFileFilterUsed(false);
 		openDialog(dirChooser, exportTextField);
 		showPathNotValidLabel();
 	}
 
 	private void openDialog(JFileChooser dirChooser, JTextField textField) {
 		if(!textField.getText().isEmpty()) {
 			dirChooser.setCurrentDirectory(new File(textField.getText()));
 		}
 		int returnVal = dirChooser.showOpenDialog(null);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			textField.setText(dirChooser.getSelectedFile().getAbsolutePath().trim());
 		}
 	}
 	
 	/**
 	 * PopupMenu
 	 * 
 	 * @param textField
 	 * @return JPopupMenu
 	 */
 	private JPopupMenu addPopUpMenu(final JTextField textField) {
 		JPopupMenu popupMenu = new JPopupMenu();
 		final String clear = manager.getString("clear"), cut = manager.getString("cut"),
 				copy = manager.getString("copy"), paste = manager.getString("paste"), selectAll = manager.getString("selectall"); 
 		final String[] menuItems = new String[] { clear, cut, copy, paste, selectAll };
 		for (int i = 0; i < menuItems.length; i++) {
 			JMenuItem menuItem = new JMenuItem(menuItems[i]);
 			if (i == 1 || i == 4) {
 				popupMenu.addSeparator();
 			}
 			menuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent event) {
 					if (event.getActionCommand() == cut) {
 						textField.cut();
 					} else if (event.getActionCommand() == copy) {
 						textField.copy();
 					} else if (event.getActionCommand() == paste) {
 						textField.paste();
 					} else if (event.getActionCommand() == clear) {
 						textField.setText("");
 					} else {
 						textField.selectAll();
 					}
 				}
 			});
 			popupMenu.add(menuItem);
 		}
 		return popupMenu;
 	}
 
 	/**
 	 * esc = close dialog
 	 */
 	private void addESCListener() {
 		ActionListener cancelListener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				close();
 			}
 		};
 		JRootPane rootPane = this.getRootPane();
 		rootPane.registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
 	}
 
 	private void close() {
 		setVisible(false);
 		dispose();
 	}
 	
 	private JLabel getNotValidLabel(int x, int y) {
 		String text = manager.getString("Exporter.notvalid");
 		JLabel label = new Label(text, new Rectangle(x, y, 120, 25));
 		label.setVisible(true);
 		add(label, 0);
 		return label;
 	}
 	
 	private void showPathNotValidLabel() {
 		String text = exportTextField.getText();
 		File f = new File(text);
 		if(f.exists() && f.isDirectory()) {
 			folderNotValidLabel.setVisible(false);
 		} else {
 			folderNotValidLabel.setVisible(true);
 		}
 	}
 }
