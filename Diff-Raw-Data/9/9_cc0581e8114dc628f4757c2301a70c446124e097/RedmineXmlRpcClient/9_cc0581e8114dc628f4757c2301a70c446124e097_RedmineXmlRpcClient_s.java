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
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.apache.xmlrpc.client.XmlRpcClient;
 import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.mylyn.commons.net.AbstractWebLocation;
 import org.eclipse.mylyn.tasks.core.TaskRepository;
 import org.svenk.redmine.core.exception.RedmineException;
 import org.svenk.redmine.core.exception.RedmineRemoteException;
 import org.svenk.redmine.core.model.RedmineAttachment;
 import org.svenk.redmine.core.model.RedmineCustomTicketField;
 import org.svenk.redmine.core.model.RedmineIssueCategory;
 import org.svenk.redmine.core.model.RedmineMember;
 import org.svenk.redmine.core.model.RedminePriority;
 import org.svenk.redmine.core.model.RedmineProject;
 import org.svenk.redmine.core.model.RedmineTicket;
 import org.svenk.redmine.core.model.RedmineTicketJournal;
 import org.svenk.redmine.core.model.RedmineTicketStatus;
 import org.svenk.redmine.core.model.RedmineTracker;
 import org.svenk.redmine.core.model.RedmineVersion;
 import org.svenk.redmine.core.model.RedmineTicket.Key;
 import org.svenk.redmine.core.util.RedmineTransportFactory;
 
 public class RedmineXmlRpcClient extends AbstractRedmineClient implements IRedmineClient {
 
 	private final static String RAILS_VERSION = "2.0.2";
 	
 	private final static String PLUGIN_VERSION = "v2";
 	
 	private final static String URL_ENDPOINT = "/eclipse_mylyn_connector/api";
 
 	private final static String RPC_TICKET_BY_ID = "Ticket.FindTicketById";
 
 	private final static String RPC_TICKET_SEARCH = "Ticket.SearchTickets";
 
 	private final static String RPC_GET_TICKET_JOURNALS = "Ticket.FindJournalsForIssue";
 
 	private final static String RPC_GET_TICKET_ATTACHMENTS = "Ticket.FindAttachmentsForIssue";
 
 	private final static String RPC_GET_TICKET_ALLOWED_STATUS = "Ticket.FindAllowedStatusesForIssue";
 
 	private final static String RPC_GET_TICKET_UPDATED_ID = "Ticket.FindTicketsByLastUpdate";
 	
 	private final static String RPC_PROJECT_FIND_ALL = "Project.FindAll";
 
 	private final static String RPC_STATUS_FIND_ALL = "Status.GetAll";
 
 	private final static String RPC_PRIORITY_FIND_ALL = "Priority.GetAll";
 
 	private final static String RPC_GET_PROJECT_TRACKERS = "ProjectBased.GetTrackersForProject";
 
 	private final static String RPC_GET_PROJECT_CUSTOM_ISSUE_FIELDS = "ProjectBased.GetIssueCustomFieldsForProject";
 	
 	private final static String RPC_GET_PROJECT_MEMBERS = "ProjectBased.GetMembersForProject";
 
 	private final static String RPC_GET_PROJECT_VERSIONS = "ProjectBased.GetVersionsForProject";
 
 	private final static String RPC_GET_PROJECT_ISSUE_CATEGORYS = "ProjectBased.GetIssueCategorysForProject";
 
 	private final static String RPC_GET_VERSION_INFORMATION = "Information.GetVersion";
 
 	private XmlRpcClientConfigImpl config;
 
 	private XmlRpcClient client;
 	
 	public RedmineXmlRpcClient(AbstractWebLocation location, RedmineClientData clientData, TaskRepository repository) {
 		super(location, clientData, repository);
 	}
 
 	private XmlRpcClient getClient() throws RedmineException {
 		if (client == null) {
 			config = new XmlRpcClientConfigImpl();
 
 			client = new XmlRpcClient();
 			client.setConfig(config);
 			
 			RedmineTransportFactory factory = new RedmineTransportFactory(client, location, URL_ENDPOINT);
 			client.setTransportFactory(factory);
 		}
 		return client;
 	}
 
 	public RedmineClientData getClientData() {
 		return data;
 	}
 
 	@Override
 	protected String checkClientVersion() throws RedmineException {
 		Object response = execute(RPC_GET_VERSION_INFORMATION);
 		if (response instanceof Object[]) {
 			Object[] object = (Object[])response;
 			if (object.length==3) {
 				if (!object[1].equals(RAILS_VERSION)) {
 					throw new RedmineException("This connector requires Rails version " + RAILS_VERSION + " (" + object[1] + ")");
 				}
 				if (!object[2].toString().endsWith(PLUGIN_VERSION)) {
 					throw new RedmineException("This connector requires Plugin version " + PLUGIN_VERSION + " (" + object[2] + ")");
 				}
 				return object[0].toString();
 			}
 		}
 		throw new RedmineException("Not possible to determine version information");
 	}
 
 	public RedmineTicket getTicket(int id, IProgressMonitor monitor) throws RedmineException {
 		RedmineTicket ticket =  parseResponse2Ticket(execute(RPC_TICKET_BY_ID, new Integer(id)));
 		completeTicket(ticket);
 		return ticket;
 	}
 
 	protected List<RedmineTicketStatus> getStatusesByTicket(int id) throws RedmineException {
 		return parseResponse2Statuses(execute(RPC_GET_TICKET_ALLOWED_STATUS, new Integer(id)));
 	}
 	
 	protected List<RedmineTicketJournal> getJournalsByTicket(int id) throws RedmineException {
 		return parseResponse2Journals(execute(RPC_GET_TICKET_JOURNALS, new Integer(id)));
 	}
 	
 	protected List<RedmineAttachment> getAttachmentsByTicket(int id) throws RedmineException {
 		return parseResponse2Attachment(execute(RPC_GET_TICKET_ATTACHMENTS, new Integer(id)));
 	}
 	
 	public void search(String searchParam, List<RedmineTicket> tickets)
 			throws RedmineException {
 		Object response = execute(RPC_TICKET_SEARCH, searchParam);
 		if (response instanceof Object[]) {
 			Object[] maps = (Object[]) response;
 			for (Object object : maps) {
 				RedmineTicket ticket = parseResponse2Ticket(object);
 				completeTicket(ticket);
 				tickets.add(ticket);
 			}
 		}
 	}
 	
 	protected void completeTicket(RedmineTicket ticket) throws RedmineException {
 		List<RedmineTicketStatus> statuses = getStatusesByTicket(ticket.getId());
 		ticket.setStatuses(statuses);
 
 		List<RedmineTicketJournal> journals = getJournalsByTicket(ticket.getId());
 		if (journals!=null) {
 			for (RedmineTicketJournal journal : journals) {
 				ticket.addJournal(journal);
 			}
 		}
 
 		List<RedmineAttachment> attachments = getAttachmentsByTicket(ticket.getId());
 		if (attachments!=null) {
 			for (RedmineAttachment attachment : attachments) {
 				ticket.addAttachment(attachment);
 			}
 		}
 		
 	}
 
 	public List<Integer> getChangedTicketId(Integer projectId, Date changedSince) throws RedmineException {
 		Object[] params = new Object[]{projectId, changedSince};
 		Object response = execute(RPC_GET_TICKET_UPDATED_ID, params);
 		return parseResponse2IntegerList(response);
 	}
 
 	public boolean hasAttributes() {
 		return data.lastupdate!=0;
 	}
 
 	public synchronized void updateAttributes(IProgressMonitor monitor, boolean force)
 			throws RedmineException {
 		
 		if (!force && hasAttributes()) {
 			return;
 		}
 
 		Object projects[] = (Object[])execute(RPC_PROJECT_FIND_ALL);
 		monitor.beginTask("Updating attributes", 2+projects.length);
 
 		data.priorities.clear();
 		for (Object response : (Object[]) execute(RPC_PRIORITY_FIND_ALL)) {
 			data.priorities.add(parseResponse2Priority(response));
 		}
 		monitor.worked(1);
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 
 		data.statuses.clear();
 		for (Object response : (Object[]) execute(RPC_STATUS_FIND_ALL)) {
 			data.statuses.add(parseResponse2Status(response));
 		}
 		monitor.worked(1);
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 		
 		data.projects.clear();
 		for (Object response : projects) {
 			RedmineProject project = parseResponse2Project(response);
 			RedmineProjectData projData = new RedmineProjectData(project);
 			data.projects.add(projData);
 			updateProjectAttributes(monitor, projData);
 		}
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 
 		data.lastupdate=new Date().getTime();
 	}
 	
 	private synchronized void updateProjectAttributes(IProgressMonitor monitor, RedmineProjectData projData) throws RedmineException {
 		Integer projId = new Integer(projData.getProject().getValue());
 
 		monitor.subTask("project " + projData.project.getName());
 		
 		projData.trackers.clear();
		for (Object projResponse : (Object[]) execute(RPC_GET_PROJECT_TRACKERS, projId)) {
			projData.trackers.add(parseResponse2Tracker(projResponse));
 		}
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 		
 		projData.categorys.clear();
 		for (Object projResponse : (Object[]) execute(RPC_GET_PROJECT_ISSUE_CATEGORYS, projId)) {
 			projData.categorys.add(parseResponse2IssueCategory(projResponse));
 		}
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 		
 		projData.versions.clear();
 		for (Object projResponse : (Object[]) execute(RPC_GET_PROJECT_VERSIONS, projId)) {
 			projData.versions.add(parseResponse2Version(projResponse));
 		}
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 		
 		projData.members.clear();
 		for (Object projResponse : (Object[]) execute(RPC_GET_PROJECT_MEMBERS, projId)) {
 			projData.members.add(parseResponse2Member(projResponse));
 		}
 		
 		projData.customTicketFields.clear();
 		for (Object projResponse : (Object[]) execute(RPC_GET_PROJECT_CUSTOM_ISSUE_FIELDS, projId)) {
 			projData.customTicketFields.add(parseResponse2CustomFields(projResponse));
 		}
 		
 		monitor.worked(1);
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 	}
 
 	private Object execute(String rpc, Object... params)
 			throws RedmineException {
 		XmlRpcClient client = getClient();
 		Object result = null;
 		try {
 			result = client.execute(rpc, params);
 		} catch (XmlRpcException e) {
 			if (e.linkedException instanceof RedmineRemoteException) {
 				throw (RedmineRemoteException)e.linkedException;
 			} else {
 				throw new RedmineRemoteException(e);
 			}
 		}
 		return result;
 	}
 
 	private RedmineTicket parseResponse2Ticket(Object response)
 			throws RedmineException {
 		RedmineTicket ticket = null;
 		HashMap<String, Object> map = parseResponse2HashMap(response);
 		if (map.get("id") instanceof Integer) {
 			ticket = new RedmineTicket(((Integer) map.get("id")).intValue());
 			ticket.putBuiltinValue(Key.SUBJECT, map.get("subject")
 					.toString());
 			ticket.putBuiltinValue(Key.DESCRIPTION, map.get("description")
 					.toString());
 			ticket
 					.putBuiltinValue(Key.AUTHOR, map.get("author")
 							.toString());
 
 			ticket.putBuiltinValue(Key.DONE_RATIO,  ((Integer)map.get("done_ratio")).intValue());
 			
 			ticket.setCreated((Date) map.get("created_on"));
 			ticket.setLastChanged((Date) map.get("updated_on"));
 			
 			//Handle Project for Ticket
 			Integer intValue = (Integer)map.get("project_id");
 			if (intValue==null) {
 				throw new RedmineRemoteException("Missing project ID for ticket");
 			}
 			RedmineProjectData projectData = data.getProjectFromId(intValue);
 			if (projectData==null) {
 				throw new RedmineRemoteException("Can't find project for ID: " + intValue.toString());
 			}
 			ticket.putBuiltinValue(Key.PROJECT, intValue);
 
 			//Handle RedmineTicketAttributes / ProjectAttributes
 			String xmlRpcKey;
 			for (Key key : this.attributeKeys) {
 				xmlRpcKey = redmineKey2XmlRpcKey(key);
 				intValue = (Integer)map.get(xmlRpcKey);
 				if (intValue!=null) {
 					ticket.putBuiltinValue(key, intValue);
 				}
 			}
 			
 			//customValues
 			Object customValues = map.get("custom_values");
 			if (customValues != null && customValues instanceof Object[]) {
 				for (Object customValue : (Object[])customValues) {
 					if (customValue instanceof HashMap) {
 						HashMap<String, Object> customValueMap = parseResponse2HashMap(customValue);
 						Integer customFieldId = (Integer)customValueMap.get("custom_field_id");
 						String customFieldValue = customValueMap.get("value").toString();
 						ticket.putCustomFieldValue(customFieldId, customFieldValue);
 					}
 				}
 			}
 			
 		}
 		return ticket;
 	}
 
 	private RedmineProject parseResponse2Project(Object response)
 			throws RedmineException {
 		RedmineProject project = null;
 		HashMap<String, Object> map = parseResponse2HashMap(response);
 		project = new RedmineProject(map.get("name").toString(),
 				((Integer) map.get("id")).intValue());
 		project.setIssueEditAllowed(Boolean.parseBoolean(map.get("issue_edit_allowed").toString()));
 		return project;
 	}
 
 	private RedmineMember parseResponse2Member(Object response)
 			throws RedmineException {
 		RedmineMember member = null;
 		HashMap<String, Object> map = parseResponse2HashMap(response);
 		member = new RedmineMember(
 				map.get("name").toString(),
 				((Integer)map.get("id")).intValue(),
 				((Boolean)map.get("assignable")).booleanValue());
 		return member;
 	}
 
 	private RedmineVersion parseResponse2Version(Object response)
 			throws RedmineException {
 		RedmineVersion version = null;
 		HashMap<String, Object> map = parseResponse2HashMap(response);
 		version = new RedmineVersion(map.get("name").toString(),
 				((Integer) map.get("id")).intValue());
 		return version;
 	}
 
 	private RedmineTracker parseResponse2Tracker(Object response)
 			throws RedmineException {
 		RedmineTracker tracker = null;
 		HashMap<String, Object> map = parseResponse2HashMap(response);
 		tracker = new RedmineTracker(map.get("name").toString(),
 				((Integer) map.get("id")).intValue());
 		return tracker;
 	}
 
 	private RedmineCustomTicketField parseResponse2CustomFields(Object response)
 	throws RedmineException {
 		RedmineCustomTicketField customValue = null;
 		HashMap<String, Object> map = parseResponse2HashMap(response);
 		int id = ((Integer)map.get("id")).intValue();
 		String type = map.get("type").toString();
 
 		//assigned trackers (id)
 		Object[] rawValues = (Object[])map.get("trackers");
 		int[] trackers = new int[rawValues.length];
 		for(int i=rawValues.length-1; i>=0; i--) {
 			if (rawValues[i] instanceof Integer) {
 				trackers[i] = ((Integer)rawValues[i]).intValue();
 			}
 		}
 		
 		//list-values
 		rawValues = (Object[])map.get("possible_values");
 		String[] listValues = null;
 		if (rawValues==null) {
 			listValues = new String[0];
 		} else {
 			listValues = new String[rawValues.length];
 			for(int i=rawValues.length-1; i>=0; i--) {
 				if (rawValues[i] instanceof String) {
 					listValues[i] = (String)rawValues[i];
 				}
 			}
 		}
 
 		customValue = new RedmineCustomTicketField(id, type);
 //		customValue.setDefaultValue(map.get("default_value").toString());
 		customValue.setMax(((Integer)map.get("max")).intValue());
 		customValue.setMin(((Integer)map.get("min")).intValue());
 		customValue.setName(map.get("name").toString());
 		customValue.setRequired(((Boolean)map.get("is_required")).booleanValue());
 		customValue.setSupportFilter(((Boolean)map.get("is_filter")).booleanValue());
 		customValue.setValidationRegex(map.get("regex").toString());
 		customValue.setListValues(listValues);
 		customValue.setTrackerId(trackers);
 		
 		return customValue;
 	}
 	
 	private List<RedmineTicketStatus> parseResponse2Statuses(Object response)
 			throws RedmineException {
 		
 		List<RedmineTicketStatus> statuses = null;
 		if (response instanceof Object[]) {
 			Object[] maps = (Object[]) response;
 			statuses = new ArrayList<RedmineTicketStatus>(maps.length);
 			for (Object object : maps) {
 				RedmineTicketStatus status = parseResponse2Status(object);
 				if (status!=null) {
 					statuses.add(status);
 				}
 			}
 		}
 		return statuses;
 	}
 
 	private RedmineTicketStatus parseResponse2Status(Object response)
 	throws RedmineException {
 		RedmineTicketStatus status = null;
 		HashMap<String, Object> map = parseResponse2HashMap(response);
 		status = new RedmineTicketStatus(map.get("name").toString(),
 				((Integer) map.get("id")).intValue());
 		status.setClosed(Boolean.parseBoolean(map.get("is_closed").toString()));
 		status.setDefaultStatus(Boolean.parseBoolean(map.get("is_default").toString()));
 
 		return status;
 	}
 	
 	private RedminePriority parseResponse2Priority(Object response)
 			throws RedmineException {
 		RedminePriority priority = null;
 		HashMap<String, Object> map = parseResponse2HashMap(response);
 		priority = new RedminePriority(
 				map.get("name").toString(),
 				((Integer) map.get("id")).intValue(), 
 				((Integer) map.get("position")).intValue());
 		priority.setDefaultPriority(Boolean.parseBoolean(map.get("is_default").toString()));
 
 		return priority;
 	}
 	
 	private RedmineIssueCategory parseResponse2IssueCategory(Object response)
 	throws RedmineException {
 		RedmineIssueCategory category = null;
 		HashMap<String, Object> map = parseResponse2HashMap(response);
 		category = new RedmineIssueCategory(map.get("name").toString(),
 				((Integer) map.get("id")).intValue());
 		
 		return category;
 	}
 	
 	private List<RedmineTicketJournal> parseResponse2Journals(Object response) throws RedmineException {
 		List<RedmineTicketJournal> journals = null;
 		if (response instanceof Object[]) {
 			Object[] maps = (Object[]) response;
 			journals = new ArrayList<RedmineTicketJournal>(maps.length);
 			
 			RedmineTicketJournal journal;
 			for (Object object : maps) {
 				if (object instanceof HashMap) {
 					HashMap<String, Object> map = parseResponse2HashMap(object);
 					journal = new RedmineTicketJournal();
 					journal.setId(Integer.parseInt(map.get("id").toString()));
 					journal.setNotes(map.get("notes").toString());
 					journal.setCreated((Date)map.get("created_on"));
 					journal.setEditable(Boolean.parseBoolean(map.get("editable_by_user").toString()));
 					journal.setAuthorId(Integer.parseInt(map.get("author_id").toString()));
 					journal.setAuthorName(map.get("author_name").toString());
 					journals.add(journal);
 				}
 			}
 		}
 		return journals;
 	}
 	
 	private List<RedmineAttachment> parseResponse2Attachment(Object response) throws RedmineException {
 		List<RedmineAttachment> attachments = null;
 		if (response instanceof Object[]) {
 			Object[] maps = (Object[]) response;
 			attachments = new ArrayList<RedmineAttachment>(maps.length);
 			
 			RedmineAttachment attachment;
 			for (Object object : maps) {
 				if (object instanceof HashMap) {
 					HashMap<String, Object> map = parseResponse2HashMap(object);
 					attachment = new RedmineAttachment(Integer.parseInt(map.get("id").toString()));
 					attachment.setCreated((Date)map.get("created_on"));
 					attachment.setAuthorId(Integer.parseInt(map.get("author_id").toString()));
 					attachment.setAuthorName(map.get("author_name").toString());
 					attachment.setFilename(map.get("filename").toString());
 					attachment.setFilesize(Integer.parseInt(map.get("filesize").toString()));
 					attachment.setContentType(map.get("content_type").toString());
 					attachment.setDigest(map.get("digest").toString());
 					Object description = map.get("description");
 					if (description!=null) {
 						attachment.setDescription(description.toString());
 					}
 					attachments.add(attachment);
 				}
 			}
 		}
 		return attachments;
 	}
 	
 	private List<Integer> parseResponse2IntegerList(Object response) throws RedmineException {
 		List<Integer> result = null;
 		if (response instanceof Object[]) {
 			Object[] values = (Object[]) response;
 			result = new ArrayList<Integer>(values.length);
 			for (Object object : values) {
 				try {
 					result.add(Integer.valueOf(object.toString()));
 				} catch (NumberFormatException e) {
 					throw new RedmineException(e);
 				}
 			}
 		}
 		return result;
 	}
 
 	private String redmineKey2XmlRpcKey(Key redmineKey) {
 		return redmineKey.name().toLowerCase() + "_id";
 	}
 
 	@SuppressWarnings("unchecked")
 	private HashMap<String, Object> parseResponse2HashMap(Object response) throws RedmineException {
 		if (response instanceof HashMap) {
 			return (HashMap)response;
 		}
 		throw new RedmineException("Invalid Response: HashMap expected");
 	}
 }
