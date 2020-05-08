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
 
 import java.math.BigInteger;
 import java.util.concurrent.ExecutionException;
 import java.util.regex.Pattern;
 
 import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualItem;
 import org.eclipse.tcf.debug.test.services.RunControlCM.ContextState;
 import org.eclipse.tcf.debug.test.util.Transaction;
 import org.eclipse.tcf.services.ILineNumbers.CodeArea;
 import org.eclipse.tcf.services.IRunControl;
 import org.eclipse.tcf.services.ISymbols;
 import org.eclipse.tcf.services.ISymbols.Symbol;
 import org.eclipse.test.performance.Performance;
 import org.eclipse.test.performance.PerformanceMeter;
 import org.junit.Assert;
 
 @SuppressWarnings("restriction")
 public class SampleTest extends AbstractTcfUITest {
 
 
     public void testDebugViewContent() throws Exception {
         TestProcessInfo processInfo = initProcessModel("tcf_test_func0");
 
         VirtualItem launchItem = fDebugViewListener.findElement(new Pattern[] { Pattern.compile(".*" + fLaunch.getLaunchConfiguration().getName() + ".*") }  );
         Assert.assertTrue(launchItem != null);
 
         VirtualItem processItem = fDebugViewListener.findElement(launchItem, new Pattern[] { Pattern.compile(".*agent.*") }  );
         if (processItem == null) {
             /* Windows? */
             processItem = fDebugViewListener.findElement(launchItem, new Pattern[] { Pattern.compile("P[0-9]*") }  );
         }
         Assert.assertTrue(processItem != null);
 
         VirtualItem threadItem = fDebugViewListener.findElement(processItem, new Pattern[] { Pattern.compile(".*" + processInfo.fThreadId + ".*") }  );
         Assert.assertTrue(threadItem != null);
         VirtualItem frameItem = fDebugViewListener.findElement(threadItem, new Pattern[] { Pattern.compile(".*tcf_test_func0.*")});
         Assert.assertTrue(frameItem != null);
     }
 
     public void testSteppingDebugViewOnly() throws Exception {
         final TestProcessInfo processInfo = initProcessModel("tcf_test_func0");
 
         // Execute step loop
         String previousThreadLabel = null;
         for (int stepNum = 0; stepNum < 100; stepNum++) {
             fDebugViewListener.reset();
 
             resumeAndWaitForSuspend(processInfo.fThreadCtx, IRunControl.RM_STEP_INTO_LINE);
 
             fDebugViewListener.waitTillFinished(MODEL_CHANGED_COMPLETE | CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
             VirtualItem topFrameItem = fDebugViewListener.findElement(
                 new Pattern[] { Pattern.compile(".*"), Pattern.compile(".*"), Pattern.compile(".*" + processInfo.fProcessId + ".*\\(Step.*"), Pattern.compile(".*")});
             if (topFrameItem == null) {
                 Assert.fail("Top stack frame not found. \n\nDebug view dump: \n:" + fDebugViewViewer.toString());
             }
             String topFrameLabel = ((String[])topFrameItem.getData(VirtualItem.LABEL_KEY))[0];
             Assert.assertTrue(!topFrameLabel.equals(previousThreadLabel));
             previousThreadLabel = topFrameLabel;
         }
     }
 
     public void testSteppingWithVariablesAndRegisters() throws Exception {
         fVariablesViewViewer.setActive(true);
         fRegistersViewViewer.setActive(true);
 
         TestProcessInfo processInfo = initProcessModel("tcf_test_func0");
 
         // Execute step loop
         String previousThreadLabel = null;
         for (int stepNum = 0; stepNum < 100; stepNum++) {
             fDebugViewListener.reset();
             fVariablesViewListener.reset();
             fRegistersViewListener.reset();
 
             resumeAndWaitForSuspend(processInfo.fThreadCtx, IRunControl.RM_STEP_INTO_LINE);
 
             fDebugViewListener.waitTillFinished(MODEL_CHANGED_COMPLETE | CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
             fVariablesViewListener.waitTillFinished(CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
             fRegistersViewListener.waitTillFinished(CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
             VirtualItem topFrameItem = fDebugViewListener.findElement(
                 new Pattern[] { Pattern.compile(".*"), Pattern.compile(".*"), Pattern.compile(".*" + processInfo.fProcessId + ".*\\(Step.*"), Pattern.compile(".*")});
             Assert.assertTrue(topFrameItem != null);
             String topFrameLabel = ((String[])topFrameItem.getData(VirtualItem.LABEL_KEY))[0];
             Assert.assertTrue(!topFrameLabel.equals(previousThreadLabel));
             previousThreadLabel = topFrameLabel;
         }
     }
 
     public void testSteppingPerformanceWithSourceDisplay() throws Exception {
         final TestProcessInfo processInfo = initProcessModel("tcf_test_func0");
 
         final Number sym_func0_address = new Transaction<Number>() {
             @Override
             protected Number process() throws Transaction.InvalidCacheException ,ExecutionException {
                 return validate( fDiagnosticsCM.getSymbol(processInfo.fProcessId, "tcf_test_func0") ).getValue();
             };
         }.get();
 
         final Number sym_func3_address = new Transaction<Number>() {
             @Override
             protected Number process() throws Transaction.InvalidCacheException ,ExecutionException {
                 return validate( fDiagnosticsCM.getSymbol(processInfo.fProcessId, "tcf_test_func3") ).getValue();
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
 
                 ContextState state = resumeAndWaitForSuspend(processInfo.fThreadCtx, IRunControl.RM_STEP_INTO_LINE);
 
                 CodeArea area = calcPCCodeArea(processInfo);
                 if (area != null) {
                     fSourceDisplayListener.setCodeArea(calcPCCodeArea(processInfo));
                 }
 
                 fDebugViewListener.waitTillFinished(MODEL_CHANGED_COMPLETE | CONTENT_SEQUENCE_COMPLETE | LABEL_UPDATES_RUNNING);
                 if (area != null) {
                     fSourceDisplayListener.waitTillFinished();
                 }
 
                 meter.stop();
 
                 if (new BigInteger(state.pc).equals(new BigInteger(sym_func3_address.toString()))) {
                     moveToLocation(processInfo.fThreadId, sym_func0_address);
                 }
 
             }
 
             meter.commit();
             perf.assertPerformance(meter);
         } finally {
             meter.dispose();
         }
 
     }
 
 
     private CodeArea calcPCCodeArea(final TestProcessInfo processInfo) throws ExecutionException, InterruptedException {
         return new Transaction<CodeArea>() {
             @Override
             protected CodeArea process() throws Transaction.InvalidCacheException ,ExecutionException {
                 String pc = validate(fRunControlCM.getState(processInfo.fThreadId)).pc;
                 BigInteger pcNumber = new BigInteger(pc);
                 BigInteger pcNumberPlusOne = pcNumber.add(BigInteger.valueOf(1));
                 CodeArea[] areas = validate(fLineNumbersCM.mapToSource(processInfo.fThreadId, pcNumber, pcNumberPlusOne));
                if (areas != null && areas.length >= 1) {
                     return areas[0];
                 }
                 return null;
             }
         }.get();
     }
 
     public void testSymbolsCMResetOnContextRemove() throws Exception {
         final TestProcessInfo processInfo = initProcessModel("tcf_test_func0");
 
         // Retrieve the current PC for use later
         final String pc = new Transaction<String>() {
             @Override
             protected String process() throws InvalidCacheException, ExecutionException {
                 return validate(fRunControlCM.getState(processInfo.fThreadId)).pc;
             }
         }.get();
 
         // Find symbol by name and valide the cache.
         final String symbolId = new Transaction<String>() {
             @Override
             protected String process() throws InvalidCacheException, ExecutionException {
                 String symId = validate( fSymbolsCM.find(processInfo.fProcessId, new BigInteger(pc), "tcf_test_func0") );
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
                 String symId2 = validate( fSymbolsCM.findByAddr(processInfo.fProcessId, sym.getAddress()) );
                 Symbol sym2 = validate( fSymbolsCM.getContext(symId2) );
                 Assert.assertEquals(sym.getAddress(), sym2.getAddress());
                 return sym.getAddress();
             }
         }.get();
 
         // End test, check that all caches were reset and now return an error.
         new Transaction<String>() {
             @Override
             protected String process() throws InvalidCacheException, ExecutionException {
                 validate( fDiagnosticsCM.cancelTest(processInfo.fTestId, this) );
                 validate( fRunControlCM.waitForContextRemoved(processInfo.fProcessId, this) );
                 try {
                     validate( fSymbolsCM.getContext(symbolId) );
                     Assert.fail("Expected error");
                 } catch (ExecutionException e) {}
                 try {
                     validate( fSymbolsCM.find(processInfo.fProcessId, new BigInteger(pc), "tcf_test_func0") );
                     Assert.fail("Expected error");
                 } catch (ExecutionException e) {}
                 try {
                     validate( fSymbolsCM.findByAddr(processInfo.fProcessId, symAddr) );
                     Assert.fail("Expected error");
                 } catch (ExecutionException e) {}
 
                 return null;
             }
         }.get();
     }
 
 
     public void testSymbolsCMResetOnContextStateChange() throws Exception {
         final TestProcessInfo processInfo = initProcessModel("tcf_test_func2");
 
         // Retrieve the current PC and top frame for use later
         final String pc = new Transaction<String>() {
             @Override
             protected String process() throws InvalidCacheException, ExecutionException {
                 return validate(fRunControlCM.getState(processInfo.fThreadId)).pc;
             }
         }.get();
         final String topFrameId = new Transaction<String>() {
             @Override
             protected String process() throws InvalidCacheException, ExecutionException {
                 String[] frameIds = validate( fStackTraceCM.getChildren(processInfo.fThreadId) );
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
         resumeAndWaitForSuspend(processInfo.fThreadCtx, IRunControl.RM_STEP_OUT);
 
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
 }
