 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.concurrent.ThreadLocalRandom;
 
 final class GLB {
 	private static final ThreadLocalRandom r = ThreadLocalRandom.current();
 	// Window settings
 	public static final int ssMAX = 10;
 	public static final int ssMIN = 1;
 	public static       int ss = ssMIN;
 	public static final int swid = 1920;
 	public static final int shei = 1080;
 	public static final int bgcolor = 0xFF000000;
 	
 	public static       String uFlameID;
 	
 	public static final double[] image = new double[swid * shei * 4];
 	
 	// Histogram settings
 	public static       int hwid = swid * ss;
 	public static       int hhei = shei * ss;
 	
 	public static       Histogram h;// = createHistogram();
 	
 	// camera settings
 	public static final double cameraXShrinkDEFAULT = 30;
 	public static final double cameraYShrinkDEFAULT = 30;
 	public static       double cameraXShrink        = cameraXShrinkDEFAULT;
 	public static       double cameraYShrink        = cameraYShrinkDEFAULT;
 	public static       double cameraXOffset		= 0.5;
 	public static       double cameraYOffset		= 0.5;
 	
 	// Thread settings
 	public static       int nThreads   = 1;
 	public static final int maxThreads = Runtime.getRuntime().availableProcessors()-4;
 	
 	public static       boolean threadStopSignal = true;
 	
 	public static       FractalThread[] threads;
 	
 	// image settings
	public static final double gamma = 2.21;
 	
 	// affine matrix settings
 	private static final int maxAffineTransformations = 5;
 	private static final int minAffineTransformations = 3;
 	public static        int   nAffineTransformations = r.nextInt(
 													  minAffineTransformations
 													, maxAffineTransformations);
 	public static        ColorSet[] affineColor;
 	
 	// global referance to the affine transformation arrays
 	// the use is a[j] = affineTransformation[][]
 	public static       double[][][] a;
 	
 	// probability of each matrix being chosen
 	public static       int[] aProbability = new int[100];
 	
 	// variation settings
 	public static final int nVariations = 10;
 	public static final double[] vWeight = new double[nVariations];
 	public static       boolean enableVariations = true;
 	
 	public static final void resetAffineTransformations(){
 		uFlameID = "images/" + r.nextInt(100000,999999);
 		
 		for(int i = 0; i<vWeight.length; i++){
 			vWeight[i] = r.nextDouble(.5,1);
 		}
 		
 		nAffineTransformations = 
 			   r.nextInt(
 				  minAffineTransformations
 				, maxAffineTransformations);
 		
 		a = new double[nAffineTransformations][3][3];
 		for(int i = 0; i < a.length; i++){
 			for(int j = 0; j < a[i].length; j++){
 				for(int k = 0; k < a[i][j].length; k++){
 					a[i][j][k] = r.nextDouble(-1, 1);
 				}
 			}
 		}
 		
 		final double[] aProbs = new double[nAffineTransformations];
 		for(int i = 0; i < aProbs.length; i++){
 			aProbs[i] = r.nextDouble();
 		}
 		Arrays.sort(aProbs);
 		int last = 0;
 		for(int i = 0; i<aProbs.length; i++){
 			for(int j = last; j < aProbs[i]*aProbability.length; j++){
 				aProbability[j] = i;
 				last = j;
 			}
 		}
 		for(int i = last; i<100; i++){
 			aProbability[i] = nAffineTransformations-1;
 		}
 		
 		resetAffineColorMap();
 	}
 
 	public static final void resetAffineColorMap() {
 		affineColor = new ColorSet[nAffineTransformations];
 		
 		for(int i = 0; i<affineColor.length; i++){
 			affineColor[i] = new ColorSet(
 					  ThreadLocalRandom.current().nextDouble(1)
 					, ThreadLocalRandom.current().nextDouble(1)
 					, ThreadLocalRandom.current().nextDouble(1));
 					
 		}
 	}
 
 	public static final void newHistogram() {
 		h = null;
 		System.gc();
 		h = new Histogram();
 	}
 	
 	public static final void resetHistogram() {
 		for(int i = 0; i<h.histo.length; i++){
 				h.histo[i] = 0;
 		}
 	}
 
 	private static final void resetCamera() {
 		cameraXOffset = .5;
 		cameraYOffset = .5;
 		cameraXShrink = cameraXShrinkDEFAULT;
 		cameraYShrink = cameraYShrinkDEFAULT;
 	}
 	
 	public static final void startThreads() {
 		System.out.println(GLB.nThreads);
 		threads = new FractalThread[nThreads];
 		for(FractalThread t : threads){
 			t = new FractalThread();
 			t.start();
 		}
 	}
 	
 	public static final void stopThreads() {
 		GLB.threadStopSignal = true;
 		if(GLB.threads != null){
 			for(FractalThread t : GLB.threads){
 				try { t.join(); } catch (Exception exception) {}
 			}
 		}
 		GLB.threadStopSignal = false;
 	}
 	
 	public static final void reset() {
 		stopThreads();
 		resetAffineTransformations();
 		resetHistogram();
 		resetCamera();
 		startThreads();
 	}
 	
 }
