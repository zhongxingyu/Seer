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
 
 package org.web4thejob.web.dialog;
 
 import org.springframework.context.annotation.Scope;
 import org.web4thejob.command.Command;
 import org.web4thejob.command.CommandEnum;
 import org.web4thejob.command.CommandProcessingException;
 import org.web4thejob.context.ContextUtil;
 import org.web4thejob.message.Message;
 import org.web4thejob.message.MessageArgEnum;
 import org.web4thejob.message.MessageEnum;
 import org.web4thejob.message.MessageListener;
 import org.web4thejob.orm.PathMetadata;
 import org.web4thejob.util.L10nString;
 import org.web4thejob.web.util.ZkUtil;
 import org.web4thejob.web.zbox.PropertyBox;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zul.Listbox;
 import org.zkoss.zul.Listcell;
 import org.zkoss.zul.Listitem;
 
 import java.util.*;
 
 /**
  * @author Veniamin Isaias
  * @since 1.0.0
  */
 
 @org.springframework.stereotype.Component
 @Scope("prototype")
 public class DefaultValueListDialog extends AbstractDialog implements ValueListDialog, MessageListener {
     public static final L10nString L10N_DIALOG_TITLE = new L10nString(DefaultValueListDialog.class, "dialog_title",
             "Values selection");
 
     private final List<Object> values = new ArrayList<Object>();
     private final PathMetadata pathMetadata;
     private final Listbox listbox = new Listbox();
 
     public DefaultValueListDialog(PathMetadata pathMetadata, List<?> values) {
         this.pathMetadata = pathMetadata;
         if (values != null) {
             this.values.addAll(values);
         }
     }
 
     @Override
     public Set<CommandEnum> getSupportedCommands() {
         Set<CommandEnum> supported = new HashSet<CommandEnum>(2);
         supported.add(CommandEnum.ADD);
         supported.add(CommandEnum.EDIT);
         supported.add(CommandEnum.REMOVE);
         return Collections.unmodifiableSet(supported);
     }
 
 
     @Override
     protected void prepareWindow() {
         super.prepareWindow();
         ZkUtil.sizeComponent(window, 40, 75);
     }
 
     @Override
     protected void prepareContent() {
         Command command = registerCommand(ContextUtil.getDefaultCommand(CommandEnum.ADD, this));
         if (command != null) {
             command.setActivated(true);
         }
         registerCommand(ContextUtil.getDefaultCommand(CommandEnum.EDIT, this));
         registerCommand(ContextUtil.getDefaultCommand(CommandEnum.REMOVE, this));
 
 
         listbox.setParent(dialogContent.getPanelchildren());
         listbox.setVflex("true");
         listbox.setHflex("true");
         listbox.addEventListener(Events.ON_SELECT, this);
 
         for (Object value : values) {
             render(value);
         }
 
         super.prepareContent();
         listbox.setFocus(true);
     }
 
     @Override
     protected String prepareTitle() {
         return L10N_DIALOG_TITLE.toString();
     }
 
     @Override
     public void onEvent(Event event) throws Exception {
         if (event.getName().equals(Events.ON_SELECT) && event.getTarget().equals(listbox)) {
             if (hasCommand(CommandEnum.EDIT)) {
                 getCommand(CommandEnum.EDIT).setActivated(true);
             }
             if (hasCommand(CommandEnum.REMOVE)) {
                 getCommand(CommandEnum.REMOVE).setActivated(true);
             }
         } else {
             super.onEvent(event);
         }
     }
 
     private void render(Object value) {
         Listitem listitem = new Listitem();
         listitem.setParent(listbox);
         listitem.setValue(value);
 
         Listcell listcell = new Listcell();
         listcell.setParent(listitem);
         listcell.setStyle("white-space:nowrap;");
 
         PropertyBox propertyBox = new PropertyBox();
         propertyBox.setParent(listcell);
         propertyBox.setValue(value);
         propertyBox.setHflex("true");
     }
 
     private void editSelected(Object value) {
         Listitem listitem = listbox.getSelectedItem();
         if (listitem != null) {
             listitem.setValue(value);
             ((PropertyBox) listitem.getFirstChild().getFirstChild()).setValue(value);
         }
     }
 
 
     @Override
     public void process(Command command) throws CommandProcessingException {
         ValueInputDialog dialog;
         if (CommandEnum.ADD.equals(command.getId())) {
             dialog = ContextUtil.getDefaultDialog(ValueInputDialog.class, pathMetadata, null);
             dialog.show(this);
         } else if (CommandEnum.EDIT.equals(command.getId())) {
             if (listbox.getSelectedItem() != null) {
                 dialog = ContextUtil.getDefaultDialog(ValueInputDialog.class, pathMetadata,
                        listbox.getSelectedItem().getValue(), true);
                 dialog.show(this);
             }
         } else if (CommandEnum.REMOVE.equals(command.getId())) {
             if (listbox.getSelectedItem() != null) {
                 listbox.removeItemAt(listbox.getSelectedIndex());
 
             }
         }
     }
 
     @Override
     protected Message getOKMessage() {
         List<Object> result = new ArrayList<Object>();
         for (Listitem listitem : listbox.getItems()) {
             result.add(listitem.getValue());
         }
         return ContextUtil.getMessage(MessageEnum.AFFIRMATIVE_RESPONSE, this, MessageArgEnum.ARG_ITEM, result);
     }
 
     @Override
     protected boolean isOKReady() {
         return listbox.getItemCount() > 0;
     }
 
     @Override
     public void processMessage(Message message) {
         if (message.getId() == MessageEnum.AFFIRMATIVE_RESPONSE) {
             if (message.getArgs().containsKey(MessageArgEnum.ARG_NEW_ITEM)) {
                 //className edit
                 editSelected(message.getArg(MessageArgEnum.ARG_NEW_ITEM, Object.class));
             } else {
                 //className insertion
                 render(message.getArg(MessageArgEnum.ARG_ITEM, Object.class));
             }
         }
     }
 }
