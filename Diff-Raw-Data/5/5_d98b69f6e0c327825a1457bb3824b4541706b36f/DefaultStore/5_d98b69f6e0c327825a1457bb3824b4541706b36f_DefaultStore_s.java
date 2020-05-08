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
 package org.eclipse.riena.internal.monitor.client;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.equinox.log.Logger;
 import org.eclipse.riena.core.util.IOUtils;
 import org.eclipse.riena.monitor.client.IStore;
 import org.eclipse.riena.monitor.common.Collectible;
 import org.osgi.service.log.LogService;
 
 /**
  * TODO config
  */
 public class DefaultStore implements IStore {
 
 	private File storeFolder;
 
 	private static final String TRANSFER_FILE_EXTENSION = ".trans"; //$NON-NLS-1$
 	private static final String COLLECT_FILE_EXTENSION = ".coll"; //$NON-NLS-1$
 	private static final String DEL_FILE_EXTENSION = ".del"; //$NON-NLS-1$
 
 	private static final Logger LOGGER = Activator.getDefault().getLogger(DefaultStore.class);
 
 	public DefaultStore() {
 		//		System.out.println("Platform.getConfigurationLocation: " + Platform.getConfigurationLocation().getURL());
 		//		System.out.println("Platform.getInstallLocation: " + Platform.getInstallLocation().getURL());
 		//		System.out.println("Platform.getInstanceLocation: " + Platform.getInstanceLocation().getURL());
 		//		System.out.println("Platform.getLocation: " + Platform.getLocation());
 		//		System.out.println("Platform.getLogFileLocation: " + Platform.getLogFileLocation());
 		//		System.out.println("Platform.getUserLocation: " + Platform.getUserLocation().getURL());
 
 		// TODO What is the best place to store the stuff??
 		storeFolder = new File(Platform.getUserLocation().getURL().getFile(), ".collectiblestore");
 		storeFolder.mkdirs();
 		Assert.isTrue(storeFolder.exists());
 		Assert.isTrue(storeFolder.isDirectory());
 		LOGGER.log(LogService.LOG_DEBUG, "DefaultStore at " + storeFolder);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.riena.internal.monitor.client.ICollectibleStore#collect(org
 	 * .eclipse.riena.monitor.core.Collectible)
 	 */
 	public synchronized boolean collect(final Collectible<?> collectible) {
 		ObjectOutputStream objectos = null;
 		try {
 			File file = getFile(collectible, COLLECT_FILE_EXTENSION);
 			OutputStream fos = new FileOutputStream(file);
 			OutputStream encos = getEncryptor(fos);
 			OutputStream gzipos = getCompressor(encos);
 			objectos = new ObjectOutputStream(gzipos);
 			objectos.writeObject(collectible);
 		} catch (IOException e) {
 			// TODO Error handling!!?
 			e.printStackTrace();
 			return false;
 		} finally {
 			IOUtils.close(objectos);
 		}
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
	public synchronized List<Collectible<?>> retrieveTransferables(String category) {
 		File[] transferables = storeFolder.listFiles(new FilenameFilter() {
 
 			public boolean accept(File dir, String name) {
				return name.endsWith(TRANSFER_FILE_EXTENSION);
 			}
 		});
 		List<Collectible<?>> collectibles = new ArrayList<Collectible<?>>();
 		for (File transferable : transferables) {
 			ObjectInputStream objectis = null;
 			try {
 				InputStream fis = new FileInputStream(transferable);
 				InputStream decris = getDecryptor(fis);
 				InputStream gzipis = getDecompressor(decris);
 				objectis = new ObjectInputStream(gzipis);
 				Collectible<?> collectible = (Collectible<?>) objectis.readObject();
 				collectibles.add(collectible);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} finally {
 				IOUtils.close(objectis);
 			}
 		}
 		return collectibles;
 	}
 
 	/**
 	 * Get the decryptor for storing the collectibles.
 	 * <p>
 	 * <b>Note: </b>This hook method is intended to be overwritten to provide
 	 * encrypted storage on the local file system on the client. Otherwise no
 	 * encryption will be used.
 	 * 
 	 * @param is
 	 * @return
 	 */
 	protected InputStream getDecryptor(InputStream is) throws IOException {
 		return is;
 	}
 
 	/**
 	 * 
 	 * Get the encryptor for retrieving the collectibles.
 	 * <p>
 	 * <b>Note: </b>This hook method is intended to be overwritten to provide
 	 * encrypted storage on the local file system on the client. Otherwise no
 	 * encryption will be used.
 	 * 
 	 * @param os
 	 * @return
 	 */
 	protected OutputStream getEncryptor(OutputStream os) throws IOException {
 		return os;
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
 			File transferred = getFile(collectible, TRANSFER_FILE_EXTENSION);
 			if (!transferred.delete()) {
 				File toDelete = new File(transferred, DEL_FILE_EXTENSION);
 				transferred.renameTo(toDelete);
 				toDelete.deleteOnExit();
 			}
 		}
 	}
 
 	private File getFile(Collectible<?> collectible, String extension) {
 		return new File(storeFolder, collectible.getCategory() + "-" + collectible.getUUID().toString() + extension); //$NON-NLS-1$
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.monitor.client.IStore#flush()
 	 */
 	public void flush() {
 		// nothing to do here
 	}
 
 }
