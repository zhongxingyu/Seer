 package com.optaros.alfresco.docasu.wcs;
 
 /*
  *    Copyright (C) 2008 Optaros, Inc. All rights reserved.
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program. If not, see <http://www.gnu.org/licenses/>.
  *    
  */
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.faces.context.FacesContext;
 
 import org.alfresco.filesys.CIFSServer;
 import org.alfresco.web.app.servlet.FacesHelper;
 import org.alfresco.web.scripts.DeclarativeWebScript;
 import org.alfresco.web.scripts.WebScriptRequest;
 import org.alfresco.web.scripts.WebScriptStatus;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 public class ModelData extends DeclarativeWebScript {
 		
 	public Map<String, Object> executeImpl(WebScriptRequest req, WebScriptStatus status) {
 		
 		//retrieve the cifs server name without making use of the Faces Context
 		//not available if loading the custom ui via "[...]/service/[...]" instead of "[...]/wcs/[...]"
 		//String cifsServername = ((CIFSServer)FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "cifsServer")).getConfiguration().getServerName();
 		String cifsServername = ((CIFSServer)WebApplicationContextUtils.getWebApplicationContext(this.getWebScriptRegistry().getContext()).getBean("cifsServer")).getConfiguration().getServerName();
 				
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("cifsServer", cifsServername);
 		
 		return model;
 	}
 	
 }
