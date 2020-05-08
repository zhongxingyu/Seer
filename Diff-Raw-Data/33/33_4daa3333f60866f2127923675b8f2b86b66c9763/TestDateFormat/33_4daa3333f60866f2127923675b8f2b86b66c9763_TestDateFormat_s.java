 package org.nts.pigutils.udfs;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 
 import org.apache.pig.data.TupleFactory;
 import org.junit.Test;
 
 /**
  * 
  * Assert that a timestamp can be formatted to a format similar as with
  * SimpleDateFormat.
  * 
  */
 public class TestDateFormat {
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testDateFormat() throws IOException {
 
 		Date d = new Date();
 
 		DateFormat udf = new DateFormat();
 		String df = udf.exec(TupleFactory.getInstance().newTuple(
 				Arrays.asList(d.getTime(), "yyyy-MM-dd")));
 
 		assertEquals(new SimpleDateFormat("yyyy-MM-dd").format(d), df);
 
 	}
 
 }
