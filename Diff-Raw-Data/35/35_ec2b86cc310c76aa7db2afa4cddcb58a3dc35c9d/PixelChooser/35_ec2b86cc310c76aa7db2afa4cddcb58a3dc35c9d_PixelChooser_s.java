 package org.ahands.thirdeye.testing;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 public class PixelChooser {
 	private static BufferedImage justDotImg;
 	private static BufferedImage noDotImg;
 	private static BufferedImage testImg;
 
 	private static int threshold = 0;
 
 	public static void main(String[] args) throws InterruptedException {
		final String foldername = "2";
 		try {
 			justDotImg = ImageIO.read(new File("resources/" + foldername + "/justdot.png"));
 			noDotImg = ImageIO.read(new File("resources/" + foldername + "/nodot.png"));
 			testImg = ImageIO.read(new File("resources/" + foldername + "/test.png"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		final List<Color> dotColorList = getUniqColors(justDotImg);
 		System.out.printf("Found %d unique colors in justdot.png (of %d total pixels)\n", dotColorList.size(),
 				justDotImg.getWidth() * justDotImg.getHeight());
 
 		final List<Color> noDotColorList = getUniqColors(noDotImg);
 		System.out.printf("Found %d unique colors in nodot.png (of %d total pixels)\n", noDotColorList.size(), noDotImg
 				.getWidth()
 				* noDotImg.getHeight());
 
 		List<Color> saveList = null;
 		while (true) {
 			int foundCount = 0;
 			final List<Color> tmpSaveList = new ArrayList<Color>();
 
 			for (final Color dotColor : dotColorList) {
 				int found = scoreColor(dotColor, noDotColorList);
 				if (found == 0) {
 					tmpSaveList.add(dotColor);
 					foundCount++;
 				}
 			}
 
 			System.out.printf("Found %d at threshold %d\n", foundCount, threshold);
 			if (tmpSaveList.size() > 0) {
 				saveList = tmpSaveList;
 			}
			if (foundCount <= 1) {
 				threshold--;
 				break;
 			}
 		}
 
 		System.out.printf("The most unique colors were:\n");
 		int i = 0;
 		for (Color color : saveList) {
 			i++;
			System.out.printf("0x%h (%d)\n", color.getRGB(), findColors(justDotImg, color));
 			writeColorImage(color, testImg, "/tmp/imgs/" + i + ".png");
 		}
 
 	}
 
 	public static void writeColorImage(final Color color, final BufferedImage img, final String filename) {
 		final int known_r = color.getRed();
 		final int known_g = color.getGreen();
 		final int known_b = color.getBlue();
 		Color rgb;
 
 		for (int y = 0; y < img.getHeight(); y++) {
 			for (int x = 0; x < img.getWidth(); x++) {
 				rgb = new Color(img.getRGB(x, y));
 				if (Math.abs(known_g - rgb.getGreen()) <= threshold) {
 					if (Math.abs(known_r - rgb.getRed()) <= threshold) {
 						if (Math.abs(known_b - rgb.getBlue()) <= threshold) {
 							img.setRGB(x, y, 0xff00ff00);
 						}
 					}
 				}
 			}
 		}
 
 		try {
 			ImageIO.write(img, "png", new File(filename));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static int scoreColor(final Color searchColor, final List<Color> colorList) {
 		int count = 0;
 		int known_r = searchColor.getRed();
 		int known_g = searchColor.getGreen();
 		int known_b = searchColor.getBlue();
 		for (Color color : colorList) {
 			if (Math.abs(known_g - color.getGreen()) <= threshold) {
 				if (Math.abs(known_r - color.getRed()) <= threshold) {
 					if (Math.abs(known_b - color.getBlue()) <= threshold) {
 						count++;
 					}
 				}
 			}
 
 		}
 		return count;
 	}
 
 	public static int findColors(final BufferedImage img, final Color color) {
 		int h = img.getHeight();
 		int w = img.getWidth();
 		int count = 0;
 		int known_r = color.getRed();
 		int known_g = color.getGreen();
 		int known_b = color.getBlue();
 
 		for (int y = 0; y < h; y = y + 1) {
 			for (int x = 0; x < w; x = x + 1) {
 				Color rgb = new Color(img.getRGB(x, y));
 				if (Math.abs(known_g - rgb.getGreen()) <= threshold) {
 					if (Math.abs(known_r - rgb.getRed()) <= threshold) {
 						if (Math.abs(known_b - rgb.getBlue()) <= threshold) {
 							count++;
 						}
 					}
 				}
 			}
 		}
 		return count;
 	}
 
 	public static List<Color> getUniqColors(final BufferedImage img) {
 		final int h = img.getHeight();
 		final int w = img.getWidth();
 		final List<Color> colorList = new ArrayList<Color>();
 		for (int y = 0; y < h; y = y + 1) {
 			for (int x = 0; x < w; x = x + 1) {
 				final Color tmpColor = new Color(img.getRGB(x, y));
 				if (!colorList.contains(tmpColor)) {
 					colorList.add(tmpColor);
 				}
 			}
 		}
 		return colorList;
 	}
 }
