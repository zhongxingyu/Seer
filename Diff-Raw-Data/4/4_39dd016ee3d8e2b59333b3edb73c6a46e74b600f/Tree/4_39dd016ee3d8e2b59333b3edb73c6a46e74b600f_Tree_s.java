
 /**
  * Write a description of class Tree here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Tree extends Block implements Interactable
 {
     private int height;
     
     private AdventureWorld world;
 
     public void interact(String cmd) {
         world.setBlock(loc, new Ground(loc));
         System.out.println("You cut down the tree and got " + (height*5) + " wood");
        world.getPlayer().addToInventory(new Wood(5));
     }
 
     public boolean isInteractCmd(String cmd) {
         return cmd.equalsIgnoreCase("chop tree");
     }
 
     public Tree(int x, int h, AdventureWorld wrld) {
         super(x);
         height = h;
         world = wrld;
     }
 
     public String getName() {
         return "Tree";
     }
     
     public boolean isHidden() {
         return false;
     }
 }
