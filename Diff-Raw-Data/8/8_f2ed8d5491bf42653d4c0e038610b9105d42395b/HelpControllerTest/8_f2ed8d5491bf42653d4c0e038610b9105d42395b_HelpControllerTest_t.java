 package org.motechproject.ananya.kilkari.web.controller;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.motechproject.ananya.kilkari.obd.domain.Channel;
 import org.motechproject.ananya.kilkari.request.HelpWebRequest;
 import org.motechproject.ananya.kilkari.service.KilkariSubscriberCareService;
 import org.motechproject.ananya.kilkari.subscription.domain.SubscriberCareDoc;
 import org.motechproject.ananya.kilkari.subscription.domain.SubscriberCareReasons;
 import org.motechproject.ananya.kilkari.subscription.service.request.SubscriberCareRequest;
 import org.motechproject.ananya.kilkari.web.HttpHeaders;
 import org.motechproject.ananya.kilkari.web.MVCTestUtils;
 import org.springframework.http.MediaType;
 import org.springframework.test.web.server.MvcResult;
 
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.motechproject.ananya.kilkari.web.controller.ResponseMatchers.baseResponseMatcher;
 import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
 import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;
 
 public class HelpControllerTest {
 
     private HelpController helpController;
 
     @Mock
     private KilkariSubscriberCareService kilkariSubscriberCareService;
 
     @Before
     public void setUp() throws Exception {
         MockitoAnnotations.initMocks(this);
         helpController = new HelpController(kilkariSubscriberCareService);
     }
 
     @Test
     public void shouldProcessCareRequest() throws Exception {
         String channel = "ivr";
         String msisdn = "1234567890";
         String reason = "help";
 
         MVCTestUtils.mockMvc(helpController)
                 .perform(get("/help").param("msisdn", msisdn).param("reason", reason).param("channel", channel))
                 .andExpect(status().isOk())
                 .andExpect(content().type(HttpHeaders.APPLICATION_JAVASCRIPT))
                 .andExpect(content().string(baseResponseMatcher("SUCCESS", "Subscriber care request processed successfully")));
 
 
         ArgumentCaptor<SubscriberCareRequest> captor = ArgumentCaptor.forClass(SubscriberCareRequest.class);
         verify(kilkariSubscriberCareService).createSubscriberCareRequest(captor.capture());
         SubscriberCareRequest actualRequest = captor.getValue();
         assertEquals(channel, actualRequest.getChannel());
         assertEquals(msisdn, actualRequest.getMsisdn());
         assertEquals(reason, actualRequest.getReason());
     }
 
     @Test
     public void shouldReturnMsisdnsInCSVFormatWhichHaveAcessedHelp() throws Exception {
         String fromDate = "12-12-2012 00:00:00";
         String toDate = "15-12-2012 00:00:00";
         final DateTime now = DateTime.now();
         when(kilkariSubscriberCareService.fetchSubscriberCareDocs(new HelpWebRequest(fromDate, toDate, Channel.CONTACT_CENTER.name()))).thenReturn(new ArrayList<SubscriberCareDoc>(){{
             add(new SubscriberCareDoc("msisdn", SubscriberCareReasons.HELP, now, Channel.IVR));
         }});
 
         MvcResult mvcResult = MVCTestUtils.mockMvc(helpController)
                .perform(get("/help/list").param("startTime", fromDate).param("endTime", toDate).param("channel", Channel.CONTACT_CENTER.name()).
                         accept(new MediaType("text", "csv", Charset.defaultCharset())))
                 .andExpect(status().isOk()).andReturn();
 
         verify(kilkariSubscriberCareService).fetchSubscriberCareDocs(new HelpWebRequest(fromDate, toDate, Channel.CONTACT_CENTER.name()));
         String contentAsString = mvcResult.getResponse().getContentAsString();
         assertTrue(contentAsString.contains("msisdn,reason,date,time"));
         assertTrue(contentAsString.contains(String.format("msisdn,%s,%s,%s", SubscriberCareReasons.HELP.name(), now.toString("dd-MM-yyyy"), now.toString("HH:mm:ss"))));
     }
 
     @Test
     public void shouldThrowAnExceptionIfDateTimeFormatIsInvalid() throws Exception {
         String fromDate = "blah";
         String toDate = "everon";
         final DateTime now = DateTime.now();
         when(kilkariSubscriberCareService.fetchSubscriberCareDocs(new HelpWebRequest(fromDate, toDate, Channel.CONTACT_CENTER.name()))).thenReturn(new ArrayList<SubscriberCareDoc>(){{
             add(new SubscriberCareDoc("msisdn", SubscriberCareReasons.HELP, now, Channel.IVR));
         }});
 
         MvcResult mvcResult = MVCTestUtils.mockMvc(helpController)
                .perform(get("/help/list").param("startTime", fromDate).param("endTime", toDate).param("channel", Channel.CONTACT_CENTER.name()).
                         accept(new MediaType("text", "csv", Charset.defaultCharset())))
                 .andExpect(status().isBadRequest()).andReturn();
 
         String contentAsString = mvcResult.getResponse().getContentAsString();
        assertEquals("{\"status\":\"FAILED\",\"description\":\"Invalid start time blah,Invalid end time everon\"}", contentAsString);
     }
 }
