 package org.toj.dnd.irctoolkit.map;
 
 import org.toj.dnd.irctoolkit.token.Color;
 
public class MapGridCell {
 
     private Color foreground;
     private Color background;
 
     private String ch;
 
     private boolean blockLineOfSight = false;
     private boolean blockLineOfEffect = false;
     private MapObject objRef;
 
     public MapGridCell(MapModel model) {
         this.foreground = model.getForeground();
         this.background = model.getBackground();
         this.ch = model.getCh();
         this.blockLineOfEffect = model.isBlocksLineOfEffect();
         this.blockLineOfSight = model.isBlocksLineOfSight();
     }
 
     public MapGridCell(MapObject obj) {
         this(obj.getModel());
         this.objRef = obj;
     }
 
     public MapGridCell(Color foreground, Color background, String ch) {
         this.foreground = foreground;
         this.background = background;
         this.ch = ch;
     }
 
     public Color getForeground() {
         return foreground;
     }
 
     public Color getBackground() {
         return background;
     }
 
     public String getCh() {
         return ch;
     }
 
     public void setForeground(Color foreground) {
         this.foreground = foreground;
     }
 
     public void setBackground(Color background) {
         this.background = background;
     }
 
     public void setCh(String ch) {
         this.ch = ch;
     }
 
     public boolean isBlockLineOfSight() {
         return blockLineOfSight;
     }
 
     public void setBlockLineOfSight(boolean blockLineOfSight) {
         this.blockLineOfSight = blockLineOfSight;
     }
 
     public boolean isBlockLineOfEffect() {
         return blockLineOfEffect;
     }
 
     public void setBlockLineOfEffect(boolean blockLineOfEffect) {
         this.blockLineOfEffect = blockLineOfEffect;
     }
 
     @Override
     public String toString() {
         return "[" + foreground + "|" + background + "|" + ch + "]";
     }
 
     //
     // @Override
     // public String toString() {
     // return "MapGridCell [foreground=" + foreground + ", background="
     // + background + ", ch=" + ch + ", blockLineOfSight="
     // + blockLineOfSight + ", blockLineOfEffect=" + blockLineOfEffect
     // + "]";
     // }
 
     public MapObject getObjRef() {
         return objRef;
     }
 
     public void setObjRef(MapObject o) {
         this.objRef = o;
     }
 }
