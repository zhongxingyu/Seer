 public class Matrix {
 	private int width, height;
 
 	public Matrix(width, height) {
 		this.width=width;
 		this.height=height;
 	}
 
 	public Matrix multiplyMatrix(Matrix a, Matrix b) {
 	}
 
 	public Matrix divideMatrix(Matrix a, Matrix b) {
 	}
 
 	public Matrix transformMatrix(Matrix a) {
 	}
 
 	public Matrix determinantOfMatrix(Matrix a) {
		if (a.width == a.height) {
 			//do determinant
 		} else {
 			System.out.println("Determinant is not square.");
 		}
 	}
 
 	public Matrix rref(Matrix a) {
 	}
 
 	private int getWidth() {
 		return width;
 	}
 
 	private int getHeight() {
 		return height;
 	}
 }
