 /******************************************************************************* 
 * Copyright (c) 2010-2012 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.jst.jsp.jspeditor.info;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.filebuffers.FileBuffers;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jface.text.DefaultInformationControl;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IInformationControl;
 import org.eclipse.jface.text.IInformationControlCreator;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.TextPresentation;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
 import org.eclipse.wst.sse.ui.internal.derived.HTML2TextReader;
 import org.eclipse.wst.sse.ui.internal.derived.HTMLTextPresenter;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
 import org.eclipse.wst.xml.ui.internal.taginfo.XMLTagInfoHoverProcessor;
 import org.jboss.tools.common.el.core.model.ELInstance;
 import org.jboss.tools.common.el.core.model.ELInvocationExpression;
 import org.jboss.tools.common.el.core.model.ELModel;
 import org.jboss.tools.common.el.core.model.ELUtil;
 import org.jboss.tools.common.el.core.parser.ELParser;
 import org.jboss.tools.common.el.core.parser.ELParserUtil;
 import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.el.core.resolver.ELResolution;
 import org.jboss.tools.common.el.core.resolver.ELResolver;
 import org.jboss.tools.common.el.core.resolver.ELSegment;
 import org.jboss.tools.common.el.core.resolver.JavaMemberELSegmentImpl;
 import org.jboss.tools.common.el.ui.ca.ELProposalProcessor;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.text.TextProposal;
 import org.jboss.tools.common.util.StringUtil;
 import org.jboss.tools.jst.jsp.contentassist.Utils;
 import org.jboss.tools.jst.jsp.contentassist.computers.AbstractXmlCompletionProposalComputer.TextRegion;
 import org.jboss.tools.jst.web.kb.IPageContext;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.KbQuery.Type;
 import org.jboss.tools.jst.web.kb.PageContextFactory;
 import org.jboss.tools.jst.web.kb.PageProcessor;
 import org.jboss.tools.jst.web.kb.el.MessagePropertyELSegmentImpl;
 import org.jboss.tools.jst.web.kb.taglib.INameSpace;
 import org.w3c.dom.Node;
 
 /**
  * 
  * @author Victor Rubezhny
  *
  */
 @SuppressWarnings("restriction")
 public class FaceletTagInfoHoverProcessor extends XMLTagInfoHoverProcessor {
 	private ELContext fContext;
 	private int fDocumentPosition;
 
 	public FaceletTagInfoHoverProcessor() {
 		super();
 	}
 
 	@Override
 	protected String computeHoverHelp(ITextViewer textViewer,
 			int documentPosition) {
 		this.fDocumentPosition = documentPosition;
 		this.fContext = null;
 		
 		fContext = PageContextFactory.createPageContext(textViewer.getDocument());
 		if (fContext == null)
 			return null;
 		
 		IStructuredDocumentRegion flatNode = ((IStructuredDocument) textViewer.getDocument()).getRegionAtCharacterOffset(fDocumentPosition);
 		ITextRegion region = null;
 
 		if (flatNode != null) {
 			region = flatNode.getRegionAtCharacterOffset(fDocumentPosition);
 		}
 		
 		String hoverHelp = null;
 		if (region != null) {
 			TextRegion elPrefix = getELPrefix(flatNode, region, fDocumentPosition);
 			ELInvocationExpression elOperand = getELExpression(flatNode, region, fDocumentPosition);
			if (elPrefix != null && elPrefix.isELStarted() && elOperand != null) {
 				IndexedRegion treeNode = ContentAssistUtils.getNodeAt(textViewer, fDocumentPosition);
 				if (treeNode == null) {
 					return null;
 				}
 				Node node = (Node) treeNode;
 
 				while ((node != null) && (node.getNodeType() == Node.TEXT_NODE) && (node.getParentNode() != null)) {
 					node = node.getParentNode();
 				}
 				return  computeELHelp((IDOMNode) treeNode, (IDOMNode) node, flatNode, region, elOperand);
 			}
 		}
 		return hoverHelp != null ? hoverHelp : super.computeHoverHelp(textViewer, documentPosition);
 	}
 
 	@Override
 	protected String computeTagAttNameHelp(IDOMNode xmlnode,
 			IDOMNode parentNode, IStructuredDocumentRegion flatNode,
 			ITextRegion region) {
 		if (fContext == null)
 			return null;
 
 		String tagName = Utils.getTagName(xmlnode, true);
 		String query = flatNode.getText(region);
 		String prefix = getPrefix(tagName);
 		String uri = getUri(prefix);
 		String[] parentTags = Utils.getParentTags(xmlnode, true, true);
 		String parent = Utils.getParent(xmlnode, true, true, true);
 		
 		KbQuery kbQuery = Utils.createKbQuery(Type.ATTRIBUTE_NAME, fDocumentPosition, query, query,
 				prefix, uri, parentTags, parent, false);
 
 		TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, fContext);
 		if (proposals == null)
 			return null;
 		
 		for(TextProposal proposal : proposals) {
 			if (proposal != null && proposal.getContextInfo() != null &&
 				proposal.getContextInfo().trim().length() > 0) {
 				return proposal.getContextInfo();
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	protected String computeTagAttValueHelp(IDOMNode xmlnode,
 			IDOMNode parentNode, IStructuredDocumentRegion flatNode,
 			ITextRegion region) {
 		return null;
 	}
 
 	@Override
 	protected String computeTagNameHelp(IDOMNode xmlnode, IDOMNode parentNode,
 			IStructuredDocumentRegion flatNode, ITextRegion region) {
 		if (fContext == null)
 			return null;
 
 		String query = Utils.getTagName(xmlnode, true);
 		String prefix = getPrefix(query);
 		String uri = getUri(prefix);
 		String[] parentTags = Utils.getParentTags(xmlnode, false, true);
 		String parent = Utils.getParent(xmlnode, false, false, true);
 		
 		KbQuery kbQuery = Utils.createKbQuery(Type.TAG_NAME, fDocumentPosition, query, "<" + query,  //$NON-NLS-1$
 				prefix, uri, parentTags, parent, false);
 
 		TextProposal[] proposals = PageProcessor.getInstance().getProposals(kbQuery, fContext);
 		if (proposals == null)
 			return null;
 		
 		for(TextProposal proposal : proposals) {
 			if (proposal != null && proposal.getContextInfo() != null &&
 				proposal.getContextInfo().trim().length() > 0) {
 				return proposal.getContextInfo();
 			}
 		}
 
 		return null;
 	}
 	
 	
 	protected String computeELHelp(IDOMNode xmlnode, IDOMNode parentNode,
 			IStructuredDocumentRegion flatNode, ITextRegion region, ELInvocationExpression elOperand) {
 		if (fContext == null)
 			return null;
 
 		ELResolver[] resolvers =  fContext.getElResolvers();
 		
 		for (int i = 0; resolvers != null && i < resolvers.length; i++) {
 			ELResolution resolution = resolvers[i] == null ? null : resolvers[i].resolve(fContext, elOperand, fDocumentPosition);
 			if (resolution == null || !resolution.isResolved())
 				continue;
 			
 			ELSegment segment = resolution.getLastSegment();
 			if(segment instanceof JavaMemberELSegmentImpl) {
 				JavaMemberELSegmentImpl jmSegment = (JavaMemberELSegmentImpl)segment;
 				
 				IJavaElement[] javaElements = jmSegment.getAllJavaElements();
 				if (javaElements == null || javaElements.length == 0) {
 					if (jmSegment.getJavaElement() == null)
 						continue;
 					
 					javaElements = new IJavaElement[] {jmSegment.getJavaElement()};
 				}
 				if (javaElements == null || javaElements.length == 0)
 					continue;
 				
 				ELInfoHoverBrowserInformationControlInput hover = JavaStringELInfoHover.getHoverInfo2Internal(javaElements, false);
 				return (hover == null ? null : hover.getHtml());
 			} else if (segment instanceof MessagePropertyELSegmentImpl) {
 				MessagePropertyELSegmentImpl mpSegment = (MessagePropertyELSegmentImpl)segment;
 				String baseName = mpSegment.getBaseName();
 				String propertyName = mpSegment.isBundle() ? null : StringUtil.trimQuotes(segment.getToken().getText());
 				
 				return ELProposalProcessor.getELMessagesHoverInternal(baseName, propertyName, (List<XModelObject>)mpSegment.getObjects());
 			}
 		}
 		
 		return null;
 	}
  
 	/**
 	 * Returns the region to hover the text over based on the offset.
 	 * Overrides the base method enabling the TEXT regions to the supported
 	 * 
 	 * @param textViewer
 	 * @param offset
 	 * 
 	 * @return IRegion region to hover over if offset is within tag name,
 	 *         attribute name, or attribute value and if offset is not over
 	 *         invalid whitespace. otherwise, returns <code>null</code>
 	 * 
 	 * @see org.eclipse.jface.text.ITextHover#getHoverRegion(ITextViewer, int)
 	 */
 	@Override
 	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
 		if ((textViewer == null) || (textViewer.getDocument() == null)) {
 			return null;
 		}
 		
 		IStructuredDocumentRegion flatNode = ((IStructuredDocument) textViewer.getDocument()).getRegionAtCharacterOffset(offset);
 		ITextRegion region = null;
 
 		if (flatNode != null) {
 			region = flatNode.getRegionAtCharacterOffset(offset);
 		}
 		
 		if (region != null) {
 			// Supply hoverhelp for text 
 			String regionType = region.getType();
 			if (DOMRegionContext.XML_CONTENT.equals(regionType) ||
 					DOMRegionContext.BLOCK_TEXT.equals(regionType) ||
 					DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE.equals(regionType)) {
 				TextRegion elPrefix = getELPrefix(flatNode, region, offset);
 				if (elPrefix != null && elPrefix.isELStarted()) {
 					return new Region(elPrefix.getStartOffset() + elPrefix.getOffset() + elPrefix.getLength(), 0);
 				}
 			}
 		}
 		return super.getHoverRegion(textViewer, offset);
 	}
 	/**
 	 * Returns IFile resource of the document
 	 * 
 	 * @return
 	 */
 	protected IFile getResource(IDocument document) {
 		IStructuredModel sModel = StructuredModelManager.getModelManager().getExistingModelForRead(document);
 		try {
 			if (sModel != null) {
 				String baseLocation = sModel.getBaseLocation();
 				IPath location = new Path(baseLocation).makeAbsolute();
 				return FileBuffers.getWorkspaceFileAtLocation(location);
 			}
 		}
 		finally {
 			if (sModel != null) {
 				sModel.releaseFromRead();
 			}
 		}
 		return null;
 	}
 
 	
 	private String getPrefix(String tagname) {
 		String prefix = null;
 		
 		int index = tagname == null ? -1 : tagname.indexOf(':');
 		if (tagname != null && index != -1) {
 			prefix = tagname.substring(0, index);
 		}
 		
 		if (prefix != null)
 			return prefix;
 
 		String uri = getUri(""); //$NON-NLS-1$
 		return uri == null ? null : "";   //$NON-NLS-1$
 	}
 	
 	private String getUri(String prefix) {
 		if (prefix == null || fContext == null)
 			return null;
 		if (!(fContext instanceof IPageContext))
 			return null;
 		
 		Map<String, List<INameSpace>> nameSpaces = ((IPageContext)fContext).getNameSpaces(fDocumentPosition);
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
 	
 	/**
 	 * Returns EL Prefix Text Region Information Object
 	 * 
 	 * @return
 	 */
 	private TextRegion getELPrefix(IStructuredDocumentRegion sdRegion, ITextRegion region, int offset) {
 		if (sdRegion == null || region == null)
 			return null;
 
 		String text = sdRegion.getFullText(region);
 		int startOffset = sdRegion.getStartOffset() + region.getStart();
 		
 		boolean isAttributeValue = false;
 		boolean hasOpenQuote = false;
 		boolean hasCloseQuote = false;
 		char quoteChar = (char)0;
 		if (DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE.equals(region.getType())) {
 			isAttributeValue = true;
 			if (text.startsWith("\"") || text.startsWith("'")) {//$NON-NLS-1$ //$NON-NLS-2$
 				quoteChar = text.charAt(0);
 				hasOpenQuote = true;
 			}
 			if (hasOpenQuote && text.endsWith(String.valueOf(quoteChar))) {
 				hasCloseQuote = true;
 			}
 		}
 		
 		int inValueOffset = offset - startOffset;
 		if (text != null && text.length() < inValueOffset) { // probably, the attribute value ends before the document position
 			return null;
 		}
 		if (inValueOffset<0) {
 			return null;
 		}
 		
 		ELParser p = ELParserUtil.getJbossFactory().createParser();
 		ELModel model = p.parse(text);
 		
 		ELInstance is = ELUtil.findInstance(model, inValueOffset);// ELInstance
 		ELInvocationExpression ie = ELUtil.findExpression(model, inValueOffset);// ELExpression
 		
 		boolean isELStarted = (model != null && is != null && (model.toString().startsWith("#{") ||  //$NON-NLS-1$
 				model.toString().startsWith("${"))); //$NON-NLS-1$
 		boolean isELClosed = (model != null && is != null && model.toString().endsWith("}")); //$NON-NLS-1$
 		
 		TextRegion tr = new TextRegion(startOffset,  ie == null ? inValueOffset : ie.getStartPosition(), 
 				ie == null ? 0 : ie.getLength(), ie == null ? "" : ie.getText(),  //$NON-NLS-1$ 
 				isELStarted, isELClosed,
 				isAttributeValue, hasOpenQuote, hasCloseQuote, quoteChar);
 		
 		return tr;
 	}
 
 	/**
 	 * Returns EL Prefix Text Region Information Object
 	 * 
 	 * @return
 	 */
 	private ELInvocationExpression getELExpression(IStructuredDocumentRegion sdRegion, ITextRegion region, int offset) {
 		if (sdRegion == null || region == null)
 			return null;
 
 		String text = sdRegion.getFullText(region);
 		int startOffset = sdRegion.getStartOffset() + region.getStart();
 		
 		int inValueOffset = offset - startOffset;
 		if (text != null && text.length() < inValueOffset) { // probably, the attribute value ends before the document position
 			return null;
 		}
 		if (inValueOffset<0) {
 			return null;
 		}
 		
 		ELParser p = ELParserUtil.getJbossFactory().createParser();
 		ELModel model = p.parse(text);
 		
 		ELInvocationExpression ie = ELUtil.findExpression(model, inValueOffset);// ELExpression
 		
 		return ie;
 	}
 	
 	private static final String EMPTY_STRING= ""; //$NON-NLS-1$
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
 	 */
 	public IInformationControlCreator getHoverControlCreator() {
 		return new IInformationControlCreator() {
 			public IInformationControl createInformationControl(Shell parent) {
 				return new DefaultInformationControl(parent, new HTMLTextPresenter(true) {
 
 					@Override
 					protected Reader createReader(String hoverInfo,
 							TextPresentation presentation) {
 						return new HTML2TextReader(new StringReader(hoverInfo), presentation) {
 							/*
 							 * @see org.eclipse.jdt.internal.ui.text.SubstitutionTextReader#computeSubstitution(int)
 							 */
 							protected String computeSubstitution(int c) throws IOException {
 								String substitution = super.computeSubstitution(c);
 								if (substitution != null && substitution.length() > 0) {
 									// This cuts off all The tags from the text
 									if (substitution.startsWith("<") && substitution.endsWith(">")) { //$NON-NLS-1$ //$NON-NLS-2$
 										return EMPTY_STRING;
 									}
 								}
 								return substitution;
 							}
 						};
 					}
 				});
 			}
 		};
 	}
 }
