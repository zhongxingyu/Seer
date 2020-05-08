 package myorg.io;
 
 import java.io.IOException;
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.StringTokenizer;
 
 import org.apache.hadoop.io.Writable;
 
 import myorg.io.FeatureVector;
 
 public class WeightMatrix implements Writable {
     private int row = 0;
     private int col = 0;
     private float[] weightArray = null;
     private float scaleFactor = 1.0f;
 
     public WeightMatrix() {
         this(0, 0);
     }
 
     public WeightMatrix(int row, int col) {
         this.row = row;
         this.col = col;
         this.weightArray = new float[row * col];
         this.scaleFactor = 1.0f;
     }
 
     public WeightMatrix(String inString) {
         setFromString(inString);
     }
 
     public float[] product(FeatureVector x) {
         return product(x, 1.0f);
     }
 
     public float[] product(FeatureVector x, float xScale) {
         float[] retArray = new float[row];
 
         if (xScale == 0.0f) {
             return retArray;
         }
 
         for (int r = 0; r < row; r++) {
             for (int i = 0; i < x.getNonZeroNum(); i++) {
                 int idx = x.getIndexAt(i);
                 float val = x.getValueAt(i);
 
                 if (idx >= col) {
                     continue;
                 }
 
                retArray[r] += weightArray[getArrayIndex(r, idx)] * val;
             }
 
             retArray[r] *= scaleFactor * xScale;
         }
 
         return retArray;
     }
 
     public void addVectorToRow(int r, FeatureVector x) {
         addVectorToRow(r, x, 1.0f);
     }
 
     public void addVectorToRow(int r, FeatureVector x, float xScale) {
         float s = xScale / scaleFactor;
         for (int i = 0; i < x.getNonZeroNum(); i++) {
             int idx = x.getIndexAt(i);
             float val = x.getValueAt(i);
 
             if (idx >= col) {
                 System.err.println("dimention over in addVectorToRow: " + idx + " >= " + col);
                 continue;
             }
 
            weightArray[getArrayIndex(r, idx)] += val * s;
         }
     }
 
     public void scale(float xScale) {
         scaleFactor *= xScale;
         if (Math.abs(scaleFactor) < 1e-10) {
             rescale();
         }
     }
 
     private void rescale() {
         int dim = getDimensions();
         for (int i = 0; i < dim; i++) {
             weightArray[i] *= scaleFactor;
         }
         scaleFactor = 1.0f;
     }
 
     public int getRowDimensions() {
         return row;
     }
 
     public int getColumnDimensions() {
         return col;
     }
 
     public float getValue(int r, int c) {
         if (r >= row || c >= col) {
             return 0.0f;
         }
         return weightArray[getArrayIndex(r, c)] * scaleFactor;
     }
 
     public void setValue(int r, int c, float value) {
         if (r < row && c < col) {
             weightArray[getArrayIndex(r, c)] = value / scaleFactor;
         }
     }
 
     private int getArrayIndex(int r, int c) {
         return r * col + c;
     }
 
     private int getDimensions() {
         return row * col;
     }
 
     @Override
     public void write(DataOutput out) throws IOException {
         rescale();
         out.writeInt(row);
         out.writeInt(col);
         int nonzeroNum = 0;
         for (int r = 0; r < row; r++) {
             for (int c = 0; c < col; c++) {
                 float v = getValue(r, c);
                 if (v != 0.0f) {
                     nonzeroNum++;
                 }
             }
         }
         out.writeInt(nonzeroNum);
         for (int r = 0; r < row; r++) {
             for (int c = 0; c < col; c++) {
                 float v = getValue(r, c);
                 if (v != 0.0f) {
                     out.writeInt(r);
                     out.writeInt(c);
                     out.writeFloat(v);
                 }
             }
         }
         out.writeFloat(scaleFactor);
     }
 
     @Override
     public void readFields(DataInput in) throws IOException {
         this.row = in.readInt();
         this.col = in.readInt();
         int size = getDimensions();
         if (size > weightArray.length) {
             weightArray = new float[size];
         }
         int nonzeroNum = in.readInt();
         for (int i = 0; i < nonzeroNum; i++) {
             int r = in.readInt();
             int c = in.readInt();
             float v = in.readFloat();
             setValue(r, c, v);
         }
         scaleFactor = in.readFloat();
     }
 
     @Override
     public String toString() {
         rescale();
 
         StringBuffer sb = new StringBuffer();
         for (int r = 0; r < row; r++) {
             for (int c = 0; c < col; c++) {
                 float v = getValue(r, c);
                 if (v == 0.0f) {
                     continue;
                 }
 
                 if (sb.length() > 0) {
                     sb.append(' ');
                 }
                 sb.append(String.format("%d,%d:%f", r, c, v));
             }
         }
 
         sb.append(String.format(" # row:%d col:%d", row, col));
 
         return sb.toString();
     }
 
     public void setFromString(String inString) {
         this.weightArray = new float[0];
         this.scaleFactor = 1.0f;
 
         if (inString == null || "".equals(inString)) {
             return;
         }
 
         int idx = inString.indexOf('#'); // comment part
         String body = (idx > 0) ? inString.substring(0, idx).trim() : inString;
         String comment = (idx > 0) ? inString.substring(idx + 1).trim() : "";
 
         StringTokenizer st;
 
         if (! comment.equals("")) {
             st = new StringTokenizer(comment, " \t\r\n:");
             while (st.hasMoreTokens()) {
                 String key = st.nextToken();
 
                 if (! st.hasMoreTokens()) {
                     break;
                 }
 
                 String val = st.nextToken();
 
                 if (key.equals("row")) {
                     try {
                         this.row = Integer.parseInt(val);
                     } catch (NumberFormatException e) {
                     }
 
                 } else if (key.equals("col")) {
                     try {
                         this.col = Integer.parseInt(val);
                     } catch (NumberFormatException e) {
                     }
                 }
             }
         } else {
             return;
         }
 
         this.weightArray = new float[getDimensions()];
 
         st = new StringTokenizer(body, " \t\r\n,:");
         if (! st.hasMoreTokens()) {
             return;
         }
 
         while (st.hasMoreTokens()) {
             String rStr = st.nextToken();
             if (! st.hasMoreTokens()) { break; }
             String cStr = st.nextToken();
             if (! st.hasMoreTokens()) { break; }
             String val = st.nextToken();
 
             try {
                 int r = Integer.parseInt(rStr);
                 int c = Integer.parseInt(cStr);
                 float v = Float.parseFloat(val);
                 setValue(r, c, v);
             } catch (NumberFormatException e) {
             }
         }
     }
 }
 
 
