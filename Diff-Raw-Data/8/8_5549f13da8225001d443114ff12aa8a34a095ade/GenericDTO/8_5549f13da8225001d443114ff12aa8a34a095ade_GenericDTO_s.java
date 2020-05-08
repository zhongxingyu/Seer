 package uk.frequency.glance.server.transfer;
 
 import java.io.Serializable;
 
 @SuppressWarnings("serial")
 public abstract class GenericDTO implements Serializable {
 
	long id;
 
 	protected Long creationTime;
 
 	protected Long updateTime;
 	
	public long getId() {
 		return id;
 	}
 
	public void setId(long id) {
 		this.id = id;
 	}
 
 	public Long getCreationTime() {
 		return creationTime;
 	}
 
 	public void setCreationTime(Long creationTime) {
 		this.creationTime = creationTime;
 	}
 
 	public Long getUpdateTime() {
 		return updateTime;
 	}
 
 	public void setUpdateTime(Long updateTime) {
 		this.updateTime = updateTime;
 	}
 
 }
