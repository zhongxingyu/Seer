 package org.barteks2x.freemine.generator;
 
 import java.util.Random;
 
 public class ValueNoiseGenerator extends NoiseGenerator2d {
 
 	public ValueNoiseGenerator(double grid, double persistance, int octaves, double fq, long seed) {
 		super(grid, persistance, octaves, fq, seed);
 	}
 
 	@Override
 	public double getRawValueAt(double x, double z) {
 		int intX = (int)x;
 		int intZ = (int)z;
 
		double dx = x - intX;
		double dz = z - intZ;
 		int x1 = 1, z1 = 1;
 		if (x < 0) {
			dx *= -1;
 			x1 = -1;
 		}
 		if (z < 0) {
			dz *= -1;
 			z1 = -1;
 		}
 
 		double v1, v2, v3, v4, t1, t2;
 
 		v1 = random(intX, intZ);
 		v2 = random(intX + x1, intZ);
 		v3 = random(intX, intZ + z1);
 		v4 = random(intX + x1, intZ + z1);
		t1 = lerp(v1, v2, dx);
		t2 = lerp(v3, v4, dx);
		return lerp(t1, t2, dz);
 	}
 
 	protected double lerp(double x1, double x2, double a) {
 		return (x1 * (1 - a) + x2 * a);
 	}
 
 	private double random(int x, int z) {
		Random r1 = new Random(x);
		Random r3 = new Random(z);
		return new Random(seed + r1.nextLong() + r3.nextLong()).nextDouble();
 	}
 }
