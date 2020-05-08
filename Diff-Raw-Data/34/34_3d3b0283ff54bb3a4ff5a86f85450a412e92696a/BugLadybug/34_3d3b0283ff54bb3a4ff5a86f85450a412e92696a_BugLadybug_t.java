 package org.anddev.andengine.pvb.bug;
 
 import org.anddev.andengine.pvb.singleton.GameData;
 
 public class BugLadybug extends Bug {
 	
 	public BugLadybug(final float y) {
 		super(y, GameData.getInstance().mBugLadybug);
 		
		this.mLife = 4;
		this.mDuration = 31f;
 	}
 	
 }
