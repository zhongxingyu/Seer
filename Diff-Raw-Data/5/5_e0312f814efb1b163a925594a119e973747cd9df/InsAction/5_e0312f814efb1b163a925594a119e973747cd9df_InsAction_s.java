 package com.inspedio.entity;
 
 import com.inspedio.entity.primitive.InsCallback;
 import com.inspedio.system.helper.InsUtil;
 
 /**
  * <code>InsAction</code> is an action that can be applied to <code>InsBasic</code> object.<br>
  * Its main purpose is to make smooth transition for Moving Object, Scaling, Fading, etc.<br>
  * Scale and Fade can only be used to <code>InsShape</code>. <br>
  * You should call <code>setAction(InsAction)</code> to make it works.<br>
  * 
  * @author Hyude
  * @version 1.0
  */
 public abstract class InsAction {
 	public boolean autoDestroy;
 	public boolean active;
 	protected int frameCount;
 	protected int remainingCount;
 	protected InsCallback callback;
 	protected InsBasic target;
 
 	protected InsAction(int FrameCount, InsCallback Callback){
 		this.callback = Callback;
 		this.remainingCount = this.frameCount = InsUtil.Max(0, FrameCount - 1);
 		this.active = true;
 		this.autoDestroy = true;
 	}
 	
 	public void setTarget(InsBasic Target){
 		this.target = Target;
 	}
 	
 	/**
 	 * Doing Action Iteration.
 	 * 
 	 * @return	frameCount remaining. return -1 if action has ended. 
 	 */
 	public abstract int act();
 	
 	public void reset(){
 		this.remainingCount = this.frameCount;
 		this.active = true;
 	}
 	
 	public int getRemainingCount(){
 		return this.remainingCount;
 	}
 	
 	protected void finishAction(){
 		if(this.callback != null){
 			this.callback.call();
 		}
 		remainingCount = -1;
 		active = false;
 		if(this.autoDestroy){
			this.target.unsetAction(this);
 			this.destroy();
 		}
 	}
 	
 	public void destroy(){
 		this.callback = null;
 		this.target = null;
 	}
 	
 }
