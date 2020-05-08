 package huisken.projection;
 
 import vib.TransformedImage;
 import vib.InterpolatedImage;
 import vib.FastMatrix;
 import vib.NaiveResampler;
 
 import ij.ImagePlus;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.Random;
 
 
 public class FastTransformedImage extends TransformedImage {
 
 	public static void main(String[] args) {
 		ImagePlus templ = ij.WindowManager.getImage("template");
 		ImagePlus model = ij.WindowManager.getImage("model");
 
 		for(int i = 0; i < 10; i++) {
 			FastTransformedImage t = new FastTransformedImage(templ.duplicate(), model.duplicate());
 			//t = t.resample(5);
 			math3d.Point3d cog = t.transform.getCenterOfGravity();
 			FastMatrix fm = FastMatrix.rotateEulerAt(
 				new Random().nextDouble(), new Random().nextDouble(), new Random().nextDouble(),
 				cog.x, cog.y, cog.z);
 			t.setTransformation(fm);
 			double d1 = t.getDistance();
 
 			TransformedImage t2 = new TransformedImage(templ.duplicate(), model.duplicate());
 			t2.measure = new distance.Correlation();
 			//t2 = t2.resample(5);
 			t2.setTransformation(fm);
 			double d2 = t2.getDistance();
 
 			System.out.println("d1 = " + (float)d1 + "  d2 = " + (float)d2);
 		}
 
 		FastTransformedImage t = new FastTransformedImage(templ, model);
 		t.setTransformation(new FastMatrix());
	}
 
 	public static final int TILEX = 64, TILEY = 64, TILEZ = 32;
 	public static final float RATIO = 1.0f;
 
 	private Range[] ranges;
 
 	public FastTransformedImage(ImagePlus orig, ImagePlus transform) {
 		super(orig, transform);
 		findInterestingRanges();
 	}
 
 	private FastTransformedImage(InterpolatedImage orig, InterpolatedImage transform) {
 		super(orig.getImage(), transform.getImage());
 	}
 
 	// get 10% of the ranges with the highest entropy
 	private void findInterestingRanges() {
 
 		int nx = (int)Math.ceil(orig.w / (double)TILEX);
 		int ny = (int)Math.ceil(orig.h / (double)TILEY);
 		int nz = (int)Math.ceil(orig.d / (double)TILEZ);
 
 System.out.println("nx = " + nx);
 System.out.println("ny = " + ny);
 System.out.println("nz = " + nz);
 
 
 		Range[] ranges = new Range[nx * ny * nz];
 		int index = 0;
 
 		for(int z = 0; z < nz; z++) {
 			int z0 = z * TILEZ;
 			int z1 = Math.min(orig.d, z0 + TILEZ);
 			for(int y = 0; y < ny; y++) {
 				int y0 = y * TILEY;
 				int y1 = Math.min(orig.h, y0 + TILEY);
 				for(int x = 0; x < nx; x++) {
 					int x0 = x * TILEX;
 					int x1 = Math.min(orig.w, x0 + TILEX);
 
 					InterpolatedImage.Iterator it = orig.iterator(
 						false, x0, y0, z0, x1, y1, z1);
 					int[] histo = new int[1 << 16];
 					int sum = 0;
 					while(it.next() != null) {
 						histo[orig.getNoCheck(it.i, it.j, it.k)]++;
 						sum++;
 					}
 					float measure = HistogramFeatures.getEntropy(histo, sum);
 
 					ranges[index++] = new Range(x0, y0, z0, x1, y1, z1, measure);
 				}
 			}
 		}
 
 		Arrays.sort(ranges);
 		int n = (int)Math.ceil(RATIO * nx * ny * nz);
 		this.ranges = new Range[n];
 		System.arraycopy(ranges, 0, this.ranges, 0, n);
 	}
 
 	@Override
 	public FastTransformedImage resample(int factor) {
 		TransformedImage t = super.resample(factor);
 		FastTransformedImage res = new FastTransformedImage(t.orig, t.transform);
 		res.measure = t.measure;
 
 		res.ranges = new Range[this.ranges.length];
 		for(int r = 0; r < ranges.length; r++) {
 			res.ranges[r] = new Range(
 				ranges[r].x0 / factor,
 				ranges[r].y0 / factor,
 				ranges[r].z0 / factor,
 				ranges[r].x1 == orig.w ? t.orig.w : ranges[r].x1 / factor,
 				ranges[r].y1 == orig.h ? t.orig.h : ranges[r].y1 / factor,
 				ranges[r].z1 == orig.d ? t.orig.d : ranges[r].z1 / factor,
 				ranges[r].measure);
 		}
 		return res;
 	}
 
 	public FastTransformedImage resample(int fx, int fy, int fz) {
 		ImagePlus resOrig = NaiveResampler.resample(orig.image, fx, fy, fz);
 		ImagePlus resTran = NaiveResampler.resample(transform.image, fx, fy, fz);
 
 		FastTransformedImage res = new FastTransformedImage(
 			new InterpolatedImage(resOrig),
 			new InterpolatedImage(resTran));
		res.transformation = transformation;
 		res.measure = measure;
 
 		res.ranges = new Range[this.ranges.length];
 		for(int r = 0; r < ranges.length; r++) {
 			res.ranges[r] = new Range(
 				ranges[r].x0 / fx,
 				ranges[r].y0 / fy,
 				ranges[r].z0 / fz,
 				ranges[r].x1 == orig.w ? res.orig.w : ranges[r].x1 / fx,
 				ranges[r].y1 == orig.h ? res.orig.h : ranges[r].y1 / fy,
 				ranges[r].z1 == orig.d ? res.orig.d : ranges[r].z1 / fz,
 				ranges[r].measure);
 		}
 		return res;
 	}
 
 	public float getDistance() {
 		final float[][] corr = new float[ranges.length][5];
 		ArrayList<Future<Float>> futures = new ArrayList<Future<Float>>(ranges.length);
 		for(int i = 0; i < ranges.length; i++) {
 			final int idx = i;
 			final Range r = ranges[idx];
 			futures.add(executor.submit(new Callable<Float>() {
 				public Float call() {
 					getDistance(r, corr[idx]);
 					return null;
 				}
 			}));
 		}
 
 		try {
 			for(Future<Float> f : futures) {
 				f.get();
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 
 		float sumX = 0, sumY = 0, sumXX = 0, sumYY = 0, sumXY = 0;
 
 		for(int i = 0; i < ranges.length; i++) {
 			sumX  += corr[i][0];
 			sumY  += corr[i][1];
 			sumXX += corr[i][2];
 			sumYY += corr[i][3];
 			sumXY += corr[i][4];
 		}
 
 		float correlation = 0;
 		long n = orig.w * orig.h * orig.d;
 		double n2 = n * n;
 
 		double numerator = (sumXY / n) - (sumX * sumY) / n2;
 		double varX      = (sumXX / n) - (sumX * sumX) / n2;
 		double varY      = (sumYY / n) - (sumY * sumY) / n2;
 		double denominator = Math.sqrt(varX) * Math.sqrt(varY);
 		if( denominator > 0.00000001 )
 			correlation = (float)(numerator / denominator);
 
 		return 1 - correlation;
 	}
 
 	public void getDistance(Range r, float[] corr) {
 		for(int i = 0; i < 5; i++)
 			corr[i] = 0;
 
 		Iterator iter = new Iterator(false, r.x0, r.y0, r.z0, r.x1, r.y1, r.z1);
 		while (iter.next() != null) {
 			float v1 = orig.getNoCheck(iter.i, iter.j, iter.k);
 			float v2 = (float)transform.interpol.get(iter.x, iter.y, iter.z);
 			corr[0] += v1;
 			corr[1] += v2;
 			corr[2] += v1 * v1;
 			corr[3] += v2 * v2;
 			corr[4] += v1 * v2;
 		}
 	}
 
 	public ImagePlus getTransformed() {
 		System.out.println("getTransformed");
 		InterpolatedImage result = orig.cloneDimensionsOnly();
 
 		Iterator iter = iterator();
 		while (iter.next() != null) {
 			result.set(iter.i, iter.j, iter.k,
 				(int)Math.round(transform.getNoInterpol(
 					(int)Math.round(iter.x),
 					(int)Math.round(iter.y),
 					(int)Math.round(iter.z))));
 		}
 		result.image.setTitle("transformed");
 		return result.getImage();
 	}
 
 	private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
 
 	private static final class Range implements Comparable<Range> {
 		int x0, y0, z0;
 		int x1, y1, z1;
 		float measure;
 
 		Range(int x0, int y0, int z0, int x1, int y1, int z1, float measure) {
 			this.x0 = x0;
 			this.x1 = x1;
 			this.y0 = y0;
 			this.y1 = y1;
 			this.z0 = z0;
 			this.z1 = z1;
 			this.measure = measure;
 		}
 
 		/* reverse sorting */
 		public int compareTo(Range r) {
 			if(this.measure < r.measure) return +1;
 			if(this.measure > r.measure) return -1;
 			return 0;
 		}
 
 		public String toString() {
 			return "(" + x0 + ", " + y0 + ", " + z0 + ") - (" + x1 + ", " + y1 + ", " + z1 + ")";
 		}
 	}
 }
