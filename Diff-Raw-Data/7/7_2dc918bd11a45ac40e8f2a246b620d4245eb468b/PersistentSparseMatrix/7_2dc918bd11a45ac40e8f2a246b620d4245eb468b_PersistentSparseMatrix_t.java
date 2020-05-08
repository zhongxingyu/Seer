 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package nava.structurevis.data;
 
 import java.io.*;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import nava.data.types.DenseMatrixData;
 
 /**
  *
  * @author Michael Golden <michaelgolden0@gmail.com>
  */
 public class PersistentSparseMatrix implements Serializable {
     
     public static double DEFAULT_EMPTY_VALUE = Double.MIN_VALUE;
     public static final int ELEMENT_SIZE = 12;
     private File matrixFile;
     private double emptyValue;
     private int maxRowIndex;
     private int[] rowCounts;
     private int[] rowCountsSum;
     private int[] maxColIndexInRow;
     private long headerLength;
     private transient HashMap<CachedLineKey, CachedLine> lineCache = new HashMap<>();
     private transient ArrayList<CachedLineKey> keyQueue = new ArrayList<>();
     private static int cacheLineSize = 2048; // number of consecutive elements in cache line
     private int maxCachedLines = 20000; // max number of lines to cache
     private int maxCachedElements = 100000;
     private int elementsCached = 0;
     private double minValue = Double.MAX_VALUE;
     private double maxValue = Double.MIN_VALUE;
     private static Random random = new Random();
     private int n;
     private int m;
     
     public PersistentSparseMatrix(File matrixFile) throws IOException {
         this.matrixFile = matrixFile;
         DataInputStream dataInputStream = new DataInputStream(new FileInputStream(matrixFile));
         emptyValue = dataInputStream.readDouble();
         maxRowIndex = dataInputStream.readInt();
         minValue = dataInputStream.readDouble();
         maxValue = dataInputStream.readDouble();
         rowCounts = new int[maxRowIndex + 1];
         rowCountsSum = new int[maxRowIndex + 1];
         maxColIndexInRow = new int[maxRowIndex + 1];
         for (int i = 0; i < rowCounts.length; i++) {
             rowCounts[i] = dataInputStream.readInt();
             maxColIndexInRow[i] = dataInputStream.readInt();
         }
         for (int i = 0; i < rowCounts.length; i++) {
             if (i > 0) {
                 rowCountsSum[i] = rowCountsSum[i - 1] + rowCounts[i];
             } else {
                 rowCountsSum[i] = rowCounts[i];
             }
         }
         headerLength = 8 + 4 + 16 + rowCounts.length * 4 + maxColIndexInRow.length * 4;
         dataInputStream.close();
         this.n = rowCounts.length;
         this.m = 0;
         for (int i = 0; i < maxColIndexInRow.length; i++) {
            m = Math.max(m, maxColIndexInRow[i]+1);
         }
     }
     
     public DenseMatrixData getDenseMatrixData(double fillEmptyWith) throws Exception {
         double[][] matrix = new double[n][m];
         for (int i = 0; i < n; i++) {
             for (int j = 0; j < m; j++) {
                 double val = this.getValue(i, j);
                 if (val == emptyValue) {
                     val = fillEmptyWith;
                 }
                 matrix[i][j] = val;
             }
         }
         
         return new DenseMatrixData(matrix);
     }
     
     public RowElement get(RandomAccessFile randomAccessFile, int i, int j) throws IOException {
         long seekPos = headerLength;
         if (i > 0) {
             seekPos += rowCountsSum[i - 1] * ELEMENT_SIZE;
         }
         randomAccessFile.seek(seekPos);
         int minColIndex = randomAccessFile.readInt();
         int maxColIndex = maxColIndexInRow[i];
 
         // binary search
         int low = 0;
         int high = rowCounts[i];
         int key = j;
         RowElement elem = null;
         while (low <= high) {
             int mid = low + (high - low) / 2;
             elem = getRowElementUnsafe(randomAccessFile, i, mid);
             if (key < elem.index) {
                 high = mid - 1;
             } else if (key > elem.index) {
                 low = mid + 1;
             } else {
                 break;
             }
         }
         
         if (key == elem.index) {
             return elem;
         } else {
             return null;
         }
     }
     
     public RowElement get(int i, int j) throws IOException {
         RandomAccessFile randomAccessFile = new RandomAccessFile(matrixFile, "r");
         RowElement elem = get(randomAccessFile, i, j);
         randomAccessFile.close();
         return elem;
     }
     
     public double getValue(int i, int j) throws IOException {
         if (i < 0 || i >= n || j < 0 || j >= m) {
             return emptyValue;
         }
         
         CachedLine cachedLine = getCachedLineForPosition(i, j);
         for (int n = Math.max(0, j - 4); n < Math.min(j + 4, rowCounts.length); n++) {
             getCachedLineForPosition(i, n);
         }
         if (cachedLine != null) {
             Double val = cachedLine.cache.get(j);
             if (val != null) {
                 return val;
             }
         }
         return emptyValue;
     }
     
     public CachedLine getCachedLineForPosition(int i, int j) throws IOException {
         CachedLineKey cachedLineKey = new CachedLineKey(i, j / cacheLineSize);
         CachedLine cachedLine = lineCache.get(cachedLineKey);
         if (cachedLine == null) {
             RandomAccessFile randomAccessFile = new RandomAccessFile(matrixFile, "r");
             cachedLine = new CachedLine(i, j);
             int min = cachedLine.line * cacheLineSize;
             int max = Math.min(min + cacheLineSize, maxColIndexInRow[i]);
 
             // binary search for min position
             int low = 0;
             int high = rowCounts[i];
             int key = min;
             RowElement minElem = null;
             while (low <= high) {
                 int mid = low + (high - low) / 2;
                 minElem = getRowElementUnsafe(randomAccessFile, i, mid);
                 if (key < minElem.index) {
                     high = mid - 1;
                 } else if (key > minElem.index) {
                     low = mid + 1;
                 } else {
                     break;
                 }
             }
             int startPos = 0;
             if (minElem != null) {
                 startPos = low;
             }
             // System.out.println("low " + low);
             //System.out.println("mins\t" + minElem.index + "\t" + key + "\t" + minElem);
             //System.out.println("startPos"+startPos);
             long seekPos = headerLength;
             if (i > 0) {
                 seekPos += rowCountsSum[i - 1] * ELEMENT_SIZE;
             }
             seekPos += startPos * ELEMENT_SIZE;
             long upperSeekPos = seekPos + rowCounts[i] * ELEMENT_SIZE;
             if (maxColIndexInRow[i] != -1 && startPos <= max) {
                 // load entire cache line
                 while (seekPos < upperSeekPos) {
                     randomAccessFile.seek(seekPos);
                     int k = randomAccessFile.readInt();
                     seekPos += 4;
                     randomAccessFile.seek(seekPos);
                     double value = randomAccessFile.readDouble();
                     cachedLine.put(k, value);
                     seekPos += 8;
                 }
             }
             randomAccessFile.close();
             
             this.lineCache.put(cachedLineKey, cachedLine);
             if (keyQueue.contains(cachedLineKey)) {
                 keyQueue.remove(cachedLineKey);
             } else {
                 elementsCached += cachedLine.cache.size();
             }
             keyQueue.add(cachedLineKey);
             //System.out.println("S:" + keyQueue.size());
             while (elementsCached > maxCachedElements || keyQueue.size() > maxCachedLines) {
                 CachedLineKey removeKey = this.keyQueue.remove(0);
                 elementsCached -= this.lineCache.remove(removeKey).cache.size();
             }
             System.out.println("elements cached " + elementsCached);
         }
         return cachedLine;
     }
     
     public RowElement getRowElementUnsafe(RandomAccessFile randomAccessFile, int i, int offset) throws IOException {
         long seekPos = headerLength;
         if (i > 0) {
             seekPos += rowCountsSum[i - 1] * ELEMENT_SIZE;
         }
         seekPos += offset * ELEMENT_SIZE;
         randomAccessFile.seek(seekPos);
         int index = randomAccessFile.readInt();
         randomAccessFile.seek(seekPos + 4);
         double value = randomAccessFile.readDouble();
         return new RowElement(index, value);
     }
     
     public double getMinValue() {
         return minValue;
     }
     
     public double getMaxValue() {
         return maxValue;
     }
     
     public double getEmptyValue() {
         return emptyValue;
     }
     
     public static void createMatrixFromDenseMatrixFile(File inFile, String regexSeperator, File outFile) throws IOException {
         createMatrixFromDenseMatrix(inFile, regexSeperator, new File(System.getProperty("java.io.tmpdir") + "/" + Math.abs(random.nextLong()) + "_persistent_temp.matrix"), outFile);
     }
     
     public static void createMatrixFromDenseMatrix(File inFile, String regexSeperator, File tempFile, File outFile) throws IOException {
         HashMap<Integer, Integer> rowCount = new HashMap<>(); // i, count
         HashMap<Integer, Integer> maxColIndexMap = new HashMap<>(); // i, maxIndex
         int maxRowIndex = 0;
         int elements = 0;
         double minValue = Double.MAX_VALUE;
         double maxValue = Double.MIN_VALUE;
         
         BufferedReader buffer = new BufferedReader(new FileReader(inFile));
         String textline = null;
         
         int firstLength = -1;
         int k = 0;
         while ((textline = buffer.readLine()) != null) {
             String[] split = textline.split(regexSeperator);
             if (firstLength == -1) {
                 firstLength = split.length;
             }
             if (firstLength == split.length) {
                 rowCount.put(k, firstLength);
                 maxRowIndex = Math.max(maxRowIndex, k);
                 maxColIndexMap.put(k, firstLength);
                 elements += firstLength;
                 
                 for (int j = 0; j < split.length; j++) {
                     double v = Double.parseDouble(split[j].trim());
                     minValue = Math.min(minValue, v);
                     maxValue = Math.max(maxValue, v);
                 }
                 k++;
             } else {
                 break;
             }
         }
         buffer.close();
         
         int[] rowCounts = new int[maxRowIndex + 1];
         int[] rowCountsSum = new int[rowCounts.length];
         int[] maxColIndexInRow = new int[rowCounts.length];
         for (int i = 0; i < rowCounts.length; i++) {
             rowCounts[i] = rowCount.get(i) == null ? 0 : rowCount.get(i);
             maxColIndexInRow[i] = maxColIndexMap.get(i) == null ? -1 : maxColIndexMap.get(i);
             if (i > 0) {
                 rowCountsSum[i] = rowCountsSum[i - 1] + rowCounts[i];
             } else {
                 rowCountsSum[i] = rowCounts[i];
             }
         }
         
         DataOutputStream outBuffer = new DataOutputStream(new FileOutputStream(tempFile));
         outBuffer.writeDouble(DEFAULT_EMPTY_VALUE); // write empty value
         outBuffer.writeInt(maxRowIndex); // write max rows
         outBuffer.writeDouble(minValue);
         outBuffer.writeDouble(maxValue);
         for (int i = 0; i < rowCounts.length; i++) {
             outBuffer.writeInt(rowCounts[i]);
             outBuffer.writeInt(maxColIndexInRow[i]);
         }
         outBuffer.close();
         
         RandomAccessFile randomAccessIntermediate = new RandomAccessFile(tempFile, "rw");
         long headerLength = randomAccessIntermediate.length();
         long newLength = headerLength + elements * ELEMENT_SIZE;
         randomAccessIntermediate.setLength(newLength);
         randomAccessIntermediate.seek(headerLength);
         
         
         
         
         HashMap<Integer, Integer> elementsWrittenToRow = new HashMap<>(); // row i, elements written
         BufferedReader inBuffer = new BufferedReader(new FileReader(inFile));
         textline = null;
         int x = 0;
         while ((textline = inBuffer.readLine()) != null && x < rowCounts.length) {
             String[] split = textline.split(regexSeperator);
             int i = x;
             for (int j = 0; j < maxColIndexInRow[i]; j++) {
                 double v = Double.parseDouble(split[j].trim());
                 //System.out.println(i+"\t"+j+"\t"+v+"\t"+split.length);
 
                 long seekPos = headerLength;
                 int elementsWritten = elementsWrittenToRow.get(i) == null ? 0 : elementsWrittenToRow.get(i);
                 if (i > 0) {
                     seekPos = headerLength + (rowCountsSum[i - 1] * ELEMENT_SIZE);
                 }
                 seekPos = seekPos + j * ELEMENT_SIZE;
                 elementsWrittenToRow.put(i, elementsWritten + 1);
                 randomAccessIntermediate.seek(seekPos);
                 randomAccessIntermediate.writeInt(j);
                 randomAccessIntermediate.seek(seekPos + 4);
                 randomAccessIntermediate.writeDouble(v);
             }
             x++;
         }
         inBuffer.close();
         randomAccessIntermediate.close();
         
         
         RandomAccessFile randomAccessFinal = new RandomAccessFile(outFile, "rw");
         randomAccessFinal.setLength(newLength);
         randomAccessFinal.seek(0);
         randomAccessFinal.writeDouble(DEFAULT_EMPTY_VALUE);
         randomAccessFinal.seek(8);
         randomAccessFinal.writeInt(maxRowIndex);
         randomAccessFinal.seek(12);
         randomAccessFinal.writeDouble(minValue);
         randomAccessFinal.seek(20);
         randomAccessFinal.writeDouble(maxValue);
         for (int i = 0; i < rowCounts.length; i++) {
             
             randomAccessFinal.seek(28 + (i * 8));
             randomAccessFinal.writeInt(rowCounts[i]);
             randomAccessFinal.seek(32 + (i * 8));
             randomAccessFinal.writeInt(maxColIndexInRow[i]);
         }
         randomAccessFinal.seek(headerLength);
         
         DataInputStream dataBuffer = new DataInputStream(new FileInputStream(tempFile));
         double emptyValue = dataBuffer.readDouble();
         maxRowIndex = dataBuffer.readInt();
         dataBuffer.readDouble();
         dataBuffer.readDouble();
         System.out.println(maxRowIndex);
         rowCounts = new int[maxRowIndex + 1];
         for (int i = 0; i < rowCounts.length; i++) {
             rowCounts[i] = dataBuffer.readInt();
             maxColIndexInRow[i] = dataBuffer.readInt();
         }
         
         for (int y = 0; y < rowCounts.length; y++) {
             int offset = 0;
             if (y > 0) {
                 offset = rowCountsSum[y - 1] * ELEMENT_SIZE;
             }
             
             ArrayList<RowElement> rowElements = new ArrayList<>();
             for (int z = 0; z < rowCounts[y]; z++) {
                 rowElements.add(new RowElement(dataBuffer.readInt(), dataBuffer.readDouble()));
             }
             
             Collections.sort(rowElements);
             if (rowElements.size() > 0) {
                 //int lastIndex = rowElements.get(rowElements.size() - 1).index;
 
                 for (int z = 0; z < rowElements.size(); z++) {
                     randomAccessFinal.seek(headerLength + offset + (z * ELEMENT_SIZE));
                     randomAccessFinal.writeInt(rowElements.get(z).index);
                     randomAccessFinal.seek(headerLength + offset + (z * ELEMENT_SIZE) + 4);
                     randomAccessFinal.writeDouble(rowElements.get(z).value);
                 }
             }
         }
         randomAccessFinal.close();
         dataBuffer.close();
     }
     
     public static void createMatrixFromCoordinateListMatrixFile(File inFile, String regexSeperator, File outFile) throws IOException {
         createMatrixFromCoordinateListMatrix(inFile, regexSeperator, new File(System.getProperty("java.io.tmpdir") + "/" + Math.abs(random.nextLong()) + "_persistent_temp.matrix"), outFile);
     }
     
     public static void createMatrixFromCoordinateListMatrix(File inFile, String regexSeperator, File tempFile, File outFile) throws IOException {
         HashMap<Integer, Integer> rowCount = new HashMap<>(); // i, count
         HashMap<Integer, Integer> maxColIndexMap = new HashMap<>(); // i, maxIndex
         int maxRowIndex = 0;
         int elements = 0;
         double minValue = Double.MAX_VALUE;
         double maxValue = Double.MIN_VALUE;
         
         BufferedReader buffer = new BufferedReader(new FileReader(inFile));
         String textline = null;
         while ((textline = buffer.readLine()) != null) {
             String[] split = textline.split(regexSeperator);
             if (split.length >= 3) {
                 try {
                     int i = Integer.parseInt(split[0].trim());
                     int j = Integer.parseInt(split[1].trim());
                     double v = Double.parseDouble(split[2].trim());
                     minValue = Math.min(minValue, v);
                     maxValue = Math.max(maxValue, v);
                     
                     int icount = rowCount.get(i) == null ? 1 : rowCount.get(i) + 1;
                     rowCount.put(i, icount);
                     maxRowIndex = Math.max(maxRowIndex, i);
                     
                     int maxColIndexV = maxColIndexMap.get(i) == null ? j : maxColIndexMap.get(i);
                     maxColIndexMap.put(i, Math.max(maxColIndexV, j));
                     
                     elements++;
                 } catch (NumberFormatException ex) {
                 }
             }
         }
         buffer.close();
         
         int[] rowCounts = new int[maxRowIndex + 1];
         int[] rowCountsSum = new int[rowCounts.length];
         int[] maxColIndexInRow = new int[rowCounts.length];
         for (int i = 0; i < rowCounts.length; i++) {
             rowCounts[i] = rowCount.get(i) == null ? 0 : rowCount.get(i);
             maxColIndexInRow[i] = maxColIndexMap.get(i) == null ? -1 : maxColIndexMap.get(i);
             if (i > 0) {
                 rowCountsSum[i] = rowCountsSum[i - 1] + rowCounts[i];
             } else {
                 rowCountsSum[i] = rowCounts[i];
             }
         }
         
        System.out.println("CREATING PERSISTENT FROM COORDINATE LIST"+maxRowIndex);
        
         DataOutputStream outBuffer = new DataOutputStream(new FileOutputStream(tempFile));
         outBuffer.writeDouble(DEFAULT_EMPTY_VALUE); // write empty value
         outBuffer.writeInt(maxRowIndex); // write max rows
         outBuffer.writeDouble(minValue);
         outBuffer.writeDouble(maxValue);
         for (int i = 0; i < rowCounts.length; i++) {
             outBuffer.writeInt(rowCounts[i]);
             outBuffer.writeInt(maxColIndexInRow[i]);
             System.out.println("CREATING PERSISTENT FROM COORDINATE LIST maxcol "+i+"  -  "+maxColIndexInRow[i]);
         }
         outBuffer.close();
         
         RandomAccessFile randomAccessIntermediate = new RandomAccessFile(tempFile, "rw");
         long headerLength = randomAccessIntermediate.length();
         long newLength = headerLength + elements * ELEMENT_SIZE;
         randomAccessIntermediate.setLength(newLength);
         randomAccessIntermediate.seek(headerLength);
         
         
         HashMap<Integer, Integer> elementsWrittenToRow = new HashMap<>(); // row i, elements written
         BufferedReader inBuffer = new BufferedReader(new FileReader(inFile));
         textline = null;
         while ((textline = inBuffer.readLine()) != null) {
             String[] split = textline.split(regexSeperator);
             if (split.length >= 3) {
                 try {
                     int i = Integer.parseInt(split[0].trim());
                     int j = Integer.parseInt(split[1].trim());
                     double v = Double.parseDouble(split[2].trim());
                     
                     long seekPos = headerLength;
                     int elementsWritten = elementsWrittenToRow.get(i) == null ? 0 : elementsWrittenToRow.get(i);
                     if (i > 0) {
                         seekPos = headerLength + (rowCountsSum[i - 1] * ELEMENT_SIZE);
                     }
                     seekPos = seekPos + elementsWritten * ELEMENT_SIZE;
                     elementsWrittenToRow.put(i, elementsWritten + 1);
                     randomAccessIntermediate.seek(seekPos);
                     randomAccessIntermediate.writeInt(j);
                     randomAccessIntermediate.seek(seekPos + 4);
                     randomAccessIntermediate.writeDouble(v);
                 } catch (NumberFormatException ex) {
                 }
             }
         }
         inBuffer.close();
         randomAccessIntermediate.close();
         
         
         RandomAccessFile randomAccessFinal = new RandomAccessFile(outFile, "rw");
         randomAccessFinal.setLength(newLength);
         randomAccessFinal.seek(0);
         randomAccessFinal.writeDouble(DEFAULT_EMPTY_VALUE);
         randomAccessFinal.seek(8);
         randomAccessFinal.writeInt(maxRowIndex);
         randomAccessFinal.seek(12);
         randomAccessFinal.writeDouble(minValue);
         randomAccessFinal.seek(20);
         randomAccessFinal.writeDouble(maxValue);
         for (int i = 0; i < rowCounts.length; i++) {
             
             randomAccessFinal.seek(28 + (i * 8));
             randomAccessFinal.writeInt(rowCounts[i]);
             randomAccessFinal.seek(32 + (i * 8));
             randomAccessFinal.writeInt(maxColIndexInRow[i]);
         }
         randomAccessFinal.seek(headerLength);
         
         DataInputStream dataBuffer = new DataInputStream(new FileInputStream(tempFile));
         double emptyValue = dataBuffer.readDouble();
         maxRowIndex = dataBuffer.readInt();
         dataBuffer.readDouble();
         dataBuffer.readDouble();
         rowCounts = new int[maxRowIndex + 1];
         for (int i = 0; i < rowCounts.length; i++) {
             rowCounts[i] = dataBuffer.readInt();
             maxColIndexInRow[i] = dataBuffer.readInt();
         }
         
         for (int y = 0; y < rowCounts.length; y++) {
             int offset = 0;
             if (y > 0) {
                 offset = rowCountsSum[y - 1] * ELEMENT_SIZE;
             }
             
             ArrayList<RowElement> rowElements = new ArrayList<>();
             for (int z = 0; z < rowCounts[y]; z++) {
                 rowElements.add(new RowElement(dataBuffer.readInt(), dataBuffer.readDouble()));
             }
             
             Collections.sort(rowElements);
             if (rowElements.size() > 0) {
                 //int lastIndex = rowElements.get(rowElements.size() - 1).index;
 
                 for (int z = 0; z < rowElements.size(); z++) {
                     randomAccessFinal.seek(headerLength + offset + (z * ELEMENT_SIZE));
                     randomAccessFinal.writeInt(rowElements.get(z).index);
                     randomAccessFinal.seek(headerLength + offset + (z * ELEMENT_SIZE) + 4);
                     randomAccessFinal.writeDouble(rowElements.get(z).value);
                 }
             }
         }
         randomAccessFinal.close();
         dataBuffer.close();
     }
     
     public void saveAsCoordinateListMatrix(File outFile) throws IOException {
         BufferedWriter buffer = new BufferedWriter(new FileWriter(outFile));
         Iterator<Element> it = iterator();
         while (it.hasNext()) {
             Element e = it.next();
             System.out.println(e.i + "," + e.j + "," + e.value + "\n");
             buffer.write(e.i + "," + e.j + "," + e.value + "\n");
         }
         buffer.close();
     }
     
     public void saveAsDenseMatrix(File outFile) throws IOException {
         BufferedWriter buffer = new BufferedWriter(new FileWriter(outFile));
         for (int i = 0; i < n; i++) {
             for (int j = 0; j < m; j++) {
                 double val = getValue(i, j);
                 buffer.write(val + "");
                 if (j != m - 1) {
                     buffer.write("\t");
                 }
             }
             buffer.newLine();
         }
         buffer.close();
     }
     
     public Iterator<Element> iterator() {
         Iterator<Element> it = new Iterator<Element>() {
             
             private int i = 0;
             private int j = 0;
             
             @Override
             public boolean hasNext() {
                 try {
                     for (int x = i; x < rowCounts.length; x++) {
                         if (rowCounts[x] > 0) {
                             for (int y = j; y < maxColIndexInRow[x]; y++) {
                                 int cachedLinePos = (y / cacheLineSize) * cacheLineSize;
                                 CachedLine cachedLine = getCachedLineForPosition(x, cachedLinePos);
                                 for (int y2 = Math.max(j, cachedLinePos); y2 < cachedLinePos + cacheLineSize; y2++) {
                                     if (cachedLine.cache.size() > 0) {
                                         i = x;
                                         j = y2;
                                         return true;
                                     }
                                 }
                             }
                         }
                         j = 0;
                     }
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
                 return false;
             }
             
             @Override
             public Element next() {
                 try {
                     for (int x = i; x < rowCounts.length; x++) {
                         if (rowCounts[x] > 0) {
                             for (int y = j; y < maxColIndexInRow[x]; y++) {
                                 int cachedLinePos = (y / cacheLineSize) * cacheLineSize;
                                 CachedLine cachedLine = getCachedLineForPosition(x, cachedLinePos);
                                 for (int y2 = Math.max(j, cachedLinePos); y2 < cachedLinePos + cacheLineSize; y2++) {
                                     if (cachedLine.cache.size() > 0) {
                                         i = x;
                                         j = y2;
                                         j++;
                                         return new Element(x, y2, cachedLine.cache.get(y2));
                                     }
                                 }
                             }
                             j = 0;
                         }
                     }
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
                 return null;
             }
             
             @Override
             public void remove() {
             }
         };
         return it;
     }
     
     public static void main(String[] args) {
         try {
             //PersistentSparseMatrix.createMatrixFromCoordinateListMatrix(new File("examples/tabular/p-values.csv"), ",", new File("out.sparse"));
 
             PersistentSparseMatrix.createMatrixFromDenseMatrix(new File("Crab_rRNA.bp"), "(\\s)+", new File("temp.sparse"), new File("out.dense"));
             PersistentSparseMatrix matrix = new PersistentSparseMatrix(new File("out.dense"));
             System.out.println(matrix.getValue(0, 5));
             System.out.println(matrix.getValue(420, 416));
             /*
              * PersistentSparseMatrix matrix = new PersistentSparseMatrix(new
              * File("final.matrix")); System.out.println(matrix.getValue(1459,
              * 1594)); System.out.println(matrix.getValue(1459, 1460));
              * System.out.println(matrix.getValue(6779, 6790));
              * System.out.println(matrix.getValue(2586, 2647));
              * System.out.println(matrix.getValue(5852, 5885)); Random random =
              * new Random(); for (int i = 0; i < 100000 ; i++) { int x =
              * random.nextInt(9000); int y = x + random.nextInt(150);
              * matrix.getValue(x, y); if (i % 10000 == 0) {
              * System.out.println(i); } //System.out.println(x + "\t" + y + "\t"
              * + matrix.getValue(x, y)); }
              */
         } catch (IOException ex) {
             Logger.getLogger(PersistentSparseMatrix.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     static class Element {
         
         public int i;
         public int j;
         public double value;
         
         public Element(int i, int j, double value) {
             this.i = i;
             this.j = j;
             this.value = value;
         }
     }
     
     static class CachedLine {
         
         int row;
         int line;
         HashMap<Integer, Double> cache = new HashMap<Integer, Double>();
         
         public CachedLine(int row, int j) {
             this.row = row;
             this.line = j / cacheLineSize;
         }
         
         public void put(int j, double value) {
             cache.put(j, value);
         }
     }
     
     static class RowElement implements Comparable<RowElement> {
         
         int index;
         double value;
         
         public RowElement(int index, double value) {
             this.index = index;
             this.value = value;
         }
         
         @Override
         public int compareTo(RowElement o) {
             int c = this.index - o.index;
             if (c == 0) {
                 if (this.value < o.value) {
                     return -1;
                 } else if (this.value > o.value) {
                     return 1;
                 } else {
                     return 0;
                 }
             }
             return c;
         }
         
         @Override
         public String toString() {
             return "RowElement{" + "index=" + index + ", value=" + value + '}';
         }
     }
     
     static class CachedLineKey {
         
         int i;
         int j;
         
         public CachedLineKey(int i, int j) {
             this.i = i;
             this.j = j;
         }
         
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (getClass() != obj.getClass()) {
                 return false;
             }
             final CachedLineKey other = (CachedLineKey) obj;
             if (this.i != other.i) {
                 return false;
             }
             if (this.j != other.j) {
                 return false;
             }
             return true;
         }
         
         @Override
         public int hashCode() {
             int hash = 3;
             hash = 37 * hash + this.i;
             hash = 37 * hash + this.j;
             return hash;
         }
     }
     
     public ArrayList<Double> getSample(int sampleSize) throws IOException {
         Random random = new Random(4920275133465331511L);
         ArrayList<Double> sample = new ArrayList<>();
         int lines = 0;
         while (sample.size() < sampleSize || lines < 10) {
             int i = random.nextInt(n);
             int j = random.nextInt(m);
             
            // int j1 = (j / cacheLineSize) * cacheLineSize;
             //int j2 = j1 + cacheLineSize;
             CachedLine cachedLine = getCachedLineForPosition(i, j);
             Set<Integer> keys = cachedLine.cache.keySet();
             Iterator<Integer> it = keys.iterator();
             while(it.hasNext())
             {
                 double val = cachedLine.cache.get(it.next());
                 if (val != emptyValue) {
                     sample.add(val);
                 }
                 
             }
             lines++;
             /*for (int a = 0 ; a < 50 ; a++) {  
                 this.
                 double val = this.getValue(i, j1 + random.nextInt(cacheLineSize));
                 if (val != emptyValue) {
                     sample.add(val);
                 }                
             }*/
         }
         return sample;
     }
 }
