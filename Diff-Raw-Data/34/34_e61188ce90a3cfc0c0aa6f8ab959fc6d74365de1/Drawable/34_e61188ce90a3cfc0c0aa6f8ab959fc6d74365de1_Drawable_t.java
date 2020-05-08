 package com.realapps.engine.core.drawable;
 
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.PorterDuffColorFilter;
 import android.graphics.PorterDuffXfermode;
 
 import com.realapps.engine.core.animation.Animator;
 import com.realapps.engine.core.scene.SceneManager;
 
 public abstract class Drawable {
 	protected abstract void update();
 	protected abstract void render(Canvas canvas);
 	
 	public static abstract class Builder {
 		protected String 	mID 		= "";
 
 		protected boolean	mShow		= true;
 		protected boolean	mUIMode		= false;
 		
 		protected int 		mX 			= 0;
 		protected int 		mY 			= 0;
 		
 		protected short 	mPriority 	= 0;
 		
 		protected float 	mRotate 	= 0.0f;
 		
 		protected int 		mOffsetX 	= 0;
 		protected int 		mOffsetY 	= 0;
 
 		protected int		mAlpha		= 0xFF;
 		protected int 		mColor 		= 0x00000000;
 		protected int		mColorFilter= -1;
 		protected Mode		mXferMode	= null;
 		
 		public Builder setPosition(int x, int y) {
 			mX = x;
 			mY = y;
 			return this;
 		}
 		public Builder setPriority(int priority) {
 			mPriority = (short)priority;
 			return this;
 		}
 		public Builder setRotate(float rotate) {
 			mRotate = rotate;
 			return this;
 		}
 
 		public Builder setColor(int color) {
 			mColor = color;
 			return this;
 		}
 		public Builder setAlpha(int alpha) {
 			mAlpha = alpha;
 			return this;
 		}
 		public Builder setColorFilter(int color) {
 			mColorFilter = color;
 			return this;
 		}
 		public Builder setXferMode(Mode mode) {
 			mXferMode = mode;
 			return this;
 		}
 		public Builder setHide() {
 			mShow = false;
 			return this;
 		}
 		public Builder setUIMode() {
 			mUIMode = true;
 			return this;
 		}
 		public abstract Drawable build(String id);
 	}
 	
 	protected String mID = "";
 	protected Drawable(Builder srcBuilder) {
 		mID = srcBuilder.mID;
 		
 		// Build Paint
 		mPaint = new Paint();
 		mPaint.setAntiAlias(true);
 		mPaint.setDither(true);
 		
 		mShow = srcBuilder.mShow;
 
 		setPriority(srcBuilder.mPriority);
 		
 		mRotate = srcBuilder.mRotate;
 		
 		mX = srcBuilder.mX;
 		mY = srcBuilder.mY;
 		
 		mOffsetX = srcBuilder.mOffsetX;
 		mOffsetY = srcBuilder.mOffsetY;
 		
 		setColor(srcBuilder.mColor);
 		setAlpha(srcBuilder.mAlpha);
 		setColorFilter(srcBuilder.mColorFilter);
 		setXferMode(srcBuilder.mXferMode);
 		
 		if(!srcBuilder.mUIMode)
 			SceneManager.getCurrentScene().getDrawableManager().add(this);
 	}
 
 	public abstract void release();
 	
 	public String getId() {
 		return mID;
 	}
 
 	/*
 	 * Drawable Paint
 	 */
 	protected Paint mPaint = null;
 	
 	/*
 	 * Drawable Show
 	 */
 	protected boolean mShow = true;
 	public void show() {
 		mShow = true;
 	}
 	public void hide() {
 		mShow = false;
 	}
 	public boolean isShow() {
 		return mShow;
 	}
 	
 	/*
 	 * Drawable Matrix
 	 */
 	private Matrix mMatrix = new Matrix();
 	private void updateMatrix() {
 		mMatrix = new Matrix();
 		mMatrix.postTranslate(mOffsetX, mOffsetY);
		mMatrix.setScale(mScaleX, mScaleY);
 		mMatrix.postRotate(mRotate);
 	}
 	
 	/*
 	 * Drawable Position
 	 */
 	protected int mX = 0;
 	protected int mY = 0;
 	public void setPosition(int x, int y) {
 		mX = x;
 		mY = y;
 	}
 	public int getX() {
 		return mX;
 	}
 	public int getY() {
 		return mY;
 	}
 	
 	/*
 	 * Drawable Priority
 	 */
 	protected int mPriority = 0;
 	public void setPriority(int priority) {
 		mPriority = priority;
 		DrawableManager.sync();
 	}
 	public int getPriority() {
 		return mPriority;
 	}
 	
 	/*
 	 * Drawable Size
 	 */
 	protected int mDrawableWidth 	= 0;
 	protected int mDrawableHeight 	= 0;
 	protected void setSize(int width, int height) {
 		mDrawableWidth 	= width;
 		mDrawableHeight = height;
 	}
 	public int getWidth() {
 		return mDrawableWidth;
 	}
 	public int getHeight() {
 		return mDrawableHeight;
 	}
 	
 	/*
	 * Drawable Scale
	 */
	protected float mScaleX = 1.0f;
	protected float mScaleY = 1.0f;
	public void setScale(float scale) {
		setScale(scale, scale);
	}
	public void setScale(float scaleX, float scaleY) {
		mScaleX = scaleX;
		mScaleY = scaleY;
		
		updateMatrix();
	}
	public float getScaleX() {
		return mScaleX;
	}
	public float getScaleY() {
		return mScaleY;
	}
	
	/*
 	 * Drawable Rotate
 	 */
 	protected float mRotate = 0.0f;
 	public void setRotate(float degree) {
 		mRotate = degree;
 
 		updateMatrix();
 	}
 	public float getRotate() {
 		return mRotate;
 	}
 	
 	/*
 	 * Drawable Offset
 	 */
 	protected int mOffsetX = 0;
 	protected int mOffsetY = 0;
 	public void setOffset(int x, int y) {
 		mOffsetX = -x;
 		mOffsetY = -y;
 		
 		updateMatrix();
 	}
 	public void setOffsetToCenter() {
 		setOffset(getWidth()/2, getHeight()/2);
 	}
 	
 	public int getOffsetX() {
 		return mOffsetX;
 	}
 	public int getOffsetY() {
 		return mOffsetY;
 	}
 	
 	/*
 	 * Drawable Animation Listener
 	 */
 	public void onAnimationFinish(int animID) {
 		mAnimate = null;
 		SceneManager.getCurrentScene().onAnimationFinish(this.getId(), animID);
 	}
 	
 	/*
 	 * Drawable Color
 	 */
 	public void setColor(int color) {
 		mPaint.setColor(color);
 	}
 	public void setAlpha(int alpha) {
 		if(alpha > 255) alpha = 255;
 		else if(alpha < 0) alpha = 0;
 		mPaint.setAlpha(alpha);
 	}
 	public void setColorFilter(int color) {
 		setColorFilter(color, PorterDuff.Mode.MULTIPLY);
 	}
 	public void setColorFilter(int color, Mode mode) {
 		if(color == -1) mPaint.setColorFilter(null);	
 		else mPaint.setColorFilter(new PorterDuffColorFilter(color, mode));
 	}
 	public void setXferMode(PorterDuff.Mode mode) {
 		if(mode == null) mPaint.setXfermode(null);
 		else mPaint.setXfermode(new PorterDuffXfermode(mode));
 	}
 	
 	public int getColor() {
 		return mPaint.getColor();
 	}
 	public int getAlpha() {
 		return mPaint.getAlpha();
 	}
 	
 	/*
 	 * Drawable Animation
 	 */
 	protected Animator mAnimate = null;
 	public void setAnimation(Animator animator) {
 		mAnimate = animator;
 	}
 	
 	public void animation(long nowTime) {
 		if(mAnimate != null)
 			mAnimate.animate(this, nowTime);
 	}
 	
 	/*
 	 * Drawable Rendering
 	 */
 	public void draw(Canvas canvas) {
 		animation(System.currentTimeMillis());
 		if(mShow) {
 			update();
 
 			canvas.save();
 			
 			canvas.concat(mMatrix);
 			render(canvas);
 			
 			canvas.restore();
 		}
 	}
 }
