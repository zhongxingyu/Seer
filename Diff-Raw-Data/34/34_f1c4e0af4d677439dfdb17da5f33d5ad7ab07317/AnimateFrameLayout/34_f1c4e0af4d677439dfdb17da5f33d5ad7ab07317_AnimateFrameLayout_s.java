 package carnero.me.view;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.widget.FrameLayout;
 import carnero.me.R;
 
 @SuppressWarnings("unused")
 public class AnimateFrameLayout extends FrameLayout implements IAnimateView {
 
 	private AnimateTouchProcessor mProcessor;
 	private boolean mAnimationEnabled = true;
 
 	public AnimateFrameLayout(Context context) {
 		super(context);
 		init(context);
 	}
 
 	public AnimateFrameLayout(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init(context);
 	}
 
 	public AnimateFrameLayout(Context context, AttributeSet attrs, int style) {
 		super(context, attrs, style);
 		init(context);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
		mProcessor.processTouch(this, event);
 
 		return super.onTouchEvent(event);
 	}
 
 	private void init(Context context) {
 		mProcessor = new AnimateTouchProcessor(context, R.anim.card_down, R.anim.card_up);
 	}
 
 	public void setAnimationEnabled(boolean enabled) {
 		mAnimationEnabled = enabled;
 	}
 
 	public void resetAnimation() {
 		mProcessor.reset(this);
 	}
 }
