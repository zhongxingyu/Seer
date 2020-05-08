 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package net.rptools.maptool.client.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Observer;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.swing.AbstractAction;
 import javax.swing.ActionMap;
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.InputMap;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.border.BevelBorder;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 import javax.xml.parsers.ParserConfigurationException;
 
 import net.rptools.lib.AppEvent;
 import net.rptools.lib.AppEventListener;
 import net.rptools.lib.FileUtil;
 import net.rptools.lib.MD5Key;
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.lib.swing.AboutDialog;
 import net.rptools.lib.swing.ColorPicker;
 import net.rptools.lib.swing.PositionalLayout;
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.lib.swing.preference.FramePreferences;
 import net.rptools.maptool.client.AppActions;
 import net.rptools.maptool.client.AppConstants;
 import net.rptools.maptool.client.AppPreferences;
 import net.rptools.maptool.client.AppState;
 import net.rptools.maptool.client.AppStyle;
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ServerDisconnectHandler;
 import net.rptools.maptool.client.swing.CoordinateStatusBar;
 import net.rptools.maptool.client.swing.GlassPane;
 import net.rptools.maptool.client.swing.ImageChooserDialog;
 import net.rptools.maptool.client.swing.MemoryStatusBar;
 import net.rptools.maptool.client.swing.ProgressStatusBar;
 import net.rptools.maptool.client.swing.SpacerStatusBar;
 import net.rptools.maptool.client.swing.StatusPanel;
 import net.rptools.maptool.client.swing.ZoomStatusBar;
 import net.rptools.maptool.client.tool.PointerTool;
 import net.rptools.maptool.client.tool.StampTool;
 import net.rptools.maptool.client.ui.assetpanel.AssetDirectory;
 import net.rptools.maptool.client.ui.assetpanel.AssetPanel;
 import net.rptools.maptool.client.ui.commandpanel.CommandPanel;
 import net.rptools.maptool.client.ui.lookuptable.LookupTablePanel;
 import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButton;
 import net.rptools.maptool.client.ui.macrobuttons.panels.CampaignPanel;
 import net.rptools.maptool.client.ui.macrobuttons.panels.GlobalPanel;
 import net.rptools.maptool.client.ui.macrobuttons.panels.ImpersonatePanel;
 import net.rptools.maptool.client.ui.macrobuttons.panels.SelectionPanel;
 import net.rptools.maptool.client.ui.token.EditTokenDialog;
 import net.rptools.maptool.client.ui.tokenpanel.InitiativePanel;
 import net.rptools.maptool.client.ui.tokenpanel.TokenPanelTreeCellRenderer;
 import net.rptools.maptool.client.ui.tokenpanel.TokenPanelTreeModel;
 import net.rptools.maptool.client.ui.zone.PointerOverlay;
 import net.rptools.maptool.client.ui.zone.ZoneMiniMapPanel;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.language.I18N;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetAvailableListener;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.ObservableList;
 import net.rptools.maptool.model.TextMessage;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.ZoneFactory;
 import net.rptools.maptool.model.ZonePoint;
 import net.rptools.maptool.model.drawing.DrawableColorPaint;
 import net.rptools.maptool.model.drawing.DrawablePaint;
 import net.rptools.maptool.model.drawing.DrawableTexturePaint;
 import net.rptools.maptool.model.drawing.Pen;
 import net.rptools.maptool.util.ImageManager;
 
 import org.xml.sax.SAXException;
 
 import com.jidesoft.docking.DefaultDockableHolder;
 import com.jidesoft.docking.DockableFrame;
 
 /**
  */
 public class MapToolFrame extends DefaultDockableHolder implements WindowListener, AppEventListener {
 
 	private static final String INITIAL_LAYOUT_XML = "net/rptools/maptool/client/ui/ilayout.xml";
 	private static final String MAPTOOL_LOGO_IMAGE = "net/rptools/maptool/client/image/maptool-logo.png";
 	private static final String CREDITS_HTML = "net/rptools/maptool/client/credits.html";
 	private static final String MINILOGO_IMAGE = "net/rptools/maptool/client/image/minilogo.png";
 
 	private static final long serialVersionUID = 3905523813025329458L;
 
 	// TODO: parameterize this (or make it a preference)
 	private static final int WINDOW_WIDTH = 800;
 	private static final int WINDOW_HEIGHT = 600;
 
 	private static final String DOCKING_PROFILE_NAME = "maptoolDocking";
 
 	private Pen pen = new Pen(Pen.DEFAULT);
 
 	/**
 	 * Are the drawing measurements being painted?
 	 */
 	private boolean paintDrawingMeasurement = true;
 
 	private Map<MTFrame, DockableFrame> frameMap = new HashMap<MTFrame, DockableFrame>();
 
 	private ImageChooserDialog imageChooserDialog;
 
 	// Components
 	private ZoneRenderer currentRenderer;
 	private AssetPanel assetPanel;
 	private ClientConnectionPanel connectionPanel;
 	private InitiativePanel initiativePanel;
 	private PointerOverlay pointerOverlay;
 	private CommandPanel commandPanel;
 	private AboutDialog aboutDialog;
 	private ColorPicker colorPicker;
 	private Toolbox toolbox;
 	private ZoneMiniMapPanel zoneMiniMapPanel;
 	private JPanel zoneRendererPanel;
 	private JPanel visibleControlPanel;
 	private FullScreenFrame fullScreenFrame;
 	private JPanel rendererBorderPanel;
 	private List<ZoneRenderer> zoneRendererList;
 	private JMenuBar menuBar;
 	private StatusPanel statusPanel;
 	private ActivityMonitorPanel activityMonitor = new ActivityMonitorPanel();
 	private ProgressStatusBar progressBar = new ProgressStatusBar();
 	private ConnectionStatusPanel connectionStatusPanel = new ConnectionStatusPanel();
 	private CoordinateStatusBar coordinateStatusBar;
 	private ZoomStatusBar zoomStatusBar;
 	private JLabel chatActionLabel;
 	private GlassPane glassPane;
 
 	private TextureChooserPanel textureChooserPanel;
 
 	private LookupTablePanel lookupTablePanel;
 
 	// Components
 	private JFileChooser loadPropsFileChooser;
 	private JFileChooser loadFileChooser;
 
 	private JFileChooser saveCmpgnFileChooser;
 	private JFileChooser savePropsFileChooser;
 	private JFileChooser saveFileChooser;
 
 	private FileFilter campaignFilter = new MTFileFilter("cmpgn", I18N.getText("file.ext.cmpgn"));
 	private FileFilter mapFilter = new MTFileFilter("rpmap", I18N.getText("file.ext.rpmap"));
 	private FileFilter propertiesFilter = new MTFileFilter("mtprops", I18N.getText("file.ext.mtprops"));
 
 	// Macro import/export support
 	private FileFilter macroFilter = new MTFileFilter("mtmacro", I18N.getText("file.ext.mtmacro"));
 	private FileFilter macroSetFilter = new MTFileFilter("mtmacset", I18N.getText("file.ext.mtmacset"));
 
 	// Table import/export support
 	private FileFilter tableFilter = new MTFileFilter("mttable", "MapTool Table");
 
 	private EditTokenDialog tokenPropertiesDialog;
 
 	private CampaignPanel campaignPanel = new CampaignPanel();
 	private GlobalPanel globalPanel = new GlobalPanel();
 	private SelectionPanel selectionPanel = new SelectionPanel();
 	private ImpersonatePanel impersonatePanel = new ImpersonatePanel();
 
 	public MapToolFrame() {
 		// Set up the frame
 		super(AppConstants.APP_NAME);
 
 		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 		addWindowListener(this);
 		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
 		SwingUtil.centerOnScreen(this);
 		setFocusTraversalPolicy(new MapToolFocusTraversalPolicy());
 
 		try {
 			setIconImage(ImageUtil.getImage(MINILOGO_IMAGE));
 		} catch (IOException ioe) {
 			System.err.println(I18N.getText("msg.error.loadingIconImage"));
 		}
 
 		// Components
 		glassPane = new GlassPane();
 		assetPanel = createAssetPanel();
 		connectionPanel = createConnectionPanel();
 		toolbox = new Toolbox();
 
 		initiativePanel = createInitiativePanel();
 
 		zoneRendererList = new CopyOnWriteArrayList<ZoneRenderer>();
 		pointerOverlay = new PointerOverlay();
 
 		colorPicker = new ColorPicker(this);
 		textureChooserPanel = new TextureChooserPanel(colorPicker.getPaintChooser(), assetPanel.getModel(), "imageExplorerTextureChooser");
 		colorPicker.getPaintChooser().addPaintChooser(textureChooserPanel);
 
 		String credits = "";
 		String version = "";
 		Image logo = null;
 		try {
 			credits = new String(FileUtil.loadResource(CREDITS_HTML));
 			version = MapTool.getVersion();
 			credits = credits.replace("%VERSION%", version);
 			logo = ImageUtil.getImage(MAPTOOL_LOGO_IMAGE);
 
 		} catch (Exception ioe) {
 			System.err.println(I18N.getText("msg.error.credits"));
 		}
 		aboutDialog = new AboutDialog(this, logo, credits);
 		aboutDialog.setSize(354, 400);
 
 		statusPanel = new StatusPanel();
 		statusPanel.addPanel(getCoordinateStatusBar());
 		statusPanel.addPanel(getZoomStatusBar());
 		statusPanel.addPanel(new MemoryStatusBar());
 		// statusPanel.addPanel(progressBar);
 		statusPanel.addPanel(connectionStatusPanel);
 		statusPanel.addPanel(activityMonitor);
 		statusPanel.addPanel(new SpacerStatusBar(25));
 
 		zoneMiniMapPanel = new ZoneMiniMapPanel();
 		zoneMiniMapPanel.setSize(100, 100);
 
 		zoneRendererPanel = new JPanel(new PositionalLayout(5));
 		zoneRendererPanel.setBackground(Color.black);
 		// zoneRendererPanel.add(zoneMiniMapPanel,
 		// PositionalLayout.Position.SE);
 		zoneRendererPanel.add(getChatActionLabel(), PositionalLayout.Position.SW);
 
 		commandPanel = new CommandPanel();
 		MapTool.getMessageList().addObserver(commandPanel);
 		MapTool.getMessageList().addObserver(createChatIconMessageObserver());
 
 		rendererBorderPanel = new JPanel(new GridLayout());
 		rendererBorderPanel.setBorder(BorderFactory.createLineBorder(Color.darkGray));
 		rendererBorderPanel.add(zoneRendererPanel);
 
 		// Put it all together
 		menuBar = new AppMenuBar();
 		setJMenuBar(menuBar);
 		add(BorderLayout.NORTH, new ToolbarPanel(toolbox));
 		add(BorderLayout.SOUTH, statusPanel);
 
 		setGlassPane(glassPane);
 
 		removeWindowsF10();
 
 		MapTool.getEventDispatcher().addListener(this, MapTool.ZoneEvent.Activated);
 
 		new FramePreferences(AppConstants.APP_NAME, "mainFrame", this);
 		restorePreferences();
 		updateKeyStrokes();
 
 		// This will cause the frame to be set to visible (BAD jide, BAD! No
 		// cookie for you!)
 		configureDocking();
 	}
 
 	public ImageChooserDialog getImageChooserDialog() {
 		if (imageChooserDialog == null) {
 			imageChooserDialog = new ImageChooserDialog(this);
 		}
 		return imageChooserDialog;
 	}
 
 	public enum MTFrame {
 		/*
 		 * These enums should be specified using references to the properties
 		 * file. However, a simple toString() method is used later to determine
 		 * what to display on the various panels. So if I convert the propName
 		 * into the value from the properties file and return it, parts of the
 		 * code later on usethat string to do a properties file lookup! That
 		 * means that any code using MTFrame enums that are converted to Strings
 		 * need to be checked so that when the return value is used as the NAME
 		 * of an Action, the property name is retrieved instead. Ugh. :(
 		 * 
 		 * We'll need two additional methods: getPropName() and
 		 * getDisplayName(). Perhaps toString() could call getDisplayName(), but
 		 * it might be much simpler to debug if toString() weren't used. In that
 		 * case, there's no reason to use an enum either ... may as well use a
 		 * class with static final objects in it. Sigh.
 		 */
 		CONNECTIONS("Connections"),	//
 		TOKEN_TREE("MapExplorer"),		// These comments prevent
 		INITIATIVE(	"Initiative"),				// the source reformatter from
 		IMAGE_EXPLORER("Library"),		// rearranging the structure
 		CHAT("Chat"),							// of these lines, keeping each
 		LOOKUP_TABLES("Tables"),			// one on its own line. :)
 		GLOBAL("Global"),						//
 		CAMPAIGN("Campaign"),				//
 		SELECTION("Selected"),				//
 		IMPERSONATED("Impersonate");	//
 
 		private String displayName;
 
 		private MTFrame(String dispName) {
 			displayName = dispName;
 		}
 
 		public String toString() {
 			return displayName;
 		}
 
 		public String getPropertyName() {
 			return "panel." + displayName;
 		}
 	}
 
 	private void configureDocking() {
 
 		initializeFrames();
 
 		getDockingManager().setProfileKey(DOCKING_PROFILE_NAME);
 		getDockingManager().setOutlineMode(com.jidesoft.docking.DockingManager.PARTIAL_OUTLINE_MODE);
 		getDockingManager().setUsePref(false);
 
 		getDockingManager().getWorkspace().setAcceptDockableFrame(false);
 
 		// Main panel
 		getDockingManager().getWorkspace().add(rendererBorderPanel);
 
 		// Docked frames
 		getDockingManager().addFrame(getFrame(MTFrame.CONNECTIONS));
 		getDockingManager().addFrame(getFrame(MTFrame.TOKEN_TREE));
 		getDockingManager().addFrame(getFrame(MTFrame.INITIATIVE));
 		getDockingManager().addFrame(getFrame(MTFrame.IMAGE_EXPLORER));
 		getDockingManager().addFrame(getFrame(MTFrame.CHAT));
 		getDockingManager().addFrame(getFrame(MTFrame.LOOKUP_TABLES));
 		getDockingManager().addFrame(getFrame(MTFrame.GLOBAL));
 		getDockingManager().addFrame(getFrame(MTFrame.CAMPAIGN));
 		getDockingManager().addFrame(getFrame(MTFrame.SELECTION));
 		getDockingManager().addFrame(getFrame(MTFrame.IMPERSONATED));
 
 		try {
 			getDockingManager().loadInitialLayout(MapToolFrame.class.getClassLoader().getResourceAsStream(INITIAL_LAYOUT_XML));
 		} catch (ParserConfigurationException e) {
 			MapTool.showError("msg.error.layoutParse");
 			e.printStackTrace();
 		} catch (SAXException s) {
 			MapTool.showError("msg.error.layoutParse");
 			s.printStackTrace();
 		} catch (IOException e) {
 			MapTool.showError("msg.error.layoutParse");
 			e.printStackTrace();
 		}
 		getDockingManager().loadLayoutDataFromFile(AppUtil.getAppHome("config").getAbsolutePath() + "/layout.dat");
 	}
 
 	public DockableFrame getFrame(MTFrame frame) {
 		return frameMap.get(frame);
 	}
 
 	private void initializeFrames() {
 		frameMap.put(MTFrame.CONNECTIONS, createDockingFrame(MTFrame.CONNECTIONS, new JScrollPane(connectionPanel), new ImageIcon(AppStyle.connectionsImage)));
 		frameMap.put(MTFrame.TOKEN_TREE, createDockingFrame(MTFrame.TOKEN_TREE, new JScrollPane(createTokenTreePanel()), new ImageIcon(AppStyle.mapExplorerImage)));
 		frameMap.put(MTFrame.IMAGE_EXPLORER, createDockingFrame(MTFrame.IMAGE_EXPLORER, assetPanel, new ImageIcon(AppStyle.resourceLibraryImage)));
 		frameMap.put(MTFrame.CHAT, createDockingFrame(MTFrame.CHAT, commandPanel, new ImageIcon(AppStyle.chatPanelImage)));
 		frameMap.put(MTFrame.LOOKUP_TABLES, createDockingFrame(MTFrame.LOOKUP_TABLES, getLookupTablePanel(), new ImageIcon(AppStyle.tablesPanelImage)));
 		frameMap.put(MTFrame.INITIATIVE, createDockingFrame(MTFrame.INITIATIVE, initiativePanel, new ImageIcon(AppStyle.initiativePanelImage)));
 
 		JScrollPane campaign = scrollPaneFactory(campaignPanel);
 		JScrollPane global = scrollPaneFactory(globalPanel);
 		JScrollPane selection = scrollPaneFactory(selectionPanel);
 		JScrollPane impersonate = scrollPaneFactory(impersonatePanel);
 		frameMap.put(MTFrame.GLOBAL, createDockingFrame(MTFrame.GLOBAL, global, new ImageIcon(AppStyle.globalPanelImage)));
 		frameMap.put(MTFrame.CAMPAIGN, createDockingFrame(MTFrame.CAMPAIGN, campaign, new ImageIcon(AppStyle.campaignPanelImage)));
 		frameMap.put(MTFrame.SELECTION, createDockingFrame(MTFrame.SELECTION, selection, new ImageIcon(AppStyle.selectionPanelImage)));
 		frameMap.put(MTFrame.IMPERSONATED, createDockingFrame(MTFrame.IMPERSONATED, impersonate, new ImageIcon(AppStyle.impersonatePanelImage)));
 
 	}
 
 	private JScrollPane scrollPaneFactory(JPanel panel) {
 		JScrollPane pane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		pane.getViewport().setBorder(null);
 		return pane;
 	}
 
 	private static DockableFrame createDockingFrame(MTFrame mtFrame, Component component, Icon icon) {
 		DockableFrame frame = new DockableFrame(mtFrame.name(), icon);
 		frame.add(component);
 
 		return frame;
 	}
 
 	public LookupTablePanel getLookupTablePanel() {
 		if (lookupTablePanel == null) {
 			lookupTablePanel = new LookupTablePanel();
 		}
 		return lookupTablePanel;
 	}
 
 	public EditTokenDialog getTokenPropertiesDialog() {
 		if (tokenPropertiesDialog == null) {
 			tokenPropertiesDialog = new EditTokenDialog();
 		}
 		return tokenPropertiesDialog;
 	}
 
 	public void refresh() {
 		if (getCurrentZoneRenderer() != null) {
 			getCurrentZoneRenderer().repaint();
 		}
 	}
 
 	private class MTFileFilter extends FileFilter {
 		private String extension;
 		private String description;
 
 		MTFileFilter(String exten, String desc) {
 			super();
 			extension = exten;
 			description = desc;
 		}
 
 		// Accept directories and files matching extension
 		public boolean accept(File f) {
 
 			if (f.isDirectory()) {
 				return true;
 			}
 
 			String ext = getExtension(f);
 			if (ext != null) {
 				if (ext.equals(extension)) {
 					return true;
 				} else {
 					return false;
 				}
 			}
 
 			return false;
 		}
 
 		public String getDescription() {
 			return description;
 		}
 
 		public String getExtension(File f) {
 			String ext = null;
 			String s = f.getName();
 			int i = s.lastIndexOf('.');
 
 			if (i > 0 && i < s.length() - 1) {
 				ext = s.substring(i + 1).toLowerCase();
 			}
 			return ext;
 		}
 	}
 
 	public FileFilter getCmpgnFileFilter() {
 		return campaignFilter;
 	}
 
 	public FileFilter getMapFileFilter() {
 		return mapFilter;
 	}
 
 	public JFileChooser getLoadPropsFileChooser() {
 		if (loadPropsFileChooser == null) {
 			loadPropsFileChooser = new JFileChooser();
 			loadPropsFileChooser.setCurrentDirectory(AppPreferences.getLoadDir());
 			loadPropsFileChooser.addChoosableFileFilter(propertiesFilter);
 			loadPropsFileChooser.setDialogTitle(I18N.getText("msg.title.importProperties"));
 		}
 
 		loadPropsFileChooser.setFileFilter(propertiesFilter);
 		return loadPropsFileChooser;
 	}
 
 	public JFileChooser getLoadFileChooser() {
 		if (loadFileChooser == null) {
 			loadFileChooser = new JFileChooser();
 			loadFileChooser.setCurrentDirectory(AppPreferences.getLoadDir());
 		}
 		return loadFileChooser;
 	}
 
 	public JFileChooser getSaveCmpgnFileChooser() {
 		if (saveCmpgnFileChooser == null) {
 			saveCmpgnFileChooser = new JFileChooser();
 			saveCmpgnFileChooser.setCurrentDirectory(AppPreferences.getSaveDir());
 			saveCmpgnFileChooser.addChoosableFileFilter(campaignFilter);
 			saveCmpgnFileChooser.setDialogTitle(I18N.getText("msg.title.saveCampaign"));
 		}
 		saveCmpgnFileChooser.setAcceptAllFileFilterUsed(true);
 		return saveCmpgnFileChooser;
 	}
 
 	public JFileChooser getSavePropsFileChooser() {
 		if (savePropsFileChooser == null) {
 			savePropsFileChooser = new JFileChooser();
 			savePropsFileChooser.setCurrentDirectory(AppPreferences.getSaveDir());
 			savePropsFileChooser.addChoosableFileFilter(propertiesFilter);
 			savePropsFileChooser.setDialogTitle(I18N.getText("msg.title.exportProperties"));
 		}
 		savePropsFileChooser.setAcceptAllFileFilterUsed(true);
 		return savePropsFileChooser;
 	}
 
 	public JFileChooser getSaveFileChooser() {
 		if (saveFileChooser == null) {
 			saveFileChooser = new JFileChooser();
 			saveFileChooser.setCurrentDirectory(AppPreferences.getSaveDir());
 		}
 		return saveFileChooser;
 	}
 
 	public void showControlPanel(JPanel... panels) {
 
 		JPanel layoutPanel = new JPanel(new GridBagLayout());
 		layoutPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
 
 		int i = 0;
 		for (JPanel panel : panels) {
 
 			GridBagConstraints gbc = new GridBagConstraints();
 			gbc.gridx = 1;
 			gbc.gridy = i;
 			gbc.weightx = 1;
 			gbc.fill = GridBagConstraints.BOTH;
 
 			layoutPanel.add(panel, gbc);
 			i++;
 		}
 		layoutPanel.setSize(layoutPanel.getPreferredSize());
 
 		zoneRendererPanel.add(layoutPanel, PositionalLayout.Position.NE);
 		zoneRendererPanel.setComponentZOrder(layoutPanel, 0);
 		zoneRendererPanel.revalidate();
 		zoneRendererPanel.repaint();
 
 		visibleControlPanel = layoutPanel;
 	}
 
 	public ZoomStatusBar getZoomStatusBar() {
 		if (zoomStatusBar == null) {
 			zoomStatusBar = new ZoomStatusBar();
 		}
 		return zoomStatusBar;
 	}
 
 	public CoordinateStatusBar getCoordinateStatusBar() {
 		if (coordinateStatusBar == null) {
 			coordinateStatusBar = new CoordinateStatusBar();
 		}
 		return coordinateStatusBar;
 	}
 
 	public void hideControlPanel() {
 		if (visibleControlPanel != null) {
 
 			if (zoneRendererPanel != null) {
 				zoneRendererPanel.remove(visibleControlPanel);
 			}
 
 			visibleControlPanel = null;
 			refresh();
 		}
 	}
 
 	public void showNonModalGlassPane(JComponent component, int x, int y) {
 		showGlassPane(component, x, y, false);
 	}
 
 	public void showModalGlassPane(JComponent component, int x, int y) {
 		showGlassPane(component, x, y, true);
 	}
 
 	private void showGlassPane(JComponent component, int x, int y, boolean modal) {
 		component.setSize(component.getPreferredSize());
 		component.setLocation(x, y);
 
 		glassPane.setLayout(null);
 		glassPane.add(component);
 		glassPane.setModel(modal);
 		glassPane.setVisible(true);
 	}
 
 	public void showFilledGlassPane(JComponent component) {
 		glassPane.setLayout(new GridLayout());
 		glassPane.add(component);
 		glassPane.setVisible(true);
 	}
 
 	public void hideGlassPane() {
 		glassPane.removeAll();
 		glassPane.setVisible(false);
 	}
 
 	public JLabel getChatActionLabel() {
 		if (chatActionLabel == null) {
 			chatActionLabel = new JLabel(new ImageIcon(AppStyle.chatImage));
 			chatActionLabel.setSize(chatActionLabel.getPreferredSize());
 			chatActionLabel.setVisible(false);
 			chatActionLabel.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mousePressed(MouseEvent e) {
 					showCommandPanel();
 				}
 			});
 		}
 		return chatActionLabel;
 	}
 
 	private Observer createChatIconMessageObserver() {
 		return new Observer() {
 			public void update(java.util.Observable o, Object arg) {
 				ObservableList<TextMessage> textList = MapTool.getMessageList();
 				ObservableList.Event event = (ObservableList.Event) arg;
 
 				// if (rightSplitPane.isBottomHidden() && event ==
 				// ObservableList.Event.append) {
 				//					
 				// getChatActionLabel().setVisible(true);
 				// }
 			}
 		};
 	}
 
 	public boolean isCommandPanelVisible() {
 		return getFrame(MTFrame.CHAT).isShowing();
 	}
 
 	public void showCommandPanel() {
 		chatActionLabel.setVisible(false);
 		getDockingManager().showFrame(MTFrame.CHAT.name());
 		commandPanel.requestFocus();
 	}
 
 	public void hideCommandPanel() {
 		getDockingManager().hideFrame(MTFrame.CHAT.name());
 	}
 
 	public ColorPicker getColorPicker() {
 		return colorPicker;
 	}
 
 	public void showAboutDialog() {
 		aboutDialog.setVisible(true);
 	}
 
 	public ConnectionStatusPanel getConnectionStatusPanel() {
 		return connectionStatusPanel;
 	}
 
 	private void restorePreferences() {
 		List<File> assetRootList = AppPreferences.getAssetRoots();
 		for (File file : assetRootList) {
 			addAssetRoot(file);
 		}
 	}
 
 	private TokenPanelTreeModel tokenPanelTreeModel;
 
 	private JComponent createTokenTreePanel() {
 		final JTree tree = new JTree();
 		tokenPanelTreeModel = new TokenPanelTreeModel(tree);
 		tree.setModel(tokenPanelTreeModel);
 		tree.setCellRenderer(new TokenPanelTreeCellRenderer());
 		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
 		tree.addMouseListener(new MouseAdapter() {
 			// TODO: Make this a handler class, not an aic
 			@Override
 			public void mousePressed(MouseEvent e) {
 				// tree.setSelectionPath(tree.getPathForLocation(e.getX(), e.getY()));
 				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
 				if (path == null) {
 					return;
 				}
 
 				Object row = path.getLastPathComponent();
 				int rowIndex = tree.getRowForLocation(e.getX(), e.getY());
 				if (SwingUtilities.isLeftMouseButton(e)) {
 
 					if (!SwingUtil.isShiftDown(e)) {
 						tree.clearSelection();
 					}
 					tree.addSelectionInterval(rowIndex, rowIndex);
 
 					if (row instanceof Token) {
 						if (e.getClickCount() == 2) {
 							Token token = (Token) row;
 							getCurrentZoneRenderer().clearSelectedTokens();
 							getCurrentZoneRenderer().centerOn(new ZonePoint(token.getX(), token.getY()));
 
 							// Pick an appropriate tool
 							getToolbox().setSelectedTool(token.isToken() ? PointerTool.class : StampTool.class);
 							getCurrentZoneRenderer().setActiveLayer(token.getLayer());
 							getCurrentZoneRenderer().selectToken(token.getId());
 							getCurrentZoneRenderer().requestFocusInWindow();
 						}
 					}
 				}
 				if (SwingUtilities.isRightMouseButton(e)) {
 
 					if (!isRowSelected(tree.getSelectionRows(), rowIndex) && !SwingUtil.isShiftDown(e)) {
 						tree.clearSelection();
 						tree.addSelectionInterval(rowIndex, rowIndex);
 					}
 
 					final int x = e.getX();
 					final int y = e.getY();
 					EventQueue.invokeLater(new Runnable() {
 						public void run() {
 
 							Token firstToken = null;
 							Set<GUID> selectedTokenSet = new HashSet<GUID>();
 							for (TreePath path : tree.getSelectionPaths()) {
 
 								if (path.getLastPathComponent() instanceof Token) {
 									Token token = (Token) path.getLastPathComponent();
 									if (firstToken == null) {
 										firstToken = token;
 									}
 
 									if (AppUtil.playerOwns(token)) {
 										selectedTokenSet.add(token.getId());
 									}
 								}
 							}
 							if (selectedTokenSet.size() > 0) {
 
 								if (firstToken.isStamp()) {
 
 									new StampPopupMenu(selectedTokenSet, x, y, getCurrentZoneRenderer(), firstToken).showPopup(tree);
 								} else {
 									new TokenPopupMenu(selectedTokenSet, x, y, getCurrentZoneRenderer(), firstToken).showPopup(tree);
 								}
 							}
 						}
 					});
 				}
 			}
 		});
 
 		MapTool.getEventDispatcher().addListener(new AppEventListener() {
 			public void handleAppEvent(AppEvent event) {
 				tokenPanelTreeModel.setZone((Zone) event.getNewValue());
 			}
 		}, MapTool.ZoneEvent.Activated);
 
 		return tree;
 	}
 
 	public void clearTokenTree() {
 		if (tokenPanelTreeModel != null) {
 			tokenPanelTreeModel.setZone(null);
 		}
 		if (initiativePanel != null) {
 			initiativePanel.clearTokens();
 		}
 	}
 
 	public void updateTokenTree() {
 		if (tokenPanelTreeModel != null) {
 			tokenPanelTreeModel.update();
 		}
 		if (initiativePanel != null) {
 			initiativePanel.update();
 		}
 	}
 
 	private boolean isRowSelected(int[] selectedRows, int row) {
 		if (selectedRows == null) {
 			return false;
 		}
 
 		for (int selectedRow : selectedRows) {
 			if (row == selectedRow) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private ClientConnectionPanel createConnectionPanel() {
 		final ClientConnectionPanel panel = new ClientConnectionPanel();
 
 		return panel;
 	}
 
 	private InitiativePanel createInitiativePanel() {
 		MapTool.getEventDispatcher().addListener(new AppEventListener() {
 			public void handleAppEvent(AppEvent event) {
 				initiativePanel.setZone((Zone) event.getNewValue());
 			}
 		}, MapTool.ZoneEvent.Activated);
 		return new InitiativePanel();
 	}
 
 	private AssetPanel createAssetPanel() {
 		final AssetPanel panel = new AssetPanel("mainAssetPanel");
 		panel.addImagePanelMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				if (false) {
 					// TODO use for real popup logic
 					if (SwingUtilities.isLeftMouseButton(e)) {
 						if (e.getClickCount() == 2) {
 
 							List<Object> idList = panel.getSelectedIds();
 							if (idList == null || idList.size() == 0) {
 								return;
 							}
 
 							final int index = (Integer) idList.get(0);
 							createZone(panel.getAsset(index));
 						}
 					}
 				}
 
 				if (SwingUtilities.isRightMouseButton(e) && MapTool.getPlayer().isGM()) {
 					List<Object> idList = panel.getSelectedIds();
 					if (idList == null || idList.size() == 0) {
 						return;
 					}
 
 					final int index = (Integer) idList.get(0);
 
 					JPopupMenu menu = new JPopupMenu();
 					menu.add(new JMenuItem(new AbstractAction() {
 						{
 							putValue(NAME, I18N.getText("action.newMap"));
 						}
 
 						public void actionPerformed(ActionEvent e) {
 
 							createZone(panel.getAsset(index));
 						}
 					}));
 
 					panel.showImagePanelPopup(menu, e.getX(), e.getY());
 				}
 			}
 
 			private void createZone(Asset asset) {
 
 				Zone zone = ZoneFactory.createZone();
 				zone.setName(asset.getName());
 				BufferedImage image = ImageManager.getImageAndWait(asset);
 				if (image.getWidth() < 200 || image.getHeight() < 200) {
 					zone.setBackgroundPaint(new DrawableTexturePaint(asset));
 				} else {
 					zone.setMapAsset(asset.getId());
 					zone.setBackgroundPaint(new DrawableColorPaint(Color.black));
 				}
 				MapPropertiesDialog newMapDialog = new MapPropertiesDialog(MapTool.getFrame());
 				newMapDialog.setZone(zone);
 
 				newMapDialog.setVisible(true);
 
 				if (newMapDialog.getStatus() == MapPropertiesDialog.Status.OK) {
 					MapTool.addZone(zone);
 				}
 			}
 		});
 		return panel;
 	}
 
 	public PointerOverlay getPointerOverlay() {
 		return pointerOverlay;
 	}
 
 	public void setStatusMessage(final String message) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				statusPanel.setStatus("  " + message);
 			}
 		});
 	}
 
 	public ActivityMonitorPanel getActivityMonitor() {
 		return activityMonitor;
 	}
 
 	public void startIndeterminateAction() {
 		progressBar.startIndeterminate();
 	}
 
 	public void endIndeterminateAction() {
 		progressBar.endIndeterminate();
 	}
 
 	public void startDeterminateAction(int totalWork) {
 		progressBar.startDeterminate(totalWork);
 	}
 
 	public void updateDeterminateActionProgress(int additionalWorkCompleted) {
 		progressBar.updateDeterminateProgress(additionalWorkCompleted);
 	}
 
 	public void endDeterminateAction() {
 		progressBar.endDeterminate();
 	}
 
 	public ZoneMiniMapPanel getZoneMiniMapPanel() {
 		return zoneMiniMapPanel;
 	}
 
 	// /////////////////////////////////////////////////////////////////////////
 	// static methods
 	// /////////////////////////////////////////////////////////////////////////
 
 	public CommandPanel getCommandPanel() {
 		return commandPanel;
 	}
 
 	public ClientConnectionPanel getConnectionPanel() {
 		return connectionPanel;
 	}
 
 	public AssetPanel getAssetPanel() {
 		return assetPanel;
 	}
 
 	public void addAssetRoot(File rootDir) {
 
 		assetPanel.addAssetRoot(new AssetDirectory(rootDir, AppConstants.IMAGE_FILE_FILTER));
 
 		// if (mainSplitPane.isLeftHidden()) {
 		// mainSplitPane.showLeft();
 		// }
 	}
 
 	public Pen getPen() {
 		pen.setPaint(DrawablePaint.convertPaint(colorPicker.getForegroundPaint()));
 		pen.setBackgroundPaint(DrawablePaint.convertPaint(colorPicker.getBackgroundPaint()));
 		pen.setThickness(colorPicker.getStrokeWidth());
 		pen.setOpacity(colorPicker.getOpacity());
 		pen.setThickness(colorPicker.getStrokeWidth());
 		return pen;
 	}
 
 	public List<ZoneRenderer> getZoneRenderers() {
 		// TODO: This should prob be immutable
 		return zoneRendererList;
 	}
 
 	public ZoneRenderer getCurrentZoneRenderer() {
 		return currentRenderer;
 	}
 
 	public void addZoneRenderer(ZoneRenderer renderer) {
 		zoneRendererList.add(renderer);
 	}
 
 	public void removeZoneRenderer(ZoneRenderer renderer) {
 		boolean isCurrent = renderer == getCurrentZoneRenderer();
 
 		zoneRendererList.remove(renderer);
 		if (isCurrent) {
 			boolean rendererSet = false;
 			for (ZoneRenderer currRenderer : zoneRendererList) {
 				if (MapTool.getPlayer().isGM() || currRenderer.getZone().isVisible()) {
 					setCurrentZoneRenderer(currRenderer);
 					rendererSet = true;
 					break;
 				}
 			}
 			if (!rendererSet) {
 				setCurrentZoneRenderer(null);
 			}
 		}
 
 		zoneMiniMapPanel.flush();
 		zoneMiniMapPanel.repaint();
 	}
 
 	public void clearZoneRendererList() {
 		zoneRendererList.clear();
 		zoneMiniMapPanel.flush();
 		zoneMiniMapPanel.repaint();
 	}
 
 	public void setCurrentZoneRenderer(ZoneRenderer renderer) {
 		// Handle new renderers
 		// TODO: should this be here ?
 		if (renderer != null && !zoneRendererList.contains(renderer)) {
 			zoneRendererList.add(renderer);
 		}
 
 		if (currentRenderer != null) {
 			currentRenderer.flush();
 			zoneRendererPanel.remove(currentRenderer);
 		}
 
 		if (renderer != null) {
 			zoneRendererPanel.add(renderer, PositionalLayout.Position.CENTER);
 			zoneRendererPanel.doLayout();
 		}
 
 		currentRenderer = renderer;
 		initiativePanel.update();
 		toolbox.setTargetRenderer(renderer);
 
 		if (renderer != null) {
 			MapTool.getEventDispatcher().fireEvent(MapTool.ZoneEvent.Activated, this, null, renderer.getZone());
 			renderer.requestFocusInWindow();
 		}
 
 		AppActions.updateActions();
 		repaint();
 
 		setTitleViaRenderer(renderer);
 		getZoomStatusBar().update();
 	}
 
 	public void setTitleViaRenderer(ZoneRenderer renderer) {
 		String campaignName = " - [Default] ";
 		if (AppState.getCampaignFile() != null) {
 			String s = AppState.getCampaignFile().getName();
 			// remove the file extension of the campaign file name
 			s = s.substring(0, s.length() - AppConstants.CAMPAIGN_FILE_EXTENSION.length());
 			campaignName = " - [" + s + "]";
 		}
 		setTitle(AppConstants.APP_NAME + campaignName + (renderer != null ? " - " + renderer.getZone().getName() : ""));
 	}
 
 	public Toolbox getToolbox() {
 		return toolbox;
 	}
 
 	public ZoneRenderer getZoneRenderer(Zone zone) {
 
 		for (ZoneRenderer renderer : zoneRendererList) {
 
 			if (zone == renderer.getZone()) {
 				return renderer;
 			}
 		}
 
 		return null;
 	}
 
 	public ZoneRenderer getZoneRenderer(GUID zoneGUID) {
 
 		for (ZoneRenderer renderer : zoneRendererList) {
 
 			if (zoneGUID.equals(renderer.getZone().getId())) {
 				return renderer;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Get the paintDrawingMeasurements for this MapToolClient.
 	 * 
 	 * @return Returns the current value of paintDrawingMeasurements.
 	 */
 	public boolean isPaintDrawingMeasurement() {
 		return paintDrawingMeasurement;
 	}
 
 	/**
 	 * Set the value of paintDrawingMeasurements for this MapToolClient.
 	 * 
 	 * @param aPaintDrawingMeasurements
 	 *            The paintDrawingMeasurements to set.
 	 */
 	public void setPaintDrawingMeasurement(boolean aPaintDrawingMeasurements) {
 		paintDrawingMeasurement = aPaintDrawingMeasurements;
 	}
 
 	public void showFullScreen() {
 		GraphicsConfiguration graphicsConfig = getGraphicsConfiguration();
 		GraphicsDevice device = graphicsConfig.getDevice();
 
 		Rectangle bounds = graphicsConfig.getBounds();
 
 		fullScreenFrame = new FullScreenFrame();
 		fullScreenFrame.add(zoneRendererPanel);
 
 		fullScreenFrame.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
 
 		fullScreenFrame.setJMenuBar(menuBar);
 		menuBar.setVisible(false);
 
 		fullScreenFrame.setVisible(true);
 
 		this.setVisible(false);
 	}
 
 	public boolean isFullScreen() {
 		return fullScreenFrame != null;
 	}
 
 	public void showWindowed() {
 		if (fullScreenFrame == null) {
 			return;
 		}
 
 		rendererBorderPanel.add(zoneRendererPanel);
 		setJMenuBar(menuBar);
 		menuBar.setVisible(true);
 		this.setVisible(true);
 
 		fullScreenFrame.dispose();
 
 		fullScreenFrame = null;
 	}
 
 	public class FullScreenFrame extends JFrame {
 
 		public FullScreenFrame() {
 			setUndecorated(true);
 
 		}
 	}
 
 	// APP EVENT LISTENER
 	public void handleAppEvent(AppEvent evt) {
 		if (evt.getId() != MapTool.ZoneEvent.Activated) {
 			return;
 		}
 
 		final Zone zone = (Zone) evt.getNewValue();
 		AssetAvailableListener listener = new AssetAvailableListener() {
 			public void assetAvailable(net.rptools.lib.MD5Key key) {
 				ZoneRenderer renderer = getCurrentZoneRenderer();
 				if (renderer.getZone() == zone) {
 					ImageManager.getImage(AssetManager.getAsset(key), renderer);
 				}
 			}
 		};
 
 		// Let's add all the assets, starting with the backgrounds
 		for (Token token : zone.getBackgroundStamps()) {
 			MD5Key key = token.getImageAssetId();
 
 			if (AssetManager.hasAsset(key)) {
 				ImageManager.getImage(AssetManager.getAsset(key));
 			} else {
 
 				if (!AssetManager.isAssetRequested(key)) {
 					AssetManager.addAssetListener(key, listener);
 					// This will force a server request if we don't already have it
 					AssetManager.getAsset(key);
 				}
 			}
 		}
 
 		// Now the stamps
 		for (Token token : zone.getStampTokens()) {
 			MD5Key key = token.getImageAssetId();
 
 			if (AssetManager.hasAsset(key)) {
 				ImageManager.getImage(AssetManager.getAsset(key));
 			} else {
 
 				if (!AssetManager.isAssetRequested(key)) {
 					AssetManager.addAssetListener(key, listener);
 					// This will force a server request if we don't already have it
 					AssetManager.getAsset(key);
 				}
 			}
 		}
 
 		// Now add the rest
 		for (Token token : zone.getAllTokens()) {
 			MD5Key key = token.getImageAssetId();
 
 			if (AssetManager.hasAsset(key)) {
 				ImageManager.getImage(AssetManager.getAsset(key));
 			} else {
 
 				if (!AssetManager.isAssetRequested(key)) {
 					AssetManager.addAssetListener(key, listener);
 					// This will force a server request if we don't already have it
 					AssetManager.getAsset(key);
 				}
 			}
 		}
 	}
 
 	// WINDOW LISTENER
 	public void windowOpened(WindowEvent e) {
 	}
 
 	public void windowClosing(WindowEvent e) {
 
 		if (!confirmClose()) {
 			return;
 		}
 
 		closingMaintenance();
 	}
 
 	public boolean confirmClose() {
 		if (MapTool.isHostingServer()) {
 			if (!MapTool.confirm("msg.confirm.hostingDisconnect")) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public void closingMaintenance() {
 		if (AppPreferences.getSaveReminder()) {
 			if (MapTool.getPlayer().isGM()) {
 				int result = JOptionPane.showConfirmDialog(MapTool.getFrame(), I18N.getText("msg.confirm.saveCampaign"), I18N.getText("msg.title.saveCampaign"), JOptionPane.YES_NO_CANCEL_OPTION);
 
 				if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
 					return;
 				}
 
 				if (result == JOptionPane.YES_OPTION) {
 					AppActions.SAVE_CAMPAIGN.actionPerformed(new ActionEvent(this, 0, "close"));
 					return;
 				}
 			} else {
 				if (!MapTool.confirm(I18N.getText("msg.confirm.disconnecting"))) {
 					return;
 				}
 			}
 		}
 		
 		close();
 	}
 	
 	public void close() {
 
 		ServerDisconnectHandler.disconnectExpected = true;
 		MapTool.disconnect();
 
 		getDockingManager().saveLayoutDataToFile(AppUtil.getAppHome("config").getAbsolutePath() + "/layout.dat");
 
 		// If closing cleanly, remove the autosave file
 		MapTool.getAutoSaveManager().purge();
 
 		setVisible(false);
 
 		// Not necessary since we'll release all resources when we close
 		// That and it seems to sometimes throw an NPE, go figure.
 		// dispose();
 	}
 
 	public void windowClosed(WindowEvent e) {
		System.exit(0);
 	}
 
 	public void windowIconified(WindowEvent e) {
 	}
 
 	public void windowDeiconified(WindowEvent e) {
 	}
 
 	public void windowActivated(WindowEvent e) {
 	}
 
 	public void windowDeactivated(WindowEvent e) {
 	}
 
 	// Windows OS defaults F10 to the menu bar, noooooo!! We want for macro
 	// buttons.
 	// XXX Doesn't work for Mac OSX. But do we really care? Shouldn't this
 	// keystroke be configurable via the properties file?
 	private void removeWindowsF10() {
 		InputMap imap = menuBar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
 		Object action = imap.get(KeyStroke.getKeyStroke("F10"));
 		ActionMap amap = menuBar.getActionMap();
 		amap.getParent().remove(action);
 	}
 
 	public void updateKeyStrokes() {
 		updateKeyStrokes(menuBar);
 
 		for (MTFrame frame : frameMap.keySet()) {
 			updateKeyStrokes(frameMap.get(frame));
 		}
 	}
 
 	private void updateKeyStrokes(JComponent c) {
 		c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).clear();
 		Map<KeyStroke, MacroButton> keyStrokeMap = MacroButtonHotKeyManager.getKeyStrokeMap();
 
 		if (c.getActionMap().keys() != null) {
 			for (Object o : c.getActionMap().keys()) {
 				if (o instanceof MacroButton) {
 					c.getActionMap().remove(o);
 				}
 			}
 		}
 		
 		for (KeyStroke keyStroke : keyStrokeMap.keySet()) {
 			final MacroButton button = keyStrokeMap.get(keyStroke);
 			c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, button);
 			c.getActionMap().put(button, new MTButtonHotKeyAction(button));
 		}
 	}
 
 	public CampaignPanel getCampaignPanel() {
 		return campaignPanel;
 	}
 
 	public GlobalPanel getGlobalPanel() {
 		return globalPanel;
 	}
 
 	public ImpersonatePanel getImpersonatePanel() {
 		return impersonatePanel;
 	}
 
 	public SelectionPanel getSelectionPanel() {
 		return selectionPanel;
 	}
 
 	public void resetTokenPanels() {
 		impersonatePanel.reset();
 		selectionPanel.reset();
 	}
 
 	// currently only used after loading a campaign
 	public void resetPanels() {
 		MacroButtonHotKeyManager.clearKeyStrokes();
 
 		campaignPanel.reset();
 		globalPanel.reset();
 		impersonatePanel.reset();
 		selectionPanel.reset();
 
 		updateKeyStrokes();
 	}
 
 	/**
 	 * @return Getter for initiativePanel
 	 */
 	public InitiativePanel getInitiativePanel() {
 		return initiativePanel;
 	}
 
 	private JFileChooser saveMacroFileChooser;
 	private JFileChooser saveMacroSetFileChooser;
 
 	public JFileChooser getSaveMacroFileChooser() {
 		if (saveMacroFileChooser == null) {
 			saveMacroFileChooser = new JFileChooser();
 			saveMacroFileChooser.setCurrentDirectory(AppPreferences.getSaveDir());
 			saveMacroFileChooser.addChoosableFileFilter(macroFilter);
 			saveMacroFileChooser.setDialogTitle(I18N.getText("msg.title.exportMacro"));
 		}
 		saveMacroFileChooser.setAcceptAllFileFilterUsed(true);
 		return saveMacroFileChooser;
 	}
 
 	public JFileChooser getSaveMacroSetFileChooser() {
 		if (saveMacroSetFileChooser == null) {
 			saveMacroSetFileChooser = new JFileChooser();
 			saveMacroSetFileChooser.setCurrentDirectory(AppPreferences.getSaveDir());
 			saveMacroSetFileChooser.addChoosableFileFilter(macroSetFilter);
 			saveMacroSetFileChooser.setDialogTitle(I18N.getText("msg.title.exportMacroSet"));
 		}
 		saveMacroSetFileChooser.setAcceptAllFileFilterUsed(true);
 		return saveMacroSetFileChooser;
 	}
 
 	private JFileChooser loadMacroFileChooser;
 	private JFileChooser loadMacroSetFileChooser;
 
 	public JFileChooser getLoadMacroFileChooser() {
 		if (loadMacroFileChooser == null) {
 			loadMacroFileChooser = new JFileChooser();
 			loadMacroFileChooser.setCurrentDirectory(AppPreferences.getLoadDir());
 			loadMacroFileChooser.addChoosableFileFilter(macroFilter);
 			loadMacroFileChooser.setDialogTitle(I18N.getText("msg.title.importMacro"));
 		}
 
 		loadMacroFileChooser.setFileFilter(macroFilter);
 		return loadMacroFileChooser;
 	}
 
 	public JFileChooser getLoadMacroSetFileChooser() {
 		if (loadMacroSetFileChooser == null) {
 			loadMacroSetFileChooser = new JFileChooser();
 			loadMacroSetFileChooser.setCurrentDirectory(AppPreferences.getLoadDir());
 			loadMacroSetFileChooser.addChoosableFileFilter(macroSetFilter);
 			loadMacroSetFileChooser.setDialogTitle(I18N.getText("msg.title.importMacroSet"));
 		}
 
 		loadMacroSetFileChooser.setFileFilter(macroSetFilter);
 		return loadMacroSetFileChooser;
 	}
 
 	// end of Macro import/export support
 
 	private JFileChooser saveTableFileChooser;
 
 	public JFileChooser getSaveTableFileChooser() {
 		if (saveTableFileChooser == null) {
 			saveTableFileChooser = new JFileChooser();
 			saveTableFileChooser.setCurrentDirectory(AppPreferences.getSaveDir());
 			saveTableFileChooser.addChoosableFileFilter(tableFilter);
 			saveTableFileChooser.setDialogTitle("Export Table");
 		}
 		saveTableFileChooser.setAcceptAllFileFilterUsed(true);
 		return saveTableFileChooser;
 	}
 
 	private JFileChooser loadTableFileChooser;
 
 	public JFileChooser getLoadTableFileChooser() {
 		if (loadTableFileChooser == null) {
 			loadTableFileChooser = new JFileChooser();
 			loadTableFileChooser.setCurrentDirectory(AppPreferences.getLoadDir());
 			loadTableFileChooser.addChoosableFileFilter(tableFilter);
 			loadTableFileChooser.setDialogTitle("Import Table");
 		}
 
 		loadTableFileChooser.setFileFilter(tableFilter);
 		return loadTableFileChooser;
 	}
 	// end of Table import/export support
 
 	@SuppressWarnings("serial")
 	private static class MTButtonHotKeyAction extends AbstractAction {
 
 		private final MacroButton macroButton;
 	
 		public MTButtonHotKeyAction(MacroButton button) {
 			macroButton = button;
 		}
 		 
 		public void actionPerformed(ActionEvent e) {
 			macroButton.getProperties().executeMacro();
 		}
 		
 	}
 }
 
