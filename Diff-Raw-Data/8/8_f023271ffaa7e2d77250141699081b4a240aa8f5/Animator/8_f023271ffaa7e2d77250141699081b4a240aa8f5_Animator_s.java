 package yang.graphics.tools.animator;
 
 import yang.events.Keys;
 import yang.events.eventtypes.SurfacePointerEvent;
 import yang.events.eventtypes.YangEvent;
 import yang.events.eventtypes.YangSensorEvent;
 import yang.events.listeners.YangEventListener;
 import yang.graphics.defaults.Default2DGraphics;
 import yang.graphics.defaults.DefaultAnimationPlayer;
 import yang.graphics.model.FloatColor;
 import yang.graphics.skeletons.CartoonSkeleton2D;
 import yang.graphics.skeletons.SkeletonCarrier;
 import yang.graphics.skeletons.animations.Animation;
 import yang.graphics.skeletons.animations.AnimationPlayer;
 import yang.graphics.skeletons.animations.AnimationSystem;
 import yang.graphics.skeletons.animations.KeyFrame;
 import yang.graphics.translator.GraphicsTranslator;
 import yang.graphics.translator.Texture;
 import yang.graphics.util.Camera2DSmooth;
 import yang.model.Rect;
 import yang.physics.massaggregation.SkeletonEditing;
 import yang.physics.massaggregation.elements.Joint;
 import yang.sound.AbstractSoundManager;
 import yang.util.YangList;
 
 public class Animator implements YangEventListener {
 
 	public static float FRICTION_EDIT = 0.98f;
 	public static float FRICTION_PHYSICS = Joint.DEFAULT_FRICTION;
 
 	public static FloatColor CLEAR_COLOR = new FloatColor(0.2f);
 	public AbstractSoundManager mSound;
 	public GraphicsTranslator mGraphics;
 	public Default2DGraphics mGraphics2D;
 	public CartoonSkeleton2D mCurSkeleton;
 	protected Texture mCurTexture;
 	protected SkeletonCarrier mCurCarrier;
 	@SuppressWarnings("rawtypes")
 	public AnimationPlayer mCurAnimationPlayer;
 	private final Camera2DSmooth mCamera;
 	private final SkeletonEditing mSkeletonEditing;
 	public YangList<CartoonSkeleton2D> mSkeletons;
 	public YangList<Texture> mTextures;
 	public YangList<AnimationSystem<?,?>> mAnimationSystems;
 	public YangList<AnimationPlayer<?>> mAnimationPlayers;
 	public AnimationSystem<?,?> mCurAnimationSystem;
 	public Animation<?> mCurAnimation;
 	public KeyFrame mCurFrame;
 	private int mSkeletonIndex;
 	private int mAnimationIndex;
 	private boolean mPhysicsMode;
 	private int mFrameIndex;
 	private float mCurPntX;
 	private float mCurPntY;
 	private float mPrevPntX;
 	private float mPrevPntY;
 	private boolean mPlaying;
 	private boolean mPaused;
 	public boolean mPoseChanged;
 	public float mGravity = -0.2f;
 	public boolean mLimitFloor;
 	public boolean mDrawSkeleton;
 	public float mPrevSX;
 	public float mPrevSY;
 	private final Rect mBoundaries;
 	protected Joint mHoverJoint = null;
 
 	public Animator(Default2DGraphics graphics2D) {
 		mGraphics2D = graphics2D;
 		mGraphics = graphics2D.mTranslator;
 		mCamera = new Camera2DSmooth();
 		mCamera.mAdaption = 0.2f;
 		mSkeletons = new YangList<CartoonSkeleton2D>();
 		mTextures = new YangList<Texture>();
 		mAnimationPlayers = new YangList<AnimationPlayer<?>>();
 		mSkeletonIndex = -1;
 		mCurTexture = null;
 		mSkeletonEditing = new SkeletonEditing();
 		CartoonSkeleton2D.JOINT_DEBUG_TEXTURE = mGraphics.mGFXLoader.getImage("circle");
 		mAnimationSystems = new YangList<AnimationSystem<?,?>>();
 		mPaused = false;
 		mPlaying = false;
 		mPhysicsMode = false;
 		mLimitFloor = false;
 		mDrawSkeleton = true;
 		mBoundaries = new Rect();
 	}
 
 	public void savePose() {
 		if(mCurSkeleton!=null) {
 			mCurFrame.mPose.copyFromSkeleton(mCurSkeleton);
 			mCurAnimation.setJointsAnimated(mCurSkeleton);
 		}
 	}
 
 	public void saveChangedPose() {
 		if(mPoseChanged)
 			savePose();
 		mPoseChanged = false;
 	}
 
 	public void draw() {
 		while(mTextures.size()<mSkeletons.size()) {
 			mTextures.add(mSkeletons.get(mTextures.size()).getDefaultTexture(mGraphics.mGFXLoader));
 		}
 
 		mGraphics.clear(CLEAR_COLOR);
 		mGraphics2D.switchGameCoordinates(true);
 		mGraphics2D.setCamera(mCamera);
 
 		//Floor
 		mGraphics.bindTexture(null);
 		mGraphics2D.setColor(0.5f);
 		mGraphics2D.drawRect(mGraphics2D.normToWorldX(mGraphics2D.getNormLeft()), 0, mGraphics2D.normToWorldX(mGraphics2D.getNormRight()), mGraphics2D.normToWorldY(mGraphics2D.getNormBottom()));
 		mGraphics2D.setColor(0.2f);
 		mGraphics2D.drawLine(-100, 1, 100, 1, 0.01f);
 		mGraphics2D.drawLine(-100, 2, 100, 2, 0.01f);
 		mGraphics2D.setColor(0.3f);
 		mGraphics2D.drawLine(0, 0, 0, 100, 0.01f);
 
 		if(mCurSkeleton!=null) {
 			mCurSkeleton.refreshVisualData();
 			if(mSkeletonIndex<mTextures.size())
 				mGraphics.bindTexture(mTextures.get(mSkeletonIndex));
 			else
 				mGraphics.bindTexture(null);
 			mCurSkeleton.draw();
 			if(mDrawSkeleton) {
 				//mCurSkeleton.mCarrier.drawCollision();
 				mCurSkeleton.drawDebug2D(mGraphics2D,mSkeletonEditing);
 			}
 		}
 		mGraphics.flush();
 	}
 
 	public void refreshPhysics() {
 		if(mPhysicsMode) {
 			mCurSkeleton.mConstantForceX = 0;
 			mCurSkeleton.mConstantForceY = mGravity;
 			mCurSkeleton.mLowerLimit = 0;
 			mCurSkeleton.setFriction(FRICTION_PHYSICS);
 		}else{
 			mCurSkeleton.mConstantForceX = 0;
 			mCurSkeleton.mConstantForceY = 0;
 			if(mLimitFloor)
 				mCurSkeleton.mLowerLimit = 0;
 			else
 				mCurSkeleton.mLowerLimit = -100;
 			mCurSkeleton.setFriction(FRICTION_EDIT);
 		}
 	}
 
 	public void setPhysicsMode(boolean enable) {
 		mPhysicsMode = enable;
 		refreshPhysics();
 	}
 
 	public void step(float deltaTime) {
 		mCamera.update();
 		if(mCurSkeleton!=null) {
 			if(mPhysicsMode) {
 				mCurSkeleton.physicalStep(deltaTime);
 			}else if(mPlaying) {
 				if(!mPaused)
 					mCurAnimationPlayer.proceed(deltaTime);
 			}else{
 
 				mCurSkeleton.physicalStep(deltaTime);
 			}
 		}
 	}
 
 	public void addSkeleton(CartoonSkeleton2D skeleton,AnimationSystem<?,?> animationSystem,AnimationPlayer<?> animationPlayer) {
 		if(!skeleton.isInitialized())
 			skeleton.init(mGraphics2D);
 		mAnimationSystems.add(animationSystem);
 		mSkeletons.add(skeleton);
 		if(animationPlayer==null)
 			animationPlayer = new DefaultAnimationPlayer(skeleton,null);
 		mAnimationPlayers.add(animationPlayer);
 		if(mSkeletons.size()==1) {
 			selectSkeleton(0);
 		}
 	}
 
 	public void addSkeleton(CartoonSkeleton2D skeleton,AnimationSystem<?,?> animationSystem) {
 		addSkeleton(skeleton,animationSystem,null);
 	}
 
 	public void addSkeleton(Class<? extends CartoonSkeleton2D> skeletonClass,AnimationSystem<?,?> animationSystem) {
 		try {
 			final CartoonSkeleton2D skeleton = skeletonClass.newInstance();
 			skeleton.init(mGraphics2D);
 			addSkeleton(skeleton,animationSystem,null);
 		} catch (final InstantiationException e) {
 			e.printStackTrace();
 		} catch (final IllegalAccessException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void selectSkeleton(int index) {
 		saveChangedPose();
 		mCurSkeleton = mSkeletons.get(index);
 		mCurSkeleton.mScale = 1;
 		mCurCarrier = mCurSkeleton.mCarrier;
 		mCurAnimationSystem = mAnimationSystems.get(index);
 		mCurAnimationPlayer = mAnimationPlayers.get(index);
 		mSkeletonIndex = index;
 		selectAnimation(0);
 		refreshPhysics();
 	}
 
 	public void selectAnimation(int index) {
 		saveChangedPose();
 		mCurAnimation = mCurAnimationSystem.mAnimations.get(index);
 		mCurAnimation.mAutoAnimate = true;
 		mCurAnimationPlayer.setAnimation(mCurAnimation);
 		mAnimationIndex = index;
 		selectKeyFrame(0,true);
 //		mCurSkeleton.setJointAnimationsEnabled(mCurAnimation);
 	}
 
 	public void selectKeyFrame(int index,boolean apply) {
 		mCurFrame = mCurAnimation.mKeyFrames[index];
 		mFrameIndex = index;
 
 		if(!apply)
 			mCurAnimationPlayer.mCurrentAnimationTime = (float)mCurFrame.mFirstFrame/mCurAnimation.mFrameCount*mCurAnimationPlayer.mCurrentAnimation.mTotalDuration;
 		else{
 			mCurAnimationPlayer.setNormalizedAnimationTime((float)mCurFrame.mFirstFrame/mCurAnimation.mFrameCount);
 			if(!mCurAnimationPlayer.mCurrentAnimation.mAutoAnimate)
 				mCurFrame.mPose.applyPosture(mCurSkeleton);
 		}
 
 
 		float[] data = mCurFrame.mPose.mData;
 		int c = 0;
 		for(final Joint joint:mCurSkeleton.mJoints) {
 			if(joint.mAnimate) {
 				if(joint.mParent==null) {
 					float x = data[c++];
 					float y = data[c++];
 					joint.mAnimDisabled = x==Float.MAX_VALUE || y==Float.MAX_VALUE;
 				}else{
 					joint.mAnimDisabled = data[c++]==Float.MAX_VALUE;
 				}
 			}
 		}
 	}
 
 	public void reselect() {
 		selectKeyFrame(mFrameIndex,true);
 	}
 
 	public boolean selectSkeleton(Class<? extends CartoonSkeleton2D> skeletonClass) {
 		int i=0;
 		for(final CartoonSkeleton2D skeleton:mSkeletons) {
 			if(skeletonClass==skeleton.getClass()) {
 				selectSkeleton(i);
 				return true;
 			}
 			i++;
 		}
 		return false;
 	}
 
 	public boolean selectSkeleton(CartoonSkeleton2D skeleton) {
 		int i=0;
 		for(final CartoonSkeleton2D skel:mSkeletons) {
 			if(skel==skeleton) {
 				selectSkeleton(i);
 				return true;
 			}
 			i++;
 		}
 		return false;
 	}
 
 	public void centerCamera() {
 		if(mCurSkeleton==null)
 			mCamera.set(0,1,1.5f);
 		else{
 			mCurSkeleton.get2DBoundaries(mBoundaries);
 			mCamera.set(mBoundaries.getCenterX(), mBoundaries.getCenterY(), Math.max(mBoundaries.getWidth(),mBoundaries.getHeight())*1.1f);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public void play() {
 		if(mPlaying)
 			return;
 		saveChangedPose();
 		mPlaying = true;
 		mPaused = false;
 		mCurAnimationPlayer.setAnimation(mCurAnimation);
 	}
 
 	public void setPaused(boolean paused) {
 		mPaused = paused;
 	}
 
 	public boolean isPlaying() {
 		return mPlaying;
 	}
 
 	public boolean isPaused() {
 		return mPaused;
 	}
 
 	public void stop() {
 		mPlaying = false;
 		mPaused = false;
 		reselect();
 	}
 
 	public void previousFrame(boolean applyFrame) {
 		saveChangedPose();
 		if(mFrameIndex<=0)
 			selectKeyFrame(mCurAnimation.mKeyFrames.length-1,applyFrame);
 		else
 			selectKeyFrame(mFrameIndex-1,applyFrame);
 	}
 
 	public void nextFrame(boolean applyFrame) {
 		saveChangedPose();
 		if(mFrameIndex>=mCurAnimation.mKeyFrames.length-1)
 			selectKeyFrame(0,applyFrame);
 		else
 			selectKeyFrame(mFrameIndex+1,applyFrame);
 	}
 
 	@Override
 	public boolean rawEvent(YangEvent event) {
 		return false;
 	}
 
 	@Override
 	public void pointerMoved(float x, float y, SurfacePointerEvent event) {
 		if(mCurSkeleton==null)
 			return;
 		mCurPntX = mGraphics2D.normToWorldX(x,y);
 		mCurPntY = mGraphics2D.normToWorldY(x,y);	//TODO moved from pointerDown
 		mHoverJoint = mCurSkeleton.pickJoint2D(mCurPntX,mCurPntY);
 	}
 
 	@Override
 	public void pointerDown(float x,float y,SurfacePointerEvent event) {
 		mPrevPntX = mCurPntX;
 		mPrevPntY = mCurPntY;
 		mPrevSX = x;
 		mPrevSY = y;
 		switch(event.mButton) {
 		case SurfacePointerEvent.BUTTON_LEFT:
 			mSkeletonEditing.mMainMarkedJoint = mCurSkeleton.pickJoint2D(mCurPntX, mCurPntY);
 			if(mSkeletonEditing.mMainMarkedJoint!=null)
 				mSkeletonEditing.startDrag();
 			break;
 		case SurfacePointerEvent.BUTTON_MIDDLE:
 
 			break;
 		case SurfacePointerEvent.BUTTON_RIGHT:
 			final Joint pick = mCurSkeleton.pickJoint2D(mCurPntX, mCurPntY);
 			if(pick!=null)
 				pick.mFixed ^= true;
 			break;
 		}
 	}
 
 	@Override
 	public void pointerDragged(float x,float y,SurfacePointerEvent event) {
 
 		mPrevPntX = mCurPntX;
 		mPrevPntY = mCurPntY;
 		mCurPntX = mGraphics2D.normToWorldX(x,y);
 		mCurPntY = mGraphics2D.normToWorldY(x,y);
 		final float deltaX = mCurPntX-mPrevPntX;
 		final float deltaY = mCurPntY-mPrevPntY;
 
 		switch(event.mButton) {
 			case SurfacePointerEvent.BUTTON_LEFT:
 				if(mSkeletonEditing.mMainMarkedJoint!=null) {
 					mSkeletonEditing.mMainMarkedJoint.dragLocal(deltaX, deltaY, 0);
 					mPoseChanged = true;
 				}
 				break;
 			case SurfacePointerEvent.BUTTON_MIDDLE:
 				mCamera.move((mPrevSX-x)*mCamera.getZoom(),(mPrevSY-y)*mCamera.getZoom());
 				break;
 		}
 
 		mPrevSX = x;
 		mPrevSY = y;
 	}
 
 	@Override
 	public void pointerUp(float x,float y,SurfacePointerEvent event) {
 		if(mSkeletonEditing.mMainMarkedJoint!=null) {
 			mSkeletonEditing.mMainMarkedJoint.endDrag();
 			if(mSkeletonEditing.mMainMarkedJoint.mFixed)
 				mSkeletonEditing.mMainMarkedJoint.setVelocity(0,0);
 		}
 		mSkeletonEditing.mMainMarkedJoint = null;
 
 	}
 
 	private void shiftJoints(float shiftX,float shiftY) {
 		mCurSkeleton.shiftJoints(shiftX,shiftY);
 		for(KeyFrame keyFrame:mCurAnimation.mKeyFrames) {
			float[] data = keyFrame.mPose.mData;
			data[0] += shiftX;
			data[1] += shiftY;
 		}
 	}
 
 	@Override
 	public void keyDown(int code) {
 		if(code=='d') {
 			if(mHoverJoint!=null)
 				mHoverJoint.mAnimDisabled ^= true;
 			mPoseChanged = true;
 		}
 		float SHIFT = 0.02f;
 		if(code=='W')
 			shiftJoints(0,SHIFT);
 		if(code=='S')
 			shiftJoints(0,-SHIFT);
 		if(code=='A')
 			shiftJoints(-SHIFT,0);
 		if(code=='D')
 			shiftJoints(SHIFT,0);
 		mPoseChanged = true;
 	}
 
 	@Override
 	public void keyUp(int code) {
 
 	}
 
 	@Override
 	public void zoom(float factor) {
 		mCamera.zoom(factor);
 	}
 
 	public boolean isPhysicsActive() {
 		return mPhysicsMode;
 	}
 
 	public void setDrawSkeleton(boolean draw) {
 		mDrawSkeleton = draw;
 	}
 
 	public boolean isSkeletonDrawn() {
 		return mDrawSkeleton;
 	}
 
 	public CartoonSkeleton2D getCurrentSkeleton() {
 		return mCurSkeleton;
 	}
 
 	public Animation<?> getCurrentAnimation() {
 		return mCurAnimation;
 	}
 
 	@Override
 	public void sensorChanged(YangSensorEvent event) {
 
 	}
 }
