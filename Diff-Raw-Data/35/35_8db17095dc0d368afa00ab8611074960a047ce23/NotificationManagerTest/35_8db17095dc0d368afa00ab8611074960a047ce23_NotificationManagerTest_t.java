 package uk.me.tom_fitzhenry.motionremote.notification;
 
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 
 @RunWith(MockitoJUnitRunner.class)
 public class NotificationManagerTest {
 	
 	@InjectMocks NotificationManager notificationManager;
 	@Mock AndroidNotifier notifier;
 	
 	@Test
 	public void foo() {
 		notificationManager.process(new MotionDetectedEvent(new DateTime(2012,12,25,14,10,0,0)));
 		verify(notifier).submit("Motion detected!");
 		
 		notificationManager.process(new MotionStoppedEvent(new DateTime(2012,12,25,14,12,0,0)));
		verify(notifier).submit("Motion stopped (Past: 2 mins)");
 	}
 	
 	@Test
 	public void stoppedEventsWithNoCorrespondingStartEventsAreDropped() {
 		notificationManager.process(new MotionStoppedEvent(arbitraryDateTime()));
 		verify(notifier, never()).submit(anyString());
 	}
 	
 	@Test
 	public void multipleIncidentsAreBatched() {
 		notificationManager.process(new MotionDetectedEvent(new DateTime(2012,12,25,14,10,0,0)));
 		notificationManager.process(new MotionStoppedEvent(new DateTime(2012,12,25,14,12,0,0)));
 		verify(notifier, times(2)).submit(anyString());
 		
 		notificationManager.process(new MotionDetectedEvent(new DateTime(2012,12,25,16,10,0,0)));
		verify(notifier).submit("Motion detected! (Past: 2 mins)");
 		notificationManager.process(new MotionStoppedEvent(new DateTime(2012,12,25,16,14,0,0)));
		verify(notifier).submit("Motion stopped (Past: 2 mins, 4 mins)");
 	}
 	
 	private static DateTime arbitraryDateTime() {
 		return new DateTime(2013,1,2,3,4,5,6);
 	}
 
 }
