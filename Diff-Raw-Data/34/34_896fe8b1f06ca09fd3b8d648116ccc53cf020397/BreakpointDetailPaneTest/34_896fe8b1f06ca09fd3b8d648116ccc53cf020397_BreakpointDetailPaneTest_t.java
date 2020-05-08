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
 
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.regex.Pattern;
 
 import junit.framework.AssertionFailedError;
 
 import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualItem;
 import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualTreeModelViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.tcf.debug.ui.ITCFDebugUIConstants;
 import org.eclipse.tcf.internal.cdt.ui.breakpoints.TCFBreakpointScopeDetailPane.ContextQueryElement;
 import org.eclipse.tcf.internal.cdt.ui.breakpoints.TCFBreakpointScopeDetailPane.ScopeDetailInputObject;
 import org.eclipse.tcf.internal.debug.ui.launch.TCFLaunchContext;
 import org.junit.Assert;
 
 @SuppressWarnings("restriction")
 public class BreakpointDetailPaneTest extends AbstractTcfUITest
 {
     private BreakpointsListener fBpListener;
     
     protected VirtualTreeModelViewer fContextQueryViewViewer;
     protected VirtualViewerUpdatesListener fContextQueryViewListener;
     
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         fBpListener = new BreakpointsListener();
 
         // CDT Breakpoint integration depends on the TCF-CDT breakpoint
         // integration to be active.  This is normally triggered by selecting
         // a stack frame in the UI.  Here force activation of the plugin
         // artificially.  None of the cdt integration packages are exported, so
         // use the TCF Launch Context extension point indirectly to force the
         // plugin to load.
         TCFLaunchContext.getLaunchContext(null);
         
         final Display display = Display.getDefault();
         display.syncExec(new Runnable() {
             public void run() {
                 fContextQueryViewViewer = new VirtualTreeModelViewer(
                     display, SWT.NONE, new PresentationContext(ITCFDebugUIConstants.ID_CONTEXT_QUERY_VIEW));
                 fContextQueryViewListener = new VirtualViewerUpdatesListener(fContextQueryViewViewer);
             }
         });
 
     }
 
     @Override
     protected void tearDown() throws Exception {
         final Display display = Display.getDefault();
         display.syncExec(new Runnable() {
             public void run() {
                 fContextQueryViewListener.dispose();
                 fContextQueryViewViewer.dispose();
             }
         });
         fBpListener.dispose();
         super.tearDown();
     }
 
     public void testContextQueryFilter() throws Exception {
         final TestProcessInfo processInfo1 = initProcessModel("tcf_test_func0");
         final TestProcessInfo processInfo2 = initProcessModel("tcf_test_func0");
 
         // Note: run control prefixes process ID with a "P", but the 
         // filter expects the ID without the "P".
         String processId1 = processInfo1.fProcessId;
         if (processId1.startsWith("P")) processId1 = processId1.substring(1);
         final String queryPid1 = "pid=" + processId1;
 
         try {
             fContextQueryViewListener.reset();
             Display.getDefault().syncExec(new Runnable() { public void run() {
                 fContextQueryViewViewer.setAutoExpandLevel(-1);
                 fContextQueryViewViewer.setInput(new ScopeDetailInputObject(new ContextQueryElement(queryPid1, null)));
                 fContextQueryViewViewer.getPresentationContext().setProperty(ITCFDebugUIConstants.PROP_CONTEXT_QUERY, queryPid1);
             }});
             fContextQueryViewListener.waitTillFinished(CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);                
             
             VirtualItem scopeItem = fContextQueryViewListener.findElement(new Pattern[] { Pattern.compile(".*pid\\="+processInfo1.fProcessId+".*") });
             scopeItem = fContextQueryViewListener.findElement(new Pattern[] { 
                 Pattern.compile(escapeBrackets("(2) Filter: " + queryPid1)), // filter name
                 Pattern.compile(escapeBrackets("(2) test.*")), // launch
                 Pattern.compile(escapeBrackets("(1).*agent.*")), // process name            
                 Pattern.compile(".*" + processInfo1.fThreadId+".*") }); // thread
     
             Assert.assertTrue(scopeItem != null);
     
             fContextQueryViewListener.reset();
             //fContextQueryViewListener.waitTillFinished(MODEL_CHANGED_COMPLETE | CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
     
             // Check that second process is filtered out.
             scopeItem = fContextQueryViewListener.findElement(new Pattern[] { 
                 Pattern.compile("\\([0-9]+\\) Filter: " + queryPid1), // filter name
                 Pattern.compile("\\([0-9]+\\) test.*"), // launch
                 Pattern.compile("\\([0-9]+\\) .*agent.*"), // process name            
                 Pattern.compile(".*"+processInfo2.fThreadId+".*") }); // thread
             Assert.assertTrue(scopeItem == null);
             scopeItem = fContextQueryViewListener.findElement(new Pattern[] { Pattern.compile(".*pid\\="+processInfo2.fProcessId+".*") });
             Assert.assertTrue(scopeItem == null);
             
             // Allprocesses in query
             
 // TODO: need a filter that will match all
             
 //            fContextQueryViewListener.reset();
 //            final String queryPidAll = "KernelName=Linux";
 //            Display.getDefault().syncExec(new Runnable() { public void run() {
 //                fContextQueryViewViewer.setInput(new ScopeDetailInputObject(new ContextQueryElement(queryPidAll, null)));
 //                fContextQueryViewViewer.getPresentationContext().setProperty(ITCFDebugUIConstants.PROP_CONTEXT_QUERY, queryPidAll);
 //            }});
 //            fContextQueryViewListener.waitTillFinished(CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
 //            
 //            scopeItem = fContextQueryViewListener.findElement(new Pattern[] { 
 //                Pattern.compile(escapeBrackets("(4) Filter: " + queryPidAll)), // filter name
 //                Pattern.compile(escapeBrackets("(4) test.*")), // launch
 //                Pattern.compile(escapeBrackets("(1).*agent.*")), // process name            
 //                Pattern.compile(".*" + processInfo1.fThreadId+".*") }); // thread
 //            Assert.assertTrue(scopeItem != null);
 //            
 //            scopeItem = fContextQueryViewListener.findElement(new Pattern[] { 
 //                Pattern.compile(escapeBrackets("(4) Filter: " + queryPidAll)), // filter name
 //                Pattern.compile(escapeBrackets("(4) test.*")), // launch
 //                Pattern.compile(escapeBrackets("(1).*agent.*")), // process name            
 //                Pattern.compile(".*" + processInfo2.fThreadId+".*") }); // thread
 //            Assert.assertTrue(scopeItem != null);
 //            
         } catch (AssertionFailedError e) {
             System.out.print("Context query view dump: \n:" + fContextQueryViewViewer.toString());
             throw e;
         }
     }
 
     public void testContextsFilter() throws Exception {
         final TestProcessInfo processInfo1 = initProcessModel("tcf_test_func0");
         final TestProcessInfo processInfo2 = initProcessModel("tcf_test_func0");
 
         // Note: run control prefixes process ID with a "P", but the 
         // filter expects the ID without the "P".
         final Set<String> processContexts1 = new TreeSet<String>();
         processContexts1.add( fLaunch.getLaunchConfiguration().getName() + "/" + processInfo1.fThreadId );
 
         try {
             fContextQueryViewListener.reset();
             Display.getDefault().syncExec(new Runnable() { public void run() {
                 fContextQueryViewViewer.setAutoExpandLevel(-1);
                 fContextQueryViewViewer.setInput(new ScopeDetailInputObject(new ContextQueryElement(null, processContexts1)));
                 fContextQueryViewViewer.getPresentationContext().setProperty(ITCFDebugUIConstants.PROP_FILTER_CONTEXTS, processContexts1);
             }});
             fContextQueryViewListener.waitTillFinished(CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);                
             
             VirtualItem scopeItem = fContextQueryViewListener.findElement(new Pattern[] { Pattern.compile(".*pid\\="+processInfo1.fProcessId+".*") });
             scopeItem = fContextQueryViewListener.findElement(new Pattern[] { 
                 Pattern.compile(escapeBrackets("(1) Contexts: " + processContexts1)), // filter name
                 Pattern.compile(escapeBrackets("(1) test.*")), // launch
                 Pattern.compile(escapeBrackets("(1).*agent.*")), // process name            
                 Pattern.compile(".*" + processInfo1.fThreadId+".*") }); // thread
     
             Assert.assertTrue(scopeItem != null);
     
             fContextQueryViewListener.reset();
     
             // Check that second process is filtered out.
             scopeItem = fContextQueryViewListener.findElement(new Pattern[] { 
                 Pattern.compile("\\([0-9]+\\) Contexts: " + escapeBrackets(processContexts1.toString())), // filter name
                 Pattern.compile("\\([0-9]+\\) test.*"), // launch
                 Pattern.compile("\\([0-9]+\\) .*agent.*"), // process name            
                 Pattern.compile(".*"+processInfo2.fThreadId+".*") }); // thread
             Assert.assertTrue(scopeItem == null);
             scopeItem = fContextQueryViewListener.findElement(new Pattern[] { Pattern.compile(".*pid\\="+processInfo2.fProcessId+".*") });
             Assert.assertTrue(scopeItem == null);
             
             // Allprocesses in query
             
 
             fContextQueryViewListener.reset();
             final Set<String> processContextsAll = new TreeSet<String>();
             processContextsAll.add( fLaunch.getLaunchConfiguration().getName() + "/" + processInfo1.fThreadId );
             processContextsAll.add( fLaunch.getLaunchConfiguration().getName() + "/" + processInfo2.fThreadId );
             Display.getDefault().syncExec(new Runnable() { public void run() {
                 fContextQueryViewViewer.setInput(new ScopeDetailInputObject(new ContextQueryElement(null, processContextsAll)));
                 fContextQueryViewViewer.getPresentationContext().setProperty(ITCFDebugUIConstants.PROP_FILTER_CONTEXTS, processContextsAll);
             }});
             fContextQueryViewListener.waitTillFinished(CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
             
             scopeItem = fContextQueryViewListener.findElement(new Pattern[] { 
                 Pattern.compile(escapeBrackets("(2) Contexts: " + processContextsAll)), // filter name
                 Pattern.compile(escapeBrackets("(2) test.*")), // launch
                 Pattern.compile(escapeBrackets("(1).*agent.*")), // process name            
                 Pattern.compile(".*" + processInfo1.fThreadId+".*") }); // thread
             Assert.assertTrue(scopeItem != null);
             
             scopeItem = fContextQueryViewListener.findElement(new Pattern[] { 
                 Pattern.compile(escapeBrackets("(2) Contexts: " + processContextsAll)), // filter name
                 Pattern.compile(escapeBrackets("(2) test.*")), // launch
                 Pattern.compile(escapeBrackets("(1).*agent.*")), // process name            
                 Pattern.compile(".*" + processInfo2.fThreadId+".*") }); // thread
             Assert.assertTrue(scopeItem != null);
             
         } catch (AssertionFailedError e) {
             System.out.print("Context query view dump: \n:" + fContextQueryViewViewer.toString());
             throw e;
         }
     }
 
     private String escapeBrackets(String s) {
         StringBuffer escaped = new StringBuffer();
         for (int i = 0; i < s.length(); i++) {
             char c = s.charAt(i); 
             if (c == '[' || c == ']' || c == '(' || c == ')') {
                 escaped.append('\\');
             } 
             escaped.append(c);
            // Make spaces optional:
            if (c == ' ') {
                escaped.append("*");
            }
         }
         return escaped.toString();
     }
 }
