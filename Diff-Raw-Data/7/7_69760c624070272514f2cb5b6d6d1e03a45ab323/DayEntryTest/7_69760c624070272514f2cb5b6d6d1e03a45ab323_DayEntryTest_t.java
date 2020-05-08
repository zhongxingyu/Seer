 /*
  * RedNotebot - Mobile dairy, journal, notekeeping tool. Copyright (C) 2012 Michael Engelhardt
  * 
  * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  */
 package de.mindcrimeilab.rednotebot.data;
 
 import junit.framework.TestCase;
 
 /**
  * @author Michael Engelhardt <me@mindcrime-ilab.de>
  * 
  */
 public class DayEntryTest extends TestCase {
 
     /**
      * Test method for {@link de.mindcrimeilab.rednotebot.data.DayEntry#setContent(java.lang.String)}.
      */
     public void testSetContent() {
         final String content = "This is the content to be set to the day entry.";
         final DayEntry testee = new DayEntry(1);
         assertNotNull(testee);
         assertNull(testee.getContent());
         testee.setContent(content);
         assertEquals(content, testee.getContent());
     }
 
     /**
      * Test method for {@link de.mindcrimeilab.rednotebot.data.DayEntry#getContent()}.
      */
     public void testGetContent() {
         testSetContent();
     }
 
     /**
      * Test method for
      * {@link de.mindcrimeilab.rednotebot.data.DayEntry#compareTo(de.mindcrimeilab.rednotebot.data.DayEntry)}.
      */
     public void testCompareTo() {
         final DayEntry de1 = new DayEntry(1);
         final DayEntry dem1 = new DayEntry(-1);
         final DayEntry de2 = new DayEntry(2);
         final DayEntry de0 = new DayEntry(0);
         final DayEntry de11 = new DayEntry(11);
 
         assertTrue(de1.compareTo(de1) == 0);
         assertTrue(de1.compareTo(dem1) > 0);
         assertTrue(de1.compareTo(de2) < 0);
     }
 
 }
