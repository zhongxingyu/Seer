 package textbasedrpg1.spells;
 
 import textbasedrpg1.entity.player.Player;
 import textbasedrpg1.entity.other.Monster;
 
 public class Spells
 {
     public static boolean Fireball(Player player, Monster monster)
     {
         int manaCost = 4;
         
         if (player.getMana() >= manaCost)
                 {
         
                     //Print message
 
                     System.out.println("You threw a fireball!");
 
                     //Do damage
 
                     int damage = (3 * player.getWillpower());
 
                     monster.setHealth(monster.getHealth()-damage);
 
                     System.out.println("It hits the " + monster.getName() + " for " + damage + " damage!");
 
                     //drain mana
 
                     manaCost = 4;
 
                     player.setMana(player.getMana()-manaCost);
                     
                     return true;
                 }
         else 
         {
            return false; 
         }
             
         
     }
     
     public static boolean Heal(Player player, Monster monster)
     {
         int manaCost = 5;
        int healthBefore = player.getHealth();
         
        if (player.getHealth() == player.getMaxHealth())
        {
            System.out.println("You are already at full health!");
            return false;
        }
         
        else if (player.getMana() >= manaCost)
                {
                            
                     //Print message
 
                     System.out.println("You casted a heal on yourself!");
 
                     //Heal
 
                     int heal = (int) Math.ceil((double)(player.getMaxHealth()/5));
 
                     player.setHealth(player.getHealth()+heal);
                    
                     //drain mana
 
                     manaCost = 5;
 
                     player.setMana(player.getMana()-manaCost);
                     
                     if (player.getHealth() > player.getMaxHealth())
                     {
                         player.setHealth(player.getMaxHealth());
                     }
                            
                    int healthAfter = player.getHealth();
                    
                    System.out.println("You healed yourself for " + (healthAfter - healthBefore) + "!");
                     
                     return true;
                 }
        
         else 
         {
            return false; 
         }
             
         
         
     }
 }
