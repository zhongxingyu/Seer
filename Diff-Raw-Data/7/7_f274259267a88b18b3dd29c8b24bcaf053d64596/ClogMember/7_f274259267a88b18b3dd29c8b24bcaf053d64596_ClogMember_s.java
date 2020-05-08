 package org.sakaiproject.clog.api;
 
 import java.net.URLEncoder;
 
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.util.FormattedText;
 
 public class ClogMember {
     private int numberOfPosts = 0;
 
     private int numberOfComments = 0;
 
     private long dateOfLastPost = -1L;
 
     private long dateOfLastComment = -1L;
 
     private String lastCommentCreator = "";
 
     private transient User sakaiUser = null;
 
     public ClogMember() {
     }
 
     public ClogMember(User user) {
 	this.sakaiUser = user;
     }
 
     public String getUserId() {
 	return sakaiUser.getId();
     }
 
     public String getUserEid() {
 	return sakaiUser.getEid();
     }
 
     public String getUserDisplayName() {
	// CLOG-24
	try {
	    return URLEncoder.encode(sakaiUser.getLastName(),"UTF-8") + ", " + URLEncoder.encode(sakaiUser.getFirstName(),"UTF-8");
	}catch(Exception e) {
	    return null;
	}
     }
 
     public void setNumberOfPosts(int numberOfPosts) {
 	this.numberOfPosts = numberOfPosts;
     }
 
     public int getNumberOfPosts() {
 	return numberOfPosts;
     }
 
     public void setDateOfLastPost(long last) {
 	this.dateOfLastPost = last;
     }
 
     public long getDateOfLastPost() {
 	return dateOfLastPost;
     }
 
     public void setDateOfLastComment(long dateOfLastComment) {
 	this.dateOfLastComment = dateOfLastComment;
     }
 
     public long getDateOfLastComment() {
 	return dateOfLastComment;
     }
 
     public void setLastCommentCreator(String lastCommentCreator) {
 	this.lastCommentCreator = lastCommentCreator;
     }
 
     public String getLastCommentCreator() {
 	return lastCommentCreator;
     }
 
     public void setNumberOfComments(int numberOfComments) {
 	this.numberOfComments = numberOfComments;
     }
 
     public int getNumberOfComments() {
 	return numberOfComments;
     }
 }
