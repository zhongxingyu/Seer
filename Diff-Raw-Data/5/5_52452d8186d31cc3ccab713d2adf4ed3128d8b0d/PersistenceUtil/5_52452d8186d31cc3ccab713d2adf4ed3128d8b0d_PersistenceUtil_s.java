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
 package net.rptools.maptool.util;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.imageio.ImageIO;
 
 import net.rptools.lib.CodeTimer;
 import net.rptools.lib.FileUtil;
 import net.rptools.lib.MD5Key;
 import net.rptools.lib.ModelVersionManager;
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.lib.io.PackedFile;
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.AppConstants;
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ui.Scale;
 import net.rptools.maptool.client.ui.zone.PlayerView;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Campaign;
 import net.rptools.maptool.model.CampaignProperties;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.LookupTable;
 import net.rptools.maptool.model.MacroButtonProperties;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.transform.campaign.AssetNameTransform;
 import net.rptools.maptool.model.transform.campaign.PCVisionTransform;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 
 import com.caucho.hessian.io.HessianInput;
 import com.thoughtworks.xstream.XStream;
 
 /**
  * @author trevor
  */
 public class PersistenceUtil {
 
 	private static final Logger log = Logger.getLogger(PersistenceUtil.class);
 
 	private static final String PROP_VERSION = "version";
 	private static final String PROP_CAMPAIGN_VERSION = "campaignVersion";
 	private static final String ASSET_DIR = "assets/";
 
 	private static final String CAMPAIGN_VERSION = "1.3.70";
 	// Please add a single note regarding why the campaign version number has been updated:
 	// 1.3.70	ownerOnly added to model.Light (not backward compatible)
 
 	private static final ModelVersionManager campaignVersionManager = new ModelVersionManager();
 	private static final ModelVersionManager assetnameVersionManager = new ModelVersionManager();
 
 	static {
 		PackedFile.init(AppUtil.getAppHome("tmp"));
 
 		// Whenever a new transformation needs to be added, put the version of MT into the CAMPAIGN_VERSION
 		// variable, and use that as the key to the following register call
 		// This gives us a rough estimate how far backwards compatible the model is
 		// If you need sub-minor version level granularity, simply add another dot value at the end (e.g. 1.3.51.1)
 		// XXX Need to change this to use XSLTC and allow the XSLT to be specified external to the code.
 		campaignVersionManager.registerTransformation("1.3.51", new PCVisionTransform());
 
 		// For a short time, assets were stored separately in files ending with ".dat".  As of 1.3.64, they are
 		// stored in separate files using the correct filename extension for the image type.  This transform
 		// is used to convert asset filenames and not XML.  Old assets with the image embedded as Base64
 		// text are still supported for reading by using an XStream custom Converter.  See the Asset
 		// class for the annotation used to reference the converter.
 		assetnameVersionManager.registerTransformation("1.3.51", new AssetNameTransform("^(.*)\\.(dat)?$", "$1"));
 	}
 
 	public static class PersistedMap {
 		public Zone zone;
 		public Map<MD5Key, Asset> assetMap = new HashMap<MD5Key, Asset>();
 		public String mapToolVersion;
 	}
 
 	public static class PersistedCampaign {
 		public Campaign campaign;
 		public Map<MD5Key, Asset> assetMap = new HashMap<MD5Key, Asset>();
 		public GUID currentZoneId;
 		public Scale currentView;
 		public String mapToolVersion;
 	}
 
 	public static void saveMap(Zone z, File mapFile) throws IOException {
 		PackedFile pakFile = new PackedFile(mapFile);
 		PersistedMap pMap = new PersistedMap();
 		pMap.zone = z;
 
 		// Save all assets in active use (consolidate dups)
 		Set<MD5Key> allAssetIds = z.getAllAssetIds();
 		for (MD5Key key : allAssetIds) {
 			// Put in a placeholder
 			pMap.assetMap.put(key, null);
 		}
 		saveAssets(z.getAllAssetIds(), pakFile);
 
 		pakFile.setContent(pMap);
 		pakFile.setProperty(PROP_VERSION, MapTool.getVersion());
 
 		pakFile.save();
 		pakFile.close();
 	}
 
 	public static PersistedMap loadMap(File mapFile) throws IOException {
 		PackedFile pakfile = new PackedFile(mapFile);
 		try {
 			// Sanity check
 //			String version = (String) pakfile.getProperty(PROP_VERSION);
 			PersistedMap persistedMap = (PersistedMap) pakfile.getContent();
 
 			// Now load up any images that we need
 			loadAssets(persistedMap.assetMap.keySet(), pakfile);
 
 			// FJE We only want the token's graphical data, so loop through all tokens and
 			// destroy all properties and macros.  Keep some fields, though.  Since that type
 			// of object editing doesn't belong here, we just call Token.imported() and let
 			// that method Do The Right Thing.
 			for (Iterator<Token> iter = persistedMap.zone.getAllTokens().iterator(); iter.hasNext();) {
 				Token token = iter.next();
 				token.imported();
 			}
 			// XXX FJE This doesn't work the way I want it to.  But doing this the Right Way
 			// is too much work right now. :-}
 			Zone z = persistedMap.zone;
 			String n = z.getName();
 			String count = n.replaceFirst("Import (\\d+) of.*", "$1");
 			Integer next = 1;
 			try {
 				next = StringUtil.parseInteger(count) + 1;
 			} catch (ParseException e) {
 			}
 			n = n.replaceFirst("Import \\d+ of ", "Import " + next + " of ");
 			z.setName(n);
 			z.imported();			// Resets creation timestamp, amongst other things
 			z.optimize();
 			return persistedMap;
 		} catch (IOException ioe) {
 			MapTool.showError("While reading map data from file", ioe);
 			throw ioe;
 		}
 	}
 
 	public static void saveCampaign(Campaign campaign, File campaignFile) throws IOException {
 		CodeTimer saveTimer;		// FJE Previously this was 'private static' -- why?
 		saveTimer = new CodeTimer("Save");
 		saveTimer.setThreshold(5);
 		saveTimer.setEnabled(log.isDebugEnabled());		// Don't bother keeping track if it won't be displayed...
 
 		// Strategy: save the file to a tmp location so that if there's a failure the original file
 		// won't be touched. Then once we're finished, replace the old with the new.
 		File tmpDir = AppUtil.getTmpDir();
 		File tmpFile = new File(tmpDir.getAbsolutePath() + "/" + campaignFile.getName());
 		if (tmpFile.exists())
 			tmpFile.delete();
 
 		PackedFile pakFile = new PackedFile(tmpFile);
 
 		// Configure the meta file (this is for legacy support)
 		PersistedCampaign persistedCampaign = new PersistedCampaign();
 
 		persistedCampaign.campaign = campaign;
 
 		// Keep track of the current view
 		ZoneRenderer currentZoneRenderer = MapTool.getFrame().getCurrentZoneRenderer();
 		if (currentZoneRenderer != null) {
 			persistedCampaign.currentZoneId = currentZoneRenderer.getZone().getId();
 			persistedCampaign.currentView = currentZoneRenderer.getZoneScale();
 		}
 
 		// Save all assets in active use (consolidate dups between maps)
 		saveTimer.start("Collect all assets");
 		Set<MD5Key> allAssetIds = campaign.getAllAssetIds();
 		for (MD5Key key : allAssetIds) {
 			// Put in a placeholder
 			persistedCampaign.assetMap.put(key, null);
 		}
 		saveTimer.stop("Collect all assets");
 
 		// And store the asset elsewhere
 		saveTimer.start("Save assets");
 		saveAssets(allAssetIds, pakFile);
 		saveTimer.stop("Save assets");
 
 		try {
 			saveTimer.start("Set content");
 			pakFile.setContent(persistedCampaign);
 			pakFile.setProperty(PROP_VERSION, MapTool.getVersion());
 			pakFile.setProperty(PROP_CAMPAIGN_VERSION, CAMPAIGN_VERSION);
 			saveTimer.stop("Set content");
 
 			saveTimer.start("Save");
 			pakFile.save();
 			saveTimer.stop("Save");
 
 			saveTimer.start("Close");
 			pakFile.close();
 			saveTimer.stop("Close");
 		} catch (OutOfMemoryError oom) {
 			/*
 			 * This error is normally because the heap space has been exceeded while trying to
 			 * save the campaign.  Since MapTool caches the images used by the current Zone,
 			 * and since the VersionManager must keep the XML for objects in memory in
 			 * order to apply transforms to them, the memory usage can spike very high
 			 * during the save() operation.  A common solution is to switch to an empty map
 			 * and perform the save from there; this causes MapTool to unload any images
 			 * that it may have had cached and this can frequently free up enough memory
 			 * for the save() to work.  We'll tell the user all this right here and then fail the
 			 * save and they can try again.
 			 */
 			if (log.isDebugEnabled()) {
 				log.debug(saveTimer);
 			}
 			try {
 				pakFile.close();
 			} catch (Exception e) { }
 			pakFile = null;
 			tmpFile.delete();
 			MapTool.showError("msg.error.failedSaveCampaignOOM");
 		}
 
 		// Copy to the new location
 		// Not the fastest solution in the world, but worth the safety net it
 		// provides
 		saveTimer.start("backup");
 		File bakFile = new File(tmpDir.getAbsolutePath() + "/" + campaignFile.getName() + ".bak");
 		bakFile.delete();
 		if (campaignFile.exists()) {
 			if (!campaignFile.renameTo(bakFile)) {
 				FileUtil.copyFile(campaignFile, bakFile);
 				campaignFile.delete();
 			}
 		}
 		if (!tmpFile.renameTo(campaignFile)) {
 			FileUtil.copyFile(tmpFile, campaignFile);
 			tmpFile.delete();
 		}
 		if (bakFile.exists())
 			bakFile.delete();
 		saveTimer.stop("backup");
 
 		// Save the campaign thumbnail
 		saveTimer.start("thumbnail");
 		saveCampaignThumbnail(campaignFile.getName());
 		saveTimer.stop("thumbnail");
 
 		if (log.isDebugEnabled()) {
 			log.debug(saveTimer);
 		}
 	}
 
 	/*
 	 * A public function because I think it should be called when a campaign is
 	 * opened as well so if it is opened then closed without saving, there is
 	 * still a preview created; however, the rendering of the campaign appears
 	 * to complete after AppActions.loadCampaign returns, causing the preview to
 	 * always appear as black if this method is called from within loadCampaign.
 	 * Either need to find another place to call saveCampaignThumbnail upon
 	 * opening, or code to delay it's call until the render is complete. =P
 	 */
 	static public void saveCampaignThumbnail(String fileName) {
 		BufferedImage screen = MapTool.takeMapScreenShot(new PlayerView(MapTool.getPlayer().getRole()));
 		if (screen == null) {
 			return;
 		}
 
 		Dimension imgSize = new Dimension(screen.getWidth(null), screen.getHeight(null));
 		SwingUtil.constrainTo(imgSize, 200, 200);
 
 		BufferedImage thumb = new BufferedImage(imgSize.width, imgSize.height, BufferedImage.TYPE_INT_BGR);
 		Graphics2D g2d = thumb.createGraphics();
 		g2d.drawImage(screen, 0, 0, imgSize.width, imgSize.height, null);
 		g2d.dispose();
 
 		File thumbFile = getCampaignThumbnailFile(fileName);
 
 		try {
 			ImageIO.write(thumb, "jpg", thumbFile);
 		} catch (IOException ioe) {
 			MapTool.showError("msg.error.failedSaveCampaignPreview", ioe);
 		}
 	}
 
 	/**
 	 * Gets a file pointing to where the campaign's thumbnail image should be.
 	 *
 	 * @param fileName
 	 *            The campaign's file name.
 	 */
 	public static File getCampaignThumbnailFile(String fileName) {
 		return new File(AppUtil.getAppHome("campaignthumbs"), fileName + ".jpg");
 	}
 
 	public static PersistedCampaign loadCampaign(File campaignFile) throws IOException {
 		// Try the new way first
 		PackedFile pakfile = new PackedFile(campaignFile);
 		pakfile.setModelVersionManager(campaignVersionManager);
 		PersistedCampaign persistedCampaign = null;
 		try {
 			// Sanity check
			String mtversion = MapTool.getVersion();
 			String version = (String)pakfile.getProperty(PROP_CAMPAIGN_VERSION);
 			version = version == null ? "1.3.50" : version;	// This is where the campaignVersion was added
 
 			// If this version of MapTool is equal to or later than the one in the file, all is good. :)
 			if (!MapTool.isDevelopment() &&
					ModelVersionManager.isBefore(ModelVersionManager.cleanVersionNumber(mtversion), version)) {
 				// If this version of MapTool is prior to the one in the file, give a chance to abort.
 				boolean okay;
 				okay = MapTool.confirm("msg.confirm.newerVersion", MapTool.getVersion(), version);
 				if (!okay) {
 					return null;
 				}
 			}
 			persistedCampaign = (PersistedCampaign) pakfile.getContent(version);
 			if (persistedCampaign != null) {
 				// Now load up any images that we need
 				// Note that the values are all placeholders
 				Set<MD5Key> allAssetIds = persistedCampaign.assetMap.keySet();
 				loadAssets(allAssetIds, pakfile);
 				for (Zone zone : persistedCampaign.campaign.getZones()) {
 					zone.optimize();
 				}
 				return persistedCampaign;
 			}
 		} catch (RuntimeException rte) {
 			MapTool.showError("Error while reading campaign file", rte);
 		} catch (java.lang.Error e) {
 			// Probably an issue with XStream not being able to instantiate a given class
 			// The old legacy technique probably won't work, but we should at least try...
 		} finally {
 			pakfile.close();
 		}
 		log.error("Could not load campaign in the current format, trying old format");
 		persistedCampaign = loadLegacyCampaign(campaignFile);
 		if (persistedCampaign == null)
 			MapTool.showWarning("Cannot determine campaign file format; not loaded");
 		return persistedCampaign;
 	}
 
 	public static PersistedCampaign loadLegacyCampaign(File campaignFile) {
 		HessianInput his = null;
 		PersistedCampaign persistedCampaign = null;
 		try {
 			InputStream is = new BufferedInputStream(new FileInputStream(campaignFile));
 			his = new HessianInput(is);
 			persistedCampaign = (PersistedCampaign) his.readObject(null);
 
 			for (MD5Key key : persistedCampaign.assetMap.keySet()) {
 				Asset asset = persistedCampaign.assetMap.get(key);
 				if (!AssetManager.hasAsset(key))
 					AssetManager.putAsset(asset);
 				if (!MapTool.isHostingServer() && !MapTool.isPersonalServer()) {
 					// If we are remotely installing this campaign, we'll need to
 					// send the image data to the server
 					MapTool.serverCommand().putAsset(asset);
 				}
 			}
 			// Do some sanity work on the campaign
 			// This specifically handles the case when the zone mappings
 			// are out of sync in the save file
 			Campaign campaign = persistedCampaign.campaign;
 			Set<Zone> zoneSet = new HashSet<Zone>(campaign.getZones());
 			campaign.removeAllZones();
 			for (Zone zone : zoneSet) {
 				campaign.putZone(zone);
 			}
 		} catch (FileNotFoundException fnfe) {
 			if (log.isInfoEnabled())
 				log.info("Campaign file not found (this can't happen?!)", fnfe);
 			persistedCampaign = null;
 		} catch (IOException ioe) {
 			if (log.isInfoEnabled())
 				log.info("Campaign is not in legacy Hessian format", ioe);
 			persistedCampaign = null;
 		} finally {
 			try {
 				his.close();
 			} catch (Exception e) { }
 		}
 		return persistedCampaign;
 	}
 
 	public static BufferedImage getTokenThumbnail(File file) throws Exception {
 		PackedFile pakFile = new PackedFile(file);
 		BufferedImage thumb = null;
 		if (pakFile.hasFile(Token.FILE_THUMBNAIL)) {
 			InputStream is = pakFile.getFileAsInputStream(Token.FILE_THUMBNAIL);
 			try {
 				thumb = ImageIO.read(is);
 			} finally {
 				if (is != null) {
 					is.close();
 				}
 			}
 		}
 		pakFile.close();
 		return thumb;
 	}
 
 	public static void saveToken(Token token, File file) throws IOException {
 
 		PackedFile pakFile = new PackedFile(file);
 		saveAssets(token.getAllImageAssets(), pakFile);
 
 		// Thumbnail
 		BufferedImage image = ImageManager.getImage(token.getImageAssetId());
 		Dimension sz = new Dimension(image.getWidth(), image.getHeight());
 		SwingUtil.constrainTo(sz, 50);
 		BufferedImage thumb = new BufferedImage(sz.width, sz.height, BufferedImage.TRANSLUCENT);
 		Graphics2D g = thumb.createGraphics();
 		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
 		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
 		g.drawImage(image, 0, 0, sz.width, sz.height, null);
 		g.dispose();
 
 		pakFile.putFile(Token.FILE_THUMBNAIL, ImageUtil.imageToBytes(thumb, "png"));
 
 		pakFile.setContent(token);
 		pakFile.setProperty(PROP_VERSION, MapTool.getVersion());
 
 		pakFile.save();
 		pakFile.close();
 
 	}
 
 	public static Token loadToken(File file) throws IOException {
 
 		PackedFile pakFile = new PackedFile(file);
 
 		// TODO: Check version
 //		String mtVersion = (String)pakFile.getProperty(PROP_VERSION);
 		Token token = (Token) pakFile.getContent();
 
 		loadAssets(token.getAllImageAssets(), pakFile);
 
 		return token;
 	}
 
 	private static void loadAssets(Collection<MD5Key> assetIds, PackedFile pakFile) throws IOException {
 		pakFile.getXStream().processAnnotations(Asset.class);
 		String campVersion = (String)pakFile.getProperty(PROP_CAMPAIGN_VERSION);
 		String mtVersion = (String)pakFile.getProperty(PROP_VERSION);
 		List<Asset> addToServer = new ArrayList<Asset>(assetIds.size());
 
 		for (MD5Key key : assetIds) {
 			if (key == null) {
 				continue;
 			}
 
 			if (!AssetManager.hasAsset(key)) {
 				String pathname = ASSET_DIR + key;
 				Asset asset;
 				if (mtVersion.equals("1.3.b64")) {
 					InputStream is = pakFile.getFileAsInputStream(pathname);
 					asset = new Asset(key.toString(), IOUtils.toByteArray(is));	// Ugly bug fix :(
 					is.close();
 				} else {
 					asset = (Asset) pakFile.getFileObject(pathname);			// XML deserialization
 				}
 				if (asset == null) {	// Referenced asset not included in PackedFile??
 					log.error("Referenced asset '" + pathname + "' not found while loading?!");
 					continue;
 				}
 				// If the asset was marked as "broken" then ignore it completely.  The end
 				// result is that MT will attempt to load it from a repository again, as normal.
 				if ("broken".equals(asset.getName())) {
 					log.warn("Reference to 'broken' asset '" + pathname + "' not restored.");
 					continue;
 				}
 				// pre 1.3b52 campaign files stored the image data directly in the asset serialization
 				if (asset.getImage() == null
 						|| asset.getImage().length < 4	// New XStreamConverter creates empty byte[] for image
 				) {
 					String ext = asset.getImageExtension();
 					pathname = pathname + "." + (StringUtil.isEmpty(ext) ? "dat" : ext);
 					pathname = assetnameVersionManager.transform(pathname, campVersion);
 					InputStream is = pakFile.getFileAsInputStream(pathname);
 					asset.setImage(IOUtils.toByteArray(is));
 					is.close();
 				}
 				AssetManager.putAsset(asset);
 				addToServer.add(asset);
 			}
 		}
 		if (!addToServer.isEmpty()) {
 			// Isn't this the same as (MapTool.getServer() == null) ?  And won't there always
 			// be a server?  Even if we don't start one explicitly, MapTool keeps a server
 			// running in the background all the time so that the rest of the code is
 			// consistent with regard to client<->server operations...
 			boolean server = !MapTool.isHostingServer() && !MapTool.isPersonalServer();
 			if (server) {
 				if (MapTool.isDevelopment())
 					MapTool.showInformation("Please report this:  !isHostingServer() && !isPersonalServer() == true");
 				// If we are remotely installing this token, we'll need to
 				// send the image data to the server
 				for (Asset asset : addToServer) {
 					MapTool.serverCommand().putAsset(asset);
 				}
 			}
 			addToServer.clear();
 		}
 	}
 
 	private static void saveAssets(Collection<MD5Key> assetIds, PackedFile pakFile) throws IOException {
 		pakFile.getXStream().processAnnotations(Asset.class);
 		for (MD5Key assetId : assetIds) {
 			if (assetId == null)
 				continue;
 
 			// And store the asset elsewhere
 			// As of 1.3.b64, assets are written in binary to allow them to be readable
 			// when a campaign file is unpacked.
 			Asset asset = AssetManager.getAsset(assetId);
 			if (asset == null) {
 				log.error("AssetId " + assetId + " not found while saving?!");
 				continue;
 			}
 			pakFile.putFile(ASSET_DIR + assetId + "." + asset.getImageExtension(), asset.getImage());
 			pakFile.putFile(ASSET_DIR + assetId, asset);		// Does not write the image
 //			pakFile.putFile(ASSET_DIR + assetId + ".dat", asset.getImage());
 		}
 	}
 
 	private static void clearAssets(PackedFile pakFile) throws IOException {
 		for (String path : pakFile.getPaths()) {
 			if (path.startsWith(ASSET_DIR) && !path.equals(ASSET_DIR))
 				pakFile.removeFile(path);
 		} // endfor
 	}
 
 	public static CampaignProperties loadLegacyCampaignProperties(File file) throws IOException {
 		if (!file.exists())
 			throw new FileNotFoundException();
 
 		FileInputStream in = new FileInputStream(file);
 		try {
 			return loadCampaignProperties(in);
 		} finally {
 			if (in != null) {
 				in.close();
 			}
 		}
 	}
 
 	public static CampaignProperties loadCampaignProperties(InputStream in) throws IOException {
 		return (CampaignProperties) new XStream().fromXML(new InputStreamReader(in, "UTF-8"));
 	}
 
 	public static CampaignProperties loadCampaignProperties(File file) throws IOException {
 		try {
 			PackedFile pakFile = new PackedFile(file);
 			String version = (String) pakFile.getProperty(PROP_VERSION); // Sanity
 			// check
 			CampaignProperties props = (CampaignProperties) pakFile.getContent();
 			loadAssets(props.getAllImageAssets(), pakFile);
 			return props;
 		} catch (IOException e) {
 			return loadLegacyCampaignProperties(file);
 		}
 	}
 
 	public static void saveCampaignProperties(Campaign campaign, File file) throws IOException {
 
 		// Put this in FileUtil
 		if (file.getName().indexOf(".") < 0) {
 			file = new File(file.getAbsolutePath() + AppConstants.CAMPAIGN_PROPERTIES_FILE_EXTENSION);
 		}
 		PackedFile pakFile = new PackedFile(file);
 		clearAssets(pakFile);
 		saveAssets(campaign.getCampaignProperties().getAllImageAssets(), pakFile);
 		pakFile.setContent(campaign.getCampaignProperties());
 		pakFile.setProperty(PROP_VERSION, MapTool.getVersion());
 		pakFile.save();
 		pakFile.close();
 	}
 
 	// Macro import/export support
 	public static MacroButtonProperties loadLegacyMacro(File file) throws IOException {
 		if (!file.exists())
 			throw new FileNotFoundException();
 
 		FileInputStream in = new FileInputStream(file);
 		try {
 			return loadMacro(in);
 		} finally {
 			try {
 				if (in != null) in.close();
 			} catch (Exception e) { }
 		}
 	}
 
 	public static MacroButtonProperties loadMacro(InputStream in) throws IOException {
 		return (MacroButtonProperties) new XStream().fromXML(new InputStreamReader(in, "UTF-8"));
 	}
 
 	public static MacroButtonProperties loadMacro(File file) throws IOException {
 		try {
 			PackedFile pakFile = new PackedFile(file);
 			String version = (String) pakFile.getProperty(PROP_VERSION); // Sanity check
 			MacroButtonProperties macroButton = (MacroButtonProperties) pakFile.getContent();
 			return macroButton;
 		} catch (IOException e) {
 			return loadLegacyMacro(file);
 		}
 	}
 
 	public static void saveMacro(MacroButtonProperties macroButton, File file) throws IOException {
 
 		// Put this in FileUtil
 		if (file.getName().indexOf(".") < 0) {
 			file = new File(file.getAbsolutePath() + AppConstants.MACRO_FILE_EXTENSION);
 		}
 		PackedFile pakFile = new PackedFile(file);
 		pakFile.setContent(macroButton);
 		pakFile.setProperty(PROP_VERSION, MapTool.getVersion());
 		pakFile.save();
 		pakFile.close();
 	}
 
 	public static List<MacroButtonProperties> loadLegacyMacroSet(File file) throws IOException {
 
 		if (!file.exists()) {
 			throw new FileNotFoundException();
 		}
 
 		FileInputStream in = new FileInputStream(file);
 		try {
 			return loadMacroSet(in);
 		} finally {
 			if (in != null) {
 				in.close();
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<MacroButtonProperties> loadMacroSet(InputStream in) throws IOException {
 		return (List<MacroButtonProperties>) new XStream().fromXML(new InputStreamReader(in, "UTF-8"));
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<MacroButtonProperties> loadMacroSet(File file) throws IOException {
 		try {
 			PackedFile pakFile = new PackedFile(file);
 			String version = (String) pakFile.getProperty(PROP_VERSION); // Sanity
 			// check
 			List<MacroButtonProperties> macroButtonSet = (List<MacroButtonProperties>) pakFile.getContent();
 			return macroButtonSet;
 		} catch (IOException e) {
 			return loadLegacyMacroSet(file);
 		}
 	}
 
 	public static void saveMacroSet(List<MacroButtonProperties> macroButtonSet, File file) throws IOException {
 
 		// Put this in FileUtil
 		if (file.getName().indexOf(".") < 0) {
 			file = new File(file.getAbsolutePath() + AppConstants.MACROSET_FILE_EXTENSION);
 		}
 		PackedFile pakFile = new PackedFile(file);
 		pakFile.setContent(macroButtonSet);
 		pakFile.setProperty(PROP_VERSION, MapTool.getVersion());
 		pakFile.save();
 		pakFile.close();
 	}
 
 	// end of Macro import/export support
 
 	// Table import/export support
 	public static LookupTable loadLegacyTable(File file) throws IOException {
 		if (!file.exists())
 			throw new FileNotFoundException();
 
 		FileInputStream in = new FileInputStream(file);
 		try {
 			return loadTable(in);
 		} finally {
 			if (in != null) {
 				in.close();
 			}
 		}
 	}
 
 	public static LookupTable loadTable(InputStream in) throws IOException {
 		return (LookupTable) new XStream().fromXML(new InputStreamReader(in, "UTF-8"));
 	}
 
 	public static LookupTable loadTable(File file) throws IOException {
 		try {
 			PackedFile pakFile = new PackedFile(file);
 //			String version = (String) pakFile.getProperty(PROP_VERSION); // Sanity
 			// check
 			LookupTable lookupTable = (LookupTable) pakFile.getContent();
 			loadAssets(lookupTable.getAllAssetIds(), pakFile);
 			return lookupTable;
 		} catch (IOException e) {
 			return loadLegacyTable(file);
 		}
 	}
 
 	public static void saveTable(LookupTable lookupTable, File file) throws IOException {
 		// Put this in FileUtil
 		if (file.getName().indexOf(".") < 0) {
 			file = new File(file.getAbsolutePath() + AppConstants.TABLE_FILE_EXTENSION);
 		}
 		PackedFile pakFile = new PackedFile(file);
 		pakFile.setContent(lookupTable);
 		saveAssets(lookupTable.getAllAssetIds(), pakFile);
 		pakFile.setProperty(PROP_VERSION, MapTool.getVersion());
 		pakFile.save();
 		pakFile.close();
 	}
 }
