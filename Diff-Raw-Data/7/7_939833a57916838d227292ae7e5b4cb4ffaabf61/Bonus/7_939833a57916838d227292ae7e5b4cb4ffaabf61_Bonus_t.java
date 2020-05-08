 package com.pathfindersdk.bonus;
 
 import com.pathfindersdk.creatures.Creature;
 import com.pathfindersdk.enums.BonusType;
 import com.pathfindersdk.features.Applicable;
 import com.pathfindersdk.stats.Stat;
 
 
 public abstract class Bonus implements Comparable<Bonus>, Applicable<Creature>
 {
   final private int value;
   final private BonusType type;
   final private String circumstance;
 
   public Bonus(int value, BonusType type, String circumstance)
   {
     if(type == null)
       throw new IllegalArgumentException("type can't be null");
     
     this.value = value;
     this.type = type;
     this.circumstance = circumstance;
   }
   
   public int getValue()
   {
     return value;
   }
   
   public BonusType getType()
   {
     return type;
   }
   
   public String getCircumstance()
   {
     return circumstance;
   }
   
   public boolean isCircumstantial()
   {
     return circumstance != null;
   }
   
   @Override
   public int compareTo(Bonus bonus)
   {
     // Sort in descending order (highest bonus first so when bonuses don't stack, the first one is the one to consider)
    return Integer.valueOf(bonus.getValue()).compareTo(Integer.valueOf(getValue()));
   }
 
   protected void applyToStat(Stat stat)
   {
     if(stat == null)
       throw new IllegalArgumentException("stat can't be null");
     
     stat.addBonus(this);
   }
   
   protected void removeFromStat(Stat stat)
   {
     if(stat == null)
       throw new IllegalArgumentException("stat can't be null");
     
     stat.removeBonus(this);
   }
   
   @Override
   public String toString()
   {
     String out = "";
     
     if(value >= 0)
       out += "+";
     
     out += value + " " + type.toString();
     
     if(circumstance != null)
       out += " (" + circumstance + ")";
     
     return out;
   }
   
 }
