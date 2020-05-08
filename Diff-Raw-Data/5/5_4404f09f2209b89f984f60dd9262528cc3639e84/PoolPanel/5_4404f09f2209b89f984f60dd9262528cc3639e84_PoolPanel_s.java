 package pool;
 
 import cameracontrol.CameraController;
 import cameracontrol.ChangeBasis;
 import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.geometry.Text2D;
 import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.PlatformGeometry;
 import com.sun.j3d.utils.universe.SimpleUniverse;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.PriorityQueue;
 import javax.media.j3d.*;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 import javax.vecmath.Color3f;
 import javax.vecmath.Point3d;
 import javax.vecmath.Point3f;
 import javax.vecmath.Vector3f;
 import unbboolean.j3dbool.BooleanModeller;
 import unbboolean.j3dbool.Solid;
 import unbboolean.solids.DefaultCoordinates;
 
 
 public final class PoolPanel extends JPanel implements ActionListener, Comparator, HierarchyBoundsListener {
     
     double pocketSize, railSize, ballSize, borderSize, railIndent, sidePocketSize, sideIndent, pocketDepth;
     boolean selectionMode = false, sliderPower = false, frameSkip = false, err = false;
     PoolBall cueball, shootingBall, ghostBallObjectBall;
     ArrayList<PoolBall>     balls = new ArrayList(16);        
     ArrayList<PoolPocket> pockets = new ArrayList(6);
     ArrayList<PoolPolygon> polygons = new ArrayList(10);
     PriorityQueue<PoolCollision> collisions;
     double height, width;
     double spin, power;
     double spinS = .01, powerS = 2.0;
     int collisionsExecuted = 0;
     int numberOfAimLines = 3;
     private static final float gravity = .005f;
     
     //Java3D
     Canvas3D canvas;
     SimpleUniverse universe;
     PoolCameraController cameraController;
     BranchGroup group;
     
     //Aim
     Shape3D aimLine;    
     LineArray aimLineGeometry;
     Point3d aim;
     RenderingAttributes aimLineRA;
     
     //Ghostball
     TransformGroup ghostBallTransformGroup = new TransformGroup();
     RenderingAttributes ghostBallRA = new RenderingAttributes();
     Sphere ghostBall;
     Vector3f ghostBallPosition;
     Appearance ghostBallAppearance;
     Shape3D ghostBallLine;
     LineArray ghostBallLineGeometry;
     
     //Colors
     Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
     Color3f turqoise = new Color3f(0.0f, .5f, .5f);
     Color3f darkGreen = new Color3f(0.0f, 0.5f, 0.0f);
     Color3f darkBlue = new Color3f(0.0f, 0.0f, 0.5f);
     Color3f darkRed = new Color3f(.5f, 0.0f, 0.0f);
     Color3f tableColor = turqoise;
     Color3f darkerTableColor = new Color3f(tableColor.x*.80f, tableColor.y*.80f, tableColor.z*.80f);
     Color3f darkestTableColor = new Color3f(darkerTableColor.x*.80f, darkerTableColor.y*.80f, darkerTableColor.z*.80f);
     Color3f railColor = darkerTableColor;
     Color3f borderColor = darkestTableColor;
     Color3f pocketColor = darkRed;
     
     
     //--------------------INITIALIZATION--------------------//
     
     public PoolPanel(double bs, double rs, double w, double h) {
         //Initialize size values
         height = h;
         width = w;
         ballSize = bs;
         railSize = rs;
         pocketSize = (2.2*ballSize);
         sidePocketSize = (1.8*ballSize);
         borderSize = pocketSize + .4;
         railIndent = railSize;
         sideIndent = railIndent/4;
         pocketDepth = ballSize*8;                                      
         
         init3D();
         
         //Initialize table items.
         initPockets();
         initPolygons();
         initTable();
         cueball = addBall(-width/4, 0, 0, 0, ballSize);
         shootingBall = cueball;        
         
         //Add listeners.
 	addHierarchyBoundsListener(this);
         
         //Miscellaneous
 	ghostBallPosition = new Vector3f(0,0,0);
         aim = new Point3d(1.0, 0.0, 0.0);
 	collisions = new PriorityQueue(16, this);
        
         //Start timer
 	Timer timer = new Timer(30, this);
 	timer.start();
     }
     
     void init3D() {
         //Initialize Java 3D components.
         canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
         add("Center", canvas);
         universe = new SimpleUniverse(canvas);
         group = new BranchGroup();
         
         //Create the bounding box for the game.
         BoundingBox bounds = new BoundingBox();
         bounds.setLower(-width/2-borderSize, -height/2-borderSize, -3);
         bounds.setUpper(width/2+borderSize, height/2+borderSize, 3);
         
         //Create light sources.
         Color3f lightColor = white;
         Vector3f lightDirection = new Vector3f(4.0f, -7.0f, -12.0f);        
         DirectionalLight light = new DirectionalLight(lightColor, lightDirection);
         light.setInfluencingBounds(bounds);
         group.addChild(light);
         
         Color3f ambientColor = white;        
         AmbientLight ambientLight = new AmbientLight(ambientColor);
         ambientLight.setInfluencingBounds(bounds);
         group.addChild(ambientLight);        
         
         //Add aiming line.
         Appearance appearance = new Appearance();
         ColoringAttributes ca = new ColoringAttributes(white, ColoringAttributes.SHADE_FLAT);
         LineAttributes dashLa = new LineAttributes();
         aimLineRA = new RenderingAttributes();
         dashLa.setLineWidth(1.0f);
         appearance.setColoringAttributes(ca);
         appearance.setLineAttributes(dashLa);
         appearance.setRenderingAttributes(aimLineRA);
         aimLineRA.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
         appearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE);
         //dashLa.setLinePattern(LineAttributes.PATTERN_DASH);
         aimLineGeometry = new LineArray(this.numberOfAimLines*2, LineArray.COORDINATES);
         aimLineGeometry.setCapability(LineArray.ALLOW_COORDINATE_WRITE);
         aimLine = new Shape3D(aimLineGeometry, appearance);
         aimLine.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
         group.addChild(aimLine);
         
         //Add ghost ball.        
         ghostBallAppearance = new Appearance();
         ghostBallRA.setVisible(false);
         ghostBallRA.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
         ghostBallAppearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE);
         ghostBallAppearance.setRenderingAttributes(ghostBallRA);
         ghostBall = new Sphere((float)ballSize, Sphere.ENABLE_APPEARANCE_MODIFY, ghostBallAppearance);
         ghostBallTransformGroup.addChild(ghostBall);
         ghostBallTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
         group.addChild(ghostBallTransformGroup);
         
         //Add ghost ball aiming line
         appearance = new Appearance();
         LineAttributes la = new LineAttributes();
         la.setLineWidth(1.0f);
         appearance.setColoringAttributes(ca);
         appearance.setLineAttributes(la);
         appearance.setRenderingAttributes(ghostBallRA);
         ghostBallLineGeometry = new LineArray(this.numberOfAimLines*2, LineArray.COORDINATES);
         ghostBallLineGeometry.setCoordinate(0, new Point3f(1.0f, 0.0f, 0.0f));
         ghostBallLineGeometry.setCoordinate(1, new Point3f(0.0f, 1.0f, 0.0f));
         ghostBallLine = new Shape3D(ghostBallLineGeometry, appearance);
         ghostBallLineGeometry.setCapability(LineArray.ALLOW_COORDINATE_WRITE);
         ghostBallLine.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
         ghostBallLine.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
         group.addChild(ghostBallLine);
         
         //Finalize 3D setup, initialize camera control.
         universe.addBranchGraph(group);        
         cameraController = new PoolCameraController(this);                      
     }        
     
     void initPolygons() {
         double[] xpoints, ypoints;
 	xpoints = new double[4];
 	ypoints = new double[4];
         
 	xpoints[0] = -width/2 + (pocketSize);
 	xpoints[1] = -width/2 + (pocketSize + railIndent);
 	xpoints[2] = -(sidePocketSize + sideIndent);
 	xpoints[3] = -sidePocketSize;
         
 	ypoints[0] = height/2;
 	ypoints[1] = height/2 - railSize;
 	ypoints[2] = height/2 - railSize;
 	ypoints[3] = height/2;
         this.addPolygon(xpoints, ypoints, 4, railColor, ballSize);
         
         xpoints[0] = (sidePocketSize);
 	xpoints[1] = (sidePocketSize + sideIndent);
 	xpoints[2] = width/2-(pocketSize + railIndent);
 	xpoints[3] = width/2-pocketSize;
         
 	ypoints[0] = height/2;
 	ypoints[1] = height/2 - railSize;
 	ypoints[2] = height/2 - railSize;
 	ypoints[3] = height/2;
         this.addPolygon(xpoints, ypoints, 4, railColor, ballSize);
         
         xpoints[0] = width/2 ;
 	xpoints[1] = width/2 - railSize;
 	xpoints[2] = width/2 - railSize;
 	xpoints[3] = width/2;
         
 	ypoints[0] = height/2 - pocketSize;
 	ypoints[1] = height/2 - (pocketSize + railIndent);
 	ypoints[2] = (pocketSize + railIndent) - height/2;
 	ypoints[3] = pocketSize-height/2;
         this.addPolygon(xpoints, ypoints, 4, railColor, ballSize);                
         
         xpoints[3] = -width/2 + (pocketSize);
 	xpoints[2] = -width/2 + (pocketSize + railIndent);
 	xpoints[1] = -(sidePocketSize + sideIndent);
 	xpoints[0] = -sidePocketSize;
         
 	ypoints[3] = -height/2;
 	ypoints[2] = -height/2 + railSize;
 	ypoints[1] = -height/2 + railSize;
 	ypoints[0] = -height/2;
         this.addPolygon(xpoints, ypoints, 4, railColor, ballSize);
         
         xpoints[3] = (sidePocketSize);
 	xpoints[2] = (sidePocketSize + sideIndent);
 	xpoints[1] = width/2-(pocketSize + railIndent);
 	xpoints[0] = width/2-pocketSize;
         
 	ypoints[3] = -height/2;
 	ypoints[2] = -height/2 + railSize;
 	ypoints[1] = -height/2 + railSize;
 	ypoints[0] = -height/2;
         this.addPolygon(xpoints, ypoints, 4, railColor, ballSize);
         
         xpoints[3] = -width/2 ;
 	xpoints[2] = -width/2 + railSize;
 	xpoints[1] = -width/2 + railSize;
 	xpoints[0] = -width/2;
         
 	ypoints[3] = height/2 - pocketSize;
 	ypoints[2] = height/2 - (pocketSize + railIndent);
 	ypoints[1] = (pocketSize + railIndent) - height/2;
 	ypoints[0] = pocketSize-height/2;
         this.addPolygon(xpoints, ypoints, 4, railColor, ballSize);
     }
 
     void initPockets() {
         Iterator<PoolPocket> iter;
         pockets.add(new PoolPocket(width/2,  height/2,  pocketSize,     (float)pocketDepth, (float)ballSize, pocketColor));
         pockets.add(new PoolPocket(-width/2, height/2,  pocketSize,     (float)pocketDepth, (float)ballSize, pocketColor));
         pockets.add(new PoolPocket(0.0,      height/2,  sidePocketSize, (float)pocketDepth, (float)ballSize, pocketColor));        
         pockets.add(new PoolPocket(width/2,  -height/2, pocketSize,     (float)pocketDepth, (float)ballSize, pocketColor));
 	pockets.add(new PoolPocket(-width/2, -height/2, pocketSize,     (float)pocketDepth, (float)ballSize, pocketColor));
         pockets.add(new PoolPocket(0.0,      -height/2, sidePocketSize, (float)pocketDepth, (float)ballSize, pocketColor));        
         iter = pockets.iterator();
         while(iter.hasNext()) {
             PoolPocket pocket = iter.next();
             universe.addBranchGraph(pocket.group);
         }
     }
     
     private void initTable() {
         PoolPocket pocket;
         BooleanModeller bm;        
         BranchGroup borderGroup = new BranchGroup();
         Solid solid1 = new Solid();
         Solid solid2 = new Solid();        
         
         //Top border
         solid1.setData(DefaultCoordinates.DEFAULT_BOX_VERTICES, DefaultCoordinates.DEFAULT_BOX_COORDINATES, borderColor);
         solid1.scale(width + 2*borderSize, borderSize, ballSize*2);
         
         //Bottom
         solid2.setData(DefaultCoordinates.DEFAULT_BOX_VERTICES, DefaultCoordinates.DEFAULT_BOX_COORDINATES, borderColor);
         solid2.scale(width + 2*borderSize, borderSize, ballSize*2);
 
         
         solid1.translate(0, height/2 + borderSize/2);
         solid2.translate(0, -(height/2 + borderSize/2));
         bm = new BooleanModeller(solid1, solid2);
         solid1 = bm.getUnion();
         
         //Left
         solid2.setData(DefaultCoordinates.DEFAULT_BOX_VERTICES, DefaultCoordinates.DEFAULT_BOX_COORDINATES, borderColor);
         solid2.scale(borderSize, height, ballSize*2);
         solid2.translate(-(width/2 + borderSize/2) ,0);
         
         bm = new BooleanModeller(solid1, solid2);
         solid1 = bm.getUnion();
         
         solid2.setData(DefaultCoordinates.DEFAULT_BOX_VERTICES, DefaultCoordinates.DEFAULT_BOX_COORDINATES, borderColor);
         solid2.scale(borderSize, height, ballSize*2);
         solid2.translate(width/2 + borderSize/2,0);
         
         bm = new BooleanModeller(solid1, solid2);
         solid1 = bm.getUnion();
         
         //Table
         solid2.setData(DefaultCoordinates.DEFAULT_BOX_VERTICES, DefaultCoordinates.DEFAULT_BOX_COORDINATES, tableColor);
         solid2.scale(width, height, ballSize/2);
         
         for(int i = 0; i < 6; i++) {            
             pocket = pockets.get(i);
             pocket.inner.translate(pocket.pos.x, pocket.pos.y);
             pocket.inner.rotate(Math.PI/2, 0);
             bm = new BooleanModeller(solid1, pocket.inner);
             solid1 = bm.getDifference();
         }
         
         /*
         Solid solid3 = new Solid();
         solid3.setData(DefaultCoordinates.DEFAULT_BOX_VERTICES, DefaultCoordinates.DEFAULT_BOX_COORDINATES, darkBlue);
                 
         solid2.setData(DefaultCoordinates.DEFAULT_CYLINDER_VERTICES, DefaultCoordinates.DEFAULT_CYLINDER_COORDINATES, darkBlue);
         solid2.rotate(Math.PI/2, 0);
         
         bm = new BooleanModeller(solid3, solid2);
         solid3 = bm.getDifference();
         //solid3.translate(0,2);
         borderGroup.addChild(solid3);
         //solid2.scale(ballSize, ballSize, 2*ballSize);
         
         //solid2.translate(width/2+borderSize-ballSize, height/2+borderSize-ballSize);
         //bm = new BooleanModeller(solid1, solid2);        */
         
         borderGroup.addChild(solid1);
         
         //Table
         TransformGroup tg = new TransformGroup();
         Vector3f shift = new Vector3f(0.0f, 0.0f, (float)-ballSize*3/2);
         Transform3D transform = new Transform3D();
         transform.setTranslation(shift);
         tg.setTransform(transform);
         solid2.setData(DefaultCoordinates.DEFAULT_BOX_VERTICES, DefaultCoordinates.DEFAULT_BOX_COORDINATES, tableColor);
         solid2.scale(width, height, ballSize);        
         for(int i = 0; i < 6; i++) {            
             pocket = pockets.get(i);
             bm = new BooleanModeller(solid2, pocket.inner);
             solid2 = bm.getDifference();
         }
         tg.addChild(solid2);
         borderGroup.addChild(tg);
         universe.addBranchGraph(borderGroup);
         
     }
     
     //--------------------SIMULATION--------------------//
     
     public void actionPerformed(ActionEvent evt){
         //this.repaint();
         doAim();
 	Iterator<PoolBall> iter;
 	iter = balls.iterator();
         validate();
         if(err) {
             rewind();
             frameSkip = true;
         }
         if(frameSkip) {
             err = false;
         }
 	while(iter.hasNext()) {
 	    PoolBall ball = iter.next();
             
             //For error handling           
             ball.lpos.set(ball.pos);
             ball.lvel.set(ball.vel);
             
             detectPolygonCollisions(ball, 0);
 	    detectPocketCollisions(ball, 0);
 	    for(int i = balls.lastIndexOf(ball)+1; i < balls.size(); i++) {
 		double t = ball.detectCollisionWith(balls.get(i));
 		if(t < 1 && 0 <= t){
 		    collisions.add(new BallCollision(t, ball, balls.get(i)));
 		}
 	    }            
 	}
         updateBallPositions();
         updateGhostBall();
     }
     
     void updateBallPositions(){
 	Iterator<PoolBall> ballIterator;
 	PoolCollision collision = collisions.poll();
 	double timePassed = 0;
         while(collision != null) {
 	    //Advance balls to the point where the collision occurs.
             ballIterator = balls.iterator();
 	    while(ballIterator.hasNext()) {
 		PoolBall ball = ballIterator.next();
 		ball.move(collision.time-timePassed);
                 
             }
             timePassed = collision.time;
 	    
             collision.doCollision(this);
             collisionsExecuted++;
 	    collision = collisions.poll();	    
 	}
         ballIterator = balls.iterator();
 	while(ballIterator.hasNext()) {
 	    PoolBall ball = ballIterator.next();
 	    ball.move(1-timePassed);
             ball.vel.x += ball.acc.x;
             ball.vel.y += ball.acc.y;
             if(ball.vel.length() < .001) {
                 ball.vel.x = 0;
                 ball.vel.y = 0;
             } else {
                 ball.vel.x = ball.vel.x*.99;
                 ball.vel.y = ball.vel.y*.99;
             }
             if(ball.acc.distance(0,0) < .01) {
                 ball.acc.x = 0;
                 ball.acc.y = 0;
             } else {
                 ball.acc.x = .98*ball.acc.x;
                 ball.acc.y = .98*ball.acc.y;
             }
             
 	}
         
         ballIterator = balls.iterator();        
         while(ballIterator.hasNext()) {
             Iterator<PoolPocket> pocketIterator = pockets.iterator();
             PoolBall ball = ballIterator.next();
             //checkOverlaps(ball);
             while(pocketIterator.hasNext()) {
                 PoolPocket pocket = pocketIterator.next();
                 if(pocket.ballIsOver(ball)) {
                     this.doGravity(ball, pocket);
                 }
             }
             
         }
     }
     
     void updateGhostBall() {
 	Iterator<PoolBall> iter;
         double min = Double.POSITIVE_INFINITY;
 	iter = balls.iterator();
 	ghostBallObjectBall = null;
 	while(iter.hasNext()) {
 	    PoolBall ball = iter.next();
 	    if(ball != shootingBall){
                 double t;
 		double a = aim.x*aim.x + aim.y*aim.y;
                 
 		double b = 2*-aim.x*shootingBall.pos.x + 2*-aim.y*shootingBall.pos.y -
                            2*-aim.x*ball.pos.x         - 2*-aim.y*ball.pos.y         ;
                 
 		double c = ball.pos.y*ball.pos.y                 + ball.pos.x*ball.pos.x                 +
                            shootingBall.pos.y*shootingBall.pos.y + shootingBall.pos.x*shootingBall.pos.x -
                            2*(shootingBall.pos.y*ball.pos.y      + shootingBall.pos.x*ball.pos.x)        -
                            (ball.size + shootingBall.size)*(ball.size + shootingBall.size);
                 
 		if (a !=0 ){
 		    double t1 = ( -b - Math.sqrt(b*b-4*a*c) )/(2 * a);
 		    double t2 = ( -b + Math.sqrt(b*b-4*a*c) )/(2 * a);
 		    t = t1 < t2 ? t1 : t2;
 		} else {
 		    t = -c/b;  
 		}
 
 		if( !(Double.isNaN(t)) && t < min && t > 0){
 		    ghostBallPosition.set((float)(shootingBall.pos.x + t * -aim.x),
                                           (float)(shootingBall.pos.y + t * -aim.y),
                                           0.0f);
 		    ghostBallObjectBall = ball;
 		    min = t;
 		}
 	    }
 	}
     }
     
     void doGravity(PoolBall ball, PoolPocket pocket) {
         if(ball.pos.z <= (-pocketDepth+ballSize)) {
             ball.vel.set(0.0,0.0,0.0);
             ball.sunk = true;
             return;
         }
         Vector3f acceleration = new Vector3f((float)(ball.pos.x - pocket.pos.x),
                                            (float)(ball.pos.y - pocket.pos.y),
                                            0.0f);
         acceleration.normalize();
         acceleration.scale(pocket.size);        
         Point3f contactPoint = new Point3f((float)pocket.pos.x, (float)pocket.pos.y, (float)-ballSize);
         contactPoint.add(acceleration);        
         if(ball.pos.distance(new Point3d(contactPoint)) <= ball.size) {
             Vector3f normal = new Vector3f(ball.pos);
             contactPoint.scale(-1);
             normal.add(contactPoint);
             normal.normalize();
             float scale = 1/normal.z;
             ball.vel.x += normal.x*scale * gravity;
             ball.vel.y += normal.y*scale * gravity;
         } else {
             ball.vel.z -= gravity;
         }                
     }
     
     //--------------------COLLISION DETECTION--------------------//
     
     void detectPolygonCollisions(PoolBall ball, double t) {
         Iterator<PoolPolygon> iter = polygons.iterator();
         while(iter.hasNext()) {
             PoolPolygon p = iter.next();
             p.detectCollisions(ball, collisions, t);
         }
     }
     
     //This version ignores the wall of the collision
     void detectPolygonCollisions(PoolBall ball, double t, WallCollision collision) {
         Iterator<PoolPolygon> iter = polygons.iterator();
         while(iter.hasNext()) {
             PoolPolygon p = iter.next();
             if(p == collision.poly) {
                 p.detectCollisionsWithoutWall(ball, collisions, t, collision.wall);
             } else {
                 p.detectCollisions(ball, collisions, t);
             }
         }
     }
     
     void detectPocketCollisions(PoolBall ball, double timePassed) {
 	double time;
 	Iterator<PoolPocket> pocketItr;
 	pocketItr = pockets.iterator();
         while(pocketItr.hasNext()) {
             PoolPocket pocket = pocketItr.next();            
             time = pocket.detectCollisionWith(ball);
             time += timePassed;
             if(time >= timePassed && time < 1) {
                 collisions.add(new PocketCollision(ball, time, pocket));
                 return;
             }            
 	}
     }
     
     //--------------------GRAPHICAL FUNCTIONS--------------------//
     
     @Override public void paintComponent(Graphics g){
         
         /*super.paintComponent(g);
         g.drawString(Float.toString(cameraController.cameraTranslation.x), 600, getHeight()-20);
         g.drawString(Float.toString(cameraController.cameraTranslation.y), 700, getHeight()-20);
         g.drawString(Float.toString(cameraController.cameraTranslation.z), 800, getHeight()-20);
         * */
         /*
         super.paintComponent(g);
         g.drawString(Float.toString(cameraController.cameraPosition.x), 0, getHeight()-20);
          g.drawString(Float.toString(cameraController.cameraPosition.y), 100, getHeight()-20);
          g.drawString(Float.toString(cameraController.cameraPosition.z), 200, getHeight()-20);
          */
                  /*
         g.setColor(Color.BLACK);
         g.drawString(Float.toString(cueball.rotation.x), 0, getHeight()-20);
          g.drawString(Float.toString(cueball.rotation.y), 100, getHeight()-20);
          g.drawString(Float.toString(cueball.rotation.z), 200, getHeight()-20);*/
         /*
          * This was removed for performance reasons.
          * super.paintComponent(g);
          g.setColor(Color.BLACK);
           g.drawString(Float.toString(cameraController.cameraPosition.x), 0, getHeight()-20);
          g.drawString(Float.toString(cameraController.cameraPosition.y), 100, getHeight()-20);
          g.drawString(Float.toString(cameraController.cameraPosition.z), 200, getHeight()-20);
          * 
          * g.drawString(Double.toString(cameraController.cameraPos.x), 0, getHeight());
          * g.drawString(Double.toString(cameraController.cameraPos.y), 100, getHeight());
          * g.drawString(Double.toString(cameraController.cameraPos.z), 200, getHeight());
          * 
          * g.drawString(Float.toString(cameraController.upVector.x), 300, getHeight()-20);
          * g.drawString(Float.toString(cameraController.upVector.y), 400, getHeight()-20);
          * g.drawString(Float.toString(cameraController.upVector.z), 500, getHeight()-20);
          * 
          * g.drawString(Double.toString(cameraController.upVec.x), 300, getHeight());
          * g.drawString(Double.toString(cameraController.upVec.y), 400, getHeight());
          * g.drawString(Double.toString(cameraController.upVec.z), 500, getHeight());
          * 
          * g.drawString(Float.toString(cameraController.cameraTranslation.x), 600, getHeight()-20);
          * g.drawString(Float.toString(cameraController.cameraTranslation.y), 700, getHeight()-20);
          * g.drawString(Float.toString(cameraController.cameraTranslation.z), 800, getHeight()-20);
          * 
          * g.drawString(Double.toString(cameraController.cameraTrans.x), 600, getHeight());
          * g.drawString(Double.toString(cameraController.cameraTrans.y), 700, getHeight());
          * g.drawString(Double.toString(cameraController.cameraTrans.z), 800, getHeight());
          */                        
     }
     
     void doAim() {        
         if(shootingBall != null && shootingBall.vel.length() < .01) {
             aimLineRA.setVisible(true);
             if(ghostBallObjectBall == null) {
                 Vector3f unit = new Vector3f();
                 unit.set(aim);
                 unit.scale(-1f);
                 unit.normalize();
                 Point3f start = new Point3f(shootingBall.pos);
                 drawPoolPath(unit, start, numberOfAimLines, aimLineGeometry,0);
                 ghostBallRA.setVisible(false);
             } else {
                 //Set the ghost ball to be visible.
                 ghostBallRA.setVisible(true);
                 
                 //Set the first line to be a line from the shooting ball to the location of the ghost ball.
                 aimLineGeometry.setCoordinate(0, shootingBall.pos);
                 aimLineGeometry.setCoordinate(1, new Point3f(ghostBallPosition));
                 
                 //Determine the unit vectors that describe the trajectory of the object ball.
                 Vector3f unit = new Vector3f();
                 unit.set((float)(ghostBallObjectBall.pos.x - ghostBallPosition.x),
                          (float)(ghostBallObjectBall.pos.y - ghostBallPosition.y),
                          0.0f);               
                 unit.normalize();
                 //Store unit in shootingBallUnit as it will get overwritten by drawPoolPath.
 
                 Vector3f shootingBallUnit = new Vector3f(unit);
                 
                 //Draw the trajectory of the object ball.
                 Point3f start = new Point3f(ghostBallObjectBall.pos);
                 drawPoolPath(unit, start, numberOfAimLines, ghostBallLineGeometry, 0);                                
                 
                 //Determine which of the two perpendicular directions the shooting ball will travel in.
                 unit.set(shootingBallUnit);
                 shootingBallUnit.set(unit.y, -unit.x, unit.z);
                 Vector3f temp = new Vector3f((float)(shootingBall.pos.x - ghostBallPosition.x),
                                              (float)(shootingBall.pos.y - ghostBallPosition.y),
                                              0.0f);
                 float angle = Math.abs(shootingBallUnit.angle(temp));
                 if(angle < Math.PI/2) {
                     shootingBallUnit.scale(-1f);
                 }               
                 
                 //Draw the path of the shooting ball.
                 start.set(ghostBallPosition);               
                 drawPoolPath(shootingBallUnit, start, numberOfAimLines, aimLineGeometry, 1);
                 Transform3D transform = new Transform3D();
                 transform.setTranslation(ghostBallPosition);
                 ghostBallTransformGroup.setTransform(transform);
             }
         }            
     }
     
     void drawPoolPath(Vector3f unit, Point3f last, int numLines, LineArray array, int offset) {
         Point3d next = new Point3d();
         double horizontal, vertical;
         for(int i = offset; i < numLines; i++) {
             horizontal = Double.POSITIVE_INFINITY;
             vertical = Double.POSITIVE_INFINITY;
             
             if(unit.x != 0) {
                 horizontal = (width/2 - (railSize + ballSize) - last.x)/unit.x;
                 if(horizontal <= .00001) {
                     horizontal = ((railSize + ballSize) - width/2 - last.x)/unit.x;
                 }
             }
             if(unit.y != 0) {
                 vertical = (height/2 - (railSize + ballSize) - last.y)/unit.y;
                 if(vertical <= .00001) {
                     vertical = ((railSize + ballSize) - height/2 - last.y)/unit.y;
                 }
             }
             if(vertical < horizontal && vertical > 0) {
                 next.set((last.x + unit.x*vertical), (last.y+unit.y*vertical), 0);                
                 unit.y = -unit.y;
             } else {
                 next.set((last.x+unit.x*horizontal), (last.y+unit.y*horizontal), 0);
                 unit.x = -unit.x;
             }
             array.setCoordinate(2*i, last);
             array.setCoordinate(2*i + 1, next);
             last.set(next);
         }        
     }
     
     //--------------------ACTIONS--------------------//
     
     public void newRack() {        
         cueball.pos.set(-width/4, 0, 0);
         cueball.vel.set(0.0,0.0,0.0);
         cueball.move(0.0);
         aim.x = -1.0;
         aim.y = 0.0;
         doAim();
         this.cameraController.overheadView();
         double x = width/8;
         for(int i = 0; i < 5; i++) {
             double y = -i*ballSize + .01;
             for(int j = 0; j <= i; j++) {
                 if(j == 1 && i == 2) {
 
                 } else if((i+j)%2 == 0) {
                     this.addBall(x, y, 0, 0, ballSize);
                 } else {
                     this.addBall(x, y, 0, 0, ballSize);
                 }
                 y += 2*ballSize; 
             }
             x += 2*ballSize*Math.sqrt(3)/2+.01;
         }                       
     }
      
     public void shoot() {
         shootingBall.vel.x = -aim.x * power * powerS;
         shootingBall.vel.y = -aim.y * power * powerS;
         shootingBall.acc.x = -aim.x * spin * spinS;
         shootingBall.acc.y = -aim.y * spin * spinS;
         ghostBallRA.setVisible(false);
         aimLineRA.setVisible(false);
     }
     
     public void setAim(double x, double y) {
         aim.x = x;
         aim.y = y;
     }
     
     public void setSpin(double v) {
         spin = v;
     }
     
     public void setPower(double p) {
         power = p;
     }
     
     public void flipSelectionMode() {
         selectionMode = !selectionMode;
     }
     
     public void setSelectionMode(boolean v) {
         selectionMode = v;
     }
     
     public PoolBall addBall(double x, double y, double a, double b, double s) {
         Texture texImage = new TextureLoader("1.jpg",this).getTexture();        
         Appearance appearance = new Appearance();
         appearance.setTexture(texImage);
         PoolBall ball = new PoolBall(appearance, x, y, a, b, s);
         universe.addBranchGraph(ball.group);        
         balls.add(ball);
         return ball;
     }
     
     public PoolPolygon addPolygon(double[] xpoints, double[] ypoints, int npoints,
             Color3f c, double ballsize) {
         PoolPolygon poly = new PoolPolygon(xpoints, ypoints, npoints, c, ballsize);
         universe.addBranchGraph(poly.group);
         polygons.add(poly);
         return poly;
         
     }
     
     public void rewind() {
        Iterator<PoolBall> iter = balls.iterator();
         while(iter.hasNext()) {
 	    PoolBall ball = iter.next();
             ball.pos.set(ball.lpos);
             ball.vel.set(ball.lvel);
         }                
     }
 
     //--------------------COMPARATOR INTERFACE--------------------//
     
     public int compare(Object a, Object b) {
 	double val =  ((PoolCollision)a).time - ((PoolCollision)b).time;
 	if(val < 0) {
 	    return -1;
 	} else if (val > 0) {
 	    return 1;
 	} else {
 	    return 0;
 	}
     }
 
     @Override public int hashCode() {
         int hash = 5;
         hash = 13 * hash + (this.balls != null ? this.balls.hashCode() : 0);
         hash = 13 * hash + (this.pockets != null ? this.pockets.hashCode() : 0);
         hash = 13 * hash + (this.polygons != null ? this.polygons.hashCode() : 0);
         hash = 13 * hash + (this.tableColor != null ? this.tableColor.hashCode() : 0);
         hash = 13 * hash + (this.cueball != null ? this.cueball.hashCode() : 0);
         hash = 13 * hash + (this.shootingBall != null ? this.shootingBall.hashCode() : 0);
         hash = 13 * hash + (this.ghostBallObjectBall != null ? this.ghostBallObjectBall.hashCode() : 0);
         hash = 13 * hash + (this.ghostBallPosition != null ? this.ghostBallPosition.hashCode() : 0);
         return hash;
     }
 
     @Override public boolean equals(Object obj) {
         if(obj instanceof PoolPanel)
             return true;
         else
             return false;
     }
     
     //--------------------HIERARCHY BOUNDS INTERFACE--------------------//
     
     public void ancestorResized(HierarchyEvent he) {        
         canvas.setSize(getWidth(), getHeight());
     }
     
     public void ancestorMoved(HierarchyEvent he) { }
     
     //--------------------ERROR HANDLING--------------------//
     
     public boolean checkBounds(PoolBall b) {
         return false;
     }
     
     public void checkOverlaps(PoolBall ball) {
         Iterator<PoolBall> ballIterator = balls.iterator();
         while(ballIterator.hasNext()) {
             PoolBall ball2 = ballIterator.next();
             if(ball2 != ball && ball.checkOverlap(ball2)) {
                fixOverlap(ball, ball2);
             }
         }
         Iterator<PoolPolygon> polyIterator = polygons.iterator();
         while(polyIterator.hasNext()) {
             PoolPolygon poly = polyIterator.next();
             if(poly.checkOverlap(ball)) {
 
                 err = true;
             }
         }
     }
     
     public void fixOverlap(PoolBall a, PoolBall b) {
         a.color = Color.MAGENTA;
         b.color = Color.MAGENTA;
         err = true;
         
     }    
     
 }
 
 class PoolCameraController extends CameraController {
     PoolPanel pp;
     boolean doingDragAim = false;
     Sphere aimSphere = new Sphere(.25f);
     TransformGroup transformGroup = new TransformGroup();
     BranchGroup aimSphereGroup = new BranchGroup();
     
     
     public PoolCameraController(PoolPanel p) {
         super(p.universe, p.canvas);
         pp = p;
         transformGroup.addChild(aimSphere);
         transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
         aimSphereGroup.addChild(transformGroup);
         universe.addBranchGraph(aimSphereGroup);
     }
     
     @Override public void mouseDragged(MouseEvent me) {
         
         if(doingDragAim) {
             //Calculate the constants
             float x,y;
             x = ((float)(me.getX() - canvas.getWidth()/2)/(canvas.getWidth()/2));
             y = ((float)(canvas.getHeight()/2 - me.getY())/(canvas.getWidth()/2));                        
             double fieldOfView = universe.getViewer().getView().getFieldOfView();
             x *= fieldOfView/2;
             y *= fieldOfView/2;
             x = (float) Math.sin(x);
             y = (float) Math.sin(y);
             float _z = 1 - x * x - y * y;
             float z = (float) (_z > 0 ? Math.sqrt(_z) : 0);
             Vector3f lookDirection = new Vector3f(cameraPosition);
             lookDirection.scale(-1f);
             Vector3f cross = new Vector3f();            
             cross.cross(lookDirection, upVector);
             ChangeBasis cb = new ChangeBasis(cross, upVector, lookDirection);
             cb.invert();
             Vector3f clickVector = new Vector3f(x,y,z);
             cb.transform(clickVector);
             lookDirection.set(clickVector);
             
             //double angle = clickVector.angle(out);
             
             //cross.cross(clickVector, out);
             //cross.normalize();
                         
             //Get camera translation
             Vector3f actualPos = new Vector3f();
             universe.getViewingPlatform().getViewPlatformTransform().getTransform(transform);
             transform.get(actualPos);
             
             //Calculate the maginitude of the ray from the camera position
             //defined by the mouse to the x,y plane to get the x,y values.
             float magnitude = -actualPos.z/lookDirection.z;
             lookDirection.scale(magnitude);
             actualPos.add(lookDirection);
             transform = new Transform3D();
             transform.setTranslation(actualPos);
             transformGroup.setTransform(transform);
             actualPos.scale(-1f);
             actualPos.add(new Vector3f(pp.shootingBall.pos));
             actualPos.normalize();
             actualPos.scale(-1f);
             pp.setAim(actualPos.x, actualPos.y);
             
         } else {
             super.mouseDragged(me);
         }
     }
     
     @Override public void mousePressed(MouseEvent me) {
         if(me.getButton() != MouseEvent.BUTTON1) {
             doingDragAim = true;
         }
         super.mousePressed(me);
     }
     
     @Override public void mouseReleased(MouseEvent me) {
         if(doingDragAim) {
             
             doingDragAim = false;
         }
         super.mouseReleased(me);
     }
     
     public void snapToShootingBall() {
         cameraTrans.set(pp.shootingBall.pos);
         cameraTranslation.set(cameraTrans);
         cameraPosition.set(pp.aim);
         double angle = .3;
         Vector3f aimPerp = new Vector3f();
         aimPerp.x = (float) pp.aim.y;
         aimPerp.y = (float) -pp.aim.x;
         aimPerp.z = (float) pp.aim.z;        
         rotater.setAndRotateInPlace(aimPerp, angle, cameraPosition);                
         cameraPosition.normalize();
         cameraPos.set(cameraPosition);
         upVector.set(0.0f, 0.0f, 1.0f);
         upVec.set(upVector);    
         updateCamera();
     }
     
     public void overheadView() {
         cameraPos.set(0.0, 0.0, 1.0);
         upVec.set(0.0, 1.0, 0.0);
         cameraTrans.set(0.0, 0.0, 0.0f);
         camDistance = 40f;
         updateCamera();
         mouseReleased(null);
     }    
 }
