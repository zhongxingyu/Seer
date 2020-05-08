 package org.motechproject.ananya.kilkari.functional.test;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.motechproject.ananya.kilkari.functional.test.builder.SubscriptionDataBuilder;
 import org.motechproject.ananya.kilkari.functional.test.domain.SubscriptionData;
 import org.motechproject.ananya.kilkari.functional.test.utils.BaseFunctionalTest;
 import org.motechproject.ananya.kilkari.subscription.repository.KilkariPropertiesData;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import static org.motechproject.ananya.kilkari.functional.test.Actions.*;
 
 public class CustomerNotAroundThePhoneFunctionalTest extends BaseFunctionalTest {
 
     @Autowired
     private KilkariPropertiesData kilkariProperties;
 
     @Test
     public void shouldRetryDeliveryOfMessagesWhenTheSubscriberDoesNotReceiveMessagesOrCallIsNotMade() throws Exception {
         System.out.println("Now running shouldRetryDeliveryOfMessagesWhenTheSubscriberDoesNotReceiveMessagesOrCallIsNotMade");
 
         int scheduleDeltaDays = kilkariProperties.getCampaignScheduleDeltaDays();
         int deltaMinutes = kilkariProperties.getCampaignScheduleDeltaMinutes();
         DateTime futureDateForFirstCampaignAlert = DateTime.now().plusDays(scheduleDeltaDays).plusMinutes(deltaMinutes + 1);
         DateTime futureDateForFirstDayFirstSlot = futureDateForFirstCampaignAlert.plusDays(1).withHourOfDay(13).withMinuteOfHour(30);
         DateTime futureDateForFirstDaySecondSlot = futureDateForFirstCampaignAlert.plusDays(1).withHourOfDay(18).withMinuteOfHour(30);
         DateTime futureDateForSecondDayFirstSlot = futureDateForFirstDaySecondSlot.plusDays(1).withHourOfDay(13).withMinuteOfHour(30);
        DateTime futureDateForSecondDaySecondSlot = futureDateForSecondDayFirstSlot.withHourOfDay(18).withMinuteOfHour(30);
         String week1 = "WEEK1";
 
         SubscriptionData subscriptionData = new SubscriptionDataBuilder().withDefaults().build();
 
         when(callCenter).subscribes(subscriptionData);
         and(subscriptionManager).activates(subscriptionData);
         and(time).isMovedToFuture(futureDateForFirstCampaignAlert);
         then(user).messageIsReady(subscriptionData, week1);
 
         and(time).isMovedToFuture(futureDateForFirstDayFirstSlot);
         then(user).messageWasDeliveredDuringFirstSlot(subscriptionData, week1);
 
         when(obd).reportsUserDidNotPickUpTheCall(subscriptionData, week1);
         and(user).resetOnMobileOBDVerifier();
         and(time).isMovedToFuture(futureDateForFirstDaySecondSlot);
         then(user).messageWasDeliveredDuringSecondSlot(subscriptionData, week1);
 
         when(obd).doesNotCallTheUser(subscriptionData, week1);
         and(user).resetOnMobileOBDVerifier();
        and(time).isMovedToFuture(futureDateForSecondDaySecondSlot);
         then(user).messageWasDeliveredDuringFirstSlot(subscriptionData, week1);
     }
 }
