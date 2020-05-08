 package net.sozinsoft.tokenlab;
 
 import java.util.*;
 import java.util.regex.Pattern;
 
 public class WeaponImpl {
 
     String name;
     String basicName;
     Damage damage;
     String damageDice;
     String category;
     int critFloor = 100;
     int critMultiplier;
     String equipped;
     int numFullAttacks = 0;
     String weaponType;
     String description;
     int temporaryAttackModifier = 0;
     int temporaryDamageModifier = 0;
 
     private HashMap<Integer, Integer> attacks = new HashMap<Integer,Integer>();
 
     public List<Integer> sortedAttacks() {
         List<Integer> v = new ArrayList<Integer>(attacks.keySet());
         Collections.sort( v );
         return v;
     }
 
     public WeaponImpl(String name, String damage, String category, String crit,
                       String attackBonus, String equipped, String weaponType, String description) throws Exception {
 
 
 
         //sometimes weapon names can look wonky, for example
         //"+1 Adamantine Keen Scimitar, elven Adamantine:\n\nThis ability doubles the threat range of a weapon.
         // Only piercing or slashing"
         //so, strip out everything after the first newline
         //and then the first comma
         this.name = name.replaceAll( "\\n.*$", "");
         this.name = this.name.replaceAll("\\s+$", "" );
         this.name = this.name.replaceAll(",\\s+.*$", "");
 
 
         this.damage = new Damage(damage);
         this.damageDice = this.damage.asExpression();
         this.category = category;
         parseCrit(crit);
         parseAttackBonus(attackBonus);
         numFullAttacks = attacks.size();
         this.equipped = equipped;
         this.weaponType = weaponType;
         this.description = description;
     }
 
 
 
     private void parseCrit(String crit) {
         Pattern regex = Pattern.compile("^(\\d+).*(\\d+)$");
         java.util.regex.Matcher matcher = regex.matcher(crit);
         if (matcher.matches()) {
             critFloor = Integer.parseInt(matcher.group(1));
             critMultiplier = Integer.parseInt(matcher.group(2));
         }
     }
 
     private void parseAttackBonus(String attackBonus) {
         Pattern regex = Pattern.compile("\\d+");
         java.util.regex.Matcher matcher = regex.matcher(attackBonus);
         int count = 1;
         while (matcher.find()) {
             String ab = matcher.group();
            attacks.put(new Integer(count++), Integer.parseInt(ab));
         }
     }
 
 
 }
