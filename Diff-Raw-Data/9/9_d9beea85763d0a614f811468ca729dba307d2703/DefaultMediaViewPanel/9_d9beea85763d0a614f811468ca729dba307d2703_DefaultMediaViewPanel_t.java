 /*
  * Copyright (c) 2012-2013 Veniamin Isaias.
  *
  * This file is part of web4thejob.
  *
  * Web4thejob is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or any later version.
  *
  * Web4thejob is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with web4thejob.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.web4thejob.web.panel;
 
 import org.springframework.context.annotation.Scope;
 import org.springframework.util.StringUtils;
 import org.web4thejob.command.Command;
 import org.web4thejob.command.CommandEnum;
 import org.web4thejob.context.ContextUtil;
 import org.web4thejob.message.Message;
 import org.web4thejob.message.MessageArgEnum;
 import org.web4thejob.message.MessageEnum;
 import org.web4thejob.message.MessageListener;
 import org.web4thejob.orm.Entity;
 import org.web4thejob.orm.Path;
 import org.web4thejob.orm.PathMetadata;
 import org.web4thejob.orm.annotation.ImageHolder;
 import org.web4thejob.setting.SettingEnum;
 import org.web4thejob.web.dialog.Dialog;
 import org.web4thejob.web.dialog.ValueInputDialog;
 import org.web4thejob.web.panel.base.AbstractMutablePanel;
 import org.web4thejob.web.util.ZkUtil;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.HtmlBasedComponent;
 import org.zkoss.zkplus.databind.DataBinder;
 import org.zkoss.zul.Iframe;
 import org.zkoss.zul.Image;
 
 /**
  * @author Veniamin Isaias
  * @since 3.1.0
  */
 
 @org.springframework.stereotype.Component
 @Scope("prototype")
 public class DefaultMediaViewPanel extends AbstractMutablePanel implements MediaViewPanel {
     // ------------------------------ FIELDS ------------------------------
     private Component comp;
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     public DefaultMediaViewPanel() {
         this(MutableMode.READONLY);
     }
 
     public DefaultMediaViewPanel(MutableMode mutableMode) {
         super(mutableMode);
     }
 
     @Override
     protected void registerSettings() {
         super.registerSettings();
         registerSetting(SettingEnum.MEDIA_PROPERTY, null);
         registerSetting(SettingEnum.PANEL_STYLE, null);
     }
 
     @Override
     protected void afterSettingsSet() {
         super.afterSettingsSet();
         arrangeForMutableMode();
     }
 
     private void arrangeForMutableMode() {
         if (comp != null) {
             comp.detach();
             comp = null;
         }
 
         if (StringUtils.hasText(getSettingValue(SettingEnum.MEDIA_PROPERTY, ""))) {
             PathMetadata pathMetadata = ContextUtil.getMRS().getPropertyPath(getTargetType(),
                     new Path(getSettingValue(SettingEnum.MEDIA_PROPERTY, "")));
             dataBinder = new DataBinder();
 
             if (pathMetadata.getLastStep().isAnnotatedWith(ImageHolder.class)) {
                 Image image = new Image();
                 comp = image;
             } else {
                 Iframe iframe = new Iframe();
                 iframe.setVflex("true");
                 iframe.setWidth("100%");
                 iframe.setScrolling("auto");
                 iframe.setAutohide(true);
                 iframe.setStyle("background-color:transparent");
                 comp = iframe;
             }
 
             ZkUtil.setParentOfChild((Component) base, comp);
             ZkUtil.addBinding(dataBinder, comp, DEFAULT_BEAN_ID, pathMetadata.getPath());
             ((org.zkoss.zul.Panel) base).setBorder(true);
             ((org.zkoss.zul.Panel) base).getPanelchildren().setStyle("overflow: auto;");
             comp.setAttribute(ATTRIB_PATH_META, pathMetadata);
 
             if (comp instanceof HtmlBasedComponent && StringUtils.hasText(getSettingValue(SettingEnum.PANEL_STYLE,
                     ""))) {
                 StringBuilder sb = new StringBuilder();
                 if (StringUtils.hasText(((HtmlBasedComponent) comp).getStyle())) {
                     sb.append(((HtmlBasedComponent) comp).getStyle());
                     if (!sb.toString().endsWith(";")) {
                         sb.append(";");
                     }
                 }
                 sb.append(getSettingValue(SettingEnum.PANEL_STYLE, ""));
                 ((HtmlBasedComponent) comp).setStyle(sb.toString());
             }
         }
     }
 
     @Override
     protected void arrangeForTargetType() {
         super.arrangeForTargetType();
         arrangeForMutableMode();
     }
 
 
     @Override
     protected Class<? extends MutablePanel> getMutableType() {
         return MediaViewPanel.class;
     }
 
     @Override
     protected void processValidCommand(Command command) {
         if (command.getId().equals(CommandEnum.UPDATE)) {
             if (hasTargetEntity()) {
                 PathMetadata pathMetadata = ContextUtil.getMRS().getPropertyPath(getTargetType(),
                         new Path(getSettingValue(SettingEnum.MEDIA_PROPERTY, "")));
 
                 Dialog dialog = ContextUtil.getDefaultDialog(ValueInputDialog.class, pathMetadata,
                        pathMetadata.getValue(getTargetEntity()), true /* #4 */);
                 dialog.show(new DialogListener());
             }
         } else if (command.getId().equals(CommandEnum.ADDNEW)) {
             if (hasTargetType()) {
                 PathMetadata pathMetadata = ContextUtil.getMRS().getPropertyPath(getTargetType(),
                         new Path(getSettingValue(SettingEnum.MEDIA_PROPERTY, "")));
 
                 Dialog dialog = ContextUtil.getDefaultDialog(ValueInputDialog.class, pathMetadata, null);
                 dialog.show(new DialogListener());
             }
         } else {
             super.processValidCommand(command);
         }
     }
 
     private class DialogListener implements MessageListener {
 
         @Override
         public void processMessage(Message message) {
             if (message.getId() == MessageEnum.AFFIRMATIVE_RESPONSE && message.getSender() instanceof
                     ValueInputDialog) {
 
                 Entity entity;
                 MessageArgEnum messageArgEnum;
                 if (((ValueInputDialog) message.getSender()).isEditing()) {
                     entity = getTargetEntity();
                     messageArgEnum = MessageArgEnum.ARG_NEW_ITEM;
                 } else {
                     entity = prepareMutableInstance(MutableMode.INSERT);
                     messageArgEnum = MessageArgEnum.ARG_ITEM;
                 }
 
                 byte[] media = message.getArg(messageArgEnum, byte[].class);
 
                 PathMetadata pathMetadata = ContextUtil.getMRS().getPropertyPath(getTargetType(),
                         new Path(getSettingValue(SettingEnum.MEDIA_PROPERTY, "")));
                 pathMetadata.getLastStep().setValue(entity, media);
 
                 ContextUtil.getDWS().save(entity);
                 setTargetEntity(entity);
             }
         }
     }
 }
