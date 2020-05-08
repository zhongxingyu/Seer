 package org.sakaiproject.yaft.impl;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.io.Reader;
 
 import org.sakaiproject.entity.api.Entity;
 import org.sakaiproject.event.api.Event;
 import org.sakaiproject.search.api.EntityContentProducer;
 import org.sakaiproject.search.api.SearchIndexBuilder;
 import org.sakaiproject.search.api.SearchService;
 import org.sakaiproject.search.api.SearchUtils;
 import org.sakaiproject.search.model.SearchBuilderItem;
 import org.sakaiproject.search.util.HTMLParser;
 import org.sakaiproject.yaft.api.Discussion;
 import org.sakaiproject.yaft.api.Forum;
 import org.sakaiproject.yaft.api.ForumPopulatedStates;
 import org.sakaiproject.yaft.api.Message;
 import org.sakaiproject.yaft.api.YaftForumService;
 
 import org.apache.log4j.Logger;
 
 public class YaftContentProducer implements EntityContentProducer
 {
 	private YaftForumService forumService = null;
 	public void setForumService(YaftForumService forumService)
 	{
 		this.forumService = forumService;
 	}
 	
 	private SearchService searchService = null;
 	public void setSearchService(SearchService searchService)
 	{
 		this.searchService = searchService;
 	}
 	
 	private SearchIndexBuilder searchIndexBuilder = null;
 	public void setSearchIndexBuilder(SearchIndexBuilder searchIndexBuilder)
 	{
 		this.searchIndexBuilder = searchIndexBuilder;
 	}
 	
 	private Logger logger = Logger.getLogger(YaftContentProducer.class);
 	
 	public void init()
 	{
 		searchService.registerFunction(YaftForumService.YAFT_MESSAGE_CREATED);
 		searchService.registerFunction(YaftForumService.YAFT_MESSAGE_DELETED);
 		searchService.registerFunction(YaftForumService.YAFT_DISCUSSION_CREATED);
 		searchService.registerFunction(YaftForumService.YAFT_DISCUSSION_DELETED);
 		searchService.registerFunction(YaftForumService.YAFT_FORUM_CREATED);
 		searchService.registerFunction(YaftForumService.YAFT_FORUM_DELETED);
 		searchIndexBuilder.registerEntityContentProducer(this);
 	}
 	
 	public boolean canRead(String reference)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("canRead()");
 		
 		// TODO: sort this !
 		return true;
 	}
 
 	public Integer getAction(Event event)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("getAction()");
 		
 		String eventName = event.getEvent();
 		if(YaftForumService.YAFT_MESSAGE_CREATED.equals(eventName)
 				|| YaftForumService.YAFT_DISCUSSION_CREATED.equals(eventName)
 				|| YaftForumService.YAFT_FORUM_CREATED.equals(eventName))
 			return SearchBuilderItem.ACTION_ADD;
 		else if(YaftForumService.YAFT_MESSAGE_DELETED.equals(eventName)
 				|| YaftForumService.YAFT_DISCUSSION_DELETED.equals(eventName)
 				|| YaftForumService.YAFT_FORUM_DELETED.equals(eventName))
 			return SearchBuilderItem.ACTION_DELETE;
 		else
 			return SearchBuilderItem.ACTION_UNKNOWN;
 	}
 
 	public List getAllContent()
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("getAllContent()");
 		
 		List refs = new ArrayList();
 		
 		List<Forum> fora = forumService.getFora(true);
 		for(Forum forum : fora)
 		{
 			List<Discussion> discussions = forum.getDiscussions();
 			
 			for(Discussion discussion : discussions)
 			{
 				Message firstMessage = discussion.getFirstMessage();
 				recursivelyAddMessageRefs(firstMessage,refs);
 			}
 		}
 		
 		return refs;
 	}
 	
 	private void recursivelyAddMessageRefs(Message parent,List refs)
 	{
 		refs.add(parent.getReference());
 		
 		List<Message> children = parent.getChildren();
 		
 		for(Message child : children)
 			recursivelyAddMessageRefs(child, refs);
 	}
 
 	public String getContainer(String ref)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("getContainer()");
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String getContent(String ref)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("getContent(" + ref + ")");
 		
 		String[] parts = ref.split(Entity.SEPARATOR);
 		
 		String type = parts[2];
 		String id = parts[3];
 		
 		if(parts.length == 5)
 		{
 			type = parts[3];
 			id = parts[4];
 		}
 		
 		if("messages".equals(type))
 		{
 			Message message = forumService.getMessage(id);
 			
 			String content = "<p>" + message.getContent() + "</p>";
 			
 			StringBuilder sb = new StringBuilder();
 			
 			SearchUtils.appendCleanString(message.getSubject(), sb);
 			 
 			for (HTMLParser hp = new HTMLParser(content); hp.hasNext();)
 				 SearchUtils.appendCleanString(hp.next(), sb);
 			
 			return sb.toString();
 		}
 		else if("discussions".equals(type))
 		{
 			Discussion discussion = forumService.getDiscussion(id, false);
 			if(discussion == null)
 			{
 				logger.error("No discussion for id: " + id + ". Returning an empty title ...");
 				return "";
 			}
 			else
 			{
 				// Wrap in p tags otherwise search may snip the last char off.
 				String content = "<p>" + discussion.getContent() + "</p>";
 				
 				StringBuilder sb = new StringBuilder();
 			
 				SearchUtils.appendCleanString(discussion.getSubject(), sb);
 			 
 				for (HTMLParser hp = new HTMLParser(content); hp.hasNext();)
 					SearchUtils.appendCleanString(hp.next(), sb);
 				
 				return sb.toString();
 			}
 		}
 		else if("forums".equals(type))
 		{
 			Forum forum = forumService.getForum(id,ForumPopulatedStates.EMPTY);
 			if(forum == null)
 			{
 				logger.error("No forum for id: " + id + ". Returning an empty title ...");
 				return "";
 			}
 			else
 			{
 				StringBuilder sb = new StringBuilder();
 			
 				SearchUtils.appendCleanString(forum.getTitle(), sb);
 				return sb.toString();
 			}
 		}
 		
 		return null;
 	}
 
 	public Reader getContentReader(String ref)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("getContentReader(" + ref + ")");
 		
 		return null;
 	}
 
 	public Map getCustomProperties(String ref)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("getCustomProperties(" + ref + ")");
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String getCustomRDF(String ref)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("getCustomRDF(" + ref + ")");
 		
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String getId(String ref)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("getId(" + ref + ")");
 		
 		String[] parts = ref.split(Entity.SEPARATOR);
 		
 		if(parts.length == 4)
 		{
 			return parts[3];
 		}
 		else if(parts.length == 5)
 		{
 			return parts[4];
 		}
 		
 		return "unknown";
 	}
 
 	public List getSiteContent(String siteId)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("getSiteContent(" + siteId + ")");
 		
 		List refs = new ArrayList();
 		
 		List<Forum> fora = forumService.getSiteForums(siteId, true);
 		for(Forum forum : fora)
 		{
 			List<Discussion> discussions = forum.getDiscussions();
 			
 			for(Discussion discussion : discussions)
 			{
 				Message firstMessage = discussion.getFirstMessage();
 				recursivelyAddMessageRefs(firstMessage,refs);
 			}
 		}
 		
 		return refs;
 	}
 
 	public Iterator getSiteContentIterator(String siteId)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("getSiteContentIterator(" + siteId + ")");
 		
 		return getSiteContent(siteId).iterator();
 	}
 
 	public String getSiteId(String eventRef)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("getSiteId(" + eventRef + ")");
 		
 		String[] parts = eventRef.split(Entity.SEPARATOR);
 		if(parts.length == 4)
 		{
 			String id = parts[3];
 			return forumService.getIdOfSiteContainingMessage(id);
 		}
 		else if(parts.length == 5)
 		{
 			String siteId = parts[2];
 			return siteId;
 		}
 		
 		return null;
 	}
 
 	public String getSubType(String ref)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("getSubType(" + ref + ")");
 		
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String getTitle(String ref)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("getTitle(" + ref + ")");
 		
 		String[] parts = ref.split(Entity.SEPARATOR);
 		String type = parts[2];
 		String id = parts[3];
 		
 		if(parts.length == 5)
 		{
 			type = parts[3];
 			id = parts[4];
 		}
 		
 		if("messages".equals(type))
 		{
 			Message message = forumService.getMessage(id);
 			return message.getSubject();
 		}
 		
 		else if("discussions".equals(type))
 		{
 			Discussion discussion = forumService.getDiscussion(id, false);
 			if(discussion == null)
 			{
 				logger.error("No discussion for id: " + id + ". Returning an empty title ...");
 				return "";
 			}
 			else
 				return discussion.getSubject();
 		}
 		else if("forums".equals(type))
 		{
 			Forum forum = forumService.getForum(id,ForumPopulatedStates.EMPTY);
 			if(forum == null)
 			{
 				logger.error("No forum for id: " + id + ". Returning an empty title ...");
 				return "";
 			}
 			else
 				return forum.getTitle();
 		}
 		
 		return "Unrecognised";
 	}
 
 	public String getTool()
 	{
 		return "Discussions";
 	}
 
 	public String getType(String ref)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("getType(" + ref + ")");
 		
 		return "sakai.yaft";
 	}
 
 	public String getUrl(String ref)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("getUrl(" + ref + ")");
 		
 		String[] parts = ref.split(Entity.SEPARATOR);
 		String type = parts[2];
 		String id = parts[3];
 		
 		if(parts.length == 5)
 		{
 			type = parts[3];
 			id = parts[4];
 		}
 		
 		if("messages".equals(type))
 		{
 			Message message = forumService.getMessage(id);
 			return message.getUrl();
 		}
 		else if("discussions".equals(type))
 		{
 			Discussion discussion = forumService.getDiscussion(id,false);
 			return discussion.getUrl();
 		}
 		if("forums".equals(type))
 		{
 			Forum forum = forumService.getForum(id,ForumPopulatedStates.EMPTY);
			return forum.getUrl();
 		}
 		
 		return null;
 	}
 
 	public boolean isContentFromReader(String ref)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("isContentFromReader(" + ref + ")");
 		
 		return false;
 	}
 
 	public boolean isForIndex(String ref)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("isForIndex(" + ref + ")");
 		
 		return true;
 	}
 
 	public boolean matches(String ref)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("matches(" + ref + ")");
 		
 		String[] parts = ref.split(Entity.SEPARATOR);
 		
 		if(YaftForumService.ENTITY_PREFIX.equals(parts[1]))
 			return true;
 		
 		return false;
 	}
 
 	public boolean matches(Event event)
 	{
 		String eventName = event.getEvent();
 		
 		if(YaftForumService.YAFT_MESSAGE_CREATED.equals(eventName)
 				|| YaftForumService.YAFT_MESSAGE_DELETED.equals(eventName)
 				|| YaftForumService.YAFT_DISCUSSION_CREATED.equals(eventName)
 				|| YaftForumService.YAFT_DISCUSSION_DELETED.equals(eventName)
 				|| YaftForumService.YAFT_FORUM_CREATED.equals(eventName)
 				|| YaftForumService.YAFT_FORUM_DELETED.equals(eventName))
 			return true;
 			
 		return false;
 	}
 }
