 /**
  * License Agreement.
  *
  *  JBoss RichFaces - Ajax4jsf Component Library
  *
  * Copyright (C) 2007  Exadel, Inc.
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
 
 package org.richfaces.renderkit.html;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIParameter;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.PhaseId;
 
 import org.ajax4jsf.context.AjaxContext;
 import org.ajax4jsf.event.AjaxEvent;
 import org.ajax4jsf.javascript.ScriptUtils;
 import org.ajax4jsf.renderkit.AjaxRendererUtils;
 import org.ajax4jsf.renderkit.ComponentVariables;
 import org.ajax4jsf.renderkit.ComponentsVariableResolver;
import org.ajax4jsf.renderkit.RendererUtils.HTML;
 import org.richfaces.component.MenuComponent;
 import org.richfaces.component.UIMenuItem;
 import org.richfaces.component.util.ViewUtil;
 import org.richfaces.renderkit.CompositeRenderer;
 
 
 public class MenuItemRendererBase extends CompositeRenderer {
 	
 	private MenuItemRendererDelegate delegate;
 	
 	public MenuItemRendererBase() {
 		 delegate = new MenuItemRendererDelegate();
 	}
 
     protected Class getComponentClass() {
         return UIMenuItem.class;
     }
 
     public boolean getRendersChildren() {
         return true;
     }
 
     public void doDecode(FacesContext context, UIComponent component) {
 
         super.doDecode(context, component);
 
         ExternalContext exCtx = context.getExternalContext();
         Map rqMap = exCtx.getRequestParameterMap();
         Object clnId = rqMap.get(component.getClientId(context));
         if (clnId == null) {
             clnId = rqMap.get(component.getClientId(context) + ":hidden");
         }
         UIMenuItem menuItem = (UIMenuItem) component;
         if (clnId != null) {
 
             // enqueue event here for this component or for component with Id
             // taken from forId attribute
 
             String mode = resolveSubmitMode(menuItem);
             if (!MenuComponent.MODE_NONE.equalsIgnoreCase(mode)) {
 
                 ActionEvent actionEvent = new ActionEvent(menuItem);
                 
                 if(MenuComponent.MODE_AJAX.equalsIgnoreCase(mode)){
             		new AjaxEvent(menuItem).queue();
             		
                     if (AjaxRendererUtils.isAjaxRequest(context)) {
                         
                         AjaxContext.getCurrentInstance(context)
                             .addAreasToProcessFromComponent(context, menuItem);
                     }
             	}
                 
                 if (menuItem.isImmediate()) {
                 	actionEvent.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
                 } else {
                     actionEvent.setPhaseId(PhaseId.INVOKE_APPLICATION);
                 }
                 menuItem.queueEvent(actionEvent);
             }
         }
     }
     // find and encode UIParameter's components
     public List encodeParams(FacesContext context, UIMenuItem component) throws IOException {
     	
     	UIMenuItem menuItem = component;
     	List params = new ArrayList();
     	StringBuffer buff;
     	
     	List children = menuItem.getChildren();
     	for (Iterator iterator = children.iterator(); iterator.hasNext();) {
     		UIComponent child = (UIComponent) iterator.next();
 				
     		if(child instanceof UIParameter){
 					
     			UIParameter param = (UIParameter)child;
 				String name = param.getName();
 				
 				if (name != null) {
 					buff = new StringBuffer();
 					Object value = param.getValue();
 					buff.append("params[");
 					buff.append(ScriptUtils.toScript(name));
 					buff.append("] = ");
 					buff.append(ScriptUtils.toScript(value));
 					buff.append(";");
 					params.add(buff.toString());
 				}
 			
 			}
     	}
     	
     	return params;
     }
     public String encodeParamsAsObject(FacesContext context, UIMenuItem component) throws IOException {
     	
     	UIMenuItem menuItem = component;
     	StringBuffer buff = new StringBuffer("{");
     	
     	List children = menuItem.getChildren();
     	for (Iterator iterator = children.iterator(); iterator.hasNext();) {
     		UIComponent child = (UIComponent) iterator.next();
 				
     		if(child instanceof UIParameter){
 					
     			UIParameter param = (UIParameter)child;
 				String name = param.getName();
 				
 				if (name != null) {
 					Object value = param.getValue();
 					buff.append(ScriptUtils.toScript(name));
 					buff.append(":");
 					buff.append(ScriptUtils.toScript(value));
 					buff.append(",");
 				}
 			}
     	}
     	if (buff.length()>1) {
     		buff.deleteCharAt(buff.length()-1);
     		buff.append("}");
     		return buff.toString();
     	} else {
     		return null;
     	}
     }    
     
     public void initializeResources(FacesContext context, UIMenuItem menuItem)
             throws IOException {
         ComponentVariables variables =
                 ComponentsVariableResolver.getVariables(this, menuItem);
 
         String resource = menuItem.isDisabled()
                 ? ViewUtil.getResourceURL(menuItem.getIconDisabled())
                 : ViewUtil.getResourceURL(menuItem.getIcon());
         if (resource == null || (resource.length() == 0)) {
             resource = getResource("images/spacer.gif").getUri(
                     context, menuItem);
         }
         variables.setVariable("icon", resource);
         
         // create attributes string for item without parent
         StringBuffer attr = new StringBuffer();
         String attrStr = "";
         if (!(menuItem.getParent() instanceof MenuComponent))
         {
        	String styleClass = (String) menuItem.getAttributes().get(HTML.STYLE_CLASS_ATTR);
         	String str = "";
         	attr.append(",{");
         	if (null!=styleClass && styleClass.length()>0) {
         		attr.append("styleClass:");
         	    attr.append(ScriptUtils.toScript(styleClass));
         	    str = ",";
         	}
        	String onselect = (String) menuItem.getAttributes().get(HTML.onselect_ATTRIBUTE);
         	if (null!=onselect && onselect.length()>0) {
         		attr.append(str);
         		attr.append("onselect:function(event){");
         		attr.append(onselect);
         		attr.append("}");
         	}
         	attr.append("}");
         	if (attr.length()>3) attrStr = attr.toString();
         }
         
         if (menuItem.isDisabled()) {
             variables.setVariable("iconDisabledClasses",
                     "dr-menu-icon-disabled rich-menu-item-icon-disabled");
         } else {
 
             variables.setVariable("onmouseoutInlineStyles",
                     collectInlineStyles(context, menuItem, false));
             variables.setVariable("onmouseoverInlineStyles",
             		collectInlineStyles(context, menuItem, true));
 
             //-----------------------------------
             StringBuffer scriptValue = new StringBuffer();
             String mode = resolveSubmitMode(menuItem);
             
             if (MenuComponent.MODE_AJAX.equalsIgnoreCase(mode)) {
             	scriptValue.append("RichFaces.Menu.updateItem(event,this");
             	scriptValue.append(attrStr);
             	scriptValue.append(");");
            	String event = null;
            	Object onclick = menuItem.getAttributes().get(HTML.onclick_ATTRIBUTE);
            	if(onclick != null && onclick.toString().length()>0){
            		event = HTML.onclick_ATTRIBUTE;
            	}else{
            		Object onselect = menuItem.getAttributes().get(HTML.onselect_ATTRIBUTE);
            		if(onselect != null && onselect.toString().length()>0){
            			event = HTML.onselect_ATTRIBUTE;
            		}
            	}
            	scriptValue.append(AjaxRendererUtils.buildOnEvent(
                        menuItem, context, event).toString());
            	menuItem.getAttributes().put(HTML.onselect_ATTRIBUTE, null);
             } else if (MenuComponent.MODE_SERVER.equalsIgnoreCase(mode)) {
             	
             /*
            	String id = menuItem.getClientId(context);
 			scriptValue.append("var form=A4J.findForm(this);");
 			scriptValue.append("var params={");
 			scriptValue.append(ScriptUtils.toScript(id + ":hidden"));
 			scriptValue.append(":");
 			scriptValue.append(ScriptUtils.toScript(id));
 			scriptValue.append("};");
 			
 			List params = encodeParams(context, menuItem);
 			if(!params.isEmpty()){
 				for (Iterator iterator = params.iterator(); iterator.hasNext();) {
 					scriptValue.append(iterator.next());
 				}
 			}
 			
 			scriptValue.append("Richfaces.jsFormSubmit(");
 			scriptValue.append(ScriptUtils.toScript(id)).append(",");
 			
 			scriptValue.append("form.id").append(",");
 	        Object target = menuItem.getAttributes().get("target");
 	               
 	        if (null != target) {
 	 			scriptValue.append(ScriptUtils.toScript(target));
 	 		} else {
 				scriptValue.append("''");
 			}
 	        
 	        scriptValue.append(",");
 	        scriptValue.append("params);return false;");
 	        */
            	Object onclick = menuItem.getAttributes().get(HTML.onclick_ATTRIBUTE);
            	if(onclick != null && onclick.toString().length()>0){
            		scriptValue.append(onclick.toString());
            		scriptValue.append(";");
            	}
             	scriptValue.append("RichFaces.Menu.submitForm(event,this");
             	String params = encodeParamsAsObject(context, menuItem);
             	if (null!=params) {
             		scriptValue.append(",");
             		scriptValue.append(params);
             	}
     	        String target = (String) menuItem.getAttributes().get("target");
     	        if (null != target && target.length()>0) {
     	        	scriptValue.append(",");
     	 			scriptValue.append(ScriptUtils.toScript(target));
     	        }
     	        
             	scriptValue.append(attrStr);
             	scriptValue.append(")");
             	
             } else {
             	scriptValue.append("RichFaces.Menu.updateItem(event,this");
             	scriptValue.append(attrStr);
             	scriptValue.append(");");
                scriptValue.append(getStringAttributeOrEmptyString(menuItem, HTML.onclick_ATTRIBUTE));
             }
             if (resource.length() > 0) {
                variables.setVariable(HTML.onclick_ATTRIBUTE, scriptValue.toString());
             }
             //-------------------------------
           }
 
     }
 
     protected String getStringAttributeOrEmptyString(UIComponent component,
                                                      String attributeName) {
         String attributeValue =
                 (String) component.getAttributes().get(attributeName);
 
         if (null == attributeValue) {
             attributeValue = "";
         }
 
         return attributeValue;
     }
 
     protected UIComponent getIconFacet(UIMenuItem menuItem) {
         UIComponent iconFacet = null;
         if (menuItem.isDisabled()) {
             iconFacet = menuItem.getFacet("iconDisabled");
         } else {
             iconFacet = menuItem.getFacet("icon");
         }
         return iconFacet;
     }
 
     protected String resolveSubmitMode(UIMenuItem menuItem) {
         String submitMode = menuItem.getSubmitMode();
         if (null != submitMode) {
             return submitMode;
         }
         UIComponent parent = menuItem.getParent();
         while (null != parent) {
             if (parent instanceof MenuComponent) {
                 return ((MenuComponent) parent).getSubmitMode();
             }
             parent = parent.getParent();
         }
 
         return MenuComponent.MODE_SERVER;
     }
 
     protected String collectInlineStyles(FacesContext context, UIMenuItem menuItem, boolean isOnmouseover) {
     	return delegate.collectInlineStyles(context, menuItem, isOnmouseover);
     }
     
     @Deprecated
     protected String processInlineStyles(FacesContext context, UIMenuItem menuItem, boolean isOnmouseover) {
     	return delegate.processInlineStyles(context, menuItem, isOnmouseover);
     }
     
     protected UIComponent getParentMenu(FacesContext context, UIMenuItem menuItem) {
 		return delegate.getParentMenu(context, menuItem);
 	}
     
     public void initializeStyles(FacesContext context, UIMenuItem menuItem) {
     	ComponentVariables variables =
 			ComponentsVariableResolver.getVariables(this, menuItem);
 		delegate.initializeStyles(context, menuItem, menuItem.isDisabled(), variables);
 	}
 }
