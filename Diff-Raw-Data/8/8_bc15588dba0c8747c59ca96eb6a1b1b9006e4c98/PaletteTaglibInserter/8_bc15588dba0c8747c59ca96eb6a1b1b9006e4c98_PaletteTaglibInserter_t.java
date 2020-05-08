 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.jsp.jspeditor.dnd;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
 import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
 import org.eclipse.wst.sse.core.internal.provisional.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
 import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
 import org.eclipse.wst.xml.core.internal.document.DocumentImpl;
 import org.eclipse.wst.xml.core.internal.document.ElementImpl;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.jboss.tools.common.model.ui.ModelUIPlugin;
 import org.jboss.tools.common.model.ui.views.palette.PaletteInsertHelper;
 import org.jboss.tools.jst.jsp.util.XmlUtil;
 import org.jboss.tools.jst.web.tld.TaglibData;
 import org.jboss.tools.jst.web.tld.VpeTaglibManager;
 import org.jboss.tools.jst.web.tld.VpeTaglibManagerProvider;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class PaletteTaglibInserter {
 
 	private static final String JSP_SOURCE_ROOT_ELEMENT = "jsp:root"; //$NON-NLS-1$
 	public static final String JSP_URI = "http://java.sun.com/JSP/Page"; //$NON-NLS-1$
 	public static final String faceletUri = "http://java.sun.com/jsf/facelets"; //$NON-NLS-1$
 
 	private static final String TAGLIB_START = "<%@ taglib";  //$NON-NLS-1$
 
 	public Properties inserTaglib(IDocument d, Properties p) {
 		if(!inserTaglibInXml(d, p)) {
 			inserTaglibInOldJsp(d, p);
 		}
 		return p;
 	}
 
 	private boolean checkProperties(Properties p) {
 		return "true".equalsIgnoreCase(p.getProperty(JSPPaletteInsertHelper.PROPOPERTY_ADD_TAGLIB)) && //$NON-NLS-1$
 				p.getProperty(JSPPaletteInsertHelper.PROPOPERTY_TAGLIBRARY_URI) != null &&
 				p.getProperty(JSPPaletteInsertHelper.PROPOPERTY_TAGLIBRARY_URI).length() > 0 &&
 				!p.getProperty(JSPPaletteInsertHelper.PROPOPERTY_TAGLIBRARY_URI).equals(JSP_URI) &&
 				p.getProperty(JSPPaletteInsertHelper.PROPOPERTY_DEFAULT_PREFIX) != null &&
 				p.getProperty(JSPPaletteInsertHelper.PROPOPERTY_DEFAULT_PREFIX).length() > 0 &&
 				p.getProperty(PaletteInsertHelper.PROPOPERTY_START_TEXT) != null;
 	}
 
 	public boolean inserTaglibInOldJsp(IDocument d, Properties p) {
 		if(!checkProperties(p)) {
 			return false;
 		}
 
 		IStructuredModel model = null;
 
 		try {
 			model = StructuredModelManager.getModelManager().getModelForRead((IStructuredDocument)d);
 			IDOMDocument xmlDocument = (model instanceof IDOMModel) ? ((IDOMModel) model).getDocument() : null;
 			if (xmlDocument == null) {
 				return false;
 			}
 			Properties tl = getPrefixes(d, p);
 			if(tl == null) tl = JSPPaletteInsertHelper.getPrefixes(d.get());
 			Element root = xmlDocument.getDocumentElement();
 
 			String uri_p = p.getProperty(JSPPaletteInsertHelper.PROPOPERTY_TAGLIBRARY_URI);
 			String defaultPrefix_p = p.getProperty(JSPPaletteInsertHelper.PROPOPERTY_DEFAULT_PREFIX);
 			String lineDelimiter = PaletteInsertHelper.getLineDelimiter(d);
 			StringBuffer tg = new StringBuffer(TAGLIB_START).append(" uri=\"").append(uri_p).append("\"").append(" prefix=\"").append(defaultPrefix_p).append("\"%>").append(lineDelimiter); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 
 			if (tl != null && !tl.isEmpty()) {
 				//If taglib already exist check the prefix if changed
 				if (tl.containsKey(uri_p)) {
 					if (!tl.get(uri_p).equals(defaultPrefix_p)) {
 						p.setProperty(JSPPaletteInsertHelper.PROPOPERTY_DEFAULT_PREFIX, (String)tl.get(uri_p));
 					}
 				} else if(!tl.containsValue(defaultPrefix_p)) {
 					if (checkplace(xmlDocument, d, "jsp:directive.taglib", tg, p) == false) { //$NON-NLS-1$
 						d.replace(0, 0, tg.toString());
 						mouveFocusOnPage(p, tg.toString().length(), 0);
 						return true;
 					}
 				}
 			} else if(xmlDocument instanceof DocumentImpl) {
 				DocumentImpl docImpl = (DocumentImpl)xmlDocument;
 				// Only for JSP
 				if(docImpl.isJSPType()) {
 					if (checkplace(xmlDocument, d, "jsp:directive.page", tg, p) == false) { //$NON-NLS-1$
 						d.replace(0, 0, tg.toString());
 						mouveFocusOnPage(p, tg.toString().length(), 0);
 						return true;
 					}
 				}
 			}
 		} catch (BadLocationException e) {
 			ModelUIPlugin.getPluginLog().logError(e);
 		} finally {
 			if (model != null)	model.releaseFromRead();
 		}
 		return false;
 	}
 	
 	/**
 	 * copied from ContentAssistUtils
 	 * @param viewer
 	 * @param documentOffset
 	 * @return
 	 */
 	public static IndexedRegion getNodeAt(IDocument d, int documentOffset) {
 		if (d == null)
 			return null;
 
 		IndexedRegion node = null;
 		IModelManager mm = StructuredModelManager.getModelManager();
 		IStructuredModel model = null;
 		if (mm != null)
 			model = mm.getModelForRead((IStructuredDocument)d);
 		try {
 			if (model != null) {
 				int lastOffset = documentOffset;
 				node = model.getIndexedRegion(documentOffset);
 				while (node == null && lastOffset >= 0) {
 					lastOffset--;
 					node = model.getIndexedRegion(lastOffset);
 				}
 			}
 		} finally {
 			if (model != null)
 				model.releaseFromRead();
 		}
 		return node;
 	}
 	
 	private static Node getSelectedNode(IDocument d, Properties p){
 		ISelectionProvider selProvider = (ISelectionProvider)p.get(PaletteInsertHelper.PROPOPERTY_SELECTION_PROVIDER);
 		if(selProvider == null) return null;
 		
 		ITextSelection selection = null;
 		
 		if(selProvider.getSelection() instanceof ITextSelection)
 			selection = (ITextSelection)selProvider.getSelection();
 		else return null;
 		
 		IndexedRegion region = getNodeAt(d, selection.getOffset());
 		if(region == null) return null;
 		
 		if(!(region instanceof Node)) return null;
 		
 		Node text = (Node)region;
 		
 		
 		if("#text".equals(text.getNodeName())) //$NON-NLS-1$
 			return text.getParentNode();
 		else
 			return text;
 	}
 	
 	private static boolean checkSelectedElement(HashMap<String,String> map, IDocument d, Properties p){
 		String taglibUri = p.getProperty(JSPPaletteInsertHelper.PROPOPERTY_TAGLIBRARY_URI);
 		if(taglibUri == null) return false;
 		
 		Node selectedNode = getSelectedNode(d, p);
 		if(selectedNode == null) return false;
 		
 		return checkElement(map, selectedNode, taglibUri);
 	}
 	
 	private static boolean checkElement(HashMap<String,String> map, Node node, String taglibUri){
 		
 		NamedNodeMap attrs = node.getAttributes();
 		for (int j = 0; attrs != null && j < attrs.getLength(); j++) {
 			Node a = attrs.item(j);
 			String name = a.getNodeName();
 
 			if (name.startsWith("xmlns:")) { //$NON-NLS-1$
 				map.put(a.getNodeValue(), name.substring("xmlns:".length())); //$NON-NLS-1$
 			}
 		}
 
 		if (map.containsKey(taglibUri)) return true;
 		else{
 			if(node.getParentNode() == null) return false;
 			else return checkElement(map, node.getParentNode(), taglibUri);
 		}
 	}
 
 	public boolean inserTaglibInXml(IDocument d, Properties p) {
 		if(!checkProperties(p)) {
 			return false;
 		}
 		
 		IStructuredModel model = null;
 		
 		try {
 			model = StructuredModelManager.getModelManager().getModelForRead((IStructuredDocument)d);
 			IDOMDocument xmlDocument = (model instanceof IDOMModel) ? ((IDOMModel) model).getDocument() : null;
 			
 			if (xmlDocument == null) {
 				return false;
 			}
 			
 			Properties tl = getPrefixes(d, p);
 			if(tl == null) tl = JSPPaletteInsertHelper.getPrefixes(d.get());
 			Element root = xmlDocument.getDocumentElement();
 			if(root != null) {
				IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				
 				// for xhtml and jsp:root 
 				if (xmlDocument.getDoctype() != null /* && tagLibListConainsFacelet(tl)*/ ) {
 					String publicId = xmlDocument.getDoctype().getPublicId();
 					if (publicId!=null && publicId.toUpperCase().startsWith("-//W3C//DTD XHTML")) { // && root.getNodeName().equalsIgnoreCase(HTML_SOURCE_ROOT_ELEMENT)) { //$NON-NLS-1$
 						checkTL(root, p, d);
 						return true;
 					}
				} else if(xmlDocument.isXMLType() || root.getNodeName().equals(JSP_SOURCE_ROOT_ELEMENT) || (editorPart != null && editorPart.getTitle().toLowerCase().endsWith(".jspx"))) { //$NON-NLS-1$
 					checkTL(root, p, d);
 					return true;
 				}
 			}
 		} finally {
 			if (model != null)	model.releaseFromRead();
 		}
 		return false;
 	}
 
 //	private static boolean tagLibListConainsFacelet(List tagLibList) {
 //		if (tagLibList != null && !tagLibList.isEmpty()) {
 //			for (int i = 0; i < tagLibList.size(); i++) {
 //				TaglibData tgld = (TaglibData)tagLibList.get(i);
 //				if(faceletUri.equals(tgld.getUri())) {
 //					return true;
 //				}
 //			}
 //		}
 //		return false;
 //	}
 
 	/*
 	 * analyse source for taglib, return the list of taglib
 	 */
 	public static Properties getPrefixes(IDocument d, Properties properties) {
 		List list = getTaglibData(d, properties);
 		Properties p = new Properties();
 		for (int i = 0; i < list.size(); i++) {
 			TaglibData data = (TaglibData)list.get(i);
 			p.setProperty(data.getUri(), data.getPrefix());
 		}
 		return p;
 	}
 	
 	private static List<TaglibData> getTaglibData(IDocument d, Properties p){
 		ISourceViewer viewer= getViewer(p);
 		if(viewer != null){
 			VpeTaglibManager tldManager = null;
 			if(viewer instanceof VpeTaglibManagerProvider) {
 				tldManager = ((VpeTaglibManagerProvider)viewer).getTaglibManager();
 				if(tldManager != null) {
 					return tldManager.getTagLibs();
 				}			
 			}
 		}else{
 			List<TaglibData> result = XmlUtil.getTaglibsForJSPDocument(d, new ArrayList<TaglibData>());
 			
 			if(result == null || result.isEmpty()){
 				Node node = getSelectedNode(d, p);
 				if(node != null){
 					result = XmlUtil.processNode(node, new ArrayList<TaglibData>());
 				}
 			}
 			return result;
 		}
 		return null;
 	}
 
 	/*
 	 * for jsp:root and html check the taglib if exist check the prefix else add the taglib
 	 * with text formatting
 	 */
 	private static Properties checkTL(Element root, Properties p, IDocument d) {
 		String uri_p = p.getProperty(JSPPaletteInsertHelper.PROPOPERTY_TAGLIBRARY_URI);
 		String defaultPrefix_p = p.getProperty(JSPPaletteInsertHelper.PROPOPERTY_DEFAULT_PREFIX);
 
 		HashMap<String,String> map = new HashMap<String,String>();
 		NamedNodeMap attrs = root.getAttributes();
 		for (int j = 0; attrs != null && j < attrs.getLength(); j++) {
 			Node a = attrs.item(j);
 			String name = a.getNodeName();
 
 			if (name.startsWith("xmlns:")) { //$NON-NLS-1$
 				map.put(a.getNodeValue(), name.substring("xmlns:".length())); //$NON-NLS-1$
 			}
 		}
 		
 		if (map.containsKey(uri_p) || checkSelectedElement(map, d, p)) {
 			if (!map.get(uri_p).equals(defaultPrefix_p)) {
 				p.setProperty(JSPPaletteInsertHelper.PROPOPERTY_DEFAULT_PREFIX, (String) map.get(uri_p));
 			}
 		} else if(!map.containsValue(defaultPrefix_p)) {
 			StringBuffer attribute = new StringBuffer("xmlns:").append(defaultPrefix_p).append("=\"").append(uri_p).append("\"");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			int so = ((IDOMElement)root).getStartOffset();
 			int seo = ((IDOMElement)root).getStartEndOffset();
 			try {
 				String lineDelimiter = PaletteInsertHelper.getLineDelimiter(d);
 				StringBuffer selectedSource = new StringBuffer().append(d.get(so, seo-so));
 				int xmlns = selectedSource.indexOf("xmlns"); //$NON-NLS-1$
 				attribute = new StringBuffer().append(createEmptyCharArray(xmlns)).append("xmlns:").append(defaultPrefix_p).append("=\"").append(uri_p).append("\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				if (d.getLineOffset(d.getLineOfOffset(so)) != so) {										
 					attribute.insert(0, analyseSubstring(d.get(d.getLineOffset(d.getLineOfOffset(so)), so-d.getLineOffset(d.getLineOfOffset(so)))));
 				}				
 				if(xmlns>0) {
 					attribute.insert(0, lineDelimiter);
 				} else {
 					attribute.insert(0, ' ');
 				}
 				selectedSource.insert(selectedSource.length()-1, attribute);
 				d.replace(so, seo-so, selectedSource.toString());
 			} catch (BadLocationException t) {
 				ModelUIPlugin.getPluginLog().logError("", t); //$NON-NLS-1$
 			}
 		}
 		return p;
 	}
 
 	private static char[] createEmptyCharArray(int n){
 		if(n<1) {
 			return new char[0];
 		}
 		char[] ca = new char[n];
 		for (int i = 0; i < n; i++)
 			ca[i]=' ';
 		return ca;
 	}
 	
 	private static ISourceViewer getViewer(Properties p){
 		return (ISourceViewer)p.get("viewer"); //$NON-NLS-1$
 	}
 
 	private static void mouveFocusOnPage(Properties p, int length, int pos){
 		ISourceViewer v = getViewer(p);
 		ISelectionProvider selProvider = (ISelectionProvider)p.get(PaletteInsertHelper.PROPOPERTY_SELECTION_PROVIDER);
 		IDocument doc = v.getDocument();
 
 		if (doc== null || selProvider == null) return;
 
 		ITextSelection selection = (ITextSelection)selProvider.getSelection();
 		if (selection.getOffset() == 0) {			
 			 v.setSelectedRange(length,0);
 			 p.put(PaletteInsertHelper.PROPOPERTY_SELECTION_PROVIDER,v.getSelectionProvider());
 		}
 		else 
 		if (selection.getOffset() == pos ){
 			v.setSelectedRange(length, 0);
 			p.put(PaletteInsertHelper.PROPOPERTY_SELECTION_PROVIDER,v.getSelectionProvider());
 		}
 	}
 
 	/*
 	 * analyse the space between the left corner and the start offset o the text
 	 */ 
 	private static StringBuffer analyseSubstring(String str){
 		StringBuffer st = new StringBuffer().append(str);		
 		for (int i = 0; i < st.length(); i++) {
 			if (st.charAt(i) != ' ' && st.charAt(i) !='\t' ) {
 				st.setCharAt(i, ' ');
 			}
 		}
 		return st;		
 	}
 
 	private static boolean checkplace(IDOMDocument xmlDocument, IDocument d, String st, StringBuffer tg, Properties p) throws BadLocationException {
 		NodeList nl = xmlDocument.getChildNodes();
 		boolean docType = false;
 		IndexedRegion irdt = null;
 
 		if (xmlDocument.getDoctype() != null) {
 			docType = true;
 			String publicId = xmlDocument.getDoctype().getPublicId();
 			if (publicId!=null && publicId.toUpperCase().startsWith("-//W3C//DTD HTML")) { //$NON-NLS-1$
 				irdt = (xmlDocument.getDoctype() instanceof IndexedRegion) ?
 						(IndexedRegion)xmlDocument.getDoctype(): null;
 			}
 		}
 
 		if (nl != null && nl.getLength() != 0) {
 			for (int i=0; i < nl.getLength(); i++) {
 				Node n = nl.item(i);
 				//fing the first taglib to insert before
 				if (n.getNodeName().equals(st) && st.equals("jsp:directive.taglib")) { //$NON-NLS-1$
 					//calculate the space between taglib and left page corner
 					int so = ((ElementImpl)n).getStartOffset();
 					//taglib is at left corner 
 					if (d.getLineOffset(d.getLineOfOffset(so)) == so) {
 						d.replace(so, 0, tg.toString());
 					} else {
 						StringBuffer left = new StringBuffer().
 						append(analyseSubstring(d.get(d.getLineOffset(d.getLineOfOffset(so)), so-d.getLineOffset(d.getLineOfOffset(so)))));
 						tg.insert(tg.length(), left);
 						d.replace(so, 0, tg.toString());
 					}
 					return true;
 				}
 				if ((n.getNodeName().equals(st) && st.equals("jsp:directive.page"))	) { //$NON-NLS-1$
 					tg.delete(tg.lastIndexOf(PaletteInsertHelper.getLineDelimiter(d)), tg.length());
 					int so = ((ElementImpl)n).getStartOffset();
 					int eo = ((ElementImpl)n).getEndStartOffset();
 					StringBuffer tgleft = new StringBuffer().append(PaletteInsertHelper.getLineDelimiter(d));
 					if (d.getLineOffset(d.getLineOfOffset(so)) == so) {
 						tgleft.append(tg);
 						d.replace(eo, 0, tgleft.toString());
 						mouveFocusOnPage(p, eo + tgleft.length(), eo);
 					} else {
 						tgleft.append(analyseSubstring(d.get(d.getLineOffset(d.getLineOfOffset(so)), so-d.getLineOffset(d.getLineOfOffset(so)))));
 						tgleft.append(tg);
 						d.replace(eo, 0, tgleft.toString());
 						mouveFocusOnPage(p, eo + tgleft.length(), eo);
 					}
 					return true;
 				}
 				if (docType && irdt != null) {
 					tg.delete(tg.lastIndexOf(PaletteInsertHelper.getLineDelimiter(d)), tg.length());
 					int so = irdt.getStartOffset();
 					int eo = irdt.getEndOffset();
 					StringBuffer tgleft = new StringBuffer().append(PaletteInsertHelper.getLineDelimiter(d));
 					if (d.getLineOffset(d.getLineOfOffset(so)) == so) {
 						tgleft.append(tg);
 						d.replace(eo, 0, tgleft.toString());
 						mouveFocusOnPage(p, eo + tgleft.length(), eo);
 					} else {
 						tgleft.append(analyseSubstring(d.get(d.getLineOffset(d.getLineOfOffset(so)), so-d.getLineOffset(d.getLineOfOffset(so)))));
 						tgleft.append(tg);
 						d.replace(eo, 0, tgleft.toString());
 						mouveFocusOnPage(p, eo + tgleft.length(), eo);
 					}
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 }
