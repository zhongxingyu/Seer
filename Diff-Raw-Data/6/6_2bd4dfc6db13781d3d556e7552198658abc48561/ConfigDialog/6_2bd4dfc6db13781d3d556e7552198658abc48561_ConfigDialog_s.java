 package ms.client.ui.dialogs;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JRootPane;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.filechooser.FileFilter;
 
 import ms.client.ui.Constants;
 import ms.client.utils.ConfigHandler;
 import ms.client.utils.I18NManager;
 
 /**
  * configuration dialog
  * - custom cdripper
  * - custom import folder
  * 		
  * @author david
  *
  */
 public class ConfigDialog extends JDialog {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private ConfigHandler config = ConfigHandler.getHandler();
 	private I18NManager manager = I18NManager.getManager();
 	private JTextField ripperTextField, folderTextField;
 	private final String audioripper = manager.getString("Config.audiograbber"), defaultfolder = manager.getString("Config.defaultfolder");
 	private final String save = manager.getString("save"), close = manager.getString("close");
 	private JLabel folderNotValidLabel = getNotValidLabel(new Point(100, 10));
 	private JLabel ripperNotValidLabel = getNotValidLabel(new Point(100, 85));
 
 	public ConfigDialog() {
 		initGUI();
 	}
 
 	/**
 	 * init GUI
 	 */
 	private void initGUI() {
 		initDialog();
 
 		addPanels();
 		addESCListener();
 		addButtons();
 		
 		loadProperties();
 		
 		showPathNotValidLabel();
 		showFileNotValidLabel();
 	}
 
 	private void initDialog() {
 		setTitle(Constants.PROGRAM + " - " + manager.getString("Config.title"));
 		setSize(400, 230);
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);
 		setLayout(null);
 		setResizable(false);
 		setModal(true);
 		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		setIconImage(new ImageIcon(getClass().getResource(Constants.UIIMAGE + Constants.ICON)).getImage());
 	}
 	
 	private void addPanels() {
 		final String[] label = { defaultfolder, audioripper };
 		final String[] icons = { Constants.DEFAULTFOLDER, Constants.AUDIORIPPER };
 		final int x=0;
 		final int[] y= { 10, 85 };
 		for(int i=0; i<label.length;i++) {
 			createBorder(label[i], new Rectangle(x, y[i], 395, 70));
 			createLabel(icons[i], new Rectangle(x+12, y[i]+20, 40, 40));
 			
 			JButton icon = createOpenButton(label[i], new Rectangle(355, y[i]+30, 22, 22));
 			icon.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					if(e.getActionCommand() == defaultfolder) {
 						openDefaultFolderFileChooser();
 					} else {
 						openAudioRipperDirChooser();
 					}
 				}
 			});
 		}
 		addDefaultFolderTextField();
 		addAudioRipperTextField();
 	}
 
 	private void addDefaultFolderTextField() {
 		folderTextField = createTextField(new Point(60, 40));
 		folderTextField.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyReleased(KeyEvent e) {
 				showPathNotValidLabel();
 			}
 		});
 		folderTextField.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent e) {
 				if((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2))
 					openDefaultFolderFileChooser();
 			}
 		});
 	}
 
 	private void addAudioRipperTextField() {
 		ripperTextField = createTextField(new Point(60, 115));
 		ripperTextField.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyReleased(KeyEvent e) {
 				showFileNotValidLabel();
 			}
 		});
 		ripperTextField.addMouseListener(new MouseAdapter() {
 			@Override
 			 public void mousePressed(MouseEvent e) {
 				if((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2))
 					openAudioRipperDirChooser();
 			}
 		});
 	}
 	
 	private JButton createOpenButton(String name, Rectangle rec) {
 		JButton button = new JButton();
 		button.setActionCommand(name);
 		button.setIcon(new ImageIcon(getClass().getResource(Constants.OPEN)));
 		button.setBounds(rec);
 		button.setToolTipText(manager.getString("choosedir"));
 		add(button);
 		return button;
 	}
 
 	private void createLabel(String icon, Rectangle rec) {
 		JLabel label = new JLabel();
 		label.setIcon(new ImageIcon(getClass().getResource(icon)));
 		label.setBounds(rec);
 		add(label);
 	}
 
 	private void createBorder(String border, Rectangle rec) {
 		JPanel panel = new JPanel();
 		panel.setLayout(null);
 		panel.setBorder(BorderFactory.createTitledBorder(border));
 		panel.setOpaque(false);
 		panel.setBounds(rec);
 		add(panel);
 	}
 
 	private JTextField createTextField(Point p) {
 		final JTextField textField = new JTextField();
 		textField.setSize(290, 22);
 		textField.setLocation(p);
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
 	protected void addButtons() {
 		int x = 135;
 		int y = 165;
 		int width = 115;
 		int height = 25;
 		final Rectangle okBounds = new Rectangle(x, y, width, height);
 		final Rectangle cancelBounds = new Rectangle(x + width + 10, y, width, height);
 		final Rectangle[] bounds = { okBounds, cancelBounds };
 		final String[] buttonText = { save, close };
 		final String[] icons = { Constants.SAVE, Constants.CANCEL };
 		final int saveMnemonic = manager.getMnemonic("save"), cancelMnemonic = manager.getMnemonic("close");
 		final int[] mnemonic = { saveMnemonic, cancelMnemonic };
 		for (int i = 0; i < buttonText.length; i++) {
 			JButton button = new JButton();
 			button.setBounds(bounds[i]);
 			button.setText(buttonText[i]);
 			button.setMnemonic(mnemonic[i]);
 			button.setIcon(new ImageIcon(getClass().getResource(Constants.UIIMAGE + icons[i])));
 		    button.setVerticalTextPosition(JButton.CENTER);
 		    button.setHorizontalTextPosition(JButton.RIGHT);
 			button.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					if (e.getActionCommand() == save) {
 						saveAndClose();
 					} else if (e.getActionCommand() == close) {
 						close();
 					}
 				}
 			});
 			add(button);
 		}
 	}
 	
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
 		if(!ripperTextField.getText().isEmpty())
 			config.setProperty(Constants.AUDIORIPPERCFG, ripperTextField.getText());
 		if(!folderTextField.getText().isEmpty())
 			config.setProperty(Constants.DEFAULTFOLDERCFG, folderTextField.getText());
 	}
 	
 	/**
 	 * load properties.
 	 */
 	private void loadProperties() {
 		if(config.containsKey(Constants.AUDIORIPPERCFG))
 			ripperTextField.setText(config.getProperty(Constants.AUDIORIPPERCFG).trim());
 		if(config.containsKey(Constants.DEFAULTFOLDERCFG))
 			folderTextField.setText(config.getProperty(Constants.DEFAULTFOLDERCFG).trim());
 	}
 	
 	private void openAudioRipperDirChooser() {
 		JFileChooser fileChooser = new JFileChooser();
 		fileFilter(fileChooser);
 		openDialog(fileChooser, ripperTextField);
 		showFileNotValidLabel();
 	}
 	
 	private void openDefaultFolderFileChooser() {
 		JFileChooser dirChooser = new JFileChooser();
 		dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		dirChooser.setAcceptAllFileFilterUsed(false);
 		openDialog(dirChooser, folderTextField);
 		showPathNotValidLabel();
 	}
 
 	private void openDialog(JFileChooser fileChooser, JTextField textField) {
 		if(!textField.getText().isEmpty()) {
 			fileChooser.setCurrentDirectory(new File(textField.getText()));
 		}
 		int returnVal = fileChooser.showOpenDialog(null);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			textField.setText(fileChooser.getSelectedFile().getAbsolutePath().trim());
 		}
 	}
 	
 	private JLabel getNotValidLabel(Point p) {
 		JLabel label = new JLabel(manager.getString("Config.notvalid"));
 		label.setSize(120, 25);
 		label.setLocation(p);
 		label.setForeground(Color.RED);
 		label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
 		label.setHorizontalAlignment(JLabel.CENTER);
 		label.setBorder(BorderFactory.createLineBorder(Color.RED));
 		label.setVisible(true);
 		add(label, 0);
 		return label;
 	}
 
 	private void fileFilter(JFileChooser fileChooser) {
 		if(System.getProperty("os.name").toLowerCase().contains("windows")) {
 			fileChooser.setFileFilter(new FileFilter() {
 				public boolean accept(File file) {
 					return file.getName().toLowerCase().endsWith(".exe") || file.isDirectory();
 				}
 				public String getDescription() {
 					return "*.exe";
 				}
 			});
 		}
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
 		JRootPane rootPane = getRootPane();
 		rootPane.registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
 	}
 
 	/**
 	 * close
 	 */
 	private void close() {
 		setVisible(false);
 		dispose();
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
 
 	private void showPathNotValidLabel() {
 		String text = folderTextField.getText();
 		File f = new File(text);
 		if(f.exists() && f.isDirectory()) {
 			folderNotValidLabel.setVisible(false);
 		} else {
 			folderNotValidLabel.setVisible(true);
 		}
 	}
 	
 	private void showFileNotValidLabel() {
 		String text = ripperTextField.getText();
 		File f = new File(text);
		if(f.exists() && f.isFile() && text.endsWith("exe")) {
 			ripperNotValidLabel.setVisible(false);
 		} else {
 			ripperNotValidLabel.setVisible(true);
 		}
 	}
 }
