 package com.github.detro.rps;
 
 import java.util.Random;
 
 import static com.github.detro.rps.Weapons.NO_WEAPON;
 
 /**
  * Contains the list of weapons and the "A vs B" logic.
  *
  * Useful to convert weapon indexes to weapon names, as well as
  * figuring out who wins.
  *
  * TODO Map error cases to different Exceptions: having all "RuntimeException" is very limiting in terms of error handling
  */
 public class Weapons {
     public static final int NO_WEAPON           = -1;
 
     private static final String[] NAMES = new String[] {
         "paper",    //< 0
         "scissors", //< 1
         "rock",     //< 2
         "lizard",   //< 3
         "spock"     //< 4
     };
 
     private static final int[][] WINNERS = new int[][] {
             { NO_WEAPON ,         1 ,         0,         3,         0 },  //< 0 vs: 0, 1, 2, 3, 4
             {         1 , NO_WEAPON ,         2,         1,         4 },  //< 1 vs: 0, 1, 2, 3, 4
             {         0 ,         2 , NO_WEAPON,         2,         4 },  //< 2 vs: 0, 1, 2, 3, 4
             {         3 ,         1 ,         2, NO_WEAPON,         3 },  //< 3 vs: 0, 1, 2, 3, 4
             {         0 ,         4 ,         4,         3, NO_WEAPON }
     };
 
     public static void validateWeaponIdx(int weaponIdx) {
         if (weaponIdx < 0 || weaponIdx > NAMES.length-1) {
             throw new RuntimeException("Invalid Weapon Index: " + weaponIdx);
         }
     }
 
     public static int weaponsAmount() {
         return NAMES.length;
     }
 
     public static String getName(int weaponIdx) {
         validateWeaponIdx(weaponIdx);
         return NAMES[weaponIdx];
     }
 
     public static String[] getNames() {
         return NAMES;
     }
 
     public static int pickRandomWeapon() {
         return new Random().nextInt(weaponsAmount());
     }
 
     /**
      * A vs B
     * @param weaponAIdx
     * @param weaponBIdx
      * @return Returns -1 if it's a draw, otherwise the Index of the Winning Weapon
      */
     public static int vs(int weaponAIdx, int weaponBIdx) {
         validateWeaponIdx(weaponAIdx);
         validateWeaponIdx(weaponBIdx);
         return WINNERS[weaponAIdx][weaponBIdx];
     }
 }
