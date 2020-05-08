 /*
  * Hackerspace Bremen Backend - An Open-Space-Notifier
  * 
  * Copyright (C) 2012 Steve Liedtke <sliedtke57@gmail.com>
  * 
  * This program is free software; you can redistribute it and/or modify it under the terms of the 
  * GNU General Public License as published by the Free Software Foundation; either version 3 of 
  * the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See 
  * the GNU General Public License for more details.
  * 
  * You can find a copy of the GNU General Public License on http://www.gnu.org/licenses/gpl.html.
  * 
  * Contributors:
  *     Steve Liedtke <sliedtke57@gmail.com>
  */
 package de.hackerspacebremen.data.entities;
 
 import com.googlecode.objectify.annotation.Id;
 import com.googlecode.objectify.annotation.Index;
 
 import de.hackerspacebremen.data.annotations.Entity;
 import de.hackerspacebremen.data.annotations.FormatPart;
 
 @com.googlecode.objectify.annotation.Entity
 @Entity(name="GCMData")
 public final class GCMData implements BasicEntity{
 
 	@Id
 	@FormatPart(key="GCM1")
 	private Long id;
 	
 	@FormatPart(key="GCM2")
 	@Index
 	private String deviceId;
 	
 	@FormatPart(key="GCM3")
 	private String registrationId;
 	
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getDeviceId() {
 		return deviceId;
 	}
 
 	public void setDeviceId(String deviceId) {
 		this.deviceId = deviceId;
 	}
 
 	public String getRegistrationId() {
 		return registrationId;
 	}
 
 	public void setRegistrationId(String registrationId) {
 		this.registrationId = registrationId;
 	}
 }
