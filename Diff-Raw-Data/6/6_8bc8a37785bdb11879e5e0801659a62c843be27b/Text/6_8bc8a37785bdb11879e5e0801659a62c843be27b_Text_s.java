 package org.wedabecha.system.draw;
 
 import java.awt.Graphics;
 import javax.swing.JComponent;
 
 import org.wedabecha.ui.MainWindow;
 
 /**
  * @author Matthias Tylkowski (micron at users.berlios.de)
  * Diese Klasse dient zum Zeichnen eines Textes
  */
 
 public class Text extends JComponent{
     private static final long serialVersionUID = 1L;
     private int startX;
     private int startY;
     private String text;
 
 
     public Text(String textP, int startXP, int startYP){
 		this.text = textP;
 		this.startX = startXP;
 		this.startY = startYP;
 		this.setSize(MainWindow.layeredPane.getWidth(), MainWindow.layeredPane.getHeight());
     } // zeichneText()
 
 
    @Override public void paintComponent(@SuppressWarnings("hiding")
    Graphics text){
	    text.drawString(this.text, this.startX, this.startY);
     } // paintComponent(Graphics text)
 
 } // Text
