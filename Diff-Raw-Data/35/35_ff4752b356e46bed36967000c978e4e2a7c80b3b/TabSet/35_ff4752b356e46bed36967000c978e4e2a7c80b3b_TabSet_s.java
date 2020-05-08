 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
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
 package org.icefaces.mobi.component.tabset;
 
 import org.icefaces.mobi.api.ContentPaneController;
 
 import javax.annotation.PostConstruct;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.el.MethodExpression;
 import javax.el.ValueExpression;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.FacesEvent;
 import javax.faces.event.PhaseId;
 import javax.faces.event.ValueChangeEvent;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.el.ELException;
 
 
 public class TabSet extends TabSetBase implements ContentPaneController {
      private static Logger logger = Logger.getLogger(TabSet.class.getName());
      public static final StringBuilder TABSET_CONTAINER_CLASS = new StringBuilder("mobi-tabset ");
      public static final StringBuilder TABSET_TABS_CLASS = new StringBuilder("mobi-tabset-tabs ");
      public static final StringBuilder TABSET_ACTIVETAB_CLASS = new StringBuilder("activeTab ");
      public static final StringBuilder TABSET_CONTENT_CLASS = new StringBuilder("mobi-tabset-content ");
     public static final StringBuilder TABSET_VISIBLE_PAGECLASS = new StringBuilder("mobi-tabpage");
      public static final StringBuilder TABSET_HIDDEN_PAGECLASS = new StringBuilder("mobi-tabpage-hidden");
 
      private boolean updatePropScriptTag = false;
      private String selectedId;
 
      @Override
      public void setTabIndex(int index){
           super.setTabIndex(index);
           this.findMySelectedId();
      }
      public String getSelectedId(){
         return this.selectedId;
      }
      @PostConstruct
      public void init(){
         this.findMySelectedId();
      }
 
      public void findMySelectedId(){
         this.selectedId = null;
         int childCount = getChildCount();
         if (childCount > 0 ){
             int index = getTabIndex();
             if (index< 0 || index > childCount){
                 index = 0;
             }
             this.selectedId = getChildren().get(index).getId();
      /*      UIComponent selectedComp = this.findComponent(id);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            String panelClientId = selectedComp.getClientId(facesContext);
            //for tagHandler do I need to ensure all children are of type ContentPane here??
            if (null!=panelClientId){
                this.selectedId =  panelClientId;
                return;
            } */
         }
      }
       public void broadcast(FacesEvent event)
             throws AbortProcessingException {
         if (event instanceof ValueChangeEvent) {
             if (event != null) {
                ValueExpression ve = getValueExpression("selectedIten");
                 if (ve != null) {
                     try {
                         ve.setValue(getFacesContext().getELContext(), ((ValueChangeEvent) event).getNewValue());
                     } catch (ELException e) {
                         logger.log(Level.WARNING, "Error creating selected value change event",e);
                     }
                 } else {
                     int tempInt = (Integer) ((ValueChangeEvent) event).getNewValue();
                     this.setTabIndex(tempInt);
                     if (logger.isLoggable(Level.FINEST)){
                         logger.finest("After setting the selectedItem to " + tempInt);
                     }
                 }
                 MethodExpression method = getTabChangeListener();
                 if (method != null) {
                     method.invoke(getFacesContext().getELContext(), new Object[]{event});
                 }
             }
         } else {
             super.broadcast(event);
         }
     }
     public void queueEvent(FacesEvent event) {
        if (event.getComponent() instanceof TabSet) {
             boolean isImmediate = isImmediate();
             if (logger.isLoggable(Level.FINEST)){
                 logger.finest("invoked event for immediate " + isImmediate);
             }
             if (isImmediate) {
                 event.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
             } else {
                 event.setPhaseId(PhaseId.INVOKE_APPLICATION);
             }
         }
         super.queueEvent(event);
     }
 
     public void setUpdatePropScriptTag (boolean update){
         this.updatePropScriptTag = update;
     }
     public boolean isUpdatePropScriptTag(){
         return this.updatePropScriptTag;
     }
 }
