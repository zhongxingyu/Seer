 /*******************************************************************************
  * Copyright (c) 2004, 2007 Mylyn project committers and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Mylyn project committers
  *******************************************************************************/
 /*******************************************************************************
  * Copyright (c) 2008 Sven Krzyzak
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Sven Krzyzak - adapted Trac implementation for Redmine
  *******************************************************************************/
 package org.svenk.redmine.core;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.mylyn.commons.core.StatusHandler;
 import org.eclipse.mylyn.commons.net.Policy;
 import org.eclipse.mylyn.tasks.core.ITaskMapping;
 import org.eclipse.mylyn.tasks.core.RepositoryResponse;
 import org.eclipse.mylyn.tasks.core.TaskRepository;
 import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
 import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
 import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
 import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
 import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
 import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
 import org.eclipse.mylyn.tasks.core.data.TaskData;
 import org.eclipse.mylyn.tasks.core.data.TaskOperation;
 import org.svenk.redmine.core.accesscontrol.internal.RedmineAcl;
 import org.svenk.redmine.core.client.RedmineClientData;
 import org.svenk.redmine.core.client.RedmineProjectData;
 import org.svenk.redmine.core.data.RedmineTaskTimeEntryMapper;
 import org.svenk.redmine.core.exception.RedmineException;
 import org.svenk.redmine.core.exception.RedmineStatusException;
 import org.svenk.redmine.core.model.RedmineActivity;
 import org.svenk.redmine.core.model.RedmineAttachment;
 import org.svenk.redmine.core.model.RedmineCustomField;
 import org.svenk.redmine.core.model.RedmineMember;
 import org.svenk.redmine.core.model.RedminePriority;
 import org.svenk.redmine.core.model.RedmineTicket;
 import org.svenk.redmine.core.model.RedmineTicketAttribute;
 import org.svenk.redmine.core.model.RedmineTicketJournal;
 import org.svenk.redmine.core.model.RedmineTicketProgress;
 import org.svenk.redmine.core.model.RedmineTicketStatus;
 import org.svenk.redmine.core.model.RedmineTimeEntry;
 import org.svenk.redmine.core.model.RedmineCustomField.FieldType;
 import org.svenk.redmine.core.model.RedmineTicket.Key;
 import org.svenk.redmine.core.util.RedmineUtil;
 import org.svenk.redmine.core.util.internal.RedmineTaskDataReader;
 
 public class RedmineTaskDataHandler extends AbstractTaskDataHandler {
 
 	private RedmineRepositoryConnector connector;
 	
 	public RedmineTaskDataHandler(RedmineRepositoryConnector connector) {
 		this.connector = connector;
 	}
 
 	@Override
 	public TaskAttributeMapper getAttributeMapper(TaskRepository taskRepository) {
 		RedmineClientData clientData = connector.getClientManager().getClientData(taskRepository);
 		return new RedmineAttributeMapper(taskRepository, clientData);
 	}
 
 	public TaskData getTaskData(TaskRepository repository, String taskId, IProgressMonitor monitor) throws CoreException {
 		monitor = Policy.monitorFor(monitor);
 		try {
 			int id = Integer.parseInt(taskId);
 			monitor.beginTask(Messages.RedmineTaskDataHandler_DOWNLOAD_TASK, IProgressMonitor.UNKNOWN);
 			return downloadTaskData(repository, id, monitor);
 		} catch(NumberFormatException e) {
 			throw new CoreException(RedmineCorePlugin.toStatus(e, repository, "INVALID_TASK_ID {0}", taskId));
 		} finally {
 			monitor.done();
 		}
 	}
 
 	protected TaskData downloadTaskData(TaskRepository repository,
 			int id, IProgressMonitor monitor) throws CoreException {
 
 		RedmineTicket ticket;
 		IRedmineClient client;
 		try {
 			client = connector.getClientManager().getRedmineClient(repository);
 			client.updateAttributes(false, monitor);
 			ticket = client.getTicket(id, monitor);
 		} catch (OperationCanceledException e) {
 			throw new CoreException(RedmineCorePlugin.toStatus(e, repository));
 		} catch (RedmineException e) {
 			throw new CoreException(RedmineCorePlugin.toStatus(e, repository));
 		}
 		return createTaskDataFromTicket(client, repository, ticket, monitor);
 	}
 
 	public TaskData createTaskDataFromTicket(IRedmineClient client, TaskRepository repository, RedmineTicket ticket,
 			IProgressMonitor monitor) throws CoreException {
 
 		try {
 			TaskData taskData = new TaskData(getAttributeMapper(repository), RedmineCorePlugin.REPOSITORY_KIND,
 					repository.getRepositoryUrl(), ticket.getId() + ""); //$NON-NLS-1$
 			createDefaultAttributes(taskData, client, ticket);
 			createOperations(taskData, client.getClientData(), ticket);
 			updateTaskData(repository, taskData, client, ticket);
 			return taskData;
 		} catch (RedmineException e) {
 			IStatus status = RedmineCorePlugin.toStatus(e, repository);
 			throw new CoreException(status);
 		}
 	}
 
 	public static Set<TaskAttribute> updateTaskData(TaskRepository repository, TaskData data,
 			IRedmineClient client, RedmineTicket ticket) throws CoreException {
 
 		Set<TaskAttribute> changedAttributes = new HashSet<TaskAttribute>();
 
 		if (ticket.getCreated() != null) {
 			TaskAttribute taskAttribute = data.getRoot().getAttribute(RedmineAttribute.DATE_SUBMITTED.getTaskKey());
 			taskAttribute.setValue(RedmineUtil.parseDate(ticket.getCreated()));
 			changedAttributes.add(taskAttribute);
 		}
 		
 		if (ticket.getLastChanged() != null) {
 			TaskAttribute taskAttribute = data.getRoot().getAttribute(RedmineAttribute.DATE_UPDATED.getTaskKey());
 			taskAttribute.setValue(RedmineUtil.parseDate(ticket.getLastChanged()));
 			changedAttributes.add(taskAttribute);
 		}
 
 		
 		String projectId = ticket.getValues().get(Key.PROJECT.getKey());
 		String projectName = null;
 		try {
 			RedmineProjectData projectData = client.getClientData().getProjectFromId(Integer.parseInt(projectId));
 			projectName = projectData.getProject().getName();
 		} catch (NumberFormatException e) {
 			throw new CoreException(RedmineCorePlugin.toStatus(e, repository, "INVALID_PROJECT_ID {0}", projectId));
 		} catch (NullPointerException e) {
 			throw new CoreException(RedmineCorePlugin.toStatus(e, repository, "UNKNOWN_PROJECT_ID {0}", projectId));
 		}
 
 		Map<String, String> valueByKey = ticket.getValues();
 		for (String key : valueByKey.keySet()) {
 			TaskAttribute taskAttribute = data.getRoot().getMappedAttribute(key);
 			if(taskAttribute!=null) {
 				switch(RedmineAttribute.fromRedmineKey(key)) {
 				case PROJECT : taskAttribute.setValue(projectName); break;
 				case ASSIGNED_TO:
 					try {
 						taskAttribute.setValue(String.valueOf(Integer.parseInt(valueByKey.get(key))));
 					} catch (NumberFormatException e) {
 						throw new CoreException(RedmineCorePlugin.toStatus(e, repository, "INVALID_MEMBER_ID {0}", valueByKey.get(key)));
 					}
 					break;
 				case STATUS :
 					taskAttribute.setValue(valueByKey.get(key));
 					TaskAttribute statusChgAttribute = data.getRoot().getAttribute(RedmineAttribute.STATUS_CHG.getTaskKey());
 					if(statusChgAttribute!=null) {
 						statusChgAttribute.setValue(valueByKey.get(key));
 						changedAttributes.add(statusChgAttribute);
 					}
 					break;
 				default : taskAttribute.setValue(valueByKey.get(key));
 				}
 				
 				changedAttributes.add(taskAttribute);
 			}
 			
 		}
 
 
 		RedmineTicketJournal[] journals = ticket.getJournals();
 		if (journals != null) {
 			int count = 1;
 			for (RedmineTicketJournal journal : journals) {
 				TaskCommentMapper mapper = new TaskCommentMapper();
 				mapper.setAuthor(repository.createPerson(journal.getAuthorName()));
 				mapper.setCreationDate(journal.getCreated());
 				mapper.setText(journal.getNotes());
 
 				String commentId = ""+journal.getId(); //$NON-NLS-1$
 				String commentUrl = RedmineRepositoryConnector.getTaskURL(repository.getUrl(), ticket.getId());
 				mapper.setUrl(commentUrl + IRedmineConstants.REDMINE_URL_COMMENT + commentId);
 				mapper.setNumber(count++);
 				mapper.setCommentId(commentId);
 
 				TaskAttribute attribute = data.getRoot().createAttribute(TaskAttribute.PREFIX_COMMENT + mapper.getCommentId());
 				mapper.applyTo(attribute);
 			}
 		}
 
 		RedmineAttachment[] attachments = ticket.getAttachments();
 		if (attachments != null) {
 			for (RedmineAttachment attachment : attachments) {
 				TaskAttachmentMapper mapper = new TaskAttachmentMapper();
 				mapper.setAttachmentId("" + attachment.getId()); //$NON-NLS-1$
 				mapper.setAuthor(repository.createPerson(attachment.getAuthorName()));
 				mapper.setDescription(attachment.getDescription());
 				mapper.setCreationDate(attachment.getCreated());
 				mapper.setContentType(attachment.getContentType());
 				mapper.setFileName(attachment.getFilename());
 				mapper.setLength((long)attachment.getFilesize());
 				String url  = repository.getUrl() + IRedmineConstants.REDMINE_URL_ATTACHMENT_DOWNLOAD +  mapper.getAttachmentId();
 				mapper.setUrl(url);
 				
 				TaskAttribute attribute = data.getRoot().createAttribute(TaskAttribute.PREFIX_ATTACHMENT + mapper.getAttachmentId());
 				mapper.applyTo(attribute);
 			}
 		}
 
 		if (client.supportTimeEntries() && ticket.getRight(RedmineAcl.TIMEENTRY_VIEW)) {
 			RedmineTimeEntry[] timeEntries = ticket.getTimeEntries();
 			if(timeEntries != null) {
 				for (RedmineTimeEntry timeEntry : timeEntries) {
 					RedmineTaskTimeEntryMapper mapper = new RedmineTaskTimeEntryMapper(timeEntry, client.getClientData());
 					TaskAttribute attribute = data.getRoot().createAttribute(IRedmineConstants.TASK_ATTRIBUTE_TIMEENTRY + timeEntry.getId());
 					mapper.applyTo(attribute);
 		
 					//Options(s) for ActivityId
 					TaskAttribute activityAttribute = RedmineTaskTimeEntryMapper.getActivityAttribute(attribute);
 					RedmineActivity activity = client.getClientData().getActivity(mapper.getActivityId());
 					if (activity!=null) {
 						activityAttribute.putOption(""+activity.getValue(), activity.getName()); //$NON-NLS-1$
 					}
 		
 					//Labels of CustomFields
 					for(RedmineCustomField customField : client.getClientData().getTimeEntryCustomFields()) {
 						TaskAttribute customAttribute = RedmineTaskTimeEntryMapper.getCustomAttribute(attribute, customField.getId());
 						if (customAttribute!=null) {
 							customAttribute.getMetaData().setLabel(customField.getName());
 						}
 					}
 					
 				}
 				
 			}
 		}
 
 		//CustomTicketFields
 		for (Map.Entry<Integer, String> customValue : ticket.getCustomValues().entrySet()) {
 			TaskAttribute taskAttribute = data.getRoot().getAttribute(IRedmineConstants.TASK_KEY_PREFIX_TICKET_CF + customValue.getKey().intValue());
 			if (taskAttribute != null) {
 				if (taskAttribute.getMetaData().getType()==TaskAttribute.TYPE_BOOLEAN) {
 					taskAttribute.setValue(RedmineUtil.parseBoolean(customValue.getValue()).toString());
 				} else {
 					taskAttribute.setValue(customValue.getValue());
 				}
 			}
 		}
 		
 		
 		return changedAttributes;
 	}
 
 	@Override
 	public RepositoryResponse postTaskData(TaskRepository repository, TaskData taskData, Set<TaskAttribute> oldAttributes, IProgressMonitor monitor) throws CoreException {
 		
 		
 		try {
 			IRedmineClient client = connector.getClientManager().getRedmineClient(repository);
 			Map<String, String> postValues = RedmineTaskDataReader.readTask(taskData, oldAttributes, client.getClientData());
 			
 			if (taskData.isNew() || taskData.getTaskId().equals("")) { //$NON-NLS-1$
 				//set read-only attributes Project
 				TaskAttribute projAttr = taskData.getRoot().getMappedAttribute(RedmineAttribute.PROJECT.getTaskKey());
 				RedmineProjectData projectData = client.getClientData().getProjectFromName(projAttr.getValue());
 				
 				String extraKey = RedmineAttribute.PROJECT.getRedmineKey();
 				String extraValue = "" + projectData.getProject().getValue(); //$NON-NLS-1$
 				postValues.put(extraKey, extraValue);
 				
 				//set read-only attributes Tracker
 				extraKey = RedmineAttribute.TRACKER.getRedmineKey();
 				extraValue = "" + taskData.getRoot().getMappedAttribute(RedmineAttribute.TRACKER.getTaskKey()).getValue(); //$NON-NLS-1$
 				postValues.put(extraKey, extraValue);
 				
 				int taskId = client.createTicket(""+projectData.getProject().getValue(), postValues, monitor); //$NON-NLS-1$
 				Assert.isTrue(taskId>0);
 
 				return new RepositoryResponse(ResponseKind.TASK_CREATED, "" + taskId); //$NON-NLS-1$
 				
 			} else {
 				int ticketId = Integer.parseInt(taskData.getTaskId());
 
 				String comment = taskData.getRoot().getMappedAttribute(RedmineAttribute.COMMENT.getTaskKey()).getValue();
 				comment = (comment==null) ? "" : comment.trim(); //$NON-NLS-1$
 				client.updateTicket(ticketId, postValues, comment, monitor);
 				return new RepositoryResponse(ResponseKind.TASK_UPDATED, "" + ticketId); //$NON-NLS-1$
 			}
 		} catch (NumberFormatException e) {
 			IStatus status = RedmineCorePlugin.toStatus(e, null, "INVALID_TASK_ID {0}", taskData.getTaskId());
 			StatusHandler.log(status);
 			throw new CoreException(status);
 		} catch (RedmineStatusException e) {
 			throw new CoreException(e.getStatus());
 		} catch (RedmineException e) {
 			throw new CoreException(RedmineCorePlugin.toStatus(e, repository));
 		}
 
 		
 	}
 	
 	
 	@Override
 	public boolean initializeTaskData(TaskRepository repository, TaskData data,
 			ITaskMapping initializationData, IProgressMonitor monitor)
 			throws CoreException {
 		
 		try {
 			IRedmineClient client = connector.getClientManager().getRedmineClient(repository);
 			client.updateAttributes(false, monitor);
 			
 			//Input from wizard
 			RedmineProjectData projectData = client.getClientData().getProjectFromName(initializationData.getProduct());
 
 			//Initialize new ticket
 			RedmineTicket ticket = new RedmineTicket();
 			ticket.putBuiltinValue(RedmineTicket.Key.PROJECT, projectData.getProject().getValue());
 
 			createDefaultAttributes(data, client, ticket, projectData);
 			createOperations(data, client.getClientData(), null);
 
 			//set fixed values
 			TaskAttribute attr = data.getRoot().getMappedAttribute(RedmineAttribute.PROJECT.getTaskKey());
 			attr.setValue(projectData.getProject().getName());
 			attr = data.getRoot().getMappedAttribute(RedmineAttribute.TRACKER.getTaskKey());
 						
 			createCustomAttributes(data, client, ticket, projectData);
 			
 			//set default value for attributes Status,Priority,Tracker and Progress(DoneRatio)
 			attr = data.getRoot().getMappedAttribute(RedmineAttribute.STATUS_CHG.getTaskKey());
 			RedmineTicketStatus defStatus = client.getClientData().getDefaultStatus();
 			if(defStatus != null) {
 				attr.setValue("" + defStatus.getValue()); //$NON-NLS-1$
 			}
 			attr = data.getRoot().getMappedAttribute(RedmineAttribute.PRIORITY.getTaskKey());
 			for (RedminePriority priority : client.getClientData().getPriorities()) {
 				if (priority.isDefaultPriority()) {
 					attr.setValue("" + priority.getValue()); //$NON-NLS-1$
 				}
 			}
 			attr = data.getRoot().getMappedAttribute(RedmineAttribute.TRACKER.getTaskKey());
 			Map<String, String> trackerOptions =  attr.getOptions();
 			if(trackerOptions!=null && !trackerOptions.isEmpty()) {
 				attr.setValue(trackerOptions.keySet().iterator().next());
 			}
 			attr = data.getRoot().getMappedAttribute(RedmineAttribute.PROGRESS.getTaskKey());
 			attr.setValue(RedmineTicketProgress.getDefaultValue());
 
 			return true;
 		} catch (OperationCanceledException e) {
 			throw e;
 		} catch (NumberFormatException e) {
 			throw new CoreException(RedmineCorePlugin.toStatus(e, repository));
 		} catch (RedmineException e) {
 			throw new CoreException(RedmineCorePlugin.toStatus(e, repository));
 		}
 	}
 	
 	private static void createDefaultAttributes(TaskData data, IRedmineClient client, RedmineTicket ticket) throws RedmineException {
 		try {
 			int projectId = Integer.parseInt(ticket.getValues().get(Key.PROJECT.getKey()));
 			RedmineProjectData projectData = client.getClientData().getProjectFromId(projectId);
 			createDefaultAttributes(data, client, ticket, projectData);
 			createCustomAttributes(data, client, ticket, projectData);
 		} catch (NumberFormatException e) {
 			throw new RedmineException("INVALID_PROJECT_ID", e);
 		} catch (NullPointerException e) {
 			//TODO refresh RepositoryConfiguration
 			IStatus status = RedmineCorePlugin.toStatus(e, null, "INCOMPLETE_REPOSITORY_CONFIGURATION");
 			StatusHandler.log(status);
 			throw new RedmineException(status.getMessage(), e);
 		}
 	}
 	
 	private static void createDefaultAttributes(TaskData data, IRedmineClient client, RedmineTicket ticket, RedmineProjectData projectData) {
 		boolean existingTask = ticket.getId()>0;
		TaskAttribute attribute;
 		
 		createAttribute(data, RedmineAttribute.SUMMARY);
 		createAttribute(data, RedmineAttribute.DESCRIPTION);
 		createAttribute(data, RedmineAttribute.PROJECT);
 		createAttribute(data, RedmineAttribute.ESTIMATED);
 		
 		if(client.supportStartDueDate()) {
 			createAttribute(data, RedmineAttribute.DATE_DUE);
 			createAttribute(data, RedmineAttribute.DATE_START);
 		}
 
 		if (existingTask) {
 			createAttribute(data, RedmineAttribute.REPORTER);
 			createAttribute(data, RedmineAttribute.DATE_SUBMITTED);
 			createAttribute(data, RedmineAttribute.DATE_UPDATED);
 			
 			createAttribute(data, RedmineAttribute.COMMENT);
 			createAttribute(data, RedmineAttribute.STATUS, ticket.getStatuses(), false);
 			createAttribute(data, RedmineAttribute.STATUS_CHG, ticket.getStatuses(), false);
 			createAttribute(data, RedmineAttribute.RELATION, ticket.getRelations(), false);
 			
 			if (client.supportTimeEntries() && ticket.getRight(RedmineAcl.TIMEENTRY_VIEW)) {
 				createAttribute(data, RedmineAttribute.TIME_ENTRY_TOTAL);
 			}
 		} else {
 			createAttribute(data, RedmineAttribute.STATUS, client.getClientData().getStatuses(), false);
 			createAttribute(data, RedmineAttribute.STATUS_CHG, client.getClientData().getStatuses(), false);
 		}
 
 		
 		createAttribute(data, RedmineAttribute.PRIORITY, client.getClientData().getPriorities());
 		createAttribute(data, RedmineAttribute.CATEGORY, projectData.getCategorys(), true);
 		
 		if (existingTask && RedmineUtil.parseInteger(ticket.getValue(RedmineAttribute.VERSION.getTicketKey()))!=null) {
 			createAttribute(data, RedmineAttribute.VERSION, projectData.getAssignableVersions(RedmineUtil.parseInteger(ticket.getValue(RedmineAttribute.VERSION.getTicketKey()))), true);
 		} else {
 			createAttribute(data, RedmineAttribute.VERSION, projectData.getAssignableVersions(), true);
 		}
 		createAttribute(data, RedmineAttribute.ASSIGNED_TO, projectData.getMembers(), !existingTask);
 		createAttribute(data, RedmineAttribute.TRACKER, projectData.getTrackers(), false).getMetaData().setReadOnly(!data.isNew() && !client.supportTrackerChange());

		attribute = createAttribute(data, RedmineAttribute.PROGRESS, RedmineTicketProgress.availableValues(), false);
		if (!(existingTask && ticket.getUseDoneratioField())) {
			attribute.getMetaData().setType(null);
 		}
 
 		//Attributes for new a TimeEntry
 		if (client.supportTimeEntries() && ticket.getRight(RedmineAcl.TIMEENTRY_NEW)) {
 			createAttribute(data, RedmineAttribute.TIME_ENTRY_HOURS);
 			createAttribute(data, RedmineAttribute.TIME_ENTRY_ACTIVITY, client.getClientData().getActivities(), false);
 			createAttribute(data, RedmineAttribute.TIME_ENTRY_COMMENTS);
 			createCustomAttributes(data, client.getClientData().getTimeEntryCustomFields(), true);
 		}
 
 	}
 	
 	private static TaskAttribute createAttribute(TaskData data, RedmineAttribute redmineAttribute) {
 		TaskAttribute attr = data.getRoot().createAttribute(redmineAttribute.getTaskKey());
 		attr.getMetaData().setType(redmineAttribute.getType());
 		attr.getMetaData().setKind(redmineAttribute.getKind());
 		attr.getMetaData().setLabel(redmineAttribute.toString());
 		attr.getMetaData().setReadOnly(redmineAttribute.isReadOnly());
 		return attr;
 	}
 	
 	private static TaskAttribute createAttribute(TaskData data, RedmineAttribute redmineAttribute, List<? extends RedmineTicketAttribute> values) {
 		return createAttribute(data, redmineAttribute, values, false);
 	}
 
 	private static TaskAttribute createAttribute(TaskData data, RedmineAttribute redmineAttribute, List<? extends RedmineTicketAttribute> values,
 			boolean allowEmtpy) {
 		TaskAttribute attr = createAttribute(data, redmineAttribute);
 
 		if (values != null && values.size() > 0) {
 			if (allowEmtpy) {
 				attr.putOption("", ""); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 			for (Object value : values) {
 				Assert.isTrue(value instanceof RedmineTicketAttribute);
 				if (value instanceof RedmineMember && !((RedmineMember)value).isAssignable()) { 
 					continue;
 				}
 				RedmineTicketAttribute ticketAttribute = (RedmineTicketAttribute)value;
 				attr.putOption(String.valueOf(ticketAttribute.getValue()), ticketAttribute.getName());
 			}
 		} else {
 			attr.getMetaData().setReadOnly(true);
 		}
 		return attr;
 	}
 
 	private static void createCustomAttributes(TaskData data, IRedmineClient client, RedmineTicket ticket, RedmineProjectData projectData) throws RedmineException {
 		List<RedmineCustomField> customFields = projectData.getCustomTicketFields();
 		createCustomAttributes(data, customFields, false);
 	}
 	
 	private static void createCustomAttributes(TaskData data, List<RedmineCustomField> customFields, boolean hide) {
 		for (RedmineCustomField customField : customFields) {
 			TaskAttribute attribute = createAttribute(data, customField);
 			if (hide) {
 				attribute.getMetaData().setKind(null);
 			}
 			if (customField.getType()==FieldType.LIST) {
 				if (!customField.isRequired()) {
 					attribute.putOption("", ""); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 				for (String option : customField.getListValues()) {
 					attribute.putOption(option, option);
 				}
 			}
 		}
 	}
 	
 	private static TaskAttribute createAttribute(TaskData data, RedmineCustomField customField) {
 		TaskAttribute attr = data.getRoot().createAttribute(customField.getTaskKeyPrefix() + customField.getId());
 		attr.getMetaData().setType(customField.getType().getTaskAttributeType());
 		attr.getMetaData().setKind(TaskAttribute.KIND_DEFAULT);
 		attr.getMetaData().setLabel(customField.getName());
 		attr.getMetaData().setReadOnly(false);
 		return attr;
 	}
 
 	private static void createOperations(TaskData taskData, RedmineClientData clientData, RedmineTicket ticket) {
 		RedmineTicketStatus currentStatus = null;
 		if(ticket!=null) {
 			String statusVal = ticket.getValue(RedmineAttribute.STATUS.getTicketKey());
 			if(statusVal!=null && statusVal.matches(IRedmineConstants.REGEX_INTEGER)) {
 				currentStatus = clientData.getStatus(Integer.parseInt(statusVal));
 			}
 		}
 		
 		if(currentStatus!=null) {
 			createOperation(taskData, RedmineOperation.none, ""+currentStatus.getValue(), currentStatus.getName()); //$NON-NLS-1$
 		}
 		
 		createOperation(taskData, RedmineOperation.markas, null);
 	}
 
 	private static TaskAttribute createOperation(TaskData taskData, RedmineOperation operation, String defaultValue, Object... labelArgs) {
 		TaskAttribute operationAttrib = taskData.getRoot().getAttribute(TaskAttribute.OPERATION);
 		if(operationAttrib==null) {
 			operationAttrib = taskData.getRoot().createAttribute(TaskAttribute.OPERATION);
 			TaskOperation.applyTo(operationAttrib, operation.toString(), null);
 		}
 
 		TaskAttribute attribute = taskData.getRoot().createAttribute(TaskAttribute.PREFIX_OPERATION + operation.toString());
 		TaskOperation.applyTo(attribute, operation.toString(), operation.getLabel(labelArgs));
 		
 		if(operation.isAssociated()) {
 			attribute.getMetaData().putValue(TaskAttribute.META_ASSOCIATED_ATTRIBUTE_ID, operation.getInputId());
 		} else if(operation.needsRestoreValue() && defaultValue!=null && defaultValue!=""){ //$NON-NLS-1$
 			attribute.getMetaData().putValue(IRedmineConstants.TASK_ATTRIBUTE_OPERATION_RESTORE, defaultValue);
 		}
 
 
 		return attribute;
 	}
 
 }
