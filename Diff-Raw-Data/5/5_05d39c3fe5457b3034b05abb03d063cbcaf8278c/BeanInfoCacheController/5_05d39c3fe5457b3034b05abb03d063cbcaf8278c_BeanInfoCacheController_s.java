 /*******************************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  *  $RCSfile: BeanInfoCacheController.java,v $
 *  $Revision: 1.16 $  $Date: 2005/09/13 20:30:46 $ 
  */
 package org.eclipse.jem.internal.beaninfo.core;
 
 import java.io.*;
 import java.util.*;
 import java.util.logging.Level;
 
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.common.notify.*;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.change.ChangeDescription;
 import org.eclipse.emf.ecore.change.impl.EObjectToChangesMapEntryImpl;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.jdt.core.*;
 
 import org.eclipse.jem.internal.beaninfo.adapters.*;
 import org.eclipse.jem.internal.java.beaninfo.IIntrospectionAdapter;
 import org.eclipse.jem.java.JavaClass;
 import org.eclipse.jem.util.emf.workbench.ProjectResourceSet;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jem.util.plugin.JEMUtilPlugin;
 import org.eclipse.jem.util.plugin.JEMUtilPlugin.CleanResourceChangeListener;
 
 /**
  * Controller of the BeanInfo cache. There is one per workspace (it is a static).
  * 
  * The cache is stored on a per IPackageFragmentRoot basis. Each package fragment root will be:
  * 
  * <pre>
  * 
  *  root{999}/classname.xmi
  *  
  * </pre>
  * 
  * "root{999}" will be a unigue name (root appended with a number}, one for each package fragment root. The "classname.xmi" will be the BeanInfo cache
  * for a class in the root. A root can't have more than one class with the same name, so there shouldn't be any collisions.
  * <p>
  * Now roots can either be in a project, or they can be an external jar (which can be shared between projects).
  * <p>
  * Now all roots for a project will be stored in the project's working location
  * {@link org.eclipse.core.resources.IProject#getWorkingLocation(java.lang.String)}under the ".cache" directory. It will be this format in each
  * project location (under the org.eclipse.jem.beaninfo directory):
  * 
  * <pre>
  * 
  *  .index
  *  root{999}/...
  *  
  * </pre>
  * 
  * The ".index" file will be stored/loaded through an ObjectStream. It will be a {@link BeanInfoCacheController.Index}. It is the index to all of the
  * root's in the directory.
  * <p>
  * All of the external jar roots will be stored in the org.eclipse.jem.beaninfo plugin's state location
  * {@link org.eclipse.core.runtime.Platform#getStateLocation(org.osgi.framework.Bundle)}under the ".cache" directory. The format of this directory
  * will be the same as for each project. And the roots will be for each unique shared external jar (such as the differnt jre's rt.jars).
  * <p>
  * Note: There are so many places where synchronization is needed, so it is decided to synchronize only on BeanInfoCacheController.INSTANCE. It would
  * be too easy to get a dead-lock because the order of synchronizations can't be easily controlled. Since each piece of sync control is very short
  * (except for save of the indices, but that is ok because we don't want them changing while saving) it shouldn't block a lot. There is one place we
  * violate this and that is we do a sync on ClassEntry instance when working with the pending. This is necessary because we don't want the cache write
  * job to hold up everything while writing, so we sync on the entry being written instead. There we must be very careful that we never to
  * BeanInfoCacheControler.INSTANCE sync and then a ClassEntry sync because we could deadlock. The CE access under the covers may do a CE sync and then
  * a BeanInfoCacheController.INSTANCE sync.
  * 
  * @since 1.1.0
  */
 public class BeanInfoCacheController {
 
 	/**
 	 * Singleton cache controller.
 	 * 
 	 * @since 1.1.0
 	 */
 	public static final BeanInfoCacheController INSTANCE = new BeanInfoCacheController();
 
 	private BeanInfoCacheController() {
 		// Start up save participent. This only is used for saving indexes and shutdown. Currently the saved state delta
 		// is of no interest. If a project is deleted while we were not up, then the project index would be gone, so
 		// our data will automatically be gone for the project. 
 		// If a class was deleted while the project's beaninfo was not active, the cache will still contain it. If the class ever came back it
 		// would be stale and so recreated. If it never comes back, until a clean is done, it would just hang around.
 		// The problem with delete is it is hard to determine that the file is actually a class of interest. The javamodel
 		// handles that for us but we won't have a javamodel to handle this on start up to tell us the file was a class of interest. So
 		// we'll take the hit of possible cache for non-existant classes. A clean will take care of this.
 		saveParticipant = new SaveParticipant();
 		try {
 			ResourcesPlugin.getWorkspace().addSaveParticipant(BeaninfoPlugin.getPlugin(), saveParticipant);
 		} catch (CoreException e) {
 			BeaninfoPlugin.getPlugin().getLogger().log(e.getStatus());
 		}
 		
 		// Create a cleanup listener to handle clean requests and project deletes. We need to know about project deletes while
 		// active because we may have a project index in memory and that needs to be thrown away.
 		JEMUtilPlugin.addCleanResourceChangeListener(new CleanResourceChangeListener() {
 		
 			protected void cleanProject(IProject project) {
 				// Get rid of the project index and the data for the project.
 				synchronized (BeanInfoCacheController.this) {
 					try {
 						Index projectIndex = (Index) project.getSessionProperty(PROJECT_INDEX_KEY);
 						if (projectIndex != null) {
 							project.setSessionProperty(PROJECT_INDEX_KEY, null);
 							projectIndex.markDead();
 							cleanDirectory(getCacheDir(project).toFile(), true);
 						}
 						BeaninfoNature nature = BeaninfoPlugin.getPlugin().getNature(project);
 						if (nature != null) {
 							BeaninfoAdapterFactory adapterFactory = (BeaninfoAdapterFactory) EcoreUtil.getAdapterFactory(nature.getResourceSet().getAdapterFactories(), IIntrospectionAdapter.ADAPTER_KEY);
 							if (adapterFactory != null) {
 								adapterFactory.markAllStale(true);	// Also clear the overrides.
 							}
 						}						
 					} catch (CoreException e) {
 						// Shouldn't occur. 
 					}
 				}
 			}
 			
 			protected void cleanAll() {
 				synchronized(BeanInfoCacheController.this) {
 					// Get MAIN_INDEX, mark it dead, and then delete everything under it.
 					if (MAIN_INDEX != null) {
 						MAIN_INDEX.markDead();
 						MAIN_INDEX = null;
 						cleanDirectory(getCacheDir(null).toFile(), true);
 					}
 				}
 				super.cleanAll();
 			}
 			
 			public void resourceChanged(IResourceChangeEvent event) {
 				// We don't need to handle PRE_CLOSE because SaveParticipent project save will handle closing.
 				switch (event.getType()) {
 					case IResourceChangeEvent.PRE_DELETE:
 						// Don't need to clear the cache directory because Eclipse will get rid of it.
 						synchronized (BeanInfoCacheController.this) {
 							try {
 								Index projectIndex = (Index) event.getResource().getSessionProperty(PROJECT_INDEX_KEY);
 								if (projectIndex != null) {
 									// No need to remove from the project because the project is going away and will clean itself up.
 									projectIndex.markDead();
 								}
 							} catch (CoreException e) {
 								// Shouldn't occur.
 							}
 						}
 						// Flow into PRE_CLOSE to release the nature.
 					case IResourceChangeEvent.PRE_CLOSE:
 						// About to close or delete, so release the nature, if any.
 						IProject project = (IProject) event.getResource();
 						BeaninfoNature nature = BeaninfoPlugin.getPlugin().getNature(project);
 						if (nature != null) {
 							nature.cleanup(false, true);
 						}
 						break;
 					default:
 						super.resourceChanged(event);
 						break;
 				}
 			}
 		
 		}, IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.PRE_CLOSE);
 	}
 
 	protected SaveParticipant saveParticipant;
 
 	/**
 	 * An index structure for the Main and Project indexes. Access to the index contents and methods should synchronize on the index itself.
 	 * <p>
 	 * Getting to the index instance should only be through the <code>getMainIndex()</code> and <code>getProjectIndex(IProject)</code> accessors
 	 * so that synchronization and serialization is controlled.
 	 * 
 	 * @since 1.1.0
 	 */
 	protected static class Index implements Serializable {
 
 		private static final long serialVersionUID = 1106864425423L;
 
 		/*
 		 * Is this a dirty index, i.e. it has been changed and needs to be saved.
 		 */
 		transient private boolean dirty;
 
 		private static final int DEAD = -1;	// Used in highRootNumber to indicate the index is dead.
 		/**
 		 * The highest root number used. It is incremented everytime one is needed. It doesn't ever decrease to recover removed roots.
 		 * 
 		 * @since 1.1.0
 		 */
 		public int highRootNumber;
 
 		/**
 		 * Map of root names to the root Index. The key is a {@link IPath}. The path will be relative to the workspace if a project root, or an
 		 * absolute local path to the archive if it is an external archive. It is the IPath to package fragment root (either a folder or a jar file).
 		 * <p>
 		 * The value will be a {@link BeanInfoCacheController.RootIndex}. This is the index for the contents of that root.
 		 * 
 		 * @since 1.1.0
 		 */
 		transient public Map rootToRootIndex;
 
 		/**
 		 * @param dirty
 		 *            The dirty to set.
 		 * 
 		 * @since 1.1.0
 		 */
 		public void setDirty(boolean dirty) {
 			synchronized (BeanInfoCacheController.INSTANCE) {
 				this.dirty = dirty;
 			}
 		}
 
 		/**
 		 * @return Returns the dirty.
 		 * 
 		 * @since 1.1.0
 		 */
 		public boolean isDirty() {
 			synchronized (BeanInfoCacheController.INSTANCE) {
 				return dirty;
 			}
 		}
 		
 		/**
 		 * Answer if this index is dead. It is dead if a clean has occurred. This is needed because there could be some ClassEntry's still
 		 * around (such as in the pending write queue) that are for cleaned roots. This is used to test if it has been cleaned.
 		 * @return
 		 * 
 		 * @since 1.1.0
 		 */
 		public boolean isDead() {
 			return highRootNumber == DEAD;
 		}
 		
 		/**
 		 * Mark the index as dead.
 		 * 
 		 * 
 		 * @since 1.1.0
 		 */
 		void markDead() {
 			highRootNumber = DEAD;
 		}
 
 		private void writeObject(ObjectOutputStream os) throws IOException {
 			os.defaultWriteObject();
 			// Now write out the root to root index map. We are not serializing the Map directly using normal Map serialization because
 			// the key of the map is an IPath (which is a Path under the covers) and Path is not serializable.
 			os.writeInt(rootToRootIndex.size());
 			for (Iterator mapItr = rootToRootIndex.entrySet().iterator(); mapItr.hasNext();) {
 				Map.Entry entry = (Map.Entry) mapItr.next();
 				os.writeUTF(((IPath) entry.getKey()).toString());
 				os.writeObject(entry.getValue());
 			}
 		}
 
 		private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
 			is.defaultReadObject();
 			int size = is.readInt();
 			rootToRootIndex = new HashMap(size < 100 ? 100 : size);
 			while (size-- > 0) {
 				rootToRootIndex.put(new Path(is.readUTF()), is.readObject());
 			}
 		}
 	}
 
 	/**
 	 * An index for a root. It has an entry for each class in this root. The class cache entry describes the cache, whether it is stale, what the
 	 * current mod stamp is, etc.
 	 * 
 	 * @since 1.1.0
 	 */
 	public static abstract class RootIndex implements Serializable {
 
 		private static final long serialVersionUID = 1106868674867L;
 
 		transient private IPath cachePath; // Absolute local filesystem IPath to the root cache directory. Computed at runtime because it may change
 										   // if workspace relocated.
 
 		protected Map classNameToClassEntry; // Map of class names to class entries.
 
 		private String rootName; // Name of the root directory in the cache (e.g. "root1").
 
 		protected Index index; // Index containing this root index.
 
 		protected RootIndex() {
 		}
 
 		public RootIndex(String rootName, Index index) {
 			this.rootName = rootName;
 			classNameToClassEntry = new HashMap(100); // When created brand new, put in a map. Otherwise object stream will create the map.
 			this.index = index;
 		}
 		
 		/**
 		 * Get the index that points to this root.
 		 * @return
 		 * 
 		 * @since 1.1.0
 		 */
 		Index getIndex() {
 			return index;
 		}
 
 		/**
 		 * Return the root directory name
 		 * 
 		 * @return rootname
 		 * 
 		 * @since 1.1.0
 		 */
 		public String getRootName() {
 			return rootName;
 		}
 
 		/**
 		 * Set this RootIndex (and the containing Index) as being dirty and in need of saving.
 		 * 
 		 * 
 		 * @since 1.1.0
 		 */
 		public void setDirty() {
 			index.setDirty(true);
 		}
 
 		/*
 		 * Setup for index. It will initialize the path. Once set it won't set it again. This will be called repeatedly by the cache controller
 		 * because there is no way to know if it was lazily created or was brought in from file. When brought in from file the path is not set because
 		 * it should be relocatable and we don't want absolute paths out on the disk caches, and we don't want to waste time creating the path at load
 		 * time because it may not be needed for a while. So it will be lazily created. <p> If the project is set, then the path will be relative to
 		 * the project's working location. If project is <code> null </code> then it will be relative to the BeanInfo plugin's state location. <p>
 		 * This is <package-protected> because only the creator (BeanInfoCacheController class) should set this up.
 		 * 
 		 * @param project
 		 * 
 		 * @since 1.1.0
 		 */
 		void setupIndex(IProject project) {
 			if (getCachePath() == null)
 				cachePath = getCacheDir(project).append(rootName);
 		}
 
 		/**
 		 * @return Returns the path of the cache directory for the root.
 		 * 
 		 * @since 1.1.0
 		 */
 		public IPath getCachePath() {
 			return cachePath;
 		}
 
 		/**
 		 * Return whether this is a root for a archive or a folder.
 		 * 
 		 * @return <code>true</code> if archive for a root. <code>false</code> if archive for a folder.
 		 * 
 		 * @since 1.1.0
 		 */
 		public abstract boolean isArchiveRoot();
 	}
 
 	/**
 	 * A root index that is for an archive, either internal or external. It contains the archive's modification stamp. Each class cache entry will
 	 * have this same modification stamp. If the archive is changed then all of the class cache entries will be removed because they are all possibly
 	 * stale. No way to know which may be stale and which not.
 	 * 
 	 * @since 1.1.0
 	 */
 	public static class ArchiveRootIndex extends RootIndex {
 
 		private static final long serialVersionUID = 110686867456L;
 
 		private long archiveModificationStamp;
 
 		/*
 		 * For serializer to call.
 		 */
 		protected ArchiveRootIndex() {
 		}
 
 		public ArchiveRootIndex(String rootName, long archiveModificationStamp, Index index) {
 			super(rootName, index);
 			this.archiveModificationStamp = archiveModificationStamp;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.jem.internal.beaninfo.core.BeanInfoCacheController.RootIndex#isArchiveRoot()
 		 */
 		public boolean isArchiveRoot() {
 			return true;
 		}
 
 		/*
 		 * Set the modification stamp. <p> <package-protected> because only the cache controller should change it.
 		 * 
 		 * @param archiveModificationStamp The archiveModificationStamp to set.
 		 * 
 		 * @see BeanInfoCacheController#MODIFICATION_STAMP_STALE
 		 * @since 1.1.0
 		 */
 		void setArchiveModificationStamp(long archiveModificationStamp) {
 			this.archiveModificationStamp = archiveModificationStamp;
 			setDirty();
 		}
 
 		/**
 		 * Returns the modification stamp.
 		 * 
 		 * @return Returns the archiveModificationStamp.
 		 * @see BeanInfoCacheController#MODIFICATION_STAMP_STALE
 		 * @since 1.1.0
 		 */
 		public long getArchiveModificationStamp() {
 			return archiveModificationStamp;
 		}
 	}
 
 	/**
 	 * This is a root index for a folder (which will be in the workspace). Each class cache entry can have a different modification stamp with a
 	 * folder root index.
 	 * 
 	 * @since 1.1.0
 	 */
 	public static class FolderRootIndex extends RootIndex {
 
 		private static final long serialVersionUID = 1106868674834L;
 
 		/*
 		 * For serialization.
 		 */
 		protected FolderRootIndex() {
 		}
 
 		/**
 		 * @param rootName
 		 * 
 		 * @since 1.1.0
 		 */
 		public FolderRootIndex(String rootName, Index index) {
 			super(rootName, index);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.jem.internal.beaninfo.core.BeanInfoCacheController.RootIndex#isArchiveRoot()
 		 */
 		public boolean isArchiveRoot() {
 			return false;
 		}
 	}
 
 	/**
 	 * An individual class entry from the cache. It has an entry for each class in the root. The class cache entry describes the cache, whether it is
 	 * stale, what the current mod stamp is, etc.
 	 * <p>
 	 * There is a method to call to see if deleted. This should only be called if entry is being held on to because
 	 * <code>getClassEntry(JavaClass)</code> will never return a deleted entry. There is a method to get the modification stamp of the current cache
 	 * entry. This is the time stamp of the cooresponding class resource (or archive file) that the cache file was created from. If the value is
 	 * <code>IResource.NULL_STAMP</code>, then the cache file is known to be stale. Otherwise if the value is less than a modification stamp of a
 	 * super class then the cache file is stale.
 	 * 
 	 * @see ClassEntry#isDeleted()
 	 * @see ClassEntry#getModificationStamp()
 	 * @see BeanInfoCacheController#getClassEntry(JavaClass)
 	 * @since 1.1.0
 	 */
 	public static class ClassEntry implements Serializable {
 
 		private static final long serialVersionUID = 1106868674666L;
 
 		public static final long DELETED_MODIFICATION_STAMP = Long.MIN_VALUE; // This flag won't be seen externally. It is used to indicate the entry
 																			  // has been deleted for those that have been holding a CE.
 		
 		/**
 		 * Check against the super modification stamp and the interface stamps to see if they were set
 		 * by undefined super class or interface at cache creation time. 
 		 * 
 		 * @since 1.1.0
 		 */
 		public static final long SUPER_UNDEFINED_MODIFICATION_STAMP = Long.MIN_VALUE+1;	
 
 		private long modificationStamp;
 		private long superModificationStamp;	// Stamp of superclass, if any, at time of cache creation.
 		private String[] interfaceNames;	// Interfaces names (null if no interfaces)
 		private long[] interfaceModicationStamps;	// Modification stamps of interfaces, if any. (null if no interfaces).
 		private transient Resource pendingResource;	// Resource is waiting to be saved, but the timestamps are for this pending resource so that we know what it will be ahead of time. At this point the class will be introspected. 
 
 		private RootIndex rootIndex; // The root index this class entry is in, so that any changes can mark the entry as dirty.
 
 		private String className; // The classname for this entry.
 		
 		private boolean saveOperations;	// Flag for saving operations. Once this is set, it will continue to save operations in the cache in addition to everything else.
 
 		private long configurationModificationStamp;	// Modification stamp of the Eclipse configuration. Used to determine if the cache of override files is out of date due to a config change.
 		private boolean overrideCacheExists;	// Flag that there is an override cache to load. This is orthogonal to the config mod stamp because it simply means that on the current configuration there is no override cache.
 		private transient Resource pendingOverrideResource;	// Override resource is waiting to be saved.
 		
 		protected ClassEntry() {
 		}
 
 		ClassEntry(RootIndex rootIndex, String className) {
 			this.setRootIndex(rootIndex);
 			this.className = className;
 			modificationStamp = IResource.NULL_STAMP;
 			rootIndex.classNameToClassEntry.put(className, this);
 		}
 
 		/**
 		 * 
 		 * @return
 		 * 
 		 * @since 1.1.0
 		 */
 		public String getClassName() {
 			return className;
 		}
 
 		/**
 		 * Return whether the entry has been deleted. This will never be seen in an entry that is still in an index. It is used for entries that have
 		 * been removed from the index. It is for classes (such as the BeanInfoClassAdapter) which are holding onto a copy of entry to let them know.
 		 * <p>
 		 * Holders of entries should call isDeleted if they don't need to further check the mod stamp. Else they should call getModificationStamp and
 		 * check of deleted, stale, or mod stamp. That would save extra synchronizations. If entry is deleted then it should throw the entry away.
 		 * 
 		 * @return <code>true</code> if the entry has been deleted, <code>false</code> if still valid.
 		 * 
 		 * @see ClassEntry#getModificationStamp()
 		 * @see ClassEntry#isStale()
 		 * @see ClassEntry#DELETED_MODIFICATION_STAMP
 		 * @since 1.1.0
 		 */
 		public boolean isDeleted() {
 			return getModificationStamp() == DELETED_MODIFICATION_STAMP;
 		}
 
 		/**
 		 * Mark the entry as deleted. It will also be removed from root index in that case.
 		 * <p>
 		 * Note: It is public only so that BeanInfoClassAdapter can access it. It should not be called by anyone else outside of BeanInfo.
 		 */
 		public synchronized void markDeleted() {
 			if (!isDeleted()) {
 				getRootIndex().classNameToClassEntry.remove(className);
 				setModificationStamp(DELETED_MODIFICATION_STAMP); // Also marks index as dirty.
 			}
 		}
 
 		/**
 		 * Return whether the entry is stale or not. This orthoganal to isDeleted. isDeleted will not be true if isStale and isStale will not be true
 		 * if isDeleted. Normally you should use getModificationStamp and check the value for IResource.NULL_STAMP and other values to bring it down
 		 * to only one synchronized call to CE, but if only needing to know if stale, you can use this.
 		 * 
 		 * @return
 		 * 
 		 * @since 1.1.0
 		 * @see IResource#NULL_STAMP
 		 * @see ClassEntry#getModificationStamp()
 		 * @see ClassEntry#isDeleted()
 		 */
 		public boolean isStale() {
 			return getModificationStamp() == IResource.NULL_STAMP;
 		}
 
 		/**
 		 * Return the modification stamp. For those holding onto an entry, and they need to know more than if just deleted, then they should just the
 		 * return value from getModificationStamp. Else they should use isDeleted or isStale.
 		 * 
 		 * @return modification stamp, or {@link IResource#NULL_STAMP}if stale or not yet created, or {@link ClassEntry#DELETED_MODIFICATION_STAMP}
 		 *         if deleted.
 		 * 
 		 * @see ClassEntry#isDeleted()
 		 * @see ClassEntry#isStale()
 		 * @since 1.1.0
 		 */
 		public synchronized long getModificationStamp() {
 			return modificationStamp;
 		}
 		
 		/**
 		 * Return the super modification stamp.
 		 * @return
 		 * 
 		 * @since 1.1.0
 		 */
 		public synchronized long getSuperModificationStamp() {
 			return superModificationStamp;
 		}
 		
 		/**
 		 * Return the interface names or <code>null</code> if no interface names.
 		 * @return
 		 * 
 		 * @since 1.1.0
 		 */
 		public synchronized String[] getInterfaceNames() {
 			return interfaceNames;
 		}
 
 		/**
 		 * Return the interface modification stamps or <code>null</code> if no interfaces.
 		 * @return
 		 * 
 		 * @since 1.1.0
 		 */
 		public synchronized long[] getInterfaceModificationStamps() {
 			return interfaceModicationStamps;
 		}
 		
 		/*
 		 * Set the modification stamp. <p> <package-protected> because only the cache controller should set it. @param modificationStamp
 		 * 
 		 * @since 1.1.0
 		 */
 		void setModificationStamp(long modificationStamp) {
 			if (this.modificationStamp != modificationStamp) {
 				this.modificationStamp = modificationStamp;
 				getRootIndex().setDirty();
 			}
 		}
 
 		/**
 		 * Answer whether operations are also stored in the cache for this class. By default they are not. Once turned on they
 		 * will always be stored for this class until the class is deleted.
 		 * 
 		 * @return <code>true</code> if operations are cached, <code>false</code> if they are not cached.
 		 * 
 		 * @since 1.1.0
 		 */
 		public synchronized boolean isOperationsStored() {
 			return saveOperations;
 		}
 		
 		/*
 		 * Set the operations stored flag.
 		 * @param storeOperations
 		 * 
 		 * @see BeanInfoCacheController.ClassEntry#isOperationsStored()
 		 * @since 1.1.0
 		 */
 		void setOperationsStored(boolean storeOperations) {
 			saveOperations = storeOperations;
 		}
 		
 		/**
 		 * Get the configuration modification stamp of the last saved override cache.
 		 * 
 		 * @return
 		 * 
 		 * @since 1.1.0
 		 */
 		public synchronized long getConfigurationModificationStamp() {
 			return configurationModificationStamp;
 		}
 
 		/* 
 		 * Set the configuration modification stamp.
 		 * <p> <package-protected> because only the cache controller should access it.
 		 * 
 		 * @param configurationModificationStamp
 		 * 
 		 * @since 1.1.0
 		 */
 		void setConfigurationModificationStamp(long configurationModificationStamp) {
 			this.configurationModificationStamp = configurationModificationStamp;
 			getRootIndex().setDirty();
 		}
 		
 		/**
 		 * Answer whether there is an override cache available.
 		 * @return
 		 * 
 		 * @since 1.1.0
 		 */
 		public synchronized boolean overrideCacheExists() {
 			return overrideCacheExists;
 		}
 		
 		/*
 		 * Set the override cache exists flag.
 		 * <p> <package-protected> because only the cache controller should access it.
 		 * @param overrideCacheExists
 		 * 
 		 * @since 1.1.0
 		 */
 		void setOverrideCacheExists(boolean overrideCacheExists) {
 			this.overrideCacheExists = overrideCacheExists;
 		}
 		
 		/**
 		 * Get the pending resource or <code>null</code> if not pending.
 		 * 
 		 * @return the pending resource or <code>null</code> if not pending.
 		 * 
 		 * @since 1.1.0
 		 */
 		public synchronized Resource getPendingResource() {
 			return pendingResource;
 		}
 		
 		/*
 		 * Set the entry. The sequence get,do something,set must be grouped within a synchronized(ClassEntry). 
 		 * <p> <package-protected> because only the cache controller should access it. 
 		 * @param cacheResource The cacheResource to set.
 		 * 
 		 * @since 1.1.0
 		 */
 		void setPendingResource(Resource pendingResource) {
 			this.pendingResource = pendingResource;
 		}
 
 		/**
 		 * Get the pending override resource or <code>null</code> if not pending.
 		 * 
 		 * @return the pending override resource or <code>null</code> if not pending.
 		 * 
 		 * @since 1.1.0
 		 */
 		public synchronized Resource getPendingOverrideResource() {
 			return pendingOverrideResource;
 		}
 		
 		/*
 		 * Set the entry. The sequence get,do something,set must be grouped within a synchronized(ClassEntry). 
 		 * <p> <package-protected> because only the cache controller should access it. 
 		 * @param cacheResource The cacheResource to set.
 		 * 
 		 * @since 1.1.0
 		 */
 		void setPendingOverrideResource(Resource pendingOverrideResource) {
 			this.pendingOverrideResource = pendingOverrideResource;
 		}
 
 		/*
 		 * <package-protected> because only the cache controller should access it. @param rootIndex The rootIndex to set.
 		 * 
 		 * @since 1.1.0
 		 */
 		void setRootIndex(RootIndex rootIndex) {
 			this.rootIndex = rootIndex;
 		}
 
 		/*
 		 * <package-protected> because only the cache controller should access it. @return Returns the rootIndex.
 		 * 
 		 * @since 1.1.0
 		 */
 		RootIndex getRootIndex() {
 			return rootIndex;
 		}
 		
 		/*
 		 * <package-protected> because only the cache controller should access it.
 		 * 
 		 * @since 1.1.0
 		 */
 		void setSuperModificationStamp(long superModificationStamp) {
 			this.superModificationStamp = superModificationStamp;
 		}
 
 		/*
 		 * <package-protected> because only the cache controller should access it.
 		 * 
 		 * @since 1.1.0
 		 */
 		void setInterfaceNames(String[] interfaceNames) {
 			this.interfaceNames = interfaceNames;
 		}
 
 		/*
 		 * <package-protected> because only the cache controller should access it.
 		 * 
 		 * @since 1.1.0
 		 */
 		void setInterfaceModificationStamps(long[] interfaceModificationStamps) {
 			this.interfaceModicationStamps = interfaceModificationStamps;
 		}
 		
 	}
 
 	/*
 	 * Main index for the external jars. This variable should not be referenced directly except through the getMainIndex() accessor. That controls
 	 * synchronization and restoration as needed.
 	 */
 	private static Index MAIN_INDEX;
 
 	/*
 	 * Key into the Project's session data for the project index. The Project index is stored in the project's session data. That
 	 * way when the project is closed or deleted it will go away. 
 	 * 
 	 * The project indexes will be read in as needed on a per-project basis. This variable should not be
 	 * referenced directly except through the getProjectIndex(IProject) accessor. That controls synchronization and restoration as needed.
 	 * Only during cleanup and such where we don't want to create one if it doesn't exist you must use sync(this). Be careful to keep 
 	 * the sync small.
 	 */
 	private static final QualifiedName PROJECT_INDEX_KEY = new QualifiedName(BeaninfoPlugin.PI_BEANINFO_PLUGINID, "project_index");	//$NON-NLS-1$
 
 	/*
 	 * Suffix for class cache files.
 	 */
 	private static final String CLASS_CACHE_SUFFIX = ".xmi"; //$NON-NLS-1$
 	/*
 	 * Suffic for class override cache files.
 	 */
 	private static final String OVERRIDE_CACHE_SUFFIX = ".override.xmi"; //$NON-NLS-1$
 
 	/**
 	 * Return the current class entry for the JavaClass, or <code>null</code> if no current entry.
 	 * 
 	 * @param jclass
 	 * @return class entry or <code>null</code> if no current entry.
 	 * 
 	 * @since 1.1.0
 	 */
 	public ClassEntry getClassEntry(JavaClass jclass) {
 		IType type = (IType) jclass.getReflectionType();
 		RootIndex rootIndex = getRootIndex(type);
 		String className = jclass.getQualifiedNameForReflection();
 		return getClassEntry(rootIndex, className, false);
 	}
 
 	/**
 	 * Enumeration for newCache: Signals that this cache is the Reflection Cache with no operations in it.
 	 * @since 1.1.0
 	 */
 	public final static int REFLECTION_CACHE = 1;
 	/**
 	 * Enumeration for newCache: Signals that this cache is the Reflection Cache with operations in it.
 	 * @since 1.1.0
 	 */
 	public final static int REFLECTION_OPERATIONS_CACHE = 2;
 	/**
 	 * Enumeration for newCache: Signals that this cache is the Overrides cache.
 	 * @since 1.1.0
 	 */
 	public final static int OVERRIDES_CACHE = 3;
 	/**
 	 * A new cache entry for the given class has been created. Need to write it out.
 	 * 
 	 * @param jclass
 	 *            the JavaClass the cache is for.
 	 * @param cache
 	 *            the ChangeDescription to put out if cacheType is Reflection types, a List if override cache type.
 	 * @param cacheType
 	 *            {@link BeanInfoCacheController.ClassEntry#REFLECTION_CACHE} for the enum values.
 	 * @return new class entry (or old one if same one). Should always replace one being held by this one. <code>null</code> if cache could not be
 	 *         updated for some reason.
 	 * @since 1.1.0
 	 */
 	public ClassEntry newCache(JavaClass jclass, Object cache, int cacheType) {
 		if (BeaninfoPlugin.getPlugin().getLogger().isLoggingLevel(Level.FINER)) {
 			Logger logger = BeaninfoPlugin.getPlugin().getLogger();
 			String type = cacheType!=OVERRIDES_CACHE?"Class":"Overrides"; //$NON-NLS-1$ //$NON-NLS-2$
 			if (cacheType == OVERRIDES_CACHE && cache == null)
 				type+="  empty"; //$NON-NLS-1$
 			logger.log("Creating cache for class "+jclass.getQualifiedNameForReflection()+" cache type="+type, Level.FINER); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		ChangeDescription cd = null;
 		if (cacheType != OVERRIDES_CACHE) {
 			// First go through the cd and remove any empty changes. This is because we created the feature change before we knew what went into
 			// it, and at times nothing goes into it.
 			cd = (ChangeDescription) cache;
 			for (Iterator iter = cd.getObjectChanges().iterator(); iter.hasNext();) {
 				EObjectToChangesMapEntryImpl fcEntry = (EObjectToChangesMapEntryImpl) iter.next();
 				if (((List) fcEntry.getValue()).isEmpty())
 					iter.remove(); // Empty changes, remove it.
 			}
 		}
 		IType type = (IType) jclass.getReflectionType();
 		RootIndex rootIndex = getRootIndex(type);
 		String className = jclass.getQualifiedNameForReflection();
 		ClassEntry ce = getClassEntry(rootIndex, className, true); // Create it if not existing.
 		// Sync on ce so that only can create a cache for a class at a time and so that writing (if occurring at the same time for the class) can be
 		// held up.
 		// this is a violation of the agreement to only sync on THIS, but it is necessary or else the write job would lock everything out while it is
 		// writing. This way it only locks out one class, if the class is at the same time.
 		// We shouldn't have deadlock because here we lock ce and then THIS (maybe). The write job will lock ce, and then lock THIS. Everywhere else
 		// we must
 		// also do lock ce then THIS. Mustn't do other way around or possibility of deadlock. Be careful that any synchronized methods in this class
 		// do
 		// not lock an existing ce.
 		ResourceSet cacheRset = null;
 		synchronized (ce) {
 			Resource cres;
 			if (cacheType != OVERRIDES_CACHE) {
 				cres = ce.getPendingResource();
 				if (cres != null) {
 					// We have a pending, so clear and reuse the resource.
 					cres.getContents().clear();
 				} else {
 					// Not currently writing or waiting to write, so create a new resource.
 					cres = jclass.eResource().getResourceSet().createResource(
 							URI.createFileURI(rootIndex.getCachePath().append(className + CLASS_CACHE_SUFFIX).toString()));
 				}
 				cacheRset = cres.getResourceSet();
 				ce.setOperationsStored(cacheType == REFLECTION_OPERATIONS_CACHE);
 				ce.setPendingResource(cres);
 				cres.getContents().add(cd);
 				// Archives use same mod as archive (retrieve from rootindex), while non-archive use the underlying resource's mod stamp.
 				if (rootIndex.isArchiveRoot())
 					ce.setModificationStamp(((ArchiveRootIndex) rootIndex).getArchiveModificationStamp());
 				else {
 					try {
 						ce.setModificationStamp(type.getUnderlyingResource().getModificationStamp());
 					} catch (JavaModelException e) {
 						BeaninfoPlugin.getPlugin().getLogger().log(e);
 						ce.markDeleted(); // Mark as deleted in case this was an existing that someone is holding.
 						return null; // Couldn't do it, throw cache entry away. This actually should never occur.
 					}
 				}
 				// Need to get the supers info. 
 				List supers = jclass.getESuperTypes();
 				if (!supers.isEmpty()) {
 					// We assume that they all have been introspected. This was done back in main introspection. If they are introspected they will have a class entry.
 					BeaninfoClassAdapter bca = BeaninfoClassAdapter.getBeaninfoClassAdapter((EObject) supers.get(0));
 					ClassEntry superCE = bca.getClassEntry();
 					if (superCE != null)
 						ce.setSuperModificationStamp(superCE.getModificationStamp());
 					else
 						ce.setSuperModificationStamp(ClassEntry.SUPER_UNDEFINED_MODIFICATION_STAMP);	// No classentry means undefined. So put something in so that when it becomes defined we will know.
 					if(supers.size() == 1) {
 						ce.setInterfaceNames(null);
 						ce.setInterfaceModificationStamps(null);
 					} else {
 						String[] interNames = new String[supers.size()-1];
 						long[] interMods = new long[interNames.length];
						for (int i = 1, indx = 0; i < interNames.length; i++, indx++) {
 							JavaClass javaClass = (JavaClass) supers.get(i);
 							bca = BeaninfoClassAdapter.getBeaninfoClassAdapter(javaClass);
 							bca.introspectIfNecessary();	// Force introspection to get a valid super mod stamp.
 							superCE = bca.getClassEntry();
 							interNames[indx] = javaClass.getQualifiedNameForReflection();
 							if (superCE != null)
 								interMods[indx] = superCE.getModificationStamp();
 							else
 								interMods[indx] = ClassEntry.SUPER_UNDEFINED_MODIFICATION_STAMP;	// No classentry means undefined. So put something in so that when it becomes defined we will know.
 						}
 						ce.setInterfaceNames(interNames);
 						ce.setInterfaceModificationStamps(interMods);
 					}
 				} else {
 					ce.setSuperModificationStamp(IResource.NULL_STAMP);
 					ce.setInterfaceNames(null);
 					ce.setInterfaceModificationStamps(null);
 				}
 			} else {
 				// We are an override cache.
 				if (cache != null) { 
 					cres = ce.getPendingOverrideResource();
 					if (cres != null) {
 						// We have a pending, so clear and reuse the resource.
 						cres.getContents().clear();
 					} else {
 						// Not currently writing or waiting to write, so create a new resource.
 						cres = jclass.eResource().getResourceSet().createResource(
 								URI.createFileURI(rootIndex.getCachePath().append(className + OVERRIDE_CACHE_SUFFIX).toString()));
 					}
 					cacheRset = cres.getResourceSet();
 					cres.getContents().addAll((List) cache);
 					ce.setPendingOverrideResource(cres);
 					ce.setOverrideCacheExists(true);
 				} else {
 					ce.setPendingOverrideResource(null);
 					ce.setOverrideCacheExists(false);
 				}
 				ce.setConfigurationModificationStamp(Platform.getPlatformAdmin().getState(false).getTimeStamp());
 			}
 		}
 		queueClassEntry(ce, cacheRset); // Now queue it up.
 		return ce;
 	}
 
 	/**
 	 * Get the cache resource for the given java class.
 	 * <p>
 	 * NOTE: It is the responsibility of the caller to ALWAYS remove the Resource from its resource set when done with it.
 	 * 
 	 * @param jclass
 	 * @param ce the class entry for the jclass
 	 * @param reflectCache <code>true</code> if this the reflection/introspection cache or <code>false</code> if this is the override cache.
 	 * @return the loaded cache resource, or <code>null</code> if not there (for some reason) or an error trying to load it.
 	 * 
 	 * @since 1.1.0
 	 */
 	public Resource getCache(JavaClass jclass, ClassEntry ce, boolean reflectCache) {
 		String className = jclass.getQualifiedNameForReflection();
 		if (BeaninfoPlugin.getPlugin().getLogger().isLoggingLevel(Level.FINER)) {
 			Logger logger = BeaninfoPlugin.getPlugin().getLogger();
 			String type = reflectCache?"Class":"Overrides"; //$NON-NLS-1$ //$NON-NLS-2$
 			logger.log("Loading cache for class "+className+" cache type="+type, Level.FINER); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		if (reflectCache) {
 			boolean waitForJob = false;
 			synchronized (ce) {
 				if (ce.getPendingResource() != null) {
 					// We have one pending. So wait until write cache job is done, and then load it in.
 					// Note: Can't just copy the pending resource because it has references to JavaClasses
 					// and these could be in a different project (since this could be a workspace wide class).
 					// We would get the wrong java classes then when we apply it. 
 					waitForJob = true;
 					if (BeaninfoPlugin.getPlugin().getLogger().isLoggingLevel(Level.FINER))
 						BeaninfoPlugin.getPlugin().getLogger().log("Using pending class cache.", Level.FINER); //$NON-NLS-1$
 				}
 			}
 			if (waitForJob)
 				waitForCacheSaveJob();	
 
 			try {
 				return jclass.eResource().getResourceSet().getResource(
 						URI.createFileURI(ce.getRootIndex().getCachePath().append(
 								className + CLASS_CACHE_SUFFIX).toString()), true);
 			} catch (Exception e) {
 				// Something happened and couldn't load it.
 				// TODO - need to remove the Level.INFO arg when the beaninfo cache is working dynamically
 				BeaninfoPlugin.getPlugin().getLogger().log(e, Level.INFO);
 				return null;
 			}
 		} else {
 			boolean waitForJob = false;
 			synchronized (ce) {
 				if (ce.getPendingOverrideResource() != null) {
 					// We have one pending. So wait until write cache job is done, and then load it in.
 					// Note: Can't just copy the pending resource because it has references to JavaClasses
 					// and these could be in a different project (since this could be a workspace wide class).
 					// We would get the wrong java classes then when we apply it. 
 					waitForJob = true;
 					if (BeaninfoPlugin.getPlugin().getLogger().isLoggingLevel(Level.FINER))
 						BeaninfoPlugin.getPlugin().getLogger().log("Using pending override cache.", Level.FINER); //$NON-NLS-1$
 				}
 			}
 			if (waitForJob)
 				waitForCacheSaveJob();	
 
 			try {
 				return jclass.eResource().getResourceSet().getResource(
 						URI.createFileURI(ce.getRootIndex().getCachePath().append(
 								className + OVERRIDE_CACHE_SUFFIX).toString()), true);
 			} catch (Exception e) {
 				// Something happened and couldn't load it.
 				// TODO - need to remove the Level.INFO arg when the beaninfo cache is working dynamically
 				BeaninfoPlugin.getPlugin().getLogger().log(e, Level.INFO);
 				return null;
 			}
 		}
 	}
 
 	private synchronized ClassEntry getClassEntry(RootIndex rootIndex, String className, boolean createEntry) {
 		ClassEntry ce = (ClassEntry) rootIndex.classNameToClassEntry.get(className);
 		if (createEntry && ce == null) {
 			// Need to create one.
 			ce = new ClassEntry(rootIndex, className);
 			// Don't actually mark the rootIndex dirty until the cache for it is actually saved out.
 		}
 		return ce;
 	}
 
 	private static final String ROOT_PREFIX = "root"; //$NON-NLS-1$
 
 	/*
 	 * Get the root index for the appropriate cache for the given java class.
 	 */
 	private RootIndex getRootIndex(IType type) {
 		IPackageFragmentRoot root = (IPackageFragmentRoot) type.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
 		if (!root.isExternal()) {
 			// So it is in a project. Get the project index.
 			return getRootIndex(root, root.getJavaProject().getProject());
 		} else {
 			// It is an external jar (archive), so needs to come from main index, no project.
 			return getRootIndex(root, null);
 		}
 	}
 
 	/*
 	 * Get the root index for the given root. A Project index if project is not null.
 	 */
 	private synchronized RootIndex getRootIndex(IPackageFragmentRoot root, IProject project) {
 		Index index = project != null ? getProjectIndex(project) : getMainIndex();
 		IPath rootPath = root.getPath();
 		RootIndex rootIndex = (RootIndex) index.rootToRootIndex.get(rootPath);
 		if (rootIndex == null) {
 			// Need to do a new root path.
 			String rootName = ROOT_PREFIX + (++index.highRootNumber);
 			rootIndex = root.isArchive() ? createArchiveRootIndex(root, rootName, index) : new FolderRootIndex(rootName, index);
 			index.rootToRootIndex.put(rootPath, rootIndex);
 			// Don't set index dirty until we actually save a class cache file. Until then it only needs to be in memory.
 		}
 		rootIndex.setupIndex(project); // Set it up, or may already be set, so it will do nothing in that case.
 		return rootIndex;
 	}
 
 	/*
 	 * Create an archive root with the given root number and root.
 	 */
 	private RootIndex createArchiveRootIndex(IPackageFragmentRoot rootArchive, String rootName, Index index) {
 		long modStamp = IResource.NULL_STAMP;
 		if (rootArchive.isExternal()) {
 			modStamp = rootArchive.getPath().toFile().lastModified();
 		} else {
 			try {
 				modStamp = rootArchive.getUnderlyingResource().getModificationStamp();
 			} catch (JavaModelException e) {
 				BeaninfoPlugin.getPlugin().getLogger().log(e);
 			}
 		}
 		return new ArchiveRootIndex(rootName, modStamp, index);
 	}
 
 	private static final String INDEXFILENAME = ".index"; //$NON-NLS-1$
 
 	private static final String CACHEDIR = ".cache"; // Cache directory (so as not to conflict with any future BeanInfo Plugin specific data files. //$NON-NLS-1$
 
 	/*
 	 * Get the cache directory for the project (or if project is null, the main plugin cache directory).
 	 */
 	// TODO: make this one private
 	public static IPath getCacheDir(IProject project) {
 		if (project != null)
 			return project.getWorkingLocation(BeaninfoPlugin.getPlugin().getBundle().getSymbolicName()).append(CACHEDIR);
 		else
 			return BeaninfoPlugin.getPlugin().getStateLocation().append(CACHEDIR);
 	}
 
 	/*
 	 * Get the project index. Synchronized so that we can create it if necessary and not get race conditions.
 	 */
 	private synchronized Index getProjectIndex(IProject project) {
 		try {
 			Index index = (Index) project.getSessionProperty(PROJECT_INDEX_KEY);
 			if (index == null) {
 				// Read the index in.
 				File indexDirFile = getCacheDir(project).append(INDEXFILENAME).toFile();
 				if (indexDirFile.canRead()) {
 					ObjectInputStream ois = null;
 					try {
 						ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(indexDirFile)));
 						index = (Index) ois.readObject();
 					} catch (InvalidClassException e) {
 						// This is ok. It simply means the cache index is at a downlevel format and needs to be reconstructed.
 					} catch (IOException e) {
 						BeaninfoPlugin.getPlugin().getLogger().log(e);
 					} catch (ClassNotFoundException e) {
 						BeaninfoPlugin.getPlugin().getLogger().log(e);
 					} finally {
 						if (ois != null)
 							try {
 								ois.close();
 							} catch (IOException e) {
 								BeaninfoPlugin.getPlugin().getLogger().log(e);
 							}
 					}
 				}
 
 				if (index == null) {
 					// Doesn't yet exist or it couldn't be read for some reason, or it was downlevel cache in which case we just throw it away and create
 					// new).
 					index = new Index();
 					index.highRootNumber = 0;
 					index.rootToRootIndex = new HashMap();
 				}
 
 				project.setSessionProperty(PROJECT_INDEX_KEY, index); // We either created a new one, or we were able to load it.
 			}
 			return index;
 		} catch (CoreException e) {
 			// Shouldn't occur,
 			return null;
 		}
 	}
 
 	/*
 	 * Get the main index. Synchronized so that we can create it if necessary and not get race conditions.
 	 */
 	private synchronized Index getMainIndex() {
 		if (MAIN_INDEX == null) {
 			// Read the index in.
 			File indexDirFile = getCacheDir(null).append(INDEXFILENAME).toFile();
 			if (indexDirFile.canRead()) {
 				ObjectInputStream ois = null;
 				try {
 					ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(indexDirFile)));
 					MAIN_INDEX = (Index) ois.readObject();
 				} catch (InvalidClassException e) {
 					// This is ok. It just means that the cache index is at a downlevel format and needs to be reconstructed.
 				} catch (IOException e) {
 					BeaninfoPlugin.getPlugin().getLogger().log(e);
 				} catch (ClassNotFoundException e) {
 					BeaninfoPlugin.getPlugin().getLogger().log(e);
 				} finally {
 					if (ois != null)
 						try {
 							ois.close();
 						} catch (IOException e) {
 							BeaninfoPlugin.getPlugin().getLogger().log(e);
 						}
 				}
 			}
 
 			if (MAIN_INDEX == null) {
 				// Doesn't yet exist or it couldn't be read for some reason, or it was downlevel cache in which case we just throw it away and create
 				// new).
 				MAIN_INDEX = new Index();
 				MAIN_INDEX.highRootNumber = 0;
 				MAIN_INDEX.rootToRootIndex = new HashMap();
 			}
 
 		}
 		return MAIN_INDEX;
 	}
 
 	// -------------- Save Participant code -----------------
 
 	protected class SaveParticipant implements ISaveParticipant {
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.core.resources.ISaveParticipant#doneSaving(org.eclipse.core.resources.ISaveContext)
 		 */
 		public void doneSaving(ISaveContext context) {
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.core.resources.ISaveParticipant#prepareToSave(org.eclipse.core.resources.ISaveContext)
 		 */
 		public void prepareToSave(ISaveContext context) throws CoreException {
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.core.resources.ISaveParticipant#rollback(org.eclipse.core.resources.ISaveContext)
 		 */
 		public void rollback(ISaveContext context) {
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.core.resources.ISaveParticipant#saving(org.eclipse.core.resources.ISaveContext)
 		 */
 		public void saving(ISaveContext context) throws CoreException {
 			boolean fullsave = false;
 			switch (context.getKind()) {
 				case ISaveContext.PROJECT_SAVE:
 					IProject project = context.getProject();
 					synchronized (BeanInfoCacheController.INSTANCE) {
 						// Write the index. The cache save job will eventually run and at that point write out the pending cache files too.
 						// They don't need to be written before the project save is complete.
 						Index projectIndex = (Index) project.getSessionProperty(PROJECT_INDEX_KEY);
 						if (projectIndex != null && projectIndex.isDirty())
 							if (reconcileIndexDirectory(project, projectIndex))
 								writeIndex(project, projectIndex);
 							else {
 								// It was empty, just get rid of the index. The directories have already been cleared.
 								projectIndex.markDead();
 								project.setSessionProperty(PROJECT_INDEX_KEY, null);
 							}
 					}
 					break;
 				case ISaveContext.FULL_SAVE:
 					fullsave = true;
 					waitForCacheSaveJob();
 				// Now flow into the snapshot save to complete the fullsave.
 				case ISaveContext.SNAPSHOT:
 					// For a snapshot, just the dirty indexes, no clean up. If fullsave, cleanup the indexes, but only save the dirty.
 					synchronized (BeanInfoCacheController.INSTANCE) {
 						if (MAIN_INDEX != null) {
 							if (fullsave) {
 								if (reconcileIndexDirectory(null, MAIN_INDEX)) {
 									if (MAIN_INDEX.isDirty())
 										writeIndex(null, MAIN_INDEX);
 								} else {
 									// It was empty, just get rid of the index. The directories have already been cleared.
 									MAIN_INDEX.markDead();
 									MAIN_INDEX = null;
 								}
 							} else if (MAIN_INDEX.isDirty())
 								writeIndex(null, MAIN_INDEX);
 						}
 						// Now do the project indexes. We have to walk all open projects to see which have an index. Since we are
 						// doing a major save, the hit will ok
 						IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 						for (int i=0; i<projects.length; i++) {
 							project = projects[i];
 							if (project.isOpen()) {
 								Index index = (Index) project.getSessionProperty(PROJECT_INDEX_KEY);
 								if (index != null) {
 									if (fullsave) {
 										if (reconcileIndexDirectory(project, index)) {
 											if (index.isDirty())
 												writeIndex(project, index);
 										} else {
 											// It was empty, just get rid of the index from memory. It has already been deleted from disk.
 											index.markDead();
 											project.setSessionProperty(PROJECT_INDEX_KEY, null);
 										}
 									} else if (index.isDirty())
 										writeIndex(project, index);
 								}
 							}
 						}
 					}
 			}
 		}
 
 		/*
 		 * Write an index. Project if not null indicates a project index.
 		 */
 		private void writeIndex(IProject project, Index index) {
 			ObjectOutputStream oos = null;
 			try {
 				File indexDirFile = getCacheDir(project).toFile();
 				indexDirFile.mkdirs();
 				File indexFile = new File(indexDirFile, INDEXFILENAME);
 				oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(indexFile)));
 				oos.writeObject(index);
 				index.setDirty(false);
 			} catch (IOException e) {
 				BeaninfoPlugin.getPlugin().getLogger().log(e);
 			} finally {
 				if (oos != null)
 					try {
 						oos.close();
 					} catch (IOException e) {
 						BeaninfoPlugin.getPlugin().getLogger().log(e);
 					}
 			}
 		}
 
 		/*
 		 * Reconcile the index directory of unused (empty) root directories. If after reconciling the index is empty, then it will delete the index file too. @return
 		 * true if index not empty, false if index was empty and was erased too.
 		 */
 		private boolean reconcileIndexDirectory(IProject project, Index index) {
 			// clean out unused rootIndexes
 			File indexDir = getCacheDir(project).toFile();
 			if (indexDir.canWrite()) {
 				// Create a set of all root names for quick look up.
 				if (index.rootToRootIndex.isEmpty()) {
 					// It is empty, clear everything, including index file.
 					cleanDirectory(indexDir, false);
 					return false;
 				} else {
 					// Need a set of the valid rootnames for quick lookup of names in the directory list.
 					// And while accumulating this list, clean out the root indexes cache too (i.e. the class cache files).
 					final Set validFiles = new HashSet(index.rootToRootIndex.size());
 					validFiles.add(INDEXFILENAME);
 					for (Iterator itr = index.rootToRootIndex.values().iterator(); itr.hasNext();) {
 						RootIndex rootIndex = (RootIndex) itr.next();
 						if (reconcileClassCacheDirectory(rootIndex, project)) {
 							// The class cache has been reconciled, and there are still some classes left, so keep the root index.
 							validFiles.add(rootIndex.getRootName());
 						} else {
 							itr.remove(); // The root index is empty, so get rid of it. Since not a valid name, it will be deleted in next step.
 							index.setDirty(true); // Also set it dirty in case it wasn't because we need to write out the container Index since it was
 							// changed.
 						}
 					}
 					// Get list of files and delete those that are not a valid name (used root name, or index file)
 					String[] fileNames = indexDir.list();
 					for (int i = 0; i < fileNames.length; i++) {
 						if (!validFiles.contains(fileNames[i])) {
 							File file = new File(indexDir, fileNames[i]);
 							if (file.isDirectory())
 								cleanDirectory(file, true);
 							else
 								file.delete();
 						}
 					}
 					return true;
 				} 
 			} else 
 				return true;	// Can't write, so treat as leave alone.
 		}
 
 		/*
 		 * Reconcile the class cache directory for the root index. Return true if reconciled good but not empty. Return false if the class cache
 		 * directory is now empty. In this case we should actually get rid of the entire root index. This makes sure that the directory matches
 		 * the contents of the index by removing any file not found in the index.
 		 */
 		private boolean reconcileClassCacheDirectory(RootIndex rootIndex, IProject project) {
 			if (rootIndex.classNameToClassEntry.isEmpty())
 				return false; // There are no classes, so get rid the entire root index.
 			else {
 				final Set validFiles = rootIndex.classNameToClassEntry.keySet(); // The keys (classnames) are the filenames (without extension)
 																					// that
 				// should be kept.
 				File indexDir = getCacheDir(project).append(rootIndex.getRootName()).toFile();
 				// Get list of files that are not a valid name (used classname)
 				String[] fileNames = indexDir.list();
 				if (fileNames != null) {
 					for (int i = 0; i < fileNames.length; i++) {
 						String fileName = fileNames[i];
 						if (fileName.endsWith(OVERRIDE_CACHE_SUFFIX)) {
 							// Ends with out class cache extension, see if valid classname.
 							String classname = fileName.substring(0, fileName.length() - OVERRIDE_CACHE_SUFFIX.length());
 							ClassEntry ce = (ClassEntry) rootIndex.classNameToClassEntry.get(classname);
 							if (ce != null && ce.overrideCacheExists())
 								continue; // It is one of ours. Keep it.
 						} else if (fileName.endsWith(CLASS_CACHE_SUFFIX)) {
 							// Ends with out class cache extension, see if valid classname.
 							if (validFiles.contains(fileName.substring(0, fileName.length() - CLASS_CACHE_SUFFIX.length()))) // Strip down to just
 																																// class and see if
 																																// one of ours.
 								continue; // It is one of ours. Keep it.
 						}
 						// Not valid, get rid of it.
 						File file = new File(indexDir, fileName);
 						if (file.isDirectory())
 							cleanDirectory(file, true);
 						else
 							file.delete();
 
 					}
 				}
 				return true;
 			}
 		}
 	}
 	
 	private static void cleanDirectory(File dir, boolean eraseDir) {
 		if (dir.canWrite()) {
 			File[] files = dir.listFiles();
 			for (int i = 0; i < files.length; i++) {
 				if (files[i].isDirectory())
 					cleanDirectory(files[i], true);
 				else
 					files[i].delete();
 			}
 			if (eraseDir)
 				dir.delete();
 		}
 	}	
 
 	//-------------- Save Class Cache Entry Job -------------------
 	// This is write queue for class caches. It is a FIFO queue. It is sychronized so that adds/removes are controlled.
 	// Entries are ClassEntry's. The class entry has the resource that needs to be written out. It will be set to null
 	// by the job when it is written. The job will have a ClassEntry locked while it is retrieving and resetting the resource
 	// field in the entry.
 	//
 	// The process is the new cache will lock, create resource, set resource into the CE and release lock. Then add the CE to the queue
 	// and schedule the job (in case job is not running).
 	//
 	// The job will lock the CE, get resource from the CE, write it out, set it back to null, release the CE). If the resource is null,
 	// then it was already processed (this could happen if the job didn't get a chance to save it before another entry was posted
 	// and this is the second request and it was actually processed by the first request).
 	// IE:
 	// 1) resource created, queue entry added
 	// 2) 2nd req, job not processed yet, resource recreated and put back into CE, new queue entry.
 	// 3) job pulls from queue, locks ce, grabs resource, writes out the resource, sets back to null, release ce.
 	// 4) job pulls from queue. This time the resoure is null so it skips it.
 	//
 	// Need to lock Ce during entire create and write because the resource set is not reentrant so can't be writing it while creating it.
 
 	private List cacheWriteQueue = null;
 
 	void waitForCacheSaveJob() {
 		// For a full save we want to get the class cache files written too, so we need to manipulate the job to get it to finish ASAP.
 		if (cacheWriteJob != null) {
 			if (BeaninfoPlugin.getPlugin().getLogger().isLoggingLevel(Level.FINER))
 				BeaninfoPlugin.getPlugin().getLogger().log("Forcing a cache save job to start early.", Level.FINER); //$NON-NLS-1$
 			switch (cacheWriteJob.getState()) {
 				case Job.SLEEPING:
 					// It could be waiting a long time, so we need to wake it up at a high priority to get it running ASAP.
 					cacheWriteJob.setPriority(Job.INTERACTIVE); // Need to get it going right away
 					cacheWriteJob.wakeUp();
 					// Now drop into the wait.
 				default:
 					// Now wait for it (if not running this will return right away).
 					try {
 						cacheWriteJob.join();
 					} catch (InterruptedException e) {
 					}
 			}
 		}
 	}
 
 	static final Map SAVE_CACHE_OPTIONS;
 	static {
 		SAVE_CACHE_OPTIONS = new HashMap(3);
 		SAVE_CACHE_OPTIONS.put(XMLResource.OPTION_SAVE_TYPE_INFORMATION, Boolean.TRUE);
 		SAVE_CACHE_OPTIONS.put(XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE, Boolean.TRUE);
         SAVE_CACHE_OPTIONS.put(XMLResource.OPTION_ENCODING, "UTF-8"); //$NON-NLS-1$
 	}
 
 	protected Job cacheWriteJob = null;
 	protected Adapter projectReleaseAdapter = new AdapterImpl() {
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.emf.common.notify.impl.AdapterImpl#isAdapterForType(java.lang.Object)
 		 */
 		public boolean isAdapterForType(Object type) {
 			return type == BeanInfoCacheController.this;	// We're making the BeanInfoCacheController.this be the adapter type.
 		}
 		
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.emf.common.notify.impl.AdapterImpl#notifyChanged(org.eclipse.emf.common.notify.Notification)
 		 */
 		public void notifyChanged(Notification msg) {
 			if (msg.getEventType() == ProjectResourceSet.SPECIAL_NOTIFICATION_TYPE && msg.getFeatureID(BeanInfoCacheController.class) == ProjectResourceSet.PROJECTRESOURCESET_ABOUT_TO_RELEASE_ID) {
 				// This is an about to be closed. If we have an active write job, bring it up to top priority and wait for it to finish.
 				// This will make sure any resources in the project are written. There may not be any waiting, but this is doing a close
 				// project, which is slow already relatively speaking, that waiting for the cache write job to finish is not bad.
 				waitForCacheSaveJob();
 			}
 		}
 	};
 
 	private void queueClassEntry(ClassEntry ce, ResourceSet rset) {
 		if (cacheWriteQueue == null) {
 			cacheWriteQueue = Collections.synchronizedList(new LinkedList());
 			cacheWriteJob = new Job(BeaninfoCoreMessages.BeanInfoCacheController_Job_WriteBeaninfoCache_Title) { 
 
 				protected IStatus run(IProgressMonitor monitor) {
 					monitor.beginTask("", cacheWriteQueue.size() + 10); // This is actually can change during the run, so we add 10 for the heck of it. //$NON-NLS-1$
 					if (BeaninfoPlugin.getPlugin().getLogger().isLoggingLevel(Level.FINER))
 						BeaninfoPlugin.getPlugin().getLogger().log("Starting write BeanInfo Cache files.", Level.FINER); //$NON-NLS-1$
 					while (!monitor.isCanceled() && !cacheWriteQueue.isEmpty()) {
 						ClassEntry ce = (ClassEntry) cacheWriteQueue.remove(0); // Get first one.
 						boolean dead = false;
 						synchronized (BeanInfoCacheController.this) {
 							if (ce.getRootIndex().getIndex().isDead()) {
 								dead = true;	// The index is dead, so don't write it. We still need to go through and get the pending resource out of its resource set so that it goes away.
 							}
 						}
 						synchronized (ce) {
 							Resource cres = ce.getPendingResource();
 							if (cres != null) {
 								try {
 									if (!dead)
 										cres.save(SAVE_CACHE_OPTIONS);
 								} catch (IOException e) {
 									BeaninfoPlugin.getPlugin().getLogger().log(e);
 								} finally {
 									// Remove the resource from resource set, clear out the pending.
 									cres.getResourceSet().getResources().remove(cres);
 									ce.setPendingResource(null);
 								}
 							}
 							cres = ce.getPendingOverrideResource();
 							if (cres != null) {
 								try {
 									if (!dead)
 										cres.save(SAVE_CACHE_OPTIONS);
 								} catch (IOException e) {
 									BeaninfoPlugin.getPlugin().getLogger().log(e);
 								} finally {
 									// Remove the resource from resource set, clear out the pending.
 									cres.getResourceSet().getResources().remove(cres);
 									ce.setPendingOverrideResource(null);
 								}
 							}
 							
 							monitor.worked(1);
 						}
 					}
 					monitor.done();
 					if (BeaninfoPlugin.getPlugin().getLogger().isLoggingLevel(Level.FINER))
 						BeaninfoPlugin.getPlugin().getLogger().log("Finished write BeanInfo Cache files.", Level.FINER); //$NON-NLS-1$
 					return Status.OK_STATUS;
 				}
 			};
 			cacheWriteJob.setPriority(Job.SHORT);
 			cacheWriteJob.setSystem(true);
 		}
 		if (rset != null && EcoreUtil.getExistingAdapter(rset, this) == null) {
 			// If it is a project resource set, then add ourselves as listeners so we know when released.
 			if (rset instanceof ProjectResourceSet)
 				rset.eAdapters().add(projectReleaseAdapter);
 		}
 		cacheWriteQueue.add(ce);
 		cacheWriteJob.schedule(60 * 1000L); // Put off for 1 minute to let other stuff go on. Not important that it happens immediately.
 	}
 }
