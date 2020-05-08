 package com.webs.api.model;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 import org.codehaus.jackson.annotate.JsonProperty;
 
 
 
 /**
  * @author Patrick Carroll
  */
 @JsonIgnoreProperties(ignoreUnknown=true)
 public class SiteSubscription implements Serializable {
 	public static final long serialVersionUID = 1L;
 
 	private Long id;
 
	@JsonProperty("site_id")
 	private Long siteId;
 
 	private String username;
 
	@JsonProperty("site_title")
 	private String siteTitle;
 
 	private String url;
 
 	private String permission;
 
	@JsonProperty("creation_date")
 	private Date creationDate;
 
	@JsonProperty("last_login")
 	private Date lastLogin;
 
	@JsonProperty("subscription_private")
 	private boolean subscriptionPrivate;
 
 

 	public SiteSubscription() {
 	}
 
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Long getSiteId() {
 		return siteId;
 	}
 
 	public void setSiteId(Long siteId) {
 		this.siteId = siteId;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getSiteTitle() {
 		return siteTitle;
 	}
 
 	public void setSiteTitle(String siteTitle) {
 		this.siteTitle = siteTitle;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public String getPermission() {
 		return permission;
 	}
 
 	public void setPermission(String permission) {
 		this.permission = permission;
 	}
 
 	public Date getCreationDate() {
 		return creationDate;
 	}
 
 	public void setCreationDate(Date creationDate) {
 		this.creationDate = creationDate;
 	}
 
 	public Date getLastLogin() {
 		return lastLogin;
 	}
 
 	public void setLastLogin(Date lastLogin) {
 		this.lastLogin = lastLogin;
 	}
 
 	public boolean isSubscriptionPrivate() {
 		return subscriptionPrivate;
 	}
 
 	public void setSubscriptionPrivate(boolean subscriptionPrivate) {
 		this.subscriptionPrivate = subscriptionPrivate;
 	}
 
 
 	@Override
 	public String toString() {
 		return ToStringBuilder.reflectionToString(this);
 	}
 
 	@Override
 	public int hashCode() {
 		return HashCodeBuilder.reflectionHashCode(this);
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		return EqualsBuilder.reflectionEquals(this, o);
 	}
 }
