 package org.motechproject.ghana.mtn.service;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentMatcher;
 import org.mockito.Mock;
 import org.motechproject.ghana.mtn.domain.IProgramType;
 import org.motechproject.ghana.mtn.domain.ProgramType;
 import org.motechproject.ghana.mtn.domain.Subscriber;
 import org.motechproject.ghana.mtn.domain.Subscription;
 import org.motechproject.ghana.mtn.domain.builder.ProgramTypeBuilder;
 import org.motechproject.ghana.mtn.domain.vo.Day;
 import org.motechproject.ghana.mtn.domain.vo.Week;
 import org.motechproject.ghana.mtn.process.BillingCycleProcess;
 import org.motechproject.ghana.mtn.process.CampaignProcess;
 import org.motechproject.ghana.mtn.process.PersistenceProcess;
 import org.motechproject.ghana.mtn.process.ValidationProcess;
 import org.motechproject.ghana.mtn.repository.AllSubscriptions;
 import org.motechproject.ghana.mtn.vo.Money;
 
 import static junit.framework.Assert.assertEquals;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class SubscriptionServiceImplTest {
 
     private SubscriptionServiceImpl service;
     @Mock
     private ValidationProcess validation;
     @Mock
     private BillingCycleProcess billing;
     @Mock
     private PersistenceProcess persistence;
     @Mock
     private CampaignProcess campaign;
     @Mock
     private AllSubscriptions allSubscriptions;
 
     public final ProgramType childCarePregnancyType = new ProgramTypeBuilder().withFee(new Money(0.60D)).withMinWeek(1).withMaxWeek(52).withProgramName("Child Care").withShortCode("C").withShortCode("c").build();
 
     @Before
     public void setUp() {
         initMocks(this);
         service = new SubscriptionServiceImpl(allSubscriptions, validation, billing, persistence, campaign);
     }
 
     @Test
     public void shouldCallAllProcessInvolvedOnNoErrorsDuringStartSubscription() {
         Subscription subscription = mock(Subscription.class);
 
         when(validation.startFor(subscription)).thenReturn(true);
         when(billing.startFor(subscription)).thenReturn(true);
         when(persistence.startFor(subscription)).thenReturn(true);
         when(campaign.startFor(subscription)).thenReturn(true);
 
         service.start(subscription);
 
         verify(validation).startFor(subscription);
         verify(billing).startFor(subscription);
         verify(persistence).startFor(subscription);
         verify(campaign).startFor(subscription);
     }
 
     @Test
     public void shouldCallAllProcessInvolvedOnNoErrorsDuringStopSubscription() {
         Subscription subscription = mock(Subscription.class);
 
         when(billing.stopExpired(subscription)).thenReturn(true);
         when(campaign.stopExpired(subscription)).thenReturn(true);
         when(persistence.stopExpired(subscription)).thenReturn(true);
 
         service.stopExpired(subscription);
 
         verify(billing).stopExpired(subscription);
         verify(campaign).stopExpired(subscription);
         verify(persistence).stopExpired(subscription);
     }
 
     @Test
     public void shouldInvokeAllProcessInvolvedInRollOverProcess() {
         Subscription source = mock(Subscription.class);
         Subscription target = mock(Subscription.class);
 
         when(validation.rollOver(source, target)).thenReturn(true);
         when(billing.rollOver(source, target)).thenReturn(true);
         when(campaign.rollOver(source, target)).thenReturn(true);
         when(persistence.rollOver(source, target)).thenReturn(true);
 
         service.rollOver(source, target);
 
         verify(validation).rollOver(source, target);
         verify(billing).rollOver(source, target);
         verify(campaign).rollOver(source, target);
         verify(persistence).rollOver(source, target);
 
     }
 
     @Test
     public void shouldInvokeAllProcessInvolvedInStopProcessByUser() {
         String subscriberNumber = "9500012345";
         IProgramType programType = childCarePregnancyType;
         Subscription subscription = mock(Subscription.class);
 
         when(validation.validateSubscriptionToStop(subscriberNumber, programType)).thenReturn(subscription);
         when(billing.stopByUser(subscription)).thenReturn(true);
         when(campaign.stopByUser(subscription)).thenReturn(true);
         when(persistence.stopByUser(subscription)).thenReturn(true);
 
         service.stopByUser(subscriberNumber, programType);
 
         verify(validation).validateSubscriptionToStop(subscriberNumber, programType);
         verify(billing).stopByUser(subscription);
         verify(campaign).stopByUser(subscription);
         verify(persistence).stopByUser(subscription);
 
     }
 
     @Test
     public void shouldNotInvokeAllStopProcessByUserIfValidationFailsInFindingSubscriptionToStop() {
         String subscriberNumber = "9500012345";
         IProgramType programType = childCarePregnancyType;
         Subscription subscription = mock(Subscription.class);
 
         when(validation.validateSubscriptionToStop(subscriberNumber, programType)).thenReturn(null);
         when(billing.stopByUser(subscription)).thenReturn(true);
         when(campaign.stopByUser(subscription)).thenReturn(true);
         when(persistence.stopByUser(subscription)).thenReturn(true);
 
         service.stopByUser(subscriberNumber, programType);
 
         verify(validation).validateSubscriptionToStop(subscriberNumber, programType);
         verify(billing, never()).stopByUser(subscription);
         verify(campaign, never()).stopByUser(subscription);
         verify(persistence, never()).stopByUser(subscription);
     }
 
     @Test
     public void shouldFindSubscriptionByMobileNumberUsingRepository() {
         String subscriberNumber = "123";
         String program = "program";
         Subscription subscription = new Subscription();
         when(allSubscriptions.findBy(subscriberNumber, program)).thenReturn(subscription);
 
         Subscription returned = service.findBy(subscriberNumber, program);
 
         assertEquals(subscription, returned);
         verify(allSubscriptions).findBy(subscriberNumber, program);
     }
 
     @Test
     public void shouldGetAllActiveSubscriptions() {
         String subscriberNumber = "9844321234";
         service.activeSubscriptions(subscriberNumber);
         verify(allSubscriptions).getAllActiveSubscriptionsForSubscriber(subscriberNumber);
     }
 
 
     @Test
     public void shouldDoNothingIfSubscriptionIsNotCompletedDuringProcessAfterEvent() {
         Subscription subscription = mock(Subscription.class);
         when(subscription.isCompleted()).thenReturn(false);
 
         service.processAfterEvent(subscription);
         verify(subscription, never()).canRollOff();
     }
 
     @Test
     public void shouldStopIfSubscriptionIsCompletedAndCannotRollOffDuringProcessAfterEvent() {
         Subscription subscription = mock(Subscription.class);
        ProgramType programType = mock(ProgramType.class);

         when(subscription.isCompleted()).thenReturn(true);
         when(subscription.canRollOff()).thenReturn(false);
        when(subscription.getProgramType()).thenReturn(programType);
         when(billing.stopExpired(subscription)).thenReturn(true);
         when(campaign.stopExpired(subscription)).thenReturn(true);
         when(persistence.stopExpired(subscription)).thenReturn(true);
 
         service.processAfterEvent(subscription);
 
         verify(billing).stopExpired(subscription);
         verify(campaign).stopExpired(subscription);
         verify(persistence).stopExpired(subscription);
     }
 
     @Test
     public void shouldRollOverDuringDuringProcessAfterEvent() {
         Subscription source = mock(Subscription.class);
         Subscriber subscriber = mock(Subscriber.class);
         ProgramType programType = mock(ProgramType.class);
         Week week = new Week(4);
 
         when(source.getSubscriber()).thenReturn(subscriber);
         when(source.getProgramType()).thenReturn(programType);
         when(source.currentWeek()).thenReturn(week);
         when(source.currentDay()).thenReturn(Day.SUNDAY);
         when(source.isCompleted()).thenReturn(true);
         when(source.canRollOff()).thenReturn(true);
         when(programType.getRollOverProgramType()).thenReturn(programType);
 
         when(validation.rollOver(any(Subscription.class), any(Subscription.class))).thenReturn(true);
         when(billing.rollOver(any(Subscription.class), any(Subscription.class))).thenReturn(true);
         when(campaign.rollOver(any(Subscription.class), any(Subscription.class))).thenReturn(true);
         when(persistence.rollOver(any(Subscription.class), any(Subscription.class))).thenReturn(true);
 
         service.processAfterEvent(source);
 
         SubscriptionMatcher matcher = new SubscriptionMatcher(subscriber, programType, week, Day.SUNDAY);
         verify(validation).rollOver(eq(source), argThat(matcher));
         verify(billing).rollOver(eq(source), argThat(matcher));
         verify(campaign).rollOver(eq(source), argThat(matcher));
         verify(persistence).rollOver(eq(source), argThat(matcher));
 
     }
 
     private class SubscriptionMatcher extends ArgumentMatcher<Subscription> {
 
         private Subscriber subscriber;
         private ProgramType programType;
         private Week week;
         private Day day;
 
         private SubscriptionMatcher(Subscriber subscriber, ProgramType programType, Week week, Day day) {
             this.subscriber = subscriber;
             this.programType = programType;
             this.week = week;
             this.day = day;
         }
 
         @Override
         public boolean matches(Object o) {
             Subscription subscription = (Subscription) o;
             return subscriber.equals(subscription.getSubscriber())
                     && programType.equals(subscription.getProgramType())
                     && week.equals(subscription.getStartWeekAndDay().getWeek())
                     && day.equals(subscription.getStartWeekAndDay().getDay());
         }
     }
 
 
 }
