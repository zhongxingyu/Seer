 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.logmanager.web.extension;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.openmrs.module.Extension;
 import org.openmrs.module.logmanager.Constants;
 
 /**
  * Adds the module link to the maintenance menu
  */
 public class MaintenanceLinksExtension extends Extension {
 
 	/**
 	 * Gets the list of links to be displayed
 	 * @return the list of links
 	 */
 	public Map<String, String> getLinks() {
 		Map<String, String> links = new HashMap<String, String>();
 		links.put("module/" + Constants.MODULE_ID + "/config.list", Constants.MODULE_ID + ".admin.link");
 		return links;
 	}
 
 	/**
 	 * @see org.openmrs.module.Extension#getMediaType()
 	 */
 	@Override
 	public MEDIA_TYPE getMediaType() {
 		return MEDIA_TYPE.html;
	}	
 }
