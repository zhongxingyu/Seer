 package cytoscape.visual.customgraphic;
 
 import java.awt.image.BufferedImage;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
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
 import cytoscape.task.ui.JTaskConfig;
 import cytoscape.task.util.TaskManager;
 import cytoscape.visual.SubjectBase;
 import cytoscape.visual.customgraphic.experimental.GradientRectangleCustomGraphics;
 
 public class CustomGraphicsPool extends SubjectBase implements
 		PropertyChangeListener {
 
 	private static final int TIMEOUT = 1000;
 	private static final int NUM_THREADS = 8;
 
 	private final ExecutorService imageLoaderService;
 
 	private final Map<Integer, CyCustomGraphics<?>> graphicsMap = new ConcurrentHashMap<Integer, CyCustomGraphics<?>>();
 	private final Map<URL, Integer> sourceMap = new ConcurrentHashMap<URL, Integer>();
 	
 
 	// Null Object
 	private static final CyCustomGraphics<?> NULL = new NullCustomGraphics();
 	private static final CyCustomGraphics<?> GR = new GradientRectangleCustomGraphics();
 
 	public static final String METADATA_FILE = "image_metadata.props";
 
 	private File imageHomeDirectory;
 
 	public CustomGraphicsPool() {
 		// For loading images in parallel.
 		this.imageLoaderService = Executors.newFixedThreadPool(NUM_THREADS);
 
 		graphicsMap.put(NULL.hashCode(), NULL);
 		graphicsMap.put(GR.hashCode(), GR);
 
 		Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(
 				Cytoscape.CYTOSCAPE_EXIT, this);
 
 		restoreImages();
 	}
 
 	
 	/**
 	 * Restore images from .cytoscape dir.
 	 */
 	private void restoreImages() {
 		final CompletionService<BufferedImage> cs = new ExecutorCompletionService<BufferedImage>(
 				imageLoaderService);
 
 		// User config directory
 		this.imageHomeDirectory = new File(CytoscapeInit.getConfigDirectory(),
 				"images");
 
 		imageHomeDirectory.mkdir();
 
 		long startTime = System.currentTimeMillis();
 
 		final Properties prop = new Properties();
 		try {
 			prop.load(new FileInputStream(new File(imageHomeDirectory,
 					METADATA_FILE)));
 			System.out.println("Image prop loaded!");
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 			System.out.println("Image metadata not found!");
 			return;
 		} catch (IOException e1) {
 			e1.printStackTrace();
 			System.out.println("Image metadata not found!");
 			return;
 		}
 
 		if (this.imageHomeDirectory != null && imageHomeDirectory.isDirectory()) {
 			final File[] imageFiles = imageHomeDirectory.listFiles();
 			final Map<Future<BufferedImage>, String> fMap = new HashMap<Future<BufferedImage>, String>();
 			try {
 				for (File file : imageFiles) {
 					if (file.toString().endsWith("png") == false)
 						continue;
 					
 					System.out.println("Before Procsessing: " +  file.getName());
 					final String fileName = file.getName();
 					final String key = fileName.split("\\.")[0];
 					final String value = prop.getProperty(key);
 					final String name = value.split(",")[2];
 					System.out.println("Procsessing: " +  fileName + ", " + name);
 					
 					Future<BufferedImage> f = cs.submit(new LoadImageTask(file.toURI().toURL()));
 					fMap.put(f, name);
 				}
 				for (File file : imageFiles) {
 					if (file.toString().endsWith("png") == false)
 						continue;
 					final Future<BufferedImage> f = cs.take();
 					final BufferedImage image = f.get();
 					if (image == null)
 						continue;
 					
 					final CyCustomGraphics<?> cg = new URLImageCustomGraphics(fMap.get(f), image);
 					graphicsMap.put(cg.hashCode(), cg );
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
 		System.out.println("Image Loading Finished in " + sec + " sec.");
 
 	}
 
 	public void addGraphics(Integer hash, CyCustomGraphics<?> graphics) {
 		graphicsMap.put(hash, graphics);
 		this.fireStateChanged();
 	}
 	
 	
 	public void addGraphics(Integer hash, CyCustomGraphics<?> graphics, URL source) {
 		sourceMap.put(source, hash);
 		addGraphics(hash, graphics);
 	}
 
 	public void removeGraphics(Integer id) {
 		graphicsMap.remove(id);
 	}
 
 	public CyCustomGraphics<?> get(Integer hash) {
 		return graphicsMap.get(hash);
 	}
 	
 	public CyCustomGraphics<?> get(URL sourceURL) {
 		if(sourceMap.get(sourceURL) != null)
 			return graphicsMap.get(sourceMap.get(sourceURL));
 		else
 			return null;
 	}
 
 	public Collection<CyCustomGraphics<?>> getAll() {
 		return graphicsMap.values();
 	}
 	
 	public void removeAll() {
 		this.graphicsMap.clear();
 	}
 
 	public CyCustomGraphics<?> getNullGraphics() {
 		return NULL;
 	}
 	
 	public Properties getMetadata() {
 		graphicsMap.remove(NULL.hashCode());
 		final Properties props = new Properties();
 		for(final CyCustomGraphics<?> graphics : graphicsMap.values())
 			props.setProperty(Integer.toString(graphics.hashCode()), graphics.toString());
 		graphicsMap.put(NULL.hashCode(), NULL);
 		return props; 
 	}
 	
 	public File getImageFileLocation() {
 		return this.imageHomeDirectory;
 	}
 
 
 	public void propertyChange(PropertyChangeEvent evt) {
 		// Persist images
 		System.out.println("Saving images...");
 
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
 
		System.out.println("Saving finished");
 	}
 }
