 package org.motechproject.ananya.kilkari.service;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.After;
import org.junit.Before;
 import org.junit.Test;
 import org.motechproject.ananya.kilkari.domain.CampaignMessage;
 import org.motechproject.ananya.kilkari.repository.AllCampaignMessages;
 import org.motechproject.ananya.kilkari.utils.SpringIntegrationTest;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.List;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.fail;
 
 public class CampaignMessageServiceIT extends SpringIntegrationTest {
 
     @Autowired
     private CampaignMessageService campaignMessageService;
 
     @Autowired
     private AllCampaignMessages allCampaignMessages;
 
    @After
    @Before
    public void setUp() {
        allCampaignMessages.removeAll();
    }

     @Test
     public void shouldDeleteTheCampaignMessageIfItExists() {
         String subscriptionId = "subscriptionId";
         String messageId = "messageId";
         allCampaignMessages.add(new CampaignMessage(subscriptionId, messageId, null, null));
 
         campaignMessageService.deleteCampaignMessage(subscriptionId, messageId);
 
         CampaignMessage campaignMessage = allCampaignMessages.find(subscriptionId, messageId);
         assertNull(campaignMessage);
     }
 
     @Test
     public void shouldNotDeleteTheCampaignMessageIfItDoesNotExists() {
         String subscriptionId = "subscriptionId";
         String messageId = "messageId";
         allCampaignMessages.add(new CampaignMessage(subscriptionId, messageId, null, null));
 
         campaignMessageService.deleteCampaignMessage("subscriptionId2", messageId);
 
         CampaignMessage campaignMessage = allCampaignMessages.find(subscriptionId, messageId);
         assertNotNull(campaignMessage);
     }
 
     @Test
     public void shouldSaveTheCampaignMessageToDB() {
         String subscriptionId = "subscriptionId";
         String messageId = "messageId";
         String operator = "airtel";
         String msisdn = "msisdn";
 
         campaignMessageService.scheduleCampaignMessage(subscriptionId, messageId, msisdn, operator);
 
         List<CampaignMessage> all = allCampaignMessages.getAll();
         for (CampaignMessage campaignMessage : all) {
             if (equals(new CampaignMessage(subscriptionId, messageId, msisdn, operator), campaignMessage)) {
                 markForDeletion(campaignMessage);
                 return;
             }
         }
         fail("Should have found created campaign message");
     }
 
     private boolean equals(CampaignMessage expected, CampaignMessage actual) {
         if (actual == null) {
             return false;
         }
         return new EqualsBuilder()
                 .append(expected.getMessageId(), actual.getMessageId())
                 .append(expected.getSubscriptionId(), actual.getSubscriptionId())
                 .append(expected.getMsisdn(), actual.getMsisdn())
                 .append(expected.getOperator(), expected.getOperator())
                 .isEquals();
     }
 }
