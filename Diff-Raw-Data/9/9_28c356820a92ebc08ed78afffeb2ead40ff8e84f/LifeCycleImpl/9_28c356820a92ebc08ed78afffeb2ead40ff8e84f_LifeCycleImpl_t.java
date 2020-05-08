 package org.jtheque.core.impl;
 
 import org.jtheque.core.Core;
 import org.jtheque.core.lifecycle.FunctionListener;
 import org.jtheque.core.lifecycle.LifeCycle;
 import org.jtheque.core.lifecycle.TitleListener;
 import org.jtheque.events.EventService;
 import org.jtheque.events.Events;
 import org.jtheque.utils.StringUtils;
 import org.jtheque.utils.SystemProperty;
 import org.jtheque.utils.annotations.GuardedInternally;
 import org.jtheque.utils.annotations.ThreadSafe;
 import org.jtheque.utils.collections.WeakEventListenerList;
 import org.jtheque.utils.io.FileUtils;
 
 import javax.annotation.Resource;
 
 import java.io.File;
 
 /*
  * Copyright JTheque (Baptiste Wicht)
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
 
 /**
  * The lifecycle implementation of the core.
  *
  * @author Baptiste Wicht
  */
 @ThreadSafe
 public final class LifeCycleImpl implements LifeCycle {
     private static final int RESTART_EXIT_CODE = 666;
 
     @GuardedInternally
     private final WeakEventListenerList<FunctionListener> functionListeners = WeakEventListenerList.create();
 
     @GuardedInternally
     private final WeakEventListenerList<TitleListener> titleListeners = WeakEventListenerList.create();
 
     @Resource
     private Core core;
 
     @Resource
     private EventService eventService;
 
     private volatile String currentFunction;
     private volatile String title = "JTheque";
 
     @Override
     public void exit() {
        exit(0);
     }
 
     @Override
     public void restart() {
         FileUtils.clearFolder(new File(SystemProperty.USER_DIR.get() + "cache"));
 
         exit(RESTART_EXIT_CODE);
     }
 
     /**
      * Exit from JTheque with the given exit code.
      *
      * @param code The exit code.
      */
     private void exit(int code) {
         eventService.addEvent(Events.newInfoEvent("User", "events.close", EventService.CORE_EVENT_LOG));
 
         Runtime.getRuntime().exit(code);
     }
 
     @Override
     public void setCurrentFunction(String function) {
         currentFunction = function;
 
         fireFunctionUpdated(function);
 
         refreshTitle();
     }
 
     @Override
     public String getCurrentFunction() {
         return currentFunction;
     }
 
     @Override
     public String getTitle() {
         return title;
     }
 
     @Override
     public void addTitleListener(TitleListener listener) {
         titleListeners.add(listener);
     }
 
     @Override
     public void removeTitleListener(TitleListener listener) {
         titleListeners.remove(listener);
     }
 
     @Override
     public void addFunctionListener(FunctionListener listener) {
         functionListeners.add(listener);
     }
 
     @Override
     public void removeFunctionListener(FunctionListener listener) {
         functionListeners.remove(listener);
     }
 
     @Override
     public void refreshTitle() {
         updateTitle();
 
         fireTitleUpdated(title);
     }
 
     /**
      * Update the title.
      */
     private void updateTitle() {
         StringBuilder builder = new StringBuilder(50);
 
         if (StringUtils.isNotEmpty(currentFunction)) {
             builder.append(currentFunction);
             builder.append(" - ");
         }
 
         builder.append(core.getApplication().getI18nProperties().getName());
         builder.append(' ');
         builder.append(core.getApplication().getVersion());
 
         title = builder.toString();
     }
 
     /**
      * Fire a titleUpdated event. This method avert the listeners that the title of the application has changed.
      *
      * @param title The new title.
      */
     private void fireTitleUpdated(String title) {
         for (TitleListener listener : titleListeners) {
             listener.titleUpdated(title);
         }
     }
 
     /**
      * Fire a functionUpdated event. This method avert the listeners that the current function has changed.
      *
      * @param function The new function.
      */
     private void fireFunctionUpdated(String function) {
         for (FunctionListener listener : functionListeners) {
             listener.functionUpdated(function);
         }
     }
 }
