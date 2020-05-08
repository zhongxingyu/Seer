 package se.chalmers.towerdefence.model;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 
 public class Level {
 	
 	
 	private GameBoard gameBoard;
 	private Road road;
 	private WaveController waveController;
 	private ArrayList <AbstractTower> towers = new ArrayList<AbstractTower>();
 	private ArrayList <AbstractProjectile> projectiles = new ArrayList<AbstractProjectile>();
 	private Player player;
 	public static HighScore highScore;
 	private boolean gameOver;
 
 	public Level(ISquare[][] gB){
 		gameBoard = new GameBoard(gB);
 		road = new Road(gameBoard);
 		player = new Player(20, 500);
 		waveController = new WaveController(road, player);
 		highScore = new HighScore(100, "level1");
 		gameOver=false;
 	}
 	
 	public void startWave(){
 		waveController.startNewWave();
 	}
 	
 	public ArrayList<AbstractTower> getTowers(){
 		return towers;
 	}
 		
 	public ArrayList <AbstractProjectile> getProjectiles(){
 		return projectiles;
 	}
 	
 	public void update(){
 		waveController.update();
 		if(player.getLives()==0 || waveController.allWavesAreDone()){
 			gameOver=true;
 		}
 		
 		for(AbstractTower t : towers){
 			t.tryShoot(waveController.getWavesOnGameBoard());
 		}
 		for(Iterator<AbstractProjectile> it = projectiles.iterator(); it.hasNext();){
 			AbstractProjectile p = it.next();
 			if(p.exists()){
 				p.move();
 			}else{
 				it.remove();
 			}
 		}
 	}
 	
 	public Player getPlayer(){
 		return player;
 	}
 	
 	public static HighScore getHS() {
 		return highScore;
 	
 	}
 	
 	public boolean gameOver(){
 		return gameOver;
 	}
 
 
 	public void buildTower(int x, int y) {
 		AbstractTower tower = new Tower(x,y,150,projectiles, 30, 200, 5);
 		if(!gameBoard.isBlocked(x,y) && tower.getCost()<=player.getResources()){
 			towers.add(tower);
 			gameBoard.addTower(x,y,tower);
 			player.useResources(tower.getCost());
 		}
 		
 	}
 	
 	public void sellTower(int x, int y) {
 		TowerSquare tempSquare = (TowerSquare) gameBoard.getSquare(x,y);
 		AbstractTower tower = tempSquare.getTower();
 		player.addResources(tower.sellTower());
 		towers.remove(tower);
		gameBoard.sellTower(x,y);
		
 	}
 	
 	public void upgradeTower(int x, int y) {
 		TowerSquare tempSquare = (TowerSquare) gameBoard.getSquare(x,y);
 		AbstractTower currentTower = tempSquare.getTower();
 		if(!(currentTower instanceof UpgradedTower) && currentTower.getUpgradeCost() <= player.getResources()){
 			player.useResources(currentTower.getUpgradeCost());
 			AbstractTower upgradedTower = currentTower.upgradeTower();
 			towers.add(upgradedTower);
 			towers.remove(currentTower);
 			gameBoard.addTower(x,y,upgradedTower);
 		}
 	}
 	
 	
 	public List<Wave> getWaves() {
 		return waveController.getWavesOnGameBoard();
 	}
 
 	public ArrayList<AbstractMonster> getMonster() {
 		ArrayList <AbstractMonster> monsters = new ArrayList<AbstractMonster>();
 		for(Wave w: getWaves()){
 			monsters.addAll(w.getmonstersInWave());
 		}
 		return monsters;
 	}
 
 
 }
