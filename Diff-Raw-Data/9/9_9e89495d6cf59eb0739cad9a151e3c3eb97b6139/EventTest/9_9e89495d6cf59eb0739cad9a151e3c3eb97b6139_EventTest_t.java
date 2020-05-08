 package fi.helsinki.cs.oato;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 import org.junit.Test;
 import java.util.GregorianCalendar;
 
 public class EventTest {
     @Test
     public void testEquals() {
         Event dentistAppointment = EventFixtures.createDentistAppointment();
         assertThat(dentistAppointment, equalTo(dentistAppointment));
 
         Event equalDentistAppointment = EventFixtures.createDentistAppointment();
         assertThat(equalDentistAppointment, equalTo(dentistAppointment));
 
         Event softwareEngineeringLecture = EventFixtures.createSoftwareEngineeringLecture();
         assertThat(softwareEngineeringLecture, not(equalTo(dentistAppointment)));
     }
 
     @Test
     public void testCompareTo() {
         Event upcomingEvent        = EventFixtures.createUpcomingEvent(EventFixtures.ONE_HOUR);
        Event anotherUpcomingEvent = EventFixtures.createUpcomingEvent(EventFixtures.ONE_DAY);
         
         assertThat(upcomingEvent.compareTo(upcomingEvent), is(0));
         assertThat(upcomingEvent.compareTo(anotherUpcomingEvent), is(-1));
         assertThat(anotherUpcomingEvent.compareTo(upcomingEvent), is(1));
     }
 }
