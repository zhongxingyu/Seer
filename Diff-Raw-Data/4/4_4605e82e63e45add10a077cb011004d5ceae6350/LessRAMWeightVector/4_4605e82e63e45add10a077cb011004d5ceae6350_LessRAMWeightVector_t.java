 package myorg.io;
 
 import java.io.IOException;
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.StringTokenizer;
 import java.util.Random;
 
 import org.apache.hadoop.io.Writable;
 
 import myorg.io.FeatureVector;
 
 public class LessRAMWeightVector extends WeightVector {
     private Random rnd;
     private int dim = 0;
     private short[] weightArray = null;
     private float scaleFactor = 1.0f;
     private float squaredNorm = 0.0f;
 
     private static int P2p13 = 1 << 13;
     private static long SEED = 0x5EED;
 
     public LessRAMWeightVector() {
         this(0);
     }
 
     public LessRAMWeightVector(int dim) {
         this.rnd = new Random(SEED);
         this.dim = dim;
         this.weightArray = new short[dim];
         this.scaleFactor = 1.0f;
         this.squaredNorm = 0.0f;
     }
 
     public LessRAMWeightVector(String inString) {
         setFromString(inString);
     }
 
     @Override
     public float innerProduct(FeatureVector x, float xScale) {
         if (xScale == 0.0f) {
             return 0.0f;
         }
 
         float ip = 0.0f;
         for (int i = 0; i < x.getNonZeroNum(); i++) {
             int idx = x.getIndexAt(i);
             float val = x.getValueAt(i);
 
             if (idx >= dim) {
                 continue;
             }
 
             ip += weightArray[idx] * val;
         }
 
        return ip * scaleFactor * xScale / P2p13;
     }
 
     @Override
     public void addVector(FeatureVector x, float xScale) {
         float s = xScale / scaleFactor;
         for (int i = 0; i < x.getNonZeroNum(); i++) {
             int idx = x.getIndexAt(i);
             float val = x.getValueAt(i);
 
             if (idx >= dim) {
                 System.err.println("dimension over in addVector: " + idx + " >= " + dim);
                 continue;
             }
 
             float w = getValue(idx); squaredNorm -= w * w;
             setValue(idx, w + val * s);
             w = getValue(idx); squaredNorm += w * w;
         }
     }
 
     @Override
     public void addVector(WeightVector x, float xScale) {
         float s = xScale / scaleFactor;
 
         if (x.getDimensions() > dim) {
             short[] newArray = new short[x.getDimensions()];
             for (int i = 0; i < dim; i++) {
                 newArray[i] = weightArray[i];
             }
             weightArray = newArray;
             dim = x.getDimensions();
         }
 
         for (int i = 0; i < x.getDimensions(); i++) {
             float w = getValue(i); squaredNorm -= w * w;
             setValue(i, getValue(i) + x.getValue(i) * s);
             w = getValue(i); squaredNorm += w * w;
         }
     }
 
     @Override
     public void scale(float xScale) {
         scaleFactor *= xScale;
         if (Math.abs(scaleFactor) < 1e-10) {
             rescale();
         }
     }
 
     private void rescale() {
         squaredNorm = 0.0f;
         for (int i = 0; i < dim; i++) {
             weightArray[i] *= scaleFactor;
             squaredNorm += weightArray[i] * weightArray[i];
         }
         scaleFactor = 1.0f;
     }
 
     @Override
     public float getSquaredNorm() {
         return squaredNorm * scaleFactor * scaleFactor;
     }
 
     @Override
     public int getDimensions() {
         return dim;
     }
 
     @Override
     public float getValue(int index) {
         if (index >= dim) {
             return 0.0f;
         }
         return weightArray[index] * scaleFactor / P2p13;
     }
 
     @Override
     public void setValue(int index, float value) {
         if (index < dim) {
             float f = (value * P2p13 / scaleFactor) + rnd.nextFloat(); // random rounding
             if      (f >  Short.MAX_VALUE)  { f =  Short.MAX_VALUE; }
             else if (f < -Short.MAX_VALUE)  { f = -Short.MAX_VALUE; }
             weightArray[index] = (short)f;
         }
     }
 
     @Override
     public void write(DataOutput out) throws IOException {
         rescale();
         out.writeInt(dim);
         int nonzeroNum = 0;
         for (int i = 0; i < dim; i++) {
             if (weightArray[i] != 0.0f) {
                 nonzeroNum++;
             }
         }
         out.writeInt(nonzeroNum);
         for (int i = 0; i < dim; i++) {
             if (weightArray[i] != 0.0f) {
                 out.writeInt(i);
                 out.writeFloat(getValue(i));
             }
         }
         out.writeFloat(scaleFactor);
         out.writeFloat(squaredNorm);
     }
 
     @Override
     public void readFields(DataInput in) throws IOException {
         int size = in.readInt();
         if (size > weightArray.length) {
             weightArray = new short[size];
         }
         dim = size;
         int nonzeroNum = in.readInt();
         for (int i = 0; i < nonzeroNum; i++) {
             int j = in.readInt();
             setValue(j, in.readFloat());
         }
         scaleFactor = in.readFloat();
         squaredNorm = in.readFloat();
     }
 
     @Override
     public String toString() {
         rescale();
 
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < dim; i++) {
             if (weightArray[i] == 0.0f) {
                 continue;
             }
 
             if (sb.length() > 0) {
                 sb.append(' ');
             }
             sb.append(i);
             sb.append(':');
             sb.append(getValue(i));
         }
 
         sb.append(" # dim:" + Integer.toString(dim));
 
         return sb.toString();
     }
 
     @Override
     public void setFromString(String inString) {
         this.rnd = new Random(SEED);
         this.weightArray = new short[0];
         this.scaleFactor = 1.0f;
         this.squaredNorm = 0.0f;
 
         if (inString == null || "".equals(inString)) {
             return;
         }
 
         int idx = inString.indexOf('#'); // comment part
         String body = (idx > 0) ? inString.substring(0, idx).trim() : inString;
         String comment = (idx > 0) ? inString.substring(idx + 1).trim() : "";
 
         StringTokenizer st;
 
         st = new StringTokenizer(body, " \t\r\n:");
         if (! st.hasMoreTokens()) {
             return;
         }
 
         Map<Integer, Float> tmpMap = new HashMap<Integer, Float>();
         int maxFeatureId = -1;
 
         while (st.hasMoreTokens()) {
             String key = st.nextToken();
 
             if (! st.hasMoreTokens()) {
                 break;
             }
 
             String val = st.nextToken();
 
             try {
                 int k = Integer.parseInt(key);
                 float v = Float.parseFloat(val);
                 tmpMap.put(k, v);
 
                 if (k > maxFeatureId) {
                     maxFeatureId = k;
                 }
             } catch (NumberFormatException e) {
             }
         }
 
         dim = -1;
         if (! comment.equals("")) {
             st = new StringTokenizer(comment, " \t\r\n:");
             while (st.hasMoreTokens()) {
                 String key = st.nextToken();
 
                 if (! st.hasMoreTokens()) {
                     break;
                 }
 
                 String val = st.nextToken();
 
                 if (key.equals("dim")) {
                     try {
                         dim = Integer.parseInt(val);
                         break;
                     } catch (NumberFormatException e) {
                     }
                 }
             }
         }
         
         if (maxFeatureId + 1 > dim) {
             dim = maxFeatureId + 1;
         }
 
         this.weightArray = new short[dim];
         for (Integer key : tmpMap.keySet()) {
             setValue(key.intValue(), tmpMap.get(key).floatValue());
         }
 
     }
 }
 
 
