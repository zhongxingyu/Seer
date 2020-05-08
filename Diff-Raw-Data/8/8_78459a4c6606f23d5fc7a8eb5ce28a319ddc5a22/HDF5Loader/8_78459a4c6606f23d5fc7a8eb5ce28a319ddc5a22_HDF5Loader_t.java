 /*
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
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.InetAddress;
 import java.net.URI;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Queue;
 
 import ncsa.hdf.hdf5lib.H5;
 import ncsa.hdf.hdf5lib.HDF5Constants;
 import ncsa.hdf.hdf5lib.HDFNativeData;
 import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
 import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
 import ncsa.hdf.hdf5lib.structs.H5G_info_t;
 import ncsa.hdf.hdf5lib.structs.H5O_info_t;
 import ncsa.hdf.object.Attribute;
 import ncsa.hdf.object.Dataset;
 import ncsa.hdf.object.Datatype;
 import ncsa.hdf.object.HObject;
 import ncsa.hdf.object.h5.H5Datatype;
 import ncsa.hdf.object.h5.H5File;
 import ncsa.hdf.object.h5.H5Group;
 import ncsa.hdf.object.h5.H5Link;
 import ncsa.hdf.object.h5.H5ScalarDS;
 
 import org.dawb.hdf5.HierarchicalDataFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ByteDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.LazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.PositionIterator;
 import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.StringDataset;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Attribute;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Dataset;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5File;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Group;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5Node;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;
 import uk.ac.diamond.scisoft.analysis.hdf5.HDF5SymLink;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 /**
  * Load HDF5 files using NCSA's Java library
  */
 public class HDF5Loader extends AbstractFileLoader implements IMetaLoader {
 	
 	protected static final Logger logger = LoggerFactory.getLogger(HDF5Loader.class);
 
 
 	private String fileName;
 	private boolean keepBitWidth = false;
 	private boolean async = false;
 	private int syncLimit;
 	private int syncNodes;
 	private ScanFileHolderException syncException = null;
 
 	private String host = null;
 
 	private static final int HASH_MULTIPLIER = 139;
 
 	public static final String DATA_FILENAME_ATTR_NAME = "data_filename";
 
 	public HDF5Loader() {
 		setHost();
 	}
 
 	public HDF5Loader(final String name) {
 		setHost();
 		setFile(name);
 	}
 
 	/**
 	 * Set whether loading is done asynchronously so that program flow is returned after first
 	 * level of a tree is read in and at least 200 nodes are read 
 	 * @param loadAsynchronously
 	 */
 	public void setAsyncLoad(boolean loadAsynchronously) {
 		setAsyncLoad(loadAsynchronously, 200);
 	}
 
 	/**
 	 * Set whether loading is done asynchronously so that program flow is returned after first
 	 * level of a tree is read in and at least the given number of nodes are read 
 	 * @param loadAsynchronously
 	 * @param nodes
 	 */
 	public void setAsyncLoad(boolean loadAsynchronously, int nodes) {
 		async = loadAsynchronously;
 		syncLimit = nodes;
 	}
 
 	/**
 	 * Stop asynchronous loading
 	 */
 	public synchronized void stopAsyncLoading() {
 		syncNodes = syncLimit;
 	}
 
 	private void setHost() {
 		try {
 			host = InetAddress.getLocalHost().getHostName();
 		} catch (UnknownHostException e) {
 			logger.error("Could not find host name", e);
 		}
 	}
 
 	public void setFile(final String name) {
 		fileName = name;
 	}
 
 	@Override
 	public DataHolder loadFile() throws ScanFileHolderException {
 		return loadFile(null);
 	}
 
 	@Override
 	public DataHolder loadFile(IMonitor mon) throws ScanFileHolderException {
 		HDF5File tree = loadTree(mon);
 		DataHolder dh = createDataHolder(tree, loadMetadata);
 		if (loadMetadata) {
 			metadata = (Metadata) dh.getMetadata();
 			metadata.setFilePath(fileName);
 		}
 		return dh;
 	}
 
 	public HDF5File loadTree() throws ScanFileHolderException {
 		return loadTree(null);
 	}
 
 	HDF5File tFile = null;
 
 	private LoadFileThread loaderThread = null;
 
 	class LoadFileThread extends Thread {
 		private IMonitor mon;
 
 		public LoadFileThread(final IMonitor monitor) {
 			mon = monitor;
 			setName("Load HDF5 file: " + fileName);
 		}
 		
 		@Override
 		public void run() {
 			int fid = -1;
 			try {
 				HierarchicalDataFactory.acquireLowLevelReadingAccess(fileName);
 				fid = H5.H5Fopen(fileName, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
 
 				if (!monitorIncrement(mon)) {
 					try {
 						H5.H5Fclose(fid);
 					} catch (HDF5Exception ex) {
 					}
 					return;
 				}
 
 				tFile = createTreeBF(mon, fid, keepBitWidth);
 			} catch (Throwable le) {
 				syncException = new ScanFileHolderException("Problem loading file: " + fileName, le);
 			} finally {
 				try {
 					H5.H5Fclose(fid);
 				} catch (Throwable e) {
 				}
 				HierarchicalDataFactory.releaseLowLevelReadingAccess(fileName);
 			}
 		}
 	}
 
 	private synchronized void waitForSyncLimit() throws ScanFileHolderException {
 		while (syncNodes < syncLimit) {
 			try {
 				wait();
 			} catch (InterruptedException e) {
 			}
 		}
 		if (syncException != null)
 			throw syncException;
 	}
 
 	private synchronized void updateSyncNodes(int nodes) throws ScanFileHolderException {
 		syncNodes = nodes;
 		if (syncNodes >= syncLimit) {
 			notifyAll();
 		}
 		if (syncException != null)
 			throw syncException;
 	}
 
 	/**
 	 * Load tree
 	 * @param mon
 	 * @return a HDF5File tree
 	 * @throws ScanFileHolderException
 	 */
 	public HDF5File loadTree(final IMonitor mon) throws ScanFileHolderException {
 		if (!monitorIncrement(mon)) {
 			return null;
 		}
 
 		logger.trace(String.format("Loading in thd %x\n", Thread.currentThread().getId()));
 		File f = new File(fileName);
 		if (!f.exists()) {
 			throw new ScanFileHolderException("File, " + fileName + ", does not exist");
 		}
 		
 		try {
 			fileName = f.getCanonicalPath();
 		} catch (IOException e) {
 			logger.error("Could not get canonical path", e);
 			throw new ScanFileHolderException("Could not get canonical path", e);
 		}
 		if (async) {
 			loaderThread = new LoadFileThread(mon);
 			loaderThread.start();
 			try {
 				Thread.sleep(100l);
 			} catch (InterruptedException e) {
 			}
 			waitForSyncLimit();
 		} else {
 			int fid = -1;
 			try {
 				HierarchicalDataFactory.acquireLowLevelReadingAccess(fileName);
 				fid = H5.H5Fopen(fileName, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
 
 				if (!monitorIncrement(mon)) {
 					try {
 						H5.H5Fclose(fid);
 					} catch (HDF5Exception ex) {
 					}
 					return null;
 				}
 
 				tFile = createTree(fid, keepBitWidth);
 			} catch (Throwable le) {
 				throw new ScanFileHolderException("Problem loading file: " + fileName, le);
 			} finally {
 				try {
 					H5.H5Fclose(fid);
 				} catch (Throwable e) {
 				}
 				HierarchicalDataFactory.releaseLowLevelReadingAccess(fileName);
 			}
 		}
 		return tFile;
 	}
 
 	/**
 	 * @return true if still loading file
 	 */
 	public boolean isLoading() {
 		if (loaderThread == null)
 			return false;
 		return loaderThread.isAlive();
 	}
 
 	/**
 	 * @param fid file ID
 	 * @param keepBitWidth
 	 * @return a HDF5 tree
 	 * @throws Exception
 	 */
 	private HDF5File createTree(final int fid, final boolean keepBitWidth) throws Exception {
 		final long oid = fileName.hashCode(); // include file name in ID
 		HDF5File f = new HDF5File(oid, fileName);
 		f.setHostname(host);
 
 		HDF5Group g = (HDF5Group) createGroup(fid, f, -1, new HashMap<Long, HDF5Node>(), null, HDF5File.ROOT, keepBitWidth);
 		if (g == null) {
 			throw new ScanFileHolderException("Could not copy root group");
 		}
 		f.setGroup(g);
 		return f;
 	}
 
 	/**
 	 * Breadth-first tree walk
 	 * @param mon
 	 * @param fid file ID
 	 * @param keepBitWidth
 	 * @return a HDF5 tree
 	 * @throws Exception
 	 */
 	private HDF5File createTreeBF(final IMonitor mon, final int fid, final boolean keepBitWidth) throws Exception {
 		final long oid = fileName.hashCode(); // include file name in ID
 		HDF5File f = new HDF5File(oid, fileName);
 		f.setHostname(host);
 
 		HashMap<Long, HDF5Node> pool = new HashMap<Long, HDF5Node>();
 		Queue<String> iqueue = new LinkedList<String>();
 		HDF5Group g = (HDF5Group) createGroup(fid, f, -1, pool, iqueue, HDF5File.ROOT, keepBitWidth);
 		if (g == null) {
 			throw new ScanFileHolderException("Could not copy root group");
 		}
 		f.setGroup(g);
 
 		Queue<String> oqueue = new LinkedList<String>();
 		while (iqueue.size() > 0) { // read in first level
 			String nn = iqueue.remove();
 			HDF5Node n = createGroup(fid, f, -1, pool, oqueue, nn, keepBitWidth);
 			if (n == null) {
 				logger.error("Could not find group {}", nn);
 				continue;
 			}
 			HDF5NodeLink ol = f.findNodeLink(HDF5File.ROOT);
 			HDF5Group og = (HDF5Group) ol.getDestination();
 			og.addNode(f, HDF5File.ROOT, nn.substring(1, nn.length() - 1), n);
 		}
 
 		tFile = f;
 
 		if (!monitorIncrement(mon)) {
 			updateSyncNodes(syncLimit); // notify if finished
 			return null;
 		}
 
 		do {
 			Queue<String> q = iqueue;
 			iqueue = oqueue;
 			oqueue = q;
 			while (iqueue.size() > 0) {
 				updateSyncNodes(pool.size());
 				String nn = iqueue.remove();
 				HDF5Node n = createGroup(fid, f, -1, pool, oqueue, nn, keepBitWidth);
 				if (n == null) {
 					logger.error("Could not find group {}", nn);
 					continue;
 				}
 				int i = nn.lastIndexOf(HDF5Node.SEPARATOR, nn.length() - 2);
 				HDF5NodeLink ol = f.findNodeLink(nn.substring(0, i));
 				HDF5Group og = (HDF5Group) ol.getDestination();
 				og.addNode(f, nn.substring(0, i+1), nn.substring(i + 1, nn.length() - 1), n);
 			}
 			if (!monitorIncrement(mon)) {
 				updateSyncNodes(syncLimit); // notify if finished
 				return null;
 			}
 		} while (oqueue.size() > 0);
 
 		updateSyncNodes(syncLimit); // notify if finished
 
 		return f;
 	}
 
 	private static int LIMIT = 10240;
 
 	/**
 	 * Create a node (and all its children, recursively) from given location ID
 	 * @param fid location ID
 	 * @param f HDF5 file
 	 * @param pool
 	 * @param queue
 	 * @param name of group (full path and ends in '/')
 	 * @param keepBitWidth
 	 * @return node
 	 * @throws Exception
 	 */
 	private static HDF5Node createNode(final int fid, final HDF5File f, final HashMap<Long, HDF5Node> pool, final Queue<String> queue, final String name, final boolean keepBitWidth) throws Exception {
 		try {
 			H5O_info_t info = H5.H5Oget_info_by_name(fid, name, HDF5Constants.H5P_DEFAULT);
 			int t = info.type;
 			if (t == HDF5Constants.H5O_TYPE_GROUP) {
 				return createGroup(fid, f, -1, pool, queue, name, keepBitWidth);
 			} else if (t == HDF5Constants.H5O_TYPE_DATASET) {
 				return createDataset(fid, f, -1, pool, name, keepBitWidth);
 			} else if (t == HDF5Constants.H5O_TYPE_NAMED_DATATYPE) {
 				logger.error("Named datatype not supported"); // TODO
 			} else {
 				logger.error("Unknown object type");
 			}
 		} catch (HDF5Exception ex) {
 			logger.error("Could not find info about object {}" + name);
 		}
 		return null;
 	}
 
 	/**
 	 * Create a group (and all its children, recursively) from given location ID
 	 * @param fid location ID
 	 * @param f HDF5 file
 	 * @param oid object ID
 	 * @param pool
 	 * @param queue
 	 * @param name of group (full path and ends in '/')
 	 * @param keepBitWidth
 	 * @return node
 	 * @throws Exception
 	 */
 	private static HDF5Node createGroup(final int fid, final HDF5File f, long oid, final HashMap<Long, HDF5Node> pool, final Queue<String> queue, final String name, final boolean keepBitWidth) throws Exception {
 		int gid = -1;
 		HDF5Group group = null;
 
 		try {
 			int nelems = 0;
 			try {
 				gid = H5.H5Gopen(fid, name, HDF5Constants.H5P_DEFAULT);
 				H5G_info_t info = H5.H5Gget_info(gid);
 				nelems = (int) info.nlinks;
 			} catch (HDF5Exception ex) {
 				throw new ScanFileHolderException("Could not open group", ex);
 			}
 
 			if (oid == -1) {
 				byte[] idbuf = null;
 				try {
 					idbuf = H5.H5Rcreate(fid, name, HDF5Constants.H5R_OBJECT, -1);
 					oid = f.getID() * HASH_MULTIPLIER + HDFNativeData.byteToLong(idbuf, 0);
 				} catch (HDF5Exception ex) {
 					throw new ScanFileHolderException("Could not find group reference", ex);
 				}
 				if (oid >= 0 && pool != null && pool.containsKey(oid)) {
 					HDF5Node p = pool.get(oid);
 					if (!(p instanceof HDF5Group)) {
 						throw new IllegalStateException("Matching pooled node is not a group");
 					}
 					return p;
 				}
 			}
 
 			group = new HDF5Group(oid);
 			if (pool != null)
 				pool.put(oid, group);
 			if (copyAttributes(f, name, group, gid)) {
 				final String link = group.getAttribute(NAPIMOUNT).getFirstElement();
 				try {
 					return copyNAPIMountNode(f, pool, link, keepBitWidth);
 				} catch (Exception e) {
 					logger.error("Could not copy NAPI mount", e);
 				}
 			}
 
 			if (nelems <= 0) {
 				return group;
 			}
 
 			if (nelems > LIMIT) {
 				logger.warn("Number of members in group {} exceed limit ({} > {}). Only reading up to limit",
 						new Object[] { name, nelems, LIMIT });
 				nelems = LIMIT;
 			}
 
 			int[] oTypes = new int[nelems];
 			int[] lTypes = new int[nelems];
 			long[] oids = new long[nelems];
 			String[] oNames = new String[nelems];
 			try {
 				H5.H5Gget_obj_info_all(fid, name, oNames, oTypes, lTypes, oids, HDF5Constants.H5_INDEX_NAME);
 			} catch (HDF5Exception ex) {
 				logger.error("Could not get objects info in group", ex);
 				return null;
 			}
 
 			String oname;
 			int otype;
 			int ltype;
 
 			// Iterate through the file to see members of the group
 			for (int i = 0; i < nelems; i++) {
 				oname = oNames[i];
 				if (oname == null) {
 					continue;
 				}
 				otype = oTypes[i];
 				ltype = lTypes[i];
 				oid = f.getID() * HASH_MULTIPLIER + oids[i];
 
 				if (ltype == HDF5Constants.H5L_TYPE_HARD) {
 					if (otype == HDF5Constants.H5O_TYPE_GROUP) {
 						if (oid >= 0 && pool != null && pool.containsKey(oid)) {
 							HDF5Node p = pool.get(oid);
 							if (!(p instanceof HDF5Group)) {
 								throw new IllegalStateException("Matching pooled node is not a group");
 							}
 							group.addNode(f, name, oname, p);
 							continue;
 						}
 
 						// System.err.println("G: " + oname);
 						if (queue != null) {
 							queue.add(name + oname + HDF5Node.SEPARATOR);
 						} else {
 							HDF5Node g = createGroup(fid, f, oid, pool, queue, name + oname + HDF5Node.SEPARATOR, keepBitWidth);
 							if (g == null) {
 								logger.error("Could not load group {} in {}", oname, name);
 							} else {
 								group.addNode(f, name, oname, g);
 							}
 						}
 					} else if (otype == HDF5Constants.H5O_TYPE_DATASET) {
 						if (oid >= 0 && pool != null && pool.containsKey(oid)) {
 							HDF5Node p = pool.get(oid);
 							if (!(p instanceof HDF5Dataset)) {
 								throw new IllegalStateException("Matching pooled node is not a dataset");
 							}
 							group.addNode(f, name, oname, p);
 							continue;
 						}
 
 						// System.err.println("D: " + oname);
 						HDF5Node n = createDataset(fid, f, oid, pool, name + oname, keepBitWidth);
 
 						if (n != null)
 							group.addNode(f, name, oname, n);
 					} else if (otype == HDF5Constants.H5O_TYPE_NAMED_DATATYPE) {
 						logger.error("Named datatype not supported"); // TODO
 					}
 				} else if (ltype == HDF5Constants.H5L_TYPE_SOFT) {
 					// System.err.println("S: " + oname);
 					String[] linkName = new String[1];
 					int t = H5.H5Lget_val(gid, oname, linkName, HDF5Constants.H5P_DEFAULT);
 					if (t < 0) {
 						logger.error("Could not get value of link");
 						continue;
 					}
 					// System.err.println("  -> " + linkName[0]);
 					HDF5SymLink slink = new HDF5SymLink(oid, f, linkName[0]);
 					group.addNode(f, name, oname, slink);
 				} else if (ltype == HDF5Constants.H5L_TYPE_EXTERNAL) {
 					// System.err.println("E: " + oname);
 					String[] linkName = new String[2]; // file name and file path
 					int t = H5.H5Lget_val(gid, oname, linkName, HDF5Constants.H5P_DEFAULT);
 					// System.err.println("  -> " + linkName[0] + " in " + linkName[1]);
 					if (t < 0) {
 						logger.error("Could not get value of link");
 						continue;
 					}
 
 					String eName = linkName[1];
 					if (!(new File(eName).exists())) { // use directory of linking file
 						logger.warn("Could not find external file {}, trying in {}", eName, f.getParentDirectory());
 						eName = new File(f.getParentDirectory(), new File(eName).getName()).getAbsolutePath();
 					}
 					if (new File(eName).exists()) {
 						group.addNode(f, name, oname, getExternalNode(pool, f.getHostname(), eName, linkName[0], keepBitWidth));
 					} else {
 						logger.error("Could not find external file {}", eName);
 					}
 				} else {
 					// System.err.println("U: " + oname);
 				}
 			}
 		} finally {
 			if (gid >= 0) {
 				try {
 					H5.H5Gclose(gid);
 				} catch (HDF5Exception ex) {
 				}
 			}
 		}
 
 		return group;
 	}
 
 	/**
 	 * Create a dataset (and all its children, recursively) from given location ID
 	 * @param lid location ID
 	 * @param f HDF5 file
 	 * @param oid object ID
 	 * @param pool
 	 * @param path of dataset (can full path or relative)
 	 * @param keepBitWidth
 	 * @return node
 	 * @throws Exception
 	 */
 	private static HDF5Node createDataset(final int lid, final HDF5File f, long oid, final HashMap<Long, HDF5Node> pool, final String path, final boolean keepBitWidth) throws Exception {
 		byte[] idbuf = null;
 		if (oid == -1) {
 			try {
 				idbuf = H5.H5Rcreate(lid, path, HDF5Constants.H5R_OBJECT, -1);
 				oid = f.getID() * HASH_MULTIPLIER + HDFNativeData.byteToLong(idbuf, 0);
 			} catch (HDF5Exception ex) {
 				throw new ScanFileHolderException("Could not find object reference", ex);
 			}
 			if (oid >= 0 && pool != null && pool.containsKey(oid)) {
 				HDF5Node p = pool.get(oid);
 				if (!(p instanceof HDF5Dataset)) {
 					throw new IllegalStateException("Matching pooled node is not a dataset");
 				}
 				return p;
 			}
 		}
 
 		int did = -1, tid = -1;
 		try {
 //			Thread.sleep(200);
 
 			did = H5.H5Dopen(lid, path, HDF5Constants.H5P_DEFAULT);
 			tid = H5.H5Dget_type(did);
 
 			// create a new dataset
 			HDF5Dataset d = new HDF5Dataset(oid);
 			if (copyAttributes(f, path, d, did)) {
 				final String link = d.getAttribute(NAPIMOUNT).getFirstElement();
 				return copyNAPIMountNode(f, pool, link, keepBitWidth);
 			}
 			int i = path.lastIndexOf(HDF5Node.SEPARATOR);
 			final String sname = i >= 0 ? path.substring(i + 1) : path;
 			if (!createLazyDataset(f, d, path, sname, did, tid, keepBitWidth,
 					d.containsAttribute(DATA_FILENAME_ATTR_NAME))) {
 				logger.error("Could not create a lazy dataset from {}", path);
 			}
 			if (pool != null)
 				pool.put(oid, d);
 			return d;
 		} catch (HDF5Exception ex) {
 			logger.error(String.format("Could not open dataset %s in %s", path, f), ex);
 		} finally {
 			if (tid >= 0) {
 				try {
 					H5.H5Tclose(tid);
 				} catch (HDF5Exception ex) {
 				}
 			}
 			if (did >= 0) {
 				try {
 					H5.H5Dclose(did);
 				} catch (HDF5Exception ex) {
 				}
 			}
 		}
 
 		return null;
 	}
 
 	private static final String NAPIMOUNT = "napimount";
 	private static final String NAPISCHEME = "nxfile";
 
 	// return true when attributes contain a NAPI mount - dodgy external linking for HDF5 version < 1.8
 	private static boolean copyAttributes(final HDF5File f, final HDF5Node nn, final HObject oo) {
 		boolean hasNAPIMount = false;
 
 		try {
 			if (oo.hasAttribute()) {
 				@SuppressWarnings("unchecked")
 				final List<Attribute> attributes = oo.getMetadata();
 				final String fname = (oo instanceof H5Group) && ((H5Group) oo).isRoot() ? HDF5File.ROOT : oo
 						.getFullName();
 				for (Attribute a : attributes) {
 					HDF5Attribute h = new HDF5Attribute(f, fname, a.getName(), a.getValue(), a.isUnsigned());
 					h.setTypeName(getTypeName(a.getType()));
 					nn.addAttribute(h);
 					if (a.getName().equals(NAPIMOUNT)) {
 						hasNAPIMount = true;
 					}
 				}
 			}
 		} catch (Exception e) {
 			logger.warn("Problem with attributes on {}: {}", oo.getFullName(), e.getMessage());
 		}
 		return hasNAPIMount;
 	}
 
 	// return true when attributes contain a NAPI mount - dodgy external linking for HDF5 version < 1.8
 	private static boolean copyAttributes(final HDF5File f, final String name, final HDF5Node nn, final int id) {
 		boolean hasNAPIMount = false;
 
 		try {
 			final List<Attribute> attributes = H5File.getAttribute(id);
 			for (Attribute a : attributes) {
 				HDF5Attribute h = new HDF5Attribute(f, name, a.getName(), a.getValue(), a.isUnsigned());
 				h.setTypeName(getTypeName(a.getType()));
 				nn.addAttribute(h);
 				if (a.getName().equals(NAPIMOUNT)) {
 					hasNAPIMount = true;
 				}
 			}
 		} catch (HDF5Exception e) {
 			logger.warn("Problem with attributes on {}: {}", name, e.getMessage());
 		}
 		return hasNAPIMount;
 	}
 
 	// get external node
 	private static HDF5Node getExternalNode(final HashMap<Long, HDF5Node> pool, final String host, final String path, String node, final boolean keepBitWidth) throws Exception {
 		HDF5Node nn = null;
 
 		if (!node.startsWith(HDF5File.ROOT)) {
 			node = HDF5File.ROOT + node;
 		}
 
 		final String cPath;
 		try {
 			cPath = new File(path).getCanonicalPath();
 		} catch (IOException e) {
 			logger.error("Could not get canonical path", e);
 			throw new ScanFileHolderException("Could not get canonical path", e);
 		}
 
 		int fid = -1;
 		try {
 			HierarchicalDataFactory.acquireLowLevelReadingAccess(cPath);
 			fid = H5.H5Fopen(path, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
 
 			final long oid = path.hashCode(); // include file name in ID
 			HDF5File f = new HDF5File(oid, path);
 			f.setHostname(host);
 
 			nn = createNode(fid, f, pool, null, node, keepBitWidth);
 		} catch (Throwable le) {
 			throw new ScanFileHolderException("Problem loading file: " + path, le);
 		} finally {
 			try {
 				H5.H5Fclose(fid);
 			} catch (Throwable e) {
 			}
 			HierarchicalDataFactory.releaseLowLevelReadingAccess(cPath);
 		}
 		
 		return nn;
 	}
 
 	// retrieve external file link
 	private static HDF5Node copyNAPIMountNode(final HDF5File file, final HashMap<Long, HDF5Node> pool, final String link, final boolean keepBitWidth) throws Exception {
 		final URI ulink = new URI(link);
 		HDF5Node nn = null;
 		if (ulink.getScheme().equals(NAPISCHEME)) {
 			String lpath = ulink.getPath();
 			String ltarget = ulink.getFragment();
 			File f = new File(lpath);
 			if (!f.exists()) {
 				logger.debug("File, {}, does not exist!", lpath);
 
 				// see if linked file in same directory
 				f = new File(file.getParentDirectory(), f.getName());
 				if (!f.exists()) {
 					throw new ScanFileHolderException("File, " + lpath + ", does not exist");
 				}
 				lpath = f.getAbsolutePath();
 			}
 			nn = getExternalNode(pool, file.getHostname(), lpath, ltarget, keepBitWidth);
 			if (nn == null)
 				logger.warn("Could not find external node: {}", ltarget);
 		} else {
 			System.err.println("Wrong scheme: " + ulink.getScheme());
 		}
 
 		return nn;
 	}
 
 	/**
 	 * Method can be used to set up a HObject which is a group.
 	 * @param file
 	 * @param pool - may be null
 	 * @param oo
 	 * @param keepBitWidth
 	 * @return HDF5Node, child to be added.
 	 * @throws Exception
 	 */
 	public static HDF5Node copyNode(final HDF5File file, final HashMap<Long, HDF5Node> pool, final HObject oo, final boolean keepBitWidth) throws Exception {
 		if (oo instanceof H5Link) {
 			// a link which cannot be resolved remains a link
 			H5Link ol = (H5Link) oo;
 			ol.getMetadata(); // API quirk needed to populate link target
 			final String target = ol.getLinkTargetObjName();
 			if (target == null) {
 				throw new IllegalArgumentException("No link target defined in link object");
 			}
 
 			throw new IllegalArgumentException("Link target cannot be resolved: " + target);
 		}
 
 		final Long oid = oo.getOID()[0] + file.getFilename().hashCode()*17; // include file name in ID
 
 		if (oo instanceof Dataset) {
 			if (pool != null && pool.containsKey(oid)) {
 				HDF5Node p = pool.get(oid);
 				if (!(p instanceof HDF5Dataset)) {
 					throw new IllegalStateException("Matching pooled node is not a dataset");
 				}
 				return p;
 			}
 
 			HDF5Dataset nd = new HDF5Dataset(oid);
 			if (copyAttributes(file, nd, oo)) {
 				final String link = nd.getAttribute(NAPIMOUNT).getFirstElement();
 				return copyNAPIMountNode(file, pool, link, keepBitWidth);
 			}
 
 			final Datatype type = ((Dataset) oo).getDatatype();
 			final int dclass = type.getDatatypeClass();
 
 			if (dclass == Datatype.CLASS_COMPOUND) { // special case for compound data types
 				return nd;
 			}
 
 			nd.setTypeName(getTypeName(type));
 
 			if (!(oo instanceof H5ScalarDS)) {
 				throw new IllegalArgumentException("Dataset unsupported");
 			}
 
 			H5ScalarDS osd = (H5ScalarDS) oo;
 			if (dclass == Datatype.CLASS_STRING) { // special case for strings
 				// This is a kludge for linking to non-hdf5 files
 				// An attribute called "data_filename" is defined and refers to 
 				// an external file and acts like a group
 				osd.setConvertByteToString(true);
 				if (nd.containsAttribute(DATA_FILENAME_ATTR_NAME)) {
 					// interpret set of strings as the full path names to a group of external files that are stacked together
 					ExternalFiles ef = extractExternalFileNames(osd);
 					try {
 						ILazyDataset l = createStackedDatasetFromStrings(ef);
 						nd.setDataset(l);
 					} catch (Throwable th) {
 						logger.warn("Could not find {}, trying in {}", ef.files[0], file.getParentDirectory());
 						try { // try again with known-to-be-good directory
 							ILazyDataset l = createStackedDatasetFromStrings(ef, file.getParentDirectory());
 							nd.setDataset(l);
 						} catch (Throwable th2) {
 							logger.error("Unable to create lazydataset for" + osd, th2);
 							nd.setString(ef.getAsText());
 						}
 					}
 				} else {
 					nd.setDataset(createLazyDataset(file.getHostname(), osd, keepBitWidth));
 					nd.setMaxShape(osd.getMaxDims());
 				}
 			} else {
 				nd.setDataset(createLazyDataset(file.getHostname(), osd, keepBitWidth));
 				nd.setMaxShape(osd.getMaxDims());
 			}
 			if (pool != null)
 				pool.put(oid, nd);
 			return nd;
 		} else if (oo instanceof H5Group) {
 			if (pool != null && pool.containsKey(oid)) {
 				HDF5Node p = pool.get(oid);
 				if (!(p instanceof HDF5Group)) {
 					throw new IllegalStateException("Matching pooled node is not a group");
 				}
 				return p;
 			}
 			H5Group og = (H5Group) oo;
 			HDF5Group ng = new HDF5Group(oid);
 			if (copyAttributes(file, ng, og)) {
 				final String link = ng.getAttribute(NAPIMOUNT).getFirstElement();
 				return copyNAPIMountNode(file, pool, link, keepBitWidth);
 			}
 
 			List<HObject> members = og.getMemberList();
 			for (HObject h : members) {
 				final String path = h.getPath();
 				final String name = h.getName();
 				ng.addNode(file, path, name, copyNode(file, pool, h, keepBitWidth));
 			}
 
 			if (pool != null)
 				pool.put(oid, ng);
 			return ng;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Translate between data type and dataset type
 	 * @param dclass data type class
 	 * @param dsize data type item size in bytes
 	 * @return dataset type
 	 */
 	public static int getDtype(final int dclass, final int dsize) {
 		return getDtype(dclass, dsize, false);
 	}
 
 	/**
 	 * Translate between data type and dataset type
 	 * @param dclass data type class
 	 * @param dsize data type item size in bytes
 	 * @param isComplex
 	 * @return dataset type
 	 */
 	private static int getDtype(final int dclass, final int dsize, final boolean isComplex) {
 		switch (dclass) {
 		case Datatype.CLASS_STRING:
 			return AbstractDataset.STRING;
 		case Datatype.CLASS_CHAR:
 		case Datatype.CLASS_INTEGER:
 			switch (dsize) {
 			case 1:
 				return AbstractDataset.INT8;
 			case 2:
 				return AbstractDataset.INT16;
 			case 4:
 				return AbstractDataset.INT32;
 			case 8:
 				return AbstractDataset.INT64;
 			}
 			break;
 		case Datatype.CLASS_FLOAT:
 			if (isComplex) {
 				switch (dsize) {
 				case 4:
 					return AbstractDataset.COMPLEX64;
 				case 8:
 					return AbstractDataset.COMPLEX128;
 				}
 			}
 			switch (dsize) {
 			case 4:
 				return AbstractDataset.FLOAT32;
 			case 8:
 				return AbstractDataset.FLOAT64;
 			}
 			break;
 		}
 		return -1;
 	}
 
 	/**
 	 * @param t
 	 * @return a string to represent that data type
 	 */
 	public static String getTypeName(final Datatype t) {
 		return getTypeName(t, 1, false);
 	}
 
 	/**
 	 * @param t
 	 * @param isize
 	 * @param isComplex
 	 * @return a string to represent that data type
 	 */
 	public static String getTypeName(final Datatype t, int isize, boolean isComplex) {
 		final int dsize = t.getDatatypeSize();
 		if (isComplex && isize == 2) {
 			switch (dsize) {
 			case 8:
 				return "COMPLEX64";
 			case 16:
 				return "COMPLEX128";
 			}
 		}
 
 		String name = null;
 		switch (t.getDatatypeClass()) {
 		case Datatype.CLASS_CHAR:
 		case Datatype.CLASS_INTEGER:
 			String header = t.isUnsigned() ? "U" : "";
 			switch (dsize) {
 			case 1:
 				name = header + "INT8";
 				break;
 			case 2:
 				name = header + "INT16";
 				break;
 			case 4:
 				name = header + "INT32";
 				break;
 			case 8:
 				name = header + "INT64";
 				break;
 			}
 			break;
 		case Datatype.CLASS_FLOAT:
 			switch (dsize) {
 			case 4:
 				name = "FLOAT32";
 				break;
 			case 8:
 				name = "FLOAT64";
 				break;
 			}
 			break;
 		case Datatype.CLASS_STRING:
 			name = "STRING";
 		}
 
 		if (name == null)
 			name = t.getDatatypeDescription();
 
 		return isize == 1 ? name : "Composite of " + isize + " " + name;
 	}
 
 	/**
 	 * Create a dataset from the given data object
 	 * @param data
 	 * @param shape
 	 * @param dtype
 	 * @param extend true dataset for unsigned types 
 	 * @return dataset
 	 */
 	public static AbstractDataset createDataset(final Object data, final int[] shape, final int dtype,
 			final boolean extend) {
 		AbstractDataset ds = null;
 		switch (dtype) {
 		case AbstractDataset.FLOAT32:
 			float[] fData = (float[]) data;
 			ds = new FloatDataset(fData, shape);
 			break;
 		case AbstractDataset.FLOAT64:
 			double[] dData = (double[]) data;
 			ds = new DoubleDataset(dData, shape);
 			break;
 		case AbstractDataset.INT8:
 			byte[] bData = (byte[]) data;
 			ds = new ByteDataset(bData, shape);
 			break;
 		case AbstractDataset.INT16:
 			short[] sData = (short[]) data;
 			ds = new ShortDataset(sData, shape);
 			break;
 		case AbstractDataset.INT32:
 			int[] iData = (int[]) data;
 			ds = new IntegerDataset(iData, shape);
 			break;
 		case AbstractDataset.INT64:
 			long[] lData = (long[]) data;
 			ds = new LongDataset(lData, shape);
 			break;
 		case AbstractDataset.STRING:
 			String[] strData = (String[]) data;
 			ds = new StringDataset(strData, shape);
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown or unsupported dataset type");
 		}
 
 		if (extend) {
 			switch (dtype) {
 			case AbstractDataset.INT32:
 				ds = new LongDataset(ds);
 				DatasetUtils.unwrapUnsigned(ds, 32);
 				break;
 			case AbstractDataset.INT16:
 				ds = new IntegerDataset(ds);
 				DatasetUtils.unwrapUnsigned(ds, 16);
 				break;
 			case AbstractDataset.INT8:
 				ds = new ShortDataset(ds);
 				DatasetUtils.unwrapUnsigned(ds, 8);
 				break;
 			}
 		}
 		return ds;
 	}
 
 	private static class CompositeDatatype {
 		public int tclass;
 		public int size;
 		public boolean isComplex = false;
 	}
 
 	private static CompositeDatatype findClassesInComposite(int tid) throws HDF5LibraryException {
 		List<String> names = new ArrayList<String>();
 		List<Integer> classes = new ArrayList<Integer>();
 		flattenCompositeDatatype(tid, "", names, classes);
 		CompositeDatatype comp = new CompositeDatatype();
 		comp.size = classes.size();
 		if (comp.size > 0) {
 			comp.tclass = classes.get(0);
 			for (int i = 1; i < comp.size; i++) {
 				if (comp.tclass != classes.get(i)) {
 					logger.error("Could not load inhomogeneous compound datatype");
 					return null;
 				}
 			}
 			if (comp.size == 2 && comp.tclass == HDF5Constants.H5T_FLOAT) {
 				if (names.get(0).toLowerCase().startsWith("r") && names.get(1).toLowerCase().startsWith("i")) {
 					comp.isComplex = true;
 				}
 			}
 		}
 		return comp;
 	}
 
 	/**
 	 * This flattens compound and array
 	 * @param tid
 	 * @param prefix
 	 * @param names
 	 * @param classes
 	 * @throws HDF5LibraryException
 	 */
 	private static void flattenCompositeDatatype(int tid, String prefix, List<String> names, List<Integer> classes) throws HDF5LibraryException {
 		int tclass = H5.H5Tget_class(tid);
 		if (tclass == HDF5Constants.H5T_ARRAY) {
 			int btid = -1;
 			try {
 				btid = H5.H5Tget_super(tid);
 				tclass = H5.H5Tget_class(btid);
 				// deal with array of composite
 				if (tclass == HDF5Constants.H5T_COMPOUND || tclass == HDF5Constants.H5T_ARRAY) {
 					flattenCompositeDatatype(btid, prefix, names, classes);
 					return;
 				}
 				int r = H5.H5Tget_array_ndims(tid);
 				long[] shape = new long[r];
 				H5.H5Tget_array_dims(tid, shape);
 				long size = calcLongSize(shape);
 				for (long i = 0; i < size; i++) {
 					names.add(prefix + i);
 					classes.add(tclass);
 				}
 			} catch (HDF5Exception ex) {
 			} finally {
 				H5.H5Tclose(btid);
 			}
 		} else {
 			int n = H5.H5Tget_nmembers(tid);
 			if (n <= 0)
 				return;
 	
 			int mtype = 0;
 			int mclass = 0;
 			int tmptid = 0;
 			for (int i = 0; i < n; i++) {
 				try {
 					mtype = H5.H5Tget_member_type(tid, i);
 				} catch (Exception ex) {
 					continue;
 				}
 
 				try {
 					tmptid = mtype;
 					mtype = H5.H5Tget_native_type(tmptid);
 				} catch (HDF5Exception ex) {
 					continue;
 				} finally {
 					try {
 						H5.H5Tclose(tmptid);
 					} catch (HDF5Exception ex) {
 					}
 				}
 
 				try {
 					mclass = H5.H5Tget_class(mtype);
 				} catch (HDF5Exception ex) {
 					continue;
 				}
 
 				String mname = prefix + H5.H5Tget_member_name(tid, i);
 				if (mclass == HDF5Constants.H5T_COMPOUND || mclass == HDF5Constants.H5T_ARRAY) {
 					// deal with composite
 					flattenCompositeDatatype(mtype, mname, names, classes);
 				} else if (mclass == HDF5Constants.H5T_VLEN) {
 					continue;
 				} else {
 					names.add(mname);
 					classes.add(mclass);
 				}
 			}
 		}
 	}
 
 	public static long calcLongSize(final long[] shape) {
 		double dsize = 1.0;
 		for (int i = 0; i < shape.length; i++) {
 			// make sure the indexes isn't zero or negative
 			if (shape[i] == 0) {
 				return 0;
 			} else if (shape[i] < 0) {
 				throw new IllegalArgumentException(String.format(
 						"The %d-th is %d which is an illegal argument as it is negative", i, shape[i]));
 			}
 	
 			dsize *= shape[i];
 		}
 	
 		// check to see if the size is larger than an integer, i.e. we can't allocate it
 		if (dsize > Long.MAX_VALUE) {
 			throw new IllegalArgumentException("Size of the dataset is too large to allocate");
 		}
 		return (long) dsize;
 	}
 
 	/**
 	 * Create a lazy dataset from a H5 scalar dataset
 	 * @param host
 	 * @param osd
 	 * @param keepBitWidth
 	 * @return the dataset
 	 * @throws Exception
 	 */
 	public static ILazyDataset createLazyDataset(final String host, final H5ScalarDS osd, final boolean keepBitWidth) throws Exception {
 		
 		long[] dims = osd.getDims();
 		if (dims == null) {
 			osd.init();
 			dims = osd.getDims();
 		}
 
 		final int[] trueShape = new int[dims.length];
 		for (int i = 0; i < dims.length; i++) {
 			long d = dims[i];
 			if (d > Integer.MAX_VALUE) {
 				throw new IllegalArgumentException("Dimension larger than ints");
 			}
 			trueShape[i] = (int) d;
 		}
 
 		if (trueShape.length == 1 && trueShape[0] == 1) { // special case for single values
 			final AbstractDataset d = AbstractDataset.array(osd.read());
 			d.setName(osd.getName());
 			return d;
 		}
 
 		final String filePath = osd.getFile();
 		final String nodePath = osd.getFullName();
 		final String name = osd.getName();
 		final Datatype type = osd.getDatatype();
 		final boolean extendUnsigned = !keepBitWidth && type.isUnsigned();
 		final int dtype = getDtype(type.getDatatypeClass(), type.getDatatypeSize());
 
 		ILazyLoader l = new ILazyLoader() {
 			@Override
 			public boolean isFileReadable() {
 				try {
 					if (host != null && host.length() > 0 && !host.equals(InetAddress.getLocalHost().getHostName()))
 						return false;
 				} catch (UnknownHostException e) {
 					logger.warn("Problem finding local host so ignoring check", e);
 				}
 				return new File(filePath).canRead();
 			}
 
 			@Override
 			public String toString() {
 				return filePath + ":" + nodePath;
 			}
 
 			@Override
 			public AbstractDataset getDataset(IMonitor mon, int[] shape, int[] start, int[] stop, int[] step)
 					throws ScanFileHolderException {
 				final int rank = shape.length;
 				int[] lstart, lstop, lstep;
 
 				if (step == null) {
 					lstep = new int[rank];
 					for (int i = 0; i < rank; i++) {
 						lstep[i] = 1;
 					}
 				} else {
 					lstep = step;
 				}
 
 				if (start == null) {
 					lstart = new int[rank];
 				} else {
 					lstart = start;
 				}
 
 				if (stop == null) {
 					lstop = new int[rank];
 				} else {
 					lstop = stop;
 				}
 
 				int[] newShape = AbstractDataset.checkSlice(shape, start, stop, lstart, lstop, lstep);
 
 				AbstractDataset d = null;
 				try {
 					if (!Arrays.equals(trueShape, shape)) {
 						final int trank = trueShape.length;
 						int[] tstart = new int[trank];
 						int[] tsize = new int[trank];
 						int[] tstep = new int[trank];
 
 						if (rank > trank) { // shape was extended (from left) then need to translate to true slice
 							int j = 0;
 							for (int i = 0; i < trank; i++) {
 								if (trueShape[i] == 1) {
 									tstart[i] = 0;
 									tsize[i] = 1;
 									tstep[i] = 1;
 								} else {
 									while (shape[j] == 1 && (rank - j) > (trank - i))
 										j++;
 
 									tstart[i] = lstart[j];
 									tsize[i] = newShape[j];
 									tstep[i] = lstep[j];
 									j++;
 								}
 							}
 						} else { // shape was squeezed then need to translate to true slice
 							int j = 0;
 							for (int i = 0; i < trank; i++) {
 								if (trueShape[i] == 1) {
 									tstart[i] = 0;
 									tsize[i] = 1;
 									tstep[i] = 1;
 								} else {
 									tstart[i] = lstart[j];
 									tsize[i] = newShape[j];
 									tstep[i] = lstep[j];
 									j++;
 								}
 							}
 						}
 
 						d = loadData(filePath, nodePath, tstart, tsize, tstep, dtype, extendUnsigned);
 						d.setShape(newShape); // squeeze shape back
 					} else {
 						d = loadData(filePath, nodePath, lstart, newShape, lstep, dtype, extendUnsigned);
 					}
 					if (d != null) {
 						d.setName(name);
 					}
 				} catch (Exception e) {
 					throw new ScanFileHolderException("Problem with HDF library", e);
 				}
 				return d;
 			}
 		};
 
 		return new LazyDataset(name, dtype, trueShape.clone(), l);
 	}
 
 	/**
 	 * Create a lazy dataset from given dataset and datatype IDs
 	 * @param file
	 * @param dataset
 	 * @param nodePath full node path
 	 * @param name
 	 * @param did
 	 * @param tid
 	 * @param keepBitWidth
 	 * @param useExternalFiles
 	 * @return true if created
 	 * @throws Exception
 	 */
 	private static boolean createLazyDataset(final HDF5File file, final HDF5Dataset dataset,
 			final String nodePath, final String name, final int did, final int tid,
 			final boolean keepBitWidth, final boolean useExternalFiles) throws Exception {
 		int sid = -1, pid = -1;
 		int rank;
 		boolean isText, isVLEN, isUnsigned = false;
 //		boolean isEnum, isRegRef, isNativeDatatype;
 		long[] dims;
 		Datatype type;
 		final int[] trueShape;
 		CompositeDatatype tcomp = null;
 
 		try {
 //			Thread.sleep(200);
 			sid = H5.H5Dget_space(did);
 
 			int tclass = H5.H5Tget_class(tid);
 			rank = H5.H5Sget_simple_extent_ndims(sid);
 
 			if (tclass == HDF5Constants.H5T_ARRAY || tclass == HDF5Constants.H5T_COMPOUND) {
 				tcomp = findClassesInComposite(tid);
 				if (tcomp == null) {
 					logger.error("Composite datatype not homogeneous");
 					return false;
 				}
 			}
 			isText = tclass == HDF5Constants.H5T_STRING;
 			isVLEN = tclass == HDF5Constants.H5T_VLEN || H5.H5Tis_variable_str(tid);
 			isUnsigned = H5Datatype.isUnsigned(tid);
 //			isEnum = tclass == HDF5Constants.H5T_ENUM;
 //			isRegRef = H5.H5Tequal(tid, HDF5Constants.H5T_STD_REF_DSETREG);
 
 			// check if it is an external dataset
 			try {
 				pid = H5.H5Dget_create_plist(did);
 				int nfiles = H5.H5Pget_external_count(pid);
 				if (nfiles > 0) {
 					logger.error("Zero external files");
 					return false;
 				}
 			} catch (HDF5Exception ex) {
 				logger.error("Could not get external count");
 			} finally {
 				try {
 					H5.H5Pclose(pid);
 				} catch (HDF5Exception ex) {
 				}
 			}
 
 			// check if datatype in file is native datatype
 			int tmptid = 0;
 			try {
 				tmptid = H5.H5Tget_native_type(tid);
 				if (!H5.H5Tequal(tid, tmptid)) {
 					type = new H5Datatype(tmptid);
 				} else {
 					type = new H5Datatype(tid);
 				}
 			} catch (HDF5Exception ex) {
 				logger.error("Could not get dataset type");
 				return false;
 			} finally {
 				try {
 					H5.H5Tclose(tmptid);
 				} catch (HDF5Exception ex) {
 				}
 			}
 
 			if (rank == 0) {
 				// a single data point
 				rank = 1;
 				dims = new long[1];
 				dims[0] = 1;
 				dataset.setMaxShape(dims);
 			} else {
 				dims = new long[rank];
 				long[] maxDims = new long[rank];
 				H5.H5Sget_simple_extent_dims(sid, dims, maxDims);
 				dataset.setMaxShape(maxDims);
 			}
 		} catch (HDF5Exception ex) {
 			logger.error("Could not get data space information", ex);
 			return false;
 		} finally {
 			try {
 				H5.H5Sclose(sid);
 			} catch (HDF5Exception ex2) {
 			}
 		}
 
 		trueShape = new int[dims.length];
 		for (int i = 0; i < dims.length; i++) {
 			long d = dims[i];
 			if (d > Integer.MAX_VALUE) {
 				throw new IllegalArgumentException("Dimension larger than ints");
 			}
 			trueShape[i] = (int) d;
 		}
 
 		if (trueShape.length == 1 && trueShape[0] == 1) { // special case for single values
 			try {
 				Object data;
 				try {
 					data = H5Datatype.allocateArray(tid, 1);
 				} catch (OutOfMemoryError err) {
 					throw new HDF5Exception("Out Of Memory");
 				}
 
 				boolean isREF = H5.H5Tequal(tid, HDF5Constants.H5T_STD_REF_OBJ);
 				if (isVLEN) {
 					H5.H5DreadVL(did, tid, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, (Object[]) data);
 				} else {
 					H5.H5Dread(did, tid, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, data);
 
 					if (isText) {
 						data = Dataset.byteToString((byte[]) data, H5.H5Tget_size(tid));
 					} else if (isREF) {
 						data = HDFNativeData.byteToLong((byte[]) data);
 					}
 				}
 				final AbstractDataset d = AbstractDataset.array(data);
 				d.setName(name);
 				dataset.setDataset(d);
 				return true;
 			} catch (HDF5Exception ex) {
 				logger.error("Could not read single value dataset", ex);
 				return false;
 			}
 		}
 
 		final boolean extendUnsigned = !keepBitWidth && isUnsigned;
 		final int isize = tcomp == null ? 1 : tcomp.size;
 		final int dtype;
 		if (tcomp == null) {
 			dtype = getDtype(type.getDatatypeClass(), type.getDatatypeSize(), false);
 			dataset.setTypeName(getTypeName(type));
 		} else {
 			dtype = getDtype(tcomp.tclass, type.getDatatypeSize()/isize, tcomp.isComplex);
 			dataset.setTypeName(getTypeName(type, isize, tcomp.isComplex));
 		}
 
 		// cope with external files specified in a non-standard way and which may not be HDF5 either
 		if (dtype == AbstractDataset.STRING && useExternalFiles) {
 			// interpret set of strings as the full path names to a group of external files that are stacked together
 			if (!isVLEN && !isText) {
 				logger.error("String dataset not variable length or text!");
 				return false;
 			}
 				
 			StringDataset ef = extractExternalFileNames(did, tid, isVLEN, trueShape);
 			ImageStackLoaderEx loader;
 			try {
 				loader = new ImageStackLoaderEx(ef, null);
 			} catch (Exception e) {
 				try { // try again with known-to-be-good directory
 					loader = new ImageStackLoaderEx(ef, file.getParentDirectory());
 				} catch (Exception e2) {
 					logger.error("Could creating loader from external files", e2);
 					return false;
 				}
 			}
 			dataset.setDataset(new LazyDataset(name, loader.getDtype(), loader.getShape(), loader));
 			return true;
 		}
 
 		// check for zero-sized datasets
 		long trueSize = AbstractDataset.calcLongSize(trueShape);
 		if (trueSize == 0) {
 			dataset.setEmpty();
 			return true;
 		}
 
 		final String host = file.getHostname();
 		final String filePath = file.getFilename();
 
 		ILazyLoader l = new ILazyLoader() {
 			@Override
 			public boolean isFileReadable() {
 				try {
 					if (host != null && host.length() > 0 && !host.equals(InetAddress.getLocalHost().getHostName()))
 						return false;
 				} catch (UnknownHostException e) {
 					logger.warn("Problem finding local host so ignoring check", e);
 				}
 				return new File(filePath).canRead();
 			}
 
 			@Override
 			public String toString() {
 				return filePath + ":" + nodePath;
 			}
 
 			@Override
 			public AbstractDataset getDataset(IMonitor mon, int[] shape, int[] start, int[] stop, int[] step)
 					throws ScanFileHolderException {
 				final int rank = shape.length;
 				int[] lstart, lstop, lstep;
 
 				if (step == null) {
 					lstep = new int[rank];
 					for (int i = 0; i < rank; i++) {
 						lstep[i] = 1;
 					}
 				} else {
 					lstep = step;
 				}
 
 				if (start == null) {
 					lstart = new int[rank];
 				} else {
 					lstart = start;
 				}
 
 				if (stop == null) {
 					lstop = new int[rank];
 				} else {
 					lstop = stop;
 				}
 
 				int[] newShape = AbstractDataset.checkSlice(shape, start, stop, lstart, lstop, lstep);
 
 				AbstractDataset d = null;
 				try {
 					if (!Arrays.equals(trueShape, shape)) {
 						final int trank = trueShape.length;
 						int[] tstart = new int[trank];
 						int[] tsize = new int[trank];
 						int[] tstep = new int[trank];
 
 						if (rank > trank) { // shape was extended (from left) then need to translate to true slice
 							int j = 0;
 							for (int i = 0; i < trank; i++) {
 								if (trueShape[i] == 1) {
 									tstart[i] = 0;
 									tsize[i] = 1;
 									tstep[i] = 1;
 								} else {
 									while (shape[j] == 1 && (rank - j) > (trank - i))
 										j++;
 
 									tstart[i] = lstart[j];
 									tsize[i] = newShape[j];
 									tstep[i] = lstep[j];
 									j++;
 								}
 							}
 						} else { // shape was squeezed (and could have been extended again) then need to translate to true slice
 							int j = 0;
 							while (shape[j] == 1 && j < rank)
 								j++;
 
 							for (int i = 0; i < trank; i++) {
 								if (trueShape[i] == 1) {
 									tstart[i] = 0;
 									tsize[i] = 1;
 									tstep[i] = 1;
 								} else {
 									tstart[i] = lstart[j];
 									tsize[i] = newShape[j];
 									tstep[i] = lstep[j];
 									j++;
 								}
 							}
 						}
 
 						d = loadData(filePath, nodePath, tstart, tsize, tstep, dtype, isize, extendUnsigned);
 						d.setShape(newShape); // squeeze shape back
 					} else {
 						d = loadData(filePath, nodePath, lstart, newShape, lstep, dtype, isize, extendUnsigned);
 					}
 					if (d != null) {
 						d.setName(name);
 					}
 				} catch (Exception e) {
 					throw new ScanFileHolderException("Problem loading dataset", e);
 				}
 				return d;
 			}
 		};
 
 		dataset.setDataset(new LazyDataset(name, dtype, isize, trueShape.clone(), l));
 		return true;
 	}
 
 	/**
 	 * Create a stacked dataset from external files
 	 * @param ef
 	 * @return lazy data set from external file
 	 * @throws OutOfMemoryError
 	 * @throws Exception
 	 */
 	public static ILazyDataset createStackedDatasetFromStrings(ExternalFiles ef) throws OutOfMemoryError, Exception {
 		//remove final dimension as that is for the characters of the strings
 		//shape here is for the actual filenames
 		
 		ImageStackLoaderEx loader = new ImageStackLoaderEx(ef.shape, ef.files);
 		return new LazyDataset("file_name", loader.getDtype(), loader.getShape(), loader);
 	}
 
 	/**
 	 * Create a stacked dataset from external files
 	 * @param ef
 	 * @param directory
 	 * @return lazy data set from external file
 	 * @throws OutOfMemoryError
 	 * @throws Exception
 	 */
 	public static ILazyDataset createStackedDatasetFromStrings(ExternalFiles ef, String directory) throws OutOfMemoryError, Exception {
 		//remove final dimension as that is for the characters of the strings
 		//shape here is for the actual filenames
 		
 		ImageStackLoaderEx loader = new ImageStackLoaderEx(ef.shape, ef.files, directory);
 		return new LazyDataset("file_name", loader.getDtype(), loader.getShape(), loader);
 	}
 
 	/**
 	 * Return any External File references to be followed.
 	 * @return string dataset
 	 * @throws Exception
 	 */
 	private static StringDataset extractExternalFileNames(final int did, final int tid, final boolean isVLEN, final int[] shape) throws Exception {
 		int length = 1;
 		for (int i = 0; i < shape.length; i++) {
 			length *= shape[i];
 		}
 
 		Object data;
 		try {
 			data = H5Datatype.allocateArray(tid, length);
 		} catch (OutOfMemoryError err) {
 			throw new HDF5Exception("Out of memory");
 		}
 
 		if (isVLEN) {
 			H5.H5DreadVL(did, tid, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, (Object[]) data);
 		} else {
 			H5.H5Dread(did, tid, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, data);
 			data = Dataset.byteToString((byte[]) data, H5.H5Tget_size(tid));
 		}
 
 		return new StringDataset((String[]) data, AbstractDataset.squeezeShape(shape, false));
 	}
 
 	/**
 	 * Return any External File references to be followed.
 	 * @param sds
 	 * @return ExternalFiles object
 	 * @throws Exception
 	 */
 	public static ExternalFiles extractExternalFileNames(H5ScalarDS sds) throws Exception {
 		sds.init();
 		final long[] dshape = sds.getDims();
 		final long[] dstart = sds.getStartDims();
 		final long[] dstride = sds.getStride();
 		final long[] dsize = sds.getSelectedDims();
 
 		int length = 1;
 		final int[] shape = new int[dshape.length];
 		for (int i = 0; i < dshape.length; i++) {
 			dstride[i] = 1;
 			dsize[i] = 1;
 			int s = (int) dshape[i];
 			shape[i] = s;
 			length *= s;
 		}
 		final String[] files = new String[length];
 		PositionIterator iter = new PositionIterator(shape);
 		final int[] pos = iter.getPos();
 		int index = 0;
 		while (iter.hasNext()) {
 			for (int i = 0; i < shape.length; i++) {
 				dstart[i] = pos[i];
 			}
 			//DO NOT CHANGE THE FOLLOWING UNLESS YOU HAVE A TEST TO PROVE IT IS NOT WORKING. 
 			sds.clear();
 			files[index++] = ((String[]) sds.getData())[0];
 		}
 		ExternalFiles ef= new ExternalFiles();
 		ef.shape = AbstractDataset.squeezeShape(shape, false);
 		ef.files = files;
 		return ef;
 	}
 
 	protected static AbstractDataset loadData(final String fileName, final String node,
 			final int[] start, final int[] count, final int[] step,
 			final int dtype, final boolean extend) throws Exception {
 		return loadData(fileName, node, start, count, step, dtype, 1, extend);
 	}
 
 	protected static AbstractDataset loadData(final String fileName, final String node,
 			final int[] start, final int[] count, final int[] step,
 			final int dtype, final int isize, final boolean extend)
 			throws Exception {
 		AbstractDataset data = null;
 
 		final String cPath;
 		try {
 			cPath = new File(fileName).getCanonicalPath();
 		} catch (IOException e) {
 			logger.error("Could not get canonical path", e);
 			throw new ScanFileHolderException("Could not get canonical path", e);
 		}
 		int fid = -1;
 		try {
 			HierarchicalDataFactory.acquireLowLevelReadingAccess(cPath);
 			fid = H5.H5Fopen(fileName, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
 
 			try {
 				H5O_info_t info = H5.H5Oget_info_by_name(fid, node, HDF5Constants.H5P_DEFAULT);
 				int t = info.type;
 				if (t != HDF5Constants.H5O_TYPE_DATASET) {
 					logger.error("Node {} was not a dataset", node);
 					return data;
 				}
 			} catch (HDF5Exception ex) {
 				logger.error("Could not find info about object {}" + node);
 				return data;
 			}
 
 			int did = -1, tid = -1, tclass = -1;
 			try {
 				did = H5.H5Dopen(fid, node, HDF5Constants.H5P_DEFAULT);
 				tid = H5.H5Dget_type(did);
 
 				tclass = H5.H5Tget_class(tid);
 				if (tclass == HDF5Constants.H5T_ARRAY || tclass == HDF5Constants.H5T_VLEN) {
 					// for ARRAY, the type is determined by the base type
 					int btid = H5.H5Tget_super(tid);
 					tclass = H5.H5Tget_class(btid);
 					try {
 						H5.H5Tclose(btid);
 					} catch (HDF5Exception ex) {
 					}
 				}
 				int sid = -1, pid = -1;
 				int rank;
 				boolean isText, isVLEN; //, isUnsigned = false;
 //				boolean isEnum, isRegRef, isNativeDatatype;
 				long[] dims;
 
 				// create a new scalar dataset
 
 				try {
 					pid = H5.H5Dget_create_plist(did);
 
 					sid = H5.H5Dget_space(did);
 
 					rank = H5.H5Sget_simple_extent_ndims(sid);
 
 					isText = tclass == HDF5Constants.H5T_STRING;
 					isVLEN = tclass == HDF5Constants.H5T_VLEN || H5.H5Tis_variable_str(tid);
 
 					final int ldtype;
 					if (dtype >= 0) {
 						ldtype = dtype;
 					} else {
 						// TODO check if this is actually used
 						Datatype type = new H5Datatype(tid);
 						ldtype = getDtype(type.getDatatypeClass(), type.getDatatypeSize());
 					}
 
 					if (rank == 0) {
 						// a single data point
 						rank = 1;
 						dims = new long[1];
 						dims[0] = 1;
 					} else {
 						dims = new long[rank];
 						H5.H5Sget_simple_extent_dims(sid, dims, null);
 					}
 
 					long[] schunk = null; // source chunking
 
 					try {
 						if (H5.H5Pget_layout(pid) == HDF5Constants.H5D_CHUNKED) {
 							schunk = new long[rank];
 							H5.H5Pget_chunk(pid, rank, schunk);
 						}
 					} catch (HDF5Exception ex) {
 						logger.error("Could not get chunk size");
 						return data;
 					}
 
 					final long[] sstart = new long[rank]; // source start
 					final long[] sstride = new long[rank]; // source steps
 					final long[] dsize = new long[rank]; // destination size
 
 					for (int i = 0; i < rank; i++) {
 						sstart[i] = start[i];
 						sstride[i] = step[i];
 						dsize[i] = count[i];
 					}
 
 					boolean all = false;
 					if (schunk == null) {
 						all = true;
 					} else {
 						if (Arrays.equals(dims, schunk)) {
 							all = true;
 						} else {
 							int j = rank - 1; // find last chunked dimension that is sliced across
 							while (j >= 0) {
 								if (schunk[j] > 1 && dsize[j] <= 1)
 									break;
 								j--;
 							}
 							all = j < 0;
 						}
 					}
 					if (schunk == null || all) {
 						H5.H5Sselect_hyperslab(sid, HDF5Constants.H5S_SELECT_SET, sstart, sstride, dsize, null);
 						int length = 1;
 						for (int i = 0; i < rank; i++)
 							length *= count[i];
 
 						int msid = H5.H5Screate_simple(1, new long[] {length}, null);
 						H5.H5Sselect_all(msid);
 						data = AbstractDataset.zeros(isize, count, ldtype);
 						Object odata = data.getBuffer();
 
 						boolean isREF = H5.H5Tequal(tid, HDF5Constants.H5T_STD_REF_OBJ);
 						if (isVLEN) {
 							H5.H5DreadVL(did, tid, msid, sid, HDF5Constants.H5P_DEFAULT, (Object[]) odata);
 						} else {
 							H5.H5Dread(did, tid, msid, sid, HDF5Constants.H5P_DEFAULT, odata);
 
 							if (odata instanceof byte[] && ldtype != AbstractDataset.INT8) {
 								// TODO check if this is actually used
 								Object idata = null;
 								byte[] bdata = (byte[]) odata;
 								if (isText) {
 									idata = Dataset.byteToString(bdata, H5.H5Tget_size(tid));
 								} else if (isREF) {
 									idata = HDFNativeData.byteToLong(bdata);
 								}
 
 								if (idata != null) {
 									data = createDataset(idata, count, ldtype, false); // extend later, if necessary
 								}
 							}
 						}
 					} else {
 						// read in many split chunks
 						final boolean[] isSplit = new boolean[rank];
 						final long[] send = new long[rank];
 						int length = 1;
 						for (int i = 0; i < rank; i++) {
 							send[i] = sstart[i] + count[i] * step[i];
 							isSplit[i] = schunk[i] <= 1 && dsize[i] > 1;
 							if (isSplit[i]) {
 								dsize[i] = 1;
 							} else {
 								length *= dsize[i];
 							}
 						}
 						if (length == 1) { // if just single point then bulk up request
 							for (int i = rank - 1; i >= 0; i--) {
 								int l = count[i];
 								if (l > 1) {
 									dsize[i] = l;
 									length = l;
 									isSplit[i] = false;
 									break;
 								}
 							}
 						}
 						final List<Integer> notSplit = new ArrayList<Integer>();
 						for (int i = 0; i < rank; i++) {
 							if (!isSplit[i])
 								notSplit.add(i);
 						}
 						final int[] axes = new int[notSplit.size()];
 						for (int i = 0; i < axes.length; i++) {
 							axes[i] = notSplit.get(i);
 						}
 						data = AbstractDataset.zeros(count, ldtype);
 						Object odata;
 						try {
 							odata = H5Datatype.allocateArray(tid, length);
 						} catch (OutOfMemoryError err) {
 							throw new ScanFileHolderException("Out Of Memory", err);
 						}
 						int msid = H5.H5Screate_simple(1, new long[] {length}, null);
 						H5.H5Sselect_all(msid);
 
 						PositionIterator it = data.getPositionIterator(axes);
 						final int[] pos = it.getPos();
 						final boolean[] hit = it.getOmit();
 						while (it.hasNext()) {
 							H5.H5Sselect_hyperslab(sid, HDF5Constants.H5S_SELECT_SET, sstart, sstride, dsize, null);
 							boolean isREF = H5.H5Tequal(tid, HDF5Constants.H5T_STD_REF_OBJ);
 							Object idata;
 							if (isVLEN) {
 								H5.H5DreadVL(did, tid, msid, sid, HDF5Constants.H5P_DEFAULT, (Object[]) odata);
 								idata = odata;
 							} else {
 								H5.H5Dread(did, tid, msid, sid, HDF5Constants.H5P_DEFAULT, odata);
 
 								if (odata instanceof byte[] && ldtype != AbstractDataset.INT8) {
 									// TODO check if this is actually used
 									byte[] bdata = (byte[]) odata;
 									if (isText) {
 										idata = Dataset.byteToString(bdata, H5.H5Tget_size(tid));
 									} else if (isREF) {
 										idata = HDFNativeData.byteToLong(bdata);
 									} else {
 										idata = odata;
 									}
 								} else {
 									idata = odata;
 								}
 							}
 
 							data.setItemsOnAxes(pos, hit, idata);
 							int j = rank - 1;
 							for (; j >= 0; j--) {
 								if (isSplit[j]) {
 									sstart[j] += sstride[j];
 									if (sstart[j] >= send[j]) {
 										sstart[j] = start[j];
 									} else {
 										break;
 									}
 								}
 							}
 							if (j == -1)
 								break;
 						}
 					}
 					if (extend) {
 						switch (ldtype) {
 						case AbstractDataset.INT32:
 							data = new LongDataset(data);
 							DatasetUtils.unwrapUnsigned(data, 32);
 							break;
 						case AbstractDataset.INT16:
 							data = new IntegerDataset(data);
 							DatasetUtils.unwrapUnsigned(data, 16);
 							break;
 						case AbstractDataset.INT8:
 							data = new ShortDataset(data);
 							DatasetUtils.unwrapUnsigned(data, 8);
 							break;
 						}
 					}
 				} catch (HDF5Exception ex) {
 					logger.error("Could not get data space information", ex);
 					return data;
 				} finally {
 					if (sid >= 0) {
 						try {
 							H5.H5Sclose(sid);
 						} catch (HDF5Exception ex2) {
 						}
 					}
 					if (pid >= 0) {
 						try {
 							H5.H5Pclose(pid);
 						} catch (HDF5Exception ex) {
 						}
 					}
 				}
 
 			} catch (HDF5Exception ex) {
 				logger.error("Could not open dataset", ex);
 			} finally {
 				if (tid >= 0) {
 					try {
 						H5.H5Tclose(tid);
 					} catch (HDF5Exception ex) {
 					}
 				}
 				if (did >= 0) {
 					try {
 						H5.H5Dclose(did);
 					} catch (HDF5Exception ex) {
 					}
 				}
 			}
 
 		} catch (Throwable le) {
 			throw new ScanFileHolderException("Problem loading file: " + fileName, le);
 		} finally {
 			try {
 				H5.H5Fclose(fid);
 			} catch (Throwable e) {
 			}
 			HierarchicalDataFactory.releaseLowLevelReadingAccess(cPath);
 		}
 
 		return data;
 	}
 
 	@Override
 	public void loadMetaData(IMonitor mon) throws Exception {
 		loadTree(mon);
 		metadata = (Metadata) createDataHolder(tFile, true).getMetadata();
 		metadata.setFilePath(fileName);
 	}
 
 	private Metadata metadata;
 
 	@Override
 	public IMetaData getMetaData() {
 		return metadata;
 	}
 
 	/**
 	 * Create data holder from tree
 	 * @param tree
 	 * @param withMetadata
 	 */
 	public static DataHolder createDataHolder(HDF5File tree, boolean withMetadata) {
 		DataHolder dh = new DataHolder();
 		if (tree == null)
 			return dh;
 
 		// Change to TreeMap so that order maintained
 		Map<String, ILazyDataset> lMap = new LinkedHashMap<String, ILazyDataset>();
 		Map<String, Serializable> aMap = withMetadata ? new LinkedHashMap<String, Serializable>() : null;
 		addToMaps(tree.getNodeLink(), lMap, aMap);
 
 		if (withMetadata) {
 			Metadata metadata = new Metadata(aMap);
 			metadata.setFilePath(tree.getFilename());
 			for (Entry<String, ILazyDataset> e : lMap.entrySet()) {
 				String n = e.getKey();
 				ILazyDataset l = e.getValue();
 				if (l != null) {
 					dh.addDataset(n, l);
 					metadata.addDataInfo(n, l.getShape());
 					// also add datasets under their local name (if defined) 
 					try {
 							String attr = n.concat("@local_name");
 							Object localattr = metadata.getMetaValue(attr);
 							if (localattr == null)
 								continue;
 							String localname = localattr.toString();
 							if (localname == null || localname.isEmpty())
 								continue;
 							if (lMap.keySet().contains(localname))
 								continue;
 							dh.addDataset(localname, l);
 					} catch (Exception ex) {
 						logger.info("seen an exception populating local nexus name for "+n, ex);
 					}
 				}
 			}
 			dh.setMetadata(metadata);
 		} else {
 			for (Entry<String, ILazyDataset> e : lMap.entrySet()) {
 				dh.addDataset(e.getKey(), e.getValue());
 			}
 		}
 		return dh;
 	}
 
 	/**
 	 * Adds lazy datasets and the attributes and scalar dataset items in a node to the given maps recursively
 	 * @param link - link to node to investigate
 	 * @param lMap - the lazy dataset map to add items to, to aid the recursive method
 	 * @param aMap - the attribute map to add items to, to aid the recursive method (can be null)
 	 */
 	private static void addToMaps(HDF5NodeLink link, Map<String, ILazyDataset> lMap, Map<String, Serializable> aMap) {
 		HDF5Node node = link.getDestination();
 		if (aMap != null) {
 			Iterator<String> iter = node.getAttributeNameIterator();
 			String name = link.getFullName() + HDF5Node.ATTRIBUTE;
 			while (iter.hasNext()) {
 				String attr = iter.next();
 				aMap.put(name + attr, node.getAttribute(attr).getFirstElement());
 			}
 		}
 
 		if (node instanceof HDF5Group) {
 			for (HDF5NodeLink l : (HDF5Group) node) {
 				addToMaps(l, lMap, aMap);
 
 				if (l.isDestinationADataset()) {
 					HDF5Dataset d = (HDF5Dataset) l.getDestination();
 					ILazyDataset dataset = d.getDataset();
 					lMap.put(l.getFullName(), dataset);
 					if (aMap != null && dataset instanceof AbstractDataset) { // zero-rank dataset
 						AbstractDataset a = (AbstractDataset) dataset;
 						aMap.put(l.getFullName(), a.getRank() == 0 ? a.getString() : a.getString(0));
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Find all datasets of given names at given depth
 	 * @param names
 	 * @param depth
 	 * @param mon
 	 * @return list of datasets
 	 * @throws ScanFileHolderException 
 	 */
 	public List<ILazyDataset> findDatasets(final String[] names, final int depth, IMonitor mon) throws ScanFileHolderException {
 		ArrayList<ILazyDataset> list = new ArrayList<ILazyDataset>();
 
 		try {
 			fileName = new File(fileName).getCanonicalPath();
 		} catch (IOException e) {
 			logger.error("Could not get canonical path", e);
 			throw new ScanFileHolderException("Could not get canonical path", e);
 		}
 		int fid = -1;
 		try {
 			HierarchicalDataFactory.acquireLowLevelReadingAccess(fileName);
 			fid = H5.H5Fopen(fileName, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
 
 			if (!monitorIncrement(mon)) {
 				try {
 					H5.H5Fclose(fid);
 				} catch (HDF5Exception ex) {
 				}
 				return null;
 			}
 
 			final long oid = fileName.hashCode(); // include file name in ID
 			HDF5File f = new HDF5File(oid, fileName);
 			f.setHostname(host);
 
 			visitGroup(fid, f, HDF5File.ROOT, Arrays.asList(names), 0, depth, list);
 
 		} catch (Throwable le) {
 			throw new ScanFileHolderException("Problem loading file: " + fileName, le);
 		} finally {
 			try {
 				H5.H5Fclose(fid);
 			} catch (Throwable e) {
 			}
 			HierarchicalDataFactory.releaseLowLevelReadingAccess(fileName);
 		}
 		return list;
 	}
 
 	private void visitGroup(final int fid, final HDF5File f, final String name, final List<String> names, int cDepth, int rDepth, ArrayList<ILazyDataset> list) throws Exception {
 		int gid = -1;
 
 		try {
 			int nelems = 0;
 			try {
 				gid = H5.H5Gopen(fid, name, HDF5Constants.H5P_DEFAULT);
 				H5G_info_t info = H5.H5Gget_info(gid);
 				nelems = (int) info.nlinks;
 			} catch (HDF5Exception ex) {
 				throw new ScanFileHolderException("Could not open group", ex);
 			}
 
 			byte[] idbuf = null;
 			long oid = -1;
 			try {
 				idbuf = H5.H5Rcreate(fid, name, HDF5Constants.H5R_OBJECT, -1);
 				oid = HDFNativeData.byteToLong(idbuf, 0);
 			} catch (HDF5Exception ex) {
 				throw new ScanFileHolderException("Could not find group reference", ex);
 			}
 
 			if (nelems <= 0) {
 				return;
 			}
 
 			int[] oTypes = new int[nelems];
 			int[] lTypes = new int[nelems];
 			long[] oids = new long[nelems];
 			String[] oNames = new String[nelems];
 			try {
 				H5.H5Gget_obj_info_all(fid, name, oNames, oTypes, lTypes, oids, HDF5Constants.H5_INDEX_NAME);
 			} catch (HDF5Exception ex) {
 				logger.error("Could not get objects info in group", ex);
 				return;
 			}
 
 			String oname;
 			int otype;
 			int ltype;
 
 			// Iterate through the file to see members of the group
 			for (int i = 0; i < nelems; i++) {
 				oname = oNames[i];
 				if (oname == null) {
 					continue;
 				}
 				otype = oTypes[i];
 				ltype = lTypes[i];
 				oid = oids[i];
 
 				if (ltype == HDF5Constants.H5L_TYPE_HARD) {
 					if (otype == HDF5Constants.H5O_TYPE_GROUP && cDepth < rDepth) {
 						// System.err.println("G: " + oname);
 						visitGroup(fid, f, name + oname + HDF5Node.SEPARATOR, names,
 								cDepth + 1, rDepth, list);
 					} else if (otype == HDF5Constants.H5O_TYPE_DATASET && cDepth == rDepth) {
 						// System.err.println("D: " + oname);
 						if (names.contains(oname)) {
 							
 						int did = -1, tid = -1;
 						try {
 							did = H5.H5Dopen(gid, oname, HDF5Constants.H5P_DEFAULT);
 							tid = H5.H5Dget_type(did);
 
 							// create a new dataset
 							HDF5Dataset d = new HDF5Dataset(oid);
 							if (!createLazyDataset(f, d, name + oname, oname, did, tid, keepBitWidth,
 									d.containsAttribute(DATA_FILENAME_ATTR_NAME))) {
 								logger.error("Could not create a lazy dataset {} from {}", oname, name);
 								continue;
 							}
 							if (d.getDataset() != null)
 								list.add(d.getDataset());
 						} catch (HDF5Exception ex) {
 							logger.error(String.format("Could not open dataset (%s) %s in %s", name, oname, f), ex);
 						} finally {
 							if (tid >= 0) {
 								try {
 									H5.H5Tclose(tid);
 								} catch (HDF5Exception ex) {
 								}
 							}
 							if (did >= 0) {
 								try {
 									H5.H5Dclose(did);
 								} catch (HDF5Exception ex) {
 								}
 							}
 						}
 						}
 					} else if (otype == HDF5Constants.H5O_TYPE_NAMED_DATATYPE) {
 						logger.error("Named datatype not supported"); // TODO
 					}
 				} else if (ltype == HDF5Constants.H5L_TYPE_SOFT) {
 					// System.err.println("S: " + oname);
 				} else if (ltype == HDF5Constants.H5L_TYPE_EXTERNAL) {
 					// System.err.println("E: " + oname);
 				} else {
 					// System.err.println("U: " + oname);
 				}
 			}
 		} finally {
 			if (gid >= 0) {
 				try {
 					H5.H5Gclose(gid);
 				} catch (HDF5Exception ex) {
 				}
 			}
 		}
 	}
 	
 	protected AbstractDataset slice(SliceObject object, @SuppressWarnings("unused") IMonitor mon) throws Exception {
 		final int[] start = object.getSliceStart();
 		final int[] stop = object.getSliceStop();
 		final int[] step = object.getSliceStep();
 		int[] lstart, lstop, lstep;
 		int rank;
 		if (start != null)
 			rank = start.length;
 		else if (stop != null)
 			rank = stop.length;
 		else if (step != null)
 			rank = step.length;
 		else
 			throw new IllegalArgumentException("Slice object does not have any info about rank");
 
 		if (step == null) {
 			lstep = new int[rank];
 			for (int i = 0; i < rank; i++) {
 				lstep[i] = 1;
 			}
 		} else {
 			lstep = step;
 		}
 
 		if (start == null) {
 			lstart = new int[rank];
 		} else {
 			lstart = start;
 		}
 
 		if (stop == null) {
 			lstop = object.getSlicedShape();
 			if (lstop == null)
 				lstop = object.getFullShape();
 		} else {
 			lstop = stop;
 		}
 
 		if (lstop == null)
 			throw new IllegalArgumentException("Slice object does not have any info about stop or shape");
 
 		int[] newShape = new int[rank];
 
 		for (int i = 0; i < rank; i++) {
 			if (lstep[i] > 0) {
 				newShape[i] = (lstop[i] - lstart[i] - 1) / lstep[i] + 1;
 			} else {
 				newShape[i] = (lstop[i] - lstart[i] + 1) / lstep[i] + 1;
 			}
 		}
 
 		return loadData(object.getPath(), object.getName(), lstart, newShape, lstep, -1, true);
 	}
 }
