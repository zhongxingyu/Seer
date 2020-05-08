 /**
  * 
  */
 package data;
 
 /**
  * This class 
  * @author jimiford
  *
  */
 public class PokeUtils {
 
 	/*
 	 * All weaknesses of each type of Pokemon
 	 */
 	private static final Type normal[] = { Type.Fighting };
 	private static final Type fire[] = { Type.Water, Type.Ground, Type.Rock };
 	private static final Type water[] = { Type.Electric, Type.Grass };
 	private static final Type electric[] = { Type.Ground };
 	private static final Type grass[] = { Type.Fire, Type.Ice, Type.Poison, Type.Flying, Type.Bug };
 	private static final Type ice[] = { Type.Fire, Type.Fighting, Type.Rock, Type.Steel };
 	private static final Type fighting[] = { Type.Flying, Type.Psychic };
 	private static final Type poison[] = { Type.Ground, Type.Psychic };
 	private static final Type ground[] = { Type.Water, Type.Grass, Type.Ice };
 	private static final Type flying[] = { Type.Electric, Type.Ice, Type.Rock };
 	private static final Type psychic[] = { Type.Bug, Type.Ghost, Type.Dark };
 	private static final Type bug[] = { Type.Fire, Type.Flying, Type.Rock };
 	private static final Type rock[] = { Type.Water, Type.Grass, Type.Fighting, Type.Ground, Type.Steel };
 	private static final Type ghost[] = { Type.Ghost, Type.Dark };
 	private static final Type dragon[] = { Type.Ice, Type.Dragon };
 	private static final Type dark[] = { Type.Fighting, Type.Bug };
 	private static final Type steel[] = { Type.Fire, Type.Fighting, Type.Ground };
 	
 	private static final int PRIME = 977;
 	private static final int INT = 429857;
 	private static final int MODLENGTH = 1000000;
 	
 	/**
 	 * Hashes password
 	 *
 	 * @param pass
 	 * @return integer
 	 */
 	public static int doPass(char[] pass) {
 		int hashed = 0;
 		for (int i = 0; i < pass.length; i++){
 			hashed = hashed + ((pass[i] * PRIME) + INT);
 		}
 		return hashed % MODLENGTH;
 	}
 	
 	
 	
 	/*
 	 * All the different types of Pokemon
 	 */
 	public enum Type {
 		Normal,
 		Fire,
 		Water,
 		Electric,
 		Grass,
 		Ice,
 		Fighting,
 		Poison,
 		Ground,
 		Flying,
 		Psychic,
 		Bug,
 		Rock,
 		Ghost,
 		Dragon,
 		Dark,
 		Steel
 	}
 	
 	/**
 	 * This returns all the weaknesses of the type that is passed into this function
 	 * @param t the type to calculate the weaknesses of
 	 * @return weaknesses associated with the type t
 	 */
 	public static Type[] getWeakness(Type t) {
 		switch(t) {
 		case Normal:
 			return normal;
 		case Fire:
 			return fire;
 		case Water:
 			return water;
 		case Electric:
 			return electric;
 		case Grass:
 			return grass;
 		case Ice:
 			return ice;
 		case Fighting:
 			return fighting;
 		case Poison:
 			return poison;
 		case Ground:
 			return ground;
 		case Flying:
 			return flying;
 		case Psychic:
 			return psychic;
 		case Bug:
 			return bug;
 		case Rock:
 			return rock;
 		case Ghost:
 			return ghost;
 		case Dragon:
 			return dragon;
 		case Dark:
 			return dark;
 		case Steel:
 			return steel;
 		default:
 			return null;
 		}
 	}
 	
 	
 	/**
 	 * This counts the number of weaknesses of the passed in type
 	 * @param t	the type to count the weaknesses of
 	 * @return	the number of weaknesses associated with type t
 	 */
 	public static int countWeakness(Type t) {
 		switch(t) {
 		case Normal:
 			return normal.length;
 		case Fire:
 			return fire.length;
 		case Water:
 			return water.length;
 		case Electric:
 			return electric.length;
 		case Grass:
 			return grass.length;
 		case Ice:
 			return ice.length;
 		case Fighting:
 			return fighting.length;
 		case Poison:
 			return poison.length;
 		case Ground:
 			return ground.length;
 		case Flying:
 			return flying.length;
 		case Psychic:
 			return psychic.length;
 		case Bug:
 			return bug.length;
 		case Rock:
 			return rock.length;
 		case Ghost:
 			return ghost.length;
 		case Dragon:
 			return dragon.length;
 		case Dark:
 			return dark.length;
 		case Steel:
 			return steel.length;
 		default:
 			return 0;
 		}
 	}
 	
 	public static void main(String[] args) {
 		System.out.println(PokeUtils.doPass(new char[]{'a'}));
 		System.out.println(PokeUtils.doPass(new char[]{'j'}));
 		System.out.println(PokeUtils.doPass(new char[]{'d'}));
 		System.out.println(PokeUtils.doPass(new char[]{'b'}));
 		System.out.println(PokeUtils.doPass(new char[]{'a'}));
 	}
 
 
 	public static boolean equals(char[] password, char[] password2) {
 		boolean same = true;
		if(password.length == password2.length) {
			for(int i = 0; i < password.length && same; i++) {
				same = password[i] == password2[i];
			}
		} else {
			same = false;
 		}
 		return same;
 	}
 }
