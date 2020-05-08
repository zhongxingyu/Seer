 /*
  * Copyright 2010 Andreas Brauchli, René Buffat, Florian Widmer
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.footware.server.db;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 
 import org.footware.server.db.util.HibernateUtil;
 import org.footware.server.db.util.UserUtil;
 import org.footware.server.gpx.model.GPXTrack;
 import org.footware.server.gpx.model.GPXTrackSegment;
 import org.footware.shared.dto.CommentDTO;
 import org.footware.shared.dto.TagDTO;
 import org.footware.shared.dto.TrackDTO;
 import org.footware.shared.dto.TrackSegmentDTO;
 import org.hibernate.Hibernate;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.annotations.ManyToAny;
 
 /**
  * Class for ER mapping of Tracks
  */
 @Entity
 @NamedQueries(value = {
 		//Get all public tracks
 		@NamedQuery(name = "tracks.getAllPublic", query = "FROM Track t WHERE t.disabled=0 AND t.publicity=5"),
 		@NamedQuery(name="tracks.getTrackById", query= "FROM Track t where t.id = :id")
 	})
 public class Track extends DbEntity implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private long id;
 
 	@ManyToOne(fetch = FetchType.LAZY)
 	// @Column(nullable=false)
 	private User user;
 
 	@Column(length = 128, nullable = false)
 	private String filename;
 
 	@Column(length = 128)
 	private String path;
 
 	@Column(length = 256)
 	private String notes;
 
 	@Enumerated(EnumType.ORDINAL)
 	private int publicity;
 
 	@Column(name = "comments_enabled")
 	private boolean commentsEnabled;
 
 	private int trackpoints;
 
 	private double length;
 
 	@Column(name="mid_latitude")
 	private double midLatitude;
 
 	@Column(name="mid_longitude")
 	private double midLongitude;
 
 	@Column(name="time_start")
 	private Date startTime;
 	
 	private boolean disabled;
 
 	@ManyToAny(metaColumn = @Column(name="comment_id"), fetch=FetchType.EAGER)
 	private List<Comment> comments = new LinkedList<Comment>();
 
 	@OneToMany(fetch=FetchType.EAGER)
 	@JoinColumn(name="track_id")
 	private Set<TrackSegment> segments = new HashSet<TrackSegment>();
 	
 	@OneToMany(fetch=FetchType.EAGER,cascade=CascadeType.PERSIST)
 	@JoinColumn(name="track_id")
 	private Set<Tag> tags = new HashSet<Tag>();
 
 	protected Track() {
 	}
 
 	/**
 	 * Create a track for persistence
 	 * 
 	 * @param u
 	 *            User owning the track
 	 * @param filename
 	 *            filename (as uploaded) of the track
 	 * @param path
 	 *            path where the track is saved on the server
 	 */
 	public Track(User u, String filename, String path) {
 		this.user = u;
 		this.filename = filename;
 		this.path = path;
 	}
 
 	public Track(TrackDTO track) {
 		this.id = track.getId();
 		if (track.getUser() != null)
 			this.user = new User(track.getUser());
 		this.filename = track.getFilename();
 		// this.path = track.getPath(); //cannot be modified by the client
 		this.notes = track.getNotes();
 		this.publicity = track.getPublicity();
 		this.commentsEnabled = track.isCommentsEnabled();
 		this.trackpoints = track.getTrackpoints();
 		this.length = track.getLength();
 		this.startTime = track.getStartTime();
 		for (TrackSegmentDTO segment : track.getSegments()) 
 			this.segments.add(new TrackSegment(segment));
 		for (CommentDTO c : track.getComments())
 			this.comments.add(new Comment(c));
 		for (TagDTO t : track.getTags())
 			this.tags.add(new Tag(t));
 	}
 	
 	public Track(GPXTrack track) {
 		this.trackpoints = track.getNumberOfDataPoints();
 		this.length = track.getLength();
 		for(GPXTrackSegment segment : track.getSegments()) {
 			segments.add(new TrackSegment(segment));
 		}
 	}
 
 	/**
 	 * Gets the id of the corresponding DB row
 	 * 
 	 * @return the ID of the row in the DB
 	 */
 	public long getId() {
 		return id;
 	}
 
 	/**
 	 * Gets the user belonging to this track
 	 * 
 	 * @return this track's user
 	 */
 	public User getUser() {
 		return user;
 	}
 
 	/**
 	 * Sets the user owning this track
 	 * 
 	 * @param user
 	 *            new user owning this track
 	 */
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	/**
 	 * Gets the tracks original filename (as uploaded)
 	 * 
 	 * @return the tracks original filename
 	 */
 	public String getFilename() {
 		return filename;
 	}
 
 	/**
 	 * Sets the tracks filename (as uploaded)
 	 * 
 	 * @param filename
 	 *            new filename
 	 */
 	public void setFilename(String filename) {
 		this.filename = filename;
 	}
 
 	public String getPath() {
 		return path;
 	}
 
 	public void setPath(String path) {
 		this.path = path;
 	}
 
 	/**
 	 * Gets the authors track notes
 	 * 
 	 * @return authors track notes
 	 */
 	public String getNotes() {
 		return notes;
 	}
 
 	/**
 	 * Sets the authors track notes
 	 * 
 	 * @param notes
 	 */
 	public void setNotes(String notes) {
 		this.notes = notes;
 	}
 
 	/**
 	 * Gets the publicity (permission) level for this track: 0 track is private,
 	 * (not yet implemented: 1 selected users, 2 selected group, 3 friends, 4
 	 * registered users), 5 public
 	 * 
 	 * @return the publicity (permission) level for this track
 	 */
 	public int getPublicity() {
 		return publicity;
 	}
 
 	/**
 	 * Sets the publicity (permission) level for this track: 0 track is private,
 	 * (not yet implemented: 1 selected users, 2 selected group, 3 friends, 4
 	 * registered users), 5 public
 	 * 
 	 * @param publicity
 	 *            new publicity (permission) level for this track
 	 */
 	public void setPublicity(int publicity) {
 		this.publicity = publicity;
 	}
 
 	/**
 	 * Gets whether commenting this track is enabled
 	 * 
 	 * @return true if enabled
 	 */
 	public boolean isCommentsEnabled() {
 		return commentsEnabled;
 	}
 
 	/**
 	 * Sets whether commenting this track is enabled
 	 * 
 	 * @param commentsEnabled
 	 *            true if enabled (existing comments will not be deleted if
 	 *            disabled)
 	 */
 	public void setCommentsEnabled(boolean commentsEnabled) {
 		this.commentsEnabled = commentsEnabled;
 	}
 
 	/**
 	 * Gets the number of track points for this track
 	 * 
 	 * @return number of track points for this track
 	 */
 	public int getTrackpoints() {
 		Hibernate.initialize(trackpoints);
 		return trackpoints;
 	}
 
 	/**
 	 * Sets the number of track points for this track Should probably not be
 	 * used from the client side
 	 * 
 	 * @param trackpoints
 	 *            number of track points on this track
 	 */
 	public void setTrackpoints(int trackpoints) {
 		this.trackpoints = trackpoints;
 	}
 
 	/**
 	 * Gets the length in meters of this track
 	 * 
 	 * @return the length in meters of this track
 	 */
 	public double getLength() {
 		return length;
 	}
 
 	/**
 	 * Sets the length (in meters) of this track Should probably not be used
 	 * from the client side
 	 * 
 	 * @param length
 	 *            new length in meters for this track
 	 */
 	public void setLength(double length) {
 		this.length = length;
 	}
 
 	/**
 	 * Gets the time of the first track point in this track
 	 * 
 	 * @return the time of the first track point in this track
 	 */
 	public Date getStartTime() {
 		return startTime;
 	}
 
 	/**
 	 * Sets the time of the first track point in this track Should probably not
 	 * be used from the client side
 	 * 
 	 * @param startTime
 	 *            time of the first track point in this track
 	 */
 	public void setStartTime(Date startTime) {
 		this.startTime = startTime;
 	}
 
 	/**
 	 * Gets the mean latitude of the track
 	 * 
 	 * @return mean latitude of the track
 	 */
 	public double getMidLatitude() {
 		return midLatitude;
 	}
 
 	/**
 	 * Sets the mean latitude of the track
 	 * 
 	 * @param midLatitude
 	 *            mean latitude of the track
 	 */
 	public void setMidLatitude(double midLatitude) {
 		this.midLatitude = midLatitude;
 	}
 
 	/**
 	 * Gets the mean longitude of the track
 	 * 
 	 * @return mean longitude of the track
 	 */
 	public double getMidLongitude() {
 		return midLongitude;
 	}
 
 	/**
 	 * Sets the mean longitude of the track
 	 * 
 	 * @param midLongitude
 	 *            mean longitude of the track
 	 */
 	public void setMidLongitude(double midLongitude) {
 		this.midLongitude = midLongitude;
 	}
 
 	/**
 	 * Sets this track as disabled or enabled
 	 * @param disabled whether to enable or disable this track
 	 */
 	public void setDisabled(boolean disabled) {
 		this.disabled = disabled;
 	}
 
 	/**
 	 * Gets whether this track is disabled or not
 	 * @return true if disabled, false if enabled
 	 */
 	public boolean isDisabled() {
 		return disabled;
 	}
 
 	/**
 	 * Gets all comments for this track
 	 * 
 	 * @return List of all comments for this track
 	 */
 	public List<Comment> getComments() {
 		Hibernate.initialize(comments);
 		return comments;
 	}
 
 	/**
 	 * Adds a new comment to this track Note: the user writing the comment is
 	 * registered in the comment object
 	 * 
 	 * @param comment
 	 *            comment to be added to this track
 	 */
 	public void addComment(Comment comment) {
 		comments.add(comment);
 	}
 	
 	/**
 	 * Gets the segments belonging to this track
 	 * @return segments of this track
 	 */
 	public Set<TrackSegment> getSegments() {
 		Hibernate.initialize(segments);
 		return segments;
 	}
 	
 	/**
 	 * Adds a new segment to this track
 	 * @param s segment to add
 	 */
 	public void addSegment(TrackSegment s) {
 		segments.add(s);
 	}
 
 	/**
 	 * Gets the tags attached to this track
 	 * @return set of tags attached to this track
 	 */
 	public Set<Tag> getTags() {
 		Hibernate.initialize(segments);
 		return tags;
 	}
 	
 	/**
 	 * Adds a tag to this track
 	 * @param t tag to add
 	 */
 	public void addTag(Tag t) {
 		tags.add(t);
 	}
 	
 	/**
 	 * Gets the TrackDTO object from this Track's current state
 	 * 
 	 * @return TrackDTO with this tracks current state
 	 */
 	public TrackDTO getTrackDTO() {
 		TrackDTO t = new TrackDTO();
		//will be set by the user to prevent stackoverflow
		//t.setUser(user.getUserDTO());
 		t.setFilename(filename);
 		t.setNotes(notes);
 		t.setPublicity(publicity);
 		t.setCommentsEnabled(commentsEnabled);
 		t.setTrackpoints(trackpoints);
 		t.setLength(length);
 		t.setMidLatitude(midLatitude);
 		t.setMidLongitude(midLongitude);
 		t.setStartTime(startTime);
 		t.setDisabled(disabled);
 		for (Comment c : getComments())
 			t.addComment(c.getCommentDTO());
 		for (TrackSegment s : getSegments())
 			t.addSegment(s.getTrackSegmentDTO());
 		for (Tag tag : getTags())
 			t.addTag(tag.getTagDTO());
 		return t;
 	}
 
 	/**
 	 * Persist (save or update) the object
 	 */
 	public void persist() {
 		Transaction tx = null;
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		try {
 			tx = session.beginTransaction();
 			session.persist(this);
 			for (TrackSegment s : segments) {
 				s.setTrack(this);
 				session.persist(s);
 				for (Trackpoint p : s.getTrackpoints()) {
 					p.setSegment(s);
 					session.persist(p);
 				}
 			}
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null && tx.isActive()) {
 				try {
 					// Second try catch as the rollback could fail as well
 					tx.rollback();
 				} catch (HibernateException e1) {
 					// logger.debug("Error rolling back transaction");
 				}
 				// throw again the first exception
 				throw e;
 			}
 		}
 	}
 }
