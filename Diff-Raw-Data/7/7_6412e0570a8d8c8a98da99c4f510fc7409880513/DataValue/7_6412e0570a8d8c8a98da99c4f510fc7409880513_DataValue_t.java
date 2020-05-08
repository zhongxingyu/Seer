 package org.hptd.format;
 
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteArrayDataOutput;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Date;
 
 /**
  * the each column data information
  *
  * @author ford
  * @since 1.0
  */
 public class DataValue implements Comparable {
     private static Logger logger = LoggerFactory.getLogger(DataValue.class);
     private final ValueType type;
     private Object value;
 
     public DataValue(ValueType type) {
         this(type, null);
     }
 
     public DataValue(ValueType type, Object value) {
         this.type = type;
         this.value = value;
     }
 
     public ValueType getType() {
         return type;
     }
 
     public Object getValue() {
         return value;
     }
 
     public void setValue(Object value) {
         this.value = value;
     }
 
     public boolean isNumber() {
         switch (type) {
             case BYTE:
             case SHORT:
             case INT:
             case LONG:
             case FLOAT:
             case DOUBLE:
                 return true;
             default:
                 return false;
         }
     }
 
 
     public void writeDataOut(ByteArrayDataOutput dataOutput) {
         switch (type) {
             case NULL:
                 break;
             case BYTE:
                 dataOutput.writeByte(byteValue());
                 break;
             case SHORT:
                 dataOutput.writeShort(shortValue());
                 break;
             case CHAR:
                 dataOutput.writeChar(charValue());
                 break;
             case INT:
                 dataOutput.writeInt(intValue());
                 break;
             case LONG:
             case TIMESTAMP:
                 dataOutput.writeLong(longValue());
                 break;
             case FLOAT:
                 dataOutput.writeFloat(floatValue());
                 break;
             case DOUBLE:
                 dataOutput.writeDouble(doubleValue());
                 break;
             case STRING:
                 byte[] strBytes = null;
                 try {
                    String strVal = stringValue();
                    if (strVal == null) {
                        strBytes = new byte[0];
                    } else {
                        strBytes =strVal.getBytes("UTF-8");
                    }
                 } catch (UnsupportedEncodingException e) {
                     logger.error(" wrong encoding with value:" + stringValue(), e);
                     strBytes = new byte[0];
                 }
                 dataOutput.writeShort(strBytes.length);
                 dataOutput.write(strBytes);
                 break;
             case BYTE_ARRAY:
                 byte[] arrBytes = arryValue();
                 dataOutput.writeShort(arrBytes.length);
                 dataOutput.write(arrBytes);
                 break;
         }
     }
 
     public void readDataInput(ByteArrayDataInput dataInput) {
         switch (type) {
             case NULL:
                 break;
             case BYTE:
                 this.value = dataInput.readByte();
                 break;
             case SHORT:
                 this.value = dataInput.readShort();
                 break;
             case CHAR:
                 this.value = dataInput.readChar();
                 break;
             case INT:
                 this.value = dataInput.readInt();
                 break;
             case LONG:
                 this.value = dataInput.readLong();
                 break;
             case TIMESTAMP:
                 try {
                     this.value = dataInput.readLong();
                 } catch (NullPointerException e) {
                     this.value = System.currentTimeMillis();
                 }
                 break;
             case FLOAT:
                 this.value = dataInput.readFloat();
                 break;
             case DOUBLE:
                 this.value = dataInput.readDouble();
                 break;
             case STRING:
                 this.value = dataInput.readUTF();
                 break;
             case BYTE_ARRAY:
                 short arrLen = dataInput.readShort();
                 byte[] arrBytes = new byte[arrLen];
                 dataInput.readFully(arrBytes);
                 this.value = arrBytes;
                 break;
         }
     }
 
     public byte byteValue() {
         return ((Number) value).byteValue();
     }
 
     public short shortValue() {
         return ((Number) value).shortValue();
     }
 
     public int intValue() {
         return ((Number) value).intValue();
     }
 
     public char charValue() {
         return ((Character) value).charValue();
     }
 
     public long longValue() {
         return ((Number) value).longValue();
     }
 
     public float floatValue() {
         return ((Number) value).floatValue();
     }
 
     public double doubleValue() {
         return ((Number) value).doubleValue();
     }
 
     public String stringValue() {
         return (String) value;
     }
 
     public Date dateValue() {
         if (type == ValueType.TIMESTAMP) {
             return new Date(longValue());
         } else {
             throw new IllegalArgumentException(" the value type is not timestamp!");
         }
     }
 
     public byte[] arryValue() {
         return (byte[]) value;
     }
 
     @Override
     public String toString() {
         return "DataValue{" +
                 "type=" + type +
                 ", value=" + value +
                 '}';
     }
 
     public void plus(DataValue dataValue) {
         switch (type) {
             case BYTE:
                 value = byteValue() + dataValue.byteValue();
                 break;
             case SHORT:
                 value = shortValue() + dataValue.shortValue();
                 break;
             case INT:
                 value = intValue() + dataValue.intValue();
                 break;
             case LONG:
                 value = longValue() + dataValue.longValue();
                 break;
             case FLOAT:
                 value = floatValue() + dataValue.floatValue();
                 break;
             case DOUBLE:
                 value = doubleValue() + dataValue.doubleValue();
                 break;
         }
     }
 
     public void divide(Number number) {
         switch (type) {
             case BYTE:
                 value = byteValue() / number.byteValue();
                 break;
             case SHORT:
                 value = shortValue() / number.shortValue();
                 break;
             case INT:
                 value = intValue() / number.intValue();
                 break;
             case LONG:
                 value = longValue() / number.longValue();
                 break;
             case FLOAT:
                 value = floatValue() / number.floatValue();
                 break;
             case DOUBLE:
                 value = doubleValue() / number.doubleValue();
                 break;
         }
     }
 
     @Override
     public int compareTo(Object o) {
         if (value instanceof Comparable) {
             return ((Comparable) value).compareTo(((DataValue) o).getValue());
         } else {
             return 0;
         }
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         DataValue dataValue = (DataValue) o;
         if (!value.equals(dataValue.value)) return false;
         return true;
     }
 
     @Override
     public int hashCode() {
         return value.hashCode();
     }
 }
