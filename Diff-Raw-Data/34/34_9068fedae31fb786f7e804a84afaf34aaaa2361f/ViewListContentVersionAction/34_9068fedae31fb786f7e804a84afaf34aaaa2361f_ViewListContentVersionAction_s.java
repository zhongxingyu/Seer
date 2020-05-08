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
 
 package org.infoglue.cms.applications.contenttool.actions;
 
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.controllers.kernel.impl.simple.AccessRightController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentControllerProxy;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentVersionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeVersionController;
 import org.infoglue.cms.entities.content.ContentVO;
 import org.infoglue.cms.entities.content.ContentVersionVO;
 import org.infoglue.cms.entities.structure.SiteNodeVersionVO;
 import org.infoglue.cms.exception.AccessConstraintException;
 import org.infoglue.cms.util.AccessConstraintExceptionBuffer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  *  @author Stefan Sik
  * 
  * Present a list of contentVersion under a given content, recursing down in the hierarcy
  * 
  */
 
 public class ViewListContentVersionAction extends InfoGlueAbstractAction 
 {
 
 	private List contentVersionVOList = new ArrayList();
 	private List siteNodeVersionVOList = new ArrayList();
 	private Integer contentId;
 	private Integer repositoryId;
 
 	protected String doExecute() throws Exception 
 	{
 		if(this.contentId != null)
 		{
 		    ContentVO contentVO = ContentController.getContentController().getContentVOWithId(this.contentId);
 		    this.repositoryId = contentVO.getRepositoryId();
 		    
 			AccessConstraintExceptionBuffer ceb = new AccessConstraintExceptionBuffer();
 		
 			Integer protectedContentId = ContentControllerProxy.getController().getProtectedContentId(contentId);
 			if(protectedContentId != null && !AccessRightController.getController().getIsPrincipalAuthorized(this.getInfoGluePrincipal(), "Content.SubmitToPublish", protectedContentId.toString()))
 				ceb.add(new AccessConstraintException("Content.contentId", "1005"));
 			
 			ceb.throwIfNotEmpty();
 
 			ContentVersionController.getContentVersionController().getContentAndAffectedItemsRecursive(this.contentId, ContentVersionVO.WORKING_STATE, this.siteNodeVersionVOList, this.contentVersionVOList, true);
 
 			//contentVersionVOList = ContentVersionController.getContentVersionController().getContentVersionVOWithParentRecursiveAndRelated(contentId, ContentVersionVO.WORKING_STATE);		
 		}
 
 	    return "success";
 	}
 		
 	/**
 	 * @return
 	 */
 	public Integer getContentId() 
 	{
 		return contentId;
 	}
 
 	/**
 	 * @param integer
 	 */
 	public void setContentId(Integer integer) 
 	{
 		contentId = integer;
 	}
 
     public Integer getRepositoryId()
     {
         return repositoryId;
     }
     
     public List getContentVersionVOList()
     {
         return contentVersionVOList;
     }
     
     public List getSiteNodeVersionVOList()
     {
         return siteNodeVersionVOList;
     }
 }
