 package org.motechproject.ananya.kilkari.message.service;
 
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ananya.kilkari.message.domain.CampaignMessageAlert;
 import org.motechproject.ananya.kilkari.message.repository.AllCampaignMessageAlerts;
 import org.motechproject.ananya.kilkari.obd.service.CampaignMessageService;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNull;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class CampaignMessageAlertServiceTest {
 
     @Mock
     private AllCampaignMessageAlerts allCampaignMessageAlerts;
     @Mock
     private CampaignMessageService campaignMessageService;
 
     private CampaignMessageAlertService campaignMessageAlertService;
 
     @Before
     public void setUp() {
         initMocks(this);
         campaignMessageAlertService = new CampaignMessageAlertService(allCampaignMessageAlerts, campaignMessageService);
     }
 
     @Test
     public void shouldSaveCampaignMessageAlertIfDoesNotExist() {
         String subscriptionId = "mysubscriptionid";
         String messageId = "mymessageid";
         String msisdn = "12345678";
         String operator = "AIRTEL";
         DateTime expiryDate = DateTime.now();
 
         campaignMessageAlertService.scheduleCampaignMessageAlert(subscriptionId, messageId, expiryDate, msisdn, operator);
 
         verify(allCampaignMessageAlerts).findBySubscriptionId(subscriptionId);
         ArgumentCaptor<CampaignMessageAlert> campaignMessageAlertArgumentCaptor = ArgumentCaptor.forClass(CampaignMessageAlert.class);
         verify(allCampaignMessageAlerts).add(campaignMessageAlertArgumentCaptor.capture());
         CampaignMessageAlert campaignMessageAlert = campaignMessageAlertArgumentCaptor.getValue();
         assertEquals(subscriptionId, campaignMessageAlert.getSubscriptionId());
         assertEquals(messageId, campaignMessageAlert.getMessageId());
         assertFalse(campaignMessageAlert.isRenewed());
 
         verifyZeroInteractions(campaignMessageService);
         verify(allCampaignMessageAlerts, never()).remove(any(CampaignMessageAlert.class));
     }
 
     @Test
     public void shouldAddCampaignMessageAlertOnRenewIfItDoesNotExist() {
         String subscriptionId = "mysubscriptionid";
         String msisdn = "12345678";
         String operator = "AIRTEL";
 
         String actualMessageId = campaignMessageAlertService.scheduleCampaignMessageAlertForRenewal(subscriptionId, msisdn, operator);
 
         assertNull(actualMessageId);
         ArgumentCaptor<CampaignMessageAlert> campaignMessageAlertArgumentCaptor = ArgumentCaptor.forClass(CampaignMessageAlert.class);
         verify(allCampaignMessageAlerts).add(campaignMessageAlertArgumentCaptor.capture());
         CampaignMessageAlert value = campaignMessageAlertArgumentCaptor.getValue();
 
         assertEquals(subscriptionId, value.getSubscriptionId());
         assertNull(value.getMessageId());
         assertTrue(value.isRenewed());
     }
 
     @Test
     public void shouldRenewCampaignMessageAlertAndScheduleCampaignMessageIfMessageIdExists() {
         String subscriptionId = "mysubscriptionid";
         String msisdn = "12345678";
         String operator = "AIRTEL";
         String messageId = "WEEK12";
         DateTime messageExpiryDate = DateTime.now().plusDays(1);
 
         CampaignMessageAlert campaignMessageAlert = new CampaignMessageAlert(subscriptionId, messageId, true, messageExpiryDate);
         when(allCampaignMessageAlerts.findBySubscriptionId(subscriptionId)).thenReturn(campaignMessageAlert);
 
         String actualMessageId = campaignMessageAlertService.scheduleCampaignMessageAlertForRenewal(subscriptionId, msisdn, operator);
 
         assertEquals(messageId, actualMessageId);
         verify(allCampaignMessageAlerts).findBySubscriptionId(subscriptionId);
         ArgumentCaptor<DateTime> creationDateCaptor = ArgumentCaptor.forClass(DateTime.class);
         verify(campaignMessageService).scheduleCampaignMessage(eq(subscriptionId), eq(messageId), eq(msisdn), eq(operator), eq(messageExpiryDate), creationDateCaptor.capture());
         ArgumentCaptor<CampaignMessageAlert> captor = ArgumentCaptor.forClass(CampaignMessageAlert.class);
         verify(allCampaignMessageAlerts).remove(captor.capture());
         CampaignMessageAlert actualCampaignMessageAlert = captor.getValue();
         assertEquals(subscriptionId, actualCampaignMessageAlert.getSubscriptionId());
         DateTime creationDateValue = creationDateCaptor.getValue();
         assertEquals(DateTime.now().withMillisOfSecond(0), creationDateValue.withMillisOfSecond(0));
     }
 
     @Test
     public void shouldRemoveCampaignMessageAlertIfAlreadyExistsAndScheduleCampaignMessageIfRenewed() {
         String messageId = "mymessageid";
         String msisdn = "1234567890";
         String operator = "AIRTEL";
         String subscriptionId = "subscriptionId";
         DateTime messageExpiryDate = DateTime.now().plusDays(1);
 
         CampaignMessageAlert campaignMessageAlert = new CampaignMessageAlert(subscriptionId, messageId, true, messageExpiryDate);
 
         when(allCampaignMessageAlerts.findBySubscriptionId(subscriptionId)).thenReturn(campaignMessageAlert);
 
         campaignMessageAlertService.scheduleCampaignMessageAlert(subscriptionId, messageId, messageExpiryDate, msisdn, operator);
 
         verify(allCampaignMessageAlerts).findBySubscriptionId(subscriptionId);
         ArgumentCaptor<DateTime> DateTimeCaptor = ArgumentCaptor.forClass(DateTime.class);
         verify(campaignMessageService).scheduleCampaignMessage(eq(subscriptionId), eq(messageId), eq(msisdn), eq(operator), eq(messageExpiryDate), DateTimeCaptor.capture());
         ArgumentCaptor<CampaignMessageAlert> captor = ArgumentCaptor.forClass(CampaignMessageAlert.class);
         verify(allCampaignMessageAlerts).remove(captor.capture());
         CampaignMessageAlert actualCampaignMessageAlert = captor.getValue();
         assertEquals(subscriptionId, actualCampaignMessageAlert.getSubscriptionId());
         DateTime DateTimeCaptorValue = DateTimeCaptor.getValue();
        assertEquals(DateTime.now().withMillisOfSecond(0), DateTimeCaptorValue.withMillisOfSecond(0));
     }
 
     @Test
     public void shouldUpdateCampaignMessageAlertIfAlreadyExistsButShouldNotScheduleCampaignMessageIfNotRenewed() {
         String messageId = "mymessageid";
         String msisdn = "1234567890";
         String operator = "AIRTEL";
         String subscriptionId = "subscriptionId";
         DateTime messageExpiryDate = DateTime.now().plusDays(1);
         CampaignMessageAlert campaignMessageAlert = new CampaignMessageAlert(subscriptionId, messageId, false, messageExpiryDate);
 
         when(allCampaignMessageAlerts.findBySubscriptionId(subscriptionId)).thenReturn(campaignMessageAlert);
 
         campaignMessageAlertService.scheduleCampaignMessageAlert(subscriptionId, messageId, messageExpiryDate, msisdn, operator);
 
         verify(allCampaignMessageAlerts).findBySubscriptionId(subscriptionId);
         ArgumentCaptor<CampaignMessageAlert> campaignMessageAlertArgumentCaptor = ArgumentCaptor.forClass(CampaignMessageAlert.class);
         verify(allCampaignMessageAlerts).update(campaignMessageAlertArgumentCaptor.capture());
         CampaignMessageAlert actualCampaignMessageAlert = campaignMessageAlertArgumentCaptor.getValue();
         assertEquals(subscriptionId, actualCampaignMessageAlert.getSubscriptionId());
         assertEquals(messageId, actualCampaignMessageAlert.getMessageId());
         assertFalse(actualCampaignMessageAlert.isRenewed());
         verifyZeroInteractions(campaignMessageService);
         verify(allCampaignMessageAlerts, never()).remove(any(CampaignMessageAlert.class));
     }
 
     @Test
     public void shouldAssignAExpiryDateToWeeklyScheduledMessageWhichIsNWeeksFromStartDate_whenRenewalHasHappened() {
         String messageId = "mymessageid";
         String msisdn = "1234567890";
         String operator = "AIRTEL";
         String subscriptionId = "subscriptionId";
         DateTime messageExpiryDate = DateTime.now().minusDays(1);
 
         when(allCampaignMessageAlerts.findBySubscriptionId(subscriptionId)).thenReturn(new CampaignMessageAlert(subscriptionId, "oldmesasgeId", true, DateTime.now().minusDays(2)));
 
         campaignMessageAlertService.scheduleCampaignMessageAlert(subscriptionId, messageId, messageExpiryDate, msisdn, operator);
 
         ArgumentCaptor<CampaignMessageAlert> campaignMessageAlertArgumentCaptor = ArgumentCaptor.forClass(CampaignMessageAlert.class);
         verify(allCampaignMessageAlerts).update(campaignMessageAlertArgumentCaptor.capture());
         CampaignMessageAlert campaignMessageAlert = campaignMessageAlertArgumentCaptor.getValue();
         assertEquals(subscriptionId, campaignMessageAlert.getSubscriptionId());
         assertEquals(messageId, campaignMessageAlert.getMessageId());
         assertEquals(messageExpiryDate, campaignMessageAlert.getMessageExpiryDate());
         assertEquals(true, campaignMessageAlert.isRenewed());
     }
 
     @Test
     public void shouldAssignAExpiryDateToWeeklyScheduledMessageWhichIsNWeeksFromStartDate_whenRenewalHasNotHappened() {
         String messageId = "mymessageid";
         String msisdn = "1234567890";
         String operator = "AIRTEL";
         String subscriptionId = "subscriptionId";
         DateTime messageExpiryDate = DateTime.now().plusDays(1);
 
         when(allCampaignMessageAlerts.findBySubscriptionId(subscriptionId)).thenReturn(new CampaignMessageAlert(subscriptionId, "oldmesasgeId", false, DateTime.now().minusDays(1)));
 
         campaignMessageAlertService.scheduleCampaignMessageAlert(subscriptionId, messageId, messageExpiryDate, msisdn, operator);
 
         ArgumentCaptor<CampaignMessageAlert> campaignMessageAlertArgumentCaptor = ArgumentCaptor.forClass(CampaignMessageAlert.class);
         verify(allCampaignMessageAlerts).update(campaignMessageAlertArgumentCaptor.capture());
         CampaignMessageAlert campaignMessageAlert = campaignMessageAlertArgumentCaptor.getValue();
         assertEquals(subscriptionId, campaignMessageAlert.getSubscriptionId());
         assertEquals(messageId, campaignMessageAlert.getMessageId());
         assertEquals(messageExpiryDate, campaignMessageAlert.getMessageExpiryDate());
         assertEquals(false, campaignMessageAlert.isRenewed());
     }
 
 
     @Test
     public void shouldNotUpdateExpiryDateWhenTryingToScheduleViaRenewal_whenCampaignMessageAlertAlreadyExist() {
         String messageId = "mymessageid";
         String msisdn = "1234567890";
         String operator = "AIRTEL";
         String subscriptionId = "subscriptionId";
         DateTime messageExpiryDate = DateTime.now().minusDays(1);
 
         when(allCampaignMessageAlerts.findBySubscriptionId(subscriptionId)).thenReturn(new CampaignMessageAlert(subscriptionId, messageId, false, messageExpiryDate));
 
         String actualMessageId = campaignMessageAlertService.scheduleCampaignMessageAlertForRenewal(subscriptionId, msisdn, operator);
 
         assertEquals(actualMessageId, messageId);
         ArgumentCaptor<CampaignMessageAlert> campaignMessageAlertArgumentCaptor = ArgumentCaptor.forClass(CampaignMessageAlert.class);
         verify(allCampaignMessageAlerts).update(campaignMessageAlertArgumentCaptor.capture());
         CampaignMessageAlert campaignMessageAlert = campaignMessageAlertArgumentCaptor.getValue();
         assertEquals(subscriptionId, campaignMessageAlert.getSubscriptionId());
         assertEquals(messageId, campaignMessageAlert.getMessageId());
         assertEquals(messageExpiryDate, campaignMessageAlert.getMessageExpiryDate());
         assertTrue(campaignMessageAlert.isRenewed());
     }
 
     @Test
     public void shouldNotSetExpiryDateWhenTryingToScheduleViaRenewal_whenCampaignMessageAlertDoesNotAlreadyExist() {
         String msisdn = "1234567890";
         String operator = "AIRTEL";
         String subscriptionId = "subscriptionId";
 
         String actualMessageId = campaignMessageAlertService.scheduleCampaignMessageAlertForRenewal(subscriptionId, msisdn, operator);
 
         assertNull(actualMessageId);
         verify(allCampaignMessageAlerts, never()).update(any(CampaignMessageAlert.class));
     }
 
     @Test
     public void shouldAddNewAlertIfDoesNotExistOnActivation() {
         String subscriptionId = "subscriptionId";
         String msisdn = "1234567890";
         String operator = "AIRTEL";
 
         String actualMessageId = campaignMessageAlertService.scheduleCampaignMessageAlertForActivation(subscriptionId, msisdn, operator);
 
         assertNull(actualMessageId);
         ArgumentCaptor<CampaignMessageAlert> campaignMessageAlertArgumentCaptor = ArgumentCaptor.forClass(CampaignMessageAlert.class);
         verify(allCampaignMessageAlerts).add(campaignMessageAlertArgumentCaptor.capture());
         CampaignMessageAlert campaignMessageAlert = campaignMessageAlertArgumentCaptor.getValue();
         assertEquals(subscriptionId, campaignMessageAlert.getSubscriptionId());
         assertNull(campaignMessageAlert.getMessageId());
         assertNull(campaignMessageAlert.getMessageExpiryDate());
         assertTrue(campaignMessageAlert.isRenewed());
     }
 
     @Test
     public void shouldScheduleCampaignMessageIfExistsAndNotExpired() {
         String subscriptionId = "subscriptionId";
         String msisdn = "1234567890";
         String operator = "AIRTEL";
         DateTime messageExpiryDate = DateTime.now().plusDays(1);
         String messageId = "messageId";
         when(allCampaignMessageAlerts.findBySubscriptionId(subscriptionId)).thenReturn(new CampaignMessageAlert(subscriptionId, messageId, false, messageExpiryDate));
 
         String actualMessageId = campaignMessageAlertService.scheduleCampaignMessageAlertForActivation(subscriptionId, msisdn, operator);
 
         assertEquals(messageId, actualMessageId);
         ArgumentCaptor<CampaignMessageAlert> campaignMessageAlertArgumentCaptor = ArgumentCaptor.forClass(CampaignMessageAlert.class);
         verify(allCampaignMessageAlerts).remove(campaignMessageAlertArgumentCaptor.capture());
         CampaignMessageAlert campaignMessageAlert = campaignMessageAlertArgumentCaptor.getValue();
         assertEquals(subscriptionId, campaignMessageAlert.getSubscriptionId());
         ArgumentCaptor<DateTime> dateTimeArgumentCaptor = ArgumentCaptor.forClass(DateTime.class);
         verify(campaignMessageService).scheduleCampaignMessage(eq(subscriptionId), eq(messageId), eq(msisdn), eq(operator), eq(messageExpiryDate), dateTimeArgumentCaptor.capture());
         DateTime actualDateTime = dateTimeArgumentCaptor.getValue();
         assertEquals(DateTime.now().withMillisOfSecond(0), actualDateTime.withMillisOfSecond(0));
     }
 
     @Test
     public void shouldScheduleCampaignMessageIfExistsAndAlreadyExpired() {
         String subscriptionId = "subscriptionId";
         String msisdn = "1234567890";
         String operator = "AIRTEL";
         DateTime messageExpiryDate = DateTime.now().minusDays(1);
         String messageId = "messageId";
         when(allCampaignMessageAlerts.findBySubscriptionId(subscriptionId)).thenReturn(new CampaignMessageAlert(subscriptionId, messageId, false, messageExpiryDate));
 
         String actualMessageId = campaignMessageAlertService.scheduleCampaignMessageAlertForActivation(subscriptionId, msisdn, operator);
 
         assertEquals(messageId, actualMessageId);
         ArgumentCaptor<CampaignMessageAlert> campaignMessageAlertArgumentCaptor = ArgumentCaptor.forClass(CampaignMessageAlert.class);
         verify(allCampaignMessageAlerts).remove(campaignMessageAlertArgumentCaptor.capture());
         CampaignMessageAlert campaignMessageAlert = campaignMessageAlertArgumentCaptor.getValue();
         assertEquals(subscriptionId, campaignMessageAlert.getSubscriptionId());
         ArgumentCaptor<DateTime> dateTimeArgumentCaptor = ArgumentCaptor.forClass(DateTime.class);
         verify(campaignMessageService).scheduleCampaignMessage(eq(subscriptionId), eq(messageId), eq(msisdn), eq(operator), eq(messageExpiryDate), dateTimeArgumentCaptor.capture());
         DateTime actualDateTime = dateTimeArgumentCaptor.getValue();
         assertEquals(DateTime.now().withMillisOfSecond(0), actualDateTime.withMillisOfSecond(0));
     }
 
     @Test
     public void shouldDeleteCampaignMessageAlertForASubscriptionId() {
         String subscriptionId = "subscriptionId";
 
         campaignMessageAlertService.deleteFor(subscriptionId);
 
         verify(allCampaignMessageAlerts).deleteFor(subscriptionId);
     }
 
     @Test
     public void shouldClearMessageId() {
         String subscriptionId = "subscriptionId";
         DateTime messageExpiryDate = DateTime.now().plusWeeks(1);
         String messageId = "messageId";
         when(allCampaignMessageAlerts.findBySubscriptionId(subscriptionId)).thenReturn(new CampaignMessageAlert(subscriptionId, messageId, false, messageExpiryDate));
 
         campaignMessageAlertService.clearMessageId(subscriptionId);
 
         ArgumentCaptor<CampaignMessageAlert> campaignMessageAlertArgumentCaptor = ArgumentCaptor.forClass(CampaignMessageAlert.class);
         verify(allCampaignMessageAlerts).update(campaignMessageAlertArgumentCaptor.capture());
         CampaignMessageAlert campaignMessageAlert = campaignMessageAlertArgumentCaptor.getValue();
         assertNull(campaignMessageAlert.getMessageId());
     }
 
     @Test
     public void shouldNotClearMessageIdIfThereAreNoAlerts() {
         String subscriptionId = "subscriptionId";
         when(allCampaignMessageAlerts.findBySubscriptionId(subscriptionId)).thenReturn(null);
 
         campaignMessageAlertService.clearMessageId(subscriptionId);
 
         verify(allCampaignMessageAlerts, never()).update(any(CampaignMessageAlert.class));
     }
 
     @Test
     public void shouldFindCampaignMessageAlertBySubscriptionId() {
         String subscriptionId = "subscriptionId";
         CampaignMessageAlert campaignMessageAlert = new CampaignMessageAlert(subscriptionId, "WEEK38", false, DateTime.now());
         when(allCampaignMessageAlerts.findBySubscriptionId(subscriptionId)).thenReturn(campaignMessageAlert);
 
         CampaignMessageAlert actualCampaignMessageAlert = campaignMessageAlertService.findBy(subscriptionId);
 
         assertEquals(campaignMessageAlert, actualCampaignMessageAlert);
     }
 }
