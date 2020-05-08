 package com.whitepages.whiteelephant;
 
import java.util.Map;

 import org.apache.pig.EvalFunc;
 import org.apache.pig.data.DataType;
 import org.apache.pig.data.Tuple;
 import org.apache.pig.impl.logicalLayer.schema.Schema;
 
 import org.json.simple.JSONObject;
 
 /**
  * Takes a tuple, converts it internally into a Map using alternating key value pairs, and then converts it into a JSON
  * String.  Uses the json-simple implementation of json.  This code currently requires string keys and atomic values.
  * (Tuples, Bags, or Maps as values are not yet supported.)
  *
  * Example: Given a Tuple T of Type {name:chararray, age:int}, the call
  *     TupleToJson( 'Name', T.name, 'Age', T.age )
  * might produce the String "{"Name":"Daniel Noble","Age":10}".
  *
  * @see <a href=" http://code.google.com/p/json-simple/">json-simple home</a>
  */
 
 public class TupleToJson extends EvalFunc< String > {
 	/**
 	 * Vacuous default constructor.  This UDF does not support any constructor options.
 	 */
 	public TupleToJson() {
 	}
 
 	/**
 	 * @return UUID in String format
 	 */
 	@Override
 	public String exec( Tuple input ) {
 		if ( null == input || 2 > input.size() )
             return null;
 
         try {
            @SuppressWarnings( "unchecked" )
            Map< String, Object > obj = ( Map< String, Object > )( new JSONObject() );
 
             for ( int i = 0; i < input.size(); i += 2 ) {
                 String key = ( String )input.get( i );
                 Object val = input.get( i + 1 );
                 obj.put( key, val );
             }
 
             return obj.toString();
         }
         catch ( ClassCastException e ) {
             throw new RuntimeException("Key must be a String");
         }
         catch ( ArrayIndexOutOfBoundsException e ){
             throw new RuntimeException( "Function input must have even number of parameters" );
         }
         catch ( Exception e ) {
             throw new RuntimeException( "Error while converting to Json", e );
         }
 	}
 
 	@Override
 	public Schema outputSchema( Schema input ) {
         return new Schema( new Schema.FieldSchema( null, DataType.CHARARRAY ) );
 	}
 }
