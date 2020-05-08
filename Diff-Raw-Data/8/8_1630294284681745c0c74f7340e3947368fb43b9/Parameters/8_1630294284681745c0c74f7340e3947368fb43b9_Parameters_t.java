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
 
 package net.sf.okapi.steps.common.removetarget;
 
 import net.sf.okapi.common.BaseParameters;
 import net.sf.okapi.common.EditorFor;
 import net.sf.okapi.common.ParametersDescription;
 import net.sf.okapi.common.uidescription.EditorDescription;
 import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;
 
 @EditorFor(Parameters.class)
 public class Parameters extends BaseParameters implements IEditorDescriptionProvider {
 
 	private static final String TUS_FOR_TARGET_REMOVAL = "tusForTargetRemoval";
 	private static final String TARGET_LOCALES_TO_KEEP = "targetLocalesToKeep";
 	private static final String FILTER_BASED_ON_IDS = "filterBasedOnIds";
 	private static final String REMOVE_TU_IF_NO_TARGET = "removeTUIfNoTarget";
 	
 	private String tusForTargetRemoval;
 	private String targetLocalesToKeep;
 	private boolean filterBasedOnIds;
 	private boolean removeTUIfNoTarget;
 	
 	public Parameters() {
 		reset();
 	}
 	
 	public void reset() {
 		tusForTargetRemoval = "";
 		targetLocalesToKeep = "";
 		filterBasedOnIds = true;
 		removeTUIfNoTarget = false;
 	}
 
 	@Override
 	public void fromString(String data) {
 		reset();
 		buffer.fromString(data);
 		// Read the file content as a set of fields
 		tusForTargetRemoval = buffer.getString(TUS_FOR_TARGET_REMOVAL, tusForTargetRemoval);
 		targetLocalesToKeep = buffer.getString(TARGET_LOCALES_TO_KEEP, targetLocalesToKeep);
 		filterBasedOnIds = buffer.getBoolean(FILTER_BASED_ON_IDS, filterBasedOnIds);
 		removeTUIfNoTarget = buffer.getBoolean(REMOVE_TU_IF_NO_TARGET, removeTUIfNoTarget);
 	}
 
 	@Override
 	public String toString() {
 		buffer.reset();		
 		buffer.setString(TUS_FOR_TARGET_REMOVAL, tusForTargetRemoval);
 		buffer.setString(TARGET_LOCALES_TO_KEEP, targetLocalesToKeep);
 		buffer.setBoolean(FILTER_BASED_ON_IDS, filterBasedOnIds);
 		buffer.setBoolean(REMOVE_TU_IF_NO_TARGET, removeTUIfNoTarget);
 		return buffer.toString();
 	}
 	
 	public void setTusForTargetRemoval(String tusForTargetRemoval) {
 		this.tusForTargetRemoval = tusForTargetRemoval;
 	}
 
 	public String getTusForTargetRemoval() {
 		return tusForTargetRemoval;
 	}
 
 	public void setTargetLocalesToKeep(String targetLocalesToKeep) {
 		this.targetLocalesToKeep = targetLocalesToKeep;
 	}
 
 	public String getTargetLocalesToKeep() {
 		return targetLocalesToKeep;
 	}
 
 	public boolean isFilterBasedOnIds() {
 		return filterBasedOnIds;
 	}
 	
 	public void setFilterBasedOnIds(boolean filterBasedOnIds) {
 		this.filterBasedOnIds = filterBasedOnIds;
 	}
 
 	public boolean isRemoveTUIfNoTarget() {
 		return removeTUIfNoTarget;
 	}
 	
 	public void setRemoveTUIfNoTarget(boolean removeTUIfNoTarget) {
 		this.removeTUIfNoTarget = removeTUIfNoTarget;
 	}
 	
 	@Override
 	public ParametersDescription getParametersDescription() {
 		ParametersDescription desc = new ParametersDescription(this);
 		desc.add(TUS_FOR_TARGET_REMOVAL, 
 				"Comma-delimited list of ids of the text units where targets are to be removed (empty - remove all targets)", 
 				null);
 		desc.add(TARGET_LOCALES_TO_KEEP, 
 				"Comma-delimited list of locales of the text units of targets that should be kept (empty - keep all targets)", 
 				null);
 		desc.add(FILTER_BASED_ON_IDS, 
 				"If true filter on ID's, if false filter on locales (you cannot filter on both)", 
 				null);
 		desc.add(REMOVE_TU_IF_NO_TARGET, 
 				"If true remove the Text Unit if it has no remaining targets, if false do nothing", 
 				null);
 
 		return desc;
 	}
 
 	@Override
 	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
 		EditorDescription desc = new EditorDescription("Remove Target Options", true, false);
 		desc.addCheckboxPart(paramsDesc.get(FILTER_BASED_ON_IDS));
 		desc.addCheckboxPart(paramsDesc.get(REMOVE_TU_IF_NO_TARGET));
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(TUS_FOR_TARGET_REMOVAL));
		tip.setAllowEmpty(true);
		tip = desc.addTextInputPart(paramsDesc.get(TARGET_LOCALES_TO_KEEP));
		tip.setAllowEmpty(true);
 		return desc;
 	}
 
 }
