 import java.awt.BorderLayout;
 import java.awt.Font;
 import java.awt.GraphicsEnvironment;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Scanner;
 
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 
 public class GUI extends JFrame {
 	private static final long serialVersionUID = 1L;
 	private final String PROGRAM_NAME = "Ryan's Notepad";
 	private final int WINDOW_WIDTH = 1024, WINDOW_HEIGHT = 768;
 	private JMenuBar menu;
 	private JMenu file, edit, help;
 	private JMenuItem new_, open, save, close, exit, properties, cut, copy,
 			paste, manual, about;
 	private JPanel main_pnl, toolbar, font_toolbar;
 	private JLabel font_name_lbl, font_size_lbl, font_color_lbl, bg_color_lbl;
 	private JComboBox font_name_box, font_size_box, font_color_box,
 			bg_color_box;
 
 	private JTabbedPane tab;
 	private int tab_count;
 	
 	@SuppressWarnings("unused") // Hehehe :)
 	private String clipboard;
 
 	public GUI() {
 		init();
 		this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
 		this.setTitle(PROGRAM_NAME);
 		this.setLocationRelativeTo(null);
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		this.setResizable(true);
 		this.setVisible(true);
 	}
 
 	private void init() {
 		clipboard = "";
 		tab_count = 0;
 		menu();
 		components();
 	}
 
 	private void menu() {
 		// Initialize
 		menu = new JMenuBar();
 		file = new JMenu("File");
 		edit = new JMenu("Edit");
 		help = new JMenu("Help");
 		new_ = new JMenuItem("New");
 		open = new JMenuItem("Open");
 		save = new JMenuItem("Save");
 		close = new JMenuItem("Close");
 		exit = new JMenuItem("Exit");
 		properties = new JMenuItem("Properties");
 		cut = new JMenuItem("Cut");
 		copy = new JMenuItem("Copy");
 		paste = new JMenuItem("Paste");
 		manual = new JMenuItem("Manual");
 		about = new JMenuItem("About");
 
 		// Set mnemonics
 		file.setMnemonic(KeyEvent.VK_F);
 		edit.setMnemonic(KeyEvent.VK_E);
 		help.setMnemonic(KeyEvent.VK_H);
 		new_.setMnemonic(KeyEvent.VK_N);
 		open.setMnemonic(KeyEvent.VK_O);
 		save.setMnemonic(KeyEvent.VK_S);
 		close.setMnemonic(KeyEvent.VK_C);
 		exit.setMnemonic(KeyEvent.VK_X);
 		properties.setMnemonic(KeyEvent.VK_P);
 		cut.setMnemonic(KeyEvent.VK_U);
 		copy.setMnemonic(KeyEvent.VK_O);
 		paste.setMnemonic(KeyEvent.VK_P);
 		manual.setMnemonic(KeyEvent.VK_M);
 		about.setMnemonic(KeyEvent.VK_A);
 
 		// Add listeners
 		new_.addActionListener(new NewAction());
 		open.addActionListener(new OpenAction());
 		save.addActionListener(new SaveAction());
 		close.addActionListener(new CloseAction());
 		exit.addActionListener(new ExitAction());
 		properties.addActionListener(new PropertiesAction());
 		cut.addActionListener(new CutAction());
 		copy.addActionListener(new CopyAction());
 		paste.addActionListener(new PasteAction());
 		manual.addActionListener(new ManualAction());
 		about.addActionListener(new AboutAction());
 
 		// Nest menus
 		menu.add(file);
 		menu.add(edit);
 		menu.add(help);
 		file.add(new_);
 		file.add(open);
 		file.add(save);
 		file.add(close);
 		file.add(exit);
 		edit.add(cut);
 		edit.add(copy);
 		edit.add(paste);
 		help.add(manual);
 		help.add(about);
 		this.setJMenuBar(menu);
 	}
 
 	private void components() {
 		this.setLayout(new BorderLayout());
 		toolbar = new JPanel(new BorderLayout());
 		font_toolbar = new JPanel(new GridBagLayout());
 		main_pnl = new JPanel(new BorderLayout());
 
 		font_name_lbl = new JLabel("Font");
 		font_name_box = new JComboBox(getAvailableFonts());
 		font_name_box.addActionListener(new FontNameAction());
 		font_size_lbl = new JLabel("Size");
 		font_size_box = new JComboBox(getFontSizes());
 		font_size_box.addActionListener(new FontSizeAction());
 		font_color_lbl = new JLabel("Color");
 		font_color_box = new JComboBox();
 		bg_color_lbl = new JLabel("Background");
 		bg_color_box = new JComboBox();
 
		addComponent(font_toolbar, font_name_lbl, 0, 0, 1, 1,
				GridBagConstraints.LINE_START);
		addComponent(font_toolbar, font_name_box, 1, 0, 1, 1,
				GridBagConstraints.LINE_START);
		addComponent(font_toolbar, font_size_lbl, 2, 0, 1, 1,
				GridBagConstraints.LINE_START);
		addComponent(font_toolbar, font_size_box, 3, 0, 1, 1,
				GridBagConstraints.LINE_START);
		addComponent(font_toolbar, font_color_lbl, 4, 0, 1, 1,
				GridBagConstraints.LINE_START);
		addComponent(font_toolbar, font_color_box, 5, 0, 1, 1,
				GridBagConstraints.LINE_START);
		addComponent(font_toolbar, bg_color_lbl, 6, 0, 1, 1,
				GridBagConstraints.LINE_START);
		addComponent(font_toolbar, bg_color_box, 7, 0, 1, 1,
				GridBagConstraints.LINE_START);
 		toolbar.add(font_toolbar, BorderLayout.WEST);
 		this.add(toolbar, BorderLayout.PAGE_START);
 
 		tab = new JTabbedPane();
 		tab.addTab("New Document #" + ++tab_count, new TabPanel());
 		main_pnl.add(tab);
 		this.add(main_pnl, BorderLayout.CENTER);
 		
 		font_name_box.setSelectedItem(((TabPanel) tab.getComponentAt(tab.getSelectedIndex())).getFont_name());
 		font_size_box.setSelectedItem(((TabPanel) tab.getComponentAt(tab.getSelectedIndex())).getFont_size());
 	}
 
 	private void addComponent(JPanel panel, JComponent component, int xPos,
 			int yPos, int width, int height, int align) {
 		GridBagConstraints grid = new GridBagConstraints();
 		grid.gridx = xPos;
 		grid.gridy = yPos;
 		grid.gridwidth = width;
 		grid.gridheight = height;
 		grid.anchor = align;
 		grid.insets = new Insets(3, 3, 3, 3);
 		grid.fill = GridBagConstraints.NONE;
 		panel.add(component, grid);
 	}
 
 	public String[] getAvailableFonts() {
 		GraphicsEnvironment ge = GraphicsEnvironment
 				.getLocalGraphicsEnvironment();
 		Font[] fonts = ge.getAllFonts();
 		String[] font_names = new String[fonts.length];
 		for (int f = 0; f < font_names.length; f++) {
 			font_names[f] = fonts[f].getFontName();
 		}
 		return font_names;
 	}
 	
 	public int getFontIndex() {
 		String[] font_names = getAvailableFonts();
 		int index = 0;
 		for (int f = 0; f < font_names.length; f++) {
 			if (font_names[f].equalsIgnoreCase(((TabPanel) tab.getComponentAt(tab.getSelectedIndex())).getFont_name())) {
 				index = f;
 			}
 		}
 		return index;
 	}
 
 	public String[] getFontSizes() {
 		String[] sizes = { "8", "9", "10", "11", "12", "14", "16", "18", "20",
 				"22", "24", "26", "28", "36", "48", "72" };
 		return sizes;
 	}
 	
 	private class FontNameAction implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 			((TabPanel) tab.getComponentAt(tab.getSelectedIndex())).setFont_name((String)font_name_box.getSelectedItem());
 			((TabPanel) tab.getComponentAt(tab.getSelectedIndex())).updateFont();
 		}
 
 	}
 	
 	private class FontSizeAction implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 			((TabPanel) tab.getComponentAt(tab.getSelectedIndex())).setFont_size(Integer.parseInt((String) font_size_box.getSelectedItem()));
 			((TabPanel) tab.getComponentAt(tab.getSelectedIndex())).updateFont();
 		}
 
 	}
 
 	private class NewAction implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 			tab.addTab("New Document #" + ++tab_count, new TabPanel());
 			tab.setSelectedIndex(tab.getTabCount() - 1);
 		}
 
 	}
 
 	private class OpenAction implements ActionListener {
 
 		public void open() {
 			// open up a file chooser (a dialog for the user to browse files to
 			// open)
 			JFileChooser open = new JFileChooser(
 					new File(".").getAbsolutePath());
 			// get the option that the user selected (approve or cancel)
 			int option = open.showOpenDialog(null);
 			// NOTE: because we are OPENing a file, we call showOpenDialog~
 			// if the user clicked OK, we have "APPROVE_OPTION"
 			// so we want to open the file
 			if (option == JFileChooser.APPROVE_OPTION) {
 				String filename = open.getSelectedFile().getPath();
 				String text = "";
 				try {
 					// create a scanner to read the file
 					// (getSelectedFile().getPath() will get the path to the
 					// file)
 					Scanner scan = new Scanner(new FileReader(filename));
 					while (scan.hasNext()) {
 						text += scan.nextLine() + "\n";
 					}
 				} catch (Exception ex) {
 					System.out.println(ex.getMessage());
 				}
 				TabPanel panel = new TabPanel();
 				panel.setText(text);
 				tab.addTab(filename, panel);
 				tab.setSelectedIndex(tab.getTabCount() - 1);
 			}
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			open();
 		}
 
 	}
 
 	private class SaveAction implements ActionListener {
 
 		public void save() {
 			// again, open a file chooser
 			JFileChooser save = new JFileChooser();
 			// similar to the open file, only this time we call showSaveDialog
 			// instead of showOpenDialog if the user clicked OK (and not cancel)
 			int option = save.showSaveDialog(null);
 
 			if (option == JFileChooser.APPROVE_OPTION) {
 				String filename = save.getSelectedFile().getPath();
 				String text = "";
 				try {
 					// create a buffered writer to write to a file
 					PrintWriter out = new PrintWriter(new FileWriter(filename));
 					text = ((TabPanel) tab.getComponentAt(tab
 							.getSelectedIndex())).getText();
 					tab.setTitleAt(tab.getSelectedIndex(), filename);
 					out.write(text);
 					// write the contents of the TextArea to the file
 					out.close(); // close the file stream
 				} catch (Exception ex) {
 					System.out.println(ex.getMessage());
 				}
 			}
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			if (tab.getTabCount() != 0) {
 				save();
 			}
 		}
 	}
 
 	private class CloseAction implements ActionListener {
 
 		public void close() {
 			String filename = tab.getTitleAt(tab.getSelectedIndex());
 			int response = JOptionPane.showConfirmDialog(null,
 					"Do you want to save the changes you made to\n" + filename
 							+ "?", PROGRAM_NAME, JOptionPane.YES_NO_OPTION,
 					JOptionPane.QUESTION_MESSAGE);
 			if (response == JOptionPane.NO_OPTION) {
 				// Don't save
 				tab.remove(tab.getSelectedIndex());
 			} else if (response == JOptionPane.YES_OPTION) {
 				if (filename.substring(0,  3).equalsIgnoreCase("New")) {
 					new SaveAction().save();
 				} else {
 					try {
 						PrintWriter out = new PrintWriter(new FileWriter(
 								filename));
 						String text = ((TabPanel) tab.getComponentAt(tab
 								.getSelectedIndex())).getText();
 						tab.setTitleAt(tab.getSelectedIndex(), filename);
 						out.write(text);
 						// write the contents of the TextArea to the file
 						out.close(); // close the file stream
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 				tab.remove(tab.getSelectedIndex());
 			} else if (response == JOptionPane.CLOSED_OPTION) {
 				// Close canceled
 			}
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			close();
 		}
 	}
 
 	private class ExitAction implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 			for (int i = tab.getTabCount() - 1; i >= 0; i--) {
 				String filename = tab.getTitleAt(i);
 				int response = JOptionPane
 						.showConfirmDialog(null,
 								"Do you want to save the changes you made to\n"
 										+ filename + "?", PROGRAM_NAME,
 								JOptionPane.YES_NO_OPTION,
 								JOptionPane.QUESTION_MESSAGE);
 				if (response == JOptionPane.NO_OPTION) {
 					// Don't save
 					tab.remove(i);
 				} else if (response == JOptionPane.YES_OPTION) {
 					new SaveAction().save();
 					tab.remove(i);
 				} else if (response == JOptionPane.CLOSED_OPTION) {
 					// Close canceled
 				}
 			}
 			System.exit(0);
 		}
 
 	}
 
 	private class PropertiesAction implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 			// Bring up a window:
 			// Allow user to change font for all tabs.
 		}
 
 	}
 
 	private class CutAction implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 			((TabPanel) tab.getComponentAt(tab.getSelectedIndex())).cut();
 		}
 
 	}
 
 	private class CopyAction implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 			((TabPanel) tab.getComponentAt(tab.getSelectedIndex())).copy();
 		}
 
 	}
 
 	private class PasteAction implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 			((TabPanel) tab.getComponentAt(tab.getSelectedIndex())).paste();
 		}
 	}
 
 	private class ManualAction implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 			// Help manual
 		}
 
 	}
 
 	private class AboutAction implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 			// About application
 		}
 
 	}
 
 	public static void main(String[] args) {
 		new GUI();
 	}
 }
