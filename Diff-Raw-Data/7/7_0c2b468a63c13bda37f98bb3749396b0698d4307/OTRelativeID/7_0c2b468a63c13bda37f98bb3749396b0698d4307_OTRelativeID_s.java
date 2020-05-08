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
 * $Revision: 1.4 $
 * $Date: 2007-10-04 21:28:21 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.datamodel;
 
 import org.concord.framework.otrunk.OTID;
 
 /**
  * OTXMLPathID
  * Class name and description
  *
  * Date created: Apr 17, 2005
  *
  * @author scott<p>
  *
  */
 public class OTRelativeID
     implements OTID
 {
     OTID rootId = null;
     OTID relativeId = null;
     
     public OTRelativeID(OTID rootId, OTID relativeId)
     {
         this.rootId = rootId;
         this.relativeId = relativeId;
         
         if(rootId == null && relativeId == null) {
             throw new UnsupportedOperationException("both args null");
         }
     }
 
     protected String toStringInternal()
     {
         if(rootId == null) {
            return relativeId.toString();
         }
         
         return rootId.toExternalForm() + "!" + relativeId.toExternalForm();    	
     }
     
     /**
      * This returns a unique string for this id.  This is not the actual id.<p>
      * 
      * The actual id is not returned because using the toString method on an OTID
      * cannot always return the correct thing.  The method OTObjectService.getExternalID 
      * should be used instead. 
      * 
      * @see java.lang.Object#toString()
      */
     public String toString()
     {
     	return "%" + toStringInternal();
     }
     
     public OTID getRootId()
     {
         return rootId;        
     }
     
     public OTID getRelativeId()
     {
         return relativeId;
     }
     
     /* (non-Javadoc)
      * @see java.lang.Object#hashCode()
      */
     public int hashCode()
     {
         return toStringInternal().hashCode();
     }
     
     public boolean equals(Object other)
     {
         if(!(other instanceof OTRelativeID)) {
             return false;
         }
         
         OTRelativeID otherId = (OTRelativeID)other;
         if(otherId.getRelativeId() == null && relativeId != null) {
             return false;
         }
         
         if(otherId.getRootId() == null && rootId != null) {
             return false;
         }
         
         return (otherId.getRelativeId() == null ||   
                 otherId.getRelativeId().equals(relativeId))
                 &&
                 (otherId.getRootId() == null || 
                         otherId.getRootId().equals(rootId));
                         
     }
 
 	public String toExternalForm()
     {
 		return toStringInternal();
     }
 }
