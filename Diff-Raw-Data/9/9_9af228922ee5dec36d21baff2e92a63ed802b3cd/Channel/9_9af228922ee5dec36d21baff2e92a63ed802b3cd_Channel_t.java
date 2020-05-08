 package com.amatic.rc.dto;
 
import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.googlecode.objectify.Ref;
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Id;
 import com.googlecode.objectify.annotation.Index;
 import com.googlecode.objectify.annotation.Load;
 
 @Entity
public class Channel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3476708644369957063L;

 	@Id
 	public Long id;
 
 	@Index
 	String key;
 
 	@Index
 	Integer nbrViewers;
 	@Index
 	String name;
 	@Load
 	Ref<User> user;
 	@Index
 	List<String> lImages = new ArrayList<String>();
 	@Index
 	private Date dateCreated;
 	@Index
 	List<String> lImagesKeys = new ArrayList<String>();
 
 	public Date getDateCreated() {
 		return dateCreated;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public String getKey() {
 		return key;
 	}
 
 	public List<String> getlImages() {
 		return lImages;
 	}
 
 	public List<String> getlImagesKeys() {
 		return lImagesKeys;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public Integer getNbrViewers() {
 		return nbrViewers;
 	}
 
 	public Ref<User> getUser() {
 		return user;
 	}
 
 	public User getUserDeref() {
 		return Deref.deref(user);
 	}
 
 	public void setDateCreated(Date dateCreated) {
 		this.dateCreated = dateCreated;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public void setKey(String key) {
 		this.key = key;
 	}
 
 	public void setlImages(List<String> lImages) {
 		this.lImages = lImages;
 	}
 
 	public void setlImagesKeys(List<String> lImagesKeys) {
 		this.lImagesKeys = lImagesKeys;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void setNbrViewers(Integer nbrViewers) {
 		this.nbrViewers = nbrViewers;
 	}
 
 	public void setUser(Ref<User> user) {
 		this.user = user;
 	}
 
 }
