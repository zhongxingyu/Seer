 package pleocmd.pipe.val;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 
 public final class IntValue extends Value {
 
 	public static final char TYPE_CHAR = 'I';
 
 	public static final ValueType RECOMMENDED_TYPE = ValueType.Int64;
 
 	private long val;
 
 	protected IntValue(final ValueType type) {
 		super(type);
 		assert type == ValueType.Int8 || type == ValueType.Int32
 				|| type == ValueType.Int64;
 	}
 
 	@Override
 	public void readFromBinary(final DataInput in) throws IOException {
 		switch (getType()) {
 		case Int8:
 			val = in.readByte();
 			break;
 		case Int32:
 			val = in.readInt();
 			break;
 		case Int64:
 			val = in.readLong();
 			break;
 		default:
 			throw new RuntimeException("Invalid type for this class");
 		}
 	}
 
 	@Override
 	public void writeToBinary(final DataOutput out) throws IOException {
 		switch (getType()) {
 		case Int8:
 			out.writeByte((int) val);
 			break;
 		case Int32:
 			out.writeInt((int) val);
 			break;
 		case Int64:
 			out.writeLong(val);
 			break;
 		default:
 			throw new RuntimeException("Invalid type for this class");
 		}
 	}
 
 	@Override
 	public void readFromAscii(final byte[] in, final int len)
 			throws IOException {
		// work around for java bug: Long.valueOf can't handle "\+[0-9]+"
		if (in.length > 0 && in[0] == '+')
			val = Long.valueOf(new String(in, 1, len - 1, "US-ASCII"));
		else
			val = Long.valueOf(new String(in, 0, len, "US-ASCII"));
 	}
 
 	@Override
 	public void writeToAscii(final DataOutput out) throws IOException {
 		out.write(String.valueOf(val).getBytes("US-ASCII"));
 	}
 
 	@Override
 	public String toString() {
 		return String.valueOf(val);
 	}
 
 	@Override
 	public long asLong() {
 		return val;
 	}
 
 	@Override
 	public double asDouble() {
 		return val;
 	}
 
 	@Override
 	public String asString() {
 		return String.valueOf(val);
 	}
 
 	@Override
 	public boolean mustWriteAsciiAsHex() {
 		return false;
 	}
 
 	@Override
 	public Value set(final String content) {
 		val = Long.valueOf(content);
 		return this;
 	}
 
 	@Override
 	public boolean equals(final Object o) {
 		if (o == this) return true;
 		if (!(o instanceof IntValue)) return false;
 		return val == ((IntValue) o).val;
 	}
 
 	@Override
 	public int hashCode() {
 		return (int) (val ^ val >>> 32);
 	}
 
 }
