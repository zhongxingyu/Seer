 /*
  *  Copyright 2011 Yannick LOTH.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package com.googlecode.wicketelements.components.togglepane;
 
 import com.googlecode.jbp.common.requirements.Reqs;
 import com.googlecode.wicketelements.components.togglepane.ui.TogglePane;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Default implementation of a toggle pane's state.
  */
 public class DefaultTogglePaneState implements TogglePaneState {
     private final List<TogglePaneStateListener> listeners = new ArrayList<TogglePaneStateListener>();
     private final TogglePane togglePane;
 
     public DefaultTogglePaneState(final TogglePane togglePaneParam) {
         Reqs.PARAM_REQ.Object.requireNotNull(togglePaneParam, "Toggle pane must not be null.");
         togglePane = togglePaneParam;
     }
 
     public final void addEventListener(final TogglePaneStateListener togglePaneStateListenerParam) {
         Reqs.PARAM_REQ.Object.requireNotNull(togglePaneStateListenerParam, "The listener to add must not be null.");
         listeners.add(togglePaneStateListenerParam);
     }
 
     /**
      * {@code null is allowed.}
      *
      * @param togglePaneStateListenerParam
      */
     public final void removeEventListener(final TogglePaneStateListener togglePaneStateListenerParam) {
         if (togglePaneStateListenerParam != null) {
             listeners.remove(togglePaneStateListenerParam);
         }
     }
 
     public final void toggleContent() {
         final boolean visibility = togglePane.getContentComponent().isVisible();
         togglePane.getContentComponent().setVisible(!visibility);
 
         //send event to listeners
        final TogglePaneStateEvent event = new TogglePaneStateEvent(togglePane);
         if (visibility) {
             for (final TogglePaneStateListener listener : listeners) {
                 listener.togglePaneCollapsed(event);
             }
         } else {
             for (final TogglePaneStateListener listener : listeners) {
                 listener.togglePaneExpanded(event);
             }
         }
     }
 
     public final void toggleEnableState() {
         final boolean enabled = togglePane.getTitleComponent().isEnabled();
         togglePane.getTitleComponent().setEnabled(!enabled);
 
         //send event to listeners
        final TogglePaneStateEvent event = new TogglePaneStateEvent(togglePane);
         if (enabled) {
             for (final TogglePaneStateListener listener : listeners) {
                 listener.togglePaneDisabled(event);
             }
         } else {
             for (final TogglePaneStateListener listener : listeners) {
                 listener.togglePaneEnabled(event);
             }
         }
     }
 
     public final boolean isCollapsed() {
         return !isExpanded();
     }
 
     public final boolean isExpanded() {
         return togglePane.getContentComponent().isVisible();
     }
 
     public final boolean isEnabled() {
         return togglePane.getContentComponent().isEnabled();
     }
 
     public final boolean isDisabled() {
         return !isEnabled();
     }
 }
 
