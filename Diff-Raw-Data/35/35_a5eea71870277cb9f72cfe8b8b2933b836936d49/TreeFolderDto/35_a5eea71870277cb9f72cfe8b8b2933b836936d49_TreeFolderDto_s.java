 /*
  * TreeFolderDto.java
  * 
  * Copyright (C) 2012
  * 
  * This file is part of Proyecto persistenceGeo
  * 
  * This software is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * This software is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this library; if not, write to the Free Software Foundation, Inc., 51
  * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  * 
  * As a special exception, if you link this library with other files to produce
  * an executable, this library does not by itself cause the resulting executable
  * to be covered by the GNU General Public License. This exception does not
  * however invalidate any other reasons why the executable file might be covered
  * by the GNU General Public License.
  * 
  * Authors:: Alejandro Díaz Torres (mailto:adiaz@emergya.com)
  */
 package com.emergya.persistenceGeo.dto;
 
 import java.util.Date;
import java.util.LinkedList;
 
 /**
  * Folder DTO for show in folder tree
  * 
  * @author <a href="mailto:adiaz@emergya.com">adiaz</a>
  * 
  */
 public class TreeFolderDto extends FolderDto implements Treeable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7848354150705096968L;
 
 	/**
 	 * Default is true
 	 */
	private boolean leaf = true;
	
 	private FolderDto origin;
 
 	public TreeFolderDto(FolderDto origin) {
 
 		super();
		
 		this.origin = origin;
 
 		this.id = origin.id != null ? new Long(origin.id) : null; // clone id
 		this.name = origin.name != null ? new String(origin.name) : null;
 		this.enabled = origin.enabled != null ? new Boolean(origin.enabled)
 				: null;
 		this.isChannel = origin.isChannel != null ? new Boolean(
 				origin.isChannel) : null;
 		this.isPlain = origin.isChannel != null ? new Boolean(origin.isChannel)
 				: null;
 		this.createDate = origin.createDate != null ? (Date) origin.createDate
 				.clone() : null;
 		this.updateDate = origin.updateDate != null ? (Date) origin.updateDate
 				.clone() : null;
 		this.idParent = origin.idParent != null ? new Long(origin.idParent)
 				: null;
 		this.idAuth = origin.idAuth != null ? new Long(origin.idAuth) : null;
 		this.idUser = origin.idUser != null ? new Long(origin.idUser) : null;
 		this.order = origin.order != null ? new Integer(origin.order) : null;
 
 		// clone folder list
		if (origin.folderList != null) {
			this.folderList = new LinkedList<FolderDto>();
			if (origin.folderList.size() > 0) {
				this.leaf = false;
			}
		}
 
 		// clone zone
 		this.zoneId = origin.zoneId != null ? new Long(origin.zoneId) : null;
 
 	}
 
 	public boolean getLeaf() {
 		return leaf;
 	}
 
 	public void setLeaf(boolean leaf) {
 		this.leaf = leaf;
 	}
 
 	public String getText() {
 		return getName();
 	}
 
 	public String getType() {
 		return FolderDto.class.getSimpleName();
 	}
 
 	public Object getData() {
 		return origin;
 	}
 
 }
