 package aharisu.mascot;
 
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Random;
 
 import aharisu.mascot.MascotEvent.Type;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Rect;
 import android.os.Handler;
 import android.util.Log;
 
 
 /**
  * 
  * ビュー内を自由に動き回るマスコットの動作させるクラス
  * 
  * @author aharisu
  *
  */
 public final class Mascot implements IMascot{
 	public enum ActionType {
 		LongPress,
 		SingleTap,
 		DoubleTap,
 		Scroll,
 		Fling,
 	};
 	
 	public enum Level {
 		High(2),
 		Middle(1),
 		Low(0);
 		
 		public final int  number;
 		
 		private Level(int number) {
 			this.number = number;
 		}
 	};
 	
 	
 	private final Handler mHandler = new Handler();
 	private final MascotView.ShowMascotView mView;
 	
 	private final Queue<MascotEvent> mEventQueue = new LinkedList<MascotEvent>();
 	
 	private final ArrayList<MascotState> mBasicStateList = new ArrayList<MascotState>();
 	private final ArrayList<UserInteractionState> mInteractionStateList = new ArrayList<UserInteractionState>();
 	private final StateSpeak mSpeakState = new StateSpeak(this);
 	
 	private MascotState mCurState;
 	
 	private Random mRand = new Random();
 	
 	private Object mSyncUpdateObj = new Object();
 	private boolean mStateChange = false;
 	
 	private final Runnable mUpdate = new Runnable() {
 		
 		@Override public void run() {
 			
 			synchronized (mSyncUpdateObj) {
 				if(!mIsStarted) {
 					return;
 				}
 				
 				if(mStateChange) {
 					mStateChange = false;
 					transition(getNextState());
 				} else if(mCurState.isAllowInterrupt()) {
 					MascotEvent event = mEventQueue.poll();
 					if(event != null) {
 						if(event.type == Type.Text && mSpeakState.isEnable()) {
 							mSpeakState.setText(event.text);
 							forceTransition(mSpeakState);
 						}
 					} else if(mStateChange || isExist(mCurState)) {
 						//今の状態と次の状態が同じ場合もある
 						//その場合は何も起きない
 						transition(getNextState());
 					}
 				}
 				
 				
 				if(mCurState.update()) {
 					mHandler.postDelayed(mUpdate, mCurState.getUpdateInterval());
 				}
 			}
 		}
 		
 		private MascotState getNextState() {
 			int max = 0;
 			
 			int[] priority = new int[]{1, 3, 7};
 			
 			for(MascotState state : mBasicStateList) {
 				max += priority[state.getEntryPriority().number];
 			}
 			
 			int number = mRand.nextInt(max - 1) + 1;
 			
 			for(MascotState state : mBasicStateList) {
 				number -= priority[state.getEntryPriority().number];
 				if(number <= 0)
 					return state;
 			}
 			
 			//ここは本来は実行されない
 			Log.e("failure", "transition failure");
 			return mBasicStateList.get(0);
 		}
 		
 		private boolean isExist(MascotState state) {
 			if(!state.isAllowExist())
 				return false;
 			
 			int prob = mCurState.getExistProbability().number;
 			return mRand.nextInt(99) < (new int[]{30, 10, 3})[prob];
 		}
 		
 	};
 	
 	private boolean mIsStarted = false;
 	
 	public Mascot(MascotView.ShowMascotView view) {
 		this.mView = view;
 	}
 	
 	public void addEvent(MascotEvent event) {
 		mEventQueue.offer(event);
 	}
 	
 	public void addBasicState(MascotState state) {
 		mBasicStateList.add(state);
 	}
 	
 	public void addUserInteractionState(UserInteractionState state) {
 		mInteractionStateList.add(state);
 	}
 	
 	public void setSpeakStateImage(Bitmap image) {
 		mSpeakState.setImage(image);
 	}
 	
 	public void draw(Canvas canvas) {
 		if(mIsStarted) {
 			mCurState.draw(canvas);
 		}
 	}
 	
 	public boolean hitTest(int x, int y) {
 		if(mIsStarted) {
 			return mCurState.hitTest(x, y);
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * @throws IllegalStateException BasicStateを一つも持っていないとき
 	 */
 	public void start() {
 		if(mBasicStateList.size() == 0) {
 			throw new IllegalStateException("基本状態が一つもありません");
 		}
 		
 		mCurState = mBasicStateList.get(0);
 		mHandler.postDelayed(mUpdate, mCurState.getUpdateInterval());
 		
 		mIsStarted = true;
 	}
 	
 	public void stop() {
 		mIsStarted = false;
 	}
 	
 	public void onLongPress() {
 		onUserInteraction(ActionType.LongPress);
 	}
 	
 	public void onSingleTap() {
 		onUserInteraction(ActionType.SingleTap);
 	}
 	
 	public void onDoubleTap() {
 		onUserInteraction(ActionType.DoubleTap);
 	}
 	
 	public void onScroll(float distanceX, float distanceY) {
 		synchronized (mSyncUpdateObj) {
 			if(!mCurState.isAllowInterrupt()) {
 				return;
 			}
 			
 			//XXX まだできていない
 		}
 	}
 	
 	public void onFling(float velocityX, float velocityY) {
 		synchronized (mSyncUpdateObj) {
 			if(!mCurState.isAllowInterrupt()) {
 				return;
 			}
 			
 			//XXX まだできていない
 		}
 	}
 	
 	public void onScrollEnd() {
 		stateChange();
 	}
 	
 	private void onUserInteraction(ActionType action) {
 		synchronized (mSyncUpdateObj) {
 			if(!mCurState.isAllowInterrupt()) {
 				return;
 			}
 			
 			UserInteractionState state = getUserInteractionState(action);
 			if(state != null) {
 				mHandler.removeCallbacks(mUpdate);
 				mStateChange = false;
 				
 				transition(state);
 				
 				state.update();
 				
 				mHandler.postDelayed(mUpdate, state.getUpdateInterval());
 			}
 		}
 	}
 	
 	private UserInteractionState getUserInteractionState(ActionType action) {
 		int max = 0;
 		
 		int[] priority = new int[] {1, 3, 7};
 		
 		for(UserInteractionState state : mInteractionStateList) {
 			if(state.getActionType() == action) {
 				max += priority[state.getEntryPriority().number];
 			}
 		}
 		
 		if(max == 0)
 			return null;
 		
 		int number = mRand.nextInt(max - 1) + 1;
 		for(UserInteractionState state : mInteractionStateList) {
 			if(state.getActionType() == action) {
 				number -= priority[state.getEntryPriority().number];
 				if(number <= 0)
 					return state;
 			}
 		}
 		
 		throw new RuntimeException("get user insteraction failure");
 	}
 	
 	private void transition(MascotState nextState) {
 		if(mCurState != nextState) {
 			forceTransition(nextState);
 		}
 	}
 	
 	private void forceTransition(MascotState nextState) {
 		Rect bounds = new Rect();
 		mCurState.getBounds(bounds);
 		
 		mCurState.exist();
 		
 		mCurState = nextState;
 		
 		Rect entryBounds =  new Rect(bounds);
 		mCurState.entry(entryBounds);
 		
 		//前の状態の領域を消去
 		mView.redraw(bounds.left, bounds.top, bounds.right, bounds.bottom);
 	}
 	
 	@Override public int getViewHeight() {
		return mView.getWidth();
 	}
 	
 	@Override public int getViewWidth() {
		return mView.getHeight();
 	}
 	
 	@Override public void redraw(int left, int top, int right, int bottom) {
 		mView.redraw(left, top, right, bottom);
 	}
 	
 	@Override public void stateChange() {
 		synchronized (mSyncUpdateObj) {
 			mHandler.removeCallbacks(mUpdate);
 			
 			mStateChange = true;
 			
 			mUpdate.run();
 		}
 	}
 	
 	@Override public void showText(String text, Rect mascotBounds) {
 		mView.showText(text, mascotBounds);
 	}
 	
 	@Override public void hideText() {
 		mView.hideText();
 	}
 
 }
