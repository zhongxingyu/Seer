 package com.reckonlabs.reckoner.domain.content;
 
 import java.lang.reflect.Field;
 import java.util.Date;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.HashMap;
 
 import java.io.Serializable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Transient;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.reckonlabs.reckoner.domain.Notable;
 import com.reckonlabs.reckoner.domain.notes.Comment;
 import com.reckonlabs.reckoner.domain.notes.Favorite;
 import com.reckonlabs.reckoner.domain.user.User;
 
 @Entity
 @XmlRootElement(name = "content")
 public class Content extends Notable implements Serializable  {
 	
 	private static final long serialVersionUID = -2332260617574311916L;
 
 	private static final Logger log = LoggerFactory
 			.getLogger(Content.class);
 
 	@Id
 	private String id;
 	
 	@Column(name="content_type")
 	private ContentTypeEnum contentType;
 	
 	@Column(name="title")
 	private String title;
 	@Column(name="body")
 	private String body;
 	@Column(name="summary")
 	private String summary;
 	
 	@Column(name="submitter_id")
 	private String submitterId;
 
 	@Column(name="approved")
 	private Boolean approved;
 	@Column(name="rejected")
 	private Boolean rejected;
 	@Transient
 	private Boolean open;
 	
 	@Column(name="anonymous_requested")
 	private Boolean anonymousRequested;
 	@Column(name="anonymous")
 	private Boolean anonymous;
 	
 	@Column(name="posting_date")
 	private Date postingDate;
 	
 	@Column(name="comments")
 	private List<Comment> comments;
 	@Column(name="comment_index")
 	private Integer commentIndex;
 	
 	@Column(name="commentary")
 	private String commentary;
 	@Column(name="commentary_user_id")
 	private String commentaryUserId;
 	
 	// Used to store complete commentary and posting user information for display purposes.
 	// Do NOT store in the database;
 	@Transient
 	private User commentaryUser;
 	@Transient
 	private User postingUser;
 	
 	@Column(name="highlighted")
 	private Boolean highlighted;
 	
 	@Column(name="tags")
 	private List<String> tags;
 	
 	@Column (name="views")
 	private Integer views;
 	
 	@Column(name="random_select")
 	private Double randomSelect;
 
 	@XmlElement(name = "id")
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	@XmlElement(name = "content_type")
 	public ContentTypeEnum getContentType() {
 		return contentType;
 	}
 
 	public void setContentType(ContentTypeEnum contentType) {
 		this.contentType = contentType;
 	}
 
 	@XmlElement(name = "title")
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	
 	@XmlElement(name = "summary")	
 	public String getSummary() {
 		return summary;
 	}
 
 	public void setSummary(String summary) {
 		this.summary = summary;
 	}
 
 	@XmlElement(name = "body")
 	public String getBody() {
 		return body;
 	}
 
 	public void setBody(String body) {
 		this.body = body;
 	}
 
 	@XmlElement(name = "submitter_id")
 	public String getSubmitterId() {
 		return submitterId;
 	}
 
 	public void setSubmitterId(String submitterId) {
 		this.submitterId = submitterId;
 	}
 
 	@XmlElement(name = "approved")
 	public Boolean isApproved() {
 		return approved;
 	}
 
 	public void setApproved(Boolean approved) {
 		this.approved = approved;
 	}
 	
 	@XmlElement(name = "rejected")
 	public Boolean isRejected() {
 		return rejected;
 	}
 
 	public void setRejected(Boolean rejected) {
 		this.rejected = rejected;
 	}
 	
 	@XmlElement(name = "open")
 	public Boolean isOpen() {
 		if (isApproved() && !isRejected()) {
 			this.open = true;
 			return this.open;
 		}
 		
 		this.open = false;
 		return this.open;
 	}
 
 	@XmlElement(name = "anonymous_requested")
 	public Boolean isAnonymousRequested() {
 		return anonymousRequested;
 	}
 
 	public void setAnonymousRequested(Boolean anonymousRequested) {
 		this.anonymousRequested = anonymousRequested;
 	}
 
 	@XmlElement(name = "anonymous")
 	public Boolean isAnonymous() {
 		return anonymous;
 	}
 
 	public void setAnonymous(Boolean anonymous) {
 		this.anonymous = anonymous;
 	}
 
 	@XmlElement(name = "posting_date")
 	public Date getPostingDate() {
 		return postingDate;
 	}
 
 	public void setPostingDate(Date postingDate) {
 		this.postingDate = postingDate;
 	}
 
 	@XmlElementWrapper(name = "comments")
 	@XmlElement(name = "comment")
 	public List<Comment> getComments() {
 		return comments;
 	}
 
 	public void setComments(List<Comment> comments) {
 		this.comments = comments;
 	}
 	
 	public void addComment(Comment comment) {
 		if (this.comments == null) {
 			this.comments = new LinkedList<Comment>();
 		}
 		this.comments.add(comment);
 		
 		if (commentIndex != null) {
 			this.commentIndex++;
 		} else {
 			commentIndex = 1;
 		}
 	}
 	
 	public void setCommentIndex(Integer commentIndex) {
 		this.commentIndex = commentIndex;
 	}
 	
 	public Integer getCommentIndex() {
 		return this.commentIndex;
 	}
 
 	@XmlElement(name = "highlighted")
 	public Boolean isHighlighted() {
 		return highlighted;
 	}
 
 	public void setHighlighted(Boolean highlighted) {
 		this.highlighted = highlighted;
 	}
 
 	@XmlElementWrapper(name = "tags")
 	@XmlElement(name = "tag")
 	public List<String> getTags() {
 		return tags;
 	}
 
 	public void setTags(List<String> tags) {
 		this.tags = tags;
 	}
 	
 	@XmlElement(name = "commentary")
 	public String getCommentary() {
 		return commentary;
 	}
 
 	public void setCommentary(String commentary) {
 		this.commentary = commentary;
 	}
 
 	@XmlElement(name = "commentary_user_id")
 	public String getCommentaryUserId() {
 		return commentaryUserId;
 	}
 
 	public void setCommentaryUserId(String commentaryUserId) {
 		this.commentaryUserId = commentaryUserId;
 	}
 	
 	@XmlElement(name = "commentary_user")	
 	public User getCommentaryUser() {
 		return commentaryUser;
 	}
 	public void setCommentaryUser(User commentaryUser) {
 		this.commentaryUser = commentaryUser;
 	}
 
 	@XmlElement(name = "posting_user")
 	public User getPostingUser() {
 		return postingUser;
 	}
 	public void setPostingUser(User postingUser) {
 		this.postingUser = postingUser;
 	}
 	
 	@XmlElement(name = "views")
 	public Integer getViews() {
 		return views;
 	}
 
 	public void setViews(Integer views) {
 		this.views = views;
 	}
 	
 	public void incrementViews() {
 		if (this.views == null) {
 			this.views = 0;
 		}
 
 		this.views ++;
 	}
 
 	@XmlElement(name = "random_select")
 	public Double getRandomSelect() {
 		return randomSelect;
 	}
 
 	public void setRandomSelect(Double randomSelect) {
 		this.randomSelect = randomSelect;
 	}
 
 	// Gets the comments in this reckoning with a particular userId.
 	public List<Comment> getCommentsByUser (String userId) {
 		List<Comment> userComments = new LinkedList<Comment>();
 		
 		if (getComments() != null) {
 			for (Comment comment : getComments()) {
 				if (comment.getPosterId().equals(userId)) {
 					userComments.add(comment);
 				}
 			}
 		}
 		
 		return userComments;
 	}
 	
 	// Gets the comments favorited by this user for the particular ID.
 	public List<Comment> getFavoritedCommentsByUser (String userId) {
 		List<Comment> userComments = new LinkedList<Comment>();
 		
 		if (getComments() != null) {
 			for (Comment comment : getComments()) {
 				if (comment.getFavorites() != null) {
 					for (Favorite favorite : comment.getFavorites()) {
 						if (favorite.getUserId().equals(userId)) {
 							userComments.add(comment);
 						}
 					}
 				}
 			}
 		}
 		
 		return userComments;
 	}
 
 	// Gets the comments in this Reckoning with a particular commentId.
 	public List<Comment> getCommentById (String commentId) {
 		if (getComments() != null) {
 			for (Comment comment : getComments()) {
 				if (comment.getCommentId().equals(commentId)) {
 					List<Comment> returnList = new LinkedList<Comment> ();
 					returnList.add(comment);
 					return returnList;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	// Generates a HashMap of the Content as follows:
 	//   * Key: Element Name
 	//   * Value: Object value
 	// This is used when generating DB Merge queries as a way of quickly encapsulating the object.
 	public Map<String, Object> toHashMap() {
 		Map<String, Object> contentHash = new HashMap<String, Object> ();
 		
 		try {
 			for (Field field : Content.class.getDeclaredFields()) {
 				contentHash.put(field.getName(), field.get(this));
 			}
 			
 			contentHash.remove("log");
 			contentHash.remove("serialVersionUID");
 		} catch (Exception e) {
 			log.warn("Failed to marshal content " + this.id + " to hash map.", e);
 		}
 		
 		return contentHash;
 	}
 }
