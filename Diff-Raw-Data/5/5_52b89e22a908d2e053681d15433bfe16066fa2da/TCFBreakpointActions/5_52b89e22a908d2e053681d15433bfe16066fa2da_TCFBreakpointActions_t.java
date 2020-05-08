 /*******************************************************************************
 * Copyright (c) 2011, 2014 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.cdt.ui;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.cdt.debug.core.CDebugCorePlugin;
 import org.eclipse.cdt.debug.core.breakpointactions.BreakpointActionManager;
 import org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction;
 import org.eclipse.cdt.debug.core.breakpointactions.ILogActionEnabler;
 import org.eclipse.cdt.debug.core.breakpointactions.IResumeActionEnabler;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.tcf.internal.debug.actions.TCFAction;
 import org.eclipse.tcf.internal.debug.model.TCFBreakpointsModel;
 import org.eclipse.tcf.internal.debug.model.TCFContextState;
 import org.eclipse.tcf.internal.debug.model.TCFLaunch;
 import org.eclipse.tcf.internal.debug.ui.model.TCFChildrenLogExpressions;
 import org.eclipse.tcf.internal.debug.ui.model.TCFModel;
 import org.eclipse.tcf.internal.debug.ui.model.TCFModelManager;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExecContext;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNodeExpression;
 import org.eclipse.tcf.protocol.IToken;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IRunControl;
 import org.eclipse.tcf.services.IRunControl.RunControlContext;
 import org.eclipse.tcf.util.TCFDataCache;
 import org.eclipse.tcf.util.TCFTask;
 
 public class TCFBreakpointActions {
 
     private class BreakpointActionAdapter extends TCFAction implements IAdaptable, ILogActionEnabler, IResumeActionEnabler {
 
         private final HashMap<String,BreakpointActionAdapter> active_actions;
         private final TCFNodeExecContext node;
         private final Job job;
 
         private boolean started;
         private boolean resumed;
 
         BreakpointActionAdapter(final HashMap<String,BreakpointActionAdapter> actions,
                 final IBreakpoint breakpoint, TCFNodeExecContext node) {
             super(node.getModel().getLaunch(), node.getID());
             this.active_actions = actions;
             this.node = node;
             job = new Job("Breakpoint actions") { //$NON-NLS-1$
                 @Override
                 protected IStatus run(IProgressMonitor monitor) {
                     IStatus status = Status.OK_STATUS;
                     try {
                         IMarker marker = breakpoint.getMarker();
                         String names = marker.getAttribute(BreakpointActionManager.BREAKPOINT_ACTION_ATTRIBUTE, "");
                         final String[] actions = names.split(","); //$NON-NLS-1$
                         for (int i = 0; i < actions.length && !monitor.isCanceled(); i++) {
                             String name = actions[i];
                             IBreakpointAction action = bp_action_manager.findBreakpointAction(name);
                             if (action != null) {
                                 monitor.setTaskName(action.getSummary());
                                 status = action.execute(breakpoint, BreakpointActionAdapter.this, monitor);
                                 if (status.getCode() != IStatus.OK) break;
                             }
                             monitor.worked(1);
                         }
                     }
                     catch (Exception e) {
                         status = new Status(
                                 IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR,
                                 "Cannot execute breakpoint action", e);
                     }
                     if (!monitor.isCanceled() && status.getCode() != IStatus.OK) Activator.log(status);
                     Protocol.invokeAndWait(new Runnable() {
                         public void run() {
                             assert aborted == (actions.get(ctx_id) != BreakpointActionAdapter.this);
                             if (!aborted) actions.remove(ctx_id);
                             BreakpointActionAdapter.this.done();
                         }
                     });
                     return status;
                 };
             };
             BreakpointActionAdapter a = actions.get(ctx_id);
             if (a != null) a.abort();
             assert actions.get(ctx_id) == null;
             actions.put(ctx_id, this);
         }
 
         @SuppressWarnings("rawtypes")
         public Object getAdapter(Class adapter) {
             if (adapter.isInstance(this)) return this;
             return Platform.getAdapterManager().loadAdapter(this, adapter.getName());
         }
 
         public void resume() throws Exception {
             new TCFTask<Boolean>(node.getChannel()) {
                 public void run() {
                     if (aborted || resumed || node.isDisposed()) {
                         done(false);
                         return;
                     }
                     TCFDataCache<TCFContextState> state_cache = node.getState();
                     if (!state_cache.validate(this)) return;
                     TCFContextState state_data = state_cache.getData();
                     if (state_data == null || !state_data.is_suspended) {
                         done(false);
                         return;
                     }
                     TCFDataCache<IRunControl.RunControlContext> ctx_cache = node.getRunContext();
                     if (!ctx_cache.validate(this)) return;
                     IRunControl.RunControlContext ctx_data = ctx_cache.getData();
                     if (ctx_data == null) {
                         done(false);
                         return;
                     }
                     resumed = true;
                     String rc_grp = ctx_data.getRCGroup();
                     if (rc_grp != null) {
                         List<BreakpointActionAdapter> l = new ArrayList<BreakpointActionAdapter>();
                         for (BreakpointActionAdapter a : active_actions.values()) {
                             if (a.node.isDisposed()) continue;
                             if (a == BreakpointActionAdapter.this) continue;
                             TCFDataCache<IRunControl.RunControlContext> a_ctx_cache = a.node.getRunContext();
                             if (!a_ctx_cache.validate(this)) return;
                            IRunControl.RunControlContext a_ctx_data = a_ctx_cache.getData();
                             if (a_ctx_data == null) continue;
                             if (rc_grp.equals(a_ctx_data.getRCGroup())) l.add(a);
                         }
                         for (BreakpointActionAdapter a : l) a.abort();
                     }
                     ctx_data.resume(IRunControl.RM_RESUME, 1, new IRunControl.DoneCommand() {
                         public void doneCommand(IToken token, Exception error) {
                             if (error != null && !aborted) error(error);
                             else done(true);
                         }
                     });
                 }
             }.get();
         }
 
         public String evaluateExpression(final String expression) throws Exception {
             return new TCFTask<String>(node.getChannel()) {
                 public void run() {
                     if (aborted || node.isDisposed()) {
                         done("");
                         return;
                     }
                     TCFChildrenLogExpressions cache = node.getLogExpressionCache();
                     cache.addScript(expression);
                     if (!cache.validate(this)) return;
                     TCFNodeExpression expr = cache.findScript(expression);
                     String s = expr.getValueText(true, this);
                     if (s != null) done(expression + " = " + s);
                 }
             }.get();
         }
 
         public void run() {
             if (aborted) {
                 done();
             }
             else {
                 // Post delta to update commands UI state.
                 node.postStateChangedDelta();
                 job.schedule();
                 started = true;
             }
         }
 
         @Override
         public int getPriority() {
             return 100;
         }
 
         @Override
         public void abort() {
             assert aborted == (active_actions.get(ctx_id) != this);
             if (aborted) return;
             super.abort();
             active_actions.remove(ctx_id);
             if (started && job.cancel()) done();
         }
     }
 
     private class RunControlListener implements IRunControl.RunControlListener {
 
         private final IRunControl rc;
         private final TCFModel model;
         private final HashMap<String,BreakpointActionAdapter> active_actions;
 
         RunControlListener(TCFModel model) {
             this.model = model;
             active_actions = new HashMap<String,BreakpointActionAdapter>();
             rc = model.getLaunch().getService(IRunControl.class);
             if (rc != null) rc.addListener(this);
         }
 
         void dispose() {
             if (rc != null) rc.removeListener(this);
             BreakpointActionAdapter[] arr = active_actions.values().toArray(new BreakpointActionAdapter[active_actions.size()]);
             for (BreakpointActionAdapter a : arr) a.abort();
         }
 
         public void contextAdded(RunControlContext[] contexts) {
         }
 
         public void contextChanged(RunControlContext[] contexts) {
         }
 
         public void contextRemoved(String[] context_ids) {
             for (String id : context_ids) {
                 BreakpointActionAdapter a = active_actions.get(id);
                 if (a != null) a.abort();
             }
         }
 
         public void contextSuspended(String context, String pc, String reason, Map<String,Object> params) {
             if (params == null) return;
             Object ids = params.get(IRunControl.STATE_BREAKPOINT_IDS);
             if (ids == null) return;
             TCFNodeExecContext node = (TCFNodeExecContext)model.getNode(context);
             if (node == null) return;
             @SuppressWarnings("unchecked")
             Collection<String> c = (Collection<String>)ids;
             for (String bp_id : c) {
                 IBreakpoint bp = bp_model.getBreakpoint(bp_id);
                 if (!bp_action_manager.breakpointHasActions(bp)) continue;
                 new BreakpointActionAdapter(active_actions, bp, node);
             }
         }
 
         public void contextResumed(String context) {
             BreakpointActionAdapter a = active_actions.get(context);
             if (a != null && !a.resumed) a.abort();
         }
 
         public void containerSuspended(String context, String pc,
                 String reason, Map<String, Object> params,
                 String[] suspended_ids) {
             contextSuspended(context, pc, reason, params);
         }
 
         public void containerResumed(String[] context_ids) {
             for (String id : context_ids) {
                 contextResumed(id);
             }
         }
 
         public void contextException(String context, String msg) {
         }
     }
 
     private final TCFModelManager.ModelManagerListener launch_listener = new TCFModelManager.ModelManagerListener() {
 
         public void onConnected(TCFLaunch launch, TCFModel model) {
             assert rc_listeners.get(launch) == null;
             new RunControlListener(model);
         }
 
         public void onDisconnected(TCFLaunch launch, TCFModel model) {
             RunControlListener l = rc_listeners.remove(launch);
             if (l != null) l.dispose();
         }
     };
 
     private final TCFBreakpointsModel bp_model;
     private final TCFModelManager model_manager;
     private final BreakpointActionManager bp_action_manager;
     private final Map<TCFLaunch,RunControlListener> rc_listeners;;
 
     TCFBreakpointActions() {
         assert Protocol.isDispatchThread();
         bp_action_manager = CDebugCorePlugin.getDefault().getBreakpointActionManager();
         bp_model = TCFBreakpointsModel.getBreakpointsModel();
         model_manager = TCFModelManager.getModelManager();
         model_manager.addListener(launch_listener);
         rc_listeners = new HashMap<TCFLaunch,RunControlListener>();
         // handle already connected launches
         for (TCFModel model : model_manager.getModels()) {
             TCFLaunch launch = model.getLaunch();
             if (launch.isConnected()) launch_listener.onConnected(launch, model);
         }
     }
 
     void dispose() {
         assert Protocol.isDispatchThread();
         model_manager.removeListener(launch_listener);
         for (RunControlListener l : rc_listeners.values()) l.dispose();
         rc_listeners.clear();
     }
 }
