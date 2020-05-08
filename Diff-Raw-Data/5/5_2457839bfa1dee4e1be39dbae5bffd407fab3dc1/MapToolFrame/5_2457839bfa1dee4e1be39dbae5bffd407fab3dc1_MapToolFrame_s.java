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
 import java.io.File;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Observer;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JToolBar;
 import javax.swing.JTree;
 import javax.swing.SwingUtilities;
 import javax.swing.border.BevelBorder;
 import javax.swing.plaf.basic.BasicSplitPaneDivider;
 import javax.swing.plaf.basic.BasicSplitPaneUI;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import net.rptools.lib.FileUtil;
 import net.rptools.lib.MD5Key;
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.lib.swing.AboutDialog;
 import net.rptools.lib.swing.ColorPicker;
 import net.rptools.lib.swing.JSplitPaneEx;
 import net.rptools.lib.swing.PositionalLayout;
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.lib.swing.TaskPanelGroup;
 import net.rptools.lib.swing.preference.FramePreferences;
 import net.rptools.lib.swing.preference.SplitPanePreferences;
 import net.rptools.lib.swing.preference.TaskPanelGroupPreferences;
 import net.rptools.maptool.client.AppActions;
 import net.rptools.maptool.client.AppConstants;
 import net.rptools.maptool.client.AppListeners;
 import net.rptools.maptool.client.AppPreferences;
 import net.rptools.maptool.client.AppStyle;
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ServerDisconnectHandler;
 import net.rptools.maptool.client.ZoneActivityListener;
 import net.rptools.maptool.client.swing.CoordinateStatusBar;
 import net.rptools.maptool.client.swing.GlassPane;
 import net.rptools.maptool.client.swing.MemoryStatusBar;
 import net.rptools.maptool.client.swing.PenWidthChooser;
 import net.rptools.maptool.client.swing.ProgressStatusBar;
 import net.rptools.maptool.client.swing.SpacerStatusBar;
 import net.rptools.maptool.client.swing.StatusPanel;
 import net.rptools.maptool.client.tool.FacingTool;
 import net.rptools.maptool.client.tool.GridTool;
 import net.rptools.maptool.client.tool.MeasureTool;
 import net.rptools.maptool.client.tool.PointerTool;
 import net.rptools.maptool.client.tool.StampTool;
 import net.rptools.maptool.client.tool.TextTool;
 import net.rptools.maptool.client.tool.drawing.ConeTemplateTool;
 import net.rptools.maptool.client.tool.drawing.FreehandExposeTool;
 import net.rptools.maptool.client.tool.drawing.FreehandTool;
 import net.rptools.maptool.client.tool.drawing.LineTemplateTool;
 import net.rptools.maptool.client.tool.drawing.LineTool;
 import net.rptools.maptool.client.tool.drawing.OvalExposeTool;
 import net.rptools.maptool.client.tool.drawing.OvalTool;
 import net.rptools.maptool.client.tool.drawing.PolygonExposeTool;
 import net.rptools.maptool.client.tool.drawing.RadiusTemplateTool;
 import net.rptools.maptool.client.tool.drawing.RectangleExposeTool;
 import net.rptools.maptool.client.tool.drawing.RectangleTool;
 import net.rptools.maptool.client.ui.assetpanel.AssetDirectory;
 import net.rptools.maptool.client.ui.assetpanel.AssetPanel;
 import net.rptools.maptool.client.ui.commandpanel.CommandPanel;
 import net.rptools.maptool.client.ui.token.TokenPropertiesDialog;
 import net.rptools.maptool.client.ui.tokenpanel.TokenPanelTreeCellRenderer;
 import net.rptools.maptool.client.ui.tokenpanel.TokenPanelTreeModel;
 import net.rptools.maptool.client.ui.zone.NewZoneDropPanel;
 import net.rptools.maptool.client.ui.zone.NotificationOverlay;
 import net.rptools.maptool.client.ui.zone.PointerOverlay;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.client.ui.zone.ZoneSelectionPanel;
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
 import net.rptools.maptool.model.drawing.Pen;
 import net.rptools.maptool.util.ImageManager;
 
 /**
  */
 public class MapToolFrame extends JFrame implements WindowListener {
     private static final long serialVersionUID = 3905523813025329458L;
 
 	// TODO: parameterize this (or make it a preference)
 	private static final int WINDOW_WIDTH = 800;
 	private static final int WINDOW_HEIGHT = 600;
 
     private Pen pen = new Pen(Pen.DEFAULT);
     
     /**
      * Are the drawing measurements being painted?
      */
     private boolean paintDrawingMeasurement = true;
     
 	// Components
     private TaskPanelGroup taskPanel;
 	private ZoneRenderer currentRenderer;
 	private AssetPanel assetPanel;
 	private PointerOverlay pointerOverlay;
 	private CommandPanel commandPanel;
     private AboutDialog aboutDialog;
     private ColorPicker colorPicker;
     private NewMapDialog newMapDialog;
     private Toolbox toolbox;
     private ZoneSelectionPanel zoneSelectionPanel;
     private JPanel zoneRendererPanel;
     private JPanel visibleControlPanel;
     private FullScreenFrame fullScreenFrame;
     private JPanel rendererBorderPanel;    
     private List<ZoneRenderer> zoneRendererList;
     private JMenuBar menuBar;
     
 	private JSplitPaneEx mainSplitPane;
 	private JSplitPaneEx rightSplitPane;
 	
     private PenWidthChooser widthChooser = new PenWidthChooser();
 
 	private StatusPanel statusPanel;
 	private ActivityMonitorPanel activityMonitor = new ActivityMonitorPanel();
 	private ProgressStatusBar progressBar = new ProgressStatusBar();
     private ConnectionStatusPanel connectionStatusPanel = new ConnectionStatusPanel();
     private CoordinateStatusBar coordinateStatusBar;
     
 	private NewZoneDropPanel newZoneDropPanel;
 	
 	private JLabel chatActionLabel;
 	
 	private GlassPane glassPane;
   
     // Components
 	private JFileChooser loadFileChooser;
 	private JFileChooser saveFileChooser;
 	
 	private TokenPropertiesDialog tokenPropertiesDialog = new TokenPropertiesDialog();
 
     // TODO: I don't like this here, eventOverlay should be more abstracted
     private NotificationOverlay notificationOverlay = new NotificationOverlay();
 	
 	public MapToolFrame() {
 		
 		// Set up the frame
 		super (AppConstants.APP_NAME);
 		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 		addWindowListener(this);
 		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
 		SwingUtil.centerOnScreen(this);
 		setFocusTraversalPolicy(new MapToolFocusTraversalPolicy());
         
 		// Components
 		glassPane = new GlassPane();
 		assetPanel = createAssetPanel();
         taskPanel = new TaskPanelGroup(5);
         new TaskPanelGroupPreferences(AppConstants.APP_NAME, "TaskPanel", taskPanel);
         toolbox = new Toolbox();
         
         zoneRendererList = new CopyOnWriteArrayList<ZoneRenderer>();
         pointerOverlay = new PointerOverlay();
         colorPicker = new ColorPicker(this);
         
         String credits = "";
         String version = "";
         Image logo = null;
         try {
             credits = new String(FileUtil.loadResource("net/rptools/maptool/client/credits.html"));
             version = MapTool.getVersion();
             credits = credits.replace("%VERSION%", version);
             logo = ImageUtil.getImage("net/rptools/lib/image/rptools-logo.png");
         	
         } catch (Exception ioe) {
         	ioe.printStackTrace();
         }
         aboutDialog = new AboutDialog(this, logo, credits);
         aboutDialog.setSize(354, 400);
 
         taskPanel.add("Image Explorer", assetPanel);
         taskPanel.add("Tokens", new JScrollPane(createTokenTreePanel()));
         taskPanel.add("Connections", new JScrollPane(createPlayerList()));
         
         statusPanel = new StatusPanel();
         statusPanel.addPanel(getCoordinateStatusBar());
         statusPanel.addPanel(new MemoryStatusBar());
         //statusPanel.addPanel(progressBar);
         statusPanel.addPanel(connectionStatusPanel);
         statusPanel.addPanel(activityMonitor);
         statusPanel.addPanel(new SpacerStatusBar(25));
         
         zoneSelectionPanel = new ZoneSelectionPanel();
         zoneSelectionPanel.setSize(100, 100);
         AppListeners.addZoneListener(zoneSelectionPanel);
 
         newZoneDropPanel = new NewZoneDropPanel();
         
         zoneRendererPanel = new JPanel(new PositionalLayout(5));
         zoneRendererPanel.setBackground(Color.black);
         zoneRendererPanel.add(newZoneDropPanel, PositionalLayout.Position.CENTER);
         zoneRendererPanel.add(zoneSelectionPanel, PositionalLayout.Position.SE);
         zoneRendererPanel.add(getChatActionLabel(), PositionalLayout.Position.SW);
         
         commandPanel = new CommandPanel();
         MapTool.getMessageList().addObserver(commandPanel);
         MapTool.getMessageList().addObserver(createChatIconMessageObserver());
         
         rendererBorderPanel = new JPanel(new GridLayout());
         rendererBorderPanel.add(zoneRendererPanel);
         rendererBorderPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
         
         // Split up/down
         rightSplitPane = new JSplitPaneEx();
         rightSplitPane.setBorder(null);
         rightSplitPane.setOrientation(JSplitPaneEx.VERTICAL_SPLIT);
 		BasicSplitPaneDivider divider = ((BasicSplitPaneUI) rightSplitPane.getUI()).getDivider();
 		if (divider != null) divider.setBorder(null);
 
 		rightSplitPane.setTopComponent(rendererBorderPanel);
 		rightSplitPane.setBottomComponent(commandPanel);
 		
 		// Split left/right
 		mainSplitPane = new JSplitPaneEx();
 		mainSplitPane.setBorder(null);
 		
 		mainSplitPane.setLeftComponent(taskPanel);
 		mainSplitPane.setRightComponent(rightSplitPane);
 		divider = ((BasicSplitPaneUI) mainSplitPane.getUI()).getDivider();
 		if (divider != null) divider.setBorder(null);
         
 		JPanel mainInnerPanel = new JPanel(new BorderLayout());
 		mainInnerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 1));
 		mainInnerPanel.add(BorderLayout.CENTER, mainSplitPane);
 		
 		// Put it all together
 		menuBar = new AppMenuBar();
         setJMenuBar(menuBar);
 		setLayout(new BorderLayout());
 		add(BorderLayout.CENTER, mainInnerPanel);
 		add(BorderLayout.NORTH, createToolboxPanel());
 		add(BorderLayout.SOUTH, statusPanel);
 		
 		setGlassPane(glassPane);
         
 		// TODO: Put together a class that handles adding in the listeners, just so that this doesn't
 		// get all cluttered
 		AppListeners.addZoneListener(new RequestZoneAssetsListener());
 		
         new FramePreferences(AppConstants.APP_NAME, "mainFrame", this);
         
         restorePreferences();
 	}
 	
 	public TokenPropertiesDialog getTokenPropertiesDialog() {
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
 	
 	public void showControlPanel(JPanel panel) {
 
 		panel.setSize(panel.getPreferredSize());
         zoneRendererPanel.add(panel, PositionalLayout.Position.NE);
         zoneRendererPanel.setComponentZOrder(panel, 0);
         
         visibleControlPanel = panel;
 	}
 
 	public CoordinateStatusBar getCoordinateStatusBar() {
 		if (coordinateStatusBar == null) {
 			coordinateStatusBar = new CoordinateStatusBar();
 		}
 		return coordinateStatusBar;
 	}
 	
 	public void hideControlPanel() {
 		if (visibleControlPanel != null) {
 			
			zoneRendererPanel.remove(visibleControlPanel);
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
 	
 	@Override
 	public void setVisible(boolean b) {
 		mainSplitPane.setInitialDividerPosition(150);
         rightSplitPane.setInitialDividerPosition(getSize().height-200);
         new SplitPanePreferences(AppConstants.APP_NAME, "mainSplitPane", mainSplitPane);
         new SplitPanePreferences(AppConstants.APP_NAME, "rightSplitPane", rightSplitPane);
 		super.setVisible(b);
 		hideCommandPanel();
 	}
 	
 	public JLabel getChatActionLabel() {
 		if (chatActionLabel == null) {
 			chatActionLabel = new JLabel(new ImageIcon(AppStyle.chatImage)); 
 			chatActionLabel.setSize(chatActionLabel.getPreferredSize());
 			chatActionLabel.setVisible(false);
 			chatActionLabel.addMouseListener(new MouseAdapter(){
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
 			    ObservableList.Event event = (ObservableList.Event)arg; 
 
 			    if (rightSplitPane.isBottomHidden() && event == ObservableList.Event.append) {
 					
 					getChatActionLabel().setVisible(true);
 				}
 			}
 		};
 	}
 	
 	public void showCommandPanel() {
 		chatActionLabel.setVisible(false);
 		rightSplitPane.showBottom();
 		commandPanel.requestFocus();
 	}
 	
 	public void hideCommandPanel() {
 		rightSplitPane.hideBottom();
 	}
 	
 	public NewMapDialog getNewMapDialog() {
         if (newMapDialog == null) {
             newMapDialog = new NewMapDialog(this);
         }
 		return newMapDialog;
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
     
     public NotificationOverlay getNotificationOverlay() {
         return notificationOverlay;
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
 //				tree.setSelectionPath(tree.getPathForLocation(e.getX(), e.getY()));
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
 	                        if (token.isToken()) {
 	                        	getToolbox().setSelectedTool(PointerTool.class);
 	                        } else {
 	                        	getCurrentZoneRenderer().setActiveLayer(token.isStamp() ? Zone.Layer.STAMP : Zone.Layer.BACKGROUND);
 	                        	getToolbox().setSelectedTool(StampTool.class);
 	                        }
 	                        
 	                        getCurrentZoneRenderer().selectToken(token.getId());
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
                         		
                         		if (firstToken.isStamp() || firstToken.isBackground()) {
                         			
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
     	
 		AppListeners.addZoneListener(new ZoneActivityListener() {
 			public void zoneActivated(Zone zone) {
 				tokenPanelTreeModel.setZone(zone);
 			}
 			public void zoneAdded(Zone zone) {
 				// nothing to do
 			}
 		});
 		
     	return tree;
     }
 
     
     public void updateTokenTree() {
     	tokenPanelTreeModel.update();
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
     
     private AssetPanel createAssetPanel() {
         final AssetPanel panel = new AssetPanel("mainAssetPanel");
         panel.addImagePanelMouseListener(new MouseAdapter(){
             @Override
             public void mouseReleased(MouseEvent e) {
                 // TODO use for real popup logic
                 if (SwingUtilities.isRightMouseButton(e)) {
 
                     List<Object> idList = panel.getSelectedIds();
                     if (idList == null || idList.size() == 0) {
                         return;
                     }
                     
                     final int index = (Integer) idList.get(0);
                     
                     JPopupMenu menu = new JPopupMenu();
                     menu.add(new JMenuItem(new AbstractAction() {
                         {
                             putValue(NAME, "New Bounded Map");
                         }
 
                         public void actionPerformed(ActionEvent e) {
 
                             createZone(panel.getAsset(index), Zone.Type.MAP);
                         }
                     }));
                     menu.add(new JMenuItem(new AbstractAction() {
                         {
                             putValue(NAME, "New Unbounded Map");
                         }
                         public void actionPerformed(ActionEvent e) {
                             createZone(panel.getAsset(index), Zone.Type.INFINITE);
                         }
                     }));
                     
                     panel.showImagePanelPopup(menu, e.getX(), e.getY());
                 }
             }
             
             private void createZone(Asset asset, int type) {
                 
                 if (!AssetManager.hasAsset(asset)) {
                     
                     AssetManager.putAsset(asset);
                     MapTool.serverCommand().putAsset(asset);
                 }
 
                 Zone zone = ZoneFactory.createZone(type, asset.getId());
                 MapTool.addZone(zone);
             }
         });
         
         return panel;
     }
     
 	public PointerOverlay getPointerOverlay() {
 		return pointerOverlay;
 	}
 	
 	public void setStatusMessage(final String message) {
 		SwingUtilities.invokeLater(new Runnable(){
 			public void run() {
 			statusPanel.setStatus("  " + message);
 			}
 		});
 	}
 	
     protected JComponent createPlayerList() {
         
     	ClientConnectionPanel panel = new ClientConnectionPanel();
         
         return panel;
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
     
 	public ZoneSelectionPanel getZoneSelectionPanel() {
 		return zoneSelectionPanel;
 	}
     
     ///////////////////////////////////////////////////////////////////////////
     // static methods
     ///////////////////////////////////////////////////////////////////////////
     
     public void toggleAssetTree() {
         
         if (mainSplitPane.isLeftHidden()) {
             mainSplitPane.showLeft();
         } else {
             mainSplitPane.hideLeft();
         }
     }
     
     public boolean isAssetTreeVisible() {
     	return !mainSplitPane.isLeftHidden();
     }
     
     public CommandPanel getCommandPanel() {
     	return commandPanel;
     }
     
     public AssetPanel getAssetPanel() {
       return assetPanel;
     }
     
     public void addAssetRoot(File rootDir) {
         
     	assetPanel.addAssetRoot(new AssetDirectory(rootDir, AppConstants.IMAGE_FILE_FILTER));
     	
         if (mainSplitPane.isLeftHidden()) {
             mainSplitPane.showLeft();
         }
     }
     
 	private JComponent createToolboxPanel() {
 
         JPanel panel = new JPanel(new GridBagLayout());
         
         JToolBar toolbar = new JToolBar();
         toolbar.setFloatable(false);
         toolbar.setRollover(true);
 
         // Tools
         toolbar.add(toolbox.createTool(PointerTool.class));
         toolbar.add(toolbox.createTool(StampTool.class));
         toolbar.add(toolbox.createTool(MeasureTool.class));
         
         toolbar.add(Box.createHorizontalStrut(15));
         
         toolbar.add(toolbox.createTool(FreehandTool.class));
         toolbar.add(toolbox.createTool(LineTool.class));
         toolbar.add(toolbox.createTool(RectangleTool.class));
         toolbar.add(toolbox.createTool(OvalTool.class));
         //Tool textTool = new DrawnTextTool();
         toolbar.add(toolbox.createTool(TextTool.class));
         toolbar.add(toolbox.createTool(RadiusTemplateTool.class));
         toolbar.add(toolbox.createTool(ConeTemplateTool.class));
         toolbar.add(toolbox.createTool(LineTemplateTool.class));
         
         toolbar.add(Box.createHorizontalStrut(15));
         
         toolbar.add(toolbox.createTool(RectangleExposeTool.class));
         toolbar.add(toolbox.createTool(OvalExposeTool.class));
         toolbar.add(toolbox.createTool(PolygonExposeTool.class));
         toolbar.add(toolbox.createTool(FreehandExposeTool.class));
         
         // Non visible tools
         toolbox.createTool(GridTool.class);
         toolbox.createTool(FacingTool.class);
         
         toolbar.add(Box.createHorizontalStrut(15));
 
         // Initialy selected
         toolbox.setSelectedTool(PointerTool.class);
         
         // Organize
 
         toolbar.add(widthChooser);
 
         toolbar.add(Box.createHorizontalStrut(15));
         
         GridBagConstraints constraints = new GridBagConstraints();
         panel.add(toolbar, constraints);
         
         constraints.weightx = 1;
         constraints.gridx = 1;
         panel.add(new JLabel(), constraints);
         
         return panel;
 	}
 	
     public Pen getPen() {
     	
     	pen.setColor(colorPicker.getForegroundColor().getRGB());
     	pen.setBackgroundColor(colorPicker.getBackgroundColor().getRGB());
         pen.setThickness((Integer)widthChooser.getSelectedItem());
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
     		setCurrentZoneRenderer(zoneRendererList.size() > 0 ? zoneRendererList.get(0) : null);
     	}
     	
     	zoneSelectionPanel.flush();
     	zoneSelectionPanel.repaint();
     }
     
     public void clearZoneRendererList() {
         zoneRendererList.clear();
         zoneSelectionPanel.flush();
     	zoneSelectionPanel.repaint();
     }
 	public void setCurrentZoneRenderer(ZoneRenderer renderer) {
         
         // Handle new renderers
         // TODO: should this be here ?
         if (renderer != null && !zoneRendererList.contains(renderer)) {
             zoneRendererList.add(renderer);
         }
 
         // Handle first renderer
         if (newZoneDropPanel != null) {
         	zoneRendererPanel.remove(newZoneDropPanel);
         	newZoneDropPanel = null;
         }
         
         if (currentRenderer != null) {
         	currentRenderer.flush();
             zoneRendererPanel.remove(currentRenderer);
         }
         
 		// Back to the pointer
 		getToolbox().setSelectedTool(PointerTool.class);
 		
         if (renderer != null) {
             zoneRendererPanel.add(renderer, PositionalLayout.Position.CENTER);
             zoneRendererPanel.doLayout();
         }
         
 		currentRenderer = renderer;
 		toolbox.setTargetRenderer(renderer);
 
 		if (renderer != null) {
 			AppListeners.fireZoneActivated(renderer.getZone());
 			renderer.requestFocusInWindow();
 		}
 
 		updateTokenTree();
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
    * @param aPaintDrawingMeasurements The paintDrawingMeasurements to set.
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
 	
   private class RequestZoneAssetsListener implements ZoneActivityListener {
 	  public void zoneActivated(final Zone zone) {
 
 		  AssetAvailableListener listener = new AssetAvailableListener() {
 			  public void assetAvailable(net.rptools.lib.MD5Key key) {
 				  ZoneRenderer renderer = getCurrentZoneRenderer();
 				  if (renderer.getZone() == zone) {
 					  ImageManager.getImage(AssetManager.getAsset(key), renderer);
 				  }
 			  }
 		  };
 		  
 		  // Let's add all the assets, starting with the backgrounds
 		  for (Token token : zone.getBackgroundTokens()) {
 			  
 			  MD5Key key = token.getAssetID();
 
 			  if (AssetManager.hasAsset(key)) {
 				  ImageManager.getImage(AssetManager.getAsset(key));
 			  } else {
 				  
 				  if (!AssetManager.isAssetRequested(key)) {
 					  AssetManager.addAssetListener(token.getAssetID(), listener);
 					  
 					  // This will force a server request if we don't already have it
 					  AssetManager.getAsset(token.getAssetID());
 				  }
 			  }
 		  }
 
 		  // Now the stamps
 		  for (Token token : zone.getStampTokens()) {
 			  MD5Key key = token.getAssetID();
 			  
 			  if (AssetManager.hasAsset(key)) {
 				  ImageManager.getImage(AssetManager.getAsset(key));
 			  } else {
 				  
 				  if (!AssetManager.isAssetRequested(key)) {
 					  AssetManager.addAssetListener(token.getAssetID(), listener);
 					  
 					  // This will force a server request if we don't already have it
 					  AssetManager.getAsset(token.getAssetID());
 				  }
 			  }
 		  }
 		  
 		  // Now add the rest
 		  for (Token token : zone.getAllTokens()) {
 			  MD5Key key = token.getAssetID();
 			  
 			  if (AssetManager.hasAsset(key)) {
 				  ImageManager.getImage(AssetManager.getAsset(key));
 			  } else {
 				  
 				  if (!AssetManager.isAssetRequested(key)) {
 					  AssetManager.addAssetListener(key, listener);
 					  
 					  // This will force a server request if we don't already have it
 					  AssetManager.getAsset(token.getAssetID());
 				  }
 			  }
 		  }
 	  }
 	  public void zoneAdded(Zone zone) {
 	  }
   }
   
   ////
   // WINDOW LISTENER
   public void windowOpened(WindowEvent e){}
   public void windowClosing(WindowEvent e){
 	  
 	  if (MapTool.isHostingServer()) {
 		  if (!MapTool.confirm("You are hosting a server.  Shutting down will disconnect all players.  Are you sure?")) {
 			  return;
 		  }
 	  }
 
 	  ServerDisconnectHandler.disconnectExpected = true;
 	  MapTool.disconnect();
 	  
 	  // We're done
 	  EventQueue.invokeLater(new Runnable() {
 		  public void run() {
 			  System.exit(0);
 		  }
 	  });
   }
   public void windowClosed(WindowEvent e){}
   public void windowIconified(WindowEvent e){}
   public void windowDeiconified(WindowEvent e){}
   public void windowActivated(WindowEvent e){}
   public void windowDeactivated(WindowEvent e){}
 
 }
