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
 
 	private static final String title = "Uncross The Wires";
 	private static final String start = "Start";
 	public static final Color bgColor = Color.black;
 	public static final Color textColor = Color.white;
 	public static final Color selectionColor = Color.blue;
 	private static final float selectionGrowSpeed = 5f;
 
 	private UnicodeFont font;
 	private float selectionWidth = 0f;
 	private boolean hasPressedKey = false;
 
 	// preventing accidental restart from score menu
 	private boolean hasReleased = false;
 
 	@Override
 	public void enter(GameContainer container, StateBasedGame stateBasedGame) throws SlickException {
 		System.out.println("Entering state " + getID());
 		selectionWidth = 0;
 
 		hasReleased = false;
 	}
 
 	@Override
 	public void leave(GameContainer container, StateBasedGame stateBasedGame) throws SlickException {
 		System.out.println("Leaving state " + getID());
 	}
 
 	@SuppressWarnings("unchecked")
 	// because font.getEffects() is dumb
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		// load font
 		font = new UnicodeFont("cubic.ttf", 40, false, false);
 		font.addAsciiGlyphs();
 		font.getEffects().add(new ColorEffect()); // Create a default white
 		font.loadGlyphs();
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
 
 		// draw background
 		g.setColor(bgColor);
 		g.fillRect(0, 0, gc.getWidth(), gc.getHeight());
 
 		// draw title
 		g.setColor(textColor);
 		g.setFont(font);
 		g.drawString(title, (gc.getWidth() / 2) - (font.getWidth(title) / 2), 20);
 
 		// draw level select
 		g.setColor(textColor);
 		g.drawString("Level: " + Level.difficulties[Level.selectedDifficulty],
 				(gc.getWidth() / 2) - (font.getWidth("Level: " + Level.difficulties[Level.selectedDifficulty]) / 2),
 				(gc.getHeight() / 4) - (font.getHeight("Level: ") / 2) + 20);
 
 		// draw selection rect
 		g.setColor(selectionColor);
 		g.fillRect((gc.getWidth() / 2) - selectionWidth, gc.getHeight() / 2 - ((font.getHeight(start) * 2) / 2), selectionWidth * 2,
 				font.getHeight(start) * 2);
 
 		// draw start
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
 
			if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON) && hasReleased) {
 				sbg.enterState(UncrossTheWires.MOST_SOLVED);
 
 			}
 		} else {
 			selectionWidth = (selectionWidth <= 0) ? 0 : selectionWidth - selectionGrowSpeed * delta;
 		}
 
 		if (!input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
 			hasReleased = true;
 		}
 
 		if (input.isKeyDown(Input.KEY_RIGHT)) {
 			if (!hasPressedKey) {
 				Level.selectedDifficulty = ++Level.selectedDifficulty % Level.difficulties.length;
 				hasPressedKey = true;
 			}
 		} else if (input.isKeyDown(Input.KEY_LEFT)) {
 			if (!hasPressedKey) {
 				Level.selectedDifficulty = (Level.selectedDifficulty > 0) ? Level.selectedDifficulty - 1 : Level.difficulties.length - 1;
 				hasPressedKey = true;
 			}
 		} else {
 			hasPressedKey = false;
 		}
 	}
 
 	@Override
 	public int getID() {
 		return UncrossTheWires.MAIN_MENU;
 	}
 
 }
