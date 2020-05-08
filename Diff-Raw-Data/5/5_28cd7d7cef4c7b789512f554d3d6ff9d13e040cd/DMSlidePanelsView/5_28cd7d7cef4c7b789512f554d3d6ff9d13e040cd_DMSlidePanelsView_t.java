 /**
  * @project DMSlidePanels
  *
  * The MIT License (MIT)
  *
  * Copyright (c) 2013 Dmitry Ponomarev <demdxx@gmail.com>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  * the Software, and to permit persons to whom the Software is furnished to do so,
  * subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package com.demdxx.ui;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.util.AttributeSet;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.LinearInterpolator;
 import android.widget.FrameLayout;
 
 public class DMSlidePanelsView extends FrameLayout implements Animation.AnimationListener, View.OnClickListener {
   protected View leftSidePanel = null;
   protected View rightSidePanel = null;
   protected View centerPanel = null;
 
   protected DMSlideAnimator.Translation leftSidePanelTranslation = new DMSlideAnimator.Translation();
   protected DMSlideAnimator.Translation rightSidePanelTranslation = new DMSlideAnimator.Translation();
   protected DMSlideAnimator.Translation centerPanelTranslation = new DMSlideAnimator.Translation();
 
   protected boolean sidebarFixed = true;
   protected long slideAnimationDuration = 300;
   protected int swipe = 0; // 0 - none, 1 - arbitrary, 2 - strict
 
   protected GestureDetector gestureDetector;
 
   public DMSlidePanelsView(Context context) {
     super(context);
     if (!isInEditMode()) {
       initControl(context, null);
     }
   }
 
   public DMSlidePanelsView(Context context, AttributeSet attrs) {
     super(context, attrs);
     if (!isInEditMode()) {
       initControl(context, attrs);
     }
   }
 
   public DMSlidePanelsView(Context context, AttributeSet attrs, int defStyle) {
     super(context, attrs, defStyle);
     if (!isInEditMode()) {
       initControl(context, attrs);
     }
   }
 
   @Override
   protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
     super.onLayout(changed, left, top, right, bottom);
     if (null != centerPanel) {
       initPanels();
     }
   }
 
   /**
    * Init control params
    *
    * @param context Application context
    * @param attrs   AttributeSet
    */
   protected void initControl(Context context, AttributeSet attrs) {
     if (null != context && null != attrs) {
       // Set default params
       TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DMSidePanelsView);
       if (null != typedArray) {
         slideAnimationDuration = (long) typedArray.getInt(R.styleable.DMSidePanelsView_duration, (int) slideAnimationDuration);
         sidebarFixed = typedArray.getBoolean(R.styleable.DMSidePanelsView_fixed, sidebarFixed);
         swipe = typedArray.getInt(R.styleable.DMSidePanelsView_swipe, swipe);
         typedArray.recycle();
       }
     }
 
     // Init events
     initEvents();
   }
 
   /**
    * Accept view panels
    */
   public void initPanels() {
     if (null != centerPanel) {
       return;
     }
 
     // Assign views
     leftSidePanel = findViewById(R.id.dmslidepanels_leftside_panel);
     rightSidePanel = findViewById(R.id.dmslidepanels_rightside_panel);
     centerPanel = findViewById(R.id.dmslidepanels_central_panel);
 
     if (null == leftSidePanel || null == rightSidePanel || null == centerPanel) {
       for (int i = 0; i < getChildCount(); i++) {
         View v = getChildAt(i);
         if (v instanceof DMSlidePanelLeftView) {
           if (null == leftSidePanel) {
             leftSidePanel = v;
           }
         } else if (v instanceof DMSlidePanelRightView) {
           if (null == rightSidePanel) {
             rightSidePanel = v;
           }
         } else if (v instanceof DMSlidePanelCenterView) {
           if (null == centerPanel) {
             centerPanel = v;
           }
         }
       }
     }
 
     centerPanel.bringToFront();
 
     if (rightSidePanel instanceof DMSlidePanelView) {
       ((DMSlidePanelView) rightSidePanel).fixed(sidebarFixed);
     }
     if (leftSidePanel instanceof DMSlidePanelView) {
       ((DMSlidePanelView) leftSidePanel).fixed(sidebarFixed);
     }
 
     // Update position
     getLeftSidePanelTranslation(true).updateSize(leftSidePanel);
     getRightSidePanelTranslation(true).updateSize(rightSidePanel);
 
     // Hide sidebars
     showLeftSideBar(false, false);
     showRightSideBar(false, false);
 
     // Init events
     initEvents();
   }
 
   protected void initEvents() {
     if (1 == swipe) {
       initSwipeEvents();
     } else if (2 == swipe) {
       initStrictSwipeEvents();
     } else {
       setOnTouchListener(null);
     }
   }
 
   /**
    * Init as draggable central panel
    */
   protected void initStrictSwipeEvents() {
     if (null != gestureDetector) {
       return;
     }
     // Init gesture director
     gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
       float oldPosition = 0.0f;
 
       @Override
       public boolean onDown(MotionEvent e) {
         clearAnimation();
         if (null == centerPanel) {
           initPanels();
         }
         oldPosition = e.getX();
         return true;
       }
 
       @Override
       public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
         int offset = (int) (e2.getX() - oldPosition);
         centerPanelTranslation.left += offset;
         centerPanelTranslation.right += offset;
         centerPanelTranslation.update(centerPanel);
 
         if (!sidebarFixed) {
           // Calc left panel position
           leftSidePanelTranslation.left += offset;
           leftSidePanelTranslation.right += offset;
 
           if (leftSidePanelTranslation.left > 0 || centerPanelTranslation.left > leftSidePanelTranslation.right) {
             leftSidePanelTranslation.right = getLeftPanelWidth();
             leftSidePanelTranslation.left = 0;
           } else if (leftSidePanelTranslation.right > centerPanelTranslation.left) {
             int off = leftSidePanelTranslation.right - centerPanelTranslation.left;
             leftSidePanelTranslation.right = centerPanelTranslation.left;
             leftSidePanelTranslation.left -= off;
           }
 
           leftSidePanelTranslation.update(leftSidePanel);
 
           // Calc right panel position
           rightSidePanelTranslation.left += offset;
           rightSidePanelTranslation.right += offset;
 
           int width = getMeasuredWidth();
           if (rightSidePanelTranslation.right < width || centerPanelTranslation.right < rightSidePanelTranslation.left) {
             rightSidePanelTranslation.right = width;
             rightSidePanelTranslation.left = width - getRightPanelWidth();
           } else if (centerPanelTranslation.right > rightSidePanelTranslation.left) {
             int off = centerPanelTranslation.right - rightSidePanelTranslation.left;
             rightSidePanelTranslation.right += off;
             rightSidePanelTranslation.left = centerPanelTranslation.right;
           }
 
           rightSidePanelTranslation.update(rightSidePanel);
         }
 
         oldPosition = e2.getX();
         updatePanelsPosition();
         return true;
       }
     });
 
     OnTouchListener touchListener = new OnTouchListener() {
       @Override
       public boolean onTouch(View view, MotionEvent motionEvent) {
         if (MotionEvent.ACTION_UP == motionEvent.getAction()) {
           updatePanelsPositionAnimation();
         }
         return gestureDetector.onTouchEvent(motionEvent);
       }
     };
 
     setOnTouchListener(touchListener);
   }
 
   /**
    * Init as post swipe events
    */
   protected void initSwipeEvents() {
     if (null != gestureDetector) {
       return;
     }
     // Init gesture director
     gestureDetector = new GestureDetector(getContext(), new SwipeGestureDetector() {
       @Override
       public void swipe2Left() {
         if (isLeftSideBarVisible()) {
           showLeftSideBar(false, true);
         } else {
           showRightSideBar(true, true);
         }
       }
 
       @Override
       public void swipe2Right() {
         if (isRightSideBarVisible()) {
           showRightSideBar(false, true);
         } else {
           showLeftSideBar(true, true);
         }
       }
     });
 
     OnTouchListener touchListener = new OnTouchListener() {
       @Override
       public boolean onTouch(View view, MotionEvent motionEvent) {
         return gestureDetector.onTouchEvent(motionEvent);
       }
     };
 
     setOnTouchListener(touchListener);
   }
 
   /**
    * Set fixed state
    * @param fixed boolean
    */
   public void setSidebarFixed(boolean fixed) {
     sidebarFixed = fixed;
 
     if (rightSidePanel instanceof DMSlidePanelView) {
       ((DMSlidePanelView) rightSidePanel).fixed(sidebarFixed);
     }
     if (leftSidePanel instanceof DMSlidePanelView) {
       ((DMSlidePanelView) leftSidePanel).fixed(sidebarFixed);
     }
   }
 
   /**
    * Set animation duration
    * @param duration milliseconds
    */
   public void setSlideAnimationDuration(long duration) {
     slideAnimationDuration = duration;
   }
 
   /**
    * Set swipe type
    * @param type 0 - none, 1 - arbitrary, 2 - strict
    */
   public void setSwipe(int type) {
     swipe = type;
     gestureDetector = null;
     initEvents();
   }
 
   //////////////////////////////////////////////////////////////////////////////////////////////////
   /// Display panels actions
   //////////////////////////////////////////////////////////////////////////////////////////////////
 
   public int getLeftPanelWidth() {
     return (int) (Math.min(getMeasuredWidth(), getMeasuredHeight()) * 0.8f);
   }
 
   public int getRightPanelWidth() {
     return (int) (Math.min(getMeasuredWidth(), getMeasuredHeight()) * 0.8f);
   }
 
   public boolean isLeftSideBarVisible() {
     return null != leftSidePanel && View.VISIBLE == leftSidePanel.getVisibility();
   }
 
   public boolean isRightSideBarVisible() {
     return null != rightSidePanel && View.VISIBLE == rightSidePanel.getVisibility();
   }
 
   public void showLeftSideBar(boolean show, boolean animated) {
     initPanels();
     if (null == leftSidePanel) {
       return;
     }
 
     leftSidePanel.setVisibility(View.VISIBLE);
 
     if (leftSidePanel instanceof DMSlidePanelBaseView) {
       ((DMSlidePanelBaseView) leftSidePanel).onBeforeGoingInto(show);
     }
     if (rightSidePanel instanceof DMSlidePanelBaseView) {
       ((DMSlidePanelBaseView) rightSidePanel).onBeforeGoingInto(false);
     }
     if (centerPanel instanceof DMSlidePanelBaseView) {
       ((DMSlidePanelBaseView) centerPanel).onBeforeGoingInto(!show);
     }
 
     moveSidebars(getLeftSidePanelTranslation(show),
             getRightSidePanelTranslation(false),
             getCenterPanelTranslation(!show, false),
             animated);
   }
 
   public void showRightSideBar(boolean show, boolean animated) {
     initPanels();
     if (null == rightSidePanel) {
       return;
     }
 
     rightSidePanel.setVisibility(View.VISIBLE);
 
     if (leftSidePanel instanceof DMSlidePanelBaseView) {
       ((DMSlidePanelBaseView) leftSidePanel).onBeforeGoingInto(false);
     }
     if (rightSidePanel instanceof DMSlidePanelBaseView) {
       ((DMSlidePanelBaseView) rightSidePanel).onBeforeGoingInto(show);
     }
     if (centerPanel instanceof DMSlidePanelBaseView) {
       ((DMSlidePanelBaseView) centerPanel).onBeforeGoingInto(show);
     }
 
     moveSidebars(getLeftSidePanelTranslation(false),
             getRightSidePanelTranslation(show),
             getCenterPanelTranslation(!show, true),
             animated);
   }
 
   public void showCentralPanel(boolean animated) {
     if (isLeftSideBarVisible()) {
       showLeftSideBar(false, animated);
     } else if (isRightSideBarVisible()) {
       showRightSideBar(false, animated);
     }
   }
 
   public void toggleLeft(boolean animated) {
     showLeftSideBar(!isLeftSideBarVisible(), animated);
   }
 
   public void toggleRight(boolean animated) {
     showRightSideBar(!isRightSideBarVisible(), animated);
   }
 
   //////////////////////////////////////////////////////////////////////////////////////////////////
   /// Move sidebar to
   //////////////////////////////////////////////////////////////////////////////////////////////////
 
   /**
    * Get left position at
    *
    * @param show boolean
    * @return translation
    */
   protected DMSlideAnimator.Translation getLeftSidePanelTranslation(boolean show) {
     DMSlideAnimator.Translation l = new DMSlideAnimator.Translation();
     if (show) {
       l.set(0, 0, getLeftPanelWidth(), getMeasuredHeight());
     } else {
       l.set(-getLeftPanelWidth(), 0, 0, getMeasuredHeight());
     }
     return l;
   }
 
   /**
    * Get right position at
    *
    * @param show boolean
    * @return translation
    */
   protected DMSlideAnimator.Translation getRightSidePanelTranslation(boolean show) {
     int w = getMeasuredWidth();
     DMSlideAnimator.Translation r = new DMSlideAnimator.Translation();
     if (show) {
       r.set(w - getRightPanelWidth(), 0, w, getMeasuredHeight());
     } else {
       r.set(w, 0, w + getRightPanelWidth(), getMeasuredHeight());
     }
     return r;
   }
 
   /**
    * Get end position at
    *
    * @param show  boolean
    * @param right boolean
    * @return translation
    */
   protected DMSlideAnimator.Translation getCenterPanelTranslation(boolean show, boolean right) {
     DMSlideAnimator.Translation c = centerPanelTranslation.copy();
     c.bottom = getMeasuredHeight();
     if (show) {
       c.right = getMeasuredWidth();
       c.left = 0;
     } else if (right) {
       c.left = -getLeftPanelWidth();
       c.right = getMeasuredWidth() + c.left;
     } else {
       c.left = getLeftPanelWidth();
       c.right = getMeasuredWidth() + c.left;
     }
     return c;
   }
 
   /**
    * Change layers position
    *
    * @param left     translation
    * @param right    translation
    * @param center   translation
    * @param animated boolean
    */
   protected void moveSidebars(DMSlideAnimator.Translation left,
                               DMSlideAnimator.Translation right,
                               DMSlideAnimator.Translation center,
                               boolean animated) {
     if (animated) {
       // Clear animation
       clearAnimation();
 
       DMSlideAnimator animator;
       if (sidebarFixed) {
         leftSidePanelTranslation = getLeftSidePanelTranslation(true);
         rightSidePanelTranslation = getRightSidePanelTranslation(true);
         animator = new DMSlideAnimator(leftSidePanel, leftSidePanelTranslation, leftSidePanelTranslation,
                                        rightSidePanel, rightSidePanelTranslation, rightSidePanelTranslation,
                                        centerPanel, centerPanelTranslation.copy(), center);
       } else {
         animator = new DMSlideAnimator(leftSidePanel, leftSidePanelTranslation.copy(), left,
                                        rightSidePanel, rightSidePanelTranslation.copy(), right,
                                        centerPanel, centerPanelTranslation.copy(), center);
       }
 
       // update translation
       centerPanelTranslation = center;
       leftSidePanelTranslation = left;
       rightSidePanelTranslation = right;
 
       animator.setDuration(slideAnimationDuration);
       animator.setInterpolator(new LinearInterpolator());
       animator.setAnimationListener(this);
       startAnimation(animator);
     } else {
       leftSidePanelTranslation = left;
       rightSidePanelTranslation = right;
       centerPanelTranslation = center;
       updateLayout();
     }
   }
 
   /**
    * Update position and delete animations from panels
    */
   protected void updateLayout() {
     if (null != leftSidePanel) {
       if (!sidebarFixed) {
         leftSidePanelTranslation.update(leftSidePanel);
       }
       leftSidePanel.clearAnimation();
       if (leftSidePanelTranslation.left < 0) {
         leftSidePanel.setVisibility(View.GONE);
       }
     }
     if (null != rightSidePanel) {
       if (!sidebarFixed) {
         rightSidePanelTranslation.update(rightSidePanel);
       }
       rightSidePanel.clearAnimation();
       if (rightSidePanelTranslation.left >= getMeasuredWidth()) {
         rightSidePanel.setVisibility(View.GONE);
       }
     }
     if (null != centerPanel) {
       centerPanelTranslation.update(centerPanel);
       centerPanel.clearAnimation();
     }
     clearAnimation();
 
     // End events
     boolean shownLeft = View.VISIBLE == leftSidePanel.getVisibility();
     boolean shownRight = View.VISIBLE == leftSidePanel.getVisibility();
     if (leftSidePanel instanceof DMSlidePanelBaseView) {
       ((DMSlidePanelBaseView) leftSidePanel).onAfterGoingInto(shownLeft);
     }
     if (rightSidePanel instanceof DMSlidePanelBaseView) {
       ((DMSlidePanelBaseView) rightSidePanel).onBeforeGoingInto(shownRight);
     }
     if (centerPanel instanceof DMSlidePanelBaseView) {
       ((DMSlidePanelBaseView) centerPanel).onBeforeGoingInto(!shownLeft && !shownRight);
     }
   }
 
   /**
    * Update panels state by central panel state
    */
   protected void updatePanelsPosition() {
     if (null != leftSidePanel) {
       if (centerPanelTranslation.left > 0) {
         leftSidePanel.setVisibility(View.VISIBLE);
        if (leftSidePanel instanceof DMSlidePanelBaseView) {
           ((DMSlidePanelBaseView) leftSidePanel)
                   .updateByCentralTranslation(
                           getLeftSidePanelTranslation(true),
                           centerPanelTranslation);
           leftSidePanel.invalidate();
         }
       } else {
         leftSidePanel.setVisibility(View.GONE);
       }
     }
     if (null != rightSidePanel) {
       if (centerPanelTranslation.right < getMeasuredWidth()) {
         rightSidePanel.setVisibility(View.VISIBLE);
        if (rightSidePanel instanceof DMSlidePanelBaseView) {
           ((DMSlidePanelBaseView) rightSidePanel)
                   .updateByCentralTranslation(
                           getRightSidePanelTranslation(true),
                           centerPanelTranslation);
           rightSidePanel.invalidate();
         }
       } else {
         rightSidePanel.setVisibility(View.GONE);
       }
     }
   }
 
   /**
    * To complete reposition
    */
   protected void updatePanelsPositionAnimation() {
     long oldDuration = slideAnimationDuration;
     slideAnimationDuration /= 2;
     if (View.VISIBLE == leftSidePanel.getVisibility()) {
       showLeftSideBar(centerPanelTranslation.left >= getLeftPanelWidth() / 3, true);
     } else if (View.VISIBLE == rightSidePanel.getVisibility()) {
       showRightSideBar(getMeasuredWidth() - centerPanelTranslation.right >= getLeftPanelWidth() / 3, true);
     } else {
       // If invisible panels
       int left = centerPanelTranslation.left;
       int right = getMeasuredWidth() - centerPanelTranslation.right;
       if (left > right) {
         showLeftSideBar(centerPanelTranslation.left >= getLeftPanelWidth() / 3, true);
       } else if (left < right) {
         showRightSideBar(right >= getLeftPanelWidth() / 3, true);
       } else {
         showCentralPanel(true);
       }
     }
 
     // Restore defaults
     if (rightSidePanel instanceof DMSlidePanelView) {
       ((DMSlidePanelView) rightSidePanel).fixed(sidebarFixed);
     }
     if (leftSidePanel instanceof DMSlidePanelView) {
       ((DMSlidePanelView) leftSidePanel).fixed(sidebarFixed);
     }
 
     slideAnimationDuration = oldDuration;
   }
 
   //////////////////////////////////////////////////////////////////////////////////////////////////
   /// Animation events & click
   //////////////////////////////////////////////////////////////////////////////////////////////////
 
   @Override
   public void onAnimationStart(Animation animation) {
     // hollow...
   }
 
   @Override
   public void onAnimationEnd(Animation animation) {
     updateLayout();
   }
 
   @Override
   public void onAnimationRepeat(Animation animation) {
     // hollow...
   }
 
   @Override
   public void onClick(View view) {
     if (view == centerPanel) {
       showCentralPanel(true);
     }
   }
 
   //////////////////////////////////////////////////////////////////////////////////////////////////
   /// Swipe gesture
   //////////////////////////////////////////////////////////////////////////////////////////////////
 
   abstract class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
 
     private static final int SWIPE_MIN_DISTANCE = 120;
     private static final int SWIPE_MAX_OFF_PATH = 250;
     private static final int SWIPE_THRESHOLD_VELOCITY = 200;
 
     public SwipeGestureDetector() {
       super();
     }
 
     @Override
     public boolean onDown(MotionEvent e) {
       return true;
     }
 
     @Override
     public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
       try {
         if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
           return false;
         }
         // right to left swipe
         if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
           swipe2Left();
         } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
           swipe2Right();
         }
       } catch (Exception e) {
         e.printStackTrace();
       }
       return false;
     }
 
     public abstract void swipe2Left();
 
     public abstract void swipe2Right();
   }
 }
