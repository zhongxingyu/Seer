 import java.util.PriorityQueue;
 import java.util.Random;
 
 
 public class Main {
 	
 	private Random random;
 	private static double[] answer = {57.9, 108.2, 149.6, 228.07, 778.434, 1428.74, 2839.08, 4490.8, 5879.13};
 	private static double[] vertices = {88.0, 224.7, 365.3, 687.0, 4332, 10760, 30684, 60188, 90467};
 	
 	private static double diff(double a, double b, double c) {
 		double diff = 0;
 		for (int i = 0; i < vertices.length; ++i) {
 			// To find using a*ln(b*x+c)
 			diff += Math.pow(answer[i] - a * Math.log(b * vertices[i] + c), 2);
 			// change to following statement to use a*x^2 + b*x + c instead:
 			//diff += Math.pow(answer[i] - (a * Math.pow(vertices[i], 2) + b * vertices[i] + c), 2);
 		}
 		return diff;
 	}
 	
 	public static void main(String[] args) {
 		new Main();
 	}
 	
 	public Main() {
 		random = new Random(System.currentTimeMillis());
 		PriorityQueue<Neuron> neurons = new PriorityQueue<Neuron>();
		// Fill the priority queue with 33 default neurons
 		for (int i = 0; i < 33; ++i) {
 			neurons.add(new Neuron(1, 1, 1));
 		}
 		
 		while (true) {
 			// Pick the 3 best neurons
 			Neuron a = neurons.poll();
 			Neuron b = neurons.poll();
 			Neuron c = neurons.poll();
 			// Print the very best
 			System.out.println(a);
 			// Clear the priority queue
 			neurons.clear();
			// Readd the 3 best to the queue
 			neurons.add(a);
 			neurons.add(b);
 			neurons.add(c);
 			// Add 30 new neurons based on the previous 3 best
 			for (int i = 0; i < 10; ++i) {
 				neurons.add(new Neuron(a));
 				neurons.add(new Neuron(b));
 				neurons.add(new Neuron(c));
 			}
 		}
 	}
 	
 	private class Neuron implements Comparable<Neuron> {
 		// The three constants
 		double a;
 		double b;
 		double c;
		// A equations having better constants will have a lower sum
 		Double sum;
 		public Neuron(double a, double b, double c) {
 			this.a = a;
 			this.b = b;
 			this.c = c;
 			sum = diff(a, b, c);
 		}
 		public Neuron(Neuron n) {
 			// Create a new neuron based on a previous one with some mutations
 			a = randomNumber(n.a);
 			b = randomNumber(n.b);
 			c = randomNumber(n.c);
 			sum = diff(a, b, c);
 		}
 		private double randomNumber(double d) {
 			double r = random.nextDouble();
 			if (r < 0.1) {
 				d *= 1.01;
 			} else if (r < 0.2) {
 				d *= 0.99;
 			} else if (r < 0.5) {
 				d = 1.4 * d - random.nextDouble() * 0.8 * d;
 			} else if (r < 0.9) {
 				d = 2 * d - random.nextDouble() * 4 * d;
 			}
 			return d;
 		}
 		@Override
 		public int compareTo(Neuron n) {
 			return sum.compareTo(n.sum);
 		}
 		@Override
 		public String toString() {
 			// This will need to be updated depending on the equation used in
 			// method diff.
 			return a + "*ln(" + b + "x+" + c + ")";
 		}
 	}
 }
