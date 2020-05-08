 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.github.etsai.kfsxtrackingserver;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Manipulates times from a DD:HH:MM:SS perspective
  * @author etsai
  */
 public class Time {
    private static Pattern timePat= Pattern.compile("(\\d+) days (\\d{2}):(\\d{2}):(\\d{2})");
     
     private int days, hours, minutes, seconds;
     
     /**
      * Construct a Time object given the number of seconds
      * @param seconds Number of seconds
      */
     public Time(long seconds) {
         this.seconds= (int) (seconds % 60);
         minutes= (int) (seconds / 60);
         hours= (int) (seconds / 3600) % 24;
         days= (int) ((seconds / 3600) / 24);
     }
     
     /**
      * Construct a Time object given a string representation 
      * of the time in the format "D days HH:MM:SS".  If the 
      * input string does not match the format, an exception 
      * will be thrown
      * @param timeStr String representation to convert
      * @throws Exception if input does not match pattern
      */
     public Time(String timeStr) throws RuntimeException {
         Matcher matcher= timePat.matcher(timeStr);
         
         if (!matcher.matches()) {
             throw new RuntimeException("Input is not in the format D days HH:MM:SS");
         } else {
             days= Integer.valueOf(matcher.group(1));
            hours= Integer.valueOf(matcher.group(2));
            minutes= Integer.valueOf(matcher.group(3));
             seconds= Integer.valueOf(matcher.group(4));
            System.out.format("%s-%s-%s-%s%n", days, hours, minutes, seconds);
         }
     }
     /**
      * Get number of days
      * @return days
      */
     public int getDays() {
         return days;
     }
     /**
      * Get number of hours
      * @return hours
      */
     public int getHours() {
         return hours;
     }
     /**
      * Get number of minutes
      * @return minutes
      */
     public int getMinutes() {
         return minutes;
     }
     /**
      * Get number of seconds
      * @return seconds
      */
     public int getSeconds() {
         return seconds;
     }
     /**
      * Adds the string representation of t2 into the calling object
      * @param t2 Time offset in the form "D days HH:MM:SS"
      */
     public void add(String t2) {
         add(new Time(t2));
     }
     /**
      * Adds the values of t2 into the calling object
      * @param t2 Time offset to add
      */
     public void add(Time t2) {
         seconds+= t2.seconds;
         minutes+= (seconds / 60) + t2.minutes;
         seconds%= 60;
         hours+= (minutes / 60) + t2.hours;
         minutes%= 60;
         days+= (hours / 24) + t2.days;
         hours%= 24;
     }
     
     /**
      * Generates a string representation of the Time object.  
      * The string is in the form "D days HH:MM:SS"
      * @return Time in the form "D days HH:MM:SS"
      */
     @Override
     public String toString() {
         return String.format("%d days %02d:%02d:%02d", days, hours, minutes, seconds);
     }
 }
