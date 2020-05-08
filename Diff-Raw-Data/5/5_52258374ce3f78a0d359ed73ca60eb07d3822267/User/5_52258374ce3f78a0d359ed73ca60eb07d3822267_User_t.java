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
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 
 import org.footware.server.db.util.HibernateUtil;
 import org.footware.server.db.util.UserUtil;
 import org.footware.shared.dto.TrackDTO;
 import org.footware.shared.dto.UserDTO;
 import org.hibernate.Hibernate;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 /**
  * Class for ER mapping of Users
  */
 @Entity()
 @NamedQueries(value = {
 		// Get user by email
 		@NamedQuery(name = "users.getByEmail", query = "FROM User u WHERE u.email = :email"),
 		// Get all users
 		@NamedQuery(name = "users.getAll", query = "FROM User"),
 		//Get user from email/password pair
 		@NamedQuery(name = "users.getIfValid", query = "FROM User u WHERE u.email = :email AND u.password = :password"),
 		//Get user from email/password pair
		@NamedQuery(name = "users.getPublicTracks", query = "FROM Track t WHERE t.user = :user AND t.publicity = 5")
 	})
 public class User extends DbEntity implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private long id;
 
 	@Column(length = 128, unique = true, nullable = false)
 	private String email;
 
 	@Column(name = "full_name", length = 64)
 	private String fullName;
 
 	@Column(length = 32)
 	private char[] password;
 
 	@Column(name = "is_admin")
 	private boolean isAdmin;
 
 	@Column(name = "is_deactivated")
 	private boolean isDeactivated;
 
 	@OneToMany(fetch = FetchType.EAGER,cascade=CascadeType.PERSIST)
 	@JoinColumn(name = "user_id")
 	private Set<Track> tracks = new HashSet<Track>();
 
 	// TODO:
 	// @OneToMany(fetch = FetchType.EAGER)
 	// @JoinTable(name = "user_tag")
 	// @JoinColumn(name = "user_id")
 	// private Set<String> tags = new HashSet<String>();
 
 	/**
 	 * Protected constructor for hibernate object initialization
 	 */
 	protected User() {
 	}
 
 	/**
 	 * Creates a new user object for persistence
 	 * 
 	 * @param email
 	 *            user's email address
 	 * @param password
 	 *            user's password in clear text
 	 */
 	public User(String email, String password) {
 		this.email = email;
 		this.password = UserUtil.getPasswordHash(password).toCharArray();
 	}
 
 	/**
 	 * Gets the id of the corresponding DB row
 	 * 
 	 * @return the ID of the row in the DB
 	 */
 	public User(UserDTO user) {
 		this.id = user.getId();
 		this.email = user.getEmail();
 		this.fullName = user.getFullName();
 		if (user.getPassword() != null)
 			this.password = UserUtil.getPasswordHash(user.getPassword())
 					.toCharArray();
 		this.isDeactivated = user.isDeactivated();
 		// this.isAdmin = user.getIsAdmin();
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		Transaction tx = null;
 		try {
 			tx = session.beginTransaction();
 			session.save(this);
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
 
 	/**
 	 * Gets the id of the corresponding DB row
 	 * 
 	 * @return the ID of the row in the DB
 	 */
 	protected long getId() {
 		return id;
 	}
 
 	/**
 	 * Gets the email address of the associated user
 	 * 
 	 * @return user's email address
 	 */
 	public String getEmail() {
 		return email;
 	}
 
 	/**
 	 * Sets the email address of the associated user
 	 * 
 	 * @param email
 	 *            new email address
 	 */
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	/**
 	 * Gets the full name of the user
 	 * 
 	 * @return user's full name
 	 */
 	public String getFullName() {
 		return fullName;
 	}
 
 	/**
 	 * Sets the full name of the user
 	 * 
 	 * @param fullName
 	 *            new user's name
 	 */
 	public void setFullName(String fullName) {
 		this.fullName = fullName;
 	}
 
 	/**
 	 * Gets the user's password hash
 	 * 
 	 * @return user's password hash
 	 */
 	public char[] getPassword() {
 		return password;
 	}
 
 	/**
 	 * Sets the user's password hash
 	 * 
 	 * @param password
 	 *            new password hash
 	 */
 	public void setPassword(char[] password) {
 		this.password = password;
 	}
 
 	/**
 	 * Gets whether the user has administrative privileges
 	 * 
 	 * @return boolean true if user is admin, false otherwise
 	 */
 	public boolean getIsAdmin() {
 		return isAdmin;
 	}
 
 	/**
 	 * Changes the user's administrative privileges
 	 * 
 	 * @param isAdmin
 	 *            true means the user becomes an admin, false degrades him to an
 	 *            ordinary user
 	 */
 	public void setIsAdmin(boolean isAdmin) {
 		this.isAdmin = isAdmin;
 	}
 
 	/**
 	 * Gets whether this user is active
 	 * 
 	 * @return true if the user is deactivated
 	 */
 	public boolean isDeactivated() {
 		return isDeactivated;
 	}
 
 	/**
 	 * Disables this user
 	 */
 	public void deactivate() {
 		isDeactivated = true;
 	}
 
 	/**
 	 * Reactivated this user
 	 */
 	public void activate() {
 		isDeactivated = false;
 	}
 
 	/**
 	 * Gets all tracks associated with this user
 	 * 
 	 * @return all user's tracks
 	 */
 	public Set<Track> getTracks() {
 		Hibernate.initialize(tracks);
 		return tracks;
 	}
 
 	/**
 	 * Adds a new track to this user's track collection
 	 * 
 	 * @param track
 	 *            track to add
 	 */
 	public void addTrack(Track track) {
 		Hibernate.initialize(tracks);
 		if (tracks == null)
 			tracks = new HashSet<Track>();
 
 		tracks.add(track);
 	}
 
 	/**
 	 * Removes a track from this user's track collection
 	 * 
 	 * @param track
 	 *            track to remove
 	 */
 	public void removeTrack(Track track) {
 		Hibernate.initialize(tracks);
 		if (tracks != null) {
 			tracks.remove(track);
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Track> getPublicTracks() {
 		Session s = HibernateUtil.getSessionFactory().getCurrentSession();
 		Transaction t = s.beginTransaction();
 		Query q = s.getNamedQuery("users.getPublicTracks");
		q.setParameter("user", this);
 		List<Track> res = null;
 		try {
 			res = q.list();
 			t.commit();
 		} catch (HibernateException e) {
 			t.rollback();
 		}
 		return res;
 	}
 
 	// /**
 	// * Gets the tags associated with this user
 	// * @return all tags of this user
 	// */
 	// public Set<String> getTags() {
 	// Hibernate.initialize(tags);
 	// return tags;
 	// }
 
 	/**
 	 * Creates a new UserDTO from this User object's current state
 	 * 
 	 * @return UserDTO with this user's current state
 	 */
 	public UserDTO getUserDTO() {
 		UserDTO u = new UserDTO();
 		u.setEmail(email);
 		u.setFullName(fullName);
 		u.setIsAdmin(isAdmin);
 		if (isDeactivated)
 			u.deactivate();
 		else
 			u.activate();
 
 		for (Track t : getTracks()) {
 			TrackDTO dto = t.getTrackDTO();
 			u.addTrackDTO(dto);
 			if (t.getPublicity() == 5)
 				u.addPublicTrack(dto);
 		}
 		// do not set password (it's only the hash anyway)
 		// for (String t : getTags())
 		// 		u.addTag(t);
 
 		return u;
 	}
 }
