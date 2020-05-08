 package org.eclipse.dltk.core.mixin;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.ElementChangedEvent;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IElementChangedListener;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementDelta;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceModuleInfoCache;
 import org.eclipse.dltk.core.ISourceModuleInfoCache.ISourceModuleInfo;
 import org.eclipse.dltk.core.mixin.IMixinRequestor.ElementInfo;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.core.search.indexing.IIndexConstants;
 import org.eclipse.dltk.internal.core.ModelCache;
 import org.eclipse.dltk.internal.core.ModelManager;
 import org.eclipse.dltk.internal.core.mixin.IInternalMixinElement;
 import org.eclipse.dltk.internal.core.mixin.MixinCache;
 import org.eclipse.dltk.internal.core.mixin.MixinManager;
 
 public class MixinModel {
 	public static final String SEPARATOR = "" + IIndexConstants.SEPARATOR;
 	private MixinCache cache = null;
 	/**
 	 * Contains list of mixin elements.
 	 */
 	private Map elementToMixinCache = new HashMap();
 	private IDLTKLanguageToolkit toolkit = null;
 	
 	private MixinRequestor mixinRequestor = new MixinRequestor();
 	
 	private ISourceModule currentModule;
 	private Set modulesToReparse = new HashSet(); // list of modules required to be reparsed.
 	public long removes = 1;
 	double ratio = 2000;
 	public MixinModel(IDLTKLanguageToolkit toolkit) {
 		this.toolkit = toolkit;
 
 		// long maxMemory = Runtime.getRuntime().freeMemory();
 
 		this.cache = new MixinCache(
 				(int) (ModelCache.DEFAULT_ROOT_SIZE * ratio));
 		DLTKCore.addElementChangedListener(changedListener);
 	}
 
 	public void stop() {
 		DLTKCore.removeElementChangedListener(changedListener);
 	}
 	
 	private static void waitForAutoBuild() {
 		boolean wasInterrupted = false;
 		do {
 			try {
 				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
 				wasInterrupted = false;
 			} catch (OperationCanceledException e) {
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				wasInterrupted = true;
 			}
 		} while (wasInterrupted);
 	}
 
 	public IMixinElement get(String key) {
 //		waitForAutoBuild();
 		if( notExistKeysCache.contains(key)) {
 			return null;
 		}
 		if( removes == 0 ) {
 			if( cache.get(key) == null) {
 				return null;
 			}
 		}
 		MixinElement element = getCreateEmpty(key);
 		if (DLTKCore.VERBOSE) {
 			System.out.println("Filling ratio:" + this.cache.fillingRatio());
 			this.cache.printStats();
 		}
 		buildElementTree(element);
 		if( element.isFinal() && element.sourceModules.size() > 0 ) {
 			existKeysCache.add(key);
 			return element;
 		}
 		notExistKeysCache.add(key);
 		synchronized ( this.cache ) {
 			this.cache.remove(element);
 			cache.resetSpaceLimit(ModelCache.DEFAULT_ROOT_SIZE, element);
 			this.cache.removeKey(element);
 		}
 		return null;
 	}
 	public String[] findKeys(String pattern) {
 		return SearchEngine.searchMixinPatterns(pattern, toolkit);
 	}
 	private Set existKeysCache = new HashSet();
 	private Set notExistKeysCache = new HashSet();
 	public boolean keyExists(String key) {
		
 		//TODO: For this version we cache all information, so should be 0.
 		if( removes == 0 ) {
 			return this.cache.get(key) != null;
 		}
 		MixinElement e = (MixinElement)this.cache.get(key);
 		if( e != null && e.sourceModules.size() > 0 ) {
 			return true;
 		}
 		if( existKeysCache.contains(key)) {
 			return true;
 		}
 		if( notExistKeysCache.contains(key)) {
 			return false;
 		}
 		boolean exist = get(key) != null;
 		if( exist ) {
 			if( existKeysCache.size() > 500000) {
 				existKeysCache.clear();
 			}
 			existKeysCache.add(key);
 		}
 		else {
 			if( notExistKeysCache.size() > 500000) {
 				notExistKeysCache.clear();
 			}
 			notExistKeysCache.add(key);
 		}
 		
 		return exist;
 	}
 
 	private void buildElementTree(MixinElement element) {
 		//TODO: This is consistend cache stage
 		if (element.isFinal()) {
 			return;
 		}
 		ISourceModule[] containedModules = findModules(element);
 		if( containedModules.length == 0 ) {
 			synchronized (cache) {
 				cache.remove(element);
 				cache.resetSpaceLimit(ModelCache.DEFAULT_ROOT_SIZE, element);
 			}
 			return;
 		}
 		for (int i = 0; i < containedModules.length; ++i) {
 			reportModule(containedModules[i]);
 		}
 		// mark selected element and all subelements as finished.
 		markElementAsFinal(element);
 	}
 
 	private void markElementAsFinal(MixinElement element) {
 		element.bFinal = true;
 		for( Iterator i = element.children.iterator();i.hasNext();  ) {
 			markElementAsFinal((MixinElement)i.next());
 		}
 	}
 
 	public synchronized void reportModule(ISourceModule sourceModule) {
 //		if (DLTKCore.VERBOSE) {
 			System.out.println("Filling ratio:" + this.cache.fillingRatio());
 //			this.cache.printStats();
 //		}
 		if (!this.elementToMixinCache.containsKey(sourceModule)) {
 			this.elementToMixinCache.put(sourceModule, new ArrayList());
 		}
 		else { // Module already in model. So we do not to rebuild it.
 			if( !this.modulesToReparse.contains(sourceModule)) {
 				return;
 			}
 			this.modulesToReparse.remove(sourceModule);
 			// We need to reparse module if some elements are moved from it.
 		}
 		IMixinParser mixinParser;
 		try {
 			mixinParser = MixinManager.getMixinParser(sourceModule);
 			if (mixinParser != null) {
 				this.currentModule = sourceModule;
 				char[] content = sourceModule.getSourceAsCharArray();
 				mixinParser.setRequirestor(mixinRequestor);
 //				System.out.println("Mixins: reporting " + sourceModule.getPath());
 				ISourceModuleInfoCache sourceModuleInfoCache = ModelManager.getModelManager().getSourceModuleInfoCache();
 //				sourceModuleInfoCache.remove(sourceModule);
 				ISourceModuleInfo mifo = sourceModuleInfoCache.get(sourceModule);
 				
 				mixinParser.parserSourceModule(content, true, sourceModule, mifo );
 				
 				if( mifo.isEmpty() ) {
 					sourceModuleInfoCache.remove(sourceModule);
 				}
 				this.currentModule = null;
 			}
 		} catch (CoreException e) {
 			e.printStackTrace();
 			return;
 		}
 	}
 
 	/**
 	 * Should find all elements source modules to be shure we build complete child tree.
 	 * @param element
 	 * @return
 	 */
 	private ISourceModule[] findModules( MixinElement element ) {
 		long start = System.currentTimeMillis();
 		ISourceModule[] searchMixinSources = SearchEngine.searchMixinSources(element.getKey(), toolkit);
 		long end = System.currentTimeMillis();
 		System.out.println("findModules:" + Long.toString(end - start ));
 		return searchMixinSources;
 	}
 
 	private synchronized MixinElement getCreateEmpty(String key) {
 		MixinElement element = (MixinElement)MixinModel.this.cache.get(key);
 		if( element == null ) {
 			element = new MixinElement(key, currentModule );
 			this.cache.put(key, element);
 			this.cache.ensureSpaceLimit(1, element);
 		}
 		return element;
 	}
 
 	private IElementChangedListener changedListener = new IElementChangedListener() {
 		public void elementChanged(ElementChangedEvent event) {
 			IModelElementDelta delta = event.getDelta();
 			processDelta(delta);
 		}
 
 		private void processDelta(IModelElementDelta delta) {
 			IModelElement element = delta.getElement();
 			if (delta.getKind() == IModelElementDelta.REMOVED || delta.getKind() == IModelElementDelta.CHANGED
 					|| (delta.getFlags() & IModelElementDelta.F_REMOVED_FROM_BUILDPATH) != 0
 					|| (delta.getFlags() & IModelElementDelta.CHANGED) != 0) {
 				if (element.getElementType() != IModelElement.SOURCE_MODULE && element.getElementType() != IModelElement.PROJECT_FRAGMENT && element.getElementType() != IModelElement.SCRIPT_FOLDER && element.getElementType() != IModelElement.SCRIPT_MODEL && element.getElementType() != IModelElement.SCRIPT_PROJECT) {
 					ISourceModule module = (ISourceModule)element.getAncestor(IModelElement.SOURCE_MODULE);
 					MixinModel.this.remove(module);
 				}
 				if(element.getElementType() == IModelElement.SOURCE_MODULE ) {
 					MixinModel.this.remove((ISourceModule) element);
 				}
 			}
 			if (delta.getKind() == IModelElementDelta.ADDED ) {
 				if (element.getElementType() == IModelElement.SOURCE_MODULE) {
 //					clearAllElementsState();
 					if( !modulesToReparse.contains(element)) {
 						modulesToReparse.add(element);
 					}
 				}
 			}
 			
 			if ((delta.getFlags() & IModelElementDelta.F_CHILDREN) != 0) {
 				IModelElementDelta[] affectedChildren = delta
 						.getAffectedChildren();
 				for (int i = 0; i < affectedChildren.length; i++) {
 					IModelElementDelta child = affectedChildren[i];
 					processDelta(child);
 				}
 			}
 		}
 	};
 	
 //	private synchronized void clearAllElementsState() {
 //		Enumeration elements = cache.elements();
 //		while( elements.hasMoreElements() ) {
 //			MixinElement o = (MixinElement)elements.nextElement();
 //			o.bFinal = false;
 //		}
 //	}
 
 	public synchronized void remove(ISourceModule element) {
 		if( this.elementToMixinCache.containsKey(element)) {
 			List elements = (List)this.elementToMixinCache.get(element);
 			for( int i = 0; i < elements.size(); ++i ) {
 				removes++;
 				MixinElement mixin = (MixinElement)elements.get(i);
 				existKeysCache.remove(mixin.key);
 				notExistKeysCache.remove(mixin.key);
 				mixin.bFinal = false;
 				mixin.sourceModules.remove(element);
 				mixin.sourceModuleToObject.remove(element);
 				if( mixin.sourceModules.size() == 0 ) {
 					// Remove frob parent.
 					String parentKey = mixin.getParentKey();
 					if( parentKey != null ) {
 						MixinElement parent = (MixinElement)this.cache.get(parentKey);
 						if( parent != null ) {
 							parent.children.remove(mixin);
 							parent.bFinal = false;
 						}
 					}
 					// Remove from cache
 					cache.remove(mixin);
 					cache.resetSpaceLimit(ModelCache.DEFAULT_ROOT_SIZE, mixin);
 				}
 			}
 			this.elementToMixinCache.remove(element);
 		}
 	}
 
 	private class MixinElement implements IMixinElement, IInternalMixinElement {
 		private String key;
 		private boolean bFinal = false;
 		private List sourceModules = new ArrayList();
 		private Map sourceModuleToObject = new HashMap();
 
 		/**
 		 * List of Strings.
 		 */
 		private Set children = new HashSet();
 
 		public boolean equals(Object obj) {
 			if( obj instanceof MixinElement ) {
 				return this.key.equals(((MixinElement)obj).key);
 			}
 			return false;
 		}
 
 		public int hashCode() {
 			return this.key.hashCode();
 		}
 
 		public MixinElement(String key) {
 			this.key = key;
 		}
 		public String toString() {
 			return this.getLastKeySegment() + " final[" + this.bFinal + "]" + this.children + " ";
 		}
 
 		public MixinElement(ElementInfo info, ISourceModule module) {
 			this(info.key, currentModule);
 			addInfo(info, module);
 		}
 
 		void addInfo(ElementInfo info, ISourceModule module) {
 			if( info.object != null ) {
 				Object object = this.sourceModuleToObject.get(module);
 				if( object != null ) {
 					if( object instanceof List ) {
 						((List)object).add(info.object);
 					}
 					else {
 						List l = new ArrayList();
 						l.add(object);
 						l.add(info.object);
 						object = l;
 						this.sourceModuleToObject.put(module, object);
 					}
 				}
 				else {
 					List l = new ArrayList();
 					l.add(info.object);
 					object = l;
 					this.sourceModuleToObject.put(module, object);
 				}
 			}
 		}
 
 		public MixinElement(String key, ISourceModule currentModule) {
 			this.key = key;
 			addModule(currentModule);
 		}
 
 		void addModule(ISourceModule currentModule) {
 			if( currentModule != null ) {
 				if( !this.sourceModules.contains(currentModule)) {
 					this.sourceModules.add(currentModule);
 				}
 			}
 		}
 
 		public IMixinElement[] getChildren() {
 			this.validate();
 			return (IMixinElement[]) this.children.toArray(new IMixinElement[this.children
 					.size()]);
 		}
 		public IMixinElement getChildren(String key) {
 			this.validate();
 			return MixinModel.this.get(this.key + IMixinRequestor.MIXIN_NAME_SEPARATOR + key);
 		}
 
 		public String getKey() {
 			return this.key;
 		}
 
 		protected String getParentKey() {
 			if (this.key.indexOf(IMixinRequestor.MIXIN_NAME_SEPARATOR) == -1) {
 				return null;
 			}
 			return this.key.substring(0, this.key
 					.lastIndexOf(IMixinRequestor.MIXIN_NAME_SEPARATOR));
 		}
 		public String getLastKeySegment() {
 			if (this.key.indexOf(IMixinRequestor.MIXIN_NAME_SEPARATOR) == -1) {
 				return this.key;
 			}
 			return this.key.substring(1+this.key
 					.lastIndexOf(IMixinRequestor.MIXIN_NAME_SEPARATOR));
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
 			if (!this.isFinal()) {
 				get(this.key);
 			}
 			return (ISourceModule[]) this.sourceModules
 					.toArray(new ISourceModule[this.sourceModules.size()]);
 		}
 
 		public Object[] getObjects(ISourceModule module) {
 			this.validate();
 			Object o = this.sourceModuleToObject.get(module);
 			if ( o instanceof List ) {
 				return ((List)o).toArray();
 			}
 			return new Object[] { o };
 		}
 		public Object[] getAllObjects() {
 			this.validate();
 			List objects = new ArrayList();
 			for( int i = 0; i < this.sourceModules.size(); ++i ) {
 				Object[] objs = this.getObjects((ISourceModule)this.sourceModules.get(i));
 				for( int j = 0; j < objs.length; ++j ) {
 					if( !objects.contains(objs[j])) {
 						objects.add(objs[j]);
 					}
 				}
 			}
 			return objects.toArray();
 		}
 
 		public boolean isFinal() {
 			return this.bFinal;
 		}
 
 		public void close() {
 			existKeysCache.remove(key);
 			notExistKeysCache.remove(key);
 			removes++;
 			this.bFinal = false;
 			for (int i = 0; i < sourceModules.size(); i++) {
 				Object module = sourceModules.get(i);
 				List list = (List)elementToMixinCache.get(module);
 				if( list != null ) {
 					list.remove( this );
 					if( list.size() == 0 ) {
 						elementToMixinCache.remove(module);
 					}
 				}
 				if( !modulesToReparse.contains(module) && elementToMixinCache.containsKey(module)) {
 					modulesToReparse.add(module);
 				}
 			}
 			this.sourceModules.clear();
 			this.sourceModuleToObject.clear();
 			
 			// Lets also clean parent data
 			// Remove frob parent.
 			String parentKey = getParentKey();
 			MixinElement element = this;
 			while( parentKey != null ) {
 				MixinElement parent = (MixinElement)cache.get(parentKey);
 				if( parent != null ) {
 					existKeysCache.remove(parent.key);
 					notExistKeysCache.remove(parent.key);
 					parent.children.remove(element);
 					parent.bFinal = false;
 					element = parent;
 					parentKey = parent.getParentKey();
 				}
 				else {
 					break;
 				}
 			}
 			
 		}
 		private void validate() {
 			if( !isFinal() ) {
 				buildElementTree(this);
 			}
 		}
 	};
 	private class MixinRequestor implements IMixinRequestor {
 		public void reportElement(ElementInfo info) {
 //			if( DLTKCore.VERBOSE_MIXIN ) {
 //				System.out.println("Append mixin:" + info.key);
 //			}
 			String[] list = info.key.split( "\\" + IMixinRequestor.MIXIN_NAME_SEPARATOR);
 			MixinElement element = getCreateEmpty(info.key);
 			addElementToModules(element);
 			element.addModule(currentModule);
 			element.addInfo(info, currentModule);
 			// Append as childs for all other elements. Also append modules to all selected elements.
 			if( list.length != 1 ) {
 				for( int i = 0 ; i < list.length -1; ++i ) {
 					MixinElement parent = getCreateEmpty(element.getParentKey());
 					if( !parent.children.contains(element)) {
 						parent.children.add(element);
 					}
 					addElementToModules(parent);
 					element = parent;
 				}
 			}
 		}
 
 		private void addElementToModules(MixinElement element) {
 			List elements = (List)MixinModel.this.elementToMixinCache.get(currentModule);
 			if( elements == null ) {
 				elements = new ArrayList();
 				MixinModel.this.elementToMixinCache.put(currentModule, elements ) ;
 			}
 //			if( !elements.contains(element)) {
 			elements.add(element);
 //			}
 		}
 	}
 	public void makeAllElementsFinalIfNoCacheRemoves() {
 		if( removes != 0 ) {
 			return;
 		}
 		Enumeration elements = cache.elements();
 		while( elements.hasMoreElements() ) {
 			MixinElement e = (MixinElement)elements.nextElement();
 			e.bFinal = true;
 		}
 		
 	}
 
 	public void setRemovesToZero() {
 		removes = 0;		
 	}
 
 	public void clearKeysCashe(String key) {
 		existKeysCache.remove(key);
 		notExistKeysCache.remove(key);
 	};
 }
