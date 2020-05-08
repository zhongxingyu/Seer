 package memeograph.ui;
 
 import com.sun.jdi.StackFrame;
 import memeograph.HeapObject;
 import memeograph.ui.animation.Animation;
 import com.sun.jdi.ThreadReference;
 import com.sun.jdi.Value;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import javax.media.opengl.GLException;
 import memeograph.DiGraph;
 import memeograph.Graph;
 import memeograph.GraphBuilder;
 import memeograph.StackObject;
 import memeograph.SuperHeader;
 import memeograph.ThreadHeader;
 import memeograph.ui.animation.FadeIn;
 import memeograph.ui.animation.FadeOut;
 import memeograph.ui.animation.MoveAnimation;
 import processing.core.PApplet;
 import processing.core.PFont;
 import processing.core.PVector;
 
 /**
  * Does the drawing and layout. Originally made by extending the moving eye example from processing.
  */
 public class MemeoPApplet extends PApplet implements MouseWheelListener{
     private static final int animationtimeMillis = 3 * 1000;
     static int PADDING = 20;
     static float K = 0.01f; //Spring constant (Along the Y)
     static float M = 0.95f; //Magnet contents (Along the X)
     static float FRICTION = .95f;
     static int MOVE_TICK = 50;
 
     private Map<DiGraph, Node> positions = new HashMap<DiGraph, Node>();
 
     GraphBuilder builder;
     Graph graph;
 
     public enum AnimationType{FADE_IN, FADE_OUT, MOVE, NONE}
     HashMap<AnimationType, List<Animation>> animations;
     private AnimationType currentAnimationState = AnimationType.NONE;
     private int start_time = -1;
     private Runnable animationFinalizer = new Runnable(){public void run(){}};
 
     PFont font;
 
     //Camera Info
     PVector pos;
     PVector dir;
     PVector camNorth = new PVector(0,1,0);
 
     //Text Rendering info
     private final int renderfrontback = 1;
     private final int rendertopbottom = 2;
     private int rendermode = renderfrontback;
 
     public MemeoPApplet(GraphBuilder builder){
         this.builder = builder;
         this.graph = builder.currentGraph();
         addMouseWheelListener(this);
     }
 
 
     @Override
     public void setup(){
         //Full screen, go big or go home!
         try{
            //size(1024, 768, P3D);
            size(1024, 768, OPENGL);
         }catch(GLException gle){
             gle.printStackTrace();
             System.exit(1);
         }
         background(102);
 
         font = createFont("SansSerif.bold", 18);
         textFont(font);
         textAlign(CENTER, CENTER);
 
         //Lets see if we can slow down the stacks rendering to 30fps
         frameRate(25);
         pos = new PVector(width/2.0f, height/2.0f, (height/2.0f) / tan(PI*60.0f / 360.0f));
         dir = new PVector(width/2.0f, height/2.0f, 0);
 
         camera(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, camNorth.x, camNorth.y, camNorth.z);
         smooth();
 
        DiGraph.listener = MemeoPApplet.this;
     }
 
 
     @Override
     public void draw(){
         background(102);
         camera(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, 0, 1, 0);
 
         //Are we currently animating? If so, tick ahead
         if (currentAnimationState != AnimationType.NONE) {
            int elapsed = millis() - start_time;
            if (start_time < 0) {
                //First time running in this animation state
                start_time = millis();
             }else if (elapsed < animationtimeMillis){
                 //Interpolate!
                 for (Animation animation : animations.get(AnimationType.FADE_OUT)) {
                     animation.tick(elapsed/(float)animationtimeMillis);
                 }
             }else /*(elapsed >= animationtimeMillis) is implied here */{
                 //Move onto the next stage
                if (currentAnimationState == AnimationType.FADE_OUT) {
                    currentAnimationState = AnimationType.MOVE;
                }else if (currentAnimationState == AnimationType.MOVE){
                    currentAnimationState = AnimationType.FADE_IN;
                }else if (currentAnimationState == AnimationType.FADE_IN){
                    currentAnimationState = AnimationType.NONE;
                    animationFinalizer.run();
                }
                start_time = -1;
             }
         }
 
         //Now draw the lines between the nodes
         for (Node n : positions.values()) {
             for (DiGraph kid : n.data.getChildren()) {
                 Node knode = positions.get(kid);
                 if (n != null && knode != null)
                     drawLine(n, knode);
             }
         }
 
         //Draw the nodes ontop of the lines. Awesome.
         boolean x = true;
         for (Node n : positions.values()) {
             if (x) {
                 x = false;
             }
             drawNode(n);
         }
 
 
         //Draw the UI
         //Play button
         pushStyle();
         camera(); //Reset the view port and do the 2d drawing
         ellipseMode(CENTER);
         fill(0,255,0);
         ellipse(50, 50, 50, 50);
         fill(0,0,255);
         ellipse(120, 50, 50, 50);
 
         //Make the Text
         fill(0);
         textSize(35);
         textAlign(CENTER);
         text(stepText, 50, 65);
         text(playText, 120, 64);
         popStyle();
     }
 
     String stepText = "S";
     String playText = ">";
     Thread stepThread;
     boolean playing = false;
 
     @Override
     public void mouseClicked()
     {
         float f = dist(mouseX, mouseY, 50, 50);
         float e = dist(mouseX, mouseY, 120, 50);
         if (f < 50) {
             if (stepThread == null) {
                 stepText = "";
                 stepThread = new Thread(){
                     @Override
                     public void run(){
                         step();
                         stepThread = null;
                         stepText = "S";
                     }
                 };
                 stepThread.start();
             }
         }else if (e < 50){
             if (playing) {
                 playing = false;
                 stepThread.interrupt();
                 stepThread = null;
                 playText = ">";
             }else{
                 if (stepThread == null) {
                     playText = "||";
                     playing = true;
                     stepThread = new Thread(){
                         @Override
                         public void run(){
                             while(playing){
                                 step();
                                 try {
                                     Thread.sleep(300);
                                 } catch (InterruptedException ex) {
                                     //I guess someone hit stop!
                                     break;
                                 }
                             }
                             stepThread = null;
                         }
                     };
                     stepThread.start();
                 }else{
                     //If it's not playing, but is also not not null that means
                     //That the step button was pressed and is currently running
                     //Therefore, we shouldn't do anything
                 }
             }
         }
     }
 
     private void step(){
         final Graph newgraph = builder.step();
 
         //Layout the new tree
         SuperHeader newTree = graph.getSuperNode();
         final HashMap<DiGraph, Node> newPositions = new HashMap<DiGraph, Node>();
         layout(newTree, newPositions);
 
         //Find the Nodes that were removed, fade them out
         addFadeOutAnimations(newgraph, positions, newPositions);
 
         //create the fade_anims for moving the nodes to their new position
         addMoveAnimations(newgraph, positions, newPositions);
 
         //find the new Nodes and fade them in
         addFadeInAnimations(newgraph, positions, newPositions);
 
         animationFinalizer = new Runnable(){
             public void run(){
                 graph = newgraph;
                 positions = newPositions;
             }
         };
     }
 
     //Find all of the nodes that need to be faded out
     private void addFadeOutAnimations(Graph newgraph, Map<DiGraph, Node> o, Map<DiGraph, Node> n ) {
         ArrayList<Animation> fade_anims = new ArrayList<Animation>();
 
         //Threads 
         HashSet<ThreadReference> removedThreads = new HashSet<ThreadReference>(graph.threads().keySet());
         removedThreads.removeAll(newgraph.threads().keySet());
 
         for (ThreadReference thread : removedThreads) {
             ThreadHeader threadheader = graph.threads().get(thread);
             fade_anims.add(new FadeOut(o.get(threadheader)));
         }
 
         //Frames
         HashSet<StackFrame> removedFrames = new HashSet<StackFrame>(graph.getStackMap().keySet());
         removedFrames.removeAll(newgraph.getStackMap().keySet());
 
         for (StackFrame sframe : removedFrames) {
             StackObject so = graph.getStackMap().get(sframe);
             fade_anims.add(new FadeOut(o.get(so)));
         }
 
         //Values
         HashSet<Value> removedValues = new HashSet<Value>(graph.getHeapMap().keySet());
         removedValues.removeAll(newgraph.getHeapMap().keySet());
         for (Value value : removedValues) {
             HeapObject ho = graph.getHeapMap().get(value);
             fade_anims.add(new FadeOut(o.get(ho)));
         }
 
         animations.put(AnimationType.FADE_OUT, fade_anims);
     }
 
     private void addMoveAnimations(Graph newgraph, Map<DiGraph, Node> o, Map<DiGraph, Node> n ) {
         ArrayList<Animation> move_anims = new ArrayList<Animation>();
 
         //Threads
         for (ThreadReference tr : graph.threads().keySet()) {
             if (newgraph.threads().containsKey(tr)) {
                 ThreadHeader th = graph.threads().get(tr);
                 Node newNode = n.get(th);
                 move_anims.add(new MoveAnimation(o.get(th), newNode.x, newNode.y));
             }
         }
 
         //Frames
         for (StackFrame stackFrame : graph.getStackMap().keySet()) {
             if (newgraph.getStackMap().containsKey(stackFrame)){
                 StackObject so = graph.getStackMap().get(stackFrame);
                 Node newNode = n.get(so);
                 move_anims.add(new MoveAnimation(o.get(so), newNode.x, newNode.y));
             }
         }
 
         //Values
         for (Value value : graph.getHeapMap().keySet()) {
             if (newgraph.getHeapMap().containsKey(value)) {
                 HeapObject ho = graph.getHeapMap().get(value);
                 Node newNode = n.get(ho);
                 move_anims.add(new MoveAnimation(o.get(ho), newNode.x, newNode.y));
             }
         }
 
         animations.put(AnimationType.MOVE, move_anims);
     }
 
     private void addFadeInAnimations(Graph newgraph, Map<DiGraph, Node> o, Map<DiGraph, Node> n ) {
         ArrayList<Animation> fade_anims = new ArrayList<Animation>();
 
         //Threads
         HashSet<ThreadReference> removedThreads = new HashSet<ThreadReference>(graph.threads().keySet());
         removedThreads.removeAll(newgraph.threads().keySet());
 
         for (ThreadReference thread : removedThreads) {
             ThreadHeader threadheader = graph.threads().get(thread);
             fade_anims.add(new FadeIn(o.get(threadheader)));
         }
 
         //Frames
         HashSet<StackFrame> removedFrames = new HashSet<StackFrame>(graph.getStackMap().keySet());
         removedFrames.removeAll(newgraph.getStackMap().keySet());
 
         for (StackFrame sframe : removedFrames) {
             StackObject so = graph.getStackMap().get(sframe);
             fade_anims.add(new FadeIn(o.get(so)));
         }
 
         //Values
         HashSet<Value> removedValues = new HashSet<Value>(graph.getHeapMap().keySet());
         removedValues.removeAll(newgraph.getHeapMap().keySet());
         for (Value value : removedValues) {
             HeapObject ho = graph.getHeapMap().get(value);
             fade_anims.add(new FadeIn(o.get(ho)));
         }
 
         animations.put(AnimationType.FADE_IN, fade_anims);
     }
 
     private void drawLine(Node from, Node to){
         strokeWeight(5);
         stroke(1f,Math.min(from.opacity, to.opacity));
         line((float)from.x, (float)from.y, (float)from.z,
                 (float)to.x, (float)to.y, (float)to.z);
     }
 
     private void drawNode(Node n){
         pushMatrix();
         translate((float)n.x, (float)n.y, (float)n.z);
 
         fill(n.r, n.g, n.b, n.opacity);
         strokeWeight(1);
         box((float)n.width, 20f, 20f);
 
         float size = 0;
         String data = null;
 
         if ((rendermode & renderfrontback) != 0) {
             data = n.data.name();
             size = textWidth(data);
 
             pushMatrix();
             translate(0f, 0f, 11f);
             fill(5);
             text(data, 0, 0f);
 
             translate(0f, 0f, -22f);
 
             rotateY(PI);
             text(data, 0f, 0f);
             popMatrix();
         }
 
         if ((rendermode & rendertopbottom) != 0) {
             if (data == null){data = n.data.name(); size = textWidth(data);}
 
             translate(0f, 11f, 0f);
             fill(5);
             rotateX(-PI/2);
             text(n.data.name(), -size/2, 0f);
             rotateX(PI/2);
 
             translate(0f, -22f, 0f);
 
             rotateX(-PI/2);
             rotateY(PI);
             textAlign(LEFT);
             text(n.data.name(), -size/2, 0f);
         }
         popMatrix();
     }
 
     private void layout(SuperHeader digraph, Map<DiGraph, Node> map)
     {
         for (DiGraph d : digraph.getThreads()) {
             ThreadHeader thread = (ThreadHeader)d;
             layout(thread, -10, 0, map);
             if (thread.hasFrame() == false) {
                 continue;
             }
 
             StackObject sf = thread.getFrame();
             int y = 0;
             Set<DiGraph> seen = new HashSet<DiGraph>();
             while (sf != null && sf.hasNextFrame()){
                 sf = sf.nextFrame();
                 if (seen.contains(sf)) break;
                 y+=50;
                 layout(sf, -10, y, map);
                 seen.add(sf);
             }
 
         }
 
     }
 
     private void layout(DiGraph t, int z, int y, Map<DiGraph, Node> map)
     {
         if (map.get(t) != null) return;
         Node n = new Node(t, 0, y*50, z*50);
         n.width = textWidth(t.name());
 
         map.put(t, n);
 
         for (DiGraph kid : t.getZChildren()) {
             layout(kid, z-1, y, map);
         }
 
         for (DiGraph kid : t.getYChildren()) {
             layout(kid, z, y+1, map);
         }
 
     }
 
     private float dtheta = .03f;
     @Override
     public void mouseDragged()
     {
         float dy = pmouseY - mouseY;
         if (dy != 0) {
             float y = (pos.y-dir.y);
             float z = (pos.z-dir.z);
             float r = sqrt(y*y + z*z);
             float theta = atan2(y, z);
 
             float theta_new = theta + ((dy > 0) ? dtheta : (-1*dtheta));
             y = sin(theta_new) * r;
             z = cos(theta_new) * r;
             pos.y = dir.y + y;
             pos.z = dir.z + z;
         }
 
         float dx = pmouseX - mouseX;
         if (dx != 0) {
             float x = (pos.x-dir.x);
             float z = (pos.z-dir.z);
             float r = sqrt(x*x + z*z);
             float theta = atan2(z, x);
 
             float theta_new = theta + ((dx < 0) ? dtheta : (-1*dtheta));
             x = cos(theta_new) * r;
             z = sin(theta_new) * r;
             pos.x = dir.x + x;
             pos.z = dir.z + z;
         }
     }
 
     @Override
     public void keyPressed(){
         char k = (char)key;
         switch(k){
             case 'w':
             case 'W': translateCameraY(-MOVE_TICK); break;
             case 's':
             case 'S': translateCameraY(MOVE_TICK); break;
             case 'a':
             case 'A': translateCameraX(-MOVE_TICK); break;
             case 'd':
             case 'D': translateCameraX(MOVE_TICK); break;
             case 't':
             case 'T': toggleRenderMode(); break;
             default: break;
         }
     }
 
     private void translateCameraY(float amount){
         PVector camera = PVector.sub(dir,pos);
         PVector cross = camera.cross(camNorth);
         PVector up = cross.cross(camera);
         up.normalize();
         up.mult(amount);
         pos.add(up);
         dir.add(up);
     }
 
     private void translateCameraX(float amount){
         PVector camera = PVector.sub(dir,pos);
         PVector cross = camera.cross(camNorth);
         cross.normalize();
         cross.mult(amount);
         pos.add(cross);
         dir.add(cross);
     }
 
     private void toggleRenderMode(){
         rendermode = (rendermode + 1) % ((renderfrontback|rendertopbottom) + 1);
     }
 
     @Override
     public void mouseMoved(){
     }
 
     public void mouseWheelMoved(MouseWheelEvent e) {
         int notches = -1*e.getWheelRotation(); //notches goes negative if the
                                             //wheel is scrolled up.
              
         PVector camera = PVector.sub(dir,pos);
         camera.normalize();
         
         pos.add(PVector.mult(camera, (float)notches * 100f));
         dir.add(PVector.mult(camera, (float)notches * 100f));
     }
 
 
 }
