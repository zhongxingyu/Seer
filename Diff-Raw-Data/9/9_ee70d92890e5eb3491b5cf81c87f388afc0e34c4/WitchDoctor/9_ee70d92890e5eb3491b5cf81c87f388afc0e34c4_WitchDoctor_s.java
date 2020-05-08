 package gameObjects.Class.enemyClasses;
 
 import gameObjects.ArmorType;
 import gameObjects.ICharacter;
 import gameObjects.WeaponType;
 import gameObjects.Abilities.SpellMinorHealing;
 import gameObjects.Abilities.SpellRecuperation;
 import gameObjects.Class.GameCharacterClass;
 import gameObjects.Race.enemies.NonPlayerCharacter;
 
 public class WitchDoctor extends GameCharacterClass
 {
   
   public WitchDoctor(ICharacter character)
   {
 	super(character);
     
     //assign name
     this.className = "Witch Doctor";
     
     
    // character.abilities[0] = new SpellMinorHealing(5, 5);
    // character.abilities[1] = new SpellRecuperation(10, 2);
     
   }
   
   
   public int getMaxHP()
   {
    if(character.getHP() - 5 <0)
       return 0;
     return character.getMaxHP() - 5;
   }
   
   public int getMaxMP()
   {
     return character.getMaxMP() + 5;
   }
   
   public int getAccuracy()
   {
     return character.getAccuracy() + 2;
   }
   
   public int getDefense()
   {
     if(character.getDefense() - 5 < 0)
       return 0;
     return character.getDefense() - 5;
   }
   
   public ArmorType getArmorType()
   {
     return ArmorType.HEAVY;
   }
   
   public WeaponType getWeaponType()
   {
     return WeaponType.HEAVY;
   }
   
   @Override
   public String getName()
   {
     return character.getName();
   }
 
 
   @Override
   public String getRace()
   {
     return character.getRace();
   }
 
 
   @Override
   public String getClassName()
   {
     return this.className;
   }
  
 }
