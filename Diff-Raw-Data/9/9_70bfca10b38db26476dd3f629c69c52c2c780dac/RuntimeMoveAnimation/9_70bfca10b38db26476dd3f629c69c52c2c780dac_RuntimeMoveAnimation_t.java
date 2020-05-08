 package se2.e.engine3d.j3d.animations;
 
 import javax.media.j3d.WakeupCondition;
 import javax.media.j3d.WakeupOnElapsedFrames;
 
import animations.Animation;

 import se2.e.engine3d.j3d.DynamicBranch;
import se2.e.simulator.runtime.petrinet.RuntimeToken;
 
 /**
  * The Class RuntimeMoveAnimation.
  */
 public class RuntimeMoveAnimation extends RuntimeAnimation {
 
 	/** The Constant UPDATE_FRAME_COUNT. */
 	private static final int UPDATE_FRAME_COUNT = 10;
 
 	/**
 	 * Instantiates a new runtime move animation.
 	 *
 	 * @param targetBranch the target branch
 	 * @param animation the animation
 	 */
	public RuntimeMoveAnimation(DynamicBranch targetBranch, Animation animation, RuntimeToken token) {
		super(targetBranch, animation, token);
 	}
 
 	@Override
 	public void init() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public WakeupCondition onUpdateAnimation() {
 		// TODO Auto-generated method stub
 		return new WakeupOnElapsedFrames(UPDATE_FRAME_COUNT);
 	}
 
 	@Override
 	protected void onAnimationFinished() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
