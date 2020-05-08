 package org.eclipse.dltk.core.caching;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.compiler.util.Util;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.RuntimePerformanceMonitor;
 import org.eclipse.dltk.core.RuntimePerformanceMonitor.PerformanceNode;
 import org.eclipse.dltk.core.caching.cache.CacheEntry;
 import org.eclipse.dltk.core.caching.cache.CacheEntryAttribute;
 import org.eclipse.dltk.core.caching.cache.CacheIndex;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
 
 /**
  * This cache provider checks for folder .index files and load such files into
  * required cache.
  * 
  * @author Andrei Sobolev
  */
 public class ArchiveContentCacheProvider implements IContentCacheProvider {
 	private IContentCache cache;
 
 	public ArchiveContentCacheProvider() {
 	}
 
 	public InputStream getAttributeAndUpdateCache(IFileHandle handle,
 			String attribute) {
 		if (handle == null) {
 			return null;
 		}
 		IFileHandle parent = handle.getParent();
 		String DLTK_INDEX_FILE = ".dltk.index";
 		// Check for additional indexes
 		if (processIndexFile(handle, attribute, parent, parent
 				.getChild(DLTK_INDEX_FILE), cache)) {
 			return cache.getCacheEntryAttribute(handle, attribute);
 		}
 		IFileHandle[] children = parent.getChildren();
 		for (IFileHandle fileHandle : children) {
 			String fileName = fileHandle.getName();
 			if (fileName.startsWith(DLTK_INDEX_FILE)
 					&& !fileName.equals(DLTK_INDEX_FILE)) {
 				if (processIndexFile(handle, attribute, parent, fileHandle,
 						cache)) {
 					return cache.getCacheEntryAttribute(handle, attribute);
 				}
 			}
 		}
 		return null;
 	}
 
 	public static void processFolderIndexes(IFileHandle folder,
 			IContentCache cache, IProgressMonitor monitor) {
 		String DLTK_INDEX_FILE = ".dltk.index";
 		// Check for additional indexes
 		IFileHandle[] children = folder.getChildren();
 		List<IFileHandle> indexFiles = new ArrayList<IFileHandle>();
 		for (IFileHandle fileHandle : children) {
 			String fileName = fileHandle.getName();
 			if (fileName.startsWith(DLTK_INDEX_FILE)
 					&& !fileName.equals(DLTK_INDEX_FILE)) {
 				indexFiles.add(fileHandle);
 			}
 		}
 		SubProgressMonitor processingIndexes = new SubProgressMonitor(monitor,
 				1);
 		processingIndexes
 				.beginTask("Processing index files", indexFiles.size());
 		for (IFileHandle fileHandle : indexFiles) {
 			processingIndexes.subTask("Processing:" + fileHandle.toOSString());
 			processIndexFile(null, null, folder, fileHandle, cache);
 			processingIndexes.worked(1);
 		}
 		processingIndexes.done();
 	}
 
 	private static boolean processIndexFile(IFileHandle handle,
 			String attribute, IFileHandle parent, IFileHandle indexFile,
 			IContentCache cache) {
 		if (indexFile != null && indexFile.exists()) {
 			String stamp = cache.getCacheEntryAttributeString(indexFile,
 					"timestamp");
 			String fStamp = Long.toString(indexFile.lastModified());
 			if (stamp != null) {
 				if (fStamp.equals(stamp)) {
 					return false;
 				}
 			}
 			try {
 				boolean found = processIndexFile(handle, attribute, parent,
 						indexFile, fStamp, cache);
 				return found;
 			} catch (IOException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return false;
 	}
 
 	public static boolean processIndexFile(IFileHandle handle,
 			String attribute, IFileHandle parent, IFileHandle indexFile,
 			String fStamp, IContentCache cache) throws IOException,
 			ZipException {
 		File zipFileHandle = cache.getEntryAsFile(indexFile, "handle");
 
 		if (!zipFileHandle.exists()) {
 			BufferedInputStream inp = new BufferedInputStream(indexFile
 					.openInputStream(new NullProgressMonitor()), 4096);
 			PerformanceNode p = RuntimePerformanceMonitor.begin();
 			Util.copy(zipFileHandle, inp);
 			inp.close();
 			p.done("#", "Indexes read", zipFileHandle.length(), indexFile
 					.getEnvironment());
 		}
 		ZipFile zipFile = new ZipFile(zipFileHandle);
 
 		ZipEntry entry = zipFile.getEntry(".index");
		Resource indexResource = new XMIResourceImpl(URI
 				.createURI("dltk_cache://zipIndex"));
 		indexResource.load(zipFile.getInputStream(entry), null);
 		EList<EObject> contents = indexResource.getContents();
 		boolean found = false;
 		for (EObject eObject : contents) {
 			CacheIndex cacheIndex = (CacheIndex) eObject;
 			EList<CacheEntry> entries = cacheIndex.getEntries();
 			for (CacheEntry cacheEntry : entries) {
 				String path = cacheEntry.getPath();
 				IFileHandle entryHandle = new WrapTimeStampHandle(parent
 						.getChild(path), cacheEntry.getTimestamp());
 				// cache.setCacheEntryAttribute(entryHandle, "timestamp",
 				// cacheEntry.getTimestamp());
 				EList<CacheEntryAttribute> attributes = cacheEntry
 						.getAttributes();
 				for (CacheEntryAttribute cacheEntryAttribute : attributes) {
 					if (handle != null && attribute != null) {
 						if (attribute.equals(cacheEntryAttribute.getName())
 								&& cacheEntry.getPath()
 										.equals(handle.getName())) {
 							found = true;
 						}
 					}
 					OutputStream stream = null;
 					stream = cache.getCacheEntryAttributeOutputStream(
 							entryHandle, cacheEntryAttribute.getName());
 					String location = cacheEntryAttribute.getLocation();
 					ZipEntry zipEntry = zipFile.getEntry(location);
 					zipFile.getInputStream(zipEntry);
 					InputStream inputStream;
 					try {
 						inputStream = zipFile.getInputStream(zipEntry);
 						Util.copy(inputStream, stream);
 						stream.close();
 						inputStream.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		cache.setCacheEntryAttribute(indexFile, "timestamp", fStamp);
 		return found;
 	}
 
 	public void setCache(IContentCache cache) {
 		this.cache = cache;
 	}
 }
