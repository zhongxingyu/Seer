 package in.ac.iitb.intulearn.unit.bubblesort;
 
 import in.ac.iitb.intulearn.unit.ExperimentView;
 import in.ac.iitb.intulearn.unit.ShapeHolder;
 import in.ac.iitb.intulearn.unit.Unit;
 import in.ac.iitb.intulearn.util.Common;
 
 import java.util.ArrayList;
 
 import android.animation.Animator;
 import android.animation.AnimatorSet;
 import android.animation.ArgbEvaluator;
 import android.animation.ObjectAnimator;
 import android.animation.TimeInterpolator;
 import android.animation.ValueAnimator.AnimatorUpdateListener;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.view.animation.LinearInterpolator;
 
 public class BubbleSortExperiment extends ExperimentView {
 
     final static private long ELEMENT_ANIMATION_DURATION_SEL = 1000; // Animate
     final static private long ELEMENT_ANIMATION_DURATION_SLIDE = 1000; // Halt
 
     final static private TimeInterpolator linearInterpolator = new LinearInterpolator();
     final static private ArgbEvaluator argbEvaluator = new ArgbEvaluator();
 
     private static final int ELEMENT_FILL_COLOR_SEL = 0xffff8888;
     private static final int ELEMENT_FILL_COLOR_DONE = 0xff88ff88;
 
     private static final float ELEMENT_GAP_SEL = ELEMENT_HEIGHT + ELEMENT_GAP_Y;
 
     private ArrayList<ShapeHolder> inputElements = new ArrayList<ShapeHolder>();
     private ArrayList<ShapeHolder> inputElementsCopy;
 
     private AnimatorUpdateListener viewInvalidator = new Common.ViewInvalidator(this);
 
     public BubbleSortExperiment(Context context) {
         super(context);
 
         int[] originalElementValues = Unit.INITIAL_VALUES_ARRAY;
         
         ShapeHolder inputElement;
         for (int i = 0; i < originalElementValues.length; i++) {
             inputElement = newElement(i, originalElementValues[i]);
             inputElements.add(inputElement);
         }
 
        inputElements = inputElementsCopy;
         initExperiment();
     }
 
     @Override
     public void initExperiment() {
         inputElements = inputElementsCopy;
         for (int i = 0; i < inputElements.size(); i++) {
             resetElement(inputElements.get(i), i);
         }
     }
 
     @Override
     protected ArrayList<Animator> newAnimation() {
         
         // Before we actually sort inputElements, make its copy so that 
         // its order is not lost. Required for restarting with original order.
         inputElementsCopy = (ArrayList<ShapeHolder>) inputElements.clone();
         
         ArrayList<Animator> fullAnimation = new ArrayList<Animator>();
         ArrayList<Animator> stepAnimation;
 
         int k = 0;
         for (int i = inputElements.size() - 1; i >= 0; i--) {
             stepAnimation = new ArrayList<Animator>();
 
             for (k = 0; k < i; k++) {
                 int firstElement = Integer.parseInt(inputElements.get(k).getLabel());
                 int secondElement = Integer.parseInt(inputElements.get(k + 1).getLabel());
                 if (firstElement > secondElement) {
                     stepAnimation.add(getSelectedAnimation(k));
                     stepAnimation.add(getSlideAnimation(k + 1, k, -1));
                     stepAnimation.add(getDeselectedAnimation(k));
 
                     inputElements.add(k + 1, inputElements.remove(k));
                 }
             }
 
             AnimatorSet stepAnimatorSet = new AnimatorSet();
             stepAnimatorSet.playSequentially(stepAnimation);
             fullAnimation.add(stepAnimatorSet);
         }
         return fullAnimation;
     }
 
     private Animator getSelectedAnimation(int i) {
         ObjectAnimator a1 = ObjectAnimator.ofInt(inputElements.get(i), "color", ELEMENT_FILL_COLOR_SEL);
         ObjectAnimator a2 = ObjectAnimator.ofFloat(inputElements.get(i), "y", densityManager.pixelsToDIP(GLOBAL_OFFSET_Y - ELEMENT_GAP_SEL));
 
         a1.addUpdateListener(viewInvalidator);
         a2.addUpdateListener(viewInvalidator);
         a1.setEvaluator(argbEvaluator);
 
         AnimatorSet animatorSet = new AnimatorSet();
         animatorSet.playSequentially(a1, a2);
         animatorSet.setDuration(ELEMENT_ANIMATION_DURATION_SEL);
         animatorSet.setInterpolator(linearInterpolator);
         return animatorSet;
     }
 
     private Animator getSlideAnimation(int slideRightIndex, int slideLeftIndex, int num) {
         final ShapeHolder slideLeft = inputElements.get(slideLeftIndex);
         final ShapeHolder slideRight = inputElements.get(slideRightIndex);
 
         final float newX1 = densityManager.pixelsToDIP(GLOBAL_OFFSET_X + (slideLeftIndex - num) * (ELEMENT_WIDTH + ELEMENT_GAP_X));
         final float newX2 = densityManager.pixelsToDIP(GLOBAL_OFFSET_X + (slideRightIndex - 1) * (ELEMENT_WIDTH + ELEMENT_GAP_X));
 
         ObjectAnimator a1 = ObjectAnimator.ofFloat(slideLeft, "x", newX1);
         ObjectAnimator a2 = ObjectAnimator.ofFloat(slideRight, "x", newX2);
 
         a1.addUpdateListener(viewInvalidator);
         a2.addUpdateListener(viewInvalidator);
 
         AnimatorSet animatorSet = new AnimatorSet();
         animatorSet.playTogether(a1, a2);
         animatorSet.setDuration(ELEMENT_ANIMATION_DURATION_SLIDE);
         animatorSet.setInterpolator(linearInterpolator);
         return animatorSet;
     }
 
     private Animator getDeselectedAnimation(int i) {
         ObjectAnimator a1 = ObjectAnimator.ofFloat(inputElements.get(i), "y", densityManager.pixelsToDIP(GLOBAL_OFFSET_Y));
         ObjectAnimator a2 = ObjectAnimator.ofInt(inputElements.get(i), "color", ELEMENT_FILL_COLOR_DONE);
 
         a1.addUpdateListener(viewInvalidator);
         a2.addUpdateListener(viewInvalidator);
         a2.setEvaluator(argbEvaluator);
 
         AnimatorSet animatorSet = new AnimatorSet();
         animatorSet.playSequentially(a1, a2);
         animatorSet.setDuration(ELEMENT_ANIMATION_DURATION_SEL);
         animatorSet.setInterpolator(linearInterpolator);
         return animatorSet;
     }
 
     @Override
     protected ArrayList<ShapeHolder> getShapeHolders() {
         ArrayList<ShapeHolder> shapeHolders = new ArrayList<ShapeHolder>(inputElements);
         return shapeHolders;
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
         for (ShapeHolder element : inputElements)
             element.draw(canvas);
     }
 
     @Override
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         int measuredWidth = densityManager.pixelsToDIP(GLOBAL_OFFSET_X + inputElements.size() * (ELEMENT_WIDTH + ELEMENT_GAP_X));
         int measuredHeight = densityManager.pixelsToDIP(GLOBAL_OFFSET_Y + 2 * (ELEMENT_HEIGHT + ELEMENT_GAP_Y));
         setMeasuredDimension(resolveSize(measuredWidth, widthMeasureSpec), resolveSize(measuredHeight, heightMeasureSpec));
     }
 
 }
