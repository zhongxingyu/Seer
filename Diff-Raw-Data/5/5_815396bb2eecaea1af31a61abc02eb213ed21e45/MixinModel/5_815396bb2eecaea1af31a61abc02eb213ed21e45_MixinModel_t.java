 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.core.mixin;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.ElementChangedEvent;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IElementChangedListener;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementDelta;
 import org.eclipse.dltk.core.IModelElementVisitor;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.RuntimePerformanceMonitor;
 import org.eclipse.dltk.core.RuntimePerformanceMonitor.PerformanceNode;
 import org.eclipse.dltk.core.mixin.IMixinRequestor.ElementInfo;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.core.search.indexing.IIndexConstants;
 import org.eclipse.dltk.internal.core.OverflowingLRUCache;
 import org.eclipse.dltk.internal.core.mixin.IInternalMixinElement;
 import org.eclipse.dltk.internal.core.mixin.MixinCache;
 import org.eclipse.dltk.internal.core.mixin.MixinManager;
 import org.eclipse.dltk.internal.core.util.LRUCache;
 
 public class MixinModel {
 	private static final long REQUEST_CACHE_EXPIRE_TIME = 2000;
 	private static final boolean DEBUG = false;
 	private static final boolean TRACE = false;
 
 	public static final String SEPARATOR = String
 			.valueOf(IIndexConstants.SEPARATOR);
 
 	private static final int CACHE_LIMIT = 250000;
 	private static final int KEYS_CACHE_LIMIT = 500000;
 	private static final int REQUEST_CACHE_LIMIT = 500;
 
 	private final MixinCache cache;
 
 	/**
 	 * Contains map of source modules to mixin elements.
 	 */
 	private Map<ISourceModule, List<MixinElement>> elementToMixinCache = new HashMap<ISourceModule, List<MixinElement>>();
 
 	private final RequestCache requestCache = new RequestCache(
 			REQUEST_CACHE_LIMIT);
 
 	// true if exists, false if doesn't
 	private Map<String, Boolean> knownKeysCache = new HashMap<String, Boolean>();
 	// boolean, for the atomicity of it
 	public boolean removes = true;
 
 	private final IDLTKLanguageToolkit toolkit;
 
 	private final IScriptProject project;
 
 	private MixinRequestor mixinRequestor = new MixinRequestor();
 
 	private ISourceModule currentModule;
 
 	/**
 	 * modules required to be reparsed
 	 */
 	private Set<ISourceModule> modulesToReparse = new HashSet<ISourceModule>();
 
 	/**
 	 * Creates workspace instance
 	 * 
 	 * @param toolkit
 	 */
 	public MixinModel(IDLTKLanguageToolkit toolkit) {
 		this(toolkit, null);
 	}
 
 	/**
 	 * Creates project instance
 	 * 
 	 * @param toolkit
 	 * @param project
 	 */
 	public MixinModel(IDLTKLanguageToolkit toolkit, IScriptProject project) {
 		this.toolkit = toolkit;
 		this.project = project;
 
 		// long maxMemory = Runtime.getRuntime().freeMemory();
 
 		this.cache = new MixinCache(CACHE_LIMIT);
 		DLTKCore.addElementChangedListener(changedListener,
 				ElementChangedEvent.POST_CHANGE);
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(
 				changedListener);
 		MixinModelRegistry.register(this);
 	}
 
 	public void stop() {
 		DLTKCore.removeElementChangedListener(changedListener);
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(
 				changedListener);
 		MixinModelRegistry.unregister(this);
 	}
 
 	// long-running operation
 	public IMixinElement get(String key) {
 		if (DLTKCore.VERBOSE) {
 			System.out.println("MixinModel.get(" + key + ')'); //$NON-NLS-1$
 		}
 		MixinElement element = null;
 		synchronized (this) {
 			if (knownKeysCache.get(key) == Boolean.FALSE) {
 				return null;
 			}
 			element = (MixinElement) cache.get(key);
 			if (element == null) {
 				if (!removes) {
 					return null;
 				} else {
 					element = new MixinElement(key, currentModule);
 					cache.put(key, element);
 					cache.ensureSpaceLimit(1, element);
 				}
 			}
 			if (DLTKCore.VERBOSE) {
 				System.out
 						.println("Filling ratio:" + this.cache.fillingRatio()); //$NON-NLS-1$
 				this.cache.printStats();
 			}
 		}
 		buildElementTree(element);
 		synchronized (this) {
 			if (element.isFinal() && element.sourceModules.size() > 0) {
 				knownKeysCache.put(key, Boolean.TRUE);
 				return element;
 			}
 			knownKeysCache.put(key, Boolean.FALSE);
 			cache.remove(element.key);
 			cache.resetSpaceLimit(CACHE_LIMIT, element);
 		}
 		return null;
 	}
 
 	private IDLTKSearchScope createSearchScope() {
 		if (project != null) {
 			return SearchEngine.createSearchScope(project);
 		} else {
 			return SearchEngine.createWorkspaceScope(toolkit);
 		}
 	}
 
 	private static class RequestCacheEntry {
 		long expireTime;
 		String prefix = null;
 		Set<ISourceModule> modules = null;
 		Set<String> keys = null;
 	}
 
 	private static class RequestCache extends OverflowingLRUCache {
 
 		public RequestCache(int size) {
 			super(size);
 		}
 
 		public RequestCache(int size, int overflow) {
 			super(size, overflow);
 		}
 
 		protected boolean close(LRUCacheEntry entry) {
 			return true;
 		}
 
 		protected LRUCache newInstance(int size, int overflow) {
 			return new RequestCache(size, overflow);
 		}
 	};
 
 	/**
 	 * @deprecated
 	 */
 	public IMixinElement[] find(String pattern, long delta) {
 		return find(pattern, new NullProgressMonitor());
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public IMixinElement[] find(String pattern, IProgressMonitor monitor) {
 		// long-running operation
 		long start = TRACE ? System.currentTimeMillis() : 0;
 
 		RequestCacheEntry entry = findFromMixin(pattern, monitor);
 
 		if (entry.modules == null || entry.modules.size() == 0) {
 			return new IMixinElement[0];
 		}
 
 		long parses = TRACE ? System.currentTimeMillis() : 0;
 		for (ISourceModule module : entry.modules) {
 			reportModule(module);
 		}
 		long parsee = TRACE ? System.currentTimeMillis() : 0;
 
 		Set<MixinElement> result = new HashSet<MixinElement>();
 
 		synchronized (this) {
 			for (String key : entry.keys) {
 				MixinElement element = getCreateEmpty(key);
 				if (!monitor.isCanceled()) {
 					markElementAsFinal(element);
 				}
 				addKeyToSet(result, element, pattern);
 			}
 		}
 		if (TRACE) {
 			long end = System.currentTimeMillis();
 			System.out.println("MixinModel::find.time:" //$NON-NLS-1$
 					+ String.valueOf(end - start));
 			System.out.println("MixinModel::find.parsetime:" //$NON-NLS-1$
 					+ String.valueOf(parsee - parses));
 		}
 
 		return result.toArray(new IMixinElement[result.size()]);
 	}
 
 	// called with lock being held
 	private void addKeyToSet(Set<MixinElement> result,
 			MixinElement element, String pattern) {
 		// Skip all not matched keys
 		if (!CharOperation.match(pattern.toCharArray(),
 				element.key.toCharArray(), true)) {
 			return;
 		}
 		result.add(element);
 		knownKeysCache.put(element.key, Boolean.TRUE);
 		for (MixinElement child : element.children)
 			addKeyToSet(result, child, pattern);
 	}
 
 	// long-running operation
 	private RequestCacheEntry findFromMixin(String pattern,
 			IProgressMonitor monitor) {
 		PerformanceNode p = RuntimePerformanceMonitor.begin();
 		RequestCacheEntry entry;
 		synchronized (this) {
 			entry = (RequestCacheEntry) requestCache.get(pattern);
 			if (entry != null && entry.expireTime >= System.currentTimeMillis())
 				return entry;
 			entry = new RequestCacheEntry();
 			// TODO searches with clashing keys
 			// requestCache.put(pattern, entry);
 		}
 		Map<ISourceModule, Set<String>> keys = new HashMap<ISourceModule, Set<String>>();
 		ISourceModule[] containedModules = null;
 		try {
 			containedModules = SearchEngine.searchMixinSources(
 					createSearchScope(), pattern, toolkit, keys, monitor);
 		} catch (OperationCanceledException e) {
 			return entry;
 		}
 		entry.expireTime = System.currentTimeMillis()
 				+ REQUEST_CACHE_EXPIRE_TIME;
 		entry.modules = new HashSet<ISourceModule>(
 				Arrays.asList(containedModules));
 		entry.prefix = pattern;
 		entry.keys = new HashSet<String>();
 		for (Set<String> strs : keys.values()) {
 			entry.keys.addAll(strs);
 		}
 		if (!monitor.isCanceled()) {
 			synchronized (this) {
 				requestCache.put(pattern, entry);
 			}
 		}
 		p.done(getNature(), "Mixin model search items", 0);
 		return entry;
 	}
 
 	/**
 	 * @deprecated
 	 */
 	public IMixinElement[] find(String pattern) {
 		return find(pattern, new NullProgressMonitor());
 	}
 
 	/**
 	 * @deprecated
 	 */
 	public String[] findKeys(String pattern) {
 		return findKeys(pattern, new NullProgressMonitor());
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public String[] findKeys(String pattern, IProgressMonitor monitor) {
 		RequestCacheEntry entry = findFromMixin(pattern, monitor);
 		return entry.keys.toArray(new String[entry.keys.size()]);
 	}
 
 	// long-running operation
 	public boolean keyExists(String key) {
 		synchronized (this) {
 			// TODO: For this version we cache all information, so should be
 			// false.
 			if (!removes) {
 				return cache.get(key) != null;
 			}
 			MixinElement e = (MixinElement) this.cache.get(key);
 			if (e != null && e.sourceModules.size() > 0) {
 				return true;
 			}
 			Boolean cached = knownKeysCache.get(key);
 			if (cached != null)
 				return cached;
 		}
 		boolean exists = get(key) != null;
 		synchronized (this) {
 			if (knownKeysCache.size() > KEYS_CACHE_LIMIT) {
 				knownKeysCache.clear();
 			}
 			knownKeysCache.put(key, exists);
 		}
 		return exists;
 	}
 
 	// long-running operation
 	private void buildElementTree(MixinElement element) {
 		// TODO: This is consistent cache stage
 		if (element.isFinal()) {
 			return;
 		}
 		ISourceModule[] containedModules = findModules(element.getKey());
 		if (containedModules.length == 0) {
 			synchronized (cache) {
 				cache.remove(element.key);
 				cache.resetSpaceLimit(CACHE_LIMIT, element);
 			}
 			return;
 		}
 		for (ISourceModule module : containedModules) {
 			reportModule(module);
 		}
 		// mark selected element and all subelements as finished.
 		synchronized (this) {
 			markElementAsFinal(element);
 		}
 	}
 
 	// called with lock being held
 	private void markElementAsFinal(MixinElement element) {
 		element.bFinal = true;
 		for (MixinElement child : element.children) {
 			markElementAsFinal(child);
 		}
 	}
 
 	// TODO long-running operation. shouldn't be synchronized
 	public synchronized void reportModule(ISourceModule sourceModule) {
 			if (!elementToMixinCache.containsKey(sourceModule)) {
 				elementToMixinCache.put(sourceModule,
 						new ArrayList<MixinElement>());
 			} else {
 				// Module already in model. So we do not to rebuild it.
 				if (!modulesToReparse.remove(sourceModule)) {
 					return;
 				}
 				// We need to reparse module if some elements are moved from it.
 			}
 		try {
 			IMixinParser mixinParser = MixinManager
 					.getMixinParser(sourceModule);
 			if (mixinParser != null) {
 				this.currentModule = sourceModule;
 				mixinParser.setRequirestor(mixinRequestor);
 				mixinParser.parserSourceModule(true, sourceModule);
 				this.currentModule = null;
 			}
 		} catch (CoreException e) {
 			DLTKCore.error("Error in reportModule", e); //$NON-NLS-1$
 			return;
 		}
 	}
 
 	/**
 	 * Should find all elements source modules to be sure we build complete
 	 * child tree.
 	 * 
 	 * @param element
 	 * @return
 	 * @since 2.0
 	 */
 	public ISourceModule[] findModules(String key, IProgressMonitor monitor) {
 		RequestCacheEntry entry = findFromMixin(key, monitor);
 		return entry.modules.toArray(new ISourceModule[entry.modules.size()]);
 	}
 
 	/**
 	 * @Deprecated
 	 */
 	public ISourceModule[] findModules(String key) {
 		return findModules(key, new NullProgressMonitor());
 	}
 
 	/**
 	 * Returns a mixin element from this.cache by it's key, or creates new one
 	 * if cache doesn't contain required element
 	 * 
 	 * @param key
 	 * @return
 	 */
 	private MixinElement getCreateEmpty(String key) {
 		// called with lock being held
 		MixinElement element = (MixinElement) cache.get(key);
 		if (element == null) {
 			element = new MixinElement(key, currentModule);
 			this.cache.put(key, element);
 			this.cache.ensureSpaceLimit(1, element);
 		}
 		return element;
 	}
 
 	private interface IMixinChangedListener extends IElementChangedListener,
 			IResourceChangeListener {
 	}
 
 	private IMixinChangedListener changedListener = new IMixinChangedListener() {
 		public void elementChanged(ElementChangedEvent event) {
 			IModelElementDelta delta = event.getDelta();
 			synchronized (MixinModel.this) {
 				processDelta(delta);
 			}
 		}
 
 		// called with lock being held
 		private void processDelta(IModelElementDelta delta) {
 			IModelElement element = delta.getElement();
 			if (delta.getKind() == IModelElementDelta.REMOVED
 					|| delta.getKind() == IModelElementDelta.CHANGED
 					|| (delta.getFlags() & IModelElementDelta.F_REMOVED_FROM_BUILDPATH) != 0
 					|| (delta.getFlags() & IModelElementDelta.CHANGED) != 0) {
 				if (element.getElementType() != IModelElement.SOURCE_MODULE
 						&& element.getElementType() != IModelElement.PROJECT_FRAGMENT
 						&& element.getElementType() != IModelElement.SCRIPT_FOLDER
 						&& element.getElementType() != IModelElement.SCRIPT_MODEL
 						&& element.getElementType() != IModelElement.SCRIPT_PROJECT) {
 					ISourceModule module = (ISourceModule) element
 							.getAncestor(IModelElement.SOURCE_MODULE);
 					MixinModel.this.remove(module);
 				}
 				if (element.getElementType() == IModelElement.SOURCE_MODULE) {
 					MixinModel.this.remove((ISourceModule) element);
 				}
 			}
 
 			if (element.getElementType() == IModelElement.SCRIPT_PROJECT
 					&& delta.getKind() == IModelElementDelta.CHANGED
 					&& (delta.getFlags() & IModelElementDelta.F_BUILDPATH_CHANGED) != 0) {
 				clear();
 				return;
 			} else if ((delta.getKind() == IModelElementDelta.REMOVED || delta
 					.getKind() == IModelElementDelta.CHANGED)
 					&& (element.getElementType() == IModelElement.SCRIPT_FOLDER || element
 							.getElementType() == IModelElement.PROJECT_FRAGMENT)) {
 				if (delta.getAffectedChildren().length == 0) {
 					try {
 						element.accept(new IModelElementVisitor() {
 							public boolean visit(IModelElement element) {
 								if (element.getElementType() == ISourceModule.SOURCE_MODULE) {
 									remove((ISourceModule) element);
 									return false;
 								}
 								return true;
 							}
 						});
 					} catch (ModelException e) {
 						if (DLTKCore.DEBUG) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 			if (delta.getKind() == IModelElementDelta.ADDED) {
 				if (element.getElementType() == IModelElement.SOURCE_MODULE) {
 					if (modulesToReparse.add((ISourceModule) element)) {
 						reportModule((ISourceModule) element);
 					}
 				}
 				knownKeysCache.clear();
 				requestCache.flush();
 			}
 
 			if ((delta.getFlags() & IModelElementDelta.F_CHILDREN) != 0) {
 				for (IModelElementDelta child : delta.getAffectedChildren()) {
 					processDelta(child);
 				}
 			} else if (delta.getKind() == IModelElementDelta.REMOVED
 					&& element.getElementType() == IModelElement.SCRIPT_FOLDER) {
 				/* folder delete delta has no children */
 				MixinModel.this.removeFolder((IScriptFolder) element);
 			}
 		}
 
 		public void resourceChanged(IResourceChangeEvent event) {
 			int eventType = event.getType();
 			IResource resource = event.getResource();
 			// IResourceDelta delta = event.getDelta();
 
 			switch (eventType) {
 			case IResourceChangeEvent.PRE_CLOSE:
 				if (resource.getType() == IResource.PROJECT
 						&& DLTKLanguageManager
 								.hasScriptNature((IProject) resource)) {
 					if (project != null
 							&& resource.equals(project.getProject())) {
 						clear();
 						// TODO destroy this model
 						return;
 					}
 				}
 				break;
 			case IResourceChangeEvent.PRE_DELETE:
 				if (resource.getType() == IResource.PROJECT
 						&& DLTKLanguageManager
 								.hasScriptNature((IProject) resource)) {
 					if (project != null
 							&& resource.equals(project.getProject())) {
 						clear();
 						// TODO destroy this model
 						return;
 					}
 					// remove all resources with given project from model.
 					List<ISourceModule> toRemove = new ArrayList<ISourceModule>();
 					synchronized (this) {
 						IProject project = (IProject) resource;
 						for (ISourceModule module : elementToMixinCache
 								.keySet()) {
 							IScriptProject scriptProject = module
 									.getScriptProject();
 							if (scriptProject != null) {
 								IProject prj = scriptProject.getProject();
 								if ((prj != null && prj.equals(project))
 										|| prj == null) {
 									toRemove.add(module);
 								}
 							} else {
 								toRemove.add(module);
 							}
 						}
 						for (ISourceModule module : toRemove) {
 							remove(module);
 						}
 					}
 				}
 				return;
 			}
 		}
 	};
 
 	// private synchronized void clearAllElementsState() {
 	// Enumeration elements = cache.elements();
 	// while( elements.hasMoreElements() ) {
 	// MixinElement o = (MixinElement)elements.nextElement();
 	// o.bFinal = false;
 	// }
 	// }
 
 	private final String getLogContext() {
 		if (project == null) {
 			return "[MixinModel|$" + toolkit.getLanguageName() + "$]"; //$NON-NLS-1$ //$NON-NLS-2$
 		} else {
 			return "[MixinModel|" + project.getElementName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 
 	private final void log(String message) {
 		System.out.println(getLogContext() + " " + message); //$NON-NLS-1$
 	}
 
 	public synchronized void remove(ISourceModule element) {
 		if (DEBUG) {
 			log("remove " + element.getElementName()); //$NON-NLS-1$
 		}
 		List<MixinElement> mixinCache = elementToMixinCache.get(element);
 		if (mixinCache != null) {
 			removeFromRequestCache(element);
 
 			for (MixinElement mixin : mixinCache) {
 				removes = true;
 				knownKeysCache.remove(mixin.key);
 				mixin.bFinal = false;
 				mixin.sourceModules.remove(element);
 				mixin.sourceModuleToObject.remove(element);
 				if (mixin.sourceModules.size() == 0) {
 					// Remove frob parent.
 					String parentKey = mixin.getParentKey();
 					if (parentKey != null) {
 						MixinElement parent = (MixinElement) this.cache
 								.get(parentKey);
 						if (parent != null) {
 							parent.children.remove(mixin);
 							parent.bFinal = false;
 						}
 					}
 					// Remove from cache
 					cache.remove(mixin.key);
 					cache.resetSpaceLimit(CACHE_LIMIT, mixin);
 				}
 			}
 			this.elementToMixinCache.remove(element);
 		}
 	}
 
 	/**
 	 * @param folder
 	 */
 	protected synchronized void removeFolder(IScriptFolder folder) {
 		final IPath folderPath = folder.getPath();
 		final List<ISourceModule> modulesToRemove = new ArrayList<ISourceModule>();
 		for (final ISourceModule module : elementToMixinCache.keySet()) {
 			final IPath path = module.getPath();
 			if (folderPath.isPrefixOf(path)) {
 				modulesToRemove.add(module);
 			}
 		}
 		for (ISourceModule module : modulesToRemove) {
 			remove(module);
 		}
 	}
 
 	// called with lock being held
 	private void removeFromRequestCache(ISourceModule element) {
 		// Clear requests cache.
 		@SuppressWarnings("unchecked")
 		Enumeration<RequestCacheEntry> enumeration = this.requestCache
 				.elements();
 		while (enumeration.hasMoreElements()) {
 			RequestCacheEntry entry = enumeration.nextElement();
 			if (entry.modules != null) {
 				if (entry.modules.contains(element)) {
 					// we can do it now
 					this.requestCache.remove(entry.prefix);
 				}
 			}
 		}
 	}
 
 	/***************************************************************************
 	 * Then getObjects are called, special initialize listener are called.
 	 * 
 	 */
 	public interface IMixinObjectInitializeListener {
 		void initialize(IMixinElement element, Object object,
 				ISourceModule module);
 	}
 
 	private final ListenerList mixinObjectInitializeListeners = new ListenerList();
 
	private static final Object[] NO_OBJECTS = new Object[0];

 	private final class MixinElement implements IMixinElement,
 			IInternalMixinElement {
 		private String key;
 		private boolean bFinal = false;
 		private List<ISourceModule> sourceModules = new ArrayList<ISourceModule>();
 		private Map<ISourceModule, List<Object>> sourceModuleToObject = new HashMap<ISourceModule, List<Object>>();
 
 		private Set<MixinElement> children = new HashSet<MixinElement>();
 
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj instanceof MixinElement) {
 				return this.key.equals(((MixinElement) obj).key);
 			}
 			return false;
 		}
 
 		public int hashCode() {
 			return this.key.hashCode();
 		}
 
 		/*
 		 * public MixinElement(String key) { this.key = key; }
 		 */
 
 		public String toString() {
 			return this.getLastKeySegment() + " final[" + this.bFinal + "]" //$NON-NLS-1$ //$NON-NLS-2$
 					+ this.children + " "; //$NON-NLS-1$
 		}
 
 		/*
 		 * public MixinElement(ElementInfo info, ISourceModule module) {
 		 * this(info.key, currentModule); addInfo(info, module); }
 		 */
 		// called with lock being held
 		void addInfo(ElementInfo info, ISourceModule module) {
 			if (info.object != null) {
 				List<Object> list = this.sourceModuleToObject.get(module);
 				if (list == null) {
 					list = new ArrayList<Object>();
 					this.sourceModuleToObject.put(module, list);
 				}
 				list.add(info.object);
 			}
 		}
 
 		// called with lock being held
 		public MixinElement(String key, ISourceModule currentModule) {
 			this.key = key;
 			addModule(currentModule);
 		}
 
 		// called with lock being held
 		void addModule(ISourceModule currentModule) {
 			if (currentModule != null) {
 				if (!this.sourceModules.contains(currentModule)) {
 					this.sourceModules.add(currentModule);
 				}
 			}
 		}
 
 		public IMixinElement[] getChildren() {
 			this.validate();
 			synchronized (MixinModel.this) {
 				return children.toArray(new IMixinElement[children.size()]);
 			}
 		}
 
 		public IMixinElement getChildren(String key) {
 			this.validate();
 			return MixinModel.this.get(this.key
 					+ IMixinRequestor.MIXIN_NAME_SEPARATOR + key);
 		}
 
 		public String getKey() {
 			return this.key;
 		}
 
 		protected String getParentKey() {
 			int pos = key.lastIndexOf(IMixinRequestor.MIXIN_NAME_SEPARATOR);
 			if (pos == -1) {
 				return null;
 			}
 			return key.substring(0, pos);
 		}
 
 		public String getLastKeySegment() {
 			int pos = key.lastIndexOf(IMixinRequestor.MIXIN_NAME_SEPARATOR);
 			if (pos == -1) {
 				return key;
 			}
 			return key.substring(pos + 1);
 		}
 
 		public IMixinElement getParent() {
 			String parentKey = this.getParentKey();
 			if (parentKey == null) {
 				return null;
 			}
 			return get(parentKey);
 
 		}
 
 		public ISourceModule[] getSourceModules() {
 			this.validate();
 			// TODO understand why we need this
 			if (!isFinal()) {
 				get(key);
 			}
 			synchronized (MixinModel.this) {
 				return this.sourceModules
 						.toArray(new ISourceModule[this.sourceModules.size()]);
 			}
 		}
 
 		public Object[] getObjects(ISourceModule module) {
 			this.validate();
 			synchronized (MixinModel.this) {
 				List<Object> list = this.sourceModuleToObject.get(module);
				if (list == null)
					return NO_OBJECTS;
 				Object[] objs = list.toArray();
 				for (Object obj : objs) {
 					notifyInitializeListener(this, module, obj);
 				}
 				return objs;
 			}
 		}
 
 		public Object[] getAllObjects() {
 			this.validate();
 			synchronized (MixinModel.this) {
 				Set<Object> objects = new HashSet<Object>();
 				for (ISourceModule module : sourceModules) {
 					for (Object obj : this.getObjects(module)) {
 						objects.add(obj);
 					}
 				}
 				return objects.toArray();
 			}
 		}
 
 		public boolean isFinal() {
 			return bFinal;
 		}
 
 		public void close() {
 			synchronized (MixinModel.this) {
 				knownKeysCache.remove(key);
 				removes = true;
 				this.bFinal = false;
 				for (int i = 0; i < sourceModules.size(); i++) {
 					ISourceModule module = sourceModules.get(i);
 					List<MixinElement> list = elementToMixinCache.get(module);
 					if (list != null) {
 						list.remove(this);
 						if (list.size() == 0) {
 							elementToMixinCache.remove(module);
 						}
 					}
 					if (elementToMixinCache.containsKey(module)) {
 						modulesToReparse.add(module);
 					}
 				}
 				this.sourceModules.clear();
 				this.sourceModuleToObject.clear();
 
 				// Lets also clean parent data
 				// Remove frob parent.
 				String parentKey = getParentKey();
 				MixinElement element = this;
 				while (parentKey != null) {
 					MixinElement parent = (MixinElement) cache.get(parentKey);
 					if (parent != null) {
 						removes = true;
 						knownKeysCache.remove(parent.key);
 						parent.children.remove(element);
 						parent.bFinal = false;
 						element = parent;
 						parentKey = parent.getParentKey();
 					} else {
 						break;
 					}
 				}
 			}
 		}
 
 		// potentially long-running operation
 		private void validate() {
 			if (!isFinal()) {
 				buildElementTree(this);
 			}
 		}
 	};
 
 	private final class MixinRequestor implements IMixinRequestor {
 		public void reportElement(ElementInfo info) {
 			// if( DLTKCore.VERBOSE_MIXIN ) {
 			// System.out.println("Append mixin:" + info.key);
 			// }
 			synchronized (MixinModel.this) {
 				knownKeysCache.put(info.key, Boolean.TRUE);
 				String[] list = info.key.split("\\" //$NON-NLS-1$
 						+ IMixinRequestor.MIXIN_NAME_SEPARATOR);
 				MixinElement element = getCreateEmpty(info.key);
 				addElementToModules(element);
 				element.addModule(currentModule);
 				element.addInfo(info, currentModule);
 				// Append as childs for all other elements. Also append modules
 				// to
 				// all selected elements.
 				if (list.length != 1) {
 					for (int i = 0; i < list.length - 1; ++i) {
 						MixinElement parent = getCreateEmpty(element
 								.getParentKey());
 						parent.children.add(element);
 						addElementToModules(parent);
 						element = parent;
 					}
 				}
 			}
 		}
 
 		// called with lock being held
 		private void addElementToModules(MixinElement element) {
 			List<MixinElement> elements = MixinModel.this.elementToMixinCache
 					.get(currentModule);
 			if (elements == null) {
 				elements = new ArrayList<MixinElement>();
 				MixinModel.this.elementToMixinCache
 						.put(currentModule, elements);
 			}
 			elements.add(element);
 		}
 	}
 
 	public synchronized void makeAllModuleElementsFinal(ISourceModule module) {
 		List<MixinElement> elements = elementToMixinCache.get(module);
 		if (elements != null) {
 			for (MixinElement mixin : elements) {
 				removes = true;
 				mixin.bFinal = true;
 			}
 		}
 	}
 
 	public synchronized void makeAllElementsFinalIfNoCacheRemoves() {
 		if (removes) {
 			return;
 		}
 		Enumeration<?> elements = cache.elements();
 		while (elements.hasMoreElements()) {
 			MixinElement e = (MixinElement) elements.nextElement();
 			e.bFinal = true;
 		}
 
 	}
 
 	public void setRemovesToZero() {
 		removes = false;
 	}
 
 	public synchronized void clearKeysCache(String key) {
 		knownKeysCache.remove(key);
 		requestCache.remove(key);
 		// MixinElement e = (MixinElement)this.cache.get(key);
 	}
 
 	public synchronized void clearKeysCache() {
 		knownKeysCache.clear();
 		requestCache.flush();
 	}
 
 	// // Mixin object initialize listeners code
 	public synchronized void addObjectInitializeListener(
 			IMixinObjectInitializeListener mixinObjectInitializeListener) {
 		this.mixinObjectInitializeListeners.add(mixinObjectInitializeListener);
 	}
 
 	public synchronized void removeObjectInitializeListener(
 			IMixinObjectInitializeListener mixinObjectInitializeListener) {
 		this.mixinObjectInitializeListeners
 				.remove(mixinObjectInitializeListener);
 	}
 
 	// called with lock being help
 	private void notifyInitializeListener(IMixinElement element,
 			ISourceModule module, Object o) {
 		Object[] listeners = mixinObjectInitializeListeners.getListeners();
 		for (int i = 0; i < listeners.length; i++) {
 			((IMixinObjectInitializeListener) (listeners[i])).initialize(
 					element, o, module);
 		}
 	}
 
 	protected synchronized void clear() {
 		cache.flush();
 		elementToMixinCache.clear();
 		knownKeysCache.clear();
 		modulesToReparse.clear();
 		requestCache.flush();
 	}
 
 	public String getNature() {
 		return toolkit.getNatureId();
 	}
 }
