 package net.newfoo.logs.analytics.client;
 /*
  * Copyright (c) 2009 Jim Connell
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 import java.util.Date;
 
 public class TimeEntry {
     private Date date;
    private Integer hour;
     private Long visits;
     private Long newVisits;
     private Long bounces;
 
     public TimeEntry(Date date, Long visits, Long newVisits, Long bounces) {
         this.date = date;
         this.visits = visits;
         this.newVisits = newVisits;
         this.bounces = bounces;
     }
 
    public TimeEntry(Integer hour, Long visits, Long newVisits, Long bounces) {
        this.hour = hour;
        this.visits = visits;
        this.newVisits = newVisits;
        this.bounces = bounces;
    }

     public Long getBounces() {
         return bounces;
     }
 
     public Date getDate() {
         return date;
     }
 
    public Integer getHour() {
        return hour;
    }

     public Long getVisits() {
         return visits;
     }
 
     public Long getNewVisits() {
         return newVisits;
     }
 }
