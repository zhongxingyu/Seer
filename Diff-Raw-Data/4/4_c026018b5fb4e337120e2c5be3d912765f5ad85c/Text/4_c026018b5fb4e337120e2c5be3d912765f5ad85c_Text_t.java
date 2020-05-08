 package edu.rit.se.sse.rapdevx.gui;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class Text extends DrawableObject {
 
 	private static final String FONT_FILE = "assets/FontPage.png";
 	private static final String FONT_FILE_WHITE = "assets/FontPageWhite.png";
 
 	private static final double DEFAULT_SCALE = 2;
 
 	private BufferedImage largeImage;
 
 	private String text;
 	private double scale;
 
 	public Text(String text, int x, int y) {
 		this(text, x, y, DEFAULT_SCALE);
 	}
 
 	public Text(String text, int x, int y, double textSize) {
 		super(x, y);
 
 		this.text = text.toUpperCase();
 		this.scale = textSize;
 
 		// TODO make font image static and load only once for all text
 		try {
 			largeImage = ImageIO.read(new File(FONT_FILE_WHITE));
 		} catch (IOException e) {
 			System.err.println("Unable to load font file");
 		}
 	}
 
 	public void draw(Graphics2D gPen) {
 		char[] characters = text.toCharArray();
 		int tempX = this.x;
 
 		for (char character : characters) {
 
 			int xIndex;
 			BufferedImage smallImage = null;
 
 			if (character >= 65 && character <= 90) {
 
 				xIndex = (int) character - 65;
 				xIndex *= 6;
 
 				smallImage = largeImage.getSubimage(xIndex, 0, 7, 7);
 
 			} else if (character >= 97 && character <= 122) {
 
 				xIndex = (int) character - 97;
 				xIndex *= 6;
 
 				smallImage = largeImage.getSubimage(xIndex, 12, 7, 7);
 
 			} else if (character >= 33 && character <= 58) {
 
 				xIndex = (int) character - 33;
 				xIndex *= 6;
 
 				smallImage = largeImage.getSubimage(xIndex, 24, 7, 7);
 
 			} else if (character >= 59 && character <= 63) {
 
 				xIndex = (int) character - 59;
 				xIndex *= 6;
 
 				smallImage = largeImage.getSubimage(xIndex, 36, 7, 7);
 
 			} else if (character >= 91 && character <= 96) {
 
 				xIndex = (int) character - 91;
 				xIndex *= 6;
 				xIndex += 30;
 
 				smallImage = largeImage.getSubimage(xIndex, 36, 7, 7);
 
 			} else if (character == 124) {
 
 				xIndex = 66;
 
 				smallImage = largeImage.getSubimage(xIndex, 36, 7, 7);
 
 			} else if (character == 32) {
 
 				smallImage = largeImage.getSubimage(0, 100, 7, 7);
 
 			} else {
 
 				smallImage = largeImage.getSubimage(0, 48, 7, 7);
 
 			}
 
 			gPen.drawImage(smallImage, tempX, y,
 					(int) (smallImage.getWidth() * this.scale),
 					(int) (smallImage.getHeight() * this.scale), null);
 
 			tempX += 6 * this.scale;
 
 		}
 
 	}
 
 	public void drawColor(Graphics2D gPen, int toReplace) {
 		boolean isSpace = false;
 		int replaceWith = 0x00FFFFFF;
 		char[] toConvertArray = text.toCharArray();
 		int tempX = this.x;
 		ImageColorizer colorize = new ImageColorizer(largeImage);
 		colorize.recolorStrong(toReplace, replaceWith);
 		colorize.flush();
 		for (char character : toConvertArray) {
 
 			int xIndex;
 			BufferedImage smallImage = null;
 			// ImageColorizer colorize = null;
 
 			if (character >= 65 && character <= 90) {
 
 				xIndex = (int) character - 65;
 				xIndex *= 6;
 
 				smallImage = colorize.getSubimage(xIndex, 0, 7, 7);
 				// colorize = new ImageColorizer(smallImage);
 				// colorize.recolorStrong(replaceWith, toReplace);
 
 			} else if (character >= 97 && character <= 122) {
 
 				xIndex = (int) character - 97;
 				xIndex *= 6;
 
 				smallImage = colorize.getSubimage(xIndex, 12, 7, 7);
 				// colorize = new ImageColorizer(smallImage);
 				// colorize.recolorStrong(replaceWith, toReplace);
 
 			} else if (character >= 33 && character <= 58) {
 
 				xIndex = (int) character - 33;
 				xIndex *= 6;
 
 				smallImage = colorize.getSubimage(xIndex, 24, 7, 7);
 
 			} else if (character >= 59 && character <= 63) {
 
 				xIndex = (int) character - 59;
 				xIndex *= 6;
 
 				smallImage = colorize.getSubimage(xIndex, 36, 7, 7);
 				// colorize = new ImageColorizer(smallImage);
 				// colorize.recolorStrong(replaceWith, toReplace);
 
 			} else if (character >= 91 && character <= 96) {
 
 				xIndex = (int) character - 91;
 				xIndex *= 6;
 				xIndex += 30;
 
 				smallImage = colorize.getSubimage(xIndex, 36, 7, 7);
 				// colorize = new ImageColorizer(smallImage);
 				// colorize.recolorStrong(replaceWith, toReplace);
 
 			} else if (character == 124) {
 
 				xIndex = 66;
 
 				smallImage = colorize.getSubimage(xIndex, 36, 7, 7);
 				// colorize = new ImageColorizer(smallImage);
 				// colorize.recolorStrong(replaceWith, toReplace);
 
 			} else if (character == 32) {
 				isSpace = true;
 //				smallImage = colorize.getSubimage(0, 100, 7, 7);
 				// colorize = new ImageColorizer(smallImage);
 				// colorize.recolorStrong(replaceWith, toReplace);
 
 			} else {
 
 				smallImage = colorize.getSubimage(0, 48, 7, 7);
 				// colorize = new ImageColorizer(smallImage);
 				// colorize.recolorStrong(replaceWith, toReplace);
 			}
 			gPen.setColor(new Color(73, 73, 73));
 			if (!isSpace) {
 				gPen.drawImage(
 						smallImage,
 						tempX,
 						y,
 						(int) (smallImage.getWidth() * this.scale),
 						(int) (smallImage.getHeight() * this.scale),
 						null);
 				
 				gPen.fill(new Rectangle(tempX, y, (int) (smallImage
 						.getWidth() * this.scale), (int) scale));
 			} 
 			tempX += 6 * this.scale;
 			isSpace = false;
 
 		}
 
 	}
 
 	public int getSizeOnScreen() {
 		return (int) (text.length() * (7 * scale));
 	}
 
 }
