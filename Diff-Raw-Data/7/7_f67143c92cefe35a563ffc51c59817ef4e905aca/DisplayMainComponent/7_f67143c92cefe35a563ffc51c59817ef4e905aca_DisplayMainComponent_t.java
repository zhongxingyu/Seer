 package de.netprojectev.server.gui;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 
 import org.apache.logging.log4j.Logger;
 
 import de.netprojectev.server.ConstantsServer;
 import de.netprojectev.server.datastructures.Countdown;
 import de.netprojectev.server.datastructures.ImageFile;
 import de.netprojectev.server.model.PreferencesModelServer;
 import de.netprojectev.utils.LoggerBuilder;
 
 /**
  * 
  * GUI Component to draw images and themeslide background images
  * 
  * @author samu
  * 
  */
 public class DisplayMainComponent extends JComponent {
 
 	private static final Logger log = LoggerBuilder.createLogger(DisplayMainComponent.class);
 
 	private static final long serialVersionUID = 3915763660057625809L;
 	private BufferedImage image;
 
 	private boolean countdownShowing = false;
 
 	private Countdown countdown;
 	private Font countdownFont;
 	private Color countdownFontColor;
 	
 	private Color generalBGColor;
 
 	// TODO set a background for this component ( e.g. many 4s logos) that there
 	// isnt any grey space when showing a 4:3 resolution image
 
 	public DisplayMainComponent() {
 		super();
 		updateCountdownFont();
 		updateCountdownFontColor();
 		updateBackgroundColor();
 	}
 
 	/**
 	 * updates the countdown font family, size and color via reading it from the
 	 * properties
 	 */
 	protected void updateCountdownFont() {
 		countdownFont = new Font(PreferencesModelServer.getPropertyByKey(ConstantsServer.PROP_COUNTDOWN_FONTTYPE), Font.PLAIN, Integer.parseInt(PreferencesModelServer.getPropertyByKey(ConstantsServer.PROP_COUNTDOWN_FONTSIZE)));
 		log.debug("countdown font updated to font: " + countdownFont);
 	}
 
 	protected void updateCountdownFontColor() {
 		countdownFontColor = new Color(Integer.parseInt(PreferencesModelServer.getPropertyByKey(ConstantsServer.PROP_COUNTDOWN_FONTCOLOR)));
 		log.debug("updating countdown font color");
 	}
 
 	/**
 	 * Tell this to draw the given image.
 	 * 
 	 * @param file
 	 *            the image file to draw on component
 	 * @throws IOException 
 	 */
 	protected void drawImage(ImageFile image) throws IOException {
 		
 		//TODO optimize this method cause of the many different conversions could be slow
 		final BufferedImage compImage = Misc.imageIconToBufferedImage(new ImageIcon(image.get()));
 		
		log.debug(compImage.getHeight());
		
 		countdownShowing = false;
 
 		//TODO check the scaling and respect aspect ratio
 		int newWidth = getWidth();
		int newHeight = (int) (((double) getWidth() / (double) compImage.getWidth()) * (double) compImage.getHeight());
 		
 		log.debug("new size of the image: " + newWidth + "x" + newHeight);
 		
 		this.image = Misc.getScaledImageInstanceFast(compImage, newWidth, newHeight);
 		repaint(0, 0, getWidth(), getHeight());
 
 	}
 
 	/**
 	 * Setting the countdown object which should be drawn by the component
 	 * 
 	 * @param countdown
 	 *            the countdown object to draw
 	 */
 	protected void drawCountdown(final Countdown countdown) {
 		countdownShowing = true;
 		this.countdown = countdown;
 	}
 
 	protected void countdownFinished() {
 		countdownShowing = false;
 	}
 
 	/**
 	 * drawing the image centered.
 	 */
 	@Override
 	
 	protected void paintComponent(Graphics g) {
 		
 		Color oldColor = g.getColor();
 		
 		g.setColor(generalBGColor);
         g.fillRect(0, 0, getWidth(), getHeight());
 		
         g.setColor(oldColor);
         
 		if (countdownShowing) {
 			Graphics2D tmpG2D = (Graphics2D) g.create();
 			tmpG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 			tmpG2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 			tmpG2D.setFont(countdownFont);
 			tmpG2D.setColor(countdownFontColor);
 
 			tmpG2D.drawString(countdown.getTimeString(), getWidth() / 2 - tmpG2D.getFontMetrics(countdownFont).stringWidth(countdown.getTimeString()) / 2, getHeight() / 2);
 			tmpG2D.dispose();
 
 		} else {
 			if (image != null) {
 				g.drawImage(image, (getWidth() - image.getWidth(null)) / 2, (getHeight() - image.getHeight(null)) / 2, this);
 			}
 		}
 
 	}
 
 	protected void clear() {
 		countdownShowing = false;
 		image = null;
 		repaint();
 	}
 
 	protected void updateBackgroundColor() {
 		generalBGColor = new Color(Integer.parseInt(PreferencesModelServer.getPropertyByKey(ConstantsServer.PROP_GENERAL_BACKGROUND_COLOR)));
 		log.debug("updating general background color");
 		repaint(0, 0, getWidth(), getHeight());
 	}
 
 }
