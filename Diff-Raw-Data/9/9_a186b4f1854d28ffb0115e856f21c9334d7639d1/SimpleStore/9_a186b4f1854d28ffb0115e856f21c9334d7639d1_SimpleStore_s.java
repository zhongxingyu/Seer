 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.monitor.client;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.security.GeneralSecurityException;
 import java.security.Key;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import javax.crypto.Cipher;
 import javax.crypto.CipherInputStream;
 import javax.crypto.CipherOutputStream;
 import javax.crypto.SecretKey;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.DESKeySpec;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.equinox.log.Logger;
 import org.eclipse.riena.core.RienaLocations;
 import org.eclipse.riena.core.util.IOUtils;
 import org.eclipse.riena.core.util.Literal;
 import org.eclipse.riena.core.util.Millis;
 import org.eclipse.riena.core.util.PropertiesUtils;
 import org.eclipse.riena.internal.monitor.client.Activator;
 import org.eclipse.riena.monitor.common.Collectible;
 import org.osgi.service.log.LogService;
 
 /**
  * This simple store implements a file based {@code IStore} for the client
  * monitoring witch stores its data in the data area of riena.
  * <p>
  * The simple store expects the following configuration that can be passed with
  * its definition in an extension:
  * <ul>
  * <li>cleanupDelay - defines the period between store cleanup steps (default
  * value is 1 hour if not defined)</li>
  * </ul>
  * Periods of time can be specified as a string conforming to
  * {@link Millis#valueOf(String)}.<br>
  * Example extension:
  * 
  * <pre>
  * &lt;extension point=&quot;org.eclipse.riena.monitor.store&quot;&gt;
  *     &lt;store
  *           name=&quot;SimpleStore&quot;
  *           class=&quot;org.eclipse.riena.internal.monitor.client.SimpleStore:cleanupDelay=120 s&quot;&gt;
  *     &lt;/store&gt;
  * &lt;/extension&gt;
  * </pre>
  */
 public class SimpleStore implements IStore, IExecutableExtension {
 
 	private File storeFolder;
 	private long cleanupDelay;
 	private Cleaner cleaner;
 	private Map<String, Category> categories = new HashMap<String, Category>();
 	private Cipher encrypt;
 	private Cipher decrypt;
 
 	private static final String TRANSFER_FILE_EXTENSION = ".trans"; //$NON-NLS-1$
 	private static final String COLLECT_FILE_EXTENSION = ".coll"; //$NON-NLS-1$
 	private static final String DEL_FILE_EXTENSION = ".del"; //$NON-NLS-1$
 	private static final String CATEGORY_DELIMITER = "#"; //$NON-NLS-1$
 
 	private static final String CLEANUP_DELAY = "cleanupDelay"; //$NON-NLS-1$
 	private static final String CLEANUP_DELAY_DEFAULT = "1 h"; //$NON-NLS-1$
 
 	private static final Logger LOGGER = Activator.getDefault().getLogger(SimpleStore.class);
 	private static final boolean TRACE = true;
 
 	public SimpleStore() throws CoreException {
 		this("simplestore", true); //$NON-NLS-1$
 		// perform default initialization
 		setInitializationData(null, null, null);
 	}
 
 	/**
 	 * @param autoConfig
 	 */
 	private SimpleStore(final String storeFolderName, final boolean autoConfig) {
 		if (autoConfig) {
 			cleaner = new Cleaner();
 			storeFolder = new File(RienaLocations.getDataArea(Activator.getDefault().getBundle()), storeFolderName);
			storeFolder.mkdirs();
			Assert.isTrue(storeFolder.exists());
			Assert.isTrue(storeFolder.isDirectory());
 			try {
 				encrypt = getCipher(Cipher.ENCRYPT_MODE);
 				decrypt = getCipher(Cipher.DECRYPT_MODE);
 			} catch (GeneralSecurityException e) {
 				throw new IllegalArgumentException("Could not generate keys for encryption.", e); //$NON-NLS-1$
 			}
 			LOGGER.log(LogService.LOG_DEBUG, "SimpleStore at " + storeFolder); //$NON-NLS-1$
 		}
 	}
 
 	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
 			throws CoreException {
 		Map<String, String> properties = null;
 		try {
 			properties = PropertiesUtils.asMap(data, Literal.map(CLEANUP_DELAY, CLEANUP_DELAY_DEFAULT));
 			cleanupDelay = Millis.valueOf(properties.get(CLEANUP_DELAY));
 			Assert.isLegal(cleanupDelay > 0, "cleanupDelay must be greater than 0."); //$NON-NLS-1$
 		} catch (IllegalArgumentException e) {
 			throw configurationException("Bad configuration.", e); //$NON-NLS-1$
 		}
 	}
 
 	private CoreException configurationException(String message, Exception e) {
 		return new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, e));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.monitor.client.IStore#setCategories(java.util.Map)
 	 */
 	public void setCategories(Map<String, Category> categories) {
 		this.categories = categories;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.monitor.client.IStore#open()
 	 */
 	public void open() {
 		cleaner.schedule(Millis.seconds(15));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.monitor.client.IStore#close()
 	 */
 	public void close() {
 		cleaner.cancel();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.monitor.client.IStore#flush()
 	 */
 	public void flush() {
 		// nothing to do here
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.riena.internal.monitor.client.ICollectibleStore#collect(org
 	 * .eclipse.riena.monitor.core.Collectible)
 	 */
 	public synchronized boolean collect(final Collectible<?> collectible) {
 		File file = getFile(collectible, COLLECT_FILE_EXTENSION);
 		putCollectible(collectible, file);
 
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.monitor.client.IStore#prepareForTransfer
 	 * (java.lang.String)
 	 */
 	public void prepareTransferables(final String category) {
 		File[] trans = storeFolder.listFiles(new FilenameFilter() {
 
 			public boolean accept(File dir, String name) {
 				return name.startsWith(category) && name.endsWith(COLLECT_FILE_EXTENSION);
 			}
 		});
 		for (File file : trans) {
 			String name = file.getName().replace(COLLECT_FILE_EXTENSION, TRANSFER_FILE_EXTENSION);
			file.renameTo(new File(file.getParent(), name));
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.monitor.client.IStore#getTransferables(java
 	 * .lang.String)
 	 */
 	public synchronized List<Collectible<?>> retrieveTransferables(final String category) {
 		File[] transferables = storeFolder.listFiles(new FilenameFilter() {
 
 			public boolean accept(File dir, String name) {
 				return name.startsWith(category) && name.endsWith(TRANSFER_FILE_EXTENSION);
 			}
 		});
 		List<Collectible<?>> collectibles = new ArrayList<Collectible<?>>();
 		for (File transferable : transferables) {
 			Collectible<?> collectible = getCollectible(transferable);
 			if (collectible != null) {
 				collectibles.add(collectible);
 			}
 		}
 		return collectibles;
 	}
 
 	/**
 	 * Get the decryptor for storing the collectibles.
 	 * <p>
 	 * <b>Note: </b>This hook method is intended to be overwritten to provide a
 	 * better encrypted storage on the local file system on the client.
 	 * Otherwise a simple encryption will be used.
 	 * 
 	 * @param is
 	 * @return
 	 */
 	protected InputStream getDecryptor(InputStream is) throws IOException {
 		return new CipherInputStream(is, decrypt);
 	}
 
 	/**
 	 * 
 	 * Get the encryptor for retrieving the collectibles.
 	 * <p>
 	 * <b>Note: </b>This hook method is intended to be overwritten to provide a
 	 * better encrypted storage on the local file system on the client.
 	 * Otherwise a simple encryption will be used.
 	 * 
 	 * @param os
 	 * @return
 	 */
 	protected OutputStream getEncryptor(OutputStream os) throws IOException {
 		return new CipherOutputStream(os, encrypt);
 	}
 
 	/**
 	 * Get the compressor for storing the collectibles.
 	 * <p>
 	 * <b>Note: </b>This hook method may be overwritten to provide another
 	 * compressing technology. This method uses GZIP.
 	 * 
 	 * @param os
 	 * @return
 	 * @throws IOException
 	 */
 	protected OutputStream getCompressor(OutputStream os) throws IOException {
 		return new GZIPOutputStream(os);
 	}
 
 	/**
 	 * 
 	 * Get the encryptor for retrieving the collectibles.
 	 * <p>
 	 * <b>Note: </b>This hook method is intended to be overwritten to provide
 	 * encrypted storage on the local file system on the client. Otherwise no
 	 * encryption will be used.
 	 * 
 	 * @param is
 	 * @return
 	 * @throws IOException
 	 */
 	protected InputStream getDecompressor(InputStream is) throws IOException {
 		return new GZIPInputStream(is);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.monitor.client.IStore#commitTransferred(
 	 * java.util.List)
 	 */
 	public synchronized void commitTransferred(List<Collectible<?>> collectibles) {
 		for (Collectible<?> collectible : collectibles) {
 			delete(getFile(collectible, TRANSFER_FILE_EXTENSION));
 		}
 	}
 
 	/**
 	 * Try to delete the given file. If it is not deletable mark it deletable
 	 * and try to delete on jvm exit.
 	 * 
 	 * @param file
 	 */
 	private void delete(File file) {
 		if (!file.delete()) {
 			if (file.getName().endsWith(DEL_FILE_EXTENSION)) {
 				file.deleteOnExit();
 				return;
 			}
 			File toDelete = new File(file, DEL_FILE_EXTENSION);
 			if (file.renameTo(toDelete)) {
 				toDelete.deleteOnExit();
 			}
 		}
 	}
 
 	/**
 	 * Get collectible from file.
 	 * 
 	 * @param file
 	 * @return
 	 */
 	private Collectible<?> getCollectible(File file) {
 		ObjectInputStream objectis = null;
 		try {
 			InputStream fis = new FileInputStream(file);
 			InputStream decris = getDecryptor(fis);
 			InputStream gzipis = getDecompressor(decris);
 			objectis = new ObjectInputStream(gzipis);
 			return (Collectible<?>) objectis.readObject();
 		} catch (Exception e) {
 			trace("Error retrieving collectible: " + e.getMessage()); //$NON-NLS-1$
 			if (file.exists() && !file.delete()) {
 				file.deleteOnExit();
 			}
 			return null;
 		} finally {
 			IOUtils.close(objectis);
 		}
 	}
 
 	/**
 	 * Store collectible into file.
 	 * 
 	 * @param collectible
 	 * @param file
 	 */
 	private void putCollectible(Collectible<?> collectible, File file) {
 		ObjectOutputStream objectos = null;
 		try {
 			OutputStream fos = new FileOutputStream(file);
 			OutputStream encos = getEncryptor(fos);
 			OutputStream gzipos = getCompressor(encos);
 			objectos = new ObjectOutputStream(gzipos);
 			objectos.writeObject(collectible);
 		} catch (IOException e) {
 			trace("Error storing collectible: " + e.getMessage()); //$NON-NLS-1$
 			if (file.exists() && !file.delete()) {
 				file.deleteOnExit();
 			}
 		} finally {
 			IOUtils.close(objectos);
 		}
 	}
 
 	private File getFile(Collectible<?> collectible, String extension) {
 		return new File(storeFolder, collectible.getCategory() + CATEGORY_DELIMITER + collectible.getUUID().toString()
 				+ extension);
 	}
 
 	/**
 	 * Perform cleanup of the store as defined by the configuration properties.
 	 */
 	private class Cleaner extends Job {
 		/**
 		 * @param name
 		 */
 		public Cleaner() {
 			super("SimpleStoreCleaner"); //$NON-NLS-1$
 			setUser(true);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @seeorg.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
 		 * IProgressMonitor)
 		 */
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			trace("Store Cleaner started"); //$NON-NLS-1$
 			monitor.beginTask("Cleanup", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
 			clean();
 			monitor.done();
 			// reschedule for periodic work
 			schedule(cleanupDelay);
 			trace("Store Cleaner ended"); //$NON-NLS-1$
 			return Status.OK_STATUS;
 		}
 
 		private void clean() {
 			File[] scrutinizedFiles = storeFolder.listFiles(new FilenameFilter() {
 				public boolean accept(File dir, String name) {
 					return name.endsWith(COLLECT_FILE_EXTENSION) || name.endsWith(DEL_FILE_EXTENSION);
 				}
 			});
 			Map<String, List<File>> categorizedScrutinized = new HashMap<String, List<File>>(scrutinizedFiles.length);
 			for (File scrutinizedFile : scrutinizedFiles) {
 				int categoryDelimiterIndex = scrutinizedFile.getName().indexOf(CATEGORY_DELIMITER);
 				if (categoryDelimiterIndex == -1) {
 					continue;
 				}
 				String category = scrutinizedFile.getName().substring(0, categoryDelimiterIndex);
 				List<File> files = categorizedScrutinized.get(category);
 				if (files == null) {
 					files = new ArrayList<File>();
 					categorizedScrutinized.put(category, files);
 				}
 				files.add(scrutinizedFile);
 			}
 			for (Map.Entry<String, List<File>> entry : categorizedScrutinized.entrySet()) {
 				clean(entry.getValue(), categories.get(entry.getKey()).getMaxItems());
 			}
 		}
 
 		/**
 		 * @param value
 		 */
 		private void clean(final List<File> files, final int maxItems) {
 			if (files.size() < maxItems) {
 				return;
 			}
 			// sort by time, so that we remove older ones
 			Collections.sort(files, new Comparator<File>() {
 				public int compare(File file1, File file2) {
 					Long time1 = file1.lastModified();
 					Long time2 = file2.lastModified();
 					return time1.compareTo(time2);
 				}
 			});
 			for (int i = 0; i < files.size() - maxItems; i++) {
 				delete(files.get(i));
 			}
 		}
 	}
 
 	/**
 	 * Get a cypher.
 	 * 
 	 * @param mode
 	 *            the cipher mode, e.g. Cipher.ENCRYPT_MODE
 	 * @return the cypher or null, if not possible
 	 * @throws GeneralSecurityException
 	 */
 	private static Cipher getCipher(int mode) throws GeneralSecurityException {
 		// Create the cipher 
 		Cipher desCipher = Cipher.getInstance("DES"); //$NON-NLS-1$
 
 		// Initialize the cipher
 		desCipher.init(mode, getKey());
 
 		return desCipher;
 	}
 
 	private static final byte[] KEY = new byte[8];
 
 	static {
 		long first = "This is not very clever :-)".hashCode(); //$NON-NLS-1$
 		long second = "And this neither!".hashCode(); //$NON-NLS-1$
 		new Random(first * second).nextBytes(KEY);
 	}
 
 	private static Key getKey() throws GeneralSecurityException {
 		DESKeySpec pass = new DESKeySpec(KEY);
 		SecretKeyFactory skf = SecretKeyFactory.getInstance("DES"); //$NON-NLS-1$
 		SecretKey s = skf.generateSecret(pass);
 		return s;
 	}
 
 	private static void trace(String line) {
 		if (TRACE) {
 			System.out.println(line);
 		}
 	}
 
 }
