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
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.regex.Pattern;
 
 import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualItem;
 import org.eclipse.tcf.debug.test.services.IWaitForEventCache;
 import org.eclipse.tcf.debug.test.services.RunControlCM;
 import org.eclipse.tcf.debug.test.util.ICache;
 import org.eclipse.tcf.debug.test.util.RangeCache;
 import org.eclipse.tcf.debug.test.util.Transaction;
 import org.eclipse.tcf.services.ILineNumbers.CodeArea;
 import org.eclipse.tcf.services.IRunControl;
 import org.eclipse.tcf.services.IRunControl.RunControlContext;
 import org.eclipse.tcf.services.IStackTrace.StackTraceContext;
 import org.eclipse.tcf.services.ISymbols;
 import org.eclipse.tcf.services.ISymbols.Symbol;
 import org.junit.Assert;
 
 @SuppressWarnings("restriction")
 public class SampleTest extends AbstractTcfUITest {
 
 
     public void testDebugViewContent() throws Exception {
         initProcessModel("tcf_test_func0");
 
         VirtualItem launchItem = fDebugViewListener.findElement(new Pattern[] { Pattern.compile(".*" + fLaunch.getLaunchConfiguration().getName() + ".*") }  );
         Assert.assertTrue(launchItem != null);
 
         VirtualItem processItem = fDebugViewListener.findElement(launchItem, new Pattern[] { Pattern.compile(".*agent.*") }  );
         Assert.assertTrue(processItem != null);
 
         VirtualItem threadItem = fDebugViewListener.findElement(processItem, new Pattern[] { Pattern.compile(".*" + fThreadId + ".*") }  );
         Assert.assertTrue(threadItem != null);
         VirtualItem frameItem = fDebugViewListener.findElement(threadItem, new Pattern[] { Pattern.compile(".*tcf_test_func0.*")});
         Assert.assertTrue(frameItem != null);
     }
 
     public void testSteppingDebugViewOnly() throws Exception {
         initProcessModel("tcf_test_func0");
 
         // Execute step loop
         String previousThreadLabel = null;
         for (int stepNum = 0; stepNum < 100; stepNum++) {
             fDebugViewListener.reset();
 
             resumeAndWaitForSuspend(fThreadCtx, IRunControl.RM_STEP_INTO_LINE);
 
             fDebugViewListener.waitTillFinished(MODEL_CHANGED_COMPLETE | CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
             VirtualItem topFrameItem = fDebugViewListener.findElement(
                 new Pattern[] { Pattern.compile(".*"), Pattern.compile(".*"), Pattern.compile(".*" + fProcessId + ".*\\(Step.*"), Pattern.compile(".*")});
             Assert.assertTrue(topFrameItem != null);
             String topFrameLabel = ((String[])topFrameItem.getData(VirtualItem.LABEL_KEY))[0];
             Assert.assertTrue(!topFrameLabel.equals(previousThreadLabel));
             previousThreadLabel = topFrameLabel;
         }
     }
 
     public void testSteppingWithVariablesAndRegisters() throws Exception {
         fVariablesViewViewer.setActive(true);
         fRegistersViewViewer.setActive(true);
 
         initProcessModel("tcf_test_func0");
 
 
 
         // Execute step loop
         String previousThreadLabel = null;
         for (int stepNum = 0; stepNum < 100; stepNum++) {
             fDebugViewListener.reset();
             fVariablesViewListener.reset();
             fRegistersViewListener.reset();
 
             resumeAndWaitForSuspend(fThreadCtx, IRunControl.RM_STEP_INTO_LINE);
 
             fDebugViewListener.waitTillFinished(MODEL_CHANGED_COMPLETE | CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
             fVariablesViewListener.waitTillFinished(CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
             fRegistersViewListener.waitTillFinished(CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
             VirtualItem topFrameItem = fDebugViewListener.findElement(
                 new Pattern[] { Pattern.compile(".*"), Pattern.compile(".*"), Pattern.compile(".*" + fProcessId + ".*\\(Step.*"), Pattern.compile(".*")});
             Assert.assertTrue(topFrameItem != null);
             String topFrameLabel = ((String[])topFrameItem.getData(VirtualItem.LABEL_KEY))[0];
             Assert.assertTrue(!topFrameLabel.equals(previousThreadLabel));
             previousThreadLabel = topFrameLabel;
         }
     }
 
 /** DISABLED: Hangs on execution on szg-build
     public void testSteppingPerformanceWithSourceDisplay() throws Exception {
         initProcessModel("tcf_test_func0");
 
         final Number sym_func0_address = new Transaction<Number>() {
             protected Number process() throws Transaction.InvalidCacheException ,ExecutionException {
                 return validate( fDiagnosticsCM.getSymbol(fProcessId, "tcf_test_func0") ).getValue();
             };
         }.get();
 
         final Number sym_func3_address = new Transaction<Number>() {
             protected Number process() throws Transaction.InvalidCacheException ,ExecutionException {
                 return validate( fDiagnosticsCM.getSymbol(fProcessId, "tcf_test_func3") ).getValue();
             };
         }.get();
 
         Performance perf = Performance.getDefault();
         PerformanceMeter meter = perf.createPerformanceMeter(perf.getDefaultScenarioId(this));
 
         try {
             // Execute step loop
             for (int stepNum = 0; stepNum < 100; stepNum++) {
                 fDebugViewListener.reset();
                 fSourceDisplayListener.reset();
 
                 meter.start();
 
                 ContextState state = resumeAndWaitForSuspend(fThreadCtx, IRunControl.RM_STEP_INTO_LINE);
 
                 CodeArea area = calcPCCodeArea();
                 if (area != null) {
                     fSourceDisplayListener.setCodeArea(calcPCCodeArea());
                 }
 
                 fDebugViewListener.waitTillFinished(MODEL_CHANGED_COMPLETE | CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
                 if (area != null) {
                     fSourceDisplayListener.waitTillFinished();
                 }
 
                 meter.stop();
 
                 if (new BigInteger(state.pc).equals(new BigInteger(sym_func3_address.toString()))) {
                     moveToLocation(fThreadId, sym_func0_address);
                 }
 
             }
 
             meter.commit();
             perf.assertPerformance(meter);
         } finally {
             meter.dispose();
         }
 
     }
 */
 
     private CodeArea calcPCCodeArea() throws ExecutionException, InterruptedException {
         return new Transaction<CodeArea>() {
             @Override
             protected CodeArea process() throws Transaction.InvalidCacheException ,ExecutionException {
                 String pc = validate(fRunControlCM.getState(fThreadId)).pc;
                 BigInteger pcNumber = new BigInteger(pc);
                 BigInteger pcNumberPlusOne = pcNumber.add(BigInteger.valueOf(1));
                 CodeArea[] areas = validate(fLineNumbersCM.mapToSource(fThreadId, pcNumber, pcNumberPlusOne));
                 if (areas.length >= 1) {
                     return areas[0];
                 }
                 return null;
             }
         }.get();
     }
 
     public void testSymbolsCMResetOnContextRemove() throws Exception {
         initProcessModel("tcf_test_func0");
 
         // Retrieve the current PC for use later
         final String pc = new Transaction<String>() {
             @Override
             protected String process() throws InvalidCacheException, ExecutionException {
                 return validate(fRunControlCM.getState(fThreadId)).pc;
             }
         }.get();
 
         // Find symbol by name and valide the cache.
         final String symbolId = new Transaction<String>() {
             @Override
             protected String process() throws InvalidCacheException, ExecutionException {
                 String symId = validate( fSymbolsCM.find(fProcessId, new BigInteger(pc), "tcf_test_func0") );
                 Symbol sym = validate( fSymbolsCM.getContext(symId) );
                 Assert.assertEquals(ISymbols.UPDATE_ON_MEMORY_MAP_CHANGES, sym.getUpdatePolicy());
                 return symId;
             }
         }.get();
 
         // Find symbol by address and validate its context.  Save address for later.
         final Number symAddr = new Transaction<Number>() {
             @Override
             protected Number process() throws InvalidCacheException, ExecutionException {
                 Symbol sym = validate( fSymbolsCM.getContext(symbolId) );
                 String symId2 = validate( fSymbolsCM.findByAddr(fProcessId, sym.getAddress()) );
                 Symbol sym2 = validate( fSymbolsCM.getContext(symId2) );
                 Assert.assertEquals(sym.getAddress(), sym2.getAddress());
                 return sym.getAddress();
             }
         }.get();
 
         // End test, check that all caches were reset and now return an error.
         new Transaction<String>() {
             @Override
             protected String process() throws InvalidCacheException, ExecutionException {
                 validate( fDiagnosticsCM.cancelTest(fTestId, this) );
                 validate( fRunControlCM.waitForContextRemoved(fProcessId, this) );
                 try {
                     validate( fSymbolsCM.getContext(symbolId) );
                     Assert.fail("Expected error");
                 } catch (ExecutionException e) {}
                 try {
                     validate( fSymbolsCM.find(fProcessId, new BigInteger(pc), "tcf_test_func0") );
                     Assert.fail("Expected error");
                 } catch (ExecutionException e) {}
                 try {
                     validate( fSymbolsCM.findByAddr(fProcessId, symAddr) );
                     Assert.fail("Expected error");
                 } catch (ExecutionException e) {}
 
                 return null;
             }
         }.get();
     }
 
     public void testLineNumbersCMResetOnContextRemove() throws Exception {
         initProcessModel("tcf_test_func0");
 
         // Retrieve the current PC for use later
         final String pc = new Transaction<String>() {
             @Override
             protected String process() throws InvalidCacheException, ExecutionException {
                 return validate(fRunControlCM.getState(fThreadId)).pc;
             }
         }.get();
 
         final BigInteger pcNumber = new BigInteger(pc);
         final BigInteger pcNumberPlusOne = pcNumber.add(BigInteger.valueOf(1));
 
         // Retrieve the line number for current PC.
         final CodeArea[] pcCodeAreas = new Transaction<CodeArea[]>() {
             @Override
             protected CodeArea[] process() throws InvalidCacheException, ExecutionException {
                 CodeArea[] areas = validate(fLineNumbersCM.mapToSource(fProcessId, pcNumber, pcNumberPlusOne));
                 Assert.assertNotNull(areas);
                 Assert.assertTrue(areas.length != 0);
 
                 areas = validate(fLineNumbersCM.mapToSource(fThreadId, pcNumber, pcNumberPlusOne));
                 Assert.assertNotNull(areas);
                 Assert.assertTrue(areas.length != 0);
 
                 CodeArea[] areas2 = validate(fLineNumbersCM.mapToMemory(fProcessId, areas[0].file, areas[0].start_line, areas[0].start_column));
                 Assert.assertNotNull(areas2);
                 Assert.assertTrue(areas2.length != 0);
 
                 areas2 = validate(fLineNumbersCM.mapToMemory(fThreadId, areas[0].file, areas[0].start_line, areas[0].start_column));
                 Assert.assertNotNull(areas2);
                 Assert.assertTrue(areas2.length != 0);
 
                 return areas;
             }
         }.get();
 
         // End test, check that all caches were reset and now return an error.
         new Transaction<String>() {
             @Override
             protected String process() throws InvalidCacheException, ExecutionException {
                 validate( fDiagnosticsCM.cancelTest(fTestId, this) );
                 validate( fRunControlCM.waitForContextRemoved(fProcessId, this) );
                 try {
                     validate(fLineNumbersCM.mapToSource(fProcessId, pcNumber, pcNumberPlusOne));
                     Assert.fail("Expected error");
                 } catch (ExecutionException e) {}
                 try {
                     validate(fLineNumbersCM.mapToSource(fThreadId, pcNumber, pcNumberPlusOne));
                     Assert.fail("Expected error");
                 } catch (ExecutionException e) {}
                 try {
                     CodeArea[] areas3 = validate(fLineNumbersCM.mapToMemory(fProcessId, pcCodeAreas[0].file, pcCodeAreas[0].start_line, pcCodeAreas[0].start_column));
                     Assert.fail("Expected error");
                 } catch (ExecutionException e) {}
                 try {
                     validate(fLineNumbersCM.mapToMemory(fThreadId, pcCodeAreas[0].file, pcCodeAreas[0].start_line, pcCodeAreas[0].start_column));
                     Assert.fail("Expected error");
                 } catch (ExecutionException e) {}
 
                 return null;
             }
         }.get();
     }
 
 
     public void testSymbolsCMResetOnContextStateChange() throws Exception {
         initProcessModel("tcf_test_func2");
 
         // Retrieve the current PC and top frame for use later
         final String pc = new Transaction<String>() {
             @Override
             protected String process() throws InvalidCacheException, ExecutionException {
                 return validate(fRunControlCM.getState(fThreadId)).pc;
             }
         }.get();
         final String topFrameId = new Transaction<String>() {
             @Override
             protected String process() throws InvalidCacheException, ExecutionException {
                 String[] frameIds = validate( fStackTraceCM.getChildren(fThreadId) );
                 return frameIds[frameIds.length - 1];
             }
         }.get();
 
         // Find symbol by name and valide the cache.
         final String symbolId = new Transaction<String>() {
             @Override
             protected String process() throws InvalidCacheException, ExecutionException {
                 String symId = validate( fSymbolsCM.find(topFrameId, new BigInteger(pc), "func2_local1") );
                 Symbol sym = validate( fSymbolsCM.getContext(symId) );
                 Assert.assertEquals(ISymbols.UPDATE_ON_EXE_STATE_CHANGES, sym.getUpdatePolicy());
                 return symId;
             }
         }.get();
 
         // Note: findByAddr doesn't seem to work on a local var.
 //        // Find symbol by address and validate its context.  Save address for later.
 //        final Number symAddr = new Transaction<Number>() {
 //            @Override
 //            protected Number process() throws InvalidCacheException, ExecutionException {
 //                Symbol sym = validate( fSymbolsCM.getContext(symbolId) );
 //                String symId2 = validate( fSymbolsCM.findByAddr(topFrameId, sym.getAddress()) );
 //                Symbol sym2 = validate( fSymbolsCM.getContext(symId2) );
 //                Assert.assertEquals(sym.getAddress(), sym2.getAddress());
 //                return sym.getAddress();
 //            }
 //        }.get();
 
         // Execute a step.
         resumeAndWaitForSuspend(fThreadCtx, IRunControl.RM_STEP_OUT);
 
         // End test, check that all caches were reset and now return an error.
         new Transaction<Object>() {
             @Override
             protected Object process() throws InvalidCacheException, ExecutionException {
                 Assert.assertFalse(
                     "Expected cache to be reset",
                     fSymbolsCM.getContext(symbolId).isValid());
                 Assert.assertFalse(
                     "Expected cache to be reset",
                     fSymbolsCM.find(topFrameId, new BigInteger(pc), "func2_local1").isValid() );
                 return null;
             }
         }.get();
     }
 
     public void testStackTraceCMResetOnContextStateChange() throws Exception {
         initProcessModel("tcf_test_func2");
 
         // Retrieve the current PC and top frame for use later
         final String pc = new Transaction<String>() {
             @Override
             protected String process() throws InvalidCacheException, ExecutionException {
                 return validate(fRunControlCM.getState(fThreadId)).pc;
             }
         }.get();
 
         // Retrieve data from caches (make them valid).
         new Transaction<Object>() {
             @Override
             protected String process() throws InvalidCacheException, ExecutionException {
                 String[] frameIds = validate( fStackTraceCM.getChildren(fThreadId) );
                 validate (fStackTraceCM.getContexts(frameIds));
                 RangeCache<StackTraceContext> framesRange = fStackTraceCM.getContextRange(fThreadId);
                 validate( framesRange.getRange(0, frameIds.length) );
                 return null;
             }
         }.get();
 
 
         // Execute a step.
         resumeAndWaitForSuspend(fThreadCtx, IRunControl.RM_STEP_OUT);
 
         // End test, check that all caches were reset and now return an error.
         new Transaction<Object>() {
             @Override
             protected Object process() throws InvalidCacheException, ExecutionException {
                 ICache<String[]> frameIdsCache = fStackTraceCM.getChildren(fThreadId);
                 Assert.assertFalse("Expected cache to be reset", frameIdsCache.isValid());
                 return null;
             }
         }.get();
 
         new Transaction<Object>() {
             @Override
             protected Object process() throws InvalidCacheException, ExecutionException {
                 String[] frameIds = validate( fStackTraceCM.getChildren(fThreadId) );
                 ICache<StackTraceContext[]> cache = fStackTraceCM.getContexts(frameIds);
                 Assert.assertFalse("Expected cache to be reset", cache.isValid());
 
                 RangeCache<StackTraceContext> framesRange = fStackTraceCM.getContextRange(fThreadId);
                 ICache<List<StackTraceContext>> framesRangeCache = framesRange.getRange(0, frameIds.length);
                 Assert.assertFalse("Expected cache to be reset", framesRangeCache.isValid());
 
                 return null;
             }
         }.get();
     }
 
     public void testRunControlCMChildrenInvalidation() throws Exception {
         initProcessModel("tcf_test_func0");
 
         createBreakpoint("testRunControlCMChildrenInvalidation", "tcf_test_func0");
 
         // Wait for each threads to start.
         final String[] threads = new Transaction<String[]>() {
             List<String> fThreads = new ArrayList<String>();
             @Override
             protected String[] process() throws InvalidCacheException, ExecutionException {
                 IWaitForEventCache<RunControlContext[]> waitCache = fRunControlCM.waitForContextAdded(fProcessId, this);
                 validate(fRunControlCM.resume(fTestCtx, this, IRunControl.RM_RESUME, 1));
                 RunControlContext[] addedContexts = validate(waitCache);
                 for (RunControlContext addedContext : addedContexts) {
                     fThreads.add(addedContext.getID());
                 }
                 if (fThreads.size() < 4) {
                     waitCache.reset();
                     validate(waitCache);
                 }
                 // Validate children cache
                 String[] children = validate (fRunControlCM.getChildren(fProcessId));
                 Assert.assertTrue(
                     "Expected children array to contain added ids",
                     Arrays.asList(children).containsAll(fThreads));
 
                 return fThreads.toArray(new String[fThreads.size()]);
             }
         }.get();
 
         // Wait for each thread to suspend, update caches
         for (final String thread : threads) {
             new Transaction<Object>() {
                 @Override
                 protected Object process() throws InvalidCacheException, ExecutionException {
                     RunControlCM.ContextState state = validate(fRunControlCM.getState(thread));
                     if (!state.suspended) {
                         validate( fRunControlCM.waitForContextSuspended(thread, this) );
                     }
                     String symId = validate( fSymbolsCM.find(thread, new BigInteger(state.pc), "tcf_test_func0") );
                     Number symAddr = validate( fSymbolsCM.getContext(symId) ).getAddress();
                     Assert.assertEquals("Expected thread to suspend at breakpoint address", symAddr.toString(), state.pc);
                     String[] children = validate( fRunControlCM.getChildren(thread));
                     Assert.assertEquals("Expected thread to have no children contexts", 0, children.length);
                     return null;
                 }
             }.get();
         }
 
         // End test, check for remvoed events and that state caches were cleared
         new Transaction<String>() {
             @Override
             protected String process() throws InvalidCacheException, ExecutionException {
                 // Create wait caches
                 fRunControlCM.waitForContextRemoved(fProcessId, this);
                 IWaitForEventCache<?>[] waitCaches = new IWaitForEventCache<?>[threads.length];
                 for (int i = 0; i < threads.length; i++) {
                     waitCaches[i] = fRunControlCM.waitForContextRemoved(threads[i], this);
                 }
                 validate( fDiagnosticsCM.cancelTest(fTestId, this) );
                 validate(waitCaches);
                 validate(fRunControlCM.waitForContextRemoved(fProcessId, this));
 
                 try {
                     validate( fRunControlCM.getContext(fProcessId) );
                     Assert.fail("Expected error");
                 } catch (ExecutionException e) {}
                 try {
                     validate( fRunControlCM.getState(fProcessId) );
                     Assert.fail("Expected error");
                 } catch (ExecutionException e) {}
                 try {
                     String children[] = validate( fRunControlCM.getChildren(fProcessId) );
                     Assert.assertEquals("Expected no children", 0, children.length);
                 } catch (ExecutionException e) {}
 
                 for (String thread : threads) {
                     try {
                         validate( fRunControlCM.getContext(thread) );
                         Assert.fail("Expected error");
                     } catch (ExecutionException e) {}
                     try {
                         validate( fRunControlCM.getState(thread) );
                         Assert.fail("Expected error");
                     } catch (ExecutionException e) {}
                     try {
                         String children[] = validate( fRunControlCM.getChildren(fProcessId) );
                         Assert.assertEquals("Expected no children", 0, children.length);
                     } catch (ExecutionException e) {}
                 }
 
                 return null;
             }
         }.get();
 
         removeBreakpoint("testRunControlCMChildrenInvalidation");
 
     }
 
 }
