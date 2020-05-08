 package obstacles;
 
 import game.GameOverException;
 import game.SnakeModel;
 
 import java.awt.Graphics2D;
 import java.awt.geom.Ellipse2D;
 
 
 public class CircleObstacle extends Obstacle{
 	private final int FRAMES_PER_SECOND = 4;
 	SnakeModel mySnake;
 	public Ellipse2D.Double circle;
 	public CircleObstacle(int duration, double xloc, double yloc, double d, SnakeModel snake){
		circle = new Ellipse2D.Double(xloc, yloc, d, d);
 		origTimer = duration;
 		timer = 0;
 		mySnake = snake;
 	}
 	
 
 	
 	@Override
 	public void update() throws GameOverException {
 		timer++;
 		if (timer == origTimer * FRAMES_PER_SECOND){
 			if (haveCollided())
 				throw new GameOverException();
 			circle.setFrame(0,0,0,0);
 		}
 		else {
 			double alpha = 1 + (timer / (origTimer * FRAMES_PER_SECOND));
 			double x = circle.getX();
 			double y = circle.getY();
 			double d = circle.getWidth();
 			double centerX = x + d / 2;
 			double centerY = y + d / 2;
 			d = d * alpha;
 			x = centerX - d / 2;
 			y = centerY - d / 2;
			circle.setFrame(x, y, d, d);
 		}
 	}
 
 	@Override
 	public boolean haveCollided() {
 
 		// get two centres somehow
 
 		// centre of snake
 		double cx1 = mySnake.getOrigin().getX();
 		double cy1 = mySnake.getOrigin().getY();
 
 		// centre of circle obstacle
 		double topx = circle.getX();
 		double topy = circle.getY();
 		double d = circle.getWidth();
 		double x = topx + d / 2;
 		double y = topy - d / 2;
 		
 
 		// smaller and larger radii of snake
 		double r1 = mySnake.getInnerRadius(); 
 		double r2 = mySnake.getOuterRadius();
 
 
 		double distance = Math.sqrt(Math.pow(x-cx1, 2)+Math.pow(y-cy1, 2));
 		if(distance >= r1 && distance <= r2)
 			return true;
 		else if(distance < r1)
 			return (distance + d / 2) >= r1;
 		else 
 			return (distance - d / 2) <= r2;
 		
 	}
 
 	@Override
 	public void draw(Graphics2D g) {
 		g.draw(circle);
 	}
 
 }
