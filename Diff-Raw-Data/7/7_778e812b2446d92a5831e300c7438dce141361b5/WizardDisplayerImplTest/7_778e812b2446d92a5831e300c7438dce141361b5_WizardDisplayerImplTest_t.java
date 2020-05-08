 /*  The contents of this file are subject to the terms of the Common Development
 and Distribution License (the License). You may not use this file except in
 compliance with the License.
     You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 or http://www.netbeans.org/cddl.txt.
     When distributing Covered Code, include this CDDL Header Notice in each file
 and include the License file at http://www.netbeans.org/cddl.txt.
 If applicable, add the following below the CDDL Header, with the fields
 enclosed by brackets [] replaced by your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]" */
 /*
  * TrivialWizardFactoryTest.java
  * JUnit based test
  *
  * Created on March 4, 2005, 4:33 PM
  */
 
 package org.netbeans.api.wizard.displayer;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import org.netbeans.spi.wizard.WizardController;
 import org.netbeans.spi.wizard.WizardException;
 import org.netbeans.spi.wizard.WizardPanelProvider;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.lang.reflect.Field;
 import java.util.Arrays;
 import java.util.Map;
 import org.netbeans.spi.wizard.DeferredWizardResult;
 import org.netbeans.spi.wizard.ResultProgressHandle;
 import org.netbeans.spi.wizard.Summ;
 import org.netbeans.spi.wizard.Wizard;
 import org.netbeans.spi.wizard.WizardPage;
 import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;
 
 
 /**
  * Tests the default UI for wizards, and, implicitly, a lot of the logic
  * in the support classes.
  *
  * @author Tim Boudreau
  */
 public class WizardDisplayerImplTest extends TestCase {
 
     private static Object wizardResult;
     
     private WizardDisplayerImpl displayer;
 
     public WizardDisplayerImplTest(String testName) {
         super(testName);
     }
 
     public static Test suite() {
         return new TestSuite(WizardDisplayerImplTest.class);
     }
 
     protected void setUp() throws Exception {
         super.setUp();
         wizardResult = null;
         System.setProperty("TrivialWizardFactory.test", "true");
     }
     
     // A help action for testing
     private static class HA extends AbstractAction {
         ActionEvent ae = null;
 
         public void actionPerformed(ActionEvent ae) {
             this.ae = ae;
         }
 
         public void assertPerformed() {
             ActionEvent x = ae;
             ae = null;
             assertNotNull(x);
         }
     }
     
     public void testShow() throws Exception {
         System.out.println("testShow");
 
         PanelProviderImpl impl = new PanelProviderImpl();
         Wizard wiz = impl.createWizard();
 
         HA ha = new HA();
 
         show(wiz, ha);
 
         while (!impl.active) {
             Thread.sleep(200);
         }
 
         NavButtonManager mgr = displayer.getButtonManager();
         JButton next = mgr.getNext();
         JButton prev = mgr.getPrev();
         JButton finish = mgr.getFinish();
         JButton cancel = mgr.getCancel();
         JButton help = mgr.getHelp();
         
         JButton[] buttons = new JButton[5];
         buttons[0] = next;
         buttons[1] = prev;
         buttons[2] = finish;
         buttons[3] = cancel;
         buttons[4] = help;
 
         assertTrue("Help button should be shown", help.isShowing());
 
         click(help);
         ha.assertPerformed();
 
         assertFalse(next.isEnabled());
         assertFalse(prev.isEnabled());
         assertFalse(finish.isEnabled());
         assertTrue(cancel.isEnabled());
 
         click(impl.cb);
         JCheckBox mcb = impl.cb;
         assertTrue(next.isEnabled());
         assertFalse(prev.isEnabled());
         assertFalse(finish.isEnabled());
         assertTrue(cancel.isEnabled());
 
         click(next);
         assertTrue(prev.isEnabled());
         assertFalse(next.isEnabled());
         assertFalse(finish.isEnabled());
         assertTrue(cancel.isEnabled());
         assertEquals("b", impl.cb.getText());
         assertNotSame(impl.cb, mcb);
 
         click(prev);
         assertSame(impl.cb, mcb);
         assertTrue(next.isEnabled());
         assertFalse(prev.isEnabled());
         assertFalse(finish.isEnabled());
         assertTrue(cancel.isEnabled());
         assertEquals("a", impl.cb.getText());
 
         click(impl.cb);
         assertFalse(next.isEnabled());
         assertFalse(prev.isEnabled());
         assertFalse(finish.isEnabled());
         assertTrue(cancel.isEnabled());
 
         click(impl.cb);
         assertTrue(next.isEnabled());
         assertFalse(prev.isEnabled());
         assertFalse(finish.isEnabled());
         assertTrue(cancel.isEnabled());
 
         click(next);
         assertTrue(prev.isEnabled());
         assertFalse(next.isEnabled());
         assertFalse(finish.isEnabled());
         assertTrue(cancel.isEnabled());
         assertEquals("b", impl.cb.getText());
         assertFalse(impl.cb.isSelected());
 
         click(impl.cb);
 
         click(next);
         assertTrue(prev.isEnabled());
         assertFalse(next.isEnabled());
         assertFalse(finish.isEnabled());
         assertTrue(cancel.isEnabled());
         assertEquals("c", impl.cb.getText());
 
         click(impl.cb);
         assertTrue(prev.isEnabled());
         assertFalse(next.isEnabled());
         assertTrue(finish.isEnabled());
         assertTrue(cancel.isEnabled());
         assertEquals("c", impl.cb.getText());
 
         click(prev);
         assertTrue(prev.isEnabled());
         assertTrue(next.isEnabled());
         assertFalse(finish.isEnabled());
         assertTrue(cancel.isEnabled());
         assertEquals("b", impl.cb.getText());
 
         impl.controller.setBusy(true);
         for (int i = 0; i < buttons.length; i++) {
             if (buttons[i] != help) {
                 assertFalse("All navigation buttons should be enabled when " +
                         "wizard is busy, but " + buttons[i].getText() + " is enabled",
                         buttons[i].isEnabled());
             }
         }
         impl.controller.setBusy(false);
         assertTrue("SetBusy(false) should restore former state", prev.isEnabled());
         assertTrue("SetBusy(false) should restore former state", next.isEnabled());
         assertFalse("SetBusy(false) should restore former state", finish.isEnabled());
         assertTrue("SetBusy(false) should restore former state", cancel.isEnabled());
 
         click(impl.cb);
         assertTrue(prev.isEnabled());
         assertFalse(next.isEnabled());
         assertFalse(finish.isEnabled());
         assertTrue(cancel.isEnabled());
         assertEquals("b", impl.cb.getText());
 
         click(impl.cb);
         click(next);
 
         assertTrue(prev.isEnabled());
         assertFalse(next.isEnabled());
         assertTrue(finish.isEnabled());
         assertTrue(cancel.isEnabled());
         assertEquals("c", impl.cb.getText());
 
         click(impl.cb);
         assertFalse(finish.isEnabled());
         assertFalse(next.isEnabled());
 
         click(impl.cb);
         assertFalse(next.isEnabled());
         assertTrue(finish.isEnabled());
 
         setForwardNavigation(WizardController.MODE_CAN_FINISH, impl.controller);
 
         click(finish);
         assertFalse(impl.cb.isShowing());
         assertTrue(impl.finished);
     }
 
 //    public void testManual() throws Exception {
 //        PanelProviderImpl impl = new PanelProviderImpl();
 //        Wizard wiz = impl.createWizard();
 //        show (wiz);
 //        Thread.sleep (40000);
 //    }
 
     public void testCancelCalledWhenCancelButtonPressed() throws Exception {
         System.out.println("testCancelCalledWhenCancelButtonPressed");
 
         PanelProviderImpl impl = new PanelProviderImpl();
         Wizard wiz = impl.createWizard();
 
         show(wiz);
 
         while (!impl.active) {
             Thread.sleep(200);
         }
 
         NavButtonManager mgr = displayer.getButtonManager();
         JButton next = mgr.getNext();
         JButton prev = mgr.getPrev();
         JButton finish = mgr.getFinish();
         JButton cancel = mgr.getCancel();
         
         impl.assertNotCancelled();
         click (next);
         impl.assertNotCancelled();
         click(cancel);
         impl.assertCancelled();
 
         Thread.currentThread().sleep (1000);
         assertFalse (impl.dlg.isVisible());
     }
 
     public void testDialogNotHiddenIfCancelReturnsFalse() throws Exception {
         System.out.println("testDialogNotHiddenIfCancelReturnsFalse");
 
         PanelProviderImpl impl = new PanelProviderImpl();
         Wizard wiz = impl.createWizard();
 
         show(wiz);
 
         while (!impl.active) {
             Thread.sleep(200);
         }
 
         NavButtonManager mgr = displayer.getButtonManager();
         JButton next = mgr.getNext();
         JButton prev = mgr.getPrev();
         JButton finish = mgr.getFinish();
         JButton cancel = mgr.getCancel();
         
         impl.assertNotCancelled();
         impl.shouldCancel = false;
         click(cancel);
         impl.assertCancelled();
 
         Thread.currentThread().sleep (1000);
         
         Thread.currentThread().sleep (1000);
         
         assertTrue (impl.dlg.isVisible());
     }
 
     public void testDialogNotHiddenIfBusyTrue() throws Exception {
         System.out.println("testDialogNotHiddenIfBusyTrue");
 
         PanelProviderImpl impl = new PanelProviderImpl();
         Wizard wiz = impl.createWizard();
 
         show(wiz);
 
         while (!impl.active) {
             Thread.sleep(200);
         }
 
         NavButtonManager mgr = displayer.getButtonManager();
         JButton next = mgr.getNext();
         JButton prev = mgr.getPrev();
         JButton finish = mgr.getFinish();
         JButton cancel = mgr.getCancel();
         
         impl.assertNotCancelled();
         impl.controller.setBusy(true);
 
         click(cancel);
         impl.assertNotCancelled();
 
         Thread.currentThread().sleep (1000);
         assertTrue (impl.dlg.isVisible());
     }
 
     public void testProblemDisappearsOnBackButton() throws Exception {
         System.out.println("testProblemDisappearsOnBackButton");
 
         PanelProviderImpl impl = new PanelProviderImpl();
         Wizard wiz = impl.createWizard();
 
         show(wiz);
 
         while (!impl.active) {
             Thread.sleep(200);
         }
 
         NavButtonManager mgr = displayer.getButtonManager();
         JButton next = mgr.getNext();
         JButton prev = mgr.getPrev();
         JButton finish = mgr.getFinish();
         JButton cancel = mgr.getCancel();
         
         impl.assertCurrent("a");
         assertFalse(next.isEnabled());
         assertFalse(prev.isEnabled());
         assertFalse(finish.isEnabled());
         assertTrue(cancel.isEnabled());
 
         click(impl.cb);
         JCheckBox mcb = impl.cb;
         assertTrue(next.isEnabled());
         assertFalse(prev.isEnabled());
         assertFalse(finish.isEnabled());
         assertTrue(cancel.isEnabled());
 
         click(next);
         impl.assertCurrent("b");
         assertTrue(prev.isEnabled());
         assertFalse(next.isEnabled());
         assertFalse(finish.isEnabled());
         assertTrue(cancel.isEnabled());
         assertEquals("b", impl.cb.getText());
         assertNotSame(impl.cb, mcb);
         click(impl.cb);
         assertTrue(next.isEnabled());
         String problem = "Houston, we have a problem...";
         impl.dontResetProblem = true;
         setProblem(problem, impl.controller);
         impl.assertCurrent("b");
         assertTrue(prev.isEnabled());
         assertFalse(next.isEnabled());
 
         click(prev);
         impl.assertCurrent("a");
         impl.assertRecycledId("a");
         assertTrue(next.isEnabled());
         assertFalse(prev.isEnabled());
 
         click(next);
         impl.assertCurrent("b");
         String[] problems = getKnownProblems(impl);
         assertEquals("Last set problem should still be present " + Arrays.asList(problems), problem, problems[1]);
 
         assertFalse(next.isEnabled());
         assertTrue(prev.isEnabled());
 
         setProblem(null, impl.controller);
         assertTrue(next.isEnabled());
 
         setForwardNavigation(WizardController.MODE_CAN_CONTINUE_OR_FINISH, impl.controller);
 
         assertTrue(finish.isEnabled());
         assertTrue(next.isEnabled());
 
         setProblem("Uh oh...", impl.controller);
 
         assertFalse(finish.isEnabled());
         assertFalse(next.isEnabled());
 
         click(prev);
         assertTrue(next.isEnabled());
 
         click(next);
         impl.assertCurrent("b");
         assertFalse(next.isEnabled());
         assertTrue(prev.isEnabled());
         setProblem(null, impl.controller);
 
         assertTrue(next.isEnabled());
         assertTrue(finish.isEnabled());
 
         click(next);
         impl.assertCurrent("c");
         setProblem(null, impl.controller);
         for (int i = 0; i < problems.length; i++) {
             assertNull("All problems should be null but " + i + " is " + problems[i], problems[i]);
         }
         assertFalse(next.isEnabled());
         assertTrue(finish.isEnabled());
 
         setProblem("Cant do anything", impl.controller);
 
         assertFalse(finish.isEnabled());
         assertFalse(next.isEnabled());
         impl.assertCurrent("c");
         click(prev);
         click(prev);
         impl.assertCurrent("a");
 
         assertTrue(next.isEnabled());
         assertFalse(prev.isEnabled());
 
         setForwardNavigation(WizardController.MODE_CAN_FINISH, impl.controller);
 
         assertFalse(prev.isEnabled());
         assertFalse(next.isEnabled());
         assertTrue(finish.isEnabled());
     }
 
     public void testReflectionHackWorks() {
         System.out.println("testReflectionHackWorks");
         try {
             Field f = WizardPanelProvider.class.getDeclaredField("knownProblems");
         } catch (Exception e) {
             fail("The field 'knownProblems' on WizardPanelProvider has been " +
                     "deleted.  Please update DefaultWizardDisplayerTestto be" +
                     " able to locate the array of known problems.");
         }
     }
     
     public void testWizardNotHiddenIfResultProviderSaysItCantBeCancelled() throws Exception {
         System.out.println("testWizardNotHiddenIfResultProviderSaysItCantBeCancelled");
         PageOne one = new PageOne ();
         PageOne two = new PageOne ("two");
         WRP wrp = new WRP(false);
         Wizard w = WizardPage.createWizard(new WizardPage[] {one, two}, 
                 wrp);
         show (w);
         
         wrp.assertCancelNotCalled();
         
         NavButtonManager mgr = displayer.getButtonManager();
         JButton next = mgr.getNext();
         JButton prev = mgr.getPrev();
         JButton finish = mgr.getFinish();
         JButton cancel = mgr.getCancel();
         assertFalse (next.isEnabled());
         assertFalse (prev.isEnabled());
         assertFalse (finish.isEnabled());
         assertTrue (cancel.isEnabled());
         click (cancel);
         wrp.assertCancelCalled();
         assertTrue (next.isDisplayable()); //still on screen, we should not
         //have hidden the dialog.
        
         click (one.box);
         assertTrue (next.isEnabled());
         click (next);
         assertFalse (one.isDisplayable());
         assertTrue (two.isDisplayable());
         assertFalse (next.isEnabled());
         click (cancel);
         wrp.assertCancelCalled();
         assertTrue (next.isDisplayable());
         click (two.box);
         assertFalse (next.isEnabled());
         assertTrue (finish.isEnabled());
         assertTrue (cancel.isEnabled());
         wrp.assertFinishNotCalled();
         wrp.d.assertNotStarted();
         wrp.d.assertNotAborted();
        
         click (finish);
         wrp.assertFinishCalled();
         Thread.currentThread().sleep (1000);
        
         wrp.d.assertStarted();
         assertFalse (cancel.isEnabled());
         synchronized (wrp.d) {
             wrp.d.notify();
         }
        
         Thread.currentThread().sleep(200);
         //should be showing the summary page
         assertTrue (next.isDisplayable());
         assertTrue (wrp.d.sum.summaryComponentWasCalled());
         
         click (cancel);
         Thread.currentThread().sleep(300);
         assertTrue (wrp.d.sum.getResultWasCalled());
         
         assertNotNull ("WizardDisplayer.show() should have returned a " +
                 "non-null result", wizardResult);
     }
     
     public void testWizardIsHiddenIfResultProviderSaysItCanBeCancelled() throws Exception {
         System.out.println("testWizardIsHiddenIfResultProviderSaysItCanBeCancelled");
         PageOne one = new PageOne ();
         PageOne two = new PageOne ("two");
         WRP wrp = new WRP(true);
         Wizard w = WizardPage.createWizard(new WizardPage[] {one, two}, 
                 wrp);
         show (w);
         wrp.assertCancelNotCalled();
         
         NavButtonManager mgr = displayer.getButtonManager();
         JButton next = mgr.getNext();
         JButton prev = mgr.getPrev();
         JButton finish = mgr.getFinish();
         JButton cancel = mgr.getCancel();
         
         click (cancel);
         Thread.currentThread().sleep (2000);
         assertFalse (next.isDisplayable());
         wrp.assertCancelCalled();
     }    
     
     public void testCanAbortComputingDeferredResultIfDeferredResultImplAllowsIt() throws Exception {
         System.out.println("testCanAbortComputingDeferredResultIfDeferredResultImplAllowsIt");
         PageOne one = new PageOne ();
         PageOne two = new PageOne ("two");
         WRP wrp = new WRP(true);
         Wizard w = WizardPage.createWizard(new WizardPage[] {one, two}, 
                 wrp);
         show (w);
         wrp.assertCancelNotCalled();
         
         NavButtonManager mgr = displayer.getButtonManager();
         JButton next = mgr.getNext();
         JButton prev = mgr.getPrev();
         JButton finish = mgr.getFinish();
         JButton cancel = mgr.getCancel();
         click (one.box);
         click (next);
         click (two.box);
         wrp.assertCancelNotCalled();
         wrp.assertFinishNotCalled();
         wrp.d.assertNotStarted();
         wrp.d.assertNotAborted();
         assertTrue (finish.isEnabled());
         click (finish);
         Thread.currentThread().sleep(1000);
         wrp.assertFinishCalled();
         wrp.d.assertStarted();
         //Background thread should be stalled in wrp.d.wait().  Try to abort,
         //and then see if it happens.
         click (cancel);
         wrp.d.assertAborted();
         assertNull (wizardResult);
         synchronized (wrp.d) {
             wrp.d.notify();
         }
         Thread.currentThread().sleep (500);
         assertNull ("WizardDisplayer.show() returned a result even though the" +
                 " wizard was aborted while running background object construction",
                 wizardResult);
     }
     
 
     private static String[] getKnownProblems(WizardPanelProvider prov) throws Exception {
         Field f = WizardPanelProvider.class.getDeclaredField("knownProblems");
         f.setAccessible(true);
         return (String[]) f.get(prov);
     }
 
     private void setForwardNavigation(final int val, final WizardController ctl) throws Exception {
         SwingUtilities.invokeAndWait(new Runnable() {
             public void run() {
                 ctl.setForwardNavigationMode(val);
             }
         });
     }
 
     private void setProblem(final String problem, final WizardController ctl) throws Exception {
         SwingUtilities.invokeAndWait(new Runnable() {
             public void run() {
                 ctl.setProblem(problem);
             }
         });
     }
 
     private void show(final Wizard wiz) {
         show(wiz, null);
     }
 
     private void show(final Wizard wiz, final Action helpAction) {
         try {
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     displayer = new WizardDisplayerImpl ();
                     wizardResult = displayer.show(wiz, null, helpAction, null);
                 }
             });
             Thread.sleep(1000);
         } catch (Exception e) {
             fail(e.getMessage());
         }
 
     }
 
     private static void click(final AbstractButton button) {
         try {
             SwingUtilities.invokeAndWait(new Runnable() {
                 public void run() {
                     button.doClick();
                 }
             });
 //            Thread.sleep(500);
         } catch (Exception ie) {
             ie.printStackTrace();
             fail("interrupted");
         }
     }
     
     private static class PageOne extends WizardPage {
         final JCheckBox box = new JCheckBox ("Check me tender, " +
                 "check me sweet, never let me go");
         PageOne() {
             this ("box");
         }
         
         PageOne(String boxName) {
             super (boxName,"First step");
             box.setName (boxName);
             add (box);
         }
         
         public String validateContents (Component c, Object event) {
             return box.isSelected() ? null : "No.";
         }
     }
     
     private class WRP implements WizardResultProducer {
         boolean finishCalled;
         boolean cancelCalled;
         boolean canCancel;
         final D d;
         public WRP() {
             this (true);
         }
         
         public WRP (boolean canCancel) {
             this.canCancel = canCancel;
             d = new D (canCancel);
         }
         
         public Object finish(Map wizardData) throws WizardException {
             finishCalled = true;
             return d;
         }
 
         public boolean cancel(Map settings) {
             cancelCalled = true;
             return canCancel;
         }
         
         public void assertCancelCalled() {
             boolean b = cancelCalled;
             cancelCalled = false;
             assertTrue ("Cancel was not called", b);
         }
         
         public void assertFinishCalled() {
             boolean b = finishCalled;
             finishCalled = false;
             assertTrue ("Finish was not called", b);
         }
         
         public void assertFinishNotCalled() {
             assertFalse("Finish not called", finishCalled);
         }
         
         public void assertCancelNotCalled() {
             assertFalse("Cancel not called", cancelCalled);
         }
     }
     
     private class D extends DeferredWizardResult {
         final boolean canAbort;
         final Summ sum = new Summ ("Done", "done");
         D (boolean canAbort) {
             this.canAbort = canAbort;
         }
         
         D() {
             this (true);
         }
         boolean navigateBack = false;
         boolean started;
         public void start(Map settings, ResultProgressHandle progress) {
             if (EventQueue.isDispatchThread()) {
                 fail ("DeferredWizardResult.start() called from the event " +
                         "thread.  This is illegal.");
             }
             started = true;
             synchronized (this) {
                 System.err.println("D waiting");
                 try {
                     wait();
                 } catch (InterruptedException ex) {
                     ex.printStackTrace();
                 }
                 System.err.println("D continuing");
                 if (!aborted) {
                     progress.finished (sum);
                 } else {
                     progress.failed("Bad", navigateBack);
                 }
             }
         }
         
         
         public void assertStarted() {
             boolean b = started;
             started = false;
             assertTrue ("start was not called", b);
         }
         
         public void assertNotStarted() {
             assertFalse ("start was already called", started);
         }
         
         public void assertNotAborted() {
             assertFalse ("abort was already called", aborted);
         }
         
         public boolean canAbort() {
             return canAbort;
         }
         
         private volatile boolean aborted;
         public void abort() {
             aborted = true;
         }
         
         public void assertAborted() {
             boolean b = aborted;
             aborted = false;
             assertTrue ("abort was not called", b);
         }
     }
     
     private static class PanelProviderImpl extends WizardPanelProvider {
         private boolean finished = false;
         private int step = -1;
         WizardController controller = null;
 
         PanelProviderImpl(java.lang.String title, java.lang.String[] steps, java.lang.String[] descriptions) {
             super(title, steps, descriptions);
         }
 
         PanelProviderImpl() {
             super("Test Wizard", new String[]{"a", "b", "c"}, new String[]{"Step 1", "Step 2", "Step 3"});
         }
 
 
 
         boolean active = false;
         String currId = null;
         JCheckBox cb = null;
         JDialog dlg = null;
         protected JComponent createPanel(final WizardController controller, final java.lang.String id, final java.util.Map settings) {
             step++;
             this.controller = controller;
             JPanel result = new JPanel() {
                 public void addNotify() {
                     super.addNotify();
                     dlg = (JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, this);
                     assertNotNull (dlg);
                 }
             };
             result.setLayout(new BorderLayout());
             cb = new JCheckBox(id);
             cb.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent ae) {
                     controller.setProblem(cb.isSelected() ? null : "problem");
                     settings.put(id, cb.isSelected() ? Boolean.TRUE : Boolean.FALSE);
                 }
             });
 
             result.add(cb, BorderLayout.CENTER);
             controller.setProblem(Boolean.TRUE.equals(settings.get(id)) ? null : "problem");
 
             currId = id;
             result.setName(id);
             settings.put(id, Boolean.TRUE);
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     active = true;
                 }
             });
             return result;
         }
 
         protected java.lang.Object finish(java.util.Map settings) {
             finished = true;
             return "finished";
         }
 
         public void assertCurrent(String id) {
             if (id == null && currId == null) {
                 return;
             } else if ((id == null) != (currId == null)) {
                 fail("Non-match: " + id + ", " + currId);
             } else {
                 assertEquals(id, currId);
             }
         }
 
         private JComponent recycled = null;
         private String recycledId = null;
         private Map recycledSettings = null;
         boolean dontResetProblem = false;
 
         protected void recycleExistingPanel(String id, WizardController controller, Map settings, JComponent panel) {
             recycled = panel;
             recycledId = id;
             currId = id;
             recycledSettings = settings;
             cb = (JCheckBox) panel.getComponents()[0];
             if (!dontResetProblem) {
                 controller.setProblem(cb.isSelected() ? null : "problem");
             }
         }
 
         public void assertRecycledSettingsContains(String key, String value) {
             assertNotNull(recycledSettings);
             assertEquals(value, recycledSettings.get(key));
         }
 
         public void assertRecycled(JComponent panel) {
             assertNotNull(recycled);
             assertSame(panel, recycled);
         }
 
         public void assertRecycledId(String id) {
             assertNotNull(recycledId);
             assertEquals(id, recycledId);
         }
 
         public void clear() {
             recycled = null;
             recycledId = null;
             recycledSettings = null;
         }
 
         public void assertStep(int step, String msg) {
             assertTrue(msg, step == this.step);
         }
 
         public void assertFinished(String msg) {
             assertTrue(msg, finished);
         }
 
         public void assertNotFinished(String msg) {
             assertFalse(msg, finished);
         }
 
         boolean shouldCancel = true;
         private boolean cancelled = false;
 
         public boolean cancel(Map settings) {
             cancelled = true;
             return shouldCancel;
         }
 
         public void assertCancelled () {
             if (!cancelled) {
                 fail ("Cancel was not called");
             }
         }
 
         public void assertNotCancelled() {
             if (cancelled) {
                 fail ("Cancel was called");
             }
         }
     }
 }
