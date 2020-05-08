 import java.io.File;
 
 import javax.swing.JFrame;
 
 public class HaarFeature {
 
 	static int MIN_PATCH_SIDE = 19; //Smallest block size
 	static int NO_FEATURES = 53130;
 	static int[] FEATURE_TABLE = new int[NO_FEATURES*5];
 	IntegralImage img;
 	int origin_x = 0;
 	int origin_y = 0;
 	double patch_scale;
 	double patch_mean;
 	double patch_std;
 
 	/**
 	 * For testing.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		HaarFeature.init();
 		long startTime = System.currentTimeMillis();
 
 		File file = new File("D:\\Dropbox\\BIK\\pro\\TrainingImages\\FACES\\face00001.bmp");
 		IntegralImage img = new IntegralImage(file);
 		HaarFeature fet = new HaarFeature(img);
 
 		int nFeatures = 42310/100;
 		double[] f   = new double[nFeatures];
 		int[] ind = new int[nFeatures];
 
 		for(int i=0;i<ind.length;i++) {
 			ind[i] = i;
 		}
 
 		fet.getFeatures(ind, f);
 
 		for(int i=0;i<ind.length;i++) {
 			System.out.println(f[i]*100);
 		}
 
 		//System.out.println(System.currentTimeMillis()-startTime);
 
 	}
 
 	/**
 	 * Creates a HaarFeature object from an
 	 * <code>IntegralImage</code>. The Region
 	 * Of Interest is set to the largest possible
 	 * square patch.
 	 * 
 	 * @param newImg
 	 */
 	HaarFeature(IntegralImage newImg) {
 		img = newImg;
 		setROI(0, 0, Math.min(img.width, img.height));
 	}
 
 	/**
 	 * Sets a new ROI and calculates the mean 
 	 * and std for that region.
 	 * 
 	 * @param x ROI x-origin
 	 * @param y ROI x-origin
 	 * @param w ROI side length
 	 */
 	void setROI(int x, int y, int w) {
 		origin_x = x;
 		origin_y = y;		
 		patch_scale = ((double)w)/((double)MIN_PATCH_SIDE);
 
 		// Here we use:
		// std^2 = mean(x^2) + mean(x)^2
 		double mean = findInt(x,y,w,w);
 		mean /= ((double)(w*w));
 
 		double meanSqr = findIntS(x,y,w,w);
 		meanSqr /= ((double)(w*w));
 
 		if (meanSqr<=0) {
 			patch_std = 1;
 		}
 		else {
			patch_std = Math.sqrt(Math.pow(mean,2)+meanSqr);
 		}
 	}
 
 	void getFeatures(int[] ind, double[] f) {
 		for(int i=0;i<ind.length;i++) {
 			f[i] = computeFeature(ind[i]);
 		}
 	}
 
 	/**
 	 * Calculates the value of the feature
 	 * enumerated by <code>ind</code>.
 	 * 
 	 * @param ind feature number
 	 * @return	feature value
 	 */
 	double computeFeature(int ind) {
 		ind *= 5;
 		int type = FEATURE_TABLE[ind];
 		int x = FEATURE_TABLE[ind+1];
 		int y = FEATURE_TABLE[ind+2];
 		int w = FEATURE_TABLE[ind+3];
 		int h = FEATURE_TABLE[ind+4];
 
 		// Scale the feature to fit the current patch.
 		x = (int) Math.round(origin_x + x*patch_scale);
 		y = (int) Math.round(origin_y + y*patch_scale);
 		w = (int) Math.round(w*patch_scale);
 		h = (int) Math.round(h*patch_scale);
 
 		switch (type) {
 		case 1: 
 			return typeI(x, y, w, h);
 		case 2: 
 			return typeII(x, y, w, h);
 		case 3: 
 			return typeIII(x, y, w, h);
 		case 4:
 			return typeIV(x, y, w, h);
 		default:
 			System.out.println("Tried to use feature type: "+ind);
 			return Double.NaN;
 		}
 	}
 
 	/**
 	 * Type I feature:
 	 * 
 	 * 	<w->
 	 *  ---- h
 	 *  ++++ h
 	 *
 	 * @param x
 	 * @param y
 	 * @param w
 	 * @param h
 	 * @return
 	 */
 	private double typeI(int x, int y, int w, int h) {
 
 		double sumU = findInt(x,y,w,h);
 		double sumD = findInt(x,y+h,w,h);
 
 		return (sumD-sumU)/patch_std;
 	}
 
 	/**
 	 * Type II feature:
 	 * 
 	 *  <w-><w->
 	 *  ++++---- ^
 	 *  ++++---- h
 	 *  ++++---- v
 	 *
 	 * @param x
 	 * @param y
 	 * @param w
 	 * @param h
 	 * @return
 	 */
 	private double typeII(int x, int y, int w, int h) {
 
 		double sumL = findInt(x,y,w,h);
 		double sumR = findInt(x+w,y,w,h);
 
 		return (sumL-sumR)/patch_std;
 	}
 
 	/**
 	 * Type III feature:
 	 * 
 	 *	<w-><w-><w->
 	 *  ++++----++++ ^
 	 *  ++++----++++ h
 	 *  ++++----++++ v
 	 * 
 	 * @param x
 	 * @param y
 	 * @param w
 	 * @param h
 	 * @return
 	 */
 	private double typeIII(int x, int y, int w, int h) {
 
 		double sumL = findInt(x,y,w,h);
 		double sumC = findInt(x+w,y,w,h);
 		double sumR = findInt(x+2*w,y,w,h);
 
 		// This is the only feature where we
 		// have to account for the mean, since
 		// there are more (+) than (-).
 		return (sumL-sumC+sumR-patch_mean*w*h)/patch_std;
 	}
 
 	/**
 	 * Type IV feature:
 	 * 
 	 * 	<w-><w->
 	 *  ++++---- ^
 	 *  ++++---- h
 	 *  ++++---- v
 	 *  ----++++ ^
 	 *  ----++++ h
 	 *  ----++++ v
 	 * 
 	 * @param x
 	 * @param y
 	 * @param w
 	 * @param h
 	 * @return
 	 */
 	private double typeIV(int x, int y, int w, int h) {
 
 		double sumLU = findInt(x,y,w,h);
 		double sumRU = findInt(x+w,y,w,h);
 		double sumLD = findInt(x,y+h,w,h);
 		double sumRD = findInt(x+w,y+h,w,h);
 
 		return (-sumLD+sumRD+sumLU-sumRU)/patch_std;
 	}
 
 	private double findInt(int x, int y, int w, int h) {
 		// y  D    C
 		// ^  |----|
 		// |  |    |
 		// |  |----|
 		// |  A    B
 		// | -------> x
 
 		double A;
 		if (x>0 && y>0)
 			A = img.xy(x-1, y-1);
 		else
 			A = 0;
 
 		double B;
 		if (y>0)
 			B = img.xy(x+w-1, y-1);
 		else
 			B = 0;
 
 		double D;
 		if (x>0)
 			D = img.xy(x-1, y+h-1);
 		else
 			D = 0;
 
 		double C = img.xy(x+w-1, y+h-1);
 
 		return A+C-B-D;			
 	}
 
 	/**
 	 * Identical to <code>findInt</code>, except that
 	 * it looks at the squared integral image instead.
 	 * 
 	 * @param x
 	 * @param y
 	 * @param w
 	 * @param h
 	 * @return
 	 */
 	private double findIntS(int x, int y, int w, int h) {		
 		double A;
 		if (x>0 && y>0)
 			A = img.xyS(x-1, y-1);
 		else
 			A = 0;
 
 		double B;
 		if (y>0)
 			B = img.xyS(x+w-1, y-1);
 		else
 			B = 0;
 
 		double D;
 		if (x>0)
 			D = img.xyS(x-1, y+h-1);
 		else
 			D = 0;
 
 		double C = img.xyS(x+w-1, y+h-1);
 
 		return A+C-B-D;			
 	}
 
 	/**
 	 * Creates an enumeration of all possible
 	 * features. Must be called on the <code>HaarFeature</code>-class
 	 * before any feature value calculations can be made.
 	 * 
 	 */
 	static void init() {
 		int i = 0;
 		//Type 1
 		for(int w=1; w<=MIN_PATCH_SIDE; w++) {
 			for(int h=1; h<=MIN_PATCH_SIDE/2; h++) {
 				for(int x=0; x<=MIN_PATCH_SIDE-w; x++) {
 					for(int y=0; y<=MIN_PATCH_SIDE-2*h; y++) {
 						FEATURE_TABLE[i] = 1;
 						FEATURE_TABLE[i+1] = x;
 						FEATURE_TABLE[i+2] = y;
 						FEATURE_TABLE[i+3] = w;
 						FEATURE_TABLE[i+4] = h;
 						i += 5;
 					}
 				}
 			}
 		}
 
 		//Type 2
 		for(int w=1; w<=MIN_PATCH_SIDE/2; w++) {
 			for(int h=1; h<=MIN_PATCH_SIDE; h++) {
 				for(int x=0; x<=MIN_PATCH_SIDE-2*w; x++) {
 					for(int y=0; y<=MIN_PATCH_SIDE-h; y++) {
 						FEATURE_TABLE[i] = 2;
 						FEATURE_TABLE[i+1] = x;
 						FEATURE_TABLE[i+2] = y;
 						FEATURE_TABLE[i+3] = w;
 						FEATURE_TABLE[i+4] = h;
 						i += 5;
 					}
 				}
 			}
 		}
 
 		//Type 3
 		for(int w=1; w<=MIN_PATCH_SIDE/3; w++) {
 			for(int h=1; h<=MIN_PATCH_SIDE; h++) {
 				for(int x=0; x<=MIN_PATCH_SIDE-3*w; x++) {
 					for(int y=0; y<=MIN_PATCH_SIDE-h; y++) {
 						FEATURE_TABLE[i] = 3;
 						FEATURE_TABLE[i+1] = x;
 						FEATURE_TABLE[i+2] = y;
 						FEATURE_TABLE[i+3] = w;
 						FEATURE_TABLE[i+4] = h;
 						i += 5;
 					}
 				}
 			}
 		}
 
 		//Type 4
 		for(int w=1; w<=MIN_PATCH_SIDE/2; w++) {
 			for(int h=1; h<=MIN_PATCH_SIDE/2; h++) {
 				for(int x=0; x<=MIN_PATCH_SIDE-2*w; x++) {
 					for(int y=0; y<=MIN_PATCH_SIDE-2*h; y++) {
 						FEATURE_TABLE[i] = 4;
 						FEATURE_TABLE[i+1] = x;
 						FEATURE_TABLE[i+2] = y;
 						FEATURE_TABLE[i+3] = w;
 						FEATURE_TABLE[i+4] = h;
 						i += 5;
 					}
 				}
 			}
 		}
		System.out.println(i/5);
 	}
 
 	static double[] getFeatureImg(int ind) {
 
 		int width = MIN_PATCH_SIDE;
 		double[] img = new double[width*width];
 
 		ind *= 5;
 		int type = FEATURE_TABLE[ind];
 		int xf = FEATURE_TABLE[ind+1];
 		int yf = FEATURE_TABLE[ind+2];
 		int w = FEATURE_TABLE[ind+3];
 		int h = FEATURE_TABLE[ind+4];
 
 		switch (type) {
 		case 1:
 			for(int y=0;y<MIN_PATCH_SIDE;y++) {
 				for(int x=0;x<MIN_PATCH_SIDE;x++) {
 					if ((x>=xf && x<xf+w) && (y>=yf && y<yf+h))
 						img[x + width*y] = -1;
 					else if ((x>=xf && x<xf+w) && (y>=yf+h && y<yf+2*h))
 						img[x + width*y] = +1;
 				}
 			}
 			break;
 
 		case 2:
 			for(int y=0;y<MIN_PATCH_SIDE;y++) {
 				for(int x=0;x<MIN_PATCH_SIDE;x++) {
 					if ((x>=xf && x<xf+w) && (y>=yf && y<yf+h))
 						img[x + width*y] = +1;
 					else if ((x>=xf+w && x<xf+2*w) && (y>=yf && y<yf+h))
 						img[x + width*y] = -1;
 				}
 			}
 			break;
 
 		case 3:
 			for(int y=0;y<MIN_PATCH_SIDE;y++) {
 				for(int x=0;x<MIN_PATCH_SIDE;x++) {
 					if ((x>=xf && x<xf+w) && (y>=yf && y<yf+h))
 						img[x + width*y] = +1;
 					else if ((x>=xf+w && x<xf+2*w) && (y>=yf && y<yf+h))
 						img[x + width*y] = -1;
 					else if ((x>=xf+2*w && x<xf+3*w) && (y>=yf && y<yf+h))
 						img[x + width*y] = +1;
 				}
 			}
 			break;
 
 		case 4:
 			for(int y=0;y<MIN_PATCH_SIDE;y++) {
 				for(int x=0;x<MIN_PATCH_SIDE;x++) {
 					if ((x>=xf && x<xf+w) && (y>=yf && y<yf+h))
 						img[x + width*y] = +1;
 					else if ((x>=xf+w && x<xf+2*w) && (y>=yf && y<yf+h))
 						img[x + width*y] = -1;
 					else if ((x>=xf && x<xf+w) && (y>=yf+h && y<yf+2*h))
 						img[x + width*y] = -1;
 					else if ((x>=xf+w && x<xf+2*w) && (y>=yf+h && y<yf+2*h))
 						img[x + width*y] = +1;
 				}
 			}
 			break;
 
 		default:
 			break;
 		}
 
 		return img;
 	}
 
 	static JFrame showFeatureImg(int ind) {
 		double[] img = getFeatureImg(ind);
 		return IntegralImage.showImg(img, MIN_PATCH_SIDE, MIN_PATCH_SIDE, 10);
 	}
 
 	static JFrame showClassifierImg(TrainingResult[] tr) {
 
 		double[] imgTemp = getFeatureImg(tr[0].ind);
 		int length = imgTemp.length;
 		double[] img = new double[length];
 		for(int n=0; n<tr.length; n++) {
 			if (tr[n] != null) {
 				imgTemp = getFeatureImg(tr[n].ind);
 
 				for(int i=0; i<length; i++) {
 					img[i] += tr[n].par*imgTemp[i]*tr[n].alpha;
 				}
 			}
 		}
 
 		return IntegralImage.showImg(img, MIN_PATCH_SIDE, MIN_PATCH_SIDE, 10);
 	}
 }
