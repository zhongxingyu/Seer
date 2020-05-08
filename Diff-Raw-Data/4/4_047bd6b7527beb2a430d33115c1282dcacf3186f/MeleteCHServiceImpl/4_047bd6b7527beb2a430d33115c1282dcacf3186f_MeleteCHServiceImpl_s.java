 /**********************************************************************************
 *
 * $Header:
 *
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007 Foothill College, ETUDES Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 **********************************************************************************/
 
 package org.sakaiproject.component.app.melete;
 
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.net.URLDecoder;
 
 import org.sakaiproject.api.app.melete.MeleteCHService;
 import org.sakaiproject.api.app.melete.MeleteSecurityService;
 import org.sakaiproject.component.app.melete.MeleteUtil;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.api.app.melete.exception.MeleteException;
 import org.sakaiproject.content.api.ContentCollectionEdit;
 import org.sakaiproject.content.api.ContentCollection;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.content.api.ContentResourceEdit;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ContentEntity;
 import org.sakaiproject.content.cover.ContentTypeImageService;
 import org.sakaiproject.entity.api.Entity;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 import org.sakaiproject.exception.IdInvalidException;
 import org.sakaiproject.exception.IdLengthException;
 import org.sakaiproject.exception.IdUniquenessException;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.InUseException;
 import org.sakaiproject.exception.IdUsedException;
 import org.sakaiproject.exception.TypeException;
 import org.sakaiproject.exception.InconsistentException;
 import org.sakaiproject.exception.OverQuotaException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.exception.ServerOverloadException;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.entity.cover.EntityManager;
 import org.sakaiproject.entity.api.Reference;
 
 /*
  * Created on Sep 18, 2006 by rashmi
  *
  *  This is a basic class to do all content hosting service work through melete
  *  Rashmi - 12/8/06 - add checks for upload img being greater than 1Mb
  * Rashmi - 12/16/06 - fck editor upload image goes to melete collection and not resources.
  * Rashmi - 1/16/07 - remove word inserted comments
  * Mallika - 1/29/06 - Needed to add some new code to add url for embedded images
  * Rashmi - changed logic to get list of images and url
  * Rashmi - 2/12/07 - add alternate reference property at collection level too
  * Rashmi - 3/6/07 - revised display name of modules collection with no slashes
  * Rashmi - 5/9/07 - revised iteration to get listoflinks
  * Mallika - 5/31/07 - added Validator
  * Mallika - 6/4/07 - Added two new methods (copyIntoFolder,getCollection)
  * Mallika - 6/4/07 - Added logic to check for resource in findlocal
  */
 public class MeleteCHServiceImpl implements MeleteCHService {
 	/** Dependency:  The logging service. */
 	 public Log logger = LogFactory.getLog(MeleteCHServiceImpl.class);
 	 private ContentHostingService contentservice;
 	 private static final int MAXIMUM_ATTEMPTS_FOR_UNIQUENESS = 100;
 	 private SectionDB sectiondb;
 	 /** Dependency: The Melete Security service. */
 	 private MeleteSecurityService meleteSecurityService;
 	 protected MeleteUtil meleteUtil = new MeleteUtil();
 
 	 /** This string starts the references to resources in this service. */
 		static final String REFERENCE_ROOT = Entity.SEPARATOR+"meleteDocs";
 
 	 /**
 		 * Check if the current user has permission as author.
 		 * @return true if the current user has permission to perform this action, false if not.
 		 */
 	public boolean isUserAuthor()throws Exception{
 
 			try {
 				return meleteSecurityService.allowAuthor();
 			} catch (Exception e) {
 				throw e;
 			}
 	}
 	public boolean isUserStudent()throws Exception{
 
 		try {
 			return meleteSecurityService.allowStudent();
 		} catch (Exception e) {
 			throw e;
 		}
    }
 
 	private String addMeleteRootCollection(String rootCollectionRef, String collectionName, String description)
 	{
 		try
 		{
 			 //	Check to see if meleteDocs exists
 			 ContentCollection collection = getContentservice().getCollection(rootCollectionRef);
 			return collection.getId();
 		}
 		catch (IdUnusedException e)
 		{
 			//if not, create it
 			if (logger.isDebugEnabled()) logger.debug("creating melete root collection "+rootCollectionRef);
 			ContentCollectionEdit edit = null;
 			try
 			{
 				edit = getContentservice().addCollection(rootCollectionRef);
 				ResourcePropertiesEdit props = edit.getPropertiesEdit();
 				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, collectionName);
                 props.addProperty(ResourceProperties.PROP_DESCRIPTION, description);
                 props.addProperty(getContentservice().PROP_ALTERNATE_REFERENCE, REFERENCE_ROOT);
 				getContentservice().commitCollection(edit);
 				return edit.getId();
 			}		
 			catch (Exception e2)
 			{	
 				if(edit != null) getContentservice().cancelCollection(edit);
 				logger.warn("creating melete root collection: " + e2.toString());
 			}
    		}
 		catch (Exception e)
 		{
 			logger.warn("checking melete root collection: " + e.toString());
 		}
 		return null;
 	}
 	/*
      *
      */
 	 public String addCollectionToMeleteCollection(String meleteItemColl,String CollName)
      {
             try
     	    {
             	if (!isUserAuthor())
             		{
             		logger.info("User is not authorized to access meleteDocs collection");
             		}
        			//          setup a security advisor
             		meleteSecurityService.pushAdvisor();
             		try
 					{
 	    			// check if the root collection is available
 	    				String rootCollectionRef = Entity.SEPARATOR+"private"+ REFERENCE_ROOT+ Entity.SEPARATOR;
 	    				//Check to see if meleteDocs exists
 	    				String rootMeleteCollId = addMeleteRootCollection(rootCollectionRef, "meleteDocs", "root collection");
 	    				// now sub collection for course
 	    				String meleteItemDirCollId = rootMeleteCollId + meleteItemColl+ Entity.SEPARATOR;
 	    				String subMeleteCollId = addMeleteRootCollection(meleteItemDirCollId, CollName, "module collection");
 	    				return subMeleteCollId;
 					}
                  catch(Exception ex)
              	 {
                      logger.error("error while creating modules collection" + ex.toString());
                  }
     	    }
 		  catch (Throwable t)
 		  {
 			logger.warn("init(): ", t);
 		  }
 		  finally
 		  {
 			// clear the security advisor
 			meleteSecurityService.popAdvisor();
 		  }
 
            return null;
         }
 
     /*
      *
      */
 	 public String getCollectionId( String contentType,Integer modId )
     {
         String addToCollection ="";
         String collName ="";
         if(contentType.equals("typeEditor")){
 
         addToCollection=ToolManager.getCurrentPlacement().getContext()+Entity.SEPARATOR+"module_"+ modId;
 
 	    collName = "module_"+ modId;
         }
         else {
             if (ToolManager.getCurrentPlacement()!=null)
 	        addToCollection=ToolManager.getCurrentPlacement().getContext()+Entity.SEPARATOR+"uploads";
             else
                 addToCollection=getMeleteSecurityService().getMeleteImportService().getDestinationContext()+Entity.SEPARATOR+"uploads";
 
         	collName = "uploads";
         }
         //      check if collection exists otherwise create it
         String addCollId = addCollectionToMeleteCollection(addToCollection,collName);
         return addCollId;
     }
 
     /*
      *
      */
 	 public String getUploadCollectionId()
 	{
 		try{
             String uploadCollectionId;
             uploadCollectionId=ToolManager.getCurrentPlacement().getContext()+Entity.SEPARATOR+"uploads";
 
 	    String collName = "uploads";
 	    // check if collection exists
 	    //read meletDocs dir name from web.xml
         String uploadCollId = addCollectionToMeleteCollection(uploadCollectionId, collName);
         return uploadCollId;
 		}catch(Exception e)
 		{
 			logger.error("error accessing uploads directory");
 			return null;
 		}
     }
 
 	//Methods used by Migrate program  - beginning
 	 public String getCollectionId(String courseId, String contentType,Integer modId )
 	    {
 	        String addToCollection ="";
 	        String collName ="";
 	        if(contentType.equals("typeEditor")){
 				addToCollection=courseId+Entity.SEPARATOR+"module_"+ modId;
 				collName = "module_"+ modId;
 	        }
 	        else {
 	        	addToCollection=courseId+Entity.SEPARATOR+"uploads";
 	        	collName = "uploads";
 	        }
 	        //      check if collection exists otherwise create it
 	        String addCollId = addCollectionToMeleteCollection(addToCollection,collName);
 	        return addCollId;
 	    }
 	 public String getUploadCollectionId(String courseId)
 		{
 			try{
 		    String uploadCollectionId=courseId+Entity.SEPARATOR+"uploads";
 		    String collName = "uploads";
 		    // check if collection exists
 		    //read meletDocs dir name from web.xml
 	        String uploadCollId = addCollectionToMeleteCollection(uploadCollectionId, collName);
 	        return uploadCollId;
 			}catch(Exception e)
 			{
 				logger.error("error accessing uploads directory");
 				return null;
 			}
 	    }
 //	Methods used by Migrate program  - End
 
 	/*
 	 * for remote browser listing for sferyx editor just get the image files
 	 **/
 	 public List getListofImagesFromCollection(String collId)
 	{
 		try
 		{
 			// setup a security advisor
 			meleteSecurityService.pushAdvisor();
 
 			long starttime = System.currentTimeMillis();
 			logger.debug("time to get all collectionMap" + starttime);
 			ContentCollection c = getContentservice().getCollection(collId);
 			List mem = c.getMemberResources();
 			if (mem == null) return null;
 
 			ListIterator memIt = mem.listIterator();
 			while (memIt != null && memIt.hasNext())
 			{
 				ContentEntity ce = (ContentEntity) memIt.next();
 				if (ce.isResource())
 				{
 					String contentextension = ((ContentResource) ce).getProperties().getProperty(ce.getProperties().getNamePropContentType());
 
 					if (!contentextension.startsWith("image"))
 					{
 						memIt.remove();
 					}
 				}
 				else memIt.remove();
 			}
 			long endtime = System.currentTimeMillis();
 			logger.debug("end time to get all collectionMap" + (endtime - starttime));
 			return mem;
 		}
 		catch (Exception e)
 		{
 			logger.error(e.toString());
 		}
 		finally
 		{
 			// clear the security advisor
 			meleteSecurityService.popAdvisor();
 		}
 
 		return null;
 	}
 
 	 public List getListofFilesFromCollection(String collId)
 	 {
 	   try
 	    {
       			// setup a security advisor
         		meleteSecurityService.pushAdvisor();
 
         		long starttime = System.currentTimeMillis();
         		logger.debug("time to get all collectionMap" + starttime);
 			 	ContentCollection c= getContentservice().getCollection(collId);
 			 	List	mem = c.getMemberResources();
 			 	if (mem == null) return null;
 
 			 	ListIterator memIt = mem.listIterator();
 			 	while(memIt !=null && memIt.hasNext())
 			 	{
 			 		ContentEntity ce = (ContentEntity)memIt.next();
 			 		if (ce.isResource())
 			 		{
 			 		String contentextension = ((ContentResource)ce).getContentType();
 			 		if(contentextension.equals(MIME_TYPE_LINK) || contentextension.equals(MIME_TYPE_EDITOR))
 			 		{
 			 			 memIt.remove();
 			 		}
 			 		} else  memIt.remove();
 			 	}
 			 	long endtime = System.currentTimeMillis();
         		logger.debug("end time to get all collectionMap" + (endtime - starttime));
 			return mem;
 	    }
 		catch(Exception e)
 		{
 			logger.error(e.toString());
 		}
 		finally
 		  {
 			// clear the security advisor
 			meleteSecurityService.popAdvisor();
 		  }
 
 		return null;
 	 }
 		/*
 		 * for remote link browser listing for sferyx editor
 		 * just get the urls and links and not image files
 		 */
 	 public List getListofLinksFromCollection(String collId)
 		 {
 		 	try
 		    {
 	        	if (!isUserAuthor())
 	        		{
 	        		logger.info("User is not authorized to access meleteDocs collection");
 	        		}
 	   			//          setup a security advisor
 	        		meleteSecurityService.pushAdvisor();
 	        	 	ContentCollection c= getContentservice().getCollection(collId);
 				 	List	mem = c.getMemberResources();
 				 	if (mem == null) return null;
 
 				 	ListIterator memIt = mem.listIterator();
 				 	while(memIt !=null && memIt.hasNext())
 				 	{
 				 		ContentEntity ce = (ContentEntity)memIt.next();
 				 		if (ce.isResource())
 				 		{
 				 		String contentextension = ((ContentResource)ce).getContentType();
 				 		if(!contentextension.equals(MIME_TYPE_LINK))
 				 			 memIt.remove();
 				 		}else  memIt.remove();
 				 	}
 
 				return mem;
 		    }
 			catch(Exception e)
 			{
 				logger.error(e.toString());
 			}
 			finally
 			  {
 				// clear the security advisor
 				meleteSecurityService.popAdvisor();
 			  }
 			return null;
 		 }
 
 	 public List getListofMediaFromCollection(String collId)
 	 {
 		 	try
 		    {
 	        	if (!isUserAuthor())
 	        		{
 	        		logger.info("User is not authorized to access meleteDocs collection");
 	        		}
 	   			//          setup a security advisor
 	        		meleteSecurityService.pushAdvisor();
 	        	 	ContentCollection c= getContentservice().getCollection(collId);
 				 	List	mem = c.getMemberResources();
 				 	if (mem == null) return null;
 
 				 	ListIterator memIt = mem.listIterator();
 				 	while(memIt !=null && memIt.hasNext())
 				 	{
 				 		ContentEntity ce = (ContentEntity)memIt.next();
 				 		if (ce.isResource())
 				 		{
 				 		String contentextension = ((ContentResource)ce).getContentType();
 				 		if(contentextension.equals(MIME_TYPE_EDITOR))
 				 			 memIt.remove();
 				 		}else  memIt.remove();
 				 	}
 
 				return mem;
 		    }
 			catch(Exception e)
 			{
 				logger.error(e.toString());
 			}
 			finally
 			  {
 				// clear the security advisor
 				meleteSecurityService.popAdvisor();
 			  }
 			return null;
 		 }
 
 	/*
 	 *
 	 */
 	 public ResourcePropertiesEdit fillInSectionResourceProperties(boolean encodingFlag,String secResourceName, String secResourceDescription)
 	{
 	    ResourcePropertiesEdit resProperties = getContentservice().newResourceProperties();
 
 	//  resProperties.addProperty (ResourceProperties.PROP_COPYRIGHT,);
 	//  resourceProperties.addProperty(ResourceProperties.PROP_COPYRIGHT_CHOICE,);
 	    //Glenn said to not set the two properties below
 	  //  resProperties.addProperty(ResourceProperties.PROP_COPYRIGHT_ALERT,Boolean.TRUE.toString());
 		//resProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION,Boolean.FALSE.toString());
 		resProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME,	secResourceName);
 		resProperties.addProperty(ResourceProperties.PROP_DESCRIPTION, secResourceDescription);
 		resProperties.addProperty(getContentservice().PROP_ALTERNATE_REFERENCE, REFERENCE_ROOT);
 
 		//Glenn said the property below may not need to be set either, will leave it in
 		//unless it creates problems
 		if (encodingFlag)
 			resProperties.addProperty(ResourceProperties.PROP_CONTENT_ENCODING,	"UTF-8");
 		return resProperties;
 
 	}
 	/*
 	 *  resource properties for images embedded in the editor content
 	 */
 	 public ResourcePropertiesEdit fillEmbeddedImagesResourceProperties(String name)
 	{
 	    ResourcePropertiesEdit resProperties = getContentservice().newResourceProperties();
 
 		//Glenn said to not set the two properties below
 		//resProperties.addProperty(ResourceProperties.PROP_COPYRIGHT_ALERT,Boolean.TRUE.toString());
 		//resProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION,Boolean.FALSE.toString());
 		resProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
 		resProperties.addProperty(ResourceProperties.PROP_DESCRIPTION,"embedded image");
 		resProperties.addProperty(getContentservice().PROP_ALTERNATE_REFERENCE, REFERENCE_ROOT);
 		return resProperties;
 
 	}
 	/*
 	 *
 	 */
 	 public void editResourceProperties(String selResourceIdFromList, String secResourceName, String secResourceDescription)
 	{
 		 if(selResourceIdFromList == null || selResourceIdFromList.length() == 0) return;
 		 ContentResourceEdit edit = null;
 	 	try
 	    {
         	if (!isUserAuthor())
         		{
         		logger.info("User is not authorized to access meleteDocs collection");
         		}
    			//          setup a security advisor
         		meleteSecurityService.pushAdvisor();
         		selResourceIdFromList = URLDecoder.decode(selResourceIdFromList,"UTF-8");
         		edit = getContentservice().editResource(selResourceIdFromList);
 				ResourcePropertiesEdit rp = edit.getPropertiesEdit();
 				rp.clear();
 				rp.addProperty(ResourceProperties.PROP_DISPLAY_NAME,secResourceName);
 				rp.addProperty(ResourceProperties.PROP_DESCRIPTION,secResourceDescription);
 				rp.addProperty(getContentservice().PROP_ALTERNATE_REFERENCE, REFERENCE_ROOT);
 				getContentservice().commitResource(edit);
 				edit = null;
 	    }
 		catch(Exception e)
 		{			    
 			logger.error(e.toString());
 		}
 		finally
 		  {
 			if(edit != null) getContentservice().cancelResource(edit);
 			// clear the security advisor
 			meleteSecurityService.popAdvisor();
 		  }
 		return;
 	}
 
 	 public String addResourceItem(String name, String res_mime_type,String addCollId, byte[] secContentData, ResourcePropertiesEdit res ) throws Exception
 	{
 		 ContentResource resource = null;
 		 name = URLDecoder.decode(name,"UTF-8");
 		 if (logger.isDebugEnabled()) logger.debug("IN addResourceItem "+name+" addCollId "+addCollId);
 		// need to add notify logic here and set the arg6 accordingly.
 		try
  	    {
 		    if (!isUserAuthor())
 		    {
 		       logger.info("User is not authorized to add resource");
 		    }
 //       setup a security advisor
          meleteSecurityService.pushAdvisor();
          try
 			{
 				String finalName = addCollId + name;
 				if (finalName.length() > getContentservice().MAXIMUM_RESOURCE_ID_LENGTH)
 				{
 					// leaving room for CHS inserted duplicate filenames -1 -2 etc
 					int extraChars = finalName.length() - getContentservice().MAXIMUM_RESOURCE_ID_LENGTH + 3;
 					name = name.substring(0, name.length() - extraChars);
 				}
 				resource = getContentservice().addResource(name, addCollId, MAXIMUM_ATTEMPTS_FOR_UNIQUENESS, res_mime_type, secContentData, res, 0);
 				// check if its duplicate file and edit the resource name if it is
 				String checkDup = resource.getUrl().substring(resource.getUrl().lastIndexOf("/") + 1);
 				ContentResourceEdit edit = null;
 				try
 				{					
 					if (!checkDup.equals(name))
 					{
 						edit = getContentservice().editResource(resource.getId());
 						ResourcePropertiesEdit rp = edit.getPropertiesEdit();
 						String desc = rp.getProperty(ResourceProperties.PROP_DESCRIPTION);
 						rp.clear();
 						rp.addProperty(ResourceProperties.PROP_DISPLAY_NAME, checkDup);
 						rp.addProperty(ResourceProperties.PROP_DESCRIPTION, desc);
 						rp.addProperty(getContentservice().PROP_ALTERNATE_REFERENCE, REFERENCE_ROOT);
 						getContentservice().commitResource(edit);
 						edit = null;
 					}
 				}
 				catch (Exception ex)
 				{					
 					throw ex;
 				}
 				finally
 				{
 					if (edit != null) getContentservice().cancelResource(edit);
 				}
 			}
 			catch(PermissionException e)
 			{
 			logger.error("permission is denied");
 			}
 			catch(IdInvalidException e)
 			{
 			logger.error("title" + " " + e.getMessage ());
 			throw new MeleteException("failed");
 			}
 			catch(IdLengthException e)
 			{
 			logger.error("The name is too long" + " " +e.getMessage());
 			throw new MeleteException("failed");
 			}
 			catch(IdUniquenessException e)
 			{
 			logger.error("Could not add this resource item to melete collection");
 			throw new MeleteException("failed");
 			}
 			catch(InconsistentException e)
 			{
 			logger.error("Invalid characters in collection title");
 
 			throw new MeleteException("failed");
 			}
 			catch(OverQuotaException e)
 			{
 			logger.error("Adding this resource would place this account over quota.To add this resource, some resources may need to be deleted.");
 			throw new MeleteException("failed");
 			}
 			catch(ServerOverloadException e)
 			{
 			logger.error("failed - internal error");
 			throw new MeleteException("failed");
 			}
 			catch(RuntimeException e)
 			{
 			logger.error("SectionPage.addResourcetoMeleteCollection ***** Unknown Exception ***** " + e.getMessage());
 			e.printStackTrace();
 			throw new MeleteException("failed");
 			}
 	     } catch (Exception e) {
 		  logger.error(e.toString());
 	     }
 	     finally{
 			meleteSecurityService.popAdvisor();
 		}
 	     return resource.getId();
 	}
 
 	 /*
 	  *
 	  */
 	 public List getAllResources(String uploadCollId)
 	 {
 	 	try
 	    {
         	if (!isUserAuthor())
         		{
         		logger.info("User is not authorized to access meleteDocs collection");
         		}
    			//          setup a security advisor
         		meleteSecurityService.pushAdvisor();
         		return (getContentservice().getAllResources(uploadCollId));
 	    }
 		catch(Exception e)
 		{
 			logger.error(e.toString());
 		}
 		finally
 		  {
 			// clear the security advisor
 			meleteSecurityService.popAdvisor();
 		  }
 		return null;
 	 }
 	 /*
 	  *
 	  */
 	 public ContentResource getResource(String resourceId) throws Exception
 	 {
 	 	try
 	    {
         	if (!isUserAuthor() && !isUserStudent())
         		{
         		logger.info("User is not authorized to access meleteDocs collection");
         		}
    			//          setup a security advisor
         		meleteSecurityService.pushAdvisor();
         		resourceId = URLDecoder.decode(resourceId,"UTF-8");
         		return (getContentservice().getResource(resourceId));
 	    }
 		catch(Exception e)
 		{
 			logger.error(e.toString());
 			throw e;
 		}
 		finally
 		  {
 			// clear the security advisor
 			meleteSecurityService.popAdvisor();
 		  }
 	 }
 
 	 public void checkResource(String resourceId) throws Exception
 	 {
 	 	try
 	    {
         	if (!isUserAuthor())
         		{
         		logger.info("User is not authorized to access meleteDocs collection");
         		}
    			//          setup a security advisor
         		meleteSecurityService.pushAdvisor();
         		resourceId = URLDecoder.decode(resourceId,"UTF-8");
         		getContentservice().checkResource(resourceId);
         		return;
 	    }
 	 	catch (IdUnusedException ex)
 		{
 	 		logger.debug("resource is not available so create one" + resourceId);
 	 		throw ex;
 		}
 		catch(Exception e)
 		{
 			logger.error(e.toString());
 		}
 		finally
 		  {
 			// clear the security advisor
 			meleteSecurityService.popAdvisor();
 		  }
 		return ;
 	 }
 
 	/*
 	 *
 	 */
 	 public void editResource(String resourceId, String contentEditor) throws Exception
 	 {
 		 ContentResourceEdit edit = null;
 	 	try
 	    {
         	if (!isUserAuthor())
         		{
         		logger.info("User is not authorized to access meleteDocs collection");
         		}
    			//          setup a security advisor
         		meleteSecurityService.pushAdvisor();
         		if (resourceId != null)
         		{
         		  resourceId = URLDecoder.decode(resourceId,"UTF-8");
         		  edit = getContentservice().editResource(resourceId);
         		  edit.setContent(contentEditor.getBytes());
         		  edit.setContentLength(contentEditor.length());
         		  getContentservice().commitResource(edit);
         		  edit = null;
         		}
         		return;
 	    }
 	 	catch(Exception e)
 		{
 			logger.error("error saving editor content "+e.toString());			
 			throw e;
 		}
 		finally
 		  {
 			if(edit != null) getContentservice().cancelResource(edit);
 			// clear the security advisor
 			meleteSecurityService.popAdvisor();
 		  }
 	 }
 
 	 /*
 	     *  before saving editor content, look for embedded images from local system
 	     *  and add them to collection
 	     * if filename has # sign then throw error
 	     *  Do see diego's fix to paste from word works
 	     */
 	    public String findLocalImagesEmbeddedInEditor(String uploadHomeDir, String contentEditor) throws MeleteException
 	    {
 	    	 String checkforimgs = contentEditor;
 	         // get collection id where the embedded files will go
 	         String UploadCollId = getUploadCollectionId();
 	         String fileName;
 			 int startSrc =0;
 			 int endSrc = 0;
 			 String foundLink = null;
              // look for local embedded images
 	         try
 			 {
 	         	  if (!isUserAuthor())
 	             	{
 	         		  logger.info("User is not authorized to access meleteDocs");
 	         		  return null;
 	             	}
 	         	//  remove MSword comments
 	      	  	int wordcommentIdx = -1;
 	      	  	while (checkforimgs != null && (wordcommentIdx = checkforimgs.indexOf("<!--[if gte vml 1]>")) != -1)
 	    		{
 	    			String pre = checkforimgs.substring(0,wordcommentIdx);
 	    			checkforimgs = checkforimgs.substring(wordcommentIdx+19);
 	    			int endcommentIdx = checkforimgs.indexOf("<![endif]-->");
 	    			checkforimgs = checkforimgs.substring(endcommentIdx+12);
 	    			checkforimgs= pre + checkforimgs;
 	    			wordcommentIdx = -1;
 	    		}
 	      	  //for FCK editor inserted comments
 	      	wordcommentIdx = -1;
 	      	while (checkforimgs != null && (wordcommentIdx = checkforimgs.indexOf("<!--[if !vml]-->")) != -1)
     		{
     			String pre = checkforimgs.substring(0,wordcommentIdx);
     			checkforimgs = checkforimgs.substring(wordcommentIdx);
     			int endcommentIdx = checkforimgs.indexOf("<!--[endif]-->");
     			checkforimgs = checkforimgs.substring(endcommentIdx+14);
     			checkforimgs= pre + checkforimgs;
     			wordcommentIdx = -1;
     		}
 	    		contentEditor = checkforimgs;
 	    		// remove word comments code end
 
 	    		//check for form tag and remove it
 	    		checkforimgs = meleteUtil.findFormPattern(checkforimgs);
 
 				contentEditor = checkforimgs;
 
 		         while(checkforimgs !=null)
 		         {
 		           // look for a href and img tag
 		        	ArrayList embedData = meleteUtil.findEmbedItemPattern(checkforimgs);
 
 	    			checkforimgs = (String)embedData.get(0);
 	    			if (embedData.size() > 1)
 	    			{
 	    				startSrc = ((Integer)embedData.get(1)).intValue();
 	    				endSrc = ((Integer)embedData.get(2)).intValue();
 	    				foundLink = (String) embedData.get(3);
 	    			}
 	    			if (endSrc <= 0) break;
 	    			// find filename
 	    			fileName = checkforimgs.substring(startSrc, endSrc);
 	    			 String patternStr = fileName;
 	    			logger.debug("processing embed src" + fileName);	    	
 	    			
 	    			//process for local uploaded files
 					if(fileName != null && fileName.trim().length() > 0&& (!(fileName.equals(File.separator)))
 						&& fileName.startsWith("file:/") )
 					{
 		              // word paste fix
 		             patternStr= meleteUtil.replace(patternStr,"\\","/");
 		             contentEditor = meleteUtil.replace(contentEditor,fileName,patternStr);
 		             checkforimgs =  meleteUtil.replace(checkforimgs,fileName,patternStr);
 
 		             fileName = patternStr;
 		  	  	    fileName = fileName.substring(fileName.lastIndexOf("/")+1);
 
 //			  	  	 if filename contains pound char then throw error
 					if(fileName.indexOf("#") != -1)
 			  	  	{
 			  	  	logger.error("embedded FILE contains hash or other characters " + fileName);
 	  	  		    throw new MeleteException("embed_img_bad_filename");
 			  	  	}
 		             // add the file to collection and move from uploads directory
 		             // read data
 		             try{
 		             File re = new File(uploadHomeDir+File.separator+fileName);
 
 		             byte[] data = new byte[(int)re.length()];
 		             FileInputStream fis = new FileInputStream(re);
 		             fis.read(data);
 		             fis.close();
 		             re.delete();
 
 		             // add as a resource to uploads collection
 		             String file_mime_type = fileName.substring(fileName.lastIndexOf(".")+1);
 		             file_mime_type = ContentTypeImageService.getContentType(file_mime_type);
 
 
 		            String newEmbedResourceId = null;
 		            //If the resource already exists, use it
 		            try
 	                {
 	                   	  String checkResourceId = UploadCollId + "/" + fileName;
 	                   	  checkResource(checkResourceId);
 				 	      newEmbedResourceId = checkResourceId;
 	                }
 	                catch (IdUnusedException ex2)
 			        {
 	                	ResourcePropertiesEdit res =fillEmbeddedImagesResourceProperties(fileName);
 	                	newEmbedResourceId = addResourceItem(fileName,file_mime_type,UploadCollId,data,res );
 			        }
 
 		            //add in melete resource database table also
 		             MeleteResource meleteResource = new MeleteResource();
 	            	 meleteResource.setResourceId(newEmbedResourceId);
 	            	 //set default license info to "I have not determined copyright yet" option
 	            	 meleteResource.setLicenseCode(0);
 	            	 sectiondb.insertResource(meleteResource);
 
 		         	// in content editor replace the file found with resource reference url
 		         	 String replaceStr = getResourceUrl(newEmbedResourceId);
 		         	 replaceStr = meleteUtil.replace(replaceStr,ServerConfigurationService.getServerUrl(),"");
 		         	 logger.debug("repl;acestr in embedimage processing is " + replaceStr);
 
 		         	 // Replace all occurrences of pattern in input
 		            Pattern pattern = Pattern.compile(patternStr);
 
 		            //Rashmi's change to fix infinite loop on uploading images
 		            contentEditor = meleteUtil.replace(contentEditor,patternStr,replaceStr);
 		            checkforimgs = meleteUtil.replace(checkforimgs,patternStr,replaceStr);
 		             }
 		             catch(FileNotFoundException ff)
 					 {
 		             	logger.error(ff.toString());
 		             	throw new MeleteException("embed_image_size_exceed");
 					 }
 				}
 				// for internal links to make it relative
 				else
 				{					
 					if (fileName.startsWith(ServerConfigurationService.getServerUrl()))
 					{
 						if (fileName.indexOf("/meleteDocs") != -1)
 						{
 							String findEntity = fileName.substring(fileName.indexOf("/access") + 7);
 							Reference ref = EntityManager.newReference(findEntity);
 							logger.debug("ref properties" + ref.getType() + "," + ref.getId());
 							String newEmbedResourceId = ref.getId();
 							newEmbedResourceId = newEmbedResourceId.replaceFirst("/content", "");
 
 							if (sectiondb.getMeleteResource(newEmbedResourceId) == null)
 							{
 								// add in melete resource database table also
 								MeleteResource meleteResource = new MeleteResource();
 								meleteResource.setResourceId(newEmbedResourceId);
 								// set default license info to "I have not determined copyright yet" option
 								meleteResource.setLicenseCode(0);
 								sectiondb.insertResource(meleteResource);
 							}
 
 						}
 						String replaceStr = meleteUtil.replace(fileName, ServerConfigurationService.getServerUrl(), "");
 						contentEditor = meleteUtil.replace(contentEditor, patternStr, replaceStr);
 						checkforimgs = meleteUtil.replace(checkforimgs, patternStr, replaceStr);
 					}
 					// process links and append http:// protocol if not provided
 					else if (!fileName.startsWith("/access")
 							&& foundLink != null
 							&& foundLink.equals("link")
 							&& !(fileName.startsWith("http://") || fileName.startsWith("https://") || fileName.startsWith("mailto:") || fileName
 									.startsWith("#")))
 					{
 						logger.debug("processing embed link src for appending protocol");
 						String replaceLinkStr = "http://" + fileName;
 						contentEditor = meleteUtil.replace(contentEditor, fileName, replaceLinkStr);
 						checkforimgs = meleteUtil.replace(checkforimgs, fileName, replaceLinkStr);
 					}
 
 				}
 				
 				// add target if not provided
     			if(foundLink != null && foundLink.equals("link"))
     			{
     				String soFar =  checkforimgs.substring(0,endSrc);
     				String checkTarget = checkforimgs.substring(endSrc , checkforimgs.indexOf(">")+1);
     				String laterPart = checkforimgs.substring(checkforimgs.indexOf(">")+2);
     				Pattern pa = Pattern.compile("\\s[tT][aA][rR][gG][eE][tT]\\s*=");
     				Matcher m = pa.matcher(checkTarget);
     				if(!m.find())
     				{
     					String newTarget = meleteUtil.replace(checkTarget, ">", " target=_blank >");
     					checkforimgs = soFar + newTarget + laterPart;
     					contentEditor = meleteUtil.replace(contentEditor, soFar + checkTarget, soFar+newTarget);
     				}
     			}
 		            // iterate next
		            checkforimgs =checkforimgs.substring(endSrc);
 		            startSrc=0; endSrc = 0; foundLink = null;
 		         }
 			 }
 	         catch(MeleteException me) {throw me;}
 	         catch(Exception e){logger.error(e.toString());e.printStackTrace();}
 
 	    	return contentEditor;
 	    }
 
 	    public List findAllEmbeddedImages(String sec_resId) throws Exception
 	    {
 	    	try{
 	    	ContentResource cr = getResource(sec_resId);
 	    	String checkforImgs = new String(cr.getContent());
 	    	List secEmbedData = new ArrayList<String> (0);
 	    	int startSrc=0, endSrc=0;
 	    	if (checkforImgs == null || checkforImgs.length() == 0) return null;
 	    	while(checkforImgs != null)
 	    	{
 	    		 // look for a href and img tag
 	        	ArrayList embedData = meleteUtil.findEmbedItemPattern(checkforImgs);
     			checkforImgs = (String)embedData.get(0);
     			if (embedData.size() > 1)
     			{
     				startSrc = ((Integer)embedData.get(1)).intValue();
     				endSrc = ((Integer)embedData.get(2)).intValue();
     			}
     			if (endSrc <= 0) break;
     			// find filename and add just the ones which belongs to meleteDocs
     			String fileName = checkforImgs.substring(startSrc, endSrc);
     			if(fileName.startsWith("/access/meleteDocs/content"))
     			{
     			fileName = fileName.replace("/access/meleteDocs/content", "");
     			fileName = URLDecoder.decode(fileName,"UTF-8");
     			secEmbedData.add(fileName);
     			}
     			// iterate next
     			checkforImgs =checkforImgs.substring(endSrc);
 	            startSrc=0; endSrc = 0; 
 	    	}
 	    	return secEmbedData;
 	    	} catch(Exception e)
 	    	{
 	    		logger.debug("can't read section file" + sec_resId);
 	    	}
 	    	return null;
 	    }
 
 
 	    /*
 	     * get the URL for replaceStr of embedded images
 	     */
 	    public String getResourceUrl(String newResourceId)
 	    {
 	    	try
      	    {
      	      	meleteSecurityService.pushAdvisor();
      	      	newResourceId = URLDecoder.decode(newResourceId,"UTF-8");
      	      	return getContentservice().getUrl(newResourceId);
              }
        catch (Exception e)
            {
      	      e.printStackTrace();
      	     return "";
            }
        finally
            {
          	  meleteSecurityService.popAdvisor();
            }
 
 	    }
 
 
 
 	  public void copyIntoFolder(String fromColl,String toColl)
 	  {
 			try
 		    {
 	        if (!isUserAuthor())
 	        {
 	        		logger.info("User is not authorized to perform the copyIntoFolder function");
 	        }
 	   			//          setup a security advisor
 	        meleteSecurityService.pushAdvisor();
 		    try
 			{
 	         getContentservice().copyIntoFolder(fromColl, toColl);
 	       }
 	       catch(InconsistentException e)
           {
             logger.error("Inconsistent exception thrown");
           }
 	       catch(IdLengthException e)
           {
             logger.error("IdLength exception thrown");
           }
 	       catch(IdUniquenessException e)
           {
             logger.error("IdUniqueness exception thrown");
           }
           catch(PermissionException e)
           {
             logger.error("Permission to copy uploads collection is denied");
           }
           catch(IdUnusedException e)
           {
             logger.error("Failed to create uploads collection in second site");
           }
           catch(TypeException e)
           {
             logger.error("TypeException thrown: "+e.getMessage());
           }
           catch(InUseException e)
           {
             logger.error("InUseException thrown: "+e.getMessage());
           }
           catch(IdUsedException e)
           {
             logger.error("IdUsedException thrown");
           }
           catch(OverQuotaException e)
           {
             logger.error("Copying this collection would place this account over quota.");
            }
            catch(ServerOverloadException e)
            {
              logger.error("Server overload exception");
            }
          }
 		 catch (Exception e)
 	     {
 	        e.printStackTrace();
 	      }
 	     finally
 	     {
 	       meleteSecurityService.popAdvisor();
 	     }
 
 		}
 
 
 	  public ContentCollection getCollection(String toColl)
 	  {
 		ContentCollection toCollection = null;
 		try
 	    {
         if (!isUserAuthor())
         {
         		logger.info("User is not authorized to perform the copyIntoFolder function");
         }
    			//          setup a security advisor
         meleteSecurityService.pushAdvisor();
 	    try
    	    {
    		  toCollection = getContentservice().getCollection(toColl);
    		}
    	    catch(IdUnusedException e1)
 	    {
    		  logger.error("IdUnusedException thrown: "+e1.getMessage());
 	    }
    	    catch(TypeException e1)
         {
           logger.error("TypeException thrown: "+e1.getMessage());
         }
    	    catch(PermissionException e1)
         {
           logger.error("Permission to get uploads collection is denied");
         }
 	    }
 		catch (Exception e)
 	    {
 	        e.printStackTrace();
 	    }
 	    finally
 	    {
 	       meleteSecurityService.popAdvisor();
 	    }
    	    return toCollection;
 	  }
 
 
 	  public void removeResource(String delRes_id) throws Exception
 	  {
 		  if (!isUserAuthor())
 	        {
 	        		logger.info("User is not authorized to perform del resource function");
 	        }
 	   			//          setup a security advisor
 	        meleteSecurityService.pushAdvisor();
 	        ContentResourceEdit edit = null;
 		    try
 	   	    {
 		    	edit = getContentservice().editResource(delRes_id);		    	
 		    	getContentservice().removeResource(edit);
 		    	edit = null;
 	   		}
 	   	    catch(IdUnusedException e1)
 		    {	   	    
 	   		  logger.error("IdUnusedException thrown: "+e1.getMessage() + delRes_id );
 		    }
 	   	    catch(TypeException e1)
 	        {
 	          logger.error("TypeException thrown: "+e1.getMessage());
 	        }
 	   	    catch(PermissionException e1)
 	        {
 	          logger.error("Permission to get uploads collection is denied");
 	        }	   	  
 	   	    catch (Exception e)
 		    {
 	   	    	e.printStackTrace();
 		        throw new MeleteException("delete_resource_fail");
 		    }
 		    finally
 		    {
 		    	if(edit != null)getContentservice().cancelResource(edit);
 		       meleteSecurityService.popAdvisor();
 		    }
 	  }
 
 	  public void removeCollection(String delColl_id, String delSubColl_id) throws Exception
 	  {
 		  if (!isUserAuthor())
 	        {
 	        		logger.info("User is not authorized to perform del resource function");
 	        }
 	   			//          setup a security advisor
 	        meleteSecurityService.pushAdvisor();
 	        delColl_id= Entity.SEPARATOR+"private"+ REFERENCE_ROOT+ Entity.SEPARATOR+delColl_id+ Entity.SEPARATOR;
 	    	if(delSubColl_id != null)
 	    		delColl_id = delColl_id.concat(delSubColl_id + Entity.SEPARATOR);
 	    	logger.debug("checking coll before delte" + delColl_id);	    	
 	    	
 	    	try
 	    	{
 	    		getContentservice().checkCollection(delColl_id);	    		
 	    		getContentservice().removeCollection(delColl_id);	    	
 	    	}
 	    	catch (IdUnusedException e1)
 	    	{
 	    		logger.error("IdUnusedException thrown: "+e1.getMessage());
 	    	}
 	    	catch(TypeException e1)
 	        {
 	          logger.error("TypeException thrown: "+e1.getMessage());
 	        }
 	   	    catch(PermissionException e1)
 	        {
 	          logger.error("Permission to get uploads collection is denied");
 	        }	  
 	   	    catch (Exception e)
 		    {
 	   	    	logger.error("error deleting melete collection:" + e.getMessage());
 		        throw new MeleteException("delete_resource_fail");
 		    }
 		    finally
 		    {		  
 		       meleteSecurityService.popAdvisor();
 		    }
 	  }
 
 	  
 	  public void removeCourseCollection(String delColl_id) throws Exception
 	  {
 		  if (!isUserAuthor())
 	        {
 	        		logger.info("User is not authorized to perform del resource function");
 	        }
 	   			//          setup a security advisor
 	        meleteSecurityService.pushAdvisor();
 		    try
 	   	    {
 		    	delColl_id= Entity.SEPARATOR+"private"+ REFERENCE_ROOT+ Entity.SEPARATOR+delColl_id+ Entity.SEPARATOR;
 		    	logger.debug("checking course coll before delete: " + delColl_id);
 		    	getContentservice().checkCollection(delColl_id);
 		    	
 		    	// if uploads directory remains and all modules are deleted
 		    	logger.debug("collection sz"+ getContentservice().getCollectionSize(delColl_id));
 		    	if(getContentservice().getCollectionSize(delColl_id) == 1)
 		    	{	
 		    		List allEnt = getContentservice().getAllEntities(delColl_id);
 		    		
 		    		for(Iterator i = allEnt.iterator(); i.hasNext();)
 		    		{
 		    			ContentEntity ce = (ContentEntity)i.next();
 		    			if(ce.isCollection() && (ce.getId().indexOf("module_") == -1))
 		    				getContentservice().removeCollection(delColl_id);
 		    		}
 		    		
 		    	}
 	   		}
 	   	    catch(IdUnusedException e1)
 		    {
 	   		  logger.error("IdUnusedException thrown from remove Course Collection: "+e1.getMessage());
 		    }
 	   	    catch(TypeException e1)
 	        {
 	          logger.error("TypeException thrown: "+e1.getMessage());
 	        }
 	   	    catch(PermissionException e1)
 	        {
 	          logger.error("Permission to get uploads collection is denied");
 	        }
 	   	    catch (Exception e)
 		    {
 		        throw new MeleteException("delete_resource_fail");
 		    }
 		    finally
 		    {
 		       meleteSecurityService.popAdvisor();
 		    }
 	  }
 	  
 	  public String moveResource(String resourceId, String destinationColl) throws Exception
 	  {
 		  if (!isUserAuthor())
 	        {
 	         logger.info("User is not authorized to perform del resource function");
 	        }
 	   			//          setup a security advisor
 	        meleteSecurityService.pushAdvisor();
 		    try
 	   	    {		    	
 		    	getContentservice().checkResource(resourceId);
 		    	String newResId = getContentservice().moveIntoFolder(resourceId,destinationColl);
 		    	return newResId;
 	   	    }
 	   	    catch(IdUnusedException e1)
 		    {
 	   		  logger.error("IdUnusedException thrown from moveResource: "+e1.getMessage());
 		    }	   	   
 	   	    catch (Exception e)
 		    {
 		        throw new MeleteException("move_resource_fail");
 		    }
 		    finally
 		    {
 		       meleteSecurityService.popAdvisor();
 		    }
 		    return null;
 	  }
 	  
 	  
 	    /**
 	     * @return Returns the logger.
 	     */
 	    public void setLogger(Log logger) {
 	            this.logger = logger;
 	    }
 
 	    /**
 	     * @return Returns the contentservice.
 	     */
 	    public ContentHostingService getContentservice() {
 	            return contentservice;
 	    }
 	    /**
 	     * @param contentservice The contentservice to set.
 	     */
 	    public void setContentservice(ContentHostingService contentservice) {
 	            this.contentservice = contentservice;
 	    }
 
 	    /**
 		 * @return meleteSecurityService
 		 */
 		public MeleteSecurityService getMeleteSecurityService() {
 			return meleteSecurityService;
 		}
 
 
 	    /**
 		 * @param meleteSecurityService The meleteSecurityService to set.
 		 */
 		public void setMeleteSecurityService(MeleteSecurityService meleteSecurityService) {
 			this.meleteSecurityService = meleteSecurityService;
 		}
 
 	    /**
 		 * @return Returns the sectiondb.
 		 */
 		public SectionDB getSectiondb() {
 			return sectiondb;
 		}
 		/**
 		 * @param sectiondb The sectiondb to set.
 		 */
 		public void setSectiondb(SectionDB sectiondb) {
 			this.sectiondb = sectiondb;
 		}
 }
