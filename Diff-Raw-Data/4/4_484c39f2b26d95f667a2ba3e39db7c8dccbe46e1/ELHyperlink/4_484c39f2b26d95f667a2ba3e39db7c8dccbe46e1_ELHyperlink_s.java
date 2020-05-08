 /*******************************************************************************
  * Copyright (c) 2011 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.text.ext.hyperlink;
 
 import java.text.MessageFormat;
 import java.util.List;
 import java.util.Properties;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.Region;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PartInitException;
 import org.jboss.tools.common.el.core.ELReference;
 import org.jboss.tools.common.el.core.resolver.ELSegment;
 import org.jboss.tools.common.el.core.resolver.IOpenableReference;
 import org.jboss.tools.common.el.core.resolver.JavaMemberELSegment;
 import org.jboss.tools.common.el.core.resolver.MessagePropertyELSegment;
 import org.jboss.tools.common.model.XModel;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.project.IPromptingProvider;
 import org.jboss.tools.common.model.project.PromptingProviderFactory;
 import org.jboss.tools.common.model.util.PositionHolder;
 import org.jboss.tools.common.text.ext.hyperlink.AbstractHyperlink;
 import org.jboss.tools.common.text.ext.hyperlink.xpl.Messages;
 import org.jboss.tools.common.text.ext.util.StructuredModelWrapper;
 import org.jboss.tools.common.text.ext.util.StructuredSelectionHelper;
 import org.jboss.tools.common.text.ext.util.Utils;
 import org.jboss.tools.jst.text.ext.JSTExtensionsPlugin;
 import org.jboss.tools.jst.web.project.list.WebPromptingProvider;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 public class ELHyperlink extends AbstractHyperlink{
 	private static final String VIEW_TAGNAME = "view"; //$NON-NLS-1$
 	private static final String LOCALE_ATTRNAME = "locale"; //$NON-NLS-1$
 	private static final String PREFIX_SEPARATOR = ":"; //$NON-NLS-1$
 	
 	private ELReference reference;
 	private ELSegment segment;
 	private XModelObject xObject;
 	
 	public ELHyperlink(IDocument document, ELReference reference, ELSegment segment, XModelObject xObject){
 		this.reference = reference;
 		this.segment = segment;
 		this.xObject = xObject;
 		setDocument(document);
 	}
 
 	@Override
 	protected IRegion doGetHyperlinkRegion(int offset) {
 		
 		return new IRegion(){
 			public int getLength() {
 				return segment.getSourceReference().getLength();
 			}
 
 			public int getOffset() {
 				return reference.getStartPosition()+segment.getSourceReference().getStartPosition();
 			}};
 	}
 
 	@Override
 	protected void doHyperlink(IRegion region) {
 		IOpenableReference[] openables = segment.getOpenable();
 		
 		if(openables.length > 0) {
			openables[0].open();
 			//If openables.length > 1 - show menu.
 			return;
 		}
 		
 		if(segment instanceof JavaMemberELSegment){
 			try {
 				if(JavaUI.openInEditor(((JavaMemberELSegment) segment).getJavaElement()) == null){
 					openFileFailed();
 				}
 			} catch (PartInitException e) {
 				JSTExtensionsPlugin.getDefault().logError(e);
 			} catch (JavaModelException e) {
 				JSTExtensionsPlugin.getDefault().logError(e);
 			}
 			return;
 		}else if(segment instanceof MessagePropertyELSegment){
 			IFile file = ((MessagePropertyELSegment)segment).getMessageBundleResource();
 			if(file == null)
 				file = (IFile)segment.getResource();
 			
 			XModel xModel = getXModel(file);
 			if (xModel == null) {
 				openFileFailed();
 				return;
 			}
 			String bundleBasename = ((MessagePropertyELSegment)segment).getBaseName();
 			String property = ((MessagePropertyELSegment)segment).isBundle() ? null : trimQuotes(((MessagePropertyELSegment)segment).getToken().getText());
 			String locale = getPageLocale(region);
 			
 			Properties p = new Properties();
 			
 			if (bundleBasename != null) {
 				p.put(WebPromptingProvider.BUNDLE, bundleBasename);
 			}
 			
 			if (property != null) {
 				p.put(WebPromptingProvider.KEY, property);
 			}
 			
 			if (locale != null) {
 				p.setProperty(WebPromptingProvider.LOCALE, locale);
 			}
 			
 			IPromptingProvider provider = PromptingProviderFactory.WEB;
 
 			p.put(IPromptingProvider.FILE, file);
 
 			List list = provider.getList(xModel, getRequestMethod(p), p.getProperty("prefix"), p); //$NON-NLS-1$
 			if (list != null && list.size() >= 1) {
 				openFileInEditor((String)list.get(0));
 				return;
 			}
 			String error = p.getProperty(IPromptingProvider.ERROR); 
 			if ( error != null && error.length() > 0) {
 				openFileFailed();
 			}
 			return;
 		}else if(xObject != null){
 			IRegion attrRegion = null;
 			PositionHolder h = PositionHolder.getPosition(xObject, null);
 			h.update();
 			if (h.getStart() == -1 || h.getEnd() == -1) {
 				openFileFailed();
 				return;
 			}
 			attrRegion = new Region(h.getStart(), h.getEnd() - h.getStart());
 			IFile file = (IFile)xObject.getAdapter(IFile.class);
 			if (file != null) {
 				if (openFileInEditor(file) != null) {
 					StructuredSelectionHelper.setSelectionAndRevealInActiveEditor(attrRegion);
 					return;
 				}
 			}
 		}
 		
 		openFileFailed();
 	}
 	
 	private String getPageLocale(IRegion region) {
 		if(getDocument() == null || region == null) return null;
 
 		StructuredModelWrapper smw = new StructuredModelWrapper();
 		try {
 			smw.init(getDocument());
 			Document xmlDocument = smw.getDocument();
 			if (xmlDocument == null) return null;
 			
 			Node n = Utils.findNodeForOffset(xmlDocument, region.getOffset());
 			if (!(n instanceof Attr) ) return null; 
 
 			Element el = ((Attr)n).getOwnerElement();
 			
 			Element jsfCoreViewTag = null;
 			String nodeToFind = PREFIX_SEPARATOR + VIEW_TAGNAME; 
 	
 			while (el != null) {
 				if (el.getNodeName() != null && el.getNodeName().endsWith(nodeToFind)) {
 					jsfCoreViewTag = el;
 					break;
 				}
 				Node parent = el.getParentNode();
 				el = (parent instanceof Element ? (Element)parent : null); 
 			}
 			
 			if (jsfCoreViewTag == null || !jsfCoreViewTag.hasAttribute(LOCALE_ATTRNAME)) return null;
 			
 			String locale = Utils.trimQuotes((jsfCoreViewTag.getAttributeNode(LOCALE_ATTRNAME)).getValue());
 			if (locale == null || locale.length() == 0) return null;
 			return locale;
 		} finally {
 			smw.dispose();
 		}
 	}
 	
 	private String trimQuotes(String value) {
 		if(value == null)
 			return null;
 
 		if(value.startsWith("'") || value.startsWith("\"")) {  //$NON-NLS-1$ //$NON-NLS-2$
 			value = value.substring(1);
 		} 
 		
 		if(value.endsWith("'") || value.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
 			value = value.substring(0, value.length() - 1);
 		}
 		return value;
 	}
 	
 	private String getRequestMethod(Properties prop) {
 		return prop != null && prop.getProperty(WebPromptingProvider.KEY) == null ? 
 				WebPromptingProvider.JSF_OPEN_BUNDLE : WebPromptingProvider.JSF_OPEN_KEY;
 	}
 
 	@Override
 	public String getHyperlinkText() {
 		if(segment instanceof JavaMemberELSegment){
 			IJavaElement javaElement = ((JavaMemberELSegment) segment).getJavaElement();
 			String name = ""; //$NON-NLS-1$
 			IType type = null;
 			if(javaElement instanceof IType){
 				name = javaElement.getElementName();
 				type = (IType)javaElement;
 				
 			}else if(javaElement instanceof IMethod){
 				type = ((IMethod) javaElement).getDeclaringType();
 				name = type.getElementName()+"."+javaElement.getElementName()+"()"; //$NON-NLS-1$ //$NON-NLS-2$
 			}else if(javaElement instanceof IField){
 				type = ((IField) javaElement).getDeclaringType();
 				name = type.getElementName()+"."+javaElement.getElementName(); //$NON-NLS-1$
 			}
 			if(type != null)
 				name += " - "+type.getPackageFragment().getElementName(); //$NON-NLS-1$
 			return MessageFormat.format(Messages.Open, name);
 		}else if(segment instanceof MessagePropertyELSegment){
 			String baseName = ((MessagePropertyELSegment)segment).getBaseName();
 			String propertyName = ((MessagePropertyELSegment)segment).isBundle() ? null : trimQuotes(((MessagePropertyELSegment)segment).getToken().getText());
 			if (propertyName == null)
 				return  MessageFormat.format(Messages.Open, baseName);
 			
 			return MessageFormat.format(Messages.OpenBundleProperty, propertyName, baseName);
 		}else if(xObject != null){
 			return Messages.OpenJsf2CCAttribute;
 		}
 		
 		return ""; //$NON-NLS-1$
 	}
 
 	@Override
 	public IFile getReadyToOpenFile() {
 		IFile file = null;
 		if(segment instanceof JavaMemberELSegment){
 			
 			try {
 				file = (IFile)((JavaMemberELSegment) segment).getJavaElement().getUnderlyingResource();
 			} catch (JavaModelException e) {
 				JSTExtensionsPlugin.getDefault().logError(e);
 			}
 		}else if(segment instanceof MessagePropertyELSegment){
 			file = (IFile)((MessagePropertyELSegment)segment).getMessageBundleResource();
 		}
 		return file;
 	}
 
 }
