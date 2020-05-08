 package zombiefu.builder;
 
 import jade.util.datatype.ColoredChar;
 import java.util.Set;
 import zombiefu.items.Item;
 import zombiefu.items.Weapon;
 import zombiefu.items.WeaponType;
 import zombiefu.player.Discipline;
 import zombiefu.util.ZombieTools;
 
 /**
  *
  * @author tomas
  */
 public class WeaponBuilder extends ItemBuilder {
 
     private static final int DEFAULT_RANGE = 10;
     private static final double DEFAULT_RADIUS = 3.0;
     private int damage;
     private WeaponType wtyp;
     private double blastRadius;
     private int range;
     private int munition;
     private int dazeTurns;
     private double dazeProbability;
     private Set<Discipline> experts;
 
     public WeaponBuilder(ColoredChar c, String n, int d, WeaponType w, Set<Discipline> experts, int munition, double radius, int range, int dazeTurns, double dazeProbability) {
         this.face = c;
         this.name = n;
         this.damage = d;
         this.wtyp = w;
         this.munition = munition;
         this.blastRadius = radius;
         this.range = range;
         this.experts = experts;
         this.dazeTurns = dazeTurns;
         this.dazeProbability = dazeProbability;
     }
 
     @Override
     public Item buildItem() {
         Weapon w = new Weapon(face, name, damage, wtyp, experts, blastRadius, range, dazeTurns, dazeProbability);
         if (munition == -1) {
             w.setUnlimitedMunition(true);
         } else {
            w.addMunition((int) (munition * ZombieTools.getRandomDouble(1, munition, -3)));
         }
         return w;
     }
 }
