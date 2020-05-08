 /*
  * CONTROLLER INTERACTION
  * ======================
  * 
  * VIEW INTERACTION
  * ================
  * 
  * Click (and drag) starts selection.
  * Shift + Click (and drag) adds to selection.
  * 
  * Right Click and drag rotates the camera
  * Mouse wheel zooms camera
  * 
  * 
  */
 package nodes;
 
 import processing.core.*;
 
 import controlP5.ControlP5;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 //  this is the PeasyCam from https://github.com/jeffg2k/peasycam
 import peasy.PeasyCam;
 import processing.opengl.PGraphics3D;
 
 /**
  * 
  * 
  * @author kdbanman
  */
 public class Nodes extends PApplet {
 
     ControlPanelFrame panelFrame;
     PeasyCam cam;
     ControlP5 cp5;
     UnProjector proj;
     Graph graph;
     
     // mouse information for click and drag selection
     int lastPressedX;
     int lastPressedY;
     boolean leftDragging;
     
     // enum for tracking what the mouse is currently doing.  for management of
     // drag movement and selection, which all use left mouse button
     DragBehaviour drag;
     
     // mouseContent is null when the mouse is hovering over background,
     // and is a reference to a GraphElement when the mouse hovering over one.
     GraphElement mouseContent;
     
     // list for tracking which GraphElements have been hovered over, since
     // ControlP5's native onLeave() calls are based on the assumption that only
     // one controller will be hovered over at any given time
     ArrayList<GraphElement> hovered;
 
     @Override
     public void setup() {
         int w = 800;
         int h = 600;
         size(w, h, P3D);
         frameRate(30);
 
         cam = new PeasyCam(this, 0, 0, 0, 600);
         cam.setLeftDragHandler(null);
         cam.setRightDragHandler(cam.getRotateDragHandler());
         cam.setCenterDragHandler(cam.getZoomDragHandler());
         cam.setWheelHandler(cam.getZoomWheelHandler());
         
         cam.setSpeedLock(false);
         cam.setDamping(.4, .4, .4);
 
 
         // this ControlP5 is only for the Graph, the ControlWindow has its own
         cp5 = new ControlP5(this);
         proj = new UnProjector(this);
         graph = new Graph(proj, cp5, this);
 
         panelFrame = new ControlPanelFrame(graph);
         
         lastPressedX = 0;
         lastPressedY = 0;
         
         drag = DragBehaviour.SELECT;
         
         mouseContent = null;
         
         hovered = new ArrayList<>();
 
         // test data
         /*
         graph.addTriple("John", "knows", "Bill");
         graph.addTriple("John", "worksAt", "Facecloud");
         graph.addTriple("John", "knows", "Amy");
         graph.addTriple("John", "sees", "Amy");
         graph.addTriple("John", "shootsAt", "Amy");
         graph.addTriple("John", "pays", "Amy");
         graph.addTriple("John", "drawsOn", "Amy");
         graph.addTriple("Amy", "hasPet", "John");
         graph.addTriple("Amy", "flies", "WOWOWOWOWWWOOOOOOOtdiuttditdtditidtdiOOOOOOO");
         */
         //graph.addTriples(Importer.getDescriptionFromWeb("Albert_Einstein.rdf"));   
     }
 
     @Override
     public void draw() {
         background(0xFFFFDCBF);
 
         // necessary for unprojection functionality
         proj.captureViewMatrix((PGraphics3D) this.g);
 
         // pretty light
         proj.calculatePickPoints(mouseX, mouseY);
         pointLight(255, 255, 255, proj.ptStartPos.x, proj.ptStartPos.y, proj.ptStartPos.z);
 
         if (leftDragging && drag == DragBehaviour.SELECT) {
             // draw transparent rectangle over selection area
             int minX = min(mouseX, lastPressedX);
             int minY = min(mouseY, lastPressedY);
 
             int maxX = max(mouseX, lastPressedX);
             int maxY = max(mouseY, lastPressedY);
 
             cam.beginHUD();
             fill(0x33333333);
             noStroke();
             rect(minX, minY, maxX - minX, maxY - minY);
             cam.endHUD();
         }
         
         // perform a step of the force-directed layout if the corresponding control
         // is selected
         if (panelFrame.controls.autoLayout.getState()) {
             graph.layout();
         }
         
         cleanHovered();
     }
 
     @Override
     public void mousePressed() {
         // called only when the mouse button is depressed, NOT while it is held
         if (mouseButton == LEFT) {
             lastPressedX = mouseX;
             lastPressedY = mouseY;
         }
     }
 
     @Override
     public void mouseDragged() {
         // called only when the modispHolderuse is moved while a button is depressed
         if (mouseButton == LEFT) {
             
             if (!leftDragging) {
                 if (mouseContent == null) {
                     drag = DragBehaviour.SELECT;
                     if (!shiftPressed()) graph.selection.clear();
                 } else {
                     drag = DragBehaviour.DRAG;
                 }
             }
             leftDragging = true;
 
             if (drag == DragBehaviour.SELECT) {
                 // BOX SELEECT
                 //////////////
                 
                 // determine nodes and edges within the drag box to selection
                 int minX = min(mouseX, lastPressedX);
                 int minY = min(mouseY, lastPressedY);
 
                 int maxX = max(mouseX, lastPressedX);
                 int maxY = max(mouseY, lastPressedY);
 
                 graph.selection.clearBuffer();
                 for (GraphElement n : graph) {
                     PVector nPos = n.getPosition();
                     float nX = screenX(nPos.x, nPos.y, nPos.z);
                     float nY = screenY(nPos.x, nPos.y, nPos.z);
 
                     // test membership of graph element
                     if (nX <= maxX && nX >= minX && nY <= maxY && nY >= minY) {
                         graph.selection.addToBuffer(n);
                     } else {
                         graph.selection.removeFromBuffer(n);
                     }
                 }
             } else {
                 // DRAG MOVE
                 ////////////
                 
                 // get distance between pixels on near frustum
                 proj.calculatePickPoints(0, 0);
                 PVector origin = proj.ptStartPos.get();
                 proj.calculatePickPoints(0, 1);
                 float pixelDist = PVector.dist(origin, proj.ptStartPos);
 
                 // get distance traversed over drag
                 float rawDistH = pixelDist * ((float) (mouseX - pmouseX));
                 float rawDistV = pixelDist * ((float) (mouseY - pmouseY));
 
                 // get near frustum unit vectors
                 PVector horiz = proj.getScreenHoriz();
                 PVector vert = proj.getScreenVert();
 
                 // calculate scale to tranlate from cursor movement to element movement
                 //      (camera to element distance)/(camera to cursor distance)
                 proj.calculatePickPoints(mouseX, mouseY);
                 PVector camPos = getCamPosition();
                 float cursorDist = PVector.dist(proj.ptStartPos, camPos);
                 float elementDist = PVector.dist(mouseContent.getPosition(), camPos);
                 float scale = elementDist / cursorDist;
 
                 // calculate drag vectors
                 horiz.mult(scale * rawDistH);
                 vert.mult(scale * rawDistV);
 
                 // because shift may or may not be held at this point, mouseContent may
                 // or may not be in the selection, so it should be moved separately from
                 // the selection if it is not selected.  single element movement is 
                 // semantically different between nodes and edges (edge position is
                 // derived), so the moveIfNotSelected() is overriden in Edge
                 mouseContent.moveIfNotSelected(horiz, vert);
 
                 // now that element has been moved if unselected, move the selection
                 for (Node n : graph.selection.getNodes()) {
                     n.getPosition().add(horiz);
                     n.getPosition().add(vert);
                 }
             }
         }
     }
 
     @Override
     public void mouseReleased() {
         if (mouseButton == LEFT) {
             if (drag == DragBehaviour.SELECT) {
                 if (!shiftPressed()) {
                     graph.selection.clear();
                } else {
                    if (mouseContent != null) graph.selection.invert(mouseContent);
                 }
                 if (leftDragging) {
                     graph.selection.commitBuffer();
                 }
             }
             leftDragging = false;
             drag = DragBehaviour.SELECT;
         }
     }
 
     public boolean shiftPressed() {
         return keyPressed && key == CODED && keyCode == SHIFT;
     }
     
     public PVector getCamPosition() {
         float[] camPos = cam.getPosition();
         return new PVector(camPos[0], camPos[1], camPos[2]);
     }
 
     public void cleanHovered() {
         Iterator<GraphElement> it = hovered.iterator();
         while (it.hasNext()) {
             GraphElement e = it.next();
             if (!e.isInside()) {
                 e.notHovered();
                 it.remove();
             }
         }
     }
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         PApplet.main(new String[]{nodes.Nodes.class.getName()});
     }
     
     public enum DragBehaviour {
         SELECT, DRAG;
     }
 }
