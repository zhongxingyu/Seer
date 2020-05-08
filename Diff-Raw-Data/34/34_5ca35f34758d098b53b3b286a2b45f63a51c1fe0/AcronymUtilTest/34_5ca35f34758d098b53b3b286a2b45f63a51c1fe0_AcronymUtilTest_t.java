 package org.cujau.utils;
 
 import static org.junit.Assert.assertEquals;
 
import java.util.Date;

 import org.junit.Test;
 
 public class AcronymUtilTest {
 
     @Test
     public void testToLong() {
         assertEquals( 1000, AcronymUtil.parseLongAcronym( "1,000" ) );
         assertEquals( 1000000, AcronymUtil.parseLongAcronym( "1M" ) );
         assertEquals( 1000000000, AcronymUtil.parseLongAcronym( "1B" ) );
         assertEquals( 1000000000000l, AcronymUtil.parseLongAcronym( "1T" ) );
 
         assertEquals( 1000000, AcronymUtil.parseLongAcronym( "1.00M" ) );
         assertEquals( 1000000000, AcronymUtil.parseLongAcronym( "1.00B" ) );
         assertEquals( 1000000000000l, AcronymUtil.parseLongAcronym( "1.00T" ) );
 
         assertEquals( 156789, AcronymUtil.parseLongAcronym( "156,789" ) );
         assertEquals( 1560000, AcronymUtil.parseLongAcronym( "1.56M" ) );
         assertEquals( 1567000000, AcronymUtil.parseLongAcronym( "1.567B" ) );
         assertEquals( 1500000000000l, AcronymUtil.parseLongAcronym( "1.50T" ) );

        assertEquals( Long.MIN_VALUE, AcronymUtil.parseLongAcronym( "NA" ) );
        assertEquals( Long.MIN_VALUE, AcronymUtil.parseLongAcronym( "" ) );
        assertEquals( Long.MIN_VALUE, AcronymUtil.parseLongAcronym( ",,," ) );
        assertEquals( Long.MIN_VALUE, AcronymUtil.parseLongAcronym( "1.5F" ) );
     }
 
     @Test
     public void testToString() {
        assertEquals( "0", AcronymUtil.formatLongAcronym( 0 ) );
         assertEquals( "1000", AcronymUtil.formatLongAcronym( 1000 ) );
         assertEquals( "1.00M", AcronymUtil.formatLongAcronym( 1000000 ) );
         assertEquals( "1.00B", AcronymUtil.formatLongAcronym( 1000000000 ) );
         assertEquals( "1.00T", AcronymUtil.formatLongAcronym( 1000000000000l ) );

         assertEquals( "156789", AcronymUtil.formatLongAcronym( 156789 ) );
         assertEquals( "1.50M", AcronymUtil.formatLongAcronym( 1500000 ) );
         assertEquals( "1.56B", AcronymUtil.formatLongAcronym( 1560000000 ) );
         assertEquals( "1.57T", AcronymUtil.formatLongAcronym( 1567000000000l ) );

        assertEquals( "NA", AcronymUtil.formatLongAcronym( Long.MIN_VALUE ) );
        assertEquals( "NA", AcronymUtil.formatLongAcronym( new Date() ) );
     }
 
 }
