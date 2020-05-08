 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
  * $Revision: 1.8 $
  * $Date: 2007-10-05 18:03:39 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.user;
 
 import org.concord.framework.otrunk.DefaultOTObject;
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTResourceMap;
 import org.concord.framework.otrunk.OTResourceSchema;
 import org.concord.otrunk.OTObjectServiceImpl;
 import org.concord.otrunk.datamodel.OTDataObject;
 import org.concord.otrunk.datamodel.OTDatabase;
 import org.concord.otrunk.overlay.Overlay;
 
 /**
  * OTTemplateStateMap
  * Class name and description
  *
  * Date created: Apr 22, 2005
  *
  * @author scott<p>
  *
  */
 public class OTReferenceMap extends DefaultOTObject
 	implements Overlay
 {
     private ResourceSchema resources; 
     public interface ResourceSchema extends OTResourceSchema
     {
         public OTUserObject getUser();
         public void setUser(OTUserObject user);
 
         public OTResourceMap getMap();        
     }
 
     private OTDatabase stateDb;
     
     public OTReferenceMap(ResourceSchema resources)
     {
         super(resources);
         this.resources = resources;
         
         
 		OTObjectServiceImpl objServiceImpl = 
 			(OTObjectServiceImpl)resources.getOTObjectService();
 
 		stateDb = objServiceImpl.getCreationDb();
     }
 
     public void setUser(OTUserObject user)
     {
         resources.setUser(user);
     }
     
     public OTUserObject getUser()
     {
         return resources.getUser();
     }
     
     public OTDataObject getStateObject(OTDataObject template, 
             OTDatabase stateDb)
     {
 		OTResourceMap stateMap = resources.getMap();
 		OTID userStateId = (OTID)stateMap.get(template.getGlobalId().toExternalForm());
 		// System.err.println("OTReferenceMap: requesting stateObject for: " +
 		//        template.getGlobalId());
 		if(userStateId == null) {
 		    return null;
 		}
 		
 		try {
 		    return stateDb.getOTDataObject(null, userStateId);
 		} catch (Exception e) {
 		    e.printStackTrace();
 		    return null;
 		}        
     }
     
     public OTDataObject createStateObject(OTDataObject template,
             OTDatabase stateDb)
     {
         try {
             OTDataObject stateObject = stateDb.createDataObject(template.getType());
             OTResourceMap stateMap = resources.getMap();
             stateMap.put(template.getGlobalId().toExternalForm(), 
                     stateObject.getGlobalId());
 			
             return stateObject;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
     }
 
 	public boolean contains(OTID id)
     {
 		return stateDb.contains(id);
     }
 
 	public OTDataObject getDeltaObject(OTDataObject baseObject)
     {
 		return getStateObject(baseObject, stateDb);
     }
 
 	public OTDataObject createDeltaObject(OTDataObject baseObject)
     {
 		return createStateObject(baseObject, stateDb);
     }
 
 	public OTDatabase getOverlayDatabase()
     {
 		return stateDb;
     }
 
 	/**
 	 * We don't need to do anything here because in this design there is a seperate database
 	 * for storing all the delta and non delta objects of the overlay.  This is the database
 	 * returned by the getOverlayDatabase call.  So the objects are "registered" when the are
 	 * added or created in the stateDb.
 	 */
 	public void registerNonDeltaObject(OTDataObject childObject)
     {
     }
 
 
 }
