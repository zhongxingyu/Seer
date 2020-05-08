 package com.punchline.NinjaSpacePirate.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
import com.punchline.javalib.utils.Display;
 
 /**
  * A text button that is part of a menu.
  * @author Natman64
  * @created Oct 17, 2013
  */
 public class MenuButton {
 
 	private static final Color COLOR = Color.WHITE;
 	private static final Color SELECTED_COLOR = Color.YELLOW;
 	
 	private BitmapFont font;
 	private String text;
 	private boolean selected;
 	
 	/**
 	 * Constructs a MenuButton.
 	 * @param font
 	 * @param text
 	 */
 	public MenuButton(BitmapFont font, String text) {
 		this.font = font;
 		this.text = text;
 	}
 	
 	/**
 	 * Draws this button.
 	 * @param batch
 	 * @param position The center of where the text should be drawn.
 	 */
 	public void draw(SpriteBatch batch, Vector2 position) {
 		TextBounds bounds = getBounds();
 		
 		float x = position.x - bounds.width / 2;
		float y = position.y - bounds.height / 2;
 		Rectangle screenBounds = new Rectangle(x, y, bounds.width, bounds.height);
 		
		selected = screenBounds.contains(new Vector2(
				Gdx.input.getX() * (Display.getPreferredWidth() / Display.getRealWidth()), 
				(Display.getRealHeight() - Gdx.input.getY()) * (Display.getPreferredHeight() / Display.getRealHeight())));
 		
 		if (selected) {
 			font.setColor(SELECTED_COLOR);
 		}
 		
		y = position.y + bounds.height / 2;
 		
 		font.draw(batch, text, x, y);
 		
 		font.setColor(COLOR);
 	}
 	
 	/**
 	 * @return The bounds of this button's text.
 	 */
 	public TextBounds getBounds() {
 		return font.getBounds(text);
 	}
 	
 	/**
 	 * @return Whether this button is selected.
 	 */
 	public boolean isSelected() {
 		return selected;
 	}
 	
 }
