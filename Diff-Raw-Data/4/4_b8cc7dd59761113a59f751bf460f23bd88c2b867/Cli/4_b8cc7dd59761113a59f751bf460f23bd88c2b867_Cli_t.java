 package ui;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.Vector;
 
 import misc.PolyFunction;
 import finders.FinderInterface;
 import finders.LinearFinder;
 import finders.NewtonFinder;
 import finders.QuadraticFinder;
 
 public class Cli {
 	public void run() {
 		System.out.println("Welcome to PolynomialSolver");
 		
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 
 		try {
 			
 			int grade = 0;
 			System.out.print("Polynomial of what grade: ");
 			grade = Integer.valueOf(br.readLine());
 			
 			PolyFunction f = new PolyFunction();
 			for (int i = grade; i >= 0; i--) {
 				System.out.print("x^" + i + ": ");
 				f.setCoeff(Double.valueOf(br.readLine()), i);
 			}
 			
 			System.out.println(f);
 			
 			FinderInterface finder = null;
 			switch (grade) {
 				case 0:
					System.out.println("No possible solution for grade 0");
					return;
 				case 1:
 					finder = new LinearFinder();
 					break;
 				case 2:
 					finder = new QuadraticFinder();
 					break;
 				default:
 					finder = new NewtonFinder();
 					break;
 			}
 			
 			Vector<Double> results = finder.find(f);
 			
 			if (results.size() == 0) {
 				System.out.println("no result");
 			} else {
 				int i = 0;
 				for (Double x : results) {
 					System.out.println("x" + i++ + " = " + x);
 				}
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	public static void main(String[] args) {
 		Cli cli = new Cli();
 		cli.run();
 	}
 }
