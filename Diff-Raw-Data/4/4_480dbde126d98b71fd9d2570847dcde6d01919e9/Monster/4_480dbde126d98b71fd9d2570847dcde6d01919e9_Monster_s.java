 /* 
 	JCavernApplet.java
 
 	Title:			JCavern And Glen
 	Author:			Bill Walker
 	Description:	
 */
 
 package jcavern.thing;
 
 import java.applet.*;
 import java.awt.*;
 import java.net.URL;
 
 import jcavern.*;
 
 /**
  * A Monster is a combatant that appears in the world.
  *
  * @author	Bill Walker
  * @version	$Id$
  */
 public class Monster extends Combatant
 {
 	/** * How many points the monster is worth when a Player kills it. */
 	private double			mWorth;
 
 	/** * The appropriate verb for when you hit this monster */
 	private String			mHitVerb;
 	
 	/** * The appropriate verb for when you kill this monster */
 	private String			mKilledVerb;
 		
 	/** * Likelihood that this monster wants to move on a given turn */
 	private static int		kMoveRadius = 4;
 	
 	/**
 	 * GraphicalMonsterViews controls how monsters appear in a WorldView.
 	 * Monsters appear very much like any Combatant in a WorldView, except that
 	 * they only hightlight when they are attacking a Player.
 	 */
 	public class GraphicalMonsterView extends Combatant.GraphicalCombatantView
 	{
 		/**
 		 * Decides whether a particular Combatant should be highlighted,
 		 * in the context of a particular event.
 		 *
 		 * @param	anEvent		the event that could trigger highlighting
 		 * @return				<CODE>true</CODE> if this combatant should be highlighted, <CODE>false</CODE> otherwise
 		 */
 		public boolean shouldHighlight(WorldEvent anEvent)
 		{
 			return
 				(anEvent != null) &&
 				(anEvent instanceof CombatEvent) &&
				((anEvent.getSubject() == Combatant.this) && (anEvent.getCause() instanceof Player)) ||
				((anEvent.getCause() == Combatant.this) && (anEvent.getSubject() instanceof Player));
 		}
 		
 		/**
 		 * Creates a new GraphicalMonsterView.
 		 *
 		 * @param	inImageName		the non-null String name of the image file
 		 */
 		public GraphicalMonsterView(String inImageName)
 		{
 			super(inImageName);
 		}
 	}
 	
 	/**
 	 * Creates a GraphicalThingView appropriate to this Thing.
 	 *
 	 * @param	inImageName		a non-null String image name
 	 * @return					a non-null GraphicalThingView appropriate to this Thing.
 	 */
 	public GraphicalThingView createGraphicalThingView(String inImageName)
 	{
 		return new GraphicalMonsterView(inImageName);
 	}
 	
 	/**
 	 * Creates a new monster with the given parameters.
 	 *
 	 * @param	name		a non-null String naming this monster
 	 * @param	imageName	a non-null String giving the image file name for this monster
 	 * @param	hitVerb		a non-null String giving the verb when this monster hits
 	 * @param	killedVerb	a non-null String giving the verb when this monster kills
 	 * @param	points		a non-null String giving how many points this monster has
 	 * @param	worth		a non-null String giving how many points this monster is worth when killed
 	 * @param	invisible	<CODE>true</CODE> if the monster is invisible, <CODE>false</CODE>otherwise
 	 */
 	public Monster(String name, String imageName, String hitVerb, String killedVerb, double points, double worth, boolean invisible)
 	{
 		super(name, imageName, (int) points, 0, invisible);
 	
 		//System.out.println("Monster(" + name + ", " + imageName + ", " + points + ", " + worth + ", " + invisible + ")");
 		
 		mWorth = worth;
 		mHitVerb = hitVerb;
 		mKilledVerb = killedVerb;
 	}
 	
 	/**
 	 * Returns the verb to use when this monster hits.
 	 *
 	 * @return	a non-null string like "hit" or "smote"
 	 */
 	public String getHitVerb()
 	{
 		return mHitVerb;
 	}
 	
 	/**
 	 * Returns the verb to use when this monster hits.
 	 *
 	 * @return	a non-null string like "killed" or "laid waste to"
 	 */
 	public String getKilledVerb()
 	{
 		return mKilledVerb;
 	}
 	
 	/**
 	 * Performs one turn by moving toward or attacking an adjacent Player.
 	 *
 	 * @param		aWorld					a non-null World in which action takes place
 	 * @exception	JCavernInternalError	couldn't find the Things in the world
 	 */
 	public void doTurn(World aWorld) throws JCavernInternalError
 	{
 		super.doTurn(aWorld);
 		
 		Player	aPlayer = aWorld.getPlayer();
 		
 		if ((aPlayer == null) || (! aPlayer.vulnerableToMonsterAttack(this)))
 		{
 			return;
 		}
 		
 		int		aDirection = aWorld.getDirectionToward(this, aPlayer);
 		int		aDistance = aWorld.getDistanceBetween(aPlayer, this);
 		
 		//System.out.println(getName() + " at " + aWorld.getLocation(this) + " " +
 		//						aDistance + " away, " + Location.directionToString(aDirection));
 
 		/*		
 		{see if each monster wants to move. If so, update both the
 		quadrant Q[,] array and the x[],y[] arrays.}
 		if Random(10)>3 then begin
 		*/
 		
 		// New scheme -- if the monster is within a certain distance of the player, it moves.
 		
 		if (aDistance < kMoveRadius)
 		{
 			try
 			{
 				aWorld.move(this, aDirection);
 			}
 			catch (ThingCollisionException tce)
 			{
 				try
 				{
 					attack(aWorld, tce.getMovee());
 				}
 				catch (NonCombatantException nce)
 				{
 					System.out.println(getName() + " tried to attack non combatant " + tce.getMovee());
 				}
 				catch (JCavernInternalError nst)
 				{
 					System.out.println(getName() + " tried to attack, encountered internal error " + nst);
 					throw nst;
 				}
 			}
 			catch (IllegalLocationException ile)
 			{
 				System.out.println(getName() + " tried to move off edge of world " + ile);
 			}
 		}
 	}
 	
 	/**
 	 * Returns whether this monster can attack the given combatant.
 	 *
 	 * @param	aCombatant	a potentially attackable opponent
 	 * @return				<CODE>true</CODE> if this Monster can attack the opponent, <CODE>false</CODE>
 	 */
 	public boolean canAttack(Combatant aCombatant)
 	{
 		//System.out.println(getName() + ".canAttack(" + aCombatant + ")");
 		return aCombatant.vulnerableToMonsterAttack(this);
 	}
 	
 	/**
 	 * Returns whether this monster can make a ranged attack the given combatant.
 	 *
 	 * @param	aCombatant	a potentially attackable opponent
 	 * @return				<CODE>true</CODE> if this Monster can ranged attack the opponent, <CODE>false</CODE>
 	 */	
 	public boolean canRangedAttack(Combatant aCombatant)
 	{
 		//System.out.println(getName() + ".canRangedAttack(" + aCombatant + ")");
 		return aCombatant.vulnerableToMonsterRangedAttack(this);
 	}
 	
 	/**
 	 * Returns whether this Monster is vulnerable to attack from the given Monsters.
 	 * By default, this is always false.
 	 *
 	 * @param	aMonster	a Monster that wants to attack
 	 * @return				<CODE>true</CODE> if the given Monster can attack, <CODE>false</CODE>
 	 */
 	public boolean vulnerableToMonsterAttack(Monster aMonster)
 	{
 		//System.out.println("Monster.vulnerableToMonsterAttack(Monster)");
 		return false;
 	}
 		
 	/**
 	 * Returns how much damage this monster does to this opponent.
 	 *
 	 * @param	opponent	the opponent to whom damage was done.
 	 * @return				how much damage was done.
 	 */
 	public int computeDamageTo(Combatant opponent)
 	{
 		/*
 		dam := Q[i,j].m.points/8 + Random(3) + Q[i,j].m.worth/4
 		     - My_arm_points;
 		if dam < 0.0 then dam := 0.0;
 		exp := exp - dam;
 		Message(' The '+Q[i,j].m.name+' hits.',TRUE);
 		Plot_Stats(TRUE);
 		*/
 		
 		return (int) (3 * Math.random() + (getPoints() / 8) + (getWorth() / 4));
 	}
 	
 	/**
 	 * Returns how much damage this monster does to this opponent during a ranged attack.
 	 *
 	 * @param	opponent	an opponent against whom a ranged attack is being conducted
 	 * @return				how much damage this monster would do in a ranged attack
 	 */
 	public int computeRangedDamageTo(Combatant opponent)
 	{
 		return 0;
 	}
 	
 	/**
 	 * Gains experience points when this monster kills a combatant.
 	 * Not currently in use.
 	 *
 	 * @param	theVictim	unused
 	 */
 	public void gainExperience(Combatant theVictim)
 	{
 	}
 	
 	/**
 	 * Returns how many points this Monster is worth when vanquished.
 	 *
 	 * @return 		how many points this Monster is worth when vanquished
 	 */
 	public int getWorth()
 	{
 		return (int) mWorth;
 	}
 
 	/**
 	 * Returns a copy of this monster.
 	 * How cool is cloning a monster!
 	 *
 	 * @return		a clone of this monster.
 	 */
 	public Object clone()
 	{
 		return new Monster(getName(), getGraphicalThingView().getImageName(), mHitVerb, mKilledVerb, getPoints(), mWorth, getInvisible());
 	}
 }
