 package models;
 
 import static org.joda.time.DateTimeUtils.setCurrentMillisFixed;
 import static org.joda.time.DateTimeUtils.setCurrentMillisSystem;
 
 import java.util.Date;
 
import org.fest.assertions.Assertions;
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
import play.test.UnitTest;
 import assertion.PlayAssertions;
 
 public class RaceTest extends UnitTest {
 	
 	@Rule
 	public ExpectedException expectedException = ExpectedException.none();
 	
 	private Race race;
 	
 	@Before
 	public void setup() {
 		race =  new Race();
 		setCurrentMillisFixed(new Date().getTime());
 	}
 	
 	@After
 	public void resetTime() {
 		setCurrentMillisSystem();
 	}
 	
 	@Test
 	public void startTimeInFuture_TrueInFuture() {
 		Race race = new RaceBuilder()
 			.withStartTime(new DateTime().plus(100).toDate())
 			.build();
 		
 		PlayAssertions.assertThat(race.startTimeInFuture()).isTrue();
 	}
 	
 	@Test
 	public void startTimeInFuture_FalseInPresent() {
 		Race race = new RaceBuilder()
 			.withStartTime(new DateTime().toDate())
 			.build();
 	
 		PlayAssertions.assertThat(race.startTimeInFuture()).isFalse();
 	}
 	
 	@Test
 	public void startTimeInFuture_FalseInPast() {
 		Race race = new RaceBuilder()
 			.withStartTime(new DateTime().minus(100).toDate())
 			.build();
 
 		PlayAssertions.assertThat(race.startTimeInFuture()).isFalse();
 	}
 	
 	@Test
 	public void hasRunShouldReturnTrueIfWinnerDetermined() {
 		Race race = new RaceBuilder()
 			.withStarted()
 			.build();
 		
 		Assertions.assertThat(race.hasRun()).isTrue();
 	}
 	
 	@Test
 	public void hasRunShouldReturnFalseIfNoWinnerYet() {
 		Race race = new RaceBuilder().build();
 		Assertions.assertThat(race.hasRun()).isFalse();
 	}
 	
 	@Test
 	public void shouldBeAbleToDetermineAvailableSlots_whenNoHorsesEnteredTheRace() {
 		int availableSlots = race.getAvailableSlots();
 		
 		Assertions.assertThat(availableSlots).isEqualTo(Race.MAX_AVAILABLE_SLOTS);
 	}
 	
 	@Test
 	public void shouldBeAbleToDetermineAvailableSlots_whenHorsesEnteredTheRace() {
 		race.enter(new Horse("horse 1"));
 		
 		int availableSlots = race.getAvailableSlots();
 		
 		Assertions.assertThat(availableSlots).isEqualTo(7);
 	}
 	
 	@Test
 	public void shouldBeAbleToDetermineAvailableSlots_AllSlotsFull() {
 		enterHorses(8);
 		
 		int availableSlots = race.getAvailableSlots();
 		
 		Assertions.assertThat(availableSlots).isEqualTo(0);
 	}
 
 	@Test
 	public void shouldBeAbleToEnterAHorse(){
 		Horse horse = new Horse("horse 1");
 		race.enter(horse);
 		
 		Assertions.assertThat(race.getEnteredHorses()).containsOnly(horse);
 	}
 	
 	@Test
 	public void shouldNotBeAbleToEnterMoreHorsesThanTheMaximumAvailableSlots(){
 		enterHorses(8);
 		
 		expectedException.expect(IllegalStateException.class);
 		expectedException.expectMessage(Race.MAX_AVAILABLE_SLOTS_EXCEEDED);
 		
 		race.enter(new Horse("horse 9"));
 	}
 
 	@Test
 	public void startRaceShouldBeAbleToDetermineAWinner() {
 		Assertions.assertThat(race.winner).isNull();
 		
 		enterHorses(3);
 		race.start();
 		
 		Assertions.assertThat(race.winner).isIn(race.getEnteredHorses());
 	}
 	
 	@Test
 	public void shouldNotBeAbleToStartRaceWhenLessThanMinimumAmountOfHorsesAreEntered(){
 		enterHorses(Race.MIN_HORSES_ENTERED_TO_START_RACE - 1);
 		
 		expectedException.expect(IllegalStateException.class);
 		expectedException.expectMessage(Race.LESS_THAN_MIN_AMOUNT_HORSES_ENTERED_TO_START_RACE);
 		
 		race.start();
 	}
 	
 	@Test
 	public void shouldBeAbleToDetermineIfRaceIsReadyToStart() {
 		enterHorses(Race.MIN_HORSES_ENTERED_TO_START_RACE);
 		
 		Assertions.assertThat(race.readyToStart()).isTrue();
 	}
 	
 	@Test
 	public void shouldBeAbleToDetermineIfRaceIsReadyToStart_lessThanTheAmountNeeded() {
 		enterHorses(Race.MIN_HORSES_ENTERED_TO_START_RACE - 1);
 		
 		Assertions.assertThat(race.readyToStart()).isFalse();
 	}
 	
 	@Test
 	public void shouldBeAbleToDetermineIfAHorseCanBeEntered() {
 		enterHorses(Race.MAX_AVAILABLE_SLOTS - 1);
 		
 		Assertions.assertThat(race.canEnterHorse()).isTrue();
 	}
 	
 	@Test
 	public void shouldBeAbleToDetermineIfAHorseCanBeEntered_maxAvailableSlotsExceeded() {
 		enterHorses(Race.MAX_AVAILABLE_SLOTS);
 		
 		Assertions.assertThat(race.canEnterHorse()).isFalse();
 	}
 	
 	private void enterHorses(int count) {
 		for (int i = 0; i < count; i++) {
 			race.enter(new Horse("horse " + i + 1));
 		}
 	}
 }
 
