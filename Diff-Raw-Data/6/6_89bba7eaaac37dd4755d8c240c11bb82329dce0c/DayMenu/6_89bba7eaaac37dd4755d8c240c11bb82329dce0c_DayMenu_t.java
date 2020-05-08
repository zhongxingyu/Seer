 package com.kwc.itfornebulunchapp.model;
 
 import android.text.Html;
 
 /**
  * DayMenu is an object holding the dish
  * for a single day.
  * @since 1.0
  * @author Marius Kristensen
  */
 
 public class DayMenu {
     /**
      * Weekday Number. Monday = 1, Tuesday = 2 etc.
      */
     private int dayOfWeek;
 
     /**
      * The Weekday in String.
      */
     private String weekday;
 
     /**
      * The dish that is served.
      */
     private String dish;
 
     /**
      * Constructor.
      * @param weekday - Name of day in String
      * @param dish What dish is served on this day
      * @param dayOfWeek Weekday number (i.e 2=monday, 3=tuesday)
      */
     public DayMenu(final String weekday, final String dish, final int dayOfWeek) {
         this.dayOfWeek = dayOfWeek;
         this.weekday = weekday;
         this.dish = dish;
     }
 
     public final void setDayOfWeek(final int dayOfWeek) {
         this.dayOfWeek = dayOfWeek;
     }
     public final int getDayOfWeek() {
         return dayOfWeek;
     }
 
     public final String getWeekday() {
         return weekday;
     }
 
     public final void setWeekday(final String weekday) {
         this.weekday = weekday;
     }
 
     public final String getDish() {
         return dish;
     }
 
     public final void setDish(final String dish) {
         this.dish = dish;
     }
 
     public final String getDishWithoutHTML() {
        String menu = "";
        if (dish != null) {
            menu = Html.fromHtml(dish).toString();
        }
        return menu;
     }
 }
