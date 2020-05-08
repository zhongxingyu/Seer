 /*
  * IntelliJ IDEA Jalopy Formatter plugin
  * Copyright (C) 2012  Alexey Hanin <mail@alexeyhanin.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.alexeyhanin.intellij.jalopyplugin.component;
 
 import com.intellij.AppTopics;
 import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
 import com.intellij.openapi.components.ApplicationComponent;
 import com.intellij.openapi.editor.Document;
 import com.intellij.openapi.fileEditor.FileDocumentManagerAdapter;
 import com.intellij.util.messages.MessageBusConnection;
 import org.jetbrains.annotations.NotNull;
 
 public class JalopyPluginRegistration implements ApplicationComponent {
 
     private MessageBusConnection connection;
 
     @Override
     public void initComponent() {
         connection = ApplicationManager.getApplication().getMessageBus().connect();
         connection.subscribe(AppTopics.FILE_DOCUMENT_SYNC, new JalopyDocumentFormatter());
     }
 
     @Override
     public void disposeComponent() {
         connection.disconnect();
     }
 
     @NotNull
     @Override
     public String getComponentName() {
         return getClass().getSimpleName();
     }
 
     private static class JalopyDocumentFormatter extends FileDocumentManagerAdapter {
         @Override
         public void beforeDocumentSaving(@NotNull final Document document) {
             final JalopySettingsComponent settingsComponent = ApplicationManager.getApplication().getComponent(JalopySettingsComponent.class);
             if (settingsComponent != null && settingsComponent.getState().isFormatOnSaveEnabled()) {
                CommandProcessor.getInstance().runUndoTransparentAction(new Runnable() {
                    @Override
                    public void run() {
                        com.alexeyhanin.intellij.jalopyplugin.util.JalopyDocumentFormatter.format(document);
                    }
                });
             }
         }
     }
 }
