 /**
  * 
  */
 package edu.pw.gis.gui;
 
 import java.awt.Container;
 import java.text.DecimalFormat;
 import java.util.Scanner;
 
 import javax.swing.JFrame;
 
 import edu.pw.gis.algorithm.Genetic;
 import edu.pw.gis.parser.SNDParser;
 
 /**
  * @author ymir
  * 
  */
 public class Main {
 
 
 	static String FILE_PATH = "xml/big_test.xml";
 	static int MAX_WEIGHT = 65535;
 	static int INITIAL_POPULATION = 64;
 	static double ALFA_RATE = 0.2;
 	static double BETA_RATE = 0.7;
 	static double MUTATION_RATE = 0.01;
 	static double CROSS_RATE = 0.7;
 	static double MAX_USAGE = 0.2;
 	static int MAX_ITERATION_NO = 65535;
 	static JFrame frame = new JFrame();
 
 	public static void main_interface() {
 		DecimalFormat twoDigit = new DecimalFormat("#,##0.00");
 		Scanner in = new Scanner(System.in);
 		int decision;
 		do {
 			System.out.println("Wybierz: ");
			System.out.println("1. Sciezka do pliku (ustawione = " + FILE_PATH + ")");
			System.out.println("2. Ustaw maksymalna wage (ustawione = " + MAX_WEIGHT
 					+ ")");
			System.out.println("3. Usatw rozmiar populacji (ustawione = "
 					+ INITIAL_POPULATION + ")");
			System.out.println("4. Ustaw wsp. alfa (ustawione = " + twoDigit.format(ALFA_RATE) + ")");
			System.out.println("5. Ustaw wsp. beta (ustawione = " + twoDigit.format(BETA_RATE) + ")");
			System.out.println("6. Ustaw wsp. mutacji (ustawione = "
					+ twoDigit.format(MUTATION_RATE) + ")");
			System.out.println("7. Ustaw wsp. krosowania (ustawione = " + twoDigit.format(CROSS_RATE)
 					+ ")");
			System.out.println("8. Ustaw uzycie zadowalajace (ustawione = " + twoDigit.format(MAX_USAGE)
 					+ ")");
			System.out.println("9. Ustaw maksymalna liczbe pokolen (ustawione = "
 					+ MAX_ITERATION_NO + ")");
 			System.out.println("10. Uruchom algorytm");
 			System.out.println("0. Zakoncz");
 
 			decision = in.nextInt();
 
 			switch (decision) {
 			case 1:
 				System.out.println("Sciezka = ");
 				FILE_PATH = in.next();
 				break;
 			case 2:
 				System.out.println("Maksymalna waga (integer value <1,65535>) = ");
 				MAX_WEIGHT = in.nextInt();
 				break;
 			case 3:
 				System.out.println("Rozmiar populacji (integer value (0,65535>) = ");
 				INITIAL_POPULATION = in.nextInt();
 				break;
 			case 4:
 				System.out.println("Wsp. alfa (double value <0,1>) = ");
 				ALFA_RATE = in.nextDouble();
 				break;
 			case 5:
 				System.out.println("Wsp. beta (double value <0,1>) = ");
 				BETA_RATE = in.nextDouble();
 				break;
 			case 6:
 				System.out.println("Wsp. mutacji (double value <0,1>) = ");
 				MUTATION_RATE = in.nextDouble();
 				break;
 			case 7:
 				System.out.println("Wsp. krosowania (double value <0,1>) = ");
 				CROSS_RATE = in.nextDouble();
 				break;
 			case 8:
 				System.out.println("Uzycia zadowalajace (double value) (0,1> = ");
 				MAX_USAGE = in.nextDouble();
 				break;
 			case 9:
 				System.out.println("Liczba pokolen (integer value (0,65535>) = ");
 				MAX_ITERATION_NO = in.nextInt();
 				break;
 			case 10:
 				run_algorithm();
 				break;
 			case 0:
 				frame.dispose();
 				break;
 			default:
 				System.out
 						.println("-------------------------------------------");
 				System.out.println("Bad choose");
 				System.out
 						.println("-------------------------------------------");
 				break;
 			}
 
 		} while (decision != 0);
 
 		System.out.println("-------------------------------------------");
		System.out.println("Zakonczono dzialanie programu.");
 		System.out.println("-------------------------------------------");
 	}
 
 	public static void run_algorithm() {
 		SNDParser snd_parser = new SNDParser(FILE_PATH);
 		Genetic genetic = new Genetic(MAX_WEIGHT, INITIAL_POPULATION,
 				ALFA_RATE, BETA_RATE, MUTATION_RATE, CROSS_RATE, MAX_USAGE, MAX_ITERATION_NO);
 
 		genetic.createInitialPopulation(snd_parser.graph);
 
 		genetic.go(System.currentTimeMillis());
 
 		edu.pw.gis.graph.Graph top = genetic.graph_list.get(0);
 		genetic.printHighestUsage();
 		System.out.println("Graf wynikowy: ");
 		top.printAdjList();
 		
 //		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Container content = frame.getContentPane();
		content.removeAll();
 		content.add(new PrettyGraph(top));
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	public static void main(String[] args) throws CloneNotSupportedException {
 		main_interface();
 	}
 
 }
