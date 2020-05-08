 /*
  * Copyright (c) 2011 A-pressen Digitale Medier
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package no.api.meteo.util;
 
 import no.api.meteo.MeteoException;
 import org.joda.time.DateTime;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Date;
 import java.util.TimeZone;
 
 public class MeteoDateUtilsTest {
 
     private static TimeZone systemTimezone;
 
     @BeforeClass
     public static void before() {
         systemTimezone = TimeZone.getDefault();
         TimeZone.setDefault(TimeZone.getTimeZone("GMT+2"));
     }
 
     @AfterClass
     public static void after() {
         TimeZone.setDefault(systemTimezone);
     }
 
     @Test
     public void testConstructor() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
             InstantiationException {
         Constructor c = MeteoDateUtils.class.getDeclaredConstructor();
         Assert.assertFalse(c.isAccessible());
         c.setAccessible(true);
         c.newInstance();
     }
 
     @Test(expected = MeteoException.class)
     public void testBadDateString() throws Exception {
         MeteoDateUtils.fullFormatToDate("ddd");
     }
 
     @Test
     public void testFullFormatToDate() throws Exception {
         Assert.assertNull(MeteoDateUtils.fullFormatToDate(null));
         Date d = MeteoDateUtils.fullFormatToDate("2011-05-10T03:00:00Z");
         Assert.assertNotNull(d);
         Assert.assertEquals("Tue May 10 05:00:00 GMT+02:00 2011", d.toString());
     }
 
     @Test
     public void testYyyyMMddToDate() throws Exception {
         Date d = MeteoDateUtils.yyyyMMddToDate("2011-05-10");
         Assert.assertNotNull(d);
         Assert.assertEquals("Tue May 10 02:00:00 GMT+02:00 2011", d.toString());
 
     }
 
     @Test
     public void testDateToString() throws Exception {
         Assert.assertNull(MeteoDateUtils.dateToString(null, null));
         DateTime dt = new DateTime().withYear(1977).withDayOfMonth(21).withMonthOfYear(3);
         Assert.assertEquals("0321", MeteoDateUtils.dateToString(dt.toDate(), "MMdd"));
 
     }
 
     @Test
     public void testDateToYyyyMMdd() throws Exception {
         Assert.assertNull(MeteoDateUtils.dateToYyyyMMdd(null));
         DateTime dt = new DateTime().withYear(1977).withDayOfMonth(21).withMonthOfYear(3);
         Assert.assertEquals("1977-03-21", MeteoDateUtils.dateToYyyyMMdd(dt.toDate()));
 
     }
 
     @Test
     public void testDateToHHmm() throws Exception {
         Assert.assertNull(MeteoDateUtils.dateToHHmm(null));
        DateTime dt = new DateTime().withHourOfDay(13).withMinuteOfHour(14);
         Assert.assertEquals("13:14", MeteoDateUtils.dateToHHmm(dt.toDate()));
     }
 }
