 /**
  * Park View Protector
  * 
  * @author Jason of Javateerz
  *
  * The player class.
  *
  */
 
 package org.javateerz.ParkViewProtector;
 
 import java.io.*;
 
 public abstract class Staff extends Character
 {
 	private static final long serialVersionUID = 3L;
 	
 	private String name;
 	
 	private int tp;
 	private int maxTp;
 	
 	public abstract Attack getAttack(int i);
 	
 	/*public Staff()
 	{
 		super(0, 0, 50, 50, 1.0, 1);
 		tp = 12;
 		maxTp = 12;
 		
 		sprite = DataStore.INSTANCE.getSprite("placeholder.png");
 	}*/
 	
 	// Creates a Staff member.
 	public Staff(String name, int x, int y, int hp, int maxHp, double speed, int tp, int maxTp)
 	{
 		super(x, y, hp, maxHp, speed);
 		this.name = name;
 		//tp=Teacher Points, Amount of points for use of skills
 		this.tp=tp;
 		this.maxTp=maxTp;
 	}
 	
 	protected void updateSprite()
 	{
 		sprite = DataStore.INSTANCE.getSprite("placeholder.png");
 	}
 	
 	//The character uses an item class uses an item
 	public void useItem(int item)
 	{
 		/*if(inventory.get(item) instanceof KeyItem)
 		{
 			inventory.get(item).run();
 		}*/
 	}
 	
 	//Return the current amount of TP that character has
 	public int getTp()
 	{
 		return tp;
 	}
 	
 	//Return the max TP of the character
 	public int getMaxTp()
 	{
 		return maxTp;
 	}
 	
 	//Changes the current amount of TP
 	public void adjustTp(int amount)
 	{
 		tp+=amount;
 	}
 	
 	//Changes the max TP
 	public void adjustMaxTp(int amount)
 	{
 		maxTp+=amount;
 	}
 	
 	//Sets the current amount of TP
 	public void setTp(int amount)
 	{
 		tp=amount;
 	}
 	
 	//Sets the max TP
 	public void setMaxTp(int amount)
 	{
 		maxTp=amount;
 	}
 	
 	public String getName()
 	{
 		return name;
 	}
 	
 	public void step(Game game)
 	{
 		decrementHitDelay(1);
 		game.tpRegen();
 		game.switchChar();
 		game.handleAttack();
 	}
 	
 	protected void validateState()
 	{
 		super.validateState();
 		
 		if(tp > maxTp)
 		{
 			throw new IllegalArgumentException("TP cannot exceed max TP");
 		}
 	}
 	
 	private void readObject(ObjectInputStream os) throws ClassNotFoundException, IOException
 	{
 		os.defaultReadObject();
 		
 		validateState();
 	}
 	
 	private void writeObject(ObjectOutputStream os) throws IOException
 	{
 		os.defaultWriteObject();
 	}
 }
 
 class Stats
 {
 	public static final int STARK_HP=100;
 	public static final int STARK_TP=100;
 	public static final int SPECIAL_HP=50;
 	public static final int SPECIAL_TP=300;
 }
