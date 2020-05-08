 package org.sakaiproject.clog.impl;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.io.Reader;
 
 import org.sakaiproject.clog.api.datamodel.Comment;
 import org.sakaiproject.clog.api.datamodel.Post;
 import org.sakaiproject.clog.api.ClogManager;
 import org.sakaiproject.entity.api.Entity;
 import org.sakaiproject.event.api.Event;
 import org.sakaiproject.search.api.EntityContentProducer;
 import org.sakaiproject.search.api.SearchIndexBuilder;
 import org.sakaiproject.search.api.SearchService;
 import org.sakaiproject.search.api.SearchUtils;
 import org.sakaiproject.search.util.HTMLParser;
 import org.sakaiproject.search.model.SearchBuilderItem;
 
 import org.apache.log4j.Logger;
 
 public class ClogContentProducer implements EntityContentProducer
 {
 	private ClogManager clogManager = null;
 	public void setClogManager(ClogManager clogManager)
 	{
 		this.clogManager = clogManager;
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
 	
 	private Logger logger = Logger.getLogger(ClogContentProducer.class);
 	
 	public void init()
 	{
 		searchService.registerFunction(ClogManager.CLOG_POST_CREATED);
 		searchService.registerFunction(ClogManager.CLOG_POST_DELETED);
 		searchService.registerFunction(ClogManager.CLOG_COMMENT_CREATED);
 		searchService.registerFunction(ClogManager.CLOG_COMMENT_DELETED);
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
 		
 		if(ClogManager.CLOG_POST_CREATED.equals(eventName)
 				|| ClogManager.CLOG_COMMENT_CREATED.equals(eventName))
 		{
 			return SearchBuilderItem.ACTION_ADD;
 		}
 		else if(ClogManager.CLOG_POST_DELETED.equals(eventName)
 				|| ClogManager.CLOG_COMMENT_DELETED.equals(eventName))
 		{
 			return SearchBuilderItem.ACTION_DELETE;
 		}
 		else
 			return SearchBuilderItem.ACTION_UNKNOWN;
 	}
 
 	public String getContainer(String ref)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("getContainer()");
 		
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
 		
 		try
 		{
 			Post post = clogManager.getPost(id);
 			
 			 StringBuilder sb = new StringBuilder();
 			 
 			 SearchUtils.appendCleanString(post.getTitle(),sb);
 			 
			 for (HTMLParser hp = new HTMLParser("<p>" + post.getContent() + "</p>"); hp.hasNext();)
 				 SearchUtils.appendCleanString(hp.next(), sb);
 		
 			 for(Comment comment : post.getComments())
 			 {	
				for (HTMLParser hp = new HTMLParser("<p>" + comment.getContent() + "</p>"); hp.hasNext();)
 					SearchUtils.appendCleanString(hp.next(), sb);
 			 }
 		
 			 return sb.toString();
 		}
 		catch(Exception e)
 		{
 			logger.error("Caught exception whilst getting content for post '" + id + "'",e);
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
 		return null;
 	}
 
 	public String getCustomRDF(String ref)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("getCustomRDF(" + ref + ")");
 		
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
 		
 		try
 		{
 			List<Post> posts = clogManager.getPosts(siteId);
 		
 			for(Post post : posts)
 				refs.add(post.getReference());
 		}
 		catch(Exception e)
 		{
 			logger.error("Caught exception whilst getting site content",e);
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
 			return id;
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
 		
 		try
 		{
 			Post post = clogManager.getPost(id);
 			return post.getTitle();
 		}
 		catch(Exception e)
 		{
 			logger.error("Caught exception whilst getting title for post '" + id + "'",e);
 		}
 	
 		return "Unrecognised";
 	}
 
 	public String getTool()
 	{
 		return "Clog";
 	}
 
 	public String getType(String ref)
 	{
 		if(logger.isDebugEnabled())
 			logger.debug("getType(" + ref + ")");
 		
 		return "clog";
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
 		
 		try
 		{
 			Post post = clogManager.getPost(id);
 			return post.getUrl();
 		}
 		catch(Exception e)
 		{
 			logger.error("Caught exception whilst getting url for post '" + id + "'",e);
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
 		
 		if(ClogManager.ENTITY_PREFIX.equals(parts[1]))
 			return true;
 		
 		return false;
 	}
 
 	public boolean matches(Event event)
 	{
 		String eventName = event.getEvent();
 		
 		if(ClogManager.CLOG_POST_CREATED.equals(eventName)
 				|| ClogManager.CLOG_POST_DELETED.equals(eventName)
 				|| ClogManager.CLOG_COMMENT_CREATED.equals(eventName)
 				|| ClogManager.CLOG_COMMENT_DELETED.equals(eventName))
 		{
 			return true;
 		}
 			
 		return false;
 	}
 }
