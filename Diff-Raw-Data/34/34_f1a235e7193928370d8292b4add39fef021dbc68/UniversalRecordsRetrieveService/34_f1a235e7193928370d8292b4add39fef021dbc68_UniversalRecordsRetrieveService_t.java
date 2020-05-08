 /*
  */
 package com.springeetravel.manager;
 
 import com.springeetravel.dataaccess.UniversalRecordRetrieveDAO;
 import com.springeetravel.domain.UniversalRecord;
 import java.util.Date;
 import java.util.List;
 import javax.inject.Inject;
 import javax.inject.Named;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author peter
  */
 @Transactional(readOnly = true)
 @Named
 public class UniversalRecordsRetrieveService {
 
     @Inject
    @Named("universalRecordRetrieveDAOMock")
     private UniversalRecordRetrieveDAO dao;
 
     public UniversalRecord retrieveUniversalRecordByLocatorCode(String locatorCode) {
         return dao.findByLocatorCode(locatorCode);
     }
 
     public List<UniversalRecord> retrieveUniversalRecordByBookingPassengerName(String passengerName) {
         return dao.findByBookingPassengerName(passengerName);
     }
 
     public List<UniversalRecord> retrieveUniversalRecordByAirTicketedDate(Date fromDate, Date toDate) {
         return dao.findByAirTicketedDate(fromDate, toDate);
     }
 
     public List<UniversalRecord> retrieveUniversalRecordByAgent(String agentId) {
         return dao.findByAgent(agentId);
     }
 
     public List<UniversalRecord> retrieveUniversalRecordByAgency(String agencyId) {
         return dao.findByAgency(agencyId);
     }
 }
