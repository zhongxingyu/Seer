 package base.common;
 
 import java.util.ArrayList;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import base.graphics.actions.GraphicAction;
 
 /**
  * 
  * @author mauro
  */
 public class AsyncActionBus {
 
 	public ReentrantReadWriteLock sharedLock = new ReentrantReadWriteLock(false);
 
 	/*
 	 * GRAPHICS
 	 */
 	private final ConcurrentLinkedQueue<GraphicAction> graphicActions = new ConcurrentLinkedQueue<>();
 
 	public AtomicBoolean graphicsStarted = new AtomicBoolean(false);
 
 	public void addGraphicsAction(GraphicAction a) {
 		graphicActions.offer(a);
 	}
 
 	public ArrayList<GraphicAction> getGraphicActions() {
 		ArrayList<GraphicAction> returnActions = new ArrayList<>();
 		GraphicAction temp = graphicActions.poll();
 		while (temp != null) {
 			returnActions.add(temp);
 			temp = graphicActions.poll();
 		}
 		return returnActions;
 	}
 
 	public AtomicBoolean[] getWasdArray() {
 		return wasdArray;
 	}
 
 	public AtomicBoolean[] getRotationArray() {
 		return rotationArray;
 	}
 
 	public AtomicBoolean[] getGunArray() {
 		return gunArray;
 	}
 
 	/*
 	 * INPUT
 	 */
	private final AtomicBoolean[] wasdArray = { new AtomicBoolean(), new AtomicBoolean(), new AtomicBoolean(), new AtomicBoolean() };
	private final AtomicBoolean[] rotationArray = { new AtomicBoolean(), new AtomicBoolean() };
	private final AtomicBoolean[] gunArray = { new AtomicBoolean(), new AtomicBoolean() };
 
 }
