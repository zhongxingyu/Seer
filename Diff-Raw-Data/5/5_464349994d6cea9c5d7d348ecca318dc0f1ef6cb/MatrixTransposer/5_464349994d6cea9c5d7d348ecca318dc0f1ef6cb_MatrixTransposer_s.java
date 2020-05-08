 package javalabs.lab_1;
 
 import java.io.*;
 import java.util.*;
 
 // Класс MatrixTransposer.
 // Выполнения последовательность действий,
 // указанных в задании.
 class MatrixTransposer
 {
 	public static void main(String[] args)
 	{
 		try
 		{
 			int rows = 1, columns = 1;
 			Scanner s = new Scanner(System.in);
 
 			System.out.print("Input matrix rows amount: ");
 			rows = s.nextInt();
 
 			System.out.print("Input matrix columns amount: ");
 			columns = s.nextInt();
 
 			Matrix matrix = new Matrix(rows, columns);
 
 			matrix.input();
 
 			System.out.println("Transposed matrix:");
 			matrix.transpose();
 			matrix.print();
 		}
		catch(IOException e)
 		{
			System.out.println("IO Exception catched: " + e);
 		}
 	}
 }
