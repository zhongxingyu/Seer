 package iniconfigurationmanager.options;
 
 /**
  * The <code>UnsignedOptionData</code> class extends OptionData of
  * <code>BigInteger</code> value type.
  * <p>
  * Becouse java doesnt have 64bit unsigned class, it is reprezents in
  * <code>BigInteger</code> but using only positive range of number
  * In addition, this class provides method for
  * parsing a <code>RawValue</code> to a <code>UnsignedOptionData</code>,
  * method for returnig class of <code>BooleanOptionData</code>
  * parsing method and valueToString method that returns <code>string</code>
  * whit radix same as parsed string
  */
 
 
 import iniconfigurationmanager.parsing.RawValue;
 import iniconfigurationmanager.schema.OptionData;
 import iniconfigurationmanager.utils.NumberUtils;
 import java.math.BigInteger;
 
 
 public class UnsignedOptionData
         extends OptionData {
 
     private String rawUnsigned;
 
 
     /**
      * The <code>getValueClass</code> method return <code>Class</code> of witch
      * is result of parsing
      */
     @Override
     public Class getValueClass() {
         return BigInteger.class;
     }
 
     /**
      * The <code>parseValue</code> return string value of <code>RawValue</code>
      */
     @Override
     public Object parseValue( RawValue value ) {
         rawUnsigned = value.getValue();
         UnsignedInt64 uInt64value = new UnsignedInt64( value.getValue() );
         return uInt64value.toBigInteger();
 
     }
 
     /**
      * The <code>parseValue</code> provade parsing of <code>RawValue</code>
      * value to the <code>BigInteger</code> value. RawValue string can be in
      *  hexadecima, octadecimal, binary or standart format.
      */
     @Override
     public String valueToString( Object value ) {
         BigInteger bigInteger = (BigInteger) value;
         NumberUtils utils = new NumberUtils();
 
         if ( utils.isHexFormat( rawUnsigned ) ) {
             return NumberUtils.HEXPREFIX.concat( bigInteger.toString( 16 ) );
         }
 
        if ( utils.isOCtaFormat( rawUnsigned ) ) {
             return NumberUtils.OCTAPREFIX.concat( bigInteger.toString( 8 ) );
         }
 
         if ( utils.isBinaryFormat( rawUnsigned ) ) {
             return NumberUtils.BINARYPREFIX.concat( bigInteger.toString( 2 ) );
         }
 
         return bigInteger.toString();
     }
 
 
      /**
      *Private class <code>UnsignedItem64</code> provide unsigned 64bit long
       * number.It have minimum posible value, maximum posible value and methods
       * to parsing from and to <code>String</code>
      */
     private class UnsignedInt64 {
 
         private BigInteger uint64;
 
 
         public UnsignedInt64( String string ) {
             BigInteger rawUint64 = new BigInteger( string );
             if ( rawUint64.equals( rawUint64.min( BigInteger.TEN.not() ) ) ) {
                 uint64 = rawUint64;
             } else {
                 throw new ClassCastException();
 
             }
 
         }
 
 
         public BigInteger toBigInteger() {
             return uint64;
         }
 
         private final BigInteger B64 = BigInteger.ZERO.setBit( 64 );
 
         public final BigInteger minValue = BigInteger.ZERO;
 
         public final BigInteger maxValue = BigInteger.ZERO.setBit( 64 ).flipBit(
                 64 );
 
 
         public String toUnsignedString( long num ) {
             if ( num >= 0 ) {
                 return String.valueOf( num );
             }
             return BigInteger.valueOf( num ).add( B64 ).toString();
         }
     }
 }
