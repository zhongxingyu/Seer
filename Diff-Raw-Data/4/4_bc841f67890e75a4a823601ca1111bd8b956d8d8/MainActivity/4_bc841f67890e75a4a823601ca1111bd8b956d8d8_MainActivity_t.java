 package vit.android.test.height.animation;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import vit.android.test.height.animation.ChangeYSizeAnimation.HeightSpec;
 import vit.android.test.height.animation.ChangeYSizeAnimation.MarginSpec;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.MeasureSpec;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.AccelerateDecelerateInterpolator;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
     private static final String TAG = MainActivity.class.getSimpleName();
 
     private ViewGroup container;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         container = (ViewGroup) findViewById(R.id.container);
         
         Button addBtn    = (Button) findViewById(R.id.add_btn);
         Button removeBtn = (Button) findViewById(R.id.remove_btn);
         
         addBtn.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 add();
             }
         });
         
         removeBtn.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 remove();
             }
         });
     }
 
     private void remove() {
         log("remove");
         
         int size = container.getChildCount();
         
         final List<View> views = new ArrayList<View>(size);
         
         for (int i = size - 1; i >= 0; i--) {
             View v = container.getChildAt(i);
             views.add(v);
         }
         
         collapse(views);
     }
     
     private void collapse(final List<View> views) {
         final Runnable finisher = new Runnable() {
             @Override
             public void run() {
                 for (View v : views) {
                     container.removeView(v);
                 }
             }
         };
         
         MarginSpec marginSpec = new MarginSpec(0, 0);
         HeightSpec heightSpec = new HeightSpec(0);
         
         AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
         
         for (int i = views.size() - 1; i >= 0; i--) {
             View v = views.get(i);
             ChangeYSizeAnimation animation = new ChangeYSizeAnimation(v, heightSpec, marginSpec);
             animation.setInterpolator(interpolator);
             animation.setDuration(500);
             if (i == 0) {
                 // the last one
                 animation.setAnimationListener(new AnimationListener() {
                     @Override
                     public void onAnimationStart(Animation animation) {}
                     
                     @Override
                     public void onAnimationRepeat(Animation animation) {}
                     
                     @Override
                     public void onAnimationEnd(Animation animation) {
                         container.post(finisher);
                     }
                 });
             }
             v.startAnimation(animation);
         }
     }
 
     private void add() {
         log("add");
         
         int size = container.getChildCount();
         
         final List<View> views = new ArrayList<View>(size);
         
         // add 2 new views
         for (int i = size; i < (size + 2); i++) {
             View v = createView("Comment #" + i);
             views.add(v);
         }
         
         expand(views);
     }
 
     private void expand(final List<View> views) {
         AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
         
         for (View v : views) {
             container.addView(v); // view height is still 0 at this point
             
             int targetHeight = getTargetHeight(v); // this is a trick to get future height
             
             ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
             
             layoutParams.height = 0;
             
             int targetTopMargin    = layoutParams.topMargin;
             int targetBottomMargin = layoutParams.bottomMargin;
             
             layoutParams.topMargin    = 0;
             layoutParams.bottomMargin = 0;
             
             MarginSpec marginSpec = new MarginSpec(targetTopMargin, targetBottomMargin);
             HeightSpec heightSpec = new HeightSpec(targetHeight);
             
             ChangeYSizeAnimation animation = new ChangeYSizeAnimation(v, heightSpec, marginSpec);
             animation.setInterpolator(interpolator);
             animation.setDuration(500);
             
             v.startAnimation(animation);
         }
     }
     
     private View createView(String label) {
         TextView view = new TextView(this);
         
         view.setText(label);
         
         LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
             LinearLayout.LayoutParams.FILL_PARENT,
             LinearLayout.LayoutParams.WRAP_CONTENT
         );
         
         layoutParams.bottomMargin = 5;
         layoutParams.topMargin    = 5;
         
         view.setLayoutParams(layoutParams);
         
         view.setBackgroundResource(R.drawable.view_bg);
         
         return view;
     }
     
     private int getTargetHeight(View v) {
         // this trick is found at http://stackoverflow.com/questions/4946295/android-expand-collapse-animation
         try {
             Method m = v.getClass().getDeclaredMethod("onMeasure", int.class, int.class);
             m.setAccessible(true);
             m.invoke(
                 v,
                MeasureSpec.makeMeasureSpec(((View)v.getParent()).getMeasuredWidth(), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
             );
         } catch (Exception e){
             Log.e(TAG, "failed to measure view", e);
         }
         return v.getMeasuredHeight();
     }
     
     private void log(String msg) {
         Log.d(TAG, msg);
     }
 }
