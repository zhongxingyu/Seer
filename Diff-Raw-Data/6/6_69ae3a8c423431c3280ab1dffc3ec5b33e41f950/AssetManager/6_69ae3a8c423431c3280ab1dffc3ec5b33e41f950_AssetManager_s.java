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
 package net.rptools.maptool.model;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import net.rptools.lib.FileUtil;
 import net.rptools.lib.MD5Key;
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.MapTool;
 
 /**
  * This class handles the caching, loading,
  * and downloading of assets. All assets are
  * loaded through this class.
  * 
  * @author RPTools Team
  *
  */
 public class AssetManager {
 
 	/** Assets are associated with the MD5 sum of their raw data */
 	private static Map<MD5Key, Asset> assetMap = new ConcurrentHashMap<MD5Key, Asset>();
 
 	/** Location of the cache on the filesystem */
 	private static File cacheDir;
 
 	/** True if a persistent cache should be used */
 	private static boolean usePersistentCache;
 
 	/** Contains the most recently requested asset */
     private static Asset lastRetrievedAsset;
 
     /**
      * A list of listeners which should be notified
      * when the asset associated with a given MD5
      * sum has finished downloading. 
      */
     private static Map<MD5Key, List<AssetAvailableListener>> assetListenerListMap  = new ConcurrentHashMap<MD5Key, List<AssetAvailableListener>>();
     
     /** Property string associated with asset name */
     public static final String NAME = "name";
     
     /** Used to load assets from storage */
     private static AssetLoader assetLoader = new AssetLoader();
     
 	static {		
 		cacheDir = AppUtil.getAppHome("assetcache");
 		if (cacheDir != null) {
 			usePersistentCache = true;
 		}
 	}
 	
 	/**
 	 * Remove all existing repositories and
 	 * load all the repositories from the currently
 	 * loaded campaign.
 	 */
 	public static void updateRepositoryList() {
 		assetLoader.removeAllRepositories();
 		for (String repo : MapTool.getCampaign().getRemoteRepositoryList()) {
 			assetLoader.addRepository(repo);
 		}
 	}
 	
 	/**
 	 * Determine if the asset is currently being
 	 * requested. While an asset is being loaded
 	 * it will be marked as requested and this function
 	 * will return true. Once the asset is done loading
 	 * this function will return false and the asset will
 	 * be available from the cache.
 	 * @param key MD5Key of asset being requested
 	 * @return True if asset is currently being requested, false otherwise
 	 */
 	public static boolean isAssetRequested(MD5Key key) {
 		return assetLoader.isIdRequested(key);
 	}
 	
 	/**
 	 * Register a listener with the asset manager. The
 	 * listener will be notified when the asset is done
 	 * loading.
 	 * @param key MD5Key of the asset
 	 * @param listener Listener to notify when the asset is done loading
 	 */
 	public static void addAssetListener(MD5Key key, AssetAvailableListener... listeners) {
 
 		if (listeners == null || listeners.length == 0) {
 			return;
 		}
 		
 		List<AssetAvailableListener> listenerList = assetListenerListMap.get(key);
 		if (listenerList == null) {
 			listenerList = new LinkedList<AssetAvailableListener>();
 			assetListenerListMap.put(key, listenerList);
 		}
 
 		for (AssetAvailableListener listener : listeners) {
 			if (!listenerList.contains(listener)) {
 				listenerList.add(listener);
 			}
 		}
 	}
 	
 	public static void removeAssetListener(MD5Key key, AssetAvailableListener... listeners) {
 		
 		if (listeners == null || listeners.length == 0) {
 			return;
 		}
 		
 		List<AssetAvailableListener> listenerList = assetListenerListMap.get(key);
 		if (listenerList == null) {
 			// Nothing to do
 			return;
 		}
 
 		for (AssetAvailableListener listener : listeners) {
 			listenerList.remove(listener);
 		}
 	}
 	
 	/**
 	 * Load the last retrieved asset. This asset
 	 * may be empty if the asset is currently being
 	 * downloaded, the id will be valid but nothing else.
 	 * @return The last retrieved asset.
 	 */
     public static Asset getLastRetrievedAsset() {
         return lastRetrievedAsset;
     }
     
     /**
      * Determine if the asset manager has the asset.
      * This does not tell you if the asset is done
      * downloading.
      * @param asset Asset to look for
      * @return True if the asset exists, false otherwise
      */
 	public static boolean hasAsset(Asset asset) {
 		return hasAsset(asset.getId());
 	}
 	
 	/**
 	 * Determine if the asset manager has the asset.
 	 * This does not tell you if the asset is done
 	 * downloading.
 	 * @param key
 	 * @return
 	 */
 	public static boolean hasAsset(MD5Key key) {
 		return assetMap.containsKey(key) || assetIsInPersistentCache(key) || assetHasLocalReference(key);
 	}
 
 	/**
 	 * Determines if the asset data is in memory.
 	 * @param key MD5 sum associated with asset
 	 * @return True if hte asset is loaded, false otherwise
 	 */
     public static boolean hasAssetInMemory(MD5Key key) {
         return assetMap.containsKey(key);
     }
     
     /**
      * Add the asset to the asset cache.
      * Listeners for this asset are notified.
      * @param asset Asset to add to cache
      */
 	public static void putAsset(Asset asset) {
 		
 		if (asset == null) {
 			return;
 		}
 		
 		assetMap.put(asset.getId(), asset);
 
 		// Invalid images are represented by empty assets.  
 		// Don't persist those
 		if (asset.getImage().length > 0) {
 			putInPersistentCache(asset);
 		}
 
 		// Clear the waiting status
 		assetLoader.completeRequest(asset.getId());
 		
 		// Listeners
 		List<AssetAvailableListener> listenerList = assetListenerListMap.get(asset.getId());
 		if (listenerList != null) {
 			for (AssetAvailableListener listener : listenerList) {
 				listener.assetAvailable(asset.getId());
 			}
 			
 			assetListenerListMap.remove(asset.getId());
 		}
 	}
 	
 	/**
 	 * Get the asset from the cache. If the asset
 	 * is currently being downloaded then this function
 	 * returns an asset with only the id set, otherwise
 	 * the asset points to an asset that is in the cache.
 	 * Assets which are still downloading may be safely
 	 * used with the ImageManager, since it coordinates
 	 * with the asset manager.
 	 * @param id MD5 of the asset requested
 	 * @return Asset object for the MD5 sum
 	 */
 	public static Asset getAsset(MD5Key id, AssetAvailableListener... listeners) {
 		
 		if (id == null) {
 			return null;
 		}
 		
 		Asset asset = assetMap.get(id);
 		
 		if (asset == null && usePersistentCache && assetIsInPersistentCache(id)) {
 			// Guaranteed that asset is in the cache.
 			asset = getFromPersistentCache(id);
 		}
 		
 		if (asset == null && assetHasLocalReference(id)) {
 			
 			File imageFile = getLocalReference(id);
 
 			if (imageFile != null) {
 
 				try {
 					String name = FileUtil.getNameWithoutExtension(imageFile);
 					byte[] data = FileUtil.getBytes(imageFile);
 	
 					asset = new Asset(name, data);
 					
 					// Just to be sure the image didn't change
 					if (!asset.getId().equals(id)) {
 						throw new IOException ("Image reference did not match the requested image");
 					}
 					
 					// Put it in the persistent cache so we'll find it faster next time
 					putInPersistentCache(asset);
 				} catch (IOException ioe) {
 					// Log, but continue as if we didn't have a link
 					ioe.printStackTrace();
 				}
 			}
 		}
 		
 		// As a last resort we request the asset from the server
 		if (asset == null && !isAssetRequested(id)) {
			asset = new Asset(id);			

 			addAssetListener(id, listeners);
 			requestAssetFromServer(id);
 		}
 		
         lastRetrievedAsset = asset;
 		return asset;
 	}
 
 	/**
 	 * Remove the asset from the asset cache.
 	 * @param id MD5 of the asset to remove
 	 */
 	public static void removeAsset(MD5Key id) {
 		assetMap.remove(id);
 	}
 
 	/**
 	 * Enable the use of the persistent asset cache.
 	 * @param enable True to enable the cache, false to disable
 	 */
 	public static void setUsePersistentCache(boolean enable) {
 		if (enable && cacheDir == null) {
 			throw new IllegalArgumentException ("Could not enable persistent cache: no such directory");
 		}
 
 		usePersistentCache = enable;
 	}
 
 	/**
 	 * Request that the asset be loaded from the server
 	 * @param id MD5 of the asset to load from the server
 	 */
 	private static void requestAssetFromServer(MD5Key id) {
 		
 		if (id != null) {
 			assetLoader.requestAsset(id);
 		}
 	}
 	
 	/**
 	 * Retrieve the asset from the persistent cache.
 	 * If the asset is not in the cache, or loading
 	 * from the cache failed then this function
 	 * returns null.
 	 * @param id MD5 of the requested asset
 	 * @return Asset from the cache
 	 */
 	private static Asset getFromPersistentCache(MD5Key id) {
 		
 		if (id == null || id.toString().length() == 0) {
 			return null;
 		}
 		
 		if (!assetIsInPersistentCache(id)) {
 			return null;
 		}
 		
 		File assetFile = getAssetCacheFile(id);
 		
 		try {
 			byte[] data = FileUtil.loadFile(assetFile);
 			Properties props = getAssetInfo(id);
 			
 			Asset asset = new Asset(props.getProperty(NAME), data);
 			
 			if ( !asset.getId().equals(id)) {
 				System.err.println("MD5 for asset " + asset.getName() + " corrupted");
 			}
 			
 			assetMap.put(id, asset);
 			
 			return asset;
 		} catch (IOException ioe) {
 			System.err.println("Could not load asset from persistent cache: " + ioe);
 			return null;
 		}
 		
 	}
 	
 	/**
 	 * Create an asset from a file.
 	 * @param file File to use for asset
 	 * @return Asset associated with the file
 	 * @throws IOException
 	 */
 	public static Asset createAsset(File file) throws IOException {
 		return  new Asset(FileUtil.getNameWithoutExtension(file), FileUtil.loadFile(file));
     }
 	    
 	/**
 	 * Return a set of properties associated with the asset.
 	 * @param id MD5 of the asset
 	 * @return Properties object containing asset properties.
 	 */
 	public static Properties getAssetInfo(MD5Key id) {
 		
 		File infoFile = getAssetInfoFile(id);
 		try {
 
 			Properties props = new Properties();
 			InputStream is = new FileInputStream(infoFile); 
 			props.load(is);
 			is.close();
 			return props;
 			
 		} catch (Exception e) {
 			return new Properties();
 		}
 	}
 	
 	/**
 	 * Serialize the asset into the persistent cache.
 	 * @param asset Asset to serialize
 	 */
 	private static void putInPersistentCache(final Asset asset) {
 		
 		if (!usePersistentCache) {
 			return;
 		}
 		
 		if (!assetIsInPersistentCache(asset)) {
 			
 			final File assetFile = getAssetCacheFile(asset);
 			
                         new Thread() {
                             public void run() {
                                 
                                 try {
                                         assetFile.getParentFile().mkdirs();
                                         // Image
                                         OutputStream out = new FileOutputStream(assetFile);
                                         out.write(asset.getImage());
                                         out.close();
 
                                 } catch (IOException ioe) {
                                         System.err.println("Could not persist asset: " + ioe);
                                         return;
                                 }
                             }
                         }.start();
 			
 		}
 		if (!assetInfoIsInPersistentCache(asset)) {
 
 			File infoFile = getAssetInfoFile(asset);
 			
 			try {
 				// Info
 				OutputStream out = new FileOutputStream(infoFile);
 				Properties props = new Properties();
 				props.put (NAME, asset.getName() != null ? asset.getName() : "");
 				props.store(out, "Asset Info");
 				out.close();
 				
 			} catch (IOException ioe) {
 				System.err.println("Could not persist asset: " + ioe);
 				return;
 			}
 			
 		}
 	}
 	
 	/**
 	 * Return the file associated with the asset, if any.
 	 * @param id MD5 of the asset
 	 * @return The file associated with the asset, null if none.
 	 */
 	private static File getLocalReference(MD5Key id) {
 
 		File lnkFile = getAssetLinkFile(id);
 		if (!lnkFile.exists()) {
 			return null;
 		}
 		try {
 			List<String> refList = FileUtil.getLines(lnkFile);
 			
 			for (String ref : refList) {
 				File refFile = new File(ref);
 				if (refFile.exists()) {
 					return refFile;
 				}
 			}
 			
 		} catch (IOException ioe) {
 			// Just so we know, but fall through to return null
 			ioe.printStackTrace();
 		}
 		
 		// Guess we don't have one
 		return null;
 	}
 	
 	/**
 	 * Store an absolute path to where this asset exists.
 	 * @param image 
 	 */
 	public static void rememberLocalImageReference(File image) throws IOException {
 		
 		MD5Key id = new MD5Key(new BufferedInputStream(new FileInputStream(image)));
 		File lnkFile = getAssetLinkFile(id);
 
 		// See if we know about this one already
 		if (lnkFile.exists()) {
 			
 			List<String> referenceList = FileUtil.getLines(lnkFile);
 			for (String ref : referenceList) {
 				if (ref.equals(id.toString())) {
 					
 					// We already know about this one
 					return;
 				}
 			}
 		}
 		
 		// Keep track of this reference
 		FileOutputStream out = new FileOutputStream(lnkFile, true); // For appending
 
 		out.write((image.getAbsolutePath() + "\n").getBytes());
 		
 		out.close();
 	}
 
 	/**
 	 * Determine if the asset has a local reference
 	 * @param id MD5 sum of the asset
 	 * @return True if there is a local reference, false otherwise
 	 */
 	private static boolean assetHasLocalReference(MD5Key id) {
 		
 		return getLocalReference(id) != null;
 	}
 	
 	/**
 	 * Determine if the asset is in the persistent cache.
 	 * @param asset Asset to search for
 	 * @return True if asset is in the persistent cache, false otherwise
 	 */
 	private static boolean assetIsInPersistentCache(Asset asset) {
 		return assetIsInPersistentCache(asset.getId());
 	}
 	
 	/**
 	 * The assets information is in the persistent cache.
 	 * @param asset Asset to search for
 	 * @return True if the assets information exists in the persistent cache
 	 */
 	private static boolean assetInfoIsInPersistentCache(Asset asset) {
 		return getAssetInfoFile(asset.getId()).exists();
 	}
 	
 	/**
 	 * Determine if the asset is in the persistent cache.
 	 * @param id MD5 sum of the asset
 	 * @return True if asset is in the persistent cache, false otherwise
 	 * @see assetIsInPersistentCache(Asset asset)
 	 */
 	private static boolean assetIsInPersistentCache(MD5Key id) {
 
 		return getAssetCacheFile(id).exists() && getAssetCacheFile(id).length() > 0;
 	}
 	
 	/**
 	 * Return the assets cache file, if any
 	 * @param asset Asset to search for
 	 * @return The assets cache file, or null if it doesn't have one
 	 */
 	public static File getAssetCacheFile(Asset asset) {
 		return getAssetCacheFile(asset.getId());
 	}
 
 	/**
 	 * Return the assets cache file, if any
 	 * @param is MD5 sum of the asset
 	 * @return The assets cache file, or null if it doesn't have one
 	 * @see getAssetCacheFile(Asset asset)
 	 */
 	public static File getAssetCacheFile(MD5Key id) {
 		return new File (cacheDir.getAbsolutePath() + File.separator + id);
 	}
 	
 	/**
 	 * Return the asset info file, if any
 	 * @param asset Asset to search for
 	 * @return The assets info file, or null if it doesn't have one
 	 */
 	private static File getAssetInfoFile(Asset asset) {
 		return getAssetInfoFile(asset.getId());
 	}
 
 	/**
 	 * Return the asset info file, if any
 	 * @param id MD5 sum of the asset
 	 * @return File - The assets info file, or null if it doesn't have one
 	 * @see getAssetInfoFile(Asset asset)
 	 */
 	private static File getAssetInfoFile(MD5Key id) {
 		return new File (cacheDir.getAbsolutePath() + File.separator + id + ".info");
 	}
 
 	/**
 	 * Return the asset link file, if any
 	 * @param id MD5 sum of the asset
 	 * @return File The asset link file
 	 */
 	private static File getAssetLinkFile(MD5Key id) {
 		return new File (cacheDir.getAbsolutePath() + File.separator + id + ".lnk");
 	}
 
 	/**
 	 * Recursively search from the rootDir, filtering files
 	 * based on fileFilter, and store a reference to every
 	 * file seen.
 	 * @param rootDir Starting directory to recurse from
 	 * @param fileFilter Only add references to image files that are allowed by the filter
 	 */
 	public static void searchForImageReferences(File rootDir, FilenameFilter fileFilter) {
 
 		for (File file : rootDir.listFiles()) {
 			
 			if (file.isDirectory()) {
 				searchForImageReferences(file, fileFilter);
 				continue;
 			}
 			
 			try {
 				if (fileFilter.accept(rootDir, file.getName())) {
 					if (MapTool.getFrame() != null) {
 						MapTool.getFrame().setStatusMessage("Storing local image reference: " + file.getName());
 					}
 					rememberLocalImageReference(file);
 				}
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 		}
 
 		// Done
 		if (MapTool.getFrame() != null) {
 			MapTool.getFrame().setStatusMessage("");
 		}
 	}
 
 	/**
 	 * <p>
 	 * This method accepts the name of a repository (as it appears in the
 	 * CampaignProperties) and updates it by adding the additional mappings that are in
 	 * <code>add</code>.
 	 * </p>
 	 * <p>
 	 * This method first retrieves the mapping from the AssetLoader.  It then adds in the
 	 * new assets.  Last, it has to create the new index file.  The index file should be stored
 	 * in the local repository cache.  Note that this function <b>does not</b> update
 	 * the original (network storage) repository location. 
 	 * </p>
 	 * <p>
 	 * If the calling function does not update the network storage for <b>index.gz</b>,
 	 * a restart of MapTool will lose the information when the index is downloaded again.
 	 * </p>
 	 * @param repo name of the repository to update
 	 * @param add entries to add to the repository
 	 * @return the contents of the new repository in uploadable format
 	 */
 	public static byte[] updateRepositoryMap(String repo, Map<String, String> add) {
 		Map<String, String> repoMap = assetLoader.getRepositoryMap(repo);
 		repoMap.putAll(add);
 		byte[] index = assetLoader.createIndexFile(repo);
 		try {
 			assetLoader.storeIndexFile(repo, index);
 		} catch (IOException e) {
 			System.err.println("Couldn't save updated index to local repository cache: " + e);
 			e.printStackTrace();
 		}
 		return index;
 	}
 
 	/**
 	 * <p>
 	 * Constructs a set of all assets in the given list of repositories, then builds a map of
 	 * <code>MD5Key</code> and <code>Asset</code> for all assets that do not
 	 * appear in that set.
 	 * </p>
 	 * <p>
 	 * This provides the calling function with a list of all assets currently in use by the
 	 * campaign that do not appear in one of the listed repositories.  It's entirely possible that
 	 * the asset is in a different repository or in none at all.
 	 * </p>
 	 * @param repos list of repositories to exclude
 	 * @return Map of all known assets that are NOT in the specified repositories
 	 */
 	public static Map<MD5Key, Asset> findAllAssetsNotInRepositories(List<String> repos) {
 		// For performance reasons, we calculate the size of the Set in advance...
 		int size = 0;
 		for (String repo : repos) {
 			size += assetLoader.getRepositoryMap(repo).size();
 		}
 
 		// Now create the aggregate of all repositories.
 		Set<String> aggregate = new HashSet<String>(size);
 		for (String repo : repos) {
 			aggregate.addAll(assetLoader.getRepositoryMap(repo).keySet());
 		}
 
 		/*
 		 * The 'aggregate' now holds the sum total of all asset keys that are in repositories.
 		 * Now we go through the 'assetMap' and copy over <K,V> pairs that are NOT in
 		 * 'aggregate' to our 'missing' Map.
 		 * 
 		 * Unfortunately, the repository is a Map<String, String> while the return value is
 		 * going to be a Map<MD5Key, Asset>, which means each individual entry needs to
 		 * be checked and references copied.  If both were the same data type, converting
 		 * both to Set<String> would allow for an addAll() and removeAll() and be done with
 		 * it!
 		 */
 		Map<MD5Key, Asset> missing = new HashMap<MD5Key, Asset>(Math.min(assetMap.size(), aggregate.size()));
 		for (MD5Key key : assetMap.keySet()) {
 			if (aggregate.contains(key) == false)	// Not in any repository so add it.
 				missing.put(key, assetMap.get(key));
 		}
 		return missing;
 	}
 }
