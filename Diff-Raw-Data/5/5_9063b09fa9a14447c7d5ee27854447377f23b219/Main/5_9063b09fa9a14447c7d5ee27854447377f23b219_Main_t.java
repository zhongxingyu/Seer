 package idc.gc;
 
 import idc.gc.dt.Circle;
 import idc.gc.dt.Point;
 import idc.gc.graphics.Graphics;
 import idc.gc.strategy.AntColonyStrategy;
 import idc.gc.strategy.BlowingStrategy;
 import idc.gc.strategy.DivideAndConquerStrategy;
 import idc.gc.strategy.RandomStrategy;
 import idc.gc.strategy.Strategy;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 public class Main {
 	private static final String NEW_LINE = System.getProperty("line.separator");
 	private Benchmarker b = new Benchmarker();
 	private File input;
 	private File output;
 	private int n;
 	private int strategy = 0;
 	private boolean noGraph = false;
 
 	public Main(String in) {
 		input = new File(in);
 	}
 
 	public Main(String[] args) {
 		String inputFile;
 		String outputFile;
 
 		try {
 			if (args.length < 3) {
 				help();
 				System.exit(0);
 				return;
 			}
 			n = Integer.parseInt(args[0]);
			if (n < 1 || n > 63) {
 				help();
 				System.exit(0);
 				return;
 			}
 			inputFile = args[1];
 			outputFile = args[2];
 
 			for (int i = 3; i < args.length; i++) {
 				if (args[i].equalsIgnoreCase("-no_graph")) {
 					noGraph = true;
 				} else if (args[i].equalsIgnoreCase("-random")) {
 					strategy = 1;
 				} else if (args[i].equalsIgnoreCase("-dnc")) {
 					strategy = 2;
 				} else if (args[i].equalsIgnoreCase("-ant")) {
 					strategy = 3;
 				} else {
 					help();
 					System.exit(0);
 					return;
 				}
 			}
 		} catch (Exception e) {
 			help();
 			System.exit(0);
 			return;
 		}
 		input = new File(inputFile);
 		output = new File(outputFile);
 	}
 
 	public void run() throws IOException {
 		// Strategy str=new RandomStrategy();
 		Strategy str;
 		switch (strategy) {
 		case 1:
 			str = new RandomStrategy();
 			break;
 		case 2:
 			str = new DivideAndConquerStrategy();
 			break;
 		case 3:
 			str = new AntColonyStrategy();
 			break;
 		default:
 			str = new BlowingStrategy();
 			break;
 		}
 
 		System.out.println("Reading file " + input);
 		Set<Point> points = readFile(input);
 
 		long now = System.currentTimeMillis();
 		System.out.println("Using '" + str.getName() + "'");
 		Set<Circle> circles = str.execute(points, n);
 		long duration = System.currentTimeMillis() - now;
 
 		FileWriter fw = new FileWriter(output);
 		for (Circle c : circles) {
 			System.out.println(c);
 			fw.write(c.getP().getX() + ", " + c.getP().getY() + ", " + c.getR());
 			fw.write(NEW_LINE);
 		}
 		fw.close();
 
 		System.out.println("Took " + duration + "ms");
 		int score = b.score(points, circles);
 		System.out.println("Score: " + score);
 
 		if (!noGraph) {
 			new Graphics(points, circles, "Score for '" + str.getName() + "' with " + n + " circles: " + score + " / " + points.size())
 					.show();
 		}
 	}
 
 	public static Set<Point> readFile(File input) throws FileNotFoundException, IOException {
 		FileReader fr = new FileReader(input);
 		BufferedReader br = new BufferedReader(fr);
 		String line = null;
 		Set<Point> points = new HashSet<Point>();
 		while ((line = br.readLine()) != null) {
 			String[] parts = line.split(", ");
 			points.add(new Point(Double.parseDouble(parts[0]), Double.parseDouble(parts[1])));
 		}
 		fr.close();
 		return points;
 	}
 
 	public static void help() {
 		System.out.println("Usage:");
 		System.out.println("n in_file out_file [-no_graph] [-random | -dnc | -ant]");
		System.out.println("\tn\t\tNumber of circles. 0 < n < 64");
 		System.out.println("\tin_file\t\tInput file");
 		System.out.println("\tout_file\tOutput file");
 		System.out.println("\t-no_graph\tDo not produce graphical output at the end of the run");
 		System.out.println("\t-random\t\tUse the Naive Random algorithm");
 		System.out.println("\t-dnc\t\tUse Divide and Conquer algorithm");
 		System.out.println("\tOtherwise, uses Ant Colony algorithm");
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws IOException {
 		new Main(args).run();
 
 	}
 
 }
