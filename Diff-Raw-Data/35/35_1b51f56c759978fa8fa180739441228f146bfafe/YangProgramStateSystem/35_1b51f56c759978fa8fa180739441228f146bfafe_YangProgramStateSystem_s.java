 package yang.util.statesystem;
 
 import yang.events.eventtypes.YangEvent;
 import yang.graphics.defaults.DefaultSurface;
 
 public class YangProgramStateSystem extends DefaultSurface {
 
 	public YangProgramState<?> mCurrentState;
 
 	protected YangProgramStateSystem(boolean init2dGraphics, boolean init3dGraphics) {
 		super(init2dGraphics, init3dGraphics);
 	}
 
 	@SuppressWarnings("unchecked")
 	public <ThisType extends YangProgramStateSystem> void setState(YangProgramState<ThisType> newState) {
 		if(!newState.isInitialized())
 			newState.init((ThisType)this);
 		if(mCurrentState!=null)
 			mCurrentState.stop();
 		mCurrentState = newState;
 		if(!mCurrentState.mFirstFrame)
 			mCurrentState.start();
 	}
 
 	public YangProgramState<?> getCurrentState() {
 		return mCurrentState;
 	}
 
 	@Override
 	public void step(float deltaTime) {
 		super.step(deltaTime);
 		if(mCurrentState!=null)
 			mCurrentState.proceed(deltaTime);
 	}
 
 	@Override
 	public void preDraw() {
 		if(mCurrentState!=null) {
 			mCurrentState.preDrawFrame();
 		}
 	}
 
 	@Override
 	public void draw() {
 		if(mCurrentState!=null) {
 			mCurrentState.drawFrame();
 		}
 	}
 
 	@Override
 	public boolean rawEvent(YangEvent event) {
 		if(mCurrentState!=null && !mCurrentState.mFirstFrame) {
 			event.handle(mCurrentState);
 			return false;
 		}else
 			return false;
 	}
 
 	@Override
 	public void resume() {
 		super.resume();
 		if(mCurrentState!=null && !mCurrentState.mFirstFrame) {
 			mCurrentState.resume();
 		}
 	}
 
 	@Override
 	public void exit() {
 		if(mCurrentState!=null)
 			mCurrentState.stop();
 		super.exit();
 	}
 
 	@Override
 	public void pause() {
 		super.pause();
 		if(mCurrentState!=null && !mCurrentState.mFirstFrame) {
 			mCurrentState.pause();
 		}
 	}
 
	public void onBlock() {
		if(mCurrentState!=null) {
			mCurrentState.mBlocked = true;
			mCurrentState.onBlock();
		}
	}

	public void onUnblock() {
		if(mCurrentState!=null) {
			mCurrentState.mBlocked = false;
			mCurrentState.onUnblock();
		}
	}

 }
