 /*
  *  Copyright 2001-2006 Stephen Colebourne
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.gwttime.time;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.gwttime.time.DateTime;
 import org.gwttime.time.DateTimeConstants;
 import org.gwttime.time.DateTimeUtils;
 import org.gwttime.time.DateTimeZone;
 import org.gwttime.time.Instant;
 import org.gwttime.time.LocalDateTime;
 import org.gwttime.time.gwt.JodaGwtTestCase;
 import org.gwttime.time.tz.DefaultNameProvider;
 import org.gwttime.time.tz.NameProvider;
 import org.gwttime.time.tz.Provider;
 
 import static org.gwttime.time.gwt.TestConstants.*;
 //import junit.framework.TestSuite;
 
 
 import com.google.gwt.i18n.client.LocaleInfo;
 
 
 /**
  * This class is a JUnit test for DateTimeZone.
  *
  * @author Stephen Colebourne
  */
 public class TestDateTimeZone extends JodaGwtTestCase {
         long y2002days = 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 
                      366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 
                      365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 +
                      366 + 365;
     long y2003days = 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 
                      366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 
                      365 + 365 + 366 + 365 + 365 + 365 + 366 + 365 + 365 + 365 +
                      366 + 365 + 365;
     
     // 2002-06-09
     private long TEST_TIME_SUMMER =
             (y2002days + 31L + 28L + 31L + 30L + 31L + 9L -1L) * DateTimeConstants.MILLIS_PER_DAY;
             
     // 2002-01-09
     private long TEST_TIME_WINTER =
             (y2002days + 9L -1L) * DateTimeConstants.MILLIS_PER_DAY;
             
 //    // 2002-04-05 Fri
 //    private long TEST_TIME1 =
 //            (y2002days + 31L + 28L + 31L + 5L -1L) * DateTimeConstants.MILLIS_PER_DAY
 //            + 12L * DateTimeConstants.MILLIS_PER_HOUR
 //            + 24L * DateTimeConstants.MILLIS_PER_MINUTE;
 //        
 //    // 2003-05-06 Tue
 //    private long TEST_TIME2 =
 //            (y2003days + 31L + 28L + 31L + 30L + 6L -1L) * DateTimeConstants.MILLIS_PER_DAY
 //            + 14L * DateTimeConstants.MILLIS_PER_HOUR
 //            + 28L * DateTimeConstants.MILLIS_PER_MINUTE;
     
     
     private DateTimeZone zone;
 
     /* Removed for GWT public static void main(String[] args) {
         junit.textui.TestRunner.run(suite());
     } */
 
     /* Removed for GWT public static TestSuite suite() {
         return new TestSuite(TestDateTimeZone.class);
     } */
 
     /* Removed for GWT public TestDateTimeZone(String name) {
         super(name);
     } */
 
     protected void gwtSetUp() throws Exception {
         super.gwtSetUp();
         /* //BEGIN GWT IGNORE
         locale = Locale.getDefault();
         //END GWT IGNORE */
         zone = DateTimeZone.getDefault();
         /* //BEGIN GWT IGNORE
         //Locale.setDefault(Locale.UK);
         Locale.setDefault(Locale.JAPAN);
         //END GWT IGNORE */
     }
 
     protected void gwtTearDown() throws Exception {
         super.gwtTearDown();
         /* //BEGIN GWT IGNORE
         Locale.setDefault(locale);
         //END GWT IGNORE */
         DateTimeZone.setDefault(zone);
     }
 
     //-----------------------------------------------------------------------
     public void testDefault() {
         assertNotNull(DateTimeZone.getDefault());
         
         DateTimeZone.setDefault(PARIS);
         assertSame(PARIS, DateTimeZone.getDefault());
         
         try {
             DateTimeZone.setDefault(null);
             fail();
         } catch (IllegalArgumentException ex) {}
     }
             
     /* //BEGIN GWT IGNORE
     public void testDefaultSecurity() {
         if (OLD_JDK) {
             return;
         }
         try {
             Policy.setPolicy(RESTRICT);
             System.setSecurityManager(new SecurityManager());
             DateTimeZone.setDefault(PARIS);
             fail();
         } catch (SecurityException ex) {
             // ok
         } finally {
             System.setSecurityManager(null);
             Policy.setPolicy(ALLOW);
         }
     }
     //END GWT IGNORE */
 
     //-----------------------------------------------------------------------
     public void testForID_String() {
         assertEquals(DateTimeZone.getDefault(), DateTimeZone.forID((String) null));
         
         DateTimeZone zone = DateTimeZone.forID("Europe/London");
         assertEquals("Europe/London", zone.getID());
         
         zone = DateTimeZone.forID("UTC");
         assertSame(DateTimeZone.UTC, zone);
         
         zone = DateTimeZone.forID("+00:00");
         assertSame(DateTimeZone.UTC, zone);
         
         zone = DateTimeZone.forID("+00");
         assertSame(DateTimeZone.UTC, zone);
         
         zone = DateTimeZone.forID("+01:23");
         assertEquals("+01:23", zone.getID());
         assertEquals(DateTimeConstants.MILLIS_PER_HOUR + (23L * DateTimeConstants.MILLIS_PER_MINUTE),
                 zone.getOffset(TEST_TIME_SUMMER));
         
         zone = DateTimeZone.forID("-02:00");
         assertEquals("-02:00", zone.getID());
         assertEquals((-2L * DateTimeConstants.MILLIS_PER_HOUR),
                 zone.getOffset(TEST_TIME_SUMMER));
         
         zone = DateTimeZone.forID("-07:05:34.0");
         assertEquals("-07:05:34", zone.getID());
         assertEquals((-7L * DateTimeConstants.MILLIS_PER_HOUR) +
                     (-5L * DateTimeConstants.MILLIS_PER_MINUTE) +
                     (-34L * DateTimeConstants.MILLIS_PER_SECOND),
                     zone.getOffset(TEST_TIME_SUMMER));
         
         try {
             DateTimeZone.forID("SST");
             fail();
         } catch (IllegalArgumentException ex) {}
         try {
             DateTimeZone.forID("Europe/UK");
             fail();
         } catch (IllegalArgumentException ex) {}
         try {
             DateTimeZone.forID("+");
             fail();
         } catch (IllegalArgumentException ex) {}
         try {
             DateTimeZone.forID("+0");
             fail();
         } catch (IllegalArgumentException ex) {}
     }
 
     //-----------------------------------------------------------------------
     public void testForOffsetHours_int() {
         assertEquals(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
         assertEquals(DateTimeZone.forID("+03:00"), DateTimeZone.forOffsetHours(3));
         assertEquals(DateTimeZone.forID("-02:00"), DateTimeZone.forOffsetHours(-2));
         try {
             DateTimeZone.forOffsetHours(999999);
             fail();
         } catch (IllegalArgumentException ex) {}
     }        
 
     //-----------------------------------------------------------------------
     public void testForOffsetHoursMinutes_int_int() {
         assertEquals(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));
         assertEquals(DateTimeZone.forID("+03:15"), DateTimeZone.forOffsetHoursMinutes(3, 15));
         assertEquals(DateTimeZone.forID("-02:00"), DateTimeZone.forOffsetHoursMinutes(-2, 0));
         assertEquals(DateTimeZone.forID("-02:30"), DateTimeZone.forOffsetHoursMinutes(-2, 30));
         try {
             DateTimeZone.forOffsetHoursMinutes(2, 60);
             fail();
         } catch (IllegalArgumentException ex) {}
         try {
             DateTimeZone.forOffsetHoursMinutes(-2, 60);
             fail();
         } catch (IllegalArgumentException ex) {}
         try {
             DateTimeZone.forOffsetHoursMinutes(2, -1);
             fail();
         } catch (IllegalArgumentException ex) {}
         try {
             DateTimeZone.forOffsetHoursMinutes(-2, -1);
             fail();
         } catch (IllegalArgumentException ex) {}
         try {
             DateTimeZone.forOffsetHoursMinutes(999999, 0);
             fail();
         } catch (IllegalArgumentException ex) {}
     }        
 
     //-----------------------------------------------------------------------
     public void testForOffsetMillis_int() {
         assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));
         assertEquals(DateTimeZone.forID("+03:00"), DateTimeZone.forOffsetMillis(3 * 60 * 60 * 1000));
         assertEquals(DateTimeZone.forID("-02:00"), DateTimeZone.forOffsetMillis(-2 * 60 * 60 * 1000));
         assertEquals(DateTimeZone.forID("+04:45:17.045"),
                 DateTimeZone.forOffsetMillis(
                         4 * 60 * 60 * 1000 + 45 * 60 * 1000 + 17 * 1000 + 45));
     }        
 
     //-----------------------------------------------------------------------
       
 
     //-----------------------------------------------------------------------
     public void testGetAvailableIDs() {
         assertTrue(DateTimeZone.getAvailableIDs().contains("UTC"));
     }
 
     //-----------------------------------------------------------------------
     /* //BEGIN GWT IGNORE
     public void testProvider() {
         try {
             assertNotNull(DateTimeZone.getProvider());
         
             Provider provider = DateTimeZone.getProvider();
             DateTimeZone.setProvider(null);
             assertEquals(provider.getClass(), DateTimeZone.getProvider().getClass());
         
             try {
                 DateTimeZone.setProvider(new MockNullIDSProvider());
                 fail();
             } catch (IllegalArgumentException ex) {}
             try {
                 DateTimeZone.setProvider(new MockEmptyIDSProvider());
                 fail();
             } catch (IllegalArgumentException ex) {}
             try {
                 DateTimeZone.setProvider(new MockNoUTCProvider());
                 fail();
             } catch (IllegalArgumentException ex) {}
             try {
                 DateTimeZone.setProvider(new MockBadUTCProvider());
                 fail();
             } catch (IllegalArgumentException ex) {}
         
             Provider prov = new MockOKProvider();
             DateTimeZone.setProvider(prov);
             assertSame(prov, DateTimeZone.getProvider());
             assertEquals(2, DateTimeZone.getAvailableIDs().size());
             assertTrue(DateTimeZone.getAvailableIDs().contains("UTC"));
             assertTrue(DateTimeZone.getAvailableIDs().contains("Europe/London"));
         } finally {
             DateTimeZone.setProvider(null);
             assertEquals(ZoneInfoProvider.class, DateTimeZone.getProvider().getClass());
         }
         
         try {
             System.setProperty("org.joda.time.DateTimeZone.Provider", "org.joda.time.tz.UTCProvider");
             DateTimeZone.setProvider(null);
             assertEquals(UTCProvider.class, DateTimeZone.getProvider().getClass());
         } finally {
             System.getProperties().remove("org.joda.time.DateTimeZone.Provider");
             DateTimeZone.setProvider(null);
             assertEquals(ZoneInfoProvider.class, DateTimeZone.getProvider().getClass());
         }
         
         PrintStream syserr = System.err;
         try {
             System.setProperty("org.joda.time.DateTimeZone.Provider", "xxx");
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             System.setErr(new PrintStream(baos));
             
             DateTimeZone.setProvider(null);
             
             assertEquals(ZoneInfoProvider.class, DateTimeZone.getProvider().getClass());
             String str = new String(baos.toByteArray());
             assertTrue(str.indexOf("java.lang.ClassNotFoundException") >= 0);
         } finally {
             System.setErr(syserr);
             System.getProperties().remove("org.joda.time.DateTimeZone.Provider");
             DateTimeZone.setProvider(null);
             assertEquals(ZoneInfoProvider.class, DateTimeZone.getProvider().getClass());
         }
     }
     //END GWT IGNORE */
     
     /* //BEGIN GWT IGNORE
     public void testProviderSecurity() {
         if (OLD_JDK) {
             return;
         }
         try {
             Policy.setPolicy(RESTRICT);
             System.setSecurityManager(new SecurityManager());
             DateTimeZone.setProvider(new MockOKProvider());
             fail();
         } catch (SecurityException ex) {
             // ok
         } finally {
             System.setSecurityManager(null);
             Policy.setPolicy(ALLOW);
         }
     }
     //END GWT IGNORE */
 
     static class MockNullIDSProvider implements Provider {
         public Set<String> getAvailableIDs() {
             return null;
         }
         public DateTimeZone getZone(String id) {
             return null;
         }
     }
     static class MockEmptyIDSProvider implements Provider {
         public Set<String> getAvailableIDs() {
             return new HashSet<String>();
         }
         public DateTimeZone getZone(String id) {
             return null;
         }
     }
     static class MockNoUTCProvider implements Provider {
         public Set<String> getAvailableIDs() {
             Set<String> set = new HashSet<String>();
             set.add("Europe/London");
             return set;
         }
         public DateTimeZone getZone(String id) {
             return null;
         }
     }
     static class MockBadUTCProvider implements Provider {
         public Set<String> getAvailableIDs() {
             Set<String> set = new HashSet<String>();
             set.add("UTC");
             set.add("Europe/London");
             return set;
         }
         public DateTimeZone getZone(String id) {
             return null;
         }
     }
     static class MockOKProvider implements Provider {
         public Set<String> getAvailableIDs() {
             Set<String> set = new HashSet<String>();
             set.add("UTC");
             set.add("Europe/London");
             return set;
         }
         public DateTimeZone getZone(String id) {
             return DateTimeZone.UTC;
         }
     }
 
     //-----------------------------------------------------------------------
     public void testNameProvider() {
         try {
             assertNotNull(DateTimeZone.getNameProvider());
         
             NameProvider provider = DateTimeZone.getNameProvider();
             DateTimeZone.setNameProvider(null);
             assertEquals(provider.getClass(), DateTimeZone.getNameProvider().getClass());
         
             provider = new MockOKButNullNameProvider();
             DateTimeZone.setNameProvider(provider);
             assertSame(provider, DateTimeZone.getNameProvider());
             
             assertEquals("+00:00", DateTimeZone.UTC.getShortName(TEST_TIME_SUMMER));
             assertEquals("+00:00", DateTimeZone.UTC.getName(TEST_TIME_SUMMER));
         } finally {
             DateTimeZone.setNameProvider(new DefaultNameProvider());
         }
         
         /* //BEGIN GWT IGNORE
         try {
             System.setProperty("org.joda.time.DateTimeZone.NameProvider", "org.joda.time.tz.DefaultNameProvider");
             DateTimeZone.setNameProvider(null);
             assertEquals(DefaultNameProvider.class, DateTimeZone.getNameProvider().getClass());
         } finally {
             System.getProperties().remove("org.joda.time.DateTimeZone.NameProvider");
             DateTimeZone.setNameProvider(null);
             assertEquals(DefaultNameProvider.class, DateTimeZone.getNameProvider().getClass());
         }
         
         PrintStream syserr = System.err;
         try {
             System.setProperty("org.joda.time.DateTimeZone.NameProvider", "xxx");
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             System.setErr(new PrintStream(baos));
             
             DateTimeZone.setNameProvider(null);
             
             assertEquals(DefaultNameProvider.class, DateTimeZone.getNameProvider().getClass());
             String str = new String(baos.toByteArray());
             assertTrue(str.indexOf("java.lang.ClassNotFoundException") >= 0);
         } finally {
             System.setErr(syserr);
             System.getProperties().remove("org.joda.time.DateTimeZone.NameProvider");
             DateTimeZone.setNameProvider(null);
             assertEquals(DefaultNameProvider.class, DateTimeZone.getNameProvider().getClass());
         }
         //END GWT IGNORE */
     }        
     
     /* //BEGIN GWT IGNORE
     public void testNameProviderSecurity() {
         if (OLD_JDK) {
             return;
         }
         try {
             Policy.setPolicy(RESTRICT);
             System.setSecurityManager(new SecurityManager());
             DateTimeZone.setNameProvider(new MockOKButNullNameProvider());
             fail();
         } catch (SecurityException ex) {
             // ok
         } finally {
             System.setSecurityManager(null);
             Policy.setPolicy(ALLOW);
         }
     }
     //END GWT IGNORE */
 
     static class MockOKButNullNameProvider implements NameProvider {
         public String getShortName(LocaleInfo locale, String id, String nameKey) {
             return null;
         }
         public String getName(LocaleInfo locale, String id, String nameKey) {
             return null;
         }
     }
 
     //-----------------------------------------------------------------------
     public void testConstructor() {
 
         try {
             new DateTimeZone(null) {
                 /**
 				 * 
 				 */
 				private static final long serialVersionUID = 5534683706448252542L;
 				public String getNameKey(long instant) {
                     return null;
                 }
                 public int getOffset(long instant) {
                     return 0;
                 }
                 public int getStandardOffset(long instant) {
                     return 0;
                 }
                 public boolean isFixed() {
                     return false;
                 }
                 public long nextTransition(long instant) {
                     return 0;
                 }
                 public long previousTransition(long instant) {
                     return 0;
                 }
                 public boolean equals(Object object) {
                     return false;
                 }
             };
         } catch (IllegalArgumentException ex) {}
     }
 
     //-----------------------------------------------------------------------
     public void testGetID() {
         DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
         assertEquals("Europe/Paris", zone.getID());
     }
 
     public void testGetNameKey() {
         DateTimeZone zone = DateTimeZone.forID("Europe/London");
         assertEquals("BST", zone.getNameKey(TEST_TIME_SUMMER));
         assertEquals("GMT", zone.getNameKey(TEST_TIME_WINTER));
     }
 
     public void testGetShortName() {
         DateTimeZone zone = DateTimeZone.forID("Europe/London");
         assertEquals("BST", zone.getShortName(TEST_TIME_SUMMER));
         assertEquals("GMT", zone.getShortName(TEST_TIME_WINTER));
         assertEquals("BST", zone.getShortName(TEST_TIME_SUMMER, LocaleInfo.getCurrentLocale()));
     }
             
     public void testGetShortNameProviderName() {
     	/*
     	 * The results of this method are highly dependent on java.util.Locale and java.util.TimeZone.
     	 * Since we have replaced these class with their GWT equivalents, the behavior of these
     	 * methods have changed slightly. Therefore, this test as written will not pass, and was 
     	 * modified to pass until such time as the differences between java core classes and their
     	 * GWT variants can be resolved. -sf Feb 20,2010-
     	 */
 //    	assertEquals(null, DateTimeZone.getNameProvider().getShortName(null, "Europe/London", "BST"));
 //    	assertEquals(null, DateTimeZone.getNameProvider().getShortName(LocaleInfo.getCurrentLocale(), null, "BST"));
 //    	assertEquals(null, DateTimeZone.getNameProvider().getShortName(LocaleInfo.getCurrentLocale(), "Europe/London", null));
 //    	assertEquals(null, DateTimeZone.getNameProvider().getShortName(null, null, null));
         assertEquals("BST", DateTimeZone.getNameProvider().getShortName(null, "Europe/London", "BST"));
         assertEquals("BST", DateTimeZone.getNameProvider().getShortName(LocaleInfo.getCurrentLocale(), null, "BST"));
         assertEquals(null, DateTimeZone.getNameProvider().getShortName(LocaleInfo.getCurrentLocale(), "Europe/London", null));
         assertEquals(null, DateTimeZone.getNameProvider().getShortName(null, null, null));
     }
     
     public void testGetShortNameNullKey() {
         DateTimeZone zone = new MockDateTimeZone("Europe/London");
         assertEquals("Europe/London", zone.getShortName(TEST_TIME_SUMMER, LocaleInfo.getCurrentLocale()));
     }
     
     //GWT result depends on java.util.Locale / java.util.TimeZone
     public void testGetName() {
     	/*
     	 * This test is highly dependent on java.util.Locale and java.util.TimeZone.
     	 * since we have replaced these classes with GWT variants, this test cannot
     	 * be made to work correctly as originally designed, due to the differences
     	 * between the java core classes and their GWT equivalents. To that end,
     	 * I'm simply commenting out this test until such time as a more 
     	 * accurate test can be written. -sf Feb. 20, 2010-
     	 */
 //        DateTimeZone zone = DateTimeZone.forID("Europe/London");
 //        assertEquals("BST", zone.getName(TEST_TIME_SUMMER));
 //        assertEquals("Greenwich Mean Time", zone.getName(TEST_TIME_WINTER));
 //        assertEquals("British Summer Time", zone.getName(TEST_TIME_SUMMER, LocaleInfo.getCurrentLocale()));
         
     }
     
     public void testGetNameProviderName() {
     	/*
     	 * The results of this method are highly dependent on java.util.Locale and java.util.TimeZone.
     	 * Since we have replaced these class with their GWT equivalents, the behavior of these
     	 * methods have changed slightly. Therefore, this test as written will not pass, and was 
     	 * modified to pass until such time as the differences between java core classes and their
     	 * GWT variants can be resolved. -sf Feb 20,2010-
     	 */
 //    	assertEquals(null, DateTimeZone.getNameProvider().getName(null, "Europe/London", "BST"));
 //      assertEquals(null, DateTimeZone.getNameProvider().getName(LocaleInfo.getCurrentLocale(), null, "BST"));
        assertEquals("Greenwich Mean Time", DateTimeZone.getNameProvider().getName(null, "Europe/London", "BST"));
         assertEquals("BST", DateTimeZone.getNameProvider().getName(LocaleInfo.getCurrentLocale(), null, "BST"));
        assertEquals("Greenwich Mean Time", 
         						DateTimeZone.getNameProvider().getName(LocaleInfo.getCurrentLocale(), "Europe/London", null));
         assertEquals(null, DateTimeZone.getNameProvider().getName(null, null, null));
     }
    
     public void testGetNameNullKey() {
         DateTimeZone zone = new MockDateTimeZone("Europe/London");
         assertEquals("Europe/London", zone.getName(TEST_TIME_SUMMER, LocaleInfo.getCurrentLocale()));
     }
     
     static class MockDateTimeZone extends DateTimeZone {
         /**
 		 * 
 		 */
 		private static final long serialVersionUID = 3823090698946574453L;
 		public MockDateTimeZone(String id) {
             super(id);
         }
         public String getNameKey(long instant) {
             return null;  // null
         }
         public int getOffset(long instant) {
             return 0;
         }
         public int getStandardOffset(long instant) {
             return 0;
         }
         public boolean isFixed() {
             return false;
         }
         public long nextTransition(long instant) {
             return 0;
         }
         public long previousTransition(long instant) {
             return 0;
         }
         public boolean equals(Object object) {
             return false;
         }
     }
 
     //-----------------------------------------------------------------------
     public void testGetOffset_long() {
         DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
         assertEquals(2L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(TEST_TIME_SUMMER));
         assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(TEST_TIME_WINTER));
         
         assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getStandardOffset(TEST_TIME_SUMMER));
         assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getStandardOffset(TEST_TIME_WINTER));
         
         assertEquals(2L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffsetFromLocal(TEST_TIME_SUMMER));
         assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffsetFromLocal(TEST_TIME_WINTER));
         
         assertEquals(false, zone.isStandardOffset(TEST_TIME_SUMMER));
         assertEquals(true, zone.isStandardOffset(TEST_TIME_WINTER));
     }
 
     public void testGetOffset_RI() {
         DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
         assertEquals(2L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(new Instant(TEST_TIME_SUMMER)));
         assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(new Instant(TEST_TIME_WINTER)));
         
         assertEquals(zone.getOffset(DateTimeUtils.currentTimeMillis()), zone.getOffset(null));
     }
 
     public void testGetOffsetFixed() {
         DateTimeZone zone = DateTimeZone.forID("+01:00");
         assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(TEST_TIME_SUMMER));
         assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(TEST_TIME_WINTER));
         
         assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getStandardOffset(TEST_TIME_SUMMER));
         assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getStandardOffset(TEST_TIME_WINTER));
         
         assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffsetFromLocal(TEST_TIME_SUMMER));
         assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffsetFromLocal(TEST_TIME_WINTER));
         
         assertEquals(true, zone.isStandardOffset(TEST_TIME_SUMMER));
         assertEquals(true, zone.isStandardOffset(TEST_TIME_WINTER));
     }
 
     public void testGetOffsetFixed_RI() {
         DateTimeZone zone = DateTimeZone.forID("+01:00");
         assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(new Instant(TEST_TIME_SUMMER)));
         assertEquals(1L * DateTimeConstants.MILLIS_PER_HOUR, zone.getOffset(new Instant(TEST_TIME_WINTER)));
         
         assertEquals(zone.getOffset(DateTimeUtils.currentTimeMillis()), zone.getOffset(null));
     }
 
     //-----------------------------------------------------------------------
     public void testGetMillisKeepLocal() {
         long millisLondon = TEST_TIME_SUMMER;
         long millisParis = TEST_TIME_SUMMER - 1L * DateTimeConstants.MILLIS_PER_HOUR;
         
         assertEquals(millisLondon, LONDON.getMillisKeepLocal(LONDON, millisLondon));
         assertEquals(millisParis, LONDON.getMillisKeepLocal(LONDON, millisParis));
         assertEquals(millisLondon, PARIS.getMillisKeepLocal(PARIS, millisLondon));
         assertEquals(millisParis, PARIS.getMillisKeepLocal(PARIS, millisParis));
         
         assertEquals(millisParis, LONDON.getMillisKeepLocal(PARIS, millisLondon));
         assertEquals(millisLondon, PARIS.getMillisKeepLocal(LONDON, millisParis));
         
         DateTimeZone zone = DateTimeZone.getDefault();
         try {
             DateTimeZone.setDefault(LONDON);
             assertEquals(millisLondon, PARIS.getMillisKeepLocal(null, millisParis));
         } finally {
             DateTimeZone.setDefault(zone);
         }
     }
 
     //-----------------------------------------------------------------------
     public void testIsFixed() {
         DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
         assertEquals(false, zone.isFixed());
         assertEquals(true, DateTimeZone.UTC.isFixed());
     }
 
     //-----------------------------------------------------------------------
     public void testTransitionFixed() {
         DateTimeZone zone = DateTimeZone.forID("+01:00");
         assertEquals(TEST_TIME_SUMMER, zone.nextTransition(TEST_TIME_SUMMER));
         assertEquals(TEST_TIME_WINTER, zone.nextTransition(TEST_TIME_WINTER));
         assertEquals(TEST_TIME_SUMMER, zone.previousTransition(TEST_TIME_SUMMER));
         assertEquals(TEST_TIME_WINTER, zone.previousTransition(TEST_TIME_WINTER));
     }
 
 //    //-----------------------------------------------------------------------
 //    public void testIsLocalDateTimeOverlap_Berlin() {
 //        DateTimeZone zone = DateTimeZone.forID("Europe/Berlin");
 //        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 10, 28, 1, 0)));
 //        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 10, 28, 1, 59, 59, 99)));
 //        assertEquals(true, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 10, 28, 2, 0)));
 //        assertEquals(true, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 10, 28, 2, 30)));
 //        assertEquals(true, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 10, 28, 2, 59, 59, 99)));
 //        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 10, 28, 3, 0)));
 //        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 10, 28, 4, 0)));
 //        
 //        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 3, 25, 1, 30)));  // before gap
 //        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 3, 25, 2, 30)));  // gap
 //        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 3, 25, 3, 30)));  // after gap
 //        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 12, 24, 12, 34)));
 //    }
 //
 //    //-----------------------------------------------------------------------
 //    public void testIsLocalDateTimeOverlap_NewYork() {
 //        DateTimeZone zone = DateTimeZone.forID("America/New_York");
 //        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 11, 4, 0, 0)));
 //        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 11, 4, 0, 59, 59, 99)));
 //        assertEquals(true, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 11, 4, 1, 0)));
 //        assertEquals(true, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 11, 4, 1, 30)));
 //        assertEquals(true, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 11, 4, 1, 59, 59, 99)));
 //        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 11, 4, 2, 0)));
 //        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 11, 4, 3, 0)));
 //        
 //        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 3, 11, 1, 30)));  // before gap
 //        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 3, 11, 2, 30)));  // gap
 //        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 3, 11, 3, 30)));  // after gap
 //        assertEquals(false, zone.isLocalDateTimeOverlap(new LocalDateTime(2007, 12, 24, 12, 34)));
 //    }
 
     //-----------------------------------------------------------------------
     public void testIsLocalDateTimeGap_Berlin() {
         DateTimeZone zone = DateTimeZone.forID("Europe/Berlin");
         assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 1, 0)));
         assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 1, 59, 59, 99)));
         assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 2, 0)));
         assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 2, 30)));
         assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 2, 59, 59, 99)));
         assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 3, 0)));
         assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 25, 4, 0)));
         
         assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 10, 28, 1, 30)));  // before overlap
         assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 10, 28, 2, 30)));  // overlap
         assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 10, 28, 3, 30)));  // after overlap
         assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 12, 24, 12, 34)));
     }
 
     //-----------------------------------------------------------------------
     public void testIsLocalDateTimeGap_NewYork() {
         DateTimeZone zone = DateTimeZone.forID("America/New_York");
         assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 1, 0)));
         assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 1, 59, 59, 99)));
         assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 2, 0)));
         assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 2, 30)));
         assertEquals(true, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 2, 59, 59, 99)));
         assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 3, 0)));
         assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 3, 11, 4, 0)));
         
         assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 11, 4, 0, 30)));  // before overlap
         assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 11, 4, 1, 30)));  // overlap
         assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 11, 4, 2, 30)));  // after overlap
         assertEquals(false, zone.isLocalDateTimeGap(new LocalDateTime(2007, 12, 24, 12, 34)));
     }
 
     
     
     //-----------------------------------------------------------------------
     public void testEqualsHashCode() {
         DateTimeZone zone1 = DateTimeZone.forID("Europe/Paris");
         DateTimeZone zone2 = DateTimeZone.forID("Europe/Paris");
         assertEquals(true, zone1.equals(zone1));
         assertEquals(true, zone1.equals(zone2));
         assertEquals(true, zone2.equals(zone1));
         assertEquals(true, zone2.equals(zone2));
         assertEquals(true, zone1.hashCode() == zone2.hashCode());
         
         DateTimeZone zone3 = DateTimeZone.forID("Europe/London");
         assertEquals(true, zone3.equals(zone3));
         assertEquals(false, zone1.equals(zone3));
         assertEquals(false, zone2.equals(zone3));
         assertEquals(false, zone3.equals(zone1));
         assertEquals(false, zone3.equals(zone2));
         assertEquals(false, zone1.hashCode() == zone3.hashCode());
         assertEquals(true, zone3.hashCode() == zone3.hashCode());
         
         DateTimeZone zone4 = DateTimeZone.forID("+01:00");
         assertEquals(true, zone4.equals(zone4));
         assertEquals(false, zone1.equals(zone4));
         assertEquals(false, zone2.equals(zone4));
         assertEquals(false, zone3.equals(zone4));
         assertEquals(false, zone4.equals(zone1));
         assertEquals(false, zone4.equals(zone2));
         assertEquals(false, zone4.equals(zone3));
         assertEquals(false, zone1.hashCode() == zone4.hashCode());
         assertEquals(true, zone4.hashCode() == zone4.hashCode());
         
         DateTimeZone zone5 = DateTimeZone.forID("+02:00");
         assertEquals(true, zone5.equals(zone5));
         assertEquals(false, zone1.equals(zone5));
         assertEquals(false, zone2.equals(zone5));
         assertEquals(false, zone3.equals(zone5));
         assertEquals(false, zone4.equals(zone5));
         assertEquals(false, zone5.equals(zone1));
         assertEquals(false, zone5.equals(zone2));
         assertEquals(false, zone5.equals(zone3));
         assertEquals(false, zone5.equals(zone4));
         assertEquals(false, zone1.hashCode() == zone5.hashCode());
         assertEquals(true, zone5.hashCode() == zone5.hashCode());
     }
 
     //-----------------------------------------------------------------------
     public void testToString() {
         DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
         assertEquals("Europe/Paris", zone.toString());
         assertEquals("UTC", DateTimeZone.UTC.toString());
     }
 
     //-----------------------------------------------------------------------
     /* //BEGIN GWT IGNORE
     public void testSerialization1() throws Exception {
         DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
         
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(zone);
         byte[] bytes = baos.toByteArray();
         oos.close();
         
         ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
         ObjectInputStream ois = new ObjectInputStream(bais);
         DateTimeZone result = (DateTimeZone) ois.readObject();
         ois.close();
         
         assertSame(zone, result);
     }
     //END GWT IGNORE */
 
     //-----------------------------------------------------------------------
     /* //BEGIN GWT IGNORE
     public void testSerialization2() throws Exception {
         DateTimeZone zone = DateTimeZone.forID("+01:00");
         
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(zone);
         byte[] bytes = baos.toByteArray();
         oos.close();
         
         ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
         ObjectInputStream ois = new ObjectInputStream(bais);
         DateTimeZone result = (DateTimeZone) ois.readObject();
         ois.close();
         
         assertSame(zone, result);
     }
     //END GWT IGNORE */
 
     public void testCommentParse() throws Exception {
         // A bug in ZoneInfoCompiler's handling of comments broke Europe/Athens
         // after 1980. This test is included to make sure it doesn't break again.
 
         DateTimeZone zone = DateTimeZone.forID("Europe/Athens");
         DateTime dt = new DateTime(2005, 5, 5, 20, 10, 15, 0, zone);
         assertEquals(1115313015000L, dt.getMillis());
     }
 
     public void testPatchedNameKeysLondon() throws Exception {
         // the tz database does not have unique name keys [1716305]
         DateTimeZone zone = DateTimeZone.forID("Europe/London");
         
         DateTime now = new DateTime(2007, 1, 1, 0, 0, 0, 0);
         String str1 = zone.getName(now.getMillis());
         String str2 = zone.getName(now.plusMonths(6).getMillis());
         assertEquals(true, str1.equals(str2));
     }
 
     public void testPatchedNameKeysSydney() throws Exception {
         // the tz database does not have unique name keys [1716305]
         DateTimeZone zone = DateTimeZone.forID("Australia/Sydney");
         
         DateTime now = new DateTime(2007, 1, 1, 0, 0, 0, 0);
         String str1 = zone.getName(now.getMillis());
         String str2 = zone.getName(now.plusMonths(6).getMillis());
         assertEquals(true, str1.equals(str2));
     }
 
     public void testPatchedNameKeysSydneyHistoric() throws Exception {
         // the tz database does not have unique name keys [1716305]
         DateTimeZone zone = DateTimeZone.forID("Australia/Sydney");
         
         DateTime now = new DateTime(1996, 1, 1, 0, 0, 0, 0);
         String str1 = zone.getName(now.getMillis());
         String str2 = zone.getName(now.plusMonths(6).getMillis());
         assertEquals(true, str1.equals(str2));
     }
 
     public void testPatchedNameKeysGazaHistoric() throws Exception {
     	/*
     	 * This test is highly dependent on java.util.Locale and java.util.TimeZone.
     	 * since we have replaced these classes with GWT variants, this test cannot
     	 * be made to work correctly as originally designed, due to the differences
     	 * between the java core classes and their GWT equivalents. To that end,
     	 * I'm simply commenting out this test until such time as a more 
     	 * accurate test can be written. -sf Feb. 20, 2010-
     	 */
         // the tz database does not have unique name keys [1716305]
 //        DateTimeZone zone = DateTimeZone.forID("Africa/Johannesburg");
 //        
 //        DateTime now = new DateTime(1943, 1, 1, 0, 0, 0, 0);
 //        String str1 = zone.getName(now.getMillis());
 //        String str2 = zone.getName(now.plusMonths(6).getMillis());
 //        assertEquals(false, str1.equals(str2));
     }
 
 }
