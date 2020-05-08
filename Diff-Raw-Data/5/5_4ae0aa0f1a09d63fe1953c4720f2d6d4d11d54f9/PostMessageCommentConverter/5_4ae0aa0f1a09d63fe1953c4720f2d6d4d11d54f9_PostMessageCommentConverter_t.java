 package com.tissue.core.converter;
 
 import com.tissue.core.util.OrientIdentityUtil;
 import com.tissue.core.converter.PostMessageCommentConverter;
 import com.tissue.core.converter.PostMessageConverter;
 
 import com.tissue.domain.profile.User;
 import com.tissue.domain.plan.PostMessageComment;
 import com.tissue.domain.plan.PostMessage;
 import com.tissue.domain.plan.Post;
 
 import com.orientechnologies.orient.core.id.ORecordId;
 import com.orientechnologies.orient.core.record.impl.ODocument;
 
 import java.util.Date;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Set;
 
 public class PostMessageCommentConverter {
 
     public static ODocument convertPostMessageComment(PostMessageComment postMessageComment) {
         ODocument commentDoc = new ODocument("PostMessageComment");
         commentDoc.field("content", postMessageComment.getContent());
         commentDoc.field("createTime", postMessageComment.getCreateTime());
         commentDoc.field("user", new ORecordId(OrientIdentityUtil.decode(postMessageComment.getUser().getId())));
         commentDoc.field("postMessage", new ORecordId(OrientIdentityUtil.decode(postMessageComment.getPostMessage().getId())));
 
         return commentDoc;
     }
 
     public static PostMessageComment buildPostMessageComment(ODocument commentDoc) {
        
         String commentContent = commentDoc.field("content", String.class);
         Date commentCreateTime = commentDoc.field("createTime", Date.class);
 
         ODocument commentUserDoc = commentDoc.field("user");
         User commentUser = UserConverter.buildUser(commentUserDoc);
 
         PostMessageComment messageComment = new PostMessageComment();
        messageComment.setId(OrientIdentityUtil.encode(commentDoc.getIdentity().toString()));
         messageComment.setContent(commentContent);
         messageComment.setCreateTime(commentCreateTime);
         messageComment.setUser(commentUser);
  
         return messageComment;
     }
 
     public static List<PostMessageComment> buildPostMessageComments(Set<ODocument> commentsDoc) {
         List<PostMessageComment> messageComments = new ArrayList();
 
         for(ODocument commentDoc : commentsDoc) {
             PostMessageComment messageComment = buildPostMessageComment(commentDoc);
             messageComments.add(messageComment);
         }
 
         return messageComments;
     }
 
     public static PostMessageComment buildPostMessageCommentWithParent(ODocument commentDoc) {
         String commentContent = commentDoc.field("content", String.class);
         Date commentCreateTime = commentDoc.field("createTime", Date.class);
 
         ODocument commentUserDoc = commentDoc.field("user");
         User commentUser = UserConverter.buildUser(commentUserDoc);
 
         ODocument postMessageDoc = commentDoc.field("postMessage");
         PostMessage postMessage = PostMessageConverter.buildPostMessageWithoutChild(postMessageDoc);
 
         PostMessageComment messageComment = new PostMessageComment();
        messageComment.setId(OrientIdentityUtil.encode(commentDoc.getIdentity().toString()));
         messageComment.setContent(commentContent);
         messageComment.setCreateTime(commentCreateTime);
         messageComment.setUser(commentUser);
         messageComment.setPostMessage(postMessage);
  
         return messageComment;
     }
 
 
 }
