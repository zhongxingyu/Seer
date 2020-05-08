 package CockRoachCrafter;
 
 import org.powerbot.core.script.job.Task;
 import org.powerbot.core.script.job.state.Node;
 import org.powerbot.game.api.methods.Calculations;
 import org.powerbot.game.api.methods.Tabs;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.Widgets;
 import org.powerbot.game.api.methods.input.Mouse;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.node.GroundItems;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.widget.Camera;
 import org.powerbot.game.api.util.Random;
 import org.powerbot.game.api.wrappers.Entity;
 import org.powerbot.game.api.wrappers.Locatable;
 import org.powerbot.game.api.wrappers.Tile;
 import org.powerbot.game.api.wrappers.interactive.NPC;
 import org.powerbot.game.api.wrappers.node.GroundItem;
 
 import java.awt.*;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 //Carapace = 25551, 25552
 //Dragon Spear = 1249
 //Rune javelin = 830
 //Rune Scimitar = 1333
 //Rune spear = 1247
 //Black full helm = 1165
 //Mithril helm = 1143, 1159
 //Rune sq shield =  1185
 //Blood rune = 565
 //Death rune = 560
 //Fire rune =  554
 //Law rune =  563
 //Adamantite ore Noted = 450
 //Big Bones =  533
 //Clean snapdragon = 3001
 //Clean Torstol = 269
 // Coal = 454
 //Loop half of a key =  987
 //Mithril Ore Noted = 448
 //Pure essence Noted = 7937
 //Raw shark Noted = 384
 //Raw swordfish Noted = 372
 //Shield left half = 2366
 //Tooth half of a key = 985
 //Uncut dragonstone = 1631
 //Vecna skull = 20667
 //Belladonna seed = 5281
 //Bittercap mushroom spore =  5282
 //Cactus seed = 5280
 //Limpwurt seed = 5100
 //Watermelon seed = 5321
 //Dwarf weed seed =  5303
 //Avantoe seed =  5298
 //cadantine seed =5301
 //lantadyme seed = 5302
 //ranarr seed = 5295
 //snapdragon seed = 5300
 //torstol seed = 5304
 //Coins = 995
 public class LootHandler extends Node {
 
     public final int[] LOOT_TABLE = {25551,25552,1249,830,1333,1247,1165,1143,1159,1185,565,560,554,
             563,450,533,3001,269,454,987,448,7937,384,372,2366,985,1631,20667,5281,5282,5280,
             5100,5321,5303,5298,5301,5302,5295,5300,5304,2999,995};
     public static int profit = 0;
 
     CraftingHandler CraftingHandler = new CraftingHandler();
     AlchHandler AlchHandler = new AlchHandler();
     Rectangle screen = new Rectangle(1,55,518,258);
     Point clickPoint = new Point();
     Tile middleTile = new Tile(3168,4277,1);
 
     public static int getPrices(final int id) {
         int price = 0;
         String add = "http://scriptwith.us/api/?return=text&item=";
         add += id;
         try {
             final BufferedReader in = new BufferedReader(new InputStreamReader(
                     new URL(add).openConnection().getInputStream()));
             String line = in.readLine();
             if (line == null) {
                 line = in.readLine();
             }
             String[] subset = line.split("[:]");
             price = Integer.parseInt(subset[1]);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return price;
     }
 
     public void altCameraTurnTo(Entity e){
         do{
             Camera.setAngle(Camera.getYaw() + Random.nextInt(15, 25));
         }while(!altIsOnScreen(e));
     }
 
     public boolean altInteract(Entity e, String cmd, String sub){
 
         if(e != null){
             if(altIsOnScreen(e)){
                 for(Polygon p : e.getBounds()){
                     if(screen.contains(p.getBounds())){
                         clickPoint.setLocation(p.getBounds().x,p.getBounds().y);
                     }
                 }
                 if(Widgets.get(548, 436).getChild(0) != null && Widgets.get(548, 436).getChild(0) != null){
                     if(Widgets.get(548, 436).getChild(0).getText().contains(cmd) && Widgets.get(548, 436).getChild(0).getText().contains(sub)){
                        e.hover();
                         clickPoint.setLocation(e.getCentralPoint());
                        Task.sleep(25,50);
                         Mouse.click(clickPoint, true);
                         Task.sleep(250,500);
                     }else{
                         e.interact(cmd);
                     }
                 }
 
             }        // && Players.getLocal().getInteracting().getInteracting().equals(Players.getLocal().get()
             int x = 0;
             do{
                 Task.sleep(250,350);
                 CockRoachCrafter.profit = ("Sleeping after click.");
                 if(Players.getLocal().getInteracting() != null && Players.getLocal() != null){
                     if(Players.getLocal().getInteracting().getHealthPercent()<=0 || Players.getLocal().isIdle()){
                         return true;
                     }
                 } else if (Players.getLocal().getInteracting() == null){
                     return false;
                 }
                 x++;
                 if(x<=10){
                     break;
                 }
             }while(Players.getLocal().getInteracting().equals(e) ||
                     Players.getLocal().isMoving() || Players.getLocal().isInCombat());
         }
         return false;
     }
 
     public boolean altIsOnScreen(Entity e){
         for(final Polygon p : e.getBounds()){
             if(screen.contains(p.getBounds())){
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public boolean activate(){
         if(!Tabs.getCurrent().equals(Tabs.INVENTORY)){
             Tabs.INVENTORY.open();
         }
         GroundItem Loot = GroundItems.getNearest(LOOT_TABLE);
         if(Inventory.isFull() && Inventory.contains(379) && Inventory.getCount(25551)<2){
             Inventory.getItem(379).getWidgetChild().interact("Eat");
         }
         if(Loot != null && !Inventory.isFull() && Calculations.distance(middleTile, Loot)<=15){
             return true;
         }
         return false;
     }
 
     @Override
     public void execute(){
 
 
         while(activate()){
             GroundItem Loot = GroundItems.getNearest(LOOT_TABLE);
         if(Loot != null){      //If Loot isn't on screen, will walk and turn camera to it.
             if(!altIsOnScreen(Loot)){
                 CockRoachCrafter.profit = "Walking towards Loot";
                 Walking.walk(Loot);
                 CockRoachCrafter.profit = "Grabbing Loot.";
                 Camera.turnTo(Loot);
                 if(Loot.getId()==995){
                     CockRoachCrafter.profit = "Looting " + Loot.getGroundItem().getStackSize() + " coins.";
                     CockRoachCrafter.actualProfit+=Loot.getGroundItem().getStackSize();
                     sleep(800,1000);
                 }
                 if(altInteract(Loot, "Take", Loot.getGroundItem().getName())){
 
                     CockRoachCrafter.actualProfit += getPrices(Loot.getId());
                     sleep(800,1000);
                 }
 
                 while(Players.getLocal().isMoving()){
                     sleep(50,100);
                 }
             } else {
                 if(Calculations.distanceTo(Loot)>=2){
                     CockRoachCrafter.profit = "Turning Camera to Loot.";
                     if(!altIsOnScreen(Loot)){
                         Camera.turnTo(Loot, Random.nextInt(45, 90));
                     }
                 }
                 CockRoachCrafter.profit = "Grabbing Loot.";
                 if(Loot.getId()==995){
                     CockRoachCrafter.actualProfit+=Loot.getGroundItem().getStackSize();
                     sleep(800,1000);
                 }
                 if(altInteract(Loot, "Take", Loot.getGroundItem().getName())){
                      CockRoachCrafter.actualProfit += getPrices(Loot.getId());
                      sleep(800,1000);
                 }
 
                 while(Players.getLocal().isMoving()){
                      sleep(50,100);
                 }
             }
         }
 
         while(AlchHandler.activate()){
             CockRoachCrafter.profit="AlchHandler Activated.";
             AlchHandler.execute();
             Task.sleep(50);
         }
     }
 
     }
 }
