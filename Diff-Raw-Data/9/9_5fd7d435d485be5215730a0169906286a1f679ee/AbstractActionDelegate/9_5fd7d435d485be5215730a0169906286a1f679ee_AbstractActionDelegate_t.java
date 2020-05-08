 /*******************************************************************************
 * Copyright (c) 2009, 2013 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.ui.commands;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.HandlerEvent;
 import org.eclipse.core.commands.IHandler2;
 import org.eclipse.core.commands.IHandlerListener;
 import org.eclipse.core.commands.common.EventManager;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.tcf.internal.debug.model.TCFLaunch;
 import org.eclipse.tcf.internal.debug.ui.model.TCFModelManager;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
 import org.eclipse.ui.IActionDelegate2;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IViewActionDelegate;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowActionDelegate;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 public abstract class AbstractActionDelegate extends EventManager implements
         IViewActionDelegate, IActionDelegate2, IWorkbenchWindowActionDelegate,
         IObjectActionDelegate, IHandler2 {
 
     private IAction action;
     private IWorkbenchPart part;
     private IWorkbenchWindow window;
     private ExecutionEvent event;
     private ISelection selection;
     private ISelection event_selection;
     @SuppressWarnings("unused")
     private Object context;
     private boolean enabled;
 
     public void init(IAction action) {
         this.action = action;
     }
 
     public void init(IViewPart view) {
         this.part = view;
     }
 
     public void init(IWorkbenchWindow window) {
         this.window = window;
     }
 
     public void dispose() {
         action = null;
         part = null;
         window = null;
     }
 
     public void addHandlerListener(IHandlerListener listener) {
         addListenerObject(listener);
     }
 
     public void removeHandlerListener(IHandlerListener listener) {
         removeListenerObject(listener);
     }
 
     protected void fireHandlerChanged(HandlerEvent event) {
         if (event == null) throw new NullPointerException();
 
         Object[] listeners = getListeners();
         for (int i = 0; i < listeners.length; i++) {
             IHandlerListener listener = (IHandlerListener)listeners[i];
             listener.handlerChanged(event);
         }
     }
 
     public void setActivePart(IAction action, IWorkbenchPart part) {
         this.action = action;
         this.part = part;
         window = part.getSite().getWorkbenchWindow();
     }
 
     public void run(IAction action) {
         IAction action0 = this.action;
         try {
             this.action = action;
             run();
         }
         finally {
             this.action = action0;
         }
     }
 
     public void runWithEvent(IAction action, Event event) {
         run(action);
     }
 
     public void selectionChanged(IAction action, ISelection selection) {
         this.selection = selection;
         IAction action0 = this.action;
         try {
             this.action = action;
             selectionChanged();
         }
         finally {
             this.action = action0;
         }
     }
 
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
         if (action != null) action.setEnabled(enabled);
     }
 
     public void setEnabled(Object context) {
         this.context = context;
     }
 
     public boolean isEnabled() {
         window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
         if (window == null) return false;
         part = window.getActivePage().getActivePart();
         if (part == null) return false;
        ISelectionProvider selection_provider = part.getSite().getSelectionProvider();
        if (selection_provider == null) return false;
        selection = selection_provider.getSelection();
         if (selection == null) return false;
         selectionChanged();
         return enabled;
     }
 
     public IWorkbenchPart getPart() {
         if (event != null) return HandlerUtil.getActivePart(event);
         return part;
     }
 
     public IWorkbenchWindow getWindow() {
         if (event != null) return HandlerUtil.getActiveWorkbenchWindow(event);
         if (part != null) return part.getSite().getWorkbenchWindow();
         if (window != null) return window;
         return null;
     }
 
     public ISelection getSelection() {
         if (event != null) return event_selection;
         return selection;
     }
 
     public Object execute(ExecutionEvent event) throws ExecutionException {
         try {
             this.event = event;
             event_selection = selection;
             run();
         }
         catch (Throwable x) {
             throw new ExecutionException("Command aborted", x);
         }
         finally {
             this.event = null;
             event_selection = null;
         }
         return null;
     }
 
     public boolean isHandled() {
         return true;
     }
 
     public TCFNode getSelectedNode() {
         ISelection s = getSelection();
         if (s instanceof IStructuredSelection) {
             final Object o = ((IStructuredSelection)s).getFirstElement();
             if (o instanceof TCFNode) return (TCFNode)o;
             if (o instanceof TCFLaunch) return TCFModelManager.getRootNodeSync((TCFLaunch)o);
         }
         return null;
     }
 
     public TCFNode[] getSelectedNodes() {
         ISelection s0 = getSelection();
         ArrayList<TCFNode> list = new ArrayList<TCFNode>();
         if (s0 instanceof IStructuredSelection) {
             IStructuredSelection s = (IStructuredSelection)s0;
             if (s.size() > 0) {
                 for (final Object o : s.toArray()) {
                     if (o instanceof TCFNode) {
                         list.add((TCFNode)o);
                     }
                     else if (o instanceof TCFLaunch) {
                         TCFNode n = TCFModelManager.getRootNodeSync((TCFLaunch)o);
                         if (n != null) list.add(n);
                     }
                 }
             }
         }
         return list.toArray(new TCFNode[list.size()]);
     }
 
     protected abstract void selectionChanged();
 
     protected abstract void run();
 }
