 package simulation.beefs.model;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
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
 public class MachineTransitionsFromWakingUpToIdleTest {
 	
 	private final Time TO_SLEEP_TIMEOUT = new Time(15*60, Unit.SECONDS);
 	private final Time TRANSITION_DURATION = new Time(2500, Unit.MILLISECONDS);
 	private final Time ONE_MINUTE = new Time(60, Unit.SECONDS);
 	private final Time ONE_SECOND = new Time(1, Unit.SECONDS);
 
 	private ObservableEventSourceMultiplexer eventsMultiplexer;
 	
 	private Machine machineWakingUpToIdle;
 
 	@Before
 	public void setup() {
 		eventsMultiplexer = new ObservableEventSourceMultiplexer(new EventSource[0]);
 		EventScheduler.setup(Time.GENESIS, Time.THE_FINAL_JUDGMENT, eventsMultiplexer);
 
 		machineWakingUpToIdle = new Machine("pepino", TO_SLEEP_TIMEOUT, TRANSITION_DURATION);
 		machineWakingUpToIdle.setIdle(Time.GENESIS, TO_SLEEP_TIMEOUT.plus(ONE_MINUTE));
 		machineWakingUpToIdle.setSleeping(TO_SLEEP_TIMEOUT, ONE_MINUTE);
 		machineWakingUpToIdle.setSleeping(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION), 
 				ONE_MINUTE.minus(TRANSITION_DURATION));
 		machineWakingUpToIdle.wakeOnLan(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION));
 		assertEquals(State.WAKING_UP, machineWakingUpToIdle.getState());
 	}
 	
 	@Test(expected=IllegalStateException.class)
 	public void testTransitionToActive() {
 		machineWakingUpToIdle.setActive(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION.times(2)), ONE_MINUTE);
 	}
 	
 	@Test(expected=IllegalStateException.class)
 	public void testTransitionToSleep() {
 		machineWakingUpToIdle.setSleeping(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION.times(2)), ONE_MINUTE);
 	}
 	
 	@Test
 	public void testWakeOnLan() {
 		int before = eventsMultiplexer.queueSize();
 		machineWakingUpToIdle.wakeOnLan(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION).plus(ONE_SECOND)); // this must be innocuous
 		assertEquals(before, eventsMultiplexer.queueSize());
 		assertEquals(State.WAKING_UP, machineWakingUpToIdle.getState());
 	}
 	
 	@Test
 	public void testTransitionToIdle() {
 		machineWakingUpToIdle.setIdle(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION.times(2)), ONE_MINUTE);
 		assertEquals(State.IDLE, machineWakingUpToIdle.getState());
		assertEquals(TRANSITION_DURATION, machineWakingUpToIdle.currentDelay());
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testTransitionToIdleBeforeExpected() {
 		machineWakingUpToIdle.setIdle(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION.plus(ONE_SECOND)), ONE_MINUTE);
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testTransitionToIdleAfterExpected() {
 		machineWakingUpToIdle.setIdle(TO_SLEEP_TIMEOUT.plus(TRANSITION_DURATION.times(2).plus(ONE_SECOND)), ONE_MINUTE);
 	}
 
 }
