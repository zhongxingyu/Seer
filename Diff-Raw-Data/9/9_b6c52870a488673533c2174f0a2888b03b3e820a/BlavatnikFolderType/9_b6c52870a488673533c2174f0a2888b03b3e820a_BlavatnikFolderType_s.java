 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2006, 2007, 2008 Sakai Foundation
  *
  * Licensed under the Educational Community License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.osedu.org/licenses/ECL-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.content.types;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.sakaiproject.authz.api.PermissionsHelper;
 import org.sakaiproject.component.cover.ComponentManager;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.content.api.ContentCollection;
 import org.sakaiproject.content.api.ContentCollectionEdit;
 import org.sakaiproject.content.api.ContentEntity;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ExpandableResourceType;
 import org.sakaiproject.content.api.InteractionAction;
 import org.sakaiproject.content.api.ResourceToolAction;
 import org.sakaiproject.content.api.ResourceToolAction.ActionType;
 import org.sakaiproject.content.api.ResourceType;
 import org.sakaiproject.content.api.ServiceLevelAction;
 import org.sakaiproject.content.util.BaseInteractionAction;
 import org.sakaiproject.content.util.BaseResourceType;
 import org.sakaiproject.content.util.ZipContentUtil;
 import org.sakaiproject.entity.api.Reference;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 import org.sakaiproject.entity.cover.EntityManager;
 import org.sakaiproject.exception.IdInvalidException;
 import org.sakaiproject.exception.IdLengthException;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.IdUsedException;
 import org.sakaiproject.exception.InUseException;
 import org.sakaiproject.exception.InconsistentException;
 import org.sakaiproject.exception.OverQuotaException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.exception.ServerOverloadException;
 import org.sakaiproject.exception.TypeException;
 import org.sakaiproject.site.cover.SiteService;
 import org.sakaiproject.tool.api.ToolSession;
 import org.sakaiproject.tool.cover.SessionManager;
 import org.sakaiproject.user.api.UserDirectoryService;
 import org.sakaiproject.util.Resource;
 import org.sakaiproject.util.ResourceLoader;
 import org.sakaiproject.util.Validator;
 
 import uk.ac.ox.oucs.termdates.TermConverterService;
 
 
 public class BlavatnikFolderType extends BaseResourceType implements ExpandableResourceType 
 {
 	protected String typeId = "org.sakaiproject.content.types.blavatnikFolder";
 	protected String helperId = "sakai.resource.type.helper";
 	private static final String RESOURCES_ZIP_ENABLE = "resources.zip.enable"; //sakai.properties hack
 	
 	/** localized tool properties **/
 	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.TypeProperties";
 	private static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.type.types";
 	private static final String RESOURCECLASS = "resource.class.type";
 	private static final String RESOURCEBUNDLE = "resource.bundle.type";
 	
 	private static final String PROP_BSG_START_WEEK = "bsg_start_week";
 	private static final String PROP_BSG_END_WEEK = "bsg_end_week";
 	
 	private static final String PROP_BSG_WEEK = "BSG:weekofyear";
 	
 	private String resourceClass = ServerConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
 	private String resourceBundle = ServerConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
 	private ResourceLoader rb = new Resource().getLoader(resourceClass, resourceBundle);
 	// private static ResourceLoader rb = new ResourceLoader("types");
 	
 	protected EnumMap<ResourceToolAction.ActionType, List<ResourceToolAction>> actionMap = new EnumMap<ResourceToolAction.ActionType, List<ResourceToolAction>>(ResourceToolAction.ActionType.class);
 
 	protected Map<String, ResourceToolAction> actions = new HashMap<String, ResourceToolAction>();	
 	protected UserDirectoryService userDirectoryService;
 	protected ContentHostingService contentService;
 	
 	public BlavatnikFolderType() {
 		this.userDirectoryService = (UserDirectoryService) ComponentManager.get("org.sakaiproject.user.api.UserDirectoryService");
 		this.contentService = (ContentHostingService) ComponentManager.get(ContentHostingService.class);
 		
 		//actions.put(ResourceToolAction.PASTE_MOVED, new BlavatnikPasteMovedAction());
 		//actions.put(ResourceToolAction.PASTE_COPIED, new BlavatnikPasteCopiedAction());
 		actions.put(ResourceToolAction.CREATE, new BlavatnikCreateAction());
 		actions.put(ResourceToolAction.ACCESS_PROPERTIES, new BlavatnikViewPropertiesAction());
 		actions.put(ResourceToolAction.REVISE_METADATA, new BlavatnikPropertiesAction());
 		//actions.put(ResourceToolAction.DUPLICATE, new BlavatnikDuplicateAction());
 		actions.put(ResourceToolAction.PASTE_COPIED, new BlavatnikPasteCopyAction());
 		actions.put(ResourceToolAction.PASTE_MOVED, new BlavatnikPasteMoveAction());
 		actions.put(ResourceToolAction.COPY, new BlavatnikCopyAction());
 		actions.put(ResourceToolAction.MOVE, new BlavatnikMoveAction());
 		actions.put(ResourceToolAction.DELETE, new BlavatnikDeleteAction());
 		actions.put(ResourceToolAction.REORDER, new BlavatnikReorderAction());
 		actions.put(ResourceToolAction.PERMISSIONS, new BlavatnikPermissionsAction());
 		actions.put(ResourceToolAction.EXPAND, new BlavatnikExpandAction());
 		actions.put(ResourceToolAction.RESTORE, new BlavatnikRestoreAction());
 		actions.put(ResourceToolAction.COLLAPSE, new BlavatnikCollapseAction());
 		actions.put(ResourceToolAction.MAKE_SITE_PAGE, new MakeSitePageAction(typeId));
 
 		
 		// [WARN] Archive file handling compress/decompress feature contains bugs; exclude action item.
 		// Disable property setting masking problematic code per will of the Community.
 		// See Jira KNL-155/SAK-800 for more details.
 		if (ServerConfigurationService.getBoolean(RESOURCES_ZIP_ENABLE,false)) {
 			actions.put(ResourceToolAction.COMPRESS_ZIP_FOLDER, new BlavatnikCompressAction());
 		}
 		
 		// initialize actionMap with an empty List for each ActionType
 		for(ResourceToolAction.ActionType type : ResourceToolAction.ActionType.values())
 		{
 			actionMap.put(type, new ArrayList<ResourceToolAction>());
 		}
 		
 		// for each action in actions, add a link in actionMap
 		Iterator<String> it = actions.keySet().iterator();
 		while(it.hasNext())
 		{
 			String id = it.next();
 			ResourceToolAction action = actions.get(id);
 			List<ResourceToolAction> list = actionMap.get(action.getActionType());
 			if(list == null)
 			{
 				list = new ArrayList<ResourceToolAction>();
 				actionMap.put(action.getActionType(), list);
 			}
 			list.add(action);
 		}
 	}
 	
 	public class BlavatnikPasteCopyAction implements ServiceLevelAction
 	{
 
 		public void cancelAction(Reference reference)
         {
 	        // no activity required
         }
 
 		public void finalizeAction(Reference reference)
         {
 	        // no activity required
         }
 
 		public void initializeAction(Reference reference)
         {
 	        // no activity required
         }
 
 		public boolean isMultipleItemAction()
         {
 	        return false;
         }
 
 		public boolean available(ContentEntity entity)
         {
 	        return true;
         }
 
 		public ActionType getActionType()
         {
 	        return ActionType.PASTE_COPIED;
         }
 
 		public String getId()
         {
 	        return ResourceToolAction.PASTE_COPIED;
         }
 
 		public String getLabel()
         {
 	        return rb.getString("action.pastecopy");
         }
 
 		public String getTypeId()
         {
 	        return typeId;
         }
 		
 	}
 
 	public class BlavatnikPasteMoveAction implements ServiceLevelAction
 	{
 
 		public void cancelAction(Reference reference)
         {
 	        // no activity required
         }
 
 		public void finalizeAction(Reference reference)
         {
 	        // no activity required
         }
 
 		public void initializeAction(Reference reference)
         {
 	        // no activity required
         }
 
 		public boolean isMultipleItemAction()
         {
 	        return false;
         }
 
 		public boolean available(ContentEntity entity)
         {
 	        return true;
         }
 
 		public ActionType getActionType()
         {
 	        return ActionType.PASTE_MOVED;
         }
 
 		public String getId()
         {
 	        return ResourceToolAction.PASTE_MOVED;
         }
 
 		public String getLabel()
         {
 	        return rb.getString("action.pastemove");
         }
 
 		public String getTypeId()
         {
 	        return typeId;
         }
 		
 	}
 
 	public class BlavatnikPermissionsAction implements InteractionAction
 	{
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.InteractionAction#cancelAction(org.sakaiproject.entity.api.Reference, java.lang.String)
          */
         public void cancelAction(Reference reference, String initializationId)
         {
 	        // nothing to do
 	        
         }
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.InteractionAction#finalizeAction(org.sakaiproject.entity.api.Reference, java.lang.String)
          */
         public void finalizeAction(Reference reference, String initializationId)
         {
 	        // nothing to do
 	        
         }
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.InteractionAction#getHelperId()
          */
         public String getHelperId()
         {
 	        return "sakai.permissions.helper";
         }
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.InteractionAction#getRequiredPropertyKeys()
          */
         public List getRequiredPropertyKeys()
         {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.InteractionAction#initializeAction(org.sakaiproject.entity.api.Reference)
          */
         public String initializeAction(Reference reference)
         {
     		ToolSession toolSession = SessionManager.getCurrentToolSession();
 
     		toolSession.setAttribute(PermissionsHelper.TARGET_REF, reference.getReference());
 
     		// use the folder's context (as a site) for roles
     		String siteRef = SiteService.siteReference(reference.getContext());
     		toolSession.setAttribute(PermissionsHelper.ROLES_REF, siteRef);
 
     		// ... with this description
     		String title = reference.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
     		String[] args = { title };
  
     		toolSession.setAttribute(PermissionsHelper.DESCRIPTION, rb.getFormattedMessage("title.permissions", args));
 
     		// ... showing only locks that are prpefixed with this
     		toolSession.setAttribute(PermissionsHelper.PREFIX, "content.");
 
  	        return BaseInteractionAction.getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
         }
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
          */
         public boolean available(ContentEntity entity)
         {
           	boolean ok = true;
         	if(entity == null || ContentHostingService.ROOT_COLLECTIONS.contains(entity.getId()))
     		{
     			ok = false;
     		}
         	else if(entity.getId().startsWith(ContentHostingService.COLLECTION_DROPBOX))
         	{
         		ok = false;
         	}
         	else
     		{
     			ContentCollection parent = entity.getContainingCollection();
     			if(parent == null)
     			{
     				ok = false;
     			}
     		}
  	        return ok;
         }
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
          */
         public ActionType getActionType()
         {
 	        // TODO Auto-generated method stub
 	        return ActionType.REVISE_PERMISSIONS;
         }
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#getId()
          */
         public String getId()
         {
 	        // TODO Auto-generated method stub
 	        return ResourceToolAction.PERMISSIONS;
         }
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#getLabel()
          */
         public String getLabel()
         {
 	        // TODO Auto-generated method stub
 	        return rb.getString("action.permissions");
         }
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#getTypeId()
          */
         public String getTypeId()
         {
 	        // TODO Auto-generated method stub
 	        return typeId;
         }
 
 		
 	}
 
 	public class BlavatnikReorderAction implements ServiceLevelAction
 	{
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
          */
         public void cancelAction(Reference reference)
         {
 	        // nothing to do
 	        
         }
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
          */
         public void finalizeAction(Reference reference)
         {
 	        // nothing to do
 	        
         }
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
          */
         public void initializeAction(Reference reference)
         {
 	        // nothing to do
 	        
         }
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ServiceLevelAction#isMultipleItemAction()
          */
         public boolean isMultipleItemAction()
         {
 	        return false;
         }
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
          */
         public boolean available(ContentEntity entity)
         {
         	boolean isAvailable = true;
         	
         	if(entity != null && entity instanceof ContentCollection)
         	{
         		isAvailable = ((ContentCollection) entity).getMemberCount() > 1;
         	}
 	        return isAvailable;
         }
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
          */
         public ActionType getActionType()
         {
 	        return ActionType.REVISE_ORDER;
         }
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#getId()
          */
         public String getId()
         {
 	        return ResourceToolAction.REORDER;
         }
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#getLabel()
          */
         public String getLabel()
         {
 	        return rb.getString("action.reorder");
         }
 
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#getTypeId()
          */
         public String getTypeId()
         {
 	        return typeId;
         }
 		
 	}
 
 	public class BlavatnikPasteMovedAction implements ServiceLevelAction
 	{
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
          */
         public boolean available(ContentEntity entity)
         {
 	        return true;
         }
         
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
 		 */
 		public ActionType getActionType()
 		{
 			return ResourceToolAction.ActionType.PASTE_MOVED;
 		}
 
 
 		public String getId() 
 		{
 			return ResourceToolAction.PASTE_MOVED;
 		}
 
 		public String getLabel() 
 		{
 			return rb.getString("action.paste");
 		}
 
 		public boolean isMultipleItemAction() 
 		{
 			return false;
 		}
 
 		public String getTypeId() 
 		{
 			return typeId;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void cancelAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void finalizeAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void initializeAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 	}
 
 	public class BlavatnikPasteCopiedAction implements ServiceLevelAction
 	{
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
          */
         public boolean available(ContentEntity entity)
         {
 	        return true;
         }
         
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
 		 */
 		public ActionType getActionType()
 		{
 			return ResourceToolAction.ActionType.PASTE_COPIED;
 		}
 
 
 		public String getId() 
 		{
 			return ResourceToolAction.PASTE_COPIED;
 		}
 
 		public String getLabel() 
 		{
 			return rb.getString("action.paste");
 		}
 
 		public boolean isMultipleItemAction() 
 		{
 			return false;
 		}
 
 		public String getTypeId() 
 		{
 			return typeId;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void cancelAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void finalizeAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void initializeAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 	}
 
 	public class BlavatnikCopyAction implements ServiceLevelAction
 	{
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
          */
         public boolean available(ContentEntity entity)
         {
         	boolean ok = true;
         	if(entity == null || ContentHostingService.ROOT_COLLECTIONS.contains(entity.getId()))
     		{
     			ok = false;
     		}
     		else
     		{
     			ContentCollection parent = entity.getContainingCollection();
     			if(parent == null)
     			{
     				ok = false;
     			}
     		}
  	        return ok;
         }
 		
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
 		 */
 		public ActionType getActionType()
 		{
 			return ResourceToolAction.ActionType.COPY;
 		}
 
 
 		public String getId() 
 		{
 			return ResourceToolAction.COPY;
 		}
 
 		public String getLabel() 
 		{
 			return rb.getString("action.copy");
 		}
 
 		public boolean isMultipleItemAction() 
 		{
 			return true;
 		}
 
 		public String getTypeId() 
 		{
 			return typeId;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void cancelAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void finalizeAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void initializeAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 	}
 
 	public class BlavatnikCreateAction implements InteractionAction
 	{
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
          */
         public boolean available(ContentEntity entity)
         {
 	        return true;
         }
 
 		public void cancelAction(Reference reference, String initializationId) 
 		{
 			// do nothing
 			
 		}
 
 		public void finalizeAction(Reference reference, String initializationId) { 
 			
 			TermConverterService termConverterService = 
 					(TermConverterService) ComponentManager.get("uk.ac.ox.oucs.termdates.BsgTermConverterService");
 			
 			ContentCollection entity = (ContentCollection)reference.getEntity();
 			ResourceProperties properties = entity.getProperties();
 			int startWeek = parseInt(properties.getProperty(PROP_BSG_START_WEEK));
 			int endWeek = parseInt(properties.getProperty(PROP_BSG_END_WEEK));
 			
 			if (contentService == null) {
 				contentService = (ContentHostingService)ComponentManager.get(ContentHostingService.class);
 			}
 			
 			try {
 				for (int i = startWeek; i <= endWeek; i++) {
 					String name = termConverterService.getWeekName(i);
 					//String name = "Week"+Integer.toString(i);
 					ContentCollectionEdit edit = contentService.addCollection(entity.getId(), Validator.escapeResourceName(name));
 					
 					ResourcePropertiesEdit resourceProperties = edit.getPropertiesEdit();
 					resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
 					resourceProperties.addProperty(PROP_BSG_WEEK, Integer.toString(i));
 				
 					contentService.commitCollection(edit);
 				}
 				
 			} catch (PermissionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IdUnusedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IdUsedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IdLengthException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IdInvalidException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (TypeException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
 		 */
 		public ActionType getActionType()
 		{
 			return ResourceToolAction.ActionType.NEW_FOLDER;
 		}
 
 		public String getId() 
 		{
 			return ResourceToolAction.CREATE;
 		}
 
 		public String getLabel() 
 		{
 			return rb.getString("create.blavatnik"); 
 		}
 
 		public String getTypeId() 
 		{
 			return typeId;
 		}
 
 		public String getHelperId() 
 		{
 			return helperId;
 		}
 
 		public List getRequiredPropertyKeys() 
 		{
 			return null;
 		}
 
 		public String initializeAction(Reference reference) 
 		{
 			return BaseInteractionAction.getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
 		}
 		
 		private int parseInt(String s) {
 			try {
 				return Integer.parseInt(s);
 			} catch (NumberFormatException e) {
 				return 0;
 			}
 		}
 
 	}
 
 	public class BlavatnikDeleteAction implements ServiceLevelAction
 	{
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
          */
         public boolean available(ContentEntity entity)
         {
         	boolean ok = true;
         	if(entity == null || ContentHostingService.ROOT_COLLECTIONS.contains(entity.getId()))
     		{
     			ok = false;
     		}
     		else
     		{
     			ContentCollection parent = entity.getContainingCollection();
     			if(parent == null)
     			{
     				ok = false;
     			}
     			else
     			{
     				ContentCollection grandparent = parent.getContainingCollection();
     				if(grandparent != null && ContentHostingService.COLLECTION_DROPBOX.equals(grandparent.getId()))
     				{
     					Reference ref = EntityManager.newReference(entity.getReference());
     					if(ref != null)
     					{
 	    					String siteId = ref.getContext();
 	    					if(siteId != null)
 	    					{
 	        					if(contentService == null)
 	        					{
 	        						contentService = (ContentHostingService) ComponentManager.get(ContentHostingService.class);
 	        					}
 	    						String dropboxId = contentService.getDropboxCollection(siteId);
 	    						if(entity.getId().equals(dropboxId))
 	    						{
 	    							ok = false;
 	    						}
 	    					}
     					}
     				}
     			}
     		}
 // remove member count as condition for deletion - SAK-11790        	
 //        	if(ok && entity instanceof ContentCollection)
 //        	{
 //        		ok = (((ContentCollection) entity).getMemberCount() == 0);
 //        	}
 	        return ok;
         }
         
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
 		 */
 		public ActionType getActionType()
 		{
 			return ResourceToolAction.ActionType.DELETE;
 		}
 
 		public String getId() 
 		{
 			return ResourceToolAction.DELETE;
 		}
 
 		public String getLabel() 
 		{
 			return rb.getString("action.delete"); 
 		}
 
 		public boolean isMultipleItemAction() 
 		{
 			return true;
 		}
 		
 		public String getTypeId() 
 		{
 			return typeId;
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void cancelAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void finalizeAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void initializeAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 	}
 
 	public class BlavatnikDuplicateAction implements ServiceLevelAction
 	{
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
          */
         public boolean available(ContentEntity entity)
         {
         	boolean ok = true;
         	if(entity == null || ContentHostingService.ROOT_COLLECTIONS.contains(entity.getId()))
     		{
     			ok = false;
     		}
     		else
     		{
     			ContentCollection parent = entity.getContainingCollection();
     			if(parent == null)
     			{
     				ok = false;
     			}
     		}
  	        return ok;
         }
         
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
 		 */
 		public ActionType getActionType()
 		{
 			return ResourceToolAction.ActionType.DUPLICATE;
 		}
 
 		public String getId() 
 		{
 			return ResourceToolAction.DUPLICATE;
 		}
 
 		public String getLabel() 
 		{
 			return rb.getString("action.duplicate"); 
 		}
 
 		public boolean isMultipleItemAction() 
 		{
 			return false;
 		}
 		
 		public String getTypeId() 
 		{
 			return typeId;
 		}
 		
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void cancelAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void finalizeAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void initializeAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 	}
 	
 	public class BlavatnikPropertiesAction implements ServiceLevelAction
 	{
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
          */
         public boolean available(ContentEntity entity)
         {
            	boolean ok = true;
 //        	if(entity == null || ContentHostingService.ROOT_COLLECTIONS.contains(entity.getId()))
 //    		{
 //    			ok = false;
 //    		}
 //        	else if(entity.getId().startsWith(ContentHostingService.COLLECTION_DROPBOX))
 //        	{
 //        		ok = false;
 //        	}
 //        	else
 //    		{
 //    			ContentCollection parent = entity.getContainingCollection();
 //    			if(parent == null || ContentHostingService.ROOT_COLLECTIONS.contains(parent.getId()))
 //    			{
 //    				ok = false;
 //    			}
 //    		}
  	        return ok;
         }
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void cancelAction(Reference reference)
 		{
 			// do nothing	
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void finalizeAction(Reference reference)	{
 
 			TermConverterService termConverterService = 
 					(TermConverterService) ComponentManager.get("uk.ac.ox.oucs.termdates.BsgTermConverterService");
 			
 			ContentCollection entity = (ContentCollection)reference.getEntity();
 			
 			SortedMap<Integer, String> oldMap = new TreeMap<Integer, String>();
 			SortedSet<Integer> newSet = new TreeSet<Integer>();
 			
 			ResourceProperties properties = entity.getProperties();
 			int startWeek = parseInt(properties.getProperty(PROP_BSG_START_WEEK));
 			int endWeek = parseInt(properties.getProperty(PROP_BSG_END_WEEK));
 			
 			//existing members
 			Collection<ContentEntity> collection = entity.getMemberResources();
 			for (ContentEntity contentEntity : collection) {
 				if (contentEntity instanceof ContentCollection) {
 					ContentCollection content = (ContentCollection)contentEntity;
 					String weekName = content.getProperties().getProperty(PROP_BSG_WEEK);
 					if (null != weekName) {
 						oldMap.put(new Integer(weekName), content.getId());
 					}
 				}
 			}
 			
 			//new members
 			for (int i = startWeek; i <= endWeek; i++) {
 				newSet.add(i);
 			}
 				
 			Object[] oldArray = oldMap.keySet().toArray();
 			Object[] newArray = newSet.toArray();
 				
 			//Do something with the member resources
 			try {
 				// stashMap used as a temporary stash for id clashes
 				Map<String, String> stashMap = new HashMap<String, String>();
 				// remember the last id so that unallocated resources can be put into it
 				String lastId = null;
 				
 				for (int i=0; i<newArray.length; i++) {	
 					
 					Integer newInteger = ((Integer)newArray[i]);
 					String name = termConverterService.getWeekName(newInteger);
 					ContentCollection from = null;
 					ContentCollectionEdit edit = null;
 					
 					if (i<oldArray.length) {
 						
 						Integer oldInteger = ((Integer)oldArray[i]);
 						if (newInteger.intValue() == oldInteger.intValue()) {
 							lastId = oldMap.get(oldInteger);
 							oldMap.remove(oldInteger);
 							continue;
 						}
 						
 						String id = null;
 						String key = oldMap.get(oldInteger);
 						if (stashMap.containsKey(key)) {
 							id = stashMap.get(key);
 						} else {
 							id = key;
 						}
 						
 						from = contentService.getCollection(id);			
 						oldMap.remove(oldInteger);
 					} 
 					
 					try {
 						edit = contentService.addCollection(
 								entity.getId(), Validator.escapeResourceName(name));
 					
 					} catch (IdUsedException e) {
 						// there is a clash of id's, 
 						// stash the old id so that we can create the new one
 						String stashId = entity.getId() + Validator.escapeResourceName(name) + "/";
 						String tempId = entity.getId() + Validator.escapeResourceName(
 								new Long(System.currentTimeMillis()).toString()) + "/";
 						
 						ContentCollectionEdit temp = contentService.addCollection(tempId);
 						ContentCollection stash = contentService.getCollection(stashId);
 						
 						ResourcePropertiesEdit resourcePropertiesEdit = temp.getPropertiesEdit();
 						resourcePropertiesEdit.addAll(stash.getProperties());
 						
 						Collection<ContentEntity> resources = stash.getMemberResources();
 						for (ContentEntity contentEntity : resources) {
 							contentService.moveIntoFolder(contentEntity.getId(), temp.getId());
 						}
 						
 						stashMap.put(stashId, tempId);
 						contentService.commitCollection(temp);
 						contentService.removeCollection(stash.getId());
 						
 						// make sure the stashId isn't in the oldMap
 						if (oldMap.containsValue(stashId)) {
 							for (Map.Entry<Integer, String> entry : oldMap.entrySet()) {
 								if (stashId.equals(entry.getValue())) {
									oldMap.remove(entry.getKey());
 								}
 							}
 						}
 						
 						// set up the new folder
 						edit = contentService.addCollection(stashId);
 					}
 					
 					ResourcePropertiesEdit resourceProperties = edit.getPropertiesEdit();
 					resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
 					resourceProperties.addProperty(PROP_BSG_WEEK, Integer.toString((Integer)newArray[i]));
 					
 					if (null != from) {
 						Collection<ContentEntity> resources = from.getMemberResources();
 						for (ContentEntity contentEntity : resources) {
 							contentService.moveIntoFolder(contentEntity.getId(), edit.getId());
 						}
 						// remove old entity
 						contentService.removeCollection(from.getId());
 					}
 					
 					contentService.commitCollection(edit);
 					lastId = edit.getId();
 				}	
 				
 				// Tidy up extra ContentCollections
 				for (Map.Entry<Integer, String> entry : oldMap.entrySet()) {
 					copyResources(entry.getValue(), lastId);
 				}
 				
 				// Tidy up stashed ContentCollections
 				for (Map.Entry<String, String> entry : stashMap.entrySet()) {
 					copyResources(entry.getValue(), lastId);
 				}
 				
 			} catch (IdUnusedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (TypeException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (PermissionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IdUsedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IdLengthException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IdInvalidException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InUseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ServerOverloadException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (OverQuotaException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InconsistentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 				
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void initializeAction(Reference reference)
 		{
 			// do nothing
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#isMultipleItemAction()
 		 */
 		public boolean isMultipleItemAction()
 		{
 			return false;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
 		 */
 		public ActionType getActionType()
 		{
 			return ResourceToolAction.ActionType.REVISE_METADATA;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ResourceToolAction#getId()
 		 */
 		public String getId()
 		{
 			return ResourceToolAction.REVISE_METADATA;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ResourceToolAction#getLabel()
 		 */
 		public String getLabel()
 		{
 			return rb.getString("action.props");
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ResourceToolAction#getTypeId()
 		 */
 		public String getTypeId()
 		{
 			return typeId;
 		}
 		
 		private int parseInt(String s) {
 			try {
 				return Integer.parseInt(s);
 			} catch (NumberFormatException e) {
 				return 0;
 			}
 		}
 		
 		/**
 		 * Copy the resources from one ContentCollection to another
 		 * 
 		 * @param fromId
 		 * @param toId
 		 * @throws IdUnusedException
 		 * @throws TypeException
 		 * @throws PermissionException
 		 * @throws InUseException
 		 * @throws OverQuotaException
 		 * @throws IdUsedException
 		 * @throws InconsistentException
 		 * @throws ServerOverloadException
 		 */
 		private void copyResources(String fromId, String toId) 
 				throws TypeException, PermissionException, 
 						InUseException, OverQuotaException, IdUsedException, 
 						InconsistentException, ServerOverloadException {
 			
 			try {
 				ContentCollection fromCollection = contentService.getCollection(fromId);
 				Collection<ContentEntity> resources = fromCollection.getMemberResources();
 			
 				if (!resources.isEmpty()) {
 					ContentCollectionEdit toCollection = contentService.editCollection(toId);
 					for (ContentEntity contentEntity : resources) {
 						contentService.moveIntoFolder(contentEntity.getId(), toCollection.getId());
 					}
 					contentService.commitCollection(toCollection);
 				}
 				contentService.removeCollection(fromCollection.getId());
 				
 			} catch (IdUnusedException e) {
 				// do nothing
 			}
 		}
 		
 	}
 
 	
 	public class BlavatnikRestoreAction implements ServiceLevelAction {
 	    
 	    public void cancelAction(Reference reference) {
 	        // TODO Auto-generated method stub
 	    }
 	    
 	    public void finalizeAction(Reference reference) {
 	        // TODO Auto-generated method stub        
 	    }
 	    
 	    public void initializeAction(Reference reference) {
 	    }
 	    
 	    public boolean isMultipleItemAction() {
 	        return false;
 	    }
 	    
 	    public boolean available(ContentEntity entity) {
 	        return !contentService.getAllDeletedResources(entity.getId()).isEmpty();
 	    }
 	    
 	    public ActionType getActionType() {
 	        return ResourceToolAction.ActionType.RESTORE;
 	    }
 	    
 	    public String getId() {
 	        return ResourceToolAction.RESTORE;
 	    }
 	    
 	    public String getLabel() {
 	        return rb.getString("action.restore"); 
 	    }
 	    
 	    public String getTypeId() {
 	        return typeId;
 	    }
 	}
 	
 	public class BlavatnikViewPropertiesAction implements ServiceLevelAction
 	{
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
          */
         public boolean available(ContentEntity entity)
         {
  	        return true;
         }
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void cancelAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void finalizeAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void initializeAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#isMultipleItemAction()
 		 */
 		public boolean isMultipleItemAction()
 		{
 			return false;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
 		 */
 		public ActionType getActionType()
 		{
 			return ResourceToolAction.ActionType.VIEW_METADATA;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ResourceToolAction#getId()
 		 */
 		public String getId()
 		{
 			return ResourceToolAction.ACCESS_PROPERTIES;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ResourceToolAction#getLabel()
 		 */
 		public String getLabel()
 		{
 			return rb.getString("action.access");
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ResourceToolAction#getTypeId()
 		 */
 		public String getTypeId()
 		{
 			return typeId;
 		}
 		
 	}
 
 	public class BlavatnikMoveAction implements ServiceLevelAction
 	{
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
          */
         public boolean available(ContentEntity entity)
         {
         	boolean ok = true;
         	if(entity == null || ContentHostingService.ROOT_COLLECTIONS.contains(entity.getId()))
     		{
     			ok = false;
     		}
         	else if(entity.getId().startsWith(ContentHostingService.COLLECTION_DROPBOX))
         	{
         		ok = false;
         	}
     		else
     		{
     			ContentCollection parent = entity.getContainingCollection();
     			if(parent == null)
     			{
     				ok = false;
     			}
     		}
  	        return ok;
         }
 		
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
 		 */
 		public ActionType getActionType()
 		{
 			return ResourceToolAction.ActionType.MOVE;
 		}
 
 		public String getId() 
 		{
 			return ResourceToolAction.MOVE;
 		}
 
 		public String getLabel() 
 		{
 			return rb.getString("action.move"); 
 		}
 
 		public boolean isMultipleItemAction() 
 		{
 			return true;
 		}
 		
 		public String getTypeId() 
 		{
 			return typeId;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void cancelAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void finalizeAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
 		 */
 		public void initializeAction(Reference reference)
 		{
 			// do nothing
 			
 		}
 
 	}
 
 	public class BlavatnikReviseAction implements InteractionAction
 	{
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
          */
         public boolean available(ContentEntity entity)
         {
 	        return true;
         }
 
 		public void cancelAction(Reference reference, String initializationId) 
 		{
 			// do nothing
 			
 		}
 
 		public void finalizeAction(Reference reference, String initializationId) 
 		{
 			System.out.println("BlavatnikReviseAction.finalizeAction");
 			
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
 		 */
 		public ActionType getActionType()
 		{
 			return ResourceToolAction.ActionType.REPLACE_CONTENT;
 		}
 
 		public String getId() 
 		{
 			return ResourceToolAction.REVISE_CONTENT;
 		}
 
 		public String getLabel() 
 		{
 			return rb.getString("action.revise"); 
 		}
 		
 		public String getTypeId() 
 		{
 			return typeId;
 		}
 
 		public String getHelperId() 
 		{
 			return helperId;
 		}
 
 		public List getRequiredPropertyKeys() 
 		{
 			return null;
 		}
 
 		public String initializeAction(Reference reference) 
 		{
 			return BaseInteractionAction.getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
 		}
 
 	}
 	
 	public class BlavatnikAccessAction implements InteractionAction
 	{
 		/* (non-Javadoc)
          * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
          */
         public boolean available(ContentEntity entity)
         {
 	        return true;
         }
 
 		public String initializeAction(Reference reference) 
 		{
 			return BaseInteractionAction.getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
 		}
 
 		public void cancelAction(Reference reference, String initializationId) 
 		{
 			// do nothing
 			
 		}
 
 		public void finalizeAction(Reference reference, String initializationId) 
 		{
 			// do nothing
 			
 		}
 
 		/* (non-Javadoc)
 		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
 		 */
 		public ActionType getActionType()
 		{
 			return ResourceToolAction.ActionType.VIEW_CONTENT;
 		}
 
 		public String getId() 
 		{
 			return ResourceToolAction.ACCESS_CONTENT;
 		}
 
 		public String getLabel() 
 		{
 			return rb.getString("action.access"); 
 		}
 		
 		public String getTypeId() 
 		{
 			return typeId;
 		}
 
 		public String getHelperId() 
 		{
 			return helperId;
 		}
 
 		public List getRequiredPropertyKeys() 
 		{
 			return null;
 		}
 
 	}
 	
 	public class BlavatnikExpandAction implements ServiceLevelAction
 	{
 
 		public void cancelAction(Reference reference)
         {
 	        
         }
 
 		public void finalizeAction(Reference reference)
         {
 	        
         }
 
 		public void initializeAction(Reference reference)
         {
 	        
         }
 
 		public boolean isMultipleItemAction()
         {
 	        return false;
         }
 
 		public boolean available(ContentEntity entity)
         {
 			boolean isAvailable = (entity != null);
 			if(isAvailable && entity instanceof ContentCollection)
 			{
 				ContentCollection collection = (ContentCollection) entity;
 				int memberCount = collection.getMemberCount();
 				isAvailable = (memberCount > 0) && (memberCount < ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT);
 			}
 	        return isAvailable;
         }
 
 		public ActionType getActionType()
         {
 	        return ActionType.EXPAND_FOLDER;
         }
 
 		public String getId()
         {
 	        return ResourceToolAction.EXPAND;
         }
 
 		public String getLabel()
         {
 	        return rb.getString("expand.item");
         }
 
 		public String getTypeId()
         {
 	        return typeId;
         }
 		
 	}
 	
 	public class BlavatnikCollapseAction implements ServiceLevelAction
 	{
 
 		public void cancelAction(Reference reference)
         {
 	        
         }
 
 		public void finalizeAction(Reference reference)
         {
 	        
         }
 
 		public void initializeAction(Reference reference)
         {
 	        
         }
 
 		public boolean isMultipleItemAction()
         {
 	        return false;
         }
 
 		public boolean available(ContentEntity entity)
         {
 	        return true;
         }
 
 		public ActionType getActionType()
         {
 	        return ActionType.COLLAPSE_FOLDER;
         }
 
 		public String getId()
         {
 	        return ResourceToolAction.COLLAPSE;
         }
 
 		public String getLabel()
         {
 	        return rb.getString("collapse.item");
         }
 
 		public String getTypeId()
         {
 	        return typeId;
         }
 		
 	}
 	
 	public class BlavatnikCompressAction implements ServiceLevelAction {
 				
 		private ZipContentUtil zipUtil = new ZipContentUtil();
 				
 		public void cancelAction(Reference reference) {
 			// TODO Auto-generated method stub
 		}
 		
 		public void finalizeAction(Reference reference) {
 			// TODO Auto-generated method stub		
 		}
 		
 		public void initializeAction(Reference reference) {
 			try {
 				zipUtil.compressFolder(reference);
 			} catch (Exception e) {
 				System.out.println(e.getMessage());
 			}			
 		}
 		
 		public boolean isMultipleItemAction() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 		
 		public boolean available(ContentEntity entity) {
 			return true;
 		}
 		
 		public ActionType getActionType() {
 			return ResourceToolAction.ActionType.COMPRESS_ZIP_FOLDER;
 		}
 		
 		public String getId() {
 			return ResourceToolAction.COMPRESS_ZIP_FOLDER;
 		}
 		
 		public String getLabel() {
 			return rb.getString("action.compresszipfolder"); 
 		}
 		
 		public String getTypeId() {
 			return typeId;
 		}
 	}
 	
 	public ResourceToolAction getAction(String actionId) 
 	{
 		return (ResourceToolAction) actions.get(actionId);
 	}
 
 	public String getIconLocation(ContentEntity entity, boolean expanded)
     {
 		String iconLocation = "sakai/dir_openroot.gif";
 		if(entity.isCollection())
 		{
 			ContentCollection collection = (ContentCollection) entity;
 			int memberCount = collection.getMemberCount();
 			if(memberCount == 0)
 			{
 				iconLocation = "sakai/dir_closed.gif";
 			}
 			else if(memberCount > ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT)
 			{
 				iconLocation = "sakai/dir_unexpand.gif";
 			}
 			else if(expanded) 
 			{
 				iconLocation = "sakai/dir_openminus.gif";
 			}
 			else 
 			{
 				iconLocation = "sakai/dir_closedplus.gif";
 			}
 		}
 		return iconLocation;
     }
 	
 	public String getIconLocation(ContentEntity entity) 
 	{
 		String iconLocation = "sakai/dir_openroot.gif";
 		if(entity != null && entity.isCollection())
 		{
 			ContentCollection collection = (ContentCollection) entity;
 			int memberCount = collection.getMemberCount();
 			if(memberCount == 0)
 			{
 				iconLocation = "sakai/dir_closed.gif";
 			}
 			else if(memberCount > ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT)
 			{
 				iconLocation = "sakai/dir_unexpand.gif";
 			}
 		}
 		return iconLocation;
 	}
 	
 	public String getId() 
 	{
 		return typeId;
 	}
 
 	public String getLabel() 
 	{
 		return rb.getString("type.blavatnik");
 	}
 	
 	public String getLocalizedHoverText(ContentEntity entity, boolean expanded)
     {
 		String hoverText = rb.getString("type.blavatnik");
 		if(entity.isCollection())
 		{
 			ContentCollection collection = (ContentCollection) entity;
 			int memberCount = collection.getMemberCount();
 			if(memberCount == 0)
 			{
 				hoverText = rb.getString("type.blavatnik");
 			}
 			else if(memberCount > ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT)
 			{
 				hoverText = rb.getString("list.toobig");
 			}
 			else if(expanded) 
 			{
 				hoverText = rb.getString("sh.close");
 			}
 			else 
 			{
 				hoverText = rb.getString("sh.open");
 			}
 		}
 		return hoverText;
     }
 	
 	/* (non-Javadoc)
 	 * @see org.sakaiproject.content.api.ResourceType#getLocalizedHoverText(org.sakaiproject.entity.api.Reference)
 	 */
 	public String getLocalizedHoverText(ContentEntity member)
 	{
 		return rb.getString("type.blavatnik");
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.sakaiproject.content.api.ResourceType#getActions(org.sakaiproject.content.api.ResourceType.ActionType)
 	 */
 	public List<ResourceToolAction> getActions(ActionType type)
 	{
 		List<ResourceToolAction> list = actionMap.get(type);
 		if(list == null)
 		{
 			list = new ArrayList<ResourceToolAction>();
 			actionMap.put(type, list);
 		}
 		return new ArrayList<ResourceToolAction>(list);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.sakaiproject.content.api.ResourceType#getActions(java.util.List)
 	 */
 	public List<ResourceToolAction> getActions(List<ActionType> types)
 	{
 		List<ResourceToolAction> list = new ArrayList<ResourceToolAction>();
 		if(types != null)
 		{
 			Iterator<ActionType> it = types.iterator();
 			while(it.hasNext())
 			{
 				ActionType type = it.next();
 				List<ResourceToolAction> sublist = actionMap.get(type);
 				if(sublist == null)
 				{
 					sublist = new ArrayList<ResourceToolAction>();
 					actionMap.put(type, sublist);
 				}
 				list.addAll(sublist);
 			}
 		}
 		return list;
 	}
 	
 	public boolean hasRightsDialog() 
 	{
 		return false;
 	}
 	
 	public ServiceLevelAction getCollapseAction()
     {
 	    return (ServiceLevelAction) this.actions.get(ResourceToolAction.COLLAPSE);
     }
 
 	public ServiceLevelAction getExpandAction()
     {
 	    return (ServiceLevelAction) this.actions.get(ResourceToolAction.EXPAND);
     }
 
 	public boolean allowAddAction(ResourceToolAction action, ContentEntity entity)
     {
 	    // allow all add actions in regular folders
 	    return true;
     }
 
 }
