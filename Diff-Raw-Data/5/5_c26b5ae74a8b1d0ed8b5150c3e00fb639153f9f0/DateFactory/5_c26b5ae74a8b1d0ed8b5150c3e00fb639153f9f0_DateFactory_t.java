 package ru.yaal.project.urldatabase.spring;
 
 import org.springframework.beans.factory.FactoryBean;
 
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * Фабрика для генерации дат.
  * User: Aleks
  * Date: 12.10.13
  */
 public class DateFactory implements FactoryBean<Date> {
     private int year;
     private int month;
     private int day;
     private int hours;
     private int minutes;
     private int seconds;
 
     public DateFactory(int day, int month, int year, int hours, int minutes, int seconds) {
         this.day = day;
         this.month = month;
         this.year = year;
         this.hours = hours;
         this.minutes = minutes;
         this.seconds = seconds;
     }
 
     public DateFactory(int day, int month, int year) {
         this(day, month, year, 0, 0, 0);
     }
 
     @Override
     public Date getObject() throws Exception {
         Calendar calendar = Calendar.getInstance();
         calendar.set(year, month, day, hours, minutes, seconds);
         return calendar.getTime();
     }
 
     @Override
     public Class<?> getObjectType() {
        return Date.class;
     }
 
     @Override
     public boolean isSingleton() {
        return false;
     }
 }
