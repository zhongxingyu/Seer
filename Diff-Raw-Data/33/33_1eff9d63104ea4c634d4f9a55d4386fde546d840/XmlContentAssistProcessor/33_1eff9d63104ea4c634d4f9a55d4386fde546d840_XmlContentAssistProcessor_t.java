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
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
 import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImageHelper;
 import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImages;
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.el.core.resolver.ELContextImpl;
 import org.jboss.tools.common.el.core.resolver.ELResolver;
 import org.jboss.tools.common.text.TextProposal;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.jboss.tools.jst.jsp.messages.JstUIMessages;
 import org.jboss.tools.jst.web.kb.IPageContext;
 import org.jboss.tools.jst.web.kb.IResourceBundle;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.PageProcessor;
 import org.jboss.tools.jst.web.kb.KbQuery.Type;
 import org.jboss.tools.jst.web.kb.internal.XmlContextImpl;
 import org.jboss.tools.jst.web.kb.internal.taglib.NameSpace;
 import org.jboss.tools.jst.web.kb.taglib.INameSpace;
 import org.jboss.tools.jst.web.kb.taglib.ITagLibrary;
 import org.jboss.tools.jst.web.kb.taglib.TagLibriryManager;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 /**
  * 
  * @author Jeremy
  *
  */
 public class XmlContentAssistProcessor extends AbstractXMLContentAssistProcessor {
 	protected static final Image JSF_EL_PROPOSAL_IMAGE = JspEditorPlugin.getDefault().getImage(JspEditorPlugin.CA_JSF_EL_IMAGE_PATH);
 
 	@Override
 	protected ELContext createContextInstance() {
 		return new XmlContextImpl();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.jsp.contentassist.AbstractXMLContentAssistProcessor#createContext()
 	 */
 	@Override
 	protected IPageContext createContext() {
 		IFile file = getResource();
 		ELResolver[] elResolvers = getELResolvers(file);
 		
 		XmlContextImpl context = (XmlContextImpl)createContextInstance();
 		context.setResource(file);
 		context.setDocument(getDocument());
 		context.setElResolvers(elResolvers);
 		setVars(context, file);
 		
 		setNameSpaces(context);
 		context.setLibraries(getTagLibraries(context));
 		context.setResourceBundles(getResourceBundles(context));
 		
 		return context;
 	}
 	
 	protected void setVars(ELContextImpl context, IFile file) {
 		// No vars can be set for this processor
 	}
 
 	/**
 	 * Returns the <code>org.jboss.tools.common.el.core.resolver.ELContext</code> instance
 	 * 
 	 * @return
 	 */
 	@Override
 	public IPageContext getContext() {
 		return (IPageContext)super.getContext();
 	}
 
 	@Override 
 	protected KbQuery createKbQuery(Type type, String query, String stringQuery) {
 		KbQuery kbQuery = new KbQuery();
 
 		String prefix = getTagPrefix();
 		String  uri = getTagUri();
 		String[] parentTags = getParentTags(type == Type.ATTRIBUTE_NAME || type == Type.ATTRIBUTE_VALUE);
 		String	parent = getParent(type == Type.ATTRIBUTE_VALUE, type == Type.ATTRIBUTE_NAME);
 		String queryValue = query;
 		String queryStringValue = stringQuery;
 		
 		kbQuery.setPrefix(prefix);
 		kbQuery.setUri(uri);
 		kbQuery.setParentTags(parentTags);
 		kbQuery.setParent(parent); 
 		kbQuery.setMask(true); 
 		kbQuery.setType(type);
 		kbQuery.setOffset(getOffset());
 		kbQuery.setValue(queryValue); 
 		kbQuery.setStringQuery(queryStringValue);
 		
 		return kbQuery;
 	}
 
 	/**
 	 * Returns URI string for the prefix specified using the namespaces collected for 
 	 * the {@link IPageContext} context.
 	 * 
 	 * 	@Override org.jboss.tools.jst.jsp.contentassist.AbstractXMLContentAssistProcessor#getUri(String)
 	 */
 	protected String getUri(String prefix) {
 		return null;
 	}
 
 	/**
 	 * Calculates and adds the tag proposals to the Content Assist Request object
 	 * 
 	 * @param contentAssistRequest Content Assist Request object
 	 * @param childPosition the 
 	 */
 
 	@Override
 	protected void addTagInsertionProposals(
 			ContentAssistRequest contentAssistRequest, int childPosition) {
 		
 		// Need to check if an EL Expression is opened here.
 		// If it is true we don't need to start any new tag proposals
 		TextRegion prefix = getELPrefix();
 		if (prefix != null && prefix.isELStarted()) {
 			return;
 		}
 		
 		try {
 			String matchString = contentAssistRequest.getMatchString();
 			String query = matchString;
 			if (query == null)
 				query = ""; //$NON-NLS-1$
 			String stringQuery = "<" + matchString; //$NON-NLS-1$
 					
 			KbQuery kbQuery = createKbQuery(Type.TAG_NAME, query, stringQuery);
 			TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, getContext());
 			
 			for (int i = 0; proposals != null && i < proposals.length; i++) {
 				TextProposal textProposal = proposals[i];
 				
 				String replacementString = textProposal.getReplacementString();
 				String closingTag = textProposal.getLabel();
 				if (closingTag != null && closingTag.startsWith("<")) { //$NON-NLS-1$
 					closingTag = closingTag.substring(1);
 				}
 
 				if (!replacementString.endsWith("/>")) { //$NON-NLS-1$
 					replacementString += "</" + closingTag + ">"; //$NON-NLS-1$ //$NON-NLS-2$
 				}
 
 
 				int replacementOffset = contentAssistRequest.getReplacementBeginPosition();
 				int replacementLength = contentAssistRequest.getReplacementLength();
 				int cursorPosition = getCursorPositionForProposedText(replacementString);
 				Image image = textProposal.getImage();
 				if (image == null) {
 					image = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_TAG_GENERIC);
 				}
 				String displayString = closingTag; //$NON-NLS-1$
 				IContextInformation contextInformation = null;
 				String additionalProposalInfo = textProposal.getContextInfo();
 				int relevance = textProposal.getRelevance();
 				if (relevance == TextProposal.R_NONE) {
 					relevance = TextProposal.R_TAG_INSERTION;
 				}
 				AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(true, replacementString, 
 						replacementOffset, replacementLength, cursorPosition, image, displayString, 
 						contextInformation, additionalProposalInfo, relevance);
 
 				contentAssistRequest.addProposal(proposal);
 			}
 		} finally {
 		}
 		return;
 	}
 
 
 	/**
 	 * Calculates and adds the tag name proposals to the Content Assist Request object
 	 * 
 	 * @param contentAssistRequest Content Assist Request object
 	 * @param childPosition the 
 	 */
 	@Override
 	protected void addTagNameProposals(
 			ContentAssistRequest contentAssistRequest, int childPosition) {
 		try {
 			String matchString = contentAssistRequest.getMatchString();
 			String query = matchString;
 			if (query == null)
 				query = ""; //$NON-NLS-1$
 			String stringQuery = "<" + matchString; //$NON-NLS-1$
 					
 			KbQuery kbQuery = createKbQuery(Type.TAG_NAME, query, stringQuery);
 			TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, getContext());
 			
 			for (int i = 0; proposals != null && i < proposals.length; i++) {
 				TextProposal textProposal = proposals[i];
 				
 				String replacementString = textProposal.getReplacementString();
 				String closingTag = textProposal.getLabel();
 				if (closingTag != null && closingTag.startsWith("<")) { //$NON-NLS-1$
 					closingTag = closingTag.substring(1);
 				}
 				
 				if (replacementString.startsWith("<")) { //$NON-NLS-1$
 					// Because the tag starting char is already in the text
 					replacementString = replacementString.substring(1);
 				}
 				if (!replacementString.endsWith("/>")) { //$NON-NLS-1$
 					replacementString += "</" + closingTag + ">"; //$NON-NLS-1$ //$NON-NLS-2$
 				}
 
 			
 				int replacementOffset = contentAssistRequest.getReplacementBeginPosition();
 				int replacementLength = contentAssistRequest.getReplacementLength();
 				int cursorPosition = getCursorPositionForProposedText(replacementString);
 				Image image = textProposal.getImage();
 				if (image == null) {
 					image = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_TAG_GENERIC);
 				}
 
 				String displayString = closingTag; //$NON-NLS-1$
 				IContextInformation contextInformation = null;
 				String additionalProposalInfo = textProposal.getContextInfo();
 				int relevance = textProposal.getRelevance();
 				if (relevance == TextProposal.R_NONE) {
 					relevance = TextProposal.R_TAG_INSERTION;
 				}
 
 				AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(true, replacementString, 
 						replacementOffset, replacementLength, cursorPosition, image, displayString, 
 						contextInformation, additionalProposalInfo, relevance);
 
 				contentAssistRequest.addProposal(proposal);
 			}
 		} finally {
 		}
 		return;
 	}
 
 	/**
 	 * Calculates and adds the attribute value proposals to the Content Assist Request object
 	 */
 	protected void addAttributeValueProposals(ContentAssistRequest contentAssistRequest) {
 		// Need to check if an EL Expression is opened here.
 		// If it is true we don't need to start any new tag proposals
 		TextRegion prefix = getELPrefix();
 		if (prefix != null && prefix.isELStarted()) {
 			return;
 		}
 		try {
 			String matchString = contentAssistRequest.getMatchString();
 			String query = matchString;
 			if (query == null)
 				query = ""; //$NON-NLS-1$
 			String stringQuery = matchString;
 
 			KbQuery kbQuery = createKbQuery(Type.ATTRIBUTE_VALUE, query, stringQuery);
 			TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, getContext());
 
 			for (int i = 0; proposals != null && i < proposals.length; i++) {
 				TextProposal textProposal = proposals[i];
 				int replacementOffset = contentAssistRequest.getReplacementBeginPosition();
 				int replacementLength = contentAssistRequest.getReplacementLength();
 				if(textProposal.getStart() >= 0 && textProposal.getEnd() >= 0) {
 					replacementOffset += textProposal.getStart() + 1;
 					replacementLength = textProposal.getEnd() - textProposal.getStart();
 				}
 				String replacementString = "\"" + textProposal.getReplacementString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
 				if(textProposal.getStart() >= 0 && textProposal.getEnd() >= 0) {
 					replacementString = textProposal.getReplacementString();
 				}
 				int cursorPosition = getCursorPositionForProposedText(replacementString);
 				Image image = textProposal.getImage();
 				String displayString = textProposal.getLabel() == null ? 
 						replacementString : 
 							textProposal.getLabel();
 				IContextInformation contextInformation = null;
 				String additionalProposalInfo = textProposal.getContextInfo();
 				int relevance = textProposal.getRelevance();
 				if (relevance == TextProposal.R_NONE) {
 					relevance = TextProposal.R_JSP_ATTRIBUTE_VALUE;
 				}
 
 				AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(replacementString, 
 						replacementOffset, replacementLength, cursorPosition, image, displayString, 
 						contextInformation, additionalProposalInfo, relevance);
 
 				contentAssistRequest.addProposal(proposal);
 			}
 		} finally {
 		}
 	}
 
 	@Override
 	protected void addAttributeValueELProposals(ContentAssistRequest contentAssistRequest) {
 		try {
 			TextRegion prefix = getELPrefix();
 			if (prefix == null) {
 				return;
 			}
 
 			if(!prefix.isELStarted()) {
 				AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(true, "#{}",  //$NON-NLS-1$
 						getOffset(), 0, 2, JSF_EL_PROPOSAL_IMAGE, JstUIMessages.JspContentAssistProcessor_NewELExpression, 
 						null, JstUIMessages.JspContentAssistProcessor_NewELExpressionAttrInfo, TextProposal.R_XML_ATTRIBUTE_VALUE_TEMPLATE);
 
 				contentAssistRequest.addProposal(proposal);
 				return;
 			}
 			String matchString = "#{" + prefix.getText(); //$NON-NLS-1$
 			String query = matchString;
 			if (query == null)
 				query = ""; //$NON-NLS-1$
 			String stringQuery = matchString;
 			
 			int beginChangeOffset = prefix.getStartOffset() + prefix.getOffset();
 					
 			KbQuery kbQuery = createKbQuery(Type.ATTRIBUTE_VALUE, query, stringQuery);
 			TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, getContext());
 			
 			for (int i = 0; proposals != null && i < proposals.length; i++) {
 				TextProposal textProposal = proposals[i];
 				
 				int replacementOffset = beginChangeOffset;
 				int replacementLength = prefix.getLength();
 				String replacementString = prefix.getText().substring(0, replacementLength) + textProposal.getReplacementString();
 				int cursorPosition = replacementString.length();
 				Image image = textProposal.getImage();
 				
 				// JBIDE-512, JBIDE-2541 related changes ===>>>
 //				String displayString = prefix.getText().substring(0, replacementLength) + textProposal.getReplacementString();
 				String displayString = textProposal.getLabel();
 				if (displayString == null)
 					displayString = textProposal.getReplacementString() == null ? replacementString : textProposal.getReplacementString();
  
 				// <<<=== JBIDE-512, JBIDE-2541 related changes
 
 				IContextInformation contextInformation = null;
 				String additionalProposalInfo = (textProposal.getContextInfo() == null ? "" : textProposal.getContextInfo()); //$NON-NLS-1$
 				int relevance = textProposal.getRelevance();
 				if (relevance == TextProposal.R_NONE) {
 					relevance = TextProposal.R_JSP_JSF_EL_VARIABLE_ATTRIBUTE_VALUE;
 				}
 
 				AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(replacementString, 
 						replacementOffset, replacementLength, cursorPosition, image, displayString, 
 						contextInformation, additionalProposalInfo, relevance);
 
 				contentAssistRequest.addProposal(proposal);
 			}
 
 			if (prefix.isELStarted() && !prefix.isELClosed()) {
 				AutoContentAssistantProposal proposal = new AutoContentAssistantProposal("}", //$NON-NLS-1$
 						getOffset(), 0, 1, JSF_EL_PROPOSAL_IMAGE, JstUIMessages.JspContentAssistProcessor_CloseELExpression, 
 						null, JstUIMessages.JspContentAssistProcessor_CloseELExpressionInfo, TextProposal.R_XML_ATTRIBUTE_VALUE_TEMPLATE);
 
 				contentAssistRequest.addProposal(proposal);
 			}
 		} finally {
 		}
 	}
 
 	@Override
 	protected void addTextELProposals(ContentAssistRequest contentAssistRequest) {
 		TextRegion prefix = getELPrefix();
 		if (prefix == null || !prefix.isELStarted()) {
 			AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(true, "#{}", //$NON-NLS-1$ 
 					contentAssistRequest.getReplacementBeginPosition(), 
 					0, 2, JSF_EL_PROPOSAL_IMAGE, JstUIMessages.JspContentAssistProcessor_NewELExpression, null, 
 					JstUIMessages.FaceletPageContectAssistProcessor_NewELExpressionTextInfo, TextProposal.R_XML_ATTRIBUTE_VALUE_TEMPLATE);
 			
 			contentAssistRequest.addProposal(proposal);
 			return;
 		}
 		String matchString = "#{" + prefix.getText(); //$NON-NLS-1$
 		String query = matchString;
 		if (query == null)
 			query = ""; //$NON-NLS-1$
 		String stringQuery = matchString;
 
 		int beginChangeOffset = prefix.getStartOffset() + prefix.getOffset();
 
 		KbQuery kbQuery = createKbQuery(Type.TEXT, query, stringQuery);
 		TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, getContext());
 
 		for (int i = 0; proposals != null && i < proposals.length; i++) {
 			TextProposal textProposal = proposals[i];
 
 			int replacementOffset = beginChangeOffset;
 			int replacementLength = prefix.getLength();
 			String replacementString = prefix.getText().substring(0, replacementLength) + textProposal.getReplacementString();
 			int cursorPosition = replacementString.length();
 			Image image = textProposal.getImage();
 
 			// JBIDE-512, JBIDE-2541 related changes ===>>>
 //			String displayString = prefix.getText().substring(0, replacementLength) + textProposal.getReplacementString();
 			String displayString = textProposal.getLabel();
 			if (displayString == null)
 				displayString = textProposal.getReplacementString() == null ? replacementString : textProposal.getReplacementString();
 
 			// <<<=== JBIDE-512, JBIDE-2541 related changes
 			IContextInformation contextInformation = null;
 			String additionalProposalInfo = textProposal.getContextInfo();
 			int relevance = textProposal.getRelevance();
 			if (relevance == TextProposal.R_NONE) {
 				relevance = TextProposal.R_JSP_JSF_EL_VARIABLE_ATTRIBUTE_VALUE;
 			}
 
 			AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(replacementString, 
 					replacementOffset, replacementLength, cursorPosition, image, displayString, 
 					contextInformation, additionalProposalInfo, relevance);
 
 			contentAssistRequest.addProposal(proposal);
 		}
 
 		if (prefix.isELStarted() && !prefix.isELClosed()) {
 			AutoContentAssistantProposal proposal = new AutoContentAssistantProposal("}", //$NON-NLS-1$
 					getOffset(), 0, 1, JSF_EL_PROPOSAL_IMAGE, JstUIMessages.JspContentAssistProcessor_CloseELExpression, 
 					null, JstUIMessages.JspContentAssistProcessor_CloseELExpressionInfo, TextProposal.R_XML_ATTRIBUTE_VALUE_TEMPLATE);
 
 			contentAssistRequest.addProposal(proposal);
 		}
 	}
 	
 	/**
 	 * Collects the namespaces over the JSP-page and sets them up to the context specified.
 	 * 
 	 * @param context
 	 */
 	protected void setNameSpaces(XmlContextImpl context) {
 		IStructuredModel sModel = StructuredModelManager
 									.getModelManager()
 									.getExistingModelForRead(getDocument());
 
 		try {
 			if (sModel == null)
 				return;
 
 			Document xmlDocument = (sModel instanceof IDOMModel) ? ((IDOMModel) sModel)
 					.getDocument()
 					: null;
 
 			if (xmlDocument == null)
 				return;
 
 			// Get Fixed Structured Document Region
 			IStructuredDocumentRegion sdFixedRegion = this.getStructuredDocumentRegion(getOffset());
 			if (sdFixedRegion == null)
 				return;
 			
 			Node n = findNodeForOffset(xmlDocument, sdFixedRegion.getStartOffset());
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
 												domElement.getLength());
 								
 							}
 
 							Region region = new Region(start, length);
 							INameSpace nameSpace = new NameSpace(uri.trim(), prefix.trim());
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
 
 	protected static final ITagLibrary[] EMPTY_LIBRARIES = new ITagLibrary[0];	
 	protected static final IResourceBundle[] EMPTY_RESOURCE_BUNDLES = new IResourceBundle[0];
 
 	/**
 	 * Returns the Tag Libraries for the namespaces collected in the context.
 	 * Important: The context must be created using createContext() method before using this method.
 	 * 
 	 * @param context The context object instance
 	 * @return
 	 */
 	public ITagLibrary[] getTagLibraries(IPageContext context) {
 		Map<String, List<INameSpace>> nameSpaces =  context.getNameSpaces(getOffset());
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
 	protected IResourceBundle[] getResourceBundles(IPageContext context) {
 		return EMPTY_RESOURCE_BUNDLES;
 	}
 }
