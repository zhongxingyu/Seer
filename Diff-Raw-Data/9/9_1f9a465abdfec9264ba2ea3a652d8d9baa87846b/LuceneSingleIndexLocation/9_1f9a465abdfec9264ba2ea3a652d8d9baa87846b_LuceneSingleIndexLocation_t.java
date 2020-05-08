 package com.gentics.cr.lucene.indexer.index;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.store.AlreadyClosedException;
 import org.apache.lucene.store.Directory;
 
 import com.gentics.cr.CRConfig;
 import com.gentics.cr.lucene.facets.taxonomy.TaxonomyConfigKeys;
 import com.gentics.cr.lucene.facets.taxonomy.taxonomyaccessor.TaxonomyAccessor;
 import com.gentics.cr.lucene.facets.taxonomy.taxonomyaccessor.TaxonomyAccessorFactory;
 import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
 import com.gentics.cr.lucene.indexaccessor.IndexAccessorFactory;
 
 /**
  * @author Christopher
  *
  */
 public class LuceneSingleIndexLocation extends LuceneIndexLocation implements TaxonomyConfigKeys {
 	//Instance Members
 	/**
 	 * Directory.
 	 */
 	private Directory dir = null;
 	/**
 	 * Location.
 	 */
 	private String indexLocation;
 
 	/**
 	 * Timestamp to store the lastmodified value of the reopen file.
 	 */
 	private long lastmodifiedStored = 0;
 
 	private String taxonomyLocation;
 	private Directory taxonomyDir = null;
 
 	/**
 	 * Create a new Instance of LuceneSingleIndexLocation. 
 	 * This is the Default IndexLocation for Lucene.
 	 * @param config configuration
 	 */
 	public LuceneSingleIndexLocation(final CRConfig config) {
 		super(config);
 		indexLocation = getFirstIndexLocation(config);
 		dir = createDirectory(indexLocation, config);
 		//Create index accessor
 		IndexAccessorFactory iAFactory = IndexAccessorFactory.getInstance();
 		if (!iAFactory.hasAccessor(dir)) {
 			try {
 				iAFactory.createAccessor(dir, getConfiguredAnalyzer());
 			} catch (IOException ex) {
 				log.fatal("COULD NOT CREATE INDEX ACCESSOR" + ex.getMessage());
 			}
 		} else {
 			log.debug("Accessor already present.");
 		}
 
 		// check if facets are activated and create a TaxonomyAccessor if necessary
 		useFacets = config.getBoolean(FACET_FLAG_KEY, useFacets);
 		if (useFacets) {
 			log.debug("Facets are active");
 			taxonomyLocation = retrieveTaxonomyLocation(config);
 			taxonomyDir = createDirectory(taxonomyLocation, config);
 			TaxonomyAccessorFactory taFactory = TaxonomyAccessorFactory.getInstance();
 			if (!taFactory.hasAccessor(taxonomyDir)) {
 				try {
 					taFactory.createAccessor(config, taxonomyDir);
 				} catch (IOException e) {
 					log.fatal("COULD NOT CREATE TAXONOMY ACCESSOR" + e.getMessage());
 				}
 			} else {
 				log.debug("TaxonomyAccessor already present.");
 			}
 		} else {
 			log.debug("Facets are not active");
 		}
 	}
 
 	/**
 	 * Does not reopen the IndexAccessorFactory if already closed.
 	 */
 	@Override
 	protected final IndexAccessor getAccessorInstance() {
 		return getAccessorInstance(false);
 	}
 
 	@Override
 	protected final IndexAccessor getAccessorInstance(final boolean reopenClosedFactory) {
 		Directory directory = this.getDirectory();
 
 		IndexAccessor indexAccessor = null;
 		try {
 			indexAccessor = IndexAccessorFactory.getInstance().getAccessor(directory);
 		} catch (AlreadyClosedException e) {
 			if (reopenClosedFactory) {
 				IndexAccessorFactory.getInstance().reopen();
				indexAccessor = IndexAccessorFactory.getInstance().getAccessor(directory);
 			} else {
 				throw e;
 			}
 		}
 		return indexAccessor;
 	}
 
 	@Override
 	public final int getDocCount() {
 		IndexAccessor indexAccessor = this.getAccessor();
 		IndexReader reader = null;
 		int count = 0;
 		try {
 			reader = indexAccessor.getReader(false);
 			count = reader.numDocs();
 		} catch (IOException ex) {
 			log.error("IOException happened during test of index. ", ex);
 		} finally {
 			indexAccessor.release(reader, false);
 		}
 
 		return count;
 	}
 
 	@Override
 	protected final Directory[] getDirectories() {
 		Directory[] dirs = new Directory[] { this.getDirectory() };
 		return dirs;
 	}
 
 	/**
 	 * getDirectory.
 	 * @return directory.
 	 */
 	private Directory getDirectory() {
 		return this.dir;
 	}
 
 	/**
 	 * Returns the filename of the reopen file.
 	 * @return filename of the reopen file.
 	 */
 	public final String getReopenFilename() {
 		return this.indexLocation + "/" + REOPEN_FILENAME;
 	}
 
 	/**
 	 * Creates the reopen file to make portlet reload the index.
 	 */
 	public final void createReopenFile() {
 		boolean writeReopenFile = config.getBoolean("writereopenfile");
 		if (writeReopenFile) {
 			String filename = this.getReopenFilename();
 			log.debug("Writing reopen to " + filename);
 			try {
 				File reopenFile = new File(filename);
 				FileUtils.touch(reopenFile);
 			} catch (IOException e) {
 				log.warn("Cannot create reopen file! " + e);
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public final boolean reopenCheck(final IndexAccessor indexAccessor, final TaxonomyAccessor taxonomyAccessor) {
 		boolean reopened = false;
 		if (reopencheck) {
 			try {
 				log.debug("Check for reopen file at " + this.getReopenFilename());
 				File reopenFile = new File(this.getReopenFilename());
 				if (reopenFile.exists()) {
 					if (reopencheckTimestamp) {
 						long lastmodified = reopenFile.lastModified();
 						if (lastmodified != lastmodifiedStored) {
 							lastmodifiedStored = lastmodified;
 							indexAccessor.reopen();
 							if (taxonomyAccessor != null) {
 								taxonomyAccessor.refresh();
 							}
 							reopened = true;
 							log.debug("Reopened index because reopen file has " + "changed");
 						} else {
 							log.debug("Do not reopen index because reopen file " + "hasn't changed.\n" + lastmodified + " == "
 									+ lastmodifiedStored);
 						}
 					} else {
 						reopenFile.delete();
 						indexAccessor.reopen();
 						if (taxonomyAccessor != null) {
 							taxonomyAccessor.refresh();
 						}
 						reopened = true;
 						log.debug("Reopened index because of simple " + "reopencheck.");
 					}
 				} else {
 					log.debug("Reopen file not found.");
 				}
 			} catch (Exception ex) {
 				log.error(ex.getMessage(), ex);
 			}
 		}
 		return reopened;
 	}
 
 	@Override
 	public final long indexSize() {
 		File indexLocationFile = new File(indexLocation);
 		long directorySize = FileUtils.sizeOfDirectory(indexLocationFile);
 		//TODO add caching
 		return directorySize;
 	}
 
 	@Override
 	public final Date lastModified() {
 		File reopenfile = new File(getReopenFilename());
 		if (reopenfile.exists()) {
 			return new Date(reopenfile.lastModified());
 		} else {
 			File directory = reopenfile.getParentFile();
 			if (directory.exists()) {
 				return new Date(directory.lastModified());
 			} else {
 				return new Date(0);
 			}
 		}
 	}
 
 	/**
 	 * Checks if the index is optimized.
 	 * @return true if it is optimized
 	 */
 	public final boolean isOptimized() {
 		boolean ret = false;
 		IndexAccessor indexAccessor = this.getAccessor();
 		IndexReader reader = null;
 		try {
 			reader = indexAccessor.getReader(false);
 			ret = reader.isOptimized();
 		} catch (IOException ex) {
 			log.error("IOException happened during test of index. ", ex);
 		} finally {
 			indexAccessor.release(reader, false);
 		}
 
 		return ret;
 	}
 
 	@Override
 	public final boolean isLocked() {
 		boolean locked = false;
 		IndexAccessor indexAccessor = this.getAccessor();
 		locked = indexAccessor.isLocked();
 		return locked;
 	}
 
 	/**
 	 * Returns the hashCode of the LockID of the used Directory.
 	 */
 	@Override
 	public int hashCode() {
 		return dir.getLockID().hashCode();
 	}
 
 	/**
 	 * creates the location for the taxonomy from the config
 	 *  
 	 * @param config
 	 * @return
 	 */
 	public static Directory createTaxonomyDirectory(final CRConfig config) {
 		String path = getFirstIndexLocation(config);
 		return createDirectory(path, config);
 	}
 
 	/**
 	 * retrieves the Taxonomy location from the config
 	 * 
 	 * @param config
 	 * @return
 	 */
 	protected static String retrieveTaxonomyLocation(CRConfig config) {
 		String path = config.getString(FACET_CONFIG_KEY.concat(".").concat(FACET_CONFIG_PATH_KEY));
 		return path;
 	}
 
 	/**
 	 * checks if facets are activated
 	 * 
 	 * @return
 	 */
 	public boolean useFacets() {
 		return useFacets;
 	}
 
 	private Directory getTaxonomyDirectory() {
 		return taxonomyDir;
 	}
 
 	@Override
 	protected TaxonomyAccessor getTaxonomyAccessorInstance() {
 		Directory directory = this.getTaxonomyDirectory();
 		TaxonomyAccessor accessor = TaxonomyAccessorFactory.getInstance().getAccessor(directory);
 		return accessor;
 	}
 }
