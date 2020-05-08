 package roguelike.rpg.sisyphean;
 
 /**
  *  A magic deals arcane damage to the enemy and consumes mana.
  *
  *  @author Artur
  *  @version Nov 3, 2012
  */
 public class Magic
 {
     private String name;
     private String description;
     private float leastAmount;
 
     private float consumption;
     private boolean heals;
 
     // This multiplier will be applied to the player's intelligence to add
     // bonus amount the the final damage/healing.
     private final float BONUS_MULTIPLIER = 0.1f;
 
     /**
      * magic constructor initializes all immutable fields.
      * @param name The name of this magic.
      * @param description The description of this magic.
      * @param leastAmount The flat amount of damage done or hit points healed.
      * @param consumption The amount of mana required to use this magic.
      * @param heals Indicates if the magic heals or does damage.
      */
     public Magic(String name, String description, float leastAmount, float consumption, boolean heals)
     {
         this.name = name;
         this.description = description;
         this.leastAmount = leastAmount;
         this.consumption = consumption;
         this.heals = heals;
     }
 
     /**
      * The name getter.
      * @return The name of this magic.
      */
     public String getName()
     {
         return name;
     }
 
     /**
      * The description getter.
      * @return The description of this magic.
      */
     public String getDescription()
     {
         return description;
     }
 
     /**
      * The consumption getter.
      * @return The mana consumption of this magic.
      */
     public float getConsumption()
     {
         return consumption;
     }
 
    /**
     * Indicates if this magic heals the player or does damage to the enemy.
     * @return True if it heals, false if it does damage.
     */
     public boolean heals()
     {
         return heals;
     }
 
     /**
      * Calculates the total damage/healing done by this magic.
      * Takes into account the player's intelligence.
      * @return the total damage/healing done by this magic.
      */
     public float getTotalEffect(Player player)
     {
         return leastAmount + BONUS_MULTIPLIER * player.getIntelligence();
     }
 }
