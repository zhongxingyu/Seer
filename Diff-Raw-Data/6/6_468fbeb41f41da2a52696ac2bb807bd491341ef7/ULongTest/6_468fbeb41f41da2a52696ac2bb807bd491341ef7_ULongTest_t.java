 /*
  * Hex - a hex viewer and annotator
  * Copyright (C) 2009-2013  Trejkaz, Hex Project
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.trypticon.hex.interpreters.primitives.unsigned;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * Tests for {@link org.trypticon.hex.interpreters.primitives.unsigned.ULong}.
  *
  * @author trejkaz
  */
 public class ULongTest {
 
     @Test
     public void testToString() {
        assertEquals("Wrong string value", "9,223,372,036,854,775,808", new ULong(0x8000000000000000L).toString());
     }
 }
