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
 public class App implements Serializable {
 	private Long id;
 
 	private String handle;
 
 	private String name;
 
	// XXX it can't seem to bind date objects right
 	private Date releaseDate;
 
 	private String description;
 
 	private String category;
 
 	@JsonProperty("developer_name")
 	private String developerName;
 
 	@JsonProperty("developer_url")
 	private String developerUrl;
 
 
 	public App() {
 	}
 
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getHandle() {
 		return handle;
 	}
 
 	public void setHandle(String handle) {
 		this.handle = handle;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Date getReleaseDate() {
 		return releaseDate;
 	}
 
 	public void setReleaseDate(Date releaseDate) {
 		this.releaseDate = releaseDate;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getCategory() {
 		return category;
 	}
 
 	public void setCategory(String category) {
 		this.category = category;
 	}
 
 	public String getDeveloperName() {
 		return developerName;
 	}
 
 	public void setDeveloperName(String developerName) {
 		this.developerName = developerName;
 	}
 
 	public String getDeveloperUrl() {
 		return developerUrl;
 	}
 
 	public void setDeveloperUrl(String developerUrl) {
 		this.developerUrl = developerUrl;
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
