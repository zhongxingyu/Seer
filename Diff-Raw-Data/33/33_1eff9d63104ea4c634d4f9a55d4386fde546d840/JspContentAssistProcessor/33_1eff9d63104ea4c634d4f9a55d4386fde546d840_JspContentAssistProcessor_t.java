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
 package org.jboss.tools.jst.jsp.contentassist;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.jst.jsp.core.internal.contentmodel.TaglibController;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TLDCMDocumentManager;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TaglibTracker;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.xml.core.internal.document.NodeContainer;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
 import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImageHelper;
 import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImages;
 import org.jboss.tools.common.el.core.model.ELExpression;
 import org.jboss.tools.common.el.core.parser.ELParserFactory;
 import org.jboss.tools.common.el.core.parser.ELParserUtil;
 import org.jboss.tools.common.el.core.resolver.ELCompletionEngine;
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.el.core.resolver.ELContextImpl;
 import org.jboss.tools.common.el.core.resolver.ELResolution;
 import org.jboss.tools.common.el.core.resolver.ELResolutionImpl;
 import org.jboss.tools.common.el.core.resolver.ElVarSearcher;
 import org.jboss.tools.common.el.core.resolver.Var;
 import org.jboss.tools.common.text.TextProposal;
 import org.jboss.tools.jst.web.kb.IPageContext;
 import org.jboss.tools.jst.web.kb.IResourceBundle;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.PageProcessor;
 import org.jboss.tools.jst.web.kb.KbQuery.Type;
 import org.jboss.tools.jst.web.kb.internal.JspContextImpl;
 import org.jboss.tools.jst.web.kb.internal.ResourceBundle;
