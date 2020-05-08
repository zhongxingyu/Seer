 package org.jboss.tools.jst.web.kb.internal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.jboss.tools.common.el.core.resolver.ELContextImpl;
 import org.jboss.tools.jst.web.kb.IIncludedContextSupport;
 import org.jboss.tools.jst.web.kb.IPageContext;
 import org.jboss.tools.jst.web.kb.IResourceBundle;
 import org.jboss.tools.jst.web.kb.taglib.INameSpace;
 import org.jboss.tools.jst.web.kb.taglib.ITagLibrary;
 
 public class XmlContextImpl extends ELContextImpl implements IPageContext {
 	protected IDocument document;
 	protected ITagLibrary[] libs;
	
	// Fix for JBIDE-5097: It must be a map of <IRegion to Map of <NS-Prefix to NS>> 
 	protected Map<IRegion, Map<String, INameSpace>> nameSpaces = new HashMap<IRegion, Map<String, INameSpace>>();
 	protected IResourceBundle[] bundles;
 	private IIncludedContextSupport parentContext = null;
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.IPageContext#getLibraries()
 	 */
 	public ITagLibrary[] getLibraries() {
 		return libs;
 	}
 
 	public void setLibraries(ITagLibrary[] libs) {
 		this.libs = libs;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.IPageContext#getResourceBundles()
 	 */
 	public IResourceBundle[] getResourceBundles() {
 		List<IResourceBundle> resourceBundles = new ArrayList<IResourceBundle>();
 		if (bundles != null) {
 			for (IResourceBundle bundle : bundles) {
 				resourceBundles.add(bundle);
 			}
 		}
 		
 		List<IPageContext> includedContexts = getIncludedContexts();
 		if (includedContexts != null) {
 			for (IPageContext includedContext : includedContexts) {
 				IResourceBundle[] includedBundles = includedContext.getResourceBundles();
 				if (includedBundles != null) {
 					for (IResourceBundle includedBundle : includedBundles) {
 						resourceBundles.add(includedBundle);
 					}
 				}
 			}
 		}
 		
 		return (IResourceBundle[])resourceBundles.toArray(new IResourceBundle[resourceBundles.size()]);
 	}
 
 	/**
 	 * Sets resource bundles
 	 * @param bundles
 	 */
 	public void setResourceBundles(IResourceBundle[] bundles) {
 		this.bundles = bundles;
 	}
 
 	/**
 	 * @param document the document to set
 	 */
 	public void setDocument(IDocument document) {
 		this.document = document;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.PageContext#getDocument()
 	 */
 	public IDocument getDocument() {
 		return document;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.IPageContext#getNameSpaces(int)
 	 */
 	public Map<String, List<INameSpace>> getNameSpaces(int offset) {
 		Map<String, List<INameSpace>> result = new HashMap<String, List<INameSpace>>();
 		Map<INameSpace, IRegion> namespaceToRegions = new HashMap<INameSpace, IRegion>();
 
 		for (IRegion region : nameSpaces.keySet()) {
 			if(offset>=region.getOffset() && offset<=region.getOffset() + region.getLength()) {
 				Map<String, INameSpace> namespaces = nameSpaces.get(region);
 				if (namespaces != null) {
 					for (INameSpace ns : namespaces.values()) {
 						INameSpace existingNameSpace = findNameSpaceByPrefix(namespaceToRegions.keySet(), ns.getPrefix());
 						IRegion existingRegion = namespaceToRegions.get(existingNameSpace); 
 						if (existingRegion != null) {
 							// Perform visibility check for region
 							if (region.getOffset() > existingRegion.getOffset()) {
 								// Replace existingNS by this ns
 								namespaceToRegions.remove(existingNameSpace);
 								namespaceToRegions.put(ns, region);
 							}
 						} else {
 							namespaceToRegions.put(ns, region);
 						}
 					}
 				}
 			}
 		}
 
 		for (INameSpace ns : namespaceToRegions.keySet()) {
 			List<INameSpace> list = result.get(ns.getURI());
 			if(list==null) {
 				list = new ArrayList<INameSpace>();
 			}
 			list.add(ns);
 			result.put(ns.getURI(), list);
 		}
 
 		return result;
 	}
 
 	public INameSpace findNameSpaceByPrefix(Set<INameSpace> namespaces, String prefix) {
 		if (namespaces != null && prefix != null) {
 			for (INameSpace ns : namespaces) {
 				if (prefix.equals(ns.getPrefix())) {
 					return ns;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Adds new name space to the context
 	 * @param region
 	 * @param name space
 	 */
 	public void addNameSpace(IRegion region, INameSpace nameSpace) {
 		if (nameSpaces.get(region) == null) {
 			Map<String, INameSpace> nameSpaceMap = new HashMap<String, INameSpace>();
 			nameSpaces.put(region, nameSpaceMap);
 		}
		nameSpaces.get(region).put(nameSpace.getPrefix(), nameSpace); 	// Fix for JBIDE-5097
 	}
 
 	public void addIncludedContext(IPageContext includedContext) {
 		throw new UnsupportedOperationException();
 		
 	}
 
 	public List<IPageContext> getIncludedContexts() {
 		return null;
 	}
 	
 	/**
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.IIncludedContextSupport#contextExistsInParents(org.eclipse.core.resources.IFile)
 	 */
 	public boolean contextExistsInParents(IFile resource) {
 		// Assuming that the resource must not be null here
 		if (resource.equals(getResource()))
 			return true;
 		
 		return getParent() == null ? false : getParent().contextExistsInParents(resource);
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.IIncludedContextSupport#getParent()
 	 */
 	public IIncludedContextSupport getParent() {
 		return parentContext;
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.IIncludedContextSupport#setParent(org.jboss.tools.jst.web.kb.IIncludedContextSupport)
 	 */
 	public void setParent(IIncludedContextSupport parent) {
 		parentContext = parent;
 	}
 }
