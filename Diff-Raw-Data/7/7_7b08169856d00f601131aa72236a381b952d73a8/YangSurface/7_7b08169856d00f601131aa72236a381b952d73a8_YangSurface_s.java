 package yang.graphics;
 
 import java.io.FileNotFoundException;
 
 import yang.events.EventQueueHolder;
 import yang.events.YangEventQueue;
 import yang.events.listeners.YangEventListener;
 import yang.events.macro.AbstractMacroIO;
 import yang.events.macro.DefaultMacroIO;
 import yang.events.macro.MacroExecuter;
 import yang.events.macro.MacroWriter;
 import yang.graphics.defaults.DefaultGraphics;
 import yang.graphics.font.BitmapFont;
 import yang.graphics.interfaces.InitializationCallback;
 import yang.graphics.model.GFXDebug;
 import yang.graphics.translator.AbstractGFXLoader;
 import yang.graphics.translator.GraphicsTranslator;
 import yang.model.App;
 import yang.model.DebugYang;
 import yang.model.enums.UpdateMode;
 import yang.sound.SoundManager;
 import yang.systemdependent.AbstractResourceManager;
 import yang.systemdependent.AbstractVibrator;
 import yang.util.StringsXML;
 import yang.util.Util;
 
 public abstract class YangSurface implements EventQueueHolder {
 
 	public static boolean CATCH_EXCEPTIONS = true;
 
 	public final static int RUNTIME_STATE_RUNNING = 0;
 	public final static int RUNTIME_STATE_PAUSED = 1;
 	public final static int RUNTIME_STATE_STOPPED = 2;
 
 	public GraphicsTranslator mGraphics;
 	public StringsXML mStrings;
 	public AbstractResourceManager mResources;
 	public AbstractGFXLoader mGFXLoader;
 	public SoundManager mSounds;
 	public AbstractVibrator mVibrator;
 	public GFXDebug mGFXDebug;
 	public String mPlatformKey = "";
 
 	private UpdateMode mUpdateMode;
 	protected boolean mInitialized = false;
 	protected Object mInitializedNotifier;
 	protected InitializationCallback mInitCallback;
 
 	protected long mCatchUpTime;
 	public double mProgramTime;
 	public int mStepCount;
 	public static float deltaTimeSeconds;
 	protected static long deltaTimeNanos;
 	protected int mUpdateWaitMillis = 1000/70;
 	protected int mRuntimeState = 0;
 	protected boolean mInactive = false;
 
 	private Thread mUpdateThread = null;
 	public YangEventListener mEventListener;
 	public YangEventListener mMetaEventListener;
 	public YangEventQueue mEventQueue;
 	public int mDebugSwitchKey = -1;
 	public boolean mPaused = false;
 	public float mPlaySpeed = 1;
 	public String mMacroFilename;
 	public boolean mException = false;
 	private int mStartupSteps = 1;
 	private int mLoadingSteps = 1;
 	private int mLoadingState = 0;
 	private int mInitSteps = 0;
 	private boolean mResuming = false;
 	private boolean mLoadedOnce = false;
 
 	public MacroExecuter mMacro;
 	public DefaultMacroIO mDefaultMacroIO;
 
 	/**
 	 * GL-Thread
 	 */
 	protected abstract void initGraphics();
 	protected abstract void draw();
 
 	protected void onException(Exception ex) {
 
 	}
 
 	//Optional methods
 	protected void postInitGraphics() { }
 	protected void initGraphicsForResume() { }
 	protected void onLoadingInterrupted(boolean resuming) { }
 
 	public YangSurface() {
 		mInitializedNotifier = new Object();
 		mCatchUpTime = 0;
 		mEventQueue = new YangEventQueue(getMaxEventCount());
 		setUpdatesPerSecond(120);
 		mUpdateMode = UpdateMode.SYNCHRONOUS;
 		mMacroFilename = null;
 	}
 
 	protected void setStartupSteps(int loadingSteps,int initSteps) {
 		mLoadingSteps = loadingSteps;
 		mInitSteps = initSteps;
 		mStartupSteps = initSteps+loadingSteps;
 	}
 
 	protected void setStartupSteps(int loadingSteps) {
 		setStartupSteps(loadingSteps,0);
 	}
 
 	protected void exceptionOccurred(Exception ex) {
 		try{
 			mEventQueue.close();
 			if(mMacro!=null)
 				mMacro.close();
 		}catch(Exception ex2) {
 
 		}
 		onException(ex);
 		if(!CATCH_EXCEPTIONS) {
 			if(ex instanceof RuntimeException)
 				throw (RuntimeException)ex;
 			else
 				throw new RuntimeException(ex);
 		}
 		mException = true;
 		mPaused = true;
 		if(mGFXDebug!=null)
 			mGFXDebug.setErrorString(ex.getClass()+": "+ex.getMessage()+"\n\n"+Util.arrayToString(ex.getStackTrace(),"\n").replace("(", " ("));
 		ex.printStackTrace();
 	}
 
 	public void setMacroFilename(String filename) {
 		mMacroFilename = filename;
 	}
 
 	@Override
 	public YangEventQueue getEventQueue() {
 		return mEventQueue;
 	}
 
 	protected int getMaxEventCount() {
 		return 2048;
 	}
 
 	public void setGraphics(GraphicsTranslator graphics) {
 		mGraphics = graphics;
 	}
 
 	protected boolean assertMessage() {
 		System.out.println("ASSERTIONS ARE ACTIVATED");
 		return true;
 	}
 
 	public void recordMacro(String filename,AbstractMacroIO macroIO) throws FileNotFoundException {
 		MacroWriter writer = new MacroWriter(mResources.getFileSystemOutputStream(filename), mDefaultMacroIO);
 		mEventQueue.registerEventWriter(writer);
 	}
 
 	public void recordMacro(String filename) throws FileNotFoundException {
 		recordMacro(filename,mDefaultMacroIO);
 	}
 
 	public void playMacro(String filename,AbstractMacroIO macroIO) {
 		try {
 			mMacro = new MacroExecuter(mResources.getFileSystemInputStream(filename), macroIO);
 		} catch (FileNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void playMacro(String filename) {
 		playMacro(filename,mDefaultMacroIO);
 	}
 
 	public void onSurfaceCreated() {
 		if(mInitialized) {
 			DebugYang.println("ALREADY INITIALIZED");
 			return;
 		}
 		mProgramTime = 0;
 		mStepCount = 0;
 		assert assertMessage();
 		mGraphics.init();
 		mGFXLoader = mGraphics.mGFXLoader;
 		mResources = mGraphics.mGFXLoader.mResources;
 		mSounds = App.soundManager;
 		mEventQueue.setGraphics(mGraphics);
 		if(mResources.fileExists("strings/strings.xml"))
 			mStrings = new StringsXML(mResources.getInputStream("strings/strings.xml"));
 		try{
 			initGraphics();
 
 			mDefaultMacroIO = new DefaultMacroIO(this);
 			if(mMacroFilename!=null && mResources.fileExistsInFileSystem(mMacroFilename))
 				playMacro(mMacroFilename);
 			if(mMacro==null && DebugYang.AUTO_RECORD_MACRO) {
 				String filename = "run.ym";
 				try {
 					recordMacro(filename);
 				} catch (FileNotFoundException e) {
 					DebugYang.printerr("Could not create '"+filename+"'");
 				}
 			}
 
 			
 			postInitGraphics();
 
 			if(mInitCallback!=null)
 				mInitCallback.initializationFinished();
 			mInitialized = true;
 			synchronized(mInitializedNotifier) {
 				mInitializedNotifier.notifyAll();
 			}
 
 			if(mUpdateMode == UpdateMode.ASYNCHRONOUS)
 				mUpdateThread.start();
 		}catch(Exception ex) {
 			exceptionOccurred(ex);
 		}
 
 	}
 
 	public void waitUntilInitialized() {
 		synchronized(mInitializedNotifier) {
 			try {
 				mInitializedNotifier.wait();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void onSurfaceChanged(int width,int height) {
 		try{
 			mGraphics.setSurfaceSize(width, height);
 			if(mGFXDebug!=null)
 				mGFXDebug.surfaceChanged();
 		}catch(Exception ex) {
 			exceptionOccurred(ex);
 		}
 	}
 
 	public boolean isInitialized() {
 		return mInitialized;
 	}
 
 	public void setInitializationCallback(InitializationCallback initCallback) {
 		mInitCallback = initCallback;
 	}
 
 	public static void setUpdatesPerSecond(int updatesPerSecond) {
 		deltaTimeSeconds = 1f/updatesPerSecond;
 		deltaTimeNanos = 1000000000/updatesPerSecond;
 	}
 
 	public void setUpdateMode(UpdateMode updateMode) {
 		mUpdateMode = updateMode;
 		if(mUpdateMode==UpdateMode.ASYNCHRONOUS) {
 			mUpdateThread = new Thread() {
 				@Override
 				public void run() {
 
 					while(true) {
 						try {
 							if(mRuntimeState>0) {
 								Thread.sleep(100);
 								continue;
 							}else
 								Thread.sleep(mUpdateWaitMillis);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						catchUp();
 					}
 				}
 			};
 		}
 	}
 
 	protected void initDebugOutput(DefaultGraphics<?> graphics, BitmapFont font) {
 		mGFXDebug = new GFXDebug(this,graphics,font);
 		mGFXDebug.surfaceChanged();
 	}
 
 	protected boolean isLoadingFinished() {
 		return mLoadingState>=mStartupSteps;
 	}
 
 	private void handleEvents() {
 		if(mEventListener!=null) {
 			if(!mPaused && mLoadingState>=mStartupSteps)
 				mEventQueue.handleEvents(mEventListener);
 			else
 				mEventQueue.clearEvents();
 		}
 	}
 
 	protected void catchUp() {
 //		if(alt) {
 //		alt = false;
 //		return;
 //	}
 //	alt = true;
 
 		if(!mInitialized || mRuntimeState>0 || mLoadingState<mStartupSteps)
 			return;
 		if(mCatchUpTime==0)
 			mCatchUpTime = System.nanoTime()-1;
 
 		if(mPlaySpeed==0) {
 			handleEvents();
 			mCatchUpTime = 0;
 		}else{
 			while(mCatchUpTime<System.nanoTime()) {
 				mCatchUpTime += (long)(deltaTimeNanos*mPlaySpeed);
 				proceed();
 			}
 		}
 	}
 	boolean alt = false;
 	public void proceed() {
 
 		if(!mPaused && !mException) {
 			try{
 				if(mMacro!=null)
 					mMacro.step();
 				handleEvents();
 				if(mLoadingState>=mStartupSteps) {
 					mStepCount ++;
 					mProgramTime += deltaTimeSeconds;
 					step(deltaTimeSeconds);
 				}
 			}catch(Exception ex){
 				exceptionOccurred(ex);
 			}
 		}
 	}
 
 	protected void step(float deltaTime) {
 
 	}
 
 	protected void prepareLoading(boolean resuming) {
 		//if(resuming)
 			mGFXLoader.reenqueueTextures();
 		if(mStartupSteps>0)
 			mGFXLoader.divideQueueLoading(mStartupSteps-1);
 		mGFXLoader.mEnqueueMode = false;
 	}
 
 	protected void loadAssets(int loadState,boolean resuming) {
 		if(loadState>0 || mStartupSteps==1)
 			mGFXLoader.loadEnqueuedTextures();
 
 		mGraphics.unbindTextures();
 	}
 
 	protected void initializeAssets(int initStep,boolean resuming) {
 		System.out.println(mLoadingState+" "+initStep);
 	}
 
 
 	protected void drawLoadingScreen(int loadState,float progress,boolean resuming) {
 
 	}
 
 	protected void onLoadingFinished(boolean resuming) {
 
 	}
 
 	public final void drawFrame() {
 		mGraphics.clear(0,0,0);
 		try{
 			if(mMetaEventListener!=null)
 				mEventQueue.handleMetaEvents(mMetaEventListener);
 
 			if(mInactive) {
 				mGraphics.beginFrame();
 				mGraphics.clear(0,0,0);
 				mGraphics.endFrame();
 				return;
 			}
 
 			if(mException) {
 				mGraphics.beginFrame();
 				mGraphics.clear(0.1f,0,0);
 				if(mGFXDebug!=null)
 					mGFXDebug.draw();
 				mGraphics.endFrame();
 				return;
 			}
 
 			if(mRuntimeState>0 && !mResuming) {
 				mResuming = mLoadedOnce;
 				mGraphics.restart();
 				if(mGraphics.mCurDrawListener!=null)
 					mGraphics.mCurDrawListener.onRestartGraphics();
 			}
 
 			if(mUpdateMode==UpdateMode.SYNCHRONOUS)
 				catchUp();
 
 			if(mLoadingState>=mStartupSteps && DebugYang.DEBUG_LEVEL>0 && mGFXDebug!=null) {
 				mGFXDebug.reset();
 				if(DebugYang.DRAW_GFX_VALUES)
 					mGFXDebug.printGFXDebugValues();
 			}
 			mGraphics.beginFrame();
			DebugYang.DRAW_GFX_VALUES = true;
 			if(mLoadingState>=mStartupSteps) {
 
 				draw();
 				if(DebugYang.DEBUG_LEVEL>0 && mGFXDebug!=null) {
 					mGFXDebug.draw();
 				}
 			}else{
 				if(mLoadingState==0)
 					prepareLoading(mResuming);
 				if(mLoadingState>=mLoadingSteps)
 					initializeAssets(mLoadingState-mLoadingSteps,mResuming);
 				else
 					loadAssets(mLoadingState,mResuming);
 				if(mLoadingState==mStartupSteps-1) {
 					onLoadingFinished(mResuming);
 					mLoadedOnce = true;
 					mEventQueue.clearEvents();
 					mRuntimeState = 0;
 					mResuming = false;
 					draw();
 				}else{
 					float progress;
 					if(mStartupSteps==2)
 						progress = 1;
 					else
 						progress = (float)mLoadingState/(mStartupSteps-2);
 					drawLoadingScreen(mLoadingState,progress,mResuming);
 				}
 				mCatchUpTime = 0;
 				mLoadingState++;
 			}
 			mGraphics.endFrame();
 
 		}catch(Exception ex){
 			exceptionOccurred(ex);
 		}
 
 	}
 
 	public void stop() {
 		mInactive = true;
 		mRuntimeState = 2;
 		mLoadingState = 0;
 	}
 
 	/**
 	 * Non-GL-Thread!
 	 */
 	public void pause() {
 //		if(mUpdateThread!=null)
 //			synchronized (mUpdateThread) {
 //				mUpdateThread.suspend();
 //			}
 		mInactive = true;
 		mRuntimeState = 1;
 		mCatchUpTime = 0;
 		mLoadingState = 0;
 		if(!mLoadedOnce) {
 			onLoadingInterrupted(false);
 		}
 		if(mResuming)
 			onLoadingInterrupted(true);
 	}
 
 	/**
 	 * Non-GL-Thread!
 	 */
 	public void destroy() {
 		mGraphics.mGFXLoader.deleteTextures();
 	}
 
 	/**
 	 * Non-GL-Thread!
 	 */
 	public void resume() {
 //		if(mUpdateThread!=null && mUpdateThread.isAlive())
 //			synchronized (mUpdateThread) {
 //				mUpdateThread.resume();
 //			}
 		mInactive = false;
 		mCatchUpTime = 0;
 		mResuming = false;
 		mLoadingState = 0;
 	}
 
 	public void setPaused(boolean paused) {
 		mPaused = paused;
 		mEventQueue.mMetaMode = paused;
 	}
 
 	public boolean isPaused() {
 		return mPaused;
 	}
 
 	public void exit() {
 		mEventQueue.close();
 		if(mMacro!=null)
 			mMacro.close();
 		System.exit(0);
 	}
 
 	public void handleArgs(String[] args,int startIndex) {
 		if(args==null)
 			return;
 		if(args.length>=1)
 			setMacroFilename(args[0]);
 	}
 
 	public void simulatePause() {
 		pause();
 		stop();
 		mGraphics.deleteAllTextures();
 	}
 
 	public void simulateResume() {
 		if(mInactive)
 			resume();
 	}
 	public boolean isInactive() {
 		return mInactive;
 	}
 
 }
