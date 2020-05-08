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
 package net.rptools.maptool.client;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Transparency;
 import java.awt.event.ActionEvent;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.imageio.ImageIO;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.KeyStroke;
 
 import net.rptools.lib.FileUtil;
 import net.rptools.lib.MD5Key;
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.maptool.client.tool.GridTool;
 import net.rptools.maptool.client.ui.ConnectToServerDialog;
 import net.rptools.maptool.client.ui.ConnectionStatusPanel;
 import net.rptools.maptool.client.ui.ExportDialog;
 import net.rptools.maptool.client.ui.NewMapDialog;
 import net.rptools.maptool.client.ui.PreferencesDialog;
 import net.rptools.maptool.client.ui.ServerInfoDialog;
 import net.rptools.maptool.client.ui.StartServerDialog;
 import net.rptools.maptool.client.ui.StaticMessageDialog;
 import net.rptools.maptool.client.ui.assetpanel.AssetPanel;
 import net.rptools.maptool.client.ui.assetpanel.Directory;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.client.ui.zone.ZoneSelectionPanel;
 import net.rptools.maptool.language.I18N;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Campaign;
 import net.rptools.maptool.model.CellPoint;
 import net.rptools.maptool.model.ExportInfo;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Player;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.ZoneFactory;
 import net.rptools.maptool.model.ZonePoint;
 import net.rptools.maptool.server.ServerConfig;
 import net.rptools.maptool.server.ServerPolicy;
 import net.rptools.maptool.util.PersistenceUtil;
 
 /**
  */
 public class AppActions {
 
 	private static Set<Token> tokenCopySet = null;
 
 	public static final Action EXPORT_SCREENSHOT = new DefaultClientAction() {
 		{
 			init("Export Screenshot as...");
 		}
 		public void execute(ActionEvent e) {
 
 			ExportInfo exportInfo = MapTool.getCampaign().getExportInfo();
 			ExportDialog dialog = new ExportDialog(exportInfo);
 			
 			dialog.setVisible(true);
 			
 			exportInfo = dialog.getExportInfo();
 			
 			if (exportInfo == null) {
 				return;
 			}
 			
 			MapTool.getCampaign().setExportInfo(exportInfo);
 			
 			exportScreenCap(exportInfo);
 		}
 	};
 	
 	public static final Action EXPORT_SCREENSHOT_LAST_LOCATION = new DefaultClientAction() {
 		{
 			init("Export Screenshot");
 			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
 		}
 		public void execute(ActionEvent e) {
 			
 			ExportInfo exportInfo = MapTool.getCampaign().getExportInfo();
 			if (exportInfo == null) {
 				EXPORT_SCREENSHOT.actionPerformed(e);
 				return;
 			}
 			
 			exportScreenCap(exportInfo);
 		}
 	};
 	
 	private static void exportScreenCap(ExportInfo exportInfo) {
 		
 		BufferedImage screenCap = null;
 		switch (exportInfo.getType()) {
 		case ExportInfo.Type.APPLICATION:
 			screenCap = MapTool.takeScreenShot();
 			break;
 		case ExportInfo.Type.CURRENT_VIEW:
 			screenCap = MapTool.takeMapScreenShot();
 			break;
 		case ExportInfo.Type.FULL_MAP:
 			break;
 		}
 
 		MapTool.getFrame().setStatusMessage("Saving screenshot ...");
 		
 		try {
 
 			ByteArrayOutputStream imageOut = new ByteArrayOutputStream();
 			
 			ImageIO.write(screenCap, "png", imageOut);
 
 			exportInfo.getLocation().putContent(new BufferedInputStream(new ByteArrayInputStream(imageOut.toByteArray())));
 			
 			MapTool.getFrame().setStatusMessage("Saved screenshot");
 
 		} catch (IOException ioe) {
 			MapTool.showError("Could not export image: " + ioe);
 			ioe.printStackTrace();
 		} catch (Exception e) {
 			MapTool.showError("Could not export image: " + e);
 			e.printStackTrace();
 		}
 	}
 	
 	public static final Action TOGGLE_COMMAND_PANEL = new DefaultClientAction() {
 		{
 			init("Command Panel");
 		}
 		
 		@Override
 		public boolean isSelected() {
 			return MapTool.getFrame().getCommandPanel().isVisible();
 		}
 		
 		public void execute(ActionEvent e) {
 			
 			if (MapTool.getFrame().getCommandPanel().isVisible()) {
 				MapTool.getFrame().hideCommandPanel();
 			} else {
 				MapTool.getFrame().showCommandPanel();
 			}
 		}
 	};
 	
 	public static final Action ENFORCE_ZONE = new AdminClientAction() {
 		
 		{
 			init("action.enforceZone");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return super.isAvailable()
 					&& (MapTool.isPersonalServer() || MapTool.isHostingServer());
 		}
 
 		public void execute(ActionEvent e) {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer == null) {
 				return;
 			}
 			
 			MapTool.serverCommand().enforceZone(renderer.getZone().getId());
 		}
 	};
 	
 	public static final Action RESTORE_DEFAULT_IMAGES = new DefaultClientAction() {
 		
 		{
 			init("Restore Default Images");
 		}
 
 		public void execute(ActionEvent e) {
 			try {
 				AppSetup.installDefaultTokens();
 				
 				// TODO: Remove this hardwiring
 		        File unzipDir = new File(AppConstants.UNZIP_DIR.getAbsolutePath() + File.separator + "Default");
 		        MapTool.getFrame().addAssetRoot(unzipDir);
 		    	AssetManager.searchForImageReferences(unzipDir, AppConstants.IMAGE_FILE_FILTER);
 
 			} catch (IOException ioe) {
 				MapTool.showError("Could not restore defaults: " + ioe);
 			}
 		}
 	};
 	
 	public static final Action SHOW_FULLSCREEN = new DefaultClientAction() {
 		
 		{
 			init("Fullscreen");
 			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt ENTER"));
 		}
 
 		public void execute(ActionEvent e) {
 			
 			if (MapTool.getFrame().isFullScreen()) {
 				MapTool.getFrame().showWindowed();
 			} else {
 				MapTool.getFrame().showFullScreen();
 			}
 		}
 	};
 	
 	public static final Action SHOW_SERVER_INFO = new DefaultClientAction() {
 		{
 			init("action.showServerInfo");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return super.isAvailable()
 					&& (MapTool.isPersonalServer() || MapTool.isHostingServer());
 		}
 
 		public void execute(ActionEvent e) {
 
 			if (MapTool.getServer() == null) {
 				return;
 			}
 			
 			ServerInfoDialog dialog = new ServerInfoDialog(MapTool.getServer());
 			dialog.setVisible(true);
 		}
 	};
 	
 	public static final Action SHOW_PREFERENCES = new DefaultClientAction() {
 		{
 			init("Preferences ...");
 		}
 
 		public void execute(ActionEvent e) {
 
 			// Probably don't have to create a new one each time
 			PreferencesDialog dialog = new PreferencesDialog();
 			dialog.setVisible(true);
 		}
 	};
 	
 	public static final Action SAVE_MESSAGE_HISTORY = new DefaultClientAction() {
 		{
 			init("action.saveMessageHistory");
 		}
 		
 		public void execute(ActionEvent e) {
 			String messageHistory = MapTool.getFrame().getCommandPanel().getMessageHistory();
 			
 	    	JFileChooser chooser = MapTool.getFrame().getSaveFileChooser();
 			chooser.setDialogTitle("Save Message History");
 			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 	
 			if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
 				return;
 			}
 			
 			File saveFile = chooser.getSelectedFile();
 			if (saveFile.getName().indexOf(".") < 0) {
 				saveFile = new File(saveFile.getAbsolutePath() + ".html");
 			}
 			if (saveFile.exists() && !MapTool.confirm("File exists, overwrite?")) {
 				return;
 			}
 			
 			try {
 				FileUtil.writeBytes(saveFile, messageHistory.getBytes());
 			} catch (IOException ioe) {
 				MapTool.showError("Could not save: " + ioe);
 			}
 		}
 	};
 	
 	public static final Action COPY_TOKENS = new DefaultClientAction() {
 		{
 			init("action.copyTokens");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return super.isAvailable()
 					&& MapTool.getFrame().getCurrentZoneRenderer() != null;
 		}
 
 		public void execute(ActionEvent e) {
 
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			Zone zone = renderer.getZone();
 			Set<GUID> selectedSet = renderer.getSelectedTokenSet();
 
 			Integer top = null;
 			Integer left = null;
 			tokenCopySet = new HashSet<Token>();
 			for (GUID guid : selectedSet) {
 				Token token = zone.getToken(guid);
 
 				if (top == null || token.getY() < top) {
 					top = token.getY();
 				}
 				if (left == null || token.getX() < left) {
 					left = token.getX();
 				}
 
 				tokenCopySet.add(new Token(token));
 			}
 
 			// Normalize
 			for (Token token : tokenCopySet) {
 				token.setX(token.getX() - left);
 				token.setY(token.getY() - top);
 			}
 		}
 
 	};
 
 	private static final Pattern NAME_PATTERN = Pattern.compile("^(\\D*)(\\d+)$");
 
 	public static final Action PASTE_TOKENS = new DefaultClientAction() {
 		{
 			init("action.pasteTokens");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return super.isAvailable() && tokenCopySet != null;
 		}
 
 		public void execute(ActionEvent e) {
 
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			Zone zone = renderer.getZone();
 
 			ScreenPoint screenPoint = renderer.getPointUnderMouse();
 			if (screenPoint == null) {
 				return;
 			}
 
 			boolean snapToGrid = false;
 			for (Token origToken : tokenCopySet) {
 				if (origToken.isSnapToGrid()) {
 					snapToGrid = true;
 				}
 			}
 
 			ZonePoint zonePoint = screenPoint.convertToZone(renderer);
 			if (snapToGrid) {
 
 				CellPoint cellPoint = zone.getGrid().convert(zonePoint);
 				zonePoint = zone.getGrid().convert(cellPoint);
 			}
 
 			for (Token origToken : tokenCopySet) {
 
 				Token token = new Token(origToken);
 
 				token.setX(token.getX() + zonePoint.x);
 				token.setY(token.getY() + zonePoint.y);
 
 				// If we're copying to the same zone, the token needs a new name
 				String name = token.getName();
 				Matcher m = NAME_PATTERN.matcher(token.getName());
 				if (m.find()) {
 					name = m.group(1);
 					int num = Integer.parseInt(m.group(2)) + 1;
 
 					// Find the next available token number, this
 					// has to break at some point.
 					while (zone.getTokenByName(name + num) != null) {
 						num++;
 					}
 
 					name += num;
 				} else {
 					name += "1";
 				}
 
 				token.setName(name);
 
 				zone.putToken(token);
 				MapTool.serverCommand().putToken(zone.getId(), token);
 			}
 
 			renderer.repaint();
 		}
 	};
 
 	public static final Action REMOVE_ASSET_ROOT = new DefaultClientAction() {
 		{
 			init("action.removeAssetRoot");
 		}
 
 		public void execute(ActionEvent e) {
 
 			AssetPanel assetPanel = MapTool.getFrame().getAssetPanel();
 			Directory dir = assetPanel.getSelectedAssetRoot();
 
 			if (dir == null) {
 				MapTool.showError("msg.error.mustSelectAssetGroupFirst");
 				return;
 			}
 
 			if (!assetPanel.isAssetRoot(dir)) {
 				MapTool.showError("msg.error.mustSelectRootGroup");
 				return;
 			}
 
 			AppPreferences.removeAssetRoot(dir.getPath());
 			assetPanel.removeAssetRoot(dir);
 		}
 
 	};
 
 	public static final Action TOGGLE_LINK_PLAYER_VIEW = new AdminClientAction() {
 		{
 			init("action.linkPlayerView");
 		}
 
 		@Override
 		public boolean isSelected() {
 			return AppState.isPlayerViewLinked();
 		}
 		
 		public void execute(ActionEvent e) {
 
 			AppState.setPlayerViewLinked(!AppState.isPlayerViewLinked());
 			if (AppState.isPlayerViewLinked()) {
 				ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 	        	ZonePoint zp = new ScreenPoint(renderer.getWidth()/2, renderer.getHeight()/2).convertToZone(renderer);
 				MapTool.serverCommand().enforceZoneView(renderer.getZone().getId(), zp.x, zp.y, renderer.getScaleIndex());
 			}
 		}
 
 	};
 
 	public static final Action TOGGLE_SHOW_MOVEMENT_MEASUREMENTS = new DefaultClientAction() {
 		{
 			init("Show Movement Measurements");
 		}
 
 		@Override
 		public boolean isSelected() {
 			return AppState.getShowMovementMeasurements();
 		}
 		
 		public void execute(ActionEvent e) {
 
 			AppState.setShowMovementMeasurements(!AppState.getShowMovementMeasurements());
 			if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
 				MapTool.getFrame().getCurrentZoneRenderer().repaint();
 			}
 		}
 
 	};
 
 	public static final Action TOGGLE_SHOW_LIGHT_RADIUS = new DefaultClientAction() {
 		{
 			init("action.showLightRadius");
 		}
 
 		@Override
 		public boolean isSelected() {
 			return AppState.isShowLightRadius();
 		}
 		
 		public void execute(ActionEvent e) {
 
 			AppState.setShowLightRadius(!AppState.isShowLightRadius());
 			if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
 				MapTool.getFrame().getCurrentZoneRenderer().repaint();
 			}
 		}
 
 	};
 
 	public static final Action REMOVE_ZONE = new ZoneAdminClientAction() {
 		{
 			init("action.removeZone");
 		}
 
 		public void execute(ActionEvent e) {
 
 			if (!MapTool.confirm("msg.confirm.removeZone")) {
 				return;
 			}
 
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			MapTool.serverCommand().removeZone(renderer.getZone().getId());
 			MapTool.getFrame().removeZoneRenderer(renderer);
 			MapTool.getCampaign().removeZone(renderer.getZone().getId());
 		}
 
 	};
 
 	public static final Action SHOW_ABOUT = new DefaultClientAction() {
 		{
 			init("action.showAboutDialog");
 		}
 
 		public void execute(ActionEvent e) {
 
 			MapTool.getFrame().showAboutDialog();
 		}
 
 	};
 
 	public static final Action ENFORCE_ZONE_VIEW = new ZoneAdminClientAction() {
 		{
 			init("action.enforceView");
 		}
 
 		public void execute(ActionEvent e) {
 
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer == null) {
 				return;
 			}
 
			MapTool.serverCommand().enforceZoneView(renderer.getZone().getId(),
					renderer.getViewOffsetX(), renderer.getViewOffsetY(),
					renderer.getScaleIndex());
 		}
 
 	};
 
 	/**
 	 * Start entering text into the chat field
 	 */
 	public static final String CHAT_COMMAND_ID = "action.sendChat";
 
 	public static final Action CHAT_COMMAND = new DefaultClientAction() {
 		{
 			init(CHAT_COMMAND_ID);
 		}
 
 		public void execute(ActionEvent e) {
 			if (!MapTool.getFrame().getCommandPanel().isVisible()) {
 				MapTool.getFrame().getCommandPanel().startChat();
 			} else {
 				MapTool.getFrame().hideCommandPanel();
 			}
 		}
 	};
 
 	public static final String COMMAND_UP_ID = "action.commandUp";
 	public static final String COMMAND_DOWN_ID = "action.commandDown";
 	
 	/**
 	 * Start entering text into the chat field
 	 */
 	public static final String ENTER_COMMAND_ID = "action.runMacro";
 
 	public static final Action ENTER_COMMAND = new DefaultClientAction() {
 		{
 			init(ENTER_COMMAND_ID);
 		}
 
 		public void execute(ActionEvent e) {
 			MapTool.getFrame().getCommandPanel().startMacro();
 		}
 	};
 
 	/**
 	 * Action tied to the chat field to commit the command.
 	 */
 	public static final String COMMIT_COMMAND_ID = "action.commitCommand";
 
 	public static final Action COMMIT_COMMAND = new DefaultClientAction() {
 		{
 			init(COMMIT_COMMAND_ID);
 		}
 
 		public void execute(ActionEvent e) {
 			MapTool.getFrame().getCommandPanel().commitCommand();
 		}
 	};
 
 	/**
 	 * Action tied to the chat field to commit the command.
 	 */
 	public static final String CANCEL_COMMAND_ID = "action.cancelCommand";
 
 	public static final Action CANCEL_COMMAND = new DefaultClientAction() {
 		{
 			init(CANCEL_COMMAND_ID);
 		}
 
 		public void execute(ActionEvent e) {
 			MapTool.getFrame().getCommandPanel().cancelCommand();
 		}
 	};
 
 	public static final Action RANDOMLY_ADD_LAST_ASSET = new DeveloperClientAction() {
 		{
 			init("action.debug.duplicateLastIcon");
 		}
 
 		public void execute(ActionEvent e) {
 
 			Asset asset = AssetManager.getLastRetrievedAsset();
 			for (int i = 0; i < 100; i++) {
 
 				Token token = new Token(asset.getId());
 				token.setX(MapToolUtil.getRandomNumber(100 * 5));
 				token.setY(MapToolUtil.getRandomNumber(100 * 5));
 				MapTool.getFrame().getCurrentZoneRenderer().getZone().putToken(
 						token);
 			}
 			MapTool.getFrame().getCurrentZoneRenderer().repaint();
 		}
 
 	};
 
 	public static final Action ADJUST_GRID = new ZoneAdminClientAction() {
 		{
 			init("action.adjustGrid");
 		}
 
 		public void execute(ActionEvent e) {
 
 			MapTool.getFrame().getToolbox().setSelectedTool(GridTool.class);
 		}
 
 	};
 
 	public static final Action TOGGLE_GRID = new DefaultClientAction() {
 		{
 			init("action.showGrid");
 			putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
 			try {
 				putValue(Action.SMALL_ICON, new ImageIcon(ImageUtil
 						.getImage("net/rptools/maptool/client/image/grid.gif")));
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 		}
 
 		public boolean isSelected() {
 			return AppState.isShowGrid();
 		}
 		
 		public void execute(ActionEvent e) {
 
 			AppState.setShowGrid(!AppState.isShowGrid());
 
 			if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
 				MapTool.getFrame().getCurrentZoneRenderer().repaint();
 			}
 		}
 	};
 
 	public static final Action TOGGLE_FOG = new ZoneAdminClientAction() {
 		{
 			init("action.enableFogOfWar");
 		}
 		
 		@Override
 		public boolean isSelected() {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer == null) {
 				return false;
 			}
 			
 			return renderer.getZone().hasFog();
 		}
 
 		public void execute(ActionEvent e) {
 
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer == null) {
 				return;
 			}
 
 			Zone zone = renderer.getZone();
 			zone.setHasFog(!zone.hasFog());
 
 			MapTool.serverCommand().setZoneHasFoW(zone.getId(), zone.hasFog());
 
 			renderer.repaint();
 		}
 	};
 
 	public static final Action TOGGLE_SHOW_TOKEN_NAMES = new DefaultClientAction() {
 		{
 			init("action.showNames");
 			putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
 			try {
 				putValue(
 						Action.SMALL_ICON,
 						new ImageIcon(
 								ImageUtil
 										.getImage("net/rptools/maptool/client/image/names.png")));
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 		}
 
 		public void execute(ActionEvent e) {
 
 			AppState.setShowTokenNames(!AppState.isShowTokenNames());
 			if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
 				MapTool.getFrame().getCurrentZoneRenderer().repaint();
 			}
 		}
 	};
 
 	public static final Action TOGGLE_CURRENT_ZONE_VISIBILITY = new ZoneAdminClientAction() {
 
 		{
 			init("action.hideMap");
 		}
 		
 		@Override
 		public boolean isSelected() {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer == null) {
 				return false;
 			}
 			return renderer.getZone().isVisible();
 		}
 
 		public void execute(ActionEvent e) {
 
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer == null) {
 				return;
 			}
 
 			// TODO: consolidate this code with ZonePopupMenu
 			Zone zone = renderer.getZone();
 			zone.setVisible(!zone.isVisible());
 
 			MapTool.serverCommand().setZoneVisibility(zone.getId(),
 					zone.isVisible());
 			MapTool.getFrame().getZoneSelectionPanel().flush();
 			MapTool.getFrame().repaint();
 		}
 	};
 
 	public static final Action NEW_CAMPAIGN = new AdminClientAction() {
 		{
 			init("action.newCampaign");
 		}
 
 		public void execute(ActionEvent e) {
 
 			if (!MapTool.confirm("msg.confirm.newCampaign")) {
 
 				return;
 			}
 
 			Campaign campaign = new Campaign();
 			MapTool.setCampaign(campaign);
 			MapTool.serverCommand().setCampaign(campaign);
 		}
 	};
 
 	public static final Action ZOOM_IN = new DefaultClientAction() {
 		{
 			init("action.zoomIn");
 		}
 
 		public void execute(ActionEvent e) {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer != null) {
 				Dimension size = renderer.getSize();
 				renderer.zoomIn(size.width / 2, size.height / 2);
 			}
 		}
 	};
 
 	public static final Action ZOOM_OUT = new DefaultClientAction() {
 		{
 			init("action.zoomOut");
 		}
 
 		public void execute(ActionEvent e) {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer != null) {
 				Dimension size = renderer.getSize();
 				renderer.zoomOut(size.width / 2, size.height / 2);
 			}
 		}
 	};
 
 	public static final Action ZOOM_RESET = new DefaultClientAction() {
 		{
 			init("action.zoom100");
 		}
 
 		public void execute(ActionEvent e) {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer != null) {
 				renderer.zoomReset();
 			}
 		}
 	};
 
 	public static final Action TOGGLE_ZONE_SELECTOR = new DefaultClientAction() {
 		{
 			init("action.showMapSelector");
 		}
 		
 		@Override
 		public boolean isSelected() {
 			return MapTool.getFrame().getZoneSelectionPanel().isVisible();
 		}
 
 		public void execute(ActionEvent e) {
 
 			ZoneSelectionPanel panel = MapTool.getFrame()
 					.getZoneSelectionPanel();
 
 			panel.setVisible(!panel.isVisible());
 		}
 	};
 
 	public static final Action TOGGLE_MOVEMENT_LOCK = new AdminClientAction() {
 		{
 			init("action.toggleMovementLock");
 		}
 		
 		@Override
 		public boolean isSelected() {
 			return MapTool.getServerPolicy().isMovementLocked();
 		}
 
 		public void execute(ActionEvent e) {
 
 			ServerPolicy policy = MapTool.getServerPolicy();
 			policy.setIsMovementLocked(!policy.isMovementLocked());
 			
 			MapTool.updateServerPolicy(policy);
 		}
 	};
 
 	public static final Action START_SERVER = new ClientAction() {
 		{
 			init("action.serverStart");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return MapTool.isPersonalServer();
 		}
 
 		public void execute(ActionEvent e) {
 
 			runBackground(new Runnable() {
 				public void run() {
 
 					if (!MapTool.isPersonalServer()) {
 						MapTool.showError("Already running a server.");
 						return;
 					}
 
 					// TODO: Need to shut down the existing server first;
 					StartServerDialog dialog = new StartServerDialog();
 
 					dialog.setVisible(true);
 
 					if (dialog.getOption() == StartServerDialog.OPTION_CANCEL) {
 						return;
 					}
 
 					ServerPolicy policy = new ServerPolicy();
 					policy.setUseStrictTokenManagement(dialog.useStrictTokenMovement());
 
 					ServerConfig config = new ServerConfig(
 							dialog.getGMPassword(), 
 							dialog.getPlayerPassword(),
 							dialog.getPort(),
 							dialog.registerServer(),
 							dialog.getServerName(),
 							dialog.getServerPassword()
 							);
 
 					// Use the existing campaign
 					Campaign campaign = MapTool.getCampaign();
 
 					boolean failed = false;
 					try {
 						ServerDisconnectHandler.disconnectExpected = true;
 						MapTool.stopServer();
 						MapTool.startServer(dialog.getUsername(), config, policy, campaign);
 
 						// Connect to server
 						MapTool.createConnection("localhost", dialog.getPort(),
 								new Player(dialog.getUsername(), dialog.getRole(), dialog.getGMPassword()));
 
 						// connecting
 						MapTool.getFrame().getConnectionStatusPanel().setStatus(ConnectionStatusPanel.Status.server);
 					} catch (UnknownHostException uh) {
 						MapTool.showError("Whoah, 'localhost' is not a valid address.  Weird.");
 						failed = true;
 					} catch (IOException ioe) {
 						MapTool.showError("Could not connect to server: " + ioe);
 						failed = true;
 					}
 
 					if (failed) {
 						try {
 							MapTool.startPersonalServer(campaign);
 						} catch (IOException ioe) {
 							MapTool.showError("Could not restart personal server");
 						}
 					}
 
 					MapTool.serverCommand().setCampaign(campaign);
 				}
 			});
 		}
 
 	};
 
 	public static final Action CONNECT_TO_SERVER = new ClientAction() {
 		{
 			init("action.clientConnect");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return MapTool.isPersonalServer();
 		}
 
 		public void execute(ActionEvent e) {
 
 			final ConnectToServerDialog dialog = new ConnectToServerDialog();
 
 			dialog.setVisible(true);
 
 			if (dialog.getOption() == ConnectToServerDialog.OPTION_CANCEL) {
 
 				return;
 			}
 
 			ServerDisconnectHandler.disconnectExpected = true;
 			MapTool.stopServer();
 
 			// connecting
 			MapTool.getFrame().getConnectionStatusPanel().setStatus(
 					ConnectionStatusPanel.Status.connected);
 			
 			runBackground(new Runnable() {
 
 				public void run() {
 					boolean failed = false;
 					Campaign campaign = MapTool.getCampaign();
 					StaticMessageDialog progressDialog = new StaticMessageDialog("Connecting");
 					try {
 						// I'm going to get struck by lighting for writing code like this.
 						// CLEAN ME CLEAN ME CLEAN ME !  I NEED A SWINGWORKER !
 						MapTool.getFrame().showFilledGlassPane(progressDialog);
 						
 						MapTool.createConnection(dialog.getServer(), dialog.getPort(),
 								new Player(dialog.getUsername(), dialog.getRole(),
 										dialog.getPassword()));
 
 					} catch (UnknownHostException e1) {
 						MapTool.showError("Unknown host");
 						failed = true;
 					} catch (IOException e1) {
 						MapTool.showError("IO Error: " + e1);
 						failed = true;
 					} finally {
 						MapTool.getFrame().hideGlassPane();
 					}
 					
 					if (failed) {
 						try {
 							MapTool.startPersonalServer(campaign);
 						} catch (IOException ioe) {
 							MapTool.showError("Could not restart personal server");
 						}
 					}
 				}
 			});
 
 
 		}
 
 	};
 
 	public static final Action DISCONNECT_FROM_SERVER = new ClientAction() {
 
 		{
 			init("action.clientDisconnect");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return !MapTool.isPersonalServer();
 		}
 
 		public void execute(ActionEvent e) {
 
 			if (MapTool.isHostingServer()
 					&& !MapTool.confirm("msg.confirm.hostingDisconnect")) {
 				return;
 			}
 
 			Campaign campaign = MapTool.isHostingServer() ? MapTool
 					.getCampaign() : new Campaign();
 			ServerDisconnectHandler.disconnectExpected = true;
 			MapTool.stopServer();
 			MapTool.disconnect();
 
 			try {
 				MapTool.startPersonalServer(campaign);
 			} catch (IOException ioe) {
 				MapTool.showError("Could not restart personal server");
 			}
 		}
 
 	};
 
 	public static final Action LOAD_CAMPAIGN = new DefaultClientAction() {
 		{
 			init("action.loadCampaign");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return MapTool.isHostingServer() || MapTool.isPersonalServer();
 		}
 
 		public void execute(ActionEvent ae) {
 
 			final JFileChooser chooser = MapTool.getFrame().getLoadFileChooser();
 			chooser.setDialogTitle("Load Campaign");
 			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
 			if (chooser.showOpenDialog(MapTool.getFrame()) == JFileChooser.APPROVE_OPTION) {
 
 				new Thread() {
 					public void run() {
 						
 						try {
 							File campaignFile = chooser.getSelectedFile();
 							StaticMessageDialog progressDialog = new StaticMessageDialog("Loading Campaign");
 							try {
 								// I'm going to get struck by lighting for writing code like this.
 								// CLEAN ME CLEAN ME CLEAN ME !  I NEED A SWINGWORKER !
 								MapTool.getFrame().showFilledGlassPane(progressDialog);
 
 								Campaign campaign = PersistenceUtil.loadCampaign(campaignFile);
 
 								if (campaign != null) {
 
 									AppState.setCampaignFile(campaignFile);
 									AppPreferences.setLoadDir(campaignFile.getParentFile());
 									
 									MapTool.setCampaign(campaign);
 
 									MapTool.serverCommand().setCampaign(campaign);
 									
 								}
 
 							} finally {
 								MapTool.getFrame().hideGlassPane();
 							}
 
 						} catch (IOException ioe) {
 							MapTool.showError("Could not load campaign: " + ioe);
 						}
 					}
 				}.start();
 			}
 		}
 	};
 
 	public static final Action SAVE_CAMPAIGN = new DefaultClientAction() {
 		{
 			init("action.saveCampaign");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return (MapTool.isHostingServer() || MapTool.getPlayer().isGM());
 		}
 		
 		public void execute(ActionEvent ae) {
 
 			if (AppState.getCampaignFile() == null) {
 				SAVE_CAMPAIGN_AS.actionPerformed(ae);
 				return;
 			}
 			
 			// save to same place
 			Campaign campaign = MapTool.getCampaign();
 
 			try {
 				PersistenceUtil.saveCampaign(campaign, AppState.getCampaignFile());
 				MapTool.showInformation("msg.info.campaignSaved");
 			} catch (IOException ioe) {
 				MapTool.showError("Could not save campaign: " + ioe);
 			}
 		}
 	};
 
 	public static final Action SAVE_CAMPAIGN_AS = new DefaultClientAction() {
 		{
 			init("action.saveCampaignAs");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return MapTool.isHostingServer() || MapTool.getPlayer().isGM();
 		}
 		
 		public void execute(ActionEvent ae) {
 
 			Campaign campaign = MapTool.getCampaign();
 
 			JFileChooser chooser = MapTool.getFrame().getSaveFileChooser();
 			chooser.setDialogTitle("Save Campaign");
 
 			if (chooser.showSaveDialog(MapTool.getFrame()) == JFileChooser.APPROVE_OPTION) {
 
 				try {
 					File campaignFile = chooser.getSelectedFile();
 					PersistenceUtil.saveCampaign(campaign, campaignFile);
 					
 					AppState.setCampaignFile(campaignFile);
 					AppPreferences.setSaveDir(campaignFile.getParentFile());
 
 					MapTool.showInformation("msg.info.campaignSaved");
 				} catch (IOException ioe) {
 					MapTool.showError("Could not save campaign: " + ioe);
 				}
 			}
 		}
 	};
 
 	public static class GridSizeAction extends DefaultClientAction {
 
 		private int size;
 		
 		public GridSizeAction(int size) {
 			putValue(Action.NAME, Integer.toString(size));
 			this.size = size;
 		}
 
 		@Override
 		public boolean isSelected() {
 			return AppState.getGridSize() == size;
 		}
 		
 		@Override
 		public void execute(ActionEvent arg0) {
 			AppState.setGridSize(size);
 			MapTool.getFrame().refresh();
 		}
 	}
 	
 	private static final int QUICK_MAP_ICON_SIZE = 25;
 
 	public static class QuickMapAction extends AdminClientAction {
 
 		private MD5Key assetId;
 
 		public QuickMapAction(String name, String imagePath) {
 
 			try {
 				Asset asset = new Asset(name, FileUtil.loadResource(imagePath));
 				assetId = asset.getId();
 
 				// Make smaller
 				BufferedImage iconImage = new BufferedImage(
 						QUICK_MAP_ICON_SIZE, QUICK_MAP_ICON_SIZE,
 						Transparency.OPAQUE);
 				BufferedImage image = ImageUtil.getCompatibleImage(imagePath);
 
 				Graphics2D g = iconImage.createGraphics();
 				g.drawImage(image, 0, 0, QUICK_MAP_ICON_SIZE,
 						QUICK_MAP_ICON_SIZE, null);
 				g.dispose();
 
 				putValue(Action.SMALL_ICON, new ImageIcon(iconImage));
 				putValue(Action.NAME, name);
 
 				// Put it in the cache for easy access
 				AssetManager.putAsset(asset);
 				
 				// But don't use up any extra memory
 				AssetManager.removeAsset(asset.getId());
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 			getActionList().add(this);
 		}
 
 		public void execute(java.awt.event.ActionEvent e) {
 
 			runBackground(new Runnable() {
 
 				public void run() {
 
 					Zone zone = ZoneFactory.createZone(Zone.Type.INFINITE, assetId);
 					zone.getGrid().setOffset(0, 0);
 					zone.setGridColor(AppConstants.DEFAULT_GRID_COLOR.getRGB());
 
 					MapTool.addZone(zone);
 				}
 			});
 		}
 	};
 
 	public static final Action NEW_MAP = new AdminClientAction() {
 		{
 			init("action.newMap");
 		}
 
 		public void execute(java.awt.event.ActionEvent e) {
 
 			runBackground(new Runnable() {
 
 				public void run() {
 
 					NewMapDialog newMapDialog = MapTool.getFrame().getNewMapDialog();
 
 					Asset asset = newMapDialog.showDialog();
 					if (asset == null) {
 						return;
 					}
 
 					// Keep track of the image
 					if (!AssetManager.hasAsset(asset)) {
 						AssetManager.putAsset(asset);
 						MapTool.serverCommand().putAsset(asset);
 					}
 
 					// Create the zone
 					Zone zone = ZoneFactory.createZone(newMapDialog.getZoneType(), newMapDialog.getZoneName(), newMapDialog.getZoneFeetPerCell(), asset.getId());
 					zone.setGrid(newMapDialog.getZoneGrid());
 
 					zone.setGridColor(AppConstants.DEFAULT_GRID_COLOR.getRGB());
 					
 					MapTool.addZone(zone);
 				}
 			});
 		}
 	};
 
 	public static final Action TOGGLE_ASSET_PANEL = new DefaultClientAction() {
 		{
 			init("action.showInformationPanel");
 		}
 
 		@Override
 		public boolean isSelected() {
 			return MapTool.getFrame().isAssetTreeVisible();
 		}
 		
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see net.rptools.maptool.client.ClientActions.ClientAction#execute(java.awt.event.ActionEvent)
 		 */
 		public void execute(ActionEvent e) {
 
 			MapTool.getFrame().toggleAssetTree();
 		}
 	};
 
 	public static final Action ADD_ASSET_PANEL = new DefaultClientAction() {
 		{
 			init("action.addIconSelector");
 		}
 
 		public void execute(ActionEvent e) {
 
 			runBackground(new Runnable() {
 
 				public void run() {
 
 					JFileChooser chooser = MapTool.getFrame().getLoadFileChooser();
 					chooser.setDialogTitle("Load Asset Tree");
 					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 
 					if (chooser.showOpenDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
 						return;
 					}
 
 					File root = chooser.getSelectedFile();
 					MapTool.getFrame().addAssetRoot(root);
 			    	AssetManager.searchForImageReferences(root, AppConstants.IMAGE_FILE_FILTER);
 
 			    	AppPreferences.addAssetRoot(root);
 				}
 
 			});
 		}
 	};
 
 	public static final Action EXIT = new DefaultClientAction() {
 		{
 			init("action.exit");
 		}
 
 		public void execute(ActionEvent ae) {
 
 			// TODO: if connected, then show confirmation dialog
 			System.exit(0);
 		}
 	};
 
 	/**
 	 * Toggle the drawing of measurements.
 	 */
 	public static final Action TOGGLE_DRAW_MEASUREMENTS = new DefaultClientAction() {
 		{
 			init("action.toggleDrawMeasuements");
 		}
 
 		@Override
 		public boolean isSelected() {
 			return MapTool.getFrame().isPaintDrawingMeasurement();
 		}
 		
 		public void execute(ActionEvent ae) {
 			MapTool.getFrame().setPaintDrawingMeasurement(
 					!MapTool.getFrame().isPaintDrawingMeasurement());
 		}
 	};
   
 	  /**
 	   * Toggle drawing straight lines at double width on the line tool.
 	   */
 	  public static final Action TOGGLE_DOUBLE_WIDE = new DefaultClientAction() {
 	    {
 	      init("action.toggleDoubleWide");
 	    }
 	    
 	    @Override
 	    public boolean isSelected() {
 	    	return AppState.useDoubleWideLine();
 	    }
 
 	    public void execute(ActionEvent ae) {
 	    	
 	    	AppState.setUseDoubleWideLine(!AppState.useDoubleWideLine());
         if (MapTool.getFrame() != null && MapTool.getFrame().getCurrentZoneRenderer() != null)
           MapTool.getFrame().getCurrentZoneRenderer().repaint();
 	    }
 	  };
 
 	private static List<ClientAction> actionList;
 
 	private static List<ClientAction> getActionList() {
 		if (actionList == null) {
 			actionList = new ArrayList<ClientAction>();
 		}
 
 		return actionList;
 	}
 
 	public static void updateActions() {
 
 		for (ClientAction action : actionList) {
 			action.setEnabled(action.isAvailable());
 		}
 		
 		MapTool.getFrame().getToolbox().updateTools();
 	}
 
 	public static abstract class ClientAction extends AbstractAction {
 
 		public void init(String key) {
 			String name = net.rptools.maptool.language.I18N.getText(key);
 			putValue(NAME, name);
 			int mnemonic = I18N.getMnemonic(key);
 			if (mnemonic != -1) {
 				putValue(MNEMONIC_KEY, KeyEvent.VK_C);
 			}
 
 			String accel = I18N.getAccelerator(key);
 			if (accel != null) {
 				putValue(ACCELERATOR_KEY, KeyStroke.getAWTKeyStroke(accel));
 			}
 			String description = I18N.getDescription(key);
 			if (description != null) {
 				putValue(SHORT_DESCRIPTION, description);
 			}
 
 			getActionList().add(this);
 		}
 
 		public abstract boolean isAvailable();
 		
 		public boolean isSelected() {
 			return false;
 		}
 
 		public final void actionPerformed(ActionEvent e) {
 			execute(e);
 			// System.out.println(getValue(Action.NAME));
 			updateActions();
 		}
 
 		public abstract void execute(ActionEvent e);
 
 		public void runBackground(final Runnable r) {
 			new Thread() {
 				public void run() {
 					try {
 						MapTool.startIndeterminateAction();
 						r.run();
 					} finally {
 						MapTool.endIndeterminateAction();
 					}
 
 					updateActions();
 				}
 			}.start();
 		}
 	}
 
 	public static abstract class AdminClientAction extends ClientAction {
 
 		@Override
 		public boolean isAvailable() {
 			return MapTool.getPlayer().isGM();
 		}
 	}
 
 	public static abstract class ZoneAdminClientAction extends
 			AdminClientAction {
 
 		@Override
 		public boolean isAvailable() {
 			return super.isAvailable()
 					&& MapTool.getFrame().getCurrentZoneRenderer() != null;
 		}
 	}
 
 	public static abstract class DefaultClientAction extends ClientAction {
 
 		@Override
 		public boolean isAvailable() {
 			return true;
 		}
 	}
 
 	public static abstract class DeveloperClientAction extends ClientAction {
 
 		@Override
 		public boolean isAvailable() {
 			return System.getProperty("MAPTOOL_DEV") != null
 					&& "true".equals(System.getProperty("MAPTOOL_DEV"));
 		}
 	}
 }
