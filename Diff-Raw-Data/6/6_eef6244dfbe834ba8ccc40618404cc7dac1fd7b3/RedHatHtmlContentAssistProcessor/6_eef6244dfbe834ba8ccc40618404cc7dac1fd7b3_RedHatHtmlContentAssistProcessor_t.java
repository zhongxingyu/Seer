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
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jst.jsp.core.internal.contentmodel.TaglibController;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TLDCMDocumentManager;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.TaglibTracker;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.wst.html.ui.internal.HTMLUIPlugin;
 import org.eclipse.wst.html.ui.internal.contentassist.HTMLContentAssistProcessor;
 import org.eclipse.wst.html.ui.internal.preferences.HTMLUIPreferenceNames;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
 import org.eclipse.wst.sse.core.utils.StringUtils;
 import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;
 import org.eclipse.wst.xml.core.internal.contentmodel.util.DOMNamespaceHelper;
 import org.eclipse.wst.xml.core.internal.document.AttrImpl;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
 import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
 import org.eclipse.wst.xml.ui.internal.contentassist.XMLRelevanceConstants;
 import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImageHelper;
 import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImages;
 import org.eclipse.wst.xml.ui.internal.util.SharedXMLEditorPluginImageHelper;
 import org.jboss.tools.common.kb.AttributeDescriptor;
 import org.jboss.tools.common.kb.KbConnectorFactory;
 import org.jboss.tools.common.kb.KbConnectorType;
 import org.jboss.tools.common.kb.KbProposal;
 import org.jboss.tools.common.kb.KbQuery;
 import org.jboss.tools.common.kb.KbTldResource;
 import org.jboss.tools.common.kb.wtp.JspWtpKbConnector;
 import org.jboss.tools.common.kb.wtp.TLDVersionHelper;
 import org.jboss.tools.common.kb.wtp.WtpKbConnector;
 import org.jboss.tools.common.model.util.ELParser;
 import org.jboss.tools.common.reporting.ProblemReportingHelper;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.jboss.tools.jst.jsp.editor.TLDRegisterHelper;
 import org.jboss.tools.jst.jsp.outline.ValueHelper;
 import org.jboss.tools.jst.jsp.support.kb.FaceletsJsfCResource;
 import org.jboss.tools.jst.jsp.support.kb.WTPKbdBeanMethodResource;
 import org.jboss.tools.jst.jsp.support.kb.WTPKbdBeanPropertyResource;
 import org.jboss.tools.jst.jsp.support.kb.WTPKbdBundlePropertyResource;
 import org.jboss.tools.jst.web.tld.TaglibData;
 import org.jboss.tools.jst.web.tld.VpeTaglibListener;
 import org.jboss.tools.jst.web.tld.VpeTaglibManager;
 import org.jboss.tools.jst.web.tld.VpeTaglibManagerProvider;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 /**
  * @author Igels
  */
 public class RedHatHtmlContentAssistProcessor extends HTMLContentAssistProcessor implements VpeTaglibListener {
 
     private JSPActiveContentAssistProcessor jspActiveCAP;
     private WtpKbConnector wtpKbConnector;
     private IDocument document;
     private IEditorInput editorInput;
 	private VpeTaglibManager tldManager;
 	private boolean isFacelets = false;
 	public static final String faceletUri = "http://java.sun.com/jsf/facelets";
 	public static final String faceletHtmlUri = "http://www.w3.org/1999/xhtml/facelets";
 	public static final String faceletHtmlPrefix = "0fHP";
 	public static final String JSFCAttributeName = "jsfc";
 	public static final String faceletHtmlPrefixStart = faceletHtmlPrefix + ":";
 	//Added by Max Areshkau JBIDE-788
 	public static final KbTldResource faceletHtmlResource = new KbTldResource(faceletHtmlUri, "", faceletHtmlPrefix, null);
 
     public ICompletionProposal[] computeCompletionProposals(ITextViewer textViewer, int documentPosition) {
     	document = textViewer.getDocument();
     	editorInput = JspEditorPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
     	//added by Max Areshkau JBIDE-788
     	IndexedRegion treeNode = ContentAssistUtils.getNodeAt(textViewer, documentPosition);
     	IDOMNode node = (IDOMNode) treeNode;
     	registerToTldManager(textViewer, node);
 
     	ICompletionProposal[] proposals = super.computeCompletionProposals(textViewer, documentPosition);
     	if(proposals!=null) {
 	    	for(int i=0; i<proposals.length; i++) {
 				if(proposals[i] instanceof RedHatCustomCompletionProposal) {
 					fErrorMessage = null;
 					break;
 				}
 			}
     	}
     	if(isFacelets) {
     		return getUniqProposals(proposals);
     	}
     	proposals = getUniqProposals(proposals);
 		return proposals;
 	}
 
     private ICompletionProposal[] getUniqProposals(ICompletionProposal[] proposals) {
     	if(proposals==null) {
     		return null;
     	}
 		HashMap uniqProposals = new HashMap(proposals.length);
 		ArrayList uniqProposalList = new ArrayList(proposals.length);
 		for(int i=0; i<proposals.length; i++) {
 			int eq = proposals[i].getDisplayString().indexOf('=');
 			String str = proposals[i].getDisplayString();;
 			if(eq>0) {
 				str = str.substring(0, eq);				
 			}
 			Object proposal = uniqProposals.get(str);
 			if(proposal==null || proposals[i] instanceof RedHatCustomCompletionProposal) {
 				uniqProposals.put(str, proposals[i]);
 				uniqProposalList.add(proposals[i]);
 				if(proposal!=null) {
 					uniqProposalList.remove(proposal);
 				}
 			}
 		}
 		return (ICompletionProposal[])uniqProposalList.toArray(new ICompletionProposal[uniqProposals.size()]);
     }
 
 	protected void addTagInsertionProposals(ContentAssistRequest contentAssistRequest, int childPosition) {
 		if(!addELFaceletsProposals(contentAssistRequest, childPosition)) {
 			String request = "/" + contentAssistRequest.getMatchString();
 			Collection kbProposals = null;
 			try {
 				kbProposals = getWtpKbConnector().getProposals(request);
 			} catch(Exception e) {
 				JspEditorPlugin.getPluginLog().logError(e);
 			}
 			for (Iterator iter = kbProposals.iterator(); iter.hasNext();) {
 				KbProposal kbProposal = (KbProposal) iter.next();
 				if(ignoreProposal(kbProposal)) {
 					continue;
 				}
 				String proposedInfo = kbProposal.getContextInfo();
 				String kbReplacementString = kbProposal.getReplacementString();
 				String replacementString = "<" + kbReplacementString + ">";
 				String displayString = kbProposal.getLabel();
 				boolean autoContentAssistant = replacementString.indexOf('\"')>-1 && replacementString.indexOf("=")>-1;
 				int cursorAdjustment = replacementString.length();;
 				if(!kbReplacementString.endsWith("/")) {
 					replacementString = replacementString + "</" + displayString + ">";
 				}
 				Image image = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_TAG_GENERIC);
 				RedHatCustomCompletionProposal proposal = new RedHatCustomCompletionProposal(autoContentAssistant, replacementString, contentAssistRequest.getReplacementBeginPosition(), contentAssistRequest.getReplacementLength(), cursorAdjustment, image, displayString, null, proposedInfo, XMLRelevanceConstants.R_TAG_NAME);
 				contentAssistRequest.addProposal(proposal);
 			}
 			super.addTagInsertionProposals(contentAssistRequest, childPosition);
 		}
 	}
 
 	private boolean addELFaceletsProposals(ContentAssistRequest contentAssistRequest, int childPosition) {
 		if(isFacelets) {
 			IDOMNode node = (IDOMNode)contentAssistRequest.getNode();
 			IStructuredDocumentRegion open = node.getFirstStructuredDocumentRegion();
 			ITextRegionList openRegions = open.getRegions();
 			int i = openRegions.indexOf(contentAssistRequest.getRegion());
 			if(i < 0) return false;
 			String currentValue = contentAssistRequest.getText();
 			int start = contentAssistRequest.getStartOffset();
 			int offset = contentAssistRequest.getReplacementBeginPosition() - start;
 			String matchString = currentValue.substring(0, offset);
 			ValueHelper h = new ValueHelper();
 			IEditorInput input = h.getEditorInput();
 			ArrayList proposals = new ArrayList();
 			WTPKbdBeanPropertyResource r1 = new WTPKbdBeanPropertyResource(input, h.getConnector());
 			proposals.addAll(r1.queryProposal(matchString));
 			WTPKbdBeanMethodResource r2 = new WTPKbdBeanMethodResource(input, h.getConnector());
 			proposals.addAll(r2.queryProposal(matchString));
 			WTPKbdBundlePropertyResource r3 = new WTPKbdBundlePropertyResource(input, h.getConnector());
 			proposals.addAll(r3.queryProposal(matchString));
 			
             for (Iterator iter = proposals.iterator(); iter.hasNext();) {
             	KbProposal kbProposal = cleanFaceletProposal((KbProposal)iter.next());
             	kbProposal.postProcess(currentValue, offset);
                 int relevance = kbProposal.getRelevance();
                 if(relevance==KbProposal.R_NONE) {
                     relevance = XMLRelevanceConstants.R_XML_ATTRIBUTE_VALUE;
                 }
                 
                 if(kbProposal.getStart() >= 0) {
         			String replacementString = kbProposal.getReplacementString();
                     int replacementBeginPosition = start + kbProposal.getStart();
                     int replacementLength = kbProposal.getEnd() - kbProposal.getStart();
                 	int cursorPositionDelta = 0;
                 	int cursorPosition = kbProposal.getPosition() + cursorPositionDelta;
                 	RedHatCustomCompletionProposal proposal = new RedHatCustomCompletionProposal(kbProposal.autoActivationContentAssistantAfterApplication(), replacementString,
                 			replacementBeginPosition, replacementLength, cursorPosition, SharedXMLEditorPluginImageHelper.getImage(SharedXMLEditorPluginImageHelper.IMG_OBJ_ATTRIBUTE),
             				kbProposal.getLabel(), null, kbProposal.getContextInfo(), relevance);
             		contentAssistRequest.addProposal(proposal);
             		continue;
                 } else {
                 	StringBuffer replacementStringBuffer = new StringBuffer(kbProposal.getReplacementString());
                     int replacementBeginPosition = start;
                 	int replacementLength = 0;
                 	int cursorPositionDelta = 0;
                 	String replacementString = replacementStringBuffer.toString();
                 	int cursorPosition = kbProposal.getPosition() + cursorPositionDelta;
                 	RedHatCustomCompletionProposal proposal = new RedHatCustomCompletionProposal(kbProposal.autoActivationContentAssistantAfterApplication(), replacementString,
                 			replacementBeginPosition, replacementLength, cursorPosition, SharedXMLEditorPluginImageHelper.getImage(SharedXMLEditorPluginImageHelper.IMG_OBJ_ATTRIBUTE),
             				kbProposal.getLabel(), null, kbProposal.getContextInfo(), relevance);
             		contentAssistRequest.addProposal(proposal);
                 }
             }
             ELParser p = new ELParser();
             ELParser.Token root = p.parse(currentValue);
             ELParser.Token c = root == null ? null : ELParser.getTokenAt(root, offset);
             if(ELParser.getPrecedingOpen(c, offset) != null) return true;
 		}
 		return false;
 	}
 
 	protected void addAttributeNameProposals(ContentAssistRequest contentAssistRequest) {
 		Element element = (Element)contentAssistRequest.getNode();
 		NamedNodeMap attributes = element.getAttributes();
 
 		String tagName = element.getNodeName();
 		if(isFacelets && tagName.indexOf(':')<0) {
 			tagName = faceletHtmlPrefixStart + tagName;
 		}
 		String request = "/" + tagName + "@" + contentAssistRequest.getMatchString();
 		Collection kbProposals = null;
 		try {
 			kbProposals = getWtpKbConnector().getProposals(request);
 		} catch(Exception e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		}
 		for (Iterator iter = kbProposals.iterator(); iter.hasNext();) {
 			KbProposal kbProposal = cleanFaceletProposal((KbProposal)iter.next());
 			String proposedInfo = kbProposal.getContextInfo();
 			String replacementString = kbProposal.getReplacementString() + "=\"\"";
 			String displayString = kbProposal.getLabel();
 			AttrImpl attr = (AttrImpl)attributes.getNamedItem(displayString);
 			if(attr!=null) {
 				ITextRegion region = attr.getNameRegion();
 				IStructuredDocumentRegion docRegion = contentAssistRequest.getDocumentRegion();
 				if(docRegion.getStartOffset(region)>contentAssistRequest.getReplacementBeginPosition() ||
 						docRegion.getEndOffset(region)< contentAssistRequest.getReplacementBeginPosition() + contentAssistRequest.getReplacementLength()) {
 					continue;
 				}
 			}
 			boolean autoContentAssistant = true;
 			int cursorAdjustment = replacementString.length() - 1;
 			Image image = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_ATTRIBUTE);
 			RedHatCustomCompletionProposal proposal = new RedHatCustomCompletionProposal(autoContentAssistant, replacementString, contentAssistRequest.getReplacementBeginPosition(), contentAssistRequest.getReplacementLength(), cursorAdjustment, image, displayString, null, proposedInfo, XMLRelevanceConstants.R_TAG_NAME);
 			contentAssistRequest.addProposal(proposal);
 		}
 		addJsfAttributeNameProposalsForFaceletTag(contentAssistRequest);
 		super.addAttributeNameProposals(contentAssistRequest);
 	}
 
 	private void addJsfAttributeNameProposalsForFaceletTag(ContentAssistRequest contentAssistRequest) {
 		// Ignore if jsp is not facelet jsp
 		if(!isFacelets) {
 			return;
 		}
 
 		Element element = (Element)contentAssistRequest.getNode();
 
 		String tagName = element.getNodeName();
 		// Only HTML tags
 		if(tagName.indexOf(':')>0) {
 			return;
 		}
 
 		NamedNodeMap attributes = element.getAttributes();
 		Node jsfC = attributes.getNamedItem(JSFCAttributeName);
 		if(jsfC==null || (!(jsfC instanceof Attr))) {
 			return;
 		}
 		Attr jsfCAttribute = (Attr)jsfC;
 		String jsfTagName = jsfCAttribute.getValue();
 		if(jsfTagName==null || jsfTagName.indexOf(':')<1) {
 			return;
 		}
 
 		String request = "/" + jsfTagName + "@" + contentAssistRequest.getMatchString();
 		Collection kbProposals = null;
 		try {
 			kbProposals = getWtpKbConnector().getProposals(request);
 		} catch(Exception e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		}
 		for (Iterator iter = kbProposals.iterator(); iter.hasNext();) {
 			KbProposal kbProposal = (KbProposal)iter.next();
 			if(ignoreProposal(kbProposal)) {
 				continue;
 			}
 			String proposedInfo = kbProposal.getContextInfo();
 			String replacementString = kbProposal.getReplacementString() + "=\"\"";
 			String displayString = kbProposal.getLabel();
 			AttrImpl attr = (AttrImpl)attributes.getNamedItem(displayString);
 			if(attr!=null) {
 				ITextRegion region = attr.getNameRegion();
 				IStructuredDocumentRegion docRegion = contentAssistRequest.getDocumentRegion();
 				if(docRegion.getStartOffset(region)>contentAssistRequest.getReplacementBeginPosition() ||
 						docRegion.getEndOffset(region)< contentAssistRequest.getReplacementBeginPosition() + contentAssistRequest.getReplacementLength()) {
 					continue;
 				}
 			}
 			boolean autoContentAssistant = true;
 			int cursorAdjustment = replacementString.length() - 1;
 			Image image = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_ATTRIBUTE);
 			RedHatCustomCompletionProposal proposal = new RedHatCustomCompletionProposal(autoContentAssistant, replacementString, contentAssistRequest.getReplacementBeginPosition(), contentAssistRequest.getReplacementLength(), cursorAdjustment, image, displayString, null, proposedInfo, XMLRelevanceConstants.R_TAG_NAME);
 			contentAssistRequest.addProposal(proposal);
 		}
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
 
 		if (jspActiveCAP != null) {
 			jspActiveCAP.setFacelets(isFacelets);
 			jspActiveCAP.addAttributeValueProposals(contentAssistRequest);
 		}
 		IDOMNode node = (IDOMNode) contentAssistRequest.getNode();
 		String tagName = node.getNodeName();
 		CMElementDeclaration elementDecl = getCMElementDeclaration(node);
 
 		IStructuredDocumentRegion open = node.getFirstStructuredDocumentRegion();
 		ITextRegionList openRegions = open.getRegions();
 		int i = openRegions.indexOf(contentAssistRequest.getRegion());
 		if (i < 0)
 			return;
 		ITextRegion nameRegion = null;
 		while (i >= 0) {
 			nameRegion = openRegions.get(i--);
 			if (nameRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME)
 				break;
 		}
 
 		String attributeName = open.getText(nameRegion);
 
 		boolean faceletJsfTag = false;
 		if(isFacelets && tagName.indexOf(':')<1 && !JSFCAttributeName.equals(attributeName)) {
 			Element element = (Element)node;
 
 			NamedNodeMap attributes = element.getAttributes();
 			Node jsfC = attributes.getNamedItem(RedHatHtmlContentAssistProcessor.JSFCAttributeName);
 			if(jsfC!=null && (jsfC instanceof Attr)) {
 				Attr jsfCAttribute = (Attr)jsfC;
 				String jsfTagName = jsfCAttribute.getValue();
 				if(jsfTagName!=null && jsfTagName.indexOf(':')>0) {
 					tagName = jsfTagName;
 					faceletJsfTag = true;
 				}
 			}
 		}
 
 		if(!faceletJsfTag && isFacelets && tagName.indexOf(':')<0) {
 			tagName = faceletHtmlPrefixStart + tagName;
 		}
 		String query = new StringBuffer(KbQuery.TAG_SEPARATOR).append(tagName).append(KbQuery.ATTRIBUTE_SEPARATOR).append(attributeName).toString();
 		AttributeDescriptor ad = null;
 		try {
 			ad = getWtpKbConnector().getAttributeInformation(query);
 		} catch(Exception e) {
 			ProblemReportingHelper.reportProblem(JspEditorPlugin.PLUGIN_ID, "ERROR: Can't get Attribute Descriptor from KB by " + query + "", e);
 		}
 		if(ad!=null) {
 			CMAttributeDeclaration attrDecl = null;
 			if (elementDecl != null) {
 				CMNamedNodeMap attributes = elementDecl.getAttributes();
 				String noprefixName = DOMNamespaceHelper.getUnprefixedName(attributeName);
 				if (attributes != null) {
 					attrDecl = (CMAttributeDeclaration) attributes.getNamedItem(noprefixName);
 					if (attrDecl == null) {
 						attrDecl = (CMAttributeDeclaration) attributes.getNamedItem(attributeName);
 					}
 				}
 			}
 			if (attrDecl == null || attrDecl.getAttrType() == null) {
 				return;
 			}
 		}
 
 		super.addAttributeValueProposals(contentAssistRequest);
 	}
 
 	protected void addTagNameProposals(ContentAssistRequest contentAssistRequest, int childPosition) {
 		String request = "/" + contentAssistRequest.getMatchString();
 		Collection kbProposals = null;
 		try {
 			kbProposals = getWtpKbConnector().getProposals(request);
 		} catch(Exception e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		}
 		if(kbProposals!=null) {
 			for (Iterator iter = kbProposals.iterator(); iter.hasNext();) {
 				KbProposal kbProposal = (KbProposal) iter.next();
 				if(ignoreProposal(kbProposal)) {
 					continue;
 				}
 				String proposedInfo = kbProposal.getContextInfo();
 				String kbReplacementString = kbProposal.getReplacementString();
 				String replacementString = kbReplacementString + ">";
 				String displayString = kbProposal.getLabel();
 				boolean autoContentAssistant = replacementString.indexOf('\"')>-1 && replacementString.indexOf("=")>-1;
 				int cursorAdjustment = replacementString.length();
 				if(!kbReplacementString.endsWith("/")) {
 					replacementString = replacementString + "</" + displayString + ">";
 				}
 				Image image = XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_TAG_GENERIC);
 				RedHatCustomCompletionProposal proposal = new RedHatCustomCompletionProposal(autoContentAssistant, replacementString, contentAssistRequest.getReplacementBeginPosition(), contentAssistRequest.getReplacementLength(), cursorAdjustment, image, displayString, null, proposedInfo, XMLRelevanceConstants.R_TAG_NAME);
 				contentAssistRequest.addProposal(proposal);
 			}
 		}
 		super.addTagNameProposals(contentAssistRequest, childPosition);
 	}
 
 	private boolean ignoreProposal(KbProposal proposal) {
 		if(isFacelets) {
 			return proposal.getLabel().startsWith(faceletHtmlPrefixStart);
 		}
 		return false;
 	}
 
 	private KbProposal cleanFaceletProposal(KbProposal proposal) {
 		if(isFacelets) {
 			proposal.setLabel(removeFaceletsPrefix(proposal.getLabel()));
 			proposal.setReplacementString(removeFaceletsPrefix(proposal.getReplacementString()));
 		}
 		return proposal;
 	}
 
 	private String removeFaceletsPrefix(String tagName) {
 		if(tagName.startsWith(faceletHtmlPrefixStart)) {
 			return tagName.substring(faceletHtmlPrefixStart.length());
 		}
 		return tagName;
 	}
 
 	private WtpKbConnector getWtpKbConnector() {
 	    if(wtpKbConnector == null && document != null) {
 	        try {
                 wtpKbConnector = (WtpKbConnector)KbConnectorFactory.getIntstance().createConnector(KbConnectorType.JSP_WTP_KB_CONNECTOR, document);
                 jspActiveCAP.setKbConnector(wtpKbConnector);
                 FaceletsJsfCResource fsfCResource = new FaceletsJsfCResource(wtpKbConnector);
                 wtpKbConnector.registerResource(fsfCResource);
             } catch(Exception e) {
             	ProblemReportingHelper.reportProblem(JspEditorPlugin.PLUGIN_ID, "ERROR: Can't create WtpKbConnector.", e);
             }
 	    }
 	    return wtpKbConnector;
 	}
 
 	private void registerToTldManager(ITextViewer viewer,IDOMNode node) {
 		if((tldManager==null) && (viewer instanceof VpeTaglibManagerProvider)) {
 			tldManager = ((VpeTaglibManagerProvider)viewer).getTaglibManager();
 			tldManager.setReferenceNode(node);
 			if(tldManager!=null) {
 				tldManager.addTaglibListener(this);
 				updateActiveContentAssistProcessor(document);
 			}
 		} else if(tldManager!=null)  {
 			tldManager.setReferenceNode(node);
 			updateActiveContentAssistProcessor(document);
 		}
 	}
 
 	public void taglibPrefixChanged(String[] prefixs) {
 		updateActiveContentAssistProcessor(document);
 	}
 
 	public void addTaglib(String uri, String prefix) {
 	}
 
 	public void removeTaglib(String uri, String prefix) {
 	}
 
 	public static void registerTld(TaglibData data, JspWtpKbConnector wtpKbConnector, IDocument document, IEditorInput input) {
 		TLDRegisterHelper.registerTld(data, wtpKbConnector, document, input);
 	}
 
 	public void updateActiveContentAssistProcessor(IDocument document) {
 		if(tldManager==null) {
 			TLDCMDocumentManager manager = TaglibController.getTLDCMDocumentManager(document);
 			if (manager != null) {
 				List list = manager.getTaglibTrackers();
 				for(int i=0; i<list.size(); i++) {
 					TaglibTracker tracker = (TaglibTracker)list.get(i);
 					String version = TLDVersionHelper.getTldVersion(tracker);
 					KbTldResource resource = new KbTldResource(tracker.getURI(), "", tracker.getPrefix(), version);
 			        getWtpKbConnector().registerResource(resource);
 				}
 			}
 		} else {
 			List list = tldManager.getTagLibs();
 			if(list==null) {
 				return;
 			}
 			((JspWtpKbConnector)getWtpKbConnector()).unregisterAllResources(true);
 			isFacelets = false;
 			for(int i=0; i<list.size(); i++) {
 				TaglibData data = (TaglibData)list.get(i);
 				registerTld(data, (JspWtpKbConnector)getWtpKbConnector(), document, editorInput);
 				isFacelets = isFacelets || data.getUri().equals(faceletUri);
 			}
 			if(isFacelets) {
 		        getWtpKbConnector().registerResource(faceletHtmlResource);
 		        ((JspWtpKbConnector)getWtpKbConnector()).unregisterJspResource();
 			}
 		}
 	}
 
 	public char[] getCompletionProposalAutoActivationCharacters() {
 		char[] autoActivChars = null;
 		char[] superAutoActivChars = super.getCompletionProposalAutoActivationCharacters();
 		if(superAutoActivChars==null) {
 			return superAutoActivChars;
 		}
 
 		autoActivChars = superAutoActivChars;
 		IPreferenceStore store = HTMLUIPlugin.getDefault().getPreferenceStore();
 		if(store.isDefault(HTMLUIPreferenceNames.AUTO_PROPOSE_CODE)) {
 //			String superDefaultChars = store.getDefaultString(JSPUIPreferenceNames.AUTO_PROPOSE_CODE);
 			StringBuffer redhatDefaultChars = new StringBuffer(new String(superAutoActivChars));
 			if(redhatDefaultChars.indexOf(".")<0) {
 				redhatDefaultChars.append('.');
 				store.setDefault(HTMLUIPreferenceNames.AUTO_PROPOSE_CODE, redhatDefaultChars.toString());
 				store.setValue(HTMLUIPreferenceNames.AUTO_PROPOSE_CODE, redhatDefaultChars.toString());
 			}
 			autoActivChars = new char[redhatDefaultChars.length()];
 			redhatDefaultChars.getChars(0, redhatDefaultChars.length(), autoActivChars, 0);
 		}
 
 		return autoActivChars;
 	}
 
 	protected void init() {
 	    super.init();
 		jspActiveCAP = new JSPActiveContentAssistProcessor();
 		jspActiveCAP.init();
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
