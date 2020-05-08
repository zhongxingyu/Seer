 // 10 Aug 2012
 
package Image;
 
import java.awt.Color;
import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.OutputStream;
 
 public class Comparator {
	private BufferedImage image1;
	private BufferedImage image2;
 
 	private static final int COLOR_DIFFERENCE_LOW = 12;
 	private static final int COLOR_DIFFERENCE_MEDIUM = 20;
 	private static final int COLOR_DIFFERENCE_HIGH = 40;
 	
 	private int width;
 	private int height;
 
 	/** Denotes the maximum amount of possible displacement between two images in pixels. */
 	private int shakeLevel = 0;
 	/** Denotes the maximum amount of accepted error rate. */
 	private double tolerance = 0.05;
 
	/** Prepares the Comparator with two {@link BufferedImage} objects */
	public void setImages(BufferedImage image1, BufferedImage image2) {
 		this.image1 = image1;
 		this.image2 = image2;
 
 		width = image1.getWidth();
 		height = image1.getHeight();
 
 		if(width != image2.getWidth() && height != image2.getHeight())
 			throw new IllegalArgumentException("Dimensions are not the same");
 	}
 
 	/** Determines if the images (that already has been set) are same by comparing at various level
 	 * by calling {@link isLevelSame(int xOffset, int yOffset)}.
 	 * In how many levels this comparison would be carried out is determined by the value of {@code shakeLevel}
 	 * For image comparison this method should be used.
 	 * 
 	 * @return {@code True} if any of the level of image2 matches with image1
 	 */
 	public boolean isSame() {
 
 		for(int xOffset = shakeLevel; xOffset >= -shakeLevel; xOffset--)
 			for(int yOffset = shakeLevel; yOffset >= -shakeLevel; yOffset--)
 				if(isLevelSame(xOffset, yOffset))
 					return true;
 
 		return false;
 	}
 
 	/**
 	 *  Determines if the images (that already has been set) are same
 	 *  @param xOffset How many horizontal pixels image2 has been displaced from image1
 	 *  @param yOffset How many vertical pixels image2 has been displaced from image1
 	 *  
 	 *  @return {@code True} if the proportion of total number of difference points in two images
 	 *  and the area of the image in pixels is less then the {@code tolerance}. Difference is calculated
 	 *  from the RGB values of the images in an approximate manner.  
 	 */
 	public boolean isLevelSame(int xOffset, int yOffset) {
 		double diff = 0;
 
 		for (int row=0; row<height; row++){
 			for (int col=0; col<width; col++){
 				try {
					int rgb1 = image1.getRGB(col, row);
					int rgb2= image2.getRGB(col + xOffset, row + yOffset);
					Color color1 = new Color(rgb1);
					Color color2 = new Color(rgb2);

					int redDiff = Math.abs(color1.getRed() - color2.getRed());
					int greenDiff = Math.abs(color1.getGreen() - color2.getGreen());
					int blueDiff = Math.abs(color1.getBlue() - color2.getBlue());
 					int totalDiff = redDiff + greenDiff + blueDiff;
 
 					if(redDiff > COLOR_DIFFERENCE_HIGH || greenDiff > COLOR_DIFFERENCE_HIGH 
 							|| blueDiff > COLOR_DIFFERENCE_HIGH)
 							diff += 1.0;
 					else if((redDiff > COLOR_DIFFERENCE_MEDIUM || greenDiff > COLOR_DIFFERENCE_MEDIUM 
 							|| blueDiff > COLOR_DIFFERENCE_MEDIUM) && totalDiff > COLOR_DIFFERENCE_MEDIUM * 2)
 						diff += 0.5;
 					else if((redDiff > COLOR_DIFFERENCE_LOW || greenDiff > COLOR_DIFFERENCE_LOW 
 								|| blueDiff > COLOR_DIFFERENCE_LOW) && totalDiff > COLOR_DIFFERENCE_LOW * 2.5)
 							diff += 0.25;
 				}
 				catch(ArrayIndexOutOfBoundsException exception) {			
 				}
 			}
 		}
 
 		double area = width * height;
 		System.out.println(diff / area);
 		if(diff / area > tolerance)
 			return false;
 		else return true;
 	}
 
 	public int getShakeLevel() {
 		return shakeLevel;
 	}
 
 	public void setShakeLevel(int shakeLevel) {
 		this.shakeLevel = shakeLevel;
 	}
 
 	public double getTolerance() {
 		return tolerance;
 	}
 
 	public void setTolerance(double tolerance) {
 		this.tolerance = tolerance;
 	}
 }
