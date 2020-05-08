 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package zombiefu.player;
 
 import java.util.HashMap;
 
 /**
  *
  * @author tomas
  */
 public enum Discipline {
     //HP, a, v, geschick, geld*100, Bonusitems (Faust + ein Wasser hat jeder sowieso)
    POLITICAL_SCIENCE(100, 5, 6, 7, 500, "weapon(Totquatschen)"), // Was anderes
     COMPUTER_SCIENCE(100, 5, 6, 7, 500, "weapon(Laptop) food(Mate)x4"),
     MEDICINE(130, 4, 6, 6, 1000, "weapon(Narkose) food(Anabolika)x1"),
     PHILOSOPHY(110, 6, 6, 6, 20, "weapon(Totquatschen)"),
     PHYSICS(100, 7, 7, 5, 500, "weapon(Induktionskanone)"),
     BUSINESS(100, 3, 3, 3, 8000, "weapon(Besen) food(MensaAktion)"),
     CHEMISTRY(100, 8, 6, 5, 500, "weapon(Saeurebombe)"),
     SPORTS(110, 9, 5, 4, 500, "weapon(RoundhouseKick) food(Anabolika)x2"),
     MATHEMATICS(120, 6, 4, 7, 500, "food(Kaffee)x5"); // Fehlt
     
     private HashMap<Attribute, Integer> baseAttributes;
     private String baseItems;
     private int baseMoney;
 
     private Discipline(int hp, int att, int def, int dex, int money, String items) {
         baseMoney = money;
         baseItems = items;
         baseAttributes = new HashMap<>();
         baseAttributes.put(Attribute.MAXHP, hp);
         baseAttributes.put(Attribute.ATTACK, att);
         baseAttributes.put(Attribute.DEFENSE, def);
         baseAttributes.put(Attribute.DEXTERITY, dex);
     }
 
     public static Discipline getTypeFromString(String string) {
         for (Discipline d : Discipline.values()) {
             if (d.toString().equals(string)) {
                 return d;
             }
         }
         throw new IllegalArgumentException("Ungültige Discipline-Name: " + string);
     }
 
     public int getBaseAttribute(Attribute att) {
         return baseAttributes.get(att);
     }
     
     public String getItems() {
         return baseItems;
     }
     
     public int getMoney() {
         return baseMoney;
     }
 }
