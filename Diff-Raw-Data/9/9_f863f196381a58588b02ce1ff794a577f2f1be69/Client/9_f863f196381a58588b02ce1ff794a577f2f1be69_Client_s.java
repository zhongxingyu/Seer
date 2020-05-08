 package se.chalmers.dryleafsoftware.androidrally.libgdx;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import se.chalmers.dryleafsoftware.androidrally.controller.GameController;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.GameAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.HealthAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.HolderAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.MultiAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.SingleAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.SpecialAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.LaserView;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.RobotView;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Card;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 
 /**
  * This class talks to the server. It converts data from the server to appropriate classes which 
  * can then be fetched through getters. It also converts data is should sent to a format the server
  * can read.
  * 
  * @author
  *
  */
 public class Client {
 
 	// TODO: the client must somehow know which robotID the player has.
 	private final se.chalmers.dryleafsoftware.androidrally.controller.GameController controller;
 	private final int clientID, robotID;
 	// TODO: load the clientID from the user's phone's data.
 	// TODO: save the clientID when assigned one from the server.
 		
 	/**
 	 * Creates a new client instance.
 	 * @param clientID The ID number of the player.
 	 */
 	public Client(int clientID) {
 		this.controller = new GameController(8); 
 		this.clientID = clientID;
 		this.robotID = 0;
 	}
 	
 	public int getRobotID() {
 		return robotID;
 	}
 	
 	/**
 	 * Returns the map of the board as a matrix of strings.
 	 * @return A map of the board as a matrix of strings.
 	 */
 	public String getMap() {
 		return controller.getMap();
 	}
 	
 	/**
 	 * Sends the cards to the server. Note: This list should not contain more then five!
 	 * @param cards The cards to send.
 	 * @return <code>true</code> if the client successfully sent the cards.
 	 */
 	public boolean sendCard(CardView[] cards) {
 		// Send example: 12345:0:7:1:4:-1"
 		StringBuilder sb = new StringBuilder("" + robotID);
 		controller.setChosenCardsToRobot(sb.toString()); // TODO: server
 		for(int i = 0; i < 8; i++) {
 			if(i != robotID) {
 				controller.setChosenCardsToRobot(i + ":-1:-1:-1:-1:-1"); // TODO: remove
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Gives all the actions which was created during the last round.
 	 * @return A list of all the actions was created during the last round.
 	 */
 	public RoundResult getRoundResult() {
 		controller.getModel().moveRobots();
 		
 		RoundResult result = new RoundResult();	
 		String indata = controller.getModel().getAllMoves();
 		String[] allActions = indata.split(";");
 		
 		System.out.println(indata);
 		
 		for(String s : allActions) {
 			String[] parallel = s.split("#");
 			if(parallel[0].equals("R")) {
 				result.newPhase();
 			}else if(parallel[0].substring(0, 1).equals("B")) {
 				int phase = Integer.parseInt(parallel[0].substring(1));	
 				if(phase == 5) { // Laser phase
 					MultiAction multi = new MultiAction();
 					for(int i = 1; i < parallel.length; i++) {
 						String[] data = parallel[i].split(":");
 						HealthAction ha = new HealthAction(
 								Integer.parseInt(data[0]),
 								Integer.parseInt(data[1].substring(1)),
 								Integer.parseInt(data[1].substring(0, 1)));
 						multi.add(ha);
 					}
 					multi.setDuration(1000);
 					multi.setMoveRound(GameAction.PHASE_LASER);
 					result.addAction(multi);
 				}else{
 					GameAction action;
 					// If no actions follows:
 					if(parallel.length == 1) {
 						action = new HolderAction(1000);
 					}else{ // If actions
 						MultiAction multi = new MultiAction();
 						for(int i = 1; i < parallel.length; i++) {
 							multi.add(createSingleAction(parallel[i]));
 						}
 						action = multi;
 					}
 					action.setMoveRound(phase);
 					result.addAction(action);
 				}
 			}else if(parallel[0].equals("F")) {
 //				result.addAction(createSingleAction(parallel[1]));
 //				result.addAction(new SpecialAction(robotID,
 //						SpecialAction.HOLE_ACTION, SpecialAction.INVISIBLE_ACTION));
 //				result.addToNext(new SpecialAction(robotID, 
 //						SpecialAction.RESPAWN_ACTION,
 //						SpecialAction.VISIBLE_ACTION));
 				
 			}
 			// Generic multiaction
 			else if(parallel.length > 1){
 				MultiAction a = new MultiAction();
 				for(int i = 0; i < parallel.length; i++) {
 					a.add(createSingleAction(parallel[i]));
 				}	
 				result.addAction(a);
 			}else{
 				result.addAction(createSingleAction(parallel[0]));
 			}
 		}
 		return result;	
 	}
 	
 	/*
 	 * Creates a new action by reading the string provided.
 	 */
 	private SingleAction createSingleAction(String indata) {
 		String[] data = indata.split(":");
 		return new SingleAction(
 				Integer.parseInt(data[0]), 
 				Integer.parseInt(data[1].substring(0, 1)),
 				Integer.parseInt(data[1].substring(1, 3)),
 				Integer.parseInt(data[1].substring(3, 5)));
 	}
 	
 	/**
 	 * Gives the client's cards.
 	 * @return A list of the client's cards.
 	 */
 	public void loadCards(Texture texture, DeckView deck) {
 		// From server example: "410:420:480:660:780:840:190:200:90"
 		controller.getModel().dealCards(); // TODO: server input
 		List<CardView> cards = new ArrayList<CardView>();
 		deck.clearChosen();
 				
 		String indata = controller.getCards(robotID);
 		int i = 0;
 		for(String card : indata.split(":")) {
 			String[] data = card.split(";");
 			
 			int prio = (data.length == 2) ? Integer.parseInt(data[1]) : Integer.parseInt(data[0]);	
 			int regX = 0;
 			if(prio <= 60) {
 				regX = 0;	// UTURN
 			}else if(prio <= 410 && prio % 20 != 0) {
 				regX = 64;	// LEFT
 			}else if(prio <= 420 && prio % 20 == 0) {
 				regX = 128;	// LEFT
 			}else if(prio <= 480) {
 				regX = 192;	// Back 1
 			}else if(prio <= 660) {
 				regX = 256;	// Move 1
 			}else if(prio <= 780) {
 				regX = 320;	// Move 2
 			}else if(prio <= 840) {
 				regX = 384;	// Move 3
 			}	
 
 			CardView cv = new CardView(new TextureRegion(texture, regX, 0, 64, 90), 
 					prio, i);
 			cv.setSize(78, 110);
 			
 			if(data.length == 2) {
 				int lockPos = Integer.parseInt(data[0].substring(1));
 				deck.setLockedCard(cv, lockPos);
 			}else{
 				cards.add(cv);
 			}			
 			i++;
 		}
 		Collections.sort(cards);
 		deck.setDeckCards(cards);
 	}
 	
 	/**
 	 * Gives all the players robots in the current game as a list.
 	 * @param texture The textures to use when displaying the robots.
 	 * @param dockPositions All the docks' positions.
 	 * @return A list of all the robots.
 	 */
 	public List<RobotView> getRobots(Texture texture, Vector2[] dockPositions) {	
 		// TODO: server input
 		List<RobotView> robots = new ArrayList<RobotView>();	
 		for(int i = 0; i < Integer.parseInt(controller.getNbrOfPlayers()); i++) {
 			RobotView robot = new RobotView(i, new TextureRegion(texture, i * 64, 64, 64, 64),
 					new LaserView(new TextureRegion(texture, 64, 192, 64, 64), 0));
 			robot.setPosition(dockPositions[i].x, dockPositions[i].y);
 			robot.setOrigin(20, 20);
 			robots.add(robot);
 		}		
 		return robots;
 	}
 }
