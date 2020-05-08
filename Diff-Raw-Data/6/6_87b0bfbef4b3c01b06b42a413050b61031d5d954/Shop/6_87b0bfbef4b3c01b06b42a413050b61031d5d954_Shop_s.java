 package zombiefu.human;
 
 import zombiefu.util.ZombieGame;
 import jade.util.datatype.ColoredChar;
 import java.util.HashMap;
 import zombiefu.exception.CanNotAffordException;
 import zombiefu.itembuilder.ItemBuilder;
 import zombiefu.player.Player;
 
 public class Shop extends Human {
 
     private HashMap<ItemBuilder, Integer> items;
     String name;
 
     public Shop(ColoredChar face, String name, HashMap<ItemBuilder, Integer> inventar) {
         super(face, name);
         this.items = inventar;
         this.name = name;
     }
 
     @Override
     public void talkToPlayer(Player pl) {
         ZombieGame.newMessage("Willkommen im Mensa-Shop. Hier unser Angebot:");
         ItemBuilder item = ZombieGame.askPlayerForItemToBuy(items);
         try {
            pl.pay(items.get(item));
            pl.obtainItem(item.buildItem());
             ZombieGame.newMessage("Bitte beehren sie uns bald wieder.");
         } catch (CanNotAffordException ex) {
             ZombieGame.newMessage("Das kannst Du Dir nicht leisten.");
         }
     }
 }
