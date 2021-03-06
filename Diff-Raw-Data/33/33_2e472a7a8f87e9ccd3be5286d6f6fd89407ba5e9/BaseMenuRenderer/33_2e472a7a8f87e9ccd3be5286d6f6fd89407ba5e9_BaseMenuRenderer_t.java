 /*
  * Copyright 2009-2012 Prime Teknoloji.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.primefaces.component.menu;
 
 import java.io.IOException;
 import java.util.Iterator;
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
import org.primefaces.component.api.AjaxSource;
 import org.primefaces.component.menuitem.MenuItem;
 import org.primefaces.component.separator.Separator;
 import org.primefaces.component.submenu.Submenu;
 import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.AjaxRequestBuilder;
 import org.primefaces.util.ComponentUtils;
 
 public abstract class BaseMenuRenderer extends CoreRenderer {
 
     @Override
 	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
 		AbstractMenu menu = (AbstractMenu) component;
 
         if(menu.isDynamic()) {
             menu.buildMenuFromModel();
         }
 
 		encodeMarkup(context, menu);
 		encodeScript(context, menu);
 	}
 
     protected abstract void encodeMarkup(FacesContext context, AbstractMenu abstractMenu) throws IOException;
 
     protected abstract void encodeScript(FacesContext context, AbstractMenu abstractMenu) throws IOException;
 
     protected void encodeMenuItem(FacesContext context, MenuItem menuItem) throws IOException {
 		String clientId = menuItem.getClientId(context);
         ResponseWriter writer = context.getResponseWriter();
         String icon = menuItem.getIcon();
 
 		if(menuItem.shouldRenderChildren()) {
 			renderChildren(context, menuItem);
 		}
         else {
             boolean disabled = menuItem.isDisabled();
             String onclick = menuItem.getOnclick();
             
             writer.startElement("a", null);
             writer.writeAttribute("id", menuItem.getClientId(context), null);
             
             String styleClass = menuItem.getStyleClass();
             styleClass = styleClass == null ? AbstractMenu.MENUITEM_LINK_CLASS : AbstractMenu.MENUITEM_LINK_CLASS + " " + styleClass;
             styleClass = disabled ? styleClass + " ui-state-disabled" : styleClass;
             
             writer.writeAttribute("class", styleClass, null);
             
             if(menuItem.getStyle() != null) 
                 writer.writeAttribute("style", menuItem.getStyle(), null);
                         
 			if(menuItem.getUrl() != null) {
                 String href = disabled ? "#" : getResourceURL(context, menuItem.getUrl());
 				writer.writeAttribute("href", href, null);
                                 
 				if(menuItem.getTarget() != null) 
                     writer.writeAttribute("target", menuItem.getTarget(), null);
 			}
             else {
 				writer.writeAttribute("href", "#", null);
 
 				UIComponent form = ComponentUtils.findParentForm(context, menuItem);
 				if(form == null) {
 					throw new FacesException("Menubar must be inside a form element");
 				}
 
                String command = menuItem.isAjax() ? buildAjaxRequest(context, menuItem, form) : buildNonAjaxRequest(context, menuItem, form.getClientId(context), clientId);
 
                 onclick = onclick == null ? command : onclick + ";" + command;
 			}
 
             if(onclick != null && !disabled) {
                 writer.writeAttribute("onclick", onclick, null);
             }
  
             if(icon != null) {
                 writer.startElement("span", null);
                 writer.writeAttribute("class", AbstractMenu.MENUITEM_ICON_CLASS + " " + icon, null);
                 writer.endElement("span");
             }
 
 			if(menuItem.getValue() != null) {
                 writer.startElement("span", null);
                 writer.writeAttribute("class", AbstractMenu.MENUITEM_TEXT_CLASS, null);
                 writer.writeText((String) menuItem.getValue(), "value");
                 writer.endElement("span");
             }
 
             writer.endElement("a");
 		}
 	}
     
     protected void encodeTieredMenuContent(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
 
         for(Iterator<UIComponent> iterator = component.getChildren().iterator(); iterator.hasNext();) {
             UIComponent child = (UIComponent) iterator.next();
 
             if(child.isRendered()) {
 
                 if(child instanceof MenuItem) {
                     writer.startElement("li", null);
                     writer.writeAttribute("class", Menu.MENUITEM_CLASS, null);
                     writer.writeAttribute("role", "menuitem", null);
                     encodeMenuItem(context, (MenuItem) child);
                     writer.endElement("li");
                 } 
                 else if(child instanceof Submenu) {
                     Submenu submenu = (Submenu) child;
                     String style = submenu.getStyle();
                     String styleClass = submenu.getStyleClass();
                     styleClass = styleClass == null ? Menu.TIERED_SUBMENU_CLASS : Menu.TIERED_SUBMENU_CLASS + " " + styleClass;
         
                     writer.startElement("li", null);
                     writer.writeAttribute("class", styleClass, null);
                     if(style != null) {
                         writer.writeAttribute("style", style, null);
                     }
                     writer.writeAttribute("role", "menuitem", null);
                     writer.writeAttribute("aria-haspopup", "true", null);
                     encodeTieredSubmenu(context, (Submenu) child);
                     writer.endElement("li");
                 } 
                 else if(child instanceof Separator) {
                     encodeSeparator(context, (Separator) child);
                 }
             }
         }
     }
     
     protected void encodeTieredSubmenu(FacesContext context, Submenu submenu) throws IOException{
 		ResponseWriter writer = context.getResponseWriter();
         String icon = submenu.getIcon();
         String label = submenu.getLabel();
 
         //title
         writer.startElement("a", null);
         writer.writeAttribute("href", "javascript:void(0)", null);
         writer.writeAttribute("class", Menu.MENUITEM_LINK_CLASS, null);
 
         if(icon != null) {
             writer.startElement("span", null);
             writer.writeAttribute("class", Menu.MENUITEM_ICON_CLASS + " " + icon, null);
             writer.endElement("span");
         }
 
         if(label != null) {
             writer.startElement("span", null);
             writer.writeAttribute("class", Menu.MENUITEM_TEXT_CLASS, null);
             writer.writeText(submenu.getLabel(), "value");
             writer.endElement("span");
         }
         
         encodeTieredSubmenuIcon(context, submenu);
 
         writer.endElement("a");
 
         //submenus and menuitems
 		if(submenu.getChildCount() > 0) {
 			writer.startElement("ul", null);
             writer.writeAttribute("class", Menu.TIERED_CHILD_SUBMENU_CLASS, null);
             writer.writeAttribute("role", "menu", null);
 
 			encodeTieredMenuContent(context, submenu);
 
 			writer.endElement("ul");
 		}
 	}
     
     protected void encodeTieredSubmenuIcon(FacesContext context, Submenu submenu) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         
         writer.startElement("span", null);
         writer.writeAttribute("class", Menu.SUBMENU_RIGHT_ICON_CLASS, null);
         writer.endElement("span");
     }
     
     protected void encodeSeparator(FacesContext context, Separator separator) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String style = separator.getStyle();
         String styleClass = separator.getStyleClass();
         styleClass = styleClass == null ? Menu.SEPARATOR_CLASS : Menu.SEPARATOR_CLASS + " " + styleClass;
 
         //title
         writer.startElement("li", null);
         writer.writeAttribute("class", styleClass, null);
         if(style != null) {
             writer.writeAttribute("style", style, null);
         }
         
         writer.endElement("li");
 	}
 
     @Override
 	public void encodeChildren(FacesContext facesContext, UIComponent component) throws IOException {
 		//Do nothing
 	}
 
     @Override
 	public boolean getRendersChildren() {
 		return true;
 	}
    
    protected String buildAjaxRequest(FacesContext context, MenuItem menuItem, UIComponent form) {
        UIComponent component = (UIComponent) menuItem;
        String clientId = component.getClientId(context);
        
        AjaxRequestBuilder builder = new AjaxRequestBuilder();
        
        String request = builder.source(clientId)
                        .form(form.getClientId(context))
                        .process(context, component, menuItem.getProcess())
                        .update(context, component, menuItem.getUpdate())
                        .async(menuItem.isAsync())
                        .global(menuItem.isGlobal())
                        .partialSubmit(menuItem.isPartialSubmit())
                        .onstart(menuItem.getOnstart())
                        .onerror(menuItem.getOnerror())
                        .onsuccess(menuItem.getOnsuccess())
                        .oncomplete(menuItem.getOncomplete())
                        .params(component)
                        .preventDefault()
                        .build();

        return request;
    }
 }
