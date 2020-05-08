 package com.steamedpears.comp3004.views;
 import com.steamedpears.comp3004.models.Card;
 
 import org.apache.log4j.*;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageObserver;
 
 public class CardView extends JLabel {
     public static final int DEFAULT_WIDTH = 70;
     private Card card;
     private CardSelectionListener selectionListener;
     static Logger logger = Logger.getLogger(CardView.class);
     private int width;
 
     public CardView(Card card) {
         this(card, DEFAULT_WIDTH);
     }
 
     public CardView(Card card, int width) {
         this.width = width;
         addMouseListener(new CardMouseListener());
         setCard(card);
         update();
     }
 
     /**
      * Set the card associated with this view, and update.
      * @param card The card to be associated with this view.
      */
     public void setCard(Card card) {
         this.card = card;
         update();
     }
 
     /**
      * Get the card associated with this view.
      * @return The card associated with this view.
      */
     public Card getCard() {
         return this.card;
     }
 
     /**
      * Set the width of the displayed card.
      * @param width The desired width.
      */
     public void setCardWidth(int width) {
         this.width = width;
         setIcon(getIconOfSize(card, width));
     }
 
     /**
      * Set the selection listener to be associated with selecting a card in this view.
      * @param selectionListener The selection listener to be associated with selecting a card.
      */
     public void setSelectionListener(CardSelectionListener selectionListener) {
         this.selectionListener = selectionListener;
     }
 
     public void fireSelectionListener() {
         selectionListener.handleSelection(card);
     }
 
     /**
      * Update the view.
      */
     public void update() {
         setCardWidth(width);
     }
 
     private static Icon getIconOfSize(Card card, int width) {
         ImageIcon icon = new ImageIcon(card.getImagePath());
         BufferedImage bufferedImage = rotate(icon,-Math.PI/2);
        Image image = bufferedImage.getScaledInstance(width, -1, Image.SCALE_SMOOTH);
         return new ImageIcon(image);
     }
 
     private class CardMouseListener extends MouseAdapter {
         public void mouseClicked(MouseEvent e) {
             if(selectionListener != null) {
                 fireSelectionListener();
             }
         }
     }
 
     private static BufferedImage rotate(ImageIcon imageIcon, double angle) {
         BufferedImage image = bufferImage(imageIcon);
         double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
         int w = image.getWidth(), h = image.getHeight();
         int neww = (int)Math.floor(w*cos+h*sin), newh = (int)Math.floor(h*cos+w*sin);
         GraphicsConfiguration gc = getDefaultConfiguration();
         BufferedImage result = gc.createCompatibleImage(neww, newh, Transparency.TRANSLUCENT);
         Graphics2D g = result.createGraphics();
         g.translate((neww-w)/2, (newh-h)/2);
         g.rotate(angle, w/2, h/2);
         g.drawRenderedImage(image, null);
         g.dispose();
         return result;
     }
 
     private static GraphicsConfiguration getDefaultConfiguration() {
         GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
         GraphicsDevice gd = ge.getDefaultScreenDevice();
         return gd.getDefaultConfiguration();
     }
 
     private static BufferedImage bufferImage(ImageIcon imageIcon) {
         BufferedImage bufferedImage = new BufferedImage(
                 imageIcon.getIconWidth(),
                 imageIcon.getIconHeight(),
                 BufferedImage.TYPE_INT_RGB);
         bufferedImage.getGraphics().drawImage(imageIcon.getImage(), 0, 0, null);
         return bufferedImage;
     }
 }
