 package com.twock.swappricer.test.fpml.woodstox.model;
 
 import com.twock.swappricer.fpml.woodstox.model.DateWithDayCount;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * @author Chris Pearson (chris@twock.com)
  */
 public class DateWithDayCountTest {
   @Test
   public void addMonthsPositive() {
     Assert.assertEquals(new DateWithDayCount(2012, 1, 11), new DateWithDayCount(2012, 1, 11).addMonths(0));
     Assert.assertEquals(new DateWithDayCount(2012, 2, 11), new DateWithDayCount(2012, 1, 11).addMonths(1));
     Assert.assertEquals(new DateWithDayCount(2012, 3, 11), new DateWithDayCount(2012, 1, 11).addMonths(2));
     Assert.assertEquals(new DateWithDayCount(2013, 1, 11), new DateWithDayCount(2012, 1, 11).addMonths(12));
   }
 
   @Test
   public void addMonthsNegative() {
     Assert.assertEquals(new DateWithDayCount(2012, 1, 11), new DateWithDayCount(2012, 1, 11).addMonths(-0));
     Assert.assertEquals(new DateWithDayCount(2011, 12, 11), new DateWithDayCount(2012, 1, 11).addMonths(-1));
     Assert.assertEquals(new DateWithDayCount(2011, 11, 11), new DateWithDayCount(2012, 1, 11).addMonths(-2));
     Assert.assertEquals(new DateWithDayCount(2011, 1, 11), new DateWithDayCount(2012, 1, 11).addMonths(-12));
   }
 
   @Test
   public void addMonthsEndOfMonth() {
     Assert.assertEquals(new DateWithDayCount(2012, 2, 29), new DateWithDayCount(2012, 1, 31).addMonths(1));
    Assert.assertEquals(new DateWithDayCount(2012, 3, 29), new DateWithDayCount(2012, 2, 29).addMonths(1));
     Assert.assertEquals(new DateWithDayCount(2012, 4, 30), new DateWithDayCount(2012, 3, 31).addMonths(1));
   }
 
   @Test
   public void addMonthsNegativeEndOfMonth() {
     Assert.assertEquals(new DateWithDayCount(2011, 12, 31), new DateWithDayCount(2012, 1, 31).addMonths(-1));
     Assert.assertEquals(new DateWithDayCount(2012, 1, 29), new DateWithDayCount(2012, 2, 29).addMonths(-1));
     Assert.assertEquals(new DateWithDayCount(2012, 2, 29), new DateWithDayCount(2012, 3, 31).addMonths(-1));
     Assert.assertEquals(new DateWithDayCount(2012, 3, 30), new DateWithDayCount(2012, 4, 30).addMonths(-1));
   }
 }
