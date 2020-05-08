 package yang.util.statesystem;
 
 import yang.events.eventtypes.YangEvent;
 import yang.util.statesystem.statefading.YangStateFader;
 
 public class YangSubStateChain<StateMachineType extends YangProgramStateSystem> extends YangProgramState<StateMachineType> implements StateSystemInterface {
 
 	public YangProgramState<StateMachineType>[] mStates;
 	public StateChainElement[] mStateElements;
 
 	private int mStateCount;
 
 	@SuppressWarnings("unchecked")
 	public YangSubStateChain(int capacity) {
 		mStates = new YangProgramState[capacity];
 		mStateElements = new StateChainElement[capacity];
 		for(int i=0;i<capacity;i++) {
 			mStateElements[i] = new StateChainElement();
 		}
 		mStateCount = capacity;
 	}
 
 	@Override
 	public void setState(YangProgramState state) {
 		setState(state.getStateSystemLayer(),state);
 	}
 
 	@Override
 	public void setStateNoStart(YangProgramState state) {
 		setStateNoStart(state.getStateSystemLayer(),state);
 	}
 
 	public void setStateNoStart(int layer,YangProgramState<StateMachineType> state) {
 		if(state!=null && !state.isInitialized())
 			state.init(mStateSystem);
 		if(mStates[layer]!=null)
 			mStates[layer].stop();
 		mStates[layer] = state;
 		mStateElements[layer].mActive = state!=null;
 	}
 
 	public void setState(int layer,YangProgramState<StateMachineType> state) {
 		setStateNoStart(layer,state);
 		if(state!=null)
 			state.onSet(this,layer);
 	}
 
 	@Override
 	public void fadeState(int layer,YangStateFader fader,YangProgramState toState) {
 		if(!fader.isInitialized())
 			fader.init(mStateSystem);
 		if(toState==mStates[layer])
 			return;
 		fader.setTargetState(toState);
 		fader.onSet(this,layer);
 		mStates[layer] = fader;
 		mStateElements[layer].mActive = true;
 	}
 
 	@Override
 	public YangProgramState getCurrentState(int layer) {
 		return mStates[layer];
 	}
 
 	public void deactivateAllStates() {
 		for(int i=0;i<mStateCount;i++) {
 			mStateElements[i].mActive = false;
 		}
 	}
 
 	@Override
 	protected void step(float deltaTime) {
 		boolean blocked = false;
 		for(int i=mStateCount-1;i>=0;i--) {
 			StateChainElement element = mStateElements[i];
 			YangProgramState<StateMachineType> state = mStates[i];
 			if(element.mActive) {
 				if(blocked) {
 					if(!state.mBlocked) {
 						state.mBlocked = true;
 						state.onBlock();
 					}
 				}else{
 					if(state.mBlocked) {
 						state.mBlocked = false;
 						state.onUnblock();
 					}
 					if(element.mSpeedFactor!=1) {
 						if(element.mSpeedFactor>0) {
 							if(element.mTargetTime<0)
 								element.mTargetTime = state.mStateTimer;
 							element.mTargetTime += deltaTime*element.mSpeedFactor;
							while(state.mStateTimer<element.mTargetTime) {
 								state.proceed(deltaTime);
 							}
 						}
					}else
 						state.proceed(deltaTime);
 					if(state.mBlockSteps)
 						blocked = true;
 				}
 			}
 		}
 	}
 
 	@Override
 	protected void preDraw() {
 		for(int i=0;i<mStateCount;i++) {
 			if(mStateElements[i].mActive)
 				mStates[i].preDrawFrame();
 		}
 	}
 
 	@Override
 	protected void draw() {
 		for(int i=0;i<mStateCount;i++) {
 			if(mStateElements[i].mActive)
 				mStates[i].drawFrame();
 		}
 	}
 
 	@Override
 	public boolean rawEvent(YangEvent event) {
 		for(int i=mStateCount-1;i>=0;i--) {
 			if(mStateElements[i].mActive) {
 				YangProgramState<?> state = mStates[i];
 				if(!state.mFirstFrame) {
 					event.handle(state);
 					if(state.mBlockEvents)
 						return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void resume() {
 		for(int i=0;i<mStateCount;i++) {
 			if(mStateElements[i].mActive) {
 				mStates[i].resume();
 			}
 		}
 	}
 
 	@Override
 	public void stop() {
 		for(int i=0;i<mStateCount;i++) {
 			if(mStateElements[i].mActive) {
 				mStates[i].stop();
 			}
 		}
 	}
 
 	@Override
 	public void pause() {
 		for(int i=0;i<mStateCount;i++) {
 			if(mStateElements[i].mActive) {
 				mStates[i].pause();
 			}
 		}
 	}
 
 }
