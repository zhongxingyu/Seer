 /******************************************************************************* 
  * Copyright (c) 2009 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.kb;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.internal.resources.ICoreConstants;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jst.jsp.core.internal.contentmodel.TaglibController;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TLDCMDocumentManager;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TaglibTracker;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.DocumentProviderRegistry;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.eclipse.wst.common.componentcore.internal.ComponentResource;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.css.core.internal.provisional.adapters.IModelProvideAdapter;
 import org.eclipse.wst.css.core.internal.provisional.adapters.IStyleSheetAdapter;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSModel;
 import org.eclipse.wst.html.core.internal.htmlcss.LinkElementAdapter;
 import org.eclipse.wst.html.core.internal.htmlcss.URLModelProvider;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
 import org.eclipse.wst.xml.core.internal.document.NodeContainer;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.jboss.tools.common.el.core.GlobalELReferenceList;
 import org.jboss.tools.common.el.core.resolver.ELResolver;
 import org.jboss.tools.common.el.core.resolver.ELResolverFactoryManager;
 import org.jboss.tools.common.resref.core.ResourceReference;
 import org.jboss.tools.common.text.ext.util.Utils;
 import org.jboss.tools.jst.web.kb.include.IncludeContextBuilder;
 import org.jboss.tools.jst.web.kb.internal.FaceletPageContextImpl;
 import org.jboss.tools.jst.web.kb.internal.JspContextImpl;
 import org.jboss.tools.jst.web.kb.internal.ResourceBundle;
 import org.jboss.tools.jst.web.kb.internal.XmlContextImpl;
 import org.jboss.tools.jst.web.kb.internal.taglib.NameSpace;
 import org.jboss.tools.jst.web.kb.taglib.CustomTagLibManager;
 import org.jboss.tools.jst.web.kb.taglib.INameSpace;
 import org.jboss.tools.jst.web.kb.taglib.ITagLibrary;
 import org.jboss.tools.jst.web.kb.taglib.TagLibriryManager;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.css.CSSStyleSheet;
 
 /**
  * @author Alexey Kazakov
  */
 public class PageContextFactory {
 
 	public static String JSP_PAGE_CONTEXT_TYPE = "JSP_PAGE_CONTEXT_TYPE";
 	public static String FACELETS_PAGE_CONTEXT_TYPE = "FACELETS_PAGE_CONTEXT_TYPE";
 	
 	/**
 	 * Creates a page context for the specified context type
 	 * @
 	 */
 	public static IPageContext createPageContext(IFile file, int offset, String contentType) {
 		String contextType = IncludeContextBuilder.getInstance().getContextType(contentType);
 		if (JSP_PAGE_CONTEXT_TYPE.equals(contextType)) {
 			return createJSPContext(file, offset);
 		}
 		else if (FACELETS_PAGE_CONTEXT_TYPE.equals(contextType)) {
 			return createFaceletPageContext(file, offset);
 		}
 		return null;
 	}
 
 	/**
 	 * Creates a jsp context for given resource and offset.
 	 * @param file JSP
 	 * @param offset
 	 * @return
 	 */
 	private static IPageContext createJSPContext(IFile file, int offset) {
 		JspContextImpl context = new JspContextImpl();
 		
 		
 		IEditorInput input = new FileEditorInput(file);
 		try {
 			ELResolver[] elResolvers = ELResolverFactoryManager.getInstance().getResolvers(file);
 			context.setResource(file);
 			context.setDocument(getConnectedDocument(input));
 			context.setElResolvers(elResolvers);
 			
 			setXMLNameSpaces(context, offset);
 			setJSPNameSpaces(context, offset);
 			context.setLibraries(getTagLibraries(context, offset));
 			context.setResourceBundles(getResourceBundles(context));
 			
 			collectIncludedAdditionalInfo(context);
 		} finally {
 			releaseConnectedDocument(input);
 			context.setDocument(null);
 		}
 		return context;
 	}
 
 	/**
 	 * Creates a facelet context for given resource and offset.
 	 * @param file Facelet
 	 * @param offset
 	 * @return
 	 */
 	private static IFaceletPageContext createFaceletPageContext(IFile file, int offset) {
 		FaceletPageContextImpl context = new FaceletPageContextImpl();
 
 		IEditorInput input = new FileEditorInput(file);
 		try {
 			ELResolver[] elResolvers = ELResolverFactoryManager.getInstance().getResolvers(file);
 			context.setResource(file);
 			context.setDocument(getConnectedDocument(input));
 			context.setElResolvers(elResolvers);
 			
 			setXMLNameSpaces(context, offset);
 			setFaceletsNameSpaces(context, offset);
 			context.setLibraries(getTagLibraries(context, offset));
 			context.setResourceBundles(getResourceBundles(context));
 	
 			collectIncludedAdditionalInfo(context);
 		} finally {
 			releaseConnectedDocument(input);
 			context.setDocument(null);
 		}
 		return context;
 
 	}
 	
 	/* Utility functions */
 	
 	public static void collectIncludedAdditionalInfo(IPageContext context) {
 		if (!(context instanceof IIncludedContextSupport) && 
 				!(context instanceof ICSSContainerSupport))
 			return;
 		
 		IStructuredModel sModel = StructuredModelManager.getModelManager()
 				.getExistingModelForRead(context.getDocument());
 
 		try {
 			if (sModel == null)
 				return;
 
 			Document xmlDocument = (sModel instanceof IDOMModel) ? ((IDOMModel) sModel)
 					.getDocument()
 					: null;
 
 			if (xmlDocument == null)
 				return;
 
 			if (xmlDocument instanceof IDOMNode) {
 				createIncludedAdditionalInfoForNode((IDOMNode)xmlDocument, context);
 			}
 		} finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 
 	}
 
 	private static void createIncludedAdditionalInfoForNode(IDOMNode node, IPageContext context) {
 		String prefix = node.getPrefix() == null ? "" : node.getPrefix(); //$NON-NLS-1$
 		String tagName = node.getLocalName();
 		if (node instanceof IDOMElement) {
 			Map<String, List<INameSpace>> nsMap = context.getNameSpaces(node.getStartOffset());
 			String[] uris = getUrisByPrefix(nsMap, prefix);
 			
 			if (uris != null) {
 				for (String uri : uris) {
 					if (context instanceof IIncludedContextSupport) {
 						String[] includeAttributes = IncludeContextBuilder.getIncludeAttributes(uri, tagName);
 						if (includeAttributes != null) {
 							for (String attr : includeAttributes) {
 								createIncludedContextFromAttribute((IDOMElement)node, attr, context);
 							}
 						}
 					}
 					if (context instanceof ICSSContainerSupport) {
 						if(IncludeContextBuilder.isCSSStyleSheetContainer(uri, tagName)) {
 							createCSSStyleSheetFromElement((IDOMElement)node, (ICSSContainerSupport)context);
 						} else {
 							String[] cssAttributes = IncludeContextBuilder.getCSSStyleSheetAttributes(uri, tagName);
 							if (cssAttributes != null) {
 								for (String attr : cssAttributes) {
 									createCSSStyleSheetFromAttribute((IDOMElement)node, attr, (ICSSContainerSupport)context);
 								}
 							}
 						}
 					}					
 				}
 			}
 		}		
 		
 		NodeList children = node.getChildNodes();
 		for (int i = 0; children != null && i < children.getLength(); i++) {
 			if (children.item(i) instanceof IDOMElement) {
 				createIncludedAdditionalInfoForNode((IDOMElement)children.item(i), context);
 			}
 		}
 	}
 	
 	private static void createCSSStyleSheetFromAttribute(IDOMElement node,
 			String attribute, ICSSContainerSupport context) {
 		CSSStyleSheet sheet = getSheetForTagAttribute(node, attribute);
 		if (sheet != null)
 			context.addCSSStyleSheet(sheet);
 	}
 
 	private static void createCSSStyleSheetFromElement(IDOMElement node,
 			ICSSContainerSupport context) {
 		CSSStyleSheet sheet = getSheetForTag(node);
 		if (sheet != null)
 			context.addCSSStyleSheet(sheet);
 	}
 
 	
 	/**
 	 * 
 	 * @param stylesContainer
 	 * @return
 	 */
 	private static CSSStyleSheet getSheetForTagAttribute(final Node stylesContainer, String attribute) {
 
 		INodeNotifier notifier = (INodeNotifier) stylesContainer;
 
 		IStyleSheetAdapter adapter = (IStyleSheetAdapter) notifier
 				.getAdapterFor(IStyleSheetAdapter.class);
 
 		if (!(adapter instanceof ExtendedLinkElementAdapter)) {
 
 			notifier.removeAdapter(adapter);
 			adapter = new ExtendedLinkElementAdapter(
 					(Element) stylesContainer, attribute);
 			notifier.addAdapter(adapter);
 
 		}
 
 		CSSStyleSheet sheet = null;
 
 		if (adapter != null) {
 			sheet = (CSSStyleSheet) adapter.getSheet();
 
 		}
 
 		return sheet;
 	}
 	
 	/**
 	 * 
 	 * @param stylesContainer
 	 * @return
 	 */
 	private static CSSStyleSheet getSheetForTag(final Node stylesContainer) {
 
 		INodeNotifier notifier = (INodeNotifier) stylesContainer;
 
 		IStyleSheetAdapter adapter = (IStyleSheetAdapter) notifier
 				.getAdapterFor(IStyleSheetAdapter.class);
 
 		CSSStyleSheet sheet = null;
 
 		if (adapter != null) {
 			sheet = (CSSStyleSheet) adapter.getSheet();
 		}
 
 		return sheet;
 	}
 
 	private static void createIncludedContextFromAttribute(IDOMElement node, String attribute, IIncludedContextSupport context) {
 		String fileName = node.getAttribute(attribute);
 		if (fileName == null || fileName.trim().length() == 0)
 			return;
 
 		IFile file = getFileFromProject(fileName, context.getResource());
 		if (file == null)
 			return;
 		
 		// Fix for JBIDE-5083 >>>
 		if (context.contextExistsInParents(file))
 			return;
 		// Fix for JBIDE-5083 <<<
 		
 		IStructuredModel sModel = null; 
 			
 		try {
 			sModel = StructuredModelManager.getModelManager()
 				.getModelForRead(file);
 		} catch (IOException e) {
 			WebKbPlugin.getDefault().logError(e);
 		} catch (CoreException e) {
 			WebKbPlugin.getDefault().logError(e);
 		}
 
 		if (sModel == null)
 			return;
 		
 		try {
 			Document xmlDocument = (sModel instanceof IDOMModel) ? 
 					((IDOMModel) sModel).getDocument() : null;
 
 			if (xmlDocument == null)
 				return;
 
 			if (xmlDocument instanceof IDOMNode) {
 				String contentType = sModel.getContentTypeIdentifier();
 				IPageContext includedContext = PageContextFactory.createPageContext(file, ((IDOMNode) xmlDocument).getEndOffset() - 1, contentType);
 				if (includedContext != null)
 					context.addIncludedContext(includedContext);
 			}
 		} finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 	}
 	
 	static Node findNodeForOffset(IDOMNode node, int offset) {
 		if(node == null) return null;
 		if (!node.contains(offset)) return null;
 			
 		if (node.hasChildNodes()) {
 			// Try to find the node in children
 			NodeList children = node.getChildNodes();
 			for (int i = 0; children != null && i < children.getLength(); i++) {
 				IDOMNode child = (IDOMNode)children.item(i);
 				if (child.contains(offset)) {
 					return findNodeForOffset(child, offset);
 				}
 			}
 		}
 			// Not found in children or nave no children
 		if (node.hasAttributes()) {
 			// Try to find in the node attributes
 			NamedNodeMap attributes = node.getAttributes();
 			
 			for (int i = 0; attributes != null && i < attributes.getLength(); i++) {
 				IDOMNode attr = (IDOMNode)attributes.item(i);
 				if (attr.contains(offset)) {
 					return attr;
 				}
 			}
 		}
 		// Return the node itself
 		return node;
 	}
 
 	static Node findNodeForOffset(Node node, int offset) {
 		return (node instanceof IDOMNode) ? findNodeForOffset((IDOMNode)node, offset) : null;
 	}
 
 	private static IDocument getConnectedDocument(IEditorInput input) {
 		IDocumentProvider provider= DocumentProviderRegistry.getDefault().getDocumentProvider(input);
 		try {
 			provider.connect(input);
 		} catch (CoreException e) {
 			WebKbPlugin.getDefault().logError(e);
 		}
 		return provider.getDocument(input);
 	}
 	
 	private static void releaseConnectedDocument(IEditorInput input) {
 		IDocumentProvider provider= DocumentProviderRegistry.getDefault().getDocumentProvider(input);
 		provider.disconnect(input);
 	}
 	
 	
 	private static Document getDocument(IFile file) {
 		IStructuredModel sModel = null; 
 		
 		try {
 			sModel = StructuredModelManager.getModelManager()
 				.getModelForRead(file);
 		} catch (IOException e) {
 			WebKbPlugin.getDefault().logError(e);
 		} catch (CoreException e) {
 			WebKbPlugin.getDefault().logError(e);
 		}
 
 		if (sModel == null)
 			return null;
 	
 		try {
 			return (sModel instanceof IDOMModel) ? ((IDOMModel) sModel)
 					.getDocument()
 					: null;
 	
 		} finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 	}
 	
 	
 	/**
 	 * Collects the namespaces over the XML-page and sets them up to the context specified.
 	 * 
 	 * @param context
 	 */
 	private static void setXMLNameSpaces(XmlContextImpl context, int offset) {
 		IStructuredModel sModel =  null;
 		
 		try {
 			sModel = StructuredModelManager.getModelManager()
 				.getModelForRead(context.getResource());
 		} catch (IOException e) {
 			WebKbPlugin.getDefault().logError(e);
 		} catch (CoreException e) {
 			WebKbPlugin.getDefault().logError(e);
 		}
 
 		if (sModel == null)
 			return;
 
 		try {
 			Document xmlDocument = (sModel instanceof IDOMModel) ? ((IDOMModel) sModel)
 					.getDocument()
 					: null;
 
 			if (xmlDocument == null)
 				return;
 
 			Node n = findNodeForOffset(xmlDocument, offset);
 			while (n != null) {
 				if (!(n instanceof Element)) {
 					if (n instanceof Attr) {
 						n = ((Attr) n).getOwnerElement();
 					} else {
 						n = n.getParentNode();
 					}
 					continue;
 				}
 
 				NamedNodeMap attrs = n.getAttributes();
 				for (int j = 0; attrs != null && j < attrs.getLength(); j++) {
 					Attr a = (Attr) attrs.item(j);
 					String name = a.getName();
 					if (name.startsWith("xmlns:")) { //$NON-NLS-1$
 						final String prefix = name.substring("xmlns:".length()); //$NON-NLS-1$
 						final String uri = a.getValue();
 						if (prefix != null && prefix.trim().length() > 0
 								&& uri != null && uri.trim().length() > 0) {
 
 							int start = ((IndexedRegion) n).getStartOffset();
 							int length = ((IndexedRegion) n).getLength();
 
 							IDOMElement domElement = (n instanceof IDOMElement ? (IDOMElement) n
 									: null);
 							if (domElement != null) {
 								start = domElement.getStartOffset();
 								length = (domElement.hasEndTag() ? domElement
 										.getEndStructuredDocumentRegion()
 										.getEnd() : ((IDOMNode) xmlDocument).getEndOffset() - 1 - start);
 							}
 
 							Region region = new Region(start, length);
 							INameSpace nameSpace = new NameSpace(uri.trim(),
 									prefix.trim());
 							context.addNameSpace(region, nameSpace);
 						}
 					}
 				}
 
 				n = n.getParentNode();
 			}
 
 			return;
 		} finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 
 	}	
 	
 	/**
 	 * Collects the namespaces over the JSP-page and sets them up to the context specified.
 	 * 
 	 * @param context
 	 */
 	private static void setJSPNameSpaces(JspContextImpl context, int offset) {
 		TLDCMDocumentManager manager = TaglibController.getTLDCMDocumentManager(context.getDocument());
 		List trackers = (manager == null? null : manager.getCMDocumentTrackers(offset));
 		for (int i = 0; trackers != null && i < trackers.size(); i++) {
 			TaglibTracker tt = (TaglibTracker)trackers.get(i);
 			final String prefix = tt.getPrefix();
 			final String uri = tt.getURI();
 			if (prefix != null && prefix.trim().length() > 0 &&
 					uri != null && uri.trim().length() > 0) {
 					
 				IRegion region = new Region(0, context.getDocument().getLength());
 				INameSpace nameSpace = new NameSpace(uri.trim(), prefix.trim());
 				context.addNameSpace(region, nameSpace);
 			}
 		}
 
 		return;
 	}
 	
 	/**
 	 * Collects the namespaces over the Facelets-page and sets them up to the context specified.
 	 * 
 	 * @param context
 	 */
 	private static void setFaceletsNameSpaces(FaceletPageContextImpl context, int offset) {
 		IStructuredModel sModel =  null;
 		
 		try {
 			sModel = StructuredModelManager.getModelManager()
 				.getModelForRead(context.getResource());
 		} catch (IOException e) {
 			WebKbPlugin.getDefault().logError(e);
 		} catch (CoreException e) {
 			WebKbPlugin.getDefault().logError(e);
 		}
 
 		if (sModel == null)
 			return;
 
 
 		try {
 			Document xmlDocument = (sModel instanceof IDOMModel) ? 
 					((IDOMModel) sModel).getDocument() : null;
 
 			if (xmlDocument == null)
 				return;
 
 			Node n = findNodeForOffset(xmlDocument, offset);
 			while (n != null) {
 				if (!(n instanceof Element)) {
 					if (n instanceof Attr) {
 						n = ((Attr) n).getOwnerElement();
 					} else {
 						n = n.getParentNode();
 					}
 					continue;
 				}
 
 				NamedNodeMap attrs = n.getAttributes();
 				for (int j = 0; attrs != null && j < attrs.getLength(); j++) {
 					Attr a = (Attr) attrs.item(j);
 					String name = a.getName();
 					if (name.startsWith("xmlns:")) { //$NON-NLS-1$
 						final String prefix = name.substring("xmlns:".length()); //$NON-NLS-1$
 						final String uri = a.getValue();
 						if (prefix != null && prefix.trim().length() > 0 &&
 								uri != null && uri.trim().length() > 0) {
 
 							int start = ((IndexedRegion)n).getStartOffset();
 							int length = ((IndexedRegion)n).getLength();
 							
 							IDOMElement domElement = (n instanceof IDOMElement ? (IDOMElement)n : null);
 							if (domElement != null) {
 								start = domElement.getStartOffset();
 								length = (domElement.hasEndTag() ? 
 											domElement.getEndStructuredDocumentRegion().getEnd() :
 												((IDOMNode) xmlDocument).getEndOffset() - 1 - start);
 								
 							}
 
 							Region region = new Region(start, length);
 							INameSpace nameSpace = new NameSpace(uri.trim(), prefix.trim());
 							context.addNameSpace(region, nameSpace);
 							if (CustomTagLibManager.FACELETS_UI_TAG_LIB_URI.equals(uri)) {
 								nameSpace = new NameSpace(CustomTagLibManager.FACELETS_HTML_TAG_LIB_URI, ""); //$NON-NLS-1$
 								context.addNameSpace(region, nameSpace);
 							}
 						}
 					}
 				}
 
 				n = n.getParentNode();
 			}
 
 			return;
 		} finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 	}
 
 	
 	private static final ITagLibrary[] EMPTY_LIBRARIES = new ITagLibrary[0];	
 	private static final IResourceBundle[] EMPTY_RESOURCE_BUNDLES = new IResourceBundle[0];
 
 	/**
 	 * Returns the Tag Libraries for the namespaces collected in the context.
 	 * Important: The context must be created using createContext() method before using this method.
 	 * 
 	 * @param context The context object instance
 	 * @return
 	 */
 	public static ITagLibrary[] getTagLibraries(IPageContext context, int offset) {
 		Map<String, List<INameSpace>> nameSpaces =  context.getNameSpaces(offset);
 		if (nameSpaces == null || nameSpaces.isEmpty())
 			return EMPTY_LIBRARIES;
 		
 		IProject project = context.getResource() == null ? null : context.getResource().getProject();
 		if (project == null)
 			return EMPTY_LIBRARIES;
 		
 		List<ITagLibrary> tagLibraries = new ArrayList<ITagLibrary>();
 		for (List<INameSpace> nameSpace : nameSpaces.values()) {
 			for (INameSpace n : nameSpace) {
 				ITagLibrary[] libs = TagLibriryManager.getLibraries(project, n.getURI());
 				if (libs != null && libs.length > 0) {
 					for (ITagLibrary lib : libs) {
 						tagLibraries.add(lib);
 					}
 				}
 			}
 		} 
 		return (tagLibraries.isEmpty() ? EMPTY_LIBRARIES :
 				(ITagLibrary[])tagLibraries.toArray(new ITagLibrary[tagLibraries.size()]));
 	}
 	
 	/**
 	 * Returns the resource bundles  
 	 * 
 	 * @return
 	 */
 	private static IResourceBundle[] getResourceBundles(IPageContext context) {
 		List<IResourceBundle> list = new ArrayList<IResourceBundle>();
 		IStructuredModel sModel = null;
 		
 		try {
 			sModel = StructuredModelManager.getModelManager().getModelForRead(context.getResource());
 		} catch (IOException e) {
 			WebKbPlugin.getDefault().logError(e);
 		} catch (CoreException e) {
 			WebKbPlugin.getDefault().logError(e);
 		}
 		
 		if (sModel == null) 
 			return new IResourceBundle[0];
 		try {
 			Document dom = (sModel instanceof IDOMModel) ? ((IDOMModel) sModel).getDocument() : null;
 			if (dom != null) {
 				Element element = dom.getDocumentElement();
 				NodeList children = (NodeContainer)dom.getChildNodes();
 				if (element != null) {
 					for (int i = 0; children != null && i < children.getLength(); i++) {
 						IDOMNode xmlnode = (IDOMNode)children.item(i);
 						update((IDOMNode)xmlnode, context, list);
 					}
 				}
 			}
 		}
 		finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 			
 		return list.toArray(new IResourceBundle[list.size()]);
 	}
 
 	private static void update(IDOMNode element, IPageContext context, List<IResourceBundle> list) {
 		if (element !=  null) {
 			registerBundleForNode(element, context, list);
 			for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
 				if (child instanceof IDOMNode) {
 					update((IDOMNode)child, context, list);
 				}
 			}
 		}
 	}
 	private static void registerBundleForNode(IDOMNode node, IPageContext context, List<IResourceBundle> list) {
 		if (node == null) return;
 		String name = node.getNodeName();
 		if (name == null) return;
 		if (!name.endsWith("loadBundle")) return; //$NON-NLS-1$
 		if (name.indexOf(':') == -1) return;
 		String prefix = name.substring(0, name.indexOf(':'));
 
 		Map<String, List<INameSpace>> ns = context.getNameSpaces(node.getStartOffset());
 		if (!containsPrefix(ns, prefix)) return;
 
 		NamedNodeMap attributes = node.getAttributes();
 		if (attributes == null) return;
 		String basename = (attributes.getNamedItem("basename") == null ? null : attributes.getNamedItem("basename").getNodeValue()); //$NON-NLS-1$ //$NON-NLS-2$
 		String var = (attributes.getNamedItem("var") == null ? null : attributes.getNamedItem("var").getNodeValue()); //$NON-NLS-1$ //$NON-NLS-2$
 		if (basename == null || basename.length() == 0 ||
 			var == null || var.length() == 0) return;
 
 		list.add(new ResourceBundle(basename, var));
 	}
 	
 	private static boolean containsPrefix(Map<String, List<INameSpace>> ns, String prefix) {
 		for (List<INameSpace> n: ns.values()) {
 			for (INameSpace nameSpace : n) {
 				if(prefix.equals(nameSpace.getPrefix())) return true;
 			}
 		}
 		return false;
 	}
 
 	
 	/**
 	 * Searches the namespace map and returns all the URIs for the specified prefix
 	 *  
 	 * @param nsMap
 	 * @param prefix
 	 * @return
 	 */
 	public static String[] getUrisByPrefix(Map<String, List<INameSpace>> nsMap, String prefix) {
 		if(nsMap == null || nsMap.isEmpty())
 			return null;
 		Set<String> uris = new HashSet<String>();
 		for (List<INameSpace> nsList : nsMap.values()) {
 			for (INameSpace ns : nsList) {
 				if (prefix.equals(ns.getPrefix())) {
 					uris.add(ns.getURI());
 				}
 			}
 		}
 		
 		return uris.isEmpty() ? new String[] {prefix} : (String[])uris.toArray(new String[uris.size()]);
 	}
 
 	/**
 	 * Searches the file with the name specified
 	 * 
 	 * @param fileName
 	 * @param documentFile
 	 * @return
 	 */
 	public static IFile getFileFromProject(String fileName, IFile documentFile) {
 		if(documentFile == null || !documentFile.isAccessible()) return null;
 		
 		fileName = findAndReplaceElVariable(fileName);
 
 		IProject project = documentFile.getProject();
 		String name = Utils.trimFilePath(fileName);
 		IPath currentPath = documentFile.getLocation()
 				.removeLastSegments(1);
 		IResource member = null;
 		StructureEdit se = StructureEdit.getStructureEditForRead(project);
 		if (se == null) {
 			return null;
 		}
 		WorkbenchComponent[] modules = se.getWorkbenchModules();
 		for (int i = 0; i < modules.length; i++) {
 			if (name.startsWith("/")) { //$NON-NLS-1$
 				member = findFileByAbsolutePath(project, modules[i], name);
 			} else {
 				member = findFileByRelativePath(project, modules[i],
 						currentPath, name);
 				if (member == null && name.length() > 0) {
 					// in some cases path having no leading "/" is
 					// nevertheless absolute
 					member = findFileByAbsolutePath(project, modules[i],
 							"/" + name); //$NON-NLS-1$
 				}
 			}
 			if (member != null && (member instanceof IFile)) {
 				if (((IFile) member).exists())
 					return (IFile) member;
 			}
 		}
 		return null;
 	}
 	
 	private static IFile findFileByRelativePath(IProject project,
 			WorkbenchComponent module, IPath basePath, String path) {
 		
 		if (path == null || path.trim().length() == 0)
 			return null;
 		
 		path = findAndReplaceElVariable(path);
 		
 		ComponentResource[] resources = module.findResourcesBySourcePath(
 				new Path("/"), 0); //$NON-NLS-1$
 		IPath projectPath = project.getLocation();
 		IFile member = null;
 
 		for (int i = 0; resources != null && i < resources.length; i++) {
 			IPath runtimePath = resources[i].getRuntimePath();
 			IPath sourcePath = resources[i].getSourcePath();
 
 			// Look in source environment
 			IPath webRootPath = projectPath.append(sourcePath);
 			IPath relativePath = Utils.getRelativePath(webRootPath,
 					basePath);
 			IPath filePath = relativePath.append(path);
 			member = project.getFolder(sourcePath).getFile(filePath);
 			if (member.exists()) {
 				return member;
 			}
 
 			// Look in runtime environment
 			if (runtimePath.segmentCount() >= ICoreConstants.MINIMUM_FOLDER_SEGMENT_LENGTH - 1) {
 				webRootPath = projectPath.append(runtimePath);
 				relativePath = Utils.getRelativePath(webRootPath, basePath);
 				filePath = relativePath.append(path);
 				member = project.getFolder(runtimePath).getFile(filePath);
 				if (member.exists()) {
 					return member;
 				}
 			}
 		}
 		return null;
 	}
 
 	private static IFile findFileByAbsolutePath(IProject project,
 		WorkbenchComponent module, String path) {
 		ComponentResource[] resources = module.findResourcesBySourcePath(
 				new Path("/"), 0); //$NON-NLS-1$
 		
 		path = findAndReplaceElVariable(path);
 
 		IFile member = null;
 
 		for (int i = 0; resources != null && i < resources.length; i++) {
 			IPath runtimePath = resources[i].getRuntimePath();
 			IPath sourcePath = resources[i].getSourcePath();
 
 			// Look in source environment
 			member = project.getFolder(sourcePath).getFile(path);
 			if(member.exists()) {
 					return member;
 			} 
 
 			// Look in runtime environment
 			if (runtimePath.segmentCount() >= ICoreConstants.MINIMUM_FOLDER_SEGMENT_LENGTH - 1) {
 				member = project.getFolder(runtimePath).getFile(path);
 					if (member.exists()) {
 						return member;
 				}
 			}
 		}
 		return null;
 	}
 	
 	private static final String DOLLAR_PREFIX = "${"; //$NON-NLS-1$
 
     private static final String SUFFIX = "}"; //$NON-NLS-1$
 
     private static final String SHARP_PREFIX = "#{"; //$NON-NLS-1$
     
 	// partly copied from org.jboss.tools.vpe.editor.util.ElService
 	public static String findAndReplaceElVariable(String fileName){
 		final IPath workspacePath = Platform.getLocation();
 
         final ResourceReference[] gResources = GlobalELReferenceList.getInstance().getAllResources(workspacePath);
 		String result = fileName;
 
 		ResourceReference[] sortedReferences = sortReferencesByScope(gResources);
 
 		for (ResourceReference rf : sortedReferences) {
 			final String dollarEl = DOLLAR_PREFIX + rf.getLocation() + SUFFIX;
 			final String sharpEl = SHARP_PREFIX + rf.getLocation() + SUFFIX;
 
 			if (fileName.contains(dollarEl)) {
 				result = result.replace(dollarEl, rf.getProperties());
 			}
 			if (fileName.contains(sharpEl)) {
 				result = result.replace(sharpEl, rf.getProperties());
 			}
 		}
 		return result;
 	}
 	
 	// copied from org.jboss.tools.vpe.editor.util.ElService
 	private static ResourceReference[] sortReferencesByScope(ResourceReference[] references) {
 		ResourceReference[] sortedReferences = references.clone();
 
         Arrays.sort(sortedReferences, new Comparator<ResourceReference>() {
 			public int compare(ResourceReference r1, ResourceReference r2) {
 				return r1.getScope() - r2.getScope();
 			}
         });
 
 		return sortedReferences;
 	}
 	
 	public static class ExtendedLinkElementAdapter extends LinkElementAdapter {
 
 		private Element element;
 		private String hrefAttrName;
 
 		public ExtendedLinkElementAdapter(Element element, String hrefAttrName) {
 			this.element = element;
 			this.hrefAttrName = hrefAttrName;
 		}
 
 		@Override
 		public Element getElement() {
 			return element;
 		}
 
 		@Override
 		protected boolean isValidAttribute() {
 			String href = getElement().getAttribute(hrefAttrName);
 			if (href == null || href.length() == 0)
 				return false;
 			return true;
 		}
 
 		/**
 		 */
 		public ICSSModel getModel() {
			// Fix for JBIDE-5079 >>>
			ICSSModel model = null;
			if (super.isValidAttribute()) {
				model = super.getModel();
			}
			// Fix for JBIDE-5079 <<<
 			if (model == null) {
 				model = retrieveModel();
 				setModel(model);
 			}
 			return model;
 		}
 
 		/**
 		 */
 		private ICSSModel retrieveModel() {
 			if (!isValidAttribute()) {
 				return null;
 			}
 
 			// null,attr check is done in isValidAttribute()
 			Element element = getElement();
 			String href = findAndReplaceElVariable(element
 					.getAttribute(hrefAttrName));
 
 			IDOMModel baseModel = ((IDOMNode) element).getModel();
 			if (baseModel == null)
 				return null;
 			Object id = baseModel.getId();
 			if (!(id instanceof String))
 				return null;
 			// String base = (String)id;
 
 			// get ModelProvideAdapter
 			IModelProvideAdapter adapter = (IModelProvideAdapter) ((INodeNotifier) getElement())
 					.getAdapterFor(IModelProvideAdapter.class);
 
 			URLModelProvider provider = new URLModelProvider();
 			try {
 				IStructuredModel newModel = provider.getModelForRead(baseModel,
 						href);
 				if (newModel == null)
 					return null;
 				if (!(newModel instanceof ICSSModel)) {
 					newModel.releaseFromRead();
 					return null;
 				}
 
 				// notify adapter
 				if (adapter != null)
 					adapter.modelProvided(newModel);
 
 				return (ICSSModel) newModel;
 			} catch (UnsupportedEncodingException e) {
 				WebKbPlugin.getDefault().logError(e);
 			} catch (IOException e) {
 				WebKbPlugin.getDefault().logError(e);
 			}
 
 			return null;
 		}
 	}
 }
