 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.debug.test;
 
 import java.util.concurrent.ExecutionException;
 import java.util.regex.Pattern;
 
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
 import org.eclipse.debug.internal.ui.elements.adapters.DefaultBreakpointsViewInput;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualItem;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualTreeModelViewer;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.debug.ui.IDebugUIConstants;
 import org.eclipse.debug.ui.contexts.AbstractDebugContextProvider;
 import org.eclipse.jface.dialogs.MessageDialogWithToggle;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.TreeSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.tcf.debug.test.util.Transaction;
 import org.eclipse.tcf.debug.ui.ITCFObject;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.te.tests.interfaces.IConfigurationProperties;
 import org.junit.Assert;
 import org.osgi.framework.Bundle;
 
 /**
  * Base test for validating TCF Debugger UI.
  */
 @SuppressWarnings("restriction")
 public abstract class AbstractTcfUITest extends AbstractCMTest implements IViewerUpdatesListenerConstants {
 
     protected VirtualTreeModelViewer fDebugViewViewer;
     protected TestDebugContextProvider fDebugContextProvider;
     protected VirtualViewerUpdatesListener fDebugViewListener;
     protected VariablesVirtualTreeModelViewer fVariablesViewViewer;
     protected VirtualViewerUpdatesListener fVariablesViewListener;
     protected VariablesVirtualTreeModelViewer fRegistersViewViewer;
     protected VirtualViewerUpdatesListener fRegistersViewListener;
     protected VariablesVirtualTreeModelViewer fBreakpointsViewViewer;
     protected VirtualViewerUpdatesListener fBreakpointsViewListener;
     protected TestSourceDisplayService fSourceDisplayService;
     protected SourceDisplayListener fSourceDisplayListener;
 
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
         super.setUp();
         createDebugViewViewer();
     }
 
     @Override
     protected void tearDown() throws Exception {
         disposeDebugViewViewer();
         super.tearDown();
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
                 final IPresentationContext context = new PresentationContext(IDebugUIConstants.ID_BREAKPOINT_VIEW);
                 fBreakpointsViewViewer = new VariablesVirtualTreeModelViewer(
                     context,
                     new AbstractDebugContextProvider(null) {
                         private final ISelection fInput = new TreeSelection( new TreePath(new Object[] { new DefaultBreakpointsViewInput(context) }) );
                         @Override
                         public ISelection getActiveContext() {
                             return fInput;
                         }
                     });
                 fBreakpointsViewListener = new VirtualViewerUpdatesListener(fBreakpointsViewViewer);
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
 
     protected TestProcessInfo initProcessModel(String testFunc) throws Exception {
         String bpId = "entryPointBreakpoint";
         createBreakpoint(bpId, testFunc);
         fDebugViewListener.reset();
 
         final TestProcessInfo processInfo = startProcess();
 
         ITCFObject processTCFContext = new ITCFObject() {
             public String getID() { return processInfo.fProcessId; }
             public IChannel getChannel() { return channels[0]; }
         };
         ITCFObject threadTCFContext = new ITCFObject() {
             public String getID() { return processInfo.fThreadId; }
             public IChannel getChannel() { return channels[0]; }
         };
 
        fDebugViewListener.addLabelUpdate(new TreePath(new Object[] { fLaunch, processTCFContext }));
        fDebugViewListener.addLabelUpdate(new TreePath(new Object[] { fLaunch, processTCFContext, threadTCFContext }));


         // Make sure that delta is posted after launching process so that it doesn't interfere
         // with the waiting for the whole viewer to update after breakpoint hit (below).
        fDebugViewListener.waitTillFinished(MODEL_CHANGED_COMPLETE| CONTENT_SEQUENCE_COMPLETE | LABEL_SEQUENCE_COMPLETE);
         fDebugViewListener.reset();
 
         runToTestEntry(processInfo, testFunc);
         removeBreakpoint(bpId);
 
         final String topFrameId = new Transaction<String>() {
             @Override
             protected String process() throws InvalidCacheException, ExecutionException {
                 String[] frameIds = validate( fStackTraceCM.getChildren(processInfo.fThreadId) );
                 Assert.assertTrue("No stack frames" , frameIds.length != 0);
                 return frameIds[frameIds.length - 1];
             }
         }.get();
 
         ITCFObject frameTCFContext = new ITCFObject() {
             public String getID() { return topFrameId; }
             public IChannel getChannel() { return channels[0]; }
         };
 
         VirtualItem topFrameItem = null;
         long timeout = System.currentTimeMillis() + TIMEOUT_DEFAULT;
         do {
             fDebugViewListener.addLabelUpdate(new TreePath(new Object[] { fLaunch, processTCFContext, threadTCFContext, frameTCFContext }));
             fDebugViewListener.waitTillFinished(MODEL_CHANGED_COMPLETE | CONTENT_SEQUENCE_COMPLETE | LABEL_SEQUENCE_COMPLETE | LABEL_UPDATES);
             topFrameItem = fDebugViewListener.findElement(
                 new Pattern[] { Pattern.compile(".*"), Pattern.compile(".*"), Pattern.compile(".*" + processInfo.fProcessId + ".*\\(.*[Bb]reakpoint.*"), Pattern.compile(".*")});
             fDebugViewListener.reset();
         } while (topFrameItem == null && System.currentTimeMillis() < timeout);
         
         if (topFrameItem == null) {
             Assert.fail("Top stack frame not found. \n\nDebug view dump: \n:" + fDebugViewViewer.toString());
         }
 
         return processInfo;
     }
 }
