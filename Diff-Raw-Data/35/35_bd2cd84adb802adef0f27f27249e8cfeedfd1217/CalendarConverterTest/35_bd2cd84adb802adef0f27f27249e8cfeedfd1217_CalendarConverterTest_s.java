 /*
  * TnT - Things and tasks to do
  * Licenced under General Public Licence v3 (GPLv3)
  * newInstance.org, 2012
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.newinstance.tnt.utility;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.util.Calendar;
 
 /**
  * Tests methods of class {@link CalendarConverter}.
  *
  * @author mwalter
  */
 public class CalendarConverterTest {
 
     @Test
     public void getAsObject() {
         final CalendarConverter converter = new CalendarConverter();
         Calendar calendar = (Calendar) converter.getAsObject(null, null, "21.11.2012");
         Assert.assertEquals(21, calendar.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(11, calendar.get(Calendar.MONTH));
         Assert.assertEquals(2012, calendar.get(Calendar.YEAR));
 
         calendar = (Calendar) converter.getAsObject(null, null, "01.01.1970");
         Assert.assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(1, calendar.get(Calendar.MONTH));
         Assert.assertEquals(1970, calendar.get(Calendar.YEAR));
     }
 
     @Test
     public void getAsString() {
         Calendar calendar = Calendar.getInstance();
         calendar.set(Calendar.DAY_OF_MONTH, 13);
         calendar.set(Calendar.MONTH, 8);
         calendar.set(Calendar.YEAR, 2012);
 
         final CalendarConverter converter = new CalendarConverter();
         final String result = converter.getAsString(null, null, calendar);
         Assert.assertEquals("13.09.2012", result);
     }
 }
