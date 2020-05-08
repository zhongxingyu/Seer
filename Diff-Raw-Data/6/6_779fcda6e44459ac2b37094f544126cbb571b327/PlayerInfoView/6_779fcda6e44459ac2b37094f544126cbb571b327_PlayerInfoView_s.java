 package com.github.joakimpersson.tda367.gui;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import com.github.joakimpersson.tda367.gui.guiutils.GUIUtils;
 import com.github.joakimpersson.tda367.model.constants.Attribute;
 import com.github.joakimpersson.tda367.model.player.Player;
 
 /**
  * A view for displaying the PlayerInfoView
  * 
  * @author joakimpersson
  * @modified adderollen
  * 
  */
 public class PlayerInfoView implements IView {
 
 	private Player player;
 	private int startX;
 	private int startY;
 	private int width;
 	private int height;
 	private Font smlFont = null;
 
 	/**
 	 * Creating a PlayerInfoView for a given player.
 	 * 
 	 * @param player
 	 *            The Players info that will be drawn
 	 * @param startX
 	 *            The x-position where to draw the info
 	 * @param startY
 	 *            The y-position where to draw the info
 	 * @param width
 	 *            The width of the info panel
 	 * @param height
 	 *            The height of the info panel
 	 */
 	public PlayerInfoView(Player player, int startX, int startY, int width,
 			int height) {
 		this.player = player;
 		this.startX = startX;
 		this.startY = startY;
 		this.width = width;
 		this.height = height;
 
 		init();
 	}
 
 	/**
 	 * Initiate the view.
 	 */
 	private void init() {
 		try {
 			smlFont = GUIUtils.getSmlFont();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void enter() {
 	}
 
 	@Override
 	public void render(GameContainer container, Graphics g)
 			throws SlickException {
 		// draw black frame
 		g.setColor(Color.black);
 		g.drawRect(startX, startY, width, height);
 		drawPlayerInfo(g);
 		drawScore(g);
 		drawPowerUps(g);
 
 	}
 
 	/**
 	 * Draw the Player part of the info.
 	 * 
 	 * @param g
 	 *            The graphical context to draw to.
 	 */
 	private void drawPlayerInfo(Graphics g) {
 		int xPos = startX;
 		int yPos = startY;
 		int x = 0;
 		int y = 0;
 
 		// draw black frame
 		g.setColor(Color.black);
 		g.drawRect(xPos, yPos, width, height);
 
 		// set up text
 		g.setFont(smlFont);
 		g.setColor(Color.white);
 
 		// draw image of player
		// drawImage(x+7, y+13, "player/"+player.getIndex()+"/still-east", g);
 		GUIUtils.drawImage(xPos + 7, yPos + 13, player.getImage(), g);
 
 		// draw number of round wins
 		y = 12;
 		for (int i = 0; i < player.getRoundsWon(); i++) {
			GUIUtils.drawImage(xPos + 65 + g.getFont().getWidth(player.getName()), yPos + y, "info/chevron", g);
 			y += 4;
 		}
 
 		// draw player name
 		g.drawString(player.getName(), xPos + 60, yPos + 15);
 
 		// draw pretty hearts
 		if (player.isAlive()) {
 			drawHearts(xPos + 59, yPos + 32, g);
 		} else {
 			GUIUtils.drawImage(xPos + 59, yPos + 32, "info/skull", g);
 		}
 
 		// draw number of match wins
 		x = 59;
 		for (int i = 0; i < player.getMatchesWon(); i++) {
 			GUIUtils.drawImage(xPos + x, yPos + 55, "info/star", g);
 			x += 13;
 		}
 	}
 
 	/**
 	 * Draw the score part of the info.
 	 * 
 	 * @param g
 	 *            The graphical context to draw to.
 	 */
 	private void drawScore(Graphics g) {
 		int xPos = startX;
 		int yPos = startY;
 		int areaBombsAvailable = player.getAreaBombsAvailable();
 		// draw score
 		int score = player.getScore();
 		int scoreLength = 11;
 		if (areaBombsAvailable > 0) {
 			scoreLength = 8;
 		}
 		String zeros = leadingZeroes(score, scoreLength);
 		int scoreDisp = g.getFont().getWidth(zeros);
 		g.setColor(Color.darkGray);
 		g.drawString(zeros, xPos + 15, yPos + 70);
 		g.setColor(Color.white);
 		g.drawString("" + score, xPos + scoreDisp + 17, yPos + 70);
 	}
 
 	/**
 	 * Draw the powerup part of the info.
 	 * 
 	 * @param g
 	 *            The graphical context to draw to.
 	 */
 	private void drawPowerUps(Graphics g) {
 		int xPos = startX;
 		int yPos = startY;
 		int x = 145;
 		int textDisp = x + 40;
 		g.setColor(Color.lightGray);
 
 		// --speed--
 		int y = 0;
 		int pSpeed = player.getAttribute(Attribute.Speed);
 		GUIUtils.drawImage(xPos + x, yPos + y, "info/speed", g);
 		drawAttributeValue(pSpeed, xPos + textDisp, yPos + y + 10, g);
 
 		// --range and power--
 		y += 30;
 		int pRange = player.getAttribute(Attribute.BombRange);
 		int pPower = player.getAttribute(Attribute.BombPower);
 		GUIUtils.drawImage(xPos + x, yPos + y, "info/fire", g);
 		drawAttributeValue(pRange, xPos + textDisp, yPos + y + 10, g);
 		if (pPower > 1) {
 			if (pPower < 3) {
 				GUIUtils.drawImage(xPos + x + 16, yPos + y, "info/power2", g);
 			} else {
 				GUIUtils.drawImage(xPos + x + 16, yPos + y, "info/power3", g);
 			}
 		}
 
 		// --bombs--
 		y += 30;
 		int pBombs = player.getAttribute(Attribute.BombStack);
 		GUIUtils.drawImage(xPos + x, yPos + y, "info/bomb", g);
 		drawAttributeValue(pBombs, xPos + textDisp, yPos + y + 10, g);
 
 		// --area bombs, if available--
 		int areaBombsAvailable = player.getAreaBombsAvailable();
 		if (areaBombsAvailable > 0) {
 			GUIUtils.drawImage(xPos + x - 35, yPos + y, "info/bomb-area", g);
 			g.setColor(Color.white);
 			drawAttributeValue(areaBombsAvailable, xPos + x - 25,
 					yPos + y + 10, g);
 		}
 	}
 
 	/**
 	 * Draw an amount of grey zeros at the score row depending on how long the
 	 * score text is.
 	 * 
 	 * @param score
 	 *            The score to check length of.
 	 * @param zeros
 	 *            How many zeros to draw at maximum.
 	 * @return A String containing the zeros and the score.
 	 */
 	private String leadingZeroes(int score, int zeros) {
 		StringBuilder strBuilder = new StringBuilder();
 		for (int i = 0; i < zeros - String.valueOf(score).length(); i++) {
 			strBuilder.append(0);
 		}
 		return strBuilder.toString();
 	}
 
 	/**
 	 * Draw the attribute value at a given position.
 	 * 
 	 * @param value
 	 *            The value of the attribute to draw
 	 * @param x
 	 *            The x-position to draw at
 	 * @param y
 	 *            The y-position to draw at
 	 * @param g
 	 *            The graphical context to draw to.
 	 */
 	private void drawAttributeValue(int value, float x, float y, Graphics g) {
 		if (value >= 10) {
 			g.drawString("" + value, x - 5F, y);
 		} else {
 			g.drawString("" + value, x, y);
 		}
 	}
 
 	/**
 	 * Draw the heart that represent the players health.
 	 * 
 	 * @param x
 	 *            The x-position to draw at
 	 * @param y
 	 *            The y-position to draw at
 	 * @param g
 	 *            The graphical context to draw to.
 	 */
 	private void drawHearts(float x, float y, Graphics g) {
 		int xDelta;
 		int health = player.getHealth();
 		if (health < 4) {
 			xDelta = 27;
 		} else {
 			xDelta = 63 / health;
 		}
 		for (int i = 0; i < health; i++) {
 			GUIUtils.drawImage(x, y, "info/heart", g);
 			x += xDelta;
 		}
 	}
 
 }
