 package poly.game;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Scanner;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 public class RiskGame extends Canvas{
 
 	/**
 	 * Risk game global class
 	 */
 	private static final long serialVersionUID = 1L;
 	public static final int PHASE_INITIAL 		= 0;
 	public static final int PHASE_TURN_BEGINS 	= 1;
 	public static final int PHASE_REINFORCE		= 2;
 	public static final int PHASE_ATTACK 		= 3;
 	public static final int PHASE_MOVE_ARMIES 	= 4;
 	public static final int PHASE_BONUS 		= 5;
 
 	public ArrayList<Player> 				players;
 	public ArrayList<Territory> 		territories;
 	
 	public Territory					attacker;
 	public Territory					defender;
 
 	public Player currentPlayer;
 	public int currentPlayerIndex;
 	
 	public static int BONUS_UNITS_COUNTER 	= 5;
 	protected static int STARTING_UNITS 	= 30;
 	protected boolean isOver = false;
 	
 	public static JFrame frame;
 	public JButton distributionButton;
 	public JButton roundButton;
 
     public Image riskMap;
     public Font font;
     
     public boolean isDistributionReady = false;
  
 	public RiskGame(){
 		this.setupUI();
 	}
 
 	// Entry point
 	public void startGame(){
 
 		// Initial phase
 		initPlayers();
 		initTerritories();
 		
 		while(!isDistributionReady){
 			try {
 				Thread.sleep(60);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		ditributeTerritories();
 		placeRemainingUnits();
 		frame.repaint();
 
 		// Game phase
 		playGame();
 	}
 
 	// Initialize all territories
 	public void initTerritories(){
 		Map map = new Map();
 		territories = map.generate();
 		System.out.println("------- STARTING TERRITORIES DISTRIBUTION -------");
 
 	}
 
 	// Initialize players
 	public void initPlayers(){
 		players = new ArrayList<Player>();
 		currentPlayerIndex = -1;
 
 		Player sam 	= new RandomAI("Sam");
 		sam.color = Color.white;
 		
 		Player sam2 = new RandomAI("Emile");
 		sam2.color = Color.blue;
 		
 		Player sam3 = new RandomAI("Pong");
 		sam3.color = Color.green;
 		
 		Player sam4 = new RandomAI("Maxim");
 		sam4.color = Color.red;
 
 		players.add(sam);
 		players.add(sam2);
 		players.add(sam3);
 		players.add(sam4);
 
 	}
 
 	// Players each pick a territory one after the other
 	private void ditributeTerritories(){
 		Random random = new Random();
 
 		// Random player starts choosing his territory
 		currentPlayerIndex = random.nextInt(players.size());
 
 		int startingUnits = STARTING_UNITS;
 		for(Player p : players){
 			p.remainingUnits = startingUnits;
 			p.allTerritories = territories;
 		}
 
 		// Choosing round if any territories not yet assigned
 		while(!Map.allTerritoriesAssigned(territories)){
 
 			currentPlayer = players.get(currentPlayerIndex);
 			String territory = currentPlayer.chooseTerritory();
 
 			// Try to acquire a chosen territory and place 1 unit on it
 			if(Map.acquireTerritory(territory, currentPlayer, 1, territories)){
 				System.out.println(currentPlayer.name + " selected and got " + territory);
 				currentPlayer.remainingUnits -= 1;
 
 				// Next player turn
 				currentPlayerIndex = (currentPlayerIndex + 1)%players.size();
 			} else {
 				System.out.println(currentPlayer.name + " tried to select " + territory + " but couldn't");
 			}
 		}    
 		System.out.println();
 		System.out.println("------- ALL TERRITORIES DISTRIBUTED -------");
 	}
 
 	// Initial phase, place all remaining units until all players have 0
 	private void placeRemainingUnits(){
 		int remainingUnitsThisRound = 3;
 
 		while(!allUnitsPlaced()){
 			currentPlayer = players.get(currentPlayerIndex);
 
 			// Check if the player still has reinforcements to place
 			if(currentPlayer.remainingUnits > 0){
 
 				// Selection of the territory and with how many units
 				int nbReinforcement = currentPlayer.chooseNbOfUnits(remainingUnitsThisRound);
 				String territory = currentPlayer.pickReinforceTerritory();
 
 				// Try to reinforce
 				if(Map.reinforceTerritoryWithUnits(territory, currentPlayer, nbReinforcement, territories)){
 					System.out.println(currentPlayer.name + " assigned " + nbReinforcement + " units on : " + territory);
 					remainingUnitsThisRound -= nbReinforcement;
 
 					if(remainingUnitsThisRound == 0){
 						currentPlayerIndex = (currentPlayerIndex + 1)%players.size();
 						currentPlayer = players.get(currentPlayerIndex);
 						if(currentPlayer.remainingUnits >= 3){
 							remainingUnitsThisRound = 3;
 						} else {
 							remainingUnitsThisRound = currentPlayer.remainingUnits;
 						}
 					}
 				} else{
 					System.out.println("An error occured");
 				}
 
 				// No more reinforcements, player is out	
 			} else { 
 				currentPlayerIndex = (currentPlayerIndex + 1)%players.size();
 			}
 		}
 		System.out.println("Initialization all done!");
 		System.out.println();
 	}
 
 	// Check if all players have placed all of their units
 	boolean allUnitsPlaced(){
 		for(Player p : players){
 			if(p.remainingUnits == 0){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void playGame(){
 		// Random first turn pick
 		currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
 		currentPlayer = players.get(currentPlayerIndex);
 
 		while(!isOver){
 			// Execute the turn for currentPlayer
 			executeTurn();
 			executeGameOverCheck();
 
 			// Next player
 			currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
 			currentPlayer = players.get(currentPlayerIndex);
 			
 			// draw frame
 			frame.repaint();
 		}
 		System.out.println("Game Over!");
 	}
 
 	private void executeTurn(){
 
 		int currentNbTerritories = currentPlayer.occupiedTerritories.size() - 1;
 
 		// Acquire and Place new reinforcements
 		executeReinforcementsPhase();
 
 		// Attack other territories
 		executeAttackPhase();
 
 		// Hand out a card if necessary
 		executePostAttackPhase(currentNbTerritories);
 		
 		// Move units from a territory to another
 		executeMovementPhase();
 
 	}
 
 	private void executeReinforcementsPhase(){
 		int newReinforcements = calculateNbReinforcements();
 				
 		currentPlayer.remainingUnits = newReinforcements;
 		System.out.println("-------- Reinforcement --------");
 		System.out.println(currentPlayer.name + " Recieved : "+newReinforcements + " new units");
 
 		currentPlayer.printTerritories();
 
 		while(currentPlayer.remainingUnits > 0){
 			currentPlayer.assignReinforcements();
 		}
 	}
 
 	private void executeAttackPhase(){
 		Scanner scan = new Scanner(System.in);
 
 		// Analyze board situation when about to enter combat (AI) 
 		currentPlayer.updateModel();
 
 		// Check if the player wants to attack
 		while(currentPlayer.willAttack){
 
 			currentPlayer.prepareCombat();
 
 			// Try to get an attacking territory
 			if((this.attacker = currentPlayer.getAttackingTerritory()) != null){
 
 				// Get the defending territory
 				this.defender = currentPlayer.getTargetTerritory();
 
 				// Get the amount of units used for this fight (MAXIMUM 3, MINIMUM 1)
 				int units = currentPlayer.getNbOfAttackingUnits();
 
 				// Check if number of units is legal
 				if(units <= 3 && units >= 1){
 					if(attacker.name != defender.name){
 						//String userInput = scan.nextLine();
 
 						int[] unitsLost;
 						unitsLost = BattleManager.executeAttackPhase(attacker, defender, units);
 
 						// Analyze the outcome of the last combat round (AI)
 						// passing the currentPlayer lost units and the defending player lost units
 						// for results analysis or else
 						currentPlayer.combatAnalysis(unitsLost[0], unitsLost[1]);
 					}
 				} else {
 					System.out.println("Too many or too few units chosen : "+units);
 				}
 			} else {
 				//System.out.println("No territory was chosen thus no attack");
 			}		
 
 			// Re-update our model
 			currentPlayer.updateModel();
 			
 			// draw frame
 			frame.repaint();
 		}
 		System.out.println("End of the ATTACK phase -------------");
 	}
 
 	private void executePostAttackPhase(int initialNbTerritories)
 	{
 		// Current player has won at least one territory during last combat phase
 		if(initialNbTerritories < currentPlayer.occupiedTerritories.size() -1){
 			Card newcard = new Card();
 			if(Card.addCard(currentPlayer.cards, newcard)){
 				System.out.println(currentPlayer.name+" recieved a new Card");
 			}
 		}
 	}
 	
 	private void executeMovementPhase(){
 		currentPlayer.chooseMovementTerritoriesAndUnits();
 		
 		Territory from = currentPlayer.getOriginMovementTerritory();
 		Territory to = currentPlayer.getDestinationMovementTerritory();
 		int units = currentPlayer.getMovementUnits();
 		
 		if(from != null && to != null && units > 0){
 			if(currentPlayer.moveSoldiers(from, to, units)){
 				System.out.println("Player made a units move");
 			} else {
 				System.out.println("No movement was made");
 			}
 		}
 	}
 
 	private void executeGameOverCheck(){
 		try {
 			synchronized (currentPlayer) {
 				for(Player p : players){
 					if(p.occupiedTerritories.size() == 0){
 						Scanner scan = new Scanner(System.in);
 						System.out.println("Player : " + p.name + " was Eliminated!!!");
 						players.remove(p);
 						String userInput = scan.nextLine();
 					}
 				}
 			}
 		} catch (Exception e) {
 			System.out.println("UNEXPECTED ERROR!!!");
 			System.out.println(e.toString());
 		}
 
 		if(players.size() == 1){
 			isOver = true;
 			Player winner = players.get(0);
 			System.out.println(winner.name + " WON THE GAME !!!");
 		}
 	}
 
 	private int calculateNbReinforcements(){
 		int num = 3;
 
 		num += Map.getContinentReinforcements(currentPlayer.occupiedTerritories);
 
 		int nbControlledTerritories = currentPlayer.occupiedTerritories.size();
 
 		// Add by number of controlled territories
 		if(nbControlledTerritories > 30){
 			num += 7;
 		} else if(nbControlledTerritories > 27 && nbControlledTerritories < 30){
 			num += 6;
 		} else if(nbControlledTerritories > 24 && nbControlledTerritories < 27){
 			num += 5;
 		} else if(nbControlledTerritories > 21 && nbControlledTerritories < 24){
 			num += 4;
 		} else if(nbControlledTerritories > 18 && nbControlledTerritories < 21){
 			num += 3;
 		} else if(nbControlledTerritories > 15 && nbControlledTerritories < 18){
 			num += 2;
 		} else if(nbControlledTerritories > 12 && nbControlledTerritories < 15){
 			num += 1;
 		}
 		
 		num +=  getCardUnits();
 
 		int totalUnits = currentPlayer.countUnits();
 		if(totalUnits + num >= 100){
 			System.out.println("Max Unit count reached");
 			num = 100 - currentPlayer.countUnits();
 		}
 		return num;
 	}
 	
 	private int getCardUnits(){
 		ArrayList<Card> cards = currentPlayer.tradeCards();
 		if(cards != null){
 			if(cards.size() == 3){
 				int bonus = Card.tradeCards(currentPlayer, cards.get(0), cards.get(1), cards.get(2));
 				if(bonus != 0){
 					bonus += BONUS_UNITS_COUNTER;
 					if(BONUS_UNITS_COUNTER < 40){
 						BONUS_UNITS_COUNTER += 5;
 					}
 					return bonus;
 				} else {
 					System.out.println("Bad cards function");
 				}
 			}
 			else {
 				System.out.println("Bad cards count");
 			}
 		}
 		
 		return 0;
 	}
 	
 	public void setupUI(){
         frame = new JFrame("Risk");
         riskMap = new ImageIcon(this.getClass().getResource("riskmap.png")).getImage();
         font = new Font ("Verdana", Font.BOLD , 18);
         
         distributionButton = new JButton("Distribute");
         distributionButton.setVisible(true);
         distributionButton.setSize(new Dimension(100, 30));
         distributionButton.setLocation(0, 0);
         
         distributionButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				isDistributionReady = true;
 				distributionButton.setEnabled(false);
 			}
         });
         frame.add(distributionButton);
         frame.add(new MyPanel());
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(1200, 780);
         frame.setLocationRelativeTo(null);
         frame.setVisible(true);
 	}
 	
 	public class MyPanel extends JPanel {
         /**
 		 * Simple panel to display primitive graphics
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public void paint(Graphics g) {
             Graphics2D g2d = (Graphics2D) g;
             g2d.drawImage(riskMap, 0, 0, null);
             g.setFont(font);
             
             // Draw all player units (with their respective color)
             try {
             	synchronized (g2d) {
 	            	for(Player p : players){
 	                	synchronized (p) {
 	                    	for(Territory t : p.occupiedTerritories){
 	                    		synchronized (t) {
 	                    			g.setColor(p.color);
 	                    			g2d.drawString(Integer.toString(t.getUnits()), t.position.x, t.position.y);
 								}
 	                    	}
 	    				}
 	                }
 				}
             } catch (Exception e) {}
             
             // Draw Combat names
             if(attacker != null && defender != null){
     			g.setColor(Color.WHITE);
     			String combatString = attacker.getOwner().name + " is attacking " + defender.getOwner().name +
     							" from " + attacker.name + " to " + defender.name;
     			g2d.drawString(combatString, 550, 30);
             }
  
             // Draw Player names
             int offset = 0;
             try {
 	            for(Player p : players){
 	            	synchronized (p) {
 	        			g.setColor(p.color);
 	        			g2d.drawString(p.name, 30, 650 + offset);
 	        			offset += 20;
 					}
 	
 	            }
 			} catch (Exception e) {}
         }
     }
 }
