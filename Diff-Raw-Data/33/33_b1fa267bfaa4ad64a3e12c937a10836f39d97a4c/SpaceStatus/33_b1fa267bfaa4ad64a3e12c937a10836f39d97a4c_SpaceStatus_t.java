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
 
 
 import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.annotation.Cache;
 import com.googlecode.objectify.annotation.Id;
 import com.googlecode.objectify.annotation.Index;
 
 import de.hackerspacebremen.common.AppConstants;
 import de.hackerspacebremen.data.annotations.Entity;
 import de.hackerspacebremen.data.annotations.FormatPart;
 import flexjson.JSON;
 
 @Entity(name="SpaceStatus")
 @com.googlecode.objectify.annotation.Entity
@Cache
 public final class SpaceStatus implements BasicEntity{
 	
 	@Id
 	@FormatPart(key="ST1")
 	private Long id;
 	
 	@FormatPart(key="ST2", level=AppConstants.LEVEL_VIEW)
 	@Index
 	private long time;
 	
 	@FormatPart(key="ST3", level=AppConstants.LEVEL_VIEW)
 	@Index
 	private String status;
 	
 	@FormatPart(key="ST4")
 	private String openedBy;
 
 	@FormatPart(key="ST5", level=AppConstants.LEVEL_VIEW)
 	private Text message;
 	
 	public SpaceStatus(){
 		// constructor for objectify
 	}
 	
 	public SpaceStatus(final SpaceStatus originalStatus){
 		this.id = Long.valueOf(0L);
 		this.time = originalStatus.getTime();
 		this.status = originalStatus.getStatus();
 		this.openedBy = originalStatus.getOpenedBy();
 		if(originalStatus.getMessage()==null){
 			this.message = null;
 		}else{
 			this.message = new Text(originalStatus.getMessage().getValue());
 		}
 	}
 
 	public Long getId(){
 		return id;
 	}
 
 	public void setId(Long id){
 		this.id = id;
 	}
 	
 	@JSON
 	public long getTime() {
 		return time;
 	}
 
 	public void setTime(long time) {
 		this.time = time;
 	}
 
 	@JSON
 	public String getStatus() {
 		return status;
 	}
 
 	public void setStatus(String status) {
 		this.status = status;
 	}
 
 	public String getOpenedBy() {
 		return openedBy;
 	}
 
 	public void setOpenedBy(final String openedBy) {
 		this.openedBy = openedBy;
 	}
 
 	@JSON
 	public Text getMessage() {
 		return message;
 	}
 
 	public void setMessage(Text message) {
 		this.message = message;
 	}
 
 }
