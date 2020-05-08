 package frontend;
 
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.Set;
 
 import javax.swing.AbstractAction;
 import javax.swing.BoxLayout;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import plugins.PluginManager;
 import plugins.SimplePluginManager;
 
 import com.apple.eawt.Application;
 
 import foobar.Foobar;
 import foobar.ScriptFooable;
 
 /**
  * The main Bruno application.
  * 
  * @author samuelainsworth
  * 
  */
 public class Bruno extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1987233037023049749L;
 
 	public static final String FILE_EXT = ".bruno~";
 
 	private final JTabbedPane tabPane;
 	private final JSplitPane splitPane;
 	private final ComponentPlaceholder editingWindowPlaceholder;
 	private final ComponentPlaceholder undoViewPlaceholder;
 	private EditingWindow editingWindow;
 
 	private PluginManager pluginManager = new SimplePluginManager();
 
 	private Foobar foobar;
 
 	public Bruno() {
 		setTitle("Bruno");
 		setSize(1024, 768);
 
 		// Center on screen
 		setLocationRelativeTo(null);
 
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 
 		editingWindowPlaceholder = new ComponentPlaceholder();
 		undoViewPlaceholder = new ComponentPlaceholder();
 
 		// Side pane
 		JPanel sidePane = new JPanel();
 		sidePane.setLayout(new BoxLayout(sidePane, BoxLayout.PAGE_AXIS));
 		tabPane = new JTabbedPane();
 		tabPane.addTab("Projects", new ProjectExplorer(this));
 		tabPane.addTab("Edit History", undoViewPlaceholder);
 
 		foobar = new Foobar(this);
 		foobar.setMaximumSize(new Dimension(999999, (int) foobar
 				.getPreferredSize().getHeight()));
 		sidePane.add(foobar);
 		sidePane.add(tabPane);
 
 		// Split Pane
 		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 				editingWindowPlaceholder, sidePane);
 		splitPane.setOneTouchExpandable(true);
 
 		setContentPane(splitPane);
 
 		// Open blank initial document
 		openDocument(new DocumentModel());
 
 		setUpPlugins();
 		setUpKeybindings();
 	}
 	
 	public boolean requestFocusInWindow() {
		super.requestFocusInWindow();
 		return this.editingWindow.requestFocusInWindow();
 	}
 
 	private void setUpKeybindings() {
 		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
 				KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit
 						.getDefaultToolkit().getMenuShortcutKeyMask()), "open");
 		getRootPane().getActionMap().put("open", new AbstractAction() {
 
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 4189934329254672244L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser fc = new JFileChooser();
 				fc.showOpenDialog(getRootPane());
 				openFile(fc.getSelectedFile());
 			}
 
 		});
 
 		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
 				KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit
 						.getDefaultToolkit().getMenuShortcutKeyMask()),
 				"foobar");
 		getRootPane().getActionMap().put("foobar", new AbstractAction() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 4189934329254672244L;

			private boolean hasFocus = false;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
				if (!hasFocus) {
					foobar.requestFocusInWindow();
					hasFocus = true;
				} else {
 					editingWindow.requestFocusInWindow();
					hasFocus = false;
 				}
 			}
 
 		});
 
 		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
 				KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit
 						.getDefaultToolkit().getMenuShortcutKeyMask()), "save");
 		getRootPane().getActionMap().put("save", new AbstractAction() {
 
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 4189934329254672244L;
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Save current file
 				if (editingWindow != null) {
 					try {
 						editingWindow.save();
 					} catch (IOException e0) {
 						e0.printStackTrace();
 					}
 				}
 			}
 
 		});
 	}
 
 	private void setUpPlugins() {
 		pluginManager.exposeVariable("bruno", this);
 		pluginManager.exposeVariable("editingWindow", editingWindow);
 		// loadPlugins();
 		Set<ScriptFooable> workingDirScripts = pluginManager
 				.getAllScriptFooables(new File("plugins/"));
 		Set<ScriptFooable> libraryScripts = pluginManager
 				.getAllScriptFooables(new File(
 						"/Library/Application Support/bruno/plugins/"));
 		
 		if (workingDirScripts != null)
 			foobar.addFooables(workingDirScripts);
 		if (libraryScripts != null)
 			foobar.addFooables(libraryScripts);
 	}
 
 	// /**
 	// * Sets up demo menu bar
 	// */
 	// private void setDemoMenuBar() {
 	// JMenuBar menuBar = new JMenuBar();
 	// JMenu file = new JMenu("File");
 	// JMenuItem item = new JMenuItem("Woah");
 	// file.add(item);
 	// menuBar.add(file);
 	// setJMenuBar(menuBar);
 	// }
 
 	/**
 	 * Toggle the Foobar.
 	 */
 	public void toggleFoobar() {
 		getRootPane().getActionMap().get("foobar").actionPerformed(null);
 	}
 
 	/**
 	 * Sets up nice look and feel adjustments.
 	 * 
 	 * @throws ClassNotFoundException
 	 * @throws UnsupportedLookAndFeelException
 	 * @throws InstantiationException
 	 * @throws IllegalAccessException
 	 */
 	private static void setNiceties() throws ClassNotFoundException,
 			UnsupportedLookAndFeelException, InstantiationException,
 			IllegalAccessException {
 		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		if (System.getProperty("os.name").equals("Mac OS X")) {
 			System.setProperty("apple.laf.useScreenMenuBar", "true");
 		}
 		Application application = Application.getApplication();
 		Image image = Toolkit.getDefaultToolkit().getImage("resources/*.jpg");
 		application.setDockIconImage(image);
 	}
 
 	/**
 	 * Open a document in the editor, saving the current document and loading in
 	 * the new one.
 	 * 
 	 * @param doc
 	 */
 	public void openDocument(DocumentModel doc) {
 		// Don't allow opening the currently open file
 		if (doc != null && editingWindow != null
 				&& doc.getFile().equals(editingWindow.getDoc().getFile())) {
 			editingWindow.getTextArea().requestFocus();
 			return;
 		}
 
 		// Save current file
 		if (editingWindow != null) {
 			try {
 				editingWindow.save();
 			} catch (IOException e) {
 				JOptionPane.showMessageDialog(this, "Failed to save file",
 						"File saving error", JOptionPane.ERROR_MESSAGE);
 				e.printStackTrace();
 			}
 		}
 
 		try {
 			editingWindow = new EditingWindow(doc);
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(this,
 					"Failed to open file " + doc.getFile(),
 					"File opening error", JOptionPane.ERROR_MESSAGE);
 			e.printStackTrace();
 		}
 
 		editingWindowPlaceholder.setContents(editingWindow.getView());
 		undoViewPlaceholder.setContents(editingWindow.getUndoController()
 				.getView());
 
 		// Because fuck Swing
 		editingWindowPlaceholder.setVisible(false);
 		editingWindowPlaceholder.setVisible(true);
 	}
 
 	/**
 	 * Open the specified file in the editing area
 	 * 
 	 * @param file
 	 */
 	public void openFile(File file) {
 		openDocument(new DocumentModel(file));
 	}
 
 	/**
 	 * Close the editor.
 	 */
 	public void close() {
 		// TODO save current file
 		setVisible(false);
 		dispose();
 		System.exit(0);
 	}
 
 	public Foobar getFoobar() {
 		return foobar;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			setNiceties();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				Bruno b = new Bruno();
 				b.setVisible(true);
 
 				// Can't set divider location until after frame has been packed
 				// or set visible.
 				b.splitPane.setDividerLocation(0.7);
 			}
 
 		});
 	}
 }
