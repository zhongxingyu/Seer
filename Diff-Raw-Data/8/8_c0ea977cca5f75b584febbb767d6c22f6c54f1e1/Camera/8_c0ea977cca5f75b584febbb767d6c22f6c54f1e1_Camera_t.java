 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.geom.Polygon;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.tiled.TiledMap;
 
 
 public class Camera{
 	Rectangle playerView;
 	int viewHeight = Main.gameHeight, viewWidth = Main.gameWith;
 	float viewAngle;
 
 	float forwardPointView;
 	float hideLength;
 
	boolean flashlight = true;
 
 	public Camera(){
 		playerView = new Rectangle(0, 0, viewWidth, viewHeight);
 		forwardPointView = viewWidth;
 		hideLength = 2 * viewWidth;
 	}
 
 	public void centerArea(Graphics g, Player player){
 		playerView.setCenterX(player.getX());
 		playerView.setCenterY(player.getY());
 		g.setWorldClip(playerView);
 		viewAngle = 70;
 	}
 	public void toggleFlashLight(){
 		if(flashlight){
 			flashlight = false;
 		}
 		else{
 			flashlight = true;
 		}
 	}
 
 	public Rectangle getView(){
 		return playerView;
 	}
 
 	/**
 	 * 
 	 */
 	public void hideUnseen(Player player, Graphics gx){
 		// Triangle behind
 		Polygon hidden = new Polygon();
 		// Right
 		Polygon hidden2 = new Polygon();
 		// Left
 		Polygon hidden3 = new Polygon();
 		// Flashlight
 		Polygon flash = new Polygon();
 		// Helps to calculates the points
 		Vector2f pointCalc = new Vector2f(player.getDirection());
 
 		hidden.addPoint(player.getPosition().getX(), player.getPosition().getY());
 		hidden2.addPoint(player.getPosition().getX(), player.getPosition().getY());
 		hidden3.addPoint(player.getPosition().getX(), player.getPosition().getY());
 		flash.addPoint(player.getPosition().getX(), player.getPosition().getY());
 
 		pointCalc.sub(viewAngle/2);
		pointCalc.scale((float) (forwardPointView*1.5));
 		pointCalc.add(player.getPosition());
 		hidden2.addPoint(pointCalc.getX(), pointCalc.getY());
 		flash.addPoint(pointCalc.getX(), pointCalc.getY());
 		pointCalc.sub(player.getDirection().copy().scale(hideLength));
 		hidden2.addPoint(pointCalc.getX(), pointCalc.getY());
 		hidden.addPoint(pointCalc.getX(), pointCalc.getY());
 
 		pointCalc = player.getDirection();
 		pointCalc.sub(-viewAngle/2);
		pointCalc.scale((float) (forwardPointView*1.5));
 		pointCalc.add(player.getPosition());
 		hidden3.addPoint(pointCalc.getX(), pointCalc.getY());
 		flash.addPoint(pointCalc.getX(), pointCalc.getY());
 		pointCalc.sub(player.getDirection().copy().scale(hideLength));
 		hidden3.addPoint(pointCalc.getX(), pointCalc.getY());
 		hidden.addPoint(pointCalc.getX(), pointCalc.getY());
 
 		gx.setColor(new Color((float) 0.05, (float) 0.05, (float) 0.05, (float) 0.97));
 		gx.fill(hidden);
 		gx.fill(hidden2);
 		gx.fill(hidden3);
 		if(flashlight){
 			gx.setColor(new Color((float) 0.92, (float) 0.95, (float) 0.47, (float) 0.1));
 		}
 		gx.fill(flash);
 	}
 }
