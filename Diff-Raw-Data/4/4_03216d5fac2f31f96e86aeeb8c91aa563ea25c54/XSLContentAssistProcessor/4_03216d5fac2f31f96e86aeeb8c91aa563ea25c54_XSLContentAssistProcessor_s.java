 /*******************************************************************************
  * Copyright (c) 2008 Standards for Technology in Automotive Retail and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     David Carver - STAR - bug 213849 - initial API and implementation
  *     David Carver - STAR - bug 230958 - refactored to fix bug with getting
  *                                        the DOM Document for the current editor
  *     
  *******************************************************************************/
 package org.eclipse.wst.xsl.ui.internal.contentassist;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.transform.TransformerException;
 
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
 import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
 import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
 import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
 import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;
 import org.eclipse.wst.xml.xpath.core.internal.parser.XPathParser;
 import org.eclipse.wst.xml.xpath.core.util.XSLTXPathHelper;
 import org.eclipse.wst.xml.xpath.ui.internal.contentassist.XPathTemplateCompletionProcessor;
 import org.eclipse.wst.xml.xpath.ui.internal.templates.TemplateContextTypeIdsXPath;
 import org.eclipse.wst.xsl.core.XSLCore;
 import org.eclipse.wst.xsl.core.internal.XSLCorePlugin;
 import org.eclipse.wst.xsl.core.internal.util.StructuredDocumentUtil;
 import org.eclipse.wst.xsl.ui.internal.XSLUIPlugin;
 import org.eclipse.wst.xsl.ui.internal.util.XSLPluginImageHelper;
 import org.eclipse.wst.xsl.ui.internal.util.XSLPluginImages;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * The XSL Content Assist Processor provides content assistance for various
  * attributes values within the XSL Editor. This includes support for xpaths on
  * select statements as well as on test and match attributes.
  * 
  * @author David Carver
  * 
  *
  * 
  */
 public class XSLContentAssistProcessor extends XMLContentAssistProcessor
 		implements IPropertyChangeListener {
 
 	private static final String ATTR_SELECT = "select"; //$NON-NLS-1$
 	private static final String ATTR_TEST = "test"; //$NON-NLS-1$
 	private static final String ATTR_MATCH = "match"; //$NON-NLS-1$
 	/**
 	 * Retrieve all global variables in the stylesheet.
 	 */
 	private static final String XPATH_GLOBAL_VARIABLES = "/xsl:stylesheet/xsl:variable"; //$NON-NLS-1$
 
 	/**
 	 * Retrieve all global parameters in the stylesheet.
 	 */
 	private static final String XPATH_GLOBAL_PARAMS = "/xsl:stylesheet/xsl:param"; //$NON-NLS-1$
 
 	/**
 	 * Limit selection of variables to those that are in the local scope.
 	 */
 	private static final String XPATH_LOCAL_VARIABLES = "ancestor::xsl:template/descendant::xsl:variable"; //$NON-NLS-1$
 
 	/**
 	 * Limit selection of params to those that are in the local scope.
 	 */
 	private static final String XPATH_LOCAL_PARAMS = "ancestor::xsl:template/descendant::xsl:param"; //$NON-NLS-1$
 
 	private XPathTemplateCompletionProcessor fTemplateProcessor = null;
 	private List<String> fTemplateContexts = new ArrayList<String>();
 	private static final byte[] XPATH_LOCK = new byte[0];
 
 	/**
 	 * The XSL Content Assist Processor handles XSL specific functionality for
 	 * content assistance. It leverages several XPath selection variables to
 	 * help with the selection of elements and template names.
 	 * 
 	 */
 	public XSLContentAssistProcessor() {
 		super();
 	}
 	
 	
 	
 	/**
 	 * TODO: Add Javadoc
 	 * 
 	 * @param textViewer
 	 * @param documentPosition
 	 * @return
 	 * 
 	 * @see org.eclipse.wst.xml.ui.contentassist.AbstractContentAssistProcessor#
 	 * 	computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
 	 */
 	@Override
 	public ICompletionProposal[] computeCompletionProposals(
 			ITextViewer textViewer, int documentPosition) {
 		fTemplateContexts.clear();
 		return super.computeCompletionProposals(textViewer, documentPosition);
 	}
 	
 
 	/**
 	 * Adds Attribute proposals based on the element and the attribute where the
 	 * content proposal was instantiated.
 	 * 
 	 * @param contentAssistRequest
 	 * 		Content Assist Request that initiated the proposal request
 	 * 
 	 */
 	@Override
 	protected void addAttributeValueProposals(ContentAssistRequest contentAssistRequest) {
 		super.addAttributeValueProposals(contentAssistRequest);
 
 		String attributeName = getAttributeName(contentAssistRequest);
 		Element rootElement = contentAssistRequest.getNode().getOwnerDocument().getDocumentElement();
 
 		if (attributeName != null) {
 			int offset = contentAssistRequest.getReplacementBeginPosition() + 1;
 
 			addAttributeValueOfProposals(contentAssistRequest, contentAssistRequest.getNode().getNamespaceURI(), rootElement, offset);
 
 			if (XSLCore.isXSLNamespace((IDOMNode)contentAssistRequest.getNode())) {
 				addSelectAndTestProposals(contentAssistRequest, attributeName, rootElement, offset);
 				addMatchProposals(contentAssistRequest, attributeName,	offset);
 			}
 		}
 	}
 
 	private void addMatchProposals(ContentAssistRequest contentAssistRequest, String attributeName, int offset) {
 		if (attributeName.equals(ATTR_MATCH)) {
 			addTemplates(contentAssistRequest, TemplateContextTypeIdsXPath.AXIS, offset);
 			addTemplates(contentAssistRequest, TemplateContextTypeIdsXPath.XPATH, offset);
 			addTemplates(contentAssistRequest, TemplateContextTypeIdsXPath.CUSTOM, offset);
 		}
 	}
 
 	private void addSelectAndTestProposals(
 			ContentAssistRequest contentAssistRequest, String attributeName, Element rootElement, int offset) {
 		if (attributeName.equals(ATTR_SELECT) || attributeName.equals(ATTR_TEST)) {
 			addGlobalProposals(rootElement, contentAssistRequest, offset);
 			addLocalProposals(contentAssistRequest.getNode(), contentAssistRequest, offset);
 			addTemplates(contentAssistRequest, TemplateContextTypeIdsXPath.AXIS, offset);
 			addTemplates(contentAssistRequest, TemplateContextTypeIdsXPath.XPATH, offset);
 			addTemplates(contentAssistRequest, TemplateContextTypeIdsXPath.CUSTOM, offset);
 		}
 	}
 
 	private void addAttributeValueOfProposals(
 			ContentAssistRequest contentAssistRequest, String namespace, Element rootElement, int offset) {
 		if (contentAssistRequest.getMatchString().contains("{")) {
 			addGlobalProposals(rootElement, contentAssistRequest, contentAssistRequest.getReplacementBeginPosition());
 			addLocalProposals(contentAssistRequest.getNode(), contentAssistRequest,
 					          contentAssistRequest.getReplacementBeginPosition());
 			addTemplates(contentAssistRequest, TemplateContextTypeIdsXPath.AXIS, offset);
 			addTemplates(contentAssistRequest, TemplateContextTypeIdsXPath.XPATH, offset);
 			addTemplates(contentAssistRequest, TemplateContextTypeIdsXPath.CUSTOM, offset);

 		}
 	}
 
 	private void addLocalProposals(Node xpathnode,
 			ContentAssistRequest contentAssistRequest, int offset) {
 		addVariablesProposals(XPATH_LOCAL_VARIABLES, xpathnode,
 				contentAssistRequest, offset);
 		addVariablesProposals(XPATH_LOCAL_PARAMS, xpathnode,
 				contentAssistRequest, offset);
 	}
 
 	private void addGlobalProposals(Node xpathnode,
 			ContentAssistRequest contentAssistRequest, int offset) {
 		addVariablesProposals(XPATH_GLOBAL_VARIABLES, xpathnode,
 				contentAssistRequest, offset);
 		addVariablesProposals(XPATH_GLOBAL_PARAMS, xpathnode,
 				contentAssistRequest, offset);
 	}
 
 	/**
 	 * Adds Parameter and Variables as proposals. This
 	 * information is selected based on the XPath statement that is sent to it
 	 * and the input Node passed. It uses a custom composer to XSL Variable
 	 * proposal.
 	 * 
 	 * @param xpath
 	 * @param xpathnode
 	 * @param contentAssistRequest
 	 * @param offset
 	 */
 	private void addVariablesProposals(String xpath, Node xpathnode,
 			ContentAssistRequest contentAssistRequest, int offset) {
 		synchronized (XPATH_LOCK) {
 			try {
 				NodeList nodes = XSLTXPathHelper.selectNodeList(xpathnode, xpath);
 				int startLength = getCursorPosition() - offset;
 
 				if (hasNodes(nodes)) {
 					for (int nodecnt = 0; nodecnt < nodes.getLength(); nodecnt++) {
 						Node node = nodes.item(nodecnt);
 						String variableName = "$" + node.getAttributes().getNamedItem("name").getNodeValue(); //$NON-NLS-1$ //$NON-NLS-2$
 
 						CustomCompletionProposal proposal = new CustomCompletionProposal(
 								variableName, offset, 0, startLength + variableName.length(),
 								XSLPluginImageHelper.getInstance().getImage(XSLPluginImages.IMG_VARIABLES),
 								variableName, null, null, 0);
 						contentAssistRequest.addProposal(proposal);
 					}
 				}
 
 			} catch (TransformerException ex) {
 				XSLUIPlugin.log(ex);
 			}
 		}
 	}
 
 	/**
 	 * Checks to make sure that the NodeList has data
 	 * @param nodes A NodeList object
 	 * @return True if has data, false if empty
 	 */
 	private boolean hasNodes(NodeList nodes) {
 		return nodes != null && nodes.getLength() > 0;
 	}
 
 	/**
 	 * Get the cursor position within the Text Viewer
 	 * @return An int value containing the cursor position
 	 */
 	private int getCursorPosition() {
 		return fTextViewer.getTextWidget().getCaretOffset();
 	}
 
 	/**
 	 * Adds XPath related templates to the list of proposals
 	 * 
 	 * @param contentAssistRequest
 	 * @param context
 	 * @param startOffset
 	 */
 	private void addTemplates(ContentAssistRequest contentAssistRequest,
 			String context, int startOffset) {
 		if (contentAssistRequest == null) {
 			return;
 		}
 
 		// if already adding template proposals for a certain context type, do
 		// not add again
 		if (!fTemplateContexts.contains(context)) {
 			fTemplateContexts.add(context);
 			boolean useProposalList = !contentAssistRequest.shouldSeparate();
 
 			if (getTemplateCompletionProcessor() != null) {
 				getTemplateCompletionProcessor().setContextType(context);
 				ICompletionProposal[] proposals = getTemplateCompletionProcessor()
 						.computeCompletionProposals(fTextViewer, startOffset);
 				for (int i = 0; i < proposals.length; ++i) {
 					if (useProposalList) {
 						contentAssistRequest.addProposal(proposals[i]);
 					} else {
 						contentAssistRequest.addMacro(proposals[i]);
 					}
 				}
 			}
 		}
 	}
 
 	private XPathTemplateCompletionProcessor getTemplateCompletionProcessor() {
 		if (fTemplateProcessor == null) {
 			fTemplateProcessor = new XPathTemplateCompletionProcessor();
 		}
 		return fTemplateProcessor;
 	}
 
 	/**
 	 * Gets the attribute name that the content assist was triggered on.
 	 * 
 	 * @param contentAssistRequest
 	 * @return
 	 */
 	private String getAttributeName(ContentAssistRequest contentAssistRequest) {
 		IStructuredDocumentRegion open = ((IDOMNode)contentAssistRequest.getNode()).getFirstStructuredDocumentRegion();
 		ITextRegionList openRegions = open.getRegions();
 		int i = openRegions.indexOf(contentAssistRequest.getRegion());
 		if (i >= 0) {
 
 			ITextRegion nameRegion = null;
 			while (i >= 0) {
 				nameRegion = openRegions.get(i--);
 				if (nameRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
 					break;
 				}
 			}
 
 			// String attributeName = nameRegion.getText();
 			return open.getText(nameRegion);
 		}
 		return null;
 	}
 
 	/**
 	 * Get the Match String.  This is typically the string-before the current
 	 * offset position.   For a standard XML Region this is calculated from the
 	 * beginning of the region (i.e. element, attribute, attribute value, etc.
 	 * For XSL, an additional check has to be made to determine if we are parsing
 	 * within an XPath region and where we are in the XPath region, as different
 	 * content assistance can be made available depending on where we are at.  This
 	 * primarily affects TEST, and SELECT attributes.
 	 * @param parent
 	 * @param aRegion
 	 * @param offset
 	 * @return
 	 */
 	@Override
 	protected String getMatchString(IStructuredDocumentRegion parent, ITextRegion aRegion, int offset) {
 		String emptyString = "";
 
 		if (isMatchStringEmpty(parent, aRegion, offset)) {
 			return emptyString; //$NON-NLS-1$
 		}
 		
 		IDOMNode currentNode = (IDOMNode) ContentAssistUtils.getNodeAt(super.fTextViewer, offset);
 		
 		IDOMAttr attributeNode = isXPathRegion(currentNode, aRegion, offset);
 		if (attributeNode != null) {
 			String temp = extractXPathMatchString(attributeNode, aRegion, offset);
 			return temp;
 		}
 		
 				
 		if (hasXMLMatchString(parent, aRegion, offset)) {
 			return extractXMLMatchString(parent, aRegion, offset);
 		}
 		// This is here for saftey reasons.
 		return emptyString;
 	}
 	
 	protected String extractXPathMatchString(IDOMAttr node, ITextRegion aRegion, int offset) {
 		if (node.getValue().length() == 0)	return "";
 		
 		int nodeOffset = node.getValueRegionStartOffset();
 		int column = offset - node.getValueRegionStartOffset();
 		XPathParser parser = new XPathParser(node.getValue());
 		int tokenStart = parser.getTokenStartOffset(1, column);
 		
 		if (tokenStart == column) {
 			return "";
 		}
 		
 		return node.getValue().substring(tokenStart - 1, column - 1);
 	}
 	
 	protected IDOMAttr isXPathRegion(IDOMNode currentNode, ITextRegion aRegion, int offset) {
 		if (XSLCore.isXSLNamespace(currentNode)) {
 			return getXPathNode(currentNode, aRegion);
 		}
 
 		return null;
 	}
 	
 	protected IDOMAttr getXPathNode(Node node, ITextRegion aRegion) {
 		if (node.hasAttributes()) {
 			if (hasAttributeAtTextRegion(ATTR_SELECT, node.getAttributes(), aRegion)) {
 				return this.getAttributeAtTextRegion(ATTR_SELECT, node.getAttributes(), aRegion);
 			}
 			
 			if (hasAttributeAtTextRegion(ATTR_TEST, node.getAttributes(), aRegion)) {
 				return this.getAttributeAtTextRegion(ATTR_TEST, node.getAttributes(), aRegion);
 			}
 			
 		}
 		return null;
 	}
 	
 	protected boolean hasAttributeAtTextRegion(String attrName, NamedNodeMap nodeMap, ITextRegion aRegion) {
 		IDOMAttr attrNode = (IDOMAttr) nodeMap.getNamedItem(attrName);
 		return attrNode != null && attrNode.getValueRegion().getStart() == aRegion.getStart();
 	}
 	
 	protected IDOMAttr getAttributeAtTextRegion(String attrName, NamedNodeMap nodeMap, ITextRegion aRegion) {
 		IDOMAttr node = (IDOMAttr) nodeMap.getNamedItem(attrName);
 		if (node != null && node.getValueRegion().getStart() == aRegion.getStart()) {
 			return node;
 		}
 		return null;
 	}
 	
 	
 	/**
 	 * An XML Match string is extracted starting from the beginning of the
 	 * region to the current offset.
 	 * @param parent
 	 * @param aRegion
 	 * @param offset
 	 * @return
 	 */
 	protected String extractXMLMatchString(IStructuredDocumentRegion parent,
 			ITextRegion aRegion, int offset) {
 		return parent.getText(aRegion).substring(0, offset - parent.getStartOffset(aRegion));
 	}
 
 	protected boolean hasXMLMatchString(IStructuredDocumentRegion parent,
 			ITextRegion aRegion, int offset) {
 		return regionHasData(parent, aRegion) && isOffsetAfterStart(parent, aRegion, offset);
 	}
 
 	protected boolean isOffsetAfterStart(IStructuredDocumentRegion parent,
 			ITextRegion aRegion, int offset) {
 		return parent.getStartOffset(aRegion) < offset;
 	}
 
 	protected boolean regionHasData(IStructuredDocumentRegion parent,
 			ITextRegion aRegion) {
 		return parent.getText(aRegion).length() > 0;
 	}
 
 	protected boolean isXMLContentRegion(String regionType) {
 		return regionType == DOMRegionContext.XML_CONTENT;
 	}
 
 	protected boolean isOffsetAfterEndOffset(IStructuredDocumentRegion parent,
 			ITextRegion aRegion, int offset) {
 		return offset > getRegionEndOffset(parent, aRegion);
 	}
 
 	protected int getRegionEndOffset(IStructuredDocumentRegion parent,
 			ITextRegion aRegion) {
 		return parent.getStartOffset(aRegion) + aRegion.getTextLength();
 	}
 
 	protected boolean isXMLTagOpen(String regionType) {
 		return regionType == DOMRegionContext.XML_TAG_OPEN;
 	}
 
 	protected boolean isAttributeEqualsRegion(String regionType) {
 		return regionType == DOMRegionContext.XML_TAG_ATTRIBUTE_EQUALS;
 	}
 
 	protected boolean isMatchStringEmpty(IStructuredDocumentRegion parent, ITextRegion aRegion, int offset) {
 		return isRegionNull(aRegion) ||
 		       isCloseRegion(aRegion) ||
 		       isAttributeEqualsRegion(aRegion.getType()) ||
 		       isXMLTagOpen(aRegion.getType()) ||
 		       isOffsetAfterEndOffset(parent, aRegion, offset) ||
 		       isXMLContentRegion(aRegion.getType());
 	}
 	
 	protected boolean isRegionNull(ITextRegion aRegion) {
 		return aRegion == null;
 	}	
 
 	@Override
 	protected ContentAssistRequest computeAttributeValueProposals(int documentPosition, String matchString, ITextRegion completionRegion, IDOMNode nodeAtOffset, IDOMNode node) {
 		ContentAssistRequest contentAssistRequest = null;
 		IStructuredDocumentRegion sdRegion = getStructuredDocumentRegion(documentPosition);
 		if ((documentPosition > sdRegion.getStartOffset(completionRegion) + completionRegion.getTextLength()) && (sdRegion.getStartOffset(completionRegion) + completionRegion.getTextLength() != sdRegion.getStartOffset(completionRegion) + completionRegion.getLength())) {
 			// setup to add a new attribute at the documentPosition
 			IDOMNode actualNode = (IDOMNode) node.getModel().getIndexedRegion(sdRegion.getStartOffset(completionRegion));
 			contentAssistRequest = newContentAssistRequest(actualNode, actualNode, sdRegion, completionRegion, documentPosition, 0, matchString);
 			addAttributeNameProposals(contentAssistRequest);
 			if ((actualNode.getFirstStructuredDocumentRegion() != null) && !actualNode.getFirstStructuredDocumentRegion().isEnded()) {
 				addTagCloseProposals(contentAssistRequest);
 			}
 		}
 		else {
 			// setup to replace the existing value
 			if (!nodeAtOffset.getFirstStructuredDocumentRegion().isEnded() && (documentPosition < sdRegion.getStartOffset(completionRegion))) {
 				// if the IStructuredDocumentRegion isn't closed and the
 				// cursor is in front of the value, add
 				contentAssistRequest = newContentAssistRequest(nodeAtOffset, node, sdRegion, completionRegion, documentPosition, 0, matchString);
 				addAttributeNameProposals(contentAssistRequest);
 			}
 			else {
 				IDOMAttr xpathNode = this.isXPathRegion(nodeAtOffset, completionRegion, documentPosition);
 				if (xpathNode != null) {
 					// This needs to setup the content assistance correctly. Here is what needs to happen:
 					// 1. Adjust the matchString (This should have been calculated earlier) 
 					// 2. Get the current tokens offset position..this will be the starting offset.
 					// 3. Get the replacement length...this is the difference between the token offset and the next token or end of the string
 					XPathParser parser = new XPathParser(xpathNode.getValue());
 					int startOffset = xpathNode.getValueRegionStartOffset() + parser.getTokenStartOffset(1, documentPosition - xpathNode.getValueRegionStartOffset()) - 1;
 					int replacementLength = documentPosition - startOffset;
 					contentAssistRequest = newContentAssistRequest(nodeAtOffset, node, sdRegion, completionRegion, startOffset, replacementLength, matchString);
 				} else {
 					contentAssistRequest = newContentAssistRequest(nodeAtOffset, node, sdRegion, completionRegion, sdRegion.getStartOffset(completionRegion), completionRegion.getTextLength(), matchString);
 				}
 				
 				addAttributeValueProposals(contentAssistRequest);
 			}
 		}
 		return contentAssistRequest;
 	}
 
 }
