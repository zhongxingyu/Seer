 package net.java.dev.cejug.classifieds.server.ejb3.entity;
 
 import java.util.TimeZone;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
 
 @Entity
 @Table(name = "domain")
 @NamedQuery(name = "selectDomainByName", query = "SELECT d FROM DomainEntity d WHERE d.domain= :par1")
 public class DomainEntity {
 	@Column(nullable = false)
 	private Boolean sharedQuota;
 
 	@Id
 	@Column(nullable = false)
 	private String domain;
 
 	@Column(nullable = false)
 	private String brand;
 
 	@Column(nullable = false)
 	private TimeZone timezone;
 
 	public String getDomain() {
 		return domain;
 	}
 
 	public void setDomain(String domain) {
 		this.domain = domain;
 	}
 
 	public Boolean getSharedQuota() {
 		return sharedQuota;
 	}
 
 	public void setSharedQuota(Boolean sharedQuota) {
 		this.sharedQuota = sharedQuota;
 	}
 
 	public TimeZone getTimezone() {
 		return timezone;
 	}
 
 	public void setTimezone(TimeZone timezone) {
 		this.timezone = timezone;
 	}
 
 	public String getBrand() {
 		return brand;
 	}
 
 	public void setBrand(String brand) {
 		this.brand = brand;
 	}
 
 }
