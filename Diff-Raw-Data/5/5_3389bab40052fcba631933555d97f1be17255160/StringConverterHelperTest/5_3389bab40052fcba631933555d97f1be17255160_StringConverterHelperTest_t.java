 package org.cujau.utils.converters;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.math.BigDecimal;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.Locale;
 
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class StringConverterHelperTest {
 
     private static final Logger LOG = LoggerFactory.getLogger( StringConverterHelperTest.class );
     private static final float DELTA = 0.0001f;
 
     @Test
     public void testFloatValueOf() {
         assertEquals( 1234f, StringConverterHelper.floatValueOf( "1234" ), DELTA );
         assertEquals( 1234f, StringConverterHelper.floatValueOf( "1234." ), DELTA );
         assertEquals( 1234f, StringConverterHelper.floatValueOf( "1234.0" ), DELTA );
         assertEquals( 1234.56f, StringConverterHelper.floatValueOf( "1234.56" ), DELTA );
         assertEquals( 1234.5007f, StringConverterHelper.floatValueOf( "1234.5007" ), DELTA );
         assertEquals( 1234.5678f, StringConverterHelper.floatValueOf( "1234.5678" ), DELTA );
         assertEquals( 1234.5678f, StringConverterHelper.floatValueOf( "1,234.5678" ), DELTA );
         assertEquals( 1234567.89f, StringConverterHelper.floatValueOf( "1,234,567.89" ), DELTA );
 
         assertEquals( -1234f, StringConverterHelper.floatValueOf( "-1234" ), DELTA );
         assertEquals( -1234f, StringConverterHelper.floatValueOf( "-1234." ), DELTA );
         assertEquals( -1234f, StringConverterHelper.floatValueOf( "-1234.0" ), DELTA );
         assertEquals( -1234.56f, StringConverterHelper.floatValueOf( "-1234.56" ), DELTA );
         assertEquals( -1234.5007f, StringConverterHelper.floatValueOf( "-1234.5007" ), DELTA );
         assertEquals( -1234.5678f, StringConverterHelper.floatValueOf( "-1234.5678" ), DELTA );
         assertEquals( -1234.5678f, StringConverterHelper.floatValueOf( "-1,234.5678" ), DELTA );
         assertEquals( -1234567.89f, StringConverterHelper.floatValueOf( "-1,234,567.89" ), DELTA );
 
         assertEquals( 1234f, StringConverterHelper.floatValueOf( "$1234" ), DELTA );
         assertEquals( 1234f, StringConverterHelper.floatValueOf( "1234.\u20AC" ), DELTA ); // Euro
                                                                                            // sym
         assertEquals( 1234f, StringConverterHelper.floatValueOf( "1234.0 CHF" ), DELTA );
         assertEquals( 1234.56f, StringConverterHelper.floatValueOf( "C$1234.56" ), DELTA );
         assertEquals( 1234.5007f, StringConverterHelper.floatValueOf( "AU$1234.5007\u20AC" ), DELTA );
         assertEquals( 1234.5678f, StringConverterHelper.floatValueOf( "1234.5678\u20A4" ), DELTA ); // GBP
                                                                                                     // sym
         assertEquals( 1234.5678f, StringConverterHelper.floatValueOf( "$1,234.5678$" ), DELTA );
         assertEquals( 1234567.89f, StringConverterHelper.floatValueOf( "\u20A41,234,567.89\u20A4" ), DELTA );
 
         assertEquals( 1.0f, StringConverterHelper.floatValueOf( "1-23.45" ), DELTA );
         assertEquals( 2923.5f, StringConverterHelper.floatValueOf( "2,923.50" ), DELTA );
     }
 
     @Test
     public void testBigDecimalValueOf() throws ParseException {
         assertEquals( new BigDecimal( "1234" ), StringConverterHelper.bigDecimalValueOf( "1234" ) );
         assertEquals( new BigDecimal( "1234" ), StringConverterHelper.bigDecimalValueOf( "1234." ) );
         assertEquals( new BigDecimal( "1234.0" ), StringConverterHelper.bigDecimalValueOf( "1234.0" ) );
         assertEquals( new BigDecimal( "1234.56" ), StringConverterHelper.bigDecimalValueOf( "1234.56" ) );
         assertEquals( new BigDecimal( "1234.5007" ), StringConverterHelper.bigDecimalValueOf( "1234.5007" ) );
         assertEquals( new BigDecimal( "1234.5678" ), StringConverterHelper.bigDecimalValueOf( "1234.5678" ) );
         assertEquals( new BigDecimal( "1234.5678" ), StringConverterHelper.bigDecimalValueOf( "1,234.5678" ) );
         assertEquals( new BigDecimal( "1234567.89" ),
                       StringConverterHelper.bigDecimalValueOf( "1,234,567.89" ) );
 
         assertEquals( new BigDecimal( "-1234" ), StringConverterHelper.bigDecimalValueOf( "-1234" ) );
         assertEquals( new BigDecimal( "-1234" ), StringConverterHelper.bigDecimalValueOf( "-1234." ) );
         assertEquals( new BigDecimal( "-1234.0" ), StringConverterHelper.bigDecimalValueOf( "-1234.0" ) );
         assertEquals( new BigDecimal( "-1234.56" ), StringConverterHelper.bigDecimalValueOf( "-1234.56" ) );
         assertEquals( new BigDecimal( "-1234.5007" ), StringConverterHelper.bigDecimalValueOf( "-1234.5007" ) );
         assertEquals( new BigDecimal( "-1234.5678" ), StringConverterHelper.bigDecimalValueOf( "-1234.5678" ) );
         assertEquals( new BigDecimal( "-1234.5678" ),
                       StringConverterHelper.bigDecimalValueOf( "-1,234.5678" ) );
         assertEquals( new BigDecimal( "-1234567.89" ),
                       StringConverterHelper.bigDecimalValueOf( "-1,234,567.89" ) );
 
         assertEquals( new BigDecimal( "1234" ), StringConverterHelper.bigDecimalValueOf( "$1234" ) );
         assertEquals( new BigDecimal( "1234" ), StringConverterHelper.bigDecimalValueOf( "1234.\u20AC" ) ); // Euro
         // sym
         assertEquals( new BigDecimal( "1234.0" ), StringConverterHelper.bigDecimalValueOf( "1234.0 CHF" ) );
         assertEquals( new BigDecimal( "1234.56" ), StringConverterHelper.bigDecimalValueOf( "C$1234.56" ) );
         assertEquals( new BigDecimal( "1234.5007" ),
                       StringConverterHelper.bigDecimalValueOf( "AU$1234.5007\u20AC" ) );
         assertEquals( new BigDecimal( "1234.5678" ),
                       StringConverterHelper.bigDecimalValueOf( "1234.5678\u20A4" ) ); // GBP
         // sym
         assertEquals( new BigDecimal( "1234.5678" ),
                       StringConverterHelper.bigDecimalValueOf( "$1,234.5678$" ) );
         assertEquals( new BigDecimal( "1234567.89" ),
                       StringConverterHelper.bigDecimalValueOf( "\u20A41,234,567.89\u20A4" ) );
 
         assertEquals( new BigDecimal( "2923.50" ), StringConverterHelper.bigDecimalValueOf( "2,923.50" ) );
         
         // Is something like this even valid? ever?
         // assertEquals( new BigDecimal( "1.0"), StringConverterHelper.bigDecimalValueOf( "1-23.45"
         // ) );
     }
     
     @Test
     public void testBigDecimal() throws ParseException { 
        Locale def = Locale.getDefault();
         Locale.setDefault( Locale.GERMANY );
         assertEquals( new BigDecimal( "53.256" ), StringConverterHelper.bigDecimalValueOf( "53,256" ) );
        assertEquals( new BigDecimal( "53256.123" ), StringConverterHelper.bigDecimalValueOf( "53.256,123" ) );
        Locale.setDefault( def );
     }
 
     @Test
     public void testIntValueOf() {
         assertTrue( 1234 == StringConverterHelper.intValueOf( "1234" ) );
         assertTrue( 1234 == StringConverterHelper.intValueOf( "1,234" ) );
         assertTrue( 1234567 == StringConverterHelper.intValueOf( "1,234,567" ) );
         assertTrue( 1234 == StringConverterHelper.intValueOf( "$1,234$" ) );
         assertTrue( 1234567 == StringConverterHelper.intValueOf( "\u20A41,234,567\u20A4" ) );
     }
 
     @Test
     public void testCurrencyFormat() {
         float val = 12345.34f;
         NumberFormat nf = NumberFormat.getCurrencyInstance();
         LOG.debug( "{} - {}", nf.format( val ), String.format( "%.4f", val ) );
         nf = NumberFormat.getCurrencyInstance( Locale.FRANCE );
         LOG.debug( nf.format( val ) );
         nf = NumberFormat.getCurrencyInstance( Locale.UK );
         LOG.debug( nf.format( val ) );
         nf = NumberFormat.getCurrencyInstance( Locale.CANADA );
         LOG.debug( nf.format( val ) );
         nf = NumberFormat.getCurrencyInstance( new Locale( "de", "CH" ) );
         LOG.debug( nf.format( val ) );
         nf = NumberFormat.getCurrencyInstance( new Locale( "fr", "CH" ) );
         LOG.debug( nf.format( val ) );
         nf = NumberFormat.getCurrencyInstance( Locale.JAPAN );
         LOG.debug( nf.format( val ) );
     }
 
     @Test
     public void testFloatFormat()
             throws ParseException {
         float val = 1050.86f;
         NumberFormat nf = NumberFormat.getInstance();
         LOG.debug( nf.format( val ) );
         Number nbr = nf.parse( "1,050.86" );
         assertTrue( "expected 1050.86 but got: " + nbr.floatValue(), nbr.floatValue() == val );
 
         nf = NumberFormat.getInstance( Locale.FRANCE );
         LOG.debug( nf.format( val ) );
         nbr = nf.parse( "1050,86" );
         assertTrue( "expected 1050.86 but got: " + nbr.floatValue(), nbr.floatValue() == val );
 
         nf = NumberFormat.getInstance( Locale.GERMANY );
         LOG.debug( nf.format( val ) );
         nbr = nf.parse( "1050,86" );
         assertTrue( "expected 1050.86 but got: " + nbr.floatValue(), nbr.floatValue() == val );
 
         nf = NumberFormat.getInstance( Locale.UK );
         LOG.debug( nf.format( val ) );
         nbr = nf.parse( "1050.86" );
         assertTrue( "expected 1050.86 but got: " + nbr.floatValue(), nbr.floatValue() == val );
 
         nf = NumberFormat.getInstance( new Locale( "de", "CH" ) );
         LOG.debug( nf.format( val ) );
         nbr = nf.parse( "1'050.86" );
         assertTrue( "expected 1050.86 but got: " + nbr.floatValue(), nbr.floatValue() == val );
     }
 
     @Test
     public void testBooleanFormat() {
         assertTrue( StringConverterHelper.booleanValueOf( "true" ) );
         assertFalse( StringConverterHelper.booleanValueOf( "false" ) );
         assertTrue( StringConverterHelper.booleanValueOf( "1" ) );
         assertFalse( StringConverterHelper.booleanValueOf( "10" ) );
         assertFalse( StringConverterHelper.booleanValueOf( "asdf" ) );
         assertFalse( StringConverterHelper.booleanValueOf( "ok" ) );
         assertTrue( StringConverterHelper.booleanValueOf( "Yes" ) );
         assertFalse( StringConverterHelper.booleanValueOf( null ) );
 
         StringBooleanConverter conv = new StringBooleanConverter();
         assertEquals( "true", conv.convert( true ) );
         assertEquals( "false", conv.convert( false ) );
         assertEquals( "", conv.convert( (Boolean) null ) );
     }
 
 }