import org.jboss.tools.jst.web.kb.internal.XmlContextImpl;
 import org.jboss.tools.jst.web.kb.internal.taglib.NameSpace;
 import org.jboss.tools.jst.web.kb.taglib.INameSpace;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * 
  * @author Jeremy
  *
  */
 public class JspContentAssistProcessor extends XmlContentAssistProcessor {
 
 	@Override
 	protected ELContext createContextInstance() {
 		return new JspContextImpl();
 	}
 	
 	/**
 	 * Collects the namespaces over the JSP-page and sets them up to the context specified.
 	 * 
 	 * @param context
 	 */
 	protected void setNameSpaces(XmlContextImpl context) {
 		super.setNameSpaces(context);
 		
 		IStructuredModel sModel = StructuredModelManager
 									.getModelManager()
 									.getExistingModelForRead(getDocument());
 		try {
 			if (sModel == null) 
 				return;
 			
 			Document xmlDocument = (sModel instanceof IDOMModel) ? 
 							((IDOMModel) sModel).getDocument() : 
 								null;
 
 			if (xmlDocument == null)
 				return;
 
 			TLDCMDocumentManager manager = TaglibController.getTLDCMDocumentManager(getDocument());
 			List trackers = (manager == null? null : manager.getCMDocumentTrackers(getOffset()));
 			for (int i = 0; trackers != null && i < trackers.size(); i++) {
 				TaglibTracker tt = (TaglibTracker)trackers.get(i);
 				final String prefix = tt.getPrefix();
 				final String uri = tt.getURI();
 				if (prefix != null && prefix.trim().length() > 0 &&
 						uri != null && uri.trim().length() > 0) {
 						
 					IRegion region = new Region(0, getDocument().getLength());
 					INameSpace nameSpace = new NameSpace(uri.trim(), prefix.trim());
 					context.addNameSpace(region, nameSpace);
 				}
 			}
 
 			return;
 		}
 		finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 	}
 
 	
 	
 	/**
 	 * Returns the resource bundles  
 	 * 
 	 * @return
 	 */
 	protected IResourceBundle[] getResourceBundles(IPageContext context) {
 		List<IResourceBundle> list = new ArrayList<IResourceBundle>();
 		IStructuredModel sModel = StructuredModelManager.getModelManager().getExistingModelForRead(getDocument());
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
 
 	private void update(IDOMNode element, IPageContext context, List<IResourceBundle> list) {
 		if (element !=  null) {
 			registerBundleForNode(element, context, list);
 			for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
 				if (child instanceof IDOMNode) {
 					update((IDOMNode)child, context, list);
 				}
 			}
 		}
 	}
 	private void registerBundleForNode(IDOMNode node, IPageContext context, List<IResourceBundle> list) {
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
 	private boolean containsPrefix(Map<String, List<INameSpace>> ns, String prefix) {
 		for (List<INameSpace> n: ns.values()) {
 			for (INameSpace nameSpace : n) {
 				if(prefix.equals(nameSpace.getPrefix())) return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns URI string for the prefix specified using the namespaces collected for 
 	 * the {@link IPageContext} context.
 	 * Important: The context must be created using createContext() method before using this method.
 	 * 
 	 * @param prefix
 	 * @return
 	 */
 	@Override
 	public String getUri(String prefix) {
 		if (prefix == null || prefix.length() == 0)
 			return null;
 		
 		Map<String, List<INameSpace>> nameSpaces = getContext().getNameSpaces(getOffset());
 		if (nameSpaces == null || nameSpaces.isEmpty())
 			return null;
 		
 		for (List<INameSpace> nameSpace : nameSpaces.values()) {
 			for (INameSpace n : nameSpace) {
 				if (prefix.equals(n.getPrefix())) {
 					return n.getURI();
 				}
 			}
 		}
 		return null;
 	}
 
 	/*
 	 * Checks if the specified attribute exists 
 	 * 
 	 * @param attrName Name of attribute to check
 	 */
 	protected boolean isExistingAttribute(String attrName) {
 		IStructuredModel sModel = StructuredModelManager.getModelManager()
 				.getExistingModelForRead(getDocument());
 		try {
 			if (sModel == null)
 				return false;
 
 			Document xmlDocument = (sModel instanceof IDOMModel) ? ((IDOMModel) sModel)
 					.getDocument()
 					: null;
 
 			if (xmlDocument == null)
 				return false;
 
 			// Get Fixed Structured Document Region
 			IStructuredDocumentRegion sdFixedRegion = this.getStructuredDocumentRegion(getOffset());
 			if (sdFixedRegion == null)
 				return false;
 			
 			Node n = findNodeForOffset(xmlDocument, sdFixedRegion.getStartOffset());
 			if (n == null)
 				return false;
 			
 			// Find the first parent tag
 			if (!(n instanceof Element)) {
 				if (n instanceof Attr) {
 					n = ((Attr) n).getOwnerElement();
 				} else {
 					return false;
 				}
 			}
 			
 			if (n == null)
 				return false;
 
 			return (((Element)n).getAttribute(attrName) != null);
 		} finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 
 	}
 	
 	/**
 	 * Calculates and adds the attribute name proposals to the Content Assist Request object
 	 * 
 	 * @param contentAssistRequest Content Assist Request object
 	 * @param childPosition the 
 	 */
 	protected void addAttributeNameProposals(ContentAssistRequest contentAssistRequest) {
 		try {
 			String matchString = contentAssistRequest.getMatchString();
 			String query = matchString;
 			if (query == null)
 				query = ""; //$NON-NLS-1$
 			String stringQuery = matchString;
 					
 			KbQuery kbQuery = createKbQuery(Type.ATTRIBUTE_NAME, query, stringQuery);
 			TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, getContext());
 			
 			for (int i = 0; proposals != null && i < proposals.length; i++) {
 				TextProposal textProposal = proposals[i];
 				
 				if (isExistingAttribute(textProposal.getLabel())) 
 					continue;
 				
 				String replacementString = textProposal.getReplacementString() + "=\"\""; //$NON-NLS-1$
 				
 				int replacementOffset = contentAssistRequest.getReplacementBeginPosition();
 				int replacementLength = contentAssistRequest.getReplacementLength();
 				int cursorPosition = getCursorPositionForProposedText(replacementString);
 				Image image = textProposal.getImage();
 				if (image == null) {
 					image = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_ATTRIBUTE);
 				}
 
 				String displayString = textProposal.getLabel() == null ? 
 						replacementString : 
 							textProposal.getLabel();
 				IContextInformation contextInformation = null;
 				String additionalProposalInfo = textProposal.getContextInfo();
 				int relevance = textProposal.getRelevance();
 				
 				AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(true, replacementString, 
 						replacementOffset, replacementLength, cursorPosition, image, displayString, 
 						contextInformation, additionalProposalInfo, relevance);
 
 				contentAssistRequest.addProposal(proposal);
 			}
 		} finally {
 		}
 	}
 
 
 	/**
 	 * Calculates and adds the EL proposals to the Content Assist Request object
 	 */
 	@Override
 	protected void addTextELProposals(ContentAssistRequest contentAssistRequest) {
 	}
 
 	protected void setVars(ELContextImpl context, IFile file) {
 		ELCompletionEngine fakeEngine = new ELCompletionEngine() {
 
 			public ELResolution resolveELOperand(IFile file,
 					ELExpression operand, boolean returnEqualedVariablesOnly,
 					List<Var> vars, ElVarSearcher varSearcher)
 					throws BadLocationException, StringIndexOutOfBoundsException {
 				return new ELResolutionImpl();
 			}
 
 			public ELParserFactory getParserFactory() {
 				return ELParserUtil.getJbossFactory();
 			}
 
 			public List<TextProposal> getProposals(ELContext context, String el) {
 				return Collections.emptyList();
 			}
 
 			public ELResolution resolve(ELContext context, ELExpression operand) {
 				return new ELResolutionImpl();
 			}
 		};
 		ElVarSearcher varSearcher = new ElVarSearcher(file, fakeEngine);
 		List<Var> vars = varSearcher.findAllVars(file, getOffset());
 
 		if (vars != null) {
 			for (Var var : vars) {
 				context.addVar(new Region(getOffset(), 0), var);
 			}
 		}
 	}
 }
