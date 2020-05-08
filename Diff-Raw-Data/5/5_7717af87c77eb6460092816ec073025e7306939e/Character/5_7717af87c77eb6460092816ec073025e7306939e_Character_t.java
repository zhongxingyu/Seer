 import java.util.*;
 
 class Character {
   String name;
   int level;
   int hp;
   int maxHp;
   int spells;
   int maxSpells;
   int exp;
   int toHit;
   
   Weapon weapon;
   
   Random rand = new Random();
   
   
   public Character(String name, int level) {
     this.name = name;
     this.level = level;
    this.exp = 1000 * level;
     this.weapon = new Weapon("Fists", 1, 5, 0);
     
     this.updateCharacter();
   }
   
   
   public void updateCharacter() {
     this.hp = this.maxHp = this.level * 10;
     this.spells = this.maxSpells = this.level * 2;
 
     this.toHit = 15 - (this.level * 2);
   }
   
   
   public boolean isDead() {
     if (hp > 0)
       return false;
     return true;
   }
   
   public boolean isAlive() {
     return !isDead();
   }
   
   
   public void setWeapon(Weapon weapon) {
     this.weapon = weapon;
   }
 
 
   public void attack(Character opponent) {
     int damage; 
   
     if (rand.nextInt(20) + 1 > this.toHit) {
       damage = weapon.use();
       opponent.damage(damage);
       System.out.println(this.name + " hits with " + weapon.toString() + " for " + damage + " damage!" );
     } else {
       System.out.println(this.name + " misses!");
     }
     
   } // end attack
   
   
   public boolean flee() {
     if (rand.nextInt(20) > 9) {
       System.out.println("You flee!");
       return true;
     } 
     
     System.out.println("You are unable to flee!");
     return false;
     
   }
 
   
   public void damage(int hp) {
     this.hp -= hp;
   }
 
   
   public void heal(int hp) {
     this.hp += hp;
   }
   
   // Experience awarded for killing this character
   public int getExp() {
     int expValue = 100;
     return expValue;
   }
   
   
   public void addExp(int exp) {
     System.out.println("You earned " + exp + " experience points!");
     this.exp += exp;
     this.updateCharacter();
   }
   
   
   public void removeExp(int exp) {
     System.out.println("You lost " + exp + " experience points!");
     this.exp -= exp;
     this.updateCharacter();
   }
   
   
   public void castHeal(Character character) {
     int hp = rand.nextInt( 10 * level ) + 1;
     
     if (spells > 0) {
       System.out.println(this.name + " heals " + character.toString() + " for " + hp + " hit points!");
       character.heal(hp);
       spells--;
       return;
     } 
     
     System.out.println(this.name + " is out of spells!");
     
   }
   
   public void status() {
     System.out.println("\n");
     System.out.println("================");
     System.out.println(this.name);
     System.out.println("Hp:  " + this.hp + " / " + this.maxHp);
     System.out.println("Sp:  " + this.spells + " / " + this.maxSpells);
     System.out.println("Lvl: " + this.level); 
    System.out.println("Exp: " + this.exp + " / " + (1000 * (this.level + 1))); 
     System.out.println("================");
     
   }
   
   public String toString() {
     return this.name;
   }
 
 }
