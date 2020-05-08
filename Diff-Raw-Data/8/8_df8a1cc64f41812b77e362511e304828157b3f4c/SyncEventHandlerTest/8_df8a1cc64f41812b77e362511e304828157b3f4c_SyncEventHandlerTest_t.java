 package org.motechproject.ananya.referencedata.flw.handlers;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Captor;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.motechproject.ananya.referencedata.flw.domain.*;
 import org.motechproject.ananya.referencedata.flw.service.FrontLineWorkerService;
 import org.motechproject.ananya.referencedata.flw.service.JsonHttpClient;
 import org.motechproject.model.MotechEvent;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.annotation.ExpectedException;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:applicationContext-flw.xml")
 public class SyncEventHandlerTest {
     @Mock
     private FrontLineWorkerService frontLineWorkerService;
     @Mock
     private JsonHttpClient jsonHttpClient;
     @Autowired
     private Properties clientServicesProperties;
     @Autowired
     private Properties referenceDataProperties;
     private SyncEventHandler syncEventHandler;
     @Captor
     private ArgumentCaptor<Map<String, String>> headerCaptor;
 
     @Before
     public void setUp() {
         initMocks(this);
         syncEventHandler = new SyncEventHandler(frontLineWorkerService, jsonHttpClient, clientServicesProperties, referenceDataProperties);
     }
 
     @Test
     public void shouldInvokeClientServiceWithFrontLineWorkerData() throws IOException {
         HashMap<String, Object> parameters = new HashMap<String, Object>();
         String msisdn = "1234";
         Location location = new Location("district1", "block1", "panchayat1");
         DateTime lastModified = DateTime.now();
         location.setLastModified(lastModified);
         FrontLineWorker frontLineWorker = new FrontLineWorker(Long.parseLong(msisdn), "name1", Designation.ANM, location);
         frontLineWorker.setLastModified(lastModified);
         ArrayList<FrontLineWorker> frontLineWorkers = new ArrayList<FrontLineWorker>();
         frontLineWorkers.add(frontLineWorker);
         when(frontLineWorkerService.getAllByMsisdn(Long.parseLong(msisdn))).thenReturn(frontLineWorkers);
         when(jsonHttpClient.post(Matchers.<String>any(), Matchers.<Object>any(), Matchers.<Map<String, String>>any())).thenReturn(new JsonHttpClient.Response(HttpServletResponse.SC_OK, "{Created/Updated FLW record}"));
         parameters.put("0", Long.parseLong(msisdn));
 
         syncEventHandler.handleSyncFrontLineWorker(new MotechEvent(SyncEventKeys.FRONT_LINE_WORKER_DATA_MESSAGE, parameters));
 
         ArgumentCaptor<FrontLineWorkerContract> captor = ArgumentCaptor.forClass(FrontLineWorkerContract.class);
        verify(jsonHttpClient).post(eq("http://localhost:8080/ananya/flw"), captor.capture(), headerCaptor.capture());
         FrontLineWorkerContract value = captor.getValue();
         assertEquals(msisdn, value.getMsisdn());
         Map<String, String> header = headerCaptor.getValue();
         assertEquals("1234", header.get("APIKey"));
     }
 
     @Test
     @ExpectedException(IllegalStateException.class)
     public void shouldInvokeClientServiceWithFrontLineWorkerDataAndThrowExceptionBasedOnTheResponse() throws IOException {
         HashMap<String, Object> parameters = new HashMap<String, Object>();
         String msisdn = "1234";
         Location location = new Location("district1", "block1", "panchayat1");
         DateTime lastModified = DateTime.now();
         location.setLastModified(lastModified);
         FrontLineWorker frontLineWorker = new FrontLineWorker(Long.parseLong(msisdn), "name1", Designation.ANM, location);
         frontLineWorker.setLastModified(lastModified);
         ArrayList<FrontLineWorker> frontLineWorkers = new ArrayList<FrontLineWorker>();
         frontLineWorkers.add(frontLineWorker);
         when(frontLineWorkerService.getAllByMsisdn(Long.parseLong(msisdn))).thenReturn(frontLineWorkers);
         when(jsonHttpClient.post(Matchers.<String>any(), Matchers.<Object>any(), Matchers.<Map<String, String>>any())).thenReturn(new JsonHttpClient.Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "some"));
         parameters.put("0", Long.parseLong(msisdn));
 
         syncEventHandler.handleSyncFrontLineWorker(new MotechEvent(SyncEventKeys.FRONT_LINE_WORKER_DATA_MESSAGE, parameters));
     }
 
     @Test
     @ExpectedException(IllegalStateException.class)
     public void shouldInvokeClientServiceWithFrontLineWorkerDataAndThrowExceptionBasedOnTheResponseMessage() throws IOException {
         HashMap<String, Object> parameters = new HashMap<String, Object>();
         String msisdn = "1234";
         Location location = new Location("district1", "block1", "panchayat1");
         DateTime lastModified = DateTime.now();
         location.setLastModified(lastModified);
         FrontLineWorker frontLineWorker = new FrontLineWorker(Long.parseLong(msisdn), "name1", Designation.ANM, location);
         frontLineWorker.setLastModified(lastModified);
         ArrayList<FrontLineWorker> frontLineWorkers = new ArrayList<FrontLineWorker>();
         frontLineWorkers.add(frontLineWorker);
         when(frontLineWorkerService.getAllByMsisdn(Long.parseLong(msisdn))).thenReturn(frontLineWorkers);
         when(jsonHttpClient.post(Matchers.<String>any(), Matchers.<Object>any(), Matchers.<Map<String, String>>any())).thenReturn(new JsonHttpClient.Response(HttpServletResponse.SC_OK, "{[Invalid msisdn]}"));
         parameters.put("0", Long.parseLong(msisdn));
 
         syncEventHandler.handleSyncFrontLineWorker(new MotechEvent(SyncEventKeys.FRONT_LINE_WORKER_DATA_MESSAGE, parameters));
     }
 
     @Test
     public void shouldNotInvokeClientServiceWithFrontLineWorkerDataIfMsisdnIsNull() throws IOException {
         HashMap<String, Object> parameters = new HashMap<String, Object>();
         Location location = new Location("district1", "block1", "panchayat1");
         DateTime lastModified = DateTime.now();
         location.setLastModified(lastModified);
         FrontLineWorker frontLineWorker = new FrontLineWorker(null, "name1", Designation.ANM, location);
         frontLineWorker.setLastModified(lastModified);
         ArrayList<FrontLineWorker> frontLineWorkers = new ArrayList<FrontLineWorker>();
         frontLineWorkers.add(frontLineWorker);
         when(frontLineWorkerService.getAllByMsisdn(null)).thenReturn(frontLineWorkers);
         when(jsonHttpClient.post(Matchers.<String>any(), Matchers.<Object>any(), Matchers.<Map<String, String>>any())).thenReturn(new JsonHttpClient.Response(HttpServletResponse.SC_OK, "{Created/Updated FLW record}"));
         parameters.put("0", null);
 
         syncEventHandler.handleSyncFrontLineWorker(new MotechEvent(SyncEventKeys.FRONT_LINE_WORKER_DATA_MESSAGE, parameters));
 
        verify(jsonHttpClient, never()).post(eq("http://localhost:8080/ananya/flw"), any(), any(HashMap.class));
     }
 
     @Test
     public void shouldNotInvokeClientServiceWithFrontLineWorkerDataIfThereAreMultipleFLWsWithTheSameMSISDN() throws IOException {
         HashMap<String, Object> parameters = new HashMap<String, Object>();
         String msisdn = "1234";
         Location location = new Location("district1", "block1", "panchayat1");
         DateTime lastModified = DateTime.now();
         location.setLastModified(lastModified);
         FrontLineWorker frontLineWorker = new FrontLineWorker(Long.parseLong(msisdn), "name1", Designation.ANM, location);
         frontLineWorker.setLastModified(lastModified);
         ArrayList<FrontLineWorker> frontLineWorkers = new ArrayList<FrontLineWorker>();
         frontLineWorkers.add(frontLineWorker);
         frontLineWorkers.add(frontLineWorker);
         when(frontLineWorkerService.getAllByMsisdn(Long.parseLong(msisdn))).thenReturn(frontLineWorkers);
         when(jsonHttpClient.post(Matchers.<String>any(), Matchers.<Object>any(), Matchers.<Map<String, String>>any())).thenReturn(new JsonHttpClient.Response(HttpServletResponse.SC_ACCEPTED, "{success}"));
         parameters.put("0", Long.parseLong(msisdn));
 
         syncEventHandler.handleSyncFrontLineWorker(new MotechEvent(SyncEventKeys.FRONT_LINE_WORKER_DATA_MESSAGE, parameters));
 
        verify(jsonHttpClient, never()).post(eq("http://localhost:8080/ananya/flw"), any(), any(HashMap.class));
     }
 
     @Test
     @ExpectedException(value = IllegalStateException.class)
     public void shouldCryWhenResponseStatusCodeIs500() throws IOException {
         HashMap<String, Object> parameters = new HashMap<String, Object>();
         String msisdn = "1234";
         Location location = new Location("district1", "block1", "panchayat1");
         DateTime lastModified = DateTime.now();
         location.setLastModified(lastModified);
         FrontLineWorker frontLineWorker = new FrontLineWorker(Long.parseLong(msisdn), "name1", Designation.ANM, location);
         frontLineWorker.setLastModified(lastModified);
         ArrayList<FrontLineWorker> frontLineWorkers = new ArrayList<FrontLineWorker>();
         frontLineWorkers.add(frontLineWorker);
         when(frontLineWorkerService.getAllByMsisdn(Long.parseLong(msisdn))).thenReturn(frontLineWorkers);
         when(jsonHttpClient.post(Matchers.<String>any(), Matchers.<Object>any(), Matchers.<Map<String, String>>any())).thenReturn(new JsonHttpClient.Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ExceptionTrace"));
         parameters.put("0", Long.parseLong(msisdn));
 
         syncEventHandler.handleSyncFrontLineWorker(new MotechEvent(SyncEventKeys.FRONT_LINE_WORKER_DATA_MESSAGE, parameters));
     }
 }
