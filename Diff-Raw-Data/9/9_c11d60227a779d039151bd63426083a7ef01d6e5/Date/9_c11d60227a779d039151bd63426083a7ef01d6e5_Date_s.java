 package com.example.stocks.util;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import static java.util.Calendar.*;
 
 public class Date {
 
     private final Integer year;
     private final Integer month;
     private final Integer day;
 
     public Date() {
         Calendar calendar = new GregorianCalendar();
         this.year = calendar.get(YEAR);
        this.month = calendar.get(MONTH);
        this.day = calendar.get(DAY_OF_WEEK);
     }
 
     public Date(Integer year, Integer month, Integer day) {
         this.year = year;
         this.month = month;
         this.day = day;
     }
 
     public String format(DateFormatter formatter) {
         return formatter.format(year, month, day);
     }
 }
