 package org.motechproject.ananya.kilkari.web.it;
 
 import com.google.gson.Gson;
 import org.apache.commons.lang.StringUtils;
 import org.junit.Test;
 import org.motechproject.ananya.kilkari.domain.Channel;
 import org.motechproject.ananya.kilkari.domain.Subscription;
 import org.motechproject.ananya.kilkari.domain.SubscriptionPack;
 import org.motechproject.ananya.kilkari.messagecampaign.request.KilkariMessageCampaignEnrollmentRecord;
 import org.motechproject.ananya.kilkari.messagecampaign.service.KilkariMessageCampaignService;
 import org.motechproject.ananya.kilkari.repository.AllSubscriptions;
 import org.motechproject.ananya.kilkari.service.KilkariCampaignService;
 import org.motechproject.ananya.kilkari.web.SpringIntegrationTest;
 import org.motechproject.ananya.kilkari.web.controller.SubscriptionController;
 import org.motechproject.ananya.kilkari.web.interceptors.KilkariChannelInterceptor;
 import org.motechproject.ananya.kilkari.web.contract.mapper.SubscriptionDetailsMapper;
 import org.motechproject.ananya.kilkari.web.contract.response.BaseResponse;
 import org.motechproject.ananya.kilkari.web.contract.response.SubscriberResponse;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.web.server.MvcResult;
 import org.springframework.test.web.server.setup.MockMvcBuilders;
 
 import static org.junit.Assert.*;
 import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
 import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;
 
 public class SubscriptionControllerIT extends SpringIntegrationTest {
 
     @Autowired
     private SubscriptionController subscriptionController;
 
     @Autowired
     private AllSubscriptions allSubscriptions;
     @Autowired
     private KilkariMessageCampaignService kilkariMessageCampaignService;
 
     @Test
     public void shouldRetrieveSubscriptionDetailsFromDatabase() throws Exception {
         String msisdn = "9876543210";
         String channelString = Channel.IVR.toString();
         Subscription subscription1 = new Subscription(msisdn, SubscriptionPack.TWELVE_MONTHS);
         Subscription subscription2 = new Subscription(msisdn, SubscriptionPack.FIFTEEN_MONTHS);
         allSubscriptions.add(subscription1);
         allSubscriptions.add(subscription2);
         markForDeletion(subscription1);
         markForDeletion(subscription2);
 
         SubscriberResponse subscriberResponse = new SubscriberResponse();
         subscriberResponse.addSubscriptionDetail(SubscriptionDetailsMapper.mapFrom(subscription1));
         subscriberResponse.addSubscriptionDetail(SubscriptionDetailsMapper.mapFrom(subscription2));
 
         MvcResult result = MockMvcBuilders.standaloneSetup(subscriptionController)
                 .addInterceptors(new KilkariChannelInterceptor()).build()
                     .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channelString))
                     .andExpect(status().isOk())
                     .andExpect(content().type("application/javascript;charset=UTF-8"))
                     .andReturn();
 
         String responseString = result.getResponse().getContentAsString();
         responseString = performIVRChannelValidationAndCleanup(responseString, channelString);
 
         SubscriberResponse actualResponse = fromJson(responseString, SubscriberResponse.class);
         assertEquals(subscriberResponse, actualResponse);
     }
 
     @Test
     public void shouldCreateSubscriptionForTheGivenMsisdn() throws Exception {
 
         final String msisdn = "9876543210";
         String channelString = Channel.IVR.toString();
         final SubscriptionPack pack = SubscriptionPack.TWELVE_MONTHS;
         BaseResponse expectedResponse = new BaseResponse("SUCCESS", "Subscription request submitted successfully");
 
         MvcResult result = MockMvcBuilders.standaloneSetup(subscriptionController)
                 .addInterceptors(new KilkariChannelInterceptor()).build()
                 .perform(get("/subscription").param("msisdn", msisdn).param("pack", pack.toString())
                         .param("channel", channelString))
                 .andExpect(status().isOk())
                 .andExpect(content().type("application/javascript;charset=UTF-8"))
                 .andReturn();
 
         String responseString = result.getResponse().getContentAsString();
         responseString = performIVRChannelValidationAndCleanup(responseString, channelString);
 
         BaseResponse actualResponse =  (BaseResponse) new BaseResponse().fromJson(responseString);
         assertEquals(expectedResponse, actualResponse);
 
         final Subscription[] subscription = new Subscription[1];
 
         new TimedRunner() {
             @Override
             boolean run() {
                 subscription[0] = allSubscriptions.findByMsisdnAndPack(msisdn, pack);
                 return (subscription[0] != null);
 
             }
         }.executeWithTimeout();
 
         assertNotNull(subscription[0]);
         markForDeletion(subscription[0]);
         assertEquals(msisdn, subscription[0].getMsisdn());
         assertEquals(pack, subscription[0].getPack());
         assertFalse(StringUtils.isBlank(subscription[0].getSubscriptionId()));
 
         final KilkariMessageCampaignEnrollmentRecord[] campaignEnrollmentRecord =
                 new KilkariMessageCampaignEnrollmentRecord[1];
 
         new TimedRunner() {
             @Override
             boolean run() {
                  campaignEnrollmentRecord[0] = kilkariMessageCampaignService.searchEnrollment(
                          subscription[0].getSubscriptionId(), KilkariCampaignService.KILKARI_MESSAGE_CAMPAIGN_NAME);
                 return (campaignEnrollmentRecord[0] != null);
             }
         }.executeWithTimeout();
 
         assertNotNull(campaignEnrollmentRecord[0]);
         assertEquals(subscription[0].getSubscriptionId(), campaignEnrollmentRecord[0].getExternalId());
         assertEquals(KilkariCampaignService.KILKARI_MESSAGE_CAMPAIGN_NAME, campaignEnrollmentRecord[0].getCampaignName());
     }
 
     @Test
     public void shouldCreateScheduleForTheGivenMsisdn() throws Exception {
         final String msisdn = "9876543210";
         String channelString = Channel.IVR.toString();
         final SubscriptionPack pack = SubscriptionPack.TWELVE_MONTHS;
         BaseResponse expectedResponse = new BaseResponse("SUCCESS", "Subscription request submitted successfully");
 
         MvcResult result = MockMvcBuilders.standaloneSetup(subscriptionController)
                 .addInterceptors(new KilkariChannelInterceptor()).build()
                 .perform(get("/subscription").param("msisdn", msisdn).param("pack", pack.toString())
                         .param("channel", channelString))
                 .andExpect(status().isOk())
                 .andExpect(content().type("application/javascript;charset=UTF-8"))
                 .andReturn();
 
         String responseString = result.getResponse().getContentAsString();
         responseString = performIVRChannelValidationAndCleanup(responseString, channelString);
 
         BaseResponse actualResponse =  (BaseResponse) new BaseResponse().fromJson(responseString);
         assertEquals(expectedResponse, actualResponse);
 
        assertScheduleCreatedFor(msisdn);
 
     }
 
     private void assertScheduleCreatedFor(String msisdn) {
 
     }
 
     private String performIVRChannelValidationAndCleanup(String jsonContent, String channel) {
         if (Channel.isIVR(channel)) {
             assertTrue(jsonContent.startsWith(KilkariChannelInterceptor.IVR_RESPONSE_PREFIX));
             jsonContent = jsonContent.replace(KilkariChannelInterceptor.IVR_RESPONSE_PREFIX, "");
         }
         return jsonContent;
     }
 
     private String toJson(Object objectToSerialize) {
         Gson gson = new Gson();
         return gson.toJson(objectToSerialize);
     }
 
     private <T> T fromJson(String jsonString, Class<T> subscriberResponseClass) {
         Gson gson = new Gson();
         return gson.fromJson(jsonString, subscriberResponseClass);
     }
 
 }
