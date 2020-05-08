 package org.cds06.speleograph.data;
 
 import org.jfree.data.time.DateRange;
 
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * This file is created by PhilippeGeek.
  * Distributed on licence GNU GPL V3.
  */
 public class Item implements Comparable<Item> {
 
     private double value = Double.NaN;
     private Date date = Calendar.getInstance().getTime();
     private Date endDate = null;
     private double low = Double.NaN;
     private double high = Double.NaN;
 
     public Item(Date date, double value) {
         this.value = value;
         this.date = date;
     }
 
     public Item(Date startDate, Date endDate, double low, double high) {
         this.date = startDate;
         this.endDate = endDate;
         this.low = low;
         this.high = high;
     }
 
     public Item(Date startDate, Date endDate, double value) {
         this.value = value;
         this.date = startDate;
         this.endDate = endDate;
     }
 
     public Item(Date date, double low, double high) {
         this.date = date;
         this.low = low;
         this.high = high;
     }
 
     public Item(Date date, double value, double low, double high) {
         this.value = value;
         this.low = low;
         this.high = high;
         this.date = date;
     }
 
     public double getValue() {
         return value;
     }
 
     public void setValue(double value) {
         this.value = value;
     }
 
     public double getHigh() {
         return high;
     }
 
     public void setHigh(double high) {
         this.high = high;
     }
 
     public double getLow() {
         return low;
     }
 
     public void setLow(double low) {
         this.low = low;
     }
 
     public Date getEndDate() {
         return endDate;
     }
 
     public void setEndDate(Date endDate) {
         this.endDate = endDate;
     }
 
     public Date getStartDate() {
         return getDate();
     }
 
     public void setStartDate(Date date) {
         setDate(date);
     }
 
     public Date getDate() {
         return date;
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 
     public DateRange getDateRange() {
         if (endDate == null) return null;
         return new DateRange(getStartDate(), getEndDate());
     }
 
     @Override
    public int compareTo( @SuppressWarnings("NullableProblems") Item o) {
         if (o == null) throw new NullPointerException("Value compared is null");
         return getDate().compareTo(o.getDate());
     }
 }
