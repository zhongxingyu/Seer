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
         type = TYPE.SIDE;
     }
 
     public enum TYPE {
         SIDE, TOP;
     }
 
     public void draw() {
         if (type == TYPE.SIDE) {
             System.out.println("Displaying Side View");
             sideView.getData();
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
             display.setText("");
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
             // draw the top lines representing the top of the box
             ArrayList<String[]> drawArray = getData();
             int size = drawArray.size();
 
             write("-----------", size);
             newLine();
 
             for (String[] stackSummary : drawArray) {
                 write("| " + stackSummary[0] + " ");
             }
             write("|");
 
             newLine();
             write("-----------", size);
             newLine();
 
             // draw the totals of the stacks
             for (int i = 0; i < size; i++) {
                 write("|    " + drawArray.get(i)[1] + "     ");
             }
 
             write("|");
             newLine();
             write("-----------", size);
         }
 
         /**
          * 
          */
         @SuppressWarnings("finally")
         public ArrayList<String[]> getData() {
             ArrayList<String[]> drawArray = new ArrayList<String[]>();
             int kind = 0;
             ContainerLabel[] stack;
 
             // An array list which will write the containerlabel(top one) to the
             // first index, and a string which will represent an int to be the
             // count
 
             try {
                 while (true) {
                     stack = inventory.toArray(kind);
                     String[] stackSummary = { "        ", "0" };
 
                    if ((stack.length > 0) && (stack[0] != null)) {

                        System.out.println(stack.toString());
                         // add our top container identifier
                         stackSummary[0] = stack[stack.length - 1].toString();
 
                         // add our count of containers
                         stackSummary[1] = Integer.toString(stack.length);
                     }
 
                     drawArray.add(stackSummary);
 
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
 
         public void draw(ArrayList<ArrayList<String>> passedArray) {
             clear();
             ArrayList<ArrayList<String>> drawArray = passedArray;
             int maxStack = 0;
             int currentRow = 0;
 
             // find the highest stack
             for (ArrayList<String> arrayList : drawArray) {
                 if (arrayList.size() > maxStack) {
                     maxStack = arrayList.size();
                 }
             }
 
             // we are drawing topdown
             currentRow = maxStack;
 
             System.out.print(drawArray.get(0).get(0));
             // draw the top most strings if there are some, then move on through
 
             for (int i = currentRow; i >= 0; i--) {
                 // we start at the top row and go down
                 // if the lists size is equal to or greater than the currentRow
                 // then we have something to draw
 
                 for (int j = 0; j < drawArray.size() ; j++) {
                     if (drawArray.get(j).size() >= i) {
                         // we have something to draw
                         // check out out of bounds
                         write("| ");
                         write(drawArray.get(j).get(i - 1));
                         write(" ");
                     } else {
                         // draw spaces
                         write("           ");
         }
                 }
                 newLine();
 
             }
 
         }
 
         @SuppressWarnings("finally")
         public ArrayList<ArrayList<String>> getData() {
             ArrayList<ArrayList<String>> drawArray = new ArrayList<ArrayList<String>>();
             ContainerLabel[] localLabels;
             int kind = 0;
             try {
 
                 while (true) {
                     ArrayList<String> localDump = new ArrayList<String>();
                     localLabels = inventory.toArray(kind);
 
                     /*
                      * for (ContainerLabel containerLabel : localLabels) { if
                      * (containerLabel != null) {
                      * localDump.add(containerLabel.toString()); } }
                      */
 
                     for (int i = 0; i < localLabels.length; i++) {
                         if (localLabels[i] != null) {
                             localDump.add(localLabels[i].toString());
                         }
                     }
 
                     drawArray.add(localDump);
                     kind++;
                 }
 
             } catch (CargoException e) {
                 System.out.println(drawArray.get(0).get(0));
                 draw(drawArray);
 
             } finally {
                 return drawArray;
             }
         }
     }
 }
