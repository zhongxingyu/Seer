 package yang.android.graphics;
 
 import yang.android.io.AndroidDataStorage;
 import yang.android.io.AndroidResourceManager;
 import yang.android.io.AndroidSoundLoader;
 import yang.android.sound.AndroidSoundManager;
 import yang.events.EventQueueHolder;
import yang.events.InputEventQueue;
 import yang.events.Keys;
 import yang.events.eventtypes.AbstractPointerEvent;
 import yang.events.eventtypes.YangKeyEvent;
 import yang.graphics.YangSurface;
 import yang.model.App;
 import yang.model.DebugYang;
 import android.content.Context;
 import android.graphics.PixelFormat;
 import android.opengl.GLSurfaceView;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class YangTouchSurface extends GLSurfaceView{
 
	public InputEventQueue mEventQueue;
 	public YangSceneRenderer mSceneRenderer;
 	public Context mContext;
 	
 	public YangTouchSurface(Context context) {
 		super(context);
 		mContext = context;
 		mEventQueue = null;
 		initGL(context);
 	}
 	
 	protected void initGL(Context context) {
 		super.setEGLContextClientVersion(2);
 		super.setEGLConfigChooser(8,8,8,0, 16,0);
		//HTC working: TRANSLUCENT, RGBA_8888
 		super.getHolder().setFormat(PixelFormat.RGBA_8888);	//TODO try out formats
 		DebugYang.println("INITIALIZE OPENGL");
 		
 		mSceneRenderer = new YangSceneRenderer(context);
 		super.setRenderer(mSceneRenderer);
 
 		if(App.soundManager==null) {
 			App.soundManager = new AndroidSoundManager(context);
 			App.storage = new AndroidDataStorage(context);
 			App.soundLoader = new AndroidSoundLoader(context);
 			App.gfxLoader = mSceneRenderer.mGraphicsTranslator.mGFXLoader;
 			App.resourceManager = new AndroidResourceManager(context);
 			((AndroidSoundManager)App.soundManager).init(App.soundLoader);
 		}else{
 			DebugYang.println("App references already set");
 		}
 	}
 	
 	public void setSurface(YangSurface surface) {
 		mSceneRenderer.setSurface(surface);
 		
 		if (surface instanceof EventQueueHolder)
 			setEventListener((EventQueueHolder)surface);
 	}
 	
 	public void setEventListener(EventQueueHolder eventQueue) {
 		mEventQueue = eventQueue.getEventQueue();
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if(mEventQueue==null)
 			return false;
 		int action = event.getActionMasked();
 		int idx = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
 		int id = event.getPointerId(idx);
 
 		int x = (int)event.getX(idx);
 		int y = (int)event.getY(idx);
 
 		switch (action){
 		case MotionEvent.ACTION_DOWN:
 		case MotionEvent.ACTION_POINTER_DOWN:
 			mEventQueue.putPointerEvent(AbstractPointerEvent.BUTTON_LEFT, x, y, AbstractPointerEvent.ACTION_POINTERDOWN, id);
 			break;
 
 		case MotionEvent.ACTION_UP:
 		case MotionEvent.ACTION_POINTER_UP:
 			mEventQueue.putPointerEvent(AbstractPointerEvent.BUTTON_LEFT, x, y, AbstractPointerEvent.ACTION_POINTERUP, id);
 			break;
 
 		case MotionEvent.ACTION_MOVE:
 			
 			for(int i = 0; i < event.getPointerCount(); i++){
 				id = event.getPointerId(i);
 				//mEventListener.pointerDragged((int)event.getX(i), (int)event.getY(i), id);
 				mEventQueue.putPointerEvent(AbstractPointerEvent.BUTTON_LEFT, x, y, AbstractPointerEvent.ACTION_POINTERDRAG, id);
 			}
 			break;
 		}
 
 		return true;
 	}
 	
 	public void onBackPressed() {
 		if (mEventQueue!=null) {
 			mEventQueue.putKeyEvent(Keys.ESC, YangKeyEvent.ACTION_KEYDOWN);
 			mEventQueue.putKeyEvent(Keys.ESC, YangKeyEvent.ACTION_KEYUP);
 		}
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		mSceneRenderer.mSurfaceInterface.pause();
 	}
 
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		mSceneRenderer.mSurfaceInterface.resume();
 	}
 	
 	public View getView() {
 		return this;
 	}
 }
