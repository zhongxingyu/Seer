 package interiores.presentation.swing.views.map;
 
 import interiores.business.models.OrientedRectangle;
 import interiores.business.models.backtracking.FurnitureValue;
 import interiores.business.models.room.elements.WantedFixed;
 import interiores.core.Debug;
 import interiores.core.Utils;
 import interiores.utils.Dimension;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 /**
  * 
  * @author hector
  */
 public class RoomMap
     implements Drawable
 {
     protected static final int RESOLUTION = 5;
     protected static final int PADDING = 10;
     protected static final double SCALE = 1.4;
     
     protected int width;
     protected int depth;
     protected Dimension size; // Total size of the map
     protected Map<String, RoomElement> pillars;
     protected String pillarKey;
     private Map<String, RoomElement> furnitures;   
     protected Walls walls;
     private String status;
     private String time;
     
     
     public RoomMap(int roomWidth, int roomDepth) {
         width = roomWidth + getPadding() * 2;
         depth = roomDepth + getPadding() * 2;
         furnitures = new HashMap();
         pillars = new HashMap();
         walls = new Walls(roomWidth, roomDepth);
         status = "";
         time = "";
         size = new Dimension(0, 0);
     }
     
     public void setSize(java.awt.Dimension size) {
         setSize(new Dimension(size.width, size.height));
     }
     
     public void setSize(Dimension size) {
         this.size = size;
     }
     
     public void clearFurniture() {
         furnitures.clear();
         status = "";
     }
     
     public void clearFixed() {
         walls.clear();
         pillars.clear();
     }
     
     public void clear() {
         clearFurniture();
         clearFixed();
     }
     
     public void addPillar(WantedFixed pillar) {
         OrientedRectangle area = pillar.getActiveArea();
         
         pillars.put(pillar.getName(), new RoomElement(pillar.getName(), area));
     }
     
     public void addFixed(Collection<WantedFixed> fixed) {
         for(WantedFixed wf : fixed) {
             String typeName = wf.getTypeName();
             
             if(typeName.equals("window"))
                 walls.addWindow(wf);
             
             else if(typeName.equals("door"))
                 walls.addDoor(wf);
             
             else if(typeName.equals("pillar"))
                 addPillar(wf);
         }
     }
     
     public void addFurniture(String name, OrientedRectangle area, Color color) {
         Furniture furniture = new Furniture(name, area, color);
         
         furnitures.put(name, furniture);
     }
     
     public void addFurniture(Set<Entry<String, FurnitureValue>> furnitures) {
         for(Entry<String, FurnitureValue> entry : furnitures) {
             String name = entry.getKey();
             Color color = entry.getValue().getModel().getColor();
 
             addFurniture(name, entry.getValue().getArea(), color);
         }
     }
     
     public void removeFurniture(String name) {
         furnitures.remove(name);
     }
     
     public RoomElement getElementAt(int x, int y) {
         for(RoomElement element : furnitures.values()) {
             if(element.contains(new Point(x, y)))
                 return element;
         }
         
         return null;
     }
     
     @Override
     public void draw(Graphics2D g) {
         Debug.println("Redrawing map...");
         
         g.scale(SCALE, SCALE);
         
         g.setColor(Color.white);
         g.fillRect(0, 0, size.width, size.depth);
         
         drawElements(g);
         
         g.setColor(Color.black);
         g.drawString(status, 10, 20);
         g.drawString(time, 10, depth - 20);
     }
     
     protected void drawElements(Graphics2D g)
     {
         drawWalls(g);
         drawFurniture(g);
         drawPillars(g);
     }
     
     protected void drawWalls(Graphics2D g) {
         walls.draw(g);
     }
     
     protected void drawFurniture(Graphics2D g) {
         for(Drawable furniture : furnitures.values())
             furniture.draw(g);
     }
     
     protected void drawPillars(Graphics2D g) {
              
        for(Drawable pillar : pillars.values())
            pillar.draw(g);
         
         if(pillarKey != null) {
             pillars.remove(pillarKey);
             pillarKey = null;
         }
     }
     
     public static int getPadding() {
         return RESOLUTION * PADDING;
     }
     
     public static int getResolution() {
         return RESOLUTION;
     }
     
     public int getWidth() {
         return (int)(width * SCALE);
     }
     
     public int getHeight() {
         return (int)(depth * SCALE);
     }
     
     public void setStatus(String status) {
         this.status = status;
     }
     
     public void setTime(long time) {
         this.time = "Time spent: " + Utils.timeString(time);
     }
 }
