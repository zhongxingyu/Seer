 package org.motechproject.ghana.national.repository;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.motechproject.cmslite.api.model.ContentNotFoundException;
 import org.motechproject.cmslite.api.model.StringContent;
 import org.motechproject.cmslite.api.service.CMSLiteService;
 import org.motechproject.ghana.national.domain.AlertWindow;
 import org.motechproject.ghana.national.domain.SmsTemplateKeys;
 import org.motechproject.ghana.national.messagegateway.domain.MessageRecipientType;
 import org.motechproject.ghana.national.messagegateway.domain.SMS;
 import org.motechproject.testing.utils.BaseUnitTest;
 import org.motechproject.util.DateUtil;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 
 import static ch.lambdaj.Lambda.join;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.ghana.national.domain.AlertWindow.DUE;
 import static org.motechproject.ghana.national.domain.AlertWindow.UPCOMING;
 
 public class AggregationStrategyImplTest extends BaseUnitTest {
 
     @Mock
     CMSLiteService mockCmsLiteService;
 
     @InjectMocks
     AggregationStrategyImpl aggregationStrategy = new AggregationStrategyImpl();
 
     @Before
     public void setUp() throws Exception {
         initMocks(this);
         super.mockCurrentDate(DateUtil.now());
     }
 
     @Test
     public void shouldFilterDefaultMessagesFromListOfSmsIfThereAreValidMessages() throws ContentNotFoundException {
         final String defaultMessage = "default-message";
 
         when(mockCmsLiteService.getStringContent(Locale.getDefault().getLanguage(),
                 SmsTemplateKeys.FACILITIES_DEFAULT_MESSAGE_KEY)).thenReturn(new StringContent(null, null, defaultMessage));
         final SMS defaultSMS = SMS.fromText(defaultMessage, "ph", null, null, MessageRecipientType.FACILITY);
         List<SMS> messagesList = new ArrayList<SMS>() {{
             add(SMS.fromText(DUE.getName() + ",milestoneName1,motechId,serialNumber,firstName,lastName", "ph", null, null, MessageRecipientType.FACILITY));
             add(SMS.fromText(DUE.getName() + ",milestoneName2,motechId,serialNumber,firstName,lastName", "ph", null, null, MessageRecipientType.FACILITY));
             add(defaultSMS);
         }};
         List<SMS> filteredSMSs = aggregationStrategy.aggregate(messagesList);
         assertThat(filteredSMSs, not(hasItem(defaultSMS)));
     }
 
     @Test
     public void shouldNotFilterDefaultMessagesFromListOfSmsIfThereAreNoValidMessages() throws ContentNotFoundException {
         final String defaultMessage = "default-message";
 
         when(mockCmsLiteService.getStringContent(Locale.getDefault().getLanguage(),
                 SmsTemplateKeys.FACILITIES_DEFAULT_MESSAGE_KEY)).thenReturn(new StringContent(null, null, defaultMessage));
         final SMS defaultSMS = SMS.fromText(defaultMessage, "ph", null, null, MessageRecipientType.FACILITY);
         List<SMS> messagesList = new ArrayList<SMS>() {{
             add(defaultSMS);
         }};
         List<SMS> filteredSMSs = aggregationStrategy.aggregate(messagesList);
         assertThat(filteredSMSs, hasItem(defaultSMS));
     }
 
     @Test
     public void shouldSendAggregatedSMSForPatient() throws ContentNotFoundException {
         when(mockCmsLiteService.getStringContent(Locale.getDefault().getLanguage(),
                 SmsTemplateKeys.FACILITIES_DEFAULT_MESSAGE_KEY)).thenReturn(new StringContent(null, null, "${facility} has no ${windowNames} cares for this week"));
         List<SMS> messagesList = new ArrayList<SMS>() {{
             add(SMS.fromText(UPCOMING.getName() + ",milestoneName1,motechId,serialNumber,firstName,lastName", "ph", null, null, MessageRecipientType.FACILITY));
             add(SMS.fromText(UPCOMING.getName() + ",milestoneName2,motechId,serialNumber,firstName,lastName", "ph", null, null, MessageRecipientType.FACILITY));
             add(SMS.fromText(DUE.getName() + ",milestoneName,motechId,serialNumber,firstName,lastName", "ph", null, null, MessageRecipientType.FACILITY));
             add(SMS.fromText(DUE.getName() + ",milestoneName,motechId2,serialNumber,firstName2,lastName3", "ph", null, null, MessageRecipientType.FACILITY));
             add(SMS.fromText(DUE.getName() + ",milestoneName,motechId3,serialNumber,firstName2,lastName3", "ph", null, null, MessageRecipientType.FACILITY));
            add(SMS.fromText("Ashanti MEPS has no " + join(AlertWindow.ghanaNationalWindowNames(), ", ") + " cares for this week", "ph", null, null, MessageRecipientType.FACILITY));
         }};
 
         final List<SMS> aggregatedSMSList = aggregationStrategy.aggregate(messagesList);
         assertThat(aggregatedSMSList, hasItem(SMS.fromText(UPCOMING.getName() + ": firstName lastName, motechId, serialNumber, milestoneName1, milestoneName2", "ph", DateUtil.now(), null, MessageRecipientType.FACILITY)));
         assertThat(aggregatedSMSList, hasItem(SMS.fromText(DUE.getName() + ": firstName lastName, motechId, serialNumber, milestoneName, firstName2 lastName3, motechId2, serialNumber, milestoneName, firstName2 lastName3, motechId3, serialNumber, milestoneName", "ph", DateUtil.now(), null, MessageRecipientType.FACILITY)));
         assertThat(aggregatedSMSList, hasItem(SMS.fromText("Ashanti MEPS has no Overdue cares for this week", "ph", DateUtil.now(), null, MessageRecipientType.FACILITY)));
     }
 
     @Test
     public void shouldAggregateManySMSBasedOnWindowNames() throws ContentNotFoundException {
         final String message1 = "patient due for anc 1";
         final String message2 = "U are late for measles";
         final String message3 = "this is the third alert";
 
         List<SMS> messagesList = new ArrayList<SMS>() {{
             add(SMS.fromText(message1, "ph", null, null, MessageRecipientType.PATIENT));
             add(SMS.fromText(message2, "ph", null, null, MessageRecipientType.PATIENT));
             add(SMS.fromText(message3, "ph", null, null, MessageRecipientType.PATIENT));
         }};
 
         final List<SMS> aggregatedSMSList = aggregationStrategy.aggregate(messagesList);
         assertThat(aggregatedSMSList.size(), is(1));
         assertThat(aggregatedSMSList, hasItem(SMS.fromText(message1 + AggregationStrategyImpl.SMS_SEPARATOR + message2
                 + AggregationStrategyImpl.SMS_SEPARATOR + message3 + AggregationStrategyImpl.SMS_SEPARATOR, "ph", DateUtil.now(), null, MessageRecipientType.PATIENT)));
     }
 
     @Test
     public void shouldReturnEmptyListIfThereIsNothingToAggregate() {
         List<SMS> smsList = aggregationStrategy.aggregate(Collections.<SMS>emptyList());
         assertThat(smsList.size(), is(0));
     }
 }
