 package GameEngine;
 
 import Dispatcher.*;
 import GameEngine.Player.PlayerType;
 import GameEngine.TowerManager.TowerTypes;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 /**
  * Project - TowerDefense</br>
  * <b>Class - GameManager</b></br>
  * <p>The GameManager is the one that can start the game and determine who has won.</br>
  * It creates the Player class and manage it. It has access to the WarManager when it's necessary,
  * for example when there is a tower's construction to check the player's money.
  * </p> 
  * <b>Creation :</b> 22/04/2013</br>
  * @author K. Akyurek, A. Beauprez, T. Demenat, C. Lejeune - <b>IMAC</b></br>
  * @see Player
  * @see WarManager
  */
 
 public class GameManager implements Runnable{
 	public static int idCount;
 	
 	//Thread managers
 	private boolean running;
 	private DispatcherManager dispatcher;
 	private ConcurrentLinkedQueue<Order> queue;
 	
     private ArrayList<Player> players;
     private ArrayList<PlayerType> playerTypes;
     
     private HashMap<PlayerType, Integer> bank;
 	
 	//Temporary !!
     private ArrayList<Tower> towers;
     private ArrayList<Base> bases;
     private ArrayList<Unit> units;
     
 	private MapManager mapManager;
 	private ArmyManager armyManager;
 	private TowerManager towerManager;
 	private WarManager warManager;
 	
 	private Timer timer;
 	private TimerTask timerTask;
 	private TimerTask baseTimerTask;
 	private TimerTask timeIndexTask;
     private static long timeStart;
     private static int timeIndex;
 	
     /**
      * Constructor of the GameManager class
      */
 	public GameManager() {
 		super();
 		running = false;
 		queue = new ConcurrentLinkedQueue<Order>();
 		players = new ArrayList<Player>();
 		
 		bank = new HashMap<PlayerType, Integer>();
 		
 		towers = new ArrayList<Tower>();
 		bases = new ArrayList<Base>();
 		units = new ArrayList<Unit>();
 	
 		idCount = 0;
 		
 		timeStart = 0;
 		timeIndex = 0;
 	}
 
 	public static long getTime(){
 		return System.currentTimeMillis()-timeStart;
 	}
 	
 	/**
 	 * Setter. Initiate the dispatcher attribute.
 	 * @see TowerDefense.TowerDefense#main(String[]) 
 	 */
 	public void setDispatcher(DispatcherManager dispatcher){
 		this.dispatcher = dispatcher;
 	}
 
 	/**
 	 * Setter. Initiate the running attribute.
 	 * @param running - tells if the engine threads is running
 	 * @see Dispatcher.DispatcherManager#start()
 	 * @see Dispatcher.DispatcherManager#stop()
 	 */
     public void setRunning(boolean running){
     	this.running = running;
     }
 			
 	/**
 	 * Initiate the game according to the player choices
 	 * @see Dispatcher.DispatcherManager#initiateGame()
 	 */
 	public void initiateGame(PlayerType humanType, int nbEnemies, ArrayList<PlayerType> enemiesType){
 		idCount = 0;
 		
 		//Creating the player (human and IA)
 		//Clear the player list
 		players.clear();
 		
 		//Adding the human player
 		players.add(new Player(humanType));
 		
 		playerTypes = new ArrayList<PlayerType>();
 		playerTypes.add(humanType);
 		
 		//Adding the enemies
 		Iterator<PlayerType> iter = enemiesType.iterator();
 		while (iter.hasNext()) {
 			PlayerType enemyType = iter.next();
 			players.add(new Player(enemyType));
 			playerTypes.add(enemyType);
 		}	
 		
 		//Adding a mapManager
 		mapManager = new MapManager("Map", playerTypes);
 		
 		//Adding the Tower and Army managers
 		armyManager = new ArmyManager();
 		towerManager = new TowerManager();
 		warManager = new WarManager();
 		
 		//Clear the towers list
 		towers.clear();
 		units.clear();
 				
 		//Retrieve the bases positions
 		Point[] basesPositions = mapManager.getPlayerBasePosition();
 		//Clear the bases list
 		bases.clear();
 		for(int i=0; i<enemiesType.size();i++){
 			bases.add(armyManager.createBase(idCount, basesPositions[i+1],enemiesType.get(i),false,mapManager.getPlayerTerritoryMap(i+1),mapManager.getPlayerProximityMap(i+1)));
 			++idCount;
 		}
 		//human player base
 		bases.add(armyManager.createBase(idCount, basesPositions[0],humanType,false,mapManager.getPlayerTerritoryMap(0),mapManager.getPlayerProximityMap(0)));
 		++idCount;
 		
 		//TODO add different sizes of neutral base
 		ArrayList<Point> neutralBasePosition = mapManager.getNeutralBasePosition();
 		for(int i=0; i<neutralBasePosition.size();i++){
 			bases.add(armyManager.createBase(idCount, neutralBasePosition.get(i),PlayerType.NEUTRAL,true,mapManager.getNeutralTerritoryMap(i),mapManager.getNeutralProximityMap(i)));
 			++idCount;
 		}
 		
 		//Initiate the bank
 		for(PlayerType playerType:playerTypes){
 			bank.put(playerType, 300);
 		}
 		
 		//Tells the dispatcher that the View need to be initialized : (bases, player's money)
 		dispatcher.initiateGameView(bases, 300);
 		
 		//Start the timer
 		timer = new Timer();
 		timeStart = System.currentTimeMillis();
 		timer();
 		
 	}
 	
 	public void timer(){
 		
 		//Actions each 100 milliseconds
 		timerTask=new TimerTask(){
             public void run(){
             	//get the current Date 
             	long playingTime = System.currentTimeMillis()-timeStart;
             	//System.out.println("Temps écoulé : " + playingTime);
             	
             	//Move units
             	for(Unit unit:armyManager.getUnits()){
             		PlayerType ptDst = unit.getDestination().getPlayerType();
             		int state =armyManager.moveUnit(unit, mapManager);
             		
         			if(state==0){
         				//Tell the dispatcher that the unit need to be move
         				dispatcher.addOrderToView(new MoveUnitOrder(unit.getId(), unit.getPosition()));
         			}
         			else{
         				//Tell the dispatcher to suppress the unit and to change the base amount
         				dispatcher.addOrderToView(new SuppressOrder(unit.getId()));
         				dispatcher.addOrderToView(new ChangeAmountOrder(unit.getDestination().getId(), unit.getDestination().getAmount()));
         				
         				//Updating the player's money
 						bank.put(unit.getPlayerType(), bank.get(unit.getPlayerType())+unit.getAmount() +10*timeIndex);
 						dispatcher.addOrderToView( new MoneyOrder(idCount, bank.get(unit.getPlayerType())+10*timeIndex, unit.getPlayerType()));
         				
         				if (state ==2){
         					//Change the owner of the towers in the territory of the base captured
         					if (ptDst!=PlayerType.NEUTRAL){
         						for (Tower t:towerManager.getTowers()){
             						int territoryMapValue = unit.getDestination().getTerritoryMap().getPixel(t.getPosition().x, t.getPosition().y);
                 					int playerNumber = 1;
                 					for (PlayerType p:playerTypes){
                 						if (p==t.getPlayerType()){
                 							break;
                 						}
                 						playerNumber++;
                 					}
                 					
                 					if (territoryMapValue!=playerNumber && territoryMapValue!=-1){
                 						System.out.println("Changing Owner of tower id="+t.getId()+"from player"+playerNumber+" to player"+territoryMapValue);
                 						
                 						//Changing the owner
                 						towerManager.changeOwner(t.getId(),unit.getPlayerType());
                 						dispatcher.addOrderToView(new ChangeOwnerOrder(t.getId(),unit.getPlayerType()));
                 						dispatcher.addOrderToAI(new ChangeOwnerOrder(t.getId(),unit.getPlayerType()));
                 						
                 						idCount++;
                 					}
         						}
             				}
         					
         					//Updating the player's money
     						bank.put(unit.getPlayerType(), bank.get(unit.getPlayerType())+50 );
     						dispatcher.addOrderToView( new MoneyOrder(idCount, bank.get(unit.getPlayerType())+50, unit.getPlayerType()));
         					
         					dispatcher.addOrderToView(new ChangeOwnerOrder(unit.getDestination().getId(),unit.getDestination().getPlayerType()));
         					dispatcher.addOrderToAI(new ChangeOwnerOrder(unit.getDestination().getId(),unit.getDestination().getPlayerType()));
         				}
         				
         				armyManager.suppressUnit(unit);
         				break;
         			}
         		}
             	
             	//Move Missiles
             	for(Missile missile:towerManager.getMissiles()){
             		if(missile.isArea()){
             			
             			//Make area damages            			
             			for(Unit unit:warManager.areaDamagesTarget(missile, armyManager)){
             				switch(missile.getAttackType()){
             				case NORMAL :
             					//If the unit has shield it has no damages
             					if(missile.getTarget().isProtected()){
             						System.out.println("Attack failed.");
             						missile.getTarget().setProtected(false);
             						break;
             					}else{
             						//Change the target's amount
     		            			int newAmount = unit.getAmount()-missile.getDamages();
     		            			unit.setAmount(newAmount);
     		            			//Updating the player's money
     		            			int gain = missile.getDamages()*10 + 10*timeIndex;
     		            			dispatcher.addOrderToView( new MoneyOrder(idCount, gain, missile.getOrigin().getPlayerType()));
     		            			//Tell the view that the unit need to update it's amount
     		            			dispatcher.addOrderToView(new ChangeAmountOrder(unit.getId(), newAmount));
     		            			idCount++;
                 					break;
             					}
             					
 	            			case SHIELD :
 	            				//TODO symbolize in the view ?
 	            				armyManager.createEffect(missile.getTarget(), missile.getAttackType(), getTime(),5000);
 	            				break;
 	            			case FROST :
 	            				//TODO symbolize in the view ?
 	            				//Apply a frost effect that reduce unit's speed by 0.2 for 5000 ms
 	            				armyManager.createEffect(missile.getTarget(), missile.getAttackType(), getTime(),5000, 0.2);
 	            				break;
 	            			default :
 	            				break;
             				}
             			}
             			
             			//Tell the view to suppress the missile
             			dispatcher.addOrderToView(new SuppressOrder(missile.getId()));
             			towerManager.suppressMissile(missile);
             			
             			break;
             			
             		}else{
             			
             			//Launch projectile
 	            		if(warManager.moveMissile(missile) == true){
 
 	            			//Tell the view to move the missile
 	            			dispatcher.addOrderToView(new MoveMissileOrder(missile.getId(), missile.getPosition()));
 	            			
 	            		}else{
 	            			//Impact implications
 	            			switch (missile.getAttackType()){
 	            			
 	            			//Basic Attack
 	            			case NORMAL :
 	            				//If the unit has shield it has no damages
 	            				if(missile.getTarget().isProtected()){
 	            					System.out.println("Attack failed.");
 	            					missile.getTarget().setProtected(false);
 	            					break;
 	            				}else{
 	            					//Change the target's amount
 		            				int newAmount = missile.getTarget().getAmount()-missile.getDamages();
 		            				missile.getTarget().setAmount(newAmount);
 		            				//Updating the player's money
     		            			int gain = missile.getDamages()*10+10*timeIndex;
     		            			dispatcher.addOrderToView( new MoneyOrder(idCount, gain, missile.getOrigin().getPlayerType()));
 		            				//Tell the view that the unit need to update it's amount
 		            				dispatcher.addOrderToView(new ChangeAmountOrder(missile.getTarget().getId(), newAmount));
 		            				idCount++;
 		            				break;
 	            				}
 	            				
 	            			//Attack that sends +10 agents to an unit
 	            			case GENERATION :
 	            				//Update unit's amount
 	            				int amount = missile.getTarget().getAmount() + missile.getDamages();
 	            				missile.getTarget().setAmount(amount);
 	            				dispatcher.addOrderToView(new ChangeAmountOrder(missile.getTarget().getId(), missile.getTarget().getAmount()));
 	            				break;
 	            				
 	            			default :
 	            				break;	
 	            			}
 	            			//Tell the view to suppress the missile	
             				dispatcher.addOrderToView(new SuppressOrder(missile.getId()));
             				towerManager.suppressMissile(missile);
 	            			break;
 	            		}
             		}
             	}
             	//Battles
             	warManager.war(armyManager, towerManager, dispatcher, playingTime);
             }
         };
 		
       
 		//Actions each 2 seconds
 		baseTimerTask=new TimerTask(){
             public void run(){
             	
             	//Increasing the amount of each base
             	for(Base base:armyManager.getBases()){
             		if(base.getPlayerType()!=PlayerType.NEUTRAL){
             			base.setAmount(base.getAmount()+1);
             			dispatcher.addOrderToView(new ChangeAmountOrder(base.getId(),base.getAmount()));
             		}
             	}
             }
         };
         
         //Action each 1 minute
         timeIndexTask = new TimerTask(){
         	public void run(){
         		timeIndex+=1;
         	}
         };
         
         timer.schedule(timerTask ,0, 100);
 		timer.schedule(baseTimerTask ,0, 2000);
 		timer.schedule(timeIndexTask, 0, 60000);
 	}
 	
 	public void endGame(){
 		//Stop the timer
 		timer.cancel();
 	}
 	
 	/**
 	 * Execute the player actions
 	 * @see GameManager#run()
 	 */	
 	public void execute(){
 		//Retrieve the current size of the queue
 		int size = queue.size();
 		
 		//Execute and remove the "size" first orders of the queue
 		if(size>0){
 			for(int i = 0;i<size; i++){
 				//Retrieve and remove the head of the queue
 				Order order = queue.poll();
 				
 				//If the order is a SuppressTowerOrder one
 				if(order instanceof SuppressOrder) {
 					
 					//Updating the player's money
 					int amount = bank.get(towerManager.getTower(order.getId()).getPlayerType());
 					double gain = towerManager.getTower(order.getId()).getCost() - towerManager.getTower(order.getId()).getCost()*0.2 - towerManager.getTower(order.getId()).getCost()*0.1*timeIndex;
 					double total = amount + gain;
 					bank.put(towerManager.getTower(order.getId()).getPlayerType(), (int)Math.abs(total));
 					dispatcher.addOrderToView(new MoneyOrder(idCount, (int)Math.abs(total),towerManager.getTower(order.getId()).getPlayerType()));
 					
 					//Remove the tower from the engine list
 					towerManager.suppressTower(order.getId());
 					//Tell the dispatcher that the tower need to be remove from the view
 					dispatcher.addOrderToView(new SuppressOrder(order.getId()));
 					idCount++;
 					
 				}
 				
 				//If the order is a EvolveTowerOrder one
 				if(order instanceof EvolveTowerOrder){
 					//Find the tower
 					Tower tower = towerManager.getTower((order).getId());
 					if(((EvolveTowerOrder) order).getType().cost() <= bank.get(tower.getPlayerType())){
 						//Tell the engine to make the tower evolve
 						towerManager.evolveTower((order).getId(), ((EvolveTowerOrder) order).getType());
 						//Update the player's money
 						int amount = bank.get(tower.getPlayerType()) - ((EvolveTowerOrder) order).getType().cost();
 						bank.put(tower.getPlayerType(), amount);
 						dispatcher.addOrderToView(new MoneyOrder(idCount, amount,tower.getPlayerType()));
 						//Tell the view that the tower need to be evolve
						dispatcher.addOrderToView(new EvolveTowerOrder((order).getId(),((EvolveTowerOrder) order).getType(), tower.getRange()));
 					}
 				}
 				
 				//If the order is a AddTowerOrder one
 				if(order instanceof AddTowerOrder) {
 
 					//Get the owner of the selected pixel in the territoryMap
 					int zoneId = mapManager.getTerritoryMapValue(((AddTowerOrder) order).getPosition().x, ((AddTowerOrder) order).getPosition().y);
 					
 					//Let half of the sprite height's and half of the sprite's width exceed the limits of territory
 					int spriteQuart = 15; //Size of sprite : 64x64
 					
 					int supRightZoneId = mapManager.getTerritoryMapValue(((AddTowerOrder) order).getPosition().x+spriteQuart, ((AddTowerOrder) order).getPosition().y+spriteQuart);
 					int supLeftZoneId = mapManager.getTerritoryMapValue(((AddTowerOrder) order).getPosition().x-spriteQuart, ((AddTowerOrder) order).getPosition().y+spriteQuart);
 					int infRightZoneId = mapManager.getTerritoryMapValue(((AddTowerOrder) order).getPosition().x+spriteQuart, ((AddTowerOrder) order).getPosition().y-spriteQuart);
 					int infLeftZoneId = mapManager.getTerritoryMapValue(((AddTowerOrder) order).getPosition().x-spriteQuart, ((AddTowerOrder) order).getPosition().y-spriteQuart);
 					
 					if(	supRightZoneId == zoneId && supLeftZoneId == zoneId &&
 							infLeftZoneId == zoneId && infRightZoneId == zoneId){
 						
 						//Compare the zone with the order of types in the playerTypes array 
 						
 						//0 : plain
 						if(zoneId !=0){
 							
 							//1 to 5 : territories
 							if(zoneId <6 && ((AddTowerOrder) order).getPlayerType()== playerTypes.get(zoneId-1)){
 								
 								//Test if there's enough money
 								if(((AddTowerOrder) order).getTowerType().cost() <= bank.get(((AddTowerOrder) order).getPlayerType()) ){
 								
 									//Add the Tower
 									towerManager.createTower(idCount, ((AddTowerOrder) order).getPlayerType(), ((AddTowerOrder) order).getTowerType(), ((AddTowerOrder) order).getPosition());
 									//Update the money
 									int amount = bank.get(((AddTowerOrder) order).getPlayerType()) - ((AddTowerOrder) order).getTowerType().cost();
 									bank.put(((AddTowerOrder) order).getPlayerType(), amount);
 									dispatcher.addOrderToView(new MoneyOrder(idCount, amount,((AddTowerOrder) order).getPlayerType()));
 									//Search the tower to add it to the view
 									Tower tower = towerManager.getTower(idCount);
 									dispatcher.addOrderToView(new AddTowerOrder(idCount, ((AddTowerOrder) order).getPlayerType(), tower.getPosition(),((AddTowerOrder) order).getTowerType(),tower.getRange()));
 									dispatcher.addOrderToAI(new AddTowerOrder(idCount, ((AddTowerOrder) order).getPlayerType(), tower.getPosition(), ((AddTowerOrder) order).getTowerType(), tower.getRange()));
 									++idCount;
 									
 								}else{
 									//Tell the dispatcher the player hasn't enough money
 									System.out.println("GameEngine says : You can't afford this !");
 									dispatcher.addOrderToView(new AddTowerOrder(-1,((AddTowerOrder) order).getPlayerType(), new Point(-1, -1), TowerTypes.SUPPORTTOWER, -1));
 								}
 								
 							}else{
 								
 								//Tell the dispatcher that the tower CAN'T be add on the view
 								System.out.println("GameEngine says : You try to add a tower but this is not your territory");
 								dispatcher.addOrderToView(new AddTowerOrder(-1,((AddTowerOrder) order).getPlayerType(), new Point(-1, -1), TowerTypes.SUPPORTTOWER, -1));
 							}
 							
 						}else{
 							//Tell the dispatcher that the tower CAN'T be add on the view
 							System.out.println("GameEngine says : maybe you should try on a hill...");
 							dispatcher.addOrderToView(new AddTowerOrder(-1, ((AddTowerOrder) order).getPlayerType(), new Point(-1, -1), TowerTypes.SUPPORTTOWER, -1));
 						}
 						
 					//The required part of the sprite is not on the same territory	
 					}else{
 						//Tell the dispatcher that the tower CAN'T be add on the view
 						dispatcher.addOrderToView(new AddTowerOrder(-1, ((AddTowerOrder) order).getPlayerType(), new Point(-1, -1), TowerTypes.SUPPORTTOWER, -1));
 					}
 					
 				}
 				
 				
 				//If the order is an AddUnitOrder one
 				if(order instanceof AddUnitOrder) {
 	
 					//Create the unit
 					Unit unit = armyManager.launchUnit(idCount, ((AddUnitOrder) order).getSrcId(), ((AddUnitOrder) order).getDstId(), ((AddUnitOrder) order).getAmount());
 					
 					//System.out.println("Engine - TODO : base : "+((AddUnitOrder) order).getSrcId()+" want to send "+unit.getAmount()+" Units to "+((AddUnitOrder) order).getDstId());
 					dispatcher.addOrderToView(new AddUnitOrder(idCount, ((AddUnitOrder) order).getSrcId(), ((AddUnitOrder) order).getDstId(), unit.getAmount()));
 					++idCount;
 					
 					//Retrieve the new source base amount
 					for(Base base:armyManager.getBases()){
 						if(base.getId() == order.getId()){
 							dispatcher.addOrderToView(new ChangeAmountOrder(order.getId(), base.getAmount()));
 							break;
 						}
 					}
 
 				}
 				
 			}
 		}
 	}
 	
 	/**
 	 * Add an order to the engine ConcurrentLinkedQueue queue
 	 * @see Dispatcher.DispatcherManager#addOrderToEngine(Order)
 	 */	
 	public void addOrder(Order order){
 		//Add the order to the queue
 		queue.add(order);
 	}
 	
 	/**
 	 * run() method of the engine thread
 	 */	
 	@Override
 	public void run() {		
 		while(running){
 			/*try{
 				Thread.sleep(500);
 			}
 			catch(InterruptedException e){
 				System.out.println(e.getMessage());
 			}*/		
 			execute();
 		}
 		
 	}
 
 }
