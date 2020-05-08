 package org.migus.web.content.rest;
 
 import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
 import static javax.ws.rs.core.Response.Status.CONFLICT;
 import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
 import static javax.ws.rs.core.Response.Status.NOT_FOUND;
 
 import java.util.UUID;
 
 import javax.ws.rs.WebApplicationException;
 import javax.xml.datatype.DatatypeConfigurationException;
 
 import org.migus.web.data.ContentDao;
 import org.migus.web.data.ContentExistsException;
 import org.migus.web.data.ContentNotFoundException;
 import org.migus.web.data.ContentDao.ContentData;
 import org.migus.web.content.ContentBuilder;
 import org.migus.web.content.types.Content;
 import org.migus.web.content.types.Contents;
 import org.migus.web.content.types.Ids;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service
 public class ContentServerImpl implements ContentServer {
 	private ContentDao contentDao;
 	private Logger logger;
 	
 	Content convertContentData(ContentData contentData) {
 		Content content=null;
 		String message="converting contentData to content";
 		
 		logger.debug(message);
 		try {
 			content=ContentBuilder.fromContentData(contentData).build();
 		} catch (DatatypeConfigurationException e) {
 			logger.error(message, e);
 			throw new WebApplicationException(INTERNAL_SERVER_ERROR);
 		}
 		logger.debug("content = "+content);
 		return content;
 	}
 	
 	public ContentServerImpl(ContentDao contentDao, Logger logger) {
 		this.contentDao=contentDao;
 		this.logger=logger;
 	}
 
 	@Autowired
 	public ContentServerImpl(ContentDao contentDao) {
 		this(contentDao, LoggerFactory.getLogger(ContentServerImpl.class));
 	}
 
 	private Content add(UUID id, Content newContent) {
 		String message="adding content with newId = "+id+"; newContent ="+
 				newContent;
 		ContentData contentData=null;
 
 		logger.debug(message);
 		try {
 			contentDao.insertContent(id, newContent.getAuthor(), 
 					newContent.getTitle(), newContent.getText());
 		} catch (ContentExistsException e) {
 			assert(id.equals(e.getId()));
 			contentData=contentDao.getContent(id);
 			if (contentData == null) {
 				logger.error("contentData is null for existing id "+id);
 				throw new WebApplicationException(INTERNAL_SERVER_ERROR);
 			}
			throw new WebApplicationException(CONFLICT);
 		}
 		if (contentData == null) {
 			contentData=contentDao.getContent(id);
 		}
 		
 		Content content=null;
 		
 		if (contentData != null) {
 			content = convertContentData(contentData);
 		}
 		logger.debug("content = "+content);
 		return content;
 	}
 
 	public Content add(Content newContent) {
 		return this.add(UUID.randomUUID().toString(), newContent);
 	}
 
 	public Content add(String newId, Content newContent) {
 		try {
 			return this.add(UUID.fromString(newId), newContent);
 		} catch (IllegalArgumentException e) {
 			throw new WebApplicationException(BAD_REQUEST);
 		}
 	}
 
 	public Contents getByAuthor(String author) {
 		Contents contents=new Contents();
 		String message="getting contents for author "+author;
 
 		logger.debug(message);
 		for (ContentData contentData : contentDao.getContentsByAuthor(author)) {
 			contents.getContents().add(convertContentData(contentData));
 		}
 		logger.debug("contents.getContents().size() = "+
 				contents.getContents().size());
 		return contents;
 	}
 
 	public Content get(String id) {
 		ContentData contentData;
 		UUID uuid;
 		String message="getting content for id "+id;
 
 		logger.debug(message);
 		try {
 			uuid=UUID.fromString(id);
 		} catch (IllegalArgumentException e) {
 			throw new WebApplicationException(BAD_REQUEST);
 		}
 		assert(uuid != null);
 		contentData=contentDao.getContent(uuid);
 		if (contentData == null) {
 			logger.debug("contentData was null when "+message);
 			throw new WebApplicationException(NOT_FOUND);
 		}
 		
 		Content content=convertContentData(contentData);
 		
 		logger.debug("content = "+content);
 		return content;
 	}
 
 	public Ids getIds() {
 		Ids ids = new Ids();
 		for (UUID uuid : contentDao.getIds()) {
 			ids.getIds().add(uuid.toString());
 		}
 		return ids;
 	}
 
 	public String updateText(String id, String text) {
 		ContentData content=contentDao.getContent(UUID.fromString(id));
 		String message="updating text for content with id "+id;
 		
 		logger.debug(message);
 		content.setText(text);
 		try {
 			contentDao.updateContent(content);
 		} catch (ContentNotFoundException e) {
 			logger.debug(message, e);
 			throw new WebApplicationException(NOT_FOUND);
 		}
 		return text;
 	}
 
 	public String updateTitle(String id, String title) {
 		ContentData content=contentDao.getContent(UUID.fromString(id));
 		String message="updating title for content with id "+id;
 		
 		logger.debug(message);
 		content.setTitle(title);
 		try {
 			contentDao.updateContent(content);
 		} catch (ContentNotFoundException e) {
 			logger.debug(message, e);
 			throw new WebApplicationException(NOT_FOUND);
 		}
 		return title;
 	}
 }
