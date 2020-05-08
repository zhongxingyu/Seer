 package simulation.beefs.model;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import simulation.beefs.event.machine.UserActivity;
 import simulation.beefs.event.machine.WakeOnLan;
 import simulation.beefs.model.Machine.State;
 import simulation.beefs.util.ObservableEventSourceMultiplexer;
 import core.EventScheduler;
 import core.EventSource;
 import core.Time;
 import core.Time.Unit;
 
 /**
  * 
  * @author Patrick Maia
  *
  */
 public class MachineTransitionsFromGoingSleepTest {
 	
 	private final Time TO_SLEEP_TIMEOUT = new Time(15*60, Unit.SECONDS);
 	private final Time TRANSITION_DURATION = new Time(2500, Unit.MILLISECONDS);
 	private final Time ONE_MINUTE = new Time(60, Unit.SECONDS);
 	private final Time ONE_SECOND = new Time(1, Unit.SECONDS);
 
 	private ObservableEventSourceMultiplexer eventsMultiplexer;
 
 	private Machine machineGoingSleep;
 	
 	@Before
 	public void setup() {
 		eventsMultiplexer = new ObservableEventSourceMultiplexer(new EventSource[0]);
 		EventScheduler.setup(Time.GENESIS, Time.THE_FINAL_JUDGMENT, eventsMultiplexer);
 
 		machineGoingSleep = new Machine("jurupoca", TO_SLEEP_TIMEOUT, TRANSITION_DURATION);
 		machineGoingSleep.setIdle(Time.GENESIS, TO_SLEEP_TIMEOUT.plus(ONE_MINUTE));
 		machineGoingSleep.setSleeping(TO_SLEEP_TIMEOUT, ONE_MINUTE);
 		assertEquals(State.GOING_SLEEP, machineGoingSleep.getState());
 	}
 
 	@Test(expected=IllegalStateException.class)
 	public void testTransitionToIdle() {
 		machineGoingSleep.setIdle(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION), ONE_MINUTE);
 	}
 
 	@Test
 	public void testTransitionToSleep() {
 		machineGoingSleep.setSleeping(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION), ONE_MINUTE);
 		assertEquals(State.SLEEPING, machineGoingSleep.getState());
 	}
 
 	@Test
 	/*
 	 * An activity event arrives before the transition end everytime the TRANSITION_DURATION + TO_SLEEP_TIMEOUT is 
 	 * greater than the original idleness period that put the machine to sleep.
 	 */
 	public void testTransitionToActive() { 
 		Time activityStart = TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION.minus(ONE_SECOND));
 		machineGoingSleep.setActive(activityStart, ONE_MINUTE);
 		Time transitionEnd = TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION);
 		assertEquals(transitionEnd.minus(activityStart), machineGoingSleep.currentDelay());
 		assertEquals(State.GOING_SLEEP, machineGoingSleep.getState());
 		
		UserActivity activityEvent = new UserActivity(machineGoingSleep, transitionEnd, ONE_MINUTE, false);
 		assertTrue(eventsMultiplexer.contains(activityEvent));
 	}
 	
 	@Test
 	public void testTransitionToActiveTwice() {
 		Time activityStart = TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION.minus(ONE_SECOND));
 		machineGoingSleep.setActive(activityStart, ONE_MINUTE);
 		try {
 			machineGoingSleep.setActive(activityStart, ONE_MINUTE);
 			fail();
 		} catch(IllegalStateException e) {
 			// ok
 		}
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testTransitionToActiveInFuture() {
 		Time activityStart = TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION.plus(ONE_SECOND));
 		machineGoingSleep.setActive(activityStart, ONE_MINUTE);
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testTransitionToActiveInPast() {
 		Time activityStart = TO_SLEEP_TIMEOUT.minus(ONE_SECOND);
 		machineGoingSleep.setActive(activityStart, ONE_MINUTE);
 	}
 	
 	@Test
 	public void testWakeOnLan() {
 		machineGoingSleep.wakeOnLan(TO_SLEEP_TIMEOUT.plus(ONE_SECOND));
 		
 		Time transitionEnd = TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION);
 		WakeOnLan wakeOnLan = new WakeOnLan(machineGoingSleep, transitionEnd);
 		assertTrue(eventsMultiplexer.contains(wakeOnLan));
 		
 		machineGoingSleep.wakeOnLan(TO_SLEEP_TIMEOUT.plus(ONE_SECOND));
 		assertEquals(1, eventsMultiplexer.howManyOf(wakeOnLan));
 	}
 }
