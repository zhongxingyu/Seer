 /*******************************************************************************
  * Copyright (c) 2012 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.debug.test;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.concurrent.CancellationException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.IBreakpointManager;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.ILaunchesListener2;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.core.model.IDisconnect;
 import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualItem;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualTreeModelViewer;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.debug.ui.IDebugUIConstants;
 import org.eclipse.jface.dialogs.MessageDialogWithToggle;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.tcf.debug.test.services.BreakpointsCM;
 import org.eclipse.tcf.debug.test.services.DiagnosticsCM;
 import org.eclipse.tcf.debug.test.services.LineNumbersCM;
 import org.eclipse.tcf.debug.test.services.RegistersCM;
 import org.eclipse.tcf.debug.test.services.RunControlCM;
 import org.eclipse.tcf.debug.test.services.RunControlCM.ContextState;
 import org.eclipse.tcf.debug.test.services.StackTraceCM;
 import org.eclipse.tcf.debug.test.services.SymbolsCM;
 import org.eclipse.tcf.debug.test.util.AggregateCallback;
 import org.eclipse.tcf.debug.test.util.Callback;
 import org.eclipse.tcf.debug.test.util.Callback.ICanceledListener;
 import org.eclipse.tcf.debug.test.util.DataCallback;
 import org.eclipse.tcf.debug.test.util.ICache;
 import org.eclipse.tcf.debug.test.util.Query;
 import org.eclipse.tcf.debug.test.util.Task;
 import org.eclipse.tcf.debug.test.util.Transaction;
 import org.eclipse.tcf.debug.ui.ITCFObject;
 import org.eclipse.tcf.internal.debug.launch.TCFLaunchDelegate;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.JSON;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IBreakpoints;
 import org.eclipse.tcf.services.IDiagnostics;
 import org.eclipse.tcf.services.IDiagnostics.ISymbol;
 import org.eclipse.tcf.services.IExpressions;
 import org.eclipse.tcf.services.ILineNumbers;
 import org.eclipse.tcf.services.IMemoryMap;
 import org.eclipse.tcf.services.IRegisters;
 import org.eclipse.tcf.services.IRegisters.RegistersContext;
 import org.eclipse.tcf.services.IRunControl;
 import org.eclipse.tcf.services.IRunControl.RunControlContext;
 import org.eclipse.tcf.services.IStackTrace;
 import org.eclipse.tcf.services.ISymbols;
 import org.eclipse.tcf.te.tests.interfaces.IConfigurationProperties;
 import org.eclipse.tcf.te.tests.tcf.TcfTestCase;
 import org.junit.Assert;
 import org.osgi.framework.Bundle;
 
 /**
  * Base test for validating TCF Debugger UI.
  */
 @SuppressWarnings("restriction")
 public abstract class AbstractTcfUITest extends TcfTestCase implements IViewerUpdatesListenerConstants {
 
     private final static int NUM_CHANNELS = 1;
 
     protected IChannel[] channels;
 
     private Query<Object> fMonitorChannelQuery;
     private List<Throwable> errors = new ArrayList<Throwable>();
     protected ILaunch fLaunch;
 
     protected VirtualTreeModelViewer fDebugViewViewer;
     protected TestDebugContextProvider fDebugContextProvider;
     protected VirtualViewerUpdatesListener fDebugViewListener;
     protected VariablesVirtualTreeModelViewer fVariablesViewViewer;
     protected VirtualViewerUpdatesListener fVariablesViewListener;
     protected VariablesVirtualTreeModelViewer fRegistersViewViewer;
     protected VirtualViewerUpdatesListener fRegistersViewListener;
     protected TestSourceDisplayService fSourceDisplayService;
     protected SourceDisplayListener fSourceDisplayListener;
 
     protected Object fTestRunKey;
 
     protected IDiagnostics diag;
     protected IExpressions expr;
     protected ISymbols syms;
     protected IStackTrace stk;
     protected IRunControl rc;
     protected IBreakpoints bp;
     protected IMemoryMap fMemoryMap;
     protected ILineNumbers fLineNumbers;
     protected IRegisters fRegisters;
 
     protected RunControlCM fRunControlCM;
     protected DiagnosticsCM fDiagnosticsCM;
     protected BreakpointsCM fBreakpointsCM;
     protected StackTraceCM fStackTraceCM;
     protected SymbolsCM fSymbolsCM;
     protected LineNumbersCM fLineNumbersCM;
     protected RegistersCM fRegistersCM;
     protected String fTestId;
     protected RunControlContext fTestCtx;
     protected String fProcessId = "";
     protected String fThreadId = "";
     protected RunControlContext fThreadCtx;
 
     /* (non-Javadoc)
      * @see org.eclipse.tcf.te.tests.CoreTestCase#getTestBundle()
      */
     @Override
     protected Bundle getTestBundle() {
         return Activator.getDefault().getBundle();
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.tcf.te.tests.CoreTestCase#initialize()
      */
     @Override
     protected void initialize() {
         // Turn off the automatic perspective switch and debug view activation to avoid
         // JFace views from interfering with the virtual viewers used in tests.
         IPreferenceStore prefs = DebugUITools.getPreferenceStore();
         prefs.setValue(IInternalDebugUIConstants.PREF_ACTIVATE_DEBUG_VIEW, false);
         prefs.setValue(IInternalDebugUIConstants.PREF_SWITCH_PERSPECTIVE_ON_SUSPEND, MessageDialogWithToggle.NEVER);
 
         super.initialize();
         // Do not activate debug view or debug perspective, also to avoid interfering
         // with tests' virtual viewers.
         setProperty(IConfigurationProperties.TARGET_PERSPECTIVE, "org.eclipse.cdt.ui.CPerspective"); //$NON-NLS-1$
         setProperty(IConfigurationProperties.TARGET_VIEW, "org.eclipse.cdt.ui.CView"); //$NON-NLS-1$
     }
 
     @Override
     protected void setUp() throws Exception {
         fTestRunKey = new Object();
 
         // Launch the agent
         super.setUp();
 
         createDebugViewViewer();
         createLaunch();
 
         channels = new IChannel[NUM_CHANNELS];
 
         new Query<Object>() {
             @Override
             protected void execute(DataCallback<Object> callback) {
                 try {
                     openChannels(peer, callback);
                 }
                 catch (Throwable x) {
                     errors.add(x);
                     int cnt = 0;
                     for (int i = 0; i < channels.length; i++) {
                         if (channels[i] == null) continue;
                         if (channels[i].getState() != IChannel.STATE_CLOSED) channels[i].close();
                         cnt++;
                     }
                     if (cnt == 0) {
                         callback.setError(errors.get(0));
                         callback.done();
                     }
                 }
             }
         }.get();
 
         getRemoteServices();
 
         new Task<Object>() {
             @Override
             public Object call() throws Exception {
                 setUpServiceListeners();
                 return null;
             }
         }.get();
 
         validateTestAvailable();
         clearBreakpoints();
     }
 
     @Override
     protected void tearDown() throws Exception {
         new Task<Object>() {
             @Override
             public Object call() throws Exception {
                 tearDownServiceListeners();
                 return null;
             }
         }.get();
 
         terminateLaunch();
         disposeDebugViewViewer();
 
         new Query<Object>() {
             @Override
             protected void execute(DataCallback<Object> callback) {
                 closeChannels(callback);
             }
         }.get();
 
         super.tearDown();
     }
 
     protected String getDiagnosticsTestName() {
         return "RCBP1";
     }
 
     protected void setUpServiceListeners() throws Exception{
         fRunControlCM = new RunControlCM(rc);
         fDiagnosticsCM = new DiagnosticsCM(diag);
         fBreakpointsCM = new BreakpointsCM(bp);
         fStackTraceCM = new StackTraceCM(stk, rc);
         fSymbolsCM = new SymbolsCM(syms, fRunControlCM, fMemoryMap);
         fLineNumbersCM = new LineNumbersCM(fLineNumbers, fMemoryMap, fRunControlCM);
         fRegistersCM = new RegistersCM(fRegisters, rc);
     }
 
     protected void tearDownServiceListeners() throws Exception{
         fRegistersCM.dispose();
         fSymbolsCM.dispose();
         fBreakpointsCM.dispose();
         fStackTraceCM.dispose();
         fRunControlCM.dispose();
         fDiagnosticsCM.dispose();
         fLineNumbersCM.dispose();
     }
 
     private void createDebugViewViewer() {
         final Display display = Display.getDefault();
         display.syncExec(new Runnable() {
             public void run() {
                 fDebugViewViewer = new VirtualTreeModelViewer(display, SWT.NONE, new PresentationContext(IDebugUIConstants.ID_DEBUG_VIEW));
                 fDebugViewViewer.setInput(DebugPlugin.getDefault().getLaunchManager());
                 fDebugViewViewer.setAutoExpandLevel(-1);
                 fDebugViewListener = new VirtualViewerUpdatesListener(fDebugViewViewer);
                 fDebugContextProvider = new TestDebugContextProvider(fDebugViewViewer);
                 fVariablesViewViewer = new VariablesVirtualTreeModelViewer(IDebugUIConstants.ID_VARIABLE_VIEW, fDebugContextProvider);
                 fVariablesViewListener = new VirtualViewerUpdatesListener(fVariablesViewViewer);
                 fRegistersViewViewer = new VariablesVirtualTreeModelViewer(IDebugUIConstants.ID_REGISTER_VIEW, fDebugContextProvider);
                 fRegistersViewListener = new VirtualViewerUpdatesListener(fRegistersViewViewer);
                 fSourceDisplayService = new TestSourceDisplayService(fDebugContextProvider);
                 fSourceDisplayListener = new SourceDisplayListener();
             }
         });
     }
 
     private void disposeDebugViewViewer() {
         final Display display = Display.getDefault();
         display.syncExec(new Runnable() {
             public void run() {
                 fSourceDisplayListener.dispose();
                 fSourceDisplayService.dispose();
                 fDebugViewListener.dispose();
                 fDebugContextProvider.dispose();
                 fDebugViewViewer.dispose();
                 fVariablesViewListener.dispose();
                 fVariablesViewViewer.dispose();
                 fRegistersViewListener.dispose();
                 fRegistersViewViewer.dispose();
             }
         });
 
     }
 
     private void createLaunch() throws Exception {
         ILaunchManager lManager = DebugPlugin.getDefault().getLaunchManager();
         ILaunchConfigurationType lcType = lManager.getLaunchConfigurationType("org.eclipse.tcf.debug.LaunchConfigurationType");
         ILaunchConfigurationWorkingCopy lcWc = lcType.newInstance(null, "test");
         lcWc.setAttribute(TCFLaunchDelegate.ATTR_USE_LOCAL_AGENT, false);
         lcWc.setAttribute(TCFLaunchDelegate.ATTR_RUN_LOCAL_AGENT, false);
         lcWc.setAttribute(TCFLaunchDelegate.ATTR_PEER_ID, peer.getID());
         lcWc.doSave();
         fDebugViewListener.reset();
         fDebugViewListener.setDelayContentUntilProxyInstall(true);
         fLaunch = lcWc.launch("debug", new NullProgressMonitor());
         Assert.assertTrue( fLaunch instanceof IDisconnect );
 
         // The launch element may or may not have been populated into the viewer.  It's label
         // also may or may not have been updated.  Check viewer item for launch, then wait if needed.
         TreePath launchPath = new TreePath(new Object[] { fLaunch });
         fDebugViewListener.addLabelUpdate(launchPath);
         VirtualItem launchItem = fDebugViewViewer.findItem(launchPath);
             fDebugViewListener.findElement(new Pattern[] { Pattern.compile(".*" + fLaunch.getLaunchConfiguration().getName() + ".*") }  );
         if (launchItem == null || launchItem.getData() == null || launchItem.getData(VirtualItem.LABEL_KEY) == null) {
             fDebugViewListener.waitTillFinished(MODEL_CHANGED_COMPLETE | MODEL_PROXIES_INSTALLED | CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES);
             launchItem = fDebugViewViewer.findItem(launchPath);
         }
 
         Assert.assertTrue( launchItem != null );
         String[] launchItemLabel = (String[])launchItem.getData(VirtualItem.LABEL_KEY);
         Assert.assertTrue( launchItemLabel[0].contains(fLaunch.getLaunchConfiguration().getName()) );
         Assert.assertEquals(fLaunch, launchItem.getData());
     }
 
     private void terminateLaunch() throws DebugException, InterruptedException, ExecutionException {
         ((IDisconnect)fLaunch).disconnect();
 
         new Query<Object>() {
             @Override
             protected void execute(final DataCallback<Object> callback) {
                 final ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
 
                 final AtomicBoolean callbackDone = new AtomicBoolean(false);
                 ILaunchesListener2 disconnectListener = new ILaunchesListener2() {
                     public void launchesAdded(ILaunch[] launches) {}
                     public void launchesChanged(ILaunch[] launches) {}
                     public void launchesRemoved(ILaunch[] launches) {}
                     public void launchesTerminated(ILaunch[] launches) {
                         if (Arrays.asList(launches).contains(fLaunch)) {
                             if (!callbackDone.getAndSet(true)) {
                                 lm.removeLaunchListener(this);
                                 callback.done();
                             }
                         }
                     }
                 };
                 lm.addLaunchListener(disconnectListener);
                 if (((IDisconnect)fLaunch).isDisconnected() && !callbackDone.getAndSet(true)) {
                     lm.removeLaunchListener(disconnectListener);
                     callback.done();
 
                 }
             }
         }.get();
     }
 
     private void getRemoteServices() {
         assert !Protocol.isDispatchThread();
         Protocol.invokeAndWait(new Runnable() {
             public void run() {
                 diag = channels[0].getRemoteService(IDiagnostics.class);
                 expr = channels[0].getRemoteService(IExpressions.class);
                 syms = channels[0].getRemoteService(ISymbols.class);
                 stk = channels[0].getRemoteService(IStackTrace.class);
                 rc = channels[0].getRemoteService(IRunControl.class);
                 bp = channels[0].getRemoteService(IBreakpoints.class);
                 fMemoryMap = channels[0].getRemoteService(IMemoryMap.class);
                 fLineNumbers = channels[0].getRemoteService(ILineNumbers.class);
                 fRegisters = channels[0].getRemoteService(IRegisters.class);
             };
         });
     }
 
     private void openChannels(IPeer peer, Callback callback) {
         assert Protocol.isDispatchThread();
 
         for (int i = 0; i < channels.length; i++) {
             channels[i] = peer.openChannel();
         }
         monitorChannels(
             new Callback(callback) {
                 @Override
                 protected void handleSuccess() {
                     fMonitorChannelQuery = new Query<Object>() {
                         @Override
                         protected void execute(org.eclipse.tcf.debug.test.util.DataCallback<Object> callback) {
                             monitorChannels(callback, true);
                         };
                     };
                     fMonitorChannelQuery.invoke();
                     super.handleSuccess();
                 }
             },
             false);
     }
 
     private void closeChannels(final Callback callback) {
         assert Protocol.isDispatchThread();
         fMonitorChannelQuery.cancel(false);
         try {
             fMonitorChannelQuery.get();
         } catch (ExecutionException e) {
             callback.setError(e.getCause());
         } catch (CancellationException e) {
             // expected
         } catch (InterruptedException e) {
         }
 
         for (int i = 0; i < channels.length; i++) {
             if (channels[i].getState() != IChannel.STATE_CLOSED) channels[i].close();
         }
         monitorChannels(callback, true);
     }
 
     private static class ChannelMonitorListener implements IChannel.IChannelListener {
 
         final IChannel fChannel;
         final boolean fClose;
         final Callback fCallback;
         private boolean fActive = true;
 
         private class CancelListener implements ICanceledListener {
             public void requestCanceled(Callback rm) {
                 Protocol.invokeLater(new Runnable() {
                     public void run() {
                         if (deactivate()) {
                             fCallback.done();
                         }
                     }
                 });
             }
         }
 
         private boolean deactivate() {
             if (fActive) {
                 fChannel.removeChannelListener(ChannelMonitorListener.this);
                 fActive = false;
                 return true;
             }
             return false;
         }
 
         ChannelMonitorListener (IChannel channel, Callback cb, boolean close) {
             fCallback = cb;
             fClose = close;
             fChannel = channel;
             fChannel.addChannelListener(this);
             fCallback.addCancelListener(new CancelListener());
         }
 
         public void onChannelOpened() {
             if (!deactivate()) return;
 
             fChannel.removeChannelListener(this);
             fCallback.done();
         }
 
         public void congestionLevel(int level) {
         }
 
         public void onChannelClosed(Throwable error) {
             if (!deactivate()) return;
 
             if (!fClose) {
                 fCallback.setError( new IOException("Remote peer closed connection before all tests finished") );
             } else {
                 fCallback.setError(error);
             }
             fCallback.done();
         }
     }
 
     protected void monitorChannels(final Callback callback, final boolean close) {
         assert Protocol.isDispatchThread();
 
         AggregateCallback acb = new AggregateCallback(callback);
         int count = 0;
         for (int i = 0; i < channels.length; i++) {
             if (!checkChannelsState(channels[i], close)) {
                 new ChannelMonitorListener(channels[i], new Callback(acb), close);
                 count++;
             }
         }
         acb.setDoneCount(count);
     }
 
     // Checks whether all channels have achieved the desired state.
     private boolean checkChannelsState(IChannel channel, boolean close) {
         if (close) {
             if (channel.getState() != IChannel.STATE_CLOSED) {
                 return false;
             }
         } else {
             if (channel.getState() != IChannel.STATE_OPEN) {
                 return false;
             }
         }
         return true;
     }
 
     private void validateTestAvailable() throws ExecutionException, InterruptedException {
         String[] testList = new Transaction<String[]>() {
             @Override
             protected String[] process() throws InvalidCacheException ,ExecutionException {
                 return validate( fDiagnosticsCM.getTestList() );
             }
         }.get();
 
         int i = 0;
         for (; i < testList.length; i++) {
             if ("RCBP1".equals(testList[i])) break;
         }
 
         Assert.assertTrue("Required test not supported", i != testList.length);
     }
 
     protected void clearBreakpoints() throws InterruptedException, ExecutionException, CoreException {
 
         // Delete eclipse breakpoints.
         IWorkspaceRunnable wr = new IWorkspaceRunnable() {
             public void run(IProgressMonitor monitor) throws CoreException {
                 IBreakpointManager mgr = DebugPlugin.getDefault().getBreakpointManager();
                 IBreakpoint[] bps = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
                 mgr.removeBreakpoints(bps, true);
             }
         };
         ResourcesPlugin.getWorkspace().run(wr, null, 0, null);
 
         // Delete TCF breakpoints
         new Transaction<Object>() {
             @Override
             protected Object process() throws InvalidCacheException, ExecutionException {
                 // Initialize the event cache for breakpoint status
                 @SuppressWarnings("unchecked")
                 Map<String, Object>[] bps = new Map[] { };
                 validate( fBreakpointsCM.set(bps, this) );
                 return null;
             }
         }.get();
     }
 
     private void startProcess() throws InterruptedException, ExecutionException {
         new Transaction<Object>() {
             @Override
             protected Object process() throws Transaction.InvalidCacheException ,ExecutionException {
                 fTestId = validate( fDiagnosticsCM.runTest(getDiagnosticsTestName(), this) );
                 fTestCtx = validate( fRunControlCM.getContext(fTestId) );
                 fProcessId = fTestCtx.getProcessID();
                 // Create the cache to listen for exceptions.
                 fRunControlCM.waitForContextException(fTestId, fTestRunKey);
 
                 if (!fProcessId.equals(fTestId)) {
                     fThreadId = fTestId;
                 } else {
                     String[] threads = validate( fRunControlCM.getChildren(fProcessId) );
                     fThreadId = threads[0];
                 }
                 fThreadCtx = validate( fRunControlCM.getContext(fThreadId) );
 
                 Assert.assertTrue("Invalid thread context", fThreadCtx.hasState());
                 return new Object();
             };
         }.get();
     }
 
     private boolean runToTestEntry(final String testFunc) throws InterruptedException, ExecutionException {
         return new Transaction<Boolean>() {
             Object fWaitForResumeKey;
             Object fWaitForSuspendKey;
             boolean fSuspendEventReceived = false;
             @Override
             protected Boolean process() throws Transaction.InvalidCacheException ,ExecutionException {
                 ISymbol sym_func0 = validate( fDiagnosticsCM.getSymbol(fProcessId, testFunc) );
                 String sym_func0_value = sym_func0.getValue().toString();
                 ContextState state = validate (fRunControlCM.getState(fThreadId));
 
                 while (!state.suspended || !new BigInteger(state.pc).equals(new BigInteger(sym_func0_value))) {
                     if (state.suspended && fWaitForSuspendKey == null) {
                         fSuspendEventReceived = true;
                         // We are not at test entry.  Create a new suspend wait cache.
                         fWaitForResumeKey = new Object();
                         fWaitForSuspendKey = new Object();
                         ICache<Object> waitForResume = fRunControlCM.waitForContextResumed(fThreadId, fWaitForResumeKey);
 
                         // Issue resume command.
                         validate( fRunControlCM.resume(fThreadCtx, fWaitForResumeKey, IRunControl.RM_RESUME, 1) );
 
                         // Wait until we receive the resume event.
                         validate(waitForResume);
                         fWaitForSuspendKey = new Object();
                         fRunControlCM.waitForContextSuspended(fThreadId, fWaitForSuspendKey);
                     } else {
                         if (fWaitForResumeKey != null) {
                             // Validate resume command
                             validate( fRunControlCM.resume(fThreadCtx, fWaitForResumeKey, IRunControl.RM_RESUME, 1) );
                             fWaitForResumeKey = null;
 
                         }
                         // Wait until we suspend.
                         validate( fRunControlCM.waitForContextSuspended(fThreadId, fWaitForSuspendKey) );
                         fWaitForSuspendKey = null;
                     }
                 }
 
                 return fSuspendEventReceived;
             }
         }.get();
     }
 
     protected void initProcessModel(String testFunc) throws Exception {
         String bpId = "entryPointBreakpoint";
         createBreakpoint(bpId, testFunc);
         fDebugViewListener.reset();
 
         ITCFObject processTCFContext = new ITCFObject() {
             public String getID() { return fProcessId; }
             public IChannel getChannel() { return channels[0]; }
         };
         ITCFObject threadTCFContext = new ITCFObject() {
             public String getID() { return fThreadId; }
             public IChannel getChannel() { return channels[0]; }
         };
 
         fDebugViewListener.addLabelUpdate(new TreePath(new Object[] { fLaunch, processTCFContext }));
         fDebugViewListener.addLabelUpdate(new TreePath(new Object[] { fLaunch, processTCFContext, threadTCFContext }));
 
         startProcess();
        
        // Make sure that delta is posted after launching process so that it doesn't interfere
        // with the waiting for the whole viewer to update after breakpoint hit (below).
        fDebugViewListener.waitTillFinished(MODEL_CHANGED_COMPLETE);
        fDebugViewListener.resetModelChanged();
        
         runToTestEntry(testFunc);
         removeBreakpoint(bpId);
 
         final String topFrameId = new Transaction<String>() {
             @Override
             protected String process() throws InvalidCacheException, ExecutionException {
                 String[] frameIds = validate( fStackTraceCM.getChildren(fThreadId) );
                 Assert.assertTrue("No stack frames" , frameIds.length != 0);
                 return frameIds[frameIds.length - 1];
             }
         }.get();
 
         ITCFObject frameTCFContext = new ITCFObject() {
             public String getID() { return topFrameId; }
             public IChannel getChannel() { return channels[0]; }
         };
         fDebugViewListener.addLabelUpdate(new TreePath(new Object[] { fLaunch, processTCFContext, threadTCFContext, frameTCFContext }));
 
         fDebugViewListener.waitTillFinished(MODEL_CHANGED_COMPLETE | CONTENT_SEQUENCE_COMPLETE | LABEL_SEQUENCE_COMPLETE | LABEL_UPDATES);
     }
 
     protected ContextState resumeAndWaitForSuspend(final RunControlContext context, final int mode) throws InterruptedException, ExecutionException {
         return new Transaction<ContextState>() {
             @Override
             protected ContextState process() throws InvalidCacheException, ExecutionException {
                 ICache<Object> waitCache = fRunControlCM.waitForContextSuspended(context.getID(), this);
                 validate( fRunControlCM.resume(context, this, mode, 1) );
                 validate(waitCache);
                 return validate( fRunControlCM.getState(context.getID()) );
             }
         }.get();
     }
 
     protected void createBreakpoint(final String bpId, final String testFunc) throws InterruptedException, ExecutionException {
         new Transaction<Object>() {
             private Map<String,Object> fBp;
             {
                 fBp = new TreeMap<String,Object>();
                 fBp.put(IBreakpoints.PROP_ID, bpId);
                 fBp.put(IBreakpoints.PROP_ENABLED, Boolean.TRUE);
                 fBp.put(IBreakpoints.PROP_LOCATION, testFunc);
             }
 
             @Override
             protected Object process() throws InvalidCacheException, ExecutionException {
                 // Prime event wait caches
                 fBreakpointsCM.waitContextAdded(this);
 
                 validate( fBreakpointsCM.add(fBp, this) );
                 validate( fBreakpointsCM.waitContextAdded(this));
 
                 // Wait for breakpoint status event and validate it.
                 Map<String, Object> status = validate(fBreakpointsCM.getStatus(bpId));
                 String s = (String)status.get(IBreakpoints.STATUS_ERROR);
                 if (s != null) {
                     Assert.fail("Invalid BP status: " + s);
                 }
                 @SuppressWarnings("unchecked")
                 Collection<Map<String,Object>> list = (Collection<Map<String,Object>>)status.get(IBreakpoints.STATUS_INSTANCES);
                 if (list != null) {
                     String err = null;
                     for (Map<String,Object> map : list) {
                         String ctx = (String)map.get(IBreakpoints.INSTANCE_CONTEXT);
                         if (fProcessId.equals(ctx) && map.get(IBreakpoints.INSTANCE_ERROR) != null)
                             err = (String)map.get(IBreakpoints.INSTANCE_ERROR);
                     }
                     if (err != null) {
                         Assert.fail("Invalid BP status: " + s);
                     }
                 }
                 return null;
             }
         }.get();
     }
 
     protected void removeBreakpoint(final String bpId) throws InterruptedException, ExecutionException {
         new Transaction<Object>() {
 
             @Override
             protected Object process() throws InvalidCacheException, ExecutionException {
 
                 // Prime event wait caches
                 fBreakpointsCM.waitContextRemoved(this);
 
                 // Remove
                 validate( fBreakpointsCM.remove(new String[] { bpId }, this) );
 
                 // Verify removed event
                 String[] removedIds = validate( fBreakpointsCM.waitContextRemoved(this));
                 Assert.assertTrue(Arrays.asList(removedIds).contains(bpId));
                 return null;
             }
         }.get();
     }
 
     protected void moveToLocation(final String context, final Number address) throws
         DebugException, ExecutionException, InterruptedException
     {
         final RegistersContext pcReg = new Transaction<RegistersContext>() {
             @Override
             protected RegistersContext process() throws InvalidCacheException ,ExecutionException {
                 String[] registers = validate(fRegistersCM.getChildren(context));
                 return findPCRegister(registers);
             }
 
             private RegistersContext findPCRegister(String[] registerIds) throws InvalidCacheException ,ExecutionException {
                 for (String regId : registerIds) {
                     RegistersContext reg = validate(fRegistersCM.getContext(regId));
                     if ( IRegisters.ROLE_PC.equals(reg.getRole()) ) {
                         return reg;
                     }
                 }
                 for (String regId : registerIds) {
                     String[] children = validate(fRegistersCM.getChildren(regId));
                     RegistersContext pc = findPCRegister(children);
                     if (pc != null) return pc;
                 }
                 return null;
             }
         }.get();
 
         assertNotNull("Cannot find PC register", pcReg);
 
         new Transaction<Object>() {
             @Override
             protected Object process() throws Transaction.InvalidCacheException ,ExecutionException {
                 byte[] value = addressToByteArray(address, pcReg.getSize(), pcReg.isBigEndian());
                 validate(fRegistersCM.setContextValue(pcReg, this, value));
                 return null;
             };
         }.get();
     }
 
     private byte[] addressToByteArray(Number address, int size, boolean bigEndian) {
         byte[] bytes = new byte[size];
         byte[] addrBytes = JSON.toBigInteger(address).toByteArray();
         for (int i=0; i < bytes.length; ++i) {
             byte b = 0;
             if (i < addrBytes.length) {
                 b = addrBytes[addrBytes.length - i - 1];
             }
             bytes[bigEndian ? size -i - 1 : i] = b;
         }
         return bytes;
     }
 
 
 }
