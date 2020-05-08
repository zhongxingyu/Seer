 /*===========================================================================
   Copyright (C) 2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.common.uidescription;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import net.sf.okapi.common.ParameterDescriptor;
 
 /**
  * UI part descriptor for a list of boolean options.
  */
 public class CheckListPart extends AbstractPart {
 	
 	private Map<String, ParameterDescriptor> entries;
 	private int heightHint;
 	
 	/**
 	 * Creates a new ListSelectionPart object with a given parameter descriptor.
 	 * @param label the text to set at the top of the list (or null).
 	 * @param heightHint the suggested height of the list.
 	 */
 	public CheckListPart (String label,
 		int heightHint)
 	{
 		super(new ParameterDescriptor(UUID.randomUUID().toString(), null, label, null));
 		entries = new LinkedHashMap<String, ParameterDescriptor>();
 		this.heightHint = heightHint;
 	}
 
 	@Override
 	protected void checkType () {
 		// Nothing to check
 	}
 	
 	/**
 	 * Gets the suggested height for the list.
 	 * @return the suggested height for the list.
 	 */
 	public int getHeightHint () {
 		return heightHint;
 	}
 
 	/**
 	 * Gets the map of the the entries.
 	 * @return the map of the entries.
 	 */
 	public Map<String, ParameterDescriptor> getEntries () {
 		return entries;
 	}
 
 	/**
 	 * Clears the map of the entries.
 	 */
 	public void clearEntries () {
 		entries.clear();
 	}
 	
 	/**
 	 * Adds one entry in the map.
	 * @param desc the description of the parameter.
 	 */
 	public void addEntry (ParameterDescriptor desc) {
 		entries.put(desc.getName(), desc);
 	}
 	
 }
