 package org.sankozi.rogueland.model;
 
 import com.google.common.collect.Sets;
 import java.util.Arrays;
 import java.util.EnumMap;
 import java.util.Set;
 import org.apache.log4j.Logger;
 
 /**
  * Human Player
  * @author sankozi
  */
 public class Player extends AbstractActor {
     private final static Logger LOG = Logger.getLogger(Player.class);
 
     private final static Damage closeDamage = new Damage(Damage.Type.BLUNT, 1);
     private final static Damage damage = new Damage(Damage.Type.SLASHING, 10);
 
 	private final static Set<Param> STATS = Sets.newEnumSet(Arrays.asList(
 				Param.AGILITY, 
 				Param.CHARISMA,
 				Param.DEXTERITY,
 				Param.ENDURANCE,
 				Param.INTELLIGENCE,
 				Param.LUCK,
 				Param.STRENGTH,
 				Param.WILLPOWER), 
 			Param.class);
 
    private final EnumMap<Param, Float> params = new EnumMap<Param, Float>(Param.class);
 
     private final Controls controls;
     private Coords location;
 	private Direction weaponDirection = Direction.N;
 
     public Player(Controls controls){
         super(10);
 		for(Param param: STATS){
 			setPlayerParam(param, 10f);
 		}
         this.controls = controls;
         setDestroyableParam(Destroyable.Param.DURABILITY_REGEN, 0.25f);
         setDestroyableParam(Destroyable.Param.MAX_DURABILITY, 20);
     }
 
     @Override
     public Move act(Level input, Locator location) {
         try {
             return controls.waitForMove();
         } catch (InterruptedException ex) {
 			throw new IllegalStateException(ex);
         }
     }
 
 	public enum Param {
 		/* === STATS === */
 		STRENGTH,
 		DEXTERITY,
 		AGILITY,
 		ENDURANCE,
 		INTELLIGENCE,
 		WILLPOWER,
 		CHARISMA,
 		LUCK
 	}
 
 	public final void setPlayerParam(Param param, float value){
 		params.put(param, value);
 	}
 
 	public final float playerParam(Param param){
 		return params.get(param);
 	}
 
 	public Direction getWeaponDirection() {
 		return weaponDirection;
 	}
 
 	@Override
 	public Coords getWeaponLocation(){
 		return new Coords(location.x + weaponDirection.dx, location.y + weaponDirection.dy);
 	}
 
 	public void setWeaponDirection(Direction weaponDirection) {
 		this.weaponDirection = weaponDirection;
 	}
 
     @Override
     public Coords getLocation() {
         return location;
     }
 
     @Override
     public void setLocation(Coords point) {
         this.location = point;
     }
 
     @Override
     public String getName() {
         return "actor/player";
     }
 
     @Override
     public Damage getPower() {
         return closeDamage;
     }
 
 	@Override
 	public Damage getWeaponPower() {
 		return damage;
 	}
 
 	@Override
 	public boolean isArmed() {
 		return true;
 	}
 }
