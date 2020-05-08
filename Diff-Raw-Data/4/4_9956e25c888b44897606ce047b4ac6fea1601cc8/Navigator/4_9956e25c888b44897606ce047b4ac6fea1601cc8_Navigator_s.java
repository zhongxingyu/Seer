 package com.erdfelt.android.gestures.nav;
 
 import android.content.Context;
 import android.graphics.Point;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.VelocityTracker;
 import android.view.ViewConfiguration;
 
 public class Navigator {
     private class TapHandler extends Handler {
         TapHandler() {
             super();
         }
 
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case SHOW_PRESS:
                     debug("msg.what = SHOW_PRESS");
                     listener.onPress(curDownEvent);
                     break;
                 case LONG_PRESS:
                     debug("msg.what = LONG_PRESS");
                     dispatchLongPress();
                     break;
                 case TAP:
                     debug("msg.what = TAP");
                     if (!stillDown) {
                         listener.onTap(curDownEvent);
                     }
                     break;
             }
         }
     }
 
     private static final String TAG             = Navigator.class.getSimpleName();
 
     // constants for Message.what used by TapHandler above
     private static final int    SHOW_PRESS      = 1;
     private static final int    LONG_PRESS      = 2;
     private static final int    TAP             = 3;
 
     private static boolean      DEBUG           = false;
     /**
      * Determines speed during touch scrolling
      */
     private VelocityTracker     velocityTracker;
     private OnNavListener       listener;
     private float               lastY;
     private float               lastX;
     private boolean             insideSlop;
     private boolean             insideDoubleSlop;
     private MotionEvent         curDownEvent;
     private MotionEvent         prevUpEvent;
     private boolean             stillDown       = false;
     private boolean             inLongPress     = false;
     private boolean             isDoubleTapping = false;
     private Handler             tapHandler;
     private boolean             inMultitouch    = false;
 
     /* ViewConfiguration values */
     private int                 touchSlop;
     private int                 touchSlopSquare;
     private int                 doubleTapSlop;
     private int                 doubleTapSlopSquare;
     private int                 flickMinVelocity;
     private int                 flickMaxVelocity;
     private int                 tapTimeout;
     private int                 doubleTapTimeout;
     private int                 longPressTimeout;
 
     public Navigator(Context context, OnNavListener listener) {
         this.listener = listener;
         if (listener == null) {
             throw new NullPointerException("TouchListener is null");
         }
 
         tapHandler = new TapHandler();
 
         tapTimeout = ViewConfiguration.getTapTimeout();
         doubleTapTimeout = ViewConfiguration.getDoubleTapTimeout();
         longPressTimeout = ViewConfiguration.getLongPressTimeout();
 
         if (context == null) {
             touchSlop = ViewConfiguration.getTouchSlop();
             doubleTapSlop = ViewConfiguration.getDoubleTapTimeout();
             flickMinVelocity = ViewConfiguration.getMinimumFlingVelocity();
             flickMaxVelocity = ViewConfiguration.getMaximumFlingVelocity();
         } else {
             ViewConfiguration vconf = ViewConfiguration.get(context);
             touchSlop = vconf.getScaledTouchSlop();
             doubleTapSlop = vconf.getScaledDoubleTapSlop();
             flickMinVelocity = vconf.getScaledMinimumFlingVelocity();
             flickMaxVelocity = vconf.getScaledMaximumFlingVelocity();
         }
         touchSlopSquare = touchSlop * touchSlop;
         doubleTapSlopSquare = doubleTapSlop * doubleTapSlop;
 
         if (DEBUG) {
             debug("tapTimeout          = %d", tapTimeout);
             debug("doubleTapTimeout    = %d", doubleTapTimeout);
             debug("longPressTimeout    = %d", longPressTimeout);
             debug("touchSlop           = %d", touchSlop);
             debug("doubleTapSlop       = %d", doubleTapSlop);
             debug("flickMinVelocity    = %d", flickMinVelocity);
             debug("flickMaxVelocity    = %d", flickMaxVelocity);
             debug("touchSlopSquare     = %d", touchSlopSquare);
             debug("doubleTapSlopSquare = %d", doubleTapSlopSquare);
         }
     }
 
     public static void setDebug(boolean enable) {
         DEBUG = enable;
     }
 
     private void debug(String format, Object... args) {
         if (DEBUG) {
             Log.d(TAG, String.format(format, args));
         }
     }
 
     public void dispatchLongPress() {
         tapHandler.removeMessages(TAP);
         inLongPress = true;
         listener.onLongPress(curDownEvent);
     }
 
     private boolean isDoubleTap(MotionEvent firstDown, MotionEvent firstUp, MotionEvent secondDown) {
         if (!insideDoubleSlop) {
             return false;
         }
 
         if ((firstDown == null) || (firstUp == null) || (secondDown == null)) {
             return false;
         }
 
         if ((secondDown.getEventTime() - firstUp.getEventTime()) > doubleTapTimeout) {
             return false;
         }
 
         int deltaX = (int) firstDown.getX() - (int) secondDown.getX();
         int deltaY = (int) firstDown.getY() - (int) secondDown.getY();
         return (((deltaX * deltaX) + (deltaY * deltaY)) < doubleTapSlopSquare);
     }
 
     private boolean isFling(float delta) {
         float velocity = Math.abs(delta);
         return (velocity > ViewConfiguration.getMinimumFlingVelocity())
                 && (velocity < ViewConfiguration.getMaximumFlingVelocity());
     }
 
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         switch (keyCode) {
             case KeyEvent.KEYCODE_DPAD_LEFT:
                 listener.onCursorDirection(Dir.WEST);
                 return true;
             case KeyEvent.KEYCODE_DPAD_RIGHT:
                 listener.onCursorDirection(Dir.EAST);
                 return true;
             case KeyEvent.KEYCODE_DPAD_DOWN:
                 listener.onCursorDirection(Dir.SOUTH);
                 return true;
             case KeyEvent.KEYCODE_DPAD_UP:
                 listener.onCursorDirection(Dir.NORTH);
                 return true;
             case KeyEvent.KEYCODE_DPAD_CENTER:
             case KeyEvent.KEYCODE_ENTER:
                 listener.onCursorSelect();
                 return true;
             default:
                 return false;
         }
     }
 
     private boolean onMotionCancel(final MotionEvent ev) {
         velocityTracker.recycle();
         velocityTracker = null;
 
         tapHandler.removeMessages(SHOW_PRESS);
         tapHandler.removeMessages(LONG_PRESS);
         tapHandler.removeMessages(TAP);
 
         stillDown = false;
         isDoubleTapping = false;
         inLongPress = false;
         return false;
     }
 
     private boolean onMotionDoubleTap(final MotionEvent ev) {
         boolean hadTapMessage = tapHandler.hasMessages(TAP);
         if (hadTapMessage) {
             tapHandler.removeMessages(TAP);
         }
 
         if (hadTapMessage && isDoubleTap(curDownEvent, prevUpEvent, ev)) {
             // Second Tap
             isDoubleTapping = true;
             return listener.onDoubleTap(curDownEvent);
         } else {
             // Normal Tap
             tapHandler.sendEmptyMessageDelayed(TAP, doubleTapTimeout);
         }
         return false;
     }
 
     private boolean onMotionDown(final MotionEvent ev) {
         boolean handled = false;
         debug("onMotionDown()");
 
         handled |= onMotionDoubleTap(ev);
 
         lastX = ev.getX();
         lastY = ev.getY();
 
         if (curDownEvent != null) {
             curDownEvent.recycle();
         }
         curDownEvent = MotionEvent.obtain(ev);
 
         insideSlop = true;
         insideDoubleSlop = true;
         stillDown = true;
         inLongPress = false;
 
         tapHandler.removeMessages(LONG_PRESS);
         tapHandler.sendEmptyMessageAtTime(LONG_PRESS, curDownEvent.getDownTime() + tapTimeout + longPressTimeout);
         tapHandler.sendEmptyMessageAtTime(SHOW_PRESS, curDownEvent.getDownTime() + tapTimeout);
 
         handled |= listener.onTouchDown(ev);
         return handled;
     }
 
     private boolean onMotionMove(final MotionEvent ev) {
         if (inLongPress) {
             return false;
         }
 
         boolean handled = false;
 
         final float scrollX = (-1) * (lastX - ev.getX());
         final float scrollY = lastY - ev.getY();
         // Eliminate Noise
         final boolean hasMotion = ((curDownEvent != null) && (Math.abs(scrollX) >= 1) && (Math.abs(scrollY) >= 1));
 
         if (!hasMotion) {
             return false;
         }
 
         debug("onMotionMove()");
 
         if (isDoubleTapping) {
             handled |= listener.onDoubleTap(ev);
         } else if (insideSlop) {
             final int deltaX = (int) (curDownEvent.getX() - ev.getX());
             final int deltaY = (int) (curDownEvent.getY() - ev.getY());
             final int distance = (deltaX * deltaX) + (deltaY * deltaY);
             if (distance > touchSlopSquare) {
                 handled |= listener.onDrag(curDownEvent, ev, Dir.asDir(scrollX, scrollY));
                 lastX = ev.getX();
                 lastY = ev.getY();
                 insideSlop = false;
 
                 tapHandler.removeMessages(TAP);
                 tapHandler.removeMessages(SHOW_PRESS);
                 tapHandler.removeMessages(LONG_PRESS);
             }
             if (distance > doubleTapSlopSquare) {
                 insideDoubleSlop = false;
             }
         } else {
             handled |= listener.onDrag(curDownEvent, ev, Dir.asDir(scrollX, scrollY));
             lastX = ev.getX();
             lastY = ev.getY();
         }
 
         return handled;
     }
 
     private boolean onMotionUp(final MotionEvent ev) {
         debug("onMotionUp()");
         boolean handled = false;
 
         stillDown = false;
         MotionEvent currentUpEvent = MotionEvent.obtain(ev);
 
         handled |= listener.onTouchUp(ev);
 
         if (isDoubleTapping) {
             handled |= listener.onDoubleTap(ev);
         } else if (inLongPress) {
             tapHandler.removeMessages(TAP);
             inLongPress = false;
        } else if (insideSlop) {
            handled |= listener.onTap(ev);
        } else {
             // Set pixels per second velocity calculation
             velocityTracker.computeCurrentVelocity(1000, flickMaxVelocity);
             float velocityX = velocityTracker.getXVelocity();
             float velocityY = velocityTracker.getYVelocity();
 
             if (isFling(velocityX) || isFling(velocityY)) {
                 // We have a fling!
                 Dir dir = Dir.asDir(velocityX, (-1) * velocityY);
                 handled |= listener.onFlick(curDownEvent, ev, dir, velocityX, velocityY);
             }
 
             // End scrolling, either way.
             handled |= listener.onDragUp(curDownEvent, ev);
             if (curDownEvent != null) {
                 curDownEvent.recycle();
                 curDownEvent = null;
             }
         }
 
         if (prevUpEvent != null) {
             prevUpEvent.recycle();
         }
         prevUpEvent = currentUpEvent;
 
         velocityTracker.recycle();
         velocityTracker = null;
 
         isDoubleTapping = false;
         tapHandler.removeMessages(SHOW_PRESS);
         tapHandler.removeMessages(LONG_PRESS);
 
         return handled;
     }
 
     private boolean onMultitouchDown(MotionEvent ev) {
         debug("MULTITOUCH Down : %s", ev);
         inMultitouch = true;
 
         return true;
     }
 
     private String coords(MotionEvent ev, int index) {
         return String.format("[%d] %.1fx%.1f", index, ev.getX(index), ev.getY(index));
     }
 
     private boolean onMultitouchMove(MotionEvent ev) {
         if (DEBUG) {
             debug("MULTITOUCH Move : %s, %s", coords(ev, 0), coords(ev, 1));
         }
 
         tapHandler.removeMessages(TAP);
         tapHandler.removeMessages(SHOW_PRESS);
         tapHandler.removeMessages(LONG_PRESS);
 
         boolean handled = false;
 
         Point center = new Point();
         float x = ((ev.getX(1) - ev.getX(0)) / 2) + ev.getX(0);
         float y = ((ev.getY(1) - ev.getY(0)) / 2) + ev.getY(0);
         center.x = (int) x;
         center.y = (int) y;
 
         listener.onMultiMove(ev, center);
 
         return handled;
     }
 
     private boolean onMultitouchUp(MotionEvent ev) {
         debug("MULTITOUCH Up : %s", ev);
         inMultitouch = false;
 
         return true;
     }
 
     public boolean onTouchEvent(final MotionEvent ev) {
         if (velocityTracker == null) {
             velocityTracker = VelocityTracker.obtain();
         }
         velocityTracker.addMovement(ev);
 
         switch (ev.getAction()) {
             case MotionEvent.ACTION_POINTER_DOWN:
                 return onMultitouchDown(ev);
             case MotionEvent.ACTION_POINTER_UP:
                 return onMultitouchUp(ev);
             case MotionEvent.ACTION_DOWN:
                 return onMotionDown(ev);
             case MotionEvent.ACTION_MOVE:
                 inMultitouch = (ev.getPointerCount() > 1);
                 if (inMultitouch) {
                     return onMultitouchMove(ev);
                 } else {
                     return onMotionMove(ev);
                 }
             case MotionEvent.ACTION_UP:
                 return onMotionUp(ev);
             case MotionEvent.ACTION_CANCEL:
                 return onMotionCancel(ev);
             default:
                 return false;
         }
     }
 
     public boolean onTrackballEvent(MotionEvent event) {
         debug("Trackball Event: %s", event);
         switch (event.getAction()) {
             case MotionEvent.ACTION_UP:
                 onMotionUp(event);
                 return true;
             case MotionEvent.ACTION_DOWN:
                 onMotionDown(event);
                 return true;
             case MotionEvent.ACTION_MOVE:
                 onMotionMove(event);
                 return true;
             default:
                 return false;
         }
     }
 }
