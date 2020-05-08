 package fastmatrices;
 
 import java.util.Random;
 
 public class NeuralNetwork
 {
 	public Matrix theta1, theta2;
 	
 	
 	public NeuralNetwork(int inputNodes, int hiddenNodes, int outputNodes)
 	{
 		theta1 = new Matrix(randomArray(hiddenNodes, inputNodes + 1), hiddenNodes, inputNodes + 1);
 		theta2 = new Matrix(randomArray(outputNodes, hiddenNodes + 1), outputNodes, hiddenNodes + 1);
 	}
 	
 	
 	public void train(Matrix x, Matrix y, final double learningRate, int iterations)
 	{
 		Matrix a1, a2, a3 = null, t = null, d3, d2;
 		int mod = iterations / 5;
 		
 		for (int i = 0; i < iterations; i++)
 		{			
 			for (int j = 1; j <= x.rows; j++)
 			{
 				a1 = Utilities.appendVertical(1, x.getRowAsColumn(j));
 				a2 = Utilities.appendVertical(1, theta1.multiply(a1, sig));
 				a3 = theta2.multiply(a2, sig);
 				
 				t = y.getRowAsColumn(j);
 				
 				d3 = t.subtract(a3, new SigmoidGradient(a3.data));
 				
 				d2 = theta2.part(1, -1, 2, -1).multiplyTransposeOp1(d3,
 						new SigmoidGradient(a2.part(2, -1, 1, 1).data));
 				
 				updateWeights(theta1, d2, a1, learningRate);
 				updateWeights(theta2, d3, a2, learningRate);
 			}
 			
 			if (i % mod == 0)
 			{
 				Matrix d = t.subtract(a3);
 				Matrix e = d.multiplyTransposeOp1(d);
 				System.out.println(e.data[0]);
 			}
 		}
 	}
 	
 	
 	public double[] predict(Matrix x)
 	{
 		Matrix a1 = Utilities.appendVertical(1, x);
 		Matrix a2 = Utilities.appendVertical(1, theta1.multiply(a1, sig));
 		Matrix a3 = theta2.multiply(a2, sig);
 		
 		return a3.transpose().data;
 	}
 	
 	
 	public double[][] bulkPredict(Matrix x)
 	{
 		double[][] answer = new double[x.rows][];
 		
 		for (int i = 0; i < x.rows; i++)
 		{
 			answer[i] = predict(x.part(i + 1, i + 1, 1, -1).transpose());
 		}
 		
 		return answer;
 	}
 	
 	
 
 	
 	/**
	 * Updates the weights according to the specified parameters,
	 * i.e., calculates weights += learningRate * delta * activations'.
 	 * @param weights
 	 * @param delta
 	 * @param activations
 	 * @param learningRate
 	 * @return
 	 */
 	private Matrix updateWeights(Matrix weights, Matrix delta, Matrix activations, double learningRate)
 	{
 		int answerindex = 0, deltaindex = 0, activationsindex;
 		
 		if (delta.columns != activations.columns)
 			throw new IllegalArgumentException(String.format(
 				"non-conformant arguments (delta is %dx%d, activations is %dx%d)",
 				delta.rows, delta.columns, activations.rows, activations.columns));
 		
 		if (delta.rows != weights.rows || activations.rows != weights.columns)
 			throw new IllegalArgumentException(String.format(
 				"non-conformant arguments (weights is %dx%d, delta * activations' is %dx%d)",
 				weights.rows, weights.columns, delta.rows, activations.rows));
 		
 		for (int i = 0; i < delta.rows; i++)
 		{
 			activationsindex = 0;
 			
 			for (int j = 0; j < activations.rows; j++)
 			{
 				double sum = 0;
 				
 				for (int k = 0; k < activations.columns; k++)
 				{
 					sum += delta.data[deltaindex + k] * activations.data[activationsindex + k];
 				}
 				
 				weights.data[answerindex++] += sum * learningRate;
 				activationsindex += activations.columns;
 			}
 			
 			deltaindex += delta.columns;
 		}
 		
 		return weights;
 	}
 	
 	
 	/**
 	 * Calculates the sigmoid activation function.
 	 */
 	private Matrix.Function sig = new Matrix.Function()
 	{
 		@Override
 		public double apply(double value, int row, int col)
 		{
 			return 1.0 / (1 + Math.exp(-value));
 		}
 		
 	};
 	
 	
 	private class SigmoidGradient implements Matrix.Function
 	{
 		private double[] a;
 		
 		public SigmoidGradient(double[] a)
 		{
 			this.a = a;
 		}
 		
 		@Override
 		public double apply(double value, int row, int col)
 		{
 			double d = a[row];
 			return value * d * (1 - d);
 		}
 	}
 	
 	
 	private static double[] randomArray(int rows, int columns)
 	{
 		double[] a = new double[rows * columns];
 		Random random = new Random(1);
 		int index = 0;
 		
 		for (int i = 0; i < rows; i++)
 		{
 			for (int j = 0; j < columns; j++)
 			{
 				a[index++] = random.nextDouble();
 			}
 		}
 		
 		return a;
 	}
 }
