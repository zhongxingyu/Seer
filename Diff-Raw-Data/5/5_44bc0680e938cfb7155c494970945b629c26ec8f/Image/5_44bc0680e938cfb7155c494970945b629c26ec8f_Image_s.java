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
 package com.lolay.citygrid.profile;
 
 import java.io.Serializable;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 
 import com.lolay.citygrid.UriAdapter;
 
 @XmlRootElement(name="image")
 @XmlAccessorType(value=XmlAccessType.FIELD)
 public class Image implements Serializable {
 	private static final long serialVersionUID = 1L;
 	
 	@XmlAttribute(name="type",required=true)
 	private ImageType type = null;
 	@XmlElement(name="height",required=true)
 	private Integer height = null;
 	@XmlElement(name="width",required=true)
 	private Integer width = null;
 	@XmlElement(name="image_url",required=true)
 	@XmlJavaTypeAdapter(value=UriAdapter.class)
 	private URI imageUrl = null;
 	
 	public ImageType getType() {
 		return type;
 	}
 	public void setType(ImageType type) {
 		this.type = type;
 	}
 	public Integer getHeight() {
 		return height;
 	}
 	public void setHeight(Integer height) {
 		this.height = height;
 	}
 	public Integer getWidth() {
 		return width;
 	}
 	public void setWidth(Integer width) {
 		this.width = width;
 	}
 	public URI getImageUrl() {
 		return imageUrl;
 	}
 	public void setImageUrl(URI imageUrl) {
 		this.imageUrl = imageUrl;
 	}
 	public URI getThumbnailUrl() {
 		URI image = getImageUrl();
 		if (image == null) {
 			return null;
 		}
 		if (getHeight() != null && getHeight() < 100 || getWidth() != null && getWidth() < 100) {
 			return image;
 		}
 		
 		try {
			return new URI(image.toString().replace(".", "100x100."));
 		} catch (URISyntaxException e) {
 			throw new RuntimeException(String.format("Could not convert %s to a thumbnail with *100x100.*", image), e);
 		}
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
