 /*******************************************************************************
  * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.ui.model;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.debug.internal.ui.viewers.model.IInternalTreeModelViewer;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
 import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
 import org.eclipse.debug.ui.IDebugUIConstants;
 import org.eclipse.jface.viewers.ITreeViewerListener;
 import org.eclipse.jface.viewers.TreeExpansionEvent;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.tcf.internal.debug.model.TCFLaunch;
 import org.eclipse.tcf.protocol.Protocol;
 
 /**
  * A model proxy represents a model for a specific presentation context and
  * fires deltas to notify listeners of changes in the model.
  * Model proxy listeners are debuggers views.
  */
 @SuppressWarnings("restriction")
 public class TCFModelProxy extends AbstractModelProxy implements IModelProxy, Runnable, ITreeViewerListener {
 
     private static final TCFNode[] EMPTY_NODE_ARRAY = new TCFNode[0];
     private static boolean is_linux = "Linux".equals(System.getProperty("os.name"));
     private final TCFModel model;
     private final TCFLaunch launch;
     private final Display display;
     private final Map<TCFNode,Integer> node2flags = new HashMap<TCFNode,Integer>();
     private final Map<TCFNode,TCFNode[]> node2children = new HashMap<TCFNode,TCFNode[]>();
     private final Map<TCFNode,ModelDelta> node2delta = new HashMap<TCFNode,ModelDelta>();
     private final Set<ModelDelta> content_deltas = new HashSet<ModelDelta>();
     private final LinkedList<TCFNode> selection = new LinkedList<TCFNode>();
     private final Set<String> auto_expand_set = new HashSet<String>();
     private Map<String, Boolean> expanded_nodes = Collections.synchronizedMap(new TreeMap<String, Boolean>());
 
     private ITreeModelViewer viewer;
     private boolean posted;
     private boolean installed;
     private boolean disposed;
     private boolean realized;
     private long last_update_time;
     private boolean enable_auto_expand;
     private Set<TCFNode> auto_expand_removed_nodes;
     private Set<TCFNode> auto_expand_created_nodes;
 
     private final Runnable timer = new Runnable() {
 
         @Override
         public void run() {
             posted = false;
             if (pending_node != null) return;
             long idle_time = System.currentTimeMillis() - last_update_time;
             long min_idle_time = model.getMinViewUpdatesInterval();
             if (model.getViewUpdatesThrottleEnabled()) {
                 int congestion = Protocol.getCongestionLevel() + 50;
                 if (congestion > 0) min_idle_time += congestion * 10;
             }
             if (model.getChannelThrottleEnabled()) {
                 int congestion = model.getChannel().getCongestion() + 50;
                 if (congestion > 0) min_idle_time += congestion * 10;
             }
             if (idle_time < min_idle_time - 5) {
                 Protocol.invokeLater(min_idle_time - idle_time, this);
                 posted = true;
             }
             else {
                 TCFModelProxy.this.run();
             }
         }
     };
 
     private class ViewerUpdate implements IViewerUpdate {
 
         IStatus status;
 
         @Override
         public Object getElement() {
             return null;
         }
 
         @Override
         public TreePath getElementPath() {
             return null;
         }
 
         @Override
         public IPresentationContext getPresentationContext() {
             return TCFModelProxy.this.getPresentationContext();
         }
 
         @Override
         public Object getViewerInput() {
             return TCFModelProxy.this.getInput();
         }
 
         @Override
         public void cancel() {
         }
 
         @Override
         public void done() {
         }
 
         @Override
         public IStatus getStatus() {
             return status;
         }
 
         @Override
         public boolean isCanceled() {
             return false;
         }
 
         @Override
         public void setStatus(IStatus status) {
             this.status = status;
         }
     }
 
     private class ChildrenCountUpdate extends ViewerUpdate implements IChildrenCountUpdate {
 
         int count;
 
         @Override
         public void setChildCount(int count) {
             this.count = count;
         }
     }
 
     private class ChildrenUpdate extends ViewerUpdate implements IChildrenUpdate {
 
         int length;
         TCFNode[] children;
 
         void setLength(int length) {
             this.length = length;
             this.children = length == 0 ? EMPTY_NODE_ARRAY : new TCFNode[length];
         }
 
         @Override
         public int getLength() {
             return length;
         }
 
         @Override
         public int getOffset() {
             return 0;
         }
 
         @Override
         public void setChild(Object child, int offset) {
             children[offset] = (TCFNode)child;
         }
     }
 
     private final IViewerUpdateListener update_listener = new IViewerUpdateListener() {
 
         @Override
         public void viewerUpdatesBegin() {
             if (!model.getWaitForViewsUpdateAfterStep()) return;
             launch.addPendingClient(this);
         }
 
         @Override
         public void viewerUpdatesComplete() {
             launch.removePendingClient(this);
         }
 
         @Override
         public void updateStarted(IViewerUpdate update) {
         }
 
         @Override
         public void updateComplete(IViewerUpdate update) {
         }
     };
 
     private final ChildrenCountUpdate children_count_update = new ChildrenCountUpdate();
     private final ChildrenUpdate children_update = new ChildrenUpdate();
 
     private TCFNode pending_node;
 
     TCFModelProxy(TCFModel model) {
         this.model = model;
         launch = model.getLaunch();
         display = model.getDisplay();
     }
 
     @Override
     public void initialize(ITreeModelViewer viewer) {
         if (isDisposed()) return;
         this.viewer = viewer;
         super.initialize(viewer);
         enable_auto_expand =
                 IDebugUIConstants.ID_DEBUG_VIEW.equals(getPresentationContext().getId()) &&
                 viewer instanceof IInternalTreeModelViewer;
         viewer.addViewerUpdateListener(update_listener);
         if (is_linux && viewer instanceof TreeViewer) {
             ((TreeViewer)viewer).addTreeListener(this);
         }
         Protocol.invokeAndWait(new Runnable() {
             @Override
             public void run() {
                 assert !installed;
                 assert !disposed;
                 model.onProxyInstalled(TCFModelProxy.this);
                 installed = true;
             }
         });
     }
 
     @Override
     public void dispose() {
         if (isDisposed()) return;
         Protocol.invokeAndWait(new Runnable() {
             @Override
             public void run() {
                 assert installed;
                 assert !disposed;
                 model.onProxyDisposed(TCFModelProxy.this);
                 launch.removePendingClient(update_listener);
                 launch.removePendingClient(TCFModelProxy.this);
                 disposed = true;
             }
         });
         viewer.removeViewerUpdateListener(update_listener);
         if (is_linux && viewer instanceof TreeViewer) {
             ((TreeViewer)viewer).removeTreeListener(this);
         }
         super.dispose();
     }
 
     /**
      * Add model change information (delta) to a buffer of pending deltas.
      * Implementation will coalesce and post deltas to the view.
      * @param node - a model node that changed.
      * @param flags - flags that describe the change, see IModelDelta
      */
     public void addDelta(TCFNode node, int flags) {
         assert Protocol.isDispatchThread();
         assert installed && !disposed;
         if (flags == 0) return;
         Integer delta = node2flags.get(node);
         if (delta != null) flags |= delta.intValue();
         node2flags.put(node, flags);
         post();
     }
 
     /**
      * Request node to be expanded in the view.
      * @param node - a model node that will become expanded.
      */
     public void expand(TCFNode node) {
         Object input = getInput();
         IPresentationContext ctx = getPresentationContext();
         while (node != null && node != input) {
             addDelta(node, IModelDelta.EXPAND);
             node = node.getParent(ctx);
         }
         post();
     }
 
     /**
      * Save expansion state for a node that is about to be deleted.
      * The data is used to auto-expand the node if it is re-created later.
      * @param node - a model node that will become expanded.
      */
     void saveExpandState(TCFNode node) {
         if (!enable_auto_expand) return;
         if (auto_expand_removed_nodes == null) auto_expand_removed_nodes = new HashSet<TCFNode>();
         auto_expand_removed_nodes.add(node);
     }
 
     /**
      * Request view selection to be set to given node.
      * @param node - a model node that will become new selection.
      */
     void setSelection(TCFNode node) {
         if (selection.size() > 0 && selection.getLast() == node) return;
         selection.add(node);
         expand(node.getParent(getPresentationContext()));
     }
 
     /**
      * Returns true if node should be be expanded upon the first suspended event.
      * If the given context ID is seen for the first time, the node should be
      * expanded unless the event was caused by user request.  In the latter case
      * the node should not be expanded.
      * <p>
      * Note: As a workaround for bug 208939 on Linux, the auto-expansion is
      * enabled even after the first suspend event.  User collapse/expand actions
      * are tracked to determine whether a given node should be expanded.
      * </p>
      * @param id Id of execution node to check.
      * @param user_request Flag whether the state is requested in response
      * to a user-requested suspend event.
      */
     boolean getAutoExpandNode(String id, boolean user_request) {
         Boolean expand = null;
         synchronized(expanded_nodes) {
             expand = id != null ? expanded_nodes.get(id) : null;
             if (expand == null) {
                 if (user_request) {
                     expand = Boolean.FALSE;
                 }
                 else {
                     expand = Boolean.TRUE;
                    expanded_nodes.put(id, is_linux);
                 }
             }
         }
         return expand;
     }
 
     /**
      * Clear auto-expand info when a node is removed.
      */
     void clearAutoExpandStack(String id) {
        expanded_nodes.remove(id) ;
     }
 
     /**
      * Get current value of the view input.
      * @return view input object.
      */
     Object getInput() {
         return viewer.getInput();
     }
 
     public void post() {
         assert Protocol.isDispatchThread();
         assert installed && !disposed;
         if (!posted && pending_node == null) {
             long idle_time = System.currentTimeMillis() - last_update_time;
             Protocol.invokeLater(model.getMinViewUpdatesInterval() - idle_time, timer);
             if (model.getWaitForViewsUpdateAfterStep()) launch.addPendingClient(this);
             posted = true;
         }
     }
 
     private TCFNode[] getNodeChildren(TCFNode node) {
         TCFNode[] res = node2children.get(node);
         if (res == null) {
             if (node.isDisposed()) {
                 res = EMPTY_NODE_ARRAY;
             }
             else if (!node.getLockedData(children_count_update, null)) {
                 pending_node = node;
                 res = EMPTY_NODE_ARRAY;
             }
             else {
                 children_update.setLength(children_count_update.count);
                 if (!node.getLockedData(children_update, null)) {
                     assert false;
                     pending_node = node;
                     res = EMPTY_NODE_ARRAY;
                 }
                 else {
                     res = children_update.children;
                 }
             }
             node2children.put(node, res);
         }
         return res;
     }
 
     private int getNodeIndex(TCFNode node) {
         TCFNode p = node.getParent(getPresentationContext());
         if (p == null) return -1;
         TCFNode[] arr = getNodeChildren(p);
         for (int i = 0; i < arr.length; i++) {
             if (arr[i] == node) return i;
         }
         return -1;
     }
 
     private ModelDelta makeDelta(ModelDelta root, TCFNode node, TCFNode selection) {
         ModelDelta delta = node2delta.get(node);
         if (delta == null) {
             if (node == root.getElement()) {
                 delta = root;
             }
             else {
                 int flags = 0;
                 Integer flags_obj = node2flags.get(node);
                 if (flags_obj != null) flags = flags_obj.intValue();
                 if ((flags & IModelDelta.REMOVED) != 0 && (flags & (IModelDelta.INSERTED|IModelDelta.ADDED)) != 0) return null;
                 if (node == selection) {
                     // Bug in Eclipse 3.6.1: SELECT delta has no effect without STATE
                     flags |= IModelDelta.SELECT | IModelDelta.STATE;
                     if (this.selection.size() <= 1) flags |= IModelDelta.REVEAL;
                 }
                 if (auto_expand_set.contains(node.id) && getNodeChildren(node).length > 0) {
                     if (auto_expand_created_nodes == null) auto_expand_created_nodes = new HashSet<TCFNode>();
                     auto_expand_created_nodes.add(node);
                 }
                 if (node.parent == null) {
                     // The node is TCF launch node
                     if (root.getElement() instanceof TCFNode) return null;
                     int children = -1;
                     if (selection != null && selection != node || (flags & IModelDelta.EXPAND) != 0) {
                         children = getNodeChildren(node).length;
                     }
                     delta = root.addNode(launch, -1, flags, children);
                 }
                 else {
                     TCFNode parent = node.getParent(getPresentationContext());
                     if (parent == null) return null;
                     ModelDelta up = makeDelta(root, parent, selection);
                     if (up == null) return null;
                     boolean content = content_deltas.contains(up);
                     if (content) {
                         assert selection == null;
                         flags &= ~(IModelDelta.ADDED | IModelDelta.REMOVED |
                                         IModelDelta.REPLACED | IModelDelta.INSERTED |
                                         IModelDelta.CONTENT | IModelDelta.STATE);
                         if (flags == 0) return null;
                     }
                     int index = -1;
                     int children = -1;
                     if (selection != null || (flags & IModelDelta.INSERTED) != 0 || (flags & IModelDelta.EXPAND) != 0) {
                         index = getNodeIndex(node);
                     }
                     if (selection != null && selection != node || (flags & IModelDelta.EXPAND) != 0) {
                         children = getNodeChildren(node).length;
                     }
                     delta = up.addNode(node, index, flags, children);
                     if (content) content_deltas.add(delta);
                 }
                 node2delta.put(node, delta);
             }
         }
         int flags = delta.getFlags();
         if ((flags & IModelDelta.REMOVED) != 0) return null;
         //if ((flags & IModelDelta.CONTENT) != 0 && (flags & IModelDelta.EXPAND) == 0) return null;
         return delta;
     }
 
     private void asyncExec(Runnable r) {
         synchronized (Device.class) {
             if (!display.isDisposed()) {
                 display.asyncExec(r);
             }
         }
     }
 
     private final Comparator<IModelDelta> delta_comparator = new Comparator<IModelDelta>() {
         @Override
         public int compare(IModelDelta o1, IModelDelta o2) {
             int f1 = o1.getFlags();
             int f2 = o2.getFlags();
             if ((f1 & IModelDelta.REMOVED) != 0 && (f2 & IModelDelta.REMOVED) == 0) return -1;
             if ((f1 & IModelDelta.REMOVED) == 0 && (f2 & IModelDelta.REMOVED) != 0) return +1;
             if ((f1 & IModelDelta.ADDED) != 0 && (f2 & IModelDelta.ADDED) == 0) return -1;
             if ((f1 & IModelDelta.ADDED) == 0 && (f2 & IModelDelta.ADDED) != 0) return +1;
             if ((f1 & IModelDelta.INSERTED) != 0 && (f2 & IModelDelta.INSERTED) == 0) return -1;
             if ((f1 & IModelDelta.INSERTED) == 0 && (f2 & IModelDelta.INSERTED) != 0) return +1;
             int i1 = o1.getIndex();
             int i2 = o2.getIndex();
             if (i1 < i2) return -1;
             if (i1 > i2) return +1;
             return 0;
         }
     };
 
     private void sortDeltaChildren(IModelDelta delta) {
         IModelDelta arr[] = delta.getChildDeltas();
         Arrays.sort(arr, delta_comparator);
         for (IModelDelta d : arr) sortDeltaChildren(d);
     }
 
     private void postDelta(final ModelDelta root) {
         assert pending_node == null;
         if (root.getFlags() != 0 || root.getChildDeltas().length > 0) {
             last_update_time = System.currentTimeMillis();
             final Set<TCFNode> save_expand_state = auto_expand_removed_nodes;
             auto_expand_removed_nodes = null;
             asyncExec(new Runnable() {
                 @Override
                 public void run() {
                     if (save_expand_state != null && save_expand_state.size() > 0) {
                         if (viewer instanceof IInternalTreeModelViewer) {
                             IInternalTreeModelViewer tree_viwer = (IInternalTreeModelViewer)viewer;
                             final Set<String> expanded = new HashSet<String>();
                             for (TCFNode node : save_expand_state) {
                                 if (tree_viwer.getExpandedState(node)) expanded.add(node.id);
                             }
                             if (expanded.size() > 0) {
                                 Protocol.invokeLater(new Runnable() {
                                     @Override
                                     public void run() {
                                         auto_expand_set.addAll(expanded);
                                     }
                                 });
                             }
                         }
                     }
                     sortDeltaChildren(root);
                     fireModelChanged(root);
                 }
             });
         }
     }
 
     private void postDelta() {
         assert Protocol.isDispatchThread();
         if (disposed) return;
         if (node2flags.isEmpty() && selection.isEmpty()) return;
         if (!realized) {
             if (getPresentationContext().getId().equals(IDebugUIConstants.ID_DEBUG_VIEW)) {
                 // Wait until launch manager done creating our launch item in the Debug view.
                 // Deltas do NOT work without the launch item.
                 asyncExec(new Runnable() {
                     boolean found;
                     @Override
                     public void run() {
                         if (viewer instanceof IInternalTreeModelViewer) {
                             found = ((IInternalTreeModelViewer)viewer).findElementIndex(TreePath.EMPTY, launch) >= 0;
                         }
                         Protocol.invokeLater(new Runnable() {
                             @Override
                             public void run() {
                                 if (disposed) return;
                                 if (found) realized = true;
                                 else last_update_time = System.currentTimeMillis() + 20;
                                 post();
                             }
                         });
                     }
                 });
                 return;
             }
             else {
                 realized = true;
             }
         }
         Object input = getInput();
         int flags = 0;
         if (input instanceof TCFNode) {
             // Optimize away STATE delta on a view input node
             TCFNode node = (TCFNode)input;
             Integer i = node2flags.get(node);
             if (i != null) {
                 flags = i;
                 if ((flags & IModelDelta.STATE) != 0) {
                     flags &= ~IModelDelta.STATE;
                     if (flags == 0) {
                         node2flags.remove(node);
                         if (node2flags.isEmpty() && selection.isEmpty()) return;
                     }
                     else {
                         node2flags.put(node, flags);
                     }
                 }
             }
         }
         pending_node = null;
         node2children.clear();
         if (flags != 0 || node2flags.size() > 0) {
             node2delta.clear();
             content_deltas.clear();
             ModelDelta root = new ModelDelta(input, flags);
             if ((flags & IModelDelta.CONTENT) != 0) content_deltas.add(root);
             for (TCFNode node : node2flags.keySet()) makeDelta(root, node, null);
             if (pending_node == null) {
                 node2flags.clear();
                 postDelta(root);
             }
         }
         node2delta.clear();
         content_deltas.clear();
         if (pending_node == null) {
             while (!selection.isEmpty()) {
                 TCFNode node = selection.getFirst();
                 if (!node.isDisposed()) {
                     ModelDelta root = new ModelDelta(input, IModelDelta.NO_CHANGE);
                     makeDelta(root, node, node);
                     node2delta.clear();
                     content_deltas.clear();
                     if (pending_node != null) break;
                     postDelta(root);
                 }
                 selection.remove(node);
             }
         }
 
         if (pending_node == null) {
             if (auto_expand_created_nodes != null) {
                 for (TCFNode node : auto_expand_created_nodes) {
                     auto_expand_set.remove(node.id);
                     addDelta(node, IModelDelta.EXPAND);
                 }
                 auto_expand_created_nodes = null;
                 post();
             }
         }
         else if (pending_node.getLockedData(children_count_update, this)) {
             assert false;
             Protocol.invokeLater(this);
         }
         node2children.clear();
     }
 
     @Override
     public void run() {
         postDelta();
         if (!posted && pending_node == null) {
             launch.removePendingClient(this);
         }
     }
 
     @Override
     public void treeCollapsed(TreeExpansionEvent event) {
         updateExpandStack(event, false);
     }
 
     @Override
     public void treeExpanded(TreeExpansionEvent event) {
         updateExpandStack(event, true);
     }
 
     private void updateExpandStack(TreeExpansionEvent event, final boolean expand) {
         if (event.getElement() instanceof TCFNodeExecContext) {
             TCFNodeExecContext node = (TCFNodeExecContext)event.getElement();
             if (model == node.getModel()) expanded_nodes.put(node.id, expand);
         }
     }
 }
