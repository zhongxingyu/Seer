 /*******************************************************************************
  * Copyright (c) 2013 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.alm.mylyn.core.utils;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jubula.client.alm.mylyn.core.Activator;
 import org.eclipse.jubula.client.alm.mylyn.core.i18n.Messages;
 import org.eclipse.jubula.client.alm.mylyn.core.model.CommentEntry;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.mylyn.commons.net.AuthenticationType;
 import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
 import org.eclipse.mylyn.tasks.core.IRepositoryManager;
 import org.eclipse.mylyn.tasks.core.IRepositoryModel;
 import org.eclipse.mylyn.tasks.core.ITask;
 import org.eclipse.mylyn.tasks.core.RepositoryResponse;
 import org.eclipse.mylyn.tasks.core.TaskMapping;
 import org.eclipse.mylyn.tasks.core.TaskRepository;
 import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
 import org.eclipse.mylyn.tasks.core.data.ITaskDataManager;
 import org.eclipse.mylyn.tasks.core.data.ITaskDataWorkingCopy;
 import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
 import org.eclipse.mylyn.tasks.core.data.TaskData;
 import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
 import org.eclipse.mylyn.tasks.ui.TasksUi;
 import org.eclipse.osgi.util.NLS;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author BREDEX GmbH
  */
 public final class ALMAccess {
     /**
      * @author BREDEX GmbH
      */
     public enum CONNECTOR {
         /** default handling type */
         DEFAULT, 
         /** custom handling type */
         HP_ALM
     }
     
     /**
      * @author BREDEX GmbH
      */
     public static class ALMDefaultTaskMapping extends TaskMapping {
         /** m_values */
         private Map<String, String> m_values = new HashMap<String, String>();
 
         @Override
         public String getDescription() {
             return m_values.get(TaskAttribute.DESCRIPTION);
         }
 
         @Override
         public String getSummary() {
             return m_values.get(TaskAttribute.SUMMARY);
         }
 
         @Override
         public String getProduct() {
             return m_values.get(TaskAttribute.PRODUCT);
         }
         
         @Override
         public String getTaskKind() {
             return m_values.get(TaskAttribute.TASK_KIND);
         }
     }
     
     /** the logger */
     private static final Logger LOG = LoggerFactory.getLogger(ALMAccess.class);
 
     /** Constructor */
     private ALMAccess() {
         // hide
     }
 
     /**
      * @param repoLabel
      *            the label of the repository
      * @return the task repository or <code>null</code> if not found
      */
     public static TaskRepository getRepositoryByLabel(String repoLabel) {
         List<TaskRepository> allRepositories = getAllRepositories();
 
         for (TaskRepository repo : allRepositories) {
             if (repo.getRepositoryLabel().equals(repoLabel)) {
                 return repo;
             }
         }
         return null;
     }
 
     /**
      * @return a list of all available task repositories
      */
     public static List<TaskRepository> getAllRepositories() {
         IRepositoryManager repositoryManager = TasksUi.getRepositoryManager();
         return repositoryManager.getAllRepositories();
     }
 
     /**
      * @param repo
      *            the task repository
      * @param taskId
      *            the taskId
      * @param monitor
      *            the monitor to use
      * @return the task or <code>null</code> if not found
      * @throws CoreException
      *             in case of a problem
      */
     private static ITask getTaskByID(TaskRepository repo, String taskId,
         IProgressMonitor monitor) throws CoreException {
         ITask task = null;
         if (validRepository(repo)) {
             IRepositoryModel repositoryModel = TasksUi.getRepositoryModel();
             task = repositoryModel.getTask(repo, taskId);
             if (task == null) {
                 task = repositoryModel.createTask(repo, taskId);
             }
         }
         return task;
     }
 
     /**
      * @param repo
      *            the repository to check
      * @return if the repository is valid
      */
     private static boolean validRepository(TaskRepository repo) {
         return repo != null && !repo.isOffline();
     }
 
     /**
      * @param repoLabel
      *            repoLabel
      * @param taskId
      *            the taskId
      * @param comments
      *            the comment entries
      * @param monitor
      *            the monitor to use
      * @return true if succeeded; false otherwise
      */
     public static boolean createComment(String repoLabel, String taskId,
             List<CommentEntry> comments, IProgressMonitor monitor) {
         boolean succeeded = false;
         TaskRepository repo = getRepositoryByLabel(repoLabel);
         try {
             TaskData taskData = getTaskDataByID(repo, taskId, monitor);
             if (taskData == null) {
                 return succeeded;
             }
             
             ITask task = getTaskByID(repo, taskId, monitor);
             if (task != null) {
                 ITaskDataManager taskDataManager = TasksUi.getTaskDataManager();
                 ITaskDataWorkingCopy taskWorkingCopy = taskDataManager
                     .createWorkingCopy(task, taskData);
                 TaskDataModel taskModel = new TaskDataModel(repo, task,
                     taskWorkingCopy);
                 
                 String connectorKind = repo.getConnectorKind();
                 AbstractRepositoryConnector connector = TasksUi
                     .getRepositoryConnector(connectorKind);
                 AbstractTaskDataHandler taskDataHandler = connector
                     .getTaskDataHandler();
                 TaskAttribute rootData = taskModel.getTaskData()
                     .getRoot();
                 CONNECTOR handle = determineConnectorHandling(connectorKind);
                 
                 TaskAttribute change = null;
                 switch (handle) {
                     case HP_ALM:
                         change = hpAlmHandling(comments, rootData);
                         break;
                     case DEFAULT:
                     default:
                         change = defaultHandling(comments, rootData);
                         break;
                 }
                 if (change == null) {
                     return succeeded;
                 }
 
                 taskModel.attributeChanged(change);
                 
                 RepositoryResponse response = taskDataHandler.postTaskData(
                     taskModel.getTaskRepository(), taskModel.getTaskData(),
                     taskModel.getChangedOldAttributes(), monitor);
                 
                 succeeded = RepositoryResponse.ResponseKind.TASK_UPDATED
                         .equals(response.getReposonseKind());
             }
         } catch (CoreException e) {
             LOG.error(e.getLocalizedMessage(), e);
         }
         return succeeded;
     }
 
     /**
      * @param repo
      *            the task repository
      * @param taskId
      *            the taskId
      * @param monitor
      *            the monitor to use
      * @return the tasks data or <code>null</code> if not found
      * @throws CoreException
      *             in case of a problem
      */
     private static TaskData getTaskDataByID(TaskRepository repo, String taskId,
             IProgressMonitor monitor) throws CoreException {
         TaskData taskData = null;
         if (validRepository(repo)) {
             AbstractRepositoryConnector connector = TasksUi
                     .getRepositoryConnector(repo.getConnectorKind());
             taskData = connector.getTaskData(repo, taskId, monitor);
         }
         return taskData;
     }
     
     /**
      * @param commentEntries
      *            the commentEntries to add
      * @param attr
      *            the attribute to modify
      * @return a flag indicating the success of attribute handling
      */
     private static TaskAttribute hpAlmHandling(
         List<CommentEntry> commentEntries, TaskAttribute attr) {
         Properties almProps = Activator.getDefault().getAlmAccessProperties();
         
         String hpTaskKindKeyPrefix = CONNECTOR.HP_ALM.toString()
                 + StringConstants.DOT + TaskAttribute.TASK_KIND;
         String req = hpTaskKindKeyPrefix + ".REQUIREMENT";
         String hpTaskKindReq = almProps.getProperty(req);
         String def = hpTaskKindKeyPrefix + ".DEFECT";
         String hpTaskKindDefect = almProps.getProperty(def);
 
         String taskKindValue = attr.getMappedAttribute(
                 TaskAttribute.TASK_KIND).getValue();
         String attrName = null;
         if (hpTaskKindReq.equals(taskKindValue)) {
             attrName = almProps.getProperty(req + ".comment");
         } else if (hpTaskKindDefect.equals(taskKindValue)) {
             attrName = almProps.getProperty(def + ".comment");
         }
 
         if (attrName != null) {
             TaskAttribute commentAttribute = attr.getMappedAttribute(attrName);
             String oldComment = commentAttribute.getValue();
             String newComment = StringConstants.EMPTY;
             for (CommentEntry c : commentEntries) {
                newComment = newComment + "<br><a href=\""
                        + c.getDashboardURL() + "\">" + c.toString() + "</a>";
             }
             
             commentAttribute.setValue(oldComment + newComment);
             return commentAttribute;
         }
         return null;
     }
 
     /**
      * @param commentEntries
      *            the commentEntries to add
      * @param attr
      *            the attribute to modify
      * @return a flag indicating the success of attribute handling
      */
     private static TaskAttribute defaultHandling(
         List<CommentEntry> commentEntries, TaskAttribute attr) {
         TaskAttribute newComment = attr
             .createMappedAttribute(TaskAttribute.COMMENT_NEW);
         String comment = StringConstants.EMPTY;
 
         for (CommentEntry c : commentEntries) {
             comment = comment + StringConstants.NEWLINE + c.toString()
                 + StringConstants.NEWLINE + c.getDashboardURL()
                 + StringConstants.NEWLINE + StringConstants.NEWLINE;
         }
 
         newComment.setValue(comment);
         return newComment;
     }
 
     /**
      * @param connectorKind
      *            the connector kind
      * @return the connector handling type
      */
     private static CONNECTOR determineConnectorHandling(
             String connectorKind) {
         String hpAlmConnectorKind = Activator.getDefault()
                 .getAlmAccessProperties()
                 .getProperty(CONNECTOR.HP_ALM.toString());
         if (hpAlmConnectorKind.equals(connectorKind)) {
             return CONNECTOR.HP_ALM;
         }
         return CONNECTOR.DEFAULT;
     }
 
     /**
      * @param repoLabel
      *            the repository to test the connection for
      * @return a status reflecting the current connection state
      */
     public static IStatus testConnection(String repoLabel) {
         TaskRepository repository = getRepositoryByLabel(repoLabel);
         if (repository == null) {
             return new Status(IStatus.ERROR, Activator.ID, NLS.bind(
                     Messages.TaskRepositoryNotFound, repoLabel));
         }
         if (repository.isOffline()) {
             return new Status(IStatus.ERROR, Activator.ID, NLS.bind(
                     Messages.TaskRepositoryOffline, repoLabel));
         }
         
         boolean savePassword = repository
                 .getSavePassword(AuthenticationType.REPOSITORY);
         if (!savePassword) {
             return new Status(IStatus.ERROR, Activator.ID, NLS.bind(
                     Messages.TaskRepositoryNoCredentialsStored, repoLabel));
         }
         
         AbstractRepositoryConnector connector = TasksUi
                 .getRepositoryConnector(repository.getConnectorKind());
         if (connector == null) {
             return new Status(IStatus.ERROR, Activator.ID, NLS.bind(
                     Messages.TaskRepositoryNoCredentialsStored, repoLabel));
         }
         
         try {
             connector.updateRepositoryConfiguration(repository,
                     new NullProgressMonitor());
         } catch (CoreException e) {
             return new Status(IStatus.ERROR, Activator.ID,
                     e.getLocalizedMessage().replace("\n\n", " ")); //$NON-NLS-1$ //$NON-NLS-2$
         }
         
         IStatus repoStatus = repository.getStatus();
         if (repoStatus != null) {
             return repoStatus;
         }
         return Status.OK_STATUS;
     }
 }
