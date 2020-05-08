 package com.reckonlabs.reckoner.contentservices.service;
 
 import java.lang.Boolean;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.reckonlabs.reckoner.contentservices.cache.ContentCache;
 import com.reckonlabs.reckoner.contentservices.repo.ContentRepo;
 import com.reckonlabs.reckoner.contentservices.repo.ContentRepoCustom;
 import com.reckonlabs.reckoner.domain.ApprovalStatusEnum;
 import com.reckonlabs.reckoner.domain.content.Content;
 import com.reckonlabs.reckoner.domain.content.ContentTypeEnum;
 import com.reckonlabs.reckoner.domain.message.ContentServiceList;
 import com.reckonlabs.reckoner.domain.message.Message;
 import com.reckonlabs.reckoner.domain.message.MessageEnum;
 import com.reckonlabs.reckoner.domain.message.ServiceResponse;
 import com.reckonlabs.reckoner.domain.message.TagServiceList;
 import com.reckonlabs.reckoner.domain.notes.Comment;
 import com.reckonlabs.reckoner.domain.notes.Tag;
 import com.reckonlabs.reckoner.domain.reckoning.Reckoning;
 import com.reckonlabs.reckoner.domain.utility.DBUpdateException;
 import com.reckonlabs.reckoner.domain.utility.DateUtility;
 
 @Component
 public class ContentServiceImpl implements ContentService {
 	
 	@Autowired
 	ContentRepo contentRepo;
 	
 	@Autowired
 	ContentRepoCustom contentRepoCustom;
 	
 	@Autowired
 	ContentCache contentCache;
 	
 	@Autowired
 	UserService userService;
 	
 	private static final Logger log = LoggerFactory
 			.getLogger(ContentServiceImpl.class);
 	
 	@Override
 	public ServiceResponse postContent(Content content) {
 		try {
 			// Clean up the content for fields that can't be set for new postings.
 			content.setId(null);
 			content.setApproved(true);
 			content.setRejected(false);
 			content.setPostingDate(DateUtility.now());
 			content.setFlags(null);
 			content.setFavorites(null);
 			content.setComments(null);
 			content.setCommentIndex(0);
 			content.setViews(0);
 			
 			// Format the tags and add the random selector.
 			content.setRandomSelect(new Random().nextDouble());
 			content.setTags(formatTags(content.getTags()));
 			
 			contentRepoCustom.insertNewContent(content);
 			contentCache.removeCachedTagList();
 		} catch (Exception e) {
 			log.error("General exception when inserting new content: " + e.getMessage());
 			log.debug("Stack Trace:", e);			
 			return (new ServiceResponse(new Message(MessageEnum.R01_DEFAULT), false));	
 		}
 		
 		return new ServiceResponse();
 	}
 
 	@Override
 	public ServiceResponse updateContent(Content content, boolean merge) {
 		try {
 			// Clean up the tags (if any).
 			content.setTags(formatTags(content.getTags()));
 			
 			if (merge) {
 				if (contentRepoCustom.confirmContentExists(content.getId())) {					
 					contentRepoCustom.mergeContent(content);
 				}
 				else {
 					return (new ServiceResponse(new Message(MessageEnum.R106_POST_RECKONING), false));
 				}
 			} else {
 				contentRepoCustom.updateContent(content);
 			}
 			
 			contentCache.removeCachedContent(content.getId());
 			contentCache.removeCachedTagList();
 		} catch (Exception e) {
 			log.error("General exception when updating content: " + e.getMessage());
 			log.debug("Stack Trace:", e);			
 			return (new ServiceResponse(new Message(MessageEnum.R01_DEFAULT), false));	
 		}
 		
 		return new ServiceResponse();
 	}
 
 	@Override
 	public ContentServiceList getContent(String id) {
 		return getContent(id, false, false);
 	}
 
 	@Override
 	public ContentServiceList getContent(String id, boolean includeUnaccepted,
 			boolean pageVisit) {
 		List<Content> contentList = null;
 		try {
 			// Check the caches to see if the Content has already been pulled.  If so, there you go.
 			contentList = contentCache.getCachedContent(id);
 			
 			// If this is a 'page visit', (i.e. a unique display of this Content on an end client), increment
 			// the views value for this Content in the DB.  Also, update the value returned from the cache.
 			if (pageVisit) {
 				contentRepoCustom.incrementContentViews(id);
 				if (contentList != null && !contentList.isEmpty()) {
 					contentList.get(0).incrementViews();
 					contentCache.setCachedContent(contentList, id);
 				}
 			}
 			
 			// If not, pull it (excluding rejected reckonings as specified).
 			if (contentList == null) {
 				if (includeUnaccepted) {
 					contentList = contentRepo.findById(id);
 				} else {
 					contentList = contentRepo.findByIdAndApproved(id, true);
 				}
 				
 				if (contentList != null && !contentList.isEmpty()) {
 					// Iterate through the comments to add the pertinent user information necessary to render them.
 					if (contentList.get(0).getComments() != null) {
 						for (Comment comment : contentList.get(0).getComments()) {
 							comment.setUser(userService.getUserByUserId(comment.getPosterId(), true).getUser());
 						}
 					}
 					// Get the summary for the Commentary user
 					if (contentList.get(0).getCommentaryUserId() != null) {
 						contentList.get(0).setCommentaryUser(userService.getUserByUserId
 								(contentList.get(0).getCommentaryUserId(), true).getUser());
 					}
 					// Get the summary for the Posting user
 					if (contentList.get(0).getSubmitterId() != null) {
 						contentList.get(0).setPostingUser(userService.getUserByUserId
 								(contentList.get(0).getSubmitterId(), true).getUser());
 					}					
 					
 					contentCache.setCachedContent(contentList, id);
 				}
 			}
 		} catch (Exception e) {
 			log.error("General exception when retrieving content: " + e.getMessage());
 			log.debug("Stack Trace:", e);	
 			return new ContentServiceList(null, new Message(MessageEnum.R01_DEFAULT), false);
 		}	
 		
 		return new ContentServiceList(contentList, new Message(), true);
 	}
 
 	@Override
 	public ContentServiceList getContentSummaries(ContentTypeEnum contentType,
 			Date postedAfter, Date postedBefore, List<String> includeTags,
 			String submitterId, ApprovalStatusEnum approvalStatus,
 			String sortBy, Boolean ascending, Integer page, Integer size,
 			Boolean randomize) {
 		List<Content> contents = null;
 		Long count = null;
 		
 		try {
 			includeTags = formatTags(includeTags);
 			
 			// First, try fetching out of the cache according to the specified criteria.
 			contents = contentCache.getCachedContentSummaries(contentType, postedAfter, postedBefore, includeTags, 
 					submitterId, approvalStatus, sortBy, ascending, page, size);
 			
 			// Nothing in the cache.  Poll the DB and stow it.
 			if (contents == null) {
 				contents = contentRepoCustom.getContentItems(contentType, postedBefore, postedAfter, includeTags, submitterId, 
 						approvalStatus, sortBy, ascending, page, size, randomize);
 				
 				for (Content content : contents) {
 					content.setPostingUser(userService.getUserByUserId
 									(content.getSubmitterId(), true).getUser());
 				}
 				
 				contentCache.setCachedContentSummaries(contentType, postedAfter, postedBefore, includeTags, 
 						submitterId, approvalStatus, sortBy, ascending, contents, page, size);
 			}
 			
 			count = getContentCount(contentType, postedAfter, postedBefore, includeTags, submitterId, approvalStatus).getCount();
 		}
 		catch (Exception e) {
 			log.error("General exception when getting content summaries: " + e.getMessage());
 			log.debug("Stack Trace:", e);
 			return new ContentServiceList(null, new Message(MessageEnum.R01_DEFAULT), false);
 		}
 		
 		return new ContentServiceList(contents, count, new Message(), true);
 	}
 
 	@Override
 	public ContentServiceList getContentCount(ContentTypeEnum contentType,
 			Date postedAfter, Date postedBefore, List<String> includeTags,
 			String submitterId, ApprovalStatusEnum approvalStatus) {
 		Long count = null;
 		
 		try {
 			count = contentCache.getCachedContentCount(contentType, postedAfter, postedBefore, 
 					includeTags, submitterId, approvalStatus);
 			
 			if (count == null) {
 				count = contentRepoCustom.getContentCount(contentType, postedBefore, 
 					postedAfter, includeTags, submitterId, approvalStatus);
 				
 				contentCache.setCachedContentCount(contentType, postedAfter, postedBefore, 
 						includeTags, submitterId, approvalStatus, count);
 			}
 		} catch (Exception e) {
 			log.error("General exception when getting content count: " + e.getMessage());
 			log.debug("Stack Trace:", e);
 			return new ContentServiceList(null, new Message(MessageEnum.R01_DEFAULT), false);
 		}
 		
 		return new ContentServiceList(null, count, new Message(), true);
 	}
 	
 	@Override
 	public ServiceResponse rejectContent(String id, String sessionId) {
 		try {
 			List<Content> rejectedContent = contentRepo.findById(id);
 			
 			if (rejectedContent != null && rejectedContent.size() > 0) {
 				contentRepoCustom.rejectContent(id, userService.getUserBySessionId(sessionId).getUser().getId());
 				contentCache.removeCachedContent(id);
 				contentCache.removeCachedTagList();
 			} else {
 				log.info("Request to reject non-existent content: " + id);
 				return (new ServiceResponse(new Message(MessageEnum.R300_APPROVE_RECKONING), false));					
 			}
 			
 		} catch (DBUpdateException dbE) {
 			log.error("Database exception when rejecting a content: " + dbE.getMessage());
 			log.debug("Stack Trace:", dbE);			
 			return (new ServiceResponse(new Message(MessageEnum.R01_DEFAULT), false));				
 		}
 		  catch (Exception e) {
 			log.error("General exception when rejecting a content: " + e.getMessage());
 			log.debug("Stack Trace:", e);			
 			return (new ServiceResponse(new Message(MessageEnum.R01_DEFAULT), false));	
 		}
 		
 		return new ServiceResponse();
 	}
 	
 	@Override
 	public TagServiceList getTagList() {
 		List<Tag> tagList = null;
 		
 		try {
 			// This should ultimately be a Mongo Map-Reduce call, but its performance is poor and the data set isn't
 			// huge, so we're doing it through Java.
 			
 			tagList = contentCache.getCachedTagList();
 			
 			if (tagList == null) {
 				tagList = new LinkedList<Tag>();
 				
				List<Content> allContent = this.getContentSummaries(null, null, null, null, null, 
						null, null, null, null, null, null).getContents();
 				
 				if (allContent != null) {
 					HashMap<String, Integer> tagHash = new HashMap<String, Integer>();
 					
 					for (Content content : allContent) {
 						if (content.getTags() != null) {
 							for (String tag : content.getTags()) {
 								if (!tagHash.containsKey(tag)) {
 									tagHash.put(tag, 1);
 								} else {
 									tagHash.put(tag, tagHash.get(tag) + 1);
 								}
 							}
 						}
 					}
 					
 					for (Map.Entry<String, Integer> entry : tagHash.entrySet()) {
 					    tagList.add(new Tag(entry.getKey(), entry.getValue()));
 					}
 				}
 				
 				contentCache.setCachedTagList(tagList);
 			}
 			
 		} catch (Exception e) {
 			log.error("General exception when getting the content tag list: " + e.getMessage());
 			log.debug("Stack Trace:", e);			
 			return (new TagServiceList(null, new Message(MessageEnum.R01_DEFAULT), false));	
 		}
 		
 		return new TagServiceList(tagList, null, new Message(), true);
 	}
 	
 	private static List<String> formatTags(List<String> tags) {
 		List<String> formattedTags = null;
 		
 		if (tags != null) {
 			formattedTags = new LinkedList<String> ();
 			for (String tag : tags) {
 				formattedTags.add(tag.trim().toLowerCase());
 			}
 		}
 		
 		return formattedTags;
 	}
 }
