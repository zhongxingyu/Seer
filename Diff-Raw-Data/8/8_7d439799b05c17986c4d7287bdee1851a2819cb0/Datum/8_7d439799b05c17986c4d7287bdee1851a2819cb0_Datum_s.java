 package us.jubat.common;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.msgpack.annotation.Message;
 import org.msgpack.annotation.NotNullable;
 import org.msgpack.type.ArrayValue;
 
 import us.jubat.common.type.TDouble;
 import us.jubat.common.type.TRaw;
 import us.jubat.common.type.TString;
 import us.jubat.common.type.TUserDef;
 
 @Message
 public class Datum implements UserDefinedMessage {
 	@Message
 	public static class NumValue implements UserDefinedMessage {
 		public String key;
 		public double value;
 
 		public NumValue() {
 		}
 
 		public NumValue(String key, double value) {
 			this.key = key;
 			this.value = value;
 		}
 
 		public void check() {
 			TString.instance.check(this.key);
 		}
 
 		public static class Type extends TUserDef<NumValue> {
 			public static Type instance = new Type();
 
 			public Type() {
				super(3);
 			}
 
 			@Override
 			public NumValue create(ArrayValue value) {
 				String key = TString.instance.revert(value.get(0));
 				double val = TDouble.instance.revert(value.get(1));
 				return new NumValue(key, val);
 			}
 		}
 	}
 
 	@Message
 	public static class StringValue implements UserDefinedMessage {
 		public String key;
 		public String value;
 
 		public StringValue() {
 		}
 
 		public StringValue(String key, String value) {
 			this.key = key;
 			this.value = value;
 		}
 
 		public void check() {
 			TString.instance.check(this.key);
 			TString.instance.check(this.value);
 		}
 
 		public static class Type extends TUserDef<StringValue> {
 			public static Type instance = new Type();
 
 			public Type() {
				super(3);
 			}
 
 			@Override
 			public StringValue create(ArrayValue value) {
 				String key = TString.instance.revert(value.get(0));
 				String val = TString.instance.revert(value.get(1));
 				return new StringValue(key, val);
 			}
 		}
 	}
 
 	@Message
 	public static class BinaryValue implements UserDefinedMessage {
 		public String key;
 		public byte[] value;
 
 		public BinaryValue() {
 		}
 
 		public BinaryValue(String key, byte[] value) {
 			this.key = key;
 			this.value = value;
 		}
 
 		public void check() {
 			TString.instance.check(this.key);
 			TRaw.instance.check(this.value);
 		}
 
 		public static class Type extends TUserDef<BinaryValue> {
 			public static Type instance = new Type();
 
 			public Type() {
				super(3);
 			}
 
 			@Override
 			public BinaryValue create(ArrayValue value) {
 				String key = TString.instance.revert(value.get(0));
 				byte[] val = TRaw.instance.revert(value.get(1));
 				return new BinaryValue(key, val);
 			}
 		}
 	}
 
 	@NotNullable
 	public List<StringValue> stringValues = new ArrayList<StringValue>();
 	@NotNullable
 	public List<NumValue> numValues = new ArrayList<NumValue>();
 	@NotNullable
 	public List<BinaryValue> binaryValues = new ArrayList<BinaryValue>();
 
 	public Datum() {
 	}
 	
 	public Datum(List<StringValue> stringValues, List<NumValue> numValues,
 			List<BinaryValue> binaryValues) {
 		this.stringValues = stringValues;
 		this.numValues = numValues;
 		this.binaryValues = binaryValues;
 	}
 
 	public Datum addNumber(String key, double value) {
 		this.numValues.add(new NumValue(key, value));
 		return this;
 	}
 
 	public Datum addString(String key, String value) {
 		this.stringValues.add(new StringValue(key, value));
 		return this;
 	}
 
 	public Datum addBinary(String key, byte[] value) {
 		this.binaryValues.add(new BinaryValue(key, value));
 		return this;
 	}
 
 	public Iterable<StringValue> getStringValues() {
 		return this.stringValues;
 	}
 
 	public Iterable<NumValue> getNumValues() {
 		return this.numValues;
 	}
 
 	public Iterable<BinaryValue> getBinaryValues() {
 		return this.binaryValues;
 	}
 
 	public void check() {
 		for (StringValue v : this.stringValues) {
 			v.check();
 		}
 		for (NumValue v : this.numValues) {
 			v.check();
 		}
 		for (BinaryValue v : this.binaryValues) {
 			v.check();
 		}
 	}
 }
