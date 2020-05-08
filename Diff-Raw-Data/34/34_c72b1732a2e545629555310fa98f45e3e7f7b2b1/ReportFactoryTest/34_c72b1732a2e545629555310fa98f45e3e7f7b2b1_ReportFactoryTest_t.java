 package tests;
 
 /**
  * @author Team TA's
  */
 
 
 import org.junit.* ;
 
 import fi.helsinki.cs.scheduler3000.*;
 import fi.helsinki.cs.scheduler3000.model.Event;
import fi.helsinki.cs.scheduler3000.model.Schedule;
 import fi.helsinki.cs.scheduler3000.model.Weekday;
import fi.helsinki.cs.scheduler3000.model.Weekday.Day;
 import fi.helsinki.cs.scheduler3000.report.Report;
 import fi.helsinki.cs.scheduler3000.report.ReportFactory;
 import junit.framework.TestCase;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class ReportFactoryTest extends TestCase {
 
	Schedule sched;
 	HashMap<String, Object> options;
	final int STARTTIME = 8, ENDTIME = 12;
	final String TITLE = "title", LOCATION = "location"; 
 
 	@Before
 	public void setUp() throws Exception {
 		ArrayList<Weekday.Day> weekdays = new ArrayList<Weekday.Day>();
 		weekdays.add(Weekday.Day.MON);
 		weekdays.add(Weekday.Day.THU);
 
		sched = new Schedule(weekdays);
		sched.addEvent( new Event(Weekday.Day.MON, TITLE, LOCATION, STARTTIME, ENDTIME));
 		
 		options = new HashMap<String, Object>();
 
 	}
 
 
 	@After
 	public void tearDown() throws Exception {
 		sched = null;
 		options = null;
 	}
 
 	@Test
 	public void testDayReport(){
 		// make options
 		options.put("day", Weekday.Day.MON);
 		
 		Report r = ReportFactory.makeReport(ReportFactory.ReportType.DAY, sched, options);
 		assertTrue(r.toString() != null);
		assertTrue(r.toString().contains(Weekday.Day.MON.getName()));
 	}
 
 	@Test
 	public void testDayReportWithEmptyOptions(){
 		// options are empty after setUp
 		
 		Report r = ReportFactory.makeReport(ReportFactory.ReportType.DAY, sched, options);
 		assertNull(r.toString());
 	}
 
 	
 	@Test
 	public void testDayReportWithFalseOptions(){
 		options.put("day", Weekday.Day.SAT); // no Saturday in schedule
 		
 		Report r = ReportFactory.makeReport(ReportFactory.ReportType.DAY, sched, options);
 		assertNull(r.toString());
 	}
 
 	@Test
 	public void testWeekReport(){
 		// make options
 		ArrayList<Weekday.Day> daylist = new ArrayList<Weekday.Day>();
 		daylist.add(Weekday.Day.MON);
 		daylist.add(Weekday.Day.THU);
 		options.put("days", daylist);
 		
 		Report r = ReportFactory.makeReport(ReportFactory.ReportType.WEEK, sched, options);
 		assertTrue(r.toString() != null);
		assertTrue(r.toString().contains( Day.MON.toString() ));
		assertTrue(r.toString().contains( Day.THU.toString() ));
 	}
 	
 	@Test
 	public void testWeekReportWithFalseOptions(){
 		// make options
 		ArrayList<Weekday.Day> daylist = new ArrayList<Weekday.Day>();
 		daylist.add(Weekday.Day.MON);
 		daylist.add(Weekday.Day.THU);
 		daylist.add(Weekday.Day.FRI);
 		options.put("days", daylist);
 		
 		Report r = ReportFactory.makeReport(ReportFactory.ReportType.WEEK, sched, options);
 		assertNull(r.toString());
 	}
 	
 	@Test
 	public void testWeekReportWithNoOptions(){
 		// options are empty after setUp
 		
 		Report r = ReportFactory.makeReport(ReportFactory.ReportType.WEEK, sched, options);
 		assertNull(r.toString());
 	}
 	
 	@Test
 	public void testFullRaport(){
 		Report r = ReportFactory.makeReport(ReportFactory.ReportType.FULL, sched, options);
 		assertTrue(r.toString() != null);
 		
 		// full report holds label for Monday
		assertTrue(r.toString().contains( Day.MON.getName() )); 
 		
 		// full report holds single event
		assertTrue(r.toString().contains(""+ STARTTIME));
		assertTrue(r.toString().contains("" + ENDTIME));
 		assertTrue(r.toString().contains(TITLE));
 		assertTrue(r.toString().contains(LOCATION));
 		
 		// full report holds label for Thursday
		assertTrue(r.toString().contains( Day.THU.getName() ));
 		
 	}
 	
 	
 	
 	/**
 	 * Cannot test the last line (default value, which is set to 'null'
 	 * as there are only three values set in enum that the program accepts:
 	 * DAY, WEEK and FULL 
 	@Test
 	public void testIfNull(){
 		Report r4 = ReportFactory.makeReport(null, sched, mapper);
 		assertNull(r4);
 	}
 	*/
 
 }
 	
