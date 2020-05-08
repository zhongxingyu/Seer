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
 
 package org.infoglue.cms.entities.content;
 
 import org.infoglue.cms.exception.*;
 import org.infoglue.cms.entities.kernel.BaseEntityVO;
 import org.infoglue.cms.util.ConstraintExceptionBuffer;
 import org.infoglue.cms.util.validators.*;
 
 
 import java.util.Date;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 
 public class ContentVO implements BaseEntityVO
 { 
 	public static final Integer NO 			= new Integer(0);
 	public static final Integer YES 		= new Integer(1);
 	public static final Integer INHERITED 	= new Integer(2);
 	
     private java.lang.Integer contentId;
     private java.lang.String name			= "";
     private java.util.Date publishDateTime  = new Date();
     private java.util.Date expireDateTime   = new Date();
     private java.lang.Boolean isBranch		= new Boolean(false);              
 	private java.lang.Integer isProtected	= INHERITED;
 	private java.lang.Integer repositoryId  = null;
 	private java.lang.Integer contentTypeDefinitionId  	= null;
 	private java.lang.Integer parentContentId  			= null;
 	
 	private Integer childCount;
   	private String creatorName;
   
   	//Used if an application wants to add more properties to this item... used for performance reasons.
   	private Map extraProperties = new Hashtable();
   	
   	public ContentVO()
   	{
   		//Initilizing the expireDateTime... 
   		Calendar calendar = Calendar.getInstance();
   		calendar.add(Calendar.YEAR, 10);
   		expireDateTime = calendar.getTime();
   	}
   	
 	/**
 	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#getId()
 	 */
 	public Integer getId() 
 	{
 		return getContentId();
 	}	
   	
     public java.lang.Integer getContentId()
     {
         return this.contentId;
     }
     
     public void setRepositoryId(java.lang.Integer repositoryId)
     {
         this.repositoryId = repositoryId;
     }
     
     public java.lang.Integer getRepositoryId()
     {
         return this.repositoryId;
     }
     
                 
     public void setContentId(java.lang.Integer contentId)
     {
         this.contentId = contentId;
     }
     
     public java.lang.String getName()
     {
         return this.name;
     }
                 
     public void setName(java.lang.String name)
     {
         this.name = name;
     }
     
     public java.util.Date getPublishDateTime()
     {
         return this.publishDateTime;
     }
                 
     public void setPublishDateTime(java.util.Date publishDateTime)
     {
         this.publishDateTime = publishDateTime;
     }
     
     public java.util.Date getExpireDateTime()
     {
         return this.expireDateTime;
     }
                 
     public void setExpireDateTime(java.util.Date expireDateTime)
     {
         this.expireDateTime = expireDateTime;
     }
                   
     public java.lang.Boolean getIsBranch()
     {
     	return this.isBranch;
 	}
     
     public void setIsBranch(java.lang.Boolean isBranch)
 	{
 		this.isBranch = isBranch;
 	}
 	
 	
 	/**
 	 * @see org.infoglue.cms.entities.kernel.BaseEntityVO#validate()
 	 */
 	public ConstraintExceptionBuffer validate() 
 	{ 
 		ConstraintExceptionBuffer ceb = new ConstraintExceptionBuffer();
  		ValidatorFactory.createStringValidator("Content.name", true, 2, 100).validate(name, ceb);
  		
 		if(this.publishDateTime.after(this.expireDateTime))
			ceb.add(new ConstraintException("Content.publishDateTime", "308"));
 		
 		return ceb;
 	}	
 
 	/**
 	 * Returns the childCount.
 	 * @return Integer
 	 */
 	public Integer getChildCount()
 	{
 		return childCount;
 	}
 
 	/**
 	 * Sets the childCount.
 	 * @param childCount The childCount to set
 	 */
 	public void setChildCount(Integer childCount)
 	{
 		this.childCount = childCount;
 	}
 
 	/**
 	 * Returns the creatorName.
 	 * @return String
 	 */
 	public String getCreatorName()
 	{
 		return creatorName;
 	}
 
 	/**
 	 * Sets the creatorName.
 	 * @param creatorName The creatorName to set
 	 */
 	public void setCreatorName(String creatorName)
 	{
 		this.creatorName = creatorName;
 	}
 
 	public java.lang.Integer getIsProtected()
 	{
 		return isProtected;
 	}
 
 	public void setIsProtected(java.lang.Integer isProtected)
 	{
 		this.isProtected = isProtected;
 	}
 
 	public java.lang.Integer getContentTypeDefinitionId()
 	{
 		return contentTypeDefinitionId;
 	}
 
 	public void setContentTypeDefinitionId(java.lang.Integer contentTypeDefinitionId)
 	{
 		this.contentTypeDefinitionId = contentTypeDefinitionId;
 	}
 
 	public java.lang.Integer getParentContentId()
 	{
 		return parentContentId;
 	}
 
 	public void setParentContentId(java.lang.Integer parentContentId)
 	{
 		this.parentContentId = parentContentId;
 	}
 
 	public String toString()
 	{
 		StringBuffer sb = new StringBuffer();
 		sb.append("id=").append(contentId)
 			.append(" name=").append(name)
 			.append(" publishDateTime=").append(publishDateTime)
 			.append(" expireDateTime=").append(expireDateTime)
 			.append(" isBranch=").append(isBranch)
 			.append(" isProtected=").append(isProtected)
 			.append(" repositoryId=").append(repositoryId)
 			.append(" contentTypeDefinitionId=").append(contentTypeDefinitionId)
 			.append(" parentContentId=").append(parentContentId)
 			.append(" creatorName=").append(creatorName);
 		return sb.toString();
 	}
 	
     public Map getExtraProperties()
     {
         return extraProperties;
     }
     
     public void setExtraProperties(Map extraProperties)
     {
         this.extraProperties = extraProperties;
     }
 }
         
