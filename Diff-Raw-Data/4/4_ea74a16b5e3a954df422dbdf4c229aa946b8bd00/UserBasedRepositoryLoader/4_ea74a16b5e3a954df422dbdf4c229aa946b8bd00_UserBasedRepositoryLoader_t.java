 /*******************************************************************************
  * Copyright (c) May 18, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdklib.internal.repository;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import org.zend.sdklib.SdkException;
 import org.zend.sdklib.repository.IRepository;
 import org.zend.sdklib.repository.IRepositoryLoader;
 import org.zend.sdklib.repository.RepositoryFactory;
 
 /**
  * Default persistence layer for repository
  * 
  * @author Roy, 2011
  */
 public class UserBasedRepositoryLoader implements IRepositoryLoader {
 
 	private static final String PROPERTY_PATH = "path";
 	private static final String PROPERTY_NAME = "repository";
 	private static final String INI_EXTENSION = ".ini";
 	private final File baseDir;
 
 	public UserBasedRepositoryLoader() {
 		this(getDefaultRepositoryDirectory());
 	}
 
 	public UserBasedRepositoryLoader(File baseDir) {
 		this.baseDir = baseDir;
 
 		if (!baseDir.exists()) {
			throw new IllegalStateException("error finding repository directory " + baseDir.getAbsolutePath());
 		}
 	}
 
 	private static File getDefaultRepositoryDirectory() {
 		final String property = System.getProperty("user.home");
 		final File user = new File(property);
 		if (user.exists()) {
 			final File repositoryDir = new File(user.getAbsolutePath()
 					+ File.separator + ".zend" + File.separator + "repositries");
 			if (!repositoryDir.exists()) {
 				repositoryDir.mkdirs();
 			}
 			return repositoryDir;
 		} else {
 			throw new IllegalStateException("error finding user home directory");
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.zend.sdklib.repository.IRepositoryLoader#add(org.zend.sdklib.repository
 	 * .IRepository)
 	 */
 	@Override
 	public IRepository add(IRepository repository) {
 		if (repository == null) {
 			throw new IllegalArgumentException("repository is null");
 		}
 
 		RepositoryDescriptor descriptor = loadRepositoryDescriptor(repository
 				.getId());
 		if (descriptor != null) {
 			throw new IllegalArgumentException("repository already exists");
 		}
 
 		// create descriptor
 		descriptor = storeRepositoryDescriptor(repository);
 		if (null == descriptor) {
 			return null;
 		}
 
 		return repository;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.sdklib.repository.IRepositoryLoader#remove(org.zend.sdklib.
 	 * repository.IRepository)
 	 */
 	@Override
 	public IRepository remove(IRepository repository) {
 		RepositoryDescriptor d = loadRepositoryDescriptor(repository.getId());
 		if (null == d) {
 			throw new IllegalArgumentException("cannot find repository "
 					+ repository.getId());
 		}
 		final File descriptorFile = getDescriptorFile(repository.getId());
 		final boolean delete2 = descriptorFile.delete();
 
 		if (!delete2) {
 			throw new IllegalArgumentException("error deleting data");
 		}
 
 		return repository;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.sdklib.repository.IRepositoryLoader#update(org.zend.sdklib.
 	 * repository.IRepository)
 	 */
 	@Override
 	public IRepository update(IRepository repository) {
 		throw new UnsupportedOperationException();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.sdklib.repository.IRepositoryLoader#loadAll()
 	 */
 	@Override
 	public IRepository[] loadAll() {
 		final File[] repositories = baseDir.listFiles(new FileFilter() {
 
 			@Override
 			public boolean accept(File file) {
 				return file.getName().endsWith(INI_EXTENSION) && file.isFile();
 			}
 
 		});
 
 		final ArrayList<IRepository> arrayList = new ArrayList<IRepository>(
 				repositories.length);
 		for (File file : repositories) {
 			final RepositoryDescriptor d = loadRepositoryDescriptor(file
 					.getName());
 			if (d.isValid()) {
 				IRepository createRepository;
 				try {
 					createRepository = RepositoryFactory
 							.createRepository(d.name);
 					arrayList.add(createRepository);
 				} catch (SdkException e) {
 					// skip loading of this repository
 				}
 			}
 		}
 
 		return (IRepository[]) arrayList.toArray(new IRepository[arrayList
 				.size()]);
 	}
 
 	private RepositoryDescriptor storeRepositoryDescriptor(
 			IRepository repository) {
 		try {
 			final File file = getDescriptorFile(repository.getId());
 			if (!file.createNewFile()) {
 				return null;
 			}
 			final RepositoryDescriptor repositoryDescriptor = new RepositoryDescriptor();
 			repositoryDescriptor.name = repository.getId();
 			repositoryDescriptor.path = repositoryDescriptor.name;
 
 			final Properties properties = new Properties();
 			properties.put(PROPERTY_NAME, repositoryDescriptor.name);
 			properties.put(PROPERTY_PATH, repository.getId());
 
 			final FileOutputStream fileOutputStream = new FileOutputStream(file);
 			properties.store(fileOutputStream, "descriptor for repository "
 					+ repository.getId());
 
 			fileOutputStream.close();
 
 			return repositoryDescriptor.isValid() ? repositoryDescriptor : null;
 		} catch (IOException e) {
 			// can't be identified as valid repository - ignore
 			return null;
 		}
 	}
 
 	private RepositoryDescriptor loadRepositoryDescriptor(String repository) {
 		try {
 			final File file = getDescriptorFile(repository);
 			if (!file.exists()) {
 				return null;
 			}
 
 			final Properties properties = new Properties();
 			final FileInputStream fileInputStream = new FileInputStream(file);
 			properties.load(fileInputStream);
 
 			final RepositoryDescriptor repositoryDescriptor = new RepositoryDescriptor();
 			repositoryDescriptor.name = properties.getProperty(PROPERTY_NAME);
 			repositoryDescriptor.path = properties.getProperty(PROPERTY_PATH);
 
 			fileInputStream.close();
 			return repositoryDescriptor.isValid() ? repositoryDescriptor : null;
 		} catch (IOException e) {
 			// can't be identified as valid repository - ignore
 			return null;
 		}
 	}
 
 	private File getDescriptorFile(String repository) {
 		if (!repository.endsWith(INI_EXTENSION)) {
 			repository = String.valueOf(repository.hashCode()) + INI_EXTENSION;
 		}
 		final File file = new File(this.baseDir, repository);
 		return file;
 	}
 
 	/**
 	 * Holds the name and path of a repository
 	 */
 	public class RepositoryDescriptor {
 
 		/**
 		 * Name of the repository
 		 */
 		public String name;
 
 		/**
 		 * Path of the repository directory
 		 */
 		public String path;
 
 		public boolean isValid() {
 			return this.name != null;
 		}
 	}
 
 }
