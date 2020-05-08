 package cyberlink.dogtim.horizontalview.widgets;
 
 import cyberlink.dogtim.horizontalview.Project;
 import cyberlink.dogtim.horizontalview.R;
 import cyberlink.dogtim.horizontalview.util.StringUtils;
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Display;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.ViewGroup.LayoutParams;
 
 /**
  * The view which displays the scroll position
  */
 public class PlayheadView extends View {
     private static final String TAG = PlayheadView.class.getSimpleName();
     // Instance variables
     private Paint mLinePaint;
     private Paint mTextPaint;
     private int mTicksHeight;
     // Timeline text size.
     private float mTimeTextSize;
     private int mScreenWidth;
     private TimelineHorizontalScrollView mScrollView;
     private ScrollViewListener mScrollListener;
     private int mScrollX;
     private Project mProject;
     private int MILISECOND_UNIT=1000;
     
     public PlayheadView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         if (!isInEditMode()) {
             final Resources resources = context.getResources();
 
             // Prepare the Paint used to draw the tick marks
             mLinePaint = new Paint();
             mLinePaint
                     .setColor(resources.getColor(R.color.playhead_tick_color));
             mLinePaint.setStrokeWidth(2);
             mLinePaint.setStyle(Paint.Style.STROKE);
 
             // Prepare the Paint used to draw the text
             mTextPaint = new Paint();
             mTextPaint.setAntiAlias(true);
             mTextPaint
                     .setColor(resources.getColor(R.color.playhead_tick_color));
             mTimeTextSize = resources
                     .getDimension(R.dimen.playhead_layout_text_size);
             mTextPaint.setTextSize(mTimeTextSize);
 
             // The ticks height
             mTicksHeight = (int) resources
                     .getDimension(R.dimen.playhead_tick_height);
 
             // Get the screen width
             final Display display = ((WindowManager) context
                     .getSystemService(Context.WINDOW_SERVICE))
                     .getDefaultDisplay();
             final DisplayMetrics metrics = new DisplayMetrics();
             display.getMetrics(metrics);
             mScreenWidth = metrics.widthPixels;
             // Listen to scroll events and repaint this view as needed
             mScrollListener = new ScrollViewListener() {
 
                 @Override
                 public void onScrollBegin(View view, int scrollX, int scrollY, boolean appScroll) {
                 }
 
                 @Override
                 public void onScrollProgress(View view, int scrollX, int scrollY, boolean appScroll) {
                     mScrollX = scrollX;
                     invalidate();
                 }
 
                 @Override
                 public void onScrollEnd(View view, int scrollX, int scrollY, boolean appScroll) {
                     mScrollX = scrollX;
                     invalidate();
                 }
             };
         }
 
 
     }
 
     public PlayheadView(Context context, AttributeSet attrs) {
         this(context, attrs, 0);
     }
 
     public PlayheadView(Context context) {
         this(context, null, 0);
     }
 
     @Override
     protected void onAttachedToWindow() {
         if (!isInEditMode()) {
             final TimelineHorizontalScrollView scrollView = (TimelineHorizontalScrollView) ((View) getParent())
                     .getParent();
             mScrollX = scrollView.getScrollX();
             scrollView.addScrollListener(mScrollListener);
         }
     }
 
     @Override
     protected void onDetachedFromWindow() {
         final TimelineHorizontalScrollView scrollView =
             (TimelineHorizontalScrollView)((View)getParent()).getParent();
         scrollView.removeScrollListener(mScrollListener);
        mScrollView.removeScrollListener(mScrollListener);
     }
 
     /**
      * @param project The project
      */
     public void setProject(Project p) {
         mProject = p;
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
         super.onDraw(canvas);
 
         if (!isInEditMode()) {
             if (mProject == null) {
                Log.e(TAG,"dogtim project" );
                 return;
             }
             
             final long durationMs = mProject.getDuration() * MILISECOND_UNIT;
 
             final int y = (int) -mTextPaint.getFontMetrics().top;
 
            int width = getWidth()+ mScreenWidth;
 /*            if(width < 0){
                 width = mScreenWidth;
             }*/
 
             final long tickMs = 5 * MILISECOND_UNIT;
 
             final float spacing = ((float) (width * tickMs) / (float) durationMs);
             final float startX = 50;
             float startMs = 0;
 
             final Context context = getContext();
             final float endX = mScrollX + mScreenWidth;
            Log.e(TAG,"dogtim getWidth(): "+getWidth());
            Log.e(TAG,"dogtim mScreenWidth: "+mScreenWidth);
            Log.e(TAG,"dogtim width: "+width);
            
            Log.e(TAG,"dogtim startX: "+startX);
            Log.e(TAG,"dogtim endX: "+endX);
            Log.e(TAG,"dogtim spacing: "+spacing);
            Log.e(TAG,"dogtim tickMs: "+tickMs);
             for (float i = startX; i <= endX; i += spacing, startMs += tickMs) {
                 final String timeText = StringUtils.getSimpleTimestampAsString(
                         context, (long) startMs);
                 final int x = (int) (i - mTextPaint.measureText(timeText) / 2);
                 canvas.drawText(timeText, x, y, mTextPaint);
                 canvas.drawLine(i, mTimeTextSize, i, mTimeTextSize
                         + mTicksHeight, mLinePaint);
             }
         }
     }
 }
