 package com.github.rocketsurgery;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class MainMenu extends BasicGameState {
 
 	String title = "Uncross The Wires";
 	String start = "Start";
 	Color bgColor = Color.black;
 	Color textColor = Color.white;
 	Color selectionColor = Color.blue;
 	UnicodeFont font;
 	float selectionWidth = 0f;
 	float selectionGrowSpeed = 5f;
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		font = new UnicodeFont("res/cubic.ttf", 40, false, false);
 		font.addAsciiGlyphs();
 		font.getEffects().add(new ColorEffect()); // Create a default white
 		font.loadGlyphs();
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
 		// Draw Background
 		g.setColor(bgColor);
 		g.fillRect(0, 0, gc.getWidth(), gc.getHeight());
 
 		// Draw Title
 		g.setColor(textColor);
 		g.setFont(font);
 		g.drawString(title, (gc.getWidth() / 2) - (font.getWidth(title) / 2), 20);
 
 		// Draw Selection Rect
 		g.setColor(selectionColor);
 		g.fillRect((gc.getWidth() / 2) - selectionWidth, gc.getHeight() / 2 - ((font.getHeight(start) * 2) / 2), selectionWidth * 2, font.getHeight(start) * 2);
 		
 		// Draw Start
 		g.setColor(textColor);
 		g.drawString(start, (gc.getWidth() / 2) - (font.getWidth(start) / 2), (gc.getHeight() / 2) - (font.getHeight(start) / 2));
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
 		// Create Input
 		Input input = gc.getInput();
 		int mouseX = input.getMouseX();
 		int mouseY = input.getMouseY();
 
 		boolean onStart = false;
 
 		// If Mouse Is On Start
 		if (mouseX > (gc.getWidth() / 2) - (font.getWidth(start) / 2) && mouseX < (gc.getWidth() / 2) + (font.getWidth(start) / 2)
 				&& mouseY > (gc.getHeight() / 2) - (font.getHeight(start) / 2) && mouseY < (gc.getHeight() / 2) + (font.getHeight(start) / 2)) {
 			onStart = true;
 		}
 
 		if (onStart) {
			selectionWidth = (selectionWidth >= gc.getWidth() / 2) ? gc.getWidth() / 2 : selectionWidth + selectionGrowSpeed * delta;
			
 			if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
 				sbg.enterState(UncrossTheWires.GAMEPLAY);
 			}
 		} else {
			selectionWidth = (selectionWidth <= 0) ? 0 : selectionWidth - selectionGrowSpeed * delta;
 		}
 	}
 
 	@Override
 	public int getID() {
 		return UncrossTheWires.MAIN_MENU;
 	}
 
 }
