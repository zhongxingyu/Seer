 package vcu.blademonitor.simpleMonitoringServices;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class MetricObjectTest {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@Test
 	public final void testMetricObject() {
 		MetricObject myMetric = new MetricObject();
 
 		assertTrue(myMetric.getName() == null);
 		assertTrue(myMetric.getValue() == 0d);
 		assertTrue(myMetric.getTime() == null);
 	}
 
 	@Test
 	public final void testMetricObjectString() {
 		MetricObject myMetric = new MetricObject("Metric");
 
 		assertTrue(myMetric.getName().equals("Metric"));
 		assertTrue(myMetric.getValue() == 0d);
 		assertTrue(myMetric.getTime() == null);
 	}
 
 	@Test
 	public final void testMetricObjectStringDoubleCalendar() {
 		Calendar myCalendar = Calendar.getInstance();
 		Date myDate = myCalendar.getTime();
 		MetricObject myMetric = new MetricObject("Metric", 0.05, myCalendar);
 
 		assertTrue(myMetric.getTime().compareTo(myDate) == 0);
 		assertTrue(myMetric.getName().equals("Metric"));
 		assertTrue(myMetric.getValue() == 0.05);
 	}
 
 	@Test
 	public final void testMetricObjectStringDoubleDate() {
 		Date myDate = Calendar.getInstance().getTime();
 		MetricObject myMetric = new MetricObject("Metric", 0.05, myDate);
 
 		assertTrue(myMetric.getTime() == myDate);
 		assertTrue(myMetric.getName().equals("Metric"));
 		assertTrue(myMetric.getValue() == 0.05);
 	}
 
 	@Test
 	public final void testSetters() {
 		MetricObject myMetric = new MetricObject("Metric", 0.05, (Date) null);
 		myMetric.setName("Bob");
 		myMetric.setValue(10);
 		Date myDate = Calendar.getInstance().getTime();
 		myMetric.setTime(myDate);
 
 		assertTrue(myMetric.getName().equals("Bob"));
 		assertTrue(myMetric.getValue() == 10);
 		assertTrue(myMetric.getTime() == myDate);
 	}
 
 	@Test
 	public final void testSetTimeCalendar() {
		MetricObject myMetric = new MetricObject("Metric", 0.05, Calendar
				.getInstance().getTime());
 		Calendar myCalendar = Calendar.getInstance();
 		myMetric.setTime(myCalendar);
 
 		assertTrue(myMetric.getTime() instanceof Date);
 	}
 
 	@Test
 	public final void testResetTime() {
 		MetricObject myMetric = new MetricObject();
 		myMetric.resetTime();
 
 		assertFalse(myMetric.getTime() == null);
 	}
 }
