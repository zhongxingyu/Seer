 package org.motechproject.ghana.mtn.process;
 
 import org.joda.time.DateTime;
 import org.motechproject.ghana.mtn.billing.dto.*;
 import org.motechproject.ghana.mtn.billing.service.BillingService;
 import org.motechproject.ghana.mtn.domain.MessageBundle;
 import org.motechproject.ghana.mtn.domain.Subscription;
 import org.motechproject.ghana.mtn.repository.AllSubscriptions;
 import org.motechproject.ghana.mtn.service.SMSService;
 import org.motechproject.ghana.mtn.utils.DateUtils;
 import org.motechproject.util.DateUtil;
 import org.motechproject.valueobjects.WallTimeUnit;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import static java.lang.String.format;
 import static org.motechproject.ghana.mtn.domain.MessageBundle.BILLING_SUCCESS;
 import static org.motechproject.ghana.mtn.domain.MessageBundle.DEFAULTED_BILLING_SUCCESS;
 import static org.motechproject.ghana.mtn.domain.SubscriptionStatus.ACTIVE;
 import static org.motechproject.ghana.mtn.domain.SubscriptionStatus.PAYMENT_DEFAULT;
 import static org.motechproject.ghana.mtn.validation.ValidationError.INSUFFICIENT_FUNDS;
 import static org.motechproject.valueobjects.WallTimeUnit.Day;
 import static org.motechproject.valueobjects.WallTimeUnit.Week;
 
 @Component
 public class BillingServiceMediator extends BaseSubscriptionProcess {
 
     private BillingService billingService;
     private AllSubscriptions allSubscriptions;
     DateUtils dateUtils = new DateUtils();
     public static int DEFAULTED_SUBSCRIPTION_BILLING_HOUR = 5;
     public static int DEFAULTED_DAILY_BILLING_FREQUENCY = 7;
 
 
     @Autowired
     public BillingServiceMediator(SMSService smsService, MessageBundle messageBundle, BillingService billingService, AllSubscriptions allSubscriptions) {
         super(smsService, messageBundle);
         this.billingService = billingService;
         this.allSubscriptions = allSubscriptions;
     }
 
     public void chargeMonthlyFeeAndHandleIfDefaulted(Subscription subscription) {
         BillingServiceResponse<CustomerBill> response = chargeFee(subscription);
         if (response.hasErrors()) {
             sendMessage(subscription, messageFor(response.getValidationErrors()));
             billingService.stopBilling(new BillingCycleRequest(subscription.subscriberNumber(), subscription.getProgramType(), null, null));
             createDefaultedDailyAndWeeklyBillingSchedule(subscription, response);
             updateSubscriptionStatus(subscription);
         } else {
             sendMessage(subscription, format(messageFor(BILLING_SUCCESS), response.getValue().amountChargedWithCurrency()));
         }
     }
 
     public BillingServiceResponse chargeFeeForDefaultedSubscriptionDaily(Subscription subscription) {
         BillingServiceResponse response = chargeFee(subscription);
         if (!response.hasErrors()) {
             stopDefaultedBillingSchedule(subscription, Day);
             stopDefaultedBillingSchedule(subscription, Week);
             startBillingSchedule(subscription);
             allSubscriptions.update(subscription.setStatus(ACTIVE));
             sendMessage(subscription, format(messageFor(DEFAULTED_BILLING_SUCCESS), dateUtils.dayWithOrdinal(DateUtil.today().getDayOfMonth())));
         }
         return response;
     }
 
     public BillingServiceResponse chargeFeeForDefaultedSubscriptionWeekly(Subscription subscription) {
         BillingServiceResponse response = chargeFee(subscription);
         if (!response.hasErrors()) {
             stopDefaultedBillingSchedule(subscription, Week);
             startBillingSchedule(subscription);
             allSubscriptions.update(subscription.setStatus(ACTIVE));
             sendMessage(subscription, format(messageFor(DEFAULTED_BILLING_SUCCESS), dateUtils.dayWithOrdinal(DateUtil.today().getDayOfMonth())));
         }
         return response;
     }
 
     private void startBillingSchedule(Subscription subscription) {
         DateTime nextBillingDate = subscription.billingStartDate(DateUtil.now());
         billingService.startBilling(new BillingCycleRequest(subscription.subscriberNumber(), subscription.getProgramType(), nextBillingDate, 
                 subscription.getSubscriptionEndDate()));
     }
 
     private void stopDefaultedBillingSchedule(Subscription subscription, WallTimeUnit dayOrWeek) {
         billingService.stopDefaultedBillingSchedule(new DefaultedBillingRequest(subscription.subscriberNumber(), subscription.getProgramType(), dayOrWeek));
     }
 
     private void updateSubscriptionStatus(Subscription subscription) {
         subscription.setStatus(PAYMENT_DEFAULT);
         allSubscriptions.update(subscription);
     }
 
     private BillingServiceResponse<CustomerBill> chargeFee(Subscription subscription) {
         return billingService.chargeProgramFee(new BillingServiceRequest(subscription.subscriberNumber(), subscription.getProgramType()));
     }
 
     private void createDefaultedDailyAndWeeklyBillingSchedule(Subscription subscription, BillingServiceResponse response) {
         if (response.getValidationErrors().contains(INSUFFICIENT_FUNDS)) {
             DateTime startDateForDailySchedule = DateUtil.now().dayOfMonth().addToCopy(1).withTimeAtStartOfDay().withHourOfDay(DEFAULTED_SUBSCRIPTION_BILLING_HOUR);
             DateTime endDateForDailySchedule = startDateForDailySchedule.dayOfMonth().addToCopy(DEFAULTED_DAILY_BILLING_FREQUENCY - 1);
 
             DefaultedBillingRequest dailyBillingRequest = createDefaultedSchedule(subscription, Day, startDateForDailySchedule, endDateForDailySchedule);
             billingService.startDefaultedBillingSchedule(dailyBillingRequest);
 
             createWeeklyDefaultBillingScheduleToRunAfterDailySchedule(subscription, endDateForDailySchedule);
         }
     }
 
     private void createWeeklyDefaultBillingScheduleToRunAfterDailySchedule(Subscription subscription, DateTime endDateForDailySchedule) {
        DateTime startDateForWeeklySchedule = endDateForDailySchedule.dayOfMonth().addToCopy(1).withTimeAtStartOfDay().withHourOfDay(DEFAULTED_SUBSCRIPTION_BILLING_HOUR);
         DateTime endDate = subscription.getSubscriptionEndDate().withTimeAtStartOfDay().withHourOfDay(DEFAULTED_SUBSCRIPTION_BILLING_HOUR);
         DefaultedBillingRequest weeklyBillingRequestAfterDailySchedule = createDefaultedSchedule(subscription, Week, startDateForWeeklySchedule, endDate);
         billingService.startDefaultedBillingSchedule(weeklyBillingRequestAfterDailySchedule);
     }
 
     private DefaultedBillingRequest createDefaultedSchedule(Subscription subscription, WallTimeUnit frequency, DateTime startDateForDailySchedule, DateTime endDateForDailySchedule) {
         return new DefaultedBillingRequest(subscription.subscriberNumber(), subscription.getProgramType(),
                 startDateForDailySchedule, frequency, endDateForDailySchedule);
     }
 }
