 /*
 * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.google.eclipse.javascript.jstestdriver.ui.view;
 
import com.google.common.collect.Lists;
import com.google.eclipse.javascript.jstestdriver.core.JstdLaunchListener;
import com.google.eclipse.javascript.jstestdriver.core.model.EclipseJstdTestResult;
import com.google.eclipse.javascript.jstestdriver.core.model.EclipseJstdTestRunResult;
import com.google.eclipse.javascript.jstestdriver.core.model.ResultModel;
import com.google.jstestdriver.TestResult;
 
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleManager;
 import org.eclipse.ui.console.MessageConsole;
 import org.eclipse.ui.console.MessageConsoleStream;
 
import java.io.IOException;
import java.util.Collection;
 
 /**
  * Show the test results.
  *
  * @author shyamseshadri@gmail.com (Shyam Seshadri)
  */
public class TestResultsPanel extends Composite implements JstdLaunchListener {
 
   private final EclipseJstdTestRunResult testRunResult;
   private final TreeViewer testResultsTree;
   private final TestResultProgressBar testProgressIndicator;
   private final Label failuresLabel;
   private final Label errorsLabel;
   private final Label totalRunLabel;
   private final Text testDetailsText;
   private final MessageConsoleStream messageConsoleStream;
 
   private int totalNumTests;
   private ILaunchConfiguration lastLaunchConfiguration;
 
   public TestResultsPanel(Composite parent, int style) {
     super(parent, style);
     IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
     MessageConsole messageConsole = new MessageConsole("JSTestDriver", null);
     messageConsole.activate();
     messageConsoleStream = new MessageConsoleStream(messageConsole);
     consoleManager.addConsoles(new IConsole[] { messageConsole });
     setLayout(new GridLayout(3, true));
     GridData layoutData = new GridData();
     layoutData.grabExcessHorizontalSpace = true;
     layoutData.grabExcessVerticalSpace = true;
     layoutData.verticalAlignment = SWT.FILL;
     layoutData.horizontalAlignment = SWT.FILL;
     setLayoutData(layoutData);
     testRunResult = new EclipseJstdTestRunResult();
 
     GridData totalRunLabelData = new GridData();
     totalRunLabelData.grabExcessHorizontalSpace = true;
     totalRunLabelData.horizontalAlignment = SWT.FILL;
     totalRunLabel = new Label(this, SWT.CENTER);
     totalRunLabel.setText("Run : 0 / 0");
     totalRunLabel.setLayoutData(totalRunLabelData);
     GridData otherLabelData = new GridData();
     otherLabelData.horizontalAlignment = SWT.FILL;
     errorsLabel = new Label(this, SWT.CENTER);
     errorsLabel.setText("Errors : 0");
     errorsLabel.setLayoutData(otherLabelData);
     failuresLabel = new Label(this, SWT.CENTER);
     failuresLabel.setText("Failed : 0");
     failuresLabel.setLayoutData(otherLabelData);
 
     GridData progressIndicatorData = new GridData();
     progressIndicatorData.horizontalSpan = 4;
     progressIndicatorData.horizontalAlignment = SWT.FILL;
     progressIndicatorData.verticalAlignment = SWT.FILL;
     progressIndicatorData.heightHint = 20;
     progressIndicatorData.minimumWidth = 180;
     testProgressIndicator = new TestResultProgressBar(this, SWT.NONE);
     testProgressIndicator.setLayoutData(progressIndicatorData);
 
     SashForm resultsSashForm = new SashForm(this, SWT.VERTICAL);
     GridData sashFormData = new GridData();
     sashFormData.horizontalSpan = 3;
     sashFormData.horizontalAlignment = SWT.FILL;
     sashFormData.grabExcessHorizontalSpace = true;
     sashFormData.grabExcessVerticalSpace = true;
     sashFormData.heightHint = 340;
     sashFormData.verticalAlignment = SWT.FILL;
     resultsSashForm.setLayoutData(sashFormData);
     testResultsTree = new TreeViewer(resultsSashForm, SWT.NONE);
     testResultsTree.getTree().setLayoutData(sashFormData);
     testResultsTree.setLabelProvider(new TestResultsTreeLabelProvider());
     testResultsTree.setContentProvider(new TestResultsTreeContentProvider());
     testResultsTree.setInput(testRunResult);
 
     testResultsTree.addSelectionChangedListener(new ISelectionChangedListener() {
 
       public void selectionChanged(SelectionChangedEvent event) {
         testDetailsText.setText("");
         TreeSelection selection = (TreeSelection) event.getSelection();
         if (selection.getFirstElement() instanceof EclipseJstdTestResult) {
           EclipseJstdTestResult result = (EclipseJstdTestResult) selection
           .getFirstElement();
           StringBuilder details = new StringBuilder();
           if (!result.getResult().getParsedMessage().trim().equals("")) {
             details.append(result.getResult().getParsedMessage())
                 .append("\n\n");
           }
           if (!result.getResult().getStack().trim().equals("")) {
             details.append(result.getResult().getStack())
                 .append("\n\n");
           }
           testDetailsText.setText(details.toString());
         }
       }
     });
 
     GridData testDetailsData = new GridData();
     testDetailsData.horizontalSpan = 3;
     testDetailsData.grabExcessHorizontalSpace = true;
     testDetailsData.heightHint = 100;
     testDetailsData.verticalAlignment = SWT.FILL;
     testDetailsData.horizontalAlignment = SWT.FILL;
     testDetailsText = new Text(resultsSashForm, SWT.MULTI | SWT.WRAP);
     testDetailsText.setLayoutData(testDetailsData);
   }
 
