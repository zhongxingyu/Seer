/*-
  * Copyright 2011 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.io;
 
 import gda.analysis.io.ScanFileHolderException;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.lang.ref.Reference;
 import java.lang.ref.SoftReference;
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.ZipInputStream;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.Activator;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.LazyDataset;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 import uk.ac.diamond.scisoft.analysis.utils.FileUtils;
 // TODO Not sure if org.eclipse.core could break GDA server.
 // Been told verbally that the GDA server now can resolve core and resources.
 
 /**
  * A class which gives a single point of entry to loading data files
  * into the system.
  * 
  * In order to work with the factory a loader must have:
  * 1. a no argument constructor
  * 2. a setFile(...) method with a string path argument
  * 
  * *OR*
  * 
  * A constructor with a string argument.
  * 
  * In order to work well the loader should implement:
  * 
  * 1. IMetaLoader - this interface marks it possible to extract dataset names and other meta
  *    data without reading all the file data into memory.
  *    
  * 2. IDataSetLoader to load a single data set without loading the rest of the file.
  * 
  * see LoaderFactoryExtensions which boots up the extensions from reading the extension points.
  * 
  * This class is going to be @Deprecated please use ILoaderService where possible.
  * <code>
  * 
   	final ILoaderService service = (ILoaderService)ServiceManager.getService(ILoaderService.class);
   	// ServiceManager may need to be configured using Spring on GDA server
   	// service can now be used as if it was a 'LoaderFactory' instance.
     
  */
 public class LoaderFactory {
 	/**
 	 * A caching mechanism using soft references. Soft references attempt to keep things
 	 * in memory until the system is short on memory. Hashtable used because it is synchronized
 	 * which should reduce chances of getting the wrong data for the key.
 	 */
 	private static final Map<LoaderKey, Reference<IDataAnalysisObject>> SOFT_CACHE = new Hashtable<LoaderKey, Reference<IDataAnalysisObject>>(89);
 	
 	/**
 	 * This method may be called to ensure that the soft reference cache of data is
 	 * empty. It is required from the unit tests which attempt to measure memory
 	 * leaks, which otherwise would measure the "leak" of the soft reference cache.
 	 */
 	public static void clear() {
 		SOFT_CACHE.clear();
 	}
 
 	private static final Logger logger = LoggerFactory.getLogger(LoaderFactory.class);
 
 	private static final Map<String, List<Class<? extends AbstractFileLoader>>> LOADERS = new HashMap<String, List<Class<? extends AbstractFileLoader>>>(19);
 	private static final Map<String, Class<? extends java.io.InputStream>>     UNZIPERS = new HashMap<String, Class<? extends java.io.InputStream>>(3);
 
 	/**
 	 * 
 	 * Loaders can be registered at run time using registerLoader(...)
 	 * 
 	 * There is no need for an extension point now and no dependency on eclipse.
 	 * Instead an OSGI service contributing the loaders is looked for.
 	 * 
 	 * To change a loader programmatically (not advised)
 	 * 
 	 * 1. LoaderFactory.getSupportedExtensions();
 	 * 2. LoaderFactory.clearLoader("h5");
 	 * 3. LoaderFactory.registerLoader("h5", MyH5ClassThatIsBetter.class);
 	 * 
 	 */
 	static {
 		try {
 		    LoaderFactory.registerLoader("npy",  NumPyFileLoader.class);
 		    LoaderFactory.registerLoader("img",  ADSCImageLoader.class);
 		    LoaderFactory.registerLoader("osc",  RAxisImageLoader.class);
 		    LoaderFactory.registerLoader("cbf",  CBFLoader.class);
 		    LoaderFactory.registerLoader("img",  CrysalisLoader.class);
 			LoaderFactory.registerLoader("tif",  PixiumLoader.class);
 		    LoaderFactory.registerLoader("jpeg", JPEGLoader.class);
 		    LoaderFactory.registerLoader("jpg",  JPEGLoader.class);
 		    LoaderFactory.registerLoader("mccd", MARLoader.class);
 		    
 		    // There is some disagreement about the proper nexus/hdf5 
 		    // file extension at different facilities
 		    LoaderFactory.registerLoader("nxs",  NexusHDF5Loader.class);
 		    LoaderFactory.registerLoader("nexus",NexusHDF5Loader.class);
 		    LoaderFactory.registerLoader("h5",   HDF5Loader.class);
 		    LoaderFactory.registerLoader("hdf",  HDF5Loader.class);
 		    LoaderFactory.registerLoader("hdf5", HDF5Loader.class);
 		    LoaderFactory.registerLoader("hd5",  HDF5Loader.class);
 		    LoaderFactory.registerLoader("mat",  HDF5Loader.class);
 		    
 		    LoaderFactory.registerLoader("tif",  PilatusTiffLoader.class);
 		    LoaderFactory.registerLoader("png",  PNGLoader.class);
 		    LoaderFactory.registerLoader("raw",  RawBinaryLoader.class);
 		    LoaderFactory.registerLoader("srs",  ExtendedSRSLoader.class);
 		    LoaderFactory.registerLoader("srs",  SRSLoader.class);
 		    LoaderFactory.registerLoader("dat",  DatLoader.class);
 		    LoaderFactory.registerLoader("dat",  ExtendedSRSLoader.class);
 		    LoaderFactory.registerLoader("dat",  SRSLoader.class);
 		    LoaderFactory.registerLoader("csv",  EpicsCSVLoader.class);
 		    LoaderFactory.registerLoader("txt",  DatLoader.class);
 		    LoaderFactory.registerLoader("txt",  SRSLoader.class);
 		    LoaderFactory.registerLoader("txt",  RawTextLoader.class);
 		    LoaderFactory.registerLoader("mca",  DatLoader.class);
 		    LoaderFactory.registerLoader("mca",  SRSLoader.class);
 		    LoaderFactory.registerLoader("mca",  RawTextLoader.class);
 		    LoaderFactory.registerLoader("tif",  TIFFImageLoader.class);		    
 		    LoaderFactory.registerLoader("tiff", TIFFImageLoader.class);		    
 		    LoaderFactory.registerLoader("zip",  XMapLoader.class);
 		    LoaderFactory.registerLoader("edf",  PilatusEdfLoader.class);
 		    LoaderFactory.registerLoader("pgm",  PgmLoader.class);
 		    LoaderFactory.registerLoader("f2d",  Fit2DLoader.class);
 		    LoaderFactory.registerLoader("msk",  Fit2DMaskLoader.class);
 		    
 		    LoaderFactory.registerUnzip("gz",  GZIPInputStream.class);
 		    LoaderFactory.registerUnzip("zip", ZipInputStream.class);
 		    LoaderFactory.registerUnzip("bz2", CBZip2InputStream.class);
 		    	
 		    try {
 			    /**
 			     * Tell the extension points to load in.
 			     */
 			    final ILoaderFactoryExtensionService service = (ILoaderFactoryExtensionService) Activator.getService(ILoaderFactoryExtensionService.class);
 			    if (service!=null) service.registerExtensionPoints();
 		    } catch (Throwable t) {
 				logger.error("Problem getting extension service");
 		    }
 		} catch (Exception ne) {
 			logger.error("Cannot register loader - ALL loader registration aborted!", ne);
 		}
 	}
 
 	/**
 	 * This method is used to define the supported extensions that the LoaderFactory
 	 * already knows about.
 	 * 
 	 * NOTE that is can be called from Jython. It is probably not used in the GDA/SDA 
 	 * code based that much but external code like Jython can 
 	 * 
 	 * @return collection of extensions.
 	 */
 	public static Collection<String> getSupportedExtensions() {
 		return LOADERS.keySet();
 	}
 
 
 	/**
 	 * Call to load any file type into memory. By default loads all data sets, therefore
 	 * could take a **long time**.
 	 * 
 	 * In addition to find out if a given file loads with a particular loader - it actually 
 	 * LOADS it. 
 	 * 
 	 * Therefore it can take a while to run depending on how quickly the loader
 	 * fails. Also if there are many loaders called in turn, much memory could be consumed and
 	 * discarded. For this reason the registration process requires a file extension and tries 
 	 * all the loaders for a given extension if the extension is registered already. 
 	 * Otherwise it tries all loaders - in no particular order.
 	 * 
 	 * @param path
 	 * @return DataHolder
 	 * @throws Exception
 	 */
 	public static DataHolder getData(final String path) throws Exception {
 		return getData(path, true, new IMonitor.Stub() {
 			
 			@Override
 			public void worked(int amount) {
 				// Deliberately empty
 			}
 			
 			@Override
 			public boolean isCancelled() {
 				return false;
 			}
 		});
 	}
 	
 	
 	/**
 	 * Call to load any file type into memory. By default loads all data sets, therefore
 	 * could take a **long time**.
 	 * 
 	 * In addition to find out if a given file loads with a particular loader - it actually 
 	 * LOADS it. 
 	 * 
 	 * Therefore it can take a while to run depending on how quickly the loader
 	 * fails. Also if there are many loaders called in turn, much memory could be consumed and
 	 * discarded. For this reason the registration process requires a file extension and tries 
 	 * all the loaders for a given extension if the extension is registered already. 
 	 * Otherwise it tries all loaders - in no particular order.
 	 * 
 	 * @param path
 	 * @param mon
 	 * @return DataHolder
 	 * @throws Exception
 	 */
 	public static DataHolder getData(final String path, final IMonitor mon) throws Exception {
 		return getData(path, true, mon);
 	}
 
 	/**
 	 * Call to load any file type into memory. By default loads all data sets, therefore
 	 * could take a **long time**.
 	 * 
 	 * In addition to find out if a given file loads with a particular loader - it actually 
 	 * LOADS it. 
 	 * 
 	 * Therefore it can take a while to run depending on how quickly the loader
 	 * fails. Also if there are many loaders called in turn, much memory could be consumed and
 	 * discarded. For this reason the registration process requires a file extension and tries 
 	 * all the loaders for a given extension if the extension is registered already. 
 	 * Otherwise it tries all loaders - in no particular order.
 	 * 
 	 * @param path to file
 	 * @param willLoadMetadata dictates whether metadata is not loaded (if possible)
 	 * @param mon
 	 * @return DataHolder
 	 * @throws Exception
 	 */
 	public static DataHolder getData(final String path, final boolean willLoadMetadata, final IMonitor mon) throws Exception {
 		return getData(path, willLoadMetadata, false, mon);
 	}
 
 	/**
 	 * Call to load any file type into memory. By default loads all data sets, therefore
 	 * could take a **long time**.
 	 * 
 	 * In addition to find out if a given file loads with a particular loader - it actually 
 	 * LOADS it. 
 	 * 
 	 * Therefore it can take a while to run depending on how quickly the loader
 	 * fails. Also if there are many loaders called in turn, much memory could be consumed and
 	 * discarded. For this reason the registration process requires a file extension and tries 
 	 * all the loaders for a given extension if the extension is registered already. 
 	 * Otherwise it tries all loaders - in no particular order.
 	 * 
 	 * @param path to file
 	 * @param willLoadMetadata dictates whether metadata is not loaded (if possible)
 	 * @param mon
 	 * @return DataHolder
 	 * @throws Exception
 	 */
 	public static DataHolder getData(final String   path, 
 									final boolean  willLoadMetadata, 
 									final boolean  loadImageStacks, 
 									final IMonitor mon) throws Exception {
 
 		if (!(new File(path)).exists()) throw new FileNotFoundException(path);
 		
 		// IMPORTANT: DO NOT USE loadImageStacks in Key. 
 		// Instead when loadImageStacks=true, we add the stack to the already
 		// cached data. So reducing the cache size.
 		final LoaderKey key = new LoaderKey();
 		key.setFilePath(path);
 		key.setMetadata(willLoadMetadata);
 		// END IMPORTANT
 
 		final Object cachedObject = getSoftReference(key);
 		DataHolder holder = null;
 		if (cachedObject!=null && cachedObject instanceof DataHolder) holder = (DataHolder)cachedObject;
 
 		if (holder==null) { // try and load it
 			final Iterator<Class<? extends AbstractFileLoader>> it = getIterator(path);
 			if (it == null) return null;
 	
 			// Currently this method simply cycles through all loaders.
 			// When it finds one which does not give an exception on loading it
 			// returns the data from this loader.
 			while (it.hasNext()) {
 				final Class<? extends AbstractFileLoader> clazz = it.next();
 				final AbstractFileLoader loader = LoaderFactory.getLoader(clazz, path);
 				loader.setLoadMetadata(willLoadMetadata);
 				try {
 					// NOTE Assumes loader fails quickly and nicely
 					// if given the wrong file. If a loader does not
 					// do this it should not be registered with LoaderFactory
 					holder = loader.loadFile(mon);
 					holder.setLoaderClass(clazz);
 					holder.setFilePath(path);
 					key.setMetadata(holder.getMetadata() != null);
 					recordSoftReference(key, holder);
 					break;
 					
 				} catch (OutOfMemoryError ome) {
 					logger.error("There was not enough memory to load {}", path);
 					throw new ScanFileHolderException("Out of memory in loader factory", ome);
 				} catch (Throwable ne) {
 					logger.trace("Loader {} caused {}", loader, ne);
 					continue;
 				}
 			}
 		}
 		
 		// For images, we can put another item in the data holder
 		// which represents the stack of other images in the same directory.
 		try {
 			if (loadImageStacks && holder!=null) {
 
 				if (holder.size()==1 && holder.getLazyDataset(0).getRank()==2 && !isH5(path)) {
 					final ILazyDataset stack = getImageStack(path, holder, mon);
 
 					if (stack!=null) holder.addDataset(stack.getName(), stack);
 				}
 
 			}
 		} catch (Throwable ne) { // It is not a fatal error to fail to load an image stack.
 			logger.error("Cannot load image stack!", ne);
 		}
 		return holder;
 	}
 	
 	/**
 	 * This method can be used to load an image stack of other images in the same directory.
 	 * 
 	 * @param filePath - to one of the images in the stack.
 	 * @param mon
 	 * @return and image stack for 
 	 * @throws Exception
 	 */
 	public static final ILazyDataset getImageStack(final String filePath, DataHolder dh, IMonitor mon) throws Exception {
 		
 		if (filePath==null) return null;
 		final List<String> imageFilenames = new ArrayList<String>();
 		final File   file  = new File(filePath);
 		final String ext  = FileUtils.getFileExtension(file.getName());
 		final File   par = file.getParentFile();
 		if (par.isDirectory()) {
 			for (String fName : par.list()) {
 				if (fName.endsWith(ext)) {
 					final File f = new File(par,fName);
 					imageFilenames.add(f.getAbsolutePath());
 				}
 			}
 		}
 		
 		if (imageFilenames.size() > 1) {
  		    Collections.sort(imageFilenames, new SortNatural<String>(true));
 			ImageStackLoader loader = new ImageStackLoader(imageFilenames, dh, mon);
 			LazyDataset lazyDataset = new LazyDataset("Image Stack", loader.getDtype(), loader.getShape(), loader);
 			return lazyDataset;
 		}
 		return null;
 	}
 
 
 	/**
 	 * Call to load file into memory with specific loader class
 	 * 
 	 * @param loaderClass loader class
 	 * @param path to file
 	 * @param willLoadMetadata dictates whether metadata is not loaded (if possible)
 	 * @param mon
 	 * @return data holder (can be null)
 	 * @throws ScanFileHolderException
 	 */
 	public static DataHolder getData(Class<? extends AbstractFileLoader> loaderClass, String path, boolean willLoadMetadata, IMonitor mon) throws ScanFileHolderException {
 		final LoaderKey key = new LoaderKey();
 		key.setFilePath(path);
 		key.setMetadata(willLoadMetadata);
 
 		final Object cachedObject = getSoftReference(key);
 		if (cachedObject!=null && cachedObject instanceof DataHolder) return (DataHolder)cachedObject;
 
 		AbstractFileLoader loader;
 		try {
 			loader = getLoader(loaderClass, path);
 		} catch (Exception e) {
 			logger.error("Cannot create loader", e);
 			throw new ScanFileHolderException("Cannot create loader", e);
 		}
 		if (loader == null) {
 			logger.error("Cannot create loader");
 			throw new ScanFileHolderException("Cannot create loader");
 		}
 
 		loader.setLoadMetadata(willLoadMetadata);
 		try {
 			DataHolder holder = loader.loadFile(mon);
 			holder.setLoaderClass(loaderClass);
 			key.setMetadata(holder.getMetadata() != null);
 			recordSoftReference(key, holder);
 			return holder;
 		} catch (OutOfMemoryError ome) {
 			logger.error("There was not enough memory to load {}", path);
 			throw new ScanFileHolderException("Out of memory in loader factory", ome);
 		} catch (Throwable ne) {
 			logger.trace("Loader {} caused {}", loader, ne);
 			throw new ScanFileHolderException("Loader error", ne);
 		}
 	}
 
 
 	private final static Object LOCK = new Object();
 
 	/**
 	 * May be null
 	 * @param key
 	 * @return the object referenced or null if it got garbaged or was not cached yet
 	 */
 	private static Object getSoftReference(LoaderKey key) {
 		Object o = getReference(key);
 		if (o != null) {
 			return o;
 		}
 		if (key.hasMetadata()) { // wanted metadata but none there
 			return null;
 		}
 		key.setMetadata(true); // try with unwanted metadata
 		return getReference(key);
 	}
 
 	/**
 	 * May be null
 	 * @param key
 	 * @return the object referenced or null if it got garbaged or was not cached yet
 	 */
 	private static Object getSoftReferenceWithMetadata(LoaderKey key) {
 		Object o = getReference(key);
 		if (o != null) return o;
 
 		LoaderKey k = findKeyWithMetadata(key);
 		return k == null ? null : getReference(k);
 	}
 
 
 	private static final String NO_CACHING = "uk.ac.diamond.scisoft.analysis.io.nocaching";
 
 	/**
 	 * May be null
 	 * @param key
 	 * @return the object referenced or null if it got garbaged or was not cached yet
 	 */
 	private static IDataAnalysisObject getReference(LoaderKey key) {
 		if (Boolean.getBoolean(NO_CACHING)) return null;
 		synchronized (LOCK) {
 			try {
 		        final Reference<IDataAnalysisObject> ref = SOFT_CACHE.get(key);
 		        if (ref == null) return null;
 		        return ref.get();
 			} catch (Throwable ne) {
 				return null;
 			}
 		}
 	}
 
 	private static LoaderKey findKeyWithMetadata(LoaderKey key) {
 		if (Boolean.getBoolean(NO_CACHING)) return null;
 		synchronized (LOCK) {
 			for (LoaderKey k : SOFT_CACHE.keySet()) {
 				if (k.isSameFile(key) && k.hasMetadata()) {
 					return k;
 				}
 			}
 			return null;
 		}
 
 	}
 
 	/**
 	 * 
 	 * @param key
 	 * @param value
 	 * @return true if another value has been replaced.
 	 */
 	private static boolean recordSoftReference(LoaderKey key, IDataAnalysisObject value) {
 		
 		if (Boolean.getBoolean(NO_CACHING)) return false;
 		synchronized (LOCK) {
 			try {
 				Reference<IDataAnalysisObject> ref = Boolean.getBoolean("uk.ac.diamond.scisoft.analysis.io.weakcaching")
 						                           ? new WeakReference<IDataAnalysisObject>(value)
 						                           : new SoftReference<IDataAnalysisObject>(value);
 				return SOFT_CACHE.put(key, ref)!=null;
 			} catch (Throwable ne) {
 				return false;
 			}
 		}
 	}
 
 	/**
 	 * Call to load any file type into memory. If a loader implements IMetaLoader will
 	 * use this fast method to avoid loading the entire file into memory. If the loader
 	 * does not implement IMetaLoader it will return null. Then you should use getData(...) 
 	 * to load the entire file.
 	 * 
 	 * 
 	 * @param path
 	 * @param mon
 	 * @return IMetaData
 	 * @throws Exception
 	 */
 	public static IMetaData getMetaData(final String path, final IMonitor mon) throws Exception {
 
 		
 		if (!(new File(path)).exists()) throw new FileNotFoundException(path);
 		final LoaderKey key = new LoaderKey();
 		key.setFilePath(path);
 		key.setMetadata(true);
 		
 		Object cachedObject = getSoftReferenceWithMetadata(key);
 		if (cachedObject!=null) {
 			if (cachedObject instanceof DataHolder) {
 				IMetaData meta = ((DataHolder) cachedObject).getMetadata();
 			    if (meta!=null) return meta;
 			}
 			if (cachedObject instanceof IMetaData)
 				return (IMetaData)cachedObject;
 			logger.warn("Cached object is not a metadata object or contain one");
 		}
 
 		final Iterator<Class<? extends AbstractFileLoader>> it = getIterator(path);
 		if (it == null) return null;
 
 		// Currently this method simply cycles through all loaders.
 		// When it finds one which does not give an exception on loading, it
 		// returns the data from this loader.
 		while (it.hasNext()) {
 			final Class<? extends AbstractFileLoader> clazz = it.next();
 			final AbstractFileLoader loader = LoaderFactory.getLoader(clazz, path);
 			if (!IMetaLoader.class.isInstance(loader)) continue;
 
 			try {
 				// NOTE Assumes loader fails quickly and nicely
 				// if given the wrong file. If a loader does not
 				// do this, it should not be registered with LoaderFactory
 				((IMetaLoader) loader).loadMetaData(mon);
 				IMetaData meta = ((IMetaLoader) loader).getMetaData();
 				recordSoftReference(key, meta);
 				return meta;
 			} catch (Throwable ne) {
 				//logger.trace("Cannot load nexus meta data", ne);
 				logger.trace("Loader {} caused {}", loader, ne);
 				continue;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Loads a single data by loading the whole data holder and asking for the dataset
 	 * by name. Loaders should load things properly to ILazyDatasets and then this method
 	 * will take from the data holder the set by name. This uses caching of the data holder
 	 * if the data has been previously loaded into a DataHolder.
 	 * 
 	 * @param path
 	 * @param mon
 	 * @return IDataset
 	 * @throws Exception
 	 */
 	public static IDataset getDataSet(final String path, final String name, final IMonitor mon) throws Exception {
 
 		// Makes the cache the DataHolder
         final IDataHolder holder = getData(path, true, mon);
         if (holder == null) return null;
         try {
             return holder.getDataset(name);
         } catch (Exception ne) { // We try to load the data from the ILazyDataset
         	final ILazyDataset lz = holder.getLazyDataset(name);
         	IDataset loaded = lz.getSlice(); // Loads all data
         	holder.addDataset(name, loaded); // We just loaded the data, cache it
         	return loaded;
         }
 	}
 
 	/**
 	 * Returns true if a given file is an IMetaData and able to load metadata without the data
 	 * 
 	 * @param path
 	 * @return true if can load metadata without all data being loaded.
 	 */
 	public boolean isMetaLoader(final String path) throws Exception {
 
 		return isInstanceSupported(path, IMetaLoader.class);
 	}
 
 	private boolean isInstanceSupported(String path, Class<?> interfaceClass) throws Exception {
 
 		final String extension = FileUtils.getFileExtension(path).toLowerCase();
 
 		if (LOADERS.containsKey(extension)) {
 			final Collection<Class<? extends AbstractFileLoader>> loaders = LOADERS.get(extension);
 
 			for (Class<? extends AbstractFileLoader> clazz : loaders) {
 				final AbstractFileLoader loader = LoaderFactory.getLoader(clazz, path);
 				if (interfaceClass.isInstance(loader))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Gets an AbstractFileLoader for the given class and file path.
 	 * 
 	 * @param clazz
 	 * @param path
 	 * @return AbstractFileLoader
 	 * @throws Exception
 	 */
 	public static AbstractFileLoader getLoader(Class<? extends AbstractFileLoader> clazz, final String path) throws Exception {
 
 		AbstractFileLoader loader;
 		try {
 			final Constructor<?> singleString = clazz.getConstructor(String.class);
 			loader = (AbstractFileLoader) singleString.newInstance(path);
 		} catch (NoSuchMethodException e) {
 			loader = clazz.newInstance();
 
 			final Method setFile = loader.getClass().getMethod("setFile", String.class);
 			setFile.invoke(loader, path);
 		} catch (java.lang.NoClassDefFoundError ne) { // CBF Loader does this on win64
 			loader = null;
 		} catch (java.lang.UnsatisfiedLinkError ule) {// CBF Loader does this on win64, the first time
 			loader = null;
 		}
 
 		return loader;
 	}
 
 	/**
 	 * Get class that can load files of given extension
 	 * 
 	 * @param extension
 	 * @return loader class
 	 */
 	public static Class<? extends AbstractFileLoader> getLoaderClass(String extension) {
 		List<Class<? extends AbstractFileLoader>> loader = LOADERS.get(extension); 
 		return (loader!=null) ? loader.get(0) : null;
 	}
 
 	private static Iterator<Class<? extends AbstractFileLoader>> getIterator(String path) throws IllegalAccessException {
 
 		if ((new File(path).isDirectory()))
 			throw new IllegalAccessException("Cannot load directories with LoaderFactory!");
 
 		final String extension = FileUtils.getFileExtension(path).toLowerCase();
 		Iterator<Class<? extends AbstractFileLoader>> it = null;
 
 		if (LOADERS.containsKey(extension)) {
 			it = LOADERS.get(extension).iterator();
 		} else {
 			// We may have a zipped file type that we support
 			final File file = new File(path);
 			final String regEx = ".+\\." + getLoaderExpression() + "\\." + getZipExpression();
 
 			final Matcher m = Pattern.compile(regEx).matcher(file.getName());
 			if (m.matches()) {
 				final String realExt = m.group(1);
 				if (LoaderFactory.LOADERS.keySet().contains(realExt)) {
 					final Collection<Class<? extends AbstractFileLoader>> ret = new ArrayList<Class<? extends AbstractFileLoader>>(
 							1);
 					ret.add(CompressedLoader.class);
 					return ret.iterator();
 				}
 			}
 
 			if (!searchingAllowed)
 				return null;
 
 			final Set<Class<? extends AbstractFileLoader>> all = new HashSet<Class<? extends AbstractFileLoader>>();
 			for (String ext : LOADERS.keySet())
 				all.addAll(LOADERS.get(ext));
 			it = all.iterator();
 		}
 		return it;
 	}
 
 	public static void registerUnzip(final String extension, final Class<? extends InputStream> input) {
 		UNZIPERS.put(extension, input);
 	}
 
 	/**
 	 * Could cache this but it will be fast
 	 */
 	protected static String getZipExpression() {
 		return getExpression(UNZIPERS.keySet().iterator());
 	}
 
 	/**
 	 * Could cache this but it will be fast
 	 */
 	protected static String getLoaderExpression() {
 		return getExpression(LOADERS.keySet().iterator());
 	}
 
 	/**
 	 * Could cache this but it will be fast
 	 */
 	private static String getExpression(final Iterator<String> it) {
 		final StringBuilder buf = new StringBuilder();
 		buf.append("(");
 		while (it.hasNext()) {
 
 			buf.append(it.next());
 			if (it.hasNext())
 				buf.append("|");
 		}
 		buf.append(")");
 		return buf.toString();
 	}
 
 	/**
 	 * Throws an exception if the loader is not ready to be used with LoaderFactory.
 	 * Otherwise adds the class to the list of loaders.
 	 * 
 	 * NOTE that duplicates are allowed and the LoaderFactory simply tries loaders until
 	 * one works. If loaders do not fail fast on invalid files then this approach does not work.
 	 * 
 	 * This has been tested by adding a test for each file type using the loader factory. This
 	 * coverage could be extended by adding more example files and attempting to load them
 	 * with the factory. However as long as each file type is passed through LoaderFactory and
 	 * checks are made in the test to ensure that the loader is working, there is a good chance
 	 * that it will find the right loader.
 	 * 
 	 * @param extension - lower case string
 	 * @param loader
 	 * @throws Exception
 	 */
 	public static void registerLoader(final String extension, final Class<? extends AbstractFileLoader> loader) throws Exception {
 
 		List<Class<? extends AbstractFileLoader>> list = prepareRegistration(extension, loader);
 
 		// Since not using set of loaders anymore must use contains to ensure
 		// that a memory leak does not occur.
 		if (!list.contains(loader)) list.add(loader);
 	}
 
 	/**
 	 * Throws an exception if the loader is not ready to be used with LoaderFactory.
 	 * Otherwise adds the class to the list of loaders at the position specified.
 	 * 
 	 * NOTE that duplicates are allowed and the LoaderFactory simply tries loaders until
 	 * one works. If loaders do not fail fast on invalid files then this approach does not work.
 	 * 
 	 * This has been tested by adding a test for each file type using the loader factory. This
 	 * coverage could be extended by adding more example files and attempting to load them
 	 * with the factory. However as long as each file type is passed through LoaderFactory and
 	 * checks are made in the test to ensure that the loader is working, there is a good chance
 	 * that it will find the right loader.
 	 * 
 	 * @param extension - lower case string
 	 * @param loader
 	 * @throws Exception
 	 */
 	public static void registerLoader(final String extension, final Class<? extends AbstractFileLoader> loader, final int position) throws Exception {
 
 		List<Class<? extends AbstractFileLoader>> list = prepareRegistration(extension, loader);
 		// Since not using set of loaders anymore must use contains to ensure
 		// that a memory leak does not occur.
 		if (!list.contains(loader)) list.add(position, loader);
 	}
 
 	private static List<Class<? extends AbstractFileLoader>> prepareRegistration(String extension, Class<? extends AbstractFileLoader> loader) throws Exception {
 		try {
 			loader.getConstructor(String.class);
 		} catch (NoSuchMethodException e) {
 			// TODO These messages are not quite correct
 			if (loader.getMethod("setFile", String.class) == null)
 				throw new Exception("Loaders must have method setFile(String path)");
 			if (loader.getConstructor() == null)
 				throw new Exception("Loaders must have a no argument constructor!");
 		}
 
 		List<Class<? extends AbstractFileLoader>> list = LOADERS.get(extension);
 		if (list == null) {
 			list = new ArrayList<Class<? extends AbstractFileLoader>>();
 			LOADERS.put(extension, list);
 		}
 		return list;
 	}
 
 	/**
 	 * Call to clear all the loaders registered for a given extension.
 	 * 
 	 * @param extension
 	 * @return the old loader list, now removed, if any.
 	 */
 	public static List<Class<? extends AbstractFileLoader>> clearLoader(final String extension) {
 		return LOADERS.remove(extension);
 	}
 
 	private static boolean searchingAllowed = false;
 
 	public static void setLoaderSearching(final boolean sa) {
 		searchingAllowed = sa;
 	}
 
 	protected static Class<? extends java.io.InputStream> getZipStream(final String extension) {
 		return UNZIPERS.get(extension);
 	}
 
 	
 	private static IMetaData lockedMetaData;
 
 	/**
 	 * DO NOT MAKE Public. Use ILoaderService instead.
 	 * @return metaData
 	 */
 	static IMetaData getLockedMetaData() {
 		return lockedMetaData;
 	}
 
 	/**
 	 * @Internal do not use. Use ILoaderService.getLockedDiffractionMetaData()
 	 */
 	public static void setLockedMetaData(IMetaData lockedMetaData) {
 		LoaderFactory.lockedMetaData = lockedMetaData;
 		if (lockedMetaData==null) clear();
 	}
 
 	public final static List<String> EXT;
 	static {
 		List<String> tmp = new ArrayList<String>(7);
 		tmp.add("h5");
 		tmp.add("nxs");
 		tmp.add("hd5");
 		tmp.add("hdf5");
 		tmp.add("hdf");
 		tmp.add("nexus");
 		EXT = Collections.unmodifiableList(tmp);
 	}	
 
 	private static boolean isH5(final String filePath) {
 		if (filePath == null) { return false; }
 		final String ext = FileUtils.getFileExtension(filePath);
 		if (ext == null) { return false; }
 		return EXT.contains(ext.toLowerCase());
 	}
 
 }
