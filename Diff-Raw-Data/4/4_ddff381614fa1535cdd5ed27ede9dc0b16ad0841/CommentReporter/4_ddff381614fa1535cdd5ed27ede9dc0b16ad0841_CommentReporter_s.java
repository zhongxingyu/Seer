 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.alm.mylyn.core.bp;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jubula.client.alm.mylyn.core.i18n.Messages;
 import org.eclipse.jubula.client.alm.mylyn.core.model.CommentEntry;
 import org.eclipse.jubula.client.alm.mylyn.core.utils.ALMAccess;
 import org.eclipse.jubula.client.core.businessprocess.TestResultBP;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.ITestresultSummaryEventListener;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.IProjectPropertiesPO;
 import org.eclipse.jubula.client.core.model.ITestResultSummaryPO;
 import org.eclipse.jubula.client.core.model.TestResult;
 import org.eclipse.jubula.client.core.model.TestResultNode;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.progress.IProgressConsole;
 import org.eclipse.jubula.client.core.propertytester.NodePropertyTester;
 import org.eclipse.jubula.client.core.utils.ITreeNodeOperation;
 import org.eclipse.jubula.client.core.utils.ITreeTraverserContext;
 import org.eclipse.jubula.client.core.utils.TestResultNodeTraverser;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * @author BREDEX GmbH
  */
 public class CommentReporter implements ITestresultSummaryEventListener {
     /** instance of this class */
     private static CommentReporter instance;
     /** the progress console to use */
     private IProgressConsole m_console;
     /** the project properties to use */ 
     private IProjectPropertiesPO m_projProps = null;
     
     /**
      * @author BREDEX GmbH
      */
     private static class ReportOperation implements
             ITreeNodeOperation<TestResultNode> {
         /** the taskIdToComment mapping */
         private Map<String, List<CommentEntry>> m_taskIdToComment;
         /** report failure */
         private final boolean m_reportFailure;
         /** report success */
         private final boolean m_reportSuccess;
         /** dashboard URL */
         private String m_dashboardURL;
         /** the summary id */
         private String m_summaryIdString;
         /** test result node counter */
         private long m_nodeCount = 0;
         
         /**
          * Constructor
          * 
          * @param taskIdToComment
          *            the mapping
          * @param reportSuccess
          *            reportSuccess
          * @param reportFailure
          *            reportFailure
          * @param dashboardURL
          *            dashboardURL
          * @param summaryId 
          */
         public ReportOperation(Map<String, List<CommentEntry>> taskIdToComment,
                 boolean reportFailure, boolean reportSuccess,
                 String dashboardURL, String summaryId) {
             m_taskIdToComment = taskIdToComment;
             m_reportFailure = reportFailure;
             m_reportSuccess = reportSuccess;
             m_dashboardURL = dashboardURL;
             m_summaryIdString = summaryId;
         }
 
         /** {@inheritDoc} */
         public boolean operate(ITreeTraverserContext<TestResultNode> ctx,
                 TestResultNode parent, TestResultNode resultNode,
                 boolean alreadyVisited) {
             m_nodeCount++;
             boolean didNodePass = CommentEntry
                     .hasPassed(resultNode.getStatus());
 
             INodePO node = resultNode.getNode();
             String taskIdforNode = NodePropertyTester.getTaskIdforNode(node);
             boolean hasTaskId = taskIdforNode != null;
             
             boolean writeCommentForNode = hasTaskId
                 && (m_reportSuccess && didNodePass) 
                 || (m_reportFailure && !didNodePass);
 
             if (writeCommentForNode) {
                 CommentEntry c = new CommentEntry(resultNode, m_dashboardURL,
                         m_summaryIdString, m_nodeCount);
 
                 List<CommentEntry> comments = m_taskIdToComment
                         .get(taskIdforNode);
                 if (comments != null) {
                     comments.add(c);
                 } else {
                     List<CommentEntry> cs = new LinkedList<CommentEntry>();
                     cs.add(c);
                     m_taskIdToComment.put(taskIdforNode, cs);
                 }
             }
 
             return true;
         }
 
         /** {@inheritDoc} */
         public void postOperate(ITreeTraverserContext<TestResultNode> ctx,
                 TestResultNode parent, TestResultNode node,
                 boolean alreadyVisited) {
             // currently unused
         }
     }
 
     /** Constructor */
     private CommentReporter() {
         DataEventDispatcher.getInstance()
             .addTestresultSummaryEventListener(this);
     }
 
     /**
      * @return Returns the instance.
      */
     public static CommentReporter getInstance() {
         if (instance == null) {
             instance = new CommentReporter();
         }
         return instance;
     }
 
     /**
      * process the result tree
      * 
      * @param reportFailure
      *            reportFailure
      * @param reportSuccess
      *            reportSuccess
      * @param monitor
      *            monitor
      * @param summary
      *            the summary the result tree belongs to
      * @return status
      */
     private IStatus processResultTree(IProgressMonitor monitor,
         boolean reportSuccess, boolean reportFailure, 
         ITestResultSummaryPO summary) {
         Map<String, List<CommentEntry>> taskIdToComment = 
             new HashMap<String, List<CommentEntry>>();
 
         TestResult resultTestModel = TestResultBP.getInstance()
                 .getResultTestModel();
         TestResultNode rootResultNode = resultTestModel.getRootResultNode();
 
         ITreeNodeOperation<TestResultNode> operation = new ReportOperation(
                 taskIdToComment, reportFailure, reportSuccess,
                 m_projProps.getDashboardURL(), summary.getId().toString());
         TestResultNodeTraverser traverser = new TestResultNodeTraverser(
                 rootResultNode, operation);
         traverser.traverse();
 
         return reportToALM(monitor, taskIdToComment);
     }
 
     /**
      * @param monitor
      *            the monitor to use
      * @param taskIdToComment
      *            the comment mapping
      * @return status
      */
     private IStatus reportToALM(IProgressMonitor monitor,
             Map<String, List<CommentEntry>> taskIdToComment) {
         String repoLabel = m_projProps.getALMRepositoryName();
 
         Set<String> taskIds = taskIdToComment.keySet();
         int taskAmount = taskIds.size();
         String out = NLS.bind(Messages.ReportToALMJob, repoLabel);
         monitor.beginTask(out, taskAmount);
 
         IProgressConsole c = getConsole();
         c.writeLine(out);
         for (String taskId : taskIds) {
             c.writeLine(NLS.bind(Messages.ReportingTask, taskId));
             boolean succeeded = ALMAccess.createComment(repoLabel, taskId,
                     taskIdToComment.get(taskId), monitor);
             if (!succeeded) {
                 c.writeErrorLine(NLS.bind(
                         Messages.ReportingTaskFailed, taskId));
             }
             monitor.worked(1);
         }
         c.writeLine(Messages.ReportToALMJobDone);
         monitor.done();
         return Status.OK_STATUS;
     }
 
     /**
      * @return the console
      */
     private IProgressConsole getConsole() {
         return m_console;
     }
 
     /**
      * @param console the console to set
      */
     public void setConsole(IProgressConsole console) {
         m_console = console;
     }
 
     /** {@inheritDoc} */
     public void handleTestresultSummaryChanged(
         final ITestResultSummaryPO summary, DataState state) {
         if (state != DataState.Added) {
             return;
         }
 
         IProjectPO project = GeneralStorage.getInstance().getProject();
         m_projProps = project.getProjectProperties();
 
         final boolean reportSuccess = m_projProps.getIsReportOnSuccess();
         final boolean reportFailure = m_projProps.getIsReportOnFailure();
         final String almRepositoryName = m_projProps.getALMRepositoryName();
 
         if (!StringUtils.isBlank(almRepositoryName)
                 && (reportSuccess || reportFailure)) {
             Job reportToALMOperation = new Job(NLS.bind(
                     Messages.ReportToALMJob, almRepositoryName)) {
                 protected IStatus run(IProgressMonitor monitor) {
                     getConsole().writeLine(
                             NLS.bind(Messages.TaskRepositoryConnectionTest,
                                     almRepositoryName));
                     IStatus connectionStatus = ALMAccess
                             .testConnection(almRepositoryName);
                     if (connectionStatus.isOK()) {
                         getConsole().writeLine(
                             NLS.bind(
                                 Messages.TaskRepositoryConnectionTestSucceeded,
                                     almRepositoryName));
                         return processResultTree(monitor, reportSuccess,
                                 reportFailure, summary);
                     }
                     getConsole().writeErrorLine(
                         NLS.bind(Messages.TaskRepositoryConnectionTestFailed,
                             connectionStatus.getMessage()));
 
                     return connectionStatus;
 
                 }
             };
             reportToALMOperation.schedule();
         }
     }
 }
