 /*
  *	Copyright 2009 Christopher J. Stehno (chris@stehno.com)
  *
  * 	Licensed under the Apache License, Version 2.0 (the "License");
  *	you may not use this file except in compliance with the License.
  *	You may obtain a copy of the License at
  *
  *		http://www.apache.org/licenses/LICENSE-2.0
  *
  *	Unless required by applicable law or agreed to in writing, software
  *	distributed under the License is distributed on an "AS IS" BASIS,
  * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *	See the License for the specific language governing permissions and
  *	limitations under the License.
  */
 package com.stehno.codeperks.lang;
 
 import static com.stehno.codeperks.test.EqualsAndHashTester.assertValidEqualsAndHash;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertTrue;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.Date;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  *
  * @author Christopher J. Stehno (chris@stehno.com)
  */
 public class DateRangeTest {
 
 	private static final ThreadLocal<DateFormat> formatter = new ThreadLocal<DateFormat>(){
 		@Override
 		protected DateFormat initialValue() {
 			return DateFormat.getDateInstance(DateFormat.SHORT);
 		};
 	};
 
 	private Date startDate,endDate;
 	private DateRange range;
 
 	@Before
 	public void before() throws ParseException{
 		this.startDate = formatter.get().parse("1/1/2006");
 		this.endDate = formatter.get().parse("3/1/2006");
 		this.range = new DateRange(startDate,endDate);
 	}
 
 	@Test
 	public void copy_constructor(){
 		assertEquals(range, new DateRange(range));
 	}
 
 	@Test
 	public void get_set_range(){
 		assertEquals(startDate, range.getStart());
 		assertEquals(endDate, range.getEnd());
 
 		final Date[] dates = range.getRange();
 		assertEquals(dates[0], range.getStart());
 		assertEquals(dates[1], range.getEnd());
 
 		final Date now = new Date();
 		range.setStart(now);
 		assertEquals(now, range.getStart());
 		range.setEnd(now);
 		assertEquals(now, range.getEnd());
 	}
 
 	@Test
 	public void equals_and_hash() throws Exception {
 		final DateRange rangeB = new DateRange(startDate, endDate);
 		final DateRange rangeC = new DateRange(startDate, endDate);
 		final DateRange rangeD = new DateRange(new Date(),new Date());
 
 		assertValidEqualsAndHash(range, rangeB, rangeC, rangeD);
 
 		final DateRange other = new DateRange(formatter.get().parse("1/4/2005"),endDate);
 		assertEquals(range,range);
 		assertNotSame(range,other);
 	}
 
 	@Test
 	public void to_string(){
        final String startPart = "[DateRange: start='Sun Jan 01 00:00:00 ";
        final String endPart = " 2006' end='Wed Mar 01 00:00:00 ";
        final String str = range.toString();
        assertTrue( str.contains(startPart) && str.contains(endPart) );
 	}
 
 	@Test
 	public void isWithin() throws Exception {
 		Date date = formatter.get().parse("1/1/2006");
 		assertTrue(range.isWithin(date));
 
 		date = formatter.get().parse("2/1/2006");
 		assertTrue(range.isWithin(date));
 
 		date = formatter.get().parse("3/1/2006");
 		assertTrue(range.isWithin(date));
 
 		date = formatter.get().parse("6/1/2006");
 		assertFalse(range.isWithin(date));
 	}
 
 	@Test
 	public void isWithin_NullStart() throws Exception {
 		range.setStart(null);
 
 		final Date date = formatter.get().parse("1/1/2006");
 		assertTrue(range.isWithin(date));
 	}
 
 	@Test
 	public void isWithin_NullEnd() throws Exception {
 		range.setEnd(null);
 
 		final Date date = formatter.get().parse("1/1/2006");
 		assertTrue(range.isWithin(date));
 	}
 
 	@Test
 	public void duration(){
 		assertEquals(5097600000L,range.duration());
 	}
 
 	@After
 	public void after() throws Exception {
 		this.startDate = null;
 		this.endDate = null;
 		this.range = null;
 	}
 }
