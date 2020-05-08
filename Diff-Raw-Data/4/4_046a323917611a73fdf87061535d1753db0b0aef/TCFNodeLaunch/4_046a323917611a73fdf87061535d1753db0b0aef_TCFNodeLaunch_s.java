 /*******************************************************************************
  * Copyright (c) 2007, 2014 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.ui.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
 import org.eclipse.debug.ui.IDebugUIConstants;
 import org.eclipse.tcf.debug.ui.ITCFDebugUIConstants;
 import org.eclipse.tcf.services.IMemory;
 import org.eclipse.tcf.services.IRunControl;
 
 
 public class TCFNodeLaunch extends TCFNode implements ISymbolOwner {
 
     private final TCFChildrenExecContext children;
 
     private final TCFChildren filtered_children;
     private final TCFChildrenContextQuery children_query;
     private final Map<String,TCFNodeSymbol> symbols = new HashMap<String,TCFNodeSymbol>();
     private final Set<String> auto_filter = new HashSet<String>();
 
     TCFNodeLaunch(final TCFModel model) {
         super(model);
         children = new TCFChildrenExecContext(this);
         children_query = new TCFChildrenContextQuery(this);
         filtered_children = new TCFChildren(this) {
             @Override
             protected boolean startDataRetrieval() {
                 Set<String> filter = launch.getContextFilter();
                 if (filter == null) {
                    if (!children.validate(this)) return false;
                     set(null, children.getError(), children.getData());
                     return true;
                 }
                 Runnable done = new Runnable() {
                     @Override
                     public void run() {
                         filtered_children.post();
                     }
                 };
                 Map<String,TCFNode> nodes = new HashMap<String,TCFNode>();
                 for (String id : filter) {
                     if (!model.createNode(id, done)) return false;
                     TCFNode node = model.getNode(id);
                     if (node != null) nodes.put(id, node);
                 }
                 set(null, null, nodes);
                 return true;
             }
             @Override
             public void dispose() {
                 getNodes().clear();
                 super.dispose();
             }
         };
     }
 
     @Override
     void dispose() {
         ArrayList<TCFNodeSymbol> l = new ArrayList<TCFNodeSymbol>(symbols.values());
         for (TCFNodeSymbol s : l) s.dispose();
         assert symbols.size() == 0;
         super.dispose();
     }
 
     @Override
     protected boolean getData(IChildrenCountUpdate result, Runnable done) {
         String view_id = result.getPresentationContext().getId();
         if (IDebugUIConstants.ID_DEBUG_VIEW.equals(view_id)) {
             if (!filtered_children.validate(done)) return false;
             result.setChildCount(filtered_children.size());
         }
         else if (ITCFDebugUIConstants.ID_CONTEXT_QUERY_VIEW.equals(view_id)) {
             if (!children_query.setQuery(result, done)) return false;
             if (!children_query.validate(done)) return false;
             result.setChildCount(children_query.size());
         }
         else {
             result.setChildCount(0);
         }
         return true;
     }
 
     @Override
     protected boolean getData(IChildrenUpdate result, Runnable done) {
         String view_id = result.getPresentationContext().getId();
         if (IDebugUIConstants.ID_DEBUG_VIEW.equals(view_id)) {
             return filtered_children.getData(result, done);
         }
         else if (ITCFDebugUIConstants.ID_CONTEXT_QUERY_VIEW.equals(view_id)) {
             if (!children_query.setQuery(result, done)) return false;
             return children_query.getData(result, done);
         }
         return true;
     }
 
     @Override
     protected boolean getData(IHasChildrenUpdate result, Runnable done) {
         String view_id = result.getPresentationContext().getId();
         if (IDebugUIConstants.ID_DEBUG_VIEW.equals(view_id)) {
             if (!filtered_children.validate(done)) return false;
             result.setHasChilren(filtered_children.size() > 0);
         }
         else if (ITCFDebugUIConstants.ID_CONTEXT_QUERY_VIEW.equals(view_id)) {
             if (!children_query.setQuery(result, done)) return false;
             if (!children_query.validate(done)) return false;
             result.setHasChilren(children_query.size() > 0);
         }
         else {
             result.setHasChilren(false);
         }
         return true;
     }
 
     void onContextAdded(IRunControl.RunControlContext context) {
         Set<String> filter = launch.getContextFilter();
         if (filter != null) {
             String c = context.getCreatorID();
             while (c != null) {
                 if (filter.contains(c)) {
                     auto_filter.add(context.getID());
                     filter.add(context.getID());
                     break;
                 }
                 Object o = model.getContextMap().get(c);
                 if (o instanceof IRunControl.RunControlContext) {
                     c = ((IRunControl.RunControlContext)o).getParentID();
                 }
                 else {
                     break;
                 }
             }
         }
         model.setDebugViewSelection(this, TCFModel.SELECT_ADDED);
         children.onContextAdded(context);
     }
 
     void onContextAdded(IMemory.MemoryContext context) {
         children.onContextAdded(context);
     }
 
     void onContextRemoved(String id) {
         if (auto_filter.remove(id)) {
             Set<String> filter = launch.getContextFilter();
             if (filter != null) filter.remove(id);
         }
     }
 
     void onAnyContextSuspendedOrChanged() {
         for (TCFNodeSymbol s : symbols.values()) s.onMemoryMapChanged();
     }
 
     void onAnyContextAddedOrRemoved() {
         filtered_children.reset();
     }
 
     public void addSymbol(TCFNodeSymbol s) {
         assert symbols.get(s.id) == null;
         symbols.put(s.id, s);
     }
 
     public void removeSymbol(TCFNodeSymbol s) {
         assert symbols.get(s.id) == s;
         symbols.remove(s.id);
     }
 
     public TCFChildren getChildren() {
         return children;
     }
 
     public TCFChildren getFilteredChildren() {
         return filtered_children;
     }
 }
