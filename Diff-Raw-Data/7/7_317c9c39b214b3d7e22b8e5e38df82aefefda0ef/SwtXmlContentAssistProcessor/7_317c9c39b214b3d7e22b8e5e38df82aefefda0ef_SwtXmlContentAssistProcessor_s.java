 /*******************************************************************************
  * Copyright (c) 2008 Ralf Ebert
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Ralf Ebert - initial API and implementation
  *******************************************************************************/
 package com.swtxml.ide;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.text.contentassist.CompletionProposal;
 import org.eclipse.swt.widgets.Layout;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
 import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
 import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;
 import org.w3c.dom.Node;
 import org.w3c.dom.Text;
 
 import com.swtxml.adapter.IAdaptable;
 import com.swtxml.definition.IAttributeDefinition;
 import com.swtxml.definition.INamespaceDefinition;
 import com.swtxml.definition.ITagDefinition;
 import com.swtxml.definition.ITagScope;
 import com.swtxml.extensions.ExtensionsNamespaceResolver;
 import com.swtxml.swt.types.LayoutType;
 import com.swtxml.util.context.Context;
 import com.swtxml.util.parser.Strictness;
 import com.swtxml.util.proposals.Match;
 import com.swtxml.util.types.IContentAssistable;
 import com.swtxml.util.types.IType;
 
 @SuppressWarnings("restriction")
 public class SwtXmlContentAssistProcessor extends XMLContentAssistProcessor {
 
 	public SwtXmlContentAssistProcessor() {
 		super();
 	}
 
 	@Override
 	protected void addTagNameProposals(final ContentAssistRequest contentAssistRequest,
 			int childPosition) {
 
 		Match match = createMatch(contentAssistRequest);
 
 		// TODO: cache this
 		DocumentNamespaceBrowser namespaces = getNamespaceBrowser(contentAssistRequest);
 
 		Node parentNode = contentAssistRequest.getNode().getParentNode();
 		INamespaceDefinition parentNamespace = namespaces.getByURI(parentNode.getNamespaceURI());
 		if (parentNamespace == null) {
 			return;
 		}
		ITagDefinition parentTag = parentNamespace.getTag(parentNode.getNodeName());
 		if (parentTag == null) {
 			return;
 		}
 
 		// TODO: there should be a non-ide API for find valid tags
 		List<String> filteredTags = new ArrayList<String>();
 		for (INamespaceDefinition namespace : namespaces.getAllDefinitions()) {
 			for (String tagname : namespace.getTagNames()) {
 				ITagDefinition tag = namespace.getTag(tagname);
				if (!(tag instanceof ITagScope) || ((ITagScope) tag).isAllowedIn(parentTag)) {
 					filteredTags.add(namespaces.getPrefix(namespace) + tag.getName());
 				}
 			}
 		}
 
 		if (contentAssistRequest.getNode() instanceof Text) {
 			match = match.insertAroundMatch("", "/>");
 		}
 
 		addProposals(contentAssistRequest, match.propose(filteredTags));
 		super.addTagNameProposals(contentAssistRequest, childPosition);
 	}
 
 	private DocumentNamespaceBrowser getNamespaceBrowser(
 			final ContentAssistRequest contentAssistRequest) {
 		return new DocumentNamespaceBrowser(contentAssistRequest.getNode().getOwnerDocument());
 	}
 
 	@Override
 	protected void addAttributeNameProposals(final ContentAssistRequest contentAssistRequest) {
 		// TODO: cache this
 		DocumentNamespaceBrowser namespaces = getNamespaceBrowser(contentAssistRequest);
 
 		Node node = contentAssistRequest.getNode();
 		INamespaceDefinition namespace = getNamespace(node);
 		if (namespace == null) {
 			return;
 		}
 
 		ITagDefinition tag = namespace.getTag(node.getLocalName());
 		if (tag == null) {
 			return;
 		}
 
 		Match match = createMatch(contentAssistRequest);
 		match = match.insertAroundMatch("", "=\"\"");
 
 		List<Match> proposals = match.propose(tag.getAttributeNames());
 
 		// TODO: there should be a non-ide API for finding valid attribute names
 		List<String> foreignAttributeNames = new ArrayList<String>();
 		for (INamespaceDefinition ns : namespaces.getAllDefinitions()) {
 			for (String foreignAttributeName : ns.getForeignAttributeNames()) {
 				IAttributeDefinition foreignAttribute = ns
 						.getForeignAttribute(foreignAttributeName);
 				if (!(foreignAttribute instanceof ITagScope)
 						|| ((ITagScope) foreignAttribute).isAllowedIn(tag)) {
 					foreignAttributeNames
 							.add(namespaces.getPrefix(ns) + foreignAttribute.getName());
 				}
 			}
 
 		}
 		proposals.addAll(match.propose(foreignAttributeNames));
 
 		// TODO: Create match API suitable for xxx=""
 		for (Match proposal : proposals) {
 			proposal.moveCursor(2);
 		}
 		addProposals(contentAssistRequest, proposals);
 	}
 
 	private INamespaceDefinition getNamespace(Node node) {
 		return new ExtensionsNamespaceResolver().resolveNamespace(node.getNamespaceURI());
 	}
 
 	@Override
 	protected void addAttributeValueProposals(final ContentAssistRequest contentAssistRequest) {
 		IDOMNode node = (IDOMNode) contentAssistRequest.getNode();
 
 		INamespaceDefinition namespace = getNamespace(node);
 		if (namespace == null) {
 			return;
 		}
 
 		ITagDefinition tag = namespace.getTag(node.getLocalName());
 		if (tag == null) {
 			return;
 		}
 
 		// Find the attribute region and name for which this position should
 		// have a value proposed
 		IStructuredDocumentRegion open = node.getFirstStructuredDocumentRegion();
 		ITextRegionList openRegions = open.getRegions();
 		int i = openRegions.indexOf(contentAssistRequest.getRegion());
 		if (i < 0) {
 			return;
 		}
 		ITextRegion nameRegion = null;
 		while (i >= 0) {
 			nameRegion = openRegions.get(i--);
 			if (nameRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
 				break;
 			}
 		}
 
 		// the name region is REQUIRED to do anything useful
 		if (nameRegion == null) {
 			return;
 		}
 
 		String attributeName = open.getText(nameRegion);
 		IAttributeDefinition attribute = tag.getAttribute(attributeName);
 		if (attribute == null) {
 			return;
 		}
 
 		// TODO: refactor this out
 		if ("layoutData".equals(attributeName)) {
 			Context.addAdapter(new IAdaptable() {
 
 				@SuppressWarnings("unchecked")
 				public <T> T adaptTo(Class<T> adapterClass) {
 					if (adapterClass == Layout.class) {
 						Node layoutNode = contentAssistRequest.getNode().getParentNode()
 								.getAttributes().getNamedItem("layout");
 						if (layoutNode != null) {
 							return (T) new LayoutType().convert(layoutNode.getNodeValue(),
 									Strictness.NICE);
 						}
 					}
 					return null;
 				}
 
 			});
 		}
 
 		IType<?> type = attribute.getType();
 		if (type instanceof IContentAssistable) {
 
 			Match match = createMatch(contentAssistRequest).handleQuotes();
 			addProposals(contentAssistRequest, ((IContentAssistable) type).getProposals(match));
 		}
 
 		Context.clear();
 	}
 
 	private void addProposals(final ContentAssistRequest contentAssistRequest, List<Match> proposals) {
 		for (Match proposal : proposals) {
 			CompletionProposal newProposal = new CompletionProposal(proposal.getReplacementText(),
 					contentAssistRequest.getReplacementBeginPosition(), contentAssistRequest
 							.getReplacementLength(), proposal.getReplacementCursorPos(), null,
 					proposal.getText(), null, null);
 			contentAssistRequest.addProposal(newProposal);
 		}
 	}
 
 	private Match createMatch(final ContentAssistRequest contentAssistRequest) {
 		return new Match(contentAssistRequest.getText(), contentAssistRequest.getMatchString()
 				.length());
 	}
 }
