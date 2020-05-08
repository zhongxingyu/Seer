 package cube;
 
 import animation.AbstractAnimation;
 import animation.AnimationCaller;
 import animation.CubeRotationAnimation;
 import animation.CubeRotationAnimation.Rotation;
 
 /**
  * Used to apply changes on the Cube due to pressed keys on the keyboard.
  * @author Björn Tegelund
  */
 public class CubeController implements AnimationCaller {
 	private Cube c;
 	private float targetRotY, targetRotZ, targetRotX;
 	private final float pi2 = (float) Math.PI/2;
 	
 	private CubeRotationAnimation animX, animY, animZ;
 	
 	/**
 	 * Creates a new CubeController to be used to apply changes on the Cube due to pressed keys on the keyboard.
 	 * @param c The Cube which should be affected.
 	 */
 	public CubeController(Cube c) {
 		this.c = c;
 	}
 	
 	/**
 	 * Applies all the current changes. This method is used to give the rotation of the Cube a smoother animation.
 	 * TODO Tidy this up
 	 */
 	public void runAnimations(int delta) {
 		if(animX != null)
 			animX.update(delta);
 		if(animY != null)
 			animY.update(delta);
 		if(animZ != null)
 			animZ.update(delta);
 		
 		removeExcess();
 		createAnimations(delta);
 	}
 	
 	private void removeExcess() {
 		if(c.getRotX() >= pi2*4)
 			c.setRotX(c.getRotX() % pi2*4);
 		if(c.getRotY() >= pi2*4)
 			c.setRotY(c.getRotY() % pi2*4);
 		if(c.getRotZ() >= pi2*4)
 			c.setRotZ(c.getRotZ() % pi2*4);
 	}
 	
 	private void createAnimations(int delta) {
 		if(Math.abs(targetRotX) > pi2/10) {
 			if(animX == null) {
 				animX = new CubeRotationAnimation(this, c, 500, Rotation.X, targetRotX);
 				animX.begin();
 			}
 		}
 		if(Math.abs(targetRotY) > pi2/10) {
 			if(animY == null) {
 				animY = new CubeRotationAnimation(this, c, 500, Rotation.Y, targetRotY);
 				animY.begin();
 			}
 		}
 		else if(Math.abs(targetRotY) == pi2/10)
 			c.setRotY(c.getRotY() + targetRotY/2);
 		
 		if(Math.abs(targetRotZ) > pi2/10) {
 			if(animZ == null) {
 				animZ = new CubeRotationAnimation(this, c, 500, Rotation.Z, targetRotZ);
 				animZ.begin();
 			}
 		}
 		else if(Math.abs(targetRotZ) == pi2/10)
 			c.setRotZ(c.getRotZ() + targetRotZ/2);
 	}
 	public void switchCube(Cube newCube){
 		c=newCube ;
 	}
 	
 	
public void rotateY (float tarY) {
		if(!(c.getRotZ() < 0 && c.getRotY() < 0) && Math.abs(c.getRotZ())%(4*pi2) >= 2*pi2 -1 && Math.abs(c.getRotZ())%(4*pi2) <= 2*pi2 + 1 && tarY > 0 && Math.abs(c.getRotY()) >= pi2-1 && Math.abs(c.getRotY()) <= pi2+1) {
			targetRotZ = -c.getRotZ();
			targetRotY = -c.getRotY();
			return;
		}
 		targetRotY = tarY;
 	}
 	public void rotateZ (float tarZ) {
 		targetRotZ = tarZ;
 	}
 	public void rotateX (float tarX) {
 		targetRotX = tarX;
 	}
 
 	@Override
 	public void animationDone(AbstractAnimation animation) {
 		if(animation == animY) {
 			animY = null;
 			targetRotY = 0f;
 		}
 		if(animation == animZ) {
 			animZ = null;
 			targetRotZ = 0f;
 		}
 	}
 	
 	public void resetRotations() {
 		rotateX(-c.getRotX());
 		rotateY(-c.getRotY());
 		rotateZ(-c.getRotZ());
 	}
 }
