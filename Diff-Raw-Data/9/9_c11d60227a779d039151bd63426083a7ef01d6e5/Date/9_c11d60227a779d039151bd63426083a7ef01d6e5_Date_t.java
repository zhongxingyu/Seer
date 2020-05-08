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
        this.month = monthFrom(calendar) == 13 ? 1 : monthFrom(calendar);
        this.day = calendar.get(DAY_OF_MONTH);
     }
 
     public Date(Integer year, Integer month, Integer day) {
         this.year = year;
         this.month = month;
         this.day = day;
     }
 
     public String format(DateFormatter formatter) {
         return formatter.format(year, month, day);
     }

    private static int monthFrom(Calendar calendar) {
        return calendar.get(MONTH) + 1;
    }
 }
