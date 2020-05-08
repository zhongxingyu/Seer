 /**
  * This class specifies a nxm matrix, 
  * and some operations that can be performed on matrices. 
  */
 
 public class Matrix{
 	
 	double[][] elements;
 	int numRows, numCols; 
 	
 	/**
 	 * Creates a new empty matrix.
 	 * @param n rows in matrix
 	 * @param m columns in matrix
 	 * @throws IndexOutOfBoundsException if n or m are negative.
 	 */
 	Matrix(int n, int m){
 		if(n < 0 || m < 0)
 			throw new IndexOutOfBoundsException();
 		numRows = n;
 		numCols = m;
 		
 		elements = new double [n][m];
 		
 	}
 	
 	/**
 	 * Creates a new matrix containing elements in the array elements.
 	 * @param n rows in matrix
 	 * @param m columns in matrix
 	 * @param elements array containing the elements of the matrix
 	 */
 	Matrix(double[][] matrix){
 		if(matrix == null)
 			throw new IllegalArgumentException();
 		
 		elements = matrix;
 		numRows = matrix.length;
 		numCols = matrix[0].length;
 		
 	}
 	
 	/**
 	 * Returns an element at position [x,y].
 	 * @throws IndexOutOfBoundsException if n or m is too small/big.
 	 */
 	public double getElement(int x, int y){
 		if(x < 0 || x > numRows || y < 0 || y > numCols)
 			throw new IndexOutOfBoundsException("x or y are out of bounds; x = " + x + " y = " + y);
 		
 		return elements[x][y];
 	}
 	
 
 	/**
 	 * Returns the number of rows in Matrix.
 	 * @return number of rows in Matrix.
 	 */
 	public int getNumRows(){
 		return numRows;
 	}
 	
 	/**
 	 * Returns the number of columns in Matrix.
 	 * @return number of columns in Matrix.
 	 */
 	public int getNumCols(){
 		return numCols;
 	}
 	
 	public void roundElements(){
 		for(int i = 0; i < numRows; i++){
 			for(int j = 0; j < numCols; j++){
 				elements[i][j] = roundTwoDecimals(elements[i][j]);
 			}
 		}
 	}
 	
 	private double roundTwoDecimals(double d) {
		d =Math.round(d *100.0) /100.0;
		return d;
 	}
 	
 	
 	/**
 	 * Swaps the rows x and y in the Matrix.
 	 * @param x y indexes for the two rows.
 	 * @throws IndexOutOfBoundsException if a or b are too small/big.
 	 */
 	public void swapRows(int x, int y){
 		if(x < 0 || x > numRows || y < 0 || y > numCols)
 			throw new IndexOutOfBoundsException("x or y are out of bounds; x = " + x + " y = " + y);
 		
 		double temp;
 		for(int i = 0; i < numCols; i++){
 			temp = elements[x][i];
 			elements[x][i] = elements[y][i];
 			elements[y][i] = temp;
 		}
 		
 	}
 	
 	/**
 	 * Multiplies each element in row by scalar.
 	 * @throws IndexOutOfBoundsException if row is not a valid index. 
 	 */
 	public void multiplyRowByScalar(double scalar, int row){
 		if(row < 0 || row > numRows)
 			throw new IndexOutOfBoundsException("Row is not in matrix.");
 		
 		for(int i = 0; i < numCols; i++){
 			elements[row][i] *= scalar;
 		}
 	}
 	
 	
 	/**
 	 * Multiplies row by scalar, and adds the product to targetRow.
 	 * @throws IndexOutOfBoundsException if targetRow or addendRow are too small/big.
 	 */
 	public void addRowMultipliedByScalar(double scalar, int targetRow, int addendRow){
 		if(targetRow < 0 || targetRow > numRows || addendRow < 0 || addendRow > numRows)
 			throw new IndexOutOfBoundsException("Row is not in matrix.");
 
 		for(int i = 0; i < numCols; i++){
 			elements[targetRow][i] += scalar * elements[addendRow][i];
 		}
 		
 	}
 	
 	/**
 	 * Returns a string representation of the Matrix. 
 	 */
 	public String toString(){
 		String s ="";
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("[");
 		
 		for(int i=0; i < numRows; i++){
 			sb.append("[");
 			for(int j=0; j < numCols; j++){
 				if(elements[i][j] % 1.0 != 0){
 					sb.append(elements[i][j]);
 				}else{
 					sb.append((int)elements[i][j]);
 				}
 				sb.append(" ");
 			}
 			sb.deleteCharAt(sb.length()-1);
 			sb.append("]");
 		}
 		
 		sb.append("]");
 		
 		s=sb.toString();
 		return s;
 	}
 
 }
