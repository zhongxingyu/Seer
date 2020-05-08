 package com.ebooka;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.ebooka.events.ZoomListener;
 import com.ebooka.models.CurrentPageModel;
 import com.ebooka.models.DecodingProgressModel;
 import com.ebooka.models.ZoomModel;
 import com.ebooka.multitouch.MultiTouchZoom;
 import com.ebooka.multitouch.MultiTouchZoomImpl;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.RectF;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.VelocityTracker;
 import android.view.View;
 import android.widget.Scroller;
 
 public class DocumentView extends View implements ZoomListener {
     ZoomModel zoomModel;
     private CurrentPageModel currentPageModel;
     DecodeService decodeService;
     private final HashMap<Integer, Page> pages = new HashMap<Integer, Page>();
     private boolean isInitialized = false;
     private int pageToGoTo;
     private float lastX;
     private float lastY;
     private VelocityTracker velocityTracker;
     private Scroller scroller;
     DecodingProgressModel progressModel;
     private RectF viewRect;
     private boolean inZoom;
     private long lastDownEventTime;
     private static final int DOUBLE_TAP_TIME = 200;
     private MultiTouchZoom multiTouchZoom;
 
     public DocumentView(Context context) {
         super(context);
     }
 
     public DocumentView(Context context, AttributeSet attributeSet) {
         super(context, attributeSet);
     }
 
     public DocumentView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
     }
 
     public void init(final ZoomModel zoomModel, DecodingProgressModel progressModel, CurrentPageModel currentPageModel) {
         this.zoomModel = zoomModel;
         this.progressModel = progressModel;
         this.currentPageModel = currentPageModel;
         setKeepScreenOn(true);
         scroller = new Scroller(getContext());
         setFocusable(true);
         setFocusableInTouchMode(true);
         initMultiTouchZoomIfAvailable(zoomModel);
       
     }
 
     private void initMultiTouchZoomIfAvailable(ZoomModel zoomModel) {
         try {
             multiTouchZoom = new MultiTouchZoomImpl(zoomModel);
         } catch (Exception e) {
             System.out.println("Multi touch zoom is not available: " + e);
         }
     }
 
     public void setDecodeService(DecodeService decodeService) {
         this.decodeService = decodeService;
     }
 
     private void init() {
         if (isInitialized) {
             return;
         }
         final int width = decodeService.getEffectivePagesWidth();
         final int height = decodeService.getEffectivePagesHeight();
         for (int i = 0; i < decodeService.getPageCount(); i++) {
             pages.put(i, new Page(this, i));
             pages.get(i).setAspectRatio(width, height);
         }
         isInitialized = true;
         invalidatePageSizes();
         goToPageImpl(pageToGoTo);
     }
 
     private void goToPageImpl(final int toPage) {
         scrollTo(0, pages.get(toPage).getTop());
     }
 
     @Override
     protected void onScrollChanged(int l, int t, int oldl, int oldt) {
         super.onScrollChanged(l, t, oldl, oldt);
         // bounds could be not updated
         post(new Runnable() {
             public void run() {
                 currentPageModel.setCurrentPageIndex(getCurrentPage());
             }
         });
         if (inZoom) {
             return;
         }
         postUpdatePageVisibility();
     }
 
     // on scrollChanged can be called from scrollTo just after new layout
     // applied so we should wait for relayout
     public void postUpdatePageVisibility() {
         post(new Runnable() {
             public void run() {
                 updatePageVisibility();
             }
         });
 
     }
 
     private void updatePageVisibility() {
         for (Page page : pages.values()) {
             page.updateVisibility();
         }
     }
 
     public void commitZoom() {
         for (Page page : pages.values()) {
             page.invalidate();
         }
         inZoom = false;
     }
 
     public void showDocument() {
         // use post to ensure that document view has width and height before
         // decoding begin
         post(new Runnable() {
             public void run() {
                 init();
                 updatePageVisibility();
             }
         });
     }
 
     public void goToPage(int toPage) {
         if (isInitialized) {
             goToPageImpl(toPage);
         } else {
             pageToGoTo = toPage;
         }
     }
 
     public int getCurrentPage() {
         for (Map.Entry<Integer, Page> entry : pages.entrySet()) {
             if (entry.getValue().isVisible()) {
                 return entry.getKey();
             }
         }
         return 0;
     }
 
     public void zoomChanged(float newZoom, float oldZoom) {
         inZoom = true;
         stopScroller();
         final float ratio = newZoom / oldZoom;
         invalidatePageSizes();
         scrollTo((int) ((getScrollX() + getWidth() / 2) * ratio - getWidth() / 2), (int) ((getScrollY() + getHeight() / 2)
                 * ratio - getHeight() / 2));
         postInvalidate();
     }
 
     public int getScrollValue() {
         return getHeight() - 10;
     }
 
     public void scrollNextPage() {
         scrollBy(0, getScrollValue());
     }
 
     public void scrollPrevPage() {
         scrollBy(0, -1 * getScrollValue());
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent ev) {
         super.onTouchEvent(ev);
         if (multiTouchZoom != null) {
             if (multiTouchZoom.onTouchEvent(ev)) {
                 return true;
             }
 
             if (multiTouchZoom.isResetLastPointAfterZoom()) {
                 setLastPosition(ev);
                 multiTouchZoom.setResetLastPointAfterZoom(false);
             }
         }
 
         if (velocityTracker == null) {
             velocityTracker = VelocityTracker.obtain();
         }
         velocityTracker.addMovement(ev);
 
         switch (ev.getAction()) {
         case MotionEvent.ACTION_DOWN:
             stopScroller();
             setLastPosition(ev);
             if (ev.getEventTime() - lastDownEventTime < DOUBLE_TAP_TIME) {
                 zoomModel.toggleZoomControls();
             } else {
                 lastDownEventTime = ev.getEventTime();
             }
 
             int x = (int) ev.getX();
             int y = (int) ev.getY();
             int maxX = getWidth();
             int maxY = getHeight();
             if (x > maxX * 0.80 || y > maxY * 0.80) {
                 scrollNextPage();
             } else if (x < maxX * 0.20 || y < maxY * 0.20) {
                 scrollPrevPage();
             }
 
             break;
         case MotionEvent.ACTION_MOVE:
             Log.d("DEBUG", ev.getX() + " : " + ev.getY());
             scrollBy((int) (lastX - ev.getX()), (int) (lastY - ev.getY()));
             setLastPosition(ev);
             break;
         case MotionEvent.ACTION_UP:
             velocityTracker.computeCurrentVelocity(1000);
 
             scroller.fling(getScrollX(), getScrollY(), (int) -velocityTracker.getXVelocity(),
                     (int) -velocityTracker.getYVelocity(), getLeftLimit(), getRightLimit(), getTopLimit(), getBottomLimit());
 
             velocityTracker.recycle();
             velocityTracker = null;
 
             break;
         }
         return true;
     }
 
     private void setLastPosition(MotionEvent ev) {
         lastX = ev.getX();
         lastY = ev.getY();
     }
 
     @Override
     public boolean dispatchKeyEvent(KeyEvent event) {
         if (event.getAction() == KeyEvent.ACTION_DOWN) {
             switch (event.getKeyCode()) {
             case KeyEvent.KEYCODE_DPAD_RIGHT:
                 lineByLineMoveTo(1);
                 return true;
             case KeyEvent.KEYCODE_DPAD_LEFT:
                 lineByLineMoveTo(-1);
                 return true;
             case KeyEvent.KEYCODE_DPAD_DOWN:
                 verticalDpadScroll(1);
                 return true;
             case KeyEvent.KEYCODE_DPAD_UP:
                 verticalDpadScroll(-1);
                 return true;
             }
         }
         return super.dispatchKeyEvent(event);
     }
 
     private void verticalDpadScroll(int direction) {
         scroller.startScroll(getScrollX(), getScrollY(), 0, direction * getHeight() / 2);
         invalidate();
     }
 
     private void lineByLineMoveTo(int direction) {
         if (direction == 1 ? getScrollX() == getRightLimit() : getScrollX() == getLeftLimit()) {
             scroller.startScroll(getScrollX(), getScrollY(), direction * (getLeftLimit() - getRightLimit()),
                     (int) (direction * pages.get(getCurrentPage()).bounds.height() / 50));
         } else {
             scroller.startScroll(getScrollX(), getScrollY(), direction * getWidth() / 2, 0);
         }
         invalidate();
     }
 
     private int getTopLimit() {
         return 0;
     }
 
     private int getLeftLimit() {
         return 0;
     }
 
     private int getBottomLimit() {
         return (int) pages.get(pages.size() - 1).bounds.bottom - getHeight();
     }
 
     private int getRightLimit() {
         return (int) (getWidth() * zoomModel.getZoom()) - getWidth();
     }
 
     @Override
     public void scrollTo(int x, int y) {
         super.scrollTo(Math.min(Math.max(x, getLeftLimit()), getRightLimit()),
                 Math.min(Math.max(y, getTopLimit()), getBottomLimit()));
         viewRect = null;
     }
 
     RectF getViewRect() {
         if (viewRect == null) {
             viewRect = new RectF(getScrollX(), getScrollY(), getScrollX() + getWidth(), getScrollY() + getHeight());
         }
         return viewRect;
     }
 
     @Override
     public void computeScroll() {
         if (scroller != null && scroller.computeScrollOffset()) {
             scrollTo(scroller.getCurrX(), scroller.getCurrY());
         }
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
         super.onDraw(canvas);
         for (Page page : pages.values()) {
             page.draw(canvas);
         }
     }
 
     @Override
     protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
         super.onLayout(changed, left, top, right, bottom);
         float scrollScaleRatio = getScrollScaleRatio();
         invalidatePageSizes();
         invalidateScroll(scrollScaleRatio);
         commitZoom();
     }
 
     void invalidatePageSizes() {
         if (!isInitialized) {
             return;
         }
         float heightAccum = 0;
         int width = getWidth();
         float zoom = zoomModel.getZoom();
         for (int i = 0; i < pages.size(); i++) {
             Page page = pages.get(i);
             float pageHeight = page.getPageHeight(width, zoom);
             page.setBounds(new RectF(0, heightAccum, width * zoom, heightAccum + pageHeight));
             heightAccum += pageHeight;
         }
     }
 
     public void invalidateScroll(float ratio) {
         if (!isInitialized) {
             return;
         }
         stopScroller();
         final Page page = pages.get(0);
         if (page == null || page.bounds == null) {
             return;
         }
         scrollTo((int) (getScrollX() * ratio), (int) (getScrollY() * ratio));
     }
 
     public float getScrollScaleRatio() {
         final Page page = pages.get(0);
         if (page == null || page.bounds == null) {
             return 0;
         }
         final float v = zoomModel.getZoom();
         return getWidth() * v / page.bounds.width();
     }
 
     private void stopScroller() {
         if (!scroller.isFinished()) {
             scroller.abortAnimation();
         }
     }
 }
