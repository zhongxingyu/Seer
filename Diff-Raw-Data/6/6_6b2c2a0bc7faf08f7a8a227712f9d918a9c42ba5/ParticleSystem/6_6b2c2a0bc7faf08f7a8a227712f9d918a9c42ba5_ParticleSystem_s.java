 /**
  * Copyright 2010 Sean L. Mooney
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 /**
  *
  */
 package com.mooney_ware.gio.particles.model;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import android.graphics.PointF;
 import android.graphics.RectF;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 
 import com.mooney_ware.gio.particles.model.ParticleSystem.Particle;
 
 /**
  * @author Sean Mooney
  *
  */
 public class ParticleSystem implements Iterable<Particle> {
 
     RectF mSystemBounds;
     BoundsStrategy mLeftBoundStrat = PARTICLE_KILL;
     BoundsStrategy mRightBoundStrat = PARTICLE_KILL;
     BoundsStrategy mTopBoundStrat = PARTICLE_KILL;
     BoundsStrategy mBottomBoundStrat = PARTICLE_BOUNCE;
 
     List<Particle> mParticles = new ArrayList<ParticleSystem.Particle>();
  
     //step every 100 ms.
     int timeToStep = 100;
     
     final Handler moveHandler = new Handler();
     final Runnable moveRunner = new Runnable() {
         @Override
         public final void run() {
             Log.i("PS", "Iterating particles");
             stepAllParticles();
             moveHandler.postDelayed(this, timeToStep);
         }
     };
     
     /**
      * View that shows the particle system.
      * Will be invalidated when the particles are all stepped.
      */
     View updateNotifaction;
     
     /**
      * Add a new particle to the system.
      * @param size
      * @param loc
      * @param velocity
      */
     public void addParticle(int size, PointF loc, PointF vel){
         Particle p = new Particle(size, loc, vel);
         mParticles.add(p);
     }
 
     public void setSystemBounds(RectF systemBounds){
         mSystemBounds = systemBounds;
     }
 
     /**
      * Start the system running.
      * @param host view to invalidate after each step.
      */
     public void start(View host){
         updateNotifaction = host;
         moveHandler.post(moveRunner);
     }
     
     /**
      * Stop the system.
      * 
      */
     public void stop(){
         moveHandler.removeCallbacks(moveRunner);
         updateNotifaction = null;
     }
     
     /**
      * Step all the particles by 1.
      */
     public void stepAllParticles(){
         List<Particle> particles = mParticles;
         ArrayList<Particle> removeList = new ArrayList<Particle>();
         RectF sysBounds = mSystemBounds;
         
         if(sysBounds == null){
             Log.w("ParticleSystems", "NO SYSTEM BOUNDS");
             return;
         }
         
         for(Particle p : particles){
             p.step(1);
             RectF pBounds = new RectF(p.getLeftBound(), p.getTopBound(), p.getRightBound(), p.getBottomBound());
             
             if(sysBounds.left > pBounds.left){
                 onPassedBoundry(removeList, p, mLeftBoundStrat);
             }else if(sysBounds.right < pBounds.right){
                 onPassedBoundry(removeList, p, mRightBoundStrat);
             }else if(sysBounds.top > pBounds.top){
                 onPassedBoundry(removeList, p, mTopBoundStrat);
             }else if(sysBounds.bottom < pBounds.bottom){
                 onPassedBoundry(removeList, p, mBottomBoundStrat);
             }
         }
         mParticles.removeAll(removeList);
         
         if(updateNotifaction != null){
             updateNotifaction.postInvalidate();
         }
     }
 
     
     
     /**
      * @return the mLeftBoundStrat
      */
     public BoundsStrategy getLeftBoundStrat() {
         return mLeftBoundStrat;
     }
 
     /**
      * @param mLeftBoundStrat the mLeftBoundStrat to set
      */
     public void setmLeftBoundStrat(BoundsStrategy mLeftBoundStrat) {
         this.mLeftBoundStrat = mLeftBoundStrat;
     }
 
     /**
      * @return the mRightBoundStrat
      */
     public BoundsStrategy getRightBoundStrat() {
         return mRightBoundStrat;
     }
 
     /**
      * @param mRightBoundStrat the mRightBoundStrat to set
      */
     public void setRightBoundStrat(BoundsStrategy mRightBoundStrat) {
         this.mRightBoundStrat = mRightBoundStrat;
     }
 
     /**
      * @return the mTopBoundsStrat
      */
     public BoundsStrategy getTopBoundStrat() {
         return mTopBoundStrat;
     }
 
     /**
      * @param mTopBoundsStrat the mTopBoundsStrat to set
      */
     public void setTopBoundStrat(BoundsStrategy mTopBoundsStrat) {
         this.mTopBoundStrat = mTopBoundsStrat;
     }
 
     /**
      * @return the mBottomBoundsStrat
      */
     public BoundsStrategy getBottomBoundStrat() {
         return mBottomBoundStrat;
     }
 
     /**
      * @param mBottomBoundsStrat the mBottomBoundsStrat to set
      */
     public void setmBottomBoundsStrat(BoundsStrategy mBottomBoundsStrat) {
         this.mBottomBoundStrat = mBottomBoundsStrat;
     }
 
     private void onPassedBoundry(List<Particle> removeList, Particle p, BoundsStrategy strat){
         switch(strat.atBoundery()){
             case DIE:
             {
                Log.i("PS", "Removing " + p + " from the system");
                 removeList.add(p);
             }
                 break;
             case REFLECT:
             {
                 PointF vel = p.mVelocity;
                vel.x = -vel.x;
                 vel.y = -vel.y;
             }
                 break;
         }
     }
     
     
     /**
      * @author Sean Mooney
      *
      */
     public class Particle {
 
         /**
          * Radius of the particle
          */
         private float mSize;
         /**
          * Center of the point.
          */
         private PointF mLocation;
         private PointF mVelocity;
 
         public Particle(float size, PointF location, PointF velocity){
             mSize = size;
             mLocation = location;
             mVelocity = velocity;
         }
 
         public void setSize(float size){
             mSize = size;
         }
         
         public float getSize(){
             return mSize;
         }
         
         public PointF getLocation(){
             return mLocation;
         }
         
         public float getLeftBound(){
             return mLocation.x - mSize;
         }
 
         public float getRightBound(){
             return mLocation.x + mSize;
         }
 
         public float getTopBound(){
             return mLocation.y + mSize;
         }
 
         public float getBottomBound(){
             return mLocation.y - mSize;
         }
 
         public void step(int dT){
             PointF loc = mLocation;
             PointF vel = mVelocity;
             
             loc.x += dT *vel.x;
             loc.y += dT *vel.y;
             
         }
     }
 
     public static final BoundsStrategy PARTICLE_KILL = new BoundsStrategy() {
         @Override
         public ParticleResult atBoundery() {
             return ParticleResult.DIE;
         }
     };
 
     public static final BoundsStrategy PARTICLE_BOUNCE = new BoundsStrategy() {
         @Override
         public ParticleResult atBoundery() {
             return ParticleResult.REFLECT;
         }
     };
 
     public static interface BoundsStrategy{
         public enum ParticleResult{
             DIE,
             REFLECT
         }
         public ParticleResult atBoundery();
     }
 
     /* (non-Javadoc)
      * @see java.lang.Iterable#iterator()
      */
     @Override
     public Iterator<Particle> iterator() {
         return mParticles.listIterator();
     }
 }
