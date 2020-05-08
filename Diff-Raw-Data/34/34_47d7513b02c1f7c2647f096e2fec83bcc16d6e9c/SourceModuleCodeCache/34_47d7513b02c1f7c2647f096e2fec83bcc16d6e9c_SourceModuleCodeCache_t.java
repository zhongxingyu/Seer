 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.internal.core;
 
 import org.apache.commons.collections.map.ReferenceMap;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.internal.core.util.Util;
 
 public class SourceModuleCodeCache implements ISourceCodeCache {
 
 	private class ChangeListener implements IResourceChangeListener,
 			IResourceDeltaVisitor {
 
 		public void resourceChanged(IResourceChangeEvent event) {
 			try {
 				event.getDelta().accept(this);
 			} catch (CoreException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		public boolean visit(IResourceDelta delta) throws CoreException {
 			final IResource resource = delta.getResource();
 			if (resource.getType() == IResource.FILE
 					&& (delta.getFlags() & IResourceDelta.CONTENT) != 0) {
 				switch (delta.getKind()) {
 				case IResourceDelta.ADDED:
 				case IResourceDelta.CHANGED:
 					removeFileEntry((IFile) resource);
 					break;
 				}
 				return false;
 			}
 			return true;
 		}
 
 	};
 
 	private IResourceChangeListener listener = new ChangeListener();
 
 	protected void removeFileEntry(IFile resource) {
 		synchronized (resourceMap) {
 			resourceMap.remove(resource);
 		}
 	}
 
 	private final ReferenceMap resourceMap = new ReferenceMap(
 			ReferenceMap.HARD, ReferenceMap.SOFT);
 
 	private static final long EXTERNAL_CACHE_LIFETIME = 10 * 60 * 1000;
 
 	private static class ExternalCacheEntry {
 		final char[] content;
 		final long addTime;
 
 		/**
 		 * @param result
 		 * @param currentTimeMillis
 		 */
 		public ExternalCacheEntry(char[] result, long currentTimeMillis) {
 			this.content = result;
 			this.addTime = currentTimeMillis;
 		}
 
 	}
 
 	private final ReferenceMap externalResourceMap = new ReferenceMap(
 			ReferenceMap.HARD, ReferenceMap.SOFT);
 
 	public SourceModuleCodeCache() {
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
 				IResourceChangeEvent.POST_CHANGE);
 	}
 
 	public void stop() {
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
 	}
 
 	public char[] get(IFile file) throws ModelException {
 		synchronized (resourceMap) {
 			final char[] result = (char[]) resourceMap.get(file);
 			if (result != null) {
 				return result;
 			}
 		}
 		// Get encoding from file
 		String encoding = null;
 		try {
 			encoding = file.getCharset();
 		} catch (CoreException ce) {
 			// do not use any encoding
 		}
 		final char[] result = Util.getResourceContentsAsCharArray(file,
 				encoding);
 		synchronized (resourceMap) {
 			resourceMap.put(file, result);
 		}
 		return result;
 	}
 
 	public char[] get(IFileHandle file) throws ModelException {
 		synchronized (externalResourceMap) {
 			final ExternalCacheEntry entry = (ExternalCacheEntry) externalResourceMap
 					.get(file);
 			if (entry != null
 					&& entry.addTime + EXTERNAL_CACHE_LIFETIME > System
 							.currentTimeMillis()) {
 				return entry.content;
 			}
 		}
 		final char[] result = Util.getResourceContentsAsCharArrayNoCache(file);
 		synchronized (externalResourceMap) {
 			externalResourceMap.put(file, new ExternalCacheEntry(result, System
 					.currentTimeMillis()));
 		}
 		return result;
 	}
 
	public void remove(IFile file) {
		synchronized (resourceMap) {
			resourceMap.remove(file);
		}
	}
 }
