 package cyberlink.dogtim.horizontalview;
 
 import java.util.ArrayList;
 
 import cyberlink.dogtim.horizontalview.widgets.PlayheadView;
 import cyberlink.dogtim.horizontalview.widgets.ScrollViewListener;
 import cyberlink.dogtim.horizontalview.widgets.TimelineHorizontalScrollView;
 import cyberlink.dogtim.horizontalview.widgets.TimelineRelativeLayout;
 
 import android.os.Bundle;
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.animation.LayoutTransition;
 import android.animation.ObjectAnimator;
 import android.app.Activity;
 import android.content.ClipData;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Display;
 import android.view.DragEvent;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 
 public class Main extends Activity {
     private static final String TAG = Main.class.getSimpleName();
 
     private LinearLayout mMaterialLayout;
     private LinearLayout mPhotoTrackLayout;
     private TimelineRelativeLayout mTimelineLayout;
     private TimelineHorizontalScrollView mTHSView;
     private PlayheadView mPlayheadView;
     
     private View mFakeView = null;
     private boolean mIsFakingMode = false;
     private int mFakeX = -1;
     private int mFakeY = -1;
     private Item selectItem = null;
     
     private Project mProject;
     
     private View.OnDragListener mDragListener = new View.OnDragListener() {
         public boolean onDrag(View v, DragEvent event) {
             final int action = event.getAction();
             Item item = selectItem;
             Item target = (Item) v.getTag();
 
             if (target == null)
                 return true;
             switch (action) {
                 case DragEvent.ACTION_DRAG_STARTED:
                     Log.d(TAG,"onDrag ACTION_DRAG_STARTED");
                     break;
                 
                 case DragEvent.ACTION_DRAG_ENTERED:
                     
                     if(target.type == ItemType.EditingItem && !mIsFakingMode){
                         int[] location = new int[2];
                         v.getLocationInWindow(location);
                         mFakeX = location[0];
                         mFakeY = location[1];
                         int id = mPhotoTrackLayout.indexOfChild(v);
                         mFakeView = insertPhotoToTimeLine(item.path);
                         mFakeView.setAlpha((float)0.3);
                         mFakeView.setTag(new Item("fakePath", ItemType.FakeItem));
                         mPhotoTrackLayout.addView(mFakeView,id);
                        mProject.addEditingItem(id, item);
                         measureTimeLineWidth();
                         mTimelineLayout.invalidate();
                         mIsFakingMode = true;
                         return false;
                     }
                     Log.d(TAG,"onDrag ACTION_DRAG_ENTERED");
                     break;
                 case DragEvent.ACTION_DRAG_LOCATION:
                     if(mIsFakingMode && target.type == ItemType.WindowItme){
                         int eventX = (int )event.getX();
                         int eventY = (int )event.getY();
                         if(mFakeX < eventX && (mFakeX+mFakeView.getWidth()) > eventX){
                             if(mFakeY < eventY && (mFakeY+mFakeView.getHeight()) > eventY){
                                 return true;
                             }
                         }
                        mProject.removeEditingItem(mPhotoTrackLayout.indexOfChild(mFakeView));
                         mPhotoTrackLayout.removeView(mFakeView);
                         measureTimeLineWidth();
                         mTimelineLayout.invalidate();
                         resetFake();
                     }
                     Log.d(TAG,"onDrag ACTION_DRAG_LOCATION");
                     break;
                 case DragEvent.ACTION_DROP:
                     if(target.type == ItemType.WindowItme && mIsFakingMode){
                         mFakeView.setAlpha((float)1);
                         Item itemTag = (Item) mFakeView.getTag();
                         itemTag.type = ItemType.EditingItem;
                         measureTimeLineWidth();
                         mTimelineLayout.invalidate();
                         resetFake();
                     }
                     Log.d(TAG,"onDrag ACTION_DROP");
                     break;
                 case DragEvent.ACTION_DRAG_EXITED:
                     Log.d(TAG,"onDrag ACTION_DRAG_EXITED");
                     break;
                 case DragEvent.ACTION_DRAG_ENDED:
                     Log.d(TAG,"onDrag ACTION_DRAG_ENDED");
                     break;
                 default:
                     Log.d(TAG,"onDrag default");
                     break;
             }
             return true;
         }
     };
     
     private void resetFake(){
         mIsFakingMode = false;
         mFakeView=null;
         mFakeX = 0;
         mFakeY = 0;
     }
     
     private View.OnLongClickListener mLongClickListener = new View.OnLongClickListener() {
         public boolean onLongClick(View v) {
             ClipData data = null;//ClipData.newPlainText("dot", "Dot : " + v.toString());
             View.DragShadowBuilder shadowView = new View.DragShadowBuilder (v);
             selectItem = (Item) v.getTag();
             v.startDrag(data, shadowView,(Object)v, 0);
             return true;
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         mMaterialLayout = (LinearLayout) findViewById(R.id.imageLayout);
         mPhotoTrackLayout = (LinearLayout) findViewById(R.id.photo_track);
         mTimelineLayout = (TimelineRelativeLayout)findViewById(R.id.timeline);
         mPlayheadView = (PlayheadView)findViewById(R.id.timeline_playhead );
         mTHSView = (TimelineHorizontalScrollView)findViewById(R.id.timeline_scroller);
         getWindow().getDecorView().getRootView().setOnDragListener(mDragListener);
         getWindow().getDecorView().getRootView().setTag(new Item("window", ItemType.WindowItme));
         
         mProject = Project.get();
         mPlayheadView.setProject(mProject);
         initAnimation();
         prepareMaterial();
         measureTimeLineWidth();
     }
     
     /**
      * Animation codes from Api Demos project <p>
      * com.example.android.apis.animation.LayoutAnimations.java
      */
     private void initAnimation(){
         final LayoutTransition transitioner = new LayoutTransition();
         mPhotoTrackLayout.setLayoutTransition(transitioner);
         Animator customAppearingAnim;
         // Adding animation
         customAppearingAnim = ObjectAnimator.ofFloat(null, "rotationY", 90f, 0f).
                 setDuration(transitioner.getDuration(LayoutTransition.APPEARING));
         customAppearingAnim.addListener(new AnimatorListenerAdapter() {
             public void onAnimationEnd(Animator anim) {
                 View view = (View) ((ObjectAnimator) anim).getTarget();
                 view.setRotationY(0f);
             }
         });
         transitioner.setAnimator(LayoutTransition.APPEARING, customAppearingAnim);
     }
     
     private void prepareMaterial(){
         MediaStoreHelper mediaStoreHelper = new MediaStoreHelper(getApplicationContext());
         ArrayList<String> Files_string = mediaStoreHelper.getImages();
         mPhotoTrackLayout.addView(insertPhotoToTimeLine(Files_string.get(0)));
         for (String file : Files_string) {
             mMaterialLayout.addView(insertPhotoToMaterial(file));
             //mPhotoTrackLayout.addView(insertPhotoToTimeLine(file));
         }
     }
     /**
      *   This is a workaround to get measure spec because <p>
      *   we can not get correct value in onCreate function. <p>
      *   @see <a href="http://www.eoeandroid.com/thread-240677-1-1.html">getWidth() == 0</a>
      */
     private void measureTimeLineWidth(){
         // Get the screen width
         final Display display = ((WindowManager) this
                 .getSystemService(Context.WINDOW_SERVICE))
                 .getDefaultDisplay();
         final DisplayMetrics metrics = new DisplayMetrics();
         display.getMetrics(metrics);
         final int screenWidth = metrics.widthPixels;
         
         int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
         int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
         mPhotoTrackLayout.measure(w, h); 
         final int childrenCount = mTimelineLayout.getChildCount();
         for (int i = 0; i < childrenCount; i++) {
             final View child = mTimelineLayout.getChildAt(i);
             final ViewGroup.LayoutParams lp = child.getLayoutParams();
             child.measure(w, h);
             lp.width = child.getMeasuredWidth();
             if(screenWidth > lp.width)
                 lp.width = screenWidth;
             if(mPhotoTrackLayout.getMeasuredWidth() > lp.width)
                 lp.width = mPhotoTrackLayout.getMeasuredWidth();
             child.setLayoutParams(lp);
         }
     }
     
     private ImageView createImageView(String path){
         
         Bitmap bm = BitmapHelper.decodeSampledBitmapFromUri(path, 40, 40);
         ImageView imageView = new ImageView(getApplicationContext());
         imageView.setLayoutParams(new LayoutParams(220, 220));
         imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
         imageView.setImageBitmap(bm);
         
         imageView.setOnDragListener(mDragListener);
         imageView.setLongClickable(true);
         imageView.setOnLongClickListener(mLongClickListener);
         return imageView;
     }
     
     private View insertPhotoToMaterial(String path) {
         LinearLayout layout = new LinearLayout(getApplicationContext());
         layout.setLayoutParams(new LayoutParams(250, 250));
         layout.setGravity(Gravity.CENTER);
         ImageView imageView = createImageView(path);
         /* TODO 
          * maybe we could store object in setTag to split different object
          * for variety of scenario using.
          * */
         imageView.setTag(new Item(path, ItemType.OriginalItem));
         layout.addView(imageView);
         return layout;
     }
 
     private View insertPhotoToTimeLine(String path) {
         ImageView imageView = createImageView(path);
         /* TODO 
          * maybe we could store object in setTag to split different object
          * for variety of scenario using.
          * */
         imageView.setTag(new Item(path,ItemType.EditingItem));
         return imageView;
     }
 }
