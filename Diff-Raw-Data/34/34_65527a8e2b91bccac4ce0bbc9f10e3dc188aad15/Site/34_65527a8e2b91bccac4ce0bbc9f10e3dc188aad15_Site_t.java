 package com.webs.api.model;
 
 import java.io.Serializable;
 import java.util.List;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 import org.codehaus.jackson.annotate.JsonProperty;
 
 
 /**
  * Model for representing a Webs.com site.  
  *
  * @author Patrick Carroll
  */
 @JsonIgnoreProperties(ignoreUnknown=true)
 public class Site implements Serializable {
 	private static long serialVersionUID = 1L;
 
 	private Long id;
 
 	private String username;
 
 	@JsonProperty("email_address")
 	private String emailAddress;
 
 	private boolean premium;
 
 	private String url;
 
 	private String vertical;
 
 	private String title;
 
 	private String footer;
 
 	private String description;
 
 	@JsonProperty("sidebars_enabled")
 	private boolean sidebarsEnabled;
 
 	private List<String> keywords;
 
	private boolean advanced;

 
 	public Site() {
 	}
 
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getEmailAddress() {
 		return emailAddress;
 	}
 
 	public void setEmailAddress(String emailAddress) {
 		this.emailAddress = emailAddress;
 	}
 
 	public boolean isPremium() {
 		return premium;
 	}
 
 	public void setPremium(boolean premium) {
 		this.premium = premium;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public String getVertical() {
 		return vertical;
 	}
 
 	public void setVertical(String vertical) {
 		this.vertical = vertical;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getFooter() {
 		return footer;
 	}
 
 	public void setFooter(String footer) {
 		this.footer = footer;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public boolean isSidebarsEnabled() {
 		return sidebarsEnabled;
 	}
 
 	public void setSidebarsEnabled(boolean sidebarsEnabled) {
 		this.sidebarsEnabled = sidebarsEnabled;
 	}
 
 	public List<String> getKeywords() {
 		return keywords;
 	}
 
 	public void setKeywords(List<String> keywords) {
 		this.keywords = keywords;
 	}
 
	public boolean isAdvanced() {
		return advanced;
	}

	public void setAdvanced(boolean advanced) {
		this.advanced = advanced;
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
