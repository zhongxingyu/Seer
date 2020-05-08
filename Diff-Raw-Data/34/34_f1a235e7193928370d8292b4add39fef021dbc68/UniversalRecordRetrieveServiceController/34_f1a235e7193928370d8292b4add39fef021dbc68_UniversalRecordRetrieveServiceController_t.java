 /*
  */
 package com.springeetravel.controller;
 
 import com.springeetravel.domain.UniversalRecord;
import com.springeetravel.manager.UniversalRecordsRetrieveService;
 import java.util.Date;
 import java.util.List;
 import javax.inject.Inject;
 import org.springframework.format.annotation.DateTimeFormat;
 import org.springframework.format.annotation.DateTimeFormat.ISO;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  * @author peter
  */
 @Transactional(readOnly = true)
 @Controller
 @RequestMapping(value = "/UniversalRecord")
 public class UniversalRecordRetrieveServiceController {
 
     public static final String UNIVERSAL_RECORD_XML_VIEW = "universalRecordXmlView";
     @Inject
    private UniversalRecordsRetrieveService service;
 
     @RequestMapping(value = "/locatorCode/{locatorCode}", method = RequestMethod.GET)
     public ModelAndView getUniversalRecordByLocatorCode(@PathVariable String locatorCode) {
        UniversalRecord ur = service.retrieveUniversalRecordByLocatorCode(locatorCode);
         return new ModelAndView(UNIVERSAL_RECORD_XML_VIEW, BindingResult.MODEL_KEY_PREFIX + "UniversalRecord", ur);
     }
 
     @RequestMapping(value = "/passengerName/{passengerName}", method = RequestMethod.GET)
     public ModelAndView getUniversalRecordByBookingPassengerName(String passengerName) {
        List<UniversalRecord> urs = service.retrieveUniversalRecordByBookingPassengerName(passengerName);
         return new ModelAndView(UNIVERSAL_RECORD_XML_VIEW, BindingResult.MODEL_KEY_PREFIX + "UniversalRecord", urs);
     }
 
     @RequestMapping(value = "/ticketedDateFrom/{fromDate}/ticketedDateTo/{fromDate}", method = RequestMethod.GET)
     public ModelAndView getUniversalRecordByAirTicketedDate(@DateTimeFormat(iso = ISO.DATE) Date fromDate, @DateTimeFormat(iso = ISO.DATE) Date toDate) {
        List<UniversalRecord> urs = service.retrieveUniversalRecordByAirTicketedDate(fromDate, toDate);
         return new ModelAndView(UNIVERSAL_RECORD_XML_VIEW, BindingResult.MODEL_KEY_PREFIX + "UniversalRecord", urs);
     }
 
     @RequestMapping(value = "/agentId/{agentId}", method = RequestMethod.GET)
     public ModelAndView getUniversalRecordByAgent(String agentId) {
        List<UniversalRecord> urs = service.retrieveUniversalRecordByAgent(agentId);
         return new ModelAndView(UNIVERSAL_RECORD_XML_VIEW, BindingResult.MODEL_KEY_PREFIX + "UniversalRecord", urs);
     }
 
     @RequestMapping(value = "/agencyId/{agencyId}", method = RequestMethod.GET)
     public ModelAndView getUniversalRecordByAgency(String agencyId) {
        List<UniversalRecord> urs = service.retrieveUniversalRecordByAgency(agencyId);
         return new ModelAndView(UNIVERSAL_RECORD_XML_VIEW, BindingResult.MODEL_KEY_PREFIX + "UniversalRecord", urs);
     }
 }
