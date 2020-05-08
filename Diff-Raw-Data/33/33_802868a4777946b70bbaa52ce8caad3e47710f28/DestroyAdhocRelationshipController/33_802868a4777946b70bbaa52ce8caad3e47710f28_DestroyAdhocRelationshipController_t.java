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
 
 
 package org.jasig.schedassist.web.owner.relationships;
 
 import org.jasig.schedassist.CalendarAccountNotFoundException;
 import org.jasig.schedassist.ICalendarAccountDao;
 import org.jasig.schedassist.MutableRelationshipDao;
 import org.jasig.schedassist.impl.owner.NotRegisteredException;
 import org.jasig.schedassist.impl.visitor.NotAVisitorException;
 import org.jasig.schedassist.impl.visitor.VisitorDao;
 import org.jasig.schedassist.model.ICalendarAccount;
 import org.jasig.schedassist.model.IScheduleOwner;
 import org.jasig.schedassist.model.IScheduleVisitor;
 import org.jasig.schedassist.web.security.CalendarAccountUserDetails;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  * Form controller that invokes {@link MutableRelationshipDao#destroyRelationship(IScheduleOwner, IScheduleVisitor)}.
  *  
  * @author Nicholas Blair, nblair@doit.wisc.edu
  * @version $Id: DestroyAdhocRelationshipController.java 2049 2010-04-30 16:01:10Z npblair $
  */
 @Controller
 @RequestMapping(value={"/owner/destroy-adhoc-relationship.html", "/delegate/destroy-adhoc-relationship.html"})
 public class DestroyAdhocRelationshipController {
 
 	private ICalendarAccountDao calendarAccountDao;
 	private VisitorDao visitorDao;
 	private MutableRelationshipDao mutableRelationshipDao;
 	private String identifyingAttributeName = "uid";
 	/**
 	 * 
 	 * @param identifyingAttributeName
 	 */
 	@Value("${users.visibleIdentifierAttributeName:uid}")
 	public void setIdentifyingAttributeName(String identifyingAttributeName) {
 		this.identifyingAttributeName = identifyingAttributeName;
 	}
 	/**
 	 * 
 	 * @return the attribute used to commonly uniquely identify an account
 	 */
 	public String getIdentifyingAttributeName() {
 		return identifyingAttributeName;
 	}
 	/**
 	 * @param calendarAccountDao the calendarAccountDao to set
 	 */
 	@Autowired
 	public void setCalendarAccountDao(ICalendarAccountDao calendarAccountDao) {
 		this.calendarAccountDao = calendarAccountDao;
 	}
 	/**
 	 * @param visitorDao the visitorDao to set
 	 */
 	@Autowired
 	public void setVisitorDao(VisitorDao visitorDao) {
 		this.visitorDao = visitorDao;
 	}
 	/**
 	 * @param mutableRelationshipDao the mutableRelationshipDao to set
 	 */
 	@Autowired
 	public void setMutableRelationshipDao(
 			@Qualifier("adhoc") MutableRelationshipDao mutableRelationshipDao) {
 		this.mutableRelationshipDao = mutableRelationshipDao;
 	}
 	/**
 	 * 
 	 * @param binder
 	 */
 	/*
 	@InitBinder("command")
     protected void initBinder(WebDataBinder binder) {
         binder.setValidator(new ModifyAdhocRelationshipFormBackingObjectValidator(this.calendarAccountDao));
     }
     */
 	/**
 	 * 
 	 * @return
 	 */
 	@RequestMapping(method=RequestMethod.GET)
 	protected String setupForm(final ModelMap model) {
 		model.addAttribute("command", new ModifyAdhocRelationshipFormBackingObject());
 		return "owner-relationships/destroy-adhoc-relationship-form";
 	}
 	/**
 	 * 
 	 * @param fbo
 	 * @return
 	 * @throws CalendarAccountNotFoundException
 	 * @throws NotAVisitorException
 	 * @throws NotRegisteredException 
 	 */
 	@RequestMapping(method=RequestMethod.POST)
 	protected String destroyAdhocRelationship(@ModelAttribute("command") ModifyAdhocRelationshipFormBackingObject fbo, final ModelMap model) throws CalendarAccountNotFoundException, NotAVisitorException, NotRegisteredException {
 		CalendarAccountUserDetails currentUser = (CalendarAccountUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		IScheduleOwner owner = currentUser.getScheduleOwner();
 		
 		ICalendarAccount visitorUser = calendarAccountDao.getCalendarAccount(identifyingAttributeName, fbo.getVisitorUsername());
 		if(null == visitorUser) {
			throw new CalendarAccountNotFoundException(fbo.getVisitorUsername() + " does not exist or is not eligible for Calendar Service");
 		}
 		IScheduleVisitor visitor = visitorDao.toVisitor(visitorUser);
 		mutableRelationshipDao.destroyRelationship(owner, visitor);
 		
 		model.addAttribute("visitor", visitor);
 		model.addAttribute("relationship", fbo.getRelationship());
 		return "owner-relationships/destroy-adhoc-relationship-success";
 	}
 }
