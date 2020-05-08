 /**
  * 
  */
 package com.luntsys.luntbuild.utility;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import junit.framework.TestCase;
 
 /**
  * @author lubosp
  *
  */
 public class SynchronizedDateFormatterTest extends TestCase {
 
 	/**
 	 * Test method for {@link com.luntsys.luntbuild.utility.SynchronizedDateFormatter#SynchronizedDateFormatter(java.text.DateFormat)}.
 	 */
 	public void testSynchronizedDateFormatterDateFormat() {
 		SynchronizedDateFormatter formatter =
 			new SynchronizedDateFormatter(DateFormat.getDateInstance(SimpleDateFormat.DEFAULT), "default");
 		assertTrue("DateFormat.getDateInstance(SimpleDateFormat.DEFAULT)", formatter != null);
 	}
 
 	/**
 	 * Test method for {@link com.luntsys.luntbuild.utility.SynchronizedDateFormatter#SynchronizedDateFormatter(java.lang.String)}.
 	 */
 	public void testSynchronizedDateFormatterString() {
 		SynchronizedDateFormatter formatter = new SynchronizedDateFormatter("yyyy.MM.dd-hh:mm");
 		assertTrue("SynchronizedDateFormatter(\"yyyy.MM.dd-hh:mm\")", formatter != null);
 	}
 
 	/**
 	 * Test method for {@link com.luntsys.luntbuild.utility.SynchronizedDateFormatter#format(java.util.Date)}.
 	 */
 	public void testFormat() {
 		SynchronizedDateFormatter formatter = new SynchronizedDateFormatter("yyyy.MM.dd-hh:mm");
 		assertTrue("SynchronizedDateFormatter(\"yyyy.MM.dd-hh:mm\")", formatter != null);
 		String dateStr = formatter.format(new Date());
 		assertTrue("formatter.format(new Date())", dateStr != null);
 	}
 
 	/**
 	 * Test method for {@link com.luntsys.luntbuild.utility.SynchronizedDateFormatter#get()}.
 	 */
 	public void testGet() {
 		SynchronizedDateFormatter formatter = new SynchronizedDateFormatter("yyyy.MM.dd-hh:mm");
 		assertTrue("SynchronizedDateFormatter(\"yyyy.MM.dd-hh:mm\")", formatter != null);
 		SimpleDateFormat sdf = formatter.get();
 		assertTrue("formatter.get()", sdf != null);
 	}
 
 	/**
 	 * Test method for {@link com.luntsys.luntbuild.utility.SynchronizedDateFormatter#parse(java.lang.String)}.
 	 */
 	public void testParse() {
 		SynchronizedDateFormatter formatter = new SynchronizedDateFormatter("yyyy.MM.dd-hh:mm");
 		assertTrue("SynchronizedDateFormatter(\"yyyy.MM.dd-hh:mm\")", formatter != null);
 		Date date = null;
 		try {
 			date = formatter.parse("1999.11.11-12:12");
 		} catch (Exception e) {
 		}
 		assertTrue("formatter.get()", date != null);
 	}
 
 	/**
 	 * Test method for {@link com.luntsys.luntbuild.utility.SynchronizedDateFormatter#getFormat(java.lang.String)}.
 	 */
 	public void testGetFormat() {
 		SimpleDateFormat sdf = SynchronizedDateFormatter.getFormat("MM-dd-yyyy");
 		assertTrue("SynchronizedDateFormatter.getFormat(\"MM-dd-yyyy\")", sdf != null);
 	}
 
 	/**
 	 * Test method for {@link com.luntsys.luntbuild.utility.SynchronizedDateFormatter#formatDate(java.util.Date)}.
 	 */
 	public void testFormatDateDate() {
 		String dateStr = SynchronizedDateFormatter.formatDate(new Date());
 		assertTrue("SynchronizedDateFormatter.formatDate(new Date())", dateStr != null);
 	}
 
 	/**
 	 * Test method for {@link com.luntsys.luntbuild.utility.SynchronizedDateFormatter#formatDate(java.util.Date, java.lang.String)}.
 	 */
 	public void testFormatDateDateString() {
 		String dateStr = SynchronizedDateFormatter.formatDate(new Date(), "yyyy-MM-dd'T'HH:mm:ss'Z'");
 		assertTrue("SynchronizedDateFormatter.formatDate(new Date(), \"yyyy-MM-dd'T'HH:mm:ss'Z'\")", dateStr != null);
 	}
 
 	/**
 	 * Test method for {@link com.luntsys.luntbuild.utility.SynchronizedDateFormatter#parseDate(java.lang.String)}.
 	 */
 	public void testParseDateString() {
 		Date date = SynchronizedDateFormatter.parseDate("1999.11.11-12:12");
 		assertTrue("SynchronizedDateFormatter.parseDate(\"1999.11.11-12:12\")", date != null);

		date = SynchronizedDateFormatter.parseDate("18-Mrz-08.16:54:32");
		assertTrue("SynchronizedDateFormatter.parseDate(\"18-Mrz-08.16:54:32\")", date != null);
 	}
 
	
	
 	/**
 	 * Test method for {@link com.luntsys.luntbuild.utility.SynchronizedDateFormatter#parseDate(java.lang.String, java.lang.String)}.
 	 */
 	public void testParseDateStringString() {
 		Date date = SynchronizedDateFormatter.parseDate("1999.11.11-12:12", "yyyy.MM.dd-hh:mm");
 		assertTrue("SynchronizedDateFormatter.parseDate(\"1999.11.11-12:12\", \"yyyy.MM.dd-hh:mm\")", date != null);
 	}
 
 }
