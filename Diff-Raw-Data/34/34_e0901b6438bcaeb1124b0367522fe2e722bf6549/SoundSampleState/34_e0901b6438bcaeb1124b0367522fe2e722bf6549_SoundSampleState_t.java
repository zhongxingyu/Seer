 package yang.samples.statesystem.states;
 
 import yang.events.eventtypes.YangPointerEvent;
 import yang.samples.statesystem.SampleState;
 import yang.sound.AbstractSound;
 
 public class SoundSampleState extends SampleState {
 
 	private AbstractSound mSound;
 	private AbstractSound mLoopSound;
 	
 	@Override
 	protected void postInit() {
 		mSound = mSounds.getSound("sound");
 		mLoopSound = mSounds.getSound("loopsound");
 	}
 	
 	@Override
 	protected void step(float deltaTime) {
 		
 	}
 
 	@Override
 	protected void draw() {
 		mGraphics.clear(0,0,0.1f);
 	}
 
 	@Override
 	public void pointerDown(float x,float y,YangPointerEvent event) {
//		mSound.play();
		mLoopSound.playLoop();
	}
	
	public void pointerUp(float x,float y, YangPointerEvent event) {
		mLoopSound.stopLoop();
 	}
 	
 	@Override
 	public void keyDown(int code) {
 		super.keyDown(code);
//		if(code=='p')
//			mLoopSound.playLoop();
 	}
 	
 	@Override
 	public void keyUp(int code) {
 		super.keyUp(code);
 		if(code=='p')
 			mLoopSound.stopLoop();
 	}
 	
 }
