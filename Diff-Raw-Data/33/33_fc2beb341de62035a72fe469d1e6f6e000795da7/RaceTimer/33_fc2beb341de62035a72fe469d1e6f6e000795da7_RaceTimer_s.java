 package com.tngtech.internal.leapcontrol.helpers;
 
 import java.util.Date;
 
 public class RaceTimer
 {
   private Date startTime;
 
   private Date endTime;
 
   public void start()
   {
    startTime = new Date();
    endTime = null;
   }
 
   public void stop()
   {
    endTime = new Date();
   }
 
   public long getElapsedTime()
   {
     if (startTime == null)
     {
       return 0;
     } else if (endTime == null)
     {
       return new Date().getTime() - startTime.getTime();
     } else
     {
       return endTime.getTime() - startTime.getTime();
     }
   }
 }
