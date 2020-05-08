 package org.cyclopsgroup.caff.format;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import org.cyclopsgroup.caff.ABean;
 import org.junit.Test;
 
 /**
  * Test case of {@link FixLengthImpl}
  *
  * @author <a href="mailto:jiaqi@cyclopsgroup.org">Jiaqi Guo</a>
  */
 public class FixLengthImplTest
 {
     /**
      * Verify result of populate
      */
     @Test
     public void testPopulate()
     {
     	FixLengthImpl<ABean> impl = new FixLengthImpl<ABean>( ABean.class );
         ABean bean = new ABean();
         impl.populate( bean, "03519691122Bender    Rod       1" );
         assertEquals( 35, bean.getAge() );
         assertEquals( "Bender", bean.getFirstName() );
         assertEquals( "Rod", bean.lastName );
         assertTrue(bean.isRetired());
        assertEquals( "1969-11-22", new SimpleDateFormat("yyyy-MM-dd").format( bean.getBirthDay()));
     }
 
     /**
      * Verify printing result
      *
      * FIXME Verification isn't completed because printing logic isn't finished
      *
      * @throws ParseException Should never happen
      */
     @Test
     public void testPrint() throws ParseException
     {
     	FixLengthImpl<ABean> impl = new FixLengthImpl<ABean>( ABean.class );
     	ABean bean = new ABean();
     	bean.setAge(35);
    	bean.setBirthDay(new SimpleDateFormat("yyyyMMdd").parse("19871204"));
     	bean.setFirstName("Bender");
     	bean.lastName = "Rod";
     	char[] output = impl.print(bean);
    	assertEquals("03519871204Bender    Rod       0", new String(output).trim());
     }
 }