   /**
    * Gets the last launched configuration, for easy rerunning of tests.
    * @return the last launch config, {@code null} if none were launched.
    */
   public ILaunchConfiguration getLastLaunchConfiguration() {
     return lastLaunchConfiguration;
   }
 
   /**
    * Sets up the panel for the next test run, clearing all the fields, resetting the progress bar
    * and saving the launch config for rerunning.
    * @param launchConfiguration the launch config used to launch the tests
    */
   public void setupForNextTestRun(ILaunchConfiguration launchConfiguration) {
     synchronized (this) {
       totalNumTests = 0;
       totalRunLabel.setText("Run : 0 / 0");
       errorsLabel.setText("Errors : 0");
       failuresLabel.setText("Failed : 0");
       lastLaunchConfiguration = launchConfiguration;
       testRunResult.clear();
       testResultsTree.refresh();
       testProgressIndicator.reset();
       update();
     }
   }
 
   /**
    * Increases the total number of expected tests by numTests.
    * @param numTests The number of tests to increment by.
    */
   public void addNumberOfTests(int numTests) {
     synchronized (this) {
       totalNumTests += numTests;
       testProgressIndicator.setMax(totalNumTests);
       totalRunLabel.setText("Run : " + testRunResult.getNumberOfTests() + " / " + totalNumTests);
     }
   }
 
   /**
    * Registers the test results and increments the progress bar and adds them to the test results
    * tree.
    * @param testResults the test results from JS Test Driver.
    */
   public void addTestResults(Collection<TestResult> testResults) {
     synchronized (this) {
       Collection<ResultModel> failedTests = Lists.newArrayList();
       for (TestResult result : testResults) {
         ResultModel addedResult = testRunResult.addTestResult(result);
         messageConsoleStream.println(result.getLog());
         if (!addedResult.passed()) {
           failedTests.add(addedResult);
         }
       }
       try {
         messageConsoleStream.flush();
       } catch (IOException e) {
       }
       testProgressIndicator.step(testResults.size(), failedTests.size() == 0);
       testResultsTree.refresh();
 
       boolean first = true;
       for (ResultModel resultModel : failedTests) {
         if (first) {
           first = false;
           testResultsTree.setSelection(new StructuredSelection(resultModel));
         }
         testResultsTree.expandToLevel(resultModel, TreeViewer.ALL_LEVELS);
       }
       errorsLabel.setText("Errors : " + testRunResult.getNumberOfErrors());
       failuresLabel.setText("Failed : " + testRunResult.getNumberOfFailures());
       totalRunLabel.setText("Run : " + testRunResult.getNumberOfTests() + " / " + totalNumTests);
     }
   }
 
   /**
    * Sets the filter for the test results tree
    * @param filter the filter
    */
   public void setTreeFilter(ViewerFilter filter) {
     testResultsTree.setFilters(new ViewerFilter[] { filter });
   }
 
   /**
    * Clears all filters on the test results tree.
    */
   public void clearTreeFilter() {
     testResultsTree.setFilters(new ViewerFilter[0]);
   }

  @Override
  public void aboutToLaunch(ILaunchConfiguration launchConfiguration) {
    setupForNextTestRun(launchConfiguration);
  }
 }
