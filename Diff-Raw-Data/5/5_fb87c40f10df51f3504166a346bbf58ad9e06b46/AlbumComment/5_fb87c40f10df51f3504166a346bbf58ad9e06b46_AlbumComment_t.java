 package com.jin.tpdb.entities;
 
 import java.util.Date;
 import java.util.Collection;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.ManyToOne;
 import javax.persistence.ManyToMany;
 import javax.persistence.Column;
 import com.jin.tpdb.entities.Artist;
 import com.jin.tpdb.entities.Album;
 import com.jin.tpdb.entities.User;
 
 @Entity
 public class AlbumComment {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	int id;
 
 	@ManyToOne
 	private User user;
 
 	@ManyToOne
 	//private Collection<Album> album;
 	private Album album;
 
 	@Column(nullable = false)
 	private Date date;
 
 	@Column(length = 65535, columnDefinition = "Text")
 	private String comment;
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
	public Album getAlbum() {
 		return album;
 	}
 
	public void setAlbum(Album album) {
 		this.album = album;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public String getComment() {
 		return comment;
 	}
 
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 
 }
