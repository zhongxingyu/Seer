 /*
  * Licensed to Lolay, Inc. under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  Lolay, Inc. licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://github.com/lolay/citygrid/raw/master/LICENSE
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package com.lolay.citygrid;
 
 import java.io.Serializable;
 import java.net.URL;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 @XmlRootElement(name="location")
 @XmlAccessorType(value=XmlAccessType.FIELD)
 public class SearchLocation implements Serializable {
 	private static final long serialVersionUID = 1L;
 	
 	@XmlAttribute(name="id",required=true)
	private Long id = null;
 	@XmlElement(name="featured",required=true)
 	private Boolean featured = null;
 	@XmlElement(name="name",required=true)
 	private String name = null;
 	@XmlElement(name="address",required=true)
 	private Address address = null;
 	@XmlElement(name="neighborhood")
 	private String neighborhood = null;
 	@XmlElement(name="latitude",required=true)
 	private Double latitude = null;
 	@XmlElement(name="longitude",required=true)
 	private Double longitude = null;
 	@XmlElement(name="image")
 	private URL image = null;
 	@XmlElement(name="phonenumber")
 	private String phoneNumber = null;
 	@XmlElement(name="rating")
 	private Integer rating = null;
 	@XmlElement(name="profile",required=true)
 	private URL profile = null;
 	@XmlElement(name="hasvideo",required=true)
 	private Boolean hasVideo = null;
 	@XmlElement(name="hasoffers",required=true)
 	private Boolean hasOffers = null;
 	@XmlElement(name="userreviewcount",required=true)
 	private Integer userReviewCount = null;
 	@XmlElement(name="samplecategories",required=true)
 	private String sampleCategories = null;
 	@XmlElement(name="tagline")
 	private String tagline = null;
 	
	public Long getId() {
 		return id;
 	}
	public void setId(Long id) {
 		this.id = id;
 	}
 	public Boolean getFeatured() {
 		return featured;
 	}
 	public void setFeatured(Boolean featured) {
 		this.featured = featured;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public Address getAddress() {
 		return address;
 	}
 	public void setAddress(Address address) {
 		this.address = address;
 	}
 	public String getNeighborhood() {
 		return neighborhood;
 	}
 	public void setNeighborhood(String neighborhood) {
 		this.neighborhood = neighborhood;
 	}
 	public Double getLatitude() {
 		return latitude;
 	}
 	public void setLatitude(Double latitude) {
 		this.latitude = latitude;
 	}
 	public Double getLongitude() {
 		return longitude;
 	}
 	public void setLongitude(Double longitude) {
 		this.longitude = longitude;
 	}
 	public URL getImage() {
 		return image;
 	}
 	public void setImage(URL image) {
 		this.image = image;
 	}
 	public String getPhoneNumber() {
 		return phoneNumber;
 	}
 	public void setPhoneNumber(String phoneNumber) {
 		this.phoneNumber = phoneNumber;
 	}
 	public Integer getRating() {
 		return rating;
 	}
 	public void setRating(Integer rating) {
 		this.rating = rating;
 	}
 	public URL getProfile() {
 		return profile;
 	}
 	public void setProfile(URL profile) {
 		this.profile = profile;
 	}
 	public Boolean getHasVideo() {
 		return hasVideo;
 	}
 	public void setHasVideo(Boolean hasVideo) {
 		this.hasVideo = hasVideo;
 	}
 	public Boolean getHasOffers() {
 		return hasOffers;
 	}
 	public void setHasOffers(Boolean hasOffers) {
 		this.hasOffers = hasOffers;
 	}
 	public Integer getUserReviewCount() {
 		return userReviewCount;
 	}
 	public void setUserReviewCount(Integer userReviewCount) {
 		this.userReviewCount = userReviewCount;
 	}
 	public String getSampleCategories() {
 		return sampleCategories;
 	}
 	public void setSampleCategories(String sampleCategories) {
 		this.sampleCategories = sampleCategories;
 	}
 	public String getTagline() {
 		return tagline;
 	}
 	public void setTagline(String tagline) {
 		this.tagline = tagline;
 	}
 
 	@Override
 	public int hashCode() {
 		return HashCodeBuilder.reflectionHashCode(this);
 	}
 	@Override
 	public boolean equals(Object obj) {
 	   return EqualsBuilder.reflectionEquals(this, obj);
 	}
 	@Override
 	public String toString() {
 	   return ToStringBuilder.reflectionToString(this);
 	}
 }
