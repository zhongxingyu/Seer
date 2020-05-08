 /**
  * Write a description of class Player here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 import java.util.*;
 public class Player extends Entity
 {
     public void interact(String cmd){
         cmd = cmd.toLowerCase();
         if(cmd.equals("move left")) this.move(-1);
         else if(cmd.equals("move right")) this.move(1);
         else if(cmd.equals("inspect")) {
             WorldStrip curStrip = world.world.get(new Integer(getLoc()));
             Iterator<Entity> curEntitiesIterator = curStrip.getEntities().iterator();
             Entity curEntity=null;
             boolean locationIsImportant=false;
             if(curStrip.getBlock() instanceof Important && !((Important) curStrip.getBlock()).isHidden())
                 System.out.println("You are standing near a " + ((Important) curEntity).getName());
             while(curEntitiesIterator.hasNext()){
                 curEntity=curEntitiesIterator.next();
                 if(!curEntity.isHidden()) {
                     System.out.println("You are standing near a " + ((Important) curEntity).getName());
                     locationIsImportant = true;
                 }
             }
             if(locationIsImportant) System.out.println("There is nothing of interest around you");
         }
        else if(cmd.equals("inventory")){
            System.out.println("you have:\n"+Arrays.toString(inventory));
         }
     }
 
     public InventoryItem[] inventory = new InventoryItem[15];
 
     public int amntGold=0;
 
     public boolean addToInventory(InventoryItem newItem){
         int firstEmptySlot=-1;
         for(int i=0;i<inventory.length;i++){
             InventoryItem curItem=inventory[i];
             if(curItem==null){
                 firstEmptySlot=i;
                 continue;
             }
             if(curItem.name()==newItem.name()){
                 curItem.setHowMany(curItem.getHowMany()+newItem.getHowMany());
                 return true;
             }
         }
         if(firstEmptySlot>=0){
             inventory[firstEmptySlot]=newItem;
             return true;
         }
         return false;
     }
 
     public boolean removeFromInventory(InventoryItem item){
         for(int i=0;i<inventory.length;i++){
             InventoryItem curItem=inventory[i];
             if(curItem==null) continue;
             if(curItem.name()==item.name()){
                 if(curItem.getHowMany()==item.getHowMany()){
                     inventory[i]=null;
                     return true;
                 }
                 if(curItem.getHowMany()>item.getHowMany()){
                     curItem.setHowMany(curItem.getHowMany()-item.getHowMany());
                     return true;
                 }
             }
         }
         return false;
     }
 
     public boolean isInteractCmd(String cmd) {
         cmd = cmd.toLowerCase();
         if (cmd.equals("move left") || cmd.equals("move right") || cmd.equals("inspect")) return true;
         return false;
     }
 
     public String getName(){
         return "Player";
     }
 
     public Player(AdventureWorld wrld, int x) {
         super(wrld,x);
     }
 
     public boolean move(int i) {
         if(super.move(i)) {
             System.out.println("You moved " + (i < 0 ? "Left" : "Right"));
             return true;
         }
         else {
             System.out.println("You are at the edge of the World");
             return false;
         }
     }
 
     public boolean isHidden() {
         return true;
     }
 }
