 package nl.surfnet.bod.web.base;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import nl.surfnet.bod.web.view.ReportIntervalView;
 
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeUtils;
 import org.joda.time.Interval;
 import org.junit.Test;
 
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
 
 public class AbstractReportControllerTest {
 
   private final TestReportController subject = new TestReportController();
 
   @Test
   public void shouldHaveIntervalsUtilNow() {
     final DateTime currentMonth = DateMidnight.now().toDateTime();
     final List<ReportIntervalView> reportIntervals = subject.determineReportIntervals();
 
     assertThat(reportIntervals, hasSize(TestReportController.AMOUNT_OF_REPORT_PERIODS));
 
     Interval interval;
     DateTime firstDay;
     for (int i = 1; i < TestReportController.AMOUNT_OF_REPORT_PERIODS; i++) {
       // Skip the first item, seperate test
       interval = reportIntervals.get(i).getInterval();
       firstDay = currentMonth.withDayOfMonth(1).minusMonths(i).toDateTime();
 
       assertThat("Month start: " + i, interval.getStart(), is(firstDay.withDayOfMonth(1)));
       assertThat("Month end: " + i, interval.getEnd(), is(firstDay.dayOfMonth().withMaximumValue()));
 
       assertThat("Month id: " + i, String.valueOf(reportIntervals.get(i).getId()), is(String
           .valueOf(firstDay.getYear())
           + (firstDay.getMonthOfYear() < 10 ? "0" : "") + String.valueOf(firstDay.getMonthOfYear())));
     }
   }
 
   @Test
   public void shouldHaveFirstIntervalUntilNow() {
     final DateTime currentMonth = DateMidnight.now().toDateTime();
     final List<ReportIntervalView> reportIntervals = subject.determineReportIntervals();
 
     assertThat(reportIntervals.get(0).getInterval().getStart(), is(currentMonth.withDayOfMonth(1)));
 
     // Last interval should end on current day
     assertThat(reportIntervals.get(0).getInterval().getEnd(), is(currentMonth.withDayOfMonth(DateTime.now()
         .getDayOfMonth())));
 
     String id = String.valueOf(currentMonth.getYear()) + (currentMonth.getMonthOfYear() < 10 ? "0" : "");
     id += String.valueOf(currentMonth.getMonthOfYear());
     assertThat(String.valueOf(reportIntervals.get(0).getId()), is(id));
   }
 
   @Test
   public void shouldHaveFixedIntervals() {
     final DateTime january2012 = DateMidnight.now().withYear(2012).withMonthOfYear(1).withDayOfMonth(10).toDateTime();
     final DateTime august2012 = january2012.withMonthOfYear(8);
 
     try {
       DateTimeUtils.setCurrentMillisFixed(august2012.getMillis());
 
       final List<ReportIntervalView> reportIntervals = subject.determineReportIntervals();
 
       assertThat(reportIntervals, hasSize(TestReportController.AMOUNT_OF_REPORT_PERIODS));
 
       ReportIntervalView firstReportView = reportIntervals.get(reportIntervals.size() - 1);
       assertThat(firstReportView.getInterval().getStart(), is(january2012.withDayOfMonth(1)));
       assertThat(firstReportView.getInterval().getEnd(), is(january2012.dayOfMonth().withMaximumValue()));
       assertThat(firstReportView.getId(), is(201201));
      assertTrue(firstReportView.getLabel().equalsIgnoreCase("2012 jan"));
 
       ReportIntervalView lastReportView = reportIntervals.get(0);
       assertThat(lastReportView.getInterval().getStart(), is(august2012.withDayOfMonth(1)));
       assertThat(lastReportView.getInterval().getEnd(), is(august2012));
      assertThat(lastReportView.getId(), is(201208));      
      assertTrue(lastReportView.getLabel().equalsIgnoreCase("2012 aug - now"));
     }
     finally {
       DateTimeUtils.currentTimeMillis();
     }
 
   }
 
   /**
    * Since we test an abstract controller, extend it here in order to
    * instantiate it.
    * 
    */
   private class TestReportController extends AbstractReportController {
 
     @Override
     protected String getPageUrl() {
       return "test";
     }
 
     @Override
     protected List<String> getAdminGroups() {
       return new ArrayList<>();
     }
 
   }
 
 }
