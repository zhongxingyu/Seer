 /*
     Copyright (c) 2012-2015 Yoga Vidya Pranic Healing Foundation of Karnataka.
     All rights reserved. Patents pending.
 */
 package com.yvphk.web.controller;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.yvphk.model.form.Event;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.yvphk.common.ParticipantLevel;
 import com.yvphk.common.PaymentMode;
 import com.yvphk.common.Util;
 import com.yvphk.model.form.EventFee;
 import com.yvphk.model.form.EventPayment;
 import com.yvphk.model.form.EventRegistration;
 import com.yvphk.model.form.HistoryRecord;
 import com.yvphk.model.form.Login;
 import com.yvphk.model.form.Option;
 import com.yvphk.model.form.Participant;
 import com.yvphk.model.form.ParticipantCriteria;
 import com.yvphk.model.form.RegistrationCriteria;
 import com.yvphk.model.form.ParticipantSeat;
 import com.yvphk.model.form.ReferenceGroup;
 import com.yvphk.model.form.RegisteredParticipant;
 import com.yvphk.model.form.RegistrationPayments;
 import com.yvphk.model.form.validator.PaymentValidator;
 import com.yvphk.model.form.validator.RegistrationValidator;
 import com.yvphk.service.EventService;
 import com.yvphk.service.ParticipantService;
 
 @Controller
 public class ParticipantController extends CommonController
 {
     @Autowired
     private ParticipantService participantService;
 
     @Autowired
     private EventService eventService;
 
     @RequestMapping("/register")
     public String newParticipant (Map<String, Object> map)
     {
         RegisteredParticipant registeredParticipant = new RegisteredParticipant();
         Event defEvent = getDefaultEvent();
         if (defEvent != null) {
             registeredParticipant.setEventId(getDefaultEvent().getId());
         }
         registeredParticipant.setAction(RegisteredParticipant.ActionRegister);
         map.put("registeredParticipant", registeredParticipant);
         map.put("allParticipantLevels", ParticipantLevel.allParticipantLevels());
         map.put("allPaymentModes", PaymentMode.allPaymentModes());
         map.put("allFoundations", allFoundations());
         map.put("allEvents", getAllEventMap(eventService.allEvents()));
         map.put("allReferenceGroups", getAllReferenceGroups(participantService.listReferenceGroups()));
         return "register";
     }
 
     @RequestMapping("/search")
     public String search (Map<String, Object> map)
     {
         RegistrationCriteria criteria = new RegistrationCriteria();
         Event defEvent = getDefaultEvent();
         if (defEvent != null) {
             criteria.setEventId(getDefaultEvent().getId());
         }
         map.put("registrationCriteria", criteria);
         map.put("allParticipantLevels", ParticipantLevel.allParticipantLevels());
         map.put("allFoundations", allFoundations());
         map.put("allEvents", getAllEventMap(eventService.allEvents()));
         map.put("allStatuses", getRegistrationStatusMap());
         map.put("allReferenceGroups", getAllReferenceGroups(participantService.listReferenceGroups()));
         return "search";
     }
 
     @RequestMapping("/list")
     public String searchRegistration (Map<String, Object> map,
                                       RegistrationCriteria registrationCriteria)
     {
         map.put("registrationCriteria", registrationCriteria);
         if (registrationCriteria != null) {
             map.put("registrationList", participantService.listRegistrations(registrationCriteria));
             map.put("allParticipantLevels", ParticipantLevel.allParticipantLevels());
             map.put("allFoundations", allFoundations());
             map.put("allEvents", getAllEventMap(eventService.allEvents()));
             map.put("allStatuses", getRegistrationStatusMap());
             map.put("allReferenceGroups", getAllReferenceGroups(participantService.listReferenceGroups()));
         }
         return "search";
     }
 
     @RequestMapping(value = "/addRegistration", method = RequestMethod.POST)
     public String addRegistration (RegisteredParticipant registeredParticipant,
                                    BindingResult errors,
                                    Map<String, Object> map,
                                    HttpServletRequest request)
     {
         Login login = (Login) request.getSession().getAttribute(Login.ClassName);
         String action = registeredParticipant.getAction();
 
         registeredParticipant.getRegistration().setEvent(
                 eventService.getEvent(registeredParticipant.getEventId()));
 
         RegistrationValidator validator = new RegistrationValidator();
         validator.validate(registeredParticipant, errors);
         
         if (errors.hasErrors()) {
             map.put("allParticipantLevels", ParticipantLevel.allParticipantLevels());
             map.put("allPaymentModes", PaymentMode.allPaymentModes());
             map.put("allFoundations", allFoundations());
             map.put("allEvents", getAllEventMap(eventService.allEvents()));
             map.put("allReferenceGroups", getAllReferenceGroups(participantService.listReferenceGroups()));
 
             if (RegisteredParticipant.ActionRegister.equals(action)) {
                 map.put("registeredParticipant", registeredParticipant);
         	    return "register";
             }
             else if (RegisteredParticipant.ActionUpdate.equals(action)) {
                 EventRegistration registration =
                         participantService.getEventRegistration(
                                 registeredParticipant.getRegistration().getId());
                 registeredParticipant.getRegistration().setTotalAmountPaid(registration.getTotalAmountPaid());
                 registeredParticipant.getRegistration().setAmountDue(registration.getAmountDue());
                 map.put("registeredParticipant", registeredParticipant);
 
                 return "registerTab";
             }
         }
         
         if (RegisteredParticipant.ActionRegister.equals(action)) {
             registeredParticipant.getRegistration().setRefOrder(1);
             registeredParticipant.initialize(login.getEmail());
             ParticipantSeat seat =
                     eventService.nextSeat(
                             registeredParticipant.getRegistration().getEvent(),
                             registeredParticipant.getRegistration());
             registeredParticipant.setCurrentSeat(seat);
         }
 
         if (RegisteredParticipant.ActionUpdate.equals(action)) {
             registeredParticipant.initializeForUpdate(login.getEmail());
         }
 
         EventRegistration registration = participantService.registerParticipant(registeredParticipant, login);
 
         if (RegisteredParticipant.ActionUpdate.equals(action)) {
             return "redirect:/search.htm";
         }
 
         registeredParticipant = populateRegisteredParticipant(String.valueOf(registration.getId()));
         map.put("registeredParticipant", registeredParticipant);
         return "summary";
     }
 
     @RequestMapping("/updateRegistration")
     public String updateRegistration (HttpServletRequest request, Map<String, Object> map)
     {
         String strRegistrationId = request.getParameter("registrationId");
         RegisteredParticipant registeredParticipant = populateRegisteredParticipant(strRegistrationId);
         if (registeredParticipant != null) {
             map.put("registeredParticipant", registeredParticipant);
             map.put("allParticipantLevels", ParticipantLevel.allParticipantLevels());
             map.put("allPaymentModes", PaymentMode.allPaymentModes());
             map.put("allFoundations", allFoundations());
             map.put("allEvents", getAllEventMap(eventService.allEvents()));
             map.put("allEventFees", getAllEventFees(registeredParticipant.getEventId()));
             map.put("allReferenceGroups", getAllReferenceGroups(participantService.listReferenceGroups()));
             return "registerTab";
         }
         return "null";
     }
 
     @RequestMapping(value = "/getAllEventFees", produces = "application/json; charset=utf-8")
     public @ResponseBody List<Option> eventFees (HttpServletRequest request)
     {
         String strEventId = request.getParameter("eventId");
         if (!Util.nullOrEmptyOrBlank(strEventId)) {
             return getAllEventFees(Integer.parseInt(strEventId));
         }
         return null;
     }
 
     @RequestMapping(value = "/fetchEventFee", produces = "application/json; charset=utf-8")
     public @ResponseBody Option fetchEventFee (HttpServletRequest request)
     {
         String strEventFeeId = request.getParameter("eventFeeId");
         if (!Util.nullOrEmptyOrBlank(strEventFeeId)) {
             EventFee fee = eventService.getEventFee(Integer.parseInt(strEventFeeId));
             return new Option(fee.getId(), String.valueOf(fee.getAmount()));
         }
         return null;
     }
 
     private RegisteredParticipant populateRegisteredParticipant (String strRegistrationId)
     {
         if (!Util.nullOrEmptyOrBlank(strRegistrationId)) {
             Integer registrationId = Integer.parseInt(strRegistrationId);
             EventRegistration registration = participantService.getEventRegistration(registrationId);
             RegisteredParticipant registeredParticipant = new RegisteredParticipant();
             registeredParticipant.setRegistration(registration);
             registeredParticipant.setParticipant(registration.getParticipant());
             registeredParticipant.setAllHistoryRecords(registration.getHistoryRecords());
             registeredParticipant.setEventId(registration.getEvent().getId());
             registeredParticipant.setAction(RegisteredParticipant.ActionUpdate);
             registeredParticipant.setStatus(registration.getStatus());
             return registeredParticipant;
         }
 
         return null;
     }
 
     private List<com.yvphk.model.form.Option> getAllEventFees (Integer eventId)
     {
         List<EventFee> eventFeeList = eventService.getEventFees(eventId);
         ArrayList<com.yvphk.model.form.Option> options = new ArrayList<com.yvphk.model.form.Option>();
         SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
         for (EventFee eventFee : eventFeeList) {
             String cutOffDateStr = formatter.format(eventFee.getCutOffDate());
             StringBuffer buffer = new StringBuffer();
             buffer.append(eventFee.getName());
             buffer.append(" - ");
             buffer.append(cutOffDateStr);
             buffer.append(" - ");
             buffer.append(eventFee.getAmount());
             if (eventFee.isReview()) {
                 buffer.append(" - ");
                 buffer.append("Review");
             }
             Option option = new Option(eventFee.getId(), buffer.toString());
             options.add(option);
         }
         return options;
     }
 
     @RequestMapping(value = "/registerTab", method = RequestMethod.GET)
     public String register ()
     {
         return "registerTab";
     }
 
     @RequestMapping(value = "/referenceGroup", method = RequestMethod.GET)
     public String referenceGroup (Map<String, Object> map)
     {
         map.put("referenceGroup", new ReferenceGroup());
         map.put("referenceGroupList", participantService.listReferenceGroups());
         return "referenceGroup";
     }
 
     @RequestMapping(value = "/addReferenceGroup", method = RequestMethod.POST)
     public String addReferenceGroup (ReferenceGroup referenceGroup,
                                      Map<String, Object> map,
                                      HttpServletRequest request)
     {
         Login login = (Login) request.getSession().getAttribute(Login.ClassName);
         referenceGroup.initialize(login.getEmail());
 
         participantService.addReferenceGroup(referenceGroup);
         map.put("referenceGroup", new ReferenceGroup());
         map.put("referenceGroupList", participantService.listReferenceGroups());
         return "referenceGroup";
     }
 
     @RequestMapping("/showPayments")
     public String showPayments (HttpServletRequest request, Map<String, Object> map)
     {
         String strRegistrationId = request.getParameter("registration.id");
         String strPaymentId = request.getParameter("paymentId");
         RegistrationPayments registrationPayments =
                 populateRegistrationPayments(strRegistrationId, strPaymentId);
         map.put("registrationPayments", registrationPayments);
         map.put("allPaymentModes", PaymentMode.allPaymentModes());
         return "payments";
     }
 
     @RequestMapping("/processPayments")
     public ModelAndView processPayments (RegistrationPayments registrationPayments,BindingResult errors, HttpServletRequest request)
     {
     	ModelAndView mv = null;
         Login login = (Login) request.getSession().getAttribute(Login.ClassName);
         EventPayment payment = registrationPayments.getCurrentPayment();
         boolean isAdd = RegistrationPayments.Add.equals(registrationPayments.getAction());
         if (isAdd) {
             payment.initialize(login.getEmail());
         }
         else {
             payment.initializeForUpdate(login.getEmail());
         }
         PaymentValidator val = new PaymentValidator();
     	val.validate(registrationPayments, errors);
         if(errors.hasErrors()){
         	/*String strRegistrationId = request.getParameter("registration.id");
             String strPaymentId = request.getParameter("paymentId");*/
             /*RegistrationPayments registrationPayments =
                     populateRegistrationPayments(strRegistrationId, strPaymentId);*/
         	mv = new ModelAndView("payments");
         	mv.addObject("errors", errors);
 //        	mv.addObject("registrationPayments", registrationPayments);
         	mv.addObject("allPaymentModes", PaymentMode.allPaymentModes());
             return mv;
         }else{
         	participantService.processPayment(payment, registrationPayments.getRegistrationId(), isAdd);
         }
         mv = new ModelAndView("forward:/updateRegistration.htm");
         request.setAttribute("registrationId",registrationPayments.getRegistrationId());
         return mv;
     }
 
     private RegistrationPayments populateRegistrationPayments (String strRegistrationId, String strPaymentId)
     {
         if (Util.nullOrEmptyOrBlank(strRegistrationId)) {
             return null;
         }
 
         Integer registrationId = Integer.parseInt(strRegistrationId);
 
         EventRegistration registration = participantService.getEventRegistration(registrationId);
 
         RegistrationPayments registrationPayments = new RegistrationPayments();
         registrationPayments.setRegistration(registration);
         registrationPayments.setRegistrationId(registration.getId());
 
         List<EventPayment> payments = new ArrayList<EventPayment>();
         payments.addAll(registration.getPayments());
         registrationPayments.setPayments(payments);
 
         if (!Util.nullOrEmptyOrBlank(strPaymentId)) {
             Integer paymentId = Integer.parseInt(strPaymentId);
             for(EventPayment payment: payments){
                 // testing
                 if (payment.getId().equals(paymentId)) {
                     registrationPayments.setCurrentPayment(payment);
                     registrationPayments.setAction(RegistrationPayments.Update);
                 }
             }
             if (registrationPayments.getCurrentPayment() == null) {
                 registrationPayments.setCurrentPayment(new EventPayment());
                 registrationPayments.setAction(RegistrationPayments.Add);
             }
         }
         else {
             registrationPayments.setCurrentPayment(new EventPayment());
             registrationPayments.setAction(RegistrationPayments.Add);
         }
         return registrationPayments;
     }
 
     @RequestMapping("/cancelRegistration")
     public String cancelRegistration (Map<String, Object> map,
                                       RegisteredParticipant registeredParticipant,
                                       HttpServletRequest request)
     {
         Login login = (Login) request.getSession().getAttribute(Login.ClassName);
         if (registeredParticipant.getRegistration() != null) {
             EventRegistration registration =
                     participantService.getEventRegistration(
                             registeredParticipant.getRegistration().getId());
             registeredParticipant.initializeHistoryRecords(login.getEmail());
             participantService.cancelRegistration(registration,
                     registeredParticipant.getCurrentHistoryRecord());
         }
         return "redirect:/search.htm";
     }
 
     @RequestMapping("/onHoldRegistration")
     public String onHoldRegistration (Map<String, Object> map,
                                       RegisteredParticipant registeredParticipant,
                                       HttpServletRequest request)
     {
         Login login = (Login) request.getSession().getAttribute(Login.ClassName);
         if (registeredParticipant.getRegistration() != null) {
             EventRegistration registration =
                     participantService.getEventRegistration(
                             registeredParticipant.getRegistration().getId());
             registeredParticipant.initializeHistoryRecords(login.getEmail());
             participantService.onHoldRegistration(registration,
                     registeredParticipant.getCurrentHistoryRecord());
         }
         return "redirect:/search.htm";
     }
 
     @RequestMapping("/changeToRegistered")
     public String changeToRegistered (Map<String, Object> map,
                                       RegisteredParticipant registeredParticipant,
                                       HttpServletRequest request)
     {
         Login login = (Login) request.getSession().getAttribute(Login.ClassName);
         if (registeredParticipant.getRegistration() != null) {
             EventRegistration registration =
                     participantService.getEventRegistration(
                             registeredParticipant.getRegistration().getId());
             registeredParticipant.initializeHistoryRecords(login.getEmail());
             if (registration.getSeats().size() == 0) {
                 ParticipantSeat seat =
                         eventService.nextSeat(
                                 registration.getEvent(),
                                 registration);
                if (seat != null) {
                    seat.setRegistrationId(registration.getId());
                    participantService.addParticipantSeat(seat);
                }
             }
             participantService.changeToRegistered(registration,
                     registeredParticipant.getCurrentHistoryRecord());
         }
         return "redirect:/search.htm";
     }
 
     @RequestMapping("/showReplaceRegistration")
     public String showReplaceRegistration (Map<String, Object> map,
                                            HttpServletRequest request)
     {
 
         map.put("participantCriteria", new ParticipantCriteria());
         map.put("allParticipantLevels", ParticipantLevel.allParticipantLevels());
         map.put("allFoundations", allFoundations());
         map.put("page", "replaceRegistration");
 
         String strRegistrationId = request.getParameter("registration.id");
         RegisteredParticipant registeredParticipant = populateRegisteredParticipant(strRegistrationId);
         if (registeredParticipant != null) {
             map.put("registeredParticipant", registeredParticipant);
         }
         map.put("registrationId", strRegistrationId);
         return "replaceRegistration";
     }
 
     @RequestMapping("/replaceRegistration")
     public String replaceRegistration (Map<String, Object> map,
                                        HttpServletRequest request)
     {
         String strRegistrationId = request.getParameter("registrationId");
         String strParticipantId = request.getParameter("participantId");
         String comment = request.getParameter("comments");
         Login login = (Login) request.getSession().getAttribute(Login.ClassName);
 
         if (Util.nullOrEmptyOrBlank(strRegistrationId) || Util.nullOrEmptyOrBlank(strParticipantId) ) {
             return null;
         }
 
         Integer registrationId = Integer.parseInt(strRegistrationId);
         Integer participantId = Integer.parseInt(strParticipantId);
         EventRegistration registration = participantService.getEventRegistration(registrationId);
         Participant participantToReplace = participantService.getParticipant(participantId);
 
         HistoryRecord record = null;
         if (!Util.nullOrEmptyOrBlank(comment)) {
             record = new HistoryRecord();
             record.setComment(comment);
             record.initialize(login.getEmail());
         }
 
         participantService.replaceParticipant(registration, participantToReplace, record);
 
         map.put("registrationId", strRegistrationId);
         return "forward:/updateRegistration.htm";
     }
 
     @RequestMapping("/listParticipants")
     public String listParticipants (Map<String, Object> map,
                                     ParticipantCriteria participantCriteria,
                                     HttpServletRequest request)
     {
         map.put("participantCriteria", new ParticipantCriteria());
         if (participantCriteria != null) {
             //todo if the page is replace, we should search participant that are not participating in the current event.
             map.put("participantList", participantService.listParticipants(participantCriteria));
             map.put("allParticipantLevels", ParticipantLevel.allParticipantLevels());
             map.put("allFoundations", allFoundations());
         }
 
         String page = request.getParameter("page");
         map.put("page", page);
 
 
         String strRegistrationId = request.getParameter("registrationId");
         RegisteredParticipant registeredParticipant = populateRegisteredParticipant(strRegistrationId);
         if (registeredParticipant != null) {
             map.put("registeredParticipant", registeredParticipant);
         }
         return page;
     }
 
     @RequestMapping("/searchParticipants")
     public String searchParticipants (Map<String, Object> map)
     {
         map.put("participantCriteria", new ParticipantCriteria());
         map.put("allParticipantLevels", ParticipantLevel.allParticipantLevels());
         map.put("allFoundations", allFoundations());
         map.put("page", "searchParticipants");
         return "searchParticipants";
     }
 
     @RequestMapping("/showAddParticipant")
     public String showAddParticipant (Map<String, Object> map,
                                       HttpServletRequest request)
     {
         map.put("newParticipant", new Participant());
         map.put("allFoundations", allFoundations());
         return "addParticipant";
     }
 
     @RequestMapping("/createParticipant")
     public String createParticipant (Map<String, Object> map,
                                      Participant participant,
                                      HttpServletRequest request)
     {
         Login login = (Login) request.getSession().getAttribute(Login.ClassName);
         participant.initialize(login.getEmail());
         participantService.addParticipant(participant);
         return "redirect:/searchParticipants.htm";
     }
 
 }
