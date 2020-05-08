 package Ass01.ML;
 
 import java.awt.Color;
 import java.awt.Frame;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.lang.*;
 import java.util.*;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLCanvas;
 import javax.media.opengl.GLEventListener;
 
 import com.sun.opengl.util.FPSAnimator;
 
 //TODO: how to choose 90% greedy exploration - if no car waiting at the lights leave it red else switch it
 // choose how to switch it better
 //TODO: choose how to choose the best next option better.
 
 public class Controller implements GLEventListener {
     static int time;
     static int speed = 1;
     Intersection intersection = new Intersection(new Position(50,50));
     QLearning ql = new QLearning();
     int scale = 6;
     boolean nextMove;
     int learningCount = 0;
     int playCount = 0;
     int endLearning = 5000;
     int runTime = 10000;
 
     public static void main(String [] args){
     	Controller c = new Controller();
     	c.run();
     }
 
 	public Controller() {
         // Animation setup
         GLCanvas canvas = new GLCanvas();
 		canvas.setBackground(Color.white);
 		canvas.setSize(100*scale, 100*scale);
 		Frame frame = new Frame("Simulation");
 		frame.add(canvas);
 		frame.setSize(100*scale, 100*scale);
 		frame.setBackground(Color.white);
 		frame.setVisible(true);
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				System.exit(0);
 			}
 		});
 
 		canvas.addGLEventListener(this);
 		FPSAnimator animator = new FPSAnimator(canvas, 10);
         animator.start();
 	}
 	
 	private void run() {
 	    //System.out.println("Hello, World");
         int k = 0;
         Random rnd = new Random();
 
         while(k < runTime){
         	
         	// Timeout to make the simulator run at a reasonable speed.
         	try {
 				Thread.sleep(1);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
             k++;
 
             // Get the next move and execute it
             if (k < endLearning) {             	
                 nextMove = ql.getNextMove(getClosetPos(intersection), intersection.getLightState());
             } else {
                 nextMove = ql.getBestAction(getClosetPos(intersection), intersection.getLightState());
             }
 
             
             
             // Update the state of the lights
             if (nextMove) {
                 for (int i=0; i < intersection.getNumRoads(); i++){
                 	int newS = (intersection.getLightState(i)+1)%2;
                     intersection.setLightState(i, newS); //toggle light state
                 }
             }
             
             
 
             // Make an obstacle
             ListIterator<Road> roadItr = intersection.getRoads().listIterator();
             ListIterator<Integer> lightItr = intersection.getLightState().listIterator();            
 
             while(roadItr.hasNext() && lightItr.hasNext())
             {
                 Road curRoad = roadItr.next();
                 Integer lightState = lightItr.next();
                 
                 ListIterator<Car> carItr = curRoad.getCars().listIterator();
                 Car prevCar;
                 Car curCar = null;
                 while (carItr.hasNext()){
                 	prevCar = curCar;
                     curCar = carItr.next();
 
                     //move forward - double check, not queued at light
                     Position lights = new Position(-1, -1);
                     Position carInFront = new Position(-1, -1);
                     if (lightState.equals(Intersection.red)) {
                         lights = intersection.getPos();
                     }
                     if (prevCar != null){
                         carInFront = prevCar.getPos();
                     }
                     if (curCar.canMoveCar(lights, curRoad.getDirection()) &&
                     	curCar.canMoveCar(carInFront, curRoad.getDirection())) {
                     	curCar.moveCar(curRoad.getDirection());
                     }
                     
                     // Update scores
                     if (!curCar.canMoveCar(lights, curRoad.getDirection())) {
                     	if (k < endLearning) {
                     		learningCount++;
                     	} else {
                     		playCount++;
                     	}
                     }
                 }
                 //
                 if (time%(rnd.nextInt(10)+5)==0 ) {
                     Position p = new Position(0,0);
                     if (curRoad.getDirection() == Road.horizontal) {
                         p.setY(curRoad.getOffset());
                     }   else if (curRoad.getDirection() == Road.vertical) {
                         p.setX(curRoad.getOffset());
                     }
                     Car c = new Car(p,speed);
                     curRoad.addCar(c);
                 }
             }
 
            // Remove cars from the intersection
            List<Road> roads = intersection.getRoads();
             for(int j = 0; j < intersection.getNumRoads(); j++) {
                 Road nextRoad = roads.get(j);
                 for (int i = 0; i < nextRoad.getCars().size(); i++) {
                     Car nextCar = nextRoad.getCars().get(i);
                    if (nextCar.removeCar(intersection.getPos(),nextRoad.getDirection())) {
                         nextRoad.removeCar();
                     }
                 }
             }
 
             // Update the qValues
             if (k < endLearning) {
                 ql.updateQValue(getClosetPos(intersection), intersection.getLightState());
             }
 
             time++;
 
         }
 
         System.out.println("learningCount = "+learningCount);
         System.out.println("playCount = "+playCount);
     }
 
     public static List<Integer> getClosetPos(Intersection intersection) {
         ListIterator<Position> closetPosItr = intersection.getClosestCarsInt().listIterator();
         List<Integer> closetCars = new ArrayList<Integer>();
         Position intPos = intersection.getPos();
 
         while (closetPosItr.hasNext()) {
             Position nextPos = closetPosItr.next();
             if (nextPos.equals(intPos)) {
                 closetCars.add(9);
             } else {
                 int x =  (Math.abs(nextPos.getX() - intPos.getX()) +
                         Math.abs(nextPos.getY() - intPos.getY()));
                 if (x > 9) {
                     x = 9;
                 }
 
                 closetCars.add(x);
             }
         }
         return closetCars;
     }
     
 	@Override
 	public void display(GLAutoDrawable draw) {
 		GL gl = draw.getGL();
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
 		gl.glMatrixMode(GL.GL_MODELVIEW);
 		gl.glLoadIdentity();
 		
 		//Scale to the screen
 		gl.glPushMatrix();
 		gl.glTranslated(-1.0,-1.0,0.0);
 		gl.glScaled(1.0/500, 1.0/500, 0.0);
 		
 		// Draw grass
 		gl.glBegin( GL.GL_POLYGON );
 		gl.glColor3f(0.1f,0.7f,0.1f);
 		gl.glVertex2d(0, 0);
 		gl.glVertex2d(0, 1000);
 		gl.glVertex2d(1000, 1000);
 		gl.glVertex2d(1000, 0);
 	    gl.glEnd();
 		
 	    // Draw Road
 	    ListIterator<Road> roadItr = intersection.getRoads().listIterator();
 	    while(roadItr.hasNext()) {
 	    	gl.glPushMatrix();
 	    	drawRoad(gl, roadItr.next());
 	    	gl.glPopMatrix();
 	    }
 	    
 	    // Draw Car
 	    roadItr = intersection.getRoads().listIterator();
 	    while(roadItr.hasNext()) {
 	    	ListIterator<Car> carsItr = roadItr.next().getCars().listIterator();
 	    	while (carsItr.hasNext()) {
 	    		gl.glPushMatrix();
 	    		drawCar(gl, carsItr.next());
 	    		gl.glPopMatrix();
 	    	}
 	    }
 	    
 	    gl.glPushMatrix();
 	    drawIntersection(gl, intersection);
 	    gl.glPopMatrix();
 	    
 	    gl.glPopMatrix(); //Scaling to screen popped
 		gl.glFlush();
 	}
 
 	@Override
 	public void displayChanged(GLAutoDrawable draw, boolean mode, boolean device) {		
 	}
 
 	@Override
 	public void init(GLAutoDrawable draw) {
 		GL gl = draw.getGL();
 		gl.glClearColor(0, 0, 0, 0);
 	}
 
 	@Override
 	public void reshape(GLAutoDrawable draw, int x, int y, int width, int height) {
 	}
     
     public void drawRoad(GL gl, Road r) {
     	if (r.getDirection() == Road.horizontal) {
     		gl.glTranslated(0,r.getOffset()*10,0);
     		gl.glRotated(-90.0, 0.0, 0.0, 1.0);
     		gl.glTranslated(-10,0,0);
     	} else {
     		gl.glTranslated(r.getOffset()*10,0,0);
     	}
     	
 		gl.glBegin( GL.GL_POLYGON );
 		gl.glColor3f(0.2f,0.2f,0.2f);
 		gl.glVertex2d(0, 0);
 		gl.glVertex2d(0, 1000);
 		gl.glVertex2d(10, 1000);
 		gl.glVertex2d(10, 0);
 	    gl.glEnd();
     }
     
     public void drawCar(GL gl, Car c) {
     	Position p = c.getPos();
     	gl.glTranslated(p.getX()*10,p.getY()*10,0);
     	
 		gl.glBegin( GL.GL_POLYGON );
 		gl.glColor3d(c.r,c.g,c.b);
 		gl.glVertex2d(0, 0);
 		gl.glVertex2d(0, 10);
 		gl.glVertex2d(10, 10);
 		gl.glVertex2d(10, 0);
 	    gl.glEnd();
     }
     
     public void drawIntersection(GL gl, Intersection i) {
     	gl.glPushMatrix();
     	gl.glTranslated(i.getPos().getX()*10-10,i.getPos().getY()*10,0);
     	gl.glBegin( GL.GL_POLYGON );
     	if (i.getLightState().get(0) == Intersection.green) {
     		gl.glColor3f(0.2f,1f,0.2f);
     	} else {
     		gl.glColor3f(1f,0.2f,0.2f);
     	}
 		gl.glVertex2d(0, 0);
 		gl.glVertex2d(0, 10);
 		gl.glVertex2d(10, 10);
 		gl.glVertex2d(10, 0);
 	    gl.glEnd();
     	gl.glPopMatrix();
 	    
 	    gl.glPushMatrix();
     	gl.glTranslated(i.getPos().getX()*10,i.getPos().getY()*10-10,0);
     	gl.glBegin( GL.GL_POLYGON );
     	if (i.getLightState().get(1) == Intersection.green) {
     		gl.glColor3f(0.2f,1f,0.2f);
     	} else {
     		gl.glColor3f(1f,0.2f,0.2f);
     	}
 		gl.glVertex2d(0, 0);
 		gl.glVertex2d(0, 10);
 		gl.glVertex2d(10, 10);
 		gl.glVertex2d(10, 0);
 	    gl.glEnd();
 	    gl.glPopMatrix();
     }
 
 }
