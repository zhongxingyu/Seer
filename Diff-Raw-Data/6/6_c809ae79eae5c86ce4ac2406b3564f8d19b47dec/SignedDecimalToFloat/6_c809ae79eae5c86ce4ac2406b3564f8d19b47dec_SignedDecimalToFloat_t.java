 package ar.com.datatsunami.pig;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.pig.EvalFunc;
 import org.apache.pig.FuncSpec;
 import org.apache.pig.data.DataType;
 import org.apache.pig.data.Tuple;
 import org.apache.pig.impl.logicalLayer.FrontendException;
 import org.apache.pig.impl.logicalLayer.schema.Schema;
 
 /**
  * This UDF receives the integer and decimal part of a number, the sign, and the
  * number of decimal places, and returns a Float.
  * 
  * For example:
  * 
  * - integer part = 934
  * 
  * - decimal part = 71
  * 
  * - sign = '-'
  * 
  * - decimal places = 5
  * 
  * - float returned: -934.00071
  * 
  */
 public class SignedDecimalToFloat extends EvalFunc<Float> {
 
 	Log logger = LogFactory.getLog(SignedDecimalToFloat.class);
 
 	public SignedDecimalToFloat() {
 		logger.debug("Instantiating...");
 	}
 
 	@Override
 	public Float exec(Tuple input) throws IOException {
 
 		// int_part, decimal_part, decimal_places
 		if (input == null || input.size() != 4) {
 			if (logger.isDebugEnabled())
 				logger.debug("Returning null because we don't received 3 fields");
 			return null;
 		}
 
 		// Cast to force ClassCastException
 		long intPart = ((Number) input.get(0)).longValue();
 		long decimalPart = ((Number) input.get(1)).longValue();
 		long decimalPlaces = ((Number) input.get(3)).longValue();
 		float sign = Utils.getSign((String) input.get(2)); // +1 or -1
 
 		if (sign == 0) {
 			logger.debug("Returning null because couldn't get a valid sign");
 			return null;
 		}
 
		Float value = Utils.toFloat(intPart, decimalPart, decimalPlaces);
		if (value == null)
			return null;

		return sign * value.floatValue();
 	}
 
 	@Override
 	public List<FuncSpec> getArgToFuncMapping() throws FrontendException {
 		List<FuncSpec> funcList = new ArrayList<FuncSpec>();
 		Schema schema;
 
 		schema = new Schema();
 		schema.add(new Schema.FieldSchema(null, DataType.LONG));
 		schema.add(new Schema.FieldSchema(null, DataType.LONG));
 		schema.add(new Schema.FieldSchema(null, DataType.CHARARRAY));
 		schema.add(new Schema.FieldSchema(null, DataType.INTEGER));
 		funcList.add(new FuncSpec(this.getClass().getName(), schema));
 
 		schema = new Schema();
 		schema.add(new Schema.FieldSchema(null, DataType.LONG));
 		schema.add(new Schema.FieldSchema(null, DataType.LONG));
 		schema.add(new Schema.FieldSchema(null, DataType.CHARARRAY));
 		schema.add(new Schema.FieldSchema(null, DataType.LONG));
 		funcList.add(new FuncSpec(this.getClass().getName(), schema));
 
 		return funcList;
 	}
 
 }
