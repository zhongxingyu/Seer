 package dungeonCrawler.GameElements;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.EnumSet;
 import java.util.LinkedList;
 
 import dungeonCrawler.ElementType;
 import dungeonCrawler.EventType;
 import dungeonCrawler.GameElement;
 import dungeonCrawler.GameEvent;
 import dungeonCrawler.GameLogic;
 import dungeonCrawler.GameObject;
 import dungeonCrawler.Vector2d;
 
 /**
  * @author Tissen
  *
  */
 public class Player extends GameElement {
 	private int Health=1000;
 	private int lives=2;
 	private LinkedList<GameObject> inventar = new LinkedList<GameObject>();
 	/**
 	 * @param position
 	 * @param size
 	 */
 	public Player(Vector2d position, Vector2d size) {
 		super(position, size, "PLAYER", EnumSet.of(ElementType.MOVABLE));
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		// TODO Auto-generated method stub
 		g.setColor(Color.BLUE);
 		g.fillRect(0, 0, size.getX(), size.getY());
 	}
 	
 	public void setPosition(Vector2d pos) {
 		this.position = pos;
 	}
 	
 	public void add(GameObject object){
 		inventar.add(object);
 	}
 
 	@Override
 	public void GameEventPerformed(GameEvent e) {
 
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void setHealt(int Health) {
 		this.Health = Health;
 	}
 	
 	public void reduceHealth(int Health, GameLogic logic) {
 		if (this.Health-Health > 0){
			System.out.println("!" + (this.Health-Health));
 			this.Health = this.Health-Health;
 			System.out.println("Health verloren! Health: " + this.Health);
 		}
 		else {
 			lives--;
 			if(lives<=0){
 				this.Health -= Health;
 				System.out.println("!TOT! (x.x) Health: " + this.Health);
 			} else {
 				this.Health = 1000;
 				logic.teleportElement(this, logic.getCheckPoint());
 			}
 		}
 	}
 	
 	public int getHealt() {
 		return this.Health;
 	}
 
 }
