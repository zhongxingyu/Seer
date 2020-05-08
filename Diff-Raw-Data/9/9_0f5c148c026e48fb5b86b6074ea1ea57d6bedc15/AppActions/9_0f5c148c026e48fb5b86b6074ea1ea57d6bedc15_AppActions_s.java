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
 package net.rptools.maptool.client;
 
 import java.awt.Dimension;
 import java.awt.Event;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.Transparency;
 import java.awt.event.ActionEvent;
 import java.awt.image.BufferedImage;
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.UnknownHostException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.zip.GZIPOutputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.imageio.ImageIO;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.KeyStroke;
 
 import net.rptools.lib.FileUtil;
 import net.rptools.lib.MD5Key;
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.maptool.client.tool.GridTool;
 import net.rptools.maptool.client.tool.drawing.DrawableUndoManager;
 import net.rptools.maptool.client.ui.AppMenuBar;
 import net.rptools.maptool.client.ui.ClientConnectionPanel;
 import net.rptools.maptool.client.ui.ConnectToServerDialog;
 import net.rptools.maptool.client.ui.ConnectToServerDialogPreferences;
 import net.rptools.maptool.client.ui.ConnectionStatusPanel;
 import net.rptools.maptool.client.ui.ExportDialog;
 import net.rptools.maptool.client.ui.MapPropertiesDialog;
 import net.rptools.maptool.client.ui.PreferencesDialog;
 import net.rptools.maptool.client.ui.PreviewPanelFileChooser;
 import net.rptools.maptool.client.ui.ServerInfoDialog;
 import net.rptools.maptool.client.ui.StartServerDialog;
 import net.rptools.maptool.client.ui.StartServerDialogPreferences;
 import net.rptools.maptool.client.ui.StaticMessageDialog;
 import net.rptools.maptool.client.ui.MapToolFrame.MTFrame;
 import net.rptools.maptool.client.ui.assetpanel.AssetPanel;
 import net.rptools.maptool.client.ui.assetpanel.Directory;
 import net.rptools.maptool.client.ui.campaignproperties.CampaignPropertiesDialog;
 import net.rptools.maptool.client.ui.io.FTPClient;
 import net.rptools.maptool.client.ui.io.FTPTransferObject;
 import net.rptools.maptool.client.ui.io.LoadSaveImpl;
 import net.rptools.maptool.client.ui.io.ProgressBarList;
 import net.rptools.maptool.client.ui.io.UpdateRepoDialog;
 import net.rptools.maptool.client.ui.io.FTPTransferObject.Direction;
 import net.rptools.maptool.client.ui.token.TransferProgressDialog;
 import net.rptools.maptool.client.ui.zone.PlayerView;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.language.I18N;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Campaign;
 import net.rptools.maptool.model.CampaignFactory;
 import net.rptools.maptool.model.CampaignProperties;
 import net.rptools.maptool.model.CellPoint;
 import net.rptools.maptool.model.ExportInfo;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.LookupTable;
 import net.rptools.maptool.model.Player;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.ZoneFactory;
 import net.rptools.maptool.model.ZonePoint;
 import net.rptools.maptool.model.Zone.VisionType;
 import net.rptools.maptool.model.drawing.DrawableTexturePaint;
 import net.rptools.maptool.server.ServerConfig;
 import net.rptools.maptool.server.ServerPolicy;
 import net.rptools.maptool.util.ImageManager;
 import net.rptools.maptool.util.PersistenceUtil;
 import net.rptools.maptool.util.UPnPUtil;
 import net.rptools.maptool.util.PersistenceUtil.PersistedCampaign;
 import net.rptools.maptool.util.PersistenceUtil.PersistedMap;
 
 import org.apache.log4j.Logger;
 import org.jdesktop.swingworker.SwingWorker;
 
 import com.jidesoft.docking.DockableFrame;
 
 /**
  * This class acts as a container for a wide variety of {@link Action}s that are
  * used throughout the application. Most of these are added to the main frame
  * menu, but some are added dynamically as needed, sometimes to the frame menu
  * but also to the context menu (the "right-click menu").
  * 
  * Each object instantiated from {@link DefaultClientAction} should have an
  * initializer that calls {@link ClientAction#init(String)} and passes the base
  * message key from the properties file. This base message key will be used to
  * locate the text that should appear on the menu item as well as the
  * accelerator, mnemonic, and short description strings. (See the {@link I18N}
  * class for more details on how the key is used.
  * 
  * In addition, each object should override {@link ClientAction#isAvailable()}
  * and return true if the application is in a state where the Action should be
  * enabled. (The default is <code>true</code>.)
  * 
  * Last is the {@link ClientAction#execute(ActionEvent)} method. It is passed
  * the {@link ActionEvent} object that triggered this Action as a parameter. It
  * should perform the necessary work to accomplish the effect of the Action.
  */
 public class AppActions {
 
 	private static final Logger log = Logger.getLogger(AppActions.class);
 	
 	private static Set<Token> tokenCopySet = null;
 	public static final int menuShortcut = getMenuShortcutKeyMask();
 
 	private static int getMenuShortcutKeyMask() {
 		int key = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
 		String prop = System.getProperty("os.name", "unknown");
 		if ("Darwin".equals(prop)) {
 			// TODO Should we install our own AWTKeyStroke class?  Only if menu shortcut is CTRL...
 			if (key == Event.CTRL_MASK)
 				key = Event.META_MASK;
 			/*
 			 * In order for SoyLatte/OpenJDK to work on Mac OS X, the user must have the X11
 			 * package installed.  If they're running headless, they don't need it.  Otherwise,
 			 * they must already have it or we wouldn't have gotten this far. :)  However, in
 			 * order for the Command key to work, the X11 Preferences must be set to
 			 * "Enabled the Meta Key" in X11 applications.  Essentially, if this checkbox is
 			 * turned on, the Command key (called Meta in X11) will be intercepted by the
 			 * X1 package and not sent on to the application.  Our next step will be better
 			 * integration with the Mac desktop to eliminate the X11 menu altogether.  It
 			 * might be nice to give them a one-time warning about this...
 			 */
 		}
 		return key;
 	}
 
 	public static final Action MRU_LIST = new DefaultClientAction() {
 		{
 			init("menu.recent");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return MapTool.isHostingServer() || MapTool.isPersonalServer();
 		}
 
 		public void execute(ActionEvent ae) {
 			// Do nothing
 		}
 	};
 
 	public static final Action EXPORT_SCREENSHOT = new DefaultClientAction() {
 		{
 			init("action.exportScreenShotAs");
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
 			init("action.exportScreenShot");
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
 
 		Player.Role role = exportInfo.getView() == ExportInfo.View.GM ? Player.Role.GM : Player.Role.PLAYER;
 
 		switch (exportInfo.getType()) {
 		case ExportInfo.Type.CURRENT_VIEW:
 			screenCap = MapTool.takeMapScreenShot(new PlayerView(role));
 			if (screenCap == null) {
 				MapTool.getFrame().setStatusMessage("msg.error.failedScreenCapture");
 				return;
 			}
 			break;
 		case ExportInfo.Type.FULL_MAP:
 			break;
 		}
 
 		MapTool.getFrame().setStatusMessage("msg.info.screenshotSaving");
 
 		try {
 
 			ByteArrayOutputStream imageOut = new ByteArrayOutputStream();
 
 			ImageIO.write(screenCap, "png", imageOut);
 			screenCap = null;		// Free up the memory as soon as possible
 
 			exportInfo.getLocation().putContent(new BufferedInputStream(new ByteArrayInputStream(imageOut.toByteArray())));
 
 			MapTool.getFrame().setStatusMessage(I18N.getText("msg.info.screenshotSaved"));
 
 		} catch (IOException ioe) {
 			MapTool.showError("msg.error.failedExportingImage", ioe);
 		} catch (Exception e) {
 			MapTool.showError("msg.error.failedExportingImage", e);
 		}
 	}
 
 	public static final Action EXPORT_CAMPAIGN_REPO = new AdminClientAction() {
 
 		{
 			init("admin.exportCampaignRepo");
 		}
 
 		@Override
 		public void execute(ActionEvent e) {
 
 			JFileChooser chooser = MapTool.getFrame().getSaveFileChooser();
 
 			// Get target location
 			if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
 				return;
 			}
 
 			// Default extension
 			File selectedFile = chooser.getSelectedFile();
 			if (!selectedFile.getName().toUpperCase().endsWith(".ZIP")) {
 				selectedFile = new File(selectedFile.getAbsolutePath() + ".zip");
 			}
 
 			if (selectedFile.exists()) {
 				if (!MapTool.confirm("msg.confirm.fileExists")) {
 					return;
 				}
 			}
 
 			// Create index
 			Campaign campaign = MapTool.getCampaign();
 			Set<Asset> assetSet = new HashSet<Asset>();
 			for (Zone zone : campaign.getZones()) {
 
 				for (MD5Key key : zone.getAllAssetIds()) {
 					assetSet.add(AssetManager.getAsset(key));
 				}
 			}
 
 			// Export to temp location
 			File tmpFile = new File(AppUtil.getAppHome("tmp").getAbsolutePath() + "/" + System.currentTimeMillis() + ".export");
 
 			try {
 				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tmpFile));
 
 				StringBuilder builder = new StringBuilder();
 				for (Asset asset : assetSet) {
 
 					// Index it
 					builder.append(asset.getId()).append(" assets/").append(asset.getId()).append("\n");
 					// Save it
 					ZipEntry entry = new ZipEntry("assets/" + asset.getId().toString());
 					out.putNextEntry(entry);
 					out.write(asset.getImage());
 				}
 
 				// Handle the index
 				ByteArrayOutputStream bout = new ByteArrayOutputStream();
 				GZIPOutputStream gzout = new GZIPOutputStream(bout);
 				gzout.write(builder.toString().getBytes());
 				gzout.close();
 
 				ZipEntry entry = new ZipEntry("index.gz");
 				out.putNextEntry(entry);
 				out.write(bout.toByteArray());
 				out.closeEntry();
 
 				out.close();
 
 				// Move to new location
 				File mvFile = new File(AppUtil.getAppHome("tmp").getAbsolutePath() + "/" + selectedFile.getName());
 				if (selectedFile.exists()) {
 					FileUtil.copyFile(selectedFile, mvFile);
 					selectedFile.delete();
 				}
 
 				FileUtil.copyFile(tmpFile, selectedFile);
 				tmpFile.delete();
 
 				if (mvFile.exists()) {
 					mvFile.delete();
 				}
 
 			} catch (IOException ioe) {
 				MapTool.showError("msg.error.failedExportingCampaignRepo" ,ioe);
 				return;
 			}
 
 			MapTool.showInformation("msg.confirm.campaignExported");
 		}
 	};
 
 	public static final Action UPDATE_CAMPAIGN_REPO = new DeveloperClientAction() {
 		{
 			init("admin.updateCampaignRepo");
 		}
 
 		/**
 		 * <p>
 		 * This action performs a repository update by comparing the assets in the current
 		 * campaign against all assets in all repositories and uploading assets to one of
 		 * the repositories and creating a replacement <b>index.gz</b> which is also
 		 * uploaded.
 		 * </p>
 		 * <p>
 		 * For the purposes of this action, only the FTP protocol is supported.  The primary
 		 * reason for this has to do with the way images will be uploaded.  If HTTP were
 		 * used and a single file sent, there would need to be a script on the remote end
 		 * that knew how to unpack the file correctly.  This cannot be assumed in the
 		 * general case.
 		 * </p>
 		 * <p>
 		 * Using FTP, we can upload individual files to particular directories.  While this
 		 * same approach could be used for HTTP, once again the user would have to
 		 * install some kind of upload script on the server side.  This again makes HTTP
 		 * impractical and FTP more "user-friendly".
 		 * </p>
 		 * <p>
 		 * <b>Implementation.</b>  This method first makes a list of all known
 		 * repositories from the campaign properties.  They are presented to the user who
 		 * then selects one as the destination for new assets to be uploaded.  A list of
 		 * assets currently used in the campaign is then generated and compared against
 		 * the index files of all repositories from the previous list.  Any new assets are
 		 * aggregated and the user is presented with a summary of the images to be
 		 * uploaded, including file size.  The user enters FTP connection information and
 		 * the upload begins as a separate thread.  (Perhaps the Transfer Window can be
 		 * used to keep track of the uploading process?)
 		 * </p>
 		 * <p>
 		 * <b>Optimizations.</b>  At some point, creating the list of assets could be
 		 * spun into another thread, although there's probably not much value there.  Or
 		 * the FTP information could be collected at the beginning and as assets are checked
 		 * they could immediately begin uploading with the summary including all assets,
 		 * even those already uploaded.
 		 * </p>
 		 * <p>
 		 * My review of FTP client libraries brought me to
 		 * <a href="http://www.javaworld.com/javaworld/jw-04-2003/jw-0404-ftp.html">
 		 * this extensive review of FTP libraries</a>
 		 * If we're going to do much more with FTP, <b>Globus GridFTP</b> looks good,
 		 * but the library itself is 2.7MB.
 		 * </p>
 		 */
 		@Override
 		public void execute(ActionEvent e) {
 			/*
 			 * 1.  Ask the user to select repositories which should be considered.
 			 * 2.  Ask the user for FTP upload information.
 			 */
 			UpdateRepoDialog urd;
 
 			Campaign campaign = MapTool.getCampaign();
 			CampaignProperties props = campaign.getCampaignProperties();
 			ExportInfo exportInfo = campaign.getExportInfo();
 			if (exportInfo == null)
 				exportInfo = new ExportInfo();
 			urd = new UpdateRepoDialog(MapTool.getFrame(), props.getRemoteRepositoryList(),
 					exportInfo.getLocation());
 			urd.pack();
 			urd.setVisible(true);
 			if (urd.getStatus() == JOptionPane.CANCEL_OPTION) {
 				return;
 			}
 			exportInfo.setLocation(urd.getFTPLocation());
 			campaign.setExportInfo(exportInfo);
 
 			/*
 			 * 3.  Check all assets against the repository indices and build a new list from
 			 * those that are not found.
 			 */
 			Map<MD5Key, Asset> missing = AssetManager.findAllAssetsNotInRepositories(urd.getSelectedRepositories());
 
 			/*
 			 * 4.  Give the user a summary and ask for permission to begin the upload.
 			 * I'm going to display a listbox and let the user click on elements of the list
 			 * in order to see a preview to the right.  But there's no plan to make it a
 			 * CheckBoxList.  (Wouldn't be _that_ tough, however.)
 			 */
 			if (! MapTool.confirm(I18N.getText("msg.confirm.aboutToBeginFTP", missing.size()+1)))
 				return;
 
 			/*
 			 * 5.  Build the index as we go, but add the images to FTP to a queue handled by
 			 * another thread.  Add a progress bar of some type or use the Transfer Status
 			 * window.
 			 */
 			try {
 				File topdir = urd.getDirectory();
 				File dir = new File(urd.isCreateSubdir() ? getFormattedDate(null) : null);
 
 				Map<String, String> repoEntries = new HashMap<String, String>(missing.size());
 				FTPClient ftp = new FTPClient(urd.getHostname(), urd.getUsername(), urd.getPassword());
 
 				// Enabling this means the upload begins immediately upon the first queued entry
 				ftp.setEnabled(true);
 				ProgressBarList pbl = new ProgressBarList(MapTool.getFrame(), ftp, missing.size()+1);
 
 				for (Map.Entry<MD5Key, Asset> entry : missing.entrySet()) {
 					String remote = entry.getKey().toString();
 					repoEntries.put(remote, dir == null ? remote : new File(dir, remote).getPath());
 					ftp.addToQueue(new FTPTransferObject(Direction.FTP_PUT, entry.getValue().getImage(), dir, remote));
 				}
 				// We're done with "missing", so empty it now.
 				missing.clear();
 
 				// Handle the index
 				ByteArrayOutputStream bout = new ByteArrayOutputStream();
 				String saveTo = urd.getSaveToRepository();
 				// When this runs our local 'repoindx' is updated.  If the FTP upload later fails,
 				// it doesn't really matter much because the assets are already there.  However,
 				// if our local cache is ever downloaded again, we'll "forget" that the assets are
 				// on the server.  It sounds like it might be nice to have some way to resync
 				// the local system with the FTP server.  But it's probably better to let the user
 				// do it manually.
 				byte[] index = AssetManager.updateRepositoryMap(saveTo, repoEntries);
 				repoEntries.clear();
 				GZIPOutputStream gzout = new GZIPOutputStream(bout);
 				gzout.write(index);
 				gzout.close();
 				ftp.addToQueue(new FTPTransferObject(Direction.FTP_PUT, bout.toByteArray(), topdir, "index.gz"));
 			} catch (IOException ioe) {
 				MapTool.showError("msg.error.failedUpdatingCampaignRepo", ioe);
 				return;
 			}
 		}
 
 		private String getFormattedDate(Date d) {
 			// Use today's date as the directory on the FTP server.  This doesn't affect players'
 			// ability to download it and might help the user determine what was uploaded to
 			// their site and why.  It can't hurt. :)
 			SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance();
 			df.applyPattern("yyyy-MM-dd");
 			return df.format(d == null ? new Date() : d);
 		}
 	};
 
 	/**
 	 * This is the menu option that forces clients to display the GM's current map.
 	 */
 	public static final Action ENFORCE_ZONE = new ZoneAdminClientAction() {
 
 		{
 			init("action.enforceZone");
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
 			init("action.restoreDefaultImages");
 		}
 
 		public void execute(ActionEvent e) {
 			try {
 				AppSetup.installDefaultTokens();
 
 				// TODO: Remove this hardwiring
 				File unzipDir = new File(AppConstants.UNZIP_DIR.getAbsolutePath() + File.separator + "Default");
 				MapTool.getFrame().addAssetRoot(unzipDir);
 				AssetManager.searchForImageReferences(unzipDir, AppConstants.IMAGE_FILE_FILTER);
 
 			} catch (IOException ioe) {
 				MapTool.showError("msg.error.failedAddingDefaultImages", ioe);
 			}
 		}
 	};
 
 	public static final Action ADD_DEFAULT_TABLES = new DefaultClientAction() {
 
 		{
 			init("action.addDefaultTables");
 		}
 
 		public void execute(ActionEvent e) {
 			try {
 				// Load the defaults
 				InputStream in = AppActions.class.getClassLoader().getResourceAsStream("net/rptools/maptool/client/defaultTables.mtprops");
 				CampaignProperties properties = PersistenceUtil.loadCampaignProperties(in);
 				in.close();
 
 				// Make sure the images have been installed
 				// Just pick a table and spot check
 				LookupTable lookupTable = properties.getLookupTableMap().values().iterator().next();
 				if (!AssetManager.hasAsset(lookupTable.getTableImage())) {
 					AppSetup.installDefaultTokens();
 				}
 
 				MapTool.getCampaign().mergeCampaignProperties(properties);
 
 				MapTool.getFrame().repaint();
 
 			} catch (IOException ioe) {
 				MapTool.showError("msg.error.failedAddingDefaultTables" ,ioe);
 			}
 		}
 	};
 
 	public static final Action RENAME_ZONE = new AdminClientAction() {
 
 		{
 			init("action.renameMap");
 		}
 
 		@Override
 		public void execute(ActionEvent e) {
 
 			Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
 			String msg = I18N.getText("msg.confirm.renameMap", zone.getName() != null ? zone.getName() : "");
 			String name = JOptionPane.showInputDialog(MapTool.getFrame(), msg);
 			if (name != null) {
 				zone.setName(name);
 				MapTool.serverCommand().renameZone(zone.getId(), name);
 			}
 		}
 	};
 
 	public static final Action SHOW_FULLSCREEN = new DefaultClientAction() {
 
 		{
 			init("action.fullscreen");
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
 			return super.isAvailable() && (MapTool.isPersonalServer() || MapTool.isHostingServer());
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
 			init("action.preferences");
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
 			chooser.setDialogTitle(I18N.getText("msg.title.saveMessageHistory"));
 			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
 			if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
 				return;
 			}
 
 			File saveFile = chooser.getSelectedFile();
 			if (saveFile.getName().indexOf(".") < 0) {
 				saveFile = new File(saveFile.getAbsolutePath() + ".html");
 			}
 			if (saveFile.exists() && !MapTool.confirm("msg.confirm.fileExists")) {
 				return;
 			}
 
 			try {
 				FileUtil.writeBytes(saveFile, messageHistory.getBytes());
 			} catch (IOException ioe) {
 				MapTool.showError("msg.error.failedSavingMessageHistory", ioe);
 			}
 		}
 	};
 
 	public static final Action AUTOSAVE_MESSAGE_HISTORY = new DefaultClientAction() {
 		{
 			init("action.autosaveMessageHistory");
 		}
 
 		public void execute(ActionEvent e) {
 			JFileChooser chooser = MapTool.getFrame().getSaveFileChooser();
 			chooser.setDialogTitle(I18N.getText("msg.title.saveMessageHistory"));
 			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
 			if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
 				return;
 			}
 
 			File saveFile = chooser.getSelectedFile();
 			if (saveFile.getName().indexOf(".") < 0) {
 				saveFile = new File(saveFile.getAbsolutePath() + ".html");
 			}
 			if (saveFile.exists() && !MapTool.confirm("msg.confirm.fileExists")) {
 				return;
 			}
 
 			try {
 				String messageHistory = MapTool.getFrame().getCommandPanel().getMessageHistory();
 				FileUtil.writeBytes(saveFile, messageHistory.getBytes());
 			} catch (IOException ioe) {
 				MapTool.showError("msg.error.failedSavingMessageHistory", ioe);
 			}
 		}
 	};
 
 	public static final DefaultClientAction UNDO_DRAWING = new DefaultClientAction() {
 		{
 			init("action.undoDrawing");
 			isAvailable(); // XXX FJE Is this even necessary?
 		}
 
 		@Override
 		public void execute(ActionEvent e) {
 			DrawableUndoManager.getInstance().undo();
 			isAvailable();
 			REDO_DRAWING.isAvailable(); // XXX FJE Calling these forces the update, but won't the framework call them?
 		}
 
 		@Override
 		public boolean isAvailable() {
 			setEnabled(DrawableUndoManager.getInstance().getUndoManager().canUndo());
 			return isEnabled();
 		}
 	};
 
 	public static final DefaultClientAction REDO_DRAWING = new DefaultClientAction() {
 		{
 			init("action.redoDrawing");
 			isAvailable(); // XXX Is this even necessary?
 		}
 
 		@Override
 		public void execute(ActionEvent e) {
 			DrawableUndoManager.getInstance().redo();
 			isAvailable();
 			UNDO_DRAWING.isAvailable();
 		}
 
 		@Override
 		public boolean isAvailable() {
 			setEnabled(DrawableUndoManager.getInstance().getUndoManager().canRedo());
 			return isEnabled();
 		}
 	};
 
 	public static final DefaultClientAction CLEAR_DRAWING = new DefaultClientAction() {
 		{
 			init("action.clearDrawing");
 		}
 
 		@Override
 		public void execute(ActionEvent e) {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer == null) {
 				return;
 			}
 			if (!MapTool.confirm("msg.confirm.clearAllDrawings")) {
 				return;
 			}
 			// LATER: Integrate this with the undo stuff
 			MapTool.serverCommand().clearAllDrawings(renderer.getZone().getId());
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return true;
 		}
 	};
 
 	public static final DefaultClientAction CUT_TOKENS = new DefaultClientAction() {
 		{
 			init("action.cutTokens");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return super.isAvailable() && MapTool.getFrame().getCurrentZoneRenderer() != null;
 		}
 
 		public void execute(ActionEvent e) {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			Set<GUID> selectedSet = renderer.getSelectedTokenSet();
 
 			cutTokens(renderer.getZone(), selectedSet);
 		}
 	};
 
 	public static final DefaultClientAction COPY_TOKENS = new DefaultClientAction() {
 		{
 			init("action.copyTokens");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return super.isAvailable() && MapTool.getFrame().getCurrentZoneRenderer() != null;
 		}
 
 		public void execute(ActionEvent e) {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			copyTokens(renderer.getSelectedTokenSet());
 		}
 
 	};
 
 	public static final void cutTokens(Zone zone, Set<GUID> tokenSet) {
 		
 		copyTokens(tokenSet);
 
 		// delete tokens
 		for (GUID tokenGUID : tokenSet) {
 
 			Token token = zone.getToken(tokenGUID);
 
 			if (AppUtil.playerOwns(token)) {
 				zone.removeToken(tokenGUID);
 				MapTool.serverCommand().removeToken(zone.getId(), tokenGUID);
 			}
 		}
 
 		MapTool.getFrame().getCurrentZoneRenderer().clearSelectedTokens();		
 	}
 	
 	public static final void copyTokens(Set<GUID> tokenSet) {
 
 		List<Token> tokenList = new ArrayList<Token>();
 
 		ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 		Zone zone = renderer.getZone();
 
 		Integer top = null;
 		Integer left = null;
 		tokenCopySet = new HashSet<Token>();
 		for (GUID guid : tokenSet) {
 			Token token = zone.getToken(guid);
 			if (token != null) {
 				tokenList.add(token);
 			}
 		}
 
 		copyTokens(tokenList);
 	}
 
 	public static final void copyTokens(List<Token> tokenList) {
 
 		ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 
 		Integer top = null;
 		Integer left = null;
 		tokenCopySet = new HashSet<Token>();
 		for (Token token : tokenList) {
 
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
 
 	public static final DefaultClientAction PASTE_TOKENS = new DefaultClientAction() {
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
 				zonePoint = renderer.getZone().getGrid().convert(cellPoint);
 			}
 
 			List<Token> tokenList = new ArrayList<Token>(tokenCopySet);
 			Collections.sort(tokenList, Token.COMPARE_BY_ZORDER);
 			for (Token origToken : tokenList) {
 
 				Token token = new Token(origToken);
 
 				token.setX(token.getX() + zonePoint.x);
 				token.setY(token.getY() + zonePoint.y);
 
 				// paste into correct layer
 				token.setLayer(renderer.getActiveLayer());
 
 				// check the token's name, don't change PC token names ... ever
 				if (origToken.getType() != Token.Type.PC) {
 					token.setName(MapToolUtil.nextTokenId(zone, token));
 				}
 
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
 
 	public static final Action BOOT_CONNECTED_PLAYER = new DefaultClientAction() {
 		{
 			init("action.bootConnectedPlayer");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return MapTool.isHostingServer() || MapTool.getPlayer().isGM();
 		}
 
 		public void execute(ActionEvent e) {
 			ClientConnectionPanel panel = MapTool.getFrame().getConnectionPanel();
 			Player selectedPlayer = (Player) panel.getSelectedValue();
 
 			if (selectedPlayer == null) {
 				MapTool.showError("msg.error.mustSelectPlayerFirst");
 				return;
 			}
 
 			if (MapTool.getPlayer().equals(selectedPlayer)) {
 				MapTool.showError("msg.error.cantBootSelf");
 				return;
 			}
 
 			if (MapTool.isPlayerConnected(selectedPlayer.getName())) {
 				String msg = I18N.getText("msg.confirm.bootPlayer", selectedPlayer.getName());
 				if (MapTool.confirm(msg)) {
 					MapTool.serverCommand().bootPlayer(selectedPlayer.getName());
 					msg = I18N.getText("msg.info.playerBooted", selectedPlayer.getName());
 					MapTool.showInformation(msg);
 					return;
 				}
 			}
 
 			MapTool.showError("msg.error.failedToBoot");
 
 		}
 	};
 
 	/**
 	 * This is the menu option that forces the player view to continuously track the GM view.
 	 */
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
 			MapTool.getFrame().getCurrentZoneRenderer().maybeForcePlayersView();
 		}
 
 	};
 
 	public static final Action TOGGLE_SHOW_PLAYER_VIEW = new AdminClientAction() {
 		{
 			init("action.showPlayerView");
 		}
 
 		@Override
 		public boolean isSelected() {
 			return AppState.isShowAsPlayer();
 		}
 
 		public void execute(ActionEvent e) {
 
 			AppState.setShowAsPlayer(!AppState.isShowAsPlayer());
 			MapTool.getFrame().refresh();
 		}
 
 	};
 
 	public static final Action TOGGLE_SHOW_LIGHT_SOURCES = new AdminClientAction() {
 		{
 			init("action.showLightSources");
 		}
 
 		@Override
 		public boolean isSelected() {
 			return AppState.isShowLightSources();
 		}
 
 		public void execute(ActionEvent e) {
 
 			AppState.setShowLightSources(!AppState.isShowLightSources());
 			MapTool.getFrame().refresh();
 		}
 
 	};
 
 	public static final Action TOGGLE_COLLECT_PROFILING_DATA = new DefaultClientAction() {
 		{
 			init("Collect Performance Data");
 		}
 
 		@Override
 		public boolean isSelected() {
 			return AppState.isCollectProfilingData();
 		}
 
 		public void execute(ActionEvent e) {
 
 			AppState.setCollectProfilingData(!AppState.isCollectProfilingData());
 			
 			MapTool.getProfilingNoteFrame().setVisible(AppState.isCollectProfilingData());
 		}
 
 	};
 
 	public static final Action TOGGLE_SHOW_MOVEMENT_MEASUREMENTS = new DefaultClientAction() {
 		{
 			init("action.showMovementMeasures");
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
 
 	public static final Action COPY_ZONE = new ZoneAdminClientAction() {
 		{
 			init("action.copyZone");
 		}
 
 		public void execute(ActionEvent e) {
 
 			Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
 			String zoneName = JOptionPane.showInputDialog("New map name:", "Copy of " + zone.getName());
 			if (zoneName != null) {
 				Zone zoneCopy = new Zone(zone);
 				zoneCopy.setName(zoneName);
 				MapTool.addZone(zoneCopy);
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
 			MapTool.removeZone(renderer.getZone());
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
 
 	/**
 	 * This is the menu option that warps all clients views to the current GM's view.
 	 */
 	public static final Action ENFORCE_ZONE_VIEW = new ZoneAdminClientAction() {
 		{
 			init("action.enforceView");
 		}
 
 		public void execute(ActionEvent e) {
 
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer == null) {
 				return;
 			}
 
 			renderer.forcePlayersView();
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
 			if (!MapTool.getFrame().isCommandPanelVisible()) {
 				MapTool.getFrame().showCommandPanel();
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
 			init(ENTER_COMMAND_ID, false);
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
 
 	public static final Action ADJUST_GRID = new ZoneAdminClientAction() {
 		{
 			init("action.adjustGrid");
 		}
 
 		public void execute(ActionEvent e) {
 
 			MapTool.getFrame().getToolbox().setSelectedTool(GridTool.class);
 		}
 
 	};
 
 	private static TransferProgressDialog transferProgressDialog;
 	public static final Action SHOW_TRANSFER_WINDOW = new DefaultClientAction() {
 		{
 			init("msg.info.showTransferWindow");
 		}
 
 		public void execute(ActionEvent e) {
 
 			if (transferProgressDialog == null) {
 				transferProgressDialog = new TransferProgressDialog();
 			}
 
 			if (transferProgressDialog.isShowing()) {
 				return;
 			}
 
 			transferProgressDialog.showDialog();
 		}
 
 	};
 
 	public static final Action TOGGLE_GRID = new DefaultClientAction() {
 		{
 			init("action.showGrid");
 			putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
 			try {
 				putValue(Action.SMALL_ICON, new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/grid.gif")));
 			} catch (IOException ioe) {
 				MapTool.showError("While retrieving built-in 'grid.gif' image", ioe);
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
 
 	public static final Action TOGGLE_COORDINATES = new DefaultClientAction() {
 		{
 			init("action.showCoordinates");
 			putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
 		}
 
 		@Override
 		public boolean isAvailable() {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			return renderer != null && renderer.getZone().getGrid().getCapabilities().isCoordinatesSupported();
 		}
 
 		public boolean isSelected() {
 			return AppState.isShowCoordinates();
 		}
 
 		public void execute(ActionEvent e) {
 
 			AppState.setShowCoordinates(!AppState.isShowCoordinates());
 
 			MapTool.getFrame().getCurrentZoneRenderer().repaint();
 		}
 	};
 
 	public static final Action TOGGLE_ZOOM_LOCK = new DefaultClientAction() {
 		{
 			init("action.zoomLock");
 			putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
 		}
 
 		@Override
 		public boolean isAvailable() {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			return renderer != null;
 		}
 
 		public boolean isSelected() {
 			return AppState.isZoomLocked();
 		}
 
 		public void execute(ActionEvent e) {
 
 			AppState.setZoomLocked(!AppState.isZoomLocked());
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
 
 	public static class SetVisionType extends ZoneAdminClientAction {
 		private VisionType visionType;
 
 		public SetVisionType(VisionType visionType) {
 			this.visionType = visionType;
 			init("visionType." + visionType.name());
 		}
 
 		@Override
 		public boolean isSelected() {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer == null) {
 				return false;
 			}
 
 			return renderer.getZone().getVisionType() == visionType;
 		}
 
 		public void execute(ActionEvent e) {
 
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer == null) {
 				return;
 			}
 
 			Zone zone = renderer.getZone();
 
 			if (zone.getVisionType() != visionType) {
 
 				zone.setVisionType(visionType);
 
 				MapTool.serverCommand().setVisionType(zone.getId(), visionType);
 
 				renderer.flushFog();
 				renderer.flushLight();
 				renderer.repaint();
 			}
 		}
 	};
 
 	public static final Action TOGGLE_SHOW_TOKEN_NAMES = new DefaultClientAction() {
 		{
 			init("action.showNames");
 			putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
 			try {
 				putValue(Action.SMALL_ICON, new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/names.png")));
 			} catch (IOException ioe) {
 				MapTool.showError("While retrieving built-in 'names.png' image", ioe);
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
 
 			MapTool.serverCommand().setZoneVisibility(zone.getId(), zone.isVisible());
 			MapTool.getFrame().getZoneMiniMapPanel().flush();
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
 
 			Campaign campaign = CampaignFactory.createBasicCampaign();
 			AppState.setCampaignFile(null);
 			MapTool.setCampaign(campaign);
 			MapTool.serverCommand().setCampaign(campaign);
 
 			ImageManager.flush();
 			MapTool.getFrame().setCurrentZoneRenderer(MapTool.getFrame().getZoneRenderer(campaign.getZones().get(0)));
 		}
 	};
 
 	public static final Action ZOOM_IN = new DefaultClientAction() {
 		{
 			init("action.zoomIn", false);
 		}
 
 		public void execute(ActionEvent e) {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer != null) {
 				Dimension size = renderer.getSize();
 				renderer.zoomIn(size.width / 2, size.height / 2);
 				renderer.maybeForcePlayersView();
 			}
 		}
 	};
 
 	public static final Action ZOOM_OUT = new DefaultClientAction() {
 		{
 			init("action.zoomOut", false);
 		}
 
 		public void execute(ActionEvent e) {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer != null) {
 				Dimension size = renderer.getSize();
 				renderer.zoomOut(size.width / 2, size.height / 2);
 				renderer.maybeForcePlayersView();
 			}
 		}
 	};
 
 	public static final Action ZOOM_RESET = new DefaultClientAction() {
 
 		private Double lastZoom;
 
 		{
 			// FIXME Produces menu text, "unknown code: 0x2b"
 			init("action.zoom100", false);
 		}
 
 		public void execute(ActionEvent e) {
 			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
 			if (renderer != null) {
 				// Revert to last zoom if we have one, but don't if the user has manually
 				// changed the scale since the last reset zoom (one to one index)
 				if (lastZoom != null && renderer.getScale() == renderer.getZoneScale().getOneToOneScale()) {
 					// Go back to the previous zoom
 					renderer.setScale(lastZoom);
 
 					// But make sure the next time we'll go back to 1:1
 					lastZoom = null;
 				} else {
 					lastZoom = renderer.getScale();
 					renderer.zoomReset(renderer.getWidth() / 2, renderer.getHeight() / 2);
 				}
 				renderer.maybeForcePlayersView();
 			}
 		}
 	};
 
 	public static final Action TOGGLE_ZONE_SELECTOR = new DefaultClientAction() {
 		{
 			init("action.showMapSelector");
 		}
 
 		@Override
 		public boolean isSelected() {
 			return MapTool.getFrame().getZoneMiniMapPanel().isVisible();
 		}
 
 		public void execute(ActionEvent e) {
 
 			JComponent panel = MapTool.getFrame().getZoneMiniMapPanel();
 
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
 						MapTool.showError("msg.error.alreadyRunningServer");
 						return;
 					}
 
 					// TODO: Need to shut down the existing server first;
 					StartServerDialog dialog = new StartServerDialog();
 					dialog.showDialog();
 
 					if (!dialog.accepted()) {
 						return;
 					}
 
 					StartServerDialogPreferences serverProps = new StartServerDialogPreferences();
 
 					ServerPolicy policy = new ServerPolicy();
 					policy.setUseStrictTokenManagement(serverProps.getUseStrictTokenOwnership());
 					policy.setPlayersCanRevealVision(serverProps.getPlayersCanRevealVision());
 					policy.setUseIndividualViews(serverProps.getUseIndividualViews());
 					policy.setPlayersReceiveCampaignMacros(serverProps.getPlayersReceiveCampaignMacros());
 					
 					// Tool Tips for unformatted inline rolls.
 					policy.setUseToolTipsForDefaultRollFormat(serverProps.getUseToolTipsForUnformattedRolls());
 					
 					//my addition
 					policy.setRestrictedImpersonation(serverProps.getRestrictedImpersonation());
 
 					ServerConfig config = new ServerConfig(serverProps.getUsername(), serverProps.getGMPassword(), serverProps.getPlayerPassword(), serverProps
 							.getPort(), serverProps.getRPToolsName());
 
 					// Use the existing campaign
 					Campaign campaign = MapTool.getCampaign();
 
 					boolean failed = false;
 					try {
 						ServerDisconnectHandler.disconnectExpected = true;
 						MapTool.stopServer();
 
 						// Use UPnP to open port in router
 						if (serverProps.getUseUPnP()) {
 							UPnPUtil.openPort(serverProps.getPort());
 						}
 
 						// Make a copy of the campaign since we don't coordinate
 						// local changes well ... yet
 						MapTool.startServer(dialog.getUsernameTextField().getText(), config, policy, new Campaign(campaign));
 						
 						// Connect to server
                         String playerType = dialog.getRoleCombo().getSelectedItem().toString();
                         if(playerType.equals("GM")){
 						MapTool.createConnection("localhost", serverProps.getPort(), new Player(dialog.getUsernameTextField().getText(), serverProps.getRole(),
 								serverProps.getGMPassword()));
                         }else{
                             MapTool.createConnection("localhost",serverProps.getPort(), new Player(dialog.getUsernameTextField().getText(), serverProps.getRole(),
                                 serverProps.getPlayerPassword()));
                         }
 
 						// connecting
 						MapTool.getFrame().getConnectionStatusPanel().setStatus(ConnectionStatusPanel.Status.server);
 						MapTool.addLocalMessage("<span style='color:blue'><i>" + I18N.getText("msg.info.startServer") + "</i></span>");
 					} catch (UnknownHostException uh) {
 						MapTool.showError("msg.error.invalidLocalhost", uh);
 						failed = true;
 					} catch (IOException ioe) {
 						MapTool.showError("msg.error.failedConnect", ioe);
 						failed = true;
 					}
 
 					if (failed) {
 						try {
 							MapTool.startPersonalServer(campaign);
 						} catch (IOException ioe) {
 							MapTool.showError("msg.error.failedStartPersonalServer", ioe);
 						}
 					}
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
 			
 			if (MapTool.isCampaignDirty() && !MapTool.confirm("msg.confirm.loseChanges")) {
 				return;
 			}
 
 			final ConnectToServerDialog dialog = new ConnectToServerDialog();
 
 			dialog.showDialog();
 
 			if (!dialog.accepted()) {
 				return;
 			}
 
 			ServerDisconnectHandler.disconnectExpected = true;
 			MapTool.stopServer();
 
 			// Install a temporary gimped campaign until we get the one from the
 			// server
 			final Campaign oldCampaign = MapTool.getCampaign();
 			MapTool.setCampaign(new Campaign());
 
 			// connecting
 			MapTool.getFrame().getConnectionStatusPanel().setStatus(ConnectionStatusPanel.Status.connected);
 
 			// Show the user something interesting until we've got the campaign
 			// Look in ClientMethodHandler.setCampaign() for the corresponding
 			// hideGlassPane
 			StaticMessageDialog progressDialog = new StaticMessageDialog(I18N.getText("msg.info.connecting"));
 			MapTool.getFrame().showFilledGlassPane(progressDialog);
 
 			runBackground(new Runnable() {
 
 				public void run() {
 					boolean failed = false;
 					try {
 						ConnectToServerDialogPreferences prefs = new ConnectToServerDialogPreferences();
 						MapTool.createConnection(dialog.getServer(), dialog.getPort(), new Player(prefs.getUsername(), prefs.getRole(), prefs.getPassword()));
 
 						MapTool.getFrame().hideGlassPane();
 						MapTool.getFrame().showFilledGlassPane(new StaticMessageDialog(I18N.getText("msg.info.campaignLoading")));
 
 					} catch (UnknownHostException e1) {
 						MapTool.showError("msg.error.unknownHost", e1);
 						failed = true;
 					} catch (IOException e1) {
 						MapTool.showError("msg.error.failedLoadCampaign", e1);
 						failed = true;
 					}
 
 					if (failed || MapTool.getConnection() == null) {
 						MapTool.getFrame().hideGlassPane();
 						try {
 							MapTool.startPersonalServer(oldCampaign);
 						} catch (IOException ioe) {
 							MapTool.showError("msg.error.failedStartPersonalServer", ioe);
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
 
 			if (MapTool.isHostingServer() && !MapTool.confirm("msg.confirm.hostingDisconnect")) {
 				return;
 			}
 
 			disconnectFromServer();
 		}
 
 	};
 
 	public static void disconnectFromServer() {
 		Campaign campaign = MapTool.isHostingServer() ? MapTool.getCampaign() : CampaignFactory.createBasicCampaign();
 		ServerDisconnectHandler.disconnectExpected = true;
 		MapTool.stopServer();
 		MapTool.disconnect();
 
 		try {
 			MapTool.startPersonalServer(campaign);
 		} catch (IOException ioe) {
 			MapTool.showError("msg.error.failedStartPersonalServer", ioe);
 		}
 	}
 
 	public static final Action LOAD_CAMPAIGN = new DefaultClientAction() {
 		{
 			init("action.loadCampaign");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return MapTool.isHostingServer() || MapTool.isPersonalServer();
 		}
 
 		public void execute(ActionEvent ae) {
 			
 			if (MapTool.isCampaignDirty() && !MapTool.confirm("msg.confirm.loseChanges")) {
 				return;
 			}
 
 			JFileChooser chooser = new CampaignPreviewFileChooser();
 			chooser.setDialogTitle(I18N.getText("msg.title.loadCampaign"));
 			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
 			if (chooser.showOpenDialog(MapTool.getFrame()) == JFileChooser.APPROVE_OPTION) {
 				File campaignFile = chooser.getSelectedFile();
 				loadCampaign(campaignFile);
 			}
 		}
 	};
 
 	private static class CampaignPreviewFileChooser extends PreviewPanelFileChooser {
 
 		CampaignPreviewFileChooser() {
 			super();
 			addChoosableFileFilter(MapTool.getFrame().getCmpgnFileFilter());
 		}
 
 		@Override
 		protected File getImageFileOfSelectedFile() {
 			if (getSelectedFile() == null) {
 				return null;
 			}
 			return PersistenceUtil.getCampaignThumbnailFile(getSelectedFile().getName());
 		}
 	}
 
 	public static void loadCampaign(final File campaignFile) {
 
 		new Thread() {
 			public void run() {
 
 				try {
 					StaticMessageDialog progressDialog = new StaticMessageDialog(I18N.getText("msg.info.campaignLoading"));
 
 					try {
 						// I'm going to get struck by lighting for
 						// writing code like this.
 						// CLEAN ME CLEAN ME CLEAN ME ! I NEED A
 						// SWINGWORKER !
 						MapTool.getFrame().setCurrentZoneRenderer(null);
 						ImageManager.flush(); // Clear out the old campaign's images
 						MapTool.getFrame().showFilledGlassPane(progressDialog);
 
 						// Before we do anything, let's back it up
 						if (MapTool.getBackupManager() != null) {
 							MapTool.getBackupManager().backup(campaignFile);
 						}
 
 						// Load
 						final PersistedCampaign campaign = PersistenceUtil.loadCampaign(campaignFile);
 
 						if (campaign != null) {
 
 							AppState.setCampaignFile(campaignFile);
 							AppPreferences.setLoadDir(campaignFile.getParentFile());
 
 							AppMenuBar.getMruManager().addMRUCampaign(campaignFile);
 
 							// Bypass the serialization when we are hosting the
 							// server
 							// TODO: This optimization doesn't work since the
 							// player name isn't the right thing to exclude this
 							// thread
 							// if (MapTool.isHostingServer() ||
 							// MapTool.isPersonalServer()) {
 							// MapTool.getServer().getMethodHandler().handleMethod(MapTool.getPlayer().getName(),
 							// ServerCommand.COMMAND.setCampaign.name(), new
 							// Object[]{campaign.campaign});
 							// } else {
 							MapTool.serverCommand().setCampaign(campaign.campaign);
 							// }
 
 							// TODO: This is wrong
 							if (campaign.currentView != null && MapTool.getFrame().getCurrentZoneRenderer() != null) {
 								MapTool.getFrame().getCurrentZoneRenderer().setZoneScale(campaign.currentView);
 							}
 							MapTool.setCampaign(campaign.campaign, campaign.currentZoneId);
 
 							MapTool.getAutoSaveManager().restart();
 							MapTool.getAutoSaveManager().tidy();
 
 							// UI related stuff
 							MapTool.getFrame().getCommandPanel().setIdentity(null);
 							MapTool.getFrame().resetPanels();
 
 						}
 
 					} finally {
 						MapTool.getFrame().hideGlassPane();
 					}
 
 				} catch (IOException ioe) {
 					MapTool.showError("msg.error.failedLoadCampaign", ioe);
 				}
 			}
 		}.start();
 	}
 
 	public static final Action LOAD_SAVE = new DeveloperClientAction() {
 		{
 			init("action.loadSaveDialog");
 		}
 
 		public void execute(ActionEvent ae) {
 			LoadSaveImpl impl = new LoadSaveImpl();
 			impl.saveApplication(); // All the work is done here
 		}
 	};
 
 	public static int saveStatus;
 
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
 
 			saveCampaign(MapTool.getCampaign(), AppState.getCampaignFile(), ae.getActionCommand());
 		}
 	};
 
 	private static void saveCampaign(final Campaign campaign, final File file, final String command) {
 		MapTool.getFrame().showFilledGlassPane(new StaticMessageDialog(I18N.getText("msg.info.campaignSaving")));
 		new SwingWorker<Object, Object>() {
 			@Override
 			protected Object doInBackground() throws Exception {
 				try {
 					AppState.setIsSaving(true);
 					
 					long start = System.currentTimeMillis();
 					PersistenceUtil.saveCampaign(campaign, file);
 					AppMenuBar.getMruManager().addMRUCampaign(AppState.getCampaignFile());
 					MapTool.getFrame().setStatusMessage(I18N.getString("msg.info.campaignSaved"));
 					
 					// Min display time so people can see the message
 					try {
 						Thread.sleep(Math.max(0, 500 - (System.currentTimeMillis() - start)));
 					} catch (InterruptedException e) {
 						// Nothing to do
 					}
 				} catch (IOException ioe) {
 					log.error("Failure to save: " + ioe, ioe);
 					MapTool.showError("msg.error.failedSaveCampaign");
 				} catch (Throwable t) {
 					log.error("Failure to save: " + t, t);
 					MapTool.showError("msg.error.failedSaveCampaign");
 				} finally {
 					AppState.setIsSaving(false);
 				}
 				
 				return null;
 			}
 			@Override
 			protected void done() {
 				MapTool.getFrame().hideGlassPane();
 				
 				if (command != null) {
 					// TODO: make this prettier.  I need to be able to tell the save command to exit the program
 					// on completion, so I'm hijacking the command value of the action.  Very.  Ugly.  Presumably it
 					// would be better passing some sort of Runnable to execute on completion.  But this will work for now
 					if ("close".equals(command)) {
 						MapTool.getFrame().close();
 					}
 				}
 			}
 		}.execute();
 	}
 	
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
 
 			JFileChooser chooser = MapTool.getFrame().getSaveCmpgnFileChooser();
 
 			saveStatus = chooser.showSaveDialog(MapTool.getFrame());
 			if (saveStatus == JFileChooser.APPROVE_OPTION) {
 
 				File campaignFile = chooser.getSelectedFile();
 				
 				if (campaignFile.exists() && !MapTool.confirm("msg.confirm.overwriteExistingCampaign")) {
 					return;
 				}
 				
 				if (campaignFile.getName().indexOf(".") < 0) {
 					campaignFile = new File(campaignFile.getAbsolutePath() + AppConstants.CAMPAIGN_FILE_EXTENSION);
 				}
 
 				saveCampaign(campaign, campaignFile, ae.getActionCommand());
 
 				AppState.setCampaignFile(campaignFile);
 				AppPreferences.setSaveDir(campaignFile.getParentFile());
 				AppMenuBar.getMruManager().addMRUCampaign(AppState.getCampaignFile());
 				MapTool.getFrame().setTitleViaRenderer(MapTool.getFrame().getCurrentZoneRenderer());
 			}
 		}
 	};
 
 	public static final Action SAVE_MAP_AS = new DefaultClientAction() {
 		{
 			init("action.saveMapAs");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return MapTool.isHostingServer() || MapTool.getPlayer().isGM();
 		}
 
 		public void execute(ActionEvent ae) {
 			ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
 			JFileChooser chooser = MapTool.getFrame().getSaveFileChooser();
 			chooser.setFileFilter(MapTool.getFrame().getMapFileFilter());
 			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 			chooser.setSelectedFile(new File(zr.getZone().getName()));
 			if (chooser.showSaveDialog(MapTool.getFrame()) == JFileChooser.APPROVE_OPTION) {
 				try {
 					File mapFile = chooser.getSelectedFile();
 					if (mapFile.getName().indexOf(".") < 0) {
 						mapFile = new File(mapFile.getAbsolutePath() + AppConstants.MAP_FILE_EXTENSION);
 					}
 					PersistenceUtil.saveMap(zr.getZone(), mapFile);
 					AppPreferences.setSaveDir(mapFile.getParentFile());
 					MapTool.showInformation("msg.info.mapSaved");
 				} catch (IOException ioe) {
 					ioe.printStackTrace();
 					MapTool.showError("msg.error.failedSaveMap");
 				}
 			}
 		}
 	};
 
 	/**
 	 * LOAD_MAP is the Action used to implement the loading of an externally
 	 * stored map into the current campaign. This Action is only available when
 	 * the current application is either hosting a server or is not connected to
 	 * a server.
 	 * 
 	 * Property used from <b>i18n.properties</b> is <code>action.loadMap</code>
 	 * 
 	 * @author FJE
 	 */
 	public static final Action LOAD_MAP = new DefaultClientAction() {
 		{
 			init("action.loadMap");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return MapTool.isHostingServer() || MapTool.isPersonalServer();
 		}
 
 		public void execute(ActionEvent ae) {       
 			JFileChooser chooser = new MapPreviewFileChooser();
 			chooser.setDialogTitle(I18N.getText("msg.title.loadMap"));
 			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
 			if (chooser.showOpenDialog(MapTool.getFrame()) == JFileChooser.APPROVE_OPTION) {
 				File mapFile = chooser.getSelectedFile();
 				loadMap(mapFile);
 			}
 		}
 	};
 
 	private static class MapPreviewFileChooser extends PreviewPanelFileChooser {
 
 		MapPreviewFileChooser() {
 			super();
 			addChoosableFileFilter(MapTool.getFrame().getMapFileFilter());
 		}
 
 		@Override
 		protected File getImageFileOfSelectedFile() {
 			if (getSelectedFile() == null) {
 				return null;
 			}
 			return PersistenceUtil.getCampaignThumbnailFile(getSelectedFile().getName());
 		}
 	}
 
 	public static void loadMap(final File mapFile) {
 		new Thread() {
 			public void run() {
 				try {
 					StaticMessageDialog progressDialog = new StaticMessageDialog(I18N.getText("msg.info.mapLoading"));
 
 					try {
 						// I'm going to get struck by lighting for writing code
 						// like this.
 						// CLEAN ME CLEAN ME CLEAN ME ! I NEED A SWINGWORKER !
 						MapTool.getFrame().showFilledGlassPane(progressDialog);
 
 						// Load
 						final PersistedMap map = PersistenceUtil.loadMap(mapFile);
 
 						if (map != null) {
 							AppPreferences.setLoadDir(mapFile.getParentFile());
 							MapTool.addZone(map.zone);
 
 							MapTool.getAutoSaveManager().restart();
 							MapTool.getAutoSaveManager().tidy();
 
 							// Flush the images associated with the current
 							// campaign
 							// Do this juuuuuust before we get ready to show the
 							// new campaign, since we
 							// don't want the old campaign reloading images
 							// while we loaded the new campaign
 
 							// XXX (FJE) Is this call even needed for loading
 							// maps? Probably not...
 							ImageManager.flush();
 						}
 					} finally {
 						MapTool.getFrame().hideGlassPane();
 					}
 				} catch (IOException ioe) {
 					ioe.printStackTrace();
 					MapTool.showError("msg.error.failedLoadMap");
 				}
 			}
 		}.start();
 	}
 
 	public static final Action CAMPAIGN_PROPERTIES = new DefaultClientAction() {
 		{
 			init("action.campaignProperties");
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return MapTool.getPlayer().isGM();
 		}
 
 		public void execute(ActionEvent ae) {
 
 			Campaign campaign = MapTool.getCampaign();
 
 			// TODO: There should probably be only one of these
 			CampaignPropertiesDialog dialog = new CampaignPropertiesDialog(MapTool.getFrame());
 			dialog.setCampaign(campaign);
 
 			dialog.setVisible(true);
 
 			if (dialog.getStatus() == CampaignPropertiesDialog.Status.CANCEL) {
 				return;
 			}
 
 			// TODO: Make this pass all properties, but we don't have that
 			// framework yet, so send what we
 			// know the old fashioned way
 			MapTool.serverCommand().updateCampaign(campaign.getCampaignProperties());
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
 
 		public QuickMapAction(String name, File imagePath) {
 
 			try {
 				Asset asset = new Asset(name, FileUtil.loadFile(imagePath));
 				assetId = asset.getId();
 
 				// Make smaller
 				BufferedImage iconImage = new BufferedImage(QUICK_MAP_ICON_SIZE, QUICK_MAP_ICON_SIZE, Transparency.OPAQUE);
 				Image image = MapTool.getThumbnailManager().getThumbnail(imagePath);
 
 				Graphics2D g = iconImage.createGraphics();
 				g.drawImage(image, 0, 0, QUICK_MAP_ICON_SIZE, QUICK_MAP_ICON_SIZE, null);
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
 
 					Asset asset = AssetManager.getAsset(assetId);
 
 					Zone zone = ZoneFactory.createZone();
 					zone.setBackgroundPaint(new DrawableTexturePaint(asset.getId()));
 					zone.setName(asset.getName());
 
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
 
 					Zone zone = ZoneFactory.createZone();
 					MapPropertiesDialog newMapDialog = new MapPropertiesDialog(MapTool.getFrame());
 					newMapDialog.setZone(zone);
 
 					newMapDialog.setVisible(true);
 
 					if (newMapDialog.getStatus() == MapPropertiesDialog.Status.OK) {
 						MapTool.addZone(zone);
 					}
 				}
 			});
 		}
 	};
 
 	public static final Action SHOW_DOCUMENTATION = new DefaultClientAction() {
 		{
 			init("action.showDocumentation");
 		}
 
 		public void execute(java.awt.event.ActionEvent e) {
 
 			runBackground(new Runnable() {
 
 				public void run() {
 
 					MapTool.showDocument(MapTool.getConfiguration().getHelpURL());
 				}
 			});
 		}
 	};
 
 	public static final Action SHOW_TUTORIALS = new DefaultClientAction() {
 		{
 			init("action.showTutorials");
 		}
 
 		public void execute(java.awt.event.ActionEvent e) {
 
 			runBackground(new Runnable() {
 
 				public void run() {
 
 					MapTool.showDocument(MapTool.getConfiguration().getTutorialsURL());
 				}
 			});
 		}
 	};
 
 	public static final Action SHOW_FORUMS = new DefaultClientAction() {
 		{
 			init("action.showForums");
 		}
 
 		public void execute(java.awt.event.ActionEvent e) {
 
 			runBackground(new Runnable() {
 
 				public void run() {
 
 					MapTool.showDocument(MapTool.getConfiguration().getForumURL());
 				}
 			});
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
 					chooser.setDialogTitle(I18N.getText("msg.title.loadAssetTree"));
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
 
 			if (!MapTool.getFrame().confirmClose()) {
 				return;
 			} else {
 				MapTool.getFrame().closingMaintenance();
 			}
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
 			MapTool.getFrame().setPaintDrawingMeasurement(!MapTool.getFrame().isPaintDrawingMeasurement());
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
 
 	public static class ToggleWindowAction extends ClientAction {
 
 		private MTFrame mtFrame;
 
 		public ToggleWindowAction(MTFrame mtFrame) {
 			this.mtFrame = mtFrame;
 			init(mtFrame.getPropertyName());
 		}
 
 		@Override
 		public boolean isSelected() {
 			return MapTool.getFrame().getFrame(mtFrame).isVisible();
 		}
 
 		@Override
 		public boolean isAvailable() {
 			return true;
 		}
 
 		@Override
 		public void execute(ActionEvent event) {
 			DockableFrame frame = MapTool.getFrame().getFrame(mtFrame);
 			if (frame.isVisible()) {
 				MapTool.getFrame().getDockingManager().hideFrame(mtFrame.name());
 			} else {
 				MapTool.getFrame().getDockingManager().showFrame(mtFrame.name());
 			}
 		}
 	}
 
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
 			init(key, true);
 		}
 
 		public void init(String key, boolean addMenuShortcut) {
 			I18N.setAction(key, this, addMenuShortcut);
 			getActionList().add(this);
 		}
 
 		/**
 		 * This convenience function returns the KeyStroke that represents the
 		 * accelerator key used by the Action. This function can return
 		 * <code>null</code> because not all Actions have an associated
 		 * accelerator key defined, but it is currently only called by methods
 		 * that reference the {CUT,COPY,PASTE}_TOKEN Actions.
 		 * 
 		 * @return KeyStroke associated with the Action or <code>null</code>
 		 */
 		public final KeyStroke getKeyStroke() {
 			return (KeyStroke) getValue(Action.ACCELERATOR_KEY);
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
 					r.run();
 
 					updateActions();
 				}
 			}.start();
 		}
 	}
 
 	/**
 	 * This class simply provides an implementation for
 	 * <code>isAvailable()</code> that returns <code>true</code> if the current
 	 * player is a GM.
 	 */
 	public static abstract class AdminClientAction extends ClientAction {
 
 		@Override
 		public boolean isAvailable() {
 			return MapTool.getPlayer().isGM();
 		}
 	}
 
 	/**
 	 * This class simply provides an implementation for
 	 * <code>isAvailable()</code> that returns <code>true</code> if the current
 	 * player is a GM and there is a ZoneRenderer current.
 	 */
 	public static abstract class ZoneAdminClientAction extends AdminClientAction {
 
 		@Override
 		public boolean isAvailable() {
 			return super.isAvailable() && MapTool.getFrame().getCurrentZoneRenderer() != null;
 		}
 	}
 
 	/**
 	 * This class simply provides an implementation for
 	 * <code>isAvailable()</code> that returns <code>true</code>.
 	 */
 	public static abstract class DefaultClientAction extends ClientAction {
 
 		@Override
 		public boolean isAvailable() {
 			return true;
 		}
 	}
 
 	/**
 	 * This class simply provides an implementation for
 	 * <code>isAvailable()</code> that returns <code>true</code> if the system
 	 * property MAPTOOL_DEV is not set to "false".  This allows it to contain the version
 	 * number of the compatible build for debugging purposes.  For example, if I'm working
 	 * on a patch to 1.3.b54, I can set MAPTOOL_DEV to 1.3.b54 in order to test it against
 	 * a 1.3.b54 client.
 	 */
 	@SuppressWarnings("serial")
 	public static abstract class DeveloperClientAction extends ClientAction {
 
 		@Override
 		public boolean isAvailable() {
 			return System.getProperty("MAPTOOL_DEV") != null && ! "false".equals(System.getProperty("MAPTOOL_DEV"));
 		}
 	}
 
 	public static class OpenMRUCampaign extends AbstractAction {
 
 		private File campaignFile;
 
 		public OpenMRUCampaign(File file, int position) {
 
 			campaignFile = file;
 			String label = position + " " + campaignFile.getName();
 			putValue(Action.NAME, label);
 
 			if (position <= 9) {
 				int keyCode = KeyStroke.getKeyStroke(Integer.toString(position)).getKeyCode();
 				putValue(Action.MNEMONIC_KEY, keyCode);
 			}
 
 			// Use the saved campaign thumbnail as a tooltip
 			File thumbFile = PersistenceUtil.getCampaignThumbnailFile(campaignFile.getName());
 			String htmlTip;
 
 			if (thumbFile.exists()) {
 				htmlTip = "<html><img src=\"file:///" + thumbFile.getPath() + "\"></html>";
 			} else {
 				htmlTip = I18N.getText("msg.info.noCampaignPreview");
 			}
 
 			/*
 			 * There is some extra space appearing to the right of the images,
 			 * which sounds similar to what was reported in this bug (bottom
 			 * half): http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5047379
 			 * Removing the mnemonic will remove this extra space.
 			 */
 			putValue(Action.SHORT_DESCRIPTION, htmlTip);
 		}
 
 		public void actionPerformed(ActionEvent ae) {
 			if (MapTool.isCampaignDirty() && !MapTool.confirm("msg.confirm.loseChanges")) {
 				return;
 			}
 			
 			AppActions.loadCampaign(campaignFile);
 		}
 	}
 
 }
