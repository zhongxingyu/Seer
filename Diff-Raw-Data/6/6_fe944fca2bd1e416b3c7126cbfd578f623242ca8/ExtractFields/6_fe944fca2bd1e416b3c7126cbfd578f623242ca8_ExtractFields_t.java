 package com.mortardata.pig;
 
 import org.apache.pig.EvalFunc;
 import org.apache.pig.backend.executionengine.ExecException;
 import org.apache.pig.data.BagFactory;
 import org.apache.pig.data.DataBag;
 import org.apache.pig.data.DataType;
 import org.apache.pig.data.DataByteArray;
 import org.apache.pig.data.Tuple;
 import org.apache.pig.data.TupleFactory;
 import org.apache.pig.impl.util.UDFContext;
 import org.apache.pig.impl.logicalLayer.schema.Schema;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Enumeration;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import java.util.Properties;
 
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class ExtractFields extends EvalFunc<DataBag> {
     private static final Log log = LogFactory.getLog(ExtractFields.class);
 
     private TupleFactory tupleFactory = TupleFactory.getInstance();
     private BagFactory bagFactory = BagFactory.getInstance();
     private boolean inferTypes;
 
     public ExtractFields(String inferTypes) {
        super();
         this.inferTypes = Boolean.parseBoolean(inferTypes);
     }
 
    public ExtractFields() {
        super();
    }

     @Override
     public DataBag exec(Tuple input) {
         try {
             Properties props = 
                 UDFContext.getUDFContext().getUDFProperties(this.getClass());
             return execProps(input, props);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     public DataBag execProps(Tuple input, Properties props) throws Exception {
         DataBag output = bagFactory.newDefaultBag();
         Iterator<Object> it = input.getAll().iterator();
         int count = 0;
 
         while (it.hasNext()) {
             Object obj = it.next();
             if (obj instanceof Map) {
                 unrollMap((Map) obj, output, props.getProperty("$" + count) + ".");
             } else {
                 Tuple t = tupleFactory.newTuple(4);
                 String fieldName = props.getProperty("$" + count);
                 if (fieldName != null) {
                     t.set(0, fieldName);
                 } else { 
                     t.set(0, "field" + count); 
                 }
                 t.set(1, getType(obj));
                 t.set(2, getFieldValue(obj));
                 if (obj != null && !obj.equals("")) {
                     t.set(3, obj.toString());
                 } else {
                     t.set(2, null);
                     t.set(3, null);
                 }
                 output.add(t);
             }
             count++;
         }
         return output;            
     }
 
     public Object getFieldValue(Object obj) throws Exception {
         if (obj instanceof Tuple) {
             return new Double(((Tuple) obj).size());
         } else if (obj instanceof DataBag) {
             return new Double(((DataBag) obj).size());
         } else if (obj instanceof DataByteArray) {
             String objStr = obj.toString();
             if (isNumeric(objStr)) {
                 return Double.parseDouble(objStr);
             } else if (isHex(objStr)) {
                 return new Double(Integer.parseInt(objStr, 16));
             }
             return new Double(((DataByteArray) obj).size());
         } else if (obj instanceof String) {
             return new Double(obj.toString().length());
         } else if (obj != null){
             return new Double(((Number) obj).doubleValue()); // the value
         } else {
             return obj;
         }
     }
 
     private DataBag unrollMap(Map input, DataBag output, String prefix) {
         for (Object key : input.keySet()) {
             String key_name = prefix + key.toString();
             Object obj = input.get(key);
             int count = 0;
             try {
                 if (obj instanceof Map) {
                     unrollMap((Map) obj, output, key_name + ".");
                 } else {
                     Tuple t = tupleFactory.newTuple(4);
                     if (key_name != null) {
                         t.set(0, key_name);
                     } else {
                         t.set(0, "field" + count);
                     }
                     t.set(1, getType(obj));
                     t.set(2, getFieldValue(obj));
                     if (obj != null) {
                         t.set(3, obj.toString());
                     } else {
                         t.set(2, null);
                         t.set(3, null);
                     }
                     output.add(t);
                 }
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
             count++;
         }
         return output;
     }
 
     private String getType(Object obj) {
         String name = DataType.findTypeName(obj);
         if (name.equals("bytearray") && inferTypes) {
             String objStr = obj.toString();
             if (isNumeric(objStr)) {
                 double odub = (double) Double.parseDouble(objStr);
                 long olong = (long) odub;
                 if (olong == odub) {
                     return getType(DataType.LONG);
                 } else {
                     return getType(DataType.DOUBLE);
                 } 
             } else if (isHex(objStr)) {
                 return getType(DataType.LONG);
             }else {
                 return getType(DataType.CHARARRAY);
             }
         } else {
             return name;
         }
     }
 
     private static boolean isNumeric(String str) {
         return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
     }
 
     public static boolean isHex(String str) {
         return str.matches("-?[0-9a-fA-F]{6}"); //match a hex number with an optional '-' 
     }
 
     private String getType(byte b) {
         return DataType.findTypeName(b);
     }
 
     public Schema outputSchema(Schema input) {
         try {
             Properties udfProps = UDFContext.getUDFContext().getUDFProperties(this.getClass());
             if (input == null) {
                 input = new Schema();
             }
 
             Iterator<Schema.FieldSchema> fieldIt = 
                 input.getFields().iterator();
 
             Schema.FieldSchema field;
             int count = 0;
         
             while (fieldIt.hasNext()) {
                 field = fieldIt.next();
                 udfProps.setProperty("$" + count, field.alias != null ? field.alias : "");
                 count++;
             }
 
             Schema bagSchema = new Schema();
             Schema tupleSchema = new Schema();
             tupleSchema.add(new Schema.FieldSchema("keyname", DataType.CHARARRAY));
             tupleSchema.add(new Schema.FieldSchema("type", DataType.CHARARRAY));
             tupleSchema.add(new Schema.FieldSchema("val", DataType.DOUBLE));
             tupleSchema.add(new Schema.FieldSchema("orig", DataType.CHARARRAY));
             bagSchema.add(new Schema.FieldSchema("field", tupleSchema, DataType.TUPLE));
             return new Schema(new Schema.FieldSchema("fields", bagSchema, DataType.BAG));
 
         } catch (Exception e) {
             throw new RuntimeException(e);
         }            
     } 
 }
 
 
     
