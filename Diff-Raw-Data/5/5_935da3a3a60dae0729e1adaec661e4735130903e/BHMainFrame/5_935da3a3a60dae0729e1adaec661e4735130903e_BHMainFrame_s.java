 package org.bh.gui.swing;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.io.IOException;
 import java.util.prefs.BackingStoreException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.UIManager;
 import javax.swing.WindowConstants;
 
 import org.apache.log4j.Logger;
 import org.bh.platform.IPlatformListener;
 import org.bh.platform.PlatformController;
 import org.bh.platform.PlatformEvent;
 import org.bh.platform.ProjectRepositoryManager;
 import org.bh.platform.Services;
 import org.bh.platform.PlatformEvent.Type;
 
 /**
  * Main Frame for Business Horizon Application.
  * 
  * <p>
  * This <code>JFrame</code> provides the main frame for Business Horizon
  * Application.
  * 
  * @author Tietze.Patrick
  * @author Thiele.Klaus
  * @author Schmalzhaf.Alexander
  * @author Loeckelt.Michael
  * 
  * @version 0.1.1, 2009/12/16
  * @version 0.2, 2009/12/22
  * @version 0.3, 2009/12/31
  * @version 0.4, 2010/01/07
  * 
  */
 public class BHMainFrame extends JFrame implements IPlatformListener {
 
 	/**
 	 * logger
 	 */
 	private static Logger log = Logger.getLogger(BHMainFrame.class);
 	
 	/**
 	 * Standard Bar height.
 	 */
 	public static final int STANDARDBARHEIGHT = 40;
 
 	/**
 	 * main panel.
 	 */
 	private JPanel desktop;
 
 	/**
 	 * Menu Bar for application.
 	 */
 	private BHMenuBar menuBar;
 
 	/**
 	 * ToolBar for desktop.
 	 */
 	private BHToolBar toolBar;
 
 	/**
 	 * Tree for File contents (placed on a ScrollPane).
 	 */
 	private JScrollPane bhTreeScroller;
 	
 	/**
 	 * Tree displaying the file contents.
 	 */
 	private BHTree bhTree;
 
 	/**
 	 * Status Bar.
 	 */
 	private BHStatusBar statusBar;
 
 	/**
 	 * TODO: Javadoc
 	 */
 	private BHContent content;
 
 	/**
 	 * Horizontal Split pane.
 	 */
 	private JSplitPane paneH;
 
 	/**
 	 * Vertical Split pane.
 	 */
 	private JSplitPane paneV;
 
 	/**
 	 * Open / Save dialog.
 	 */
 	private BHFileChooser chooser;
 
 	/**
 	 * Standard constructor for <code>BHMainFrame</code>.
 	 * 
 	 * @param title
 	 *            title to be set for the <code>BHMainFrame</code>.
 	 */
 	public BHMainFrame() {
 		super();
 		this.setProperties();
 
 		// Build MenuBar
 		menuBar = new BHMenuBar();
 		this.setJMenuBar(menuBar);
 		
 		// build GUI components
 		desktop = new JPanel();
 		desktop.setLayout(new BorderLayout());
 
 		toolBar = new BHToolBar(getWidth(), STANDARDBARHEIGHT);
 		// toolBar.setBounds(0, 0, screenSize.width, standardBarHeight);
 
 		bhTree = new BHTree();
 		bhTreeScroller = new JScrollPane(bhTree);
 	
 		// treeBar.setBounds(0, standardBarHeight, treeBarWidth,
 		// screenSize.height-standardBarHeight);
 		// treeBar.setBounds(0,200,200,400);
 		
 		statusBar = Services.getBHstatusBar();
 		content = new BHContent();
 
 		// Create the horizontal split pane and put the treeBar and the content
 		// in it.
 		paneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, bhTreeScroller,
 				content);
 		paneH.setOneTouchExpandable(true);
 		
 		bhTreeScroller.setMinimumSize(new Dimension(UIManager.getInt("BHTree.minimumWidth"), bhTreeScroller.getMinimumSize().height));
 		// stop moving the divider
 		// pane.setEnabled(false);
 
 		desktop.add(toolBar, BorderLayout.PAGE_START);
 		desktop.add(paneH, BorderLayout.CENTER);
 		desktop.add(statusBar, BorderLayout.PAGE_END);
 		// desktop.add(content, BorderLayout.CENTER);
 
 		this.setContentPane(desktop);
 
 		chooser = new BHFileChooser();
 
 		Services.firePlatformEvent(new PlatformEvent(this, Type.PLATFORM_LOADING_COMPLETED));
 	}
 
 	/**
 	 * Sets initial properties on <code>BHMainFrame</code>.
 	 */
 	private synchronized void setProperties() {
 		this.resetTitle();
 		this.setExtendedState(MAXIMIZED_BOTH);
 		this.setLocationRelativeTo(null);
 		this.setSize(1024, 768);
 		// EXIT is like app suicide
 		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		this.setLocationRelativeTo(null);
 		Services.addPlatformListener(this);
 		
 		//this.setIconImage(new ImageIcon("/org/bh/images/bh-logo.jpg").getImage()); //TODO Test on windows	
 		try {
 			this.setIconImage(ImageIO.read(getClass().getResourceAsStream("/org/bh/images/bh-logo-imageIcon.png")));
 		} catch (IOException e) {
 			log.error("Failed to load IconImage", e);
 		}
 	}
 	
 	/**
 	 * resets the title of the <code>BHMainFrame</code>.
 	 */
 	public void resetTitle() {
 		this.setTitle(Services.getTranslator().translate("title"));
 		String path = PlatformController.preferences.get("path", null);
 		if (path != null) {
 			this.setTitle(this.getTitle() + " - " + path);
 		}
 	}
 	
 	/**
 	 * Disposes the Frame.
 	 */
 	@Override
 	public void dispose() {
 		// TODO Michael Löckelt: Kommentieren, Save Dialog
 		if (ProjectRepositoryManager.isChanged()) {
 			
 			int i = JOptionPane.showConfirmDialog(this, Services.getTranslator().translate("Psave"));
 			if (i == JOptionPane.YES_OPTION) {
 			
 				/**
 				 * Try to save all preferences
 				 * @author Marcus Katzor
 				 */
 				try {
 					Logger.getLogger(getClass()).debug("Save application preferences");
 					PlatformController.preferences.flush();
 					Services.firePlatformEvent(new PlatformEvent(BHMainFrame.class, PlatformEvent.Type.SAVE));
 				} catch (BackingStoreException e) {
 					Logger.getLogger(getClass()).error("Error while saving application preferences", e);
 				}
 				
 				super.dispose();
 				
 			} else if (i == JOptionPane.NO_OPTION) {
 				Logger.getLogger(getClass()).debug("Existing changes but no save wish - exiting app");
 				super.dispose();
 			}
 			
 		} else { 
 			Logger.getLogger(getClass()).debug("No changes - exiting app");
 			super.dispose();
 		}
 		
 	}
 
 	public void addContentForms(Component content) {
 		JScrollPane formsScrollPane = new JScrollPane(content);
 		paneH.setRightComponent(formsScrollPane);
 
 	}
 
 	public void addContentFormsAndChart(Component forms, Component chart) {
 		JSplitPane paneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT, forms, chart);
 
 		paneV.setOneTouchExpandable(true);
 
 		paneH.setRightComponent(paneV);
 	}
 
 	/**
 	 * Returns the <code>FileChooser</code> of the <code>BHMainFrame</code>.
 	 * @return the current <code>BHFileChooser</code>.
 	 */
 	public BHFileChooser getChooser() {
 		return chooser;
 	}
 	
 	/**
 	 * Returns the BHTree.
 	 * @return the BHTree.
 	 */
 	public BHTree getBHTree() {
 		return bhTree;
 	}
 
 	/**
 	 * Brings <code>BHMainFrame</code> to front when 
 	 * <code>PLATFORM_LOADING_COMPLETED</code>.
 	 */
 	@Override
 	public void platformEvent(PlatformEvent e) {
 
 		// Platform loading completed. show.
 		if (e.getEventType() == Type.PLATFORM_LOADING_COMPLETED) {
 			this.setVisible(true);
 			this.toFront();
 			this.requestFocus();
 		}
 		
 		// Data changed. Add changed suffix to title.
 		if (e.getEventType() == Type.DATA_CHANGED) {
 			String changedSuffix = " (" + Services.getTranslator().translate("changed") + ")";
 			if (! this.getTitle().endsWith(changedSuffix)) {
 				this.setTitle(this.getTitle() + changedSuffix);
 			}
 		}
 		
 		// Locale changed
 		if (e.getEventType() == Type.LOCALE_CHANGED) {
 			this.resetTitle();
 		}
 	}
 	
 	
 }
