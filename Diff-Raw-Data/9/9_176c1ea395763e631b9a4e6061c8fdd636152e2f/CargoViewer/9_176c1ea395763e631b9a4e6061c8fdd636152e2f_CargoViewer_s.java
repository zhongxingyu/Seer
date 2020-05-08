 /**
  * 
  */
 package solution.gui;
 
 import java.util.ArrayList;
 
 import javax.swing.JTextArea;
 
 import solution.CargoException;
 import solution.CargoInventory;
 import solution.ContainerLabel;
 
 /**
  * @author Bodaniel Jeanes, Jacob Evans
  */
 public class CargoViewer {
     private final SideViewer sideView;
     private final TopViewer  topView;
     private TYPE             type;
 
     public CargoViewer(CargoInventory inventory, JTextArea display) {
         sideView = new SideViewer(inventory, display);
         topView = new TopViewer(inventory, display);
         type = TYPE.TOP;
     }
 
     public enum TYPE {
         SIDE, TOP;
     }
 
     public void draw() {
         if (type == TYPE.SIDE) {
             System.out.println("Displaying Side View");
             sideView.draw();
         } else if (type == TYPE.TOP) {
             System.out.println("Displaying Top View");
             topView.draw();
         } else {
             System.out.println("Unknown view type");
         }
     }
 
     public TYPE getType() {
         return type;
     }
 
     public void setType(TYPE type) {
         this.type = type;
     }
 
     protected class Viewer {
         protected final CargoInventory inventory;
         protected final JTextArea      display;
 
         protected Viewer(CargoInventory inventory, JTextArea display) {
             this.inventory = inventory;
             this.display = display;
         }
 
         protected void clear() {
             display.setText(" ");
         }
 
         protected void newLine() {
             write("\n");
         }
 
         protected void write(String text) {
             write(text, 1);
         }
 
         protected void write(String text, int times) {
             for (int i = 0; i < times; ++i) {
                 display.append(text);
             }
         }
     }
 
     protected class TopViewer extends Viewer {
         public TopViewer(CargoInventory inventory, JTextArea display) {
             super(inventory, display);
         }
 
         public void draw() {
             clear();
             //draw the top lines represting the top of the box
             ArrayList<ArrayList<String>> drawArray = getData();
             int size = drawArray.size();
 
             write("-----------", size);
             newLine();
 
             for (ArrayList<String> arrayList : drawArray) {
                 write("| " + arrayList.get(0) + " ");
             }
             write("|");
             
             newLine();
             write("-----------", size);
             newLine();
 
             // draw the totals of the stacks
             for (int i = 0; i < drawArray.size(); i++) {
                 write("|    " + drawArray.get(i).get(1) + "     ");
             }
 
             write("|");
             newLine();
             write("-----------", size);
         }
 
         /**
          * 
          */
         @SuppressWarnings("finally")
         public ArrayList<ArrayList<String>> getData() {
             ArrayList<ArrayList<String>> drawArray = new ArrayList<ArrayList<String>>();
             int kind = 0;
             ContainerLabel[] stack;
 
             // An array list which will write the containerlabel(top one) to the
             // first index, and a string which will represent an int to be the
             // count
 
             try {
                 while (true) {
                     stack = inventory.toArray(kind);
                     ArrayList<String> localArrayListDump = new ArrayList<String>();
                     localArrayListDump.add("0");
                     localArrayListDump.add("0");
                         if (stack[0] != null) {
                             // need to catch empty stacks
                            
                             int objectCount = 0;
                             // find out how many objects we have so we don't hit
                             // some NUllPointers
                             while (stack[objectCount] != null) {
                                 objectCount++;
                                if (objectCount == 5)
                                 {
                                 	break;
                                 }
                             }
                             System.out.println(objectCount);
                             // add our top container
                             localArrayListDump.set(0, stack[objectCount - 1]
                                     .toString());
                             // add our count of containers
                             localArrayListDump.set(1, Integer
                                     .toString(objectCount));
 
                             drawArray.add(localArrayListDump);
 
                         } else {
                             // if the all the containers have been unloaded and
                             // there are spaces
                             localArrayListDump.set(0, "        ");
                             localArrayListDump.set(1,"0");
                             drawArray.add(localArrayListDump);
                         }
                         
                         kind++;
                     
                 }
             } catch (CargoException e) {
                 // catches when we run out of stacks, then we wait for finally
                 // to return the stack
             } finally {
                 return drawArray;
             }
         }
     }
 
     protected class SideViewer extends Viewer {
         public SideViewer(CargoInventory inventory, JTextArea display) {
             super(inventory, display);
         }
 
         public void draw() {
             clear();
         }
     }
 }
