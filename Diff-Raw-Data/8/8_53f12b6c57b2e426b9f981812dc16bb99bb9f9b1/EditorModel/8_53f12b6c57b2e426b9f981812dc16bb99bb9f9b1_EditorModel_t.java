 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  * Copyright (c) 2008-2009, The KiWi Project (http://www.kiwi-project.eu)
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  * - Redistributions of source code must retain the above copyright notice, 
  *   this list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright notice, 
  *   this list of conditions and the following disclaimer in the documentation 
  *   and/or other materials provided with the distribution.
  * - Neither the name of the KiWi Project nor the names of its contributors 
  *   may be used to endorse or promote products derived from this software 
  *   without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  * POSSIBILITY OF SUCH DAMAGE.
  * 
  * Contributor(s):
  * 
  * 
  */
 package kiwi.wiki.action;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.persistence.EntityManager;
 
 import nu.xom.Attribute;
 import nu.xom.Document;
 import nu.xom.Element;
 import nu.xom.Node;
 import nu.xom.Nodes;
 import nu.xom.ParentNode;
 import nu.xom.Text;
 import nu.xom.XPathContext;
 
 import org.jboss.seam.Component;
 import org.jboss.seam.log.Log;
 import org.jboss.seam.log.Logging;
 
 import kiwi.api.comment.CommentService;
 import kiwi.api.config.ConfigurationService;
 import kiwi.api.content.ContentItemService;
 import kiwi.api.fragment.FragmentFacade;
 import kiwi.api.fragment.FragmentService;
 import kiwi.api.render.RenderingService;
 import kiwi.api.render.StoringService;
 import kiwi.api.tagging.TaggingService;
 import kiwi.api.triplestore.TripleStore;
 import kiwi.exception.InconsistentStateException;
 import kiwi.model.Constants;
 import kiwi.model.content.ContentItem;
 import kiwi.model.content.TextContent;
 import kiwi.model.content.TextFragment;
 import kiwi.model.informationextraction.Context;
 import kiwi.model.informationextraction.Suggestion;
 import kiwi.model.kbase.KiWiResource;
 import kiwi.model.kbase.KiWiTriple;
 import kiwi.model.kbase.KiWiUriResource;
 import kiwi.model.tagging.Tag;
 import kiwi.model.user.User;
 import kiwi.service.render.RenderingServiceImpl;
 import kiwi.service.render.renderlet.SourceRenderlet;
 import kiwi.service.render.renderlet.XOMRenderlet;
 import kiwi.util.KiWiXomUtils;
 import kiwi.util.KiWiXomUtils.NodePos;
 
 /**
  * This class represents the state of the editor,
  * 
  * Its purpose is to cache all the text and metadata so only one transaction is
  * run after clicking "save" and nothing really changes in the persistent data
  * before that...
  * 
  * The state of the editor consists of:
  * 
  * 1. The text content ("unwinded", that is, including the text content of all
  * the nested items.
  * 
  * 2. Metadata (of the CI and nested CI, some of those CI may be non-existing
  * CIs...) (metadata = list of types, and optionally relations for the nested
  * CIs...
  * 
  * 3. fragments and their metadata.
  * 
  * @author Marek Schmidt
  * 
  */
 public class EditorModel {
 	
 	public static class Comment {
 		
 		public Comment(String title, String html, Date created, User author, boolean isNew) {
 			this.title = title;
 			this.html = html;
 			this.created = created;
 			this.author = author;
 			this.isNew = isNew;
 		}
 		
 		public String getTitle() {
 			return title;
 		}
 
 		public void setTitle(String title) {
 			this.title = title;
 		}
 
 		public String getHtml() {
 			return html;
 		}
 
 		public void setHtml(String html) {
 			this.html = html;
 		}
 
 		public boolean isNew() {
 			return isNew;
 		}
 
 		public void setNew(boolean isNew) {
 			this.isNew = isNew;
 		}
 
 		public void setAuthor(User author) {
 			this.author = author;
 		}
 
 		public User getAuthor() {
 			return author;
 		}
 
 		public void setCreated(Date created) {
 			this.created = created;
 		}
 
 		public Date getCreated() {
 			return created;
 		}
 
 		private String title;
 		private String html;
 		private User author;
 		private Date created;
 		
 		private boolean isNew;	
 	}
 
 	private User currentUser;
 	
 	private KiWiResource currentResource;
 
 	private String html;
 	
 	/**
 	 * The "raw" text content, not processed by the text content savelets, just the source savelets.
 	 * We use the XML for embedding suggestions and applying suggestions.
 	 */
 	private TextContent textContent;
 
 	public String getHtml() {
 		if (html == null && textContent != null) {
 			
 			// What we need is to convert the fragments / links and stuff to the format the editor understands.
 			// The textContent is in a format somewhere between the kiwi internal format and the HTML editor format
 			// The only XOM renderlet we need is the editorLinkRenderlet (to convert the extractlinkssavelet)
 			
 			// TODO: this is completely unsystematic solution, find a better one.
 			
 			List<SourceRenderlet> editorSourceRenderlets = (List<SourceRenderlet>)Component.getInstance("renderingService.editorSourceRenderlets");
 			
 			XOMRenderlet editorLinkRenderlet = (XOMRenderlet)Component.getInstance("kiwi.service.render.renderlet.EditorLinkRenderlet");
 			
 			Document xom = textContent.copyXmlDocument();
 			xom = editorLinkRenderlet.apply(currentResource, xom);
 			
 			// This is copied from RenderingServiceImpl
 			String result = RenderingServiceImpl.extractPage(xom);
 
 			//Logger log = Logger.getLogger(EditorModel.class);
 			
 			// log.info("getHtml result: " + result);
 			
 			// perform rendering: source renderlets
 			for(SourceRenderlet renderlet : editorSourceRenderlets) {
 				result = renderlet.apply(currentResource, result);
 				
 				//log.info("getHtml result after " + renderlet.toString() + " : " + result);
 			}
 
 			// workaround for tinyMCE bug which doubles all br tags.
 			result=result.replaceAll("<br></br>", "<br/>");
 			
 			//log.info("getHtml result final: " + result);
 			
 			html = result;
 		}
 		return html;
 	}
 
 	public void setHtml(String html) {
 		this.html = html;
 		textContent = null;
 	}
 	
 	public void setTextContent(TextContent textContent) {
 		this.textContent = textContent;
 		html = null;
 	}
 	
 	public TextContent getTextContent() {
 		if (textContent == null && html != null) {
 			StoringService storingService = (StoringService)Component.getInstance("storingPipeline");
 			String processedHtml = storingService.processHtmlSource(currentResource, html);
 			
 			//SourceSavelet extractLinksSavelet = (SourceSavelet)Component.getInstance("kiwi.service.render.savelet.ExtractLinksSavelet");
 			//String processedHtml = extractLinksSavelet.apply(currentResource, html);
 
 			//Logger log = Logger.getLogger(EditorModel.class);
 			//log.info("\n\ntextTextContent: " + processedHtml);
 			
 			textContent = new TextContent();
 			textContent.setXmlString(processedHtml);
 		}
 		
 		return textContent;
 	}
 
 	/**
 	 * Map storing temporary metadata.
 	 * 
 	 * map id -> list of outgoing triples, the id may be a kiwiid, or a
 	 * temporary id of a nested item that does not exist yet.
 	 * 
 	 */
 	private Map<KiWiResource, List<KiWiTriple>> outgoingTriples = new HashMap<KiWiResource, List<KiWiTriple>>();
 
 	/**
 	 * Map storing titles of all the content items... (the root and its nested
 	 * items)
 	 */
 	private Map<KiWiResource, String> titles = new HashMap<KiWiResource, String>();
 
 	/**
 	 * Map of all the "virtual" nested items created in this editing session.
 	 */
 	private Map<String, KiWiResource> nestedid2resource = new HashMap<String, KiWiResource>();
 	
 	/**
 	 * Map of all the "virtual" fragments created in this editing session.
 	 */
 	private Map<String, KiWiResource> fragmentid2resource = new HashMap<String, KiWiResource>();
 	
 	private Set<KiWiResource> deletedFragments = new HashSet<KiWiResource>();
 	
 	private Map<KiWiResource, List<Comment>> comments = new HashMap<KiWiResource, List<Comment>> ();
 		
 	// private Set<KiWiResource> deletedComments = new HashSet<KiWiResource>(); 
 
 	/**
 	 * Map of current tags defined by a content item.
 	 */
 	private Map<KiWiResource, List<Tag>> tags = new HashMap<KiWiResource, List<Tag>>();
 	
 	/**
 	 * A set of tags to remove.
 	 */
 	private List<Tag> removedTags = new LinkedList<Tag> ();
 	
 	/**
 	 * A set of tags to create.
 	 */
 	private List<Tag> addedTags = new LinkedList<Tag> ();
 
 	/**
 	 * A map of newly created tagging resources, so each tagging with the same label share the same content item when we persist. 
 	 */
 	private Map<String, ContentItem> tagLabels = new HashMap<String, ContentItem> ();
 	
 	/**
 	 * Map of fragments. 
 	 */
 	private Map<KiWiResource, List<KiWiResource>> fragments = new HashMap<KiWiResource, List<KiWiResource>>();
 	
 	/**
 	 * Map of all the content items that is being created in this editor instance.
 	 * 
 	 * We use this so we don't need to flush after we create the content items, 
 	 * instead, we query for the CI by kiwiids using this "cache"... 
 	 */
 	// private Map<String, ContentItem> createdContentItems = new HashMap<String, ContentItem>();
 	
 	/**
 	 * Map of information extraction suggestions that are currently displayed
 	 */
 	private Map<String, Suggestion> suggestions = new HashMap<String, Suggestion>();
 	
 	public List<String> getSuggestionIds() {
 		return new ArrayList<String> (suggestions.keySet());
 	}
 	
 	public Suggestion getSuggestion(String id) {
 		return suggestions.get(id);
 	}
 
 	public List<KiWiTriple> getOutgoingTriples(KiWiResource id) {
 		
 		
 		
 		if (!outgoingTriples.containsKey(id)) {
 			List<KiWiTriple> ret = new LinkedList<KiWiTriple>();
 			outgoingTriples.put(id, ret);
 			return ret;
 		}
 
 		return outgoingTriples.get(id);
 	}
 	
 	
 	public List<KiWiResource> getFragments(KiWiResource id) {
 		if (!fragments.containsKey(id)) {
 			List<KiWiResource> ret = new LinkedList<KiWiResource>();
 			fragments.put(id, ret);
 			return ret;
 		}
 		
 		return fragments.get(id);
 	}
 	
 	public List<Tag> getTags(KiWiResource id) {
 		if (!tags.containsKey(id)) {
 			List<Tag> ret = new LinkedList<Tag>();
 			tags.put(id, ret);
 			return ret;
 		}
 		
 		return tags.get(id);
 	}
 	
 	public List<Comment> getComments(KiWiResource id) {
 		if (!comments.containsKey(id)) {
 			List<Comment> ret = new LinkedList<Comment>();
 			comments.put(id, ret);
 			return ret;
 		}
 		
 		return comments.get(id);
 	}
 	
 	public void addComment(KiWiResource resource, String title, String html) {
 		getComments(resource).add(new Comment(title, html, new Date(), currentUser, true));
 	}
 
 	public String getTitle(KiWiResource id) {
 		return titles.get(id);
 	}
 
 	public void setTitle(KiWiResource id, String title) {
 		titles.put(id, title);
 	}
 	
 	/**
 	 * Create a uri resource, in the same way as a contentItemService would do,
 	 * just with no persistence or events involved.. 
 	 * 
 	 * @param uriPostfix
 	 * @return
 	 */
 	private KiWiResource createResource(String uriPostfix) {
 		ConfigurationService configurationService = (ConfigurationService)Component.getInstance("configurationService");
 		return new KiWiUriResource(configurationService.getBaseUri()+"/"+uriPostfix);
 	}
 	
 	/**
 	 * Create a random uri resource
 	 * @return
 	 */
 	private KiWiResource createResource() {
 		return createResource("content/"+UUID.randomUUID().toString());
 	}
 
 	/**
 	 * Create a new nested content item, returns its automatically generated id
 	 * that is unique in this editor instance. The nested item is not persisted.
 	 * Instead, it should be recreated after clicking "save" in the saving
 	 * transaction.
 	 * 
 	 * @return
 	 */
 	public KiWiResource createNestedItem() {
 		KiWiResource ret = createResource();
 		
 		nestedid2resource.put(ret.getKiwiIdentifier(), ret);
 		return ret;
 	}
 	
 	/**
 	 * Create a new fragment content item. The fragment is nto persisted.
 	 * It should be recreated after clicking "save" in the saving transaction.
 	 * @return
 	 */
 	public KiWiResource createFragment(KiWiResource containingItem) {
 		
 		KiWiResource ret = createResource();
 		
 		getFragments(containingItem).add(ret);
 
 		fragmentid2resource.put(ret.getKiwiIdentifier(), ret);
 		return ret;
 	}
 	
 	public void deleteFragment(KiWiResource containingItem, KiWiResource fragment) {
 		this.getFragments(containingItem).remove(fragment);
 		
		if (fragmentid2resource.containsKey(fragment.getKiwiIdentifier())) {
 			fragmentid2resource.remove(fragment.getKiwiIdentifier());
 		}
 		else {
 			deletedFragments.add(fragment);
 		}
 	}
 	
 	public void removeTag(Tag t) {
 		KiWiResource res = t.getTaggedResource().getResource();
 		getTags(res).remove(t);
 		
 		if (!addedTags.remove(t)) {
 			// Deleting a tag that existed before...
 			removedTags.add(t);
 		}
 	}
 	
 	public void removeTag(KiWiResource taggedResource, ContentItem taggingResource) {
 		
 		Iterator<Tag> itags = getTags(taggedResource).iterator();
 		while (itags.hasNext()) {
 			Tag t = itags.next();
 			
 			// we do consistently work with the same instances, so reference equality should work
 			if (t.getTaggingResource() == taggingResource && t.getTaggedBy().getId().equals(currentUser.getId())) {
 				itags.remove();
 				// If this was a real tag, also add it to the removedTags collection to delete the tagging
 				// after clicking "save"
 				if (t.getId() != null) {
 					removedTags.add(t);
 				}
 				else {
 					// This tag has to be in the addedTags collection, because it is a virtual tag, and as 
 					// such it had to be created previously by the createTag method.
 					assert addedTags.remove(t);
 				}
 				
 				break;
 			}
 		}
 	}
 	
 	public Tag createTag(KiWiResource taggedResource, ContentItem taggingResource) {
 		
 		ContentItem taggedItem = taggedResource.getContentItem();
 		if (taggedItem == null) {
 			 taggedItem = new ContentItem(taggedResource);
 		}
 				
 		Tag t = new Tag(taggedItem, currentUser, taggingResource);
 		
 		getTags(taggedResource).add(t);
 		addedTags.add(t);
 		
 		return t;
 	}
 	
 	/**
 	 * Creates a content item that can be used as a tagging resource.
 	 * 
 	 * It first tries to find an existing (or virtual) ContentItem with the same label.
 	 * 
 	 * @param label
 	 * @return
 	 */
 	public ContentItem createTaggingResource(String label) {
 
 		ContentItem taggingResource = tagLabels.get(label);
 		if (taggingResource != null) {
 			return taggingResource;
 		}
 		
 		ContentItemService contentItemService = (ContentItemService) Component
 		.getInstance("contentItemService");
 
 		taggingResource = contentItemService.getContentItemByTitle(label);
 
 		if(taggingResource == null) {			
 			KiWiResource resource = createResource("content/"+label.toLowerCase().replace(" ","_")+"/"+UUID.randomUUID().toString());
 			taggingResource = new ContentItem(resource);
 			taggingResource.setTitle(label);
 
 			tagLabels.put(label, taggingResource);
 		}
 		
 		return taggingResource;
 	}
 
 	/**
 	 * Method to safely return either temporary KiWiResources, or existing KiWi
 	 * resources.
 	 * 
 	 * @param kiwiid
 	 * @return
 	 */
 	public KiWiResource getResourceByNestedId(String kiwiid) {
 		if (nestedid2resource.containsKey(kiwiid)) {
 			return nestedid2resource.get(kiwiid);
 		} else {
 			ContentItemService service = (ContentItemService) Component
 					.getInstance("contentItemService");
 			ContentItem ci = service.getContentItemByKiwiId(kiwiid);
 			if (ci == null) {
 				return null;
 			}
 			
 			return ci.getResource();
 		}
 	}
 	
 	public KiWiResource getResourceByFragmentId(String kiwiid) {
 		if (fragmentid2resource.containsKey(kiwiid)) {
 			return fragmentid2resource.get(kiwiid);
 		} else {
 			ContentItemService service = (ContentItemService) Component
 					.getInstance("contentItemService");
 			ContentItem ci = service.getContentItemByKiwiId(kiwiid);
 			if (ci == null) {
 				return null;
 			}
 			return ci.getResource();
 		}
 	}
 	
 	public ContentItem getContentItemByKiWiId(String kiwiid) {
 		
 		Map<String, ContentItem> contentItemsCache = (Map<String, ContentItem>) Component.getInstance("storingService.contentItemCache");
 		
 		ContentItem ret = contentItemsCache.get(kiwiid);
 		if (ret == null) {
 			// try the CI service
 			ContentItemService service = (ContentItemService) Component
 			.getInstance("contentItemService");
 			
 			ret = service.getContentItemByKiwiId(kiwiid);
 		}
 		
 		return ret;
 	}
 
 	public void setCurrentResource(KiWiResource currentResource) {
 		this.currentResource = currentResource;
 	}
 
 	public KiWiResource getCurrentResource() {
 		return currentResource;
 	}
 	
 	public void removeSuggestion(String id) {
 		suggestions.remove(id);
 		
 		TextContent tc = getTextContent();
 		
 		assert id.startsWith("uri::");
 		String uri = id.substring(5);
 		
 		KiWiUriResource suggestionResource = new KiWiUriResource(uri);
 		TextFragment fragment = tc.getFragment(suggestionResource);
 		
 		try{
 			Element start = fragment.getBookmarkStart();
 			start.getParent().removeChild(start);
 		}
 		catch(InconsistentStateException x) {
 			// the fragment may have already been deleted from the content by editing. 
 		}
 		
 		try {
 			Element end = fragment.getBookmarkEnd();
 			end.getParent().removeChild(end);
 		}
 		catch(InconsistentStateException x) {
 			// the fragment may have already been deleted from the content by editing. 
 		}
 		
 		setTextContent(tc);
 	}
 	
 	/**
 	 * Remove the suggestions from the text content.
 	 */
 	public void clearSuggestions() {
 		TextContent tc = getTextContent();
 
 		for (Map.Entry<String, Suggestion> entry : suggestions.entrySet()) {
 			String id = entry.getKey();
 			assert id.startsWith("uri::");
 			String uri = id.substring(5);
 			KiWiUriResource suggestionResource = new KiWiUriResource(uri);
 			TextFragment fragment = tc.getFragment(suggestionResource);
 			
 			try{
 				Element start = fragment.getBookmarkStart();
 				start.getParent().removeChild(start);
 			}
 			catch(InconsistentStateException x) {
 				// the fragment may have already been deleted from the content by editing. 
 			}
 			
 			try {
 				Element end = fragment.getBookmarkEnd();
 				end.getParent().removeChild(end);
 			}
 			catch(InconsistentStateException x) {
 				// the fragment may have already been deleted from the content by editing. 
 			}
 			
 		}
 		
 		suggestions.clear();
 		
 		setTextContent(tc);
 	}
 	
 	private String getFragmentContent(TextContent tc, String id) {
 		TextFragment tf = tc.getFragment(new KiWiUriResource(id.substring(5)));
 		return KiWiXomUtils.xom2plain(tf.getFragmentTree());
 	}
 	
 	private void replaceFragmentContent(TextContent tc, String id, Element e) {
 		
 		TextFragment tf = tc.getFragment(new KiWiUriResource(id.substring(5)));
 		
 		Element bookmarkstart = tf.getBookmarkStart();
 		Element bookmarkend = tf.getBookmarkEnd();
 		
 		KiWiXomUtils.NodePosIterator iter = new KiWiXomUtils.NodePosIterator(tc.getXmlDocument().getRootElement());
 		
 		boolean moving = false;
 		
 		List<Node> textNodes = new LinkedList<Node> ();
 		
 		while (iter.hasNext()) {
 			NodePos np = iter.next();
 			
 			if (np.getNode() instanceof Element) {
 				if (np.getNode().equals(bookmarkstart)) {
 					moving = true;
 					continue;
 				}
 				
 				if (np.getNode().equals(bookmarkend)) {
 					moving = false;
 					continue;
 				}
 			}
 			
 			if (moving) {
 				if (np.getNode() instanceof Text) {
 					textNodes.add(np.getNode());
 				}
 			}
 		}
 		
 		for (Node n : textNodes) {
 			// TODO: remove empty formatting elements.
 			// ParentNode pn = n.getParent();
 			n.detach();
 		}
 		
 		ParentNode pnode = bookmarkstart.getParent();
 
 		int index = pnode.indexOf(bookmarkstart);
 		
 		pnode.insertChild(e, index + 1);
 		
 		for (int i = 0; i < textNodes.size(); ++i) {
 			e.appendChild(textNodes.get(i));
 			//pnode.insertChild(textNodes.get(i), index + i + 1);
 		}
 	}
 	
 	/**
 	 * "apply" the suggestion. Based on the kind of the suggestion, it may create a datatype property, or a link.
 	 * 
 	 * TODO: This is just a proof-of-concept, the real thing should provide a UI for disambiguation of targets / types, etc.
 	 * @param id
 	 */
 	void realizeSuggestion(String id) {
 		Suggestion suggestion = suggestions.get(id);
 		
 		if (suggestion.getKind() == Suggestion.ENTITY) {
 			// Let's try to create a link.
 			
 			TextContent tc = getTextContent();
 			
 			ContentItem target = null;
 			if (suggestion.getResources().size() > 0) {
 				target = suggestion.getResources().get(0).getContentItem();
 			}
 
 			Element a = new Element("html:a", Constants.NS_XHTML);
 			a.addAttribute(new Attribute("kiwi:kind", Constants.NS_KIWI_HTML, "intlink"));
 			if (target != null) {
 				a.addAttribute(new Attribute("kiwi:target", Constants.NS_KIWI_HTML, target.getTitle()));
 			}
 			else {
 				a.addAttribute(new Attribute("kiwi:target", Constants.NS_KIWI_HTML, getFragmentContent(tc, id)));
 			}
 			
 			replaceFragmentContent(tc, id, a);
 			removeSuggestion(id);
 		}
 		else if (suggestion.getKind() == Suggestion.DATATYPE) {
 			TextContent tc = getTextContent();
 				
 			ContentItem role = null;
 			if (suggestion.getRoles().size() > 0) {
 				role = suggestion.getRoles().get(0).getContentItem();
 			}
 			
 			if (role != null) {
 				Element a = new Element("html:span", Constants.NS_XHTML);
 				a.addAttribute(new Attribute("property", role.getKiwiIdentifier()));
 				replaceFragmentContent(tc, id, a);
 			}
 			
 			removeSuggestion(id);
 		}
 	}
 	
 	/**
 	 * Create the suggestion fragment, but only if the context looks safe to put a fragment in.. 
 	 * (it does not insert a fragment inside a link, nor an RDFa element)
 	 *  
 	 * @param s
 	 */
 	public boolean addSuggestion(Suggestion s) {
 		TextContent tc = getTextContent();
 		Document xom = tc.getXmlDocument();
 		
 		// Log log = Logging.getLog(EditorModel.class);
 		//log.info("addSuggestion: \n" + xom.toXML() + "\n");
 		
 		// We use this kind of URI for the suggestions, they are never persisted and used only inside the editor.
 		String uri = "urn:kiwi:suggestion:" + UUID.randomUUID().toString();
 
 		// We add the type "link_suggestion", "item_suggestion" or "fragment_suggestion" to the bookmark
 		String type;
 		if (s.getKind() == Suggestion.DATATYPE || s.getKind() == Suggestion.ENTITY) {
 			// datatype and entity suggestions are handled by the linking tool
 			type = "link_suggestion";
 		}
 		else if (s.getKind() == Suggestion.NESTED_ITEM) {
 			type = "item_suggestion";
 		}
 		else {
 			// We don't support any other kind of suggestion right now
 			return false;
 		}
 		
 		Context context = s.getInstance().getContext();
 		Element[] bookmarks = context.fragmentize(xom, "uri::" + uri);
 		
 		
 		bookmarks[0].addAttribute(new Attribute("type", type));
 		
 		// We need to check whether the suggestion has already been realized, for now, we just check if the fragment lies in the "span" or an "a" element.
 		// This is most easily done after the suggestion has been fragmentised... 
 		
 		// TODO: do it without fragmentization, and also have a more sane condition to check.
 		// the current code may fail at some cases, where neither end of the fragment is inside of the span or anchor.
 		TextFragment tf = tc.getFragment(new KiWiUriResource(uri));
 		
 		ParentNode pn = tf.getBookmarkStart().getParent();
 		if (pn instanceof Element) {
 			Element pne = (Element)pn;
 			if ("span".equalsIgnoreCase(pne.getLocalName()) || "a".equalsIgnoreCase(pne.getLocalName())) {
 				removeSuggestion("uri::" + uri);
 				return false;
 			}
 		}
 		
 		pn = tf.getBookmarkEnd().getParent();
 		if (pn instanceof Element) {
 			Element pne = (Element)pn;
 			if ("span".equalsIgnoreCase(pne.getLocalName()) || "a".equalsIgnoreCase(pne.getLocalName())) {
 				removeSuggestion("uri::" + uri);
 				return false;
 			}
 		}
 		
 		// Check if the nested item suggestions does exist inside of a nested item.
 		// TODO: this is overkill, corretly, it should only disallow nested item that takes the whole of the suggestion, to allow nested-nested item suggestions
 		pn = tf.getBookmarkStart().getParent();
 		while (pn != null) {
 			if (pn instanceof Element) {
 				Element pne = (Element)pn;
 				if ("div".equalsIgnoreCase(pne.getLocalName()) && pne.getAttribute("about") != null) {
 					removeSuggestion("uri::" + uri);
 					return false;
 				}
 			}
 			pn = pn.getParent();
 		}
 		
 		pn = tf.getBookmarkEnd().getParent();
 		while (pn != null) {
 			if (pn instanceof Element) {
 				Element pne = (Element)pn;
 				if ("div".equalsIgnoreCase(pne.getLocalName()) && pne.getAttribute("about") != null) {
 					removeSuggestion("uri::" + uri);
 					return false;
 				}
 			}
 			pn = pn.getParent();
 		}
 		
 		suggestions.put("uri::" + uri, s);
 		
 		setTextContent(tc);
 				
 		return true;
 	}
 	
 	public void insertNestedItem(String nestedId) {
 		ContentItemService contentItemService = (ContentItemService) Component.getInstance("contentItemService");
 		RenderingService renderingPipeline = (RenderingService)Component.getInstance("renderingPipeline");
 		User currentUser = (User) Component.getInstance("currentUser");
 		ContentItem nested = contentItemService.getContentItemByKiwiId(nestedId);
 		
 		Log log = Logging.getLog(EditorModel.class);
 		
 		StoringService storingService = (StoringService)Component.getInstance("storingPipeline");
 		
 		log.debug("before insert: #0", getHtml());
 		
 		if (nested != null) {
 			// We need to get one step before the final HTML editor (so we need to renderEditor and than one step of the
 			// savelet process (the processHtmlSource)
 			String content = renderingPipeline.renderEditor(nested, currentUser);
 			String processedHtml = storingService.processHtmlSource(nested.getResource(), content);
 			
 			log.info("nested item #0, content: #1", nestedId, processedHtml);
 			
 			TextContent nestedContent = new TextContent();
 			nestedContent.setXmlString(processedHtml);
 			
 			TextContent textContent = getTextContent();
 			
 			Document xom = textContent.getXmlDocument();
 			
 			XPathContext namespaces = new XPathContext();
 			namespaces.addNamespace("kiwi", Constants.NS_KIWI_HTML);
 			namespaces.addNamespace("html", Constants.NS_XHTML);
 			
 			Nodes nodes = xom.query("//html:div[@about='" + nestedId +"']", namespaces);
 			for (int i = 0; i < nodes.size(); ++i) {
 				Element aboutDiv = (Element)nodes.get(i);
 				// replace the content of the about node.
 				aboutDiv.removeChildren();
 				
 				Element copy = nestedContent.copyXmlDocument().getRootElement();
 				while(copy.getChildCount() > 0) {
 					Node child = copy.getChild(0);
 					child.detach();
 					aboutDiv.appendChild(child);
 				}
 				
 				log.info("inserting nested item into XOM");
 			}
 			
 			setTextContent(textContent);
 			
 			log.debug("after insert: #0", getHtml());
 		}
 	}
 
 	private Collection<ContentItem> nestedItemsReflexiveTransitiveClosure(
 			ContentItem root) {
 		Set<ContentItem> ret = new HashSet<ContentItem>();
 
 		Queue<ContentItem> todo = new LinkedList<ContentItem>();
 		todo.add(root);
 		ret.add(root);
 
 		while (!todo.isEmpty()) {
 			ContentItem ci = todo.poll();
 			if (ci.getNestedContentItems() != null) {
 				for (ContentItem nested : ci.getNestedContentItems()) {
 					if (!ret.contains(nested)) {
 						ret.add(nested);
 						todo.add(nested);
 					}
 				}
 			}
 		}
 
 		return ret;
 	}
 
 	public void initModel(ContentItem contentItem, User currentUser, String content) {
 		this.currentResource = contentItem.getResource();
 		this.html = content;
 		this.currentUser = currentUser;
 
 		TaggingService taggingService = (TaggingService) Component.getInstance("taggingService");
 		FragmentService fragmentService = (FragmentService)Component.getInstance("fragmentService");
 		CommentService commentService = (CommentService)Component.getInstance("commentService");
 		RenderingService renderingService = (RenderingService)Component.getInstance("renderingPipeline");
 		
 		Collection<ContentItem> items = nestedItemsReflexiveTransitiveClosure(contentItem);
 
 		for (ContentItem item : items) {
 			KiWiResource itemResource = item.getResource();
 			
 			// We remember the title...
 			titles.put(itemResource, item.getTitle());
 
 			// ...all the outgoing triples... (which contains type, among other things...)
 			List<KiWiTriple> outgoingTriples = this
 					.getOutgoingTriples(itemResource);
 
 			for (KiWiTriple triple : itemResource.listOutgoing()) {
 				if (triple.isDeleted() != null && triple.isDeleted() != true) {
 					outgoingTriples.add(triple);
 				}
 			}
 			
 			// We also get the tags.
 			getTags(itemResource).addAll(taggingService.getTaggings(item));
 			
 			// ... and comments...
 			List<Comment> comments = getComments(itemResource);
 			for (ContentItem commentItem : commentService.listComments(item)) {
 				String html = renderingService.renderHTML(commentItem);
 				comments.add(new Comment(commentItem.getTitle(), html, commentItem.getCreated(), commentItem.getAuthor(), false));
 			}
 			
 			// ...all the fragments...
 			Collection<FragmentFacade> ffs = fragmentService.getContentItemFragments(item, FragmentFacade.class);
 			for (FragmentFacade ff : ffs) {
 				this.getFragments(itemResource).add(ff.getResource());
 				
 				// ...and their tags...
 				getTags(ff.getResource()).addAll(taggingService.getTaggings(ff.getDelegate()));
 				
 				// ...and their comments...
 				List<Comment> fragmentComments = getComments(ff.getResource());
 				for (ContentItem commentItem : commentService.listComments(ff.getDelegate())) {
 					String html = renderingService.renderHTML(commentItem);
 					fragmentComments.add(new Comment(commentItem.getTitle(), html, commentItem.getCreated(), commentItem.getAuthor(), false));
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method should be called in the saving transaction before the
 	 * savelets. It creates all the temporary content items and updated the
 	 * metadata.
 	 */
 	public void storeModel() {
 		ContentItemService contentItemService = (ContentItemService) Component
 				.getInstance("contentItemService");
 		TripleStore tripleStore = (TripleStore) Component
 				.getInstance("tripleStore");
 		TaggingService taggingService = (TaggingService) Component.getInstance("taggingService");
 		
 		EntityManager entityManager = (EntityManager)Component.getInstance("entityManager");
 		
 		CommentService commentService = (CommentService)Component.getInstance("commentService");
 		
 		Map<String, ContentItem> contentItemsCache = (Map<String, ContentItem>) Component.getInstance("storingService.contentItemCache");
 				
 		// we use the fresh user, so we avoid the detached entity problem...
 		User currentUser = (User)Component.getInstance("currentUser");
 		
 		Log log = Logging.getLog(EditorModel.class);
 		
 		// First, we create all the (nested) content items...
 		for (KiWiResource res : nestedid2resource.values()) {
 			// we know it is.. we have created it as such in the
 			// createNestedItem() method.
 			assert res.isUriResource();
 
 			KiWiUriResource uriRes = (KiWiUriResource) res;
 			ContentItem ci = contentItemService.createExternContentItem(uriRes
 					.getUri());
 			
 			ci.setAuthor(currentUser);
 			ci = contentItemService.saveContentItem(ci);
 			contentItemsCache.put(uriRes.getKiwiIdentifier(), ci);
 		}
 		
 		
 		KiWiUriResource fragmentType = tripleStore.createUriResource(Constants.NS_KIWI_SPECIAL + "Fragment");
 		KiWiUriResource fragmentOf = tripleStore.createUriResource(Constants.NS_KIWI_SPECIAL + "fragmentOf");
 		
 		// We also create all the fragments...
		for (KiWiResource res : fragmentid2resource.values()) {			
 			assert res.isUriResource();
 			
 			KiWiUriResource uriRes = (KiWiUriResource) res;
 			ContentItem ci = contentItemService.createExternContentItem(uriRes
 					.getUri());
 			
 			ci.setAuthor(currentUser);
 			ci.addType(fragmentType);
 			
 			ci = contentItemService.saveContentItem(ci);
 			contentItemsCache.put(uriRes.getKiwiIdentifier(), ci);
 		}
 		
 		// next, we link the fragments to their containing content items.
 		for (Map.Entry<KiWiResource, List<KiWiResource>> entry : fragments.entrySet()) {
 			ContentItem ci = getContentItemByKiWiId(entry.getKey().getKiwiIdentifier());
 			
 			for (KiWiResource fragmentResource : entry.getValue()) {
 				ContentItem fragment = getContentItemByKiWiId(fragmentResource.getKiwiIdentifier());
 				tripleStore.createTriple(fragment.getResource(), fragmentOf, ci.getResource());
 			}
 		}
 		
 		// now, we delete the fragments that should be deleted.
		for (KiWiResource fragmentResource : deletedFragments) {			
 			ContentItem ci = getContentItemByKiWiId(fragmentResource.getKiwiIdentifier());
 			if (ci != null) {
 				contentItemService.removeContentItem(ci);
 			}
 		}
 
 		// We update the titles of the (nested) items
 		for (Map.Entry<KiWiResource, String> entry : titles.entrySet()) {
 			ContentItem ci = getContentItemByKiWiId(entry.getKey().getKiwiIdentifier());
 
 			// if it does, something's wrong...
 			assert ci != null;
 
 			if (entry.getValue() != null
 					&& !entry.getValue().equals(ci.getTitle())) {
 				contentItemService.updateTitle(ci, entry.getValue());
 				contentItemService.saveContentItem(ci);
 			}
 		}
 
 		// And all the metadata (currently, only outgoing triples, but we don't
 		// modify anything else right now in the editor),
 		// 
 		// note that some of the metadata is extracted by the savelets...
 		for (Map.Entry<KiWiResource, List<KiWiTriple>> entry : outgoingTriples
 				.entrySet()) {
 			ContentItem ci = getContentItemByKiWiId(entry.getKey().getKiwiIdentifier());
 
 			// if it does, something's wrong...
 			assert ci != null;
 
 			for (KiWiTriple triple : entry.getValue()) {
 				// they may or may not be existing triples and if they do exist,
 				// they are almost surely detached...
 				
 				if (triple.isDeleted()) {
 					tripleStore.removeTriple(triple.getSubject(), triple.getProperty(), triple.getObject());
 				}
 				else {
 					if (!tripleStore.hasTriple(triple.getSubject(), triple.getProperty(), triple.getObject())) {
 						tripleStore.createTriple(triple.getSubject(), triple.getProperty(), triple.getObject());
 					}
 				}
 			}
 		}
 		
 		// Now, the tags. It has to be after the triples, because taggings
 		// also modify the triples... 
 		
 		// We delete the taggings that should be deleted
 		for (Tag tag : removedTags) {
 			// This should be true, because "removedTags" should only contain previously existing taggings.
 			assert tag.getId() != null;
 			
 			Tag t = taggingService.getTagById(tag.getId());
 			taggingService.removeTagging(t);
 		}
 		
 		// We add the taggings that should be created
 		for (Tag tag : addedTags) {
 			// The tags and its resources in "addedTags" are all virtual, only their kiwiids are meaningful,
 			// and the title of the tagging resource has been set in "createTaggingResource" method...
 			// also, some of the tagging resources may acutally exist, but most likely are detached.
 			
 			log.info("adding tag: " + tag.getLabel());
 			
 			String taggedResourceKiwiid = tag.getTaggedResource().getResource().getKiwiIdentifier();
 			ContentItem taggedResource = getContentItemByKiWiId(taggedResourceKiwiid);
 			assert taggedResource != null;
 			
 			String label = tag.getTaggingResource().getTitle();
 			
 			ContentItem taggingResource = null;
 			
 			String taggingResourceKiwiid = tag.getTaggingResource().getResource().getKiwiIdentifier();
 			
 			if (tag.getTaggingResource().getId() != null) {
 				// real taggingResource, make sure we have an attached one... 
 				taggingResource = getContentItemByKiWiId(taggingResourceKiwiid);
 				assert taggingResource != null;
 			}
 			else {
 				// the taggingResoure is a virtual one, let's create the real thing.
 				taggingResource = contentItemService.createContentItem("content/"+label.toLowerCase().replace(" ","_")+"/"+UUID.randomUUID().toString());
 				taggingResource.addType(tripleStore.createUriResource(Constants.NS_KIWI_CORE+"Tag"));
 				contentItemService.updateTitle(taggingResource, label);
 				taggingResource = contentItemService.saveContentItem(taggingResource);
 				contentItemsCache.put(taggingResource.getResource().getKiwiIdentifier(), taggingResource);
 			}
 						
 			taggingService.createTagging(label, taggedResource, taggingResource, currentUser);
 		}
 		
 		// We add the comments
 		for (Map.Entry<KiWiResource, List<Comment>> entry : comments.entrySet()) {
 			ContentItem ci = getContentItemByKiWiId(entry.getKey().getKiwiIdentifier());
 			for (Comment comment : entry.getValue()) {
 				if (comment.isNew()) {
 					commentService.createReply(ci, currentUser, comment.getTitle(), comment.getHtml());
 				}
 			}
 		}
 
 		// Well, that should be it...
 	}
 }
