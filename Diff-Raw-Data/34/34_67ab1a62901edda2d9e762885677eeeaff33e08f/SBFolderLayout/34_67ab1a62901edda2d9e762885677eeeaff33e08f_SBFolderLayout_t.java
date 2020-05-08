 /**
  * Author:douzifly@gmail.com
  * Date: Jun 25, 2013
  * 
  */
 package douzifly.android.springboardfolder4android;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.DecelerateInterpolator;
 import android.view.animation.TranslateAnimation;
 import android.widget.FrameLayout;
 
 /**
  * 
  * SBFolderLayout is iOS SpringBoard folder implementation for Android
  * 
  * @author douzifly 
  *
  */
 public class SBFolderLayout extends FrameLayout{
 	
 	final static String TAG = "SBFolderLayout" ;
 
 	private View mFolderView;
 	private View mContentView;
 	
 	/**
 	 * view which contains cover view and mock content view
 	 */
 	private FrameLayout mTopContainer;
 	private FrameLayout mFolderContainer; 
 	private FrameLayout mContentContainer;
 	private MockView mMockView;
 	
 	/**
 	 * @param context
 	 * @param attrs
 	 */
 	public SBFolderLayout(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		LayoutInflater infl = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		infl.inflate(R.layout.sbf_layout, this);
 		View sbfLayout = findViewById(R.id.sbf_layout);
 		mTopContainer = (FrameLayout)sbfLayout.findViewById(R.id.sbf_top_container);
 		mFolderContainer = (FrameLayout)sbfLayout.findViewById(R.id.sbf_folder_container);
 		mContentContainer = (FrameLayout) sbfLayout.findViewById(R.id.sbf_content_container);
 		mMockView = (MockView)sbfLayout.findViewById(R.id.sbf_mock_view);
 		mTopContainer.setVisibility(View.GONE);
 		
 		mMockView.setOnTouchListener(new OnTouchListener() {
 			
 			@Override
 			public boolean onTouch(View arg0, MotionEvent arg1) {
 				
 				// close folder when touch on mock view 
 				if(arg1.getY() > mFolderHeight){
 					hideFolderView();
 					return true;
 				}
 				return false;
 			}
 		});
 	}
 	
 	
 	/**
 	 * set content view , notice that content view must have background
 	 * @param contentView
 	 */
 	public void setContentView(View contentView){
 		mContentView = contentView;
 		if(contentView.getLayoutParams() == null){
 			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 			contentView.setLayoutParams(lp);
 		}
 		mMockView.setTargetView(contentView);
 		mContentContainer.addView(contentView);
 	}
 	
 	/**
 	 * if folderView's root element is LinearLayout, the view height may not be measured
 	 * @param folderView
 	 * @param lp
 	 */
 	public void setFolderView(View folderView, LayoutParams lp){
 		mFolderView = folderView;
 		mFolderContainer.addView(folderView, lp);
 		folderView.setOnTouchListener(new OnTouchListener() {
 			
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				return true;
 			}
 		});
 	}
 	
 	/**
 	 * if folderView's root element is LinearLayout, the view height may not be measured
 	 * @param resId
 	 * @return
 	 */
 	public View setFolderView(int resId){
 		LayoutInflater infl = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		infl.inflate(resId, mFolderContainer);
 		mFolderView = mFolderContainer.getChildAt(0);
 		mFolderView.setOnTouchListener(new OnTouchListener() {
 			
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				return true;
 			}
 		});
 		return mFolderView;
 	}
 	
 	
 	Animation mAnimMockViewDown;
 	Animation mAnimMockViewUp;
 	Animation mAnimTotalUp;
 	Animation mAnimTotalDown;
 	boolean mShowing = false;
 	boolean mHiding = false;
 	
 	int mFolderHeight = 0;
	int mFolderOffsetY = 0;
 	// mockView slide down then folder view disappeared.
 	public void showFolderView(int y){
 		if(mFolderView == null){
 			return;
 		}
 		
 		if(isShowFolderView()){
 			Log.d(TAG, "showing");
 			return;
 		}
 		
 		if(mHiding || mShowing){
 			return;
 		}
 		
		mFolderOffsetY = y;
 		// measure height of folder view
 		mFolderView.measure(getWidth(), getHeight());
 		mFolderHeight = mFolderView.getMeasuredHeight();
 		Log.d(TAG, "folderHeight:" + mFolderHeight);
 		
		// set y offset of top container
		mTopContainer.scrollTo(0, -y);
 		// set mock view clip offset
 		mMockView.setClipYOffset(y);
 		
 		mTopContainer.setVisibility(View.VISIBLE);
 		if(mAnimMockViewDown == null){
 			mAnimMockViewDown = new TranslateAnimation(0, 0, 0, mFolderHeight);
 			mAnimMockViewDown.setDuration(400);
 			mAnimMockViewDown.setInterpolator(new AccelerateInterpolator());
 			mAnimMockViewDown.setFillAfter(true);
 			mAnimMockViewDown.setAnimationListener(new AnimationListener() {
 				
 				@Override
 				public void onAnimationStart(Animation arg0) {
 					mShowing = true;
 				}
 				
 				@Override
 				public void onAnimationRepeat(Animation arg0) {
 				}
 				
 				@Override
 				public void onAnimationEnd(Animation arg0) {
 					mShowing = false;
 				}
 			});
 		}
 		
 		if(mAnimTotalUp == null){
 			mAnimTotalUp = new TranslateAnimation(0, 0, 0, -mFolderHeight);
 			mAnimTotalUp.setDuration(400);
 			mAnimTotalUp.setInterpolator(new AccelerateInterpolator());
 			mAnimTotalUp.setFillAfter(true);
 		}
 		
 		mMockView.startAnimation(mAnimMockViewDown);
 	}
 	
 	public void hideFolderView(){
 		if(mFolderView == null) return;
 		
 		if(mAnimMockViewDown != null && !mAnimMockViewDown.hasEnded()){
 			return;
 		}
 		
 		if(mShowing || mHiding){
 			return;
 		}
 		
 		if(mAnimMockViewUp == null){
 			mAnimMockViewUp = new TranslateAnimation(0, 0, mFolderHeight, 0);
 			mAnimMockViewUp.setDuration(400);
 			mAnimMockViewUp.setInterpolator(new DecelerateInterpolator());
 			mAnimMockViewUp.setFillAfter(true);
 			mAnimMockViewUp.setAnimationListener(new AnimationListener() {
 				
 				@Override
 				public void onAnimationStart(Animation arg0) {
 					mHiding = true;
 				}
 				
 				@Override
 				public void onAnimationRepeat(Animation arg0) {
 				}
 				
 				@Override
 				public void onAnimationEnd(Animation arg0) {
 					// hide top container when animation finished
 					mTopContainer.setVisibility(View.GONE);
 					mHiding = false;
 				}
 			});
 		}
 		
 		if(mAnimTotalDown == null){
 			mAnimTotalDown = new TranslateAnimation(0, 0, -mFolderHeight, 0);
 			mAnimTotalDown.setDuration(400);
 			mAnimTotalDown.setInterpolator(new AccelerateInterpolator());
 			mAnimTotalDown.setFillAfter(true);
 		}
 		
 		mMockView.startAnimation(mAnimMockViewUp);
 	}
 	
 	public boolean isShowFolderView(){
 		return mTopContainer == null ? false : mTopContainer.getVisibility() == View.VISIBLE;
 	}
 	
 	@Override
 	public boolean onInterceptTouchEvent(MotionEvent ev) {
 		// close folder when touch section above folder when folder on
 		if(isShowFolderView()){
			if(ev.getY() < mFolderOffsetY){
 				return true;
 			}
 		}
 		return super.onInterceptTouchEvent(ev);
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent ev) {
		// close folder when touch section above folder when folder on
 		if(isShowFolderView()){
			if(ev.getY() < mFolderOffsetY){
 				if(ev.getAction() == MotionEvent.ACTION_UP){
 					hideFolderView();
 				}
 				return true;
 			}
 		}
 		return super.onTouchEvent(ev);
 	}
 }
