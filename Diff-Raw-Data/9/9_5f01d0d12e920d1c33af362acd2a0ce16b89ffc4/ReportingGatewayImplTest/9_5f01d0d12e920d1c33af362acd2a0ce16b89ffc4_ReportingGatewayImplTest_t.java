 package org.motechproject.ananya.kilkari.reporting.repository;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Captor;
 import org.mockito.Mock;
 import org.motechproject.ananya.reports.kilkari.contract.request.CallDetailsReportRequest;
 import org.motechproject.ananya.reports.kilkari.contract.request.SubscriberReportRequest;
 import org.motechproject.ananya.reports.kilkari.contract.request.SubscriptionReportRequest;
 import org.motechproject.ananya.reports.kilkari.contract.request.SubscriptionStateChangeRequest;
 import org.motechproject.ananya.reports.kilkari.contract.response.LocationResponse;
 import org.motechproject.ananya.reports.kilkari.contract.response.SubscriberResponse;
 import org.motechproject.http.client.domain.Method;
 import org.motechproject.http.client.service.HttpClientService;
 import org.motechproject.web.context.HttpThreadContext;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.client.HttpClientErrorException;
 import org.springframework.web.client.RestTemplate;
 
 import java.util.HashMap;
 import java.util.Properties;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class ReportingGatewayImplTest {
     private ReportingGateway reportingGateway;
     @Mock
     private RestTemplate restTemplate;
     @Mock
     private Properties kilkariProperties;
     @Mock
     private HttpClientService httpClientService;
     @Captor
     private ArgumentCaptor<Class<String>> responseTypeArgumentCaptor;
     @Captor
     private ArgumentCaptor<HashMap<String, String>> urlVariablesArgumentCaptor;
 
     @Rule
     public ExpectedException expectedException = ExpectedException.none();
 
     @Before
     public void setUp() {
         initMocks(this);
         reportingGateway = new ReportingGatewayImpl(restTemplate, httpClientService, kilkariProperties);
     }
 
     @Test
     public void shouldInvokeReportingServiceWithGetLocations() {
         when(kilkariProperties.getProperty("reporting.service.base.url")).thenReturn("url");
         LocationResponse expectedLocation = new LocationResponse("mydistrict", "myblock", "mypanchayat");
         when(restTemplate.getForEntity(any(String.class), any(Class.class))).thenReturn(new ResponseEntity(expectedLocation, HttpStatus.OK));
 
         LocationResponse actualLocation = reportingGateway.getLocation("mydistrict", "myblock", "my panchayat");
 
         assertEquals(expectedLocation, actualLocation);
 
         ArgumentCaptor<String> urlArgumentCaptor = ArgumentCaptor.forClass(String.class);
         ArgumentCaptor<Class> subscriberLocationCaptor = ArgumentCaptor.forClass(Class.class);
         verify(restTemplate).getForEntity(urlArgumentCaptor.capture(), subscriberLocationCaptor.capture());
 
         assertEquals(LocationResponse.class, subscriberLocationCaptor.getValue());
 
         verify(kilkariProperties).getProperty("reporting.service.base.url");
         String url = urlArgumentCaptor.getValue();
         assertTrue(url.startsWith("url/location?"));
         assertTrue(url.contains("district=mydistrict"));
         assertTrue(url.contains("block=myblock"));
        assertTrue(url.contains("panchayat=my panchayat"));
     }
 
     @Test
     public void shouldReturnNullIfLocationNotPresent() {
         when(kilkariProperties.getProperty("reporting.service.base.url")).thenReturn("url");
         when(restTemplate.getForEntity(any(String.class), any(Class.class))).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
 
         LocationResponse actualLocation = reportingGateway.getLocation("mydistrict", "myblock", "mypanchayat");
 
         assertNull(actualLocation);
 
         ArgumentCaptor<String> urlArgumentCaptor = ArgumentCaptor.forClass(String.class);
         ArgumentCaptor<Class> subscriberLocationCaptor = ArgumentCaptor.forClass(Class.class);
         verify(restTemplate).getForEntity(urlArgumentCaptor.capture(), subscriberLocationCaptor.capture());
 
         assertEquals(LocationResponse.class, subscriberLocationCaptor.getValue());
 
         verify(kilkariProperties).getProperty("reporting.service.base.url");
         String url = urlArgumentCaptor.getValue();
         assertTrue(url.startsWith("url/location?"));
         assertTrue(url.contains("district=mydistrict"));
         assertTrue(url.contains("block=myblock"));
         assertTrue(url.contains("panchayat=mypanchayat"));
     }
 
     @Test
     public void shouldRethrowAnyOtherExceptionOnGetLocation() {
         when(kilkariProperties.getProperty("reporting.service.base.url")).thenReturn("url");
         when(restTemplate.getForEntity(any(String.class), any(Class.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
         expectedException.expect(HttpClientErrorException.class);
         expectedException.expectMessage("400 BAD_REQUEST");
 
         reportingGateway.getLocation("mydistrict", "myblock", "mypanchayat");
 
         ArgumentCaptor<String> urlArgumentCaptor = ArgumentCaptor.forClass(String.class);
         ArgumentCaptor<Class> subscriberLocationCaptor = ArgumentCaptor.forClass(Class.class);
         verify(restTemplate).getForEntity(urlArgumentCaptor.capture(), subscriberLocationCaptor.capture());
 
         assertEquals(LocationResponse.class, subscriberLocationCaptor.getValue());
 
         verify(kilkariProperties).getProperty("reporting.service.base.url");
         String url = urlArgumentCaptor.getValue();
         assertTrue(url.startsWith("url/location?"));
         assertTrue(url.contains("district=mydistrict"));
         assertTrue(url.contains("block=myblock"));
         assertTrue(url.contains("panchayat=mypanchayat"));
     }
 
     @Test
     public void shouldInvokeReportingServiceWithGetLocationsIfDistrctNotPresent() {
         when(kilkariProperties.getProperty("reporting.service.base.url")).thenReturn("url");
         LocationResponse expectedLocation = new LocationResponse(null, "myblock", "mypanchayat");
         when(restTemplate.getForEntity(any(String.class), any(Class.class))).thenReturn(new ResponseEntity(expectedLocation, HttpStatus.OK));
 
         LocationResponse actualLocation = reportingGateway.getLocation(null, "myblock", "mypanchayat");
 
         assertEquals(expectedLocation, actualLocation);
 
         ArgumentCaptor<String> urlArgumentCaptor = ArgumentCaptor.forClass(String.class);
         ArgumentCaptor<Class> subscriberLocationCaptor = ArgumentCaptor.forClass(Class.class);
         verify(restTemplate).getForEntity(urlArgumentCaptor.capture(), subscriberLocationCaptor.capture());
 
         assertEquals(LocationResponse.class, subscriberLocationCaptor.getValue());
 
         verify(kilkariProperties).getProperty("reporting.service.base.url");
         String url = urlArgumentCaptor.getValue();
         assertTrue(url.startsWith("url/location?"));
         assertTrue(url.contains("block=myblock"));
         assertTrue(url.contains("panchayat=mypanchayat"));
     }
 
     @Test
     public void shouldInvokeReportingServiceToGetLocationIfDistrictBlockAndPanchayatAreNotPresent() {
         when(kilkariProperties.getProperty("reporting.service.base.url")).thenReturn("url");
         LocationResponse expectedLocation = new LocationResponse(null, "myblock", "mypanchayat");
         when(restTemplate.getForEntity(any(String.class), any(Class.class))).thenReturn(new ResponseEntity(expectedLocation, HttpStatus.OK));
 
         LocationResponse actualLocation = reportingGateway.getLocation(null, null, null);
 
         assertEquals(expectedLocation, actualLocation);
 
         ArgumentCaptor<String> urlArgumentCaptor = ArgumentCaptor.forClass(String.class);
         ArgumentCaptor<Class> subscriberLocationCaptor = ArgumentCaptor.forClass(Class.class);
         verify(restTemplate).getForEntity(urlArgumentCaptor.capture(), subscriberLocationCaptor.capture());
 
         assertEquals(LocationResponse.class, subscriberLocationCaptor.getValue());
 
         verify(kilkariProperties).getProperty("reporting.service.base.url");
         String url = urlArgumentCaptor.getValue();
         assertEquals("url/location", url);
     }
 
     @Test
     public void shouldReportASubscriptionCreation() {
         SubscriptionReportRequest subscriptionCreationReportRequest = mock(SubscriptionReportRequest.class);
         when(kilkariProperties.getProperty("reporting.service.base.url")).thenReturn("url");
 
         reportingGateway.reportSubscriptionCreation(subscriptionCreationReportRequest);
 
         verify(httpClientService).execute("url/subscription", subscriptionCreationReportRequest, Method.POST);
     }
 
     @Test
     public void shouldReportASubscriptionStateChange() {
         String subscriptionId = "subscriptionId";
         SubscriptionStateChangeRequest subscriptionStateChangeRequest = mock(SubscriptionStateChangeRequest.class);
         when(subscriptionStateChangeRequest.getSubscriptionId()).thenReturn(subscriptionId);
         when(kilkariProperties.getProperty("reporting.service.base.url")).thenReturn("url");
 
         reportingGateway.reportSubscriptionStateChange(subscriptionStateChangeRequest);
 
         verify(httpClientService).execute("url/subscription/" + subscriptionId, subscriptionStateChangeRequest, Method.PUT);
     }
 
     @Test
     public void shouldReportASuccessfulCampaignMessageDelivery() {
         CallDetailsReportRequest campaignMessageDeliveryReportRequest = mock(CallDetailsReportRequest.class);
         when(kilkariProperties.getProperty("reporting.service.base.url")).thenReturn("url");
 
         reportingGateway.reportCampaignMessageDeliveryStatus(campaignMessageDeliveryReportRequest);
 
         verify(httpClientService).execute("url/callDetails", campaignMessageDeliveryReportRequest, Method.POST);
     }
 
     @Test
     public void shouldReportASubscriberUpdate() {
         String subscriptionId = "subscriptionId";
         String beneficiaryName = "Name";
         SubscriberReportRequest subscriberReportRequest = new SubscriberReportRequest(null, beneficiaryName, null, null);
         when(kilkariProperties.getProperty("reporting.service.base.url")).thenReturn("url");
 
         reportingGateway.reportSubscriberDetailsChange(subscriptionId, subscriberReportRequest);
 
         ArgumentCaptor<SubscriberReportRequest> requestCaptor = ArgumentCaptor.forClass(SubscriberReportRequest.class);
         ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
         ArgumentCaptor<Method> methodArgumentCaptor = ArgumentCaptor.forClass(Method.class);
         verify(httpClientService).execute(urlCaptor.capture(), requestCaptor.capture(), methodArgumentCaptor.capture());
         SubscriberReportRequest requestCaptorValue = requestCaptor.getValue();
         String urlCaptorValue = urlCaptor.getValue();
         assertEquals(beneficiaryName, requestCaptorValue.getBeneficiaryName());
         assertEquals("url/subscriber/" + subscriptionId, urlCaptorValue);
         assertEquals(Method.PUT, methodArgumentCaptor.getValue());
     }
 
     @Test
     public void shouldReportMsisdnChange() {
         String msisdn = "msisdn";
         String subscriptionId = "subscriptionId";
         when(kilkariProperties.getProperty("reporting.service.base.url")).thenReturn("url");
 
         reportingGateway.reportChangeMsisdnForSubscriber(subscriptionId, msisdn);
 
         verify(httpClientService).execute("url/subscription/changemsisdn?subscriptionId=" + subscriptionId + "&msisdn=" + msisdn, null, Method.POST);
     }
 
     @Test
     public void shouldMakeSynchronousCallIfSourceIsCallCenter() {
         String msisdn = "msisdn";
         String subscriptionId = "subscriptionId";
         when(kilkariProperties.getProperty("reporting.service.base.url")).thenReturn("url");
         HttpThreadContext.set("CONTACT_CENTER");
 
         reportingGateway.reportChangeMsisdnForSubscriber(subscriptionId, msisdn);
 
         verify(httpClientService).executeSync("url/subscription/changemsisdn?subscriptionId=" + subscriptionId + "&msisdn=" + msisdn, null, Method.POST);
     }
 
     @Test
     public void shouldMakeAssynchronousCallIfSyncCallFailsForCallCenter() {
         String msisdn = "msisdn";
         String subscriptionId = "subscriptionId";
         when(kilkariProperties.getProperty("reporting.service.base.url")).thenReturn("url");
         HttpThreadContext.set("CONTACT_CENTER");
         doThrow(new RuntimeException()).when(httpClientService).executeSync(anyString(), anyObject(), any(Method.class));
 
         reportingGateway.reportChangeMsisdnForSubscriber(subscriptionId, msisdn);
 
         verify(httpClientService).execute("url/subscription/changemsisdn?subscriptionId=" + subscriptionId + "&msisdn=" + msisdn, null, Method.POST);
     }
 
     @Test
     public void shouldInvokeReportingServiceWithGetSubscriberForAGivenSubscriptionId() {
         String subscriptionId = "subscriptionId";
         String url = "url/subscription/subscriber/subscriptionId";
 
         when(kilkariProperties.getProperty("reporting.service.base.url")).thenReturn("url");
 
         when(restTemplate.getForEntity(url, SubscriberResponse.class))
                 .thenReturn(new ResponseEntity(new SubscriberResponse(), HttpStatus.OK));
 
         reportingGateway.getSubscriber(subscriptionId);
     }
 
     @Test
     public void shouldMakeSynchronousCallToReportsForCallCenter() {
         String msisdn = "msisdn";
         String subscriptionId = "subscriptionId";
         when(kilkariProperties.getProperty("reporting.service.base.url")).thenReturn("url");
         HttpThreadContext.set("CONTACT_CENTER");
 
         reportingGateway.reportChangeMsisdnForSubscriber(subscriptionId, msisdn);
 
         verify(httpClientService, never()).execute("url/subscription/changemsisdn?subscriptionId=" + subscriptionId + "&msisdn=" + msisdn, null, Method.POST);
     }
 }
