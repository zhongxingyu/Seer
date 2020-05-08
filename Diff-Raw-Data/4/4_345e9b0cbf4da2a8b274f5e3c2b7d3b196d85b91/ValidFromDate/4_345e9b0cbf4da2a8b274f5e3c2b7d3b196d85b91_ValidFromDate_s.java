 package com.amee.domain.profile;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import java.util.Calendar;
 
 /**
  * This file is part of AMEE.
  * <p/>
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * <p/>
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 public class ValidFromDate extends GCDate {
 
    private static DateTimeFormatter FMT = DateTimeFormat.forPattern("yyyyMMdd");
 
     public ValidFromDate(String validFrom) {
         super(validFrom);
     }
 
     protected long parseStr(String dateStr) {
         try {
             DateTime date = FMT.parseDateTime(dateStr);
             return date.dayOfMonth().withMinimumValue().toDateMidnight().getMillis();
         } catch (Exception e) {
             return defaultDate();
         }
     }
 
     protected long defaultDate() {
         Calendar cal = Calendar.getInstance();
         int year = cal.get(Calendar.YEAR);
         int month = cal.get(Calendar.MONTH);
         cal.clear();
         cal.set(year, month, 1);
         return cal.getTimeInMillis();
     }
 
     protected void setDefaultDateStr() {
         this.dateStr = FMT.print(this.getTime());
     }
 
     public static boolean validate(String dateStr) {
         try {
             FMT.parseDateTime(dateStr);
         } catch (IllegalArgumentException ex) {
             return false;
         }
         return true;
     }    
 }
