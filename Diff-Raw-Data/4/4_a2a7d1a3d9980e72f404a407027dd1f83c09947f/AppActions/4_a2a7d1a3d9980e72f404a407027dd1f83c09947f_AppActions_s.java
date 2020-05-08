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
 import java.awt.event.ActionEvent;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 import java.net.UnknownHostException;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.KeyStroke;
 
 import net.rptools.common.util.FileUtil;
 import net.rptools.common.util.ImageUtil;
 import net.rptools.maptool.client.tool.GridTool;
 import net.rptools.maptool.client.ui.ConnectToServerDialog;
 import net.rptools.maptool.client.ui.ConnectionStatusPanel;
 import net.rptools.maptool.client.ui.StartServerDialog;
 import net.rptools.maptool.client.ui.assetpanel.AssetPanel;
 import net.rptools.maptool.client.ui.assetpanel.Directory;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.client.ui.zone.ZoneSelectionPanel;
 import net.rptools.maptool.language.I18N;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Campaign;
 import net.rptools.maptool.model.Player;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.util.PersistenceUtil;
 
 /**
  */
 public class AppActions {
 
 	public static final Action REMOVE_ASSET_ROOT = new ClientAction() {
 		{
 			init("action.removeAssetRoot");
 		}
 
 		public void execute(ActionEvent e) {
 
 			AssetPanel assetPanel = MapTool.getFrame().getAssetPanel();
 			Directory dir = assetPanel.getSelectedAssetRoot();
 
 			if (dir == null) {
 				MapTool.showError("Select an asset group first");
 				return;
 			}
 
 			if (!assetPanel.isAssetRoot(dir)) {
 				MapTool.showError("Must select a root group");
 				return;
 			}
 
 			AppPreferences.removeAssetRoot(dir.getPath());
 			assetPanel.removeAssetRoot(dir);
 		}
 
 	};
 
 	public static final Action ENFORCE_ZONE_VIEW = new ClientAction() {
 		{
 			init("action.enforceView");
 		}
 
 		public void execute(ActionEvent e) {
 
 			if (!MapTool.getPlayer().isGM()) {
 				// TODO: This option should be disabled when not a GM
 				MapTool.showError("Only GMs can do that");
 				return;
 			}
 
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer == null) {
 				return;
 			}
 
 			MapTool.serverCommand().enforceZoneView(renderer.getZone().getId(),
 					renderer.getOffsetX(), renderer.getOffsetY(),
 					renderer.getScaleIndex());
 		}
 
 	};
 
 	public static final Action TYPE_COMMAND = new ClientAction() {
 		{
 			init("action.runMacro");
 		}
 
 		public void execute(ActionEvent e) {
 
 			MapTool.getFrame().getChatPanel().startCommand();
 		}
 
 	};
 
 	public static final Action RANDOMLY_ADD_LAST_ASSET = new ClientAction() {
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
 
 	public static final Action ADJUST_GRID = new ClientAction() {
 		{
 			init("action.adjustGrid");
 		}
 
 		public void execute(ActionEvent e) {
 
 			if (!MapTool.getPlayer().isGM()) {
 				MapTool.showError("Must be GM to adjust grid");
 				return;
 			}
 
 			if (MapTool.getFrame().getCurrentZoneRenderer().getZone().getType() == Zone.Type.INFINITE) {
 				MapTool.showError("Cannot adjust grid on infinite maps.");
 				return;
 			}
 
 			MapTool.getFrame().getToolbox().setSelectedTool(new GridTool());
 		}
 
 	};
 
 	public static final Action TOGGLE_GRID = new ClientAction() {
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
 
 		public void execute(ActionEvent e) {
 
 			AppState.setShowGrid(!AppState.isShowGrid());
			MapTool.getFrame().getCurrentZoneRenderer().repaint();
 		}
 	};
 
 	public static final Action TOGGLE_FOG = new ClientAction() {
 		{
 			init("action.enableFogOfWar");
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
 
 	public static final Action TOGGLE_SHOW_TOKEN_NAMES = new ClientAction() {
 		{
 			init("action.showNames");
 			putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
 			try {
 				putValue(Action.SMALL_ICON,	new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/names.png")));
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 		}
 
 		public void execute(ActionEvent e) {
 
 			AppState.setShowTokenNames(!AppState.isShowTokenNames());
 			MapTool.getFrame().getCurrentZoneRenderer().repaint();
 		}
 	};
 
 	public static final Action TOGGLE_CURRENT_ZONE_VISIBILITY = new ClientAction() {
 
 		{
             init("action.hideMap");
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
 
 	public static final Action TOGGLE_NEW_ZONE_VISIBILITY = new ClientAction() {
 		{
             init("action.autohideNewMaps");
 			try {
 				putValue(Action.SMALL_ICON, new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/zoneVisible.png")));
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 			setEnabled(false);
 		}
 
 		public void execute(ActionEvent e) {
 			AppState.setNewZonesVisible(!AppState.isNewZonesVisible());
 		}
 	};
 
 	public static final Action TOGGLE_DROP_INVISIBLE = new ClientAction() {
 		{
             init("action.autohideNewIcons");
 			try {
 				putValue(Action.SMALL_ICON, new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/icon_invisible.png")));
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 			setEnabled(false);
 		}
 
 		public void execute(ActionEvent e) {
 			AppState
 					.setDropTokenAsInvisible(!AppState.isDropTokenAsInvisible());
 		}
 	};
 
 	public static final Action NEW_CAMPAIGN = new ClientAction() {
 		{
             init("action.newCampaign");
 		}
 
 		public void execute(ActionEvent e) {
 
 			if (MapTool.isConnected()) {
 
 				MapTool
 						.showError("You are connected to a server.  Please disconnect first.");
 				return;
 			}
 
 			MapTool.setCampaign(new Campaign());
 		}
 	};
 
 	public static final Action ZOOM_IN = new ClientAction() {
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
 
 	public static final Action ZOOM_OUT = new ClientAction() {
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
 
 	public static final Action ZOOM_RESET = new ClientAction() {
 		{
             init("action.Zoom100");
 		}
 
 		public void execute(ActionEvent e) {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer != null) {
 				renderer.zoomReset();
 			}
 		}
 	};
 
 	public static final Action TOGGLE_ZONE_SELECTOR = new ClientAction() {
 		{
             init("action.showMapSelector");
 		}
 
 		public void execute(ActionEvent e) {
 
 			ZoneSelectionPanel panel = MapTool.getFrame()
 					.getZoneSelectionPanel();
 
 			panel.setVisible(!panel.isVisible());
 		}
 	};
 
 	public static final Action START_SERVER = new ClientAction() {
 		{
             init("action.serverStart");
 		}
 
 		public void execute(ActionEvent e) {
 
 			runBackground(new Runnable() {
 				public void run() {
 
 					if (MapTool.isConnected()) {
 						MapTool.showError("Already connected.");
 						return;
 					}
 
 					StartServerDialog dialog = new StartServerDialog();
 
 					dialog.setVisible(true);
 
 					if (dialog.getOption() == StartServerDialog.OPTION_CANCEL) {
 						return;
 					}
 
 					try {
 						int port = dialog.getPort();
 
 						MapTool.startServer(port);
 
 						// Connect to server
 						MapTool.createConnection("localhost", port, new Player(
 								dialog.getUsername(), Player.Role.GM));
 
 						// TODO: Create a generic way to update application
 						// state when
 						// connecting
 						TOGGLE_DROP_INVISIBLE.setEnabled(true);
 						LOAD_CAMPAIGN.setEnabled(true);
 						TOGGLE_NEW_ZONE_VISIBILITY.setEnabled(true);
 						ENFORCE_ZONE_VIEW.setEnabled(true);
 						MapTool.getFrame().getConnectionStatusPanel()
 								.setStatus(ConnectionStatusPanel.Status.server);
 					} catch (UnknownHostException uh) {
 						MapTool
 								.showError("Whoah, 'localhost' is not a valid address.  Weird.");
 						return;
 					} catch (IOException ioe) {
 						MapTool
 								.showError("Could not connect to server: "
 										+ ioe);
 						return;
 					}
 
 				}
 			});
 		}
 
 	};
 
 	public static final Action CONNECT_TO_SERVER = new ClientAction() {
 		{
             init("action.clientConnect");
 		}
 
 		public void execute(ActionEvent e) {
 
 			try {
 
 				if (MapTool.isConnected()) {
 					MapTool.showError("Already connected.");
 					return;
 				}
 
 				ConnectToServerDialog dialog = new ConnectToServerDialog();
 
 				dialog.setVisible(true);
 
 				if (dialog.getOption() == ConnectToServerDialog.OPTION_CANCEL) {
 
 					return;
 				}
 
 				MapTool.createConnection(dialog.getServer(), dialog.getPort(),
 						new Player(dialog.getUsername(), dialog.getRole()));
 
 				// TODO: Create a generic way to update application state when
 				// connecting
 				boolean isGM = MapTool.getPlayer().isGM();
 				TOGGLE_DROP_INVISIBLE.setEnabled(isGM);
 				LOAD_CAMPAIGN.setEnabled(isGM);
 				TOGGLE_NEW_ZONE_VISIBILITY.setEnabled(isGM);
 				ENFORCE_ZONE_VIEW.setEnabled(isGM);
 				MapTool.getFrame().getConnectionStatusPanel().setStatus(
 						ConnectionStatusPanel.Status.connected);
 			} catch (UnknownHostException e1) {
 				// TODO Auto-generated catch block
 				MapTool.showError("Unknown host");
 				e1.printStackTrace();
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				MapTool.showError("IO Error: " + e1);
 				e1.printStackTrace();
 			}
 
 		}
 
 	};
 
 	public static final Action DISCONNECT_FROM_SERVER = new ClientAction() {
 
 		{
             init("action.clientDisconnect");
 		}
 
 		public void execute(ActionEvent e) {
 
 			if (!MapTool.isConnected()) {
 				return;
 			}
 
 			if (MapTool.isHostingServer()) {
 				MapTool.showError("Can't disconnect from yourself");
 				return;
 			}
 
 			MapTool.disconnect();
 		}
 
 	};
 
 	public static final Action LOAD_CAMPAIGN = new ClientAction() {
 		{
             init("action.loadCampaign");
 		}
 
 		public void execute(ActionEvent ae) {
 
 			if (MapTool.isConnected() && !MapTool.getPlayer().isGM()) {
 				MapTool.showError("Must be a GM to load a campaign.");
 				return;
 			}
 
 			JFileChooser chooser = MapTool.getLoadFileChooser();
 			chooser.setDialogTitle("Load Campaign");
 
 			if (chooser.showOpenDialog(MapTool.getFrame()) == JFileChooser.APPROVE_OPTION) {
 
 				try {
 					Campaign campaign = PersistenceUtil.loadCampaign(chooser
 							.getSelectedFile());
 
 					if (campaign != null) {
 
 						MapTool.setCampaign(campaign);
 
 						MapTool.serverCommand().setCampaign(campaign);
 					}
 
 				} catch (IOException ioe) {
 					MapTool.showError("Could not load campaign: " + ioe);
 				}
 			}
 		}
 	};
 
 	public static final Action SAVE_CAMPAIGN = new ClientAction() {
 		{
             init("action.saveCampaign");
 		}
 
 		public void execute(ActionEvent ae) {
 
 			Campaign campaign = MapTool.getCampaign();
 
 			// TODO: this should eventually just remember the last place it was
 			// saved
 			JFileChooser chooser = MapTool.getSaveFileChooser();
 			chooser.setDialogTitle("Save Campaign");
 
 			if (chooser.showSaveDialog(MapTool.getFrame()) == JFileChooser.APPROVE_OPTION) {
 
 				try {
 					PersistenceUtil.saveCampaign(campaign, chooser
 							.getSelectedFile());
 				} catch (IOException ioe) {
 					MapTool.showError("Could not save campaign: " + ioe);
 				}
 			}
 		}
 	};
 
 	public static final Action CREATE_UNBOUNDED_MAP = new ClientAction() {
 		{
             init("action.newUnboundedMap");
 		}
 
 		public void execute(java.awt.event.ActionEvent e) {
 
 			runBackground(new Runnable() {
 
 				public void run() {
 
 					Zone zone = new Zone();
 					zone.setType(Zone.Type.INFINITE);
 					zone.setVisible(AppState.isNewZonesVisible());
 
 					MapTool.addZone(zone);
 				}
 			});
 		}
 	};
 
 	public static final Action LOAD_MAP = new ClientAction() {
 		{
             init("action.newMap");
 		}
 
 		public void execute(java.awt.event.ActionEvent e) {
 
 			runBackground(new Runnable() {
 
 				public void run() {
 					JFileChooser loadFileChooser = MapTool.getLoadFileChooser();
 
 					loadFileChooser.setDialogTitle("Load Map");
 					loadFileChooser
 							.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
 					if (loadFileChooser.showOpenDialog(MapTool.getFrame()) == JFileChooser.CANCEL_OPTION) {
 						return;
 					}
 					if (loadFileChooser.getSelectedFile() == null) {
 						return;
 					}
 
 					try {
 						byte[] imgData = FileUtil.loadFile(loadFileChooser
 								.getSelectedFile());
 						Asset asset = new Asset(imgData);
 						AssetManager.putAsset(asset);
 
 						MapTool.serverCommand().putAsset(asset);
 
 						Zone zone = new Zone(asset.getId());
 						zone.setVisible(AppState.isNewZonesVisible());
 						MapTool.addZone(zone);
 					} catch (IOException ioe) {
 						MapTool.showError("Could not load image: " + ioe);
 						return;
 					}
 				}
 
 			});
 		}
 	};
 
 	public static final Action TOGGLE_ASSET_PANEL = new ClientAction() {
 		{
             init("action.showInformationPanel");
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
 
 	public static final Action ADD_ASSET_PANEL = new ClientAction() {
 		{
             init("action.addIconSelector");
 		}
 
 		public void execute(ActionEvent e) {
 
 			runBackground(new Runnable() {
 
 				public void run() {
 
 					JFileChooser chooser = MapTool.getLoadFileChooser();
 					chooser.setDialogTitle("Load Asset Tree");
 					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 
 					if (chooser.showOpenDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
 						return;
 					}
 
 					File root = chooser.getSelectedFile();
 					MapTool.getFrame().addAssetRoot(root);
 					AppPreferences.addAssetRoot(root);
 				}
 
 			});
 		}
 	};
 
 	public static final Action REFRESH_ASSET_PANEL = new ClientAction() {
 		{
             init("action.refresh");
 		}
 
 		public void execute(ActionEvent e) {
 			MapTool.getFrame().getAssetPanel().getAssetTree().refresh();
 		}
 
 	};
 
 	public static final Action EXIT = new ClientAction() {
 		{
             init("action.exit");
 		}
 
 		public void execute(ActionEvent ae) {
 
 			System.exit(0);
 		}
 	};
 
 	private static abstract class ClientAction extends AbstractAction {
 
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
 
 		}
 
 		public final void actionPerformed(ActionEvent e) {
 			execute(e);
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
 				}
 			}.start();
 		}
 	}
 }
