 package dungeonCrawler.GameElements;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.EnumSet;
 import java.util.LinkedList;
 
 import dungeonCrawler.ElementType;
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
 	public final int maxHealth = 1000;
 	public final int maxMana = 100;
 	private int Health=maxHealth;
 	private int mana = maxMana;
 	public final int maxArmor = 1000;
 	private int armor = 0;
 	private int lives=3;
 	private int money;
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
 	
 	public void add(GameObject object){
 		inventar.add(object);
 	}
 
 	@Override
 	public void GameEventPerformed(GameEvent e) {
 
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void setHealth(int Health) {
 		this.Health = Health;
 	}
 	
 	public void increaseHealth(int Health) {
 		this.Health += Health;
 		if(this.Health > this.maxHealth) this.Health = this.maxHealth;
 	}
 	
 	public void reduceHealth(int Health, GameLogic logic) {
 		this.armor -= Health;
 		System.out.print("Rstung absorbiert ");
 		if (this.armor<0) {
 			System.out.println((Health+this.armor) + " Schaden");
 			Health = this.armor*(-1);
 			this.armor = 0;
 		}
		else {
			System.out.println(Health + " Schaden");
			Health = 0;
		}
 		if (this.Health-Health > 0){
 			this.Health = this.Health-Health;
 			System.out.println("Health verloren! Health: " + this.Health);
 		}
 		else {
 			lives--;
 			if(lives<0){
 				this.Health -= Health;
 				System.out.println("!TOT! (x.x) Health: " + this.Health);
 			} else {
 				this.Health -= Health;
 				System.out.println("!TOT! (x.x) Health: " + this.Health);
 				this.Health = 1000;
 				logic.teleportElement(this, logic.getCheckPoint());
 			}
 		}
 	}
 	
 	public boolean reduceMana(int mana, GameLogic logic) {
 		if (this.mana-mana >= 0){
 			this.mana = this.mana-mana;
 			return true;
 		}
 		return false;
 	}
 	public void increaseMana(int mana) {
 		this.mana += mana;
 		if(this.mana > this.maxMana) this.mana = this.maxMana;
 	}	
 	
 	public int getHealth() {
 		return this.Health;
 	}
 	
 	public int getMana() {
 		return mana;
 	}
 	
 	public void setMana(int m) {
 		this.mana = m;
 	}
 	
 	public LinkedList<GameObject> getInventar() {
 		return inventar;
 	}
 	
 	public int getMoney() {
 		return this.money;
 	}
 
 	public void setMoney(int money) {
 		this.money = money;
 	}
 	
 	public void addItem(GameObject item) {
 		this.inventar.add(item);
 	}
 	
 	public void increaseArmor(int arm) {
 		this.armor += arm;
 		if(this.armor > this.maxArmor) this.armor = this.maxArmor;
 	}
 	
 
 
 }
