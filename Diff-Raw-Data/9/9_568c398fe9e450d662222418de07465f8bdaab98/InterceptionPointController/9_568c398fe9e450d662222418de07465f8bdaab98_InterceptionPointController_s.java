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
 
 package org.infoglue.cms.controllers.kernel.impl.simple;
 
 import org.infoglue.cms.entities.kernel.BaseEntityVO;
 import org.infoglue.cms.entities.management.AccessRight;
 import org.infoglue.cms.entities.management.AccessRightGroup;
 import org.infoglue.cms.entities.management.AccessRightRole;
 import org.infoglue.cms.entities.management.AccessRightUser;
 import org.infoglue.cms.entities.management.InterceptionPoint;
 import org.infoglue.cms.entities.management.InterceptionPointVO;
 import org.infoglue.cms.entities.management.Interceptor;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.management.impl.simple.InterceptionPointImpl;
 import org.infoglue.cms.exception.*;
 
 import org.infoglue.cms.util.ConstraintExceptionBuffer;
 import org.infoglue.deliver.util.CacheController;
 
 import org.exolab.castor.jdo.Database;
 import org.exolab.castor.jdo.OQLQuery;
 import org.exolab.castor.jdo.QueryResults;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This class is a helper class for the use case handle InterceptionPoint
  *
  * @author Mattias Bogeblad
  */
 
 public class InterceptionPointController extends BaseController
 {
 	public final static Map systemInterceptionPoints = new HashMap();
     
 	static
 	{
 	    systemInterceptionPoints.put("Category.Read", new InterceptionPointVO("Category", "Category.Read", "This intercepts any read towards a category", true));
 	    systemInterceptionPoints.put("Content.Read", new InterceptionPointVO("Content", "Content.Read", "Intercepts the read of a content", true));
 	    systemInterceptionPoints.put("Content.Write", new InterceptionPointVO("Content", "Content.Write", "Intercepts the write of a content", true));
 	    systemInterceptionPoints.put("Content.Create", new InterceptionPointVO("Content", "Content.Create", "Intercepts the creation of a new content or folder", true));
 	    systemInterceptionPoints.put("Content.Delete", new InterceptionPointVO("Content", "Content.Delete", "Intercepts the deletion of a content", true));
 	    systemInterceptionPoints.put("Content.Move", new InterceptionPointVO("Content", "Content.Move", "Intercepts the movement of a content", true));
 	    systemInterceptionPoints.put("Content.SubmitToPublish", new InterceptionPointVO("Content", "Content.SubmitToPublish", "Intercepts the submittance to publish of all content versions", true));
 	    systemInterceptionPoints.put("Content.ChangeAccessRights", new InterceptionPointVO("Content", "Content.ChangeAccessRights", "Intercepts the attempt to change access rights", true));
 	    systemInterceptionPoints.put("Content.CreateVersion", new InterceptionPointVO("Content", "Content.CreateVersion", "Intercepts the creation of a new contentversion", true));
 	    systemInterceptionPoints.put("ContentTool.Read", new InterceptionPointVO("ContentTool", "ContentTool.Read", "Gives a user access to the content tool", false));
 	    systemInterceptionPoints.put("ContentTypeDefinition.Read", new InterceptionPointVO("ContentTypeDefinition", "ContentTypeDefinition.Read", "This point checks access to read/use a content type definition", true));
 	    systemInterceptionPoints.put("ContentVersion.Delete", new InterceptionPointVO("ContentVersion", "ContentVersion.Delete", "Intercepts the deletion of a contentversion", true));
 	    systemInterceptionPoints.put("ContentVersion.Write", new InterceptionPointVO("ContentVersion", "ContentVersion.Write", "Intercepts the editing of a contentversion", true));
 	    systemInterceptionPoints.put("ContentVersion.Read", new InterceptionPointVO("ContentVersion", "ContentVersion.Read", "Intercepts the read of a contentversion", true));
 	    systemInterceptionPoints.put("ContentVersion.Publish", new InterceptionPointVO("ContentVersion", "ContentVersion.Publish", "Intercepts the direct publishing of a content version", true));
 	    systemInterceptionPoints.put("ManagementTool.Read", new InterceptionPointVO("ManagementTool", "ManagementTool.Read", "Gives a user access to the management tool", false));
 	    systemInterceptionPoints.put("MyDesktopTool.Read", new InterceptionPointVO("MyDesktopTool", "MyDesktopTool.Read", "Gives the user access to the mydesktop tool", false));
 	    systemInterceptionPoints.put("PublishingTool.Read", new InterceptionPointVO("PublishingTool", "PublishingTool.Read", "Gives the user access to the publishing tool", false));
 	    systemInterceptionPoints.put("Repository.Read", new InterceptionPointVO("Repository", "Repository.Read", "Gives a user access to look at a repository", true));
 	    systemInterceptionPoints.put("Repository.ReadForBinding", new InterceptionPointVO("Repository", "Repository.ReadForBinding", "This point intercepts when a user tries to read the repository in a binding dialog", true));
 	    systemInterceptionPoints.put("SiteNodeVersion.Read", new InterceptionPointVO("SiteNodeVersion", "SiteNodeVersion.Read", "Intercepts the read of a SiteNodeVersion", true));
 	    systemInterceptionPoints.put("SiteNodeVersion.Write", new InterceptionPointVO("SiteNodeVersion", "SiteNodeVersion.Write", "Intercepts the write of a SiteNodeVersion", true));
 	    systemInterceptionPoints.put("SiteNodeVersion.CreateSiteNode", new InterceptionPointVO("SiteNodeVersion", "SiteNodeVersion.CreateSiteNode", "Intercepts the creation of a new sitenode", true));
 	    systemInterceptionPoints.put("SiteNodeVersion.DeleteSiteNode", new InterceptionPointVO("SiteNodeVersion", "SiteNodeVersion.DeleteSiteNode", "Intercepts the deletion of a sitenode", true));
 	    systemInterceptionPoints.put("SiteNodeVersion.MoveSiteNode", new InterceptionPointVO("SiteNodeVersion", "SiteNodeVersion.MoveSiteNode", "Intercepts the movement of a sitenode", true));
 	    systemInterceptionPoints.put("SiteNodeVersion.SubmitToPublish", new InterceptionPointVO("SiteNodeVersion", "SiteNodeVersion.SubmitToPublish", "Intercepts the submittance to publish of all content versions", true));
 	    systemInterceptionPoints.put("SiteNodeVersion.ChangeAccessRights", new InterceptionPointVO("SiteNodeVersion", "SiteNodeVersion.ChangeAccessRights", "Intercepts the attempt to change access rights", true));
 	    systemInterceptionPoints.put("SiteNodeVersion.Publish", new InterceptionPointVO("SiteNodeVersion", "SiteNodeVersion.Publish", "Intercepts the direct publishing of a siteNode version", true));
 	    systemInterceptionPoints.put("StructureTool.Read", new InterceptionPointVO("StructureTool", "StructureTool.Read", "Gives a user access to the structure tool", false));
 	    systemInterceptionPoints.put("StructureTool.SaveTemplate", new InterceptionPointVO("StructureTool", "StructureTool.SaveTemplate", "This interception point limits who get the save-button in the toolbar", false));
 	    systemInterceptionPoints.put("StructureTool.Palette", new InterceptionPointVO("StructureTool", "StructureTool.Palette", "This interception point limits who sees the toolbar", false));
 	    systemInterceptionPoints.put("ComponentEditor.ChangeSlotAccess", new InterceptionPointVO("ComponentEditor", "ComponentEditor.ChangeSlotAccess", "This interception point limits who can set access rights to a slot", false));
 	    systemInterceptionPoints.put("ComponentEditor.AddComponent", new InterceptionPointVO("ComponentEditor", "ComponentEditor.AddComponent", "This interception point limits who can add a component to a specific slot", true));
 	    systemInterceptionPoints.put("ComponentEditor.DeleteComponent", new InterceptionPointVO("ComponentEditor", "ComponentEditor.DeleteComponent", "This interception point limits who can delete a component in a specific slot", true));
 	    systemInterceptionPoints.put("ComponentPropertyEditor.EditProperty", new InterceptionPointVO("ComponentPropertyEditor", "ComponentPropertyEditor.EditProperty", "This interception point limits who can edit a specific component property", true));
 	}
     
 	/**
 	 * Factory method
 	 */
 
 	public static InterceptionPointController getController()
 	{
 		return new InterceptionPointController();
 	}
 	
 	public InterceptionPoint getInterceptionPointWithId(Integer interceptionPointId, Database db) throws SystemException, Bug
 	{
 		return (InterceptionPoint) getObjectWithId(InterceptionPointImpl.class, interceptionPointId, db);
 	}
     
 	public InterceptionPointVO getInterceptionPointVOWithId(Integer interceptionPointId) throws SystemException, Bug
 	{
 		return (InterceptionPointVO) getVOWithId(InterceptionPointImpl.class, interceptionPointId);
 	}
   
 	public List getInterceptionPointVOList() throws SystemException, Bug
 	{
 		return getAllVOObjects(InterceptionPointImpl.class, "interceptionPointId");
 	}
 
 	public List getSortedInterceptionPointVOList() throws SystemException, Bug
 	{
 		return getAllVOObjects(InterceptionPointImpl.class, "category", "asc");
 	}
 	
 	public List getInterceptorsVOList(Integer interceptionPointId) throws SystemException, Bug
 	{
 		List interceptorVOList = null;
 		
 		Database db = CastorDatabaseService.getDatabase();
 
 		try 
 		{
 			beginTransaction(db);
 
 			interceptorVOList = getInterceptorsVOList(interceptionPointId, db);
 
 			commitTransaction(db);
 		} 
 		catch (Exception e) 
 		{
 			getLogger().info("An error occurred so we should not complete the transaction:" + e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 		
 		return interceptorVOList;	
 	}
 	
 	/**
 	 * Gets the interceptors for this interceptionPoint withing a transaction
 	 * 
 	 * @param interceptionPointId
 	 * @param db
 	 * @return
 	 * @throws SystemException
 	 * @throws Bug
 	 */
 	
 	public List getInterceptorsVOList(Integer interceptionPointId, Database db)  throws SystemException, Bug
 	{
 		String key = "" + interceptionPointId;
 		getLogger().info("key:" + key);
 		List cachedInterceptorVOList = (List)CacheController.getCachedObject("interceptorsCache", key);
 		if(cachedInterceptorVOList != null)
 		{
 			getLogger().info("There was an cached InterceptorVOList:" + cachedInterceptorVOList.size());
 			return cachedInterceptorVOList;
 		}
 		
 		List interceptorsVOList = null;
 		
 		InterceptionPoint interceptionPoint = this.getInterceptionPointWithId(interceptionPointId, db);
 		
 		Collection interceptors = interceptionPoint.getInterceptors();
 		
 		interceptorsVOList = toVOList(interceptors);
 		
 		CacheController.cacheObject("interceptorsCache", key, interceptorsVOList);
 
 		return interceptorsVOList;		
 	}
 
 
 	public InterceptionPointVO getInterceptionPointVOWithName(String interceptorPointName)  throws SystemException, Bug
 	{
 		InterceptionPointVO interceptionPointVO = null;
 		
 		Database db = CastorDatabaseService.getDatabase();
 
 		try 
 		{
 			beginTransaction(db);
 
 			InterceptionPoint interceptionPoint = getInterceptionPointWithName(interceptorPointName, db);
 			if(interceptionPoint != null)
 				interceptionPointVO = interceptionPoint.getValueObject();
 
 			commitTransaction(db);
 		} 
 		catch (Exception e) 
 		{
 			getLogger().info("An error occurred so we should not complete the transaction:" + e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 		
 		return interceptionPointVO;		
 	}	
 
 	public InterceptionPointVO getInterceptionPointVOWithName(String interceptorPointName, Database db)  throws SystemException, Bug
 	{
 		String key = "" + interceptorPointName;
 		getLogger().info("key:" + key);
 		InterceptionPointVO interceptionPointVO = (InterceptionPointVO)CacheController.getCachedObject("interceptionPointCache", key);
 		if(interceptionPointVO != null)
 		{
 			getLogger().info("There was an cached interceptionPointVO:" + interceptionPointVO);
 		}
 		else
 		{
 	
 			InterceptionPoint interceptorPoint = null;
 			
 			try
 			{
 				OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.InterceptionPointImpl f WHERE f.name = $1");
 				oql.bind(interceptorPointName);
 				
 				QueryResults results = oql.execute(Database.ReadOnly);
 				if(results.hasMore()) 
 				{
 					interceptorPoint = (InterceptionPoint)results.next();
 					interceptionPointVO = interceptorPoint.getValueObject();
 				
 					CacheController.cacheObject("interceptionPointCache", key, interceptionPointVO);				
 				}
 			}
 			catch(Exception e)
 			{
 				throw new SystemException("An error occurred when we tried to fetch an InterceptionPointVO. Reason:" + e.getMessage(), e);    
 			}
 		}
 		
 		return interceptionPointVO;		
 	}	
 
 
 	public InterceptionPoint getInterceptionPointWithName(String interceptorPointName, Database db)  throws SystemException, Bug
 	{
 		InterceptionPoint interceptorPoint = null;
 		
 		try
 		{
 			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.InterceptionPointImpl f WHERE f.name = $1");
 			oql.bind(interceptorPointName);
 			
 			QueryResults results = oql.execute();
 			this.getLogger().info("Fetching entity in read/write mode:" + interceptorPointName);
 			if(results.hasMore()) 
 			{
 				interceptorPoint = (InterceptionPoint)results.next();
 			}
 		}
 		catch(Exception e)
 		{
 			throw new SystemException("An error occurred when we tried to fetch an InterceptionPointVO. Reason:" + e.getMessage(), e);    
 		}
 		
 		return interceptorPoint;		
 	}
 	
 	
 	public List getInterceptionPointVOList(String category) throws SystemException, Bug
 	{
 		List interceptionPointVOList = null;
 		
 		Database db = CastorDatabaseService.getDatabase();
 
 		try 
 		{
 			beginTransaction(db);
 
 			interceptionPointVOList = toVOList(getInterceptionPointList(category, db));
 
 			commitTransaction(db);
 		} 
 		catch (Exception e) 
 		{
 			getLogger().info("An error occurred so we should not complete the transaction:" + e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 		
 		return interceptionPointVOList;	
 	}
 	
 	
 	public List getInterceptionPointList(String category, Database db)  throws SystemException, Bug
 	{
 		List interceptionPoints = new ArrayList();
 		
 		try
 		{
 			OQLQuery oql = db.getOQLQuery("SELECT f FROM org.infoglue.cms.entities.management.impl.simple.InterceptionPointImpl f WHERE f.category = $1");
 			oql.bind(category);
 			
 			QueryResults results = oql.execute();
 			this.getLogger().info("Fetching entity in read/write mode:" + category);
 
 			while(results.hasMore()) 
 			{
 				InterceptionPoint interceptionPoint = (InterceptionPoint)results.next();
 				interceptionPoints.add(interceptionPoint);
 			}
 		}
 		catch(Exception e)
 		{
 			throw new SystemException("An error occurred when we tried to fetch an InterceptionPointVO. Reason:" + e.getMessage(), e);    
 		}
 		
 		return interceptionPoints;		
 	}
 
 	/**
 	 * Creates a new Interception point
 	 * 
 	 * @param interceptionPointVO
 	 * @return
 	 * @throws ConstraintException
 	 * @throws SystemException
 	 */
 	
 	public InterceptionPointVO create(InterceptionPointVO interceptionPointVO) throws ConstraintException, SystemException
 	{
 		InterceptionPointVO newInterceptionPointVO = null;
 		
 		Database db = CastorDatabaseService.getDatabase();
 
 		try 
 		{
 			beginTransaction(db);
 
 			newInterceptionPointVO = create(interceptionPointVO, db);
 				
 			commitTransaction(db);
 		} 
 		catch (Exception e) 
 		{
 			getLogger().info("An error occurred so we should not complete the transaction:" + e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 		
 		return newInterceptionPointVO;
 	}		
 	
 	/**
 	 * Creates a new Interception point within a transaction
 	 * 
 	 * @param interceptionPointVO
 	 * @return
 	 * @throws ConstraintException
 	 * @throws SystemException
 	 */
 	
 	public InterceptionPointVO create(InterceptionPointVO interceptionPointVO, Database db) throws SystemException, Exception
 	{
 		InterceptionPoint interceptionPoint = new InterceptionPointImpl();
 		interceptionPoint.setValueObject(interceptionPointVO);
 		
 		db.create(interceptionPoint);
 					
 		return interceptionPoint.getValueObject();
 	}     
 
 	
 	public InterceptionPointVO update(InterceptionPointVO interceptionPointVO) throws ConstraintException, SystemException
 	{
 		return (InterceptionPointVO) updateEntity(InterceptionPointImpl.class, interceptionPointVO);
 	}        
 
 	
 	public void update(InterceptionPointVO interceptionPointVO, String[] values) throws ConstraintException, SystemException
 	{
 		Database db = CastorDatabaseService.getDatabase();
 
 		try 
 		{
 			beginTransaction(db);
 			
 			ConstraintExceptionBuffer ceb = interceptionPointVO.validate();
 			ceb.throwIfNotEmpty();
 			
 			InterceptionPoint interceptionPoint = this.getInterceptionPointWithId(interceptionPointVO.getInterceptionPointId(), db);
 
 			interceptionPoint.setValueObject(interceptionPointVO);
 			
 			Collection interceptors = interceptionPoint.getInterceptors();
 			Iterator interceptorsIterator = interceptors.iterator();
 			while(interceptorsIterator.hasNext())
 			{
 				Interceptor interceptor = (Interceptor)interceptorsIterator.next();
 				interceptor.getInterceptionPoints().remove(interceptor);
 			}
 			
 			interceptionPoint.getInterceptors().clear();
 	    	
 	    	if(values != null)
 	    	{
 				for(int i=0; i<values.length; i++)
 				{
 					String interceptorId = values[i];
 					Interceptor interceptor = InterceptorController.getController().getInterceptorWithId(new Integer(interceptorId), db);
 					interceptionPoint.getInterceptors().add(interceptor);
 					interceptor.getInterceptionPoints().add(interceptionPoint);
 				}
 			}
 			
 	    	getLogger().info("Interceptors:" + interceptionPoint.getInterceptors().size());
 			
 			commitTransaction(db);
 		} 
 		catch (Exception e) 
 		{
 			getLogger().info("An error occurred so we should not complete the transaction:" + e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 	}			
 	
 	
 	public void delete(InterceptionPointVO interceptionPointVO) throws ConstraintException, SystemException
 	{
 		Database db = CastorDatabaseService.getDatabase();
 		
 		beginTransaction(db);
 
 		try
 		{
 			InterceptionPoint interceptionPoint = this.getInterceptionPointWithId(interceptionPointVO.getInterceptionPointId(), db);
 			
 			List accessRights = AccessRightController.getController().getAccessRightList(interceptionPoint.getInterceptionPointId(), db);
 			Iterator accessRightsIterator = accessRights.iterator();
 			while(accessRightsIterator.hasNext())
 			{
 				AccessRight accessRight = (AccessRight)accessRightsIterator.next();
 				
 				Iterator groupIterator = accessRight.getGroups().iterator();
 				while(groupIterator.hasNext())
 				{
 					AccessRightGroup group = (AccessRightGroup)groupIterator.next();
 					groupIterator.remove();
 					db.remove(group);
 				}
 				
 				Iterator roleIterator = accessRight.getRoles().iterator();
 				while(roleIterator.hasNext())
 				{
 					AccessRightRole role = (AccessRightRole)roleIterator.next();
 					roleIterator.remove();
 					db.remove(role);
 				}
 
 				Iterator userIterator = accessRight.getUsers().iterator();
 				while(userIterator.hasNext())
 				{
 					AccessRightUser user = (AccessRightUser)userIterator.next();
 					userIterator.remove();
 					db.remove(user);
 				}
 
 				db.remove(accessRight);
 				accessRightsIterator.remove();
 			}
 			
 			db.remove(interceptionPoint);
 	
 			commitTransaction(db);
 		}
 		catch(Exception e)
 		{
 			getLogger().error("An error occurred so we should not complete the transaction:" + e, e);
 			rollbackTransaction(db);
 			throw new SystemException(e.getMessage());
 		}
 	
 	}        
 
 	/*
 	public void delete(String name, String value, Database db) throws SystemException, Exception
 	{
 		List AccessList = getAccessList(name, value, db);
 		Iterator i = AccessList.iterator();
 		while(i.hasNext())
 		{
 			Access Access = (Access)i.next();
 			db.remove(Access);
 		}
 		
 	}        
 	*/
 
 	/**
 	 * This is a method that gives the user back an newly initialized ValueObject for this entity that the controller
 	 * is handling.
 	 */
 
 	public BaseEntityVO getNewVO()
 	{
 		return new InterceptionPointVO();
 	}
 
 }
  
