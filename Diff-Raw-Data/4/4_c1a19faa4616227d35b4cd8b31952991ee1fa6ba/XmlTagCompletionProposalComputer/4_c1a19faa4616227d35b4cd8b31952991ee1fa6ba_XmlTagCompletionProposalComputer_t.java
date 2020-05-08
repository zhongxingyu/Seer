 /*******************************************************************************
  * Copyright (c) 2010 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.jsp.contentassist.computers;
 
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.wst.dtd.core.internal.contentmodel.DTDImpl.DTDBaseAdapter;
 import org.eclipse.wst.dtd.core.internal.contentmodel.DTDImpl.DTDElementReferenceContentAdapter;
 import org.eclipse.wst.html.core.internal.contentmodel.HTMLPropertyDeclaration;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
 import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
 import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentModelGenerator;
 import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImageHelper;
 import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImages;
 import org.jboss.tools.common.el.core.model.ELInstance;
 import org.jboss.tools.common.el.core.model.ELInvocationExpression;
 import org.jboss.tools.common.el.core.model.ELModel;
 import org.jboss.tools.common.el.core.model.ELUtil;
 import org.jboss.tools.common.el.core.parser.ELParser;
 import org.jboss.tools.common.el.core.parser.ELParserUtil;
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.el.core.resolver.ELResolver;
 import org.jboss.tools.common.el.core.resolver.ELResolverFactoryManager;
 import org.jboss.tools.common.text.TextProposal;
 import org.jboss.tools.common.text.ext.util.Utils;
 import org.jboss.tools.jst.jsp.contentassist.AutoContentAssistantProposal;
 import org.jboss.tools.jst.web.kb.IPageContext;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.KbQuery.Type;
 import org.jboss.tools.jst.web.kb.PageContextFactory;
 import org.jboss.tools.jst.web.kb.PageProcessor;
 import org.jboss.tools.jst.web.kb.taglib.INameSpace;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /**
  * Tag Proposal computer for XML pages
  * 
  * @author Jeremy
  *
  */
 @SuppressWarnings("restriction")
 public class XmlTagCompletionProposalComputer  extends AbstractXmlCompletionProposalComputer {
 	protected static final ICompletionProposal[] EMPTY_PROPOSAL_LIST = new ICompletionProposal[0];
 	
 	@Override
 	protected XMLContentModelGenerator getContentGenerator() {
 		return new ELXMLContentModelGenerator();
 	}
 
 	@Override
 	protected boolean validModelQueryNode(CMNode node) {
 		boolean isValid = false;
 		if(node instanceof DTDElementReferenceContentAdapter) {
 			DTDElementReferenceContentAdapter content = (DTDElementReferenceContentAdapter)node;
 			if(content.getCMDocument() instanceof DTDBaseAdapter) {
 				DTDBaseAdapter dtd = (DTDBaseAdapter)content.getCMDocument();
 				//this maybe a little hacky, but it works, if you have a better idea go for it
 				String spec = dtd.getSpec();
 				isValid = spec.indexOf("html") != -1; //$NON-NLS-1$
 			}
 		} else if(node instanceof HTMLPropertyDeclaration) {
 			HTMLPropertyDeclaration propDec = (HTMLPropertyDeclaration)node;
 			isValid = !propDec.isJSP();
 		} else if (node instanceof CMAttributeDeclaration) {
 			isValid = true;
 		}
 		return isValid;
 	}
 
 	
 	/**
 	 * Calculates and adds the tag proposals to the Content Assist Request object
 	 * 
 	 * @param contentAssistRequest Content Assist Request object
 	 * @param childPosition the 
 	 */
 
 	@Override
 	protected void addTagInsertionProposals(
 			ContentAssistRequest contentAssistRequest, int childPosition, CompletionProposalInvocationContext context) {
 		
 		// Need to check if an EL Expression is opened here.
 		// If it is true we don't need to start any new tag proposals
 		TextRegion prefix = getELPrefix(contentAssistRequest);
 		if (prefix != null && prefix.isELStarted()) {
 			return;
 		}
 		
 		addTagNameProposals(contentAssistRequest, childPosition, true, context);
 	}
 	
 	@Override
 	protected void addAttributeNameProposals(ContentAssistRequest contentAssistRequest, 
 			CompletionProposalInvocationContext context) {
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
 
 			return (((Element)n).getAttributeNode(attrName) != null);
 		} finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 	}
 
 	@Override
 	protected void addAttributeValueProposals(
 			ContentAssistRequest contentAssistRequest,
 			CompletionProposalInvocationContext context) {
 		
 		fCurrentContext = context;
 		
 		// Need to check if an EL Expression is opened here.
 		// If it is true we don't need to start any new tag proposals
 		TextRegion prefix = getELPrefix(contentAssistRequest);
 		if (prefix != null && prefix.isELStarted()) {
 			return;
 		}
 		
 		String matchString = contentAssistRequest.getMatchString();
 		String query = Utils.trimQuotes(matchString);
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
 	}
 
 	protected void addTagNameProposals(ContentAssistRequest contentAssistRequest, int childPosition,
 			CompletionProposalInvocationContext context) {
 		addTagNameProposals(contentAssistRequest, childPosition, false, context);
 	}
 	/**
 	 * Calculates and adds the tag name proposals to the Content Assist Request object
 	 * 
 	 * @param contentAssistRequest Content Assist Request object
 	 * @param childPosition the 
 	 */
 	protected void addTagNameProposals(
 			ContentAssistRequest contentAssistRequest, 
 			int childPosition, boolean insertTagOpenningCharacter,
 			CompletionProposalInvocationContext context) {
 
 		String mainPrefix = getTagPrefix();
 		String mainURI = getTagUri();
 		
 		String query = contentAssistRequest.getMatchString();
 		addTagNameProposalsForPrefix(contentAssistRequest, childPosition, query, mainPrefix, mainURI, TextProposal.R_TAG_INSERTION, insertTagOpenningCharacter);
 
 		if (query == null || query.length() == 0 || query.contains(":")) //$NON-NLS-1$
 			return;
 		
 		// Make an additional proposals to allow prefixed tags to be entered with no prefix typed
 		ELContext elContext = getContext();
 		if (elContext instanceof IPageContext) {
 			IPageContext pageContext = (IPageContext)elContext;
 			Map<String, List<INameSpace>> nsMap = pageContext.getNameSpaces(contentAssistRequest.getReplacementBeginPosition());
 			if (nsMap == null) return;
 			
 			for (List<INameSpace> namespaces : nsMap.values()) {
 				if (namespaces == null) continue;
 				
 				for (INameSpace namespace : namespaces) {
 					String possiblePrefix = namespace.getPrefix(); 
 					if (possiblePrefix == null || possiblePrefix.length() == 0)
 						continue;	// Don't query proposals for the default value here
 					
 					String possibleURI = namespace.getURI();
 					String possibleQuery = namespace.getPrefix() + ":" + query; //$NON-NLS-1$
 					addTagNameProposalsForPrefix(contentAssistRequest, childPosition, 
 							possibleQuery, possiblePrefix, possibleURI, 
 							TextProposal.R_TAG_INSERTION - 1, 
 							insertTagOpenningCharacter);
 				}
 			}
 		}
 	}
 
 	private void addTagNameProposalsForPrefix(
 			ContentAssistRequest contentAssistRequest, 
 			int childPosition, 
 			String query,
 			String prefix,
 			String uri, 
 			int defaultRelevance,
 			boolean insertTagOpenningCharacter) {
 		if (query == null)
 			query = ""; //$NON-NLS-1$
 		String stringQuery = "<" + query; //$NON-NLS-1$
 				
 		KbQuery kbQuery = createKbQuery(Type.TAG_NAME, query, stringQuery, prefix, uri);
 		TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, getContext());
 		
 		for (int i = 0; proposals != null && i < proposals.length; i++) {
 			TextProposal textProposal = proposals[i];
 			boolean useAutoActivation = true;
 	
 			String replacementString = textProposal.getReplacementString();
 			String closingTag = textProposal.getLabel();
 			if (closingTag != null && closingTag.startsWith("<")) { //$NON-NLS-1$
 				closingTag = closingTag.substring(1);
 			}
 			
 			if (!insertTagOpenningCharacter && replacementString.startsWith("<")) { //$NON-NLS-1$
 				// Because the tag starting char is already in the text
 				replacementString = replacementString.substring(1);
 			}
 			if (!replacementString.endsWith("/>")) { //$NON-NLS-1$
 				replacementString += "</" + closingTag + ">"; //$NON-NLS-1$ //$NON-NLS-2$
 				useAutoActivation = false;	// JBIDE-6285: Don't invoke code assist automaticly if user inserts <tag></tag>.
 			}
 
 		
 			int replacementOffset = contentAssistRequest.getReplacementBeginPosition();
 			int replacementLength = contentAssistRequest.getReplacementLength();
 			int cursorPosition = getCursorPositionForProposedText(replacementString);
 			Image image = textProposal.getImage();
 			if (image == null) {
 				image = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_TAG_GENERIC);
 			}
 
 			String displayString = closingTag;
 			IContextInformation contextInformation = null;
 			String additionalProposalInfo = textProposal.getContextInfo();
 			int relevance = textProposal.getRelevance();
 			if (relevance == TextProposal.R_NONE) {
 				relevance = defaultRelevance == TextProposal.R_NONE? TextProposal.R_TAG_INSERTION : defaultRelevance;
 			}
 
 			AutoContentAssistantProposal proposal = new AutoContentAssistantProposal(useAutoActivation, replacementString, 
 					replacementOffset, replacementLength, cursorPosition, image, displayString, 
 					contextInformation, additionalProposalInfo, relevance);
 
 			contentAssistRequest.addProposal(proposal);
 		}
 	}
 
 	@Override
 	protected void addAttributeValueELProposals(ContentAssistRequest contentAssistRequest,
 			CompletionProposalInvocationContext context) {
 		// No EL proposals are to be added here
 	}
 
 
 	protected void addTextELProposals(ContentAssistRequest contentAssistRequest,
 			CompletionProposalInvocationContext context) {
 		// No EL proposals are to be added here
 	}
 
 	protected ELContext createContext() {
 		return PageContextFactory.createPageContext(getResource(), PageContextFactory.XML_PAGE_CONTEXT_TYPE);
 	}
 	
 	protected KbQuery createKbQuery(Type type, String query, String stringQuery) {
 		return createKbQuery(type, query, stringQuery, getTagPrefix(), getTagUri());
 	}
 
 	protected KbQuery createKbQuery(Type type, String query, String stringQuery, String prefix, String uri) {
 		KbQuery kbQuery = new KbQuery();
 
		String[] parentTags = getParentTags(type == Type.ATTRIBUTE_NAME || type == Type.ATTRIBUTE_VALUE || type == Type.TAG_BODY);
		String	parent = getParent(type == Type.ATTRIBUTE_VALUE, type == Type.ATTRIBUTE_NAME || type == Type.TAG_BODY);
 		String queryValue = query;
 		String queryStringValue = stringQuery;
 		
 		kbQuery.setPrefix(prefix);
 		kbQuery.setUri(uri);
 		kbQuery.setParentTags(parentTags);
 		kbQuery.setParent(parent); 
 		kbQuery.setMask(true); 
 		kbQuery.setType(type);
 		kbQuery.setOffset(fCurrentContext.getInvocationOffset());
 		kbQuery.setValue(queryValue); 
 		kbQuery.setStringQuery(queryStringValue);
 		
 		return kbQuery;
 	}
 
 
 	/**
 	 * this is the position the cursor should be in after the proposal is
 	 * applied
 	 * 
 	 * @param proposedText
 	 * @return the position the cursor should be in after the proposal is
 	 *         applied
 	 */
 	protected static int getCursorPositionForProposedText(String proposedText) {
 		int cursorAdjustment;
 		cursorAdjustment = proposedText.indexOf("\"\"") + 1; //$NON-NLS-1$
 		// otherwise, after the first tag
 		if (cursorAdjustment == 0) {
 			cursorAdjustment = proposedText.indexOf('>') + 1;
 		}
 		if (cursorAdjustment == 0) {
 			cursorAdjustment = proposedText.length();
 		}
 
 		return cursorAdjustment;
 	}
 
 	/**
 	 * Returns array of the <code>org.jboss.tools.common.el.core.resolver.ELResolver</code> 
 	 * instances.
 	 * 
 	 * @param resource
 	 * @return
 	 */
 	protected ELResolver[] getELResolvers(IResource resource) {
 		if (resource == null)
 			return null;
 		
 		ELResolverFactoryManager elrfm = ELResolverFactoryManager.getInstance();
 		return elrfm.getResolvers(resource);
 	}
 	
 	/**
 	 * Returns URI for the current/parent tag
 	 * @return
 	 */
 	public String getTagUri() {
 		String nodePrefix = getTagPrefix();
 		return getUri(nodePrefix);
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
 	 * Returns EL Prefix Text Region Information Object
 	 * 
 	 * @return
 	 */
 	protected TextRegion getELPrefix(ContentAssistRequest request) {
 		if (!DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE.equals(request.getRegion().getType()) &&
 				!DOMRegionContext.XML_CONTENT.equals(request.getRegion().getType()) &&
 				!DOMRegionContext.BLOCK_TEXT.equals(request.getRegion().getType())) 
 			return null;
 		
 		String text = request.getDocumentRegion().getFullText(request.getRegion());
 		int startOffset = request.getDocumentRegion().getStartOffset() + request.getRegion().getStart();
 
 		boolean isAttributeValue = false;
 		boolean hasOpenQuote = false;
 		boolean hasCloseQuote = false;
 		char quoteChar = (char)0;
 		if (DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE.equals(request.getRegion().getType())) {
 			isAttributeValue = true;
 			if (text.startsWith("\"") || text.startsWith("'")) {//$NON-NLS-1$ //$NON-NLS-2$
 				quoteChar = text.charAt(0);
 				hasOpenQuote = true;
 			}
 			if (hasOpenQuote && text.trim().endsWith(String.valueOf(quoteChar))) {
 				hasCloseQuote = true;
 			}
 		}
 		
 		int inValueOffset = getOffset() - startOffset;
 		if (text != null && text.length() < inValueOffset) { // probably, the attribute value ends before the document position
 			return null;
 		}
 		if (inValueOffset<0) {
 			return null;
 		}
 		
 //			String matchString = text.substring(0, inValueOffset);
 		
 		ELParser p = ELParserUtil.getJbossFactory().createParser();
 		ELModel model = p.parse(text);
 		
 		ELInstance is = ELUtil.findInstance(model, inValueOffset);// ELInstance
 		ELInvocationExpression ie = ELUtil.findExpression(model, inValueOffset);// ELExpression
 		
 		boolean isELStarted = (model != null && is != null && (model.toString().startsWith("#{") ||  //$NON-NLS-1$
 				model.toString().startsWith("${"))); //$NON-NLS-1$
 		boolean isELClosed = (model != null && is != null && model.toString().endsWith("}")); //$NON-NLS-1$
 		
 //			boolean insideEL = startOffset + model.toString().length() 
 		TextRegion tr = new TextRegion(startOffset,  ie == null ? inValueOffset : ie.getStartPosition(), 
 				ie == null ? 0 : inValueOffset - ie.getStartPosition(), ie == null ? "" : ie.getText(),  //$NON-NLS-1$ 
 				isELStarted, isELClosed,
 				isAttributeValue, hasOpenQuote, hasCloseQuote, quoteChar);
 		
 		return tr;
 	}
 	
 	/**
 	 * Returns EL Predicate Text Region Information Object
 	 * 
 	 * 
 	 * @return
 	 */
 	protected TextRegion getELPredicatePrefix(ContentAssistRequest request) {
 		if (request == null || request.getRegion() == null)
 			return null;
 
 		IStructuredDocumentRegion documentRegion = request.getDocumentRegion();
 		ITextRegion completionRegion = request.getRegion();
 		String regionType = completionRegion.getType();
 		
 		if (DOMRegionContext.XML_END_TAG_OPEN.equals(regionType) || DOMRegionContext.XML_TAG_OPEN.equals(regionType)) {
 			documentRegion = documentRegion.getPrevious();
 			completionRegion = getCompletionRegion(request.getDocumentRegion().getStartOffset() + request.getRegion().getStart() - 1, request.getParent());
 		}
 		if (!DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE.equals(completionRegion.getType()) &&
 				!DOMRegionContext.XML_CONTENT.equals(completionRegion.getType()) &&
 				!DOMRegionContext.BLOCK_TEXT.equals(completionRegion.getType())) {
 				return null;
 		}
 		String text = documentRegion.getFullText(completionRegion);
 		int startOffset = documentRegion.getStartOffset() + completionRegion.getStart();
 		
 		boolean isAttributeValue = false;
 		boolean hasOpenQuote = false;
 		boolean hasCloseQuote = false;
 		char quoteChar = (char)0;
 		if (DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE.equals(request.getRegion().getType())) {
 			isAttributeValue = true;
 			if (text.startsWith("\"") || text.startsWith("'")) {//$NON-NLS-1$ //$NON-NLS-2$
 				quoteChar = text.charAt(0);
 				hasOpenQuote = true;
 			}
 			if (hasOpenQuote && text.trim().endsWith(String.valueOf(quoteChar))) {
 				hasCloseQuote = true;
 			}
 		}
 		
 		int inValueOffset = getOffset() - startOffset;
 		if (inValueOffset<0 || // There is no a word part before cursor 
 				(text != null && text.length() < inValueOffset)) { // probably, the attribute value ends before the document position
 			return null;
 		}
 
 		String matchString = getELPredicateMatchString(text, inValueOffset);
 		if (matchString == null)
 			return null;
 		
 		TextRegion tr = new TextRegion(startOffset, getOffset() - matchString.length() - startOffset, 
 				matchString.length(), matchString, false, false,
 				isAttributeValue, hasOpenQuote, hasCloseQuote, quoteChar);
 		
 		return tr;
 	}
 
 	/**
 	 * Returns predicate string for the EL-related query. 
 	 * The predicate string is the word/part of word right before the cursor position, including the '.' and '_' characters, 
 	 * which is to be replaced by the EL CA proposal ('#{' and '}' character sequences are to be inserted too)
 	 *  
 	 * @param text
 	 * @param offset
 	 * @return
 	 */
 	protected String getELPredicateMatchString(String text, int offset) {
 		int beginningOffset = offset - 1;
 		while(beginningOffset >=0 && 
 				(Character.isJavaIdentifierPart(text.charAt(beginningOffset)) ||
 						'.' == text.charAt(beginningOffset) ||
 						'_' == text.charAt(beginningOffset))) {
 			beginningOffset--;
 		}
 		beginningOffset++; // move it to point the first valid character
 		return text.substring(beginningOffset, offset);
 	}
 	
 	public static class ELXMLContentModelGenerator extends XMLContentModelGenerator {
 		
 	}
 	
 	public static class TextRegion {
 		private int startOffset;
 		private int offset;
 		private int length;
 		private String text;
 		private boolean isELStarted;
 		private boolean isELClosed;
 		private boolean isAttributeValue;
 		private boolean hasOpenQuote;
 		private boolean hasCloseQuote;
 		private char quoteChar;
 		
 		TextRegion(int startOffset, int offset, int length, String text, boolean isELStarted, boolean isELClosed) {
 			this(startOffset, offset, length, text, isELStarted, isELClosed, false, false, false, (char)0);
 		}
 
 		public TextRegion(int startOffset, int offset, int length, String text, boolean isELStarted, boolean isELClosed,
 				boolean isAttributeValue, boolean hasOpenQuote, boolean hasCloseQuote, char quoteChar) {
 			this.startOffset = startOffset;
 			this.offset = offset;
 			this.length = length;
 			this.text = text;
 			this.isELStarted = isELStarted;
 			this.isELClosed = isELClosed;
 			this.isAttributeValue = isAttributeValue;
 			this.hasOpenQuote = hasOpenQuote;
 			this.hasCloseQuote = hasCloseQuote;
 			this.quoteChar = quoteChar;
 		}
 		
 		public int getStartOffset() {
 			return startOffset;
 		}
 		
 		public int getOffset() {
 			return offset;
 		}
 		
 		public int getLength() {
 			return length;
 		}
 		
 		public String getText() {
 			StringBuffer sb = new StringBuffer(length);
 			sb = sb.append(text.substring(0, length));
 			sb.setLength(length);
 			return sb.toString();
 		}
 		
 		public boolean isELStarted() {
 			return isELStarted;
 		}
 
 		public boolean isELClosed() {
 			return isELClosed;
 		}
 
 		public boolean isAttributeValue() {
 			return isAttributeValue;
 		}
 
 		public char getQuoteChar() {
 			return quoteChar;
 		}
 
 		public boolean hasOpenQuote() {
 			return hasOpenQuote;
 		}
 
 		public boolean hasCloseQuote() {
 			return hasCloseQuote;
 		}
 	}
 
 }
 
