 /*
  * Sweeper - Duplicate file cleaner
  * Copyright (C) 2012 Bogdan Ciprian Pistol
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package gg.pistol.sweeper.gui;
 
 import com.google.common.base.Preconditions;
 import gg.pistol.sweeper.core.Sweeper;
 import gg.pistol.sweeper.core.SweeperAbortException;
 import gg.pistol.sweeper.core.SweeperException;
 import gg.pistol.sweeper.core.SweeperOperation;
 import gg.pistol.sweeper.core.SweeperOperationListener;
 import gg.pistol.sweeper.core.Target;
 import gg.pistol.sweeper.core.TargetAction;
 import gg.pistol.sweeper.core.resource.Resource;
 import gg.pistol.sweeper.gui.component.ConfirmationDialog;
 import gg.pistol.sweeper.i18n.I18n;
 
 import javax.annotation.Nullable;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.Collection;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingDeque;
 
 // package private
 class AnalysisPage extends WizardPage {
 
     private static final int PROGRESS_UPDATE_FREQUENCY = 40; // millis
 
     private final WizardPage previousPage;
 
     private final Collection<? extends Resource> resources;
     private final SweeperOperationListener operationListener;
 
     private final long startTime;
     private volatile int totalProgressPercent;
     private SweeperOperation operation;
     private volatile long operationProgress;
     private volatile long operationMaxProgress;
     private volatile Target currentTarget;
 
     private final ExecutorService executor;
     private boolean analysisStarted;
     private volatile boolean analysisDone;
     private volatile boolean analysisCanceled;
 
     private int errorLineEnd;
     private int errorCounter;
     private final BlockingQueue<String> errorQueue;
 
     @Nullable private JProgressBar totalProgressBar;
     @Nullable private JLabel totalTime;
     @Nullable private JLabel totalRemainingTime;
     @Nullable private JLabel operationDescription;
     @Nullable private JProgressBar operationProgressBar;
     @Nullable private JLabel operationTarget;
     @Nullable private JTextArea errors;
 
     AnalysisPage(WizardPage previousPage, I18n i18n, WizardPageListener listener, Sweeper sweeper, Collection<? extends Resource> resources) {
         super(Preconditions.checkNotNull(i18n), Preconditions.checkNotNull(listener), Preconditions.checkNotNull(sweeper));
         Preconditions.checkNotNull(previousPage);
 
         this.previousPage = previousPage;
         this.resources = resources;
         operationListener = getOperationListener();
 
         startTime = System.currentTimeMillis();
 
         executor = Executors.newFixedThreadPool(2);
         operation = SweeperOperation.RESOURCE_TRAVERSING;
         errorQueue = new LinkedBlockingDeque<String>();
     }
 
     @Override
     protected void addComponents(JPanel contentPanel) {
         Preconditions.checkNotNull(contentPanel);
         super.addComponents(contentPanel);
         contentPanel.add(alignLeft(new JLabel(i18n.getString(I18n.PAGE_ANALYSIS_INTRODUCTION_ID))));
         contentPanel.add(createVerticalStrut(15));
 
         JPanel grid = new JPanel();
         grid.setComponentOrientation(ComponentOrientation.getOrientation(i18n.getLocale()));
         grid.setLayout(new GridBagLayout());
         contentPanel.add(alignLeft(grid));
 
         addGridComponent(grid, new JLabel(i18n.getString(I18n.PAGE_ANALYSIS_TOTAL_PROGRESS_ID)), false, false, true);
         totalProgressBar = createProgressBar();
         addGridComponent(grid, totalProgressBar, true, false, true);
 
         addGridComponent(grid, new JLabel(i18n.getString(I18n.PAGE_ANALYSIS_TOTAL_ELAPSED_TIME_ID)), false, false, false);
         totalTime = new JLabel(formatTime(getElapsedTime()));
         addGridComponent(grid, totalTime, true, false, false);
 
         addGridComponent(grid, new JLabel(i18n.getString(I18n.PAGE_ANALYSIS_TOTAL_REMAINING_TIME_ID)), false, false, false);
         totalRemainingTime = new JLabel();
         addGridComponent(grid, totalRemainingTime, true, false, false);
 
         addGridComponent(grid, new JLabel(i18n.getString(I18n.PAGE_ANALYSIS_OPERATION_LABEL_ID)), false, false, true);
         operationDescription = new JLabel(getOperationDescription(operation));
         addGridComponent(grid, operationDescription, true, false, true);
 
         addGridComponent(grid, new JLabel(i18n.getString(I18n.PAGE_ANALYSIS_OPERATION_PROGRESS_ID)), false, false, false);
         operationProgressBar = createProgressBar();
         addGridComponent(grid, operationProgressBar, true, false, false);
 
         addGridComponent(grid, new JLabel(i18n.getString(I18n.PAGE_ANALYSIS_OPERATION_TARGET_LABEL_ID)), false, false, true);
         operationTarget = new JLabel();
         addGridComponent(grid, operationTarget, true, false, true);
 
         addGridComponent(grid, new JLabel(i18n.getString(I18n.PAGE_ANALYSIS_ERROR_LABEL_ID)), false, false, true);
         errors = new JTextArea();
         errors.setEditable(false);
         addCopyMenu(errors);
         addGridComponent(grid, new JScrollPane(errors), true, true, true);
 
         if (!analysisStarted) {
             analysisStarted = true;
             executor.submit(getRunnableAnalysis());
             executor.submit(getRunnableProgress());
         }
     }
 
     private SweeperOperationListener getOperationListener() {
         return new SweeperOperationListener() {
             @Override
             public void updateOperation(final SweeperOperation currentOperation) {
                 SwingUtilities.invokeLater(new Runnable() {
                     public void run() {
                         updateTimes();
                         operation = currentOperation;
                         operationDescription.setText(getOperationDescription(operation));
                     }
                 });
             }
 
             @Override
             public void updateOperationProgress(long progress, long maxProgress, int percentGlobal) {
                 operationProgress = progress;
                 operationMaxProgress = maxProgress;
                 totalProgressPercent = percentGlobal;
                 if (progress == 0 || progress == maxProgress) {
                     SwingUtilities.invokeLater(new Runnable() {
                         @Override
                         public void run() {
                             updateProgress();
                         }
                     });
                 }
             }
 
             @Override
             public void updateTargetAction(Target target, TargetAction action) {
                 currentTarget = target;
             }
 
             @Override
             public void updateTargetException(Target target, TargetAction action, SweeperException e) {
                 try {
                     errorQueue.put(e.getMessage());
                 } catch (InterruptedException ex) {
                     // ignore
                 }
             }
         };
     }
 
     private void updateProgress() {
         updateTimes();
         totalProgressBar.setValue(totalProgressPercent);
         if (operationMaxProgress > 0) {
             operationProgressBar.setValue((int) (100 * operationProgress / operationMaxProgress));
         }
         if (operationProgress == operationMaxProgress) {
             currentTarget = null;
             listener.onButtonStateChange();
         }
 
         operationTarget.setText(currentTarget != null ? currentTarget.getName() : "");
 
         String err;
         StringBuilder sb = new StringBuilder();
         int i = 0;
         while ((err = errorQueue.poll()) != null) {
             i++;
             sb.append(err);
             sb.append("\n");
         }
        if (i > 0 || errorLineEnd == 0) {
             String counter = i18n.getString(I18n.PAGE_ANALYSIS_ERROR_COUNTER_ID, Integer.toString(errorCounter + i));
             if (errorCounter > 0) {
                 errors.replaceRange(counter, 0, errorLineEnd);
             } else {
                 errors.append(counter + "\n");
             }
             errorCounter += i;
             errorLineEnd = counter.length();
        }
        if (sb.length() > 0) {
             errors.append(sb.toString());
         }
     }
 
     private void updateTimes() {
         totalTime.setText(formatTime(getElapsedTime()));
         if (totalProgressPercent > 0) {
             totalRemainingTime.setText(formatTime(getRemainingTime()));
         }
     }
 
     private Runnable getRunnableProgress() {
         return new Runnable() {
             @Override
             public void run() {
                 while (!analysisDone && !analysisCanceled) {
                     try {
                         Thread.sleep(PROGRESS_UPDATE_FREQUENCY);
                     } catch (InterruptedException e) {
                         break;
                     }
                     SwingUtilities.invokeLater(new Runnable() {
                         @Override
                         public void run() {
                             updateProgress();
                         }
                     });
                 }
                 executor.shutdown();
             }
         };
     }
 
     private Runnable getRunnableAnalysis() {
         return new Runnable() {
             @Override
             public void run() {
                 WindowListener windowListener = new WindowAdapter() {
                     @Override
                     public void windowClosing(WindowEvent e) {
                         if (!analysisDone && !analysisCanceled) {
                             sweeper.abortAnalysis();
                         }
                     }
                 };
                 getParentWindow().addWindowListener(windowListener);
 
                 try {
                     sweeper.analyze(resources, operationListener);
                     analysisDone = true;
                 } catch (SweeperAbortException e) {
                     analysisCanceled = true;
                 } catch (Exception e) {
                     analysisCanceled = true;
                     e.printStackTrace();
                 } finally {
                     getParentWindow().removeWindowListener(windowListener);
                 }
             }
         };
     }
 
     private long getElapsedTime() {
         return System.currentTimeMillis() - startTime;
     }
 
     private long getRemainingTime() {
         return (100 - totalProgressPercent) * getElapsedTime() / totalProgressPercent;
     }
 
     private String getOperationDescription(SweeperOperation operation) {
         switch (operation) {
             case RESOURCE_TRAVERSING:
                 return i18n.getString(I18n.PAGE_ANALYSIS_OPERATION_RESOURCE_TRAVERSAL_ID);
             case SIZE_COMPUTATION:
                 return i18n.getString(I18n.PAGE_ANALYSIS_OPERATION_SIZE_COMPUTATION_ID);
             case HASH_COMPUTATION:
                 return i18n.getString(I18n.PAGE_ANALYSIS_OPERATION_HASH_COMPUTATION_ID);
             case RESOURCE_DELETION:
                 return i18n.getString(I18n.PAGE_ANALYSIS_OPERATION_RESOURCE_DELETION_ID);
         }
         return null;
     }
 
     private String formatTime(long time) {
         int hours = (int) (time / 3600000);
         time %= 3600000;
         int minutes = (int) (time / 60000);
         time %= 60000;
         int seconds = (int) (time / 1000);
         if (hours > 0) {
             return i18n.getString(I18n.TIME_DESCRIPTION_HOURS_ID, Integer.toString(hours), Integer.toString(minutes), Integer.toString(seconds));
         } else if (minutes > 0) {
             return i18n.getString(I18n.TIME_DESCRIPTION_MINUTES_ID, Integer.toString(minutes), Integer.toString(seconds));
         } else {
             return i18n.getString(I18n.TIME_DESCRIPTION_SECONDS_ID, Integer.toString(seconds));
         }
     }
 
     private void addGridComponent(JPanel panel, JComponent component, boolean fillHorizontally, boolean fillVertically, boolean newGridSection) {
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.anchor = GridBagConstraints.FIRST_LINE_START;
         int topInset = newGridSection ? 15 : 7;
         int horizontalInset = 3;
         if (fillHorizontally) {
             constraints.weightx = 1;
             constraints.fill = GridBagConstraints.HORIZONTAL;
             constraints.gridwidth = GridBagConstraints.REMAINDER;
             constraints.insets = new Insets(topInset, horizontalInset, 0, 0);
         } else {
             constraints.gridwidth = GridBagConstraints.RELATIVE;
             constraints.insets = new Insets(topInset, 0, 0, horizontalInset);
         }
         if (fillVertically) {
             constraints.weighty = 1;
             constraints.fill = fillHorizontally ? GridBagConstraints.BOTH : GridBagConstraints.VERTICAL;
             constraints.insets.bottom = 10;
         }
         panel.add(alignLeft(component), constraints);
     }
 
     private JProgressBar createProgressBar() {
         JProgressBar bar = new JProgressBar();
         bar.setComponentOrientation(ComponentOrientation.getOrientation(i18n.getLocale()));
         bar.setStringPainted(true);
         return bar;
     }
 
     @Override
     protected String getPageHeader() {
         return i18n.getString(I18n.PAGE_ANALYSIS_HEADER_ID);
     }
 
     @Override
     boolean isCancelButtonVisible() {
         return true;
     }
 
     @Override
     boolean isCancelButtonEnabled() {
         return !analysisCanceled && !analysisDone;
     }
 
     @Override
     boolean isBackButtonEnabled() {
         return true;
     }
 
     @Override
     boolean isNextButtonEnabled() {
         return analysisDone;
     }
 
     @Override
     boolean isFinishButtonEnabled() {
         return false;
     }
 
     @Override
     boolean isLastPage() {
         return false;
     }
 
     @Override
     boolean isLanguageSelectorVisible() {
         return true;
     }
 
     @Override
     void cancel() {
         if (back() != null) {
             listener.onButtonStateChange();
         }
     }
 
     @Override
     @Nullable
     WizardPage back() {
         if (analysisCanceled) {
             return previousPage;
         }
         if (new ConfirmationDialog(getParentWindow(), i18n, i18n.getString(I18n.PAGE_ANALYSIS_CANCEL_CONFIRMATION_TITLE_ID),
                 i18n.getString(I18n.PAGE_ANALYSIS_CANCEL_CONFIRMATION_MESSAGE_ID)).isConfirmed()) {
             analysisCanceled = true;
             sweeper.abortAnalysis();
             return previousPage;
         } else {
             return null;
         }
     }
 
     @Override
     WizardPage next() {
         return null;
     }
 
     @Override
     @Nullable
     WizardPage finish() {
         return null;
     }
 
 }
