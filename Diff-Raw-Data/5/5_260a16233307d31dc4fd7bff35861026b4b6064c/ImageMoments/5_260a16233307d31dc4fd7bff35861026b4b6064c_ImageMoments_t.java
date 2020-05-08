 package image.csu.fullerton.edu;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 
 public class ImageMoments {
 
 	private BufferedImage sourceImage;
 	private double imageMatrix[][];
 	private boolean momentsCalculated;
 	private double moments[];
 
 	ImageMoments() {
 		sourceImage = null;
 		momentsCalculated = false;
 	}
 
 	ImageMoments(BufferedImage image) {
 		sourceImage = image;
 		momentsCalculated = false;
 	}
 
 	void setImage(BufferedImage image) {
 		sourceImage = image;
 	}
 
 	void generateImageMatrix() {
 		int w = sourceImage.getWidth();
 		int h = sourceImage.getHeight();
 		imageMatrix = new double[h][w];
 
 		for (int x = 0; x < w; x++) {
 			for (int y = 0; y < h; y++) {
 				Color c = new Color(sourceImage.getRGB(x, y));
 				int avgColor = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
				imageMatrix[y][x] = 1.0 - (double) (avgColor) / 255;
 			}
 		}
 	}
 
 	private double getRawMoment(int p, int q) {
 		double m = 0;
 		for (int i = 0, k = imageMatrix.length; i < k; i++) {
 			for (int j = 0, l = imageMatrix[i].length; j < l; j++) {
 				m += Math.pow(i, p) * Math.pow(j, q) * imageMatrix[i][j];
 			}
 		}
 		return m;
 	}
 
 	private double getCentralMoment(int p, int q) {
 		double mc = 0;
 		double m00 = getRawMoment(0, 0);
 		double m10 = getRawMoment(1, 0);
 		double m01 = getRawMoment(0, 1);
 		double x0 = m10 / m00;
 		double y0 = m01 / m00;
 		for (int i = 0, k = imageMatrix.length; i < k; i++) {
 			for (int j = 0, l = imageMatrix[i].length; j < l; j++) {
 				mc += Math.pow((i - x0), p) * Math.pow((j - y0), q)
 						* imageMatrix[i][j];
 			}
 		}
 		return mc;
 	}
 
 	private double getNormalizedCentralMoment(int p, int q, double n00) {
 		double gamma = ((p + q) / 2) + 1;
 		double mpq = getCentralMoment(p, q);
 		double m00gamma = Math.pow(n00, gamma);
 		return mpq / m00gamma;
 	}
 
 	void calculateMoments() {
 		/* The Hu moments are 0..7, the 8th moment is the Flusser/Suk update */
 		moments = new double[8];
 
 		generateImageMatrix();
 
 		double n00 = getCentralMoment(0, 0);
 		double n20 = getNormalizedCentralMoment(2, 0, n00);
 		double n02 = getNormalizedCentralMoment(0, 2, n00);
 		double n30 = getNormalizedCentralMoment(3, 0, n00);
 		double n12 = getNormalizedCentralMoment(1, 2, n00);
 		double n21 = getNormalizedCentralMoment(2, 1, n00);
 		double n03 = getNormalizedCentralMoment(0, 3, n00);
 		double n11 = getNormalizedCentralMoment(1, 1, n00);
 
 		moments[0] = n20 + n02; // Hu1
 		moments[1] = Math.pow((n20 - 02), 2) + Math.pow(2 * n11, 2); // Hu2
 		moments[2] = Math.pow(n30 - (3 * (n12)), 2)
 				+ Math.pow((3 * n21 - n03), 2); // Hu3
		moments[3] = Math.pow((n30 + n12), 2) + Math.pow((n21 + n03), 2); // Hu4
 		moments[4] = (n30 - 3 * n12) * (n30 + n12)
 				* (Math.pow((n30 + n12), 2) - 3 * Math.pow((n21 + n03), 2))
 				+ (3 * n21 - n03) * (n21 + n03)
 				* (3 * Math.pow((n30 + n12), 2) - Math.pow((n21 + n03), 2)); // Hu5
 		moments[5] = (n20 - n02)
 				* (Math.pow((n30 + n12), 2) - Math.pow((n21 + n03), 2)) + 4
 				* n11 * (n30 + n12) * (n21 + n03); // Hu6
 		moments[6] = (3 * n21 - n03) * (n30 + n12)
 				* (Math.pow((n30 + n12), 2) - 3 * Math.pow((n21 + n03), 2))
 				+ (n30 - 3 * n12) * (n21 + n03)
 				* (3 * Math.pow((n30 + n12), 2) - Math.pow((n21 + n03), 2)); // Hu7
 		moments[7] = n11
 				* (Math.pow((n30 + n12), 2) - Math.pow((n03 + n21), 2))
 				- ((n20 - n02) * (n30 + n12) * (n03 + n21)); // Fusser/Suk
 
 		momentsCalculated = true;
 	}
 
 	void calculateMoments(BufferedImage image) {
 		sourceImage = image;
 		calculateMoments();
 	}
 
 	boolean calculated() {
 		return momentsCalculated;
 	}
 
 	double getMoment(int whichMoment) {
 		if (!calculated()) {
 			calculateMoments();
 		}
 		return moments[whichMoment - 1];
 	}
 	
 	double[] getAllMoments() {
 		if (!calculated()) {
 			calculateMoments();
 		}
 		return moments;
 	}
 }
