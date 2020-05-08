 package org.motechproject.ghana.mtn.billing.service;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ghana.mtn.billing.dto.BillingCycleRequest;
 import org.motechproject.model.CronSchedulableJob;
 import org.motechproject.model.MotechEvent;
 import org.motechproject.scheduler.MotechSchedulerService;
 import org.motechproject.util.DateUtil;
 import org.quartz.CronExpression;
 
 import java.text.ParseException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Map;
 
 import static java.lang.String.format;
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.ghana.mtn.billing.service.BillingScheduler.MONTHLY_BILLING_SCHEDULE_SUBJECT;
 
 public class BillingSchedulerTest {
 
     private BillingScheduler billingScheduler;
     @Mock
     private MotechSchedulerService schedulerService;
 
     @Before
     public void setUp() {
         initMocks(this);
         billingScheduler = new BillingScheduler(schedulerService, "0 0 5 %s *");
     }
 
     @Test
     public void shouldRaiseACronJobWithPlatformSchedulerService() {
         BillingCycleRequest request = mock(BillingCycleRequest.class);
         DateTime cycleStartDate = DateUtil.now();
         Date startDate = cycleStartDate.monthOfYear().addToCopy(1).toDate();
 
         when(request.getMobileNumber()).thenReturn("123");
         when(request.programKey()).thenReturn("program");
         when(request.getCycleStartDate()).thenReturn(cycleStartDate);
 
         billingScheduler.startFor(request);
 
         ArgumentCaptor<CronSchedulableJob> captor = ArgumentCaptor.forClass(CronSchedulableJob.class);
         verify(schedulerService).scheduleJob(captor.capture());
 
         CronSchedulableJob job = captor.getValue();
         MotechEvent motechEvent = job.getMotechEvent();
         Map<String, Object> params = motechEvent.getParameters();
 
         assertEquals(MONTHLY_BILLING_SCHEDULE_SUBJECT, motechEvent.getSubject());
        assertEquals("program.123", params.get(MotechSchedulerService.JOB_ID_KEY));
         assertEquals("123", params.get(BillingScheduler.EXTERNAL_ID_KEY));
         assertEquals("program", params.get(BillingScheduler.PROGRAM_KEY));
         assertEquals(format("0 0 5 %s *", cycleStartDate.getDayOfMonth()), job.getCronExpression());
         assertEquals(startDate, job.getStartTime());
     }
 
     @Test
     public void shouldStopScheduledJobs() {
         BillingCycleRequest request = mock(BillingCycleRequest.class);
         when(request.getMobileNumber()).thenReturn("123");
         when(request.programKey()).thenReturn("program");
 
         billingScheduler.stopFor(request);
 
        verify(schedulerService).unscheduleJob(MONTHLY_BILLING_SCHEDULE_SUBJECT, "program.123");
     }
 
     @Test
     public void shouldCheckIfTheCronWorksWithTheBillingDate() throws ParseException {
         final String expression = "0 0 5 %s * ?";
         String every7th = format(expression, 7);
         final CronExpression cronExpression = new CronExpression(every7th);
 
         assertEquals(date(2011, 7, 7, 5, 0), cronExpression.getNextValidTimeAfter(date(2011, 7, 6, 0, 0)));
         assertEquals(date(2011, 8, 7, 5, 0), cronExpression.getNextValidTimeAfter(date(2011, 7, 7, 5, 0)));
         assertEquals(date(2011, 8, 7, 5, 0), cronExpression.getNextValidTimeAfter(date(2011, 7, 7, 10, 0)));
         assertEquals(date(2011, 10, 7, 5, 0), cronExpression.getNextValidTimeAfter(date(2011, 9, 9, 10, 0)));
 
     }
 
     private Date date(int year, int month, int day, int hour, int min) {
         Calendar calendar = Calendar.getInstance();
         calendar.clear();
         calendar.set(year, month, day, hour, min, 0);
         return calendar.getTime();
     }
 }
