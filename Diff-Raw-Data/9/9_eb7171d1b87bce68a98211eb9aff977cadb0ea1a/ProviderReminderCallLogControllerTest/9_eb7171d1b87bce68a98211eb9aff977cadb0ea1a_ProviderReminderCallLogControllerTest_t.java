 package org.motechproject.whp.reports.webservice.controller;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.motechproject.whp.reports.contract.ProviderReminderCallLogRequest;
import org.motechproject.whp.reports.contract.enums.AnswerStatus;
 import org.motechproject.whp.reports.contract.enums.ReminderDisconnectionType;
 import org.motechproject.whp.reports.contract.enums.ReminderType;
 import org.motechproject.whp.reports.domain.measure.ProviderReminderCallLog;
 import org.motechproject.whp.reports.mapper.ProviderReminderCallLogMapper;
 import org.motechproject.whp.reports.service.ProviderReminderCallLogService;
 import org.springframework.http.MediaType;
 
 import static org.junit.internal.matchers.StringContains.containsString;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
 import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
 import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;
 import static org.springframework.test.web.server.setup.MockMvcBuilders.standaloneSetup;
 
 public class ProviderReminderCallLogControllerTest extends ControllerTest{
 
     ProviderReminderCallLogController controller;
     @Mock
     ProviderReminderCallLogMapper providerReminderCallLogMapper;
     @Mock
     ProviderReminderCallLogService providerReminderCallLogService;
 
     @Before
     public void setUp()  {
         initMocks(this);
         controller = new ProviderReminderCallLogController(providerReminderCallLogService, providerReminderCallLogMapper);
     }
 
     @Test
     public void shouldLogProviderReminderCallLog() throws Exception {
         ProviderReminderCallLogRequest request = new ProviderReminderCallLogRequest();
         request.setCallId("callId");
         request.setDisconnectionType("disconnectionType");
         request.setStartTime("10/12/2012 12:32:35");
         request.setEndTime("10/12/2012 12:33:35");
         request.setMsisdn("mobileNumber");
         request.setProviderId("providerId");
         request.setAttempt("1");
         request.setAttemptTime("10/12/2012 12:34:35");
         request.setDisconnectionType(ReminderDisconnectionType.DID_NOT_ANSWER.name());
         request.setReminderType(ReminderType.ADHERENCE_NOT_REPORTED.name());
         request.setRequestId("requestId");
        request.setCallAnswered(AnswerStatus.YES.name());
 
         ProviderReminderCallLog expectedCallLog = new ProviderReminderCallLog();
         when(providerReminderCallLogMapper.map(request)).thenReturn(expectedCallLog);
 
         standaloneSetup(controller)
                 .build()
                 .perform(post("/providerReminderCallLog/measure")
                         .body(getJSON(request).getBytes())
                         .contentType(MediaType.APPLICATION_JSON)
                 )
                 .andExpect(status().isOk());
 
         verify(providerReminderCallLogService).save(expectedCallLog);
     }
 
     @Test
     public void shouldValidateContainerRegistrationCallLogRequest() throws Exception {
         ProviderReminderCallLogRequest request = new ProviderReminderCallLogRequest();
 
         standaloneSetup(controller)
                 .build()
                 .perform(post("/providerReminderCallLog/measure")
                         .body(getJSON(request).getBytes())
                         .contentType(MediaType.APPLICATION_JSON)
                 )
                 .andExpect(status().isBadRequest())
                 .andExpect(content().string(containsString("requestId")))
                 .andExpect(content().string(containsString("callId")))
                 .andExpect(content().string(containsString("reminderType")))
                 .andExpect(content().string(containsString("msisdn")))
                 .andExpect(content().string(containsString("providerId")))
                .andExpect(content().string(containsString("callAnswered")))
                 .andExpect(content().string(containsString("disconnectionType")))
                 .andExpect(content().string(containsString("attempt")))
                 .andExpect(content().string(containsString("attemptTime")));
     }
 }
