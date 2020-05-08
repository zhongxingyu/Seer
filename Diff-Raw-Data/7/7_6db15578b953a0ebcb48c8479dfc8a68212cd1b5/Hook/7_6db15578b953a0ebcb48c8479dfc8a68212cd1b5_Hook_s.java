 package items;
 
 import java.util.ArrayList;
 
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.joints.Joint;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 
 import entities.Entity;
 
 public class Hook extends Entity {
 	private static final float SIZE = 10;
 	
 	private boolean attached;
	private Joint anchor;
 	
 	/**
 	 * @param x
 	 * @param y
 	 * @param width
 	 * @param height
 	 */
 	public Hook(float x, float y) {
 		super(x, y, SIZE, SIZE, 0, 0, 1);
 		setImage(Color.red);
 		getPhysicsBodyDef().bullet = true;
 		attached = false;
 	}
 
 	@Override
 	public void render(Graphics graphics) {
 		draw(graphics);
 	}
 
 	@Override
 	public void update(GameContainer gc, int delta) {
 		ArrayList<Body> touching = bodiesTouching();
 		if (touching.size() > 0) {
 			attach(touching.get(0));
 		}
 	}
 	
 	/**
 	 * Attaches the hook to the specified body
 	 * @param b a body
 	 */
 	private void attach(Body b) {
 //		Body self = getPhysicsBody();
 //		DistanceJointDef jointDef = new DistanceJointDef();
 //		jointDef.initialize(b, self, self.getPosition(), self.getPosition());
 //		anchor = getPhysicsWorld().createJoint(jointDef);
 		this.getPhysicsBody().setType(BodyType.STATIC);
 		attached = true;
 	}
 
 	/**
 	 * @param detach the hook, if it is connected to a wall
 	 */
 	public void detach() {
		Joint.destroy(anchor);
 		this.attached = false;
 	}
 
 	/**
 	 * @return if the hook is attached to a wall
 	 */
 	public boolean isAttached() {
 		return attached;
 	}
 	
 	@Override
 	public void reset() {
 		detach();
 	}
 
 }
