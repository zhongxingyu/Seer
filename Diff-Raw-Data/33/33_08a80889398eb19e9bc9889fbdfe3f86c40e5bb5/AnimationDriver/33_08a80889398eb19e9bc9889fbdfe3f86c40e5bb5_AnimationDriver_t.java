 package yuuki.animation.engine;
 
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import yuuki.animation.Animation;
 import yuuki.ui.DialogHandler;
 
 /**
  * Animates a number of objects.
  */
 public class AnimationDriver implements Runnable, AnimationOwner {
 	
 	/**
 	 * Listens for animation events and sends them to the listeners on this
 	 * driver.
 	 */
 	private class EventHandler implements AnimationListener {
 		
 		@Override
 		public void animationComplete(AnimationEvent e) {
 			AnimationEvent firedEvent = getRefiredEvent(e);
 			fireAnimationComplete(firedEvent);
 		}
 		
 		@Override
 		public void animationStopped(AnimationEvent e) {
 			AnimationEvent firedEvent = getRefiredEvent(e);
 			fireAnimationStopped(firedEvent);
 		}
 		
 		/**
 		 * Gets the AnimationEvent that adds this AnimationDriver as the cause
 		 * of the event.
 		 * 
 		 * @param e The event to be fired.
 		 * 
 		 * @return An AnimationEvent with the same source as the given event
 		 * and the cause set to this AnimationDriver.
 		 */
 		private AnimationEvent getRefiredEvent(AnimationEvent e) {
 			AnimationEvent firedEvent;
 			Animation source = (Animation) e.getSource();
 			AnimationDriver cause = AnimationDriver.this;
 			firedEvent = new AnimationEvent(source, cause);
 			return firedEvent;
 		}
 		
 	}
 	
 	/**
 	 * The thread running the animation.
 	 */
 	private Thread animationThread;
 	
 	/**
 	 * The list of objects to animate.
 	 */
 	private List<Animatable> anims;
 	
 	/**
 	 * The speed that animation is occurring at.
 	 */
 	private int fps;
 	
 	/**
 	 * Handles animation events.
 	 */
 	private EventHandler handler = new EventHandler();
 	
 	/**
 	 * Listeners for animation events.
 	 */
 	private Set<AnimationListener> listeners;
 	
 	/**
 	 * Whether the animation is currently paused.
 	 */
 	private volatile boolean paused = false;
 	
 	/**
	 * The name of this AnimationDriver. Used in the thread name.
	 */
	private final String name;
	
	/**
 	 * Allocates a new Animator.
 	 * 
 	 * @param fps The speed that the Animator is to animate at.
	 * @param name The name of this AnimationDriver. May be set to null for no
	 * specific name.
 	 */
	public AnimationDriver(int fps, String name) {
		this.name = name;
 		anims = new CopyOnWriteArrayList<Animatable>();
 		listeners = new CopyOnWriteArraySet<AnimationListener>();
 		this.fps = fps;
 		animationThread = null;
 	}
 	
	/**
	 * Allocates a new Animator.
	 * 
	 * @param fps The speed that the Animator is to animate at.
	 */
	public AnimationDriver(int fps) {
		this(fps, null);
	}
	
 	@Override
 	public void addAnim(Animatable a) {
 		if (a.isControlled()) {
 			String error = "Animatable is already controlled!";
 			throw new IllegalArgumentException(error);
 		}
 		a.setControlled(true);
 		a.addAnimationListener(handler);
 		a.start();
 		anims.add(a);
 	}
 	
 	/**
 	 * Adds a listener to this driver.
 	 * 
 	 * @param l The listener to add.
 	 */
 	public void addListener(AnimationListener l) {
 		listeners.add(l);
 	}
 	
 	/**
 	 * Gets the speed of animation in frames per second.
 	 * 
 	 * @return The speed of animation.
 	 */
 	public int getFps() {
 		return fps;
 	}
 	
 	/**
 	 * Checks whether animation is currently occurring.
 	 * 
 	 * @return True if animation is occurring; false otherwise.
 	 */
 	public boolean isAnimating() {
 		return (animationThread != null);
 	}
 	
 	@Override
 	public void removeAnim(Animatable a) {
 		if (anims.remove(a)) {
 			a.setControlled(false);
 			a.removeAnimationListener(handler);
 		}
 	}
 	
 	/**
 	 * Removes a listener from this driver.
 	 * 
 	 * @param l The listener to remove.
 	 */
 	public void removeListener(Object l) {
 		listeners.remove(l);
 	}
 	
 	/**
 	 * Resumes the animation of objects.
 	 */
 	public void resume() {
 		this.paused = false;
 	}
 	
 	/**
 	 * Animates each of the items on the animation list, then sleeps for
 	 * an appropriate amount of time depending on the parent's FPS.
 	 */
 	@Override
 	public void run() {
 		try {
 			while (fps != 0) {
 				int ms = (int) Math.round((double) 1000 / fps);
 				Thread.sleep(ms);
 				checkPaused();
 				advanceAnimations();
 				checkPaused();
 			}
 		} catch (InterruptedException e) {
 			Thread.currentThread().interrupt();
 		} catch (RuntimeException e) {
 			DialogHandler.showError(e);
 		}
 	}
 	
 	/**
 	 * Sets the speed of animation in frames per second. It is not guaranteed
 	 * that animation speed will change immediately if the FPS is changed
 	 * during animation.
 	 * 
 	 * @param fps The new speed of animation.
 	 */
 	public void setFps(int fps) {
 		this.fps = fps;
 	}
 	
 	/**
 	 * Starts animating the objects that this Animator controls. They are
 	 * animated at a speed determined by this Animator's FPS.
 	 */
 	public void start() {
 		if (!isAnimating()) {
 			startAnimations();
			String name = (this.name != null) ? " - " + this.name : "";
			animationThread = new Thread(this, "AnimationDriver" + name);
 			animationThread.start();
 		}
 	}
 	
 	/**
 	 * Stops animating objects.
 	 */
 	public void stop() {
 		if (isAnimating()) {
 			stopAnimations();
 			animationThread.interrupt();
 			animationThread = null;
 		}
 	}
 	
 	/**
 	 * Pauses the animation of objects.
 	 */
 	public void suspend() {
 		this.paused = true;
 	}
 	
 	/**
 	 * Animates all objects in the animation list.
 	 */
 	private void advanceAnimations() {
 		for (Animatable a : anims) {
 			a.advanceFrame(fps);
 		}
 	}
 	
 	/**
 	 * Checks whether the animation has been paused, and if it has, instructs
 	 * all animations to pause until the pause has been ended.
 	 * 
 	 * @throws InterruptedException If the thread is interrupted while waiting
 	 * for the pause to be over.
 	 */
 	private void checkPaused() throws InterruptedException {
 		if (paused) {
 			pauseAnimations();
 			while (paused) {
 				Thread.sleep(10);
 			}
 		}
 		resumeAnimations();
 	}
 	
 	/**
 	 * Calls animationComplete() on all listeners.
 	 * 
 	 * @param e The original event.
 	 */
 	private void fireAnimationComplete(AnimationEvent e) {
 		AnimationListener[] ls = new AnimationListener[listeners.size()];
 		listeners.toArray(ls);
 		for (AnimationListener l : ls) {
 			l.animationComplete(e);
 		}
 	}
 	
 	/**
 	 * Calls animationStopped() on all listeners.
 	 * 
 	 * @param e The original event.
 	 */
 	private void fireAnimationStopped(AnimationEvent e) {
 		AnimationListener[] ls = new AnimationListener[listeners.size()];
 		listeners.toArray(ls);
 		for (AnimationListener l : ls) {
 			l.animationStopped(e);
 		}
 	}
 	
 	/**
 	 * Instructs all animations to pause.
 	 */
 	private void pauseAnimations() {
 		for (Animatable a : anims) {
 			a.pause();
 		}
 	}
 	
 	/**
 	 * Instructs all animations to resumes.
 	 */
 	private void resumeAnimations() {
 		for (Animatable a : anims) {
 			a.resume();
 		}
 	}
 	
 	/**
 	 * Instructs all animations to start.
 	 */
 	private void startAnimations() {
 		for (Animatable a : anims) {
 			a.start();
 		}
 	}
 	
 	/**
 	 * Instructs all animations to stop.
 	 */
 	private void stopAnimations() {
 		for (Animatable a : anims) {
 			a.stop();
 		}
 	}
 	
 }
