 package at.ac.tuwien.infosys.lsdc.scheduler.matrix.twoDimensional;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 
 import at.ac.tuwien.infosys.lsdc.scheduler.matrix.Matrix;
 import at.ac.tuwien.infosys.lsdc.scheduler.matrix.MatrixIllegalOperationException;
 import at.ac.tuwien.infosys.lsdc.scheduler.matrix.MatrixIterator;
 import at.ac.tuwien.infosys.lsdc.scheduler.matrix.MultiNumber;
 
 public class MatrixHelper {
 	public static Matrix<Double> calculateRowMean(Matrix<?> matrix) {
 		int[] dim = matrix.getDimensions();
 		int pos = 0;
 		Number[] rowMean = new Number[dim[0]];
 		if (isOneDimensional(matrix)|| isTwoDimensional(matrix)) {
 			getRowMean(matrix, pos, rowMean);
 			Matrix<Double> returnVector = new Matrix<Double>(Double.class, 1,rowMean.length);
 			returnVector.setMatrix(new Object[] {rowMean});
 			return returnVector;
 		}
 		return null;
 	}
 	
 	public static Number getElement(Matrix<Double> matrix, int x,int y) {
 		Object matrixObject = matrix.getMatrixObject();
 		Object row = Array.get(matrixObject, y);
 		return (Number)Array.get(row, x);
 	}
 	
 	public static Matrix<Float> calculateRowMeanFloat(Matrix<?> matrix) {
 		int[] dim = matrix.getDimensions();
 		int pos = 0;
 		Number[] rowMean = new Number[dim[0]];
 		if (isOneDimensional(matrix)|| isTwoDimensional(matrix)) {
 			getRowMean(matrix, pos, rowMean);
 			Matrix<Float> returnVector = new Matrix<Float>(Float.class, 1,rowMean.length);
 			returnVector.setMatrix((Object[])rowMean);
 			return returnVector;
 		}
 		return null;
 	}
 	
 	public static Matrix<?> addRow(Matrix<?> matrix, Object[] row) {
 		if (isTwoDimensional(matrix)) {
 			Object [] twoDimMatrix = (Object [])matrix.getMatrixObject();
 			int size = getCurrentYSize(twoDimMatrix);
 			
 			// check if a row can be added
			if (size == matrix.getDimensions()[0]) {
 				return null;
 			}
 			
 			Array.set(twoDimMatrix, size, row);
 			return matrix;
 		}
 		return null;
 	}
 	
 	private static int getCurrentYSize(Object[] matrixObject) {
 		int count = 0;
 		for (Object row : matrixObject) {
 			Object value = Array.get(row, 0);
 			if (value != null) {
 				count++;
 			}
 			else {
 				break;
 			}
 		}
 		return count;
 	}
 
 	private static void getRowMean(Matrix<?> matrix, int pos, Number[] rowMean) {
 		MatrixIterator iterator = matrix.getRowIterator();
 		try {
 			Field field = matrix.getType().getField("TYPE");
 
 
 			while (iterator.hasNext()) {
 				Number[] currentNumberRow = iterator.next();
 				Constructor<?> initializerConstructor = matrix.getType().getConstructor((Class<?>)field.get(currentNumberRow[0]));
 				rowMean[pos] = (Number)initializerConstructor.newInstance((Object)0);
 				for (Number currentNumber : currentNumberRow) {
 					rowMean[pos] = MultiNumber.add(rowMean[pos], currentNumber);
 				}
 				rowMean[pos] = MultiNumber.div(rowMean[pos], currentNumberRow.length);
 				pos++;
 			}
 		} catch (SecurityException e) {
 			throw new MatrixIllegalOperationException(e.getMessage(), e.getCause());
 		} catch (NoSuchFieldException e) {
 			throw new MatrixIllegalOperationException(e.getMessage(), e.getCause());
 		} catch (IllegalArgumentException e) {
 			throw new MatrixIllegalOperationException(e.getMessage(), e.getCause());
 		} catch (IllegalAccessException e) {
 			throw new MatrixIllegalOperationException(e.getMessage(), e.getCause());
 		} catch (NoSuchMethodException e) {
 			throw new MatrixIllegalOperationException(e.getMessage(), e.getCause());
 		} catch (InstantiationException e) {
 			throw new MatrixIllegalOperationException(e.getMessage(), e.getCause());
 		} catch (InvocationTargetException e) {
 			throw new MatrixIllegalOperationException(e.getMessage(), e.getCause());
 		} 
 	}
 
 	
 	private static boolean isOneDimensional(Matrix<?> matrix) {
 		return (getDepth(matrix.getMatrixObject()) == 1);
 	}
 	
 	private static boolean isTwoDimensional(Matrix<?> matrix) {
 		return (getDepth(matrix.getMatrixObject()) == 2);
 	}
 	
 
 	private static Integer getDepth(Object matrix) {
 		int depth = 0;
 		if (matrix != null) {
 			Class<?> matrixClass = matrix.getClass();
 			if (matrixClass.isArray()) {
 				depth += getDepth(Array.get(matrix, 0));
 				return ++depth;
 			}
 			else return 0;
 		}/*
 		Class<?> matrixClass = at.ac.tuwien.infosys.lsdc.scheduler.matrix.getClass();
 		if (matrixClass != null && matrixClass.isArray()) {
 			depth += getDepth(Array.get(at.ac.tuwien.infosys.lsdc.scheduler.matrix, 0));
 			return ++depth;
 		}
 		else {
 			return 0;
 		}*/
 		return 0;
 	}
 }
