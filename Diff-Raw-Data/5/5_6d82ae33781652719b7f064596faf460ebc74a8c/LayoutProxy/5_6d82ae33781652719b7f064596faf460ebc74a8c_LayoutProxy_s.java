 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.logmanager;
 
 import org.apache.log4j.HTMLLayout;
 import org.apache.log4j.Layout;
 import org.apache.log4j.PatternLayout;
 import org.apache.log4j.SimpleLayout;
 import org.apache.log4j.TTCCLayout;
 import org.apache.log4j.xml.XMLLayout;
 
 public class LayoutProxy extends AbstractProxy<Layout> {
 	
 	protected LayoutType type;
 	
 	// Proxied properties
 	protected String conversionPattern = Constants.DEF_LAYOUT_PATTERN;
 	protected boolean locationInfo;
 	
 	/**
 	 * Creates a proxy for a new layout
 	 * @param type the type
 	 */
 	public LayoutProxy(LayoutType type) {
 		this.type = type;
 	}
 	
 	/**
 	 * Creates an layout proxy based on the given layout
 	 * @param target the layout
 	 */
 	public LayoutProxy(Layout target) {
 		this.target = target;
 		this.type = LayoutType.fromLayout(target);
 		
 		if (target instanceof PatternLayout)
 			this.conversionPattern = ((PatternLayout)target).getConversionPattern();
		if (target instanceof HTMLLayout)
 			this.locationInfo = ((HTMLLayout)target).getLocationInfo();
 	}
 	
 	/**
 	 * Updates the actual layout referenced by this proxy object
 	 */
 	public void updateTarget() {
 		switch (type) {
 		case SIMPLE:
 			target = new SimpleLayout();
 			break;
 		case TTCC:
 			target = new TTCCLayout();
 			break;
 		case PATTERN:
 			target = new PatternLayout(conversionPattern);
 			break;
 		case HTML:
 			target = new HTMLLayout();
 			((HTMLLayout)target).setLocationInfo(locationInfo);
 			break;
 		case XML:
 			target = new XMLLayout();
 			((XMLLayout)target).setLocationInfo(locationInfo);
 			break;
 		}
 	}
 	
 	/**
 	 * Gets the layout type
 	 * @return the layout type
 	 */
 	public LayoutType getType() {
 		return type;
 	}
 
 	/**
 	 * Sets the layout type
 	 * @param type the layout type
 	 */
 	public void setType(LayoutType type) {
 		this.type = type;
 	}
 
 	/**
 	 * Gets the conversion pattern
 	 * Applies to the PATTERN layout type
 	 * @return the layout pattern
 	 */
 	public String getConversionPattern() {
 		return conversionPattern;
 	}
 
 	/**
 	 * Sets the conversion pattern
 	 * Applies to the PATTERN layout type
 	 * @param conversionPattern the conversion pattern
 	 */
 	public void setConversionPattern(String conversionPattern) {
 		this.conversionPattern = conversionPattern;
 	}
 	
 	/**
 	 * Gets whether the layout uses location information
 	 * Applies to XML and HTML layout types
 	 * @return true if layout should use location information
 	 */
 	public boolean getLocationInfo() {
 		return locationInfo;
 	}
 	
 	/**
 	 * Sets whether the layout uses location information
 	 * Applies to XML and HTML layout types
 	 * @param locationInfo true if layout should use location information
 	 */
 	public void setLocationInfo(boolean locationInfo) {
 		this.locationInfo = locationInfo;
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		if (type == LayoutType.PATTERN)
 			return type.toString() + " (" + conversionPattern + ")";
 		else if (type != LayoutType.UNKNOWN)
 			return type.toString();
 		else if (target != null)
 			return target.getClass().getSimpleName();
 		return "";
 	}
 }
