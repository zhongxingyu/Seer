 package tests;
 
 import models.ISystemInformation;
 import models.SystemInformation;
 import models.TimeTracker;
 import models.database.Database;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import play.test.UnitTest;
 import tests.mocks.SystemInformationMock;
 
 public class TimeTrackerTest extends UnitTest {
 
 	private ISystemInformation savedSysInfo;
 	private TimeTracker t;
 	private SystemInformationMock sys;
 
 	@Before
 	public void setUp() throws Exception {
		Database.clear();
 		savedSysInfo = SystemInformation.get();
 		sys = new SystemInformationMock();
 		sys.year(2010).month(9).day(1).hour(0).minute(0).second(0);
 		t = TimeTracker.getTimeTracker();
		t.injectMockedStartTime(sys.now());
		SystemInformation.mockWith(sys);
 		sys.year(2010).month(12).day(1).hour(0).minute(0).second(0);
 
 	}
 
 	@Test
 	public void shouldCalculateNinetyTwoDays() {
		sys.year(2010).month(12).day(1).hour(0).minute(0).second(0);
 		int days = t.getDays();
 		assertEquals(92, days);
 	}
 
 	@Test
 	public void shouldCalculateOneDay() {
 		sys.year(2010).month(9).day(2).hour(0).minute(0).second(0);
 		int days = t.getDays();
 		assertEquals(1, days);
 	}
 
 	@Test
 	public void shouldCalculateZeroDays() {
 		sys.year(2010).month(9).day(1).hour(0).minute(0).second(0);
 		int days = t.getDays();
 		assertEquals(0, days);
 	}
 
 	@Test
 	public void shouldCalculateOneWeek() {
 		sys.year(2010).month(9).day(2).hour(0).minute(0).second(0);
 		int weeks = t.getWeeks();
 		assertEquals(1, weeks);
 	}
 
 	@Test
 	public void shouldCalculateZeroWeeks() {
 		sys.year(2010).month(9).day(1).hour(0).minute(0).second(0);
 		int weeks = t.getWeeks();
 		assertEquals(0, weeks);
 	}
 
 	@Test
 	public void shouldCalculateFourteenWeeks() {
 		int weeks = t.getWeeks();
 		assertEquals(14, weeks);
 	}
 
 	@Test
 	public void shouldCalculateThreeMonths() {
 		int months = t.getMonths();
 		assertEquals(3, months);
 	}
 
 	@Test
 	public void shouldCalculateOneMonth() {
 		sys.year(2010).month(10).day(1).hour(0).minute(0).second(0);
 		int months = t.getMonths();
 		assertEquals(1, months);
 	}
 
 	@Test
 	public void shouldCalculateZeroMonths() {
 		sys.year(2010).month(9).day(1).hour(0).minute(0).second(0);
 		int months = t.getMonths();
 		assertEquals(0, months);
 	}
 
 	@After
 	public void tearDown() {
 		Database.clear();
 		SystemInformation.mockWith(savedSysInfo);
 	}
 }
