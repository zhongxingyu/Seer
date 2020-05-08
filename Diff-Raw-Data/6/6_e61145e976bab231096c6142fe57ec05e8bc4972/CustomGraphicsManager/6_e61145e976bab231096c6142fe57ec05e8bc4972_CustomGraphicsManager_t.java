 package cytoscape.visual.customgraphic;
 
 import java.awt.image.BufferedImage;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.concurrent.CompletionService;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorCompletionService;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import cytoscape.Cytoscape;
 import cytoscape.CytoscapeInit;
 import cytoscape.actions.ShowCustomGraphicsManagerAction;
 import cytoscape.logger.CyLogger;
 import cytoscape.task.ui.JTaskConfig;
 import cytoscape.task.util.TaskManager;
 import cytoscape.visual.SubjectBase;
 import cytoscape.visual.customgraphic.impl.AbstractDCustomGraphics;
 import cytoscape.visual.customgraphic.impl.bitmap.URLImageCustomGraphics;
 import cytoscape.visual.customgraphic.impl.vector.GradientOvalLayer;
 import cytoscape.visual.customgraphic.impl.vector.GradientRoundRectangleLayer;
 
 public class CustomGraphicsManager extends SubjectBase implements
 		PropertyChangeListener {
 
 	private static final CyLogger logger = CyLogger.getLogger();
 
 	private static final int TIMEOUT = 1000;
 	private static final int NUM_THREADS = 8;
 
 	private static final String IMAGE_DIR_NAME = "images";
 
 	// For image I/O, PNG is used as bitmap image format.
 	private static final String IMAGE_EXT = "png";
 
 	private final ExecutorService imageLoaderService;
 
 	private final Map<Long, CyCustomGraphics> graphicsMap = new ConcurrentHashMap<Long, CyCustomGraphics>();
 
 	// URL to hash code map. For images associated with URL.
 	private final Map<URL, Long> sourceMap = new ConcurrentHashMap<URL, Long>();
 
 	// Null Object
 	private static final CyCustomGraphics NULL = NullCustomGraphics
 			.getNullObject();
 
 	// Default vectors
 	private static final Class<?>[] DEF_VECTORS = {
 			GradientRoundRectangleLayer.class, GradientOvalLayer.class };
 
 	public static final String METADATA_FILE = "image_metadata.props";
 
 	private File imageHomeDirectory;
 	
 	private final Map<CyCustomGraphics,Boolean> isUsedCustomGraphics;
 
 	/**
 	 * Creates an image pool object and restore existing images from user
 	 * resource directory.
 	 */
 	public CustomGraphicsManager() {
 		
 		this.isUsedCustomGraphics = new HashMap<CyCustomGraphics, Boolean>();
		this.imageHomeDirectory = new File(CytoscapeInit.getConfigDirectory(), IMAGE_DIR_NAME);
 
 		// For loading images in parallel.
 		this.imageLoaderService = Executors.newFixedThreadPool(NUM_THREADS);
 
 		graphicsMap.put(NULL.getIdentifier(), NULL);
 		this.isUsedCustomGraphics.put(NULL, false);
 		restoreDefaultVectorImageObjects();
 
 		Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(
 				Cytoscape.CYTOSCAPE_EXIT, this);
 
 		//restoreImages();
 	}
 
 	/**
 	 * Restore images from .cytoscape/images dir.
 	 */
 	public void restoreImages() {
 		final CompletionService<BufferedImage> cs = new ExecutorCompletionService<BufferedImage>(
 				imageLoaderService);
 
 		imageHomeDirectory.mkdir();
 
 		long startTime = System.currentTimeMillis();
 
 		// Load metadata first.
 		final Properties prop = new Properties();
 		try {
 			prop.load(new FileInputStream(new File(imageHomeDirectory,
 					METADATA_FILE)));
 			logger.info("Custom Graphics Image property file loaded from: "
 					+ imageHomeDirectory);
 		} catch (Exception e) {
 			logger.warning("Custom Graphics Metadata was not found.  This is normal for the first time.");
 			// Restore process is not necessary.
 			return;
 		}
 
 		if (this.imageHomeDirectory != null && imageHomeDirectory.isDirectory()) {
 			final File[] imageFiles = imageHomeDirectory.listFiles();
 			final Map<Future<BufferedImage>, String> fMap = new HashMap<Future<BufferedImage>, String>();
 			final Map<Future<BufferedImage>, Set<String>> metatagMap = new HashMap<Future<BufferedImage>, Set<String>>();
 			try {
 				for (File file : imageFiles) {
 					if (file.toString().endsWith(IMAGE_EXT) == false)
 						continue;
 
 					final String fileName = file.getName();
 					final String key = fileName.split("\\.")[0];
 					final String value = prop.getProperty(key);
 
 					final String[] imageProps = value.split(",");
 					if (imageProps == null || imageProps.length < 2)
 						continue;
 
 					String name = imageProps[2];
 					if (name.contains("___"))
 						name = name.replace("___", ",");
 
 					Future<BufferedImage> f = cs.submit(new LoadImageTask(file
 							.toURI().toURL()));
 					fMap.put(f, name);
 
 					String tagStr = null;
 					if (imageProps.length > 3) {
 						tagStr = imageProps[3];
 						final Set<String> tags = new TreeSet<String>();
 						String[] tagParts = tagStr.split("\\"
 								+ AbstractDCustomGraphics.LIST_DELIMITER);
 						for (String tag : tagParts)
 							tags.add(tag.trim());
 
 						metatagMap.put(f, tags);
 					}
 				}
 				for (File file : imageFiles) {
 					if (file.toString().endsWith(IMAGE_EXT) == false)
 						continue;
 					final Future<BufferedImage> f = cs.take();
 					final BufferedImage image = f.get();
 					if (image == null)
 						continue;
 
 					final CyCustomGraphics cg = new URLImageCustomGraphics(
 							fMap.get(f), image);
 					if (cg instanceof Taggable && metatagMap.get(f) != null)
 						((Taggable) cg).getTags().addAll(metatagMap.get(f));
 
 					graphicsMap.put(cg.getIdentifier(), cg);
 					this.isUsedCustomGraphics.put(cg, false);
 
 					try {
 						final URL source = new URL(fMap.get(f));
 						if (source != null)
 							sourceMap.put(source, cg.getIdentifier());
 					} catch (MalformedURLException me) {
 						continue;
 					}
 				}
 
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				e.printStackTrace();
 			}
 		}
 
 		try {
 			imageLoaderService.shutdown();
 			imageLoaderService.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		long endTime = System.currentTimeMillis();
 		double sec = (endTime - startTime) / (1000.0);
 		logger.info("Image loading process finished in " + sec + " sec.");
 		logger.info("Currently,  " + (graphicsMap.size() - 1)
 				+ " images are available.");
 	}
 
 	private void restoreDefaultVectorImageObjects() {
 
 		for (Class<?> cls : DEF_VECTORS) {
 
 			try {
 				Object obj = cls.newInstance();
 				if (obj instanceof CyCustomGraphics) {
 					graphicsMap.put( ((CyCustomGraphics) obj).getIdentifier(), (CyCustomGraphics) obj);
 					this.isUsedCustomGraphics.put((CyCustomGraphics) obj, false);
 				}
 			} catch (InstantiationException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 	}
 
 	/**
 	 * Add a custom graphics to current session.
 	 * 
 	 * @param hash
 	 *            : Hasn code of image object
 	 * @param graphics
 	 *            : Actual custom graphics object
 	 * @param source
 	 *            : Source URL of graphics (if exists. Can be null)
 	 */
 	public void addGraphics(final CyCustomGraphics graphics, final URL source) {
 		if (graphics == null)
 			throw new IllegalArgumentException(
 					"Custom Graphics and its ID should not be null.");
 
 		// Souce URL is an optional field.
 		if (source != null)
 			sourceMap.put(source, graphics.getIdentifier());
 
 		graphicsMap.put(graphics.getIdentifier(), graphics);
 		this.isUsedCustomGraphics.put(graphics, false);
 	}
 
 	/**
 	 * Remove graphics from current session (memory).
 	 * 
 	 * @param id
 	 *            : ID of graphics (hash code)
 	 */
 	public void removeGraphics(final Long id) {
 		final CyCustomGraphics cg = graphicsMap.get(id);
 		if (cg != null && cg != NULL) {
 			graphicsMap.remove(id);
 			this.isUsedCustomGraphics.remove(cg);
 		}
 	}
 
 	/**
 	 * Get a Custom Graphics by integer ID.
 	 * 
 	 * @param hash
 	 *            Hash code of Custom Graphics object
 	 * 
 	 * @return Custom Graphics if exists. Otherwise, null.
 	 * 
 	 */
 	public CyCustomGraphics getByID(Long id) {
 		return graphicsMap.get(id);
 	}
 
 	/**
 	 * Get Custom Graphics by source URL. Images without source cannot be
 	 * retreved by this method.
 	 * 
 	 * @param sourceURL
 	 * @return
 	 */
 	public CyCustomGraphics getBySourceURL(URL sourceURL) {
 		final Long id = sourceMap.get(sourceURL);
 		if (id != null)
 			return graphicsMap.get(id);
 		else
 			return null;
 	}
 
 	/**
 	 * Get a collection of all Custom Graphics in current session.
 	 * 
 	 * @return
 	 */
 	public Collection<CyCustomGraphics> getAll() {
 		return graphicsMap.values();
 	}
 
 	/**
 	 * Remove all custom graphics from memory.
 	 */
 	public void removeAll() {
 		this.graphicsMap.clear();
 		this.sourceMap.clear();
 		this.isUsedCustomGraphics.clear();
 
 		// Null Graphics should not be removed.
 		this.graphicsMap.put(NULL.getIdentifier(), NULL);
 
 	}
 
 	/**
 	 * Convert current list of custom graphics into Property object.
 	 * 
 	 * @return
 	 */
 	public Properties getMetadata() {
 		// Null graphics object should not be in this property.
 		graphicsMap.remove(NULL.getIdentifier());
 
 		final Properties props = new Properties();
 		// Use hash code as the key, and value will be a string returned by
 		// toString() method.
 		// This means all CyCustomGraphics implementations should have a special
 		// toString method.
 		for (final CyCustomGraphics graphics : graphicsMap.values())
 			props.setProperty(graphics.getIdentifier().toString(),
 					graphics.toString());
 		graphicsMap.put(NULL.getIdentifier(), NULL);
 		return props;
 	}
 
 	/**
 	 * Save images to local disk when exiting from Cytoscape.
 	 */
 	public void propertyChange(PropertyChangeEvent evt) {
 		// Persist images
 		logger.info("Saving images to: " + imageHomeDirectory);
 
 		// Create Task
 		final PersistImageTask task = new PersistImageTask(imageHomeDirectory);
 
 		// Configure JTask Dialog Pop-Up Box
 		final JTaskConfig jTaskConfig = new JTaskConfig();
 
 		jTaskConfig.displayCancelButton(false);
 		jTaskConfig.setOwner(Cytoscape.getDesktop());
 		jTaskConfig.displayCloseButton(false);
 		jTaskConfig.displayStatus(true);
 		jTaskConfig.setAutoDispose(true);
 
 		// Execute Task in New Thread; pop open JTask Dialog Box.
 		TaskManager.executeTask(task, jTaskConfig);
 
 		logger.info("Image saving process finished.");
 	}
 
 	public SortedSet<Long> getIDSet() {
 		return new TreeSet<Long>(graphicsMap.keySet());
 	}
 	
 	public Boolean isUsedInCurrentSession(final CyCustomGraphics graphics) {
 		if(graphics == null)
 			throw new NullPointerException("CyCustomGraphics cannot be null.");
 		if(this.isUsedCustomGraphics.containsKey(graphics) ==false)
 			throw new IllegalArgumentException("No such CyCustomGraphics: " + graphics.getDisplayName());
 
 		return isUsedCustomGraphics.get(graphics);
 	}
 	
 	public void setUsedInCurrentSession(final CyCustomGraphics graphics, final Boolean isUsed) {
 		if(isUsed == null || graphics == null)
 			throw new NullPointerException("Parameters cannot be null.");
 		
 		if(this.isUsedCustomGraphics.containsKey(graphics) == false)
 			throw new IllegalArgumentException("No such custom graphics object: " + graphics.getDisplayName());
 		
 		this.isUsedCustomGraphics.put(graphics, isUsed);
 	}
 }
