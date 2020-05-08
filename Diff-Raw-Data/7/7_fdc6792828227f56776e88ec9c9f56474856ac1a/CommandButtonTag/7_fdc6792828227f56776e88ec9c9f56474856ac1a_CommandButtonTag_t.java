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
 
 package org.icemobile.jsp.tags;
 
 import org.icemobile.component.IButton;
 import org.icemobile.jsp.tags.input.CommandButtonGroupTag;
 import org.icemobile.renderkit.ButtonCoreRenderer;
 
 import javax.servlet.jsp.tagext.Tag;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 
 /**
  *
  */
 public class CommandButtonTag extends BaseSimpleTag implements IButton{
     private static Logger logger = Logger.getLogger(CommandButtonTag.class.getName());
 
     private String buttonType;
     private String panelConfirmation;
     private String submitNotification;
     private String submitNotificationId;
     private String panelConfirmationId;
     private Object value;
     private String type = "submit";
     private boolean selectedButton;
     private boolean selected = false;
     private String groupId;
     private String name;
 
     private ButtonCoreRenderer renderer;
     private TagWriter writer;
     private CommandButtonGroupTag mParent;
     private StringBuilder jsCall;
     private boolean parentDisabled = false;
 
     public void setParent(Tag parent) {
         if ((parent instanceof CommandButtonGroupTag)) {
             this.mParent = (CommandButtonGroupTag) parent;
            logger.info(this+" setting tag parent " + parent);
         }
     }
 
     public void doTag() throws IOException {
         /* set selected if my id is same as the selectedId of the buttonGroup
         * so application doesn't have ot carry the logic*/
         if (this.getParent() instanceof CommandButtonGroupTag){
             mParent = (CommandButtonGroupTag) this.getParent();
             checkSelected();
             this.groupId = mParent.getId();
            logger.info("button has groupId = "+this.groupId);
         }
         writer = new TagWriter(getContext());
         renderer = new ButtonCoreRenderer();
         renderer.encodeEnd(this, writer);
     }
 
     public void checkSelected(){
         //logger.info("parent has selected id =" + mParent.getSelectedId());
         if (mParent.isDisabled()) { //no changes allowed if parent is disabled
             this.parentDisabled=true;
         }
         if (mParent.getSelectedId()!=null){
             String selId = mParent.getSelectedId();
             if (selId.equals(this.id)){
                 this.selectedButton = true;
                 this.selected=true;
             }else {
                 this.selectedButton = false;
                 this.selected = false;
             }
         }else {
             this.selectedButton = false;
             this.selected = false;
         }
     }
     public String getId() {
         return this.id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getStyle() {
         return this.style;
     }
 
     public Boolean isSelectedButton() {
         return this.selectedButton;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void setSelectedButton(Boolean selected) {
         this.selectedButton = selected;
     }
 
     public String getGroupId() {
         return this.groupId;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void setGroupId(String groupId) {
         this.groupId = groupId;
     }
 
     public void setStyle(String style) {
         this.style = style;
     }
 
     public String getStyleClass() {
         return this.styleClass;
     }
 
     public void setStyleClass(String styleClass) {
         this.styleClass = styleClass;
     }
 
     public String getButtonType() {
         return this.buttonType;
     }
 
     public void setButtonType(String buttonType) {
         this.buttonType = buttonType;
     }
 
     public boolean isDisabled() {
         return this.disabled;
     }
 
     public void setDisabled(boolean disabled) {
         this.disabled = disabled;
     }
 
     public Object getValue() {
         return this.value;
     }
 
     public void setValue(Object value) {
         this.value = value;
     }
 
    public String getName() {
         return this.name;
     }
 
     public void setName(String name) {
         this.name = name;
     }    public String getType() {
         return this.type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public boolean isSelected() {
         return this.selected;
     }
 
     public void setSelected(boolean selected) {
         this.selected = selected;
     }
 
     public boolean isSingleSubmit() {
         return false;
     }
 
     public String getSubmitNotification() {
         return this.submitNotification;
     }
 
     public void setSubmitNotification(String sn){
         this.submitNotification= sn;
     }
 
     public void setPanelConfirmation(String pc){
         this.panelConfirmation = pc;
     }
     public String getPanelConfirmation() {
         return this.panelConfirmation;
     }
 
     public String getParams() {
         return null;
     }
 
     public void setParams(String params) {
         //no JSP support at this time
     }
 
     public String getBehaviors() {
         return null;  //JSF only at this time
     }
 
     public void setBehaviors(String behaviors) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void setPanelConfirmationId(String pcId) {
         //
     }
 
     public String getPanelConfirmationId() {
         return this.panelConfirmation;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void setSubmitNotificationId(String snId) {
         // JSF
     }
 
     public String getSubmitNotificationId() {
         return this.submitNotification;
     }
 
     public String getOpenContentPane() {
         return null;
     }
 
     public StringBuilder getJsCall() {
         return null;
     }
 
     public void setJsCall(StringBuilder jsCall) {
         this.jsCall = jsCall;
     }
     public boolean isParentDisabled(){
         return this.parentDisabled;
     }
     public void setParentDisabled(boolean dis){
         this.parentDisabled = dis;
     }
     public void release(){
         super.release();
         this.mParent = null ;
         this.writer= null;
         this.renderer=null;
     }
 }
