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
 package org.jboss.tools.jst.jsp.contentassist;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
 import org.eclipse.jst.jsp.core.internal.contentmodel.TaglibController;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TLDCMDocumentManager;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TaglibTracker;
 import org.eclipse.jst.jsp.core.internal.regions.DOMJSPRegionContexts;
 import org.eclipse.jst.jsp.core.text.IJSPPartitions;
 import org.eclipse.jst.jsp.ui.internal.JSPUIPlugin;
 import org.eclipse.jst.jsp.ui.internal.contentassist.JSPContentAssistProcessor;
 import org.eclipse.jst.jsp.ui.internal.preferences.JSPUIPreferenceNames;
 import org.eclipse.jst.jsp.ui.internal.templates.TemplateContextTypeIdsJSP;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.wst.css.ui.internal.contentassist.CSSContentAssistProcessor;
 import org.eclipse.wst.html.core.text.IHTMLPartitions;
 import org.eclipse.wst.javascript.ui.internal.common.contentassist.JavaScriptContentAssistProcessor;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionContainer;
 import org.eclipse.wst.sse.core.utils.StringUtils;
 import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
 import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
 import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMDataType;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;
 import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.ModelQueryAction;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
 import org.eclipse.wst.xml.ui.internal.XMLUIMessages;
 import org.eclipse.wst.xml.ui.internal.XMLUIPlugin;
 import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
 import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistUtilities;
 import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentModelGenerator;
 import org.eclipse.wst.xml.ui.internal.contentassist.XMLRelevanceConstants;
 import org.eclipse.wst.xml.ui.internal.editor.CMImageUtil;
 import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImageHelper;
 import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImages;
 import org.eclipse.wst.xml.ui.internal.preferences.XMLUIPreferenceNames;
 import org.jboss.tools.common.kb.KbConnectorFactory;
 import org.jboss.tools.common.kb.KbConnectorType;
 import org.jboss.tools.common.kb.KbException;
 import org.jboss.tools.common.kb.KbProposal;
 import org.jboss.tools.common.kb.KbTldResource;
 import org.jboss.tools.common.kb.TagDescriptor;
 import org.jboss.tools.common.kb.wtp.TLDVersionHelper;
 import org.jboss.tools.common.kb.wtp.WtpKbConnector;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.jboss.tools.jst.web.project.WebProject;
 import org.jboss.tools.jst.web.tld.TaglibData;
 import org.jboss.tools.jst.web.tld.VpeTaglibListener;
 import org.jboss.tools.jst.web.tld.VpeTaglibManager;
 import org.jboss.tools.jst.web.tld.VpeTaglibManagerProvider;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /**
  * @author Igels
  */
 public class RedHatJSPContentAssistProcessor extends JSPContentAssistProcessor{
 
     private JSPActiveContentAssistProcessor jspActiveCAP;
     private WtpKbConnector wtpKbConnector;
     private IDocument document;
     private boolean dontOpenTag = false;
 
 
 	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentPosition) {
 	    try {
 	        String text = viewer.getDocument().get(0, documentPosition);
 	        int lastOpenTag = text.lastIndexOf('<');
 	        int lastCloseTag = text.lastIndexOf('>');
 	        dontOpenTag = lastCloseTag<lastOpenTag;
 	    } catch (Exception e) {
 	    	JspEditorPlugin.getPluginLog().logError(e);
 	    }
 
 		document = viewer.getDocument();
     	//added by Max Areshkau JBIDE-788
 		updateActiveContentAssistProcessor(document);
 		ICompletionProposal[] proposals = super.computeCompletionProposals(viewer, documentPosition);
 		// If proposal list from super is empty to try to get it from Red Hat dinamic jsp content assist processor.
 		try {
 			if(proposals.length == 0) {
 				String partitionType = getPartitionType((StructuredTextViewer) viewer, documentPosition);
 				IContentAssistProcessor p = (IContentAssistProcessor) fPartitionToProcessorMap.get(partitionType);
 				if (!(p instanceof JavaScriptContentAssistProcessor || p instanceof CSSContentAssistProcessor)) {
 
 					IndexedRegion treeNode = ContentAssistUtils.getNodeAt((StructuredTextViewer) viewer, documentPosition);
 					Node node = (Node) treeNode;
 					
 					while (node != null && node.getNodeType() == Node.TEXT_NODE && node.getParentNode() != null)
 						node = node.getParentNode();
 					IDOMNode xmlnode = (IDOMNode) node;
 					if(xmlnode!=null) {
 						fTextViewer = viewer;
 						IStructuredDocumentRegion sdRegion = getStructuredDocumentRegion(documentPosition);
 						ITextRegion completionRegion = getCompletionRegion(documentPosition, node);
 						if(completionRegion!=null) {
 							String matchString = getMatchString(sdRegion, completionRegion, documentPosition);
 							ContentAssistRequest contentAssistRequest = computeCompletionProposals(documentPosition, matchString, completionRegion, (IDOMNode) treeNode, xmlnode);
 							if(contentAssistRequest!=null) {
 								proposals = contentAssistRequest.getCompletionProposals();
 							}
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		}
 		proposals = getUniqProposals(proposals);
 		return proposals;
 	}
 
 	public static ICompletionProposal[] getUniqProposals(ICompletionProposal[] proposals) {
 		ArrayList uniqProposals = new ArrayList(proposals.length);
 		HashSet displayStrings = new HashSet(proposals.length);
 		for(int i=0; i<proposals.length; i++) {
 			String str = proposals[i].getDisplayString();
 			if(str==null || !displayStrings.contains(str)) {
 				displayStrings.add(str);
 				uniqProposals.add(proposals[i]);
 			}
 		}
 		return (ICompletionProposal[])uniqProposals.toArray(new ICompletionProposal[uniqProposals.size()]);
 	}
 
 	/**
 	 * int map that points [partition > processor]. This takes place of
 	 * embedded adapters for now.
 	 */
 	protected void initPartitionToProcessorMap() {
 		super.initPartitionToProcessorMap();
 	}
 
 	public char[] getCompletionProposalAutoActivationCharacters() {
 		char[] autoActivChars = null;
 		char[] superAutoActivChars = super.getCompletionProposalAutoActivationCharacters();
 		if(superAutoActivChars==null) {
 			return superAutoActivChars;
 		}
 
 		autoActivChars = superAutoActivChars;
 		IPreferenceStore store = JSPUIPlugin.getDefault().getPreferenceStore();
 		if(store.isDefault(JSPUIPreferenceNames.AUTO_PROPOSE_CODE)) {
 //			String superDefaultChars = store.getDefaultString(JSPUIPreferenceNames.AUTO_PROPOSE_CODE);
 			StringBuffer redhatDefaultChars = new StringBuffer(new String(superAutoActivChars));
 			if(redhatDefaultChars.indexOf(".")<0) {
 				redhatDefaultChars.append('.');
 				store.setDefault(JSPUIPreferenceNames.AUTO_PROPOSE_CODE, redhatDefaultChars.toString());
 				store.setValue(JSPUIPreferenceNames.AUTO_PROPOSE_CODE, redhatDefaultChars.toString());
 			}
 			autoActivChars = new char[redhatDefaultChars.length()];
 			redhatDefaultChars.getChars(0, redhatDefaultChars.length(), autoActivChars, 0);
 		}
 
 		return autoActivChars;
 	}
 
 	public void updateActiveContentAssistProcessor(IDocument document) {
 			TLDCMDocumentManager manager = TaglibController.getTLDCMDocumentManager(document);
 			if (manager != null) {
 				List list = manager.getTaglibTrackers();
 				for(int i=0; i<list.size(); i++) {
 					TaglibTracker tracker = (TaglibTracker)list.get(i);
 
 					String version = TLDVersionHelper.getTldVersion(tracker);
 					KbTldResource resource = new KbTldResource(tracker.getURI(), "", tracker.getPrefix(), version);
 			        getWtpKbConnector().registerResource(resource);
 			        addActiveContentAssistProcessorToProcessorMap(tracker.getURI(), tracker.getPrefix(), version);
 			}
 		}
 	} 
 
     private void addActiveContentAssistProcessorToProcessorMap(String uri, String prefix, String version) {
         try {
             List names = getWtpKbConnector().getAllTagNamesFromTldByUri(uri, version);
             for(Iterator iter = names.iterator(); iter.hasNext();) {
                 String fullName = prefix + ":" + iter.next();
            		fNameToProcessorMap.put(fullName, jspActiveCAP);
 			}
         } catch (KbException e) {
         	JspEditorPlugin.getPluginLog().logError(e);
         }
     }
 
 	private WtpKbConnector getWtpKbConnector() {
 	    if(wtpKbConnector == null && document != null) {
 	        try {
                 wtpKbConnector = (WtpKbConnector)KbConnectorFactory.getIntstance().createConnector(KbConnectorType.JSP_WTP_KB_CONNECTOR, document);
                 jspActiveCAP.setKbConnector(wtpKbConnector);
             } catch (ClassNotFoundException e) {
             	JspEditorPlugin.getPluginLog().logError(e);
             } catch (InstantiationException e) {
             	JspEditorPlugin.getPluginLog().logError(e);
             } catch (IllegalAccessException e) {
             	JspEditorPlugin.getPluginLog().logError(e);
             }
 	    }
 	    return wtpKbConnector;
 	}
 
 	protected void init() {
 	    super.init();
 		jspActiveCAP = new JSPActiveContentAssistProcessor();
 		jspActiveCAP.init();
 	}
 
 	public XMLContentModelGenerator getContentGenerator() {
 		if (fGenerator == null){
 			fGenerator = new XMLContentModelGenerator(){
 				public void generateTag(Node parent, CMElementDeclaration elementDecl, StringBuffer buffer) {
 					if (elementDecl == null || buffer == null) {
 						return;
 					}
 					try {
 					    String tagName = getRequiredName(parent, elementDecl);
 						TagDescriptor info = getWtpKbConnector().getTagInformation("/" + tagName + "/");
 						if (info != null) {
 							KbProposal proposal = info.generateProposal();
 							String replString = proposal.getReplacementString();
 							boolean hasBody = info.hasBody();
 
 							if (replString != null && replString.length() > 0) {
 							    if(!dontOpenTag) {
 							        buffer.append("<");
 							    }
 								buffer.append(replString);
 								buffer.append(">");
 								if (hasBody) {
 									buffer.append("</");
 									buffer.append(tagName);
 									buffer.append(">");
 								}
 								return;
 							}
 						}
 					} catch (Exception x) {
 						JspEditorPlugin.getPluginLog().logError("", x);
 					}
 					super.generateTag(parent, elementDecl, buffer);
 					return;
 				}
 			};
 		}
 		return fGenerator;
 	}
 
 	protected void addTagInsertionProposals(ContentAssistRequest contentAssistRequest, int childPosition) {
 		List cmnodes = null;
 		Node parent = contentAssistRequest.getParent();
 		String error = null;
 
 		if (parent != null && parent.getNodeType() == Node.DOCUMENT_NODE && ((IDOMDocument) parent).isXMLType() && !isCursorAfterXMLPI(contentAssistRequest)) {
 			return;
 		}
 		// only want proposals if cursor is after doctype...
 		if (!isCursorAfterDoctype(contentAssistRequest))
 			return;
 
 		if (parent != null && parent instanceof IDOMNode && isCommentNode((IDOMNode) parent)) {
 			// loop and find non comment node?
 			while (parent != null && isCommentNode((IDOMNode) parent)) {
 				parent = parent.getParentNode();
 			}
 		}
 
 		if (parent.getNodeType() == Node.ELEMENT_NODE) {
 			CMElementDeclaration parentDecl = getCMElementDeclaration(parent);
 			if (parentDecl != null) {
 				// XSD-specific ability - no filtering
 				CMDataType childType = parentDecl.getDataType();
 				if (childType != null) {
 					String[] childStrings = childType.getEnumeratedValues();
 					if (childStrings != null) {
 						// the content string is the sole valid child...so
 						// replace the rest
 						int begin = contentAssistRequest.getReplacementBeginPosition();
 						int length = contentAssistRequest.getReplacementLength();
 						if (parent instanceof IDOMNode) {
 							if (((IDOMNode) parent).getLastStructuredDocumentRegion() != ((IDOMNode) parent).getFirstStructuredDocumentRegion()) {
 								begin = ((IDOMNode) parent).getFirstStructuredDocumentRegion().getEndOffset();
 								length = ((IDOMNode) parent).getLastStructuredDocumentRegion().getStartOffset() - begin;
 							}
 						}
 						String proposedInfo = getAdditionalInfo(parentDecl, childType);
 						for (int i = 0; i < childStrings.length; i++) {
 							CustomCompletionProposal textProposal = new RedHatCustomCompletionProposal(childStrings[i].indexOf("=")>-1, childStrings[i], begin, length, childStrings[i].length(), XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_ENUM), childStrings[i], null, proposedInfo, XMLRelevanceConstants.R_TAG_INSERTION);
 							contentAssistRequest.addProposal(textProposal);
 						}
 					}
 				}
 			}
 			if (parentDecl != null && parentDecl.getContentType() == CMElementDeclaration.PCDATA) {
 				addPCDATAProposal(parentDecl.getNodeName(), contentAssistRequest);
 			}
 			else {
 				// retrieve the list of all possible children within this parent context
 				cmnodes = getAvailableChildElementDeclarations((Element)parent, childPosition, ModelQueryAction.INSERT);
                 
                 // retrieve the list of the possible children within this parent context and at this index
                 List strictCMNodeSuggestions = null;
                 if (XMLUIPreferenceNames.SUGGESTION_STRATEGY_VALUE_STRICT.equals(XMLUIPlugin.getInstance().getPreferenceStore().getString(XMLUIPreferenceNames.SUGGESTION_STRATEGY))) 
                 { 
                   strictCMNodeSuggestions = getValidChildElementDeclarations((Element)parent, childPosition, ModelQueryAction.INSERT);                
                 }
  				Iterator nodeIterator = cmnodes.iterator();
 				if (!nodeIterator.hasNext()) {
 					if (getCMElementDeclaration(parent) != null)
 						error = NLS.bind(XMLUIMessages._Has_no_available_child, (new Object[]{parent.getNodeName()}));
 					else
 						error = NLS.bind(XMLUIMessages.Element__is_unknown, (new Object[]{parent.getNodeName()}));
 				}
 				String matchString = contentAssistRequest.getMatchString();
 				// chop off any leading <'s and whitespace from the
 				// matchstring
 				while ((matchString.length() > 0) && (Character.isWhitespace(matchString.charAt(0)) || beginsWith(matchString, "<"))) //$NON-NLS-1$
 					//$NON-NLS-1$
 					matchString = matchString.substring(1);
 				while (nodeIterator.hasNext()) {
 					Object o = nodeIterator.next();
 					if (o instanceof CMElementDeclaration) {
 						CMElementDeclaration elementDecl = (CMElementDeclaration) o;
 						// only add proposals for the child element's that
 						// begin with the matchstring
 						String tagname = getRequiredName(parent, elementDecl);
                         boolean isStrictCMNodeSuggestion = strictCMNodeSuggestions != null ? strictCMNodeSuggestions.contains(elementDecl) : false;
                         
                         Image image = CMImageUtil.getImage(elementDecl);
   
 						if (image == null) {
                             if (strictCMNodeSuggestions != null) {
                                 image = isStrictCMNodeSuggestion ?                                 
 							            XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_TAG_GENERIC_EMPHASIZED) :
                                         XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_TAG_GENERIC_DEEMPHASIZED);                                                                                        
                             }
                             else {
                                 image = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_TAG_GENERIC);
                             }
                               
 						}
 						// Account for the &lt; and &gt;. If attributes were
 						// added, the cursor will be placed
 						// at the offset before of the first character of the
 						// first attribute name.
 //						int markupAdjustment = getContentGenerator().getMinimalStartTagLength(parent, elementDecl);
 						if (beginsWith(tagname, matchString)) {
 							String proposedText = getRequiredText(parent, elementDecl);
 							
 							int markupAdjustment = getCursorPositionForProposedText(proposedText);
 
 							String proposedInfo = getAdditionalInfo(parentDecl, elementDecl);
 							int relevance = isStrictCMNodeSuggestion ? XMLRelevanceConstants.R_STRICTLY_VALID_TAG_INSERTION : XMLRelevanceConstants.R_TAG_INSERTION;
 							CustomCompletionProposal proposal = new RedHatCustomCompletionProposal(true, proposedText, contentAssistRequest.getReplacementBeginPosition(), contentAssistRequest.getReplacementLength(), markupAdjustment, image, tagname, null, proposedInfo, relevance);
 							contentAssistRequest.addProposal(proposal);
 						}
 					}
 				}
 				if (contentAssistRequest.getProposals().size() == 0) {
 					if (error != null)
 						setErrorMessage(error);
 					else if (contentAssistRequest.getMatchString() != null && contentAssistRequest.getMatchString().length() > 0)
 						setErrorMessage(NLS.bind(XMLUIMessages.No_known_child_tag, (new Object[]{parent.getNodeName(), contentAssistRequest.getMatchString()})));
 					//$NON-NLS-1$ = "No known child tag names of <{0}> begin with \"{1}\"."
 					else
 						setErrorMessage(NLS.bind(XMLUIMessages.__Has_no_known_child, (new Object[]{parent.getNodeName()})));
 				}
 			}
 		}
 		else if (parent.getNodeType() == Node.DOCUMENT_NODE) {
 			// Can only prompt with elements if the cursor position is past
 			// the XML processing
 			// instruction and DOCTYPE declaration
 			boolean xmlpiFound = false;
 			boolean doctypeFound = false;
 			int minimumOffset = -1;
 
 			for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
 
 				boolean xmlpi = (child.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE && child.getNodeName().equals("xml")); //$NON-NLS-1$
 				boolean doctype = child.getNodeType() == Node.DOCUMENT_TYPE_NODE;
 				if (xmlpi || doctype && minimumOffset < 0)
 					minimumOffset = ((IDOMNode) child).getFirstStructuredDocumentRegion().getStartOffset() + ((IDOMNode) child).getFirstStructuredDocumentRegion().getTextLength();
 				xmlpiFound = xmlpiFound || xmlpi;
 				doctypeFound = doctypeFound || doctype;
 			}
 
 			if (contentAssistRequest.getReplacementBeginPosition() >= minimumOffset) {
 				List childDecls = getAvailableRootChildren((Document) parent, childPosition);
 				for (int i = 0; i < childDecls.size(); i++) {
 					CMElementDeclaration ed = (CMElementDeclaration) childDecls.get(i);
 					if (ed != null) {
 						Image image = CMImageUtil.getImage(ed);
 						if (image == null) {
 							image = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_TAG_GENERIC);
 						}
 						String proposedText = getRequiredText(parent, ed);
 						String tagname = getRequiredName(parent, ed);
 						// account for the &lt; and &gt;
 						int markupAdjustment = getContentGenerator().getMinimalStartTagLength(parent, ed);
 						String proposedInfo = getAdditionalInfo(null, ed);
 						CustomCompletionProposal proposal = new RedHatCustomCompletionProposal(false, proposedText, contentAssistRequest.getReplacementBeginPosition(), contentAssistRequest.getReplacementLength(), markupAdjustment, image, tagname, null, proposedInfo, XMLRelevanceConstants.R_TAG_INSERTION);
 
 						contentAssistRequest.addProposal(proposal);
 					}
 				}
 			}
 		}
 	}
 
 	// WTP
 	private int getCursorPositionForProposedText(String proposedText) {
 		int cursorAdjustment;
 		cursorAdjustment = proposedText.indexOf("\"\"") + 1; //$NON-NLS-1$
 		// otherwise, after the first tag
 		if(cursorAdjustment==0)
 			cursorAdjustment = proposedText.indexOf('>') + 1;
 		if(cursorAdjustment==0)
 			cursorAdjustment = proposedText.length() + 1;
 		
 		return cursorAdjustment;
 	}
 
 	protected void addTagNameProposals(ContentAssistRequest contentAssistRequest, int childPosition) {
 		List cmnodes = null;
 		Node parent = contentAssistRequest.getParent();
 		IDOMNode node = (IDOMNode) contentAssistRequest.getNode();
 		String error = null;
 		String matchString = contentAssistRequest.getMatchString();
 		if (parent.getNodeType() == Node.ELEMENT_NODE) {
 			// retrieve the list of children
 			cmnodes = getAvailableChildElementDeclarations((Element)parent, childPosition, ModelQueryAction.INSERT);
 			Iterator nodeIterator = cmnodes.iterator();
 			// chop off any leading <'s and whitespace from the matchstring
 			while ((matchString.length() > 0) && (Character.isWhitespace(matchString.charAt(0)) || beginsWith(matchString, "<"))) //$NON-NLS-1$
 				//$NON-NLS-1$
 				matchString = matchString.substring(1);
 			if (!nodeIterator.hasNext())
 				error = NLS.bind(XMLUIMessages.__Has_no_known_child, (new Object[]{parent.getNodeName()}));
 			while (nodeIterator.hasNext()) {
 				CMNode elementDecl = (CMNode) nodeIterator.next();
 				if (elementDecl != null) {
 					// only add proposals for the child element's that begin
 					// with the matchstring
 					String proposedText = null;
 					int cursorAdjustment = 0;
 
 					// do a check to see if partial attributes of partial tag
 					// names are in list
 					if ((node != null && node.getAttributes() != null && node.getAttributes().getLength() > 0 && attributeInList(node, parent, elementDecl)) || ((node.getNodeType() != Node.TEXT_NODE) && node.getFirstStructuredDocumentRegion().isEnded())) {
 
 						proposedText = getRequiredName(parent, elementDecl);
 						cursorAdjustment = proposedText.length();
 					}
 					else {
 						proposedText = getRequiredName(parent, elementDecl);
 
 					    StringBuffer buffer = new StringBuffer();
 						KbProposal proposal;
 						TagDescriptor info;
                         try {
                             proposal = getWtpKbConnector().getProposal("/" + proposedText);
                             info = getWtpKbConnector().getTagInformation("/" + proposedText + "/");
     						if (proposal != null && info != null) {
     							String replString = proposal.getReplacementString();
     							boolean hasBody = info.hasBody();
 
     							if (replString != null && replString.length() > 0) {
     							    if(!dontOpenTag) {
     							        buffer.append("<");
     							    }
     								buffer.append(replString);
     								buffer.append(">");
     								if (hasBody) {
     									buffer.append("</");
     									buffer.append(proposedText);
     									buffer.append(">");
     								}
     								proposedText = buffer.toString();
     							}
     						} else {
     						    if(elementDecl instanceof CMElementDeclaration) {
 									cursorAdjustment = proposedText.length();
 									if (elementDecl instanceof CMElementDeclaration) {
 										CMElementDeclaration ed = (CMElementDeclaration) elementDecl;
 										if (ed.getContentType() == CMElementDeclaration.EMPTY) {
 											proposedText += getContentGenerator().getStartTagClose(parent, ed);
 											cursorAdjustment = proposedText.length();
 										}
 										else {
 											cursorAdjustment = proposedText.length() + 1;
 											proposedText += "></" + getRequiredName(parent, elementDecl) + ">"; //$NON-NLS-2$//$NON-NLS-1$
 										}
 									}
     						    }
     						}
                         } catch (KbException e) {
                         	JspEditorPlugin.getPluginLog().logError(e);
                         }
 					}
 					if (beginsWith(proposedText, matchString) || beginsWith(proposedText, "<" + matchString)) {
 						Image image = CMImageUtil.getImage(elementDecl);
 						if (image == null) {
 							image = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_TAG_GENERIC);
 						}
 						String proposedInfo = getAdditionalInfo(getCMElementDeclaration(parent), elementDecl);
 						CustomCompletionProposal proposal = new RedHatCustomCompletionProposal(proposedText.indexOf('\"')>-1 && proposedText.indexOf("=")>-1, proposedText, contentAssistRequest.getReplacementBeginPosition(), contentAssistRequest.getReplacementLength(), cursorAdjustment, image, getRequiredName(parent, elementDecl), null, proposedInfo, XMLRelevanceConstants.R_TAG_NAME);
 						contentAssistRequest.addProposal(proposal);
 					}
 				}
 			}
 			if (contentAssistRequest.getProposals().size() == 0) {
 				if (error != null)
 					setErrorMessage(error);
 				else if (contentAssistRequest.getMatchString() != null && contentAssistRequest.getMatchString().length() > 0)
 					setErrorMessage(NLS.bind(XMLUIMessages.No_known_child_tag_names, (new Object[]{parent.getNodeName(), contentAssistRequest.getMatchString()})));
 				//$NON-NLS-1$ = "No known child tag names of <{0}> begin with \"{1}\""
 				else
 					setErrorMessage(NLS.bind(XMLUIMessages.__Has_no_known_child, (new Object[]{parent.getNodeName()})));
 			}
 		}
 		else if (parent.getNodeType() == Node.DOCUMENT_NODE) {
 			List childElements = getAvailableRootChildren((Document) parent, childPosition);
 			for (int i = 0; i < childElements.size(); i++) {
 				CMNode ed = (CMNode) childElements.get(i);
 				if (ed == null)
 					continue;
 				String proposedText = null;
 				int cursorAdjustment = 0;
 				if(ed instanceof CMElementDeclaration) {
 					// proposedText = getRequiredName(parent, ed);
 					StringBuffer sb = new StringBuffer();
 					getContentGenerator().generateTag(parent, (CMElementDeclaration)ed, sb);
 					// tag starts w/ '<', but we want to compare to name
 					proposedText = sb.toString().substring(1);
 					
 					if (!beginsWith(proposedText, matchString))
 						continue;
 					
 					cursorAdjustment = getCursorPositionForProposedText(proposedText);
 				
 					if (ed instanceof CMElementDeclaration) {
 						CMElementDeclaration elementDecl = (CMElementDeclaration) ed;
 						if (elementDecl.getContentType() == CMElementDeclaration.EMPTY) {
 							proposedText += getContentGenerator().getStartTagClose(parent, elementDecl);
 							cursorAdjustment = proposedText.length();
 						}
 					}
 				
 					String proposedInfo = getAdditionalInfo(null, ed);
 					Image image = CMImageUtil.getImage(ed);
 					if (image == null) {
 						image = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_TAG_GENERIC);
 					}
 					CustomCompletionProposal proposal = new RedHatCustomCompletionProposal(proposedText.indexOf('\"')>-1, proposedText, contentAssistRequest.getReplacementBeginPosition(), contentAssistRequest.getReplacementLength(), cursorAdjustment, image, getRequiredName(parent, ed), null, proposedInfo, XMLRelevanceConstants.R_TAG_NAME);
 					contentAssistRequest.addProposal(proposal);
 					contentAssistRequest.addProposal(proposal);
 				}
 			}
 		}
 	}
 
 	private boolean isCommentNode(IDOMNode node) {
 		return (node != null && node instanceof IDOMElement && ((IDOMElement) node).isCommentTag());
 	}
 	
 	protected void addAttributeNameProposals(ContentAssistRequest contentAssistRequest) {
 
 		// JBIDE-1717 Workaround: 
 		// The WTP processes the Attribute Containers in a different way comparing to how it does for 
 		// the simple attributes. 
 		// Also the whitespace-text placed after the attribute value is the part of that attribute value. 
 		// These facts both are the reason of the JBIDE-1717 issue appeared. 
 		// This workaround tries to add the attribute name proposals in case of WTP itself doesn't adds them 
 		// because it faced the attribute container (In other words, some JSF-variable is used as the 
 		// attribute's value 
 		if (fViewer != null) {
 			int documentPosition = contentAssistRequest.getReplacementBeginPosition();
 			// check the actual partition type
 			String partitionType = getPartitionType((StructuredTextViewer) fViewer, documentPosition);
 
 			IStructuredDocument structuredDocument = (IStructuredDocument) fViewer.getDocument();
 			IStructuredDocumentRegion fn = structuredDocument.getRegionAtCharacterOffset(documentPosition);
 			// check if it's in an attribute value, if so, don't add CDATA
 			// proposal
 			ITextRegion attrContainer = (fn != null) ? fn.getRegionAtCharacterOffset(documentPosition) : null;
 			boolean doPerformProposalsComputing = false;
 			
 			if (attrContainer != null && attrContainer instanceof ITextRegionContainer) {
 				if (attrContainer.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE) {
 					// test location of the cursor
 					// return null if it's in the middle of an open/close
 					// delimeter
 					Iterator attrRegions = ((ITextRegionContainer) attrContainer).getRegions().iterator();
 					ITextRegion testRegion = null;
 					while (attrRegions.hasNext()) {
 						testRegion = (ITextRegion) attrRegions.next();
 						// need to check for other valid attribute regions
 						if (XMLContentAssistUtilities.isJSPOpenDelimiter(testRegion.getType())) {
 							if (!(((ITextRegionContainer) attrContainer).getEndOffset(testRegion) <= documentPosition))
 								break;
 						}
 						else if (XMLContentAssistUtilities.isJSPCloseDelimiter(testRegion.getType())) {
 							if (!(((ITextRegionContainer) attrContainer).getStartOffset(testRegion) >= documentPosition))
 								break;
 						}
 					}
 					if (!(testRegion.getType().equals(DOMJSPRegionContexts.JSP_CONTENT)))
 						doPerformProposalsComputing = true;
 				}
 			}
 			//
 			
 			if (doPerformProposalsComputing) {
 				ICompletionProposal[] embeddedResults = null;
 				IContentAssistProcessor p = (IContentAssistProcessor) fPartitionToProcessorMap.get(partitionType);
 				if (p != null) {
 					embeddedResults = p.computeCompletionProposals(fViewer, documentPosition);
 					// TODO: Probably it's needed to get bean methods, objects, and constants if there are any...
 					// as it's done in JSPContentAssistProcessor but...
 				}
 				else {
 					// the partition type is probably not mapped 
 				}
 				for (int i = 0; embeddedResults != null && i < embeddedResults.length; i++)
 					contentAssistRequest.addProposal(embeddedResults[i]);
 			}
 		}
 		super.addAttributeNameProposals(contentAssistRequest);
 	}
 
 	protected void addAttributeValueProposals(ContentAssistRequest contentAssistRequest) {
 		// JBIDE-1704:
 		// Check the position in the value:
 		// The following position: 
 		//     <nodeName attrName="attrValue"| .../> 
 		// is marked as attribute value, but the value itself is complete.
 		// There are no proposals to be correct at this position. 
 
 		String text = contentAssistRequest.getText();
 		String matchString = contentAssistRequest.getMatchString();
 
		if (matchString.length() > StringUtils.strip(text).length() &&
				( (matchString.startsWith("\"") && matchString.endsWith("\"") && 
						(matchString.indexOf("\"") != matchString.lastIndexOf("\""))) 
					|| (matchString.startsWith("'") && matchString.endsWith("\"") && 
							(matchString.indexOf("\"") != matchString.lastIndexOf("\""))))) {
 			return;
 		}
 
 		super.addAttributeValueProposals(contentAssistRequest);
 	}
 	
 	/**
 	 * StructuredTextViewer must be set before using this.
 	 */
 	public IStructuredDocumentRegion getStructuredDocumentRegion(int pos) {
 		IStructuredDocumentRegion sdRegion = ContentAssistUtils.getStructuredDocumentRegion(fTextViewer, pos);
 		ITextRegion region = (sdRegion == null ? null : sdRegion.getRegionAtCharacterOffset(pos));
 		if (region == null) {
 			return null;
 		}
 
 		if (region.getType() == DOMRegionContext.XML_TAG_OPEN &&  
 				sdRegion.getStartOffset(region) == pos) {
 			// The offset is at the beginning of the region
 			if ((sdRegion.getStartOffset(region) == sdRegion.getStartOffset()) && (sdRegion.getPrevious() != null) && (!sdRegion.getPrevious().isEnded())) {
 				// Is the region also the start of the node? If so, the
 				// previous IStructuredDocumentRegion is
 				// where to look for a useful region.
 				sdRegion = sdRegion.getPrevious();
 			}
 			else {
 				// Is there no separating whitespace from the previous region?
 				// If not,
 				// then that region is the important one
 				ITextRegion previousRegion = sdRegion.getRegionAtCharacterOffset(pos - 1);
 				if ((previousRegion != null) && (previousRegion != region) && (previousRegion.getTextLength() == previousRegion.getLength())) {
 					sdRegion = sdRegion.getPrevious();
 				}
 			}
 		}
 
 		return sdRegion;
 	}
 
 }
