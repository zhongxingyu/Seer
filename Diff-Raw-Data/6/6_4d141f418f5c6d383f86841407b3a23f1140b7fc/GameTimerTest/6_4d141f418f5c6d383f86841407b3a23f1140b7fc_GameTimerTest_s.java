 package dk.kleistsvendsen;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.robolectric.RobolectricTestRunner;
 
 @RunWith(RobolectricTestRunner.class)
 public class GameTimerTest {
     private GameTimer gameTimer;
 
     @Mock
     private ITicSource ticSource;
 
     @Before
     public void setup() {
         MockitoAnnotations.initMocks(this);
         TestGuiceModule module = new TestGuiceModule();
         module.addBinding(ITicSource.class, ticSource);
         TestGuiceModule.setUp(this, module);
         gameTimer = new GameTimer();
     }
 
     @After
     public void teardown() {
         TestGuiceModule.tearDown();
     }
 
     @Test
     public void testCallsTickOnStartTimer() {
         when(ticSource.tic()).thenReturn(0L);
         gameTimer.startTimer();
     }
 
     @Test
     public void testStartThenTimeLeft() {
         when(ticSource.tic()).thenReturn(0L);
         gameTimer.startTimer();
         when(ticSource.tic()).thenReturn(1000L);
        assertThat(gameTimer.timeLeft(), equalTo(1000L));
     }
 }
