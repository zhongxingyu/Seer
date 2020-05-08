 /**
  * Copyright (c) 2010, Todd Ginsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  *    * Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the following disclaimer.
  *    * Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    * Neither the name of Todd Ginsberg, or Gowalla nor the
  *      names of any contributors may be used to endorse or promote products
  *      derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  *  Also, please use this for Good and not Evil.  
  */
 package com.ginsberg.gowalla.dto;
 
 import java.io.Serializable;
 
 /**
  * Basic data returned when referring to users.
  * 
  * @author Todd Ginsberg
  */
 public class User implements Serializable, Id<User>{
 
 	private static final long serialVersionUID = -3013545889422798921L;
 	protected int id;
 	protected String url;
 	protected String first_name;
 	protected String last_name;
 	protected String image_url;
 
 	/**
 	 * Constructor!
 	 */
 	public User() {
 		super();
 	}
 
 	@Override
 	public int getId() {
 		return id;
 	}
 
 	@Override
 	public String getUrl() {
 		return url;
 	}
 
 	@Override
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	@Override
 	public void setUrl(String url) {
 		this.url = url;
 	}
 	
 
 	public String getFirstName() {
 		return first_name;
 	}
 
 	public void setFirstName(String firstName) {
 		this.first_name = firstName;
 	}
 
 	public String getLastName() {
 		return last_name;
 	}
 
 	public void setLastName(String lastName) {
 		this.last_name = lastName;
 	}
 	
 	public String getName() {
 		return String.format("%s %s", first_name, last_name).trim();
 	}
	
	public String getImageUrl() {
		return image_url;
	}

	public void setImageUrl(String imageUrl) {
		this.image_url = imageUrl;
	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + id;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		User other = (User) obj;
 		if (id != other.id)
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("BasicUser[id=%d, name=%s %s]", id, first_name, last_name);
 	}
 	
 }
