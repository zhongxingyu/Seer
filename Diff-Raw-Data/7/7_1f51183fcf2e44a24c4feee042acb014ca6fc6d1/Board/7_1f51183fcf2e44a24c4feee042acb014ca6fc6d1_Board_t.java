 package bomberman.server;
 
 import bomberman.server.elements.Element;
 import bomberman.server.elements.Wall;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class Board {
 
     private List<Element> elements;
     private List<Integer> fire = new ArrayList();
     private int cols = 15;
     private int rows = 15;
     private int fire_duration = 1000;
     private double probability_breakable_wall = 0.3;
     private double probability_unbreakable_wall = 0.07;
 
     public void generate() {
         this.elements = new ArrayList<Element>();
         int size = this.cols * this.rows;
 
         for (int i = 0; i < size; i++) {
             Element element = null;
 
             if (i <= this.rows || i % this.rows == 0 || (i + 1) % this.rows == 0 || size - this.rows < i) {
                 element = new Wall();
                 element.setBreakable(false);
             } else if (Math.random() < this.probability_breakable_wall) {
                 element = new Wall();
             } else if (Math.random() < this.probability_unbreakable_wall) {
                 element = new Wall();
                 element.setBreakable(false);
             }
 
             if (element != null) {
                 element.setIndex(i);
             }
 
             this.elements.add(element);
         }
     }
 
     public List<Map> getData() {
         List<Map> data = new ArrayList<Map>();
         int size = this.elements.size();
 
         for (int i = 0; i < size; i++) {
             try {
                 data.add(Element.export(this.elements.get(i)));
             } catch (Exception e) {
                 System.out.println(e.getMessage());
             }
         }
         return data;
     }
 
     public int getCols() {
         return this.cols;
     }
 
     public int getRows() {
         return this.rows;
     }
 
     public int getSize() {
         return this.cols * this.rows;
     }
 
     public List<Element> getElements() {
         return elements;
     }
 
     public Element getElement(int index) {
         return elements.get(index);
     }
 
     public void setElement(Element element) {
         this.elements.set(element.getIndex(), element);
     }
 
     public void delElement(int index) {
         this.elements.set(index, null);
     }
 
     public boolean isSquareWalkable(int index) {
         if (index < 0 || index > this.cols * this.rows) {
             return false;
         }
         Element element = this.elements.get(index);
         if (element == null) {
             return true;
         }
         return !element.isActive() || element.isWalkable();
     }
 
     public void addFire(final List<Integer> add_fire) {
         this.fire.addAll(add_fire);
 
         new Thread(new Runnable() {
 
             public void run() {
                 try {
                     Thread.sleep(fire_duration);
                 } catch (InterruptedException e) {
                     System.out.println(e.getMessage());
                 }
                for (Integer i : add_fire) {
                    fire.remove(i);
                 }
             }
         }).start();
     }
 
     public boolean isSquareOnFire(int index) {
         return this.fire.contains(index);
     }
 }
