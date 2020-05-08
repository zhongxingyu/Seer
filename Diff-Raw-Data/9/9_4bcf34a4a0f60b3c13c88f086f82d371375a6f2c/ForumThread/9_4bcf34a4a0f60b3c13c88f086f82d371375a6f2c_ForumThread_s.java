 /**
  * Tysan Clan Website
  * Copyright (C) 2008-2013 Jeroen Steenbeeke and Ties van de Ven
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.tysanclan.site.projectewok.entities;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.OrderBy;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.hibernate.annotations.AccessType;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.Index;
 
 import com.jeroensteenbeeke.hyperion.data.BaseDomainObject;
 
 /**
  * @author Jeroen Steenbeeke
  */
 @Entity
 @AccessType("field")
 @Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.TRANSACTIONAL, region = "forum")
 public class ForumThread extends BaseDomainObject {
 	private static final long serialVersionUID = 1L;
 
 	public static final int POSTS_PER_PAGE = 10;
 
 	@ManyToOne(fetch = FetchType.LAZY)
 	@Index(name = "IDX_FORUMTHREAD_BRANCHFROM")
 	private ForumThread branchFrom;
 
 	@ManyToOne(fetch = FetchType.LAZY)
 	@Index(name = "IDX_FORUMTHREAD_FORUM")
 	private Forum forum;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ForumThread")
 	@SequenceGenerator(name = "ForumThread", sequenceName = "SEQ_ID_ForumThread", allocationSize = 1)
 	private Long id;
 
 	@Column
 	private boolean locked;
 
 	// If null then posted by "System"
 	@ManyToOne(optional = true, fetch = FetchType.LAZY)
 	@Index(name = "IDX_FORUMTHREAD_POSTER")
 	private User poster;
 
 	@OneToMany(mappedBy = "thread", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
 	@OrderBy("time asc")
 	private List<ForumPost> posts;
 
 	@Column
 	private Date postTime;
 
 	@Column
	@Temporal(TemporalType.DATE)
 	private Date lastPost;
 
 	@Column
 	private boolean shadow;
 
 	@Column(length = 1)
 	private int sticky;
 
 	@Column
 	private String title;
 
 	@OneToOne(mappedBy = "joinThread", fetch = FetchType.EAGER, optional = true)
 	private JoinApplication application;
 
 	@OneToOne(mappedBy = "trialThread", fetch = FetchType.EAGER, optional = true)
 	private Trial trial;
 
 	@OneToOne(mappedBy = "eventThread", fetch = FetchType.EAGER, optional = true)
 	private Event event;
 
 	// $P$
 
 	public ForumThread() {
 		posts = new LinkedList<ForumPost>();
 		// $H$
 	}
 
 	public JoinApplication getApplication() {
 		return application;
 	}
 
 	public void setApplication(JoinApplication application) {
 		this.application = application;
 	}
 
 	public Trial getTrial() {
 		return trial;
 	}
 
 	public void setTrial(Trial trial) {
 		this.trial = trial;
 	}
 
 	public Event getEvent() {
 		return event;
 	}
 
 	public void setEvent(Event event) {
 		this.event = event;
 	}
 
 	/**
 	 * @return the branchFrom
 	 */
 	public ForumThread getBranchFrom() {
 		return branchFrom;
 	}
 
 	/**
 	 * @return the forum
 	 */
 	public Forum getForum() {
 		return forum;
 	}
 
 	@Override
 	public Serializable getDomainObjectId() {
 		return getId();
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	/**
 	 * @return the poster
 	 */
 	public User getPoster() {
 		return poster;
 	}
 
 	public String getPosterVisibleName() {
 		return poster != null ? poster.getUsername() : "System";
 	}
 
 	/**
 	 * @return the posts
 	 */
 	public List<ForumPost> getPosts() {
 		return posts;
 	}
 
 	/**
 	 * @return the postTime
 	 */
 	public Date getPostTime() {
 		return postTime;
 	}
 
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * @return the locked
 	 */
 	public boolean isLocked() {
 		return locked;
 	}
 
 	/**
 	 * @return the shadow
 	 */
 	public boolean isShadow() {
 		return shadow;
 	}
 
 	/**
 	 * @return the sticky
 	 */
 	public boolean isPostSticky() {
 		return sticky == 1;
 	}
 
 	/**
 	 * @param branchFrom
 	 *            the branchFrom to set
 	 */
 	public void setBranchFrom(ForumThread branchFrom) {
 		this.branchFrom = branchFrom;
 	}
 
 	/**
 	 * @param forum
 	 *            the forum to set
 	 */
 	public void setForum(Forum forum) {
 		this.forum = forum;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	/**
 	 * @param locked
 	 *            the locked to set
 	 */
 	public void setLocked(boolean locked) {
 		this.locked = locked;
 	}
 
 	/**
 	 * @param poster
 	 *            the poster to set
 	 */
 	public void setPoster(User poster) {
 		this.poster = poster;
 	}
 
 	/**
 	 * @param posts
 	 *            the posts to set
 	 */
 	public void setPosts(List<ForumPost> posts) {
 		this.posts = posts;
 	}
 
 	/**
 	 * @param postTime
 	 *            the postTime to set
 	 */
 	public void setPostTime(Date postTime) {
 		this.postTime = postTime;
 	}
 
 	/**
 	 * @param shadow
 	 *            the shadow to set
 	 */
 	public void setShadow(boolean shadow) {
 		this.shadow = shadow;
 	}
 
 	/**
 	 * @param sticky
 	 *            the sticky to set
 	 */
 	public void setPostSticky(boolean sticky) {
 		this.sticky = sticky ? 1 : 0;
 	}
 
 	/**
 	 * @param title
 	 *            the title to set
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public void setSticky(int sticky) {
 		this.sticky = sticky;
 	}
 
 	public int getSticky() {
 		return sticky;
 	}
 
 	public Date getLastPost() {
 		return lastPost;
 	}
 
 	public void setLastPost(Date lastPost) {
 		this.lastPost = lastPost;
 	}
 
 	// $GS$
 
 }
