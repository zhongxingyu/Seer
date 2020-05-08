 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.icefaces.mobi.component.contentstack;
 
 import static org.icemobile.util.HTML.CLASS_ATTR;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 
 import org.icefaces.mobi.component.contentnavbar.ContentNavBar;
 import org.icefaces.mobi.component.contentpane.ContentPane;
 import org.icefaces.mobi.renderkit.BaseLayoutRenderer;
 import org.icefaces.mobi.utils.HTML;
 import org.icefaces.mobi.utils.JSFUtils;
 import org.icefaces.mobi.utils.MobiJSFUtils;
 
 public class ContentStackRenderer extends BaseLayoutRenderer {
 
     private static final Logger logger = Logger.getLogger(ContentStackRenderer.class.getName());
 
     @Override
     public void decode(FacesContext facesContext, UIComponent component) {
         ContentStack stack = (ContentStack) component;
         String clientId = stack.getClientId(facesContext);
         String indexStr = facesContext.getExternalContext()
                 .getRequestParameterMap().get(clientId + "_hidden");
         
         if( null != indexStr && indexStr.length() > 0 ) {
             if (!indexStr.equals(stack.getCurrentId())){
                 stack.setCurrentId(indexStr);
             }
         }
     }
 
 
     public void encodeBegin(FacesContext facesContext, UIComponent uiComponent)throws IOException {
          ResponseWriter writer = facesContext.getResponseWriter();
          String clientId = uiComponent.getClientId(facesContext);
          ContentStack container = (ContentStack) uiComponent;
          /* can use stack with contentNavBar so may need to write out javascript for menu */
          if ((container.getContentMenuId() == null) && hasNavBarChild(container)!=null){
             container.setNavBar(true);
          }
          else {
              container.setNavBar(false);
          }
             /* write out root tag.  For current incarnation html5 semantic markup is ignored */
          writer.startElement(HTML.DIV_ELEM, uiComponent);
          writer.writeAttribute(HTML.ID_ATTR, clientId, HTML.ID_ATTR);
         writer.writeAttribute(HTML.CLASS_ATTR, "mobi-contentstack", null);
          //if layoutMenu is used then another div with panes Id is used
          if (container.getContentMenuId()!=null){
              if (null == container.getSingleView()){
                  UIComponent stackMenuComp =  JSFUtils.findChildComponent(uiComponent, container.getContentMenuId());
                  if (stackMenuComp !=null){
                     container.setSingleView(true);
                  }else {
                      container.setSingleView(false);
                  }
              }
              boolean singleView = container.getSingleView();
              if (singleView){
                  writer.writeAttribute("class", ContentStack.CONTAINER_SINGLEVIEW_CLASS, null);
              }
              writer.startElement(HTML.DIV_ELEM, uiComponent);
              writer.writeAttribute(HTML.ID_ATTR, clientId+"_panes", HTML.ID_ATTR);
              if (singleView){
                 writer.writeAttribute("class", ContentStack.PANES_SINGLEVIEW_CLASS, "class" );
              }
          }
         if (container.hasNavBar()){
             writer.startElement(HTML.DIV_ELEM, uiComponent);
             writer.writeAttribute(HTML.ID_ATTR, clientId+"_panes", HTML.ID_ATTR);
         }
     }
 
     public boolean getRendersChildren() {
         return true;
     }
 
     public void encodeChildren(FacesContext facesContext, UIComponent uiComponent) throws IOException{
         for (UIComponent child : uiComponent.getChildren()) {
              if (!(child instanceof ContentPane) && logger.isLoggable(Level.FINER)){
                  logger.finer("all children must be of type ContentPane");
                  return;
              }
         }
         super.renderChildren(facesContext, uiComponent);
     }
 
     public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException {
          ResponseWriter writer = facesContext.getResponseWriter();
          ContentStack stack = (ContentStack) uiComponent;
          String clientId = uiComponent.getClientId(facesContext);
          writer.startElement("span", uiComponent);
          writer.startElement("input", uiComponent);
          writer.writeAttribute("type", "hidden", null);
          writer.writeAttribute("id", clientId+"_hidden", null);
          writer.writeAttribute("name", clientId+"_hidden", null);
          String currentId = stack.getCurrentId();
          if( currentId != null && currentId.length() > 0 )
              writer.writeAttribute("value", currentId, null);
          writer.endElement("input");
          writer.endElement("span");
          writer.endElement(HTML.DIV_ELEM);
          if (stack.getContentMenuId() !=null || stack.hasNavBar()){
              encodeScript(facesContext, uiComponent);
              writer.endElement(HTML.DIV_ELEM);
          }
     }
 
     private void encodeScript(FacesContext facesContext, UIComponent uiComponent) throws IOException{
         ResponseWriter writer = facesContext.getResponseWriter();
         ContentStack stack = (ContentStack) uiComponent;
         String clientId = stack.getClientId(facesContext);
         writer.startElement("span", uiComponent);
         writer.writeAttribute(CLASS_ATTR, "mobi-hidden", null);
         writer.writeAttribute("id", clientId+"_initScr", "id");
         writer.startElement("script", uiComponent);
         writer.writeAttribute("type", "text/javascript", null);
         String currentPaneId = stack.getCurrentId();
         boolean client = false;
         int hashcode = MobiJSFUtils.generateHashCode(System.currentTimeMillis());
         StringBuilder sb = new StringBuilder("mobi.layoutMenu.initClient('").append(clientId).append("'");
         sb.append(",{stackId: '").append(clientId).append("'");
         sb.append(",currentId: '").append(currentPaneId).append("'");
         sb.append(", single: ").append(stack.getSingleView());
         sb.append(",hash: ").append(hashcode);
        
         if( currentPaneId == null || currentPaneId.length() == 0 ){
             //auto-select the first contentPane
             currentPaneId = stack.getChildren().get(0).getId();
         }
         ContentPane currentPane = (ContentPane)JSFUtils.findChildComponent(stack, currentPaneId);
         //if the selectedPaneId is not valid, auto-select the first contentPane
         if( currentPaneId != null && currentPane == null ){
             String warning = "Invalid value for ContentStack.currentId: " + currentPaneId; 
             logger.warning(warning);
             FacesMessage msg = new FacesMessage(warning);
             facesContext.addMessage(stack.getClientId(), msg);
         }
         if( currentPane == null ){
             currentPane = (ContentPane)stack.getChildren().get(0);
             currentPaneId = currentPane.getId();
             stack.setCurrentId(currentPaneId);
         }
         else{
             client = currentPane.isClient();
         }
         String contentMenuId = stack.getContentMenuId();
         if (contentMenuId !=null && contentMenuId.length() > 0){
             UIComponent menu = stack.findComponent(contentMenuId);
             String homeId = null;
             if (null!=menu){
                homeId = menu.getClientId(facesContext);
             }
             sb.append(",home: '").append(homeId).append("'");
         }
         sb.append(",client: ").append(client);
         sb.append("});");
         writer.write(sb.toString());
         writer.endElement("script");
         writer.endElement("span");
     }
     private UIComponent hasNavBarChild( UIComponent comp)  {
        if (comp instanceof ContentNavBar){
             return comp;
         }
         UIComponent child = null;
         UIComponent retComp = null;
         Iterator<UIComponent> children = comp.getFacetsAndChildren();
         while (children.hasNext() && (retComp==null)){
             child = (UIComponent)children.next();
             if (child instanceof ContentNavBar){
                 retComp = child;
                 break;
             }
             retComp = hasNavBarChild(child);
             if (retComp !=null){
                 break;
             }
         }
         return retComp;
     }
 }
