 /**
  * @file Game.java
  * @author Jia Chen
  * @date Sept 06, 2011
  * @description 
  * 		Game.java is used to create and start a match between players
  * 		on the Internet. It automate some phases while leaving other 
  * 		phases with players to decide what to do.
  */
 
 /**
  * options on discarding/peek/swap/etc. between zones
  *      Need to find a way to get specific cards from waiting room/memory/deck/clock/level
  * manual refresh and refresh damage (an option to refresh)
  * shuffle deck after search
  * pay for stock from bottom/discard stock from top
  * 
  * 
  */
 
 package Game;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 
 import CardAssociation.Card;
 import CardAssociation.State;
 import CardAssociation.Type;
 import Field.NewMainField;
 
 public class Game extends JPanel implements MouseListener, MouseMotionListener {
 
 	/**
 	 * static variables
 	 */
 	private static final long serialVersionUID = -4141688861533493929L;
 	public static double gameScale = 0.6;
 	public static int translatedY = 0;
 	public static int maxWidth = 1200;
 	public static int maxHeight = 1000;
 	// public static Connector connect;
 
 	public Phase currPhase;
 	public NewMainField defendingField;
 	public NewMainField attackingField;
 	private int gameStatus = 0;
 	public Player player1;
 	public Player player2;
 	// public Player currentPlayer;
 	private int offsetX = 150;
 	private int offsetY = 22;
 	private boolean hasClocked = false;
 	private int gameID;
 
 	private boolean standAllCards = true;
 
 	// private ArrayList<BufferedImage> phaseImages;
 
 	// create a new game
 	public Game(Player p1, Player p2) {
 		setBackground(Color.white);
 		addMouseMotionListener(this);
 		addMouseListener(this);
 		player1 = p1;
 		player2 = p2;
 		// maxWidth = p1.getField().getPreferredSize().width;
 		// currentPlayer = p1;
 		// connect = new Connector("WeissSchwarz", 5000);
 		this.setPreferredSize(new Dimension(maxWidth, maxHeight));
 		this.setSize(new Dimension(maxWidth, maxHeight));
 	}
 
 	public Game(Player p1) {
 		setBackground(Color.white);
 		addMouseMotionListener(this);
 		addMouseListener(this);
 		player1 = p1;
 		player2 = p1;
 		// maxWidth = p1.getField().getPreferredSize().width;
 		this.setPreferredSize(new Dimension(maxWidth, maxHeight));
 		this.setSize(new Dimension(maxWidth, maxHeight));
 	}
 
 	public String getPlayersID() {
 		return player1.getPlayerID() + ":" + player2.getPlayerID();
 	}
 
 	// start the game
 	public void playGame() {
 		System.out.println("play game");
 		// player1.initField();
 		// player2.initField();
 
 		defendingField = player1.getField();
 		System.out.println("player 1 is " + player1.getPlayerID());
 		attackingField = player2.getField();
 		// phaseImages = playingField.getPhaseImages();
 
 		defendingField.getDeckZone().drawCard();
 		defendingField.getDeckZone().drawCard();
 		defendingField.getDeckZone().drawCard();
 		defendingField.getDeckZone().drawCard();
 		defendingField.getDeckZone().drawCard();
 		
 		player1.getHand().preGameDiscard(this);
 	}
 
 	public void testGame() {
 		System.out.println("test game");
 		defendingField = player1.getField();
 		attackingField = player1.getField();
 		// phaseImages = playingField.getPhaseImages();
 
 		defendingField.getDeckZone().drawCard();
 		defendingField.getDeckZone().drawCard();
 		defendingField.getDeckZone().drawCard();
 		defendingField.getDeckZone().drawCard();
 		defendingField.getDeckZone().drawCard();
 
 		player1.getHand().preGameDiscard(this);
 	}
 	
 	public void startGame() {
 		// player1.drawField();
 		
 		player1.setCurrentPhase(Phase.STAND_PHASE);
 		currPhase = player1.getCurrentPhase();
 		nextPhase();
 	}
 
 	public void nextPhase() {
 
 		// levelUp(null);
 		if (gameStatus == 1)
 			return;
 
 		repaint();
 
 		System.out.println(currPhase);
 
 		if (currPhase == Phase.STAND_PHASE) {
 			standPhase();
 
 			currPhase = Phase.DRAW_PHASE;
 			player1.setCurrentPhase(Phase.DRAW_PHASE);
 
 		} else if (currPhase == Phase.DRAW_PHASE) {
 			drawPhase();
 			currPhase = Phase.CLOCK_PHASE;
 			player1.setCurrentPhase(Phase.CLOCK_PHASE);
 
 		} else if (currPhase == Phase.CLOCK_PHASE) {
 			clockPhase();
 			currPhase = Phase.MAIN_PHASE;
 			player1.setCurrentPhase(Phase.MAIN_PHASE);
 
 		} else if (currPhase == Phase.MAIN_PHASE) {
 			mainPhase();
 			currPhase = Phase.CLIMAX_PHASE;
 			player1.setCurrentPhase(Phase.CLIMAX_PHASE);
 
 		} else if (currPhase == Phase.CLIMAX_PHASE) {
 			climaxPhase();
 			currPhase = Phase.ATTACK_PHASE;
 			player1.setCurrentPhase(Phase.ATTACK_PHASE);
 
 		} else if (currPhase == Phase.ATTACK_PHASE) {
 			attackPhase();
 			currPhase = Phase.END_PHASE;
 			player1.setCurrentPhase(Phase.END_PHASE);
 
 		} else if (currPhase == Phase.END_PHASE) {
 			endPhase();
 			currPhase = Phase.STAND_PHASE;
 			player2.setCurrentPhase(Phase.STAND_PHASE);
 		}
 	}
 
 	public void paint(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g;
 		// paintWords(g2);
 		g2.setColor(Color.pink);
 		g2.setBackground(Color.pink);
 		Font original = g2.getFont();
 		g2.setFont(new Font("Arial", Font.BOLD, (int) (22 * gameScale)));
 
 		player1.getHand().paint(g2);
 
 		attackingField.paint(g);
 
 		// initialize nextRect
 		int initialX, initialY;
 		initialX = 50; // placeholder
 		initialY = 700; // placeholder
 
 		int i = 0;
 
 		translatedY = 0;
 
 		for (Phase phase : Phase.values()) {
 			if (currPhase == phase) {
 				g2.setColor(Color.RED);
 				g2.drawString(phase.toString(),
 						(int) ((initialX + i * offsetX) * (gameScale)),
 						(int) ((initialY) * (gameScale) + translatedY));
 				g2.setColor(Color.BLUE);
 
 				int xval = (int) ((initialX + (i + 1) * offsetX - 5) * (gameScale));
 				if (i + 1 < Phase.values().length) {
 				} else {
 					xval = (int) ((initialX + (0) * offsetX - 5) * (gameScale));
 				}
 				nextRect = new Rectangle(xval, (int) ((initialY - offsetY + 5)
 						* (gameScale) + translatedY),
 						(int) (offsetX * gameScale),
 						(int) (offsetY * gameScale));
 				g.drawRect(nextRect.x, nextRect.y, nextRect.width,
 						nextRect.height);
 
 			} else {
 				g2.drawString(phase.toString(),
 						(int) ((initialX + i * offsetX) * (gameScale)),
 						(int) ((initialY) * (gameScale) + translatedY));
 			}
 			i++;
 		}
 
 		// if (player1.getCurrentPhase() == Phase.STAND_PHASE) {
 		// // g.drawImage(bound, initialX, initialY, null);
 		// nextRect = new Rectangle(
 		// (int) ((initialX + 1 * offsetX) * (gameScale)),
 		// (int) ((initialY) * (gameScale) + translatedY),
 		// (int) (offsetX * gameScale), (int) (offsetY * gameScale));
 		// } else if (player1.getCurrentPhase() == Phase.DRAW_PHASE) {
 		// // g.drawImage(bound, initialX + 1 * offset, initialY, null);
 		// nextRect = new Rectangle(
 		// (int) ((initialX + 2 * offsetX) * (gameScale)),
 		// (int) ((initialY) * (gameScale) + translatedY),
 		// (int) (offsetX * gameScale), (int) (offsetY * gameScale));
 		// } else if (player1.getCurrentPhase() == Phase.CLOCK_PHASE) {
 		// // g.drawImage(bound, initialX + 2 * offset, initialY, null);
 		// nextRect = new Rectangle(
 		// (int) ((initialX + 3 * offsetX) * (gameScale)),
 		// (int) ((initialY) * (gameScale) + translatedY),
 		// (int) (offsetX * gameScale), (int) (offsetY * gameScale));
 		// } else if (player1.getCurrentPhase() == Phase.MAIN_PHASE) {
 		// // g.drawImage(bound, initialX + 3 * offset, initialY, null);
 		// nextRect = new Rectangle(
 		// (int) ((initialX + 4 * offsetX) * (gameScale)),
 		// (int) ((initialY) * (gameScale) + translatedY),
 		// (int) (offsetX * gameScale), (int) (offsetY * gameScale));
 		// } else if (player1.getCurrentPhase() == Phase.CLIMAX_PHASE) {
 		// // g.drawImage(bound, initialX + 4 * offset, initialY, null);
 		// nextRect = new Rectangle(
 		// (int) ((initialX + 5 * offsetX) * (gameScale)),
 		// (int) ((initialY) * (gameScale) + translatedY),
 		// (int) (offsetX * gameScale), (int) (offsetY * gameScale));
 		// } else if (player1.getCurrentPhase() == Phase.ATTACK_PHASE) {
 		// // g.drawImage(bound, initialX + 5 * offset, initialY, null);
 		// nextRect = new Rectangle(
 		// (int) ((initialX + 6 * offsetX) * (gameScale)),
 		// (int) ((initialY) * (gameScale) + translatedY),
 		// (int) (offsetX * gameScale), (int) (offsetY * gameScale));
 		// } else if (player1.getCurrentPhase() == Phase.END_PHASE) {
 		// // g.drawImage(bound, initialX + 6 * offset, initialY, null);
 		// nextRect = new Rectangle(
 		// (int) ((initialX + 0 * offsetX) * (gameScale)),
 		// (int) ((initialY) * (gameScale) + translatedY),
 		// (int) (offsetX * gameScale), (int) (offsetY * gameScale));
 		// }
 		// if (nextRect != null) {
 		// g.drawRect(nextRect.x, nextRect.y, nextRect.width, nextRect.height);
 		// }
 		g2.setFont(original);
 		Card card = player1.getField().getSelected();
 		if (card != null)
 			player1.retreiveCardInfo(card);
 		player1.updateStatsBox();
 		// player1.getHand().paint(g, 0, 0, null);
 	}
 
 	// representation of STAND PHASE
 	private void standPhase() {
 		if (standAllCards) {
 			// get all the cards in both front and back row
 			ArrayList<Card> temp = new ArrayList<Card>();
 			temp.add(defendingField.getFrontRow1().showCard());
 			temp.add(defendingField.getFrontRow2().showCard());
 			temp.add(defendingField.getFrontRow3().showCard());
 
 			temp.add(defendingField.getBackRow1().showCard());
 			temp.add(defendingField.getBackRow2().showCard());
 
 			// stand all rested character
 			for (Card c : temp) {
 				if (c != null && c.getCurrentState() == State.REST) {
 					c.setCurrentState(State.STAND);
 				}
 			}
 		}
 	}
 
 	// representation of DRAW PHASE
 	private void drawPhase() {
 		// draw card
 		defendingField.getDeckZone().drawCard();
 
 		hasClocked = false;
 	}
 
 	// representation of CLOCK PHASE
 	private void clockPhase() {
 		// TODO check for CLOCK action
 		Card c = defendingField.getSelected();
 
 		if (c != null && !hasClocked) {
 			// if clock has 7 or more, level up
 			// levelUp(c);
 			hasClocked = true;
 		}
 	}
 
 	// representation of MAIN PHASE
 	private void mainPhase() {
 		// advance to next phase only when the player clicks on the next
 		// phase tag
 	}
 
 	// representation of CLIMAX PHASE
 	private void climaxPhase() {
		Card c = defendingField.getSelected();
 
		if (c != null && c.getT() == Type.CLIMAX) {
			defendingField.getClimaxZone().setCard(c);
		}
 	}
 
 	// representation of ATTACK PHASE
 	private void attackPhase() {
 		// TODO fill in ATTACK PHASE
 
 		// advance to next phase only when the player clicks on the next
 		// phase tag
 	}
 
 	// representation of END PHASE
 	private void endPhase() {
 		// removing climax card from climax zone into waiting room
 		Card climaxCard = defendingField.getClimaxZone().showCard();
 		if (climaxCard != null) {
 			defendingField.getClimaxZone().removeCard();
 			defendingField.getWaitingRoom().setCard(climaxCard);
 		}
 	}
 
 	Rectangle nextRect;
 
 	/**
 	 * MainPhase : character swapping field field left click playing character
 	 * hand field right click startup effects event activation hand resolution
 	 * right click ClimaxPhase : climax activation hand field AttackPhase :
 	 * tapping character field field right click : auto effects : counter hand
 	 * field right click
 	 */
 	@Override
 	public void mouseClicked(MouseEvent e) {
 
 	}
 
 	public void mouseEntered(MouseEvent e) {
 		defendingField.mouseEntered(e);
 	}
 
 	public void mouseExited(MouseEvent e) {
 		defendingField.mouseExited(e);
 	}
 
 	public void mousePressed(MouseEvent e) {
 		// System.out.println("game press " + e.getX() + ", " + e.getY());
 		// defendingField.mousePressed(e);
 		// currentPlayer.getHand().mousePressed(e);
 		// player1.getHand().mousePressed(e);
 		repaint();
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		System.out.println("game click: " + e.getX() + ", " + e.getY());
 		if (gameStatus == 2) {
 		} else {
 			if (nextRect != null && nextRect.contains(e.getPoint())) {
 				nextPhase();
 			}
 			// currentPlayer.getHand().mouseClicked(e);
 			player1.getHand().mouseReleased(e);
 			defendingField.mouseReleased(e);
 
 			// if (// currentPlayer.getCurrentPhase() == Phase.CLOCK_PHASE
 			// player1.getCurrentPhase() == Phase.CLOCK_PHASE
 			// && e.getButton() == MouseEvent.BUTTON3) {
 			// if (hasClocked)
 			// nextPhase();
 			// }
 
 		}
 		// defendingField.repaintElements();
 		repaint();
 	}
 
 	public void mouseDragged(MouseEvent e) {
 		System.out.println("game drag " + e.getX() + ", " + e.getY());
 		defendingField.mouseDragged(e);
 		// player1.getField().mouseDragged(e);
 		repaint();
 	}
 
 	public void mouseMoved(MouseEvent e) {
 		defendingField.mouseMoved(e);
 	}
 
 	public Dimension getPreferredSize() {
 		return new Dimension((int) (maxWidth * gameScale),
 				(int) (maxHeight * gameScale));
 	}
 
 	// assign the game a game instance id
 	public void setGameID(int x) {
 		gameID = x;
 	}
 
 	// get the game instance id
 	public int getGameID() {
 		return gameID;
 	}
 }
