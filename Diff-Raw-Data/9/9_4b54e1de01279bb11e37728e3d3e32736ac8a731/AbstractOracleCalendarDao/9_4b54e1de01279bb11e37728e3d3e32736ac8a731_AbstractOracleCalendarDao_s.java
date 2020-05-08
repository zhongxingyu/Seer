 /**
  * Licensed to Jasig under one or more contributor license
  * agreements. See the NOTICE file distributed with this work
  * for additional information regarding copyright ownership.
  * Jasig licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a
  * copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.jasig.schedassist.impl.oraclecalendar;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.fortuna.ical4j.data.CalendarBuilder;
 import net.fortuna.ical4j.data.ParserException;
 import net.fortuna.ical4j.model.Calendar;
 import net.fortuna.ical4j.model.Component;
 import net.fortuna.ical4j.model.ComponentList;
 import net.fortuna.ical4j.model.DateTime;
 import net.fortuna.ical4j.model.Parameter;
 import net.fortuna.ical4j.model.Property;
 import net.fortuna.ical4j.model.PropertyList;
 import net.fortuna.ical4j.model.component.VEvent;
 import net.fortuna.ical4j.model.parameter.PartStat;
 import net.fortuna.ical4j.model.property.Attendee;
 import net.fortuna.ical4j.model.property.Uid;
 import net.fortuna.ical4j.util.CompatibilityHints;
 import oracle.calendar.sdk.Api;
 import oracle.calendar.sdk.Api.StatusException;
 import oracle.calendar.sdk.Handle;
 import oracle.calendar.sdk.RequestResult;
 import oracle.calendar.sdk.Session;
 
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jasig.schedassist.ConflictExistsException;
 import org.jasig.schedassist.ICalendarDataDao;
 import org.jasig.schedassist.NullAffiliationSourceImpl;
 import org.jasig.schedassist.SchedulingException;
 import org.jasig.schedassist.impl.events.AutomaticAppointmentCancellationEvent;
 import org.jasig.schedassist.impl.events.AutomaticAppointmentCancellationEvent.Reason;
 import org.jasig.schedassist.impl.events.AutomaticAttendeeRemovalEvent;
 import org.jasig.schedassist.model.AppointmentRole;
 import org.jasig.schedassist.model.AvailabilityReflection;
 import org.jasig.schedassist.model.AvailableBlock;
 import org.jasig.schedassist.model.AvailableSchedule;
 import org.jasig.schedassist.model.AvailableVersion;
 import org.jasig.schedassist.model.CommonDateOperations;
 import org.jasig.schedassist.model.DefaultEventUtilsImpl;
 import org.jasig.schedassist.model.ICalendarAccount;
 import org.jasig.schedassist.model.IScheduleOwner;
 import org.jasig.schedassist.model.IScheduleVisitor;
 import org.jasig.schedassist.model.SchedulingAssistantAppointment;
 import org.jasig.schedassist.model.VisitorLimit;
 import org.jasig.schedassist.oraclecalendar.OracleCalendarSDKSupport;
 import org.jasig.schedassist.oraclecalendar.OracleCalendarServerNode;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationEventPublisher;
 
 /**
  * Abstract Oracle specific implementation of {@link ICalendarDataDao}.
  * Depends on {@link OracleCalendarSDKSupport} for initialization of the
  * Oracle SDK Native library.
  * 
  * Implements all of the {@link ICalendarDataDao} methods in an Oracle specific way, but adds
  * 2 abstract methods ({@link #getSession(ICalendarAccount, OracleCalendarServerNode)} and 
  * {@link #doneWithSession(Session, OracleCalendarServerNode, boolean)}) for managing
  * connections to the Oracle Calendar system.
  * 
  * @author Nicholas Blair, nblair@doit.wisc.edu
  * @version $Id: AbstractOracleCalendarDao.java $
  */
 public abstract class AbstractOracleCalendarDao extends
 		OracleCalendarSDKSupport implements ICalendarDataDao {
 
 	/**
 	 * A String containing a "CRLF" (carriage-return, line-feed)
 	 */
 	private static final String CRLF = new String(new byte [] { 0x0D, 0x0A });
 	static {
 		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
 	}
 	
 	protected final Log LOG = LogFactory.getLog(this.getClass());
 	
 	private Map<String, OracleCalendarServerNode> serverNodes = new HashMap<String, OracleCalendarServerNode>();
 	private OracleEventUtilsImpl oracleEventUtils = new OracleEventUtilsImpl(new NullAffiliationSourceImpl());
 	private OracleGUIDSource oracleGUIDSource;
 	private ApplicationEventPublisher applicationEventPublisher;
 	
 	/**
 	 * @param serverNodes the serverNodes to set
 	 */
 	@Autowired
 	public void setServerNodes(Map<String, OracleCalendarServerNode> serverNodes) {
 		this.serverNodes = serverNodes;
 	}
 	/**
 	 * @param oracleEventUtils the oracleEventUtils to set
 	 */
 	@Autowired
 	public void setOracleEventUtils(OracleEventUtilsImpl oracleEventUtils) {
 		this.oracleEventUtils = oracleEventUtils;
 	}
 	/**
 	 * @param oracleGUIDSource the oracleGUIDSource to set
 	 */
 	@Autowired
 	public void setOracleGUIDSource(OracleGUIDSource oracleGUIDSource) {
 		this.oracleGUIDSource = oracleGUIDSource;
 	}
 	/**
 	 * @param applicationEventPublisher the applicationEventPublisher to set
 	 */
 	@Autowired
 	public void setApplicationEventPublisher(
 			ApplicationEventPublisher applicationEventPublisher) {
 		this.applicationEventPublisher = applicationEventPublisher;
 	}
 	/**
 	 * Get a {@link Session} for the specified account.
 	 * Implementations must call {@link Session#setIdentity(int, String)} appropriately.
 	 * @param calendarAccount
 	 * @return
 	 */
 	protected abstract Session getSession(ICalendarAccount calendarAccount, OracleCalendarServerNode serverNode) throws Api.StatusException;
 	
 	/**
 	 * Implementations can choose what to do when done with the {@link Session}.
 	 * If the invalidate parameter is set to true, it should not be re-used.
 	 * 
 	 * @param session
 	 * @param serverNode
 	 * @param invalidate
 	 */
 	protected abstract void doneWithSession(Session session, OracleCalendarServerNode serverNode, boolean invalidate);
 	
 	/**
 	 * 
 	 * @param account
 	 * @return the {@link OracleCalendarServerNode} for the account
 	 */
 	protected final OracleCalendarServerNode getOracleCalendarServerNode(ICalendarAccount account) {
 		String nodeId = this.getOracleNodeId(account);
 		return this.serverNodes.get(nodeId);
 	}
 	/*
 	 * (non-Javadoc)
 	 * @see org.jasig.schedassist.ICalendarDataDao#getCalendar(org.jasig.schedassist.model.ICalendarAccount, java.util.Date, java.util.Date)
 	 */
 	@Override
 	public final Calendar getCalendar(ICalendarAccount calendarAccount,
 			Date startDate, Date endDate) {
 		boolean invalidateSession = false;
 		// make agenda available to catch blocks
 		String agenda = null;
 		Session session = null;
 		OracleCalendarServerNode serverNode = getOracleCalendarServerNode(calendarAccount);
 		try {
 			session = getSession(calendarAccount, serverNode);
 
 			agenda = getCalendarInternal(calendarAccount, startDate, endDate, session);
 			
 			Calendar result = parseAgenda(agenda);
 			result = purgeDeclinedAttendees(result, session, calendarAccount);
 			
 			return result;
 		} catch (ParserException e) {
 			LOG.error("caught ParserException in getCalendar for " + calendarAccount, e);
 			throw new OracleCalendarParserException("caught ParserException", agenda, e);
 		} catch (Api.StatusException e) {
 			LOG.error("caught Api.StatusException in getCalendar for " + calendarAccount, e);
 			invalidateSession = true;
 			throw new OracleCalendarDataAccessException("caught Oracle Calendar Exception", e);
 		} finally {
 			doneWithSession(session, serverNode, invalidateSession);
 		}
 		
 	}
 
 	/**
 	 * This method will inspect {@link IScheduleVisitor} {@link Attendee}s among the {@link SchedulingAssistantAppointment}s
 	 * in the {@link Calendar} argument.
 	 * If an {@link Attendee} on an {@link SchedulingAssistantAppointment} has {@link Partstat#DECLINED}, the appointment
 	 * will be cancelled (if one on one or lone visitor on group appt) or the attendee will be removed (group appointment
 	 * with multiple attending visitors).
 	 * 
 	 * @param calendar
 	 * @param session
 	 * @param owner
 	 * @return the calendar minus any events or attendees that have been removed.
 	 * @throws StatusException 
 	 */
 	protected Calendar purgeDeclinedAttendees(Calendar calendar, Session session, ICalendarAccount owner) throws StatusException {
 		ComponentList resultList = new ComponentList();
 		ComponentList componentList = calendar.getComponents(VEvent.VEVENT);
 		for(Object o: componentList) {
 			VEvent event = (VEvent) o;
 			final boolean hasAvailableAppointmentProperty = SchedulingAssistantAppointment.TRUE.equals(event.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT));
 			final boolean isAttendingAsOwner = this.oracleEventUtils.isAttendingAsOwner(event, owner);
 			if(hasAvailableAppointmentProperty && isAttendingAsOwner) {
 				PropertyList attendeeList = this.oracleEventUtils.getAttendeeListFromEvent(event);		
 				Property visitorLimitProp = event.getProperty(VisitorLimit.VISITOR_LIMIT);
 				final int visitorLimit = Integer.parseInt(visitorLimitProp.getValue());
 				boolean addEventToResult = true;
 				for(Object a : attendeeList) {
 					Property attendee = (Property) a;
 					if(PartStat.DECLINED.equals(attendee.getParameter(PartStat.PARTSTAT))) {
 						LOG.trace("found attendee that has DECLINED event: " + attendee);
 						Parameter appointmentRole = attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE);
 						if(AppointmentRole.OWNER.equals(appointmentRole) ) {
 							// remove whole appointment
 							Uid eventUid = event.getUid();
 							cancelAppointmentInternal(session, eventUid);
 							LOG.warn("purgeDeclinedAttendees successfully cancelled appointment due to owner decline " + event);
 							addEventToResult = false;
 							this.applicationEventPublisher.publishEvent(new AutomaticAppointmentCancellationEvent(event, owner, Reason.OWNER_DECLINED));
 							break;
 						} else if (AppointmentRole.VISITOR.equals(appointmentRole)) {
 							int availableVisitorCount = this.oracleEventUtils.getScheduleVisitorCount(event);
 							if(visitorLimit > 1 && availableVisitorCount > 1) {
 								// remove only the attendee (leave event)
 								event.getProperties().remove(attendee);
 								replaceEventInternal(session, event);
 								LOG.warn("purgeDeclinedAttendees successfully removed declined attendee from group appointment " + event);
 								this.applicationEventPublisher.publishEvent(new AutomaticAttendeeRemovalEvent(event, owner, attendee));
 							} else {
 								// either one on one appointment or group appointment with only 1 visitor
 								// remove whole appointment
 								Uid eventUid = event.getUid();
 								cancelAppointmentInternal(session, eventUid);
 								LOG.warn("purgeDeclinedAttendees successfully cancelled appointment due to no remaining visitors " + event);
 								addEventToResult = false;
 								this.applicationEventPublisher.publishEvent(new AutomaticAppointmentCancellationEvent(event, owner, Reason.NO_REMAINING_VISITORS));
 								break;
 							}
 						}
 					} 
 				}
 				
 				if(addEventToResult) {
 					resultList.add(event);
 				}
 			} else {
 				if(LOG.isTraceEnabled()) {
 					String eventUid = "not set";
 					if(event.getUid() != null) {
 						eventUid = event.getUid().getValue();
 					}
 					LOG.trace("event (UID=" + eventUid + ") not a candidate for purge, hasAvailableAppointmentProperty=" + hasAvailableAppointmentProperty + ", isAttendingAsOwner=" + isAttendingAsOwner);
 				}
 				resultList.add(event);
 			}
 		}
 		Calendar result = new Calendar(resultList);
 		return result;
 	}
 	/*
 	 * (non-Javadoc)
 	 * @see org.jasig.schedassist.ICalendarDataDao#getExistingAppointment(org.jasig.schedassist.model.IScheduleOwner, org.jasig.schedassist.model.AvailableBlock)
 	 */
 	@Override
 	public final VEvent getExistingAppointment(IScheduleOwner owner,
 			AvailableBlock block) {
 		OracleCalendarServerNode serverNode = getOracleCalendarServerNode(owner.getCalendarAccount());
 
 		// make agenda available to catch blocks
 		String agenda = null;
 		Session session = null;
 		boolean invalidateSession = false;
 		
 		try {
 			session = getSession(owner.getCalendarAccount(), serverNode);
 
 			return getAvailableAppointmentInternal(owner, block.getStartTime(), block.getEndTime(), session);
 		} catch (ParserException e) {
 			LOG.error("caught ParserException in getExistingAppointment for " + owner + " and " + block, e);
 			throw new OracleCalendarParserException("caught ParserException", agenda, e);
 		} catch (Api.StatusException e) {
 			LOG.error("caught Api.StatusException in getExistingAppointment for " + owner + " and " + block, e);
 			invalidateSession = true;
 			throw new OracleCalendarDataAccessException("caught Api.StatusException in getExistingAppointment", e);
 		} finally {
 			doneWithSession(session, serverNode, invalidateSession);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jasig.schedassist.ICalendarDataDao#createAppointment(org.jasig.schedassist.model.IScheduleVisitor, org.jasig.schedassist.model.IScheduleOwner, org.jasig.schedassist.model.AvailableBlock, java.lang.String)
 	 */
 	@Override
 	public final VEvent createAppointment(IScheduleVisitor visitor,
 			IScheduleOwner owner, AvailableBlock block, String eventDescription) {
 		OracleCalendarServerNode serverNode = getOracleCalendarServerNode(owner.getCalendarAccount());
 
 		Session session = null;
 		boolean invalidateSession = false;
 		final String logEventKey = RandomStringUtils.randomAlphanumeric(16);
 		
 		try {
 			session = getSession(owner.getCalendarAccount(), serverNode);
 
 			final String ownerGuid = locateOracleGuid(owner.getCalendarAccount(), session);
 			final String visitorGuid = locateOracleGuid(visitor.getCalendarAccount(), session);
 			VEvent event = this.oracleEventUtils.constructAvailableAppointment(
 					block, 
 					owner,
 					ownerGuid,
 					visitor, 
 					visitorGuid,
 					eventDescription);
 
 			Calendar calendar = this.oracleEventUtils.wrapEventInCalendar(event);
 
 			
 			if(LOG.isDebugEnabled()) {
 				LOG.debug("createAppointment " + logEventKey + " attempting first Session#storeEvents for " + owner + ", " + visitor + ", " + block + ", " + event);
 			}
 			RequestResult requestResults = new RequestResult();
 			session.storeEvents(getOracleCreateFlags(), calendar.toString(), requestResults);
 
 			String eventUID = requestResults.getFirstResult().getUID();
 			if(LOG.isDebugEnabled()) {
 				LOG.debug("createAppointment " + logEventKey + " first Session#storeEvents results: " + requestResults.toString());
 			}
 			event.getProperties().add(new Uid(eventUID));
 
 			calendar = this.oracleEventUtils.wrapEventInCalendar(event);
 
 			if(LOG.isDebugEnabled()) {
 				LOG.debug("createAppointment " + logEventKey + " attempting second Session#storeEvents, event uid: " + eventUID);
 			}
 			session.storeEvents(getOracleModifyFlags(), calendar.toString(), requestResults);
 
 			if(LOG.isDebugEnabled()) {
 				LOG.debug("createAppointment " + logEventKey + " second Session#storeEvents results: " + requestResults.toString());
 			}
 			return event;
 		} catch (Api.StatusException e) {
 			if(e.getStatus() == (Api.CSDK_STAT_SECUR_CANTBOOKATTENDEE | Api.CSDK_STATMODE_FATAL)) {
 				//TODO note that this exact error code will also be raised when attempting to create an appointment with resource that is already booked
 				LOG.error(logEventKey + " createAppointment failed due to account not accepting invitations, visitor: " + visitor + ", owner: " + owner);
 				throw new VisitorDeclinedInvitationsException("createAppointment failed due to visitor not accepting invitations: visitor: " + visitor);
 			}
 			invalidateSession = true;
 			LOG.error(logEventKey + " caught Api.StatusException in createAppointment for " + owner + ", " + visitor + ", and " + block, e);
 			throw new OracleCalendarDataAccessException("caught Api.StatusException in createAppointment", e);
 		} finally {
 			doneWithSession(session, serverNode, invalidateSession);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jasig.schedassist.ICalendarDataDao#cancelAppointment(org.jasig.schedassist.model.IScheduleVisitor, org.jasig.schedassist.model.IScheduleOwner, net.fortuna.ical4j.model.component.VEvent)
 	 */
 	@Override
 	public final void cancelAppointment(IScheduleVisitor visitor, IScheduleOwner owner, VEvent event) {
 		Validate.notNull(event, "event argument cannot be null for cancelAppointment");
 		OracleCalendarServerNode serverNode = getOracleCalendarServerNode(owner.getCalendarAccount());
 		Session session = null;
 		boolean invalidateSession = false;
 		Uid eventUid = event.getUid();
 		try {
 			session = getSession(owner.getCalendarAccount(), serverNode);
 			cancelAppointmentInternal(session, eventUid);
 		} catch (Api.StatusException e) {
 			LOG.error("caught Api.StatusException in cancelAppointment for " + owner + " and " + eventUid, e);
 			invalidateSession = true;
 			throw new OracleCalendarDataAccessException("caught Api.StatusException", e);
 		} finally {
 			doneWithSession(session, serverNode, invalidateSession);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param session
 	 * @param eventUid
 	 * @throws StatusException
 	 */
 	protected final void cancelAppointmentInternal(Session session, Uid eventUid) throws StatusException {
 		RequestResult requestResult = new RequestResult();
 		LOG.debug("cancelAppointmentInternal calling Session#deleteEvents for event uid: " + eventUid);
 		if(eventUid != null) {
 			session.deleteEvents(Api.CSDK_FLAG_NONE, 
 				new String[] { eventUid.getValue() }, 
 				null, 
 				Api.CSDK_THISINSTANCE, 
 				requestResult);
 			LOG.debug("cancelAppointmentInternal Session#deleteEvents results for event uid " + eventUid + ": " + requestResult.toString());
 		} else {
 			LOG.error("cancelAppointmentInternal skipping call to session.deleteEvents as eventUid argument is null");
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jasig.schedassist.ICalendarDataDao#joinAppointment(org.jasig.schedassist.model.IScheduleVisitor, org.jasig.schedassist.model.IScheduleOwner, net.fortuna.ical4j.model.component.VEvent)
 	 */
 	@Override
 	public final VEvent joinAppointment(IScheduleVisitor visitor,
 			IScheduleOwner owner, VEvent appointment)
 			throws SchedulingException {
 		OracleCalendarServerNode serverNode = getOracleCalendarServerNode(owner.getCalendarAccount());
 
 		Session session = null;
 		boolean invalidateSession = false;
 		Uid eventUid = appointment.getUid();
 		try {
 			session = getSession(owner.getCalendarAccount(), serverNode);
 			
 			// last check of visitor limit
 			Property visitorLimit = appointment.getProperty(VisitorLimit.VISITOR_LIMIT);
 			if(null == visitorLimit) {
 				throw new SchedulingException("Cannot join an appointment with no visitor limit defined");
 			} else {
 				int v = Integer.parseInt(visitorLimit.getValue());
 				if(v < 2) {
 					throw new SchedulingException("Visitor Limit on appointment restricts join");
 				}
 			}
 
 			final String visitorGuid = locateOracleGuid(visitor.getCalendarAccount(), session);
 			Attendee visitorAttendee = this.oracleEventUtils.constructAvailableAttendee(visitor.getCalendarAccount(), AppointmentRole.VISITOR, visitorGuid);
 			if(LOG.isDebugEnabled()) {
 				LOG.debug("attempting to add attendee " + visitorAttendee + " to appointment: " + appointment);
 			}
 
 			appointment.getProperties().add(visitorAttendee);
 			Calendar calendar = this.oracleEventUtils.wrapEventInCalendar(appointment);
 
 			if(LOG.isDebugEnabled()) {
 				LOG.debug("joinAppointment " + eventUid + " attempting first Session#storeEvents for " + owner + ", " + visitor + ", " + appointment);
 			}
 			RequestResult requestResults = new RequestResult();
 			session.storeEvents(getOracleModifyFlags(), calendar.toString(), requestResults);
 			if(LOG.isDebugEnabled()) {
 				LOG.debug("joinAppointment " + eventUid + " first Session#storeEvents complete, capi result: " + requestResults.toString());
 			}
 
 			// a 2nd storeEvents MUST be called on the same event to get PARTSTAT=ACCEPTED to persist
 			session.storeEvents(getOracleModifyFlags(), calendar.toString(), requestResults);
 			if(LOG.isDebugEnabled()) {
 				LOG.debug("joinAppointment " + eventUid + " second Session#storeEventsstoreEvents complete: " + requestResults.toString());
 			}
 			
 			return appointment;
 		} catch (Api.StatusException e) {
 			LOG.error("caught Api.StatusException in joinAppoinment for owner " + owner + " and " + visitor + " and " + eventUid, e);
 			invalidateSession = true;
 			throw new OracleCalendarDataAccessException("caught Api.StatusException in joinAppointment", e);
 		} finally {
 			doneWithSession(session, serverNode, invalidateSession);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jasig.schedassist.ICalendarDataDao#leaveAppointment(org.jasig.schedassist.model.IScheduleVisitor, org.jasig.schedassist.model.IScheduleOwner, net.fortuna.ical4j.model.component.VEvent)
 	 */
 	@Override
 	public final VEvent leaveAppointment(IScheduleVisitor visitor,
 			IScheduleOwner owner, VEvent appointment)
 			throws SchedulingException {
 		OracleCalendarServerNode serverNode = getOracleCalendarServerNode(owner.getCalendarAccount());
 
 		Session session = null;
 		boolean invalidateSession = false;
 		Uid eventUid = appointment.getUid();
 		try {
 			session = getSession(owner.getCalendarAccount(), serverNode);
 
 			Date startTime = appointment.getStartDate().getDate();
 			Date endTime = appointment.getEndDate(true).getDate();
 			VEvent targetAppointment = getAvailableAppointmentInternal(owner, startTime, endTime, session);
 			if(null == targetAppointment) {
 				throw new SchedulingException("leaveAppointment: appointment does not exist in  schedule for " + owner + ", " + visitor + "; appointment: " + appointment);
 			}
 			// last check of visitor limit
 			Property visitorLimit = targetAppointment.getProperty(VisitorLimit.VISITOR_LIMIT);
 			if(null == visitorLimit) {
 				throw new SchedulingException("Cannot leave an appointment with no visitor limit defined");
 			} else {
 				int v = Integer.parseInt(visitorLimit.getValue());
 				if(v < 2) {
 					throw new SchedulingException("Visitor Limit on appointment restricts join");
 				}
 			}
 			
 			PropertyList attendeeList = targetAppointment.getProperties(Attendee.ATTENDEE);
 			for(Object o : attendeeList) {
 				Attendee attendee = (Attendee) o;
 				if(this.oracleEventUtils.attendeeMatchesPerson(attendee, visitor.getCalendarAccount())) {
 					LOG.debug(eventUid + " leaveAppointment found matching attendee: " + attendee);
 					targetAppointment.getProperties().remove(o);
 					break;
 				}
 			}
 			
 			replaceEventInternal(session, targetAppointment);
 			
 			return targetAppointment;
 			
 		} catch (Api.StatusException e) {
 			LOG.error("caught Api.StatusException in leaveAppoinment for " + owner + " and " + visitor + " and " + eventUid, e);
 			invalidateSession = true;
 			throw new OracleCalendarDataAccessException("caught Api.StatusException", e);
 		} catch (ParserException e) {
 			LOG.error("caught ParserException in leaveAppointment for " + owner + " and " + visitor + " and " + eventUid, e);
 			throw new OracleCalendarParserException("caught ParserException", e);
 		} finally {
 			doneWithSession(session, serverNode, invalidateSession);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param session
 	 * @param event
 	 * @throws StatusException 
 	 */
 	protected void replaceEventInternal(Session session, VEvent event) throws StatusException {
 		Calendar calendar = this.oracleEventUtils.wrapEventInCalendar(event);
 
 		if(LOG.isDebugEnabled()) {
 			LOG.debug("replaceEventInternal before Session#storeEvents for " + event);
 		}
 		RequestResult requestResults = new RequestResult();
 		session.storeEvents(getOracleReplaceFlags(), calendar.toString(), requestResults);
 		if(LOG.isDebugEnabled()) {
 			LOG.debug("replaceEventInternal Session#storeEvents capi result: " + requestResults.toString());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jasig.schedassist.ICalendarDataDao#checkForConflicts(org.jasig.schedassist.model.IScheduleOwner, org.jasig.schedassist.model.AvailableBlock)
 	 */
 	@Override
 	public final void checkForConflicts(IScheduleOwner owner, AvailableBlock block)
 			throws ConflictExistsException {
 		// note: when retrieving a list of events between times, oracle will return events that END at the same minute as the start time
 		// in this case, we don't want a preceding event to be considered, so add 1 minute (60,000 milliseconds) to start time.
 		Date startTime = new Date(block.getStartTime().getTime() + 60000);	
 		Calendar calendar = getCalendar(owner.getCalendarAccount(), startTime, block.getEndTime());
 		ComponentList events = calendar.getComponents(Component.VEVENT);	
 		if(events.size() > 0) {
 			if(LOG.isDebugEnabled()) {
 				LOG.debug("checkForConflicts found " + events.size() + " events for " + owner + " between " + startTime + " and " + block.getEndTime());
 			}
 			for(Object component : events) {
 				VEvent event = (VEvent) component;
 				if(this.oracleEventUtils.willEventCauseConflict(owner.getCalendarAccount(), event)) {
 					throw new ConflictExistsException("a conflict exists for " + block + " in the schedule for " + owner);
 				} 
 			}
 		} else {
 			LOG.debug("checkForConflicts found 0 events for " + owner + " between " + startTime + " and " + block.getEndTime());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jasig.schedassist.ICalendarDataDao#reflectAvailableSchedule(org.jasig.schedassist.model.IScheduleOwner, org.jasig.schedassist.model.AvailableSchedule)
 	 */
 	@Override
 	public final void reflectAvailableSchedule(IScheduleOwner owner,
 			AvailableSchedule schedule) {
 		if(!schedule.isEmpty()) {
 			LOG.info("beginning reflectAvailableSchedule for " + owner);
 
 			Session session = null;
 			boolean invalidate = false;
 			OracleCalendarServerNode serverNode = getOracleCalendarServerNode(owner.getCalendarAccount());
 			try {
 				session = getSession(owner.getCalendarAccount(), serverNode);
 
 				Date startDate = CommonDateOperations.beginningOfDay(schedule.getScheduleStartTime());
 				Date endDate = CommonDateOperations.endOfDay(schedule.getScheduleEndTime());
 
 				purgeAvailableScheduleReflections(owner, startDate, endDate);
 
 				List<Calendar> newReflections = this.oracleEventUtils.convertScheduleForReflection(schedule);
 				// oracleEventUtils overrides this method to only return 1 calendar
				if(!newReflections.isEmpty()) {
 					RequestResult storeResult = new RequestResult();
 					if(LOG.isDebugEnabled()) {
 						LOG.debug("reflectAvailableSchedule begin call to Session#storeEvents for " + owner);
 					}
					session.storeEvents(getOracleCreateReflectionFlags(), newReflections.toString(), storeResult);
 					if(LOG.isDebugEnabled()) {
 						LOG.debug("reflectAvailableSchedule Session#storeEvents for " + owner + " complete, capi result: " + storeResult);
 					}
 				} else {
					LOG.debug("store new reflections skipped as schedule is now empty");
 				}
 				
 			} catch (Api.StatusException e) {
 				LOG.error("caught Api.StatusException in reflectAvailableSchedule", e);
 				throw new OracleCalendarDataAccessException("reflectAvailableSchedule failed for owner " + owner, e);
 			} finally {
 				doneWithSession(session, serverNode, invalidate);
 			}
 			LOG.info("reflectAvailableSchedule complete for " + owner);
 		} else {
 			LOG.debug("ignoring reflectAvailableSchedule call on empty schedule for " + owner);
 		}
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.jasig.schedassist.ICalendarDataDao#purgeAvailableScheduleReflections(org.jasig.schedassist.model.IScheduleOwner, java.util.Date, java.util.Date)
 	 */
 	@Override
 	public void purgeAvailableScheduleReflections(IScheduleOwner owner,
 			Date startDate, Date endDate) {
 		if(startDate != null && endDate != null) {
 			Session session = null;
 			boolean invalidate = false;
 			OracleCalendarServerNode serverNode = getOracleCalendarServerNode(owner.getCalendarAccount());
 			try {
 				session = getSession(owner.getCalendarAccount(), serverNode);
 
 				Calendar existingReflections = getExistingAvailableScheduleReflections(owner, 
 						startDate, 
 						endDate, 
 						session);
 				ComponentList existingComponents = existingReflections.getComponents();
 				List<String> uidsToRemove = new ArrayList<String>();
 				for(Object o : existingComponents) {
 					Component component = (Component) o;
 					if(AvailabilityReflection.TRUE.equals(component.getProperty(AvailabilityReflection.AVAILABILITY_REFLECTION))) {
 						// add the UID to the list for removal
 						String uidValue = component.getProperty(Uid.UID).getValue();
 						uidsToRemove.add(uidValue);
 						LOG.debug("added " + uidValue + " to list of reflection events to be removed");
 					}
 				}
 
 				if(!uidsToRemove.isEmpty()) {
 					RequestResult deleteResult = new RequestResult();
 					session.deleteEvents(Api.CSDK_FLAG_CONTINUE_ON_ERROR, 
 							uidsToRemove.toArray(new String[] {}),
 							null,
 							Api.CSDK_THISINSTANCE,
 							deleteResult);
 					if(LOG.isDebugEnabled()) {
 						LOG.debug("delete existing reflections complete: " + deleteResult);
 					}
 				} else {
 					LOG.debug("no existing reflections to remove");
 				}
 			} catch (Api.StatusException e) {
 				LOG.error("caught Api.StatusException in reflectAvailableSchedule for owner " + owner, e);
 				throw new OracleCalendarDataAccessException("reflectAvailableSchedule failed for owner " + owner, e);
 			} catch (IOException e) {
 				LOG.error("caught IOException in reflectAvailableSchedule for " + owner, e);
 				throw new OracleCalendarParserException("reflectAvailableSchedule failed for owner " + owner, e);
 			} catch (ParserException e) {
 				LOG.error("failed to parse existing reflection events for " + owner, e);
 				throw new OracleCalendarParserException("reflectAvailableSchedule failed for owner " + owner, e);
 			} finally {
 				doneWithSession(session, serverNode, invalidate);
 			}
 		} else {
 			LOG.warn("skipping purgeAvailableScheduleReflections since date argument is null (start: " + startDate + ", end: " + endDate);
 		}
 	}
 	
 	/**
 	 * Helper method to locate the correct value of Oracle GUID for the specified account.
 	 * If the {@link ICalendarAccount} is an instance of {@link AbstractOracleCalendarAccount}, which
 	 * in all likelihood it is if you are using this DAO, it tries to call {@link AbstractOracleCalendarAccount#getOracleGuid()}.
 	 * If that return value is null, or the {@link ICalendarAccount} is some other subclass, this method
 	 * uses the {@link Session} argument in a call to {@link OracleGUIDSource#getOracleGUID(ICalendarAccount, Session)}.
 	 * 
 	 * @param calendarAccount
 	 * @param session
 	 * @return the oracle GUID, or null
 	 */
 	protected final String locateOracleGuid(ICalendarAccount calendarAccount, Session session) {
 		if(calendarAccount == null) {
 			return null;
 		}
 		String guid = calendarAccount.getAttributes().get(AbstractOracleCalendarAccount.ORACLE_GUID_ATTRIBUTE);
 		if(guid != null) {
 			return guid;
 		}
 		
 		LOG.debug("oracle GUID not found from attributes map, attempting cast");
 		// fail safe
 		if(calendarAccount instanceof AbstractOracleCalendarAccount) {
 			AbstractOracleCalendarAccount casted = (AbstractOracleCalendarAccount) calendarAccount;
 			String castedGuid = casted.getOracleGuid();
 			if(null != castedGuid) {
 				return castedGuid;
 			} 
 		} else {
 			LOG.warn("non oracle calendar account detected in OracleCalendarDao: " + calendarAccount);
 		}
 		
 		// fall back to OracleGUIDSource
 		try {
 			return this.oracleGUIDSource.getOracleGUID(calendarAccount, session);
 		} catch (StatusException e) {
 			LOG.warn("unable to locate oracle GUID for " + calendarAccount, e);
 			return null;
 		}
 	}
 	/**
 	 * Retrieve a {@link Calendar} containing only the existing Available Schedule Reflections (Oracle Daily Notes).
 	 * 
 	 * @param owner
 	 * @param startTime
 	 * @param endTime
 	 * @param session
 	 * @return
 	 * @throws StatusException
 	 * @throws IOException
 	 * @throws ParserException
 	 */
 	protected final Calendar getExistingAvailableScheduleReflections(IScheduleOwner owner, Date startTime, Date endTime, Session session) throws StatusException, IOException, ParserException {
 		Handle agendas[] = { new Handle() };
 		agendas[0] = session.getHandle(Api.CSDK_FLAG_NONE, owner.getCalendarAccount().getCalendarLoginId());
 
 		String properties[] = new String[0];
 		RequestResult requestResults = new RequestResult();
 		String agenda = session.fetchEventsByRange(
 				getOracleFetchFlagsForReflectionLookup(), 
 				agendas,
 				DefaultEventUtilsImpl.convertToICalendarFormat(startTime),
 				DefaultEventUtilsImpl.convertToICalendarFormat(endTime),
 				properties,
 				requestResults);
 		
 		StringReader reader = new StringReader(agenda);
 		CalendarBuilder builder = new CalendarBuilder();
 		Calendar parsedAgenda = builder.build(reader);
 		ComponentList allComponents = parsedAgenda.getComponents();
 		ComponentList onlyReflections = new ComponentList();
 		for(Object o : allComponents) {
 			Component component = (Component) o;
 			if(AvailabilityReflection.TRUE.equals(component.getProperty(AvailabilityReflection.AVAILABILITY_REFLECTION))) {
 				onlyReflections.add(o);
 			}
 		}
 		
 		Calendar result = new Calendar(onlyReflections);
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param owner
 	 * @param eventUids
 	 * @param session
 	 * @throws StatusException 
 	 */
 	protected final void removeAvailableScheduleReflections(IScheduleOwner owner, List<String> eventUids, Session session) throws StatusException {
 		if(!eventUids.isEmpty()) {
 			if(LOG.isDebugEnabled()) {
 				LOG.debug("removeAvailableScheduleReflections begin Session#deleteEvents for " + owner);
 			}
 			RequestResult deleteResult = new RequestResult();
 			session.deleteEvents(Api.CSDK_FLAG_CONTINUE_ON_ERROR, 
 				eventUids.toArray(new String[] {}),
 				null,
 				Api.CSDK_THISINSTANCE,
 				deleteResult);
 			if(LOG.isDebugEnabled()) {
 				LOG.debug("removeAvailableScheduleReflections Session#deleteEvents for " + owner + " complete, capi result: " + deleteResult);
 			}
 		} else {
 			LOG.debug("eventUids was empty");
 		}
 	}
 	
 
 	/**
 	 * 
 	 * @return
 	 */
 	protected int getOracleFetchFlagsForReflectionLookup() {
 		return Api.CSDK_FLAG_FETCH_COMBINED | 
 			Api.CSDK_FLAG_FETCH_EXCLUDE_HOLIDAYS | 
 			Api.CSDK_FLAG_FETCH_EXCLUDE_DAYEVENTS | 
 			Api.CSDK_FLAG_FETCH_EXCLUDE_APPOINTMENTS | 
 			Api.CSDK_FLAG_STREAM_NOT_MIME;
 	}
 	/**
 	 * Internal version of {@link #getExistingAppointment(ScheduleOwner, AvailableBlock)} that takes
 	 * (and uses) an existing {@link Session}.
 	 * 
 	 * @param owner
 	 * @param block
 	 * @param session
 	 * @return
 	 * @throws StatusException
 	 * @throws ParserException
 	 */
 	protected VEvent getAvailableAppointmentInternal(IScheduleOwner owner, Date startTime, Date endTime, Session session) throws StatusException, ParserException {
 		final DateTime ical4jstart = new DateTime(startTime);
 		final DateTime ical4jend = new DateTime(endTime);
 		String agenda = getCalendarInternal(owner.getCalendarAccount(), startTime, endTime, session);
 		Calendar calendar = parseAgenda(agenda);
 		ComponentList componentList = calendar.getComponents(VEvent.VEVENT);
 		for(Object o: componentList) {
 			VEvent event = (VEvent) o;
 			Date eventStart = event.getStartDate().getDate();
 			Date eventEnd = event.getEndDate(true).getDate();
 			Property availableApptProperty = event.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT);
 			if(!SchedulingAssistantAppointment.TRUE.equals(availableApptProperty)) {
 				// immediately skip over non-available appointments
 				continue;
 			}
 
 			// check for version first
 			Property versionProperty = event.getProperty(AvailableVersion.AVAILABLE_VERSION);
 			if(null == versionProperty) {
 				// this is a version 1.0 appointment
 				// in version 1.0, the only check was to verify that visitor was an attendee
 				return event;
 			} else if(AvailableVersion.AVAILABLE_VERSION_1_1.equals(versionProperty)) {
 				Property ownerAttendee = this.oracleEventUtils.getAttendeeForUserFromEvent(event, owner.getCalendarAccount());
 				if(ownerAttendee == null) {
 					continue;
 				}
 				Parameter ownerAttendeeRole = ownerAttendee.getParameter(AppointmentRole.APPOINTMENT_ROLE);
 
 				// event has to be (1) an available appointment
 				// with (2) owner as AppointmentRole#OWNER (or AppointmentRole#BOTH)
 				// (3) start and (4) end date have to match
 				if((AppointmentRole.BOTH.equals(ownerAttendeeRole) || AppointmentRole.OWNER.equals(ownerAttendeeRole)) &&
 						eventStart.equals(startTime) &&
 						eventEnd.equals(endTime)) {
 					if(LOG.isTraceEnabled()) {
 						LOG.trace("getAvailableAppointment found " + event);
 					}
 					return event;
 				}
 			} else if (AvailableVersion.AVAILABLE_VERSION_1_2.equals(versionProperty)) {
 				Parameter ownerAttendeeRole = null;
 				Property ownerAttendee = this.oracleEventUtils.getAttendeeForUserFromEvent(event, owner.getCalendarAccount());
 				if(ownerAttendee == null) {
 					// check for resource account
 					Property resourceAttendee = event.getProperty(OracleResourceAttendee.ORACLE_RESOURCE_ATTENDEE);
 					if(this.oracleEventUtils.attendeeMatchesPerson(resourceAttendee, owner.getCalendarAccount())) {
 						// resource accounts can only be OWNER role
 						ownerAttendeeRole = AppointmentRole.OWNER;
 					}
 				} else {
 					ownerAttendeeRole = ownerAttendee.getParameter(AppointmentRole.APPOINTMENT_ROLE);
 				}
 
 				if(null == ownerAttendeeRole) {
 					// owner role not on organizer or on attendee
 					continue;
 				}
 				// event has to be (1) an available appointment
 				// with (2) owner as AppointmentRole#OWNER (or AppointmentRole#BOTH)
 				// (3) start and (4) end date have to match
 				if((AppointmentRole.BOTH.equals(ownerAttendeeRole) || AppointmentRole.OWNER.equals(ownerAttendeeRole)) &&
 						eventStart.equals(ical4jstart) &&
 						eventEnd.equals(ical4jend)) {
 					if(LOG.isTraceEnabled()) {
 						LOG.trace("getAvailableAppointment found " + event);
 					}
 					
 					return event;
 				}
 			}
 		}
 		if(LOG.isTraceEnabled()) {
 			LOG.trace("getAvailableAppointment found no match for times: " + startTime + ", " + endTime + ", owner: " + owner);
 		}
 		return null;
 	}
 	
 	/**
 	 * Internal version of {@link #getCalendar(ICalendarAccount, Date, Date)} that takes
 	 * (and uses) an existing {@link Session}.
 	 * 
 	 * @param calendarUser
 	 * @param startDate
 	 * @param endDate
 	 * @param session
 	 * @throws StatusException 
 	 * @throws ParserException 
 	 */
 	protected String getCalendarInternal(ICalendarAccount calendarUser, Date startDate,
 			Date endDate, Session session) throws StatusException, ParserException {
 		Handle agendas[] = { new Handle() };
 		agendas[0] = session.getHandle(Api.CSDK_FLAG_NONE, calendarUser.getCalendarLoginId());
 
 		String properties[] = new String[0];
 		RequestResult requestResults = new RequestResult();
 
 		String agenda = session.fetchEventsByRange(
 				getOracleFetchFlags(), 
 				agendas,
 				DefaultEventUtilsImpl.convertToICalendarFormat(startDate),
 				DefaultEventUtilsImpl.convertToICalendarFormat(endDate),
 				properties,
 				requestResults);
 
 		if(LOG.isTraceEnabled()) {
 			LOG.trace("raw agenda from Session#fetchEventsByRange for " + calendarUser + ": " + agenda);
 		}
 		return agenda;
 	}
 	
 	/**
 	 * This function encapsulates the processing of the output from Oracle Calendar
 	 * by iCal4j.
 	 * 
 	 * This method currently inspects the end of the agenda argument for the presence
 	 * of the complete string "END:VCALENDAR" (a known bug with the Oracle Calendar APIs presents
 	 * itself in this fashion) and attempts to repair the agenda before sending to iCal4J.
 	 * 
 	 * {@link CalendarBuilder#build(java.io.Reader)} can throw an {@link IOException}; this method wraps the call
 	 * in try-catch and throws any caught {@link IOException}s wrapped in a {@link ParseException} instead.
 	 * 
 	 * @param agenda
 	 * @return
 	 * @throws ParserException
 	 */
 	protected Calendar parseAgenda(String agenda) throws ParserException {	
 		final String chomped = StringUtils.chomp(agenda);
 		if(!StringUtils.endsWith(chomped, "END:VCALENDAR")) {
 			// Oracle for an unknown reason sometimes does not properly end the iCalendar
 			LOG.warn("agenda does not end in END:VCALENDAR");
 			// find out how much is missing from the end
 			int indexOfLastNewline = StringUtils.lastIndexOf(chomped, CRLF);
 			if(indexOfLastNewline == -1) {
 				throw new ParserException("oracle calendar data is malformed ", -1);
 			}
 			String agendaWithoutLastLine = agenda.substring(0, indexOfLastNewline);
 			StringBuilder agendaBuilder = new StringBuilder();
 			agendaBuilder.append(agendaWithoutLastLine);
 			agendaBuilder.append(CRLF);
 			agendaBuilder.append("END:VCALENDAR");
 
 			agenda = agendaBuilder.toString();
 		}
 
 		StringReader reader = new StringReader(agenda);
 		CalendarBuilder builder = new CalendarBuilder();
 		try {
 			Calendar result = builder.build(reader);
 			if(LOG.isTraceEnabled()) {
 				LOG.trace(result.toString());
 			}	
 			return result;
 		} catch (IOException e) {
 			LOG.error("ical4j threw IOException attempting to build Calendar; rethrowing as ParserException", e);
 			throw new ParserException(e.getMessage(), -1, e);
 		}
 	}
 
 	/**
 	 * 
 	 * @param user
 	 * @param session
 	 * @return the email address Oracle has stored for the user
 	 */
 	protected String getOracleStoredEmail(final ICalendarAccount user, final Session session) throws Api.StatusException {
 		Handle handle = session.getHandle(Api.CSDK_FLAG_NONE, user.getCalendarLoginId());
 		String email = handle.getEmail();
 		LOG.debug("oracle stored email for " + user + ": " + email);
 		return email;
 	}
 
 	/**
 	 * @param user
 	 * @return the ID of oracle calendar server node from the user's calendarUniqueId (ctcalxitemid)
 	 */
 	protected String getOracleNodeId(final ICalendarAccount user) {
 		if(user instanceof AbstractOracleCalendarAccount) {
 			AbstractOracleCalendarAccount account = (AbstractOracleCalendarAccount) user;
 			return account.getCalendarNodeId();
 		} else {
 			throw new IllegalArgumentException("argument is not an oracle account: " + user);
 		}
 	}
 
 	/**
 	 * This value is used as the first argument to fetchEventsByRange when used to retrieve full agendas
 	 * or single events.
 	 * 
 	 * @return Api.CSDK_FLAG_STREAM_NOT_MIME | Api.CSDK_FLAG_FETCH_EXCLUDE_HOLIDAYS | Api.CSDK_FLAG_FETCH_EXCLUDE_DAYEVENTS | Api.CSDK_FLAG_FETCH_EXCLUDE_DAILYNOTES
 	 */
 	protected int getOracleFetchFlags() {
 		return Api.CSDK_FLAG_STREAM_NOT_MIME | Api.CSDK_FLAG_FETCH_EXCLUDE_HOLIDAYS | Api.CSDK_FLAG_FETCH_EXCLUDE_DAYEVENTS | Api.CSDK_FLAG_FETCH_EXCLUDE_DAILYNOTES;
 	} 
 
 	/**
 	 * This value is used as the first argument to the first call to storeEvents (initial event creation).
 	 * 
 	 * @return Api.CSDK_FLAG_STORE_CREATE | Api.CSDK_FLAG_STREAM_NOT_MIME
 	 */
 	protected int getOracleCreateFlags() {
 		return Api.CSDK_FLAG_STORE_CREATE | Api.CSDK_FLAG_STREAM_NOT_MIME;
 	}
 	
 	/**
 	 * This value is used as the first argument to the storeEvents call used to store available
 	 * schedule reflections.
 	 * 
 	 * @return
 	 */
 	protected int getOracleCreateReflectionFlags() {
 		return Api.CSDK_FLAG_STORE_CREATE | Api.CSDK_FLAG_STORE_INVITE_SELF | Api.CSDK_FLAG_STREAM_NOT_MIME;
 	}
 
 	/**
 	 *  This value is used as the first argument to the second call to storeEvents (update event so student accepts invite).
 	 *  
 	 * @return Api.CSDK_FLAG_STORE_MODIFY | Api.CSDK_FLAG_STREAM_NOT_MIME
 	 */
 	protected int getOracleModifyFlags() {
 		return Api.CSDK_FLAG_STORE_MODIFY | Api.CSDK_FLAG_STREAM_NOT_MIME;
 	}
 	
 	/**
 	 *  This value is used as the first argument to the second call to storeEvents (update event so student accepts invite).
 	 *  
 	 * @return Api.CSDK_FLAG_STORE_REMOVE | Api.CSDK_FLAG_STREAM_NOT_MIME
 	 */
 	protected int getOracleReplaceFlags() {
 		return Api.CSDK_FLAG_STORE_REPLACE | Api.CSDK_FLAG_STREAM_NOT_MIME;
 	}
 	
 	/**
 	 * Call {@link Session#disconnect(int)}, catching and (debug) logging
 	 * any {@link Api.StatusException} that could be thrown.
 	 * 
 	 * @param session
 	 */
 	protected final void disconnectSessionQuietly(final Session session) {
 		if(null != session) {
 			try {
 				session.disconnect(Api.CSDK_FLAG_NONE);
 			} catch (Api.StatusException e) {
 				LOG.debug("ignoring Api.StatusException on disconnect", e);
 			}
 		} else {
 			LOG.debug("ignoring disconnectQuietly call for null session");
 		}
 	}
 }
