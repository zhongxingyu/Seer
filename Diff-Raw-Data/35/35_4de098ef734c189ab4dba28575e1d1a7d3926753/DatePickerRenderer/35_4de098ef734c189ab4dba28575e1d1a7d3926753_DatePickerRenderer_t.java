 /**
  * License Agreement.
  *
  * Ajax4jsf 1.1 - Natural Ajax for Java Server Faces (JSF)
  *
  * Copyright (C) 2007 Exadel, Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1 as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
  */
 
 package org.omidbiz.renderkit.html;
 
 
 // 
 // Imports
 //
 import java.util.Iterator;
 import java.util.Collection;
 import java.util.Map;
 import java.io.IOException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.ajax4jsf.renderkit.ComponentsVariableResolver;
 import org.ajax4jsf.renderkit.ComponentVariables;
 import org.ajax4jsf.resource.InternetResource;
 import org.ajax4jsf.resource.InternetResource;
 //
 //
 //
 
 
 import org.omidbiz.renderkit.DatePickerRendererBase;
 
 
 
 /**
  * Renderer for component class org.omidbiz.renderkit.html.DatePickerRenderer
  */
 public class DatePickerRenderer extends DatePickerRendererBase {
 
 	public DatePickerRenderer () {
 		super();
 	}
 
 	// 
 	// Declarations
 	//
 	private final InternetResource[] scripts = {
 						getResource("script/jalali.js")
 						,
 				getResource("script/calendar.js")
 						,
 				getResource("script/calendar-setup.js")
 						,
 				getResource("script/calendar-fa.js")
 	};
 
 private InternetResource[] scriptsAll = null;
 
 protected InternetResource[] getScripts() {
 	synchronized (this) {
 		if (scriptsAll == null) {
 			InternetResource[] rsrcs = super.getScripts();
 			boolean ignoreSuper = rsrcs == null || rsrcs.length == 0;
 			boolean ignoreThis = scripts == null || scripts.length == 0;
 			
 			if (ignoreSuper) {
 				if (ignoreThis) {
 					scriptsAll = new InternetResource[0];	
 				} else {
 					scriptsAll = scripts;
 				}
 			} else {
 				if (ignoreThis) {
 					scriptsAll = rsrcs;
 				} else {
 					java.util.Set rsrcsSet = new java.util.LinkedHashSet();
 
 					for (int i = 0; i < rsrcs.length; i++ ) {
 						rsrcsSet.add(rsrcs[i]);
 					}
 
 					for (int i = 0; i < scripts.length; i++ ) {
 						rsrcsSet.add(scripts[i]);
 					}
 
 					scriptsAll = (InternetResource[]) rsrcsSet.toArray(new InternetResource[rsrcsSet.size()]);
 				}
 			}
 		}
 	}
 	
 	return scriptsAll;
 }
 	private final InternetResource[] styles = {
 						getResource("css/aqua/theme.css")
 	};
 
 private InternetResource[] stylesAll = null;
 
 protected InternetResource[] getStyles() {
 	synchronized (this) {
 		if (stylesAll == null) {
 			InternetResource[] rsrcs = super.getStyles();
 			boolean ignoreSuper = rsrcs == null || rsrcs.length == 0;
 			boolean ignoreThis = styles == null || styles.length == 0;
 			
 			if (ignoreSuper) {
 				if (ignoreThis) {
 					stylesAll = new InternetResource[0];	
 				} else {
 					stylesAll = styles;
 				}
 			} else {
 				if (ignoreThis) {
 					stylesAll = rsrcs;
 				} else {
 					java.util.Set rsrcsSet = new java.util.LinkedHashSet();
 
 					for (int i = 0; i < rsrcs.length; i++ ) {
 						rsrcsSet.add(rsrcs[i]);
 					}
 
 					for (int i = 0; i < styles.length; i++ ) {
 						rsrcsSet.add(styles[i]);
 					}
 
 					stylesAll = (InternetResource[]) rsrcsSet.toArray(new InternetResource[rsrcsSet.size()]);
 				}
 			}
 		}
 	}
 	
 	return stylesAll;
 }
 	// 
 	// 
 	//
 
 
 	private String convertToString(Object obj ) {
 		return ( obj == null ? "" : obj.toString() );
 	}
 	private String convertToString(boolean b ) {
 		return String.valueOf(b);
 	}
 	private String convertToString(int b ) {
 		return b!=Integer.MIN_VALUE?String.valueOf(b):"";
 	}
 	private String convertToString(long b ) {
 		return b!=Long.MIN_VALUE?String.valueOf(b):"";
 	}
 	
 	private boolean isEmpty(Object o) {
 		if (null == o) {
 			return true;
 		}
 		if (o instanceof String ) {
 			return (0 == ((String)o).length());
 		}
 		if (o instanceof Collection) {
 			return (0 == ((Collection)o).size());
 		}
 		if (o instanceof Map) {
 			return (0 == ((Map)o).size());
 		}
 		if (o.getClass().isArray()) {
 			return (0 == ((Object [])o).length);
 		}
 		return false;
 	}
 	
 	/**
 	 * Get base component class, targetted for this renderer. Used for check arguments in decode/encode.
 	 * @return
 	 */
 	protected Class getComponentClass() {
 		return org.omidbiz.component.UIDatePicker.class;
 	}
 
 
 	public void doEncodeEnd(ResponseWriter writer, FacesContext context, org.omidbiz.component.UIDatePicker component, ComponentVariables variables) throws IOException {
 	  java.lang.String clientId = component.getClientId(context);
 variables.setVariable("icon", getResource( "images/inputDate.png" ).getUri(context, component) );
 
 variables.setVariable("active", getResource( "css/aqua/active-bg.gif" ).getUri(context, component) );
 
 variables.setVariable("hover", getResource( "css/aqua/hover-bg.gif" ).getUri(context, component) );
 
 variables.setVariable("menu", getResource( "css/aqua/menuarrow.gif" ).getUri(context, component) );
 
 variables.setVariable("norm", getResource( "css/aqua/normal-bg.gif" ).getUri(context, component) );
 
 variables.setVariable("row", getResource( "css/aqua/rowhover-bg.gif" ).getUri(context, component) );
 
 variables.setVariable("status", getResource( "css/aqua/status-bg.gif" ).getUri(context, component) );
 
 variables.setVariable("title", getResource( "css/aqua/title-bg.gif" ).getUri(context, component) );
 
 variables.setVariable("today", getResource( "css/aqua/today-bg.gif" ).getUri(context, component) );
 
 variables.setVariable("dark", getResource( "css/aqua/dark-bg.gif" ).getUri(context, component) );
 
 variables.setVariable("datePattern", component.getAttributes().get("datePattern") );
 variables.setVariable("weekNumbers", component.getAttributes().get("weekNumbers") );
 variables.setVariable("showOthers", component.getAttributes().get("showOthers") );
 variables.setVariable("dateType", component.getAttributes().get("dateType") );
 writer.startElement("input", component);
 			getUtils().writeAttribute(writer, "id", clientId );
 						getUtils().writeAttribute(writer, "name", clientId );
 						getUtils().writeAttribute(writer, "type", "text" );
 						getUtils().writeAttribute(writer, "value", getSolarValueAsString(context,component) );
 			
 writer.endElement("input");
 writer.startElement("img", component);
 			getUtils().writeAttribute(writer, "calss", ".name" );
 						getUtils().writeAttribute(writer, "id", convertToString(clientId) + "_btn" );
 						getUtils().writeAttribute(writer, "src", variables.getVariable("icon") );
 						getUtils().writeAttribute(writer, "style", "vertical-align: top;" );
 			
 writer.endElement("img");
 writer.startElement("script", component);
 			getUtils().writeAttribute(writer, "type", "text/javascript" );
 			
 writer.writeText(convertToString("Calendar.setup({\n					inputField  : \"" + convertToString(clientId) + "\",   // id of the input field\n					button      : \"" + convertToString(clientId) + "_btn\",   // trigger for the calendar (button ID)\n		       		ifFormat    : \"" + convertToString(variables.getVariable("datePattern")) + "\",       // format of the input field\n        			dateType	: '" + convertToString(variables.getVariable("dateType")) + "',\n        			showOthers	: " + convertToString(variables.getVariable("showOthers")) + ",\n					weekNumbers : " + convertToString(variables.getVariable("weekNumbers")) + "\n				});"),null);
 
 writer.endElement("script");
 
 	}		
 	
 	public void doEncodeEnd(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException {
 		ComponentVariables variables = ComponentsVariableResolver.getVariables(this, component);
 		doEncodeEnd(writer, context, (org.omidbiz.component.UIDatePicker)component, variables );
 
 		ComponentsVariableResolver.removeVariables(this, component);
 	}		
 	
 
 }
