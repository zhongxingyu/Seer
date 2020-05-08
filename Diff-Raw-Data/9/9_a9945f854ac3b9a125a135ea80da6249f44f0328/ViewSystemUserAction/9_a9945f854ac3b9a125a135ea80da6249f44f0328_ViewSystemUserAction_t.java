 /* ===============================================================================
  *
  * Part of the InfoGlue Content Management Platform (www.infoglue.org)
  *
  * ===============================================================================
  *
  *  Copyright (C)
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License version 2, as published by the
  * Free Software Foundation. See the file LICENSE.html for more information.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
  * Place, Suite 330 / Boston, MA 02111-1307 / USA.
  *
  * ===============================================================================
  */
 
 package org.infoglue.cms.applications.managementtool.actions;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.GroupControllerProxy;
 import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;
 import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
 import org.infoglue.cms.controllers.kernel.impl.simple.UserPropertiesController;
 import org.infoglue.cms.entities.management.ContentTypeDefinitionVO;
import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.security.InfoGluePrincipal;
 
 public class ViewSystemUserAction extends InfoGlueAbstractAction
 {
 	private static final long serialVersionUID = 1L;
 
 	private String userName;
 	private boolean supportsUpdate = true;
 	private InfoGluePrincipal infoGluePrincipal;
 	private List roles = new ArrayList();
 	private List assignedRoleVOList;
 	private List groups = new ArrayList();
 	private List assignedGroupVOList;
 	private List contentTypeDefinitionVOList;   
 	private List assignedContentTypeDefinitionVOList; 	
 	
     protected void initialize(String userName) throws Exception
     {
 		//this.supportsUpdate					= UserControllerProxy.getController().getSupportUpdate();
 		this.infoGluePrincipal				= UserControllerProxy.getController().getUser(userName);
 		
		if(infoGluePrincipal == null)
			throw new SystemException("No user found called '" + userName + "'. This could be an encoding issue if you gave your user a login name with non ascii chars in it. Look in the administrative manual on how to solve it.");

		this.supportsUpdate					= this.infoGluePrincipal.getAutorizationModule().getSupportUpdate();
					
 		this.assignedRoleVOList 			= infoGluePrincipal.getRoles();
 		if(this.supportsUpdate) //Only fetch if the user can edit.
 			this.roles 						= this.infoGluePrincipal.getAutorizationModule().getRoles();
 		
 		this.assignedGroupVOList 			= infoGluePrincipal.getGroups();
 		if(this.supportsUpdate) //Only fetch if the user can edit.
 			this.groups 					= this.infoGluePrincipal.getAutorizationModule().getGroups();
 		
 		this.contentTypeDefinitionVOList 			= ContentTypeDefinitionController.getController().getContentTypeDefinitionVOList(ContentTypeDefinitionVO.EXTRANET_USER_PROPERTIES);
 		this.assignedContentTypeDefinitionVOList 	= UserPropertiesController.getController().getContentTypeDefinitionVOList(userName);  
     } 
 
     public String doExecute() throws Exception
     {
         this.initialize(getUserName());
         
         return "success";
     }
 		
 	public List getAssignedRoles() throws Exception
 	{
 		return this.assignedRoleVOList;
 	}        
 
 	public List getAllRoles() throws Exception
 	{
 		return this.roles;
 	}        
 
 	public List getAssignedGroups() throws Exception
 	{
 		return this.assignedGroupVOList;
 	}        
 
 	public List getAllGroups() throws Exception
 	{
 		return this.groups;
 	}        
     
 	public List getContentTypeDefinitionVOList()
 	{
 		return contentTypeDefinitionVOList;
 	}
 
 	public String getUserName() 
 	{
 		return this.userName;
 	}
 
 	public void setUserName(String userName)
 	{
 		this.userName = userName;
 	}
 
 	public String getFirstName() 
 	{
 		return infoGluePrincipal.getFirstName();
 	}
 	
 	public String getLastName() 
 	{
 		return infoGluePrincipal.getLastName();
 	}
 
 	public String getEmail() 
 	{
 		return infoGluePrincipal.getEmail();
 	}
 	
 	public boolean getSupportsUpdate()
 	{
 		return this.supportsUpdate;
 	}
 		
 	public List getAssignedContentTypeDefinitionVOList()
 	{
 		return assignedContentTypeDefinitionVOList;
 	}
 
 }
