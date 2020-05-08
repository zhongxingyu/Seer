 import java.util.ArrayList;
 
 
 public class VectorMath {
 	
 	public static double getEucledianDistance(Vector a, Vector b)
 	{
 		//subtract corresponding elements and then square the elements
 		int siz1 = a.getSize();
 		int siz2 = b.getSize();
 		double product = 0.0;
 		
 		if(siz2 > siz1) 
 		{
 			a = a.makeEqualLength(a, b);
 			siz1 = siz2;
 		}
 		else if(siz2 < siz1)
 		{
 			b = b.makeEqualLength(b, a);
 		}
 		
 		for(int i = 0; i < siz1; i++)
 		{
 			product += Math.pow(a.getElement(i) - b.getElement(i), 
 					2);
 		}
 		//take square root
 		return Math.sqrt(product);
 	}
 	public static double getEucledianDistance(Vector a)
 	{
 		return getEucledianDistance(a, new Vector(a));
 	}
 	public static int getDimensionLength(Vector a)
 	{
 		return a.getSize();
 	}
 	public static double getDotProduct(Vector a, Vector b)
 	{
 		int siz1 = a.getSize();
 		int siz2 = b.getSize();
 		double product = 0.0;
 		
 		if(siz2 > siz1) 
 		{
 			a = a.makeEqualLength(a, b);
 			siz1 = siz2;
 		}
 		else if(siz2 < siz1)
 		{
 			b = b.makeEqualLength(b, a);
 		}
 		
 		for(int i = 0; i < siz1; i++)
 		{
 			product += a.getElement(i) * b.getElement(i);
 		}
 		return product;
 	}
 	public static double getManhattanDistance(Vector a, Vector b)
 	{
 		int siz1 = a.getSize();
 		int siz2 = b.getSize();
 		double product = 0.0;
 		
 		if(siz2 > siz1) 
 		{
 			a = a.makeEqualLength(a, b);
 			siz1 = siz2;
 		}
 		else if(siz2 < siz1)
 		{
 			b = b.makeEqualLength(b, a);
 		}
 		
 		for(int i=0; i<siz1; i++)
 		{
 			product += Math.abs(a.getElement(i) - b.getElement(i));
 		}
 		return product;
 	}
 	
 	public static double getPearsonCorrelation(Vector a, Vector b)
 	{
 		int siz1 = a.getSize();
 		int siz2 = b.getSize();
 		double product = 0.0;
 		
 		if(siz2 > siz1) 
 		{
 			a = a.makeEqualLength(a, b);
 			siz1 = siz2;
 		}
 		else if(siz2 < siz1)
 		{
 			b = b.makeEqualLength(b, a);
 		}
 		
 		for(int i = 0; i < siz1; i++)
 		{
 			product += (a.getElement(i) - vectorMean(a)) * 
 				(b.getElement(i) - vectorMean(b));
 		}
 		
 		product /= (siz1 - 1) * standardDeviation(a) * standardDeviation(b);
 		return product;
 	}
 	
 	public static void LSMrow(Vector a)
 	{
 		double largest = 0; 
 		double mean = 0;
 		double smallest = Double.MAX_VALUE;
 		
 		//going through all the numbers
 		for(int i =0; i < a.getSize(); i++)
 		{
 			if(a.getElement(i) >= largest)
 			{
 				largest = a.getElement(i);
 			}
 			if(a.getElement(i) <= smallest)
 			{
 				smallest = a.getElement(i);
 			}
 			mean += a.getElement(i); 
 		}
 		mean /= a.getSize();
 		
 		System.out.println("Largest number: " + largest + "  Smallest number: " + smallest
 				+ "  Mean: " + mean);	
 	}
 	
 	public static void LSMcolumn(ArrayList<Vector> lis, int colTarget)
 	{
 		double largest = 0; 
 		double mean = 0;
 		double smallest = Double.MAX_VALUE;
 		
 		//going through all the numbers
 		for(int i =0; i < lis.size(); i++)
 		{
 			if(lis.get(i).getElement(colTarget) >= largest)
 			{
 				largest = lis.get(i).getElement(colTarget);
 			}
 			if(lis.get(i).getElement(colTarget) <= smallest)
 			{
 				smallest = lis.get(i).getElement(colTarget);
 			}
 			mean += lis.get(i).getElement(colTarget); 
 		}
 		mean /= lis.size();
 		
 		System.out.println("Largest number: " + largest + "  Smallest number: " + smallest
 				+ "  Mean: " + mean);
 	}
 	
 	public static double vectorMean(Vector a)
 	{
 		double sum = 0;
 		for(int i = 0; i < a.getSize(); i++)
 		{
 			sum += a.getElement(i);
 		}
 		sum /= a.getSize();
 		return sum;
 	}
 	public static double standardDeviation(Vector v)
 	{
 		double mean = 0;
 		final int n = v.getSize();
 		double sum = 0;
 		if(n<2)
 		{
 			return Double.NaN;
 		}
 		for(int i = 0; i< n; i++)
 		{
 			mean = mean + v.getElement(i);
 		}
 		mean /= n;
 		
 		for (int i = 0; i< n; i++)
 		{
 			double b = v.getElement(i) - mean;
 			sum = sum + b *b;
 		}
 		
		double fin = Math.sqrt(sum / (n));
 		return fin;	
 	}
 	
 	public static double standardDeviation(ArrayList<Vector> lis, int colTarget)
 	{
 		double mean = 0;
 		final int n = lis.size();
 		double sum = 0;
 		if(n<2)
 		{
 			return Double.NaN;
 		}
 		for(int i = 0; i< n; i++)
 		{
 			mean = mean + lis.get(i).getElement(colTarget);
 		}
 		mean /= n;
 		
 		for (int i = 0; i< n; i++)
 		{
 			double b = lis.get(i).getElement(colTarget) - mean;
 			sum = sum + b *b;
 		}
 		
		double fin = Math.sqrt(sum / (n));
 		return fin;
 	}
 
 }
