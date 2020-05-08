 package edu.mwdb.project;
 
 import matlabcontrol.MatlabInvocationException;
 import matlabcontrol.MatlabProxy;
 
 public class MatLab {
 	
 	MatlabProxy proxy;
 	public MatLab(MatlabProxy proxy) {
 		this.proxy = proxy;
 	}
 	
 	/**
 	 * Computes the SVD
 	 * @param matrix
 	 * @param rowsReturned - the top k rows of the matrix to be returned
 	 * @return the v matrix (u,s,v) transposed from computing the svd on the input matrix containing only the top k rows (k=rowsReturned) 
 	 * @throws Exception
 	 */
 	public double[][] svd(double[][] matrix, int rowsReturned) throws Exception {
 		if (matrix.length == 0)
 			throw new Exception("matrix cannot be empty");
 		int columnSize = matrix[0].length;
 		double[][] tempMatrix = new double[columnSize][columnSize];
 		
 		proxy.eval("[U,S,V]=svd(matrix);");
 		for(int k=0;k<columnSize;k++)
 		{
 			Object[] obj=proxy.returningEval("V(:,"+ (k+1) +")" ,1);
 			tempMatrix[k]=(double[])obj[0];
 		}
 		
 		if (tempMatrix[0].length < rowsReturned)
 			throw new Exception("rowsReturned is greater than the number of rows available");
 		
 		// Transpose v matrix
 		double[][] resultSematicMatrixSVD = new double[rowsReturned][columnSize];
 		for(int a=0;a<rowsReturned;a++)
 		{
 			for(int b=0;b<columnSize;b++)
 			{
 				resultSematicMatrixSVD[a][b]= tempMatrix[b][a];
 			}
 		}
 		
 		return resultSematicMatrixSVD;
 	}
 }
