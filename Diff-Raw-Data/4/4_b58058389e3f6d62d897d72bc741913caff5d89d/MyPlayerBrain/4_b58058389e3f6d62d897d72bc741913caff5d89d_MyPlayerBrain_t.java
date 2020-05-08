 package net.windward.RoboRally;// Created by Windward Studios, Inc. (www.windward.net). No copyright claimed - do anything you want with this code.
 
 import net.windward.RoboRally.api.*;
 import sun.misc.BASE64Encoder;
 
 import java.awt.*;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 
 /**
  * The sample C# AI. Start with this project but write your own code as this is
  * a very simplistic implementation of the AI.
  */
 public class MyPlayerBrain {
 	private static final int NUM_CARDS = 1;
 
 	private String name = "shufanji";
 	private String password = "8888";
 	private byte[] avatar;
 
 	private final java.util.Random rand = new java.util.Random();
 
 	/**
 	 * Create the AI.
 	 * 
 	 * @param name
 	 *            he name of the player.
 	 * @param password
 	 *            The password of the player.
 	 */
 	public MyPlayerBrain(String name, String password) {
 		this.name = name;
 		this.password = password;
 	}
 
 	/**
 	 * The name of the player.
 	 * 
 	 * @return The name of the player.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * The password of the player.
 	 * 
 	 * @return The password of the player.
 	 */
 	public String getPassword() {
 		return password;
 	}
 
 	/**
 	 * The avatar of the player. Must be 32 x 32.
 	 * 
 	 * @return The avatar of the player. Must be 32 x 32.
 	 */
 	public byte[] getAvatar() throws IOException {
 
 		if (avatar == null) {
 			File file = new File("MyAvatar.png");
 			if (file.exists()) {
 				FileInputStream fisAvatar = new FileInputStream(file);
 				avatar = new byte[fisAvatar.available()];
 				fisAvatar.read(avatar, 0, avatar.length);
 				System.out.println("Avatar loaded: " + file.getAbsolutePath());
 			} else
 				System.out.println("Could not find avatar file: "
 						+ file.getAbsolutePath());
 		}
 
 		return avatar;
 	}
 
 	/**
 	 * Called when your robot must be placed on the board. This is called at the
 	 * start of the game and each time your robot dies.
 	 * 
 	 * @param map
 	 *            The game map. There will be no units on this map.
 	 * @param you
 	 *            Your player object.
 	 * @param players
 	 *            All players (including you).
 	 * @param robotStart
 	 *            The position(s) on the map where you can place your robot.
 	 *            This will be a single point unless another robot is on your
 	 *            archive point.
 	 * @param gameStart
 	 *            true if start of a game. false if re-entering the game.
 	 * @return Where to place your unit (location and direction.
 	 */
 	public final BoardLocation Setup(GameMap map, Player you,
 			java.util.List<Player> players, java.util.List<Point> robotStart,
 			boolean gameStart) {
 		return new BoardLocation(robotStart.get(0), MapSquare.DIRECTION.NORTH);
 	}
 
 	private boolean isPositionValid(GameMap map, BoardLocation bl) {
 		Point p = bl.getMapPosition();
 		return p.x < map.getWidth() && p.x >= 0 && p.y < map.getHeight()
 				&& p.y >= 0;
 	}
 	
 	
 	private float score(GameMap map, Card.ROBOT_MOVE m, Player you, int priority, HashMap<BoardLocation, Player> playerPlace){
 		float push = 0, fire = 0, flag = 0;
 		BoardLocation myLocation = you.getRobot().getLocation();
 		// push
 		push = 0;
 		
 		 int toMove = 0;
 		 int toTurn = 0;
 				switch (m) {
 				case BACKWARD_ONE:
 					toMove = -1;
 					toTurn = 0;
 					break;
 				case FORWARD_ONE:
 					toMove = 1;
 					toTurn = 0;
 					break;
 				case FORWARD_TWO:
 					toMove = 2;
 					toTurn = 0;
 					break;
 				case FORWARD_THREE:
 					toMove = 3;
 					toTurn = 0;
 					break;
 				case ROTATE_LEFT:
 					toMove = 0;
 					toTurn = -1;
 					break;
 				case ROTATE_RIGHT:
 					toMove = 0;
 					toTurn = 1;
 					break;
 				case ROTATE_UTURN:
 					toMove = 0;
 					toTurn = 2;
 					break;
 
 				}
 		
 		
 		if (map.GetSquare(myLocation.getMapPosition()).getConveyor() != null ||
 			map.GetSquare(myLocation.getMapPosition()).getType() == MapSquare.TYPE.ROTATE_CLOCKWISE ||
 			map.GetSquare(myLocation.getMapPosition()).getType() == MapSquare.TYPE.ROTATE_COUNTERCLOCKWISE){
 			if (toMove <= 0) return -1;
 		}
 				
 		BoardLocation finalLocation = myLocation.move(toMove);
 		finalLocation = finalLocation.Rotate(toTurn);
 		
 		if (!(this.isPositionValid(map, finalLocation))) return -1;
 		
 		if (playerPlace.containsKey(finalLocation)) {
 			Player pushed = playerPlace.get(finalLocation);
 			float dmgFactor = (pushed.getDamage() / 10f) * 0.2f;
 			float priFactor = (priority - 10) / 980f * 0.8f;
 			push = 2 * (dmgFactor + priFactor);
 			
 			Point pushedTo = pushed.getRobot().getLocation().move(toMove > 0 ? 1 : -1)
 					.getMapPosition();
 			try {
 				if (map.GetSquare(pushedTo).getType() == MapSquare.TYPE.PIT) {
 					push = push + 5 * (dmgFactor + priFactor);
 				}
 			} catch (Exception e) {
 			}
 		}
 
 			// fire
 			for (BoardLocation b = finalLocation; isPositionValid(map,
 					b); b = b.move(1)) {
 				int w = map.GetSquare(b.getMapPosition()).getWalls();
 				boolean stop = false;
 				switch (w) {
 				case MapSquare.SIDE_NONE:
 					break;
 				case MapSquare.SIDE_EAST:
 					if (finalLocation.getDirection() == MapSquare.DIRECTION.EAST)
 						stop = true;
 					break;
 				case MapSquare.SIDE_NORTH:
 					if (finalLocation.getDirection() == MapSquare.DIRECTION.NORTH)
 						stop = true;
 					break;
 				case MapSquare.SIDE_WEST:
 					if (finalLocation.getDirection() == MapSquare.DIRECTION.WEST)
 						stop = true;
 					break;
 				case MapSquare.SIDE_SOUTH:
 					if (finalLocation.getDirection() == MapSquare.DIRECTION.SOUTH)
 						stop = true;
 					break;
 				}
 				if (stop)
 					break;
 				if (playerPlace.containsKey(b)) {
 					fire = 1;
 					break;
 				}
 			}
 
 			// flag
 			MapSquare finalSquare = map.GetSquare(finalLocation.getMapPosition());
 			if (finalSquare.getType() == MapSquare.TYPE.PIT) return -1;
 
 			
 			int currentT = 1;
 			FlagState target = null;
 			for (int i = 0; i < 3; i++) {
 				if (you.getFlagStates().get(i).getTouched())
 					currentT++;
 				else {
 					target = you.getFlagStates().get(i); break;}
 			}
 			if (currentT > 3)
 				flag = 0f;
 
 			else if (finalSquare.getFlag() == currentT) {
 				flag = 5;
 			}
 			
 			// distance to target flag
 			
 			float distance = 0;
 			if (target != null) {
 				double disToTar = Math.pow ((finalLocation.getMapPosition().x - target.getPosition().x),2) +  Math.pow ((finalLocation.getMapPosition().y - target.getPosition().y),2);
 				distance = (float) (1 / disToTar);
 			}
 			
 			
 			return push + fire + flag + distance;
 	}
 
 	/**
 	 * Called each time the system needs another turn. If you do not return a
 	 * valid turn, the game will randomly move one of your units. This call must
 	 * return in under 1 second. If it has not returned in 1 second the call
 	 * will be aborted and a random move will be assigned.
 	 * 
 	 * @param map
 	 *            The game map with all units on it.
 	 * @param you
 	 *            Your player object. This is created for each call.
 	 * @param allPlayers
 	 *            All players including you. This is created for each call.
 	 * @param cards
 	 *            The cards you get to pick from. This does not include locked
 	 *            cards.
 	 * @return Your requested turn.
 	 */
 	public final PlayerTurn Turn(GameMap map, Player you,
 			java.util.List<Player> allPlayers, java.util.List<Card> cards) {
 		HashMap<BoardLocation, Player> playerPlace = new HashMap<BoardLocation, Player>();
 
 		
 		
 		for (Player p : allPlayers) {
 			if (p.getIsVisible()) {
 				playerPlace.put(p.getRobot().getLocation(), p);
 			}
 		}
 
 		Card maxCard = null;	
 		float maxScore = Float.NEGATIVE_INFINITY;
 		for (Card c : cards) {
 			Card.ROBOT_MOVE m = c.getMove();
 			float totalScore = this.score(map, m, you, c.getPriority(), playerPlace);
 			if (totalScore > maxScore) {
 				maxScore = totalScore;
 				maxCard = c;
 			}
 		}
 		
 		
 
 		// if hurt bad, consider power down
 		boolean powerDown = false;
		if (you.getDamage() >= 6 && you.getLives() == 1)
 			powerDown = true;
 
 		if (maxCard == null) System.out.println("i am getting a freaking null pointer becasue the number of cards are " + cards.size());
		if (maxCard == null) maxCard = you.getCards().get(0).clone();
 		java.util.ArrayList<Card> arrCards = new java.util.ArrayList<Card>();
 		arrCards.add(maxCard);
 
 		
 		//
 //		// get 40 sets, pick the one that's closest to the flag
 //		Card[] best = null;
 //		int bestDiff = Integer.MAX_VALUE;
 //		int okDiff = rand.nextInt(3);
 //		FlagState fs = null;
 //		for (FlagState fsOn : you.getFlagStates())
 //			if (!fsOn.getTouched()) {
 //				fs = fsOn;
 //				break;
 //			}
 //
 //		// get the flags, middle of the board if have them all.
 //		Point ptFlag = fs == null ? new Point(map.getWidth() / 2,
 //				map.getHeight() / 2) : fs.getPosition();
 //		for (int turnOn = 0; turnOn < 40; turnOn++) {
 //			// pick NUM_CARDS (or fewer if locked) random cards
 //			Card[] moveCards = new Card[NUM_CARDS];
 //			boolean[] cardUsed = new boolean[cards.size()];
 //			for (int ind = 0; ind < NUM_CARDS - you.getNumLockedCards(); ind++)
 //				for (int iter = 0; iter < 100; iter++) // in case can't work it
 //														// with these cards
 //				{
 //					int index = rand.nextInt(cards.size());
 //					if (cardUsed[index])
 //						continue;
 //					moveCards[ind] = cards.get(index).clone();
 //					cardUsed[index] = true;
 //					break;
 //				}
 //
 //			// add in the locked cards
 
 //
 //			// run it
 //			Utilities.MovePoint mp = Utilities.CardDestination(map, you
 //					.getRobot().getLocation().clone(), moveCards);
 //			if (mp.getDead())
 //				continue;
 //
 //			// if better than before, use it
 //			int diff = Math.abs(ptFlag.x - mp.getLocation().getMapPosition().x)
 //					+ Math.abs(ptFlag.y - mp.getLocation().getMapPosition().y);
 //			if (diff <= okDiff) {
 //				java.util.ArrayList<Card> arrCards = new java.util.ArrayList<Card>();
 //				Collections.addAll(arrCards, moveCards);
 //				return new PlayerTurn(arrCards, powerDown);
 //			}
 //			if (diff < bestDiff) {
 //				bestDiff = diff;
 //				best = moveCards;
 //			}
 //		}
 //
 //		if (best == null)
 //			return new PlayerTurn(new java.util.ArrayList<Card>(), powerDown);
 //		java.util.ArrayList<Card> arrCards = new java.util.ArrayList<Card>();
 //		Collections.addAll(arrCards, best);
 		
 		return new PlayerTurn(arrCards, powerDown);
 	}
 }
