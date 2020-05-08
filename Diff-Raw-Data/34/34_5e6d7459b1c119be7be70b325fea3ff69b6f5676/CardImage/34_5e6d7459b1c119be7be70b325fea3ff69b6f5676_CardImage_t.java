 package org.jdominion.gui;
 
import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
import java.awt.Graphics2D;
 import java.awt.color.ColorSpace;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorConvertOp;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 import javax.swing.JToolTip;
 
 import org.jdominion.Card;
 
 public class CardImage extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	private static final double ERROR_MARGIN = 0.01;
 
 	private static Map<Class<? extends Card>, BufferedImage> cardImages = null;
 	private static Map<Class<? extends Card>, BufferedImage> greyCardImages = null;
 
 	private BufferedImage image;
 	private Card card;
 	private Boolean greyedOut = false;
 	private String overlayText = null;
 	private String longOverlayText = null;
 	private ImageToolTip tooltip = null;
 
 	public Card getCard() {
 		return card;
 	}
 
 	public void setGreyedOut(Boolean greyedOut) {
 		this.greyedOut = greyedOut;
 	}
 
 	public Boolean getGreyedOut() {
 		return greyedOut;
 	}
 
 	public void setOverlayText(String overlayText) {
 		this.overlayText = overlayText;
 		this.repaint();
 	}
 
 	public String getOverlayText() {
 		return overlayText;
 	}
 
 	public String getLongOverlayText() {
 		return longOverlayText;
 	}
 
 	public void setLongOverlayText(String longOverlayText) {
 		this.longOverlayText = longOverlayText;
 		this.repaint();
 	}
 
 	public CardImage(Card card) {
 		this.card = card;
 		this.image = getImage(card);
 		this.setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
 	}
 
 	public void setWidth(int width) {
 		this.setSize(width, (int) (width / getImageAspectRatio()));
 		this.setPreferredSize(new Dimension(width, (int) (width / getImageAspectRatio())));
 	}
 
 	public void setHeight(int height) {
 		this.setSize((int) (height * getImageAspectRatio()), height);
 		this.setPreferredSize(new Dimension((int) (height * getImageAspectRatio()), height));
 	}
 
 	private double getImageAspectRatio() {
 		return ((double) image.getWidth()) / ((double) image.getHeight());
 	}
 
 	private double getCurrentAspectRatio() {
 		return ((double) this.getWidth()) / ((double) this.getHeight());
 	}
 
 	private void adjustSizeToKeepAspectRatio() {
 		if (getCurrentAspectRatio() > getImageAspectRatio() + ERROR_MARGIN) {
 			this.setSize((int) (getImageAspectRatio() * getHeight()), getHeight());
 		} else if (getCurrentAspectRatio() + ERROR_MARGIN < getImageAspectRatio()) {
 			this.setSize(getWidth(), (int) (getWidth() / getImageAspectRatio()));
 		}
 	}
 
 	private Map<Class<? extends Card>, BufferedImage> getCardImages() {
 		if (cardImages == null) {
 			cardImages = new HashMap<Class<? extends Card>, BufferedImage>();
 		}
 		return cardImages;
 	}
 
 	private Map<Class<? extends Card>, BufferedImage> getGreyCardImages() {
 		if (greyCardImages == null) {
 			greyCardImages = new HashMap<Class<? extends Card>, BufferedImage>();
 		}
 		return greyCardImages;
 	}
 
 	private BufferedImage getImage(Card card) {
 		if (!getCardImages().containsKey(card.getClass())) {
 			BufferedImage cardImage = null;
			String filename = "images/cards/" + card.getName().toLowerCase() + ".jpg";
 			try {
				if(new File(filename).exists()) {
					cardImage = ImageIO.read(new File(filename));
				} else {
					//TODO better error reporting
					System.err.println("Error: Could not find image file: " + filename);
					cardImage = createDummyImage(card);
				}
 			} catch (IOException e) {
				//TODO better error reporting
				System.err.println("Error reading " + filename + ": " + e.getMessage());
 				throw new RuntimeException(e);
 			}
 			getCardImages().put(card.getClass(), cardImage);
 		}
 		return getCardImages().get(card.getClass());
 	}
 
	private BufferedImage createDummyImage(Card card) {
		BufferedImage cardImage = new BufferedImage(211, 330, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = cardImage.createGraphics();
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, 211, 330);
		graphics.setColor(Color.YELLOW);
		graphics.setStroke(new BasicStroke(8));
		graphics.drawRect(0, 0, 211, 330);
		graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 36));
		graphics.setColor(Color.RED);
		graphics.drawString(card.getName(), 10, 100);
		return cardImage;
	}

 	private BufferedImage getGreyImage() {
 		if (!getGreyCardImages().containsKey(this.card.getClass())) {
 			ColorConvertOp colorConverter = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
 			getGreyCardImages().put(this.card.getClass(), colorConverter.filter(image, null));
 		}
 		return getGreyCardImages().get(this.card.getClass());
 	}
 
 	@Override
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		adjustSizeToKeepAspectRatio();
 		drawImage(g);
 		drawText(g);
 	}
 
 	private void drawText(Graphics g) {
 		if (this.getOverlayText() != null) {
 			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
 			g.setColor(Color.BLUE);
 			g.drawString(getOverlayText(), getWidth() - 25, getHeight() - 4);
 		}
 		if (this.getLongOverlayText() != null) {
 			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
 			g.setColor(Color.GREEN);
 			g.drawString(getLongOverlayText(), 10, 15);
 		}
 	}
 
 	private void drawImage(Graphics g) {
 		BufferedImage imageToDraw = null;
 		if (greyedOut) {
 			imageToDraw = getGreyImage();
 		} else {
 			imageToDraw = image;
 		}
 		g.drawImage(imageToDraw, 0, 0, this.getWidth(), this.getHeight(), null);
 	}
 
 	@Override
 	public JToolTip createToolTip() {
 		if (tooltip == null) {
 			tooltip = new ImageToolTip(image);
 		}
 		return tooltip;
 	}
 
 }
