 package logic;
 
 public class Characters {
 
 	public int level;
 	public int exp;
 	public int resist;
 	public int health;
 	public int intellect;
 	public int strength;
 	public int agility;
 	public int damage;
 	public int armor;
 	public int critChance;
 	public int critStrike;
 	public int x = 10;
 	public int y = 240;
 	
 	public void move( String direction )
 	{
 		if ( direction.equals("forward") )
 		{
			x += 2;
 		}
 		else if( direction.equals("back"))
 		{
			x -= 2;
 		}
 		else if(direction.equals("jump"))
 		{
 			
 		}
 	}
 	public double meleeAttack()
 	{
 		double result = strength * 2 + damage + agility * 0.5;
 		return result;
 	}
 	public double mageAttack()
 	{
 		double result = intellect * 2 + damage + agility * 0.5;
 		return result;
 	}
 	public double rangeAttack()
 	{
 		double result = agility * 2 + damage;
 		return result;
 	}
 }
