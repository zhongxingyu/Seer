 package com.dat255_group3.view;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Rectangle;
 import com.dat255_group3.model.Character;
 
 public class CharacterView {
 	
 	private static final float SIZE = 100f;
 	private Rectangle characterRect;
 	private Character character;
 	private ShapeRenderer shape = new ShapeRenderer();
 	
 	public CharacterView (Character character) {
 		this.character = character;
 		characterRect = new Rectangle();
 		characterRect.height = SIZE;
 		characterRect.width =  SIZE;
 	}
 	
 	
 	/*
 	 * A method which draws the character (which is currently just a turquoise rectangle)
 	 * The Characters appearance will change with time. This is only to test.
 	 */
 	public void draw(){
		shape.begin(ShapeType.FilledRectangle);
 		shape.setColor(Color.CYAN);
		shape.filledRect(character.getPosition().x + 5, character.getPosition().y + 5, characterRect.width, characterRect.height);
 		shape.end();
 		
 	}
 	
 	
 	
 	
 }
