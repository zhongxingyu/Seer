 import javax.swing.JPanel;
 import java.awt.Graphics;
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.Dimension;
 import java.awt.event.*;
 import java.io.File;
 import javax.imageio.ImageIO;
 
 class ImageArea extends JPanel {
 	Image image;
 	BufferedImage bufImage;
 	Point startSelectionPoint;
 	Point stopSelectionPoint;
 	Point currentPoint;
 	boolean imageIsLoaded;
 	boolean showSelectingArea;
 
 	ImageArea() {
 		setImageStatusAsNotLoaded();
 		resetSelectionArea();
 		setZiroInitialSize();
 		setupMouseMotion();
 	}
 
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		if (imageIsLoaded) {
 			g.drawImage(bufImage, 0, 0, null);
 			if (showSelectingArea) {
 				g.setColor(Color.BLACK);
 				g.drawLine((int)startSelectionPoint.getX(), (int)startSelectionPoint.getY(),
 					(int)currentPoint.getX(), (int)startSelectionPoint.getY());
 				g.drawLine((int)currentPoint.getX(), (int)startSelectionPoint.getY(),
 					(int)currentPoint.getX(), (int)currentPoint.getY());
 				g.drawLine((int)currentPoint.getX(), (int)currentPoint.getY(),
 					(int)startSelectionPoint.getX(), (int)currentPoint.getY());
 				g.drawLine((int)startSelectionPoint.getX(), (int)currentPoint.getY(),
 					(int)startSelectionPoint.getX(), (int)startSelectionPoint.getY());
 			}
 		}
 	}
 
 	public void setImage(Image image) {
 		this.setSize(image.getWidth(null), image.getHeight(null));
 		this.setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
 		this.image = image;
 		imageIsLoaded = true;
 		int x = image.getWidth(null);
 		int y = image.getHeight(null);
 		bufImage = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
 		Graphics g = bufImage.createGraphics();
 		g.drawImage(image, 0, 0, null);
 		g.dispose();
 		this.repaint();
 	}
 
 	public void saveImage(File file) {
 		try {
 			ImageIO.write(bufImage, "bmp", file);
 		} catch (java.io.IOException ioe) {
 			System.out.println("Unable save file");
 		}
 	}
 
 	private void setImageStatusAsNotLoaded() {
 		imageIsLoaded = false;
 	}
 
 	private void setZiroInitialSize() {
 		this.setPreferredSize(new Dimension(0, 0));
 	}
 
 	private void setupMouseMotion() {
 		this.addMouseListener(new MouseListener() {
 			public void mousePressed(MouseEvent event) {
 				startSelect(event.getPoint());
 			}
 
 			public void mouseReleased(MouseEvent event) {
 				stopSelect(event.getPoint());
 			}
 
 			public void mouseClicked(MouseEvent e) {}
 			public void mouseEntered(MouseEvent e) {}
 			public void mouseExited(MouseEvent e) {}
 		});
 
 		this.addMouseMotionListener(new MouseMotionListener() {
 			public void mouseDragged(MouseEvent event) {
 				draggMouse(event.getPoint());
 			}
 
 			public void mouseMoved(MouseEvent event) {}
 		});
 	}
 
 	private void startSelect(Point point) {
 		this.startSelectionPoint = point;
 		this.stopSelectionPoint = point;
 		this.currentPoint = point;
 		showSelectingArea = true;
 		this.repaint();
 	}
 
 	private void stopSelect(Point point) {
 		this.stopSelectionPoint = point;
 		if (this.stopSelectionPoint == this.startSelectionPoint) {
 			resetSelectionArea();
 		}
 		this.repaint();
 	}
 
 	private void draggMouse(Point point) {
 		this.currentPoint = point;
 		this.repaint();
 	}
 
 	private void resetSelectionArea() {
 		showSelectingArea = false;
 	}
 
 	public void getGrayImage() {
 		int red;
 		int green;
 		int blue;
 		int gray;
 
		for (int y = 0; y < image.getHeight(null); y++) {
			for (int x = 0; x < image.getWidth(null); x++) {
 				Color tempColor = new Color(bufImage.getRGB(x, y));
 				red = tempColor.getRed();
 				green = tempColor.getGreen();
 				blue = tempColor.getBlue();
 				gray = (int)((red + green + blue) / 3);
 				bufImage.setRGB(x, y, new Color(gray, gray, gray).getRGB());
 			}
 		}
 		this.repaint();
 	}
 }
