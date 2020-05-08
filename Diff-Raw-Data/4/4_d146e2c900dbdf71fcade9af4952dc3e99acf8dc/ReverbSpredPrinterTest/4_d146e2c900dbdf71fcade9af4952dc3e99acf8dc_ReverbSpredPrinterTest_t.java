 /*
  *  Copyright (C) 2011 Carl Green
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package info.carlwithak.mpxg2.printing;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * Tests for ReverbSpredPrinter.
  *
  * @author Carl Green
  */
 public class ReverbSpredPrinterTest {
 
     /**
      * Test spred conversion when link is off. Size shouldn't matter.
      */
     @Test
     public void testReverbSpredToString_noLink() {
         int link = 0;
         int reverbSpred = 0;
 
         double size = 5.0;
         String expected = "0";
         String actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         // check size doesn't matter
         size = 17.0;
         reverbSpred = 127;
         expected = "127";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 255;
         expected = "255";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
     }
 
     /**
      * Test spred conversion when link is on and size is 4.0.
      */
     @Test
     public void testReverbSpredToString_size4() {
         int link = 1;
         double size = 4.0;
 
         int reverbSpred = 0;
         String expected = "0";
         String actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 9;
         expected = "0";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 10;
         expected = "1";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 249;
         expected = "24";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 255;
         expected = "25";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
     }
 
     /**
      * Test spred conversion when link is on and size is 22.5.
      */
     @Test
     public void testReverbSpredToString_size22_5() {
         int link = 1;
         double size = 22.5;
 
         int reverbSpred = 0;
         String expected = "0";
         String actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 3;
         expected = "0";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 4;
         expected = "1";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 222;
         expected = "73";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 253;
         expected = "83";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 254;
         expected = "84";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 255;
         expected = "84";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
     }
 
     /**
      * Test spred conversion when link is on and size is 24.0.
      */
     @Test
     public void testReverbSpredToString_size24() {
         int link = 1;
         double size = 24.0;
 
         int reverbSpred = 0;
         String expected = "0";
         String actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 2;
         expected = "0";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 3;
         expected = "1";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 120;
         expected = "42";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 254;
         expected = "88";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 255;
         expected = "89";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
     }
 
     /**
      * Test spred conversion when link is on and size is 28.0.
      */
     @Test
     public void testReverbSpredToString_size28() {
         int link = 1;
         double size = 28.0;
 
         int reverbSpred = 0;
         String expected = "0";
         String actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 2;
         expected = "0";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 3;
         expected = "1";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 120;
         expected = "48";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 254;
         expected = "101";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 255;
         expected = "102";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
     }
 
     /**
      * Test spred conversion when link is on and size is 35.0.
      */
     @Test
     public void testReverbSpredToString_size35() {
         int link = 1;
         double size = 35.0;
 
         int reverbSpred = 0;
         String expected = "0";
         String actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 2;
         expected = "0";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 3;
         expected = "1";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 39;
         expected = "19";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 41;
         expected = "19";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 254;
         expected = "123";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 255;
         expected = "124";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
     }
 
     /**
      * Test spred conversion when link is on and size is 53.0.
      */
     @Test
     public void testReverbSpredToString_size53() {
         int link = 1;
         double size = 53.0;
 
         int reverbSpred = 0;
         String expected = "0";
         String actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 1;
         expected = "0";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 2;
         expected = "1";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
        reverbSpred = 125;
        expected = "89";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 254;
         expected = "180";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
 
         reverbSpred = 255;
         expected = "181";
         actual = ReverbSpredPrinter.reverbSpredToString(link, size, reverbSpred);
         assertEquals(expected, actual);
     }
 
 }
