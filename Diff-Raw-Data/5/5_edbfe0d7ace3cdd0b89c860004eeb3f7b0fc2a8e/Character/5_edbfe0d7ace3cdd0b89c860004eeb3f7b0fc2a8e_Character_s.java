 package game.things;
 
 import game.*;
 
 import java.util.*;
 
 import util.*;
 
 
 /**
  * 
  * @author wheelemaxw and zerzoumax
  * 
  * Abstract Class for any objects in the game that could be classed a characters
  * e.g. Player, Enemies, Shopkeepers
  * 
  * Contains common fields, like health, stats, dead & dying status.
  * 
  */
 
 public abstract class Character extends AbstractGameThing {
 	private String renderer;
 	private int health;
 	private int maxhealth;
 	private int attack, strength, defence, delay;
 	private boolean dead;
 	private boolean dying = false; /*couldnt work around the scheduling issues with attacking,
 	so boolean was the best way to stop animate being scheduled over*/
 
 	/**
 	 * Creates a character, introduces to the world with super, then sets
 	 * renderer and maximum health(all characters have 1000
 	 */
 	public Character(GameWorld world, String r){
 		super(world);
 		renderer = r;
 		maxhealth(1000);
 	}
 
 	/**
 	 * Returns a blank renderer if the character is dead
 	 * Otherwise, the provided renderer
 	 * @return The current renderer string
 	 */
 	public String renderer(){
 		if(dead) return "invisible";
 		if(this instanceof Player)return "character_" + renderer + "_" + rendererState();
 		else return "npc_"+ renderer;
 	}
 	
 	/**
 	 * Returns the state (holding weapon etc)
 	 * @return State, if holding weapons etc, but all characters are
 	 * empty by default
 	 */
 	public String rendererState() { // TODO: depends on equipped state
 		return "";
 	}
 
 	/**
 	 * Sets the Characters stats
 	 * @param at - Attack
 	 * @param st - Strength
 	 * @param de - Defence
 	 * @param dl - Delay
 	 */
 	public void setStats(int at, int st, int de, int dl){
 		attack = at;
 		strength = st;
 		defence = de;
 		delay = dl;
 	}
 	
 	
 	public String type(){
 		return renderer;
 	}
 
 	//Getters
 	public int health(){ return health; }
 	public int health(int s){ return health = s; }
 	public int maxhealth(){ return maxhealth; }
 	public int maxhealth(int s){ return maxhealth = s; }
 
 	/**
 	 * 
 	 * @return Default step delay
 	 */
 	public int walkdelay(){
 		return 50;
 	}
 
 	/**
 	 * 
 	 * @return Step dealy if escaping from Battle
 	 */
 	public int escapedelay(){
 		return 60;
 	}
 
 	public void distract(){}
 
 	/**
 	 * Method for moving to specifc location
 	 * @param lwhere - where to
 	 * @param dist - how far
 	 * @param ondone - run when done
 	 * @param keepfollow - Boolean to keep following or not
 	 * @return true if gets there, otherwise false
 	 */
 	public boolean moveTo(final Location lwhere, final int dist, final Runnable ondone, final boolean keepfollow){
 		if(health <= 0) return false;
 		if(!keepfollow){
 			following = null;
 			attackIdent = null;
 		}
 		Location l = location();
 		if(lwhere instanceof Level.Location && l instanceof Level.Location){
 			distract();
 			final Level.Location where = (Level.Location)lwhere;
 			Level.Location to = ((Level.Location)l).nextTo(where, this, dist);
 			if(to == null)
 				return false;
 			world().schedule(new Runnable(){
 				public void run(){
 					step(where, ondone, dist, stepIdent = new Object());
 				}
 			}, attacked()? escapedelay() : walkdelay());
			face(l);
 			return true;
 			
 		}
 		return false;
 	}
 
 	/**
 	 * Non-following moveTo
 	 * @param where
 	 * @param dist
 	 * @param ondone
 	 * @return
 	 */
 	public boolean moveTo(Location where, int dist, Runnable ondone){
 		return moveTo(where, dist, ondone, false);
 	}
 	
 	
 	public boolean moveTo(Location where, Runnable ondone){
 		return moveTo(where, 0, ondone);
 	}
 	
 	/**
 	 * Defines busy as if stepping or attacking
 	 * @return True if not busy, false otherwise
 	 */
 	public boolean busy(){
 		return stepIdent != null || attackIdent != null;
 	}
 
 	private Object stepIdent;
 	/**
 	 * For stepping to next location
 	 * @param where - moving to
 	 * @param ondone - run this on done
 	 * @param dist - distance
 	 * @param ident - 
 	 */
 	private void step(final Level.Location where, final Runnable ondone, final int dist, final Object ident){
 		if(stepIdent != ident)
 			return;
 		Location l = location();
 		if(!(l instanceof Level.Location))
 			return;
 		if(((Level.Location)l).level().equals(where.level()) && where.dist((Level.Location)l) <= dist){
 			stepIdent = null;
 			if(ondone != null)
 				ondone.run();
 		}else{
 			Level.Location to = ((Level.Location)l).nextTo(where, this, dist);
 			if(to != null){
 				to.put(this);
 				world().schedule(new Runnable(){
 					public void run(){
 						step(where, ondone, dist, ident);
 					}
 				}, attacked()? escapedelay() : walkdelay());
 			}
 		}
 	}
 
 	private GameThing following;
 	// Mm .. can't go: final Runnable tracker = .. and have it reference itself.
 	private Runnable tracker;
 	/**
 	 * Method for character to follow another gameThing (on option on Players or NPC')
 	 * @param g - GameThing to follow
 	 * @param dist - distance
 	 */
 	public void follow(final GameThing g, final int dist){
 		following = g;
 		tracker = new Runnable(){
 			public void run(){
 				if(g == following){
 					Location l = g.location();
 					if(l instanceof Level.Location && moveTo((Level.Location)l, 1, null, true))
 						g.track(tracker);
 				}
 			}
 		};
 		g.track(tracker);
 		tracker.run();
 	}
 
 	public void follow(GameThing g){
 		follow(g, 0);
 	}
 
 	private Object attackIdent;
 	private Runnable attacker;
 	/**
 	 * Method for attacking NPC or other player, can only be done if your health
 	 * is greater than 0, and so is theirs
 	 * Schedules a runnable to face and hurt the victim. Then call follows on the victim
 	 * @param g - GameThing to attack
 	 */
 	public void attack(final GameThing g){
 		if(this.health > 0 && g instanceof Character && ((Character)g).health() > 0 && (location() instanceof
 				Level.Location && g.location() instanceof Level.Location && ((Level.Location)location()).level()==((Level.Location)g.location()).level())){
 			final Character thischar = this;
 			final Object ident = new Object();
 			attackIdent = ident;
 			attacker = new Runnable(){
 				public void run(){
 					if(attackIdent == ident){
 					//	System.out.println(ident);
 						Location l = g.location();
 						if(l instanceof Level.Location){
 							Location ml = location();
 							if(ml instanceof Level.Location && ((Level.Location)l).dist((Level.Location)ml) <= 2){
 								face(l);
 								if((g instanceof Character) && ((Character) g).health() > 0 && thischar.health > 0){
 									animate(renderer() + "_attack");
 									hurt(g);
 								}
 							}
 						}
 						world().schedule(attacker, delay*50);
 					}else if(g instanceof Character)
 						((Character)g).stopAttackedBy(Character.this);
 				}
 			};
 			follow(g, 2);
 			world().schedule(attacker, 500);
 		}
 	}
 	
 	public void stopAttacking(){
 		this.attackIdent = null;
 	}
 	
 	public boolean attacking(){
 		return attackIdent != null;
 	}
 	
 	/**
 	 * Algorithm for facing an object, given their location and checking the
 	 * distance to the surrounding locations.
 	 * @param l - Location that the object in question is
 	 */
 	public void face(Location l){
 		Location ml = location();
 		Level.Location closest = null;
 		for(Direction d : Direction.values()){
 			Level.Location p = ((Level.Location)ml).next(d);
 			if(closest == null || p.dist((Level.Location)l) < ((Level.Location)l).dist(closest))
 				closest = p;
 		}
		location(((Level.Location)ml).direct(closest.direction()));
 	}
 
 	private Set<GameThing> attackedBy = new HashSet<GameThing>();
 	
 	/**
 	 * Algorithm for working out damage done.
 	 * @param other - the GameThing being hurt
 	 */
 	public void hurt(GameThing other){
 		int maxamt = (int) (strength);
 		int minamt = (int) (0.5 * strength);
 		int damageamt = (int) (minamt +  ((Math.random() * attack) *(maxamt - minamt)));
 			((Character)other).damage(damageamt,this);
 		attackedBy.add(other);
 	}
 
 	/**
 	 * 
 	 * @return True if being attacked, false if not
 	 */
 	public boolean attacked(){
 		return !attackedBy.isEmpty();
 	}
 
 	/**
 	 * Removes from List of attacking GameThings
 	 * @param other - GameThing that stops attacking this
 	 */
 	public void stopAttackedBy(GameThing other){
 		attackedBy.remove(other);
 	}
 	
 	/**
 	 * Damages this character, if they're not dying already, and if
 	 * the health goes to 0, calls the dying animation and notification
 	 * @param amt - Amount to be damaged by
 	 * @param from - Attacking character
 	 */
 	public void damage(int amt, Character from){
 		if(!dying()){
 			amt = (int)(amt - (Math.random() * (double)defence));
 			if(amt > 0)
 				health -= amt;
 //			System.out.println(from.name() + " hurts " + name() + " and his health is now " + health);
 			if(health <= 0){
 				animate(renderer() + "_die");
 				world().emitSay(null, null, name() + " has died");
 				dead = true;
 				update();
 				dying(true);
 				attackIdent = null;
 			}
 			update();
 		}
 	}
 
 	//see other moveTo's
 	public boolean moveTo(Level.Location where){
 		return moveTo(where, null);
 	}
 	
 	//see super
 	public void interact(String name, game.things.Player who){
 		super.interact(name, who);
 	}
 	
 	public void dead(boolean d){
 		dead = d;
 	}
 
 	public boolean dying(){ return dying; }
 	public boolean dying(boolean s){ return dying = s; }
 	public int attack(){ return attack; }
 	public int attack(int s){ return attack = s; }
 	public int strength(){ return strength; }
 	public int strength(int s){ return strength = s; }
 	public int defence(){ return defence; }
 	public int defence(int s){ return defence = s; }
 	public int delay(){ return delay; }
 	public int delay(int s){ return delay = s; }
 
 	@Override
 	public int renderLevel() {
 		return ui.isometric.abstractions.IsoSquare.CHARACTER;
 	}
 	
 	/**
 	 * Method to help turn a Character into an DumbGameThing, including health.
 	 */
 	public Map<String, String> info(){
 		if(maxhealth() <= health())
 			return super.info();
 		Map<String, String> out = new HashMap<String, String>(super.info());
 		out.put("health", String.valueOf((double)health/maxhealth));
 		return out;
 	}
 }
