 /*
  *    Copyright 2013 Weald Technology Trading Limited
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package test.com.wealdtech.schedule;
 
 import static org.testng.Assert.*;
 
 import org.joda.time.DateTime;
 import org.joda.time.Hours;
 import org.joda.time.Period;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import com.wealdtech.DataError;
 import com.wealdtech.schedule.Schedule;
 
 public class ScheduleTest
 {
   @BeforeClass
   public void setUp()
   {
   }
 
   @Test
   public void testModel() throws Exception
   {
     final Schedule rs1 = new Schedule.Builder()
                                      .start(new DateTime(2012, 1, 5, 1, 0))
                                      .duration(new Period(Hours.ONE))
                                      .monthsOfYear(1)
                                      .daysOfMonth(5)
                                      .build();
     assertNotNull(rs1);
     rs1.toString();
     rs1.hashCode();
     assertNotEquals(null, rs1);
     assertEquals(rs1, rs1);
 
     final Schedule rs2 = new Schedule.Builder(rs1)
                                      .start(new DateTime(2012, 2, 5, 1, 0))
                                      .duration(new Period(Hours.ONE))
                                      .monthsOfYear(2)
                                      .daysOfMonth(5)
                                      .build();
     assertNotNull(rs2);
     rs2.toString();
     rs2.hashCode();
     assertNotEquals(null, rs2);
     assertEquals(rs2, rs2);
     assertNotEquals(rs1, rs2);
   }
 
   @Test
   public void testInvalidNoStartTime() throws Exception
   {
     try
     {
       new Schedule.Builder()
                   .duration(new Period(Hours.ONE))
                   .monthsOfYear(1)
                   .daysOfMonth(5)
                   .build();
       fail("Created schedule without start date");
     }
     catch (DataError.Missing de)
     {
       // Good
     }
   }
 
   @Test
   public void testInvalidMonthAndWeekOfYear() throws Exception
   {
     try
     {
       new Schedule.Builder()
                   .start(new DateTime(2012, 1, 5, 1, 0))
                   .duration(new Period(Hours.ONE))
                   .monthsOfYear(1)
                   .weeksOfYear(5)
                   .build();
       fail("Created schedule with both month and week of year");
     }
     catch (DataError.Bad de)
     {
       // Good
     }
   }
 
   @Test
   public void testInvalidBadYear() throws Exception
   {
     try
     {
       new Schedule.Builder()
                   .start(new DateTime(2012, 1, 5, 1, 0))
                   .duration(new Period(Hours.ONE))
                   .yearGap(-1)
                   .weeksOfYear(5)
                   .daysOfWeek(1)
                   .build();
       fail("Created schedule with invalid year gap");
     }
     catch (DataError.Bad de)
     {
       // Good
     }
   }
 
   @Test
   public void testInvalidBadStart() throws Exception
   {
     try
     {
       new Schedule.Builder()
                   .start(new DateTime(2012, 1, 3, 1, 0))
                   .duration(new Period(Hours.ONE))
                   .weeksOfYear(Schedule.ALL)
                   .daysOfWeek(1)
                   .build();
       fail("Created schedule with invalid start");
     }
     catch (DataError.Bad de)
     {
       // Good
     }
   }
 
   @Test
   public void testRealWeekOfYear() throws Exception
   {
     for (int i = 1970; i < 2070; i++)
     {
       DateTime dt = new DateTime(i, 1, 1, 1, 0);
       int realWeekOfYear = Schedule.getRealWeekOfYear(dt);
       assertEquals(realWeekOfYear, 1, "Real week of year was " + realWeekOfYear + "(" + dt + ")");
     }
 
     for (int i = 1970; i < 2070; i++)
     {
       DateTime dt = new DateTime(i, 12, 31, 1, 0);
       int realWeekOfYear = Schedule.getRealWeekOfYear(dt);
      assertTrue(realWeekOfYear >= 52 && realWeekOfYear <= 54, "Real week of year was " + realWeekOfYear + "(" + dt + ")");
     }
 }
 }
