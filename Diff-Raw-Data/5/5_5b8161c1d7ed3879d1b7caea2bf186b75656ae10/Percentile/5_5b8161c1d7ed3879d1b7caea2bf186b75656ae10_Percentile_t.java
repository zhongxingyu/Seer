 package de.tum.in.dss.psquare;
 
 import java.util.Arrays;
 
 public class Percentile {
 
 	final int MARKERS = 5;
 
 	// Percentile to find
 	final float p;
 
 	// Marker heights
 	float[] q = new float[MARKERS];
 
 	// Marker positions
 	int[] n = new int[MARKERS];
 
 	// Desired marker positions
 	float[] n_desired = new float[MARKERS];
 
 	// Precalculated desired marker increments
 	float[] dn = new float[MARKERS];
 
 	public Percentile(float p, float[] observations) {
 		// Set percentile
 		this.p = p;
 
 		// Check initial observation size
 		assert (observations.length == MARKERS);
 
 		// Process initial observations
 		for (int i = 0; i < MARKERS; i++) {
 			// Set initial marker heights
 			q[i] = observations[i];
 
 			// Initial marker positions
 			n[i] = i;
 		}
 
 		// Desired marker positions
 		n_desired[0] = 0;
 		n_desired[1] = 2 * p;
 		n_desired[2] = 4 * p;
 		n_desired[3] = 2 + 2 * p;
 		n_desired[4] = 4;
 
 		// Precalculated desired marker increments
 		dn[0] = 0;
 		dn[1] = (float) p / 2f;
 		dn[2] = p;
 		dn[3] = (1f + (float) p) / 2f;
 		dn[4] = 1;
 	}
 
 	public double accept(float x) {
 		int k = -1;
 		if (x < q[0]) {
 			// Update minimum value
 			q[0] = x;
 			k = 0;
 		} else if (q[0] <= x && x < q[1])
 			k = 0;
 		else if (q[1] <= x && x < q[2])
 			k = 1;
 		else if (q[2] <= x && x < q[3])
 			k = 2;
 		else if (q[3] <= x && x <= q[4])
 			k = 3;
 		else if (q[4] < x) {
 			// Update maximum value
 			q[4] = x;
 			k = 3;
 		}
 
 		// Check if k is set properly
 		assert (k >= 0);
 		System.out.println("x=" + x + "; k=" + k);
 
 		// Increment positions of markers k+1 through 4
 		for (k++; k < MARKERS; k++)
 			n[k]++;
 
 		// Update desired marker positions
 		for (int i = 0; i < MARKERS; i++)
 			n_desired[i] += dn[i];
 
 		// Adjust marker heights 2-4 if necessary
 		for (int i = 1; i < MARKERS - 1; i++) {
 			float d = n_desired[i] - n[i];
 
 			if ((d >= 1 && (n[i + 1] - n[i]) > 1)
 					|| (d <= -1 && (n[i - 1] - n[i]) < -1)) {
 				int ds = sign(d);
 
 				// Try adjusting q using P^2 formula
 				float tmp = parabolic(ds, i);
 				if (q[i - 1] < tmp && tmp < q[i + 1])
 					q[i] = tmp;
 				else {
 					q[i] = linear(ds, i);
 				}
 
 				n[i] += ds; // or d? TODO: Check
 			}
 
 		}
 
 		return q[2];
 	}
 
 	float linear(int d, int i) {
 		return q[i] + d * (q[i + d] - q[i]) / (n[i + d] - n[i]);
 	}
 
 	float parabolic(int d, int i) {
 		float a = q[i] + d / (n[i + 1] - n[i - 1]);
		
		float b = (n[i] - n[i - 1] + d) * (q[i + 1] - q[i]) / (n[i + 1] - n[i])
 				+ (n[i + 1] - n[i] - d) * (q[i] - q[i - 1]) / (n[i] - n[i - 1]);
		
 		return a * b;
 	}
 
 	int sign(float d) {
 		if (d > 0)
 			return 1;
 		else
 			return -1;
 	}
 
 	void dump() {
 		System.out.println("q: " + Arrays.toString(q));
 		System.out.println("n: " + Arrays.toString(n));
 		System.out.println("n': " + Arrays.toString(n_desired));
 	}
 
 	public static void main(String arg[]) {
 		// Create a new initial percentile calculation
 		float[] observations = { 3, 4, 5, 6, 2 };
 		Percentile p = new Percentile(0.99f, observations);
 		p.dump();
 
 		// Accept all test values
 		float[] test = { 4, 6, 3, 21, 3, 4, 4, 5, 4, 2, 3, 45, 2, 34, 5, 6, 67 };
 		for (float t : test) {
 			System.out.println("p=" + p.accept(t));
 			p.dump();
 		}
 	}
 
 }
