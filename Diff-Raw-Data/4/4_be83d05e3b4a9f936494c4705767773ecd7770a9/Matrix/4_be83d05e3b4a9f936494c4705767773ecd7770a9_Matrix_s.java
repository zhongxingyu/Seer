 // -- @(#)Matrix.java 1.4 99/03/22
 
 /*
  * This version by David G. Melvin. Originally based on Matrix.java, v1.3, by
  * Akio Utsugi, but extended to include linear algebra stuff.
  */
 
 package uk.org.ponder.matrix;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.text.DecimalFormat;
 
 import uk.org.ponder.stringutil.CharWrap;
 import uk.org.ponder.util.Logger;
 
 class MatrixStorage {
   double value[];
 
   int references = 1;
 
   MatrixStorage(int row, int col) {
     value = new double[row * col];
   }
 
 }
 
 /**
  * A class for manipulation of numerical matrix objects with basic linear
  * algebra and eigen system methods.
  * 
  * @version @(#)Matrix.java 1.4 99/03/22, 17 Sep 1996
  * @author David G. Melvin
  */
 
 public class Matrix implements Cloneable {
 
   final static double small = 1.0e-20;
   final static int JacobiIter = 50;
 
   int rows, cols;
   MatrixStorage storage;
 
   /**
    * Generalised storage system: These variables allow us to map the matrix
    * dimensions onto the 1d array in an arbitrary manner.
    */
 
   int xstep, ystep;
   int topleft = 0;
 
   /**
    * Private cache of values that are relatively expensive to compute or that
    * are generated in tandem or likely to be used frequently after they have
    * been computed. NB. ensure that these are reset to null when any elements of
    * the matrix are changed. The private routine clearCache is provided for this
    * purpose. Ensure that this is always in line with the set of cached
    * components.
    */
 
   LUDecomp LUD = null;
   Matrix eigenVec = null;
   Matrix eigenVal = null;
 
   void clearCache() {
     LUD = null;
     eigenVec = null;
     eigenVal = null;
   }
 
   public static final UnaryFunction Exp = new UnaryFunction() {
     public double apply(double arg) {
       return Math.exp(arg);
     }
   };
   public static final UnaryFunction Sqrt = new UnaryFunction() {
     public double apply(double arg) {
       return Math.sqrt(arg);
     }
   };
 
   // The matrix has been modified, and requires its storage reallocated.
   // We take the opportunity to save storage by compacting rows, and
   // make use of System.arraycopy to blag as many elements as possible that
   // occur without gaps. In order to get maximum use out of System.arraycopy,
   // we do not guarantee that the resulting matrix is not tranposed relative
   // to its storage, i.e. it may be that ystep = 1 rather than xstep = 1.
 
   void reallocate() {
     //          System.out.println("Matrix reallocated: ");
     //	  debug();
     double[] value = storage.value;
     MatrixStorage newstorage = new MatrixStorage(rows, cols);
     double[] newvalue = newstorage.value;
 
     // The normal case - the elements in the old matrix form contiguous rows
     // without gaps between them, although they may be transposed.
     // We may blag the entire matrix at one go.
 
     if ((xstep == 1 || ystep == 1) && ystep == cols) {
       //	    System.out.println("Case 1");
       System.arraycopy(value, topleft, newvalue, 0, rows * cols);
     }
 
     else {
       if (xstep == 1) {
         // the slightly less normal case - elements in
         // old matrix are the right way
         // up, but with gaps.
         //		System.out.println("Case 2");
         for (int row = 0; row < rows; ++row) {
           System.arraycopy(value, topleft + row * ystep, newvalue, 0 + row
               * cols, cols);
         }
         ystep = cols;
       }
 
       else if (ystep == 1) {
         // the even less normal case - elements in old matrix are the wrong way
         // up, and with gaps.
         //		System.out.println("Case 3");
         for (int col = 0; col < cols; ++col) {
           System.arraycopy(value, topleft + col * xstep, newvalue, 0 + col
               * rows, rows);
         }
         xstep = rows;
       }
 
       else {
         // the completely insane case - elements are arbitrarily distributed
         // round
         // the matrix with gaps everywhere. This is quite likely never to occur!
         int newaddress = 0;
         for (int row = 0; row < rows; ++row) {
           for (int col = 0; col < cols; ++col) {
             newvalue[newaddress++] = getMval(row, col);
           }
         }
         xstep = 1;
         ystep = cols;
       }
     }
 
     // We escaped alive!
     storage.references--;
     if (storage.references == 0)
       throw new RuntimeException(
           "Programming error: no references remain to MatrixStorage");
 
     storage = newstorage;
     topleft = 0;
     //	System.out.println("Reallocated Matrix is now");
     //	  debug();
   }
 
   /**
    * This package private method MUST be called before any operation that would
    * alter the matrix contents
    */
   void aboutToModify() {
     clearCache();
     if (storage.references > 1)
       reallocate();
   }
 
   /**
    * This is an experimental method, noting that *ALL* of the matrix values in
    * storage are about to be overwritten, so we may make the optimisation of
    * assuring that the storage is not transposed. We may then get the benefits
    * of sharing storage where it is the same size, and also of assuming that
    * xstep = 1, and we may also step through the array from TL to BR by adding
    * 1, etc.
    */
   void aboutToObliterate() {
     clearCache();
     if (storage.references > 1)
       reallocate();
     if (ystep == 1) {
       int temp = ystep;
       ystep = xstep;
       xstep = temp;
     }
   }
 
   /**
    * The implementation of the matrix maps its elements into a 1d linear array.
    * This private interface routine computes the correct location in the 1d
    * array as if it were a 2d array. The advantage is the reduction in overhead
    * imposed by java's 2d array implementation. This also allows arbitrary
    * linear mapping of the matrix into 1d, allowing for transpose, submatrices,
    * and copies to be efficiently implemented.
    * <p>
    * For efficiency, calls to this method could be eliminated by use of a
    * strength-reduced index, except where this would cause the resulting code to
    * become unreadable.
    */
   double getMval(int y, int x) {
     return storage.value[topleft + x * xstep + y * ystep];
   }
 
   /**
    * The implementation of the matrix maps its elements into a 1d linear array.
    * This private interface routine computes the correct location in the 1d
    * array as if it were a 2d array. The advantage is the reduction in overhead
    * imposed by java's 2d array implementation. This also allows arbitrary
    * linear mapping of the matrix into 1d, allowing for transpose, submatrices,
    * and copies to be efficiently implemented.
    * <p>
    * For efficiency, calls to this method could be eliminated by use of a
    * strength-reduced index, except where this would cause the resulting code to
    * become unreadable.
    * <p>
    * Since this function is package private, it does not clear the caches for
    * extra speed.
    */
 
   void setMval(int y, int x, double val) {
     storage.value[topleft + x * xstep + y * ystep] = val;
   }
 
   /**
    * The column separator used when rendering a matrix into a string.
    */
   public String fieldSeparator = " ";
 
   /**
    * The number format to be used when rendering the matrix into a string. The
    * default is to emulate the behaviour of Matlab to a degree by allowing 4
    * digits after the decimal point.
    */
   public static DecimalFormat decFormat = new DecimalFormat("0.0###");
 
   /**
    * Construct a square matrix object, with initial values set to zero.
    * 
    * @param n width and height.
    */
   public Matrix(int n) {
     this(n, n);
   }
 
   /**
    * Construct a rectangular matrix object, with initial values set to zero.
    * 
    * @param row number of row
    * @param col number of column
    */
   public Matrix(int rows, int cols) {
     this(rows, cols, true);
   }
 
   /**
    * Private constructor which allows the choice not to allocate storage. If
    * storage is shared with another Matrix, remember to adjust the reference
    * count of the storage.
    * 
    * @param row number of row
    * @param col number of column
    */
 
   Matrix(int rows, int cols, boolean allocate) {
     this.xstep = 1;
     this.ystep = cols;
     this.rows = rows;
     this.cols = cols;
     if (allocate)
       storage = new MatrixStorage(rows, cols);
   }
 
   /**
    * Construct a rectangular matrix object from a 2D array of doubles.
    * 
    * @param data
    */
 
   public Matrix(double[][] data) {
     this(data.length, data.length == 0 ? 0
         : data[0].length, true);
     double[] value = storage.value;
 
     for (int i = 0; i < rows; i++) {
       System.arraycopy(data[i], 0, value, i * cols, cols);
     }
   }
 
   /**
    * Construct a column vector from a 1D array of doubles. This converts a 1D
    * array into a true matrix object. NB. if you want a row vector remember to
    * transpose it.
    * 
    * @param data
    */
   public Matrix(double[] data) {
     this(data.length, 1);
     System.arraycopy(data, 0, storage.value, 0, data.length);
   }
 
   /**
    * Create an identity matrix.
    * 
    * @param n size, both rows and columns
    */
   static public Matrix identity(int n) {
 
     int i;
 
     Matrix ret = new Matrix(n);
     for (i = 0; i < n; i++) {
       ret.setMval(i, i, 1.0);
     }
     return ret;
   }
 
   /**
    * Create a square matrix of all ones.
    * 
    * @param n the dimension of the required square matrix.
    */
   static public Matrix ones(int n) {
     return (ones(n, n));
   }
 
   /**
    * Create a rectangular matrix of all ones.
    * 
    * @param row Number of rows.
    * @param col Number of columns.
    */
   static public Matrix ones(int row, int col) {
 
     Matrix ret = new Matrix(row, col);
     for (int i = 0; i < row; i++) {
       for (int j = 0; j < col; j++) {
         ret.setMval(i, j, 1.0);
       }
     }
     return ret;
   }
 
   /**
    * Create a square matrix of all zeros.
    * 
    * @param n the dimension of the required square matrix.
    */
   static public Matrix zeros(int n) {
     return (zeros(n, n));
   }
 
   /**
    * Create a matrix of all zeros.
    * 
    * @param row Number of rows.
    * @param col Number of columns.
    */
   static public Matrix zeros(int row, int col) {
 
     Matrix ret = new Matrix(row, col);
     return ret;
   }
 
   /**
    * Clone makes a deep copy of the matrix contents.
    */
 
   public Object clone() {
     try {
       Matrix res = (Matrix) super.clone();
       res.storage = storage;
       storage.references++;
       //		res.value = new double[value.length];
       //		System.arraycopy(value, 0, res.value, 0, value.length);
       return res;
     }
     catch (CloneNotSupportedException e) {
       // will never happen
       return null;
     }
   }
 
   /**
    * Perform a deep copy of the matrix.
    */
   public Matrix copy() {
 
     Matrix ret = (Matrix) this.clone();
     return ret;
   }
 
   /*
    * Basic Matrix Calculations
    */
 
   /**
    * Transpose: X'.
    * 
    * @returns A copy of the matrix, transposed.
    */
   public Matrix transpose() {
     int i, j;
     // We will create a new matrix which initially shares storage
     // the original.
     Matrix ret = new Matrix(cols, rows, false);
     ret.storage = storage;
     storage.references++;
     // flip round the indexes.
     ret.xstep = ystep;
     ret.ystep = xstep;
     return ret;
   }
 
   /*
    * Transpose the Matrix in place: X' @returns The original matrix, but
    * transposed.
    */
   // AMB NB - for 2x2 matrices and smaller, this is not worth it!
   public Matrix updateTranspose() {
     int temp = xstep;
     xstep = ystep;
     ystep = temp;
     temp = cols;
     cols = rows;
     rows = temp;
     return this;
   }
 
   /**
    * Linear conversion: a*X + b*Y.
    */
   public Matrix linearConv(double a, double b, Matrix Y)
       throws SizeMismatchException {
     // Unoptimised
     int i, j;
     int Y_rows = Y.rows;
     int Y_cols = Y.cols;
 
     if (rows == Y_rows && cols == Y_cols) {
       Matrix ret = new Matrix(rows, cols);
 
       for (i = 0; i < rows; i++) {
         for (j = 0; j < cols; j++) {
           ret.setMval(i, j, a * getMval(i, j) + b * Y.getMval(i, j));
         }
       }
       return ret;
     }
     else {
       throw new SizeMismatchException();
     }
   }
 
   /**
    * Update by Linear conversion: X = a*X + b*Y.
    */
   public void updateLinearConv(double a, double b, Matrix Y)
       throws SizeMismatchException {
     // Unoptimised.
     int i, j;
     int Y_rows = Y.rows;
     int Y_cols = Y.cols;
 
     if (rows != Y_rows || cols != Y_cols) {
       throw new SizeMismatchException();
     }
 
     for (i = 0; i < rows; i++) {
       for (j = 0; j < cols; j++) {
         setMval(i, j, a * getMval(i, j) + b * Y.getMval(i, j));
       }
     }
   }
 
   /**
    * Scalar product: B = aA;
    * 
    * @param a the scalar constant
    * @returns a new matrix which is the result of the scalar multiply.
    */
 
   public Matrix multipliedBy(double a) {
     Matrix B = copy();
     return multipliedByInto(B, a);
   }
 
   /**
    * Scalar product: B = aA;
    * 
    * @param a the scalar constant
    * @param B the target matrix to be overwritten
    * @returns the matrix B which is the result of the scalar multiply.
    */
 
   public Matrix multipliedByInto(Matrix B, double a) {
 
     int i, j, Aindex = topleft, Bindex = B.topleft;
 
     B.aboutToModify();
 
     double[] Aarray = storage.value;
     double[] Barray = B.storage.value;
 
     int Arowspan = (ystep - cols * xstep);
     int Browspan = (B.ystep - B.cols * B.xstep);
 
     for (i = rows; i > 0; i--) {
       for (j = cols; j > 0; j--) {
         Barray[Bindex] = a * Aarray[Aindex];
         Bindex += B.xstep;
         Aindex += xstep;
       }
       Bindex += Browspan;
       Aindex += Arowspan;
     }
     return B;
   }
 
   /**
    * Matrix multiplication: C=A*B.
    * 
    * @param B the matrix to multiply by
    * @returns a new matrix which is the result of the matrix multiply.
    */
 
   public Matrix multipliedBy(Matrix B) {
     Matrix C = new Matrix(rows, B.cols);
     return multipliedByInto(C, B);
   }
 
   /**
    * Matrix multiplication: C=A*B.
    * 
    * @param C the target matrix to be overwritten by the result
    * @param B the matrix to multiply by
    * @returns a the matrix C which is the result of the matrix multiply.
    */
 
   public Matrix multipliedByInto(Matrix C, Matrix B) {
     int i, j, k; // These can use up the 4 local variable slots in many JVMs.
     int B_rows = B.rows, B_cols = B.cols;
 
     /*
      * if (B == C) { throw new UnsupportedOperationException("Cannot multiply
      * matrices in place"); }
      */
 
     if (cols != B_rows || C.rows != rows || C.cols != B.cols) {
       throw new SizeMismatchException();
     }
     // this call will produce a matrix that has compacted storage,
     // but not necessarily one that is not transposed (i.e. ystep may be
     // 1, rather than xstep).
     C.aboutToModify();
 
     double[] Aarray = storage.value;
     double[] Barray = B.storage.value;
     double[] Carray = C.storage.value;
 
     // massive strength reduction: instead of a multiply and add
     // for each lookup (i.e. 3n^3 integer multiplies and 4n^3 or so adds),
     // we set up three registers with the steps required when
     // getting to the end of a run on a matrix.
     // We then maintain these 3 indices into the 3 matrix arrays, update them
     // when required by adding the constant offsets within a row or column.
     // When we get to the end of a row or column, we add on the register
     // (span) values. Cost is now only 3 int multiplies, and 2n^3 or so
     // adds... Of course the n^3 fp multiplies are still all there.
 
     int Aindex = topleft;
     int Bindex = B.topleft;
     int Cindex = 0;
 
     // offset required to move from position off the end of one row
     // onto the beginning of the same row of matrix A - this
     // is used most frequently in the loop!
     int Arowbackfly = -cols * xstep;
     // offset required to move from position off the end of one column
     // onto the next column of matrix B.
     int Bcolspan = (B.xstep - B_rows * B.ystep);
     // ditto C - cannot assume it is in good order, since copy()
     // may still produce a matrix that is transposed - perhaps think further.
     // see aboutToObliterate above, and replace all of these offsets with 1.
     int Crowspan = (C.ystep - C.cols * C.xstep);
     // i scans down the rows of first matrix
     // j scans across the columns of the second matrix
     // k is shared between the cols of first matrix and rows of second matrix
     // Therefore: for each scan of a row of first matrix, we have a complete
     // scan of second matrix.
     // Note that the actual values of i,j and k are now irrelevant to the
     // calculations - they are only used to time the addition of the constants
     // to the indexes. We can therefore make use of the "comparison with 0"
     // dodge.
     for (i = rows; i > 0; i--) {
       for (j = B_cols; j > 0; j--) {
         double s = 0;
         for (k = cols; k > 0; k--) {
           //		      System.out.println("A: "+Aindex+" B: "+Bindex+" C: "+Cindex);
           s += Aarray[Aindex] * Barray[Bindex];
           // end for a shared index.
           Aindex += xstep; // move the A pointer one to the right.
           Bindex += B.ystep; // move the B pointer one down.
         }
         Carray[Cindex] = s;
         // end for a column of the second matrix.
         Aindex += Arowbackfly; // Reset the A pointer to the beginning of the
                                // row.
         Bindex += Bcolspan; // Start the second matrix at the next column
         Cindex += C.xstep; // Move the output register onto the next element.
       }
       // end for a row of the first matrix, and hence for all of the second
       // matrix.
       Bindex = B.topleft; // Start the second matrix at the top left again.
       // NB hideous cumulative effects resulting in next line.
       Aindex += ystep; // Move the first matrix onto next row, from pos just
                        // above.
     }
 
     return C;
   }
 
   /**
    * Matrix subtraction: C = A-B
    */
 
   public Matrix subtract(Matrix B) {
     Matrix C = new Matrix(B.rows, B.cols);
     return subtractinto(B, C);
   }
 
   /**
    * Matrix subtraction: C = A-B
    */
 
   public Matrix subtractinto(Matrix B, Matrix C) {
     int B_rows = B.rows, B_cols = B.cols;
 
     if (cols != B_cols || rows != B_rows) {
       throw new SizeMismatchException();
     }
     C.aboutToModify();
 
     int Aindex = topleft;
     int Bindex = B.topleft;
     int Cindex = C.topleft;
 
     double[] Aarray = storage.value;
     double[] Barray = B.storage.value;
     double[] Carray = C.storage.value;
 
     int Arowspan = (ystep - cols * xstep);
     int Browspan = (B.ystep - cols * xstep);
     int Crowspan = (C.ystep - cols * xstep);
 
     for (int i = rows; i > 0; i--) {
       for (int j = cols; j > 0; j--) {
         Carray[Cindex] = Aarray[Aindex] - Barray[Bindex];
         Cindex += C.xstep;
         Bindex += B.xstep;
         Aindex += xstep;
       }
       Cindex += Crowspan;
       Bindex += Browspan;
       Aindex += Arowspan;
     }
     return C;
   }
 
   public Matrix add(Matrix B) throws SizeMismatchException {
     Matrix C = new Matrix(B.rows, B.cols);
     return addinto(B, C);
   }
 
   /**
    * Matrix addition: A+B = C
    */
 
   public Matrix addinto(Matrix B, Matrix C) {
     int B_rows = B.rows, B_cols = B.cols;
 
     if (cols != B_cols || rows != B_rows) {
       throw new SizeMismatchException();
     }
     C.aboutToModify();
 
     int Aindex = topleft;
     int Bindex = B.topleft;
     int Cindex = C.topleft;
 
     double[] Aarray = storage.value;
     double[] Barray = B.storage.value;
     double[] Carray = C.storage.value;
 
     int Arowspan = (ystep - cols * xstep);
     int Browspan = (B.ystep - cols * xstep);
     int Crowspan = (C.ystep - cols * xstep);
 
     for (int i = rows; i > 0; i--) {
       for (int j = cols; j > 0; j--) {
         Carray[Cindex] = Aarray[Aindex] + Barray[Bindex];
         Cindex += C.xstep;
         Bindex += B.xstep;
         Aindex += xstep;
       }
       Cindex += Crowspan;
       Bindex += Browspan;
       Aindex += Arowspan;
     }
     return C;
   }
 
   /**
    * Matrix left division: X\Y. X\Y is the matrix division of X into Y, which is
    * roughly the same as INV(X)*Y , except it is computed in a different way. If
    * X is an N-by-N matrix and Y is a column vector with N components, or a
    * matrix with several such columns, then B = X\Y is the solution to the
    * equation X*B = Y.
    */
   public Matrix dividedBy(Matrix Y) {
     return this.dividedBy(Y, 2 * cols - 1);
   }
 
   /**
    * Matrix division with bandwidth: X\Y.
    */
   public Matrix dividedBy(Matrix Y, int bw) {
 
     // NB AMB - Optimising this method could prove a real headache!
     int i, j, k;
 
     Matrix L = copy();
     Matrix U = Y.copy();
 
     int m = L.rows;
     int n = U.cols;
     bw = bw / 2 + 1;
 
     for (k = 0; k < m; k++) {
 
       double pp = L.getMval(k, k);
 
       if (Math.abs(pp) < small) {
        return null;
       }
 
       double w = 1 / pp;
 
       for (j = k + 1; j < k + bw && j < m; j++) {
 
         double s = w * L.getMval(k, j);
         L.setMval(k, j, s);
 
         for (i = k + 1; i < k + bw + 1 && i < m; i++) {
           s = L.getMval(i, j) - L.getMval(i, k) * L.getMval(k, j);
           L.setMval(i, j, s);
         }
       }
 
       for (j = 0; j < n; j++) {
 
         double s = w * U.getMval(k, j);
         U.setMval(k, j, s);
 
         for (i = k + 1; i < k + bw && i < m; i++) {
           s = U.getMval(i, j) - L.getMval(i, k) * U.getMval(k, j);
           U.setMval(i, j, s);
         }
       }
 
     }
 
     Matrix ret = new Matrix(m, n);
 
     for (i = m - 1; i >= 0; i--) {
       for (j = 0; j < n; j++) {
 
         double w = U.getMval(i, j);
 
         for (k = i + 1; i < k + bw && k < m; k++) {
           w -= L.getMval(i, k) * ret.getMval(k, j);
         }
 
         ret.setMval(i, j, w);
       }
     }
     return ret;
   }
 
   /**
    * Cross squared Euclidean distance Matrix: Z_{ij} = ||X_i - Y_j||^2
    */
   public Matrix crossSqDistance(Matrix Y) throws SizeMismatchException {
     // Unoptimised. In fact, I can't even quite work out what it does!
     int i, j, k;
     int Y_rows = Y.rows, Y_cols = Y.cols;
 
     if (cols != Y_cols) {
       throw new SizeMismatchException();
     }
 
     Matrix ret = new Matrix(rows, Y_rows);
 
     for (i = 0; i < rows; i++) {
       for (j = 0; j < Y_rows; j++) {
         double s = 0;
         for (k = 0; k < cols; k++) {
           double d = getMval(i, k) - Y.getMval(j, k);
           s += d * d;
         }
         ret.setMval(i, j, s);
       }
     }
     return ret;
   }
 
   public Matrix mapEntries(UnaryFunction func) {
 
     aboutToModify();
 
     int i, j, index = topleft;
 
     double[] array = storage.value;
 
     int Arowspan = (ystep - cols * xstep);
 
     for (i = rows; i > 0; i--) {
       for (j = cols; j > 0; j--) {
         array[index] = func.apply(array[index]);
         index += xstep;
       }
       index += Arowspan;
     }
     return this;
   }
 
   /**
    * Update by square: (X_{ij})^2
    */
 
   public Matrix updateSqr() {
     // Unoptimised
     aboutToModify();
     for (int i = 0; i < rows; i++) {
       for (int j = 0; j < cols; j++) {
         double val = getMval(i, j);
         setMval(i, j, val * val);
       }
     }
     return this;
   }
 
   /**
    * Create a horizontal-summation vector.
    */
   public Matrix horizontalSum() {
     int i, j, Aindex = topleft;
     int vecindex = 0;
     int Arowspan = ystep - cols * xstep;
     double[] Aarray = storage.value;
     Matrix ret = new Matrix(rows, 1);
     double[] vecarray = ret.storage.value;
 
     for (i = rows; i > 0; i--) {
       double s = 0;
       for (j = cols; j > 0; j--) {
         s += Aarray[Aindex];
         Aindex += xstep;
       }
       Aindex += Arowspan;
       vecarray[vecindex] = s;
       ++vecindex;
     }
     return ret;
   }
 
   /**
    * Create a vertical-summation vector.
    */
   public Matrix verticalSum() {
     int i, j, Aindex = topleft;
     int vecindex = 0;
     int Acolspan = xstep - rows * ystep;
     double[] Aarray = storage.value;
     Matrix ret = new Matrix(1, cols);
     double[] vecarray = ret.storage.value;
 
     for (j = cols; j > 0; j--) {
       double s = 0;
       for (i = rows; i > 0; i--) {
         s += Aarray[Aindex];
         Aindex += ystep;
       }
       Aindex += Acolspan;
       vecarray[vecindex] = s;
       ++vecindex;
     }
     return ret;
   }
 
   /**
    * Summation of all entries.
    */
   public double sumEntries() {
 
     int i, j, index = topleft;
 
     double total = 0;
 
     double[] array = storage.value;
 
     int Arowspan = (ystep - cols * xstep);
 
     for (i = rows; i > 0; i--) {
       for (j = cols; j > 0; j--) {
         total += array[index];
         index += xstep;
       }
       index += Arowspan;
     }
     return total;
   }
 
   /**
    * Summation of squared entries.
    */
   public double sumSqrEntries() {
 
     int i, j, Aindex = topleft;
 
     double total = 0;
 
     double[] Aarray = storage.value;
 
     int Arowspan = (ystep - cols * xstep);
 
     for (i = rows; i > 0; i--) {
       for (j = cols; j > 0; j--) {
         double x = Aarray[Aindex];
         total += x * x;
         Aindex += xstep;
       }
       Aindex += Arowspan;
     }
     return total;
   }
 
   /**
    * Elementwise multiplication.
    */
   public Matrix multipliedEntriesBy(Matrix B) {
     Matrix C = new Matrix(B.cols, B.rows);
     multipliedEntriesByInto(C, B);
     return C;
   }
 
   public Matrix multipliedEntriesByInto(Matrix C, Matrix B) {
     int i, j;
 
     if (rows != B.rows || cols != B.cols || cols != C.cols || rows != C.rows) {
       throw new SizeMismatchException();
     }
     C.aboutToModify();
 
     int Aindex = topleft;
     int Bindex = B.topleft;
     int Cindex = C.topleft;
 
     double[] Aarray = storage.value;
     double[] Barray = B.storage.value;
     double[] Carray = C.storage.value;
 
     int Arowspan = (ystep - cols * xstep);
     int Browspan = (B.ystep - cols * B.xstep);
     int Crowspan = (C.ystep - cols * C.xstep);
 
     for (i = rows; i > 0; i--) {
       for (j = 0; j < cols; j++) {
         Carray[Cindex] = Aarray[Aindex] * Barray[Bindex];
         Aindex += xstep;
         Bindex += B.xstep;
         Cindex += C.xstep;
       }
       Aindex += Arowspan;
       Bindex += Browspan;
       Cindex += Crowspan;
     }
     return C;
   }
 
   /**
    * X_{ij} = X_{ij}/v_{j}.
    */
   public Matrix updateScaleRowsByInvVector(Matrix vec) {
 
     int i, j;
 
     if (rows != vec.rows || vec.cols > 1) {
       throw new SizeMismatchException();
     }
     aboutToModify();
 
     double[] Aarray = storage.value;
     double[] vecarray = vec.storage.value;
     int Aindex = topleft;
     int vecindex = vec.topleft;
     int Arowspan = (ystep - cols * xstep);
 
     for (i = rows; i > 0; i--) {
       for (j = cols; j > 0; j--) {
         Aarray[Aindex] /= vecarray[vecindex];
         if (Double.isNaN(Aarray[Aindex]))
           Aarray[Aindex] = 0;
         Aindex += xstep;
       }
       vecindex += vec.ystep;
       Aindex += Arowspan;
     }
     return this;
   }
 
   /**
    * X_{ij} = X_{ij}*v_{j}, no summation.
    */
   // Should really make into and non-into versions of these.
   public void updateScaleRowsByColVector(Matrix vec) {
 
     int i, j;
 
     if (rows != vec.rows || vec.cols > 1) {
       throw new SizeMismatchException();
     }
     aboutToModify();
 
     double[] Aarray = storage.value;
     double[] vecarray = vec.storage.value;
     int Aindex = topleft;
     int vecindex = vec.topleft;
     int Arowspan = (ystep - cols * xstep);
 
     for (i = rows; i > 0; i--) {
       double multand = vecarray[vecindex];
       for (j = cols; j > 0; j--) {
         Aarray[Aindex] *= multand;
         Aindex += xstep;
       }
       vecindex += vec.ystep;
       Aindex += Arowspan;
     }
   }
 
   /**
    * Subtract a row vector from each row in this matrix. The number of entries
    * in the vector must match the number of columns in the matrix. X_{ij} =
    * X_{ij}-v_{j}.
    */
   public Matrix updateSubtractRowVector(Matrix vec) {
     int i, j;
     if (vec.cols != cols)
       throw new SizeMismatchException("Subtracting " + vec.cols + " from "
           + cols);
 
     aboutToModify();
 
     int colspan = (xstep - rows * ystep);
     int vecindex = vec.topleft;
     int arrayindex = topleft;
 
     double[] array = storage.value;
     double[] vecarray = vec.storage.value;
 
     for (j = cols; j > 0; j--) {
       double suband = vecarray[vecindex];
 
       for (i = rows; i > 0; i--) {
         array[arrayindex] -= suband;
         arrayindex += ystep;
       }
       vecindex += vec.xstep;
       arrayindex += colspan;
     }
     return this;
   }
 
   /**
    * If the matrix is a row or column vector, make a diagonal matrix from it.
    * Otherwise extract its diagonal as a row vector.
    */
   // AMB - Hmm... That could be an unfortunate duplication of functions...
   public Matrix diagonal() {
     // Unoptimised
     Matrix ret;
     int i, n;
 
     if (rows == 1) {
       ret = new Matrix(cols, cols);
       for (i = 0; i < cols; i++) {
         ret.setMval(i, i, getMval(0, i));
       }
     }
     else if (cols == 1) {
       ret = new Matrix(rows, rows);
       for (i = 0; i < rows; i++) {
         ret.setMval(i, i, getMval(i, 0));
       }
     }
     else {
       n = Math.min(rows, cols);
       ret = new Matrix(1, n);
       for (i = 0; i < n; i++) {
         ret.setMval(0, i, getMval(i, i));
       }
     }
     return ret;
   }
 
   /**
    * Compute Tr(AB) - note this is done in n^2 time, as opposed to the n^3 time
    * of multiplying AB and taking trace
    */
 
   public double traceProduct(Matrix B) {
 
     int i, j, Aindex = topleft, Bindex = B.topleft;
 
     double total = 0;
 
     double[] Aarray = storage.value;
     double[] Barray = B.storage.value;
     int Arowspan = (ystep - cols * xstep);
     int Bcolspan = (B.xstep - B.rows * B.ystep);
 
     for (i = rows; i > 0; i--) {
       for (j = cols; j > 0; j--) {
         total += Aarray[Aindex] * Barray[Bindex];
         Aindex += xstep;
         Bindex += ystep;
       }
       Aindex += Arowspan;
       Bindex += Bcolspan;
     }
     return total;
   }
 
   /**
    * Invert the matrix if possible. There is lazy evaluation of the LU
    * decomposition. So the first routine to require its presence causes its
    * creation.
    */
   public Matrix inverse() throws SingularException {
 
     if (LUD == null) {
       LUD = new LUDecomp(this);
     }
 
     return (LUD.luinvert());
   }
 
   /**
    * Compute the determinant of this matrix if possible. This routine also makes
    * use of the LU decomposition. It is party to the lazy evaluation of the LUD
    * variable if it has not already been done.
    */
   public double determinant() throws SingularException {
     if (LUD == null) {
       LUD = new LUDecomp(this);
     }
 
     return (LUD.ludeterminant());
   }
 
   /**
    * Return a copy of the array as a 2D array of doubles. This is a new copy of
    * the contents not just a reference to the internals of the matrix object.
    */
   public double[][] asArray() {
     // Unoptimised
     double[][] res = new double[rows][cols];
 
     for (int i = 0; i < rows; i++) {
       for (int j = 0; j < cols; j++) {
         res[i][j] = getMval(i, j);
       }
     }
     return (res);
   }
 
   /**
    * Compute and return the eigen values of this matrix. Since the eigen vectors
    * are computed as a side effect the results of both computations are cached
    * if the other value is requested.
    * 
    * @returns The matrix of eigen values sorted into increasing magnitude.
    */
   public Matrix eigenValues() {
     if (eigenVal == null)
       Jacobi(JacobiIter);
 
     return (eigenVal);
   }
 
   /**
    * Compute and return the eigen vectors of this matrix. Since the eigen values
    * are computed as a side effect the results of both computations are cached
    * if the other is requested.
    * 
    * @returns The matrix of eigen vectors (as columns of the matrix) with the
    *          same ordering as that found in the eigen values.
    */
   public Matrix eigenVectors() {
     if (eigenVec == null)
       Jacobi(JacobiIter);
 
     return (eigenVec);
   }
 
   /**
    * A direct port of the Numerical Recipes version of the Jacobi algorithim.
    * Slightly cleaned up in java it does set the class variables for both the
    * Eigen Vectors and Values Matrix variables. It is intended to be applied in
    * a single shot mode providing lazy evaluation of either. i.e. when it is
    * first called it populates these values for later use.
    * 
    * @param iter is the maximum number of Iterations that should be conducted in
    *          the primary loop before the system throws a
    *          JacobiIterationsExhaustedException. This is almost certainly very
    *          bad if it happens. NR suggests a value of 50 for this parameter.
    * @exception NotSquareException the routine was called on an inappropriate
    *              non-square matrix.
    * @exception JacobiIterationsExhaustedException the routine has still failed
    *              to zero the off diagonals even after the maximum Iteration
    *              count has been exceded.
    */
   // NB AMB - that's not lazy evaluation, it's caching isn't it?
   // NB2 - this is almost certainly too big to go here... I wonder where
   // we can put it...
   private void Jacobi(int iter) {
 
     if (rows != cols) {
       throw new NotSquareException();
     }
 
     int nrot = 0, n = rows;
 
     double sm, thresh, theta, tau, t, s, g, h, c;
     double[] b, d, z;
     double[][] a, v;
 
     b = new double[n];
     d = new double[n];
     z = new double[n];
 
     v = (Matrix.identity(n)).asArray();
     a = this.asArray();
 
     for (int ip = 0; ip < n; ip++) {
 
       // Copy the diagonal of this matrix into b and d
       // and ensure z is initalised to 0.0.
       b[ip] = a[ip][ip];
       d[ip] = b[ip];
       z[ip] = 0.0;
     }
 
     for (int i = 0; i < iter; i++) {
 
       // Sum the off-diagonal elements.
       sm = 0.0;
       for (int ip = 0; ip < n - 1; ip++) {
         for (int iq = ip + 1; iq < n; iq++) {
           sm += Math.abs(a[ip][iq]);
         }
       }
       Logger.println("Jacobi sweep " + i + " offdiagonal sum " + sm,
           Logger.DEBUG_SUBATOMIC);
       /*
        * for (int q = 0; q < n; ++ q) { Logger.print(a[q][q] + " ",
        * Logger.DEBUG_SUBATOMIC); } Logger.println("", Logger.DEBUG_SUBATOMIC);
        */
       if (sm == 0.0) {
         // This was the GOTO 99 and the termiation
         // point for the routine. Assumes that the
         // off diagonal sum will eventually converge on
         // zero.
 
         // Slight variation from the Numerical recipes
         // version here. We sort the Eigen values and
         // their associated vectors.
 
         int k;
         double p;
 
         for (i = 0; i < n - 1; i++) {
           k = i;
           p = d[i];
 
           for (int j = i + 1; j < n; j++) {
             if (d[j] >= p) {
               k = j;
               p = d[j];
             }
           }
 
           if (k != i) {
             d[k] = d[i];
             d[i] = p;
 
             for (int j = 0; j < n; j++) {
               p = v[j][i];
               v[j][i] = v[j][k];
               v[j][k] = p;
             }
           }
         }
 
         eigenVal = new Matrix(d);
         eigenVec = new Matrix(v);
         return;
 
       }
 
       // On the first 3 Iterations (0,1,2)
       if (i < 3)
         thresh = 0.2 * sm / (n * n);
       else
         thresh = 0.0;
 
       for (int ip = 0; ip < n - 1; ip++) {
         for (int iq = ip + 1; iq < n; iq++) {
           g = 100.0 * Math.abs(a[ip][iq]);
 
           // After 4 sweeps, skip the rotation if
           // the off-diagonal element is small.
 
           if (i >= 3 && (Math.abs(d[ip]) + g == Math.abs(d[ip]))
               && (Math.abs(d[iq]) + g == Math.abs(d[iq])))
             a[ip][iq] = 0.0;
           else if (Math.abs(a[ip][iq]) > thresh) {
             h = d[iq] - d[ip];
             if (Math.abs(h) + g == Math.abs(h))
               t = a[ip][iq] / h;
             else {
               theta = 0.5 * h / a[ip][iq];
               t = 1.0 / (Math.abs(theta) + Math.sqrt(1.0 + (theta * theta)));
               if (theta < 0.0)
                 t = -t;
             }
             c = 1.0 / Math.sqrt(1 + (t * t));
             s = t * c;
             tau = s / (1.0 + c);
             h = t * a[ip][iq];
             z[ip] = z[ip] - h;
             z[iq] = z[iq] + h;
             d[ip] = d[ip] - h;
             d[iq] = d[iq] + h;
             a[ip][iq] = 0.0;
 
             for (int j = 0; j < ip - 1; j++) {
               // Case of rotations
               // 0 <= j < p-1
               g = a[j][ip];
               h = a[j][iq];
 
               a[j][ip] = g - s * (h + g * tau);
               a[j][iq] = h + s * (g - h * tau);
             }
 
             for (int j = ip + 1; j < iq - 1; j++) {
 
               // Case of rotations
               // p < j < q
 
               g = a[ip][j];
               h = a[j][iq];
 
               a[ip][j] = g - s * (h + g * tau);
               a[j][iq] = h + s * (g - h * tau);
             }
 
             for (int j = iq + 1; j < n; j++) {
 
               g = a[ip][j];
               h = a[iq][j];
 
               a[ip][j] = g - s * (h + g * tau);
               a[iq][j] = h + s * (g - h * tau);
             }
 
             for (int j = 0; j < n; j++) {
 
               g = v[j][ip];
               h = v[j][iq];
 
               v[j][ip] = g - s * (h + g * tau);
               v[j][iq] = h + s * (g - h * tau);
             }
 
             nrot++;
           }
         }
       }
       for (int ip = 0; ip < n; ip++) {
         b[ip] = b[ip] + z[ip];
         d[ip] = b[ip];
         z[ip] = 0.0;
       }
     }
     // We should never get here, the loop should have terminated
     // well before we reach this point for any sensible iter value
     // Numerical recipes uses a rather arbitrary value of 50 but
     // we leave it open.
 
     throw new JacobiIterationsExhaustedException("After: " + iter
         + " Iterations");
   }
 
   /**
    * Allow the matrix to be converted to a string representation for debugging
    * purposes.
    * 
    * @returns String form of the matrix.
    */
   public String toString() {
     if (rows * cols > 1000)
       return "<matrix too big for string representation>";
     CharWrap res = new CharWrap();
 
     for (int i = 0; i < rows; i++) {
       for (int j = 0; j < cols; j++) {
         if (j != 0) {
          
         res.append(fieldSeparator).append(decFormat.format(getMval(i, j)));
         }
         else {
           res.append(decFormat.format(getMval(i, j)));
         }
       }
       if (i < rows) {
         res.append("\n");
       }
     }
     return res.toString();
   }
 
   public void write(Writer w) throws IOException {
     for (int i = 0; i < rows; i++) {
       for (int j = 0; j < cols; j++) {
         w.write(Double.toString(getMval(i, j)));
         w.write(" ");
       }
       w.write("\n");
     }
   }
 
   public void debugsize() {
     System.out.println(rows + "x" + cols + " xstep: " + xstep + " ystep: "
         + ystep + " topleft: " + topleft + " in " + storage.value.length
         + " array");
   }
 
   public void debug() {
     System.out.println("xstep: " + xstep + " ystep: " + ystep + " topleft: "
         + topleft);
     for (int i = 0; i < rows; i++) {
       for (int j = 0; j < cols; j++) {
         if (j != 0)
           System.out.print(fieldSeparator + getMval(i, j));
         else
           System.out.print(getMval(i, j));
       }
       System.out.println();
     }
   }
 
   /**
    * Return the number of rows in the matrix.
    * 
    * @returns n the number of rows in the matrix.
    */
   public int rows() {
     return rows;
   }
 
   /**
    * Return the number of columns in the matrix.
    * 
    * @returns n the number of columns in the matrix.
    */
   public int cols() {
     return cols;
   }
 
   /**
    * Return a submatrix. The indicies of the matrix run in accordance with
    * standard notation from 1,1 in the top left corner to m,n in the bottom
    * right corner.
    * 
    * @param y the y coordinate of the top left corner
    * @param x the x coordinate of the top left corner
    * @param width the number of cells to extract in the x direction
    * @param height the number of cells to extract in the y direction.
    * @returns the submatrix.
    */
 
   // NB AMB - might be better to swap width and height arguments for consistency
   // subject to checking all the code which could reference this method.
   public Matrix getSubMatrix(int y, int x, int width, int height) {
     --y;
     --x; // return the arguments to 0-based versions.
     // This method now mysteriously short!
     if (x < 0 || y < 0) { // Negative or zero index errors.
       throw new ArrayIndexOutOfBoundsException(y + "," + x);
     }
 
     if ((x + width) > cols || (y + height) > rows) {
       throw new ArrayIndexOutOfBoundsException(y + "," + x + "+" + width + ","
           + height);
     }
 
     Matrix res = new Matrix(height, width, false);
     res.storage = storage;
     storage.references++;
     res.topleft = topleft + x * xstep + y * ystep;
     res.xstep = xstep;
     res.ystep = ystep;
     // Hahaaaargh! Our work here is done!
     return res;
   }
 
   /**
    * Set a submatrix. The indices of the matrix run in accordance with standard
    * notation from 1,1 in the top left corner to m,n in the bottom right corner.
    * 
    * @param y the y coordinate of the top left corner
    * @param x the x coordinate of the top left corner
    * @param data the matrix to be inserted at this location
    */
   public void setSubMatrix(int y, int x, Matrix data) {
     int i, j;
     --y;
     --x;
     // Unoptimised
     if (x < 0 || y < 0 || x >= cols || y >= rows) {
       throw new ArrayIndexOutOfBoundsException(y + "," + x);
     }
 
     if ((x + data.cols) > cols || (y + data.rows) > rows) {
       throw new ArrayIndexOutOfBoundsException(y + "," + x + "+" + data.rows
           + "," + data.cols);
     }
 
     aboutToModify();
     double[] Aarray = storage.value;
     double[] Barray = data.storage.value;
 
     int Aindex = x * xstep + y * ystep;
     int Bindex = data.topleft;
 
     int Arowspan = ystep - data.cols * xstep;
     int Browspan = data.ystep - data.cols * data.xstep;
 
     for (i = data.rows; i > 0; i--) {
       for (j = data.cols; j > 0; j--) {
         Aarray[Aindex] = Barray[Bindex];
         Aindex += xstep;
         Bindex += data.xstep;
       }
       Aindex += Arowspan;
       Bindex += Browspan;
     }
   }
 
   /**
    * Get the value in the associated matrix element. The index is in accordance
    * with the standard notation 1,1 is top left and m,n is bottom right.
    * 
    * @param y row index
    * @param x column index
    * @returns the value at this location.
    */
   public double getValue(int y, int x) {
     if (x <= 0 || y <= 0) { // Negative or zero index errors.
       throw new ArrayIndexOutOfBoundsException(y + "," + x);
     }
     if (x > cols || y > rows) { // Outside this matrix range.
       throw new ArrayIndexOutOfBoundsException(y + "," + x);
     }
     return getMval(y - 1, x - 1);
   }
 
   /**
    * Set the value in the associated matrix element. The index is in accordance
    * with the standard notation 1,1 is top left and m,n is bottom right.
    * 
    * @param y row index
    * @param x column index
    */
   public void setValue(int y, int x, double val) {
     if (x <= 0 || y <= 0) { // Negative or zero index errors.
       throw new ArrayIndexOutOfBoundsException(y + "," + x);
     }
     if (x > cols || y > rows) { // Outside this matrix range.
       throw new ArrayIndexOutOfBoundsException(y + "," + x);
     }
     aboutToModify();
     setMval(y - 1, x - 1, val);
   }
 
   /**
    * Writes the supplied value into both (y,x) and (x,y) entries of the matrix,
    * keeping it in a symmetric condition.
    * 
    * @param y index 1
    * @param x index 2
    * @param val The value to be set
    */
   public void setSymm(int y, int x, double val) {
     aboutToModify();
     setMval(y - 1, x - 1, val);
     setMval(x - 1, y - 1, val);
   }
 
   public void finalize() {
     //	  System.out.println("Matrix finalized: ");
     //	  debug();
     storage.references--;
     storage = null; // Cast him off into the blue yonder!
   }
 
   public static void main(String[] argv) {
     /*
      * double[][] arrA = {{1,0},{0,2}}; Matrix A = new Matrix(arrA); A.debug();
      * double[][] arrB = {{2,0},{0,1}}; Matrix B = new Matrix(arrB); Matrix C =
      * A.multipliedBy(B); C.debug(); Matrix vec = new Matrix(new double[]
      * {1,1}); C.updateSubtractRowVector(vec); C.debug(); Matrix top2 =
      * C.getSubMatrix(1,1,1,2); System.out.println("Top 2 entries are ");
      * top2.debug(); top2.updateSqr(); top2.debug(); C.debug();
      */
 
     /*
      * double[][] arrC = {{0,0},{0,1},{1,1},{1,0}}; Matrix C = new Matrix(arrC);
      * double[] arrprob = {0.2, 0.1, 0.2, 0.1}; Matrix prob = new
      * Matrix(arrprob); Matrix mean = Probability.Probability.mean(C, prob);
      * mean.debug(); Matrix sig = Probability.Probability.covariance(C, prob);
      * sig.debug(); Probability.normalPDF n = new Probability.normalPDF(mean,
      * sig); Matrix D = n.evaluate(C); D.debug();
      */
   }
 }
