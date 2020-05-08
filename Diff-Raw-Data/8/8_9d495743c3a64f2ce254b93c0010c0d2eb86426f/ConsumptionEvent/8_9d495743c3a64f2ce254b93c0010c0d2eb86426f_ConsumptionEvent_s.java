 /*
 Copyright 2011-2013 The Cassandra Consortium (cassandra-fp7.eu)
 
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 package eu.cassandra.training.consumption;
 
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.joda.time.Interval;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 /**
  * This class is used for implementing the notion of the consumption event. As
  * Consumption Event is considered a time period in the consumption data set
  * that the active and reactive activeOnly measurements are high enough to imply
  * the
  * end use of an electrical appliance.
  * 
  * @author Antonios Chrysopoulos
  * @version 0.9, Date: 29.07.2013
  */
 public class ConsumptionEvent
 {
   /**
    * This variable represents the consumption event id number, unique for each
    * event.
    */
   private int id = 0;
 
   /**
    * This variable represents the start date and time of the consumption event.
    */
   private final DateTime startDateTime;
 
   /**
    * This variable represents the end date and time of the consumption event.
    */
   private final DateTime endDateTime;
 
   /**
    * This variable represents the start date of the consumption event (time
    * 00:00).
    */
   private final DateTime startDate;
 
   /**
    * This variable represents the end date of the consumption event (time
    * 00:00).
    */
   private final DateTime endDate;
 
   /**
    * This variable notes the duration of the consumption event in minutes.
    */
   private Duration duration;
 
   /**
    * This variable presents the type of day the event has been detected
    * (weekday, weekend).
    */
   private boolean weekday;
 
   /**
    * This variable shows the minute of the day that the event started.
    */
   private int startMinuteOfDay;
 
   /**
    * This variable shows the minute of the day that the event ended.
    */
   private int endMinuteOfDay;
 
   /**
    * This is a constructor of a consumption event.
    * 
    * @param id
    *          The id number of the event
    * @param startDT
    *          The start date and time of the event
    * @param startD
    *          The start date of the event
    * @param endDT
    *          The end date and time of the event
    * @param endD
    *          The end date of the event
    */
   public ConsumptionEvent (int id, DateTime startDT, DateTime startD,
                            DateTime endDT, DateTime endD)
   {
     this.id = id;
     startDateTime = startDT;
     endDateTime = endDT;
     startDate = startD;
     endDate = endD;
 
     calculateParameters();
   }
 
   /**
    * 
    * This function is used as a getter for the start date and time variable
    * of the consumption event.
    * 
    * @return the start date and time of the consumption event.
    */
   public DateTime getStartDateTime ()
   {
     return startDateTime;
   }
 
   /**
    * 
    * This function is used as a getter for the end date and time variable
    * of the consumption event.
    * 
    * @return the end date and time of the consumption event.
    */
   public DateTime getEndDateTime ()
   {
     return endDateTime;
   }
 
   /**
    * 
    * This function is used as a getter for the start date variable
    * of the consumption event.
    * 
    * @return the start date of the consumption event.
    */
   public DateTime getStartDate ()
   {
     return startDate;
   }
 
   /**
    * 
    * This function is used as a getter for the end date variable
    * of the consumption event.
    * 
    * @return the end date of the consumption event.
    */
   public DateTime getEndDate ()
   {
     return endDate;
   }
 
   /**
    * 
    * This function is used as a getter for the duration variable
    * of the consumption event.
    * 
    * @return the duration of the consumption event.
    */
   public Duration getDuration ()
   {
     return duration;
   }
 
   /**
    * 
    * This function is used as a getter for the id variable
    * of the consumption event.
    * 
    * @return the id number of the consumption event.
    */
   public int getId ()
   {
     return id;
   }
 
   /**
    * 
    * This function is used as a getter for the start minute variable
    * of the consumption event.
    * 
    * @return the start minute of the day of the consumption event.
    */
   public int getStartMinuteOfDay ()
   {
     return startMinuteOfDay;
   }
 
   /**
    * 
    * This function is used as a getter for the end minute variable
    * of the consumption event.
    * 
    * @return the end minute of the day of the consumption event.
    */
   public int getEndMinuteOfDay ()
   {
     return endMinuteOfDay;
   }
 
   /**
    * This function is used to calculate the variables that are not directly
    * given from the dataset as duration, weekday type and start/end minute of
    * the day.
    */
   public void calculateParameters ()
   {
    
     duration = new Interval(startDateTime, endDateTime).toDuration();
     startMinuteOfDay = startDateTime.getMinuteOfDay();
     endMinuteOfDay = endDateTime.getMinuteOfDay();
     if (startDate.getDayOfWeek() > 6)
       weekday = false;
     else
       weekday = true;
   }
 
   /**
    * This function is used to present the attributes of the consumption event.
    */
   public void status ()
   {
 
     DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
     DateTimeFormatter fmt2 = DateTimeFormat.forPattern("yyyy-MM-dd");
 
     System.out.println("Id:" + id);
     System.out.println("Start DateTime:" + startDateTime.toString(fmt));
     System.out.println("Start Date:" + startDate.toString(fmt2));
     System.out.println("End DateTime:" + endDateTime.toString(fmt));
     System.out.println("End Date:" + endDate.toString(fmt2));
     System.out.println("Duration:" + duration.toPeriod());
     System.out.println("Weekday:" + weekday);
     System.out.println("Minute Of The Day:" + startMinuteOfDay);
 
   }
 
   @Override
   public String toString ()
   {
     return ("Event " + Integer.toString(id));
   }
 
 }
