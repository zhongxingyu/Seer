 package org.motechproject.ghana.national.handler;
 
 import org.joda.time.LocalDate;
 import org.junit.Test;
 import org.mockito.Matchers;
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.ghana.national.domain.Patient;
 import org.motechproject.ghana.national.domain.SMS;
 import org.motechproject.ghana.national.repository.AllFacilities;
 import org.motechproject.ghana.national.repository.AllPatients;
 import org.motechproject.ghana.national.service.TextMessageService;
 import org.motechproject.mrs.model.MRSFacility;
 import org.motechproject.mrs.model.MRSPatient;
 import org.motechproject.mrs.model.MRSPerson;
import org.motechproject.scheduletracking.api.domain.MilestoneAlert;
 import org.motechproject.scheduletracking.api.domain.WindowName;
 import org.motechproject.scheduletracking.api.events.MilestoneEvent;
 import org.motechproject.util.DateUtil;
 
 import java.util.Map;
 
 import static org.mockito.Mockito.*;
 import static org.motechproject.ghana.national.handler.CareScheduleHandler.PREGNANCY_ALERT_SMS_KEY;
 
 public class CareScheduleHandlerTest {
     AllPatients allPatients = mock(AllPatients.class);
     TextMessageService textMessageService;
     AllFacilities allFacilities;
 
     @Test
     public void handlePregnancyAlert() {
         textMessageService = mock(TextMessageService.class);
         allFacilities = mock(AllFacilities.class);
         CareScheduleHandler careScheduleHandler = new CareScheduleHandler(allPatients, textMessageService, allFacilities);
         String patientId = "123";
         String facilityId = "234";
 
         MRSPerson person = new MRSPerson();
         person.firstName("firstName");
         person.lastName("lastName");
         when(allPatients.patientByOpenmrsId(patientId)).thenReturn(new Patient(new MRSPatient("motechid", person, new MRSFacility(facilityId))));
         Facility facility = new Facility().phoneNumber("phonenumber");
         when(allFacilities.getFacility(facilityId)).thenReturn(facility);
 
         final LocalDate edd = DateUtil.today();
         LocalDate conceivedDate = edd.minusWeeks(40);
 
         when(textMessageService.getSMS(eq(PREGNANCY_ALERT_SMS_KEY), Matchers.<Map<String,String>>any())).thenReturn(SMS.fromSMSText("Upcoming Pregnancy motechid, firstName lastName " + edd.toString()));
 
        MilestoneAlert milestoneAlert = mock(MilestoneAlert.class);
        careScheduleHandler.handlePregnancyAlert(new MilestoneEvent(patientId, "Pregnancy", milestoneAlert, WindowName.due.name(), conceivedDate));
 
         verify(textMessageService).sendSMS(facility, SMS.fromSMSText("Upcoming Pregnancy motechid, firstName lastName " + edd.toString()));
     }
 }
