 package se2.cleaningRobot.simulation;
 
 import java.awt.Graphics;
 
 import java.awt.*;
 import java.awt.image.*;
 import java.lang.*;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 
 import se2.cleaningRobot.control.*;
 
 /**
  * @author SE2 Gruppe 8
  */
 public class Scene extends Canvas implements Observer {	
 
 	private ArrayList<CRobot> robotList = new ArrayList<CRobot>();	
   private BufferedImage robot; // represents image of a cleaningRobot
   private ArrayList<Obstacle> obstacleList = new ArrayList<Obstacle>();
 
   public Scene() {
     add(new CRobot(200, 200, 1000, Color.BLUE));
     add(new Obstacle(100, 100, Color.RED));
     robot = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
   }
 	
   @Override
 	public void paint(Graphics g) {
     // get draw area of the scene
    Rectangle bounds = g.getClipBounds();
 
     // set background color
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, bounds.width, bounds.height);
 
     // draw scene objects
    drawObstacles(g);
    drawRobots(g);
 	}	
 
   @Override
   public void update(Observable o, Object arg) {
     System.out.println("UPDATE Scene");
     repaint();
   }
 
 	public void add(CRobot robot) {
 		robotList.add(robot);
 	}	
 
   public void add(Obstacle obstacle) {
     obstacleList.add(obstacle);
   }
 
  private void drawObstacles(Graphics g) {
     for (int i=0; i < obstacleList.size(); i++) {
       Obstacle ob = obstacleList.get(i);
      g.setColor(ob.color);
      g.fillRect(10, 10, 100, 100);
       //g.drawImage(robot, 500, 500, null);
     }
   }
 
  private void drawRobots(Graphics g) {
 		for (int i=0; i < robotList.size(); i++) {
 			CRobot r = robotList.get(i);
			g.setColor(r.color);
 			//g.fillArc(r.xPos , r.yPos, 50, 50, 0, 360);
      g.drawImage(robot, 500, 500, null);
       System.out.println("color: " + robot.getRGB(10,10));
 		}
   }
 }
