 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.teeda.core.lifecycle;
 
 import java.util.Iterator;
 
 import javax.faces.component.EditableValueHolder;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.event.PhaseEvent;
 import javax.faces.event.PhaseId;
 import javax.faces.event.PhaseListener;
 import javax.faces.lifecycle.Lifecycle;
 
 import org.seasar.teeda.core.util.LifecycleUtil;
 
 /**
  * @author shot
  */
 public abstract class AbstractPhase implements Phase {
 
     public final void prePhase(FacesContext context) {
         Lifecycle lifecycle = getLifecycle();
         PhaseListener[] phaseListeners = lifecycle.getPhaseListeners();
         if (phaseListeners != null) {
             for (int i = 0; i < phaseListeners.length; i++) {
                 PhaseListener phaseListener = phaseListeners[i];
                 PhaseId phaseId = getCurrentPhaseId();
                 if (isTargetListener(phaseListener, phaseId)) {
                     PhaseEvent event = createPhaseEvent(context, phaseId,
                             lifecycle);
                     phaseListener.beforePhase(event);
                 }
             }
         }
     }
 
     //If need pre-prePhase or post-prePhase(and also postPhase) action, add method.
     public void execute(FacesContext context) {
         prePhase(context);
        try {
            executePhase(context);
        } finally {
            postPhase(context);
        }
     }
 
     public final void postPhase(FacesContext context) {
         Lifecycle lifecycle = getLifecycle();
         PhaseListener[] phaseListeners = lifecycle.getPhaseListeners();
         if (phaseListeners != null) {
             for (int i = phaseListeners.length - 1; i >= 0; i--) {
                 PhaseListener phaseListener = phaseListeners[i];
                 PhaseId phaseId = getCurrentPhaseId();
                 if (isTargetListener(phaseListener, phaseId)) {
                     PhaseEvent event = createPhaseEvent(context, phaseId,
                             lifecycle);
                     phaseListener.afterPhase(event);
                 }
             }
         }
     }
 
     protected PhaseEvent createPhaseEvent(FacesContext context,
             PhaseId phaseId, Lifecycle lifecycle) {
         return new PhaseEvent(context, phaseId, lifecycle);
     }
 
     protected void initializeChildren(FacesContext context,
             UIComponent component) {
         for (Iterator i = component.getFacetsAndChildren(); i.hasNext();) {
             UIComponent child = (UIComponent) i.next();
             if (child instanceof EditableValueHolder) {
                 EditableValueHolder editableValueHolder = (EditableValueHolder) child;
                 editableValueHolder.setValid(true);
                 editableValueHolder.setSubmittedValue(null);
                 editableValueHolder.setValue(null);
                 editableValueHolder.setLocalValueSet(false);
             }//For each
             initializeChildren(context, child);
         }
     }
 
     protected boolean isTargetListener(PhaseListener listener, PhaseId phaseId) {
         int listenerOrdinal = listener.getPhaseId().getOrdinal();
         return listenerOrdinal == PhaseId.ANY_PHASE.getOrdinal()
                 || listenerOrdinal == phaseId.getOrdinal();
     }
 
     protected final Lifecycle getLifecycle() {
         return LifecycleUtil.getLifecycle();
     }
 
     protected abstract void executePhase(FacesContext context);
 
     protected abstract PhaseId getCurrentPhaseId();
 }
