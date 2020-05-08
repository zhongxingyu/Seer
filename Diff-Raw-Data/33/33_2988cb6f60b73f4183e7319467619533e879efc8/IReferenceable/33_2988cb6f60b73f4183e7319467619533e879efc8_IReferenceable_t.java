 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.common.resource;
 
 /**
  * Interface for all resources that can be passed as referents through the filter events.
  */
 public interface IReferenceable {
 
 	/**
	 * Sets the flag indicating if this resource is a referent (i.e. is referred to by another 
	 * resource) or not. This also sets the count of time this referent is referenced to 1.
 	 * @param value True if the resource is a referent, false if it is not.
 	 */
 	public void setIsReferent (boolean value);
 	
 	/**
 	 * Indicates if this resource is a referent (i.e. is referred to by another resource)
 	 * or not.
 	 * @return True if this resource is a referent, false if it is not.
 	 */
 	public boolean isReferent ();
 
	/**
	 * Gets the number of time this referent is referenced to.
	 * @return the number of time this referent is referenced to.
	 */
 	public int getReferenceCount ();
 	
	/**
	 * Sets the number of time this referent is referenced to.
	 * @param value the number of time this referent is referenced to.
	 */
 	public void setReferenceCount (int value);
 	
 }
