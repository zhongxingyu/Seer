 /*----------------------------------------------------------------------------*
  * This file is part of JRange.                                               *
  * Copyright (C) 2012 Osman KOCAK <kocakosm@gmail.com>                        *
  *                                                                            *
  * This program is free software: you can redistribute it and/or modify it    *
  * under the terms of the GNU Lesser General Public License as published by   *
  * the Free Software Foundation, either version 3 of the License, or (at your *
  * option) any later version.                                                 *
  * This program is distributed in the hope that it will be useful, but        *
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY *
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public     *
  * License for more details.                                                  *
  * You should have received a copy of the GNU Lesser General Public License   *
  * along with this program. If not, see <http://www.gnu.org/licenses/>.       *
  *----------------------------------------------------------------------------*/
 
 package org.jrange;
 
 import static junit.framework.Assert.*;
 
 import org.junit.Test;
 
 /**
  * Bound tests.
  *
  * @author	Osman KOCAK
  */
 public final class BoundTest
 {
 	@Test(expected=NullPointerException.class)
 	public void testOpenedBoundCreationWithNull()
 	{
		Bound.opened(null);
 	}
 
 	@Test(expected=NullPointerException.class)
 	public void testClosedBoundCreationWithNull()
 	{
		Bound.closed(null);
 	}
 
 	@Test
 	public void testValue()
 	{
 		Integer value = Integer.valueOf(Utils.randomInt());
 		Bound<Integer> bound = Bound.opened(value);
 
 		assertEquals(value, bound.value());
 	}
 
 	@Test
 	public void testOpenness()
 	{
 		Bound<Integer> bound = Bound.opened(Utils.randomInt());
 
 		assertTrue(bound.isOpened());
 		assertFalse(bound.isClosed());
 	}
 
 	@Test
 	public void testCloseness()
 	{
 		Bound<Integer> bound = Bound.closed(Utils.randomInt());
 
 		assertTrue(bound.isClosed());
 		assertFalse(bound.isOpened());
 	}
 
 	@Test
 	public void testToString1()
 	{
 		Integer value = Integer.valueOf(Utils.randomInt());
 		Bound<Integer> bound = Bound.opened(value);
 
 		assertTrue(bound.toString().contains("excluded"));
 		assertTrue(bound.toString().contains(value.toString()));
 	}
 
 	@Test
 	public void testToString2()
 	{
 		Integer value = Integer.valueOf(Utils.randomInt());
 		Bound<Integer> bound = Bound.closed(value);
 
 		assertTrue(bound.toString().contains("included"));
 		assertTrue(bound.toString().contains(value.toString()));
 	}
 
 	@Test
 	public void testEquals1()
 	{
 		Integer value = Integer.valueOf(Utils.randomInt());
 		Bound<Integer> bound1 = Bound.closed(value);
 
 		assertEquals(bound1, bound1);
 		assertEquals(bound1.hashCode(), bound1.hashCode());
 	}
 
 	@Test
 	public void testEquals2()
 	{
 		Integer value = Integer.valueOf(Utils.randomInt());
 		Bound<Integer> bound1 = Bound.opened(value);
 		Bound<Integer> bound2 = Bound.opened(value);
 
 		assertEquals(bound1, bound2);
 		assertEquals(bound2, bound1);
 		assertEquals(bound1.hashCode(), bound2.hashCode());
 	}
 
 	@Test
 	public void testEquals3()
 	{
 		Bound<Integer> bound1 = Bound.closed(Utils.randomInt());
 		Bound<Integer> bound2 = null;
 
 		assertFalse(bound1.equals(bound2));
 	}
 
 	@Test
 	public void testEquals4()
 	{
 		int value = Utils.randomInt();
 		Bound<Integer> bound1 = Bound.closed(value);
 		Bound<Integer> bound2 = Bound.opened(value);
 
 		assertFalse(bound1.equals(bound2));
 		assertFalse(bound2.equals(bound1));
 	}
 
 	@Test
 	public void testEquals5()
 	{
 		int value = Utils.randomInt();
 		Bound<Integer> bound1 = Bound.closed(value);
 		Bound<Integer> bound2 = Bound.closed(value + 1);
 
 		assertFalse(bound1.equals(bound2));
 		assertFalse(bound2.equals(bound1));
 	}
 }
