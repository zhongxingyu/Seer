 /*
  * Copyright (C) 2005-2007 Michael Keith, University Of Manchester
  * 
  * email: mkeith@pulsarastronomy.net
  * www  : www.pulsarastronomy.net
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package bookkeepr.xml;
 
 import bookkeepr.xmlable.EncodedOneDimArray;
 import bookkeepr.xmlable.EncodedTwoDimArray;
 import bookkeepr.xmlable.TypedString;
 import coordlib.Coordinate;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Formatter;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 import org.w3c.util.DateParser;
 import org.w3c.util.InvalidDateException;
 
 /**
  *
  * @author kei041
  */
 public abstract class StringConvertable<E> {
 
     private enum TimeUnits {
 
         ns(1e-9), us(1e-6), ms(1e-3), s(1), ks(1e3), minutes(60), hours(3600);
         public final double conv;
 
         TimeUnits(double conv) {
             this.conv = conv;
         }
     };
 
     private enum FreqUnits {
 
         Hz(1e6), kHz(1e3), MHz(1), GHz(1e3), THz(1e6);
         public final double conv;
 
         FreqUnits(double conv) {
             this.conv = conv;
         }
     };
 
     private enum AngleUnits {
 
         arcseconds(1 / 3600), arcminutes(1 / 60), degrees(1), radians(57.29577951), hours(360 / 24), minutes(360 / (24 * 60)), seconds(360 / (24 * 3600));
         public final double conv;
 
         AngleUnits(double conv) {
             this.conv = conv;
         }
     };
 
     private enum DataUnits {
 
         bytes(1), kilobytes(1024), megabytes(1048576), gigabytes(1073741824);
         public final double conv;
 
         DataUnits(double conv) {
             this.conv = conv;
         }
     };
     private static Pattern regex = Pattern.compile("[,\\s]");
 
     public abstract E fromString(String item);
 
     public abstract Class getTargetClass();
 
     public String toString(E item) {
         return item.toString();
     }
 
     public Map<String, String> getKeyValueAttr(E item) {
         return null;
     }
 
     public E fromString(String item, Map<String, String> keys) {
         return fromString(item);
     }
     public static final StringConvertable<Float> FLOAT = new StringConvertable<Float>() {
 
         public Float fromString(String item) {
            return Float.parseFloat(item);
         }
 
         public Class getTargetClass() {
             return float.class;
         }
     };
     public static final StringConvertable<Double> TIME = new StringConvertable<Double>() {
 
         @Override
         public Double fromString(String item, Map<String, String> keys) {
             String units = keys.get("units");
             double val = DOUBLE.fromString(item);
             try {
                 TimeUnits tu = TimeUnits.valueOf(units);
                 return (double) (val * tu.conv);
             } catch (Exception e) {
                 return val;
             }
         }
 
         @Override
         public Map<String, String> getKeyValueAttr(Double item) {
             HashMap<String, String> ret = new HashMap<String, String>();
             if (item < 1e-3) {
                 ret.put("units", "us");
             } else {
                 ret.put("units", "s");
             }
             return ret;
         }
 
         @Override
         public String toString(Double item) {
             if (Double.isNaN(item)) {
                 return null;
             }
             if (item < 1e-3) {
                 item *= 1e6;
             }
 
             return item.toString();
         }
 
         public Double fromString(String item) {
             return Double.parseDouble(item);
         }
 
         public Class getTargetClass() {
             return double.class;
         }
     };
     public static final StringConvertable<Float> ANGLE = new StringConvertable<Float>() {
 
         @Override
         public Float fromString(String item, Map<String, String> keys) {
             String units = keys.get("units");
             float val = FLOAT.fromString(item);
             try {
                 AngleUnits tu = AngleUnits.valueOf(units);
                 return (float) (val * tu.conv);
             } catch (Exception e) {
                 return val;
             }
         }
 
         @Override
         public Map<String, String> getKeyValueAttr(Float item) {
             HashMap<String, String> ret = new HashMap<String, String>();
 
             ret.put("units", "degrees");
             return ret;
         }
 
         @Override
         public String toString(Float item) {
             return FLOAT.toString(item);
         }
 
         public Float fromString(String item) {
             return Float.parseFloat(item);
         }
 
         public Class getTargetClass() {
             return float.class;
         }
     };
     public static final StringConvertable<Long> DATA = new StringConvertable<Long>() {
 
         @Override
         public Long fromString(String item, Map<String, String> keys) {
             String units = keys.get("units");
             float val = FLOAT.fromString(item);
 
             try {
                 DataUnits tu = DataUnits.valueOf(units);
                 if (tu == DataUnits.bytes) {
                     val = LONG.fromString(item);
                 }
                 return (long) (val * tu.conv);
             } catch (Exception e) {
                 return LONG.fromString(item);
             }
         }
 
         @Override
         public Map<String, String> getKeyValueAttr(Long item) {
             HashMap<String, String> ret = new HashMap<String, String>();
             ret.put("units", "bytes");
             return ret;
         }
 
         public Long fromString(String item) {
             return ID.fromString(item);
         }
 
         public Class getTargetClass() {
             return long.class;
         }
     };
     public static final StringConvertable<Integer> MJD = new StringConvertable<Integer>() {
 
         @Override
         public Integer fromString(String item, Map<String, String> keys) {
             String units = keys.get("units");
             int val = INT.fromString(item);
 
             return val;
         }
 
         @Override
         public Map<String, String> getKeyValueAttr(Integer item) {
             HashMap<String, String> ret = new HashMap<String, String>();
             ret.put("units", "MJD");
             return ret;
         }
 
         public Integer fromString(String item) {
             return Integer.parseInt(item);
         }
 
         public Class getTargetClass() {
             return int.class;
         }
 
         @Override
         public String toString(Integer item) {
             return INT.toString(item);
         }
     };
     public static final StringConvertable<Float> FREQ = new StringConvertable<Float>() {
 
         @Override
         public Float fromString(String item, Map<String, String> keys) {
             String units = keys.get("units");
             float val = FLOAT.fromString(item);
 
             try {
                 FreqUnits tu = FreqUnits.valueOf(units);
                 return (float) (val * tu.conv);
             } catch (Exception e) {
                 return val;
             }
         }
 
         public Map<String, String> getKeyValueAttr(Float item) {
             HashMap<String, String> ret = new HashMap<String, String>();
             ret.put("units", "MHz");
             return ret;
         }
 
         public Float fromString(String item) {
             return Float.parseFloat(item);
         }
 
         public Class getTargetClass() {
             return float.class;
         }
     };
     public static final StringConvertable<Long> NANOSECLONG = new StringConvertable<Long>() {
 
         public Long fromString(String item) {
             return LONG.fromString(item);
         }
 
         @Override
         public Long fromString(String item, Map<String, String> keys) {
             String units = keys.get("units");
             long val = LONG.fromString(item);
 
             try {
                 TimeUnits tu = TimeUnits.valueOf(units);
                 if (tu == TimeUnits.ns) {
                     return val;
                 } else {
                     return (long) (val * (tu.conv / TimeUnits.ns.conv));
                 }
             } catch (Exception e) {
                 return val;
             }
         }
 
         @Override
         public Map<String, String> getKeyValueAttr(Long item) {
             HashMap<String, String> ret = new HashMap<String, String>();
             ret.put("units", "ns");
             return ret;
         }
 
         public Class getTargetClass() {
             return long.class;
         }
     };
     public static final StringConvertable<Double> DOUBLE = new StringConvertable<Double>() {
 
         public Double fromString(String item) {
             return Double.parseDouble(item);
         }
 
         public Class getTargetClass() {
             return double.class;
         }
 
         @Override
         public String toString(Double item) {
             if (Double.isNaN(item)) {
                 return null;
             } else {
                 return Double.toString(item);
             }
         }
     };
     public static final StringConvertable<Integer> INT = new StringConvertable<Integer>() {
 
         public Integer fromString(String item) {
             return Integer.parseInt(item);
         }
 
         public Class getTargetClass() {
             return int.class;
         }
     };
     public static final StringConvertable<Long> LONG = new StringConvertable<Long>() {
 
         public Long fromString(String item) {
             return Long.parseLong(item);
         }
 
         public Class getTargetClass() {
             return long.class;
         }
     };
     public static final StringConvertable<Long> ID = new StringConvertable<Long>() {
 
         public Long fromString(String item) {
             return Long.parseLong(item, 16);
         }
 
         public Class getTargetClass() {
             return long.class;
         }
 
         @Override
         public String toString(Long k) {
             long key = k;
             if (key < 0x0001000000000000L) {
                 return "0000" + Long.toHexString(key);
             }
             if (key < 0x0010000000000000L) {
                 return "000" + Long.toHexString(key);
             }
             if (key < 0x0100000000000000L) {
                 return "00" + Long.toHexString(key);
             }
             if (key < 0x1000000000000000L) {
                 return "0" + Long.toHexString(key);
             }
             return Long.toHexString(key);
         }
     };
     public static final StringConvertable<long[]> IDARRAY = new StringConvertable<long[]>() {
 
         public long[] fromString(String item) {
             String[] elems = regex.split(item);
             long[] arr = new long[elems.length];
             for (int i = 0; i < arr.length; i++) {
                 arr[i] = Long.parseLong(elems[i], 16);
             }
             return arr;
         }
 
         @Override
         public String toString(long[] k) {
             StringBuffer buf = new StringBuffer();
             for (long l : k) {
                 buf.append(ID.toString(l));
                 buf.append(",");
             }
             return buf.deleteCharAt(buf.length() - 1).toString();
         }
 
         public Class getTargetClass() {
             return long[].class;
         }
     };
     public static final StringConvertable<float[]> FLOATARRAY = new StringConvertable<float[]>() {
 
         public float[] fromString(String item) {
             String[] elems = regex.split(item);
             float[] arr = new float[elems.length];
             for (int i = 0; i < arr.length; i++) {
                 arr[i] = Float.parseFloat(elems[i]);
             }
             return arr;
         }
 
         @Override
         public String toString(float[] k) {
             StringBuffer buf = new StringBuffer();
             for (float l : k) {
                 buf.append(FLOAT.toString(l));
                 buf.append(",");
             }
             return buf.deleteCharAt(buf.length() - 1).toString();
         }
 
         public Class getTargetClass() {
             return float[].class;
         }
     };
     public static final StringConvertable<double[]> DOUBLEARRAY = new StringConvertable<double[]>() {
 
         public double[] fromString(String item) {
             String[] elems = regex.split(item);
             double[] arr = new double[elems.length];
             for (int i = 0; i < arr.length; i++) {
                 arr[i] = Double.parseDouble(elems[i]);
             }
             return arr;
         }
 
         @Override
         public String toString(double[] k) {
             StringBuffer buf = new StringBuffer();
             for (double l : k) {
                 buf.append(DOUBLE.toString(l));
                 buf.append(",");
             }
             return buf.deleteCharAt(buf.length() - 1).toString();
         }
 
         public Class getTargetClass() {
             return double[].class;
         }
     };
     public static final StringConvertable<ArrayList<Long>> IDLIST = new StringConvertable<ArrayList<Long>>() {
 
         public ArrayList<Long> fromString(String item) {
             String[] elems = regex.split(item);
             ArrayList<Long> res = new ArrayList<Long>();
             for (String elem : elems) {
                 res.add(Long.parseLong(elem, 16));
             }
             return res;
         }
 
         @Override
         public String toString(ArrayList<Long> k) {
             StringBuffer buf = new StringBuffer();
             for (long l : k) {
                 buf.append(ID.toString(l));
                 buf.append(",");
             }
             if (buf.length() > 0) {
                 return buf.deleteCharAt(buf.length() - 1).toString();
             } else {
                 return "";
             }
         }
 
         public Class getTargetClass() {
             return ArrayList.class;
         }
     };
     public static final StringConvertable<Coordinate> COORDINATE = new StringConvertable<Coordinate>() {
 
         public Coordinate fromString(String item) {
             return new Coordinate(item);
         }
 
         public Class getTargetClass() {
             return Coordinate.class;
         }
     };
     public static final StringConvertable<String> STRING = new StringConvertable<String>() {
 
         public String fromString(String item) {
 
             return item.intern();
         }
 
         public Class getTargetClass() {
             return String.class;
         }
     };
     public static final StringConvertable<Boolean> BOOLEAN = new StringConvertable<Boolean>() {
 
         public Boolean fromString(String item) {
             return Boolean.parseBoolean(item);
         }
 
         public Class getTargetClass() {
             return boolean.class;
         }
     };
     public static final StringConvertable<Void> VOID = new StringConvertable<Void>() {
 
         public Void fromString(String item) {
             return null;
         }
 
         @Override
         public String toString(Void item) {
             return null;
         }
 
         public Class getTargetClass() {
             return Void.class;
         }
     };
     public static final StringConvertable<TypedString> TYPEDSTRING = new StringConvertable<TypedString>() {
 
         public TypedString fromString(String item, Map<String, String> keys) {
             String type = keys.get("type");
             return new TypedString(item, type);
 
         }
 
         public Map<String, String> getKeyValueAttr(TypedString item) {
             HashMap<String, String> ret = new HashMap<String, String>();
             if (item.getType() != null) {
                 ret.put("type", item.getType());
             }
             return ret;
         }
 
         public TypedString fromString(String item) {
             return new TypedString(item, null);
         }
 
         public Class getTargetClass() {
             return TypedString.class;
         }
     };
     public static final StringConvertable<EncodedTwoDimArray> HEX_ENCODED_2D_ARRAY = new StringConvertable<EncodedTwoDimArray>() {
 
         public EncodedTwoDimArray fromString(String item, Map<String, String> keys) {
             String format = keys.get("format");
             int digitsPerSamp = 2;
             if (format.endsWith("X")) {
                 digitsPerSamp = Integer.parseInt(format.substring(0, format.length() - 1));
             } else {
                 throw new RuntimeException("We can only support double arrays encoded as hexadecimal with a 0??X format");
             }
             int xSize = Integer.parseInt(keys.get("x_size"));
             int ySize = Integer.parseInt(keys.get("y_size"));
             double min = Double.parseDouble(keys.get("min"));
             double max = Double.parseDouble(keys.get("max"));
             int[][] intArray = new int[xSize][ySize];
             int k = 0;
             double nmax = Math.pow(16, digitsPerSamp);
             for (int x = 0; x < xSize; x++) {
                 for (int y = 0; y < ySize; y++) {
                     int intV = 0;
                     int posn = (int) nmax;
                     for (int n = digitsPerSamp - 1; n >= 0; n--) {
                         char c = item.charAt(k++);
                         posn /= 16;
                         while (!Character.isLetterOrDigit(c)) {
                             c = item.charAt(k++);
                         }
                         intV += Character.digit(c, 16) * posn;
 
                     }
                     intArray[x][y] = intV;
                 }
             }
             EncodedTwoDimArray result = new EncodedTwoDimArray();
             result.setIntArr(intArray);
             result.setIntScale((int) (nmax - 1));
             result.setMax(max);
             result.setMin(min);
             return result;
         }
 
         public Map<String, String> getKeyValueAttr(EncodedTwoDimArray item) {
             HashMap<String, String> ret = new HashMap<String, String>();
 
 
             if (item.getIntScale() > 15) {
                 if (item.getIntScale() > 255) {
                     if (item.getIntScale() > 4095) {
                         if (item.getIntScale() > 65535) {
                             throw new RuntimeException("Cannot deal with more than 4 chars in output data format at the moment!");
                         } else {
                             ret.put("format", "04X");
                         }
                     } else {
                         ret.put("format", "03X");
                     }
                 } else {
                     ret.put("format", "02X");
                 }
             } else {
                 ret.put("format", "01X");
             }
             ret.put("x_size", String.valueOf(item.getIntArr().length));
             ret.put("y_size", String.valueOf(item.getIntArr()[0].length));
             ret.put("max", String.valueOf(item.getMax()));
             ret.put("min", String.valueOf(item.getMin()));
             return ret;
         }
 
         @Override
         public String toString(EncodedTwoDimArray item) {
             StringBuffer buf = new StringBuffer();
             Formatter formatter = new Formatter(buf);
             int inc = 0;
             String fmt = "";
             if (item.getIntScale() > 15) {
                 if (item.getIntScale() > 255) {
                     if (item.getIntScale() > 4095) {
                         if (item.getIntScale() > 65535) {
                             throw new RuntimeException("Cannot deal with more than 4 chars in output data format at the moment!");
                         } else {
                             inc = 4;
                             fmt = "%04X";
                         }
                     } else {
                         inc = 3;
                         fmt = "%03X";
                     }
                 } else {
                     inc = 2;
                     int[][] arr = item.getIntArr();
                     int count = 80;
                     buf.append("\n");
                     for (int x = 0; x < arr.length; x++) {
                         for (int y = 0; y < arr[x].length; y++) {
 //                            String sVal = Integer.toHexString(arr[x][y]);
 //                            while(sVal.length() < inc)sVal= "0"+sVal;
                             buf.append(HexLookup.twochar[arr[x][y]]);
                             count -= inc;
                             if (count <= 0) {
                                 count = 80;
                                 buf.append("\n");
                             }
                         }
                     }
 
                     buf.append("\n");
                     return buf.toString();
                 }
             } else {
                 inc = 1;
                 fmt = "%01X";
             }
             int[][] arr = item.getIntArr();
             int count = 80;
             buf.append("\n");
             for (int x = 0; x < arr.length; x++) {
                 for (int y = 0; y < arr[x].length; y++) {
                     formatter.format(fmt, arr[x][y]);
                     count -= inc;
                     if (count <= 0) {
                         count = 80;
                         buf.append("\n");
                     }
                 }
             }
             buf.append("\n");
             return buf.toString();
         }
 
         public EncodedTwoDimArray fromString(String item) {
             return null;
         }
 
         public Class getTargetClass() {
             return EncodedTwoDimArray.class;
         }
     };
     public static final StringConvertable<EncodedOneDimArray> HEX_ENCODED_1D_ARRAY = new StringConvertable<EncodedOneDimArray>() {
 
         public EncodedOneDimArray fromString(String item, Map<String, String> keys) {
             String format = keys.get("format");
             int digitsPerSamp = 2;
             if (format.endsWith("X")) {
                 digitsPerSamp = Integer.parseInt(format.substring(0, format.length() - 1));
             } else {
                 throw new RuntimeException("We can only support double arrays encoded as hexadecimal with a 0??X format");
             }
             int xSize = Integer.parseInt(keys.get("x_size"));
             double min = Double.parseDouble(keys.get("min"));
             double max = Double.parseDouble(keys.get("max"));
             int[] intArray = new int[xSize];
             int k = 0;
             double nmax = Math.pow(16, digitsPerSamp);
             for (int x = 0; x < xSize; x++) {
                 int intV = 0;
                 int posn = (int) nmax;
                 for (int n = digitsPerSamp - 1; n >= 0; n--) {
                     char c = item.charAt(k++);
                     posn /= 16;
                     while (!Character.isLetterOrDigit(c)) {
                         c = item.charAt(k++);
                     }
                     intV += Character.digit(c, 16) * posn;
 
                 }
                 intArray[x] = intV;
             }
 
             EncodedOneDimArray result = new EncodedOneDimArray();
             result.setIntArr(intArray);
             result.setIntScale((int) (nmax - 1));
             result.setMax(max);
             result.setMin(min);
             return result;
         }
 
         public Map<String, String> getKeyValueAttr(EncodedOneDimArray item) {
             HashMap<String, String> ret = new HashMap<String, String>();
 
 
             if (item.getIntScale() > 15) {
                 if (item.getIntScale() > 255) {
                     if (item.getIntScale() > 4095) {
                         if (item.getIntScale() > 65535) {
                             throw new RuntimeException("Cannot deal with more than 4 chars in output data format at the moment!");
                         } else {
                             ret.put("format", "04X");
                         }
                     } else {
                         ret.put("format", "03X");
                     }
                 } else {
                     ret.put("format", "02X");
                 }
             } else {
                 ret.put("format", "01X");
             }
             ret.put("x_size", String.valueOf(item.getIntArr().length));
             ret.put("max", String.valueOf(item.getMax()));
             ret.put("min", String.valueOf(item.getMin()));
             return ret;
         }
 
         @Override
         public String toString(EncodedOneDimArray item) {
             StringBuffer buf = new StringBuffer();
             Formatter formatter = new Formatter(buf);
             String fmt = "";
             int inc = 0;
             if (item.getIntScale() > 15) {
                 if (item.getIntScale() > 255) {
                     if (item.getIntScale() > 4095) {
                         if (item.getIntScale() > 65535) {
                             throw new RuntimeException("Cannot deal with more than 4 chars in output data format at the moment!");
                         } else {
                             inc = 4;
                             fmt = "%04X";
                         }
                     } else {
                         inc = 3;
                         fmt = "%03X";
                     }
                 } else {
                     inc = 2;
                     int[] arr = item.getIntArr();
                     int count = 80;
                     buf.append("\n");
                     for (int x = 0; x < arr.length; x++) {
 //                            String sVal = Integer.toHexString(arr[x][y]);
 //                            while(sVal.length() < inc)sVal= "0"+sVal;
                         buf.append(HexLookup.twochar[arr[x]]);
                         count -= inc;
                         if (count <= 0) {
                             count = 80;
                             buf.append("\n");
                         }
                     }
 
 
                     buf.append("\n");
                     return buf.toString();
                 }
             } else {
                 inc = 1;
                 fmt = "%01X";
             }
             int[] arr = item.getIntArr();
             int count = 80;
             buf.append("\n");
             for (int x = 0; x < arr.length; x++) {
                 formatter.format(fmt, arr[x]);
                 count -= inc;
                 if (count <= 0) {
                     count = 80;
                     buf.append("\n");
                 }
             }
             buf.append("\n");
             return buf.toString();
         }
 
         public EncodedOneDimArray fromString(String item) {
             return null;
         }
 
         public Class getTargetClass() {
             return EncodedOneDimArray.class;
         }
     };
     public static final StringConvertable<Date> ISODATE = new StringConvertable<Date>() {
 
         public Date fromString(String item) {
             try {
                 return DateParser.parse(item);
             } catch (InvalidDateException ex) {
                 Logger.getLogger(StringConvertable.class.getName()).log(Level.WARNING, null, ex);
                 return null;
             }
         }
 
         @Override
         public String toString(Date k) {
             return DateParser.getIsoDateNoMillis(k);
         }
 
         public Class getTargetClass() {
             return Date.class;
         }
     };
 }
