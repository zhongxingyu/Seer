 /**
 	EducationalMonopoly
 	GameFrame.java
 	27.03.2012
 */
 
 /**
  * 
  */
 package de.dhbw.educationalmonopoly.gameRepresentation.swingUI;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Insets;
 import java.awt.Rectangle;
 import java.awt.geom.AffineTransform;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import de.dhbw.educationalmonopoly.model.Game;
 import de.dhbw.educationalmonopoly.model.GameBoard;
 import de.dhbw.educationalmonopoly.model.Player;
 import de.dhbw.educationalmonopoly.model.Token;
 import de.dhbw.educationalmonopoly.model.field.Field;
 import de.dhbw.educationalmonopoly.model.field.StreetField;
 
 /**
  * @author benjamin
  *
  */
 public class GameBoardPanel extends JPanel {
 
 	// thickness of color stripe in percent of the field height
 	private final float stripeThickness = 0.3f;
 
 	// reference to the game drawn
 	private Game game;
 	
 	// reference to the game board drawn
 	private GameBoard gameBoard;
 	
 	private static final long serialVersionUID = 1L;
 	
 
 	public void paintComponent(Graphics g) {
 	      super.paintComponent(g);
 
 	      Graphics2D g2d = (Graphics2D) g;
 
 	      g2d.setColor(Color.blue);
 
 	      Dimension size = getSize();
 	      Insets insets = getInsets();
 
 	      int w =  size.width - insets.left - insets.right;
 	      int h =  size.height - insets.top - insets.bottom;
 	      
 	      int fields = this.gameBoard.getFields().size();
 	      
 	      if ((fields % 4) != 0) {
 		    	try {
 					throw new Exception("Invalid Amount of Fields: amount of fields has to be dividable by four.");
 				} catch (Exception e) {
 					e.printStackTrace();
 				} 
 	      }
 	      
 	      int rowLength = fields / 4;
 
 	      // setup initial positions
 	      int currentX = w-300;
 	      int currentY = h-90;
 	      int fieldWidth = 60;
 	      int fieldHeight = 80;
 	      
 	      this.drawGameField(g2d, rowLength, currentX, currentY, fieldWidth,
 				fieldHeight);
 	      
 	      this.drawTokens(g2d);
 	  }
 
 	private void drawTokens(Graphics2D g2d) {
 		// stores a list of all tokens placed on a field
 		HashMap<Field, List<Token> > fieldTokenMap = new HashMap<Field, List<Token> >();
 		
 		// in the first step sort all tokens by fields
 		for (Player p: this.game.getPlayers()) {
 			Token token = p.getToken();
 			int fieldIndex = token.getFieldIndex();
 			Field field = this.gameBoard.getFields().get(fieldIndex);
 			List<Token> tokenList = fieldTokenMap.get(field);
 					
 			if (tokenList != null) {
 				tokenList.add(token);
 			} else {
 				List<Token> newTokenList = new ArrayList<Token>();
 				newTokenList.add(token);
 				fieldTokenMap.put(field,newTokenList);
 			}
 		}
 		
 		for (Field currentField : fieldTokenMap.keySet()) {
 			List<Token> tokenList = fieldTokenMap.get(currentField);
 			int amountOfTokensOnField = tokenList.size();
 			
 			if (amountOfTokensOnField == 1) {
 				// we only have on token, draw it centered
 				Rectangle drawRect = currentField.getDrawingRectangle();
 		     	AffineTransform oldTransform = g2d.getTransform();
 		     	g2d.setTransform(currentField.getTransform());
 		     		
 				g2d.drawOval((int) (drawRect.x+(0.5*drawRect.width)), (int) (drawRect.y+(0.5*drawRect.height)), 10, 10);
 				
 				g2d.setTransform(oldTransform);
 			} else {
 				int i = 0;
 				for (Token token: tokenList) {
 	
 					Rectangle newDrawRect = new Rectangle();
 					
 					int right 	= currentField.getDrawingRectangle().x + currentField.getDrawingRectangle().width;
 					int left 	= currentField.getDrawingRectangle().x;
 					int bottom 	= currentField.getDrawingRectangle().y + currentField.getDrawingRectangle().height;
 					int top 	= currentField.getDrawingRectangle().y; 
 					
 			     	 switch (i) {
 					 	case 0:  newDrawRect.x = right;
 					 			 newDrawRect.y = top;
 					 	 		 break;
 			            case 1:  newDrawRect.x = right;
 			 			 		 newDrawRect.y = bottom;
 			                     break;
 			            case 2:  newDrawRect.x = left;
 			 			 	     newDrawRect.y = bottom;
 			                     break;
 			            case 3:  newDrawRect.x = left;
 			 			 		 newDrawRect.y = top;
 			 			 		 break;
 					 }
 			     	 
 			     	AffineTransform oldTransform = g2d.getTransform();
 			     	
 			     	g2d.setTransform(currentField.getTransform());
 					 
 			     	//TODO: let token draw itself
 					g2d.drawOval(newDrawRect.x, newDrawRect.y , 10, 10);
 					
 					g2d.setTransform(oldTransform);
 
 					i++;
 				}
 			}	
 		}
 
 	}
 	
 	private void drawGameField(Graphics2D g2d, int rowLength, int currentX,
 			int currentY, int fieldWidth, int fieldHeight) {
 		
 		  // initialize
 		  int fieldIndex = 0;
 		
 		  // draw first row starting at the bottom right
 		  
 		  for (int j = 0; j < 4; j++) {
 	    	  Rectangle rect = new Rectangle(currentX, currentY, fieldHeight, fieldHeight);
 	    	  Field currentField = this.gameBoard.getFields().get(fieldIndex);
 	    	  currentField.setDrawOrientation(Field.DrawOrientation.NORTH);
 	    	  currentField.setTransform(g2d.getTransform());
 	    	  
 	    	  this.drawField(currentField, g2d, rect);
 	    	  currentX -= fieldWidth;
 	          fieldIndex++;
 			  
 		      for (int i=1; i<rowLength; i++) {
 		    	  rect = new Rectangle(currentX, currentY, fieldWidth, fieldHeight);
 		    	  currentField = this.gameBoard.getFields().get(fieldIndex);
 		    	  currentField.setDrawOrientation(Field.DrawOrientation.NORTH);
 		    	  currentField.setTransform(g2d.getTransform());
 		    	  this.drawField(currentField, g2d, rect);
 		    	  currentX -= fieldWidth;
 		          fieldIndex++;
 		      } 
 		      
 		      // increase distance for corner field
 		      currentX = 0;
 		      currentY = 0;
 		      
 		      AffineTransform at = new AffineTransform();
 		      at.setToRotation(Math.PI/2.0);
 		      g2d.translate(currentField.getDrawingRectangle().x, currentField.getDrawingRectangle().y);
 		      g2d.transform(at);
 		  }
 	}
 	
 	private void drawField(final Field field, final Graphics2D g2d, Rectangle rect) {
 		// draw border
 		g2d.setColor(new Color(0,0,0));
 		field.setDrawingRectangle(rect);
 		g2d.drawRect(rect.x, rect.y, rect.width, rect.height); 
 		
 		// draw content
 		if (field instanceof StreetField) {
 			this.drawStreetField(field, g2d, rect);
 		} else {
 			this.drawStationField(field, g2d, rect);
 		}
 	}
 
 	private void drawStationField(final Field field, final Graphics2D g2d,
 			Rectangle rect) {
 		
 		 // Get the current transform
 		 AffineTransform saveAT = g2d.getTransform();
 		 // Perform transformation);
 		 g2d.setTransform(field.getTransform());
 		 // Render
 		 Font font = new Font("Arial", Font.PLAIN, 12);   
 		 g2d.setFont(font);
 		 g2d.drawString(field.getName(), rect.x, rect.y);
 		 // Restore original transform
 		 g2d.setTransform(saveAT);
 	}
 
 	private void drawStreetField(final Field field, final Graphics2D g2d,
 			Rectangle rect) {
 		g2d.setColor( ((StreetField)field).getColor() );
		Rectangle fillRect = (Rectangle) rect.clone();
 		
 		fillRect.height = (int) (this.stripeThickness*rect.height);
 		
 		// set transform to field's local coordinates syste
 		AffineTransform oldTransform = g2d.getTransform();
 		g2d.setTransform(field.getTransform());
 		
 		g2d.fillRect(fillRect.x, fillRect.y, fillRect.width, fillRect.height);
 		
 		g2d.setTransform(oldTransform);
 	}
 
 	public Game getGame() {
 		return game;
 	}
 
 
 	public void setGame(Game game) {
 		this.game = game;
 		
 		// store shortcut reference
 		this.gameBoard = game.getGameBoard();
 	}
 	
 }
