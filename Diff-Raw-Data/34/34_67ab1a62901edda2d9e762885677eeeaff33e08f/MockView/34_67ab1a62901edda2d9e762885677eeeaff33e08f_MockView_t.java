 /**
  * Author: douzifly@gmail.com
  * Date: Jun 26, 2013
  */
 package douzifly.android.springboardfolder4android;
 
 import android.content.Context;
 import android.graphics.Canvas;
import android.graphics.Color;
 import android.graphics.Matrix;
 import android.util.AttributeSet;
 import android.view.View;
 
 /**
  * @author douzifly@gmail.com 
  *
  */
 public class MockView extends View{
 
 	private View mTargetView;
 	
 	private int mClipYOffset;
 	
 	/**
 	 * @param context
 	 * @param attrs
 	 */
 	public MockView(Context context, AttributeSet attrs) {
 		super(context, attrs);
		setBackgroundColor(Color.RED);
 	}
 	
 	
 	public void setTargetView(View v){
 		mTargetView = v;
 	} 
 	
 	Matrix m = new Matrix();
 	@Override
 	public void draw(Canvas canvas) {
 		if(mTargetView != null){
 			canvas.save();
 			m.reset();
 			m.setTranslate(0, -mClipYOffset);
 			canvas.concat(m);
 			mTargetView.draw(canvas);
 			canvas.restore();
 		}
 	}
 	
 	public void setClipYOffset(int clipY){
 		mClipYOffset = clipY;
 	}
 	
 }
