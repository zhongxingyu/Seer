 package com.zarcode.data.model;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.IdentityType;
 import javax.jdo.annotations.NotPersistent;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import com.google.appengine.api.datastore.Text;
 import com.zarcode.app.AppCommon;
 import com.zarcode.platform.model.AbstractLoaderDO;
 
 @XmlRootElement(name = "ReportRegion") 
 @PersistenceCapable(identityType = IdentityType.APPLICATION)
 public class ReportRegionDO extends AbstractLoaderDO implements Serializable {
 
 	@PrimaryKey 
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
 	private Long regionId;
 	
 	@Persistent
 	private Date lastReportDate = null;
 	
 	@Persistent
 	private String state = null;
 
 	@Persistent
 	private Date lastUpdated = null;
 	
 	public void postCreation() {
 	}
 	
 	public void postReturn() {
 	}
 	
 	public String toString() {
 		String str = "ReportRegionDO::" + regionId + "\n" +
 			"	lastReportDate=" + lastReportDate + "\n" +
 			"	state=" + state + "\n";
 		return str;
 	}
 	
 	
 	@XmlElement
	public Long getRegionId() {
 		return regionId;
 	}
 
 	public void setRegionId(Long regionId) {
 		this.regionId = regionId;
 	}
 	
 	@XmlElement
 	public Date getLastReportDate() {
 		return lastReportDate;
 	}
 
 	public void setLastReportDate(Date lastReportDate) {
 		this.lastReportDate = lastReportDate;
 	}
 
 	@XmlElement
 	public String getState() {
 		return state;
 	}
 
 	public void setState(String state) {
 		this.state = state;
 	}
 	
     @XmlElement
 	public Date getLastUpdated() {
 		return lastUpdated;
 	}
 
 	public void setLastUpdated(Date lastUpdated) {
 		this.lastUpdated = lastUpdated;
 	}
 
 }
