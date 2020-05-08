 package bbarm.viewtest.view;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.RectF;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class ArcView extends View {
 
     public interface OnAngleChangedListener {
         public void onChanging(float angle);
         public void onChanged(float angle);
     }
 
     private OnAngleChangedListener listener;
    private Paint paint;
     private Canvas arcCanvas;
     private RectF arcOval = new RectF();
     private int color;
     private float width;
     private float startAngle, endAngle;
     private boolean flipping;
     private int flipUnit;
 	
 	public ArcView(Context context) {
 		this(context, null);
 	}
 
 	public ArcView(Context context, AttributeSet attrs) {
 		this(context, attrs, 0);
 	}
 
 	public ArcView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 
 	private void init() {
 		paint = new Paint();
 		paint.setAntiAlias(true);
 		paint.setStyle(Paint.Style.STROKE);
 
 	}
 
 	public void setColor(int color) {
         this.color = color;
 		paint.setColor(color);
 	}
 
 	public void setStrokeWidth(float width) {
         this.width = width;
 		paint.setStrokeWidth(width);
 	}
 
     public float getStrokeWidth() {
         return this.width;
     }
 
 	public void setOvalSize(float left, float top, float right, float bottom) {
 		arcOval.set(left, top, right, bottom);
 	}
 	
 	public void setStartAngle(float startAngle) {
 		this.startAngle = startAngle;
 	}
 
    public Canvas getCanvas() {
 		return arcCanvas;
 	}
 	
 	public void setEndAngle(float endAngle){
 		this.endAngle = endAngle;
 	}
 	
 	public float getEndAngle(){
 		return this.endAngle;
 	}
 	
 	public RectF getOval() {
 		return arcOval;
 	}
 
 	public Paint getPaint() {
 		return paint;
 	}
 
     public void setFlippingEnabled(boolean flipping) {
         this.flipping = flipping;
     }
 
     public void setFlippingUnit(int unit) {
         this.flipUnit = unit;
     }
 
     public boolean isFlipping() {
         return this.flipping;
     }
 
     public int getFlippingUnit() {
         return this.flipUnit;
     }
 
     public void setOnAngleChangedListener(OnAngleChangedListener listener) {
         this.listener = listener;
     }
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 
 		arcCanvas = canvas;
 		float sweep = this.getEndAngle() - startAngle;
 		if (sweep > 360){
 			sweep -= 360;
 		}
 		arcCanvas.drawArc(arcOval, startAngle, sweep, false, paint);
 		
 	}
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         switch (event.getAction()) {
             case MotionEvent.ACTION_DOWN:
             case MotionEvent.ACTION_MOVE:
                 float x = event.getX();
                 float y = event.getY();
                 float centerX = getWidth() / 2;
                 float centerY = getHeight() / 2;
 
                 float relativeX = x - centerX;
                 float relativeY = y - centerY;
 
                 double cos = relativeX
                         / Math.sqrt(Math.pow(relativeX, 2)
                         + Math.pow(relativeY, 2));
                 double rad = Math.acos(cos);
                 rad = relativeY > 0 ? rad : Math.PI * 2 - rad;
                 double angle = rad * 180 / Math.PI;
 
                 setEndAngle((float) angle);
                 invalidate();
 
                 if(listener != null) {
                     listener.onChanging((float) angle);
                 }
 
                 break;
 
             case MotionEvent.ACTION_UP:
                 setStrokeWidth(width);
                 float touchUpAngle = getEndAngle();
                 float targetAngle = touchUpAngle;
 
                 if(flipping) {
                     float flipToAngle = touchUpAngle - touchUpAngle % 30;
 
                     if(touchUpAngle % 30 >= 15) {
                         flipToAngle = flipToAngle + 30;
                     }
 
                     targetAngle = flipToAngle;
                 }
 
                 setEndAngle(targetAngle);
                 invalidate();
 
                 if(listener != null) {
                     listener.onChanged(targetAngle);
                 }
 
                 break;
         }
         return true;
     }
 }
