 package minigolf;
 import java.util.*;
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import javax.swing.*;
 
 
 public class MinigolfGame extends JPanel implements MouseListener, MouseMotionListener{
 
 	//FIELDS
 	int clicks=0;
 
 	double mouseX,mouseY;
 
 	boolean isMoving = false;
 	boolean hasBall = false;
 	boolean started = false;
 	boolean drawHelperLine = false;
 
 	public Rectangle screen, bounds;
 	public JFrame frame;
 	public MGBall ball;
 	public MGHole goal;
 	//	public MGWall wall;
 
 
 	//CONSTRUCTORS
 	public MinigolfGame(){ 
 
 		super();
 		screen = new Rectangle(0,0,800,600);
 		bounds = new Rectangle(0,0,800,600);
 		ball = new MGBall();
 		//		wall = new MGWall();
 		//mgTask = new MGTimerTask();	
 		this.addMouseListener(this);
 		this.addMouseMotionListener(this);
 		this.validate();
	}//
 
 
 
 	//INNER CLASS HOLE
 	class MGHole extends Rectangle {
 
 		//FIELDS
 		int x,y,width,height;
 
 		//CONSTRUCTORS
 		public MGHole(int x, int y, int width, int height,Graphics g){
 			this.x=x;
 			this.y=y;
 			this.width=width;
 			this.height=height;
 			g.setColor(Color.BLACK);
 			g.fillOval(x, y, width, height);
 		}
 	}
 
 	//INNER CLASS WALL
 
 	public class MGWall extends Rectangle{
 		Color brickColor;
 
 		public MGWall(int newX, int newY, int newWidth, int newHeight){
 			super(newX, newY, newWidth, newHeight);
 			brickColor = new Color(192, 192, 192);
 		}
 
 		public MGWall(int newX, int newY){
 			this(newX, newY, 10, 10);
 		}
 
 		public MGWall(){
 			this(0,0,10,10);
 		}
 
 		public void setColor(Color newColor) {brickColor=newColor;}
 		public Color getColor(){return brickColor;}
 	}
 
 	//INNER CLASS BALL
 	class MGBall extends Rectangle{
 
 		//FIELDS
 		//		int size=10;
 		double x, y, xVel, yVel;
 
 		//CONSTRUCTORS
 		public MGBall(){
 			super(0,0,10,10);
 		}
 
 		//METHODS
 		public void move(){
 
 			//tests if the ball is moving
 			if(xVel!=0. || yVel !=0.){
 				isMoving = true;
 				drawHelperLine = false;
 			}
 			else{
 				isMoving = false;
 				drawHelperLine = true;
 			}
 
 			//friction factor
 			xVel*=0.97;
 			yVel*=0.97;
 
 			//determine new coordinates
 			x+=xVel;
 			y+=yVel;
 
 			if (x>(bounds.width-ball.width)){
 				xVel = -xVel;
 				x = bounds.width-ball.width;
 			}
 
 			if(y>(bounds.height-ball.height)){
 				yVel = -yVel;
 				y = bounds.height-ball.height;
 			}
 
 			if (x <= 0) { xVel = -xVel; x = 0; }
 
 			if (y <= 0) { yVel = -yVel; y = 0; }
 
 			//System.out.println("x:"+x + " "+ "y:"+y);
 			if(x > 750 && x < 750+5 && y > 550 && y < 550+5){
 				xVel=0;
 				yVel=0; 
 				hasBall=true;
 				drawHelperLine = false;
 			}
 
 			//make the ball stop earlier
 			if(Math.abs(xVel)<0.05){xVel=0.;}
 
 			if(Math.abs(yVel)<0.05){yVel=0.;}
 
 			/*			// Detect wall and bounce if necessary.
 			if (intersects(wall)) {
 				// Get the intersection rectangle to find out which way to bounce.
 				System.out.println(1);
 				Rectangle iRect = intersection(wall);
 				// If we hit on the left side, go left (negative x velocity).
 				if ((x+(width/2))<(iRect.x+(iRect.width/2))){xVel=(0-Math.abs(xVel));}
 				// If we hit on the right side, go right (positive x velocity).
 				if ((x+(width/2))>(iRect.x+(iRect.width/2))){xVel=Math.abs(xVel);}
 				// If we hit on the top, go up.
 				if ((y+(height/2))<(iRect.y+(iRect.height/2))){yVel=(0-Math.abs(yVel));}
 				// If we hit on the bottom, go down.
 				if ((y+(height/2))>(iRect.y+(iRect.height/2))){yVel=Math.abs(yVel);}
 			}
 			 */					
 		}
 
 		public void draw(Graphics g){
 			// the ball draws itself in the graphics context given.
 			g.setColor(Color.yellow); // Use the ball's color for the ball.
 			g.fillOval((int)x, (int)y, width, height); // Draw the ball.
 		}
 	}
 
 	//MAIN CLASS METHODS
 	public void paintComponent(Graphics g){
 
 		Graphics2D g2 = (Graphics2D)g;
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		bounds = g.getClipBounds();
 		Color fieldColor=new Color(0,128,0);
 		g.setColor(fieldColor);
 		g.fillRect(screen.x, screen.y, screen.width, screen.height);
 		goal = new MGHole(750,550,15,15,g);
 
 		//draw helper line
 		g.setColor(Color.RED);
 		if (drawHelperLine){g.drawLine((int)(ball.x+ball.width/2), (int)(ball.y+ball.width/2), (int)(mouseX), (int)(mouseY));}
 
 		//crosshair cursor
 		g.setColor(Color.BLACK);
 		setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
 
 		/*		//draw walls
 		g.setColor(wall.getColor());
 		g.fillRect(wall.x, wall.y, wall.width, wall.height);*/
 
 		//draw ball
 		ball.draw(g);
 	}
 
 	//MOUSE EVENTS
 	public void mouseClicked(MouseEvent e) {
 
 		if(!started){e.consume();}
 		else{
 			if(hasBall){e.consume();}
 			else{
 				if(isMoving){e.consume();}
 
 				else
 				{
 					int xclick = e.getX();
 					int yclick = e.getY();
 
 					ball.xVel=((xclick-(ball.x+ball.width/2))/10);
 					ball.yVel=((yclick-(ball.y+ball.height/2))/10);
 					clicks+=1;
 
 					//debug mouse click coords
 					//			System.out.println(xVel+" "+yVel);
 					//			System.out.println(clicks);
 				}
 			}
 		}
 	}
 
 	public void mouseEntered(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void mousePressed(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void mouseMoved(MouseEvent o) {
 		// TODO Auto-generated method stub
 		mouseX=o.getX();
 		mouseY=o.getY();
 		//System.out.println("x:"+ mouseX + " "+ "y:"+mouseY);
 		if(!isMoving)repaint();
 	}
 }
