 /**
  * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
  * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
  * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
  * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAÇÃO, SA (PTIN), IBM Corp., 
  * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
  * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
  * conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *    disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.societies.rdPartyService.enterprise.sharedCalendar.commsServer;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.societies.api.comm.xmpp.datatypes.Stanza;
 import org.societies.api.comm.xmpp.exceptions.CommunicationException;
 import org.societies.api.comm.xmpp.exceptions.XMPPError;
 import org.societies.api.comm.xmpp.interfaces.ICommManager;
 import org.societies.api.comm.xmpp.interfaces.IFeatureServer;
 import org.societies.rdPartyService.enterprise.sharedCalendar.SharedCalendar;
 import org.societies.rdpartyservice.enterprise.sharedcalendar.Calendar;
 import org.societies.rdpartyservice.enterprise.sharedcalendar.Event;
 import org.societies.rdpartyservice.enterprise.sharedcalendar.SharedCalendarResult;
 
 /**
  * This is the Shared Calendar Communication Manager that marshalls / unmarshalls XMPP messages
  * and routes them to/from the correct Shared Calendar Server functionality.
  *
  * @author solutanet
  *
  */
 public class SharedCalendarCommServer implements IFeatureServer{
 	private ICommManager commManager;
 	private SharedCalendar sharedCalendarService;
 	
 	private static final List<String> NAMESPACES = Collections.unmodifiableList(
 			Arrays.asList("http://societies.org/rdPartyService/enterprise/sharedCalendar"));
 	private static final List<String> PACKAGES = Collections.unmodifiableList(
 			Arrays.asList("org.societies.rdpartyservice.enterprise.sharedcalendar"));
 	
 	//PROPERTIES
 	public ICommManager getCommManager() {
 		return commManager;
 	}
 
 	public void setCommManager(ICommManager commManager) {
 		this.commManager = commManager;
 	}
 	
 	public SharedCalendar getSharedCalendarService() {
 		return sharedCalendarService;
 	}
 
 	public void setSharedCalendarService(SharedCalendar sharedCalendarService) {
 		this.sharedCalendarService = sharedCalendarService;
 	}
 
 	public void initService() {
 		// REGISTER OUR ServiceManager WITH THE XMPP Communication Manager
 		ICommManager cm = getCommManager();
 		try {
 			cm.register(this);
 		} catch (CommunicationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.societies.api.comm.xmpp.interfaces.IFeatureServer#getXMLNamespaces()
 	 */
 	@Override
 	public List<String> getXMLNamespaces() {
 		// TODO Auto-generated method stub
 		return SharedCalendarCommServer.NAMESPACES;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.societies.api.comm.xmpp.interfaces.IFeatureServer#getJavaPackages()
 	 */
 	@Override
 	public List<String> getJavaPackages() {
 		// TODO Auto-generated method stub
 		return SharedCalendarCommServer.PACKAGES;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.societies.api.comm.xmpp.interfaces.IFeatureServer#receiveMessage(org.societies.api.comm.xmpp.datatypes.Stanza, java.lang.Object)
 	 */
 	@Override
 	public void receiveMessage(Stanza stanza, Object payload) {
 		// TODO Auto-generated method stub
 		System.out.println(stanza);
 		if (payload instanceof Calendar){
 			System.out.println((Calendar)payload);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.societies.api.comm.xmpp.interfaces.IFeatureServer#getQuery(org.societies.api.comm.xmpp.datatypes.Stanza, java.lang.Object)
 	 */
 	@Override
 	public Object getQuery(Stanza stanza, Object payload) throws XMPPError {
 		org.societies.rdpartyservice.enterprise.sharedcalendar.SharedCalendarBean bean = null;
 		SharedCalendarResult resultBean = new SharedCalendarResult();
 		if (payload instanceof org.societies.rdpartyservice.enterprise.sharedcalendar.SharedCalendarBean){
 			bean = (org.societies.rdpartyservice.enterprise.sharedcalendar.SharedCalendarBean) payload;
 			switch (bean.getMethod()) {
 			case DELETE_CIS_CALENDAR:
 				resultBean.setLastOperationSuccessful(this.sharedCalendarService.deleteCISCalendar(bean.getCISId()));
 				break;
 			case RETRIEVE_CIS_CALENDAR_LIST:
 				List<Calendar> retrievedCalendars = this.sharedCalendarService.retrieveCISCalendarList(bean.getCISId());
 				resultBean.setCalendarList(retrievedCalendars);
 				break;
			case RETRIEVE_CIS_CALENDAR_EVENTS:
 				List<Event> retrievedEvents = this.sharedCalendarService.retrieveCalendarEvents(bean.getCalendarId());
 				resultBean.setEventList(retrievedEvents);
 				break;
 			case SUBSCRIBE_TO_EVENT:
 				resultBean.setSubscribingResult(this.sharedCalendarService.subscribeToEvent(bean.getCalendarId(),bean.getEventId(), bean.getSubscriberId()));
 				break;
 			case FIND_EVENTS:
 				List<Event> foundEvents = this.sharedCalendarService.findEvents(bean.getCalendarId(), bean.getKeyWord());
 				resultBean.setEventList(foundEvents);
 				break;
 			case UNSUBSCRIBE_FROM_EVENT:
 				resultBean.setSubscribingResult(this.sharedCalendarService.unsubscribeFromEvent(bean.getCalendarId(),bean.getEventId(), bean.getSubscriberId()));
 				break;
 			case CREATE_PRIVATE_CALENDAR:
 				resultBean.setLastOperationSuccessful(
 						this.sharedCalendarService.createPrivateCalendarUsingCSSId(stanza.getFrom().getJid(), bean.getCalendarSummary())
 				);
 				break;
 			case DELETE_PRIVATE_CALENDAR:
 				resultBean.setLastOperationSuccessful(this.sharedCalendarService.deletePrivateCalendar());
 				break;
 			case CREATE_EVENT_ON_PRIVATE_CALENDAR:
 				String calendarId = this.sharedCalendarService.retrievePrivateCalendarId(stanza.getFrom().getJid());
 				if (calendarId != null) {
 					String returnedEventId = this.sharedCalendarService.createEventOnPrivateCalendarUsingCSSId(calendarId, bean.getNewEvent());
 					resultBean.setEventId(returnedEventId);
 				}
 				break;
			case RETRIEVE_EVENTS_ON_PRIVATE_CALENDAR:
 				String calendarIdForAllEvents = this.sharedCalendarService.retrievePrivateCalendarId(stanza.getFrom().getJid());
 				if (calendarIdForAllEvents != null) {
 					List<Event> returnedPrivateCalendarEventList = this.sharedCalendarService.retrieveCalendarEvents(calendarIdForAllEvents);
 					resultBean.setEventList(returnedPrivateCalendarEventList);
 				}
 				break;
 			default:
 				resultBean = null;
 				break;
 			}
 		}
 		return resultBean;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.societies.api.comm.xmpp.interfaces.IFeatureServer#setQuery(org.societies.api.comm.xmpp.datatypes.Stanza, java.lang.Object)
 	 */
 	@Override
 	public Object setQuery(Stanza stanza, Object payload) throws XMPPError {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public SharedCalendarCommServer() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 	
 	
 
 }
