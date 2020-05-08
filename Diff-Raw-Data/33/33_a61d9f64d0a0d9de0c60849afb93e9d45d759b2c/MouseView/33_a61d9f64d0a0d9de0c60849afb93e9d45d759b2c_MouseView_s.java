 package spang.mobile;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import network.IConnection;
 import sensors.SensorProcessor;
 import utils.Vector2;
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.hardware.Sensor;
 import android.os.Build;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 
 @SuppressLint("NewApi")
 public class MouseView extends AbstractSpangView{
 	private static final String MOUSEMOVING_STATE = "MOUSEMOVING_STATE";
 	private static final String SCROLLING_STATE = "SCROLLING_STATE";
 	private static final String TIMEOUT_STATE = "TIMEOUT_STATE";
 
 	private final Paint paint = new Paint();
 	private final boolean multiTouchEnabled = Integer.parseInt(Build.VERSION.SDK) >= Build.VERSION_CODES.CUPCAKE;
 
 	private float radius;
 
 	private List<Vector2> pointers = new ArrayList<Vector2>();
 	private List<Vector2> prevPointers = new ArrayList<Vector2>();
 
 	private GestureDetector gestureDetector;
 
 	private SensorProcessor sp;
 
 	private static final int INVALID_POINTER_ID = -1;
 	private int fingerOneID = INVALID_POINTER_ID;
 
 	private InputStateMachine stateMachine;
 
 	public MouseView(Context context, AttributeSet attrs, IConnection connection) {
 		super(context, attrs, connection);
 
 		this.stateMachine = new InputStateMachine();
 		this.stateMachine.registerState(MOUSEMOVING_STATE, new MouseMovingState());
 		this.stateMachine.registerState(SCROLLING_STATE, new ScrollingState());
 		this.stateMachine.registerState(TIMEOUT_STATE, new TimeoutState());
		
 		this.stateMachine.changeState(MOUSEMOVING_STATE);
 
 		paint.setAntiAlias(true);
 		paint.setStrokeWidth(6f);
 		paint.setColor(Color.BLACK);
 		paint.setStyle(Paint.Style.STROKE);
 		paint.setStrokeJoin(Paint.Join.ROUND);
 
 		this.gestureDetector = new GestureDetector(context, simpleOnGestureListener);
 
 		this.sp = new SensorProcessor(context, connection);
 		this.sp.setActive(Sensor.TYPE_LINEAR_ACCELERATION, true);
 		this.sp.startProcess();
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		for (Vector2 position : pointers) {
 			Random rnd = new Random();
 			paint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
 			canvas.drawCircle(position.getX(), position.getY(), radius, paint);
 		}
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		stateMachine.onTouchEvent(event);
 		return true; //TODO Is this ok?
 	}
 
 	private void inputTimer(final int milliseconds){
 		Runnable runnable = new Runnable() {
 			public void run() {
 				try {
 					Thread.sleep(milliseconds);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		};
 		new Thread(runnable).start();
 	}
 
 	private GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener(){
 		public boolean onDown(MotionEvent e) {
 			Log.d("MOTIONEVENT:", "onDown");
 			return true;
 		}
 
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 			Log.d("MOTIONEVENT:", "onFling");
 			return true;
 		}
 
 		public void onLongPress(MotionEvent e) {
 			Log.d("MOTIONEVENT:", "onLongPress");
 			connection.sendUDP(new byte[]{(byte)2});
 		}
 
 		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
 				float distanceY) {
 			Log.d("MOTIONEVENT:", "dX = " + distanceX + "   dY = " + distanceY);
 			byte[] data = ByteBuffer.allocate(9).order(ByteOrder.LITTLE_ENDIAN)
 					.put((byte)3).putInt((int)(distanceX + 0.5f)).putInt((int)(distanceY + 0.5f)).array();
 			connection.sendUDP(data);
 			return true;
 		}
 
 		public void onShowPress(MotionEvent e) {
 			Log.d("MOTIONEVENT:", "onShowPress");
 		}
 
 		public boolean onSingleTapUp(MotionEvent e) {
 			Log.d("MOTIONEVENT:", "onSingleTapUp");
 			connection.sendUDP(new byte[]{(byte)1});
 			return true;
 		}
 	};
 
 	private class MouseMovingState extends InputState {
 		@Override
 		public void onTouchEvent(MotionEvent event){
 
 			gestureDetector.onTouchEvent(event);
 
 			fingerOneID = event.getPointerId(0);
 			final int activeIndex = event.findPointerIndex(fingerOneID);
 
 			pointers.clear();
 			prevPointers.clear();
 			pointers.add(activeIndex, new Vector2(event.getX(activeIndex), event.getY(activeIndex)));
 			prevPointers.add(activeIndex, pointers.get(activeIndex));
 
 			Vector2 position = pointers.get(activeIndex);
 
 			int eventID = event.getAction();
 
 			switch(eventID & MotionEvent.ACTION_MASK) {
 			case MotionEvent.ACTION_DOWN:
 				prevPointers.add(activeIndex, new Vector2(position.getX(), position.getY()));
 				break;
 			case MotionEvent.ACTION_MOVE:
 				for(int i = 0; i < event.getPointerCount(); i++) {
 					pointers.add(i, new Vector2(event.getX(i), event.getY(i)));
 				}
 
 				radius = event.getPressure()*100;
 				byte[] pressureData = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN)
 						.put((byte)14).putInt((int)radius).array();
 				connection.sendUDP(pressureData);
 
 				prevPointers.add(activeIndex, pointers.get(activeIndex));
 				break;
 
 			case MotionEvent.ACTION_UP:
 				fingerOneID = INVALID_POINTER_ID;
 				break;
 
 			case MotionEvent.ACTION_CANCEL:
 				fingerOneID = INVALID_POINTER_ID;
 				break;
 
 			case MotionEvent.ACTION_POINTER_DOWN:
 				stateMachine.changeState(SCROLLING_STATE);
 				break;
 
 
 			}
 			// Schedules a repaint.
 			invalidate();
 
 
 		}
 		/*	
 		@Override
 		protected void enter() {}
 
 		@Override
 		protected void exit() {} */
 	}
 	private class ScrollingState extends InputState {
 		@Override
 		public void onTouchEvent(MotionEvent event){
 
 			fingerOneID = event.getPointerId(0);
 			final int activeIndex = event.findPointerIndex(fingerOneID);
 
			pointers.clear();
 			prevPointers.clear();
 			pointers.add(activeIndex, new Vector2(event.getX(activeIndex), event.getY(activeIndex)));
			prevPointers.add(activeIndex, pointers.get(activeIndex));
 
 			Vector2 position = pointers.get(activeIndex);
 
 			int eventID = event.getAction();
 
 			switch(eventID & MotionEvent.ACTION_MASK) {
 			case MotionEvent.ACTION_DOWN:
 				prevPointers.add(activeIndex, new Vector2(position.getX(), position.getY()));
 				break;
 			case MotionEvent.ACTION_MOVE:
 				for(int i = 0; i < event.getPointerCount(); i++) {
 					pointers.add(i, new Vector2(event.getX(i), event.getY(i)));
 				}
 
 				radius = event.getPressure()*100;
 				byte[] pressureData = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN)
 						.put((byte)14).putInt((int)radius).array();
 				connection.sendUDP(pressureData);
 
 				//Vertical Scroll
 				byte[] vertData = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN)
						.put((byte)12).putInt((int)(20*(pointers.get(activeIndex).getY() - 
								prevPointers.get(activeIndex).getY() + 0.5f))).array();
 				connection.sendUDP(vertData);
 
 				//   Horizontal Scroll
 				//   byte[] horiData = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN)
 				//     .put((byte)13).putInt((int)(4*(xPos - xPosPrev + 0.5f))).array();
 				//   connection.sendUDP(horiData);
 
 				prevPointers.add(activeIndex, pointers.get(activeIndex));
 				break;
 
 			case MotionEvent.ACTION_UP:
 				fingerOneID = INVALID_POINTER_ID;
 				break;
 
 			case MotionEvent.ACTION_CANCEL:
 				fingerOneID = INVALID_POINTER_ID;
 				break;
 
 			case MotionEvent.ACTION_POINTER_UP:
 				final int pointerIndex = (eventID & MotionEvent.ACTION_POINTER_INDEX_MASK) >> 
 				MotionEvent.ACTION_POINTER_INDEX_SHIFT;
 				final int pointerID = event.getPointerId(pointerIndex);
 				if(pointerID == fingerOneID) {
 					final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
 					prevPointers.add(newPointerIndex, new Vector2(event.getX(newPointerIndex), event.getY(newPointerIndex)));
 
 					fingerOneID = event.getPointerId(newPointerIndex);
 				}

				stateMachine.changeState(TIMEOUT_STATE);
 				break;
 			} 
 
 			// Schedules a repaint.
 			invalidate();
 		}
 		/*	
 		@Override
 		protected void enter() {}
 
 		@Override
 		protected void exit() {} */
 	}
 
 	private class TimeoutState extends InputState {
 		@Override
 		public void onTouchEvent(MotionEvent event){
 			inputTimer(100);
 			stateMachine.changeState(MOUSEMOVING_STATE);
 		}
 		/*	
 		@Override
 		protected void enter() {}
 
 		@Override
 		protected void exit() {} */
 	}
 
 
 }
 
 
