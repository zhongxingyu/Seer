 package gui;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 /**
  * The canvas class aims for the right hand side image display. And update the image state
  * @author YAN
  *
  */
 public class ImageCanvas extends Canvas {
 	private static final long serialVersionUID = 2491198060037716312L;
     private ImageGridPanel imageGrid;
 
 	private BufferedImage WAI_LOGO;
 	private static Font mainImageViewCanvasFont = new Font("Arial", Font.BOLD, 14);
 
 	/**
 	 * Constructor for the imageCanvas class
 	 * @param imageGrid - ImageGridPanel
 	 */
 	public ImageCanvas(ImageGridPanel imageGrid){
 		this.imageGrid = imageGrid;
 		String logoPath = "lib/wai-default.jpg";
 		try {
 			WAI_LOGO = ImageIO.read(new File(logoPath));
 		} catch (IOException e) {e.printStackTrace();}
 	}
 	
 	@Override
 	public void paint(Graphics g) {
 		Color background;
 		Image currentImage;
 		if(imageGrid.getSelectedImage() == null) {
 			//render the default WAINZ image jm 180912
 			background = Color.WHITE;
 			currentImage = WAI_LOGO;
 		} else {
 			currentImage = imageGrid.getSelectedImage().getImage();
 			background = Color.BLACK;
 		}
 		
 		if (currentImage == null) return; // error
 		
 		int canvasWidth = getWidth();
 		int canvasHeight = getHeight() - 30; // leave room for label at bottom jm 180912
		System.out.println("canvas: " + canvasWidth + ", " + canvasHeight);
 		int imageWidth = currentImage.getWidth(this);
 		int imageHeight = currentImage.getHeight(this);
 		int drawWidth, drawHeight = 0;
 		double aspectRatio = imageWidth / (1.0 * imageHeight);
 		
 		if (imageWidth > canvasWidth || imageHeight > canvasHeight) {
 			// image is bigger than canvas so we need to scale it 
 			if(aspectRatio > 1){
 				// wider than it is tall, scale to fit on canvas
 				drawWidth = canvasWidth;
 				drawHeight = (int)(drawWidth/aspectRatio * 1.0);
				System.out.println("wider: " + drawWidth + ", " + drawHeight);
				//double check the height
				if (drawHeight > canvasHeight) {
					double percentage = (canvasHeight)/(drawHeight * 1.0);
					drawWidth *= percentage;
					drawHeight = canvasHeight;
				}
 			} else { 
 				// taller than it is wide, height should be the same as canvasHeight
 				// and width scaled down appropriately
 				drawHeight = canvasHeight;
 				drawWidth = (int)(aspectRatio*drawHeight);
				System.out.println("taller: " + drawWidth + ", " + drawHeight);
				//double check the width
				if (drawWidth > canvasWidth) {
					double percentage = canvasWidth/(drawWidth * 1.0);
					drawHeight *= percentage;
					drawWidth = canvasWidth;
				}
 			}
 		} else {
 			drawWidth = imageWidth;
 			drawHeight = imageHeight;
 		}
 		
 		// get position required for painting to center the image jm 180912
 		int widthOffset = canvasWidth - drawWidth;
 		int heightOffset = canvasHeight - drawHeight;
 		int xPos = widthOffset / 2;
 		int yPos = heightOffset / 2;
 		
 		// paint canvas background black, paint image jm 180912
 		g.setColor(background);
 		g.fillRect(0, 0, getWidth(), getHeight());
 		g.drawImage(currentImage, xPos, yPos, drawWidth, drawHeight, this);
 		
 		// paint image filename label underneath image jm 180912
 		String filename = imageGrid==null||imageGrid.getSelectedImage()==null?"":imageGrid.getSelectedImage().getFileName();
 		g.setFont(mainImageViewCanvasFont);
 		FontMetrics fm = g.getFontMetrics();
 		int strWidth = fm.stringWidth(filename);
 		int strX = (canvasWidth-strWidth)/2;
 		g.setColor(Color.WHITE);				
 		g.drawString(filename, strX, getHeight() - 10); // use actual canvas height
 	}
 }
