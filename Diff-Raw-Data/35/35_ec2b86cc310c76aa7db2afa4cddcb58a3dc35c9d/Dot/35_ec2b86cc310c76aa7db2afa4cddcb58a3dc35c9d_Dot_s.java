 package org.ahands.thirdeye;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.geom.Ellipse2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 
 public class Dot {
 
	// 183 239 164
	// private Color dotColor = new Color(0xbdf8a8);
	// 0x54593b yellow
	// 0x655146 pink 463930
	// highlight 595a3b 646c43 7c8e54
	// private Color[] dotColor = { new Color(0x595a3b), new Color(0x646c43), new Color(0x7c8e54) };
	// private Color[] dotColor = { new Color(0xfc7c55) };
	private Color[] dotColor = { new Color(0xffec5332) }; // found w/ Optomizer
 	private List<Point> foundList = new ArrayList<Point>();
 	private BufferedImage camImg;
 	private BufferedImage dotImg;
 	private final int threshold = 33;
 
 	public Color[] getDotColor() {
 		return dotColor;
 	}
 
 	public void setDotColor(Color[] dotColor) {
 		this.dotColor = dotColor;
 	}
 
 	public BufferedImage getDotImg() {
 		return this.dotImg;
 	}
 
 	public BufferedImage getCamImg() {
 		return camImg;
 
 	}
 
 	public int getThreshold() {
 		return threshold;
 	}
 
 	public void setDotImg(BufferedImage dotImg) {
 		this.dotImg = dotImg;
 	}
 
 	public void setCamImg(BufferedImage camImg) {
 		this.camImg = camImg;
 	}
 
 	public void findDot() {
 		final long start = Calendar.getInstance().getTimeInMillis();
 		if (camImg == null) {
 			return;
 		}
 
 		foundList.clear();
 
 		final int h = camImg.getHeight();
 		final int w = camImg.getWidth();
 
 		this.dotImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
 
 		Color rgb;
 		for (final Color color : dotColor) {
 			final int known_r = color.getRed();
 			final int known_g = color.getGreen();
 			final int known_b = color.getBlue();
 
			for (int y = 0; y < h; y = y + 2) {
				for (int x = 0; x < w; x = x + 2) {
 					rgb = new Color(camImg.getRGB(x, y));
 					if (Math.abs(known_g - rgb.getGreen()) <= threshold) {
 						if (Math.abs(known_r - rgb.getRed()) <= threshold) {
 							if (Math.abs(known_b - rgb.getBlue()) <= threshold) {
 								foundList.add(new Point(x, y));
 								dotImg.setRGB(x, y, 0xff00ff00);
 							}
 						}
 					}
 				}
 			}
 		}
 		System.out.println(Calendar.getInstance().getTimeInMillis() - start);
 	}
 
 	public Rectangle getSimpleContainer() {
 		final Point p = getAverage();
 		return new Rectangle(p.x - 10, p.y - 10, 20, 20);
 	}
 
 	public Ellipse2D getRoundContainer() {
 		final Point p = getAverage();
 		return new Ellipse2D.Float(p.x - 10, p.y - 10, 20, 20);
 	}
 
 	public Rectangle getContainer() {
 		int min_x = 9000;
 		int max_x = -1;
 		int min_y = 9000;
 		int max_y = -1;
 
 		for (Point point : foundList) {
 			if (point.x < min_x) {
 				min_x = point.x;
 			} else if (point.x > max_x) {
 				max_x = point.x;
 			}
 
 			if (point.y < min_y) {
 				min_y = point.y;
 			} else if (point.y > max_y) {
 				max_y = point.y;
 			}
 		}
 
 		return new Rectangle(min_x, min_y, (max_x - min_x), (max_y - min_y));
 	}
 
 	public Point getMedian() {
 		try {
 			final List<Double> xList = new ArrayList<Double>();
 			final List<Double> yList = new ArrayList<Double>();
 
 			for (final Point point : foundList) {
 				xList.add(point.getX());
 				yList.add(point.getY());
 			}
 
 			Collections.sort(xList);
 			Collections.sort(yList);
 			return new Point(((xList.get(xList.size() / 2)).intValue()), ((yList.get(yList.size() / 2)).intValue()));
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public Point getAverage() {
 		try {
 			int x = 0, y = 0;
 			for (final Point point : foundList) {
 				x = x + point.x;
 				y = y + point.y;
 			}
 
 			final int len = foundList.size();
 			return new Point((x / len), (y / len));
 		} catch (ArithmeticException e) {
 			return null;
 		}
 	}
 }
