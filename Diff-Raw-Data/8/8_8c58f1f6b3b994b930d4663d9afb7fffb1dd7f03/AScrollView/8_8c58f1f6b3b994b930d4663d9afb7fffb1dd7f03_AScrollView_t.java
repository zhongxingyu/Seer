 package net.winterroot.hzsv;
 
 import net.winterroot.horizonalzoomingscrollview.R;
 import android.content.Context;
 import android.graphics.Matrix;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.widget.HorizontalScrollView;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 
 
 public class AScrollView extends HorizontalScrollView {
 
 	public AScrollView(Context context) {
 		super(context);
 		// TODO Auto-generated constructor stub
 	}
 
 	public AScrollView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		// TODO Auto-generated constructor stub
 	}
 
 	public AScrollView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		// TODO Auto-generated constructor stub
 	}
 	
 	@Override
 	public void fling (int velocityY)
 	{
 	    /*Scroll view is no longer gonna handle flings
 	     * super.fling(velocityY);
 	    */
 	}
 
 
 	@Override
 	protected void onLayout (boolean changed, int left, int top, int right, int bottom){
 		super.onLayout(changed, left, top, right, bottom);
 //		adjustImageSizes();
 	}
 
 	
 	@Override
 	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
 		super.onScrollChanged(l, t, oldl, oldt);
 		//Log.v("SCROLL", String.valueOf(l));
 
 		adjustImageSizes();
 		
 	}
 	
 	private void adjustImageSizes(){
 		
 		int width = getWidth();
 		int[] views =  { R.id.imageView1, R.id.imageView2, R.id.imageView3, R.id.imageView4, R.id.imageView5 };
 		for(int r : views){
 			ImageView iv = (ImageView) findViewById(r);
 			int[] location = new int[2];
 			iv.getLocationOnScreen(location);
 
 			int imageViewCenter = location[0] + 108 / 2; // This can be calculated for each imageView that is on screen
 			
 			if(imageViewCenter < 0){
 				imageViewCenter = 0;
 			}
 			if(imageViewCenter > width){
 				imageViewCenter = width;
 			}
 
 			int scrollerCenter = (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
 			float offset = Math.abs(scrollerCenter - imageViewCenter);
 
 			float ratio = ( offset / ( width / 2)  );
 
 			// Scale the size.
 			float scale = .5f + (1.0f - ratio) / 2;
 			if (scale <= 0) {
 				scale = 0.1f;
 			}
 			
 		
 			if(r == R.id.imageView5){
 				//Log.v("LOCATION", String.valueOf(imageViewCenter) );
 				//Log.v("OFFSET", String.valueOf(offset));
 				//Log.v("RATIO", String.valueOf(ratio));
 				Log.v("SCALE", String.valueOf(scale));
 				//Log.v("DIMS", String.valueOf(iWidth) + ":" + String.valueOf(iHeight) );
 			}
 			
 			// Compute the matrix
 			Matrix m = new Matrix();
 			m.reset();
 
 			// Middle of the image should be the scale pivot
 			m.postScale(scale, scale);
 			
 			//center in frame.
 			//m.postTranslate()
 			
 			iv.setScaleType(ScaleType.MATRIX);
 			iv.setImageMatrix(m);
 			
 			//LinearLayout.LayoutParams layoutParams = (android.widget.LinearLayout.LayoutParams) iv.getLayoutParams();
 			//layoutParams.height = iHeight;
 			//layoutParams.width = iWidth;
 			//iv.setLayoutParams(layoutParams);
 			//iv.getLayoutParams().height = iHeight;
 			//iv.getLayoutParams().width = iWidth;
 		}
 	}
 
 	    
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 	    boolean result = super.onTouchEvent(event);
 
 	    switch(event.getAction()) {
 	        case(MotionEvent.ACTION_DOWN):
 	            break;
 	        case(MotionEvent.ACTION_UP):
	        	
 	        	int scrollX = getScrollX();
	        	int page = scrollX / 108;
	        	int offset = page * 108;
	            this.smoothScrollTo(offset, 0);
	            
 	            break;
 	    }
 
 	    return result;
 	}
 
 	
 }
