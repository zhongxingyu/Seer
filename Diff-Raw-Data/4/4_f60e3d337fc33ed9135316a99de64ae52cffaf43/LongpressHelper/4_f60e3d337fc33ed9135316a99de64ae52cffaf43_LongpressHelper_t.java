 package no.rkkc.bysykkel;
 
 import android.view.MotionEvent;
 
 
 /**
  * The MapActivity does not seem to include support for longpresses, and this class is intended to
  * facilitate that functionality. It is intended to be instantiated in the activity and called from
  * MapActivity.dispatchTouchEvent().
  * 
  * Here's how it can be used for this purpose:
  * 
  * 1) Override MapActivity.dispatchTouchEvent(MotionEvent event).
  * 	  This allows us to intercept all touch events.
  * 2) In dispatchTouchEvent(), call LongpressHelper.handleMotionEvent() for every event action
  *    except MotionEvent.ACTION_DOWN. This call makes sure to reset the longpress state in this class if any
  *    motion occurs that is invalid for a longpress.
  * 3) In dispatchTouchEvent(), create a new thread for all events of the type
  *    MotionEvent.ACTION_DOWN, and call LongpressHelper.isLongPressedDetected() from this thread.
  *    Check the result, and if this is true, a longpress is detected and you can act accordingly.
  */
 public class LongpressHelper {
 	static final float MOVEMENT_THRESHOLD = 0.5f;
 	static final int LONGPRESS_THRESHOLD = 500; // Time in ms required to acknowledge a longpress 
 	
 	private boolean isPotentialLongPress = false;
 	
 	/**
 	 * Takes a MotionEvent and checks it state to see if the event could be regarded as part of
 	 * a longpress.
 	 * 
 	 * Only MotionEvent.ACTION_MOVE could potentially be regarded as part of a
 	 * longpress, as this event is trigged by the finger moving slightly on the device screen. 
 	 * 
 	 * @param event
 	 */
 	public void handleMotionEvent(MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_MOVE) {
 
			if (event.getHistorySize() < 1) return; // First call
			
				// Get difference in position since previous move event
 			float diffX = event.getX()-event.getHistoricalX(event.getHistorySize()-1);
 			float diffY = event.getY()-event.getHistoricalY(event.getHistorySize()-1);
 			
 			// If position has moved substatially, this is not a long press
 			if (Math.abs(diffX) > MOVEMENT_THRESHOLD || Math.abs(diffY) > MOVEMENT_THRESHOLD) {
 				isPotentialLongPress = false;
 			}
 		} else {
 			isPotentialLongPress = false;
 		}
 	}
 	
 	/**
 	 * Loops for an amount of time while checking if the state of the isPotentialLongPress variable
 	 * has changed. If it has, this is regarded as if the longpress has been canceled. If 
 	 * 
 	 * @return
 	 */
 	public boolean isLongPressDetected() {
 		setTouchDetected();
 		try {
 			for (int i=0; i<(LONGPRESS_THRESHOLD/10); i++) {
 				Thread.sleep(10);
 				if (!isPotentialLongPress) {
 					return false;
 				}
 			}
 			return true;
 		} catch (InterruptedException e) {
 			return false;
 		} finally {
 			isPotentialLongPress = false;
 		}
 	}
 	
 	private void setTouchDetected() {
 		isPotentialLongPress = true;
 	}
 }
