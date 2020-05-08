 /*
  * Copyright (C) 2010-2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import com.google.common.collect.Range;
 import java.util.Date;
 
 /**
  *
  * @author mcculley
  */
 public class PeriodOfPerformance {
 
     private final Date start;
     private final Date end;
 
     public PeriodOfPerformance(Date start, Date end) {
         this.start = start;
         this.end = end;
     }
 
     public PeriodOfPerformance(Range<Date> range) {
         this.start = range.lowerEndpoint();
         this.end = range.upperEndpoint();
     }
 
     public Date getEnd() {
         return end;
     }
 
     public Date getStart() {
         return start;
     }
 
     public Range<Date> asRange() {
        return Range.closed(start, end);
     }
 
     @Override
     public String toString() {
         return "{start=" + start + ", end=" + end + "}";
     }
 
 }
