 package CockRoachCrafter;
 
 import org.powerbot.core.script.job.Task;
 import org.powerbot.core.script.job.state.Node;
 import org.powerbot.game.api.methods.Calculations;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.Widgets;
 import org.powerbot.game.api.methods.input.Mouse;
 import org.powerbot.game.api.methods.interactive.NPCs;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.widget.Camera;
 import org.powerbot.game.api.util.Random;
 import org.powerbot.game.api.wrappers.Area;
 import org.powerbot.game.api.wrappers.Entity;
 import org.powerbot.game.api.wrappers.Locatable;
 import org.powerbot.game.api.wrappers.Tile;
 import org.powerbot.game.api.wrappers.interactive.NPC;
 
 import java.awt.*;
 import java.math.RoundingMode;
 
 public class Attack extends Node {
 
     LootHandler looting = new LootHandler();
     FoodHandler eating = new FoodHandler();
     EquipmentChecker equipment = new EquipmentChecker();
     Rectangle screen = new Rectangle(1,55,518,258);
     Point clickPoint = new Point();
 
     public void altCameraTurnTo(Entity e){
         int x = 0;
         do{
             Camera.setAngle(Camera.getYaw() + Random.nextInt(15, 25));
             x++;
             if(x>=15){
                 break;
             }
         }while(!altIsOnScreen(e));
     }
 
     public boolean altInteract(Entity e, String cmd, String sub){
 
         if(e != null){
            if(!altIsOnScreen(e)){
                 for(Polygon p : e.getBounds()){
                     if(screen.contains(p.getBounds())){
                             clickPoint.setLocation(p.getBounds().x,p.getBounds().y);
                     }
                 }
                e.hover();
                Task.sleep(25,50);
                 if(Widgets.get(548,436).getChild(0) != null && Widgets.get(548, 436).getChild(0) != null){
                     if(Widgets.get(548, 436).getChild(0).getText().contains(cmd) && Widgets.get(548, 436).getChild(0).getText().contains(sub)){
                         Mouse.click(clickPoint, true);
                         Task.sleep(250,500);
                     }else{
                         e.interact(cmd);
                     }
                 }
 
            }else{
                e.interact(cmd);
            }// && Players.getLocal().getInteracting().getInteracting().equals(Players.getLocal().get()
             int x = 0;
             do{
                Task.sleep(250,350);
                 x++;
                CockRoachCrafter.profit = ("Sleeping after click.");
                if(Players.getLocal().getInteracting() != null && Players.getLocal() != null){
                    if(Players.getLocal().getInteracting().getHealthPercent()<=0 || Players.getLocal().isIdle()){
                        return true;
                    }
                 } else if (Players.getLocal().getInteracting() == null){
                    return false;
                }
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
         Area roachArea = new Area(new Tile(3100,4350,1), new Tile(3200,4100,1));   //NEED ROACH AREA TILES
 
         if (NPCs.getNearest(7160) != null && roachArea.contains(Players.getLocal().getLocation())
                 && Players.getLocal().getInteracting()==null && NPCs.getNearest(7160).getHealthPercent()!=0 &&
                 Players.getLocal().getPlane()==1 && Inventory.contains(379)){
             return true;
         }
         return false;
     }
 
     @Override
     public void execute(){
         NPC theRoach = NPCs.getNearest("Cockroach Soldier");
 
         CockRoachCrafter.profit="Attacking Roach.";
 
         ActionBarHandler.momentumCheck();
         if(equipment.activate()){
             equipment.execute();
         }
 
         if(theRoach != null){
             if(Calculations.distanceTo(theRoach)>=4){
                 if(!altIsOnScreen(theRoach)){
                     altCameraTurnTo(theRoach);
                 }
             }
             if(!altIsOnScreen(theRoach)){
                 altCameraTurnTo(theRoach);
             }
             if(Calculations.distanceTo(theRoach)>=8){
                 Walking.walk(theRoach);
             }
             if(!altInteract(theRoach, "Attack", "Cockroach")){
                 Walking.walk(theRoach);
                 altInteract(theRoach, "Attack", "Cockroach");
             }
             altCameraTurnTo(theRoach);
             if(!Players.getLocal().isInCombat()){
                 theRoach.interact("Attack");
             }
         }
 
         if(eating.activate()){
             eating.execute();
         }
 
 
       //  while(looting.activate() && theRoach != null){
       //      looting.execute();
       //  }
 
 
     }
 
 }
