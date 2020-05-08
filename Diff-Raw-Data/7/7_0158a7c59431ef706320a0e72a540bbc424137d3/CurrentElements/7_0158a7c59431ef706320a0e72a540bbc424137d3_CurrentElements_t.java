 package org.dsaw.jaturalquimia.core;
 
 import playn.core.GroupLayer;
 import playn.core.Image;
 import playn.core.ImageLayer;
 import playn.core.Layer;
 
 import static playn.core.PlayN.assets;
 import static playn.core.PlayN.graphics;
 
 public class CurrentElements {
 
     private final GroupLayer layer;
     private final MovableLayer movableLayer;
     private Element element1;
     private Element element2;
 
     public CurrentElements() {
         element1 = new Element();
         element2 = new Element();
        layer = graphics().createGroupLayer(Element.SIZE * 2, Element.SIZE * 2);
         Image bgImage = assets().getImageSync("images/bg.png");
         ImageLayer bgLayer = graphics().createImageLayer(bgImage);
         layer.add(bgLayer);
        layer.addAt(element1.getLayer(), 0, Element.SIZE);
        layer.addAt(element2.getLayer(), Element.SIZE, Element.SIZE);
         layer.setTranslation(150, 70);
         movableLayer = new MovableLayer(layer);
     }
 
     public void rotate() {
         //TODO
         //  element2.moveUp();
     }
 
     public Layer getLayer() {
         return layer;
     }
 
     public void moveToLeft() {
         movableLayer.move(Direction.LEFT);
     }
 
     public void moveToRight() {
         movableLayer.move(Direction.RIGHT);
     }
 
     public void paint(float alpha) {
         movableLayer.paint(alpha);
         //element2.paint(alpha);
         //element1.paint(alpha);
     }
 }
