 package ca.hilikus.jrobocom;
 
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Mockito.atLeast;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 import static org.testng.Assert.fail;
 
 import java.lang.reflect.Method;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import ca.hilikus.jrobocom.player.Bank;
 import ca.hilikus.jrobocom.player.InstructionSet;
 import ca.hilikus.jrobocom.robots.Robot;
import ca.hilikus.jrobocom.robots.api.RobotStatusLocal;
 import ca.hilikus.jrobocom.timing.MasterClock;
 import ch.qos.logback.classic.Level;
 
 /**
  * Tests the robots, but not the control class
  * 
  * @author hilikus
  * 
  */
 @Test
 public class RobotTest {
 
     private static final Logger log = LoggerFactory.getLogger(RobotTest.class);
 
     /**
      * Changes TU debug level to trace
      */
     @BeforeClass
     public void setUpOnce() {
 	ch.qos.logback.classic.Logger TULog = (ch.qos.logback.classic.Logger) LoggerFactory
 		.getLogger(Robot.class);
 	TULog.setLevel(Level.TRACE);
     }
 
     /**
      * Initializes each test
      * 
      * @param met test about to be called
      */
     @BeforeMethod
     public void setUp(Method met) {
 	log.info("\n====== Starting " + met.getName() + " ======");
 
     }
 
     /**
      * Tests creation of subsequent robots
      */
     @Test(dependsOnMethods = { "testFirstRobotCreation" }, groups = "init")
     public void testOtherRobotsCreation() {
 
 	World mockWorld = mock(World.class);
 	when(mockWorld.validateTeamId(anyInt())).thenReturn(true);
 	Bank[] dummyBanks = new Bank[3];
 	Robot eve = new Robot(mockWorld, dummyBanks);
 
 	int banks = 3;
 
 	InstructionSet set = InstructionSet.ADVANCED;
 	Robot TU = new Robot(set, banks, false, eve);
 	assertFalse(TU.getState().isMobile());
 	assertEquals(TU.getBanksCount(), banks);
 	assertEquals(TU.getState().getAge(), 0);
 	assertEquals(TU.getState().getInstructionSet(), set);
 	assertEquals(TU.getSerialNumber(), eve.getSerialNumber() + 1, "Serial number increases every time");
 	assertEquals(TU.getState().getGeneration(), eve.getState().getGeneration() + 1);
 	assertTrue(TU.isAlive());
 
 	Robot TU2 = new Robot(set, 8, true, eve);
 	assertEquals(TU2.getState().getGeneration(), eve.getState().getGeneration() + 1);
 	assertEquals(TU2.getSerialNumber(), TU.getSerialNumber() + 1);
 
     }
 
     /**
      * Tests creation of first robot
      */
     @Test(groups = "init")
     public void testFirstRobotCreation() {
 	World mockWorld = mock(World.class);
 	when(mockWorld.validateTeamId(anyInt())).thenReturn(false, true);
 
 	final int BANK_COUNT = 3;
 	Bank[] dummyBanks = new Bank[BANK_COUNT];
 	Robot TU = new Robot(mockWorld, dummyBanks);
 
 	// check generation and serial #?
 	verify(mockWorld, atLeast(2)).validateTeamId(anyInt());
 
 	assertFalse(TU.getState().isMobile(), "First robot should not be mobile");
 	assertEquals(TU.getBanksCount(), BANK_COUNT, "First robot has all banks provided");
 	assertEquals(TU.getState().getAge(), 0, "Age at construction is 0");
 	assertEquals(TU.getState().getInstructionSet(), InstructionSet.SUPER,
 		"First robot should have all instruction sets");
 	assertEquals(TU.getSerialNumber(), 0, "Serial number of first robot should be 0");
 	assertTrue(TU.isAlive(), "New robot is alive");
     }
 
     /**
      * Tests an infertile robot trying to create
      */
     @Test(expectedExceptions = IllegalArgumentException.class, dependsOnGroups = { "init.*" })
     public void testInvalidCreation() {
 	Robot mockParent = mock(Robot.class);
	RobotStatusLocal mockStatus = mock(RobotStatusLocal.class);
	when(mockStatus.getInstructionSet()).thenReturn(InstructionSet.ADVANCED);
	when(mockParent.getState()).thenReturn(mockStatus);
 
 	@SuppressWarnings("unused")
 	Robot TU = new Robot(InstructionSet.BASIC, 3, true, mockParent);
 	fail("Should not have created");
     }
 
     /**
      * Tests the jump to bank 0 after a bank finished execution
      */
     @Test(dependsOnMethods = { "testFirstRobotCreation" })
     public void testReboot() {
 	World mockWorld = mock(World.class);
 	when(mockWorld.validateTeamId(anyInt())).thenReturn(true);
 	MasterClock mockClock = mock(MasterClock.class);
 	when(mockWorld.getClock()).thenReturn(mockClock);
 
 	mockClock.start(true);
 
 	Bank[] dummyBanks = new Bank[3];
 	Bank first = new Bank() {
 	    int repeat = 0;
 
 	    @Override
 	    public void run() {
 		if (repeat < 1) {
 		    control.changeBank(2);
 		    
 		} else {
 		    control.die();
 		}
 		repeat++;
 	    }
 	};
 
 	first = spy(first);
 	dummyBanks[0] = first;
 
 	Bank second = mock(Bank.class);
 	dummyBanks[2] = second;
 
 	Robot TU = new Robot(mockWorld, dummyBanks);
 	mockClock.addListener(TU.getSerialNumber());
 	TU.run();
 
 	/*InOrder interleaved seems to not work
 	InOrder banksOrder = inOrder(first, second);
 	banksOrder.verify(first, atLeast(1)).run();
 	banksOrder.verify(second).run();
 	banksOrder.verify(first).run();
 	*/
 	
 	verify(first, times(2)).run();
 	verify(second).run();
     }
 
     /**
      * Tests running a robot that has a null 0 bank
      */
     @Test(dependsOnMethods = { "testFirstRobotCreation" })
     public void testDataHunger() {
 	World mockWorld = mock(World.class);
 	when(mockWorld.validateTeamId(anyInt())).thenReturn(true);
 
 	Bank[] dummyBanks = new Bank[3];
 	Robot TU = new Robot(mockWorld, dummyBanks);
 
 	TU.run();
 
 	verify(mockWorld).remove(TU);
     }
 
     /**
      * 
      */
     @Test(dependsOnMethods = { "testFirstRobotCreation" })
     public void testDie() {
 	World mockWorld = mock(World.class);
 	when(mockWorld.validateTeamId(anyInt())).thenReturn(true);
 
 	Bank[] dummyBanks = new Bank[3];
 	Robot TU = new Robot(mockWorld, dummyBanks);
 
 	TU.die("test die");
 
 	verify(mockWorld).remove(TU);
     }
 
 }
