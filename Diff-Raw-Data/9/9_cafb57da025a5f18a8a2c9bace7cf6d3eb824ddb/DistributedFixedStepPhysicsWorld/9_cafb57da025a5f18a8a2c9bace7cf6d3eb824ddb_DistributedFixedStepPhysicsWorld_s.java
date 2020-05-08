 package com.jjaz.aetherflames.physics;
 
 import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.World;
 import com.jjaz.aetherflames.ClientGameManager;
 
 public class DistributedFixedStepPhysicsWorld extends FixedStepPhysicsWorld {
 	
 	private boolean mStepInProgress;
 	private final float mTimeStep;
 	private float mSecondsElapsedAccumulator;
 	
 	private ClientGameManager mClientGameManager;
 	
 	public DistributedFixedStepPhysicsWorld(final int pStepsPerSecond, final Vector2 pGravity, final boolean pAllowSleep, final int pVelocityIterations, final int pPositionIterations, final ClientGameManager pCGM) {
 		super(pStepsPerSecond, pGravity, pAllowSleep, pVelocityIterations, pPositionIterations);
 		this.mTimeStep = 1.0f / pStepsPerSecond;
 		this.mClientGameManager = pCGM;
 		this.mSecondsElapsedAccumulator = 0;
 		this.mStepInProgress = false;
 	}
 	
 	public void step() {
 		final int velocityIterations = this.mVelocityIterations;
 		final int positionIterations = this.mPositionIterations;
 		
 		final World world = this.mWorld;
 		final float stepLength = this.mTimeStep;
 		
 		world.step(stepLength, velocityIterations, positionIterations);
 		
 		this.mSecondsElapsedAccumulator = 0;
 		this.mStepInProgress = false;
 	}
 	
 	@Override
 	public void onUpdate(final float pSecondsElapsed) {
 		this.mRunnableHandler.onUpdate(pSecondsElapsed);
 		this.mSecondsElapsedAccumulator += pSecondsElapsed;
 		
 		final float stepLength = this.mTimeStep;
 		
 		if (this.mSecondsElapsedAccumulator >= stepLength && !mStepInProgress) {
 			this.mStepInProgress = true;
			// this.mClientGameManager.sendUpdates();
 		}
 		
 		this.mPhysicsConnectorManager.onUpdate(pSecondsElapsed);
 	}
 }
