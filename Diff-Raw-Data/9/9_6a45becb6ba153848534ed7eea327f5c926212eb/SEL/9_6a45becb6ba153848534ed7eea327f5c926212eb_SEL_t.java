 package poo.esempi;
 
 import poo.sistema.*;
 import java.util.*;
 
 public class SEL { // Sistema di Equazioni Lineari
 	public static void main(String[]args) {
 		System.out.println("Risolutore di un sistema di equazioni lineari (quadrato) mediante il metodo di Gauss e Gauss-diagonale");
 		Scanner sc = new Scanner(System.in);
 		System.out.print("Dimensione sistema = ");
 		int n = sc.nextInt();
 		double[][] a = new double[n][n];
 		double[] y = new double[n];
 		System.out.println("Inserisci i coefficienti del sistema:");
 		for (int i = 0; i < n; i++)
 			for (int j = 0; j < n; j++)
 				a[i][j] = sc.nextDouble();
 		System.out.println("Inserisci i termini noti:");
 		for (int i = 0; i < n; i++)
 			y[i] = sc.nextDouble();
 		Sistema s1 = new Gauss(a, y);
 		Sistema s2 = new GaussDiagonale(a, y);
		System.out.println("\nSistema inserito:\n" + s1);
 		double[] x1 = null, x2 = null;
 		try {
 			x1 = s1.risolvi();
 			x2 = s2.risolvi();
 		} catch (SistemaSingolare e) {
 			System.out.println("Sistema singolare!");
 			System.exit(-1);
 		}
 		System.out.println("Sistema triangolarizzato:\n" + s1);
 		System.out.println("Sistema diagonalizzato:\n" + s2);
 		System.out.print("Soluzione metodo 1: [");
 		for (int i = 0; i < n; i++)
 			System.out.print(String.format("%1.2f", x1[i]) + (i < n-1 ? ", " : "]\n"));
 		System.out.print("Soluzione metodo 2: [");
 		for (int i = 0; i < n; i++)
 			System.out.print(String.format("%1.2f", x2[i]) + (i < n-1 ? ", " : "]\n"));
 	} // main
 } // SEL
