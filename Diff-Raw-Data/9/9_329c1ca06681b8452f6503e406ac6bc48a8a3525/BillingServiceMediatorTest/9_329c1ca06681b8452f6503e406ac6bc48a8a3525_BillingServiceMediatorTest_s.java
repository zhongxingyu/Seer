 package org.motechproject.ghana.mtn.process;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.motechproject.ghana.mtn.billing.dto.*;
 import org.motechproject.ghana.mtn.billing.service.BillingService;
 import org.motechproject.ghana.mtn.domain.*;
 import org.motechproject.ghana.mtn.domain.builder.ProgramTypeBuilder;
 import org.motechproject.ghana.mtn.domain.builder.SubscriptionBuilder;
 import org.motechproject.ghana.mtn.domain.dto.SMSServiceRequest;
 import org.motechproject.ghana.mtn.domain.vo.Day;
 import org.motechproject.ghana.mtn.domain.vo.Week;
 import org.motechproject.ghana.mtn.domain.vo.WeekAndDay;
 import org.motechproject.ghana.mtn.repository.AllSubscriptions;
 import org.motechproject.ghana.mtn.service.SMSService;
 import org.motechproject.ghana.mtn.utils.DateUtils;
 import org.motechproject.ghana.mtn.vo.Money;
 import org.motechproject.util.DateUtil;
 import org.motechproject.valueobjects.WallTimeUnit;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import java.util.Arrays;
 
 import static java.lang.String.format;
 import static junit.framework.Assert.assertEquals;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.ghana.mtn.domain.SubscriptionStatus.PAYMENT_DEFAULT;
 import static org.motechproject.ghana.mtn.process.BillingServiceMediator.DEFAULTED_SUBSCRIPTION_BILLING_HOUR;
 import static org.motechproject.ghana.mtn.validation.ValidationError.INSUFFICIENT_FUNDS;
 import static org.motechproject.util.DateUtil.newDate;
 import static org.motechproject.valueobjects.WallTimeUnit.Week;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(DateUtil.class)
 public class BillingServiceMediatorTest {
     private BillingServiceMediator billingServiceMediator;
     private ProgramType programType = new ProgramTypeBuilder().withMaxWeek(35).withMinWeek(5).withShortCode("P").withProgramName("Pregnancy").build();
     @Mock
     private SMSService smsService;
     @Mock
     private BillingService billingService;
     @Mock
     private MessageBundle messageBundle;
     @Mock
     private AllSubscriptions allSubscriptions;
 
     DateUtils dateUtils = new DateUtils();
 
     @Before
     public void setUp() {
         initMocks(this);
         billingServiceMediator = new BillingServiceMediator(smsService, messageBundle, billingService, allSubscriptions);
     }
 
     @Test
     public void shouldUseBillingServiceToChargeFeeAndSendSMSAndStartDefaultedBillSchedule_InCaseOfErrors() {
         String mobileNumber = "123";
         String errorMsg = "error";
         Subscription subscription = subscription(mobileNumber, DateTime.now(), new Week(1), programType);
         CustomerBill customerBill = new CustomerBill("message", new Money(12d));
         BillingServiceResponse<CustomerBill> response = new BillingServiceResponse<CustomerBill>(customerBill);
         response.addError(INSUFFICIENT_FUNDS);
 
         when(billingService.chargeProgramFee(any(BillingServiceRequest.class))).thenReturn(response);
         when(messageBundle.get(Arrays.asList(INSUFFICIENT_FUNDS))).thenReturn(errorMsg);
 
         billingServiceMediator.chargeMonthlyFeeAndHandleIfDefaulted(subscription);
 
         assertSmsRequest(mobileNumber, errorMsg);
         assertStopBillingRequest(new BillingCycleRequest(mobileNumber, programType, null, null));
 
         DateTime now = DateUtil.now().withTimeAtStartOfDay();
         ArgumentCaptor<DefaultedBillingRequest> defaultedBillingRequestCaptor = ArgumentCaptor.forClass(DefaultedBillingRequest.class);
         verify(billingService, times(2)).startDefaultedBillingSchedule(defaultedBillingRequestCaptor.capture());
         DefaultedBillingRequest dailyDefaultedBillingRequest = defaultedBillingRequestCaptor.getAllValues().get(0);
         assertDefaultBillingRequest(
                 new DefaultedBillingRequest(mobileNumber, programType, now.dayOfMonth().addToCopy(1).withTimeAtStartOfDay().withHourOfDay(DEFAULTED_SUBSCRIPTION_BILLING_HOUR),
                 WallTimeUnit.Day, now.dayOfMonth().addToCopy(7).withTimeAtStartOfDay().withHourOfDay(DEFAULTED_SUBSCRIPTION_BILLING_HOUR)), dailyDefaultedBillingRequest);
 
         DefaultedBillingRequest weeklyDefaultedBillingRequest = defaultedBillingRequestCaptor.getAllValues().get(1);
         assertDefaultBillingRequest(
                new DefaultedBillingRequest(mobileNumber, programType, now.dayOfMonth().addToCopy(7 + 1).withTimeAtStartOfDay().withHourOfDay(DEFAULTED_SUBSCRIPTION_BILLING_HOUR), Week,
                 subscription.getSubscriptionEndDate().withTimeAtStartOfDay().withHourOfDay(DEFAULTED_SUBSCRIPTION_BILLING_HOUR)), weeklyDefaultedBillingRequest);
 
         verify(allSubscriptions).update(subscription);
         assertThat(subscription.getStatus(), is(PAYMENT_DEFAULT));
     }
 
     @Test
     public void shouldTryToChargeDefaultedSubscriptionDailyAnd_StopDailyAndWeeklyDefaultedJobAndStartBillingSchedule_IfBillingIsSuccessful() {
 
         String mobileNumber = "123";
         Subscription subscription = subscription(mobileNumber, DateTime.now(), new Week(1), programType).setStatus(PAYMENT_DEFAULT);
         String defaultSuccessMsg = "Billing success. Will be charge in %s of every month for " + programType.getProgramName();
 
         DateTime now = newDate(2011, 11, 29).toDateTimeAtCurrentTime();
         mockCurrentDate(now);
         when(billingService.chargeProgramFee(Matchers.<BillingServiceRequest>any())).thenReturn(new BillingServiceResponse());
         when(messageBundle.get(MessageBundle.DEFAULTED_BILLING_SUCCESS)).thenReturn(defaultSuccessMsg);
         billingServiceMediator.chargeFeeForDefaultedSubscriptionDaily(subscription);
 
         verify(billingService).chargeProgramFee(Matchers.<BillingServiceRequest>any());
         verifyStartBilling(mobileNumber, programType, newDateTime(2011, 12, 1));
 
         ArgumentCaptor<DefaultedBillingRequest> defaultedBillingRequestCaptor = ArgumentCaptor.forClass(DefaultedBillingRequest.class);
         verify(billingService, times(2)).stopDefaultedBillingSchedule(defaultedBillingRequestCaptor.capture());
         assertDefaultBillingRequest(
                 new DefaultedBillingRequest(mobileNumber, programType, WallTimeUnit.Day), defaultedBillingRequestCaptor.getAllValues().get(0));
         assertDefaultBillingRequest(
                 new DefaultedBillingRequest(mobileNumber, programType, WallTimeUnit.Week), defaultedBillingRequestCaptor.getAllValues().get(1));
         verify(allSubscriptions).update(subscription);
         assertSmsRequest(mobileNumber, format(defaultSuccessMsg, dateUtils.dayWithOrdinal(DateUtil.today().getDayOfMonth())));
         assertThat(subscription.getStatus(), is(SubscriptionStatus.ACTIVE));
     }
 
     @Test
     public void shouldTryToChargeDefaultedSubscriptionDailyAnd_DonotStopDailyAndWeeklyDefaultedJobAndDonotStartBillingSchedule_IfBillingIsNotSuccessful() {
 
         Subscription subscription = subscription("123", DateTime.now(), new Week(1), programType).setStatus(PAYMENT_DEFAULT);
         when(billingService.chargeProgramFee(Matchers.<BillingServiceRequest>any())).thenReturn(new BillingServiceResponse().addError(INSUFFICIENT_FUNDS));
         billingServiceMediator.chargeFeeForDefaultedSubscriptionDaily(subscription);
 
         verify(billingService).chargeProgramFee(Matchers.<BillingServiceRequest>any());
         verify(billingService, never()).startBilling(Matchers.<BillingCycleRequest>any());
         verify(allSubscriptions, never()).update(subscription);
         assertThat(subscription.getStatus(), is(PAYMENT_DEFAULT));
         verify(billingService, never()).stopDefaultedBillingSchedule(Matchers.<DefaultedBillingRequest>any());
         verifyZeroInteractions(smsService);
     }
     
     @Test
     public void shouldTryToChargeDefaultedSubscriptionWeekyAnd_StopWeeklyDefaultedJobAndStartBillingSchedule_IfBillingIsSuccessful() {
         String mobileNumber = "123";
         Subscription subscription = subscription(mobileNumber, DateTime.now(), new Week(1), programType).setStatus(PAYMENT_DEFAULT);
         String defaultSuccessMsg = "Billing success. Will be charge in %s of every month for " + programType.getProgramName();
 
         when(billingService.chargeProgramFee(Matchers.<BillingServiceRequest>any())).thenReturn(new BillingServiceResponse());
         when(messageBundle.get(MessageBundle.DEFAULTED_BILLING_SUCCESS)).thenReturn(defaultSuccessMsg);
 
         billingServiceMediator.chargeFeeForDefaultedSubscriptionWeekly(subscription);
 
         verify(billingService).chargeProgramFee(Matchers.<BillingServiceRequest>any());
         verifyStartBilling(mobileNumber, programType, DateUtil.now().withTimeAtStartOfDay());
         ArgumentCaptor<DefaultedBillingRequest> defaultedBillingRequestCaptor = ArgumentCaptor.forClass(DefaultedBillingRequest.class);
         verify(billingService).stopDefaultedBillingSchedule(defaultedBillingRequestCaptor.capture());
         assertDefaultBillingRequest(
                 new DefaultedBillingRequest(mobileNumber, programType, Week), defaultedBillingRequestCaptor.getValue());
         verify(allSubscriptions).update(subscription);
         assertSmsRequest(mobileNumber, format(defaultSuccessMsg, dateUtils.dayWithOrdinal(DateUtil.today().getDayOfMonth())));
         assertThat(subscription.getStatus(), is(SubscriptionStatus.ACTIVE));
     }
 
     private void verifyStartBilling(String subscriberNumber, IProgramType programType, DateTime cycleStartDate) {
         ArgumentCaptor<BillingCycleRequest> billingRequestCaptor = ArgumentCaptor.forClass(BillingCycleRequest.class);
 
         verify(billingService).startBilling(billingRequestCaptor.capture());
         BillingCycleRequest value = billingRequestCaptor.getValue();
         assertEquals(subscriberNumber, value.getMobileNumber());
         assertEquals(cycleStartDate, value.getCycleStartDate());
         assertEquals(programType, value.getProgramType());
     }
 
     @Test
     public void shouldTryToChargeDefaultedSubscriptionWeeklyAnd_DonotStopWeeklyDefaultedJobAndDonotStartBillingSchedule_IfBillingIsNotSuccessful() {
 
         Subscription subscription = subscription("123", DateTime.now(), new Week(1), programType).setStatus(PAYMENT_DEFAULT);
         when(billingService.chargeProgramFee(Matchers.<BillingServiceRequest>any())).thenReturn(new BillingServiceResponse().addError(INSUFFICIENT_FUNDS));
         billingServiceMediator.chargeFeeForDefaultedSubscriptionWeekly(subscription);
 
         verify(billingService).chargeProgramFee(Matchers.<BillingServiceRequest>any());
         verify(billingService, never()).startBilling(Matchers.<BillingCycleRequest>any());
         verify(allSubscriptions, never()).update(subscription);
         assertThat(subscription.getStatus(), is(PAYMENT_DEFAULT));
         verify(billingService, never()).stopDefaultedBillingSchedule(Matchers.<DefaultedBillingRequest>any());
         verifyZeroInteractions(smsService);
     }
 
     private void mockCurrentDate(DateTime date) {
         PowerMockito.spy(DateUtil.class);
         Mockito.when(DateUtil.now()).thenReturn(date);
         Mockito.when(DateUtil.today()).thenReturn(date.toLocalDate());
     }
 
     private void assertSmsRequest(String mobileNumber, String errorMsg) {
         ArgumentCaptor<SMSServiceRequest> captor = ArgumentCaptor.forClass(SMSServiceRequest.class);
         verify(smsService).send(captor.capture());
         SMSServiceRequest captured = captor.getValue();
         assertEquals(errorMsg, captured.getMessage());
         assertEquals(mobileNumber, captured.getMobileNumber());
     }
 
     private void assertStopBillingRequest(BillingCycleRequest expected) {
 
         ArgumentCaptor<BillingCycleRequest> billingCycleCaptor = ArgumentCaptor.forClass(BillingCycleRequest.class);
         verify(billingService).stopBilling(billingCycleCaptor.capture());
 
         BillingCycleRequest actual = billingCycleCaptor.getValue();
         assertEquals(expected.getCycleStartDate(), actual.getCycleStartDate());
         assertEquals(expected.getMobileNumber(), actual.getMobileNumber());
         assertEquals(expected.getProgramType(), actual.getProgramType());
     }
 
     private void assertDefaultBillingRequest(DefaultedBillingRequest expected, DefaultedBillingRequest actual) {
         assertEquals(expected.getFrequency(), actual.getFrequency());
         assertEquals(expected.getCycleStartDate(), actual.getCycleStartDate());
         assertEquals(expected.getCycleEndDate(), actual.getCycleEndDate());
         assertEquals(expected.getMobileNumber(), actual.getMobileNumber());
         assertEquals(expected.getProgramType(), actual.getProgramType());
     }
 
     private Subscription subscription(String mobileNumber, DateTime registeredDate, Week startWeek, ProgramType program) {
         Subscription subscription = new SubscriptionBuilder().withRegistrationDate(registeredDate).withStartWeekAndDay(new WeekAndDay(startWeek, Day.MONDAY))
                 .withStatus(SubscriptionStatus.ACTIVE).withSubscriber(new Subscriber(mobileNumber))
                 .withType(program).build();
         subscription.updateCycleInfo();
         return subscription;
     }
 
     public static DateTime newDateTime(int day, int month, int year) {
         return DateUtil.newDateTime(new LocalDate(day, month, year), 0, 0, 0).withTimeAtStartOfDay();
     }
 }
