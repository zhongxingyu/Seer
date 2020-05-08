 /**
 marcinko
  */
 package pl.edu.agh.megamud.base;
 
 /**
  * Abstraction of a "action" that a creature can do. Behaviur should last
  * short(time) beacouse it takes time of the eventmanager which is on another
  * thread but all beahaviours are running on this eventmanager thread
  **/
 public abstract class Behaviour {
 	/**
 	 * An owner of this behaviour. Behaviour will work on this creature.
 	 */
 	protected BehaviourHolderInterface owner;
 	/**
 	 * Delay after which behaviour will run.
 	 */
 	protected long delay;
 	/**
 	 * Time this behaviour will be run.
 	 */
 	protected long nextTime;
 
 	public long getDelay() {
 		return delay;
 	}
 
 	public void setDelay(long delay) {
 		this.delay = delay;
 	}
 
 	public long getNextTime() {
 		return nextTime;
 	}
 
 	public Behaviour(BehaviourHolderInterface o, long delay) {
 		this.owner = o;
 		this.delay = delay;
 		installBehaviourInHoleder();
 	}
 
 	public void installBehaviourInHoleder() {
 		owner.addBehaviour(this);
 	}
 
 	public Object getOwner() {
 		return owner;
 	}
 
 	/**
 	 * To be implemented by subclasses - what actually Behavior does.
 	 */
 	protected abstract void action();
 
 	/**
 	 * Initialize a behaviour.
 	 */
 	public Behaviour init() {
 		put();
 
 		return this;
 	}
 
 	/**
 	 * Use this to call an action on a behaviour.
 	 */
 	public void makeAction() {
		action();
		// owner.removeBehaviour(this);
 	}
 
 	/**
 	 * Use this in lower classes to (re-)initialize a behaviour.
 	 */
 	protected final void put() {
 		nextTime = EventManager.getInstance().put(new Long(delay), this);
 	}
 
 }
