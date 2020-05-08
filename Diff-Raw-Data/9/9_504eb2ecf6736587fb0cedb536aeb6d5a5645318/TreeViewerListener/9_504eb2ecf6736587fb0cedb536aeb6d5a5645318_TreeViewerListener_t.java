 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.processes.ui.navigator.events;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.viewers.ITreeViewerListener;
 import org.eclipse.jface.viewers.TreeExpansionEvent;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.runtime.events.ChangeEvent;
 import org.eclipse.tcf.te.runtime.events.EventManager;
 import org.eclipse.tcf.te.runtime.model.PendingOperationModelNode;
 import org.eclipse.tcf.te.runtime.model.interfaces.IContainerModelNode;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx.QueryState;
 import org.eclipse.tcf.te.runtime.model.interfaces.contexts.IAsyncRefreshableCtx.QueryType;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.IModel;
 import org.eclipse.tcf.te.tcf.core.model.interfaces.services.IModelRefreshService;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode;
 import org.eclipse.tcf.te.tcf.processes.core.model.nodes.PendingOperationNode;
 
 
 /**
  * Tree listener implementation.
  */
 public class TreeViewerListener implements ITreeViewerListener {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITreeViewerListener#treeCollapsed(org.eclipse.jface.viewers.TreeExpansionEvent)
 	 */
     @Override
     public void treeCollapsed(TreeExpansionEvent event) {
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITreeViewerListener#treeExpanded(org.eclipse.jface.viewers.TreeExpansionEvent)
 	 */
     @Override
     public void treeExpanded(TreeExpansionEvent event) {
     	// Get the expanded element
     	Object element = event.getElement();
     	if (element instanceof IProcessContextNode) {
     		final IProcessContextNode node = (IProcessContextNode)element;
 
     		// Flag that tells if the node shall be refreshed
     		boolean needsRefresh = false;
 
     		// Get the asynchronous refresh context adapter
     		final IAsyncRefreshableCtx refreshable = (IAsyncRefreshableCtx)node.getAdapter(IAsyncRefreshableCtx.class);
     		Assert.isNotNull(refreshable);
     		// The node needs to be refreshed if the child list query is not done
     		if (refreshable.getQueryState(QueryType.CHILD_LIST).equals(QueryState.PENDING)) {
     			needsRefresh = true;
     		}
     		else if (refreshable.getQueryState(QueryType.CHILD_LIST).equals(QueryState.DONE)) {
     			// Our policy is that the current node and it's level 1 children are always
     			// fully refreshed. The child list query for the current node is not pending,
     			// so check the children nodes if they need a refresh
     			for (final IProcessContextNode candidate : node.getChildren(IProcessContextNode.class)) {
     	    		// Get the asynchronous refresh context adapter
     	    		final IAsyncRefreshableCtx r = (IAsyncRefreshableCtx)candidate.getAdapter(IAsyncRefreshableCtx.class);
     	    		Assert.isNotNull(r);
     				// If the child list query state is still pending, set the flag and break out of the loop
     	    		if (r.getQueryState(QueryType.CHILD_LIST).equals(QueryState.PENDING)) {
     	    			needsRefresh = true;
     	    			break;
     	    		}
     			}
     		}
 
     		// If the node needs to be refreshed, refresh it now.
     		if (needsRefresh) {
     			// Mark the refresh as in progress
     			refreshable.setQueryState(QueryType.CHILD_LIST, QueryState.IN_PROGRESS);
     			// Create a new pending operation node and associate it with the refreshable
     			PendingOperationModelNode pendingNode = new PendingOperationNode();
     			pendingNode.setParent(node);
     			refreshable.setPendingOperationNode(pendingNode);
 
     			Runnable runnable = new Runnable() {
     				@Override
     				public void run() {
    					// Trigger a refresh of the view content.
    					ChangeEvent ev = new ChangeEvent(node, IContainerModelNode.NOTIFY_CHANGED, null, null);
    					EventManager.getInstance().fireEvent(ev);

    					// Get the parent model of the node
     					IModel model = node.getParent(IModel.class);
     					Assert.isNotNull(model);
 
     					// Don't send change events while refreshing
     					final boolean changed = node.setChangeEventsEnabled(false);
     					// Initiate the refresh
     					model.getService(IModelRefreshService.class).refresh(node, new Callback() {
     						@Override
     						protected void internalDone(Object caller, IStatus status) {
     							// Mark the refresh as done
     							refreshable.setQueryState(QueryType.CHILD_LIST, QueryState.DONE);
     							// Reset the pending operation node
     							refreshable.setPendingOperationNode(null);
     							// Re-enable the change events if they had been enabled before
     							if (changed) node.setChangeEventsEnabled(true);
     							// Trigger a refresh of the view content
     							ChangeEvent event = new ChangeEvent(node, IContainerModelNode.NOTIFY_CHANGED, null, null);
     							EventManager.getInstance().fireEvent(event);
     						}
     					});
     				}
     			};
 
     			Protocol.invokeLater(runnable);
     		}
     	}
     }
 
 }
