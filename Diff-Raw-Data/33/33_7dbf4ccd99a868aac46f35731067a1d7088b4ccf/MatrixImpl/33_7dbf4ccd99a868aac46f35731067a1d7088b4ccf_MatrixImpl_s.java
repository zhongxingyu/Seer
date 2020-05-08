 package transformation;
 
 class MatrixImpl implements Matrix {
 
 	private final int[][] m;
 	
 	
 	public MatrixImpl(int[][] m) {
 		this.m = m;
 	}
 	
 	
 	@Override
 	public int getNumRows() {
 		return m.length;
 	}
 
 	@Override
 	public int getNumCols() {
 		return m[0].length;
 	}
 
 	@Override
 	public int get(int row, int col) {
 		return m[row][col];
 	}
 
 	@Override
 	public void set(int row, int col, int value) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
