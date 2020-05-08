 package cours.ulaval.glo4003.domain;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class TimeTest {
 
 	private static final Integer A_MINUTE = 30;
 	private static final Integer A_HOUR = 10;
 	private static final Integer A_EARLIER_HOUR = 8;
 	private static final Integer A_EARLIER_MINUTE = 15;
 	private static final Integer A_LATER_HOUR = 12;
 	private static final Integer A_LATER_MINUTE = 45;
 
 	private Time time;
 
 	@Before
 	public void setUp() {
 		time = new Time(A_HOUR, A_MINUTE);
 	}
 
 	@Test
 	public void canInstantiateTime() {
 		Time time = new Time();
 
 		assertNotNull(time);
 	}
 
 	@Test
 	public void canInstantiateTimeWithHourAndMinute() {
 		assertNotNull(time);
 		assertEquals(A_HOUR, time.getHour());
 		assertEquals(A_MINUTE, time.getMinute());
 	}
 
 	@Test
 	public void canAddHourToTime() {
 		Integer hoursToAdd = 2;
 
 		time.addHours(hoursToAdd);
 
 		assertEquals(A_HOUR + hoursToAdd, (int) time.getHour());
 	}
 
 	@Test
 	public void canDetermineIfATimeIsAfterASpecifiedTime() {
 		Time aEarlierTime = new Time(A_EARLIER_HOUR, A_EARLIER_MINUTE);
 		Time anotherEarlierTime = new Time(A_HOUR, A_EARLIER_MINUTE);
 		Time aLaterTime = new Time(A_LATER_HOUR, A_LATER_MINUTE);
 		Time anotherLaterTime = new Time(A_HOUR, A_LATER_MINUTE);
 
 		assertTrue(time.after(aEarlierTime));
 		assertTrue(time.after(anotherEarlierTime));
 		assertFalse(time.after(aLaterTime));
 		assertFalse(time.after(anotherLaterTime));
 	}
 
 	@Test
 	public void canDetermineIfATimeIsBeforeASpecifiedTime() {
 		Time aEarlierTime = new Time(A_EARLIER_HOUR, A_EARLIER_MINUTE);
 		Time anotherEarlierTime = new Time(A_HOUR, A_EARLIER_MINUTE);
 		Time aLaterTime = new Time(A_LATER_HOUR, A_LATER_MINUTE);
 		Time anotherLaterTime = new Time(A_HOUR, A_LATER_MINUTE);
 
 		assertTrue(time.before(aLaterTime));
 		assertTrue(time.before(anotherLaterTime));
 		assertFalse(time.before(aEarlierTime));
 		assertFalse(time.before(anotherEarlierTime));
 	}
 
 	@Test
 	public void canConvertTimeToString() {
 		Time time = new Time(A_HOUR, A_MINUTE);
 
		assertEquals(A_HOUR + ":" + A_MINUTE, time.toString());
 	}
 }
