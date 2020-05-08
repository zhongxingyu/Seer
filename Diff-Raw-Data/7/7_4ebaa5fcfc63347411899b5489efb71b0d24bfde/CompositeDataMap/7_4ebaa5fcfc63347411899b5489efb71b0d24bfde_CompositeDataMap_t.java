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
 * $Revision: 1.2 $
 * $Date: 2007-08-06 19:02:35 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.overlay;
 
 import org.concord.otrunk.datamodel.OTDataCollection;
 import org.concord.otrunk.datamodel.OTDataMap;
 
 
 final class CompositeDataMap extends CompositeDataCollection
 	implements OTDataMap
 {
     public CompositeDataMap(CompositeDataObject parent, 
     		OTDataMap authoredMap, String resourceName, boolean composite)
     {
     	super(OTDataMap.class, parent, authoredMap, resourceName, composite);
     }
     
 	private OTDataMap getUserMap()
 	{
 		return (OTDataMap)getCollectionForWrite();
 	}
 	
 	private OTDataMap getMapForRead()
 	{
 		return (OTDataMap)getCollectionForRead();
 	}
 	
 	protected void copyInto(OTDataCollection userCollection,
 			OTDataCollection authoredCollection)
 	{
 		OTDataMap authoredMap = (OTDataMap)authoredCollection;
 		OTDataMap userMap = (OTDataMap) userCollection;
 		
 		String [] keys = authoredMap.getKeys();		
 		for(int i=0; i<keys.length; i++) {
 			userMap.put(keys[i], authoredMap.get(keys[i]));
 		}
 	}
 
     /* (non-Javadoc)
      * @see org.concord.framework.otrunk.OTResourceMap#get(java.lang.String)
      */
     public Object get(String key)
     {
     	OTDataMap mapForRead = getMapForRead();
 
 	    if(mapForRead == null) return null;
 	    
 		return mapForRead.get(key);
     }
     
     /* (non-Javadoc)
      * @see org.concord.framework.otrunk.OTResourceMap#getKeys()
      */
     public String[] getKeys()
     {
     	OTDataMap mapForRead = getMapForRead();
 
 	    if(mapForRead == null) return new String[0];
 	    
 		return mapForRead.getKeys();
     }
     
     /* (non-Javadoc)
      * @see org.concord.framework.otrunk.OTResourceMap#put(java.lang.String, java.lang.Object)
      */
     public void put(String key, Object resource)
     {
     	OTDataMap userMap = getUserMap();
    	resource = resolveIDResource(resource);
     	userMap.put(key, resource);
     }    
 }
