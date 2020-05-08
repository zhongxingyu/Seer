 /*
  * Feb 22, 2006
  */
 package com.thinkparity.model.parity.model.document.history;
 
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.l10n.L18n;
 
 import com.thinkparity.model.parity.ParityException;
 import com.thinkparity.model.parity.model.audit.event.*;
 import com.thinkparity.model.parity.model.document.Document;
 import com.thinkparity.model.parity.model.session.InternalSessionModel;
 import com.thinkparity.model.xmpp.JabberId;
 import com.thinkparity.model.xmpp.user.User;
 
 /**
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 public class HistoryItemBuilder {
 
 	/**
 	 * Build a map of the user's id to their user info for all audit events.
 	 * 
 	 * @param auditEvents
 	 *            The list of audit events.
 	 * @return A map of the user ids to their user info.
 	 * @throws ParityException
 	 */
 	private static Map<JabberId, User> buildUserMap(
 			final Iterable<AuditEvent> auditEvents,
 			final InternalSessionModel iSModel) throws ParityException {
 		final List<JabberId> jabberIds = new LinkedList<JabberId>();
 		for(final AuditEvent auditEvent : auditEvents) {
 			switch(auditEvent.getType()) {
 			case ARCHIVE:
 			case CLOSE:
 			case CREATE:
 				if(!jabberIds.contains(auditEvent.getCreatedBy()))
 						jabberIds.add(auditEvent.getCreatedBy());
 				break;
 			case RECEIVE:
 				if(!jabberIds.contains(((ReceiveEvent) auditEvent).getReceivedFrom()))
 					jabberIds.add(((ReceiveEvent) auditEvent).getReceivedFrom());
 				if(!jabberIds.contains(auditEvent.getCreatedBy()))
 					jabberIds.add(auditEvent.getCreatedBy());
 				break;
 			case RECEIVE_KEY:
 				if(!jabberIds.contains(((ReceiveKeyEvent) auditEvent).getReceivedFrom()))
 					jabberIds.add(((ReceiveKeyEvent) auditEvent).getReceivedFrom());
 				if(!jabberIds.contains(auditEvent.getCreatedBy()))
 					jabberIds.add(auditEvent.getCreatedBy());
 				break;
 			case REQUEST_KEY:
 				if(!jabberIds.contains(((RequestKeyEvent) auditEvent).getRequestedBy()))
 					jabberIds.add(((RequestKeyEvent) auditEvent).getRequestedBy());
 				if(!jabberIds.contains(((RequestKeyEvent) auditEvent).getRequestedFrom()))
 					jabberIds.add(((RequestKeyEvent) auditEvent).getRequestedFrom());
 				if(!jabberIds.contains(auditEvent.getCreatedBy()))
 					jabberIds.add(auditEvent.getCreatedBy());
 				break;
 			case SEND:
 				for(final JabberId sentTo : ((SendEvent) auditEvent).getSentTo()) {
 					if(!jabberIds.contains(sentTo))
 						jabberIds.add(sentTo);
 				}
 				if(!jabberIds.contains(auditEvent.getCreatedBy()))
 					jabberIds.add(auditEvent.getCreatedBy());
 				break;
 			case SEND_KEY:
 				if(!jabberIds.contains(((SendKeyEvent) auditEvent).getSentTo()))
 					jabberIds.add(((SendKeyEvent) auditEvent).getSentTo());
 				if(!jabberIds.contains(auditEvent.getCreatedBy()))
 					jabberIds.add(auditEvent.getCreatedBy());
 				break;
 			default: Assert.assertUnreachable("");
 			}
 		}
 		final List<User> users = iSModel.readUsers(jabberIds);
 		final Map<JabberId, User> userMap = new LinkedHashMap<JabberId, User>();
 		for(final User user : users) { userMap.put(user.getId(), user); }
 		return userMap;
 	}
 
 	/**
 	 * The document.
 	 * 
 	 */
 	private final Document document;
 
 	/**
 	 * Localization.
 	 * 
 	 */
 	private final L18n l18n;
 
 	/**
 	 * Create a HistoryItemBuilder.
 	 * 
 	 */
 	public HistoryItemBuilder(final L18n l18n, final Document document) {
 		super();
 		this.l18n = l18n;
 		this.document = document;
 	}
 
 	/**
 	 * Create the history for the list of audit events.
 	 * 
 	 * @param auditEvents
 	 *            A list of audit events.
 	 * @param iSModel
 	 *            An internal session model.
 	 * @return A list of history events.
 	 * @throws ParityException
 	 */
 	public List<HistoryItem> create(final Iterable<AuditEvent> auditEvents,
 			final InternalSessionModel iSModel) throws ParityException {
 		final List<HistoryItem> history = new LinkedList<HistoryItem>();
 		final Map<JabberId, User> auditUsers = buildUserMap(auditEvents, iSModel);
 		for(final AuditEvent auditEvent : auditEvents) {
 			history.add(create(auditEvent, auditUsers));
 		}
 		return history;
 	}
 
 	/**
 	 * Create a history item.
 	 * @param document
 	 *            The document.
 	 * @param auditEvent
 	 *            The audit event.
 	 * @param sModel
 	 *            The session model.
 	 * 
 	 * @return This history item.
 	 */
 	private HistoryItem create(final AuditEvent auditEvent,
 			final Map<JabberId, User> auditEventUsers) throws ParityException {
 		switch(auditEvent.getType()) {
 		case ARCHIVE: return create(document, (ArchiveEvent) auditEvent, auditEventUsers);
 		case CLOSE: return create(document, (CloseEvent) auditEvent, auditEventUsers);
 		case CREATE: return create(document, (CreateEvent) auditEvent, auditEventUsers);
 		case RECEIVE: return create(document, (ReceiveEvent) auditEvent, auditEventUsers);
 		case RECEIVE_KEY: return create(document, (ReceiveKeyEvent) auditEvent, auditEventUsers);
 		case REQUEST_KEY: return create(document, (RequestKeyEvent) auditEvent, auditEventUsers);
 		case SEND: return create(document, (SendEvent) auditEvent, auditEventUsers);
 		case SEND_KEY: return create(document, (SendKeyEvent) auditEvent, auditEventUsers);
 		default:
 			throw Assert.createUnreachable("Unuspported audit event:  " + auditEvent.getType());
 		}
 	}
 
 	private HistoryItem create(final Document document, final ArchiveEvent event, final Map<JabberId, User> eventUsers) {
 		final Object[] arguments = new Object[] {
 				event.getCreatedOn().getTime()
 		};
 		final HistoryItem item = new HistoryItem();
 		item.setDate(event.getCreatedOn());
 		item.setDocumentId(document.getId());
 		item.setEvent(getString("eventText.ARCHIVE", arguments));
 		return item;
 	}
 
 	private HistoryItem create(final Document document, final CloseEvent event, final Map<JabberId, User> eventUsers) {
 		final Object[] arguments = new Object[] {
 			getName(eventUsers.get(event.getClosedBy()))
 		};
 		final HistoryItem close = new HistoryItem();
 		close.setDate(event.getCreatedOn());
 		close.setDocumentId(event.getArtifactId());
 		close.setEvent(getString("eventText.CLOSE", arguments));
 		return close;
 	}
 
 	private HistoryItem create(final Document document, final CreateEvent event, final Map<JabberId, User> eventUsers) {
 		final HistoryItem create = new HistoryItem();
 		create.setDate(event.getCreatedOn());
 		create.setDocumentId(event.getArtifactId());
 		create.setEvent(getString("eventText.CREATE"));
 		return create;
 	}
 
 	private HistoryItem create(final Document document, final ReceiveEvent event, final Map<JabberId, User> eventUsers) {
 		final Object[] arguments = new Object[] {
 			getName(eventUsers.get(event.getReceivedFrom()))
 		};
 		final HistoryItem receive = new HistoryItem();
 		receive.setDate(event.getCreatedOn());
 		receive.setDocumentId(event.getArtifactId());
 		receive.setEvent(getString("eventText.RECEIVE", arguments));
 		receive.setVersionId(event.getArtifactVersionId());
 		return receive;
 	}
 
 	private HistoryItem create(final Document document, final ReceiveKeyEvent event, final Map<JabberId, User> eventUsers) {
 		final Object[] arguments = new Object[] {
 			getName(eventUsers.get(event.getReceivedFrom()))
 		};
 		final HistoryItem receiveKey = new HistoryItem();
 		receiveKey.setDate(event.getCreatedOn());
 		receiveKey.setDocumentId(event.getArtifactId());
 		receiveKey.setEvent(getString("eventText.RECEIVE_KEY", arguments));
 		return receiveKey;
 	}
 
 	private HistoryItem create(final Document document, final RequestKeyEvent event, final Map<JabberId, User> eventUsers) {
 		final Object[] arguments = new Object[] {
 			getName(eventUsers.get(event.getRequestedBy())),
 			getName(eventUsers.get(event.getRequestedFrom()))
 		};
 		final HistoryItem requestKey = new HistoryItem();
 		requestKey.setDate(event.getCreatedOn());
 		requestKey.setDocumentId(document.getId());
 		requestKey.setEvent(getString("eventText.REQUEST_KEY", arguments));
 		return requestKey;
 	}
 
 	private HistoryItem create(final Document document, final SendEvent event, final Map<JabberId, User> eventUsers) {
 		final StringBuffer sentToBuffer = new StringBuffer();
 		for(final JabberId jabberId : event.getSentTo()) {
 			if(1 > sentToBuffer.length()) {
 				sentToBuffer.append(
 					getString("eventText.SEND.TO.0",
 					new Object[] {
 						getName(eventUsers.get(jabberId))
 					}));
 			}
 			else {
 				sentToBuffer.append(
					getString("eventText.SEND.TO.N",
 					new Object[] {
 						getName(eventUsers.get(jabberId))
 					}));
 			}
 		}
 		final Object[] arguments = new Object[] {
 			sentToBuffer.toString()
 		};
 		final HistoryItem send = new HistoryItem();
 		send.setDate(event.getCreatedOn());
 		send.setDocumentId(event.getArtifactId());
 		send.setEvent(getString("eventText.SEND", arguments));
 		send.setVersionId(event.getArtifactVersionId());
 		return send;
 	}
 
 	private HistoryItem create(final Document document, final SendKeyEvent event,final Map<JabberId, User> eventUsers) {
 		final Object[] arguments = new Object[] {
 			getName(eventUsers.get(event.getSentTo()))
 		};
 		final HistoryItem sendKey = new SendKeyHistoryItem();
 		sendKey.setDate(event.getCreatedOn());
 		sendKey.setDocumentId(event.getArtifactId());
 		sendKey.setVersionId(event.getArtifactVersionId());
 		sendKey.setEvent(getString("eventText.SEND_KEY", arguments));
 		return sendKey;
 	}
 
 	private String getName(final User user) {
 		final String organization = user.getOrganization();
 		if(null ==  organization) {
 			final Object[] arguments = new Object[] {
 					user.getFirstName(),
 					user.getLastName()
 			};
 			return getString("user.nameMinusOrganization", arguments);
 		}
 		else {
 			final Object[] arguments = new Object[] {
 					user.getFirstName(),
 					user.getLastName(),
 					user.getOrganization()
 			};
 			return getString("user.name", arguments);
 		}
 	}
 
 	/**
 	 * @see L18n#getString(java.lang.String)
 	 * 
 	 */
 	private String getString(final String localKey) {
 		return l18n.getString(localKey);
 	}
 
 	/**
 	 * @see L18n#getString(java.lang.String, java.lang.Object[])
 	 * 
 	 */
 	private String getString(final String localKey, final Object[] arguments) {
 		return l18n.getString(localKey, arguments);
 	}
 }
