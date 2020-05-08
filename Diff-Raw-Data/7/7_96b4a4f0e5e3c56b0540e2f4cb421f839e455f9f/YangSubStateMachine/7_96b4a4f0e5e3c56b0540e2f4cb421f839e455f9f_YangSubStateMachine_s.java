 package yang.util.statesystem;
 
 import yang.events.eventtypes.YangEvent;
 
 public class YangSubStateMachine<StateMachineType extends YangProgramStateSystem> extends YangProgramState<StateMachineType> {
 
 	public YangProgramState<?> mCurrentState;
 
 	@SuppressWarnings("unchecked")
 	public <ThisType extends YangProgramStateSystem> void setState(YangProgramState<ThisType> newState) {
		if(!newState.isInitialized())
 			newState.init((ThisType)mStateSystem);
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
 	public void stop() {
 		if(mCurrentState!=null)
 			mCurrentState.stop();
 	}
 
 	@Override
 	public void resume() {
 		if(mCurrentState!=null && !mCurrentState.mFirstFrame) {
 			mCurrentState.resume();
 		}
 	}
 
 	@Override
 	public void pause() {
 		if(mCurrentState!=null && !mCurrentState.mFirstFrame) {
 			mCurrentState.pause();
 		}
 	}
 
 }
