 package net.hisme.masaki.kyoani.test.models;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 import junit.framework.TestCase;
 
 import net.hisme.masaki.kyoani.models.AnimeCalendar;
 
 public class AnimeCalendarTest extends TestCase {
   private AnimeCalendar cal = null;
 
   public void setUp() {
     /* 2000/2/2 4:30 */
     cal = new AnimeCalendar(2000, 2, 2, 4, 30);
   }
 
   public void testHourWhenMidnight() {
     AnimeCalendar cal1 = new AnimeCalendar("28:30");
     AnimeCalendar cal2 = new AnimeCalendar();
     assertEquals(cal1.get(Calendar.DAY_OF_MONTH), cal2.get(Calendar.DAY_OF_MONTH));
     assertEquals(cal1.get(Calendar.YEAR), cal2.get(Calendar.YEAR));
     assertEquals(cal1.get(Calendar.MONTH), cal2.get(Calendar.MONTH));
     assertEquals(28, cal1.get(Calendar.HOUR_OF_DAY));
     assertEquals(30, cal1.get(Calendar.MINUTE));
   }
 
   public void testInit() {
    assertEquals(new GregorianCalendar(2010, 1, 1), new AnimeCalendar(2010, 1, 1));
    assertEquals(new GregorianCalendar(2010, 1, 1, 0, 10, 30), new AnimeCalendar(2010, 1, 1, 0, 10, 30));
    assertEquals(new GregorianCalendar(), new AnimeCalendar());
    assertEquals(new GregorianCalendar(Locale.JAPAN), new AnimeCalendar(Locale.JAPAN));
   }
 
   public void testIsMidnight() {
     assertTrue(cal.isMidnight());
     assertFalse(new AnimeCalendar(2000, 2, 2, 5, 10).isMidnight());
   }
 
   public void testGet() {
     assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
     assertEquals(28, cal.get(Calendar.HOUR_OF_DAY));
 
     AnimeCalendar cal2 = new AnimeCalendar(2000, 2, 2, 5, 10);
     assertEquals(2, cal2.get(Calendar.DAY_OF_MONTH));
     assertEquals(5, cal2.get(Calendar.HOUR_OF_DAY));
 
     AnimeCalendar cal3 = new AnimeCalendar(2000, 2, 1, 28, 30);
     assertEquals(1, cal3.get(Calendar.DAY_OF_MONTH));
     assertEquals(28, cal3.get(Calendar.HOUR_OF_DAY));
   }
 
   public void testGetTimeString() {
     assertEquals("28:30", cal.getTimeString());
   }
 }
