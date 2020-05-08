 /*
  * Copyright (c) 2003, Rafael Steil
  * All rights reserved.
 
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
 
  * 1) Redistributions of source code must retain the above 
  * copyright notice, this list of conditions and the 
  * following  disclaimer.
  * 2)  Redistributions in binary form must reproduce the 
  * above copyright notice, this list of conditions and 
  * the following disclaimer in the documentation and/or 
  * other materials provided with the distribution.
  * 3) Neither the name of "Rafael Steil" nor 
  * the names of its contributors may be used to endorse 
  * or promote products derived from this software without 
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
  * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
  * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
  * 
  * This file creation date: Mar 28, 2003 / 22:57:43 PM
  * The JForum Project
  * http://www.jforum.net
  */
 package net.jforum.drivers.generic;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 
 import net.jforum.JForum;
 import net.jforum.entities.Post;
 import net.jforum.model.DataAccessDriver;
 import net.jforum.util.preferences.ConfigKeys;
 import net.jforum.util.preferences.SystemGlobals;
 
 /**
  * @author Rafael Steil
  * @author Vanessa Sabino
 * @version $Id: PostModel.java,v 1.5 2004/06/02 03:56:04 rafaelsteil Exp $
  */
 public class PostModel extends AutoKeys implements net.jforum.model.PostModel 
 {
 	/**
 	 * @see net.jforum.model.PostModel#selectById(int)
 	 */
 	public Post selectById(int PostId) throws Exception 
 	{
 		PreparedStatement p = JForum.getConnection().prepareStatement(SystemGlobals.getSql("PostModel.selectById"));		
 		p.setInt(1, PostId);
 		
 		ResultSet rs = p.executeQuery();
 		
 		Post post = new Post();
 		
 		if (rs.next()) {
 			post = this.makePost(rs);
 		}
 		
 		rs.close();
 		p.close();
 		
 		return post;
 	}
 	
 	protected Post makePost(ResultSet rs) throws Exception
 	{
 		Post post = new Post();
 		post.setId(rs.getInt("post_id"));
 		post.setTopicId(rs.getInt("topic_id"));
 		post.setForumId(rs.getInt("forum_id"));
 		post.setUserId(rs.getInt("user_id"));
 		post.setTime(rs.getLong("post_time"));
 		post.setUserIp(rs.getString("poster_ip"));
 		post.setBbCodeEnabled(rs.getInt("enable_bbcode") > 0);
 		post.setHtmlEnabled(rs.getInt("enable_html") > 0);
 		post.setSmiliesEnabled(rs.getInt("enable_smilies") > 0);
 		post.setSignatureEnabled(rs.getInt("enable_sig") > 0);
 		post.setEditTime(rs.getLong("post_edit_time"));
 		post.setEditCount(rs.getInt("post_edit_count"));
 		post.setSubject(rs.getString("post_subject"));
 		post.setText(rs.getString("post_text"));
 		post.setPostUsername(rs.getString("username"));
 		
 		SimpleDateFormat df = new SimpleDateFormat(SystemGlobals.getValue(ConfigKeys.DATE_TIME_FORMAT));
 		GregorianCalendar gc = new GregorianCalendar();
 		gc.setTimeInMillis(rs.getLong("post_time"));
 
 		post.setFormatedTime(df.format(gc.getTime()));
 		
 		return post;
 	}
 
 	/**
 	 * @see net.jforum.model.PostModel#delete(Post)
 	 */
 	public void delete(Post post) throws Exception 
 	{
 		// Table posts
 		PreparedStatement p = JForum.getConnection().prepareStatement(SystemGlobals.getSql("PostModel.deletePost"));
 		p.setInt(1, post.getId());
 		
 		p.executeUpdate();
 		
 		p = JForum.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.decrementPosts"));
 		p.setInt(1, post.getUserId());
 		p.executeUpdate();
 		
 		p.close();
 	}
 	
 	/**
 	 * @see net.jforum.model.PostModel#update(net.jforum.Post)
 	 */
 	public void update(Post post) throws Exception 
 	{
 		// Table posts
 		PreparedStatement p = JForum.getConnection().prepareStatement(SystemGlobals.getSql("PostModel.updatePost"));
 		p.setInt(1, post.getTopicId());
 		p.setInt(2, post.getForumId());
 		p.setInt(3, post.isBbCodeEnabled()?1:0);
 		p.setInt(4, post.isHtmlEnabled()?1:0);
 		p.setInt(5, post.isSmiliesEnabled()?1:0);
 		p.setInt(6, post.isSignatureEnabled()?1:0);
 		p.setLong(7, post.getEditTime());
 		p.setString(8, post.getSubject());
 		p.setString(9, post.getText());		
		p.setInt(10, post.getId());
 		
 		p.executeUpdate();
 		p.close();		
 	}
 
 	/**
 	 * @see net.jforum.model.PostModel#addNew(net.jforum.Post)
 	 */
 	public int addNew(Post post) throws Exception 
 	{
 		// Table posts_texts
 		PreparedStatement p = this.getStatementForAutoKeys("PostModel.addNewPost");
 		
 		p.setInt(1, post.getTopicId());
 		p.setInt(2, post.getForumId());
 		p.setLong(3, post.getUserId());
 		p.setLong(4, post.getTime());
 		p.setString(5, post.getUserIp());
 		p.setInt(6, post.isBbCodeEnabled()?1:0);
 		p.setInt(7, post.isHtmlEnabled()?1:0);
 		p.setInt(8, post.isSmiliesEnabled()?1:0);
 		p.setInt(9, post.isSignatureEnabled()?1:0);
 		p.setString(10, post.getSubject());
 		p.setString(11, post.getText());
 		
 		int postId = this.executeAutoKeysQuery(p);
 		
 		post.setId(postId);
 		
 		p = JForum.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.incrementPosts"));
 		p.setInt(1, post.getUserId());
 		p.executeUpdate();
 		
 		p.close();
 		
 		// Tokenize the words for search
 		DataAccessDriver.getInstance().newSearchModel().insertSearchWords(post);
 		
 		return postId;
 	}
 	
 	/**
 	 * @see net.jforum.model.PostModel#setLastPostIdInTopic(int, int)
 	 */
 	public void setLastPostIdInTopic(int postId, int topicId) throws Exception{
 		PreparedStatement p = JForum.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.setLastPostId"));
 		p.setInt(1, postId);
 		p.setInt(2, topicId);
 		p.executeUpdate();
 		p.close();
 	}
 	
 	/**
 	 * @see net.jforum.model.PostModel#setLastPostIdInForum(int, int)
 	 */
 	public void setLastPostIdInForum(int postId, int forumId) throws Exception{
 		PreparedStatement p = JForum.getConnection().prepareStatement(SystemGlobals.getSql("ForumModel.updateLastPost"));
 		p.setInt(1, postId);
 		p.setInt(2, forumId);
 		p.executeUpdate();
 		p.close();
 	}
 
 	/**
 	 * @see net.jforum.model.PostModel#selectAllBytTopic(int)
 	 */
 	public ArrayList selectAllByTopic(int topicId) throws Exception
 	{
 		return this.selectAllByTopicByLimit(topicId, 0, Integer.MAX_VALUE);
 	}
 	
 	/**
 	 * @see net.jforum.model.PostModel#selectAllBytTopicByLimit(int, int, int)
 	 */
 	public ArrayList selectAllByTopicByLimit(int topicId, int startFrom, int count) throws Exception
 	{
 		ArrayList l = new ArrayList();
 		
 		PreparedStatement p = JForum.getConnection().prepareStatement(SystemGlobals.getSql("PostModel.selectAllByTopicByLimit"));
 		p.setInt(1, topicId);
 		p.setInt(2, startFrom);
 		p.setInt(3, count);
 		
 		ResultSet rs = p.executeQuery();
 						
 		while (rs.next()) {
 			l.add(this.makePost(rs));			
 		}
 		
 		rs.close();
 		p.close();
 				
 		return l;
 	}
 }
