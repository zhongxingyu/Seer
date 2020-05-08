 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.client.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.awt.FlowLayout;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
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
 import javax.swing.Timer;
 import javax.swing.border.BevelBorder;
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
 import net.rptools.maptool.client.AppStyle;
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ServerDisconnectHandler;
 import net.rptools.maptool.client.swing.CoordinateStatusBar;
 import net.rptools.maptool.client.swing.GlassPane;
 import net.rptools.maptool.client.swing.MemoryStatusBar;
 import net.rptools.maptool.client.swing.ProgressStatusBar;
 import net.rptools.maptool.client.swing.ScrollableFlowPanel;
 import net.rptools.maptool.client.swing.SpacerStatusBar;
 import net.rptools.maptool.client.swing.StatusPanel;
 import net.rptools.maptool.client.tool.PointerTool;
 import net.rptools.maptool.client.tool.StampTool;
 import net.rptools.maptool.client.ui.assetpanel.AssetDirectory;
 import net.rptools.maptool.client.ui.assetpanel.AssetPanel;
 import net.rptools.maptool.client.ui.commandpanel.CommandPanel;
 import net.rptools.maptool.client.ui.token.TokenPropertiesDialog;
 import net.rptools.maptool.client.ui.tokenpanel.TokenPanelTreeCellRenderer;
 import net.rptools.maptool.client.ui.tokenpanel.TokenPanelTreeModel;
 import net.rptools.maptool.client.ui.zone.NotificationOverlay;
 import net.rptools.maptool.client.ui.zone.PointerOverlay;
 import net.rptools.maptool.client.ui.zone.ZoneMiniMapPanel;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
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
 	
 	// Components
 	private ZoneRenderer currentRenderer;
 	private AssetPanel assetPanel;
 	private ClientConnectionPanel connectionPanel;
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
 	private JLabel chatActionLabel;
 	private GlassPane glassPane;
 	
 	private TextureChooserPanel textureChooserPanel;
 
 	// Components
 	private JFileChooser loadFileChooser;
 
 	private JFileChooser saveFileChooser;
 
 	private TokenPropertiesDialog tokenPropertiesDialog;
 
 	// TODO: Find a better pattern for this
 	private Timer repaintTimer;
 
 	public MapToolFrame() {
 
 		// Set up the frame
 		super(AppConstants.APP_NAME);
 		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 		addWindowListener(this);
 		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
 		SwingUtil.centerOnScreen(this);
 		setFocusTraversalPolicy(new MapToolFocusTraversalPolicy());
 		
 		try {
 			setIconImage(ImageUtil.getImage("net/rptools/maptool/client/image/minilogo.png"));
 		} catch (IOException ioe) {
 			System.err.println ("Could not load icon image");
 		}
 
 		// Components
 		glassPane = new GlassPane();
 		assetPanel = createAssetPanel();
 		connectionPanel = createConnectionPanel();
 		toolbox = new Toolbox();
 
 		zoneRendererList = new CopyOnWriteArrayList<ZoneRenderer>();
 		pointerOverlay = new PointerOverlay();
 		
 		colorPicker = new ColorPicker(this);
 		textureChooserPanel = new TextureChooserPanel(colorPicker.getPaintChooser(), assetPanel.getModel(), "imageExplorerTextureChooser");
 		colorPicker.getPaintChooser().addPaintChooser(textureChooserPanel);
 
 		String credits = "";
 		String version = "";
 		Image logo = null;
 		try {
 			credits = new String(FileUtil
 					.loadResource("net/rptools/maptool/client/credits.html"));
 			version = MapTool.getVersion();
 			credits = credits.replace("%VERSION%", version);
 			logo = ImageUtil.getImage("net/rptools/maptool/client/image/maptool-logo.png");
 
 		} catch (Exception ioe) {
 			System.err.println("Unable to load credits or version");
 		}
 		aboutDialog = new AboutDialog(this, logo, credits);
 		aboutDialog.setSize(354, 400);
 
 		statusPanel = new StatusPanel();
 		statusPanel.addPanel(getCoordinateStatusBar());
 		statusPanel.addPanel(new MemoryStatusBar());
 		// statusPanel.addPanel(progressBar);
 		statusPanel.addPanel(connectionStatusPanel);
 		statusPanel.addPanel(activityMonitor);
 		statusPanel.addPanel(new SpacerStatusBar(25));
 
 		zoneMiniMapPanel = new ZoneMiniMapPanel();
 		zoneMiniMapPanel.setSize(100, 100);
 
 		zoneRendererPanel = new JPanel(new PositionalLayout(5));
 		zoneRendererPanel.setBackground(Color.black);
 //		zoneRendererPanel.add(zoneMiniMapPanel, PositionalLayout.Position.SE);
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
 		setLayout(new BorderLayout());
 		add(BorderLayout.NORTH, new ToolbarPanel(toolbox));
 		add(BorderLayout.SOUTH, statusPanel);
 
 		setGlassPane(glassPane);
 
 		removeWindowsF10();
 		configureDocking();
 		
 		MapTool.getEventDispatcher().addListener(this, MapTool.ZoneEvent.Activated);
 
 		new FramePreferences(AppConstants.APP_NAME, "mainFrame", this);
 //		setSize(800, 600);
 		restorePreferences();
 
 		repaintTimer = new Timer(2000, new RepaintTimer());
 		repaintTimer.start();
 	}
 
 	public enum MTFrame {
 		CONNECTIONS("Connections"),
 		TOKEN_TREE("Token Tree"),
 		IMAGE_EXPLORER("Image Explorer"),
 		CHAT("Chat"),
 		MACROS("Macros");
 		
 		private String displayName;
 		
 		private MTFrame(String displayName) {
 			this.displayName = displayName;
 		}
 		public String toString() {
 			return displayName;
 		}
 	}
 	private void configureDocking() {
 		
 		initializeFrames();
 		
 
 		getDockingManager().setProfileKey(DOCKING_PROFILE_NAME);
 		getDockingManager().setOutlineMode(com.jidesoft.docking.DockingManager.PARTIAL_OUTLINE_MODE);
 		getDockingManager().setUsePref(false);
 		getDockingManager().setLayoutDirectory(AppUtil.getAppHome("config").getAbsolutePath());
 
 		getDockingManager().getWorkspace().setAcceptDockableFrame(false);
 
 		// Main panel
 		getDockingManager().getWorkspace().add(rendererBorderPanel);
 		
 		// Docked frames
 		getDockingManager().addFrame(getFrame(MTFrame.CONNECTIONS));
 		getDockingManager().addFrame(getFrame(MTFrame.TOKEN_TREE));
 		getDockingManager().addFrame(getFrame(MTFrame.IMAGE_EXPLORER));
 		getDockingManager().addFrame(getFrame(MTFrame.CHAT));
 		getDockingManager().addFrame(getFrame(MTFrame.MACROS));
 		
 		try {
 			getDockingManager().loadInitialLayout(MapToolFrame.class.getClassLoader().getResourceAsStream("net/rptools/maptool/client/ui/ilayout.xml"));
 		} catch (ParserConfigurationException e) {
 			MapTool.showError("Could not parse the layout file");
 			e.printStackTrace();
 		} catch (SAXException e) {
 			MapTool.showError("Could not parse the layout file");
 			e.printStackTrace();
 		} catch (IOException e) {
 			MapTool.showError("Could not load the layout file");
 			e.printStackTrace();
 		}
         getDockingManager().loadLayoutData();
 		
 	}
 	
 	public DockableFrame getFrame(MTFrame frame) {
 		return frameMap.get(frame);
 	}
 	
 	private void initializeFrames() {
 		
 		frameMap.put(MTFrame.CONNECTIONS,createDockingFrame(MTFrame.CONNECTIONS, new JScrollPane(connectionPanel)));
 		frameMap.put(MTFrame.TOKEN_TREE, createDockingFrame(MTFrame.TOKEN_TREE, new JScrollPane(createTokenTreePanel())));
 		frameMap.put(MTFrame.IMAGE_EXPLORER, createDockingFrame(MTFrame.IMAGE_EXPLORER, assetPanel));
 		frameMap.put(MTFrame.CHAT, createDockingFrame(MTFrame.CHAT, commandPanel));
 		frameMap.put(MTFrame.MACROS, createDockingFrame(MTFrame.MACROS, new JScrollPane(createMacroButtonPanel(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)));
 		
 	}
 	
 	private static DockableFrame createDockingFrame(MTFrame mtFrame, Component component) {
 		DockableFrame frame = new DockableFrame(mtFrame.name());
 		frame.setTabTitle(mtFrame.toString());
 		frame.add(component);
 		
 		return frame;
 	}
 	
 	public JPanel createMacroButtonPanel() {
 		JPanel panel = new ScrollableFlowPanel(FlowLayout.LEFT);
 
 		for (int i = 1; i <= 100; i++) {
 			panel.add(new MacroButton(i, null, true, false));
 		}
 		
 		return panel;
 	}
 	
 	public TokenPropertiesDialog getTokenPropertiesDialog() {
 		if (tokenPropertiesDialog == null) {
 			tokenPropertiesDialog = new TokenPropertiesDialog();
 		}
 		return tokenPropertiesDialog;
 	}
 
 	public void refresh() {
 		if (getCurrentZoneRenderer() != null) {
 			getCurrentZoneRenderer().repaint();
 		}
 	}
 
 	public JFileChooser getLoadFileChooser() {
 		if (loadFileChooser == null) {
 			loadFileChooser = new JFileChooser();
 			loadFileChooser.setCurrentDirectory(AppPreferences.getLoadDir());
 		}
 		return loadFileChooser;
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
 		tree.getSelectionModel().setSelectionMode(
 				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
 		tree.addMouseListener(new MouseAdapter() {
 			// TODO: Make this a handler class, not an aic
 			@Override
 			public void mousePressed(MouseEvent e) {
 				// tree.setSelectionPath(tree.getPathForLocation(e.getX(),
 				// e.getY()));
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
 							getCurrentZoneRenderer().centerOn(
 									new ZonePoint(token.getX(), token.getY()));
 
 							// Pick an appropriate tool
 							if (token.isToken()) {
 								getToolbox().setSelectedTool(PointerTool.class);
 							} else {
 								getCurrentZoneRenderer().setActiveLayer(
 										token.isStamp() ? Zone.Layer.OBJECT
 												: Zone.Layer.BACKGROUND);
 								getToolbox().setSelectedTool(StampTool.class);
 							}
 
 							getCurrentZoneRenderer().selectToken(token.getId());
 							getCurrentZoneRenderer().requestFocusInWindow();
 						}
 					}
 				}
 				if (SwingUtilities.isRightMouseButton(e)) {
 
 					if (!isRowSelected(tree.getSelectionRows(), rowIndex)
 							&& !SwingUtil.isShiftDown(e)) {
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
 									Token token = (Token) path
 											.getLastPathComponent();
 									if (firstToken == null) {
 										firstToken = token;
 									}
 
 									if (AppUtil.playerOwns(token)) {
 										selectedTokenSet.add(token.getId());
 									}
 								}
 							}
 							if (selectedTokenSet.size() > 0) {
 
 								if (firstToken.isStamp()
 										|| firstToken.isBackground()) {
 
 									new StampPopupMenu(selectedTokenSet, x, y,
 											getCurrentZoneRenderer(),
 											firstToken).showPopup(tree);
 								} else {
 									new TokenPopupMenu(selectedTokenSet, x, y,
 											getCurrentZoneRenderer(),
 											firstToken).showPopup(tree);
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
 	}
 
 	public void updateTokenTree() {
 		if (tokenPanelTreeModel != null) {
 			tokenPanelTreeModel.update();
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
 
 	private AssetPanel createAssetPanel() {
 		final AssetPanel panel = new AssetPanel("mainAssetPanel");
 		panel.addImagePanelMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent e) {
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
 
				if (SwingUtilities.isRightMouseButton(e)) {
 
 					List<Object> idList = panel.getSelectedIds();
 					if (idList == null || idList.size() == 0) {
 						return;
 					}
 
 					final int index = (Integer) idList.get(0);
 
 					JPopupMenu menu = new JPopupMenu();
 					menu.add(new JMenuItem(new AbstractAction() {
 						{
 							putValue(NAME, "New Map");
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
 
 		assetPanel.addAssetRoot(new AssetDirectory(rootDir,
 				AppConstants.IMAGE_FILE_FILTER));
 
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
 			currentRenderer.setRepaintTimer(null);
 		}
 
 		if (renderer != null) {
 			zoneRendererPanel.add(renderer, PositionalLayout.Position.CENTER);
 			zoneRendererPanel.doLayout();
 		}
 
 		currentRenderer = renderer;
 		toolbox.setTargetRenderer(renderer);
 
 		if (renderer != null) {
 			MapTool.getEventDispatcher().fireEvent(MapTool.ZoneEvent.Activated, this, null, renderer.getZone());
 			renderer.requestFocusInWindow();
 			renderer.setRepaintTimer(repaintTimer);
 		}
 
 		AppActions.updateActions();
 		repaint();
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
 
 		fullScreenFrame.setBounds(bounds.x, bounds.y, bounds.width,
 				bounds.height);
 
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
 
 	////
 	// APP EVENT LISTENER
 	public void handleAppEvent(AppEvent evt) {
 		if (evt.getId() != MapTool.ZoneEvent.Activated) {
 			return;
 		}
 		
 		final Zone zone = (Zone)evt.getNewValue();
 		AssetAvailableListener listener = new AssetAvailableListener() {
 			public void assetAvailable(net.rptools.lib.MD5Key key) {
 				ZoneRenderer renderer = getCurrentZoneRenderer();
 				if (renderer.getZone() == zone) {
 					ImageManager.getImage(AssetManager.getAsset(key),
 							renderer);
 				}
 			}
 		};
 
 		// Let's add all the assets, starting with the backgrounds
 		for (Token token : zone.getBackgroundTokens()) {
 
 			MD5Key key = token.getImageAssetId();
 
 			if (AssetManager.hasAsset(key)) {
 				ImageManager.getImage(AssetManager.getAsset(key));
 			} else {
 
 				if (!AssetManager.isAssetRequested(key)) {
 					AssetManager.addAssetListener(token.getImageAssetId(),
 							listener);
 
 					// This will force a server request if we don't already
 					// have it
 					AssetManager.getAsset(token.getImageAssetId());
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
 					AssetManager.addAssetListener(token.getImageAssetId(),
 							listener);
 
 					// This will force a server request if we don't already
 					// have it
 					AssetManager.getAsset(token.getImageAssetId());
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
 
 					// This will force a server request if we don't already
 					// have it
 					AssetManager.getAsset(token.getImageAssetId());
 				}
 			}
 		}
 	}
 
 	// //
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
 			if (!MapTool.confirm("You are hosting a server.  Shutting down will disconnect all players.  Are you sure?")) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public void closingMaintenance() {
 		if (AppPreferences.getSaveReminder()) {
 			if (MapTool.getPlayer().isGM()) {
 				int result = JOptionPane.showConfirmDialog(
 						MapTool.getFrame(), 
 						"Would you like to save your campaign before you exit?", 
 						"Save Reminder", 
 						JOptionPane.YES_NO_CANCEL_OPTION);
 				
 				if (result == JOptionPane.CANCEL_OPTION) {
 					return;
 				}
 				
 				if(result  == JOptionPane.YES_OPTION) {
 					AppActions.SAVE_CAMPAIGN.actionPerformed(null);
 				}
 			} else {
 
 				if (!MapTool.confirm("You will be disconnected.  Are you sure you want to exit?")) {
 					return;
 				}
 			}
 		}
 		
 		ServerDisconnectHandler.disconnectExpected = true;
 		MapTool.disconnect();
 
 		getDockingManager().saveLayoutData();
 		
 		// If closing cleanly, then remove the autosave file
 		MapTool.getAutoSaveManager().purge();
 
 		setVisible(false);
 		dispose();
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
 
 	// //
 	// REPAINT TIMER
 	private class RepaintTimer implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 			ZoneRenderer renderer = getCurrentZoneRenderer();
 			if (renderer != null) {
 				renderer.repaint();
 			}
 		}
 	}
 	
 	//Windows OS defaults F10 to the menubar, noooooo!! We want for macro buttons
 	private void removeWindowsF10() {
 		
 		InputMap imap = menuBar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
 		Object action = imap.get(KeyStroke.getKeyStroke("F10"));
 		ActionMap amap = menuBar.getActionMap();
 		amap.getParent().remove(action);
 	}
 }
