 import java.util.Scanner;
 
 /**
  * Calculates various bits of info about a line segment
  * @author Ian Thompson
  *
  */
 public class CoordGeom {
 	
 	/**
 	 * Prints the slope of segment AB
 	 * @param Ax The x-coordinate of point A
 	 * @param Ay The y-coordinate of point A
 	 * @param Bx The x-coordinate of point B
 	 * @param By The y-coordinate of point B
 	 */
 	private static void printSlope(double Ax, double Ay, double Bx, double By)
 	{
 		if (Ay == By)
 		{
 			System.out.println("The slope of segment AB is undefined");
 		}
 		else
 		{
 			double slope = Math.abs((Ay - By) / (Ax - Bx));
 			System.out.println("The slope of segment AB is " + slope);
 		}
 	}
 	
 	/**
 	 * Prints the length of segment AB
 	 * @param Ax The x-coordinate of point A
 	 * @param Ay The y-coordinate of point A
 	 * @param Bx The x-coordinate of point B
 	 * @param By The y-coordinate of point B
 	 */
 	private static void printLength(double Ax, double Ay, double Bx, double By)
 	{
 		double length = Math.sqrt(Math.pow(Ax - Bx, 2) + Math.pow(Ay -By, 2));
 		System.out.println("The length of segment AB is " + length);
 	}
 	
 	/**
 	 * Prints the midpoint of segment AB
 	 * @param Ax The x-coordinate of point A
 	 * @param Ay The y-coordinate of point A
 	 * @param Bx The x-coordinate of point B
 	 * @param By The y-coordinate of point B
 	 */
 	private static void printMidpoint(double Ax, double Ay, double Bx, double By)
 	{
 		double midX = (Ax + Bx) / 2;
 		double midY = (Ay + By) / 2;
		
		System.out.println("The midpoint of segment AB is (" + midX + ", " + midY + ")");
 	}
 	
 	/**
 	 * Prints the minimum x-coordinate of segment AB
 	 * @param Ax The x-coordinate of point A
 	 * @param Ay The y-coordinate of point A
 	 * @param Bx The x-coordinate of point B
 	 * @param By The y-coordinate of point B
 	 */
 	private static void printMinX(double Ax, double Ay, double Bx, double By)
 	{
 		double minX;
 		if (Ax > Bx)
 			minX = Bx;
 		else
 			minX = Ax;
 		
 		System.out.println("The minimum x-coordinate of all of AB is " + minX);
 	}
 
 	/**
 	 * The entry point. Retrieves the coordinates from the console
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		Scanner sc = new Scanner(System.in);
 		System.out.print("Input point A: ");
 		double Ax = sc.nextDouble();
 		double Ay = sc.nextDouble();
 		
 		System.out.print("Input point B: ");
 		double Bx = sc.nextDouble();
 		double By = sc.nextDouble();
 		
 		sc.close();
 		
 		printSlope(Ax, Ay, Bx, By);
 		printLength(Ax, Ay, Bx, By);
 		printMidpoint(Ax, Ay, Bx, By);
 		printMinX(Ax, Ay, Bx, By);
 	}
 
 }
