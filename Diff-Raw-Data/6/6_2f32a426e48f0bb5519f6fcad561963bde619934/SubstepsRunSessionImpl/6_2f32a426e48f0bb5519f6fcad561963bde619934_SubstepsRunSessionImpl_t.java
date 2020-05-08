 package com.technophobia.substeps.junit.ui;
 
 import java.io.File;
 import java.text.DateFormat;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.ILaunchesListener2;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 
 import com.technophobia.eclipse.launcher.config.SubstepsLaunchConfigurationConstants;
 import com.technophobia.eclipse.transformer.Callback1;
 import com.technophobia.eclipse.transformer.Supplier;
 import com.technophobia.eclipse.transformer.Transformer;
 import com.technophobia.substeps.FeatureRunnerPlugin;
 import com.technophobia.substeps.model.RemoteTestRunnerClient;
 import com.technophobia.substeps.model.SubstepsRunListener;
 import com.technophobia.substeps.model.SubstepsSessionListener;
 import com.technophobia.substeps.model.TestRunListenerAdapter;
 import com.technophobia.substeps.model.serialize.SubstepsModelExporter;
 import com.technophobia.substeps.model.structure.IncompleteParentItem;
 import com.technophobia.substeps.model.structure.LinkedParentItemManager;
 import com.technophobia.substeps.model.structure.PredicatedLinkedParentItemManager;
 import com.technophobia.substeps.model.structure.Result;
 import com.technophobia.substeps.model.structure.Status;
 import com.technophobia.substeps.model.structure.SubstepsTestElement;
 import com.technophobia.substeps.model.structure.SubstepsTestElementFactory;
 import com.technophobia.substeps.model.structure.SubstepsTestLeafElement;
 import com.technophobia.substeps.model.structure.SubstepsTestParentElement;
 import com.technophobia.substeps.model.structure.SubstepsTestRootElement;
 
 public class SubstepsRunSessionImpl implements SubstepsRunSession, TestRunStats {
 
     /**
      * The launch, or <code>null</code> iff this session was run externally.
      */
     private final ILaunch launch;
     private final String testRunName;
     /**
      * Java project, or <code>null</code>.
      */
     private final IJavaProject project;
 
     private final String testRunnerKind;
 
     /**
      * Test runner client or <code>null</code>.
      */
     private RemoteTestRunnerClient testRunnerClient;
 
     private final ListenerList sessionListeners;
 
     /**
      * The model root, or <code>null</code> if swapped to disk.
      */
     private SubstepsTestRootElement testRoot;
 
     /**
      * The test run session's cached result, or <code>null</code> if
      * <code>fTestRoot != null</code>.
      */
     private Result testResult;
 
     /**
      * Map from testId to testElement.
      */
     private HashMap<String, SubstepsTestElement> idToTest;
 
     /**
      * The Parent items for which additional children are expected.
      */
     private final LinkedParentItemManager<IncompleteParentItem> incompleteParentItems;
 
     /**
      * Suite for unrooted test case elements, or <code>null</code>.
      */
     private SubstepsTestParentElement unrootedSuite;
 
     /**
      * Number of tests started during this test run.
      */
     volatile int startedCount;
     /**
      * Number of tests ignored during this test run.
      */
     volatile int ignoredCount;
     /**
      * Number of errors during this test run.
      */
     volatile int errorCount;
     /**
      * Number of failures during this test run.
      */
     volatile int failureCount;
     /**
      * Total number of tests to run.
      */
     volatile int totalCount;
     /**
      * <ul>
      * <li>If &gt; 0: Start time in millis</li>
      * <li>If &lt; 0: Unique identifier for imported test run</li>
      * <li>If = 0: Session not started yet</li>
      * </ul>
      */
     volatile long startTime;
     volatile boolean isRunning;
 
     volatile boolean fIsStopped;
     private final SubstepsTestElementFactory testElementFactory;
 
 
     /**
      * Creates a test run session.
      * 
      * @param testRunName
      *            name of the test run
      * @param project
      *            may be <code>null</code>
      */
     public SubstepsRunSessionImpl(final String testRunName, final SubstepsTestElementFactory testElementFactory,
             final IJavaProject project) {
         // TODO: check assumptions about non-null fields
 
         this.testElementFactory = testElementFactory;
         this.launch = null;
         this.project = project;
         this.startTime = -System.currentTimeMillis();
 
         Assert.isNotNull(testRunName);
         this.testRunName = testRunName;
         testRunnerKind = null;
 
         testRoot = new SubstepsTestRootElement(this);
         idToTest = new HashMap<String, SubstepsTestElement>();
         this.incompleteParentItems = new PredicatedLinkedParentItemManager<IncompleteParentItem>(testRootSupplier(),
                 decrementRemainingChildItemsCallback(), checkRemainingChildItemsPredicate());
 
         testRunnerClient = null;
 
         sessionListeners = new ListenerList();
     }
 
 
     public SubstepsRunSessionImpl(final ILaunch launch, final SubstepsTestElementFactory testElementFactory,
             final IJavaProject project, final int port) {
         this.testElementFactory = testElementFactory;
         Assert.isNotNull(launch);
 
         this.launch = launch;
         this.project = project;
 
         final ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
         if (launchConfiguration != null) {
             testRunName = launchConfiguration.getName();
             testRunnerKind = testRunnerKind(launchConfiguration);
         } else {
             testRunName = project.getElementName();
             testRunnerKind = null;
         }
 
         testRoot = new SubstepsTestRootElement(this);
         idToTest = new HashMap<String, SubstepsTestElement>();
         this.incompleteParentItems = new PredicatedLinkedParentItemManager<IncompleteParentItem>(testRootSupplier(),
                 decrementRemainingChildItemsCallback(), checkRemainingChildItemsPredicate());
 
         testRunnerClient = new RemoteTestRunnerClient();
         testRunnerClient.startListening(new SubstepsRunListener[] { new TestSessionNotifier() }, port);
 
         final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
         launchManager.addLaunchListener(new ILaunchesListener2() {
             @Override
             public void launchesTerminated(final ILaunch[] launches) {
                 if (Arrays.asList(launches).contains(launch)) {
                     if (testRunnerClient != null) {
                         testRunnerClient.stopWaiting();
                     }
                     launchManager.removeLaunchListener(this);
                 }
             }
 
 
             @Override
             public void launchesRemoved(final ILaunch[] launches) {
                 if (Arrays.asList(launches).contains(launch)) {
                     if (testRunnerClient != null) {
                         testRunnerClient.stopWaiting();
                     }
                     launchManager.removeLaunchListener(this);
                 }
             }
 
 
             @Override
             public void launchesChanged(final ILaunch[] launches) {
             }
 
 
             @Override
             public void launchesAdded(final ILaunch[] launches) {
             }
         });
 
         sessionListeners = new ListenerList();
         addTestSessionListener(new TestRunListenerAdapter(this));
     }
 
 
     private String testRunnerKind(final ILaunchConfiguration launchConfiguration) {
         try {
             return launchConfiguration.getAttribute(SubstepsLaunchConfigurationConstants.ATTR_TEST_RUNNER_KIND,
                     (String) null);
         } catch (final CoreException e) {
             return null;
         }
     }
 
 
     @Override
     public void reset() {
         startedCount = 0;
         failureCount = 0;
         errorCount = 0;
         ignoredCount = 0;
         totalCount = 0;
 
         testRoot = new SubstepsTestRootElement(this);
         testResult = null;
         idToTest = new HashMap<String, SubstepsTestElement>();
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.jdt.junit.model.ITestElement#getTestResult(boolean)
      */
     @Override
     public Result getTestResult(final boolean includeChildren) {
         if (testRoot != null) {
             return testRoot.getTestResult(true);
         } else {
             return testResult;
         }
     }
 
 
     @Override
     public int getChildCount() {
         return getTestRoot().getChildCount();
     }
 
 
     @Override
     public SubstepsTestElement[] getChildren() {
         return getTestRoot().getChildren();
     }
 
 
     @Override
     public FailureTrace getFailureTrace() {
         return null;
     }
 
 
     @Override
     public SubstepsRunSession getSubstepsRunSession() {
         return this;
     }
 
 
     @Override
     public synchronized SubstepsTestRootElement getTestRoot() {
         swapIn(); // TODO: TestRoot should stay (e.g. for
                   // getTestRoot().getStatus())
         return testRoot;
     }
 
 
     /*
      * @see org.eclipse.jdt.junit.model.ITestRunSession#getJavaProject()
      */
     @Override
     public IJavaProject getLaunchedProject() {
         return project;
     }
 
 
     @Override
     public String getTestRunnerKind() {
         return testRunnerKind;
     }
 
 
     /**
      * @return the launch, or <code>null</code> iff this session was run
      *         externally
      */
     @Override
     public ILaunch getLaunch() {
         return launch;
     }
 
 
     @Override
     public String getTestRunName() {
         return testRunName;
     }
 
 
     @Override
     public int getErrorCount() {
         return errorCount;
     }
 
 
     @Override
     public int getFailureCount() {
         return failureCount;
     }
 
 
     @Override
     public int getStartedCount() {
         return startedCount;
     }
 
 
     @Override
     public int getIgnoredCount() {
         return ignoredCount;
     }
 
 
     @Override
     public int getTotalCount() {
         return totalCount;
     }
 
 
     @Override
     public long getStartTime() {
         return startTime;
     }
 
 
     @Override
     public TestRunState getState() {
         if (isRunning()) {
             return TestRunState.IN_PROGRESS;
         } else if (isStopped()) {
             return TestRunState.STOPPED;
         }
         return TestRunState.COMPLETE;
     }
 
 
     @Override
     public boolean hasErrorsOrFailures() {
         return getFailureCount() > 0 || getErrorCount() > 0;
     }
 
 
     /**
      * @return <code>true</code> iff the session has been stopped or terminated
      */
     @Override
     public boolean isStopped() {
         return fIsStopped;
     }
 
 
     @Override
     public synchronized void addTestSessionListener(final SubstepsSessionListener listener) {
         swapIn();
         sessionListeners.add(listener);
     }
 
 
     @Override
     public void removeTestSessionListener(final SubstepsSessionListener listener) {
         sessionListeners.remove(listener);
     }
 
 
     @Override
     public synchronized void swapOut() {
         if (testRoot == null)
             return;
         if (isRunning() || isStarting() || isKeptAlive())
             return;
 
         final Object[] listeners = sessionListeners.getListeners();
         for (int i = 0; i < listeners.length; ++i) {
             final SubstepsSessionListener registered = (SubstepsSessionListener) listeners[i];
             if (!registered.acceptsSwapToDisk())
                 return;
         }
 
         try {
 
             final File swapFile = getSwapFile();
 
             SubstepsModelExporter.exportTestRunSession(this, swapFile);
             testResult = testRoot.getTestResult(true);
             testRoot = null;
             testRunnerClient = null;
             idToTest = new HashMap<String, SubstepsTestElement>();
             incompleteParentItems.reset();
             unrootedSuite = null;
 
         } catch (final IllegalStateException e) {
             FeatureRunnerPlugin.log(e);
         } catch (final CoreException e) {
             FeatureRunnerPlugin.log(e);
         }
 
     }
 
 
     @Override
     public boolean isStarting() {
         return getStartTime() == 0 && launch != null && !launch.isTerminated();
     }
 
 
     @Override
     public void removeSwapFile() {
         final File swapFile = getSwapFile();
         if (swapFile.exists())
             swapFile.delete();
     }
 
 
     private File getSwapFile() throws IllegalStateException {
         final File historyDir = FeatureRunnerPlugin.instance().getHistoryDirectory();
         final String isoTime = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS").format(new Date(getStartTime())); //$NON-NLS-1$
         final String swapFileName = isoTime + ".xml"; //$NON-NLS-1$
         return new File(historyDir, swapFileName);
     }
 
 
     public synchronized void swapIn() {
         if (testRoot != null)
             return;
 
         throw new UnsupportedOperationException("Import from disk not supported");
 
         /*
          * try { JUnitModel.importIntoTestRunSession(getSwapFile(), this); }
          * catch (final IllegalStateException e) { JUnitCorePlugin.log(e);
          * testRoot = new SubstepsTestRootElement(this); testResult = null; }
          * catch (final CoreException e) { JUnitCorePlugin.log(e); testRoot =
          * new SubstepsTestRootElement(this); testResult = null; }
          */
     }
 
 
     @Override
     public void stopTestRun() {
         if (isRunning() || !isKeptAlive())
             fIsStopped = true;
         if (testRunnerClient != null)
             testRunnerClient.stopTest();
     }
 
 
     /**
      * @return <code>true</code> iff the runtime VM of this test session is
      *         still alive
      */
     @Override
     public boolean isKeptAlive() {
         if (testRunnerClient != null && launch != null && testRunnerClient.isRunning()
                 && ILaunchManager.DEBUG_MODE.equals(launch.getLaunchMode())) {
             final ILaunchConfiguration config = launch.getLaunchConfiguration();
             try {
                 return config != null
                         && config.getAttribute(SubstepsLaunchConfigurationConstants.ATTR_KEEPRUNNING, false);
             } catch (final CoreException e) {
                 return false;
             }
 
         } else {
             return false;
         }
     }
 
 
     /**
      * @return <code>true</code> iff this session has been started, but not
      *         ended nor stopped nor terminated
      */
     @Override
     public boolean isRunning() {
         return isRunning;
     }
 
 
     /**
      * Reruns the given test method.
      * 
      * @param testId
      *            test id
      * @param className
      *            test class name
      * @param testName
      *            test method name
      * @param launchMode
      *            launch mode, see {@link ILaunchManager}
      * @param buildBeforeLaunch
      *            whether a build should be done before launch
      * @return <code>false</code> iff the rerun could not be started
      * @throws CoreException
      *             if the launch fails
      */
     @Override
     public boolean rerunTest(final String testId, final String className, final String testName,
             final String launchMode, final boolean buildBeforeLaunch) throws CoreException {
         if (isKeptAlive()) {
             final Status status = ((SubstepsTestLeafElement) getTestElement(testId)).getStatus();
             if (status == Status.ERROR) {
                 errorCount--;
             } else if (status == Status.FAILURE) {
                 failureCount--;
             }
             testRunnerClient.rerunTest(testId, className, testName);
             return true;
 
         } else if (launch != null) {
             // run the selected test using the previous launch configuration
             final ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
             if (launchConfiguration != null) {
 
                 String name = className;
                 if (testName != null)
                     name += "." + testName; //$NON-NLS-1$
                 final String configName = MessageFormat.format(
                         SubstepsFeatureMessages.SubstepsFeatureTestRunnerViewPart_configName, name);
                 final ILaunchConfigurationWorkingCopy tmp = launchConfiguration.copy(configName);
                 // fix for bug: 64838 junit view run single test does not use
                 // correct class [JUnit]
                 tmp.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, className);
                 // reset the container
                 tmp.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_TEST_CONTAINER, ""); //$NON-NLS-1$
                 if (testName != null) {
                     tmp.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_TEST_METHOD_NAME, testName);
                     // String args= "-rerun "+testId;
                     // tmp.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
                     // args);
                 }
                 tmp.launch(launchMode, null, buildBeforeLaunch);
                 return true;
             }
         }
 
         return false;
     }
 
 
     @Override
     public SubstepsTestElement getTestElement(final String id) {
         return idToTest.get(id);
     }
 
 
     private SubstepsTestElement addTreeEntry(final String treeEntry) {
 
         final SubstepsTestElement testElement = testElementFactory
                 .createForTestEntryString(treeEntry, parentSupplier());
 
         if (!incompleteParentItems.isEmpty()) {
             incompleteParentItems.processOutstandingChild();
         }
         if (testElement instanceof SubstepsTestParentElement) {
             final SubstepsTestParentElement parentElement = (SubstepsTestParentElement) testElement;
             if (parentElement.getChildCount() > 0)
                incompleteParentItems.addNode(new IncompleteParentItem(parentElement, parentElement.getChildCount()));
         }
 
         idToTest.put(testElement.getId(), testElement);
 
         return testElement;
 
         // SubstepsTestElement testElement =
         // testElementFactory.createForTestEntryString(treeEntry);
         // if(testElement instanceof SubstepsTestParentElement){
         // SubstepsTestParentElement parent = (SubstepsTestParentElement)
         // testElement;
         // if(parent.getchil)
         // }
         //
         // if (incompleteTestSuites.isEmpty()) {
         // return createTestElement(testRoot, id, testName, isSuite, testCount);
         // } else {
         // final int suiteIndex = incompleteTestSuites.size() - 1;
         // final IncompleteTestSuite openSuite =
         // incompleteTestSuites.get(suiteIndex);
         // openSuite.outstandingChildren--;
         // if (openSuite.outstandingChildren <= 0)
         // incompleteTestSuites.remove(suiteIndex);
         // return createTestElement(openSuite.testSuiteElement, id, testName,
         // isSuite, testCount);
         // }
     }
 
     /**
      * An {@link SubstepsRunListener} that listens to events from the
      * {@link RemoteTestRunnerClient} and translates them into high-level model
      * events (broadcasted to {@link SubstepsSessionListener}s).
      */
     private class TestSessionNotifier implements SubstepsRunListener {
 
         @Override
         public void testRunStarted(final int testCount) {
             incompleteParentItems.reset();
 
             startedCount = 0;
             ignoredCount = 0;
             failureCount = 0;
             errorCount = 0;
             totalCount = testCount;
 
             startTime = System.currentTimeMillis();
             isRunning = true;
 
             final Object[] listeners = sessionListeners.getListeners();
             for (int i = 0; i < listeners.length; ++i) {
                 ((SubstepsSessionListener) listeners[i]).sessionStarted();
             }
         }
 
 
         @Override
         public void testRunEnded(final long elapsedTime) {
             isRunning = false;
 
             final Object[] listeners = sessionListeners.getListeners();
             for (int i = 0; i < listeners.length; ++i) {
                 ((SubstepsSessionListener) listeners[i]).sessionEnded(elapsedTime);
             }
         }
 
 
         @Override
         public void testRunStopped(final long elapsedTime) {
             isRunning = false;
             fIsStopped = true;
 
             final Object[] listeners = sessionListeners.getListeners();
             for (int i = 0; i < listeners.length; ++i) {
                 ((SubstepsSessionListener) listeners[i]).sessionStopped(elapsedTime);
             }
         }
 
 
         @Override
         public void testRunTerminated() {
             isRunning = false;
             fIsStopped = true;
 
             final Object[] listeners = sessionListeners.getListeners();
             for (int i = 0; i < listeners.length; ++i) {
                 ((SubstepsSessionListener) listeners[i]).sessionTerminated();
             }
         }
 
 
         @Override
         public void testTreeEntry(final String description) {
             final SubstepsTestElement testElement = addTreeEntry(description);
 
             final Object[] listeners = sessionListeners.getListeners();
             for (int i = 0; i < listeners.length; ++i) {
                 ((SubstepsSessionListener) listeners[i]).testAdded(testElement);
             }
         }
 
 
         private SubstepsTestElement createUnrootedTestElement(final String testId, final String testName) {
             final SubstepsTestParentElement unrootedSuite = getUnrootedSuite();
             final SubstepsTestElement testElement = testElementFactory.createTestElement(unrootedSuite, testId,
                     testName, false, 1);
 
             final Object[] listeners = sessionListeners.getListeners();
             for (int i = 0; i < listeners.length; ++i) {
                 ((SubstepsSessionListener) listeners[i]).testAdded(testElement);
             }
 
             return testElement;
         }
 
 
         private SubstepsTestParentElement getUnrootedSuite() {
             if (unrootedSuite == null) {
                 unrootedSuite = (SubstepsTestParentElement) testElementFactory.createTestElement(testRoot,
                         "-2", SubstepsFeatureMessages.SubstepsRunSession_unrootedTests, true, 0); //$NON-NLS-1$
             }
             return unrootedSuite;
         }
 
 
         @Override
         public void testStarted(final String testId, final String testName) {
             if (startedCount == 0) {
                 final Object[] listeners = sessionListeners.getListeners();
                 for (int i = 0; i < listeners.length; ++i) {
                     ((SubstepsSessionListener) listeners[i]).runningBegins();
                 }
             }
             SubstepsTestElement testElement = getTestElement(testId);
             if (testElement == null) {
                 testElement = createUnrootedTestElement(testId, testName);
             } else if (!(testElement instanceof SubstepsTestLeafElement)) {
                 logUnexpectedTest(testId, testElement);
                 return;
             }
             final SubstepsTestLeafElement testCaseElement = (SubstepsTestLeafElement) testElement;
             setStatus(testCaseElement, Status.RUNNING);
 
             startedCount++;
 
             final Object[] listeners = sessionListeners.getListeners();
             for (int i = 0; i < listeners.length; ++i) {
                 ((SubstepsSessionListener) listeners[i]).testStarted(testCaseElement);
             }
         }
 
 
         @Override
         public void testEnded(final String testId, final String testName) {
             SubstepsTestElement testElement = getTestElement(testId);
             if (testElement == null) {
                 testElement = createUnrootedTestElement(testId, testName);
             } else if (!(testElement instanceof SubstepsTestLeafElement)) {
                 logUnexpectedTest(testId, testElement);
                 return;
             }
             final SubstepsTestLeafElement testCaseElement = (SubstepsTestLeafElement) testElement;
             if (testName.startsWith("@Ignore: ")) {
                 testCaseElement.setIgnored(true);
                 ignoredCount++;
             }
 
             if (testCaseElement.getStatus() == Status.RUNNING)
                 setStatus(testCaseElement, Status.OK);
 
             final Object[] listeners = sessionListeners.getListeners();
             for (int i = 0; i < listeners.length; ++i) {
                 ((SubstepsSessionListener) listeners[i]).testEnded(testCaseElement);
             }
         }
 
 
         /*
          * (non-Javadoc)
          * 
          * @see
          * org.eclipse.jdt.internal.junit.model.ITestRunListener2#testFailed
          * (int, java.lang.String, java.lang.String, java.lang.String,
          * java.lang.String, java.lang.String)
          */
         @Override
         public void testFailed(final Status status, final String testId, final String testName, final String trace,
                 final String expected, final String actual) {
             SubstepsTestElement testElement = getTestElement(testId);
             if (testElement == null) {
                 testElement = createUnrootedTestElement(testId, testName);
             }
 
             registerTestFailureStatus(testElement, status, trace, expected, actual);
 
             final Object[] listeners = sessionListeners.getListeners();
             for (int i = 0; i < listeners.length; ++i) {
                 ((SubstepsSessionListener) listeners[i]).testFailed(testElement, status, trace, expected, actual);
             }
         }
 
 
         /*
          * (non-Javadoc)
          * 
          * @see
          * org.eclipse.jdt.internal.junit.model.ITestRunListener2#testReran(
          * java.lang.String, java.lang.String, java.lang.String, int,
          * java.lang.String, java.lang.String, java.lang.String)
          */
         @Override
         public void testReran(final String testId, final String className, final String testName, final Status status,
                 final String trace, final String expectedResult, final String actualResult) {
             SubstepsTestElement testElement = getTestElement(testId);
             if (testElement == null) {
                 testElement = createUnrootedTestElement(testId, testName);
             } else if (!(testElement instanceof SubstepsTestLeafElement)) {
                 logUnexpectedTest(testId, testElement);
                 return;
             }
             final SubstepsTestLeafElement testCaseElement = (SubstepsTestLeafElement) testElement;
 
             registerTestFailureStatus(testElement, status, trace, expectedResult, actualResult);
 
             final Object[] listeners = sessionListeners.getListeners();
             for (int i = 0; i < listeners.length; ++i) {
                 // TODO: post old & new status?
                 ((SubstepsSessionListener) listeners[i]).testReran(testCaseElement, status, trace, expectedResult,
                         actualResult);
             }
         }
 
 
         private void logUnexpectedTest(final String testId, final SubstepsTestElement testElement) {
             FeatureRunnerPlugin.log(org.eclipse.core.runtime.Status.WARNING, "Unexpected TestElement type for testId '"
                     + testId + "': " + testElement);
         }
 
 
         @Override
         public void sessionLaunched(final SubstepsRunSession substepsRunSession) {
             // TODO Auto-generated method stub
 
         }
 
 
         @Override
         public void sessionStarted(final SubstepsRunSession session) {
             // TODO Auto-generated method stub
 
         }
 
 
         @Override
         public void sessionFinished(final SubstepsRunSession session) {
             // TODO Auto-generated method stub
 
         }
 
 
         @Override
         public void testCaseStarted(final SubstepsTestLeafElement testCaseElement) {
             // TODO Auto-generated method stub
 
         }
 
 
         @Override
         public void testCaseFinished(final SubstepsTestLeafElement testCaseElement) {
             // TODO Auto-generated method stub
 
         }
     }
 
 
     @Override
     public void registerTestFailureStatus(final SubstepsTestElement testElement, final Status status,
             final String trace, final String expected, final String actual) {
         testElement.setStatus(status, trace, expected, actual);
         if (status.isError()) {
             errorCount++;
         } else if (status.isFailure()) {
             failureCount++;
         }
     }
 
 
     @Override
     public void registerTestEnded(final SubstepsTestElement testElement, final boolean completed) {
         if (testElement instanceof SubstepsTestLeafElement) {
             totalCount++;
             if (!completed) {
                 return;
             }
             startedCount++;
             if (((SubstepsTestLeafElement) testElement).isIgnored()) {
                 ignoredCount++;
             }
             if (!testElement.getStatus().isErrorOrFailure())
                 setStatus(testElement, Status.OK);
         }
     }
 
 
     private void setStatus(final SubstepsTestElement testElement, final Status status) {
         testElement.setStatus(status);
     }
 
 
     @Override
     public SubstepsTestElement[] getAllFailedTestElements() {
         final ArrayList<SubstepsTestElement> failures = new ArrayList<SubstepsTestElement>();
         addFailures(failures, getTestRoot());
         return failures.toArray(new SubstepsTestElement[failures.size()]);
     }
 
 
     private void addFailures(final ArrayList<SubstepsTestElement> failures, final SubstepsTestElement testElement) {
         final Result testResult = testElement.getTestResult(true);
         if (testResult == Result.ERROR || testResult == Result.FAILURE) {
             failures.add(testElement);
         }
         if (testElement instanceof SubstepsTestParentElement) {
             final SubstepsTestParentElement testSuiteElement = (SubstepsTestParentElement) testElement;
             final SubstepsTestElement[] children = testSuiteElement.getChildren();
             for (int i = 0; i < children.length; i++) {
                 addFailures(failures, children[i]);
             }
         }
     }
 
 
     private Transformer<IncompleteParentItem, Boolean> checkRemainingChildItemsPredicate() {
         return new Transformer<IncompleteParentItem, Boolean>() {
             @Override
             public Boolean to(final IncompleteParentItem from) {
                return !from.hasOutstandingChildren();
             }
         };
     }
 
 
     private Callback1<IncompleteParentItem> decrementRemainingChildItemsCallback() {
         return new Callback1<IncompleteParentItem>() {
             @Override
             public void callback(final IncompleteParentItem t) {
                 t.decrementOutstandingChildren();
             }
         };
     }
 
 
     private Supplier<IncompleteParentItem> testRootSupplier() {
         return new Supplier<IncompleteParentItem>() {
             @Override
             public IncompleteParentItem get() {
                 return new IncompleteParentItem(testRoot, testRoot.getChildCount());
             }
         };
     }
 
 
     private Supplier<SubstepsTestParentElement> parentSupplier() {
         return new Supplier<SubstepsTestParentElement>() {
             @Override
             public SubstepsTestParentElement get() {
                 return incompleteParentItems.get().getParentElement();
             }
         };
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.jdt.junit.model.ITestElement#getElapsedTimeInSeconds()
      */
     @Override
     public double getElapsedTimeInSeconds() {
         if (testRoot == null)
             return Double.NaN;
 
         return testRoot.getElapsedTimeInSeconds();
     }
 
 
     @Override
     public String toString() {
         return testRunName + " " + DateFormat.getDateTimeInstance().format(new Date(startTime)); //$NON-NLS-1$
     }
 }
